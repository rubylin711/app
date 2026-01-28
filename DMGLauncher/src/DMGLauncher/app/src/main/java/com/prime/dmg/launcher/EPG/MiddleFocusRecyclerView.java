package com.prime.dmg.launcher.EPG;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.dmg.launcher.R;

/** @noinspection FieldCanBeLocal, CommentedOutCode */
public class MiddleFocusRecyclerView extends RecyclerView {
    private final String TAG = MiddleFocusRecyclerView.class.getSimpleName();
    private static final int QUICK_SCROLL_DURATION = 50;
    private int g_next_item_size = -1;
    private long g_previousMs = 0;
    private int gListCenterY = 0;
    private View gItemView;

    public MiddleFocusRecyclerView(Context context) {
        super(context);
    }

    public MiddleFocusRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MiddleFocusRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void set_next_item_size(int size) {
        g_next_item_size = size;
    }

    public void focus_middle_vertical(View nextItem) {
        int height = getHeight();
        int height2 = (height - nextItem.getHeight()) / 2;
        int i = g_next_item_size;
        if (i > 0) {
            height2 = (height - i) / 2;
        }
        super.smoothScrollBy(0, nextItem.getTop() - height2);
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        /*if (getId() == R.id.lo_epg_sub_list_rcv_channel) {
            scroll_to_center(gItemView);
        }*/
    }

    public void scroll_to_center(View itemView) {
        if (null == itemView)
            return;
        gItemView = itemView;
        int[] location = new int[2];
        itemView.getLocationOnScreen(location);
        int viewCenterY = location[1] + itemView.getHeight() / 2;
        int listCenterY = get_list_center();
        int offsetY = viewCenterY - listCenterY;
        if (offsetY != 0) {
            if (offsetY > 0 && offsetY < 10)
                scrollBy(0, offsetY);
            else if (offsetY < 0 && offsetY > -10)
                scrollBy(0, offsetY);
            else
                smoothScrollBy(0, offsetY, null, 300);
            //Log.e(TAG, "scroll_to_center: [center] " + viewCenterY + " / " + listCenterY + ", [time diff] " + get_time_diff() + ", [offset] " + offsetY);
        }
        //else Log.e(TAG, "scroll_to_center: do nothing");
    }

    private int get_list_center() {
        if (gListCenterY == 0) {
            int[] location = new int[2];
            getLocationOnScreen(location);
            gListCenterY = location[1] + getHeight() / 2;
        }
        return gListCenterY;
    }

    public long get_time_diff() {
        long timeDiff = System.currentTimeMillis() - g_previousMs;
        g_previousMs = System.currentTimeMillis();
        return timeDiff;
    }

    public LinearLayoutManager get_layout_manager() {
        return (LinearLayoutManager) getLayoutManager();
    }

    public void focus_middle_horizontal(View nextItem) {
        int width = getWidth();
        int width2 = (width - nextItem.getWidth()) / 2;
        int i = g_next_item_size;
        if (i > 0) {
            width2 = (width - i) / 2;
        }
        super.smoothScrollBy(nextItem.getLeft() - width2, 0);
    }

    public void move_to_position(int position) {
        LinearLayoutManager layoutManager = get_layout_manager();
        int offset = layoutManager != null ? layoutManager.getHeight() / 2 : 0;
        if (layoutManager != null) {
            layoutManager.scrollToPositionWithOffset(position, offset);
            focus_first_child(position);
        }
    }

    public void move_to_position(int position, int offset) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
        if (layoutManager != null) {
            layoutManager.scrollToPositionWithOffset(position, offset);
            focus_first_child(position);
        }
    }

    private void focus_first_child(final int position) {
        post(new Runnable() {
            @Override
            public void run() {
                LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
                if (layoutManager == null) {
                    Log.e(TAG, "focus_first_child: layout manager is null");
                    return;
                }
                int findFirstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                if (isComputingLayout() || getChildAt(position - findFirstVisibleItemPosition) == null) {
                    postDelayed(this, 10L);
                    return;
                }
                ViewHolder holder = findViewHolderForAdapterPosition(position);
                if (holder != null)
                    holder.itemView.requestFocus();
                else
                    Log.e(TAG, "focus_first_child: holder is null");
            }
        });
    }
}
