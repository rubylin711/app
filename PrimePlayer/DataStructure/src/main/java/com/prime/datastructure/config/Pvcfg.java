package com.prime.datastructure.config;

import static com.prime.datastructure.config.Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_BLINCAST;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.SystemProperties;
import android.util.Log;

import java.net.NetworkInterface;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/** @noinspection BooleanMethodIsAlwaysInverted */
public class Pvcfg {
    public static final String TAG = "Pvcfg";

    public class Encoding {
        public static final String BIG5 = "Big5";
        public static final String UTF8 = "Utf-8";
        public static final String ISO_8859_1 = "ISO-8859-1";
    }

    public class AVSYNC_MODE {
        public static final int PCR = 0;
        public static final int AUDIO = 1;
    }

    public class LAUNCHER_MODULE {
        public static final String LAUNCHER_MODULE_SUND = "SunDirect";
        public static final String LAUNCHER_MODULE_TBC = "TBC";
        public static final String LAUNCHER_MODULE_DMG = "DMG";
        public static final String LAUNCHER_MODULE_CNS = "CNS";
        public static final String LAUNCHER_MODULE_BLINCAST = "Blincast";
        public static final String LAUNCHER_MODULE_DIGITURK = "Digiturk";
        public static final String LAUNCHER_MODULE_VECTRA = "Vectra";
        public static final String LAUNCHER_MODULE_EXPRESS = "Express";
        public static final String LAUNCHER_MODULE_TURKSAT = "Turksat";
        public static final String LAUNCHER_MODULE_TOP = "Top";
        public static final String LAUNCHER_MODULE_MNBC = "MNBC";
        public static final String LAUNCHER_MODULE_IPTV = "IPTV";
        public static final String LAUNCHER_MODULE_BAHIAVISION = "BahiavisionColor";
    }

    public class CA_TYPE {
        // define ca type
        public static final int CA_NONE = 0x0;
        public static final int CA_CONAX = 0x1;
        public static final int CA_SC = 0x2;
        public static final int CA_IRDETO = 0x04;
        public static final int CA_CLOAK = 0x08;
        public static final int CA_VMX = 0x10;
        public static final int CA_WIDEVINE_CAS = 0x20;// eric lin 20210107 widevine cas
    }

    /************ Config **************/
    public static final int UIMODEL_3796 = 3796;
    public static final int UIMODEL_3798 = 3798;
    public static final int UI_TYPE = 3;// Scoty 20180912 add UI type cfg
    public static final int UI_MODEL = UIMODEL_3796;// Scoty 20181024 add UI Model cfg
    public static final int PESI_CA_TYPE = CA_TYPE.CA_NONE; // eric lin 20210107 widevine cas

    public static final int PROJECT_HOME_TP_TUNER_ID = 3;
    public static int NUM_OF_RECORDING = 2;
    public static int PVR_TIMESHIFTPBUFF_4K = 30;// 30 min
    public static int PVR_TIMESHIFTPBUFF_HD = 120;// 120 min
    public static int PVR_TIMESHIFTPBUFF_SD = 720;// 720 min
    public static int PVR_HDD_LIMIT_SIZE = 250;// 250 mb // for not enough space
    public static int PVR_HDD_CRITICAL_SIZE = 50; // 50 mb // for not enough space
    public static int PVR_HDD_TOTAL_SIZE_LIMIT = 32000; // 32 gb // for total size limit
    public static int PVR_HDD_AUTHORIZED_SIZE = -1; // mb, -1 means all
    public static boolean PVR_INIT = false;
    public static boolean PVR_UI_V2 = false;
    private static final String PERSIST_PVR_UI_V2_ENABLE = "persist.sys.prime.pvr_ui_v2_enable";
    public static boolean PVR_TIMESHIFT_AUTO = false;
    public static boolean PVR_TIMESHIFT_FORWARD = false;
    public static boolean PVR_TIMESHIFT_REWIND = false;
    public static boolean PVR_TIMESHIFT_WAIT_STOP = true;
    private static final String PERSIST_TIMESHIFT_WAIT_STOP = "persist.sys.prime.timeshift_wait_stop";
    public static boolean PVR_RECORD_MUSIC = true;

