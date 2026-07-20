package com.goinghatway.app.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.goinghatway.app.databinding.ActivityPostTripBinding;
import com.goinghatway.app.utils.Constants;
import com.goinghatway.app.viewmodels.TripViewModel;

public class PostTripActivity extends AppCompatActivity {

    private ActivityPostTripBinding binding;
    private TripViewModel viewModel;

    private double originLat, originLng, destLat, destLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostTripBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(TripViewModel.class);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupTransportModeSpinner();

        binding.btnPostTrip.setOnClickListener(v -> postTrip());
    }

    private void setupTransportModeSpinner() {
        String[] modes = {Constants.MODE_CAR, Constants.MODE_BUS,
                          Constants.MODE_TRAIN, Constants.MODE_WALK, Constants.MODE_OTHER};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, modes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTransportMode.setAdapter(adapter);
    }

    private void postTrip() {
        if (!validateForm()) return;

        String originAddr    = binding.etOriginAddress.getText().toString().trim();
        String destAddr      = binding.etDestinationAddress.getText().toString().trim();
        String departure     = binding.etDepartureTime.getText().toString().trim();
        String arrival       = binding.etArrivalTime.getText().toString().trim();
        String transportMode = binding.spinnerTransportMode.getSelectedItem().toString();
        int    seatsAvailable= Integer.parseInt(binding.etSeatsAvailable.getText().toString().trim());
        String notes         = binding.etNotes.getText().toString().trim();

        setLoading(true);

        viewModel.createTrip(originAddr, originLat, originLng,
                destAddr, destLat, destLng,
                departure, arrival, transportMode, seatsAvailable, notes)
                .observe(this, response -> {
                    setLoading(false);
                    if (response != null && response.isSuccess()) {
                        Toast.makeText(this, "Trip posted! We will find matching ride requests.",
                                Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        String msg = response != null ? response.getError() : "Failed to post trip";
                        // Surface 403 "Only approved association drivers" clearly
                        if (msg != null && msg.contains("403")) {
                            msg = "Only approved association drivers can post trips. Please apply as a driver first.";
                        }
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;
        if (TextUtils.isEmpty(binding.etOriginAddress.getText())) {
            binding.etOriginAddress.setError("Required"); valid = false;
        }
        if (TextUtils.isEmpty(binding.etDestinationAddress.getText())) {
            binding.etDestinationAddress.setError("Required"); valid = false;
        }
        if (TextUtils.isEmpty(binding.etDepartureTime.getText())) {
            binding.etDepartureTime.setError("Required"); valid = false;
        }
        if (TextUtils.isEmpty(binding.etSeatsAvailable.getText())) {
            binding.etSeatsAvailable.setError("Required"); valid = false;
        }
        return valid;
    }

    private void setLoading(boolean loading) {
        binding.btnPostTrip.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
