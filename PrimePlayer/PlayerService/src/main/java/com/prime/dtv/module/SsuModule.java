package com.prime.dtv.module;

import android.os.Parcel;
import android.util.Log;

import com.prime.dtv.PrimeDtvMediaPlayer;
import com.prime.datastructure.sysdata.OTACableParameters;
import com.prime.datastructure.sysdata.OTATerrParameters;

public class SsuModule {
    private static final String TAG = "SsuModule";
    private static final int CMD_SSU_BASE = PrimeDtvMediaPlayer.CMD_Base + 0xB00;

    // SSU cmd
    private static final int CMD_SSU_StartOtaMonitor = CMD_SSU_BASE + 0x01;
    private static final int CMD_SSU_StopOtaMonitor = CMD_SSU_BASE + 0x02;
    private static final int CMD_SSU_StartDownloadOtaFile = CMD_SSU_BASE + 0x03;

    private static final int CMD_SSU_PESI_USB = CMD_SSU_BASE + 0x64;
    private static final int CMD_SSU_PESI_OTA_DVB_C = CMD_SSU_BASE + 0x65;
    private static final int CMD_SSU_PESI_OTA_DVB_S = CMD_SSU_BASE + 0x66;
    private static final int CMD_SSU_PESI_OTA_DVB_T = CMD_SSU_BASE + 0x67;
    private static final int CMD_SSU_PESI_OTA_DVB_T2 = CMD_SSU_BASE + 0x68;
    private static final int CMD_SSU_PESI_IP_1 = CMD_SSU_BASE + 0x69;
    private static final int CMD_SSU_PESI_IP_2 = CMD_SSU_BASE + 0x6A;
    private static final int CMD_SSU_PESI_FS = CMD_SSU_BASE + 0x6B;
    private static final int CMD_SSU_PESI_GET_OTA_PARAMES = CMD_SSU_BASE + 0x6C;
    private static final int CMD_SSU_PESI_OTA_ISDBT = CMD_SSU_BASE + 0x6D;
    private static final int CMD_SSU_PESI_MTEST_OTA = CMD_SSU_BASE + 0x6E;//Scoty 20190410 add Mtest Trigger OTA command
    private static final int CMD_SSU_PESI_MTEST_ENABLE_OPT = CMD_SSU_BASE + 0x6F;


