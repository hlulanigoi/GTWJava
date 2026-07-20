package com.goinghatway.app.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.goinghatway.app.utils.LocationPickerHelper;
import com.goinghatway.app.utils.OsmMapUtils;
import com.goinghatway.app.utils.PriceCalculator;
import com.goinghatway.app.utils.RideLifecycleStateMachine;
import com.goinghatway.app.viewmodels.RideViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

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
    private boolean pinningPickup = true;
    private Marker pickupMarker;
    private Marker destinationMarker;

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

        OsmMapUtils.init(this);
        setupMapView();

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupLuggageSpinner();
        setupFareCalculator();
        setupLocationButtons();

        binding.btnProceedToPayment.setOnClickListener(v -> {
            isOnDemandMode = false;
            proceedToScheduledPayment();
        });

        binding.btnRequestRideNow.setOnClickListener(v -> {
            isOnDemandMode = true;
            requestLocationForOnDemand();
        });
    }

    private void setupMapView() {
        OsmMapUtils.configure(binding.mapView);
        OsmMapUtils.centerOn(binding.mapView, OsmMapUtils.SA_LAT, OsmMapUtils.SA_LNG, 10.0);

        pickupMarker = OsmMapUtils.addMarker(binding.mapView, OsmMapUtils.SA_LAT, OsmMapUtils.SA_LNG,
                "Pickup", "Tap the map to set a pickup pin", false);
        destinationMarker = OsmMapUtils.addMarker(binding.mapView, OsmMapUtils.SA_LAT, OsmMapUtils.SA_LNG,
                "Destination", "Tap the map to set a destination pin", false);

        MapEventsOverlay eventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(org.osmdroid.util.GeoPoint p) {
                setPinnedLocation(p.getLatitude(), p.getLongitude());
                return true;
            }

            @Override
            public boolean longPressHelper(org.osmdroid.util.GeoPoint p) {
                return false;
            }
        });
        binding.mapView.getOverlays().add(eventsOverlay);
        binding.mapView.invalidate();
    }

    private void setPinnedLocation(double lat, double lng) {
        if (pinningPickup) {
            pickupLat = lat;
            pickupLng = lng;
            pickupMarker.setPosition(new org.osmdroid.util.GeoPoint(lat, lng));
            binding.etPickupAddress.setText("Pinned pickup location");
            Toast.makeText(this, "Pickup pin set", Toast.LENGTH_SHORT).show();
        } else {
            destLat = lat;
            destLng = lng;
            destinationMarker.setPosition(new org.osmdroid.util.GeoPoint(lat, lng));
            binding.etDestinationAddress.setText("Pinned destination location");
            Toast.makeText(this, "Destination pin set", Toast.LENGTH_SHORT).show();
        }
        binding.mapView.invalidate();
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
        binding.etPassengerCount.setOnEditorActionListener((v, actionId, event) -> {
            updateFarePreview();
            return false;
        });

        binding.rgRideType.setOnCheckedChangeListener((group, checkedId) -> updateFarePreview());

        TextWatcher summaryWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateFarePreview();
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        binding.etPassengerCount.addTextChangedListener(summaryWatcher);
        binding.etPickupAddress.addTextChangedListener(summaryWatcher);
        binding.etDestinationAddress.addTextChangedListener(summaryWatcher);
    }

    private void setupLocationButtons() {
        binding.btnPinPickup.setOnClickListener(v -> {
            pinningPickup = true;
            Toast.makeText(this, "Tap the map to place the pickup pin", Toast.LENGTH_SHORT).show();
        });
        binding.btnPinDestination.setOnClickListener(v -> {
            pinningPickup = false;
            Toast.makeText(this, "Tap the map to place the destination pin", Toast.LENGTH_SHORT).show();
        });
        binding.btnUseCurrentLocation.setOnClickListener(v -> requestLocationForOnDemand());
        binding.btnGeoPickup.setOnClickListener(v -> resolveAddress(binding.etPickupAddress.getText().toString().trim(), true));
        binding.btnGeoDestination.setOnClickListener(v -> resolveAddress(binding.etDestinationAddress.getText().toString().trim(), false));
    }

    private void updateFarePreview() {
        String countStr = binding.etPassengerCount.getText().toString().trim();
        String pickupText = binding.etPickupAddress.getText().toString().trim();
        String destinationText = binding.etDestinationAddress.getText().toString().trim();
        String rideType = getSelectedRideType();

        if (TextUtils.isEmpty(countStr)) {
            binding.tvFareText.setText("Add the passenger count to preview the fare and booking summary.");
            binding.tvBookingSummary.setText("Choose a ride type and destination to see your booking summary.");
            binding.tvFareBreakdown.setVisibility(View.VISIBLE);
            binding.tvBookingSummary.setVisibility(View.VISIBLE);
            return;
        }

        try {
            int count = Integer.parseInt(countStr);
            if (count < 1) {
                binding.tvFareText.setText("Passenger count must be at least 1.");
                binding.tvBookingSummary.setText("Passenger count must be at least 1.");
                binding.tvFareBreakdown.setVisibility(View.VISIBLE);
                binding.tvBookingSummary.setVisibility(View.VISIBLE);
                return;
            }

            double fare = PriceCalculator.calculateRideFare(count, rideType);
            double driverEarning = PriceCalculator.calculateDriverEarning(fare);
            double platform = PriceCalculator.calculatePlatformFee(fare);
            String pickupLabel = TextUtils.isEmpty(pickupText) ? "Pickup" : pickupText;
            String destinationLabel = TextUtils.isEmpty(destinationText) ? "Destination" : destinationText;

            binding.tvFareText.setText(String.format(Locale.getDefault(),
                    "Fare: R%.2f  |  Driver earns: R%.2f  |  Platform: R%.2f",
                    fare, driverEarning, platform));
            binding.tvBookingSummary.setText(String.format(Locale.getDefault(),
                    "%s ride • %d passenger(s) • Est. R%.2f\n%s → %s",
                    rideType, count, fare, pickupLabel, destinationLabel));
            binding.tvFareBreakdown.setVisibility(View.VISIBLE);
            binding.tvBookingSummary.setVisibility(View.VISIBLE);
        } catch (NumberFormatException ignored) {
            binding.tvFareText.setText("Passenger count should be a number.");
            binding.tvBookingSummary.setText("Passenger count should be a number.");
            binding.tvFareBreakdown.setVisibility(View.VISIBLE);
            binding.tvBookingSummary.setVisibility(View.VISIBLE);
        }
    }

    private void resolveAddress(String query, boolean isPickup) {
        if (TextUtils.isEmpty(query)) {
            Toast.makeText(this, "Enter an address first", Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true);
        LocationPickerHelper.autoFillFromAddress(this, query, new LocationPickerHelper.OnLocationResolved() {
            @Override
            public void onResolved(double lat, double lng, String formattedAddress) {
                setLoading(false);
                if (isPickup) {
                    pickupLat = lat;
                    pickupLng = lng;
                    binding.etPickupAddress.setText(formattedAddress);
                    Toast.makeText(RequestRideActivity.this, "Pickup location ready", Toast.LENGTH_SHORT).show();
                } else {
                    destLat = lat;
                    destLng = lng;
                    binding.etDestinationAddress.setText(formattedAddress);
                    Toast.makeText(RequestRideActivity.this, "Destination location ready", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(RequestRideActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void proceedToScheduledPayment() {
        if (!validateScheduledForm()) return;
        int count = Integer.parseInt(binding.etPassengerCount.getText().toString().trim());
        double fare = PriceCalculator.calculateRideFare(count, getSelectedRideType());
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
            double fare = PriceCalculator.calculateRideFare(count, getSelectedRideType());
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
        double fare = PriceCalculator.calculateRideFare(count);

        setLoading(true);
        viewModel.createRide(notes, count, luggageSize,
                pickupAddr, pickupLat, pickupLng,
                destAddr, destLat, destLng,
                fare, pendingPaymentRef)
                .observe(this, response -> {
                    setLoading(false);
                    if (response != null && response.isSuccess()) {
                        Intent next = new Intent();
                        next.setComponent(new ComponentName("com.goinghatway.requester",
                                "com.goinghatway.requester.RequesterActiveRideActivity"));
                        next.putExtra("ride_status", RideLifecycleStateMachine.STATUS_SEARCHING);
                        next.putExtra("ride_type", getSelectedRideType());
                        next.putExtra("pickup_address", binding.etPickupAddress.getText().toString().trim());
                        next.putExtra("destination_address", binding.etDestinationAddress.getText().toString().trim());
                        next.putExtra("fare", PriceCalculator.calculateRideFare(Integer.parseInt(binding.etPassengerCount.getText().toString().trim()), getSelectedRideType()));
                        next.putExtra("passenger_count", Integer.parseInt(binding.etPassengerCount.getText().toString().trim()));
                        next.putExtra("payment_reference", pendingPaymentRef);
                        startActivity(next);
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
                        Intent next = new Intent();
                        next.setComponent(new ComponentName("com.goinghatway.requester",
                                "com.goinghatway.requester.RequesterActiveRideActivity"));
                        next.putExtra("ride_status", RideLifecycleStateMachine.STATUS_SEARCHING);
                        next.putExtra("ride_type", getSelectedRideType());
                        next.putExtra("pickup_address", binding.etPickupAddress.getText().toString().trim());
                        next.putExtra("destination_address", binding.etDestinationAddress.getText().toString().trim());
                        next.putExtra("fare", PriceCalculator.calculateRideFare(Integer.parseInt(binding.etPassengerCount.getText().toString().trim()), getSelectedRideType()));
                        next.putExtra("passenger_count", Integer.parseInt(binding.etPassengerCount.getText().toString().trim()));
                        next.putExtra("payment_reference", pendingPaymentRef);
                        startActivity(next);
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
        String passengerCount = binding.etPassengerCount.getText().toString().trim();
        if (TextUtils.isEmpty(passengerCount)) {
            binding.etPassengerCount.setError("Required"); valid = false;
        } else {
            int count = Integer.parseInt(passengerCount);
            if (count < 1) {
                binding.etPassengerCount.setError("Enter at least 1 passenger"); valid = false;
            }
        }

        if (TextUtils.isEmpty(binding.etPickupAddress.getText())) {
            binding.etPickupAddress.setError("Required"); valid = false;
        }
        if (TextUtils.isEmpty(binding.etDestinationAddress.getText())) {
            binding.etDestinationAddress.setError("Required"); valid = false;
        } else if (!TextUtils.isEmpty(binding.etPickupAddress.getText())
                && binding.etPickupAddress.getText().toString().trim().equalsIgnoreCase(
                binding.etDestinationAddress.getText().toString().trim())) {
            binding.etDestinationAddress.setError("Destination should be different from pickup"); valid = false;
        }
        return valid;
    }

    private boolean validateOnDemandForm() {
        boolean valid = true;
        String passengerCount = binding.etPassengerCount.getText().toString().trim();
        if (TextUtils.isEmpty(passengerCount)) {
            binding.etPassengerCount.setError("Required"); valid = false;
        } else {
            int count = Integer.parseInt(passengerCount);
            if (count < 1) {
                binding.etPassengerCount.setError("Enter at least 1 passenger"); valid = false;
            }
        }

        if (TextUtils.isEmpty(binding.etDestinationAddress.getText())) {
            binding.etDestinationAddress.setError("Required"); valid = false;
        }
        return valid;
    }

    private String getSelectedRideType() {
        int checkedId = binding.rgRideType.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_premium) return "Premium";
        if (checkedId == R.id.rb_shared) return "Shared";
        return "Standard";
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
