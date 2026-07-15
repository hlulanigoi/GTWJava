package com.goinghatway.app.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.goinghatway.app.adapters.admin.AdminPaymentAdapter;
import com.goinghatway.app.databinding.FragmentAdminPaymentsBinding;
import com.goinghatway.app.viewmodels.admin.AdminViewModel;

import java.util.List;
import java.util.Map;

public class AdminPaymentsFragment extends Fragment implements AdminPaymentAdapter.Listener {

    private FragmentAdminPaymentsBinding binding;
    private AdminViewModel viewModel;
    private AdminPaymentAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminPaymentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);

        adapter = new AdminPaymentAdapter(this);
        binding.rvPayments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPayments.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(this::loadPayments);
        loadPayments();
    }

    private void loadPayments() {
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.loadPendingPayments().observe(getViewLifecycleOwner(), response -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);
            if (response != null && response.isSuccess()) {
                List<Map<String, Object>> payments = response.getData();
                adapter.setPayments(payments);
                binding.tvEmpty.setVisibility(payments == null || payments.isEmpty()
                        ? View.VISIBLE : View.GONE);
                binding.tvPendingCount.setText(
                        (payments != null ? payments.size() : 0) + " pending verification");
            }
        });
    }

    @Override
    public void onVerify(String paymentRef) {
        viewModel.verifyPayment(paymentRef)
                .observe(getViewLifecycleOwner(), r -> {
                    Toast.makeText(requireContext(),
                            r != null && r.isSuccess() ? "Payment verified ✓" : "Failed",
                            Toast.LENGTH_SHORT).show();
                    if (r != null && r.isSuccess()) loadPayments();
                });
    }

    @Override
    public void onReject(String paymentRef) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Reject Payment")
                .setMessage("Are you sure you want to reject this payment reference: " + paymentRef + "?")
                .setPositiveButton("Reject", (d, w) ->
                        viewModel.rejectPayment(paymentRef, "Rejected by admin")
                                .observe(getViewLifecycleOwner(), r -> {
                                    Toast.makeText(requireContext(),
                                            r != null && r.isSuccess() ? "Payment rejected" : "Failed",
                                            Toast.LENGTH_SHORT).show();
                                    if (r != null && r.isSuccess()) loadPayments();
                                }))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
