package com.prime.datastructure.sysdata;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.utils.LogUtils;
import com.prime.datastructure.utils.TVScanParams;

import java.util.ArrayList;

/**
 * Created by gary_hsu on 2017/11/10.
 */

public class GposInfo implements Parcelable {
    public static final String TAG = "GposInfo";
    public static final int NUMBER_OF_AUDIO_LANGUAGE_SELECTION = 2;
    // middle ware
    //content provider
    private static String URI_PATH = "Gpos";
    private static String KEY = "KEY";
    private static String VALUE = "VALUE";

    // if add new member , should add new String member for member, and also add in
    public static final String GPOS_DB_VSERSION = "DBVersion";
    public static final String GPOS_CUR_CHANNELID = "CurChannelId";
    public static final String GPOS_CUR_GROUP_TYPE = "CurGroupType";
    public static final String GPOS_PASSWORD_VALUE = "PasswordValue";
    public static final String GPOS_WORKER_PASSWORD_VALUE = "WorkerPasswordValue";
    public static final String GPOS_PURCHASE_PASSWORD_VALUE = "PurchasePasswordValue";
    public static final String GPOS_PARENTAL_RATE = "ParentalRate";
    public static final String GPOS_PARENTAL_LOCK_ONOFF = "ParentalLockOnOff";
    public static final String GPOS_INSTALL_LOCK_ONOFF = "InstallLockOnOff";
    public static final String GPOS_BOX_POWER_STATUS = "BoxPowerStatus";
    public static final String GPOS_START_ON_CHANNELID = "StartOnChannelId";
    public static final String GPOS_START_ON_CHTYPE = "StartOnChType";
    public static final String GPOS_VOLUME = "Volume";
    public static final String GPOS_AUDIO_TRACK_MODE = "AudioTrackMode";
    public static final String GPOS_AUTO_REGION_TIME_OFFSET = "AutoRegionTimeOffset";
    public static final String GPOS_REGION_TIME_OFSSET = "RegionTimeOffset";
    public static final String GPOS_REGION_SUMMER_TIME = "RegionSummerTime";
    public static final String GPOS_LNB_POWER = "LnbPower";
    public static final String GPOS_SCREEN_16X9 = "Screen16x9";
    public static final String GPOS_CONVERSION = "Conversion";
    public static final String GPOS_RESOLUTION = "Resolution";
    public static final String GPOS_OSD_LANGUAGE = "OSDLanguage";
    public static final String GPOS_SEARCH_PROGRAM_TYPE = "SearchProgramType";
    public static final String GPOS_SEARCH_MODE = "SearchMode";
    public static final String GPOS_AUDIO_LANG_SELECT_1 = "AudioLanguageSelection1";
    public static final String GPOS_AUDIO_LANG_SELECT_2 = "AudioLanguageSelection2";
    public static final String GPOS_SUBTITLE_LANG_SELECT_1 = "SubtitleLanguageSelection1";
    public static final String GPOS_SUBTITLE_LANG_SELECT_2 = "SubtitleLanguageSelection2";
    public static final String GPOS_SORT_BY_LCN = "SortByLcn";
    public static final String GPOS_OSD_TRANSPARENCY = "OSDTransparency";
    public static final String GPOS_BANNER_TIMEOUT = "BannerTimeout";
    public static final String GPOS_HARD_HEARING = "HardHearing";
    public static final String GPOS_AUTO_STANDBY_TIME = "AutoStandbyTime";
    public static final String GPOS_DOLBY_MODE = "DolbyMode";
    public static final String GPOS_HDCP_ONOFF = "HDCPOnOff";
    public static final String GPOS_DEEP_SLEEP_MODE = "DeepSleepMode";
    public static final String GPOS_SUBTITLE_ONOFF = "SubtitleOnOff";
    public static final String GPOS_AV_STOP_MODE = "AvStopMode";
    public static final String GPOS_TIMESHIFT_DURATION = "TimeshiftDuration";
    public static final String GPOS_RECORD_ICON_ONOFF = "RecordIconOnOff";
    public static final String GPOS_MAIL_SETTINGS_SHOPPING = "MailSettingsShopping";
    public static final String GPOS_MAIL_SETTINGS_NEWS = "MailSettingsNews";
    public static final String GPOS_MAIL_SETTINGS_POPULAR = "MailSettingsPopular";
    public static final String GPOS_MAIL_SETTINGS_COUPON = "MailSettingsCoupon";
    public static final String GPOS_MAIL_SETTINGS_SERVICE = "MailSettingsService";
    public static final String GPOS_CHANNEL_LOCK_COUNT = "ChannelLockCount";
    public static final String GPOS_TIME_LOCK_ALL_DAY = "TimeLockAllDay";
    public static final String GPOS_TIME_LOCK_PERIOD_1_START = "TimeLockPeriod1Start";
    public static final String GPOS_TIME_LOCK_PERIOD_2_START = "TimeLockPeriod2Start";
    public static final String GPOS_TIME_LOCK_PERIOD_3_START = "TimeLockPeriod3Start";
    public static final String GPOS_TIME_LOCK_PERIOD_1_END = "TimeLockPeriod1End";
    public static final String GPOS_TIME_LOCK_PERIOD_2_END = "TimeLockPeriod2End";
    public static final String GPOS_TIME_LOCK_PERIOD_3_END = "TimeLockPeriod3End";
    public static final String GPOS_NIT_VERSION = "NitTableVersionNumber";
    public static final String GPOS_NIT_ID = "NitTableNetworkId";
    public static final String GPOS_BAT_VERSION = "BatTableVersionNumber";
    public static final String GPOS_BAT_ID = "BatTableBatId";
    public static final String GPOS_ZIPCODE = "ZipCode";
    public static final String GPOS_INTRODUCTION_TIME = "IntroductionTime";
    public static final String GPOS_PVRENABLE = "PvrEnable";
    public static final String GPOS_IrdCommand = "IrdCommand_";
    public static final String GPOS_HOME_ID = "HomeId";
    public static final String GPOS_SO = "So";
    public static final String GPOS_MOBILE = "Mobile";
    public static final String GPOS_STANDBY_REDIRECT = "StandbyRedirect";
    public static final String GPOS_MAIL_NOTIFY_STATUS = "MailNotifyStatus";

    public static final String GPOS_SEARCH_ONE_TP_FLAG = "SearchOneTPFlag";
    public static final String GPOS_ISMUTE = "MuteFlag";
    public static final String GPOS_WVCAS_LICENSE_URL = "WVCasLicenseURL";
    public static final String GPOS_WVCAS_REF_CASDATA_TIME = "STBRefCasDataTime";
    public static final String GPOS_DVR_MANAGEMENT_LIMIT = "DVRManagementLimit";
    public static final String GPOS_SI_NIT_ID = "SINitNetworkID";
    public static final String GPOS_OTA_MD5 = "OTAMD5";

    public static final String GPOS_BouquetIds = "BouquetIds_";
    public static final String GPOS_BouquetVers = "BouquetVers_";
    public static final String GPOS_BouquetNames = "BouquetNames_";
    public static final String GPOS_TUNER_OUTPUT_5V = "TunerOutput5V";
    public static final String GPOS_AREA_CODE = "AreaCode";

    public static final String GPOS_TUTORIALSETTING = "TutorialSetting";
    public static final String GPOS_STBDATA_RETURN = "STBDataReturn";
    public static final String GPOS_HDMI_CEC = "HdmiCecOnOff";
    public static final String GPOS_CAS_UPDATE_TIME = "CasUpdateTime";
    public static final String GPOS_CAS_OVERDUE_PAYMENT_URL = "CasOverduePaymentURL";
    public static final String GPOS_FTI_CREATE = "FTI_CREATE";

    public static final String CNS_CM_MODE = "CmMode";
    public static final String CNS_LIVE_TV_LAST_PLAY_CHANNEL_ID = "cns_live_tv_last_play_channel_id";
    public static final String CNA_SLEEP_TIMEOUT = "SleepTimeout";
    public static final String CNS_TVMAIL_URL_PREFIX = "TvMailUrlPrefix";
    public static final String CNS_LAUNCHER_TOKEN = "LauncherToken";
    public static final String CNS_TVMAIL_DEBUG_HEARTBEAT = "TvMailDebugHeartBeat";
    public static final String CNS_TVMAIL_HEARTBEAT = "TvMailHeartBeat";
    public static final String CNS_CURRENT_ROLE_TYPE = "CurrentRoleType";
    public static final String CNS_TUTORIAL_URL = "TutorialUrl";
    public static final String CNS_ADULT_SHOW = "AdultShow";
    public static final String CNS_CA_MAIL = "CaMail";
    public static final String CNS_SINGLE_C_DIALOG_SHOW = "SingleCDialogShow";
    public static final String CNS_LAST_WATCH_CHANNEL_URI = "LastWatchChannelUri";
    public static final String CNS_TIME_SETTINGS_ENABLE = "TimeSettingsEnable";
    public static final String CNS_CHANNEL_DATA_FILE_PATH = "ChannelDataFilePath";
    public static final String CNS_LAUNCHER_PROJECT_ID = "LauncherProjectId";
    public static final String CNS_LAU_CACHE_SHA_256 = "LauCacheSha256-";
    public static final String CNS_ALL_ROLE_TYPE = "AllRoleType";
    public static final String CNS_ALL_ROLE_TYPE_AND_LAUNCHER = "AllRoleTypeAndLauncher";
    public static final String CNS_ALL_CUSTOM_LIST_HOT_UPDATES = "AllCustomListHotUpdates";
    public static final String CNS_ALL_ROTATION_HOT_UPDATES = "AllRotationHotUpdates";
    public static final String CNS_PRE_ALL_ROLE_TYPE = "PreAllRoleType";
    public static final String CNS_ROLE_TYPE = "RoleType-";
    public static final String CNS_LAUNCHER_DOMAIN = "LauncherDomain";
    public static final String CNS_HAS_MESSAGE = "HasMessage";
    public static final String CNS_SCREEN_OFF_ENABLE = "ScreenOffEnable";
    public static final String CNS_SPEAKER_BIND_STATUS = "SpeakerBindStatus";
    public static final String CNS_LAST_LI_AD_SHOW_TIME = "LastLiAdShowTime";

