package com.example.finalproject.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.finalproject.DatabaseClient;
import com.example.finalproject.ImageFormatter;
import com.example.finalproject.R;
import com.example.finalproject.models.Event;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.example.finalproject.Common.NO_ATTENDEES_CAP_SET;
import static com.example.finalproject.Common.TAGS;
import static com.example.finalproject.TimeAndDateFormatter.formatDateForStorage;
import static com.example.finalproject.TimeAndDateFormatter.formatDateForView;
import static com.example.finalproject.TimeAndDateFormatter.formatTime;


public class HostEventFragment extends Fragment implements AdapterView.OnItemSelectedListener,
        View.OnFocusChangeListener {

    private static final String TAG = "Host Event Fragment";
    private static final int DEFAULT_MIN_WIDTH_QUALITY = 400;        // min pixels
    private static final String NONE_KEY = "None";

    private TextInputEditText etTitle;
    private EditText etDescription;
    private ImageView ivPhoto;
    private EditText etDate;
    private EditText etTime;
    private Spinner spMaxAttendees;
    private Button btnPost;
    private Uri imageToUpload;
    private Uri downloadUri;
    private String address;
    private Long maxAttendees;
    private ChipGroup cgTags;
    private HashMap<String, Boolean> eventTags;


    public HostEventFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_host_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!Places.isInitialized()) {
            Places.initialize(getContext(), getString(R.string.api_key));
        }
        PlacesClient placesClient = Places.createClient(getContext());

        etTitle = view.findViewById(R.id.etTitle);
        etDescription = view.findViewById(R.id.etDescription);
        ivPhoto = view.findViewById(R.id.ivPhoto);
        etDate = view.findViewById(R.id.etDate);
        etTime = view.findViewById(R.id.etTime);
        spMaxAttendees = view.findViewById(R.id.spMaxAttendees);
        btnPost = view.findViewById(R.id.btnPost);
        cgTags = view.findViewById(R.id.cgTags);

        etTitle.setOnFocusChangeListener(this);
        etDescription.setOnFocusChangeListener(this);
        eventTags = new HashMap<>();

        // Initialize the AutocompleteSupportFragment.
        final AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ADDRESS, Place.Field.NAME));
        autocompleteFragment.setHint("Address");
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                address = place.getAddress();
                autocompleteFragment.setText(address);
                //autocompleteFragment.setHint(address);
                Log.i(TAG, "Place: " + place.getAddress());
            }

            @Override
            public void onError(Status status) {
                Log.e(TAG, "An error occurred: " + status);
            }
        });

        // launch an option to either add photo to post from camera/gallery
        ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(getContext());
            }
        });
        // launch a date picker when filling out date, populate date picker w/ current day/month/year to begin with;
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                DatePickerDialog picker = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                etDate.setText(formatDateForView(year, monthOfYear, dayOfMonth));
                            }
                        }, year, month, day);
                picker.show();
            }
        });

        // launch a time picker when filling out the time
        etTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog picker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int sHour, int sMinute) {
                        etTime.setText(formatTime(sHour, sMinute));
                    }
                }, 1, 1, false);
                picker.show();
            }
        });

        spMaxAttendees.setOnItemSelectedListener(this);
        // Save Post to database on click
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = etTitle.getText().toString();
                String description = etDescription.getText().toString();
                String date = etDate.getText().toString();
                String time = etTime.getText().toString();
                if (!checkIfFieldsAreFilled(title, description, address, date, time, downloadUri)) {
                    return;
                }
                date = formatDateForStorage(date);
                Event event = new Event(title, description, address, date, time, maxAttendees, downloadUri, getContext());
                event.setTags(eventTags);
                DatabaseClient.postEvent(getContext(), event);
                resetFields();
            }
        });
    }

    private void resetFields() {
        etTitle.setText("");
        etDescription.setText("");
        etDate.setText("");
        etTime.setText("");
        ivPhoto.setImageResource(R.drawable.ic_baseline_add_box_24);
    }

    private boolean checkIfFieldsAreFilled(String title, String description, String address, String date, String time, Uri downloadUri) {
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (description.isEmpty()) {
            Toast.makeText(getContext(), "Description cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (address == null || address.isEmpty()) {
            Toast.makeText(getContext(), "Address cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (date.isEmpty()) {
            Toast.makeText(getContext(), "Date cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (time.isEmpty()) {
            Toast.makeText(getContext(), "Time cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (downloadUri == null) {
            Toast.makeText(getContext(), "No photo selected", Toast.LENGTH_SHORT).show();
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
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
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
                        bm = ImageFormatter.getImageResized(getContext(), selectedImage);
                        int rotation = ImageFormatter.getRotation(getContext(), selectedImage, false);
                        bm = ImageFormatter.rotate(bm, rotation);
                    }
                    break;
            }
            ivPhoto.setImageBitmap(bm);
            imageToUpload = ImageFormatter.getImageUri(getContext(), bm);
            DatabaseClient.uploadImage(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    downloadUri = task.getResult();
                }
            }, imageToUpload, getContext());
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String selection = adapterView.getItemAtPosition(i).toString();
        if (selection.equals(NONE_KEY)) {
            maxAttendees = NO_ATTENDEES_CAP_SET;
        } else {
            maxAttendees = Long.valueOf(selection);
        }
        Log.i(TAG, "max attendees: " + maxAttendees);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        Log.i(TAG, view.toString() + "has focus: " + hasFocus);
        if (!hasFocus) {
            String info;
            switch (view.getId()) {
                case R.id.etTitle:
                    info = etTitle.getText().toString();
                    break;
                case R.id.etDescription:
                    info = etDescription.getText().toString();
                    break;
                default:
                    info = "";
                    break;
            }
            InputMethodManager inputMethodManager =
                    (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            setTags(info);
        }
    }

    private void setTags(String info) {
        info = info.toLowerCase();
        for (String tag : TAGS.keySet()) {
            ArrayList<String> words = TAGS.get(tag);
            for (String word : words) {
                if (info.contains(word.toLowerCase())) {
                    if (!eventTags.containsKey(tag)) {
                        eventTags.put(tag, true);
                        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_filter, null, false);
                        chip.setText(tag);
                        cgTags.addView(chip);
                    }
                }
            }
        }

    }
}

