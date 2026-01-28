package com.prime.dtvplayer.Sysdata;

/**
 * Created by gary_hsu on 2017/11/10.
 */


public class GposInfo {
    private static final String TAG="GposInfo";
    public static final int NUMBER_OF_AUDIO_LANGUAGE_SELECTION = 4;

    //if add new member , should add new String member for member, and also add in middle ware
    public static final String GPOS_DB_VSERSION = "DBVersion";
    public static final String GPOS_CUR_CHANNELID = "CurChannelId";
    public static final String GPOS_CUR_GROUP_TYPE = "CurGroupType";
    public static final String GPOS_PASSWORD_VALUE = "PasswordValue";
    public static final String GPOS_PARENTAL_RATE = "ParentalRate";
    public static final String GPOS_PARENTAL_LOCK_ONOFF = "ParentalLockOnOff";
    public static final String GPOS_INSTALL_LOCK_ONOFF = "InstallLockOnOff";
    public static final String GPOS_BOX_POWER_STATUS = "BoxPowerStatus";
    public static final String GPOS_START_ON_CHANNELID = "StartOnChannelId";
    public static final String GPOS_START_ON_CHTYPE = "StartOnChType";
    public static final String GPOS_VOLUME = "Volume";
    public static final String GPOS_AUDIO_TRACK_MODE = "AudioTrackMode";
    public static final String GPOS_AUTO_REGION_TIME_OFFSET = "AutoRegionTimeOffset"; //自動時區設定
    public static final String GPOS_REGION_TIME_OFSSET = "RegionTimeOffset"; //時區,單位小時
    public static final String GPOS_REGION_SUMMER_TIME = "RegionSummerTime"; //日光時間
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

    // parental rate define
    public static final int PARENTALRATE_DISABLE = 0;
    public static final int PARENTALRATE_SIX = 6;
    public static final int PARENTALRATE_TWELVE = 12;
    public static final int PARENTALRATE_SIXTEEN = 16;
    public static final int PARENTALRATE_EIGHTEEN = 18;
    public static final int PARENTALRATE_ALLBLOCKED = 99;

    private String DBVersion;
    private long CurChannelId;
    private int CurGroupType;
    private int PasswordValue;
    private int ParentalRate;
    private int ParentalLockOnOff;
    private int InstallLockOnOff;
    private int BoxPowerStatus;
    private long StartOnChannelId;
    private int StartOnChType;
    private int Volume;
    private int AudioTrackMode;
    private int AutoRegionTimeOffset; //自動時區設定
    private Float RegionTimeOffset; //時區,單位小時
    private int RegionSummerTime; //日光時間
    private int LnbPower;
    private int Screen16x9;
    private int Conversion;
    private int Resolution;
    private String OSDLanguage;
    private int SearchProgramType;
    private int SearchMode;
    private String[] AudioLanguageSelection = new String[2];
    private String[] SubtitleLanguageSelection = new String[2];
    private int SortByLcn;
    private int OSDTransparency;
    private int BannerTimeout;
    private int HardHearing;
    private int AutoStandbyTime;
    private int DolbyMode;
    private int HDCPOnOff;
    private int DeepSleepMode;
    private int SubtitleOnOff;
    private int AvStopMode;
    private int TimeshiftDuration;
    private int RecordIconOnOff;

