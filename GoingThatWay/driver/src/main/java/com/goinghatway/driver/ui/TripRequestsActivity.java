package com.goinghatway.driver.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.goinghatway.app.utils.RideLifecycleStateMachine;
import com.goinghatway.driver.databinding.ActivityTripRequestsBinding;

public class TripRequestsActivity extends AppCompatActivity {
    private ActivityTripRequestsBinding binding;
    private boolean tripOneAccepted = false;
    private boolean tripTwoAccepted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTripRequestsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnAcceptOne.setOnClickListener(v -> handleDecision(true, 1));
        binding.btnDeclineOne.setOnClickListener(v -> handleDecision(false, 1));
        binding.btnAcceptTwo.setOnClickListener(v -> handleDecision(true, 2));
        binding.btnDeclineTwo.setOnClickListener(v -> handleDecision(false, 2));

        binding.btnStartTripOne.setOnClickListener(v -> updateTripState(1, "in progress"));
        binding.btnBoardPassengerOne.setOnClickListener(v -> updateTripState(1, "passenger on board"));
        binding.btnCompleteTripOne.setOnClickListener(v -> updateTripState(1, "completed"));
        binding.btnStartTripTwo.setOnClickListener(v -> updateTripState(2, "in progress"));
        binding.btnBoardPassengerTwo.setOnClickListener(v -> updateTripState(2, "passenger on board"));
        binding.btnCompleteTripTwo.setOnClickListener(v -> updateTripState(2, "completed"));

        binding.tvRequestOne.setText("2 riders • Sandton to Rosebank • 18:30 • R 180");
        binding.tvRequestTwo.setText("1 rider • Midrand to Pretoria • 20:00 • R 95");
    }

    private void handleDecision(boolean accepted, int requestNumber) {
        String label = accepted ? "accepted" : "declined";
        Toast.makeText(this, "Request " + requestNumber + " " + label, Toast.LENGTH_SHORT).show();

        if (requestNumber == 1) {
            tripOneAccepted = accepted;
            binding.requestOneGroup.setVisibility(View.GONE);
            binding.tvStatusOne.setText("Status: " + label);
            binding.tvStatusOne.setVisibility(View.VISIBLE);
            binding.tripOneActions.setVisibility(accepted ? View.VISIBLE : View.GONE);
            if (accepted) {
                Intent intent = new Intent(this, DriverTripLiveActivity.class);
                intent.putExtra("trip_status", RideLifecycleStateMachine.STATUS_ASSIGNED);
                startActivity(intent);
            }
        } else {
            tripTwoAccepted = accepted;
            binding.requestTwoGroup.setVisibility(View.GONE);
            binding.tvStatusTwo.setText("Status: " + label);
            binding.tvStatusTwo.setVisibility(View.VISIBLE);
            binding.tripTwoActions.setVisibility(accepted ? View.VISIBLE : View.GONE);
            if (accepted) {
                Intent intent = new Intent(this, DriverTripLiveActivity.class);
                intent.putExtra("trip_status", RideLifecycleStateMachine.STATUS_ASSIGNED);
                startActivity(intent);
            }
        }
    }

    private void updateTripState(int requestNumber, String state) {
        if (requestNumber == 1 && !tripOneAccepted) {
            Toast.makeText(this, "Accept the request first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (requestNumber == 2 && !tripTwoAccepted) {
            Toast.makeText(this, "Accept the request first", Toast.LENGTH_SHORT).show();
            return;
        }

        String nextStatus = RideLifecycleStateMachine.getNextStatus(
                requestNumber == 1 ? RideLifecycleStateMachine.STATUS_ASSIGNED : RideLifecycleStateMachine.STATUS_ASSIGNED,
                state.equals("in progress") ? "start" : state.equals("passenger on board") ? "board" : "complete");

        if (requestNumber == 1) {
            binding.tvStatusOne.setText("Status: " + RideLifecycleStateMachine.getDisplayStatus(nextStatus));
        } else {
            binding.tvStatusTwo.setText("Status: " + RideLifecycleStateMachine.getDisplayStatus(nextStatus));
        }
        Toast.makeText(this, "Trip " + requestNumber + " marked as " + RideLifecycleStateMachine.getDisplayStatus(nextStatus), Toast.LENGTH_SHORT).show();
    }
}
