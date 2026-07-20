package com.goinghatway.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.goinghatway.app.adapters.BookingAdapter;
import com.goinghatway.app.databinding.ActivityTripDetailBinding;
import com.goinghatway.app.models.Booking;
import com.goinghatway.app.utils.Constants;
import com.goinghatway.app.viewmodels.BookingViewModel;
import com.goinghatway.app.viewmodels.TripViewModel;

import java.util.List;

public class TripDetailActivity extends AppCompatActivity {

    private ActivityTripDetailBinding binding;
    private TripViewModel tripViewModel;
    private BookingViewModel bookingViewModel;
    private BookingAdapter bookingAdapter;
    private String tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTripDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tripViewModel   = new ViewModelProvider(this).get(TripViewModel.class);
        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tripId = getIntent().getStringExtra(Constants.EXTRA_TRIP_ID);

        setupBookingsRecycler();

        binding.btnFindRides.setOnClickListener(v -> findMatchingRides());

        binding.btnViewMap.setOnClickListener(v -> {
            Intent mapIntent = new Intent(this, TripMapActivity.class);
            startActivity(mapIntent);
        });

        if (tripId != null) loadBookings();
    }

    private void setupBookingsRecycler() {
        bookingAdapter = new BookingAdapter(booking -> {
            Intent intent = new Intent(this, RideDetailActivity.class);
            intent.putExtra(Constants.EXTRA_RIDE_ID, booking.getRideId());
            startActivity(intent);
        });
        binding.rvMatches.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMatches.setAdapter(bookingAdapter);
    }

    private void loadBookings() {
        bookingViewModel.getMyBookings().observe(this, response -> {
            if (response != null && response.isSuccess() && response.getData() != null) {
                List<Booking> tripBookings = response.getData().stream()
                        .filter(b -> tripId.equals(b.getTripId()))
                        .collect(java.util.stream.Collectors.toList());
                bookingAdapter.setBookings(tripBookings);
                binding.tvMatchCount.setText(tripBookings.size() + " ride request(s) matched");
            }
        });
    }

    private void findMatchingRides() {
        binding.btnFindRides.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);
        tripViewModel.matchRidesToTrip(tripId).observe(this, response -> {
            binding.btnFindRides.setEnabled(true);
            binding.progressBar.setVisibility(View.GONE);
            if (response != null && response.isSuccess() && response.getData() != null) {
                int count = response.getData().size();
                bookingAdapter.setBookings(response.getData());
                Toast.makeText(this, count + " matching ride request(s) found!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No new matches found.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
