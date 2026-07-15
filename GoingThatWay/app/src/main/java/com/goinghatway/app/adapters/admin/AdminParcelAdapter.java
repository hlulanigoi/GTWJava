package com.goinghatway.app.adapters.admin;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.goinghatway.app.databinding.ItemAdminParcelBinding;
import com.goinghatway.app.models.Parcel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminParcelAdapter extends RecyclerView.Adapter<AdminParcelAdapter.VH> {

    public interface Listener { void onCancelParcel(Parcel parcel); }

    private List<Parcel> parcels = new ArrayList<>();
    private final Listener listener;

    public AdminParcelAdapter(Listener listener) { this.listener = listener; }

    public void setParcels(List<Parcel> parcels) {
        this.parcels = parcels != null ? parcels : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemAdminParcelBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(parcels.get(pos), listener); }
    @Override public int getItemCount() { return parcels.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemAdminParcelBinding b;
        VH(ItemAdminParcelBinding b) { super(b.getRoot()); this.b = b; }

        void bind(Parcel p, Listener listener) {
            b.tvDescription.setText(p.getDescription());
            b.tvRoute.setText(p.getPickupAddress() + " → " + p.getDestinationAddress());
            b.tvFee.setText(String.format(Locale.getDefault(), "R %.2f", p.getFee()));
            b.tvStatus.setText(p.getStatus());

            String color = statusColor(p.getStatus());
            b.tvStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color)));

            boolean canCancel = !"CANCELLED".equals(p.getStatus()) && !"DELIVERED".equals(p.getStatus());
            b.btnCancel.setVisibility(canCancel ? android.view.View.VISIBLE : android.view.View.GONE);
            b.btnCancel.setOnClickListener(v -> listener.onCancelParcel(p));
        }

        private String statusColor(String status) {
            if (status == null) return "#9E9E9E";
            switch (status) {
                case "PENDING":   return "#FF9800";
                case "MATCHED":   return "#1565C0";
                case "COLLECTED": return "#6A1B9A";
                case "DELIVERED": return "#2E7D32";
                case "CANCELLED": return "#C62828";
                default:          return "#9E9E9E";
            }
        }
    }
}
