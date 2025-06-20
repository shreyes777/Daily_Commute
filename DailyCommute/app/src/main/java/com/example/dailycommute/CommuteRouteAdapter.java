package com.example.dailycommute;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.List;

public class CommuteRouteAdapter extends RecyclerView.Adapter<CommuteRouteAdapter.ViewHolder> {

    private List<CommuteRoute> commuteRouteList;
    private Context context;

    // Constructor with corrected parameter type and naming
    public CommuteRouteAdapter(List<CommuteRoute> commuteRouteList, Context context) {
        this.commuteRouteList = commuteRouteList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_commuteroute, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Corrected to use commuteRouteList
        CommuteRoute route = commuteRouteList.get(position);
        holder.mode.setText(route.getMode());
        holder.time.setText(route.getTime());
        holder.distance.setText(route.getDistance());

        // Use Picasso to load image
        Picasso.get().load(route.getImageUrl()).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return commuteRouteList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView mode, time, distance;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mode = itemView.findViewById(R.id.route_mode);
            time = itemView.findViewById(R.id.route_time);
            distance = itemView.findViewById(R.id.route_distance);
            image = itemView.findViewById(R.id.route_image);
        }
    }
}
