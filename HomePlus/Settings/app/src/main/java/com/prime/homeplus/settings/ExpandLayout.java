package com.prime.homeplus.settings;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class ExpandLayout extends LinearLayout {

    public ExpandLayout(Context context) {
        super(context);
    }

    public ExpandLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ExpandLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setExpandView(View view) {
        ExpandListener expandListener = this.expandListener;
        if (expandListener != null) {
            expandListener.onExpand(true);
        }
    }

    private ExpandListener expandListener;

    public void setExpandListener(ExpandListener expandListener) {
        this.expandListener = expandListener;
    }

    public void setViewExpand(boolean expand) {
        this.isExpand = expand;
    }

    private boolean isExpand = false;

    public boolean isViewExpand() {
        return isExpand;
    }

    public void show() {
        View view = this;

        if (isExpand) {
            view.setVisibility(VISIBLE);
        } else {
            view.setVisibility(INVISIBLE);
        }

        expandListener.onExpand(isExpand);
    }
}
