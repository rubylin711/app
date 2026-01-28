package com.prime.dtv.utils;

import android.content.Context;
import android.hardware.hdmi.HdmiControlManager;
import android.os.Build;
import android.os.SystemProperties;
import android.provider.Settings;

import com.prime.datastructure.utils.LogUtils;

public class HdmiCecUtil {
    private static final String TAG = "HdmiCecUtil";

    private HdmiControlManager mHdmiControlManager;
    private static final String NRDP_CEC_SUPPORT = "persist.sys.rtk.nrdp_cec_support";
    public static final String NRDP_VIDEO_PLATFORM_CAP = "nrdp_video_platform_capabilities";
    private static final String PROP_CEC_SUSPEND = "persist.sys.rtk.cec.wakeup";
    Context mContext;

    public HdmiCecUtil(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mHdmiControlManager = context.getSystemService(HdmiControlManager.class);
        }
        mContext = context;
    }

    public void setHdmiCecEnabled(boolean enabled) {
        LogUtils.d("setHdmiCecEnabled : "+enabled+"");
        if (SystemProperties.getBoolean(NRDP_CEC_SUPPORT, true))
            setNrdpStayusChange(enabled);
        else
            Settings.Global.putString(mContext.getContentResolver(),NRDP_VIDEO_PLATFORM_CAP,"{\"false\"}");
        mHdmiControlManager.setHdmiCecEnabled(
                enabled
                        ? HdmiControlManager.HDMI_CEC_CONTROL_ENABLED
                        : HdmiControlManager.HDMI_CEC_CONTROL_DISABLED);
        setCecSuspendMode(enabled);
        String nrdp = Settings.Global.getString(mContext.getContentResolver(),"nrdp_video_platform_capabilities");
        LogUtils.d( "nrdp_video_platform_capabilities : "+nrdp);
    }

    private void setNrdpStayusChange(boolean enabled) {
        Settings.Global.putString(mContext.getContentResolver(), NRDP_VIDEO_PLATFORM_CAP,
                enabled
                        ? "{\"activeCecState\":\"active\"}"
                        : "{\"activeCecState\":\"unknown\"}");
    }

    private void setCecSuspendMode(boolean enabled) {
        SystemProperties.set(PROP_CEC_SUSPEND, enabled ? "1" : "0");
    }
}
