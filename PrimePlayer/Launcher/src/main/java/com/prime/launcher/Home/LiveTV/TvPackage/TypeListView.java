package com.prime.launcher.Home.LiveTV.TvPackage;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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
import com.prime.launcher.Utils.JsonParser.RecommendPackage;

import java.lang.ref.WeakReference;
import java.util.List;

/** @noinspection CommentedOutCode, UnusedReturnValue */
public class TypeListView extends RecyclerView {

    String TAG = getClass().getSimpleName();

    public static final int DURATION_SCROLL         = 100;
    public static final int DURATION_SCROLL_QUICK   = 10;
    public static final int DURATION_ANIMATE        = 100;
    public static final int ITEM_VIEW_CACHE_SIZE    = 1000;
    public static final int SCROLL_DISTANCE_Y       = 90;
    public static final int FLAG_TYPE_POSITION_NONE = 0;
    public static final int FLAG_TYPE_POSITION_TOP  = 1;
    public static final int FLAG_TYPE_POSITION_BOTTOM = 2;

    WeakReference<AppCompatActivity> g_ref;
    TvPackageDialog g_tvPackageDialog;
    Handler g_handler;
    int g_dyRuntime;
    int g_dyShouldBe;
    int g_position;
    int g_previousPkgIndex;
    int g_flagTopBottom;

    public TypeListView(@NonNull Context context) {
        super(context);
    }

    public TypeListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TypeListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init_all(TvPackageDialog tvPackageDialog, List<RecommendPackage> recommendPackages) {
        g_ref = new WeakReference<>(tvPackageDialog.get());
        g_handler = new Handler(Looper.getMainLooper());
        g_tvPackageDialog = tvPackageDialog;
        g_flagTopBottom = FLAG_TYPE_POSITION_NONE;
        Log.d(TAG, "init_all: adapter, layout manager");
        setAdapter(new TypeListAdapter(tvPackageDialog, recommendPackages));
        setLayoutManager(new LinearLayoutManager(get(), LinearLayoutManager.VERTICAL, false));
        setItemViewCacheSize(ITEM_VIEW_CACHE_SIZE);
        setHasFixedSize(true);
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        g_dyRuntime += dy;
        if (g_flagTopBottom == FLAG_TYPE_POSITION_TOP)
            focus_first_item();
        if (g_flagTopBottom == FLAG_TYPE_POSITION_BOTTOM)
            focus_last_item();
    }

