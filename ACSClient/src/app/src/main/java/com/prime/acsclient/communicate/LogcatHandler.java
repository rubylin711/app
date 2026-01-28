package com.prime.acsclient.communicate;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.prime.acsclient.ACSService;
import com.prime.acsclient.acsdata.ACSServerJsonFenceName;
import com.prime.acsclient.acsdata.HttpContentValue;
import com.prime.acsclient.common.CommonDefine;
import com.prime.acsclient.common.CommonFunctionUnit;
import com.prime.acsclient.common.DeviceControlUnit;

import java.io.File;
import java.io.IOException;

public class LogcatHandler extends Handler {
    private final static String TAG = "ACS_LogcatHandler" ;
    private static HandlerThread g_logcat_HandlerThread = null;
    private String g_upload_log_url = null ;
    private int g_log_size = 100 ;
    private boolean g_enable_log = false ;
    private boolean g_auto_log = false ;
    private Thread g_log_thread = null ;
    private Process g_logcat_process = null;
    private String LOGCAT_FILE_NAME = "logcat.txt" ;
    private String MERGED_LOGCAT_FILE_NAME = "merged_logcat.txt" ;
    private File g_log_save_file_handle = new File(ACSService.g_context.getApplicationContext().getExternalFilesDir(null) + "/" + LOGCAT_FILE_NAME);
    public LogcatHandler(Looper looper) {
        super(looper);
    }

    public static LogcatHandler createHandler() {
        Log.d( TAG, "createHandler") ;
        g_logcat_HandlerThread = new HandlerThread("logcat_HandlerThread");
        g_logcat_HandlerThread.start();
        return new LogcatHandler(g_logcat_HandlerThread.getLooper());
    }

    @Override
    public void handleMessage(Message message)
    {
        switch (message.what) {
            case CommonDefine.ACS_LOGCAT_INIT_CASEID: {
                String tmp_str = null ;
                g_upload_log_url = CommonFunctionUnit.get_acs_provider_data(ACSService.g_context, ACSServerJsonFenceName.ProvisionResponse.LOG_UPLOAD_URL);
                tmp_str = CommonFunctionUnit.get_acs_provider_data(ACSService.g_context, ACSServerJsonFenceName.ProvisionResponse.IS_SAVE_LOG_ENABLE);
                if ( tmp_str != null )
                    g_enable_log = Boolean.valueOf(tmp_str);

                tmp_str = CommonFunctionUnit.get_acs_provider_data(ACSService.g_context, ACSServerJsonFenceName.ProvisionResponse.AUTO_LOG);
                if ( tmp_str != null )
                    g_auto_log = Boolean.valueOf(tmp_str);

                tmp_str = CommonFunctionUnit.get_acs_provider_data(ACSService.g_context, ACSServerJsonFenceName.ProvisionResponse.SAVE_LOG_SIZE_MB);
                if ( tmp_str != null )
                    g_log_size = Integer.parseInt(tmp_str);

                create_log_thread() ;
            }break;
            case CommonDefine.ACS_LOGCAT_DELETE_CASEID: {
                // delete logcat
            }break;
            case CommonDefine.ACS_LOGCAT_CAPTURE_CASEID: {
                // capture log
            }break;
            case CommonDefine.ACS_LOGCAT_UPDATE_PARAM_CASEID:
            {
                boolean have_to_restart_log = false ;
                if ( g_log_size != HttpContentValue.ProvisionResponse.save_log_size_mb )
                {
                    have_to_restart_log = true ;
                }

                g_auto_log = HttpContentValue.ProvisionResponse.auto_log ;
                g_enable_log = HttpContentValue.ProvisionResponse.is_save_log_enable ;
                g_log_size = HttpContentValue.ProvisionResponse.save_log_size_mb ;
                g_upload_log_url = HttpContentValue.ProvisionResponse.log_upload_url ;
                if ( ! g_enable_log || have_to_restart_log )
                {
                    if (g_logcat_process != null)
                    {
                        g_logcat_process.destroy();
                        g_logcat_process = null;
                        Log.d(TAG, "logcat process terminated 111");
                    }
                }
            }break;
            case CommonDefine.ACS_LOGCAT_UPLOAD_CASEID:
            {
                boolean pre_enable_log = g_enable_log ;
                g_enable_log = false ;
                File merged_log = DeviceControlUnit.merge_logs_into_file(MERGED_LOGCAT_FILE_NAME, g_log_save_file_handle) ;
                String upload_url = HttpContentValue.ProvisionResponse.log_upload_url + "?mac=" + HttpContentValue.ProvisionDeviceInfo.eth_mac_address + "&type=log" ;
                DeviceControlUnit.upload_file_to_acs_server(upload_url, String.valueOf(merged_log.getAbsoluteFile()));
                g_enable_log = pre_enable_log ;
            }break;
        }
    }

    private void create_log_thread()
    {
        g_log_thread = new Thread(() -> {
            while (true) {
                try {
                    if (g_enable_log)
                    {
                        String[] command = new String[]{
                                "logcat",
                                "-v", "time",
                                "-r", g_log_size>=10?Integer.toString((g_log_size/10)*1024):"10240",
                                "-n", String.valueOf(CommonDefine.LOG_SEPARATE_PART),
                                "-f", g_log_save_file_handle.getAbsolutePath()
                        };

                        Log.d( TAG, "logcat process start" ) ;
                        g_logcat_process = Runtime.getRuntime().exec(command);
                        g_logcat_process.waitFor();
                    } else
                    {
                        if (g_logcat_process != null) {
                            g_logcat_process.destroy();
                            g_logcat_process = null;
                            Log.d(TAG, "logcat process terminated 222");
                        }
                    }

                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        g_log_thread.start();
    }
}
