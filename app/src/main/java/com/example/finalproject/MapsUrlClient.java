package com.example.finalproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import com.example.finalproject.models.Event;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

public class MapsUrlClient {
    private static final String TAG = "MapsUrlClient";
    public static final String MAP_SEARCH_URL_KEY = "https://www.google.com/maps/search/?api=1&query=";
    public static final String MAP_STATIC_URL_KEY = "https://maps.googleapis.com/maps/api/staticmap?center=";
    public static final String MAP_STATIC_DEFAULT_SETTINGS = "zoom=14&size=400x300&markers=color:red%7C";
    public static final String MAP_KEY_KEY = "&key=";

    public static void launchGoogleMaps(Context context, Event event) {
        launchGoogleMaps(context, event.getLocation().getWrittenAddress());
    }

    public static void launchGoogleMaps(Context context, String address) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MAP_SEARCH_URL_KEY + convertAddressToSearchQuery(address)));
        context.startActivity(intent);
    }

    // Get the static map and set the image to the static map
    public static void setGoogleMapThumbnail(Context context, String address, final ImageView ivMap) {
        String url = getGoogleMapThumbnailUrl(context, address);
        Log.i(TAG, url);
        final Request request = new Request.Builder().url(url).build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG, e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.i(TAG, response.toString());
                    final Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            ivMap.setImageBitmap(bitmap);
                        }
                    });
                }
            }
        });
    }

    // Get the url to query for a static map
    private static String getGoogleMapThumbnailUrl(Context context, String address) {
        String query = convertAddressToSearchQuery(address);
        return MAP_STATIC_URL_KEY + query + MAP_STATIC_DEFAULT_SETTINGS + query + MAP_KEY_KEY + context.getString(R.string.api_key);
    }

    //Takes standard address format and converts it into maps query preferred format (spaces -> +, commas -> %2C)
    private static String convertAddressToSearchQuery(String address) {
        String searchQuery = "";
        for (int i = 0; i < address.length(); ++i) {
            char currLetter = address.charAt(i);
            if (currLetter == ' ') {
                searchQuery += "+";
            } else if (currLetter == ',') {
                searchQuery += "%2C";
            } else {
                searchQuery += currLetter;
            }
        }
        return searchQuery;
    }
}
