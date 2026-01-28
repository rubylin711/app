package com.prime.dtv.config;

import android.os.SystemProperties;

import java.net.NetworkInterface;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

/** @noinspection BooleanMethodIsAlwaysInverted*/
public class Pvcfg {
    /************ Define **************/
    //define ca type
    public static final int CA_NONE = 0x0;
    public static final int CA_CONAX =0x1;
    public static final int CA_SC = 0x2;
    public static final int CA_IRDETO = 0x04;
    public static final int CA_CLOAK = 0x08;
    public static final int CA_VMX = 0x10;
    public static final int CA_WIDEVINE_CAS = 0x20;//eric lin 20210107 widevine cas
    /************ Config **************/
    public static final int UIMODEL_3796 = 3796;
    public static final int UIMODEL_3798 = 3798;
    public static final int UI_TYPE = 3;//Scoty 20180912 add UI type cfg
    public static final int UI_MODEL = UIMODEL_3796;//Scoty 20181024 add UI Model cfg
    public static final int PESI_CA_TYPE = CA_NONE; //eric lin 20210107 widevine cas
    private static boolean PVR_PJ = false;//PVR_PJ = 1
    public static final int PROJECT_HOME_TP_TUNER_ID = 3;
    public static int NUM_OF_RECORDING = 2;
    public static int PVR_TIMESHIFTPBUFF_4K = 30;//30 min
    public static int PVR_TIMESHIFTPBUFF_HD = 120;//120 min
    public static int PVR_TIMESHIFTPBUFF_SD = 720;//720 min
    public static int PVR_HDD_LIMIT_SIZE = 250;//250 mb
    public static int PVR_HDD_CRITICAL_SIZE = 50; // 50 mb
    public static boolean PVR_TIMESHIFT_FORWARD = false;
    public static boolean PVR_TIMESHIFT_REWIND = false;
    public static boolean PVR_RECORD_MUSIC = true;
    private static final String PERSIST_TIMESHIFTPBUFF_4K = "persist.sys.prime.timshfit_buff.4k";
    private static final String PERSIST_TIMESHIFTPBUFF_HD = "persist.sys.prime.timshfit_buff.hd";
    private static final String PERSIST_TIMESHIFTPBUFF_SD = "persist.sys.prime.timshfit_buff.sd";
    private static final String PERSIST_TIMESHIFT_FORWARD = "persist.sys.prime.timeshift_forward";
    private static final String PERSIST_TIMESHIFT_REWIND = "persist.sys.prime.timeshift_rewind";
    private static final String PERSIST_RECORD_MUSIC = "persist.sys.prime.record_music";
    private static final String PERSIST_PVR_HDD_LIMIT_SIZE = "persist.sys.prime.pvr_hdd_limit_size";
    private static final String PERSIST_PVR_HDD_CRITICAL_SIZE = "persist.sys.prime.pvr_hdd_critical_size";
    private static final String PERSIST_PVRPJ = "persist.sys.prime.pvr_pj";
    public static final boolean PIP = true;
    public static final boolean RANKING = false;
    public static boolean ENABLE_NETWORK_PROGRAM = false;//Enable Youtube & Vod Streams to Programs
    // if true, simple channel channel number = lcn, false = display number
    private static final boolean USE_LCN = true;
    private static boolean FCC_ENABLE = true;
    private static final String PERSIST_FCC = "persist.sys.prime.fcc";
    private static boolean FCC_V2_ENABLE = true;
    private static final String PERSIST_FCC_V2 = "persist.sys.prime.fcc_v2";
    private static boolean FCC_V3_ENABLE = false;
    private static final String PERSIST_FCC_V3 = "persist.sys.prime.fcc_v3";
    private static boolean PMTUPDATE_ENABLE = true;
    private static final String PERSIST_PMT_UPDATE_ENABLE = "persist.sys.prime.pmt_update";
    private static boolean SIUPDATE_ENABLE = true;
    private static final String PERSIST_SI_UPDATE_ENABLE = "persist.sys.prime.si_update";


    // alti license server
    public static String DMG_DR_SERVER = "https://124.109.125.60:12100";
    public static String DMG_LABDR_SERVER = "https://124.109.125.59:12100";
    public static String TBC_DR_SERVER = "https://116.241.97.225:12100";
    private static final String PERSIST_USE_LABDR = "persist.sys.prime.use_labdr";
    //public static final String WVCAS_LICENSE_URL_TBC = "https://116.241.97.225:12100/fortress/wvcas/license";
    //public static final String WVCAS_LICENSE_URL_DMG = "https://116.241.97.225:12100/fortress/wvcas/license";
    // google license server
//    public static final String WVCAS_LICENSE_URL = "https://proxy.uat.widevine.com/proxy";
    //public static final String WVCAS_ENTITLEMENT_URL = "https://116.241.97.225:12100/fortress/wvcas/entitlements";
    private static int ScanOneTP = 0;

