package com.example.finalproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.finalproject.adapters.EventsAdapter;
import com.example.finalproject.fragments.HostEventFragment;
import com.example.finalproject.models.Event;
import com.example.finalproject.models.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class DatabaseClient {
    private static final String KEY_POSTS = "Posts";
    private static final String KEY_ATTENDEES = "attendees";
    private static final String KEY_PROFILE = "Profiles";
    private static final String TAG = "Database Client";
    private final static DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private final static String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private final static StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    // Add a new event to the database
    public static void postEvent(final Context context, String title, String description, String location, String date, String time, Uri downloadUri){
        String key = database.child(KEY_POSTS).push().getKey();
        Event event = new Event(key, uid, title, description, downloadUri.toString(), location, date, time);
        database.child(KEY_POSTS).child(key).setValue(event).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Posted Successfully", Toast.LENGTH_SHORT).show();
            }
        });
        //database.child("UserEvents").child(author).child("eventsHosting").child(key).setValue(true);
    }

    // Add a new user profile to the database
    public static void createUser(String name, String bio, String address){
        User user = new User (name, bio, address);
        database.child(KEY_PROFILE).child(uid).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "profile added to database");
            }
        });
    }
    // Adds user to attendees section of post
    public static void rsvpUser(final Event event, Context context){
        DatabaseReference attendeesRef = database.child(KEY_POSTS).child(event.getEventId()).child(KEY_ATTENDEES);
        // check if the user has already rsvp'd
        if (!event.isAttending(uid)){
            attendeesRef.child(uid).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    // update local if successful in updating database
                    event.addAttendee(uid);
                }
            });
            Toast.makeText(context, "Successfully Registered", Toast.LENGTH_SHORT).show();
            //database.child("UserEvents").child(uid).child("eventsAttending").child(event.getEventId()).setValue(true);

        } else {
            Toast.makeText(context, "Already Registered", Toast.LENGTH_SHORT).show();
        }

    }

    // Get User profile
    public static void getUserProfile(ValueEventListener listener, String userId){
        Query ref = database.child(KEY_PROFILE).child(userId);
        ref.addListenerForSingleValueEvent(listener);
    }

//    // Get User profile
//    public static void getUserProfile(String userId){
//        Query ref = database.child(KEY_PROFILE).child(userId);
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot singleSnapshot : snapshot.getChildren()){
//                    User user = singleSnapshot.getValue(User.class);
//                    EventDetailsActivity.setUser(user);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }

    // Query events for main Event feed
    public static void queryEvents(final EventsAdapter adapter, final SwipeRefreshLayout swipeContainer){
        Query ref = database.child(KEY_POSTS).orderByKey();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapter.clear();
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Event event = singleSnapshot.getValue(Event.class);
                    event.setEventId(singleSnapshot.getKey());
                    adapter.add(event);
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
    // Query events for events that User is attending
    public static void queryUserAttendingEvents(ValueEventListener listener){
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        Query ref = database.child(KEY_POSTS).orderByKey();
        ref.addListenerForSingleValueEvent(listener);
    }
    // Query events for events that User is attending
//    public static void queryUserAttendingEvents(final EventsAdapter adapter, final SwipeRefreshLayout swipeContainer){
//        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
//        Query ref = database.child(KEY_POSTS).orderByKey();
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                adapter.clear();
//                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
//                    Event event = singleSnapshot.getValue(Event.class);
//                    event.setEventId(singleSnapshot.getKey());
//                    if(event.isAttending(uid)){
//                        adapter.add(event);
//                    }
//                }
//                adapter.notifyDataSetChanged();
//                swipeContainer.setRefreshing(false);
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.e(TAG, "onCancelled", databaseError.toException());
//            }
//        });
//    }

    //upload the image to firebase storage, can also use listener to get download url
    public static void uploadImage(OnCompleteListener<Uri> listener, Uri filePath, final Context context ){
        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            final StorageReference ref = storageRef.child("images/"+ UUID.randomUUID().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(context, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(context, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(listener);

        }
    }


}
