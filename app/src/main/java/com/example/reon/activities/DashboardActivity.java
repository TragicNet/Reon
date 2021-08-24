package com.example.reon.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.example.reon.R;
import com.example.reon.adapters.RoomListAdapter;
import com.example.reon.classes.Room;
import com.example.reon.databinding.ActivityDashboardBinding;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardActivity extends BaseActivity implements RoomListAdapter.OnRoomListener {

    private ActivityDashboardBinding binding;
    private FirebaseUser firebaseUser;

    private DatabaseReference userRoomsRef, allRoomsRef;

    ArrayList<String> roomIds = new ArrayList<>();
    ArrayList<Room> rooms = new ArrayList<>();
    private RecyclerView roomList;
    private RoomListAdapter roomListAdapter;
    private RecyclerView.LayoutManager roomListLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();

        userRoomsRef = app.getDatabase().getReference("users").child(app.getCurrentUser().getUid()).child("roomList");
        allRoomsRef = app.getDatabase().getReference("rooms");

        initailizeRecyclerView();

        binding.buttonCreateRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RoomEditActivity.class));
            }
        });
    }

    private void initailizeRecyclerView() {
        roomList = binding.recyclerRoomList;
        roomList.setNestedScrollingEnabled(false);
        roomList.setHasFixedSize(false);
        roomListLayoutManager = new GridLayoutManager(getApplicationContext(), 2, RecyclerView.VERTICAL, false);
        roomList.setLayoutManager(roomListLayoutManager);
        roomListAdapter = new RoomListAdapter(getApplicationContext(), rooms, DashboardActivity.this);
        roomList.setAdapter(roomListAdapter);

        userRoomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    roomIds.clear();
                    for(DataSnapshot ds : snapshot.getChildren()) {
                        roomIds.add((String) ds.getValue());
                    }

                    allRoomsRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists() && !roomIds.isEmpty()) {
                                rooms.clear();
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    if(roomIds.contains(ds.getKey())) {
                                        //Map<String, Object> map = (Map<String, Object>) ds.getValue();
                                        rooms.add(ds.getValue(Room.class));
                                    }
                                }
                                roomListAdapter.setRooms(rooms);
                                roomListAdapter.notifyDataSetChanged();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d(TAG, error.getMessage());
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menuItem_profile) {
            Log.d(TAG, "Pressed profile menuitem");
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        } else if (itemId == R.id.menuItem_settings) {
            Log.d(TAG, "Pressed settings menuitem");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRoomClick(int position) {
        Intent intent = new Intent(getApplicationContext(), RoomActivity.class);
        intent.putExtra("roomId", rooms.get(position).getId());
        intent.putExtra("roomName", rooms.get(position).getName());
        startActivity(intent);
    }
}