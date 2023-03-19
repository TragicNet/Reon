package com.example.reon.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.reon.R;
import com.example.reon.databinding.FragmentProfileCreateBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileCreateFragment extends BaseFragment {

    private FragmentProfileCreateBinding binding;
    private String email;

    public ProfileCreateFragment() {
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
        binding = FragmentProfileCreateBinding.inflate(inflater, container, false);

        DatabaseReference userRef = getDatabaseReference("users").child(getCurrentUser().getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                binding.editUserName.setText((String) dataSnapshot.child("name").getValue());
                binding.editUserAbout.setText((String) dataSnapshot.child("about").getValue());
                email = (String) dataSnapshot.child("email").getValue();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });


        binding.buttonUpdateProfile.setOnClickListener(v -> {
            String name = binding.editUserName.getText().toString();
            String about = binding.editUserAbout.getText().toString();

            // Toast if empty Name
            if (name.equals("")) {
                Toast.makeText(getMainActivity(), "Enter a Valid Name", Toast.LENGTH_SHORT).show();
            } else {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("name", name);
                userMap.put("about", about);
                if(email == null)
                    userMap.put("email", getCurrentUser().getEmail());
                userRef.updateChildren(userMap);

                FragmentManager fragmentManager = getMainActivity().getSupportFragmentManager();
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.nav_host_fragment, new DashboardFragment());
                transaction.commit();

            }
        });

        if(binding.editUserName.requestFocus()) {
            getMainActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init("Profile", false);

    }
}