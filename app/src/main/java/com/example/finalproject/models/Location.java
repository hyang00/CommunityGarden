package com.example.finalproject.models;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.example.finalproject.SignUpActivity;

import org.parceler.Parcel;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Parcel
public class Location {
    private static final String TAG = "Location";

    private String writtenAddress;
    private String locality;
    private double lat;
    private double longitude;

    public Location() {
    }

    public Location(String writtenAddress, Context context) {
        Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> myAddress = geoCoder.getFromLocationName(writtenAddress, 1);
            this.writtenAddress = myAddress.get(0).getAddressLine(0);
            this.locality = myAddress.get(0).getLocality();
            this.lat = myAddress.get(0).getLatitude();
            this.longitude = myAddress.get(0).getLongitude();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getWrittenAddress() {
        return writtenAddress;
    }

    public void setWrittenAddress(String writtenAddress) {
        this.writtenAddress = writtenAddress;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
