package com.prime.launcher.teletextservice.fullpageteletext;

import android.graphics.Color;

public class FttxSegment {

    int start;
    int length;

    boolean visible;

    int foreground;
    int background;

    boolean doubleWidth;
    boolean doubleHeight;
    boolean flash;
    boolean mosaic;

    FttxSegment() {
        foreground = Color.WHITE;
        background = Color.BLACK;
    }

    FttxSegment(FttxSegment segment) {
        foreground = segment.getForeground();
        background = segment.getBackground();

        doubleWidth = segment.doubleWidth;
        doubleHeight = segment.doubleHeight;

        flash = segment.isFlash();
        start = segment.getStart() + segment.getLength();
    }

    public boolean isDoubleWidth() {
        return doubleWidth;
    }

    public boolean isDoubleHeight() {
        return doubleHeight;
    }

    public int getStart() {
        return start;
    }

    public int getLength() {
        return length;
    }

    public int getForeground() {
        return foreground;
    }

    public int getBackground() {
        return background;
    }

    public void setBackground(int bg) { background = bg; }

    public boolean isFlash() {
        return flash;
    }

    public boolean isMosaic() {
        return mosaic;
    }
}