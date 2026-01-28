package com.prime.homeplus.tv.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.homeplus.tv.R;
import com.prime.homeplus.tv.data.ScheduledProgramData;
import com.prime.homeplus.tv.utils.ProgramRatingUtils;
import com.prime.homeplus.tv.utils.ScheduledProgramUtils;
import com.prime.homeplus.tv.utils.TimeUtils;

import java.util.List;

public class PvrScheduledListAdapter extends RecyclerView.Adapter<PvrScheduledListAdapter.ViewHolder> {
    private static final String TAG = "PvrScheduledListAdapter";

    private RecyclerView recyclerView;
    private OnRecyclerViewInteractionListener interactionListener;

    private Context context;
    List<ScheduledProgramData> scheduledProgramList;
    private boolean isProgramNameLock = true;
    private boolean isShowDetails = false;
    private boolean isInSeriesFolder = false;

    public PvrScheduledListAdapter(RecyclerView recyclerView, List<ScheduledProgramData> scheduledProgramList) {
        Log.d(TAG, "PvrScheduledListAdapter");
        this.recyclerView = recyclerView;
        this.context = recyclerView.getContext();
        this.scheduledProgramList = scheduledProgramList;
    }

    public interface OnRecyclerViewInteractionListener {
        void onPageUp();
        void onPageDown();
        void onEnterSeriesFolder(String episodeName);
        void onExitSeriesFolder(String episodeName);
        void onDeleteScheduledProgram(ScheduledProgramData ScheduledProgramData);
        void onShowParentalPinDialog();
        void onBackToPreview();
    }

    public void setOnRecyclerViewInteractionListener(OnRecyclerViewInteractionListener listener) {
        this.interactionListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llScheduledProgramList;
        TextView tvScheduledProgramChannelNumber, tvScheduledProgramChannelName,
                tvScheduledProgramName, tvScheduledProgramDate,
                tvScheduledProgramStartEndTime;

        ImageView ivScheduledProgramPadlock, ivScheduledProgramSeries,
                ivScheduledProgramRecording, ivScheduledProgramRating;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "ViewHolder");
            llScheduledProgramList = itemView.findViewById(R.id.llScheduledProgramList);
            tvScheduledProgramChannelNumber = itemView.findViewById(R.id.tvScheduledProgramChannelNumber);
            tvScheduledProgramChannelName = itemView.findViewById(R.id.tvScheduledProgramChannelName);
            tvScheduledProgramName = itemView.findViewById(R.id.tvScheduledProgramName);
            tvScheduledProgramDate = itemView.findViewById(R.id.tvScheduledProgramDate);
            tvScheduledProgramStartEndTime = itemView.findViewById(R.id.tvScheduledProgramStartEndTime);

