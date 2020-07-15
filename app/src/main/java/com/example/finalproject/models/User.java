package com.example.finalproject.models;

public class User {
    private String uid;
    private String screenName;
    private String location;
    private String bio;

    public User(){

    }

    public User(String screenName, String location, String bio){
        //this.uid = uid;
        this.screenName = screenName;
        this.location = location;
        this.bio = bio;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
