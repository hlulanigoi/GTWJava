package com.goinghatway.app.models;

import com.google.gson.annotations.SerializedName;

public class Ride {

    public static final String STATUS_PENDING   = "PENDING";
    public static final String STATUS_MATCHED   = "MATCHED";
    public static final String STATUS_EN_ROUTE  = "EN_ROUTE";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    public static final String TYPE_SCHEDULED  = "SCHEDULED";
    public static final String TYPE_ON_DEMAND  = "ON_DEMAND";

    public static final String LUGGAGE_NONE   = "NONE";
    public static final String LUGGAGE_SMALL  = "SMALL";
    public static final String LUGGAGE_MEDIUM = "MEDIUM";
    public static final String LUGGAGE_LARGE  = "LARGE";

    @SerializedName("id")
    private String id;

    @SerializedName("rider_id")
    private String riderId;

    @SerializedName("rider")
    private User rider;

    @SerializedName("driver_id")
    private String driverId;

    @SerializedName("driver")
    private User driver;

    @SerializedName("ride_type")
    private String rideType; // SCHEDULED | ON_DEMAND

    @SerializedName("passenger_count")
    private int passengerCount;

    @SerializedName("luggage_size")
    private String luggageSize; // NONE, SMALL, MEDIUM, LARGE

    @SerializedName("notes")
    private String notes;

    @SerializedName("pickup_address")
    private String pickupAddress;

    @SerializedName("pickup_lat")
    private double pickupLat;

    @SerializedName("pickup_lng")
    private double pickupLng;

    @SerializedName("destination_address")
    private String destinationAddress;

    @SerializedName("destination_lat")
    private double destinationLat;

    @SerializedName("destination_lng")
    private double destinationLng;

    @SerializedName("fare")
    private double fare;

    @SerializedName("platform_fee")
    private double platformFee;

    @SerializedName("driver_earning")
    private double driverEarning;

    @SerializedName("payment_reference")
    private String paymentReference;

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    public Ride() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRiderId() { return riderId; }
    public void setRiderId(String riderId) { this.riderId = riderId; }

    public User getRider() { return rider; }
    public void setRider(User rider) { this.rider = rider; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public User getDriver() { return driver; }
    public void setDriver(User driver) { this.driver = driver; }

    public String getRideType() { return rideType; }
    public void setRideType(String rideType) { this.rideType = rideType; }

    public int getPassengerCount() { return passengerCount; }
    public void setPassengerCount(int passengerCount) { this.passengerCount = passengerCount; }

    public String getLuggageSize() { return luggageSize; }
    public void setLuggageSize(String luggageSize) { this.luggageSize = luggageSize; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public double getPickupLat() { return pickupLat; }
    public void setPickupLat(double pickupLat) { this.pickupLat = pickupLat; }

    public double getPickupLng() { return pickupLng; }
    public void setPickupLng(double pickupLng) { this.pickupLng = pickupLng; }

    public String getDestinationAddress() { return destinationAddress; }
    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }

    public double getDestinationLat() { return destinationLat; }
    public void setDestinationLat(double destinationLat) { this.destinationLat = destinationLat; }

    public double getDestinationLng() { return destinationLng; }
    public void setDestinationLng(double destinationLng) { this.destinationLng = destinationLng; }

    public double getFare() { return fare; }
    public void setFare(double fare) { this.fare = fare; }

    public double getPlatformFee() { return platformFee; }
    public void setPlatformFee(double platformFee) { this.platformFee = platformFee; }

    public double getDriverEarning() { return driverEarning; }
    public void setDriverEarning(double driverEarning) { this.driverEarning = driverEarning; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
