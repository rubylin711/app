package com.prime.dmg.launcher.Home.Marquee;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.prime.dmg.launcher.Ticker.Ticker;

public class MarqueeSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    String TAG = MarqueeSurfaceView.class.getSimpleName();

    static final float Y_INTERVAL = 20;

    private Thread g_marqueeThread;
    private boolean g_isRunning;
    private Paint g_paint;
    private int g_currentX;
    private int g_textWidth;
    private String g_text = "This is a marquee";
    private int g_count;
    private int g_speed;

    public interface MarqueeCallback {
        void on_marquee_complete();
    }
    MarqueeCallback g_marquee_callback;

    public MarqueeSurfaceView(MarqueeManager marqueeManager,Ticker ticker) {
        super(marqueeManager.get());
        init_marquee(marqueeManager,ticker);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        g_textWidth = (int) g_paint.measureText(g_text);
        g_currentX = getWidth();
        g_isRunning = true;
        //adjustHeight();

        g_marqueeThread = new Thread(() -> {
            while (g_isRunning) {
                Canvas canvas = null;
                try {
                    canvas = getHolder().lockCanvas();
                    draw_canvas(canvas);
                }
                finally {
                    if (canvas != null)
                        getHolder().unlockCanvasAndPost(canvas);
                }
                sleep(10/*g_speed*/); // scroll speed
            }
        });
        g_marqueeThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        g_isRunning = false;
        try {
            g_marqueeThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private float spToPixel(Context context, Float sp) {
        return sp.floatValue() * context.getResources().getDisplayMetrics().scaledDensity;
    }

    private void set_scroll_speed(String speed_str) {
        if(speed_str.equals("fast"))
            g_speed = 5;
        else if(speed_str.equals("middle"))
            g_speed = 4;
        else if(speed_str.equals("slow"))
            g_speed = 3;
        else
            g_speed = 5;
    }
    private void init_marquee(MarqueeManager marqueeManager, Ticker ticker) {
        g_text = marqueeManager.get_marquee_text();
        getHolder().addCallback(this);
        g_paint = new Paint();
//        Log.d(TAG,"ticker.getFont_size() = "+ticker.getFont_size());
        float font_size = spToPixel(getContext(), Float.valueOf(ticker.getFont_size()));
        g_paint.setTextSize(font_size);
        int f_color = Color.parseColor(ticker.getFont_color());
        g_paint.setColor(f_color);
        int bg_color = Color.parseColor(ticker.getBg_color());
        g_paint.setShadowLayer(4.0f, 2.0f, 2.0f, bg_color);
//        g_paint.setAntiAlias(true);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        g_count = ticker.getLeft_time();
        set_scroll_speed(ticker.getScroll_speed());
//        adjustHeight();
    }

    private void draw_canvas(Canvas canvas) {
        if (canvas == null)
            return;

        // set background color
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); // canvas.drawColor(Color.BLACK);
        float y = -g_paint.getFontMetrics().top+Y_INTERVAL;//-g_paint.getFontMetrics().ascent+10;//(canvas.getHeight() / 2) - ((g_paint.descent() + g_paint.ascent()) / 2.0f)+10; //getHeight()/*getHeight() / 2*/
//        Log.d(TAG,"y = "+y);
        canvas.drawText(g_text, g_currentX, y, g_paint);
//        Log.d(TAG,"canvas.getHeight() = "+canvas.getHeight());
//        Log.d(TAG,"g_paint.descent() + g_paint.ascent() = "+g_paint.descent() + g_paint.ascent());
        g_currentX -= g_speed;
        if (g_currentX < -g_textWidth) {
            g_currentX = getWidth();
            g_count--;
            if(g_count <= 0)
                g_marquee_callback.on_marquee_complete();
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms); // scroll speed
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void adjustHeight() {
        Paint.FontMetrics fontMetrics = g_paint.getFontMetrics();
        float textHeight = Math.abs(fontMetrics.top) + Math.abs(fontMetrics.bottom);

        ViewGroup.LayoutParams params = getLayoutParams();
        if (params != null) {
            params.height = (int) textHeight; // 將高度設置為文字的高度
            setLayoutParams(params);
        }
    }

    public void set_text(String newText) {
        g_text = newText;
        g_textWidth = (int) g_paint.measureText(g_text);
        g_currentX = getWidth(); // Reset the position to start from the right edge
    }

    public void set_marquee_callback(MarqueeCallback marqueeCallback) {
        g_marquee_callback = marqueeCallback;
    }

    public boolean isRunning() {
        return g_isRunning;
    }

    public float get_text_font_height() {
        float font_height = g_paint.getFontMetrics().bottom-g_paint.getFontMetrics().top+Y_INTERVAL;
        return font_height;
    }
}
