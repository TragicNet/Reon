package com.example.reon.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Scroller;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.reon.R;
import com.example.reon.classes.AlertDialogBuilder;
import com.example.reon.databinding.FragmentProfileBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends BaseFragment {

    private FragmentProfileBinding binding;
    private String userName = "", userDescription = "";
    private DatabaseReference userRef;
    private String email;

    public ProfileFragment() {
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
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        email = getCurrentUser().getEmail();
        binding.nameView.setText(email);

        userRef = getDatabaseReference("users").child(getCurrentUser().getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(email.equals(""))
                    email = (String) dataSnapshot.child("email").getValue();
                userName = (String) dataSnapshot.child("name").getValue();
                userDescription = (String) dataSnapshot.child("about").getValue();
                binding.textUserName.setText(userName);
                binding.textUserDescription.setText(userDescription);
                binding.textUserDescription.setMovementMethod(new ScrollingMovementMethod());

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });

        binding.nameContainerFrame.setOnClickListener(v -> editName());

        binding.descriptionContainerFrame.setOnClickListener(v -> editDescription());

        // handle logout
        binding.buttonLogout.setOnClickListener(v -> {
            getApp().getAuth().signOut();

            GoogleSignIn.getClient(
                    getMainActivity(),
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            ).signOut().addOnCompleteListener(task -> openSignInFragment());
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init("Profile", true);
    }

    private void editName() {
        LayoutInflater inflater = LayoutInflater.from(getMainActivity());
        View userNameView = inflater.inflate(R.layout.alert_edit_text, null);

        // Need current activity context
        AlertDialogBuilder builder = new AlertDialogBuilder(getMainActivity());
        builder.setView(userNameView);

        final EditText editUserName = userNameView.findViewById(R.id.edit_text_field);
        editUserName.setText(userName);
        editUserName.setHint("Enter User Name");

        builder.setTitle("Edit Name");
        builder.setCancelable(true);
        builder.setPositiveButton("Ok", (dialog, which) -> {
            String name = editUserName.getText().toString();
            // Toast if empty Name
            if (name.equals("")) {
                Toast.makeText(getMainActivity(), "Enter a Valid Name", Toast.LENGTH_SHORT).show();
            } else {
                userName = name;
                binding.textUserName.setText(userName);

                Map<String, Object> userMap = new HashMap<>();
                userMap.put("name", userName);
//                    if(email == null)
//                        userMap.put("email", getCurrentUser().getEmail());
                userRef.updateChildren(userMap);
            }

        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog userNameDialog = builder.create();
        userNameDialog.show();

        editUserName.requestFocus();
        editUserName.postDelayed(() -> {
            InputMethodManager keyboard=(InputMethodManager) getMainActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(editUserName,0);
        }, 200);

    }

    private void editDescription() {
        LayoutInflater inflater = LayoutInflater.from(getMainActivity());
        View userDescriptionView = inflater.inflate(R.layout.alert_edit_text, null);

        // Need current activity context
        AlertDialogBuilder builder = new AlertDialogBuilder(getMainActivity());
        builder.setView(userDescriptionView);

        final EditText editUserDescription = userDescriptionView.findViewById(R.id.edit_text_field);
        editUserDescription.setText(userDescription);
        editUserDescription.setHint("Enter User About");

        // Setup for multi line
        editUserDescription.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editUserDescription.setSingleLine(false);
        editUserDescription.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);

        editUserDescription.setScroller(new Scroller(getMainActivity()));
        editUserDescription.setMaxLines(10);

        editUserDescription.setVerticalScrollBarEnabled(true);
        editUserDescription.setMovementMethod(new ScrollingMovementMethod());


        builder.setTitle("Edit About");
        builder.setCancelable(true);
        builder.setPositiveButton("Ok", (dialog, which) -> {

            userDescription = editUserDescription.getText().toString();
            binding.textUserDescription.setText(userDescription);

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("about", userDescription);
//                if(email == null)
//                    userMap.put("email", getCurrentUser().getEmail());
            userRef.updateChildren(userMap);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog userAboutDialog = builder.create();
        userAboutDialog.show();

        editUserDescription.requestFocus();
        editUserDescription.postDelayed(() -> {
            InputMethodManager keyboard=(InputMethodManager) getMainActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(editUserDescription,0);
        }, 200);

    }
}