package com.goinghatway.driver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.goinghatway.driver.databinding.ActivityMainBinding;
import com.goinghatway.driver.ui.EarningsActivity;
import com.goinghatway.driver.ui.ProfileActivity;
import com.goinghatway.driver.ui.SupportActivity;
import com.goinghatway.driver.ui.TripFormActivity;
import com.goinghatway.driver.ui.TripListActivity;
import com.goinghatway.driver.ui.TripRequestsActivity;
import com.goinghatway.driver.utils.DriverAvailabilityFormatter;
import com.goinghatway.shared.SessionManager;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private final DriverAvailabilityFormatter availabilityFormatter = new DriverAvailabilityFormatter();
    private boolean isOnline = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SessionManager sessionManager = new SessionManager(this);
        binding.tvWelcome.setText(getString(R.string.welcome_driver, sessionManager.getName()));
        updateAvailabilityUi();

        binding.btnPostTrip.setOnClickListener(v ->
                startActivity(new Intent(this, TripFormActivity.class)));

        binding.btnMyTrips.setOnClickListener(v ->
                startActivity(new Intent(this, TripListActivity.class)));

        binding.btnProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        binding.btnEarnings.setOnClickListener(v ->
                startActivity(new Intent(this, EarningsActivity.class)));

        binding.btnSupport.setOnClickListener(v ->
                startActivity(new Intent(this, SupportActivity.class)));

        binding.btnTripRequests.setOnClickListener(v ->
                startActivity(new Intent(this, TripRequestsActivity.class)));

        binding.btnSupport.setOnClickListener(v -> {
            requestNotificationPermissionIfNeeded();
            Toast.makeText(this, "Notifications are ready for trip updates", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SupportActivity.class));
        });

        requestNotificationPermissionIfNeeded();
        initializeAuthState();
        binding.tvStatus.setOnClickListener(v -> toggleAvailability());
    }

    private void initializeAuthState() {
        AuthManager authManager = new AuthManager(this);
        if (!authManager.isSignedIn()) {
            authManager.saveUser("firebase-driver-001", "driver@example.com");
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }
    }

    private void toggleAvailability() {
        isOnline = !isOnline;
        updateAvailabilityUi();
        if (isOnline) {
            new NotificationScheduler().scheduleTripReminder(this);
        }
        Toast.makeText(this, isOnline ? "You are now available" : "You are now offline", Toast.LENGTH_SHORT).show();
    }

    private void updateAvailabilityUi() {
        binding.tvStatus.setText(availabilityFormatter.getStatusMessage(isOnline));
        binding.btnPostTrip.setText(isOnline ? getString(R.string.post_trip) : "Post Trip Later");
    }
}
