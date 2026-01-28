package com.prime.launcher.Home.Recommend.Installer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.launcher.R;

import java.lang.ref.WeakReference;

public class PageListView extends RecyclerView {

    String TAG = getClass().getSimpleName();

    public static final int DURATION_SCROLL = 500;
    public static final int DURATION_ANIMATE_SCALE_DOWN = 100;
    public static final int DURATION_ANIMATE_SCALE_UP = 100;
    public static final int ITEM_VIEW_CACHE_SIZE = 1000;

    WeakReference<AppCompatActivity> g_ref;
    int g_dyRuntime;
    int g_dyShouldBe;

    public PageListView(@NonNull Context context) {
        super(context);
    }

    public PageListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PageListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init_list(AppCompatActivity activity, InstallData installData) {
        g_ref = new WeakReference<>(activity);
        setAdapter(new PageListAdapter(get(), installData));
        setLayoutManager(new LinearLayoutManager(get(), VERTICAL, false));
        setHasFixedSize(true);
        setItemViewCacheSize(ITEM_VIEW_CACHE_SIZE);
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        int position = get_position();
        int dyStart = get().getResources().getDimensionPixelSize(R.dimen.install_app_list_scroll_size_first);
        int dyNormal = get().getResources().getDimensionPixelSize(R.dimen.install_app_screenshots_total_height);

        // dy runtime
        g_dyRuntime += dy;
        // dy should scroll
        g_dyShouldBe = 0;
        if (position >= 1)
            g_dyShouldBe = dyStart + (dyNormal * (position - 1));
        // re-scroll
        smoothScrollBy(0, g_dyShouldBe - g_dyRuntime, null, DURATION_SCROLL);
        //Log.e(TAG, "onScrolled: [dx] " + g_dyRuntime + " / " + g_dyShouldBe + ", [position] " + position + ", [count] " + get_count());
    }

    public InstallerActivity get() {
        return (InstallerActivity) g_ref.get();
    }

    public int get_position() {
        if (true) {
            if (null == getAdapter())
                return 0;

            PageListAdapter adapter = (PageListAdapter) getAdapter();
            return adapter.get_position();
        }

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

    public int get_count() {
        PageListAdapter adapter = (PageListAdapter) getAdapter();
        if (adapter == null)
            return 0;
        return adapter.getItemCount();
    }
}
