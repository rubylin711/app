package com.prime.launcher.Home.Recommend.List;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.launcher.R;

import java.lang.ref.WeakReference;
import java.util.List;

/** @noinspection CommentedOutCode, ConstantValue */
public class RecommendListView extends RecyclerView {
    private static final String TAG = RecommendListView.class.getSimpleName();

    public final static int GRID_COLUMN_NUM = 5;
    public final static int SCROLL_DURATION = 150;
    public final static int QUICK_SCROLL_DURATION = 50;

    WeakReference<AppCompatActivity> g_ref;
    int g_focused_pos;
    int g_middle_pos;
    int g_prev_pos;
    int g_prev_row;
    int g_scroll_state;
    int g_dxRuntime;
    int g_dxShouldBe;

    public RecommendListView(@NonNull Context context) {
        super(context);
    }

    public RecommendListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecommendListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (!gainFocus)
            return;
        focus_child();
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        // disable quick scroll
        if (true)
            return;
        int position = get_position();
        int dxStart = get().getResources().getDimensionPixelSize(R.dimen.home_list_rcv_scroll_size_first);
        int dxNormal = get().getResources().getDimensionPixelSize(R.dimen.home_list_rcv_scroll_size_normal);

        // dx runtime
        g_dxRuntime += dx;
        // dx should scroll
        g_dxShouldBe = 0;
        if (position >= 3)
            g_dxShouldBe = dxStart + (dxNormal * (position - 3));
        // re-scroll
        smoothScrollBy(g_dxShouldBe - g_dxRuntime, 0, null, QUICK_SCROLL_DURATION);
        //scrollBy(g_dxShouldBe - g_dxRuntime, 0);
        //Log.e(TAG, "onScrolled: [dx] " + g_dxRuntime + " / " + g_dxShouldBe + ", [position] " + position + ", [previous] " + get_previous() + ", [count] " + get_count());
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        g_scroll_state = state;
    }

    public void init_list_view(AppCompatActivity activity, List<RecommendItem> itemList) {
        activity.runOnUiThread(() -> {
            Log.d(TAG, "init_list_view: " + this);
            g_ref = new WeakReference<>(activity);
            g_scroll_state = SCROLL_STATE_IDLE;
            // layout manager
            if (is_grid())
                setLayoutManager(new GridLayoutManager(get(), GRID_COLUMN_NUM, GridLayoutManager.VERTICAL, false));
            else
                setLayoutManager(new LinearLayoutManager(get(), LinearLayoutManager.HORIZONTAL, false));
            // data
            setAdapter(new RecommendAdapter(get(), getId(), itemList));
        });
    }

    public boolean is_scroll_idle() {
        return g_scroll_state == SCROLL_STATE_IDLE;
    }

    public boolean is_in_home() {
        return getId() == R.id.lo_home_rcv_popular ||
               getId() == R.id.lo_home_rcv_apps    ||
               getId() == R.id.lo_home_rcv_apps_games;
    }

    public boolean is_in_side_menu() {
        return getId() == R.id.lo_side_menu_app_game_grid ||
               getId() == R.id.lo_side_menu_app_list;
    }

    public boolean is_grid() {
        return getId() == R.id.lo_side_menu_app_game_grid;
    }

    public AppCompatActivity get() {
        return g_ref.get();
    }

    /*public int get_dx_right(int pos) {
        int itemWidth, itemMargin, delta;

        if (is_in_home()) {
            itemMargin = getResources().getDimensionPixelSize(R.dimen.home_list_title_margin_start);
            itemWidth  = getResources().getDimensionPixelSize(R.dimen.home_list_rcv_block_width);
            delta      = getResources().getDimensionPixelSize(R.dimen.home_list_rcv_block_middle_ref);
            return pos == 2 ? 4 * itemWidth + itemMargin - delta
                            : itemWidth;
        }
        if (is_in_side_menu()) {
            itemMargin = get().getResources().getDimensionPixelSize(R.dimen.side_menu_app_list_item_margin_x);
            itemWidth  = get().getResources().getDimensionPixelSize(R.dimen.side_menu_app_list_item_width) + itemMargin * 2;
            delta      = get().getResources().getDimensionPixelSize(R.dimen.side_menu_app_list_item_middle_delta);
            return pos == 2 ? 4 * itemWidth - delta - itemMargin
                            : itemWidth;
        }
        Log.e(TAG, "get_dx_right: not focus Home/Side Menu, pos = " + pos);
        return 0;
    }

    public int get_dx_left(int pos) {
        int itemWidth, itemWidthLast, itemMargin, delta, scrollPos;
        scrollPos = get_count() - 3;

        if (is_in_home()) {
            itemWidth     = getResources().getDimensionPixelSize(R.dimen.home_list_rcv_block_width);
            itemWidthLast = getResources().getDimensionPixelSize(R.dimen.home_list_rcv_block_out_width);
            delta         = getResources().getDimensionPixelSize(R.dimen.home_list_rcv_block_middle_ref);
            return (pos == scrollPos) ? delta - (3 * itemWidth + itemWidthLast)
                                      : -itemWidth;
        }
        if (is_in_side_menu()) {
            itemMargin = get().getResources().getDimensionPixelSize(R.dimen.side_menu_app_list_item_margin_x);
            itemWidth  = get().getResources().getDimensionPixelSize(R.dimen.side_menu_app_list_item_width) + itemMargin * 2;
            delta      = get().getResources().getDimensionPixelSize(R.dimen.side_menu_app_list_item_middle_delta);
            return (pos == scrollPos) ? delta - 4 * itemWidth + itemMargin
                                      : -itemWidth;
        }
        Log.e(TAG, "get_dx_left: not focus Home/Side Menu, pos = " + pos);
        return 0;
    }*/

    public int get_count() {
        RecommendAdapter adapter = (RecommendAdapter) getAdapter();
        if (!hasFocus())
            return 0;
        if (adapter == null)
            return 0;
        return adapter.getItemCount();
    }

    public int get_position() {
        if (true) {
            return g_focused_pos;
        }
        if (!hasFocus())
            return 0;
        View focusedView = getFocusedChild();
        return getChildAdapterPosition(focusedView);
    }

    public int get_previous() {
        return g_prev_pos;
    }

    public void focus_child() {
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

        ViewHolder holder = findViewHolderForAdapterPosition(g_focused_pos);
        if (holder == null) {
            Log.e(TAG, "focus_child: null view holder, focus pos = " + g_focused_pos + ", view = " + this);
            scrollToPosition(g_focused_pos);
            postDelayed(this::focus_child, 50);
            return;
        }

        TextView label = holder.itemView.findViewById(R.id.lo_rcv_item_label);
        Log.d(TAG, "focus_child: focused pos = " + g_focused_pos + ", label = " + label.getText());
        holder.itemView.requestFocus();
    }

    public void add_local_app(String pkgName) {
        RecommendAdapter adapter = (RecommendAdapter) getAdapter();
        if (adapter != null)
            adapter.add_local_app(ListManager.g_localApps, pkgName);
    }

    public void remove_local_app(String pkgName) {
        RecommendAdapter adapter = (RecommendAdapter) getAdapter();
        if (adapter != null)
            adapter.remove_local_app(ListManager.g_localApps, pkgName);
    }
}