    public static int BatId;
    private static final String PERSIST_BAT_ID = "persist.sys.prime.bat_id";

    public static final int MODULE_DMG = 0;
    public static final int MODULE_TBC = 1;
    private static int mModuleType = MODULE_DMG;
    private static final String PERSIST_MODULE_TYPE = "persist.sys.prime.module_type";

    public static final int ALTIMEDIA_CA_SYSTEM_ID = 7687;
    public static final int WVCAS_CA_SYSTEM_ID = 19156;

    private static boolean TRY_LICENSE_ENTITLED_ONLY = false;
    private static final boolean mMultiAudio_Enable = false;
    private static final String PERSIST_MULTI_AUDIO = "persist.sys.prime.multiaudio";

    public static final int SYNC_MODE_PCR = 0;
    public static final int SYNC_MODE_AUDIO = 1;

    private static int mSyncMode = SYNC_MODE_AUDIO;
    private static final String PERSIST_SYNC_MODE = "persist.sys.prime.avsync_mode";

    private static boolean mProcessECM = true;
    private static final String PERSIST_ECM_ENABLE = "persist.sys.prime.ecm_enable";
    private static boolean mProcessEMM = true;
    private static final String PERSIST_EMM_ENABLE = "persist.sys.prime.emm_enable";
    private static boolean mEnableEIT_PF = true;
    private static final String PERSIST_EIT_PF_ON = "persist.sys.prime.enable_epg_pf";

    private static boolean mEnableEIT_Scheduler = true;
    private static final String PERSIST_EIT_SCHEDULER_ENABLE = "persist.sys.prime.enable_epg_scheduler";
    private static boolean mCheckSectionComplete = false;
    private static final String PERSIST_CHECK_SECTION_COMPLETED = "persist.sys.prime.check_section_completed";
    private static boolean mUseCasn = true;
    private static final String PERSIST_USE_CASN = "persist.sys.prime.use_casn";
    private static boolean mLiADFeature = true;
    private static boolean mLiADDebug = false;
    private static final String PERSIST_LIAD_FEATURE = "persist.sys.prime.liad_feature";
    private static final String PERSIST_LIAD_DEBUG = "persist.sys.prime.liad_debug";
    private static final String PERSIST_CHECK_AD_TIME = "persist.sys.prime.check_ad_time";
    private static int mCheckADTime = 3;// AM 3:00  => 0:00~5:59

    public static final int SERIES_EXPIRE_WEEKS = 4;
    public static final int SERIES_CHECK_MINUTE = 1;

    private static boolean mCheckIllegalNetwork = true;
    private static final String PERSIST_CHECK_ILLEGAL_NETWORK = "persist.sys.prime.check_illegal_network";

    private static int mE213CheckTime = 5;//5 sec
    private static final String PERSIST_E213_CHECK_TIME = "persist.sys.prime.check_e213_time";

    private static boolean mEnableEPGEventMap = false;
    private static final String PERSIST_ENABLE_EPG_EVENT_MAP = "persist.sys.prime.enable_epg_event_map";

    private static boolean mFixedModuelType = false;
    private static final String PERSIST_FIXED_MODULE_TYPE = "persist.sys.prime.fixed_module_type";
    private static boolean mWaitSurfaceReady = true;
    private static final String PERSIST_WAIT_SURFACE_READY = "persist.sys.prime.wait_surface_ready";
    private static boolean mAVPlayRunOnUIThread = false;
    private static final String PERSIST_AV_ON_UI = "persist.sys.prime.av_on_ui";
    private static boolean mRunRefreshCasByTime = true;
    private static final String PERSIST_RUN_REFRESHCAS_BY_TIME = "persist.sys.prime.run_refreshcas_by_time";
    private static int mFixedRefreshTime = 0;
    private static final String PERSIST_FIXED_REFRESH_TIME = "persist.sys.prime.fixd_refreshcas_time";
    private static boolean mUpdateTdt = true;
    private static final String PERSIST_UPDATE_RF_TDT = "persist.sys.prime.update_rf_tdt";

    public static boolean getMultiAudio_Enable(){
        boolean isEnable = SystemProperties.getBoolean(PERSIST_MULTI_AUDIO,mMultiAudio_Enable);
        return isEnable;
    }
    public static boolean getPVR_PJ(){
        boolean isEnable = SystemProperties.getBoolean(PERSIST_PVRPJ,PVR_PJ);
        return isEnable;
    }

