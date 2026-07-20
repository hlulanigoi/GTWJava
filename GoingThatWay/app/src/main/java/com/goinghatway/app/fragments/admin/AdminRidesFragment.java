package com.goinghatway.app.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.goinghatway.app.adapters.admin.AdminRideAdapter;
import com.goinghatway.app.databinding.FragmentAdminRidesBinding;
import com.goinghatway.app.models.Ride;
import com.goinghatway.app.viewmodels.admin.AdminViewModel;

import java.util.List;

public class AdminRidesFragment extends Fragment implements AdminRideAdapter.Listener {

    private FragmentAdminRidesBinding binding;
    private AdminViewModel viewModel;
    private AdminRideAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminRidesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        adapter = new AdminRideAdapter(this);
        binding.rvRides.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRides.setAdapter(adapter);

        String[] statuses = {"ALL", "PENDING", "MATCHED", "EN_ROUTE", "COMPLETED", "CANCELLED"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, statuses);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerStatus.setAdapter(spinnerAdapter);
        binding.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                String status = pos == 0 ? null : statuses[pos];
                loadRides(status);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.swipeRefresh.setOnRefreshListener(() -> loadRides(null));
        loadRides(null);
    }

    private void loadRides(String status) {
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.loadRides(1, status).observe(getViewLifecycleOwner(), response -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);
            if (response != null && response.isSuccess() && response.getData() != null) {
                List<Ride> rides = response.getData().getData();
                adapter.setRides(rides);
                binding.tvEmpty.setVisibility(rides == null || rides.isEmpty()
                        ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onCancelRide(Ride ride) {
        viewModel.updateRideStatus(ride.getId(), "CANCELLED")
                .observe(getViewLifecycleOwner(), r -> {
                    Toast.makeText(requireContext(),
                            r != null && r.isSuccess() ? "Ride cancelled" : "Failed",
                            Toast.LENGTH_SHORT).show();
                    if (r != null && r.isSuccess()) loadRides(null);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
