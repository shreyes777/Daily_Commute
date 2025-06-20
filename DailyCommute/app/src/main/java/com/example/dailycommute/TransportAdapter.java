package com.example.dailycommute;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.List;

public class TransportAdapter extends RecyclerView.Adapter<TransportAdapter.ViewHolder> {
    private List<Transport> transportList;

    public TransportAdapter(List<Transport> transportList) {
        this.transportList = transportList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transport, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transport transport = transportList.get(position);
        holder.name.setText(transport.getName());
        holder.type.setText(transport.getType());
        holder.vicinity.setText(transport.getVicinity());
        Picasso.get().load(transport.getImageUrl()).into(holder.image);
    }

    @Override
    public int getItemCount() { return transportList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, type, vicinity;
        ImageView image;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.transportName);
            type = itemView.findViewById(R.id.transportType);
            vicinity = itemView.findViewById(R.id.transportVicinity);
            image = itemView.findViewById(R.id.transportImage);
        }
    }
}
