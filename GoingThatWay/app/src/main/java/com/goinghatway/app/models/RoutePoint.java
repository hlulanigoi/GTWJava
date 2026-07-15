package com.goinghatway.app.models;

import com.google.gson.annotations.SerializedName;

public class RoutePoint {

    @SerializedName("lat")
    private double lat;

    @SerializedName("lng")
    private double lng;

    @SerializedName("address")
    private String address;

    @SerializedName("order")
    private int order;

    public RoutePoint() {}

    public RoutePoint(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public RoutePoint(double lat, double lng, String address) {
        this.lat = lat;
        this.lng = lng;
        this.address = address;
    }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}
