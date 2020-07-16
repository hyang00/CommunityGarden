package com.example.finalproject.models;

import java.util.ArrayList;
import java.util.List;

public class UserEvents {
    private List<String> eventsAttending;
    private List<String> eventsHosting;


    public UserEvents(){

    }

    public UserEvents(boolean mine){
        eventsAttending = new ArrayList<>();
        //eventsAttending.add("hi");
        eventsHosting = new ArrayList<>();
    }

    public List<String> getEventsAttending() {
        return eventsAttending;
    }

    public void setEventsAttending(List<String> eventsAttending) {
        this.eventsAttending = eventsAttending;
    }

    public List<String> getEventsHosting() {
        return eventsHosting;
    }

    public void setEventsHosting(List<String> eventsHosting) {
        this.eventsHosting = eventsHosting;
    }
}
