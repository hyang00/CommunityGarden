package com.example.finalproject;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;

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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;

import static com.example.finalproject.Common.NO_LABEL_FOUND;

public class PlantLabeler {

    private static final String TAG = "Plant Labeler";

    // Take the uploaded bitmap and run it through the plant classifier data model to find out if it contains any
    // particularily identifiable plants
    public static String runLabeler(Bitmap bm, Activity activity, Context context) {

        final int IMAGE_SIZE_X = 224;
        final int IMAGE_SIZE_Y = 224;
        final int NUM_CLASS = 2102;

        String likelyLabel = NO_LABEL_FOUND;

        // Initialize interpreter w/ premade model
        Interpreter tflite = null;
        try {
            tflite = new Interpreter(loadModelFile(activity));
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
            associatedAxisLabels = FileUtil.loadLabels(context, ASSOCIATED_AXIS_LABELS);
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
                if (floatMap.get(label) >= .5 && floatMap.get(label) > max) {
                    max = floatMap.get(label);
                    likelyLabel = label;
                    Log.i(TAG, "label: " + label + " prob: " + floatMap.get(label));
                }
            }
        }
        return likelyLabel;
    }

    // Memory-map the model file in Assets.
    private static MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(getModelPath());
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private static String getModelPath() {
        return "aiy_vision_classifier_plants_V1_1.tflite";
    }
}
