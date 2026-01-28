package com.prime.launcher.teletextservice;

import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;

import com.prime.launcher.teletextservice.fullpageteletext.FttxLine;
import com.prime.launcher.teletextservice.fullpageteletext.FttxPage;
import com.prime.launcher.teletextservice.fullpageteletext.FttxSegment;

import java.util.ArrayList;
import java.util.HashMap;

class TeletextPainter {
    private static final String TAG = "TeletextPainter";
    private static final int ZOOM_NONE = 0;
    private static final int ZOOM_UPPER = 1;
    private static final int ZOOM_LOWER = 2;
    private static final int DEFAULT_BITMAP_WIDTH = 620;
    private static final int DEFAULT_BITMAP_HEIGHT = 466;
    private static final float DEFAULT_SCALE = 1.0F;
    private static final int DEFAULT_HEIGHT_PADDING = 10;
    private RenderMode mRenderMode = RenderMode.SCALED_TO_DISPLAY_DISPLAY_AR;
    private float mScale = DEFAULT_SCALE;
    private int mScaledBitmapHeight = DEFAULT_BITMAP_HEIGHT;
    private int mScaledBitmapWidth = DEFAULT_BITMAP_WIDTH;
    private boolean mInstrumentationTesting;
    private Bitmap mBitmap;
    private DimensionsInfo mDimensionInfo;

    enum RenderMode {
        /**
         * Render lines to original size and aspect ratio destination
         * (BITMAP_WIDTH, BITMAP_HEIGHT, AR 4:3)
         */
        ORIGINAL,
        /**
         * Render to scaled size to display with original aspect ratio
         * (BITMAP_WIDTH * scale, BITMAP_HEIGHT == display height, AR 4:3)
         */
        SCALED_TO_DISPLAY_DEFAULT_AR,
        /**
         * Render to display with display aspect ratio
         */
        SCALED_TO_DISPLAY_DISPLAY_AR
    };

    private class DimensionsInfo {
        int pageHeight;
        int pageWidth;
        int charWidth;
        int lineHeight;
        int lineCount;
        float scale;
        Rect page;
        Rect canvas;

        /**
         * Mosaic characters font map
         * Different fonts have different mosaic character bounds
         * which needs to be adjusted to get proper alignment
         */
        HashMap<Integer, Integer> heightFontMap;
    }

    TeletextPainter(boolean instrumentationTesting, float destinationScale) {
        Log.d(TAG, "TeletextPainter, instrumentation testing " + instrumentationTesting);
        mInstrumentationTesting = instrumentationTesting;
        mScale = (destinationScale <= 1.0F && destinationScale >= 0.0F) ? destinationScale : 1.0F;
    }

    public void setDestinationScale(float scale) {
        mScale = (scale <= 1.0F && scale >= 0.0F) ? scale : 1.0F;
    }

