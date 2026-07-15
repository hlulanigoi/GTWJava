package com.goinghatway.app.repositories;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.goinghatway.app.api.ApiClient;
import com.goinghatway.app.api.ApiService;
import com.goinghatway.app.api.responses.ApiResponse;
import com.goinghatway.app.api.responses.PaginatedResponse;
import com.goinghatway.app.models.Parcel;
import com.goinghatway.app.utils.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParcelRepository {

    private final ApiService api;

    public ParcelRepository(Context context) {
        api = ApiClient.getService(context);
    }

    public MutableLiveData<ApiResponse<PaginatedResponse<Parcel>>> getParcels(
            int page, String status, double lat, double lng, double radiusKm) {
        MutableLiveData<ApiResponse<PaginatedResponse<Parcel>>> result = new MutableLiveData<>();
        api.getParcels(page, status, lat, lng, radiusKm).enqueue(
                new Callback<ApiResponse<PaginatedResponse<Parcel>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PaginatedResponse<Parcel>>> call,
                                           Response<ApiResponse<PaginatedResponse<Parcel>>> response) {
                        result.postValue(response.isSuccessful() ? response.body() : errorResponse("Failed to load parcels"));
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<PaginatedResponse<Parcel>>> call, Throwable t) {
                        result.postValue(errorResponse("Network error: " + t.getMessage()));
                    }
                });
        return result;
    }

    public MutableLiveData<ApiResponse<List<Parcel>>> getMyParcels() {
        MutableLiveData<ApiResponse<List<Parcel>>> result = new MutableLiveData<>();
        api.getMyParcels().enqueue(new Callback<ApiResponse<List<Parcel>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Parcel>>> call,
                                   Response<ApiResponse<List<Parcel>>> response) {
                result.postValue(response.isSuccessful() ? response.body() : errorResponse("Failed to load your parcels"));
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Parcel>>> call, Throwable t) {
                result.postValue(errorResponse("Network error: " + t.getMessage()));
            }
        });
        return result;
    }

    public MutableLiveData<ApiResponse<Parcel>> getParcel(String id) {
        MutableLiveData<ApiResponse<Parcel>> result = new MutableLiveData<>();
        api.getParcel(id).enqueue(new Callback<ApiResponse<Parcel>>() {
            @Override
            public void onResponse(Call<ApiResponse<Parcel>> call,
                                   Response<ApiResponse<Parcel>> response) {
                result.postValue(response.isSuccessful() ? response.body() : errorResponse("Parcel not found"));
            }

            @Override
            public void onFailure(Call<ApiResponse<Parcel>> call, Throwable t) {
                result.postValue(errorResponse("Network error: " + t.getMessage()));
            }
        });
        return result;
    }

    public MutableLiveData<ApiResponse<Parcel>> createParcel(
            String description, double weightKg, String sizeLabel,
            String pickupAddress, double pickupLat, double pickupLng,
            String destAddress, double destLat, double destLng,
            double fee, String paymentRef, String specialInstructions) {

        MutableLiveData<ApiResponse<Parcel>> result = new MutableLiveData<>();

        Map<String, Object> body = new HashMap<>();
        body.put("description", description);
        body.put("weight_kg", weightKg);
        body.put("size_label", sizeLabel);
        body.put("pickup_address", pickupAddress);
        body.put("pickup_lat", pickupLat);
        body.put("pickup_lng", pickupLng);
        body.put("destination_address", destAddress);
        body.put("destination_lat", destLat);
        body.put("destination_lng", destLng);
        body.put("fee", fee);
        body.put("carrier_earnings", fee * Constants.CARRIER_SHARE_PERCENT);
        body.put("platform_fee", fee * Constants.PLATFORM_FEE_PERCENT);
        body.put("payment_reference", paymentRef);
        body.put("special_instructions", specialInstructions);

        api.createParcel(body).enqueue(new Callback<ApiResponse<Parcel>>() {
            @Override
            public void onResponse(Call<ApiResponse<Parcel>> call,
                                   Response<ApiResponse<Parcel>> response) {
                result.postValue(response.isSuccessful() ? response.body() : errorResponse("Failed to create parcel"));
            }

            @Override
            public void onFailure(Call<ApiResponse<Parcel>> call, Throwable t) {
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
