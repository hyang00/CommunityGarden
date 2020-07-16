package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.example.finalproject.models.Event;

import org.parceler.Parcels;

public class EventDetailsActivity extends AppCompatActivity {
    public static final  String TAG = "EventDetailsActivity";
    private Event event;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        event = (Event) Parcels.unwrap(getIntent().getParcelableExtra(Event.class.getSimpleName()));
        Log.i(TAG, event.getTitle());
    }
}