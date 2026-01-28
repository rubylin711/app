package com.prime.dmg.launcher.ACSDatabase;

import static com.prime.dmg.launcher.Settings.fragment.EngineeringSettingFragment.INFO_HINT_OFF_VALUE;
import static java.lang.Thread.sleep;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.prime.dmg.launcher.HomeApplication;
import com.prime.dmg.launcher.HomeBackgroundService;
import com.prime.dmg.launcher.Settings.ChannelScanningActivity;
import com.prime.dmg.launcher.Utils.DownloadImageSpeedTest;
import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.sysdata.GposInfo;
import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.Mail.MailManager;
import com.prime.dtv.sysdata.EnTableType;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.sysdata.TpInfo;
import com.prime.dtv.utils.LogUtils;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;


public class ACSHelper {
    private static final String TAG = "ACSHelper";

    public static final int PROJECT_TBC = 1;
    public static final int PROJECT_DMG = 2;
    public static final int PROJECT_NKH = 3;

    public static final String ACS_ACTION = "com.prime.acsclient";
    //from provision
    public static final String MUSIC_CATEGORY = "com.prime.acsclient.update.music_category";
    public static final String EULA = "com.prime.acsclient.update.Eula";
    public static final String ILLEGAL_NETWORK = "com.prime.acsclient.update.IllegalNetwork";
    public static final String PROJECT_ID = "com.prime.acsclient.update.projectId";
    public static final String NETWORK_QUALITY_DEFINED= "com.prime.acsclient.update.networkQualityDefined";
    public static final String LAUNCHER_PROFILE = "com.prime.acsclient.update.launcher_profile";

    //from device operation
    public static final String TV_MAIL_STATUS = "com.prime.acsclient.update.TVMAILStatus";
    public static final String DVR_STATUS = "com.prime.acsclient.update.DVRStatus";
    public static final String DTV_STATUS = "com.prime.acsclient.update.DTVStatus";
    public static final String IP_WHITELIST_STATUS = "com.prime.acsclient.update.IPWhitelistStatus";
    public static final String TICKER_STATUS = "com.prime.acsclient.update.TickerStatus"; //0 : 不強制show ticker,跟據ticker的mail_type決定. 1 : 強制show ticker
    public static final String SYSTEM_ALERT_STATUS = "com.prime.acsclient.update.SystemAlertStatus";
    public static final String LAUNCHER_LOCK_STATUS = "com.prime.acsclient.update.LauncherLockStatus";
    public static final String STANDBY_REDIRECT_STATUS = "com.prime.acsclient.update.StandbyRedirectStatus";
    public static final String FORCE_SCREEN_STATUS = "com.prime.acsclient.update.ForceScreenStatus";
    public static final String PIN_CODE = "com.prime.acsclient.update.pin_code";
    public static final String HOME_ID = "com.prime.acsclient.update.home_id";
    public static final String NIT_ID = "com.prime.acsclient.update.nit_id";
    public static final String SO = "com.prime.acsclient.update.so";
    public static final String BAT_ID = "com.prime.acsclient.update.bat_id";
    public static final String BOX_SI_NIT = "com.prime.acsclient.update.box_si_nit";
    public static final String NETWORK_SYNC_TIME = "com.prime.acsclient.update.network_sync_time";
    public static final String STORAGE_SERVER = "com.prime.acsclient.update.storage_server";
    public static final String AD_LIST = "com.prime.acsclient.update.ad_list";
    public static final String RECOMMENDS = "com.prime.acsclient.update.recommends";
    public static final String RANK_LIST = "com.prime.acsclient.update.rank_list";
    public static final String HOT_VIDEO = "com.prime.acsclient.update.hot_video";
    public static final String RECOMMEND_PACKAGES = "com.prime.acsclient.update.recommend_packages";
    public static final String MUSIC_AD = "com.prime.acsclient.update.ad_profile";
    public static final String PARENTAL_LOCK_PASSWORD = "com.prime.acsclient.update.parental_lock_password";
    public static final String TV_MAIL = "com.prime.acsclient.update.tv_mail";
    public static final String OTA_PARAM = "com.prime.acsclient.update.ota_param";
    public static final String ZIPCODE = "com.prime.acsclient.update.zipcode";
    public static final String MOBILE = "com.prime.acsclient.update.mobile";

