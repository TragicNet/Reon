package com.example.reon.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.reon.R;
import com.example.reon.databinding.ActivityMainBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(binding.navHostFragment.getId());
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
                    Log.d(TAG, "roomid: " + roomId);
                    Log.d(TAG, "folderid: " + folderId);

                    openRoom(roomId, folderId);
                }
            }
        });
    }

    private void openRoom(String roomId, String folderId) {
        DatabaseReference roomRef = getDatabaseReference("rooms").child(roomId);
        DatabaseReference userRoomsRef = getDatabaseReference("users").child(getCurrentUser().getUid()).child("roomList");

        userRoomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> roomIds = new ArrayList<>();
                for(DataSnapshot ds : snapshot.getChildren()) {
                    roomIds.add((String) ds.getValue());
                }

                if(!roomIds.contains(roomId)) {
                    roomIds.add(roomId);
                    userRoomsRef.setValue(roomIds);
                }

                roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> members = new ArrayList<>();
                        for(DataSnapshot ds : snapshot.child("memberList").getChildren()) {
                            members.add((String) ds.getValue());
                        }
                        if(!members.contains(getApp().getCurrentUser().getUid())) {
                            members.add(getCurrentUser().getUid());
                            roomRef.child("memberList").setValue(members);
                        }

                        Bundle bundle = new Bundle();
                        bundle.putString("roomId", roomId);
                        bundle.putString("folderId", folderId);
                        getNavController().navigate(R.id.roomFragment, bundle, new NavOptions.Builder().setPopUpTo(R.id.dashboardFragment, false).build());

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, error.getMessage());
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

        return navController.navigateUp() || super.onSupportNavigateUp();
    }

}