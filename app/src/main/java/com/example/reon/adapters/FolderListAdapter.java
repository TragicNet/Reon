package com.example.reon.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reon.R;
import com.example.reon.classes.Folder;

import java.util.ArrayList;

public class FolderListAdapter extends RecyclerView.Adapter<FolderListAdapter.ViewHolder> {
    private ArrayList<Folder> folders;
    private Context context;
    private FolderListAdapter.OnFolderListener listener;

    public FolderListAdapter(Context context, ArrayList<Folder> folders, FolderListAdapter.OnFolderListener listener) {
        this.folders = folders;
        this.context = context;
        this.listener = listener;
    }

    public void setFolders(ArrayList<Folder> folders) {
        this.folders = folders;
    }

    @NonNull
    @Override
    public FolderListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_folder, parent, false);
        return new FolderListAdapter.ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderListAdapter.ViewHolder holder, int position) {
        holder.name.setText(folders.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView name;
        public ImageView image;

        FolderListAdapter.OnFolderListener onFolderListener;

        public ViewHolder(View view, FolderListAdapter.OnFolderListener onFolderListener) {
            super(view);

            name = view.findViewById(R.id.folder_item_name);
            image = view.findViewById(R.id.folder_item_image);

            this.onFolderListener = onFolderListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onFolderListener.onFolderClick(getAdapterPosition());
        }
    }

    public interface OnFolderListener {
        void onFolderClick(int position);
        void onFolderLongClick(int position);
    }

}
