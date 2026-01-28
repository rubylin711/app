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

import com.prime.datastructure.sysdata.PvrRecFileInfo;
import com.prime.homeplus.tv.R;
import com.prime.homeplus.tv.data.RecordedProgramData;
import com.prime.homeplus.tv.utils.FileUtils;
import com.prime.homeplus.tv.utils.ProgramRatingUtils;
import com.prime.homeplus.tv.utils.RecordedProgramUtils;
import com.prime.homeplus.tv.utils.TimeUtils;

import java.util.List;

public class PvrRecordedListAdapter extends RecyclerView.Adapter<PvrRecordedListAdapter.ViewHolder> {
    private static final String TAG = "PvrRecordedListAdapter";

    private RecyclerView recyclerView;
    private OnRecyclerViewInteractionListener interactionListener;

    private Context context;
    List<RecordedProgramData> recordedProgramList;
    private boolean isProgramNameLock = true;
    private boolean isShowDetails = false;
    private boolean isInSeriesFolder = false;

    public PvrRecordedListAdapter(RecyclerView recyclerView, List<RecordedProgramData> recordedProgramList) {
        Log.d(TAG, "PvrRecordedListAdapter");
        this.recyclerView = recyclerView;
        this.context = recyclerView.getContext();
        this.recordedProgramList = recordedProgramList;
    }

    public interface OnRecyclerViewInteractionListener {
        void onPageUp();
        void onPageDown();
        void onPlayRecording(RecordedProgramData recordedProgramData);
        void onEnterSeriesFolder(String episodeName);
        void onExitSeriesFolder(String episodeName);
        void onDeleteRecordedProgram(RecordedProgramData recordedProgramData);
        void onShowParentalPinDialog();
        void onBackToPreview();
    }

    public void setOnRecyclerViewInteractionListener(OnRecyclerViewInteractionListener listener) {
        this.interactionListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llRecordedProgramList;
        TextView tvRecordedProgramChannelNumber, tvRecordedProgramChannelName,
                tvRecordedProgramName, tvRecordedProgramDate,
                tvRecordedProgramDuration, tvRecordedProgramFileSize;

        ImageView ivRecordedProgramPadlock, ivRecordedProgramSeries,
                ivRecordedProgramFail, ivRecordedProgramRecording,
                ivRecordedProgramRating;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "ViewHolder");
            llRecordedProgramList = itemView.findViewById(R.id.llRecordedProgramList);
            tvRecordedProgramChannelNumber = itemView.findViewById(R.id.tvRecordedProgramChannelNumber);
            tvRecordedProgramChannelName = itemView.findViewById(R.id.tvRecordedProgramChannelName);
            tvRecordedProgramName = itemView.findViewById(R.id.tvRecordedProgramName);
            tvRecordedProgramDate = itemView.findViewById(R.id.tvRecordedProgramDate);
            tvRecordedProgramDuration = itemView.findViewById(R.id.tvRecordedProgramDuration);
            tvRecordedProgramFileSize = itemView.findViewById(R.id.tvRecordedProgramFileSize);

