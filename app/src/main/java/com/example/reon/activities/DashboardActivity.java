package com.example.reon.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.reon.classes.Room;
import com.example.reon.databinding.ActivityDashboardBinding;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DashboardActivity extends BaseActivity {

    private ActivityDashboardBinding binding;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();

        firebaseUser = app.getCurrentUser();

        binding.nameView.setText(firebaseUser.getEmail());

        binding.buttonAddRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                Log.d(TAG, "roomKey: " + roomKey);
                String userId = firebaseUser.getUid();
                adminList.add(userId);
                memberList.add(userId);
                Room room = new Room(roomKey, Calendar.getInstance().getTime(), "room2", "abcd", adminList, memberList);
                Toast.makeText(DashboardActivity.this, "Room Created...\n" + room.getId(), Toast.LENGTH_SHORT).show();
                assert roomKey != null;
                roomsRef.child(roomKey).setValue(room);

                DatabaseReference userRoomsRef = app.getDatabase().getReference("users").child(firebaseUser.getUid()).child("rooms");
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
        });
    }
}