package com.example.reon.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reon.R;
import com.example.reon.classes.File;
import com.example.reon.classes.Folder;

import java.util.ArrayList;
import java.util.List;

public class FolderListAdapter extends RecyclerView.Adapter<FolderListAdapter.ViewHolder> implements Filterable {
    private final String TAG = "reon_FolderListAdapter";
    private ArrayList<Folder> folders;
    private ArrayList<Folder> allFolders;
    private final Context context;
    private final FolderListAdapter.OnFolderListener listener;

    public FolderListAdapter(Context context, ArrayList<Folder> folders, FolderListAdapter.OnFolderListener listener) {
        this.folders = folders;
        this.allFolders = folders;
        this.context = context;
        this.listener = listener;
    }

    public void setFolders(ArrayList<Folder> folders) {
        this.folders = folders;
        this.allFolders = folders;
    }

    @NonNull
    @Override
    public FolderListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_folder, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderListAdapter.ViewHolder holder, int position) {
        holder.name.setText(folders.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    @Override
    public Filter getFilter() {
        return fileFilter;
    }

    private final Filter fileFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Folder> filteredList = new ArrayList<>();
            if (constraint.toString().isEmpty()) {
                filteredList.addAll(allFolders);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Folder folder : allFolders) {
                    if (folder.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(folder);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            folders = (ArrayList<Folder>) results.values;
            notifyDataSetChanged();
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView name;
        public ImageView image;

        FolderListAdapter.OnFolderListener onFolderListener;

        public ViewHolder(View view, FolderListAdapter.OnFolderListener onFolderListener) {
            super(view);

            name = view.findViewById(R.id.folder_item_name);
            image = view.findViewById(R.id.folder_item_image);

            this.onFolderListener = onFolderListener;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onFolderListener.onFolderClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) { onFolderListener.onFolderLongClick(getAdapterPosition()); return true; }
    }

    public interface OnFolderListener {
        void onFolderClick(int position);
        void onFolderLongClick(int position);
    }

}
