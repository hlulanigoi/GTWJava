package com.goinghatway.driver;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthManager {
    private static final String PREF_NAME = "driver_auth";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";

    private final SharedPreferences prefs;

    public AuthManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUser(String userId, String email) {
        prefs.edit().putString(KEY_USER_ID, userId).putString(KEY_EMAIL, email).apply();
    }

    public boolean isSignedIn() {
        return prefs.contains(KEY_USER_ID);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "guest");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "driver@example.com");
    }
}
