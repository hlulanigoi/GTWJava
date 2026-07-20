package com.goinghatway.app.repositories.admin;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.goinghatway.app.api.ApiClient;
import com.goinghatway.app.api.AdminApiService;
import com.goinghatway.app.api.responses.ApiResponse;
import com.goinghatway.app.api.responses.PaginatedResponse;
import com.goinghatway.app.models.AdminStats;
import com.goinghatway.app.models.Ride;
import com.goinghatway.app.models.Ticket;
import com.goinghatway.app.models.Trip;
import com.goinghatway.app.models.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRepository {

    private static AdminRepository instance;
    private final AdminApiService api;

    private AdminRepository() {
        api = ApiClient.getInstance().create(AdminApiService.class);
    }

    public static synchronized AdminRepository getInstance() {
        if (instance == null) instance = new AdminRepository();
        return instance;
    }

    // ─── Stats ────────────────────────────────────────────────────────────────
    public LiveData<ApiResponse<AdminStats>> getStats() {
        MutableLiveData<ApiResponse<AdminStats>> result = new MutableLiveData<>();
        api.getStats().enqueue(new Callback<ApiResponse<AdminStats>>() {
            @Override public void onResponse(Call<ApiResponse<AdminStats>> c, Response<ApiResponse<AdminStats>> r) {
                result.setValue(r.isSuccessful() && r.body() != null ? r.body() : error("Failed to load stats"));
            }
            @Override public void onFailure(Call<ApiResponse<AdminStats>> c, Throwable t) {
                result.setValue(error(t.getMessage()));
            }
        });
        return result;
    }

    // ─── Users ────────────────────────────────────────────────────────────────
    public LiveData<ApiResponse<PaginatedResponse<User>>> getUsers(int page, String search, String role) {
        MutableLiveData<ApiResponse<PaginatedResponse<User>>> result = new MutableLiveData<>();
        api.getUsers(page, search, role).enqueue(new Callback<ApiResponse<PaginatedResponse<User>>>() {
            @Override public void onResponse(Call<ApiResponse<PaginatedResponse<User>>> c,
                                              Response<ApiResponse<PaginatedResponse<User>>> r) {
                result.setValue(r.isSuccessful() && r.body() != null ? r.body() : error("Failed to load users"));
            }
            @Override public void onFailure(Call<ApiResponse<PaginatedResponse<User>>> c, Throwable t) {
                result.setValue(error(t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<ApiResponse<User>> setUserActive(String userId, boolean active) {
        MutableLiveData<ApiResponse<User>> result = new MutableLiveData<>();
        Call<ApiResponse<User>> call = active ? api.activateUser(userId) : api.deactivateUser(userId);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override public void onResponse(Call<ApiResponse<User>> c, Response<ApiResponse<User>> r) {
                result.setValue(r.isSuccessful() && r.body() != null ? r.body() : error("Failed"));
            }
            @Override public void onFailure(Call<ApiResponse<User>> c, Throwable t) {
                result.setValue(error(t.getMessage()));
            }
        });
        return result;
    }

    // ─── Driver approvals ─────────────────────────────────────────────────────
    public LiveData<ApiResponse<List<User>>> getPendingDriverApplications() {
        MutableLiveData<ApiResponse<List<User>>> result = new MutableLiveData<>();
        api.getPendingDriverApplications().enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override public void onResponse(Call<ApiResponse<List<User>>> c, Response<ApiResponse<List<User>>> r) {
                result.setValue(r.isSuccessful() && r.body() != null ? r.body() : error("Failed to load applications"));
            }
            @Override public void onFailure(Call<ApiResponse<List<User>>> c, Throwable t) {
                result.setValue(error(t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<ApiResponse<User>> approveDriver(String userId) {
        MutableLiveData<ApiResponse<User>> result = new MutableLiveData<>();
        api.approveDriver(userId).enqueue(new Callback<ApiResponse<User>>() {
            @Override public void onResponse(Call<ApiResponse<User>> c, Response<ApiResponse<User>> r) {
                result.setValue(r.isSuccessful() && r.body() != null ? r.body() : error("Failed to approve"));
            }
            @Override public void onFailure(Call<ApiResponse<User>> c, Throwable t) {
                result.setValue(error(t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<ApiResponse<User>> rejectDriver(String userId) {
        MutableLiveData<ApiResponse<User>> result = new MutableLiveData<>();
        api.rejectDriver(userId).enqueue(new Callback<ApiResponse<User>>() {
            @Override public void onResponse(Call<ApiResponse<User>> c, Response<ApiResponse<User>> r) {
                result.setValue(r.isSuccessful() && r.body() != null ? r.body() : error("Failed to reject"));
            }
            @Override public void onFailure(Call<ApiResponse<User>> c, Throwable t) {
                result.setValue(error(t.getMessage()));
            }
        });
        return result;
    }

    // ─── Rides ────────────────────────────────────────────────────────────────
    public LiveData<ApiResponse<PaginatedResponse<Ride>>> getRides(int page, String status) {
        MutableLiveData<ApiResponse<PaginatedResponse<Ride>>> result = new MutableLiveData<>();
        api.getRides(page, status).enqueue(new Callback<ApiResponse<PaginatedResponse<Ride>>>() {
            @Override public void onResponse(Call<ApiResponse<PaginatedResponse<Ride>>> c,
                                              Response<ApiResponse<PaginatedResponse<Ride>>> r) {
                result.setValue(r.isSuccessful() && r.body() != null ? r.body() : error("Failed to load rides"));
            }
            @Override public void onFailure(Call<ApiResponse<PaginatedResponse<Ride>>> c, Throwable t) {
                result.setValue(error(t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<ApiResponse<Ride>> updateRideStatus(String id, String status) {
        MutableLiveData<ApiResponse<Ride>> result = new MutableLiveData<>();
        Map<String, String> body = new HashMap<>();
        body.put("status", status);
        api.updateRideStatus(id, body).enqueue(new Callback<ApiResponse<Ride>>() {
            @Override public void onResponse(Call<ApiResponse<Ride>> c, Response<ApiResponse<Ride>> r) {
                result.setValue(r.isSuccessful() && r.body() != null ? r.body() : error("Failed"));
            }
            @Override public void onFailure(Call<ApiResponse<Ride>> c, Throwable t) {
                result.setValue(error(t.getMessage()));
            }
        });
        return result;
    }

    // ─── Payments ─────────────────────────────────────────────────────────────
    public LiveData<ApiResponse<List<Map<String, Object>>>> getPendingPayments() {
        MutableLiveData<ApiResponse<List<Map<String, Object>>>> result = new MutableLiveData<>();
        api.getPendingPayments().enqueue(new Callback<ApiResponse<List<Map<String, Object>>>>() {
            @Override public void onResponse(Call<ApiResponse<List<Map<String, Object>>>> c,
                                              Response<ApiResponse<List<Map<String, Object>>>> r) {
                result.setValue(r.isSuccessful() && r.body() != null ? r.body() : error("Failed"));
            }
            @Override public void onFailure(Call<ApiResponse<List<Map<String, Object>>>> c, Throwable t) {
                result.setValue(error(t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<ApiResponse<Map<String, Object>>> verifyPayment(String ref) {
        MutableLiveData<ApiResponse<Map<String, Object>>> result = new MutableLiveData<>();
        api.verifyPayment(ref).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override public void onResponse(Call<ApiResponse<Map<String, Object>>> c,
                                              Response<ApiResponse<Map<String, Object>>> r) {
                result.setValue(r.isSuccessful() && r.body() != null ? r.body() : error("Failed"));
            }
            @Override public void onFailure(Call<ApiResponse<Map<String, Object>>> c, Throwable t) {
                result.setValue(error(t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<ApiResponse<Map<String, Object>>> rejectPayment(String ref, String reason) {
        MutableLiveData<ApiResponse<Map<String, Object>>> result = new MutableLiveData<>();
        Map<String, String> body = new HashMap<>();
        body.put("reason", reason);
        api.rejectPayment(ref, body).enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
            @Override public void onResponse(Call<ApiResponse<Map<String, Object>>> c,
                                              Response<ApiResponse<Map<String, Object>>> r) {
                result.setValue(r.isSuccessful() && r.body() != null ? r.body() : error("Failed"));
            }
            @Override public void onFailure(Call<ApiResponse<Map<String, Object>>> c, Throwable t) {
                result.setValue(error(t.getMessage()));
            }
        });
        return result;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────
    private <T> ApiResponse<T> error(String msg) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setError(msg != null ? msg : "Unknown error");
        return r;
    }
}
