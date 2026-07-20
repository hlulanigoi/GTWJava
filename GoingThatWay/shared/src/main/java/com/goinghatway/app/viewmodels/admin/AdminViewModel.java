package com.goinghatway.app.viewmodels.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.goinghatway.app.api.responses.ApiResponse;
import com.goinghatway.app.api.responses.PaginatedResponse;
import com.goinghatway.app.models.AdminStats;
import com.goinghatway.app.models.Parcel;
import com.goinghatway.app.models.Ride;
import com.goinghatway.app.models.User;
import com.goinghatway.app.repositories.admin.AdminRepository;

import java.util.List;
import java.util.Map;

public class AdminViewModel extends ViewModel {

    private final AdminRepository repo = AdminRepository.getInstance();

    private final MutableLiveData<Boolean>  isLoading    = new MutableLiveData<>(false);
    private final MutableLiveData<String>   errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String>   toastMessage = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoading()    { return isLoading; }
    public LiveData<String>  getErrorMessage() { return errorMessage; }
    public LiveData<String>  getToastMessage() { return toastMessage; }

    // ─── Stats ────────────────────────────────────────────────────────────────
    public LiveData<ApiResponse<AdminStats>> loadStats() {
        isLoading.setValue(true);
        LiveData<ApiResponse<AdminStats>> ld = repo.getStats();
        ld.observeForever(r -> isLoading.postValue(false));
        return ld;
    }

    // ─── Users ────────────────────────────────────────────────────────────────
    public LiveData<ApiResponse<PaginatedResponse<User>>> loadUsers(int page, String search, String role) {
        isLoading.setValue(true);
        LiveData<ApiResponse<PaginatedResponse<User>>> ld = repo.getUsers(page, search, role);
        ld.observeForever(r -> isLoading.postValue(false));
        return ld;
    }

    public LiveData<ApiResponse<User>> setUserActive(String userId, boolean active) {
        return repo.setUserActive(userId, active);
    }

    // ─── Driver approvals ─────────────────────────────────────────────────────
    public LiveData<ApiResponse<List<User>>> loadPendingDriverApplications() {
        isLoading.setValue(true);
        LiveData<ApiResponse<List<User>>> ld = repo.getPendingDriverApplications();
        ld.observeForever(r -> isLoading.postValue(false));
        return ld;
    }

    public LiveData<ApiResponse<User>> approveDriver(String userId) {
        return repo.approveDriver(userId);
    }

    public LiveData<ApiResponse<User>> rejectDriver(String userId) {
        return repo.rejectDriver(userId);
    }

    // ─── Rides ────────────────────────────────────────────────────────────────
    public LiveData<ApiResponse<PaginatedResponse<Ride>>> loadRides(int page, String status) {
        isLoading.setValue(true);
        LiveData<ApiResponse<PaginatedResponse<Ride>>> ld = repo.getRides(page, status);
        ld.observeForever(r -> isLoading.postValue(false));
        return ld;
    }

    public LiveData<ApiResponse<Ride>> updateRideStatus(String id, String status) {
        return repo.updateRideStatus(id, status);
    }

    // ─── Parcels ─────────────────────────────────────────────────────────────
    public LiveData<ApiResponse<PaginatedResponse<Parcel>>> loadParcels(int page, String status) {
        isLoading.setValue(true);
        LiveData<ApiResponse<PaginatedResponse<Parcel>>> ld = repo.getParcels(page, status);
        ld.observeForever(r -> isLoading.postValue(false));
        return ld;
    }

    public LiveData<ApiResponse<Parcel>> updateParcelStatus(String id, String status) {
        return repo.updateParcelStatus(id, status);
    }

    // ─── Payments ─────────────────────────────────────────────────────────────
    public LiveData<ApiResponse<List<Map<String, Object>>>> loadPendingPayments() {
        isLoading.setValue(true);
        LiveData<ApiResponse<List<Map<String, Object>>>> ld = repo.getPendingPayments();
        ld.observeForever(r -> isLoading.postValue(false));
        return ld;
    }

    public LiveData<ApiResponse<Map<String, Object>>> verifyPayment(String ref) {
        return repo.verifyPayment(ref);
    }

    public LiveData<ApiResponse<Map<String, Object>>> rejectPayment(String ref, String reason) {
        return repo.rejectPayment(ref, reason);
    }
}
