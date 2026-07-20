package com.goinghatway.requester.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.goinghatway.app.activities.RequestRideActivity;
import com.goinghatway.requester.R;
import com.goinghatway.requester.RequesterActiveRideActivity;
import com.goinghatway.requester.RequesterWalletActivity;

public class RequesterHomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_requester_home, container, false);

        root.findViewById(R.id.btn_book_ride).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), RequestRideActivity.class));
        });

        root.findViewById(R.id.btn_active_ride).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), RequesterActiveRideActivity.class));
        });

        root.findViewById(R.id.btn_wallet).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), RequesterWalletActivity.class));
        });

        return root;
    }
}
