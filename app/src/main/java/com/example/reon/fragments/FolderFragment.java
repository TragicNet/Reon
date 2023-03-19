package com.example.reon.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileObserver;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reon.R;
import com.example.reon.Reon;
import com.example.reon.adapters.FileListAdapter;
import com.example.reon.classes.AlertDialogBuilder;
import com.example.reon.classes.File;
import com.example.reon.databinding.FragmentFolderBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FolderFragment extends BaseFragment implements FileListAdapter.OnFileListener {

    private FragmentFolderBinding binding;

    String folderId = "",
            folderName = "",
            roomId = "";

    DatabaseReference allFilesRef, folderFilesRef;

    ArrayList<String> fileIds = new ArrayList<>();
    ArrayList<String> adminIds = new ArrayList<>();
    ArrayList<File> files = new ArrayList<>();
    private FileListAdapter fileListAdapter;
    private Uri downloadUri;
    private long downloadId;
    private int downloadPosition;

    public FolderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        assert bundle != null;
        roomId = bundle.getString("roomId");
        folderId = bundle.getString("folderId");
//        folderName = bundle.getString("folderName");

        DatabaseReference folderRef = getDatabaseReference("folders").child(folderId);
        folderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    folderName = (String) snapshot.child("name").getValue();
                    init( folderName, true);

                    // Temporary code for updating links for folders
                    if(!snapshot.child("link").exists()) {
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
                                        String dynamicLinkUri = String.valueOf(task.getResult().getShortLink());
                                        Log.d(TAG, "Dynamic Link: " + dynamicLinkUri);

                                        Map<String, Object> folderMap = new HashMap<>();
                                        folderMap.put("link", dynamicLinkUri);
                                        folderRef.updateChildren(folderMap);
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentFolderBinding.inflate(inflater, container, false);

        allFilesRef = getDatabaseReference().child("files");
        folderFilesRef = getDatabaseReference().child("folders").child(folderId).child("filesList");

        initializeAdminList();

        initializeRecyclerView();

        FileObserver directoryFileObserver = new FileObserver(Reon.downloadsDirectory, FileObserver.ALL_EVENTS) {
            @Override
            public void onEvent(int event, String path) {
                if(path != null) {
                    if(event == FileObserver.DELETE) {
                        Log.d("reon_FileObserver: ", "File Deleted");

//                        fileListAdapter = new FileListAdapter(getMainActivity(), files,
//                                getMainActivity());
//                        fileList.setAdapter(fileListAdapter);
                        fileListAdapter.notifyDataSetChanged();
//                        fileListAdapter.notifyItemRangeChanged(0, files.size());

                    }
                }
            }
        };
        directoryFileObserver.startWatching();

        binding.buttonAddFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            Intent.createChooser(getMainActivity().getIntent(), "Select File(s)");
            chooseFileResultLauncher.launch(intent);
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init( folderName, true);

        getMainActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_folder, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.menuItem_rename) {
                    renameFolder();
                } else if(itemId == R.id.menuItem_refresh) {
                    // refresh fragment
//                    finish();
//                    overridePendingTransition(0, 0);
//                    startActivity(getIntent());
//                    overridePendingTransition(0, 0);
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
        RecyclerView fileList = binding.recyclerFileList;
        fileList.setNestedScrollingEnabled(false);
        fileList.setHasFixedSize(false);
        RecyclerView.LayoutManager fileListLayoutManager = new GridLayoutManager(getMainActivity(), 1,
                RecyclerView.VERTICAL, false);
        fileList.setLayoutManager(fileListLayoutManager);
        fileListAdapter = new FileListAdapter(getMainActivity(), files, this);
        fileList.setAdapter(fileListAdapter);

        folderFilesRef.addValueEventListener(folderFilesListener);
    }

    ValueEventListener folderFilesListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists()) {
                fileIds.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    fileIds.add((String) ds.getValue());
                }
