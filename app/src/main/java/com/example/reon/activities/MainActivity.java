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
import com.example.reon.Reon;
import com.example.reon.databinding.ActivityMainBinding;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;

    private static final String TAG = "reon/GOOGLE_SIGN_IN_TAG";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setApp((Reon) this.getApplication());
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar();

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //googleSignInClient =
        app.setGoogleSignInClient(GoogleSignIn.getClient(this, googleSignInOptions));

        // init Firebase
        checkUser();

        // SignIn Button
        binding.googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: begin");
                Intent intent = app.getGoogleSignInClient().getSignInIntent();

                googleSignInResultLauncher.launch(intent);
            }
        });

        binding.googleSignInButton.setSize(SignInButton.SIZE_WIDE);

//        int nightModeFlags = getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
//        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES)
//            binding.googleSignInButton.setColorScheme(SignInButton.COLOR_DARK);

    }

    private void checkUser() {
        // go to profile if logged
        if(app.getUser() != null) {
            Log.d(TAG, "Already logged in");
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            finish();
        }
    }

    private void authenticateGoogleAccount(GoogleSignInAccount account) {
        Log.d(TAG, "authenticateGoogleAccount: begin firebase auth with google");
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        app.getAuth().signInWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(@NonNull AuthResult authResult) {
                Log.d(TAG, "onSuccess: Logged In");

                FirebaseUser user = app.getUser();

                assert user != null;
                String uid = user.getUid();
                String email = user.getEmail();

                Log.d(TAG, "onSuccess: UID " + uid);
                Log.d(TAG, "onSuccess: Email " + email);

                // check if new or existing
                if(authResult.getAdditionalUserInfo().isNewUser()) {
                    // user is new account Created
                    Log.d(TAG, "onSuccess: account created...\n" + email);
                    Toast.makeText(MainActivity.this, "Account Created...\n" + email, Toast.LENGTH_SHORT).show();

                    //final FirebaseDatabase database = FirebaseDatabase.getInstance();
                    app.setDatabase(FirebaseDatabase.getInstance());
                    DatabaseReference userRef = app.getDatabase().getReference("users").child(user.getUid());
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!snapshot.exists()) {
                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("email", user.getEmail());
                                userMap.put("name", user.getEmail());
                                userRef.updateChildren(userMap);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });

                } else {
                    // user exists
                    Log.d(TAG, "onSuccess: account exists...\n" + email);
                    Toast.makeText(MainActivity.this, "Account Exists...\n" + email, Toast.LENGTH_SHORT).show();
                }

                // start profile activity
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Login failed " + e.getMessage());
            }
        });
    }

}