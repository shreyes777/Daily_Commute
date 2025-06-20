package com.example.dailycommute;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TrafficAdapter extends RecyclerView.Adapter<TrafficAdapter.TrafficViewHolder> {

    private List<Route> routes = new ArrayList<>();

    @NonNull
    @Override
    public TrafficViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_traffic, parent, false);
        return new TrafficViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrafficViewHolder holder, int position) {
        Route route = routes.get(position);
        Leg leg = route.getLegs().get(0);

        holder.summary.setText(route.getSummary());
        holder.distance.setText(leg.getDistance().getText());
        holder.duration.setText(leg.getDuration().getText());
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    public void updateData(List<Route> newRoutes) {
        this.routes = newRoutes;
        notifyDataSetChanged();
    }

    static class TrafficViewHolder extends RecyclerView.ViewHolder {
        TextView summary, distance, duration;

        public TrafficViewHolder(@NonNull View itemView) {
            super(itemView);
            summary = itemView.findViewById(R.id.summary);
            distance = itemView.findViewById(R.id.distance);
            duration = itemView.findViewById(R.id.duration);
        }
    }
}
