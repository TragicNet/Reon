package com.example.reon.classes;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

public class File {
    private String id;
    private String created_at;
    private String name;
    private String type;
    private String uploaded_by;
    private String link;
    private String uri;

    @Exclude
    private boolean downloading = false;
    @Exclude
    private boolean selected = false;

    public File() {}

    public File(String id, String created_at, String name, String type, String uploaded_by, String link, String uri) {
        this.id = id;
        this.created_at = created_at;
        this.name = name;
        this.type = type;
        this.uploaded_by = uploaded_by;
        this.link = link;
        this.uri = uri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public String getUploaded_by() {
        return uploaded_by;
    }

    public void setUploaded_by(String uploaded_by) {
        this.uploaded_by = uploaded_by;
    }

    public boolean isDownloading() { return downloading; }

    public void setDownloading(boolean downloading) { this.downloading = downloading; }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return "File{" +
                "id='" + id + '\'' +
                ", created_at='" + created_at + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", uploaded_by='" + uploaded_by + '\'' +
                ", link='" + link + '\'' +
                ", uri='" + uri + '\'' +
                ", downloading=" + downloading +
                ", selected=" + selected +
                '}';
    }
}
