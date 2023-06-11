package com.example.reon.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.reon.R;
import com.example.reon.Reon;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

public abstract class BaseActivity extends AppCompatActivity {

    protected final String TAG;

    protected NavController navController;
    protected NavHostFragment navHostFragment;

    public BaseActivity() {
        this.TAG = ("reon_" + this.getClass().getSimpleName());
    }

    public Reon getApp() {
        return (Reon) this.getApplication();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    public DatabaseReference getDatabaseReference() {
        return getApp().getDatabase().getReference();
    }

    public DatabaseReference getDatabaseReference(String path) {
        return getApp().getDatabase().getReference(path);
    }

    public StorageReference getStorageReference() {
        return getApp().getStorage().getReference();
    }

    public StorageReference getStorageReference(String path) {
        return getApp().getStorage().getReference(path);
    }

    public FirebaseUser getCurrentUser() {
        return getApp().getCurrentUser();
    }

    public void init() {
        init("Reon", false);
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

    public void storagePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "no permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    public NavController getNavController() {
        return this.navController;
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "Navigate Up");
        return navController.popBackStack() || super.onSupportNavigateUp();
    }

}