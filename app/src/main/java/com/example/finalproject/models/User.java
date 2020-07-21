package com.example.finalproject.models;

import android.content.Context;
import android.location.Address;

import com.google.firebase.database.IgnoreExtraProperties;

import org.parceler.Parcel;

@IgnoreExtraProperties
@Parcel
public class User {
    private String uid;
    private String screenName;
    private String bio;
    private String profileImageUrl;
    private Location location;

    public User(){

    }

    public User(String screenName, String profileImageUrl){
        this.screenName = screenName;
        this.profileImageUrl = profileImageUrl;
    }

    public User(String screenName, String bio, String profileImageUrl, String address, Context context){
        this.screenName = screenName;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
        this.location = new Location(address, context);
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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}


