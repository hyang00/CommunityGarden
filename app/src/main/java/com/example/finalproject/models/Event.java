package com.example.finalproject.models;

import android.content.Context;
import android.net.Uri;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import org.parceler.Parcel;

import java.util.HashMap;
import java.util.Map;

import static com.example.finalproject.Common.NO_ATTENDEES_CAP_SET;

@IgnoreExtraProperties
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
    private Map<String, Boolean> tags = new HashMap<>();
    private Location location;
    private Long maxAttendees;

    public Event() {

    }

    public Event(String author, String title, String description, String imageUrl, String date, String time, String address, Long maxAttendees, Context context) {
        this.author = author;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.date = date;
        this.time = time;
        this.location = new Location(address, context);
        this.maxAttendees = maxAttendees;
    }

    public Event(String title, String description, String address, String date, String time, Long maxAttendees, Uri downloadUri, Context context) {
        this.title = title;
        this.description = description;
        this.imageUrl = downloadUri.toString();
        this.date = date;
        this.time = time;
        this.maxAttendees = maxAttendees;
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

    public void removeAttendee(String uid) {
        attendees.remove(uid);
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

    @Exclude
    public int getNumberofAttendees() {
        return attendees.size();
    }

    public long spotsLeft() {
        return (maxAttendees - getNumberofAttendees());
    }

    public boolean isEventFull() {
        if (!maxAttendees.equals(NO_ATTENDEES_CAP_SET)) {
            return (getNumberofAttendees() >= maxAttendees);
        }
        return false;
    }

    public Long getMaxAttendees() {
        return maxAttendees;
    }

    public void setMaxAttendees(Long maxAttendees) {
        this.maxAttendees = maxAttendees;
    }

    public Map<String, Boolean> getTags() {
        return tags;
    }

    public void setTags(Map<String, Boolean> tags) {
        this.tags = tags;
    }
}
