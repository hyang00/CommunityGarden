package com.example.finalproject.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.finalproject.EndlessRecyclerViewScrollListener;
import com.example.finalproject.R;
import com.example.finalproject.adapters.EventsAdapter;
import com.example.finalproject.models.Event;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EventFragment extends Fragment {

    public static final  String TAG = "Event Fragment";

    private RecyclerView rvEvents;
    protected EventsAdapter adapter;
    protected List<Event> allEvents;
    protected SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;

    public EventFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvEvents = view.findViewById(R.id.rvEvents);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        allEvents = new ArrayList<>();
        adapter = new EventsAdapter(getContext(), allEvents);

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

    private void loadNextDataFromApi(int page, int totalItemsCount) {
    }

    protected void queryEvents() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = database.child("Posts");
        //Query phoneQuery = ref.orderByChild(phoneNo).equalTo("+923336091371");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapter.clear();
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    Event event = singleSnapshot.getValue(Event.class);
                    event.setEventId(singleSnapshot.getKey());
                    adapter.add(event);
                    Log.i(TAG, event.getTitle() + ": " + event.getEventId());
                }
                adapter.notifyDataSetChanged();
                swipeContainer.setRefreshing(false);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
    }
}