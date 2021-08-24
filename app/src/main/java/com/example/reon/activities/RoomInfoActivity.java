package com.example.reon.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.reon.databinding.ActivityRoomInfoBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class RoomInfoActivity extends BaseActivity {

    private ActivityRoomInfoBinding binding;

    // temp
    String roomId = "",
            roomName = "",
            roomDescription = "";

    ActivityResultLauncher<Intent> roomEditActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Intent data = result.getData();
                    if (data != null) {
                        roomName = data.getStringExtra("name");
                        Objects.requireNonNull(getSupportActionBar()).setTitle(roomName);
                        binding.textRoomName.setText(roomName);
                        binding.textRoomDescription.setText(data.getStringExtra("description"));
                    }
                }
            });

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("name", roomName);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomInfoBinding.inflate(getLayoutInflater());
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
                Objects.requireNonNull(getSupportActionBar()).setTitle((String) dataSnapshot.child("name").getValue());
                binding.textRoomName.setText((String) dataSnapshot.child("name").getValue());
                binding.textRoomDescription.setText((String) dataSnapshot.child("description").getValue());
                roomName = (String) binding.textRoomName.getText();
                roomDescription = (String) binding.textRoomDescription.getText();
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
                roomEditActivityResultLauncher.launch(intent);
            }
        });
    }
}