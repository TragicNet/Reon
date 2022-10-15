package com.example.reon.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reon.R;
import com.example.reon.Reon;
import com.example.reon.classes.File;

import java.util.ArrayList;
import java.util.Arrays;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder> {
    private ArrayList<File> files;
    private Context context;
    private FileListAdapter.OnFileListener listener;

    public FileListAdapter(Context context, ArrayList<File> files,
                        FileListAdapter.OnFileListener listener) {
        this.files = files;
        this.context = context;
        this.listener = listener;
    }

    public void setFiles(ArrayList<File> files) {
        this.files = files;
    }

    public void startDownloading(int position) { files.get(position).setDownloading(true); }

    public void stopDownloading(int position) { files.get(position).setDownloading(false); }

    @NonNull
    @Override
    public FileListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_file, parent, false);
        return new FileListAdapter.ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull FileListAdapter.ViewHolder holder, int position) {
        File file = files.get(position);
        Log.d("reon_FileListAdapter", "name: " + file.getName());
        java.io.File temp = new java.io.File((Reon.rootPath + file.getName()));
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
        }
        holder.name.setText(file.getName());
        holder.date.setText(file.getCreated_at());

        String ext = "";

        if(file.getName().contains(".")) {
            ext = file.getName().substring(file.getName().lastIndexOf("."));
        }
        if(Arrays.asList(".png", ".jpg", ".jpeg", ".gif", ".bmp").contains(ext)) {
            boolean setImage = false;
            if(temp.exists()) {
                if(file.getThumbnail() == null) {
//                    Log.d("reon_FileListAdapter", "path: " + temp.getPath());
                    Bitmap bitmap = getPreview(temp.getPath());

                    if(bitmap != null) {
                        file.setThumbnail(bitmap);
                        holder.image.setImageBitmap(bitmap);
//                        ImageViewCompat.setImageTintList(holder.image, null);
                        setImage = true;
                    }
                }
            }
            if (!setImage){
                holder.image.setImageResource(R.drawable.ic_image);
//                ImageViewCompat.setImageTintList(holder.image, ColorStateList.valueOf(android.R.attr.colorAccent));
            }
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

    public void removeThumbnail(int position) {
        files.get(position).setThumbnail(null);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
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