    //from OTA SERVICE
    public static final String ABUPDATE_BROADCAST_ERROR = "com.prime.android.tv.otaservice.abupdate.error";
    public static final String ABUPDATE_BROADCAST_COMPLETE = "com.prime.android.tv.otaservice.abupdate.complete";

    //from MQTT
    public static final String FORCE_TUNE = "com.prime.acsclient.mqtt_command.force_tune";
    public static final String CHANNEL_SCAN = "com.prime.acsclient.mqtt_command.channel_scan";
    public static final String NETWORK_SPEED_TEST = "com.prime.acsclient.mqtt_command.network_speed_test";
    public static final String SIGNAL_DETECT = "com.prime.acsclient.mqtt_command.signal_detect";
    public static final String PA_RESET = "com.prime.acsclient.mqtt_command.pa_reset";
    public static final String PA_LOCK = "com.prime.acsclient.update.pa_lock";

    //do in ui
    public static final int MSG_NEW_MAIL  =   1;
    public static final int MSG_NEW_TICKER  =   2;
    public static final int MSG_FORCE_TUNE  =   3;
    public static final int MSG_UPDATE_NETWORK_CHECK  =   4;
    public static final int MSG_PA_LOCK  =   5;
    public static final int MSG_NETWORK_QUALITY_DEFINED  =   6;
    public static final int MSG_STORAGE_SERVER  =   7;
    public static final int MSG_AD_LIST =   8;
    public static final int MSG_RECOMMENDS  =   9;
    public static final int MSG_RANK_LIST  =   10;
    public static final int MSG_HOT_VIDEO  =  11;
    public static final int MSG_RECOMMEND_PACKAGES  =   12;
    public static final int MSG_AD_PROFILE  =   13;
    public static final int MSG_UPDATE_MUSIC_UI  =   14;
    public static final int MSG_UPDATE_MUSIC_AD_UI  =   15;
    public static final int MSG_OTA_UPDATE  =   16;
    public static final int MSG_FORCE_SCREEN  =   17;
    public static final int MSG_LAUNCHER_PROFILE = 18;
    public static final int MSG_SYSTEM_ALERT = 19;
    public static final int MSG_UPDATE_HOME_MENU = 20;
    public static final int MSG_CLOSE_MAIL = 21;

    public static final String DO_FORCE_TUNE    = "com.prime.dmg.launcher.do_force_tune";

    //broadcast
    public static final String INTENT_NETWORK_QUALITY_DEFINED_UPDATE = "com.prime.acsclient.update.network_quality_defined";

    private Handler g_main_thread_handler;
    WeakReference<AppCompatActivity> g_ref;

    private static ACSHelper g_acs_helper = null;

    public static ACSHelper getInstance(Handler mainThreadHandler, AppCompatActivity activity) {
        if(g_acs_helper == null || g_acs_helper.g_main_thread_handler == null) {
            g_acs_helper = new ACSHelper(mainThreadHandler,activity);
        }
        return g_acs_helper;
    }

    public ACSHelper (Handler mainThreadHandler,AppCompatActivity activity) {
        g_main_thread_handler = mainThreadHandler;
        g_ref = new WeakReference<>(activity);
        if ( activity != null )
            init_project_id(activity.getApplicationContext()) ;
    }

    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    public Handler get_main_thread_handler() {
        return g_main_thread_handler;
    }

