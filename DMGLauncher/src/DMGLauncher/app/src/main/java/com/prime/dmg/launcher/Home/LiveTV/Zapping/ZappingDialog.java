package com.prime.dmg.launcher.Home.LiveTV.Zapping;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.prime.dmg.launcher.CustomView.MessageDialog;
import com.prime.dmg.launcher.EPG.EpgActivity;
import com.prime.dmg.launcher.Home.LiveTV.LiveTvManager;
import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.PVR.Management.TimerManagementActivity;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.BaseDialog;
import com.prime.dtv.ChannelChangeManager;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.utils.TVMessage;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** @noinspection CommentedOutCode*/
public class ZappingDialog extends BaseDialog {

    String TAG = ZappingDialog.class.getSimpleName();

    static final int MAX_CHANNEL_NUM_LENGTH  = 3;
    static final int DELAY_ZAPPING_DIALOG    = 5000;

    WeakReference<AppCompatActivity> g_ref;
    LiveTvManager   g_liveTvMgr;
    ZappingAdapter  g_zappingAdapter;
    Handler         g_zappingHandler;

    String            g_channelNum;
    List<ProgramInfo> g_channelList;
    List<ProgramInfo> g_zappingList;

    static class ZappingList {
        static boolean g_updating_zapping_list;

        public static boolean is_updating() {
            return g_updating_zapping_list;
        }
    }

