package com.example.finalproject.models;

import java.util.List;

public class Event {
    private String author; // user id
    private String title;
    private String description;
    private String location;
    private String time;
    private List<String> attendees;

    public Event(){

    }

    public Event(String author, String title, String description, String location, String time, List<String> attendees){
        this.author = author;
        this.title = title;
        this.description = description;
        this.location = location;
        this.time = time;
        this.attendees = attendees;
    }
}
