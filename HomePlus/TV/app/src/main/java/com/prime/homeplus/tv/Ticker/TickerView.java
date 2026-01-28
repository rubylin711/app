package com.prime.homeplus.tv.Ticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.prime.datastructure.ticker.CNS.CNSTickerData;

import java.util.ArrayList;
import java.util.List;

public class TickerView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    public enum Direction {
        RIGHT_TO_LEFT,
        LEFT_TO_RIGHT,
        BOTTOM_TO_TOP_LEFT,
        BOTTOM_TO_TOP_RIGHT;

        public static Direction fromInt(int value) {
            switch (value) {
                case 2:
                    return LEFT_TO_RIGHT;
                case 3:
                    return BOTTOM_TO_TOP_LEFT;
                case 4:
                    return BOTTOM_TO_TOP_RIGHT;
                case 1:
                default:
                    return RIGHT_TO_LEFT;
            }
        }
    }

    public enum TickerPosition {
        TOP,
        LEFT,
        RIGHT
    }

    private Paint mPaint;
    private List<CNSTickerData.TextData> mOriginalTexts = new ArrayList<>();
    private List<CNSTickerData.TextData> mTexts = new ArrayList<>();
    private float mSpeed = 100f; // pixels per second
    private Direction mDirection = Direction.RIGHT_TO_LEFT;
    private int mRepeatCount = 0;
    private int mCurrentRepeatCount = 0;
    private float mCurrentScrollValue = 0f;
    private float mEndValue = 0f;
    private float mTextTotalWidth = 0f;
    private float mMaxCharWidth = 0f;
    private int mTextBackgroundColor = Color.TRANSPARENT;

    private SurfaceHolder mHolder;
    private Thread mRenderThread;
    private volatile boolean mIsScrolling = false;
    private boolean mNeedsResetPosition = true;

    public TickerView(Context context) {
        super(context);
        init();
    }

    public TickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setSubpixelText(true);
        mPaint.setTextSize(30f); // Default size
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        final boolean isHorizontal = mDirection == Direction.RIGHT_TO_LEFT || mDirection == Direction.LEFT_TO_RIGHT;
        int finalWidth = width;
        int finalHeight = height;

        if (isHorizontal) {
            final float textHeight = mPaint.descent() - mPaint.ascent();
            int desiredHeight = (int) (textHeight + getPaddingTop() + getPaddingBottom());
            if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
                finalHeight = desiredHeight;
            }
        } else {
            int desiredWidth = (int) (mMaxCharWidth + getPaddingLeft() + getPaddingRight());
            if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
                finalWidth = desiredWidth;
            }
        }

        setMeasuredDimension(finalWidth, finalHeight);
    }

    public void setPaint(Paint paint) {
        if (paint != null) {
            mPaint = new Paint(paint);
            mPaint.setAntiAlias(true);
            mPaint.setSubpixelText(true);
            measureTextWidth();
        }
    }

    public void setTextSize(int unit, float size) {
        Context c = getContext();
        float textSize = TypedValue.applyDimension(unit, size, c.getResources().getDisplayMetrics());
        if (mPaint.getTextSize() != textSize) {
            mPaint.setTextSize(textSize);
            measureTextWidth();
        }
    }

    public void setTextColor(int color) {
        if (mPaint.getColor() != color) {
            mPaint.setColor(color);
            measureTextWidth();
        }
    }

    public void setTypeface(Typeface tf) {
        if (mPaint.getTypeface() != tf) {
            mPaint.setTypeface(tf);
            measureTextWidth();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mIsScrolling) {
            startRenderThread();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        measureTextWidth();
        if (mIsScrolling && mNeedsResetPosition && width > 0) {
            resetScrollPosition();
            mNeedsResetPosition = false;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopRenderThread();
    }

    private void startRenderThread() {
        stopRenderThread();
        mIsScrolling = true;
        mRenderThread = new Thread(this, "TickerRenderThread");
        mRenderThread.start();
    }

    private void stopRenderThread() {
        mIsScrolling = false;
        if (mRenderThread != null) {
            try {
                mRenderThread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            mRenderThread = null;
        }
    }

    @Override
    public void run() {
        long lastFrameTime = System.nanoTime();

        while (mIsScrolling) {
            Canvas canvas = null;
            try {
                canvas = mHolder.lockCanvas();
                if (canvas == null)
                    continue;

                if (mNeedsResetPosition && getWidth() > 0) {
                    resetScrollPosition();
                    mNeedsResetPosition = false;
                }

                long currentTime = System.nanoTime();
                float deltaTime = (currentTime - lastFrameTime) / 1_000_000_000f;
                lastFrameTime = currentTime;

                update(deltaTime);
                drawTicker(canvas);

            } finally {
                if (canvas != null) {
                    mHolder.unlockCanvasAndPost(canvas);
                }
            }

            long sleepTime = 16 - (System.nanoTime() - lastFrameTime) / 1_000_000;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored) {
                }
            }
        }

        // Animation finished, hide the view on UI thread to allow next ticker to play
        post(new Runnable() {
            @Override
            public void run() {
                stopScrolling();
            }
        });
    }

    private void update(float deltaTime) {
        float distance = mSpeed * deltaTime;

        if (mDirection == Direction.LEFT_TO_RIGHT) {
            mCurrentScrollValue += distance;
            if (mCurrentScrollValue >= mEndValue) {
                handleRepeat();
            }
        } else {
            // Default: Right to Left or Bottom to Top
            mCurrentScrollValue -= distance;
            if (mCurrentScrollValue <= mEndValue) {
                handleRepeat();
            }
        }
    }

    private void handleRepeat() {
        if (mCurrentRepeatCount < mRepeatCount) {
            mCurrentRepeatCount++;
            resetScrollPosition();
        } else {
            mIsScrolling = false;
        }
    }

    private void drawTicker(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        if (mTexts == null || mTexts.isEmpty())
            return;

        if (mTextBackgroundColor != Color.TRANSPARENT) {
            canvas.drawColor(mTextBackgroundColor);
        }

        final int width = getWidth();
        final int height = getHeight();
        final float textHeight = mPaint.descent() - mPaint.ascent();
        final boolean isHorizontal = mDirection == Direction.RIGHT_TO_LEFT || mDirection == Direction.LEFT_TO_RIGHT;

        if (isHorizontal) {
            float baseline = getPaddingTop() - mPaint.ascent();
            float top = getPaddingTop();
            float currentDrawX = mCurrentScrollValue;

            for (CNSTickerData.TextData data : mTexts) {
                if (data == null || data.text == null)
                    continue;

                float segmentWidth = data.width;
                if (currentDrawX + segmentWidth >= 0 && currentDrawX <= width) {
                    if (data.bitmap != null && !data.bitmap.isRecycled()) {
                        canvas.drawBitmap(data.bitmap, currentDrawX, top, mPaint);
                    } else {
                        mPaint.setColor(data.fontRealColor != 0 ? data.fontRealColor : mPaint.getColor());
                        canvas.drawText(data.text, currentDrawX, baseline, mPaint);
                    }
                }
                currentDrawX += segmentWidth;
                if (mDirection == Direction.RIGHT_TO_LEFT && currentDrawX > width)
                    break;
                if (mDirection == Direction.LEFT_TO_RIGHT && currentDrawX - segmentWidth > width)
                    break;
            }
        } else {
            float currentDrawY = mCurrentScrollValue;
            float fixedDrawX;
            if (mDirection == Direction.BOTTOM_TO_TOP_LEFT) {
                fixedDrawX = getPaddingLeft() - 2; // 補償渲染時的 2 像素偏移
            } else {
                fixedDrawX = width - mMaxCharWidth - getPaddingRight() - 2; // 補償渲染時的 2 像素偏移
            }

            for (CNSTickerData.TextData data : mTexts) {
                if (data == null || data.text == null)
                    continue;

                float segmentHeight = data.height;
                if (currentDrawY + segmentHeight >= 0 && currentDrawY <= height) {
                    if (data.bitmap != null && !data.bitmap.isRecycled()) {
                        canvas.drawBitmap(data.bitmap, fixedDrawX, currentDrawY, mPaint);
                    } else {
                        mPaint.setColor(data.fontRealColor != 0 ? data.fontRealColor : mPaint.getColor());
                        float charY = currentDrawY - mPaint.ascent();
                        for (char c : data.charArray) {
                            if (charY + mPaint.descent() >= 0 && charY + mPaint.ascent() <= height) {
                                canvas.drawText(String.valueOf(c), fixedDrawX + 2, charY, mPaint);
                            }
                            charY += textHeight;
                        }
                    }
                }
                currentDrawY += segmentHeight;
                if (currentDrawY > height)
                    break;
            }
        }
    }

    private void measureTextWidth() {
        mTextTotalWidth = 0;
        if (mTexts != null) {
            final float textHeight = mPaint.descent() - mPaint.ascent();
            mMaxCharWidth = 0;
            for (CNSTickerData.TextData data : mTexts) {
                if (data == null || data.text == null)
                    continue;
                data.width = mPaint.measureText(data.text);
                data.height = data.text.length() * textHeight;
                data.charArray = data.text.toCharArray();

                // 精確計算垂直模式下的最大字元寬度
                for (char c : data.charArray) {
                    float charWidth = mPaint.measureText(String.valueOf(c));
                    if (charWidth > mMaxCharWidth) {
                        mMaxCharWidth = charWidth;
                    }
                }

                mTextTotalWidth += data.width;
            }
            // 加入更大的安全邊距 (Safety Margin)，防止字元邊緣切除
            if (mMaxCharWidth > 0) {
                mMaxCharWidth += 10;
            }

            // 重新渲染 Bitmap 以反映新的寬度
            for (CNSTickerData.TextData data : mTexts) {
                if (data != null && data.text != null) {
                    renderToBitmap(data);
                }
            }
        }
        updateEndValue();
    }

    private void renderToBitmap(CNSTickerData.TextData data) {
        if (data.bitmap != null) {
            data.bitmap.recycle();
            data.bitmap = null;
        }

        boolean isHorizontal = mDirection == Direction.RIGHT_TO_LEFT || mDirection == Direction.LEFT_TO_RIGHT;
        int w = (int) Math.ceil(isHorizontal ? data.width : mMaxCharWidth);
        int h = (int) Math.ceil(isHorizontal ? (mPaint.descent() - mPaint.ascent()) : data.height);

        if (w <= 0 || h <= 0 || w > 4096 || h > 4096)
            return;

        try {
            data.bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(data.bitmap);
            int originalColor = mPaint.getColor();
            mPaint.setColor(data.fontRealColor != 0 ? data.fontRealColor : originalColor);

            if (isHorizontal) {
                canvas.drawText(data.text, 0, -mPaint.ascent(), mPaint);
            } else {
                float charHeight = mPaint.descent() - mPaint.ascent();
                float y = -mPaint.ascent();
                for (int i = 0; i < data.charArray.length; i++) {
                    // 向右偏移 2 像素繪製，防止左側切除，並為右側預留更多空間
                    canvas.drawText(data.charArray, i, 1, 2, y, mPaint);
                    y += charHeight;
                }
            }
            mPaint.setColor(originalColor);
        } catch (OutOfMemoryError e) {
            data.bitmap = null;
        }
    }

    private void updateEndValue() {
        if (mDirection == Direction.LEFT_TO_RIGHT) {
            mEndValue = getWidth() + mTextTotalWidth;
        } else if (mDirection == Direction.RIGHT_TO_LEFT) {
            mEndValue = -mTextTotalWidth;
        } else {
            // Vertical
            mEndValue = -calculateTotalVerticalHeight();
        }
    }

    private float calculateTotalVerticalHeight() {
        if (mTexts == null || mTexts.isEmpty())
            return 0f;
        final float textHeight = mPaint.descent() - mPaint.ascent();
        float totalHeight = 0f;
        for (CNSTickerData.TextData data : mTexts) {
            if (data == null || data.text == null)
                continue;
            totalHeight += data.text.length() * textHeight;
        }
        return totalHeight;
    }

    private void resetScrollPosition() {
        int width = getWidth();
        int height = getHeight();

        if (mDirection == Direction.RIGHT_TO_LEFT) {
            mCurrentScrollValue = width;
        } else if (mDirection == Direction.LEFT_TO_RIGHT) {
            mCurrentScrollValue = -mTextTotalWidth;
        } else {
            // Vertical
            mCurrentScrollValue = height;
        }
    }

    public void setSpeed(float speed) {
        this.mSpeed = speed * 1.45f;
    }

    public void startScrolling() {
        mCurrentRepeatCount = 0;
        mNeedsResetPosition = true;
        if (getWidth() > 0) {
            resetScrollPosition();
            mNeedsResetPosition = false;
        }

        if (mHolder.getSurface().isValid()) {
            startRenderThread();
        } else {
            mIsScrolling = true;
        }
        setVisibility(View.VISIBLE);
    }

    public void stopScrolling() {
        stopRenderThread();
        setVisibility(View.GONE);
    }

    private void recycleBitmaps() {
        if (mTexts != null) {
            for (CNSTickerData.TextData data : mTexts) {
                if (data != null && data.bitmap != null) {
                    data.bitmap.recycle();
                    data.bitmap = null;
                }
            }
        }
    }

    public void setRepeatCount(int repeatCount) {
        this.mRepeatCount = repeatCount;
    }

    public void setDirection(Direction direction) {
        if (direction == null)
            return;
        boolean changed = this.mDirection != direction;
        this.mDirection = direction;
        if (changed) {
            applyTexts();
            if (mIsScrolling) {
                startScrolling();
            }
        }
    }

    public void setTexts(List<CNSTickerData.TextData> texts) {
        this.mOriginalTexts = texts != null ? texts : new ArrayList<>();
        applyTexts();
    }

    private void applyTexts() {
        recycleBitmaps();
        this.mTexts = new ArrayList<>();
        if (mDirection == Direction.LEFT_TO_RIGHT) {
            // 反轉段落順序並反轉每個段落內的文字，以實現 L2R 模式下頭部字元先進入
            for (int i = mOriginalTexts.size() - 1; i >= 0; i--) {
                CNSTickerData.TextData original = mOriginalTexts.get(i);
                CNSTickerData.TextData reversed = new CNSTickerData.TextData();
                reversed.text = original.text != null ? new StringBuilder(original.text).reverse().toString() : "";
                reversed.fontColor = original.fontColor;
                reversed.fontRealColor = original.fontRealColor;
                this.mTexts.add(reversed);
            }
        } else {
            // 其他模式直接複製
            for (CNSTickerData.TextData original : mOriginalTexts) {
                CNSTickerData.TextData copy = new CNSTickerData.TextData();
                copy.text = original.text;
                copy.fontColor = original.fontColor;
                copy.fontRealColor = original.fontRealColor;
                this.mTexts.add(copy);
            }
        }

        measureTextWidth();
        if (mTexts.isEmpty()) {
            stopScrolling();
            return;
        }
        startScrolling();
    }

    public void setTextBackgroundColor(int color) {
        // mTextBackgroundColor = color;
    }
}