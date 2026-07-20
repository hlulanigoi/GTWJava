package com.goinghatway.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.goinghatway.app.databinding.ItemTripBinding;
import com.goinghatway.app.models.Trip;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {

    public interface OnTripClickListener {
        void onTripClick(Trip trip);
    }

    private List<Trip> trips = new ArrayList<>();
    private final OnTripClickListener listener;

    public TripAdapter(OnTripClickListener listener) {
        this.listener = listener;
    }

    public void setTrips(List<Trip> trips) {
        this.trips = trips != null ? trips : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTripBinding binding = ItemTripBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(trips.get(position));
    }

    @Override
    public int getItemCount() { return trips.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTripBinding b;

        ViewHolder(ItemTripBinding binding) {
            super(binding.getRoot());
            b = binding;
        }

        void bind(Trip t) {
            b.tvOrigin.setText(t.getOriginAddress());
            b.tvDestination.setText(t.getDestinationAddress());
            b.tvDeparture.setText(t.getDepartureTime());
            b.tvTransportMode.setText(t.getTransportMode());
            b.tvCapacity.setText(String.format(Locale.getDefault(),
                    "%.1f kg capacity", t.getAvailableCapacityKg()));
            b.tvMatchCount.setText(t.getMatchedParcelCount() + " parcels matched");
            b.tvStatus.setText(t.getStatus());
            b.tvStatus.setTextColor(statusColor(t.getStatus()));
            b.getRoot().setOnClickListener(v -> listener.onTripClick(t));
        }

        private int statusColor(String status) {
            if (status == null) return Color.GRAY;
            switch (status) {
                case Trip.STATUS_ACTIVE:    return Color.parseColor("#4CAF50");
                case Trip.STATUS_ONGOING:   return Color.parseColor("#2196F3");
                case Trip.STATUS_COMPLETED: return Color.parseColor("#9E9E9E");
                case Trip.STATUS_CANCELLED: return Color.parseColor("#F44336");
                default:                   return Color.GRAY;
            }
        }
    }
}
