package com.example.finalproject.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.finalproject.Common;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import nl.dionsegijn.konfetti.KonfettiView;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.example.finalproject.TimeAndDateFormatter.formatDateForStorage;


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
    private ImageView ivPickDate;
    private ShimmerFrameLayout mShimmerViewContainer;
    private ChipGroup cgTags;
    private String searchLocation;
    private String searchDate;
    private HashMap<String, Boolean> selectedTags = new HashMap<>();

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

        // Get instance of placesClient to use Places Autocomplete Location
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
        ivPickDate = view.findViewById(R.id.ivPickDate);
        cgTags = view.findViewById(R.id.cgTags);
        final KonfettiView konfettiView = view.findViewById(R.id.viewKonfetti);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        allEvents = new ArrayList<>();

        adapter = new EventsAdapter(getContext(), allEvents, eventType, konfettiView);

        // for refresh on swipe down at top of recycler view
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryEventsNearby(searchLocation);
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // Allow user to change location that they are searching for events in
        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(getContext());
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            }
        });

        // Allow user to search for events on specific date
        ivPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                DatePickerDialog picker = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                Log.i(TAG, formatDateForStorage(year, monthOfYear, dayOfMonth));
                                searchDate = formatDateForStorage(year, monthOfYear, dayOfMonth);
                                queryEventsByDate(formatDateForStorage(year, monthOfYear, dayOfMonth));
                            }
                        }, year, month, day);
                picker.show();
            }
        });

        for (String tag : Common.TAGS.keySet()) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_filter, null, false);
            chip.setText(tag);
            Log.i(TAG, tag);
            cgTags.addView(chip);
            chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    String tag = compoundButton.getText().toString();
                    if (checked) {
                        selectedTags.put(tag, true);
                    } else {
                        selectedTags.remove(tag);
                    }
                    Log.i(TAG, tag + "boolean: " + checked);
                    queryEventsNearby(searchLocation);
                }
            });
        }

        cgTags.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {

            }
        });
        rvEvents.setAdapter(adapter);
        rvEvents.setLayoutManager(linearLayoutManager);
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadNextDataFromApi(page, totalItemsCount);
            }
        };
        rvEvents.addOnScrollListener(scrollListener);

        // Listener for single and double tap events on recycler view items
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

    private void queryEventsNearby(final String searchLocation) {
        if (searchLocation == null) {
            queryEventsWithDefaultUserLocation();
        } else if (searchDate != null) {
            queryEventsByDate(searchDate);
        } else {
            DatabaseClient.queryEvents(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    loadData(snapshot);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, error.toString());
                }
            });
        }
    }

    // Query events w/ User location if search location is unknown
    private void queryEventsWithDefaultUserLocation() {
        DatabaseClient.getCurrUserProfile(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                searchLocation = user.getLocation().getLocality();
                queryEventsNearby(searchLocation);
                tvLocation.setText(searchLocation);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.toString());
            }
        });
    }

    // Filter current events by date
    private void queryEventsByDate(String date) {
        DatabaseClient.queryEventsOnDate(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadData(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.toString());
            }
        }, date);
    }

    private boolean hasTags(Event event) {
        for (String tag : selectedTags.keySet()) {
            if (!event.containsTag(tag)) {
                return false;
            }
        }
        return true;
    }

    // check whether the event should be added to the feed
    private boolean isValid(Event event) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return (!event.getAuthor().equals(uid) && !event.isAttending(uid) && hasTags(event));
    }

    // check if the event is in the same locality as user's search locality
    private static boolean isNearby(String searchLocation, Event event) {
        return (searchLocation.equals(event.getLocation().getLocality()));
    }

    // Add queried data into the adapter and check if it is valid
    // stop refreshing on the swipecontainer, and stop shimmer effect
    private void loadData(DataSnapshot snapshot) {
        adapter.clear();
        for (DataSnapshot singleSnapshot : snapshot.getChildren()) {
            Event event = singleSnapshot.getValue(Event.class);
            event.setEventId(singleSnapshot.getKey());
            if (isNearby(searchLocation, event) && isValid(event)) {
                adapter.add(event);
            }
        }
        setDefaultIfEmpty();
        adapter.notifyDataSetChanged();
        swipeContainer.setRefreshing(false);
        mShimmerViewContainer.stopShimmerAnimation();
        mShimmerViewContainer.setVisibility(View.GONE);
    }

    // Show default message if no events based on query are found
    private void setDefaultIfEmpty() {
        if (adapter.isEmpty()) {
            rvEvents.setVisibility(View.GONE);
            tvDefaultMessage.setVisibility(View.VISIBLE);
        } else {
            rvEvents.setVisibility(View.VISIBLE);
            tvDefaultMessage.setVisibility(View.GONE);
        }
    }

    // TODO: limit initial query and implement infinite scrolling
    private void loadNextDataFromApi(int page, int totalItemsCount) {
    }

}