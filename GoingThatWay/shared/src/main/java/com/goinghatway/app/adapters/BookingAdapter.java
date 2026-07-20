package com.goinghatway.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.goinghatway.app.databinding.ItemBookingBinding;
import com.goinghatway.app.models.Booking;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
    }

    private List<Booking> bookings = new ArrayList<>();
    private final OnBookingClickListener listener;

    public BookingAdapter(OnBookingClickListener listener) {
        this.listener = listener;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings != null ? bookings : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookingBinding binding = ItemBookingBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(bookings.get(position));
    }

    @Override
    public int getItemCount() { return bookings.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemBookingBinding b;

        ViewHolder(ItemBookingBinding binding) {
            super(binding.getRoot());
            b = binding;
        }

        void bind(Booking booking) {
            if (booking.getRide() != null) {
                b.tvRideNotes.setText(booking.getRide().getNotes() != null
                        ? booking.getRide().getNotes() : "No notes");
                b.tvDestination.setText(booking.getRide().getDestinationAddress());
                b.tvEarnings.setText(String.format(Locale.getDefault(),
                        "R %.2f", booking.getRide().getDriverEarning()));
            }
            b.tvMatchScore.setText(String.format(Locale.getDefault(),
                    "Match %.0f%%", booking.getScore() * 100));
            b.tvAlongRoute.setText(booking.isAlongRoute() ? "Along your route" : "Slight detour");
            b.tvAlongRoute.setTextColor(booking.isAlongRoute()
                    ? Color.parseColor("#4CAF50") : Color.parseColor("#FF9800"));
            b.tvStatus.setText(booking.getStatus());
            b.getRoot().setOnClickListener(v -> listener.onBookingClick(booking));
        }
    }
}
