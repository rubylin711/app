package com.prime.homeplus.tv.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.tvprovider.media.tv.Channel;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.prime.homeplus.tv.R;
import com.prime.homeplus.tv.utils.PrimeUtils;
import com.prime.homeplus.tv.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChannelSearchAdapter extends RecyclerView.Adapter<ChannelSearchAdapter.ViewHolder> {
    private static final String TAG = "ChannelSearchAdapter";
    private RecyclerView recyclerView;
    private List<Channel> searchChannelResultList;
    private OnItemClickListener clickListener;
    private Context context;
    private int[] colorArray;

    public ChannelSearchAdapter(RecyclerView recyclerView, List<Channel> channelList) {
        this.recyclerView = recyclerView;
        this.context = recyclerView.getContext();
        this.searchChannelResultList = new ArrayList<>();
        if (channelList != null) {
            this.searchChannelResultList.addAll(channelList);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Channel ch);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llChannelSearchResult;
        ImageView ivChannelSearchResult;
        TextView tvChannelSearchResultNumber, tvChannelSearchResultName;

        public ViewHolder(View itemView) {
            super(itemView);
            llChannelSearchResult = itemView.findViewById(R.id.llChannelSearchResult);
            ivChannelSearchResult = itemView.findViewById(R.id.ivChannelSearchResult);
            tvChannelSearchResultNumber = itemView.findViewById(R.id.tvChannelSearchResultNumber);
            tvChannelSearchResultName = itemView.findViewById(R.id.tvChannelSearchResultName);
        }
    }

    @NonNull
    @Override
    public ChannelSearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_channel_search_result, parent, false);
        colorArray = parent.getContext().getResources().getIntArray(R.array.channel_search_color);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelSearchAdapter.ViewHolder holder, int position) {
        //Log.d(TAG, "onBindViewHolder position:" + position);
        Channel ch = searchChannelResultList.get(position);
        loadChannelIcon(holder.llChannelSearchResult, ch.getDisplayNumber());
        holder.tvChannelSearchResultNumber.setText(ch.getDisplayNumber());
        holder.tvChannelSearchResultName.setText(ch.getDisplayName());

        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            Log.d(TAG, "onBindViewHolder setOnFocusChangeListener hasFocus:" + hasFocus + ", ch:" + ch.getDisplayNumber());
            holder.tvChannelSearchResultName.setSelected(hasFocus);
            if (hasFocus) {
                v.animate().scaleX(1.2f).scaleY(1.2f)
                        .setDuration(150).start();
                v.setElevation(10.0f);
            } else {
                v.animate().scaleX(1.0f).scaleY(1.0f)
                        .setDuration(150).start();
                v.setElevation(0.0f);
            }
        });

        holder.itemView.setOnClickListener((v) -> {
            Log.d(TAG, "onBindViewHolder setOnClickListener ch:" + ch.getDisplayNumber());
            if (clickListener != null) {
                clickListener.onItemClick(ch);
            }
        });

        holder.itemView.setOnKeyListener((v, keyCode, event) -> {
            Log.d(TAG, "onBindViewHolder setOnKeyListener position:" + position +
                    ", action:" + event.getAction() + ", keyCode:" + keyCode);
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                moveFocus(v, position, -1);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                moveFocus(v, position, 1);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                recyclerView.setVisibility(View.GONE);
                return true;
            }

            return false;
        });
    }

    @Override
    public int getItemCount() {
        return searchChannelResultList != null ? searchChannelResultList.size() : 0;
    }

    public void updateList(List<Channel> newList) {
        this.searchChannelResultList.clear();
        this.searchChannelResultList.addAll(newList);
        notifyDataSetChanged();

        if (!searchChannelResultList.isEmpty()) {
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void loadChannelIcon(LinearLayout ll, String channelNumber) {
        int min = 0;
        int max = colorArray.length - 1;
        int random = new Random().nextInt((max - min) + 1) + min;

        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        String soId = PrimeUtils.getSoId(context); //default value
        String num = StringUtils.normalizeInputNumber(channelNumber);

        String img = "https://cnsatv.totaltv.com.tw:8093/channel-banner/SO" + soId + "/" + soId + "_" + num + ".png";
        Log.d(TAG, "img= " + img);
        ll.setBackgroundColor(colorArray[random]);

        Glide.with(context).load(img).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                ll.setBackground(resource);
            }
        });
    }

    private void moveFocus(View v, int currentPos, int offset) {
        if (currentPos == RecyclerView.NO_POSITION) return;
        int targetPos = currentPos + offset;
        if ((targetPos >= 0) && (targetPos < getItemCount())) {
            RecyclerView rv = (RecyclerView) v.getParent();
            RecyclerView.ViewHolder targetHolder = rv.findViewHolderForAdapterPosition(targetPos);
            if (targetHolder != null) {
                targetHolder.itemView.requestFocus();
            } else {
                rv.smoothScrollToPosition(targetPos);
                rv.postDelayed(() -> {
                    RecyclerView.ViewHolder vh = rv.findViewHolderForAdapterPosition(targetPos);
                    if (vh != null) {
                        vh.itemView.requestFocus();
                    }
                }, 100);
            }
        }
    }
}

