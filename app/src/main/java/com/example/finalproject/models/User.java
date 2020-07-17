package com.example.finalproject.models;

import org.parceler.Parcel;

@Parcel
public class User {
    private String uid;
    private String screenName;
    private String location;
    private String bio;
    private String profileImageUrl;

    public User(){

    }

    public User(String screenName, String location, String bio, String profileImageUrl){
        //this.uid = uid;
        this.screenName = screenName;
        this.location = location;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
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

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
