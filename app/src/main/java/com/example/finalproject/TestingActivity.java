package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class TestingActivity extends AppCompatActivity {


    private static final String TAG = "Testing Activity";
    protected Interpreter tflite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);
        //Glide.with(this).
        String url = "https://firebasestorage.googleapis.com/v0/b/gardentours-3ec05.appspot.com/o/images%2F66cd5987-19bb-4ac0-8c00-51ca3671b48e?alt=media&token=87b71ec7-f5e5-4c20-8676-bc29d2cd6b1f";
        String url1 = "https://cloudinary-a.akamaihd.net/ufn/image/upload/u7cdzxvxu69pmubmtltc.jpg";
        //InputImage image = InputImage.fromBitmap(getBitmapFromURL(url1), 90);
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_baseline_add_box_24);
        InputImage image = InputImage.fromBitmap(icon, 0);
        //InputImage image = InputImage.fromFilePath(this, "download.jpg");
        //        Uri uri = null;
//        try {
//            uri = new Uri(url.toString());
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
//        Log.i(TAG, uri.toString());
//        InputImage image = null;
//        try {
//            image = InputImage.fromFilePath(TestingActivity.this, uri);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        LocalModel localModel =
                new LocalModel.Builder()
                        .setAssetFilePath(getModelPath())
                        // or .setAbsoluteFilePath(absolute file path to tflite model)
                        .build();

        CustomImageLabelerOptions customImageLabelerOptions =
                new CustomImageLabelerOptions.Builder(localModel)
                        .setConfidenceThreshold(0.5f)
                        .setMaxResultCount(5)
                        .build();

        ImageLabeler imageLabeler =
                ImageLabeling.getClient(customImageLabelerOptions);

        imageLabeler.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(List<ImageLabel> labels) {
                        for (ImageLabel label : labels) {
                            String text = label.getText();
                            float confidence = label.getConfidence();
                            int index = label.getIndex();
                            Log.i(TAG, "label: " + label);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                    }
                });

//        try {
//            tflite = new Interpreter(loadModelFile(this));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        final float IMAGE_MEAN = 127.5f;
//        final float IMAGE_STD = 127.5f;
//        final int IMAGE_SIZE_X = 224;
//        final int IMAGE_SIZE_Y = 224;
//        final int DIM_BATCH_SIZE = 1;
//        final int DIM_PIXEL_SIZE = 3;
//        final int NUM_BYTES_PER_CHANNEL = 4;
//        final int NUM_CLASS = 2102;
//
//        // The example uses Bitmap ARGB_8888 format.
//        Bitmap bitmap = ...;
//
//        int[] intValues = new int[IMAGE_SIZE_X * IMAGE_SIZE_Y];
//        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
//
//        ByteBuffer imgData =
//                ByteBuffer.allocateDirect(
//                        DIM_BATCH_SIZE
//                                * IMAGE_SIZE_X
//                                * IMAGE_SIZE_Y
//                                * DIM_PIXEL_SIZE
//                                * NUM_BYTES_PER_CHANNEL);
//        imgData.rewind();
//
//        // Float model.
//        int pixel = 0;
//        for (int i = 0; i < IMAGE_SIZE_X; ++i) {
//            for (int j = 0; j < IMAGE_SIZE_Y; ++j) {
//                int pixelValue = intValues[pixel++];
//                imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
//                imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
//                imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
//            }
//        }
//
////        // Quantized model.
////        int pixel = 0;
////        for (int i = 0; i < IMAGE_SIZE_X; ++i) {
////            for (int j = 0; j < IMAGE_SIZE_Y; ++j) {
////                imgData.put((byte) ((pixelValue >> 16) & 0xFF));
////                imgData.put((byte) ((pixelValue >> 8) & 0xFF));
////                imgData.put((byte) (pixelValue & 0xFF));
////            }
////        }
//
//        // Output label probabilities.
//        float[][] labelProbArray = new float[1][NUM_CLASS];
//
//        // Run the model.
//        tflite.run(imgData, labelProbArray);
    }

    /**
     * Memory-map the model file in Assets.
     */
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

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

}