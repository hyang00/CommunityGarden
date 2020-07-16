package com.example.finalproject.models;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.database.IgnoreExtraProperties;

import org.parceler.Parcel;

//@IgnoreExtraProperties
@Parcel
public class Event {
    private String eventId;
    private String author; // user id
    private String title;
    private String description;
    private String address;
    private String time;
    private List<String> attendees;

    public Event(){

    }

    public Event(String eventId, String author, String title, String description, String address, String time){
        //this.eventId = eventId;
        this.author = author;
        this.title = title;
        this.description = description;
        this.address = address;
        this.time = time;
        attendees = new ArrayList<>();
        attendees.add("hi");
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

    public List<String> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<String> attendees) {
        this.attendees = attendees;
    }

    public void addAttendee(String uid){
        attendees.add(uid);
    }

    public boolean isAttending(String uid){
        return attendees.contains(uid);
    }
}
