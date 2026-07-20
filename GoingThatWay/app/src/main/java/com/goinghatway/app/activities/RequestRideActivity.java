package com.goinghatway.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.goinghatway.app.R;
import com.goinghatway.app.databinding.ActivityRequestRideBinding;
import com.goinghatway.app.models.Ride;
import com.goinghatway.app.utils.Constants;
import com.goinghatway.app.viewmodels.RideViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.Locale;

public class RequestRideActivity extends AppCompatActivity {

    private ActivityRequestRideBinding binding;
    private RideViewModel viewModel;
    private FusedLocationProviderClient fusedLocationClient;

    // Coordinates resolved for scheduled ride form
    private double pickupLat, pickupLng, destLat, destLng;
    // On-demand: device GPS coords
    private double onDemandLat = 0, onDemandLng = 0;

    private String pendingPaymentRef = null;
    private boolean isOnDemandMode = false;

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    fetchLocationThenLaunchOnDemandPayment();
                } else {
                    Toast.makeText(this, "Location permission needed for on-demand ride", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequestRideBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RideViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupLuggageSpinner();
        setupFareCalculator();

        binding.btnProceedToPayment.setOnClickListener(v -> {
            isOnDemandMode = false;
            proceedToScheduledPayment();
        });

        binding.btnRequestRideNow.setOnClickListener(v -> {
            isOnDemandMode = true;
            requestLocationForOnDemand();
        });
    }

    private void setupLuggageSpinner() {
        String[] sizes = {Ride.LUGGAGE_NONE, Ride.LUGGAGE_SMALL, Ride.LUGGAGE_MEDIUM, Ride.LUGGAGE_LARGE};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, sizes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerLuggageSize.setAdapter(adapter);
    }

    private void setupFareCalculator() {
        binding.etPassengerCount.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) updateFarePreview();
        });
    }

    private void updateFarePreview() {
        String countStr = binding.etPassengerCount.getText().toString().trim();
        if (TextUtils.isEmpty(countStr)) return;
        try {
            int count = Integer.parseInt(countStr);
            double fare = calculateFare(count);
            double driverEarning = fare * Constants.CARRIER_SHARE_PERCENT;
            double platform = fare * Constants.PLATFORM_FEE_PERCENT;
            binding.tvFareText.setText(String.format(Locale.getDefault(),
                    "Fare: R%.2f  |  Driver earns: R%.2f  |  Platform: R%.2f",
                    fare, driverEarning, platform));
            binding.tvFareBreakdown.setVisibility(View.VISIBLE);
        } catch (NumberFormatException ignored) {}
    }

    private double calculateFare(int passengerCount) {
        if (passengerCount <= 1) return 80.0;
        if (passengerCount <= 3) return 150.0;
        return 220.0;
    }

    private void proceedToScheduledPayment() {
        if (!validateScheduledForm()) return;
        int count = Integer.parseInt(binding.etPassengerCount.getText().toString().trim());
        double fare = calculateFare(count);
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(Constants.EXTRA_AMOUNT, fare);
        intent.putExtra(Constants.EXTRA_PURPOSE, "ride");
        startActivityForResult(intent, Constants.RC_PAYMENT);
    }

    private void requestLocationForOnDemand() {
        if (!validateOnDemandForm()) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fetchLocationThenLaunchOnDemandPayment();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void fetchLocationThenLaunchOnDemandPayment() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                onDemandLat = location.getLatitude();
                onDemandLng = location.getLongitude();
            }
            int count = Integer.parseInt(binding.etPassengerCount.getText().toString().trim());
            double fare = calculateFare(count);
            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra(Constants.EXTRA_AMOUNT, fare);
            intent.putExtra(Constants.EXTRA_PURPOSE, "ride");
            startActivityForResult(intent, Constants.RC_PAYMENT);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Could not get location, please try again", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.RC_PAYMENT && resultCode == RESULT_OK && data != null) {
            pendingPaymentRef = data.getStringExtra("payment_reference");
            if (isOnDemandMode) {
                submitOnDemandRide();
            } else {
                submitScheduledRide();
            }
        }
    }

    private void submitScheduledRide() {
        if (pendingPaymentRef == null) return;
        String notes = binding.etNotes.getText().toString().trim();
        int count = Integer.parseInt(binding.etPassengerCount.getText().toString().trim());
        int sizeIdx = binding.spinnerLuggageSize.getSelectedItemPosition();
        String[] sizes = {Ride.LUGGAGE_NONE, Ride.LUGGAGE_SMALL, Ride.LUGGAGE_MEDIUM, Ride.LUGGAGE_LARGE};
        String luggageSize = sizes[sizeIdx];
        String pickupAddr = binding.etPickupAddress.getText().toString().trim();
        String destAddr = binding.etDestinationAddress.getText().toString().trim();
        double fare = calculateFare(count);

        setLoading(true);
        viewModel.createRide(notes, count, luggageSize,
                pickupAddr, pickupLat, pickupLng,
                destAddr, destLat, destLng,
                fare, pendingPaymentRef)
                .observe(this, response -> {
                    setLoading(false);
                    if (response != null && response.isSuccess()) {
                        Toast.makeText(this, "Ride booked! We are finding you a driver.",
                                Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        String msg = response != null ? response.getError() : "Failed to book ride";
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void submitOnDemandRide() {
        if (pendingPaymentRef == null) return;
        int count = Integer.parseInt(binding.etPassengerCount.getText().toString().trim());
        int sizeIdx = binding.spinnerLuggageSize.getSelectedItemPosition();
        String[] sizes = {Ride.LUGGAGE_NONE, Ride.LUGGAGE_SMALL, Ride.LUGGAGE_MEDIUM, Ride.LUGGAGE_LARGE};
        String luggageSize = sizes[sizeIdx];
        String destAddr = binding.etDestinationAddress.getText().toString().trim();

        setLoading(true);
        viewModel.createOnDemandRide(onDemandLat, onDemandLng, destAddr,
                count, luggageSize, pendingPaymentRef)
                .observe(this, response -> {
                    setLoading(false);
                    if (response != null && response.isSuccess()) {
                        Toast.makeText(this, "On-demand ride requested! Looking for nearby drivers.",
                                Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        String msg = response != null ? response.getError() : "Failed to request ride";
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateScheduledForm() {
        boolean valid = true;
        if (TextUtils.isEmpty(binding.etPassengerCount.getText())) {
            binding.etPassengerCount.setError("Required"); valid = false;
        }
        if (TextUtils.isEmpty(binding.etPickupAddress.getText())) {
            binding.etPickupAddress.setError("Required"); valid = false;
        }
        if (TextUtils.isEmpty(binding.etDestinationAddress.getText())) {
            binding.etDestinationAddress.setError("Required"); valid = false;
        }
        return valid;
    }

    private boolean validateOnDemandForm() {
        boolean valid = true;
        if (TextUtils.isEmpty(binding.etPassengerCount.getText())) {
            binding.etPassengerCount.setError("Required"); valid = false;
        }
        if (TextUtils.isEmpty(binding.etDestinationAddress.getText())) {
            binding.etDestinationAddress.setError("Required"); valid = false;
        }
        return valid;
    }

    private void setLoading(boolean loading) {
        binding.btnProceedToPayment.setEnabled(!loading);
        binding.btnRequestRideNow.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