    // standby redirect
    public static final int STANDBY_REDIRECT_NONE = 0;
    public static final int STANDBY_REDIRECT_LAUNCHER = 1;
    public static final int STANDBY_REDIRECT_LIVETV = 2;
    // parental rate define
    public static final int PARENTALRATE_DISABLE = 0;
    public static final int PARENTALRATE_SIX = 6;
    public static final int PARENTALRATE_TWELVE = 12;
    public static final int PARENTALRATE_SIXTEEN = 15;
    public static final int PARENTALRATE_EIGHTEEN = 18;
    public static final int PARENTALRATE_ALLBLOCKED = 99;

    public static final int MANAGEMENT_AUTO_CLEAR = 0;
    public static final int MANAGEMENT_MANUAL_CLEAR = 1;

    protected String DBVersion;
    protected long CurChannelId;
    protected int CurGroupType;
    protected int PasswordValue;
    protected int WorkerPasswordValue;
    protected int PurchasePasswordValue;
    protected int ParentalRate;
    protected int ParentalLockOnOff;
    protected int InstallLockOnOff;
    protected int BoxPowerStatus;
    protected long StartOnChannelId;
    protected int StartOnChType;
    protected int Volume;
    protected int AudioTrackMode;
    protected int AutoRegionTimeOffset;
    protected Float RegionTimeOffset;
    protected int RegionSummerTime;
    protected int LnbPower;
    protected int Screen16x9;
    protected int Conversion;
    protected int Resolution;
    protected String OSDLanguage;
    protected int SearchProgramType;
    protected int SearchMode;
    protected String[] AudioLanguageSelection = new String[2];
    protected String[] SubtitleLanguageSelection = new String[2];
    protected int SortByLcn;
    protected int OSDTransparency;
    protected int BannerTimeout;
    protected int HardHearing;
    protected int AutoStandbyTime;
    protected int DolbyMode;
    protected int HDCPOnOff;
    protected int DeepSleepMode;
    protected int SubtitleOnOff;
    protected int AvStopMode;
    protected int TimeshiftDuration;
    protected int RecordIconOnOff;
    protected int MailSettingsShopping;
    protected int MailSettingsNews;
    protected int MailSettingsPopular;
    protected int MailSettingsCoupon;
    protected int MailSettingsService;
    protected int Bat_version;
    protected int BatId;
    protected int ChannelLockCount;
    protected int TimeLockAllDay;
    protected int[] TimeLockPeriodStart = new int[3];
    protected int[] TimeLockPeriodEnd = new int[3];
    protected int Nit_version;
    protected int Network_ID;
    protected int SINitNetworkId;
    protected String ZipCode;
    protected int IntroductionTime;
    protected int PvrEnable;
    public static final int MAX_NUM_OF_IRD_COMMAND = 20;
    protected ArrayList<Long> IrdCommandList = new ArrayList<>();
    // protected List<Long> IrdCommandList = new ArrayList<>();
    protected String HomeId;
    protected String So;
    protected String Mobile;
    protected int StandbyRedirect; // 0,1 : from standby to Launcher , 2 : from standby to liveTV full screen
    protected int MailNotifyStatus; // 1: mail on, 2: mail off

    protected int SearchOneTPFlag;
    protected int isMute;
    protected String WVCasLicenseURL;
    protected int STBRefCasDataTime;
    protected int DVRManagementLimit;
    protected String ota_md5;
    protected int TunerOutput5V;
    protected String AreaCode;
    protected long mCasUpdateTime;

    public static final int MAX_NUM_OF_GROUP = Pvcfg.GROUP_NO;
    // protected List<Integer> BouquetIds = new ArrayList<>(MAX_NUM_OF_GROUP);
    // protected List<Integer> BouquetVers = new ArrayList<>(MAX_NUM_OF_GROUP);
    // protected List<String> BouquetNames = new ArrayList<>(MAX_NUM_OF_GROUP);
    protected int[] BouquetIds = new int[MAX_NUM_OF_GROUP];
    protected int[] BouquetVers = new int[MAX_NUM_OF_GROUP];
    protected String[] BouquetNames = new String[MAX_NUM_OF_GROUP];
    protected int CmMode;
    protected int TutorialSetting;
    protected int STBDataReturn;
    protected int HdmiCecOnOff;
    protected int SleepTimeout;
    protected String TvMailUrlPrefix;
    protected String LauncherToken;
    protected int TvMailDebugHeartBeat;
    protected int TvMailHeartBeat;
    protected String CurrentRoleType;
    protected String TutorialUrl;
    protected int AdultShow;
    protected int CaMail;
    protected int SingleCDialogShow;
    protected String LastWatchChannelUri;
    protected int TimeSettingsEnable;
    protected String ChannelDataFilePath;
    protected String LauncherProjectId;
    protected String LauCacheSha256;
    protected String AllRoleType;
    protected String AllRoleTypeAndLauncher;
    protected String AllCustomListHotUpdates;
    protected String AllRotationHotUpdates;
    protected String PreAllRoleType;
    protected String RoleType;
    protected String LauncherDomain;
    protected int HasMessage;
    protected int ScreenOffEnable;
    protected int SpeakerBindStatus;
    protected String LastLiAdShowTime;

    public GposInfo() {

    }

