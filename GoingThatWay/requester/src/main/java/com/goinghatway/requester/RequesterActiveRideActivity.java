package com.goinghatway.requester;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.goinghatway.app.activities.TripMapActivity;
import com.goinghatway.app.models.RideTrackingState;
import com.goinghatway.app.utils.OsmMapUtils;
import com.goinghatway.app.utils.RideLifecycleStateMachine;
import com.goinghatway.requester.models.RequesterRideState;

public class RequesterActiveRideActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requester_active_ride);

        RequesterRideState state = RequesterRideState.createDemoState();
        RideTrackingState trackingState = RideTrackingState.demoState();

        String statusStr = getIntent().getStringExtra("ride_status");
        if (statusStr == null || statusStr.trim().isEmpty()) {
            statusStr = RideLifecycleStateMachine.STATUS_SEARCHING;
        }
        String rideType = getIntent().getStringExtra("ride_type");
        String pickupStr = getIntent().getStringExtra("pickup_address");
        String destinationStr = getIntent().getStringExtra("destination_address");
        int passengerCount = getIntent().getIntExtra("passenger_count", 1);
        double fare = getIntent().getDoubleExtra("fare", 0.0);

        TextView tvDriverName = findViewById(R.id.tv_driver_name);
        TextView tvVehicle = findViewById(R.id.tv_vehicle);
        TextView tvEta = findViewById(R.id.tv_eta);
        TextView tvPickup = findViewById(R.id.tv_pickup);
        TextView tvDestination = findViewById(R.id.tv_destination);
        TextView tvStatus = findViewById(R.id.tv_status);
        Button btnOpenMap = findViewById(R.id.btn_open_map);

        if (tvDriverName != null) tvDriverName.setText("Driver: " + state.driverName);
        if (tvVehicle != null) tvVehicle.setText("Vehicle: " + state.vehicle);
        if (tvEta != null) tvEta.setText("ETA: " + state.eta);
        if (tvPickup != null) tvPickup.setText("Pickup: " + (pickupStr != null ? pickupStr : state.pickup));
        if (tvDestination != null) tvDestination.setText("Destination: " + (destinationStr != null ? destinationStr : state.destination));
        
        if (tvStatus != null) {
            String displayStatus = RideLifecycleStateMachine.getDisplayStatus(statusStr);
            if (rideType != null) {
                tvStatus.setText(displayStatus + " • " + rideType + " • " + passengerCount + " passenger(s) • R" + String.format(java.util.Locale.getDefault(), "%.2f", fare));
            } else {
                tvStatus.setText(displayStatus);
            }
        }

        if (btnOpenMap != null) {
            btnOpenMap.setOnClickListener(v -> {
                Intent intent = new Intent(this, TripMapActivity.class);
                intent.putExtra("origin_lat", OsmMapUtils.SA_LAT);
                intent.putExtra("origin_lng", OsmMapUtils.SA_LNG);
                intent.putExtra("dest_lat", OsmMapUtils.SA_LAT + 0.04);
                intent.putExtra("dest_lng", OsmMapUtils.SA_LNG + 0.04);
                intent.putExtra("origin_name", state.pickup);
                intent.putExtra("dest_name", state.destination);
                startActivity(intent);
            });
        }
    }
}
