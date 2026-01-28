package com.prime.dmg.launcher.Home.LiveTV.Recommendation;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.util.Log;
import android.view.Gravity;
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
import com.prime.dmg.launcher.Home.LiveTV.LiveTvManager;
import com.prime.dmg.launcher.Home.Recommend.Pager.PagerFragment;
import com.prime.dmg.launcher.Home.Recommend.Pager.PagerManager;
import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Utils.JsonParser.AdPageItem;

import java.lang.ref.WeakReference;
import java.util.List;

public class AdListAdapter extends RecyclerView.Adapter<AdListAdapter.ViewHolder> {

    String TAG = getClass().getSimpleName();

    WeakReference<AppCompatActivity> g_ref;
    RecommendationDialog g_recommendationDialog;
    List<AdPageItem> g_adPageItemList;
    boolean g_isPressLeftRight;

    public AdListAdapter(RecommendationDialog recommendationDialog, List<AdPageItem> adPageItemList) {
        g_recommendationDialog = recommendationDialog;
        g_ref = new WeakReference<>(recommendationDialog.get());
        g_adPageItemList = adPageItemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommendation_ad, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        set_ad_poster(holder, position);
        set_ad_channel(holder, position);
        set_ad_title(holder, position);
        set_ad_date_time(holder, position);
        on_focus_ad(holder, position);
        on_click_ad(holder, position);
        on_key_ad(holder, position);
    }

