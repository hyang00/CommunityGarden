package com.example.finalproject.fragments;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.finalproject.DatabaseClient;
import com.example.finalproject.models.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserEventFragment extends EventFragment{
    private static final  String TAG = "UserEventFragment";
    @Override
    protected void queryEvents() {
        DatabaseClient.queryUserAttendingEvents(adapter, swipeContainer);
    }
}
