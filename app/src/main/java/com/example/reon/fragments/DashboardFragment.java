package com.example.reon.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reon.R;
import com.example.reon.adapters.RoomListAdapter;
import com.example.reon.classes.Room;
import com.example.reon.databinding.FragmentDashboardBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardFragment extends BaseFragment implements RoomListAdapter.OnRoomListener {

    private DatabaseReference userRoomsRef, allRoomsRef;

    ArrayList<String> roomIds = new ArrayList<>();
    ArrayList<Room> rooms = new ArrayList<>();
    private RoomListAdapter roomListAdapter;
    private FragmentDashboardBinding binding;


    public DashboardFragment() {
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
        binding = FragmentDashboardBinding.inflate(inflater, container, false);

        Log.d(TAG, "creating dashboard");

        if(getCurrentUser() != null) {
            userRoomsRef = getDatabaseReference("users").child(getCurrentUser().getUid()).child("roomList");
            allRoomsRef = getDatabaseReference("rooms");
            initializeRecyclerView();
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init();

        binding.buttonCreateRoom.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_dashboardFragment_to_roomCreateFragment);
        });

        getMainActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_dashboard, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.menuItem_profile) {
                    getNavController().navigate(R.id.action_dashboardFragment_to_profileFragment);
                } else if (itemId == R.id.menuItem_settings) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/file/d/1bGps4tlbuYScIynLmEA1U4EpJA7VrfwG/view?usp=sharing")));
                    // Check for Updates
                }
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

    }

    private void initializeRecyclerView() {
        RecyclerView roomList = binding.recyclerRoomList;
        roomList.setNestedScrollingEnabled(false);
        roomList.setHasFixedSize(false);
        RecyclerView.LayoutManager roomListLayoutManager = new GridLayoutManager(getMainActivity(), 2, RecyclerView.VERTICAL, false);
        roomList.setLayoutManager(roomListLayoutManager);
        roomListAdapter = new RoomListAdapter(getMainActivity(), rooms, this);
        roomList.setAdapter(roomListAdapter);

        userRoomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                roomIds.clear();
                if(snapshot.exists()) {
                    for(DataSnapshot ds : snapshot.getChildren()) {
                        roomIds.add((String) ds.getValue());
                    }

                    allRoomsRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            rooms.clear();
                            if(snapshot.exists()) {
                                if(!roomIds.isEmpty()) {
                                    for (DataSnapshot ds : snapshot.getChildren()) {
                                        if (roomIds.contains(ds.getKey())) {
                                            //Map<String, Object> map = (Map<String, Object>) ds.getValue();
                                            rooms.add(ds.getValue(Room.class));
                                        }
                                    }
                                }
                            }
                            roomListAdapter.setRooms(rooms);
                            roomListAdapter.notifyDataSetChanged();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d(TAG, error.getMessage());
                        }
                    });
                } else {
                    rooms.clear();
                    roomListAdapter.setRooms(rooms);
                    roomListAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        });
    }

    @Override
    public void onRoomClick(int position) {
        // Setup bundle
        Bundle bundle = new Bundle();
        bundle.putString("roomId", rooms.get(position).getId());
        getMainActivity().getNavController().navigate(R.id.action_dashboardFragment_to_roomFragment, bundle);
    }

}