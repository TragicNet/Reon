package com.example.reon;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Reon extends Application {

    private final String TAG = "reon_app";

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

        File f = new File(Environment.getExternalStorageDirectory(), "Reon/downloads");
        if (!f.exists()) {
            f.mkdirs();
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
