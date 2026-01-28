package com.prime.dmg.launcher.Settings;

import static com.prime.dmg.launcher.Settings.ChannelLockActivity.KEY_DELAY;
import static com.prime.dmg.launcher.Settings.ChannelLockActivity.LOCK;
import static com.prime.dmg.launcher.Settings.ChannelLockActivity.UNLOCK;
import static com.prime.dtv.sysdata.GposInfo.GPOS_TIME_LOCK_PERIOD_1_END;
import static com.prime.dtv.sysdata.GposInfo.GPOS_TIME_LOCK_PERIOD_1_START;
import static com.prime.dtv.sysdata.GposInfo.GPOS_TIME_LOCK_PERIOD_2_END;
import static com.prime.dtv.sysdata.GposInfo.GPOS_TIME_LOCK_PERIOD_2_START;
import static com.prime.dtv.sysdata.GposInfo.GPOS_TIME_LOCK_PERIOD_3_END;
import static com.prime.dtv.sysdata.GposInfo.GPOS_TIME_LOCK_PERIOD_3_START;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.hardware.face.IFaceService;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.prime.dmg.launcher.BaseActivity;
import com.prime.dmg.launcher.EPG.MiddleFocusRecyclerView;
import com.prime.dmg.launcher.HomeApplication;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Settings.Adapter.ChannelLockAdapter;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.service.datamanager.FavGroup;
import com.prime.dtv.sysdata.EnTableType;
import com.prime.dtv.sysdata.GposInfo;
import com.prime.dtv.sysdata.MiscDefine;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.utils.LogUtils;
import com.prime.dtv.utils.TVMessage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class TimeLockActivity extends BaseActivity implements PrimeDtv.DTVCallback {
    private static final String TAG = "TimeLockActivity";

    public static final String TIME_LOCK_PERIOD = "timeLockPeriod";

    private long g_last_key_event_time = 0;
    private TextView g_textv_subtitle, g_textv_channel_num, g_textv_locked_count_title_num;
    private TextView g_textv_done, g_textv_cancel, g_textv_start_time_input, g_textv_end_time_input, g_textv_time_error;
    private StringBuffer g_start_time_buffer, g_end_time_buffer;
    private View g_view_start_time_line, g_view_end_time_line;
    private RelativeLayout g_rltvl_channel_lock, g_rltvl_time_lock, g_rltvl_start_time_delete_frame, g_rltvl_end_time_delete_frame;
    private MiddleFocusRecyclerView g_rcv_lock, g_rcv_unlock;
    private ChannelLockAdapter g_adpt_lock, g_adpt_unlock;
    private List<ProgramInfo> g_list_lock, g_list_unlock;
    private LinearLayoutManager g_lnr_layout_manager_lock, g_lnr_layout_manager_unlock;
    private PrimeDtv g_prime_dtv;
    private int g_time_lock_index;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_lock);

        g_time_lock_index = getIntent().getIntExtra("index", -2);
        if (g_time_lock_index == -2) {
            Log.d(TAG, "onCreate: lock item index error, index:" + g_time_lock_index);
            finish();
            return;
        }

        init();
        set_listener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void init() {
        Log.d(TAG, "init: ");
        g_prime_dtv = HomeApplication.get_prime_dtv();

        g_textv_subtitle = findViewById(R.id.lo_time_lock_textv_subtitle);
        g_textv_done = findViewById(R.id.lo_time_lock_textv_done);
        g_textv_cancel = findViewById(R.id.lo_time_lock_textv_cancel);

        //time lock
        g_rltvl_time_lock = findViewById(R.id.lo_time_lock_rltvl_content);
        g_textv_start_time_input = findViewById(R.id.lo_time_lock_start_time_input);
        g_textv_end_time_input = findViewById(R.id.lo_time_lock_end_time_input);
        g_view_start_time_line = findViewById(R.id.lo_time_lock_start_time_line);
        g_view_end_time_line = findViewById(R.id.lo_time_lock_end_time_line);
        g_textv_time_error = findViewById(R.id.lo_time_lock_time_error);
        g_rltvl_start_time_delete_frame = findViewById(R.id.lo_time_lock_start_time_delete_frame);
        g_rltvl_end_time_delete_frame = findViewById(R.id.lo_time_lock_end_time_delete_frame);
        g_start_time_buffer = new StringBuffer();
        g_end_time_buffer = new StringBuffer();

        //channel lock
        g_rltvl_channel_lock = findViewById(R.id.lo_channel_lock_rltvl_content);
        g_textv_channel_num = findViewById(R.id.lo_channel_lock_textv_channel_num);
        g_textv_locked_count_title_num = findViewById(R.id.lo_channel_lock_textv_locked_count_title_num);
        g_rcv_lock = findViewById(R.id.lo_channel_lock_view_lock);
        g_rcv_unlock = findViewById(R.id.lo_channel_lock_view_unlock);

        if (g_time_lock_index == -1) {
            g_textv_cancel.setVisibility(View.GONE);
            set_time_lock_index();
        }
        else {
            g_textv_cancel.setVisibility(View.VISIBLE);
            set_start_time_and_end_time();
        }

        Log.d(TAG, "init: g_time_lock_index = " + g_time_lock_index);
        get_channel_data();
        set_channel_unlock_view();
        set_channel_lock_view();
    }

    private void set_listener() {
        g_textv_start_time_input.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                g_view_start_time_line.setBackgroundColor(getColor(R.color.pvr_red_color));
                g_rltvl_start_time_delete_frame.setVisibility(View.VISIBLE);
                g_rltvl_end_time_delete_frame.setVisibility(View.GONE);
            }
            else {
                g_view_start_time_line.setBackgroundColor(getColor(R.color.white));
                g_rltvl_start_time_delete_frame.setVisibility(View.GONE);
                g_rltvl_end_time_delete_frame.setVisibility(View.VISIBLE);
            }
        });
        g_textv_end_time_input.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                g_view_end_time_line.setBackgroundColor(getColor(R.color.pvr_red_color));
            else
                g_view_end_time_line.setBackgroundColor(getColor(R.color.white));
        });

        g_textv_done.setOnClickListener((v) -> {
            if (isChannelLockViewShow()) {
                Log.d(TAG, "set_listener: g_rltvl_channel_lock VISIBLE");
                save_channel_data();
                update_time_lock_period_to_gpos_info(false);
                finish();
            }
            else if (g_rltvl_time_lock.getVisibility() == View.VISIBLE) {
                Log.d(TAG, "set_listener: g_rltvl_time_lock VISIBLE");
                if (!check_time_correct())
                    g_textv_time_error.setVisibility(View.VISIBLE);
                else {
                    g_rltvl_time_lock.setVisibility(View.GONE);
                    g_rltvl_channel_lock.setVisibility(View.VISIBLE);
                    g_textv_subtitle.setText(R.string.dmg_settings_lock_channel_hint);
                    g_textv_done.setText(R.string.lock_done);
                    g_textv_cancel.setText(R.string.lock_cancel);
                    g_textv_channel_num.requestFocus();
                }
            }
        });

        g_textv_cancel.setOnClickListener((v) -> {
            if (isChannelLockViewShow())
                finish();
            else if (g_rltvl_time_lock.getVisibility() == View.VISIBLE) {
                delete_time_lock();
                save_channel_data();
                finish();
            }
        });

        View.OnKeyListener onKeyListener = (v, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN)
                return false;

            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && isChannelLockViewShow()) {
                if (g_adpt_lock.getItemCount() > 0)
                    g_rcv_lock.requestFocus();
                else
                    g_textv_channel_num.requestFocus();
                return true;
            }
            return false;
        };

        g_textv_done.setOnKeyListener(onKeyListener);
        g_textv_cancel.setOnKeyListener(onKeyListener);
    }

    private void set_time_lock_index() {
        GposInfo gposInfo = g_prime_dtv.gpos_info_get();

        for (int i = 0; i < 3; i++) {
            Log.d(TAG, "set_time_lock_index: getTimeLockPeriodStart = " + gposInfo.getTimeLockPeriodStart(i));
            if (gposInfo.getTimeLockPeriodStart(i) == -1) {
                g_time_lock_index = i;
                break;
            }
        }
    }

    private void set_start_time_and_end_time() {
        String timeLockPeriod = getIntent().getStringExtra(TIME_LOCK_PERIOD);
        if (timeLockPeriod == null || timeLockPeriod.isEmpty())
            Log.d(TAG, "set_start_time_and_end_time: timeLockPeriod == null or timeLockPeriod.isEmpty()");
        else {
            g_textv_start_time_input.setText(timeLockPeriod.substring(0, 5));
            g_textv_end_time_input.setText(timeLockPeriod.substring(6));
            g_start_time_buffer.append(timeLockPeriod.substring(0, 5));
            g_end_time_buffer.append(timeLockPeriod.substring(6));
        }
    }

    private void get_channel_data() {
        List<ProgramInfo> allChannels, TypeTvChannels, TypeRadioChannels;
        TypeTvChannels    = g_prime_dtv.get_program_info_list(FavGroup.ALL_TV_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.POS_ALL);
        LogUtils.d("TV number: "+ TypeTvChannels.size());
        TypeRadioChannels = g_prime_dtv.get_program_info_list(FavGroup.ALL_RADIO_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.POS_ALL);
        LogUtils.d("Radio number: "+ TypeRadioChannels.size());

        allChannels = new ArrayList<>();
        allChannels.addAll(TypeTvChannels);
        allChannels.addAll(TypeRadioChannels);
        allChannels.sort(Comparator.comparingInt(ProgramInfo::getDisplayNum));

        g_list_lock = new ArrayList<>();
        g_list_unlock = new ArrayList<>();

        set_channel_lock_by_time(allChannels);

        Log.d(TAG, "get_channel_data: g_list_lock size = " + g_list_lock.size());
        Log.d(TAG, "get_channel_data: g_list_unlock size = " + g_list_unlock.size());

        if (g_list_lock.size() == 0 && g_list_unlock.size() == 0) {
            Log.d(TAG, "get_channel_data: no data");
            finish();
        }
    }

    @SuppressLint("SetTextI18n")
    private void set_channel_lock_view() {
        g_rcv_lock.setHasFixedSize(true);
        ((SimpleItemAnimator) Objects.requireNonNull(g_rcv_lock.getItemAnimator())).setSupportsChangeAnimations(false);
        g_lnr_layout_manager_lock = new LinearLayoutManager(this);
        g_rcv_lock.setLayoutManager(g_lnr_layout_manager_lock);
        g_adpt_lock = new ChannelLockAdapter(g_list_lock, true,
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    update_channel_lock_list(false, (Integer) v.getTag());
                    g_textv_channel_num.setText("");
                }},
            new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() != KeyEvent.ACTION_DOWN)
                        return false;

                    if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        g_textv_channel_num.requestFocus();
                        return true;
                    }
                    return false;
                }
        });
        g_rcv_lock.setAdapter(g_adpt_lock);
        g_textv_locked_count_title_num.setText(Integer.toString(g_list_lock.size()));
    }

    private void set_channel_unlock_view() {
        g_rcv_unlock.setHasFixedSize(true);
        ((SimpleItemAnimator) Objects.requireNonNull(g_rcv_unlock.getItemAnimator())).setSupportsChangeAnimations(false);
        g_lnr_layout_manager_unlock = new LinearLayoutManager(this);
        g_rcv_unlock.setLayoutManager(g_lnr_layout_manager_unlock);
        g_adpt_unlock = new ChannelLockAdapter(g_list_unlock, false,
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    update_channel_lock_list(true, (Integer) v.getTag());
                    g_textv_channel_num.setText("");
                }
        }, new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() != KeyEvent.ACTION_DOWN)
                        return false;

                    if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {

                        if (g_adpt_lock.getItemCount() > 0) {
                            g_rcv_lock.requestFocus();
                            return true;
                        }
                    }
                    return false;
                }
        });
        g_rcv_unlock.setAdapter(g_adpt_unlock);
    }

    private void update_channel_lock_list(boolean isLock, int displayNum) {
        if (isLock) {
            for (int i = 0; i < g_list_unlock.size(); i++) {
                if (g_list_unlock.get(i).getDisplayNum() == displayNum) {
                    g_list_unlock.get(i).setTimeLockFlag(g_time_lock_index, LOCK);
                    g_list_lock.add(g_list_unlock.get(i));
                    g_list_lock.sort(Comparator.comparingInt(ProgramInfo::getDisplayNum));
                    g_list_unlock.remove(i);
                }
            }
        }
        else {
            for (int i = 0; i < g_list_lock.size(); i++) {
                if (g_list_lock.get(i).getDisplayNum() == displayNum) {
                    g_list_lock.get(i).setTimeLockFlag(g_time_lock_index, UNLOCK);
                    g_list_unlock.add(g_list_lock.get(i));
                    g_list_unlock.sort(Comparator.comparingInt(ProgramInfo::getDisplayNum));
                    g_list_lock.remove(i);
                }
            }
        }

        update_unlock_view();
        update_lock_view();
    }

    private void update_unlock_view() {
        Log.d(TAG, "update_unlock_view: g_list_unlock size = " + g_list_unlock.size());
        if (g_list_unlock == null)
            return;
        g_adpt_unlock.set_data(g_list_unlock);

        g_lnr_layout_manager_unlock.scrollToPositionWithOffset(0, 0);
    }

    @SuppressLint("SetTextI18n")
    private void update_lock_view() {
        Log.d(TAG, "update_lock_view: g_list_lock size = "+ g_list_lock.size());
        if (g_list_lock == null)
            return;
        g_adpt_lock.set_data(g_list_lock);
        g_textv_locked_count_title_num.setText(Integer.toString(g_list_lock.size()));
        g_lnr_layout_manager_lock.scrollToPositionWithOffset(0, 0);
    }

    @SuppressLint("SetTextI18n")
    private void set_number_in_text_view(int keyCode) {
        String number = String.valueOf(keyCode - KeyEvent.KEYCODE_0);
        if (isChannelLockViewShow()) {
            Log.d(TAG, "set_number_in_text_view:");
            String channelNum = g_textv_channel_num.getText() + number;
            g_textv_channel_num.requestFocus();
            g_textv_channel_num.setText(channelNum);
            List<ProgramInfo> zappingList = get_zapping_list(channelNum, g_list_unlock);
            update_unlock_view_by_zapping(zappingList);
        }
        else if (g_rltvl_time_lock.getVisibility() == View.VISIBLE) {
            hide_error_hint();
            set_time(number);
        }

    }

    private void update_unlock_view_by_zapping(List<ProgramInfo> zappingList) {
        Log.d(TAG, "update_unlock_view: zappingList size = " + zappingList.size());
        if (zappingList == null)
            return;
        g_adpt_unlock.set_data(zappingList);
        g_lnr_layout_manager_unlock.scrollToPositionWithOffset(0, 0);
    }

    private List<ProgramInfo> get_zapping_list(String channelNum, List<ProgramInfo> channelList) {
        List<ProgramInfo> zappingList = new ArrayList<>();

        for (ProgramInfo channelInfo : channelList) {
            String currNum = String.valueOf(channelInfo.getDisplayNum());
            if (currNum.contains(channelNum))
                zappingList.add(channelInfo);
        }

        return zappingList;
    }

    private void hide_error_hint() {
        if (g_textv_time_error.getVisibility() == View.VISIBLE) {
            g_textv_time_error.setVisibility(View.GONE);
        }
    }

    private void set_time(String number) {
        if (g_textv_start_time_input.isFocused()) {
            if (g_start_time_buffer.length() >= 5)
                return;
            g_start_time_buffer.append(number);
            if (g_start_time_buffer.length() == 2)
                g_start_time_buffer.append(":");
            g_textv_start_time_input.setText(g_start_time_buffer.toString());
        }
        else if (g_textv_end_time_input.isFocused()) {
            if (g_end_time_buffer.length() >= 5)
                return;
            g_end_time_buffer.append(number);
            if (g_end_time_buffer.length() == 2)
                g_end_time_buffer.append(":");
            g_textv_end_time_input.setText(g_end_time_buffer.toString());
        }
    }

    private void clear_text() {
        if (g_textv_start_time_input.isFocused()) {
            g_textv_start_time_input.setText("");
            g_start_time_buffer.delete(0,g_start_time_buffer.length());
            hide_error_hint();
        }
        else if (g_textv_end_time_input.isFocused()) {
            g_textv_end_time_input.setText("");
            g_end_time_buffer.delete(0,g_end_time_buffer.length());
            hide_error_hint();
        }
        else if (g_textv_channel_num.isFocused()) {
            if (g_textv_channel_num.getText().equals(""))
                return;

            g_textv_channel_num.setText("");
            update_unlock_view_by_zapping(g_list_unlock);
        }
    }

    private boolean check_time_correct() {
        if (g_textv_start_time_input.getText().toString().length() != 5
            || g_textv_end_time_input.getText().toString().length() != 5)
            return false;
        else if (!check_time_format())
            return false;
        else
            return true;
    }

    private void delete_time_lock() {
        Log.d(TAG, "delete_time_lock: index " + g_time_lock_index);
        update_time_lock_period_to_gpos_info(true);
        for (ProgramInfo programInfo: g_list_lock)
            programInfo.setTimeLockFlag(g_time_lock_index, UNLOCK);
    }

    private void save_channel_data() {
        for (ProgramInfo originProgramInfo: g_list_lock) {
            g_prime_dtv.update_program_info(originProgramInfo);
        }
        for (ProgramInfo originProgramInfo: g_list_unlock) {
            //Log.d(TAG, "set_listener: " + originProgramInfo.getDisplayName() + " timeLockFlag = " + originProgramInfo.getTimeLockFlag(-1));
            g_prime_dtv.update_program_info(originProgramInfo);
        }
        g_prime_dtv.save_table(EnTableType.PROGRAME);
        g_prime_dtv.save_table(EnTableType.GPOS);
    }

    private Boolean check_time_format() {
        String startTime = g_textv_start_time_input.getText().toString();
        String endTime = g_textv_end_time_input.getText().toString();

        Boolean isCorrect = true;
        if (!check_minute(startTime, endTime))
            isCorrect = false;
        else if (!check_hour(startTime, endTime))
            isCorrect = false;
        else if (trans_time_to_min(startTime) >= trans_time_to_min(endTime))
            isCorrect = false;
        return isCorrect;
    }

    private Boolean check_minute(String startTime, String endTime) {
        return Integer.parseInt(startTime.split(":")[1]) <= 59 && Integer.parseInt(endTime.split(":")[1]) <= 59;
    }

    private Boolean check_hour(String startTime, String endTime) {
        return Integer.parseInt(startTime.split(":")[0]) <= 24 && Integer.parseInt(endTime.split(":")[0]) <= 24;
    }

    private int trans_time_to_min(String time) {
        String[] splitTime = time.split(":");
        return (Integer.parseInt(splitTime[0]) * 60) + Integer.parseInt(splitTime[1]);
    }

    private void update_time_lock_period_to_gpos_info(boolean delete) {
        Log.d(TAG, "update_time_lock_period_to_gpos_info: index = " + g_time_lock_index);
        int startTimeValue = -1;
        int endTimeValue = -1;
        if (!delete) {
            //g_start_time_buffer format 11:22
            startTimeValue = Integer.parseInt(g_start_time_buffer.toString().substring(0, 2) + g_start_time_buffer.toString().substring(3, 5));
            endTimeValue = Integer.parseInt(g_end_time_buffer.toString().substring(0, 2) + g_end_time_buffer.toString().substring(3, 5));
        }
        Log.d(TAG, "update_time_lock_period_to_gpos_info: startTimeValue = " + startTimeValue + " endTimeValue = " + endTimeValue);

        if (g_time_lock_index == 0) {
            g_prime_dtv.gpos_info_update_by_key_string(GPOS_TIME_LOCK_PERIOD_1_START, startTimeValue);
            g_prime_dtv.gpos_info_update_by_key_string(GPOS_TIME_LOCK_PERIOD_1_END, endTimeValue);
        }
        else if (g_time_lock_index == 1) {
            g_prime_dtv.gpos_info_update_by_key_string(GPOS_TIME_LOCK_PERIOD_2_START, startTimeValue);
            g_prime_dtv.gpos_info_update_by_key_string(GPOS_TIME_LOCK_PERIOD_2_END, endTimeValue);
        }
        else if (g_time_lock_index == 2) {
            g_prime_dtv.gpos_info_update_by_key_string(GPOS_TIME_LOCK_PERIOD_3_START, startTimeValue);
            g_prime_dtv.gpos_info_update_by_key_string(GPOS_TIME_LOCK_PERIOD_3_END, endTimeValue);
        }
    }

    private void set_channel_lock_by_time(List<ProgramInfo> allChannels) {
        for (ProgramInfo programInfo: allChannels) {
            //Log.d(TAG, "set_channel_lock_by_time: " + programInfo.getDisplayName() + "timelockflag = " + programInfo.getTimeLockFlag(g_time_lock_index));
            if ((programInfo.getTimeLockFlag(g_time_lock_index) == 1)) {
                //Log.d(TAG, "set_channel_lock_by_time: lock");
                g_list_lock.add(programInfo);
            }
            else {
                //Log.d(TAG, "set_channel_lock_by_time: unlock");
                g_list_unlock.add(programInfo);
            }
        }
    }

    private boolean remove_character() {
        Log.d(TAG, "remove_character:");
        if (g_textv_channel_num.getText().length() > 0) {
            g_textv_channel_num.setText(g_textv_channel_num.getText().subSequence(0, g_textv_channel_num.getText().length() - 1));
            String channelNum = g_textv_channel_num.getText().toString();
            List<ProgramInfo> zappingList = get_zapping_list(channelNum, g_list_unlock);
            update_unlock_view_by_zapping(zappingList);
            return true;
        }
        return false;
    }

    private boolean isChannelLockViewShow() {
        return g_rltvl_channel_lock.getVisibility() == View.VISIBLE;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - g_last_key_event_time < KEY_DELAY)
            return true;

        g_last_key_event_time = currentTime;
        if (keyCode >= KeyEvent.KEYCODE_0  && keyCode <= KeyEvent.KEYCODE_9) {
            set_number_in_text_view(keyCode);
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
            clear_text();
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (!g_textv_channel_num.isFocused())
                return false;
            if (remove_character())
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBroadcastMessage(Context context, Intent intent) {
        super.onBroadcastMessage(context, intent);
    }

    @Override
    public void onMessage(TVMessage tvMessage) {
        super.onMessage(tvMessage);
        switch (tvMessage.getMsgType()) {
            case -1:
                break;
        }
    }
}
