package com.goinghatway.app.adapters.admin;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.goinghatway.app.databinding.ItemAdminUserBinding;
import com.goinghatway.app.models.User;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.VH> {

    public interface Listener { void onToggleActive(User user); }

    private List<User> users = new ArrayList<>();
    private final Listener listener;

    public AdminUserAdapter(Listener listener) { this.listener = listener; }

    public void setUsers(List<User> users) {
        this.users = users != null ? users : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemAdminUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        h.bind(users.get(position), listener);
    }

    @Override public int getItemCount() { return users.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemAdminUserBinding b;
        VH(ItemAdminUserBinding b) { super(b.getRoot()); this.b = b; }

        void bind(User u, Listener listener) {
            b.tvName.setText(u.getFullName());
            b.tvEmail.setText(u.getEmail());
            b.tvPhone.setText(u.getPhone() != null ? u.getPhone() : "—");
            b.tvRole.setText(u.getRole() != null ? u.getRole() : "USER");
            b.tvRating.setText(String.format("%.1f ★", u.getRating()));

            boolean active = u.isActive();
            b.btnToggle.setText(active ? "Deactivate" : "Activate");
            b.btnToggle.setBackgroundTintList(ColorStateList.valueOf(
                    Color.parseColor(active ? "#C62828" : "#2E7D32")));
            b.btnToggle.setOnClickListener(v -> listener.onToggleActive(u));

            b.chipStatus.setText(active ? "Active" : "Inactive");
            b.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(
                    Color.parseColor(active ? "#E8F5E9" : "#FFEBEE")));
            b.chipStatus.setTextColor(Color.parseColor(active ? "#2E7D32" : "#C62828"));
        }
    }
}
