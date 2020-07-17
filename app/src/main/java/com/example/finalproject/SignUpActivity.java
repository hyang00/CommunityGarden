package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


import com.bumptech.glide.Glide;
import com.example.finalproject.models.Event;
import com.example.finalproject.models.User;
import com.example.finalproject.models.UserEvents;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.parceler.Parcels;

public class SignUpActivity extends AppCompatActivity {

    public static final String TAG = "SignUpActivity";

    private EditText etName;
    private EditText etBio;
    private EditText etAddress;
    private ImageView ivProfilePic;
    private Button btnFinish;
    private DatabaseReference mDatabase;
    private FirebaseAuth firebaseAuth;
    private String profileImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        setContentView(R.layout.activity_sign_up);
        etName = findViewById(R.id.etName);
        etBio = findViewById(R.id.etBio);
        etAddress = findViewById(R.id.etAddress);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        btnFinish = findViewById(R.id.btnFinish);

        // If user making account w/ Facebook, prepopulate fields
        if (getIntent().hasExtra(User.class.getSimpleName())){
            User user = (User) Parcels.unwrap(getIntent().getParcelableExtra(User.class.getSimpleName()));
            etName.setText(user.getScreenName());
            profileImageUrl = user.getProfileImageUrl();
            Glide.with(SignUpActivity.this).load(profileImageUrl).into(ivProfilePic);
        }

        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = etName.getText().toString();
                String bio = etBio.getText().toString();
                String address = etAddress.getText().toString();
                // Create user profile in database
                DatabaseClient.createUser(name, address, bio, profileImageUrl);
                // Go to main page after finished registering new user
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}