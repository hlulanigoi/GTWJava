package com.goinghatway.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.goinghatway.app.databinding.ActivityRegisterBinding;
import com.goinghatway.app.viewmodels.AuthViewModel;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        binding.btnRegister.setOnClickListener(v -> attemptRegister());

        binding.tvLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String fullName  = binding.etFullName.getText().toString().trim();
        String email     = binding.etEmail.getText().toString().trim();
        String phone     = binding.etPhone.getText().toString().trim();
        String password  = binding.etPassword.getText().toString().trim();
        String confirm   = binding.etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(fullName)) { binding.etFullName.setError("Required"); return; }
        if (TextUtils.isEmpty(email))    { binding.etEmail.setError("Required"); return; }
        if (TextUtils.isEmpty(phone))    { binding.etPhone.setError("Required"); return; }
        if (TextUtils.isEmpty(password)) { binding.etPassword.setError("Required"); return; }
        if (!password.equals(confirm))   {
            binding.etConfirmPassword.setError("Passwords do not match");
            return;
        }

        setLoading(true);

        viewModel.register(fullName, email, phone, password).observe(this, response -> {
            setLoading(false);
            if (response != null && response.isSuccess()) {
                Toast.makeText(this, "Account created! Welcome.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finishAffinity();
            } else {
                String msg = response != null ? response.getError() : "Registration failed";
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.btnRegister.setEnabled(!loading);
        binding.progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
