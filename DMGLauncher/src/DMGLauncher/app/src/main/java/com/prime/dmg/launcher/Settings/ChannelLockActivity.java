package com.prime.dmg.launcher.Settings;

import static com.prime.dtv.sysdata.GposInfo.GPOS_CHANNEL_LOCK_COUNT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
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
import com.prime.dtv.sysdata.MiscDefine;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.utils.LogUtils;
import com.prime.dtv.utils.TVMessage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ChannelLockActivity  extends BaseActivity implements PrimeDtv.DTVCallback {
    private static final String TAG = "ChannelLockActivity";
    public static final int KEY_DELAY = 100; // 300ms 間隔
    public static final int LOCK = 1, UNLOCK = 0;
    private long lastKeyEventTime = 0;

    private TextView g_textv_done, g_textv_cancel, g_textv_channel_num, g_textv_locked_count_title_num;
    private MiddleFocusRecyclerView g_rcv_lock, g_rcv_unlock;
    private ChannelLockAdapter g_adpt_lock, g_adpt_unlock;
    private List<ProgramInfo> g_list_lock, g_list_unlock, g_list_origin;
    private LinearLayoutManager g_lnr_layout_manager_lock, g_lnr_layout_manager_unlock;
    private PrimeDtv g_prime_dtv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_lock);

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

        g_textv_done = findViewById(R.id.lo_channel_lock_textv_done);
        g_textv_cancel = findViewById(R.id.lo_channel_lock_textv_cancel);
        g_textv_channel_num = findViewById(R.id.lo_channel_lock_textv_channel_num);
        g_textv_locked_count_title_num = findViewById(R.id.lo_channel_lock_textv_locked_count_title_num);
        g_rcv_lock = findViewById(R.id.lo_channel_lock_view_lock);
        g_rcv_unlock = findViewById(R.id.lo_channel_lock_view_unlock);

        get_channel_data();
        set_channel_unlock_view();
        set_channel_lock_view();
    }

    private void set_listener() {
        g_textv_done.setOnClickListener(v -> {
            for (ProgramInfo originProgramInfo: g_list_lock)
                g_prime_dtv.update_program_info(originProgramInfo);
            for (ProgramInfo originProgramInfo: g_list_unlock)
                g_prime_dtv.update_program_info(originProgramInfo);
            g_prime_dtv.save_table(EnTableType.PROGRAME);
            g_prime_dtv.gpos_info_update_by_key_string(GPOS_CHANNEL_LOCK_COUNT, Integer.parseInt(g_textv_locked_count_title_num.getText().toString()));
            finish();
        });

        g_textv_cancel.setOnClickListener(v -> {
            finish();
        });

        View.OnKeyListener onKeyListener = (v, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN)
                return false;

            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
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
        g_list_origin = allChannels;
        for (ProgramInfo programInfo: allChannels) {
            if (programInfo.getLock() == LOCK)
                g_list_lock.add(programInfo);
            else
                g_list_unlock.add(programInfo);
        }
        Log.d(TAG, "get_channel_data: g_list_lock size = " + g_list_lock.size());
        Log.d(TAG, "get_channel_data: g_list_unlock size = " + g_list_unlock.size());
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
                }
        },  new View.OnKeyListener() {
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
        },  new View.OnKeyListener() {
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
                    g_list_unlock.get(i).setLock(LOCK);
                    g_list_lock.add(g_list_unlock.get(i));
                    g_list_lock.sort(Comparator.comparingInt(ProgramInfo::getDisplayNum));
                    g_list_unlock.remove(i);
                }
            }
        }
        else {
            for (int i = 0; i < g_list_lock.size(); i++) {
                if (g_list_lock.get(i).getDisplayNum() == displayNum) {
                    g_list_lock.get(i).setLock(UNLOCK);
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

    private void clear_text() {
        if (g_textv_channel_num.isFocused()) {
            if (g_textv_channel_num.getText().equals(""))
                return;

            g_textv_channel_num.setText("");
            update_unlock_view_by_zapping(g_list_unlock);
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

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastKeyEventTime < KEY_DELAY)
            return true;

        lastKeyEventTime = currentTime;
        if (keyCode >= KeyEvent.KEYCODE_0  && keyCode <= KeyEvent.KEYCODE_9) {
            String channelNum = g_textv_channel_num.getText() + String.valueOf(keyCode - KeyEvent.KEYCODE_0);
            g_textv_channel_num.requestFocus();
            g_textv_channel_num.setText(channelNum);
            List<ProgramInfo> zappingList = get_zapping_list(channelNum, g_list_unlock);
            update_unlock_view_by_zapping(zappingList);
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
