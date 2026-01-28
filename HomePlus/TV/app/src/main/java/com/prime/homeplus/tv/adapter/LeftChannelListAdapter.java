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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.tvprovider.media.tv.Channel;
import androidx.tvprovider.media.tv.Program;

import com.prime.homeplus.tv.R;
import com.prime.homeplus.tv.manager.LockManager;
import com.prime.homeplus.tv.utils.ChannelUtils;
import com.prime.homeplus.tv.utils.ProgramUtils;
import com.prime.homeplus.tv.utils.TimeUtils;
import com.prime.homeplus.tv.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class LeftChannelListAdapter extends RecyclerView.Adapter<LeftChannelListAdapter.ViewHolder> {
    private static final String TAG = "LeftChannelListAdapter";

    private RecyclerView recyclerView;
    private TextView tvFavoriteState;
    private List<Channel> channelList;

    private OnItemFocusedListener channelFocusedListener;
    private OnItemClickListener channelClickListener;

    private Context context;
    private int currentGenreIndex = 0;

    public LeftChannelListAdapter(RecyclerView recyclerView, TextView tvFavoriteState, List<Channel> channelList) {
        this.recyclerView = recyclerView;
        this.tvFavoriteState = tvFavoriteState;
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
        LinearLayout llLeftChannelList;
        TextView tvLeftChannelListChannelNum, tvLeftChannelListChannelName, tvLeftChannelListProgramName, tvLeftChannelListPgStartTime, tvLeftChannelListPgEndTime;
        ImageView ivLeftChannelListPadlock, ivLeftChannelListMusic, ivLeftChannelListStar;
        ProgressBar pbLeftChannelList;

        Program cacheProgram;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            llLeftChannelList = itemView.findViewById(R.id.llLeftChannelList);
            tvLeftChannelListChannelNum = itemView.findViewById(R.id.tvLeftChannelListChannelNum);
            tvLeftChannelListChannelName = itemView.findViewById(R.id.tvLeftChannelListChannelName);
            tvLeftChannelListProgramName = itemView.findViewById(R.id.tvLeftChannelListProgramName);
            tvLeftChannelListPgStartTime = itemView.findViewById(R.id.tvLeftChannelListPgStartTime);
            tvLeftChannelListPgEndTime = itemView.findViewById(R.id.tvLeftChannelListPgEndTime);
            ivLeftChannelListPadlock = itemView.findViewById(R.id.ivLeftChannelListPadlock);
            ivLeftChannelListMusic = itemView.findViewById(R.id.ivLeftChannelListMusic);
            ivLeftChannelListStar = itemView.findViewById(R.id.ivLeftChannelListStar);
            pbLeftChannelList = itemView.findViewById(R.id.pbLeftChannelList);
        }
    }

    @NonNull
    @Override
    public LeftChannelListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_left_channel_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeftChannelListAdapter.ViewHolder holder, int position) {
        //Log.d(TAG, "onBindViewHolder position:" + position);
        Channel channel = channelList.get(position);

        if (channel == null) {
            Log.d(TAG, "channel is null, return !");
            return;
        }

        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        boolean isFavorite = ChannelUtils.isChannelFavorite(recyclerView.getContext(),channel);

        holder.tvLeftChannelListChannelNum.setText(channel.getDisplayNumber());
        holder.tvLeftChannelListChannelName.setText(channel.getDisplayName());
        holder.ivLeftChannelListPadlock.setVisibility(View.GONE);
        holder.ivLeftChannelListMusic.setVisibility("SERVICE_TYPE_AUDIO".equals(channel.getServiceType()) ? View.VISIBLE : View.GONE);
        holder.ivLeftChannelListStar.setVisibility(isFavorite ? View.VISIBLE : View.GONE);

        holder.cacheProgram = ProgramUtils.getCurrentProgram(context, channel.getId());
        bindProgramToViewHolder(holder, channel, holder.cacheProgram);

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

            if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW) { // Yellow
                Log.d(TAG, "Yellow key pressed: toggling favorite");
                // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
                boolean isFavorite2 = ChannelUtils.isChannelFavorite(recyclerView.getContext(),channel);
                if (isFavorite2)
                    ChannelUtils.setFavorite(recyclerView.getContext(), channel, 0);
                else
                    ChannelUtils.setFavorite(recyclerView.getContext(), channel, 1);
                isFavorite2 = !isFavorite2;
                holder.ivLeftChannelListStar.setVisibility(isFavorite2 ? View.VISIBLE : View.GONE);
                tvFavoriteState.setText(isFavorite2 ? context.getString(R.string.delete_favourite) : context.getString(R.string.add_favourite));
                return true;
            }
            return false;
        });

        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && channelFocusedListener != null) {
                channelFocusedListener.onItemFocused();
            }

            if (hasFocus) {
                Boolean isStarVisible = (holder.ivLeftChannelListStar.getVisibility() == View.VISIBLE);
                tvFavoriteState.setText(isStarVisible ? context.getString(R.string.delete_favourite) : context.getString(R.string.add_favourite));

                holder.llLeftChannelList.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTabSelect));
                holder.tvLeftChannelListChannelNum.setTextColor(ContextCompat.getColor(context, R.color.white));
                holder.tvLeftChannelListChannelNum.setTypeface(null, Typeface.BOLD);
                holder.tvLeftChannelListChannelName.setTextColor(ContextCompat.getColor(context, R.color.white));
                holder.tvLeftChannelListChannelName.setTypeface(null, Typeface.BOLD);

                holder.tvLeftChannelListProgramName.setTextColor(ContextCompat.getColor(context, R.color.white));
                holder.tvLeftChannelListProgramName.setTypeface(null, Typeface.BOLD);

                holder.ivLeftChannelListPadlock.setImageResource(R.drawable.icon_ch_lock_f);
                holder.ivLeftChannelListMusic.setImageResource(R.drawable.icon_ch_music_f);
                holder.ivLeftChannelListStar.setImageResource(R.drawable.icon_ch_favorite_f);
            } else {
                holder.llLeftChannelList.setBackgroundColor(Color.TRANSPARENT);
                holder.tvLeftChannelListChannelNum.setTextColor(ContextCompat.getColor(context, R.color.colorGold2));
                holder.tvLeftChannelListChannelNum.setTypeface(null, Typeface.NORMAL);
                holder.tvLeftChannelListChannelName.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteOpacity70));
                holder.tvLeftChannelListChannelName.setTypeface(null, Typeface.NORMAL);

                holder.tvLeftChannelListProgramName.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteOpacity70));
                holder.tvLeftChannelListProgramName.setTypeface(null, Typeface.NORMAL);

                holder.ivLeftChannelListPadlock.setImageResource(R.drawable.icon_ch_lock_d);
                holder.ivLeftChannelListMusic.setImageResource(R.drawable.icon_ch_music_d);
                holder.ivLeftChannelListStar.setImageResource(R.drawable.icon_ch_favorite_d);
            }
        });
    }

    @Override
    public int getItemCount() {
        return channelList != null ? channelList.size() : 0;
    }

    public int getCurrentGenreIndex() {
        return currentGenreIndex;
    }

    public void refreshVisibleProgramInfo() {
        Log.d(TAG, "refreshVisibleProgramInfo");
        if (channelList == null || channelList.isEmpty()) return;

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager == null) return;

        int firstVisible = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        int lastVisible = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();

        for (int i = firstVisible; i <= lastVisible; i++) {
            View itemView = layoutManager.findViewByPosition(i);
            if (itemView == null) continue;

            ViewHolder holder = (ViewHolder) recyclerView.getChildViewHolder(itemView);
            Channel channel = channelList.get(i);
            Program pg;
            if (holder.cacheProgram != null && ProgramUtils.isNowPlaying(holder.cacheProgram)) {
                //Log.d(TAG, "use cached program");
                pg = holder.cacheProgram;
            } else {
                //Log.d(TAG, "get new program");
                pg = ProgramUtils.getCurrentProgram(context, channel.getId());
            }
            bindProgramToViewHolder(holder, channel, pg);
        }
    }

    public void refreshFocusedProgramInfo() {
        Log.d(TAG, "refreshFocusedProgramInfo");
        if (channelList == null || channelList.isEmpty()) return;

        View focusedView = recyclerView.getFocusedChild();
        if (focusedView == null) return;

        int position = recyclerView.getLayoutManager().getPosition(focusedView);
        if (position == RecyclerView.NO_POSITION || position >= channelList.size()) return;

        ViewHolder holder = (ViewHolder) recyclerView.getChildViewHolder(focusedView);
        Channel channel = channelList.get(position);

        Program pg;
        if (holder.cacheProgram != null && ProgramUtils.isNowPlaying(holder.cacheProgram)) {
            // Log.d(TAG, "use cached program");
            pg = holder.cacheProgram;
        } else {
            // Log.d(TAG, "get new program");
            pg = ProgramUtils.getCurrentProgram(context, channel.getId());
            holder.cacheProgram = pg;
        }

        bindProgramToViewHolder(holder, channel, pg);
    }

    private void bindProgramToViewHolder(ViewHolder holder, Channel ch, Program pg) {
        LockManager lockManager = new LockManager(context.getApplicationContext());
        int lock_flag = lockManager.getHighestPriorityLockFlag(ch, pg, null);
        // Do not apply locking when lock_flag is LOCK_WORK_HOUR
        if (lock_flag != LockManager.LOCK_NONE && lock_flag != LockManager.LOCK_WORK_HOUR) {
            holder.ivLeftChannelListPadlock.setVisibility(View.VISIBLE);
            holder.tvLeftChannelListPgStartTime.setVisibility(View.GONE);
            holder.tvLeftChannelListPgEndTime.setVisibility(View.GONE);
            holder.pbLeftChannelList.setVisibility(View.GONE);

            // Default case: lock_flag is assumed to be LOCK_PARENTAL_PROGRAM
            String lockProgramName = context.getString(R.string.channel_list_parental_rated_program);
            if (lock_flag == LockManager.LOCK_ADULT_CHANNEL) {
                lockProgramName = context.getString(R.string.channel_list_adult_channel);
            } else if (lock_flag == LockManager.LOCK_PARENTAL_CHANNEL) {
                lockProgramName = context.getString(R.string.channel_list_locked_channel);
            }
            holder.tvLeftChannelListProgramName.setText(lockProgramName);
        } else {
            holder.ivLeftChannelListPadlock.setVisibility(View.GONE);
            holder.tvLeftChannelListPgStartTime.setVisibility(View.VISIBLE);
            holder.tvLeftChannelListPgEndTime.setVisibility(View.VISIBLE);
            holder.pbLeftChannelList.setVisibility(View.VISIBLE);
            if (pg != null) {
                holder.tvLeftChannelListProgramName.setText(pg.getTitle());
                holder.tvLeftChannelListPgStartTime.setText(TimeUtils.formatToLocalTime(pg.getStartTimeUtcMillis(), "HH:mm"));
                holder.tvLeftChannelListPgEndTime.setText(TimeUtils.formatToLocalTime(pg.getEndTimeUtcMillis(), "HH:mm"));
                ViewUtils.setProgressWithMillisMax(holder.pbLeftChannelList, pg.getStartTimeUtcMillis(), pg.getEndTimeUtcMillis());
            } else {
                holder.tvLeftChannelListProgramName.setText(context.getString(R.string.no_program_info));
                holder.tvLeftChannelListPgStartTime.setText("00:00");
                holder.tvLeftChannelListPgEndTime.setText("00:00");
                holder.pbLeftChannelList.setProgress(0);
            }
        }
    }

    public void updateList(int genreIndex, List<Channel> newList, LinearLayout noListView, Channel focusChannel) {
        this.currentGenreIndex = genreIndex;
        this.channelList.clear();
        this.channelList.addAll(newList);
        notifyDataSetChanged();

        // reset focus
        if (!channelList.isEmpty()) {
            final int finalFocusIndex = findIndexByChannel(focusChannel); // default index 0
            recyclerView.scrollToPosition(finalFocusIndex);
            recyclerView.postDelayed(() -> {
                View firstItem = recyclerView.getLayoutManager().findViewByPosition(finalFocusIndex);
                if (firstItem != null) {
                    firstItem.requestFocus();
                }
            }, 100);
            recyclerView.setVisibility(View.VISIBLE);
            noListView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            noListView.setVisibility(View.VISIBLE);
        }
    }

    public void clearList() {
        channelList.clear();
        this.channelList.addAll(new ArrayList<>());
        notifyDataSetChanged();
        recyclerView.setVisibility(View.GONE);
    }

    private int findIndexByChannel(Channel focusChannel) {
        if (focusChannel != null) {
            for (int i = 0; i < channelList.size(); i++) {
                Channel ch = channelList.get(i);
                if (focusChannel.getId() == ch.getId()) {
                    return i;
                }
            }
        }
        return 0;
    }
}