    public ZappingDialog(@NonNull LiveTvManager liveTvMgr) {
        super(liveTvMgr.get(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        g_liveTvMgr = liveTvMgr;
        g_ref = new WeakReference<>(liveTvMgr.get());
    }

    public ZappingDialog(@NonNull LiveTvManager liveTvMgr, String chNum, List<ProgramInfo> chList) {
        super(liveTvMgr.get(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        g_liveTvMgr   = liveTvMgr;
        g_ref = new WeakReference<>(liveTvMgr.get());
        g_channelList = chList;
        g_channelNum  = chNum;
        g_zappingList = get_zapping_list(g_channelNum, g_channelList);
    }

    public ZappingDialog(@NonNull EpgActivity epgActivity, List<ProgramInfo> chList) {
        super(epgActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        g_ref = new WeakReference<>(epgActivity);
        g_channelList = chList;
        //g_channelNum  = chNum;
        //g_zappingList = get_zapping_list(g_channelNum, g_channelList);
    }

    public ZappingDialog(@NonNull EpgActivity epgActivity, String chNum, List<ProgramInfo> chList) {
        super(epgActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        g_ref = new WeakReference<>(epgActivity);
        g_channelList = chList;
        g_channelNum  = chNum;
        g_zappingList = get_zapping_list(g_channelNum, g_channelList);
    }

    public ZappingDialog(@NonNull TimerManagementActivity timerManagementActivity, String chNum, List<ProgramInfo> chList) {
        super(timerManagementActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        g_ref = new WeakReference<>(timerManagementActivity);
        g_channelList = chList;
        g_channelNum  = chNum;
        g_zappingList = g_channelList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.color.trans);
            //getWindow().setWindowAnimations(R.style.Theme_DMGLauncher_DialogAnimation);
        }
        setContentView(R.layout.live_tv_zapping_view_dialog);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (get() instanceof HomeActivity)
            g_liveTvMgr.g_miniEPG.hide_info();

        // channel number
        set_channel_num(g_channelNum);

        // channel list
        set_zapping_list_view(g_zappingList);

        // delay
        set_delay(DELAY_ZAPPING_DIALOG);
    }

    @Override
    public void onMessage(TVMessage msg) {

    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        //Log.e(TAG, "onKeyDown: [keycode] " + keyCode + " " + getCurrentFocus());

        if (is_number(keyCode)) {
            set_channel_num(keyCode);
            set_zapping_list();
            set_delay(DELAY_ZAPPING_DIALOG);
        }
        if (KeyEvent.KEYCODE_DPAD_UP == keyCode)
            set_delay(DELAY_ZAPPING_DIALOG);
        if (KeyEvent.KEYCODE_DPAD_DOWN == keyCode)
            set_delay(DELAY_ZAPPING_DIALOG);

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (del_channel_num())
            return;
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        g_zappingHandler.removeCallbacksAndMessages(null);
        g_zappingHandler = null;
    }

    public AppCompatActivity get() {
        return g_ref.get();
    }

    public List<ProgramInfo> get_zapping_list(String chNum, List<ProgramInfo> chList) {
        Log.d(TAG, "get_zapping_list: chNum = " + chNum);
        List<ProgramInfo> zappingList = new ArrayList<>();

        for (ProgramInfo channel : chList) {
            //String currNum = channel.getDisplayNum(MAX_CHANNEL_NUM_LENGTH);
            String currNum = String.format(
                    Locale.getDefault(),
                    "%0" + MAX_CHANNEL_NUM_LENGTH +"d",
                    channel.getDisplayNum()); // e.g. display num = "2" -> "002"
            if (currNum.contains(chNum)) {
                zappingList.add(channel);
            }
        }

        return zappingList;
    }

    public void set_delay(int delay) {
        if (null == g_zappingHandler)
            g_zappingHandler = new Handler(Looper.getMainLooper());
        g_zappingHandler.removeCallbacksAndMessages(null);

        if (get() instanceof TimerManagementActivity)
            return;

        g_zappingHandler.postDelayed(this::change_channel, delay);
    }

    public void set_zapping_list() {
        ZappingList.g_updating_zapping_list = true;
        new Thread(() -> {
            List<ProgramInfo> zappingList = get_zapping_list(g_channelNum, g_channelList);
            get().runOnUiThread(() -> {
                g_zappingList.clear();
                g_zappingList.addAll(zappingList);
                ZappingList.g_updating_zapping_list = false;
                set_zapping_list_view(g_zappingList);
            });
            //set_zapping_list_view(g_zappingList);
        }, "Set Zapping List").start();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void set_zapping_list_view(List<ProgramInfo> zappingList) {
        ZappingListView     zappingListView = findViewById(R.id.lo_zapping_list);
        LinearLayoutManager layoutManager   = new LinearLayoutManager(get(), RecyclerView.VERTICAL, false);

        if (null == g_zappingAdapter) {
            g_zappingAdapter = new ZappingAdapter(this, zappingList);
            zappingListView.setLayoutManager(layoutManager);
            //zappingListView.setItemViewCacheSize(ZappingListView.ITEM_VIEW_CACHE_SIZE);
            zappingListView.setAdapter(g_zappingAdapter);
            return;
        }

        Log.d(TAG, "set_zapping_list_view: update zapping list");
        g_zappingAdapter.set_zapping_list(zappingList);
        g_zappingAdapter.notifyDataSetChanged();
        zappingListView.scrollToPosition(0);
        zappingListView.requestFocus();
    }

    public void set_channel_num(int keyCode) {
        String chNum = String.valueOf(keyCode - KeyEvent.KEYCODE_0);

        if (g_channelNum.length() >= MAX_CHANNEL_NUM_LENGTH)
            g_channelNum = "";
        g_channelNum = g_channelNum.concat(chNum);

        set_channel_num(g_channelNum);
    }

    public void set_channel_num(String chNum) {
        TextView chNumView = findViewById(R.id.lo_zapping_ch_num);
        chNumView.setText(chNum);
    }

    public void set_all_data(String chNum, List<ProgramInfo> chList) {
        Log.d(TAG, "set_all_data: chNum = " + chNum);
        g_channelList = chList;
        g_channelNum  = chNum;
        g_zappingList = get_zapping_list(g_channelNum, g_channelList);
    }

    public boolean del_channel_num() {
        int chNumLength = g_channelNum.length();
        int chNumEnd    = chNumLength - 1;

        if (chNumLength == 1 || chNumLength == 0)
            return false;

        g_channelNum = g_channelNum.substring(0, chNumEnd);
        set_channel_num(g_channelNum);
        set_zapping_list();
        set_delay(DELAY_ZAPPING_DIALOG);
        return true;
    }

    public boolean is_number(int keyCode) {
        return KeyEvent.KEYCODE_0 == keyCode || KeyEvent.KEYCODE_1 == keyCode || KeyEvent.KEYCODE_2 == keyCode || KeyEvent.KEYCODE_3 == keyCode ||
               KeyEvent.KEYCODE_4 == keyCode || KeyEvent.KEYCODE_5 == keyCode || KeyEvent.KEYCODE_6 == keyCode || KeyEvent.KEYCODE_7 == keyCode ||
               KeyEvent.KEYCODE_8 == keyCode || KeyEvent.KEYCODE_9 == keyCode;
    }

    public void change_channel(View itemView, int position, boolean closeDialog) {
        if (g_zappingList == null || position < 0 || position >= g_zappingList.size()) {
            Log.e(TAG, "change_channel: invalid position");
            return;
        }

        ChannelChangeManager changeMgr = ChannelChangeManager.get_instance(get());
        ProgramInfo curChannel = changeMgr.get_cur_channel();
        ProgramInfo newChannel = g_zappingList.get(position);

        // check full recording
        if (changeMgr.is_full_recording() && newChannel != null && !changeMgr.is_channel_recording(newChannel.getChannelId())) {
            MessageDialog dialog = new MessageDialog(get());
            dialog.set_content_message(R.string.pvr_zapping_channel_but_recording_is_full);
            dialog.set_cancel_visible(true);
            dialog.set_confirm_action(() -> changeMgr.pvr_record_stop(changeMgr.get_last_record_ch_id()));
            dialog.show_panel();

            // close dialog
            super.dismiss();
            return;
        }

        // change channel
        if (null == newChannel || null == curChannel)
            Log.e(TAG, "change_channel: channel is NULL");
        else if (newChannel.getChannelId() == curChannel.getChannelId())
            Log.e(TAG, "change_channel: same channel, do not change channel");
        else {
            // change channel
            if (get() instanceof HomeActivity) {
                Log.d(TAG, "change_channel: by Live TV, [channel] " + newChannel.getDisplayNum() + " " + newChannel.getDisplayName());
                changeMgr.set_previous_channel(curChannel);
                changeMgr.set_cur_ch_id(newChannel.getChannelId());
                LiveTvManager.change_channel(g_liveTvMgr, newChannel, true, true);
            }
            else if (get() instanceof EpgActivity epgActivity) {
                Log.d(TAG, "change_channel: by EPG, [channel] " + newChannel.getDisplayNum() + " " + newChannel.getDisplayName());
                changeMgr.set_previous_channel(epgActivity.get_list_layer().get_current_channel_info());
                changeMgr.set_cur_ch_id(newChannel.getChannelId());
                epgActivity.change_channel_from_zapping(newChannel);
            }
            else if (get() instanceof TimerManagementActivity timerManagementActivity) {
                Log.d(TAG, "change_channel: by Timer Management, [channel] " + newChannel.getDisplayNum() + " " + newChannel.getDisplayName());
                timerManagementActivity.change_channel_from_zapping(newChannel);
            }
        }

        // close dialog
        if (closeDialog)
            super.dismiss();
    }

    public void change_channel() {
        // change channel if close dialog
        ZappingListView zappingListView = findViewById(R.id.lo_zapping_list);
        View itemView = zappingListView.getFocusedChild();
        //int position = zappingListView.get_position();
        int position = 0;
        change_channel(itemView, position, false);

        // close dialog
        if (isShowing())
            super.dismiss();
    }
}
