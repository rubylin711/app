package com.prime.launcher.Home.LiveTV;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import static com.prime.launcher.Utils.ActivityUtils.*;
import static com.prime.datastructure.sysdata.ProgramInfo.PROGRAM_RADIO;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.prime.launcher.ACSDatabase.ACSDataProviderHelper;
import com.prime.launcher.BaseActivity;
import com.prime.launcher.CustomView.MessageDialog;
import com.prime.launcher.CustomView.TimeshiftBanner;
import com.prime.launcher.EPG.EpgActivity;
import com.prime.launcher.Home.ClosedCaptionUI;
import com.prime.launcher.Home.LiveTV.Music.MusicView;
import com.prime.launcher.Home.BlockChannel.BlockedChannel;
import com.prime.launcher.Home.BlockChannel.BlockedChannelDialog;
import com.prime.launcher.Home.LiveTV.Recommendation.RecommendationDialog;
import com.prime.launcher.Home.LiveTV.TvPackage.TvPackageDialog;
import com.prime.launcher.Home.LiveTV.QuickTune.QuickTuneDialog;
import com.prime.launcher.Home.LiveTV.Rank.RankListDialog;
import com.prime.launcher.Home.Menu.HomeMenuView;
import com.prime.launcher.Home.LiveTV.Zapping.ZappingDialog;
import com.prime.launcher.HomeActivity;
import com.prime.launcher.HomeApplication;
import com.prime.launcher.Mail.Mail;
import com.prime.launcher.Mail.MailDialog;
import com.prime.launcher.Mail.MailEnvelopeDialog;
import com.prime.launcher.Mail.MailManager;
import com.prime.launcher.R;
import com.prime.launcher.Ticker.Ticker;
import com.prime.launcher.Utils.JsonParser.JsonParser;
import com.prime.launcher.Utils.JsonParser.RankInfo;
import com.prime.launcher.Utils.JsonParser.RecommendPackage;
import com.prime.launcher.Utils.Utils;
import com.prime.launcher.ChannelChangeManager;
import com.prime.launcher.PrimeDtv;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.ServiceDefine.AvCmdMiddle;
import com.prime.datastructure.sysdata.FavGroup;
import com.prime.launcher.Subtitle.Subtitler;
import com.prime.datastructure.sysdata.EPGEvent;
import com.prime.datastructure.sysdata.MiscDefine;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.utils.LogUtils;
import com.prime.datastructure.utils.TVMessage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** @noinspection ConstantValue*/
@SuppressWarnings("CommentedOutCode")
public class LiveTvManager implements EpgActivity.Callback, BlockedChannelDialog.Callback, BlockedChannel.Callback {

    static final String TAG = LiveTvManager.class.getSimpleName();

