package com.example.reon;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;

public class Reon extends Application {

    private final String TAG = "reon_Application";

    private FirebaseDatabase database;
    private FirebaseStorage storage;
//    public static final String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Reon/";
    public static String downloadsDirectory;

    public final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US);

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        database.setPersistenceEnabled(true);
        String path;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
//            path = storageManager.getPrimaryStorageVolume().getDirectory().getAbsolutePath();
//            Log.d(TAG, "primary storaage path: " + path);
//        } else {
        path = getApplicationContext().getExternalFilesDir("downloads").getAbsolutePath();
//        path = Environment.getExternalStoragePublicDirectory("/Reon/downloads/").getAbsolutePath();
//        }
//        path = path + "/Reon/downloads";
        File f = new File(path);
        Log.d(TAG, "Path: " + f.getAbsolutePath());
        Log.d(TAG, Arrays.toString(getApplicationContext().getExternalMediaDirs()));//(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
        if (!f.exists()) {
            Log.d(TAG, String.valueOf(f.mkdirs()));
        }

//        Path filesDirectory = Paths.get(getApplicationContext().getExternalFilesDir(null).getAbsolutePath());
//        downloadsDirectory = filesDirectory + "/downloads/";
        downloadsDirectory = f.getAbsolutePath() + "/";

//        Log.d(TAG, "rootPath: " + downloadsDirectory);

    }

    public FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return getAuth().getCurrentUser();
    }

    public FirebaseDatabase getDatabase() {
        return database;
    }

    public FirebaseStorage getStorage() { return storage; }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
