package com.goinghatway.app.repositories;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.goinghatway.app.api.ApiClient;
import com.goinghatway.app.api.ApiService;
import com.goinghatway.app.api.responses.ApiResponse;
import com.goinghatway.app.api.responses.PaginatedResponse;
import com.goinghatway.app.models.Ride;
import com.goinghatway.app.utils.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideRepository {

    private final ApiService api;

    public RideRepository(Context context) {
        api = ApiClient.getService(context);
    }

    public MutableLiveData<ApiResponse<PaginatedResponse<Ride>>> getRides(
            double lat, double lng, double radiusKm, String status) {
        MutableLiveData<ApiResponse<PaginatedResponse<Ride>>> result = new MutableLiveData<>();
        api.getRides(lat, lng, radiusKm, status).enqueue(
                new Callback<ApiResponse<PaginatedResponse<Ride>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PaginatedResponse<Ride>>> call,
                                           Response<ApiResponse<PaginatedResponse<Ride>>> response) {
                        result.postValue(response.isSuccessful() ? response.body() : errorResponse("Failed to load rides"));
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<PaginatedResponse<Ride>>> call, Throwable t) {
                        result.postValue(errorResponse("Network error: " + t.getMessage()));
                    }
                });
        return result;
    }

    public MutableLiveData<ApiResponse<List<Ride>>> getMyRides() {
        MutableLiveData<ApiResponse<List<Ride>>> result = new MutableLiveData<>();
        api.getMyRides().enqueue(new Callback<ApiResponse<List<Ride>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Ride>>> call,
                                   Response<ApiResponse<List<Ride>>> response) {
                result.postValue(response.isSuccessful() ? response.body() : errorResponse("Failed to load your rides"));
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Ride>>> call, Throwable t) {
                result.postValue(errorResponse("Network error: " + t.getMessage()));
            }
        });
        return result;
    }

    public MutableLiveData<ApiResponse<Ride>> getRide(String id) {
        MutableLiveData<ApiResponse<Ride>> result = new MutableLiveData<>();
        api.getRide(id).enqueue(new Callback<ApiResponse<Ride>>() {
            @Override
            public void onResponse(Call<ApiResponse<Ride>> call,
                                   Response<ApiResponse<Ride>> response) {
                result.postValue(response.isSuccessful() ? response.body() : errorResponse("Ride not found"));
            }

            @Override
            public void onFailure(Call<ApiResponse<Ride>> call, Throwable t) {
                result.postValue(errorResponse("Network error: " + t.getMessage()));
            }
        });
        return result;
    }

    public MutableLiveData<ApiResponse<Ride>> createRide(
            String notes, int passengerCount, String luggageSize,
            String pickupAddress, double pickupLat, double pickupLng,
            String destAddress, double destLat, double destLng,
            double fare, String paymentRef) {

        MutableLiveData<ApiResponse<Ride>> result = new MutableLiveData<>();

        Map<String, Object> body = new HashMap<>();
        body.put("ride_type", Ride.TYPE_SCHEDULED);
        body.put("notes", notes);
        body.put("passenger_count", passengerCount);
        body.put("luggage_size", luggageSize);
        body.put("pickup_address", pickupAddress);
        body.put("pickup_lat", pickupLat);
        body.put("pickup_lng", pickupLng);
        body.put("destination_address", destAddress);
        body.put("destination_lat", destLat);
        body.put("destination_lng", destLng);
        body.put("fare", fare);
        body.put("driver_earning", fare * Constants.CARRIER_SHARE_PERCENT);
        body.put("platform_fee", fare * Constants.PLATFORM_FEE_PERCENT);
        body.put("payment_reference", paymentRef);

        api.createRide(body).enqueue(new Callback<ApiResponse<Ride>>() {
            @Override
            public void onResponse(Call<ApiResponse<Ride>> call,
                                   Response<ApiResponse<Ride>> response) {
                result.postValue(response.isSuccessful() ? response.body() : errorResponse("Failed to create ride"));
            }

            @Override
            public void onFailure(Call<ApiResponse<Ride>> call, Throwable t) {
                result.postValue(errorResponse("Network error: " + t.getMessage()));
            }
        });
        return result;
    }

    public MutableLiveData<ApiResponse<Ride>> createOnDemandRide(
            double pickupLat, double pickupLng,
            String destAddress, int passengerCount, String luggageSize,
            String paymentRef) {

        MutableLiveData<ApiResponse<Ride>> result = new MutableLiveData<>();

        Map<String, Object> body = new HashMap<>();
        body.put("pickup_lat", pickupLat);
        body.put("pickup_lng", pickupLng);
        body.put("destination_address", destAddress);
        body.put("passenger_count", passengerCount);
        body.put("luggage_size", luggageSize);
        body.put("payment_reference", paymentRef);

        api.createOnDemandRide(body).enqueue(new Callback<ApiResponse<Ride>>() {
            @Override
            public void onResponse(Call<ApiResponse<Ride>> call,
                                   Response<ApiResponse<Ride>> response) {
                result.postValue(response.isSuccessful() ? response.body() : errorResponse("Failed to request ride"));
            }

            @Override
            public void onFailure(Call<ApiResponse<Ride>> call, Throwable t) {
                result.postValue(errorResponse("Network error: " + t.getMessage()));
            }
        });
        return result;
    }

    // Generic error response helper
    private <T> ApiResponse<T> errorResponse(String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setSuccess(false);
        r.setError(message);
        return r;
    }
}