    @IntDef({ScreenType.NORMAL_SCREEN, ScreenType.FULL_SCREEN, ScreenType.EPG_SCREEN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ScreenType {
        int NORMAL_SCREEN = 0;
        int FULL_SCREEN = 1;
        int EPG_SCREEN = 2;
    }

    public static final int DEFAULT_TUNER_ID = 0;
    public static final int DEFAULT_LIMIT_OF_USB_TOTAL_SIZE = 16000;//SystemProperties.getInt("persist.sys.prime.pvr_hdd_size_limit", 64000);
    private static final long THRESHOLD_FOR_CHANGE_CHANNEL = 200;

    public static final String DEFAULT_HOST_NAME = "https://epgstore.tbc.net.tw/acs-api";
    public static final String DEFAULT_ICON_PREFIX = "ch";
    public static final String PATH_CHANNEL = "channel";

    WeakReference<AppCompatActivity> g_ref;
    public Handler g_handler;
    ConstraintLayout.LayoutParams g_param_full_screen;
    ConstraintLayout.LayoutParams g_param_normal_screen;
    ConstraintLayout.LayoutParams g_param_message_screen;
    ConstraintLayout.LayoutParams g_param_epg_screen;

    public ConstraintLayout g_liveTvRoot;
    public FrameLayout g_frameLayout;
    public SurfaceView      g_liveTv;
    public SurfaceView      g_liveTv_2;
    public SurfaceView      g_liveTv_3;
    public TextView         g_liveTvMsg;
    public View             g_liveTvFrame;
    public MiniEPG          g_miniEPG;
    public ImageView        g_subtitle;
    public QuickTuneDialog  gDlgQuickTune;
    public BlockedChannel   g_blocked_channel;
    public MusicView        g_musicTv;
    public TvPackageDialog  g_tvPackageDialog;
    public MessageDialog    gMsgDialog;
    public TimeshiftBanner  g_timeshiftBanner;
    public MailEnvelopeDialog g_mailEnvelopeDialog;
    public ZappingDialog    gZappingDialog;

    private Subtitler g_subtitler;
    private boolean g_mail_is_running;
    private List<Integer> g_ticker_service_id_list;
    private int g_ticker_service_id;
    private int g_ticker_id;
    public boolean surface_is_init = false;

    // for switch Live TV screen size
    private final ConstraintSet g_originalSet;
    private final ConstraintSet g_fullScreenSet;

    BlockedChannelDialog g_blockedChannelDialog;

    public ChannelChangeManager g_chChangeMgr;
    ExecutorService g_executorService;
    List<RankInfo> g_rankInfos;
    List<RecommendPackage> g_recommendPackages;
    private final List<ProgramInfo> allChannels;
    private List<ProgramInfo> TypeTvChannels;
    private List<ProgramInfo> TypeRadioChannels;
    private static final Object mProgramListLock = new Object();
    private boolean isE200Showing = false;
    private boolean isMusicE200Showing = false;
    private static SurfaceHolder.Callback[] mSurfaceHolderCallback = new SurfaceHolder.Callback[3];

    // for channel up, channel down
    private long previousTimeMs;
    private long new_channelId;
    private boolean is_channel_changing;

    public LiveTvManager(AppCompatActivity activity) {
        g_ref = new WeakReference<>(activity);
        g_executorService = Executors.newSingleThreadExecutor();
        g_originalSet = new ConstraintSet();
        g_fullScreenSet = new ConstraintSet();
        g_handler = new Handler(Looper.getMainLooper());
        on_create_ui();
        synchronized(mProgramListLock) {
            allChannels = new ArrayList<>();
            TypeTvChannels = new ArrayList<>();
            TypeRadioChannels = new ArrayList<>();
        }
    }

    /** @noinspection SpellCheckingInspection*/
    public void on_create_ui(){
        LogUtils.d("QQQQQQQQQQQQQQ");
        g_liveTvRoot    = get().findViewById(R.id.lo_home_live_tv_container);
        g_frameLayout = get().findViewById(R.id.lo_home_live_tv_frame_layout);
        g_liveTv        = get().findViewById(R.id.lo_home_live_tv);
        g_liveTv_2        = get().findViewById(R.id.lo_home_live_tv_2);
        g_liveTv_3        = get().findViewById(R.id.lo_home_live_tv_3);
//        g_liveTv.setVisibility(View.VISIBLE);
//        g_liveTv_2.setVisibility(View.VISIBLE);
//        g_liveTv_3.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            g_liveTv.setSurfaceLifecycle(SurfaceView.SURFACE_LIFECYCLE_FOLLOWS_ATTACHMENT);
            g_liveTv_2.setSurfaceLifecycle(SurfaceView.SURFACE_LIFECYCLE_FOLLOWS_ATTACHMENT);
            g_liveTv_3.setSurfaceLifecycle(SurfaceView.SURFACE_LIFECYCLE_FOLLOWS_ATTACHMENT);
        }
        g_liveTvMsg     = get().findViewById(R.id.lo_home_live_tv_message);
        g_liveTvFrame   = get().findViewById(R.id.lo_home_live_tv_frame);
        g_subtitle      = get().findViewById(R.id.lo_live_tv_subtitle);
        g_subtitler     = new Subtitler(Looper.getMainLooper(), g_subtitle);
        g_miniEPG       = get().findViewById(R.id.lo_home_live_tv_mini_epg);
        g_musicTv       = get().findViewById(R.id.lo_home_live_tv_view_music);
        gZappingDialog  = new ZappingDialog(this);
        gDlgQuickTune   = new QuickTuneDialog(this);
        g_timeshiftBanner    = get().findViewById(R.id.lo_home_timeshift_banner);
        g_ticker_service_id_list = new ArrayList<>();
        g_mailEnvelopeDialog = null;
        get().g_rcvPopular   = get().findViewById(R.id.lo_home_rcv_popular);
        get().g_rcvApps      = get().findViewById(R.id.lo_home_rcv_apps);
        get().g_rcvAppsGames = get().findViewById(R.id.lo_home_rcv_apps_games);
        LogUtils.d("RRRRRRRRRRRRRR");
        init_surface_view();
        init_music_view();
        init_mute_status();
        init_screen_layout();
        init_screen_constraint_set();
        //init_error_message();
        init_acs_data();
        init_callback();
        on_click_screen();
        on_focus_screen();
    }

    public void on_create() {
        Log.d(TAG, "on_create: ");
        g_chChangeMgr = ChannelChangeManager.get_instance(get());
        g_blocked_channel = BlockedChannel.get_instance(get_current_channel(), get().get_thread_handler());
        g_chChangeMgr.set_timeshift_started(false);
        g_chChangeMgr.set_timeshift_callback_started(false);
    }

    public void on_resume() {
        Log.d(TAG, "on_resume: previous time = " + MiniEPG.ms_to_time(System.currentTimeMillis()));
        previousTimeMs = System.currentTimeMillis();
        update_all_channels();
        set_intro_time();
        //init_surface_view();
        register_blocked_channel_callback();
    }

    public void on_destroy() {
        Log.d(TAG, "on_destroy: ");
    }

    public void on_focus_screen() {
        g_liveTvFrame.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Log.d(TAG, "on_focus_screen: block to re-focus LinearLayout of ListManager");
                get().g_rcvPopular.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                get().g_rcvApps.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                get().g_rcvAppsGames.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            }
        });
        g_liveTvFrame.post(() -> g_liveTvFrame.requestFocus());
    }

    public void on_click_screen() {
        g_liveTvFrame.setOnClickListener(v -> {
            BaseActivity.set_global_key(KeyEvent.KEYCODE_DPAD_CENTER);

            if (is_block_screen()) {
                open_password_dialog();
                Log.d(TAG, "on_click_screen: alert pin code");
            }
            else if (is_full_screen()) {
                g_timeshiftBanner.timeshift_start();
                Log.d(TAG, "on_click_screen: timeshift start");
            }
            else {
                enter_fullscreen();
                Log.d(TAG, "on_click_screen: enter fullscreen");
            }
        });
    }

    public boolean on_key_down(KeyEvent event) {
        int keyCode = event.getKeyCode();

        if (press_back(keyCode, false))
            return true;

        if (set_channel(event))
            return false;

        if (!is_full_screen())
            return false;

        if (is_music_channel())
            return g_musicTv.on_key_down(event);

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
                open_password_dialog();
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                open_tv_package();
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                open_recommend();
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                open_ranking();
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                open_quick_tune();
                break;
            case KeyEvent.KEYCODE_INFO:
                g_miniEPG.show_info();
                break;
            case KeyEvent.KEYCODE_PROG_GREEN:
                g_miniEPG.show_function();
                break;
            case KeyEvent.KEYCODE_PROG_BLUE:
                g_miniEPG.show_genre();
                break;
            case KeyEvent.KEYCODE_PROG_YELLOW:
                g_miniEPG.setup_remind();
                break;
            case KeyEvent.KEYCODE_PROG_RED:
                g_miniEPG.setup_record();
                break;
        }

        return false;
    }

    @SuppressWarnings("IfStatementWithIdenticalBranches")
    public boolean on_key_up(KeyEvent event) {

        if (set_channel(event))
            return false;

        return false;
    }

    public void on_volume_changed() {
        AudioManager audioManager = get().getSystemService(AudioManager.class);
        int          audioVolume  = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        //Log.e(TAG, "on_volume_changed: " + audioVolume);
        if (audioVolume == 0)
            show_mute();
        else
            hide_mute();
    }

    public void on_record_start(TVMessage msg) {
        ProgramInfo channel = get_channel(msg.getRecChannelId());
        String channelNum = channel != null ? channel.getDisplayNum(com.prime.datastructure.utils.Utils.MAX_LENGTH_OF_CHANNEL_NUM) : "000";
        String channelName = channel != null ? channel.getDisplayName() : "";

        if (TVMessage.TYPE_PVR_RECORDING_START_SUCCESS != msg.getMsgType()) {
            Log.e(TAG, "on_record_start: FAIL");
            on_record_start_error();
            return;
        }

        Log.i(TAG, "on_record_start: SUCCESS, [channelId] " + msg.getRecChannelId() + ", [channel] " + channelNum + " " + channelName);
        if (is_music_channel(channel))
            g_musicTv.set_recording_status(channel);
        else if (g_miniEPG != null) {
            g_miniEPG.set_summary_record_status(channel);
            g_miniEPG.handle_book_series();
        }

        PrimeDtv primeDtv = HomeApplication.get_prime_dtv();
        get().set_fake_book_list(primeDtv.book_info_get_list());
        Utils.show_notification(get(), "CH" + channelNum + " " + get().getString(R.string.pvr_message_start_recording_hint));
    }

    public void on_record_stop(TVMessage msg) {
        ProgramInfo channel = g_chChangeMgr != null ? get_channel(g_chChangeMgr.get_record_stop_ch_id()) : null;
        String channelNum = channel != null ? channel.getDisplayNum(com.prime.datastructure.utils.Utils.MAX_LENGTH_OF_CHANNEL_NUM) : "000";
        String channelName = channel != null ? channel.getDisplayName() : "";

        if (TVMessage.TYPE_PVR_RECORDING_STOP_SUCCESS != msg.getMsgType()) {
            Log.e(TAG, "on_record_stop: FAIL");
            on_record_stop_error();
            return;
        }

        Log.i(TAG, "on_record_stop: SUCCESS, [channel] " + channelNum + " " + channelName);
        if (is_music_channel(channel))
            g_musicTv.set_recording_status(channel);
        else if (g_miniEPG != null)
            g_miniEPG.set_summary_record_status(channel);

        Utils.show_notification(get(), "CH" + channelNum + " " + get().getString(R.string.pvr_message_stop_recording_hint));
    }

    public void on_record_start_error() {
        if (null == g_chChangeMgr)
            return;

        ProgramInfo channel = get_channel(g_chChangeMgr.get_last_record_ch_id());
        if (null == channel)
            return;

        String channelNum = channel.getDisplayNum(com.prime.datastructure.utils.Utils.MAX_LENGTH_OF_CHANNEL_NUM);
        show_message_dialog(R.string.error_e606);
        Utils.show_notification(get(), "CH" + channelNum + " " + get().getString(R.string.pvr_message_stop_recording_hint));

        g_chChangeMgr.remove_record_channel();
        Log.w(TAG, "on_record_start_error: remove recording channel = " + channelNum + " " + channel.getDisplayName());
    }

    public void on_record_stop_error() {
        // TODO: show error message
        Log.e(TAG, "on_record_stop_error: TODO: show error message");
    }

    public void on_record_completed(TVMessage msg) {
        int recId = msg.getRecId();
        long channelId = g_chChangeMgr.get_record_channel_id(recId);

        if (false) {
            ProgramInfo channel = get_channel(channelId);
            String channelNum = channel != null ? channel.getDisplayNum(com.prime.datastructure.utils.Utils.MAX_LENGTH_OF_CHANNEL_NUM) : "000";
            String channelName = channel != null ? channel.getDisplayName() : "NULL";
            Log.e(TAG, "on_record_completed: [Channel] " + channelNum + " " + channelName);
        }

        Log.i(TAG, "on_record_completed: [Record ID] " + recId + ", [Channel ID] " + channelId);
        g_chChangeMgr.pvr_record_stop(channelId);
    }

    public void on_timeshift_start(TVMessage msg) {
        if (TVMessage.TYPE_PVR_TIMESHIFT_SUCCESS != msg.getMsgType()) {
            Log.e(TAG, "on_timeshift_start: FAIL");
            return;
        }
        Log.i(TAG, "on_timeshift_start: SUCCESS");
        g_timeshiftBanner.show();
    }

    public void on_timeshift_stop(TVMessage msg) {
        if (TVMessage.TYPE_PVR_TIMESHIFT_STOP_SUCCESS != msg.getMsgType()) {
            Log.e(TAG, "on_timeshift_stop: FAIL");
            return;
        }
        Log.i(TAG, "on_timeshift_stop: SUCCESS");
        g_timeshiftBanner.hide();
    }

    public void on_timeshift_pause(TVMessage msg) {
        if (TVMessage.TYPE_PVR_TIMESHIFT_PAUSE_SUCCESS != msg.getMsgType()) {
            Log.e(TAG, "on_timeshift_pause: FAIL");
            return;
        }
        Log.i(TAG, "on_timeshift_pause: SUCCESS");
    }

    public void on_timeshift_resume(TVMessage msg) {
        if (TVMessage.TYPE_PVR_TIMESHIFT_RESUME_SUCCESS != msg.getMsgType()) {
            Log.e(TAG, "on_timeshift_resume: FAIL");
            return;
        }
        Log.i(TAG, "on_timeshift_resume: SUCCESS");
    }

    public void on_epg_opened() {
//        set_screen_epg();
    }

    public void on_epg_closed() {
//        set_screen_normal();
        check_blocked_channel(g_chChangeMgr.get_default_channel());
    }

    @Override
    public void on_unlock_program() {
        long channelId = g_chChangeMgr.get_mini_epg_cur_id();
        ProgramInfo programInfo = get_channel(channelId);
        if(programInfo != null) {
            g_blocked_channel.unblock_channel();
            Log.d(TAG,"unlock program = "+programInfo.getDisplayName());
            change_channel(this, programInfo, true, true);
        }
        else
            Log.d(TAG,"no program !!! ");

    }

    @Override
    public void on_isLock_changed(boolean isLock) {
        if(g_blocked_channel != null)
            if(isLock) {
                Log.d(TAG,"channel["+g_blocked_channel.get_cur_channel().getDisplayName()+"]"+" channel blocked !!!!!");
                g_chChangeMgr.change_channel_stop(0, 0);
                show_blocked_channel_bg();
                //g_chChangeMgr.change_channel_by_id(g_blocked_channel.get_cur_channel().getChannelId());
            }
            else {
                Log.d(TAG,"channel["+g_blocked_channel.get_cur_channel().getDisplayName()+"]"+" channel unblocked !!!!!");
                hide_blocked_channel_bg();
                close_password_dialog();
                close_ca_message();
                g_chChangeMgr.change_channel_by_id(g_blocked_channel.get_cur_channel().getChannelId());
            }
    }

    @Override
    public void on_tuner_lock(ProgramInfo channelInfo) {
        ProgramInfo p1 = get().g_dtv.get_program_by_channel_id(channelInfo.getChannelId());
        //LogUtils.d("tunerID "+ p1.getTunerId()+ " "+channelInfo.getTunerId());
        if(p1 == null)
            return;
        boolean isLock = get().g_dtv.get_tuner_status(p1.getTunerId());
        boolean isMusicChannel = is_music_channel(channelInfo);

        //Log.e(TAG, "on_tuner_lock: [tuner lock] " + isLock + " CH "+channelInfo.getDisplayNum()+" "+channelInfo.getDisplayName());
        if (!isLock) {
            // show CA message
            if (!isMusicChannel) {
                isE200Showing = true;
                show_ca_message(get().getString(R.string.error_e200));
            }

            // check record locked
            g_handler.postDelayed(this::on_record_lock, 1000);

            // stop timeshift
            if (g_timeshiftBanner.is_timeshift_start())
                g_timeshiftBanner.timeshift_stop(true);
        }else{
            if(isE200Showing && get().g_dtv.av_control_get_play_status(0) != AvCmdMiddle.PESI_SVR_AV_LIVEPLAY_STATE)
                g_chChangeMgr.change_channel_by_id(g_chChangeMgr.get_cur_ch_id());
            isE200Showing = false;
        }

        if (isMusicChannel) {
            if (isLock) {
                if (isMusicE200Showing) {
                    g_musicTv.hide_error_code();
                    get().runOnUiThread(() -> g_musicTv.setVisibility(View.VISIBLE));
                    close_ca_message();
                    isMusicE200Showing = false;
                }
            }
            else {
                isMusicE200Showing = true;
                if (is_full_screen())
                    g_musicTv.show_error_code(true, get().getString(R.string.error_e200), null);
                else {
                    get().runOnUiThread(() -> g_musicTv.setVisibility(View.GONE));
                    show_ca_message(get().getString(R.string.error_e200));
                }
            }
        }
    }

    public void on_record_lock() {
        boolean recordLock = g_chChangeMgr.is_record_locked();
        //Log.e(TAG, "on_record_lock: [record lock] " + recordLock);

        // show error E606
        if (g_chChangeMgr.has_recording() && !recordLock) {
            g_chChangeMgr.pvr_record_stop_all();
            show_message_dialog(R.string.error_e606);
        }
    }

    public void setSurfaceToPlayer(SurfaceView surfaceView, int index) {
        get().g_dtv.setSurface(get(),surfaceView.getHolder().getSurface(),index);
        surfaceView.getHolder().removeCallback(mSurfaceHolderCallback[index]);
        mSurfaceHolderCallback[index] = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                Log.d(TAG, "AvCmdMiddle mTunerId["+index+"] surfaceCreated: " + holder.getSurface() + holder +
                        " surface = "+holder.getSurface());
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                Log.d(TAG, "AvCmdMiddle mTunerId["+index+"] surfaceChanged: SurfaceHolder = " + holder +
                        " surface = "+holder.getSurface());
                get().g_dtv.setSurface(get(),holder.getSurface(),index);
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                Log.d(TAG, "AvCmdMiddle mTunerId["+index+"] surfaceDestroyed: SurfaceHolder = " + holder);
//                mSurface = null;
            }
        };
        surfaceView.getHolder().addCallback(mSurfaceHolderCallback[index]);
    }

    public void init_surface_view() {
        LogUtils.d("init_surface_view g_dtv = "+get().g_dtv);
        if (get().g_dtv == null)
            return;
        ClosedCaptionUI.getInstance().setClosedCaptionLayout(get(),g_frameLayout);


        setSurfaceToPlayer(g_liveTv,0);
        setSurfaceToPlayer(g_liveTv_2,1);
        setSurfaceToPlayer(g_liveTv_3,2);
        get().g_dtv.set_surface_view(get(), g_liveTv, 0);
        get().g_dtv.set_surface_view(get(), g_liveTv_2, 1);
        get().g_dtv.set_surface_view(get(), g_liveTv_3, 2);
        surface_is_init = true;
    }

    public void init_music_view() {
        //if (get().g_dtv == null)
        //    return;
        g_musicTv.init(get());
    }

    public void init_mute_status() {
        AudioManager audioManager = get().getSystemService(AudioManager.class);
        int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (volume == 0)
            show_mute();
        else
            hide_mute();
    }

    public void init_screen_layout() {

        // full screen
        g_param_full_screen = new ConstraintLayout.LayoutParams(g_liveTvRoot.getLayoutParams());
        g_param_full_screen.width      = MATCH_PARENT;
        g_param_full_screen.height     = MATCH_PARENT;
        g_param_full_screen.topMargin  = 0;
        g_param_full_screen.leftMargin = 0;

        // normal screen
        g_param_normal_screen = new ConstraintLayout.LayoutParams(g_liveTvRoot.getLayoutParams());
        g_param_normal_screen.width      = get().getResources().getDimensionPixelSize(R.dimen.home_live_tv_w);
        g_param_normal_screen.height     = get().getResources().getDimensionPixelSize(R.dimen.home_live_tv_h);
        g_param_normal_screen.topMargin  = get().getResources().getDimensionPixelSize(R.dimen.home_live_tv_margin_top);
        g_param_normal_screen.leftMargin = get().getResources().getDimensionPixelSize(R.dimen.home_live_tv_margin_start);
        g_param_normal_screen.topToTop   = ConstraintLayout.LayoutParams.PARENT_ID;
        g_param_normal_screen.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;

        // message screen
        g_param_message_screen = new ConstraintLayout.LayoutParams(g_liveTvRoot.getLayoutParams());
        g_param_message_screen.width      = 0;
        g_param_message_screen.height     = 0;
        g_param_message_screen.topToTop   = ConstraintLayout.LayoutParams.PARENT_ID;
        g_param_message_screen.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        g_param_message_screen.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        g_param_message_screen.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;

        // epg screen
        g_param_epg_screen = new ConstraintLayout.LayoutParams(g_liveTvRoot.getLayoutParams());
        g_param_epg_screen.width      = get().getResources().getDimensionPixelSize(R.dimen.lo_epg_sfv_live_tv_width);
        g_param_epg_screen.height     = get().getResources().getDimensionPixelSize(R.dimen.lo_epg_sfv_live_tv_height);
        g_param_epg_screen.topMargin  = 60;
        g_param_epg_screen.leftMargin = 60;
        g_param_epg_screen.topToTop   = ConstraintLayout.LayoutParams.PARENT_ID;
        g_param_epg_screen.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
    }

    public void init_screen_constraint_set() {
        ConstraintLayout rootLayout = get().findViewById(R.id.lo_home_live_tv_container);
        // original screen
        g_originalSet.clone(rootLayout);
        // full screen
        g_fullScreenSet.clone(rootLayout);
        g_fullScreenSet.clear(R.id.lo_home_live_tv_container); // clear ConstraintSet
        g_fullScreenSet.connect(R.id.lo_home_live_tv_container, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        g_fullScreenSet.connect(R.id.lo_home_live_tv_container, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        g_fullScreenSet.connect(R.id.lo_home_live_tv_container, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        g_fullScreenSet.connect(R.id.lo_home_live_tv_container, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
    }

    public void init_error_message() {
        g_liveTvMsg.setText(R.string.error_e200);
    }

    public void init_acs_data() {
        update_rank_list(false); // init ACS data
        update_recommend_packages(false); // init ACS data
    }

    public void init_callback() {
        EpgActivity.set_callback(this);
    }

    public void init_channel_manager() {
        g_chChangeMgr = ChannelChangeManager.get_instance(get());
        g_blocked_channel = BlockedChannel.get_instance(get_current_channel());
    }

    public void update_rank_list(boolean fromACS) {
        Log.d(TAG, "update_rank_list: from ACS: " + fromACS);
        String jsonRankList = ACSDataProviderHelper.get_acs_provider_data(get(), "rank_list");
        g_rankInfos = JsonParser.parse_ranking_info(jsonRankList);
    }

    public void update_recommend_packages(boolean fromACS) {
        Log.d(TAG, "update_recommend_packages: from ACS: " + fromACS);
        String json = ACSDataProviderHelper.get_acs_provider_data(get(), "recommend_packages");
        if (false)
            json = "[{\"PackageName\": \"豪華電影套餐\",\"PackageId\": \"10190\",\"ChannelList\": [{\"ServiceId\": \"2046\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3481-ch2046.png\"},{\"ServiceId\": \"2047\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3481-ch2047.png\"},{\"ServiceId\": \"2095\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3481-ch2095.png\"},{\"ServiceId\": \"2096\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3481-ch2096.png\"},{\"ServiceId\": \"3024\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3481-ch3024.png\"},{\"ServiceId\": \"3026\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3481-ch3026.png\"},{\"ServiceId\": \"3023\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3481-ch3023.png\"},{\"ServiceId\": \"3025\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3481-ch3025.png\"}]}," +
                    "{\"PackageId\": \"10191\",\"PackageName\": \"娛樂套餐\",\"ChannelList\": [{\"ServiceId\": \"2051\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3510-ch2051.png\"},{\"ServiceId\": \"2022\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3510-ch2022.png\"},{\"ServiceId\": \"4522\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3510-ch4522.png\"},{\"ServiceId\": \"2048\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3510-ch2048.png\"},{\"ServiceId\": \"2094\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3510-ch2094.png\"},{\"ServiceId\": \"2034\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3510-ch2034.png\"},{\"ServiceId\": \"2049\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3510-ch2049.png\"},{\"ServiceId\": \"2097\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3510-ch2097.png\"}]}," +
                    "{\"PackageId\": \"10192\",\"PackageName\": \"知性套餐\",\"ChannelList\": [{\"ServiceId\": \"2098\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3539-ch2098.png\"},{\"ServiceId\": \"2083\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3539-ch2083.png\"},{\"ServiceId\": \"2084\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3539-ch2084.png\"},{\"ServiceId\": \"3028\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3539-ch3028.png\"},{\"ServiceId\": \"3027\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3539-ch3027.png\"},{\"ServiceId\": \"7039\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3539-ch7039.png\"},{\"ServiceId\": \"2525\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3539-ch2525.png\"},{\"ServiceId\": \"7055\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3539-ch7055.png\"},{\"ServiceId\": \"7057\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3539-ch7057.png\"}]}," +
                    "{\"PackageId\": \"10193\",\"PackageName\": \"HBO套餐\",\"ChannelList\": [{\"ServiceId\": \"3024\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3568-ch3024.png\"},{\"ServiceId\": \"3026\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3568-ch3026.png\"},{\"ServiceId\": \"3023\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3568-ch3023.png\"},{\"ServiceId\": \"3025\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3568-ch3025.png\"}]}," +
                    "{\"PackageId\": \"10195\",\"PackageName\": \"運動套餐\",\"ChannelList\": [{\"ServiceId\": \"7050\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3626-ch7050.png\"},{\"ServiceId\": \"7049\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3626-ch7049.png\"},{\"ServiceId\": \"7054\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3626-ch7054.png\"},{\"ServiceId\": \"7047\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3626-ch7047.png\"},{\"ServiceId\": \"7052\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3626-ch7052.png\"},{\"ServiceId\": \"7053\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3626-ch7053.png\"},{\"ServiceId\": \"7051\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3626-ch7051.png\"},{\"ServiceId\": \"7048\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3626-ch7048.png\"},{\"ServiceId\": \"2042\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3626-ch2042.png\"}]}," +
                    "{\"PackageId\": \"10194\",\"PackageName\": \"兒童套餐\",\"ChannelList\": [{\"ServiceId\": \"7061\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3597-ch7061.png\"},{\"ServiceId\": \"7060\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3597-ch7060.png\"},{\"ServiceId\": \"4529\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3597-ch4529.png\"},{\"ServiceId\": \"2057\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3597-ch2057.png\"},{\"ServiceId\": \"2058\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3597-ch2058.png\"},{\"ServiceId\": \"2059\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3597-ch2059.png\"}]}," +
                    "{\"PackageId\": \"8001\",\"PackageName\": \"彩虹\",\"ChannelList\": [{\"ServiceId\": \"1511\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8001-ch1511.png\"},{\"ServiceId\": \"1512\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8001-ch1512.png\"},{\"ServiceId\": \"1513\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8001-ch1513.png\"}]}," +
                    "{\"PackageId\": \"8057\",\"PackageName\": \"松視\",\"ChannelList\": [{\"ServiceId\": \"3511\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8057-ch3511.png\"},{\"ServiceId\": \"3512\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8057-ch3512.png\"},{\"ServiceId\": \"3513\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8057-ch3513.png\"}]}," +
                    "{\"PackageId\": \"8029\",\"PackageName\": \"潘朵啦\",\"ChannelList\": [{\"ServiceId\": \"3021\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8029-ch3021.png\"},{\"ServiceId\": \"3022\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8029-ch3022.png\"}]}," +
                    "{\"PackageId\": \"5909\",\"PackageName\": \"樂活\",\"ChannelList\": [{\"ServiceId\": \"3515\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk5909-ch3515.png\"}]}," +
                    "{\"PackageId\": \"8229\",\"PackageName\": \"情趣\",\"ChannelList\": [{\"ServiceId\": \"1507\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8229-ch1507.png\"},{\"ServiceId\": \"1508\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8229-ch1508.png\"},{\"ServiceId\": \"1509\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8229-ch1509.png\"}]}," +
                    "{\"PackageId\": \"8927\",\"PackageName\": \"極限奧視蜜桃\",\"ChannelList\": [{\"ServiceId\": \"2060\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8927-ch2060.png\"},{\"ServiceId\": \"2038\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8927-ch2038.png\"},{\"ServiceId\": \"2037\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8927-ch2037.png\"}]}," +
                    "{\"PackageId\": \"10190\",\"PackageName\": \"豪華電影套餐\",\"ChannelList\": [{\"ServiceId\": \"2046\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3481-ch2046.png\"},{\"ServiceId\": \"2047\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3481-ch2047.png\"},{\"ServiceId\": \"2095\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3481-ch2095.png\"},{\"ServiceId\": \"2096\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3481-ch2096.png\"},{\"ServiceId\": \"3024\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3481-ch3024.png\"},{\"ServiceId\": \"3026\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3481-ch3026.png\"},{\"ServiceId\": \"3023\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3481-ch3023.png\"},{\"ServiceId\": \"3025\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3481-ch3025.png\"}]}," +
                    "{\"PackageId\": \"10191\",\"PackageName\": \"娛樂套餐\",\"ChannelList\": [{\"ServiceId\": \"2051\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3510-ch2051.png\"},{\"ServiceId\": \"2022\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3510-ch2022.png\"},{\"ServiceId\": \"4522\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3510-ch4522.png\"},{\"ServiceId\": \"2048\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3510-ch2048.png\"},{\"ServiceId\": \"2094\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3510-ch2094.png\"},{\"ServiceId\": \"2034\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3510-ch2034.png\"},{\"ServiceId\": \"2049\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3510-ch2049.png\"},{\"ServiceId\": \"2097\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3510-ch2097.png\"}]}," +
                    "{\"PackageId\": \"10192\",\"PackageName\": \"知性套餐\",\"ChannelList\": [{\"ServiceId\": \"2098\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3539-ch2098.png\"},{\"ServiceId\": \"2083\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3539-ch2083.png\"},{\"ServiceId\": \"2084\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3539-ch2084.png\"},{\"ServiceId\": \"3028\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3539-ch3028.png\"},{\"ServiceId\": \"3027\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3539-ch3027.png\"},{\"ServiceId\": \"7039\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3539-ch7039.png\"},{\"ServiceId\": \"2525\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3539-ch2525.png\"},{\"ServiceId\": \"7055\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3539-ch7055.png\"},{\"ServiceId\": \"7057\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3539-ch7057.png\"}]}," +
                    "{\"PackageId\": \"10193\",\"PackageName\": \"HBO套餐\",\"ChannelList\": [{\"ServiceId\": \"3024\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3568-ch3024.png\"},{\"ServiceId\": \"3026\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3568-ch3026.png\"},{\"ServiceId\": \"3023\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3568-ch3023.png\"},{\"ServiceId\": \"3025\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3568-ch3025.png\"}]}," +
                    "{\"PackageId\": \"10195\",\"PackageName\": \"運動套餐\",\"ChannelList\": [{\"ServiceId\": \"7050\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3626-ch7050.png\"},{\"ServiceId\": \"7049\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3626-ch7049.png\"},{\"ServiceId\": \"7054\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3626-ch7054.png\"},{\"ServiceId\": \"7047\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3626-ch7047.png\"},{\"ServiceId\": \"7052\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3626-ch7052.png\"},{\"ServiceId\": \"7053\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3626-ch7053.png\"},{\"ServiceId\": \"7051\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3626-ch7051.png\"},{\"ServiceId\": \"7048\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3626-ch7048.png\"},{\"ServiceId\": \"2042\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3626-ch2042.png\"}]}," +
                    "{\"PackageId\": \"10194\",\"PackageName\": \"兒童套餐\",\"ChannelList\": [{\"ServiceId\": \"7061\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3597-ch7061.png\"},{\"ServiceId\": \"7060\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3597-ch7060.png\"},{\"ServiceId\": \"4529\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3597-ch4529.png\"},{\"ServiceId\": \"2057\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3597-ch2057.png\"},{\"ServiceId\": \"2058\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3597-ch2058.png\"},{\"ServiceId\": \"2059\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk3597-ch2059.png\"}]}," +
                    "{\"PackageId\": \"8001\",\"PackageName\": \"彩虹\",\"ChannelList\": [{\"ServiceId\": \"1511\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8001-ch1511.png\"},{\"ServiceId\": \"1512\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8001-ch1512.png\"},{\"ServiceId\": \"1513\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8001-ch1513.png\"}]}," +
                    "{\"PackageId\": \"8057\",\"PackageName\": \"松視\",\"ChannelList\": [{\"ServiceId\": \"3511\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8057-ch3511.png\"},{\"ServiceId\": \"3512\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8057-ch3512.png\"},{\"ServiceId\": \"3513\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8057-ch3513.png\"}]}," +
                    "{\"PackageId\": \"8029\",\"PackageName\": \"潘朵啦\",\"ChannelList\": [{\"ServiceId\": \"3021\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8029-ch3021.png\"},{\"ServiceId\": \"3022\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8029-ch3022.png\"}]}," +
                    "{\"PackageId\": \"5909\",\"PackageName\": \"樂活\",\"ChannelList\": [{\"ServiceId\": \"3515\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk5909-ch3515.png\"}]}," +
                    "{\"PackageId\": \"8229\",\"PackageName\": \"情趣\",\"ChannelList\": [{\"ServiceId\": \"1507\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8229-ch1507.png\"},{\"ServiceId\": \"1508\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8229-ch1508.png\"},{\"ServiceId\": \"1509\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8229-ch1509.png\"}]}," +
                    "{\"PackageId\": \"8927\",\"PackageName\": \"極限奧視蜜桃\",\"ChannelList\": [{\"ServiceId\": \"2060\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8927-ch2060.png\"},{\"ServiceId\": \"2038\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8927-ch2038.png\"},{\"ServiceId\": \"2037\",\"ProgramName\": \"\",\"ProgramPoster\": \"https://epgstore.tbc.net.tw/acs-api//package/icon/pk8927-ch2037.png\"}]}]";
        if (false)
            json = "[{\"PackageName\":\"成人套餐\",\"PackageId\":\"99999991\",\"ChannelList\":[" +
                    "{\"ServiceId\":\"36024\",\"ProgramName\":\"成人全餐\",\"ProgramPoster\":\"https://epgstore.tbc.net.tw/acs-api//Dmg/package/icon/pkDMG_Adult-ch36024.png\"}," +
                    "{\"ServiceId\":\"36019\",\"ProgramName\":\"成人全餐\",\"ProgramPoster\":\"https://epgstore.tbc.net.tw/acs-api//Dmg/package/icon/pkDMG_Adult-ch36019.png\"}," +
                    "{\"ServiceId\":\"36010\",\"ProgramName\":\"成人A組-情趣套餐\",\"ProgramPoster\":\"https://epgstore.tbc.net.tw/acs-api//Dmg/package/icon/pkDMG_Adult-ch36010.png\"}," +
                    "{\"ServiceId\":\"36011\",\"ProgramName\":\"成人B組-彩虹套餐\",\"ProgramPoster\":\"https://epgstore.tbc.net.tw/acs-api//Dmg/package/icon/pkDMG_Adult-ch36011.png\"}," +
                    "{\"ServiceId\":\"36012\",\"ProgramName\":\"成人C組-松視套餐\",\"ProgramPoster\":\"https://epgstore.tbc.net.tw/acs-api//Dmg/package/icon/pkDMG_Adult-ch36012.png\"}," +
                    "{\"ServiceId\":\"36013\",\"ProgramName\":\"潘朵啦\",\"ProgramPoster\":\"https://epgstore.tbc.net.tw/acs-api//Dmg/package/icon/pkDMG_Adult-ch36013.png\"}," +
                    "{\"ServiceId\":\"36023\",\"ProgramName\":\"樂活頻道\",\"ProgramPoster\":\"https://epgstore.tbc.net.tw/acs-api//Dmg/package/icon/pkDMG_Adult-ch36023.png\"}]}]";

        Log.d(TAG, "update_recommend_packages: json = " + json);

        g_recommendPackages = JsonParser.parse_recommend_packages(json);

        if (null == g_tvPackageDialog)
            g_tvPackageDialog = new TvPackageDialog(get(), g_recommendPackages);
        else
            g_tvPackageDialog.update_data(g_recommendPackages);

        if (fromACS && g_tvPackageDialog.isShowing()) {
            g_tvPackageDialog.update_type_list();
            g_tvPackageDialog.update_content_list();
        }
    }

    public void update_present_follow(long channelId) {
        if (g_chChangeMgr == null)
            g_chChangeMgr = ChannelChangeManager.get_instance(get());
        ProgramInfo curChannel = g_chChangeMgr.get_cur_channel();
        if (curChannel.getChannelId() != channelId) {
            Log.e(TAG, "update_present_follow: not same channel");
            return;
        }

        if (is_music_channel(curChannel))
            g_musicTv.update_present_follow(curChannel);
        else
            g_miniEPG.set_channel_info(curChannel);
    }

    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    public ProgramInfo get_current_channel() {
        ChannelChangeManager changeMgr;
        long chId;
        ProgramInfo programInfo = null;

        changeMgr = ChannelChangeManager.get_instance(get());
        chId      = changeMgr.get_cur_ch_id();
        if(get().g_liveTvMgr != null)
            programInfo = get().g_liveTvMgr.get_channel(chId);
        return programInfo;
    }

    public EPGEvent get_current_present() {
        ChannelChangeManager changeManager = ChannelChangeManager.get_instance(get());
        if (null == get().g_dtv) {
            Log.e(TAG, "get_current_present: DTV is null");
            return null;
        }
        ProgramInfo curChannel = get().g_liveTvMgr.get_channel(changeManager.get_cur_ch_id());
        if (null == curChannel) {
            Log.e(TAG, "get_current_present: curChannel is null");
            return null;
        }
        return get().g_dtv.get_present_event(changeManager.get_cur_ch_id());
    }

    public EPGEvent get_current_follow() {
        ChannelChangeManager changeManager = ChannelChangeManager.get_instance(get());
        if (null == get().g_dtv)
            return null;
        return get().g_dtv.get_follow_event(changeManager.get_cur_ch_id());
    }

    public ProgramInfo get_channel(String serviceId) {
        if (serviceId != null) {
            int SERVICE_ID;
            try {
                SERVICE_ID = Integer.parseInt(serviceId);
            } catch (NumberFormatException e) {
                Log.w(TAG,"get_channel serviceId = "+serviceId+" is not Integer");
                return null;
            }

            for (ProgramInfo channel : get_channels()) {
                if (SERVICE_ID == channel.getServiceId())
                    return channel;
            }
        }
        Log.w(TAG, "get_channel: not found service Id");
        return null;
    }

    public ProgramInfo get_channel(long channelId) {
        for (ProgramInfo channel : get_channels()) {
            if (channelId == channel.getChannelId())
                return channel;
        }
        Log.w(TAG, "get_channel: not found channel Id");
        return null;
    }

    public ProgramInfo get_channel(int displayNumber) {
        for (ProgramInfo channel : get_channels()) {
            if (displayNumber == channel.getDisplayNum())
                return channel;
        }
        Log.e(TAG, "get_channel: not found channel number");
        return null;
    }

    public void update_all_channels(){
        synchronized(mProgramListLock) {
            allChannels.clear();
            TypeTvChannels.clear();
            TypeRadioChannels.clear();

            if (get().g_dtv != null) {
                TypeTvChannels = get().g_dtv.get_program_info_list(FavGroup.ALL_TV_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.POS_ALL);
                //LogUtils.d("TV number: "+ TypeTvChannels.size());
                TypeRadioChannels = get().g_dtv.get_program_info_list(FavGroup.ALL_RADIO_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.POS_ALL);
                //LogUtils.d("Radio number: "+ TypeRadioChannels.size());
            }
            allChannels.addAll(TypeTvChannels);
            allChannels.addAll(TypeRadioChannels);
            allChannels.sort(Comparator.comparingInt(ProgramInfo::getDisplayNum));

            /*for(ProgramInfo p : allChannels){
                LogUtils.d("CH "+p.getDisplayNum()+" "+p.getDisplayName());
            }*/
        }
    }

    public List<ProgramInfo> get_channels() {
        synchronized(mProgramListLock) {
            //noinspection SizeReplaceableByIsEmpty
            if (allChannels.size() == 0) {
                update_all_channels();
            }

            return allChannels;
        }
    }

    public static String get_channel_icon_url(Context context, String serviceId) {
        //noinspection ConstantConditions
        if (true) {
            String storageServer = ACSDataProviderHelper.get_acs_provider_data(context, "storage_server");

            if (null == storageServer)
                storageServer = DEFAULT_HOST_NAME;

            return storageServer + "/" + PATH_CHANNEL + "/" + DEFAULT_ICON_PREFIX + serviceId + ".png";
        }
        else
            return DEFAULT_HOST_NAME + "/" + PATH_CHANNEL + "/" + DEFAULT_ICON_PREFIX + serviceId + ".png";
    }

    /** @noinspection UnnecessaryLocalVariable*/
    @SuppressLint("DiscouragedApi")
    public static int get_channel_icon_res_id(Context context, String serviceId) {
        if (true)
            return 0;
        String resName = DEFAULT_ICON_PREFIX + serviceId;
        String resType = "mipmap";
        String pkgName = context.getPackageName();

        int resId = context.getResources().getIdentifier(resName, resType, pkgName);
        return resId;
        //return resId == 0 ? R.mipmap.dmg_logo : resId;
    }

    public ProgramInfo get_first_music_channel() {
        List<ProgramInfo> TypeRadioChannels = get().g_dtv.get_program_info_list(FavGroup.ALL_RADIO_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.POS_ALL);
        if(!TypeRadioChannels.isEmpty())
            return TypeRadioChannels.get(0);
        return null;
    }

    public ProgramInfo get_default_channel() {
        if (null == g_chChangeMgr)
            g_chChangeMgr = ChannelChangeManager.get_instance(get());
        return g_chChangeMgr.get_default_channel();
    }

    public boolean set_screen_full(boolean enable) {
        if (is_full_screen() && enable || is_normal_screen() && !enable)
            return true;
        Log.d(TAG, "set_screen_full: " + (enable ? "ENABLE" : "DISABLE"));

        final int FULL_SCREEN_TEXT_SIZE   = get().getResources().getDimensionPixelSize(R.dimen.home_live_tv_message_full_screen_text_size);
        final int FULL_SCREEN_PADDING     = get().getResources().getDimensionPixelSize(R.dimen.home_live_tv_message_full_screen_padding);
        final int NORMAL_SCREEN_TEXT_SIZE = get().getResources().getDimensionPixelSize(R.dimen.home_live_tv_message_normal_screen_text_size);
        final int NORMAL_SCREEN_PADDING   = get().getResources().getDimensionPixelSize(R.dimen.home_live_tv_message_normal_screen_padding);
        int textSize = enable ? FULL_SCREEN_TEXT_SIZE : NORMAL_SCREEN_TEXT_SIZE;
        int padding  = enable ? FULL_SCREEN_PADDING   : NORMAL_SCREEN_PADDING;

        // message
        g_liveTvMsg.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        g_liveTvMsg.setPadding(padding, 0, padding, 0);
        /* For DMG
        g_liveTvMsg.setLayoutParams(fullScreen ? g_param_full_screen : g_param_message_screen);*/

        // full screen
        g_liveTvRoot.setLayoutParams(enable ? g_param_full_screen : g_param_normal_screen);

        // frame color
        g_liveTvFrame.setBackgroundResource(enable ? R.color.trans : R.drawable.frame_focus);

        // Menu
        HomeMenuView  menuView = get().findViewById(R.id.lo_menu_rltvl_home_menu);
        menuView.setVisibility(enable ? View.GONE : View.VISIBLE);

        // recommendations list
        get().g_listMgr.set_visible_list(!enable);

        // recommendations pages
        get().g_pagerMgr.set_visible_page(!enable);

        // MiniEPG
        if (!enable)
            g_miniEPG.hide_info();

        // Mute Icon
        scale_mute(enable ? ScreenType.FULL_SCREEN : ScreenType.NORMAL_SCREEN);

        return true;
    }

    public void set_screen_normal() {
        set_menu_visible();
        set_clock_visible();
        set_screen_full(false); // set_screen_normal
    }

    public void set_screen_epg() {
        final int EPG_SCREEN_TEXT_SIZE = get().getResources().getDimensionPixelSize(R.dimen.lo_epg_sfv_live_tv_text_size);
        final int EPG_SCREEN_PADDING   = get().getResources().getDimensionPixelSize(R.dimen.lo_epg_sfv_live_tv_padding);

        g_liveTvMsg.setTextSize(EPG_SCREEN_TEXT_SIZE);
        g_liveTvMsg.setPadding(EPG_SCREEN_PADDING, 0, EPG_SCREEN_PADDING, 0);
        g_liveTvRoot.setLayoutParams(g_param_epg_screen);
        g_liveTvFrame.setBackgroundResource(R.color.trans);

        // recommendations list
        get().g_listMgr.set_visible_list(false);

        // recommendations pages
        get().g_pagerMgr.set_visible_page(false);

        set_menu_invisible();
        set_clock_invisible();
        scale_mute(ScreenType.EPG_SCREEN);
    }

    public boolean set_channel(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();

        if (null == get().g_dtv) {
            Log.w(TAG, "set_channel: null " + PrimeDtv.class.getSimpleName());
            return false;
        }

        if (KeyEvent.KEYCODE_CHANNEL_UP == keyCode) {
            BlockedChannel.set_hint_visibility(get(), true);
            set_channel_up(action);
            return true;
        }
        if (KeyEvent.KEYCODE_CHANNEL_DOWN == keyCode) {
            BlockedChannel.set_hint_visibility(get(), true);
            set_channel_down(action);
            return true;
        }
        if ((KeyEvent.KEYCODE_0 == keyCode || KeyEvent.KEYCODE_1 == keyCode || KeyEvent.KEYCODE_2 == keyCode || KeyEvent.KEYCODE_3 == keyCode ||
             KeyEvent.KEYCODE_4 == keyCode || KeyEvent.KEYCODE_5 == keyCode || KeyEvent.KEYCODE_6 == keyCode || KeyEvent.KEYCODE_7 == keyCode ||
             KeyEvent.KEYCODE_8 == keyCode || KeyEvent.KEYCODE_9 == keyCode) &&
             KeyEvent.ACTION_DOWN == action) {
            BlockedChannel.set_hint_visibility(get(), true);
            open_zapping(keyCode);
            return true;
        }
        if (KeyEvent.KEYCODE_LAST_CHANNEL == keyCode &&
            KeyEvent.ACTION_DOWN == action &&
            is_full_screen()) {
            BlockedChannel.set_hint_visibility(get(), true);
            set_channel_previous();
            return true;
        }
        return false;
    }

    public void set_channel_up(int action) {
        ProgramInfo channel;
        long channelId;

        if (null == g_chChangeMgr)
            g_chChangeMgr = ChannelChangeManager.get_instance(get());

        long nextUpId = g_chChangeMgr.get_next_ch_up_id();
        if (g_chChangeMgr.is_full_recording() && !g_chChangeMgr.is_channel_recording(nextUpId)) {
            if (KeyEvent.ACTION_DOWN == action) {
                MessageDialog dialog = new MessageDialog(get());
                dialog.set_content_message(R.string.pvr_zapping_channel_but_recording_is_full);
                dialog.set_cancel_visible(true);
                dialog.set_confirm_action(() -> g_chChangeMgr.pvr_record_stop(g_chChangeMgr.get_last_record_ch_id()));
                dialog.show_panel();
            }
            return;
        }

        if (KeyEvent.ACTION_DOWN == action) {
            if ((System.currentTimeMillis() - previousTimeMs) < THRESHOLD_FOR_CHANGE_CHANNEL)
                return;
            is_channel_changing = true;
            previousTimeMs = System.currentTimeMillis();
            set_screen_full(true); // set_channel_up
            new_channelId = g_chChangeMgr.get_mini_epg_up_id();
            channel = get_channel(new_channelId);

            if (channel != null) {
                Log.d(TAG, "set_channel_up: press to show miniEPG, [channel] " + new_channelId + " / " + channel.getDisplayName());
                stop_blocked_channel_check();
                check_blocked_channel(channel);
                setup_music_screen(channel); // set_channel_up
            }
            g_miniEPG.show_info_only(channel);
        }
        else if (KeyEvent.ACTION_UP == action) {
            channelId = new_channelId;//g_chChangeMgr.get_mini_epg_cur_id();
            channel = get_channel(channelId);

            if (channel != null) {
                Log.d(TAG, "set_channel_up: release to channel up, [channel] " + channelId + " / " + channel.getDisplayName());
                close_ca_message();
                close_mail_envelope_dialog();
                g_blocked_channel.set_check_tuner_lock_flag(false);
                g_chChangeMgr.change_channel_by_id(channelId);
                g_blocked_channel.start_blocked_channel_check();
                g_handler.postDelayed(() -> g_blocked_channel.set_check_tuner_lock_flag(true), 3000);
                close_marquee_Mgr();
                if (is_music_channel(channel))
                    g_musicTv.request_focus_on_category();
            }
            is_channel_changing = false;
        }
    }

    public void set_channel_down(int action) {
        ProgramInfo channel;
        long channelId;

        if (null == g_chChangeMgr)
            g_chChangeMgr = ChannelChangeManager.get_instance(get());

        long nextDownId = g_chChangeMgr.get_next_ch_down_id();
        if (g_chChangeMgr.is_full_recording() && !g_chChangeMgr.is_channel_recording(nextDownId)) {
            if (KeyEvent.ACTION_DOWN == action) {
                MessageDialog dialog = new MessageDialog(get());
                dialog.set_content_message(R.string.pvr_zapping_channel_but_recording_is_full);
                dialog.set_cancel_visible(true);
                dialog.set_confirm_action(() -> g_chChangeMgr.pvr_record_stop(g_chChangeMgr.get_last_record_ch_id()));
                dialog.show_panel();
            }
            return;
        }

        if (KeyEvent.ACTION_DOWN == action) {
            if ((System.currentTimeMillis() - previousTimeMs) < THRESHOLD_FOR_CHANGE_CHANNEL)
                return;
            is_channel_changing = true;
            previousTimeMs = System.currentTimeMillis();
            set_screen_full(true); // set_channel_down
            new_channelId = g_chChangeMgr.get_mini_epg_down_id();
            channel = get_channel(new_channelId);

            if (channel != null) {
                Log.d(TAG, "set_channel_down: press to show miniEPG, [channel] " + new_channelId + " / " + channel.getDisplayNameFull());
                g_blocked_channel.stop_blocked_channel_check();
                check_blocked_channel(channel);
                setup_music_screen(channel); // set_channel_down
            }

            g_miniEPG.show_info_only(channel);
        }
        else if (KeyEvent.ACTION_UP == action) {
            channelId = new_channelId;//g_chChangeMgr.get_mini_epg_cur_id();
            channel = get_channel(channelId);

            if (channel != null) {
                Log.d(TAG, "set_channel_down: release to channel down, [channel] " + channelId + " / " + channel.getDisplayName());
                close_ca_message();
                close_mail_envelope_dialog();
                g_blocked_channel.set_check_tuner_lock_flag(false);
                g_chChangeMgr.change_channel_by_id(channelId);
                g_blocked_channel.start_blocked_channel_check();
                g_handler.postDelayed(() -> g_blocked_channel.set_check_tuner_lock_flag(true), 3000);
                close_marquee_Mgr();
                if (is_music_channel(channel))
                    g_musicTv.request_focus_on_category();
            }
            is_channel_changing = false;
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void set_channel_previous() {
        if (null == g_chChangeMgr)
            g_chChangeMgr = ChannelChangeManager.get_instance(get());

        ProgramInfo currChannel = get_current_channel();
        ProgramInfo prevChannel = g_chChangeMgr.get_pre_channel();

        if (g_chChangeMgr.is_full_recording() && prevChannel != null && !g_chChangeMgr.is_channel_recording(prevChannel.getChannelId())) {
            MessageDialog dialog = new MessageDialog(get());
            dialog.set_content_message(R.string.pvr_zapping_channel_but_recording_is_full);
            dialog.set_cancel_visible(true);
            dialog.set_confirm_action(() -> g_chChangeMgr.pvr_record_stop(g_chChangeMgr.get_last_record_ch_id()));
            dialog.show_panel();
            return;
        }

        if (null == prevChannel) {
            Log.w(TAG, "set_channel_previous: null previous channel");
            g_miniEPG.show_info_only();
        }
        else if (null == currChannel) {
            Log.w(TAG, "set_channel_previous: null current channel");
            g_miniEPG.show_info_only();
        }
        else if (currChannel.getChannelId() == prevChannel.getChannelId()) {
            Log.w(TAG, "set_channel_previous: same channel, [channel LCN] " + currChannel.getLCN());
            g_miniEPG.show_info_only();
        }
        else {
            g_chChangeMgr.set_previous_channel(currChannel);
            Log.d(TAG, "set_channel_previous: change to previous channel, [channel LCN] " + prevChannel.getLCN());
            LiveTvManager.change_channel(this, prevChannel, true);
            //show_music(channel);
        }

        /*
        //check block channel
        g_blocked_channel.stop_blocked_channel_check();
        check_blocked_channel(g_chChangeMgr.get_pre_channel());
        g_miniEPG.show_info_only();
        //show_music();
        g_chChangeMgr.change_pre_channel();
        g_blocked_channel.start_blocked_channel_check();
        */
    }

    @SuppressWarnings("ConstantConditions")
    public void set_channel_default() {
        String clsName = HomeApplication.get_top_class_name();

        if (null == g_chChangeMgr)
            g_chChangeMgr = ChannelChangeManager.get_instance(get());

        if (!is_running(get(), HomeActivity.class) &&
            !is_running(get(), EpgActivity.class) &&
            !HomeActivity.class.getName().equals(clsName) &&
            !EpgActivity.class.getName().equals(clsName)) {
            Log.e(TAG, "set_channel_default: not running HomeActivity or EpgActivity");
            return;
        }

        if (HomeApplication.is_goto_hottest()) {
            Log.e(TAG, "set_channel_default: go to Hottest");
            return;
        }

        ProgramInfo channel = g_chChangeMgr.get_default_channel();
        if (null == channel) {
            Log.e(TAG, "set_channel_default: default channel is null");
            return;
        }

        if (is_channel_changing) {
            Log.e(TAG, "set_channel_default: channel is changing");
            return;
        }
        g_blocked_channel.set_check_tuner_lock_flag(false);
        boolean force = is_ca_message_visible() || !HomeActivity.HOME_AV_OK;
        Log.d(TAG, "set_channel_default: [channel] " + channel.getDisplayNum() + " " + channel.getDisplayName() + ", [force] " + force);
        LiveTvManager.change_channel(this, channel, false, force);

        get().runOnUiThread(() -> {
            if (is_music_channel(channel)) {
                g_musicTv.setVisibility(View.VISIBLE);
                if (!is_full_screen())
                    g_musicTv.hide_internal_music_ui();
                else
                    g_musicTv.show_internal_music_ui();
                if (is_block())
                    g_musicTv.setVisibility(View.GONE);
            } else
                g_musicTv.setVisibility(View.GONE);
        });

        g_handler.postDelayed(() -> g_blocked_channel.set_check_tuner_lock_flag(true), 3000);
    }

    @SuppressWarnings("ConstantConditions")
    public void set_channel_force(ProgramInfo ch) {
        if (null == g_chChangeMgr)
            g_chChangeMgr = ChannelChangeManager.get_instance(get());

        if (true) {
            if (ch != null)
                g_chChangeMgr.set_cur_ch_id(ch.getChannelId());
        }
    }

    public void set_mute(boolean show) {
        ImageView muteIcon = get().findViewById(R.id.lo_live_tv_mute);

        if (show && is_normal_screen()) scale_mute(ScreenType.NORMAL_SCREEN);
        if (show && is_full_screen())   scale_mute(ScreenType.FULL_SCREEN);
        if (show && is_epg_screen())    scale_mute(ScreenType.EPG_SCREEN);
        muteIcon.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void set_menu_visible() {
        HomeMenuView menuView = get().findViewById(R.id.lo_menu_rltvl_home_menu);
        menuView.setVisibility(View.VISIBLE);
    }

    public void set_menu_invisible() {
        HomeMenuView menuView = get().findViewById(R.id.lo_menu_rltvl_home_menu);
        menuView.setVisibility(View.INVISIBLE);
    }

    public void set_music_fullscreen(boolean fullscreen) {
        if (fullscreen) {
            if (!is_music_channel(get_current_channel())) {
                g_musicTv.hide_internal_music_ui();
                g_musicTv.setVisibility(View.GONE);
            } else if (is_music_channel(get_current_channel()))
                show_music_fullscreen();
        }
        else {
            if (g_musicTv.getVisibility() == View.VISIBLE)
                g_musicTv.hide_internal_music_ui();
            if (is_block())
                g_musicTv.setVisibility(View.GONE);
        }
    }

    public void set_clock_visible() {
        LinearLayout clockRoot = get().findViewById(R.id.lo_home_date_week_time);
        clockRoot.setVisibility(View.VISIBLE);
    }

    public void set_clock_invisible() {
        LinearLayout clockRoot = get().findViewById(R.id.lo_home_date_week_time);
        clockRoot.setVisibility(View.GONE);
    }

    public void set_subtitle(Bitmap bitmap){
        if (g_subtitler != null) {
            g_subtitler.setBitmap(bitmap);
        }
    }

    public void set_intro_time() {
        g_miniEPG.set_intro_time();
    }

    public void set_mail_is_running(boolean status) {
        g_mail_is_running = status;
    }

    public void reset_fcc() {
        if(g_chChangeMgr != null)
            g_chChangeMgr.reset_fcc();
    }

    public void channel_stop(int stop_monitor_table) {
        if(g_chChangeMgr != null) {
            g_chChangeMgr.change_channel_stop(0, stop_monitor_table);
        }
    }

    public void reset_pin_code_status() {
        if (null == g_blocked_channel)
            return;
        g_blocked_channel.reset_pin_code_status();
    }

    public boolean is_full_screen() {
        return MATCH_PARENT == g_liveTvRoot.getLayoutParams().width &&
               MATCH_PARENT == g_liveTvRoot.getLayoutParams().height;
    }

    public boolean is_normal_screen() {
        return g_liveTvRoot.getLayoutParams().width  == get().getResources().getDimensionPixelSize(R.dimen.home_live_tv_w) &&
               g_liveTvRoot.getLayoutParams().height == get().getResources().getDimensionPixelSize(R.dimen.home_live_tv_h);
    }

    public boolean is_epg_screen() {
        return g_liveTvRoot.getLayoutParams().width  == get().getResources().getDimensionPixelSize(R.dimen.lo_epg_sfv_live_tv_width) &&
               g_liveTvRoot.getLayoutParams().height == get().getResources().getDimensionPixelSize(R.dimen.lo_epg_sfv_live_tv_height);
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean press_back(int keyCode, boolean exitFullscreen) {
        if (KeyEvent.KEYCODE_BACK == keyCode &&
            is_full_screen()) {
            if (!exitFullscreen && g_timeshiftBanner.timeshift_stop(false))
                return true;
            BlockedChannel.set_hint_visibility(get(), false);
            //get().g_pagerMgr.init_pager_all();
            set_music_fullscreen(false);
            check_ticker_callback();
            return set_screen_full(false); // press_back
        }

        if (KeyEvent.KEYCODE_BACK == keyCode &&
            !is_full_screen() &&
			g_liveTvFrame != null &&
            !g_liveTvFrame.hasFocus())
            return focus_small_screen();

        if (KeyEvent.KEYCODE_BACK == keyCode)
            return true;

        return false;
    }

    public boolean is_block() {
        return g_blocked_channel != null && g_blocked_channel.isLock();
    }

    public boolean is_block_adult() {
        return g_blocked_channel != null && g_blocked_channel.isAdultLock();
    }

    public boolean is_block_channel() {
        return g_blocked_channel != null && g_blocked_channel.isChannelLock();
    }

    public boolean is_block_parental() {
        return g_blocked_channel != null && g_blocked_channel.isParentalLock();
    }

    public boolean is_block_time() {
        return g_blocked_channel != null && g_blocked_channel.isTimeLock();
    }

    public boolean is_block_screen() {
        return is_full_screen() && is_block();
    }

    public boolean is_focus_live_tv() {
        return g_liveTvRoot.hasFocus();
    }

    public boolean is_music_channel() {
        return is_music_channel(get_current_channel());
    }

    public boolean is_music_channel(ProgramInfo channelInfo) {
        if (channelInfo != null)
            return channelInfo.getType() == PROGRAM_RADIO;
        else
            return false;
    }

    public boolean is_ca_message_visible() {
        return g_liveTvMsg.getVisibility() == View.VISIBLE;
    }

    public boolean is_tuner_lock() {
        if (null == get() || null == get().g_dtv) {
            Log.e(TAG, "is_tuner_lock: something is null");
            return false;
        }

        ProgramInfo channel = get_current_channel();
        if (null == channel) {
            Log.e(TAG, "on_click_screen: channel is null");
            return false;
        }

        return get().g_dtv.get_tuner_status(channel.getTunerId());
    }

    public boolean isE200Showing() {
        return isE200Showing;
    }

    public void open_epg() {
        Log.d(TAG, "open_epg: " + EpgActivity.class.getSimpleName());
        Intent epg = new Intent(get(), EpgActivity.class);
        epg.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        get().startActivity(epg);
    }

    public void open_zapping(int keyCode) {
        String chNum = String.valueOf(keyCode - KeyEvent.KEYCODE_0);

        get().get_thread_handler().post(() -> {
            Log.d(TAG, "open_zapping: channel num = " + chNum);
            if (gZappingDialog == null)
                gZappingDialog = new ZappingDialog(this, chNum, get_channels());
            else
                gZappingDialog.set_all_data(chNum, get_channels());

            get().runOnUiThread(() -> gZappingDialog.show());
            get().runOnUiThread(() -> {
                set_screen_full(true); //enter_fullscreen();
                if (g_musicTv.is_show() && !g_musicTv.is_music_ui_show()) {
                    g_musicTv.show_internal_music_ui();
                    g_musicTv.update_current_channel();
                }
            });
        });
    }

    public void open_tv_package() {
        if (g_miniEPG.is_show())
            return;

        update_recommend_packages(false); // open_tv_package

        if (null == g_tvPackageDialog)
            g_tvPackageDialog = new TvPackageDialog(get(), g_recommendPackages); // init & open
        else
            g_tvPackageDialog.update_data(g_recommendPackages);

        //for (RecommendPackage pkg : g_recommendPackages)
        //    Log.d(TAG, "open_tv_package: [package name] " + pkg.get_package_name());

        int INTRODUCTION_TIME = get().g_dtv.gpos_info_get().getIntroductionTime();
        g_tvPackageDialog.set_intro_time(INTRODUCTION_TIME);
        g_tvPackageDialog.show();
    }

    public void open_recommend() {
        RecommendationDialog recommendationDialog;

        if (g_miniEPG.is_show())
            return;

        Log.d(TAG, "open_recommend: ");
        int INTRODUCTION_TIME = get().g_dtv.gpos_info_get().getIntroductionTime();
        recommendationDialog = new RecommendationDialog(get());
        recommendationDialog.set_intro_time(INTRODUCTION_TIME);
        recommendationDialog.show();
    }

    public void open_ranking() {
        if (!Pvcfg.RANKING)
            return;

        RankListDialog rankingDialog;

        if (g_miniEPG.is_show())
            return;

        Log.d(TAG, "open_ranking: ");
        int INTRODUCTION_TIME = get().g_dtv.gpos_info_get().getIntroductionTime();
        rankingDialog = new RankListDialog(get(), g_rankInfos);
        rankingDialog.set_intro_time(INTRODUCTION_TIME);
        rankingDialog.show();
    }

    public void open_quick_tune() {
        if (g_miniEPG.is_show())
            return;

        if (null == gDlgQuickTune)
            gDlgQuickTune = new QuickTuneDialog(this);

        int INTRODUCTION_TIME = get().g_dtv.gpos_info_get().getIntroductionTime();
        gDlgQuickTune.set_intro_time(INTRODUCTION_TIME);
        gDlgQuickTune.show();
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean open_password_dialog() {

        if (!g_blocked_channel.isLock())
            return false;

        if (g_blockedChannelDialog == null)
            g_blockedChannelDialog = new BlockedChannelDialog(get());
        g_blockedChannelDialog.register_callback(this);
        g_blockedChannelDialog.setLockTitle(g_blocked_channel);
        g_blockedChannelDialog.show();
        return true;
    }

    public void show_mini_epg() {
        Log.d(TAG, "show_mini_epg: mini EPG = " + g_miniEPG);
        if (null == g_miniEPG)
            return;
        g_miniEPG.show_info_only();
    }

    public void show_mute() {
        ImageView muteIcon = get().findViewById(R.id.lo_live_tv_mute);
        boolean   isShow   = View.VISIBLE == muteIcon.getVisibility();

        if (isShow)
            return;
        Log.d(TAG, "show_mute: ");
        set_mute(true);
    }

    public void show_ca_message(String text) {
        if (is_ca_message_visible())
            return;
        //LogUtils.d("show_ca_message: ");
        get().runOnUiThread(() -> {
            g_liveTvMsg.setText(text);
            g_liveTvMsg.setVisibility(View.VISIBLE);
        });
    }

    public void show_music_ca_message(String text) {
        get().runOnUiThread(() -> {
            if (is_music_channel()) {
                if (is_full_screen()) g_musicTv.show_error_code(true, text, null);
                else {
                    g_musicTv.setVisibility(View.GONE);
                }
            }
        });
    }

    public void show_blocked_channel_bg() {
        get().runOnUiThread(() -> {
            View blockedChannel_bg = get().findViewById(R.id.lo_home_live_tv_blocked_channel_bg);
            blockedChannel_bg.setVisibility(View.VISIBLE);
        });
    }

    public void show_message_dialog(int resId) {
        show_message_dialog(get().getString(resId));
    }

    public void show_message_dialog(String message) {
        if (null == gMsgDialog)
            gMsgDialog = new MessageDialog(get());

        if (!gMsgDialog.isShowing()) {
            gMsgDialog.set_content_message(message);
            gMsgDialog.show_dialog();
        }
    }

    public void setup_music_screen(ProgramInfo channelInfo) {

        if (is_music_channel(channelInfo)) {
            Log.d(TAG, "setup_music_screen: show music screen");
            show_music_fullscreen(channelInfo);
            return;
        }

        if (g_musicTv.is_show()) {
            Log.d(TAG, "setup_music_screen: hide music screen");
            g_musicTv.hide_internal_music_ui();
            g_musicTv.setVisibility(View.GONE);
        }
    }

    public void show_music_fullscreen(ProgramInfo channelInfo) {

        if (!is_full_screen())
            set_screen_full(true); // show_music_fullscreen

        if (!g_musicTv.is_show())
            g_musicTv.setVisibility(View.VISIBLE);

        if (!g_musicTv.is_music_ui_show())
            g_musicTv.show_internal_music_ui();

        g_musicTv.update_current_channel(channelInfo);
        g_musicTv.update_present_program(null);
        g_musicTv.update_follow_program(null);
        g_musicTv.update_present_program(get().g_dtv.get_present_event(channelInfo.getChannelId()));
        g_musicTv.update_follow_program(get().g_dtv.get_follow_event(channelInfo.getChannelId()));
        g_musicTv.check_lock();
        g_musicTv.hide_error_code_layer();
    }

    public void show_music_fullscreen() {
        if (!is_full_screen())
            set_screen_full(true); // show_music_fullscreen
        g_musicTv.update_current_channel(get_current_channel());
        g_musicTv.update_present_program(get_current_present());
        g_musicTv.update_follow_program(get_current_follow());
        g_musicTv.show_internal_music_ui();
        g_musicTv.check_lock();
        g_musicTv.setVisibility(View.VISIBLE);
    }

    public void show_mail_envelope_dialog(Mail mail) {
        //Log.d(TAG, "show_mail_envelope_dialog: g_mail_is_running = " + g_mail_is_running);
        if (g_mail_is_running) {
            //Log.d(TAG, "show_mail_envelope_dialog: mail is running");
            return;
        }

        if (is_full_screen()) {
            g_mailEnvelopeDialog = new MailEnvelopeDialog(get());
            g_mailEnvelopeDialog.show_promotion_content(mail);
            g_mailEnvelopeDialog.show();
        }
    }

    public void show_mail_dialog(Mail mail) {
        Log.d(TAG, "show_mail_dialog: ");

        if (g_mail_is_running) {
            //Log.d(TAG, "show_mail_dialog: mail is running");
            return;
        }

        if (is_full_screen()) {
            MailDialog mailDialog = new MailDialog(get());
            mailDialog.set_content(mail);
            mailDialog.show();

            MailManager mailManager = get().g_mailManager;
            if (mailManager != null) {
                mailManager.set_mail_read_status(get(), mail);
            }
        }
    }

    public void hide_mute() {
        ImageView muteIcon = get().findViewById(R.id.lo_live_tv_mute);
        boolean   isShow   = View.VISIBLE == muteIcon.getVisibility();

        if (!isShow)
            return;
        Log.d(TAG, "hide_mute: ");
        set_mute(false);
    }

    public void hide_blocked_channel_bg() {
        get().runOnUiThread(() -> {
            View blockedChannel_bg = get().findViewById(R.id.lo_home_live_tv_blocked_channel_bg);
            blockedChannel_bg.setVisibility(View.GONE);
        });
    }

    public void scale_mute(@ScreenType int screenType) {
        ImageView muteIcon = get().findViewById(R.id.lo_live_tv_mute);

        if (ScreenType.FULL_SCREEN == screenType) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) muteIcon.getLayoutParams();
            params.width = get().getResources().getDimensionPixelSize(R.dimen.home_live_tv_mute_fullscreen_width_height);
            params.height = get().getResources().getDimensionPixelSize(R.dimen.home_live_tv_mute_fullscreen_width_height);
            params.topMargin = get().getResources().getDimensionPixelSize(R.dimen.home_live_tv_mute_fullscreen_margin_y);
            muteIcon.setLayoutParams(params);
        }
        else if (ScreenType.NORMAL_SCREEN == screenType) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) muteIcon.getLayoutParams();
            params.width = get().getResources().getDimensionPixelSize(R.dimen.home_live_tv_mute_width_height);
            params.height = get().getResources().getDimensionPixelSize(R.dimen.home_live_tv_mute_width_height);
            params.topMargin = get().getResources().getDimensionPixelSize(R.dimen.home_live_tv_mute_margin_y);
            muteIcon.setLayoutParams(params);
        }
        else if (ScreenType.EPG_SCREEN == screenType) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) muteIcon.getLayoutParams();
            params.width = get().getResources().getDimensionPixelSize(R.dimen.home_live_tv_mute_epg_width_height);
            params.height = get().getResources().getDimensionPixelSize(R.dimen.home_live_tv_mute_epg_width_height);
            params.topMargin = get().getResources().getDimensionPixelSize(R.dimen.home_live_tv_mute_epg_margin_y);
            muteIcon.setLayoutParams(params);
        }
    }

    public boolean focus_small_screen() {
        get().g_pagerMgr.unblock_view();
        get().g_pagerMgr.reset_page_position();
        get().g_listMgr.scroll_to_top();
        get().get_main_handler().post(g_liveTvFrame::requestFocus);
        Log.d(TAG, "focus_small_screen: Live TV request focus");
        return true;
    }

    public void enter_fullscreen() {
        if (is_full_screen()) {
            open_password_dialog();
            return;
        }
        Log.d(TAG, "enter_fullscreen: show fullscreen");

        BlockedChannel.set_hint_visibility(get(), true);
        get().g_pagerMgr.remove_all_page();
        set_screen_full(true); // enter_fullscreen
        set_music_fullscreen(true);
        show_mini_epg();
    }

    public void exit_fullscreen() {
        press_back(KeyEvent.KEYCODE_BACK, true);
        focus_small_screen();
    }

    /** @noinspection CallToPrintStackTrace*/
    public static void change_channel(LiveTvManager liveTvManager, String serviceId, boolean showMiniEpg) {
        int service_ID = 0;

        try { service_ID = Integer.parseInt(serviceId); }
        catch (Exception e) { e.printStackTrace(); }

        change_channel(liveTvManager, service_ID, showMiniEpg);
    }

    public static void change_channel(LiveTvManager liveTvManager, int serviceId, boolean showMiniEpg) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (null == liveTvManager.get().g_dtv) {
                    liveTvManager.g_handler.postDelayed(this, 100);
                    return;
                }
                ProgramInfo channel = liveTvManager.get().g_dtv.get_program_by_service_id(serviceId);
                change_channel(liveTvManager, channel, showMiniEpg);
            }
        };
        liveTvManager.g_handler.post(runnable);
    }

    public static void change_channel(LiveTvManager liveTvManager, ProgramInfo channel, boolean showMiniEpg) {
        change_channel(liveTvManager, channel, showMiniEpg, false);
    }

    public static void change_channel(LiveTvManager liveTvManager, ProgramInfo channel, boolean showMiniEpg, boolean force) {
        ChannelChangeManager changeManager;

        if (null == liveTvManager) {
            Log.w(TAG, "change_channel: null LiveTvManager");
            return;
        }
        if (null == channel) {
            Log.w(TAG, "change_channel: null channel");
            return;
        }

        //check block channel
        liveTvManager.g_blocked_channel.stop_blocked_channel_check();
        liveTvManager.check_blocked_channel(channel);

        liveTvManager.close_ca_message();
        liveTvManager.close_marquee_Mgr();
        liveTvManager.close_mail_envelope_dialog();

        // change channel
        changeManager = ChannelChangeManager.get_instance(liveTvManager.get());
        liveTvManager.g_blocked_channel.set_check_tuner_lock_flag(false);
        changeManager.change_channel_by_id(channel.getChannelId(), force);
        changeManager.reset_mini_epg_index();
        liveTvManager.g_blocked_channel.start_blocked_channel_check();
        liveTvManager.g_handler.postDelayed(() -> liveTvManager.g_blocked_channel.set_check_tuner_lock_flag(true), 3000);

        if (null == liveTvManager.g_miniEPG) {
            Log.w(TAG, "change_channel: null miniEPG");
            return;
        }

        if (showMiniEpg) {
            liveTvManager.g_miniEPG.show_info_only();

            if (liveTvManager.is_music_channel(channel)) {
                liveTvManager.setup_music_screen(channel); // change_channel
                liveTvManager.g_musicTv.request_focus_on_category();
            }
            else {
                liveTvManager.g_musicTv.setVisibility(View.GONE);
                liveTvManager.g_musicTv.hide_internal_music_ui();
            }
        }
    }
    public void stop_blocked_channel_check(){
        if(g_blocked_channel != null)
            g_blocked_channel.stop_blocked_channel_check();
        else{
            g_blocked_channel = BlockedChannel.get_instance(get_current_channel(), get().get_thread_handler());
        }
    }

    public void check_blocked_channel(ProgramInfo channel) {
        boolean current_lock_status;

        if(channel == null) {
            LogUtils.d("channel is null !!!!!!!!");
            return;
        }

        if(g_blocked_channel != null)
            current_lock_status = g_blocked_channel.checkLock(channel);
        else {
            BlockedChannel blockedChannel = BlockedChannel.get_instance(null, null);
            if (null == blockedChannel) {
                Log.w(TAG, "check_blocked_channel: null BlockedChannel");
                return;
            }
            current_lock_status = blockedChannel.checkLock(channel);
        }

        Log.d(TAG, "check_blocked_channel: [block status] " + current_lock_status);

        if(current_lock_status)
            show_blocked_channel_bg();
        else
            hide_blocked_channel_bg();
    }

    public void check_ticker_callback() {
        Ticker ticker = get().g_marqueeMgr.get_ticker();
        if(ticker != null && ticker.getMail_type().equals(Mail.MAIL_TYPE_NORMAL)) {
            //Log.d(TAG,"get().g_marqueeMgr.marquee_isRunning() = "+get().g_marqueeMgr.marquee_isRunning());
            if(get().g_marqueeMgr.marquee_isRunning())
                get().g_marqueeMgr.on_marquee_complete();
        }
    }

    public void close_password_dialog(){
        if (g_blockedChannelDialog == null)
            return;

        get().runOnUiThread(() -> {
            if (g_blockedChannelDialog.isShowing())
                g_blockedChannelDialog.dismiss();
        });
    }

    public void close_ca_message() {
        if (!is_ca_message_visible())
            return;
        //LogUtils.d("close_ca_message: ");
        get().runOnUiThread(() -> {
            g_liveTvMsg.setVisibility(View.GONE);
        });
    }

    public void close_marquee_Mgr() {
        //Log.d(TAG, "close_marquee_Mgr: ");
        //Log.d(TAG, "close_marquee_Mgr: g_ticker_service_id list = " + g_ticker_service_id_list);

        ProgramInfo programInfo = g_chChangeMgr.get_cur_channel();
        //Log.d(TAG, "close_marquee_Mgr: name = " + programInfo.getDisplayName());
        //Log.d(TAG, "close_marquee_Mgr: serviceId = " + programInfo.getServiceId());

        if (g_ticker_service_id_list.isEmpty()) {
            //Log.d(TAG, "close_marquee_Mgr: 000");
            return;
        }
        if (g_ticker_service_id_list.contains(0)) {
            //Log.d(TAG, "close_marquee_Mgr: 111");
            return;
        }
        else if (g_ticker_service_id_list.contains(programInfo.getServiceId())) {
            //Log.d(TAG, "close_marquee_Mgr: 222");
            return;
        }
        get().runOnUiThread(() -> {
            get().g_marqueeMgr.stop_marquee();
        });
    }

    public void close_mail_envelope_dialog(Mail mail) {
        //Log.d(TAG, "close_mail_envelope_dialog: ");

        if (g_mailEnvelopeDialog == null)
            return;
        if(!g_mailEnvelopeDialog.is_the_same_mail(mail)) {
            Log.d(TAG, "close_mail_envelope_dialog => Not the same mail!!!!!");
            return;
        }
        g_mailEnvelopeDialog.dismiss();
        g_mail_is_running = false;
        g_mailEnvelopeDialog = null;
    }

    public void close_mail_envelope_dialog() {
        //Log.d(TAG, "close_mail_envelope_dialog: ");

        if (g_mailEnvelopeDialog == null)
            return;
        g_mailEnvelopeDialog.dismiss();
        g_mail_is_running = false;
        g_mailEnvelopeDialog = null;
    }

    public void set_ticker_id_and_service_id(int id, String serviceId) {
        if (g_ticker_id == id)
            return;

        //Log.d(TAG, "set_ticker_id_and_service_id: new ticker id = " + id);
        //Log.d(TAG, "set_ticker_id_and_service_id: old ticker id = " + g_ticker_id);

        g_ticker_id = id;
        String[] stringArray = serviceId.split(":");
        g_ticker_service_id_list = new ArrayList<>();
        for (String str : stringArray) {
            g_ticker_service_id_list.add(Integer.parseInt(str));
        }
        //g_ticker_service_id = serviceId;
    }

    public void register_blocked_channel_callback() {
        if(g_blocked_channel != null)
            g_blocked_channel.register_callback(this);
    }

    private void expand_to_full_screen() {
        ConstraintLayout rootLayout = get().findViewById(R.id.lo_home_live_tv_container);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) rootLayout.getLayoutParams();

        params.setMarginStart(0);
        rootLayout.setLayoutParams(params);
        g_fullScreenSet.applyTo(rootLayout);
    }

    private void resize_to_normal_screen() {
        ConstraintLayout rootLayout = get().findViewById(R.id.lo_home_live_tv_container);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) rootLayout.getLayoutParams();

        params.setMarginStart(get().getResources().getDimensionPixelSize(R.dimen.home_list_container_margin_start));
        rootLayout.setLayoutParams(params);
        g_originalSet.applyTo(rootLayout);
    }

}