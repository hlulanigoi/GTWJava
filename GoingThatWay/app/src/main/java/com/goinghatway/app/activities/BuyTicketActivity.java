package com.goinghatway.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.goinghatway.app.databinding.ActivityBuyTicketBinding;
import com.goinghatway.app.utils.Constants;
import com.goinghatway.app.viewmodels.TicketViewModel;

import java.util.Locale;

public class BuyTicketActivity extends AppCompatActivity {

    private ActivityBuyTicketBinding binding;
    private TicketViewModel viewModel;
    private double ticketPrice = Constants.DEFAULT_TICKET_PRICE;
    private String pendingPaymentRef = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBuyTicketBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(TicketViewModel.class);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadTicketPrice();

        binding.btnBuyTicket.setOnClickListener(v -> proceedToPayment());
    }

    private void loadTicketPrice() {
        viewModel.getTicketPrice().observe(this, response -> {
            if (response != null && response.isSuccess() && response.getData() != null) {
                Double price = response.getData().get("price");
                if (price != null) ticketPrice = price;
            }
            binding.tvTicketPrice.setText(
                    String.format(Locale.getDefault(), "R %.2f per ticket", ticketPrice));
        });
    }

    private void proceedToPayment() {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra(Constants.EXTRA_AMOUNT, ticketPrice);
        intent.putExtra(Constants.EXTRA_PURPOSE, "ticket");
        startActivityForResult(intent, Constants.RC_PAYMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.RC_PAYMENT && resultCode == RESULT_OK && data != null) {
            pendingPaymentRef = data.getStringExtra("payment_reference");
            activateTicket();
        }
    }

    private void activateTicket() {
        if (pendingPaymentRef == null) return;
        setLoading(true);
        viewModel.purchaseTicket(pendingPaymentRef).observe(this, response -> {
            setLoading(false);
            if (response != null && response.isSuccess()) {
                Toast.makeText(this, "Ticket purchased! You can now collect parcels.",
                        Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
                finish();
            } else {
                String msg = response != null ? response.getError() : "Purchase failed";
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.btnBuyTicket.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
