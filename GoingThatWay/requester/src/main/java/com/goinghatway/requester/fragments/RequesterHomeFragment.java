package com.goinghatway.requester.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.goinghatway.app.activities.CreateParcelActivity;
import com.goinghatway.app.activities.RequestRideActivity;
import com.goinghatway.app.utils.OsmMapUtils;
import com.goinghatway.app.utils.SessionManager;
import com.goinghatway.requester.R;
import com.goinghatway.requester.RequesterActiveRideActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.Calendar;

public class RequesterHomeFragment extends Fragment {

    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_requester_home, container, false);

        mapView = root.findViewById(R.id.map_view);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        OsmMapUtils.init(requireContext());
        OsmMapUtils.configure(mapView);
        OsmMapUtils.centerOn(mapView, OsmMapUtils.SA_LAT, OsmMapUtils.SA_LNG, 13.0);

        setupUI(root);

        return root;
    }

    private void setupUI(View root) {
        SessionManager session = new SessionManager(requireContext());
        TextView tvGreeting = root.findViewById(R.id.tv_greeting);
        
        String name = session.getUserName();
        if (name == null || name.isEmpty()) name = "User";
        
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        String greetingPrefix = "Good Morning";
        if (hour >= 12 && hour < 17) greetingPrefix = "Good Afternoon";
        else if (hour >= 17) greetingPrefix = "Good Evening";

        tvGreeting.setText(greetingPrefix + ", " + name);

        root.findViewById(R.id.btn_book_ride).setOnClickListener(v -> startBooking());
        root.findViewById(R.id.card_destination).setOnClickListener(v -> startBooking());
        root.findViewById(R.id.card_search).setOnClickListener(v -> startBooking());

        root.findViewById(R.id.btn_send_parcel).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), CreateParcelActivity.class));
        });

        root.findViewById(R.id.btn_active_ride).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), RequesterActiveRideActivity.class));
        });

        root.findViewById(R.id.btn_shortcut_home).setOnClickListener(v -> startBooking());
        root.findViewById(R.id.btn_shortcut_work).setOnClickListener(v -> startBooking());

        root.findViewById(R.id.fab_my_location).setOnClickListener(v -> centerOnMyLocation());
        
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED) {
            OsmMapUtils.addMyLocation(mapView, requireContext());
        }
    }

    private void startBooking() {
        startActivity(new Intent(requireContext(), RequestRideActivity.class));
    }

    @SuppressWarnings("MissingPermission")
    private void centerOnMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                mapView.getController().animateTo(new GeoPoint(location.getLatitude(), location.getLongitude()));
                mapView.getController().setZoom(16.0);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }
}
