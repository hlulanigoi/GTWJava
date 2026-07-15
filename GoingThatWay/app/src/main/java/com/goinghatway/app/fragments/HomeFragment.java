package com.goinghatway.app.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.goinghatway.app.activities.ParcelDetailActivity;
import com.goinghatway.app.adapters.ParcelAdapter;
import com.goinghatway.app.databinding.FragmentHomeBinding;
import com.goinghatway.app.utils.Constants;
import com.goinghatway.app.utils.OsmMapUtils;
import com.goinghatway.app.utils.SessionManager;
import com.goinghatway.app.viewmodels.ParcelViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private ParcelViewModel viewModel;
    private ParcelAdapter adapter;
    private FusedLocationProviderClient fusedLocationClient;
    private double userLat = OsmMapUtils.SA_LAT;
    private double userLng = OsmMapUtils.SA_LNG;

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    OsmMapUtils.addMyLocation(binding.mapView, requireContext());
                    fetchLastLocationThenLoadParcels();
                } else {
                    loadParcels(); // fall back to default region
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ParcelViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        SessionManager session = new SessionManager(requireContext());
        binding.tvWelcome.setText("Hi, " + session.getUserName() + "!");

        setupMap();
        setupRecycler();
        requestLocationThenLoad();

        binding.swipeRefresh.setOnRefreshListener(this::requestLocationThenLoad);
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationThenLoad() {
        if (hasLocationPermission()) {
            fetchLastLocationThenLoadParcels();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @SuppressWarnings("MissingPermission") // guarded by hasLocationPermission() at every call site
    private void fetchLastLocationThenLoadParcels() {
        if (!hasLocationPermission()) {
            loadParcels();
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLat = location.getLatitude();
                userLng = location.getLongitude();
            }
            loadParcels();
        }).addOnFailureListener(e -> loadParcels());
    }

    private void setupMap() {
        OsmMapUtils.configure(binding.mapView);
        OsmMapUtils.centerOn(binding.mapView,
                OsmMapUtils.SA_LAT, OsmMapUtils.SA_LNG, OsmMapUtils.DEFAULT_ZOOM);
        if (hasLocationPermission()) {
            OsmMapUtils.addMyLocation(binding.mapView, requireContext());
        }
    }

    private void setupRecycler() {
        adapter = new ParcelAdapter(parcel -> {
            Intent intent = new Intent(requireContext(), ParcelDetailActivity.class);
            intent.putExtra(Constants.EXTRA_PARCEL_ID, parcel.getId());
            startActivity(intent);
        });
        binding.rvNearbyParcels.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvNearbyParcels.setAdapter(adapter);
    }

    private void loadParcels() {
        if (binding == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.getParcels(1, "PENDING", userLat, userLng, 50)
                .observe(getViewLifecycleOwner(), response -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    if (response != null && response.isSuccess() && response.getData() != null) {
                        adapter.setParcels(response.getData().getData());
                        boolean empty = response.getData().getData() == null
                                || response.getData().getData().isEmpty();
                        binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                    }
                });
    }

    @Override
    public void onResume() { super.onResume(); if (binding != null) binding.mapView.onResume(); }

    @Override
    public void onPause() { super.onPause(); if (binding != null) binding.mapView.onPause(); }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
