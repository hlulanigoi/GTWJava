package com.goinghatway.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Trip {

    public static final String STATUS_ACTIVE    = "ACTIVE";
    public static final String STATUS_ONGOING   = "ONGOING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    @SerializedName("id")
    private String id;

    @SerializedName("driver_id")
    private String driverId;

    @SerializedName("driver")
    private User driver;

    @SerializedName("origin_address")
    private String originAddress;

    @SerializedName("origin_lat")
    private double originLat;

    @SerializedName("origin_lng")
    private double originLng;

    @SerializedName("destination_address")
    private String destinationAddress;

    @SerializedName("destination_lat")
    private double destinationLat;

    @SerializedName("destination_lng")
    private double destinationLng;

    @SerializedName("departure_time")
    private String departureTime;

    @SerializedName("arrival_time")
    private String arrivalTime;

    @SerializedName("transport_mode")
    private String transportMode; // CAR, BUS, TRAIN, WALK, OTHER

    @SerializedName("seats_available")
    private int seatsAvailable;

    @SerializedName("available_capacity_kg")
    private double availableCapacityKg;

    @SerializedName("waypoints")
    private List<RoutePoint> waypoints;

    @SerializedName("matched_rides")
    private List<Ride> matchedRides;

    @SerializedName("matched_ride_count")
    private int matchedRideCount;

    @SerializedName("matched_parcel_count")
    private int matchedParcelCount;

    @SerializedName("status")
    private String status;

    @SerializedName("notes")
    private String notes;

    @SerializedName("created_at")
    private String createdAt;

    public Trip() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public User getDriver() { return driver; }
    public void setDriver(User driver) { this.driver = driver; }

    public String getOriginAddress() { return originAddress; }
    public void setOriginAddress(String originAddress) { this.originAddress = originAddress; }

    public double getOriginLat() { return originLat; }
    public void setOriginLat(double originLat) { this.originLat = originLat; }

    public double getOriginLng() { return originLng; }
    public void setOriginLng(double originLng) { this.originLng = originLng; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }

    public double getDestinationLat() { return destinationLat; }
    public void setDestinationLat(double destinationLat) { this.destinationLat = destinationLat; }

    public double getDestinationLng() { return destinationLng; }
    public void setDestinationLng(double destinationLng) { this.destinationLng = destinationLng; }

    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }

    public String getTransportMode() { return transportMode; }
    public void setTransportMode(String transportMode) { this.transportMode = transportMode; }

    public int getSeatsAvailable() { return seatsAvailable; }
    public void setSeatsAvailable(int seatsAvailable) { this.seatsAvailable = seatsAvailable; }

    public double getAvailableCapacityKg() { return availableCapacityKg; }
    public void setAvailableCapacityKg(double availableCapacityKg) { this.availableCapacityKg = availableCapacityKg; }

    public List<RoutePoint> getWaypoints() { return waypoints; }
    public void setWaypoints(List<RoutePoint> waypoints) { this.waypoints = waypoints; }

    public List<Ride> getMatchedRides() { return matchedRides; }
    public void setMatchedRides(List<Ride> matchedRides) { this.matchedRides = matchedRides; }

    public int getMatchedRideCount() { return matchedRideCount; }
    public void setMatchedRideCount(int matchedRideCount) { this.matchedRideCount = matchedRideCount; }

    public int getMatchedParcelCount() { return matchedParcelCount; }
    public void setMatchedParcelCount(int matchedParcelCount) { this.matchedParcelCount = matchedParcelCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
