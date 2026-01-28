package com.prime.acsclient.common;

import android.content.ContentResolver;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;

import com.prime.acsclient.ACSService;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class DeviceInfoUtil {
    private static final String TAG = "ACS_DeviceInfoUtil" ;
    private static volatile DeviceInfoUtil g_instance = null ;
    private DeviceInfoUtil()
    {
        Log.d( TAG, "DeviceInfoUtil" ) ;
    }
    public static DeviceInfoUtil get_instance()
    {
        if ( g_instance == null )
        {
            synchronized (DeviceInfoUtil.class)
            {
                if (g_instance == null)
                {
                    g_instance = new DeviceInfoUtil();
                    Log.d( TAG, "new DeviceInfoUtil" ) ;
                }
            }
        }

        return g_instance ;
    }

    private String get_mac( String interface_name )
    {
        String rsp = "000000000000";
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (nif.getDisplayName().equalsIgnoreCase(interface_name)) {
                    byte[] macBytes = nif.getHardwareAddress();
                    nif.getInetAddresses();
                    if (macBytes != null) {
                        StringBuilder res1 = new StringBuilder();
                        for (byte b : macBytes) {
                            res1.append(String.format("%02X", Byte.valueOf(b)));
                        }

                        rsp = res1.toString();
                    }
                    Log.d(TAG, "NET INTERFACE INFO -  " + nif.getName() + " MAC: " + rsp);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d(TAG, "SOME ERROR ON READING NETWORK INTERFACE: " + ex.toString());
        }
        return rsp ;
    }

    public String get_eth_mac()
    {
        return get_mac( "eth0" ) ;
    }
    public String get_android_id()
    {
        ContentResolver contentResolver = ACSService.g_context.getContentResolver();
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
    }

    public String get_fw_version()
    {
		String fw_version;// = Build.MODEL+"-"+SystemProperties.get("ro.firmwareVersion").replace(".", "")+"-20"+Build.VERSION.INCREMENTAL.substring(0, Build.VERSION.INCREMENTAL.length() - 6);
		String m = Build.PRODUCT;
		String v = SystemProperties.get("ro.firmwareVersion").replace(".", "");
		String t;
		if(Build.VERSION.INCREMENTAL.length() > 6){
			t = "20"+Build.VERSION.INCREMENTAL.substring(0, Build.VERSION.INCREMENTAL.length() - 6);
		}
		else{
			t = "20"+Build.VERSION.INCREMENTAL;
		}
		if(SystemProperties.getBoolean("persist.sys.prime.fake_version", false))
			fw_version = "DADA_1319D-1210-20250501";
		else
			fw_version = m+"-"+v+"-"+t;
		Log.d(TAG, "fw_version = "+fw_version);
        return fw_version;
    }

    public String get_model_name()
    {
        Boolean fake = SystemProperties.getBoolean("persist.sys.prime.acs.fake_model_name", false);
        String model_name = fake?"TATV-8000":Build.MODEL;
		Log.d(TAG, "model_name = "+model_name);
        return model_name ;
    }

    public String get_sub_model_name()
    {
        return new String() ;
    }

    public String get_android_version()
    {
        return Build.VERSION.RELEASE ;
    }

    public String get_wifi_mac()
    {
        return get_mac( "wlan0" ) ;
    }

    public String get_serial_number()
    {
        return Build.getSerial() ;
    }
}
