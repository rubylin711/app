package com.prime.dmg.launcher.Home.LiveTV.QuickTune;

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
import com.prime.dmg.launcher.Home.LiveTV.LiveTvManager;
import com.prime.dmg.launcher.Home.LiveTV.MiniEPG;
import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.R;
import com.prime.dtv.sysdata.MiscDefine;
import com.prime.dtv.sysdata.ProgramInfo;

import java.lang.ref.WeakReference;
import java.util.List;

public class FavListAdapter extends RecyclerView.Adapter<FavListAdapter.Holder> {

    String TAG = getClass().getSimpleName();

    public static final int MAX_LENGTH_OF_SERVICE_ID    = MiniEPG.MAX_LENGTH_OF_SERVICE_ID;
    public static final int MAX_LENGTH_OF_CHANNEL_NUM   = MiniEPG.MAX_LENGTH_OF_CHANNEL_NUM;

    WeakReference<AppCompatActivity> g_ref;
    QuickTuneDialog g_quickDialog;
    List<ProgramInfo> g_favorites;

    public FavListAdapter(QuickTuneDialog quickDialog) {
        g_quickDialog = quickDialog;
        g_ref = new WeakReference<>(g_quickDialog.get());
        g_favorites = get().g_dtv.get_program_info_list(
                g_quickDialog.get_group_type(),
                MiscDefine.ProgramInfo.POS_ALL,
                MiscDefine.ProgramInfo.POS_ALL);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_quick_tune_add_channel, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.init_channel_item(position);
        on_keydown_channel(holder, position);
        on_click_channel(holder, position);
    }

    public void on_keydown_channel(Holder holder, int position) {
        holder.itemView.setOnKeyListener((v, keyCode, event) -> {
            if (KeyEvent.ACTION_UP == event.getAction())    return false;
            if (!is_allow_keycode(event, position))         return true;
            if (is_keycode_up_down(keyCode))                g_quickDialog.set_delay_dismiss();
            if (show_add_window(keyCode))                   { Log.d(TAG, "on_keydown_channel: show add window");         return false; }
            if (delete_favorite_channel(keyCode, position)) { Log.d(TAG, "on_keydown_channel: delete favorite channel"); return true;  }
            if (scroll_down(holder, keyCode, position))     { Log.d(TAG, "on_keydown_channel: scroll down");             return false; }
            if (scroll_up(holder, keyCode, position))       { Log.d(TAG, "on_keydown_channel: scroll up");               return false; }
            return false;
        });
    }

    boolean is_allow_keycode(KeyEvent event, int position) {
        FavListView favListView = g_quickDialog.findViewById(R.id.lo_quick_tune_favorite_channels);
        if (!favListView.is_scroll_idle())
            return false;
        if (is_keycode_down_at_last_channel(event.getKeyCode(), position))
            return false;
        return true;
    }

    public void on_click_channel(Holder holder, int position) {
        holder.itemView.setOnClickListener(v -> {
            ProgramInfo channel = g_favorites.get(position);
            String chNum = channel.getDisplayNum(MAX_LENGTH_OF_CHANNEL_NUM);
            String chName = channel.getDisplayName();

            Log.d(TAG, "on_click_channel: [Num] " + chNum + ", [Name] " + chName);
            LiveTvManager.change_channel(get().g_liveTvMgr, channel, true);
            g_quickDialog.dismiss();
        });
    }

    public boolean is_keycode_down_at_last_channel(int keyCode, int position) {
        return KeyEvent.KEYCODE_DPAD_DOWN == keyCode &&
                position == getItemCount() - 1;
    }

    boolean is_keycode_up_down(int keyCode) {
        return KeyEvent.KEYCODE_DPAD_UP == keyCode || KeyEvent.KEYCODE_DPAD_DOWN == keyCode;
    }

    HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    @Override
    public int getItemCount() {
        return g_favorites.size();
    }

    public int get_dy(Holder holder, int position, int keyCode) {
        int dy = holder.itemView.getHeight();

        if (KeyEvent.KEYCODE_DPAD_DOWN == keyCode &&
            position == 2)
                dy = get().getResources().getDimensionPixelSize(R.dimen.quick_tune_favorite_item_first_scroll_size);

        if (KeyEvent.KEYCODE_DPAD_UP == keyCode &&
            position == getItemCount() - 3)
                dy = get().getResources().getDimensionPixelSize(R.dimen.quick_tune_favorite_item_first_scroll_size);

        return dy;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void update_list_view(List<ProgramInfo> channels) {
        g_favorites = channels;
        notifyDataSetChanged();
    }

    boolean show_add_window(int keyCode) {
        if (KeyEvent.KEYCODE_PROG_YELLOW == keyCode) {
            Log.d(TAG, "show_add_window: by press yellow");
            g_quickDialog.show_add_window();
            return true;
        }
        else
            Log.w(TAG, "show_add_window: not match [keycode] " + keyCode);
        return false;
    }

    boolean delete_favorite_channel(int keyCode, int position) {
        if (KeyEvent.KEYCODE_PROG_BLUE == keyCode) {
            g_quickDialog.delete_favorite_channel(g_favorites.get(position));
            return true;
        }
        return false;
    }

    boolean scroll_down(Holder holder, int keyCode, int position) {
        if (KeyEvent.KEYCODE_DPAD_DOWN == keyCode &&
            position >= 2) {
            FavListView favListView = g_quickDialog.findViewById(R.id.lo_quick_tune_favorite_channels);
            favListView.smoothScrollBy(0, get_dy(holder, position, keyCode), null, FavListView.SCROLL_DURATION);
            return true;
        }
        return false;
    }

    boolean scroll_up(Holder holder, int keyCode, int position) {
        if (KeyEvent.KEYCODE_DPAD_UP == keyCode &&
            position <= getItemCount() - 3) {
            FavListView favListView = g_quickDialog.findViewById(R.id.lo_quick_tune_favorite_channels);
            favListView.smoothScrollBy(0, -get_dy(holder, position, keyCode), null, FavListView.SCROLL_DURATION);
            return true;
        }
        return false;
    }

    public class Holder extends RecyclerView.ViewHolder {

        public Holder(@NonNull View itemView) {
            super(itemView);
        }

        public void init_channel_item(int pos) {
            TextView chNum = itemView.findViewById(R.id.lo_add_channel_item_ch_num);
            TextView chName = itemView.findViewById(R.id.lo_add_channel_item_ch_name);
            ProgramInfo favChannel = g_favorites.get(pos);
            chNum.setText(favChannel.getDisplayNum(MAX_LENGTH_OF_CHANNEL_NUM));
            chName.setText(favChannel.getDisplayName());
            init_channel_icon(favChannel);
        }

        public void init_channel_icon(ProgramInfo favChannel) {
            ImageView channelIcon;
            String iconUrl, serviceId;
            int iconResId, iconWidth, iconHeight;

            channelIcon = itemView.findViewById(R.id.lo_add_channel_item_icon);
            serviceId   = favChannel.getServiceId(MAX_LENGTH_OF_SERVICE_ID);
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
