package com.goinghatway.driver.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.goinghatway.app.utils.RideLifecycleStateMachine;
import com.goinghatway.driver.databinding.ActivityTripRequestsBinding;

public class TripRequestsActivity extends AppCompatActivity {

    private static final long COUNTDOWN_MS          = 20_000L;
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

        // Seed route data
        binding.tvRequestOne.setText("Sandton \u2192 Rosebank  \u2022  18:30  \u2022  R 180");
        binding.tvRequestTwo.setText("Midrand \u2192 Pretoria  \u2022  20:00  \u2022  R 95");

        // Accept / Decline
        binding.btnAcceptOne.setOnClickListener(v -> handleDecision(true,  false, 1));
        binding.btnDeclineOne.setOnClickListener(v -> handleDecision(false, false, 1));
        binding.btnAcceptTwo.setOnClickListener(v -> handleDecision(true,  false, 2));
        binding.btnDeclineTwo.setOnClickListener(v -> handleDecision(false, false, 2));

        // Counter-offer (inDrive-inspired): toggle panel
        binding.btnNegotiateOne.setOnClickListener(v -> toggleCounterPanel(1));
        binding.btnNegotiateTwo.setOnClickListener(v -> toggleCounterPanel(2));

        // Send counter-offer
        binding.btnSendOfferOne.setOnClickListener(v -> sendCounterOffer(1));
        binding.btnSendOfferTwo.setOnClickListener(v -> sendCounterOffer(2));

        // Trip state progression buttons
        binding.btnStartTripOne.setOnClickListener(v -> updateTripState(1, "in progress"));
        binding.btnBoardPassengerOne.setOnClickListener(v -> updateTripState(1, "passenger on board"));
        binding.btnCompleteTripOne.setOnClickListener(v -> updateTripState(1, "completed"));
        binding.btnStartTripTwo.setOnClickListener(v -> updateTripState(2, "in progress"));
        binding.btnBoardPassengerTwo.setOnClickListener(v -> updateTripState(2, "passenger on board"));
        binding.btnCompleteTripTwo.setOnClickListener(v -> updateTripState(2, "completed"));

        startCountdown(1);
        startCountdown(2);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Countdown (20-second auto-decline)
    // ────────────────────────────────────────────────────────────────────────

    private void startCountdown(int req) {
        CountDownTimer timer = new CountDownTimer(COUNTDOWN_MS, COUNTDOWN_INTERVAL_MS) {
            @Override
            public void onTick(long millisLeft) {
                int progress = (int) (millisLeft * 100 / COUNTDOWN_MS);
                int seconds  = (int) Math.ceil(millisLeft / 1000.0);
                if (req == 1) {
                    binding.pbCountdownOne.setProgressCompat(progress, false);
                    binding.tvCountdownOne.setText(String.valueOf(seconds));
                } else {
                    binding.pbCountdownTwo.setProgressCompat(progress, false);
                    binding.tvCountdownTwo.setText(String.valueOf(seconds));
                }
            }

            @Override
            public void onFinish() {
                boolean decided = req == 1 ? tripOneDecided : tripTwoDecided;
                if (!decided) {
                    handleDecision(false, false, req);
                    Toast.makeText(TripRequestsActivity.this,
                            "Request " + req + " expired", Toast.LENGTH_SHORT).show();
                }
            }
        };
        timer.start();
        if (req == 1) countDownOne = timer;
        else          countDownTwo = timer;
    }

