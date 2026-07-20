package com.goinghatway.driver;

import static org.junit.Assert.assertEquals;

import com.goinghatway.driver.utils.DriverAvailabilityFormatter;

import org.junit.Test;

public class DriverAvailabilityFormatterTest {
    @Test
    public void formatsOnlineStateForDriverDashboard() {
        DriverAvailabilityFormatter formatter = new DriverAvailabilityFormatter();

        assertEquals("Go Offline", formatter.getButtonLabel(true));
        assertEquals("You are online and ready to take new trip requests.", formatter.getStatusMessage(true));
    }

    @Test
    public void formatsOfflineStateForDriverDashboard() {
        DriverAvailabilityFormatter formatter = new DriverAvailabilityFormatter();

        assertEquals("Go Online", formatter.getButtonLabel(false));
        assertEquals("You are offline. We will hold your requests until you return online.", formatter.getStatusMessage(false));
    }
}
