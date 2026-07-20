package com.goinghatway.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.goinghatway.app.R;
import com.goinghatway.app.databinding.ActivityCreateParcelBinding;
import com.goinghatway.app.utils.Constants;
import com.goinghatway.app.viewmodels.ParcelViewModel;

import java.util.Locale;

public class CreateParcelActivity extends AppCompatActivity {

    private ActivityCreateParcelBinding binding;
    private ParcelViewModel viewModel;

    // Locations resolved from address inputs (set after map/geocode)
    private double pickupLat, pickupLng, destLat, destLng;
    private String pendingPaymentRef = null; // set after payment flow

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateParcelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ParcelViewModel.class);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupSizeSpinner();
        setupFeeCalculator();

        binding.btnProceedToPayment.setOnClickListener(v -> proceedToPayment());
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
        // Update fee preview whenever weight changes
        binding.etWeight.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) updateFeePreview();
        });
    }

    private void updateFeePreview() {
        String weightStr = binding.etWeight.getText().toString().trim();
        if (TextUtils.isEmpty(weightStr)) return;
        try {
            double weight = Double.parseDouble(weightStr);
            double fee = calculateFee(weight);
            double yourEarning = fee * Constants.CARRIER_SHARE_PERCENT;
            double platform    = fee * Constants.PLATFORM_FEE_PERCENT;
            binding.tvFeeText.setText(String.format(Locale.getDefault(),
                    "Total fee: R%.2f  |  Carrier earns: R%.2f  |  Platform: R%.2f",
                    fee, yourEarning, platform));
            binding.tvFeeBreakdown.setVisibility(View.VISIBLE);
        } catch (NumberFormatException ignored) {}
    }

    /** Simple fee schedule — adjust to your pricing model */
    private double calculateFee(double weightKg) {
        if (weightKg <= 1)  return 30.0;
        if (weightKg <= 5)  return 60.0;
        if (weightKg <= 15) return 120.0;
        return 200.0;
    }

    private void proceedToPayment() {
        if (!validateForm()) return;

        double weight = Double.parseDouble(binding.etWeight.getText().toString().trim());
        double fee    = calculateFee(weight);

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
        double fee         = calculateFee(weight);

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
