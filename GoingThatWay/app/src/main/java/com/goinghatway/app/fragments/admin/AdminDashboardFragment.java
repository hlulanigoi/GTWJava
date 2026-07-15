package com.goinghatway.app.fragments.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.goinghatway.app.databinding.FragmentAdminDashboardBinding;
import com.goinghatway.app.models.AdminStats;
import com.goinghatway.app.viewmodels.admin.AdminViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminDashboardFragment extends Fragment {

    private FragmentAdminDashboardBinding binding;
    private AdminViewModel viewModel;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AdminViewModel.class);
        loadStats();
    }

    private void loadStats() {
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.loadStats().observe(getViewLifecycleOwner(), response -> {
            binding.progressBar.setVisibility(View.GONE);
            if (response != null && response.isSuccess()) {
                bindStats(response.getData());
            }
        });
    }

    private void bindStats(AdminStats stats) {
        if (stats == null) return;

        // Top row
        binding.tvTotalUsers.setText(String.valueOf(stats.getTotalUsers()));
        binding.tvTotalParcels.setText(String.valueOf(stats.getTotalParcels()));
        binding.tvTotalTrips.setText(String.valueOf(stats.getTotalTrips()));
        binding.tvTicketsSold.setText(String.valueOf(stats.getTotalTicketsSold()));

        // Revenue
        binding.tvTotalRevenue.setText(
                String.format(Locale.getDefault(), "R %.2f", stats.getTotalRevenue()));
        binding.tvPlatformEarnings.setText(
                String.format(Locale.getDefault(), "R %.2f", stats.getPlatformEarnings()));

        // Action items
        binding.tvPendingParcels.setText(stats.getPendingParcels() + " parcels pending");
        binding.tvActiveTrips.setText(stats.getActiveTrips() + " active trips");
        binding.tvPendingPayments.setText(stats.getPendingPayments() + " payments to verify");
        binding.tvDeliveredToday.setText(stats.getDeliveredToday() + " delivered today");

        // Chart
        if (stats.getRevenueThisWeek() != null && !stats.getRevenueThisWeek().isEmpty()) {
            setupChart(stats);
        }
    }

    private void setupChart(AdminStats stats) {
        BarChart chart = binding.barChart;
        List<BarEntry> entries = new ArrayList<>();
        List<Double> data = stats.getRevenueThisWeek();

        for (int i = 0; i < data.size(); i++) {
            entries.add(new BarEntry(i, data.get(i).floatValue()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Revenue (R)");
        dataSet.setColor(Color.parseColor("#2E7D32"));
        dataSet.setValueTextColor(Color.parseColor("#1A1A1A"));
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        chart.setData(barData);

        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setTextColor(Color.parseColor("#6B7280"));

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#6B7280"));
        xAxis.setDrawGridLines(false);
        if (stats.getWeekLabels() != null) {
            xAxis.setValueFormatter(new IndexAxisValueFormatter(stats.getWeekLabels()));
        }

        chart.animateY(800);
        chart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
