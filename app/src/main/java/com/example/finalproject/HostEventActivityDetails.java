package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.finalproject.adapters.UsersAdapter;
import com.example.finalproject.models.Event;
import com.example.finalproject.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import static com.example.finalproject.MapsUrlClient.launchGoogleMaps;
import static com.example.finalproject.MapsUrlClient.setGoogleMapThumbnail;
import static com.example.finalproject.TimeAndDateFormatter.formatDateWithDayOfWeek;

@SuppressWarnings("ALL")
public class HostEventActivityDetails extends AppCompatActivity {

    private static final String TAG = "HostEventActDetails";

    private Event event;
    private TextView tvTitle;
    private ImageView ivEventPhoto;
    private TextView tvDate;
    private TextView tvTime;
    private TextView tvDescription;
    private TextView tvAddress;
    private ImageView ivMap;
    private TextView tvAttendees;
    private RecyclerView rvAttendees;
    private UsersAdapter adapter;
    private List<User> users;

    @SuppressWarnings("RedundantCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_event_details);

        //noinspection RedundantCast
        event = (Event) Parcels.unwrap(getIntent().getParcelableExtra(Event.class.getSimpleName()));

        tvTitle = findViewById(R.id.tvTitle);
        ivEventPhoto = findViewById(R.id.ivEventPhoto);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvDescription = findViewById(R.id.tvDescription);
        tvAddress = findViewById(R.id.tvAddress);
        ivMap = findViewById(R.id.ivMap);
        tvAttendees = findViewById(R.id.tvAttendees);


        tvTitle.setText(event.getTitle());
        if (event.getImageUrl() != null) {
            Glide.with(HostEventActivityDetails.this).load(event.getImageUrl()).into(ivEventPhoto);
        }
        tvDate.setText(formatDateWithDayOfWeek(event.getDate()));
        tvTime.setText(event.getTime());
        tvDescription.setText(event.getDescription());
        tvAddress.setText(event.getLocation().getWrittenAddress());
        tvAttendees.setText(event.getNumberofAttendees() + " Attendees");

        rvAttendees = findViewById(R.id.rvAttendees);
        users = new ArrayList<>();
        adapter = new UsersAdapter(HostEventActivityDetails.this, users);
        rvAttendees.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(HostEventActivityDetails.this);
        rvAttendees.setLayoutManager(linearLayoutManager);
        queryAttendees();

        tvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchGoogleMaps(HostEventActivityDetails.this, event);
            }
        });
        setGoogleMapThumbnail(HostEventActivityDetails.this, event.getLocation().getWrittenAddress(), ivMap);
        ivMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchGoogleMaps(HostEventActivityDetails.this, event);
            }
        });
    }

    private void queryAttendees() {
        adapter.clear();
        for (String uid : event.getAttendees().keySet()) {
            DatabaseClient.getUserProfile(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    adapter.add(user);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            }, uid);
        }
    }
}