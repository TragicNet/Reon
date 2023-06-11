package com.example.reon.fragments;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Scroller;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reon.R;
import com.example.reon.adapters.FolderListAdapter;
import com.example.reon.adapters.UserListAdapter;
import com.example.reon.classes.AlertDialogBuilder;
import com.example.reon.classes.Folder;
import com.example.reon.classes.Room;
import com.example.reon.classes.User;
import com.example.reon.databinding.FragmentRoomInfoBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RoomInfoFragment extends BaseFragment implements UserListAdapter.OnUserListener {

    private FragmentRoomInfoBinding binding;

    // temp
    String roomId = "",
            roomName = "",
            roomDescription = "",
            roomLink = "";

    DatabaseReference roomRef;
    DatabaseReference usersRef;
    ArrayList<String> memberIds = new ArrayList<>();
    ArrayList<User> members = new ArrayList<>();
    private UserListAdapter userListAdapter;
    private DatabaseReference membersRef;
    private RecyclerView userList;

    public RoomInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        assert bundle != null;
        roomId = bundle.getString("roomId");

        roomRef = getDatabaseReference("rooms").child(roomId);
        usersRef =  getDatabaseReference().child("users");
        membersRef = getDatabaseReference("rooms").child(roomId).child("memberList");


        // Check for updates ValueListener
        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Objects.requireNonNull(getMainActivity().getSupportActionBar()).setTitle((String) dataSnapshot.child("name").getValue());
                binding.textRoomName.setText((String) dataSnapshot.child("name").getValue());
                binding.textRoomDescription.setText((String) dataSnapshot.child("description").getValue());
                roomLink = (String) dataSnapshot.child("link").getValue();
                roomName = (String) binding.textRoomName.getText();
                roomDescription = (String) binding.textRoomDescription.getText();
                binding.textRoomDescription.setMovementMethod(new ScrollingMovementMethod());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRoomInfoBinding.inflate(inflater, container, false);

        initializeRecyclerView();

        binding.nameContainerFrame.setOnClickListener(v -> editName());

        binding.descriptionContainerFrame.setOnClickListener(v -> editDescription());

//        binding.buttonCopyLink.setOnClickListener(v -> {
//            ClipboardManager clipboard = (ClipboardManager) getMainActivity().getSystemService(Context.CLIPBOARD_SERVICE);
//            ClipData clip = ClipData.newPlainText(roomName, roomLink);
//            clipboard.setPrimaryClip(clip);
//            Toast.makeText(getMainActivity(), "Copied link to Clipboard!", Toast.LENGTH_SHORT).show();
//        });

        return binding.getRoot();
    }

    private void initializeRecyclerView() {
        userList = binding.recyclerUserList;
        userList.setNestedScrollingEnabled(false);
        userList.setHasFixedSize(false);
        RecyclerView.LayoutManager userListLayoutManager = new GridLayoutManager(getMainActivity(), 1, RecyclerView.VERTICAL, false);
        userList.setLayoutManager(userListLayoutManager);

        membersRef.addValueEventListener(roomMembersListener);

        userListAdapter = new UserListAdapter(getMainActivity(), members, this);
        userList.setAdapter(userListAdapter);
    }

    ValueEventListener roomMembersListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists()) {
                memberIds.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    memberIds.add((String) ds.getValue());
                }
                usersRef.addListenerForSingleValueEvent(usersListener);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d(TAG, error.getMessage());
        }
    };

    ValueEventListener usersListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists() && !memberIds.isEmpty()) {
                members.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
//                    temp failsafe for id not created
//                    if(!snapshot.child("id").exists()) {
//                        DatabaseReference singleUserRef =  getDatabaseReference("users").child(ds.getKey());
//                        Map<String, Object> userMap = new HashMap<>();
//                        userMap.put("id", ds.getKey());
//                        singleUserRef.updateChildren(userMap);
//                        singleUserRef.child("link").removeValue();
//                    }
                    if (memberIds.contains(ds.getKey())) {
                        User member = ds.getValue(User.class);
                        assert member != null;
                        if(isAdmin(member.getId())) {
                            member.setAdmin(true);
                        }
                        members.add(member);
                    }
                }
                userListAdapter.setUsers(members);
                userListAdapter.notifyDataSetChanged();
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d(TAG, error.getMessage());
        }
    };


    private void editName() {
        LayoutInflater inflater = LayoutInflater.from(getMainActivity());
        View roomNameView = inflater.inflate(R.layout.alert_edit_text, null);

        // Need current activity context
        AlertDialogBuilder builder = new AlertDialogBuilder(getMainActivity());
        builder.setView(roomNameView);

        final EditText editRoomName = roomNameView.findViewById(R.id.edit_text_field);
        editRoomName.setText(roomName);
        editRoomName.setHint("Enter Room Name");

        builder.setTitle("Edit Name");
        builder.setCancelable(true);
        builder.setPositiveButton("Ok", (dialog, which) -> {
            String name = editRoomName.getText().toString();
            // Toast if empty Name
            if (name.equals("")) {
                Toast.makeText(getMainActivity(), "Enter a Valid Name", Toast.LENGTH_SHORT).show();
            } else {
                roomName = name;
                init(roomName, true);

                binding.textRoomName.setText(roomName);

                Bundle result = new Bundle();
                result.putString("roomName", roomName);
                getParentFragmentManager().setFragmentResult("editedName", result);

                Map<String, Object> roomMap = new HashMap<>();
                roomMap.put("name", name);
                roomRef.updateChildren(roomMap);
            }

        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog roomNameDialog = builder.create();
        roomNameDialog.show();

        editRoomName.requestFocus();
        editRoomName.postDelayed(() -> {
            InputMethodManager keyboard=(InputMethodManager) getMainActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(editRoomName,0);
        }, 200);

    }

    private void editDescription() {
        LayoutInflater inflater = LayoutInflater.from(getMainActivity());
        View roomDescriptionView = inflater.inflate(R.layout.alert_edit_text, null);

        // Need current activity context
        AlertDialogBuilder builder = new AlertDialogBuilder(getMainActivity());
        builder.setView(roomDescriptionView);

        final EditText editRoomDescription = roomDescriptionView.findViewById(R.id.edit_text_field);
        editRoomDescription.setText(roomDescription);
        editRoomDescription.setHint("Enter Room Description");

        // Setup for multi line
        editRoomDescription.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editRoomDescription.setSingleLine(false);
        editRoomDescription.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);

        editRoomDescription.setScroller(new Scroller(getMainActivity()));
        editRoomDescription.setMaxLines(10);

        editRoomDescription.setVerticalScrollBarEnabled(true);
        editRoomDescription.setMovementMethod(new ScrollingMovementMethod());


        builder.setTitle("Edit Description");
        builder.setCancelable(true);
        builder.setPositiveButton("Ok", (dialog, which) -> {
            String description = editRoomDescription.getText().toString();

            roomDescription = description;
            binding.textRoomDescription.setText(roomDescription);

            Map<String, Object> roomMap = new HashMap<>();
            roomMap.put("description", description);
            roomRef.updateChildren(roomMap);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog roomDescriptionDialog = builder.create();
        roomDescriptionDialog.show();

        editRoomDescription.requestFocus();
        editRoomDescription.postDelayed(() -> {
            InputMethodManager keyboard=(InputMethodManager) getMainActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(editRoomDescription,0);
        }, 200);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init( roomName, true);

        getMainActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_room_info, menu);
                menu.findItem(R.id.menuItem_leave_room).setVisible(!(isAdmin() && getMainActivity().getAdminIds().size() == 1));
                Log.d(TAG, "Admin: " + isAdmin());
                menu.findItem(R.id.menuItem_delete_room).setVisible(isAdmin());
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.menuItem_leave_room) {
                    AlertDialogBuilder builder = new AlertDialogBuilder(getMainActivity());

                    builder.setIcon(android.R.drawable.ic_dialog_alert);
                    builder.setTitle("Leave?");
                    builder.setMessage("Are you sure you want to leave this room?");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Yes", (dialog, which) -> {

                        DatabaseReference userRoomsRef = getDatabaseReference("users").child(getCurrentUser().getUid()).child("roomList");
                        userRoomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                ArrayList<String> roomList = new ArrayList<>();
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    roomList.add((String) ds.getValue());
                                }
                                if (roomList.remove(roomId)) {
                                    userRoomsRef.setValue(roomList);
                                }

                                DatabaseReference roomMembersRef = getDatabaseReference("rooms").child("memberList");
                                roomMembersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        ArrayList<String> memberList = new ArrayList<>();
                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                            memberList.add((String) ds.getValue());
                                        }
                                        if (memberList.remove(getCurrentUser().getUid())) {
                                            roomMembersRef.setValue(memberList);
                                        }

                                        DatabaseReference roomAdminsRef = getDatabaseReference("rooms").child("adminList");
                                        roomAdminsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                ArrayList<String> adminList = new ArrayList<>();
                                                for (DataSnapshot ds : snapshot.getChildren()) {
                                                    adminList.add((String) ds.getValue());
                                                }
                                                if (adminList.remove(getCurrentUser().getUid())) {
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
                                Toast.makeText(getMainActivity(), "Left Room", Toast.LENGTH_SHORT).show();


                                Navigation.findNavController(view).navigate(R.id.action_roomInfoFragment_to_dashboardFragment);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, error.getMessage());
                            }
                        });
                    });
                    builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());

                    AlertDialog leaveRoomDialog = builder.create();
                    leaveRoomDialog.show();

                } else if (itemId == R.id.menuItem_delete_room) {
                    AlertDialogBuilder builder = new AlertDialogBuilder(getMainActivity());

                    builder.setIcon(android.R.drawable.ic_dialog_alert);
                    builder.setTitle("Delete?");
                    builder.setMessage("Are you sure you want to delete this room?");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Yes", (dialog, which) -> roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Room room = snapshot.getValue(Room.class);

                            ArrayList<String> memberList;
                            assert room != null;
                            memberList = room.getMemberList();
                            if (memberList != null) {
                                for (String member : memberList) {
                                    DatabaseReference userRoomsRef = getDatabaseReference("users").child(member).child("roomList");
                                    userRoomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            ArrayList<String> roomList = new ArrayList<>();
                                            for (DataSnapshot ds : snapshot.getChildren()) {
                                                roomList.add((String) ds.getValue());
                                            }

                                            if (roomList.remove(roomId)) {
                                                Log.d(TAG, "Removed room: " + roomId);
                                                userRoomsRef.setValue(roomList);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.e(TAG, error.getMessage());
                                        }
                                    });
                                }
                            }

                            ArrayList<String> folderList;
                            folderList = room.getFolderList();
                            if (folderList != null) {
                                for (String folder : folderList) {
                                    DatabaseReference folderRef = getDatabaseReference("folders").child(folder);
                                    folderRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            Folder folder = snapshot.getValue(Folder.class);

                                            StorageReference uploadsRef = getStorageReference().child("uploads");
                                            DatabaseReference allFilesRef = getDatabaseReference().child("files");
                                            allFilesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    assert folder != null;
                                                    if (folder.getFilesList() != null) {
                                                        for (DataSnapshot ds : snapshot.getChildren()) {
                                                            if (folder.getFilesList().contains(ds.getKey())) {
                                                                allFilesRef.child(Objects.requireNonNull(ds.getKey())).removeValue();
                                                                uploadsRef.child(Objects.requireNonNull(ds.getKey())).delete();
                                                            }
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
                            }
                            DatabaseReference allRoomsRef = getDatabaseReference("rooms");
                            allRoomsRef.child(roomId).removeValue();
                            Toast.makeText(getMainActivity(), "Room Deleted", Toast.LENGTH_SHORT).show();

                            getNavController().popBackStack(R.id.dashboardFragment, false);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, error.getMessage());
                        }
                    }));
                    builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());

                    AlertDialog deleteRoomDialog = builder.create();
                    deleteRoomDialog.show();
                }
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

    }

    public boolean isAdmin() {
        return getMainActivity().getAdminIds().contains(getCurrentUser().getUid());
    }

    public boolean isAdmin(String memberId) {
        return getMainActivity().getAdminIds().contains(memberId);
    }

    @Override
    public void onUserClick(int position) {
        Log.d(TAG, "Clicked on: " + members.get(position).getId());
    }

    @Override
    public void onUserLongClick(int position) {
        User user = members.get(position);
        if(Objects.equals(user.getId(), getCurrentUser().getUid()))
            return;

        PopupMenu popup = new PopupMenu(Objects.requireNonNull(getMainActivity().getSupportActionBar()).getThemedContext(), Objects.requireNonNull(userList.findViewHolderForAdapterPosition(position)).itemView);
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.menu_room_members, popup.getMenu());

        if(user.isAdmin()) {
            popup.getMenu().findItem(R.id.menuItem_toggle_admin).setTitle("Remove Admin");
        }

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menuItem_toggle_admin) {
//                folderFilesRef.removeEventListener(folderFilesListener);
                DatabaseReference roomRef = getDatabaseReference("rooms").child(roomId);
                roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> admins = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.child("adminList").getChildren()) {
                            admins.add((String) ds.getValue());
                        }
                        if(user.isAdmin()) {
                            admins.remove(user.getId());
                        } else {
                            admins.add(user.getId());
                        }
                        user.setAdmin(!user.isAdmin());
                        roomRef.child("adminList").setValue(admins);
