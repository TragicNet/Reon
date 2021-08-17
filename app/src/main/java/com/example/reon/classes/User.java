package com.example.reon.classes;

import java.util.ArrayList;

public class User {
    private String id;
    private String name;
    private String about;
    private ArrayList<String> roomList;

    public User(){
    }

    public User(String id, String name, String about, ArrayList<String> roomList) {
        super();
        this.id = id;
        this.name = name;
        this.about = about;
        this.roomList = roomList;
    }

    public String getId() {
        return id;
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
