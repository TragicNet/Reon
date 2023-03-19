package com.example.reon.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;

import com.example.reon.R;
import com.example.reon.classes.Room;
import com.example.reon.databinding.FragmentRoomCreateBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class RoomCreateFragment extends BaseFragment {

    private FragmentRoomCreateBinding binding;

    private Uri dynamicLinkUri;

    String roomId = "";
    final Fragment thisFragment = this;

    public RoomCreateFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRoomCreateBinding.inflate(inflater, container, false);

        binding.buttonUpdateRoom.setOnClickListener(v -> {
            String roomName = binding.roomName.getText().toString();
            String roomDescription = binding.roomDescription.getText().toString();

            // Toast if empty Name
            if (roomName.equals("")) {
                Toast.makeText(getMainActivity(), "Enter a Valid Name", Toast.LENGTH_SHORT).show();
            } else {
                DatabaseReference roomsRef = getDatabaseReference("rooms");
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

                Log.d(TAG, "link: " + link);

                FirebaseDynamicLinks.getInstance().createDynamicLink()
                        .setLink(Uri.parse(link))
                        .setDomainUriPrefix("https://reon1.page.link")
                        .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                        .setSocialMetaTagParameters(new DynamicLink.SocialMetaTagParameters.Builder().setTitle(roomName).build())
                        .buildShortDynamicLink().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "res: "+ task.getResult());
                                dynamicLinkUri = task.getResult().getShortLink();
                                Log.d(TAG, "Dynamic Link: " + dynamicLinkUri);

                                String userId = getCurrentUser().getUid();
                                adminList.add(userId);
                                memberList.add(userId);
                                Room room = new Room(roomId, getApp().dateTimeFormat.format(Calendar.getInstance().getTime()),
                                        roomName, roomDescription, dynamicLinkUri.toString(), adminList, memberList, folderList);

                                roomsRef.child(roomId).setValue(room);

                                DatabaseReference userRoomsRef = getDatabaseReference("users").child(getCurrentUser().getUid()).child("roomList");
                                userRoomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        List<String> userRooms = new ArrayList<>();
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            userRooms.add((String) ds.getValue());
                                        }
                                        userRooms.add(roomId);
                                        userRoomsRef.setValue(userRooms);

                                        Bundle bundle = new Bundle();
                                        bundle.putString("roomId", roomId);
//                                        bundle.putString("roomName", roomName);

                                        // go to roomFragment and pop up to roomCreate
                                        getNavController().navigate(R.id.roomFragment, bundle, new NavOptions.Builder().setPopUpTo(R.id.roomCreateFragment, true).build());
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.d(TAG, databaseError.getMessage());
                                    }
                                });
                            } else {
                                Log.d(TAG, Objects.requireNonNull(task.getException()).getMessage());
                            }
                        });

//                .setLink(link)
//                .setDomainUriPrefix(Constants.DYNAMIC_LINK_DOMAIN)
//                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
//                .buildDynamicLink();

            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init( "New Room", true);

        binding.roomName.requestFocus();
        binding.roomName.postDelayed(() -> {
            InputMethodManager keyboard=(InputMethodManager) getMainActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(binding.roomName,0);
        }, 200);

    }

}