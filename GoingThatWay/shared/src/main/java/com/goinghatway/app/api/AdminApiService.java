package com.goinghatway.app.api;

import com.goinghatway.app.api.responses.ApiResponse;
import com.goinghatway.app.api.responses.PaginatedResponse;
import com.goinghatway.app.models.AdminStats;
import com.goinghatway.app.models.Parcel;
import com.goinghatway.app.models.Ride;
import com.goinghatway.app.models.Ticket;
import com.goinghatway.app.models.Trip;
import com.goinghatway.app.models.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AdminApiService {

    // ─── Dashboard ────────────────────────────────────────────────────────────
    @GET("admin/stats")
    Call<ApiResponse<AdminStats>> getStats();

    // ─── Users ────────────────────────────────────────────────────────────────
    @GET("admin/users")
    Call<ApiResponse<PaginatedResponse<User>>> getUsers(
            @Query("page") int page,
            @Query("search") String search,
            @Query("role") String role
    );

    @PATCH("admin/users/{id}/activate")
    Call<ApiResponse<User>> activateUser(@Path("id") String id);

    @PATCH("admin/users/{id}/deactivate")
    Call<ApiResponse<User>> deactivateUser(@Path("id") String id);

    @PATCH("admin/users/{id}/role")
    Call<ApiResponse<User>> changeRole(@Path("id") String id, @Body Map<String, String> body);

    // ─── Driver approvals ─────────────────────────────────────────────────────
    @GET("admin/drivers/pending")
    Call<ApiResponse<List<User>>> getPendingDriverApplications();

    @PATCH("admin/drivers/{id}/approve")
    Call<ApiResponse<User>> approveDriver(@Path("id") String id);

    @PATCH("admin/drivers/{id}/reject")
    Call<ApiResponse<User>> rejectDriver(@Path("id") String id);

    // ─── Rides ────────────────────────────────────────────────────────────────
    @GET("admin/rides")
    Call<ApiResponse<PaginatedResponse<Ride>>> getRides(
            @Query("page") int page,
            @Query("status") String status
    );

    @PATCH("admin/rides/{id}/status")
    Call<ApiResponse<Ride>> updateRideStatus(
            @Path("id") String id,
            @Body Map<String, String> body
    );

    // ─── Parcels ─────────────────────────────────────────────────────────────
    @GET("admin/parcels")
    Call<ApiResponse<PaginatedResponse<Parcel>>> getParcels(
            @Query("page") int page,
            @Query("status") String status
    );

    @PATCH("admin/parcels/{id}/status")
    Call<ApiResponse<Parcel>> updateParcelStatus(
            @Path("id") String id,
            @Body Map<String, String> body
    );

    // ─── Trips ────────────────────────────────────────────────────────────────
    @GET("admin/trips")
    Call<ApiResponse<PaginatedResponse<Trip>>> getTrips(
            @Query("page") int page,
            @Query("status") String status
    );

    // ─── Tickets ──────────────────────────────────────────────────────────────
    @GET("admin/tickets")
    Call<ApiResponse<List<Ticket>>> getTickets(@Query("status") String status);

    @PATCH("admin/tickets/price")
    Call<ApiResponse<Map<String, Double>>> updateTicketPrice(@Body Map<String, Double> body);

    // ─── Payments ─────────────────────────────────────────────────────────────
    @GET("admin/payments/pending")
    Call<ApiResponse<List<Map<String, Object>>>> getPendingPayments();

    @PATCH("admin/payments/{ref}/verify")
    Call<ApiResponse<Map<String, Object>>> verifyPayment(@Path("ref") String ref);

    @PATCH("admin/payments/{ref}/reject")
    Call<ApiResponse<Map<String, Object>>> rejectPayment(
            @Path("ref") String ref,
            @Body Map<String, String> body
    );

    // ─── Reports ──────────────────────────────────────────────────────────────
    @GET("admin/reports/revenue")
    Call<ApiResponse<Map<String, Object>>> getRevenueReport(@Query("period") String period);
}
