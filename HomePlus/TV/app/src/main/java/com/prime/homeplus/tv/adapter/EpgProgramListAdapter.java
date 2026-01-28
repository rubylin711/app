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
import androidx.recyclerview.widget.RecyclerView;
import androidx.tvprovider.media.tv.Program;

import com.prime.datastructure.config.Pvcfg;
import com.prime.homeplus.tv.R;
import com.prime.homeplus.tv.data.ScheduledProgramData;
import com.prime.homeplus.tv.manager.LockManager;
import com.prime.homeplus.tv.utils.ScheduledProgramUtils;
import com.prime.homeplus.tv.utils.TimeUtils;
import com.prime.homeplus.tv.utils.ProgramReminderUtils;

import java.util.List;

public class EpgProgramListAdapter extends RecyclerView.Adapter<EpgProgramListAdapter.ViewHolder> {
    private static final String TAG = "EpgProgramListAdapter";

    private RecyclerView recyclerView;
    private OnRecyclerViewInteractionListener interactionListener;

    private Context context;
    List<Program> programList;
    private int lastFocusedPosition = -1;
    private int lastActionDownKeyCode = -1;

    public EpgProgramListAdapter(RecyclerView recyclerView, List<Program> programList) {
        Log.d(TAG, "EpgProgramListAdapter");
        this.recyclerView = recyclerView;
        this.context = recyclerView.getContext();
        this.programList = programList;;
    }

    public interface OnRecyclerViewInteractionListener {
        void onPageUp();
        void onPageDown();
        void onKeyEventReceived(KeyEvent event);
        void onFocus(boolean hasFocus);
        void onFocusProgram(int index, Program pg);
        void onShowRecordingSetting(Program pg);
        void onShowReminderMenu(Program pg);
        boolean isUnlocked(int lockFlag);
    }

    public void setOnRecyclerViewInteractionListener(OnRecyclerViewInteractionListener listener) {
        this.interactionListener = listener;
    }

    public void setLastFocusedPosition(int position) {
        if (lastFocusedPosition != position) {
            lastFocusedPosition = position;
        }
    }

