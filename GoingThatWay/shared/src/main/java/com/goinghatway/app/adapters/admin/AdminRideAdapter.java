package com.goinghatway.app.adapters.admin;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.goinghatway.app.databinding.ItemAdminRideBinding;
import com.goinghatway.app.models.Ride;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminRideAdapter extends RecyclerView.Adapter<AdminRideAdapter.VH> {

    public interface Listener { void onCancelRide(Ride ride); }

    private List<Ride> rides = new ArrayList<>();
    private final Listener listener;

    public AdminRideAdapter(Listener listener) { this.listener = listener; }

    public void setRides(List<Ride> rides) {
        this.rides = rides != null ? rides : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemAdminRideBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(rides.get(pos), listener); }
    @Override public int getItemCount() { return rides.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemAdminRideBinding b;
        VH(ItemAdminRideBinding b) { super(b.getRoot()); this.b = b; }

        void bind(Ride r, Listener listener) {
            b.tvNotes.setText(r.getNotes() != null && !r.getNotes().isEmpty() ? r.getNotes() : "No notes");
            b.tvRoute.setText(r.getPickupAddress() + " → " + r.getDestinationAddress());
            b.tvFare.setText(String.format(Locale.getDefault(), "R %.2f", r.getFare()));
            b.tvStatus.setText(r.getStatus());

            String color = statusColor(r.getStatus());
            b.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color)));

            boolean canCancel = !"CANCELLED".equals(r.getStatus()) && !"COMPLETED".equals(r.getStatus());
            b.btnCancel.setVisibility(canCancel ? android.view.View.VISIBLE : android.view.View.GONE);
            b.btnCancel.setOnClickListener(v -> listener.onCancelRide(r));
        }

        private String statusColor(String status) {
            if (status == null) return "#9E9E9E";
            switch (status) {
                case "PENDING":   return "#FF9800";
                case "MATCHED":   return "#1565C0";
                case "EN_ROUTE":  return "#6A1B9A";
                case "COMPLETED": return "#2E7D32";
                case "CANCELLED": return "#C62828";
                default:          return "#9E9E9E";
            }
        }
    }
}
