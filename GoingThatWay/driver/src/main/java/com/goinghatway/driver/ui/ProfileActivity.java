package com.goinghatway.driver.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.goinghatway.driver.databinding.ActivityProfileBinding;
import com.goinghatway.driver.R;
import com.goinghatway.shared.SessionManager;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SessionManager sessionManager = new SessionManager(this);
        binding.tvName.setText(sessionManager.getName());
        binding.tvEmail.setText(sessionManager.getEmail());
        binding.tvStatus.setText("Available for rides");
    }
}
