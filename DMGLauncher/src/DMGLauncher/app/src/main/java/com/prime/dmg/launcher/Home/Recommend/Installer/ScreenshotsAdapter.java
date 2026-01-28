package com.prime.dmg.launcher.Home.Recommend.Installer;

import static android.view.KeyEvent.*;

import android.animation.Animator;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.prime.dmg.launcher.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ScreenshotsAdapter extends RecyclerView.Adapter<ScreenshotsAdapter.ViewHolder> implements Animator.AnimatorListener {

    String TAG = getClass().getSimpleName();

    WeakReference<AppCompatActivity> g_ref;
    ArrayList<String> g_screens;

    public ScreenshotsAdapter(AppCompatActivity activity, ArrayList<String> screens) {
        g_ref = new WeakReference<>(activity);
        g_screens = screens;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_screenshots_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ImageView screenshotView = holder.itemView.findViewById(R.id.lo_screenshot_image);
        set_screenshot(screenshotView, position);
        on_focus_screenshot(holder, position);
        on_key_screenshot(holder, position);
    }

    public void on_focus_screenshot(ViewHolder holder, int position) {
        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            int duration = ScreenshotsView.DURATION_ANIMATE;
            scale_view(v, hasFocus, duration, false);
            move_to_middle(v, hasFocus);
        });
    }

    public void on_key_screenshot(ViewHolder holder, int position) {
        holder.itemView.setOnKeyListener((v, keyCode, event) -> {
            PageListView pageListView = get().findViewById(R.id.lo_page_list_view);
            ScreenshotsView screenshotsView = (ScreenshotsView) holder.itemView.getParent();
            int dy = get().getResources().getDimensionPixelSize(R.dimen.install_app_screenshots_total_height);

            if (ACTION_UP == event.getAction())
                return false;

            if (KEYCODE_DPAD_UP == keyCode) {
                screenshotsView.scrollToPosition(0);
                pageListView.smoothScrollBy(0, -dy);
                Log.d(TAG, "on_key_screenshot: scroll up");
            }
            return false;
        });
    }

    @Override
    public void onAnimationStart(@NonNull Animator animation) {

    }

    @Override
    public void onAnimationEnd(@NonNull Animator animation) {

    }

    @Override
    public void onAnimationCancel(@NonNull Animator animation) {

    }

    @Override
    public void onAnimationRepeat(@NonNull Animator animation) {

    }

    public InstallerActivity get() {
        return (InstallerActivity) g_ref.get();
    }

    @Override
    public int getItemCount() {
        return g_screens.size();
    }

    public void set_screenshot(ImageView screenshotView, int position) {
        int width = get().getResources().getDimensionPixelSize(R.dimen.install_app_banner_width);
        int height = get().getResources().getDimensionPixelSize(R.dimen.install_app_banner_height);

        if (position >= g_screens.size())
            return;

        if (!get().isFinishing() && !get().isDestroyed())
            Glide.with(get())
                    .load(g_screens.get(position))
                    .placeholder(R.drawable.default_photo)
                    .error(R.drawable.internet_error)
                    .transform(new RoundedCorners(5))
                    .override(width, height)
                    .skipMemoryCache(false)
                    .into(screenshotView);
    }

    public void scale_view(View view, boolean hasFocus, int duration, boolean alignStart) {
        if (hasFocus)
            scale_up(view, duration, alignStart);
        else
            scale_down(view, duration, alignStart);
    }

    public void scale_up(View view, int duration, boolean alignStart) {
        float scaleFactor = 1.2f;
        float translationX = 0;

        if (alignStart) {
            int parentWidth = ((View) view.getParent()).getWidth();
            int scaledWidth = (int) (view.getWidth() * scaleFactor);
            translationX = Math.max(0, (scaledWidth - parentWidth) / 2);
        }

        view.animate().setListener(this);
        view.animate().scaleX(scaleFactor).scaleY(scaleFactor).z(Integer.MAX_VALUE).translationXBy(translationX).setDuration(duration).start();

        if (view instanceof TextView) {
            int color = view.getContext().getResources().getColor(R.color.pvr_red_color, null);
            ((TextView) view).setTextColor(color);
        }
    }

    public void scale_down(View view, int duration, boolean alignStart) {
        float scaleFactor = 1.2f;
        float translationX = 0;

        if (alignStart) {
            int parentWidth = ((View) view.getParent()).getWidth();
            int scaledWidth = (int) (view.getWidth() * scaleFactor);
            translationX = Math.max(0, (scaledWidth - parentWidth) / 2);
        }

        view.animate().setListener(null);
        view.animate().scaleX(1.0f).scaleY(1.0f).z(0.0f).translationXBy(-translationX).setDuration(duration).start();

        if (view instanceof TextView)
            ((TextView) view).setTextColor(Color.WHITE);
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class ItemDecoration extends RecyclerView.ItemDecoration {
        WeakReference<AppCompatActivity> g_ref;

        public ItemDecoration(AppCompatActivity activity) {
            g_ref = new WeakReference<>(activity);
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
            if (null == parent.getAdapter())
                return;

            AppCompatActivity activity = g_ref.get();
            int position = parent.getChildAdapterPosition(view);
            int count = parent.getAdapter().getItemCount();
            int itemDivider = activity.getResources().getDimensionPixelSize(R.dimen.install_app_screenshots_view_space_divider);
            int itemStartEnd = g_ref.get().getResources().getDimensionPixelSize(R.dimen.install_app_screenshots_view_space_start_end);
            int itemTop = g_ref.get().getResources().getDimensionPixelSize(R.dimen.install_app_screenshots_view_space_top);

            outRect.left = itemDivider;
            outRect.right = itemDivider;
            outRect.top = itemTop;

            if (position == 0)
                outRect.left = itemStartEnd;
            if (position == count - 1)
                outRect.right = itemStartEnd;
        }
    }
}