    public void setLastActionDownKeyCode(int keyCode) {
        if (lastActionDownKeyCode != keyCode) {
            lastActionDownKeyCode = keyCode;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llEpgProgramList;
        TextView tvEpgProgramListStartTime, tvEpgProgramListName;
        ImageView ivEpgProgramListReminder, ivEpgProgramListRecordSeries, ivEpgProgramListRecord, ivEpgProgramListRecording;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Log.d(TAG, "ViewHolder");
            llEpgProgramList = itemView.findViewById(R.id.llEpgProgramList);
            tvEpgProgramListStartTime = itemView.findViewById(R.id.tvEpgProgramListStartTime);
            tvEpgProgramListName = itemView.findViewById(R.id.tvEpgProgramListName);
            ivEpgProgramListReminder = itemView.findViewById(R.id.ivEpgProgramListReminder);
            ivEpgProgramListRecordSeries = itemView.findViewById(R.id.ivEpgProgramListRecordSeries);
            ivEpgProgramListRecord = itemView.findViewById(R.id.ivEpgProgramListRecord);
            ivEpgProgramListRecording = itemView.findViewById(R.id.ivEpgProgramListRecording);
        }

        public void applyFocusStyle() {
            llEpgProgramList.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTabSelect));
            tvEpgProgramListStartTime.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvEpgProgramListStartTime.setTypeface(null, Typeface.BOLD);
            tvEpgProgramListName.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvEpgProgramListName.setTypeface(null, Typeface.BOLD);
        }

        public void applySelectedStyle() {
            llEpgProgramList.setBackgroundColor(ContextCompat.getColor(context, R.color.colorEpgSelect));
            tvEpgProgramListStartTime.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvEpgProgramListStartTime.setTypeface(null, Typeface.BOLD);
            tvEpgProgramListName.setTextColor(ContextCompat.getColor(context, R.color.white));
            tvEpgProgramListName.setTypeface(null, Typeface.BOLD);
        }

        public void applyNormalStyle() {
            llEpgProgramList.setBackgroundColor(Color.TRANSPARENT);
            tvEpgProgramListStartTime.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteOpacity40));
            tvEpgProgramListStartTime.setTypeface(null, Typeface.NORMAL);
            tvEpgProgramListName.setTextColor(ContextCompat.getColor(context, R.color.colorWhiteOpacity70));
            tvEpgProgramListName.setTypeface(null, Typeface.NORMAL);
        }
    }

    @NonNull
    @Override
    public EpgProgramListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_epg_program_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EpgProgramListAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder position:" + position);
        Program pg = programList.get(position);
        if (pg == null) {
            holder.tvEpgProgramListStartTime.setText("");
            holder.tvEpgProgramListName.setText(context.getString(R.string.no_program_info));
        } else {
            holder.tvEpgProgramListStartTime.setText(TimeUtils.formatToLocalTime(pg.getStartTimeUtcMillis(), "HH:mm"));

            String programName = pg.getTitle();
            LockManager lockManager = new LockManager(context.getApplicationContext());
            // only check parental program lock
            int lockFlag = lockManager.getHighestPriorityLockFlag(null, pg, null);
            if (lockFlag == LockManager.LOCK_PARENTAL_PROGRAM) {
                boolean isUnlocked = false;
                if (interactionListener != null) {
                    isUnlocked = interactionListener.isUnlocked(LockManager.LOCK_EPG_PROGRAM_INFO);
                }

                if (!isUnlocked) {
                    programName = context.getString(R.string.channel_list_parental_rated_program);
                }
            }
            holder.tvEpgProgramListName.setText(programName);
        }

        if (lastFocusedPosition == -1 && position == 0) {
            // draw default select item
            holder.applySelectedStyle();
        } else {
            // reset unselect items
            holder.applyNormalStyle();
        }

        if (pg != null && ProgramReminderUtils.doesReminderExist(context, pg.getId())) {
            if(!Pvcfg.get_hideLauncherPvr()) {//eric lin 20251229 hide reminder
                holder.ivEpgProgramListReminder.setVisibility(View.VISIBLE);
            }
        } else {
            holder.ivEpgProgramListReminder.setVisibility(View.GONE);
        }

        boolean isRecording = false, isSingleRecord = false, isSeriesRecord = false;
        if (pg != null) {
            ScheduledProgramData scheduledProgramData = ScheduledProgramUtils.getScheduledProgram(context, pg.getId());
            if (scheduledProgramData != null) {
                if (TimeUtils.isInTimeRange(scheduledProgramData.getStartTimeUtcMillis(),
                        scheduledProgramData.getEndTimeUtcMillis())) {
                    isRecording = true;
                } else {
                    // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
                    if (!TextUtils.isEmpty(scheduledProgramData.getEpisodeTitle())) {
                        isSeriesRecord = true;
                    } else {
                        isSingleRecord = true;
                    }
                }
            }
        }
        if(Pvcfg.get_hideLauncherPvr()) {//eric lin 20251224 hide launcher pvr
            Log.d(TAG, "EPG BK5 (onBindViewHolder)");
            holder.ivEpgProgramListRecordSeries.setVisibility(View.GONE);
            holder.ivEpgProgramListRecord.setVisibility(View.GONE);
            holder.ivEpgProgramListRecording.setVisibility(View.GONE);
        } else {
            holder.ivEpgProgramListRecordSeries.setVisibility(isSeriesRecord ? View.VISIBLE : View.GONE);
            holder.ivEpgProgramListRecord.setVisibility(isSingleRecord ? View.VISIBLE : View.GONE);
            holder.ivEpgProgramListRecording.setVisibility(isRecording ? View.VISIBLE : View.GONE);
        }

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

            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && position == programList.size() - 1) {
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

            if (keyCode == KeyEvent.KEYCODE_PROG_RED) {
                if (interactionListener != null) {
                    Log.d(TAG, "EPG BK3 (KEYCODE_PROG_RED)");
                    if(!Pvcfg.get_hideLauncherPvr()) {//eric lin 20251224 hide launcher pvr
                        interactionListener.onShowRecordingSetting(pg);
                    }
                }
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_PROG_GREEN) {
                if (interactionListener != null) {
                    if(!Pvcfg.get_hideLauncherPvr()) {//eric lin 20251229 hide reminder
                        interactionListener.onShowReminderMenu(pg);
                }
                }
                return true;
            }

            return false;
        });

        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (interactionListener != null) {
                    interactionListener.onFocus(true);
                    interactionListener.onFocusProgram(position, pg);
                }

                setLastFocusedPosition(position);
                holder.applyFocusStyle();
            } else {
                holder.applyNormalStyle();
            }

            if(!Pvcfg.get_hideLauncherPvr()) {//eric lin 20251229 hide reminder
                setIconState(holder.ivEpgProgramListReminder, hasFocus,
                    R.drawable.icon_miniguide_resv_watch_f, R.drawable.icon_miniguide_resv_watch_d);
            }
            if(!Pvcfg.get_hideLauncherPvr()) {//eric lin 20251224 hide launcher pvr
            setIconState(holder.ivEpgProgramListRecordSeries, hasFocus,
                    R.drawable.icon_miniguide_rec_resv_sereis_f, R.drawable.icon_miniguide_rec_resv_sereis);

            setIconState(holder.ivEpgProgramListRecord, hasFocus,
                    R.drawable.icon_miniguide_rec_resv_f, R.drawable.icon_miniguide_rec_resv);

            setIconState(holder.ivEpgProgramListRecording, hasFocus,
                    R.drawable.icon_miniguide_rec_f, R.drawable.icon_miniguide_rec);
            }
        });
    }

    @Override
    public int getItemCount() {
        return programList != null ? programList.size() : 0;
    }

    public void updateList(List<Program> newList, boolean isFocusBottom) {
        Log.d(TAG, "updateList newList size:" + newList.size());
        this.programList.clear();
        this.programList.addAll(newList);
        notifyDataSetChanged();

        if (!programList.isEmpty()) {
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

    public void drawSelectItem(int selectIndex) {
        lastFocusedPosition = selectIndex;
        for (int i = 0; i < recyclerView.getAdapter().getItemCount(); i++) {
            EpgProgramListAdapter.ViewHolder viewHolderAtPosition = (EpgProgramListAdapter.ViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
            if (viewHolderAtPosition != null) {
                if (i == selectIndex) {
                    viewHolderAtPosition.applySelectedStyle();
                } else {
                    viewHolderAtPosition.applyNormalStyle();
                }
            }
        }
    }

    public void updateListWithoutFocus(List<Program> newList) {
        Log.d(TAG, "updateListWithoutFocus newList size:" + newList.size());
        lastFocusedPosition = -1;
        this.programList.clear();
        this.programList.addAll(newList);
        notifyDataSetChanged();
    }

    private void setIconState(ImageView ivIcon, boolean hasFocus, int focusIcon, int defaultIcon) {
        if (ivIcon == null || !ivIcon.isShown()) {
            return;
        }

        ivIcon.setImageResource(hasFocus ? focusIcon : defaultIcon);
    }
}

