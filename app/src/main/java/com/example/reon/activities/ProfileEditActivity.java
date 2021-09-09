package com.example.reon.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
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
import java.util.Objects;

public class ProfileEditActivity extends BaseActivity {

    private ActivityProfileEditBinding binding;
    private boolean newUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        newUser = intent.hasExtra("newUser");
        init("Profile", !newUser);

        DatabaseReference userRef = app.getDatabase().getReference("users").child(app.getCurrentUser().getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                binding.editUserName.setText((String) dataSnapshot.child("name").getValue());
                binding.editUserAbout.setText((String) dataSnapshot.child("about").getValue());
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
                    Toast.makeText(getApplicationContext(), "Enter a Valid Name", Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("name", name);
                    userMap.put("about", about);
                    userRef.updateChildren(userMap);
                    if(newUser) {
                        startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                        finish();
                    }
                    Intent intent = new Intent();
                    intent.putExtra("name", name);
                    intent.putExtra("about", about);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        if(binding.editUserName.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

    }
}