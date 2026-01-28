package com.prime.launcher.Hottest;

import static com.prime.launcher.Utils.JsonParser.AppPackage.TYPE_APP;
import static com.prime.launcher.Utils.JsonParser.AppPackage.TYPE_TBC;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.prime.launcher.R;
import com.prime.launcher.Utils.ActivityUtils;
import com.prime.launcher.Utils.JsonParser.AppPackage;

import java.lang.ref.WeakReference;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {

    String TAG = getClass().getSimpleName();
    
    WeakReference<AppCompatActivity> g_ref;
    List<AppPackage> g_appPackages;
    int g_appPosition;
    boolean g_isPressLeftRight;

    public AppListAdapter(AppCompatActivity activity, List<AppPackage> appPackages) {
        g_ref = new WeakReference<>(activity);
        g_appPackages = appPackages;
        g_isPressLeftRight = true;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_hottest_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        View itemView = holder.itemView;
        itemView.setTag(position);
        set_app_icon(itemView, position);
        set_app_name(itemView, position);
        on_focus_app(itemView, position);
        on_click_app(itemView, position);
        on_key_app(itemView, position);
    }

    public void on_focus_app(View itemView, int position) {
        TextView appName = itemView.findViewById(R.id.lo_hottest_app_item_name);
        ImageView appMask = itemView.findViewById(R.id.lo_hottest_app_item_icon_mask);

        itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                appName.setTextColor(get().getColor(R.color.white));
                appName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                appName.setMarqueeRepeatLimit(-1);
                appName.setSelected(true);
                appMask.setBackgroundResource(R.drawable.hot_icon_focus_red);
                if (is_press_left_right()) {
                    set_app_position(position);
                    update_description(position);
                    update_purchase_info(position);
                    update_content_list(position);
                    set_press_left_right(false);
                    get().unblock_detail_focus();
                }
            }
            else {
                appName.setTextColor(get().getColor(R.color.hint));
                appName.setEllipsize(TextUtils.TruncateAt.END);
                appName.setSelected(false);
                appMask.setBackgroundResource(R.drawable.circle_hottest_selector);
            }
            scale_item_view(v, hasFocus, 1.2f, 1.2f, 0.5f, 0.5f);
            move_to_middle(v, hasFocus);
        });
    }

    public void on_click_app(View itemView, int position) {
        itemView.setOnClickListener((v) -> {
            AppPackage appPackage = g_appPackages.get(position);
            String type = appPackage.get_type();
            String pkgName = appPackage.get_package_name();
            String label = appPackage.get_label();

            Log.d(TAG, "on_click_app: [type] " + type + ", [label] " + label + ", [pkgName] " + pkgName);

            if (TYPE_TBC.equals(type))
                return;

            if (TYPE_APP.equals(type))
                ActivityUtils.start_activity(get(), pkgName, label);
        });
    }

    public void on_click_purchase(AppPackage appPackage) {
        Button purchaseBtn = get().findViewById(R.id.lo_hottest_purchase_btn);
        purchaseBtn.setOnClickListener((v) -> {
            Log.d(TAG, "on_purchase_app: [package name] " + appPackage.get_package_name());
            PurchaseAppDialog purchaseAppDialog = new PurchaseAppDialog(get(), appPackage);
            purchaseAppDialog.show();
        });
    }

    public void on_key_app(View itemView, int position) {
        itemView.setOnKeyListener((v, keyCode, event) -> {
            int action = event.getAction();

            if (KeyEvent.ACTION_UP == action)
                return false;

            if (KeyEvent.KEYCODE_DPAD_LEFT == keyCode ||
                KeyEvent.KEYCODE_DPAD_RIGHT == keyCode) {
                set_press_left_right(true);
            }

            return false;
        });
    }

    public void on_key_purchase() {
        Button purchaseBtn = get().findViewById(R.id.lo_hottest_purchase_btn);
        purchaseBtn.setOnKeyListener((v, keyCode, event) -> {
            int action = event.getAction();

            if (KeyEvent.ACTION_UP == action)
                return false;

            if (KeyEvent.KEYCODE_DPAD_UP == keyCode)
                return focus_video_item();

            if (KeyEvent.KEYCODE_DPAD_DOWN == keyCode)
                return focus_app_item();

            if (KeyEvent.KEYCODE_DPAD_LEFT == keyCode ||
                KeyEvent.KEYCODE_DPAD_RIGHT == keyCode)
                return true;

            return false;
        });
    }

    public boolean focus_video_item() {
        VideoListView videoListView = get().findViewById(R.id.lo_hottest_video_list);
        VideoListAdapter adapter = (VideoListAdapter) videoListView.getAdapter();
        if (null == adapter) {
            Log.w(TAG, "focus_video_item: null video list adapter");
            return false;
        }
        View videoView = videoListView.findViewWithTag(adapter.get_video_position());
        if (null == videoView) {
            Log.w(TAG, "focus_video_item: null video view");
            return false;
        }
        videoView.requestFocus();
        return true;
    }

    public boolean focus_app_item() {
        AppListView appListView = get().findViewById(R.id.lo_hottest_app_list);
        AppListAdapter adapter = (AppListAdapter) appListView.getAdapter();
        if (null == adapter) {
            Log.w(TAG, "focus_app_item: null app list adapter");
            return false;
        }
        View appView = appListView.findViewWithTag(adapter.get_app_position());
        if (null == appView) {
            Log.w(TAG, "focus_app_item: null app view");
            return false;
        }
        appView.requestFocus();
        return true;
    }

    public HottestActivity get() {
        return (HottestActivity) g_ref.get();
    }

    @Override
    public int getItemCount() {
        return g_appPackages.size();
    }

    public int get_app_position() {
        return g_appPosition;
    }

    public void set_app_icon(View itemView, int position) {
        AppPackage appPackage = g_appPackages.get(position);
        String pkgName = appPackage.get_package_name();
        ImageView appIcon = itemView.findViewById(R.id.lo_hottest_app_item_icon);
        Drawable iconDrawable = ActivityUtils.get_app_icon(get(), pkgName, false);
        int iconWidth = appIcon.getWidth();
        int iconHeight = appIcon.getHeight();

        if (!get().isFinishing() && !get().isDestroyed())
            Glide.with(get())
                    .load(null == iconDrawable ? appPackage.get_icon_url() : iconDrawable)
                    .placeholder(R.drawable.default_photo_circle)
                    .error(R.drawable.internet_error_circle)
                    .override(iconWidth, iconHeight)
                    .transform(new MultiTransformation(new CenterCrop()), new CircleCrop())
                    .into(appIcon);
    }

    public void set_app_name(View itemView, int position) {
        AppPackage appPackage = g_appPackages.get(position);
        TextView appName = itemView.findViewById(R.id.lo_hottest_app_item_name);
        appName.setText(appPackage.get_label());
        appName.setTextColor(get().getColor(R.color.hint));
        appName.setEllipsize(TextUtils.TruncateAt.END);
        appName.setSelected(false);
    }

    public void set_app_position(int position) {
        g_appPosition = position;
    }

    public void set_press_left_right(boolean leftRight) {
        g_isPressLeftRight = leftRight;
    }

    public boolean is_press_left_right() {
        return g_isPressLeftRight;
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
        recyclerView.smoothScrollBy(offsetX, 0);
        /*if (offsetX < 0)
            Log.d(TAG, "move_to_middle: move left, [offset] " + offsetX);
        else
            Log.d(TAG, "move_to_middle: move right, [offset] " + offsetX);*/
    }

    public void update_description(int appPosition) {
        AppPackage appPackage = g_appPackages.get(appPosition);
        String type = appPackage.get_type();
        String description = "";
        int contentSize = appPackage.get_content().size();

        if (TYPE_TBC.equals(type) && contentSize > 0) {
            TextView descView = get().findViewById(R.id.lo_hottest_desc);
            description = appPackage.get_content().get(0).get_description();
            descView.setText(description);
        }
        Log.d(TAG, "update_description: [description] " + description);
    }

    public void update_purchase_info(int appPosition) {
        AppPackage appPackage = g_appPackages.get(appPosition);
        Button purchaseBtn = get().findViewById(R.id.lo_hottest_purchase_btn);
        TextView purchaseHint = get().findViewById(R.id.lo_hottest_plan_hint);

        if (TYPE_TBC.equals(appPackage.get_type())) {
            Log.d(TAG, "update_purchase_info: hide purchase view");
            purchaseBtn.setVisibility(View.GONE);
            purchaseHint.setVisibility(View.GONE);
        }
        if (TYPE_APP.equals(appPackage.get_type())) {
            Log.d(TAG, "update_purchase_info: [planText] " + appPackage.get_plan_text());
            purchaseBtn.setText(appPackage.get_btn_label());
            purchaseHint.setText(appPackage.get_plan_text());
            purchaseBtn.setVisibility(View.VISIBLE);
            purchaseHint.setVisibility(View.VISIBLE);
            on_click_purchase(appPackage);
            on_key_purchase();
        }
    }

    public void update_content_list(int appPosition) {
        Log.d(TAG, "update_content_list: [app position] " + appPosition);
        VideoListView videoListView = get().findViewById(R.id.lo_hottest_video_list);
        videoListView.update_content_list(g_appPackages, appPosition);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class ItemDecoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
            if (parent.getChildAdapterPosition(view) == 0)
                outRect.left = view.getContext().getResources().getDimensionPixelSize(R.dimen.hottest_app_item_rect_55dp);
            outRect.right = view.getContext().getResources().getDimensionPixelSize(R.dimen.hottest_app_item_rect_30dp);
            outRect.top = view.getContext().getResources().getDimensionPixelSize(R.dimen.hottest_app_item_rect_15dp);
        }
    }
}
