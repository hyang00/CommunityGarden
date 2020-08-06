package com.example.finalproject.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import com.example.finalproject.DatabaseClient;
import com.example.finalproject.ImageFormatter;
import com.example.finalproject.R;
import com.example.finalproject.adapters.PhotoGalleryAdapter;
import com.example.finalproject.models.AdditionalPhoto;
import com.example.finalproject.models.Event;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
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
    private ImageView ivAddAdditionalPhotos;
    private GridView gvPhotos;
    ArrayList<AdditionalPhoto> additionalPhotos;
    PhotoGalleryAdapter photosAdapter;


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
        etDate = view.findViewById(R.id.etDate);
        etTime = view.findViewById(R.id.etTime);
        spMaxAttendees = view.findViewById(R.id.spMaxAttendees);
        btnPost = view.findViewById(R.id.btnPost);
        cgTags = view.findViewById(R.id.cgTags);
        ivAddAdditionalPhotos = view.findViewById(R.id.ivAddAdditionalPhoto);

        gvPhotos = (GridView) view.findViewById(R.id.gvAdditionalPhotos);
        additionalPhotos = new ArrayList<>();
        //Bitmap addPhotoIcon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_baseline_add_box_24);
        //additionalPhotos.add(new AdditionalPhoto(addPhotoIcon, NO_LABEL_FOUND));
        photosAdapter = new PhotoGalleryAdapter(getContext(), additionalPhotos);
        gvPhotos.setAdapter(photosAdapter);


        etTitle.setOnFocusChangeListener(this);
        etDescription.setOnFocusChangeListener(this);
        eventTags = new HashMap<>();

        // Get instance of PlacesClient to use autocomplete support fragment w/ places
        if (!Places.isInitialized()) {
            Places.initialize(getContext(), getString(R.string.api_key));
        }
        PlacesClient placesClient = Places.createClient(getContext());

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
                selectImage(getContext(), false);
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
            }
        }
    }

    private void addAdditionalPhoto(Bitmap bm) {
        String label = runLabeler(bm);
        Uri additionalImageToUpload = ImageFormatter.getImageUri(getContext(), bm);
        final AdditionalPhoto photo = new AdditionalPhoto(bm, label);
        photosAdapter.add(photo);
        photosAdapter.notifyDataSetChanged();
        DatabaseClient.uploadImage(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                downloadUri = task.getResult();
                photo.setImageUrl(downloadUri.toString());
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

    // Take the uploaded bitmap and run it through the plant classifier data model to find out if it contains any
    // particularily identifiable plants
    private String runLabeler(Bitmap bm) {

        final int IMAGE_SIZE_X = 224;
        final int IMAGE_SIZE_Y = 224;
        final int NUM_CLASS = 2102;

        String likelyLabel = NO_LABEL_FOUND;

        // Initialize interpreter w/ premade model
        Interpreter tflite = null;
        try {
            tflite = new Interpreter(loadModelFile(getActivity()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create an ImageProcessor with all ops required. (resize to 224X224)
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeOp(IMAGE_SIZE_X, IMAGE_SIZE_Y, ResizeOp.ResizeMethod.BILINEAR))
                        .build();

        // Create a TensorImage object of tensor type uint8
        TensorImage tImage = new TensorImage(DataType.UINT8);

        // Analysis code for every frame
        // Preprocess the image
        tImage.load(bm);
        tImage = imageProcessor.process(tImage);

        //for storing output
        TensorBuffer probabilityBuffer =
                TensorBuffer.createFixedSize(new int[]{1, NUM_CLASS}, DataType.UINT8);

        // Run the model.
        tflite.run(tImage.getBuffer(), probabilityBuffer.getBuffer());

        // core
        final String ASSOCIATED_AXIS_LABELS = "aiy_plants_V1_labelmap.csv";
        List<String> associatedAxisLabels = null;

        // get file w/ labels corresponding to output probabilities
        try {
            associatedAxisLabels = FileUtil.loadLabels(getContext(), ASSOCIATED_AXIS_LABELS);
        } catch (IOException e) {
            Log.e("tfliteSupport", "Error reading label file", e);
        }

        // Post-processor which dequantize the result
        TensorProcessor probabilityProcessor =
                new TensorProcessor.Builder().add(new NormalizeOp(0, 255)).build();

        if (null != associatedAxisLabels) {
            // Map of labels and their corresponding probability
            TensorLabel labels = new TensorLabel(associatedAxisLabels,
                    probabilityProcessor.process(probabilityBuffer));

            // Create a map to access the result based on label
            Map<String, Float> floatMap = labels.getMapWithFloatValue();
            for (String label : floatMap.keySet()) {
                Float max = 0f;
                if (floatMap.get(label) >= .8 && floatMap.get(label) > max) {
                    max = floatMap.get(label);
                    likelyLabel = label;
                    Log.i(TAG, "label: " + label + " prob: " + floatMap.get(label));
                }
            }
        }
        return likelyLabel;
    }

    // Memory-map the model file in Assets.
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(getModelPath());
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private String getModelPath() {
        return "aiy_vision_classifier_plants_V1_1.tflite";
    }
}

