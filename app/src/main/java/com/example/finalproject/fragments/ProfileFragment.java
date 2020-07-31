package com.example.finalproject.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.finalproject.DatabaseClient;
import com.example.finalproject.EditProfileActivity;
import com.example.finalproject.LoginActivity;
import com.example.finalproject.R;
import com.example.finalproject.models.Event;
import com.example.finalproject.models.User;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.parceler.Parcels;


public class ProfileFragment extends Fragment {

    private static final String TAG = "Profile Fragment";

    private ImageView ivProfilePic;
    private TextView tvName;
    private TextView tvBio;
    private TextView tvLocation;
    private Button btnEditProfile;
    private TextView tvAttendedCount;
    private TextView tvHostedCount;
    private TextView tvUsersMet;
    private Toolbar toolbar;
    private User user;

    FirebaseAuth firebaseAuth;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        tvName = view.findViewById(R.id.tvName);
        tvBio = view.findViewById(R.id.tvBio);
        tvLocation = view.findViewById(R.id.tvLocation);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        tvAttendedCount = view.findViewById(R.id.tvAttendedCount);
        tvHostedCount = view.findViewById(R.id.tvHostedCount);
        tvUsersMet = view.findViewById(R.id.tvUsersMet);
        toolbar = view.findViewById(R.id.toolBar);

        setProfileFields();
        countAttendedAndHosted();
        toolbar.inflateMenu(R.menu.menu_profile);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.miLogout:
                        firebaseAuth.getInstance().signOut();
                        LoginManager.getInstance().logOut();
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), EditProfileActivity.class);
                intent.putExtra(User.class.getSimpleName(), Parcels.wrap(user));
                startActivity(intent);
            }
        });
    }

    private void countAttendedAndHosted() {
        DatabaseClient.queryPastEvents(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int attended = 0;
                int hosted = 0;
                int usersMet = 0;
                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                    Event event = singleSnapshot.getValue(Event.class);
                    event.setEventId(singleSnapshot.getKey());
                    if (hosted(event)) {
                        hosted++;
                        usersMet += event.getNumberofAttendees();
                    } else if (attended(event)) {
                        attended++;
                        usersMet += (event.getNumberofAttendees() - 1);
                    }
                }
                String strAttended = "<big>" + attended + "</big>" + "<small> Events Attended</small>";
                String strHosted = "<big>" + hosted + "</big>" + "<small> Events Hosted</small>";
                String strUsersMet = "<big>" + usersMet + "</big>" + "<small> Gardeners Met</small>";
                tvAttendedCount.setText(Html.fromHtml(strAttended));
                tvHostedCount.setText(Html.fromHtml(strHosted));
                tvUsersMet.setText(Html.fromHtml(strUsersMet));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Q: is it worth it to separate this out into a method?
    private void setProfileFields() {
        // Populate host user fields
        DatabaseClient.getCurrUserProfile(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                tvName.setText(user.getScreenName());
                String bio = "<b>" + "Bio: " + "</b> " + user.getBio();
                tvBio.setText(Html.fromHtml(bio));
                String location = "<b>" + "Location: " + "</b> " + user.getLocation().getLocality();
                tvLocation.setText(Html.fromHtml(location));
                if (user.getProfileImageUrl() != null) {
                    Glide.with(getActivity()).load(user.getProfileImageUrl()).transform(new CircleCrop()).into(ivProfilePic);
                }
                if (user.getLocation() != null) {
                    Log.i(TAG, user.getLocation().getWrittenAddress());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.toString());
            }
        });
    }

    // check whether the event should be added to the feed
    private boolean hosted(Event event) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return event.getAuthor().equals(uid);
    }

    // check whether the event should be added to the feed
    private boolean attended(Event event) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return event.isAttending(uid);
    }
}