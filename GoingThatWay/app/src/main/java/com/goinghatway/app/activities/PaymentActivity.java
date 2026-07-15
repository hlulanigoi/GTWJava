package com.goinghatway.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.goinghatway.app.api.ApiClient;
import com.goinghatway.app.api.responses.ApiResponse;
import com.goinghatway.app.databinding.ActivityPaymentBinding;
import com.goinghatway.app.utils.Constants;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Payment screen — shows bank account details and lets the user paste
 * their payment reference once they've done an EFT/bank transfer.
 *
 * For production, replace with your bank's mobile SDK or a payment
 * gateway (e.g. PayFast, Peach Payments, Paystack).
 */
public class PaymentActivity extends AppCompatActivity {

    private ActivityPaymentBinding binding;
    private String serverReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        double amount  = getIntent().getDoubleExtra(Constants.EXTRA_AMOUNT, 0.0);
        String purpose = getIntent().getStringExtra(Constants.EXTRA_PURPOSE);

        binding.tvAmountDue.setText(String.format(Locale.getDefault(), "R %.2f", amount));
        binding.tvPurpose.setText("Payment for: " + (purpose != null ? purpose : "service"));

        // Display static bank details (replace with your real banking details)
        binding.tvBankName.setText("First National Bank (FNB)");
        binding.tvAccountName.setText("Going That Way (Pty) Ltd");
        binding.tvAccountNumber.setText("62 000 000 001");
        binding.tvBranchCode.setText("250 655");

        binding.btnConfirmPayment.setEnabled(false);
        binding.tvReference.setText("Generating reference…");
        requestServerReference(amount, purpose);

        binding.btnConfirmPayment.setOnClickListener(v -> confirmPayment());
    }

    /** Server generates the reference (UUID-based) instead of a client-side timestamp. */
    private void requestServerReference(double amount, String purpose) {
        Map<String, Object> body = new HashMap<>();
        body.put("amount", amount);
        body.put("purpose", purpose != null ? purpose : "PARCEL");

        ApiClient.getService(this).initiateParcelPayment(body).enqueue(new Callback<ApiResponse<Map<String, String>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, String>>> call,
                                    Response<ApiResponse<Map<String, String>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()
                        && response.body().getData() != null) {
                    serverReference = response.body().getData().get("reference");
                    binding.tvReference.setText(serverReference);
                    binding.btnConfirmPayment.setEnabled(true);
                } else {
                    binding.tvReference.setText("Failed to generate reference");
                    Toast.makeText(PaymentActivity.this, "Could not reach the server. Pull to retry.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, String>>> call, Throwable t) {
                binding.tvReference.setText("Failed to generate reference");
                Toast.makeText(PaymentActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmPayment() {
        if (TextUtils.isEmpty(serverReference)) {
            Toast.makeText(this, "Please wait for the payment reference to load", Toast.LENGTH_SHORT).show();
            return;
        }

        // Return the server-issued reference to the calling activity
        Intent result = new Intent();
        result.putExtra("payment_reference", serverReference);
        setResult(RESULT_OK, result);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_CANCELED);
        finish();
        return true;
    }
}
