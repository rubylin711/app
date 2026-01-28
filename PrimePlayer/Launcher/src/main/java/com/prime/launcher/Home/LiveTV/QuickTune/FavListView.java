package com.prime.launcher.Home.LiveTV.QuickTune;

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
import com.prime.datastructure.sysdata.MiscDefine;
import com.prime.datastructure.sysdata.ProgramInfo;

import java.lang.ref.WeakReference;
import java.util.List;

public class FavListView extends RecyclerView {

    String TAG = getClass().getSimpleName();

    public static final int SCROLL_DURATION         = 100;
    public static final int ITEM_VIEW_CACHE_SIZE    = 1000;

    WeakReference<AppCompatActivity> g_ref;
    QuickTuneDialog g_quickDialog;
    int g_dyRuntime;
    int g_dyShouldBe;
    int g_scroll_state;

    public FavListView(@NonNull Context context) {
        super(context);
    }

    public FavListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FavListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init_all(QuickTuneDialog quickDialog) {

        if (null == g_ref) {
            Log.d(TAG, "init_all: init favorites view");
            g_ref = new WeakReference<>(quickDialog.get());
            g_quickDialog = quickDialog;
            setLayoutManager(new LinearLayoutManager(quickDialog.get(), LinearLayoutManager.VERTICAL, false));
            setAdapter(new FavListAdapter(quickDialog));
            setItemViewCacheSize(ITEM_VIEW_CACHE_SIZE);
        }
        else {
            Log.d(TAG, "init_all: update favorites view");
            update_list_view();
        }
        g_quickDialog.show_favorite_visibility();

        // reset for scroll again
        reset_for_scroll();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (KeyEvent.KEYCODE_PROG_YELLOW == keyCode) {
            Log.d(TAG, "onKeyDown: show add window");
            g_quickDialog.show_add_window();
        }
        else
            Log.w(TAG, "onKeyDown: not match [keycode] " + keyCode);

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        g_dyRuntime += dy;
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        int position, dxDiff, dyFirst, dyNormal;

        g_scroll_state =state;

        if (SCROLL_STATE_IDLE == state) {
            position = get_position();
            dyFirst = getResources().getDimensionPixelSize(R.dimen.quick_tune_favorite_item_first_scroll_size);
            dyNormal = get_item_height();

            g_dyShouldBe = dyFirst + dyNormal * (position - 3);
            Log.d(TAG, "onScrollStateChanged: [dy] " + g_dyRuntime + " / " + g_dyShouldBe + ", [position] " + position + ", [count] " + get_count());

            // 滾動 dx 不足，再執行一次 scroll
            // 正常應該滾動 dx: g_dxShouldBe
            // 但是只有滾動 dx: g_dxRuntime
            if (position > 3 &&
                position < get_count() - 4) {
                scroll_by(g_dyShouldBe - g_dyRuntime);
            }
        }
    }

    HomeActivity get() {
        return g_quickDialog.get();
    }

    public int get_item_count() {
        AddListAdapter adapter = (AddListAdapter) getAdapter();
        if (null == adapter) {
            Log.w(TAG, "get_item_count: null adapter");
            return 0;
        }
        return adapter.getItemCount();
    }

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
        if (focusedView == null)
            return 0;
        return getChildAdapterPosition(focusedView);
    }

    public int get_count() {
        if (!hasFocus())
            return 0;
        if (getAdapter() == null)
            return 0;
        return getAdapter().getItemCount();
    }

    public int get_scroll_state() {
        return g_scroll_state;
    }

    public boolean is_scroll_idle() {
        return RecyclerView.SCROLL_STATE_IDLE == g_scroll_state;
    }

    public void scroll_by(int dy) {
        scrollBy(0, dy);
        //smoothScrollBy(0, dy, null, 500);
    }

    public void update_list_view() {
        List<ProgramInfo> favChannels;
        FavListAdapter adapter;

        adapter = (FavListAdapter) getAdapter();
        if (null == adapter) {
            Log.w(TAG, "update_list_view: null adapter");
            return;
        }

        favChannels = get().g_dtv.get_program_info_list(
                g_quickDialog.get_group_type(),
                MiscDefine.ProgramInfo.POS_ALL,
                MiscDefine.ProgramInfo.POS_ALL);

        adapter.update_list_view(favChannels);
        reset_for_scroll();
        scrollToPosition(0);
        requestFocus();
    }

    public void reset_for_scroll() {
        g_dyRuntime = 0;
        g_dyShouldBe = 0;
        g_scroll_state = 0;
    }
}
