package com.example.reon.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.reon.R;
import com.example.reon.Reon;

public abstract class BaseActivity extends AppCompatActivity {

    protected Reon app;
    protected final String TAG;

    public BaseActivity() {
        this.TAG = ("reon_" + this.getClass().getSimpleName());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.app = ((Reon) this.getApplication());
    }

    public void init() {
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

}