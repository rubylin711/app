package com.prime.acsclient.common;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.EthernetManager;
import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.os.ParcelUuid;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.StatFs;
import android.os.SystemProperties;
import android.util.Log;


import androidx.annotation.RequiresApi;

import com.prime.acsclient.ACSService;
import com.prime.acsclient.acsdata.ACSServerJsonFenceName;
import com.prime.acsclient.acsdata.HttpContentValue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import vendor.prime.hardware.media.V1_0.IMedia;
import vendor.prime.hardware.misc.V1_0.IMisc;


public class DeviceControlUnit {
    private static final String TAG = "ACS_DeviceControlUnit" ;
    public static int g_unpairCount = -1;

    public static void hdmi_output_control(int on_off )
    {
        //            if (jSONObject15.has("HdmiOutStatus")) {
//                int i59 = jSONObject15.getInt("HdmiOutStatus");
//                String providerData10 = commFuncs.getProviderData(context, "hdmi_out_status");
//                Log.i("ACSService", "2 tmp_HdmiOutStatus=" + providerData10 + ", new_HdmiOutStatus=" + i59);
//                if (!providerData10.contentEquals(String.valueOf(i59))) {
//                    commFuncs.setProviderData(context, "hdmi_out_status", String.valueOf(i59));
//                    if (i59 == 0) {
//                        commFuncs.setHDMIOut(context, false);
//                    } else {
//                        commFuncs.setHDMIOut(context, true);
//                    }
//                }
//            }
    }

    public static void update_connect_acs_server_time( boolean has_connect )
    {
        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.MqttCommand.PA_ACS_SERVER_IS_CONNECTED, has_connect);
    }
    public static void check_pa_day()
    {
        // coming every one hour
        Boolean is_connected_acs = Boolean.valueOf(CommonFunctionUnit.get_acs_provider_data(ACSService.g_context, ACSServerJsonFenceName.MqttCommand.PA_ACS_SERVER_IS_CONNECTED));
        if ( !is_connected_acs )
        {
            String not_connected_hours_str = CommonFunctionUnit.get_acs_provider_data(ACSService.g_context, ACSServerJsonFenceName.MqttCommand.PA_ACS_SERVER_NO_CONNECTED_HOURS) ;
            int not_conencted_hours = 0 ;
            if ( not_connected_hours_str != null )
                not_conencted_hours = Integer.parseInt(not_connected_hours_str);

            not_conencted_hours++ ;
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.MqttCommand.PA_ACS_SERVER_NO_CONNECTED_HOURS, not_conencted_hours);
            if ( not_conencted_hours >= (HttpContentValue.ProvisionResponse.DeviceSettings.DeviceParams.pa_day*24) ) // days * 24 hours
            {
                CommonFunctionUnit.update_data_and_notify_launcher(ACSServerJsonFenceName.MqttCommand.PA_LOCK, 1, false);
            }
//            Log.d( TAG, "not_conencted_hours = " + not_conencted_hours ) ;
        }
        else
        {
            CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.MqttCommand.PA_ACS_SERVER_NO_CONNECTED_HOURS, 0);
            CommonFunctionUnit.update_data_and_notify_launcher(ACSServerJsonFenceName.MqttCommand.PA_LOCK, 0, false);
        }