    public static void do_acs_command(int what, Object obj, int arg1, int arg2) {
        Handler main_thread_handler = ACSHelper.getInstance(null,null).get_main_thread_handler();
        if(main_thread_handler == null)
            return;
        Message msg = main_thread_handler.obtainMessage();
        msg.what = what;
        msg.obj = obj;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        Log.d(TAG,"to launcher main thread do acs command : " + what);
        main_thread_handler.sendMessage(msg);
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MUSIC_CATEGORY);
        intentFilter.addAction(MUSIC_AD);
        intentFilter.addAction(EULA);
        intentFilter.addAction(ILLEGAL_NETWORK);
        intentFilter.addAction(PROJECT_ID);
        intentFilter.addAction(NETWORK_QUALITY_DEFINED);
        intentFilter.addAction(LAUNCHER_PROFILE);
        intentFilter.addAction(TV_MAIL_STATUS);
        intentFilter.addAction(DVR_STATUS);
        intentFilter.addAction(DTV_STATUS);
        intentFilter.addAction(IP_WHITELIST_STATUS);
        intentFilter.addAction(TICKER_STATUS);
        intentFilter.addAction(SYSTEM_ALERT_STATUS);
        intentFilter.addAction(LAUNCHER_LOCK_STATUS);
        intentFilter.addAction(STANDBY_REDIRECT_STATUS);
        intentFilter.addAction(FORCE_SCREEN_STATUS);
        intentFilter.addAction(PIN_CODE);
        intentFilter.addAction(HOME_ID);
        intentFilter.addAction(NIT_ID);
        intentFilter.addAction(SO);
        intentFilter.addAction(BAT_ID);
        intentFilter.addAction(BOX_SI_NIT);
        intentFilter.addAction(NETWORK_SYNC_TIME);
        intentFilter.addAction(STORAGE_SERVER);
        intentFilter.addAction(AD_LIST);
        intentFilter.addAction(RECOMMENDS);
        intentFilter.addAction(RANK_LIST);
        intentFilter.addAction(HOT_VIDEO);
        intentFilter.addAction(RECOMMEND_PACKAGES);
        intentFilter.addAction(PARENTAL_LOCK_PASSWORD);
        intentFilter.addAction(TV_MAIL);
        intentFilter.addAction(OTA_PARAM);
        intentFilter.addAction(FORCE_TUNE);
        intentFilter.addAction(CHANNEL_SCAN);
        intentFilter.addAction(NETWORK_SPEED_TEST);
        intentFilter.addAction(SIGNAL_DETECT);
        intentFilter.addAction(PA_RESET);
        intentFilter.addAction(ZIPCODE);
        intentFilter.addAction(PA_LOCK);
        intentFilter.addAction(MOBILE);
        intentFilter.addAction(ABUPDATE_BROADCAST_ERROR);
        intentFilter.addAction(ABUPDATE_BROADCAST_COMPLETE);

        return intentFilter;
    }

    public static void delete_acs_provider_data(Context context,String key_name) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        acsDataProviderHelper.delete_acs_provider_data(context,key_name);
    }

    public static void recheck_acs_command_by_provider_data(Context context,Handler handler) {
        if(handler != null) {
            Intent intent = new Intent();

            if(get_do_network_speed_test(context)) {
                Message msg = handler.obtainMessage();
                intent.setAction(NETWORK_SPEED_TEST);
                msg.obj = intent;
                handler.sendMessage(msg);
            }

            if(get_do_signal_detect(context)) {
                Message msg = handler.obtainMessage();
                intent.setAction(SIGNAL_DETECT);
                msg.obj = intent;
                handler.sendMessage(msg);
            }

            if(get_Parental_lock_password(context) != null) {
                Message msg = handler.obtainMessage();
                intent.setAction(PARENTAL_LOCK_PASSWORD);
                msg.obj = intent;
                handler.sendMessage(msg);
            }
        }
    }
