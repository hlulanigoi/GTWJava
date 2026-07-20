package com.goinghatway.app.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.goinghatway.app.databinding.ActivityRideDetailBinding;
import com.goinghatway.app.models.Ride;
import com.goinghatway.app.utils.Constants;
import com.goinghatway.app.viewmodels.RideViewModel;

import java.util.Locale;

public class RideDetailActivity extends AppCompatActivity {

    private ActivityRideDetailBinding binding;
    private RideViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRideDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RideViewModel.class);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String rideId = getIntent().getStringExtra(Constants.EXTRA_RIDE_ID);
        if (rideId != null) loadRide(rideId);
    }

    private void loadRide(String id) {
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.getRide(id).observe(this, response -> {
            binding.progressBar.setVisibility(View.GONE);
            if (response != null && response.isSuccess() && response.getData() != null) {
                bindRide(response.getData());
            }
        });
    }

    private void bindRide(Ride ride) {
        binding.tvNotes.setText(ride.getNotes() != null && !ride.getNotes().isEmpty()
                ? ride.getNotes() : "No notes");
        binding.tvPickupAddress.setText(ride.getPickupAddress());
        binding.tvDestinationAddress.setText(ride.getDestinationAddress());
        binding.tvPassengers.setText(String.format(Locale.getDefault(),
                "%d passenger(s) — Luggage: %s",
                ride.getPassengerCount(), ride.getLuggageSize()));
        binding.tvFare.setText(String.format(Locale.getDefault(), "R %.2f", ride.getFare()));
        binding.tvDriverEarning.setText(String.format(Locale.getDefault(),
                "Driver earns: R %.2f", ride.getDriverEarning()));
        binding.tvStatus.setText(ride.getStatus());
        if (ride.getRider() != null) {
            binding.tvRiderName.setText(ride.getRider().getFullName());
            binding.tvRiderRating.setText(String.format(Locale.getDefault(),
                    "%.1f ★", ride.getRider().getRating()));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
