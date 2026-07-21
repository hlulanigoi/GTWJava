package com.goinghatway.driver.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.goinghatway.driver.databinding.ActivityEarningsBinding;

import java.util.ArrayList;
import java.util.List;

public class EarningsFragment extends Fragment {

    private ActivityEarningsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityEarningsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hide toolbar if handled by Activity, but ActivityEarningsBinding has one
        if (binding.toolbar != null) {
            binding.toolbar.setNavigationIcon(null);
        }

        setupChart();
        seedData();
    }

    private void setupChart() {
        BarChart chart = binding.barChart;
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.setPinchZoom(false);
        chart.setScaleEnabled(false);

        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(new String[]{"M", "T", "W", "T", "F", "S", "S"}));

        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);
    }

    private void seedData() {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 450f));
        entries.add(new BarEntry(1, 800f));
        entries.add(new BarEntry(2, 600f));
        entries.add(new BarEntry(3, 1100f));
        entries.add(new BarEntry(4, 900f));
        entries.add(new BarEntry(5, 500f));
        entries.add(new BarEntry(6, 500f));

        BarDataSet dataSet = new BarDataSet(entries, "Earnings");
        dataSet.setColor(Color.parseColor("#0D47A1"));
        dataSet.setValueTextColor(Color.GRAY);
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        binding.barChart.setData(data);
        binding.barChart.invalidate();

        // Populate other fields
        binding.tvWalletBalance.setText("R 1,590.20");
        binding.tvWeeklyTotal.setText("R 4,850.00");
        binding.tvTotalTrips.setText("140");
        binding.tvOnlineHours.setText("62h 18m");
        binding.tvTotalDistance.setText("45 km");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