    public static final boolean PIP = true;
    public static final boolean RANKING = false;
    public static boolean ENABLE_NETWORK_PROGRAM = false;// Enable Youtube & Vod Streams to Programs
    // if true, simple channel channel number = lcn, false = display number
    private static final boolean USE_LCN = true;
    private static boolean FCC_V3_ENABLE = false;
    private static final String PROPERTY_FCC_V3 = "persist.sys.prime.fcc_v3";
    private static boolean PMTUPDATE_ENABLE = true;
    private static final String PROPERTY_PMT_UPDATE_ENABLE = "persist.sys.prime.pmt_update";
    private static boolean SIUPDATE_ENABLE = true;
    private static final String PROPERTY_SI_UPDATE_ENABLE = "persist.sys.prime.si_update";

    // alti license server
    public static String CNS_DR_SERVER = "https://203.133.7.97:12100";
    public static String CNS_LABDR_SERVER = "https://203.133.7.97:12100";
    public static String DMG_DR_SERVER = "https://124.109.125.60:12100";
    public static String DMG_LABDR_SERVER = "https://124.109.125.59:12100";
    public static String TBC_DR_SERVER = "https://116.241.97.225:12100";
    private static final String PROPERTY_USE_LABDR = "persist.sys.prime.use_labdr";
    // public static final String WVCAS_LICENSE_URL_TBC =
    // "https://116.241.97.225:12100/fortress/wvcas/license";
    // public static final String WVCAS_LICENSE_URL_DMG =
    // "https://116.241.97.225:12100/fortress/wvcas/license";
    // google license server
    // public static final String WVCAS_LICENSE_URL =
    // "https://proxy.uat.widevine.com/proxy";
    // public static final String WVCAS_ENTITLEMENT_URL =
    // "https://116.241.97.225:12100/fortress/wvcas/entitlements";
    private static int ScanOneTP = 0;

    private static final String PROPERTY_MODULE_TYPE = "persist.sys.prime.module_type";

    public static final int ALTIMEDIA_CA_SYSTEM_ID = 7687;
    public static final int WVCAS_CA_SYSTEM_ID = 19156;

    private static boolean TRY_LICENSE_ENTITLED_ONLY = false;

    public static final int RADIO_FAV_NO_MAX = 16;
    public static final int TV_FAV_NO_MAX = 16;
    public static final int GROUP_NO = 50;

    public static final int SERIES_EXPIRE_WEEKS = 4;
    public static final int SERIES_CHECK_MINUTE = 1;
    public static final int WAITING_SCHEDULE_CHECK_MINUTE = 60 * 24; // one day

//    public static final int MAX_DISPLAY_NUMBER = 100000;
    public static int MAX_DISPLAY_NUMBER;

    static {
        // 讀取名為 "persist.sys.max_display" 的屬性
        // 第二個參數 100000 是預設值（如果找不到該屬性時使用）
        if(Pvcfg.isTTCImage())
            MAX_DISPLAY_NUMBER = 100000;
        else
            MAX_DISPLAY_NUMBER = 1000;
    }

    public static boolean getMultiAudio_Enable() {
        boolean isEnable = SystemProperties.getBoolean(PropertyDefine.PROPERTY_MULTIAUDIO,
                PropertyDefaultValue.MULTIAUDIO_ENABLE);
        return isEnable;
    }

    public static boolean getPVR_PJ() {
        boolean isEnable = SystemProperties.getBoolean(PropertyDefine.PROPERTY_PVRPJ, PropertyDefaultValue.PVR_PJ);
        return isEnable;
    }

    public static void setPVR_PJ(boolean pvr_pj) {
        SystemProperties.set(PropertyDefine.PROPERTY_PVRPJ, String.valueOf(pvr_pj));
    }

    public static boolean getPIP() {
        return PIP;
    }

    public static int getUIType() {// Scoty 20180912 add UI type cfg
        return UI_TYPE;
    }

    public static int getUIModel() {// Scoty 20181024 add UI Model cfg
        return UI_MODEL;
    }

    public static int getCAType() // connie 20181107 for get CA Type
    {
        return SystemProperties.getInt(PropertyDefine.PROPERTY_CA_TYPE, PropertyDefaultValue.CA_TYPE);
    }

    public static boolean IsEnableNetworkPrograms()// Enable Youtube & Vod Streams to Programs
    {
        return ENABLE_NETWORK_PROGRAM;
    }

    public static void SetEnableNetworkPrograms(boolean enable)// Enable Youtube & Vod Streams to Programs
    {
        ENABLE_NETWORK_PROGRAM = enable;
    }

    public static boolean channelNumberUseLcn() {
        return USE_LCN;
    }

    public static void setBatId(int batId) {
        SystemProperties.set(PropertyDefine.PROPERTY_BAT_ID, String.valueOf(batId));
    }

