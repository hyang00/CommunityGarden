package com.example.finalproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.finalproject.models.Event;
import com.example.finalproject.models.User;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.parceler.Parcels;

import java.io.IOException;


public class EventDetailsActivity extends AppCompatActivity {
    public static final String TAG = "EventDetailsActivity";
    protected Event event;
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
    private ImageView ivMap;
    protected ExtendedFloatingActionButton fab;

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

        setHostProfileFields();
        tvTitle.setText(event.getTitle());
        if (event.getImageUrl() != null) {
            Glide.with(EventDetailsActivity.this).load(event.getImageUrl()).into(ivEventPhoto);
        }
        tvDate.setText(event.getDate());
        tvTime.setText(event.getTime());
        tvDescription.setText(event.getDescription());
        tvAddress.setText(event.getLocation().getWrittenAddress());
        tvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchGoogleMaps();
            }
        });
        setGoogleMapThumbnail(event.getLocation().getWrittenAddress());
        ivMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchGoogleMaps();
            }
        });
        setUpRegistrationButton();
    }

    protected void setUpRegistrationButton(){
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseClient.rsvpUser(event, EventDetailsActivity.this);
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

    private void launchGoogleMaps() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Common.MAP_SEARCH_URL_KEY + convertAddressToSearchQuery(event.getLocation().getWrittenAddress())));
        startActivity(intent);
    }

    private void setGoogleMapThumbnail(String address) {
        Log.i(TAG, getString(R.string.google_api_key));
        String url = Common.MAP_STATIC_URL_KEY + convertAddressToSearchQuery(address) + "zoom=14&size=400x300&markers=color:red%7C" + convertAddressToSearchQuery(address) + "&key=" + getString(R.string.api_key);
        Log.i(TAG, url);
        final Request request = new Request.Builder().url(url).build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.i(TAG, response.toString());
                    final Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            ivMap.setImageBitmap(bitmap);
                        }
                    });
                } else {
                    //Handle the error
                }
            }
        });
    }

    //Takes standard address format and converts it into maps query preferred format (spaces -> +, commas -> %2C)
    private static String convertAddressToSearchQuery(String address) {
        String searchQuery = "";
        for (int i = 0; i < address.length(); ++i) {
            char currLetter = address.charAt(i);
            if (currLetter == ' ') {
                searchQuery += "+";
            } else if (currLetter == ',') {
                searchQuery += "%2C";
            } else {
                searchQuery += currLetter;
            }
        }
        return searchQuery;
    }

}