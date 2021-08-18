package com.example.reon.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.reon.R;
import com.example.reon.classes.User;
import com.example.reon.databinding.ActivityProfileEditBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileEditActivity extends BaseActivity {

    private ActivityProfileEditBinding binding;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();

        binding.editUserName.setVisibility(View.INVISIBLE);
        binding.editUserAbout.setVisibility(View.INVISIBLE);

        DatabaseReference userRef = app.getDatabase().getReference("users").child(app.getCurrentUser().getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                binding.editUserName.setText((String) dataSnapshot.child("name").getValue());
                binding.editUserAbout.setText((String) dataSnapshot.child("about").getValue());

                binding.editUserName.setVisibility(View.VISIBLE);
                binding.editUserAbout.setVisibility(View.VISIBLE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });

        binding.buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.editUserName.getText().toString();
                String about = binding.editUserAbout.getText().toString();

                // Toast if empty Name
                if (name.equals("")) {
                    Toast.makeText(ProfileEditActivity.this, "Enter a Valid Name", Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("name", name);
                    userMap.put("about", about);
                    userRef.updateChildren(userMap);
                    finish();
                }
            }
        });

    }
}