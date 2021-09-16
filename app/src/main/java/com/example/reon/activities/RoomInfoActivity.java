package com.example.reon.activities;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.example.reon.R;
import com.example.reon.classes.AlertDialogBuilder;
import com.example.reon.classes.Folder;
import com.example.reon.classes.Room;
import com.example.reon.databinding.ActivityRoomInfoBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Objects;

public class RoomInfoActivity extends BaseActivity {

    private ActivityRoomInfoBinding binding;

    private ArrayList<String> adminIds = new ArrayList<>();

    // temp
    String roomId = "",
            roomName = "",
            roomDescription = "",
            roomLink = "";

    DatabaseReference roomRef;

    Room room;

    ActivityResultLauncher<Intent> roomEditActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Intent data = result.getData();
                    if (data != null) {
                        roomName = data.getStringExtra("name");
                        Objects.requireNonNull(getSupportActionBar()).setTitle(roomName);
                        binding.textRoomName.setText(roomName);
                        binding.textRoomDescription.setText(data.getStringExtra("description"));
                    }
                }
            });


    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("name", roomName);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        if(intent.hasExtra("roomId")) {
            roomId = intent.getStringExtra("roomId");
            roomName = intent.getStringExtra("roomName");
        }

        init( roomName, true);

        roomRef = app.getDatabase().getReference("rooms").child(roomId);
        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Objects.requireNonNull(getSupportActionBar()).setTitle((String) dataSnapshot.child("name").getValue());
                binding.textRoomName.setText((String) dataSnapshot.child("name").getValue());
                binding.textRoomDescription.setText((String) dataSnapshot.child("description").getValue());
                roomLink = (String) dataSnapshot.child("link").getValue();
                roomName = (String) binding.textRoomName.getText();
                roomDescription = (String) binding.textRoomDescription.getText();
                binding.textRoomDescription.setMovementMethod(new ScrollingMovementMethod());
                for (DataSnapshot ds : dataSnapshot.child("adminList").getChildren()) {
                    if (app.getCurrentUser().getUid().equals(ds.getValue())) {
                        binding.buttonLeaveRoom.setText(R.string.delete_room);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });

        initializeAdminList();

        binding.buttonCopyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(roomName, roomLink);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(RoomInfoActivity.this, "Copied link to Clipboard!", Toast.LENGTH_SHORT).show();
            }
        });

        binding.buttonLeaveRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAdmin()) {
                    AlertDialogBuilder builder = new AlertDialogBuilder(RoomInfoActivity.this);

                    builder.setIcon(android.R.drawable.ic_dialog_alert);
                    builder.setTitle("Leave?");
                    builder.setMessage("Are you sure you want to leave this room?");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            DatabaseReference userRoomsRef = app.getDatabase().getReference("users").child(app.getCurrentUser().getUid()).child("roomList");

                            userRoomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    ArrayList<String> roomList = new ArrayList<String>();
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        roomList.add((String) ds.getValue());
                                    }
                                    if(roomList.remove(roomId)) {
                                        userRoomsRef.setValue(roomList);
                                    }

                                    DatabaseReference roomMembersRef = app.getDatabase().getReference("rooms").child("memberList");
                                    roomMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            ArrayList<String> memberList = new ArrayList<String>();
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                memberList.add((String) ds.getValue());
                                            }
                                            if(memberList.remove(app.getCurrentUser().getUid())) {
                                                roomMembersRef.setValue(memberList);
                                            }

                                            DatabaseReference roomAdminsRef = app.getDatabase().getReference("rooms").child("adminList");
                                            roomAdminsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    ArrayList<String> adminList = new ArrayList<String>();
                                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                                        adminList.add((String) ds.getValue());
                                                    }
                                                    if(adminList.remove(app.getCurrentUser().getUid())) {
                                                        roomAdminsRef.setValue(adminList);
                                                    }
                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    Log.e(TAG, error.getMessage());
                                                }
                                            });
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.e(TAG, error.getMessage());
                                        }
                                    });
                                    Toast.makeText(getApplicationContext(), "Left Room", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(RoomInfoActivity.this, DashboardActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
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

                    AlertDialog leaveRoomDialog = builder.create();
                    leaveRoomDialog.show();
                } else {
                    AlertDialogBuilder builder = new AlertDialogBuilder(RoomInfoActivity.this);

                    builder.setIcon(android.R.drawable.ic_dialog_alert);
                    builder.setTitle("Delete?");
                    builder.setMessage("Are you sure you want to delete this room?");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    room = new Room();
                                    room = snapshot.getValue(Room.class);


                                    ArrayList<String> memberList = new ArrayList<>();
                                    memberList = room.getMemberList();
                                    for (String member : memberList) {
                                        DatabaseReference userRoomsRef = app.getDatabase().getReference("users").child(member).child("roomList");
                                        userRoomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                ArrayList<String> roomList = new ArrayList<String>();
                                                for (DataSnapshot ds : snapshot.getChildren()) {
                                                    roomList.add((String) ds.getValue());
                                                }
                                                if(roomList.remove(roomId)) {
                                                    userRoomsRef.setValue(roomList);
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Log.e(TAG, error.getMessage());
                                            }
                                        });
                                    }

                                    ArrayList<String> folderList = new ArrayList<>();
                                    folderList = room.getFolderList();
                                    for (String folder : folderList) {
                                        DatabaseReference folderRef = app.getDatabase().getReference("folders").child(folder);
                                        folderRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                Folder folder = snapshot.getValue(Folder.class);

                                                StorageReference uploadsRef = app.getStorage().getReference().child("uploads");
                                                DatabaseReference allFilesRef = app.getDatabase().getReference().child("files");
                                                allFilesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                                            assert folder != null;
                                                            if (folder.getFilesList().contains(ds.getKey())) {
                                                                allFilesRef.child(Objects.requireNonNull(ds.getKey())).removeValue();
                                                                uploadsRef.child(Objects.requireNonNull(ds.getKey())).delete();
                                                            }
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        Log.e(TAG, error.getMessage());
                                                    }
                                                });
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Log.e(TAG, error.getMessage());
                                            }
                                        });
                                        folderRef.removeValue();
                                    }
                                    DatabaseReference allRoomsRef = app.getDatabase().getReference("rooms");
                                    allRoomsRef.child(roomId).removeValue();
                                    Toast.makeText(getApplicationContext(), "Room Deleted", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(RoomInfoActivity.this, DashboardActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);

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

                    AlertDialog deleteRoomDialog = builder.create();
                    deleteRoomDialog.show();
                }
            }
        });

        binding.buttonEditRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RoomInfoActivity.this, RoomEditActivity.class);
                intent.putExtra("roomId", roomId);
                intent.putExtra("roomName", roomName);
                roomEditActivityResultLauncher.launch(intent);
            }
        });
    }

    private boolean isAdmin() {
        return adminIds.contains(app.getCurrentUser().getUid());
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
}