    public static void setPVR_PJ(boolean pvr_pj){
        SystemProperties.set(PERSIST_PVRPJ, String.valueOf(pvr_pj));
        PVR_PJ = pvr_pj;
    }

    public static boolean getPIP(){
        return PIP;
    }
    public static int getUIType(){//Scoty 20180912 add UI type cfg
        return UI_TYPE;
    }
    public static int getUIModel(){//Scoty 20181024 add UI Model cfg
        return UI_MODEL;
    }
    public static int getCAType() // connie 20181107 for get CA Type
    {
        return PESI_CA_TYPE;
    }
    public static boolean IsEnableNetworkPrograms()//Enable Youtube & Vod Streams to Programs
    {
        return ENABLE_NETWORK_PROGRAM;
    }
    public static void SetEnableNetworkPrograms(boolean enable)//Enable Youtube & Vod Streams to Programs
    {
        ENABLE_NETWORK_PROGRAM = enable;
    }

    public static boolean channelNumberUseLcn() {
        return USE_LCN;
    }

    public static void setBatId(int batId) {
        SystemProperties.set(PERSIST_BAT_ID, String.valueOf(batId));
         BatId=batId;
    }
    public static int getBatId() {
        int value = SystemProperties.getInt(PERSIST_BAT_ID, BatId);
        return value;
    }


    // johnny 20240801 the default charset to decode dvb text bytes
    // if first byte does not contain character coding
    // see also com.prime.dtv.service.Util.DVBString#toString(Charset)
    public static Charset getDefaultCharset() {
        if(getModuleType() == MODULE_DMG)
            return Charset.forName("Big5");
        else
            return Charset.forName("Utf-8");
    }

    public static boolean isFccEnable() {
        boolean isEnable = SystemProperties.getBoolean(PERSIST_FCC,FCC_ENABLE);
        return isEnable;
    }

    public static void setFccEnable(boolean enable) {
        FCC_ENABLE = enable;
        SystemProperties.set(PERSIST_FCC, String.valueOf(enable));
    }

    public static int getScanOneTP() {
        return ScanOneTP;
    }

    public static void setScanOneTP(int scanOneTP) {
        ScanOneTP = scanOneTP;
    }

    public static int getModuleType() {
        int value = SystemProperties.getInt(PERSIST_MODULE_TYPE, mModuleType);
        return value;
    }

    public static void setModuleType(int mModuleType) {
		if(isFixedModuelType() == false){
			Pvcfg.mModuleType = mModuleType;
			SystemProperties.set(PERSIST_MODULE_TYPE, String.valueOf(mModuleType));
		}
    }

    public static boolean isTryLicenseEntitledOnly() {
        return TRY_LICENSE_ENTITLED_ONLY;
    }

    public static void setTryLicenseEntitledOnly(boolean enable) {
        TRY_LICENSE_ENTITLED_ONLY = enable;
    }

    public static int getSyncMode() {
        int sync_mod = SystemProperties.getInt(PERSIST_SYNC_MODE, mSyncMode);
        return sync_mod;
    }

    public static void setSyncMode(int SyncMode) {
        SystemProperties.set(PERSIST_SYNC_MODE, Integer.toString(SyncMode));
        Pvcfg.mSyncMode = SyncMode;
    }

    public static boolean isProcessECM() {
        boolean value = SystemProperties.getBoolean(PERSIST_ECM_ENABLE,mProcessECM);
        return value;
    }

    public static void setmProcessECM(boolean processECM) {
        SystemProperties.set(PERSIST_ECM_ENABLE, String.valueOf(processECM));
        Pvcfg.mProcessECM = processECM;
    }

    public static boolean isEnableEIT_PF() {
        boolean value = SystemProperties.getBoolean(PERSIST_EIT_PF_ON,mEnableEIT_PF);
        return value;
    }

    public static void setEnableEIT_PF(boolean enableEITPf) {
        SystemProperties.set(PERSIST_EIT_PF_ON, String.valueOf(enableEITPf));
        Pvcfg.mEnableEIT_PF = enableEITPf;
    }

    public static boolean isSiupdateEnable() {
        boolean value = SystemProperties.getBoolean(PERSIST_SI_UPDATE_ENABLE,SIUPDATE_ENABLE);
        return value;
    }

    public static void setSiupdateEnable(boolean siupdateEnable) {
        SystemProperties.set(PERSIST_SI_UPDATE_ENABLE, String.valueOf(siupdateEnable));
        SIUPDATE_ENABLE = siupdateEnable;
    }

