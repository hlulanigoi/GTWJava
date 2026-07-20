package com.goinghatway.app.models;

import com.google.gson.annotations.SerializedName;

public class Booking {

    public static final String STATUS_PROPOSED  = "PROPOSED";
    public static final String STATUS_ACCEPTED  = "ACCEPTED";
    public static final String STATUS_REJECTED  = "REJECTED";
    public static final String STATUS_PICKED_UP = "PICKED_UP";
    public static final String STATUS_COMPLETED = "COMPLETED";

    @SerializedName("id")
    private String id;

    @SerializedName("trip_id")
    private String tripId;

    @SerializedName("trip")
    private Trip trip;

    @SerializedName("ride_id")
    private String rideId;

    @SerializedName("ride")
    private Ride ride;

    @SerializedName("driver_id")
    private String driverId;

    @SerializedName("rider_id")
    private String riderId;

    @SerializedName("score")
    private double score;

    @SerializedName("along_route")
    private boolean alongRoute;

    @SerializedName("detour_km")
    private double detourKm;

    @SerializedName("status")
    private String status;

    @SerializedName("driver_earning")
    private double driverEarning;

    @SerializedName("accepted_at")
    private String acceptedAt;

    @SerializedName("picked_up_at")
    private String pickedUpAt;

    @SerializedName("completed_at")
    private String completedAt;

    public Booking() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) { this.trip = trip; }

    public String getRideId() { return rideId; }
    public void setRideId(String rideId) { this.rideId = rideId; }

    public Ride getRide() { return ride; }
    public void setRide(Ride ride) { this.ride = ride; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public String getRiderId() { return riderId; }
    public void setRiderId(String riderId) { this.riderId = riderId; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public boolean isAlongRoute() { return alongRoute; }
    public void setAlongRoute(boolean alongRoute) { this.alongRoute = alongRoute; }

    public double getDetourKm() { return detourKm; }
    public void setDetourKm(double detourKm) { this.detourKm = detourKm; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getDriverEarning() { return driverEarning; }
    public void setDriverEarning(double driverEarning) { this.driverEarning = driverEarning; }

    public String getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(String acceptedAt) { this.acceptedAt = acceptedAt; }

    public String getPickedUpAt() { return pickedUpAt; }
    public void setPickedUpAt(String pickedUpAt) { this.pickedUpAt = pickedUpAt; }

    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }
}
