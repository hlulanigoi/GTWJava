package com.goinghatway.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.goinghatway.app.api.responses.ApiResponse;
import com.goinghatway.app.models.User;
import com.goinghatway.app.repositories.AuthRepository;

import java.util.HashMap;
import java.util.Map;

public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository repo;

    public AuthViewModel(@NonNull Application app) {
        super(app);
        repo = new AuthRepository(app);
    }

    public MutableLiveData<ApiResponse<com.goinghatway.app.api.responses.AuthResponse>> login(String email, String password) {
        return repo.login(email, password);
    }

    public MutableLiveData<ApiResponse<com.goinghatway.app.api.responses.AuthResponse>> register(
            String fullName, String email, String phone, String password) {
        return repo.register(fullName, email, phone, password);
    }

    public MutableLiveData<ApiResponse<User>> getMe() {
        return repo.getMe();
    }

    public MutableLiveData<ApiResponse<User>> applyAsDriver(
            String licenseNumber, String vehiclePlate, String vehicleModel) {
        return repo.applyAsDriver(licenseNumber, vehiclePlate, vehicleModel);
    }
}
