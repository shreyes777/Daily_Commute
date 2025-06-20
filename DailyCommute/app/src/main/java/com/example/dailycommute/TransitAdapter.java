package com.example.dailycommute;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransitAdapter extends RecyclerView.Adapter<TransitAdapter.TransitViewHolder> {
    private List<TransitDetail> transitList;

    public TransitAdapter(List<TransitDetail> transitList) {
        this.transitList = transitList;
    }

    @NonNull
    @Override
    public TransitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transit_info, parent, false);
        return new TransitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransitViewHolder holder, int position) {
        TransitDetail transitDetail = transitList.get(position);
        holder.vehicleTypeTextView.setText(transitDetail.getVehicleType());
        holder.lineNameTextView.setText(transitDetail.getLineName());
        holder.departureTimeTextView.setText(transitDetail.getDepartureTime());
        holder.arrivalTimeTextView.setText(transitDetail.getArrivalTime());
        holder.departureStopTextView.setText(transitDetail.getDepartureStop());
        holder.arrivalStopTextView.setText(transitDetail.getArrivalStop());
    }

    @Override
    public int getItemCount() {
        return transitList.size();
    }

    public static class TransitViewHolder extends RecyclerView.ViewHolder {
        TextView vehicleTypeTextView, lineNameTextView, departureTimeTextView, arrivalTimeTextView, departureStopTextView, arrivalStopTextView;

        public TransitViewHolder(@NonNull View itemView) {
            super(itemView);
            vehicleTypeTextView = itemView.findViewById(R.id.vehicleTypeTextView);
            lineNameTextView = itemView.findViewById(R.id.lineNameTextView);
            departureTimeTextView = itemView.findViewById(R.id.departureTimeTextView);
            arrivalTimeTextView = itemView.findViewById(R.id.arrivalTimeTextView);
            departureStopTextView = itemView.findViewById(R.id.departureStopTextView);
            arrivalStopTextView = itemView.findViewById(R.id.arrivalStopTextView);
        }
    }
}