Runnable gpos_update;
    public void init_project_id( Context context )
    {
        String model_type_str = "MODULE_DMG";
        int model_type = Pvcfg.MODULE_DMG;
        if(get_ProjectId(context) == PROJECT_TBC) {
            model_type = Pvcfg.MODULE_TBC;
            model_type_str = "MODULE_TBC";
        }
		if(Pvcfg.isFixedModuelType() == false){
			Pvcfg.setModuleType(model_type);
			if ( get() != null )
				get().g_dtv.set_module_type(model_type);
		}
        Log.d(TAG,"acs command to set module to "+model_type_str);
        Runnable gpos_update = new Runnable() {
            @Override
            public void run() {
                if (get() == null)
                    new Handler(Looper.getMainLooper()).postDelayed(this, 3000);
                else {
                    if (get().g_dtv == null)
                        get().g_dtv = HomeApplication.get_prime_dtv();
                    get().g_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_NIT_ID, get_NITId(context));
                    get().g_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_BAT_ID, get_BATId(context));
                    get().g_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_SO, get_So(context));
                    get().g_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_HOME_ID, get_HomeId(context));
                    get().g_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_ZIPCODE, get_Zipcode(context));
                    get().g_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_MOBILE, get_Mobile(context));
                    Pvcfg.setBatId(get().g_dtv.gpos_info_get().getBatId());
                    //set project id , tbc/dmg/knh
                }
            }
        };
        new Handler(Looper.getMainLooper()).post(gpos_update);
    }

    public int check_asc_command(Context context, Intent intent, int call_by_service) {
        String action = intent.getAction();
        Log.d(TAG,"check asc command : action["+action+"]");
//        Log.d(TAG,"g_main_thread_handler = "+g_main_thread_handler);
        int ret = 0;
        if( call_by_service == HomeBackgroundService.SERVICE_CALL )
        {
            if(action.equals(OTA_PARAM)) {
                //do in HomeBackgroundService.java
                ret = 1;
                //            OtaParam otaParam = OtaParam.parser_from_acs_data(intent);
                //            ACSHelper.do_acs_command(MSG_OTA_UPDATE, otaParam, 0, 0);
            }
            else if(action.equals(CHANNEL_SCAN)) {
                // do channel scan in backgroind service
                ret = 1;
            }
            else if(action.equals(FORCE_TUNE))
            {
                ret = 1;
            }
            else if (action.equals(ABUPDATE_BROADCAST_ERROR) || action.equals(ABUPDATE_BROADCAST_COMPLETE)) {
                ret = 1;
            }
            else if (action.equals(SIGNAL_DETECT))
            {
                ret = 1;
            }
            return ret;
        }

        if(action.equals(MUSIC_CATEGORY)) {
            ACSHelper.do_acs_command(MSG_UPDATE_MUSIC_UI, null, 0, 0);
        }
        else if(action.equals(MUSIC_AD))
        {
            ACSHelper.do_acs_command(MSG_UPDATE_MUSIC_AD_UI, null, 0, 0);
        }
        else if(action.equals(EULA)) {

        }
        else if(action.equals(ILLEGAL_NETWORK)) {
            if(Pvcfg.isCheckIllegalNetwork())
                ACSHelper.do_acs_command(MSG_UPDATE_NETWORK_CHECK, null, 0, 0);
        }
        else if(action.equals(PROJECT_ID)) {
            init_project_id(context);
        }
        else if(action.equals(NETWORK_QUALITY_DEFINED)) {
            ACSHelper.do_acs_command(MSG_NETWORK_QUALITY_DEFINED, null, 0, 0);
        }
        else if(action.equals(LAUNCHER_PROFILE)) {
            ACSHelper.do_acs_command(MSG_LAUNCHER_PROFILE, null, 0, 0);
        }
        else if(action.equals(TV_MAIL_STATUS)) {
            //set mail icon show flag
        }
        else if(action.equals(DVR_STATUS)) {
            int value = 0;
            if(ACSHelper.get_DVRStatus(context))
                value = 1;
            get().g_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_PVRENABLE,value);
            //get().g_dtv.save_table(EnTableType.GPOS);
            get().g_dtv.saveGposKeyValue(GposInfo.GPOS_PVRENABLE,value);

            Pvcfg.setPVR_PJ(value==1);
            ACSHelper.do_acs_command(MSG_UPDATE_HOME_MENU, null, 0, 0);
//            delete_acs_provider_data(context,"DVRStatus");
        }
        else if(action.equals(DTV_STATUS)) {

        }
        else if(action.equals(IP_WHITELIST_STATUS)) {
            ACSHelper.do_acs_command(MSG_UPDATE_NETWORK_CHECK, null, 0, 0);
        }
        else if(action.equals(TICKER_STATUS)) {
            //set ticke show flag
        }
        else if(action.equals(SYSTEM_ALERT_STATUS)) {
            ACSHelper.do_acs_command(MSG_SYSTEM_ALERT, null, 0, 0);
        }
        else if(action.equals(LAUNCHER_LOCK_STATUS)) {

        }
        else if(action.equals(STANDBY_REDIRECT_STATUS)) {
            int value = ACSHelper.get_StandbyRedirectStatus(context);
            get().g_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_STANDBY_REDIRECT,value);
            //get().g_dtv.save_table(EnTableType.GPOS);
            get().g_dtv.saveGposKeyValue(GposInfo.GPOS_STANDBY_REDIRECT,value);
