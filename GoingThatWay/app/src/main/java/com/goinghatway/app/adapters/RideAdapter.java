package com.goinghatway.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.goinghatway.app.databinding.ItemRideBinding;
import com.goinghatway.app.models.Ride;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.ViewHolder> {

    public interface OnRideClickListener {
        void onRideClick(Ride ride);
    }

    private List<Ride> rides = new ArrayList<>();
    private final OnRideClickListener listener;

    public RideAdapter(OnRideClickListener listener) {
        this.listener = listener;
    }

    public void setRides(List<Ride> rides) {
        this.rides = rides != null ? rides : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRideBinding binding = ItemRideBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(rides.get(position));
    }

    @Override
    public int getItemCount() { return rides.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemRideBinding b;

        ViewHolder(ItemRideBinding binding) {
            super(binding.getRoot());
            b = binding;
        }

        void bind(Ride r) {
            b.tvNotes.setText(r.getNotes() != null && !r.getNotes().isEmpty()
                    ? r.getNotes() : "No notes");
            b.tvPickup.setText(r.getPickupAddress());
            b.tvDestination.setText(r.getDestinationAddress());
            b.tvPassengers.setText(String.format(Locale.getDefault(), "%d pax · %s",
                    r.getPassengerCount(), r.getLuggageSize()));
            b.tvEarnings.setText(String.format(Locale.getDefault(),
                    "Earn R%.2f", r.getDriverEarning()));
            b.tvStatus.setText(r.getStatus());
            b.tvStatus.setTextColor(statusColor(r.getStatus()));
            b.getRoot().setOnClickListener(v -> listener.onRideClick(r));
        }

        private int statusColor(String status) {
            if (status == null) return Color.GRAY;
            switch (status) {
                case Ride.STATUS_PENDING:   return Color.parseColor("#FF9800");
                case Ride.STATUS_MATCHED:   return Color.parseColor("#2196F3");
                case Ride.STATUS_EN_ROUTE:  return Color.parseColor("#9C27B0");
                case Ride.STATUS_COMPLETED: return Color.parseColor("#4CAF50");
                case Ride.STATUS_CANCELLED: return Color.parseColor("#F44336");
                default:                    return Color.GRAY;
            }
        }
    }
}
