package com.prime.launcher.teletextservice;

import android.graphics.Typeface;

import com.prime.launcher.teletextservice.fullpageteletext.FttxGXCharset;
import com.prime.launcher.teletextservice.fullpageteletext.FttxPage;

/**
 * Holds information required for page rendering
 */
public class PageSettings {
    private int mZoomMode;
    private boolean mBlink;
    private boolean mFlashData;
    // 100% full opaque, 0% full transparent
    private int mTransparencyValue;
    private boolean mIsSubtitle;
    private boolean mNavigationExists;
    private Typeface mTypeface;
    private FttxGXCharset mCharset;
    private int mLineCount;

    public void reset() {
        mZoomMode = 0;
        mBlink = false;
        mFlashData = false;
        mTransparencyValue = 0;
        mIsSubtitle = false;
        mNavigationExists = false;
        mLineCount = FttxPage.MAX_LINES_BY_PAGE - 1;
    }

    public void setZoomMode(int zoomMode) {
        mZoomMode = zoomMode;
    }

    public void nextZoomMode() {
        mZoomMode++;
    }

    public int getZoomMode() {
        return mZoomMode;
    }

    public boolean isBlink() {
        return mBlink;
    }

    public void setBlink(boolean blink) {
        mBlink = blink;
    }

    public boolean isFlashData() {
        return mFlashData;
    }

    public void setFlashData(boolean flashData) {
        mFlashData = flashData;
    }

    public void setTransparencyValue(int transparencyValue) {
        mTransparencyValue = transparencyValue;
    }

    public int getTransparencyValue() {
        return mTransparencyValue;
    }

    public int getAlphaInverted() {
        return 256 - getAlpha();
    }

    public int getAlpha() {
        if (mTransparencyValue <= 100 && mTransparencyValue > 90) {
            return 256;
        } else if (mTransparencyValue <= 90 && mTransparencyValue > 70) {
            return 205;
        } else if (mTransparencyValue <= 70 && mTransparencyValue > 50) {
            return 179;
        } else if (mTransparencyValue <= 50 && mTransparencyValue > 30) {
            return 154;
        } else if (mTransparencyValue <= 30 && mTransparencyValue > 10) {
            return 128;
        }
        return 256;
    }

    public void setSubtitle(boolean isSubtitle) { mIsSubtitle = isSubtitle; }

    public boolean isSubtitle() { return mIsSubtitle; }

    public void setNavigationExists(boolean exists) {
        mNavigationExists = exists;
        mLineCount = exists ? FttxPage.MAX_LINES_BY_PAGE : (FttxPage.MAX_LINES_BY_PAGE - 1);
    }

    public boolean isNavigationExists() { return mNavigationExists; }

    public void setFontFace(Typeface typeface) {
        mTypeface = typeface;
    }

    public Typeface getFontFace() {
        return mTypeface;
    }

    public void setGXCharset(FttxGXCharset charset) {
        mCharset = charset;
    }

    public FttxGXCharset getGXCharset() {
        return mCharset;
    }

    public int getLineCount() { return mLineCount; }
}
