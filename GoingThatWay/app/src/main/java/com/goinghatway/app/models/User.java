package com.goinghatway.app.models;

import com.google.gson.annotations.SerializedName;

public class User {

    public static final String ROLE_USER  = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    @SerializedName("id")
    private String id;

    @SerializedName("full_name")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("profile_picture")
    private String profilePicture;

    @SerializedName("rating")
    private double rating;

    @SerializedName("total_rides_driven")
    private int totalRidesDriven;

    @SerializedName("total_rides_taken")
    private int totalRidesTaken;

    @SerializedName("tickets_owned")
    private int ticketsOwned;

    @SerializedName("is_verified")
    private boolean isVerified;

    @SerializedName("role")
    private String role; // USER | ADMIN

    @SerializedName("is_active")
    private boolean isActive;

    @SerializedName("is_approved_driver")
    private boolean isApprovedDriver;

    @SerializedName("license_number")
    private String licenseNumber;

    @SerializedName("vehicle_plate")
    private String vehiclePlate;

    @SerializedName("vehicle_model")
    private String vehicleModel;

    @SerializedName("created_at")
    private String createdAt;

    public User() {}

    public boolean isAdmin() { return ROLE_ADMIN.equals(role); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getTotalRidesDriven() { return totalRidesDriven; }
    public void setTotalRidesDriven(int totalRidesDriven) { this.totalRidesDriven = totalRidesDriven; }

    public int getTotalRidesTaken() { return totalRidesTaken; }
    public void setTotalRidesTaken(int totalRidesTaken) { this.totalRidesTaken = totalRidesTaken; }

    public int getTicketsOwned() { return ticketsOwned; }
    public void setTicketsOwned(int ticketsOwned) { this.ticketsOwned = ticketsOwned; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isApprovedDriver() { return isApprovedDriver; }
    public void setApprovedDriver(boolean approvedDriver) { isApprovedDriver = approvedDriver; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getVehiclePlate() { return vehiclePlate; }
    public void setVehiclePlate(String vehiclePlate) { this.vehiclePlate = vehiclePlate; }

    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
