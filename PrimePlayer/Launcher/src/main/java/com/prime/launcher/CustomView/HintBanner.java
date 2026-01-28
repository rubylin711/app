package com.prime.launcher.CustomView;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.prime.launcher.PVR.Management.RecordProgramsActivity;
import com.prime.launcher.R;

public class HintBanner extends LinearLayout {

    private static final String TAG = HintBanner.class.getSimpleName();

    public HintBanner(Context context) {
        super(context);
        initAll();
    }

    public HintBanner(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAll();
    }

    public HintBanner(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAll();
    }

    public HintBanner(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAll();
    }

    private void initAll() {
        inflate(getContext(), R.layout.view_bottom, this);
    }

    public void set_hint_fullscreen(boolean visible) {
        RecordProgramsActivity activity = (RecordProgramsActivity) getContext();
        Handler handler = activity.get_handler();
        handler.postDelayed(() -> {
            if (visible)    show_fullscreen();
            else            hide_fullscreen();
        }, 100);
    }

    public void set_hint_ok(int resId) {
        TextView okText = findViewById(R.id.ok_text);
        okText.setVisibility(View.VISIBLE);
        okText.setText(resId);
    }

    public void set_hint_back(int resId) {
        TextView backText = findViewById(R.id.bottom_back_text);
        backText.setVisibility(View.VISIBLE);
        backText.setText(resId);
    }

    public void set_red_text(int resId) {
        TextView redText = findViewById(R.id.fullscreen_text);
        redText.setText(resId);
    }

    public void set_green_text(int resId) {
        TextView greenText = findViewById(R.id.green_text);
        greenText.setText(resId);
    }

    public void set_yellow_text(int resId) {
        TextView yellowText = findViewById(R.id.yellow_text);
        yellowText.setText(resId);
    }

    public void set_blue_text(int resId) {
        TextView blueText = findViewById(R.id.blue_text);
        blueText.setText(resId);
    }

    public void set_ok_text(int resId) {
        TextView okText = findViewById(R.id.ok_text);
        okText.setText(resId);
    }

    public void show_back() {
        View backIcon = findViewById(R.id.bottom_back_icon);
        View backText = findViewById(R.id.bottom_back_text);
        backIcon.setVisibility(View.VISIBLE);
        backText.setVisibility(View.VISIBLE);
    }

    public void hide_back() {
        View backIcon = findViewById(R.id.bottom_back_icon);
        View backText = findViewById(R.id.bottom_back_text);
        backIcon.setVisibility(View.GONE);
        backText.setVisibility(View.GONE);
    }

    private void show_fullscreen() {
        View fullscreenIcon = findViewById(R.id.fullscreen_icon);
        View fullscreenText = findViewById(R.id.fullscreen_text);
        fullscreenIcon.setVisibility(View.VISIBLE);
        fullscreenText.setVisibility(View.VISIBLE);
    }

    private void hide_fullscreen() {
        View fullscreenIcon = findViewById(R.id.fullscreen_icon);
        View fullscreenText = findViewById(R.id.fullscreen_text);
        fullscreenIcon.setVisibility(View.GONE);
        fullscreenText.setVisibility(View.GONE);
    }

    public void show_red() {
        View fullscreenIcon = findViewById(R.id.fullscreen_icon);
        View fullscreenText = findViewById(R.id.fullscreen_text);
        fullscreenIcon.setVisibility(View.VISIBLE);
        fullscreenText.setVisibility(View.VISIBLE);
    }

    public void hide_red() {
        View fullscreenIcon = findViewById(R.id.fullscreen_icon);
        View fullscreenText = findViewById(R.id.fullscreen_text);
        fullscreenIcon.setVisibility(View.GONE);
        fullscreenText.setVisibility(View.GONE);
    }

    public void set_hint_green(int resId) {
        TextView introText = findViewById(R.id.green_text);
        introText.setVisibility(View.VISIBLE);
        introText.setText(resId);
    }

    public void show_green() {
        View greenIcon = findViewById(R.id.green_icon);
        View greenText = findViewById(R.id.green_text);
        greenIcon.setVisibility(View.VISIBLE);
        greenText.setVisibility(View.VISIBLE);
    }

    public void hide_green() {
        View greenIcon = findViewById(R.id.green_icon);
        View greenText = findViewById(R.id.green_text);
        greenIcon.setVisibility(View.GONE);
        greenText.setVisibility(View.GONE);
    }

    public void set_hint_yellow(int resId) {
        TextView introText = findViewById(R.id.yellow_text);
        introText.setVisibility(View.VISIBLE);
        introText.setText(resId);
    }

    public void show_yellow() {
        View yellowIcon = findViewById(R.id.yellow_icon);
        View yellowText = findViewById(R.id.yellow_text);
        yellowIcon.setVisibility(View.VISIBLE);
        yellowText.setVisibility(View.VISIBLE);
    }

    public void hide_yellow() {
        View yellowIcon = findViewById(R.id.yellow_icon);
        View yellowText = findViewById(R.id.yellow_text);
        yellowIcon.setVisibility(View.GONE);
        yellowText.setVisibility(View.GONE);
    }

    public void show_blue() {
        View blueIcon = findViewById(R.id.blue_icon);
        View blueText = findViewById(R.id.blue_text);
        blueIcon.setVisibility(View.VISIBLE);
        blueText.setVisibility(View.VISIBLE);
    }

    public void hide_blue() {
        View blueIcon = findViewById(R.id.blue_icon);
        View blueText = findViewById(R.id.blue_text);
        blueIcon.setVisibility(View.GONE);
        blueText.setVisibility(View.GONE);
    }

    public void show_ok() {
        View okIcon = findViewById(R.id.ok_icon);
        View okText = findViewById(R.id.ok_text);
        okIcon.setVisibility(View.VISIBLE);
        okText.setVisibility(View.VISIBLE);
    }

    public void hide_ok() {
        View okIcon = findViewById(R.id.ok_icon);
        View okText = findViewById(R.id.ok_text);
        okIcon.setVisibility(View.GONE);
        okText.setVisibility(View.GONE);
    }

    public void hide_time() {
        View hintTime = findViewById(R.id.hint_time);
        hintTime.setVisibility(View.GONE);
    }

    public void set_hint_intro(int resId) {
        TextView introText = findViewById(R.id.bottom_intro_text);
        introText.setVisibility(View.VISIBLE);
        introText.setText(resId);
    }

    public void show_intro() {
        View introIcon = findViewById(R.id.bottom_intro_icon);
        View introText = findViewById(R.id.bottom_intro_text);
        introIcon.setVisibility(View.VISIBLE);
        introText.setVisibility(View.VISIBLE);
    }

    public void hide_intro() {
        View introIcon = findViewById(R.id.bottom_intro_icon);
        View introText = findViewById(R.id.bottom_intro_text);
        introIcon.setVisibility(View.GONE);
        introText.setVisibility(View.GONE);
    }

    public void enable_shadow(boolean enable) {
        View banner = findViewById(R.id.hint_banner);

        if (enable) {
            banner.setBackgroundResource(R.drawable.hint_shadow);
        } else {
            banner.setBackgroundResource(0);
        }
    }

    public void set_hint_prev_page() {
        ImageView backIcon = findViewById(R.id.bottom_back_icon);
        set_hint_back(R.string.hint_prev_page);
        backIcon.setImageResource(R.drawable.icon_app_back);
        show_back();
    }
}
