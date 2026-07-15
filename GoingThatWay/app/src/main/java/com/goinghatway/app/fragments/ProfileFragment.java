package com.goinghatway.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.goinghatway.app.activities.LoginActivity;
import com.goinghatway.app.api.ApiClient;
import com.goinghatway.app.databinding.FragmentProfileBinding;
import com.goinghatway.app.utils.SessionManager;
import com.goinghatway.app.viewmodels.AuthViewModel;

import java.util.Locale;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private AuthViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        loadProfile();

        binding.btnLogout.setOnClickListener(v -> logout());
    }

    private void loadProfile() {
        viewModel.getMe().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.isSuccess() && response.getData() != null) {
                var user = response.getData();
                binding.tvName.setText(user.getFullName());
                binding.tvEmail.setText(user.getEmail());
                binding.tvPhone.setText(user.getPhone());
                binding.tvRating.setText(String.format(Locale.getDefault(),
                        "%.1f ★", user.getRating()));
                binding.tvDeliveries.setText(String.valueOf(user.getTotalDeliveries()));
                binding.tvParcelsSent.setText(String.valueOf(user.getTotalParcelsSent()));
                binding.tvTicketsOwned.setText(String.valueOf(user.getTicketsOwned()));
            }
        });
    }

    private void logout() {
        new SessionManager(requireContext()).clearSession();
        ApiClient.reset();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
