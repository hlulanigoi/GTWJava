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

import com.goinghatway.app.adapters.admin.AdminParcelAdapter;
import com.goinghatway.app.databinding.FragmentAdminParcelsBinding;
import com.goinghatway.app.models.Parcel;
import com.goinghatway.app.viewmodels.admin.AdminViewModel;

import java.util.List;

public class AdminParcelsFragment extends Fragment implements AdminParcelAdapter.Listener {

    private FragmentAdminParcelsBinding binding;
    private AdminViewModel viewModel;
    private AdminParcelAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminParcelsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        adapter = new AdminParcelAdapter(this);
        binding.rvParcels.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvParcels.setAdapter(adapter);

        String[] statuses = {"ALL", "PENDING", "MATCHED", "COLLECTED", "DELIVERED", "CANCELLED"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, statuses);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerStatus.setAdapter(spinnerAdapter);
        binding.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                String status = pos == 0 ? null : statuses[pos];
                loadParcels(status);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.swipeRefresh.setOnRefreshListener(() -> loadParcels(null));
        loadParcels(null);
    }

    private void loadParcels(String status) {
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.loadParcels(1, status).observe(getViewLifecycleOwner(), response -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);
            if (response != null && response.isSuccess() && response.getData() != null) {
                List<Parcel> parcels = response.getData().getData();
                adapter.setParcels(parcels);
                binding.tvEmpty.setVisibility(parcels == null || parcels.isEmpty()
                        ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onCancelParcel(Parcel parcel) {
        viewModel.updateParcelStatus(parcel.getId(), "CANCELLED")
                .observe(getViewLifecycleOwner(), r -> {
                    Toast.makeText(requireContext(),
                            r != null && r.isSuccess() ? "Parcel cancelled" : "Failed",
                            Toast.LENGTH_SHORT).show();
                    if (r != null && r.isSuccess()) loadParcels(null);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
