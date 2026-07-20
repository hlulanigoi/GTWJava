package com.goinghatway.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.goinghatway.app.activities.PostTripActivity;
import com.goinghatway.app.activities.TripDetailActivity;
import com.goinghatway.app.adapters.TripAdapter;
import com.goinghatway.app.databinding.FragmentTripsBinding;
import com.goinghatway.app.utils.Constants;
import com.goinghatway.app.viewmodels.TripViewModel;

public class TripsFragment extends Fragment {

    private FragmentTripsBinding binding;
    private TripViewModel viewModel;
    private TripAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTripsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TripViewModel.class);

        setupRecycler();
        loadMyTrips();

        binding.fabPostTrip.setOnClickListener(v ->
                startActivityForResult(
                        new Intent(requireContext(), PostTripActivity.class),
                        Constants.RC_POST_TRIP));

        binding.swipeRefresh.setOnRefreshListener(this::loadMyTrips);
    }

    private void setupRecycler() {
        adapter = new TripAdapter(trip -> {
            Intent intent = new Intent(requireContext(), TripDetailActivity.class);
            intent.putExtra(Constants.EXTRA_TRIP_ID, trip.getId());
            startActivity(intent);
        });
        binding.rvTrips.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTrips.setAdapter(adapter);
    }

    private void loadMyTrips() {
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.getMyTrips().observe(getViewLifecycleOwner(), response -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);
            if (response != null && response.isSuccess() && response.getData() != null) {
                adapter.setTrips(response.getData());
                binding.tvEmpty.setVisibility(
                        response.getData().isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.RC_POST_TRIP &&
                resultCode == android.app.Activity.RESULT_OK) {
            loadMyTrips();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
