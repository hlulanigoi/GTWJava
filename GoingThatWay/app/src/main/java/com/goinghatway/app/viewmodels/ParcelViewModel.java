package com.goinghatway.app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.goinghatway.app.api.responses.ApiResponse;
import com.goinghatway.app.api.responses.PaginatedResponse;
import com.goinghatway.app.models.Parcel;
import com.goinghatway.app.repositories.ParcelRepository;

import java.util.List;

public class ParcelViewModel extends AndroidViewModel {

    private final ParcelRepository repo;

    public ParcelViewModel(@NonNull Application app) {
        super(app);
        repo = new ParcelRepository(app);
    }

    public MutableLiveData<ApiResponse<PaginatedResponse<Parcel>>> getParcels(
            int page, String status, double lat, double lng, double radiusKm) {
        return repo.getParcels(page, status, lat, lng, radiusKm);
    }

    public MutableLiveData<ApiResponse<List<Parcel>>> getMyParcels() {
        return repo.getMyParcels();
    }

    public MutableLiveData<ApiResponse<Parcel>> getParcel(String id) {
        return repo.getParcel(id);
    }

    public MutableLiveData<ApiResponse<Parcel>> createParcel(
            String description, double weightKg, String sizeLabel,
            String pickupAddress, double pickupLat, double pickupLng,
            String destAddress, double destLat, double destLng,
            double fee, String paymentRef, String specialInstructions) {
        return repo.createParcel(description, weightKg, sizeLabel,
                pickupAddress, pickupLat, pickupLng,
                destAddress, destLat, destLng,
                fee, paymentRef, specialInstructions);
    }
}
