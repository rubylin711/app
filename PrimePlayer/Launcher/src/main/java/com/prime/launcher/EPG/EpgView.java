package com.prime.launcher.EPG;

import static com.prime.launcher.Settings.UnlockChannelActivity.PASSWORD_STAR;
import static com.prime.datastructure.sysdata.ProgramInfo.PROGRAM_RADIO;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prime.launcher.CustomView.MessageDialog;
import com.prime.launcher.Home.BlockChannel.BlockedChannel;
import com.prime.launcher.HomeApplication;
import com.prime.launcher.R;
import com.prime.launcher.Utils.Utils;
import com.prime.launcher.ChannelChangeManager;
import com.prime.launcher.PrimeDtv;
import com.prime.datastructure.ServiceDefine.AvCmdMiddle;
import com.prime.datastructure.sysdata.EPGEvent;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.ProgramInfo;

import java.util.List;
import java.util.Locale;

/** @noinspection CommentedOutCode*/
public class EpgView extends RelativeLayout implements BlockedChannel.Callback {
    private static final String TAG = EpgView.class.getSimpleName();

    private Handler gHandler;
    private Epg g_epg;
    private EpgDetailView gDetailView;
    private ListLayer gListLayer;
    private TextView g_unlock_title_text1;
    private TextView g_unlock_title_text2;
    private TextView g_unlock_title_text3;
    private TextView g_unlock_pass_text;
    private View g_pass_text_bar,g_epgBlockedChannel_bg;
    private TextView g_pass_fail;
    private PrimeDtv gPrimeDtv;
    private ChannelChangeManager gChangeMgr;
    private BlockedChannel g_blocked_channel;
    private String g_current_input_pass = "";
    private long g_curr_focus_channel_id;
    private int g_curr_focus_service_id;
    public long gPrevChId;
    public static boolean isChangeChannel = false;
    public MessageDialog g_pvrMsgDialog;
    public static ProgramInfo channel_of_change_genre;
    private boolean isE200Showing = false;

    public EpgView(Context context) {
        super(context);
        init_view();
    }

