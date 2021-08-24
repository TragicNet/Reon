package com.example.reon.classes;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;

@IgnoreExtraProperties
public class User {
    private String id;
    private String email;
    private String name;
    private String about;
    private ArrayList<String> roomList;

    public User() {}

    public User(String id, String email, String name, String about, ArrayList<String> roomList) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.about = about;
        this.roomList = roomList;
    }

    @Exclude
    @NonNull
    @Override
    public String toString() {
        return String.format("\nId: %s\nEmail: %s\nName: %s\nAbout: %s\nRooms: %s", id, email, name, about, roomList);
    }

    public void setId(String id) {
        this.id = id;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public ArrayList<String> getRoomList() {
        return roomList;
    }

    public void setRoomList(ArrayList<String> roomList) {
        this.roomList = roomList;
    }
}
