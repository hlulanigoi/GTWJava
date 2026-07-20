package com.goinghatway.app.repositories;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.goinghatway.app.api.ApiClient;
import com.goinghatway.app.api.ApiService;
import com.goinghatway.app.api.responses.ApiResponse;
import com.goinghatway.app.models.Ticket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketRepository {

    private final ApiService api;

    public TicketRepository(Context context) {
        api = ApiClient.getService(context);
    }

    public MutableLiveData<ApiResponse<List<Ticket>>> getMyTickets() {
        MutableLiveData<ApiResponse<List<Ticket>>> result = new MutableLiveData<>();
        api.getMyTickets().enqueue(new Callback<ApiResponse<List<Ticket>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Ticket>>> call,
                                   Response<ApiResponse<List<Ticket>>> response) {
                result.postValue(response.isSuccessful() ? response.body() : err("Failed to load tickets"));
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Ticket>>> call, Throwable t) {
                result.postValue(err("Network error: " + t.getMessage()));
            }
        });
        return result;
    }

    public MutableLiveData<ApiResponse<Map<String, Double>>> getTicketPrice() {
        MutableLiveData<ApiResponse<Map<String, Double>>> result = new MutableLiveData<>();
        api.getTicketPrice().enqueue(new Callback<ApiResponse<Map<String, Double>>>() {
            @Override
            public void onResponse(Call<ApiResponse<Map<String, Double>>> call,
                                   Response<ApiResponse<Map<String, Double>>> response) {
                result.postValue(response.isSuccessful() ? response.body() : err("Could not fetch price"));
            }

            @Override
            public void onFailure(Call<ApiResponse<Map<String, Double>>> call, Throwable t) {
                result.postValue(err("Network error: " + t.getMessage()));
            }
        });
        return result;
    }

    public MutableLiveData<ApiResponse<Ticket>> purchaseTicket(String paymentReference) {
        MutableLiveData<ApiResponse<Ticket>> result = new MutableLiveData<>();
        Map<String, String> body = new HashMap<>();
        body.put("payment_reference", paymentReference);

        api.purchaseTicket(body).enqueue(new Callback<ApiResponse<Ticket>>() {
            @Override
            public void onResponse(Call<ApiResponse<Ticket>> call,
                                   Response<ApiResponse<Ticket>> response) {
                result.postValue(response.isSuccessful() ? response.body() : err("Purchase failed"));
            }

            @Override
            public void onFailure(Call<ApiResponse<Ticket>> call, Throwable t) {
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
