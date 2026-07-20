package com.goinghatway.app.models;

import com.google.gson.annotations.SerializedName;

public class Match {

    public static final String STATUS_PROPOSED  = "PROPOSED";
    public static final String STATUS_ACCEPTED  = "ACCEPTED";
    public static final String STATUS_REJECTED  = "REJECTED";
    public static final String STATUS_COMPLETED = "COMPLETED";

    @SerializedName("id")
    private String id;

    @SerializedName("parcel_id")
    private String parcelId;

    @SerializedName("parcel")
    private Parcel parcel;

    @SerializedName("trip_id")
    private String tripId;

    @SerializedName("trip")
    private Trip trip;

    @SerializedName("match_score")
    private double matchScore; // 0.0 - 1.0 confidence score

    @SerializedName("detour_distance_km")
    private double detourDistanceKm; // extra km if parcel is a detour

    @SerializedName("is_along_route")
    private boolean isAlongRoute; // true = no detour needed

    @SerializedName("status")
    private String status;

    @SerializedName("proposed_at")
    private String proposedAt;

    @SerializedName("responded_at")
    private String respondedAt;

    public Match() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getParcelId() { return parcelId; }
    public void setParcelId(String parcelId) { this.parcelId = parcelId; }

    public Parcel getParcel() { return parcel; }
    public void setParcel(Parcel parcel) { this.parcel = parcel; }

    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) { this.trip = trip; }

    public double getMatchScore() { return matchScore; }
    public void setMatchScore(double matchScore) { this.matchScore = matchScore; }

    public double getDetourDistanceKm() { return detourDistanceKm; }
    public void setDetourDistanceKm(double detourDistanceKm) { this.detourDistanceKm = detourDistanceKm; }

    public boolean isAlongRoute() { return isAlongRoute; }
    public void setAlongRoute(boolean alongRoute) { isAlongRoute = alongRoute; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getProposedAt() { return proposedAt; }
    public void setProposedAt(String proposedAt) { this.proposedAt = proposedAt; }

    public String getRespondedAt() { return respondedAt; }
    public void setRespondedAt(String respondedAt) { this.respondedAt = respondedAt; }
}
