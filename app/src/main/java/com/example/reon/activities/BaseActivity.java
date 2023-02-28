package com.example.reon.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.reon.R;
import com.example.reon.Reon;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

public abstract class BaseActivity extends AppCompatActivity {

    protected Reon app;
    protected final String TAG;

    public BaseActivity() {
        this.TAG = ("reon_" + this.getClass().getSimpleName());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        this.app = ((Reon) this.getApplication());
        if(!this.getClass().equals(SignInActivity.class) && !this.getClass().equals(ProfileEditActivity.class)) {
            Log.d("_reon","Checking user" + this.getClass());
            checkUser();
        }
        // Auto Hide Navigation buttons
//        this.getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public void init(String title, boolean upEnabled) {
        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar toolbar = getSupportActionBar();
        assert toolbar != null;
        toolbar.setDisplayShowTitleEnabled(true);
        toolbar.setDisplayShowHomeEnabled(true);
        toolbar.setTitle(title);
        toolbar.setDisplayHomeAsUpEnabled(upEnabled);
    }

    public void init() {
        init("Reon", false);
    }
    
    private void checkUser() {
        // go to profile if logged
        if (app.getCurrentUser() == null) {
            // go to sign in activity if not signed in
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            finishAffinity();
        } else {
            // get name of the user
            DatabaseReference userRef = app.getDatabase().getReference("users").child(app.getCurrentUser().getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = (String) dataSnapshot.child("name").getValue();
                    //Log.d(TAG, "Username: " + name);
                    // go to sign in activity if no name
                    if(name == null) {
                        Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, databaseError.getMessage());
                }
            });
        }
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}