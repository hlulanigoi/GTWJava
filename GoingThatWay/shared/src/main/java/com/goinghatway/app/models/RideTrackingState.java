package com.goinghatway.app.models;

import com.google.gson.annotations.SerializedName;

public class RideTrackingState {
    @SerializedName("ride_id")
    private String rideId;

    @SerializedName("status")
    private String status;

    @SerializedName("driver_lat")
    private double driverLat;

    @SerializedName("driver_lng")
    private double driverLng;

    @SerializedName("eta_minutes")
    private int etaMinutes;

    public RideTrackingState() {}

    public String getRideId() { return rideId; }
    public void setRideId(String rideId) { this.rideId = rideId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getDriverLat() { return driverLat; }
    public void setDriverLat(double driverLat) { this.driverLat = driverLat; }

    public double getDriverLng() { return driverLng; }
    public void setDriverLng(double driverLng) { this.driverLng = driverLng; }

    public int getEtaMinutes() { return etaMinutes; }
    public void setEtaMinutes(int etaMinutes) { this.etaMinutes = etaMinutes; }

    public static RideTrackingState demoState() {
        RideTrackingState s = new RideTrackingState();
        s.rideId = "demo_123";
        s.status = "ongoing";
        s.driverLat = -26.1076;
        s.driverLng = 28.0567;
        s.etaMinutes = 8;
        return s;
    }
}
