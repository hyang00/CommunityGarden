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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.DatabaseClient;
import com.example.finalproject.ImageFormatter;
import com.example.finalproject.PlantLabeler;
import com.example.finalproject.R;
import com.example.finalproject.adapters.AdditionalPhotosAdapter;
import com.example.finalproject.models.AdditionalPhoto;
import com.example.finalproject.models.Event;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.example.finalproject.Common.NO_ATTENDEES_CAP_SET;
import static com.example.finalproject.Common.NO_LABEL_FOUND;
import static com.example.finalproject.Common.TAGS;
import static com.example.finalproject.TimeAndDateFormatter.formatDateForStorage;
import static com.example.finalproject.TimeAndDateFormatter.formatDateForView;
import static com.example.finalproject.TimeAndDateFormatter.formatTime;


public class HostEventFragment extends Fragment implements AdapterView.OnItemSelectedListener,
        View.OnFocusChangeListener {

    private static final String TAG = "Host Event Fragment";
    private static final String NONE_KEY = "None";
    private static final int CAMERA_PHOTO_REQUEST_CODE = 0;
    private static final int GALLERY_PHOTO_REQUEST_CODE = 1;
    private static final int CAMERA_ADDITIONAL_PHOTO_REQUEST_CODE = 2;
    private static final int GALLERY_ADDITIONAL_PHOTO_REQUEST_CODE = 3;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 30;


    private TextInputEditText etTitle;
    private EditText etDescription;
    private ImageView ivPhoto;
    private EditText etAddress;
    private EditText etDate;
    private EditText etTime;
    private Spinner spMaxAttendees;
    private ExtendedFloatingActionButton fabPost;
    private Uri imageToUpload;
    private Uri downloadUri;
    private String address;
    private Long maxAttendees;
    private ChipGroup cgTags;
    private HashMap<String, Boolean> eventTags;
    private ImageView ivAddAdditionalPhotos;
    private GridView gvPhotos;
    ArrayList<AdditionalPhoto> additionalPhotos;
    private RecyclerView rvAdditionalPhotos;
    AdditionalPhotosAdapter additionalPhotosAdapter;


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

        etTitle = view.findViewById(R.id.etTitle);
        etDescription = view.findViewById(R.id.etDescription);
        ivPhoto = view.findViewById(R.id.ivPhoto);
        etAddress = view.findViewById(R.id.etAddress);
        etDate = view.findViewById(R.id.etDate);
        etTime = view.findViewById(R.id.etTime);
        spMaxAttendees = view.findViewById(R.id.spMaxAttendees);
        fabPost = view.findViewById(R.id.fabPost);
        cgTags = view.findViewById(R.id.cgTags);
        ivAddAdditionalPhotos = view.findViewById(R.id.ivAddAdditionalPhoto);


        rvAdditionalPhotos = (RecyclerView) view.findViewById(R.id.rvAdditionalPhotos);
        additionalPhotos = new ArrayList<>();
        additionalPhotosAdapter = new AdditionalPhotosAdapter(getContext(), additionalPhotos);
        rvAdditionalPhotos.setAdapter(additionalPhotosAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        rvAdditionalPhotos.setLayoutManager(gridLayoutManager);


        etTitle.setOnFocusChangeListener(this);
        etDescription.setOnFocusChangeListener(this);
        eventTags = new HashMap<>();

        // Get instance of PlacesClient to use autocomplete support fragment w/ places
        if (!Places.isInitialized()) {
            Places.initialize(getContext(), getString(R.string.api_key));
        }
        PlacesClient placesClient = Places.createClient(getContext());

        // launch an option to either add photo to post from camera/gallery
        ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(getContext(), false);
            }
        });
        // launch activity to set address
        etAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "address clicked");
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(getContext());
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
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

        ivAddAdditionalPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(getContext(), true);
            }
        });

        spMaxAttendees.setOnItemSelectedListener(this);
        // Save Post to database on click
        fabPost.setOnClickListener(new View.OnClickListener() {
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
                DatabaseClient.postEvent(getContext(), event, additionalPhotos);
                resetFields();
            }
        });
    }

    private void resetFields() {
        etTitle.setText("");
        etDescription.setText("");
        etDate.setText("");
        etTime.setText("");
        cgTags.removeAllViews();
        eventTags.clear();
        additionalPhotosAdapter.clear();
        etAddress.setText("");
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
    private void selectImage(Context context, final boolean isAdditionalPhoto) {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose a photo for your event");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {
                int requestCode;
                if (options[item].equals("Take Photo")) {
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    takePicture.putExtra("Photo type", isAdditionalPhoto);
                    if (isAdditionalPhoto) {
                        requestCode = CAMERA_ADDITIONAL_PHOTO_REQUEST_CODE;
                    } else {
                        requestCode = CAMERA_PHOTO_REQUEST_CODE;
                    }
                    startActivityForResult(takePicture, requestCode);

                } else if (options[item].equals("Choose from Gallery")) {
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickPhoto.putExtra("Photo type", isAdditionalPhoto);
                    if (isAdditionalPhoto) {
                        requestCode = GALLERY_ADDITIONAL_PHOTO_REQUEST_CODE;
                    } else {
                        requestCode = GALLERY_PHOTO_REQUEST_CODE;
                    }
                    startActivityForResult(pickPhoto, requestCode);

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
        if (resultCode == RESULT_OK) {
            Bitmap bm = null;
            switch (requestCode) {
                case CAMERA_PHOTO_REQUEST_CODE:  // If photo from Camera
                    if (data != null) {
                        bm = (Bitmap) data.getExtras().get("data");
                        addEventPhoto(bm);
                    }
                    break;
                case GALLERY_PHOTO_REQUEST_CODE:     // If photo from Gallery
                    if (data != null) {
                        Uri selectedImage = data.getData();
                        bm = ImageFormatter.getImageResized(getContext(), selectedImage);
                        int rotation = ImageFormatter.getRotation(getContext(), selectedImage, false);
                        bm = ImageFormatter.rotate(bm, rotation);
                        addEventPhoto(bm);
                    }
                    break;
                case CAMERA_ADDITIONAL_PHOTO_REQUEST_CODE:
                    if (data != null) {
                        bm = (Bitmap) data.getExtras().get("data");
                        addAdditionalPhoto(bm);
                    }
                    break;
                case GALLERY_ADDITIONAL_PHOTO_REQUEST_CODE:
                    if (data != null) {
                        Uri selectedImage = data.getData();
                        bm = ImageFormatter.getImageResized(getContext(), selectedImage);
                        int rotation = ImageFormatter.getRotation(getContext(), selectedImage, false);
                        bm = ImageFormatter.rotate(bm, rotation);
                        addAdditionalPhoto(bm);
                    }
                    break;
                case AUTOCOMPLETE_REQUEST_CODE:
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    address = place.getAddress();
                    etAddress.setText(address);
            }
        }
    }

    private void addAdditionalPhoto(Bitmap bm) {
        final String label = PlantLabeler.runLabeler(bm, getActivity(), getContext());
        Uri additionalImageToUpload = ImageFormatter.getImageUri(getContext(), bm);
        final AdditionalPhoto photo = new AdditionalPhoto(bm, label);
        additionalPhotosAdapter.add(photo);
        additionalPhotosAdapter.notifyDataSetChanged();
        //photosAdapter.add(photo);
        //photosAdapter.notifyDataSetChanged();
        DatabaseClient.uploadImage(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                Uri tempUri = downloadUri = task.getResult();
                photo.setImageUrl(downloadUri.toString());
                if (!label.equals(NO_LABEL_FOUND)) {
                    String message = "Plant was labeled as " + label;
                    Snackbar.make(getView(), message, Snackbar.LENGTH_INDEFINITE).setAction("Remove Label", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Log.i(TAG, "remove");
                            photo.setLabel(NO_LABEL_FOUND);
                            additionalPhotosAdapter.notifyItemChanged(additionalPhotosAdapter.getItemCount() - 1);
                        }
                    }).show();
                }
            }
        }, additionalImageToUpload, getContext());
    }

    private void addEventPhoto(Bitmap bm) {
        ivPhoto.setImageBitmap(bm);
        imageToUpload = ImageFormatter.getImageUri(getContext(), bm);
        DatabaseClient.uploadImage(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                downloadUri = task.getResult();
            }
        }, imageToUpload, getContext());
    }

    // Listener for when user picks option for max # of attendees from the spinner
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

