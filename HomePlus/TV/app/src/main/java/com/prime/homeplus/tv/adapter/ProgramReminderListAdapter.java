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

import com.prime.homeplus.tv.R;
import com.prime.homeplus.tv.data.ProgramReminderData;
import com.prime.homeplus.tv.utils.ProgramRatingUtils;
import com.prime.homeplus.tv.utils.TimeUtils;

import java.util.List;

public class ProgramReminderListAdapter extends RecyclerView.Adapter<ProgramReminderListAdapter.ViewHolder> {
    private static final String TAG = "ProgramReminderListAdapter";

    private RecyclerView recyclerView;
    private OnRecyclerViewInteractionListener interactionListener;

    private Context context;
    List<ProgramReminderData> programReminderList;
    private boolean isProgramNameLock = true;

    public ProgramReminderListAdapter(RecyclerView recyclerView, List<ProgramReminderData> programReminderList) {
        Log.d(TAG, "ProgramReminderListAdapter");
        this.recyclerView = recyclerView;
        this.context = recyclerView.getContext();
        this.programReminderList = programReminderList;;
    }

    public interface OnRecyclerViewInteractionListener {
        void onPageUp();
        void onPageDown();
        void onDeleteReminder(ProgramReminderData programReminderData);
        void onShowParentalPinDialog();
    }

    public void setOnRecyclerViewInteractionListener(OnRecyclerViewInteractionListener listener) {
        this.interactionListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llProgramReminderList;
        TextView tvProgramReminderListChannelNumber, tvProgramReminderListChannelName,
                tvProgramReminderListProgramName, tvProgramReminderListProgramDate,
                tvProgramReminderListProgramStartEndTime;

        ImageView ivProgramReminderListPadlock, ivProgramReminderListProgramRating;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "ViewHolder");
            llProgramReminderList = itemView.findViewById(R.id.llProgramReminderList);
            tvProgramReminderListChannelNumber = itemView.findViewById(R.id.tvProgramReminderListChannelNumber);
            tvProgramReminderListChannelName = itemView.findViewById(R.id.tvProgramReminderListChannelName);
            tvProgramReminderListProgramName = itemView.findViewById(R.id.tvProgramReminderListProgramName);
            tvProgramReminderListProgramDate = itemView.findViewById(R.id.tvProgramReminderListProgramDate);
            tvProgramReminderListProgramStartEndTime = itemView.findViewById(R.id.tvProgramReminderListProgramStartEndTime);

            ivProgramReminderListPadlock = itemView.findViewById(R.id.ivProgramReminderListPadlock);
            ivProgramReminderListProgramRating = itemView.findViewById(R.id.ivProgramReminderListProgramRating);
        }

        public void applyFocusStyle() {
            llProgramReminderList.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTabSelect));
            tvProgramReminderListChannelNumber.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvProgramReminderListChannelNumber.setTypeface(null, Typeface.BOLD);
            tvProgramReminderListChannelName.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvProgramReminderListChannelName.setTypeface(null, Typeface.BOLD);
            tvProgramReminderListProgramName.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvProgramReminderListProgramName.setTypeface(null, Typeface.BOLD);
            tvProgramReminderListProgramDate.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvProgramReminderListProgramDate.setTypeface(null, Typeface.BOLD);
            tvProgramReminderListProgramStartEndTime.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvProgramReminderListProgramStartEndTime.setTypeface(null, Typeface.BOLD);
        }

        public void applyNormalStyle() {
            llProgramReminderList.setBackgroundColor(Color.TRANSPARENT);
            tvProgramReminderListChannelNumber.setTextColor(ContextCompat.getColor(context, R.color.colorGoldOpacity70));
            tvProgramReminderListChannelNumber.setTypeface(null, Typeface.NORMAL);
            tvProgramReminderListChannelName.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteOpacity70));
            tvProgramReminderListChannelName.setTypeface(null, Typeface.NORMAL);
            tvProgramReminderListProgramName.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteOpacity70));
            tvProgramReminderListProgramName.setTypeface(null, Typeface.NORMAL);
            tvProgramReminderListProgramDate.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteOpacity70));
            tvProgramReminderListProgramDate.setTypeface(null, Typeface.NORMAL);
            tvProgramReminderListProgramStartEndTime.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteOpacity70));
            tvProgramReminderListProgramStartEndTime.setTypeface(null, Typeface.NORMAL);
        }
    }

    @NonNull
    @Override
    public ProgramReminderListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_program_reminder_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramReminderListAdapter.ViewHolder holder, int position) {
        ProgramReminderData programReminderData = programReminderList.get(position);
        int blockedRatingAge = ProgramRatingUtils.getSystemContentBlockedRating(context);
        Log.d(TAG, "onBindViewHolder position:" + position);

        holder.tvProgramReminderListChannelNumber.setText(programReminderData.getChannelNumber());
        holder.tvProgramReminderListChannelName.setText(programReminderData.getChannelName());

        if (programReminderData.getContentRating() >= blockedRatingAge) {
            if (isProgramNameLock) {
                holder.ivProgramReminderListPadlock.setImageResource(R.drawable.icon_ch_lock_d);
            } else {
                holder.ivProgramReminderListPadlock.setImageResource(R.drawable.icon_ch_unlock_d);
            }
            holder.ivProgramReminderListPadlock.setVisibility(View.VISIBLE);
        } else {
            holder.ivProgramReminderListPadlock.setVisibility(View.INVISIBLE);
        }

        if (programReminderData.getContentRating() >= blockedRatingAge &&
                isProgramNameLock) {
            holder.tvProgramReminderListProgramName.setText(context.getString(R.string.channel_list_parental_rated_program));
        } else {
            holder.tvProgramReminderListProgramName.setText(programReminderData.getProgramName());
        }

        holder.ivProgramReminderListProgramRating.setImageResource(
                ProgramRatingUtils.getRatingIcon(programReminderData.getContentRating()));
        holder.tvProgramReminderListProgramDate.setText(
                TimeUtils.formatTimestampToDateYyMmDd(programReminderData.getStartTimeUtcMillis()));

        String start_end_time = context.getString(R.string.reminder_schedule_format,
                TimeUtils.formatTimestampWithHourMinute(programReminderData.getStartTimeUtcMillis()),
                TimeUtils.formatTimestampWithHourMinute(programReminderData.getEndTimeUtcMillis()));
        holder.tvProgramReminderListProgramStartEndTime.setText(start_end_time);

        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "onBindViewHolder click position:" + position);
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

            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && position == programReminderList.size() - 1) {
                if (interactionListener != null) {
                    interactionListener.onPageDown();
                }
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_PROG_RED) {
                if (interactionListener != null) {
                    interactionListener.onDeleteReminder(programReminderData);
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

            if (holder.ivProgramReminderListPadlock.isShown()) {
                setPadlockIconState(holder.ivProgramReminderListPadlock, isProgramNameLock, hasFocus);
            }
        });
    }

    @Override
    public int getItemCount() {
        return programReminderList != null ? programReminderList.size() : 0;
    }

    public void updateList(List<ProgramReminderData> newList, boolean isFocusBottom) {
        this.programReminderList.clear();
        this.programReminderList.addAll(newList);
        notifyDataSetChanged();

        if (!programReminderList.isEmpty()) {
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
}

