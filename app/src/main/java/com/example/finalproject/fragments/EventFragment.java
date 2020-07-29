package com.example.finalproject.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.finalproject.DatabaseClient;
import com.example.finalproject.EndlessRecyclerViewScrollListener;
import com.example.finalproject.EventDetailsActivity;
import com.example.finalproject.ItemClickSupport;
import com.example.finalproject.R;
import com.example.finalproject.adapters.EventsAdapter;
import com.example.finalproject.models.Event;
import com.example.finalproject.models.Location;
import com.example.finalproject.models.User;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.dionsegijn.konfetti.KonfettiView;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

@SuppressWarnings("ALL")
public class EventFragment extends Fragment {

    private static final String TAG = "Event Fragment";

    // fragment initialization parameter (determine whether standard event feed, hosting event feed, attending feed)
    private static final String ARG_EVENT_TYPE = "Event Type";
    private static int AUTOCOMPLETE_REQUEST_CODE = 1;

    private RecyclerView rvEvents;
    protected EventsAdapter adapter;
    protected List<Event> allEvents;
    protected SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;
    protected String eventType;
    private TextView tvDefaultMessage;
    private CollapsingToolbarLayout collapsingToolbar;
    private TextView tvLocation;
    private ImageView ivSearch;
    private ShimmerFrameLayout mShimmerViewContainer;
    private String searchLocation;

    public EventFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param eventType Parameter 1.
     * @return A new instance of fragment UserPostFragment.
     */
    public static EventFragment newInstance(String eventType) {
        EventFragment fragment = new EventFragment();
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
        return inflater.inflate(R.layout.fragment_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!Places.isInitialized()) {
            Places.initialize(getContext(), getString(R.string.api_key));
        }
        PlacesClient placesClient = Places.createClient(getContext());

        rvEvents = view.findViewById(R.id.rvEvents);

        tvDefaultMessage = view.findViewById(R.id.tvDefaultMessage);
        collapsingToolbar = view.findViewById(R.id.collapsing_toolbar);
        tvLocation = view.findViewById(R.id.tvLocation);
        mShimmerViewContainer = view.findViewById(R.id.shimmer_view_container);
        ivSearch = view.findViewById(R.id.ivSearch);
        final KonfettiView konfettiView = view.findViewById(R.id.viewKonfetti);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        allEvents = new ArrayList<>();

        //adapter = new EventsAdapter(getContext(), allEvents, eventType);
        adapter = new EventsAdapter(getContext(), allEvents, eventType, konfettiView);

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryEventsNearby(searchLocation);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(getContext());
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            }
        });


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

        ItemClickSupport.addTo(rvEvents).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                if (position != RecyclerView.NO_POSITION) {
                    Event event = allEvents.get(position);
                    Intent intent = new Intent(getContext(), EventDetailsActivity.class);
                    intent.putExtra(Event.class.getSimpleName(), Parcels.wrap(event));
                    getContext().startActivity(intent);
                }
            }

            @Override
            public void onItemDoubleClicked(RecyclerView recyclerView, final int position, View v) {
                if (position != RecyclerView.NO_POSITION) {
                    final Event event = allEvents.get(position);
                    DatabaseClient.rsvpUser(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getContext(), "Successfully Registered", Toast.LENGTH_SHORT).show();
                            adapter.removeAt(position);
                            if (konfettiView != null) {
                                adapter.launchConfetti();
                            }
                        }
                    }, event, getContext());
                    Log.i("adapter", "event key: " + event.getEventId());
                }
            }
        });

        queryEventsNearby(searchLocation);

    }

    @Override
    public void onResume() {
        super.onResume();
        mShimmerViewContainer.startShimmerAnimation();
    }

    @Override
    public void onPause() {
        mShimmerViewContainer.stopShimmerAnimation();
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Location location = new Location(place.getAddress(), getContext());
                searchLocation = location.getLocality();
                tvLocation.setText(searchLocation);
                queryEventsNearby(searchLocation);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.e(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void setDefaultIfEmpty() {
        if (adapter.isEmpty()) {
            rvEvents.setVisibility(View.GONE);
            tvDefaultMessage.setVisibility(View.VISIBLE);
        } else {
            rvEvents.setVisibility(View.VISIBLE);
            tvDefaultMessage.setVisibility(View.GONE);
        }
    }

    private void queryEventsNearby(String searchLocation) {
        if (searchLocation == null) {
            queryEventsWithDefaultUserLocation();
        } else {
            DatabaseClient.queryEventsNearby(new ValueEventListener() {
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
                    mShimmerViewContainer.stopShimmerAnimation();
                    mShimmerViewContainer.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            }, searchLocation);
        }
    }

    private void queryEventsWithDefaultUserLocation() {
        DatabaseClient.getCurrUserProfile(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final User user = snapshot.getValue(User.class);
                DatabaseClient.queryEventsNearby(new ValueEventListener() {
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
                        tvLocation.setText(user.getLocation().getLocality());
                        swipeContainer.setRefreshing(false);
                        mShimmerViewContainer.stopShimmerAnimation();
                        mShimmerViewContainer.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }, user.getLocation().getLocality());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // check whether the event should be added to the feed depending on which feed it is
    private boolean isValid(Event event) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (!event.getAuthor().equals(uid) && !event.isAttending(uid)) {
            return true;
        }
        return false;
    }

    // TODO: limit initial query and implement infinite scrolling
    private void loadNextDataFromApi(int page, int totalItemsCount) {
    }

}