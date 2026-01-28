package com.prime.acsclient;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.prime.acsclient.acsdata.ACSServerJsonFenceName;
import com.prime.acsclient.acsdata.HttpContentValue;
import com.prime.acsclient.acsdata.ProcessACSData;
import com.prime.acsclient.common.BroadcastDefine;
import com.prime.acsclient.common.CommonDefine;
import com.prime.acsclient.common.CommonFunctionUnit;
import com.prime.acsclient.common.DeviceControlUnit;
import com.prime.acsclient.common.DeviceInfoUtil;
import com.prime.acsclient.common.MqttCommand;
import com.prime.acsclient.communicate.HttpHandler;
import com.prime.acsclient.communicate.LogcatHandler;
import com.prime.acsclient.communicate.MqttHandler;
import com.prime.acsclient.communicate.SystemStatusMonitor;
import com.prime.acsclient.prodiver.ACSDatabase;

import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ACSService extends Service {
    private final String TAG = "ACS_ACSService" ;
    public static Context g_context = null ;
    public HttpHandler g_http_handler = null ;
    public MqttHandler g_mqtt_handler = null ;
    public LogcatHandler g_logcat_handler = null ;
    public SystemStatusMonitor g_system_status_monitor = null ;
    private static String LOCAL_BROADCAST_ACTION = "LOCAL_BROADCAST_ACTION" ;
    private static String MESSAGE_BUNDLE_STR = "Message_Bundle" ;
    private static String MESSAGE_OBJ_STR = "message_obj" ;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d( TAG, "onBind" ) ;
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d( TAG, "onCreate" ) ;
        g_context = getApplicationContext() ;
        // TGW SAMPLE
//        HttpContentValue.ProvisionDeviceInfo.set_provision_device_info(
//                "202B20060814",
//                "3ab0b7d49e98ca08",
//                "tb02a-r35-20240403",
//                "TATV-8000",
//                "",
//                "11",
//                "0CCF892B2F53",
//                "222050001690");
        // TBC SAMPLE
//        HttpContentValue.ProvisionDeviceInfo.set_provision_device_info(
//                "5CFB3A8C282E",
//                "3ab0b7d49e98ca08",
//                "tb02a-r35-20240403",
//                "TATV-8000",
//                "",
//                "11",
//                "0CCF892B2F53",
//                "221040002768");
        HttpContentValue.ProvisionDeviceInfo.set_provision_device_info(
//                "D4C8B0BAC76B",
                DeviceInfoUtil.get_instance().get_eth_mac(),
                DeviceInfoUtil.get_instance().get_android_id(),
                DeviceInfoUtil.get_instance().get_fw_version(),
                DeviceInfoUtil.get_instance().get_model_name(),
//                "TATV-8000",
                DeviceInfoUtil.get_instance().get_sub_model_name(),
                DeviceInfoUtil.get_instance().get_android_version(),
                DeviceInfoUtil.get_instance().get_wifi_mac(),
                DeviceInfoUtil.get_instance().get_serial_number());
                DeviceControlUnit.update_connect_acs_server_time(false);
        { // normal flow
            // g_system_status_monitor = new SystemStatusMonitor(this);
            // g_http_handler = HttpHandler.createHandler();
            // g_http_handler.sendEmptyMessage(CommonDefine.ACS_HTTP_PROVISION_CASEID);
            // g_http_handler.sendEmptyMessage(CommonDefine.ACS_HTTP_CHECK_NETWORK_CONNECT_CASEID);
            // g_http_handler.sendEmptyMessage(CommonDefine.ACS_HTTP_CHECK_PA_ACS_SERVER_STATUS);

            // g_mqtt_handler = MqttHandler.createHandler();
            // g_mqtt_handler.sendEmptyMessage(CommonDefine.ACS_MQTT_EVERY_DAY_CHECKING_CASEID);

            // g_logcat_handler = LogcatHandler.createHandler();
            // g_logcat_handler.sendEmptyMessage(CommonDefine.ACS_LOGCAT_INIT_CASEID);
        }
//        { // mqtt develop
//            g_mqtt_handler = MqttHandler.createHandler();
//            g_mqtt_handler.sendEmptyMessage(CommonDefine.ACS_MQTT_SERVER_CONNECT_CASEID);
//
//        }
//        { // prebuilt db for launcher debug
//            try {
//                CommonFunctionUnit.print_all_acs_provider_data(this);
//                copyDatabase();
//                CommonFunctionUnit.print_all_acs_provider_data(this);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }

        LocalBroadcastManager.getInstance(this).registerReceiver(g_localbroadcast_receiver, new IntentFilter(LOCAL_BROADCAST_ACTION));

        IntentFilter intentFilter = new IntentFilter();
        // intentFilter.addAction(BroadcastDefine.ACS_USER_FINISH_SETUPWIZARD);
        // intentFilter.addAction(BroadcastDefine.ACS_USER_AGREE_EULA);
        // intentFilter.addAction(BroadcastDefine.ACS_RENEW_DEVICE_COMMON_DATA);
        // intentFilter.addAction(BroadcastDefine.ACS_RENEW_FW_INFO);
        // intentFilter.addAction(BroadcastDefine.ACS_NETWORK_SPEED_TEST_DONE);
        // intentFilter.addAction(BroadcastDefine.ACS_SIGNAL_DETECT_DONE);
        // intentFilter.addAction(BroadcastDefine.MQTT_COMMAND_DEVELOP_TEST);
//         registerReceiver(new BroadcastReceiver() {
//             @Override
//             public void onReceive(Context context, Intent intent) {
//                 String action = intent.getAction();
//                 if ( action.equals(BroadcastDefine.ACS_USER_AGREE_EULA) || action.equals(BroadcastDefine.ACS_USER_FINISH_SETUPWIZARD) ) {
//                     notify_acsservice(CommonDefine.ACS_MQTT_INNER_TRIGGER_BOX_STATUS_CASEID, null, 0, 0);
//                 }
//                 else if ( action.equals(BroadcastDefine.ACS_RENEW_DEVICE_COMMON_DATA) )
//                 {
//                     notify_acsservice(CommonDefine.ACS_HTTP_RENEW_DEVICE_COMMON_CASEID, null, 0, 0);
//                 }
//                 else if ( action.equals(BroadcastDefine.ACS_RENEW_FW_INFO) )
//                 {
//                     notify_acsservice(CommonDefine.ACS_HTTP_FWINFO_CASEID, null, 0, 0);
//                 }
//                 else if ( action.equals(BroadcastDefine.ACS_SIGNAL_DETECT_DONE) )
//                 {
//                     ProcessACSData.process_all_data(ProcessACSData.MQTT_PUSLISH_SIGNAL_DETECT);
//                 }
//                 else if ( action.equals(BroadcastDefine.MQTT_COMMAND_DEVELOP_TEST) )
//                 {
//                     /*
//                     test command :
// am broadcast -a "com.prime.acsclient.mqtt_dev_test" --es data '{"task_id": 2509177, "command": "reboot", "args": ""}'
// am broadcast -a "com.prime.acsclient.mqtt_dev_test" --es data '{"task_id": 2509177, "command": "getinfo", "args": "https://acs-portal.tbcnet.net.tw/device/setinfo"}'
// am broadcast -a "com.prime.acsclient.mqtt_dev_test" --es data '{"task_id": 2509177, "command": "screencapture", "args": "https://acs-portal.tbcnet.net.tw/device/setcapture"}'
// am broadcast -a "com.prime.acsclient.mqtt_dev_test" --es data '{"task_id": 2509177, "command": "btunpair", "args": ""}'
// am broadcast -a "com.prime.acsclient.mqtt_dev_test" --es data '{"task_id": 2509177, "command": "set_parental_lock", "args": "0000"}'
// am broadcast -a "com.prime.acsclient.mqtt_dev_test" --es data '{"task_id": 2509177, "command": "doFactoryReset", "args": ""}'
// am broadcast -a "com.prime.acsclient.mqtt_dev_test" --es data '{"task_id": 2509177, "command": "get_HDMI_Info", "args": ""}'
// am broadcast -a "com.prime.acsclient.mqtt_dev_test" --es data '{"task_id": 2509177, "command": "instantMonitoring", "args": 10}'
// am broadcast -a "com.prime.acsclient.mqtt_dev_test" --es data '{"task_id": 2509177, "command": "force_tune", "args": "4503"}'
// am broadcast -a "com.prime.acsclient.mqtt_dev_test" --es data '{"task_id": 2509177, "command": "channel_scan", "args": "{\"symbol_rate\":5200,\"frequency\":303,\"qam\":256}"},'
// am broadcast -a "com.prime.acsclient.mqtt_dev_test" --es data '{"task_id": 2509177, "command": "open_browser", "args": "https://tw.yahoo.com"}'
// am broadcast -a "com.prime.acsclient.mqtt_dev_test" --es data '{"task_id": 2509177, "command": "clear_provision", "args": ""}'
// am broadcast -a "com.prime.acsclient.mqtt_dev_test" --es data '{"task_id": 2509177, "command": "network_speed_test", "args": ""}'
// am broadcast -a "com.prime.acsclient.mqtt_dev_test" --es data '{"task_id": 2509177, "command": "signal_detect", "args": ""}'
// am broadcast -a "com.prime.acsclient.mqtt_dev_test" --es data '{"task_id": 2509177, "command": "pa_reset", "args": ""}'
// am broadcast -a "com.prime.acsclient.mqtt_dev_test" --es data '{"task_id": 2509177, "command": "status_update", "args": ""}'
//                      */
//                     String json_str = intent.getStringExtra("data");
//                     notify_acsservice(CommonDefine.ACS_MQTT_DEVELOP_TEST_CASEID, json_str, 0, 0);
//                 }
//             }
//         }, intentFilter, Context.RECEIVER_EXPORTED) ;
    }

    BroadcastReceiver g_localbroadcast_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ( intent.getAction().equals(LOCAL_BROADCAST_ACTION) )
            {
                Bundle message_bundle = intent.getBundleExtra(MESSAGE_BUNDLE_STR);
                if ( message_bundle != null )
                {
                    Message message = (Message) message_bundle.get(MESSAGE_OBJ_STR);
                    Log.d(TAG, "ACSService internal message case : " + message.what);
                    if ((message.what & CommonDefine.ACS_HTTP_BASE_CASEID) == CommonDefine.ACS_HTTP_BASE_CASEID) {
                        g_http_handler.removeMessages(message.what);
                        g_http_handler.sendMessage(message);
                    } else if ((message.what & CommonDefine.ACS_MQTT_BASE_CASEID) == CommonDefine.ACS_MQTT_BASE_CASEID) {
                        g_mqtt_handler.removeMessages(message.what);
                        g_mqtt_handler.sendMessage(message);
                    } else if ((message.what & CommonDefine.ACS_LOGCAT_BASE_CASEID) == CommonDefine.ACS_LOGCAT_BASE_CASEID) {
                        g_logcat_handler.removeMessages(message.what);
                        g_logcat_handler.sendMessage(message);
                    } else if ((message.what & CommonDefine.ACS_OTA_BASE_CASEID) == CommonDefine.ACS_OTA_BASE_CASEID) {
                        if ( message.what == CommonDefine.ACS_OTA_UPDATE_PARAM )
                            g_system_status_monitor.update_ota_param();
                    }
                }
            }
        }
    };

    private void copyDatabase() throws IOException {
        InputStream input = g_context.getAssets().open("acs_provider_db.db");
        String outFileName = g_context.getApplicationInfo().dataDir + "/databases/acs_provider_db.db" ;
        OutputStream output = new FileOutputStream(outFileName);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }

        output.flush();
        output.close();
        input.close();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // if (intent != null) {
        //     boolean is_boot_complete = intent.getBooleanExtra(CommonDefine.IS_BOOT_COMPLETE, false);
        //     if ( is_boot_complete && g_system_status_monitor != null )
        //         g_system_status_monitor.notify_is_boot_complete();
        // }
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // g_system_status_monitor.onDestroy();
        // g_mqtt_handler.sendEmptyMessage(CommonDefine.ACS_MQTT_SERVER_DISCONNECT_CASEID);
        // LocalBroadcastManager.getInstance(this).unregisterReceiver(g_localbroadcast_receiver);
    }

    public static void notify_acsservice( int msg_case_id, Object obj, int param1, int param2 )
    {
        Message new_msg = Message.obtain();
        new_msg.what = msg_case_id ;
        new_msg.obj = obj ;
        new_msg.arg1 = param1 ;
        new_msg.arg2 = param2 ;

        Bundle bundle = new Bundle() ;
        bundle.putParcelable(MESSAGE_OBJ_STR, new_msg);
        Intent intent = new Intent(LOCAL_BROADCAST_ACTION);
        intent.putExtra(MESSAGE_BUNDLE_STR, bundle);
        LocalBroadcastManager.getInstance(g_context).sendBroadcast(intent);
    }

    private void registerLinkStatusReceiver(Context context) {
        Log.d(TAG,"registerLinkStatusReceiver");
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(mLinkStatusReceiver, filter);
    }

    private void unregisterLinkStatusReceiver(Context context) {
        Log.d(TAG,"unregisterLinkStatusReceiver");
        context.unregisterReceiver(mLinkStatusReceiver);
    }

    private final BroadcastReceiver mLinkStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"mLinkStatusReceiver g_unpairCount = "+DeviceControlUnit.g_unpairCount);
            if(DeviceControlUnit.g_unpairCount == -1)
                return;

            Log.d(TAG,"mLinkStatusReceiver onReceive intent="+intent);
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (!CommonFunctionUnit.is_platform_rcu_name(device.getName())) {
                Log.d(TAG,"mLinkStatusReceiver is not platform rcu name");
                return;
            }

            if (true) {
                Log.d(TAG, "There was a link status change for: " + device.getAddress());
            }

            int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,
                    BluetoothDevice.BOND_NONE);
            int previousBondState = intent.getIntExtra(
                    BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE);

            if (true) {
                Log.d(TAG, "Bond states: old = " + previousBondState + ", new = " +
                    bondState);
            }

            if (bondState == BluetoothDevice.BOND_NONE &&
                    previousBondState == BluetoothDevice.BOND_BONDED) {
                // we seem to have reverted, this is an error
                // TODO inform user, start scanning again
                Log.d(TAG, "Bond states: BOND_BONDED to BOND_NONE");
                Log.d(TAG, "Bond states: name = " + device.getName());
                DeviceControlUnit.g_unpairCount--;
                if (DeviceControlUnit.g_unpairCount == 0) {
                    DeviceControlUnit.start_bt_pair(context);
                    DeviceControlUnit.g_unpairCount = -1;   
                }    
            } 
        }
    };
}