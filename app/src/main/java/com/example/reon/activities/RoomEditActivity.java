package com.example.reon.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.reon.classes.Room;
import com.example.reon.databinding.ActivityRoomEditBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Constants;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomEditActivity extends BaseActivity {

    private ActivityRoomEditBinding binding;
    private boolean newRoom;
    private Uri dynamicLinkUri;

    String roomId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoomEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        newRoom = !intent.hasExtra("roomId");
        if (!newRoom) {
            roomId = intent.getStringExtra("roomId");
            init( intent.getStringExtra("roomName"), true);
        } else {
            init( "New Room", true);
        }

        DatabaseReference roomRef = app.getDatabase().getReference("rooms").child(roomId);
        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                binding.editRoomName.setText((String) dataSnapshot.child("name").getValue());
                binding.editRoomDescription.setText((String) dataSnapshot.child("description").getValue());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });


        binding.buttonUpdateRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.editRoomName.getText().toString();
                String description = binding.editRoomDescription.getText().toString();

                // Toast if empty Name
                if (name.equals("")) {
                    Toast.makeText(getApplicationContext(), "Enter a Valid Name", Toast.LENGTH_SHORT).show();
                } else {
                    if (newRoom) {
                        createNewRoom(name, description);
                    } else {
                        DatabaseReference roomRef = app.getDatabase().getReference("rooms").child(roomId);
                        Map<String, Object> roomMap = new HashMap<>();
                        roomMap.put("name", name);
                        roomMap.put("description", description);
                        roomRef.updateChildren(roomMap);
                        Intent intent = new Intent();
                        intent.putExtra("name", name);
                        intent.putExtra("description", description);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            }
        });

        if(binding.editRoomName.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

    }

    private void createNewRoom(String name, String description) {
        DatabaseReference roomsRef = app.getDatabase().getReference("rooms");
        roomId = roomsRef.push().getKey();
//                do {
//                    roomKey = UUID.randomUUID().toString();
//                } while(allRooms.contains(roomKey));

        ArrayList<String> adminList = new ArrayList<>();
        ArrayList<String> memberList = new ArrayList<>();
        ArrayList<String> folderList = new ArrayList<>();

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("reon1.page.link")
                .appendQueryParameter("roomid", roomId);

        String link = builder.build().toString();

        Task<ShortDynamicLink> dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(link))
                .setDomainUriPrefix("https://reon1.page.link")
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                .setSocialMetaTagParameters(new DynamicLink.SocialMetaTagParameters.Builder().setTitle(name).build())
                .buildShortDynamicLink().addOnCompleteListener(new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if(task.isSuccessful()) {
                            dynamicLinkUri = task.getResult().getShortLink();
                            Log.d(TAG, "Dynamic Link: " + dynamicLinkUri);

                            String userId = app.getCurrentUser().getUid();
                            adminList.add(userId);
                            memberList.add(userId);
                            Room room = new Room(roomId, app.dateTimeFormat.format(Calendar.getInstance().getTime()),
                                    name, description, dynamicLinkUri.toString(), adminList, memberList, folderList);

                            roomsRef.child(roomId).setValue(room);

                            DatabaseReference userRoomsRef = app.getDatabase().getReference("users").child(app.getCurrentUser().getUid()).child("roomList");
                            userRoomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    List<String> userRooms = new ArrayList<>();
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        userRooms.add((String) ds.getValue());
                                    }
                                    userRooms.add(roomId);
                                    userRoomsRef.setValue(userRooms);

                                    Intent intent = new Intent(RoomEditActivity.this, RoomActivity.class);
                                    intent.putExtra("roomId", roomId);
                                    intent.putExtra("roomName", name);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.d(TAG, databaseError.getMessage());
                                }
                            });
                        } else {
                            Log.d(TAG, task.getException().getMessage());
                        }
                    }
                });

//                .setLink(link)
//                .setDomainUriPrefix(Constants.DYNAMIC_LINK_DOMAIN)
//                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
//                .buildDynamicLink();

    }
}