//                        userListAdapter.notifyItemChanged(position);
                        userListAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            } else if (itemId == R.id.menuItem_remove_member) {
                AlertDialogBuilder builder = new AlertDialogBuilder(getMainActivity());
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setTitle("Remove User?");

                builder.setCancelable(true);
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    DatabaseReference userRoomsRef = getDatabaseReference("users").child(user.getId()).child("roomList");

                    userRoomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ArrayList<String> rooms = new ArrayList<>();
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                rooms.add((String) ds.getValue());
                            }
                            rooms.remove(user.getId());
                            userRoomsRef.setValue(rooms);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.d(TAG, error.getMessage());
                        }
                    });

                    DatabaseReference roomRef = getDatabaseReference("rooms").child(roomId);
                    roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ArrayList<String> roomMembers = new ArrayList<>();
                            for (DataSnapshot ds : snapshot.child("memberList").getChildren()) {
                                roomMembers.add((String) ds.getValue());
                            }
                            roomMembers.remove(user.getId());
                            roomRef.child("memberList").setValue(roomMembers);

                            ArrayList<String> admins = new ArrayList<>();
                            for (DataSnapshot ds : snapshot.child("adminList").getChildren()) {
                                admins.add((String) ds.getValue());
                            }
                            admins.remove(user.getId());
                            roomRef.child("adminList").setValue(admins);
//                            userListAdapter.notifyItemChanged(position);
                            userListAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                });
                builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());

                AlertDialog removeMemberDialog = builder.create();
                removeMemberDialog.show();
            }
            return true;
        });

        popup.show();//showing popup menu
    }
}