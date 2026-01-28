package com.prime.dmg.launcher.Home.LiveTV.Recommendation;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.R;

import java.lang.ref.WeakReference;

public class PopularListView extends RecyclerView {

    String TAG = getClass().getSimpleName();

    public static final int DURATION_SCROLL = 50;
    public static final int DURATION_ANIMATE_SCALE_DOWN = 100;
    public static final int DURATION_ANIMATE_SCALE_UP = 100;
    public static final int ITEM_VIEW_CACHE_SIZE = 1000;

    WeakReference<AppCompatActivity> g_ref;
    RecommendationDialog g_recommendationDialog;
    int g_dxRuntime;
    int g_dxShouldBe;
    boolean g_scrollIdle;

    public PopularListView(@NonNull Context context) {
        super(context);
    }

    public PopularListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PopularListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        int position = get_position();
        int dxStart = get().getResources().getDimensionPixelSize(R.dimen.recommendation_popular_item_scroll_size_start);
        int dxNormal = get().getResources().getDimensionPixelSize(R.dimen.recommendation_popular_item_scroll_size);

        // dx runtime
        g_dxRuntime += dx;
        // dx should scroll
        g_dxShouldBe = 0;
        if (position >= 3)
            g_dxShouldBe = dxStart + (dxNormal * (position - 3));
        // re-scroll
        smoothScrollBy(g_dxShouldBe - g_dxRuntime, 0, null, DURATION_SCROLL);
        //Log.e(TAG, "onScrolled: [dx] " + g_dxRuntime + " / " + g_dxShouldBe + ", [position] " + position + ", [count] " + get_count());
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        g_scrollIdle = PopularListView.SCROLL_STATE_IDLE == state;
    }

    public void init_list(RecommendationDialog dialog, PopularListAdapter popularListAdapter) {
        g_ref = new WeakReference<>(dialog.get());
        g_recommendationDialog = dialog;
        g_scrollIdle = true;
        setAdapter(popularListAdapter);
        setLayoutManager(new LinearLayoutManager(get(), HORIZONTAL, false));
        addItemDecoration(popularListAdapter.get_item_decoration(dialog));
        setHasFixedSize(true);
        setItemViewCacheSize(ITEM_VIEW_CACHE_SIZE);
    }

    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    public int get_item_width() {
        View focusedView = null;

        if (!hasFocus())
            return 0;

        if (isFocused() && get_count() != 0)
            focusedView = getChildAt(0);

        if (focusedView != null)
            focusedView.requestFocus();

        focusedView = getFocusedChild();
        if (null == focusedView)
            return 0;

        return focusedView.getWidth();
    }

    public int get_position() {
        View focusedView = null;

        if (!hasFocus())
            return 0;

        if (isFocused() && get_count() != 0)
            focusedView = getChildAt(0);

        if (focusedView != null)
            focusedView.requestFocus();

        focusedView = getFocusedChild();
        if (null == focusedView)
            return 0;

        return getChildAdapterPosition(focusedView);
    }

    public View get_position_view() {
        return findViewWithTag(get_position());
    }

    public int get_count() {
        PopularListAdapter adapter = (PopularListAdapter) getAdapter();
        if (adapter == null)
            return 0;
        return adapter.getItemCount();
    }

    public boolean is_scroll_idle() {
        return g_scrollIdle;
    }
}
