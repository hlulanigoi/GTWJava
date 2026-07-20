package com.goinghatway.shared;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "driver_session";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_NAME = "user_name";
    private static final String KEY_EMAIL = "user_email";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String name, String email) {
        prefs.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_NAME, name)
                .putString(KEY_EMAIL, email)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public String getName() {
        return prefs.getString(KEY_NAME, "Driver");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "driver@example.com");
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
