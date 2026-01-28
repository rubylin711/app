package com.prime.launcher.teletextservice.fullpageteletext;

import android.util.Log;

public class FttxNavigationBase {
    private static final String TAG = "FttxNavigationBase";
    public static final int NAVIGATION_POS_RED = 0;
    public static final int NAVIGATION_POS_GREEN = 1;
    public static final int NAVIGATION_POS_YELLOW = 2;
    public static final int NAVIGATION_POS_CYAN = 3;
    public static final int NAVIGATION_TYPE_TOP = 0;

    public interface PageReader {
        String[] readPage(int page);
    }

    int navigationType;
    PageReader mPageReader;

    private FttxNavigationBase() {}

    FttxNavigationBase(int type, PageReader pageReader) {
        navigationType = type;
        mPageReader = pageReader;
    }

    int navigationType() {
        return navigationType;
    }

    public void build(FttxPage page) {
        Log.e(TAG, "Not implemented!");
    }

    public boolean exists() {
        Log.e(TAG, "Not implemented!");
        return false;
    }

    protected int readLsbHamByte(int value) {
        return ((value & 0x1) << 3) |
                (value & 0x4) |
                ((value & 0x10) >> 3) |
                ((value & 0x40) >> 6);
    }

    protected int readLsbHamInt16(int value) {
        return (readLsbHamByte(value) | (readLsbHamByte(value) << 4));
    }

    protected int readLsbParityByte(int value) {
        return (byteReverse(value) & 0x7f);
    }

    private int byteReverse(int n) {
        n = (((n >> 1) & 0x55) | ((n << 1) & 0xaa));
        n = (((n >> 2) & 0x33) | ((n << 2) & 0xcc));
        n = (((n >> 4) & 0x0f) | ((n << 4) & 0xf0));
        return n;
    }

    protected int reinterpretDecAsHex(int number) {
        return ((number / 100) * 256) + (((number % 100) / 10 ) * 16) + ((number % 100) % 10);
    }
}
