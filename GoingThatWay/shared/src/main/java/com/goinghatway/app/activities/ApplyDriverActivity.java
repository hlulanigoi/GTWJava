package com.goinghatway.app.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.goinghatway.app.databinding.ActivityApplyDriverBinding;
import com.goinghatway.app.viewmodels.AuthViewModel;

public class ApplyDriverActivity extends AppCompatActivity {

    private ActivityApplyDriverBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityApplyDriverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.btnSubmitApplication.setOnClickListener(v -> submitApplication());
    }

    private void submitApplication() {
        String licenseNumber = binding.etLicenseNumber.getText().toString().trim();
        String vehiclePlate  = binding.etVehiclePlate.getText().toString().trim();
        String vehicleModel  = binding.etVehicleModel.getText().toString().trim();

        boolean valid = true;
        if (TextUtils.isEmpty(licenseNumber)) {
            binding.etLicenseNumber.setError("Required"); valid = false;
        }
        if (TextUtils.isEmpty(vehiclePlate)) {
            binding.etVehiclePlate.setError("Required"); valid = false;
        }
        if (TextUtils.isEmpty(vehicleModel)) {
            binding.etVehicleModel.setError("Required"); valid = false;
        }
        if (!valid) return;

        setLoading(true);
        viewModel.applyAsDriver(licenseNumber, vehiclePlate, vehicleModel)
                .observe(this, response -> {
                    setLoading(false);
                    if (response != null && response.isSuccess()) {
                        Toast.makeText(this,
                                "Application submitted! You will be notified once approved.",
                                Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        String msg = response != null ? response.getError() : "Submission failed";
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        binding.btnSubmitApplication.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