    private void cancelCountdown(int req) {
        if (req == 1 && countDownOne != null) { countDownOne.cancel(); countDownOne = null; }
        if (req == 2 && countDownTwo != null) { countDownTwo.cancel(); countDownTwo = null; }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Counter-offer (inDrive negotiation model)
    // ────────────────────────────────────────────────────────────────────────

    private void toggleCounterPanel(int req) {
        View panel = req == 1 ? binding.counterOfferGroupOne : binding.counterOfferGroupTwo;
        panel.setVisibility(panel.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    private void sendCounterOffer(int req) {
        String raw = req == 1
                ? (binding.etCounterOfferOne.getText() != null ? binding.etCounterOfferOne.getText().toString() : "")
                : (binding.etCounterOfferTwo.getText() != null ? binding.etCounterOfferTwo.getText().toString() : "");

        if (TextUtils.isEmpty(raw)) {
            Toast.makeText(this, "Enter your counter-offer amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try { amount = Double.parseDouble(raw); }
        catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cancel countdown — driver has engaged, waiting for sender reply
        cancelCountdown(req);

        // Hide the request card, show waiting status
        View group = req == 1 ? binding.requestOneGroup : binding.requestTwoGroup;
        group.setVisibility(View.GONE);

        String msg = String.format("Counter-offer of R %.0f sent — waiting for sender response", amount);
        if (req == 1) {
            binding.tvStatusOne.setText(msg);
            binding.tvStatusOne.setVisibility(View.VISIBLE);
        } else {
            binding.tvStatusTwo.setText(msg);
            binding.tvStatusTwo.setVisibility(View.VISIBLE);
        }

        // TODO: wire to API — POST /api/bookings/{id}/counter  { amount }
        Toast.makeText(this, "Counter-offer of R " + (int) amount + " sent!", Toast.LENGTH_SHORT).show();
    }

    // ────────────────────────────────────────────────────────────────────────
    // Accept / Decline
    // ────────────────────────────────────────────────────────────────────────

    private void handleDecision(boolean accepted, boolean isCounterAccepted, int req) {
        if (req == 1 && tripOneDecided) return;
        if (req == 2 && tripTwoDecided) return;

        cancelCountdown(req);

        if (req == 1) {
            tripOneDecided = true;
            tripOneAccepted = accepted;
            binding.requestOneGroup.setVisibility(View.GONE);
            binding.tvStatusOne.setText(accepted ? "\u2713 Accepted \u2014 trip assigned" : "Declined");
            binding.tvStatusOne.setVisibility(View.VISIBLE);
            binding.tripOneActions.setVisibility(accepted ? View.VISIBLE : View.GONE);
        } else {
            tripTwoDecided = true;
            tripTwoAccepted = accepted;
            binding.requestTwoGroup.setVisibility(View.GONE);
            binding.tvStatusTwo.setText(accepted ? "\u2713 Accepted \u2014 trip assigned" : "Declined");
            binding.tvStatusTwo.setVisibility(View.VISIBLE);
            binding.tripTwoActions.setVisibility(accepted ? View.VISIBLE : View.GONE);
        }

        if (accepted) {
            Intent intent = new Intent(this, DriverTripLiveActivity.class);
            intent.putExtra("trip_status", RideLifecycleStateMachine.STATUS_ASSIGNED);
            startActivity(intent);
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Trip state machine
    // ────────────────────────────────────────────────────────────────────────

    private void updateTripState(int req, String state) {
        boolean accepted = req == 1 ? tripOneAccepted : tripTwoAccepted;
        if (!accepted) {
            Toast.makeText(this, "Accept the request first", Toast.LENGTH_SHORT).show();
            return;
        }
        String action = state.equals("in progress") ? "start"
                : state.equals("passenger on board") ? "board" : "complete";
        String next    = RideLifecycleStateMachine.getNextStatus(
                RideLifecycleStateMachine.STATUS_ASSIGNED, action);
        String display = RideLifecycleStateMachine.getDisplayStatus(next);

        if (req == 1) binding.tvStatusOne.setText(display);
        else          binding.tvStatusTwo.setText(display);

        Toast.makeText(this, "Trip " + req + ": " + display, Toast.LENGTH_SHORT).show();
    }

    // ────────────────────────────────────────────────────────────────────────

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownOne != null) countDownOne.cancel();
        if (countDownTwo != null) countDownTwo.cancel();
    }
}
