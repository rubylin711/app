package com.prime.homeplus.tv.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.tvprovider.media.tv.Channel;

import com.prime.homeplus.tv.R;

import java.util.List;

public class PvrManualChannelListAdapter extends RecyclerView.Adapter<PvrManualChannelListAdapter.ViewHolder> {
    private static final String TAG = "PvrManualChannelListAdapter";

    private RecyclerView recyclerView;
    private OnRecyclerViewInteractionListener interactionListener;

    private Context context;
    private List<Channel> channelList;
    private int spanCount = 0;
    private int lastChannelIndex = -1;
    private int currentChannelIndex = -1;

    public PvrManualChannelListAdapter(RecyclerView recyclerView, List<Channel> channelList, int spanCount) {
        Log.d(TAG, "PvrManualChannelListAdapter");
        this.recyclerView = recyclerView;
        this.context = recyclerView.getContext();
        this.channelList = channelList;;
        this.spanCount = spanCount;
    }

    public interface OnRecyclerViewInteractionListener {
        void onClick(Channel ch);
        void onJumpToGenreList();
        void onJumpToCancel();
        void onKeyEventReceived(KeyEvent event);
    }

    public void setOnRecyclerViewInteractionListener(OnRecyclerViewInteractionListener listener) {
        this.interactionListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llPvrManualChannelList;
        RadioButton rbPvrManualChannelList;
        TextView tvPvrManualChannelListChannelNumber, tvPvrManualChannelListChannelName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "ViewHolder");
            llPvrManualChannelList = itemView.findViewById(R.id.llPvrManualChannelList);
            rbPvrManualChannelList = itemView.findViewById(R.id.rbPvrManualChannelList);
            tvPvrManualChannelListChannelNumber = itemView.findViewById(R.id.tvPvrManualChannelListChannelNumber);
            tvPvrManualChannelListChannelName = itemView.findViewById(R.id.tvPvrManualChannelListChannelName);
        }

        public void applyFocusStyle() {
            llPvrManualChannelList.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTabSelect));
            rbPvrManualChannelList.setButtonTintList(ColorStateList.valueOf(context.getColor(R.color.white)));
            tvPvrManualChannelListChannelNumber.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvPvrManualChannelListChannelName.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvPvrManualChannelListChannelName.setTypeface(null, Typeface.BOLD);
        }

        public void applyNormalStyle() {
            llPvrManualChannelList.setBackgroundColor(Color.TRANSPARENT);
            rbPvrManualChannelList.setButtonTintList(ColorStateList.valueOf(context.getColor(R.color.colorWhiteOpacity50)));
            tvPvrManualChannelListChannelNumber.setTextColor(ContextCompat.getColor(context, R.color.colorGold2));
            tvPvrManualChannelListChannelName.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteOpacity70));
            tvPvrManualChannelListChannelName.setTypeface(null, Typeface.NORMAL);
        }
    }

    @NonNull
    @Override
    public PvrManualChannelListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pvr_manual_channel_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PvrManualChannelListAdapter.ViewHolder holder, int position) {
        Channel ch = channelList.get(position);
        Log.d(TAG, "onBindViewHolder position:" + position);

        if (position == currentChannelIndex) {
            holder.rbPvrManualChannelList.setButtonDrawable(R.drawable.layer_epg_genre_radio_btn_checked);
        }
        holder.tvPvrManualChannelListChannelNumber.setText(ch.getDisplayNumber());
        holder.tvPvrManualChannelListChannelName.setText(ch.getDisplayName());

        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "onBindViewHolder click position:" + position);
            if (interactionListener != null) {
                lastChannelIndex = currentChannelIndex;
                currentChannelIndex = position;

                if (lastChannelIndex != -1 && lastChannelIndex < getItemCount()) {
                    PvrManualChannelListAdapter.ViewHolder viewHolderAtPosition = (PvrManualChannelListAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(lastChannelIndex);
                    if (viewHolderAtPosition != null) {
                        viewHolderAtPosition.rbPvrManualChannelList.setButtonDrawable(R.drawable.layer_epg_genre_radio_btn_unchecked);
                    }
                }

                holder.rbPvrManualChannelList.setButtonDrawable(R.drawable.layer_epg_genre_radio_btn_checked);

                interactionListener.onClick(ch);
            }
        });

        holder.itemView.setOnKeyListener((v, keyCode, event) -> {
            Log.d(TAG, "onBindViewHolder setOnKeyListener position:" + position +
                    ", action:" + event.getAction() + ", keyCode:" + keyCode + "channelList.size()::" + channelList.size());
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_UP && (position - spanCount) > 0) {
                recyclerView.scrollToPosition(position - spanCount);
                View lastView = recyclerView.getLayoutManager().findViewByPosition(position - spanCount);
                if (lastView != null) lastView.requestFocus();
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if ((position + spanCount) < channelList.size()){
                    recyclerView.scrollToPosition(position + spanCount);
                    View firstView = recyclerView.getLayoutManager().findViewByPosition(position + spanCount);
                    if (firstView != null) firstView.requestFocus();
                }
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && (position % spanCount == 0)) {
                if (interactionListener != null) {
                    interactionListener.onJumpToGenreList();
                }
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && (position % spanCount == (spanCount - 1))) {
                if (interactionListener != null) {
                    interactionListener.onJumpToCancel();
                }
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_PROG_RED ||
                    keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
                if (interactionListener != null) {
                    interactionListener.onKeyEventReceived(event);
                }
                return true;
            }

            return false;
        });

        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            Log.d(TAG, "onBindViewHolder setOnFocusChangeListener position:" + position + ", hasFocus:" + hasFocus);
            if (hasFocus) {
                holder.applyFocusStyle();
            } else {
                holder.applyNormalStyle();
            }
        });
    }

    @Override
    public int getItemCount() {
        return channelList != null ? channelList.size() : 0;
    }

    public void updateListAndSelectItem(List<Channel> newList, Channel targetChannel) {
        Log.d(TAG, "updateListWithoutFocus newList size:" + newList.size());
        this.channelList.clear();
        this.channelList.addAll(newList);

        currentChannelIndex = -1;
        for (int i = 0; i < channelList.size(); i++) {
            if (channelList.get(i).getId() == targetChannel.getId()) {
                currentChannelIndex = i;
                break;
            }
        }

        notifyDataSetChanged();
    }

    public void setSelectItem(Channel targetChannel) {
        Log.d(TAG, "setSelectItem targetChannel:" + targetChannel.getDisplayNumber());
        currentChannelIndex = -1;
        for (int i = 0; i < channelList.size(); i++) {
            if (channelList.get(i).getId() == targetChannel.getId()) {
                currentChannelIndex = i;
                break;
            }
        }

        notifyDataSetChanged();
    }
}

