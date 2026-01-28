package com.prime.homeplus.tv.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
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

import java.util.List;

public class EpgChannelListAdapter extends RecyclerView.Adapter<EpgChannelListAdapter.ViewHolder> {
    private static final String TAG = "EpgChannelListAdapter";

    private RecyclerView recyclerView;
    private OnRecyclerViewInteractionListener interactionListener;

    private Context context;
    List<Channel> channelList;
    private Channel lastFocusedChannel;
    private int lastFocusedPosition = -1;
    private int lastActionDownKeyCode = -1;

    public EpgChannelListAdapter(RecyclerView recyclerView, List<Channel> channelList) {
        Log.d(TAG, "EpgChannelListAdapter");
        this.recyclerView = recyclerView;
        this.context = recyclerView.getContext();
        this.channelList = channelList;;
    }

    public interface OnRecyclerViewInteractionListener {
        void onPageUp();
        void onPageDown();
        void onKeyEventReceived(KeyEvent event);
        void onFocus(boolean hasFocus);
        void onFocusChannelChanged(int index, Channel ch);
        boolean isUnlocked(int lockFlag);
    }

    public Channel getLastFocusedChannel() {
        return lastFocusedChannel;
    }

    public void setOnRecyclerViewInteractionListener(OnRecyclerViewInteractionListener listener) {
        this.interactionListener = listener;
    }

    public void setLastFocusedChannelAndPosition(int position, Channel ch) {
        if ((lastFocusedChannel == null) || (ch.getId() != lastFocusedChannel.getId())) {
            lastFocusedChannel = ch;
            lastFocusedPosition = position;
        }
    }

    public void setLastActionDownKeyCode(int keyCode) {
        if (lastActionDownKeyCode != keyCode) {
            lastActionDownKeyCode = keyCode;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llEpgChannelList;
        ImageView ivEpgChannelListIcon;
        TextView tvEpgChannelListNumber, tvEpgChannelListName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "ViewHolder");
            llEpgChannelList = itemView.findViewById(R.id.llEpgChannelList);
            ivEpgChannelListIcon = itemView.findViewById(R.id.ivEpgChannelListIcon);
            tvEpgChannelListNumber = itemView.findViewById(R.id.tvEpgChannelListNumber);
            tvEpgChannelListName = itemView.findViewById(R.id.tvEpgChannelListName);
        }

