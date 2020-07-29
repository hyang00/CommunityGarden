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
import com.example.finalproject.R;
import com.example.finalproject.models.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    private static final String TAG = "UsersAdapter";
    private Context context;
    private List<User> users;

    public UsersAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_profile, parent, false);
        return new UsersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void add(User user) {
        users.add(user);
    }

    public void clear() {
        users.clear();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivProfilePic;
        private TextView tvName;
        private TextView tvBio;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfilePic = itemView.findViewById(R.id.ivProfilePic);
            tvName = itemView.findViewById(R.id.tvName);
            tvBio = itemView.findViewById(R.id.tvBio);
        }

        public void bind(User user) {
            tvName.setText(user.getScreenName());
            tvBio.setText(user.getBio());
            if (user.getProfileImageUrl() != null) {
                Glide.with(context).load(user.getProfileImageUrl()).into(ivProfilePic);
            }
        }
    }
}
