package com.example.reon.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.reon.R;
import com.example.reon.Reon;

public abstract class BaseActivity extends AppCompatActivity {

    public Reon app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void initToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void setApp(Reon app) {
        this.app = app;
    }
}