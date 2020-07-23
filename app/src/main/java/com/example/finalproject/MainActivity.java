package com.example.finalproject;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.finalproject.fragments.EventFragment;
import com.example.finalproject.fragments.HostEventFragment;
import com.example.finalproject.fragments.ProfileFragment;
import com.example.finalproject.fragments.UserEventFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import static com.example.finalproject.Common.HOME_FRAGMENT;
import static com.example.finalproject.Common.MAIN_ACT_FRG_TO_LOAD_KEY;
import static com.example.finalproject.Common.PROFILE_FRAGMENT;
import static com.example.finalproject.Common.USER_EVENTS_FRAGMENT;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";

    private BottomNavigationView bottomNavigationView;
    private Fragment fragment;
    private int defaultFragment = R.id.action_home;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        int intentFragment = getIntent().getIntExtra(MAIN_ACT_FRG_TO_LOAD_KEY, 0);

        if (intentFragment!=0){
            switch (intentFragment){
                case HOME_FRAGMENT:
                    defaultFragment = R.id.action_home;
                    break;
                case Common.HOST_FRAGMENT:
                    defaultFragment = R.id.action_host;
                    break;
                case USER_EVENTS_FRAGMENT:
                   defaultFragment = R.id.action_user_events;
                    break;
                case PROFILE_FRAGMENT:
                    defaultFragment = R.id.action_profile;
                    break;
            }
        }

        // handle navigation selection
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_home:
                                fragment = EventFragment.newInstance(Common.EVENT_FEED_KEY);
                                break;
                            case R.id.action_host:
                                fragment = new HostEventFragment();
                                break;
                            case R.id.action_user_events:
                                fragment = new UserEventFragment();
                                break;
                            case R.id.action_profile:
                                fragment = new ProfileFragment();
                                break;
                            default:
                                fragment = new EventFragment();
                                break;
                        }
                        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                        return true;
                    }
                });
        // Set default selection
        bottomNavigationView.setSelectedItemId(defaultFragment);
    }
}