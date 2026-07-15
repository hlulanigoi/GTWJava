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

import com.goinghatway.app.activities.BuyTicketActivity;
import com.goinghatway.app.adapters.TicketAdapter;
import com.goinghatway.app.databinding.FragmentTicketsBinding;
import com.goinghatway.app.utils.Constants;
import com.goinghatway.app.viewmodels.TicketViewModel;

public class TicketsFragment extends Fragment {

    private FragmentTicketsBinding binding;
    private TicketViewModel viewModel;
    private TicketAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTicketsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TicketViewModel.class);

        setupRecycler();
        loadTickets();

        binding.fabBuyTicket.setOnClickListener(v ->
                startActivityForResult(
                        new Intent(requireContext(), BuyTicketActivity.class),
                        Constants.RC_BUY_TICKET));

        binding.swipeRefresh.setOnRefreshListener(this::loadTickets);
    }

    private void setupRecycler() {
        adapter = new TicketAdapter();
        binding.rvTickets.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTickets.setAdapter(adapter);
    }

    private void loadTickets() {
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.getMyTickets().observe(getViewLifecycleOwner(), response -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);
            if (response != null && response.isSuccess() && response.getData() != null) {
                adapter.setTickets(response.getData());
                long active = response.getData().stream()
                        .filter(t -> "ACTIVE".equals(t.getStatus())).count();
                binding.tvActiveCount.setText(active + " active ticket(s)");
                binding.tvEmpty.setVisibility(
                        response.getData().isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.RC_BUY_TICKET &&
                resultCode == android.app.Activity.RESULT_OK) {
            loadTickets();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
