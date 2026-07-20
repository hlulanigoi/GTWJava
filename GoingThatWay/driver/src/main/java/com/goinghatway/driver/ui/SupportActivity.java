package com.goinghatway.driver.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.goinghatway.driver.databinding.ActivitySupportBinding;

public class SupportActivity extends AppCompatActivity {
    private ActivitySupportBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySupportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvSupportMessage.setText("Need help? Our support team is available 24/7 for trip issues, account help, and safety concerns.");
        binding.btnCallSupport.setText("Call Support");
        binding.btnShareTripDetails.setText("Share Trip Details");
    }
}
