package com.prime.launcher.PVR.Management;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.launcher.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PrimeCalendarView extends ConstraintLayout {
    String TAG = PrimeCalendarView.class.getSimpleName();
    WeakReference<AppCompatActivity> g_ref;

    private int g_year, g_month, g_day;

    public PrimeCalendarView(@NonNull Context context) {
        super(context);
        init_ui();
    }

    public PrimeCalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init_ui();
    }

    public PrimeCalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init_ui();
    }

    public void init_ui() {
        // layout
        inflate(getContext(), R.layout.view_calendar, this);
        set_on_key_listener();
    }

    public void set_data(Context context) {
        RecyclerView calendarRecyclerView = findViewById(R.id.new_timer_calendar_recycler_iew);
        CalendarAdapter calendarAdapter;

        if ( g_ref == null) {
            g_ref = new WeakReference<>((AppCompatActivity) context);
            Calendar calendar = Calendar.getInstance();
            g_year = calendar.get(Calendar.YEAR);
            g_month = calendar.get(Calendar.MONTH) + 1;
            g_day = calendar.get(Calendar.DATE);
            calendarRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));
            calendarAdapter = new CalendarAdapter(get_activity(), get_date_list(), g_year, g_month);
            calendarRecyclerView.setAdapter(calendarAdapter);
        } else {
            calendarAdapter = (CalendarAdapter)calendarRecyclerView.getAdapter();
            calendarAdapter.update(get_date_list(), g_year, g_month);
        }

        TextView calendarYear = findViewById(R.id.new_timer_calendar_year);
        calendarYear.setText(String.valueOf(g_year));

        TextView calendarMonth = findViewById(R.id.new_timer_calendar_month);
        calendarMonth.setText(String.valueOf(g_month));
    }

    public void request_focus_on_year() {
        ConstraintLayout calendarYearLayer = findViewById(R.id.new_timer_calendar_year_layer);
        calendarYearLayer.requestFocus();
    }

    public boolean is_show() {
        return getVisibility() == View.VISIBLE;
    }

    private TimerManagementActivity get_activity() {
        return (TimerManagementActivity)g_ref.get();
    }

    private List<String> get_date_tile_string() {
        List<String> dateTileString = new ArrayList<>();
        dateTileString.add(getContext().getString(R.string.sunday_short));
        dateTileString.add(getContext().getString(R.string.monday_short));
        dateTileString.add(getContext().getString(R.string.tuesday_short));
        dateTileString.add(getContext().getString(R.string.wednesday_short));
        dateTileString.add(getContext().getString(R.string.thursday_short));
        dateTileString.add(getContext().getString(R.string.friday_short));
        dateTileString.add(getContext().getString(R.string.saturday_short));
        return dateTileString;
    }

    private List<String> get_date_list() {
        List<String> dateTileList = get_date_tile_string();
        for (int i = 0; i < get_first_index_of_week(); i++)
            dateTileList.add("");

        for (int i = 1; i <= get_days_of_month(); i++)
            dateTileList.add(String.valueOf(i));

        return dateTileList;
    }

    private int get_first_index_of_week() {
        Log.d(TAG, "get_first_index_of_week: year:" + g_year + " month:" + g_month + " day:" + 1);
        Calendar calendar = Calendar.getInstance();
        calendar.set(g_year, g_month - 1, 1);
        return calendar.get(Calendar.DAY_OF_WEEK) - 1;
    }

    private int get_days_of_month() {
        Log.d(TAG, "get_days_of_month: year:" + g_year + " month:" + g_month);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, g_year);
        calendar.set(Calendar.MONTH, g_month -1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private void update_calendar(int year, int month) {
        g_year = year;
        g_month = month;

        if (g_month == 0)
            g_month = 12;
        else if (g_month == 13)
            g_month = 1;

        TextView calendarYear = findViewById(R.id.new_timer_calendar_year);
        calendarYear.setText(String.valueOf(g_year));

        TextView calendarMonth = findViewById(R.id.new_timer_calendar_month);
        calendarMonth.setText(String.valueOf(g_month));

        RecyclerView calendarRecyclerView = findViewById(R.id.new_timer_calendar_recycler_iew);
        CalendarAdapter calendarAdapter = (CalendarAdapter) calendarRecyclerView.getAdapter();
        calendarAdapter.update(get_date_list(), g_year, g_month);
    }

    private void set_on_key_listener() {
        ConstraintLayout calendarYearLayer = findViewById(R.id.new_timer_calendar_year_layer);
        calendarYearLayer.setOnKeyListener((view, keycode, keyevent)->{
            if (keyevent.getAction() == KeyEvent.ACTION_UP)
                return true;

            if (keycode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                update_calendar(g_year+1, g_month);
                return true;
            }
            else if (keycode == KeyEvent.KEYCODE_DPAD_LEFT) {
                update_calendar(g_year-1, g_month);
                return true;
            }
            return false;
        });

        ConstraintLayout calendarMonthLayer = findViewById(R.id.new_timer_calendar_month_layer);
        calendarMonthLayer.setOnKeyListener((view, keycode, keyevent)->{
            if (keyevent.getAction() == KeyEvent.ACTION_UP)
                return true;

            if (keycode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                update_calendar(g_year, g_month+1);
                return true;
            }
            else if (keycode == KeyEvent.KEYCODE_DPAD_LEFT) {
                update_calendar(g_year, g_month-1);
                return true;
            }
            return false;
        });
    }
}
