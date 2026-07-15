package com.goinghatway.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.goinghatway.app.api.responses.ApiResponse;
import com.goinghatway.app.api.responses.AuthResponse;
import com.goinghatway.app.models.User;
import com.goinghatway.app.repositories.AuthRepository;

public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository repo;

    public AuthViewModel(@NonNull Application app) {
        super(app);
        repo = new AuthRepository(app);
    }

    public MutableLiveData<ApiResponse<AuthResponse>> login(String email, String password) {
        return repo.login(email, password);
    }

    public MutableLiveData<ApiResponse<AuthResponse>> register(
            String fullName, String email, String phone, String password) {
        return repo.register(fullName, email, phone, password);
    }

    public MutableLiveData<ApiResponse<User>> getMe() {
        return repo.getMe();
    }
}
