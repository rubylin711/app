package com.prime.launcher.Home.LiveTV.Rank;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.launcher.HomeActivity;
import com.prime.launcher.R;
import com.prime.launcher.Utils.JsonParser.RankInfo;

import java.lang.ref.WeakReference;
import java.util.List;

public class RankListView extends RecyclerView {

    String TAG = getClass().getSimpleName();

    public static final int DURATION_SCROLL         = 100;
    public static final int DURATION_SCROLL_QUICK   = 10;
    public static final int ITEM_VIEW_CACHE_SIZE    = 1000;

    WeakReference<AppCompatActivity> g_ref;
    int g_dxRuntime;
    int g_dxShouldBe;

    public RankListView(@NonNull Context context) {
        super(context);
    }

    public RankListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RankListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init_all(RankListDialog rankDialog, List<RankInfo> rankInfos) {
        Log.d(TAG, "init_all: adapter, layout manager");
        g_ref = new WeakReference<>(rankDialog.get());
        setAdapter(new RankAdapter(rankDialog, rankInfos));
        setLayoutManager(new LinearLayoutManager(get(), LinearLayoutManager.HORIZONTAL, false));
        setItemViewCacheSize(ITEM_VIEW_CACHE_SIZE);
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        g_dxRuntime += dx;
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        int position, dxDiff, dxFirst, dxNormal;

        if (SCROLL_STATE_IDLE == state) {
            position = get_position();
            dxFirst = getResources().getDimensionPixelSize(R.dimen.rank_item_scroll_first_dx);
            dxNormal = getResources().getDimensionPixelSize(R.dimen.rank_item_scroll_normal_dx);
            //g_dxShouldBe = get_item_width() * (position - 1);
            g_dxShouldBe = dxFirst + dxNormal * (position - 1);
            Log.d(TAG, "onScrollStateChanged: [dx] " + g_dxRuntime + " / " + g_dxShouldBe + ", [position] " + position + ", [count] " + get_count());

            // 滾動 dx 不足，再執行一次 scroll
            // 正常應該滾動 dx: g_dxShouldBe
            // 但是只有滾動 dx: g_dxRuntime
            if (position > 0 &&
                position < get_count() - 1) {
                scroll_by(g_dxShouldBe - g_dxRuntime);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e(TAG, "onKeyDown: ");
        return super.onKeyDown(keyCode, event);
    }

    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    public int get_item_width() {
        if (!hasFocus())
            return 0;
        View focusedView = getFocusedChild();
        return focusedView.getWidth();
    }

    public int get_position() {
        if (!hasFocus())
            return 0;
        View focusedView = getFocusedChild();
        return getChildAdapterPosition(focusedView);
    }

    public int get_count() {
        if (!hasFocus())
            return 0;
        if (getAdapter() == null)
            return 0;
        return getAdapter().getItemCount();
    }

    public void scroll_by(int dx) {
        scrollBy(dx, 0);
        //smoothScrollBy(dx, 0, null, DURATION_SCROLL_QUICK);
    }
}
