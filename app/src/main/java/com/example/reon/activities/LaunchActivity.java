package com.example.reon.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.example.reon.R;
import com.example.reon.databinding.ActivityLaunchBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LaunchActivity extends BaseActivity {

    private ActivityLaunchBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLaunchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        app.setGoogleSignInClient(GoogleSignIn.getClient(this, googleSignInOptions));
//        if(app.getDatabase() == null)
//            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
//        app.initDatabase(FirebaseDatabase.getInstance());

        // init Firebase
        checkUser();

        // SignIn Button
        binding.googleSignInButton.setSize(SignInButton.SIZE_WIDE);
        binding.googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: begin");
                Intent intent = app.getGoogleSignInClient().getSignInIntent();

                googleSignInResultLauncher.launch(intent);
            }
        });

//        int nightModeFlags = getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
//        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES)
//            binding.googleSignInButton.setColorScheme(SignInButton.COLOR_DARK);

    }

    private void checkUser() {
        // go to profile if logged
        if(app.getCurrentUser() != null) {
            Log.d(TAG, "Already logged in");
            // get name of the user
            DatabaseReference userRef = app.getDatabase().getReference("users").child(app.getCurrentUser().getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = (String) dataSnapshot.child("name").getValue();
                    Log.d(TAG, "Username: " + name);
                    // go to dashboard if name changed or go to profile edit
                    if(name == null) {
                        Log.d(TAG, "Sending to profile edit");
                        Intent intent = new Intent(getApplicationContext(), ProfileEditActivity.class);
                        intent.putExtra("newUser", true);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.d(TAG, "Sending to profile");
                        startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                        finish();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, databaseError.getMessage());
                }
            });
        }
    }

    ActivityResultLauncher<Intent> googleSignInResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult: Google SignIn result");
                    Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = accountTask.getResult(ApiException.class);
                        authenticateGoogleAccount(account);
                    } catch (Exception e) {
                        Log.d(TAG, "onActivityResult: " + e.getMessage());
                    }
                }
            });

    private void authenticateGoogleAccount(GoogleSignInAccount account) {
        Log.d(TAG, "authenticateGoogleAccount: begin firebase auth with google");
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        app.getAuth().signInWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(@NonNull AuthResult authResult) {
                Log.d(TAG, "onSuccess: Logged In");

                FirebaseUser firebaseUser = app.getCurrentUser();

                assert firebaseUser != null;
                String uid = firebaseUser.getUid();
                String email = firebaseUser.getEmail();

                Log.d(TAG, "onSuccess: UID " + uid);
                Log.d(TAG, "onSuccess: Email " + email);

                // check if new or existing
                if(Objects.requireNonNull(authResult.getAdditionalUserInfo()).isNewUser()) {
                    // firebaseUser is new account Created
                    Log.d(TAG, "onSuccess: account created...\n" + email);
                    Toast.makeText(getApplicationContext(), "Account Created...\n" + email, Toast.LENGTH_SHORT).show();

                    // app.initDatabase(FirebaseDatabase.getInstance());
                    DatabaseReference userRef = app.getDatabase().getReference("users").child(firebaseUser.getUid());
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.exists()) {
                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("email", firebaseUser.getEmail());
                                userRef.updateChildren(userMap);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });

                } else {
                    // firebaseUser exists
                    Log.d(TAG, "onSuccess: account exists: " + email);
                    Toast.makeText(getApplicationContext(), "Account Exists...\n" + email, Toast.LENGTH_SHORT).show();
                }

                checkUser();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Login failed " + e.getMessage());
            }
        });
    }

}