package com.prime.dtvplayer.View.guide;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;



public class TimelineRow extends TimelineGridView {
    private final String TAG = getClass().getSimpleName();

    private int mScrollPosition;

    public TimelineRow(Context context) {
        this(context, null);
    }

    public TimelineRow(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimelineRow(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    /**
     * Returns the current scroll position
     */
    public int getScrollOffset() {
        return Math.abs(mScrollPosition);
    }

    /**
     * Scrolls horizontally to the given position.
     */
    public void scrollTo(int scrollOffset, boolean smoothScroll) {
        int dx = (scrollOffset - getScrollOffset())
                * (getLayoutDirection() == LAYOUT_DIRECTION_LTR ? 1 : -1);
        if (smoothScroll) {
            smoothScrollBy(dx, 0);
        }
        else {
            scrollBy(dx, 0);
        }
    }


    @Override
    public void onScrolled(int dx, int dy) {
        if (dx == 0 && dy == 0) {
            mScrollPosition = 0;
        }
        else {
            mScrollPosition += dx;
        }
    }
}
