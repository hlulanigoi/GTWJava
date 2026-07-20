package com.goinghatway.driver.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.goinghatway.driver.R;
import com.goinghatway.driver.databinding.ActivityEarningsBinding;

public class EarningsActivity extends AppCompatActivity {

    private ActivityEarningsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEarningsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Seed with demo data — replace with API call when backend is wired
        binding.tvTodayEarnings.setText("R 420.00");
        binding.tvWeeklyEarnings.setText("R 2,940.00 this week");
        binding.tvTripsCount.setText("12");

        // Derived stats
        if (binding.tvAvgPerTrip != null) {
            binding.tvAvgPerTrip.setText("R 245");
        }
        if (binding.tvAcceptanceRate != null) {
            binding.tvAcceptanceRate.setText("94%");
        }
    }
}
