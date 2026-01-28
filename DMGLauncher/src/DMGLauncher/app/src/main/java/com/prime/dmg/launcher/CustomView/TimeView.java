package com.prime.dmg.launcher.CustomView;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextClock;

import com.prime.dmg.launcher.R;

public class TimeView extends RelativeLayout {

    TextClock g_CurrentWeek, g_CurrentDate, g_CurrentTime, g_CurrentAmpm;

    public TimeView(Context context) {
        super(context);
        init_time_view();
    }

    public TimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init_time_view();
    }

    public TimeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init_time_view();
    }

    public TimeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init_time_view();
    }

    void init_time_view() {
        inflate(getContext(), R.layout.view_time, this);
        g_CurrentWeek = findViewById(R.id.lo_current_week);
        g_CurrentDate = findViewById(R.id.lo_current_date);
        g_CurrentTime = findViewById(R.id.lo_current_time);
        g_CurrentAmpm = findViewById(R.id.lo_current_ampm);
    }
}
