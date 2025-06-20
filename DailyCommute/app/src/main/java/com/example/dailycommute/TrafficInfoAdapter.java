package com.example.dailycommute;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TrafficInfoAdapter extends RecyclerView.Adapter<TrafficInfoAdapter.TrafficViewHolder> {
    private List<TrafficInfo> trafficList;

    public TrafficInfoAdapter(List<TrafficInfo> trafficList) {
        this.trafficList = trafficList;
    }

    @NonNull
    @Override
    public TrafficViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_traffic_info, parent, false);
        return new TrafficViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrafficViewHolder holder, int position) {
        TrafficInfo trafficInfo = trafficList.get(position);
        holder.modeTextView.setText(trafficInfo.getMode());
        holder.trafficStatusTextView.setText(trafficInfo.getTrafficStatus());
        holder.timeTextView.setText(trafficInfo.getTime());
        holder.distanceTextView.setText(trafficInfo.getDistance());
        holder.startTextView.setText(trafficInfo.getStartAddress());
        holder.endTextView.setText(trafficInfo.getEndAddress());
    }

    @Override
    public int getItemCount() {
        return trafficList.size();
    }

    public static class TrafficViewHolder extends RecyclerView.ViewHolder {
        TextView modeTextView, trafficStatusTextView, timeTextView, distanceTextView, startTextView, endTextView;

        public TrafficViewHolder(@NonNull View itemView) {
            super(itemView);
            modeTextView = itemView.findViewById(R.id.modeTextView);
            trafficStatusTextView = itemView.findViewById(R.id.trafficStatusTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            distanceTextView = itemView.findViewById(R.id.distanceTextView);
            startTextView = itemView.findViewById(R.id.startTextView);
            endTextView = itemView.findViewById(R.id.endTextView);
        }
    }
}
