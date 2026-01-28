package com.prime.launcher;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.prime.launcher.ACSDatabase.ACSDataProviderHelper;
import com.prime.launcher.ACSDatabase.ACSHelper;
import com.prime.launcher.OTA.OtaUtils;
import com.prime.launcher.Settings.ChannelPreScanningActivity;
import com.prime.launcher.Settings.ChannelScanningActivity;
import com.prime.launcher.Settings.fragment.DMGSettingsFragment;
import com.prime.launcher.Utils.ActivityUtils;
import com.prime.launcher.OTA.OtaParam;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.utils.LogUtils;

public class HomeBackgroundService extends Service {
    static String TAG = "HomeBackgroundService";
    private static boolean g_IsRunning = false;
    public static final int ACTIVITY_CALL = 0;
    public static final int SERVICE_CALL = 1;
    public static final String IS_BOOT_COMPLETE = "is_boot_complete";
    public static final String BROADCAST_FROM_HOME_BACKGROUND_SERVICE = "from_home_background_service";

    public PrimeDtv g_dtv;
//    private acs_test_use g_acs_test_use;
    private BroadcastReceiver g_broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            ACSHelper acsHelper = ACSHelper.getInstance(null,null);
            acsHelper.init_project_id(context);
            Log.d(TAG,"HomeBackgroundService handlerThread action = "+action);
            boolean isAppAlive = HomeActivity.is_activity_alive();
            boolean isAppTop = HomeActivity.is_activity_top();
            if(g_dtv == null){
                g_dtv = HomeApplication.get_prime_dtv();
            }
//            Log.d(TAG,"HomeActivity.isActivityAlive() = "+isAppAlive);
            if(acsHelper.check_asc_command(context,intent,SERVICE_CALL) == 1) {
                if(isOtaParam(intent)) {
                    Log.d(TAG,"do ota");
                    OtaParam otaParam = OtaParam.parser_from_acs_data(intent);
                    if (get_model_name().equals(otaParam.getModel_check_name()) && otaParam.getOta_version() != null && otaParam.getDownload_url() != null)
                    {
                        if (otaParam.getIs_force_update()) {
                            //send broadcast to updater to do
                            OtaUtils.send_Start_Ota(context);
                        } else {
                            if (ActivityUtils.is_launcher_running_top(context)) {
                                LogUtils.d("OTA MD5 "+g_dtv.gpos_info_get().getOta_md5()+" "+otaParam.getMd5_checksum());
                                if(!g_dtv.gpos_info_get().getOta_md5().equals(otaParam.getMd5_checksum())) {
                                    ACSHelper.do_acs_command(ACSHelper.MSG_OTA_UPDATE, null, 0, 0);
                                }
                                //Intent intent1 = new Intent();
                                //intent1.setAction(HomeBroadcastReceiver.DO_USER_OTA_CHECK);
                                //context.sendBroadcast(intent1);
                            }
                            else
                            {
                                OtaUtils.send_stop_ota_to_updater(context);
                                // if user not in launcher, send notify again after 15 min
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        context.sendBroadcast(intent);
                                    }
                                }, 30000);
                            }
                        }
                    }
                    else {
                        Intent otaErrorIntent = new Intent(ACSHelper.ABUPDATE_BROADCAST_ERROR);
                        OtaUtils.send_ota_result_to_acs(context, otaErrorIntent);
                    }
                }
                else if (isOtaResult(intent)) {
                    OtaUtils.send_ota_result_to_acs(context, intent);
                    if(intent.getAction().equals(ACSHelper.ABUPDATE_BROADCAST_COMPLETE)) {
                        HomeApplication.get_prime_dtv().gpos_info_update_by_key_string(GposInfo.GPOS_OTA_MD5, "0");
                        HomeApplication.get_prime_dtv().saveGposKeyValue(GposInfo.GPOS_OTA_MD5, "0");
                        SystemProperties.set("persist.sys.prime.ota_download_status", "0");
                        SystemProperties.set("persist.sys.prime.ota_last_download_version", "");
                        SystemProperties.set("persist.sys.prime.zipfile.download.status", "0");
                        OtaUtils.send_start_reboot(context);
                    }
                }
                else if ( isChannelScan(intent) )
                {
                    Log.d(TAG,"do channel scan");
                    ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
                    String symbol_rate_str  = acsDataProviderHelper.get_acs_provider_data(context,"symbol_rate");
                    String frequency_str  = acsDataProviderHelper.get_acs_provider_data(context,"frequency");
                    String qam_str  = acsDataProviderHelper.get_acs_provider_data(context,"qam");
                    if ( symbol_rate_str == null )
                    {
                        symbol_rate_str = (Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG)?ChannelPreScanningActivity.DEFAULT_DMG_CHANNEL_SYMBOL_RATE:ChannelPreScanningActivity.DEFAULT_CHANNEL_SYMBOL_RATE);
                    }
                    if ( frequency_str == null )
                    {
                        frequency_str = (Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG)?ChannelPreScanningActivity.DEFAULT_DMG_CHANNEL_FREQUENCY:ChannelPreScanningActivity.DEFAULT_CHANNEL_FREQUENCY);
                    }
                    if ( qam_str == null )
                    {
                        qam_str = (Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG)?ChannelPreScanningActivity.DEFAULT_DMG_CHANNEL_QAM_TEXT:ChannelPreScanningActivity.DEFAULT_CHANNEL_QAM_TEXT);
                    }
                    Intent appIntent = new Intent();

                    Bundle bundle = new Bundle();
                    bundle.putBoolean(ChannelScanningActivity.SCAN_FROM_ACS, true);
                    bundle.putBoolean(ChannelScanningActivity.AUTO_DETECT, true);
                    bundle.putBoolean(ChannelScanningActivity.SCANNING_PAGE, false);
                    bundle.putBoolean(ChannelScanningActivity.SINGLE_FREQ, false);
                    bundle.putInt(ChannelScanningActivity.SCANNING_FREQ, Integer.valueOf(frequency_str));
                    bundle.putInt(ChannelScanningActivity.SCANNING_SR, Integer.valueOf(symbol_rate_str));
                    bundle.putInt(ChannelScanningActivity.SCANNING_BW, 6);
                    bundle.putInt(ChannelScanningActivity.SCANNING_MODULATION, get_qam(qam_str));
                    bundle.putString(ChannelScanningActivity.SCANNING_MODULATION_TEXT, qam_str);
                    bundle.putInt(ChannelScanningActivity.SCANNING_TUNERID, 0);

                    appIntent.putExtras(bundle);
                    appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    appIntent.setClass(context, ChannelScanningActivity.class);
                    startActivity(appIntent);
                }
                else if ( isForceTune(intent) )
                {
                    if(!isAppAlive) { //launcher not alive, start launcher
                        Intent appIntent = new Intent();
                        Bundle bundle = new Bundle();
                        appIntent.setClass(context, HomeActivity.class);
                        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        bundle.putString(ACSHelper.ACS_ACTION, action);
                        appIntent.putExtras(bundle);
                        context.startActivity(appIntent);
                        Log.d(TAG, "HomeBackgroundService start HomeActivity bundle = " + bundle);
                    }
                }
                else if (isSignalDetect(intent))
                {
                    Log.d(TAG, "HomeBackgroundService signal detect");

                    TpInfo tpInfo = ChannelPreScanningActivity.get_tpInfo(context);
                    int feq = tpInfo.CableTp.getFreq()/1000;
                    int sym = tpInfo.CableTp.getSymbol();
                    int qam = tpInfo.CableTp.getQamRealValue();

                    Intent signalDetectIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(DMGSettingsFragment.SCAN_FROM_ACS, false);
                    bundle.putBoolean(DMGSettingsFragment.AUTO_DETECT, true);
                    bundle.putBoolean(DMGSettingsFragment.SCANNING_PAGE, true);
                    bundle.putBoolean(DMGSettingsFragment.SINGLE_FREQ, false);
                    bundle.putInt(DMGSettingsFragment.SCANNING_FREQ, feq);
                    bundle.putInt(DMGSettingsFragment.SCANNING_SR, sym);
                    bundle.putInt(DMGSettingsFragment.SCANNING_BW, 6);
                    bundle.putInt(DMGSettingsFragment.SCANNING_MODULATION, get_qam(String.valueOf(qam)));
                    bundle.putString(DMGSettingsFragment.SCANNING_MODULATION_TEXT, String.valueOf(qam));
                    bundle.putInt(DMGSettingsFragment.SCANNING_TUNERID, 0);
                    signalDetectIntent.putExtras(bundle);
                    signalDetectIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    signalDetectIntent.setClass(context, ChannelScanningActivity.class);
                    context.startActivity(signalDetectIntent);
                }
            }
        }
    };
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG,"HomeBackgroundService onCreate");
        g_IsRunning = true;
        super.onCreate();
