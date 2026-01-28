package com.prime.dtv.module;

import android.os.Parcel;
import android.util.Log;

import com.prime.dtv.PrimeDtvMediaPlayer;
import com.prime.datastructure.sysdata.EnTableType;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.VMXProtectData;

public class ConfigModule {
    private static final String TAG = "ConfigModule";
    private static final int CMD_CFG_Base = PrimeDtvMediaPlayer.CMD_Base + 0x800;

    // Setting command
    private static final int CMD_CFG_GetCountryCode = CMD_CFG_Base + 0x01;
    private static final int CMD_CFG_SetCountryCode = CMD_CFG_Base + 0x02;
    private static final int CMD_CFG_GetAreaCode = CMD_CFG_Base + 0x03;
    private static final int CMD_CFG_SetAreaCode = CMD_CFG_Base + 0x04;
    private static final int CMD_CFG_GetStringValue = CMD_CFG_Base + 0x05;
    private static final int CMD_CFG_SetStringValue = CMD_CFG_Base + 0x06;
    private static final int CMD_CFG_GetIntValue = CMD_CFG_Base + 0x07;
    private static final int CMD_CFG_SetIntValue = CMD_CFG_Base + 0x08;
    private static final int CMD_CFG_GetGpos = CMD_CFG_Base + 50;
    private static final int CMD_CFG_SetGpos = CMD_CFG_Base + 51;
    private static final int CMD_CFG_SetStandbyOnOff = CMD_CFG_Base + 52;
    private static final int CMD_CFG_SetInViewActivity = CMD_CFG_Base + 53;
    private static final int CMD_CFG_EnableMemStatusCheck = CMD_CFG_Base + 54; /// test
    private static final int CMD_CFG_GetProtectData = CMD_CFG_Base + 55;
    private static final int CMD_CFG_SetProtectData = CMD_CFG_Base + 56;
    private static final int CMD_CFG_GetChipID = CMD_CFG_Base + 57;

    // JAVA CMD
    private static final int CMD_JAVA_Base = PrimeDtvMediaPlayer.CMD_JAVA_Base;
    private static final int CMD_CFG_RestoreDefaultConfig = CMD_JAVA_Base + CMD_CFG_Base + 0x01;//TODO
    private static final int CMD_CFG_GetConfigFileIntValue = CMD_JAVA_Base + CMD_CFG_Base + 0x02;//TODO

    private static final String TAG_CFG_PVR_PATH = "PesiPvrRecPath";
    private static final String DEFAULT_REC_PATH = "/mnt/sdcard";

    public String get_default_rec_path() {
        return DEFAULT_REC_PATH;
    }