            ivRecordedProgramPadlock = itemView.findViewById(R.id.ivRecordedProgramPadlock);
            ivRecordedProgramSeries = itemView.findViewById(R.id.ivRecordedProgramSeries);
            ivRecordedProgramFail = itemView.findViewById(R.id.ivRecordedProgramFail);
            ivRecordedProgramRecording = itemView.findViewById(R.id.ivRecordedProgramRecording);
            ivRecordedProgramRating = itemView.findViewById(R.id.ivRecordedProgramRating);
        }

        public void applyFocusStyle() {
            llRecordedProgramList.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTabSelect));
            tvRecordedProgramChannelNumber.setTextColor(ContextCompat.getColor(context, R.color.white));

            tvRecordedProgramChannelName.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvRecordedProgramChannelName.setTypeface(null, Typeface.BOLD);
            tvRecordedProgramName.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvRecordedProgramName.setTypeface(null, Typeface.BOLD);
            tvRecordedProgramDate.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvRecordedProgramDate.setTypeface(null, Typeface.BOLD);
            tvRecordedProgramDuration.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvRecordedProgramDuration.setTypeface(null, Typeface.BOLD);
            tvRecordedProgramFileSize.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvRecordedProgramFileSize.setTypeface(null, Typeface.BOLD);
        }

        public void applyNormalStyle() {
            llRecordedProgramList.setBackgroundColor(Color.TRANSPARENT);
            tvRecordedProgramChannelNumber.setTextColor(ContextCompat.getColor(context, R.color.colorGoldOpacity70));

            tvRecordedProgramChannelName.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteOpacity70));
            tvRecordedProgramChannelName.setTypeface(null, Typeface.NORMAL);
            tvRecordedProgramName.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteOpacity70));
            tvRecordedProgramName.setTypeface(null, Typeface.NORMAL);
            tvRecordedProgramDate.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteOpacity70));
            tvRecordedProgramDate.setTypeface(null, Typeface.NORMAL);
            tvRecordedProgramDuration.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteOpacity70));
            tvRecordedProgramDuration.setTypeface(null, Typeface.NORMAL);
            tvRecordedProgramFileSize.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteOpacity70));
            tvRecordedProgramFileSize.setTypeface(null, Typeface.NORMAL);
        }
    }

    @NonNull
    @Override
    public PvrRecordedListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pvr_recorded_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PvrRecordedListAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder position:" + position);
        RecordedProgramData recordedProgramData = recordedProgramList.get(position);
        int ratingAge = ProgramRatingUtils.getStrictestDvbAge(recordedProgramData.getContentRatings());
        int blockedRatingAge = ProgramRatingUtils.getSystemContentBlockedRating(context);
        Log.d(TAG, "ratingAge:" + ratingAge + ", blockedRatingAge:" + blockedRatingAge);

        holder.tvRecordedProgramChannelNumber.setText(recordedProgramData.getChannelNumber());
        holder.tvRecordedProgramChannelName.setText(recordedProgramData.getChannelName());

        if (ratingAge >= blockedRatingAge) {
            if (isProgramNameLock) {
                holder.ivRecordedProgramPadlock.setImageResource(R.drawable.icon_ch_lock_d);
            } else {
                holder.ivRecordedProgramPadlock.setImageResource(R.drawable.icon_ch_unlock_d);
            }
            holder.ivRecordedProgramPadlock.setVisibility(View.VISIBLE);
        } else {
            holder.ivRecordedProgramPadlock.setVisibility(View.INVISIBLE);
        }

        if (!TextUtils.isEmpty(recordedProgramData.getEpisodeTitle()) && !isInSeriesFolder) {
            holder.ivRecordedProgramSeries.setVisibility(View.VISIBLE);
        } else {
            holder.ivRecordedProgramSeries.setVisibility(View.GONE);
        }

        if ((ratingAge >= blockedRatingAge) && isProgramNameLock) {
            holder.tvRecordedProgramName.setText(context.getString(R.string.channel_list_parental_rated_program));
        } else {
            holder.tvRecordedProgramName.setText(recordedProgramData.getTitle());
        }

        long recordStatus = recordedProgramData.getPesiRecordStatus();
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        // show recording file state
        if (recordStatus == PvrRecFileInfo.RECORD_STATUS_FAILED) {
            holder.ivRecordedProgramFail.setVisibility(View.VISIBLE);
        } else {
            holder.ivRecordedProgramFail.setVisibility(View.GONE);
        }

        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        // check now recording programs
        if (recordStatus == PvrRecFileInfo.RECORD_STATUS_RECORDING) {
            holder.ivRecordedProgramRecording.setVisibility(View.VISIBLE);
        } else {
            holder.ivRecordedProgramRecording.setVisibility(View.GONE);
        }

        holder.ivRecordedProgramRating.setImageResource(ProgramRatingUtils.getRatingIcon(ratingAge));

        holder.tvRecordedProgramDate.setText(TimeUtils.formatTimestampToDateYyMmDd(recordedProgramData.getStartTimeUtcMillis()));

        holder.tvRecordedProgramDuration.setText(TimeUtils.formatMillisToTime(recordedProgramData.getRecordingDurationMillis()));
        holder.tvRecordedProgramDuration.setVisibility(isShowDetails ? View.VISIBLE : View.GONE);

        holder.tvRecordedProgramFileSize.setText(FileUtils.formatBytes(recordedProgramData.getRecordingDataBytes()));
        holder.tvRecordedProgramFileSize.setVisibility(isShowDetails ? View.VISIBLE : View.GONE);


        holder.itemView.setOnClickListener(v -> {
            Log.d(TAG, "onBindViewHolder click position:" + position);
            if (interactionListener != null) {
                if (!TextUtils.isEmpty(recordedProgramData.getEpisodeTitle()) &&
                    !isInSeriesFolder) {
                    interactionListener.onEnterSeriesFolder(recordedProgramData.getEpisodeTitle());
                } else {
                    if ((ratingAge >= blockedRatingAge) && isProgramNameLock) {
                        interactionListener.onShowParentalPinDialog();
                    } else {
                        interactionListener.onPlayRecording(recordedProgramData);
                    }
                }
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

            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && position == recordedProgramList.size() - 1) {
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
                    if (isInSeriesFolder && !TextUtils.isEmpty(recordedProgramData.getEpisodeTitle())) {
                        interactionListener.onExitSeriesFolder(recordedProgramData.getEpisodeTitle());
                        return true;
                    }
                    interactionListener.onBackToPreview();
                }
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_PROG_RED) {
                if (interactionListener != null) {
                    interactionListener.onDeleteRecordedProgram(recordedProgramData);
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

            if (holder.ivRecordedProgramPadlock.isShown()) {
                setPadlockIconState(holder.ivRecordedProgramPadlock, isProgramNameLock, hasFocus);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recordedProgramList != null ? recordedProgramList.size() : 0;
    }

    public void updateListWithoutFocus(List<RecordedProgramData> newList) {
        Log.d(TAG, "updateListWithoutFocus newList size:" + newList.size());
        this.isInSeriesFolder = false;
        this.recordedProgramList.clear();
        this.recordedProgramList.addAll(newList);
        notifyDataSetChanged();
    }

    public void updateList(List<RecordedProgramData> newList, boolean isFocusBottom) {
        updateList(newList, isFocusBottom, this.isInSeriesFolder);
    }

    public void updateList(List<RecordedProgramData> newList, boolean isFocusBottom, boolean isInSeriesFolder) {
        this.isInSeriesFolder = isInSeriesFolder;
        this.recordedProgramList.clear();
        this.recordedProgramList.addAll(newList);
        notifyDataSetChanged();

        if (!recordedProgramList.isEmpty()) {
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
                ((ViewHolder) vh).tvRecordedProgramDuration.setVisibility(visible ? View.VISIBLE : View.GONE);
                ((ViewHolder) vh).tvRecordedProgramFileSize.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        }
    }
}