//        g_acs_test_use = new acs_test_use(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 每次startService時會呼叫
        Log.d(TAG,"HomeBackgroundService onStartCommand Start");
//        boolean is_boot_complete = intent.getBooleanExtra(IS_BOOT_COMPLETE, false);
//        if(is_boot_complete) {
        IntentFilter filter = HomeBroadcastReceiver.getHomeBroadcastFilterForHomeBackgroundService();
        registerReceiver(g_broadcastReceiver,filter, RECEIVER_EXPORTED);
//        g_acs_test_use = new acs_test_use(this);
//        g_acs_test_use.acs_test_register_brocastReceiver(this);
//        }
        OtaUtils.init_ota_reminder_utils(this);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"HomeBackgroundService onDestroy");
        g_IsRunning = false;
        super.onDestroy();
        unregisterReceiver(g_broadcastReceiver);
//        g_acs_test_use.acs_test_unregister_brocastReceiver(this);
    }

    public boolean isOtaParam(Intent intent) {
        return intent.getAction().equals(ACSHelper.OTA_PARAM);
    }

    public boolean isOtaResult(Intent intent) {
        return intent.getAction().equals(ACSHelper.ABUPDATE_BROADCAST_ERROR)
                || intent.getAction().equals(ACSHelper.ABUPDATE_BROADCAST_COMPLETE);
    }

    public boolean isChannelScan(Intent intent) {
        return intent.getAction().equals(ACSHelper.CHANNEL_SCAN);
    }

    public boolean isForceTune(Intent intent) {
        return intent.getAction().equals(ACSHelper.FORCE_TUNE);
    }

    public boolean isSignalDetect(Intent intent) {
        return intent.getAction().equals(ACSHelper.SIGNAL_DETECT);
    }

    public static int get_qam(String qam_str) {
        if (TextUtils.isEmpty(qam_str)) {
            return -1;
        }
        int qam_in_value = Integer.valueOf(qam_str);
        if (qam_in_value == 256)
            return TpInfo.Cable.QAM_256;
        else if (qam_in_value == 128)
            return TpInfo.Cable.QAM_128;
        else if (qam_in_value == 64)
            return TpInfo.Cable.QAM_64;
        else if (qam_in_value == 32)
            return TpInfo.Cable.QAM_32;
        else if (qam_in_value == 16)
            return TpInfo.Cable.QAM_16;
        else
            return TpInfo.Cable.QAM_AUTO;
    }

    private String get_model_name()
    {
        Boolean fake = SystemProperties.getBoolean("persist.sys.prime.acs.fake_model_name", false);
        String model_name = fake?"TATV-8000":Build.MODEL;
        Log.d(TAG, "model_name = "+model_name);
        return model_name ;
    }

    public static boolean isServiceRunning() {
        return g_IsRunning;
    }
    public static void startService(Context context)
    {
        if ( isServiceRunning() ) {
            Log.d(TAG, "HomeBackgroundService already running!!!");
            return;
        }

        Intent serviceIntent = new Intent();
        serviceIntent.setClass(context, HomeBackgroundService.class);
        serviceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        serviceIntent.putExtra(HomeBackgroundService.IS_BOOT_COMPLETE, true);
        context.startService(serviceIntent);
        Log.d(TAG, "start HomeBackgroundService!!!");
    }
}