    public static boolean isEnableEIT_Scheduler() {
        boolean value = SystemProperties.getBoolean(PERSIST_EIT_SCHEDULER_ENABLE,mEnableEIT_Scheduler);
        return value;
    }

    public static void setEnableEIT_Scheduler(boolean enableEIT_Scheduler) {
        SystemProperties.set(PERSIST_EIT_SCHEDULER_ENABLE, String.valueOf(enableEIT_Scheduler));
        Pvcfg.mEnableEIT_Scheduler = enableEIT_Scheduler;
    }

    public static boolean isPmtupdateEnable() {
        boolean value = SystemProperties.getBoolean(PERSIST_PMT_UPDATE_ENABLE,PMTUPDATE_ENABLE);
        return value;
    }

    public static void setPmtupdateEnable(boolean pmtupdateEnable) {
        SystemProperties.set(PERSIST_PMT_UPDATE_ENABLE, String.valueOf(pmtupdateEnable));
        PMTUPDATE_ENABLE = pmtupdateEnable;
    }

    public static boolean isProcessEMM() {
        boolean value = SystemProperties.getBoolean(PERSIST_EMM_ENABLE,mProcessEMM);
        return value;
    }

    public static void setProcessEMM(boolean mProcessEMM) {
        SystemProperties.set(PERSIST_EMM_ENABLE, String.valueOf(mProcessEMM));
        Pvcfg.mProcessEMM = mProcessEMM;
    }

    public static void SetDRServerAddress(int mode, String address){
        if(mode == MODULE_DMG)
            DMG_DR_SERVER = address;
        else if(mode == MODULE_TBC)
            TBC_DR_SERVER = address;
    }
    public static String getWvcasEntitlementUrl(){
//        if(getModuleType() == MODULE_DMG){
            if(SystemProperties.getBoolean(PERSIST_USE_LABDR,false))
                return DMG_LABDR_SERVER+"/fortress/wvcas/entitlements";
            else
                return DMG_DR_SERVER+"/fortress/wvcas/entitlements";
//        }
//        else{
//            return TBC_DR_SERVER+"/fortress/wvcas/entitlements";
//        }
    }

    public static String getWvcasLicenseUrl(){
//        if(getModuleType() == MODULE_DMG){
            if(SystemProperties.getBoolean(PERSIST_USE_LABDR, false))
                return DMG_LABDR_SERVER+"/fortress/wvcas/license";
            else
                return DMG_DR_SERVER+"/fortress/wvcas/license";
//        }
//        else{
//            return TBC_DR_SERVER+"/fortress/wvcas/license";
//        }
	}
	
    public static boolean isCheckSectionComplete() {
        boolean value = SystemProperties.getBoolean(PERSIST_CHECK_SECTION_COMPLETED,mCheckSectionComplete);
        return value;
    }

    public static boolean IsUseCasn() {
        boolean value = SystemProperties.getBoolean(PERSIST_USE_CASN,mUseCasn);

        return value;
    }

    public static boolean isLiADFeature() {
        boolean value = SystemProperties.getBoolean(PERSIST_LIAD_FEATURE,mLiADFeature);
        return value;
    }

