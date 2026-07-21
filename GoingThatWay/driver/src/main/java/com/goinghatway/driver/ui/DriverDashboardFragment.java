package com.goinghatway.driver.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.goinghatway.app.utils.OsmMapUtils;
import com.goinghatway.driver.R;
import com.goinghatway.driver.databinding.FragmentDriverDashboardBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class DriverDashboardFragment extends Fragment {

    private FragmentDriverDashboardBinding binding;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private boolean isOnline = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDriverDashboardBinding.inflate(inflater, container, false);

        OsmMapUtils.init(requireContext());
        OsmMapUtils.configure(binding.mapView);
        OsmMapUtils.centerOn(binding.mapView, OsmMapUtils.SA_LAT, OsmMapUtils.SA_LNG, 13.0);

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetRequest);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        binding.btnToggleAvailability.setOnClickListener(v -> toggleOnlineState());
        binding.btnAcceptRequest.setOnClickListener(v -> acceptRequest());

        // Simulate an incoming request after 5 seconds if online
        return binding.getRoot();
    }

    private void toggleOnlineState() {
        isOnline = !isOnline;
        updateOnlineUI();

        if (isOnline) {
            // Simulate incoming request for demo
            binding.mapView.postDelayed(() -> {
                if (isOnline) showIncomingRequest();
            }, 3000);
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    private void updateOnlineUI() {
        if (isOnline) {
            binding.tvStatusLabel.setText("Online");
            binding.tvStatusLabel.setTextColor(requireContext().getColor(com.goinghatway.app.R.color.accent));
            binding.btnToggleAvailability.setBackgroundResource(com.goinghatway.app.R.drawable.bg_online_pill);
            Toast.makeText(requireContext(), "You are now online", Toast.LENGTH_SHORT).show();
        } else {
            binding.tvStatusLabel.setText("Offline");
            binding.tvStatusLabel.setTextColor(requireContext().getColor(com.goinghatway.app.R.color.text_secondary));
            binding.btnToggleAvailability.setBackgroundResource(com.goinghatway.app.R.drawable.bg_offline_pill);
            Toast.makeText(requireContext(), "You are now offline", Toast.LENGTH_SHORT).show();
        }
    }

    private void showIncomingRequest() {
        binding.tvRequesterName.setText("Lennert Nijenbijvank");
        binding.tvRequesterRating.setText("★ 4.8");
        binding.tvTripPayout.setText("R 185.50");
        binding.tvPickupAddress.setText("Sandton City Mall, Sandton");
        binding.tvDestinationAddress.setText("Rosebank Mall, Rosebank");

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        // Add markers for pickup/destination
        OsmMapUtils.addMarker(binding.mapView, -26.1076, 28.0567, "Pickup", "Sandton", false);
        OsmMapUtils.addMarker(binding.mapView, -26.1465, 28.0436, "Destination", "Rosebank", false);
        OsmMapUtils.drawRoute(binding.mapView, -26.1076, 28.0567, -26.1465, 28.0436);
    }

    private void acceptRequest() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        Toast.makeText(requireContext(), "Request Accepted!", Toast.LENGTH_LONG).show();
        // Here you would navigate to the live trip screen
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
