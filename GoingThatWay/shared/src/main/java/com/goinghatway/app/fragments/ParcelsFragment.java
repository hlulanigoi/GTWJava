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

import com.goinghatway.app.activities.CreateParcelActivity;
import com.goinghatway.app.activities.ParcelDetailActivity;
import com.goinghatway.app.adapters.ParcelAdapter;
import com.goinghatway.app.databinding.FragmentParcelsBinding;
import com.goinghatway.app.utils.Constants;
import com.goinghatway.app.viewmodels.ParcelViewModel;

public class ParcelsFragment extends Fragment {

    private FragmentParcelsBinding binding;
    private ParcelViewModel viewModel;
    private ParcelAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentParcelsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ParcelViewModel.class);

        setupRecycler();
        loadMyParcels();

        binding.fabCreateParcel.setOnClickListener(v ->
                startActivityForResult(
                        new Intent(requireContext(), CreateParcelActivity.class),
                        Constants.RC_CREATE_PARCEL));

        binding.swipeRefresh.setOnRefreshListener(this::loadMyParcels);
    }

    private void setupRecycler() {
        adapter = new ParcelAdapter(parcel -> {
            Intent intent = new Intent(requireContext(), ParcelDetailActivity.class);
            intent.putExtra(Constants.EXTRA_PARCEL_ID, parcel.getId());
            startActivity(intent);
        });
        binding.rvParcels.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvParcels.setAdapter(adapter);
    }

    private void loadMyParcels() {
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.getMyParcels().observe(getViewLifecycleOwner(), response -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);
            if (response != null && response.isSuccess() && response.getData() != null) {
                adapter.setParcels(response.getData());
                binding.tvEmpty.setVisibility(
                        response.getData().isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.RC_CREATE_PARCEL &&
                resultCode == android.app.Activity.RESULT_OK) {
            loadMyParcels();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
