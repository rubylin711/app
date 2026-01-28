package com.prime.sysdata;

/**
 * Created by gary_hsu on 2017/11/10.
 */


public class GposInfo {
    private static final String TAG="GposInfo";
    public static final int NUMBER_OF_AUDIO_LANGUAGE_SELECTION = 4;

    private int DBVersion;
    private int CurChannelId;
    private int CurGroupType;
    private int PasswordValue;
    private int ParentalRate;
    private int ParentalLockOnOff;
    private int InstallLockOnOff;
    private int BoxPowerStatus;
    private int StartOnChannelId;
    private int StartOnChType;
    private int Volume;
    private int AudioStereo;
    private int PalStandard;
    private int MonitorType;
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
    private int StandbyMode;
    private int HardHearing;
    private int AutoStandbyTime;
    private int DolbyMode;
    private int HDCPOnOff;
    private int DeepSleepMode;
    private int GoBackStandby;
    private int SubtitleOnOff;

    public String ToString(){
        return "[DBVersion : " + DBVersion + "CurChannelId :" + CurChannelId + "CurGroupType : " + CurGroupType + "PasswordValue : " + PasswordValue
                + "ParentalRate : "+ ParentalRate + "ParentalLockOnOff : " + ParentalLockOnOff + "InstallLockOnOff : " + InstallLockOnOff + "BoxPowerStatus : " + BoxPowerStatus
                + "StartOnChannelId : " + StartOnChannelId + "StartOnChType : " + StartOnChType + "Volume : " + Volume + "AudioStereo : " + AudioStereo
                + "PalStandard : " + PalStandard + "MonitorType : " + MonitorType + "AutoRegionTimeOffset : " + AutoRegionTimeOffset
                + "LnbPower : " + LnbPower + "AutoRegionTimeOffset : " + AutoRegionTimeOffset + "RegionSummerTime : " + RegionSummerTime
                + "Screen16x9 : " + Screen16x9 + "Conversion : " + Conversion + "OSDLanguage : " + OSDLanguage + "SearchProgramType : " + SearchProgramType + "SearchMode : " + SearchMode
                + "AudioLanguageSelection[0] : " + AudioLanguageSelection[0] + "AudioLanguageSelection[1] : " + AudioLanguageSelection[1]
                + "SubtitleLanguageSelection[0] : " + SubtitleLanguageSelection[0] + "SubtitleLanguageSelection[1] : " + SubtitleLanguageSelection[1]
                + "SortByLcn : " + SortByLcn + "OSDTransparency : " + OSDTransparency + "BannerTimeout : " + BannerTimeout + "StandbyMode : " + StandbyMode
                + "HardHearing : " + HardHearing + "AutoStandbyTime : " + AutoStandbyTime + "DolbyMode : " + DolbyMode + "HDCPOnOff : " + HDCPOnOff
                + "DeepSleepMode : " + DeepSleepMode + "GoBackStandby : " + GoBackStandby + "]";
    }

    public int getCurChannelId() {
        return CurChannelId;
    }

    public void setCurChannelId(int curChannelId) {
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

    public int getStartOnChannelId() {
        return StartOnChannelId;
    }

    public void setStartOnChannelId(int startOnChannelId) {
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

    public int getAudioStereo() {
        return AudioStereo;
    }

    public void setAudioStereo(int audioStereo) {
        AudioStereo = audioStereo;
    }

    public int getPalStandard() {
        return PalStandard;
    }

    public void setPalStandard(int palStandard) {
        PalStandard = palStandard;
    }

    public int getMonitorType() {
        return MonitorType;
    }

    public void setMonitorType(int monitorType) {
        MonitorType = monitorType;
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

    public int getStandbyMode() {
        return StandbyMode;
    }

    public void setStandbyMode(int standbyMode) {
        StandbyMode = standbyMode;
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

    public int getGoBackStandby() {
        return GoBackStandby;
    }

    public void setGoBackStandby(int goBackStandby) {
        GoBackStandby = goBackStandby;
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

    public int getDBVersion() {
        return DBVersion;
    }

    public void setDBVersion(int DBVersion) {
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
}
