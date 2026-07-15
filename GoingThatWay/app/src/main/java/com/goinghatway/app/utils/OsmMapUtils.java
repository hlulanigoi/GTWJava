package com.goinghatway.app.utils;

import android.content.Context;
import android.graphics.Color;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility helpers for osmdroid (OpenStreetMap) — no API key needed.
 */
public class OsmMapUtils {

    /**
     * Call once in Application.onCreate() or before any MapView is inflated.
     */
    public static void init(Context context) {
        Configuration.getInstance().load(context,
                context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(context.getPackageName());
    }

    /**
     * Apply standard settings to a MapView: OSM tiles, zoom controls, multi-touch.
     */
    public static void configure(MapView map) {
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.getController().setZoom(13.0);
    }

    /**
     * Center the map on a given coordinate.
     */
    public static void centerOn(MapView map, double lat, double lng, double zoom) {
        map.getController().setZoom(zoom);
        map.getController().setCenter(new GeoPoint(lat, lng));
    }

    /**
     * Add a draggable marker with a title/snippet.
     */
    public static Marker addMarker(MapView map, double lat, double lng,
                                   String title, String snippet, boolean draggable) {
        Marker marker = new Marker(map);
        marker.setPosition(new GeoPoint(lat, lng));
        marker.setTitle(title);
        marker.setSnippet(snippet);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setDraggable(draggable);
        map.getOverlays().add(marker);
        map.invalidate();
        return marker;
    }

    /**
     * Draw a dashed route line between two points (straight line approximation).
     * For real routing, call an OSRM/GraphHopper endpoint and pass the decoded shape.
     */
    public static Polyline drawRoute(MapView map, double startLat, double startLng,
                                     double endLat, double endLng) {
        List<GeoPoint> points = new ArrayList<>();
        points.add(new GeoPoint(startLat, startLng));
        points.add(new GeoPoint(endLat, endLng));
        return drawPolyline(map, points, Color.parseColor("#2E7D32"), 8f);
    }

    /**
     * Draw a polyline through a list of GeoPoints.
     */
    public static Polyline drawPolyline(MapView map, List<GeoPoint> points,
                                        int color, float width) {
        Polyline line = new Polyline(map);
        line.setPoints(points);
        line.getOutlinePaint().setColor(color);
        line.getOutlinePaint().setStrokeWidth(width);
        map.getOverlays().add(line);
        map.invalidate();
        return line;
    }

    /**
     * Add the blue "my location" dot overlay (requires ACCESS_FINE_LOCATION permission).
     */
    public static MyLocationNewOverlay addMyLocation(MapView map, Context context) {
        MyLocationNewOverlay overlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(context), map);
        overlay.enableMyLocation();
        map.getOverlays().add(overlay);
        return overlay;
    }

    /** South Africa default center */
    public static final double SA_LAT = -26.2041;
    public static final double SA_LNG =  28.0473;
    public static final double DEFAULT_ZOOM = 7.0;
}
