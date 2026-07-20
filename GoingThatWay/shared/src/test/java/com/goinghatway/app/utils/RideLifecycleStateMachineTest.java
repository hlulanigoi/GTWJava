package com.goinghatway.app.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RideLifecycleStateMachineTest {

    @Test
    public void displayStatusUsesFriendlyLabels() {
        assertEquals("Searching for driver…", RideLifecycleStateMachine.getDisplayStatus(RideLifecycleStateMachine.STATUS_SEARCHING));
        assertEquals("Driver assigned", RideLifecycleStateMachine.getDisplayStatus(RideLifecycleStateMachine.STATUS_ASSIGNED));
        assertEquals("Driver is on the way", RideLifecycleStateMachine.getDisplayStatus(RideLifecycleStateMachine.STATUS_EN_ROUTE));
    }

    @Test
    public void nextStatusTransitionsForTripActions() {
        assertEquals(RideLifecycleStateMachine.STATUS_ASSIGNED,
                RideLifecycleStateMachine.getNextStatus(RideLifecycleStateMachine.STATUS_SEARCHING, "accept"));
        assertEquals(RideLifecycleStateMachine.STATUS_EN_ROUTE,
                RideLifecycleStateMachine.getNextStatus(RideLifecycleStateMachine.STATUS_ASSIGNED, "start"));
        assertEquals(RideLifecycleStateMachine.STATUS_BOARDING,
                RideLifecycleStateMachine.getNextStatus(RideLifecycleStateMachine.STATUS_EN_ROUTE, "board"));
        assertEquals(RideLifecycleStateMachine.STATUS_COMPLETED,
                RideLifecycleStateMachine.getNextStatus(RideLifecycleStateMachine.STATUS_BOARDING, "complete"));
    }
}
