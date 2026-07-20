package com.goinghatway.app.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.goinghatway.app.R;
import com.goinghatway.app.utils.Constants;
import com.goinghatway.app.utils.OsmMapUtils;

import org.osmdroid.views.MapView;

public class TripMapActivity extends AppCompatActivity {

    private MapView mapView;

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) OsmMapUtils.addMyLocation(mapView, this);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_map);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Trip Route");
        }

        mapView = findViewById(R.id.map_view);
        OsmMapUtils.configure(mapView);

        double originLat  = getIntent().getDoubleExtra("origin_lat",  OsmMapUtils.SA_LAT);
        double originLng  = getIntent().getDoubleExtra("origin_lng",  OsmMapUtils.SA_LNG);
        double destLat    = getIntent().getDoubleExtra("dest_lat",    originLat + 0.5);
        double destLng    = getIntent().getDoubleExtra("dest_lng",    originLng + 0.5);
        String originName = getIntent().getStringExtra("origin_name");
        String destName   = getIntent().getStringExtra("dest_name");

        // Centre map between origin and destination
        double midLat = (originLat + destLat) / 2.0;
        double midLng = (originLng + destLng) / 2.0;
        OsmMapUtils.centerOn(mapView, midLat, midLng, 9.0);

        // Draw straight-line route
        OsmMapUtils.drawRoute(mapView, originLat, originLng, destLat, destLng);

        // Markers
        OsmMapUtils.addMarker(mapView, originLat, originLng,
                originName != null ? originName : "Origin", "Pickup point", false);
        OsmMapUtils.addMarker(mapView, destLat, destLng,
                destName != null ? destName : "Destination", "Drop-off point", false);

        // Show user location if available (request runtime permission if not yet granted)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            OsmMapUtils.addMyLocation(mapView, this);
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @Override
    protected void onResume() { super.onResume(); mapView.onResume(); }

    @Override
    protected void onPause() { super.onPause(); mapView.onPause(); }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
