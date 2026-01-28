package com.prime.dtvplayer.View.guide;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class TimelineGridView extends RecyclerView {
    public TimelineGridView(Context context) {
        this(context, null);
    }

    public TimelineGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimelineGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) {
            @Override
            public boolean onRequestChildFocus(RecyclerView parent, State state, View child,View focused) {
                return true;
            }
        });
        setFocusable(false);
        setItemViewCacheSize(0);
    }
}
