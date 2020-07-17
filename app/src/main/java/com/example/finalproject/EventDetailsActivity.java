package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.finalproject.models.Event;
import com.example.finalproject.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.parceler.Parcels;
import org.w3c.dom.Text;

public class EventDetailsActivity extends AppCompatActivity {
    public static final  String TAG = "EventDetailsActivity";
    private Event event;
    //private static  User user;
    private TextView tvTitle;
    private ImageView ivEventPhoto;
    private ImageView ivProfilePic;
    private TextView tvName;
    private TextView tvBio;
    private TextView tvDate;
    private TextView tvTime;
    private TextView tvDescription;
    private TextView tvAddress;
    private Button btnRSVP;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        event = (Event) Parcels.unwrap(getIntent().getParcelableExtra(Event.class.getSimpleName()));
        Log.i(TAG, event.getTitle());

        tvTitle = findViewById(R.id.tvTitle);
        ivEventPhoto = findViewById(R.id.ivEventPhoto);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        tvName = findViewById(R.id.tvName);
        tvBio = findViewById(R.id.tvBio);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvDescription = findViewById(R.id.tvDescription);
        tvAddress = findViewById(R.id.tvAddress);

        tvTitle.setText(event.getTitle());
        if(event.getImageUrl()!=null){
            Glide.with(EventDetailsActivity.this).load(event.getImageUrl()).into(ivEventPhoto);
        }
        DatabaseClient.getUserProfile(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                tvName.setText(user.getScreenName());
                tvBio.setText(user.getBio());
                // TODO: Set profile pic
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        }, event.getAuthor());
        tvDate.setText(event.getDate());
        tvTime.setText(event.getTime());
        tvDescription.setText(event.getDescription());
        tvAddress.setText(event.getAddress());
    }
//    public static void setUser(User user1){
//        user = user1;
//    }

}