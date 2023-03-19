package com.example.reon.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;

import com.example.reon.R;
import com.example.reon.Reon;
import com.example.reon.activities.BaseActivity;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

public abstract class BaseFragment extends Fragment {

    protected final String TAG;

    public BaseFragment() {
        this.TAG = ("reon_" + this.getClass().getSimpleName());
    }

    public Reon getApp() {  return ((BaseActivity) requireActivity()).getApp(); }

    public DatabaseReference getDatabaseReference() {    return ((BaseActivity) requireActivity()).getDatabaseReference();   }
    public DatabaseReference getDatabaseReference(String path) {    return ((BaseActivity) requireActivity()).getDatabaseReference(path);   }

    public StorageReference getStorageReference() { return ((BaseActivity) requireActivity()).getStorageReference(); }
    public StorageReference getStorageReference(String path) { return ((BaseActivity) requireActivity()).getStorageReference(path); }

    public FirebaseUser getCurrentUser() { return ((BaseActivity) requireActivity()).getCurrentUser(); }

    public BaseActivity getMainActivity() {    return (BaseActivity) requireActivity();    }

    public void init() {    init("Reon", false);    }

    public void init(String title, boolean upEnabled) {
        ActionBar toolbar = ((BaseActivity) requireActivity()).getSupportActionBar();
        assert toolbar != null;
        toolbar.setTitle(title);
        toolbar.setDisplayHomeAsUpEnabled(upEnabled);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(!this.getClass().equals(SignInFragment.class) && !this.getClass().equals(ProfileCreateFragment.class)) {
            Log.d(TAG,"Checking user" + this.getClass());
            checkUser();
        }

    }

    private void checkUser() {
        // go to profile if logged
        if (getCurrentUser() == null) {
            // go to sign in activity if not signed in
            openSignInFragment();
        } else {
            // get name of the user
            DatabaseReference userRef = getDatabaseReference("users").child(getCurrentUser().getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = (String) dataSnapshot.child("name").getValue();
                    //Log.d(TAG, "Username: " + name);
                    // go to sign in activity if no name
                    if(name == null) {
                        openSignInFragment();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, databaseError.getMessage());
                }
            });
        }
    }

    public void openSignInFragment() {
        Log.d(TAG, "going to SignInFragment");
        getNavController().navigate(R.id.signInFragment, null, new NavOptions.Builder().setPopUpTo(R.id.dashboardFragment, true).build());
    }

    public NavController getNavController() {
        return getMainActivity().getNavController();
    }

}
