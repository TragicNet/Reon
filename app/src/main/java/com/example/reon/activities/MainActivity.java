package com.example.reon.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ActionMode;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.reon.R;
import com.example.reon.classes.AlertDialogBuilder;
import com.example.reon.classes.Room;
import com.example.reon.databinding.ActivityMainBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;

    private ArrayList<String> adminIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        // init navController
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(binding.navHostFragment.getId());
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

        init();

        //checkForDynamicLinks(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkForDynamicLinks(intent);
        getNavController().handleDeepLink(intent);
    }

    private void checkForDynamicLinks(Intent intent) {
        if(getCurrentUser() == null)
            return;
        FirebaseDynamicLinks.getInstance().getDynamicLink(intent).addOnSuccessListener(pendingDynamicLinkData -> {
            if (pendingDynamicLinkData != null) {
                Log.d(TAG, "got link");
                Log.d(TAG, "found: " + pendingDynamicLinkData.getLink());
                Uri deepLink = pendingDynamicLinkData.getLink();

                if (deepLink != null) {
                    String roomId = deepLink.getQueryParameter("roomid");
                    String folderId = deepLink.getQueryParameter("folderid");
                    String fileId = deepLink.getQueryParameter("fileid");
                    Log.d(TAG, "roomid: " + roomId);
                    Log.d(TAG, "folderid: " + folderId);
                    Log.d(TAG, "fileid: " + fileId);

                    openRoom(this, roomId, folderId, fileId);
                }
            }
        });
    }

    private void openRoom(Context context, String roomId, String folderId, String fileId) {
        DatabaseReference userRoomsRef = getDatabaseReference("users").child(getCurrentUser().getUid()).child("roomList");

        userRoomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> roomIds = new ArrayList<>();
                for(DataSnapshot ds : snapshot.getChildren()) {
                    roomIds.add((String) ds.getValue());
                }

                if(!roomIds.contains(roomId)) {
                    // Setup dialog for joining room
                    DatabaseReference roomRef = getDatabaseReference("rooms").child(roomId);
                    roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Room room = snapshot.getValue(Room.class);
                            Log.d(TAG, "Room: " + room);

                            AlertDialogBuilder builder = new AlertDialogBuilder(context);
                            builder.setIcon(android.R.drawable.ic_dialog_info);
                            builder.setTitle("Enter Room?");

                            assert room != null;
                            if (fileId != null)
                                builder.setMessage("File in Room: " + room.getName() + "\nDo you want to enter?");
                            else if (folderId != null)
                                builder.setMessage("Folder in Room: " + room.getName() + "\nDo you want to enter?");
                            else
                                builder.setMessage("Do you want to enter room: " + room.getName() + "?");


                            builder.setCancelable(true);
                            builder.setPositiveButton("Yes", (dialog, which) -> {

                                roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        // Add User to the Room
                                        roomIds.add(roomId);
                                        userRoomsRef.setValue(roomIds);

                                        ArrayList<String> members = new ArrayList<>();
                                        for(DataSnapshot ds : snapshot.child("memberList").getChildren()) {
                                            members.add((String) ds.getValue());
                                        }
                                        if(!members.contains(getApp().getCurrentUser().getUid())) {
                                            members.add(getCurrentUser().getUid());
                                            roomRef.child("memberList").setValue(members);
                                        }

                                        // Navigate to Room Fragment
                                        Bundle bundle = new Bundle();
                                        bundle.putString("roomId", roomId);
                                        bundle.putString("folderId", folderId);
                                        bundle.putString("fileId", fileId);
                                        getNavController().navigate(R.id.roomFragment, bundle, new NavOptions.Builder().setPopUpTo(R.id.dashboardFragment, false).build());
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e(TAG, error.getMessage());
                                    }
                                });
                            });
                            builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());

                            AlertDialog joinRoomDialog = builder.create();
                            joinRoomDialog.show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("roomId", roomId);
                    bundle.putString("folderId", folderId);
                    bundle.putString("fileId", fileId);
                    getNavController().navigate(R.id.roomFragment, bundle, new NavOptions.Builder().setPopUpTo(R.id.dashboardFragment, false).build());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        });

    }

    public ArrayList<String> getAdminIds() {
        return adminIds;
    }

    public void setAdminIds(ArrayList<String> adminIds) {
        this.adminIds = adminIds;
    }

}