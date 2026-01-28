package com.prime.dmg.launcher.EPG;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MyLinearLayoutManager extends LinearLayoutManager {
    public MyLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public View onInterceptFocusSearch(View focused, int direction) {
        int position = getPosition(focused);
        int itemCount = getItemCount();
        if (getOrientation() == RecyclerView.HORIZONTAL) {
            if (position == itemCount - 1 && direction == View.FOCUS_RIGHT) {
                return focused;
            }
        } else if (getOrientation() == RecyclerView.VERTICAL && position == itemCount - 1 && direction == View.FOCUS_DOWN) {
            return focused;
        }
        return super.onInterceptFocusSearch(focused, direction);
    }

    @Override
    public View onFocusSearchFailed(View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return ((getOrientation() == RecyclerView.HORIZONTAL && (focusDirection == View.FOCUS_LEFT || focusDirection == View.FOCUS_RIGHT)) || (getOrientation() == RecyclerView.VERTICAL && (focusDirection == View.FOCUS_UP || focusDirection == View.FOCUS_DOWN))) ? focused : super.onFocusSearchFailed(focused, focusDirection, recycler, state);
    }
}
