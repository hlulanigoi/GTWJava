package com.goinghatway.driver.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.goinghatway.driver.databinding.ActivityTripRequestsBinding;

public class TripRequestsActivity extends AppCompatActivity {
    private ActivityTripRequestsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTripRequestsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvRequestOne.setText("Request: 2 riders • Sandton to Rosebank • 18:30");
        binding.tvRequestTwo.setText("Request: 1 rider • Midrand to Pretoria • 20:00");
    }
}
