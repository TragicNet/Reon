package com.example.reon.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reon.R;
import com.example.reon.Reon;
import com.example.reon.classes.File;
import com.example.reon.classes.Room;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder> implements Filterable {
    private final String TAG = "reon_FileListAdapter";
    private ArrayList<File> files;
    private ArrayList<File> allFiles;
    private final Context context;
    private final FileListAdapter.OnFileListener listener;

    public FileListAdapter(Context context, ArrayList<File> files,
                        FileListAdapter.OnFileListener listener) {
        this.files = files;
        this.allFiles = files;
        this.context = context;
        this.listener = listener;
    }

    public void setFiles(ArrayList<File> files) {
        this.files = files;
        this.allFiles = files;
    }

    public void startDownloading(int position) { files.get(position).setDownloading(true); }

    public void stopDownloading(int position) { files.get(position).setDownloading(false); }

    @Override
    public Filter getFilter() {
        return fileFilter;
    }

    private final Filter fileFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<File> filteredList = new ArrayList<>();
            if (constraint.toString().isEmpty()) {
                filteredList.addAll(allFiles);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (File file : allFiles) {
                    if (file.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(file);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            files = (ArrayList<File>) results.values;
            notifyDataSetChanged();
        }
    };

    @NonNull
    @Override
    public FileListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_file, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull FileListAdapter.ViewHolder holder, int position) {
        File file = files.get(position);
//        Log.d("reon_FileListAdapter", "name: " + file.getName());
        java.io.File temp = new java.io.File(Reon.downloadsDirectory + file.getName());
        TextView nameView = holder.itemView.findViewById(R.id.file_item_name);
        if(file.getUploaded_by().equals(((Reon) context.getApplicationContext()).getCurrentUser().getUid())) {
            nameView.setTextColor(context.getResources().getColor(R.color.common_accent));
        }
        ImageView downloadIcon = holder.itemView.findViewById(R.id.file_item_download);
        if (temp.exists()) {
            downloadIcon.setVisibility(View.GONE);
        } else {
            downloadIcon.setVisibility(View.VISIBLE);
            if(file.isDownloading()) {
                downloadIcon.setImageResource(R.drawable.ic_downloading);
            } else {
                downloadIcon.setImageResource(R.drawable.ic_download);
            }
//            Log.d("reon_FileListAdapter", "Does not exist " + temp.getAbsolutePath());
        }
        holder.name.setText(file.getName());
        holder.date.setText(file.getCreated_at());

        String ext = "";

        if(file.getName().contains(".")) {
            ext = file.getName().substring(file.getName().lastIndexOf("."));
        }
        if(Arrays.asList(".png", ".jpg", ".jpeg", ".gif", ".bmp").contains(ext)) {
            if(temp.exists()) {
                Bitmap bitmap = getPreview(temp.getPath());

                if(bitmap != null) {
                    holder.image.setImageBitmap(bitmap);
//                        ImageViewCompat.setImageTintList(holder.image, null);
                } else {
                    Log.d(TAG, file.getName() + " bitmap null");
                }
            } else {
                holder.image.setImageResource(R.drawable.ic_image);
            }

//                ImageViewCompat.setImageTintList(holder.image, ColorStateList.valueOf(android.R.attr.colorAccent));

        } else if(Arrays.asList(".mp3", ".wav", ".ogg", ".midi").contains(ext)) {
            holder.image.setImageResource(R.drawable.ic_audio);
        } else if(Arrays.asList(".mp4", ".rmvb", ".avi", ".flv", ".3gp").contains(ext)) {
            holder.image.setImageResource(R.drawable.ic_video);
        } else if(Arrays.asList(".jsp", ".html", ".htm", ".js", ".php").contains(ext)) {
            holder.image.setImageResource(R.drawable.ic_web);
        } else if(Arrays.asList(".xls", ".xlsx").contains(ext)) {
            holder.image.setImageResource(R.drawable.ic_ms_xcel);
        } else if(Arrays.asList(".doc", ".docx").contains(ext)) {
            holder.image.setImageResource(R.drawable.ic_ms_word);
        } else if(Arrays.asList(".ppt", ".pptx").contains(ext)) {
            holder.image.setImageResource(R.drawable.ic_audio);
        } else if(Arrays.asList(".pdf").contains(ext)) {
            holder.image.setImageResource(R.drawable.ic_audio);
        } else if(Arrays.asList(".jar", ".zip", ".rar", ".gz", ".7z").contains(ext)) {
            holder.image.setImageResource(R.drawable.ic_archive);
        } else if(Arrays.asList(".apk").contains(ext)) {
            holder.image.setImageResource(R.drawable.ic_android);
        } else {
            holder.image.setImageResource(R.drawable.ic_file);
        }

        if (file.isSelected()) {
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.ui_selection_bg));
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

    }

    Bitmap getPreview(String path) {
        final int scaleDown = 1;
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        int origWidth = bitmap.getWidth();
        int origHeight = bitmap.getHeight();
        bitmap = Bitmap.createScaledBitmap(bitmap, origWidth / scaleDown, origHeight / scaleDown, false);
        return bitmap;
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public int getSelectionCount() {
        int count = 0;
        for (File file : files) {
            if (file.isSelected())
                count++;
        }
        return count;
    }

    public void clearSelection() {
        int position = 0;
        for (File file : files) {
            if (file.isSelected()) {
                file.setSelected(false);
                notifyItemChanged(position);
            }
            position++;
        }
    }

    public void selectAll() {
        for (File file : files) {
            file.setSelected(true);
        }
        notifyDataSetChanged();
    }

    public List<File> getSelectedFiles() {
        List<File> selectedFiles = new ArrayList<>();
        for(File file : files) {
            if(file.isSelected())
                selectedFiles.add(file);
        }
        return selectedFiles;
    }

    public int getPosition(File file) {
        return files.indexOf(file);
    }


//    public void removeThumbnail(int position) {
//        files.get(position).setThumbnail(null);
//    }

    public void toggleSelection(int position) {
        files.get(position).setSelected(!files.get(position).isSelected());
        notifyItemChanged(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView name;
        public TextView date;
        public ImageView image;

        FileListAdapter.OnFileListener onFileListener;

        public ViewHolder(View view, FileListAdapter.OnFileListener onFileListener) {
            super(view);

            name = view.findViewById(R.id.file_item_name);
            date = view.findViewById(R.id.file_item_date);
            image = view.findViewById(R.id.file_item_image);

            this.onFileListener = onFileListener;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onFileListener.onFileClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) { onFileListener.onFileLongClick(getAdapterPosition()); return true; }

    }

    public interface OnFileListener {
        void onFileClick(int position);
        void onFileLongClick(int position);
    }

}