    public void on_focus_ad(ViewHolder holder, int position) {
        AdListView adListView = g_recommendationDialog.findViewById(R.id.lo_recommendation_view_ad_list);
        View focusView = holder.itemView.findViewById(R.id.lo_ad_item_rec_focus);
        int[] location = new int[2];

        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            focusView.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
            if (!hasFocus)
                return;
            if (is_press_left_right())
                return;
            holder.itemView.getLocationOnScreen(location);
            int viewCenterX = location[0] + holder.itemView.getWidth() / 2;
            int screenCenterX = holder.itemView.getResources().getDisplayMetrics().widthPixels / 2;
            int offsetX = viewCenterX - screenCenterX;
            adListView.smoothScrollBy(offsetX, 0);
            if (offsetX < 0)
                Log.d(TAG, "on_focus_ad: move left, [offset] " + offsetX);
            else
                Log.d(TAG, "on_focus_ad: move right, [offset] " + offsetX);
        });
    }

    public void on_click_ad(ViewHolder holder, int position) {
        holder.itemView.setOnClickListener(v -> {
            AdPageItem adPageItem = g_adPageItemList.get(position);
            PagerFragment.open_ad(get(), adPageItem);
            g_recommendationDialog.dismiss();
        });
    }

    public void on_key_ad(ViewHolder holder, int position) {
        AdListView adListView = g_recommendationDialog.findViewById(R.id.lo_recommendation_view_ad_list);
        int dxFirst = get().getResources().getDimensionPixelSize(R.dimen.recommendation_ad_item_scroll_size_first);
        int dxNormal = get().getResources().getDimensionPixelSize(R.dimen.recommendation_ad_item_scroll_size_normal);
        int dxLast = get().getResources().getDimensionPixelSize(R.dimen.recommendation_ad_item_scroll_size_last);

        holder.itemView.setOnKeyListener((v, keyCode, event) -> {
            int dx = 0;
            if (KeyEvent.ACTION_UP == event.getAction()) {
                g_isPressLeftRight = false;
                return false;
            }
            if (!adListView.is_scroll_idle())
                return true;
            if (KeyEvent.KEYCODE_DPAD_RIGHT == keyCode) {
                if (position == getItemCount() - 1) return true;
                if (position == 0)  dx = dxFirst;
                if (position > 0)   dx = dxNormal;
                g_isPressLeftRight = true;
                adListView.smoothScrollBy(dx, 0, null, AdListView.DURATION_SCROLL);
            }
            if (KeyEvent.KEYCODE_DPAD_LEFT == keyCode) {
                if (position == 0) return true;
                if (position == getItemCount() - 1) dx = -dxLast;
                if (position < getItemCount() - 1)  dx = -dxNormal;
                g_isPressLeftRight = true;
                adListView.smoothScrollBy(dx, 0, null, AdListView.DURATION_SCROLL);
            }
            return false;
        });
    }

    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    @Override
    public int getItemCount() {
        return g_adPageItemList.size();
    }

    public ItemDecoration get_item_decoration(RecommendationDialog dialog) {
        return new ItemDecoration(dialog);
    }

    public void set_ad_poster(ViewHolder holder, int position) {
        ImageView adPoster = holder.itemView.findViewById(R.id.lo_ad_item_program_poster);
        if (null == adPoster)
            return;

        AdPageItem adPageItem = g_adPageItemList.get(position);
        if (null == adPageItem)
            return;

        if (!get().isFinishing() && !get().isDestroyed())
            Glide.with(get())
                    .load(adPageItem.get_poster_art_url())
                    .error(R.drawable.internet_error)
                    .placeholder(R.drawable.default_photo)
                    .centerCrop()
                    .into(adPoster);
    }

    @SuppressLint("SetTextI18n")
    public void set_ad_channel(ViewHolder holder, int position) {
        TextView channelText = holder.itemView.findViewById(R.id.lo_ad_item_channel_name_text);
        AdPageItem adPageItem = g_adPageItemList.get(position);
        String chNum = adPageItem.get_channel_num();
        String chName = adPageItem.get_channel_name();

        if (is_ad_full(position)) {
            hide_channel_view(holder);
        }
        else {
            channelText.setText(chNum + " " + chName);
            set_ad_channel_icon(holder, position);
        }
    }

    public void set_ad_channel_icon(ViewHolder holder, int position) {
        ImageView channelIcon = holder.itemView.findViewById(R.id.lo_ad_item_channel_icon);
        AdPageItem adPageItem = g_adPageItemList.get(position);
        String serviceId = adPageItem.get_service_id();
        String iconUrl = LiveTvManager.get_channel_icon_url(get(), serviceId);
        int iconResId = LiveTvManager.get_channel_icon_res_id(get(), serviceId);

        if (!get().isFinishing() && !get().isDestroyed())
            Glide.with(get())
                    .load(iconUrl)
                    .error(iconResId)
                    .placeholder(iconResId)
                    .into(channelIcon);
    }

    public void set_ad_title(ViewHolder holder, int position) {
        TextView titleView = holder.itemView.findViewById(R.id.lo_ad_item_program_name_text);
        AdPageItem adPageItem = g_adPageItemList.get(position);
        String title = adPageItem.get_title();

        if (is_ad_full(position)) {
            hide_date_view(holder);
            titleView.setGravity(Gravity.CENTER|Gravity.START);
        }
        titleView.setText(title);
    }

    public void set_ad_date_time(ViewHolder holder, int position) {
        TextView dateView = holder.itemView.findViewById(R.id.lo_ad_item_program_time_text);
        AdPageItem adPageItem = g_adPageItemList.get(position);

        if (is_ad_horizontal(position) || is_ad_vertical(position)) {
            String dateTime = adPageItem.get_start_time();
            String endTime = adPageItem.get_end_time();
            int beginIndex = endTime.indexOf(":");
            if (beginIndex != -1) {
                beginIndex = beginIndex - 2;
                endTime = endTime.substring(beginIndex);
            }
            dateTime += "-" + endTime;
            dateView.setText(dateTime);
        }
    }

    public boolean is_ad_full(int position) {
        String adType = g_adPageItemList.get(position).get_type();
        return PagerManager.PAGE_TYPE_FULL.equals(adType);
    }

    public boolean is_ad_horizontal(int position) {
        String adType = g_adPageItemList.get(position).get_type();
        return PagerManager.PAGE_TYPE_HORIZONTAL.equals(adType);
    }

    public boolean is_ad_vertical(int position) {
        String adType = g_adPageItemList.get(position).get_type();
        return PagerManager.PAGE_TYPE_VERTICAL.equals(adType);
    }

    public boolean is_press_left_right() {
        return g_isPressLeftRight;
    }

    public void hide_channel_view(ViewHolder holder) {
        View channelView = holder.itemView.findViewById(R.id.lo_ad_item_title_layer);
        View divider = holder.itemView.findViewById(R.id.lo_divider);
        channelView.setVisibility(View.GONE);
        divider.setVisibility(View.GONE);
    }

    public void hide_date_view(ViewHolder holder) {
        View timeView = holder.itemView.findViewById(R.id.lo_ad_item_program_time_text);
        timeView.setVisibility(View.GONE);
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
