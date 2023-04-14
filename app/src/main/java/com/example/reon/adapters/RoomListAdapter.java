package com.example.reon.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reon.R;
import com.example.reon.classes.Room;

import java.util.ArrayList;
import java.util.List;

public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.ViewHolder> implements Filterable {
    private final String TAG = "reon_RoomListAdapter";
    private ArrayList<Room> rooms;
    private ArrayList<Room> allRooms;
    private final Context context;
    private final OnRoomListener listener;

    public RoomListAdapter(Context context, ArrayList<Room> rooms, OnRoomListener listener) {
        this.rooms = rooms;
        this.allRooms = rooms;
        this.context = context;
        this.listener = listener;
    }

    public void setRooms(ArrayList<Room> rooms) {
        this.rooms = rooms;
        this.allRooms = rooms;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_room, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(rooms.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    @Override
    public Filter getFilter() {
        return roomFilter;
    }

    private final Filter roomFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Room> filteredList = new ArrayList<>();
            if (constraint.toString().isEmpty()) {
                filteredList.addAll(allRooms);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Room room : allRooms) {
                    if (room.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(room);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            rooms = (ArrayList<Room>) results.values;
            notifyDataSetChanged();
        }
    };


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView name;
        public ImageView image;

        OnRoomListener onRoomListener;

        public ViewHolder(View view, OnRoomListener onRoomListener) {
            super(view);

            name = view.findViewById(R.id.room_item_name);
            image = view.findViewById(R.id.room_item_image);

            this.onRoomListener = onRoomListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onRoomListener.onRoomClick(getAdapterPosition());
        }
    }



    public interface OnRoomListener {
        void onRoomClick(int position);
    }

}
