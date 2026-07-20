package com.goinghatway.app.repositories;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.goinghatway.app.api.ApiClient;
import com.goinghatway.app.api.ApiService;
import com.goinghatway.app.api.responses.ApiResponse;
import com.goinghatway.app.api.responses.PaginatedResponse;
import com.goinghatway.app.models.Booking;
import com.goinghatway.app.models.Trip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TripRepository {

    private final ApiService api;

    public TripRepository(Context context) {
        api = ApiClient.getService(context);
    }

    public MutableLiveData<ApiResponse<List<Trip>>> getMyTrips() {
        MutableLiveData<ApiResponse<List<Trip>>> result = new MutableLiveData<>();
        api.getMyTrips().enqueue(new Callback<ApiResponse<List<Trip>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Trip>>> call,
                                   Response<ApiResponse<List<Trip>>> response) {
                result.postValue(response.isSuccessful() ? response.body() : err("Failed to load trips"));
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Trip>>> call, Throwable t) {
                result.postValue(err("Network error: " + t.getMessage()));
            }
        });
        return result;
    }

    /**
     * Create a new driver trip.
     *
     * @param corridorKm  Bolt-inspired: how far off-route (km) the driver will accept pickups.
     *                    Sent to the backend as {@code corridor_km}; backend uses this instead of
     *                    the global {@code ROUTE_BUFFER_KM} constant when present.
     * @param minFare     inDrive-inspired: minimum payout the driver will accept per parcel.
     *                    Sent as {@code min_fare}; backend skips matches below this amount.
     */
    public MutableLiveData<ApiResponse<Trip>> createTrip(
            String originAddress, double originLat, double originLng,
            String destAddress,   double destLat,   double destLng,
            String departureTime, String arrivalTime,
            String transportMode, int seatsAvailable, String notes,
            int corridorKm, double minFare) {

        MutableLiveData<ApiResponse<Trip>> result = new MutableLiveData<>();

        Map<String, Object> body = new HashMap<>();
        body.put("origin_address",      originAddress);
        body.put("origin_lat",          originLat);
        body.put("origin_lng",          originLng);
        body.put("destination_address", destAddress);
        body.put("destination_lat",     destLat);
        body.put("destination_lng",     destLng);
        body.put("departure_time",      departureTime);
        body.put("arrival_time",        arrivalTime);
        body.put("transport_mode",      transportMode);
        body.put("seats_available",     seatsAvailable);
        body.put("notes",               notes);
        // New preference fields
        body.put("corridor_km",         corridorKm);
        if (minFare > 0) body.put("min_fare", minFare);

        api.createTrip(body).enqueue(new Callback<ApiResponse<Trip>>() {
            @Override
            public void onResponse(Call<ApiResponse<Trip>> call,
                                   Response<ApiResponse<Trip>> response) {
                result.postValue(response.isSuccessful() ? response.body() : err("Failed to create trip"));
            }

            @Override
            public void onFailure(Call<ApiResponse<Trip>> call, Throwable t) {
                result.postValue(err("Network error: " + t.getMessage()));
            }
        });
        return result;
    }

    public MutableLiveData<ApiResponse<List<Booking>>> matchRidesToTrip(String tripId) {
        MutableLiveData<ApiResponse<List<Booking>>> result = new MutableLiveData<>();
        api.matchRidesToTrip(tripId).enqueue(new Callback<ApiResponse<List<Booking>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Booking>>> call,
                                   Response<ApiResponse<List<Booking>>> response) {
                result.postValue(response.isSuccessful() ? response.body() : err("Failed to match"));
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Booking>>> call, Throwable t) {
                result.postValue(err("Network error: " + t.getMessage()));
            }
        });
        return result;
    }

    public MutableLiveData<ApiResponse<Trip>> updateStatus(String tripId, String status) {
        MutableLiveData<ApiResponse<Trip>> result = new MutableLiveData<>();
        Map<String, String> body = new HashMap<>();
        body.put("status", status);
        api.updateTripStatus(tripId, body).enqueue(new Callback<ApiResponse<Trip>>() {
            @Override
            public void onResponse(Call<ApiResponse<Trip>> call,
                                   Response<ApiResponse<Trip>> response) {
                result.postValue(response.isSuccessful() ? response.body() : err("Failed to update"));
            }

            @Override
            public void onFailure(Call<ApiResponse<Trip>> call, Throwable t) {
                result.postValue(err("Network error: " + t.getMessage()));
            }
        });
        return result;
    }

    private <T> ApiResponse<T> err(String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setError(message);
        return r;
    }
}