//            delete_acs_provider_data(context,"StandbyRedirectStatus");
        }
        else if(action.equals(FORCE_SCREEN_STATUS)) {
            ACSHelper.do_acs_command(MSG_FORCE_SCREEN, null, 0, 0);
        }
        else if(action.equals(PIN_CODE)) {
            int value = ACSHelper.get_PinCode(context);
            get().g_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_WORKER_PASSWORD_VALUE,value);
            //get().g_dtv.save_table(EnTableType.GPOS);
            get().g_dtv.saveGposKeyValue(GposInfo.GPOS_WORKER_PASSWORD_VALUE,value);
            delete_acs_provider_data(context,"pin_code");
        }
        else if(action.equals(MOBILE)) {
            String value = ACSHelper.get_Mobile(context);
            get().g_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_MOBILE,value);
            //get().g_dtv.save_table(EnTableType.GPOS);
            get().g_dtv.saveGposKeyValue(GposInfo.GPOS_MOBILE,value);
//            delete_acs_provider_data(context,"mobile");
        }
        else if(action.equals(HOME_ID)) {
            String value = ACSHelper.get_HomeId(context);
            get().g_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_HOME_ID,value);
            //get().g_dtv.save_table(EnTableType.GPOS);
            get().g_dtv.saveGposKeyValue(GposInfo.GPOS_HOME_ID,value);
            //不知道要幹嘛
        }
        else if(action.equals(NIT_ID)) {
            int value = ACSHelper.get_NITId(context);
            get().g_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_NIT_ID,value);
            //get().g_dtv.save_table(EnTableType.GPOS);
            get().g_dtv.saveGposKeyValue(GposInfo.GPOS_NIT_ID,value);
