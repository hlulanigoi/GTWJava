package com.goinghatway.app.utils;

import com.goinghatway.app.models.Parcel;
import com.goinghatway.app.models.RoutePoint;
import com.goinghatway.app.models.Trip;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for matching parcels to trips based on route proximity.
 *
 * Algorithm:
 * 1. Check if the parcel pickup is within MAX_DETOUR_KM of the trip's route.
 * 2. Check if the parcel destination is AT or BEFORE the trip's destination
 *    (parcel destination must be reachable before the traveler arrives at theirs).
 * 3. Score the match — exact-route matches score 1.0, slight detours score lower.
 */
public class RouteMatchingUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Returns true if the parcel can potentially be carried on this trip.
     */
    public static boolean isMatch(Parcel parcel, Trip trip) {
        if (trip.getWaypoints() == null || trip.getWaypoints().isEmpty()) {
            // No waypoints — just check straight-line proximity
            return isPointNearSegment(
                    parcel.getPickupLat(), parcel.getPickupLng(),
                    trip.getOriginLat(), trip.getOriginLng(),
                    trip.getDestinationLat(), trip.getDestinationLng(),
                    Constants.ROUTE_BUFFER_KM
            ) && isDestinationReachable(parcel, trip);
        }

        List<RoutePoint> fullRoute = buildFullRoute(trip);
        return isPickupAlongRoute(parcel, fullRoute) && isDestinationReachable(parcel, trip);
    }

    /**
     * Score from 0.0 (no match) to 1.0 (perfect along-route match).
     */
    public static double scoreMatch(Parcel parcel, Trip trip) {
        if (!isMatch(parcel, trip)) return 0.0;

        double detourKm = estimateDetourKm(parcel, trip);
        if (detourKm <= 0.5) return 1.0;
        if (detourKm >= Constants.MAX_DETOUR_KM) return 0.1;
        return 1.0 - (detourKm / Constants.MAX_DETOUR_KM) * 0.9;
    }

    /**
     * Filter a list of parcels to only those that match a given trip.
     */
    public static List<Parcel> filterMatchingParcels(List<Parcel> parcels, Trip trip) {
        List<Parcel> matches = new ArrayList<>();
        for (Parcel parcel : parcels) {
            if (isMatch(parcel, trip)) {
                matches.add(parcel);
            }
        }
        return matches;
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private static boolean isPickupAlongRoute(Parcel parcel, List<RoutePoint> route) {
        for (int i = 0; i < route.size() - 1; i++) {
            RoutePoint a = route.get(i);
            RoutePoint b = route.get(i + 1);
            if (isPointNearSegment(
                    parcel.getPickupLat(), parcel.getPickupLng(),
                    a.getLat(), a.getLng(),
                    b.getLat(), b.getLng(),
                    Constants.ROUTE_BUFFER_KM)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Parcel destination must be within ROUTE_BUFFER_KM of any point on the
     * route AFTER the pickup segment, or equal to the trip's destination.
     */
    private static boolean isDestinationReachable(Parcel parcel, Trip trip) {
        double distToTripDest = haversineKm(
                parcel.getDestinationLat(), parcel.getDestinationLng(),
                trip.getDestinationLat(), trip.getDestinationLng()
        );
        if (distToTripDest <= Constants.ROUTE_BUFFER_KM) return true;

        if (trip.getWaypoints() == null) return false;
        List<RoutePoint> fullRoute = buildFullRoute(trip);
        for (RoutePoint point : fullRoute) {
            if (haversineKm(parcel.getDestinationLat(), parcel.getDestinationLng(),
                    point.getLat(), point.getLng()) <= Constants.ROUTE_BUFFER_KM) {
                return true;
            }
        }
        return false;
    }

    private static double estimateDetourKm(Parcel parcel, Trip trip) {
        // Rough estimate: distance from closest route point to pickup
        List<RoutePoint> fullRoute = buildFullRoute(trip);
        double minDist = Double.MAX_VALUE;
        for (RoutePoint point : fullRoute) {
            double d = haversineKm(
                    parcel.getPickupLat(), parcel.getPickupLng(),
                    point.getLat(), point.getLng()
            );
            if (d < minDist) minDist = d;
        }
        return minDist;
    }

    private static List<RoutePoint> buildFullRoute(Trip trip) {
        List<RoutePoint> route = new ArrayList<>();
        route.add(new RoutePoint(trip.getOriginLat(), trip.getOriginLng()));
        if (trip.getWaypoints() != null) {
            route.addAll(trip.getWaypoints());
        }
        route.add(new RoutePoint(trip.getDestinationLat(), trip.getDestinationLng()));
        return route;
    }

    /**
     * Returns true if point (px, py) is within {@code bufferKm} km of the
     * line segment from (ax, ay) to (bx, by).
     */
    private static boolean isPointNearSegment(
            double px, double py,
            double ax, double ay,
            double bx, double by,
            double bufferKm) {

        double closestLat = ax + projectOnSegment(px, py, ax, ay, bx, by) * (bx - ax);
        double closestLng = ay + projectOnSegment(px, py, ax, ay, bx, by) * (by - ay);
        return haversineKm(px, py, closestLat, closestLng) <= bufferKm;
    }

    private static double projectOnSegment(
            double px, double py,
            double ax, double ay,
            double bx, double by) {

        double abx = bx - ax, aby = by - ay;
        double apx = px - ax, apy = py - ay;
        double t = (apx * abx + apy * aby) / (abx * abx + aby * aby + 1e-10);
        return Math.max(0, Math.min(1, t));
    }

    /** Haversine distance in km between two lat/lng points */
    public static double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
