package com.prime.tvinputframework.subtitle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 簡單的 DVB Subtitle View：
 * - 持有一張 bitmap，畫在底部，依寬度等比縮放。
 * - 給 TvInputService.Session 的 overlay view 使用。
 */
public class DvbSubtitleView extends View {

    private static final String TAG = "DvbSubtitleView";

    private Bitmap mBitmap;
    private final Rect mSrcRect = new Rect();
    private final Rect mDstRect = new Rect();

    // 底部對齊的位置（0.0 ~ 1.0），1.0 = 紧貼畫面底部
    private float mBottomAlign = 1.0f;

    // 是否依寬度等比縮放
    private boolean mKeepAspectRatio = true;

    public DvbSubtitleView(Context context) {
        super(context);
        init();
    }

    public DvbSubtitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DvbSubtitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 告訴系統這個 View 會自己畫東西
        setWillNotDraw(false);
    }
    private void dumpSubtitleBitmap(Bitmap bmp) {
        if (bmp == null) return;

        FileOutputStream out = null;
        try {
            // 存到 /data/vendor/dtvdata
            File dir = new File("/data/vendor/dtvdata");
            if (!dir.exists() && !dir.mkdirs()) {
                Log.w(TAG, "dumpSubtitleBitmap: mkdirs failed: " + dir.getAbsolutePath());
                return;
            }

            String name = "sub_" + System.currentTimeMillis() + ".png";
            File f = new File(dir, name);

            out = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();

            Log.d(TAG, "dumpSubtitleBitmap: " + f.getAbsolutePath());
        } catch (Throwable t) {
            Log.w(TAG, "dumpSubtitleBitmap error", t);
        } finally {
            if (out != null) {
                try { out.close(); } catch (IOException ignore) {}
            }
        }
    }
    /**
     * 設定要顯示的字幕 bitmap。
     * null 表示清除字幕。
     */
    public synchronized void setSubtitleBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        if (bitmap != null) {
            //dumpSubtitleBitmap(bitmap);
            Log.d(TAG, "setSubtitleBitmap: bmp=" + bitmap +
                " size=" + bitmap.getWidth() + "x" + bitmap.getHeight());
        } else {
            Log.d(TAG, "setSubtitleBitmap: bmp=null");
        }
        postInvalidateOnAnimation();
    }

    /**
     * 清除字幕。
     */
    public synchronized void clearSubtitle() {
        Log.d(TAG, "clearSubtitle");
        mBitmap = null;
        postInvalidateOnAnimation();
    }

    /**
     * 調整字幕貼在底部的位置比例（0.0 ~ 1.0）。
     * 比如 0.8 代表距離底部留一點空間。
     */
    public synchronized void setBottomAlign(float bottomAlign) {
        if (bottomAlign < 0f) bottomAlign = 0f;
        if (bottomAlign > 1f) bottomAlign = 1f;
        mBottomAlign = bottomAlign;
        postInvalidateOnAnimation();
    }

    /**
     * 是否保持等比縮放（預設 true）。
     */
    public synchronized void setKeepAspectRatio(boolean keep) {
        mKeepAspectRatio = keep;
        postInvalidateOnAnimation();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        // ★ Debug：先畫一層半透明背景，確認 overlay 有沒有出現在畫面上
        //canvas.drawARGB(80, 0, 0, 255);  // 半透明藍色
        //super.onDraw(canvas);

        int viewWidth = getWidth();
        int viewHeight = getHeight();
        // ★ 在畫面最底部畫一條紅色粗帶
        //Paint p = new Paint();
        //p.setARGB(200, 255, 0, 0);
        //int barHeight = 120;
        //canvas.drawRect(0, viewHeight - barHeight, viewWidth, viewHeight, p);

   
        Log.d(TAG, "onDraw: view=" + viewWidth + "x" + viewHeight +
                " bmp=" + (mBitmap == null ? "null"
                        : (mBitmap.getWidth() + "x" + mBitmap.getHeight())));

        if (mBitmap == null || mBitmap.isRecycled()) {
            return;
        }

        int bmpWidth = mBitmap.getWidth();
        int bmpHeight = mBitmap.getHeight();

        if (viewWidth <= 0 || viewHeight <= 0 || bmpWidth <= 0 || bmpHeight <= 0) {
            return;
        }

        // 原圖範圍
        mSrcRect.set(0, 0, bmpWidth, bmpHeight);

        if (mKeepAspectRatio) {
            // 用 min(scaleX, scaleY) 確保整張圖都畫得下（不裁切）
            float scaleX = (float) viewWidth / (float) bmpWidth;
            float scaleY = (float) viewHeight / (float) bmpHeight;
            float scale  = Math.min(scaleX, scaleY);
        
            int dstWidth  = (int) (bmpWidth * scale);
            int dstHeight = (int) (bmpHeight * scale);
        
            // 水平置中
            int left  = (viewWidth - dstWidth) / 2;
            int right = left + dstWidth;
        
            // 貼在底部（可用 mBottomAlign 微調）
            int bottom = (int) (viewHeight * mBottomAlign);
            int top    = bottom - dstHeight;
            if (top < 0) {
                top = 0;
                bottom = dstHeight;
            }
        
            mDstRect.set(left, top, right, bottom);
        } else {
            mDstRect.set(0, 0, viewWidth, viewHeight);
        }

        canvas.drawBitmap(mBitmap, mSrcRect, mDstRect, null);
    }
}
