package com.example.finalproject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeAndDateFormatter {

    // Returns day in DD format from YYYY/MM/DD format
    public static String getDay(String storageDate) {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy/MM/dd").parse(storageDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat formatter = new SimpleDateFormat("dd");
        return formatter.format(date);
    }

    // Returns name of month (ex: Oct) from YYYY/MM/DD format
    public static String getMonth(String storageDate) {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy/MM/dd").parse(storageDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat formatter = new SimpleDateFormat("MMM");
        return formatter.format(date);
    }

    public static String formatDateWithDayOfWeek(String storageDate) {
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy/MM/dd").parse(storageDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat formatter = new SimpleDateFormat("E, MMM dd");
        return formatter.format(date);
    }

    // Takes in hours/24 and minutes/60 and formats it as 1:00 PM form
    public static String formatTime(int hour, int min) {
        String format = " AM";
        String minutes = "" + min;
        if (hour >= 12) {
            format = " PM";
        }
        if (hour == 0) {
            hour += 12;
        } else if (hour > 12) {
            hour -= 12;
        }
        if (min < 10) {
            minutes = "0" + minutes;
        }
        return "" + hour + ":" + minutes + format;
    }

    // returns date formatted in MM/DD/YYYY
    public static String formatDateForView(int year, int monthOfYear, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, monthOfYear);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        Date date = cal.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        return formatter.format(date);
    }

    //changes date formated in MM/DD/YYYY to YYYY/MM/DD so events can be sorted according to date in firebase
    public static String formatDateForStorage(String dateForView) {
        Date date = null;
        try {
            date = new SimpleDateFormat("MM/dd/yyyy").parse(dateForView);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        return formatter.format(date);
    }

}
