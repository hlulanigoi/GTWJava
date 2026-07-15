package com.goinghatway.app.repositories;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.goinghatway.app.api.ApiClient;
import com.goinghatway.app.api.ApiService;
import com.goinghatway.app.api.responses.ApiResponse;
import com.goinghatway.app.api.responses.AuthResponse;
import com.goinghatway.app.models.User;
import com.goinghatway.app.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final ApiService api;
    private final SessionManager session;

    public AuthRepository(Context context) {
        api = ApiClient.getService(context);
        session = new SessionManager(context);
    }

    public MutableLiveData<ApiResponse<AuthResponse>> login(String email, String password) {
        MutableLiveData<ApiResponse<AuthResponse>> result = new MutableLiveData<>();
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        api.login(body).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call,
                                   Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AuthResponse> apiResp = response.body();
                    if (apiResp.isSuccess() && apiResp.getData() != null) {
                        session.saveSession(apiResp.getData().getToken(),
                                apiResp.getData().getUser());
                    }
                    result.postValue(apiResp);
                } else {
                    ApiResponse<AuthResponse> err = new ApiResponse<>();
                    err.setSuccess(false);
                    err.setError("Login failed. Please check your credentials.");
                    result.postValue(err);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                ApiResponse<AuthResponse> err = new ApiResponse<>();
                err.setSuccess(false);
                err.setError("Network error: " + t.getMessage());
                result.postValue(err);
            }
        });
        return result;
    }

    public MutableLiveData<ApiResponse<AuthResponse>> register(
            String fullName, String email, String phone, String password) {
        MutableLiveData<ApiResponse<AuthResponse>> result = new MutableLiveData<>();
        Map<String, String> body = new HashMap<>();
        body.put("full_name", fullName);
        body.put("email", email);
        body.put("phone", phone);
        body.put("password", password);

        api.register(body).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call,
                                   Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AuthResponse> apiResp = response.body();
                    if (apiResp.isSuccess() && apiResp.getData() != null) {
                        session.saveSession(apiResp.getData().getToken(),
                                apiResp.getData().getUser());
                    }
                    result.postValue(apiResp);
                } else {
                    ApiResponse<AuthResponse> err = new ApiResponse<>();
                    err.setSuccess(false);
                    err.setError("Registration failed. Please try again.");
                    result.postValue(err);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                ApiResponse<AuthResponse> err = new ApiResponse<>();
                err.setSuccess(false);
                err.setError("Network error: " + t.getMessage());
                result.postValue(err);
            }
        });
        return result;
    }

    public MutableLiveData<ApiResponse<User>> getMe() {
        MutableLiveData<ApiResponse<User>> result = new MutableLiveData<>();
        api.getMe().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call,
                                   Response<ApiResponse<User>> response) {
                result.postValue(response.isSuccessful() ? response.body() : null);
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                result.postValue(null);
            }
        });
        return result;
    }
}
