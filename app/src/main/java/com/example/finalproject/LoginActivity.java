package com.example.finalproject;

import android.content.Context;
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

import com.example.finalproject.models.User;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.parceler.Parcels;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 123;

    FirebaseAuth firebaseAuth;
    Context context;
    CallbackManager facebookLoginManager;

    private LoginButton btnFacebookLogin;
    private EditText etPassword;
    private EditText etEmail;
    private Button btnLogin;
    private Button btnSignUp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // initialize context, facebook login manager, and firebase Auth
        facebookLoginManager = CallbackManager.Factory.create();
        context = getApplicationContext();
        firebaseAuth = FirebaseAuth.getInstance();

        // associate views with java variables
        btnFacebookLogin = findViewById(R.id.btnFacebookLogin);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);

        if (firebaseAuth.getCurrentUser() != null) {  // if already signed in go to home page
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
            btnFacebookLogin.setReadPermissions(Arrays.asList("email", "public_profile"));

            // register call back for logging in
            LoginManager.getInstance().registerCallback(facebookLoginManager,
                    new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            handleFacebookAccessToken(loginResult.getAccessToken());
                        }

                        @Override
                        public void onCancel() {
                        }

                        @Override
                        public void onError(FacebookException exception) {
                            Log.e(TAG, exception.toString());
                        }
                    });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Pass activity result back to Facebook SDK
        facebookLoginManager.onActivityResult(requestCode, resultCode, data);
    }

    /* After successful user sign in, get a facebook access token for the signed-in user, exchange it for a firebase credential
    and authenticate w/ Firebase using the firebase credential*/
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final FirebaseUser user = firebaseAuth.getCurrentUser();
                            // Q: should it just be returning true/false, TODO: also so so slow, how to fix or just add loading bar
                            DatabaseClient.isNewUser(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        goToMainActivity(); // if a returning user go to home page
                                    } else {
                                        createFacebookUser(user);  // otherwise populate user profile in database
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Create new user profile in firebase when new user logs in through facebook
    private void createFacebookUser(FirebaseUser firebaseUser) {
        String firebaseUserDisplayName = firebaseUser.getDisplayName();
        String firebaseUserProfilePhotoURL = "https://graph.facebook.com" + firebaseUser.getPhotoUrl().getPath() + "?type=large";
        User user = new User(firebaseUserDisplayName, firebaseUserProfilePhotoURL);
        goToSignUpActivity(user);
    }

    // Create new user using email/password method w/ firebase auth
    private void createUser(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            goToSignUpActivity();
                        } else {
                            // Show failure message
                            Toast toast = Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                            // reset email/password fields
                            etEmail.setText("");
                            etPassword.setText("");
                        }

                        // ...
                    }
                });
    }


    // Login user w/ firebase email + password option
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
                            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                            // reset email/password fields
                            etEmail.setText("");
                            etPassword.setText("");
                        }
                    }
                });
    }

    // Launch intent to go to main activity (home page w/ feed + etc)
    public void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // Launch intent to go to page that creates new user profile
    private void goToSignUpActivity() {
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }

    // Launch intent to go to page that creates new user profile w/ some info
    private void goToSignUpActivity(User user) {
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        intent.putExtra(User.class.getSimpleName(), Parcels.wrap(user));
        startActivity(intent);
        finish();
    }

}
