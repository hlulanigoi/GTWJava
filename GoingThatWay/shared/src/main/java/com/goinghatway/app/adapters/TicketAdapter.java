package com.goinghatway.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.goinghatway.app.databinding.ItemTicketBinding;
import com.goinghatway.app.models.Ticket;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.ViewHolder> {

    private List<Ticket> tickets = new ArrayList<>();

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets != null ? tickets : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTicketBinding binding = ItemTicketBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(tickets.get(position));
    }

    @Override
    public int getItemCount() { return tickets.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemTicketBinding b;

        ViewHolder(ItemTicketBinding binding) {
            super(binding.getRoot());
            b = binding;
        }

        void bind(Ticket t) {
            b.tvTicketCode.setText(t.getTicketCode());
            b.tvPricePaid.setText(String.format(Locale.getDefault(), "R %.2f", t.getPricePaid()));
            b.tvStatus.setText(t.getStatus());
            b.tvPurchasedAt.setText("Purchased: " + t.getPurchasedAt());
            b.tvExpiresAt.setText("Expires: " + t.getExpiresAt());
            b.tvStatus.setTextColor(
                    Ticket.STATUS_ACTIVE.equals(t.getStatus())
                            ? Color.parseColor("#4CAF50")
                            : Color.GRAY);
        }
    }
}