//        Log.d( TAG, "is_connected_acs = " + is_connected_acs ) ;
    }

    public static void rms_upload_data()
    {
//        if (jSONObject15.has("RMSStatus")) {
//            int i58 = jSONObject15.getInt("RMSStatus");
//            commFuncs.commonLog(LogLevel.Info, "ACSService", "Get RMSStatus!!!!!!!!!");
//            commFuncs.commonLog(LogLevel.Info, "ACSService", "RMSStatus:" + i58);
//            Config_RMSStatus = commFuncs.getProviderData(context, "rms_status");
//            if (!Config_RMSStatus.contentEquals(String.valueOf(i58))) {
//                commFuncs.setProviderData(context, "rms_client", "1");
//                this.miscHandler.sendMessage(this.miscHandler.obtainMessage(60, true));
//                Config_RMSStatus = String.valueOf(i58);
//                commFuncs.setProviderData(context, "rms_status", Config_RMSStatus);
//            }
//        }
//
//        case 60:
//        commFuncs.commonLog(LogLevel.Info, "ACSService", "==>miscHandler obtain MISC_CMD_UPLOAD_CONFIG message");
//        boolean booleanValue = ((Boolean) message.obj).booleanValue();
//        ACSService.this.miscHandler.removeMessages(60, Boolean.valueOf(booleanValue));
//        if (!ACSService.this.config_first_uploaded) {
//            booleanValue = true;
//        }
//        JSONObject filterConfig = ACSService.this.filterConfig(booleanValue);
//        if (filterConfig == null) {
//            Log.i("ACSService", "retJson is null!");
//            return;
//        } else if (ACSService.this.mMqttAndroidClient == null || !ACSService.this.mMqttAndroidClient.isConnected()) {
//            ACSService.this.miscHandler.sendMessageDelayed(ACSService.this.miscHandler.obtainMessage(60, Boolean.valueOf(booleanValue)), 30000L);
//            return;
//        } else {
//            ACSService.this.config_first_uploaded = true;
//            ACSService.this.mqttHandler.obtainMessage(26, filterConfig.toString()).sendToTarget();
//            return;
//        }
//
//        case 26:
//        Log.i("ACSService", "==>mqttHandler obtain MQTT_PUBLISH message");
//        ACSService.this.publishMessage((String) message.obj);
//        return;
    }

    public static void eth_wifi_auto_switch( int switch_on_off )
    {
        Intent intent = new Intent();
        intent.setClassName("com.android.tv.settings", "com.android.tv.settings.EthernetWifiSwitchService");
        Log.d(TAG, CommonFunctionUnit.getMethodName() + " networkSwitch : " + switch_on_off ) ;
        if ( switch_on_off == 1 )
        {
            ACSService.g_context.startService(intent);
        }
    }

    public static void update_logcat_param()
    {
        ACSService.notify_acsservice( CommonDefine.ACS_LOGCAT_UPDATE_PARAM_CASEID, null, 0, 0 );
    }

    public static File merge_logs_into_file( String merged_file_name, File log_file )
    {
        File mergedFile = new File(log_file.getParent(), merged_file_name);
        if ( mergedFile.exists() )
            mergedFile.delete();
        try {
            FileOutputStream fos = new FileOutputStream(mergedFile);
            for (int i = CommonDefine.LOG_SEPARATE_PART; i >= 0; i--) {
                File partFile ;
                if ( i == 0 )
                    partFile = new File(log_file.getAbsolutePath());
                else
                    partFile = new File(log_file.getAbsolutePath() + (i>=10?"." + String.valueOf(i):".0" + i));

                if (partFile.exists()) {
                    FileInputStream fis = new FileInputStream(partFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                    fis.close();
                }
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mergedFile;
    }

    public static void resubscribe_mqtt( int[] topic_type )
    {
        ACSService.notify_acsservice( CommonDefine.ACS_MQTT_RESUBSCRIBE_CASEID, topic_type, 0, 0);
    }

    public static void install_apk( ArrayList<String> package_name, ArrayList<String> download_path )
    {
        Intent intent = new Intent() ;
        intent.setAction(BroadcastDefine.APP_MANAGER_ACTION) ;
        intent.putExtra(BroadcastDefine.APP_MANAGER_INSTRUCTION_ID, BroadcastDefine.APP_MANAGER_INSTALL_CASE_ID );
        intent.putStringArrayListExtra( BroadcastDefine.APP_MANAGER_INSTALL_PKGNAME, package_name ) ;
        intent.putStringArrayListExtra( BroadcastDefine.APP_MANAGER_INSTALL_PKG_DOWNLOAD_PATH, download_path ) ;
        CommonFunctionUnit.send_broadcast( ACSService.g_context, BroadcastDefine.APP_MANAGER_PACAKGE_NAME, intent );
    }

    public static void uninstall_apk( ArrayList<String> package_name )
    {
        Intent intent = new Intent() ;
        intent.setAction(BroadcastDefine.APP_MANAGER_ACTION) ;
        intent.putExtra(BroadcastDefine.APP_MANAGER_INSTRUCTION_ID, BroadcastDefine.APP_MANAGER_UNINSTALL_CASE_ID );
        intent.putStringArrayListExtra( BroadcastDefine.APP_MANAGER_UNINSTALL_PKGNAME, package_name ) ;
        CommonFunctionUnit.send_broadcast( ACSService.g_context, BroadcastDefine.APP_MANAGER_PACAKGE_NAME, intent );
    }


    /////////// MQTT COMMAND ///////////
    public static void reboot_device()
    {
        PowerManager powerManager = (PowerManager) ACSService.g_context.getSystemService(Context.POWER_SERVICE);
        powerManager.reboot(null);
    }

    public static void do_factory_reset()
    {
        Intent intent = new Intent("android.intent.action.FACTORY_RESET");
        intent.setPackage("android");
        intent.putExtra("android.intent.extra.REASON", "ResetConfirmFragment");
        CommonFunctionUnit.send_broadcast( ACSService.g_context, "android", intent);
    }

    public static void set_parental_lock_password( String new_password )
    {
        CommonFunctionUnit.update_data_and_notify_launcher(ACSServerJsonFenceName.MqttCommand.PARENTAL_LOCK_PASSWORD, new_password, true);
    }

    public static void unpair_bt_device(Context context) {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        g_unpairCount = 0;
        if (defaultAdapter != null) {
            Set<BluetoothDevice> bondedDevices = defaultAdapter.getBondedDevices();
            if (bondedDevices.size() <= 0) {
                Log.d(TAG, "no paired BT RCU.");
                start_bt_pair(context);
                g_unpairCount = -1;
                return;
            }
            for (BluetoothDevice bluetoothDevice : bondedDevices) {
                if (bluetoothDevice == null || bluetoothDevice.getName() == null) {
                    Log.e(TAG, "device is null or device.getName is null");
                } else if (CommonFunctionUnit.is_platform_rcu_name(bluetoothDevice.getName())) {
                    bluetoothDevice.removeBond();
                    g_unpairCount++;
                }
            }
        }
    }

    public static void start_bt_pair(Context context) {
        Log.d(TAG, "start_bt_pair");

        Intent newIntent = new Intent();
        newIntent.setComponent(new ComponentName("com.prime.btpair", "com.prime.btpair.HookBeginActivity"));
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(newIntent);
    }

    public static void clear_widevinecas_provision()
    {
        try {
            IMedia iMedia = IMedia.getService(true) ;
            iMedia.removeWvcasLicense("");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
    public static void initial_app_download_manager()
    {
        Intent intent = new Intent() ;
        intent.setAction(BroadcastDefine.APP_MANAGER_ACTION) ;
        intent.putExtra(BroadcastDefine.APP_MANAGER_INSTRUCTION_ID, BroadcastDefine.APP_MANAGER_REGIST_CALLER_CASE_ID );
        intent.putExtra(BroadcastDefine.APP_MANAGER_RETURN_TARGET_PKGNAME, ACSService.g_context.getPackageName());
        intent.putExtra(BroadcastDefine.APP_MANAGER_RETURN_TARGET_ACTION, "" ) ;
        CommonFunctionUnit.send_broadcast( ACSService.g_context, BroadcastDefine.APP_MANAGER_PACAKGE_NAME, intent );
    }

    public static void prepare_HDMI_info_data()
    {
        String line;
        String disp_cap = "" ;
        String disp_mode = "" ;
        String hdcp_info = "" ;
        String hdmi_info = "" ;
        String hdmi_audio_info ="" ;
        try {
            IMisc misc = IMisc.getService(true) ;
            misc.invoke_cmd(MiscService.MISC_CMD_GET_EDID_INFO, 0, 0) ;
            misc.invoke_cmd(MiscService.MISC_CMD_GET_EDID_AUDIO_INFO, 0, 0) ;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        try {
            BufferedReader rtk_hdmi_reader = new BufferedReader(new InputStreamReader(exec_runtime_command("rtk_hdmi_test -u", true, true )));
            while ((line = rtk_hdmi_reader.readLine()) != null) {
                if ( line.startsWith("VIC:") )
                {
                    disp_cap += line + "\n" ;
                }

                if ( line.startsWith("CRNT_MODE:") )
                {
                    disp_mode = line + "\n" ;
                }
            }

            BufferedReader hdcp_info_reader = new BufferedReader(new InputStreamReader(exec_runtime_command("cat /sys/devices/platform/hdmi/driver/hdmi/hdcp_info", true, true)));
            while ((line = hdcp_info_reader.readLine()) != null) {
                hdcp_info += line + "\n" ;
            }

            BufferedReader hdmi_info_reader = new BufferedReader(new InputStreamReader(exec_runtime_command("cat /data/vendor/dtvdata/edidinfo.txt", true, true)));
            while ((line = hdmi_info_reader.readLine()) != null) {
                hdmi_info += line + "\n" ;
            }

            BufferedReader hdmi_audio_info_reader = new BufferedReader(new InputStreamReader(exec_runtime_command("cat /data/vendor/dtvdata/audioinfo.txt", true, true)));
            while ((line = hdmi_audio_info_reader.readLine()) != null) {
                hdmi_audio_info += line + "\n" ;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.MqttCommand.HDCP_INFO, hdcp_info);
        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.MqttCommand.DISP_CAP, disp_cap);
        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.MqttCommand.DISP_MODE, disp_mode);
        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.MqttCommand.HDCP_USER_SETTING, "N/A");
        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.MqttCommand.EDID, hdmi_info);
        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.MqttCommand.AUD_CAP, hdmi_audio_info);
    }

    private static InputStream exec_runtime_command(String command, boolean is_wait_finish)
    {
        return exec_runtime_command(command, is_wait_finish, false) ;
    }
    private static InputStream exec_runtime_command(String command, boolean is_wait_finish, boolean need_input_stream) {
        Process exec_command_proc = null;
        try {
            exec_command_proc = Runtime.getRuntime().exec(new String[]{"/system/bin/sh", "-c", command});
            if (is_wait_finish)
                exec_command_proc.waitFor();
            if ( need_input_stream )
                return exec_command_proc.getInputStream() ;
        } catch (InterruptedException | IOException e) {
            Log.d(TAG, CommonFunctionUnit.getMethodName() + " caller : " + CommonFunctionUnit.getCallerName() + " error : " + e);
            throw new RuntimeException(e);
        }

        return null ;
    }

    public static void screencapture(String file_path) {
        String command = "screencap -p " + file_path;
        File file = new File(file_path);
        file.delete();
        exec_runtime_command(command, true);
    }

    public static void prepare_instant_monitoring_data()
    {
        String line;
        String cpu_temp = "0.00" ;
        String cpu_usage = "0.00" ;
        String total_mem = "1800" ;
        String used_mem = "0" ;
        String free_mem = "1800" ;

        try {
            // get cpu temp
            BufferedReader cpu_temp_reader = new BufferedReader(new InputStreamReader(exec_runtime_command("cat /sys/class/thermal/thermal_zone0/temp", true, true )));
            while ((line = cpu_temp_reader.readLine()) != null) {
                cpu_temp = new DecimalFormat("#0.00").format(Double.valueOf(line)/1000); ;
            }
            cpu_temp_reader.close();

            // get cpu usage
            BufferedReader cpu_usage_reader = new BufferedReader(new InputStreamReader(exec_runtime_command( "vmstat 1 2", true, true)));
            int target_line_num = 4 ;
            while ((line = cpu_usage_reader.readLine()) != null) {
                target_line_num-- ;
                if (target_line_num == 0) {
                    String[] split = line.split(" ");
                    cpu_usage = new DecimalFormat("#0.00").format(Double.valueOf(100.0d - Double.parseDouble(split[split.length - 3])));
                    break;
                }
            }
            cpu_usage_reader.close();

            // get mem usage
            BufferedReader mem_usage_reader = new BufferedReader(new InputStreamReader(exec_runtime_command("free -m", true, true )));
            while ((line = mem_usage_reader.readLine()) != null) {
                if (line.contains("Mem")) {
                    String[] split = line.split("\\s+");
                    total_mem = split[1] ;
                    used_mem = split[2] ;
                    free_mem = split[3] ;
                }
            }
            mem_usage_reader.close();
        } catch (IOException e) {
            Log.d(TAG, CommonFunctionUnit.getMethodName() + " caller : " + CommonFunctionUnit.getCallerName() + " error : " + e);
            throw new RuntimeException(e);
        }

        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.MqttCommand.CPU_TEMP, cpu_temp);
        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.MqttCommand.CPU_UTILITY, cpu_usage);
        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.MqttCommand.MEM_TOTAL, total_mem);
        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.MqttCommand.MEM_USED, used_mem);
        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.MqttCommand.MEM_FREE, free_mem);
    }

    public static void prepare_box_info(Context context, String file_path) {
        update_stb_bt_paired_info() ;
        File output_file = new File(file_path);
        if (output_file.exists())
            output_file.delete();

        try {
            IMisc misc = IMisc.getService(true) ;
            misc.invoke_cmd(MiscService.MISC_CMD_PRINTKERNEL, 0, 0) ;
            misc.invoke_cmd(MiscService.MISC_CMD_PRINTIFCONFIG, 0, 0) ;
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        exec_runtime_command("echo '\n=== dmesg (equal to or greater than O) ===========================\n' >> " + file_path,true);
        exec_runtime_command( "cat /data/vendor/dtvdata/dmesg.txt >> " + file_path,true);
        exec_runtime_command( "echo '\n=== ps =============================\n' >> " + file_path,true);
        exec_runtime_command( "ps >> " + file_path,true);
        exec_runtime_command( "echo '\n=== meminfo ========================\n' >> " + file_path,true);
        exec_runtime_command( "cat /proc/meminfo >> " + file_path,true);
        exec_runtime_command( "echo '\n=== top ==========================' >> " + file_path,true);
        exec_runtime_command( "top -n 1 >> " + file_path,true);
        exec_runtime_command( "echo '\n=== df ===========================\n' >> " + file_path,true);
        exec_runtime_command( "df >> " + file_path,true);
        exec_runtime_command( "echo '\n=== mount ===========================\n' >> " + file_path,true);
        exec_runtime_command( "mount >> " + file_path,true);
        exec_runtime_command( "echo '\n=== ifconfig ===========================\n' >> " + file_path,true);
        exec_runtime_command( "cat /data/vendor/dtvdata/ifconfig.txt >> " + file_path,true);
        exec_runtime_command( "echo '\n=== Ethernet wire plugged or not ===========================\n' >> " + file_path,true);
        exec_runtime_command( "cat /sys/class/net/eth0/carrier >> " + file_path,true);

        exec_runtime_command( "echo '\n=== getprop ===========================\n' >> " + file_path,true);
        exec_runtime_command( "getprop >> " + file_path,true);
        exec_runtime_command( "echo '\n=== package info ===========================\n' >> " + file_path,true);
        exec_runtime_command( "dumpsys package com.prime.acsclient | grep -A18 \"Package \\[com.prime.acsclient\\]\" >> " + file_path,true);
        exec_runtime_command( "echo '\n=== wifi info ===========================\n' >> " + file_path,true);
        exec_runtime_command( "dumpsys wifi >> " + file_path,true);

        exec_runtime_command( "echo '\n=== paired BT devices ===========================\n' >> " + file_path,true);
        exec_runtime_command( "echo '" + CommonFunctionUnit.get_acs_provider_data(context, ACSServerJsonFenceName.BoxStatus.BT_DEVICES) + "' >> " + file_path,true);
        exec_runtime_command( "echo '\n=== All Apps ===========================\n' >> " + file_path,true);
        exec_runtime_command( "echo '" + CommonFunctionUnit.list_app_packages(context) + "' >> " + file_path,true);
    }

    private static void dumpTombstonesToFile(Context context, String file_path) {
        File outputFile = new File(file_path);
        DropBoxManager dropbox = (DropBoxManager) context.getSystemService(Context.DROPBOX_SERVICE);

        if (dropbox == null) {
            Log.e(TAG, "DropBoxManager is not available");
            return;
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outputFile, true);
            long timestamp = 0;
            DropBoxManager.Entry entry;
            String filelist = "ls " + CommonDefine.ANDROID_DROPBOX_TOMBSTONES_PATH + " | grep SYSTEM_TOMBSTONE@\n" ;
            fos.write(filelist.getBytes());

            while ((entry = dropbox.getNextEntry("SYSTEM_TOMBSTONE", timestamp)) != null) {
//            while ((entry = dropbox.getNextEntry("SYSTEM_TOMBSTONE_PROTO_WITH_HEADERS", timestamp)) != null) {
                String filename = "SYSTEM_TOMBSTONE@" + entry.getTimeMillis() + ".txt.gz\n" ;
                fos.write(filename.getBytes());
                timestamp = entry.getTimeMillis(); // 更新時間戳
                entry.close(); // 關閉條目以釋放資源
            }

            timestamp = 0;
            while ((entry = dropbox.getNextEntry("SYSTEM_TOMBSTONE", timestamp)) != null) {
//            while ((entry = dropbox.getNextEntry("SYSTEM_TOMBSTONE_PROTO_WITH_HEADERS", timestamp)) != null) {
                String fileHeader = "\n\n\n===========================================================\n" +
                        "==== " + CommonDefine.ANDROID_DROPBOX_TOMBSTONES_PATH + "SYSTEM_TOMBSTONE@" + entry.getTimeMillis() + ".txt.gz\n" +
                        "===========================================================\n\n";
                fos.write(fileHeader.getBytes());

                InputStream inputStream = entry.getInputStream();
                if (inputStream != null) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, length);
                    }
                    fos.write("\n\n".getBytes());
                } else {
                    Log.e(TAG, "Failed to get InputStream from DropBox entry");
                }

                timestamp = entry.getTimeMillis();
                entry.close();
            }

            Log.d(TAG, "All tombstone data has been written to " + file_path);
        } catch (IOException e) {
            Log.e(TAG, "Error writing tombstone to file", e);
        } finally {
            // 關閉資源
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing file output stream", e);
            }
        }
    }

    public static void prepare_tombstones(String file_path) {
        File output_file = new File(file_path);
        if (output_file.exists())
            output_file.delete();

        dumpTombstonesToFile(ACSService.g_context, file_path);

//        exec_runtime_command("echo 'ls -al " + CommonDefine.ANDROID_DROPBOX_TOMBSTONES_PATH + "' >> " + file_path,true);
//        exec_runtime_command("ls -al " + CommonDefine.ANDROID_DROPBOX_TOMBSTONES_PATH + " >> " + file_path,true);
//        File[] listFiles = new File(CommonDefine.ANDROID_DROPBOX_TOMBSTONES_PATH).listFiles(new FileFilter() {
//            @Override
//            public boolean accept(File file) {
//                return file.isFile() && (!file.getName().contains(".pb"));
//            }
//        });
//
//        for (File file : listFiles) {
//            Log.d(TAG, "jimtest file = " + file.getAbsolutePath() + " **** " + file.getName());
//            exec_runtime_command("echo '\n\n' >> " + file_path,true);
//            exec_runtime_command("echo '===========================================================' >> " + file_path,true);
//            exec_runtime_command("echo '==== " + file.getAbsolutePath() + "' >> " + file_path, true);
//            exec_runtime_command("echo '===========================================================\n' >> " + file_path,true);
//            exec_runtime_command("cat " + file.getAbsolutePath() + " >> " + file_path,true);
//        }
    }

    public static void prepare_anr( String file_path ) {
        File output_file = new File(file_path);
        if (output_file.exists())
            output_file.delete();

        exec_runtime_command( "echo 'ls -al /data/anr/' >> " + file_path,true);
        exec_runtime_command( "ls -al /data/anr/ >> " + file_path,true);

        File[] listFiles = new File(CommonDefine.ANDROID_ANR_PATH).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile();
            }
        });

        for (File file : listFiles) {
            exec_runtime_command("echo '\n\n' >> " + file_path,true);
            exec_runtime_command("echo '===========================================================' >> " + file_path,true);
            exec_runtime_command("echo '==== " + file.getAbsolutePath() + "' >> " + file_path, true);
            exec_runtime_command("echo '===========================================================\n' >> " + file_path,true);
            exec_runtime_command("cat " + file.getAbsolutePath() + " >> " + file_path,true);
        }
    }

    public static void upload_file_to_acs_server( String upload_url, String file_path )
    {
        Bundle bundle = new Bundle();
        bundle.putString( CommonDefine.HTTP_UPLOAD_URL_KEY, upload_url ) ;
        bundle.putString( CommonDefine.HTTP_UPLOAD_FILE_PATH_KEY, file_path ) ;
        ACSService.notify_acsservice( CommonDefine.ACS_HTTP_UPLOAD_FILE_CASEID, bundle, 0, 0);
    }

    public static void force_tune( int service_id )
    {
        Intent intent = new Intent( BroadcastDefine.MQTT_COMMAND_BROADCAST_ACTION_BASE + MqttCommand.FORCE_TUNE );
        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.MqttCommand.SERVICE_ID, service_id);
        CommonFunctionUnit.send_broadcast(ACSService.g_context, BroadcastDefine.LAUNCHER_PACKAGE_NAME, intent);
    }

    public static void channel_scan( int symbol_rate, int frequency, int qam )
    {
        Intent intent = new Intent( BroadcastDefine.MQTT_COMMAND_BROADCAST_ACTION_BASE + MqttCommand.CHANNEL_SCAN );
        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.MqttCommand.SYMBOL_RATE, symbol_rate);
        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.MqttCommand.FREQ, frequency);
        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.MqttCommand.QAM, qam);
        CommonFunctionUnit.send_broadcast(ACSService.g_context, BroadcastDefine.LAUNCHER_PACKAGE_NAME, intent);
    }

    public static String getCurrentTime() {
        return new SimpleDateFormat("yyyy.MM.dd HH:mm").format(Calendar.getInstance().getTime());
    }

    private static void update_stb_wifi_info() { // wifi
        String bssid = "";
        String result_ssid = "";
        String freq = "";
        String rssi = "";
        ConnectivityManager connectivityManager = ((ConnectivityManager) ACSService.g_context.getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo != null && activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI && activeNetworkInfo.isConnected()) {
            WifiManager wifiManager = (WifiManager) ACSService.g_context.getSystemService(Context.WIFI_SERVICE);
            if (wifiManager.isWifiEnabled()) {
                WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                bssid = connectionInfo.getBSSID();
                String ssid = connectionInfo.getSSID();
                result_ssid = ssid.substring(1, ssid.length() - 1);
                freq = String.valueOf(connectionInfo.getFrequency());
                rssi = String.valueOf(connectionInfo.getRssi());
                Log.d(TAG, "wifi bssid : " + bssid);
                Log.d(TAG, "wifi ssid : " + result_ssid);
                Log.d(TAG, "wifi freq : " + freq);
                Log.d(TAG, "wifi rssi : " + rssi);
            }
        }

        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.BoxStatus.WIFI_BSSID, bssid);
        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.BoxStatus.WIFI_SSID, result_ssid);
        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.BoxStatus.WIFI_FREQ, freq);
        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.BoxStatus.WIFI_RSSI, rssi);
    }

    private static int get_paired_bt_device_battery(Context context, BluetoothDevice device) {
        final int[] electricity = {0};
        final CountDownLatch latch = new CountDownLatch(1);
        BluetoothGatt bluetoothGatt = device.connectGatt(context, true, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                Log.d(TAG, "Connection status:" + status + " state:" + newState);
                if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED) {
                    gatt.discoverServices();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    Log.e(TAG, "Service discovery failure on " + gatt);
//                    return;
                }

                final BluetoothGattService battService = gatt.getService(CommonDefine.GATT_BATTERY_SERVICE_UUID);
                if (battService == null) {
                    Log.d(TAG, "No battery service");
//                    return;
                }

                final BluetoothGattCharacteristic battLevel =
                        battService.getCharacteristic(CommonDefine.GATT_BATTERY_LEVEL_CHARACTERISTIC_UUID);
                if (battLevel == null) {
                    Log.d(TAG, "No battery level");
//                    return;
                }

                gatt.readCharacteristic(battLevel);
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt,
                                             BluetoothGattCharacteristic characteristic, int status) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    Log.e(TAG, "Read characteristic failure on " + gatt + " " + characteristic);
