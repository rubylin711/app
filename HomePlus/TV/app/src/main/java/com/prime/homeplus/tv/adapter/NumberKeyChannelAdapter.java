package com.prime.homeplus.tv.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.tvprovider.media.tv.Channel;

import com.prime.homeplus.tv.R;
import com.prime.homeplus.tv.manager.LockManager;
import com.prime.homeplus.tv.utils.ChannelUtils;
import com.prime.homeplus.tv.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class NumberKeyChannelAdapter extends RecyclerView.Adapter<NumberKeyChannelAdapter.ViewHolder> {
    private static final String TAG = "NumberKeyChannelAdapter";
    private RecyclerView recyclerView;
    private List<Channel> channelList;
    private OnItemFocusedListener channelFocusedListener;
    private OnItemClickListener channelClickListener;

    private Context context;

    public NumberKeyChannelAdapter(RecyclerView recyclerView, List<Channel> channelList) {
        this.recyclerView = recyclerView;
        this.context = recyclerView.getContext();
        this.channelList = new ArrayList<>();
        if (channelList != null) {
            this.channelList.addAll(channelList);
        }
    }

    public interface OnItemFocusedListener {
        void onItemFocused();
    }

    public interface OnItemClickListener {
        void onChannelClick(Channel channel);
    }

    public void setOnItemFocusedListener(OnItemFocusedListener listener) {
        this.channelFocusedListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.channelClickListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llNumberKeyChannel;
        TextView tvNumberKeyChannelNum, tvNumberKeyChannelName;
        ImageView ivNumberKeyChannelIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            llNumberKeyChannel = itemView.findViewById(R.id.llNumberKeyChannel);
            tvNumberKeyChannelNum = itemView.findViewById(R.id.tvNumberKeyChannelNum);
            tvNumberKeyChannelName = itemView.findViewById(R.id.tvNumberKeyChannelName);
            ivNumberKeyChannelIcon = itemView.findViewById(R.id.ivNumberKeyChannelIcon);
        }
    }

    @NonNull
    @Override
    public NumberKeyChannelAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_number_key_channel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NumberKeyChannelAdapter.ViewHolder holder, int position) {
        //Log.d(TAG, "onBindViewHolder position:" + position);
        Channel channel = channelList.get(position);
        holder.tvNumberKeyChannelNum.setText(StringUtils.padToNDigits(channel.getDisplayNumber(), 3));
        holder.tvNumberKeyChannelName.setText(channel.getDisplayName());

        int imageResId = 0;
        LockManager lockManager = new LockManager(context.getApplicationContext());
        int lock_flag = lockManager.getHighestPriorityLockFlag(channel, null, null);
        if ((lock_flag == LockManager.LOCK_ADULT_CHANNEL) || lock_flag == LockManager.LOCK_PARENTAL_CHANNEL) {
            imageResId = R.drawable.state_ch_lock;
        } else if ("SERVICE_TYPE_AUDIO".equals(channel.getServiceType())) {
            imageResId = R.drawable.state_ch_music;
        } else if (ChannelUtils.isChannelFavorite(context,channel)) { // favorite
            // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
            imageResId = R.drawable.state_ch_favorite;
        }
        holder.ivNumberKeyChannelIcon.setImageResource(imageResId);
        holder.ivNumberKeyChannelIcon.setVisibility(imageResId != 0 ? View.VISIBLE : View.INVISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (channelClickListener != null) {
                channelClickListener.onChannelClick(channel);
            }
        });

        holder.itemView.setOnKeyListener((v, keyCode, event) -> {
            //Log.d(TAG, "onBindViewHolder setOnKeyListener:" + position);
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            int itemCount = getItemCount();

            // Loop Navigation
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP && position == 0) {
                recyclerView.scrollToPosition(itemCount - 1);
                recyclerView.postDelayed(() -> {
                    View lastView = recyclerView.getLayoutManager().findViewByPosition(itemCount - 1);
                    if (lastView != null) lastView.requestFocus();
                }, 100);
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && position == channelList.size() - 1) {
                recyclerView.scrollToPosition(0);
                recyclerView.postDelayed(() -> {
                    View firstView = recyclerView.getLayoutManager().findViewByPosition(0);
                    if (firstView != null) firstView.requestFocus();
                }, 100);
                return true;
            }
            return false;
        });

        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && channelFocusedListener != null) {
                channelFocusedListener.onItemFocused();
            }

            if (hasFocus) {
                holder.llNumberKeyChannel.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTabSelect));
                holder.tvNumberKeyChannelNum.setTextColor(ContextCompat.getColor(context, R.color.white));
                holder.tvNumberKeyChannelNum.setTypeface(null, Typeface.BOLD);
                holder.tvNumberKeyChannelName.setTextColor(ContextCompat.getColor(context, R.color.white));
                holder.tvNumberKeyChannelName.setTypeface(null, Typeface.BOLD);
            } else {
                holder.llNumberKeyChannel.setBackgroundColor(Color.TRANSPARENT);
                holder.tvNumberKeyChannelNum.setTextColor(ContextCompat.getColor(context, R.color.colorGold2));
                holder.tvNumberKeyChannelNum.setTypeface(null, Typeface.NORMAL);
                holder.tvNumberKeyChannelName.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteOpacity70));
                holder.tvNumberKeyChannelName.setTypeface(null, Typeface.NORMAL);
            }
        });
    }

    @Override
    public int getItemCount() {
        return channelList != null ? channelList.size() : 0;
    }

    public void updateList(List<Channel> newList) {
        this.channelList.clear();
        this.channelList.addAll(newList);
        notifyDataSetChanged();

        // reset focus
        if (!channelList.isEmpty()) {
            recyclerView.scrollToPosition(0);
            recyclerView.postDelayed(() -> {
                View firstItem = recyclerView.getLayoutManager().findViewByPosition(0);
                if (firstItem != null) {
                    firstItem.requestFocus();
                }
            }, 100);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.GONE);
        }
    }

    public void clearChannels() {
        channelList.clear();
        this.channelList.addAll(new ArrayList<>());
        notifyDataSetChanged();
        recyclerView.setVisibility(View.GONE);
    }
}

