package com.goinghatway.app.repositories;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.goinghatway.app.api.ApiClient;
import com.goinghatway.app.api.ApiService;
import com.goinghatway.app.api.responses.ApiResponse;
import com.goinghatway.app.models.Match;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchRepository {

    private final ApiService api;

    public MatchRepository(Context context) {
        api = ApiClient.getService(context);
    }

    public MutableLiveData<ApiResponse<List<Match>>> getMyMatches() {
        MutableLiveData<ApiResponse<List<Match>>> result = new MutableLiveData<>();
        api.getMyMatches().enqueue(new Callback<ApiResponse<List<Match>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Match>>> call,
                                   Response<ApiResponse<List<Match>>> response) {
                result.postValue(response.isSuccessful() ? response.body() : err("Failed to load matches"));
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Match>>> call, Throwable t) {
                result.postValue(err("Network error: " + t.getMessage()));
            }
        });
        return result;
    }

    public MutableLiveData<ApiResponse<Match>> acceptMatch(String matchId) {
        MutableLiveData<ApiResponse<Match>> result = new MutableLiveData<>();
        api.acceptMatch(matchId).enqueue(new Callback<ApiResponse<Match>>() {
            @Override
            public void onResponse(Call<ApiResponse<Match>> call,
                                   Response<ApiResponse<Match>> response) {
                result.postValue(response.isSuccessful() ? response.body() : err("Accept failed"));
            }

            @Override
            public void onFailure(Call<ApiResponse<Match>> call, Throwable t) {
                result.postValue(err("Network error: " + t.getMessage()));
            }
        });
        return result;
    }

    public MutableLiveData<ApiResponse<Match>> rejectMatch(String matchId) {
        MutableLiveData<ApiResponse<Match>> result = new MutableLiveData<>();
        api.rejectMatch(matchId).enqueue(new Callback<ApiResponse<Match>>() {
            @Override
            public void onResponse(Call<ApiResponse<Match>> call,
                                   Response<ApiResponse<Match>> response) {
                result.postValue(response.isSuccessful() ? response.body() : err("Reject failed"));
            }

            @Override
            public void onFailure(Call<ApiResponse<Match>> call, Throwable t) {
                result.postValue(err("Network error: " + t.getMessage()));
            }
        });
        return result;
    }

    public MutableLiveData<ApiResponse<Match>> markCollected(String matchId) {
        MutableLiveData<ApiResponse<Match>> result = new MutableLiveData<>();
        api.markCollected(matchId).enqueue(new Callback<ApiResponse<Match>>() {
            @Override
            public void onResponse(Call<ApiResponse<Match>> call,
                                   Response<ApiResponse<Match>> response) {
                result.postValue(response.isSuccessful() ? response.body() : err("Failed"));
            }

            @Override
            public void onFailure(Call<ApiResponse<Match>> call, Throwable t) {
                result.postValue(err("Network error: " + t.getMessage()));
            }
        });
        return result;
    }

    public MutableLiveData<ApiResponse<Match>> markDelivered(String matchId) {
        MutableLiveData<ApiResponse<Match>> result = new MutableLiveData<>();
        api.markDelivered(matchId).enqueue(new Callback<ApiResponse<Match>>() {
            @Override
            public void onResponse(Call<ApiResponse<Match>> call,
                                   Response<ApiResponse<Match>> response) {
                result.postValue(response.isSuccessful() ? response.body() : err("Failed"));
            }

            @Override
            public void onFailure(Call<ApiResponse<Match>> call, Throwable t) {
                result.postValue(err("Network error: " + t.getMessage()));
            }
        });
        return result;
    }

    private <T> ApiResponse<T> err(String msg) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setSuccess(false);
        r.setError(msg);
        return r;
    }
}
