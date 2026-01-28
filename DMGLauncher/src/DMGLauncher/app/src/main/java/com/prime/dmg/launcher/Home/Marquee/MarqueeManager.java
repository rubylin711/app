package com.prime.dmg.launcher.Home.Marquee;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.HomeApplication;
import com.prime.dmg.launcher.Ticker.Ticker;
import com.prime.dtv.ChannelChangeManager;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;

public class MarqueeManager implements MarqueeSurfaceView.MarqueeCallback {

    String TAG = MarqueeManager.class.getSimpleName();

    public static final int MARQUEE_STOP   =   0;
    public static final int MARQUEE_START   =   1;

    static final int MARQUEE_TYPE_ANIMATOR      = 0;
    static final int MARQUEE_TYPE_TEXT_VIEW     = 1;
    static final int MARQUEE_TYPE_SURFACE_VIEW  = 2;
    int g_marquee_type = MARQUEE_TYPE_SURFACE_VIEW;

    WeakReference<AppCompatActivity> g_weakReference;
    MarqueeSurfaceView  g_marquee_surfaceView;
    TextView            g_marquee_textView;
    ObjectAnimator      g_animator;
    Ticker              g_ticker;
    String              g_marquee_text;

    public MarqueeManager(AppCompatActivity activity) {
        g_weakReference = new WeakReference<>(activity);
        g_animator = ObjectAnimator.ofFloat(g_marquee_textView, "translationX", 0);
        g_marquee_text = "";
    }

    public HomeActivity get() {
        return (HomeActivity) g_weakReference.get();
    }

    private int get_screen_width() {
        return get().getResources().getDisplayMetrics().widthPixels;
    }