    public String ToString(){
        return "[DBVersion : " + DBVersion + "CurChannelId :" + CurChannelId + "CurGroupType : " + CurGroupType + "PasswordValue : " + PasswordValue
                + "ParentalRate : "+ ParentalRate + "ParentalLockOnOff : " + ParentalLockOnOff + "InstallLockOnOff : " + InstallLockOnOff + "BoxPowerStatus : " + BoxPowerStatus
                + "StartOnChannelId : " + StartOnChannelId + "StartOnChType : " + StartOnChType + "Volume : " + Volume + "AudioTrackMode : " + AudioTrackMode
                + "AutoRegionTimeOffset : " + AutoRegionTimeOffset
                + "LnbPower : " + LnbPower + "AutoRegionTimeOffset : " + AutoRegionTimeOffset + "RegionSummerTime : " + RegionSummerTime
                + "Screen16x9 : " + Screen16x9 + "Conversion : " + Conversion + "OSDLanguage : " + OSDLanguage + "SearchProgramType : " + SearchProgramType + "SearchMode : " + SearchMode
                + "AudioLanguageSelection[0] : " + AudioLanguageSelection[0] + "AudioLanguageSelection[1] : " + AudioLanguageSelection[1]
                + "SubtitleLanguageSelection[0] : " + SubtitleLanguageSelection[0] + "SubtitleLanguageSelection[1] : " + SubtitleLanguageSelection[1]
                + "SortByLcn : " + SortByLcn + "OSDTransparency : " + OSDTransparency + "BannerTimeout : " + BannerTimeout
                + "HardHearing : " + HardHearing + "AutoStandbyTime : " + AutoStandbyTime + "DolbyMode : " + DolbyMode + "HDCPOnOff : " + HDCPOnOff
                + "DeepSleepMode : " + DeepSleepMode + "AvStopMode : " + AvStopMode + "TimeShift Duration : " + TimeshiftDuration
                + "RecordIconOnOff : " + RecordIconOnOff + "]";
    }

    public long getCurChannelId() {
        return CurChannelId;
    }

    public void setCurChannelId(long curChannelId) {
        CurChannelId = curChannelId;
    }

    public int getCurGroupType() {
        return CurGroupType;
    }

    public void setCurGroupType(int curGroupType) {
        CurGroupType = curGroupType;
    }

    public int getPasswordValue() {
        return PasswordValue;
    }

    public void setPasswordValue(int passwordValue) {
        PasswordValue = passwordValue;
    }

    public int getParentalRate() {
        return ParentalRate;
    }

    public void setParentalRate(int parentalRate) {
        ParentalRate = parentalRate;
    }

    public int getParentalLockOnOff() {
        return ParentalLockOnOff;
    }

    public void setParentalLockOnOff(int parentalLockOnOff) {
        ParentalLockOnOff = parentalLockOnOff;
    }

    public int getInstallLockOnOff() {
        return InstallLockOnOff;
    }

    public void setInstallLockOnOff(int installLockOnOff) {
        InstallLockOnOff = installLockOnOff;
    }

    public int getBoxPowerStatus() {
        return BoxPowerStatus;
    }

    public void setBoxPowerStatus(int boxPowerStatus) {
        BoxPowerStatus = boxPowerStatus;
    }

    public long getStartOnChannelId() {
        return StartOnChannelId;
    }

    public void setStartOnChannelId(long startOnChannelId) {
        StartOnChannelId = startOnChannelId;
    }

    public int getStartOnChType() {
        return StartOnChType;
    }

    public void setStartOnChType(int startOnChType) {
        StartOnChType = startOnChType;
    }

    public int getVolume() {
        return Volume;
    }

    public void setVolume(int volume) {
        Volume = volume;
    }

    public int getAudioTrackMode() {
        return AudioTrackMode;
    }

    public void setAudioTrackMode(int audioTrackMode) {
        AudioTrackMode = audioTrackMode;
    }

    public float getRegionTimeOffset() {
        return RegionTimeOffset;
    }

    public void setRegionTimeOffset(Float regionTimeOffset) {
        RegionTimeOffset = regionTimeOffset;
    }

    public int getLnbPower() {
        return LnbPower;
    }

    public void setLnbPower(int lnbPower) {
        LnbPower = lnbPower;
    }

    public int getScreen16x9() {
        return Screen16x9;
    }

    public void setScreen16x9(int screen16x9) {
        Screen16x9 = screen16x9;
    }

    public String getOSDLanguage() {
        return OSDLanguage;
    }

    public void setOSDLanguage(String OSDLanguage) {
        this.OSDLanguage = OSDLanguage;
    }

