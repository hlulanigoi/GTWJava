package com.goinghatway.app.repositories;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.goinghatway.app.api.ApiClient;
import com.goinghatway.app.api.ApiService;
import com.goinghatway.app.api.responses.ApiResponse;
import com.goinghatway.app.models.Booking;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingRepository {

    private final ApiService api;

    public BookingRepository(Context context) {
        api = ApiClient.getService(context);
    }

    public MutableLiveData<ApiResponse<List<Booking>>> getMyBookings() {
        MutableLiveData<ApiResponse<List<Booking>>> result = new MutableLiveData<>();
        api.getMyBookings().enqueue(new Callback<ApiResponse<List<Booking>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Booking>>> call,
                                   Response<ApiResponse<List<Booking>>> response) {
                result.postValue(response.isSuccessful() ? response.body() : err("Failed to load bookings"));
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Booking>>> call, Throwable t) {
                result.postValue(err("Network error: " + t.getMessage()));
            }
        });
        return result;
    }

    public MutableLiveData<ApiResponse<Booking>> acceptBooking(String bookingId) {
        MutableLiveData<ApiResponse<Booking>> result = new MutableLiveData<>();
        api.acceptBooking(bookingId).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call,
                                   Response<ApiResponse<Booking>> response) {
                result.postValue(response.isSuccessful() ? response.body() : err("Accept failed"));
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                result.postValue(err("Network error: " + t.getMessage()));
            }
        });
        return result;
    }

    public MutableLiveData<ApiResponse<Booking>> rejectBooking(String bookingId) {
        MutableLiveData<ApiResponse<Booking>> result = new MutableLiveData<>();
        api.rejectBooking(bookingId).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call,
                                   Response<ApiResponse<Booking>> response) {
                result.postValue(response.isSuccessful() ? response.body() : err("Reject failed"));
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                result.postValue(err("Network error: " + t.getMessage()));
            }
        });
        return result;
    }

    public MutableLiveData<ApiResponse<Booking>> markPickedUp(String bookingId) {
        MutableLiveData<ApiResponse<Booking>> result = new MutableLiveData<>();
        api.markPickedUp(bookingId).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call,
                                   Response<ApiResponse<Booking>> response) {
                result.postValue(response.isSuccessful() ? response.body() : err("Failed"));
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                result.postValue(err("Network error: " + t.getMessage()));
            }
        });
        return result;
    }

    public MutableLiveData<ApiResponse<Booking>> markCompleted(String bookingId) {
        MutableLiveData<ApiResponse<Booking>> result = new MutableLiveData<>();
        api.markCompleted(bookingId).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call,
                                   Response<ApiResponse<Booking>> response) {
                result.postValue(response.isSuccessful() ? response.body() : err("Failed"));
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
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
