package com.goinghatway.app.adapters.admin;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.goinghatway.app.databinding.ItemAdminDriverBinding;
import com.goinghatway.app.models.User;

import java.util.ArrayList;
import java.util.List;

public class AdminDriverAdapter extends RecyclerView.Adapter<AdminDriverAdapter.VH> {

    public interface Listener {
        void onApprove(User user);
        void onReject(User user);
    }

    private List<User> drivers = new ArrayList<>();
    private final Listener listener;

    public AdminDriverAdapter(Listener listener) { this.listener = listener; }

    public void setDrivers(List<User> drivers) {
        this.drivers = drivers != null ? drivers : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemAdminDriverBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(drivers.get(pos), listener); }
    @Override public int getItemCount() { return drivers.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemAdminDriverBinding b;
        VH(ItemAdminDriverBinding b) { super(b.getRoot()); this.b = b; }

        void bind(User u, Listener listener) {
            b.tvName.setText(u.getFullName());
            b.tvLicense.setText("License: " + (u.getLicenseNumber() != null ? u.getLicenseNumber() : "—"));
            b.tvVehicle.setText((u.getVehicleModel() != null ? u.getVehicleModel() : "—")
                    + " · " + (u.getVehiclePlate() != null ? u.getVehiclePlate() : "—"));
            b.btnApprove.setOnClickListener(v -> listener.onApprove(u));
            b.btnReject.setOnClickListener(v -> listener.onReject(u));
        }
    }
}
