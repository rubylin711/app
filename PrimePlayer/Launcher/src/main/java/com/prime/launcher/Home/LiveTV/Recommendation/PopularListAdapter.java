package com.prime.launcher.Home.LiveTV.Recommendation;

import android.graphics.Rect;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.prime.launcher.Home.Recommend.List.RecommendItem;
import com.prime.launcher.HomeActivity;
import com.prime.launcher.R;
import com.prime.launcher.Utils.ActivityUtils;

import java.lang.ref.WeakReference;
import java.util.List;

public class PopularListAdapter extends RecyclerView.Adapter<PopularListAdapter.ViewHolder> {

    String TAG = getClass().getSimpleName();

    WeakReference<AppCompatActivity> g_ref;
    RecommendationDialog g_recommendationDialog;
    List<RecommendItem> g_popularData;

    public PopularListAdapter(RecommendationDialog recommendationDialog, List<RecommendItem> popularData) {
        g_ref = new WeakReference<>(recommendationDialog.get());
        g_recommendationDialog = recommendationDialog;
        g_popularData = popularData;
    }

    @NonNull
    @Override
    public PopularListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_recommendation_popular, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.itemView.setTag(position);
        set_video_icon(holder, position);
        set_video_label(holder, position);
        on_focus_video(holder, position);
        on_key_video(holder, position);
        on_click_video(holder, position);
    }

    public void on_focus_video(ViewHolder holder, int position) {
        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            View frameFocus = holder.itemView.findViewById(R.id.lo_recommendation_video_focus);
            if (hasFocus) {
                frameFocus.setVisibility(View.VISIBLE);
                float SCALE_X_Y_with_Z = 1.23f;
                holder.itemView.setZ(SCALE_X_Y_with_Z);
                holder.itemView.animate()
                        .setDuration(PopularListView.DURATION_ANIMATE_SCALE_UP)
                        .scaleX(SCALE_X_Y_with_Z)
                        .scaleY(SCALE_X_Y_with_Z)
                        .start();
            }
            else {
                frameFocus.setVisibility(View.GONE);
                float SCALE_X_Y_with_Z = 1.0f;
                holder.itemView.setZ(SCALE_X_Y_with_Z);
                holder.itemView.animate()
                        .setDuration(PopularListView.DURATION_ANIMATE_SCALE_DOWN)
                        .scaleX(SCALE_X_Y_with_Z)
                        .scaleY(SCALE_X_Y_with_Z)
                        .start();
            }
        });
    }

    public void on_key_video(ViewHolder holder, int position) {
        PopularListView popularListView = g_recommendationDialog.findViewById(R.id.lo_recommendation_view_popular_list);
        int dxFirst = get().getResources().getDimensionPixelSize(R.dimen.recommendation_popular_item_scroll_size_start);
        int dxNormal = get().getResources().getDimensionPixelSize(R.dimen.recommendation_popular_item_scroll_size);
        int dxEnd = get().getResources().getDimensionPixelSize(R.dimen.recommendation_popular_item_scroll_size_end);

        holder.itemView.setOnKeyListener((v, keyCode, event) -> {
            if (KeyEvent.ACTION_UP == event.getAction())
                return false;

            if (!popularListView.is_scroll_idle())
                return true;
            if (KeyEvent.KEYCODE_DPAD_RIGHT == keyCode && position == getItemCount() - 1)
                return true;
            if (KeyEvent.KEYCODE_DPAD_LEFT == keyCode && position == 0)
                return true;
            if (KeyEvent.KEYCODE_DPAD_RIGHT == keyCode && position == 2)
                popularListView.smoothScrollBy(dxFirst, 0, null, PopularListView.DURATION_SCROLL);
            if (KeyEvent.KEYCODE_DPAD_RIGHT == keyCode && position > 2)
                popularListView.smoothScrollBy(dxNormal, 0, null, PopularListView.DURATION_SCROLL);
            if (KeyEvent.KEYCODE_DPAD_LEFT == keyCode && position == getItemCount() - 3)
                popularListView.smoothScrollBy(-dxEnd, 0, null, PopularListView.DURATION_SCROLL);
            if (KeyEvent.KEYCODE_DPAD_LEFT == keyCode && position < getItemCount() - 3)
                popularListView.smoothScrollBy(-dxNormal, 0, null, PopularListView.DURATION_SCROLL);

            return false;
        });
    }

    public void on_click_video(ViewHolder holder, int position) {
        holder.itemView.setOnClickListener(v -> {
            String videoId, pkgName, appName;
            RecommendItem recommendItem;

            recommendItem = g_popularData.get(position);
            videoId = recommendItem.get_videoId();
            pkgName = recommendItem.get_package_name();
            appName = recommendItem.get_app_name();

            Log.d(TAG, "on_click_video: [video id] " + videoId + ", [pkgName] " + pkgName + ", [appName] " + appName);
            ActivityUtils.start_by_type(get(), recommendItem);
        });
    }

    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    @Override
    public int getItemCount() {
        return g_popularData.size();
    }

    public ItemDecoration get_item_decoration(RecommendationDialog dialog) {
        return new ItemDecoration(dialog);
    }

    public void set_video_icon(ViewHolder holder, int position) {
        ImageView videoIcon = holder.itemView.findViewById(R.id.lo_recommendation_popular_video_icon);
        if (null == videoIcon)
            return;

        RecommendItem recommendItem = g_popularData.get(position);
        if (null == recommendItem)
            return;

        if (!get().isFinishing() && !get().isDestroyed())
            Glide.with(get())
                    .load(recommendItem.get_poster())
                    .error(R.drawable.internet_error)
                    .placeholder(R.drawable.default_photo)
                    .into(videoIcon);
    }

    public void set_video_label(ViewHolder holder, int position) {
        TextView videoLabel = holder.itemView.findViewById(R.id.lo_recommendation_popular_video_label);
        if (null == videoLabel)
            return;

        RecommendItem recommendItem = g_popularData.get(position);
        if (null == recommendItem)
            return;

        videoLabel.setText(recommendItem.get_title());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class ItemDecoration extends RecyclerView.ItemDecoration {
        WeakReference<AppCompatActivity> g_ref;

        public ItemDecoration(RecommendationDialog dialog) {
            g_ref = new WeakReference<>(dialog.get());
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
            if (null == parent.getAdapter())
                return;

            AppCompatActivity activity = g_ref.get();
            int itemPosition = parent.getChildAdapterPosition(view);
            int itemCount = parent.getAdapter().getItemCount();
            int itemSpaceLeftRight = activity.getResources().getDimensionPixelSize(R.dimen.recommendation_popular_item_decoration_middle_size);
            int itemSpaceStartEnd = g_ref.get().getResources().getDimensionPixelSize(R.dimen.recommendation_popular_item_decoration_start_end_size);

            outRect.left = itemSpaceLeftRight;
            outRect.right = itemSpaceLeftRight;

            if (itemPosition == 0)
                outRect.left = itemSpaceStartEnd;
            if (itemPosition == itemCount - 1)
                outRect.right = itemSpaceStartEnd;
        }
    }
}