    public GposInfo(Parcel in) {
        Log.d(TAG, "GposInfo Parcel in = " + in);
        DBVersion = in.readString();
        CurChannelId = in.readLong();
        CurGroupType = in.readInt();
        PasswordValue = in.readInt();
        WorkerPasswordValue = in.readInt();
        PurchasePasswordValue = in.readInt();
        ParentalRate = in.readInt();
        ParentalLockOnOff = in.readInt();
        InstallLockOnOff = in.readInt();
        BoxPowerStatus = in.readInt();
        StartOnChannelId = in.readLong();
        StartOnChType = in.readInt();
        Volume = in.readInt();
        AudioTrackMode = in.readInt();
        AutoRegionTimeOffset = in.readInt();
        if (in.readByte() == 0) {
            RegionTimeOffset = null;
        } else {
            RegionTimeOffset = in.readFloat();
        }
        RegionSummerTime = in.readInt();
        LnbPower = in.readInt();
        Screen16x9 = in.readInt();
        Conversion = in.readInt();
        Resolution = in.readInt();
        OSDLanguage = in.readString();
        SearchProgramType = in.readInt();
        SearchMode = in.readInt();
        AudioLanguageSelection = in.createStringArray();
        SubtitleLanguageSelection = in.createStringArray();
        SortByLcn = in.readInt();
        OSDTransparency = in.readInt();
        BannerTimeout = in.readInt();
        HardHearing = in.readInt();
        AutoStandbyTime = in.readInt();
        DolbyMode = in.readInt();
        HDCPOnOff = in.readInt();
        DeepSleepMode = in.readInt();
        SubtitleOnOff = in.readInt();
        AvStopMode = in.readInt();
        TimeshiftDuration = in.readInt();
        RecordIconOnOff = in.readInt();
        MailSettingsShopping = in.readInt();
        MailSettingsNews = in.readInt();
        MailSettingsPopular = in.readInt();
        MailSettingsCoupon = in.readInt();
        MailSettingsService = in.readInt();
        Bat_version = in.readInt();
        BatId = in.readInt();
        ChannelLockCount = in.readInt();
        TimeLockAllDay = in.readInt();
        TimeLockPeriodStart = in.createIntArray();
        TimeLockPeriodEnd = in.createIntArray();
        Nit_version = in.readInt();
        Network_ID = in.readInt();
        SINitNetworkId = in.readInt();
        ZipCode = in.readString();
        IntroductionTime = in.readInt();
        PvrEnable = in.readInt();
        in.readList(IrdCommandList, Long.class.getClassLoader(), Long.class);
        HomeId = in.readString();
        So = in.readString();
        Mobile = in.readString();
        StandbyRedirect = in.readInt();
        MailNotifyStatus = in.readInt();
        SearchOneTPFlag = in.readInt();
        isMute = in.readInt();
        WVCasLicenseURL = in.readString();
        STBRefCasDataTime = in.readInt();
        DVRManagementLimit = in.readInt();
        ota_md5 = in.readString();
        TunerOutput5V = in.readInt();
        BouquetIds = in.createIntArray();
        BouquetVers = in.createIntArray();
        BouquetNames = in.createStringArray();
        AreaCode = in.readString();
        CmMode = in.readInt();
        TutorialSetting = in.readInt();
        STBDataReturn = in.readInt();
        HdmiCecOnOff = in.readInt();
        mCasUpdateTime = in.readLong();
        SleepTimeout = in.readInt();
        TvMailUrlPrefix = in.readString();
        LauncherToken = in.readString();
        TvMailDebugHeartBeat = in.readInt();
        TvMailHeartBeat = in.readInt();
        CurrentRoleType = in.readString();
        TutorialUrl = in.readString();
        AdultShow = in.readInt();
        CaMail = in.readInt();
        SingleCDialogShow = in.readInt();
        LastWatchChannelUri = in.readString();
        TimeSettingsEnable = in.readInt();
        ChannelDataFilePath = in.readString();
        LauncherProjectId = in.readString();
        LauCacheSha256 = in.readString();
        AllRoleType = in.readString();
        AllRoleTypeAndLauncher = in.readString();
        AllCustomListHotUpdates = in.readString();
        AllRotationHotUpdates = in.readString();
        PreAllRoleType = in.readString();
        RoleType = in.readString();
        LauncherDomain = in.readString();
        HasMessage = in.readInt();
        ScreenOffEnable = in.readInt();
        SpeakerBindStatus = in.readInt();
        LastLiAdShowTime = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.d(TAG, "writeToParcel ");
        dest.writeString(DBVersion);
        dest.writeLong(CurChannelId);
        dest.writeInt(CurGroupType);
        dest.writeInt(PasswordValue);
        dest.writeInt(WorkerPasswordValue);
        dest.writeInt(PurchasePasswordValue);
        dest.writeInt(ParentalRate);
        dest.writeInt(ParentalLockOnOff);
        dest.writeInt(InstallLockOnOff);
        dest.writeInt(BoxPowerStatus);
        dest.writeLong(StartOnChannelId);
        dest.writeInt(StartOnChType);
        dest.writeInt(Volume);
        dest.writeInt(AudioTrackMode);
        dest.writeInt(AutoRegionTimeOffset);
        if (RegionTimeOffset == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeFloat(RegionTimeOffset);
        }
        dest.writeInt(RegionSummerTime);
        dest.writeInt(LnbPower);
        dest.writeInt(Screen16x9);
        dest.writeInt(Conversion);
        dest.writeInt(Resolution);
        dest.writeString(OSDLanguage);
        dest.writeInt(SearchProgramType);
        dest.writeInt(SearchMode);
        dest.writeStringArray(AudioLanguageSelection);
        dest.writeStringArray(SubtitleLanguageSelection);
        dest.writeInt(SortByLcn);
        dest.writeInt(OSDTransparency);
        dest.writeInt(BannerTimeout);
        dest.writeInt(HardHearing);
        dest.writeInt(AutoStandbyTime);
        dest.writeInt(DolbyMode);
        dest.writeInt(HDCPOnOff);
        dest.writeInt(DeepSleepMode);
        dest.writeInt(SubtitleOnOff);
        dest.writeInt(AvStopMode);
        dest.writeInt(TimeshiftDuration);
        dest.writeInt(RecordIconOnOff);
        dest.writeInt(MailSettingsShopping);
        dest.writeInt(MailSettingsNews);
        dest.writeInt(MailSettingsPopular);
        dest.writeInt(MailSettingsCoupon);
        dest.writeInt(MailSettingsService);
        dest.writeInt(Bat_version);
        dest.writeInt(BatId);
        dest.writeInt(ChannelLockCount);
        dest.writeInt(TimeLockAllDay);
        dest.writeIntArray(TimeLockPeriodStart);
        dest.writeIntArray(TimeLockPeriodEnd);
        dest.writeInt(Nit_version);
        dest.writeInt(Network_ID);
        dest.writeInt(SINitNetworkId);
        dest.writeString(ZipCode);
        dest.writeInt(IntroductionTime);
        dest.writeInt(PvrEnable);
        dest.writeList(IrdCommandList);
        dest.writeString(HomeId);
        dest.writeString(So);
        dest.writeString(Mobile);
        dest.writeInt(StandbyRedirect);
        dest.writeInt(MailNotifyStatus);
        dest.writeInt(SearchOneTPFlag);
        dest.writeInt(isMute);
        dest.writeString(WVCasLicenseURL);
        dest.writeInt(STBRefCasDataTime);
        dest.writeInt(DVRManagementLimit);
        dest.writeString(ota_md5);
        dest.writeInt(TunerOutput5V);
        dest.writeIntArray(BouquetIds);
        dest.writeIntArray(BouquetVers);
        dest.writeStringArray(BouquetNames);
        dest.writeString(AreaCode);
        dest.writeInt(CmMode);
        dest.writeInt(TutorialSetting);
        dest.writeInt(STBDataReturn);
        dest.writeInt(HdmiCecOnOff);
        dest.writeLong(mCasUpdateTime);
        dest.writeInt(SleepTimeout);
        dest.writeString(TvMailUrlPrefix);
        dest.writeString(LauncherToken);
        dest.writeInt(TvMailDebugHeartBeat);
        dest.writeInt(TvMailHeartBeat);
        dest.writeString(CurrentRoleType);
        dest.writeString(TutorialUrl);
        dest.writeInt(AdultShow);
        dest.writeInt(CaMail);
        dest.writeInt(SingleCDialogShow);
        dest.writeString(LastWatchChannelUri);
        dest.writeInt(TimeSettingsEnable);
        dest.writeString(ChannelDataFilePath);
        dest.writeString(LauncherProjectId);
        dest.writeString(LauCacheSha256);
        dest.writeString(AllRoleType);
        dest.writeString(AllRoleTypeAndLauncher);
        dest.writeString(AllCustomListHotUpdates);
        dest.writeString(AllRotationHotUpdates);
        dest.writeString(PreAllRoleType);
        dest.writeString(RoleType);
        dest.writeString(LauncherDomain);
        dest.writeInt(HasMessage);
        dest.writeInt(ScreenOffEnable);
        dest.writeInt(SpeakerBindStatus);
        dest.writeString(LastLiAdShowTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GposInfo> CREATOR = new Creator<GposInfo>() {
        @Override
        public GposInfo createFromParcel(Parcel in) {
            Log.d(TAG, "createFromParcel Parcel= " + in);
            return new GposInfo(in);
        }

        @Override
        public GposInfo[] newArray(int size) {
            return new GposInfo[size];
        }
    };

    public String ToString() {
        String s = "[DBVersion : " + DBVersion + "CurChannelId :" + CurChannelId + "CurGroupType : " + CurGroupType
                + "PasswordValue : " + PasswordValue
                + "ParentalRate : " + ParentalRate + "ParentalLockOnOff : " + ParentalLockOnOff + "InstallLockOnOff : "
                + InstallLockOnOff + "BoxPowerStatus : " + BoxPowerStatus
                + "StartOnChannelId : " + StartOnChannelId + "StartOnChType : " + StartOnChType + "Volume : " + Volume
                + "AudioTrackMode : " + AudioTrackMode
                + "AutoRegionTimeOffset : " + AutoRegionTimeOffset
                + "LnbPower : " + LnbPower + "AutoRegionTimeOffset : " + AutoRegionTimeOffset + "RegionSummerTime : "
                + RegionSummerTime
                + "Screen16x9 : " + Screen16x9 + "Conversion : " + Conversion + "OSDLanguage : " + OSDLanguage
                + "SearchProgramType : " + SearchProgramType + "SearchMode : " + SearchMode
                + "AudioLanguageSelection[0] : " + AudioLanguageSelection[0] + "AudioLanguageSelection[1] : "
                + AudioLanguageSelection[1]
                + "SubtitleLanguageSelection[0] : " + SubtitleLanguageSelection[0] + "SubtitleLanguageSelection[1] : "
                + SubtitleLanguageSelection[1]
                + "SortByLcn : " + SortByLcn + "OSDTransparency : " + OSDTransparency + "BannerTimeout : "
                + BannerTimeout
                + "HardHearing : " + HardHearing + "AutoStandbyTime : " + AutoStandbyTime + "DolbyMode : " + DolbyMode
                + "HDCPOnOff : " + HDCPOnOff
                + "DeepSleepMode : " + DeepSleepMode + "AvStopMode : " + AvStopMode + "TimeShift Duration : "
                + TimeshiftDuration
                + "RecordIconOnOff : " + RecordIconOnOff + " MailSettingsShopping :" + MailSettingsShopping
                + " MailSettingsNews: " + MailSettingsNews
                + " MailSettingsPopular: " + MailSettingsPopular + " MailSettingsCoupon: " + MailSettingsCoupon
                + " MailSettingsService: " + MailSettingsService + " bat_id: " + BatId
                + "ChannelLockCount: " + ChannelLockCount + "TimeLockAllDay: " + TimeLockAllDay
                + "TimeLockPeriodStart[0] : " + TimeLockPeriodStart[0] + "TimeLockPeriodStart[1] : "
                + TimeLockPeriodStart[1] + "TimeLockPeriodStart[2] : " + TimeLockPeriodStart[2]
                + "TimeLockPeriodEnd[0] : " + TimeLockPeriodEnd[0] + "TimeLockPeriodEnd[1] : " + TimeLockPeriodEnd[1]
                + "TimeLockPeriodEnd[2] : " + TimeLockPeriodEnd[2]
                + " Nit_version: " + Nit_version + " Network_ID: " + Network_ID + " Bat_version: " + Bat_version
                + " BatId: " + BatId + " ZipCode: " + ZipCode + " PvrEnable: " + PvrEnable
                + " HomeId: " + HomeId + " So: " + So + " Mobile: " + Mobile + " StandbyRedirect: " + StandbyRedirect
                + " MailNotifyStatus: " + MailNotifyStatus + " DVRManagementLimit: " + DVRManagementLimit
                + " TunerOutput5V: " + TunerOutput5V + "]";
        for (int i = 0; i < MAX_NUM_OF_IRD_COMMAND; i++) {
            s += "IrdCommand_" + i + " : " + IrdCommandList.get(i) + " ";
        }
        for (int i = 0; i < MAX_NUM_OF_GROUP; i++) {
            s += "BouquetIds_" + i + " : " + BouquetIds[i] + " ";
        }
        for (int i = 0; i < MAX_NUM_OF_GROUP; i++) {
            s += "BouquetVers_" + i + " : " + BouquetVers[i] + " ";
        }
        for (int i = 0; i < MAX_NUM_OF_GROUP; i++) {
            s += "BouquetNames_" + i + " : " + BouquetNames[i] + " ";
        }
        s += " SearchOneTPFlag = "+SearchOneTPFlag;
        s += " WVCasLicenseURL = "+WVCasLicenseURL;
        s += " STBRefCasDataTime = "+STBRefCasDataTime;
        s += " AreaCode = "+AreaCode;
        s += " CasUpdateTime = "+mCasUpdateTime;
        s += "]";
        return s;
    }

    public static Uri get_gpop_uri() {
        String authority;
        if(Pvcfg.isPrimeDTVServiceEnable())
            authority = "com.prime.dtv.service.database.DVBContentProvider";// DVBContentProvider.AUTHORITY
        else
            authority = "com.prime.launcher.database.DVBContentProvider"; // DVBContentProvider.LAUNCHER_AUTHORITY

        return Uri.parse("content://" + authority + "/" + URI_PATH);
    }

    public static boolean set_gpos_provider_data(Context context, String key_name, String value) {
        boolean set_data_success = false ;
        String[] keyArr = {key_name};

        Uri GPOS_PROVIDER_CONTENT_URI = get_gpop_uri();
        Cursor query = context.getContentResolver().query(GPOS_PROVIDER_CONTENT_URI,
                new String[]{KEY, VALUE}, KEY + "=?", keyArr, null);

        if (query != null) {
            if (query.getCount() > 0) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(VALUE, value);
                set_data_success = context.getContentResolver().update(GPOS_PROVIDER_CONTENT_URI, contentValues, KEY + "=?", keyArr) > 0;
            } else {
                ContentValues contentValues2 = new ContentValues();
                contentValues2.put(KEY, key_name);
                contentValues2.put(VALUE, value);
                set_data_success = context.getContentResolver().insert(GPOS_PROVIDER_CONTENT_URI, contentValues2) != null;
            }
            query.close();
            return set_data_success;
        }
        return set_data_success;
    }

    public static String get_gpos_provider_data(Context context, String name, String defaultValue) {
        Uri GPOS_PROVIDER_CONTENT_URI = get_gpop_uri();
        String query_value = defaultValue ;
//        Log.d(TAG,"get_gpos_provider_data context = "+context);
        Cursor query = context.getContentResolver().query(GPOS_PROVIDER_CONTENT_URI,
                new String[]{KEY, VALUE}, KEY + "=?", new String[]{name}, null);
        if (query != null) {
            int count = query.getCount();
            if (count > 0) {
                query.moveToFirst();
                String query_key_name = query.getString(0);
                query_value = query.getString(1);
            }
            query.close();
        }

        return query_value;
    }

    public static int getNitId(Context context){
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_NIT_ID,  "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getNitId", e);
            return 0;
        }
    }

    public static void setNitId(Context context, int NitId){
        try {
            set_gpos_provider_data(context, GPOS_NIT_ID, String.valueOf(NitId));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setNitId", e);
        }
    }

    public static int getNitVersion(Context context){
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_NIT_VERSION, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getNitVersion", e);
            return 0;
        }
    }

    public static void setNitVersion(Context context, int NitVersion){
        try {
            set_gpos_provider_data(context, GPOS_NIT_VERSION, String.valueOf(NitVersion));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setNitVersion", e);
        }
    }

    public static int getBatVersion(Context context){
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_BAT_VERSION, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getBatVersion", e);
            return 0;
        }
    }

    public static void setBatVersion(Context context, int BatVersion){
        try {
            set_gpos_provider_data(context, GPOS_BAT_VERSION, String.valueOf(BatVersion));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setBatVersion", e);
        }
    }

    public static int getBatId(Context context){
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_BAT_ID, "25149"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getBatId", e);
            return 25149;
        }
    }

    public static void setBatId(Context context, int batId){
        try {
            set_gpos_provider_data(context, GPOS_BAT_ID, String.valueOf(batId));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setBatId", e);
        }
    }
    
    public static long getCurChannelId(Context context) {
        try {
            return Long.parseLong(get_gpos_provider_data(context, GPOS_CUR_CHANNELID, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getCurChannelId", e);
            return 0;
        }
    }

    public static void setCurChannelId(Context context, long curChannelId) {
        try {
            set_gpos_provider_data(context, GPOS_CUR_CHANNELID, String.valueOf(curChannelId));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setCurChannelId", e);
        }
    }

    public static int getCurGroupType(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_CUR_GROUP_TYPE, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getCurGroupType", e);
            return 0;
        }
    }

    public static void setCurGroupType(Context context, int curGroupType) {
        try {
            set_gpos_provider_data(context, GPOS_CUR_GROUP_TYPE, String.valueOf(curGroupType));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setCurGroupType", e);
        }
    }

    public static int getPasswordValue(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_PASSWORD_VALUE, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getPasswordValue", e);
            return 0;
        }
    }

    public static void setPasswordValue(Context context, int passwordValue) {
        try {
            set_gpos_provider_data(context, GPOS_PASSWORD_VALUE, String.valueOf(passwordValue));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setPasswordValue", e);
        }
    }

    public static int getWorkerPasswordValue(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_WORKER_PASSWORD_VALUE, "13197776"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getWorkerPasswordValue", e);
            return 13197776;
        }
    }

    public static void setWorkerPasswordValue(Context context, int workerPasswordValue) {
        try {
            set_gpos_provider_data(context, GPOS_WORKER_PASSWORD_VALUE, String.valueOf(workerPasswordValue));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setWorkerPasswordValue", e);
        }
    }

    public static int getPurchasePasswordValue(Context context){
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_PURCHASE_PASSWORD_VALUE, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getWorkerPasswordValue", e);
            return 0;
        }
    }

    public static void setPurchasePasswordValue(Context context, int PurchasePasswordValue){
        try {
            set_gpos_provider_data(context, GPOS_PURCHASE_PASSWORD_VALUE, String.valueOf(PurchasePasswordValue));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setPurchasePasswordValue", e);
        }
    }

    public static int getParentalRate(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_PARENTAL_RATE, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getParentalRate", e);
            return 0;
        }
    }

    public static void setParentalRate(Context context, int parentalRate) {
        try {
            set_gpos_provider_data(context, GPOS_PARENTAL_RATE, String.valueOf(parentalRate));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setParentalRate", e);
        }
    }

    public static int getParentalLockOnOff(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_PARENTAL_LOCK_ONOFF, "1"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getParentalLockOnOff", e);
            return 1;
        }
    }

    public static void setParentalLockOnOff(Context context, int parentalLockOnOff) {
        try {
            set_gpos_provider_data(context, GPOS_PARENTAL_LOCK_ONOFF, String.valueOf(parentalLockOnOff));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setParentalLockOnOff", e);
        }
    }

    public static int getInstallLockOnOff(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_INSTALL_LOCK_ONOFF, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getInstallLockOnOff", e);
            return 0;
        }
    }

    public static void setInstallLockOnOff(Context context, int installLockOnOff) {
        try {
            set_gpos_provider_data(context, GPOS_INSTALL_LOCK_ONOFF, String.valueOf(installLockOnOff));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setInstallLockOnOff", e);
        }
    }

    public static int getBoxPowerStatus(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_BOX_POWER_STATUS, "1"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getBoxPowerStatus", e);
            return 1;
        }
    }

    public static void setBoxPowerStatus(Context context, int boxPowerStatus) {
        try {
            set_gpos_provider_data(context, GPOS_BOX_POWER_STATUS, String.valueOf(boxPowerStatus));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setBoxPowerStatus", e);
        }
    }

    public static long getStartOnChannelId(Context context) {
        try {
            return Long.parseLong(get_gpos_provider_data(context, GPOS_START_ON_CHANNELID, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getStartOnChannelId", e);
            return 0;
        }
    }

    public static void setStartOnChannelId(Context context, long startOnChannelId) {
        try {
            set_gpos_provider_data(context, GPOS_START_ON_CHANNELID, String.valueOf(startOnChannelId));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setStartOnChannelId", e);
        }
    }

    public static int getStartOnChType(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_START_ON_CHTYPE, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getStartOnChType", e);
            return 0;
        }
    }

    public static void setStartOnChType(Context context, int startOnChType) {
        try {
            set_gpos_provider_data(context, GPOS_START_ON_CHTYPE, String.valueOf(startOnChType));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setStartOnChType", e);
        }
    }

    public static int getVolume(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_VOLUME, "15"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getVolume", e);
            return 15;
        }
    }

    public static void setVolume(Context context, int volume) {
        try {
            set_gpos_provider_data(context, GPOS_VOLUME, String.valueOf(volume));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setVolume", e);
        }
    }

    public static int getAudioTrackMode(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_AUDIO_TRACK_MODE, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getAudioTrackMode", e);
            return 0;
        }
    }

    public static void setAudioTrackMode(Context context, int audioTrackMode) {
        try {
            set_gpos_provider_data(context, GPOS_AUDIO_TRACK_MODE, String.valueOf(audioTrackMode));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setAudioTrackMode", e);
        }
    }

    public static float getRegionTimeOffset(Context context) {
        try {
            return Float.parseFloat(get_gpos_provider_data(context, GPOS_REGION_TIME_OFSSET, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getRegionTimeOffset", e);
            return 0;
        }
    }

    public static void setRegionTimeOffset(Context context, Float regionTimeOffset) {
        try {
            set_gpos_provider_data(context, GPOS_REGION_TIME_OFSSET, String.valueOf(regionTimeOffset));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setRegionTimeOffset", e);
        }
    }

    public static int getLnbPower(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_LNB_POWER, "1"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getLnbPower", e);
            return 1;
        }
    }

    public static void setLnbPower(Context context, int lnbPower) {
        try {
            set_gpos_provider_data(context, GPOS_LNB_POWER, String.valueOf(lnbPower));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setLnbPower", e);
        }
    }

    public static int getScreen16x9(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_SCREEN_16X9, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getScreen16x9", e);
            return 0;
        }
    }

    public static void setScreen16x9(Context context, int screen16x9) {
        try {
            set_gpos_provider_data(context, GPOS_SCREEN_16X9, String.valueOf(screen16x9));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setScreen16x9", e);
        }
    }

    public static String getOSDLanguage(Context context) {
        try {
            String defaultValue;
            if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG)
                    || Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_TBC)
                    || Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_CNS))
                defaultValue = "chi";
            else
                defaultValue = "eng";
            return get_gpos_provider_data(context, GPOS_OSD_LANGUAGE, defaultValue);
        } catch (Exception e) {
            Log.e(TAG, "Failed to getOSDLanguage", e);
            return "eng";
        }
    }

    public static void setOSDLanguage(Context context, String OSDLanguage) {
        try {
            set_gpos_provider_data(context, GPOS_OSD_LANGUAGE, OSDLanguage);
        } catch (Exception e) {
            Log.e(TAG, "Failed to setOSDLanguage", e);
        }
    }

    public static int getSearchProgramType(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_SEARCH_PROGRAM_TYPE, String.valueOf(TVScanParams.SEARCH_OPTION_ALL)));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getSearchProgramType", e);
            return 0;
        }
    }

    public static void setSearchProgramType(Context context, int searchProgramType) {
        try {
            set_gpos_provider_data(context, GPOS_SEARCH_PROGRAM_TYPE, String.valueOf(searchProgramType));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setSearchProgramType", e);
        }
    }

    public static int getSearchMode(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_SEARCH_MODE, String.valueOf(TVScanParams.SEARCH_OPTION_ALL)));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getSearchMode", e);
            return 0;
        }
    }

    public static void setSearchMode(Context context, int searchMode) {
        try {
            set_gpos_provider_data(context, GPOS_SEARCH_MODE, String.valueOf(searchMode));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setSearchMode", e);
        }
    }

    public static String getAudioLanguageSelection(Context context, int index) {
        try {
            if (index == 0)
                return get_gpos_provider_data(context, GPOS_AUDIO_LANG_SELECT_1, "chi");
            else
                return get_gpos_provider_data(context, GPOS_AUDIO_LANG_SELECT_2, "eng");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getAudioLanguageSelection", e);
            return "chi";
        }
    }

    public static void setAudioLanguageSelection(Context context, int index, String audioLanguageSelection) {
        try {
            if (index == 0)
                set_gpos_provider_data(context, GPOS_AUDIO_LANG_SELECT_1, audioLanguageSelection);
            else
                set_gpos_provider_data(context, GPOS_AUDIO_LANG_SELECT_2, audioLanguageSelection);
        } catch (Exception e) {
            Log.e(TAG, "Failed to setAudioLanguageSelection", e);
        }
    }

    public static String getSubtitleLanguageSelection(Context context, int index) {
        try {
            if (index == 0)
                return get_gpos_provider_data(context, GPOS_SUBTITLE_LANG_SELECT_1, "chi");
            else
                return get_gpos_provider_data(context, GPOS_SUBTITLE_LANG_SELECT_2, "eng");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getSubtitleLanguageSelection", e);
            return "chi";
        }
    }

    public static void setSubtitleLanguageSelection(Context context, int index, String subtitleLanguageSelection) {
        try {
            if (index == 0)
                set_gpos_provider_data(context, GPOS_SUBTITLE_LANG_SELECT_1, subtitleLanguageSelection);
            else
                set_gpos_provider_data(context, GPOS_SUBTITLE_LANG_SELECT_2, subtitleLanguageSelection);
        } catch (Exception e) {
            Log.e(TAG, "Failed to setSubtitleLanguageSelection", e);
        }
    }

    public static int getSortByLcn(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_SORT_BY_LCN, "1"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getSortByLcn", e);
            return 1;
        }
    }

    public static void setSortByLcn(Context context, int sortByLcn) {
        try {
            set_gpos_provider_data(context, GPOS_SORT_BY_LCN, String.valueOf(sortByLcn));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setSortByLcn", e);
        }
    }

    public static int getOSDTransparency(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_OSD_TRANSPARENCY, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getOSDTransparency", e);
            return 0;
        }
    }

    public static void setOSDTransparency(Context context, int OSDTransparency) {
        try {
            set_gpos_provider_data(context, GPOS_OSD_TRANSPARENCY, String.valueOf(OSDTransparency));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setOSDTransparency", e);
        }
    }

    public static int getBannerTimeout(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_BANNER_TIMEOUT, "5"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getBannerTimeout", e);
            return 5;
        }
    }

    public static void setBannerTimeout(Context context, int bannerTimeout) {
        try {
            set_gpos_provider_data(context, GPOS_BANNER_TIMEOUT, String.valueOf(bannerTimeout));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setBannerTimeout", e);
        }
    }

    public static int getHardHearing(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_HARD_HEARING, "1"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getHardHearing", e);
            return 1;
        }
    }

    public static void setHardHearing(Context context, int hardHearing) {
        try {
            set_gpos_provider_data(context, GPOS_BANNER_TIMEOUT, String.valueOf(hardHearing));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setHardHearing", e);
        }
    }

    public static int getAutoStandbyTime(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_AUTO_STANDBY_TIME, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getAutoStandbyTime", e);
            return 0;
        }
    }

    public static void setAutoStandbyTime(Context context, int autoStandbyTime) {
        try {
            set_gpos_provider_data(context, GPOS_AUTO_STANDBY_TIME, String.valueOf(autoStandbyTime));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setAutoStandbyTime", e);
        }
    }

    public static int getDolbyMode(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_DOLBY_MODE, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getDolbyMode", e);
            return 0;
        }
    }

    public static void setDolbyMode(Context context, int dolbyMode) {
        try {
            set_gpos_provider_data(context, GPOS_DOLBY_MODE, String.valueOf(dolbyMode));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setDolbyMode", e);
        }
    }

    public static int getHDCPOnOff(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_HDCP_ONOFF, "1"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getHDCPOnOff", e);
            return 1;
        }
    }

    public static void setHDCPOnOff(Context context, int HDCPOnOff) {
        try {
            set_gpos_provider_data(context, GPOS_HDCP_ONOFF, String.valueOf(HDCPOnOff));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setHDCPOnOff", e);
        }
    }

    public static int getDeepSleepMode(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_DEEP_SLEEP_MODE, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getDeepSleepMode", e);
            return 0;
        }
    }

    public static void setDeepSleepMode(Context context, int deepSleepMode) {
        try {
            set_gpos_provider_data(context, GPOS_DEEP_SLEEP_MODE, String.valueOf(deepSleepMode));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setDeepSleepMode", e);
        }
    }

    public static int getSubtitleOnOff(Context context) {
        try {
            String defaultValue;
            if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG)
                    || Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_TBC)
                    || Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_CNS))
                defaultValue = "1";
            else
                defaultValue = "0";

            return Integer.parseInt(get_gpos_provider_data(context, GPOS_SUBTITLE_ONOFF, defaultValue));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getSubtitleOnOff", e);
            return 1;
        }
    }

    public static void setSubtitleOnOff(Context context, int subtitleOnOff) {
        try {
            set_gpos_provider_data(context, GPOS_SUBTITLE_ONOFF, String.valueOf(subtitleOnOff));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setSubtitleOnOff", e);
        }
    }

    public static int getConversion(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_CONVERSION, "1"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getConversion", e);
            return 1;
        }
    }

    public static void setConversion(Context context, int conversion) {
        try {
            set_gpos_provider_data(context, GPOS_CONVERSION, String.valueOf(conversion));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setConversion", e);
        }
    }

    public static String getDBVersion(Context context) {
        try {
            int tunerType = Pvcfg.getTunerType();
            String dbver = "";
            if(tunerType == TpInfo.DVBC)
                dbver = "C";
            else if(tunerType == TpInfo.DVBT)
                dbver = "T";
            else if(tunerType == TpInfo.ISDBT)
                dbver = "I";
            else if(tunerType == TpInfo.DVBS)
                dbver = "S";

            return get_gpos_provider_data(context, GPOS_DB_VSERSION, dbver +"1.0");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getDBVersion", e);
            return "1.0";
        }
    }

    public static void setDBVersion(Context context, String DBVersion) {
        try {
            set_gpos_provider_data(context, GPOS_DB_VSERSION, DBVersion);
        } catch (Exception e) {
            Log.e(TAG, "Failed to setDBVersion", e);
        }
    }

    public static int getAutoRegionTimeOffset(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_AUTO_REGION_TIME_OFFSET, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getAutoRegionTimeOffset", e);
            return 0;
        }
    }

    public static void setAutoRegionTimeOffset(Context context, int autoRegionTimeOffset) {
        try {
            set_gpos_provider_data(context, GPOS_AUTO_REGION_TIME_OFFSET, String.valueOf(autoRegionTimeOffset));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setAutoRegionTimeOffset", e);
        }
    }

    public static int getRegionSummerTime(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_REGION_SUMMER_TIME, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getRegionSummerTime", e);
            return 0;
        }
    }

    public static void setRegionSummerTime(Context context, int regionSummerTime) {
        try {
            set_gpos_provider_data(context, GPOS_REGION_SUMMER_TIME, String.valueOf(regionSummerTime));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setRegionSummerTime", e);
        }
    }

    public static int getResolution(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_RESOLUTION, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getResolution", e);
            return 0;
        }
    }

    public static void setResolution(Context context, int resolution) {
        try {
            set_gpos_provider_data(context, GPOS_RESOLUTION, String.valueOf(resolution));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setResolution", e);
        }
    }

    public static int getAvStopMode(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_AV_STOP_MODE, "1"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getAvStopMode", e);
            return 1;
        }
    }

    public static void setAvStopMode(Context context, int avStopMode) {
        try {
            set_gpos_provider_data(context, GPOS_AV_STOP_MODE, String.valueOf(avStopMode));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setAvStopMode", e);
        }
    }

    public static int getTimeshiftDuration(Context context){
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_TIMESHIFT_DURATION, "5400"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getTimeshiftDuration", e);
            return 5400;
        }
    }

    public static void setTimeshiftDuration(Context context, int duration){
        try {
            set_gpos_provider_data(context, GPOS_TIMESHIFT_DURATION, String.valueOf(duration));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setTimeshiftDuration", e);
        }
    }

    public static int getRecordIconOnOff(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_RECORD_ICON_ONOFF, "1"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getRecordIconOnOff", e);
            return 1;
        }
    }

    public static void setRecordIconOnOff(Context context, int recordIconOnOff) {
        try {
            set_gpos_provider_data(context, GPOS_RECORD_ICON_ONOFF, String.valueOf(recordIconOnOff));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setRecordIconOnOff", e);
        }
    }

    public static int getMailSettingsShopping(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_MAIL_SETTINGS_SHOPPING, "1"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getRecordIconOnOff", e);
            return 1;
        }
    }

    public static void setMailSettingsShopping(Context context, int mailSettingsShopping) {
        try {
            set_gpos_provider_data(context, GPOS_MAIL_SETTINGS_SHOPPING, String.valueOf(mailSettingsShopping));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setMailSettingsShopping", e);
        }
    }

    public static int getMailSettingsNews(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_MAIL_SETTINGS_NEWS, "1"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getMailSettingsNews", e);
            return 1;
        }
    }

    public static void setMailSettingsNews(Context context, int mailSettingsNews) {
        try {
            set_gpos_provider_data(context, GPOS_MAIL_SETTINGS_NEWS, String.valueOf(mailSettingsNews));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setMailSettingsNews", e);
        }
    }

    public static int getMailSettingsPopular(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_MAIL_SETTINGS_POPULAR, "1"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getMailSettingsPopular", e);
            return 1;
        }
    }

    public static void setMailSettingsPopular(Context context, int mailSettingsPopular) {
        try {
            set_gpos_provider_data(context, GPOS_MAIL_SETTINGS_POPULAR, String.valueOf(mailSettingsPopular));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setMailSettingsPopular", e);
        }
    }

    public static int getMailSettingsCoupon(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_MAIL_SETTINGS_COUPON, "1"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getMailSettingsCoupon", e);
            return 1;
        }
    }

    public static void setMailSettingsCoupon(Context context, int mailSettingsCoupon) {
        try {
            set_gpos_provider_data(context, GPOS_MAIL_SETTINGS_COUPON, String.valueOf(mailSettingsCoupon));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setMailSettingsCoupon", e);
        }
    }

    public static int getMailSettingsService(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_MAIL_SETTINGS_SERVICE, "1"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getMailSettingsService", e);
            return 1;
        }
    }

    public static void setMailSettingsService(Context context, int mailSettingsService) {
        try {
            set_gpos_provider_data(context, GPOS_MAIL_SETTINGS_SERVICE, String.valueOf(mailSettingsService));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setMailSettingsService", e);
        }
    }

    public static long getCasUpdateTime(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_CAS_UPDATE_TIME, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getCasUpdateTime", e);
            return 0;
        }
    }

    public static void setCasUpdateTime(Context context, long casUpdateTime) {
        try {
            set_gpos_provider_data(context, GPOS_CAS_UPDATE_TIME, String.valueOf(casUpdateTime));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setCasUpdateTime", e);
        }
    }


    public static int getChannelLockCount(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_CHANNEL_LOCK_COUNT, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getChannelLockCount", e);
            return 0;
        }
    }

    public static void setChannelLockCount(Context context, int channelLockCount) {
        try {
            set_gpos_provider_data(context, GPOS_CHANNEL_LOCK_COUNT, String.valueOf(channelLockCount));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setChannelLockCount", e);
        }
    }

    public static int getTimeLockAllDay(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_TIME_LOCK_ALL_DAY, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getTimeLockAllDay", e);
            return 0;
        }
    }

    public static void setTimeLockAllDay(Context context, int timeLockAllDay) {
        try {
            set_gpos_provider_data(context, GPOS_TIME_LOCK_ALL_DAY, String.valueOf(timeLockAllDay));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setTimeLockAllDay", e);
        }
    }

    public static int getTimeLockPeriodStart(Context context, int index) {
        try {
            if (index == 0)
                return Integer.parseInt(get_gpos_provider_data(context, GPOS_TIME_LOCK_PERIOD_1_START, "-1"));
            else if (index == 1)
                return Integer.parseInt(get_gpos_provider_data(context, GPOS_TIME_LOCK_PERIOD_2_START, "-1"));
            else if (index == 2)
                return Integer.parseInt(get_gpos_provider_data(context, GPOS_TIME_LOCK_PERIOD_3_START, "-1"));
            return -1;
        } catch (Exception e) {
            Log.e(TAG, "Failed to getTimeLockPeriodStart", e);
            return -1;
        }
    }

    public static void setTimeLockPeriodStart(Context context, int index, int value) {
        try {
            if (index == 0)
                set_gpos_provider_data(context, GPOS_TIME_LOCK_PERIOD_1_START, String.valueOf(value));
            else if (index == 1)
                set_gpos_provider_data(context, GPOS_TIME_LOCK_PERIOD_2_START, String.valueOf(value));
            else if (index == 2)
                set_gpos_provider_data(context, GPOS_TIME_LOCK_PERIOD_3_START, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setTimeLockPeriodStart", e);
        }
    }

    public static int getTimeLockPeriodEnd(Context context, int index) {
        try {
            if (index == 0)
                return Integer.parseInt(get_gpos_provider_data(context, GPOS_TIME_LOCK_PERIOD_1_END, "-1"));
            else if (index == 1)
                return Integer.parseInt(get_gpos_provider_data(context, GPOS_TIME_LOCK_PERIOD_2_END, "-1"));
            else if (index == 2)
                return Integer.parseInt(get_gpos_provider_data(context, GPOS_TIME_LOCK_PERIOD_3_END, "-1"));
            return -1;
        } catch (Exception e) {
            Log.e(TAG, "Failed to getTimeLockPeriodEnd", e);
            return -1;
        }
    }

    public static void setTimeLockPeriodEnd(Context context, int index, int value) {
        try {
            if (index == 0)
                set_gpos_provider_data(context, GPOS_TIME_LOCK_PERIOD_1_END, String.valueOf(value));
            else if (index == 1)
                set_gpos_provider_data(context, GPOS_TIME_LOCK_PERIOD_2_END, String.valueOf(value));
            else if (index == 2)
                set_gpos_provider_data(context, GPOS_TIME_LOCK_PERIOD_3_END, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setTimeLockPeriodEnd", e);
        }
    }

    public static void setZipCode(Context context, String zipCode) {
        try {
            set_gpos_provider_data(context, GPOS_ZIPCODE, String.valueOf(zipCode));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setZipCode", e);
        }
    }

    public static String getZipCode(Context context) {
        try {
            return get_gpos_provider_data(context, GPOS_ZIPCODE, "0000");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getZipCode", e);
            return "0000";
        }
    }

    public static void setIntroductionTime(Context context, int introductionTime) {
        try {
            set_gpos_provider_data(context, GPOS_INTRODUCTION_TIME, String.valueOf(introductionTime));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setIntroductionTime", e);
        }
    }

    public static int getIntroductionTime(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_INTRODUCTION_TIME, "5000"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getIntroductionTime", e);
            return 5000;
        }
    }

    public static int getPvrEnable(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_PVRENABLE, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getIntroductionTime", e);
            return 0;
        }
    }

    public static void setPvrEnable(Context context, int pvrEnable) {
        try {
            set_gpos_provider_data(context, GPOS_PVRENABLE, String.valueOf(pvrEnable));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setPvrEnable", e);
        }
    }

    public static long getIrdCommand(Context context, int index){
        try {
            if(index >= MAX_NUM_OF_IRD_COMMAND) {
                LogUtils.e("IrdCommandList Out of index");
                return 0;
            }
            return Long.parseLong(get_gpos_provider_data(context, GPOS_IrdCommand + index, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getIrdCommand", e);
            return 0;
        }
        /*if(index < IrdCommandList.size())
        return IrdCommandList.get(index);
        else
            return 0;*/
    }

    public static void setIrdCommand(Context context, int index, long value){
        try {
            set_gpos_provider_data(context, GPOS_IrdCommand + index, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setIrdCommand", e);
        }
        /*if(index == MAX_NUM_OF_IRD_COMMAND){
            LogUtils.e("IrdCommandList Out of index");
            IrdCommandList.remove(0);
            IrdCommandList.add(value);
            return;
        }
        IrdCommandList.add(index, value);*/
    }

    public static int getBouquetIds(Context context, int index){
        try {
            if((index >= MAX_NUM_OF_GROUP) || (index < 0)) {
                LogUtils.e("BouquetIds Out of index");
                return -1;
            }
            else
                return Integer.parseInt(get_gpos_provider_data(context, GPOS_BouquetIds + index, "-1"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getBouquetIds", e);
            return -1;
        }
    }

    public static boolean setBouquetIds(Context context, int index, int value){
        try {
            if((index >= MAX_NUM_OF_GROUP) || (index < 0)) {
                LogUtils.e("BouquetIds Out of index");
                return false;
            }
            else {
                set_gpos_provider_data(context, GPOS_BouquetIds + index, String.valueOf(value));
                //BouquetIds[index] = value;
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to setBouquetIds", e);
            return false;
        }
    }

    public static void initBouquetIds(Context context){
        try {
            int index;
            for(index=0;index < MAX_NUM_OF_GROUP;index++){
                set_gpos_provider_data(context, GPOS_BouquetIds + index, "-1");
                //BouquetIds[index]=-1;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initBouquetIds", e);
        }
    }

    public static int getBouquetVers(Context context, int index){
        try {
            if((index >= MAX_NUM_OF_GROUP) || (index < 0)) {
                LogUtils.e("BouquetVers Out of index");
                return 0xFF;
            }
            else
                //return BouquetVers[index];
                return Integer.parseInt(get_gpos_provider_data(context, GPOS_BouquetVers + index, "0xFF"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getBouquetVers", e);
            return 0xFF;
        }


    }

    public static boolean setBouquetVers(Context context, int index, int value){
        try {
            if((index >= MAX_NUM_OF_GROUP) || (index < 0)) {
                LogUtils.e("BouquetVers Out of index");
                return false;
            }
            else {
                set_gpos_provider_data(context, GPOS_BouquetVers + index, String.valueOf(value));
                //BouquetVers[index] = value;
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to setBouquetVers", e);
            return false;
        }
    }

    public static void initBouquetVers(Context context){
        try {
            int index;
            for(index=0;index < MAX_NUM_OF_GROUP;index++){
                set_gpos_provider_data(context, GPOS_BouquetVers + index, "0xFF");
                //BouquetVers[index]=0xFF;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initBouquetVers", e);
        }
    }

    public static String getBouquetNames(Context context, int index){
        if((index >= MAX_NUM_OF_GROUP) || (index < 0)) {
            LogUtils.e("BouquetNames Out of index");
            return "";
        }
        else
            //return BouquetNames[index];
            return get_gpos_provider_data(context, GPOS_BouquetNames + index, "");
    }

    public static boolean setBouquetNames(Context context, int index, String string){
        try {
            if((index >= MAX_NUM_OF_GROUP) || (index < 0)) {
                LogUtils.e("BouquetNames Out of index");
                return false;
            }
            else {
                //BouquetNames[index] = string;
                set_gpos_provider_data(context, GPOS_BouquetNames + index, string);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to setBouquetNames", e);
            return false;
        }
    }

    public static void initBouquetNames(Context context){
        try {
            int index;
            for(index=0;index < MAX_NUM_OF_GROUP;index++){
                //BouquetNames[index]="";
                set_gpos_provider_data(context, GPOS_BouquetNames + index, String.valueOf(""));
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initBouquetNames", e);
        }
    }

    public static void setIrdCommand(Context context, long value){
        Log.e(TAG, "setIrdCommand: do not use this function");
        //set_gpos_provider_data(context, GPOS_IrdCommand, String.valueOf(value));
        //IrdCommandList.add(value);
    }

    public static String getHomeId(Context context) {
        try {
            return get_gpos_provider_data(context, GPOS_HOME_ID, "0");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getHomeId", e);
            return "0";
        }
    }

    public static void setHomeId(Context context, String homeId) {
        try {
            set_gpos_provider_data(context, GPOS_HOME_ID, homeId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to setHomeId", e);
        }
    }

    public static String getSo(Context context) {
        try {
            return get_gpos_provider_data(context, GPOS_SO, "00");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getHomeId", e);
            return "0";
        }
    }

    public static void setSo(Context context, String so) {
        try {
            set_gpos_provider_data(context, GPOS_SO, so);
        } catch (Exception e) {
            Log.e(TAG, "Failed to setSo", e);
        }
    }

    public static String getMobile(Context context) {
        try {
            return get_gpos_provider_data(context, GPOS_MOBILE, "");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getMobile", e);
            return "";
        }
    }

    public static void setMobile(Context context, String mobile) {
        try {
            set_gpos_provider_data(context, GPOS_MOBILE, mobile);
        } catch (Exception e) {
            Log.e(TAG, "Failed to setMobile", e);
        }
    }

    public static int getStandbyRedirect(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_STANDBY_REDIRECT, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getStandbyRedirect", e);
            return 0;
        }
    }

    public static void setStandbyRedirect(Context context, int standbyRedirect) {
        try {
            set_gpos_provider_data(context, GPOS_STANDBY_REDIRECT, String.valueOf(standbyRedirect));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setStandbyRedirect", e);
        }
    }

    public static int getMailNotifyStatus(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_MAIL_NOTIFY_STATUS, "1"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getStandbyRedirect", e);
            return 1;
        }
    }

    public static void setMailNotifyStatus(Context context, int mailNotifyStatus) {
        try {
            set_gpos_provider_data(context, GPOS_MAIL_NOTIFY_STATUS, String.valueOf(mailNotifyStatus));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setMailNotifyStatus", e);
        }
    }

    public static int getSearchOneTPFlag(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_SEARCH_ONE_TP_FLAG, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getStandbyRedirect", e);
            return 0;
        }
    }

    public static void setSearchOneTPFlag(Context context, int flag) {
        try {
            set_gpos_provider_data(context, GPOS_SEARCH_ONE_TP_FLAG, String.valueOf(flag));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setSearchOneTPFlag", e);
        }
    }

    public static int getIsMute(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_ISMUTE, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getIsMute", e);
            return 0;
        }
    }

    public static void setIsMute(Context context, int isMute) {
        try {
            set_gpos_provider_data(context, GPOS_ISMUTE, String.valueOf(isMute));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setIsMute", e);
        }
    }

    public static int getTunerOutput5V(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_TUNER_OUTPUT_5V, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getTunerOutput5V", e);
            return 0;
        }
    }

    public void setTunerOutput5V(Context context, int onOff) {
        try {
            set_gpos_provider_data(context, GPOS_TUNER_OUTPUT_5V, String.valueOf(onOff));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setTunerOutput5V", e);
        }
    }

    public static String getAreadCode(Context context){
        try {
            return get_gpos_provider_data(context, GPOS_AREA_CODE, "0");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getAreadCode", e);
            return "0";
        }
    }

    public static void setAreaCode(Context context, String areacode){
        try {
            set_gpos_provider_data(context, GPOS_AREA_CODE, String.valueOf(areacode));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setAreaCode", e);
        }
    }

    public static void setWVCasLicenseURL(Context context, String WVCasLicenseURL) {
        try {
            set_gpos_provider_data(context, GPOS_WVCAS_LICENSE_URL, String.valueOf(WVCasLicenseURL));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setWVCasLicenseURL", e);
        }
    }

    public static String getWVCasLicenseURL(Context context) {
        try {
            String wVCasLicenseURL = get_gpos_provider_data(context, GPOS_WVCAS_LICENSE_URL, "");
            if(wVCasLicenseURL.length() == 0)
                wVCasLicenseURL = Pvcfg.CNS_LABDR_SERVER;
            return wVCasLicenseURL;
        } catch (Exception e) {
            Log.e(TAG, "Failed to getWVCasLicenseURL", e);
            return Pvcfg.CNS_LABDR_SERVER;
        }
    }

    public static void setSTBRefCasDataTime(Context context, int time) {
        try {
            set_gpos_provider_data(context, GPOS_WVCAS_REF_CASDATA_TIME, String.valueOf(time));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setSTBRefCasDataTime", e);
        }
    }

    public static int getSTBRefCasDataTime(Context context) {
        try {
            if(Pvcfg.getFixedRefreshTime() != 0){
                return Pvcfg.getFixedRefreshTime();
            }
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_WVCAS_REF_CASDATA_TIME, "28800"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getSTBRefCasDataTime", e);
            return 28800;
        }
    }

    public static void resetTimeLockPeriods(Context context) {
        // reset time lock period to -1
        try {
            set_gpos_provider_data(context, GPOS_TIME_LOCK_PERIOD_1_START, "-1");
            set_gpos_provider_data(context, GPOS_TIME_LOCK_PERIOD_1_END, "-1");
            set_gpos_provider_data(context, GPOS_TIME_LOCK_PERIOD_2_START, "-1");
            set_gpos_provider_data(context, GPOS_TIME_LOCK_PERIOD_2_END, "-1");
            set_gpos_provider_data(context, GPOS_TIME_LOCK_PERIOD_3_START, "-1");
            set_gpos_provider_data(context, GPOS_TIME_LOCK_PERIOD_3_END, "-1");
        } catch (Exception e) {
            Log.e(TAG, "Failed to resetTimeLockPeriods", e);
        }
        //Arrays.fill(TimeLockPeriodStart, -1);
        //Arrays.fill(TimeLockPeriodEnd, -1);
    }

    public static void setDVRManagementLimit(Context context, int dvrManagementLimit) {
        try {
            set_gpos_provider_data(context, GPOS_DVR_MANAGEMENT_LIMIT, String.valueOf(dvrManagementLimit));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setDVRManagementLimit", e);
        }
    }

    public static int getDVRManagementLimit(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_DVR_MANAGEMENT_LIMIT, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getSTBRefCasDataTime", e);
            return 0;
        }
    }

    public static int getSINitNetworkId(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_SI_NIT_ID, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getSINitNetworkId", e);
            return 0;
        }
    }

    public static void setSINitNetworkId(Context context, int SINitNetworkId) {
        try {
            set_gpos_provider_data(context, GPOS_SI_NIT_ID, String.valueOf(SINitNetworkId));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setSINitNetworkId", e);
        }
    }

    public static String getOta_md5(Context context) {
        try {
            return get_gpos_provider_data(context, GPOS_OTA_MD5, "0");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getOta_md5", e);
            return "0";
        }
    }

    public static void setOta_md5(Context context, String ota_md5) {
        try {
            set_gpos_provider_data(context, GPOS_OTA_MD5, String.valueOf(ota_md5));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setOta_md5", e);
        }
    }

    public static int getCmMode(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, CNS_CM_MODE, "1"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getCmMode", e);
            return 1;
        }
    }

    public static void setCmMode(Context context, int mode) {
        try {
            set_gpos_provider_data(context, CNS_CM_MODE, String.valueOf(mode));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setCmMode", e);
        }
    }

    public static int getTutorialSetting(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_TUTORIALSETTING, "1"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getTutorialSetting", e);
            return 0;
        }
    }

    public static void setTutorialSetting(Context context, int value) {
        try {
            set_gpos_provider_data(context, GPOS_TUTORIALSETTING, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setTutorialSetting", e);
        }
    }

    public static int getSTBDataReturn(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_STBDATA_RETURN, "1"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getSTBDataReturn", e);
            return 1;
        }
    }

    public static void setSTBDataReturn(Context context, int value) {
        try {
            set_gpos_provider_data(context, GPOS_STBDATA_RETURN, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setSTBDataReturn", e);
        }
    }

    public static int getHdmiCecOnOff(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_HDMI_CEC, "1"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getHdmiCecOnOff", e);
            return 1;
        }
    }

    public static void setHdmiCecOnOff(Context context, int value) {
        try {
            set_gpos_provider_data(context, GPOS_HDMI_CEC, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setHdmiCecOnOff", e);
        }
    }

    public static void setLastPlayChannelId(Context context, String value) {
        try {
            set_gpos_provider_data(context, CNS_LIVE_TV_LAST_PLAY_CHANNEL_ID, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setLastPlayChannelId", e);
        }
    }

    public static String getLastPlayChannelId(Context context) {
        try {
            return get_gpos_provider_data(context, GPOS_HDMI_CEC, null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to getLastPlayChannelId", e);
            return null;
        }
    }

    public static void setCNSMenuSettingLang(Context context, String Lang) {
        try {
            set_gpos_provider_data(context, "cns_menu_setting_lang", String.valueOf(Lang));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setCNSMenuSettingLang", e);
        }
    }

    public static String getCNSMenuSettingLang(Context context) {
        try {
            return get_gpos_provider_data(context, "cns_menu_setting_lang", null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to getCNSMenuSettingLang", e);
            return null;
        }
    }

    public static void setCNSOverduePaymentURL(Context context, String value) {
        try {
            set_gpos_provider_data(context, GPOS_CAS_OVERDUE_PAYMENT_URL, value);
        } catch (Exception e) {
            Log.e(TAG, "Failed to setCNSOverduePaymentURL", e);
        }
    }

    public static String getCNSOverduePaymentURL(Context context) {
        try {
            return get_gpos_provider_data(context, GPOS_CAS_OVERDUE_PAYMENT_URL, "");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getCNSOverduePaymentURL", e);
        }
        return "";
    }

    //CNS property
    public static int getSleepTimeout(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, CNA_SLEEP_TIMEOUT, "-1"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getSleepTimeout", e);
            return -1;
        }
    }

    public static void setSleepTimeout(Context context, int value) {
        try {
            set_gpos_provider_data(context, CNA_SLEEP_TIMEOUT, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setSleepTimeout", e);
        }
    }

    public static String getTvMailUrlPrefix(Context context) {
        try {
            return get_gpos_provider_data(context, CNS_TVMAIL_URL_PREFIX, "");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getTvMailUrlPrefix", e);
            return "";
        }
    }

    public static void setFTI_CREATE(Context context, int value) {
        try {
            set_gpos_provider_data(context, GPOS_FTI_CREATE, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setFTI_CREATE", e);
        }
    }

    public static int getFTI_CREATE(Context context,int defaultValue) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, GPOS_FTI_CREATE, defaultValue + ""));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getFTI_CREATE", e);
            return defaultValue;
        }
    }

    public static void setTvMailUrlPrefix(Context context, String value) {
        try {
            set_gpos_provider_data(context, CNS_TVMAIL_URL_PREFIX, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setTvMailUrlPrefix", e);
        }
    }

    public static String getLauncherToken(Context context) {
        try {
            return get_gpos_provider_data(context, CNS_LAUNCHER_TOKEN, "");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getLauncherToken", e);
            return "";
        }
    }

    public static void setLauncherToken(Context context, String value) {
        try {
            set_gpos_provider_data(context, CNS_LAUNCHER_TOKEN, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setLauncherToken", e);
        }
    }

    public static int getTvMailDebugHeartBeat(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, CNS_TVMAIL_DEBUG_HEARTBEAT, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getTvMailDebugHeartBeat", e);
            return 0;
        }
    }

    public static void setTvMailDebugHeartBeat(Context context, int value) {
        try {
            set_gpos_provider_data(context, CNS_TVMAIL_DEBUG_HEARTBEAT, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setTvMailDebugHeartBeat", e);
        }
    }

    public static int getTvMailHeartBeat(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, CNS_TVMAIL_HEARTBEAT, "3600000"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getTvMailHeartBeat", e);
            return  3600000;
        }
    }

    public static void setTvMailHeartBeat(Context context, int value) {
        try {
            set_gpos_provider_data(context, CNS_TVMAIL_HEARTBEAT, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setTvMailHeartBeat", e);
        }
    }

    public static String getCurrentRoleType(Context context) {
        try {
            return get_gpos_provider_data(context, CNS_CURRENT_ROLE_TYPE, "");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getCurrentRoleType", e);
            return "";
        }
    }

    public static void setCurrentRoleType(Context context, String value) {
        try {
            set_gpos_provider_data(context, CNS_CURRENT_ROLE_TYPE, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setCurrentRoleType", e);
        }
    }

    public static String getTutorialUrl(Context context) {
        try {
            return get_gpos_provider_data(context, CNS_TUTORIAL_URL, "");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getTutorialUrl", e);
            return "";
        }
    }

    public static void setTutorialUrl(Context context, String value) {
        try {
            set_gpos_provider_data(context, CNS_TUTORIAL_URL, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setTutorialUrl", e);
        }
    }

    public static int getAdultShow(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, CNS_ADULT_SHOW, "1"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getAdultShow", e);
            return  1;
        }
    }

    public static void setAdultShow(Context context, int value) {
        try {
            set_gpos_provider_data(context, CNS_ADULT_SHOW, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setAdultShow", e);
        }
    }

    public static int getCaMail(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, CNS_CA_MAIL, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getCaMail", e);
            return  0;
        }
    }

    public static void setCaMail(Context context, int value) {
        try {
            set_gpos_provider_data(context, CNS_CA_MAIL, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setCaMail", e);
        }
    }

    public static int getSingleCDialogShow(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, CNS_SINGLE_C_DIALOG_SHOW, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getSingleCDialogShow", e);
            return  0;
        }
    }

    public static void setSingleCDialogShow(Context context, int value) {
        try {
            set_gpos_provider_data(context, CNS_SINGLE_C_DIALOG_SHOW, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setSingleCDialogShow", e);
        }
    }

    public static String getLastWatchChannelUri(Context context) {
        try {
            return get_gpos_provider_data(context, CNS_LAST_WATCH_CHANNEL_URI, "");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getLastWatchChannelUri", e);
            return "";
        }
    }

    public static void setLastWatchChannelUri(Context context, String value) {
        try {
            set_gpos_provider_data(context, CNS_LAST_WATCH_CHANNEL_URI, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setLastWatchChannelUri", e);
        }
    }

    public static int getTimeSettingsEnable(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, CNS_TIME_SETTINGS_ENABLE, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getTimeSettingsEnable", e);
            return  0;
        }
    }

    public static void setTimeSettingsEnable(Context context, int value) {
        try {
            set_gpos_provider_data(context, CNS_TIME_SETTINGS_ENABLE, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setTimeSettingsEnable", e);
        }
    }

    public static String getChannelDataFilePath(Context context) {
        try {
            return get_gpos_provider_data(context, CNS_CHANNEL_DATA_FILE_PATH, "");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getChannelDataFilePath", e);
            return "";
        }
    }

    public static void setChannelDataFilePath(Context context, String value) {
        try {
            set_gpos_provider_data(context, CNS_CHANNEL_DATA_FILE_PATH, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setChannelDataFilePath", e);
        }
    }

    public static String getLauncherProjectId(Context context) {
        try {
            return get_gpos_provider_data(context, CNS_LAUNCHER_PROJECT_ID, "");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getLauncherProjectId", e);
            return "";
        }
    }

    public static void setLauncherProjectId(Context context, String value) {
        try {
            set_gpos_provider_data(context, CNS_LAUNCHER_PROJECT_ID, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setLauncherProjectId", e);
        }
    }

    public static String getLauCacheSha256(Context context, String roleType) {
        try {
            return get_gpos_provider_data(context, CNS_LAU_CACHE_SHA_256 + roleType, "");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getLauCacheSha256", e);
            return "";
        }
    }

    public static void setLauCacheSha256(Context context, String roleType, String value) {
        try {
            set_gpos_provider_data(context, CNS_LAU_CACHE_SHA_256 + roleType, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setLauCacheSha256", e);
        }
    }

    public static String getAllRoleType(Context context) {
        try {
            return get_gpos_provider_data(context, CNS_ALL_ROLE_TYPE, "");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getAllRoleType", e);
            return "";
        }
    }

    public static void setAllRoleType(Context context, String value) {
        try {
            set_gpos_provider_data(context, CNS_ALL_ROLE_TYPE, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setAllRoleType", e);
        }
    }

    public static String getAllRoleTypeAndLauncher(Context context) {
        try {
            return get_gpos_provider_data(context, CNS_ALL_ROLE_TYPE_AND_LAUNCHER, "");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getAllRoleTypeAndLauncher", e);
            return "";
        }
    }

    public static void setAllRoleTypeAndLauncher(Context context, String value) {
        try {
            set_gpos_provider_data(context, CNS_ALL_ROLE_TYPE_AND_LAUNCHER, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setAllRoleTypeAndLauncher", e);
        }
    }

    public static String getAllCustomListHotUpdates(Context context) {
        try {
            return get_gpos_provider_data(context, CNS_ALL_CUSTOM_LIST_HOT_UPDATES, "");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getAllCustomListHotUpdates", e);
            return "";
        }
    }

    public static void setAllCustomListHotUpdates(Context context, String value) {
        try {
            set_gpos_provider_data(context, CNS_ALL_CUSTOM_LIST_HOT_UPDATES, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setAllCustomListHotUpdates", e);
        }
    }

    public static String getallRotationHotUpdates(Context context) {
        try {
            return get_gpos_provider_data(context, CNS_ALL_ROTATION_HOT_UPDATES, "");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getallRotationHotUpdates", e);
            return "";
        }
    }

    public static void setallRotationHotUpdates(Context context, String value) {
        try {
            set_gpos_provider_data(context, CNS_ALL_ROTATION_HOT_UPDATES, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setallRotationHotUpdates", e);
        }
    }

    public static String getPreAllRoleType(Context context) {
        try {
            return get_gpos_provider_data(context, CNS_PRE_ALL_ROLE_TYPE, "");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getPreAllRoleType", e);
            return "";
        }
    }

    public static void setPreAllRoleType(Context context, String value) {
        try {
            set_gpos_provider_data(context, CNS_PRE_ALL_ROLE_TYPE, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setPreAllRoleType", e);
        }
    }

    public static String getRoleName(Context context, String allRoletype) {
        try {
            return get_gpos_provider_data(context, CNS_ROLE_TYPE + allRoletype, "");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getRoleName", e);
            return "";
        }
    }

    public static void setRoleName(Context context, String allRoletype, String value) {
        try {
            set_gpos_provider_data(context, CNS_ROLE_TYPE + allRoletype, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setRoleName", e);
        }
    }

    public static String getLauncherDomain(Context context) {
        try {
            return get_gpos_provider_data(context, CNS_LAUNCHER_DOMAIN, "");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getLauncherDomain", e);
            return "";
        }
    }

    public static void setLauncherDomain(Context context, String value) {
        try {
            set_gpos_provider_data(context, CNS_LAUNCHER_DOMAIN, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setLauncherDomain", e);
        }
    }

    public static int getHasMessage(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, CNS_HAS_MESSAGE, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getHasMessage", e);
            return 0;
        }
    }

    public static void setHasMessage(Context context, int value) {
        try {
            set_gpos_provider_data(context, CNS_HAS_MESSAGE, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setHasMessage", e);
        }
    }

    public static int getScreenOffEnable(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, CNS_SCREEN_OFF_ENABLE, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getScreenOffEnable", e);
            return 0;
        }
    }

    public static void setScreenOffEnable(Context context, int value) {
        try {
            set_gpos_provider_data(context, CNS_SCREEN_OFF_ENABLE, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setScreenOffEnable", e);
        }
    }
    public static int getSpeakerBindStatus(Context context) {
        try {
            return Integer.parseInt(get_gpos_provider_data(context, CNS_SPEAKER_BIND_STATUS, "0"));
        } catch (Exception e) {
            Log.e(TAG, "Failed to getSpeakerBindStatus", e);
            return 0;
        }
    }

    public static void setSpeakerBindStatus(Context context, int value) {
        try {
            set_gpos_provider_data(context, CNS_SPEAKER_BIND_STATUS, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setSpeakerBindStatus", e);
        }
    }

    public static String getLastLiAdShowTime(Context context) {
        try {
            return get_gpos_provider_data(context, CNS_LAST_LI_AD_SHOW_TIME, "");
        } catch (Exception e) {
            Log.e(TAG, "Failed to getLastLiAdShowTime", e);
            return "";
        }
    }

    public static void setLastLiAdShowTime(Context context, String value) {
        try {
            set_gpos_provider_data(context, CNS_LAST_LI_AD_SHOW_TIME, String.valueOf(value));
        } catch (Exception e) {
            Log.e(TAG, "Failed to setLastLiAdShowTime", e);

        }
    }
}
