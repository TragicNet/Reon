package com.example.reon.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.reon.classes.Room;
import com.example.reon.databinding.ActivityRoomEditBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomEditActivity extends BaseActivity {

    private ActivityRoomEditBinding binding;
    private boolean newRoom;

    String roomId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        newRoom = !intent.hasExtra("roomId");
        if (!newRoom) {
            roomId = intent.getStringExtra("roomId");
            init( intent.getStringExtra("roomName"), true);
        } else {
            init( "New Room", true);
        }

        DatabaseReference userRef = app.getDatabase().getReference("rooms").child(roomId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                binding.editRoomName.setText((String) dataSnapshot.child("name").getValue());
                binding.editRoomDescription.setText((String) dataSnapshot.child("description").getValue());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });


        binding.buttonUpdateRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.editRoomName.getText().toString();
                String description = binding.editRoomDescription.getText().toString();

                // Toast if empty Name
                if (name.equals("")) {
                    Toast.makeText(getApplicationContext(), "Enter a Valid Name", Toast.LENGTH_SHORT).show();
                } else {
                    if (newRoom)
                        createNewRoom();
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("name", name);
                    userMap.put("description", description);
                    userRef.updateChildren(userMap);
                    if (newRoom) {
                        Intent intent = new Intent(getApplicationContext(), RoomActivity.class);
                        intent.putExtra("roomId", roomId);
                        intent.putExtra("roomName", name);
                        startActivity(intent);
                        finish();
                    }
                    finish();
                }
            }
        });

        binding.editRoomName.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

    }

    private void createNewRoom() {
        Log.d(TAG, "Add Room");
        ArrayList<String> allRooms = new ArrayList<>();
        DatabaseReference roomsRef = app.getDatabase().getReference("rooms");
        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren())
                    allRooms.add(ds.getKey());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        String roomKey;
        roomKey = roomsRef.push().getKey();
//                do {
//                    roomKey = UUID.randomUUID().toString();
//                } while(allRooms.contains(roomKey));

        ArrayList<String> adminList = new ArrayList<>();
        ArrayList<String> memberList = new ArrayList<>();
        roomId = roomKey;
        Log.d(TAG, "roomKey: " + roomKey);
        String userId = app.getCurrentUser().getUid();
        adminList.add(userId);
        memberList.add(userId);
        Room room = new Room(roomKey, app.dateTimeFormat.format(Calendar.getInstance().getTime()), "", "", adminList, memberList);
        //Toast.makeText(getApplicationContext(), "Room Created...\n" + room.getId(), Toast.LENGTH_SHORT).show();
        assert roomKey != null;
        roomsRef.child(roomKey).setValue(room);

        DatabaseReference userRoomsRef = app.getDatabase().getReference("users").child(app.getCurrentUser().getUid()).child("rooms");
        String finalUniqueId = roomKey;
        userRoomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> userRooms = new ArrayList<>();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    userRooms.add((String) ds.getValue());
                }
                userRooms.add(finalUniqueId);
                userRoomsRef.setValue(userRooms);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });
    }
}