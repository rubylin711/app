package com.prime.launcher.Home.Marquee;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class MarqueeView extends androidx.appcompat.widget.AppCompatTextView {

    String TAG = MarqueeView.class.getSimpleName();

    static final int SPEED = 1; // scroll speed

    private int g_screenWidth;
    private int g_textWidth;
    private int g_currentX;

    private Runnable        g_marqueeRunnable;
    private HandlerThread   g_handlerThread;
    private Handler         g_backgroundHandler;

    public MarqueeView(Context context) {
        super(context);
        init_marquee(context);
    }

    public MarqueeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init_marquee(context);
    }

    public MarqueeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init_marquee(context);
    }

    private void init_marquee(Context context) {
        init_marquee_handler();

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        g_screenWidth = windowManager.getDefaultDisplay().getWidth();

        setSingleLine(true);
        setEllipsize(null);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setMarqueeRepeatLimit(-1); // infinity
        setBackgroundColor(Color.TRANSPARENT);
        setText("This is overlay TextView This is overlay TextView This is overlay TextView This is overlay TextView This is overlay TextView");
        Log.d(TAG, "init_marquee_view: success");
    }

    private void init_marquee_handler() {
        g_handlerThread = new HandlerThread("MarqueeThread");
        g_handlerThread.start();
        g_backgroundHandler = new Handler(g_handlerThread.getLooper());
        Log.d(TAG, "init_background_handler: success");
    }

    private void start_marquee() {
        Log.d(TAG, "start_marquee: ");
        g_textWidth = g_screenWidth;//getPaint().measureText(getText().toString());
        g_currentX = -g_screenWidth;

        g_marqueeRunnable = new Runnable() {
            @Override
            public void run() {
                g_currentX += SPEED;
                scrollTo(g_currentX, 0);

                if (g_currentX > g_textWidth)
                    g_currentX = -g_screenWidth;
                g_backgroundHandler.postDelayed(this, 60); // update marquee after delay ms
            }
        };
        g_backgroundHandler.post(g_marqueeRunnable);
    }

    private void stop_marquee() {
        Log.d(TAG, "stop_marquee: ");
        if (g_marqueeRunnable != null) {
            g_backgroundHandler.removeCallbacks(g_marqueeRunnable);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        start_marquee();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop_marquee();
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            start_marquee();
        } else {
            stop_marquee();
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == View.VISIBLE) {
            start_marquee();
        } else {
            stop_marquee();
        }
    }
}
