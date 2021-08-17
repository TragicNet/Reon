package com.example.reon.classes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Room {
    private String id;
    private String created_at;
    private String name;
    private String description;
    private ArrayList<String> adminList;
    private ArrayList<String> memberList;

    public Room() {
    }

    public Room(String id, Date created_at, String name, String description, ArrayList<String> adminList, ArrayList<String> memberList) {
        super();
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US);
        this.id = id;
        this.created_at = dateTimeFormat.format(created_at);
        this.name = name;
        this.description = description;
        this.adminList = adminList;
        this.memberList = memberList;
    }

    public String getId() {
        return id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US);
        this.created_at = dateTimeFormat.format(created_at);
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
}
