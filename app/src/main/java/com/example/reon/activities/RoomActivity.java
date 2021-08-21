package com.example.reon.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.reon.R;
import com.example.reon.databinding.ActivityProfileBinding;
import com.example.reon.databinding.ActivityRoomBinding;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class RoomActivity extends BaseActivity {

    private ActivityRoomBinding binding;

    // temp
    String roomId = "",
            roomName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        if(intent.hasExtra("roomId")) {
            roomId = intent.getStringExtra("roomId");
            roomName = intent.getStringExtra("roomName");
        }

        init( roomName, true);

        DatabaseReference userRef = app.getDatabase().getReference("rooms").child(roomId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                binding.textRoomName.setText((String) dataSnapshot.child("name").getValue());
                binding.textRoomDescription.setText((String) dataSnapshot.child("description").getValue());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });

        binding.buttonEditRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), RoomEditActivity.class);
                intent.putExtra("roomId", roomId);
                intent.putExtra("roomName", roomName);
                startActivity(intent);

            }
        });

    }
}