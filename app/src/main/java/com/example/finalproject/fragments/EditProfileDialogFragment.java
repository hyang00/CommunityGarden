package com.example.finalproject.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.finalproject.DatabaseClient;
import com.example.finalproject.ImageFormatter;
import com.example.finalproject.R;
import com.example.finalproject.models.User;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.annotations.NotNull;

import java.util.Arrays;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class EditProfileDialogFragment extends DialogFragment {

    private static final String TAG = "EditProfDialogFragment";

    private EditText etName;
    private EditText etBio;
    private ImageView ivProfilePic;
    private FloatingActionButton fabAddProfilePic;
    private ExtendedFloatingActionButton fabSave;
    private User user;
    private String profileImageUrl;
    private String address;
    private AutocompleteSupportFragment autocompleteFragment;

    public EditProfileDialogFragment() {

    }

    public static EditProfileDialogFragment newInstance(User user){
        EditProfileDialogFragment frag = new EditProfileDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(User.class.getSimpleName(), user);
        frag.setArguments(args);
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return getActivity().getLayoutInflater().inflate(R.layout.fragment_edit_profile_dialog, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName = view.findViewById(R.id.etName);
        etBio = view.findViewById(R.id.etBio);
        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        fabAddProfilePic = view.findViewById(R.id.fabAddProfilePic);
        fabSave = view.findViewById(R.id.fabSave);
        user = (User) getArguments().getSerializable(User.class.getSimpleName());

        if (!Places.isInitialized()) {
            Places.initialize(getContext(), getString(R.string.api_key));
        }
        @SuppressWarnings("unused") PlacesClient placesClient = Places.createClient(getContext());

        // Initialize the AutocompleteSupportFragment.
        autocompleteFragment = new AutocompleteSupportFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.autocomplete_fragment, autocompleteFragment).commit();

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

        address = user.getLocation().getWrittenAddress();
        autocompleteFragment.setText(address);
        etName.setText(user.getScreenName());
        etBio.setText(user.getBio());
        profileImageUrl = user.getProfileImageUrl();
        Glide.with(getContext()).load(profileImageUrl).transform(new CircleCrop()).into(ivProfilePic);
        fabAddProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(getContext());
            }
        });

        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = etName.getText().toString();
                String bio = etBio.getText().toString();
                DatabaseClient.createUser(name, bio, profileImageUrl, address, getContext());
                sendBackResult();
            }
        });
        // Show soft keyboard automatically
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
                            bm = ImageFormatter.getImageResized(getActivity(), selectedImage);
                            int rotation = ImageFormatter.getRotation(getActivity(), selectedImage, false);
                            bm = ImageFormatter.rotate(bm, rotation);
                        }
                        break;
                }
                Glide.with(getActivity()).load(bm).transform(new CircleCrop()).into(ivProfilePic);
                imageToUpload = ImageFormatter.getImageUri(getActivity(), bm);
                DatabaseClient.uploadImage(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        profileImageUrl = task.getResult().toString();
                    }
                }, imageToUpload, getActivity());
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public interface EditProfileDialogListener {
        void onFinishEditDialog();
    }

    public void sendBackResult() {
        EditProfileDialogListener listener = (EditProfileDialogListener) getTargetFragment();
        listener.onFinishEditDialog();
        dismiss();
    }
}
