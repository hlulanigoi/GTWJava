package com.goinghatway.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.goinghatway.app.models.User;

public class SessionManager {

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveSession(String token, User user) {
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
        editor.putString(Constants.KEY_TOKEN, token);
        editor.putString(Constants.KEY_USER_ID, user.getId());
        editor.putString(Constants.KEY_USER_NAME, user.getFullName());
        editor.putString(Constants.KEY_USER_EMAIL, user.getEmail());
        editor.putString(Constants.KEY_USER_ROLE, user.getRole() != null ? user.getRole() : User.ROLE_USER);
        editor.apply();
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
    }

    public boolean isAdmin() {
        return User.ROLE_ADMIN.equals(prefs.getString(Constants.KEY_USER_ROLE, User.ROLE_USER));
    }

    public String getToken() { return prefs.getString(Constants.KEY_TOKEN, null); }
    public String getUserId() { return prefs.getString(Constants.KEY_USER_ID, null); }
    public String getUserName() { return prefs.getString(Constants.KEY_USER_NAME, null); }
    public String getUserEmail() { return prefs.getString(Constants.KEY_USER_EMAIL, null); }
    public String getUserRole() { return prefs.getString(Constants.KEY_USER_ROLE, User.ROLE_USER); }
}