    /*@Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);

        if (SCROLL_STATE_IDLE == state) {
            int position = get_position();
            g_dyShouldBe = position > 4 ? SCROLL_DISTANCE_Y * (position - 4) : 0;
            Log.d(TAG, "onScrollStateChanged: [dy] " + g_dyRuntime + " / " + g_dyShouldBe + ", [position] " + position + ", [count] " + get_count());

            // 滾動 dy 不足，再執行一次 scroll
            // 正常應該滾動 dy: g_dyShouldBe
            // 但是只有滾動 dy: g_dyRuntime
            if (position > 4 &&
                position < get_count() - 4) {
                scrollBy(0, g_dyShouldBe - g_dyRuntime);
            }

            if (focus_first_item())
                Log.d(TAG, "onScrollStateChanged: focus first item successfully");

            if (focus_last_item())
                Log.d(TAG, "onScrollStateChanged: focus last item successfully");
        }
    }*/

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
        //if (!hasFocus())
        //    return 0;
        //View focusedView = getFocusedChild();
        //return getChildAdapterPosition(focusedView);
        return g_position;
    }

    public int get_count() {
        if (!hasFocus())
            return 0;
        if (getAdapter() == null)
            return 0;
        return getAdapter().getItemCount();
    }

    public int get_item_height() {
        TypeListAdapter adapter = (TypeListAdapter) getAdapter();
        if (null == adapter) {
            Log.w(TAG, "scroll_to_bottom: null adapter");
            return 0;
        }
        return adapter.get_item_height();
    }

    public boolean is_first_item() {
        return get_position() == 0;
    }

    public boolean is_last_item() {
        return get_position() == get_count() - 1;
    }

    public boolean is_at_top() {
        return g_flagTopBottom == FLAG_TYPE_POSITION_TOP &&
               (g_previousPkgIndex == 0 || g_previousPkgIndex == -1);
    }

    public boolean is_at_bottom() {
        return g_flagTopBottom == FLAG_TYPE_POSITION_BOTTOM &&
               g_previousPkgIndex == get_count() -1;
    }

    public void press_up_down(int keyCode, KeyEvent event) {
        int pkgIndex = get_position();

        Log.d(TAG, "press_up_down: pkgIndex " + pkgIndex + ", previous " + g_previousPkgIndex + ", keycode = " + keyCode);
        if (pkgIndex == g_previousPkgIndex || (pkgIndex == 0 && g_previousPkgIndex == -1)) {
            if (is_first_item()) {
                pkgIndex = get_count() - 1;
                scroll_to_bottom();
            }
            else if (is_last_item()) {
                pkgIndex = 0;
                scroll_to_top(false);
            }
        }

        if (pkgIndex < 0 || pkgIndex >= get_count()) {
            Log.e(TAG, "press_up_down: do not update content list, [pkgIndex] " + pkgIndex);
            return;
        }
        Log.d(TAG, "press_up_down: update content list");
        g_previousPkgIndex = pkgIndex;
        g_tvPackageDialog.update_content_list(pkgIndex);
    }

    public void scroll_up_down(boolean hasFocus, int position, int keyCode) {
        if (hasFocus) {
            //g_position = position;
            // scroll down
            if (KeyEvent.KEYCODE_DPAD_DOWN == keyCode && position > 4)
                smoothScrollBy(0, SCROLL_DISTANCE_Y, null, TypeListView.DURATION_SCROLL);
            // scroll up
            if (KeyEvent.KEYCODE_DPAD_UP == keyCode && position < get_count() - 4)
                smoothScrollBy(0, -SCROLL_DISTANCE_Y, null, TypeListView.DURATION_SCROLL);
        }
    }

    public void scroll_to_top(boolean force) {
        Log.i(TAG, "scroll_to_top: [flagTopBottom] top");
        g_flagTopBottom = FLAG_TYPE_POSITION_TOP;
        g_position = 0;
        if (force)
            scrollBy(0, -Integer.MAX_VALUE);
        smoothScrollBy(0, -Integer.MAX_VALUE, null, DURATION_SCROLL_QUICK);
        smoothScrollToPosition(0);
    }

    public void scroll_to_bottom() {
        Log.i(TAG, "scroll_to_bottom: [flagTopBottom] bottom");
        g_flagTopBottom = FLAG_TYPE_POSITION_BOTTOM;
        int position = get_count() - 1;
        g_position = get_count() - 1;
        if (position < 0 || position >= get_count())
            Log.e(TAG, "scroll_to_bottom: do not scroll, [position] " + position);
        else {
            smoothScrollBy(0, Integer.MAX_VALUE, null, DURATION_SCROLL_QUICK);
            smoothScrollToPosition(position);
        }
    }

    public boolean focus_first_item() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
        if (null == layoutManager)
            return false;

        int firstPosition = layoutManager.findFirstVisibleItemPosition();
        if (firstPosition != 0)
            return false;

        if (!is_at_top())
            return false;

        View firstView = layoutManager.findViewByPosition(firstPosition);
        if (null == firstView)
            return false;

        Log.i(TAG, "focus_last_item: [flagTopBottom] none");
        g_flagTopBottom = FLAG_TYPE_POSITION_NONE;
        firstView.requestFocus();
        return true;
    }

    public boolean focus_last_item() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
        if (null == layoutManager)
            return false;

        int lastPosition = layoutManager.findLastVisibleItemPosition();
        if (lastPosition != get_count() - 1)
            return false;

        if (!is_at_bottom())
            return false;

        View lastView = layoutManager.findViewByPosition(lastPosition);
        if (null == lastView)
            return false;

        Log.i(TAG, "focus_last_item: [flagTopBottom] none");
        g_flagTopBottom = FLAG_TYPE_POSITION_NONE;
        lastView.requestFocus();
        return true;
    }
}