    private DimensionsInfo calculateDestinationSize(Canvas canvas, PageSettings settings) {
        DimensionsInfo dest = new DimensionsInfo();
        Rect pageDimension = new Rect();
        Rect canvasDimension = new Rect();
        int destWidth = canvas.getWidth();
        int destHeight = canvas.getHeight();
        RenderMode renderMode = mRenderMode;

        if (renderMode == RenderMode.ORIGINAL) {
            if (destWidth < DEFAULT_BITMAP_WIDTH && destHeight < DEFAULT_BITMAP_HEIGHT) {
                pageDimension.left = (destWidth - DEFAULT_BITMAP_WIDTH) / 2;
                pageDimension.right = pageDimension.left + DEFAULT_BITMAP_WIDTH;
                pageDimension.top = (destHeight - DEFAULT_BITMAP_HEIGHT) / 2;
                pageDimension.bottom = pageDimension.top + DEFAULT_BITMAP_HEIGHT;
                dest.page = pageDimension;
                return dest;
            } else {
                renderMode = RenderMode.SCALED_TO_DISPLAY_DEFAULT_AR;
            }
        }

        float ar = renderMode == RenderMode.SCALED_TO_DISPLAY_DEFAULT_AR ?
                (float)DEFAULT_BITMAP_WIDTH / DEFAULT_BITMAP_HEIGHT :
                (float)destWidth / destHeight;

        float height = destHeight * mScale;
        float width = height * ar;

        dest.charWidth = Math.round(width / (FttxPage.MAX_CHARS_BY_LINE + 1));
        width = ((FttxPage.MAX_CHARS_BY_LINE + 1) * dest.charWidth);
        int widthPadding = dest.charWidth / 2;

        if (settings.isNavigationExists()) {
            //Calculate paddings and size
            dest.lineHeight = Math.round((height - 2 * DEFAULT_HEIGHT_PADDING) /
                    FttxPage.MAX_LINES_BY_PAGE);
            height = FttxPage.MAX_LINES_BY_PAGE * dest.lineHeight;
            dest.lineCount = FttxPage.MAX_LINES_BY_PAGE;
        } else {
            dest.lineHeight = Math.round((height - 2 * DEFAULT_HEIGHT_PADDING) /
                    (FttxPage.MAX_LINES_BY_PAGE - 1));
            height = (FttxPage.MAX_LINES_BY_PAGE - 1) * dest.lineHeight;
            dest.lineCount = FttxPage.MAX_LINES_BY_PAGE - 1;
        }

        canvasDimension.left = (destWidth - ((int)width + (int)dest.charWidth)) / 2;
        canvasDimension.right = canvasDimension.left + (int)width + (int)dest.charWidth;
        canvasDimension.top = (destHeight - (int)(destHeight * mScale)) / 2;
        canvasDimension.bottom = canvasDimension.top + (int)(destHeight * mScale);

        pageDimension.left = canvasDimension.left + widthPadding;
        pageDimension.right = pageDimension.left +
                (FttxPage.MAX_CHARS_BY_LINE * (int)dest.charWidth);
        pageDimension.top = canvasDimension.top + DEFAULT_HEIGHT_PADDING;
        pageDimension.bottom = pageDimension.top + (int)height;

        dest.canvas = canvasDimension;
        dest.page = pageDimension;
        dest.pageHeight = dest.page.height();
        dest.pageWidth = dest.page.width();

        mapG1Codes(dest, settings);

        return dest;
    }

    private void mapG1Codes(DimensionsInfo dInfo, PageSettings pageSettings) {
        if (dInfo.heightFontMap != null) {
            // Already calculated
            return;
        }
        dInfo.heightFontMap = new HashMap<>();

        Rect bounds = new Rect();
        Paint pageFgPaint = new Paint(Paint.SUBPIXEL_TEXT_FLAG);
        pageFgPaint.setColor(Color.WHITE);
        pageFgPaint.setStyle(Paint.Style.FILL);
        pageFgPaint.setAntiAlias(false);
        pageFgPaint.setTypeface(pageSettings.getFontFace());
        Paint.FontMetrics mm = new Paint.FontMetrics();
        pageFgPaint.setTextSize(dInfo.lineHeight);
        pageFgPaint.setTextScaleX(1.0F);
        pageFgPaint.getFontMetrics(mm);

        for (int code = 0x20; code < 0x3f; code++) {
            char[] chars = { pageSettings.getGXCharset().getG1Char(code) };
            pageFgPaint.getTextBounds(chars, 0, 1, bounds);
            if (bounds.height() > dInfo.lineHeight) {
                dInfo.heightFontMap.put((int)chars[0], bounds.height());
            }
        }

        for (int code = 0x60; code < 0x7f; code++) {
            char[] chars = { pageSettings.getGXCharset().getG1Char(code) };
            pageFgPaint.getTextBounds(chars, 0, 1, bounds);
            if (bounds.height() > dInfo.lineHeight) {
                dInfo.heightFontMap.put((int)chars[0], bounds.height());
            }
        }
    }

