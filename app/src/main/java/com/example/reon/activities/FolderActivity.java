package com.example.reon.activities;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reon.R;
import com.example.reon.Reon;
import com.example.reon.adapters.FileListAdapter;
import com.example.reon.classes.AlertDialogBuilder;
import com.example.reon.classes.File;
import com.example.reon.databinding.ActivityFolderBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class FolderActivity extends BaseActivity implements FileListAdapter.OnFileListener {

    private ActivityFolderBinding binding;

    String folderId = "",
            folderName = "",
            roomId = "";

    DatabaseReference allFilesRef, folderFilesRef;

    ArrayList<String> fileIds = new ArrayList<>();
    ArrayList<String> adminIds = new ArrayList<>();
    ArrayList<File> files = new ArrayList<>();
    private RecyclerView fileList;
    private FileListAdapter fileListAdapter;
    private RecyclerView.LayoutManager fileListLayoutManager;
    private Uri downloadUri;
    private long downloadId;
    private int downloadPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFolderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        if(intent.hasExtra("folderId")) {
            roomId = intent.getStringExtra("roomId");
            folderId = intent.getStringExtra("folderId");
            folderName = intent.getStringExtra("folderName");
        }

        init( folderName, true);

        allFilesRef = app.getDatabase().getReference().child("files");
        folderFilesRef = app.getDatabase().getReference().child("folders").child(folderId).child("filesList");

        initializeAdminList();

        initailizeRecyclerView();

        binding.buttonAddFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked On Add File Button");
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                Intent.createChooser(getIntent(), "Select File(s)");
                chooseFileResultLauncher.launch(intent);
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
        fileList = binding.recyclerFileList;
        fileList.setNestedScrollingEnabled(false);
        fileList.setHasFixedSize(false);
        fileListLayoutManager = new GridLayoutManager(getApplicationContext(), 1,
                RecyclerView.VERTICAL, false);
        fileList.setLayoutManager(fileListLayoutManager);
        fileListAdapter = new FileListAdapter(getApplicationContext(), files,
                FolderActivity.this);
        fileList.setAdapter(fileListAdapter);

        folderFilesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    fileIds.clear();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        fileIds.add((String) ds.getValue());
                    }
                    allFilesRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists() && !fileIds.isEmpty()) {
                                files.clear();
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                    if (fileIds.contains(ds.getKey())) {
                                        files.add(ds.getValue(File.class));
//                                        files.get(files.size() - 1).setThumbnail(createThumbnail(files.get(files.size() - 1)));
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
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getMessage());
            }
        });
    }

    @Override
    public void onFileClick(int position) {
        File file = files.get(position);
        Log.d(TAG, "Clicked on: " + file.getName());
        java.io.File temp = new java.io.File((Reon.rootPath + file.getName()));
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
        if (app.getCurrentUser().getUid().equals(file.getUploaded_by()) || isAdmin()) {

            AlertDialogBuilder builder = new AlertDialogBuilder(FolderActivity.this);

            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setTitle("Delete?");
            builder.setMessage("Are you sure you want to delete this file?");
            builder.setCancelable(true);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d(TAG, "deleting file: " + files.get(position).getName());

                    StorageReference uploadsRef = app.getStorage().getReference().child("uploads");
                    uploadsRef.child(file.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(@NonNull Void aVoid) {
                            allFilesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.hasChild(file.getId())) {
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
                                    Toast.makeText(getApplicationContext(), "File Deleted", Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, error.getMessage());
                                }
                            });
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

            AlertDialog deleteFileDialog = builder.create();
            deleteFileDialog.show();
        }
    }

    private boolean isAdmin() {
        return adminIds.contains(app.getCurrentUser().getUid());
    }

    private void openFile(File file) {
        try {
            java.io.File temp = new java.io.File((Reon.rootPath + file.getName()));
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String ext = MimeTypeMap.getFileExtensionFromUrl(temp.getAbsolutePath());
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
            String parts[] = file.getType().split("/");
            intent.setDataAndType(FileProvider.getUriForFile(FolderActivity.this, getApplicationContext().getPackageName() +  ".provider", temp), file.getType());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public void downloadFile(File file) {
        String uri = file.getUri();
        java.io.File root = new java.io.File(Reon.rootPath);
        if(!root.exists())
            root.mkdirs();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(String.valueOf(uri)));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setMimeType(file.getType())
                .setTitle(file.getName())
                .setDescription(uri.toString())
                .setDestinationInExternalPublicDir("/Reon", file.getName());

        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        downloadId = downloadManager.enqueue(request);
        Log.d(TAG, "downloadId: " + downloadId);

        DownloadManager.Query q = new DownloadManager.Query();
        q.setFilterById(downloadId);
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Cursor c = downloadManager.query(q);
        if (c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            Log.d(TAG, "status: " + status);
            if (status == DownloadManager.STATUS_RUNNING) {
                fileListAdapter.startDownloading(downloadPosition);
                fileListAdapter.notifyItemChanged(downloadPosition);
            }
        }
        registerReceiver(broadcastReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(downloadId == intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)) {
                fileListAdapter.stopDownloading(downloadPosition);
                fileListAdapter.notifyItemChanged(downloadPosition);
                Toast.makeText(getApplicationContext(), "Download completed", Toast.LENGTH_SHORT).show();

                // temp
                DownloadManager.Query q = new DownloadManager.Query();
                q.setFilterById(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1));
                DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                Cursor c = downloadManager.query(q);
                if (c.moveToFirst()) {
                    int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        // process download
                        String title = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
                        Log.d(TAG, title);
                        // get other required data by changing the constant passed to getColumnIndex
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
                    String filePath, fileName, fileType;
                    Intent data = result.getData();
                    assert data != null;
                    if(data.getClipData() == null) {
                        filePath = data.getData().toString();
                        Log.d(TAG, "File Path: " + filePath);
                        fileName = getFileName(data.getData());
                        Log.d(TAG, "File Name: " + fileName);
                        fileType = FolderActivity.this.getContentResolver().getType(Uri.parse(filePath));

                        allFilesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                final ProgressDialog progressDialog =
                                        new ProgressDialog(FolderActivity.this);
                                progressDialog.setTitle("Uploading");
                                progressDialog.show();

                                String fileId = allFilesRef.push().getKey();

                                StorageReference uploadsRef = app.getStorage().getReference("uploads");
                                uploadsRef.child(fileId).putFile(Uri.parse(filePath)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(@NonNull Uri uri) {
                                                Log.d(TAG, "uri: " + uri);
                                                File file = new File(fileId,
                                                        app.dateTimeFormat.format(Calendar.getInstance().getTime()), fileName, fileType, app.getCurrentUser().getUid(), uri.toString());
                                                allFilesRef.child(Objects.requireNonNull(fileId)).setValue(file);
                                                folderFilesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        fileIds.clear();
                                                        if (snapshot.exists()) {
                                                            for (DataSnapshot ds : snapshot.getChildren()) {
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
                                                Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d(TAG, error.getMessage());
                            }
                        });

                    } else {
                        ArrayList<String> uris = new ArrayList<>();
                        for(int i = 0; i < data.getClipData().getItemCount(); i++) {
                            uris.add(data.getClipData().getItemAt(i).getUri().toString());
                        }
                        Log.d(TAG, uris.toString());
                    }
                }
            });

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_folder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menuItem_rename) {
            Log.d(TAG, "Pressed rename menuitem");
            renameFolder();
        }

        return super.onOptionsItemSelected(item);
    }

    private void renameFolder() {
        LayoutInflater inflater = LayoutInflater.from(FolderActivity.this);
        View folderNameView = inflater.inflate(R.layout.alert_rename_folder, null);

        // Need current activity context
        AlertDialogBuilder builder = new AlertDialogBuilder(FolderActivity.this);
        builder.setView(folderNameView);

        final EditText editFolderName = folderNameView.findViewById(R.id.edit_folder_name);
        editFolderName.setText(folderName);

        builder.setTitle("Rename Folder");
        builder.setCancelable(true);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String folderName = editFolderName.getText().toString();
                Objects.requireNonNull(getSupportActionBar()).setTitle(folderName);
                DatabaseReference folderRef = app.getDatabase().getReference("folders").child(folderId);
                folderRef.child("name").setValue(folderName);
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