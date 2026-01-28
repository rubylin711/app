package com.prime.dtv.service.CommandManager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.prime.dtv.Interface.BaseManager;
import com.prime.dtv.PesiDtvFramework;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.dtv.sysdata.GposInfo;

import java.util.ArrayList;
import java.util.List;

public class CfgCmdManager extends BaseManager {
    private static final String TAG = "CfgCmdManager" ;

    private final DataManager mDataManager;

    public CfgCmdManager(Context context, Handler handler) {
        super(context, TAG, handler, CfgCmdManager.class);

        mDataManager = DataManager.getDataManager(context);
    }
    /*
    Gpos
     */
    public GposInfo GposInfoGet() {
        return mDataManager.getGposInfo();
    }

    public void GposInfoUpdate(GposInfo gPos) {
        mDataManager.updateGposInfo(gPos);
    }

    // TODO: try to find a better way to update, maybe try to use Map
    public void GposInfoUpdateByKeyString(String key, String value) {
        GposInfo gposInfo = mDataManager.getGposInfo();
        switch (key) {
            case GposInfo.GPOS_AUDIO_LANG_SELECT_1:
                gposInfo.setAudioLanguageSelection(0, value);
                break;
            case GposInfo.GPOS_AUDIO_LANG_SELECT_2:
                gposInfo.setAudioLanguageSelection(1, value);
                break;
            case GposInfo.GPOS_SUBTITLE_LANG_SELECT_1:
                gposInfo.setSubtitleLanguageSelection(0, value);
                break;
            case GposInfo.GPOS_SUBTITLE_LANG_SELECT_2:
                gposInfo.setSubtitleLanguageSelection(1, value);
                break;
            case GposInfo.GPOS_ZIPCODE:
                gposInfo.setZipCode(value);
                break;
            case GposInfo.GPOS_HOME_ID:
                gposInfo.setHomeId(value);
                break;
            case GposInfo.GPOS_MOBILE:
                gposInfo.setMobile(value);
                break;
            case GposInfo.GPOS_SO:
                gposInfo.setSo(value);
                break;
            case GposInfo.GPOS_WVCAS_LICENSE_URL:
                gposInfo.setWVCasLicenseURL(value);
                break;
            case GposInfo.GPOS_OTA_MD5:
                gposInfo.setOta_md5(value);
                break;
            default:
                Log.d(TAG, "GposInfoUpdateByKeyString: [ERROR] save gpos member [" + key + "] not support !!");
                return;
        }

        mDataManager.updateGposInfo(gposInfo);
    }