    public void clearPage(PageSettings pageSettings, Surface surface) {
        Log.d(TAG, "clearPage ");
        Canvas canvas;
        try {
            canvas = surface.lockCanvas(null);
            canvas.drawColor(Color.TRANSPARENT, BlendMode.CLEAR);
            surface.unlockCanvasAndPost(canvas);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Log.e(TAG, "Canvas lock/unlock failed");
        }
    }

    /**
     * Renders a teletext page to surface canvas
     */
    public void paintPage(FttxPage page, PageSettings pageSettings, Surface surface) {
        Log.d(TAG, "TeletextPainter.paintPage() called");

        SparseArray<FttxLine> lines = page.getLines();

        if (lines.size() == 0) {
            clearPage(pageSettings, surface);
            return;
        }

        int lineNum = 0;
        int startLine = 0;
        int padding = 0;
        boolean DoubleHeight = false;

        Paint pageBgPaint = new Paint();
        Paint pageFgPaint = new Paint(Paint.SUBPIXEL_TEXT_FLAG);
        Paint.FontMetrics metrics = new Paint.FontMetrics();

        // setup foreground paint
        pageFgPaint.setColor(Color.WHITE);
        pageFgPaint.setTypeface(pageSettings.getFontFace());
        pageFgPaint.setStyle(Paint.Style.FILL);
        pageFgPaint.setAntiAlias(false);

        // setup background paint
        pageBgPaint.setStyle(Paint.Style.FILL);
        if (pageSettings.isSubtitle()) {
            pageBgPaint.setColor(Color.TRANSPARENT);
        } else {
            pageBgPaint.setColor(Color.BLACK);
            pageBgPaint.setAlpha(pageSettings.getAlpha());
        }
        pageBgPaint.setBlendMode(BlendMode.SRC);

        Canvas canvas;
        Canvas surfaceCanvas;
        try {
            surfaceCanvas = surface.lockCanvas(null);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Log.e(TAG, "Lock canvas failed");
            return;
        }

        if (mDimensionInfo == null || (mDimensionInfo.lineCount != pageSettings.getLineCount())) {
            mDimensionInfo = calculateDestinationSize(surfaceCanvas, pageSettings);
        }

        int bitmapHeight = mDimensionInfo.pageHeight;
        int bitmapWidth = mDimensionInfo.pageWidth;

        if (mInstrumentationTesting) {
            mBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight,
                    Bitmap.Config.ARGB_8888);
            canvas = new Canvas(mBitmap);
        } else {
            canvas = surfaceCanvas;
        }

        int lineHeight = mDimensionInfo.lineHeight;
        int charWidth = mDimensionInfo.charWidth;
        Bitmap mosaicSegment = Bitmap.createBitmap(charWidth, lineHeight,
                Bitmap.Config.ARGB_8888);

        // handling for zoom control
        switch (pageSettings.getZoomMode()) {
            case ZOOM_UPPER:
                lineHeight *= 2;
                break;
            case ZOOM_LOWER:
                startLine = FttxPage.MAX_LINES_BY_PAGE / 2;
                if (pageSettings.isNavigationExists()) {
                    lineHeight = mDimensionInfo.pageHeight / (FttxPage.MAX_LINES_BY_PAGE - startLine);
                } else {
                    lineHeight *= 2;
                }
                break;
            default:
                startLine = 0;
                pageSettings.setZoomMode(ZOOM_NONE);
                break;
        }

        // set page background
        canvas.drawRect(mDimensionInfo.canvas, pageBgPaint);

        // compute the text size of one line
        float textSizeOneLine = lineHeight;
        pageFgPaint.setTextSize(textSizeOneLine);
        pageFgPaint.getFontMetrics(metrics);
        float currentTextSize = -metrics.top + metrics.bottom;
        textSizeOneLine = textSizeOneLine * lineHeight / currentTextSize;

