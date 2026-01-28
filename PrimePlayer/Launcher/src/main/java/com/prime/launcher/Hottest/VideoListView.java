package com.prime.launcher.Hottest;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.launcher.Home.Recommend.List.RecommendItem;
import com.prime.launcher.R;
import com.prime.launcher.Utils.ActivityUtils;
import com.prime.launcher.Utils.JsonParser.AppPackage;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class VideoListView extends RecyclerView {

    String TAG = getClass().getSimpleName();

    public static final int DURATION_SCROLL = 100;
    public static final int DURATION_ANIMATE_SCALE_DOWN = 100;
    public static final int DURATION_ANIMATE_SCALE_UP = 100;
    public static final int ITEM_VIEW_CACHE_SIZE = 1000;

    WeakReference<AppCompatActivity> g_ref;

    public VideoListView(@NonNull Context context) {
        super(context);
    }

    public VideoListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(AppCompatActivity activity) {
        g_ref = new WeakReference<>(activity);
        setLayoutManager(new LinearLayoutManager(get(), HORIZONTAL, false));
        addItemDecoration(new VideoListAdapter.ItemDecoration());
        setHasFixedSize(true);
        setItemViewCacheSize(ITEM_VIEW_CACHE_SIZE);
    }

    public void update_content_list(List<AppPackage> appPackages, int appPosition) {
        AppPackage appPackage = appPackages.get(appPosition);
        String type = appPackage.get_type();
        String pkgName = appPackage.get_package_name();
        boolean is_app_installed = ActivityUtils.is_app_installed(get(), pkgName);

        Log.d(TAG, "update_content_list: [type] " + type + ", [label] " + appPackage.get_label() + ", [pkgName] " + pkgName + ", [installed] " + is_app_installed);

        hide_video_hint();

        if (AppPackage.TYPE_TBC.equals(type))
            setAdapter(new VideoListAdapter(get(), appPackages, appPosition));

        if (AppPackage.TYPE_APP.equals(type)) {
            if (is_app_installed) {
                update_content_from_db(appPackage);
                setAdapter(new VideoListAdapter(get(), appPackages, appPosition));

                if (appPackage.get_content().isEmpty())
                    show_video_hint(R.string.hottest_app_not_provide_recommend_video);
            }
            else {
                setAdapter(new VideoListAdapter(get(), appPackages, appPosition));
                show_video_hint(R.string.hottest_app_not_install);
            }
        }
        //Log.d(TAG, "update_list: [video size] " + appPackage.get_content().size());
    }

    public void update_content_from_db(AppPackage appPackage) {
        List<AppPackage.Content> contentList = new ArrayList<>();
        String pkgName = appPackage.get_package_name();

        Log.d(TAG, "update_content_from_db: [package name] " + pkgName);

        for (RecommendItem item : ActivityUtils.get_tv_programs(get(), pkgName)) {
            AppPackage.Content content = new AppPackage.Content();
            content.set_title(item.get_label());
            content.set_poster(item.get_poster());
            content.set_url(item.get_intent_uri());
            contentList.add(content);
        }
        appPackage.set_content(contentList);
    }

    public AppCompatActivity get() {
        return (HottestActivity) g_ref.get();
    }

    public void show_video_hint(int resId) {
        TextView videoHint = get().findViewById(R.id.lo_hottest_video_hint);
        videoHint.setText(resId);
        videoHint.setVisibility(VISIBLE);
    }

    public void hide_video_hint() {
        View videoHint = get().findViewById(R.id.lo_hottest_video_hint);
        videoHint.setVisibility(GONE);
    }
}
