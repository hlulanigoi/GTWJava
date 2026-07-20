package com.goinghatway.driver.utils;

public class DriverAvailabilityFormatter {
    public String getButtonLabel(boolean isOnline) {
        return isOnline ? "Go Offline" : "Go Online";
    }

    public String getStatusMessage(boolean isOnline) {
        return isOnline
                ? "You are online and ready to take new trip requests."
                : "You are offline. We will hold your requests until you return online.";
    }
}
