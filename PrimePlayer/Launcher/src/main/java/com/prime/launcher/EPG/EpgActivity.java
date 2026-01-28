package com.prime.launcher.EPG;

import static com.prime.datastructure.sysdata.FavGroup.ALL_TV_TYPE;
import static com.prime.datastructure.sysdata.FavGroup.GROUP_ADULT;
import static com.prime.datastructure.sysdata.FavGroup.GROUP_EDUCATION;
import static com.prime.datastructure.sysdata.FavGroup.GROUP_KIDS;
import static com.prime.datastructure.sysdata.FavGroup.GROUP_MOVIES;
import static com.prime.datastructure.sysdata.FavGroup.GROUP_MUSIC;
import static com.prime.datastructure.sysdata.FavGroup.GROUP_NEWS;
import static com.prime.datastructure.sysdata.FavGroup.GROUP_RELIGION;
import static com.prime.datastructure.sysdata.FavGroup.GROUP_SHOPPING;
import static com.prime.datastructure.sysdata.FavGroup.GROUP_SPORTS;
import static com.prime.datastructure.sysdata.FavGroup.GROUP_UHD;
import static com.prime.datastructure.sysdata.FavGroup.GROUP_VARIETY;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.prime.launcher.BaseActivity;
import com.prime.launcher.CustomView.Snakebar;
import com.prime.launcher.Home.Hotkey.HotkeyInfo;
import com.prime.launcher.Home.Hotkey.HotkeyRecord;
import com.prime.launcher.Home.LiveTV.Zapping.ZappingDialog;
import com.prime.launcher.HomeActivity;
import com.prime.launcher.HomeApplication;
import com.prime.launcher.R;
import com.prime.launcher.Utils.ActivityUtils;
import com.prime.launcher.Utils.ErrorCodeUtils;
import com.prime.launcher.Utils.UsbUtils;
import com.prime.launcher.Utils.Utils;
import com.prime.launcher.ChannelChangeManager;
import com.prime.launcher.PrimeDtv;
import com.prime.launcher.Receiver.PrimeVolumeReceiver;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.ServiceDefine.AvCmdMiddle;
import com.prime.datastructure.utils.ErrorCode;
import com.prime.datastructure.sysdata.BookInfo;
import com.prime.datastructure.sysdata.EPGEvent;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.utils.LogUtils;
import com.prime.datastructure.utils.TVMessage;

import java.util.List;

/** @noinspection CommentedOutCode*/
public class EpgActivity extends BaseActivity implements PrimeDtv.LauncherCallback {
    private static final String TAG = "EpgActivity";
    public static final String CLS_EPG_ACTIVITY        = "com.prime.launcher.EPG.EpgActivity";

    public static final int TV_ALL          = ALL_TV_TYPE;
    public static final int TV_KID          = GROUP_KIDS;
    public static final int TV_EDUCATION    = GROUP_EDUCATION;
    public static final int TV_NEWS         = GROUP_NEWS;
    public static final int TV_MOVIE        = GROUP_MOVIES;
    public static final int TV_VARIETY      = GROUP_VARIETY;
    public static final int TV_MUSIC        = GROUP_MUSIC;
    public static final int TV_ADULT        = GROUP_ADULT;
    public static final int TV_SPORT        = GROUP_SPORTS;
    public static final int TV_RELIGION     = GROUP_RELIGION;
    public static final int TV_UHD          = GROUP_UHD;
    public static final int TV_SHOPPING     = GROUP_SHOPPING;

    private Handler gHandler;
    private Handler gThreadHandler;
    private ChannelChangeManager gChMgr;
    private EpgHotKeyGenre g_GenreDialog;
    private EpgView g_epg_view = null;
    private HotkeyInfo gDetailInfo;
    private ListLayer gListLayer;
    private Epg g_epg;
    private PrimeDtv g_dtv;
    private SurfaceView g_surfaceView;
    private SurfaceView g_surfaceView_2;
    private SurfaceView g_surfaceView_3;
    private TextView g_CA_message;
    private BroadcastReceiver g_localReceiver;
    //private Toast g_volume_toast = null;
    private int checkAvFrameCnt = 0;

    private String gGenreName;

    // Zapping
    private ZappingDialog gZappingDialog;
    private List<ProgramInfo> gAllChList;
    private String gZappingChNum = "";
    private Runnable gOpenZapping;

