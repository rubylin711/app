package com.prime.dtv.module;

import android.os.Parcel;
import android.util.Log;

import com.prime.dtv.PrimeDtvMediaPlayer;
import com.prime.dtv.sysdata.EnNetworkType;
import com.prime.dtv.sysdata.OTACableParameters;
import com.prime.dtv.sysdata.OTATerrParameters;

public class LoaderDtvModule {
    private static final String TAG = "LoaderDtvModule";

    private static final int CMD_LOADERDTV_BASE = PrimeDtvMediaPlayer.CMD_Base + 0x1500;

    private static final int CMD_LOADERDTV_GET_JTAG = CMD_LOADERDTV_BASE + 0x01;
    private static final int CMD_LOADERDTV_SET_JTAG = CMD_LOADERDTV_BASE + 0x02;
    private static final int CMD_LOADERDTV_CHECK_DSMCCS_SERVICE = CMD_LOADERDTV_BASE + 0x03;
    private static final int CMD_LOADERDTV_GET_CHIPSET_INFO = CMD_LOADERDTV_BASE + 0x04;
    private static final int CMD_LOADERDTV_GET_STBSN = CMD_LOADERDTV_BASE + 0x05;
    private static final int CMD_LOADERDTV_GET_CHIPSET_ID = CMD_LOADERDTV_BASE + 0x06;
    private static final int CMD_LOADERDTV_GET_SWAREVER = CMD_LOADERDTV_BASE + 0x07;


    public int loader_dtv_get_jtag() {
        Log.d(TAG, "loader_dtv_get_jtag");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_LOADERDTV_GET_JTAG);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int boot_fuse = reply.readInt();
        Log.d(TAG, "loader_dtv_get_jtag boot_fuse = " + boot_fuse);
        request.recycle();
        reply.recycle();
        return boot_fuse;
    }

    public int loader_dtv_set_jtag(int value) {
        Log.d(TAG, "loader_dtv_set_jtag");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_LOADERDTV_SET_JTAG);
        request.writeInt(value);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int result = reply.readInt();
        request.recycle();
        reply.recycle();
        return result;
    }

    public int loader_dtv_check_isdbt_service(OTATerrParameters ota) {
        int freq = ota.frequency * 1000 + 143;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        Log.d(TAG, "loader_dtv_check_isdbt_service");
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_LOADERDTV_CHECK_DSMCCS_SERVICE);

        request.writeInt(0); //sat id
        request.writeInt(0); //tuner id
        request.writeInt(EnNetworkType.ISDB_TER.getValue()); //networktype

        request.writeInt(freq);
        request.writeInt(ota.bandWidth);
        request.writeInt(ota.modulation);
        request.writeInt(0); //version
        request.writeInt(ota.pid);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "loader_dtv_check_isdbt_service:    result = " + ret);
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int loader_dtv_check_terrestrial_service(OTATerrParameters ota) {
        int freq = ota.frequency * 1000 + 143;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        Log.d(TAG, "loader_dtv_check_terrestrial_service");
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_LOADERDTV_CHECK_DSMCCS_SERVICE);

        request.writeInt(0); //sat id
        request.writeInt(0); //tuner id
        request.writeInt(EnNetworkType.TERRESTRIAL.getValue()); //networktype

        request.writeInt(freq);
        request.writeInt(ota.bandWidth);
        request.writeInt(ota.modulation);
        request.writeInt(0); //version
        request.writeInt(ota.pid);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "loader_dtv_check_terrestrial_service:    result = " + ret);
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int loader_dtv_check_cable_service(OTACableParameters ota) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_LOADERDTV_CHECK_DSMCCS_SERVICE);

        request.writeInt(0); //sat id
        request.writeInt(0); //tuner id
        request.writeInt(EnNetworkType.CABLE.getValue()); //networktype

        request.writeInt(ota.frequency * 1000);
        request.writeInt(ota.symbolRate);
        request.writeInt(ota.modulation);
        request.writeInt(0); //version
        request.writeInt(ota.pid);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "loader_dtv_check_cable_service:    result = " + ret);
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int loader_dtv_get_stb_sn() {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_LOADERDTV_GET_STBSN);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        int stbsn;
        if (ret == 0)
            stbsn = reply.readInt();
        else
            stbsn = 0x0000000F;
        Log.d(TAG, "loader_dtv_get_stb_sn:    result = " + ret + " stbsn = " + stbsn);
        request.recycle();
        reply.recycle();
        return stbsn;
    }

    public int loader_dtv_get_chipset_id() {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_LOADERDTV_GET_CHIPSET_ID);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        int chip_id;
        if (ret == 0)
            chip_id = reply.readInt();
        else
            chip_id = 0x0000000F;
        Log.d(TAG, "loader_dtv_get_chipset_id:    result = " + ret + " chip_id = " + chip_id);
        request.recycle();
        reply.recycle();
        return chip_id;
    }

    public int Loader_dtv_get_sw_version() {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_LOADERDTV_GET_SWAREVER);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        int sw_ver;
        if (ret == 0)
            sw_ver = reply.readInt();
        else
            sw_ver = 0x0000000F;
        Log.d(TAG, "Loader_dtv_get_sw_version:    result = " + ret + " sw_ver = " + sw_ver);
        request.recycle();
        reply.recycle();
        return sw_ver;
    }
}
