package com.prime.datastructure.config;

import com.prime.datastructure.sysdata.TpInfo;

public class PropertyDefaultValue {
    public final static String ENCODING_DEFAULT = Pvcfg.Encoding.UTF8 ;
    public final static boolean FCC_ENABLE = true;
    public final static boolean FCC_V2_ENABLE = true;
    public final static boolean AVPlayRunOnUIThread = false;
    public final static boolean MULTIAUDIO_ENABLE = false;
    public final static int AV_SYNC_MODE = Pvcfg.AVSYNC_MODE.AUDIO;
    public final static boolean PROCESS_ECM = true;
    public final static boolean PROCESS_EMM = true;
    public final static boolean ENABLE_EIT_PF = true;
    public final static boolean SIUPDATE_ENABLE = true;
    public final static boolean ENABLE_EIT_SCHEDULER = true;
    public final static boolean PMTUPDATE_ENABLE = true;
    public final static boolean TOTUPDATE_ENABLE = true;
    public final static boolean INTERNET_EPG_ENABLE = false;
    public final static boolean APP_RECOMMENDS_ENABLE = false;
    public final static boolean LAST_CHANNEL_ENABLE = true;
    public final static String DEFAULT_LANGUAGE = "zh_TW";
    public final static String DEFAULT_LAUNCHER_MODULE = Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_CNS;
    public final static int CA_TYPE = Pvcfg.CA_TYPE.CA_WIDEVINE_CAS;
    public final static int TUNER_TYPE = TpInfo.DVBC;
    public final static boolean DEBUG_PMT = false;
    public final static boolean DEBUG_SUB_LANG_CHANGED = false;
    public final static boolean DEBUG_AUD_LANG_CHANGED = false;
    public final static boolean DEBUG_AV_PID_CHANGED = false;
    public final static boolean PARENTAL_LOCK_DEFAULT = true;
    public final static boolean TIME_LOCK_DEFAULT = false;
    public final static int CABLE_BANDWIDTH = 6;
    public final static boolean WaitSurfaceReady = true;
    public final static boolean PVR_PJ = false;//PVR_PJ = 1
    public final static int BatId = 25194;
    public final static boolean CheckIllegalNetwork = true;
    public final static int E213CheckTime = 5;//5 sec
    public final static boolean EnableEPGEventMap = false;
    public final static boolean FixedModuelType = false;
    public final static boolean RunRefreshCasByTime = true;
    public final static int FixedRefreshTime = 0;
    public final static boolean UpdateTdt = true;
    public final static boolean CheckSectionComplete = false;
    public final static boolean UseCasn = false;
    public final static boolean LiADFeature = true;
    public final static boolean LiADDebug = false;
    public final static int CheckADTime = 3;// AM 3:00  => 0:00~5:59
    public final static String TvInputId = "com.prime.tvinputframework/.PrimeTvInputService";
    public final static boolean primeDtvServiceEnable = true;
    public final static boolean primeTvInputEnable = true;
    public final static boolean mUseShareTuner = true;
    public final static boolean mDisableTableOnTimeshift = false;
    public final static boolean hideLauncherPvr = true;//eric lin 20251224 hide launcher pvr
    public final static String LiTvAdOnOff = "1";
    public final static String AreaLimitation = "1";
    public final static String ErrorCodeReportUrl = "https://sms.homeplus.net.tw/qrhandle/";
    public final static String VBM_URL = "http://vbm.totaltv.com.tw:8080/log/upload";
    public final static boolean IsTTCImage = false;
}
