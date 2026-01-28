package com.prime.launcher.Home.LiveTV.QuickTune;

import android.annotation.SuppressLint;
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
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.prime.launcher.Home.LiveTV.LiveTvManager;
import com.prime.launcher.Home.LiveTV.MiniEPG;
import com.prime.launcher.HomeActivity;
import com.prime.launcher.R;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.utils.LogUtils;

import java.lang.ref.WeakReference;
import java.util.List;

public class AddListAdapter extends RecyclerView.Adapter<AddListAdapter.Holder> {

    String TAG = getClass().getSimpleName();

    public static final int MAX_LENGTH_OF_SERVICE_ID    = com.prime.datastructure.utils.Utils.MAX_LENGTH_OF_SERVICE_ID;
    public static final int MAX_LENGTH_OF_CHANNEL_NUM   = com.prime.datastructure.utils.Utils.MAX_LENGTH_OF_CHANNEL_NUM;
    public static final int SCROLL_DURATION             = 100;

    WeakReference<AppCompatActivity> g_ref;
    QuickTuneDialog g_quickDialog;
    List<ProgramInfo> g_channels;

    public AddListAdapter(QuickTuneDialog quickDialog) {
        g_quickDialog = quickDialog;
        g_ref = new WeakReference<>(g_quickDialog.get());
        g_channels = get().g_liveTvMgr.get_channels();
    }

    public AddListAdapter(QuickTuneDialog quickDialog, List<ProgramInfo> channels) {
        g_quickDialog = quickDialog;
        g_ref = new WeakReference<>(g_quickDialog.get());
        g_channels = channels;
    }

    @NonNull
    @Override
    public AddListAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_quick_tune_add_channel, parent, false);
        return new Holder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.init_channel_item(position);
        on_keydown_channel(holder, position);
        on_click_channel(holder, position);
    }

    @Override
    public int getItemCount() {
        return g_channels.size();
    }

    public void on_keydown_channel(Holder holder, int position) {
        holder.itemView.setOnKeyListener((v, keyCode, event) -> {
            AddListView addListView = g_quickDialog.findViewById(R.id.lo_quick_tune_add_list);

            if (KeyEvent.ACTION_UP == event.getAction())    return false;
            if (KeyEvent.KEYCODE_BACK == keyCode)           return show_previous_window(null, true);
            if (!addListView.is_scroll_idle())              return true;
            if (is_last_channel_down(keyCode, position))    return true;
            // scroll down
            if (KeyEvent.KEYCODE_DPAD_DOWN == keyCode &&
                position >= 2) {
                Log.d(TAG, "on_keydown_channel: scroll down");
                addListView.smoothScrollBy(0, get_dy(holder, position), null, AddListView.SCROLL_DURATION);
            }
            // scroll up
            if (KeyEvent.KEYCODE_DPAD_UP == keyCode &&
                position <= getItemCount() - 3) {
                Log.d(TAG, "on_keydown_channel: scroll up");
                addListView.smoothScrollBy(0, -get_dy(holder, position), null, AddListView.SCROLL_DURATION);
            }
            return false;
        });
    }

    public void on_click_channel(Holder holder, int position) {
        holder.itemView.setOnClickListener(v -> {
            ProgramInfo channel = g_channels.get(position);
            show_previous_window(channel, false);
        });
    }

    public boolean is_last_channel_down(int keyCode, int position) {
        return KeyEvent.KEYCODE_DPAD_DOWN == keyCode &&
               position == getItemCount() - 1;
    }

    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    public int get_dy(Holder holder, int position) {
        int dy = holder.itemView.getHeight();

        if (position == 2)
            dy = get().getResources().getDimensionPixelSize(R.dimen.quick_tune_item_first_scroll_size);

        return dy;
    }

    public boolean show_previous_window(ProgramInfo channel, boolean pressBack) {
        if (null != channel)
            Log.d(TAG, "show_previous_window: [channel] " + channel.getDisplayNum(com.prime.datastructure.utils.Utils.MAX_LENGTH_OF_CHANNEL_NUM) + " / " + channel.getDisplayName());

        // block back key for delete channel number
        if (pressBack &&
            g_quickDialog.has_enter_channel_num()) {
            Log.w(TAG, "show_previous_window: block back key for delete channel number");
            return false;
        }

        g_quickDialog.show_previous_window(channel);
        return true;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void update_list_view(List<ProgramInfo> channels) {
        g_channels = channels;
        notifyDataSetChanged();
    }

    public class Holder extends RecyclerView.ViewHolder {

        public Holder(@NonNull View itemView) {
            super(itemView);
        }

        public void init_channel_item(int pos) {
            TextView chNum = itemView.findViewById(R.id.lo_add_channel_item_ch_num);
            TextView chName = itemView.findViewById(R.id.lo_add_channel_item_ch_name);
            ProgramInfo channel = g_channels.get(pos);
            chNum.setText(channel.getDisplayNum(com.prime.datastructure.utils.Utils.MAX_LENGTH_OF_CHANNEL_NUM));
            chName.setText(channel.getDisplayName());
            init_channel_icon(channel);
        }

        public void init_channel_icon(ProgramInfo chInfo) {
            ImageView channelIcon;
            String iconUrl, serviceId;
            int iconResId, iconWidth, iconHeight;

            channelIcon = itemView.findViewById(R.id.lo_add_channel_item_icon);
            serviceId   = chInfo.getServiceId(com.prime.datastructure.utils.Utils.MAX_LENGTH_OF_SERVICE_ID);
            iconUrl     = LiveTvManager.get_channel_icon_url(get(), serviceId);
            iconResId   = LiveTvManager.get_channel_icon_res_id(get(), serviceId);
            iconWidth   = get().getResources().getDimensionPixelSize(R.dimen.zapping_channel_item_icon_width);
            iconHeight  = get().getResources().getDimensionPixelSize(R.dimen.zapping_channel_item_icon_height);

            if (!get().isFinishing() && !get().isDestroyed())
                Glide.with(get())
                        .load(iconUrl)
                        .error(iconResId)
                        .placeholder(iconResId)
                        .override(iconWidth, iconHeight)
                        .priority(Priority.HIGH)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(channelIcon);
        }
    }
}
