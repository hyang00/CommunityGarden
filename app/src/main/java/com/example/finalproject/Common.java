package com.example.finalproject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

//
public class Common {
    public static final String EVENT_FEED_KEY = "feed";
    public static final String EVENT_ATTENDING_KEY = "attending";
    public static final String EVENT_HOSTING_KEY = "hosting";
    public static final String MAIN_ACT_FRG_TO_LOAD_KEY = "frgToLoad";
    public static final int HOME_FRAGMENT = 1;
    public static final int HOST_FRAGMENT = 2;
    public static final int USER_EVENTS_FRAGMENT = 3;
    public static final int PROFILE_FRAGMENT = 4;
    public static final Long NO_ATTENDEES_CAP_SET = Long.MAX_VALUE;
    public static final String NO_LABEL_FOUND = "no label";
    public static Map<String, ArrayList<String>> TAGS = new HashMap<>();

    static {
        TAGS.put("Edible Gardening", new ArrayList<String>(Arrays.asList("vegetable", "herb", "edible", "fruit")));
        TAGS.put("Flower Garden", new ArrayList<String>(Arrays.asList("flower", "rose", "tulip")));
        TAGS.put("Drought Tolerant", new ArrayList<String>(Arrays.asList("cact", "drought", "rock")));
    }
//    public static final ArrayList<String> TAGS = new ArrayList<String>(
//            Arrays.asList("Edibles", "Flower", "Drought Tolerant")
//    );

}
