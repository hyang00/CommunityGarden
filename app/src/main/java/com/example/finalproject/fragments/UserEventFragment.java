package com.example.finalproject.fragments;

import android.util.Log;

import com.example.finalproject.models.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserEventFragment extends EventFragment{
    private FirebaseAuth firebaseAuth;
//    @Override
//    protected void queryEvents() {
//        String uid = firebaseAuth.getInstance().getCurrentUser().getUid();
//        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
//        DatabaseReference userEventsRef = database.child("UserEvents").child(uid).child("eventsAttending");
//        //Query phoneQuery = ref.orderByChild(phoneNo).equalTo("+923336091371");
////        userEventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
////            @Override
////            public void onDataChange(DataSnapshot dataSnapshot) {
////                adapter.clear();
////                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
////                    Object postId = singleSnapshot.getValue(true);
////                    Log.i(TAG, event.getTitle() + ": " + event.getEventId());
////                }
////                adapter.notifyDataSetChanged();
////                swipeContainer.setRefreshing(false);
////            }
////            @Override
////            public void onCancelled(DatabaseError databaseError) {
////                Log.e(TAG, "onCancelled", databaseError.toException());
////            }
////        });
//    }
}
