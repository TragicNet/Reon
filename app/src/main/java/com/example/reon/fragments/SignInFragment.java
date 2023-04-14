package com.example.reon.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavOptions;

import com.example.reon.R;
import com.example.reon.databinding.FragmentSignInBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignInFragment extends BaseFragment {

    private FragmentSignInBinding binding;

    private GoogleSignInClient googleSignInClient;

    public SignInFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSignInBinding.inflate(inflater, container, false);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(getMainActivity(), googleSignInOptions);

        // init Firebase
        checkUser();

        // SignIn Button
        binding.googleSignInButton.setSize(SignInButton.SIZE_WIDE);
        binding.googleSignInButton.setOnClickListener(v -> googleSignInResultLauncher.launch(googleSignInClient.getSignInIntent()));

//        int nightModeFlags = getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
//        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES)
//            binding.googleSignInButton.setColorScheme(SignInButton.COLOR_DARK);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void checkUser() {
        // go to profile if logged
        if(getCurrentUser() != null) {
            Log.d(TAG, "Already logged in");
            // get name of the user
            DatabaseReference userRef = getDatabaseReference("users").child(getCurrentUser().getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = (String) dataSnapshot.child("name").getValue();
                    Log.d(TAG, "Username: " + name);
                    // go to dashboard if name changed or go to profile edit
                    if(name == null) {
                        getNavController().navigate(R.id.profileCreateFragment, null, new NavOptions.Builder().setPopUpTo(R.id.signInFragment, true).build());
                    } else {
                        getNavController().navigate(R.id.dashboardFragment, null, new NavOptions.Builder().setPopUpTo(R.id.signInFragment, true).build());
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
            result -> {
                Log.d(TAG, "onActivityResult: Google SignIn result");
                Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = accountTask.getResult(ApiException.class);
                    authenticateGoogleAccount(account);
                } catch (Exception e) {
                    Log.d(TAG, "onActivityResult: " + e.getMessage());
                }
            });

    private void authenticateGoogleAccount(GoogleSignInAccount account) {
        Log.d(TAG, "authenticateGoogleAccount: begin firebase auth with google");
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        getApp().getAuth().signInWithCredential(credential).addOnSuccessListener(authResult -> {
            Log.d(TAG, "onSuccess: Logged In");

            FirebaseUser firebaseUser = getCurrentUser();

            assert firebaseUser != null;
            String uid = firebaseUser.getUid();
            String email = firebaseUser.getEmail();

            Log.d(TAG, "onSuccess: UID " + uid);
            Log.d(TAG, "onSuccess: Email " + email);

            // check if new or existing
            if(Objects.requireNonNull(authResult.getAdditionalUserInfo()).isNewUser()) {
                // firebaseUser is new account Created
                Log.d(TAG, "onSuccess: account created...\n" + email);
                Toast.makeText(getMainActivity(), "Account Created...\n" + email, Toast.LENGTH_SHORT).show();

                // app.initDatabase(FirebaseDatabase.getInstance());
                DatabaseReference userRef = getDatabaseReference("users").child(firebaseUser.getUid());
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.exists()) {
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("id", uid);
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
                Toast.makeText(getMainActivity(), "Account Exists...\n" + email, Toast.LENGTH_SHORT).show();
            }

            checkUser();
        }).addOnFailureListener(e -> Log.d(TAG, "onFailure: Login failed " + e.getMessage()));
    }

}