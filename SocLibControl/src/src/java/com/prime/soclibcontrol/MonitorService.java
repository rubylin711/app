package com.prime.soclibcontrol;

import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Handler;
import android.os.HwBinder;
import android.app.Service;
import java.util.HashMap;
import java.util.Iterator;
import java.io.IOException;
import java.util.Arrays;

import com.realtek.hardware.RtkHDMIManager3;
import com.realtek.hardware.RtkHDMIManager3.DrmMode;
import com.realtek.hardware.RtkHDMIManager3.DrmOutputFormat;

public class MonitorService extends Service
{
    private final String TAG = "PrimeSocLibControl" ;
    private Context mContext = null ;
    private HDMIListener mListener = new HDMIListener();
    private RtkHDMIManager3 mRtkHDMIManager3 = null ;


    private final Handler handler = new Handler();


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void onCreate() {
        mContext = this ;
        mRtkHDMIManager3 = RtkHDMIManager3.getHDMIManager() ;
        mRtkHDMIManager3.addListener(mListener);

        ACSDataProviderHelper.set_acs_provider_data( mContext, "screen_resolution", getScreenResolution() ) ;
        ACSDataProviderHelper.set_acs_provider_data( mContext, "tv_model", getTvModelInfo() ) ;
        ACSDataProviderHelper.set_acs_provider_data( mContext, "edid_raw_data", getEdidRawDataString() ) ;
        super.onCreate();
    }

    public String getScreenResolution()
    {
        String screen_resolution = getCurrentResolutionStr() ;
        Log.d( TAG, "screen_resolution " + screen_resolution);
        return screen_resolution ;
    }

    public String getTvModelInfo()
    {
        byte[] edidRawData = mRtkHDMIManager3.getEDIDRawData();
        String manufactureName = HdmiEdidParser.extractManufacturerId(edidRawData);
        int productCode = HdmiEdidParser.extractProductCode(edidRawData);
        String tv_model = "Brand:" + manufactureName + ", Prod:" + Integer.toHexString(productCode) ;
        Log.d( TAG, "tv_model " + tv_model);
        return tv_model ;
    }

    public void onDestroy() {
        mRtkHDMIManager3.removeListener(mListener);
        super.onDestroy();
    }

    private String getCurrentResolutionStr()
    {
        DrmOutputFormat curtFmt = mRtkHDMIManager3.getOutputFormat();
        String str = curtFmt.mMode.mWidth + "x" + curtFmt.mMode.mHeight + (curtFmt.mMode.mInterlaced ? "I" : "P") + " @ " + curtFmt.mMode.mRawFps + "Hz";
        return str ;
    }

    public String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String getEdidRawDataString()
    {
        byte[] byteArray = mRtkHDMIManager3.getEDIDRawData() ;
        if ( byteArray.length>0 )
            return bytesToHex(byteArray);
        return null ;
    }

    public class HDMIListener implements RtkHDMIManager3.HDMIEventListener{
        @Override
        public void onEvent(int event) {
            Log.d(TAG, "onEvent: " + event);
            switch(event){
                case RtkHDMIManager3.EVENT_TV_SYSTEM_CHANGED:
                    ACSDataProviderHelper.set_acs_provider_data( mContext, "screen_resolution", getScreenResolution() ) ;
                    break;
                case RtkHDMIManager3.EVENT_PLUG_OUT:
                    ACSDataProviderHelper.set_acs_provider_data( mContext, "edid_raw_data", getEdidRawDataString() ) ;
                    break;
                case RtkHDMIManager3.EVENT_PLUG_IN:
                case RtkHDMIManager3.EVENT_HDCP_DOWNGRADE:
                case RtkHDMIManager3.EVENT_HDCP_UPGRADE:
                    ACSDataProviderHelper.set_acs_provider_data( mContext, "tv_model", getTvModelInfo() ) ;
                    ACSDataProviderHelper.set_acs_provider_data( mContext, "edid_raw_data", getEdidRawDataString() ) ;
                    break;
            }
        }
    }
}
