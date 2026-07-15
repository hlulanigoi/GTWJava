package com.goinghatway.app.adapters.admin;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.goinghatway.app.databinding.ItemAdminPaymentBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminPaymentAdapter extends RecyclerView.Adapter<AdminPaymentAdapter.VH> {

    public interface Listener {
        void onVerify(String paymentRef);
        void onReject(String paymentRef);
    }

    private List<Map<String, Object>> payments = new ArrayList<>();
    private final Listener listener;

    public AdminPaymentAdapter(Listener listener) { this.listener = listener; }

    public void setPayments(List<Map<String, Object>> payments) {
        this.payments = payments != null ? payments : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemAdminPaymentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        h.bind(payments.get(pos), listener);
    }
    @Override public int getItemCount() { return payments.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ItemAdminPaymentBinding b;
        VH(ItemAdminPaymentBinding b) { super(b.getRoot()); this.b = b; }

        void bind(Map<String, Object> p, Listener listener) {
            String ref    = String.valueOf(p.getOrDefault("reference", "N/A"));
            String user   = String.valueOf(p.getOrDefault("user_name", "Unknown"));
            String amount = String.valueOf(p.getOrDefault("amount", "0"));
            String type   = String.valueOf(p.getOrDefault("type", "PARCEL"));
            String date   = String.valueOf(p.getOrDefault("created_at", ""));

            b.tvReference.setText("Ref: " + ref);
            b.tvUser.setText("From: " + user);
            b.tvAmount.setText("R " + amount);
            b.tvType.setText(type);
            b.tvDate.setText(date);

            b.btnVerify.setOnClickListener(v -> listener.onVerify(ref));
            b.btnReject.setOnClickListener(v -> listener.onReject(ref));
        }
    }
}
