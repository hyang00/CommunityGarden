package com.example.finalproject.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finalproject.Common;
import com.example.finalproject.DatabaseClient;
import com.example.finalproject.EventDetailsActivity;
import com.example.finalproject.R;
import com.example.finalproject.models.Event;

import org.parceler.Parcels;

import java.util.List;

import static com.example.finalproject.TimeAndDateFormatter.formatDateWithDayOfWeek;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {

    private Context context;
    private List<Event> events;
    private String eventType = "";

    public EventsAdapter(Context context, List<Event> events, String eventType) {
        this.context = context;
        this.events = events;
        this.eventType = eventType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void add(Event event) {
        events.add(event);
    }

    public void clear() {
        events.clear();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvTitle;
        private ImageView ivEventPhoto;
        private TextView tvTime;
        private TextView tvDescription;
        private TextView tvLocation;
        private Button btnRSVP;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            ivEventPhoto = itemView.findViewById(R.id.ivEventPhoto);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            btnRSVP = itemView.findViewById(R.id.btnRSVP);
            btnRSVP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    Event event = events.get(position);
                    DatabaseClient.rsvpUser(event, context);
                    Log.i("adapter", "event key: " + event.getEventId());
                }
            });
            if (eventType.equals(Common.EVENT_HOSTING_KEY) || eventType.equals(Common.EVENT_ATTENDING_KEY)) {
                btnRSVP.setVisibility(View.GONE);
            }
            itemView.setOnClickListener(this);

        }

        public void bind(Event event) {
            // bind the event data to the view elements
            tvTitle.setText(event.getTitle());
            tvDescription.setText(event.getDescription());
            if (event.getImageUrl() != null) {
                Glide.with(context).load(event.getImageUrl()).into(ivEventPhoto);
            }
            tvTime.setText(formatDateWithDayOfWeek(event.getDate()) + " | " + event.getTime());
            tvLocation.setText(event.getLocation().getLocality());
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            // make sure the position is valid, i.e. actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                // get the event at the position
                Event event = events.get(position);
                // create intent for the new activity
                Intent intent = new Intent(context, EventDetailsActivity.class);
                // serialize the movie using parceler, use its short name as a key
                intent.putExtra(Event.class.getSimpleName(), Parcels.wrap(event));
                // show the activity
                context.startActivity(intent);
            }

        }
    }
}