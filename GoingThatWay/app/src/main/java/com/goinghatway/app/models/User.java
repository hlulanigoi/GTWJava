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

    @SerializedName("total_deliveries")
    private int totalDeliveries;

    @SerializedName("total_parcels_sent")
    private int totalParcelsSent;

    @SerializedName("tickets_owned")
    private int ticketsOwned;

    @SerializedName("is_verified")
    private boolean isVerified;

    @SerializedName("role")
    private String role; // USER | ADMIN

    @SerializedName("is_active")
    private boolean isActive;

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

    public int getTotalDeliveries() { return totalDeliveries; }
    public void setTotalDeliveries(int totalDeliveries) { this.totalDeliveries = totalDeliveries; }

    public int getTotalParcelsSent() { return totalParcelsSent; }
    public void setTotalParcelsSent(int totalParcelsSent) { this.totalParcelsSent = totalParcelsSent; }

    public int getTicketsOwned() { return ticketsOwned; }
    public void setTicketsOwned(int ticketsOwned) { this.ticketsOwned = ticketsOwned; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
