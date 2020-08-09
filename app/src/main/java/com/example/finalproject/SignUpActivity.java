package com.example.finalproject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.finalproject.models.User;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.parceler.Parcels;

import java.util.Arrays;

@SuppressWarnings("ALL")
public class SignUpActivity extends AppCompatActivity {

    public static final String TAG = "SignUpActivity";

    private EditText etName;
    private EditText etBio;
    private ImageView ivProfilePic;
    private Button btnFinish;
    private String profileImageUrl;
    private FloatingActionButton fabAddProfilePic;
    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //mDatabase = FirebaseDatabase.getInstance().getReference();

        etName = findViewById(R.id.etName);
        etBio = findViewById(R.id.etBio);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        btnFinish = findViewById(R.id.btnFinish);
        fabAddProfilePic = findViewById(R.id.fabAddProfilePic);

        if (!Places.isInitialized()) {
            Places.initialize(SignUpActivity.this, getString(R.string.api_key));
        }
        PlacesClient placesClient = Places.createClient(SignUpActivity.this);

        // Initialize the AutocompleteSupportFragment.
        final AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ADDRESS, Place.Field.NAME));
        autocompleteFragment.setHint("Address");
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                address = place.getAddress();
                //TODO: fix bug where address isn't showing
                autocompleteFragment.setHint(address);
                Log.i(TAG, "Place: " + place.getAddress());
            }

            @Override
            public void onError(Status status) {
                Log.e(TAG, "An error occurred: " + status);
            }
        });


        // If user making account w/ Facebook, prepopulate fields
        if (getIntent().hasExtra(User.class.getSimpleName())) {
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
                if (!checkIfFieldsAreFilled(name, bio, address, profileImageUrl)) {
                    return;
                }
                DatabaseClient.createUser(name, bio, profileImageUrl, address, SignUpActivity.this);
                // Go to main page after finished registering new user
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        fabAddProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(SignUpActivity.this);
            }
        });
    }

    // TODO: Clean this up
    private boolean checkIfFieldsAreFilled(String name, String bio, String address, String profileImageUrl) {
        if (name.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (bio.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Bio cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (address.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Address cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (profileImageUrl == null) {
            Toast.makeText(SignUpActivity.this, "No profile picture selected", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //Pops up a dialog to ask the user whether they would like to get a photo from camera or gallery
    private void selectImage(Context context) {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose a photo for your event");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);

                } else if (options[item].equals("Choose from Gallery")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, 1);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Uri imageToUpload;
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            if (requestCode == 0 || requestCode == 1) {
                Bitmap bm = null;
                switch (requestCode) {
                    case 0:  // If photo from Camera
                        if (resultCode == RESULT_OK && data != null) {
                            bm = (Bitmap) data.getExtras().get("data");
                        }
                        break;
                    case 1:     // If photo from Gallery
                        if (resultCode == RESULT_OK && data != null) {
                            Uri selectedImage = data.getData();
                            bm = ImageFormatter.getImageResized(SignUpActivity.this, selectedImage);
                            int rotation = ImageFormatter.getRotation(SignUpActivity.this, selectedImage, false);
                            bm = ImageFormatter.rotate(bm, rotation);
                        }
                        break;
                }
                Glide.with(SignUpActivity.this).load(bm).transform(new CircleCrop()).into(ivProfilePic);
                imageToUpload = ImageFormatter.getImageUri(SignUpActivity.this, bm);
                DatabaseClient.uploadImage(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        profileImageUrl = task.getResult().toString();
                    }
                }, imageToUpload, SignUpActivity.this);
            }

        }
    }
}