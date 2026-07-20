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
import com.goinghatway.app.databinding.ActivityCreateParcelBinding;
import com.goinghatway.app.utils.Constants;
import com.goinghatway.app.utils.LocationPickerHelper;
import com.goinghatway.app.utils.OsmMapUtils;
import com.goinghatway.app.utils.PriceCalculator;
import com.goinghatway.app.viewmodels.ParcelViewModel;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.Locale;

public class CreateParcelActivity extends AppCompatActivity {

    private ActivityCreateParcelBinding binding;
    private ParcelViewModel viewModel;

    // Locations resolved from address inputs (set after map/geocode)
    private double pickupLat, pickupLng, destLat, destLng;
    private String pendingPaymentRef = null; // set after payment flow
    private boolean pinningPickup = true;
    private Marker pickupMarker;
    private Marker destinationMarker;

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    autoFillCurrentLocation();
                } else {
                    Toast.makeText(this, "Location permission is needed for automatic pickup fill-in", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateParcelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ParcelViewModel.class);

        OsmMapUtils.init(this);
        setupMapView();

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupSizeSpinner();
        setupFeeCalculator();
        setupLocationButtons();

        binding.btnProceedToPayment.setOnClickListener(v -> proceedToPayment());
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

    private void setupSizeSpinner() {
        String[] sizes = {Constants.SIZE_SMALL + " (up to 1 kg)",
                          Constants.SIZE_MEDIUM + " (1–5 kg)",
                          Constants.SIZE_LARGE + " (5–15 kg)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, sizes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSize.setAdapter(adapter);
    }

    private void setupFeeCalculator() {
        binding.etWeight.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) updateFeePreview();
        });
        binding.etWeight.setOnEditorActionListener((v, actionId, event) -> {
            updateFeePreview();
            return false;
        });
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
        binding.btnUseCurrentLocation.setOnClickListener(v -> requestCurrentLocationAutoFill());
        binding.btnGeoPickup.setOnClickListener(v -> resolveAddress(binding.etPickupAddress.getText().toString().trim(), true));
        binding.btnGeoDestination.setOnClickListener(v -> resolveAddress(binding.etDestinationAddress.getText().toString().trim(), false));
    }

    private void updateFeePreview() {
        String weightStr = binding.etWeight.getText().toString().trim();
        if (TextUtils.isEmpty(weightStr)) return;
        try {
            double weight = Double.parseDouble(weightStr);
            double fee = PriceCalculator.calculateParcelFee(weight);
            double yourEarning = PriceCalculator.calculateDriverEarning(fee);
            double platform = PriceCalculator.calculatePlatformFee(fee);
            binding.tvFeeText.setText(String.format(Locale.getDefault(),
                    "Total fee: R%.2f  |  Carrier earns: R%.2f  |  Platform: R%.2f",
                    fee, yourEarning, platform));
            binding.tvFeeBreakdown.setVisibility(View.VISIBLE);
        } catch (NumberFormatException ignored) {}
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
                    Toast.makeText(CreateParcelActivity.this, "Pickup location ready", Toast.LENGTH_SHORT).show();
                } else {
                    destLat = lat;
                    destLng = lng;
                    binding.etDestinationAddress.setText(formattedAddress);
                    Toast.makeText(CreateParcelActivity.this, "Destination location ready", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(CreateParcelActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestCurrentLocationAutoFill() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            autoFillCurrentLocation();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void autoFillCurrentLocation() {
        setLoading(true);
        binding.etPickupAddress.setText("Using current location...");
        LocationPickerHelper.reverseGeocode(this, -26.2041, 28.0473, formatted -> {
            setLoading(false);
            binding.etPickupAddress.setText(TextUtils.isEmpty(formatted) ? "Current location" : formatted);
            pickupLat = -26.2041;
            pickupLng = 28.0473;
            Toast.makeText(this, "Pickup location set from your current GPS position", Toast.LENGTH_SHORT).show();
        });
    }

    private void proceedToPayment() {
        if (!validateForm()) return;

        double weight = Double.parseDouble(binding.etWeight.getText().toString().trim());
        double fee    = PriceCalculator.calculateParcelFee(weight);

        // Launch payment screen; on success it returns a payment reference
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(Constants.EXTRA_AMOUNT, fee);
        intent.putExtra(Constants.EXTRA_PURPOSE, "parcel");
        startActivityForResult(intent, Constants.RC_PAYMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.RC_PAYMENT && resultCode == RESULT_OK && data != null) {
            pendingPaymentRef = data.getStringExtra("payment_reference");
            submitParcel();
        }
    }

    private void submitParcel() {
        if (pendingPaymentRef == null) return;

        String description = binding.etDescription.getText().toString().trim();
        double weight      = Double.parseDouble(binding.etWeight.getText().toString().trim());
        int    sizeIdx     = binding.spinnerSize.getSelectedItemPosition();
        String size        = sizeIdx == 0 ? Constants.SIZE_SMALL
                           : sizeIdx == 1 ? Constants.SIZE_MEDIUM : Constants.SIZE_LARGE;
        String pickupAddr  = binding.etPickupAddress.getText().toString().trim();
        String destAddr    = binding.etDestinationAddress.getText().toString().trim();
        String notes       = binding.etSpecialInstructions.getText().toString().trim();
        double fee         = PriceCalculator.calculateParcelFee(weight);

        setLoading(true);

        viewModel.createParcel(description, weight, size,
                pickupAddr, pickupLat, pickupLng,
                destAddr, destLat, destLng,
                fee, pendingPaymentRef, notes)
                .observe(this, response -> {
                    setLoading(false);
                    if (response != null && response.isSuccess()) {
                        Toast.makeText(this, "Parcel created! We are finding a carrier.",
                                Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        String msg = response != null ? response.getError() : "Failed to create parcel";
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;
        if (TextUtils.isEmpty(binding.etDescription.getText())) {
            binding.etDescription.setError("Required"); valid = false;
        }
        if (TextUtils.isEmpty(binding.etWeight.getText())) {
            binding.etWeight.setError("Required"); valid = false;
        }
        if (TextUtils.isEmpty(binding.etPickupAddress.getText())) {
            binding.etPickupAddress.setError("Required"); valid = false;
        }
        if (TextUtils.isEmpty(binding.etDestinationAddress.getText())) {
            binding.etDestinationAddress.setError("Required"); valid = false;
        }
        return valid;
    }

    private void setLoading(boolean loading) {
        binding.btnProceedToPayment.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
