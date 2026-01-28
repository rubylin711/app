package com.prime.launcher.CustomView;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prime.launcher.R;

public class TimeViewKeyHint extends LinearLayout {

    public TimeViewKeyHint(Context context) {
        super(context);
        init();
    }

    public TimeViewKeyHint(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimeViewKeyHint(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_time_key_hint, this);
        initParameter();
    }

    private void initParameter() {
        TextView AirTemperature = findViewById(R.id.lo_time_view_key_hint_air_temperature);
        AirTemperature.setText(updateAirTemperature("23"));
    }

    private SpannableStringBuilder updateAirTemperature(String temp) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(temp + "oC");
        int indexOf = spannableStringBuilder.toString().indexOf("o");
        int i = indexOf + 1;
        spannableStringBuilder.setSpan(new SuperscriptSpan(), indexOf, i, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.setSpan(new AbsoluteSizeSpan(20), indexOf, i, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableStringBuilder;
    }
}