//            delete_acs_provider_data(context,"nit_id");
        }
        else if(action.equals(SO)) {
            //目前看不到作用在哪
            String value = ACSHelper.get_So(context);
            get().g_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_SO,value);
            //get().g_dtv.save_table(EnTableType.GPOS);
            get().g_dtv.saveGposKeyValue(GposInfo.GPOS_SO,value);
            SystemProperties.set("persist.sys.prime.so", value);
        }
        else if(action.equals(BAT_ID)) {
            int value = ACSHelper.get_BATId(context);
            //get().g_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_BAT_ID,value);
            //get().g_dtv.save_table(EnTableType.GPOS);
            Pvcfg.setBatId(value);
            LogUtils.d("set BAT id "+value);
//            delete_acs_provider_data(context,"bat_id");
        }
        else if(action.equals(BOX_SI_NIT)) {
            //GposInfo gposInfo = get().g_dtv.gpos_info_get();
            //int value = gposInfo.getSINitNetworkId();
            //set_BoxNITIdToASC(context,value);
        }
        else if(action.equals(NETWORK_SYNC_TIME)) {

        }
        else if(action.equals(STORAGE_SERVER)) {
            ACSHelper.do_acs_command(MSG_STORAGE_SERVER, null, 0, 0);
        }
        else if(action.equals(AD_LIST)) {
            ACSHelper.do_acs_command(MSG_AD_LIST, null, 0, 0);
        }
        else if(action.equals(RECOMMENDS)) {
            ACSHelper.do_acs_command(MSG_RECOMMENDS, null, 0, 0);
        }
        else if(action.equals(RANK_LIST)) {
            ACSHelper.do_acs_command(MSG_RANK_LIST, null, 0, 0);
        }
        else if(action.equals(HOT_VIDEO)) {
            ACSHelper.do_acs_command(MSG_HOT_VIDEO, null, 0, 0);
        }
        else if(action.equals(RECOMMEND_PACKAGES)) {
            ACSHelper.do_acs_command(MSG_RECOMMEND_PACKAGES, null, 0, 0);
        }
        else if(action.equals(PARENTAL_LOCK_PASSWORD)) {
            String strValue = get_Parental_lock_password(context);
            if(strValue != null && strValue.length() != 0) {
                int value = Integer.parseInt(strValue);
                get().g_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_PASSWORD_VALUE, value);
                get().g_dtv.saveGposKeyValue(GposInfo.GPOS_PASSWORD_VALUE,value);
                //get().g_dtv.save_table(EnTableType.GPOS);
                delete_acs_provider_data(context,"parental_lock_password");
            }
        }
        else if(action.equals(TV_MAIL)) {
//            Log.d(TAG,"do TV_MAIL call_by_service = "+call_by_service);
            MailManager mailManager = MailManager.GetInstance(null);
            ret = mailManager.parser_acs_mail_data(context,/*g_main_thread_handler,*/call_by_service);
        }
        else if(action.equals(FORCE_TUNE)) {
//            Log.d(TAG,"call_by_service = "+call_by_service);
            int service_id = ACSHelper.get_service_id(context);
            Log.d(TAG,"do force tune");
            ACSHelper.do_acs_command(MSG_FORCE_TUNE, null, service_id, 0);
            delete_acs_provider_data(context, "service_id");
        }
        else if(action.equals(NETWORK_SPEED_TEST)) {
            // do network speed test
            ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
            String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"do_network_speed_test");
            Log.d(TAG,"do_network_speed_test = "+acs_data);
            if(acs_data.equals("1")) {
                Thread thread = get_thread_network_speed_test();
                thread.start();
            }
            //then delete command
            delete_acs_provider_data(context, "do_network_speed_test");
        }
        else if(action.equals(SIGNAL_DETECT)) {
            return ret;
            //do signal detect
            /*ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
            String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"do_signal_detect");
            Log.d(TAG,"do_signal_detect = "+acs_data);
            if(acs_data.equals("1")) {
                Thread thread = get_thread_signal_detect();
                thread.start();
            }
            //then delete command
            delete_acs_provider_data(context,"do_signal_detect");*/
        }
        else if(action.equals(PA_RESET)) { //not use

        }
        else if(action.equals(ZIPCODE)) {
            String value = ACSHelper.get_Zipcode(context);
            get().g_dtv.gpos_info_update_by_key_string(GposInfo.GPOS_ZIPCODE,value);
            get().g_dtv.saveGposKeyValue(GposInfo.GPOS_ZIPCODE,value);
        }
        else if(action.equals(PA_LOCK)) {
            ACSHelper.do_acs_command(MSG_PA_LOCK, null, 0, 0);
        }
        return ret;
    }

    private @NonNull Thread get_thread_network_speed_test() {
        Runnable runnable = () -> {
            try {
                DownloadImageSpeedTest downloadImageSpeedTest = new DownloadImageSpeedTest(get(),null);
                while(downloadImageSpeedTest.is_not_running()) {
                    Log.d(TAG,"DownloadImageSpeedTest is running");
                    sleep(1000);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
        Thread thread = new Thread(runnable, "ACSHelper 1");
        return thread;
    }

    private @NonNull Thread get_thread_signal_detect() {
        Runnable runnable = () -> {
            try {
                int i = 5;
                Log.d(TAG,"signal_detect is running");
                ProgramInfo programInfo = get().signal_detect();
                TpInfo tpInfo = get().g_dtv.tp_info_get(programInfo.getTpId());
                int tuner_id = programInfo.getTunerId();
                boolean lock = false;
                int strength = 0,quality = 0, snr = 0;
                double ber = 0;
                while(i > 0 && tuner_id != -1) {
                    lock = get().g_dtv.get_tuner_status(tuner_id);
                    // tuner framework strength return thousandths of a dBm (0.001dBm)
                    // convert to dBmV
                    strength = get().g_dtv.get_signal_strength(tuner_id) / 1000 + ChannelScanningActivity.DBM_TO_DBMV;
                    // tuner framework quality return percent
                    quality = get().g_dtv.get_signal_quality(tuner_id);
                    // tuner framework SNR return thousandths of a deciBel (0.001dB)
                    snr = get().g_dtv.get_signal_snr(tuner_id) / 1000;
                    // tuner framework BER return the number of error bit per 1 billion bits.
                    // divide by 1 million according to RTK
                    ber = get().g_dtv.get_signal_ber(tuner_id) / 1000000d;
                    Log.d(TAG,String.format("lock: %b, strength: %d, quality: %d, snr: %d, ber: %E",
                            lock,strength,quality,snr,ber));
                    sleep(1000);
                    i--;
                }
                DecimalFormat ber_format = new DecimalFormat("0.0E+00");
                String str = "{\"qam_strength\":\""+strength+" dBmV\","
                        + "\"qam_mer\":\""+snr+" dB\","
                        + "\"qam\":\""+tpInfo.CableTp.getQamRealValue()+"\","
                        + "\"symbol_rate\":\""+tpInfo.CableTp.getSymbol()+"\","
                        + "\"frequency\":\""+tpInfo.CableTp.getFreq()/1000+"\","
                        + "\"qam_ber\":\""+ber_format.format(ber)+"\"}";
                Log.d(TAG,"signal info = "+str);
                set_signal_detect(get(),str);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
        Thread thread = new Thread(runnable, "ACSHelper 2");
        return thread;
    }

    public static boolean get_IpWhiteListStatus(Context context) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"IPWhitelistStatus");
        Log.d(TAG,"IPWhitelistStatus = "+acs_data);
        return (acs_data != null) ? acs_data.equals("1") : false;
    }

    public static boolean isIllegalNetwork(Context context) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"IllegalNetwork");
        Log.d(TAG,"IllegalNetwork = "+acs_data);
//        return false;
        return (acs_data != null) ? acs_data.equals("1") : false;
    }

    public static int get_ProjectId(Context context) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"projectId");
        int project_id = (acs_data != null) ? Integer.valueOf(acs_data) : 0;
        Log.d(TAG,"project_id = "+project_id);
        return project_id;
    }

    public static boolean showTicker(Context context) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"TickerStatus");
        return (acs_data != null) ? acs_data.equals("1") : true;
    }

    public static boolean showMailIcon(Context context) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"TVMAILStatus");
        String mailNotifyStatus = Integer.toString(((HomeActivity)context).g_dtv.gpos_info_get().getMailNotifyStatus());

        if (acs_data == null)
            return false;
        else if (!acs_data.equals("1"))
            return false;
        else if (mailNotifyStatus.equals(INFO_HINT_OFF_VALUE))
            return false;
        else
            return true;
    }

    public static int get_PinCode(Context context) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"pin_code");
        Log.d(TAG,"pin_code = "+acs_data);
        int value = 0;
        if(acs_data != null && acs_data.length() != 0)
            value = Integer.valueOf(acs_data);
        return value;
    }

    public static int get_StandbyRedirectStatus(Context context) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"StandbyRedirectStatus");
