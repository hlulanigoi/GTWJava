package com.goinghatway.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.goinghatway.app.databinding.ItemMatchBinding;
import com.goinghatway.app.models.Match;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.ViewHolder> {

    public interface OnMatchClickListener {
        void onMatchClick(Match match);
    }

    private List<Match> matches = new ArrayList<>();
    private final OnMatchClickListener listener;

    public MatchAdapter(OnMatchClickListener listener) {
        this.listener = listener;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches != null ? matches : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMatchBinding binding = ItemMatchBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(matches.get(position));
    }

    @Override
    public int getItemCount() { return matches.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemMatchBinding b;

        ViewHolder(ItemMatchBinding binding) {
            super(binding.getRoot());
            b = binding;
        }

        void bind(Match m) {
            if (m.getParcel() != null) {
                b.tvParcelDescription.setText(m.getParcel().getDescription());
                b.tvDestination.setText(m.getParcel().getDestinationAddress());
                b.tvEarnings.setText(String.format(Locale.getDefault(),
                        "R %.2f", m.getParcel().getCarrierEarnings()));
            }
            b.tvMatchScore.setText(String.format(Locale.getDefault(),
                    "Match %.0f%%", m.getMatchScore() * 100));
            b.tvAlongRoute.setText(m.isAlongRoute() ? "Along your route" : "Slight detour");
            b.tvAlongRoute.setTextColor(m.isAlongRoute()
                    ? Color.parseColor("#4CAF50") : Color.parseColor("#FF9800"));
            b.tvStatus.setText(m.getStatus());
            b.getRoot().setOnClickListener(v -> listener.onMatchClick(m));
        }
    }
}
