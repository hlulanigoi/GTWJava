package com.goinghatway.driver.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.goinghatway.driver.R;
import com.goinghatway.driver.databinding.FragmentDriverDashboardBinding;

public class DriverDashboardFragment extends Fragment {

    private FragmentDriverDashboardBinding binding;
    private boolean isOnline = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDriverDashboardBinding.inflate(inflater, container, false);

        binding.btnToggleAvailability.setOnClickListener(v -> toggleOnlineState());

        return binding.getRoot();
    }

    private void toggleOnlineState() {
        isOnline = !isOnline;
        updateOnlineUI();
    }

    private void updateOnlineUI() {
        if (isOnline) {
            binding.btnToggleAvailability.setText("GO OFFLINE");
            // Green tint handled by Btn.Primary; update descriptor
            binding.tvStatusDesc.setText("You are online.\nWaiting for parcel requests.");
            binding.tvStatusLabel.setText("ONLINE");
            binding.tvStatusLabel.setTextColor(requireContext().getColor(R.color.primary));
            binding.statusPill.setBackground(
                    requireContext().getDrawable(R.drawable.bg_online_pill));
            Toast.makeText(requireContext(), "You are now online", Toast.LENGTH_SHORT).show();
        } else {
            binding.btnToggleAvailability.setText("GO ONLINE");
            binding.tvStatusDesc.setText("You are offline.\nGo online to receive parcel requests.");
            binding.tvStatusLabel.setText("OFFLINE");
            binding.tvStatusLabel.setTextColor(requireContext().getColor(R.color.text_secondary));
            binding.statusPill.setBackground(
                    requireContext().getDrawable(R.drawable.bg_offline_pill));
            Toast.makeText(requireContext(), "You are now offline", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
