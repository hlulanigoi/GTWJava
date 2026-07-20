package com.goinghatway.driver.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.goinghatway.driver.R;

import java.util.ArrayList;
import java.util.List;

public class TripListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_list);

        RecyclerView recyclerView = findViewById(R.id.rv_trips);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new TripListAdapter(getSampleTrips()));
    }

    private List<String> getSampleTrips() {
        List<String> trips = new ArrayList<>();
        trips.add("Johannesburg -> Durban · 08:00 · 3 seats");
        trips.add("Cape Town -> Stellenbosch · 14:30 · 2 seats");
        return trips;
    }
}