//        Log.d(TAG,"StandbyRedirectStatus = "+acs_data);
        int value = 0;
        if(acs_data != null && acs_data.length() != 0)
            value = Integer.valueOf(acs_data);
        return value;
    }

    public static String get_Mobile(Context context) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"mobile");
//        Log.d(TAG,"mobile = "+acs_data);
        return acs_data;
    }

    public static String get_HomeId(Context context) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"home_id");
//        Log.d(TAG,"home_id = "+acs_data);
        return acs_data;
    }

    public static String get_So(Context context) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"so");
//        Log.d(TAG,"so = "+acs_data);
        //int value = 0;
        //if(acs_data != null && acs_data.length() != 0)
        //    value = Integer.valueOf(acs_data);
        return acs_data;
    }

    public static int get_NITId(Context context) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"nit_id");
        Log.d(TAG,"nit_id = "+acs_data);
        int value = 0;
        if(acs_data != null && acs_data.length() != 0)
            value = Integer.valueOf(acs_data);
        return value;
    }

    public static int get_BATId(Context context) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"bat_id");
        Log.d(TAG,"bat_id = "+acs_data);
        int value = 0;
        if(acs_data != null && acs_data.length() != 0)
            value = Integer.valueOf(acs_data);
        return value;
    }

    public static String get_Zipcode(Context context) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"zipcode");
        Log.d(TAG,"zipcode = "+acs_data);
        return acs_data;
    }

    public static void set_BoxNITIdToASC(Context context, int value) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        acsDataProviderHelper.set_acs_provider_data(context,"box_si_nit",value+"");
        Log.d(TAG,"BoxNITIdToASC nit id = "+value);
    }

    public static void set_network_speed_test(Context context, String value) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        acsDataProviderHelper.set_acs_provider_data(context,"network_quality",value+"");
        Intent intent = new Intent();
        intent.setAction("com.prime.acsclient.network_quality");
        context.sendBroadcast(intent);
        Log.d(TAG,"set_network_speed_test network_quality = "+value);
    }

    public static void set_signal_detect(Context context, String value) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        acsDataProviderHelper.set_acs_provider_data(context,"signal_detect",value+"");
        Intent intent = new Intent();
        intent.setAction("com.prime.acsclient.signal_detect");
        context.sendBroadcast(intent);
        Log.d(TAG,"set_signal_detect signal_detect = "+value);
    }

    public static int get_PaLock(Context context) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"pa_lock");
        Log.d(TAG,"pa_lock = "+acs_data);
        int value = 0;
        if(acs_data != null)
            value = Integer.valueOf(acs_data);
        return value;
    }

    public static String get_Parental_lock_password(Context context) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"parental_lock_password");
        Log.d(TAG,"parental_lock_password = "+acs_data);
        return acs_data;
    }

    public static boolean get_DVRStatus(Context context) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"DVRStatus");
        Log.d(TAG,"DVRStatus = "+acs_data);
        return (acs_data != null) ? acs_data.equals("1") : true;
    }

    public static String get_NetworkQualityDefined(Context context) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"networkQualityDefined");
        Log.d(TAG,"networkQualityDefined = "+acs_data);
        return acs_data;
    }

    public static boolean get_do_network_speed_test(Context context) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"do_network_speed_test");
        Log.d(TAG,"do_network_speed_test = "+acs_data);
        return (acs_data != null) ? acs_data.equals("1") : false;
    }

    public static boolean get_do_signal_detect(Context context) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"do_signal_detect");
        Log.d(TAG,"do_signal_detect = "+acs_data);
        return (acs_data != null) ? acs_data.equals("1") : false;
    }

    public static int get_service_id(Context context) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"service_id");
        Log.d(TAG,"service_id = "+acs_data);
        int value = 0;
        if(acs_data != null)
            value = Integer.valueOf(acs_data);
        return value;
    }

    public static ACSForceSrceenParam get_force_screen_param(Context context) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"force_screen_params");
        Log.d(TAG,"force_screen_params = "+acs_data);
        ACSForceSrceenParam param = ACSForceSrceenParam.parser_data(acs_data);
        return param;
    }

    public static boolean get_ForceScreenStatus(Context context) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"ForceScreenStatus");
        Log.d(TAG,"ForceScreenStatus = "+acs_data);
        return (acs_data != null) ? acs_data.equals("1") : false;
    }

    public static ACSSystemAlertParam get_system_alert_param(Context context) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"system_alert_params");
        Log.d(TAG,"system_alert_params = "+acs_data);
        ACSSystemAlertParam param = ACSSystemAlertParam.parser_data(acs_data);
        return param;
    }

    public static boolean get_SystemAlertStatus(Context context) {
        ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
        String acs_data = acsDataProviderHelper.get_acs_provider_data(context,"SystemAlertStatus");
        Log.d(TAG,"SystemAlertStatus = "+acs_data);
        return (acs_data != null) ? acs_data.equals("1") : false;
    }
}