//                    return;
                }

                if (CommonDefine.GATT_BATTERY_LEVEL_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                    final int batteryLevel =
                            characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    Log.d(TAG, "batteryLevel " + batteryLevel);
                    electricity[0] = batteryLevel;
                    latch.countDown();
                }
            }
        });

        try {
            latch.await(2,  TimeUnit.SECONDS);
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return electricity[0];
    }

    public static boolean isRemoteControl (BluetoothDevice device) {
        BluetoothClass bluetoothClass = device.getBluetoothClass();
        if (bluetoothClass.getMajorDeviceClass() == BluetoothClass.Device.Major.PERIPHERAL &&
                BluetoothDevice.DEVICE_TYPE_LE == device.getType() &&
                (((bluetoothClass.getDeviceClass() & CommonDefine.MINOR_DEVICE_CLASS_REMOTE)!= 0)
                        || ((bluetoothClass.getDeviceClass() & CommonDefine.MINOR_DEVICE_CLASS_KEYBOARD)!= 0))) {
//            Log.d(TAG,"isRemoteControl device="+device+" is remote control");
            return true;
        }
        return false;
    }
    private static void update_stb_bt_paired_info()
    {// bt

        JSONArray bt_devices = new JSONArray() ;
        String bt_mac = "" ;
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (defaultAdapter != null) {
            bt_mac = defaultAdapter.getAddress() ;
            Set<BluetoothDevice> bondedDevices = defaultAdapter.getBondedDevices();
            for (BluetoothDevice bluetoothDevice : bondedDevices) {
                if (bluetoothDevice == null || bluetoothDevice.getName() == null || !bluetoothDevice.isConnected() ) {
                    Log.e(TAG, "device is null or device.getName is null");
                }
                else
                {
                    if ( isRemoteControl(bluetoothDevice) ) {
                        JSONObject bt_remote = new JSONObject() ;
                        try {
                            int electricity = get_paired_bt_device_battery(ACSService.g_context, bluetoothDevice);
                            bt_remote.put(ACSServerJsonFenceName.BoxStatus.BT_DEVICES_NAME, bluetoothDevice.getName());
                            bt_remote.put(ACSServerJsonFenceName.BoxStatus.BT_DEVICES_MAC, bluetoothDevice.getAddress());
                            bt_remote.put(ACSServerJsonFenceName.BoxStatus.BT_DEVICES_ECECTRICITY, electricity);
                            bt_devices.put(bt_remote);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }

        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.BoxStatus.BT_DEVICES, bt_devices.toString());
        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.BoxStatus.BT_MAC, bt_mac);
    }

    private static void update_stb_usb_devices_info()
    {
        UsbManager usbManager = (UsbManager) ACSService.g_context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        JSONArray usbDevicesArray = new JSONArray();

        for (UsbDevice device : deviceList.values()) {
            try {
                JSONObject usbDeviceInfo = new JSONObject();
                usbDeviceInfo.put(ACSServerJsonFenceName.BoxStatus.USB_DEVICES_PRODUCT_NAME, device.getProductName());
                usbDeviceInfo.put(ACSServerJsonFenceName.BoxStatus.USB_DEVICES_SERIAL_NUMBER, device.getSerialNumber());
                usbDevicesArray.put(usbDeviceInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.BoxStatus.USB_DEVICES, usbDevicesArray.toString());
    }

    private static void update_stb_public_ip()
    {
        final String[] my_ip = {null};
        if ( my_ip[0] == null )
        {
            final CountDownLatch latch = new CountDownLatch(1);
            PublicIpFetcher.fetchWebIP("https://api.ipify.org", new PublicIpFetcher.IpFetchCallback() {
                @Override
                public void onIpFetched(String service, String ip) {
                    Log.d(TAG, service + " Public IP: " + ip);
                    my_ip[0] = ip;
                    latch.countDown();
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                    latch.countDown();
                }
            });

            try {
                latch.await(3,  TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if ( my_ip[0] == null ) // try another website
        {
            final CountDownLatch latch2 = new CountDownLatch(1);
            PublicIpFetcher.fetchWebIP("https://api.myip.com", new PublicIpFetcher.IpFetchCallback() {
                @Override
                public void onIpFetched(String service, String response) {
                    Log.d(TAG, service + " Response: " + response);
                    if (response != null) {
                        try {
                            JSONObject ip_jsonobj = new JSONObject(response);
                            my_ip[0] = ip_jsonobj.getString("ip");
                            latch2.countDown();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                    latch2.countDown();
                }
            });

            try {
                latch2.await(3,  TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.BoxStatus.PUBLIC_IP, my_ip[0]);
    }
    @RequiresApi(api = Build.VERSION_CODES.R)
    public static void update_stb_network_settings()
    {
        String network_interface = "" ;
        String network_dns = "" ;
        String network_bootproto = "" ;
        ConnectivityManager connectivityManager = (ConnectivityManager) ACSService.g_context.getSystemService(Context.CONNECTIVITY_SERVICE);

        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            LinkProperties linkProperties = connectivityManager.getLinkProperties(activeNetwork);

            if (capabilities != null && linkProperties != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    network_interface = "wifi" ;

                    WifiManager wifiManager = (WifiManager) ACSService.g_context.getSystemService(Context.WIFI_SERVICE);
                    DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();

                    network_dns = intToInetAddress(dhcpInfo.dns1).getHostAddress() ;
                    if ( dhcpInfo.serverAddress != 0 )
                        network_bootproto = "DHCP" ;
                    else
                        network_bootproto = "STATIC" ;
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    network_interface = "eth" ;

                    List<LinkAddress> linkAddresses = linkProperties.getLinkAddresses();
                    List<InetAddress> dnsServers = linkProperties.getDnsServers();

                    for (InetAddress dns : dnsServers) {
                        if ( network_dns == "" )
                            network_dns = dns.getHostAddress();
                    }

                    if ( linkProperties.getDhcpServerAddress() != null )
                        network_bootproto = "DHCP" ;
                    else
                        network_bootproto = "STATIC" ;
                }
            }
        }

        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.BoxStatus.NETWORK_INTERFACE, network_interface);
        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.BoxStatus.NETWORK_DNS, network_dns);
        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.BoxStatus.NETWORK_BOOTPROTO, network_bootproto);
    }

    private static InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = {(byte) (hostAddress & 0xff), (byte) (hostAddress >> 8 & 0xff),
                (byte) (hostAddress >> 16 & 0xff), (byte) (hostAddress >> 24 & 0xff)};
        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError(e);
        }
    }

    private static void update_stb_free_space()
    {
        StatFs stat = new StatFs("/storage/emulated/0/Download");
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        int storage_space = (int) ((availableBlocks * blockSize)/CommonDefine.HTTP_FILE_SIZE_1MB);
        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.BoxStatus.STORAGE_SPACE, storage_space);
    }

    private static void update_stb_lang()
    {
        String displayLanguage = Locale.getDefault().getDisplayLanguage();
        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.BoxStatus.LANG, displayLanguage);
    }

    private static void update_stb_audio_output_type()
    {
        String str;
        str = SystemProperties.get("persist.sys.rtk.audio.hdmioutput", "1");
        String sound_type = "" ;
        if ( str.equals("0") ) { //RAW
            sound_type = "HDMI Pass Through" ;
        }
        else
            sound_type = "PCM" ;

        CommonFunctionUnit.update_data_without_notify_launcher(ACSServerJsonFenceName.BoxStatus.SOUND_TYPE, sound_type);
    }

    public static void update_stb_hwinfo()
    {
        update_stb_wifi_info() ;
        update_stb_bt_paired_info() ;
        update_stb_usb_devices_info() ;
        update_stb_public_ip() ;
        update_stb_network_settings() ;
        update_stb_free_space() ;
        update_stb_lang() ;
        update_stb_audio_output_type() ;
    }
}
