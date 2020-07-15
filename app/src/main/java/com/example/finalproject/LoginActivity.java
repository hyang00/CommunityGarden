package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.example.finalproject.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 123;
    FirebaseAuth firebaseAuth;

    private EditText etPassword;
    private EditText etEmail;
    private Button btnLogin;
    private Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);
        // initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            // already signed in
            goToMainActivity();
        } else {
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String email = etEmail.getText().toString();
                    String password = etPassword.getText().toString();
                    loginUser(email, password);
                }
            });
            btnSignUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String email = etEmail.getText().toString();
                    String password = etPassword.getText().toString();
                    createUser(email, password);
                }
            });
        }
    }

    private void createUser(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            goToSignUpActivity();
                            //FirebaseUser user = firebaseAuth.getCurrentUser();

                            // TODO: Register user in database
                        } else {
                            // Show failure message
                            Toast toast = Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                            // reset email/password fields
                            etEmail.setText("");
                            etPassword.setText("");
                        }

                        // ...
                    }
                });
    }



    private void loginUser(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // successfully signed in
                            goToMainActivity();
                        } else {
                            // Show failure message
                            Toast toast = Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                            // reset email/password fields
                            etEmail.setText("");
                            etPassword.setText("");
                        }
                    }
                });
    }


    public void goToMainActivity(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToSignUpActivity() {
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }

}
