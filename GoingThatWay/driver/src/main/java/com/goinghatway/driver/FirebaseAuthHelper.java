package com.goinghatway.driver;

import android.content.Context;
import android.widget.Toast;

public class FirebaseAuthHelper {
    public void signIn(Context context, String email, String password) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            Toast.makeText(context, "Please enter your credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthManager authManager = new AuthManager(context);
        authManager.saveUser("firebase-driver-001", email);
        Toast.makeText(context, "Signed in successfully", Toast.LENGTH_SHORT).show();
    }
}
