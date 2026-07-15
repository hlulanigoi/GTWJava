package com.goinghatway.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AdminStats {

    @SerializedName("total_users")
    private int totalUsers;

    @SerializedName("total_parcels")
    private int totalParcels;

    @SerializedName("total_trips")
    private int totalTrips;

    @SerializedName("total_tickets_sold")
    private int totalTicketsSold;

    @SerializedName("total_revenue")
    private double totalRevenue;

    @SerializedName("platform_earnings")
    private double platformEarnings;

    @SerializedName("pending_parcels")
    private int pendingParcels;

    @SerializedName("active_trips")
    private int activeTrips;

    @SerializedName("pending_payments")
    private int pendingPayments;

    @SerializedName("delivered_today")
    private int deliveredToday;

    @SerializedName("revenue_this_week")
    private List<Double> revenueThisWeek;

    @SerializedName("week_labels")
    private List<String> weekLabels;

    public AdminStats() {}

    public int getTotalUsers() { return totalUsers; }
    public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }

    public int getTotalParcels() { return totalParcels; }
    public void setTotalParcels(int totalParcels) { this.totalParcels = totalParcels; }

    public int getTotalTrips() { return totalTrips; }
    public void setTotalTrips(int totalTrips) { this.totalTrips = totalTrips; }

    public int getTotalTicketsSold() { return totalTicketsSold; }
    public void setTotalTicketsSold(int totalTicketsSold) { this.totalTicketsSold = totalTicketsSold; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public double getPlatformEarnings() { return platformEarnings; }
    public void setPlatformEarnings(double platformEarnings) { this.platformEarnings = platformEarnings; }

    public int getPendingParcels() { return pendingParcels; }
    public void setPendingParcels(int pendingParcels) { this.pendingParcels = pendingParcels; }

    public int getActiveTrips() { return activeTrips; }
    public void setActiveTrips(int activeTrips) { this.activeTrips = activeTrips; }

    public int getPendingPayments() { return pendingPayments; }
    public void setPendingPayments(int pendingPayments) { this.pendingPayments = pendingPayments; }

    public int getDeliveredToday() { return deliveredToday; }
    public void setDeliveredToday(int deliveredToday) { this.deliveredToday = deliveredToday; }

    public List<Double> getRevenueThisWeek() { return revenueThisWeek; }
    public void setRevenueThisWeek(List<Double> revenueThisWeek) { this.revenueThisWeek = revenueThisWeek; }

    public List<String> getWeekLabels() { return weekLabels; }
    public void setWeekLabels(List<String> weekLabels) { this.weekLabels = weekLabels; }
}
