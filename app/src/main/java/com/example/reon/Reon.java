package com.example.reon;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.reon.classes.User;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Reon extends Application {

    private final String TAG = "reon_app";

    private GoogleSignInClient googleSignInClient;
    private FirebaseDatabase database;

    public FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }

    public GoogleSignInClient getGoogleSignInClient() {
        return googleSignInClient;
    }

    public void setGoogleSignInClient(GoogleSignInClient googleSignInClient) {
        this.googleSignInClient = googleSignInClient;
    }

    public FirebaseUser getCurrentUser() {
        return getAuth().getCurrentUser();
    }

    public FirebaseDatabase getDatabase() {
        return database;
    }

    public void initDatabase(FirebaseDatabase database) {
        this.database = database;
    }

//    public User getUser() {
//        DatabaseReference userRef = database.getReference("users").child(getCurrentUser().getUid());
//        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if(dataSnapshot.exists()) {
//                    ArrayList<String> roomList = new ArrayList<>();
//                    for(DataSnapshot ds : dataSnapshot.child("rooms").getChildren()) {
//                        roomList.add((String) ds.getValue());
//                    }
//                    user = new User(dataSnapshot.getKey(), (String) dataSnapshot.child("email").getValue(), (String) dataSnapshot.child("name").getValue(), (String) dataSnapshot.child("about").getValue(), roomList);
//                    //Log.d(TAG, "Created User: " + user);
//                    return user;
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.d(TAG, databaseError.getMessage());
//            }
//        });
//    }

}
