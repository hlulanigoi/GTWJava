package com.goinghatway.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.goinghatway.app.activities.admin.AdminMainActivity;
import com.goinghatway.app.utils.OsmMapUtils;
import com.goinghatway.app.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.goinghatway.app.R.layout.activity_splash);

        // Initialise osmdroid once at app start
        OsmMapUtils.init(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SessionManager session = new SessionManager(this);
            Intent intent;
            if (session.isLoggedIn()) {
                if (session.isAdmin()) {
                    intent = new Intent(this, AdminMainActivity.class);
                } else {
                    intent = new Intent(this, MainActivity.class);
                }
            } else {
                intent = new Intent(this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, SPLASH_DELAY_MS);
    }
}
