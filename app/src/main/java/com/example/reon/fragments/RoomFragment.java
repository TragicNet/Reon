package com.example.reon.fragments;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reon.R;
import com.example.reon.adapters.FolderListAdapter;
import com.example.reon.classes.AlertDialogBuilder;
import com.example.reon.classes.Folder;
import com.example.reon.databinding.FragmentRoomBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class RoomFragment extends BaseFragment implements FolderListAdapter.OnFolderListener {


    private FragmentRoomBinding binding;

    private DatabaseReference roomFoldersRef, allFoldersRef;

    String roomId = "",
            roomName = "";

    ArrayList<String> folderIds = new ArrayList<>();
    ArrayList<String> adminIds = new ArrayList<>();
    ArrayList<Folder> folders = new ArrayList<>();
    private FolderListAdapter folderListAdapter;
    private Uri dynamicLinkUri;

    public RoomFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        assert bundle != null;
        roomId = bundle.getString("roomId");
        if(bundle.containsKey("folderId") && bundle.getString("folderId") != null) {
            getNavController().navigate(R.id.action_roomFragment_to_folderFragment, bundle);
        }

        DatabaseReference roomRef = getDatabaseReference("rooms").child(roomId);
        roomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    roomName = (String) snapshot.child("name").getValue();
                    init( roomName, true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        });

        getParentFragmentManager().setFragmentResultListener("editedName", this, (requestKey, bundle1) -> {
            // We use a String here, but any type that can be put in a Bundle is supported
            roomName = bundle1.getString("roomName");
            init(roomName, true);
            // Do something with the result
        });


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRoomBinding.inflate(inflater, container, false);

        roomFoldersRef = getDatabaseReference("rooms").child(roomId).child("folderList");
        allFoldersRef = getDatabaseReference("folders");

        initializeAdminList();

        initializeRecyclerView();

        binding.buttonCreateFolder.setOnClickListener(v -> createNewFolder());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init( roomName, true);
        getMainActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_room, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.menuItem_roomInfo) {

                    Bundle bundle = new Bundle();
                    bundle.putString("roomId", roomId);
                    getNavController().navigate(R.id.action_roomFragment_to_roomInfoFragment, bundle);
                }
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void initializeAdminList() {

        DatabaseReference roomAdminsRef = getDatabaseReference("rooms").child(roomId).child("adminList");
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

    private void initializeRecyclerView() {
        RecyclerView folderList = binding.recyclerFolderList;
        folderList.setNestedScrollingEnabled(false);
        folderList.setHasFixedSize(false);
        RecyclerView.LayoutManager folderListLayoutManager = new GridLayoutManager(getMainActivity(), 2, RecyclerView.VERTICAL, false);
        folderList.setLayoutManager(folderListLayoutManager);
        folderListAdapter = new FolderListAdapter(getMainActivity(), folders, this);
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
    public void onFolderClick(int position) {

        Bundle bundle = new Bundle();
        bundle.putString("roomId", roomId);
        bundle.putString("folderId", folders.get(position).getId());
//        bundle.putString("folderName", folders.get(position).getName());

        getNavController().navigate(R.id.action_roomFragment_to_folderFragment, bundle);
    }

    @Override
    public void onFolderLongClick(int position) {
        Folder folder = folders.get(position);
        if (isAdmin()) {
            AlertDialogBuilder builder = new AlertDialogBuilder(getMainActivity());

            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setTitle("Delete?");
            builder.setMessage("Are you sure you want to delete this folder?");
            builder.setCancelable(true);
            builder.setPositiveButton("Yes", (dialog, which) -> {
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

                                StorageReference uploadsRef = getStorageReference().child("uploads");

                                DatabaseReference allFilesRef = getDatabaseReference().child("files");
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

                        Toast.makeText(getMainActivity(), "Folder Deleted", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, error.getMessage());
                    }
                });
            });
            builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());

            AlertDialog deleteFolderDialog = builder.create();
            deleteFolderDialog.show();
        }
    }

    private boolean isAdmin() {
        return adminIds.contains(getCurrentUser().getUid());
    }

    private void createNewFolder() {
        LayoutInflater inflater = LayoutInflater.from(getMainActivity());
        View folderNameView = inflater.inflate(R.layout.alert_edit_text, null);

        // Need current activity context
        AlertDialogBuilder builder = new AlertDialogBuilder(getMainActivity());
        builder.setView(folderNameView);

        final EditText editFolderName = folderNameView.findViewById(R.id.edit_text_field);

        builder.setTitle("New Folder");
        builder.setCancelable(true);
        builder.setPositiveButton("Ok", (dialog, which) -> {
            String folderName = editFolderName.getText().toString();
            String folderId = allFoldersRef.push().getKey();

            ArrayList<String> filesList = new ArrayList<>();
            String created_by = getCurrentUser().getUid();

            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.scheme("https")
                    .authority("reon1.page.link")
                    .appendQueryParameter("roomid", roomId)
                    .appendQueryParameter("folderid", folderId);

            String link = uriBuilder.build().toString();

            Log.d(TAG, "link: " + link);
            
            FirebaseDynamicLinks.getInstance().createDynamicLink()
                    .setLink(Uri.parse(link))
                    .setDomainUriPrefix("https://reon1.page.link")
                    .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                    .setSocialMetaTagParameters(new DynamicLink.SocialMetaTagParameters.Builder().setTitle(folderName).build())
                    .buildShortDynamicLink().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "res: " + task.getResult());
                            dynamicLinkUri = task.getResult().getShortLink();
                            Log.d(TAG, "Dynamic Link: " + dynamicLinkUri);

                            Folder folder = new Folder(folderId, getApp().dateTimeFormat.format(Calendar.getInstance().getTime()), folderName, dynamicLinkUri.toString(), created_by, filesList);
                            assert folderId != null;
                            allFoldersRef.child(folderId).setValue(folder);

                            folders.add(folder);

                            DatabaseReference roomFoldersRef = getDatabaseReference("rooms").child(roomId).child("folderList");
                            roomFoldersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    List<String> roomFolders = new ArrayList<>();
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        roomFolders.add((String) ds.getValue());
                                    }
                                    roomFolders.add(folderId);
                                    roomFoldersRef.setValue(roomFolders);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.d(TAG, databaseError.getMessage());
                                }
                            });
                        }
                    });
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog folderNameDialog = builder.create();
        folderNameDialog.show();

        if(editFolderName.requestFocus()) {
            folderNameDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

    }

}