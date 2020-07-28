package com.example.finalproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

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

import static com.example.finalproject.Common.MAIN_ACT_FRG_TO_LOAD_KEY;
import static com.example.finalproject.Common.USER_EVENTS_FRAGMENT;

public class DatabaseClient {
    private static final String TAG = "Database Client";
    private static final String KEY_POSTS = "Posts";
    private static final String KEY_ATTENDEES = "attendees";
    private static final String KEY_PROFILE = "Profiles";
    private static final String KEY_EVENT_DATE = "date";
    private static final String KEY_LOCALITY = "location/locality";
    private final static DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private final static StorageReference storageRef = FirebaseStorage.getInstance().getReference();

    // Add a new event to the database
    //  TODO: reduce method parameters
    public static void postEvent(final Context context, String title, String description, String location, String date, String time, Uri downloadUri) {
        String key = database.child(KEY_POSTS).push().getKey();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Event event = new Event(key, uid, title, description, downloadUri.toString(), date, time, location, context);
        database.child(KEY_POSTS).child(key).setValue(event).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, "Posted Successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void isNewUser(ValueEventListener listener) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database.child(KEY_PROFILE).child(uid).addValueEventListener(listener);
    }

    // Add a new user profile to the database
    public static void createUser(String name, String bio, String profileImageUrl, String address, Context context) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        User user = new User(name, bio, profileImageUrl, address, context);
        Log.i(TAG, "curr User uid: " + uid);
        database.child(KEY_PROFILE).child(uid).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "profile added to database");
            }
        });
    }

    // Adds user to attendees section of post
    public static void rsvpUser(OnCompleteListener<Void> listener, final Event event, final Context context) {
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference attendeesRef = database.child(KEY_POSTS).child(event.getEventId()).child(KEY_ATTENDEES);
        // check if the user has already rsvp'd
        if (!event.isAttending(uid)) {
            attendeesRef.child(uid).setValue(true).addOnCompleteListener(listener);
        } else {
            Toast.makeText(context, "Already Registered", Toast.LENGTH_SHORT).show();
        }
    }

    // removes user from attendees section of post
    public static void cancelUserRegistration(final Event event, final Context context) {
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference attendeesRef = database.child(KEY_POSTS).child(event.getEventId()).child(KEY_ATTENDEES);
        // check if the user has already rsvp'd
        if (event.isAttending(uid)) {
            attendeesRef.child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    // update local if successful in updating database
                    Toast.makeText(context, "Successfully Removed Registration", Toast.LENGTH_SHORT).show();
                    event.removeAttendee(uid);
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra(MAIN_ACT_FRG_TO_LOAD_KEY, USER_EVENTS_FRAGMENT);
                    context.startActivity(intent);
                }
            });


        } else {
            Toast.makeText(context, "Not Registered", Toast.LENGTH_SHORT).show();
        }
    }

    // Get logged in user profile
    public static void getCurrUserProfile(ValueEventListener listener) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserProfile(listener, uid);
    }

    // Get User profile
    public static void getUserProfile(ValueEventListener listener, String userId) {
        Query ref = database.child(KEY_PROFILE).child(userId);
        ref.addListenerForSingleValueEvent(listener);
    }

    // Query events (sorted by event date)
    public static void queryEvents(ValueEventListener listener) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        Query ref = database.child(KEY_POSTS).orderByChild(KEY_EVENT_DATE);
        ref.addListenerForSingleValueEvent(listener);
    }

    // Query events w/ same locale as current user
    public static void queryEventsNearby(final ValueEventListener listener) {
        getCurrUserProfile(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User currUser = snapshot.getValue(User.class);
                DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                Query ref = database.child(KEY_POSTS).orderByChild(KEY_LOCALITY).equalTo(currUser.getLocation().getLocality());
                Log.i(TAG, currUser.getLocation().getLocality());
                ref.addListenerForSingleValueEvent(listener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Query events given a locality
    public static void queryEventsNearby(final ValueEventListener listener, String locality) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        Query ref = database.child(KEY_POSTS).orderByChild(KEY_LOCALITY).equalTo(locality);
        Log.i(TAG, locality);
        ref.addListenerForSingleValueEvent(listener);
    }

    //upload the image to firebase storage, can also use listener to get download url
    public static void uploadImage(OnCompleteListener<Uri> listener, Uri filePath, final Context context) {
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            final StorageReference ref = storageRef.child("images/" + UUID.randomUUID().toString());
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
                            Toast.makeText(context, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
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
