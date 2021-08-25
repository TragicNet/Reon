package com.example.reon.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.reon.R;
import com.example.reon.Reon;

public abstract class BaseActivity extends AppCompatActivity {

    protected Reon app;
    protected final String TAG;

    public BaseActivity() {
        this.TAG = ("reon_" + this.getClass().getSimpleName());
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.app = ((Reon) this.getApplication());
        // Auto Hide navigation buttons
        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}