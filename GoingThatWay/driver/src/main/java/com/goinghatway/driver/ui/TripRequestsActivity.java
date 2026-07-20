package com.goinghatway.driver.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.goinghatway.app.utils.RideLifecycleStateMachine;
import com.goinghatway.driver.databinding.ActivityTripRequestsBinding;

public class TripRequestsActivity extends AppCompatActivity {

    private static final long COUNTDOWN_MS = 20_000L;
    private static final long COUNTDOWN_INTERVAL_MS = 100L;

    private ActivityTripRequestsBinding binding;
    private boolean tripOneDecided = false;
    private boolean tripTwoDecided = false;
    private boolean tripOneAccepted = false;
    private boolean tripTwoAccepted = false;

    private CountDownTimer countDownOne;
    private CountDownTimer countDownTwo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTripRequestsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Seed request data
        binding.tvRequestOne.setText("Sandton \u2192 Rosebank  \u2022  18:30  \u2022  R 180");
        binding.tvRequestTwo.setText("Midrand \u2192 Pretoria  \u2022  20:00  \u2022  R 95");

        // Accept / Decline buttons
        binding.btnAcceptOne.setOnClickListener(v -> handleDecision(true, 1));
        binding.btnDeclineOne.setOnClickListener(v -> handleDecision(false, 1));
        binding.btnAcceptTwo.setOnClickListener(v -> handleDecision(true, 2));
        binding.btnDeclineTwo.setOnClickListener(v -> handleDecision(false, 2));

        // Trip action buttons (shown after accept)
        binding.btnStartTripOne.setOnClickListener(v -> updateTripState(1, "in progress"));
        binding.btnBoardPassengerOne.setOnClickListener(v -> updateTripState(1, "passenger on board"));
        binding.btnCompleteTripOne.setOnClickListener(v -> updateTripState(1, "completed"));
        binding.btnStartTripTwo.setOnClickListener(v -> updateTripState(2, "in progress"));
        binding.btnBoardPassengerTwo.setOnClickListener(v -> updateTripState(2, "passenger on board"));
        binding.btnCompleteTripTwo.setOnClickListener(v -> updateTripState(2, "completed"));

        // Start countdowns for both open requests
        startCountdown(1);
        startCountdown(2);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Countdown
    // ────────────────────────────────────────────────────────────────────────

    private void startCountdown(int requestNumber) {
        CountDownTimer timer = new CountDownTimer(COUNTDOWN_MS, COUNTDOWN_INTERVAL_MS) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Progress: 100 → 0 as time runs out
                int progress = (int) (millisUntilFinished * 100 / COUNTDOWN_MS);
                // Seconds shown: ceiling so it reads "20" at start, "1" in the last second
                int seconds = (int) Math.ceil(millisUntilFinished / 1000.0);

                if (requestNumber == 1) {
                    binding.pbCountdownOne.setProgressCompat(progress, false);
                    binding.tvCountdownOne.setText(String.valueOf(seconds));
                } else {
                    binding.pbCountdownTwo.setProgressCompat(progress, false);
                    binding.tvCountdownTwo.setText(String.valueOf(seconds));
                }
            }

            @Override
            public void onFinish() {
                // Auto-decline cleanly — no penalty messaging
                boolean alreadyDecided = requestNumber == 1 ? tripOneDecided : tripTwoDecided;
                if (!alreadyDecided) {
                    handleDecision(false, requestNumber);
                    String msg = "Request " + requestNumber + " expired";
                    Toast.makeText(TripRequestsActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }
        };

        timer.start();
        if (requestNumber == 1) countDownOne = timer;
        else countDownTwo = timer;
    }

    private void cancelCountdown(int requestNumber) {
        if (requestNumber == 1 && countDownOne != null) {
            countDownOne.cancel();
            countDownOne = null;
        } else if (requestNumber == 2 && countDownTwo != null) {
            countDownTwo.cancel();
            countDownTwo = null;
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Accept / Decline
    // ────────────────────────────────────────────────────────────────────────

    private void handleDecision(boolean accepted, int requestNumber) {
        // Guard against double-fire (e.g. button tap races with auto-expire)
        if (requestNumber == 1 && tripOneDecided) return;
        if (requestNumber == 2 && tripTwoDecided) return;

        cancelCountdown(requestNumber);

        if (requestNumber == 1) {
            tripOneDecided = true;
            tripOneAccepted = accepted;
            binding.requestOneGroup.setVisibility(View.GONE);
            binding.tvStatusOne.setText(accepted ? "\u2713 Accepted \u2014 trip assigned" : "Declined");
            binding.tvStatusOne.setVisibility(View.VISIBLE);
            binding.tripOneActions.setVisibility(accepted ? View.VISIBLE : View.GONE);
            if (accepted) openLiveTrip();
        } else {
            tripTwoDecided = true;
            tripTwoAccepted = accepted;
            binding.requestTwoGroup.setVisibility(View.GONE);
            binding.tvStatusTwo.setText(accepted ? "\u2713 Accepted \u2014 trip assigned" : "Declined");
            binding.tvStatusTwo.setVisibility(View.VISIBLE);
            binding.tripTwoActions.setVisibility(accepted ? View.VISIBLE : View.GONE);
            if (accepted) openLiveTrip();
        }
    }

    private void openLiveTrip() {
        Intent intent = new Intent(this, DriverTripLiveActivity.class);
        intent.putExtra("trip_status", RideLifecycleStateMachine.STATUS_ASSIGNED);
        startActivity(intent);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Trip state machine
    // ────────────────────────────────────────────────────────────────────────

    private void updateTripState(int requestNumber, String state) {
        boolean accepted = requestNumber == 1 ? tripOneAccepted : tripTwoAccepted;
        if (!accepted) {
            Toast.makeText(this, "Accept the request first", Toast.LENGTH_SHORT).show();
            return;
        }

        String action = state.equals("in progress") ? "start"
                : state.equals("passenger on board") ? "board"
                : "complete";

        String nextStatus = RideLifecycleStateMachine.getNextStatus(
                RideLifecycleStateMachine.STATUS_ASSIGNED, action);

        String display = RideLifecycleStateMachine.getDisplayStatus(nextStatus);

        if (requestNumber == 1) {
            binding.tvStatusOne.setText(display);
        } else {
            binding.tvStatusTwo.setText(display);
        }
        Toast.makeText(this, "Trip " + requestNumber + ": " + display, Toast.LENGTH_SHORT).show();
    }

    // ────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ────────────────────────────────────────────────────────────────────────

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownOne != null) countDownOne.cancel();
        if (countDownTwo != null) countDownTwo.cancel();
    }
}
