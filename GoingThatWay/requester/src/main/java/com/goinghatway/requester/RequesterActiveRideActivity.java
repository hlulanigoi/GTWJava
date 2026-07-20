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

        String status = getIntent().getStringExtra("ride_status");
        if (status == null || status.trim().isEmpty()) {
            status = RideLifecycleStateMachine.STATUS_SEARCHING;
        }
        String rideType = getIntent().getStringExtra("ride_type");
        String pickup = getIntent().getStringExtra("pickup_address");
        String destination = getIntent().getStringExtra("destination_address");
        int passengerCount = getIntent().getIntExtra("passenger_count", 1);
        double fare = getIntent().getDoubleExtra("fare", 0.0);

        TextView driverName = findViewById(R.id.tv_driver_name);
        TextView vehicle = findViewById(R.id.tv_vehicle);
        TextView eta = findViewById(R.id.tv_eta);
        TextView pickup = findViewById(R.id.tv_pickup);
        TextView destination = findViewById(R.id.tv_destination);
        TextView status = findViewById(R.id.tv_status);
        Button openMapButton = findViewById(R.id.btn_open_map);

        driverName.setText("Driver: " + state.driverName);
        vehicle.setText("Vehicle: " + state.vehicle);
        eta.setText("ETA: " + state.eta);
        pickup.setText("Pickup: " + (pickup != null ? pickup : state.pickup));
        destination.setText("Destination: " + (destination != null ? destination : state.destination));
        status.setText(RideLifecycleStateMachine.getDisplayStatus(status));
        if (rideType != null) {
            status.setText(status + " • " + rideType + " • " + passengerCount + " passenger(s) • R" + String.format(java.util.Locale.getDefault(), "%.2f", fare));
        }

        openMapButton.setOnClickListener(v -> {
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
