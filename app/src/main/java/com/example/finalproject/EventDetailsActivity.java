package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.finalproject.adapters.AdditionalPhotosAdapter;
import com.example.finalproject.models.AdditionalPhoto;
import com.example.finalproject.models.Event;
import com.example.finalproject.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Map;

import static com.example.finalproject.MapsUrlClient.launchGoogleMaps;
import static com.example.finalproject.MapsUrlClient.setGoogleMapThumbnail;
import static com.example.finalproject.TimeAndDateFormatter.formatDateWithDayOfWeek;


public class EventDetailsActivity extends AppCompatActivity {
    public static final String TAG = "EventDetailsActivity";
    protected Event event;
    private TextView tvTitle;
    private ImageView ivEventPhoto;
    private ImageView ivProfilePic;
    private TextView tvName;
    private TextView tvBio;
    private TextView tvDate;
    private TextView tvTime;
    private TextView tvDescription;
    private TextView tvAddress;
    private ImageView ivMap;
    private TextView tvGoing;
    private TextView tvAdditionalPhotos;
    protected ExtendedFloatingActionButton fab;
    ArrayList<AdditionalPhoto> additionalPhotos;
    private RecyclerView rvAdditionalPhotos;
    AdditionalPhotosAdapter additionalPhotosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        event = (Event) Parcels.unwrap(getIntent().getParcelableExtra(Event.class.getSimpleName()));

        tvTitle = findViewById(R.id.tvTitle);
        ivEventPhoto = findViewById(R.id.ivEventPhoto);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        tvName = findViewById(R.id.tvName);
        tvBio = findViewById(R.id.tvBio);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        tvDescription = findViewById(R.id.tvDescription);
        tvAddress = findViewById(R.id.tvAddress);
        fab = findViewById(R.id.fab);
        ivMap = findViewById(R.id.ivMap);
        tvGoing = findViewById(R.id.tvGoing);
        tvAdditionalPhotos = findViewById(R.id.tvAdditionalPhotos);

        setHostProfileFields();
        tvTitle.setText(event.getTitle());
        if (event.getImageUrl() != null) {
            Glide.with(EventDetailsActivity.this).load(event.getImageUrl()).into(ivEventPhoto);
        }

        rvAdditionalPhotos = (RecyclerView) findViewById(R.id.rvAdditionalPhotos);
        additionalPhotos = new ArrayList<>();
        additionalPhotosAdapter = new AdditionalPhotosAdapter(this, additionalPhotos);
        rvAdditionalPhotos.setAdapter(additionalPhotosAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        rvAdditionalPhotos.setLayoutManager(gridLayoutManager);
        queryAdditionalPhotos();

        tvDate.setText(formatDateWithDayOfWeek(event.getDate()));
        tvTime.setText(event.getTime());
        tvDescription.setText(event.getDescription());
        tvAddress.setText(event.getLocation().getWrittenAddress());
        tvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchGoogleMaps(EventDetailsActivity.this, event);
            }
        });
        setGoogleMapThumbnail(EventDetailsActivity.this, event.getLocation().getWrittenAddress(), ivMap);
        ivMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchGoogleMaps(EventDetailsActivity.this, event);
            }
        });
        if (event.getNumberofAttendees() > 0) {
            tvGoing.setText(event.getNumberofAttendees() + " going");
        }
        setUpRegistrationButton();
    }

    private void queryAdditionalPhotos() {
        DatabaseClient.queryAdditionalPhotos(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                additionalPhotosAdapter.clear();
                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                    Map<String, String> info = (Map<String, String>) singleSnapshot.getValue();
                    AdditionalPhoto additionalPhoto = new AdditionalPhoto(info.get("imageUrl"), info.get("label"));
                    additionalPhotosAdapter.add(additionalPhoto);
                }
                if (additionalPhotosAdapter.getItemCount() > 0) {
                    tvAdditionalPhotos.setVisibility(View.VISIBLE);
                }
                additionalPhotosAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.toString());
            }
        }, event);
    }

    protected void setUpRegistrationButton() {
        if (event.isEventFull()) {
            fab.setText(R.string.rsvp_button_event_full);
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseClient.rsvpUser(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(EventDetailsActivity.this, "Successfully Registered", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(EventDetailsActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, event, EventDetailsActivity.this);
            }
        });
    }

    private void setHostProfileFields() {
        // Populate host user fields
        DatabaseClient.getUserProfile(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                tvName.setText(user.getScreenName());
                tvBio.setText(user.getBio());
                if (user.getProfileImageUrl() != null) {
                    Glide.with(EventDetailsActivity.this).load(user.getProfileImageUrl()).transform(new CircleCrop()).into(ivProfilePic);
                }
                if (user.getLocation() != null) {
                    Log.i(TAG, user.getLocation().getWrittenAddress());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        }, event.getAuthor());
    }


}