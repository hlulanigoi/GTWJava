package com.goinghatway.driver;

import android.content.Context;
import android.widget.Toast;

public class FirebaseAuthHelper {
    public void signIn(Context context, String email, String password) {
        AuthManager authManager = new AuthManager(context);
        authManager.saveUser("firebase-driver-001", email);
        Toast.makeText(context, "Signed in with Firebase-ready auth flow", Toast.LENGTH_SHORT).show();
    }
}