    public static int getBatId() {
        int value = SystemProperties.getInt(PropertyDefine.PROPERTY_BAT_ID, PropertyDefaultValue.BatId);
        return value;
    }

    // johnny 20240801 the default charset to decode dvb text bytes
    // if first byte does not contain character coding
    // see also com.prime.dtv.service.Util.DVBString#toString(Charset)
    public static Charset getDefaultCharset() {
        if (getModuleType().equals(LAUNCHER_MODULE.LAUNCHER_MODULE_DMG))
            return Charset.forName("Big5");
        return Charset.forName(getEncodingType());
    }

    public static String getEncodingType() {
        return SystemProperties.get(PropertyDefine.PROPERTY_ENCODING, PropertyDefaultValue.ENCODING_DEFAULT);
    }

    public static boolean isAVPlayRunOnUIThread() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PROPERTY_AV_ON_UI,
                PropertyDefaultValue.AVPlayRunOnUIThread);
        return value;
    }

    public static boolean isFccEnable() {
        boolean isEnable = SystemProperties.getBoolean(PropertyDefine.PROPERTY_FCC_ENABLE,
                PropertyDefaultValue.FCC_ENABLE);
        return isEnable;
    }

    public static boolean isFccV2Enable() {
        if (isFccEnable())
            return SystemProperties.getBoolean(PropertyDefine.PROPERTY_FCC_V2, PropertyDefaultValue.FCC_V2_ENABLE);
        return false;
    }

    public static boolean isFccSurfaceEnable() {
        // Default true for the new FCC SurfaceView implementation
        return SystemProperties.getBoolean("persist.sys.prime.fcc_surface", true);
    }

    public static int getCableBandwidth() {
        int cable_bandwidth = SystemProperties.getInt(PropertyDefine.PROPERTY_CABLE_BANDWIDTH,
                PropertyDefaultValue.CABLE_BANDWIDTH);
        return cable_bandwidth;
    }

    public static void setFccEnable(boolean enable) {
        SystemProperties.set(PropertyDefine.PROPERTY_FCC_ENABLE, Boolean.toString(enable));
    }

    public static int getScanOneTP() {
        return ScanOneTP;
    }

    public static void setScanOneTP(int scanOneTP) {
        ScanOneTP = scanOneTP;
    }

    public static boolean isLauncherTypeNoTMS() {
        if (Pvcfg.getModuleType().equals(LAUNCHER_MODULE.LAUNCHER_MODULE_SUND)
                || Pvcfg.getModuleType().equals(LAUNCHER_MODULE_BLINCAST)
                || Pvcfg.getModuleType().equals(LAUNCHER_MODULE.LAUNCHER_MODULE_EXPRESS)
                || Pvcfg.getModuleType().equals(LAUNCHER_MODULE.LAUNCHER_MODULE_TOP)
                || Pvcfg.getModuleType().equals(LAUNCHER_MODULE.LAUNCHER_MODULE_IPTV)
                || Pvcfg.getModuleType().equals(LAUNCHER_MODULE.LAUNCHER_MODULE_BAHIAVISION))
            return false;
        else
            return true;
    }

    public static String getModuleType() {
        return SystemProperties.get(PropertyDefine.PROPERTY_LAUNCHER_MODULE,
                PropertyDefaultValue.DEFAULT_LAUNCHER_MODULE);
    }

    public static void setModuleType(String mModuleType) {
        if (isFixedModuelType() == false) {
            SystemProperties.set(PropertyDefine.PROPERTY_LAUNCHER_MODULE, mModuleType);
        }
    }

    public static boolean isTryLicenseEntitledOnly() {
        return TRY_LICENSE_ENTITLED_ONLY;
    }

    public static void setTryLicenseEntitledOnly(boolean enable) {
        TRY_LICENSE_ENTITLED_ONLY = enable;
    }

    public static int getSyncMode() {
        int sync_mod = SystemProperties.getInt(PropertyDefine.PROPERTY_AVSYNC_MODE, PropertyDefaultValue.AV_SYNC_MODE);
        return sync_mod;
    }

    public static void setSyncMode(int SyncMode) {
        SystemProperties.set(PropertyDefine.PROPERTY_AVSYNC_MODE, Integer.toString(SyncMode));
    }

    public static boolean isProcessECM() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PROPERTY_ECM_ENABLE,
                PropertyDefaultValue.PROCESS_ECM);
        return value;
    }

    public static void setmProcessECM(boolean processECM) {
        SystemProperties.set(PropertyDefine.PROPERTY_ECM_ENABLE, Boolean.toString(processECM));
    }

    public static boolean isProcessEMM() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PROPERTY_EMM_ENABLE,
                PropertyDefaultValue.PROCESS_EMM);
        return value;
    }

    public static void setProcessEMM(boolean processEMM) {
        SystemProperties.set(PropertyDefine.PROPERTY_EMM_ENABLE, Boolean.toString(processEMM));
    }

    public static boolean isEnableEIT_PF() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PROPERTY_EPG_PF_ENABLE,
                PropertyDefaultValue.ENABLE_EIT_PF);
        return value;
    }

    public static void setEnableEIT_PF(boolean enableEITPf) {
        SystemProperties.set(PropertyDefine.PROPERTY_EPG_PF_ENABLE, Boolean.toString(enableEITPf));
    }

    public static boolean isSiupdateEnable() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PROPERTY_SI_UPDATE,
                PropertyDefaultValue.SIUPDATE_ENABLE);
        return value;
    }

    public static void setSiupdateEnable(boolean siupdateEnable) {
        SystemProperties.set(PropertyDefine.PROPERTY_SI_UPDATE, Boolean.toString(siupdateEnable));
    }

    public static boolean isEnableEIT_Scheduler() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PROPERTY_EPG_SCHEDULER_ENABLE,
                PropertyDefaultValue.ENABLE_EIT_SCHEDULER);
        return value;
    }

    public static void setEnableEIT_Scheduler(boolean enableEIT_Scheduler) {
        SystemProperties.set(PropertyDefine.PROPERTY_EPG_SCHEDULER_ENABLE, Boolean.toString(enableEIT_Scheduler));
    }

    public static boolean isInternetEpgEnable() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PROPERTY_INTERNET_EPG,
                PropertyDefaultValue.INTERNET_EPG_ENABLE);
        return value;
    }

    public static boolean isAppRecommendsEnable() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PROPERTY_APP_RECOMMENDS,
                PropertyDefaultValue.APP_RECOMMENDS_ENABLE);
        return value;
    }

    public static boolean isPmtupdateEnable() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PROPERTY_PMT_UPDATE,
                PropertyDefaultValue.PMTUPDATE_ENABLE);
        return value;
    }

    public static boolean isTotUpdateEnable() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PROPERTY_TOT_UPDATE,
                PropertyDefaultValue.TOTUPDATE_ENABLE);
        return value;
    }

    public static boolean isLastChannelEnable() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PROPERTY_LAST_CHANNEL,
                PropertyDefaultValue.LAST_CHANNEL_ENABLE);
        return value;
    }

    public static int getTunerType() {
        return SystemProperties.getInt(PropertyDefine.PROPERTY_TUNER_TYPE, PropertyDefaultValue.TUNER_TYPE);
    }

    public static boolean getDebugPmtUpdate() {
        return SystemProperties.getBoolean(PropertyDefine.PROPERTY_DEBUG_PMT, PropertyDefaultValue.DEBUG_PMT);
    }

    public static boolean getDebugPmtSublang() {
        return SystemProperties.getBoolean(PropertyDefine.PROPERTY_DEBUG_SUB_LANG_CHANGED,
                PropertyDefaultValue.DEBUG_SUB_LANG_CHANGED);
    }

    public static boolean getDebugPmtAudlang() {
        return SystemProperties.getBoolean(PropertyDefine.PROPERTY_DEBUG_AUD_LANG_CHANGED,
                PropertyDefaultValue.DEBUG_AUD_LANG_CHANGED);
    }

    public static boolean getDebugPmtAvpid() {
        return SystemProperties.getBoolean(PropertyDefine.PROPERTY_DEBUG_AV_PID_CHANGED,
                PropertyDefaultValue.DEBUG_AV_PID_CHANGED);
    }

    private static Locale getLocale(String lang) {
        int i = 0;
        String language = "", country = "";
        // Log.d(TAG,"getLocale lang = "+lang);
        for (String str : lang.split("_")) {
            if (i == 0)
                language = str;
            else if (i == 1)
                country = str;
            i++;
        }
        // Log.d(TAG,"language = "+language+" country = "+country);
        Locale locale = new Locale(language, country, "");
        return locale;
    }

    public static Context getSupportLanguage(Context context) {
        boolean languageIsSupport = false;
        Configuration config = context.getResources().getConfiguration();
        Locale currentLocale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            currentLocale = config.getLocales().get(0);
        } else {
            currentLocale = config.locale;
        }

        Log.d(TAG, "getSupportLanguage currentLocale.getLanguage() = " + currentLocale.getLanguage());
        Log.d(TAG, "getSupportLanguage currentLocale.getCountry() = " + currentLocale.getCountry());
        String setting_language = currentLocale.getLanguage() + "_" + currentLocale.getCountry();
        String supportLanguage = SystemProperties.get(PropertyDefine.PROPERTY_SUPPORT_LANGUAGE,
                PropertyDefaultValue.DEFAULT_LANGUAGE);
        Log.d(TAG, "getSupportLanguage prime support = " + supportLanguage);
        int i = 0;
        for (String str : supportLanguage.split(",")) {
            Log.d(TAG, "getSupportLanguage prime support[" + i + "] = " + str);
            if (setting_language.equals(str)) {
                languageIsSupport = true;
                break;
            }
            i++;
        }
        if (!languageIsSupport) {
            config.setLocale(getLocale(PropertyDefaultValue.DEFAULT_LANGUAGE));
            Log.d(TAG, "set default language = " + PropertyDefaultValue.DEFAULT_LANGUAGE);
            // ?萄遣銝???Context嚗 Resources ????Configuration ??
            Context newContext = context.createConfigurationContext(config);
            return newContext;
        }
        return context;
    }

    public static boolean getSupportParentalLock() {
        return SystemProperties.getBoolean(PropertyDefine.PROPERTY_PARENTAL_LOCK,
                PropertyDefaultValue.PARENTAL_LOCK_DEFAULT);
    }

    public static boolean getSupportTimeLock() {
        return SystemProperties.getBoolean(PropertyDefine.PROPERTY_TIME_LOCK, PropertyDefaultValue.TIME_LOCK_DEFAULT);
    }

    public static boolean isWaitSurfaceReady() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PROPERTY_WAIT_SURFACE_READY,
                PropertyDefaultValue.WaitSurfaceReady);
        return value;
    }

    public static void setPmtupdateEnable(boolean pmtupdateEnable) {
        SystemProperties.set(PROPERTY_PMT_UPDATE_ENABLE, String.valueOf(pmtupdateEnable));
        PMTUPDATE_ENABLE = pmtupdateEnable;
    }

    public static String getWvcasEntitlementUrl() {
        // if(getModuleType() == MODULE_DMG){
        if (SystemProperties.getBoolean(PROPERTY_USE_LABDR, false))
            return CNS_LABDR_SERVER + "/fortress/wvcas/entitlements";
        else
            return CNS_DR_SERVER + "/fortress/wvcas/entitlements";
        // }
        // else{
        // return TBC_DR_SERVER+"/fortress/wvcas/entitlements";
        // }
    }

    public static String getWvcasLicenseUrl() {
        // if(getModuleType() == MODULE_DMG){
        if (SystemProperties.getBoolean(PROPERTY_USE_LABDR, false))
            return CNS_LABDR_SERVER + "/fortress/wvcas/license";
        else
            return CNS_DR_SERVER + "/fortress/wvcas/license";
        // }
        // else{
        // return TBC_DR_SERVER+"/fortress/wvcas/license";
        // }
    }

    public static boolean isCheckSectionComplete() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PROPERTY_CHECK_SECTION_COMPLETED,
                PropertyDefaultValue.CheckSectionComplete);
        return value;
    }

    public static boolean IsUseCasn() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PROPERTY_USE_CASN, PropertyDefaultValue.UseCasn);

        return value;
    }

    public static boolean isLiADFeature() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PROPERTY_LIAD_FEATURE,
                PropertyDefaultValue.LiADFeature);
        return value;
    }

    public static String getEthernetMacAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaces) {
                // get Ethernet interface
                if (networkInterface.getName().equalsIgnoreCase("eth0")) {
                    byte[] macBytes = networkInterface.getHardwareAddress();
                    if (macBytes == null)
                        return null;

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
        int value = SystemProperties.getInt(PropertyDefine.PROPERTY_CHECK_AD_TIME, PropertyDefaultValue.CheckADTime);
        if (value < 3)
            value += 24;
        return value;
    }

    public static void setCheckADTime(int hour) {
        SystemProperties.set(PropertyDefine.PROPERTY_CHECK_AD_TIME, Integer.toString(hour));
    }

    public static int getTimeshiftBuff_4K() {
        int value = SystemProperties.getInt(PropertyDefine.PROPERTY_TIMESHIFTPBUFF_4K, PVR_TIMESHIFTPBUFF_4K);
        return value;
    }

    public static int getTimeshiftBuff_HD() {
        int value = SystemProperties.getInt(PropertyDefine.PROPERTY_TIMESHIFTPBUFF_HD, PVR_TIMESHIFTPBUFF_HD);
        return value;
    }

    public static int getTimeshiftBuff_SD() {
        int value = SystemProperties.getInt(PropertyDefine.PROPERTY_TIMESHIFTPBUFF_SD, PVR_TIMESHIFTPBUFF_SD);
        return value;
    }

    public static int getPvrHddLimitSize() {
        int value = SystemProperties.getInt(PropertyDefine.PROPERTY_PVR_HDD_LIMIT_SIZE, PVR_HDD_LIMIT_SIZE);
        return value;
    }

    public static int getPvrHddCriticalSize() {
        return SystemProperties.getInt(PropertyDefine.PROPERTY_PVR_HDD_CRITICAL_SIZE, PVR_HDD_CRITICAL_SIZE);
    }

    public static int getPvrHddTotalSizeLimit() {
        return SystemProperties.getInt(PropertyDefine.PROPERTY_PVR_HDD_TOTAL_SIZE_LIMIT, PVR_HDD_TOTAL_SIZE_LIMIT);
    }

    public static int getPvrHddAuthorizedSize() {
        return SystemProperties.getInt(PropertyDefine.PROPERTY_PVR_HDD_AUTHORIZED_SIZE, PVR_HDD_AUTHORIZED_SIZE);
    }

    public static void setPvrHddAuthorizedSize(int sizeMb) {
        SystemProperties.set(PropertyDefine.PROPERTY_PVR_HDD_AUTHORIZED_SIZE, String.valueOf(sizeMb));
    }

    public static boolean supportTimeshiftForward() {
        return SystemProperties.getBoolean(PropertyDefine.PROPERTY_TIMESHIFT_FORWARD, PVR_TIMESHIFT_FORWARD);
    }

    public static boolean supportTimeshiftRewind() {
        return SystemProperties.getBoolean(PropertyDefine.PROPERTY_TIMESHIFT_REWIND, PVR_TIMESHIFT_REWIND);
    }

    public static boolean supportRecordMusic() {
        return SystemProperties.getBoolean(PropertyDefine.PROPERTY_RECORD_MUSIC, PVR_RECORD_MUSIC);
    }

    public static boolean isCheckIllegalNetwork() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PROPERTY_CHECK_ILLEGAL_NETWORK,
                PropertyDefaultValue.CheckIllegalNetwork);
        return value;
    }

    public static void setCheckIllegalNetwork(boolean CheckIllegalNetwork) {
        SystemProperties.set(PropertyDefine.PROPERTY_CHECK_ILLEGAL_NETWORK, String.valueOf(CheckIllegalNetwork));
    }

    public static int getE213CheckTime() {
        int value = SystemProperties.getInt(PropertyDefine.PROPERTY_E213_CHECK_TIME,
                PropertyDefaultValue.E213CheckTime);
        return value;
    }

    public static void setE213CheckTime(int value) {
        SystemProperties.set(PropertyDefine.PROPERTY_E213_CHECK_TIME, Integer.toString(value));
    }

    public static boolean getEnableEPGEventMap() {
        return SystemProperties.getBoolean(PropertyDefine.PROPERTY_ENABLE_EPG_EVENT_MAP,
                PropertyDefaultValue.EnableEPGEventMap);
    }

    public static void setEnableEPGEventMap(boolean enable) {
        SystemProperties.set(PropertyDefine.PROPERTY_ENABLE_EPG_EVENT_MAP, String.valueOf(enable));
    }

    public static boolean isFixedModuelType() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PROPERTY_FIXED_MODULE_TYPE,
                PropertyDefaultValue.FixedModuelType);
        return value;
    }

    public static boolean isRunRefreshCasByTime() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PROPERTY_RUN_REFRESHCAS_BY_TIME,
                PropertyDefaultValue.RunRefreshCasByTime);
        return value;
    }

    public static int getFixedRefreshTime() {
        int value = SystemProperties.getInt(PropertyDefine.PROPERTY_FIXED_REFRESH_TIME,
                PropertyDefaultValue.FixedRefreshTime);
        return value;
    }

    public static boolean isFccV3Enable() {
        boolean value = SystemProperties.getBoolean(PROPERTY_FCC_V3, FCC_V3_ENABLE);
        return value;
    }

    public static int getSIHomeTPFrequency() {
        if (getModuleType().equals(LAUNCHER_MODULE.LAUNCHER_MODULE_DMG))
            return 615000;
        else if (getModuleType().equals(LAUNCHER_MODULE.LAUNCHER_MODULE_CNS))
            return 405000;
        else
            return 303000;
    }

    public static boolean isLiADDebug() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PROPERTY_LIAD_DEBUG, PropertyDefaultValue.LiADDebug);
        return value;
    }

    public static boolean getUpdateTdt() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PROPERTY_UPDATE_RF_TDT,
                PropertyDefaultValue.UpdateTdt);
        return value;
    }

    public static void setPvrInit(boolean pvrInit) {
        PVR_INIT = pvrInit;
    }

    public static boolean isPvrInit() {
        return PVR_INIT;
    }

    public static boolean supportWaitTimeshiftStop() {
        boolean value = getPVR_PJ()
                && SystemProperties.getBoolean(PERSIST_TIMESHIFT_WAIT_STOP, PVR_TIMESHIFT_WAIT_STOP);
        return value;
    }

    public static String getTvInputId() {
        String value = SystemProperties.get(PropertyDefine.PROPERTY_TV_INPUT_ID, PropertyDefaultValue.TvInputId);
        return value;
    }

    public static boolean isPrimeDTVServiceEnable() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PROPERTY_PRIME_DTV_SERVICE_ENABLE,
                PropertyDefaultValue.primeDtvServiceEnable);
        // Log.d(TAG,"isPrimeDTVServiceEnable = "+value);
        return value;
    }

    public static long getEpgTimezoneOffset() {
        if (Pvcfg.getModuleType().equals(LAUNCHER_MODULE.LAUNCHER_MODULE_TBC)
                || Pvcfg.getModuleType().equals(LAUNCHER_MODULE.LAUNCHER_MODULE_DMG))
            return 0;
        return TimeZone.getDefault().getRawOffset();
    }

    public static boolean isPrimeTvInputEnable() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PROPERTY_PRIME_TV_INPUT_ENABLE,
                PropertyDefaultValue.primeTvInputEnable);
        // Log.d(TAG,"primeTvInputEnable = "+value);
        return value;
    }

    public static void setSysEdidHyManuname(String value) {
        SystemProperties.set("sys.edid.hy.manuname", value);
    }

    public static String getSysEdidHyManuname() {
        String value = SystemProperties.get("sys.edid.hy.manuname", "");
        return value;
    }

    public static boolean isUseShareTuner() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PROPERTY_USE_SHARE_TUNER,
                PropertyDefaultValue.mUseShareTuner);
        return value;
    }

    public static void setUseShareTuner(boolean mUseShareTuner) {
        SystemProperties.set(PropertyDefine.PROPERTY_USE_SHARE_TUNER, String.valueOf(mUseShareTuner));
    }

    public static boolean isDisableTableOnTimeshift() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PERSIST_DISABLE_TABLE_ON_TIMESHIFT,
                PropertyDefaultValue.mDisableTableOnTimeshift);
        return value;
    }

    public static String get_device_sn() {
        return SystemProperties.get(PropertyDefine.PROPERTY_DEVICE_SN);
    }

    public static String get_ca_sn() {
        return SystemProperties.get(PropertyDefine.PROPERTY_DEVICE_SN);
    }

    public static String get_firmware_version() {
        return SystemProperties.get(PropertyDefine.PROPERTY_FIRMWARE_VERSION);
    }

    public static String get_loader_firmware_version() {
        return SystemProperties.get(PropertyDefine.PROPERTY_LOADER_FIRMWARE_VERSION);
    }

    public static String get_cns_ticker_transaction_id() {
        return SystemProperties.get(PropertyDefine.PROPERTY_CNS_TICKER_TRANSACTION_ID, "");
    }

    public static void set_cns_ticker_transaction_id(String value) {
        SystemProperties.set(PropertyDefine.PROPERTY_CNS_TICKER_TRANSACTION_ID, value);
    }

    public static String get_cns_ad_transaction_id() {
        return SystemProperties.get(PropertyDefine.PROPERTY_CNS_AD_TRANSACTION_ID, "");
    }

    public static void set_cns_ad_transaction_id(String value) {
        SystemProperties.set(PropertyDefine.PROPERTY_CNS_AD_TRANSACTION_ID, value);
    }

    public static int get_hardware_version() {
        return 1;
    }

    public static boolean get_hideLauncherPvr() {// eric lin 20251224 hide launcher pvr
        boolean isHide = SystemProperties.getBoolean(PropertyDefine.PROPERTY_HIDE_Launcher_Pvr,
                PropertyDefaultValue.hideLauncherPvr);
        return isHide;
    }

    public static String get_LiTvAd_onoff() {
        return SystemProperties.get(PropertyDefine.PROPERTY_LITV_ONOFF_SYSTEM_PROPERTY,
                PropertyDefaultValue.LiTvAdOnOff);
    }

    public static String get_area_limitation() {
        return SystemProperties.get(PropertyDefine.PROPERTY_AREA_LIMITATION, PropertyDefaultValue.AreaLimitation);
    }

    public static String get_error_code_report_url() {
        return SystemProperties.get(PropertyDefine.PROPERTY_ERROR_CODE_REPORT_URL,
                PropertyDefaultValue.ErrorCodeReportUrl);
    }

    public static void set_error_code_report_url(String value) {
        SystemProperties.set(PropertyDefine.PROPERTY_ERROR_CODE_REPORT_URL, value);
    }

    public static void set_area_limitation(String value) {
        SystemProperties.set(PropertyDefine.PROPERTY_AREA_LIMITATION, value);
    }

    public static String get_Vbm_Url() {
        return SystemProperties.get(PropertyDefine.PROP_VBM_URL, PropertyDefaultValue.VBM_URL);
    }

    public static void set_Vbm_Url(String value) {
        SystemProperties.set(PropertyDefine.PROP_VBM_URL, value);
    }

    public static void setPVR_Tuner_Resource(String value) {
        SystemProperties.set(PropertyDefine.PROP_PVR_TUNER_RESOURCE, value);
    }

    public static String getPVR_Tuner_Resource() {
        return SystemProperties.get(PropertyDefine.PROP_PVR_TUNER_RESOURCE, "");
    }

    public static void setPVR_Encrypt_Key(String value) {
        SystemProperties.set(PropertyDefine.PROP_PVR_ENCRYPT_KEY, value);
    }

    public static String getPVR_Encrypt_Key() {
        return SystemProperties.get(PropertyDefine.PROP_PVR_ENCRYPT_KEY, "");
    }

    public static void setHDD_SN(String value) {
        SystemProperties.set(PropertyDefine.PROP_HDD_PAIR_SN, value);
    }

    public static String getHDD_SN() {
        return SystemProperties.get(PropertyDefine.PROP_HDD_PAIR_SN, "");
    }

    public static void setATV_GroupId(String value) {
        SystemProperties.set(PropertyDefine.PROPERTY_ATV_GROUP_ID, value);
    }

    public static void setATV_Mode(String value) {
        SystemProperties.set(PropertyDefine.PROPERTY_ATV_MODE, value);
    }

    public static void setATV_Properties(String value) {
        SystemProperties.set(PropertyDefine.PROPERTY_ATV_MODE, value);
    }

    public static void setATV_AD_Vendor(String value) {
        SystemProperties.set(PropertyDefine.PROPERTY_ATV_AD_VENDOR, value);
    }

    public static String getATV_AD_Vendor() {
        return SystemProperties.get(PropertyDefine.PROPERTY_ATV_AD_VENDOR);
    }

    public static void setATV_AD_Enable(String value) {
        SystemProperties.set(PropertyDefine.PROPERTY_ATV_AD_ENABLE, value);
    }

    public static String getATV_AD_Enable() {
        return SystemProperties.get(PropertyDefine.PROPERTY_ATV_AD_ENABLE);
    }

    public static void setATV_AD_Properties(String value) {
        SystemProperties.set(PropertyDefine.PROPERTY_ATV_AD_PROPERTIES, value);
    }

    public static String getATV_AD_Properties() {
        return SystemProperties.get(PropertyDefine.PROPERTY_ATV_AD_PROPERTIES);
    }

    public static int get_cns_overdue_payment() {
        return SystemProperties.getInt(PropertyDefine.PROPERTY_CNS_OVERDUE_PAYMENT, 0);
    }

    public static void set_cns_overdue_payment(int value) {
        SystemProperties.set(PropertyDefine.PROPERTY_CNS_OVERDUE_PAYMENT, String.valueOf(value));
    }

    public static boolean isTTCImage() {
        boolean value = SystemProperties.getBoolean(PropertyDefine.PROPERTY_IS_TTC_IMAGE,
                PropertyDefaultValue.IsTTCImage);
        // Log.d(TAG,"value = "+value);
        return value;
    }
}
