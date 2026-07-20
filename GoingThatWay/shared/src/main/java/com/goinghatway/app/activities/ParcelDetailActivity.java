package com.goinghatway.app.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.goinghatway.app.R;
import com.goinghatway.app.databinding.ActivityParcelDetailBinding;
import com.goinghatway.app.models.Parcel;
import com.goinghatway.app.utils.Constants;
import com.goinghatway.app.viewmodels.ParcelViewModel;

import java.util.Locale;

public class ParcelDetailActivity extends AppCompatActivity {

    private ActivityParcelDetailBinding binding;
    private ParcelViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityParcelDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ParcelViewModel.class);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String parcelId = getIntent().getStringExtra(Constants.EXTRA_PARCEL_ID);
        if (parcelId != null) loadParcel(parcelId);
    }

    private void loadParcel(String id) {
        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.getParcel(id).observe(this, response -> {
            binding.progressBar.setVisibility(View.GONE);
            if (response != null && response.isSuccess() && response.getData() != null) {
                bindParcel(response.getData());
            }
        });
    }

    private void bindParcel(Parcel parcel) {
        binding.tvDescription.setText(parcel.getDescription());
        binding.tvPickupAddress.setText(parcel.getPickupAddress());
        binding.tvDestinationAddress.setText(parcel.getDestinationAddress());
        binding.tvWeight.setText(String.format(Locale.getDefault(), "%.1f kg — %s",
                parcel.getWeightKg(), parcel.getSizeLabel()));
        binding.tvFee.setText(String.format(Locale.getDefault(), "R %.2f", parcel.getFee()));
        binding.tvCarrierEarns.setText(String.format(Locale.getDefault(),
                "You earn: R %.2f", parcel.getCarrierEarnings()));
        binding.tvStatus.setText(parcel.getStatus());
        if (parcel.getSpecialInstructions() != null && !parcel.getSpecialInstructions().isEmpty()) {
            binding.tvSpecialInstructions.setText(parcel.getSpecialInstructions());
            binding.tvSpecialInstructions.setVisibility(View.VISIBLE);
        }
        if (parcel.getImageUrl() != null) {
            Glide.with(this).load(parcel.getImageUrl())
                    .placeholder(R.drawable.ic_parcel_placeholder)
                    .into(binding.ivParcelImage);
        }
        if (parcel.getSender() != null) {
            binding.tvSenderName.setText(parcel.getSender().getFullName());
            binding.tvSenderRating.setText(String.format(Locale.getDefault(),
                    "%.1f ★", parcel.getSender().getRating()));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
