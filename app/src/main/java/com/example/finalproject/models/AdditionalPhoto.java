package com.example.finalproject.models;

import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;

import org.parceler.Parcel;

@Parcel
public class AdditionalPhoto {

    private Bitmap bitmap;
    private String imageUrl;
    private String label;

    public AdditionalPhoto() {
    }

    public AdditionalPhoto(Bitmap bitmap, String label) {
        this.bitmap = bitmap;
        this.label = label;
    }

    public AdditionalPhoto(String imageUrl, String label) {
        this.imageUrl = imageUrl;
        this.label = label;
    }

    @Exclude
    public Bitmap getBitmap() {
        return bitmap;
    }

    @Exclude
    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
