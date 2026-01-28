package com.prime.dmg.launcher.Hottest;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.dmg.launcher.Utils.JsonParser.AppPackage;

import java.lang.ref.WeakReference;
import java.util.List;

public class AppListView extends RecyclerView {

    String TAG = getClass().getSimpleName();

    public static final int DURATION_SCROLL = 50;
    public static final int DURATION_ANIMATE_SCALE_DOWN = 100;
    public static final int DURATION_ANIMATE_SCALE_UP = 100;
    public static final int ITEM_VIEW_CACHE_SIZE = 1000;

    WeakReference<AppCompatActivity> g_ref;

    public AppListView(@NonNull Context context) {
        super(context);
    }

    public AppListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AppListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(AppCompatActivity activity, List<AppPackage> appPackages) {
        g_ref = new WeakReference<>(activity);
        setAdapter(new AppListAdapter(get(), appPackages));
        setLayoutManager(new LinearLayoutManager(get(), HORIZONTAL, false));
        addItemDecoration(new AppListAdapter.ItemDecoration());
        setHasFixedSize(true);
        setItemViewCacheSize(ITEM_VIEW_CACHE_SIZE);
    }

    public void update_hot_video(List<AppPackage> appPackages) {
        get().block_detail_focus();
        setAdapter(new AppListAdapter(get(), appPackages));
    }

    public HottestActivity get() {
        return (HottestActivity) g_ref.get();
    }

}
