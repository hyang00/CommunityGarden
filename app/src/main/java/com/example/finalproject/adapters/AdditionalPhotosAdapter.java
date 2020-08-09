package com.example.finalproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finalproject.Common;
import com.example.finalproject.R;
import com.example.finalproject.models.AdditionalPhoto;

import java.util.ArrayList;
import java.util.List;

public class AdditionalPhotosAdapter extends RecyclerView.Adapter<AdditionalPhotosAdapter.ViewHolder> {


    private Context context;
    private List<AdditionalPhoto> additionalPhotos;

    public AdditionalPhotosAdapter(Context context, List<AdditionalPhoto> additionalPhotos) {
        this.context = context;
        this.additionalPhotos = additionalPhotos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdditionalPhoto additionalPhoto = additionalPhotos.get(position);
        holder.bind(additionalPhoto);
    }


    @Override
    public int getItemCount() {
        return additionalPhotos.size();
    }

    public void add(AdditionalPhoto additionalPhoto) {
        additionalPhotos.add(additionalPhoto);
    }

    public void add(ArrayList<AdditionalPhoto> additionalPhotos) {
        this.additionalPhotos.addAll(additionalPhotos);
    }

    public void clear() {
        additionalPhotos.clear();
    }

    public boolean isEmpty() {
        return additionalPhotos.isEmpty();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivPhoto;
        private TextView tvLabel;

        public ViewHolder(View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            tvLabel = itemView.findViewById(R.id.tvLabel);
        }

        public void bind(AdditionalPhoto additionalPhoto) {
            if (additionalPhoto.getImageUrl() != null) {
                Glide.with(context).load(additionalPhoto.getImageUrl()).into(ivPhoto);
            } else {
                if (additionalPhoto.getBitmap() != null) {
                    Glide.with(context).load(additionalPhoto.getBitmap()).into(ivPhoto);
                }
            }
            if (additionalPhoto.getLabel() != null && !additionalPhoto.getLabel().equals(Common.NO_LABEL_FOUND)) {
                tvLabel.setText(additionalPhoto.getLabel());
                tvLabel.setVisibility(View.VISIBLE);
            }
        }

    }
}