        // adjust padding for zoom mode
        padding -= lineHeight * startLine;
        for (int i = startLine; i < lines.size(); ++i) {
            FttxLine line = lines.valueAt(i);
            float x;
            float y = mDimensionInfo.page.top + (line.getLineNumber() * lineHeight) + padding;
            // small optimization in case of zooming
            if (y > (mDimensionInfo.page.top + bitmapHeight - lineHeight)) {
                continue;
            }
            int count = 0;
            ArrayList<FttxSegment> segments = line.getSegments();
            for (FttxSegment segment : segments) {
                int size = segments.size();
                // As per spec, this is to ignore lower row after applying the double height feature
                if (lineNum == line.getLineNumber() - 1 && DoubleHeight) {
                    ++count;
                    if (size == count) {
                        DoubleHeight = false;
                    }
                    continue;
                }
                // compute segment height and text height
                float segmentHeight;
                // As per spec we should not apply double height for line 23 and 24
                if (line.checkDoubleHeight() && line.getLineNumber() != 23 &&
                        line.getLineNumber() != 24) {
                    // set double height for whole line
                    segmentHeight = lineHeight * 2;
                    DoubleHeight = true;
                    lineNum = line.getLineNumber();
                } else {
                    segmentHeight = lineHeight;
                    DoubleHeight = false;
                }
                if (segment.isDoubleHeight()) {
                    // set double text height just for segment which is marked as double height
                    pageFgPaint.setTextSize(textSizeOneLine * 2);
                } else {
                    pageFgPaint.setTextSize(textSizeOneLine);
                }
                // compute actual width text
                pageFgPaint.setTextScaleX(1.0f);
                char[] chars = line.getChars();
                float textWidth = pageFgPaint.measureText(chars, segment.getStart(),
                        segment.getLength());
                // compute theoretical width of segment
                float segmentWidth = charWidth * (segment.getLength());
                // apply scale
                if (segment.isDoubleWidth()) {
                    pageFgPaint.setTextScaleX(segmentWidth * 2.0f / textWidth);
                } else {
                    pageFgPaint.setTextScaleX(segmentWidth / textWidth);
                }

                // compute segment position
                x = mDimensionInfo.page.left + segment.getStart() * charWidth;

                // draw segment background
                pageBgPaint.setColor(segment.getBackground());
                if (!pageSettings.isSubtitle() || line.getLineNumber() == startLine) {
                    pageBgPaint.setAlpha(pageSettings.getAlpha());
                }
                pageBgPaint.setBlendMode(BlendMode.SRC);
                // Draw full screen background for packet0 data in case of subtitle page
                float rightPos = x + segmentWidth;
                if(pageSettings.isSubtitle() && line.getLineNumber() == startLine &&
                        segment.getLength() > 0) {
                    rightPos = mDimensionInfo.canvas.right;
                }
                canvas.drawRect(x, y, rightPos, y + segmentHeight, pageBgPaint);

                // draw segment text
                pageFgPaint.getFontMetrics(metrics);
                int startChar = segment.getStart();
                for (int h = 0; h < segment.getLength(); h++) {
                    float textX = x + charWidth * h;
                    float textY = y - metrics.top;
                    pageFgPaint.setColor(segment.getForeground());
                    if (segment.isFlash()) {
                        if (pageSettings.isBlink()) {
                            canvas.drawText(chars, startChar + h, 1, textX, textY,
                                    pageFgPaint);
                        }
                        pageSettings.setFlashData(true);
                    } else {
                        canvas.drawText(chars, startChar + h, 1, textX, textY,
                                pageFgPaint);
                    }
                }
            }
        }

        try {
            if (mInstrumentationTesting) {
                surfaceCanvas.drawBitmap(mBitmap, mDimensionInfo.page.left, mDimensionInfo.page.top,
                        pageFgPaint);
                surface.unlockCanvasAndPost(surfaceCanvas);
            } else {
                surface.unlockCanvasAndPost(canvas);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            Log.e(TAG, "Unlock and post canvas failed");
        }

        mosaicSegment.recycle();
    }

    public Bitmap getLastBitmap() {
        return mBitmap;
    }
}