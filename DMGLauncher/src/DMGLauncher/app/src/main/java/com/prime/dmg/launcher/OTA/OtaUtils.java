package com.prime.dmg.launcher.OTA;

import static android.content.Context.RECEIVER_EXPORTED;
import static android.webkit.WebViewZygote.getPackageName;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.audio.common.Int;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.prime.dmg.launcher.ACSDatabase.ACSHelper;
import com.prime.dmg.launcher.Settings.fragment.EngineeringSettingFragment;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OtaUtils {
    private static String TAG = "OtaUtils" ;
    private static boolean DEBUG = true ;
    public static final String START_OTA_REMINDER_ACTION = "com.prime.dmg.launcher.OTA.reminder.reach" ;
    private static String NEXT_REMINDER_MILLIS = "next_reminder_millis" ;
    private static String REMINDER_OTA_FILE_INFO = "reminder_ota_file_info" ;

    //ota service
    private static final String OTA_SERVICE_PACKAGE_NAME = "com.prime.android.tv.otaservice";
    //action
    private static final String ABUPDATE_BROADCAST_START = "com.prime.android.tv.otaservice.abupdate.start";
    //param of ABUPDATE_BROADCAST_START
    private static final String ABUPDATE_BROADCAST_UPDATE_ZIP_URL       = "prime.abupdate.url.zip";
    private static final String ABUPDATE_BROADCAST_UPDATE_ZIP_OFFSET    = "prime.abupdate.offset.zip";
    private static final String ABUPDATE_BROADCAST_UPDATE_ZIP_SIZE      = "prime.abupdate.size.zip";
    private static final String ABUPDATE_BROADCAST_UPDATE_PROPERTIES    = "prime.abupdate.properties.zip";
    private static final String ABUPDATE_BROADCAST_UPDATE_MODE          = "prime.abupdate.MODE";
    private static final String TARGET_PATH = "/data/ota_package/update.zip";
    private static final int UPDATE_MODE = 3;// 0:stream payload, 1:stream zip, 2:usb, 3:download

    ////param of ABUPDATE_BROADCAST_UPDATE_CALLER
    private static final String ABUPDATE_BROADCAST_REGISTER_CALLER   = "com.prime.android.tv.otaservice.abupdate.register.caller";
    private static final String ABUPDATE_BROADCAST_UPDATE_CALLER         = "prime.abupdate.caller";

    private static final String OTA_MODE = "persist.sys.prime.ota_mode" ;

    //ota updater
    private static final String STOP_OTA_UPDATER = "com.prime.otaupdater.stop_otaupdater";

    public static Calendar get_Increase_Hours_Calendar(int hours)
    {
        Calendar next_reminder_date = Calendar.getInstance() ;
        next_reminder_date.add(Calendar.HOUR_OF_DAY, hours);
        return next_reminder_date ;
    }
    public static void start_Ota_Reminder(Context context, Calendar next_reminder_date) {
        if( DEBUG ) {
            Log.d(TAG, "setOtaReminderDate");
            if ( next_reminder_date != null ) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String currentDate = sdf.format(Calendar.getInstance().getTime());
                String startCalFormatted = sdf.format(next_reminder_date.getTime());
                Log.d(TAG, "currentDate: " + currentDate);
                Log.d(TAG, "Start time: " + startCalFormatted + " action " + START_OTA_REMINDER_ACTION);
            }
        }

        if ( next_reminder_date == null )
        {
            clean_ota_reminder_data(context);
            return;
        }
        Intent intent = new Intent(START_OTA_REMINDER_ACTION);
        PendingIntent pending_intent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, next_reminder_date.getTimeInMillis(), pending_intent);
        set_db_next_reminder_millis(context, next_reminder_date.getTimeInMillis());
        if ( OtaParam.get_instance().getFull_intent_str() != null ) {
            set_reminder_ota_file_info(context, OtaParam.get_instance().getFull_intent_str());
        }
    }

    private static void clean_ota_reminder_data(Context context)
    {
        set_db_next_reminder_millis(context, 0);
        set_reminder_ota_file_info(context, null);
    }

    private static void notify_ACSClient_ota_started(Context context, OtaParam otaParam)
    {
        Intent intent = new Intent();
        intent.setPackage("com.prime.acsclient");
        intent.setAction("com.prime.acsclient.launcher_ota.started");
        intent.putExtra("com.prime.acsclient.launcher_ota.version", otaParam.getOta_version());
        Log.d(TAG,"notify_ACSClient_ota_started = " +intent);
        context.sendBroadcast(intent);
    }

    public static void send_Start_Ota(Context context)
    {
        if( DEBUG )
            Log.d( TAG, "sendStartOta" ) ;
        clean_ota_reminder_data(context);
        send_register_caller(context);
        Intent ota_intent = get_ota_intent(OtaParam.get_instance()) ;
        context.sendBroadcast(ota_intent);
        notify_ACSClient_ota_started(context, OtaParam.get_instance());
    }

    private static @NonNull Intent get_ota_intent( OtaParam otaParam ) {

        Intent intent;
        if (SystemProperties.get(OTA_MODE).equals("download")) {
            intent = new Intent(ABUPDATE_BROADCAST_START);
            intent.setPackage(OTA_SERVICE_PACKAGE_NAME);
            intent.putExtra(ABUPDATE_BROADCAST_UPDATE_ZIP_URL,      "file://"+TARGET_PATH);
            intent.putExtra(ABUPDATE_BROADCAST_UPDATE_ZIP_OFFSET,   0);
            intent.putExtra(ABUPDATE_BROADCAST_UPDATE_ZIP_SIZE,     "mPayloadSize");
            intent.putExtra(ABUPDATE_BROADCAST_UPDATE_PROPERTIES,   "mProperty");
            intent.putExtra(ABUPDATE_BROADCAST_UPDATE_MODE,         UPDATE_MODE);
        }
        else if (SystemProperties.get(OTA_MODE).equals("streaming_payload")){
            intent = new Intent();
            intent.setAction(EngineeringSettingFragment.OTA_UPDATER_ACTION);
            intent.setClassName(EngineeringSettingFragment.PACKAGE_OTA_UPDATER, EngineeringSettingFragment.PACKAGE_OTA_UPDATER + ".HttpOtaReceiver");
            intent.putExtra(EngineeringSettingFragment.EXTRA_HTTP_PAYLOAD_BIN,otaParam.getPayload_bin()+"/"+OtaParam.OTA_PAYLOAD_BIN_NAME);
            intent.putExtra(EngineeringSettingFragment.EXTRA_HTTP_PAYLOAD_PROP,otaParam.getPayload_properties()+"/"+OtaParam.OTA_PAYLOAD_PROPERTIES_NAME);
            intent.putExtra(EngineeringSettingFragment.EXTRA_HTTP_METADATA,otaParam.getMetadata()+"/"+OtaParam.OTA_METADATA_NAME);
            intent.putExtra(EngineeringSettingFragment.EXTRA_HTTP_UPDATE_IMAGE_VERSION, otaParam.getOta_version());
            intent.putExtra(EngineeringSettingFragment.EXTRA_HTTP_FORCE, otaParam.getIs_force_update());
            intent.putExtra(EngineeringSettingFragment.EXTRA_UPDATE_MODE, 0/*stream payload */);
        } else {
            /* fake data*/
            String UpdateZipFile = "http://10.1.4.157/DADA_1319D1_2_0.zip";
            long UpdatePayloadOffset = 4997;
            long UpdatePayloadSize = 1026471052;
            List<String> UpdateProperties = new ArrayList<>();
            UpdateProperties.add("FILE_HASH=lfAd60IqulK2LBQzTL6RFvoU40c5JrF8fN6toNfClWQ=");
            UpdateProperties.add("FILE_SIZE=1026471052");
            UpdateProperties.add("METADATA_HASH=uPGI/72UnKmlVXHUFF/mNsX5/1DZmRUnME6DXDqOKAA=");
            UpdateProperties.add("METADATA_SIZE=43111");
            String OtaVersion = "v1.2";
            boolean IsForceUpdate = true;

            intent = new Intent();
            intent.setAction(EngineeringSettingFragment.OTA_UPDATER_ACTION);
            intent.setClassName(EngineeringSettingFragment.PACKAGE_OTA_UPDATER, EngineeringSettingFragment.PACKAGE_OTA_UPDATER + ".HttpOtaReceiver");
            intent.putExtra(EngineeringSettingFragment.EXTRA_UPDATE_ZIP_URL,      UpdateZipFile);
            intent.putExtra(EngineeringSettingFragment.EXTRA_UPDATE_ZIP_OFFSET,   UpdatePayloadOffset);
            intent.putExtra(EngineeringSettingFragment.EXTRA_UPDATE_ZIP_SIZE,     UpdatePayloadSize);
            intent.putExtra(EngineeringSettingFragment.EXTRA_UPDATE_PROPERTIES,   UpdateProperties.toArray(new String[0]));
            intent.putExtra(EngineeringSettingFragment.EXTRA_HTTP_UPDATE_IMAGE_VERSION, OtaVersion);
            intent.putExtra(EngineeringSettingFragment.EXTRA_HTTP_FORCE, IsForceUpdate);
            intent.putExtra(EngineeringSettingFragment.EXTRA_CALLER, "com.prime.dmg.launcher");
            intent.putExtra(EngineeringSettingFragment.EXTRA_UPDATE_MODE, 1/*stream zip */);
        }

        Log.d(TAG,"getOtaIntent = " +intent);
        return intent;
    }

    public static void send_stop_ota_to_updater(Context context)
    {
        Log.d(TAG, "send_close_ota_to_updater: ");

        Intent intent = new Intent();
        intent.setPackage(EngineeringSettingFragment.PACKAGE_OTA_UPDATER);
        intent.setAction(STOP_OTA_UPDATER);
        context.sendBroadcast(intent);
    }

    public static void send_ota_result_to_acs(Context context, Intent intent)
    {
        Log.d(TAG, "send_ota_result_to_acs: ");
        intent.setPackage("com.prime.acsclient");
        context.sendBroadcast(intent);
    }

    public static void init_ota_reminder_utils(Context context)
    {
        IntentFilter intentFilter = new IntentFilter() ;
        intentFilter.addAction(START_OTA_REMINDER_ACTION);
        context.registerReceiver(g_Ota_BroadcastReceiver, intentFilter, RECEIVER_EXPORTED);
        Long next_reminder_millis = get_db_next_reminder_millis(context);
        if ( next_reminder_millis > 0 )
        {
            Calendar next_reminder_date = Calendar.getInstance();
            next_reminder_date.setTimeInMillis(next_reminder_millis);
            start_Ota_Reminder(context, next_reminder_date);
        }
    }

    public static void send_start_reboot(Context context)
    {
        Intent intent = new Intent(Intent.ACTION_REBOOT);
        intent.putExtra("nowait", 1);
        intent.putExtra("interval", 1);
        intent.putExtra("window", 0);
        context.sendBroadcast(intent);
    }

    private static BroadcastReceiver g_Ota_BroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d( TAG, "g_Ota_BradcastReceiver intent : " + intent.getAction() ) ;
            switch (intent.getAction()) {
                case START_OTA_REMINDER_ACTION:
                {
                    Intent ota_param = OtaParam.jsonStringToIntent(get_reminder_ota_file_info(context));
                    Log.d( TAG, "START_OTA_REMINDER_ACTION ota_param = " + ota_param);
                    clean_ota_reminder_data(context);
                    ACSHelper.do_acs_command(ACSHelper.MSG_OTA_UPDATE, null, 0, 0);
                    //context.sendBroadcast(ota_param);
                }break;
            }
        }
    };

    private static long get_db_next_reminder_millis(Context context) {
        int defValue = 0 ;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getLong(NEXT_REMINDER_MILLIS, defValue);
    }

    private static void set_db_next_reminder_millis(Context context, long next_reminder_millis) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putLong(NEXT_REMINDER_MILLIS, next_reminder_millis);
        editor.apply();
    }

    private static String get_reminder_ota_file_info(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(REMINDER_OTA_FILE_INFO, new Intent().toString());
    }

    private static void set_reminder_ota_file_info(Context context, String reminder_ota_file_info) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        Log.d( TAG, "set_reminder_ota_file_info reminder_ota_file_info : " + reminder_ota_file_info);
        editor.putString(REMINDER_OTA_FILE_INFO, reminder_ota_file_info);
        editor.apply();
    }

    private static void send_register_caller(Context context) {
        Log.d(TAG, "broadcastPackageName: package name = " + context.getPackageName());
        Intent intent = new Intent(ABUPDATE_BROADCAST_REGISTER_CALLER);
        intent.setPackage(OTA_SERVICE_PACKAGE_NAME);
        //intent.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
        intent.putExtra(ABUPDATE_BROADCAST_UPDATE_CALLER, context.getPackageName());
        context.sendBroadcast(intent);
    }
}
