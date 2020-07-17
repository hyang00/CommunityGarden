package com.example.finalproject.fragments;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.finalproject.DatabaseClient;
import com.example.finalproject.ImageFormatter;
import com.example.finalproject.MainActivity;
import com.example.finalproject.R;
import com.example.finalproject.models.Event;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class HostEventFragment extends Fragment {

    public static final  String TAG = "Event Fragment";
    private static final int DEFAULT_MIN_WIDTH_QUALITY = 400;        // min pixels
    private static Uri downloadUri;

    private TextInputEditText etTitle;
    private EditText etDescription;
    private ImageView ivPhoto;
    private EditText etAddress;
    private EditText etDate;
    private EditText etTime;
    private Button btnPost;
    private Uri imageToUpload;
    //private Uri downloadUri;


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
                                etDate.setText( (monthOfYear + 1) + "/"  +dayOfMonth +"/" + year);
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
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = etTitle.getText().toString();
                String description = etDescription.getText().toString();
                String address = etAddress.getText().toString();
                String date = etDate.getText().toString();
                String time = etTime.getText().toString();
                if (description.isEmpty()){
                    Toast.makeText(getContext(), "Description cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                DatabaseClient.postEvent(getContext(), title, description, address, date, time, downloadUri);
            }
        });
    }
    // Takes in hours/24 and minutes/60 and formats it as 1:00 PM form
    public String formatTime(int hour, int min) {
        String format = " AM";
        String minutes = ""+ min;
        if (hour>=12) {
            format = " PM";
        }
        if (hour == 0) {
            hour += 12;
        } else if (hour > 12) {
            hour -=12;
        }
        if (min<10){
            minutes = "0"+ minutes;
        }
        return ""+ hour + ":" + minutes + format;
    }

    //Pops up a dialog to ask the user whether they would like to get a photo from camera or gallery
    private void selectImage(Context context) {
        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

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
                    startActivityForResult(pickPhoto , 1);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode != RESULT_CANCELED) {
            Bitmap bm = null;
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        bm = (Bitmap) data.getExtras().get("data");
                        ivPhoto.setImageBitmap(bm);
                        imageToUpload = ImageFormatter.getImageUri(getContext(), bm);
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage =  data.getData();
                        bm = ImageFormatter.getImageResized(getContext(), selectedImage);
                        int rotation = ImageFormatter.getRotation(getContext(), selectedImage, false);
                        bm = ImageFormatter.rotate(bm, rotation);
                        ivPhoto.setImageBitmap(bm);
                        //uploadImage(selectedImage);
                    }
                    break;
            }
            imageToUpload = ImageFormatter.getImageUri(getContext(), bm);
            //uploadImage(imageToUpload);
            DatabaseClient.uploadImage(imageToUpload, getContext());
        }
    }
    public static void setDownloadUri(Uri uri){
        downloadUri = uri;
    }
}

