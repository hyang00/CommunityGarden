package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import com.example.finalproject.models.User;
import com.example.finalproject.models.UserEvents;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    public static final String TAG = "SignUpActivity";

    private EditText etName;
    private EditText etBio;
    private EditText etAddress;
    private Button btnFinish;
    private DatabaseReference mDatabase;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        setContentView(R.layout.activity_sign_up);
        etName = findViewById(R.id.etName);
        etBio = findViewById(R.id.etBio);
        etAddress = findViewById(R.id.etAddress);
        btnFinish = findViewById(R.id.btnFinish);
        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = etName.getText().toString();
                String bio = etBio.getText().toString();
                String address = etAddress.getText().toString();
                createUser(name, bio, address);
                // Go to main page after finished registering new user
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void createUser(String name, String bio, String address) {
        String uid = firebaseAuth.getInstance().getCurrentUser().getUid();
        User user = new User (name, bio, address);
        mDatabase.child("Profiles").child(uid).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "profile added to database");
            }
        });
    }
}