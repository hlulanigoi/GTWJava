package com.goinghatway.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.goinghatway.app.databinding.ItemParcelBinding;
import com.goinghatway.app.models.Parcel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ParcelAdapter extends RecyclerView.Adapter<ParcelAdapter.ViewHolder> {

    public interface OnParcelClickListener {
        void onParcelClick(Parcel parcel);
    }

    private List<Parcel> parcels = new ArrayList<>();
    private final OnParcelClickListener listener;

    public ParcelAdapter(OnParcelClickListener listener) {
        this.listener = listener;
    }

    public void setParcels(List<Parcel> parcels) {
        this.parcels = parcels != null ? parcels : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemParcelBinding binding = ItemParcelBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(parcels.get(position));
    }

    @Override
    public int getItemCount() { return parcels.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemParcelBinding b;

        ViewHolder(ItemParcelBinding binding) {
            super(binding.getRoot());
            b = binding;
        }

        void bind(Parcel p) {
            b.tvDescription.setText(p.getDescription());
            b.tvPickup.setText(p.getPickupAddress());
            b.tvDestination.setText(p.getDestinationAddress());
            b.tvWeight.setText(String.format(Locale.getDefault(), "%.1f kg · %s",
                    p.getWeightKg(), p.getSizeLabel()));
            b.tvEarnings.setText(String.format(Locale.getDefault(),
                    "Earn R%.2f", p.getCarrierEarnings()));
            b.tvStatus.setText(p.getStatus());
            b.tvStatus.setTextColor(statusColor(p.getStatus()));
            b.getRoot().setOnClickListener(v -> listener.onParcelClick(p));
        }

        private int statusColor(String status) {
            if (status == null) return Color.GRAY;
            switch (status) {
                case Parcel.STATUS_PENDING:   return Color.parseColor("#FF9800");
                case Parcel.STATUS_MATCHED:   return Color.parseColor("#2196F3");
                case Parcel.STATUS_COLLECTED: return Color.parseColor("#9C27B0");
                case Parcel.STATUS_DELIVERED: return Color.parseColor("#4CAF50");
                case Parcel.STATUS_CANCELLED: return Color.parseColor("#F44336");
                default:                      return Color.GRAY;
            }
        }
    }
}
