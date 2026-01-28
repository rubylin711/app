package com.prime.launcher.Home.Recommend.Installer;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ScreenshotsView extends RecyclerView {

    String TAG = getClass().getSimpleName();

    public static final int DURATION_SCROLL = 50;
    public static final int DURATION_ANIMATE = 100;
    public static final int ITEM_VIEW_CACHE_SIZE = 1000;

    WeakReference<AppCompatActivity> g_ref;

    public ScreenshotsView(@NonNull Context context) {
        super(context);
    }

    public ScreenshotsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ScreenshotsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init_list(AppCompatActivity activity, ArrayList<String> screens) {
        if (InstallerActivity.FLAG_FOR_TEST) {
            screens.add("https://acs-ota.tbcnet.net.tw/app_bd201/ico/1636453497296.png");
        }
        g_ref = new WeakReference<>(activity);
        setAdapter(new ScreenshotsAdapter(activity, screens));
        setLayoutManager(new LinearLayoutManager(activity, HORIZONTAL, false));
        addItemDecoration(new ScreenshotsAdapter.ItemDecoration(get()));
        setHasFixedSize(true);
        setItemViewCacheSize(ITEM_VIEW_CACHE_SIZE);
    }

    public InstallerActivity get() {
        return (InstallerActivity) g_ref.get();
    }
}
