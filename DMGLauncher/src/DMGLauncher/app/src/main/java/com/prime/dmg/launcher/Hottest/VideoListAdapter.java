package com.prime.dmg.launcher.Hottest;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.graphics.Rect;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Utils.ActivityUtils;
import com.prime.dmg.launcher.Utils.JsonParser.AppPackage;

import java.lang.ref.WeakReference;
import java.util.List;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.ViewHolder> {

    String TAG = getClass().getSimpleName();

    WeakReference<AppCompatActivity> g_ref;
    AppPackage g_appPackage;
    List<AppPackage.Content> g_contentList;
    int g_videoPosition;

    public VideoListAdapter(AppCompatActivity activity, List<AppPackage> appPackages, int appPosition) {
        g_ref = new WeakReference<>(activity);
        g_appPackage = appPackages.get(appPosition);
        g_contentList = g_appPackage.get_content();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_hottest_video, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        View itemView = holder.itemView;
        itemView.setTag(position);
        set_video_layout(itemView, position);
        set_video_title(itemView, position);
        set_video_thumb(itemView, position);
        on_focus_video(itemView, position);
        on_click_video(itemView, position);
        on_key_video(itemView, position);
    }

    public void on_focus_video(View itemView, int position) {
        TextView titleView = itemView.findViewById(R.id.lo_hottest_title);
        View focusView = itemView.findViewById(R.id.lo_hottest_rec_app_focus);

        itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                focusView.setVisibility(View.VISIBLE);
                titleView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                titleView.setMarqueeRepeatLimit(-1);
                titleView.setSelected(true);
                set_description(position);
                set_video_position(position);
            }
            else {
                focusView.setVisibility(View.GONE);
                titleView.setEllipsize(TextUtils.TruncateAt.END);
                titleView.setSelected(false);
            }
            scale_item_view(itemView, hasFocus, 1.15f, 1.15f, 0.5f, 0.5f);
            move_to_middle(itemView, hasFocus);
        });
    }

    public void on_click_video(View itemView, int position) {
        itemView.setOnClickListener((v) -> {
            AppPackage.Content content = g_contentList.get(position);
            String type = g_appPackage.get_type();
            String pkgName = g_appPackage.get_package_name();
            String sourceType = content.get_source_type();
            String uri = content.get_url();

            Log.d(TAG, "on_click_video: [type] " + type + ", [sourceType] " + sourceType + ", [uri] " + uri);
            if (AppPackage.TYPE_TBC.equals(type))
                open_content(position);

            if (AppPackage.TYPE_APP.equals(type))
                ActivityUtils.start_by_uri(get(), uri, pkgName);
        });
    }

    public void on_key_video(View itemView, int position) {
        itemView.setOnKeyListener((v, keyCode, event) -> {
            int action = event.getAction();

            if (KeyEvent.ACTION_UP == action)
                return false;

            if (KeyEvent.KEYCODE_DPAD_DOWN == keyCode) {
                String type = g_appPackage.get_type();
                if (AppPackage.TYPE_TBC.equals(type)) {
                    AppListView appListView = get().findViewById(R.id.lo_hottest_app_list);
                    AppListAdapter adapter = (AppListAdapter) appListView.getAdapter();
                    if (null == adapter) {
                        Log.w(TAG, "on_key_video: null app list adapter");
                        return false;
                    }
                    View appView = appListView.findViewWithTag(adapter.get_app_position());
                    if (null == appView) {
                        Log.w(TAG, "on_key_video: null app view");
                        return false;
                    }
                    appView.requestFocus();
                    return true;
                }
                if (AppPackage.TYPE_APP.equals(type)) {
                    Button purchaseBtn = get().findViewById(R.id.lo_hottest_purchase_btn);
                    if (null == purchaseBtn) {
                        Log.w(TAG, "on_key_video: null purchase button");
                        return false;
                    }
                    purchaseBtn.requestFocus();
                    return true;
                }
            }

            return false;
        });
    }

    public void open_content(int position) {
        AppPackage.Content content = g_contentList.get(position);
        String sourceType = content.get_source_type();

        switch (sourceType) {
            case AppPackage.SOURCE_TYPE_YOUTUBE:
                Log.w(TAG, "open_content: YOUTUBE");
                ActivityUtils.start_youtube(get(), content.get_video_id(), TAG);
                break;
            case AppPackage.SOURCE_TYPE_MYVIDEO:
                Log.w(TAG, "open_content: MYVIDEO");
                break;
            case AppPackage.SOURCE_TYPE_CATCHPLAY:
                Log.w(TAG, "open_content: CATCHPLAY");
                break;
            case AppPackage.SOURCE_TYPE_STREAM:
                Log.w(TAG, "open_content: STREAM");
                ActivityUtils.show_stream(get(), content.get_url());
                break;
            case AppPackage.SOURCE_TYPE_BROWSER:
            case AppPackage.SOURCE_TYPE_POSTER:
                Log.w(TAG, "open_content: BROWSER / POSTER");
                ActivityUtils.show_browser(get(), content.get_url());
                break;
            case AppPackage.SOURCE_TYPE_QRCODE:
                Log.d(TAG, "open_content: QRCODE [url] " + content.get_url());
                ActivityUtils.show_qrcode(get(), content.get_url());
                break;
            case AppPackage.SOURCE_TYPE_PROGRAM:
                Log.w(TAG, "open_content: PROGRAM");
                ActivityUtils.show_program_detail(get(), content);
                break;
            case AppPackage.SOURCE_TYPE_APP:
            case AppPackage.SOURCE_TYPE_CP:
            case AppPackage.SOURCE_TYPE_CP_MULTI:
                Log.w(TAG, "open_content: APP / CP / CP MULTI");
                ActivityUtils.show_app(get(), content.get_trigger_uri(), content.get_package_name());
                break;
        }
    }

    public AppCompatActivity get() {
        return (HottestActivity) g_ref.get();
    }

    @Override
    public int getItemCount() {
        return g_contentList.size();
    }

    public int get_video_position() {
        return g_videoPosition;
    }

    public void set_video_layout(View itemView, int position) {
        ConstraintLayout.LayoutParams layoutParams;
        View itemLayer, thumbView, focusView;
        int itemHeight;
        String type = g_appPackage.get_type();

        if (AppPackage.TYPE_TBC.equals(type)) {
            itemLayer = itemView.findViewById(R.id.lo_hottest_video_item_layer);
            thumbView = itemView.findViewById(R.id.lo_hottest_thumb);
            itemHeight = get().getResources().getDimensionPixelSize(R.dimen.hottest_video_item_layout_height);

            layoutParams = new ConstraintLayout.LayoutParams(WRAP_CONTENT, itemHeight);
            itemView.setLayoutParams(layoutParams);
            itemLayer.setLayoutParams(layoutParams);
            thumbView.setLayoutParams(layoutParams);
        }
    }

    public void set_video_title(View itemView, int position) {
        TextView titleView = itemView.findViewById(R.id.lo_hottest_title);
        String type = g_appPackage.get_type();

        if (AppPackage.TYPE_TBC.equals(type)) {
            titleView.setVisibility(View.GONE);
            return;
        }
        titleView.setText(g_contentList.get(position).get_title());
    }

    public void set_video_thumb(View itemView, int position) {
        ImageView thumbView = itemView.findViewById(R.id.lo_hottest_thumb);
        if (!get().isFinishing() && !get().isDestroyed())
            Glide.with(get())
                    .load(g_contentList.get(position).get_poster())
                    .placeholder(R.drawable.default_photo)
                    .error(R.drawable.internet_error)
                    .into(thumbView);
    }

    public void set_description(int videoPosition) {
        String type = g_appPackage.get_type();

        if (AppPackage.TYPE_TBC.equals(type)) {
            TextView descView = get().findViewById(R.id.lo_hottest_desc);
            String description = g_contentList.get(videoPosition).get_description();
            descView.setText(description);
            Log.d(TAG, "set_description: " + description);
        }
    }

    public void set_video_position(int position) {
        g_videoPosition = position;
    }

    public void scale_item_view(final View view, boolean hasFocus, float scaleX, float scaleY, float pivotX, float pivotY) {
        ScaleAnimation scaleAnimation;
        int duration = 60;
        int moveUp = 12;
        int moveDown = 8;

        if (hasFocus) {
            scaleAnimation = new ScaleAnimation(1.0f, scaleX, 1.0f, scaleY, 1, pivotX, 1, pivotY);
            scaleAnimation.setDuration(duration);
            scaleAnimation.setFillAfter(true);
            view.startAnimation(scaleAnimation);
            ViewCompat.setElevation(view, moveUp);
        }
        else {
            scaleAnimation = new ScaleAnimation(scaleX, 1.0f, scaleY, 1.0f, 1, pivotX, 1, pivotY);
            scaleAnimation.setDuration(duration);
            scaleAnimation.setFillAfter(true);
            view.startAnimation(scaleAnimation);
            ViewCompat.setElevation(view, moveDown);
        }
    }

    public void move_to_middle(View itemView, boolean hasFocus) {
        if (!hasFocus)
            return;
        int[] location = new int[2];
        itemView.getLocationOnScreen(location);
        int viewCenterX = location[0] + itemView.getWidth() / 2;
        int screenCenterX = itemView.getResources().getDisplayMetrics().widthPixels / 2;
        int offsetX = viewCenterX - screenCenterX;
        RecyclerView recyclerView = (RecyclerView) itemView.getParent();
        recyclerView.smoothScrollBy(offsetX, 0, null, VideoListView.DURATION_SCROLL);
        /*if (offsetX < 0)
            Log.d(TAG, "move_to_middle: move left, [offset] " + offsetX);
        else
            Log.d(TAG, "move_to_middle: move right, [offset] " + offsetX);*/
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class ItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
            int childAdapterPosition = parent.getChildAdapterPosition(view);
            int itemCount = 0;

            if (parent.getAdapter() != null)
                itemCount = parent.getAdapter().getItemCount();

            outRect.top = view.getContext().getResources().getDimensionPixelSize(R.dimen.hottest_app_item_rect_16dp);
            if (childAdapterPosition == 0)
                outRect.left = view.getContext().getResources().getDimensionPixelSize(R.dimen.hottest_app_item_rect_30dp);
            if (childAdapterPosition == itemCount - 1)
                outRect.right = view.getContext().getResources().getDimensionPixelSize(R.dimen.hottest_app_item_rect_30dp);
            else
                outRect.right = view.getContext().getResources().getDimensionPixelSize(R.dimen.hottest_app_item_rect_10dp);
        }
    }
}