    public static String getEthernetMacAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaces) {
                // get Ethernet interface
                if (networkInterface.getName().equalsIgnoreCase("eth0")) {
                    byte[] macBytes = networkInterface.getHardwareAddress();
                    if (macBytes == null) return null;

                    StringBuilder macAddress = new StringBuilder();
                    for (byte b : macBytes) {
                        macAddress.append(String.format("%02X:", b));
                    }

                    if (macAddress.length() > 0) {
                        macAddress.deleteCharAt(macAddress.length() - 1); // 移除?�後�? ":"
                    }
                    return macAddress.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getCheckADTime() {
        int value = SystemProperties.getInt(PERSIST_CHECK_AD_TIME, mCheckADTime);
        if(value < 3)
            value += 24;
        return value;
    }
    public static void setCheckADTime(int hour) {
        mCheckADTime = hour;
        SystemProperties.set(PERSIST_CHECK_AD_TIME, Integer.toString(hour));
    }

    public static int getTimeshiftBuff_4K(){
        int value = SystemProperties.getInt(PERSIST_TIMESHIFTPBUFF_4K, PVR_TIMESHIFTPBUFF_4K);
        return value;
    }

    public static int getTimeshiftBuff_HD(){
        int value = SystemProperties.getInt(PERSIST_TIMESHIFTPBUFF_HD, PVR_TIMESHIFTPBUFF_HD);
        return value;
    }

    public static int getTimeshiftBuff_SD(){
        int value = SystemProperties.getInt(PERSIST_TIMESHIFTPBUFF_SD, PVR_TIMESHIFTPBUFF_SD);
        return value;
    }

    public static int getPvrHddLimitSize(){
        int value = SystemProperties.getInt(PERSIST_PVR_HDD_LIMIT_SIZE, PVR_HDD_LIMIT_SIZE);
        return value;
    }

    public static int getPvrHddCriticalSize(){
        return SystemProperties.getInt(PERSIST_PVR_HDD_CRITICAL_SIZE, PVR_HDD_CRITICAL_SIZE);
    }

    public static boolean supportTimeshiftForward() {
        return SystemProperties.getBoolean(PERSIST_TIMESHIFT_FORWARD, PVR_TIMESHIFT_FORWARD);
    }

    public static boolean supportTimeshiftRewind() {
        return SystemProperties.getBoolean(PERSIST_TIMESHIFT_REWIND, PVR_TIMESHIFT_REWIND);
    }

    public static boolean supportRecordMusic() {
        return SystemProperties.getBoolean(PERSIST_RECORD_MUSIC, PVR_RECORD_MUSIC);
    }

    public static boolean isCheckIllegalNetwork() {
        boolean value = SystemProperties.getBoolean(PERSIST_CHECK_ILLEGAL_NETWORK,mCheckIllegalNetwork);
        return value;
    }

    public static void setCheckIllegalNetwork(boolean CheckIllegalNetwork) {
        SystemProperties.set(PERSIST_CHECK_ILLEGAL_NETWORK, String.valueOf(CheckIllegalNetwork));
        Pvcfg.mCheckIllegalNetwork = CheckIllegalNetwork;
    }

    public static int getE213CheckTime() {
        int value = SystemProperties.getInt(PERSIST_E213_CHECK_TIME, mE213CheckTime);
        return value;
    }

    public static void setE213CheckTime(int value) {
        SystemProperties.set(PERSIST_E213_CHECK_TIME, Integer.toString(value));
        Pvcfg.mE213CheckTime = value;
    }

    public static boolean getEnableEPGEventMap() {
        return SystemProperties.getBoolean(PERSIST_ENABLE_EPG_EVENT_MAP, mEnableEPGEventMap);
    }

    public static void setEnableEPGEventMap(boolean enable) {
        SystemProperties.set(PERSIST_ENABLE_EPG_EVENT_MAP, String.valueOf(enable));
        Pvcfg.mEnableEPGEventMap = enable;
    }

    public static boolean isFixedModuelType() {
        boolean value = SystemProperties.getBoolean(PERSIST_FIXED_MODULE_TYPE, mFixedModuelType);
        return value;
    }

    public static boolean isWaitSurfaceReady() {
        boolean value = SystemProperties.getBoolean(PERSIST_WAIT_SURFACE_READY, mWaitSurfaceReady);
        return value;
    }

    public static boolean isAVPlayRunOnUIThread() {
        boolean value = SystemProperties.getBoolean(PERSIST_AV_ON_UI, mAVPlayRunOnUIThread);
        return value;
    }

    public static boolean isFccV2Enable() {
        boolean value = SystemProperties.getBoolean(PERSIST_FCC_V2, FCC_V2_ENABLE);
        return value;
    }

    public static boolean isRunRefreshCasByTime() {
        boolean value = SystemProperties.getBoolean(PERSIST_RUN_REFRESHCAS_BY_TIME, mRunRefreshCasByTime);
        return value;
    }

    public static int getFixedRefreshTime() {
        int value = SystemProperties.getInt(PERSIST_FIXED_REFRESH_TIME, mFixedRefreshTime);
        return value;
    }

    public static boolean isFccV3Enable() {
        boolean value = SystemProperties.getBoolean(PERSIST_FCC_V3, FCC_V3_ENABLE);
        return value;
    }

    public static int getSIHomeTPFrequency(){
        int value = SystemProperties.getInt(PERSIST_MODULE_TYPE, mModuleType);
        if(value == MODULE_DMG)
            return 615000;
        else
            return 303000;
    }

    public static boolean isLiADDebug() {
        boolean value = SystemProperties.getBoolean(PERSIST_LIAD_DEBUG, mLiADDebug);
        return value;
    }

    public static boolean getUpdateTdt() {
        boolean value = SystemProperties.getBoolean(PERSIST_UPDATE_RF_TDT, mUpdateTdt);
        return value;
    }
}
