package com.goinghatway.app.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.goinghatway.app.R;
import com.goinghatway.app.databinding.ActivityPostTripBinding;
import com.goinghatway.app.utils.Constants;
import com.goinghatway.app.utils.NominatimAutocompleteHelper;
import com.goinghatway.app.viewmodels.TripViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PostTripActivity extends AppCompatActivity {

    private ActivityPostTripBinding binding;
    private TripViewModel viewModel;

    // Resolved coordinates from Nominatim (0,0 = not yet selected)
    private double originLat = 0, originLng = 0;
    private double destLat   = 0, destLng   = 0;

    // Selected timestamp millis (used to combine date + time picker results)
    private long departureDateMs = 0;
    private long arrivalDateMs   = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostTripBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(TripViewModel.class);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupAutocomplete();
        setupDateTimePickers();
        setupCorridorSlider();

        // The old Spinner is hidden — ChipGroup replaces it (chip_car selected by default)
        setupTransportSpinnerCompat();

        binding.btnPostTrip.setOnClickListener(v -> postTrip());
    }

    // ────────────────────────────────────────────────────────────────────────
    // OSM Nominatim autocomplete (Uber/Bolt-inspired — no API key needed)
    // ────────────────────────────────────────────────────────────────────────

    private void setupAutocomplete() {
        NominatimAutocompleteHelper.attach(
                binding.etOriginAddress,
                (lat, lng, address) -> { originLat = lat; originLng = lng; });

        NominatimAutocompleteHelper.attach(
                binding.etDestinationAddress,
                (lat, lng, address) -> { destLat = lat; destLng = lng; });
    }

    // ────────────────────────────────────────────────────────────────────────
    // Date + Time pickers (replaces free-text field)
    // ────────────────────────────────────────────────────────────────────────

    private void setupDateTimePickers() {
        binding.etDepartureTime.setOnClickListener(v ->
                showDateTimePicker("Departure", true));
        binding.etArrivalTime.setOnClickListener(v ->
                showDateTimePicker("Arrival", false));
    }

    private void showDateTimePicker(String label, boolean isDeparture) {
        long initialMs = isDeparture
                ? (departureDateMs > 0 ? departureDateMs : MaterialDatePicker.todayInUtcMilliseconds())
                : (arrivalDateMs   > 0 ? arrivalDateMs   : MaterialDatePicker.todayInUtcMilliseconds());

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText(label + " date")
                .setSelection(initialMs)
                .build();

        datePicker.addOnPositiveButtonClickListener(selectedDateMs -> {
            int defaultHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            int defaultMin  = 0;

            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(defaultHour)
                    .setMinute(defaultMin)
                    .setTitleText(label + " time")
                    .build();

            timePicker.addOnPositiveButtonClickListener(ignored -> {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(selectedDateMs);
                cal.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                cal.set(Calendar.MINUTE, timePicker.getMinute());
                cal.set(Calendar.SECOND, 0);

                SimpleDateFormat sdf = new SimpleDateFormat("EEE d MMM, HH:mm", Locale.getDefault());
                String display = sdf.format(cal.getTime());

                // ISO format for the API
                SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                String isoString = iso.format(cal.getTime());

                if (isDeparture) {
                    departureDateMs = cal.getTimeInMillis();
                    binding.etDepartureTime.setText(display);
                    binding.etDepartureTime.setTag(isoString);
                } else {
                    arrivalDateMs = cal.getTimeInMillis();
                    binding.etArrivalTime.setText(display);
                    binding.etArrivalTime.setTag(isoString);
                }
            });

            timePicker.show(getSupportFragmentManager(), "time_" + label);
        });

        datePicker.show(getSupportFragmentManager(), "date_" + label);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Corridor width slider (Bolt Driver Destinations-inspired)
    // ────────────────────────────────────────────────────────────────────────

    private void setupCorridorSlider() {
        binding.sliderCorridor.addOnChangeListener((slider, value, fromUser) -> {
            int km = Math.round(value);
            binding.tvCorridorLabel.setText(
                    "Accept pickups up to " + km + " km off my route");
            binding.tvCorridorNote.setText(
                    "Parcel requests within " + km + " km of this route will be matched");
        });
    }

    private int getCorridorKm() {
        return Math.round(binding.sliderCorridor.getValue());
    }

    // ────────────────────────────────────────────────────────────────────────
    // Transport mode — ChipGroup (replaces Spinner)
    // ────────────────────────────────────────────────────────────────────────

    private void setupTransportSpinnerCompat() {
        // Keep the hidden Spinner populated so any legacy code path still works
        String[] modes = {Constants.MODE_CAR, Constants.MODE_BUS,
                Constants.MODE_TRAIN, Constants.MODE_WALK, Constants.MODE_OTHER};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, modes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTransportMode.setAdapter(adapter);
    }

    private String getTransportMode() {
        int checkedId = binding.chipGroupTransport.getCheckedChipId();
        if (checkedId == R.id.chip_bus)   return Constants.MODE_BUS;
        if (checkedId == R.id.chip_train) return Constants.MODE_TRAIN;
        if (checkedId == R.id.chip_walk)  return Constants.MODE_WALK;
        if (checkedId == R.id.chip_other) return Constants.MODE_OTHER;
        return Constants.MODE_CAR; // default
    }

    // ────────────────────────────────────────────────────────────────────────
    // Form submission
    // ────────────────────────────────────────────────────────────────────────

    private void postTrip() {
        if (!validateForm()) return;

        String originAddr  = binding.etOriginAddress.getText().toString().trim();
        String destAddr    = binding.etDestinationAddress.getText().toString().trim();
        String departure   = getTag(binding.etDepartureTime, binding.etDepartureTime.getText().toString());
        String arrival     = getTag(binding.etArrivalTime, "");
        String mode        = getTransportMode();
        int    seats       = parseIntSafe(binding.etSeatsAvailable.getText().toString(), 1);
        String notes       = binding.etNotes.getText() != null
                ? binding.etNotes.getText().toString().trim() : "";
        int    corridorKm  = getCorridorKm();
        double minFare     = parseDoubleSafe(
                binding.etMinFare.getText() != null ? binding.etMinFare.getText().toString() : "", 0);

        setLoading(true);

        viewModel.createTrip(
                originAddr, originLat, originLng,
                destAddr,   destLat,   destLng,
                departure,  arrival,   mode, seats, notes,
                corridorKm, minFare
        ).observe(this, response -> {
            setLoading(false);
            if (response != null && response.isSuccess()) {
                Toast.makeText(this,
                        "Trip posted! We'll find matching parcel requests along your route.",
                        Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
                finish();
            } else {
                String msg = response != null ? response.getError() : "Failed to post trip";
                if (msg != null && msg.contains("403")) {
                    msg = "Only approved drivers can post trips. Please apply first.";
                }
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateForm() {
        boolean valid = true;

        if (TextUtils.isEmpty(binding.etOriginAddress.getText())) {
            binding.tilOrigin.setError("Origin required"); valid = false;
        } else {
            binding.tilOrigin.setError(null);
        }

        if (TextUtils.isEmpty(binding.etDestinationAddress.getText())) {
            binding.tilDestination.setError("Destination required"); valid = false;
        } else {
            binding.tilDestination.setError(null);
        }

        if (TextUtils.isEmpty(binding.etDepartureTime.getText())) {
            Toast.makeText(this, "Please select a departure time", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (TextUtils.isEmpty(binding.etSeatsAvailable.getText())) {
            Toast.makeText(this, "Please enter available seats / slots", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

    private void setLoading(boolean loading) {
        binding.btnPostTrip.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    /** Read the ISO string stored as a tag; fall back to raw text. */
    private String getTag(android.widget.EditText field, String fallback) {
        Object tag = field.getTag();
        return (tag instanceof String && !((String) tag).isEmpty()) ? (String) tag : fallback;
    }

    private int parseIntSafe(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return fallback; }
    }

    private double parseDoubleSafe(String s, double fallback) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return fallback; }
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