    public int getSearchProgramType() {
        return SearchProgramType;
    }

    public void setSearchProgramType(int searchProgramType) {
        SearchProgramType = searchProgramType;
    }

    public int getSearchMode() {
        return SearchMode;
    }

    public void setSearchMode(int searchMode) {
        SearchMode = searchMode;
    }

    public String getAudioLanguageSelection(int index) {
        return AudioLanguageSelection[index];
    }

    public void setAudioLanguageSelection(int index, String audioLanguageSelection) {
        AudioLanguageSelection[index] = audioLanguageSelection;
    }

    public String getSubtitleLanguageSelection(int index) {
        return SubtitleLanguageSelection[index];
    }

    public void setSubtitleLanguageSelection(int index, String subtitleLanguageSelection) {
        SubtitleLanguageSelection[index] = subtitleLanguageSelection;
    }

    public int getSortByLcn() {
        return SortByLcn;
    }

    public void setSortByLcn(int sortByLcn) {
        SortByLcn = sortByLcn;
    }

    public int getOSDTransparency() {
        return OSDTransparency;
    }

    public void setOSDTransparency(int OSDTransparency) {
        this.OSDTransparency = OSDTransparency;
    }

    public int getBannerTimeout() {
        return BannerTimeout;
    }

    public void setBannerTimeout(int bannerTimeout) {
        BannerTimeout = bannerTimeout;
    }

    public int getHardHearing() {
        return HardHearing;
    }

    public void setHardHearing(int hardHearing) {
        HardHearing = hardHearing;
    }

    public int getAutoStandbyTime() {
        return AutoStandbyTime;
    }

    public void setAutoStandbyTime(int autoStandbyTime) {
        AutoStandbyTime = autoStandbyTime;
    }

    public int getDolbyMode() {
        return DolbyMode;
    }

    public void setDolbyMode(int dolbyMode) {
        DolbyMode = dolbyMode;
    }

    public int getHDCPOnOff() {
        return HDCPOnOff;
    }

    public void setHDCPOnOff(int HDCPOnOff) {
        this.HDCPOnOff = HDCPOnOff;
    }

    public int getDeepSleepMode() {
        return DeepSleepMode;
    }

    public void setDeepSleepMode(int deepSleepMode) {
        DeepSleepMode = deepSleepMode;
    }

    public int getSubtitleOnOff() {
        return SubtitleOnOff;
    }

    public void setSubtitleOnOff(int subtitleOnOff) {
        SubtitleOnOff = subtitleOnOff;
    }

    public int getConversion() {
        return Conversion;
    }

    public void setConversion(int conversion) {
        Conversion = conversion;
    }

    public String getDBVersion() {
        return DBVersion;
    }

    public void setDBVersion(String DBVersion) {
        this.DBVersion = DBVersion;
    }

    public int getAutoRegionTimeOffset() {
        return AutoRegionTimeOffset;
    }

    public void setAutoRegionTimeOffset(int autoRegionTimeOffset) {
        AutoRegionTimeOffset = autoRegionTimeOffset;
    }

    public int getRegionSummerTime() {
        return RegionSummerTime;
    }

    public void setRegionSummerTime(int regionSummerTime) {
        RegionSummerTime = regionSummerTime;
    }

    public int getResolution() {
        return Resolution;
    }

    public void setResolution(int resolution) {
        Resolution = resolution;
    }

    public int getAvStopMode() {
        return AvStopMode;
    }

    public void setAvStopMode(int avStopMode) {
        AvStopMode = avStopMode;
    }

    public int getTimeshiftDuration(){
        return TimeshiftDuration;
    }

    public void setTimeshiftDuration(int duration){
        TimeshiftDuration = duration;
    }

    public int getRecordIconOnOff() {
        return RecordIconOnOff;
    }

    public void setRecordIconOnOff(int recordIconOnOff) {
        RecordIconOnOff = recordIconOnOff;
    }
}
