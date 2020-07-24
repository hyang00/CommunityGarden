package com.example.finalproject.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.example.finalproject.TimeAndDateFormatter.formatDateForStorage;
import static com.example.finalproject.TimeAndDateFormatter.formatDateForView;
import static com.example.finalproject.TimeAndDateFormatter.formatTime;


public class HostEventFragment extends Fragment {

    public static final String TAG = "Event Fragment";
    private static final int DEFAULT_MIN_WIDTH_QUALITY = 400;        // min pixels
    //private static Uri downloadUri;

    private TextInputEditText etTitle;
    private EditText etDescription;
    private ImageView ivPhoto;
    private EditText etAddress;
    private EditText etDate;
    private EditText etTime;
    private Button btnPost;
    private Uri imageToUpload;
    private Uri downloadUri;


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
        btnPost = view.findViewById(R.id.btnPost);
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
        // Save Post to database on click
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = etTitle.getText().toString();
                String description = etDescription.getText().toString();
                String address = etAddress.getText().toString();
                String date = etDate.getText().toString();
                String time = etTime.getText().toString();
                if (!checkIfFieldsAreFilled(title, description, address, date, time, downloadUri)){
                    return;
                }
                date = formatDateForStorage(date);
                DatabaseClient.postEvent(getContext(), title, description, address, date, time, downloadUri);
                //  TODO: Maybe go to another page when done posting?
                resetFields();
            }
        });
    }

    private void resetFields(){
        etTitle.setText("");
        etDescription.setText("");
        etAddress.setText("");
        etDate.setText("");
        etTime.setText("");
        ivPhoto.setImageResource(R.drawable.ic_baseline_add_box_24);
    }

    private boolean checkIfFieldsAreFilled(String title, String description, String address, String date, String time, Uri downloadUri){
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (description.isEmpty()) {
            Toast.makeText(getContext(), "Description cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (address.isEmpty()) {
            Toast.makeText(getContext(), "Address cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (date.isEmpty()) {
            Toast.makeText(getContext(), "Date cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (time.isEmpty()){
            Toast.makeText(getContext(), "Time cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (downloadUri == null){
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
}