//                allFilesRef.addListenerForSingleValueEvent(allFilesListener);
                allFilesRef.addValueEventListener(allFilesListener);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d(TAG, error.getMessage());
        }
    };

    ValueEventListener allFilesListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists() && !fileIds.isEmpty()) {
                files.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
//                    Log.d(TAG, ds.getKey());
                    if (fileIds.contains(ds.getKey())) {
                        files.add(ds.getValue(File.class));
//                        files.get(files.size() - 1).setThumbnail(createThumbnail(files.get(files.size() - 1)));
                    }
                }
                fileListAdapter.setFiles(files);
                fileListAdapter.notifyDataSetChanged();
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d(TAG, error.getMessage());
        }
    };

    @Override
    public void onFileClick(int position) {
        File file = files.get(position);

        Log.d(TAG, "Clicked on: " + file.getName());
        java.io.File temp = new java.io.File(Reon.downloadsDirectory + file.getName());
        if(!temp.exists()) {
            downloadPosition = position;
            downloadFile(file);
        } else {
            openFile(file);
        }
    }

    @Override
    public void onFileLongClick(int position) {
        File file = files.get(position);
        if (getCurrentUser().getUid().equals(file.getUploaded_by()) || isAdmin()) {

            AlertDialogBuilder builder = new AlertDialogBuilder(getMainActivity());

            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setTitle("Delete?");
            builder.setMessage("Are you sure you want to delete this file?");
            builder.setCancelable(true);
            builder.setPositiveButton("Yes", (dialog, which) -> {
                Log.d(TAG, "deleting file: " + files.get(position).getName());

                folderFilesRef.removeEventListener(folderFilesListener);

                StorageReference uploadsRef = getStorageReference().child("uploads");
                uploadsRef.child(file.getId()).delete().addOnSuccessListener(aVoid -> allFilesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(file.getId())) {
                            allFilesRef.child(file.getId()).removeValue();
                        }
                        folderFilesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                fileIds.remove(position);
                                folderFilesRef.setValue(fileIds);
                                fileListAdapter.removeThumbnail(position);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, error.getMessage());
                            }
                        });
                        folderFilesRef.addValueEventListener(folderFilesListener);
                        Toast.makeText(getMainActivity(), "File Deleted", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, error.getMessage());
                    }
                }));
            });
            builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());

            AlertDialog deleteFileDialog = builder.create();
            deleteFileDialog.show();
        }
    }

    private boolean isAdmin() {
        return adminIds.contains(getCurrentUser().getUid());
    }

    private void openFile(File file) {
        try {
            java.io.File temp = new java.io.File((Reon.downloadsDirectory + file.getName()));
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String ext = MimeTypeMap.getFileExtensionFromUrl(temp.getAbsolutePath());
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
            String[] parts = file.getType().split("/");
            intent.setDataAndType(FileProvider.getUriForFile(getMainActivity(), getMainActivity().getPackageName() +  ".provider", temp), file.getType());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public void downloadFile(File file) {
        String uri = file.getUri();
        getMainActivity().storagePermissionGranted();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(String.valueOf(uri)));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                .setMimeType(file.getType())
                .setTitle(file.getName())
                .setDescription(uri)
                .setDestinationInExternalPublicDir("/Reon/downloads/", file.getName());


        DownloadManager downloadManager = (DownloadManager) getMainActivity().getSystemService(Context.DOWNLOAD_SERVICE);

        downloadId = downloadManager.enqueue(request);
        Log.d(TAG, "downloadId: " + downloadId);

        DownloadManager.Query q = new DownloadManager.Query();
        q.setFilterById(downloadId);
        downloadManager = (DownloadManager) getMainActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        try (Cursor c = downloadManager.query(q)) {
            if (c.moveToFirst()) {
                @SuppressLint("Range") int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                Log.d(TAG, "status: " + status);
                if (status == DownloadManager.STATUS_RUNNING) {
                    fileListAdapter.startDownloading(downloadPosition);
                    fileListAdapter.notifyItemChanged(downloadPosition);
                }
            }
        }
        getMainActivity().registerReceiver(broadcastReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(downloadId == intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)) {
                fileListAdapter.stopDownloading(downloadPosition);
//                fileListAdapter.notifyItemChanged(downloadPosition);
                Toast.makeText(getMainActivity(), "Download completed", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Download Completed");
                try {
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getMainActivity(), notification);
                    r.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // temp
                DownloadManager.Query q = new DownloadManager.Query();
                q.setFilterById(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1));
                DownloadManager downloadManager = (DownloadManager) getMainActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                try (Cursor c = downloadManager.query(q)) {
                    if (c.moveToFirst()) {
                        @SuppressLint("Range") int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            // process download
                            @SuppressLint("Range") String title = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
                            Log.d(TAG, title);
                            fileListAdapter.notifyItemChanged(downloadPosition);
                            // get other required data by changing the constant passed to getColumnIndex
                        }
                    }
                }
            }
        }
    };

    ActivityResultLauncher<Intent> chooseFileResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() != Activity.RESULT_OK) {
                        Log.d(TAG, "onActivityResult: Back");
                        getMainActivity().finishActivity(result.getResultCode());
                    } else {
                        String filePath, fileName, fileType;
                        Intent data = result.getData();
                        assert data != null;
                        if (data.getClipData() == null) {
                            filePath = data.getData().toString();
                            Log.d(TAG, "File Path: " + filePath);
                            fileName = getFileName(data.getData());
                            Log.d(TAG, "File Name: " + fileName);
                            fileType = getMainActivity().getContentResolver().getType(Uri.parse(filePath));

                            allFilesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    final ProgressDialog progressDialog =
                                            new ProgressDialog(getMainActivity());
                                    progressDialog.setTitle("Uploading");
                                    progressDialog.show();

                                    String fileId = allFilesRef.push().getKey();

                                    StorageReference uploadsRef = getStorageReference("uploads");
                                    assert fileId != null;
                                    uploadsRef.child(fileId).putFile(Uri.parse(filePath)).addOnSuccessListener(taskSnapshot -> {

                                        Task<Uri> result1 = taskSnapshot.getStorage().getDownloadUrl();
                                        result1.addOnSuccessListener(uri -> {
                                            Log.d(TAG, "uri: " + uri);
                                            File file = new File(fileId,
                                                    getApp().dateTimeFormat.format(Calendar.getInstance().getTime()), fileName, fileType, getCurrentUser().getUid(), uri.toString());
                                            allFilesRef.child(Objects.requireNonNull(fileId)).setValue(file);
                                            folderFilesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                                    fileIds.clear();
                                                    if (snapshot1.exists()) {
                                                        for (DataSnapshot ds : snapshot1.getChildren()) {
                                                            fileIds.add((String) ds.getValue());
                                                        }
                                                    }
                                                    fileIds.add(fileId);
                                                    folderFilesRef.setValue(fileIds);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    Log.e(TAG, error.getMessage());
                                                }
                                            });
                                            progressDialog.dismiss();
                                            Toast.makeText(getMainActivity(), "File Uploaded ", Toast.LENGTH_LONG).show();
                                        });
                                    }).addOnFailureListener(exception -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(getMainActivity(), exception.getMessage(), Toast.LENGTH_LONG).show();
                                    }).addOnProgressListener(taskSnapshot -> {
                                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d(TAG, error.getMessage());
                                }
                            });

                        } else {
                            ArrayList<String> uris = new ArrayList<>();
                            for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                                uris.add(data.getClipData().getItemAt(i).getUri().toString());
                            }
                            Log.d(TAG, uris.toString());
                        }
                    }
                }
            });

    @SuppressLint("Range")
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getMainActivity().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst())
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void renameFolder() {
        LayoutInflater inflater = LayoutInflater.from(getMainActivity());
        View folderNameView = inflater.inflate(R.layout.alert_edit_text, null);

        // Need current activity context
        AlertDialogBuilder builder = new AlertDialogBuilder(getMainActivity());
        builder.setView(folderNameView);

        final EditText editFolderName = folderNameView.findViewById(R.id.edit_text_field);
        editFolderName.setText(folderName);
        editFolderName.setHint("Enter Folder Name");

        builder.setTitle("Rename Folder");
        builder.setCancelable(true);
        builder.setPositiveButton("Ok", (dialog, which) -> {
            String folderName = editFolderName.getText().toString();
            Objects.requireNonNull(getMainActivity().getSupportActionBar()).setTitle(folderName);
            DatabaseReference folderRef = getDatabaseReference("folders").child(folderId);
            folderRef.child("name").setValue(folderName);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog folderNameDialog = builder.create();
        folderNameDialog.show();

        editFolderName.requestFocus();
        editFolderName.postDelayed(() -> {
            InputMethodManager keyboard=(InputMethodManager) getMainActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(editFolderName,0);
        }, 200);
            //folderNameDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

    }


}