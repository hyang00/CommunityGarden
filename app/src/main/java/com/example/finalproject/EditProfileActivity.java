package com.example.finalproject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

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
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.annotations.NotNull;

import org.parceler.Parcels;

import java.util.Arrays;

public class EditProfileActivity extends AppCompatActivity {

    public static final String TAG = "EditProfile";
    private static final int AUTOCOMPLETE_REQUEST_CODE = 190;

    private EditText etName;
    private EditText etBio;
    private EditText etAddress;
    private ImageView ivProfilePic;
    private FloatingActionButton fabAddProfilePic;
    private ExtendedFloatingActionButton fabSave;
    private ExtendedFloatingActionButton fabCancel;
    private String profileImageUrl;
    private String address;
    private User user;
    private AutocompleteSupportFragment autocompleteFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        user = (User) Parcels.unwrap(getIntent().getParcelableExtra(User.class.getSimpleName()));

        etName = findViewById(R.id.etName);
        etBio = findViewById(R.id.etBio);
        //etAddress = findViewById(R.id.etAddress);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        fabAddProfilePic = findViewById(R.id.fabAddProfilePic);
        fabSave = findViewById(R.id.fabSave);
        fabCancel = findViewById(R.id.fabCancel);

        if (!Places.isInitialized()) {
            Places.initialize(EditProfileActivity.this, getString(R.string.api_key));
        }
        PlacesClient placesClient = Places.createClient(EditProfileActivity.this);

        // Initialize the AutocompleteSupportFragment.
        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ADDRESS, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NotNull Place place) {
                address = place.getAddress();
                autocompleteFragment.setText(place.getAddress());
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(@NotNull Status status) {
                Log.e(TAG, "An error occurred: " + status);
            }
        });


        etName.setText(user.getScreenName());
        etBio.setText(user.getBio());
        autocompleteFragment.setText(user.getLocation().getWrittenAddress());
        //etAddress.setText(user.getLocation().getWrittenAddress());
        profileImageUrl = user.getProfileImageUrl();
        Glide.with(EditProfileActivity.this).load(profileImageUrl).into(ivProfilePic);
        fabAddProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(EditProfileActivity.this);
            }
        });
        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = etName.getText().toString();
                String bio = etBio.getText().toString();
                DatabaseClient.createUser(name, bio, profileImageUrl, address, EditProfileActivity.this);
                finish();
            }
        });
        fabCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
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
                            bm = ImageFormatter.getImageResized(EditProfileActivity.this, selectedImage);
                            int rotation = ImageFormatter.getRotation(EditProfileActivity.this, selectedImage, false);
                            bm = ImageFormatter.rotate(bm, rotation);
                        }
                        break;
                }
                Glide.with(EditProfileActivity.this).load(bm).transform(new CircleCrop()).into(ivProfilePic);
                //ivProfilePic.setImageBitmap(bm);
                imageToUpload = ImageFormatter.getImageUri(EditProfileActivity.this, bm);
                DatabaseClient.uploadImage(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        profileImageUrl = task.getResult().toString();
                    }
                }, imageToUpload, EditProfileActivity.this);
            }

        }
    }
}