    public EpgView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init_view();
    }

    public EpgView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init_view();
    }

    private void check_password() {
        Log.d(TAG, "check_password: g_curr_focus_channel_id = "+g_curr_focus_channel_id);
        GposInfo gposInfo = gPrimeDtv.gpos_info_get();
        String passwordFromGposInfo = String.format(Locale.US, "%04d", gposInfo.getPasswordValue());
        g_blocked_channel.stop_blocked_channel_check();
        if(g_current_input_pass.equals(passwordFromGposInfo)) {
            epg_password_item_unlock(true);
            if (g_blocked_channel.isLock()) {
                epg_unblock_channel(g_curr_focus_channel_id);
            }
        }
        else {
            epg_password_item_unlock(false);
            if (g_blocked_channel.isLock()) {
                epg_block_channel(g_curr_focus_channel_id);
            }

        }
        g_current_input_pass = "";
        g_unlock_pass_text.setText("");
        g_blocked_channel.start_blocked_channel_check();
    }

    @SuppressLint("SetTextI18n")
    public boolean onKeyDown(int keyCode) {
        boolean ret = false;
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
//            Log.d(TAG,"in keyCode = "+keyCode+" g_current_input_pass["+g_current_input_pass+"] g_unlock_pass_text["+g_unlock_pass_text.getText()+"]");
            g_current_input_pass += Utils.number_code_to_string(keyCode);
            g_unlock_pass_text.setText(g_unlock_pass_text.getText() + PASSWORD_STAR);
            epg_password_fail_text_hide();
//            Log.d(TAG,"out keyCode = "+keyCode+" g_current_input_pass["+g_current_input_pass+"] g_unlock_pass_text["+g_unlock_pass_text.getText()+"]");
            ret = true;
        }
        if (g_current_input_pass.length() == 4) {
            Log.d(TAG,"password full to check");
            check_password();
            ret = true;
        }
        return ret;
    }

    public void epg_password_item_init() {
        g_current_input_pass = "";
        g_unlock_title_text1 = (TextView) findViewById(R.id.lo_epg_textv_unlock_title_text1);
        g_unlock_title_text2 = (TextView) findViewById(R.id.lo_epg_textv_unlock_title_text2);
        g_unlock_title_text3 = (TextView) findViewById(R.id.lo_epg_textv_unlock_title_text3);
        g_unlock_pass_text = (TextView) findViewById(R.id.lo_epg_textv_unlock_pass_text);
        g_pass_text_bar = (View) findViewById(R.id.lo_epg_view_pass_text_bar);
        g_pass_fail = (TextView) findViewById(R.id.lo_epg_textv_pass_fail);
    }

    public void epg_password_item_hide() {
        get().runOnUiThread(() -> {
            hide_blocked_channel_bg();
            g_unlock_title_text1.setVisibility(View.GONE);
            g_unlock_title_text2.setVisibility(View.GONE);
            g_unlock_title_text3.setVisibility(View.GONE);
            g_unlock_pass_text.setVisibility(View.GONE);
            g_pass_text_bar.setVisibility(View.GONE);
            g_pass_fail.setVisibility(View.GONE);
        });
    }

    public void epg_password_item_show() {
        get().runOnUiThread(() -> {
            get().close_ca_message();
            show_blocked_channel_bg();
            g_unlock_title_text1.setVisibility(View.VISIBLE);
            g_unlock_title_text2.setVisibility(View.VISIBLE);
            g_unlock_title_text3.setVisibility(View.VISIBLE);
            g_unlock_pass_text.setVisibility(View.VISIBLE);
            g_pass_text_bar.setVisibility(View.VISIBLE);

            if (g_blocked_channel.isAdultLock())
                g_unlock_title_text2.setText(R.string.lock_adult);
            else if (g_blocked_channel.isTimeLock())
                g_unlock_title_text2.setText(R.string.lock_time);
            else if (g_blocked_channel.isParentalLock())
                g_unlock_title_text2.setText(R.string.lock_parent);
            else if (g_blocked_channel.isChannelLock())
                g_unlock_title_text2.setText(R.string.lock_channel);
        });
    }

    public void epg_password_fail_text_hide() {
        g_pass_fail.setVisibility(View.GONE);
    }

    public void epg_password_item_unlock(boolean success) {
        if(success) {
            epg_password_item_hide();
        }
        else {
            g_pass_fail.setVisibility(View.VISIBLE);
        }
    }

    public void epg_block_channel(long channelId) {
        //ProgramInfo channel = gPrimeDtv.get_program_by_channel_id(channelId);
        get().close_music_background();
        gChangeMgr.change_channel_stop(0, 0);
        epg_password_item_show();
        get().close_ca_message();
        gChangeMgr.change_channel_by_id(channelId);
        if(g_blocked_channel.isAdultLock())
            gListLayer.set_adult_blocked(true);
        gListLayer.update_program(channelId); // block channel
    }

    public void epg_unblock_channel(long channelId) {
        ProgramInfo channel = gPrimeDtv.get_program_by_channel_id(channelId);
        //boolean tuner_locked = g_epg.get_tuner_status(channel.getTunerId());

        if (is_music_channel(channel))
            get().open_music_background();
        else
            get().close_music_background();

        g_blocked_channel.unblock_channel();
        epg_password_item_hide();
        get().close_ca_message();
        gChangeMgr.change_channel_by_id(channelId, true);
        gListLayer.set_adult_blocked(false);
        gListLayer.update_program(channelId); // unblock channel
    }

    public void init(Epg epg) {
        gPrimeDtv = HomeApplication.get_prime_dtv();
        gChangeMgr = ChannelChangeManager.get_instance(get());
        gListLayer.init(epg,this);
        gDetailView.init(epg);
        g_epg = epg;
        epg_password_item_init();
        g_blocked_channel = BlockedChannel.get_instance(gChangeMgr.get_cur_channel());
        gHandler = get().get_thread_handler();
    }

    public void set_context(Context context){
        if(gChangeMgr != null)
            gChangeMgr.set_context(context);
        if (g_blocked_channel != null)
            g_blocked_channel.register_callback(this);
    }

    public void show_password_item() {

    }

    public void show_blocked_channel_bg() {
        Log.d(TAG,"[DB_Surface] show blocked channel bg");
        g_epgBlockedChannel_bg.setVisibility(View.VISIBLE);
    }

    public void hide_blocked_channel_bg() {
        Log.d(TAG,"[DB_Surface] hide blocked channel bg");
        g_epgBlockedChannel_bg.setVisibility(View.INVISIBLE);
    }

    public void set_epg(Epg epg) {
        g_epg = epg;
    }

    private void init_view() {
        inflate(getContext(), R.layout.view_epg, this);
        //setZ(1000);
        gDetailView = (EpgDetailView) findViewById(R.id.lo_epg_view_detail);
        g_unlock_title_text1 = (TextView) findViewById(R.id.lo_epg_textv_unlock_title_text1);
        g_unlock_title_text2 = (TextView) findViewById(R.id.lo_epg_textv_unlock_title_text2);
        g_unlock_title_text3 = (TextView) findViewById(R.id.lo_epg_textv_unlock_title_text3);
        g_unlock_pass_text = (TextView) findViewById(R.id.lo_epg_textv_unlock_pass_text);
        g_pass_text_bar = findViewById(R.id.lo_epg_view_pass_text_bar);
        g_pass_fail = (TextView) findViewById(R.id.lo_epg_textv_pass_fail);
        g_epgBlockedChannel_bg = (View) findViewById(R.id.epg_blocked_channel_bg);
        gListLayer = findViewById(R.id.lo_epg_list_layer);

        gListLayer.set_channel_click(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                change_to_live_tv(
                        Utils.get_sec_tag_id((String) v.getTag()),
                        Utils.get_third_tag_id((String) v.getTag()),
                        Utils.get_fourth_tag((String) v.getTag()));
            }
        });

        gListLayer.set_on_item_change_listener(new EpgContentView.OnItemChangeListener() {
            @Override
            public void onFocusChange(int programId, long channelId, int serviceId, long category, EPGEvent epgEvent) {
                gDetailView.update_program(programId, channelId, epgEvent, category);
            }
        });
    }

    public void change_to_live_tv(int number, int serviceId, String type) {
    }

    public EpgDetailView get_detail_view() {
        return gDetailView;
    }

    public void tune_channel(final long channelId, final int serviceId, boolean forceAgain) {
        isChangeChannel = true;
        ProgramInfo currChannel = gPrimeDtv.get_program_by_channel_id(channelId);
        Log.d(TAG, "tune_channel: [channel] " + Epg.get_channel_full_name(currChannel) + ", [same channel] " + (gPrevChId == channelId));

        gHandler.removeCallbacksAndMessages(null);
        gListLayer.block_program_list();
        if (gPrevChId == channelId) {
            Log.w(TAG, "tune_channel: do not change channel");
            gListLayer.unblock_program_list();
            return;
        }

        gHandler.postDelayed(() -> {
            gPrevChId = channelId;
            boolean[] isBlock = {false};
            g_curr_focus_channel_id = channelId;
            g_curr_focus_service_id = serviceId;

            if (g_blocked_channel != null) {
                g_blocked_channel.stop_blocked_channel_check();
                isBlock[0] = g_blocked_channel.checkLock(currChannel);
                if (isBlock[0])
                    epg_block_channel(channelId); // tune channel
                else
                    epg_unblock_channel(channelId); // tune channel
                g_blocked_channel.start_blocked_channel_check();
            }
            else {
                get().close_ca_message();
                gChangeMgr.change_channel_by_id(g_curr_focus_channel_id);
            }
            gListLayer.unblock_program_list();
            isChangeChannel = false;
        }, 500);
    }

    public void stop_channel() {
        gChangeMgr.change_channel_stop(0, 1);
    }

    public void stop_channel(int stop_monitor_table) {
        gChangeMgr.change_channel_stop(0,stop_monitor_table);
    }

    public void update_program_data(long channelId) {
        if (g_blocked_channel.isLock()) {
            return;
        }
        if (channelId == gListLayer.get_current_channel_id()) {
            gListLayer.update_program_data(channelId);
        }
    }

    public int get_last_focus_view_type() {
        return gListLayer.get_last_focus_view_type();
    }

    public void on_genre_changed(int genreId, String name) {
        if (gListLayer == null)
            return;

        List<ProgramInfo> programInfoList = g_epg.get_program_info_list_by_genre(genreId);
        ProgramInfo currChannel = gListLayer.get_current_channel_info();

        Log.d(TAG, "on_genre_changed: current channel = " + currChannel.getDisplayNameFull());
        channel_of_change_genre = currChannel;
        boolean channelChangeFlag = !check_channel_in_genre(genreId, currChannel);
        gListLayer.update_epg_data_by_genre_id(genreId, name, false, channelChangeFlag);

        if (channelChangeFlag && programInfoList.size()!=0) {
            long channelId = programInfoList.get(0).getChannelId();
            int serviceId = programInfoList.get(0).getServiceId();
            Log.d(TAG, "on_genre_changed: [channel Id] " + channelId + " [channel num] " + programInfoList.get(0).getDisplayNum());
            tune_channel(channelId, serviceId, false);
        }
    }

    public void on_genre_changed_by_zapping(int genreId, String name) {
        if (gListLayer == null)
            return;

        gListLayer.update_epg_data_by_genre_id(genreId, name, true, true);
    }

    public String get_current_input_pass() {
        return g_current_input_pass;
    }

    public void set_current_input_pass(String g_current_input_pass) {
        this.g_current_input_pass = g_current_input_pass;
    }

    @Override
    public void on_isLock_changed(boolean isLock) {
        long channelId = g_curr_focus_channel_id;
        ProgramInfo channel = g_blocked_channel.get_cur_channel();//gPrimeDtv.get_program_by_channel_id(channelId);
        if(g_blocked_channel != null)
            if(isLock) {
//                Log.d("on_isLock_changed","channel["+channel.getDisplayName()+"]"+" channel blocked !!!!!");
//                LogUtils.d("g_tune_channel_runnable lock channel change_channel_stop !!!!!!");
                epg_block_channel(g_blocked_channel.get_cur_channel().getChannelId());
            }
            else {
//                Log.d("on_isLock_changed","channel["+channel.getDisplayName()+"]"+" channel unblocked !!!!!");
//                LogUtils.d("g_tune_channel_runnable unlock channel change_channel_stop !!!!!!");
                epg_unblock_channel(channel.getChannelId());
            }
    }

    @Override
    public void on_tuner_lock(ProgramInfo channelInfo) {
        if (g_epg == null)
            return;
        boolean tunerLocked = g_epg.get_tuner_status(channelInfo.getTunerId());
        //Log.d(TAG, "on_tuner_lock: num = " + channelInfo.getDisplayNum());
        //Log.d(TAG, "on_tuner_lock: tuner id " + channelInfo.getTunerId());
        //Log.d(TAG, "on_tuner_lock: isLock = " + isLock);

        if (!tunerLocked) {

            if (gChangeMgr.has_recording()) {
                gChangeMgr.pvr_record_stop_all();

                get().runOnUiThread(() -> {
                    if (g_pvrMsgDialog == null)
                        g_pvrMsgDialog = new MessageDialog(get());

                    if (!g_pvrMsgDialog.isShowing() && !g_blocked_channel.isLock()) {
                        g_pvrMsgDialog.show_dialog(R.string.error_e606);
                    }
                });
            }

            if (!get().has_ca_message_shown() && !g_blocked_channel.isLock()) {
                isE200Showing = true;
                get().open_ca_message(get().getString(R.string.error_e200));
            }
        }
        else {
            if (get().has_ca_message_shown() && is_music_channel(gListLayer.get_current_channel_info()))
                get().close_ca_message();
            if(isE200Showing && get().get_prime_dtv().av_control_get_play_status(0) != AvCmdMiddle.PESI_SVR_AV_LIVEPLAY_STATE)
                gChangeMgr.change_channel_by_id(gChangeMgr.get_cur_ch_id());
            isE200Showing = false;
        }
    }

    public void reset_fcc() {
        gChangeMgr.reset_fcc();
    }

    public boolean is_lock_adult() {
        return g_blocked_channel.isAdultLock();
    }

    public ListLayer get_list_layer() {
        return gListLayer;
    }

    public EpgActivity get() {
        return (EpgActivity) getContext();
    }

    public void play_channel(boolean isPlayAV) {
        ProgramInfo channel = gChangeMgr.get_cur_channel();

        if (null == channel)
            return;

        g_blocked_channel.stop_blocked_channel_check();
        boolean blocked = g_blocked_channel.checkLock(channel);
        g_curr_focus_channel_id = channel.getChannelId();
        gChangeMgr.set_previous_channel(channel.getChannelId());
        gListLayer.set_adult_blocked(blocked && g_blocked_channel.isAdultLock());

        // TODO: integrate Error Message 1
        if (is_music_channel(channel) && !blocked)
            get().open_music_background();
        else
            get().close_music_background();

        Log.d(TAG, "play_channel: [blocked] " + blocked + ", [current channel] " + channel.getDisplayNum() + " " + channel.getDisplayName());
        if (blocked)
            epg_password_item_show();
        else {
            //g_channel_change_manager.change_channel_stop(0, 1);
            epg_password_item_hide();
            get().close_ca_message();
            gChangeMgr.change_channel_by_id(g_curr_focus_channel_id, isPlayAV);
        }
        gListLayer.update_program(channel.getChannelId()); // play channel
        gListLayer.set_current_channel_id(g_curr_focus_channel_id);
        g_blocked_channel.start_blocked_channel_check();
        gPrevChId = channel.getChannelId();
    }

    public MessageDialog get_pvrMsgDialog() {
        return g_pvrMsgDialog;
    }

    private boolean is_music_channel(ProgramInfo channelInfo) {
        if (channelInfo != null)
            return channelInfo.getType() == PROGRAM_RADIO;
        else
            return false;
    }

    public void start_blocked_channel_check() {
        if (g_blocked_channel != null) {
            g_blocked_channel.stop_blocked_channel_check(); // stop first for safety
            g_blocked_channel.start_blocked_channel_check();
        }
    }

    public void stop_blocked_channel_check() {
        if (g_blocked_channel != null) {
            g_blocked_channel.stop_blocked_channel_check();
        }
    }

    public ChannelChangeManager get_channel_change_manager() {
        return gChangeMgr;
    }

    public long getCurrFocusChannelId() {
        return g_curr_focus_channel_id;
    }

    public void reset_pin_code_status() {
        if (g_blocked_channel != null) {
            g_blocked_channel.reset_pin_code_status();
        }
    }

    private boolean check_channel_in_list(List<ProgramInfo> programInfoList, ProgramInfo curProgramInfo) {
        for (ProgramInfo programInfo:programInfoList) {
            if (programInfo.getChannelId() == curProgramInfo.getChannelId())
                return true;
        }
        return false;
    }

    private boolean check_channel_in_genre(int genre, ProgramInfo programInfo) {
        Log.d(TAG, "check_channel_in_genre: [channel Id] " + programInfo.getChannelId() + " [channel num] " + programInfo.getDisplayNum());
        boolean retValue = false;

            switch (genre) {
                case EpgActivity.TV_ALL -> retValue = true;
                case EpgActivity.TV_KID ->
                        retValue = (programInfo.getCategory_type() & 0x0001) == 1;
                case EpgActivity.TV_EDUCATION ->
                        retValue = ((programInfo.getCategory_type() & 0x0002) >> 1) == 1;
                case EpgActivity.TV_NEWS ->
                        retValue = ((programInfo.getCategory_type() & 0x0004) >> 2) == 1;
                case EpgActivity.TV_MOVIE ->
                        retValue = ((programInfo.getCategory_type() & 0x0008) >> 3) == 1;
                case EpgActivity.TV_VARIETY ->
                        retValue = ((programInfo.getCategory_type() & 0x0010) >> 4) == 1;
                case EpgActivity.TV_MUSIC ->
                        retValue = ((programInfo.getCategory_type() & 0x0020) >> 5) == 1;
                case EpgActivity.TV_ADULT ->
                        retValue = ((programInfo.getCategory_type() & 0x0040) >> 6) == 1;
                case EpgActivity.TV_SPORT ->
                        retValue = ((programInfo.getCategory_type() & 0x0080) >> 7) == 1;
                case EpgActivity.TV_RELIGION ->
                        retValue = ((programInfo.getCategory_type() & 0x0200) >> 9) == 1;
                case EpgActivity.TV_UHD ->
                        retValue = ((programInfo.getCategory_type() & 0x0800) >> 11) == 1;
                case EpgActivity.TV_SHOPPING ->
                        retValue = ((programInfo.getCategory_type() & 0x80000) >> 19) == 1;
            }
        return retValue;
    }

    public void update_parental_lock() {
        g_blocked_channel.update_parental_lock();
    }

    // is channel blocked by channel lock, parental lock, time lock or adult lock
    public boolean is_channel_blocked() {
        return g_blocked_channel.isLock();
    }
}
