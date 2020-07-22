package com.example.finalproject.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.finalproject.DatabaseClient;
import com.example.finalproject.EventDetailsActivity;
import com.example.finalproject.LoginActivity;
import com.example.finalproject.R;
import com.example.finalproject.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


public class ProfileFragment extends Fragment {

    private static final String TAG = "Profile Fragment";

    private ImageView ivProfilePic;
    private TextView tvName;
    private TextView tvBio;
    private TextView tvLocation;
    private Button btnEditProfile;
    private Button btnLogout;

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
        btnLogout = view.findViewById(R.id.btnLogout);

        setProfileFields();

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
    }

    // Q: is it worth it to separate this out into a method?
    private void setProfileFields() {
        // Populate host user fields
        DatabaseClient.getCurrUserProfile(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                tvName.setText(user.getScreenName());
                String bio = "<b>" + "Bio: " + "</b> " + user.getBio();
                tvBio.setText(Html.fromHtml(bio));
                String location = "<b>" + "Location: " + "</b> " + user.getLocation().getLocality();
                tvLocation.setText(Html.fromHtml(location));
                if (user.getProfileImageUrl() != null) {
                    Glide.with(getContext()).load(user.getProfileImageUrl()).transform(new CircleCrop()).into(ivProfilePic);
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
}