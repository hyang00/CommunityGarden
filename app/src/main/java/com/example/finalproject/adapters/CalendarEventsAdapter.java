package com.example.finalproject.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.EventDetailsActivity;
import com.example.finalproject.R;
import com.example.finalproject.models.Event;

import org.parceler.Parcels;

import java.util.List;

import static com.example.finalproject.TimeAndDateFormatter.getDay;
import static com.example.finalproject.TimeAndDateFormatter.getMonth;

public class CalendarEventsAdapter extends RecyclerView.Adapter<CalendarEventsAdapter.ViewHolder> {


    private Context context;
    private List<Event> events;
    private String eventType = "";

    public CalendarEventsAdapter(Context context, List<Event> events, String eventType) {
        this.context = context;
        this.events = events;
        this.eventType = eventType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_calendar_event, parent, false);
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

        private TextView tvDay;
        private TextView tvMonth;
        private TextView tvTitle;
        private TextView tvTime;
        private TextView tvLocation;

        public ViewHolder(View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvMonth = itemView.findViewById(R.id.tvMonth);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvLocation = itemView.findViewById(R.id.tvLocation);
        }

        public void bind(Event event) {
            tvDay.setText(getDay(event.getDate()));
            tvMonth.setText(getMonth(event.getDate()));
            tvTitle.setText(event.getTitle());
            tvTime.setText(event.getTime());
            tvLocation.setText(event.getLocation().getWrittenAddress());
        }

        @Override
        public void onClick(View view) {
            //TODO: Change to diffent activity details
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
