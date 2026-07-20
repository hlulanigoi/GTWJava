package com.goinghatway.app.utils;

public class RideLifecycleStateMachine {

    public static final String STATUS_SEARCHING = "SEARCHING";
    public static final String STATUS_ASSIGNED = "ASSIGNED";
    public static final String STATUS_EN_ROUTE = "EN_ROUTE";
    public static final String STATUS_BOARDING = "BOARDING";
    public static final String STATUS_COMPLETED = "COMPLETED";

    public static String getDisplayStatus(String status) {
        switch (status) {
            case STATUS_ASSIGNED:
                return "Driver assigned";
            case STATUS_EN_ROUTE:
                return "Driver is on the way";
            case STATUS_BOARDING:
                return "Passenger boarding";
            case STATUS_COMPLETED:
                return "Trip completed";
            case STATUS_SEARCHING:
            default:
                return "Searching for driver…";
        }
    }

    public static String getNextStatus(String currentStatus, String action) {
        switch (currentStatus) {
            case STATUS_SEARCHING:
                return "accept".equals(action) ? STATUS_ASSIGNED : STATUS_SEARCHING;
            case STATUS_ASSIGNED:
                return "start".equals(action) ? STATUS_EN_ROUTE : STATUS_ASSIGNED;
            case STATUS_EN_ROUTE:
                return "board".equals(action) ? STATUS_BOARDING : STATUS_EN_ROUTE;
            case STATUS_BOARDING:
                return "complete".equals(action) ? STATUS_COMPLETED : STATUS_BOARDING;
            default:
                return currentStatus;
        }
    }
}
