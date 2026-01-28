package com.prime.launcher.PVR.Management;

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

import com.prime.launcher.CustomView.HintBanner;
import com.prime.launcher.CustomView.Snakebar;
import com.prime.launcher.Home.Hotkey.HotkeyRecord;
import com.prime.launcher.Home.LiveTV.MiniEPG;
import com.prime.launcher.R;
import com.prime.launcher.PrimeDtv;
import com.prime.datastructure.sysdata.EPGEvent;
import com.prime.datastructure.sysdata.PvrRecFileInfo;
import com.prime.datastructure.sysdata.PvrRecIdx;

import java.lang.ref.WeakReference;
import java.util.List;

public class RecordListAdapter extends RecyclerView.Adapter<RecordListAdapter.Holder> {

    private static final String TAG = RecordListAdapter.class.getSimpleName();

    private final WeakReference<AppCompatActivity> g_ref;
    private final RecordListView g_RecordListView;
    private List<PvrRecFileInfo> g_RecordList;
    private PvrRecFileInfo g_CurrentRecord;
    private boolean g_IsSeriesList;
    private int g_FocusPosition;
    private long g_previousMs;

    public RecordListAdapter(AppCompatActivity activity) {
        g_ref = new WeakReference<>(activity);
        g_RecordListView = get_activity().findViewById(R.id.record_program_list);
        g_RecordList = get_activity().get_record_list();
        g_IsSeriesList = false;
        g_FocusPosition = 0;
    }

