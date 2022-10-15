package com.example.reon.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.Toast;

import com.example.reon.R;
import com.example.reon.adapters.FolderListAdapter;
import com.example.reon.classes.AlertDialogBuilder;
import com.example.reon.classes.Folder;
import com.example.reon.databinding.ActivityRoomBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class RoomActivity extends BaseActivity implements FolderListAdapter.OnFolderListener {

    private ActivityRoomBinding binding;

    private DatabaseReference roomFoldersRef, allFoldersRef;

    String roomId = "",
            roomName = "";

    ArrayList<String> folderIds = new ArrayList<>();
    ArrayList<String> adminIds = new ArrayList<>();
    ArrayList<Folder> folders = new ArrayList<>();
    private RecyclerView folderList;
    private FolderListAdapter folderListAdapter;
    private RecyclerView.LayoutManager folderListLayoutManager;

    ActivityResultLauncher<Intent> roomInfoActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Intent data = result.getData();
                    if (data != null) {
                        roomName = data.getStringExtra("name");
                        Objects.requireNonNull(getSupportActionBar()).setTitle(roomName);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        if(intent.hasExtra("roomId")) {
            roomId = intent.getStringExtra("roomId");
            roomName = intent.getStringExtra("roomName");
        }

        init( roomName, true);

        roomFoldersRef = app.getDatabase().getReference("rooms").child(roomId).child("folderList");
        allFoldersRef = app.getDatabase().getReference("folders");

        initializeAdminList();

        initailizeRecyclerView();

        binding.buttonCreateFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewFolder();
            }
        });

    }

    private void initializeAdminList() {

        DatabaseReference roomAdminsRef = app.getDatabase().getReference("rooms").child(roomId).child("adminList");
        roomAdminsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    adminIds.clear();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        adminIds.add((String) ds.getValue());
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        });
    }

    private void initailizeRecyclerView() {
        folderList = binding.recyclerFolderList;
        folderList.setNestedScrollingEnabled(false);
        folderList.setHasFixedSize(false);
        folderListLayoutManager = new GridLayoutManager(getApplicationContext(), 2, RecyclerView.VERTICAL, false);
        folderList.setLayoutManager(folderListLayoutManager);
        folderListAdapter = new FolderListAdapter(getApplicationContext(), folders, RoomActivity.this);
        folderList.setAdapter(folderListAdapter);

        roomFoldersRef.addValueEventListener(roomFoldersListener);
    }

    ValueEventListener roomFoldersListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists()) {
                folderIds.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    folderIds.add((String) ds.getValue());
                }
                allFoldersRef.addListenerForSingleValueEvent(allFoldersListener);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d(TAG, error.getMessage());
        }
    };
    
    ValueEventListener allFoldersListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists() && !folderIds.isEmpty()) {
                folders.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    if (folderIds.contains(ds.getKey())) {
                        folders.add(ds.getValue(Folder.class));
                    }
                }
                folderListAdapter.setFolders(folders);
                folderListAdapter.notifyDataSetChanged();
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d(TAG, error.getMessage());
        }
    };
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menuItem_roomInfo) {
            Intent intent = new Intent(getApplicationContext(), RoomInfoActivity.class);
            intent.putExtra("roomId", roomId);
            intent.putExtra("roomName", roomName);
            roomInfoActivityResultLauncher.launch(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFolderClick(int position) {
        Intent intent = new Intent(getApplicationContext(), FolderActivity.class);
        intent.putExtra("roomId", roomId);
        intent.putExtra("folderId", folders.get(position).getId());
        intent.putExtra("folderName", folders.get(position).getName());
        startActivity(intent);
    }

    @Override
    public void onFolderLongClick(int position) {
        Folder folder = folders.get(position);
        if (isAdmin()) {
            AlertDialogBuilder builder = new AlertDialogBuilder(RoomActivity.this);

            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setTitle("Delete?");
            builder.setMessage("Are you sure you want to delete this folder?");
            builder.setCancelable(true);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG, "deleting folder: " + folders.get(position).getName());

                    roomFoldersRef.removeEventListener(roomFoldersListener);
                    
                    allFoldersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild(folder.getId())) {
                                allFoldersRef.child(folder.getId()).removeValue();
                            }
                            roomFoldersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    StorageReference uploadsRef = app.getStorage().getReference().child("uploads");

                                    DatabaseReference allFilesRef = app.getDatabase().getReference().child("files");
                                    allFilesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            Log.d(TAG, "deleting files: " + folder.getFilesList());
                                            if (folder.getFilesList() != null) {
                                                for (DataSnapshot ds : snapshot.getChildren()) {
                                                    if (folder.getFilesList().contains(ds.getKey())) {
                                                        Log.d(TAG, "found: " + ds.getKey());
                                                        allFilesRef.child(Objects.requireNonNull(ds.getKey())).removeValue();
                                                        uploadsRef.child(ds.getKey()).delete();

                                                        /*
                                                            CHECK
                                                         */

                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.e(TAG, error.getMessage());
                                        }
                                    });

                                    folderIds.remove(position);
                                    roomFoldersRef.setValue(folderIds);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, error.getMessage());
                                }
                            });

                            roomFoldersRef.addValueEventListener(roomFoldersListener);

                            Toast.makeText(getApplicationContext(), "Folder Deleted", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, error.getMessage());
                        }
                    });
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            AlertDialog deleteFolderDialog = builder.create();
            deleteFolderDialog.show();
        }
    }

    private boolean isAdmin() {
        return adminIds.contains(app.getCurrentUser().getUid());
    }

    private void createNewFolder() {
        LayoutInflater inflater = LayoutInflater.from(RoomActivity.this);
        View folderNameView = inflater.inflate(R.layout.alert_rename_folder, null);

        // Need current activity context
        AlertDialogBuilder builder = new AlertDialogBuilder(RoomActivity.this);
        builder.setView(folderNameView);

        final EditText editFolderName = folderNameView.findViewById(R.id.edit_folder_name);

        builder.setTitle("New Folder");
        builder.setCancelable(true);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String folderName = editFolderName.getText().toString();
                String folderKey = allFoldersRef.push().getKey();

                ArrayList<String> filesList = new ArrayList<>();
                String created_by = app.getCurrentUser().getUid();
                Folder folder = new Folder(folderKey, app.dateTimeFormat.format(Calendar.getInstance().getTime()), folderName, created_by, filesList);
                assert folderKey != null;
                allFoldersRef.child(folderKey).setValue(folder);

                folders.add(folder);

                DatabaseReference roomFoldersRef = app.getDatabase().getReference("rooms").child(roomId).child("folderList");
                roomFoldersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<String> roomFolders = new ArrayList<>();
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            roomFolders.add((String) ds.getValue());
                        }
                        roomFolders.add(folderKey);
                        roomFoldersRef.setValue(roomFolders);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG, databaseError.getMessage());
                    }
                });
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