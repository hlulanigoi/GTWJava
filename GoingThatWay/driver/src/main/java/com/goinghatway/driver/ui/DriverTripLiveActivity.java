package com.goinghatway.driver.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.goinghatway.app.utils.RideLifecycleStateMachine;
import com.goinghatway.driver.databinding.ActivityDriverTripLiveBinding;

public class DriverTripLiveActivity extends AppCompatActivity {
    private ActivityDriverTripLiveBinding binding;
    private String currentState = RideLifecycleStateMachine.STATUS_ASSIGNED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriverTripLiveBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String initialStatus = getIntent().getStringExtra("trip_status");
        if (initialStatus != null) {
            currentState = initialStatus;
        }

        binding.tvTripSummary.setText("Route: Sandton → Rosebank\nPassenger: 2 riders\nFare: R 180");
        binding.tvState.setText("Current state: " + RideLifecycleStateMachine.getDisplayStatus(currentState));

        binding.btnStartTrip.setOnClickListener(v -> {
            currentState = RideLifecycleStateMachine.getNextStatus(currentState, "start");
            binding.tvState.setText("Current state: " + RideLifecycleStateMachine.getDisplayStatus(currentState));
            Toast.makeText(this, "Trip started", Toast.LENGTH_SHORT).show();
        });

        binding.btnBoardPassenger.setOnClickListener(v -> {
            currentState = RideLifecycleStateMachine.getNextStatus(currentState, "board");
            binding.tvState.setText("Current state: " + RideLifecycleStateMachine.getDisplayStatus(currentState));
            Toast.makeText(this, "Passenger on board", Toast.LENGTH_SHORT).show();
        });

        binding.btnCompleteTrip.setOnClickListener(v -> {
            currentState = RideLifecycleStateMachine.getNextStatus(currentState, "complete");
            binding.tvState.setText("Current state: " + RideLifecycleStateMachine.getDisplayStatus(currentState));
            Toast.makeText(this, "Trip completed", Toast.LENGTH_SHORT).show();
        });
    }
}
