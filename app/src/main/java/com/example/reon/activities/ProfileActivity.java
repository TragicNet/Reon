package com.example.reon.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.example.reon.databinding.ActivityProfileBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends BaseActivity {

    private ActivityProfileBinding binding;

    ActivityResultLauncher<Intent> profileEditActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Intent data = result.getData();
                    Log.d(TAG, "resultCode: " + result.getResultCode());
                    if (data != null) {
                        Log.d(TAG, "onActivityResult: " + data);
                        binding.textUserName.setText(data.getStringExtra("name"));
                        binding.textUserAbout.setText(data.getStringExtra("about"));
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init("Profile", true);

        String email = app.getCurrentUser().getEmail();
        binding.nameView.setText(email);

        DatabaseReference userRef = app.getDatabase().getReference("users").child(app.getCurrentUser().getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                binding.textUserName.setText((String) dataSnapshot.child("name").getValue());
                binding.textUserAbout.setText((String) dataSnapshot.child("about").getValue());
                binding.textUserAbout.setMovementMethod(new ScrollingMovementMethod());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });

        Log.d(TAG, "usename: " + userRef.child("name"));

        // handle logout
        binding.buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Logging Out");

                app.getAuth().signOut();
                Log.d(TAG, "Logged Out Auth");

                GoogleSignIn.getClient(
                        getApplicationContext(),
                        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                ).signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "Logged Out Google");
                        Log.d(TAG, "Current User: " + app.getCurrentUser());
                        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                        finishAffinity();
                    }
                });
            }
        });

        binding.buttonEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileEditActivityResultLauncher.launch(new Intent(getApplicationContext(), ProfileEditActivity.class));
            }
        });

    }
}