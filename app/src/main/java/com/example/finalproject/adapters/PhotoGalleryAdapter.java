package com.example.finalproject.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finalproject.Common;
import com.example.finalproject.R;
import com.example.finalproject.models.AdditionalPhoto;
import com.example.finalproject.models.Event;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryAdapter extends ArrayAdapter<AdditionalPhoto> {


    public PhotoGalleryAdapter(@NonNull Context context, ArrayList<AdditionalPhoto> additionalPhotos) {
        super(context, 0, additionalPhotos);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        AdditionalPhoto additionalPhoto = getItem(position);
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_photo, parent, false);
        }

        ImageView ivPhoto = (ImageView) convertView.findViewById(R.id.ivPhoto);
        TextView tvLabel = (TextView) convertView.findViewById(R.id.tvLabel);

        if (additionalPhoto.getImageUrl()!=null){
            Glide.with(getContext()).load(additionalPhoto.getImageUrl()).into(ivPhoto);
        }else{
            if (additionalPhoto.getBitmap()!=null){
                Glide.with(getContext()).load(additionalPhoto.getBitmap()).into(ivPhoto);
            }
        }
        if (!additionalPhoto.getLabel().equals(Common.NO_LABEL_FOUND)){
            tvLabel.setText(additionalPhoto.getLabel());
            tvLabel.setVisibility(View.VISIBLE);
        }
        return convertView;
    }
}
