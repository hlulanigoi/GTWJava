package com.goinghatway.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.goinghatway.app.api.responses.ApiResponse;
import com.goinghatway.app.models.Booking;
import com.goinghatway.app.models.Trip;
import com.goinghatway.app.repositories.TripRepository;

import java.util.List;

public class TripViewModel extends AndroidViewModel {

    private final TripRepository repo;

    public TripViewModel(@NonNull Application app) {
        super(app);
        repo = new TripRepository(app);
    }

    public MutableLiveData<ApiResponse<List<Trip>>> getMyTrips() {
        return repo.getMyTrips();
    }

    public MutableLiveData<ApiResponse<Trip>> createTrip(
            String originAddress, double originLat, double originLng,
            String destAddress, double destLat, double destLng,
            String departureTime, String arrivalTime,
            String transportMode, int seatsAvailable, String notes) {
        return repo.createTrip(originAddress, originLat, originLng,
                destAddress, destLat, destLng,
                departureTime, arrivalTime, transportMode, seatsAvailable, notes);
    }

    public MutableLiveData<ApiResponse<List<Booking>>> matchRidesToTrip(String tripId) {
        return repo.matchRidesToTrip(tripId);
    }

    public MutableLiveData<ApiResponse<Trip>> updateStatus(String tripId, String status) {
        return repo.updateStatus(tripId, status);
    }
}