    // TODO: try to find a better way to update, maybe try to use Map
    public void GposInfoUpdateByKeyString(String key,int value) {
        GposInfo gposInfo = mDataManager.getGposInfo();
        switch (key) {
            case GposInfo.GPOS_CUR_CHANNELID:
                gposInfo.setCurChannelId(value);
                break;
            case GposInfo.GPOS_CUR_GROUP_TYPE:
                gposInfo.setCurGroupType(value);
                break;
            case GposInfo.GPOS_PASSWORD_VALUE:
                gposInfo.setPasswordValue(value);
                break;
            case GposInfo.GPOS_WORKER_PASSWORD_VALUE:
                gposInfo.setWorkerPasswordValue(value);
                break;
            case GposInfo.GPOS_PARENTAL_RATE:
                gposInfo.setParentalRate(value);
                break;
            case GposInfo.GPOS_PARENTAL_LOCK_ONOFF:
                gposInfo.setParentalLockOnOff(value);
                break;
            case GposInfo.GPOS_INSTALL_LOCK_ONOFF:
                gposInfo.setInstallLockOnOff(value);
                break;
            case GposInfo.GPOS_BOX_POWER_STATUS:
                gposInfo.setBoxPowerStatus(value);
                break;
            case GposInfo.GPOS_START_ON_CHANNELID:
                gposInfo.setStartOnChannelId(value);
                break;
            case GposInfo.GPOS_START_ON_CHTYPE:
                gposInfo.setStartOnChType(value);
                break;
            case GposInfo.GPOS_VOLUME:
                gposInfo.setVolume(value);
                break;
            case GposInfo.GPOS_AUDIO_TRACK_MODE:
                gposInfo.setAudioTrackMode(value);
                break;
            case GposInfo.GPOS_AUTO_REGION_TIME_OFFSET:
                gposInfo.setAutoRegionTimeOffset(value);
                break;
            case GposInfo.GPOS_REGION_TIME_OFSSET:
                gposInfo.setRegionTimeOffset((float)value);
                break;
            case GposInfo.GPOS_REGION_SUMMER_TIME:
                gposInfo.setRegionSummerTime(value);
                break;
            case GposInfo.GPOS_LNB_POWER:
                gposInfo.setLnbPower(value);
                break;
            case GposInfo.GPOS_SCREEN_16X9:
                gposInfo.setScreen16x9(value);
                break;
            case GposInfo.GPOS_CONVERSION:
                gposInfo.setConversion(value);
                break;
            case GposInfo.GPOS_SEARCH_PROGRAM_TYPE:
                gposInfo.setSearchProgramType(value);
                break;
            case GposInfo.GPOS_SEARCH_MODE:
                gposInfo.setSearchMode(value);
                break;
            case GposInfo.GPOS_SORT_BY_LCN:
                gposInfo.setSortByLcn(value);
                break;
            case GposInfo.GPOS_OSD_TRANSPARENCY:
                gposInfo.setOSDTransparency(value);
                break;
            case GposInfo.GPOS_BANNER_TIMEOUT:
                gposInfo.setBannerTimeout(value);
                break;
            case GposInfo.GPOS_HARD_HEARING:
                gposInfo.setHardHearing(value);
                break;
            case GposInfo.GPOS_AUTO_STANDBY_TIME:
                gposInfo.setAutoStandbyTime(value);
                break;
            case GposInfo.GPOS_DOLBY_MODE:
                gposInfo.setDolbyMode(value);
                break;
            case GposInfo.GPOS_HDCP_ONOFF:
                gposInfo.setHDCPOnOff(value);
                break;
            case GposInfo.GPOS_SUBTITLE_ONOFF:
                gposInfo.setSubtitleOnOff(value);
                break;
            case GposInfo.GPOS_AV_STOP_MODE:
                gposInfo.setAvStopMode(value);
                break;
            case GposInfo.GPOS_TIMESHIFT_DURATION:
                gposInfo.setTimeshiftDuration(value);
                break;
            case GposInfo.GPOS_MAIL_SETTINGS_SHOPPING:
                gposInfo.setMailSettingsShopping(value);
                break;
            case GposInfo.GPOS_MAIL_SETTINGS_NEWS:
                gposInfo.setMailSettingsNews(value);
                break;
            case GposInfo.GPOS_MAIL_SETTINGS_POPULAR:
                gposInfo.setMailSettingsPopular(value);
                break;
            case GposInfo.GPOS_MAIL_SETTINGS_COUPON:
                gposInfo.setMailSettingsCoupon(value);
                break;
            case GposInfo.GPOS_MAIL_SETTINGS_SERVICE:
                gposInfo.setMailSettingsService(value);
                break;
            case GposInfo.GPOS_CHANNEL_LOCK_COUNT:
                gposInfo.setChannelLockCount(value);
                break;
            case GposInfo.GPOS_TIME_LOCK_PERIOD_1_START:
                gposInfo.setTimeLockPeriodStart(0, value);
                break;
            case GposInfo.GPOS_TIME_LOCK_PERIOD_2_START:
                gposInfo.setTimeLockPeriodStart(1, value);
                break;
            case GposInfo.GPOS_TIME_LOCK_PERIOD_3_START:
                gposInfo.setTimeLockPeriodStart(2, value);
                break;
            case GposInfo.GPOS_TIME_LOCK_PERIOD_1_END:
                gposInfo.setTimeLockPeriodEnd(0, value);
                break;
            case GposInfo.GPOS_TIME_LOCK_PERIOD_2_END:
                gposInfo.setTimeLockPeriodEnd(1, value);
                break;
            case GposInfo.GPOS_TIME_LOCK_PERIOD_3_END:
                gposInfo.setTimeLockPeriodEnd(2, value);
                break;
            case GposInfo.GPOS_NIT_VERSION:
                gposInfo.setNitVersion(value);
                break;
            case GposInfo.GPOS_NIT_ID:
                gposInfo.setNitId(value);
                break;
            case GposInfo.GPOS_SI_NIT_ID:
                gposInfo.setSINitNetworkId(value);
                break;
            case GposInfo.GPOS_BAT_VERSION:
                gposInfo.setBatVersion(value);
                break;
            case GposInfo.GPOS_BAT_ID:
                gposInfo.setBatId(value);
                break;
            case GposInfo.GPOS_INTRODUCTION_TIME:
                gposInfo.setIntroductionTime(value);
                break;
            case GposInfo.GPOS_STANDBY_REDIRECT:
                gposInfo.setStandbyRedirect(value);
                break;
            case GposInfo.GPOS_MAIL_NOTIFY_STATUS:
                gposInfo.setMailNotifyStatus(value);
                break;
            case GposInfo.GPOS_WVCAS_REF_CASDATA_TIME:
                gposInfo.setSTBRefCasDataTime(value);
                break;
            case GposInfo.GPOS_DVR_MANAGEMENT_LIMIT:
                gposInfo.setDVRManagementLimit(value);
                break;
            default:
                Log.d(TAG, "GposInfoUpdateByKeyString: [ERROR] save gpos member [" + key + "] not support !!");
                return;
        }

        mDataManager.updateGposInfo(gposInfo);
    }

    /*
    other
    */
    public void EnterViewActivity(int enter) {
        Log.d(TAG, "EnterViewActivity: not implemented");
    }

    public void EnableMemStatusCheck(int enable) {
        Log.d(TAG, "EnableMemStatusCheck: not implemented");
    }

    public String GetPesiServiceVersion() {
        return PesiDtvFramework.PESI_DTV_FRAMEWORK_VERSION;
    }

    public int InvokeTest() {
        Log.d(TAG, "InvokeTest: not implemented");
        return 0;
    }

    /*
   USB
    */
    public List<Integer> GetUsbPortList() {
        Log.d(TAG, "GetUsbPortList: not implemented");
        return new ArrayList<>();
    }

    public void ResetFactoryDefault() {
        mDataManager.DataManagerSaveData(DataManager.SV_RESET_DEFAULT);
    }

    @Override
    public void BaseHandleMessage(Message msg) {

    }

    public void backupDatabase(boolean force) {
        mDataManager.DataManagerBackupDatabase(force);
    }
}