    @NonNull
    @Override
    public RecordListAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_record_list, parent, false);
        return new Holder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        on_click_record_file(holder, position);
        on_focus_record_file(holder, position);
        on_key_record_file(holder, position);
        setup_record_item_view(holder, position);
    }

    void on_click_record_file(Holder holder, int position) {
        holder.itemView.setOnClickListener(v -> {
            PvrRecFileInfo recordInfo = g_RecordList.get(position);
            boolean isSeries = recordInfo.isSeries();

            if (isSeries)
                on_press_series(holder, position);
            else
                on_press_item(holder, position);
        });
    }

    void on_focus_record_file(Holder holder, int position) {
        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                setup_record_info(position);
                setup_bottom_banner(position);
                scroll_to_middle(holder.itemView);
            }
            else
                g_RecordListView.set_previous(position);
        });
    }

    void on_key_record_file(Holder holder, int position) {
        holder.itemView.setOnKeyListener((v, keyCode, event) -> {
            if (KeyEvent.ACTION_DOWN != event.getAction())
                return false;

            return switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP,
                     KeyEvent.KEYCODE_DPAD_DOWN ->  on_press_dpad(position);
                case KeyEvent.KEYCODE_BACK ->       on_press_back(position);
                case KeyEvent.KEYCODE_PROG_BLUE ->  on_press_delete(position);
                case KeyEvent.KEYCODE_PROG_RED ->   on_press_fullscreen(position);
                default -> false;
            };
        });
    }

    void on_press_series(Holder holder, int position) {
        Log.d(TAG, "on_press_series: ");
        HintBanner hintBanner = get_activity().findViewById(R.id.record_program_list_hint_banner);
        hintBanner.show_back();
        PvrRecFileInfo recordInfo = g_RecordList.get(position);
        g_RecordList = get_activity().get_series_record_list(recordInfo);
        g_IsSeriesList = true;
        g_FocusPosition = g_RecordListView.get_position();
        hide_all_series_icon();
        notifyItemRangeChanged(0, g_RecordList.size());

        // focus first item
        View firstView = g_RecordListView.getChildAt(0);
        if (firstView != null)
            firstView.requestFocus();
    }

    void on_press_item(Holder holder, int position) {
        Log.d(TAG, "on_press_item: ");
        RecordProgramsActivity activity = get_activity();
        g_CurrentRecord = g_RecordList.get(position);

        if (from_last_position()) {
            HotkeyRecord confirmDialog = new HotkeyRecord(activity) {
                @Override
                public void on_click_confirm(View v) {
                    super.on_click_confirm(v);
                    activity.start_playback(g_CurrentRecord, false, true); // on_press_item - from last position:true - confirmDialog
                }

                @Override
                public void on_click_cancel(View v) {
                    super.on_click_cancel(v);
                    activity.start_playback(g_CurrentRecord, false, false); // on_press_item - from last position:false - confirmDialog
                }
            };
            confirmDialog.show_panel(R.string.pvr_would_you_like_to_resume_playback_from_where_you_left_off, true);
            return;
        }

        activity.start_playback(g_CurrentRecord, false, false); // on_press_item
    }

    boolean on_press_dpad(int position) {
        //Log.d(TAG, "on_press_dpad: ");
        return false;
    }

    boolean on_press_back(int position) {
        if (g_IsSeriesList) {
            HintBanner hintBanner = get_activity().findViewById(R.id.record_program_list_hint_banner);
            hintBanner.hide_back();
            g_RecordList = get_activity().get_record_list();
            g_IsSeriesList = false;
            notifyItemRangeChanged(0, g_RecordList.size());
            return true;
        }

        return false;
    }

    boolean on_press_delete(int position) {
        if (null == g_RecordList || g_RecordList.isEmpty() || null == g_RecordListView)
            return false;

        //int position = g_RecordListView.get_position();
        if (position < 0 || position >= g_RecordList.size())
            return false;

        RecordProgramsActivity activity = get_activity();
        PvrRecFileInfo recordInfo = g_RecordList.get(position);
        boolean deleteSuccess = activity.delete_record(recordInfo);

        if (deleteSuccess) {
            Log.d(TAG, "on_press_delete: successfully delete record");
            setup_record_info(new PvrRecFileInfo());
            g_RecordList.remove(position);

            if (is_empty_series()) {
                Log.d(TAG, "on_press_delete: empty series, notify item removed");
                HintBanner hintBanner = activity.findViewById(R.id.record_program_list_hint_banner);
                hintBanner.hide_back();
                g_RecordList = activity.get_record_list();
                g_RecordList.remove(g_FocusPosition);
                activity.fill_list_size_to_6(g_RecordList);
                g_IsSeriesList = false;
                notifyItemRemoved(g_FocusPosition);
                notifyItemRangeChanged(0, g_RecordList.size());
            }
            else {
                Log.d(TAG, "on_press_delete: delete record, notify item removed");
                activity.fill_list_size_to_6(g_RecordList);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, g_RecordList.size());
            }

            activity.setup_space_info();
        }
        else
            Snakebar.show(get_activity(), "Delete record failed", Snakebar.LENGTH_SHORT);

        return deleteSuccess;
    }

    boolean on_press_fullscreen(int position) {
        RecordProgramsActivity activity = get_activity();
        g_CurrentRecord = g_RecordList.get(position);
        boolean isSeries = g_CurrentRecord.isSeries();

        if (isSeries) {
            Log.w(TAG, "on_press_fullscreen: is series, return");
            return false;
        }

        if (from_last_position()) {
            HotkeyRecord confirmDialog = new HotkeyRecord(activity) {
                @Override
                public void on_click_confirm(View v) {
                    super.on_click_confirm(v);
                    activity.start_playback(g_CurrentRecord, true, true); // on_press_fullscreen - confirmDialog
                }

                @Override
                public void on_click_cancel(View v) {
                    super.on_click_cancel(v);
                    activity.start_playback(g_CurrentRecord, true, false); // on_press_fullscreen - confirmDialog
                }
            };
            confirmDialog.show_panel(R.string.pvr_would_you_like_to_resume_playback_from_where_you_left_off, true);
            return true;
        }

        Log.d(TAG, "on_press_fullscreen: play record");
        return activity.start_playback(g_CurrentRecord, true, false); // on_press_fullscreen
    }

    void setup_record_item_view(Holder holder, int position) {
        TextView recordFileName = holder.itemView.findViewById(R.id.record_file_name);
        TextView recordFileTime = holder.itemView.findViewById(R.id.record_file_time);
        ImageView recordSeries = holder.itemView.findViewById(R.id.record_series);
        TextView timePrefix = holder.itemView.findViewById(R.id.record_file_time_prefix);
        PvrRecFileInfo recordInfo = g_RecordList.get(position);
        boolean isEmpty = recordInfo.getChannelId() == 0;

        if (isEmpty) {
            timePrefix.setVisibility(View.INVISIBLE);
            recordFileName.setText("");
            recordFileTime.setText("");
            recordSeries.setVisibility(View.INVISIBLE);
            holder.itemView.setFocusable(false);
            return;
        }

        holder.itemView.setFocusable(true);
        timePrefix.setVisibility(View.VISIBLE);
        recordFileName.setText(get_record_name(position));
        recordFileTime.setText(MiniEPG.ms_to_time(recordInfo.getStartTime(), "yyyy/MM/dd HH:mm:ss"));
        recordSeries.setVisibility(recordInfo.isSeries() ? View.VISIBLE : View.INVISIBLE);

        if (position == 0)
            setup_record_info(position);

        if (focus_previous_position(holder, position))
            Log.d(TAG, "set_record_item_view: focus previous [position] " + position);
    }

    void setup_record_info(int position) {
        RecordProgramsActivity activity = get_activity();
        TextView recordName = activity.findViewById(R.id.record_name);
        TextView recordTimeLength = activity.findViewById(R.id.record_time_length);
        TextView recordDetailInfo = activity.findViewById(R.id.record_detail_info);
        PvrRecFileInfo recordInfo = g_RecordList.get(position);
        EPGEvent epgEvent = recordInfo.getEpgEvent();
        String eventName = recordInfo.getEventName();
        String description = "";
        String totalTime = "";

        if (epgEvent != null) {
            long channelId = recordInfo.getChannelId();
            int eventId = epgEvent.get_event_id();
            description = activity.get_description(channelId, eventId);
        }

        if (!recordInfo.isSeries() && eventName != null && !eventName.isEmpty())
            totalTime = MiniEPG.sec_to_duration(recordInfo.getTotalRecordTime());

        recordName.setText(eventName);
        recordTimeLength.setText(totalTime);
        recordDetailInfo.setText(description);
    }

    void setup_record_info(PvrRecFileInfo recordInfo) {
        RecordProgramsActivity activity = get_activity();
        TextView recordName = activity.findViewById(R.id.record_name);
        TextView recordTimeLength = activity.findViewById(R.id.record_time_length);
        TextView recordDetailInfo = activity.findViewById(R.id.record_detail_info);
        EPGEvent epgEvent = recordInfo.getEpgEvent();
        String eventName = recordInfo.getEventName();
        String description = "";
        String totalTime = "";

        if (epgEvent != null) {
            long channelId = recordInfo.getChannelId();
            int eventId = epgEvent.get_event_id();
            description = activity.get_description(channelId, eventId);
        }

        if (!recordInfo.isSeries() && eventName != null && !eventName.isEmpty())
            totalTime = MiniEPG.sec_to_duration(recordInfo.getTotalRecordTime());

        recordName.setText(eventName);
        recordTimeLength.setText(totalTime);
        recordDetailInfo.setText(description);
    }

    void setup_bottom_banner(int position) {
        RecordProgramsActivity activity = get_activity();
        HintBanner hintBanner = activity.findViewById(R.id.record_program_list_hint_banner);
        PvrRecFileInfo recordInfo = g_RecordList.get(position);
        boolean isSeries = recordInfo.isSeries();

        hintBanner.set_hint_ok(isSeries ? R.string.detail_open : R.string.hint_rcu_play);
        hintBanner.set_hint_fullscreen(!isSeries);
    }

    RecordProgramsActivity get_activity() {
        return (RecordProgramsActivity) g_ref.get();
    }

    @Override
    public int getItemCount() {
        return g_RecordList.size();
    }

    public int get_focus_position() {
        return g_FocusPosition;
    }

    public PvrRecFileInfo get_current_record() {
        Log.d(TAG, "get_current_record: ");
        return g_CurrentRecord;
    }

    public String get_record_name(int position) {

        if (null == g_RecordList || g_RecordList.isEmpty() || position < 0 || position >= g_RecordList.size())
            return get_activity().getString(R.string.epg_no_data);

        PvrRecFileInfo recordInfo = g_RecordList.get(position);

        if (null == recordInfo)
            return get_activity().getString(R.string.epg_no_data);

        String chNum = Integer.toString(recordInfo.getChannelNo());
        String chName = recordInfo.getChName();
        String eventName = recordInfo.getEventName();
        String recordName = chNum + " " + chName;

        if (eventName != null && !eventName.isEmpty())
            recordName += " - " + eventName;

        return recordName;
    }

    public long get_time_diff() {
        long timeDiff = System.currentTimeMillis() - g_previousMs;
        g_previousMs = System.currentTimeMillis();
        return timeDiff;
    }

    public boolean is_series_list() {
        return g_IsSeriesList;
    }

    boolean is_empty_series() {
        for (PvrRecFileInfo recordInfo : g_RecordList) {
            if (recordInfo.getEventName() != null && !recordInfo.getEventName().isEmpty())
                return false;
        }
        return g_IsSeriesList;
    }

    /** @noinspection ConstantValue*/
    public void scroll_to_middle(View itemView) { // move_to_middle
        final boolean DEBUG_LOG = false;
        int position = g_RecordListView.get_position();
        int previous = g_RecordListView.get_previous();

        if (DEBUG_LOG)
            Log.e(TAG, "scroll_to_middle: [position] " + position + ", [previous] " + previous);

        if (position == previous)
            return;

        int[] location = new int[2];
        itemView.getLocationOnScreen(location);
        int itemCenterY = location[1] + itemView.getHeight() / 2;
        int screenCenterY = g_RecordListView.getHeight() / 2 + get_activity().dp_to_px(235);
        //int screenCenterX = itemView.getParent().getResources().getDisplayMetrics().widthPixels / 2;
        int offsetY = itemCenterY - screenCenterY;
        long timeDiff = get_time_diff();

        if (DEBUG_LOG)
            Log.e(TAG, "scroll_to_middle: itemCenterY = " + itemCenterY + ", screenCenterY = " + screenCenterY + ", timeDiff = " + timeDiff);

        if (timeDiff < 100)
            g_RecordListView.scrollBy(0, offsetY);
        else
            g_RecordListView.smoothScrollBy(0, offsetY);

        if (DEBUG_LOG && offsetY < 0)   Log.d(TAG, "scroll_to_middle: move up,   [offset] " + offsetY);
        if (DEBUG_LOG && offsetY > 0)   Log.d(TAG, "scroll_to_middle: move down, [offset] " + offsetY);
    }

    boolean focus_previous_position(Holder holder, int position) {
        if (position == g_FocusPosition && !g_IsSeriesList) {
            holder.itemView.requestFocus();
            return true;
        }
        return false;
    }

    boolean from_last_position() {
        RecordProgramsActivity activity = get_activity();
        PrimeDtv primeDtv = activity.get_prime_dtv();
        int masterIndex = g_RecordListView.get_master_index();
        int seriesIndex = g_RecordListView.get_series_index();
        return !primeDtv.pvr_PlayCheckLastPositionPoint(new PvrRecIdx(masterIndex, seriesIndex));
    }

    void hide_all_series_icon() {
        Log.d(TAG, "hide_all_series_icon: ");
        RecordProgramsActivity activity = get_activity();
        RecordListView recordListView = activity.findViewById(R.id.record_program_list);
        for (int i = 0; i < recordListView.get_count(); i++) {
            View itemView = recordListView.getChildAt(i);
            if (itemView != null) {
                View seriesIcon = itemView.findViewById(R.id.record_series);
                if (seriesIcon != null)
                    seriesIcon.setVisibility(View.INVISIBLE);
            }
        }
    }

    public static class Holder extends RecyclerView.ViewHolder {
        public Holder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
