package com.example.reon.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.reon.R;
import com.example.reon.databinding.ActivityFolderBinding;

public class FolderActivity extends BaseActivity {

    private ActivityFolderBinding binding;

    String folderId = "",
            folderName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFolderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        if(intent.hasExtra("folderId")) {
            folderId = intent.getStringExtra("folderId");
            folderName = intent.getStringExtra("folderName");
        }

        init( folderName, true);

    }
}