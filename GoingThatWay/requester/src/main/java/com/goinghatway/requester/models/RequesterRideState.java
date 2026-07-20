package com.goinghatway.requester.models;

public class RequesterRideState {
    public String driverName;
    public String vehicle;
    public String eta;
    public String pickup;
    public String destination;
    public String status;

    public static RequesterRideState createDemoState() {
        RequesterRideState s = new RequesterRideState();
        s.driverName = "John Doe";
        s.vehicle = "Toyota Corolla - KDR 123 GP";
        s.eta = "5 mins";
        s.pickup = "Sandton City";
        s.destination = "Rosebank Mall";
        s.status = "Accepted";
        return s;
    }
}
