package com.prime.dmg.launcher.Home.LiveTV.Rank;

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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.prime.dmg.launcher.Home.LiveTV.LiveTvManager;
import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Utils.JsonParser.RankInfo;
import com.prime.dtv.sysdata.ProgramInfo;

import java.lang.ref.WeakReference;
import java.util.List;

public class RankAdapter extends RecyclerView.Adapter<RankAdapter.ViewHolder> {

    String TAG = getClass().getSimpleName();

    WeakReference<AppCompatActivity> g_ref;
    RankListDialog g_rankDialog;
    List<RankInfo> g_rankInfos;
    List<ProgramInfo> g_channels;
    int g_keyCode;

    public RankAdapter(RankListDialog rankDialog, List<RankInfo> rankInfos) {
        g_ref = new WeakReference<>(rankDialog.get());
        g_rankDialog = rankDialog;
        g_rankInfos = rankInfos;
        g_channels = get().g_liveTvMgr.get_channels();
    }

    @NonNull
    @Override
    public RankAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_live_tv_rank, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RankAdapter.ViewHolder holder, int position) {
        holder.on_focus_item(position);
        holder.on_key_item(position);
        holder.on_click_item(position);
        holder.set_layout_params(position);
        holder.set_rank_info(position);
    }

    @Override
    public int getItemCount() {
        return g_rankInfos.size();
    }

    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void on_focus_item(int position) {
            View selectBar = itemView.findViewById(R.id.lo_rank_item_select_bar);
            itemView.setOnFocusChangeListener((v, hasFocus) -> {
                selectBar.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);

                if (hasFocus && KeyEvent.KEYCODE_DPAD_RIGHT == g_keyCode) {
                    if (position == 1)  scroll_by(get().getResources().getDimensionPixelSize(R.dimen.rank_item_scroll_first_dx));
                    if (position > 1)   scroll_by(get().getResources().getDimensionPixelSize(R.dimen.rank_item_scroll_normal_dx));
                    Log.d(TAG, "on_focus_item: scroll to right");
                }
                if (hasFocus && KeyEvent.KEYCODE_DPAD_LEFT == g_keyCode) {
                    if (position == getItemCount() - 2) scroll_by(-get().getResources().getDimensionPixelSize(R.dimen.rank_item_scroll_last_dx));
                    if (position < getItemCount() - 2)  scroll_by(-get().getResources().getDimensionPixelSize(R.dimen.rank_item_scroll_normal_dx));
                    Log.d(TAG, "on_focus_item: scroll to left");
                }
            });
        }

        public void on_key_item(int position) {
            itemView.setOnKeyListener((v, keyCode, event) -> {
                if (KeyEvent.ACTION_UP == event.getAction())
                    return false;
                g_keyCode = keyCode;
                return false;
            });
        }

        public void on_click_item(int position) {
            itemView.setOnClickListener(v -> {
                ProgramInfo channel = get_channel(g_rankInfos.get(position));
                if (channel == null) {
                    Log.w(TAG, "on_click_item: null channel");
                    return;
                }
                LiveTvManager.change_channel(get().g_liveTvMgr, channel, true);
                g_rankDialog.dismiss();
            });
        }

        public void set_layout_params(int pos) {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            int start30dp = get().getResources().getDimensionPixelSize(R.dimen.rank_item_margin_start_end);
            int start8dp = get().getResources().getDimensionPixelSize(R.dimen.rank_item_margin_start2);
            int bottom30dp = get().getResources().getDimensionPixelSize(R.dimen.rank_item_margin_bottom);
            int end30dp = get().getResources().getDimensionPixelSize(R.dimen.rank_item_padding_end);

            params.rightMargin = start8dp;
            params.bottomMargin = bottom30dp;
            itemView.setLayoutParams(params);

            if (pos == 0)
                itemView.setPadding(start30dp, 0, 0, 0);
            if (pos == getItemCount() - 1)
                itemView.setPadding(0, 0, end30dp, 0);
        }

        @SuppressLint("SetTextI18n")
        public void set_rank_info(int pos) {
            TextView rankNum = itemView.findViewById(R.id.lo_rank_item_hint_num);
            TextView rankRatio = itemView.findViewById(R.id.lo_rank_item_ratio);
            TextView rankProgram = itemView.findViewById(R.id.lo_rank_item_program_text);
            RankInfo rankInfo = g_rankInfos.get(pos);
            ProgramInfo channel = get_channel(rankInfo);

            rankNum.setText(String.valueOf(rankInfo.get_ranking()));
            rankRatio.setText(get().getString(R.string.rank_ratio2) + " " + rankInfo.get_rating() + "%");
            rankProgram.setText(rankInfo.get_tv_name());
            set_channel_text(channel, rankInfo);
            set_channel_icon(rankInfo);
        }

        @SuppressLint("SetTextI18n")
        public void set_channel_text(ProgramInfo channel, RankInfo rankInfo) {
            TextView rankChannel = itemView.findViewById(R.id.lo_rank_item_channel_text);

            if (channel == null)
                rankChannel.setText(rankInfo.get_channel_name());
            else
                rankChannel.setText(channel.getDisplayNum(3) + " " + rankInfo.get_channel_name());
        }

        public void set_channel_icon(RankInfo rankInfo) {
            ImageView channelIcon = itemView.findViewById(R.id.lo_rank_item_channel_icon);
            int iconResId = LiveTvManager.get_channel_icon_res_id(get(), String.valueOf(rankInfo.get_service_id()));

            if (!get().isFinishing() && !get().isDestroyed())
                Glide.with(get())
                        .load(rankInfo.get_channel_poster())
                        .error(iconResId)
                        .placeholder(iconResId)
                        //.override(iconWidth, iconHeight)
                        //.skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(channelIcon);
        }

        public ProgramInfo get_channel(RankInfo rankInfo) {
            ProgramInfo channel = null;
            for (ProgramInfo ch : g_channels) {
                //String channelName = ch.getDisplayName().replace(" ", "").toUpperCase();
                //String rankName = rankInfo.get_channel_name().replace(" ", "").toUpperCase();
                //if (channelName.contains(rankName)) {
                if (ch.getServiceId() == rankInfo.get_service_id()) {
                    channel = ch;
                    break;
                }
            }
            return channel;
        }

        public void scroll_by(int dx) {
            RankListView rankListView = g_rankDialog.findViewById(R.id.lo_live_tv_rank_list);
            rankListView.smoothScrollBy(dx, 0, null, RankListView.DURATION_SCROLL);
        }
    }
}