    public int update_usb_software(String filename) {
        Log.d(TAG, "update_usb_software()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_SSU_PESI_USB);
        request.writeString(filename);//(DDN82-3796.bin)

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int update_file_system_software(String pathAndFileName, String partitionName) {
        Log.d(TAG, "update_file_system_software()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_SSU_PESI_FS);
        request.writeString(pathAndFileName);//(DDN82-3796.bin)
        request.writeString(partitionName);//(userdata)

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int update_ota_dvbc_software(int tpId, int freq, int symbol, int qam) {
        Log.d(TAG, "update_ota_dvbc_software()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_SSU_PESI_OTA_DVB_C);
        request.writeInt(tpId);
        request.writeInt(freq * 1000);//Scoty 20180614 modify ota update set freq/symbol/bandwith *1000
        request.writeInt(symbol * 1000);///Scoty 20180614 modify ota update set freq/symbol/bandwith *1000
        request.writeInt(qam);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int update_ota_dvbt_software(int tpId, int freq, int bandwith, int qam, int priority) {
        Log.d(TAG, "update_ota_dvbt_software()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_SSU_PESI_OTA_DVB_T);
        request.writeInt(tpId);
        request.writeInt(freq * 1000);//Scoty 20180614 modify ota update set freq/symbol/bandwith *1000
        request.writeInt(bandwith * 1000);//Scoty 20180614 modify ota update set freq/symbol/bandwith *1000
        request.writeInt(qam);
        request.writeInt(priority);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int update_ota_dvbt2_software(int tpId, int freq, int bandwith, int qam, int channelmode) {
        Log.d(TAG, "update_ota_dvbt2_software()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_SSU_PESI_OTA_DVB_T);
        request.writeInt(tpId);
        request.writeInt(freq * 1000);//Scoty 20180614 modify ota update set freq/symbol/bandwith *1000
        request.writeInt(bandwith * 1000);//Scoty 20180614 modify ota update set freq/symbol/bandwith *1000
        request.writeInt(qam);
        request.writeInt(channelmode);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int update_ota_isdbt_software(int tpId, int freq, int bandwith, int qam, int priority) {
        Log.d(TAG, "update_ota_isdbt_software()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_SSU_PESI_OTA_ISDBT);
        request.writeInt(tpId);
        request.writeInt(freq * 1000);//Scoty 20180614 modify ota update set freq/symbol/bandwith *1000
        request.writeInt(bandwith * 1000);//Scoty 20180614 modify ota update set freq/symbol/bandwith *1000
        request.writeInt(qam);
        request.writeInt(priority);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int update_mtest_ota_software()//Scoty 20190410 add Mtest Trigger OTA command
    {
        Log.d(TAG, "MtestOTAUpdate()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_SSU_PESI_MTEST_OTA);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int mtest_enable_opt(boolean enable) {
        Log.d(TAG, "mtest_enable_opt: " + enable);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_SSU_PESI_MTEST_ENABLE_OPT);
        request.writeInt(enable ? 1 : 0);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);
        request.recycle();
        reply.recycle();

        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public OTACableParameters dvb_get_ota_cable_paras() {
        int result = -1;
        Log.d(TAG, "dvb_get_ota_cable_paras");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_SSU_PESI_GET_OTA_PARAMES);
        request.writeInt(1); //cable
        PrimeDtvMediaPlayer.invokeex(request, reply);
        result = reply.readInt();
        OTACableParameters ota = new OTACableParameters();
        if (result == 0) {
            ota.pid = reply.readInt();
            ota.frequency = reply.readInt() / 1000; //Mhz 100-900
            ota.symbolRate = reply.readInt() / 1000;
            ota.modulation = reply.readInt();
        } else {
            ota.pid = 0;
            ota.frequency = 0; //Mhz 100-900
            ota.symbolRate = 0;
            ota.modulation = 0;
        }
        request.recycle();
        reply.recycle();
        return ota;
    }

    public OTATerrParameters dvb_get_ota_isdbt_paras() {
        int result = -1;
        Log.d(TAG, "dvb_get_ota_isdbt_paras");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_SSU_PESI_GET_OTA_PARAMES);
        request.writeInt(16); //isdbt
        PrimeDtvMediaPlayer.invokeex(request, reply);
        result = reply.readInt();
        Log.d(TAG, "dvb_get_ota_isdbt_paras:    result = " + result);
        OTATerrParameters ota = new OTATerrParameters();
        if (result == 0) {
            ota.pid = reply.readInt();
            ota.frequency = reply.readInt() / 1000; //Mhz 100-900
            ota.bandWidth = reply.readInt() / 1000; //Mhz 6-9
            ota.enDVBTPrio = 0;
            ota.modulation = 0;
            ota.enChannelMode = 0;
            Log.d(TAG, "dvb_get_ota_isdbt_paras:    pid = " + ota.pid + ", frequency = " + ota.frequency + ", bandwidth = " + ota.bandWidth);
        } else {
            ota.pid = 0;
            ota.frequency = 0; //Mhz 100-900
            ota.bandWidth = 0; //Mhz 6-9
            ota.enDVBTPrio = 0;
            ota.modulation = 0;
            ota.enChannelMode = 0;
        }
        request.recycle();
        reply.recycle();
        return ota;
    }


    public OTATerrParameters dvb_get_ota_terrestrial_paras() {
        int result = -1;
        Log.d(TAG, "dvb_get_ota_terrestrial_paras");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_SSU_PESI_GET_OTA_PARAMES);
        request.writeInt(4); //Terrestrial
        PrimeDtvMediaPlayer.invokeex(request, reply);
        result = reply.readInt();
        Log.d(TAG, "dvb_get_ota_terrestrial_paras:    result = " + result);
        OTATerrParameters ota = new OTATerrParameters();
        if (result == 0) {
            ota.pid = reply.readInt();
            ota.frequency = reply.readInt() / 1000; //Mhz 100-900
            ota.bandWidth = reply.readInt() / 1000; //Mhz 6-9
            ota.enDVBTPrio = reply.readInt();
            ota.modulation = reply.readInt();
            ota.enChannelMode = 0;
            Log.d(TAG, "dvb_get_ota_terrestrial_paras:    pid = " + ota.pid + ", frequency = " + ota.frequency + ", bandwidth = " + ota.bandWidth + ",dvbprio = " + ota.enDVBTPrio + "modulation = " + ota.modulation);
        } else {
            ota.pid = 0;
            ota.frequency = 0; //Mhz 100-900
            ota.bandWidth = 0; //Mhz 6-9
            ota.enDVBTPrio = 0;
            ota.modulation = 0;
            ota.enChannelMode = 0;
        }
        request.recycle();
        reply.recycle();
        return ota;
    }

    public OTATerrParameters dvb_get_ota_dvbt2_paras() {
        int result = -1;
        Log.d(TAG, "dvb_get_ota_dvbt2_paras");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_SSU_PESI_GET_OTA_PARAMES);
        request.writeInt(8); //DVB T2
        PrimeDtvMediaPlayer.invokeex(request, reply);
        result = reply.readInt();
        Log.d(TAG, "dvb_get_ota_dvbt2_paras:    result = " + result);
        OTATerrParameters ota = new OTATerrParameters();
        if (result == 0) {
            ota.pid = reply.readInt();
            ota.frequency = reply.readInt() / 1000; //Mhz 100-900
            ota.bandWidth = reply.readInt() / 1000; //Mhz 6-9
            ota.enDVBTPrio = 0;
            ota.enChannelMode = reply.readInt();
            ota.modulation = reply.readInt();
            Log.d(TAG, "dvb_get_ota_dvbt2_paras:    pid = " + ota.pid + ", frequency = " + ota.frequency + ", bandwidth = " + ota.bandWidth + ", channel mode = " + ota.enChannelMode + ", modulation = " + ota.modulation);
        } else {
            ota.pid = 0;
            ota.frequency = 0; //Mhz 100-900
            ota.bandWidth = 0; //Mhz 6-9
            ota.enDVBTPrio = 0;
            ota.enChannelMode = 0;
            ota.modulation = 0;
        }
        request.recycle();
        reply.recycle();
        return ota;
    }
}
