package com.goinghatway.driver;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.goinghatway.driver.databinding.ActivityMainBinding;
import com.goinghatway.driver.ui.ProfileActivity;
import com.goinghatway.driver.ui.TripFormActivity;
import com.goinghatway.driver.ui.TripListActivity;
import com.goinghatway.shared.SessionManager;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SessionManager sessionManager = new SessionManager(this);
        binding.tvWelcome.setText(getString(R.string.welcome_driver, sessionManager.getName()));

        binding.btnPostTrip.setOnClickListener(v ->
                startActivity(new Intent(this, TripFormActivity.class)));

        binding.btnMyTrips.setOnClickListener(v ->
                startActivity(new Intent(this, TripListActivity.class)));

        binding.btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }
}
