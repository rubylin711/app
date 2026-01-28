package com.prime.dtv.module;

import android.os.Parcel;
import android.util.Log;
import android.view.Surface;

import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.PrimeDtvMediaPlayer;

public class CommonModule {
    private static final String TAG = "CommonModule";

    public static final int CMD_Common_Base = PrimeDtvMediaPlayer.CMD_Base + 0x100;

    // Common Command
    private static final int CMD_COMM_PrepareDTV = CMD_Common_Base + 0x01;
    private static final int CMD_COMM_UnPrepareDTV = CMD_Common_Base + 0x02;
    private static final int CMD_COMM_SetSourceType = CMD_Common_Base + 0x03;
    private static final int CMD_COMM_GetSourceType = CMD_Common_Base + 0x04;
    private static final int CMD_COMM_GetLastDtvSourceType = CMD_Common_Base + 0x05;
    private static final int CMD_COMM_GetSupportSourceType = CMD_Common_Base + 0x06;
    private static final int CMD_COMM_FactoryReset = CMD_Common_Base + 0x07;
    private static final int CMD_COMM_GetStackVersion = CMD_Common_Base + 0x08;
    private static final int CMD_COMM_SetModuleType = CMD_Common_Base + 0x09;
    private static final int CMD_COMM_GetPesiServiceVersion = CMD_Common_Base + 101;
    private static final int CMD_COMM_TestInvoke = CMD_Common_Base + 102;

    // JAVA CMD
    // JAVA Common
    private static final int CMD_JAVA_Base = PrimeDtvMediaPlayer.CMD_JAVA_Base;
    private static final int CMD_COMM_SetSurface = CMD_JAVA_Base + CMD_Common_Base + 0x01;
    private static final int CMD_COMM_SetDisplay = CMD_JAVA_Base + CMD_Common_Base + 0x02;
    private static final int CMD_COMM_GetWindHandle = CMD_JAVA_Base + CMD_Common_Base + 0x03;
    private static final int CMD_COMM_ClearDisplay = CMD_JAVA_Base + CMD_Common_Base + 0x04;
    private static final int CMD_COMM_GetTimeshiftWindHandle = CMD_JAVA_Base + CMD_Common_Base + 0x05;

    private int g_platform = 0;
    private int g_tunerNum = 0;
    private int g_tunerType = 0;

    public int get_platform() {
        return g_platform;
    }

    public int get_tuner_num() {
        return g_tunerNum;
    }

    public int get_tuner_type() {
        return g_tunerType;
    }

    public int prepare_dtv() {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_COMM_PrepareDTV);
        Log.d(TAG, "prepare_dtv PLATFORM_PESI");
        request.writeInt(1);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            g_platform = reply.readInt();
            Log.d(TAG, "prepare_dtv g_platform = " + g_platform);
            g_tunerNum = reply.readInt();
            Log.d(TAG, "prepare_dtv g_tunerNum = " + g_tunerNum);
            for (int i = 0; i < g_tunerNum; i++) {
                g_tunerType = reply.readInt();
                Log.d(TAG, "prepare_dtv g_tunerType = " + g_tunerType);
                break;
            }
        }

        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public String get_pesi_service_version() {
        int ret;
        String version = "";
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_COMM_GetPesiServiceVersion);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        if (ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            version = reply.readString();
            Log.d(TAG, "get_pesi_service_version = " + version);
        }
        request.recycle();
        reply.recycle();
        return version;
    }

    public int invoke_test() {
        int result = -1;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_COMM_TestInvoke);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        if (ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            String interfaceName = reply.readString();
            Log.d(TAG, "invoke_test:    result = " + ret + " interfaceName = " + interfaceName);
            if (interfaceName != null && interfaceName.equals(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME))
                result = 0;
        }

        request.recycle();
        reply.recycle();
        return result;
    }

    public int comm_get_wind_handle() {
        Log.d(TAG, "comm_get_wind_handle");
        int hwind = 0;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_COMM_GetWindHandle);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        hwind = reply.readInt();
        request.recycle();
        reply.recycle();

        return hwind;
    }

    public int comm_get_timeshift_wind_handle() // Edwin 20181123 to get time shift window handle
    {
        Log.d(TAG, "comm_get_timeshift_wind_handle");
        int hwind = 0;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_COMM_GetTimeshiftWindHandle);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        hwind = reply.readInt();
        request.recycle();
        reply.recycle();

        return hwind;
    }

    private int set_surface(Surface surface) {

        Log.d(TAG, "set_surface in");
        int ret = 0;
//          {
//          //    Log.d(TAG, "dtv no longer need  setSurface");
//              Log.d(TAG, "setSurface in");
//
//              Parcel request = Parcel.obtain();
//              Parcel reply = Parcel.obtain();
//              request.writeString(DTV_INTERFACE_NAME);
//              if (null != surface)
//                  {
//                      Log.d(TAG, "setSurface");
//                      request.writeInt(CMD_COMM_SetSurface);
//                      surface.writeToParcel(request, 0);
//                      invokeex(request, reply);
//                      ret = reply.readInt();
//                  }
//
//              request.recycle();
//              reply.recycle();
//
//              //return getReturnValue(ret);
//          }
        /*
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        if (null != surface)
        {
            Log.d(TAG, "setSurface");
            request.writeInt(CMD_SetSurface);
            surface.writeToParcel(request, 1);
            invokeex(request, reply);
            ret = reply.readInt();
            request.recycle();
            reply.recycle();
        }
        else
        {
            Log.d(TAG, "createSurface");
            request.writeInt(CMD_CreateSurface);
            invokeex(request, reply);
            ret = reply.readInt();
            request.recycle();
            reply.recycle();
        }
        */
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int set_module_type(String type) {
        int result = -1;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        String module_type = type;
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_COMM_SetModuleType);
        request.writeString(module_type);
        LogUtils.d("set_module_type = "+module_type);

        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = reply.readInt();
        if (ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            result = 0;
        }
        return result;
    }
}
