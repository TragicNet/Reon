package com.example.reon.classes;

import java.util.ArrayList;

public class Folder {
    private String id;
    private String created_at;
    private String name;
    private String link;
    private String created_by;
    private ArrayList<String> filesList;

    public Folder() {}

    public Folder(String id, String created_at, String name, String link, String created_by, ArrayList<String> filesList) {
        this.id = id;
        this.created_at = created_at;
        this.name = name;
        this.link = link;
        this.created_by = created_by;
        this.filesList = filesList;
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

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public ArrayList<String> getFilesList() {
        return filesList;
    }

    public void setFilesList(ArrayList<String> filesList) {
        this.filesList = filesList;
    }
}
