package com.example.finalproject.fragments;

import android.util.Log;

import androidx.annotation.NonNull;

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
    private FirebaseAuth firebaseAuth;
    private ArrayList<String> postKeys;

    @Override
    protected void queryEvents() {

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        Query ref = database.child("Posts").orderByKey();
        //Query phoneQuery = ref.orderByChild(phoneNo).equalTo("+923336091371");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapter.clear();
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Event event = singleSnapshot.getValue(Event.class);
                    event.setEventId(singleSnapshot.getKey());
                    if(event.isAttending(firebaseAuth.getInstance().getCurrentUser().getUid())){
                        adapter.add(event);
                    }
                    Log.i(TAG, event.getTitle() + ": " + event.getEventId());
                }
                adapter.notifyDataSetChanged();
                swipeContainer.setRefreshing(false);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
    }
    //    @Override
//    protected void queryEvents() {
//        postKeys = new ArrayList<>();
//        String uid = firebaseAuth.getInstance().getCurrentUser().getUid();
//        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
//        DatabaseReference userEventsRef = database.child("UserEvents").child(uid).child("eventsAttending");
//        //Query phoneQuery = ref.orderByChild(phoneNo).equalTo("+923336091371");
//        userEventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
//                    String postkey = singleSnapshot.getKey();
//                    Object postId = singleSnapshot.getValue(true);
//                    postKeys.add(postkey);
//                    Log.i(TAG, postkey);
//                }
//                for (int i = 0; i<postKeys.size(); ++i){
//                    DatabaseReference myRef = database.child("Posts").child(postKeys.get(i));
//                    //Query query = database.child("Posts").equalTo(postKeys.get(i));
//                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            adapter.clear();
//                            Event event = snapshot.getValue(Event.class);
////                            for(DataSnapshot singleSnapshot: snapshot.getChildren()){
////                                Event event = singleSnapshot.getValue(Event.class);
////                                event.setEventId(singleSnapshot.getKey());
////                                Log.i(TAG, event.getTitle() + ": " + event.getEventId());
////                                adapter.add(event);
////                            }
//                            event.setEventId(snapshot.getKey());
//                            Log.i(TAG, event.getTitle() + ": " + event.getEventId());
//                            adapter.add(event);
//                            //adapter.notifyDataSetChanged();
//
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//                        }
//                    });
//                }
//                swipeContainer.setRefreshing(false);
//
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.e(TAG, "onCancelled", databaseError.toException());
//            }
//        });
//        Log.i(TAG, "" + postKeys.size());
//
//    }
}
