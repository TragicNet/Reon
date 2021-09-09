package com.example.reon.classes;

import java.util.ArrayList;

public class Room {
    private String id;
    private String created_at;
    private String name;
    private String description;
    private String link;
    private ArrayList<String> adminList;
    private ArrayList<String> memberList;
    private ArrayList<String> folderList;

    public Room() {
    }

    public Room(String id, String created_at, String name, String description, String link,
           ArrayList<String> adminList, ArrayList<String> memberList, ArrayList<String> folderList) {
        super();
        this.id = id;
        this.created_at = created_at;
        this.name = name;
        this.description = description;
        this.link = link;
        this.adminList = adminList;
        this.memberList = memberList;
        this.folderList = folderList;
    }

    public void setId(String id) { this.id = id; }

    public String getId() {
        return id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() { return link; }

    public void setLink(String link) { this.link = link; }

    public ArrayList<String> getAdminList() {
        return adminList;
    }

    public void setAdminList(ArrayList<String> adminList) {
        this.adminList = adminList;
    }

    public ArrayList<String> getMemberList() {
        return memberList;
    }

    public void setMemberList(ArrayList<String> memberList) {
        this.memberList = memberList;
    }

    public ArrayList<String> getFolderList() { return folderList; }

    public void setFolderList(ArrayList<String> folderList) { this.folderList = folderList; }
}
