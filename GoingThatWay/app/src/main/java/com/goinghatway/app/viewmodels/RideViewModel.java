package com.goinghatway.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.goinghatway.app.api.responses.ApiResponse;
import com.goinghatway.app.api.responses.PaginatedResponse;
import com.goinghatway.app.models.Ride;
import com.goinghatway.app.repositories.RideRepository;

import java.util.List;

public class RideViewModel extends AndroidViewModel {

    private final RideRepository repo;

    public RideViewModel(@NonNull Application app) {
        super(app);
        repo = new RideRepository(app);
    }

    public MutableLiveData<ApiResponse<PaginatedResponse<Ride>>> getRides(
            double lat, double lng, double radiusKm, String status) {
        return repo.getRides(lat, lng, radiusKm, status);
    }

    public MutableLiveData<ApiResponse<List<Ride>>> getMyRides() {
        return repo.getMyRides();
    }

    public MutableLiveData<ApiResponse<Ride>> getRide(String id) {
        return repo.getRide(id);
    }

    public MutableLiveData<ApiResponse<Ride>> createRide(
            String notes, int passengerCount, String luggageSize,
            String pickupAddress, double pickupLat, double pickupLng,
            String destAddress, double destLat, double destLng,
            double fare, String paymentRef) {
        return repo.createRide(notes, passengerCount, luggageSize,
                pickupAddress, pickupLat, pickupLng,
                destAddress, destLat, destLng,
                fare, paymentRef);
    }

    public MutableLiveData<ApiResponse<Ride>> createOnDemandRide(
            double pickupLat, double pickupLng,
            String destAddress, int passengerCount, String luggageSize,
            String paymentRef) {
        return repo.createOnDemandRide(pickupLat, pickupLng, destAddress,
                passengerCount, luggageSize, paymentRef);
    }
}
