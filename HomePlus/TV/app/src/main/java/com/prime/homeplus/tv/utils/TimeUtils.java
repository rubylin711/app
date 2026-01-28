package com.prime.homeplus.tv.utils;

import android.util.Log;
import android.widget.ProgressBar;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {
    private static final String TAG = "TimeUtils";

    public static boolean isToday(long unixTimestamp) {
        Calendar now = Calendar.getInstance();

        Calendar startOfDay = (Calendar) now.clone();
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);

        Calendar endOfDay = (Calendar) now.clone();
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);
        endOfDay.set(Calendar.MILLISECOND, 999);

        Calendar targetDate = Calendar.getInstance();
        targetDate.setTimeInMillis(unixTimestamp);

        return !targetDate.before(startOfDay) && !targetDate.after(endOfDay);
    }

    public static boolean isStartOfLocalDay(long timestampMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestampMillis);
        return calendar.get(Calendar.HOUR_OF_DAY) == 0 &&
                calendar.get(Calendar.MINUTE) == 0 &&
                calendar.get(Calendar.SECOND) == 0;
    }

    public static String formatToLocalTime(long utcMillis, String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) {
            return "";
        }

        try {
            if (utcMillis < 0 || utcMillis > 4102444800000L) { // valid from 1970 to ~2100
                return "";
            }

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(pattern);
            sdf.setTimeZone(java.util.TimeZone.getDefault()); // local time
            return sdf.format(new java.util.Date(utcMillis));

        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.toString());
            return "";
        }
    }

    public static boolean isInTimeRange(long start, long end) {
        long now = System.currentTimeMillis();
        return now >= start && now < end;
    }

    public static boolean isValidTime(int hour, int minute) {
        return hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59;
    }

    public static String formatMillisToTime(long millis) {
        if (millis < 0) millis = 0;
        long seconds = millis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    public static List<Long> generateTimestampsFromNowThenMidnights(int days) {
        List<Long> dateList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance(); // Get current date and time

        for (int i = 0; i < days; i++) {
            long timestamp;

            if (i == 0) {
                timestamp = calendar.getTimeInMillis();
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                calendar.add(Calendar.DAY_OF_MONTH, 1); // Move to the next day
                timestamp = calendar.getTimeInMillis();
            }

            dateList.add(timestamp);
        }

        return dateList;
    }

    public static String formatTimestampToDateYyMmDd(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yy.MM.dd", Locale.getDefault());
        return dateFormat.format(new Date(timestamp));
    }

    public static String formatTimestampWithHourMinute(long timestamp) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return timeFormat.format(new Date(timestamp));
    }

    public static String formatTimestampWithWeekday(long timestamp) {
        // Check if the system language is Chinese
        boolean isChinese = isSystemLanguageChinese();

        // Define the date part format
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd", Locale.getDefault());

        // Format the timestamp into the date part
        String formattedDate = dateFormat.format(new Date(timestamp));

        // Get the weekday part
        String weekday = getWeekday(timestamp, isChinese);

        // Combine the date and weekday parts
        if (isChinese) {
            return formattedDate + "(" + weekday + ")";
        } else {
            return formattedDate + " (" + weekday + ")";
        }
    }

    private static String getWeekday(long timestamp, boolean isChinese) {
        // Create a Calendar instance and set it to the given timestamp
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        // Get the weekday index (1: Sunday, 2: Monday, ..., 7: Saturday)
        int weekdayIndex = calendar.get(Calendar.DAY_OF_WEEK); // Returns values from Calendar.SUNDAY (1) to Calendar.SATURDAY (7)

        // Define the mapping for Chinese and English weekdays
        String[] chineseWeekdays = {"日", "一", "二", "三", "四", "五", "六"};
        String[] englishWeekdays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

        // Return the corresponding weekday string based on the language
        if (isChinese) {
            return chineseWeekdays[weekdayIndex - 1]; // Adjust index to match array
        } else {
            return englishWeekdays[weekdayIndex - 1];
        }
    }

    private static boolean isSystemLanguageChinese() {
        Locale locale = Locale.getDefault();
        return locale.getLanguage().equals(Locale.SIMPLIFIED_CHINESE.getLanguage()) ||
                locale.getLanguage().equals(Locale.TRADITIONAL_CHINESE.getLanguage());
    }
}
