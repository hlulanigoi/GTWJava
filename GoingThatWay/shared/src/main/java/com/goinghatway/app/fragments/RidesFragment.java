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

import com.goinghatway.app.activities.RequestRideActivity;
import com.goinghatway.app.activities.RideDetailActivity;
import com.goinghatway.app.adapters.RideAdapter;
import com.goinghatway.app.databinding.FragmentRidesBinding;
import com.goinghatway.app.utils.Constants;
import com.goinghatway.app.viewmodels.RideViewModel;

public class RidesFragment extends Fragment {

    private FragmentRidesBinding binding;
    private RideViewModel viewModel;
    private RideAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRidesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RideViewModel.class);

        setupRecycler();
        loadMyRides();

        binding.fabBookRide.setOnClickListener(v ->
                startActivityForResult(
                        new Intent(requireContext(), RequestRideActivity.class),
                        Constants.RC_CREATE_RIDE));

        binding.swipeRefresh.setOnRefreshListener(this::loadMyRides);
    }

    private void setupRecycler() {
        adapter = new RideAdapter(ride -> {
            Intent intent = new Intent(requireContext(), RideDetailActivity.class);
            intent.putExtra(Constants.EXTRA_RIDE_ID, ride.getId());
            startActivity(intent);
        });
        binding.rvRides.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRides.setAdapter(adapter);
    }

    private void loadMyRides() {
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.getMyRides().observe(getViewLifecycleOwner(), response -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);
            if (response != null && response.isSuccess() && response.getData() != null) {
                adapter.setRides(response.getData());
                binding.tvEmpty.setVisibility(
                        response.getData().isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.RC_CREATE_RIDE &&
                resultCode == android.app.Activity.RESULT_OK) {
            loadMyRides();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
