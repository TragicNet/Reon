package com.example.reon.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.example.reon.R;
import com.example.reon.classes.AlertDialogBuilder;
import com.example.reon.classes.Folder;
import com.example.reon.databinding.ActivityFolderBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_folder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menuItem_rename) {
            Log.d(TAG, "Pressed rename menuitem");
            renameFolder();
        }

        return super.onOptionsItemSelected(item);
    }

    private void renameFolder() {
        LayoutInflater inflater = LayoutInflater.from(FolderActivity.this);
        View folderNameView = inflater.inflate(R.layout.alert_rename_folder, null);

        // Need current activity context
        AlertDialogBuilder builder = new AlertDialogBuilder(FolderActivity.this);
        builder.setView(folderNameView);

        final EditText editFolderName = folderNameView.findViewById(R.id.edit_folder_name);
        editFolderName.setText(folderName);

        builder.setTitle("Rename Folder");
        builder.setCancelable(true);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String folderName = editFolderName.getText().toString();
                Objects.requireNonNull(getSupportActionBar()).setTitle(folderName);
                DatabaseReference folderRef = app.getDatabase().getReference("folders").child(folderId);
                folderRef.child("name").setValue(folderName);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog folderNameDialog = builder.create();
        folderNameDialog.show();

        if(editFolderName.requestFocus()) {
            folderNameDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

}