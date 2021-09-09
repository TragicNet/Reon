package com.example.reon.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reon.R;
import com.example.reon.Reon;
import com.example.reon.activities.FolderActivity;
import com.example.reon.classes.File;
import com.google.firebase.database.DatabaseReference;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_file, parent
                , false);
        return new FileListAdapter.ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull FileListAdapter.ViewHolder holder, int position) {
        File file = files.get(position);
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
            //String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
            ext = file.getName().substring(file.getName().lastIndexOf("."));
        }
        Log.d("reon_FileListAdapter", "file: " + file.getName());
        Log.d("reon_FileListAdapter", "ext: " + ext);
        if(Arrays.asList(".png", ".jpg", ".jpeg", ".gif", ".bmp").contains(ext)) {
            Log.d("reon_FileListAdapter", "image");
            if(temp.exists()) {
                if(file.getThumbnail() == null) {
                    Log.d("reon_FileListAdapter", "path: " + temp.getPath());
                    Bitmap bitmap = getPreview(temp.getPath());
                    final int THUMBSIZE = 64;

//                    Bitmap bitmap = ThumbnailUtils.extractThumbnail(
//                            BitmapFactory.decodeFile(temp.getAbsolutePath()),
//                            THUMBSIZE,
//                            THUMBSIZE);

                    if(bitmap != null) {
                        Log.d("reon_FileListAdapter", "set Bitmap");
                        file.setThumbnail(bitmap);
                        holder.image.setImageBitmap(bitmap);
                        ImageViewCompat.setImageTintList(holder.image, null);
                    }
                }
            } else {
                holder.image.setImageResource(R.drawable.ic_image);
            }
        } else if(Arrays.asList(".mp3", ".wav", ".ogg", ".midi").contains(ext)) {

        } else if(Arrays.asList(".mp4", ".rmvb", ".avi", ".flv", ".3gp").contains(ext)) {

        } else if(Arrays.asList(".jsp", ".html", ".htm", ".js", ".php").contains(ext)) {

        } else if(Arrays.asList(".xls", ".xlsx").contains(ext)) {

        } else if(Arrays.asList(".doc", ".docx").contains(ext)) {

        } else if(Arrays.asList(".ppt", ".pptx").contains(ext)) {

        } else if(Arrays.asList(".pdf").contains(ext)) {

        } else if(Arrays.asList(".jar", ".zip", ".rar", ".gz", ".7z").contains(ext)) {

        } else if(Arrays.asList(".apk").contains(ext)) {

        } else {

        }

//        return when ("$ext") {
//             -> R.drawable.image
//             -> R.drawable.audio
//             -> R.drawable.video
//             -> R.drawable.web
//             -> R.drawable.file
//             -> R.drawable.file
//             -> R.drawable.file
//             -> R.drawable.file
//             -> R.drawable.pdf
//             -> R.drawable.zip
//             -> R.drawable.apk
//        else -> R.drawable.file
//        }

    }

    Bitmap getPreview(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        int origWidth = bitmap.getWidth();
        int origHeight = bitmap.getHeight();
        bitmap = Bitmap.createScaledBitmap(bitmap, origWidth / 10, origHeight / 10, false);
        return bitmap;
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
        }

        @Override
        public void onClick(View v) {
            onFileListener.onFileClick(getAdapterPosition());
        }

    }

    public interface OnFileListener {
        void onFileClick(int position);
    }

}