    public interface Callback {
        void on_epg_opened();
        void on_epg_closed();
    }
    static Callback g_callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epg);
        init();

        //g_dtv.register_callbacks();
        local_receiver_register();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: ");
        super.onStart();
        gListLayer.set_default_channel();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        close_genre();
        g_epg_view.set_context(this);
        set_surface_view();
        boolean isPlayAV = true;// = ActivityUtils.is_running(this, this.getClass());
        ComponentName runningTask = ActivityUtils.get_running_task(this);
        //Log.d(TAG, "getPackageName = "+runningTask.getPackageName());
        //Log.d(TAG, "getClassName = "+runningTask.getClassName());
        if(runningTask.getPackageName().equals(this.getPackageName())){
            if(runningTask.getClassName().equals(HomeActivity.class.getName())){
                isPlayAV = false;
            }
        }
        Log.d(TAG, "isInit = "+isPlayAV);
        play_channel(isPlayAV);
        on_volume_changed();
        //set_screen_transparent();
        if (g_callback != null)
            g_callback.on_epg_opened();
        if (gZappingDialog == null)
            gZappingDialog = new ZappingDialog(this, gAllChList);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();

        if (gZappingDialog != null && gZappingDialog.isShowing())
            gZappingDialog.dismiss();

        if (g_callback != null)
            g_callback.on_epg_closed();

        //g_epg_view.reset_fcc();
        //g_dtv.av_control_play_stop_all();
        g_epg_view.stop_blocked_channel_check();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();

        if (gZappingDialog != null && gZappingDialog.isShowing())
            gZappingDialog.dismiss();

        if (!ActivityUtils.get_top_activity(this).equals(HomeActivity.CLS_HOME_ACTIVITY)) {
            g_epg_view.reset_pin_code_status();
            g_dtv.av_control_play_stop_all();
        }
        Glide.with(this).pauseRequests();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();

        if (gZappingDialog != null && gZappingDialog.isShowing())
            gZappingDialog.dismiss();

        //g_dtv.unregister_callbacks();
        local_receiver_unregister();
        //g_dtv.av_control_play_stop_all();
    }

    @Override
    public void onMessage(TVMessage tvMessage) {
        super.onMessage(tvMessage);
        switch (tvMessage.getMsgType()) {
            case TVMessage.TYPE_EPG_UPDATE:
                long channelID = tvMessage.getChannelId();
                //Log.d(TAG,"TYPE_EPG_UPDATE channelID = " + channelID);
                g_epg_view.update_program_data(channelID);
                break;
            case TVMessage.TYPE_AV_FRAME_PLAY_STATUS:
                if (tvMessage.getAvFrameChannelId() != g_epg_view.getCurrFocusChannelId())
                    return;
                if (tvMessage.getAvFrameStatus() == 0) {// av ok
                    close_ca_message();
                    if(g_dtv.av_control_get_play_status(0) != AvCmdMiddle.PESI_SVR_AV_LIVEPLAY_STATE)
                        checkAvFrameCnt++;
                    else
                        checkAvFrameCnt = 0;
                    if (checkAvFrameCnt > 3) {
                        play_channel(false);
                        checkAvFrameCnt = 0;
                    }
                }
                else {
                    checkAvFrameCnt = 0;
                    //if(g_dtv.av_control_get_play_status(0) == AvCmdMiddle.PESI_SVR_AV_LIVEPLAY_STATE)
                    //    g_epg_view.stop_channel(0);
                    if(!has_ca_message_shown())
                        showErrorMessage(ErrorCode.ERROR_E213, null);
                }
                break;
            case TVMessage.TYPE_SYSTEM_SHOW_ERROR_MESSAGE:
                //Log.d(TAG, "onMessage: TYPE_SYSTEM_SHOW_ERROR_MESSAGE");
                g_epg_view.stop_channel(0);
                showErrorMessage(tvMessage.getErrCode(), tvMessage.getMessage());
                break;
            case TVMessage.TYPE_SYSTEM_SI_UPDATE_CHANNEL_LIST:{
                LogUtils.d("SIUpdater - call UI to update channel list");
                gListLayer.update_channel_list();
                g_epg_view.get_channel_change_manager().update_all_channel();
            }break;
            case TVMessage.TYPE_SYSTEM_SI_UPDATE_SET_CHANNEL: {
                long currentChannelId = gListLayer.get_current_channel_id();
                int currentServiceId = (int) gListLayer.get_current_service_id();
                g_epg_view.tune_channel(currentChannelId, currentServiceId, true);
            }break;
            case TVMessage.TYPE_SYSTEM_SI_UPDATE_SET_1ST_CHANNEL:{
                LogUtils.d("SIUpdater - call UI to set channel");
                gListLayer.focus_1st_channel();
            }break;
            case TVMessage.TYPE_EPG_PF_VERSION_CHANGED:{
                long cur_ch_id = g_epg_view.get_channel_change_manager().get_cur_ch_id();
                if (cur_ch_id == tvMessage.getChannelId()) {
                    g_epg_view.update_parental_lock();
                }
            }break;
        }

        // PVR callback comes here
        switch (tvMessage.getMsgType()) {
            case TVMessage.TYPE_PVR_RECORDING_START_SUCCESS ->  on_record_started(tvMessage);
            case TVMessage.TYPE_PVR_RECORDING_START_ERROR ->    on_record_started(tvMessage); // TODO: show error message (can't start recording)
            case TVMessage.TYPE_PVR_RECORDING_COMPLETED ->      on_record_completed(tvMessage);
            case TVMessage.TYPE_PVR_RECORDING_STOP_SUCCESS ->   on_record_stopped(tvMessage);
            case TVMessage.TYPE_PVR_RECORDING_STOP_ERROR ->     on_record_stopped(tvMessage); // TODO: show error message (can't stop recording)

        }
    }

    @Override
    public void onBroadcastMessage(Context context, Intent intent) {
        super.onBroadcastMessage(context, intent);
        Log.d(TAG, "onMessage: EPG");

        String action = intent.getAction();
        if (action == null) {
            Log.e(TAG, "onBroadcastMessage: action == null");
            return;
        }

        switch (action) {
            case PrimeVolumeReceiver.ACTION_VOLUME_CHANGED:
            case PrimeVolumeReceiver.ACTION_STREAM_MUTE_CHANGED:
                on_volume_changed();
                break;
            case PrimeVolumeReceiver.ACTION_KEYCODE_VOLUME_DOWN:
            case PrimeVolumeReceiver.ACTION_KEYCODE_VOLUME_MUTE:
                //show_volume_down_toast();
                break;
        }
    }

    /*public void start_record(Context context, Intent intent) {
        if (!ActivityUtils.get_top_activity(context).equals(CLS_EPG_ACTIVITY))
            return;
        Log.d(TAG, "start_record: ");

        Bundle bookInfoBundle = intent.getExtras();
        if (bookInfoBundle == null) {
            Log.e(TAG, "start_record: bookInfoBundle == null");
            return;
        }

        BookInfo bookInfo = new BookInfo(bookInfoBundle);
        // skip if book cycle == BOOK_CYCLE_SERIES_EMPTY
        if (bookInfo.getBookCycle() == BookInfo.BOOK_CYCLE_SERIES_EMPTY) {
            Log.w(TAG, "start_record: book cycle = BOOK_CYCLE_SERIES_EMPTY, skip!");
            return;
        }

        Log.d(TAG, "start_record: " + bookInfo.ToString());

        ChannelChangeManager channelChangeManager = ChannelChangeManager.get_instance(context);
        channelChangeManager.pvr_record_start(bookInfo.getChannelId(), bookInfo.getEpgEventId(), bookInfo.getDuration(), false);
    }

    public void start_power_on(Context context, Intent intent) {
        if (!ActivityUtils.get_top_activity(context).equals(CLS_EPG_ACTIVITY))
            return;
        Log.d(TAG, "start_power_on: ");

        if (PrimeBroadcastReceiver.get_screen_on_status()) {
            Log.i(TAG, "start_power_on: already screen on");
            return;
        }
        Utils.input_keycode(KeyEvent.KEYCODE_POWER);
    }

    public void start_change_channel(Context context, Intent intent) {
        if (!ActivityUtils.get_top_activity(context).equals(CLS_EPG_ACTIVITY))
            return;
        Log.d(TAG, "start_change_channel: ");

        Bundle bookInfoBundle = intent.getExtras();
        if (bookInfoBundle == null) {
            Log.e(TAG, "get_genre_index: bookInfoBundle == null");
            return;
        }

        BookInfo bookInfo = new BookInfo(bookInfoBundle);
        Log.d(TAG, "start_change_channel: " + bookInfo.ToString());
        ProgramInfo programInfo = g_dtv.get_program_by_channel_id(bookInfo.getChannelId());

        change_channel_from_zapping(programInfo);
    }*/

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e(TAG, "onKeyDown: keyCode = " + keyCode + ", current focus = " + getCurrentFocus());
        int lastFocusViewType = g_epg_view.get_last_focus_view_type();
        //Log.d(TAG, "onKeyDown: lastFocusViewType = " + lastFocusViewType);

        if (KeyEvent.KEYCODE_BACK == keyCode) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else if (KeyEvent.KEYCODE_INFO == keyCode) {
            Log.d(TAG, "KEYCODE_INFO");
            show_info();
        }
        //else if (KeyEvent.KEYCODE_PROG_GREEN == keyCode)
        //    ;
        else if (KeyEvent.KEYCODE_PROG_BLUE == keyCode) {
            Log.d(TAG, "KEYCODE_PROG_BLUE");
            open_genre();
        }
        else if (KeyEvent.KEYCODE_PROG_YELLOW == keyCode && lastFocusViewType == ListLayer.FOCUS_VIEW_PROGRAM) {
            Log.d(TAG, "KEYCODE_PROG_YELLOW");
            setup_remind();
        }
        else if (KeyEvent.KEYCODE_PROG_RED == keyCode && lastFocusViewType == ListLayer.FOCUS_VIEW_PROGRAM) {
            Log.d(TAG, "KEYCODE_PROG_RED");
            setup_record();
        }
        else if (KeyEvent.KEYCODE_DPAD_CENTER == keyCode && (lastFocusViewType == ListLayer.FOCUS_VIEW_PROGRAM || lastFocusViewType == ListLayer.FOCUS_VIEW_CHANNEL))
            Log.d(TAG, "KEYCODE_DPAD_CENTER");
        else if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            if (g_epg_view.is_channel_blocked()) {
                g_epg_view.onKeyDown(keyCode);
                return true;
            }
            else
                open_zapping(keyCode);
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (is_channel_focused() && (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN)) {
            long currentChannelId = gListLayer.get_current_channel_id();
            int currentServiceId = (int) gListLayer.get_current_service_id();
            g_epg_view.tune_channel(currentChannelId, currentServiceId, false); // onKeyUp
        }
        return super.onKeyUp(keyCode, event);
    }

    public void on_volume_changed() {
        AudioManager audioManager = getSystemService(AudioManager.class);
        int          audioVolume  = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        //Log.e(TAG, "on_volume_changed: " + audioVolume);
        if (audioVolume == 0)
            show_mute();
        else
            hide_mute();
    }

    public void on_record_started(TVMessage msg) {
        if (g_dtv == null)
            g_dtv = HomeApplication.get_prime_dtv();
        ProgramInfo channel = g_dtv.get_program_by_channel_id(msg.getRecChannelId());
        String channelNum = channel != null ? channel.getDisplayNum(com.prime.datastructure.utils.Utils.MAX_LENGTH_OF_CHANNEL_NUM) : "000";
        String channelName = channel != null ? channel.getDisplayName() : "";

        if (TVMessage.TYPE_PVR_RECORDING_START_SUCCESS != msg.getMsgType()) {
            Log.e(TAG, "on_record_started: FAIL");
            on_record_started_error(channel);
            return;
        }

        Log.i(TAG, "on_record_started: SUCCESS, [channelId] " + msg.getRecChannelId() + ", [channel] " + channelNum + " " + channelName);

        Utils.show_notification(this, "CH" + channelNum + " " + getString(R.string.pvr_message_start_recording_hint));
        if (gDetailInfo == null)
            gDetailInfo = new HotkeyInfo(this);
        gDetailInfo.set_red_hint(R.string.hint_rcu_cancel_record);
    }

    public void on_record_started_error(ProgramInfo channel) {
        if (g_dtv == null)
            g_dtv = HomeApplication.get_prime_dtv();
        if (gChMgr == null)
            gChMgr = ChannelChangeManager.get_instance(this);
        if (null == channel)
            return;

        String channelNum = channel.getDisplayNum(com.prime.datastructure.utils.Utils.MAX_LENGTH_OF_CHANNEL_NUM);
        //show_message_dialog(R.string.error_e606);
        Utils.show_notification(this, "CH" + channelNum + " " + getString(R.string.pvr_message_stop_recording_hint));

        gChMgr.remove_record_channel();
        Log.w(TAG, "on_record_started_error: remove recording channel = " + channelNum + " " + channel.getDisplayName());
    }

    public void on_record_completed(TVMessage msg) {
        if (gChMgr == null)
            gChMgr = ChannelChangeManager.get_instance(this);
        if (g_dtv == null)
            g_dtv = HomeApplication.get_prime_dtv();
        int recId = msg.getRecId();
        long channelId = gChMgr.get_record_channel_id(recId);

        //noinspection ConstantValue
        if (false) {
            ProgramInfo channel = g_dtv.get_program_by_channel_id(channelId);
            String channelNum = channel != null ? channel.getDisplayNum(com.prime.datastructure.utils.Utils.MAX_LENGTH_OF_CHANNEL_NUM) : "000";
            String channelName = channel != null ? channel.getDisplayName() : "NULL";
            Log.e(TAG, "on_record_completed: [Channel] " + channelNum + " " + channelName);
        }

        Log.i(TAG, "on_record_completed: [Record ID] " + recId + ", [Channel ID] " + channelId);
        gChMgr.pvr_record_stop(channelId);
    }

    public void on_record_stopped(TVMessage msg) {
        if (g_dtv == null)
            g_dtv = HomeApplication.get_prime_dtv();
        if (gChMgr == null)
            gChMgr = ChannelChangeManager.get_instance(this);
        ProgramInfo channel = gChMgr != null ? g_dtv.get_program_by_channel_id(gChMgr.get_record_stop_ch_id()) : null;
        String channelNum = channel != null ? channel.getDisplayNum(com.prime.datastructure.utils.Utils.MAX_LENGTH_OF_CHANNEL_NUM) : "000";
        String channelName = channel != null ? channel.getDisplayName() : "";

        if (TVMessage.TYPE_PVR_RECORDING_STOP_SUCCESS != msg.getMsgType()) {
            Log.e(TAG, "on_record_stopped: FAIL");
            on_record_stopped_error();
            return;
        }

        Log.i(TAG, "on_record_stopped: SUCCESS, [channel] " + channelNum + " " + channelName);
        Utils.show_notification(this, "CH" + channelNum + " " + getString(R.string.pvr_message_stop_recording_hint));

        // update UI
        TextView recHint = findViewById(R.id.lo_epg_hot_key_hint_textv_record);
        recHint.setText(R.string.hint_rcu_record);
        int currentPosition = gListLayer.get_last_focus_program_position();
        gListLayer.get_program_adapter().update_program(currentPosition);
        if (gDetailInfo == null)
            gDetailInfo = new HotkeyInfo(this);
        gDetailInfo.set_red_hint(R.string.hint_rcu_record);
    }

    public void on_record_stopped_error() {
        // TODO: show error message
        Log.e(TAG, "on_record_stopped_error: TODO: show error message");
    }

    public void on_book_record() {
        Log.i(TAG, "on_book_record: ");
        if (gDetailInfo == null)
            gDetailInfo = new HotkeyInfo(this);
        gDetailInfo.set_red_hint(R.string.hint_rcu_cancel_record);
    }

    public void on_cancel_book_record() {
        Log.i(TAG, "on_cancel_book_record: ");
        if (gDetailInfo == null)
            gDetailInfo = new HotkeyInfo(this);
        gDetailInfo.set_red_hint(R.string.hint_rcu_record);
    }

    public void on_schedule_remind() {
        Log.i(TAG, "on_schedule_remind: ");
        if (gDetailInfo == null)
            gDetailInfo = new HotkeyInfo(this);
        gDetailInfo.set_yellow_hint(R.string.hint_rcu_cancel_remind);
    }

    public void on_cancel_remind() {
        Log.i(TAG, "on_cancel_remind: ");
        if (gDetailInfo == null)
            gDetailInfo = new HotkeyInfo(this);
        gDetailInfo.set_yellow_hint(R.string.hint_rcu_remind);
    }

    public void on_click_genre(String genreName) {
        Log.d(TAG, "on_click_genre: " + genreName);
        gGenreName = genreName;
        g_epg_view.on_genre_changed(map_genre(gGenreName), gGenreName);
    }

    /*private void show_volume_down_toast() {
        Log.d(TAG, "show_toast:");
        //        AudioManager audioManager = getSystemService(AudioManager.class);
        //        int          audioVolume  = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        if (PrimeVolumeReceiver.g_volume != 0)
            return;

        if (g_volume_toast != null) {
            Log.d(TAG, "show_toast: g_toast.cancel()");
            g_volume_toast.cancel();
        }

        g_volume_toast = Toast.makeText(top_activity, getString(R.string.unmute_by_volume_up), Toast.LENGTH_SHORT);
        //Log.d(TAG, "show_toast: g_toast.show()");
        g_volume_toast.show();
    }*/

    private void init() {
        g_dtv = HomeApplication.get_prime_dtv();
        g_epg = new Epg(g_dtv);
        init_surface_view();
        init_mute_status();
        //g_epg.fakeData();
        g_epg_view = (EpgView)findViewById(R.id.lo_epg_view);
        g_epg_view.init(g_epg);
        gGenreName = getString(R.string.genre_all);

        g_CA_message = (TextView) findViewById(R.id.lo_epg_live_tv_message);
        gListLayer = findViewById(R.id.lo_epg_list_layer);
        gHandler = new Handler(Looper.getMainLooper());
        gAllChList = g_epg.get_all_tv_program_info_list();
        gZappingDialog = new ZappingDialog(this, gAllChList);
        gOpenZapping = open_zapping();
    }

    public void init_surface_view() {
        if (g_dtv == null)
            return;
        g_surfaceView = findViewById(R.id.lo_epg_sfv_live_tv);
        Log.d(TAG,"surfaceView = "+g_surfaceView);
        g_surfaceView_2 = findViewById(R.id.lo_epg_sfv_live_tv_2);
        Log.d(TAG,"surfaceView = "+g_surfaceView_2);
        g_surfaceView_3 = findViewById(R.id.lo_epg_sfv_live_tv_3);
        Log.d(TAG,"surfaceView = "+g_surfaceView_3);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            g_surfaceView.setSurfaceLifecycle(SurfaceView.SURFACE_LIFECYCLE_FOLLOWS_ATTACHMENT);
            g_surfaceView_2.setSurfaceLifecycle(SurfaceView.SURFACE_LIFECYCLE_FOLLOWS_ATTACHMENT);
            g_surfaceView_3.setSurfaceLifecycle(SurfaceView.SURFACE_LIFECYCLE_FOLLOWS_ATTACHMENT);
        }

    }

    public void set_surface_view(){
        g_dtv.set_surface_view(this, g_surfaceView, 0);
        g_dtv.set_surface_view(this, g_surfaceView_2,1);
        g_dtv.set_surface_view(this, g_surfaceView_3,2);
        if(HomeApplication.get_prime_dtv().av_control_get_play_status(0) == AvCmdMiddle.PESI_SVR_AV_LIVEPLAY_STATE){
            ChannelChangeManager.setFCCVisible(g_dtv.AvCmdMiddle_getPlay_index());
        }
    }

    private void init_mute_status() {
        AudioManager audioManager = getSystemService(AudioManager.class);
        int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (volume == 0)
            show_mute();
        else
            hide_mute();
    }

    public void setup_remind() {
        if (!Pvcfg.getPVR_PJ()) {
            Log.e(TAG, "setup_remind: not support PVR");
            return;
        }

        EPGEvent epgEvent = get_current_epg_event();
        if (epgEvent == null) {
            Log.e(TAG, "setup_remind: epgEvent is NULL");
            return;
        }

        if (is_mark_remind()) {
            Log.i(TAG, "setup_remind: cancel remind ?");
            gListLayer.stop_remind_v2();
        }
        else {
            Log.i(TAG, "setup_remind: remind program");
            gListLayer.remind_program_v2();
        }
    }

    public void setup_record() {
        if (!Pvcfg.getPVR_PJ())
            return;

        HotkeyRecord hotkeyRecord = new HotkeyRecord(this);
        ProgramInfo programInfo = gListLayer.get_current_channel_info();
        //Log.d(TAG, "setup_record: tuner status = " + g_epg.get_tuner_status(programInfo.getTunerId()));
        if (!g_epg.get_tuner_status(programInfo.getTunerId())) {
            hotkeyRecord.show_dialog(R.string.dvr_recording_fail);
            return;
        }

        if (!UsbUtils.has_usb_disk(this)) {
            hotkeyRecord.show_dialog(R.string.error_e601);
            return;
        }

        gListLayer.setup_record();
    }

    /*public void set_surfaceVew_gone() {
        g_surfaceView.setVisibility(View.INVISIBLE);
        g_surfaceView_2.setVisibility(View.INVISIBLE);
        g_surfaceView_3.setVisibility(View.INVISIBLE);
    }

    public void set_surfaceVew_visible() {
        g_surfaceView.setVisibility(View.VISIBLE);
        g_surfaceView_2.setVisibility(View.VISIBLE);
        g_surfaceView_3.setVisibility(View.VISIBLE);
    }

    public void set_screen_transparent() {
        SurfaceView surfaceView = findViewById(R.id.lo_epg_sfv_live_tv);
        surfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        surfaceView.setBackgroundColor(Color.TRANSPARENT);
    }

    public void set_screen_black() {
        SurfaceView surfaceView = findViewById(R.id.lo_epg_sfv_live_tv);
        surfaceView.setBackgroundColor(Color.BLACK);
    }*/

    public static void set_callback(Callback callback) {
        g_callback = callback;
    }

    public String get_genre() {
        return gGenreName;
    }

    public EpgView get_epg_view() {
        return g_epg_view;
    }

    public ListLayer get_list_layer() {
        return gListLayer;
    }

    /*public SurfaceView get_surfaceVew() {
        return g_surfaceView;
    }

    public Handler get_handler() {
        return gHandler;
    }*/

    public Handler get_thread_handler() {
        if (gThreadHandler == null) {
            HandlerThread thread = new HandlerThread(EpgActivity.class.getSimpleName() + " thread handler");
            thread.start();
            gThreadHandler = new Handler(thread.getLooper());
        }
        return gThreadHandler;
    }

    /*public ProgramInfo get_current_channel_info() {
        return gListLayer.get_current_channel_info();
    }*/

    public EPGEvent get_current_epg_event() {
        List<EPGEvent> epgEventList = gListLayer.get_program_list();
        int position = gListLayer.get_last_focus_program_position();

        if (epgEventList != null && position < epgEventList.size())
            return epgEventList.get(position);

        return null;
    }

    public boolean has_ca_message_shown() {
        return g_CA_message.getVisibility() == View.VISIBLE;
    }

    /*public boolean has_surface() {
        if (g_surfaceView == null)
            return false;
        if (g_surfaceView_2 == null)
            return false;
        if (g_surfaceView_3 == null)
            return false;
        return true;
    }*/

    public boolean is_channel_focused() {
        View currentFocus = getCurrentFocus();
        if (currentFocus != null && currentFocus.getParent() instanceof MiddleFocusRecyclerView recyclerView) {
            return recyclerView.getId() == R.id.lo_epg_sub_list_rcv_channel;
        }
        return false;
    }

    public boolean is_mark_record(long channelId, EPGEvent epgEvent) {
        List<BookInfo> bookInfoList = g_dtv != null ? g_dtv.book_info_get_list() : null;
        return is_single_record(channelId, epgEvent) ||
               is_series_record(channelId, epgEvent) ||
               is_reserve_record(bookInfoList, epgEvent) ||
               is_reserve_series_record(bookInfoList, epgEvent);
    }

    public boolean is_mark_remind() {
        List<EPGEvent> epgEventList = gListLayer.get_program_list();
        int position = gListLayer.get_last_focus_program_position();

        if (epgEventList != null && position < epgEventList.size())
            return is_mark_remind(epgEventList.get(position));

        return false;
    }

    public boolean is_mark_remind(EPGEvent epgEvent) {
        return EpgProgramAdapter.is_reminded(epgEvent);
    }

    public boolean is_single_record(long channelId, EPGEvent epgEvent) {
        if (gChMgr == null)
            gChMgr = ChannelChangeManager.get_instance(this);
        return gChMgr.is_single_recording(channelId, epgEvent);
    }

    public boolean is_series_record(long channelId, EPGEvent epgEvent) {
        if (gChMgr == null)
            gChMgr = ChannelChangeManager.get_instance(this);
        return gChMgr.is_series_recording(channelId, epgEvent);
    }

    public boolean is_reserve_record(List<BookInfo> bookInfoList, EPGEvent epgEvent) {
        if (gChMgr == null)
            gChMgr = ChannelChangeManager.get_instance(this);
        return gChMgr.is_reserve_record(bookInfoList, epgEvent);
    }

    public boolean is_reserve_series_record(List<BookInfo> bookInfoList, EPGEvent epgEvent) {
        if (gChMgr == null)
            gChMgr = ChannelChangeManager.get_instance(this);
        return gChMgr.is_reserve_series_record(bookInfoList, epgEvent);
    }

    private void show_info() {
        if (gListLayer.is_adult_blocked()) {
            Log.w(TAG, "show_info: blocked adult channel");
            return;
        }
        if (show_info_detail())
            return;

        show_info_no_epg();
    }

    private boolean show_info_detail() {
        if (gDetailInfo == null)
            gDetailInfo = new HotkeyInfo(this);

        if (g_epg_view.is_lock_adult()) {
            Log.w(TAG, "show_info_detail: adult lock, not show detail");
            return false;
        }

        List<EPGEvent> epgEventList = gListLayer.get_program_list();

        if (epgEventList == null || epgEventList.isEmpty())
            return false;

        ProgramInfo channelInfo = gListLayer.get_current_channel_info();
        EPGEvent epgEvent = epgEventList.get(gListLayer.get_last_focus_program_position());
        long channelId = channelInfo.getChannelId();

        if (channelId != epgEvent.get_channel_id())
            epgEvent = g_dtv.get_present_event(channelId);

        if (epgEvent == null) {
            Log.e(TAG, "show_info_detail: epgEvent is NULL");
            return false;
        }

        if (is_mark_record(channelId, epgEvent))
            gDetailInfo.set_red_hint(R.string.hint_rcu_cancel_record);

        if (is_mark_remind(epgEvent))
            gDetailInfo.set_yellow_hint(R.string.hint_rcu_cancel_remind);

        gDetailInfo.show(g_epg, channelInfo, epgEvent);

        return true;
    }

    /** @noinspection UnusedReturnValue*/
    private boolean show_info_no_epg() {
        Log.d(TAG, "show_info_message: has no EPG");
        Snakebar.show(this, R.string.epg_no_data, Snakebar.LENGTH_SHORT);
        //Toast.makeText(this, this.getString(R.string.epg_no_data), Toast.LENGTH_SHORT).show();
        return true;
    }

    private void  show_mute() {
        ImageView muteIcon = findViewById(R.id.lo_epg_tv_mute);
        boolean   isShow   = View.VISIBLE == muteIcon.getVisibility();
        if (isShow)
            return;
        Log.d(TAG, "show_mute: ");
        muteIcon.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage(int errCode, String message) {
        String errorMessage = ErrorCodeUtils.getErrorMessage(this, errCode, message);
        runOnUiThread(() -> open_ca_message(errorMessage));
    }

    /*private void showErrorMessage(TVMessage msg) {
        //Log.e(TAG, "showErrorMessage: " + msg.getAvFrameStatus());
        //Log.e(TAG, "showErrorMessage: " + has_ca_message());
        if (msg.getAvFrameStatus() == 0) // av ok
            close_ca_message();
        //else
        //    open_ca_message(getString(R.string.error_e200));
    }*/

    private void hide_mute() {
        ImageView muteIcon = findViewById(R.id.lo_epg_tv_mute);
        boolean   isShow   = View.VISIBLE == muteIcon.getVisibility();

        if (!isShow)
            return;
        Log.d(TAG, "hide_mute: ");
        muteIcon.setVisibility(View.GONE);
    }

    private void open_genre() {
        if (null == g_GenreDialog)
            g_GenreDialog = new EpgHotKeyGenre(this);
        Log.d(TAG, "open_genre: open Genre dialog");
        g_GenreDialog.show();
    }

    /*private void open_zapping(int keyCode) {
        Log.d(TAG, "open_zapping: ");
        String chNum = String.valueOf(keyCode - KeyEvent.KEYCODE_0);
        ZappingDialog zappingDialog = new ZappingDialog(this, chNum, g_epg.get_all_tv_program_info_list());
        zappingDialog.show();
    }*/

    public void open_zapping(int keyCode) {
        gZappingChNum += String.valueOf(keyCode - KeyEvent.KEYCODE_0);
        get_thread_handler().removeCallbacks(gOpenZapping);
        get_thread_handler().post(gOpenZapping);
    }

    public Runnable open_zapping() {
        return () -> {
            gZappingDialog.set_all_data(gZappingChNum, gAllChList);
            runOnUiThread(() -> {
                gZappingDialog.show();
                gZappingChNum = "";
            });
        };
    }

    /*public void remind_program() {
        gListLayer.remind_program();
    }*/

    public void open_ca_message(String string) {
        if (has_ca_message_shown())
            return;
        runOnUiThread(() -> {
            g_CA_message.setText(string);
            g_CA_message.setVisibility(View.VISIBLE);
        });
    }

    public void close_genre() {
        if (null == g_GenreDialog)
            return;
        Log.d(TAG, "close_genre: close Genre dialog");
        g_GenreDialog.dismiss();
    }

    public void close_ca_message() {
        if (!has_ca_message_shown())
            return;
        runOnUiThread(() -> {
            g_CA_message.setVisibility(View.GONE);
        });
    }

    private int map_genre(String genre) {
        if (genre.equals(getString(R.string.genre_all)))
            return TV_ALL;
        else if (genre.equals(getString(R.string.genre_kids)))
            return TV_KID;
        else if (genre.equals(getString(R.string.genre_education)))
            return TV_EDUCATION;
        else if (genre.equals(getString(R.string.genre_news)))
            return TV_NEWS;
        else if (genre.equals(getString(R.string.genre_movie)))
            return TV_MOVIE;
        else if (genre.equals(getString(R.string.genre_variety)))
            return TV_VARIETY;
        else if (genre.equals(getString(R.string.genre_music)))
            return TV_MUSIC;
        else if (genre.equals(getString(R.string.genre_adult)))
            return TV_ADULT;
        else if (genre.equals(getString(R.string.genre_sports)))
            return TV_SPORT;
        else if (genre.equals(getString(R.string.genre_religion)))
            return TV_RELIGION;
        else if (genre.equals(getString(R.string.genre_uhd)))
            return TV_UHD;
        else if (genre.equals(getString(R.string.genre_shopping)))
            return TV_SHOPPING;

        Log.d(TAG, "map_genre MAPPING FAIL");
        return TV_ALL;
    }

    public void change_channel_from_zapping(ProgramInfo channelInfo) {
        long channelId = channelInfo.getChannelId();
        int serviceId = channelInfo.getServiceId();
        gListLayer.set_current_channel_num(channelInfo.getDisplayNum());
        g_epg_view.on_genre_changed_by_zapping(map_genre(getString(R.string.genre_all)), getString(R.string.genre_all));
        g_epg_view.tune_channel(channelId, serviceId, false); // zapping
    }

    void local_receiver_register() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(HomeActivity.ACTION_UPDATE_STORAGE_SERVER);

        g_localReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (refresh_channel(action))
                    Log.d(TAG, "onReceive: refresh channel successfully");
            }
        };

        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(g_localReceiver, filter);
    }

    void local_receiver_unregister() {
        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(g_localReceiver);
    }

    @SuppressLint("NotifyDataSetChanged")
    boolean refresh_channel(String action) {
        MiddleFocusRecyclerView channelView = findViewById(R.id.lo_epg_sub_list_rcv_channel);

        if (!HomeActivity.ACTION_UPDATE_STORAGE_SERVER.equals(action))
            return false;
        if (null == channelView)
            return false;
        if (null == channelView.getAdapter())
            return false;
        if (null == g_epg_view)
            return false;
        if (null == gListLayer)
            return false;
        // refresh
        EpgChannelAdapter channelAdapter = (EpgChannelAdapter) channelView.getAdapter();
        channelAdapter.notifyDataSetChanged();
        // focus again
        gListLayer.focus_channel();
        return true;
    }

    private void play_channel(boolean isPlayAV) {
        EpgView epgView = get_epg_view();
        epgView.play_channel(isPlayAV);
    }

    /*private void set_default_channel() {
        gListLayer.set_default_channel();;
    }*/

    //private void show_record_start_banner(/*long channelId*/) {
    //    gListLayer.show_record_start_banner(/*channelId*/);
    //}

    //private void show_record_stop_banner(/*long channelId*/) {
    //    gListLayer.show_record_stop_banner(/*channelId*/);
    //}

    //private void show_record_start_error(/*long channelId*/) {
    //    gListLayer.show_record_start_error(/*channelId*/);
    //}

    public void open_music_background() {
        runOnUiThread(() -> {
            View view = findViewById(R.id.epg_music_channel_bg);
            view.setVisibility(View.VISIBLE);
        });
    }

    public void close_music_background() {
        runOnUiThread(() -> {
            View view = findViewById(R.id.epg_music_channel_bg);
            view.setVisibility(View.GONE);
        });
    }
}
