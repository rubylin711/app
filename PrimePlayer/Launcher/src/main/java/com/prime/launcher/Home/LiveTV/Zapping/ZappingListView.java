package com.prime.launcher.Home.LiveTV.Zapping;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/** @noinspection CommentedOutCode*/
public class ZappingListView extends RecyclerView {

    private static final String TAG = ZappingListView.class.getSimpleName();

    public ZappingListView(@NonNull Context context) {
        super(context);
    }

    public ZappingListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ZappingListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*@Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        g_dyRuntime += dy;
    }*/

    /*@Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        int position;
        int dyDiff;

        if (SCROLL_STATE_IDLE == state) {
            position    = get_position();
            g_dyActual  = get_item_height() * (position - 3);
            Log.d(TAG, "onScrollStateChanged: [dy] " + g_dyRuntime + " / " + g_dyActual + ", [state] " + state + ", [position] " + position);

            // 滾動 dy 不足，再執行一次 scroll
            // 正常滾動 dy: g_dyActual
            // 實際滾動 dy: g_dyRuntime
            if (position > 2 &&
                position < get_count() - 4) {
                dyDiff = g_dyActual - g_dyRuntime;
                scrollBy(0, dyDiff);
            }
        }
    }*/

    public int get_item_height() {
        if (!hasFocus())
            return 0;
        View focusedView = getFocusedChild();
        if (focusedView == null)
            return 0;
        return focusedView.getHeight();
    }

    public int get_position() {
        if (!hasFocus())
            return 0;
        View focusedView = getFocusedChild();
        return getChildAdapterPosition(focusedView);
    }

    public int get_count() {
        ZappingAdapter adapter = (ZappingAdapter) getAdapter();
        if (!hasFocus())
            return 0;
        if (adapter == null)
            return 0;
        return adapter.getItemCount();
    }

    public void move_to_middle(View itemView, int position, int keyCode) { // special case: scroll to center only for ZappingDialog
        int viewCenterY;
        int screenCenterY = 445;
        int count = get_count();

        if (KeyEvent.KEYCODE_DPAD_DOWN == keyCode) {
            if (position < 5)               viewCenterY = 204 + position * 80;
            else if (position >= count - 3) viewCenterY = 686 - 80 * ((count - 1) - position);
            else                            viewCenterY = 525;
        }
        else if (KeyEvent.KEYCODE_DPAD_UP == keyCode) {
            if (position >= count - 4)      viewCenterY = 686 - 80 * ((count - 1) - position);
            else                            viewCenterY = 365;
        }
        else
            return;

        int offsetY = viewCenterY - screenCenterY;
        scrollBy(0, offsetY);
        //Log.e(TAG, "move_to_middle: [pos] " + position  + ", [view center] " + viewCenterY + ", [offset] " + offsetY + ", [count] " + count);
    }
}