    public String get_record_path() {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_CFG_GetStringValue);
        request.writeString(TAG_CFG_PVR_PATH);
        request.writeString(DEFAULT_REC_PATH);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        String path = null;
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            path = reply.readString(); //select usb mount path
        }

        if (path == null || path.equals("")) {
            request.writeInt(CMD_CFG_SetStringValue);
            request.writeString(TAG_CFG_PVR_PATH);
            request.writeString(DEFAULT_REC_PATH);
            PrimeDtvMediaPlayer.invokeex(request, reply);

            PrimeDtvMediaPlayer dtv = PrimeDtvMediaPlayer.get_instance();
            dtv.save_table(EnTableType.GPOS); // connie 20180530 for save record path
            path = DEFAULT_REC_PATH;
        }

        request.recycle();
        reply.recycle();

        return path;
    }

    public void set_record_path(String path) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_CFG_SetStringValue);
        request.writeString(TAG_CFG_PVR_PATH);
        request.writeString(path);

        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();

        request.recycle();
        reply.recycle();
    }

    public GposInfo gpos_info_get() {
        //tony maybe delete, update by content resolver
        /*int ret;
        GposInfo gposInfo = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_CFG_GetGpos);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        if (ret == PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS) {
            gposInfo = new GposInfo();
            gposInfo.setDBVersion(reply.readString());
            gposInfo.setCurChannelId(PrimeDtvMediaPlayer.get_unsigned_int(reply.readInt()));
            gposInfo.setCurGroupType(reply.readInt());
            gposInfo.setPasswordValue(reply.readInt());
            gposInfo.setParentalRate(reply.readInt());
            gposInfo.setParentalLockOnOff(reply.readInt());
            gposInfo.setInstallLockOnOff(reply.readInt());
            gposInfo.setBoxPowerStatus(reply.readInt());
            gposInfo.setStartOnChannelId(PrimeDtvMediaPlayer.get_unsigned_int(reply.readInt()));
            gposInfo.setStartOnChType(reply.readInt());
            gposInfo.setVolume(reply.readInt());
            gposInfo.setAudioTrackMode(reply.readInt());
            gposInfo.setAutoRegionTimeOffset(reply.readInt());
            float RegionTimeOffset = (float) (reply.readInt() / 2);
            gposInfo.setRegionTimeOffset(RegionTimeOffset);
            gposInfo.setRegionSummerTime(reply.readInt());
            gposInfo.setLnbPower(reply.readInt());
            gposInfo.setScreen16x9(reply.readInt());
            gposInfo.setConversion(reply.readInt());
            int resolution = reply.readInt();
            gposInfo.setOSDLanguage(reply.readString());
            gposInfo.setSearchProgramType(reply.readInt());
            gposInfo.setSearchMode(reply.readInt());
            gposInfo.setAudioLanguageSelection(0, reply.readString());
            gposInfo.setAudioLanguageSelection(1, reply.readString());
            gposInfo.setSubtitleLanguageSelection(0, reply.readString());
            gposInfo.setSubtitleLanguageSelection(1, reply.readString());
            gposInfo.setSortByLcn(reply.readInt());
            gposInfo.setOSDTransparency(reply.readInt());
            gposInfo.setBannerTimeout(reply.readInt());
            gposInfo.setHardHearing(reply.readInt());
            gposInfo.setAutoStandbyTime(reply.readInt());
            gposInfo.setDolbyMode(reply.readInt());
            gposInfo.setHDCPOnOff(reply.readInt());
            gposInfo.setDeepSleepMode(reply.readInt());
            gposInfo.setSubtitleOnOff(reply.readInt());
            gposInfo.setAvStopMode(reply.readInt());
            gposInfo.setTimeshiftDuration(reply.readInt());
            gposInfo.setRecordIconOnOff(reply.readInt());
        }
        request.recycle();
        reply.recycle();
        return gposInfo;*/
        return new GposInfo();
    }

    public void gpos_info_update(GposInfo gPos) {
        int ret;
        //tony maybe delete, update by content resolver
        /*Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_CFG_SetGpos);
        request.writeString(gPos.getDBVersion());
        request.writeInt((int) gPos.getCurChannelId());
        request.writeInt(gPos.getCurGroupType());
        request.writeInt(gPos.getPasswordValue());
        request.writeInt(gPos.getParentalRate());
        request.writeInt(gPos.getParentalLockOnOff());
        request.writeInt(gPos.getInstallLockOnOff());
        request.writeInt(gPos.getBoxPowerStatus());
        request.writeInt((int) gPos.getStartOnChannelId());
        request.writeInt(gPos.getStartOnChType());
        request.writeInt(gPos.getVolume());
        request.writeInt(gPos.getAudioTrackMode());
        request.writeInt(gPos.getAutoRegionTimeOffset());
        int RegionTimeOffset = (int) (gPos.getRegionTimeOffset() * 2);
        request.writeInt(RegionTimeOffset);
        request.writeInt(gPos.getRegionSummerTime());
        request.writeInt(gPos.getLnbPower());
        request.writeInt(gPos.getScreen16x9());
        request.writeInt(gPos.getConversion());
        request.writeInt(gPos.getResolution());
        request.writeString(gPos.getOSDLanguage());
        request.writeInt(gPos.getSearchProgramType());
        request.writeInt(gPos.getSearchMode());
        request.writeString(gPos.getAudioLanguageSelection(0));
        request.writeString(gPos.getAudioLanguageSelection(1));
        request.writeString(gPos.getSubtitleLanguageSelection(0));
        request.writeString(gPos.getSubtitleLanguageSelection(1));
        request.writeInt(gPos.getSortByLcn());
        request.writeInt(gPos.getOSDTransparency());
        request.writeInt(gPos.getBannerTimeout());
        request.writeInt(gPos.getHardHearing());
        request.writeInt(gPos.getAutoStandbyTime());
        request.writeInt(gPos.getDolbyMode());
        request.writeInt(gPos.getHDCPOnOff());
        request.writeInt(gPos.getDeepSleepMode());
        request.writeInt(gPos.getSubtitleOnOff());
        request.writeInt(gPos.getAvStopMode());
        request.writeInt(gPos.getTimeshiftDuration());
        request.writeInt(gPos.getRecordIconOnOff());
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();*/
    }


    public void gpos_info_update_by_key_string(String key, String value) {
        Log.d(TAG, "gpos_info_update_by_key_string key [" + key + "] value[" + value + "]");
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_CFG_SetStringValue);
        request.writeString(key);
        request.writeString(value);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
    }

    public void gpos_info_update_by_key_string(String key, int value) {
        Log.d(TAG, "gpos_info_update_by_key_string key [" + key + "] value[" + value + "]");
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_CFG_SetIntValue);
        request.writeString(key);
        request.writeInt(value);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
    }

    public int set_standby_on_off(int onOff) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_CFG_SetStandbyOnOff);
        request.writeInt(onOff);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int enter_view_activity(int enter) {
        Log.d(TAG, "EnterViewActivity:    enter = " + enter);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_CFG_SetInViewActivity);
        request.writeInt(enter);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public int enable_mem_status_check(int enable) {
//        Log.d(TAG, "EnableMemStatusCheck:    enable = " + enable);
//        Parcel request = Parcel.obtain();
//        Parcel reply = Parcel.obtain();
//        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
//        request.writeInt(CMD_CFG_EnableMemStatusCheck);
//        request.writeInt(enable);
//        PrimeDtvMediaPlayer.invokeex(request, reply);
//        int ret = reply.readInt();
//        request.recycle();
//        reply.recycle();
        return 0;
    }

    public VMXProtectData get_protect_data() {
        Log.d(TAG, "get_protect_data: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_CFG_GetProtectData);
        PrimeDtvMediaPlayer.invokeex(request, reply);

        int ret = 0;//reply.readInt();
        VMXProtectData data = new VMXProtectData();
        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == ret) {
            Log.d(TAG, "get_protect_data ok");
            data.setBlockAllChannel(reply.readInt());
            data.setLocationFirst(reply.readInt());
            data.setLocationSecond(reply.readInt());
            data.setLocationThird(reply.readInt());
            data.setLocationVersion(reply.readInt());

            data.setGroupM(reply.readInt());
            data.setGroupID(reply.readInt());
            data.SetE16Top(reply.readString());
            data.SetE16Bot(reply.readString());
            data.SetEWBS0Top(reply.readString());
            data.SetEWBS1Top(reply.readString());
            data.SetEWBS0Bot(reply.readString());
            data.SetEWBS1Bot(reply.readString());
            data.SetVirtualNum(reply.readString());//Scoty 20181225 add virtual num
        }
        request.recycle();
        reply.recycle();
        return data;
    }

    public int set_protect_data(int first, int second, int third) {
        Log.d(TAG, "set_protect_data: first =" + first + "      second =" + second + "       third =" + third);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_CFG_SetProtectData);
        request.writeInt(first);
        request.writeInt(second);
        request.writeInt(third);
        PrimeDtvMediaPlayer.invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return PrimeDtvMediaPlayer.get_return_value(ret);
    }

    public String get_chip_id() {
        int[] chipID = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
        StringBuilder strBuilderChipID = new StringBuilder();
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(PrimeDtvMediaPlayer.DTV_INTERFACE_NAME);
        request.writeInt(CMD_CFG_GetChipID);

        PrimeDtvMediaPlayer.invokeex(request, reply);

        if (PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS == reply.readInt()) {
            Log.d(TAG, "get_chip_id: success");
            chipID[0] = reply.readInt();
            chipID[1] = reply.readInt();
            chipID[2] = reply.readInt();
            chipID[3] = reply.readInt();
            chipID[4] = reply.readInt();
            chipID[5] = reply.readInt();
            chipID[6] = reply.readInt();
            chipID[7] = reply.readInt();

            boolean ignore = true;
            for (int i = 0; i < chipID.length; i++) {
                if (ignore && i < 4 && chipID[i] == 0) { // try to ignore pre 4 ch if it is 0
                    continue;
                }

                strBuilderChipID.append(String.format("%02X", chipID[i]));
                ignore = false;
            }
        }
        request.recycle();
        reply.recycle();
        Log.d(TAG, "get_chip_id:  = " + strBuilderChipID.toString());
        return strBuilderChipID.toString();
    }

    public int reset_factory_default() {
        Log.d(TAG, "reset_factory_default");
        return PrimeDtvMediaPlayer.excute_command(CMD_CFG_RestoreDefaultConfig);
    }
}
