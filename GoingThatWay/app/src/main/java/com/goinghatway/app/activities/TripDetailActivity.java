package com.goinghatway.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.goinghatway.app.adapters.MatchAdapter;
import com.goinghatway.app.databinding.ActivityTripDetailBinding;
import com.goinghatway.app.models.Match;
import com.goinghatway.app.models.Trip;
import com.goinghatway.app.utils.Constants;
import com.goinghatway.app.viewmodels.MatchViewModel;
import com.goinghatway.app.viewmodels.TripViewModel;

import java.util.List;

public class TripDetailActivity extends AppCompatActivity {

    private ActivityTripDetailBinding binding;
    private TripViewModel tripViewModel;
    private MatchViewModel matchViewModel;
    private MatchAdapter matchAdapter;
    private String tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTripDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tripViewModel  = new ViewModelProvider(this).get(TripViewModel.class);
        matchViewModel = new ViewModelProvider(this).get(MatchViewModel.class);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tripId = getIntent().getStringExtra(Constants.EXTRA_TRIP_ID);

        setupMatchesRecycler();

        binding.btnFindParcels.setOnClickListener(v -> findMatchingParcels());

        binding.btnViewMap.setOnClickListener(v -> {
            Intent mapIntent = new Intent(this, TripMapActivity.class);
            // Extra coordinates could be attached here once the Trip model is loaded
            startActivity(mapIntent);
        });

        if (tripId != null) loadTrip();
    }

    private void setupMatchesRecycler() {
        matchAdapter = new MatchAdapter(match -> {
            Intent intent = new Intent(this, ParcelDetailActivity.class);
            intent.putExtra(Constants.EXTRA_PARCEL_ID, match.getParcelId());
            startActivity(intent);
        });
        binding.rvMatches.setLayoutManager(new LinearLayoutManager(this));
        binding.rvMatches.setAdapter(matchAdapter);
    }

    private void loadTrip() {
        // Load matches for this trip
        matchViewModel.getMyMatches().observe(this, response -> {
            if (response != null && response.isSuccess() && response.getData() != null) {
                List<Match> tripMatches = response.getData().stream()
                        .filter(m -> tripId.equals(m.getTripId()))
                        .collect(java.util.stream.Collectors.toList());
                matchAdapter.setMatches(tripMatches);
                binding.tvMatchCount.setText(tripMatches.size() + " parcel(s) matched");
            }
        });
    }

    private void findMatchingParcels() {
        binding.btnFindParcels.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);
        tripViewModel.matchParcelsToTrip(tripId).observe(this, response -> {
            binding.btnFindParcels.setEnabled(true);
            binding.progressBar.setVisibility(View.GONE);
            if (response != null && response.isSuccess() && response.getData() != null) {
                int count = response.getData().size();
                matchAdapter.setMatches(response.getData());
                Toast.makeText(this, count + " matching parcel(s) found!", Toast.LENGTH_SHORT).show();
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
