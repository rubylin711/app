package com.prime.launcher;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.google.gson.Gson;
import com.prime.dtv.service.dsmcc.DsmccService;
import com.prime.launcher.ACSDatabase.ACSContentObserver;
import com.prime.launcher.ACSDatabase.ACSDataProviderHelper;
import com.prime.launcher.ACSDatabase.ACSHelper;
import com.prime.launcher.EPG.EpgActivity;
import com.prime.launcher.Home.ClosedCaptionUI;
import com.prime.launcher.Home.LiveTV.LiveTvManager;
import com.prime.launcher.Home.Marquee.MarqueeManager;
import com.prime.launcher.Home.Menu.HomeMenuView;
import com.prime.launcher.Home.Menu.Notification;
import com.prime.launcher.Home.Recommend.Installer.InstallerActivity;
import com.prime.launcher.Home.Recommend.List.ListManager;
import com.prime.launcher.Home.Recommend.List.RecommendListView;
import com.prime.launcher.Home.Recommend.Pager.PagerManager;
import com.prime.launcher.Hottest.HottestActivity;
import com.prime.launcher.Mail.Mail;
import com.prime.launcher.Mail.MailManager;
import com.prime.launcher.OTA.OtaUtils;
import com.prime.launcher.Ticker.Ticker;
import com.prime.launcher.Utils.ActivityUtils;
import com.prime.launcher.Utils.ErrorCodeUtils;
import com.prime.launcher.Utils.LiAdDayFrequencyQueryJavaUtils;
import com.prime.launcher.Utils.OnQueryDayFrequencyListener;
import com.prime.launcher.Utils.System.ForcePaHintActivity;
import com.prime.launcher.Utils.System.ForceScreenActivity;
import com.prime.launcher.Utils.System.IPAlertActivity;
import com.prime.launcher.OTA.OtaUserCheckDialogFragment;
import com.prime.launcher.Utils.System.LiAdUtils;
import com.prime.launcher.Utils.System.SystemAlertDialog;
import com.prime.launcher.Utils.Utils;
import com.prime.launcher.Receiver.NetworkChangeReceiver;
import com.prime.launcher.Receiver.PrimeAppReceiver;
import com.prime.launcher.Receiver.PrimeBroadcastReceiver;
import com.prime.launcher.Receiver.PrimeHomeReceiver;
import com.prime.launcher.Receiver.PrimeVolumeReceiver;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.ServiceDefine.AvCmdMiddle;
import com.prime.datastructure.utils.ErrorCode;
import com.prime.datastructure.sysdata.CasData;
import com.prime.datastructure.sysdata.EnTableType;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.utils.LogUtils;
import com.prime.datastructure.utils.TVMessage;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import com.inspur.adservice.IADManager;
import com.inspur.adservice.IADCallback;

/** @noinspection CommentedOutCode*/
public class HomeActivity extends BaseActivity implements PrimeDtv.LauncherCallback, PrimeAppReceiver.Callback, PrimeHomeReceiver.Callback {

    private static final String TAG = HomeActivity.class.getSimpleName();

    public static final Integer DEBUG_NORMAL  = 1;
    public static final Integer DEBUG_VERBOSE = 2;
    public static final Integer DEBUG = DEBUG_NORMAL;

    public static final String PKG_GOOGLE_PLAY          = "com.android.vending";
    public static final String PKG_GOOGLE_MOVIES        = "com.google.android.videos";
    public static final String PKG_GOOGLE_YOUTUBE       = "com.google.android.youtube.tv";
    public static final String PKG_GOOGLE_YOUTUBE_MUSIC = "com.google.android.youtube.tvmusic";
    public static final String PKG_GOOGLE_GAMES         = "com.google.android.play.games";
    public static final String PKG_NETFLIX              = "com.netflix.ninja";
    public static final String CLS_TV_SETTINGS          = "com.android.tv.settings.MainSettings";
    public static final String CLS_HOME_ACTIVITY        = "com.prime.launcher.HomeActivity";
    public static final String HOME_PACKAGE_NAME        = "com.prime.launcher";
    public static final String ACTION_UPDATE_STORAGE_SERVER = "com.prime.launcher.action.update.storage.server";
    public static final String ACTION_UPDATE_HOT_VIDEO      = "com.prime.launcher.action.update.hot.video";

    public static final int TYPE_HANDLE_ACS_DATA = 1;
    public static final int TYPE_HANDLE_DTV_CREATE = 2;
    public static final int TYPE_HANDLE_DTV_START = 3;
    public static final int TYPE_HANDLE_DTV_RESUME = 4;
    public static final int TYPE_HANDLE_DTV_PAUSE = 5;
    public static final int TYPE_HANDLE_DTV_STOP = 6;
    public static final int TYPE_HANDLE_DTV_DESTROY = 7;
    public static final int TYPE_HANDLE_DTV_NEW_INTENT = 8;

    public static final int MSG_VOLUME_CHANGED = 1000;

    public static final String EXTRA_SCREEN_TYPE = "screen_type";
    public static final String EXTRA_CHANNEL_ID = "channel_id";
    public static final int SCREEN_TYPE_LIVE_TV = 0;
    public static final int SCREEN_TYPE_HOTTEST = 1;
    public static final int SCREEN_TYPE_HOME = 2;
    public static final int SCREEN_TYPE_CHANGE_CHANNEL = 4;

    public static final boolean ENABLE_WEBVIEW = false;

    public static boolean HOME_AV_OK = false;

    public RecommendListView g_rcvPopular;
    public RecommendListView g_rcvApps;
    public RecommendListView g_rcvAppsGames;

    public PagerManager g_pagerMgr;
    public ListManager g_listMgr;
    public LiveTvManager g_liveTvMgr;
    public MarqueeManager g_marqueeMgr;
    public MailManager g_mailManager;
    public HomeMenuView g_homeMenu;
    //public BaseWebView g_webView;

    public PrimeDtv g_dtv;
    private PrimeBroadcastReceiver g_BroadcastReceiver;
    private PrimeAppReceiver    g_appReceiver;
    private PrimeHomeReceiver   g_homeReceiver;
    private NetworkChangeReceiver networkChangeReceiver;
    private Notification g_notification = null;
    private HomeBroadcastReceiver g_homeBroadcastReciever;
    private ACSHelper g_acsHelper;
    private LiAdDayFrequencyQueryJavaUtils g_liAdUtil;
    private OnQueryDayFrequencyListener g_LiAdListener;
    private boolean g_is_run_ad = false;
    private boolean openBootAD = false;
//    private acs_test_use g_acs_test_use;

    private HandlerThread g_HandlerThread = null;
    private Handler g_HandlerThreadHandler = null;
    private Handler g_MainThreadHandler = null;

    private String g_background_service_acs_command = "none";
    private static boolean g_isActivityAlive;
    private static boolean g_isActivityTop;
    private Context g_context ;
    //private Toast g_volume_toast = null;
    private boolean isFirst = true;
    private int checkAvFrameCnt = 0;
    private boolean g_assistant_skip_resume;

