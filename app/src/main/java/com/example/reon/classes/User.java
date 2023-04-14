package com.example.reon.classes;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.Objects;

@IgnoreExtraProperties
public class User {
    private String id;
    private String email;
    private String name;
    private String about;
    private ArrayList<String> roomList;

    @Exclude
    private boolean admin = false;

    public User() {}

    public User(String id, String email, String name, String about, ArrayList<String> roomList) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.about = about;
        this.roomList = roomList;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public boolean isAdmin() { return admin; }

    public void setAdmin(boolean admin) { this.admin = admin; }

    @Override
    public boolean equals(Object o) {
        if (o instanceof User) {
            return (Objects.equals(id, ((User) o).id));
        }
        else {
            return false;
        }
    }

//    @Override
//    public String toString() {
//        return "User{" +
//                "id='" + id + '\'' +
//                ", email='" + email + '\'' +
//                ", name='" + name + '\'' +
//                ", about='" + about + '\'' +
//                ", roomList=" + roomList +
//                ", admin=" + admin +
//                '}';
//    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                '}';
    }

}