            ivScheduledProgramPadlock = itemView.findViewById(R.id.ivScheduledProgramPadlock);
            ivScheduledProgramSeries = itemView.findViewById(R.id.ivScheduledProgramSeries);
            ivScheduledProgramRecording = itemView.findViewById(R.id.ivScheduledProgramRecording);
            ivScheduledProgramRating = itemView.findViewById(R.id.ivScheduledProgramRating);
        }

        public void applyFocusStyle() {
            llScheduledProgramList.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTabSelect));
            tvScheduledProgramChannelNumber.setTextColor(ContextCompat.getColor(context, R.color.white));

            tvScheduledProgramChannelName.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvScheduledProgramChannelName.setTypeface(null, Typeface.BOLD);
            tvScheduledProgramName.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvScheduledProgramName.setTypeface(null, Typeface.BOLD);
            tvScheduledProgramDate.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvScheduledProgramDate.setTypeface(null, Typeface.BOLD);
            tvScheduledProgramStartEndTime.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvScheduledProgramStartEndTime.setTypeface(null, Typeface.BOLD);
        }

        public void applyNormalStyle() {
            llScheduledProgramList.setBackgroundColor(Color.TRANSPARENT);
            tvScheduledProgramChannelNumber.setTextColor(ContextCompat.getColor(context, R.color.colorGoldOpacity70));

            tvScheduledProgramChannelName.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteOpacity70));
            tvScheduledProgramChannelName.setTypeface(null, Typeface.NORMAL);
            tvScheduledProgramName.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteOpacity70));
            tvScheduledProgramName.setTypeface(null, Typeface.NORMAL);
            tvScheduledProgramDate.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteOpacity70));
            tvScheduledProgramDate.setTypeface(null, Typeface.NORMAL);
            tvScheduledProgramStartEndTime.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteOpacity70));
            tvScheduledProgramStartEndTime.setTypeface(null, Typeface.NORMAL);
        }
    }

    @NonNull
    @Override
    public PvrScheduledListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pvr_scheduled_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PvrScheduledListAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder position:" + position);
        ScheduledProgramData scheduledProgramData = scheduledProgramList.get(position);
        int ratingAge = ProgramRatingUtils.getStrictestDvbAge(scheduledProgramData.getContentRatings());
        int blockedRatingAge = ProgramRatingUtils.getSystemContentBlockedRating(context);
        Log.d(TAG, "ratingAge:" + ratingAge + ", blockedRatingAge:" + blockedRatingAge);

        holder.tvScheduledProgramChannelNumber.setText(scheduledProgramData.getChannelNumber());
        holder.tvScheduledProgramChannelName.setText(scheduledProgramData.getChannelName());

        if (ratingAge >= blockedRatingAge) {
            if (isProgramNameLock) {
                holder.ivScheduledProgramPadlock.setImageResource(R.drawable.icon_ch_lock_d);
            } else {
                holder.ivScheduledProgramPadlock.setImageResource(R.drawable.icon_ch_unlock_d);
            }
            holder.ivScheduledProgramPadlock.setVisibility(View.VISIBLE);
        } else {
            holder.ivScheduledProgramPadlock.setVisibility(View.INVISIBLE);
        }

        if (!TextUtils.isEmpty(scheduledProgramData.getEpisodeTitle()) && !isInSeriesFolder) {
            holder.ivScheduledProgramSeries.setVisibility(View.VISIBLE);
        } else {
            holder.ivScheduledProgramSeries.setVisibility(View.GONE);
        }

        if ((ratingAge >= blockedRatingAge) && isProgramNameLock) {
            holder.tvScheduledProgramName.setText(context.getString(R.string.channel_list_parental_rated_program));
        } else {
            holder.tvScheduledProgramName.setText(scheduledProgramData.getTitle());
        }

        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        // check now recording programs
        if (ScheduledProgramUtils.isNowRecordingProgram(scheduledProgramData)) {
            holder.ivScheduledProgramRecording.setVisibility(View.VISIBLE);
        } else {
            holder.ivScheduledProgramRecording.setVisibility(View.GONE);
        }

        holder.ivScheduledProgramRating.setImageResource(ProgramRatingUtils.getRatingIcon(ratingAge));

        holder.tvScheduledProgramDate.setText(TimeUtils.formatTimestampToDateYyMmDd(scheduledProgramData.getStartTimeUtcMillis()));

        String startEndTime = TimeUtils.formatTimestampWithHourMinute(scheduledProgramData.getStartTimeUtcMillis()) +
                "-" +
                TimeUtils.formatTimestampWithHourMinute(scheduledProgramData.getEndTimeUtcMillis());
        holder.tvScheduledProgramStartEndTime.setText(startEndTime);
        holder.tvScheduledProgramStartEndTime.setVisibility(isShowDetails ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "onBindViewHolder click position:" + position);
            if (interactionListener != null &&
                    !TextUtils.isEmpty(scheduledProgramData.getEpisodeTitle()) &&
                    !isInSeriesFolder) {
                interactionListener.onEnterSeriesFolder(scheduledProgramData.getEpisodeTitle());
            }
        });

        holder.itemView.setOnKeyListener((v, keyCode, event) -> {
            Log.d(TAG, "onBindViewHolder setOnKeyListener position:" + position +
                    ", action:" + event.getAction() + ", keyCode:" + keyCode);
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            // Loop Navigation
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP && position == 0) {
                if (interactionListener != null) {
                    interactionListener.onPageUp();
                }
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && position == scheduledProgramList.size() - 1) {
                if (interactionListener != null) {
                    interactionListener.onPageDown();
                }
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (interactionListener != null && !isInSeriesFolder) {
                    interactionListener.onBackToPreview();
                }
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (interactionListener != null) {
                    if (isInSeriesFolder && !TextUtils.isEmpty(scheduledProgramData.getEpisodeTitle())) {
                        interactionListener.onExitSeriesFolder(scheduledProgramData.getEpisodeTitle());
                        return true;
                    }
                    interactionListener.onBackToPreview();
                }
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_PROG_RED) {
                if (interactionListener != null) {
                    interactionListener.onDeleteScheduledProgram(scheduledProgramData);
                }
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_PROG_BLUE && isProgramNameLock) {
                if (interactionListener != null) {
                    interactionListener.onShowParentalPinDialog();
                }
                return true;
            }

            return false;
        });

        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                holder.applyFocusStyle();
            } else {
                holder.applyNormalStyle();
            }

            if (holder.ivScheduledProgramPadlock.isShown()) {
                setPadlockIconState(holder.ivScheduledProgramPadlock, isProgramNameLock, hasFocus);
            }
        });
    }

    @Override
    public int getItemCount() {
        return scheduledProgramList != null ? scheduledProgramList.size() : 0;
    }

    public void updateListWithoutFocus(List<ScheduledProgramData> newList) {
        Log.d(TAG, "updateListWithoutFocus newList size:" + newList.size());
        this.isInSeriesFolder = false;
        this.scheduledProgramList.clear();
        this.scheduledProgramList.addAll(newList);
        notifyDataSetChanged();
    }

    public void updateList(List<ScheduledProgramData> newList, boolean isFocusBottom) {
        updateList(newList, isFocusBottom, this.isInSeriesFolder);
    }

    public void updateList(List<ScheduledProgramData> newList, boolean isFocusBottom, boolean isInSeriesFolder) {
        this.isInSeriesFolder = isInSeriesFolder;
        this.scheduledProgramList.clear();
        this.scheduledProgramList.addAll(newList);
        notifyDataSetChanged();

        if (!scheduledProgramList.isEmpty()) {
            int focusIndex = 0;
            if (isFocusBottom) {
                focusIndex = getItemCount() - 1;
            }
            final int finalFocusIndex = focusIndex;
            recyclerView.scrollToPosition(finalFocusIndex);
            recyclerView.postDelayed(() -> {
                View firstItem = recyclerView.getLayoutManager().findViewByPosition(finalFocusIndex);
                if (firstItem != null) {
                    firstItem.requestFocus();
                }
            }, 100);
        }
    }

    public void lockProgramName() {
        isProgramNameLock = true;
    }

    public void unlockProgramName() {
        isProgramNameLock = false;
    }

    private void setPadlockIconState(ImageView ivPadlockIcon, boolean isLock, boolean hasFocus) {
        int resId = 0;

        if (isLock) {
            resId = hasFocus ? R.drawable.icon_ch_lock_f : R.drawable.icon_ch_lock_d;
        } else {
            resId = hasFocus ? R.drawable.icon_ch_unlock_f : R.drawable.icon_ch_unlock_d;
        }

        ivPadlockIcon.setImageResource(resId);
    }

    public void setDetailViewsVisible(boolean visible) {
        isShowDetails = visible;
        int first = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        int last = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();

        for (int i = first; i <= last; i++) {
            RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(i);
            if (vh instanceof ViewHolder) {
                ((ViewHolder) vh).tvScheduledProgramStartEndTime.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        }
    }
}

