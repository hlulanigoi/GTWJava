package com.goinghatway.app.api;

import com.goinghatway.app.api.responses.ApiResponse;
import com.goinghatway.app.api.responses.AuthResponse;
import com.goinghatway.app.api.responses.PaginatedResponse;
import com.goinghatway.app.models.Booking;
import com.goinghatway.app.models.Match;
import com.goinghatway.app.models.Parcel;
import com.goinghatway.app.models.Ride;
import com.goinghatway.app.models.Ticket;
import com.goinghatway.app.models.Trip;
import com.goinghatway.app.models.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ─── Auth ───────────────────────────────────────────────────────────────

    @POST("auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body Map<String, String> body);

    @POST("auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body Map<String, String> body);

    @POST("auth/logout")
    Call<ApiResponse<Void>> logout();

    @GET("auth/me")
    Call<ApiResponse<User>> getMe();

    // ─── Rides ─────────────────────────────────────────────────────────────

    /** List available ride requests near a location */
    @GET("rides")
    Call<ApiResponse<PaginatedResponse<Ride>>> getRides(
            @Query("lat") double lat,
            @Query("lng") double lng,
            @Query("radius_km") double radiusKm,
            @Query("status") String status
    );

    /** My rides (as rider) */
    @GET("rides/my")
    Call<ApiResponse<List<Ride>>> getMyRides();

    @GET("rides/{id}")
    Call<ApiResponse<Ride>> getRide(@Path("id") String id);

    /** Create a scheduled ride request */
    @POST("rides")
    Call<ApiResponse<Ride>> createRide(@Body Map<String, Object> body);

    /** Create an on-demand ride (GPS pickup) */
    @POST("rides/on-demand")
    Call<ApiResponse<Ride>> createOnDemandRide(@Body Map<String, Object> body);

    @PATCH("rides/{id}/status")
    Call<ApiResponse<Ride>> updateRideStatus(
            @Path("id") String id,
            @Body Map<String, String> body
    );

    @DELETE("rides/{id}")
    Call<ApiResponse<Void>> deleteRide(@Path("id") String id);

    // ─── Parcels ─────────────────────────────────────────────────────────────

    @GET("parcels")
    Call<ApiResponse<PaginatedResponse<Parcel>>> getParcels(
            @Query("page") int page,
            @Query("status") String status,
            @Query("lat") double lat,
            @Query("lng") double lng,
            @Query("radius_km") double radiusKm
    );

    @GET("parcels/my")
    Call<ApiResponse<List<Parcel>>> getMyParcels();

    @GET("parcels/{id}")
    Call<ApiResponse<Parcel>> getParcel(@Path("id") String id);

    @POST("parcels")
    Call<ApiResponse<Parcel>> createParcel(@Body Map<String, Object> body);

    @PATCH("parcels/{id}/status")
    Call<ApiResponse<Parcel>> updateParcelStatus(
            @Path("id") String id,
            @Body Map<String, String> body
    );

    // ─── Trips ────────────────────────────────────────────────────────────────

    @GET("trips")
    Call<ApiResponse<PaginatedResponse<Trip>>> getTrips(
            @Query("page") int page,
            @Query("status") String status
    );

    /** My trips (as driver) */
    @GET("trips/my")
    Call<ApiResponse<List<Trip>>> getMyTrips();

    @GET("trips/{id}")
    Call<ApiResponse<Trip>> getTrip(@Path("id") String id);

    @POST("trips")
    Call<ApiResponse<Trip>> createTrip(@Body Map<String, Object> body);

    @PATCH("trips/{id}/status")
    Call<ApiResponse<Trip>> updateTripStatus(
            @Path("id") String id,
            @Body Map<String, String> body
    );

    /** Auto-match rides to a trip based on route proximity */
    @POST("trips/{id}/match")
    Call<ApiResponse<List<Booking>>> matchRidesToTrip(@Path("id") String id);

    // ─── Bookings ─────────────────────────────────────────────────────────────

    @GET("bookings")
    Call<ApiResponse<List<Booking>>> getMyBookings();

    @POST("bookings/{id}/accept")
    Call<ApiResponse<Booking>> acceptBooking(@Path("id") String id);

    @POST("bookings/{id}/reject")
    Call<ApiResponse<Booking>> rejectBooking(@Path("id") String id);

    @POST("bookings/{id}/pickup")
    Call<ApiResponse<Booking>> markPickedUp(@Path("id") String id);

    @POST("bookings/{id}/complete")
    Call<ApiResponse<Booking>> markCompleted(@Path("id") String id);

    // ─── Matches (Parcels) ──────────────────────────────────────────────────

    @GET("matches/my")
    Call<ApiResponse<List<Match>>> getMyMatches();

    @POST("matches/{id}/accept")
    Call<ApiResponse<Match>> acceptMatch(@Path("id") String id);

    @POST("matches/{id}/reject")
    Call<ApiResponse<Match>> rejectMatch(@Path("id") String id);

    @POST("matches/{id}/collect")
    Call<ApiResponse<Match>> markCollected(@Path("id") String id);

    @POST("matches/{id}/deliver")
    Call<ApiResponse<Match>> markDelivered(@Path("id") String id);

    // ─── Tickets ──────────────────────────────────────────────────────────────

    @GET("tickets/my")
    Call<ApiResponse<List<Ticket>>> getMyTickets();

    @GET("tickets/price")
    Call<ApiResponse<Map<String, Double>>> getTicketPrice();

    @POST("tickets/purchase")
    Call<ApiResponse<Ticket>> purchaseTicket(@Body Map<String, String> body);

    // ─── Payments ─────────────────────────────────────────────────────────────

    /** Initiate a bank payment for a new ride; returns payment reference */
    @POST("payments/initiate")
    Call<ApiResponse<Map<String, String>>> initiateRidePayment(@Body Map<String, Object> body);

    @POST("payments/initiate-parcel")
    Call<ApiResponse<Map<String, String>>> initiateParcelPayment(@Body Map<String, Object> body);

    /** Verify payment after bank transfer */
    @POST("payments/verify")
    Call<ApiResponse<Map<String, Object>>> verifyPayment(@Body Map<String, String> body);

    // ─── Users ────────────────────────────────────────────────────────────────

    @GET("users/{id}")
    Call<ApiResponse<User>> getUser(@Path("id") String id);

    @PATCH("users/profile")
    Call<ApiResponse<User>> updateProfile(@Body Map<String, String> body);

    /** Apply to become an approved driver */
    @POST("users/me/apply-driver")
    Call<ApiResponse<User>> applyAsDriver(@Body Map<String, String> body);
}
