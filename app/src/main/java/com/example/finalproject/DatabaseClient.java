package com.example.finalproject;

import android.util.Log;

import com.example.finalproject.models.Event;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DatabaseClient {

//    public static void writeNewEvent(String author, String title, String description, String location, String time){
//        Event event = new Event(author, title, description, location, time);
//        String key = mDatabase.child("Posts").push().getKey();
//        mDatabase.child("Posts").child(key).setValue(event).addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                Log.i(TAG, "posted successfully");
//            }
//        });
//    }
}
