package com.prime.dmg.launcher.Home.LiveTV.Zapping;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.prime.dmg.launcher.R;
import com.prime.dtv.sysdata.ProgramInfo;

import java.util.List;

public class ZappingAdapter extends RecyclerView.Adapter<ZappingAdapter.ViewHolder> {

    String TAG = ZappingAdapter.class.getSimpleName();

    public static final int MAX_LENGTH_OF_SERVICE_ID    = MiniEPG.MAX_LENGTH_OF_SERVICE_ID;
    public static final int MAX_LENGTH_OF_CHANNEL_NUM   = MiniEPG.MAX_LENGTH_OF_CHANNEL_NUM;

    ZappingDialog     g_zappingDialog;
    ZappingListView   g_zappingListView;
    List<ProgramInfo> g_zappingList;
    int               gKeyCode;

    public ZappingAdapter(ZappingDialog dialog, List<ProgramInfo> zappingList){
        g_zappingDialog   = dialog;
        g_zappingList     = zappingList;
        g_zappingListView = dialog.findViewById(R.id.lo_zapping_list);
        setHasStableIds(true);
    }

    /** @noinspection ClassEscapesDefinedScope*/
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.live_tv_zapping_view_item, parent, false));
    }

    /** @noinspection ClassEscapesDefinedScope*/
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (ZappingDialog.ZappingList.is_updating()) {
            Log.w(TAG, "onBindViewHolder: interrupt binding view holder");
            return;
        }
        holder.init_channel_item(position);
        holder.init_callback(position);
    }

    @Override
    public long getItemId(int position) {
        return g_zappingList.get(position).getChannelId();
    }

    @Override
    public int getItemCount() {
        return g_zappingList.size();
    }

    public AppCompatActivity get() {
        return g_zappingDialog.get();
    }

    public void set_zapping_list(List<ProgramInfo> zappingList) {
        g_zappingList = zappingList;
    }

    /** @noinspection CommentedOutCode*/
    @SuppressLint("DiscouragedApi")
    class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void init_channel_item(int pos) {
            TextView    chNum   = itemView.findViewById(R.id.lo_zapping_channel_item_ch_num);
            TextView    chName  = itemView.findViewById(R.id.lo_zapping_channel_item_ch_name);
            ProgramInfo channel = g_zappingList.get(pos);

            chNum.setText(channel.getDisplayNum(MAX_LENGTH_OF_CHANNEL_NUM));
            chName.setText(channel.getDisplayName());
            init_channel_icon(channel);
        }

        public void init_channel_icon(ProgramInfo chInfo) {
            ImageView channelIcon;
            String iconUrl, serviceId;
            int iconResId, iconWidth, iconHeight;

            channelIcon = itemView.findViewById(R.id.lo_zapping_channel_item_icon);
            serviceId   = chInfo.getServiceId(MAX_LENGTH_OF_SERVICE_ID);
            iconUrl     = LiveTvManager.get_channel_icon_url(get(), serviceId);
            iconResId   = LiveTvManager.get_channel_icon_res_id(get(), serviceId);
            iconWidth   = get().getResources().getDimensionPixelSize(R.dimen.zapping_channel_item_icon_width);
            iconHeight  = get().getResources().getDimensionPixelSize(R.dimen.zapping_channel_item_icon_height);

            if (chInfo.getType() == ProgramInfo.PROGRAM_RADIO)
                iconResId = R.mipmap.ch300;

            if (get() != null && !get().isFinishing() && !get().isDestroyed()) {
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

        public void init_callback(int pos) {
            ViewGroup chItem = itemView.findViewById(R.id.lo_zapping_channel_item);

            chItem.setOnClickListener(v ->
                    on_click_channel(v, pos));
            chItem.setOnKeyListener((v, keyCode, event) ->
                    on_key_channel(v, keyCode, event, pos));
            chItem.setOnFocusChangeListener((v, hasFocus) ->
                    on_focus_channel(v, hasFocus, pos));
        }

        private void on_focus_channel(View view, boolean hasFocus, int pos) {
            if (hasFocus)
                g_zappingListView.move_to_middle(view, pos, gKeyCode);
        }

        public void on_click_channel(View itemView, int position) {
            // change channel
            g_zappingDialog.change_channel(itemView, position, true);
        }

        public boolean on_key_channel(View v, int keyCode, KeyEvent event, int position) {
            ZappingListView zappingListView = g_zappingDialog.findViewById(R.id.lo_zapping_list);
            int             dy              = itemView.getHeight();

            if (KeyEvent.ACTION_UP == event.getAction())
                return false;

            gKeyCode = keyCode;

            /*
            if (KeyEvent.KEYCODE_DPAD_DOWN == keyCode &&
                position > 2)
                zappingListView.smoothScrollBy(0, dy, null, ZappingListView.SCROLL_DURATION);

            if (KeyEvent.KEYCODE_DPAD_UP == keyCode &&
                position < zappingListView.get_count() - 3)
                zappingListView.smoothScrollBy(0, -dy, null, ZappingListView.SCROLL_DURATION);
            */

            return false;
        }
    }
}
