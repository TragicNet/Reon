package com.example.reon.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.reon.Reon;
import com.example.reon.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileActivity extends BaseActivity {

    private static final String TAG = "reon/PROFILE_ACTIVITY";

    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setApp((Reon) this.getApplication());
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar();

        checkUser();

        // handle logout
        binding.logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                app.getAuth().signOut();
                app.getGoogleSignInClient().signOut();
                checkUser();
            }
        });

        binding.dashboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, DashboardActivity.class));
                //finish();
            }
        });

    }

    private void checkUser() {
        // get current user
        FirebaseUser user = app.getAuth().getCurrentUser();
        if(user == null) {
            // user is not logged in
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            // user is logged in
            app.setDatabase(FirebaseDatabase.getInstance());
            // get user info
            String email = user.getEmail();
            binding.nameView.setText(email);
        }
    }
}