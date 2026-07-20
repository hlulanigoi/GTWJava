package com.goinghatway.app.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.goinghatway.app.adapters.admin.AdminDriverAdapter;
import com.goinghatway.app.databinding.FragmentAdminDriversBinding;
import com.goinghatway.app.models.User;
import com.goinghatway.app.viewmodels.admin.AdminViewModel;

public class AdminDriversFragment extends Fragment implements AdminDriverAdapter.Listener {

    private FragmentAdminDriversBinding binding;
    private AdminViewModel viewModel;
    private AdminDriverAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminDriversBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        adapter = new AdminDriverAdapter(this);
        binding.rvDrivers.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDrivers.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(this::loadApplications);
        loadApplications();
    }

    private void loadApplications() {
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.loadPendingDriverApplications().observe(getViewLifecycleOwner(), response -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);
            if (response != null && response.isSuccess() && response.getData() != null) {
                adapter.setDrivers(response.getData());
                binding.tvEmpty.setVisibility(
                        response.getData().isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onApprove(User user) {
        viewModel.approveDriver(user.getId()).observe(getViewLifecycleOwner(), r -> {
            Toast.makeText(requireContext(),
                    r != null && r.isSuccess() ? "Driver approved" : "Failed",
                    Toast.LENGTH_SHORT).show();
            if (r != null && r.isSuccess()) loadApplications();
        });
    }

    @Override
    public void onReject(User user) {
        viewModel.rejectDriver(user.getId()).observe(getViewLifecycleOwner(), r -> {
            Toast.makeText(requireContext(),
                    r != null && r.isSuccess() ? "Application rejected" : "Failed",
                    Toast.LENGTH_SHORT).show();
            if (r != null && r.isSuccess()) loadApplications();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
