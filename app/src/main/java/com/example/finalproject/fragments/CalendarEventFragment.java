package com.example.finalproject.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.finalproject.Common;
import com.example.finalproject.DatabaseClient;
import com.example.finalproject.EndlessRecyclerViewScrollListener;
import com.example.finalproject.R;
import com.example.finalproject.adapters.CalendarEventsAdapter;
import com.example.finalproject.models.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarEventFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarEventFragment extends Fragment {

    private static final String TAG = "Calendar Event Fragment";

    // fragment initialization parameter (determine whether standard event feed, hosting event feed, attending feed)
    private static final String ARG_EVENT_TYPE = "Event Type";

    private RecyclerView rvEvents;
    private CalendarEventsAdapter adapter;
    private List<Event> events;
    private SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;
    private TextView tvDefaultMessage;
    private String eventType;

    public CalendarEventFragment() {
        // Required empty public constructor
    }

    /**
     * @param eventType Parameter 1.
     * @return A new instance of fragment CalendarEventFragment.
     */
    public static CalendarEventFragment newInstance(String eventType) {
        CalendarEventFragment fragment = new CalendarEventFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_TYPE, eventType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventType = getArguments().getString(ARG_EVENT_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvDefaultMessage = view.findViewById(R.id.tvDefaultMessage);
        rvEvents = view.findViewById(R.id.rvEvents);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        events = new ArrayList<>();
        adapter = new CalendarEventsAdapter(getContext(), events, eventType);

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryEvents();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // set the adapter on the recycler view
        rvEvents.setAdapter(adapter);
        // set the layout manager on the recycler view
        rvEvents.setLayoutManager(linearLayoutManager);
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadNextDataFromApi(page, totalItemsCount);
            }
        };
        rvEvents.addOnScrollListener(scrollListener);
        queryEvents();
    }

    private void setDefaultIfEmpty(){
        if (adapter.isEmpty()){
            rvEvents.setVisibility(View.GONE);
            tvDefaultMessage.setVisibility(View.VISIBLE);
        } else{
            rvEvents.setVisibility(View.VISIBLE);
            tvDefaultMessage.setVisibility(View.GONE);
        }
    }

    private void queryEvents() {
        DatabaseClient.queryEvents(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adapter.clear();
                for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
                    Event event = singleSnapshot.getValue(Event.class);
                    event.setEventId(singleSnapshot.getKey());
                    if (isValid(event)) {
                        adapter.add(event);
                    }
                }
                setDefaultIfEmpty();
                adapter.notifyDataSetChanged();
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // check whether the event should be added to the feed depending on which feed it is
    private boolean isValid(Event event) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        switch (eventType) {
            case Common.EVENT_ATTENDING_KEY:
                if (event.isAttending(uid)) {
                    return true;
                }
                break;
            case Common.EVENT_HOSTING_KEY:
                if (event.getAuthor().equals(uid)) {
                    return true;
                }
        }
        return false;
    }

    // TODO: limit initial query and implement infinite scrolling
    private void loadNextDataFromApi(int page, int totalItemsCount) {
    }
}