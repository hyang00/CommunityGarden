package com.example.finalproject.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.finalproject.Common;
import com.example.finalproject.fragments.CalendarEventFragment;

public class UserEventsTabAdapter extends FragmentPagerAdapter {

    private int totalTabs;

    public UserEventsTabAdapter(@NonNull FragmentManager fm, int behavior, int totalTabs) {
        super(fm, behavior);
        this.totalTabs = totalTabs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                CalendarEventFragment attendingFragment = CalendarEventFragment.newInstance(Common.EVENT_ATTENDING_KEY);
                return attendingFragment;
            case 1:
                CalendarEventFragment hostingFragment = CalendarEventFragment.newInstance(Common.EVENT_HOSTING_KEY);
                return hostingFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return totalTabs;
    }
}
