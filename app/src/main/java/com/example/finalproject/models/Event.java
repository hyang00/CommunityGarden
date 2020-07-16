package com.example.finalproject.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.firebase.database.IgnoreExtraProperties;

import org.parceler.Parcel;

//@IgnoreExtraProperties
@Parcel
public class Event {
    private String eventId;
    private String author; // user id
    private String title;
    private String description;
    private String imageUrl;
    private String address;
    private String time;
    private Map<String, Boolean> attendees = new HashMap<>();
    //private ArrayList<String> attendees = new ArrayList<>();

    public Event(){

    }

    public Event(String eventId, String author, String title, String description, String imageUrl, String address, String time){
        //this.eventId = eventId;
        this.author = author;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.address = address;
        this.time = time;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId){
        this.eventId = eventId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Map<String, Boolean> getAttendees() {
        return attendees;
    }

    public void setAttendees(Map<String, Boolean> attendees) {
        this.attendees = attendees;
    }

    public boolean isAttending(String uid){
        return attendees.containsKey(uid);
    }

    public void addAttendee(String uid){
        attendees.put(uid, true);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
