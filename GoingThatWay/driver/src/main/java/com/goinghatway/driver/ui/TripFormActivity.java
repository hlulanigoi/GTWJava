package com.goinghatway.driver.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.goinghatway.driver.databinding.ActivityTripFormBinding;
import com.goinghatway.driver.R;

public class TripFormActivity extends AppCompatActivity {
    private ActivityTripFormBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTripFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSaveTrip.setOnClickListener(v -> {
            String origin = binding.etOrigin.getText().toString().trim();
            String destination = binding.etDestination.getText().toString().trim();
            String time = binding.etDepartureTime.getText().toString().trim();
            String seats = binding.etSeats.getText().toString().trim();

            if (origin.isEmpty() || destination.isEmpty() || time.isEmpty() || seats.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, getString(R.string.trip_created), Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