    private WindowManager.LayoutParams get_layout_params() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                /*50*/ 100,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 0;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        params.windowAnimations = 0;
        return params;
    }

    public String get_marquee_text() {
        return g_marquee_text;
    }

    private TextView get_text_view() {
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        TextView textView = new TextView(get());
        textView.setText("This is overlay TextView");
        textView.setBackgroundColor(Color.BLACK);
        textView.setTextColor(Color.WHITE);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        textView.setMarqueeRepeatLimit(-1); //
        textView.setSelected(true);
        textView.setGravity(Gravity.START);
        textView.setLayoutParams(layoutParams);
        return textView;
    }

    public boolean marquee_isRunning() {
        return (g_marquee_surfaceView != null) ? g_marquee_surfaceView.isRunning() : false;
    }

    public int marquee_get_ticker_id() {
        return (g_marquee_surfaceView != null) ? g_ticker.getId() : 0;
    }

    public void set_ticker(Ticker ticker, int serviceId) {
        if(g_marquee_surfaceView != null && g_marquee_surfaceView.isRunning())
            return;

        int currentServiceId = get().get_live_tv_manager().get_current_channel().getServiceId();
        //Log.d(TAG, "set_ticker: serviceId = " + serviceId);
        //Log.d(TAG, "set_ticker: curr serviceId = " + currentServiceId);
        if (currentServiceId != serviceId)
            return;

//        Date now = Calendar.getInstance().getTime();
//        Log.d(TAG,"do ticker now = "+now);
        g_ticker = ticker;
        start_marquee(ticker);
    }

    private int get_text_width() {
        g_marquee_textView.measure(0, 0);
        return g_marquee_textView.getMeasuredWidth();
    }

    public void set_marquee_text(String text) {
        if (MARQUEE_TYPE_ANIMATOR == g_marquee_type ||
            MARQUEE_TYPE_TEXT_VIEW == g_marquee_type) {
            if (g_marquee_textView == null) {
                Log.w(TAG, "set_marquee_text: null marquee text view");
                return;
            }
            g_marquee_text = text;
            g_marquee_textView.setText(text);
        }
        else
        if (MARQUEE_TYPE_SURFACE_VIEW == g_marquee_type) {
            if (g_marquee_surfaceView == null) {
                Log.w(TAG, "set_marquee_text: null marquee surface view");
                return;
            }
            g_marquee_text = text;
            g_marquee_surfaceView.set_text(text);
        }
    }

    private void set_permission() {
        Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + get().getPackageName()));

        Log.d(TAG, "set_overlay_permission: " + intent);

        get().startActivity(intent);
    }

    private boolean add_marquee(Ticker ticker) {
        WindowManager windowManager;
        WindowManager.LayoutParams layoutParams;

        windowManager       = (WindowManager) get().getSystemService(Context.WINDOW_SERVICE);
        layoutParams        = get_layout_params();
        g_marquee_textView  = null;
        g_marquee_surfaceView = null;
        g_marquee_text      = ticker.getShowContent();//ticker.getContent();

        // get view
        switch (g_marquee_type) {
            case MARQUEE_TYPE_ANIMATOR:
                g_marquee_textView = get_text_view();
                break;
            case MARQUEE_TYPE_TEXT_VIEW:
                g_marquee_textView = new MarqueeView(get());
                break;
            case MARQUEE_TYPE_SURFACE_VIEW:
                g_marquee_surfaceView = new MarqueeSurfaceView(this,ticker);
                break;
            default:
                Log.e(TAG, "add_marquee: wrong marquee type");
                return false;
        }

        layoutParams.height  = (int)g_marquee_surfaceView.get_text_font_height();

        // add view
        switch (g_marquee_type) {
            case MARQUEE_TYPE_ANIMATOR:
            case MARQUEE_TYPE_TEXT_VIEW:
                windowManager.addView(g_marquee_textView, layoutParams);
                break;
            case MARQUEE_TYPE_SURFACE_VIEW:
                windowManager.addView(g_marquee_surfaceView, layoutParams);
                g_marquee_surfaceView.set_marquee_callback(this);
                break;
        }

        Log.d(TAG, "add_marquee: add to window");
        return true;
    }

    private boolean animator_start() {
        if (MARQUEE_TYPE_ANIMATOR != g_marquee_type)
            return false;

        int screenWidth     = get_screen_width();
        int textWidth       = get_text_width();
        float desiredSpeed  = 0.08f;
        float distance      = screenWidth + textWidth;
        long duration       = (long) (distance / desiredSpeed);

        g_animator = ObjectAnimator.ofFloat(g_marquee_textView, "translationX", screenWidth, -textWidth);
        g_animator.setDuration(duration);
        g_animator.setRepeatCount(ValueAnimator.INFINITE);
        g_animator.start();

        // callback
        animator_set_callback();
        return true;
    }

    private void animator_set_callback() {
        g_animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {
                Log.e(TAG, "onAnimationStart: ");
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                animator_on_end();
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {
                Log.e(TAG, "onAnimationCancel: ");
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {
                Log.e(TAG, "onAnimationRepeat: ");
            }
        });
    }

    private void animator_on_end() {
        Log.d(TAG, "on_animator_end: stop marquee");
        stop_marquee();
    }

    /**
     * // sample code
     * Ticker ticker = new Ticker();
     * ticker.setId(1602161);
     * ticker.setContent("自112年10月13日(五)起，本公司推出全新頻道『CH86鏡電視新聞台』，及調整『樂視台至CH85』、『天天購物台至CH162』供收視；最新頻道表及資費方案請瀏覽本公司官網查詢，敬祝收視愉快。");
     * ticker.setStart_time("2024-10-23 16:55");
     * ticker.setEnd_time("2025-10-23 16:55");
     * ticker.setMail_type("ticker");
     * ticker.setScroll_speed("fast");
     * ticker.setFont_color("#ffc107");
     * ticker.setBg_color("#ffc107");
     * ticker.setFont_size("70");
     * ticker.setRepeat_time(1);
     * ticker.setLeft_time(1);
     * ticker.setPeriod(1);
     * g_marqueeMgr.start_marquee(ticker);
     * @param ticker Refer to the sample code above.
     */
    public void start_marquee(Ticker ticker) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Log.w(TAG, "start_marquee: sdk version " + Build.VERSION.SDK_INT);
            return;
        }
        if (!Settings.canDrawOverlays(get())) {
            set_permission();
            return;
        }
        if (g_animator.isRunning()) {
            Log.w(TAG, "start_marquee: animator is running");
            return;
        }
        if (g_marquee_surfaceView != null) {
            Log.w(TAG, "start_marquee: already show a marquee");
            return;
        }

        // add marquee to window
        if (add_marquee(ticker)) {
            if (animator_start())
                Log.d(TAG, "start_marquee: start animator");
            Log.d(TAG, "start_marquee: success");
        }
    }

    public void stop_marquee() {
        WindowManager windowManager;

        // stop animator
        if (g_animator.isRunning()) {
            Log.d(TAG, "stop_marquee: stop animator");
            g_animator.cancel();
        }

        // remove marquee
        if (remove_marquee_text_view())
            Log.d(TAG, "stop_marquee: successfully remove text view");
        if (remove_marquee_surface_view())
            Log.d(TAG, "stop_marquee: successfully remove surface view");
        g_ticker = null;
    }

    private boolean remove_marquee_text_view() {
        WindowManager windowManager;

        if (MARQUEE_TYPE_ANIMATOR != g_marquee_type &&
            MARQUEE_TYPE_TEXT_VIEW != g_marquee_type)
            return false;

        if (g_marquee_textView == null)
            Log.w(TAG, "stop_marquee: null marquee");
        else {
            windowManager = (WindowManager) get().getSystemService(Context.WINDOW_SERVICE);
            windowManager.removeView(g_marquee_textView);
            g_marquee_textView = null;
            return true;
        }
        return false;
    }

    private boolean remove_marquee_surface_view() {
        WindowManager windowManager;

        if (MARQUEE_TYPE_SURFACE_VIEW != g_marquee_type)
            return false;

        if (g_marquee_surfaceView == null)
            Log.w(TAG, "remove_marquee_surface_view: null marquee");
        else {
            windowManager = (WindowManager) get().getSystemService(Context.WINDOW_SERVICE);
            windowManager.removeView(g_marquee_surfaceView);
            g_marquee_surfaceView = null;
            return true;
        }
        return false;
    }

    @Override
    public void on_marquee_complete() {
        Log.d(TAG, "on_marquee_complete: stop marquee");
        get().g_mailManager.set_ticker_next_show_time(g_ticker.getId());
        stop_marquee();
    }

    public Ticker get_ticker() {
        return g_ticker;
    }
}
