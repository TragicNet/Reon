package com.example.reon.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.example.reon.R;
import com.example.reon.adapters.RoomListAdapter;
import com.example.reon.classes.Room;
import com.example.reon.databinding.ActivityDashboardBinding;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class DashboardActivity extends BaseActivity implements RoomListAdapter.OnRoomListener {

    private ActivityDashboardBinding binding;
    private FirebaseUser firebaseUser;

    ArrayList<String> roomIds = new ArrayList<>();
    ArrayList<Room> rooms = new ArrayList<>();
    private RecyclerView roomList;
    private Adapter roomListAdapter;
    private RecyclerView.LayoutManager roomListLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();

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

        DatabaseReference userRoomsRef = app.getDatabase().getReference("users").child(app.getCurrentUser().getUid()).child("rooms");
        userRoomsRef.addChildEventListener(userRoomsListener);

        DatabaseReference allRoomsRef = app.getDatabase().getReference("rooms");
        allRoomsRef.addChildEventListener(allRoomsListener);
    }

    ChildEventListener userRoomsListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            if(snapshot.exists()) {
                for(DataSnapshot ds : snapshot.getChildren()) {
                    roomIds.add((String) ds.getValue());
                }
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.d(TAG, databaseError.getMessage());
        }
    };

    private ChildEventListener allRoomsListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            if(snapshot.exists()) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if(roomIds.contains(ds.getKey())) {
                        //Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        rooms.add(ds.getValue(Room.class));
                    }
                }
                roomListAdapter = new RoomListAdapter(getApplicationContext(), rooms, DashboardActivity.this);
                roomList.setAdapter(roomListAdapter);
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.d(TAG, databaseError.getMessage());
        }
    };

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