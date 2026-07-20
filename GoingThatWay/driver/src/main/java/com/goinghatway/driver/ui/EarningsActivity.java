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

        binding.tvTodayEarnings.setText("R 420.00");
        binding.tvWeeklyEarnings.setText("R 2,940.00");
        binding.tvTripsCount.setText("12 trips this week");
    }
}