    @SuppressLint("VisibleForTests")
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: instance - " + System.identityHashCode(this));
        super.onCreate(savedInstanceState);
        Glide.init(this, new GlideBuilder().setLogLevel(Log.ERROR));
        HomeBackgroundService.startService(this);
        //hook_web_view(TAG);
        setContentView(R.layout.activity_home);
        init_background_handler();
        register_receivers();
        set_activity_alive(true);
        g_context = this ;
        g_notification = new Notification(this);
        g_pagerMgr = new PagerManager(this);
        g_liveTvMgr = new LiveTvManager(this);
        g_listMgr = new ListManager(this);
        g_liAdUtil = new LiAdDayFrequencyQueryJavaUtils();
        g_LiAdListener = new OnQueryDayFrequencyListener() {
            @Override
            public void onSuccess(int appDayFrequencyCount, int serverDayFrequencyLimit) {
                LogUtils.d("appDayFrequencyCount = "+appDayFrequencyCount+" serverDayFrequencyLimit = "+serverDayFrequencyLimit);
                if(serverDayFrequencyLimit<=0 || appDayFrequencyCount< serverDayFrequencyLimit){
                    runBootAd(g_context);
                }else{
                    g_liveTvMgr.init_surface_view();
                    g_liveTvMgr.set_channel_default();
                    g_is_run_ad = false;
                }
                PrimeBroadcastReceiver.set_play_ad_flag(false);
            }

            /** @noinspection CallToPrintStackTrace*/
            @Override
            public void onFail(Throwable e) {
                g_is_run_ad = false;
                PrimeBroadcastReceiver.set_play_ad_flag(false);
                g_liveTvMgr.set_channel_default();
                e.printStackTrace();
            }
        };
        init_dtv(this);
        //g_webView = findViewById(R.id.lo_home_web_view);
        g_homeMenu = findViewById(R.id.lo_menu_rltvl_home_menu);
        g_homeMenu.set_home_activity(this);
        //g_blocked_channel = BlockedChannel.get_instance(g_chChangeMgr,get_current_channel());
        get_main_handler().post(goto_live_tv(getIntent()));
    }

    ///  IADManager test -s
    private IADManager mIADManager;
    private IADCallback iadCallback = new IADCallback.Stub() {
        @Override
        public void dataInfrom(String data) throws RemoteException {
            Gson gson = new Gson();
            String epgData = mIADManager.getADJsonData("epg");
            Log.d(TAG, "parseADData epgData==>" + epgData);

            String portalData = mIADManager.getADJsonData("portal");
            Log.d(TAG, "parseADData portalData==>" + portalData);

            String channelData = mIADManager.getADJsonData("channels");
            Log.d(TAG, "parseADData channelsData==>" + channelData);
        }
    };
    ///  IADManager test -e
    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: instance - " + System.identityHashCode(this));
        super.onStart();
        start_dtv();
        BaseActivity.set_handler(get_main_handler());
        SystemAlertDialog.showSystemAlert(HomeActivity.this);
        bindADService(this); ///  IADManager test
    }
    ///  IADManager test -s
    private ServiceConnection mADServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "mADServiceConnection onServiceConnected <...");
            mIADManager = IADManager.Stub.asInterface(iBinder);
            try {
                mIADManager.registerListener(iadCallback, "epg");
                mIADManager.registerListener(iadCallback, "portal");
                mIADManager.registerListener(iadCallback, "channels");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "mADServiceConnection onServiceDisconnected <...");
            mIADManager = null;
        }
    };

    public void bindADService(Context context) {
        Log.d(TAG, "bindADService <...");
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.prime.launcher",DsmccService.CLASS_NAME));
            context.bindService(intent, mADServiceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    ///  IADManager test -e
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (handle_assistant_intent(intent)) {
            g_assistant_skip_resume = true;
        } else {
        int screenType = get_screen_type(intent);

        check_home_background_service_bundle(intent);

        Log.d(TAG, "onNewIntent: [screen type] " + screenType);
        switch (screenType) {
            case SCREEN_TYPE_LIVE_TV:
                goto_live_tv();
                break;
            case SCREEN_TYPE_HOTTEST:
                goto_hottest();
                break;
            case SCREEN_TYPE_HOME:
                goto_home();
                break;
            case SCREEN_TYPE_CHANGE_CHANNEL:
                goto_change_channel(intent);
                break;
        }
    }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: instance - " + System.identityHashCode(this));
        super.onResume();

        // skip resume if we have changed channel due to assistant
        // maybe we should skip set_channel_default() in TYPE_HANDLE_DTV_RESUME instead?
        if (g_assistant_skip_resume) {
            Log.d(TAG, "onResume: do not invoke onResume() due to assistant_skip_resume");
            g_assistant_skip_resume = false;
            return;
        }

        String clsName = HomeApplication.get_previous_class_name();
        if (HomeActivity.class.getName().equals(clsName)) {
            if (BaseActivity.gGlobalKey == KeyEvent.KEYCODE_POWER)
                Log.i(TAG, "onResume: KEYCODE_POWER");
            if (BaseActivity.gGlobalKey == KeyEvent.KEYCODE_HOME ||
                BaseActivity.gGlobalKey == KeyEvent.KEYCODE_TV ||
                BaseActivity.gGlobalKey == KeyEvent.KEYCODE_BACK) {
                Log.i(TAG, "onResume: do not invoke onResume()");
                BaseActivity.gGlobalKey = KeyEvent.KEYCODE_UNKNOWN;
                return;
            }
        }

        // reset global key for onPause()
        set_global_key(BaseActivity.gGlobalKey == KeyEvent.KEYCODE_POWER ? KeyEvent.KEYCODE_POWER : -1);

        // open activity of LiAD
        openBootAD = Pvcfg.isLiADFeature() && PrimeBroadcastReceiver.get_play_ad_flag();
        if (openBootAD) {
            LogUtils.d("Run LiAD -- queryDayFrequency");
            g_liAdUtil.queryDayFrequency(this, g_LiAdListener);
            PrimeBroadcastReceiver.set_play_ad_flag(false);
        }

        Glide.with(this).resumeRequests();
        set_thread_handler(get_thread_handler());
        set_live_tv_manager(g_liveTvMgr);
        //g_pagerMgr.init_pager_all();
        g_liveTvMgr.init_surface_view();
        resume_dtv(this);
        //register_acs_receiver();
        if(GlobalKeyReceiver.pa_lock_check(this))
            return;
        if(GlobalKeyReceiver.update_network_check(this))
            return;
        if(GlobalKeyReceiver.force_screen_check(this))
            return;

        if(g_liveTvMgr.g_chChangeMgr != null){
            g_liveTvMgr.g_chChangeMgr.set_context(this);
            if(g_dtv.av_control_get_play_status(0) == AvCmdMiddle.PESI_SVR_AV_LIVEPLAY_STATE){
                ChannelChangeManager.setFCCVisible(g_dtv.AvCmdMiddle_getPlay_index());
            }
        }
        ACSHelper.recheck_acs_command_by_provider_data(this,g_HandlerThreadHandler);
        if(g_background_service_acs_command != null && !g_background_service_acs_command.equals("none")) {
            Message msg = g_HandlerThreadHandler.obtainMessage();
            Intent intent = new Intent();
            intent.setAction(g_background_service_acs_command);
            msg.what = TYPE_HANDLE_ACS_DATA;
            msg.obj = intent;
            Log.d(TAG,"do msg.obj = " + msg.obj);
            g_HandlerThreadHandler.sendMessage(msg);
//            Log.d(TAG,"do g_background_service_acs_command = " + g_background_service_acs_command);
            g_background_service_acs_command = "none";
        }

        // handle standby redirect if check_standby_redirect = true
        if (PrimeBroadcastReceiver.get_check_standby_redirect()) {
            int redirect = get_prime_dtv().gpos_info_get().getStandbyRedirect();
            switch (redirect) {
                case GposInfo.STANDBY_REDIRECT_LIVETV:
                    goto_live_tv();
                    break;
                case GposInfo.STANDBY_REDIRECT_NONE:
                case GposInfo.STANDBY_REDIRECT_LAUNCHER:
                    goto_home();
                    break;
            }
        }

        set_activity_top(true);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: instance - " + System.identityHashCode(this));
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: instance - " + System.identityHashCode(this));
        super.onStop();
        stop_dtv();

        if (!ActivityUtils.get_top_activity(this).equals(EpgActivity.CLS_EPG_ACTIVITY)) {
            g_liveTvMgr.reset_pin_code_status();
        }

        Glide.with(this).pauseRequests();
        g_listMgr.cancel_retry_draw();

        // no one stop runnable in MusicView when home->press guide/home->other app
        // because no one hide the music ui
        // so we should stop it
        if (g_liveTvMgr != null && g_liveTvMgr.g_musicTv != null) {
            g_liveTvMgr.g_musicTv.hide_internal_music_ui();
        }
    }

    @Override
    protected void onDestroy() {
        set_activity_alive(false);
        super.onDestroy();
        Log.d(TAG, "onDestroy: unregister");
        destroy_dtv();
        unregister_receivers();
        BaseActivity.set_handler(null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Log.e(TAG, "onKeyDown: [keyCode] " + keyCode + ", [current focus] " + getCurrentFocus());
        set_global_key(keyCode);

        if (g_dtv != null)
		    g_dtv.ResetCheckAD();

        if (g_pagerMgr.on_key_down(keyCode))
            return true;

        if (g_liveTvMgr.on_key_down(event))
            return true;

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //LogUtils.d("keyCode = "+keyCode);
        if (g_liveTvMgr.on_key_up(event))
            return true;

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onMessage(TVMessage msg) {
        super.onMessage(msg);

        if(msg.getMsgFlag() == TVMessage.FLAG_PVR){
            switch(msg.getMsgType()){
                case TVMessage.TYPE_PVR_TIMESHIFT_SUCCESS:
                    Log.d(TAG,"TYPE_PVR_TIMESHIFT_SUCCESS");
                    break;
                default:
                    break;
            }
        }
        // dtv callback comes here
        //Log.e(TAG, "onMessage: type = " + msg.getMsgType() + ", flag = " + msg.getMsgFlag());
        switch (msg.getMsgType()) {
            case TVMessage.TYPE_AV_FRAME_PLAY_STATUS: {
                HOME_AV_OK = msg.getAvFrameStatus() == 0 && ActivityUtils.is_running(this, HomeActivity.class);
                // e200 is handled in LiveTvManager on_tuner_lock()
                // only close error message if video ok
                if (msg.getAvFrameChannelId() != g_liveTvMgr.g_chChangeMgr.get_cur_ch_id())
                    return;
                if (msg.getAvFrameStatus() == 0) { // video ok
                    g_liveTvMgr.close_ca_message();
                    if (g_dtv.av_control_get_play_status(0) != AvCmdMiddle.PESI_SVR_AV_LIVEPLAY_STATE)
                        checkAvFrameCnt++;
                    else
                        checkAvFrameCnt = 0;
                    if (checkAvFrameCnt > 3) {
                        Log.d(TAG, "change_channel_by_id " + g_liveTvMgr.g_chChangeMgr.get_cur_channel().getDisplayName());
                        g_liveTvMgr.g_chChangeMgr.change_channel_by_id(g_liveTvMgr.g_chChangeMgr.get_cur_ch_id());
                        checkAvFrameCnt = 0;
                    }
                }
                else {
                    checkAvFrameCnt = 0;
                    if (msg.getAvFrameStatus() == 1 &&
                        msg.getAvFrameChannelId() == g_liveTvMgr.g_chChangeMgr.get_cur_ch_id() &&
                        g_dtv.av_control_get_play_status(0) == AvCmdMiddle.PESI_SVR_AV_LIVEPLAY_STATE) {
                        if (!g_liveTvMgr.is_ca_message_visible())
                            showErrorMessage(ErrorCode.ERROR_E213, null);
                    }
                }
                break;
            }
            case TVMessage.TYPE_AV_SUBTITLE_BIMAP:{
                //LogUtils.d("TYPE_AV_SUBTITLE_BIMAP");
                Bitmap bitmap = msg.subtitle_bitmap;
                g_liveTvMgr.set_subtitle(bitmap);
            }break;
            case TVMessage.TYPE_AV_CLOSED_CAPTION_DATA:{
                //LogUtils.d("TYPE_AV_SUBTITLE_BIMAP");
                byte[] data = (byte[]) msg.getClosedCaptionData();
                ClosedCaptionUI closedCaptionUI = ClosedCaptionUI.getInstance();
                closedCaptionUI.showCea608CCData(data);
            }break;
            case TVMessage.TYPE_AV_CLOSED_CAPTION_ENABLE:{
                //LogUtils.d("TYPE_AV_SUBTITLE_BIMAP");
                int enable = msg.getClosedCaptionEnable();
                int type = msg.getClosedCaptionType();
                ClosedCaptionUI closedCaptionUI = ClosedCaptionUI.getInstance();
                closedCaptionUI.DoClosedCaptionRenderer(enable,type);
            }break;
            case TVMessage.TYPE_ADD_MUSIC_CATEGORY_TO_FAV: {
                Log.d(TAG, "onMessage: TYPE_ADD_MUSIC_CATEGORY_TO_FAV");
                add_music_category_to_fav();
            }break;
            case TVMessage.TYPE_SYSTEM_SHOW_ERROR_MESSAGE: {
                Log.d(TAG, "onMessage: TYPE_SYSTEM_SHOW_ERROR_MESSAGE");
                ChannelChangeManager.get_instance(this).change_channel_stop(0,0);
                showErrorMessage(msg.getErrCode(), msg.getMessage());
            }break;
            case TVMessage.TYPE_SYSTEM_SI_UPDATE_CHANNEL_LIST:{
                g_liveTvMgr.update_all_channels();
                g_liveTvMgr.g_chChangeMgr.update_all_channel();
                LogUtils.d("SIUpdater - call UI to update channel list");
            }break;
            case TVMessage.TYPE_SYSTEM_SI_UPDATE_SET_CHANNEL:{
                g_liveTvMgr.close_ca_message();
                g_liveTvMgr.g_musicTv.hide_error_code();
                g_liveTvMgr.g_chChangeMgr.change_channel_stop(0,0);
                g_liveTvMgr.g_chChangeMgr.change_channel_by_id(msg.getChannelId(),true);
            }break;
            case TVMessage.TYPE_SYSTEM_SI_UPDATE_SET_1ST_CHANNEL:{
                g_liveTvMgr.g_chChangeMgr.change_1st_channel();
                LogUtils.d("SIUpdater - call UI to set channel");
            }break;
            case TVMessage.TYPE_ACS_SET_NIT_ID_TO_ASC: {
                ACSHelper.set_BoxNITIdToASC(this,msg.getNit_id());
            }break;
            case TVMessage.TYPE_CA_REFRESS_CAS_SET_CHANNEL:{
                g_liveTvMgr.close_ca_message();
                g_liveTvMgr.g_musicTv.hide_error_code();
                g_liveTvMgr.g_chChangeMgr.change_channel_stop(0,0);
                g_liveTvMgr.g_chChangeMgr.change_channel_by_id(msg.getChannelId(),true);
            }break;
            case TVMessage.TYPE_BOOK_RUN_AD:{
                LogUtils.d("TYPE_BOOK_RUN_AD");
                if(Pvcfg.isLiADFeature()) {
                    LogUtils.d("Run LiAD -- queryDayFrequency");
                    g_liAdUtil.queryDayFrequency(this, g_LiAdListener);
                }
            }break;
            case TVMessage.TYPE_EPG_PF_VERSION_CHANGED:{
                ChannelChangeManager changeManager = get_channel_change_manager();
                if (g_liveTvMgr.g_blocked_channel != null &&
                    changeManager.get_cur_ch_id() == msg.getChannelId()) {
                    g_liveTvMgr.g_blocked_channel.update_parental_lock();
                    g_liveTvMgr.update_present_follow(msg.getChannelId());
                }
                break;
            }
        }

        // PVR callback comes here
        LiveTvManager LiveTvMgr = get_live_tv_manager();
        switch (msg.getMsgType()) {
            case TVMessage.TYPE_PVR_RECORDING_START_SUCCESS,
                 TVMessage.TYPE_PVR_RECORDING_START_ERROR ->    LiveTvMgr.on_record_start(msg);
            case TVMessage.TYPE_PVR_RECORDING_STOP_SUCCESS,
                 TVMessage.TYPE_PVR_RECORDING_STOP_ERROR ->     LiveTvMgr.on_record_stop(msg);
            case TVMessage.TYPE_PVR_RECORDING_COMPLETED ->      LiveTvMgr.on_record_completed(msg);
            case TVMessage.TYPE_PVR_TIMESHIFT_SUCCESS,
                 TVMessage.TYPE_PVR_TIMESHIFT_PLAY_ERROR,
                 TVMessage.TYPE_PVR_TIMESHIFT_ERROR ->          LiveTvMgr.on_timeshift_start(msg);
            case TVMessage.TYPE_PVR_TIMESHIFT_STOP_SUCCESS,
                 TVMessage.TYPE_PVR_TIMESHIFT_STOP_ERROR ->     LiveTvMgr.on_timeshift_stop(msg);
            case TVMessage.TYPE_PVR_TIMESHIFT_PAUSE_SUCCESS,
                 TVMessage.TYPE_PVR_TIMESHIFT_PAUSE_ERROR ->    LiveTvMgr.on_timeshift_pause(msg);
            case TVMessage.TYPE_PVR_TIMESHIFT_RESUME_SUCCESS,
                 TVMessage.TYPE_PVR_TIMESHIFT_RESUME_ERROR ->   LiveTvMgr.on_timeshift_resume(msg);
        }
    }

    @Override
    public void onBroadcastMessage(Context context, Intent intent) {
        super.onBroadcastMessage(context, intent);
        //Log.d(TAG, "onBroadcastMessage: HOME");

        String action = intent.getAction();
        if (action == null) {
            Log.e(TAG, "onBroadcastMessage: action == null");
            return;
        }

        switch (action) {
            case PrimeVolumeReceiver.ACTION_VOLUME_CHANGED:
            case PrimeVolumeReceiver.ACTION_STREAM_MUTE_CHANGED:
                g_liveTvMgr.on_volume_changed();
                break;
            case PrimeVolumeReceiver.ACTION_KEYCODE_VOLUME_DOWN:
            case PrimeVolumeReceiver.ACTION_KEYCODE_VOLUME_MUTE:
                //show_volume_down_toast();
                break;
        }
    }

    @Override
    public void on_app_install(String pkgName) {
        Log.i(TAG, "on_app_install: update APPs & GAMEs, [pkgName] " + pkgName);
        g_listMgr.add_local_app(pkgName);
        InstallerActivity.send_message(InstallerActivity.MSG_APP_INSTALLED);
    }

    @Override
    public void on_app_uninstall(String pkgName) {
        if (g_listMgr.is_app_existed(pkgName)) {
            Log.i(TAG, "on_app_uninstall: update APPs & GAMEs, [pkgName] " + pkgName);
            g_listMgr.remove_local_app(pkgName);
        }
    }

    @Override
    public void on_press_home() {
        set_global_key(KeyEvent.KEYCODE_HOME);
        Log.i(TAG, "on_home_pressed: [Home's AV OK] " + HOME_AV_OK);

        HomeMenuView MenuView = findViewById(R.id.lo_menu_rltvl_home_menu);
        if (MenuView.hasFocus()) {
            Log.d(TAG, "on_home_pressed: close menu");
            MenuView.close_menu();
            if (g_liveTvMgr != null)
            	g_liveTvMgr.focus_small_screen();
        }

        Log.d(TAG, "on_home_pressed: go to home");
        g_liveTvMgr.exit_fullscreen();
    }

    public void init_background_handler() {
        Log.d(TAG, "init_background_handler: ");
        g_HandlerThread = new HandlerThread("HomeActivity background handler thread");
        //Log.d(TAG,"HomeActivity g_HandlerThread.start()");
        g_HandlerThread.start();
        g_HandlerThreadHandler = new Handler(g_HandlerThread.getLooper()) {
            @SuppressLint("SuspiciousIndentation")
            @Override
            public void handleMessage(@NonNull Message msg) {
                //
                switch (msg.what){
                    case TYPE_HANDLE_ACS_DATA:
                    {
                        ACSHelper acsHelper = ACSHelper.getInstance(get_main_handler(), HomeActivity.this);
                        Intent intent = (Intent) msg.obj;
                        String action = intent.getAction();
                        Log.d(TAG,"handlerThread action = "+action);
                        acsHelper.check_asc_command(getApplicationContext(),intent,HomeBackgroundService.ACTIVITY_CALL);
                    }break;
                    case TYPE_HANDLE_DTV_CREATE:{
                        LogUtils.d("TYPE_HANDLE_DTV_CREATE start");
                        g_dtv = HomeApplication.get_prime_dtv();
                        //g_dtv.register_callbacks();
                        g_liveTvMgr.on_create();
                        g_pagerMgr.on_create();
                        g_marqueeMgr = new MarqueeManager((HomeActivity)msg.obj);
                        g_mailManager = MailManager.GetInstance((HomeActivity)msg.obj);
                        g_mailManager.build_working_mail_from_db((HomeActivity)msg.obj);
                        networkChangeReceiver = new NetworkChangeReceiver(getApplicationContext(), (HomeActivity)msg.obj);
                        g_liveTvMgr.register_blocked_channel_callback();
                        receive_epg_data();
                        detect_usb_storage(); // ruby 20250321 check usb in boot
                        //g_liveTvMgr.g_blocked_channel.set_cur_gpos_rate(g_dtv.gpos_info_get().getParentalRate());
                        check_home_background_service_bundle(getIntent());
                        update_cas_config();
                        g_acsHelper = ACSHelper.getInstance(get_main_handler(), (HomeActivity)msg.obj);

                        // set all timers by bookinfo
                        g_dtv.schedule_all_timers();
                        g_dtv.save_table(EnTableType.TIMER);

                        LogUtils.d("TYPE_HANDLE_DTV_CREATE End");
                    }break;
                    case TYPE_HANDLE_DTV_START:{
                        networkChangeReceiver.registerNetworkCallback();
                        g_pagerMgr.on_start();
                    }break;
                    case TYPE_HANDLE_DTV_RESUME:{
                        g_dtv.build_epg_event_map(getApplicationContext());
                        g_dtv.build_network_time();
                        g_liveTvMgr.on_resume();
                        if (openBootAD) {
                            openBootAD = false;
                            Log.d(TAG, "onResume: break by LiADFeature");
                            break;
                        }
                        if (isFirst) {
                            isFirst = false;
                            Log.d(TAG, "onResume: break by isFirst");
                            Context context = HomeActivity.this;
                            boolean is_running = ActivityUtils.is_running(context, HomeActivity.class) ||
                                    HomeActivity.class.getName().equals(HomeApplication.get_top_class_name());
                            if (is_running) {
                                postDelayed(() -> {
                                    if (!g_is_run_ad && ActivityUtils.is_running(context, HomeActivity.class))
                                        g_liveTvMgr.set_channel_default();
                                }, 5000);
                            }
                        }
                        else {
                            Log.d(TAG, "onResume: break by else");
                            g_liveTvMgr.set_channel_default();
                        }
                    }break;
                    case TYPE_HANDLE_DTV_PAUSE:{
                        //g_dtv.av_control_play_stop_all();
                        //g_liveTvMgr.close_password_dialog();
                        //g_liveTvMgr.reset_fcc();
                        g_pagerMgr.on_pause();
                        g_dtv.stop_schedule_eit();
                        HomeActivity.set_activity_top(false);
                        g_liveTvMgr.close_password_dialog();

                        if (Pvcfg.isFccEnable()) {
                            g_liveTvMgr.reset_fcc(); // this will also stop channel
                            g_dtv.av_control_play_stop_all(); // stop all channel and close PESI_VIDEO_COUNT_CHECK
                        } else {
                            g_liveTvMgr.channel_stop(1);
                        }

                        if (g_liveTvMgr.g_blocked_channel != null) {
                            g_liveTvMgr.g_blocked_channel.stop_blocked_channel_check();
                        }

                        HomeActivity.HOME_AV_OK = false;
                        //Log.e(TAG, "onPause: [Home's AV OK] " + HOME_AV_OK);
                    }break;
                    case TYPE_HANDLE_DTV_STOP:{
                        g_pagerMgr.on_stop();
                        networkChangeReceiver.unregisterNetworkCallback();
                        if(!ActivityUtils.is_launcher_running_top(g_context) && g_marqueeMgr.marquee_isRunning()) {
                            Log.d(TAG,"run other app , stop marquee");
                            g_marqueeMgr.on_marquee_complete();
                        }
                        g_is_run_ad = false;
                    }break;
                    case TYPE_HANDLE_DTV_DESTROY:{
                        g_liveTvMgr.on_destroy();
                        g_pagerMgr.on_destroy();
                        //g_dtv.unregister_callbacks();
                        g_dtv.pvr_deinit();
                        g_dtv.stop_schedule_eit();
                        g_dtv.StopDVBStubtitle();
                        g_dtv.stopMonitorTable(-1, -1);
                        runOnUiThread(() -> {
                            // fix java.lang.IllegalStateException: destroyLoader must be called on the main thread
                            g_notification.destroyLoader();
                        });
                    }break;
                    case TYPE_HANDLE_DTV_NEW_INTENT:{

                    }break;
                }
            }
        };

        //g_acsHelper = ACSHelper.getInstance(getMainHandler(),this);
        //Log.d(TAG,"g_MainThreadHandler = " + getMainHandler());
        //g_acs_test_use = new acs_test_use(this);
    }

    private void init_dtv(PrimeDtv.LauncherCallback callback){
        Message msg = g_HandlerThreadHandler.obtainMessage();
        msg.what = TYPE_HANDLE_DTV_CREATE;
        msg.obj = callback;
        g_HandlerThreadHandler.sendMessage(msg);
    }

    private void start_dtv(){
        Message msg = g_HandlerThreadHandler.obtainMessage();
        msg.what = TYPE_HANDLE_DTV_START;
        //msg.obj = callback;
        g_HandlerThreadHandler.sendMessage(msg);
    }

    private void resume_dtv(Context context){
        Message msg = g_HandlerThreadHandler.obtainMessage();
        msg.what = TYPE_HANDLE_DTV_RESUME;
        msg.obj = context;
        g_HandlerThreadHandler.sendMessage(msg);
    }

    private void pause_dtv(){
        Message msg = g_HandlerThreadHandler.obtainMessage();
        msg.what = TYPE_HANDLE_DTV_PAUSE;
        //msg.obj = callback;
        g_HandlerThreadHandler.sendMessage(msg);
    }

    private void stop_dtv(){
        Message msg = g_HandlerThreadHandler.obtainMessage();
        msg.what = TYPE_HANDLE_DTV_STOP;
        //msg.obj = callback;
        g_HandlerThreadHandler.sendMessage(msg);
    }

    private void destroy_dtv(){
        Message msg = g_HandlerThreadHandler.obtainMessage();
        msg.what = TYPE_HANDLE_DTV_DESTROY;
        //msg.obj = callback;
        g_HandlerThreadHandler.sendMessage(msg);
    }

    private void new_intent_dtv(){
        Message msg = g_HandlerThreadHandler.obtainMessage();
        msg.what = TYPE_HANDLE_DTV_NEW_INTENT;
        //msg.obj = callback;
        g_HandlerThreadHandler.sendMessage(msg);
    }

    public Handler get_main_handler() {
        if (null == g_MainThreadHandler) {
            g_MainThreadHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    //
                    switch(msg.what) {
                        case ACSHelper.MSG_NEW_MAIL: {
                            Log.d(TAG,"MSG_NEW_MAIL !!");
                            int flag = msg.arg2;
                            if (flag == 1)
                                g_liveTvMgr.show_mail_envelope_dialog((Mail)msg.obj);
                            else if (flag == 0)
                                g_liveTvMgr.show_mail_dialog((Mail)msg.obj);
                        }break;
                        case ACSHelper.MSG_NEW_TICKER: {
                            if(msg.arg1 == MarqueeManager.MARQUEE_START) {
//                            Log.d(TAG,"MSG_NEW_MAIL MARQUEE_START");
                                Ticker ticker = (Ticker) msg.obj;
//                            Log.d(TAG, "handle ticker to ui show");
                                if(ActivityUtils.is_launcher_running_top(g_context)) {
//                                Log.d(TAG,"IsLauncherRunningTop !!");
                                    if ((ticker.getMail_type().equals(Mail.MAIL_TYPE_NORMAL) && g_liveTvMgr.is_full_screen()) || ticker.getMail_type().equals(Mail.MAIL_TYPE_EMERGENCY)) {
                                        //Log.d(TAG,"g_marqueeMgr.set_ticker(ticker) !!");
                                        //Log.d(TAG, "handleMessage: service id = " + (long)msg.arg2);
                                        g_marqueeMgr.set_ticker(ticker, msg.arg2);
                                        g_liveTvMgr.set_ticker_id_and_service_id(ticker.getId(), ticker.getService_id());
                                    }
                                }
                            }
                            if(msg.arg1 == MarqueeManager.MARQUEE_STOP) {
                                Log.d(TAG,"MSG_NEW_MAIL MARQUEE_STOP");
                                g_marqueeMgr.stop_marquee();
                            }
                        }break;
                        case ACSHelper.MSG_FORCE_TUNE: {
                            int service_id = msg.arg1;
                            int ts_id = msg.arg2;
                            Log.d(TAG,"MSG_FORCE_TUNE service id["+service_id+"] ts id["+ts_id+"]");
                            force_tune_from_acs(service_id, ts_id);
                        }break;
                        case ACSHelper.MSG_UPDATE_NETWORK_CHECK: {
                            Log.d(TAG,"MSG_UPDATE_NETWORK_CHECK !!");
//                        ACSHelper.showIpIllegalError(this);
                            boolean is_lock = IPAlertActivity.showIpIllegalError(HomeActivity.this);
                            if(is_lock) {
                                ChannelChangeManager.get_instance(HomeActivity.this).change_channel_stop(0,0);
                            }
                        }break;
                        case ACSHelper.MSG_PA_LOCK: {
                            Log.d(TAG,"MSG_PA_LOCK !!");
                            boolean is_lock = ForcePaHintActivity.showPaDayError(HomeActivity.this,false);
                            if(is_lock) {
                                ChannelChangeManager.get_instance(HomeActivity.this).change_channel_stop(0,0);
                            }
                        }break;
                        case ACSHelper.MSG_UPDATE_MUSIC_UI: {
                            Log.d(TAG,"MSG_UPDATE_MUSIC_UI !!");
                            g_dtv.setACSMusicList(getApplicationContext());
                            g_liveTvMgr.g_musicTv.get_music_category_list();
//                            clear_music_category_to_fav();
                            add_music_category_to_fav();
                        }break;
                        case ACSHelper.MSG_UPDATE_MUSIC_AD_UI: {
                            Log.d(TAG,"MSG_UPDATE_MUSIC_AD_UI !!");
                            update_music_ad();
                        }break;
                        case ACSHelper.MSG_NETWORK_QUALITY_DEFINED: {
                            Log.d(TAG,"MSG_NETWORK_QUALITY_DEFINED !!");
                            Intent intent = new Intent(ACSHelper.INTENT_NETWORK_QUALITY_DEFINED_UPDATE);
                            sendBroadcast(intent);
                        }break;
                        case ACSHelper.MSG_STORAGE_SERVER: {
                            Log.d(TAG,"MSG_STORAGE_SERVER !!");
                            Intent intent_update_storage_server = new Intent(HomeActivity.ACTION_UPDATE_STORAGE_SERVER);
                            LocalBroadcastManager.getInstance(HomeActivity.this).sendBroadcast(intent_update_storage_server);
                        }break;
                        case ACSHelper.MSG_AD_LIST: {
                            Log.d(TAG,"MSG_AD_LIST !!");
                            g_pagerMgr.update_ad_list(true);
                        }break;
                        case ACSHelper.MSG_RECOMMENDS: {
                            Log.d(TAG,"MSG_RECOMMENDS !!");
                            g_listMgr.update_popular(true);
                        }break;
                        case ACSHelper.MSG_LAUNCHER_PROFILE: {
                            Log.d(TAG,"MSG_LAUNCHER_PROFILE !!");
                            g_listMgr.update_launcher_profile(true);
                        }break;
                        case ACSHelper.MSG_RANK_LIST: {
                            Log.d(TAG,"MSG_RANK_LIST !!");
                            g_liveTvMgr.update_rank_list(true);
                        }break;
                        case ACSHelper.MSG_HOT_VIDEO: {
                            Log.d(TAG,"MSG_HOT_VIDEO !!");
                            Intent intent_update_hot_video = new Intent(HomeActivity.ACTION_UPDATE_HOT_VIDEO);
                            LocalBroadcastManager.getInstance(HomeActivity.this).sendBroadcast(intent_update_hot_video);
                        }break;
                        case ACSHelper.MSG_RECOMMEND_PACKAGES: {
                            Log.d(TAG,"MSG_RECOMMEND_PACKAGES !!");
                            g_liveTvMgr.update_recommend_packages(true);
                        }break;
                        case ACSHelper.MSG_AD_PROFILE: {
                            Log.d(TAG,"MSG_AD_PROFILE !!");
                        }break;
                        case ACSHelper.MSG_OTA_UPDATE: {
                            Log.d(TAG,"MSG_OTA_UPDATE !!");
//                        OtaUserCheckDialog.showOtaUserCheckDialog(g_context);
                            //if(HomeApplication.isIsSaveInstanceState() == false)
                            if(ActivityUtils.is_running(getApplicationContext(),HomeActivity.class)) {
                                OtaUtils.start_Ota_Reminder(getApplicationContext(), null);
                                OtaUserCheckDialogFragment.showOtaUserCheckDialog(getSupportFragmentManager());
                            }else{
                                Message m = new Message();
                                m.what = msg.what;
                                g_MainThreadHandler.sendMessageDelayed(m, 5000);
                            }
//                        HomeActivity.this.sendBroadcast(intent);
                            // LocalBroadcastManager.getInstance(HomeActivity.this).sendBroadcast(intent);
                        }break;
                        case ACSHelper.MSG_FORCE_SCREEN: {
                            Log.d(TAG,"MSG_FORCE_SCREEN !!");
                            boolean is_force_screen = ForceScreenActivity.showForceScreen(HomeActivity.this,false);
//                        if(is_force_screen) {
//                            ChannelChangeManager.get_instance(HomeActivity.this).change_channel_stop();
//                        }
                        }break;
                        case ACSHelper.MSG_SYSTEM_ALERT:
                        {
                            Log.d(TAG,"MSG_SYSTEM_ALERT !!");
                            SystemAlertDialog.showSystemAlert(HomeActivity.this);
                        }break;
                        case HomeActivity.MSG_VOLUME_CHANGED:
                            g_liveTvMgr.on_volume_changed();
                            break;
                        case ACSHelper.MSG_UPDATE_HOME_MENU:
                            g_homeMenu.update_menu();
                            break;
                        case ACSHelper.MSG_CLOSE_MAIL:
                            g_liveTvMgr.close_mail_envelope_dialog((Mail)msg.obj);
                            break;
                        default: {
                            Log.e(TAG, "unknown ACS data handle MSG[" + msg.what + "] , please check !!");
                        }break;
                    }
                }
            };

        }
        return g_MainThreadHandler;
    }

    public Handler get_thread_handler() {
        return g_HandlerThreadHandler;
    }

    public Notification get_notification() {
        if (g_notification == null) {
            g_notification = new Notification(this);
        }
        return g_notification;
    }

    public int get_screen_type(Intent intent) {
        int screenType = intent.getIntExtra(EXTRA_SCREEN_TYPE, -1);

        if (screenType == -1) {
            if (is_standby_redirect_live_tv(intent))
                screenType = SCREEN_TYPE_LIVE_TV;
            else if (is_standby_redirect_home(intent))
                screenType = SCREEN_TYPE_HOME;
        }

        return screenType;
    }

    public LiveTvManager get_live_tv_manager() {
        return g_liveTvMgr;
    }

    public ChannelChangeManager get_channel_change_manager() {
        return g_liveTvMgr.g_chChangeMgr;
    }

    public static int get_global_key() {
        int globalKey = SystemProperties.getInt(SYSTEM_PROPERTY_GLOBAL_KEY, KeyEvent.KEYCODE_UNKNOWN);

        //Log.i(TAG, "get_global_key: Global Key = " + globalKey + ", Activity Count = " + HomeApplication.get_activity_count());
        if (globalKey == KeyEvent.KEYCODE_UNKNOWN)
            if (HomeApplication.get_activity_count() == 0)
                globalKey = KeyEvent.KEYCODE_HOME;

        set_global_key(KeyEvent.KEYCODE_UNKNOWN);

        return globalKey;
    }

    public PagerManager get_pager_manager() {
        return g_pagerMgr;
    }

    public static void set_activity_alive(boolean isActivityAlive) {
        HomeActivity.g_isActivityAlive = isActivityAlive;
    }

    public static void set_activity_top(boolean isActivityTop) {
        HomeActivity.g_isActivityTop = isActivityTop;
    }

    public static boolean is_activity_alive() {
        return g_isActivityAlive;
    }

    public static boolean is_activity_top() {
        return g_isActivityTop;
    }

    public boolean is_standby_redirect_live_tv(Intent intent) {
        int standby_redirect = intent.getIntExtra(GposInfo.GPOS_STANDBY_REDIRECT,0);
        return standby_redirect == GposInfo.STANDBY_REDIRECT_LIVETV &&
               g_liveTvMgr != null &&
               g_liveTvMgr.get_default_channel() != null;
    }

    public boolean is_standby_redirect_home(Intent intent) {
        int standby_redirect = intent.getIntExtra(GposInfo.GPOS_STANDBY_REDIRECT,0);
        return standby_redirect == GposInfo.STANDBY_REDIRECT_LAUNCHER
                || standby_redirect == GposInfo.STANDBY_REDIRECT_NONE;
    }

    public void receive_epg_data() {
//        List<ProgramInfo> channels;
//        TunerInterface tunerInterface;
//        Tuner tuner;
//        int ret;

//        channels = g_liveTvMgr.get_channels();
//        Log.e(TAG, "receive_epg_data: [channels] " + channels.size());
//
//        ret = g_dtv.send_epg_data_id(channels);
//        Log.e(TAG, "receive_epg_data: [ret] " + ret);
//
//        tunerInterface = TunerInterface.getInstance(this);
//        Log.e(TAG, "receive_epg_data: [tunerInterface] " + tunerInterface);
//
//        tuner = tunerInterface.getTuner(LiveTvManager.DEFAULT_TUNER_ID);
//        Log.e(TAG, "receive_epg_data: [tuner] " + tuner);
//
//        Eit eit = new Eit(tuner, Table.EIT_PRESENT_FOLLOWING_TABLE_ID, null);
//        Log.e(TAG, "receive_epg_data: [eit] " + eit);

        g_dtv.setup_epg_channel(); // send all channels to pesi c service
        //g_dtv.start_schedule_eit(); // start schedule EIT filter and send data to pesi c service
        g_dtv.start_epg(0);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void register_acs_receiver() {
        if(g_homeBroadcastReciever == null) {
            g_homeBroadcastReciever = new HomeBroadcastReceiver(g_HandlerThreadHandler);
            IntentFilter filter = HomeBroadcastReceiver.getHomeBroadcastFilter();
            registerReceiver(g_homeBroadcastReciever, filter, RECEIVER_EXPORTED);
        }
    }

    public void register_acs_database() {
        ContentResolver resolver = this.getContentResolver();
        resolver.registerContentObserver(
                ACSDataProviderHelper.ACS_PROVIDER_CONTENT_URI,
                true,
                new ACSContentObserver(new Handler(Looper.getMainLooper()), this) {
            @Override
            public void onChange(boolean selfChange) {
                Log.e(TAG, "onChange: ACS database change !!");
                /*
                ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
                String acs_data = acsDataProviderHelper.get_acs_provider_data(HomeActivity.this,"pa_lock") ;
                Log.d(TAG,"onChange pa_lock = "+acs_data);
                */
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void register_normal_receiver() {
        g_BroadcastReceiver    = new PrimeBroadcastReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(g_BroadcastReceiver, filter, RECEIVER_EXPORTED);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void register_app_receiver() {
        g_appReceiver = new PrimeAppReceiver();
        g_appReceiver.register_callback(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addDataScheme("package");
        registerReceiver(g_appReceiver, filter, RECEIVER_EXPORTED);
    }


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void register_home_receiver() {
        g_homeReceiver = new PrimeHomeReceiver();
        g_homeReceiver.register_callback(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(g_homeReceiver, filter, RECEIVER_EXPORTED);
    }

    public void register_receivers() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "register_receiver: ");
            register_normal_receiver();
            register_app_receiver();
            register_acs_receiver();
            register_home_receiver();
        }
    }

    public void unregister_receivers() {
        unregisterReceiver(g_BroadcastReceiver);
        unregisterReceiver(g_appReceiver);
        unregisterReceiver(g_homeBroadcastReciever);
        unregisterReceiver(g_homeReceiver);
//        g_acs_test_use.acs_test_unregister_brocastReceiver(this);
        if(g_HandlerThread != null) {
            Log.d(TAG,"HomeActivity g_HandlerThread.quit()");
            g_HandlerThread.quit();
        }
    }

    @SuppressLint({"SoonBlockedPrivateApi", "DiscouragedPrivateApi"})
    public static void hook_web_view(String TAG) {
        int sdkInt = Build.VERSION.SDK_INT;
        try {
            Class<?> factoryClass = Class.forName("android.webkit.WebViewFactory");
            String ProviderInstance = "sProviderInstance";
            Field field = factoryClass.getDeclaredField(ProviderInstance);
            field.setAccessible(true);
            Object sProviderInstance = field.get(null);
            if (sProviderInstance != null) {
                Log.i(TAG, "hook_web_view: sProviderInstance isn't null");
                return;
            }

            Method getProviderClassMethod;
            if (sdkInt > 22) {
                String getProviderClass = "getProviderClass";
                getProviderClassMethod = factoryClass.getDeclaredMethod(getProviderClass);
            }
            else if (sdkInt == 22) {
                String getFactoryClass = "getFactoryClass";
                getProviderClassMethod = factoryClass.getDeclaredMethod(getFactoryClass);
            }
            else {
                Log.i(TAG, "hook_web_view: Don't need to Hook WebView");
                return;
            }
            getProviderClassMethod.setAccessible(true);
            Class<?> factoryProviderClass = (Class<?>) getProviderClassMethod.invoke(factoryClass);
            Class<?> delegateClass = Class.forName("android.webkit.WebViewDelegate");
            Constructor<?> delegateConstructor = delegateClass.getDeclaredConstructor();
            delegateConstructor.setAccessible(true);

            if(sdkInt < 26) { // below Android O
                if (factoryProviderClass == null)
                    return;

                Constructor<?> providerConstructor = factoryProviderClass.getConstructor(delegateClass);
                providerConstructor.setAccessible(true);
                //noinspection JavaReflectionInvocation
                sProviderInstance = providerConstructor.newInstance(delegateConstructor.newInstance());
            } else {
                String CHROMIUM_WEBVIEW_FACTORY_METHOD = "CHROMIUM_WEBVIEW_FACTORY_METHOD";
                Field chromiumMethodName = factoryClass.getDeclaredField(CHROMIUM_WEBVIEW_FACTORY_METHOD);
                chromiumMethodName.setAccessible(true);
                String chromiumMethodNameStr = (String)chromiumMethodName.get(null);
                if (chromiumMethodNameStr == null) {
                    chromiumMethodNameStr = "create";
                }
                if (factoryProviderClass == null)
                    return;
                Method staticFactory = factoryProviderClass.getMethod(chromiumMethodNameStr, delegateClass);
                //noinspection JavaReflectionInvocation
                sProviderInstance = staticFactory.invoke(null, delegateConstructor.newInstance());
            }

            if (sProviderInstance != null){
                field.set("sProviderInstance", sProviderInstance);
                Log.i(TAG, "hook_web_view: Hook success!");
            }
            else
                Log.i(TAG, "hook_web_view: Hook failed!");
        }
        catch (Throwable e) {
            Log.w(TAG, "hook_web_view: " + e);
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public void goto_live_tv() {
        if (null == g_liveTvMgr || null == g_pagerMgr)
            return;

        Log.d(TAG, "goto_live_tv: is fullscreen = " + g_liveTvMgr.is_full_screen());
        g_liveTvMgr.show_mini_epg();
        g_liveTvMgr.g_musicTv.update_current_channel();

        if (!g_liveTvMgr.is_full_screen()) {
            Log.d(TAG, "goto_live_tv: enter fullscreen");
            g_pagerMgr.unblock_view();
            g_homeMenu.close_menu();
            g_liveTvMgr.enter_fullscreen();
        }
    }

    private Runnable goto_live_tv(Intent intent) {
        return () -> {
            //LogUtils.d("");
            if (null == g_liveTvMgr || // wait for live tv manager init
                null == g_liveTvMgr.g_chChangeMgr) {
                get_main_handler().postDelayed(goto_live_tv(intent), 1000);
                return;
            }

            if (is_standby_redirect_live_tv(intent)) // goto live tv
                goto_live_tv();
            else
                Log.i(TAG, "goto_live_tv: redirect failed");
        };
    }

    private void goto_change_channel(Intent intent) {
        // get channel ID
        long channelId = intent.getLongExtra(EXTRA_CHANNEL_ID, -1);
        if (channelId == -1) {
            Log.e(TAG, "goto_change_channel: channelId == -1");
            return;
        }

        // get program info
        ProgramInfo programInfo = g_dtv.get_program_by_channel_id(channelId);
        if (programInfo == null) {
            Log.e(TAG, "goto_change_channel: programInfo == null");
            return;
        }

        // goto Live TV
        Log.d(TAG, "goto_change_channel: [channel] " + programInfo.getDisplayNum(com.prime.datastructure.utils.Utils.MAX_LENGTH_OF_CHANNEL_NUM) + " " + programInfo.getDisplayName());
        get_main_handler().postDelayed(()->{
            LiveTvManager.change_channel(g_liveTvMgr, programInfo, false);
            goto_live_tv();
        }, 1000);
    }

    public void goto_hottest() {
        Log.d(TAG, "goto_hottest: ");
        String clsName = HottestActivity.class.getName();
        ActivityUtils.start_activity(this, clsName, clsName);
        HomeApplication.set_goto_hottest(true);
    }

    private void goto_home()
    {
        if ( ActivityUtils.is_launcher_running_top(g_context) )
        {
            g_homeMenu.close_menu();
            if ( g_liveTvMgr.is_full_screen() )
                g_liveTvMgr.exit_fullscreen();
        }
    }

    public void add_music_category_to_fav() {
        g_liveTvMgr.g_musicTv.category_update_to_fav(this);
//        g_liveTvMgr.g_musicTv.category_add_to_fav();
//        g_dtv.save_table(EnTableType.GROUP);
        g_liveTvMgr.g_musicTv.update_music_category_ui();
    }

    public void clear_music_category_to_fav() {
        g_liveTvMgr.g_musicTv.clear_category_to_fav();
    }

    public void update_music_ad() {
        g_liveTvMgr.g_musicTv.update_music_ad();
    }

    public ProgramInfo signal_detect() {
        ChannelChangeManager changeManager = ChannelChangeManager.get_instance(this);
        return changeManager.signal_detect();
    }

    private void check_and_do_force_tune(Bundle bundle) {
        int do_force_tune = bundle.getInt(ACSHelper.DO_FORCE_TUNE,0);
//        Log.d(TAG,"check_and_do_force_tune do_force_tune = "+do_force_tune);
        if(do_force_tune == 1) {
            ProgramInfo programInfo = get_force_tune_program_info(bundle);

            if(programInfo != null) {
                Log.d(TAG,"check_and_do_force_tune programInfo service_id ["+programInfo.getServiceId()+"]"+" ts_id["+programInfo.getTransportStreamId()+"]");
                Log.d(TAG,"check_and_do_force_tune set program ["+programInfo.getLCN()+"]"+"["+programInfo.getDisplayName()+"]"+"["+programInfo.getChannelId()+"]");

                g_liveTvMgr.close_ca_message();
                g_liveTvMgr.set_channel_force(programInfo);
                g_liveTvMgr.enter_fullscreen();
            }
        }
    }

    private ProgramInfo get_force_tune_program_info(Bundle bundle) {
        ProgramInfo programInfo = null;
        int service_id = bundle.getInt(ProgramInfo.SERVICE_ID,0);
        int ts_id = bundle.getInt(ProgramInfo.TRANSPORT_STREAM_ID,0);
        //            Log.d(TAG,"check_and_do_force_tune service_id ["+service_id+"]"+" ts_id["+ts_id+"]");
        if(ts_id == 0 && service_id != 0)
            programInfo = g_dtv.get_program_by_service_id(service_id);
        else if(ts_id != 0 && service_id != 0)
            programInfo = g_dtv.get_program_by_service_id_transport_stream_id(service_id,ts_id);
        return programInfo;
    }

    private void check_home_background_service_bundle(Intent intent) {
        Bundle extras = intent.getExtras();
        Log.d(TAG,"check_home_background_service_bundle extras = "+extras);
        if(extras != null) {
            //do home background service resend acs command
            g_background_service_acs_command = extras.getString(ACSHelper.ACS_ACTION,"none");
            Log.d(TAG,"check_home_background_service_bundle action = "+g_background_service_acs_command);
            //
            check_and_do_force_tune(extras);
        }
    }

    private void update_cas_config() {
        Log.d(TAG, "update_cas_config: ");
        Thread thread = new Thread(() -> {
//            CasRefreshHelper cas_helper  = CasRefreshHelper.get_instance();
//            CasData cas_data = cas_helper.get_cas_data();
            CasData cas_data = get_prime_dtv().get_cas_data();
            Pvcfg.setTryLicenseEntitledOnly(cas_data.getTryLicenseIfEntitledChannel() == 1);
            //Pvcfg.setPVR_PJ(cas_data.getPvr() == 1);
        },"update_cas_config");

        thread.start();
    }

    private void showErrorMessage(int errCode, String message) {
        LogUtils.d("IN");
        String errorMessage = ErrorCodeUtils.getErrorMessage(this, errCode, message);
        g_liveTvMgr.show_ca_message(errorMessage);
        g_liveTvMgr.show_music_ca_message(errorMessage);
    }

    private void runBootAd(Context context){
        if(LiAdUtils.getOperateSo().equals("0")){
            LogUtils.e("SO in correct, return");
            return;
        }
        Bundle bundle = new Bundle();
        Intent intent = new Intent();

        g_is_run_ad = true;
        LogUtils.d(" call boot AD");
        bundle.putString("api_key", LiAdUtils.get_api_key());
        bundle.putString("app_bundle", LiAdUtils.get_app_bundle());
        bundle.putString("so", LiAdUtils.getOperateSo());
        bundle.putBoolean("toast", LiAdUtils.isHasToast());
        bundle.putBoolean("log", LiAdUtils.isHasLog());
        if(Pvcfg.isLiADDebug())
            bundle.putString("litv_env", LiAdUtils.getLitvEnv());
        intent.setClassName(LiAdUtils.get_packagename(),
                LiAdUtils.get_classname());
        intent.putExtras(bundle);
        startActivity(intent);

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

    private void force_tune_from_acs(int service_id, int ts_id) {
        if(service_id == 0) {
            Log.e(TAG, "force_tune_from_acs: service id == 0");
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putInt(ACSHelper.DO_FORCE_TUNE, 1);
        bundle.putInt(ProgramInfo.SERVICE_ID, service_id);
        bundle.putInt(ProgramInfo.TRANSPORT_STREAM_ID, ts_id);
        bundle.putInt(EXTRA_SCREEN_TYPE, SCREEN_TYPE_LIVE_TV);

        if (ActivityUtils.get_top_activity(this).equals(CLS_HOME_ACTIVITY)) {
            ProgramInfo programInfo = get_force_tune_program_info(bundle);
            if (programInfo != null) {
                LiveTvManager.change_channel(g_liveTvMgr, programInfo, true);
                g_liveTvMgr.enter_fullscreen();
            }
        }
        else {
            Intent appIntent = new Intent();
            appIntent.setClass(HomeActivity.this, HomeActivity.class);
            appIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            appIntent.putExtras(bundle);
            HomeActivity.this.startActivity(appIntent);
//          Log.d(TAG,"HomeBackgroundService start HomeActivity bundle = " + bundle);
        }
    }

    public void detect_usb_storage() {
        Context context = getApplicationContext();
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

        for (StorageVolume volume : storageManager.getStorageVolumes()) {

            if (null == volume.getDirectory())
                continue;
            if (volume.isPrimary())
                continue;
            if (!volume.isRemovable())
                continue;

            String usbPath = volume.getDirectory().getAbsolutePath();
            Utils.set_mount_usb_path(usbPath); // Store USB path for database
            g_dtv.pvr_init(Utils.get_mount_usb_path());
            g_dtv.hdd_monitor_start(Pvcfg.getPvrHddLimitSize());
            Log.d(TAG, "detect_usb_storage: USB path detected = " + usbPath);
            break;
        }
    }

    private boolean handle_assistant_intent(@NonNull Intent intent) {
        String action = intent.getAction();
        Uri dataUri = intent.getData(); // Determined by SUGGEST_COLUMN_INTENT_DATA
        String strChannelId = dataUri == null ? "0" : dataUri.getLastPathSegment(); // we put ch id at the end

        if (action != null
                && action.equals(Intent.ACTION_VIEW) // Determined by SUGGEST_COLUMN_INTENT_ACTION
                && strChannelId != null
                && TextUtils.isDigitsOnly(strChannelId)) {

            PrimeDtv dtv = g_dtv == null ? HomeApplication.get_prime_dtv() : g_dtv;
            ProgramInfo programInfo = dtv.get_program_by_channel_id(Long.parseLong(strChannelId));
            if (programInfo != null) {
                LiveTvManager.change_channel(g_liveTvMgr, programInfo, true);
                goto_live_tv();
                return true;
            }
        }

        return false;
    }
}