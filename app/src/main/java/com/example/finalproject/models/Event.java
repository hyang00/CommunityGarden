package com.example.finalproject.models;

import android.content.Context;

import org.parceler.Parcel;

import java.util.HashMap;
import java.util.Map;

//@IgnoreExtraProperties
@Parcel
public class Event {
    private String eventId;
    private String author; // user id
    private String title;
    private String description;
    private String imageUrl;
    private String date;
    private String time;
    private Map<String, Boolean> attendees = new HashMap<>();
    private Location location;

    public Event() {

    }

    public Event(String eventId, String author, String title, String description, String imageUrl, String date, String time, String address, Context context) {
        //this.eventId = eventId;
        this.author = author;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.date = date;
        this.time = time;
        this.location = new Location(address, context);
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
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

    public boolean isAttending(String uid) {
        return attendees.containsKey(uid);
    }

    public void addAttendee(String uid) {
        attendees.put(uid, true);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
