package com.example.reon.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.example.reon.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ProfileActivity extends BaseActivity {

    private ActivityProfileBinding binding;
    private FirebaseUser firebaseUser;

    ActivityResultLauncher<Intent> profileEditActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Intent data = result.getData();
                    Log.d(TAG, "resultCode: " + result.getResultCode());
                    if (data != null) {
                        Log.d(TAG, "onActivityResult: " + data);
                        binding.textUserName.setText(data.getStringExtra("name"));
                        binding.textUserAbout.setText(data.getStringExtra("about"));
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init("Profile", true);

        checkUser();

        String email = firebaseUser.getEmail();
        binding.nameView.setText(email);

        DatabaseReference userRef = app.getDatabase().getReference("users").child(app.getCurrentUser().getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                binding.textUserName.setText((String) dataSnapshot.child("name").getValue());
                binding.textUserAbout.setText((String) dataSnapshot.child("about").getValue());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });

        Log.d(TAG, "usename: " + userRef.child("name"));

        // handle logout
        binding.buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                app.getAuth().signOut();
                app.getGoogleSignInClient().signOut();
                checkUser();
            }
        });

        binding.buttonEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileEditActivityResultLauncher.launch(new Intent(getApplicationContext(), ProfileEditActivity.class));
            }
        });

    }

    private void checkUser() {
        // get current user
        firebaseUser = app.getCurrentUser();
        if(firebaseUser == null) {
            // user is not logged in
            startActivity(new Intent(getApplicationContext(), LaunchActivity.class));
            finishAffinity();
            /* Second method to clear all activities
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
             */
        }
    }
}