package com.example.reon.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.reon.R;
import com.example.reon.classes.File;
import com.example.reon.databinding.FragmentFileInfoBinding;
import com.example.reon.databinding.FragmentProfileBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class FileInfoFragment extends BaseFragment {

    private FragmentFileInfoBinding binding;
    private File file;
    private DatabaseReference fileReference;

    public FileInfoFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        assert bundle != null;
        String fileId = bundle.getString("fileId");

        fileReference = getDatabaseReference("files").child(fileId);
        fileReference.addValueEventListener(fileListener);
    }

    ValueEventListener fileListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            file = snapshot.getValue(File.class);
            assert file != null;
            DatabaseReference uploaderReference = getDatabaseReference("users").child(file.getUploaded_by());
            uploaderReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    file.setUploaded_by((String) snapshot.child("name").getValue());
                    binding.textFilename.setText(file.getName());
                    binding.textUploadedby.setText(file.getUploaded_by());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentFileInfoBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init("File Info", true);
    }

    @Override
    public void onResume() {
        super.onResume();
        fileReference.addValueEventListener(fileListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        fileReference.removeEventListener(fileListener);
    }
}