        public void applyFocusStyle() {
            llEpgChannelList.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTabSelect));
            tvEpgChannelListNumber.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvEpgChannelListNumber.setTypeface(null, Typeface.BOLD);
            tvEpgChannelListName.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvEpgChannelListName.setTypeface(null, Typeface.BOLD);
        }

        public void applySelectedStyle() {
            llEpgChannelList.setBackgroundColor(ContextCompat.getColor(context, R.color.colorEpgSelect));
            tvEpgChannelListNumber.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvEpgChannelListNumber.setTypeface(null, Typeface.BOLD);
            tvEpgChannelListName.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvEpgChannelListName.setTypeface(null, Typeface.BOLD);
        }

        public void applyNormalStyle() {
            llEpgChannelList.setBackgroundColor(Color.TRANSPARENT);
            tvEpgChannelListNumber.setTextColor(ContextCompat.getColor(context, R.color.colorGoldOpacity70));
            tvEpgChannelListNumber.setTypeface(null, Typeface.NORMAL);
            tvEpgChannelListName.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteOpacity70));
            tvEpgChannelListName.setTypeface(null, Typeface.NORMAL);
        }
    }

    @NonNull
    @Override
    public EpgChannelListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_epg_channel_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EpgChannelListAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder position:" + position);
        Channel ch = channelList.get(position);
        if (ch == null) {
            Log.d(TAG, "channel is null, return !");
            return;
        }

        if (lastFocusedPosition == -1 && position == 0) {
            // draw default select item
            holder.applySelectedStyle();
        }

        setStatusIconByChannel(holder.ivEpgChannelListIcon, ch, false);
        holder.tvEpgChannelListNumber.setText(StringUtils.padToNDigits(ch.getDisplayNumber(), 3));
        holder.tvEpgChannelListName.setText(ch.getDisplayName());

        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "onBindViewHolder click position:" + position);
        });

        holder.itemView.setOnKeyListener((v, keyCode, event) -> {
            Log.d(TAG, "onBindViewHolder setOnKeyListener position:" + position +
                    ", action:" + event.getAction() + ", keyCode:" + keyCode);
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }
            setLastActionDownKeyCode(keyCode);

            // Loop Navigation
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP && position == 0) {
                if (interactionListener != null) {
                    interactionListener.onPageUp();
                }
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && position == channelList.size() - 1) {
                if (interactionListener != null) {
                    interactionListener.onPageDown();
                }
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
                    keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ||
                    keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                if (interactionListener != null) {
                    interactionListener.onKeyEventReceived(event);
                }
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW) { // favorite
                // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
                boolean isFavorite = ChannelUtils.isChannelFavorite(recyclerView.getContext(),ch);
                if (isFavorite) {
                    ChannelUtils.setFavorite(recyclerView.getContext(), ch, 0);
                    holder.ivEpgChannelListIcon.setImageResource(0);
                    holder.ivEpgChannelListIcon.setTag(0);
                    holder.ivEpgChannelListIcon.setVisibility(View.INVISIBLE);
                }
                else {
                    ChannelUtils.setFavorite(recyclerView.getContext(), ch, 1);
                    holder.ivEpgChannelListIcon.setImageResource(R.drawable.icon_ch_favorite_f);
                    holder.ivEpgChannelListIcon.setTag(R.drawable.icon_ch_favorite_f);
                    holder.ivEpgChannelListIcon.setVisibility(View.VISIBLE);

                }

//                String channelUrl = "content://android.media.tv/channel/" + ch.getId();
//                Long fav1 = ChannelUtils.getChannelFavorite(recyclerView.getContext(), channelUrl)? 1L: 0L;
//
//                if (fav1 != null && fav1 == 1) {
//                    ChannelUtils.setFavorite(recyclerView.getContext(), Uri.parse(channelUrl), 0);
//                    holder.ivEpgChannelListIcon.setImageResource(0);
//                    holder.ivEpgChannelListIcon.setTag(0);
//                    holder.ivEpgChannelListIcon.setVisibility(View.INVISIBLE);
//                }
//                else {
//                    ChannelUtils.setFavorite(recyclerView.getContext(), Uri.parse(channelUrl), 1);
//                    holder.ivEpgChannelListIcon.setImageResource(R.drawable.icon_ch_favorite_f);
//                    holder.ivEpgChannelListIcon.setTag(R.drawable.icon_ch_favorite_f);
//                    holder.ivEpgChannelListIcon.setVisibility(View.VISIBLE);
//                }

                if (interactionListener != null) {
                    interactionListener.onKeyEventReceived(event);
                }
                return true;
            }

            return false;
        });

        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (interactionListener != null) {
                    interactionListener.onFocus(true);
                    if ((lastFocusedChannel == null) || (ch.getId() != lastFocusedChannel.getId())) {
                        interactionListener.onFocusChannelChanged(position, ch);
                    }
                }

                for (int i = 0; i < recyclerView.getAdapter().getItemCount(); i++) {
                    EpgChannelListAdapter.ViewHolder viewHolderAtPosition = (EpgChannelListAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
                    if (viewHolderAtPosition != null) {
                        if (i != position) {
                            viewHolderAtPosition.applyNormalStyle();
                        }
                    }
                }

                setLastFocusedChannelAndPosition(position, ch);
                holder.applyFocusStyle();
            } else {
                holder.applyNormalStyle();
            }

            updateIconFocusState(holder.ivEpgChannelListIcon, hasFocus);
        });
    }

    @Override
    public int getItemCount() {
        return channelList != null ? channelList.size() : 0;
    }

    public void updateListAndSetFocus(List<Channel> newList, int focusIndex) {
        this.channelList.clear();
        this.channelList.addAll(newList);
        notifyDataSetChanged();

        if (!channelList.isEmpty()) {
            int index = 0;
            if (focusIndex >= getItemCount()) {
                index = getItemCount() - 1;
            } else if (focusIndex >= 0) {
                index = focusIndex;
            }
            final int finalFocusIndex = index;
            recyclerView.scrollToPosition(finalFocusIndex);
            recyclerView.postDelayed(() -> {
                View firstItem = recyclerView.getLayoutManager().findViewByPosition(finalFocusIndex);
                if (firstItem != null) {
                    firstItem.requestFocus();
                }
            }, 100);
        }
    }

    public void drawSelectItem(int selectIndex, Channel selectChannel) {
        lastFocusedChannel = selectChannel;
        lastFocusedPosition = selectIndex;
        for (int i = 0; i < recyclerView.getAdapter().getItemCount(); i++) {
            EpgChannelListAdapter.ViewHolder viewHolderAtPosition = (EpgChannelListAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
            if (viewHolderAtPosition != null) {
                if (i == selectIndex) {
                    viewHolderAtPosition.applySelectedStyle();
                } else {
                    viewHolderAtPosition.applyNormalStyle();
                }
            }
        }
    }

    private void setStatusIconByChannel(ImageView ivStatusIcon, Channel ch, boolean hasFocus) {
        Log.d(TAG, "setStatusIconByChannel ch:" + ch.getDisplayName());
        int resId = 0;
        LockManager lockManager = new LockManager(context.getApplicationContext());
        int lockFlag = lockManager.getHighestPriorityLockFlag(ch, null, null);
        boolean isFavorite = ChannelUtils.isChannelFavorite(context, ch);
        if (lockFlag != LockManager.LOCK_NONE && lockFlag != LockManager.LOCK_WORK_HOUR) { // lock
            boolean isUnlocked = false;
            if (interactionListener != null) {
                isUnlocked = interactionListener.isUnlocked(LockManager.LOCK_EPG_PROGRAM_INFO);
            }

            if (isUnlocked) {
                resId = hasFocus ? R.drawable.icon_ch_unlock_f : R.drawable.icon_ch_unlock_d;
            } else {
                resId = hasFocus ? R.drawable.icon_ch_lock_f : R.drawable.icon_ch_lock_d;
            }
        } else if ("SERVICE_TYPE_AUDIO".equals(ch.getServiceType())) { // music
            resId = hasFocus ? R.drawable.icon_ch_music_f : R.drawable.icon_ch_music_d;
        } else if (isFavorite) { // favorite
            // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
            resId = hasFocus ? R.drawable.icon_ch_favorite_f : R.drawable.icon_ch_favorite_d;
        }

        ivStatusIcon.setImageResource(resId);
        ivStatusIcon.setTag(resId);
        if (resId == 0) {
            ivStatusIcon.setVisibility(View.INVISIBLE);
        } else {
            ivStatusIcon.setVisibility(View.VISIBLE);
        }
    }

    private void updateIconFocusState(ImageView ivStatusIcon, boolean hasFocus) {
        Object tag = ivStatusIcon.getTag();
        if (tag instanceof Integer) {
            int origResId = (int) tag, resId = 0;
            switch (origResId) {
                case 0:
                    ivStatusIcon.setVisibility(View.INVISIBLE);
                    return;
                case R.drawable.icon_ch_unlock_d:
                    resId = hasFocus ? R.drawable.icon_ch_unlock_f : -1;
                    break;
                case R.drawable.icon_ch_unlock_f:
                    resId = !hasFocus ? R.drawable.icon_ch_unlock_d : -1;
                    break;
                case R.drawable.icon_ch_lock_d:
                    resId = hasFocus ? R.drawable.icon_ch_lock_f : -1;
                    break;
                case R.drawable.icon_ch_lock_f:
                    resId = !hasFocus ? R.drawable.icon_ch_lock_d : -1;
                    break;
                case R.drawable.icon_ch_music_d:
                    resId = hasFocus ? R.drawable.icon_ch_music_f : -1;
                    break;
                case R.drawable.icon_ch_music_f:
                    resId = !hasFocus ? R.drawable.icon_ch_music_d : -1;
                    break;
                case R.drawable.icon_ch_favorite_d:
                    resId = hasFocus ? R.drawable.icon_ch_favorite_f : -1;
                    break;
                case R.drawable.icon_ch_favorite_f:
                    resId = !hasFocus ? R.drawable.icon_ch_favorite_d : -1;
                    break;
                default:
                    break;
            }

            if (resId != -1) {
                ivStatusIcon.setImageResource(resId);
                ivStatusIcon.setTag(resId);
            }
        }
    }
}

