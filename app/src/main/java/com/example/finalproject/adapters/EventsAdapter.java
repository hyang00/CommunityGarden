package com.example.finalproject.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.R;
import com.example.finalproject.models.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {

    private Context context;
    private List<Event> events;

    public EventsAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView tvTitle;
        private ImageView ivEventPhoto;
        private TextView tvDescription;
        private Button btnRSVP;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            ivEventPhoto = itemView.findViewById(R.id.ivEventPhoto);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnRSVP = itemView.findViewById(R.id.btnRSVP);
            btnRSVP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    Event event = events.get(position);
                    Log.i("adapter", "event key: " + event.getEventId());
                    //if(position !=RecyclerView.NO_POSITION)
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference eventRef = database.child("Posts").child(event.getEventId());
                    DatabaseReference userEventRef = database.child("User");//.child(uid);

                    // check if the user has already rsvp'd
                    if (!event.isAttending(uid)){
                        event.addAttendee(uid);
                        Map<String, Object> map = new HashMap<>();
                        map.put("attendees", event.getAttendees());
                        eventRef.updateChildren(map);
                        //Map<String, Object> map2 = new HashMap<>();
                        //map.put(uid, true);
                        //userEventRef.updateChildren(map2);
                        Toast.makeText(context, "Successfully Registered", Toast.LENGTH_SHORT).show();
                        database.child("UserEvents").child(uid).child("eventsAttending").child(event.getEventId()).setValue(true);

                    } else {
                        Toast.makeText(context, "Already Registered", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

        public void bind(Event event) {
            // bind the event data to the view elements
            tvTitle.setText(event.getTitle());
            tvDescription.setText(event.getDescription());
        }

        @Override
        public void onClick(View view) {
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            String ref = database.child("Posts").getKey();
            Log.i("adapter", ref );
            //Query phoneQuery = ref.orderByChild(phoneNo).equalTo("+923336091371");
//            ref.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    adapter.clear();
//                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
//                        Event event = singleSnapshot.getValue(Event.class);
//                        adapter.add(event);
//                        adapter.notifyDataSetChanged();
//
//                    }
//                    swipeContainer.setRefreshing(false);
//                }
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//                    Log.e(TAG, "onCancelled", databaseError.toException());
//                }
//            });
        }
    }
}