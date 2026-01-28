package com.prime.acsclient.communicate;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.Log;

import com.prime.acsclient.ACSService;
import com.prime.acsclient.acsdata.ACSServerJsonFenceName;
import com.prime.acsclient.acsdata.HttpContentValue;
import com.prime.acsclient.common.BroadcastDefine;
import com.prime.acsclient.common.CommonDefine;
import com.prime.acsclient.common.CommonFunctionUnit;
import com.prime.acsclient.common.DeviceControlUnit;
import com.prime.acsclient.common.DeviceInfoUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SystemStatusMonitor {
    private static final String TAG = "ACS_SystemStatusMonitor" ;
    private Context g_context;
    private OTA_PARAM g_ota_param = null ;
    private PendingIntent g_start_pending_intent = null ;
    private PendingIntent g_end_pending_intent = null ;
    private String g_last_download_version = null ;
    private RepeatingTaskManager g_repeating_task_manager = null ;
    private int g_download_status = CommonDefine.IDLE;
    class OTA_PARAM {
        public int file_size = 0 ;
        public String ota_version = null ;
        public String download_url = null ;
        public String md5_checksum = null ;
        public String model_check_name = null ;
        public String release_note = null ;
        public String update_mode = null ;
        public int is_ota_after_boot = 0 ;
        public int is_ota_immediately = 0 ;
        public int is_ota_after_standby = 0 ;
        public String is_ota_in_time_window = null ;
        public int http_status = 0 ;
    }

    public SystemStatusMonitor(Context context) {
        g_context = context;
        init_ota_param();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(BroadcastDefine.OTA_ALARAM_START_ACTION);
        filter.addAction(BroadcastDefine.OTA_ALARAM_END_ACTION);
        filter.addAction(BroadcastDefine.OTA_LAUNCHER_STATED);
        filter.addAction(BroadcastDefine.ABUPDATE_BROADCAST_DOWNLOAD_COMPLETE);
        filter.addAction(BroadcastDefine.ABUPDATE_BROADCAST_DOWNLOAD_ERROR);
        filter.addAction(BroadcastDefine.ABUPDATE_BROADCAST_COMPLETE);
        filter.addAction(BroadcastDefine.ABUPDATE_BROADCAST_ERROR);
        context.registerReceiver(systemReceiver, filter, Context.RECEIVER_EXPORTED);

        g_repeating_task_manager = new RepeatingTaskManager();
    }

    private BroadcastReceiver systemReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case BroadcastDefine.ABUPDATE_BROADCAST_COMPLETE:
                {
                    Log.i( TAG, "ota service update complete!!!" ) ;
                    set_zip_download_status(CommonDefine.IDLE);
                }break;
                case BroadcastDefine.ABUPDATE_BROADCAST_ERROR:
                {
                    Log.e( TAG, "ota service update error!!!" ) ;
                    set_zip_download_status(CommonDefine.IDLE);
                }break;
                case BroadcastDefine.ABUPDATE_BROADCAST_DOWNLOAD_COMPLETE:
                {
                    Log.d( TAG, "ota service zip file download complete!!!" ) ;
                    set_zip_download_status(CommonDefine.DOWNLOAD_COMPLETE);
                    process_ota_param();
                }break;
                case BroadcastDefine.ABUPDATE_BROADCAST_DOWNLOAD_ERROR:
                {
                    Log.e( TAG, "ota service zip file download error!!!" ) ;
                    set_zip_download_status(CommonDefine.DOWNLOAD_FAIL);
                }break;
                case Intent.ACTION_SCREEN_ON:
                case Intent.ACTION_USER_PRESENT:
                {
                    CommonFunctionUnit.update_data_without_notify_launcher( ACSServerJsonFenceName.BoxStatus.INTERACTIVE_CHANGE_TIME, DeviceControlUnit.getCurrentTime() );
                    Log.d(TAG, "wakeup from standby");
                }break;
                case Intent.ACTION_SCREEN_OFF:
                {
                    CommonFunctionUnit.update_data_without_notify_launcher( ACSServerJsonFenceName.BoxStatus.INTERACTIVE_CHANGE_TIME, DeviceControlUnit.getCurrentTime() );
                    Log.d(TAG, "entry to standby");
                    if (g_ota_param.update_mode != null && g_ota_param.update_mode.equals(CommonDefine.OTA_FORCE_UPDATE) && g_ota_param.is_ota_after_standby == 1)
                        send_ota_broadcast_to_start(true);
                }break;
                case BroadcastDefine.OTA_ALARAM_START_ACTION:
                {
                    Log.d( TAG, "is alram time start !!!" ) ;
                    if ( g_repeating_task_manager != null )
                        g_repeating_task_manager.startRepeatingTask();

                    g_start_pending_intent = set_alarms( g_context, get_next_day_calendar(get_start_calendar(g_ota_param.is_ota_in_time_window)), BroadcastDefine.OTA_ALARAM_START_ACTION ) ;
                }break;
                case BroadcastDefine.OTA_ALARAM_END_ACTION:
                {
                    Log.d( TAG, "is alarm time end !!!" ) ;
                    if ( g_repeating_task_manager != null )
                        g_repeating_task_manager.stopRepeatingTask();

                    g_end_pending_intent = set_alarms( g_context, get_next_day_calendar(get_end_calendar(g_ota_param.is_ota_in_time_window)), BroadcastDefine.OTA_ALARAM_END_ACTION ) ;
                }break;
                case BroadcastDefine.OTA_LAUNCHER_STATED:
                {
                    Log.d( TAG, "OTA_LAUNCHER_STATED clear ACS ota monitor !!!" ) ;
                    String updateVersion = intent.getStringExtra(BroadcastDefine.OTA_LAUNCHER_VERSION);
                    if (!updateVersion.equals(g_ota_param.ota_version)) {
                        Log.i( TAG, "OTA_LAUNCHER_STATED updateVersion from Launcher is not the same g_ota_param.ota_version !!!" ) ;
                        break;
                    }

                    stop_all_alarms();
                    HttpContentValue.FwInfo.http_status = CommonDefine.DEFAULT_INT_VALUE;
                    HttpContentValue.FwInfo.fw_version = null;
                    HttpContentValue.FwInfo.release_notes = null;
                    HttpContentValue.FwInfo.FW_DOWNLOAD_PATH.high_bw_url = null;
                    HttpContentValue.FwInfo.FW_DOWNLOAD_PATH.low_bw_url = null;
                    HttpContentValue.FwInfo.md5_checksum = null;
                    HttpContentValue.FwInfo.file_size = CommonDefine.DEFAULT_INT_VALUE;
                    HttpContentValue.FwInfo.update_mode = null;
                    HttpContentValue.FwInfo.model_checker = null;
                    HttpContentValue.FwInfo.is_ota_in_standby = CommonDefine.DEFAULT_INT_VALUE;
                    HttpContentValue.FwInfo.is_ota_after_boot = CommonDefine.DEFAULT_INT_VALUE;
                    HttpContentValue.FwInfo.is_ota_immediately = CommonDefine.DEFAULT_INT_VALUE;
                    HttpContentValue.FwInfo.is_ota_in_time_window = null;

                    CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.FILE_SIZE, HttpContentValue.FwInfo.file_size);
                    CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.FW_VERSION, HttpContentValue.FwInfo.fw_version);
                    CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.HIGH_BW_URL, HttpContentValue.FwInfo.FW_DOWNLOAD_PATH.high_bw_url);
                    CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.LOW_BW_URL, HttpContentValue.FwInfo.FW_DOWNLOAD_PATH.low_bw_url);
                    CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.MD5_CHECKSUM, HttpContentValue.FwInfo.md5_checksum);
                    CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.MODEL_CHECKER, HttpContentValue.FwInfo.model_checker);
                    CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.RELEASE_NOTES, HttpContentValue.FwInfo.release_notes);
                    CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.DRIVEN_BY, HttpContentValue.FwInfo.update_mode);
                    CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.OTA_AFTER_BOOT, HttpContentValue.FwInfo.is_ota_after_boot);
                    CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.OTA_AFTER_IMMEDIATELY, HttpContentValue.FwInfo.is_ota_immediately);
                    CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.OTA_AFTER_STANDBY, HttpContentValue.FwInfo.is_ota_in_standby);
                    CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.OTA_AFTER_TIME, HttpContentValue.FwInfo.is_ota_in_time_window);
                    CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.FwInfo.STATUS, HttpContentValue.FwInfo.http_status);

                    update_ota_param();
                }break;
            }
        }
    };

    private void send_ota_broadcast_to_start( boolean is_force_update )
    {
        if (SystemProperties.get(CommonDefine.OTA_DOWNLOAD_STATUS).equals(String.valueOf(CommonDefine.DOWNLOADING))) {
            Log.i( TAG, "ota zip file downloading, can not send ota update broadcast" ) ;
            return;
        }

        Intent ota_param = new Intent() ;
        ota_param.putExtra( BroadcastDefine.OTA_UPDATER_PARAM_KEY_IS_FORECE_UPDATE, is_force_update ) ;
        ota_param.putExtra( BroadcastDefine.OTA_UPDATER_PARAM_KEY_DOWNLOAD_URL, g_ota_param.download_url ) ;
        ota_param.putExtra( BroadcastDefine.OTA_UPDATER_PARAM_KEY_MD5_CHECKSUM, g_ota_param.md5_checksum ) ;
        ota_param.putExtra( BroadcastDefine.OTA_UPDATER_PARAM_KEY_FILE_SIZE, g_ota_param.file_size ) ;
        ota_param.putExtra( BroadcastDefine.OTA_UPDATER_PARAM_KEY_OTA_VERSION, g_ota_param.ota_version ) ;
        ota_param.putExtra( BroadcastDefine.OTA_UPDATER_PARAM_KEY_RELEASE_NOTE, g_ota_param.release_note ) ;
        ota_param.putExtra( BroadcastDefine.OTA_UPDATER_PARAM_KEY_MODEL_CHECK_NAME, g_ota_param.model_check_name ) ;
        ota_param.setAction(BroadcastDefine.ACS_OTA_PARAM_UPDATE) ;
        CommonFunctionUnit.send_broadcast( ACSService.g_context, BroadcastDefine.LAUNCHER_PACKAGE_NAME, ota_param );
    }

    private void process_ota()
    {
        Log.d( TAG, "process_ota" ) ;
        if ( g_ota_param == null || g_ota_param.download_url == null )
        {
            Log.d( TAG, "ota download_url not exist !!!!" ) ;
            return ;
        }

        Log.d( TAG, "ota_version = " + g_ota_param.ota_version + " fw_version = " + DeviceInfoUtil.get_instance().get_fw_version()) ;
        if (g_ota_param.ota_version == null) {
            Log.e( TAG, "ota version is null !!!!" ) ;
            return ;
        }
		
        if (g_ota_param.ota_version.length()==0) {
            Log.e( TAG, "ota version is empty" ) ;
            return ;
        }  

        if (g_ota_param.ota_version.equals(DeviceInfoUtil.get_instance().get_fw_version())) {
            Log.d( TAG, "ota version is the same current version !!!!" ) ;
            return ;
        }

        Log.d( TAG, "last_download_version = " + SystemProperties.get(CommonDefine.OTA_LAST_DOWNLOAD_VERSION, "null")) ;
        if (g_ota_param.ota_version.equals(SystemProperties.get(CommonDefine.OTA_LAST_DOWNLOAD_VERSION, "null")))
        { 
            Log.d( TAG, "ota version is the same download version !!!!" ) ;
			//set_zip_download_status(CommonDefine.DOWNLOAD_COMPLETE);
			if(SystemProperties.get(CommonDefine.OTA_DOWNLOAD_STATUS).equals(String.valueOf(CommonDefine.DOWNLOAD_COMPLETE)))
				process_ota_param();			
            return ;
        }

        if (SystemProperties.get(CommonDefine.OTA_MODE).equals("download")) {
            SystemProperties.set(CommonDefine.OTA_LAST_DOWNLOAD_VERSION, g_ota_param.ota_version);
            process_ota_download_file();
        }   
        else
            process_ota_param();
    }

    private void process_ota_download_file()
    {
        Log.d( TAG, "process_ota_download_file" ) ;
        set_zip_download_status(CommonDefine.DOWNLOADING);

        Intent intent = new Intent(BroadcastDefine.ABUPDATE_BROADCAST_DOWNLOAD_ZIP_FILE);
        intent.putExtra(BroadcastDefine.BROADCAST_DOWNLOAD_ZIP_URL, g_ota_param.download_url);
        intent.putExtra(BroadcastDefine.BROADCAST_DOWNLOAD_ZIP_MD5, g_ota_param.md5_checksum);

        CommonFunctionUnit.send_broadcast( ACSService.g_context, BroadcastDefine.OTA_SERVICE_PACKAGE_NAME, intent );
        
    }
    private void process_ota_param()
    {
//        CommonFunctionUnit.printClassFields(HttpContentValue.FwInfo.class);
        if ( g_ota_param == null || g_ota_param.update_mode == null )
        {
            Log.d( TAG, "ota param not exist !!!!" ) ;
            return ;
        }
		Log.d(TAG, "g_ota_param.update_mode = "+ g_ota_param.update_mode);
        if ( g_ota_param.update_mode.equals(CommonDefine.OTA_FORCE_UPDATE) )
        {
            if ( g_ota_param.is_ota_immediately == 1 )
                send_ota_broadcast_to_start(true);

            if ( g_ota_param.is_ota_in_time_window != null && isValidTimeRange(g_ota_param.is_ota_in_time_window) )
            {
                stop_all_alarms();
				Log.d(TAG,"111111111111111111111111");
                Calendar current_time_calendar = Calendar.getInstance() ;
                Calendar expected_start_calendar = get_start_calendar(g_ota_param.is_ota_in_time_window) ;
                Calendar expected_end_calendar = get_end_calendar(g_ota_param.is_ota_in_time_window) ;
                Calendar real_start_calendar = expected_start_calendar ;
                Calendar real_end_calendar = expected_end_calendar ;

                // start --- end --- current => change to next day
                // start --- current --- end => start change one hour later
                // current --- start --- end => not change
                if ( expected_start_calendar.getTimeInMillis() <= current_time_calendar.getTimeInMillis()
                        && expected_end_calendar.getTimeInMillis() >= current_time_calendar.getTimeInMillis() )
                {
                    real_start_calendar = get_next_15min_calendar(current_time_calendar);
                }
                else if ( expected_end_calendar.getTimeInMillis() <= current_time_calendar.getTimeInMillis() )
                {
                    real_start_calendar = get_next_day_calendar(expected_start_calendar);
                    real_end_calendar = get_next_day_calendar(expected_end_calendar);
                }


                g_start_pending_intent = set_alarms( g_context, real_start_calendar, BroadcastDefine.OTA_ALARAM_START_ACTION ) ;
                g_end_pending_intent = set_alarms( g_context, real_end_calendar, BroadcastDefine.OTA_ALARAM_END_ACTION ) ;
            }
            else
            {
				Log.d(TAG,"222222222222222222222222");
                stop_all_alarms();
            }
        }
        else if ( g_ota_param.update_mode.equals(CommonDefine.OTA_USER_UPDATE) )
        {
            stop_all_alarms();
            send_ota_broadcast_to_start(false);
        }
    }

    public void init_ota_param()
    {
        String tmp_str = null ;
        g_ota_param = new OTA_PARAM() ;

        tmp_str = CommonFunctionUnit.get_acs_provider_data(g_context, ACSServerJsonFenceName.FwInfo.FILE_SIZE);
        if ( tmp_str != null )
            g_ota_param.file_size = Integer.parseInt(tmp_str);
        g_ota_param.ota_version = CommonFunctionUnit.get_acs_provider_data(g_context, ACSServerJsonFenceName.FwInfo.FW_VERSION);
        g_ota_param.download_url = CommonFunctionUnit.get_acs_provider_data(g_context, ACSServerJsonFenceName.FwInfo.HIGH_BW_URL);
        g_ota_param.md5_checksum = CommonFunctionUnit.get_acs_provider_data(g_context, ACSServerJsonFenceName.FwInfo.MD5_CHECKSUM);
        g_ota_param.model_check_name = CommonFunctionUnit.get_acs_provider_data(g_context, ACSServerJsonFenceName.FwInfo.MODEL_CHECKER);
        g_ota_param.release_note = CommonFunctionUnit.get_acs_provider_data(g_context, ACSServerJsonFenceName.FwInfo.RELEASE_NOTES);
        g_ota_param.update_mode = CommonFunctionUnit.get_acs_provider_data(g_context, ACSServerJsonFenceName.FwInfo.DRIVEN_BY);

        tmp_str = CommonFunctionUnit.get_acs_provider_data(g_context, ACSServerJsonFenceName.FwInfo.STATUS);
        if ( tmp_str != null )
            g_ota_param.http_status = Integer.parseInt(tmp_str);

        tmp_str = CommonFunctionUnit.get_acs_provider_data(g_context, ACSServerJsonFenceName.FwInfo.OTA_AFTER_BOOT);
        if ( tmp_str != null )
            g_ota_param.is_ota_after_boot = Integer.parseInt(tmp_str);

        tmp_str = CommonFunctionUnit.get_acs_provider_data(g_context, ACSServerJsonFenceName.FwInfo.OTA_AFTER_STANDBY);
        if ( tmp_str != null )
            g_ota_param.is_ota_after_standby = Integer.parseInt(tmp_str);

        tmp_str = CommonFunctionUnit.get_acs_provider_data(g_context, ACSServerJsonFenceName.FwInfo.OTA_AFTER_IMMEDIATELY);
        if ( tmp_str != null )
            g_ota_param.is_ota_immediately = Integer.parseInt(tmp_str);

        g_ota_param.is_ota_in_time_window = CommonFunctionUnit.get_acs_provider_data(g_context, ACSServerJsonFenceName.FwInfo.OTA_AFTER_TIME);
 
        if ( SystemProperties.get(CommonDefine.OTA_MODE).equals("download")  
        && SystemProperties.get(CommonDefine.OTA_DOWNLOAD_STATUS).equals(String.valueOf(CommonDefine.DOWNLOAD_COMPLETE)))
            process_ota_param() ;
        else if ( SystemProperties.get(CommonDefine.OTA_MODE).equals("streaming_payload") 
        || SystemProperties.get(CommonDefine.OTA_MODE).equals("streaming_zip") )
            process_ota_param() ;
        else
            Log.e(TAG, "init_ota_param fail to process_ota_param");
/*
        process_ota_download_file() ; */
    }
    public void update_ota_param()
    {
        g_ota_param.file_size = HttpContentValue.FwInfo.file_size ;
        g_ota_param.ota_version = HttpContentValue.FwInfo.fw_version ;
        g_ota_param.download_url = HttpContentValue.FwInfo.FW_DOWNLOAD_PATH.high_bw_url ;
        g_ota_param.md5_checksum = HttpContentValue.FwInfo.md5_checksum ;
        g_ota_param.model_check_name = HttpContentValue.FwInfo.model_checker ;
        g_ota_param.release_note = HttpContentValue.FwInfo.release_notes ;
        g_ota_param.update_mode = HttpContentValue.FwInfo.update_mode ;
        g_ota_param.http_status = HttpContentValue.FwInfo.http_status ;
        g_ota_param.is_ota_after_boot = HttpContentValue.FwInfo.is_ota_after_boot ;
        g_ota_param.is_ota_after_standby = HttpContentValue.FwInfo.is_ota_in_standby ;
        g_ota_param.is_ota_immediately = HttpContentValue.FwInfo.is_ota_immediately ;
        g_ota_param.is_ota_in_time_window = HttpContentValue.FwInfo.is_ota_in_time_window ;

        //stop_all_alarms();
        process_ota();
    }
    public void notify_is_boot_complete()
    {
        if ( g_ota_param == null || g_ota_param.update_mode == null )
        {
            // try to avoid first time execute on boot, data are null
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    notify_is_boot_complete();
                }
            }, 10*CommonDefine.ONE_SECOND);
            return;
        }

        if ( g_ota_param.update_mode.equals(CommonDefine.OTA_FORCE_UPDATE) && g_ota_param.is_ota_after_boot == 1 )
        {
            if ( SystemProperties.get(CommonDefine.OTA_MODE).equals("download")  
            && SystemProperties.get(CommonDefine.OTA_DOWNLOAD_STATUS).equals(String.valueOf(CommonDefine.DOWNLOAD_COMPLETE)))
                send_ota_broadcast_to_start(true);
            else if ( SystemProperties.get(CommonDefine.OTA_MODE).equals("streaming_payload") 
            || SystemProperties.get(CommonDefine.OTA_MODE).equals("streaming_zip") )
                send_ota_broadcast_to_start(true);
            else
                Log.e(TAG, "notify_is_boot_complete fail to send_ota_broadcast_to_start");
        }

        CommonFunctionUnit.update_data_without_notify_launcher( ACSServerJsonFenceName.BoxStatus.INTERACTIVE_CHANGE_TIME, DeviceControlUnit.getCurrentTime() );
    }

    public boolean isValidTimeRange(String input) {
        // 定義正則表達式，包含開頭和結尾的雙引號
        String regex = "^[0-9]{4}-[0-9]{4}$";
		Log.d(TAG, "isValidTimeRange => input = "+input);
        if (!input.matches(regex)) {
			Log.d(TAG,"isValidTimeRange 11111111");
            return false;
        }

        // 去掉雙引號，檢查時間範圍
		String timeRange;
		if(input.charAt(0) == '\"')
        	timeRange = input.substring(1, input.length() - 1);
		else
			timeRange = input;
		//Log.d(TAG, "timeRange = "+timeRange);
        String[] times = timeRange.split("-");
		//Log.d(TAG, "times = "+times[0]+" "+times[1]);
        int start = Integer.parseInt(times[0]);
        int end = Integer.parseInt(times[1]);

        return isValidTime(start) && isValidTime(end);
    }

    // 檢查單一時間是否合法
    private boolean isValidTime(int time) {
        int hours = time / 100;
        int minutes = time % 100;
		Log.d(TAG, "isValidTime => hour = "+hours+" minutes = "+minutes);
        return hours >= 0 && hours <= 23 && minutes >= 0 && minutes < 60;
    }

    private Calendar get_start_calendar( String time_window )
    {
        if ( time_window == null )
        {
            Log.d( TAG, "time window is null !!!!!" ) ;
            return null ;
        }
        Calendar startTime = Calendar.getInstance() ;
        String[] times = time_window.split("-"); // "0100-0500"
        String start = times[0];
        startTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(start.substring(0, 2)));
        startTime.set(Calendar.MINUTE, Integer.parseInt(start.substring(2, 4)));
        startTime.set(Calendar.SECOND, 0);
        startTime.set(Calendar.MILLISECOND, 0);
        return startTime ;
    }

    private Calendar get_end_calendar( String time_window )
    {
        if ( time_window == null )
        {
            Log.d( TAG, "time window is null !!!!!" ) ;
            return null ;
        }
        Calendar endTime = Calendar.getInstance() ;
        String[] times = time_window.split("-"); // "0100-0500"
        String end = times[1];

        endTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(end.substring(0, 2)));
        endTime.set(Calendar.MINUTE, Integer.parseInt(end.substring(2, 4)));
        endTime.set(Calendar.SECOND, 0);
        endTime.set(Calendar.MILLISECOND, 0);
        return endTime ;
    }

    private Calendar get_next_15min_calendar( Calendar today )
    {
        Calendar next_15min_calendar = today ;
        next_15min_calendar.setTimeInMillis(today.getTimeInMillis() + AlarmManager.INTERVAL_FIFTEEN_MINUTES);
        return next_15min_calendar ;
    }
    private Calendar get_next_day_calendar( Calendar today )
    {
        Calendar next_day_calendar = today ;
        next_day_calendar.setTimeInMillis(today.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
        return next_day_calendar ;
    }
    private void stop_all_alarms()
    {
        if ( g_start_pending_intent != null )
            g_start_pending_intent.cancel();
        if ( g_end_pending_intent != null )
            g_end_pending_intent.cancel();
        if ( g_repeating_task_manager != null )
            g_repeating_task_manager.stopRepeatingTask();
    }
    private PendingIntent set_alarms(Context context, Calendar alarm_cal, String intent_action )
    {
        PendingIntent temp_pending_intent = null ;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String startCalFormatted = sdf.format(alarm_cal.getTime());
        Log.d(TAG, "Start time: " + startCalFormatted + " action " + intent_action );

        Intent endIntent = new Intent(intent_action);
        temp_pending_intent = PendingIntent.getBroadcast(context, 0, endIntent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarm_cal.getTimeInMillis(), temp_pending_intent);

        return temp_pending_intent ;
    }

    private void set_zip_download_status(int mode) {
        Log.d(TAG, "set_zip_download_status mode = " + mode);

        if (!SystemProperties.get(CommonDefine.OTA_MODE).equals("download")) {
            Log.d(TAG, "set_zip_download_status: OTA_MODE not download");
            return;
        }

        if (mode == CommonDefine.IDLE)
            SystemProperties.set(CommonDefine.OTA_LAST_DOWNLOAD_VERSION, "null");

        SystemProperties.set(CommonDefine.OTA_DOWNLOAD_STATUS, String.valueOf(mode));
    }

    public void onDestroy() {
        g_context.unregisterReceiver(systemReceiver);
        stop_all_alarms() ;
    }

    public class RepeatingTaskManager {
        private Handler handler = new Handler();
        private Runnable repeatingTask;
        private boolean taskRunning = false;

        public void startRepeatingTask() {
            if (taskRunning) return;

            repeatingTask = new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "RepeatingTaskManager");
                    if ( g_ota_param.update_mode != null && g_ota_param.update_mode.equals(CommonDefine.OTA_FORCE_UPDATE) && g_ota_param.is_ota_in_time_window != null && isValidTimeRange(g_ota_param.is_ota_in_time_window) )
                        send_ota_broadcast_to_start(true);

                    handler.postDelayed(this, AlarmManager.INTERVAL_HALF_HOUR);
                }
            };

            handler.post(repeatingTask);
            taskRunning = true;
        }

        public void stopRepeatingTask() {
            Log.d(TAG, "stopRepeatingTask taskRunning:" + taskRunning);
            if (!taskRunning) return;

            handler.removeCallbacks(repeatingTask);
            taskRunning = false;
        }
    }
}
