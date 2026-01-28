package com.dolphin.dtv;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.HwBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.WebView;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.Config.Pvcfg;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Service.DataManager.DataManager;
import com.prime.dtvplayer.Sysdata.AudioInfo;
import com.prime.dtvplayer.Sysdata.BookInfo;
import com.prime.dtvplayer.Sysdata.CaStatus;
import com.prime.dtvplayer.Sysdata.DefaultChannel;
import com.prime.dtvplayer.Sysdata.EPGEvent;
import com.prime.dtvplayer.Sysdata.EnAudioTrackMode;
import com.prime.dtvplayer.Sysdata.FavGroupName;
import com.prime.dtvplayer.Sysdata.FavInfo;
import com.prime.dtvplayer.Sysdata.GposInfo;
import com.prime.dtvplayer.Sysdata.LoaderInfo;
import com.prime.dtvplayer.Sysdata.MiscDefine;
import com.prime.dtvplayer.Sysdata.NetProgramInfo;
import com.prime.dtvplayer.Sysdata.PROGRAM_PLAY_STREAM_TYPE;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.PvrInfo;
import com.prime.dtvplayer.Sysdata.SatInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;
import com.prime.dtvplayer.Sysdata.SubtitleInfo;
import com.prime.dtvplayer.Sysdata.TeletextInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.Sysdata.VMXProtectData;
import com.prime.dtvplayer.utils.TVScanParams;
import com.prime.dtvplayer.utils.TVTunerParams;
import com.loader.structure.OTACableParameters;
import com.loader.structure.OTATerrParameters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import vendor.prime.hardware.dtvservice.V1_0.IDtvService;
import vendor.prime.hardware.dtvservice.V1_0.IDtvServiceCallback;

import static com.mtest.activity.MainActivity.mExoPlayer;
import static java.lang.Integer.parseInt;

/**
 * Created by ethan_lin on 2017/10/26.
 */

public class HiDtvMediaPlayer {
    private static final String TAG="HiDtvMediaPlayer";
    private static final int PLATFORM_HISI = 0;
    private static final int PLATFORM_PESI = 1;
    private static int PLATFORM = 0;
    private static int TUNER_TYPE = 0;
    private static int tempPesiDefaultChannelFlag = 0;
    private static String apkSwVersion = "DDN_V7.7.1.2";//centaur 20200619 fix mtest
    private static String DefaultRecPath = "/mnt/sdcard";
    private static String TAG_CFG_PVR_PATH = "PesiPvrRecPath";
    private static int TUNER_NUM = 0;//Scoty 20181113 add GetTunerNum function
    private static List<Integer> usbPort = null;
    private static final String SET_SURFACE = "com.prime.DTVPlayer.setSurface";//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    //Scoty Add Youtube/Vod Stream -s
    private static Context mContext;
    private static SimpleExoPlayer mExoPlayer;
    private static SurfaceView mSurfaceViewExoplayer;
    private static WebView mWebView;
    //Scoty Add Youtube/Vod Stream -e

    /*
      test for Hisilicon
     */
    public static final String DTV_INTERFACE_NAME = "prime.dtv.IDtvService";
//    private String mUri = "dtv-live://plugin.libhi_dtvplg";//"dtv-live://plugin.libhi_dtvfrm_plugin.libhipvr_plugin.libhidtv_multiroom_plugin";
    private static HiDtvMediaPlayer HisDtv = null ;
    private String mLang = Locale.getDefault().getISO3Language();
    public static final int CMD_RETURN_VALUE_FAILAR = -1;
    public static final long CMD_RETURN_LONG_VALUE_FAILAR = -1;
    public static final int CMD_RETURN_VALUE_SUCCESS = 99;
    public static final boolean ADD_SYSTEM_OFFSET = false;  // connie 20181106 for not add system offset
    private static final int HI_SVR_PVR_MAX_CIPHER_KEY_LEN = 128;
    private static IDtvService server = null;
    private static DtvServiceCallback mCallback = null;
    public static byte[] privateData;//eric lin 20210312 set private data from service
    /*
    private static final int CMD_Base = 0x0100;
    private static final int CMD_AV_Base = CMD_Base + 0x100;
    private static final int CMD_CS_Base = CMD_Base + 0x200;
    private static final int CMD_EPG_Base = CMD_Base + 0x400;
    private static final int CMD_FE_Base = CMD_Base + 0x500;
    private static final int CMD_PM_Base = CMD_Base + 0x600;
    private static final int CMD_BOOK_Base = CMD_Base + 0x700;
    private static final int CMD_CFG_Base = CMD_Base + 0x800;
    private static final int CMD_TMCTL_Base = CMD_Base + 0x900;

    // Common Command
    private static final int CMD_SubscribeEvent = CMD_Base + 3;
    private static final int CMD_UnSubscribeEvent = CMD_Base + 4;
    private static final int CMD_SetSurface = CMD_Base + 5;
    private static final int CMD_PrepareDTV = CMD_Base + 7;
    private static final int CMD_CreateSurface = CMD_Base + 18;
    private static final int CMD_CallBackTest = CMD_Base + 100;

    // AV Command
    private static final int CMD_AV_PlayById = CMD_AV_Base + 1;
    private static final int CMD_AV_StopLivePlay = CMD_AV_Base + 2;
    private static final int CMD_AV_ReleasePlayResource = CMD_AV_Base + 3;
    private static final int CMD_AV_ResumePlayResource = CMD_AV_Base + 4;
    private static final int CMD_AV_SetWindowSize = CMD_AV_Base + 5;
    private static final int CMD_AV_GetWindowSize = CMD_AV_Base + 6;
    private static final int CMD_AV_SetClipRect = CMD_AV_Base + 7;
    private static final int CMD_AV_GetClipRect = CMD_AV_Base + 8;
    private static final int CMD_AV_GetPlayStatus = CMD_AV_Base + 9;
    private static final int CMD_AV_ShowVideo = CMD_AV_Base + 10;
    private static final int CMD_AV_GetCurrentAudio = CMD_AV_Base + 11;// Abandon
    private static final int CMD_AV_SelectAudio = CMD_AV_Base + 12;
    private static final int CMD_AV_GetAudioListInfo = CMD_AV_Base + 13;
    private static final int CMD_AV_GetCurrentSubtitle = CMD_AV_Base + 14;
    private static final int CMD_AV_GetSubtitleList = CMD_AV_Base + 15;
    private static final int CMD_AV_SelectSubtitle = CMD_AV_Base + 16;
    private static final int CMD_AV_ShowSubtitle = CMD_AV_Base + 17;
    private static final int CMD_AV_IsSubtitleVisible = CMD_AV_Base + 18;
    private static final int CMD_AV_GetCurrentTeletext = CMD_AV_Base + 19;
    private static final int CMD_AV_GetTeletextList = CMD_AV_Base + 20;
    private static final int CMD_AV_ShowTeletext = CMD_AV_Base + 21;
    private static final int CMD_AV_IsTeletextVisible = CMD_AV_Base + 22;
    private static final int CMD_AV_SetCommand = CMD_AV_Base + 23;
    private static final int CMD_AV_GetTimeShiftBeginTime = CMD_AV_Base + 24;
    private static final int CMD_AV_GetTimeShiftPlayTime = CMD_AV_Base + 25;
    private static final int CMD_AV_GetTimeShiftRecordTime = CMD_AV_Base + 26;
    private static final int CMD_AV_GetTrickMode = CMD_AV_Base + 27;
    private static final int CMD_AV_TrickPlay = CMD_AV_Base + 28;
    private static final int CMD_AV_PausePlay = CMD_AV_Base + 29;
    private static final int CMD_AV_Play = CMD_AV_Base + 30;
    private static final int CMD_AV_SeekPlay = CMD_AV_Base + 31;
    private static final int CMD_AV_StopTimeShift = CMD_AV_Base + 32;
    private static final int CMD_AV_SetLayerOrder = CMD_AV_Base + 33;
    private static final int CMD_AV_SetMuteStatus = CMD_AV_Base + 34;
    private static final int CMD_AV_GetMuteStatus = CMD_AV_Base + 35;
    private static final int CMD_AV_SetVolume = CMD_AV_Base + 36;
    private static final int CMD_AV_GetVolume = CMD_AV_Base + 37;
    private static final int CMD_AV_SnapShot = CMD_AV_Base + 38;
    private static final int CMD_AV_SetAudioTrackMode = CMD_AV_Base + 39;
    private static final int CMD_AV_GetAudioTrackMode = CMD_AV_Base + 40;
    private static final int CMD_AV_GetVideoResolutionHeight = CMD_AV_Base + 41;
    private static final int CMD_AV_GetVideoResolutionWidth = CMD_AV_Base + 42;
    private static final int CMD_AV_StopAudioStreamOutput = CMD_AV_Base + 43;
    private static final int CMD_AV_StartAudioStreamOutput = CMD_AV_Base + 44;
    private static final int CMD_AV_GetDolbyInfoStreamType = CMD_AV_Base + 45;
    private static final int CMD_AV_GetDolbyInfoAcmod = CMD_AV_Base + 46;
    private static final int CMD_AV_SetDolbyRange = CMD_AV_Base + 47;

    private static final int CMD_AV_GetCurrentCC = CMD_AV_Base + 48;
    private static final int CMD_AV_GetCCType = CMD_AV_Base + 49;
    private static final int CMD_AV_SelectCC = CMD_AV_Base + 50;
    private static final int CMD_AV_ShowCC = CMD_AV_Base + 51;
    private static final int CMD_AV_IsCCVisible = CMD_AV_Base + 52;
    private static final int CMD_AV_GetCurrentProg = CMD_AV_Base + 53;
    private static final int CMD_AV_FreezeVideo = CMD_AV_Base + 54;
    private static final int CMD_AV_SetStopMode = CMD_AV_Base + 55;
    private static final int CMD_AV_GetStopMode = CMD_AV_Base + 56;
    private static final int CMD_AV_PauseSubtitle = CMD_AV_Base + 57;
    private static final int CMD_AV_ResumeSubtitle = CMD_AV_Base + 58;
    private static final int CMD_AV_SetSubtHiAvailable = CMD_AV_Base + 59;
    private static final int CMD_AV_SetSubtHiStatus = CMD_AV_Base + 60;
    private static final int CMD_AV_SetSubtLanguage = CMD_AV_Base + 61;
    private static final int CMD_AV_SetAudioLanguage = CMD_AV_Base + 62;
    private static final int CMD_AV_GetDRARawChannel = CMD_AV_Base + 63;

    private static final int CMD_AV_AdSetEnable = CMD_AV_Base + 64;
    private static final int CMD_AV_AdGetEnable = CMD_AV_Base + 65;
    private static final int CMD_AV_AdSetBalance = CMD_AV_Base + 66;
    private static final int CMD_AV_AdGetBalance = CMD_AV_Base + 67;
    private static final int CMD_AV_AdSetToSomePort = CMD_AV_Base + 68;
    private static final int CMD_AV_AdGetToSomePort = CMD_AV_Base + 69;

    private static final int CMD_AV_SetTimeShiftEncryption = CMD_AV_Base + 70;
    private static final int CMD_AV_SetTeletextLanguage = CMD_AV_Base + 71;
    private static final int CMD_AV_GetFPS = CMD_AV_Base + 72;
    private static final int CMD_AV_EwsActionControl = CMD_AV_Base + 73;
    private static final int CMD_AV_IsTeletextAvailable = CMD_AV_Base + 74;

    // ChannelScan command
    private static final int CMD_CS_SingleFreqSearch = CMD_CS_Base + 1;
    private static final int CMD_CS_SegmentSearch = CMD_CS_Base + 2;
    private static final int CMD_CS_AutoSearch = CMD_CS_Base + 3;
    private static final int CMD_CS_NITSearch = CMD_CS_Base + 4;
    private static final int CMD_CS_MultiTransponderSearch = CMD_CS_Base + 5;
    private static final int CMD_CS_SingleSatelliteSearch = CMD_CS_Base + 6;
    private static final int CMD_CS_SteppingSearch = CMD_CS_Base + 7;
    private static final int CMD_CS_StopSearch = CMD_CS_Base + 8;
    private static final int CMD_CS_PauseSearch = CMD_CS_Base + 9;
    private static final int CMD_CS_ResumeSearch = CMD_CS_Base + 10;
    private static final int CMD_CS_GetSearchProgress = CMD_CS_Base + 11;
    private static final int CMD_CS_GetSearchStatisticalInfo = CMD_CS_Base + 12;

    // PM command
    private static final int CMD_PM_GetChannelByID = CMD_PM_Base + 1;
    private static final int CMD_PM_GetChannelList = CMD_PM_Base + 2;
    private static final int CMD_PM_ChannelNumInGroup = CMD_PM_Base + 3;
    private static final int CMD_PM_GetChannelGroup = CMD_PM_Base + 4;
    private static final int CMD_PM_DelChannelByTag = CMD_PM_Base + 5;
    private static final int CMD_PM_CreateChannel = CMD_PM_Base + 6;
    private static final int CMD_PM_GetChannelGroupName = CMD_PM_Base + 7;
    private static final int CMD_PM_SetChannelGroupName = CMD_PM_Base + 8;
    private static final int CMD_PM_MoveChannel = CMD_PM_Base + 9;
    private static final int CMD_PM_SwapChannel = CMD_PM_Base + 10;
    private static final int CMD_PM_SetDefaultOpenChannel = CMD_PM_Base + 11;
    private static final int CMD_PM_GetDefaultOpenChannel = CMD_PM_Base + 12;
    private static final int CMD_PM_DelChannelByID = CMD_PM_Base + 13;
    private static final int CMD_PM_GetChannelNO = CMD_PM_Base + 14;
    private static final int CMD_PM_GetChannelByNO = CMD_PM_Base + 15;
    private static final int CMD_PM_GetChannelIDByLCN = CMD_PM_Base + 16;
    private static final int CMD_PM_EditChannel = CMD_PM_Base + 17;
    private static final int CMD_PM_DelChannelByNetWorkID = CMD_PM_Base + 18;
    private static final int CMD_PM_DelChannelBySignalType = CMD_PM_Base + 19;
    private static final int CMD_PM_DelChannelByTPID = CMD_PM_Base + 20;

    //pm tp
    private static final int CMD_PM_GetTPListByDeliverID = CMD_PM_Base + 50;
    private static final int CMD_PM_AddTP = CMD_PM_Base + 51;
    private static final int CMD_PM_RemoveTPByID = CMD_PM_Base + 52;
    private static final int CMD_PM_RemoveTPByDeliveryID = CMD_PM_Base + 53;
    private static final int CMD_PM_EditTP = CMD_PM_Base + 54;
    private static final int CMD_PM_GetTPByID = CMD_PM_Base + 55;

    //pm delivery
    private static final int CMD_PM_GetDeliverSystemList = CMD_PM_Base + 100;
    private static final int CMD_PM_GetDeliverSystemByID = CMD_PM_Base + 101;
    private static final int CMD_PM_AddSatellite = CMD_PM_Base + 102;
    private static final int CMD_PM_RemoveDeliverSystemByID = CMD_PM_Base + 103;
    private static final int CMD_PM_EditSatellite = CMD_PM_Base + 104;
    private static final int CMD_PM_GetAntennaInfoBySatID = CMD_PM_Base + 105;
    private static final int CMD_PM_EditAntennaInfoBySatID = CMD_PM_Base + 106;
    private static final int CMD_PM_GetTPCountByDeliverID = CMD_PM_Base + 107;
    private static final int CMD_PM_GetDeliverSystemCount = CMD_PM_Base + 108;
    private static final int CMD_PM_SetMotorType = CMD_PM_Base + 109;
    private static final int CMD_PM_EditDeliverSystem = CMD_PM_Base + 110;

    //pm file
    private static final int CMD_PM_ExportDBToFile = CMD_PM_Base + 200;
    private static final int CMD_PM_ImportDBFromFile = CMD_PM_Base + 201;
    private static final int CMD_PM_ImportDBFromIni = CMD_PM_Base + 202;
    private static final int CMD_PM_GetPresetTPCount = CMD_PM_Base + 203;
    private static final int CMD_PM_GetPresetTPList = CMD_PM_Base + 204;
    private static final int CMD_PM_Save = CMD_PM_Base + 205;
    private static final int CMD_PM_Save_DELAY = CMD_PM_Base + 206;
    private static final int CMD_PM_Clear = CMD_PM_Base + 207;
    private static final int CMD_PM_Restore = CMD_PM_Base + 208;
    private static final int CMD_PM_SaveSampleChannelList = CMD_PM_Base + 209;
    private static final int CMD_PM_SaveChannel = CMD_PM_Base + 210;
    private static final int CMD_PM_SaveGroupList = CMD_PM_Base + 211;
    private static final int CMD_PM_SaveTpList = CMD_PM_Base + 212;
    private static final int CMD_PM_SaveSatList = CMD_PM_Base + 213;
    private static final int CMD_PM_GetGroupChannel = CMD_PM_Base + 214;
    private static final int CMD_PM_GetGroupChannelList = CMD_PM_Base + 215;
    private static final int CMD_PM_DelGroupChannel = CMD_PM_Base + 216;
    private static final int CMD_PM_DelGroupAll = CMD_PM_Base + 217;
    private static final int CMD_PM_GetSimpleChannelList = CMD_PM_Base + 218;
    private static final int CMD_PM_GetSimpleChannel = CMD_PM_Base + 219;
    private static final int CMD_PM_SaveGroupName = CMD_PM_Base + 220;
    private static final int CMD_PM_GetGroupName = CMD_PM_Base + 221;
    //pm list
    private static final int CMD_PM_GetPosByID = CMD_PM_Base + 150;
    private static final int CMD_PM_GetPosByLcn = CMD_PM_Base + 151;
    private static final int CMD_PM_Sort = CMD_PM_Base + 152;
    private static final int CMD_PM_ProgramSort = CMD_PM_Base + 153;
    private static final int CMD_PM_GetUseGroups = CMD_PM_Base + 154;
    private static final int CMD_PM_RebuildGroup = CMD_PM_Base + 155;
    private static final int CMD_PM_GetGroupFilter = CMD_PM_Base + 156;
    private static final int CMD_PM_SetServiceMode = CMD_PM_Base + 157;
    private static final int CMD_PM_GetServiceMode = CMD_PM_Base + 158;
    private static final int CMD_PM_RebuildAllGroup = CMD_PM_Base + 159;
    private static final int CMD_PM_GetDefaultOpenGroupType = CMD_PM_Base + 160;

    // FrontEnd command
    private static final int CMD_FE_TunerLockFrequency = CMD_FE_Base + 1;
    private static final int CMD_FE_TunerUnLock = CMD_FE_Base + 2;
    private static final int CMD_FE_GetSignalQuality = CMD_FE_Base + 3;
    private static final int CMD_FE_GetSignalStrength = CMD_FE_Base + 4;
    private static final int CMD_FE_GetSignalBER = CMD_FE_Base + 5;
    private static final int CMD_FE_GetSignalSNR = CMD_FE_Base + 6;
    private static final int CMD_FE_SetLNBParameter = CMD_FE_Base + 7;
    private static final int CMD_FE_SetLNBPower = CMD_FE_Base + 8;
    private static final int CMD_FE_Set22KSwitch = CMD_FE_Base + 9;
    private static final int CMD_FE_SetToneBurstSwitch = CMD_FE_Base + 10;
    private static final int CMD_FE_SetAntennaPower = CMD_FE_Base + 11;
    private static final int CMD_FE_SetLocalCoordinate = CMD_FE_Base + 12;
    private static final int CMD_FE_SetPort4Information = CMD_FE_Base + 13;
    private static final int CMD_FE_SetPort16Information = CMD_FE_Base + 14;
    private static final int CMD_FE_SetLimitPosition = CMD_FE_Base + 15;
    private static final int CMD_FE_StoreSatellitePosition = CMD_FE_Base + 16;
    private static final int CMD_FE_GotoSatellitePosition = CMD_FE_Base + 17;
    private static final int CMD_FE_MoveMotor = CMD_FE_Base + 18;
    private static final int CMD_FE_StopMoveMotor = CMD_FE_Base + 19;
    private static final int CMD_FE_ResetMotor = CMD_FE_Base + 20;
    private static final int CMD_FE_RecalculateSatellitePosition = CMD_FE_Base + 21;
    private static final int CMD_FE_CalculateSatelliteAngle = CMD_FE_Base + 22;
    private static final int CMD_FE_GotoUSALSAngle = CMD_FE_Base + 23;
    private static final int CMD_FE_GetTunnerStatus = CMD_FE_Base + 24;
    private static final int CMD_FE_GetTunerLockQAM = CMD_FE_Base + 25;
    private static final int CMD_FE_GetTunnerLockSignalType = CMD_FE_Base + 26;
    private static final int CMD_FE_GetTunnerLockMultiplexInfo = CMD_FE_Base + 27;
    private static final int CMD_FE_SetMotorAutoRotationSwitch = CMD_FE_Base + 28;
    // EPG command
    private static final int CMD_EPG_GetPresentEvent = CMD_EPG_Base + 1;
    private static final int CMD_EPG_GetFollowEvent = CMD_EPG_Base + 2;
    private static final int CMD_EPG_GetByEventId = CMD_EPG_Base + 3;
    private static final int CMD_EPG_GetByFilter = CMD_EPG_Base + 4;
    private static final int CMD_EPG_GetShortDesc = CMD_EPG_Base + 5;
    private static final int CMD_EPG_GetDetailDesc = CMD_EPG_Base + 6;
    private static final int CMD_EPG_SetLanguageCode = CMD_EPG_Base + 7;
    private static final int CMD_EPG_Start = CMD_EPG_Base + 8;
    // cfg command
    private static final int CMD_CFG_SetStringValue = CMD_CFG_Base + 1;
    private static final int CMD_CFG_SetIntValue = CMD_CFG_Base + 2;
    private static final int CMD_CFG_GetStringValue = CMD_CFG_Base + 3;
    private static final int CMD_CFG_GetIntValue = CMD_CFG_Base + 4;
    private static final int CMD_CFG_RestoreDefaultConfig = CMD_CFG_Base + 5;
    private static final int CMD_CFG_GetConfigFileIntValu = CMD_CFG_Base + 6;
    private static final int CMD_CFG_GetGpos = CMD_CFG_Base + 7;
    private static final int CMD_CFG_SetGpos = CMD_CFG_Base + 8;
    // Time control command
    private static final int CMD_TMCTL_GetCurrentTimeZone = CMD_TMCTL_Base + 1;
    private static final int CMD_TMCTL_GetCurrentSystemTime = CMD_TMCTL_Base + 2;
    private static final int CMD_TMCTL_SyncTime = CMD_TMCTL_Base + 3;
    private static final int CMD_TMCTL_SetTimeZone = CMD_TMCTL_Base + 4;
    private static final int CMD_TMCTL_GetSettingTOTStatus = CMD_TMCTL_Base + 5;
    private static final int CMD_TMCTL_GetSettingTDTStatus = CMD_TMCTL_Base + 6;
    private static final int CMD_TMCTL_GetDateTime = CMD_TMCTL_Base + 7;
    private static final int CMD_TMCTL_SecondToDate = CMD_TMCTL_Base + 8;
    private static final int CMD_TMCTL_DateToSecond = CMD_TMCTL_Base + 9;
    private static final int CMD_TMCTL_SetDateTime = CMD_TMCTL_Base + 10;
    private static final int CMD_TMCTL_GetSleepTime = CMD_TMCTL_Base + 11;
    private static final int CMD_TMCTL_SetWakeUpTime = CMD_TMCTL_Base + 12;
    private static final int CMD_TMCTL_SetTimeToSystem = CMD_TMCTL_Base + 13;

    // Book command
    private static final int CMD_BOOK_GetAllTasks = CMD_BOOK_Base + 1;
    private static final int CMD_BOOK_ClearAllTasks = CMD_BOOK_Base + 2;
    private static final int CMD_BOOK_AddTask = CMD_BOOK_Base + 3;
    private static final int CMD_BOOK_GetTaskByID = CMD_BOOK_Base + 4;
    private static final int CMD_BOOK_DeleteTask = CMD_BOOK_Base + 5;
    private static final int CMD_BOOK_UpdateTask = CMD_BOOK_Base + 6;
    private static final int CMD_BOOK_GetComingTask = CMD_BOOK_Base + 7;
    private static final int CMD_BOOK_FindConflictTasks = CMD_BOOK_Base + 8;
    */
    /**************************************************
     * DTV message ID form 2000(7D0) ~ 3000(BB0)
     ***************************************************/
    //Android 8
    private static final int CMD_JAVA_Base = 0x10000;

    private static final int CMD_Base               = 0x0;
    private static final int CMD_Common_Base        = CMD_Base + 0x100;
    private static final int CMD_AV_Base            = CMD_Base + 0x200;
    private static final int CMD_PVR_Base           = CMD_Base + 0x300;
    private static final int CMD_EPG_Base           = CMD_Base + 0x400;
    private static final int CMD_FE_Base            = CMD_Base + 0x500;
    private static final int CMD_PM_Base            = CMD_Base + 0x600;
    private static final int CMD_BOOK_Base          = CMD_Base + 0x700;
    private static final int CMD_CFG_Base           = CMD_Base + 0x800;
    private static final int CMD_TMCTL_Base         = CMD_Base + 0x900;
    private static final int CMD_CI_BASE            = CMD_Base + 0xA00;
    private static final int CMD_SSU_BASE           = CMD_Base + 0xB00;
    private static final int CMD_SUBT_Base          = CMD_Base + 0xC00;
    private static final int CMD_TTX_BASE           = CMD_Base + 0xD00;
    private static final int CMD_CC_BASE            = CMD_Base + 0xE00;
    private static final int CMD_TEST_BASE          = CMD_Base + 0xF00;
    private static final int CMD_CS_Base             = CMD_Base + 0x1000;
    private static final int CMD_PIP_BASE           = CMD_Base + 0x1200;
    private static final int CMX_VMX_BASE          = CMD_Base + 0x1300;
    private static final int CMD_PIO_BASE           = CMD_Base + 0x1400;
    private static final int CMD_LOADERDTV_BASE   = CMD_Base + 0x1500;
    private static final int CMD_DEVICE_INFO_Base = CMD_Base + 0x1600;
    private static final int CMD_CA_Base  = CMD_Base + 0x1700;//eric lin 20210107 widevine cas , add by gary

    // Common Command
    private static final int CMD_COMM_PrepareDTV            = CMD_Common_Base + 0x01;
    private static final int CMD_COMM_UnPrepareDTV          = CMD_Common_Base + 0x02;
    private static final int CMD_COMM_SetSourceType         = CMD_Common_Base + 0x03;
    private static final int CMD_COMM_GetSourceType         = CMD_Common_Base + 0x04;
    private static final int CMD_COMM_GetLastDtvSourceType  = CMD_Common_Base + 0x05;
    private static final int CMD_COMM_GetSupportSourceType  = CMD_Common_Base + 0x06;
    private static final int CMD_COMM_FactoryReset          = CMD_Common_Base + 0x07;
    private static final int CMD_COMM_GetStackVersion       = CMD_Common_Base + 0x08;
    private static final int CMD_COMM_GetPesiServiceVersion      = CMD_Common_Base + 101;
    private static final int CMD_COMM_TestInvoke                = CMD_Common_Base+102;
    // AV Command
    private static final int CMD_AV_Open                = CMD_AV_Base + 0x01;
    private static final int CMD_AV_Close               = CMD_AV_Base + 0x02;
    private static final int CMD_AV_Start               = CMD_AV_Base + 0x03;
    private static final int CMD_AV_Stop                = CMD_AV_Base + 0x04;
    private static final int CMD_AV_SetAudioLanguage    = CMD_AV_Base + 0x05;
    private static final int CMD_AV_GetAudioLanguage    = CMD_AV_Base + 0x06;
    private static final int CMD_AV_GetMutiAudioInfo    = CMD_AV_Base + 0x07;
    private static final int CMD_AV_ChangeAudioTrack    = CMD_AV_Base + 0x08;
    private static final int CMD_AV_SetAudioTrackMode   = CMD_AV_Base + 0x09;
    private static final int CMD_AV_GetAudioTrackMode   = CMD_AV_Base + 0x0A;
    private static final int CMD_AV_SetWindowSize       = CMD_AV_Base + 0x0B;
    private static final int CMD_AV_ShowVideo           = CMD_AV_Base + 0x0C;
    private static final int CMD_AV_FreezeVideo         = CMD_AV_Base + 0x0D;
    private static final int CMD_AV_SetMute             = CMD_AV_Base + 0x0E;
    private static final int CMD_AV_GetMute             = CMD_AV_Base + 0x0F;
    private static final int CMD_AV_SetStopMode         = CMD_AV_Base + 0x10;
    private static final int CMD_AV_GetStopMode         = CMD_AV_Base + 0x11;
    private static final int CMD_AV_GetAdAttr           = CMD_AV_Base + 0x12;
    private static final int CMD_AV_SetAdAttr           = CMD_AV_Base + 0x13;
    private static final int CMD_AV_GetAVStatus         = CMD_AV_Base + 0x14;
    private static final int CMD_AV_EwsActionControl    = CMD_AV_Base + 0x15;
    private static final int CMD_AV_GetDRARawChannel    = CMD_AV_Base + 0x16;
    private static final int CMD_AV_GetWindowSize       = CMD_AV_Base + 0x17;
    private static final int CMD_AV_GetAspectRatio      = CMD_AV_Base + 0x64;
    private static final int CMD_AV_SetAudioOuputMode   = CMD_AV_Base + 0x65;
    private static final int CMD_AV_SetDispRatio        = CMD_AV_Base + 0x66;
    private static final int CMD_AV_PreStart            = CMD_AV_Base + 0x67;//Scoty 20180816 add fast change channel
    private static final int CMD_AV_MIC_SetInputGain    = CMD_AV_Base + 0x68;
    private static final int CMD_AV_MIC_SetAlcGain      = CMD_AV_Base + 0x69;
    private static final int CMD_AV_MIC_SetLRInputGain  = CMD_AV_Base + 0x6A;
    private static final int CMD_AV_GetVideoErrorFrameCount  = CMD_AV_Base + 0x6B;

    // PVR command
    private static final int CMD_PVR_Record_Start           = CMD_PVR_Base + 0x01;
    private static final int CMD_PVR_Record_Stop            = CMD_PVR_Base + 0x02;
    private static final int CMD_PVR_Record_Pause           = CMD_PVR_Base + 0x03;
    private static final int CMD_PVR_Record_Resume          = CMD_PVR_Base + 0x04;
    private static final int CMD_PVR_Record_GetRecTime      = CMD_PVR_Base + 0x05;
    private static final int CMD_PVR_Record_GetInfo         = CMD_PVR_Base + 0x06;
    private static final int CMD_PVR_Record_Start_V2        = CMD_PVR_Base + 0x07;

    private static final int CMD_PVR_Play_Start             = CMD_PVR_Base + 0x10;
    private static final int CMD_PVR_Play_Stop              = CMD_PVR_Base + 0x11;
    private static final int CMD_PVR_Play_Pause             = CMD_PVR_Base + 0x12;
    private static final int CMD_PVR_Play_Resume            = CMD_PVR_Base + 0x13;
    private static final int CMD_PVR_Play_Trick             = CMD_PVR_Base + 0x14;
    private static final int CMD_PVR_Play_Seek              = CMD_PVR_Base + 0x15;
    private static final int CMD_PVR_Play_GetPlayTime       = CMD_PVR_Base + 0x16;
    private static final int CMD_PVR_Play_GetInfo           = CMD_PVR_Base + 0x17;
    private static final int CMD_PVR_Play_GetMutiAudioInfo  = CMD_PVR_Base + 0x18;
    private static final int CMD_PVR_Play_ChangeAudioTrack  = CMD_PVR_Base + 0x19;
    private static final int CMD_PVR_Play_SetWindowSize     = CMD_PVR_Base + 0x1A;
    private static final int CMD_PVR_Play_SetAudioTrackMode = CMD_PVR_Base + 0x1B;
    private static final int CMD_PVR_Play_GetAudioTrackMode = CMD_PVR_Base + 0x1C;

    private static final int CMD_PVR_File_Remove            = CMD_PVR_Base + 0x20;
    private static final int CMD_PVR_File_Rename            = CMD_PVR_Base + 0x21;
    private static final int CMD_PVR_File_GetInfo           = CMD_PVR_Base + 0x22;
    private static final int CMD_PVR_File_GetExtraInfo      = CMD_PVR_Base + 0x5F;

    private static final int CMD_PVR_TimeShift_Start        = CMD_PVR_Base + 0x30;
    private static final int CMD_PVR_TimeShift_Stop         = CMD_PVR_Base + 0x31;
    private static final int CMD_PVR_TimeShift_Play         = CMD_PVR_Base + 0x32;
    private static final int CMD_PVR_TimeShift_Pause        = CMD_PVR_Base + 0x33;
    private static final int CMD_PVR_TimeShift_Resume       = CMD_PVR_Base + 0x34;
    private static final int CMD_PVR_TimeShift_Trick        = CMD_PVR_Base + 0x35;
    private static final int CMD_PVR_TimeShift_Seek         = CMD_PVR_Base + 0x36;
    private static final int CMD_PVR_TimeShift_GetPlayTime  = CMD_PVR_Base + 0x37;
    private static final int CMD_PVR_TimeShift_GetStartTime = CMD_PVR_Base + 0x38;
    private static final int CMD_PVR_TimeShift_GetRecTime   = CMD_PVR_Base + 0x39;
    private static final int CMD_PVR_TimeShift_GetInfo      = CMD_PVR_Base + 0x3A;
    private static final int CMD_PVR_TimeShift_Start_V2     = CMD_PVR_Base + 0x3B;

    private static final int CMD_PVR_Record_GetInfo_TypeStatus          = CMD_PVR_Base + 0x50;
    private static final int CMD_PVR_Record_GetInfo_TypeFileFullpath    = CMD_PVR_Base + 0x51;
    private static final int CMD_PVR_Record_GetInfo_TypeProginfo        = CMD_PVR_Base + 0x52;
    private static final int CMD_PVR_Play_GetInfo_TypeFileAttr          = CMD_PVR_Base + 0x53;
    private static final int CMD_PVR_Play_GetInfo_TypeStatus            = CMD_PVR_Base + 0x54;
    private static final int CMD_PVR_Play_GetInfo_TypeFileFullpath      = CMD_PVR_Base + 0x55;
    private static final int CMD_PVR_Play_GetInfo_TypeAvplayStatus      = CMD_PVR_Base + 0x56;
    private static final int CMD_PVR_TimeShift_GetInfo_TypeStatus       = CMD_PVR_Base + 0x57;
    private static final int CMD_PVR_GetPesiPVRmode                     = CMD_PVR_Base + 0x58;
    private static final int CMD_PVR_GetAspectRatio                     = CMD_PVR_Base + 0x59;
    private static final int CMD_PVR_Record_Check                       = CMD_PVR_Base + 0x5A;
    private static final int CMD_PVR_Record_GetAllInfo                  = CMD_PVR_Base + 0x5B;
    private static final int CMD_PVR_Record_GetMaxRecNum                = CMD_PVR_Base + 0x5C;
    private static final int CMD_PVR_SetStartPositionFlag               = CMD_PVR_Base + 0x5D;
    private static final int CMD_PVR_PlayFile_CheckLastViewPoint		= CMD_PVR_Base + 0x5E;
    private static final int CMD_PVR_Record_AllTs_Start                 = CMD_PVR_Base + 0x60;
    private static final int CMD_PVR_Record_AllTs_Stop                  = CMD_PVR_Base + 0x61;
    private static final int CMD_PVR_SetParentLockOK                    = CMD_PVR_Base + 0x62;
    private static final int CMD_PVR_Total_Record_File_Open             = CMD_PVR_Base + 0x63;
    private static final int CMD_PVR_Total_Record_File_Close            = CMD_PVR_Base + 0x64;
    private static final int CMD_PVR_Total_Record_File_Sort             = CMD_PVR_Base + 0x65;
    private static final int CMD_PVR_Total_Record_File_Get              = CMD_PVR_Base + 0x66;
    private static final int CMD_PVR_Open_Hard_Disk                     = CMD_PVR_Base + 0x67;//Scoty 20180827 add HDD Ready command and callback
    private static final int CMD_PVR_TimeShift_PlayStop                 = CMD_PVR_Base + 0x68;//Scoty 20180827 add and modify TimeShift Live Mode
    private static final int CMD_PVR_TimeShift_GetLivePauseTime         = CMD_PVR_Base + 0x69;//Scoty 20180827 add and modify TimeShift Live Mode
    private static final int CMD_PVR_File_GetEPGInfo                    = CMD_PVR_Base + 0x6A;
    private static final int CMD_PVR_Get_Total_Rec_Num                  = CMD_PVR_Base + 0x6B;
    private static final int CMD_PVR_Get_Records_File                   = CMD_PVR_Base + 0x6C;
    private static final int CMD_PVR_Get_Total_One_Series_Rec_Num       = CMD_PVR_Base + 0x6D;
    private static final int CMD_PVR_Get_One_Series_Records_File        = CMD_PVR_Base + 0x6E;
    private static final int CMD_PVR_Delete_Total_Records_File          = CMD_PVR_Base + 0x6F;
    private static final int CMD_PVR_Delete_One_Series_Folder           = CMD_PVR_Base + 0x70;
    private static final int CMD_PVR_Delete_Record_File_By_Ch_Id        = CMD_PVR_Base + 0x71;
    private static final int CMD_PVR_Delete_Record_File                 = CMD_PVR_Base + 0x72;

    // EPG command
    private static final int CMD_EPG_GetPresentEvent        = CMD_EPG_Base + 0x01;
    private static final int CMD_EPG_GetFollowEvent         = CMD_EPG_Base + 0x02;
    private static final int CMD_EPG_GetByEventId           = CMD_EPG_Base + 0x03;
    private static final int CMD_EPG_GetEventByTime         = CMD_EPG_Base + 0x04;
    private static final int CMD_EPG_GetShortDesc           = CMD_EPG_Base + 0x05;
    private static final int CMD_EPG_GetDetailDesc          = CMD_EPG_Base + 0x06;
    private static final int CMD_EPG_SetLanguageCode        = CMD_EPG_Base + 0x07;
    private static final int CMD_EPG_GetLanguageCode        = CMD_EPG_Base + 0x08;
    private static final int CMD_EPG_GetParentRate          = CMD_EPG_Base + 0x09;

    private static final int CMD_EPG_Start                  = CMD_EPG_Base + 0x0A;//TODO

    // FrontEnd command
    private static final int CMD_FE_SetAntennaType          = CMD_FE_Base + 0x01;
    private static final int CMD_FE_GetAntennaType          = CMD_FE_Base + 0x02;
    private static final int CMD_FE_SetAntennaPower         = CMD_FE_Base + 0x03; // for atsc & dvb-s
    private static final int CMD_FE_GetAntennaPower         = CMD_FE_Base + 0x04; // for atsc & dvb-s
    private static final int CMD_FE_SetConnectParam         = CMD_FE_Base + 0x05;
    private static final int CMD_FE_Disconnect              = CMD_FE_Base + 0x06;
    private static final int CMD_FE_SetFrequency            = CMD_FE_Base + 0x07;
    private static final int CMD_FE_GetFrequency            = CMD_FE_Base + 0x08;
    private static final int CMD_FE_SetBandwidth            = CMD_FE_Base + 0x09;
    private static final int CMD_FE_GetBandwidth            = CMD_FE_Base + 0x0A;
    private static final int CMD_FE_SetSymbolRate           = CMD_FE_Base + 0x0B;
    private static final int CMD_FE_GetSymbolRate           = CMD_FE_Base + 0x0C;
    private static final int CMD_FE_SetModulation           = CMD_FE_Base + 0x0D;
    private static final int CMD_FE_GetModulation           = CMD_FE_Base + 0x0E;
    private static final int CMD_FE_GetLockStatus           = CMD_FE_Base + 0x0F;
    private static final int CMD_FE_GetSignalQuality        = CMD_FE_Base + 0x10;
    private static final int CMD_FE_GetSignalStrength       = CMD_FE_Base + 0x11;
    private static final int CMD_FE_SetFakeTuner            = CMD_FE_Base + 0x12;
    private static final int CMD_FE_GetBer                  = CMD_FE_Base + 0x13; // Johnny 20190221 add for mtest ber

    private static final int CMD_FE_SetSat                  = CMD_FE_Base + 0x20;
    private static final int CMD_FE_GetSat                  = CMD_FE_Base + 0x21;
    private static final int CMD_FE_SetSatAntenna           = CMD_FE_Base + 0x22;
    private static final int CMD_FE_SetLocalCoordinate      = CMD_FE_Base + 0x23;
    private static final int CMD_FE_SetSatLNBParameter      = CMD_FE_Base + 0x24;
    private static final int CMD_FE_SetSat22kSwitch         = CMD_FE_Base + 0x25;
    private static final int CMD_FE_SetDiSEqC10             = CMD_FE_Base + 0x26;
    private static final int CMD_FE_SetDiSEqC11             = CMD_FE_Base + 0x27;
    private static final int CMD_FE_SetMotorLimitPosition   = CMD_FE_Base + 0x28;
    private static final int CMD_FE_MoveMotor               = CMD_FE_Base + 0x29;
    private static final int CMD_FE_StopMoveMotor           = CMD_FE_Base + 0x2A;
    private static final int CMD_FE_ResetMotor              = CMD_FE_Base + 0x2B;
    private static final int CMD_FE_SetMotorAutoRotation    = CMD_FE_Base + 0x2C;
    private static final int CMD_FE_StoreSatPosition        = CMD_FE_Base + 0x2D;
    private static final int CMD_FE_GotoSatPosition         = CMD_FE_Base + 0x2E;
    private static final int CMD_FE_CalUSALSAngle           = CMD_FE_Base + 0x2F;
    private static final int CMD_FE_GotoUSALSAngle          = CMD_FE_Base + 0x30;

    private static final int CMD_FE_SetFrequencyTable       = CMD_FE_Base + 0x40; // for atsc
    private static final int CMD_FE_GetFrequencyTable       = CMD_FE_Base + 0x41; // for atsc
    private static final int CMD_FE_SetScanType             = CMD_FE_Base + 0x42; // for atsc
    private static final int CMD_FE_GetScanType             = CMD_FE_Base + 0x43;
    private static final int CMD_FE_AutoScan                = CMD_FE_Base + 0x44;
    private static final int CMD_FE_StepScan                = CMD_FE_Base + 0x45;
    private static final int CMD_FE_ManualScan              = CMD_FE_Base + 0x46;
    private static final int CMD_FE_NitScan                 = CMD_FE_Base + 0x47;
    private static final int CMD_FE_SatelliteManualScan     = CMD_FE_Base + 0x48;
    private static final int CMD_FE_SatelliteBlindScan      = CMD_FE_Base + 0x49;
    private static final int CMD_FE_AbortScan               = CMD_FE_Base + 0x4A;
    private static final int CMD_FE_AtvFineTune             = CMD_FE_Base + 0x4B;

    private static final int CMD_PM_GetLastChannelID        = CMD_PM_Base + 0x01;
    private static final int CMD_PM_GetPreviousChannelID    = CMD_PM_Base + 0x02;
    private static final int CMD_PM_GetChannelUpID          = CMD_PM_Base + 0x03;
    private static final int CMD_PM_GetChannelDownID        = CMD_PM_Base + 0x04;
    private static final int CMD_PM_GetChannelByID          = CMD_PM_Base + 0x05;
    private static final int CMD_PM_GetChannelByLCN         = CMD_PM_Base + 0x06;

    // Channel edit CMD
    private static final int CMD_PM_SetChannel              = CMD_PM_Base + 0x10;
    private static final int CMD_PM_DelChannelByID          = CMD_PM_Base + 0x11;
    private static final int CMD_PM_DelAllChannel           = CMD_PM_Base + 0x12;
    private static final int CMD_PM_ClearAllChannelsInAllAntenna = CMD_PM_Base + 0x13;
    private static final int CMD_PM_LockChannel             = CMD_PM_Base + 0x14;
    private static final int CMD_PM_IsChannelLocked         = CMD_PM_Base + 0x15;
    private static final int CMD_PM_SkipChannel             = CMD_PM_Base + 0x16;
    private static final int CMD_PM_IsChannelSkipped        = CMD_PM_Base + 0x17;
    private static final int CMD_PM_ChannelRename           = CMD_PM_Base + 0x18;
    private static final int CMD_PM_ChannelMoveTo           = CMD_PM_Base + 0x19;
    private static final int CMD_PM_ChannelSwap             = CMD_PM_Base + 0x1A;
    private static final int CMD_PM_ChannelSort             = CMD_PM_Base + 0x1B;

    // Group CMD
    private static final int CMD_PM_AddChannelToFavList     = CMD_PM_Base + 0x20;
    private static final int CMD_PM_RemoveChannelFromFavList = CMD_PM_Base + 0x21;
    private static final int CMD_PM_SetServiceMode          = CMD_PM_Base + 0x22;
    private static final int CMD_PM_GetServiceMode          = CMD_PM_Base + 0x23;
    private static final int CMD_PM_GetChannelNum           = CMD_PM_Base + 0x24;
    private static final int CMD_PM_GetChannelList          = CMD_PM_Base + 0x25;
    private static final int CMD_PM_GetChannelIndexInList   = CMD_PM_Base + 0x26;

    // Store CMD
    private static final int CMD_PM_SaveToFlash             = CMD_PM_Base + 0x30;
    private static final int CMD_PM_RecoverFromFlash        = CMD_PM_Base + 0x31;
    private static final int CMD_PM_GetPresetFrequencyNum   = CMD_PM_Base + 0x32;
    private static final int CMD_PM_GetPresetFrequency      = CMD_PM_Base + 0x33;

    // Satellite TP CMD
    private static final int CMD_PM_AddSatellite            = CMD_PM_Base + 0x40;
    private static final int CMD_PM_DelSatellite            = CMD_PM_Base + 0x41;
    private static final int CMD_PM_DelAllSatellite         = CMD_PM_Base + 0x42;
    private static final int CMD_PM_SatelliteRename         = CMD_PM_Base + 0x43;
    private static final int CMD_PM_GetSatellite            = CMD_PM_Base + 0x44;
    private static final int CMD_PM_GetSatelliteNum         = CMD_PM_Base + 0x45;
    private static final int CMD_PM_GetSatelliteList        = CMD_PM_Base + 0x46;
    private static final int CMD_PM_GetSatelliteAntennaInfo = CMD_PM_Base + 0x47;
    private static final int CMD_PM_SetSatelliteAntennaInfo = CMD_PM_Base + 0x48;
    private static final int CMD_PM_SetSatelliteLongitude   = CMD_PM_Base + 0x49;
    private static final int CMD_PM_SatelliteAddTp          = CMD_PM_Base + 0x4A;
    private static final int CMD_PM_GetSatelliteTpNum       = CMD_PM_Base + 0x4B;
    private static final int CMD_PM_GetSatelliteTpList      = CMD_PM_Base + 0x4C;
    private static final int CMD_PM_GetSatelliteTpByID      = CMD_PM_Base + 0x4D;
    private static final int CMD_PM_SetSatelliteTp          = CMD_PM_Base + 0x4E;
    private static final int CMD_PM_DelSatelliteTp          = CMD_PM_Base + 0x4F;
    private static final int CMD_PM_DelSatelliteAllTp       = CMD_PM_Base + 0x50;
    private static final int CMD_PM_GetFreqInfoByChannel    = CMD_PM_Base + 0x51;

    private static final int CMD_PM_SaveSampleChannelList   = CMD_PM_Base + 209;
    private static final int CMD_PM_SaveChannel             = CMD_PM_Base + 210;
    private static final int CMD_PM_SaveGroupList           = CMD_PM_Base + 211;
    private static final int CMD_PM_SaveTpList              = CMD_PM_Base + 212;
    private static final int CMD_PM_SaveSatList             = CMD_PM_Base + 213;
    private static final int CMD_PM_GetGroupChannel         = CMD_PM_Base + 214;
    private static final int CMD_PM_GetGroupChannelList     = CMD_PM_Base + 215;
    private static final int CMD_PM_DelGroupChannel         = CMD_PM_Base + 216;
    private static final int CMD_PM_DelGroupAll             = CMD_PM_Base + 217;
    private static final int CMD_PM_GetSimpleChannelList    = CMD_PM_Base + 218;
    private static final int CMD_PM_GetSimpleChannel        = CMD_PM_Base + 219;
    private static final int CMD_PM_SaveGroupName           = CMD_PM_Base + 220;
    private static final int CMD_PM_GetGroupName            = CMD_PM_Base + 221;
    private static final int CMD_PM_GetChannelFilter        = CMD_PM_Base + 222;

    // Book command
    private static final int CMD_BOOK_GetNum                = CMD_BOOK_Base + 0x01;
    private static final int CMD_BOOK_GetBookByRowID        = CMD_BOOK_Base + 0x02;
    private static final int CMD_BOOK_GetBookByType         = CMD_BOOK_Base + 0x03;
    private static final int CMD_BOOK_FindConflictbooks     = CMD_BOOK_Base + 0x04;
    private static final int CMD_BOOK_AddBook               = CMD_BOOK_Base + 0x05;
    private static final int CMD_BOOK_UpdateBook            = CMD_BOOK_Base + 0x06;
    private static final int CMD_BOOK_DeleteBookByRowID     = CMD_BOOK_Base + 0x07;
    private static final int CMD_BOOK_DeleteBookByProg      = CMD_BOOK_Base + 0x08;
    private static final int CMD_BOOK_ClearAllBooks         = CMD_BOOK_Base + 0x09;
    private static final int CMD_BOOK_GetComingBook         = CMD_BOOK_Base + 0x0A;
    private static final int CMD_BOOK_GetAllBooks             = CMD_BOOK_Base + 0x0B;
    private static final int CMD_BOOK_SaveBookList              = CMD_BOOK_Base + 0x0C;

    // Setting command
    private static final int CMD_CFG_GetCountryCode         = CMD_CFG_Base + 0x01;
    private static final int CMD_CFG_SetCountryCode         = CMD_CFG_Base + 0x02;
    private static final int CMD_CFG_GetAreaCode            = CMD_CFG_Base + 0x03;
    private static final int CMD_CFG_SetAreaCode            = CMD_CFG_Base + 0x04;
    private static final int CMD_CFG_GetStringValue         = CMD_CFG_Base + 0x05;
    private static final int CMD_CFG_SetStringValue         = CMD_CFG_Base + 0x06;
    private static final int CMD_CFG_GetIntValue            = CMD_CFG_Base + 0x07;
    private static final int CMD_CFG_SetIntValue            = CMD_CFG_Base + 0x08;
    private static final int CMD_CFG_GetGpos                = CMD_CFG_Base + 50;
    private static final int CMD_CFG_SetGpos                = CMD_CFG_Base + 51;
    private static final int CMD_CFG_SetStandbyOnOff         = CMD_CFG_Base + 52;
    private static final int CMD_CFG_SetInViewActivity          = CMD_CFG_Base + 53;
    private static final int CMD_CFG_EnableMemStatusCheck     = CMD_CFG_Base + 54; /// test
    private static final int CMD_CFG_GetProtectData = CMD_CFG_Base + 55;
    private static final int CMD_CFG_SetProtectData = CMD_CFG_Base + 56;
    private static final int CMD_CFG_GetChipID = CMD_CFG_Base + 57;

    // Time control command
    private static final int CMD_TMCTL_GetDateTime          = CMD_TMCTL_Base + 0x01;
    private static final int CMD_TMCTL_SetDateTime          = CMD_TMCTL_Base + 0x02;
    private static final int CMD_TMCTL_GetTimeZone          = CMD_TMCTL_Base + 0x03;
    private static final int CMD_TMCTL_SetTimeZone          = CMD_TMCTL_Base + 0x04;
    private static final int CMD_TMCTL_GetDaylightSaving    = CMD_TMCTL_Base + 0x05;
    private static final int CMD_TMCTL_SetDaylightSaving    = CMD_TMCTL_Base + 0x06;
    private static final int CMD_TMCTL_DateTimeToSecond     = CMD_TMCTL_Base + 0x07;
    private static final int CMD_TMCTL_SecondToDateTime     = CMD_TMCTL_Base + 0x08;
    private static final int CMD_TMCTL_AutoSyncTimeFromDtv  = CMD_TMCTL_Base + 0x09;
    private static final int CMD_TMCTL_AutoSyncTimeZoneFromDtv = CMD_TMCTL_Base + 0x0A;
    private static final int CMD_TMCTL_GetCurSleepDuration  = CMD_TMCTL_Base + 0x0B;
    private static final int CMD_TMCTL_SetWakeupInterval    = CMD_TMCTL_Base + 0x0C;


    //CI Command
    private static final int CMD_CI_GetCardStatus           = CMD_CI_BASE + 0x01;
    private static final int CMD_CI_EnterMainMenu           = CMD_CI_BASE + 0x02;
    private static final int CMD_CI_EnterSubMenu            = CMD_CI_BASE + 0x03;
    private static final int CMD_CI_IsMenuShow              = CMD_CI_BASE + 0x04;
    private static final int CMD_CI_GetMenuTitle            = CMD_CI_BASE + 0x05;
    private static final int CMD_CI_GetMenuSubTitle         = CMD_CI_BASE + 0x06;
    private static final int CMD_CI_GetMenuBottomTitle      = CMD_CI_BASE + 0x07;
    private static final int CMD_CI_GetMenuChoiceNum        = CMD_CI_BASE + 0x08;
    private static final int CMD_CI_GetMenuChoiceInfo       = CMD_CI_BASE + 0x09;
    private static final int CMD_CI_GetCurMMIType           = CMD_CI_BASE + 0x0A;
    private static final int CMD_CI_BackToPreMenu           = CMD_CI_BASE + 0x0B;
    private static final int CMD_CI_ColseMainMenu           = CMD_CI_BASE + 0x0C;
    private static final int CMD_CI_GetEnqContent           = CMD_CI_BASE + 0x0D;
    private static final int CMD_CI_GetEnqLength            = CMD_CI_BASE + 0x0E;
    private static final int CMD_CI_GetEnqBlindMode         = CMD_CI_BASE + 0x0F;
    private static final int CMD_CI_SetEnqAnswer            = CMD_CI_BASE + 0x10;
    private static final int CMD_CI_IsMenuHaveDate          = CMD_CI_BASE + 0x11;
    private static final int CMD_CI_SetPassMode             = CMD_CI_BASE + 0x12;

    // SSU cmd
    private static final int CMD_SSU_StartOtaMonitor        = CMD_SSU_BASE + 0x01;
    private static final int CMD_SSU_StopOtaMonitor         = CMD_SSU_BASE + 0x02;
    private static final int CMD_SSU_StartDownloadOtaFile   = CMD_SSU_BASE + 0x03;

    private static final int CMD_SSU_PESI_USB               = CMD_SSU_BASE + 0x64;
    private static final int CMD_SSU_PESI_OTA_DVB_C         = CMD_SSU_BASE + 0x65;
    private static final int CMD_SSU_PESI_OTA_DVB_S         = CMD_SSU_BASE + 0x66;
    private static final int CMD_SSU_PESI_OTA_DVB_T         = CMD_SSU_BASE + 0x67;
    private static final int CMD_SSU_PESI_OTA_DVB_T2        = CMD_SSU_BASE + 0x68;
    private static final int CMD_SSU_PESI_IP_1              = CMD_SSU_BASE + 0x69;
    private static final int CMD_SSU_PESI_IP_2              = CMD_SSU_BASE + 0x6A;
    private static final int CMD_SSU_PESI_FS              = CMD_SSU_BASE + 0x6B;
    private static final int CMD_SSU_PESI_GET_OTA_PARAMES       = CMD_SSU_BASE + 0x6C;
    private static final int CMD_SSU_PESI_OTA_ISDBT         = CMD_SSU_BASE + 0x6D;
    private static final int CMD_SSU_PESI_MTEST_OTA         = CMD_SSU_BASE + 0x6E;//Scoty 20190410 add Mtest Trigger OTA command
    private static final int CMD_SSU_PESI_MTEST_ENABLE_OPT    = CMD_SSU_BASE + 0x6F;

    //subt
    private static final int CMD_SUBT_SetMode               = CMD_SUBT_Base + 0x01;
    private static final int CMD_SUBT_GetMode               = CMD_SUBT_Base + 0x02;
    private static final int CMD_SUBT_GetList               = CMD_SUBT_Base + 0x03;
    private static final int CMD_SUBT_Switch                = CMD_SUBT_Base + 0x04;
    private static final int CMD_SUBT_GetLang               = CMD_SUBT_Base + 0x05;
    private static final int CMD_SUBT_SetLang               = CMD_SUBT_Base + 0x06;
    private static final int CMD_SUBT_SetHohPreferred       = CMD_SUBT_Base + 0x07;
    private static final int CMD_SUBT_GetHohPreferred       = CMD_SUBT_Base + 0x08;

    //ttx
    private static final int CMD_TTX_Show                   = CMD_TTX_BASE + 0x01;
    private static final int CMD_TTX_Hide                   = CMD_TTX_BASE + 0x02;
    private static final int CMD_TTX_IsShow                 = CMD_TTX_BASE + 0x03;
    private static final int CMD_TTX_IsAvailable            = CMD_TTX_BASE + 0x04;
    private static final int CMD_TTX_SetLanguage            = CMD_TTX_BASE + 0x05;
    private static final int CMD_TTX_GetLanguage            = CMD_TTX_BASE + 0x06;
    private static final int CMD_TTX_GetCurrentPage         = CMD_TTX_BASE + 0x07;
    private static final int CMD_TTX_SetInitPage            = CMD_TTX_BASE + 0x08;
    private static final int CMD_TTX_SetCommand             = CMD_TTX_BASE + 0x09;
    private static final int CMD_TTX_SetRegion              = CMD_TTX_BASE + 0x0A;
    private static final int CMD_TTX_GetRegion              = CMD_TTX_BASE + 0x0B;
    private static final int CMD_TTX_GetLangInfo            = CMD_TTX_BASE + 0x0C;


    //Test
    private static final int CMD_TEST_START_INJECT          = CMD_TEST_BASE + 0x01;
    private static final int CMD_TEST_STOP_INJECT           = CMD_TEST_BASE + 0x02;
    private static final int CMD_TEST_FE_SET_FAKE_MODE      = CMD_TEST_BASE + 0x03;
    private static final int CMD_TEST_GetGPIOStatus             = CMD_TEST_BASE + 201;
    private static final int CMD_TEST_SetGPIOStatus             = CMD_TEST_BASE + 202;
    private static final int CMD_TEST_GetATRStatus             = CMD_TEST_BASE + 203;
    private static final int CMD_TEST_GetHDCPStatus             = CMD_TEST_BASE + 204;
    private static final int CMD_TEST_PowerSave                 = CMD_TEST_BASE + 205;
    private static final int CMD_TEST_SevenSegment             = CMD_TEST_BASE + 206;
    private static final int CMD_TEST_SET_CH                   = CMD_TEST_BASE + 207;
    private static final int CMD_TEST_CHANGE_TUNER             = CMD_TEST_BASE + 208;//Scoty 20180817 add Change Tuner Command
    private static final int CMD_TEST_USB_READ_WRITE           = CMD_TEST_BASE + 209;
    private static final int CMD_TEST_AV_MultiPlay              = CMD_TEST_BASE + 210; // Johnny 20181221 for mtest split screen
    private static final int CMD_TEST_AV_StopByTunerId           = CMD_TEST_BASE + 211; // Johnny 20190221 for mtest stop multi
    private static final int CMD_TEST_START_MTEST           = CMD_TEST_BASE + 212; // Johnny 20190320 for mtest
    private static final int CMD_TEST_CONNECT_PCTOOL           = CMD_TEST_BASE + 213; // Johnny 20190320 for mtest connect pctool
    private static final int CMD_TEST_WIFI_TX_RX_LEVEL          = CMD_TEST_BASE + 214;//Scoty 20190417 add wifi level command
    private static final int CMD_TEST_CHECK_KEY                  = CMD_TEST_BASE + 215; // Johnny 20190522 check key before OTA
    private static final int CMD_TEST_GetHDMIStatus             = CMD_TEST_BASE + 216;
    private static final int CMD_TEST_SetLEDOnOff             = CMD_TEST_BASE + 217;
    private static final int CMD_TEST_FrontKey             = CMD_TEST_BASE + 218;
    private static final int CMD_MOD_SetPowerLedColor            = CMD_TEST_BASE + 219;
    private static final int CMD_MOD_SetEthLinkLedColor            = CMD_TEST_BASE + 220;
    private static final int CMD_MOD_SetPPPoEWifiLedColor            = CMD_TEST_BASE + 221;
    private static final int CMD_MOD_GetHDMISupportList            = CMD_TEST_BASE + 222;
    private static final int CMD_MOD_SetHDMIResolution            = CMD_TEST_BASE + 223;



    //CS
    private static final int CMD_CS_SingleFreqSearch               = CMD_CS_Base + 0x01;
    private static final int CMD_CS_SegmentSearch                 = CMD_CS_Base + 0x02;
    private static final int CMD_CS_AutoSearch                    = CMD_CS_Base + 0x03;
    private static final int CMD_CS_NITSearch                     = CMD_CS_Base + 0x04;
    private static final int CMD_CS_MultiTransponderSearch          = CMD_CS_Base + 0x05;
    private static final int CMD_CS_SingleSatBlindSearch             = CMD_CS_Base + 0x06;
    private static final int CMD_CS_SteppingSearch                 = CMD_CS_Base + 0x07;
    private static final int CMD_CS_StopSearch                     = CMD_CS_Base + 0x08;
    private static final int CMD_CS_PauseSearch                    = CMD_CS_Base + 0x09;
    private static final int CMD_CS_ResumeSearch                  = CMD_CS_Base + 0x0A;
    private static final int CMD_CS_GetSearchProgress               = CMD_CS_Base + 0x0B;
    private static final int CMD_CS_GetSearchStatisticalInfo           = CMD_CS_Base + 0x0C;
    private static final int CMD_CS_AllSatSearch                    = CMD_CS_Base + 0x0D;
    private static final int CMD_CS_BlockSearch                         = CMD_CS_Base + 0x0E;
    //PIP
    private static final int CMD_PIP_OPEN                            = CMD_PIP_BASE + 0x01;
    private static final int CMD_PIP_CLOSE                           = CMD_PIP_BASE + 0x02;
    private static final int CMD_PIP_START                           = CMD_PIP_BASE + 0x03;
    private static final int CMD_PIP_STOP                            = CMD_PIP_BASE + 0x04;
    private static final int CMD_PIP_SET_WINSIZE                     = CMD_PIP_BASE + 0x05;
    private static final int CMD_PIP_EXCHANGE                        = CMD_PIP_BASE + 0x06;

    //VMX
    private static final int CMD_VMX_GET_EMM_CNT               = CMX_VMX_BASE + 0x01;
    private static final int CMD_VMX_GET_ECM_CNT               = CMX_VMX_BASE + 0x02;
    private static final int CMD_VMX_GET_PAIR                    = CMX_VMX_BASE + 0x03;
    private static final int CMD_VMX_GET_PURSE                  = CMX_VMX_BASE + 0x04;
    private static final int CMD_VMX_SET_PINCODE                = CMX_VMX_BASE + 0x05;
    private static final int CMD_VMX_SET_PPTV                    = CMX_VMX_BASE + 0x06;
    private static final int CMD_VMX_SET_OSM_OK                 = CMX_VMX_BASE + 0x07;
    private static final int CMD_VMX_GET_CHIPID                  = CMX_VMX_BASE + 0x08;
    private static final int CMD_VMX_GET_LIBDATE                = CMX_VMX_BASE + 0x09;
    private static final int CMD_VMX_GET_SN                      = CMX_VMX_BASE + 0x0A;
    private static final int CMD_VMX_GET_CAVER                  = CMX_VMX_BASE + 0x0B;
    private static final int CMD_VMX_GET_SCNUM                  = CMX_VMX_BASE + 0x0C;
    private static final int CMD_VMX_GET_LOADER_INFO           = CMX_VMX_BASE + 0x0D;
    private static final int CMD_VMX_GET_STATUS                 = CMX_VMX_BASE + 0x0E;
    private static final int CMX_VMX_TEST                         = CMX_VMX_BASE + 0x0F;
    private static final int CMX_VMX_STOP_EMM                   = CMX_VMX_BASE + 0x10;
    private static final int CMX_VMX_OSM_FINISH                  = CMX_VMX_BASE + 0x11;
    private static final int CMD_VMX_CAT_EMM_ENABLE          = CMX_VMX_BASE + 0x12;
    private static final int CMD_VMX_AUTO_OTA                   = CMX_VMX_BASE + 0x13;
    private static final int CMD_VMX_GET_BOXID                   = CMX_VMX_BASE + 0x14;
    private static final int CMD_VMX_GET_VIRTUAL_NUMBER      = CMX_VMX_BASE + 0x15;
    private static final int CMD_VMX_STOP_EWBS               = CMX_VMX_BASE + 0x16;//Scoty 20181218 add stop EWBS

    private static final int CMD_LOADERDTV_GET_JTAG         = CMD_LOADERDTV_BASE + 0x01;
    private static final int CMD_LOADERDTV_SET_JTAG           = CMD_LOADERDTV_BASE + 0x02;
    private static final int CMD_LOADERDTV_CHECK_DSMCCS_SERVICE = CMD_LOADERDTV_BASE + 0x03;
    private static final int CMD_LOADERDTV_GET_CHIPSET_INFO = CMD_LOADERDTV_BASE + 0x04;
    private static final int CMD_LOADERDTV_GET_STBSN = CMD_LOADERDTV_BASE + 0x05;
    private static final int CMD_LOADERDTV_GET_CHIPSET_ID = CMD_LOADERDTV_BASE + 0x06;
    private static final int CMD_LOADERDTV_GET_SWAREVER = CMD_LOADERDTV_BASE + 0x07;

    //PIO
    private static final int CMD_PIO_SetAntennaPower            = CMD_PIO_BASE + 0x01;
    private static final int CMD_PIO_SetBuzzer                  = CMD_PIO_BASE + 0x02;
    private static final int CMD_PIO_SetLedRed                  = CMD_PIO_BASE + 0x03;
    private static final int CMD_PIO_SetLedGreen                = CMD_PIO_BASE + 0x04;
    private static final int CMD_PIO_SetLedOrange               = CMD_PIO_BASE + 0x05;
    private static final int CMD_PIO_SetUsbPower                = CMD_PIO_BASE + 0x06;
    private static final int CMD_PIO_SetLedWhite                = CMD_PIO_BASE + 0x07;

    //Device
    private static final int CMD_DEVICE_INFO_USB_PORT           = CMD_DEVICE_INFO_Base + 0x01;
    private static final int CMD_DEVICE_INFO_WAKEUP_MODE      = CMD_DEVICE_INFO_Base + 0x02;

    //CA //eric lin 20210107 widevine cas , add by gary
    private static final int CMD_CA_WIDEVINE_CAS               = CMD_CA_Base + 0x01;
    //CA small command  //eric lin 20210107 widevine cas , add by gary
    private static final int INVOKE_CA_CMD_READ_SESSION     = 0x01;

    // JAVA CMD
// JAVA Common
    private static final int CMD_COMM_SetSurface            = CMD_JAVA_Base + CMD_Common_Base + 0x01;
    private static final int CMD_COMM_SetDisplay            = CMD_JAVA_Base + CMD_Common_Base + 0x02;
    private static final int CMD_COMM_GetWindHandle         = CMD_JAVA_Base + CMD_Common_Base + 0x03;
    private static final int CMD_COMM_ClearDisplay          = CMD_JAVA_Base + CMD_Common_Base + 0x04;
    private static final int CMD_COMM_GetTimeshiftWindHandle= CMD_JAVA_Base + CMD_Common_Base + 0x05;

    // JAVA PM
    private static final int CMD_FE_GetConnectParam         = CMD_JAVA_Base + CMD_FE_Base + 0x01;
    private static final int CMD_FE_PauseSearch             = CMD_JAVA_Base + CMD_FE_Base + 0x02;
    private static final int CMD_FE_ResumeSearch            = CMD_JAVA_Base + CMD_FE_Base + 0x03;
    private static final int CMD_FE_GetScanProgress         = CMD_JAVA_Base + CMD_FE_Base + 0x04;
    private static final int CMD_FE_GetScanInfo             = CMD_JAVA_Base + CMD_FE_Base + 0x05;

    private static final int CMD_PM_CreateChannel           = CMD_JAVA_Base + CMD_PM_Base + 0x01;
    private static final int CMD_PM_SetDefaultOpenChannel   = CMD_JAVA_Base + CMD_PM_Base + 0x02;
    private static final int CMD_PM_GetDefaultOpenChannel   = CMD_JAVA_Base + CMD_PM_Base + 0x03;
    private static final int CMD_PM_GetChannelNO            = CMD_JAVA_Base + CMD_PM_Base + 0x04;
    private static final int CMD_PM_GetChannelExternByID    = CMD_JAVA_Base + CMD_PM_Base + 0x05;
    private static final int CMD_PM_ChannelSortExtern       = CMD_JAVA_Base + CMD_PM_Base + 0x06;
    private static final int CMD_PM_DelChannelByTag         = CMD_JAVA_Base + CMD_PM_Base + 0x07;
    private static final int CMD_PM_DelChannelByNetWorkID   = CMD_JAVA_Base + CMD_PM_Base + 0x08;
    private static final int CMD_PM_DelChannelBySignalType  = CMD_JAVA_Base + CMD_PM_Base + 0x09;
    private static final int CMD_PM_DelChannelByTpID        = CMD_JAVA_Base + CMD_PM_Base + 0x0A;

    private static final int CMD_PM_GetChannelGroupName     = CMD_JAVA_Base + CMD_PM_Base + 0x10;
    private static final int CMD_PM_SetChannelGroupName     = CMD_JAVA_Base + CMD_PM_Base + 0x11;
    private static final int CMD_PM_RebuildGroup            = CMD_JAVA_Base + CMD_PM_Base + 0x12;
    private static final int CMD_PM_GetGroupFilter          = CMD_JAVA_Base + CMD_PM_Base + 0x13;
    private static final int CMD_PM_RebuildAllGroup         = CMD_JAVA_Base + CMD_PM_Base + 0x14;
    private static final int CMD_PM_GetDefaultOpenGroupType = CMD_JAVA_Base + CMD_PM_Base + 0x15;
    private static final int CMD_PM_GetChannelGroup         = CMD_JAVA_Base + CMD_PM_Base + 0x16;
    private static final int CMD_PM_GetUseGroups            = CMD_JAVA_Base + CMD_PM_Base + 0x17;
    private static final int CMD_PM_GetChannelListExtern    = CMD_JAVA_Base + CMD_PM_Base + 0x18;

    private static final int CMD_PM_ExportDBToFile          = CMD_JAVA_Base + CMD_PM_Base + 0x20;
    private static final int CMD_PM_ImportDBFromFile        = CMD_JAVA_Base + CMD_PM_Base + 0x21;
    private static final int CMD_PM_ImportDBFromIniFile     = CMD_JAVA_Base + CMD_PM_Base + 0x22;
    private static final int CMD_PM_ClearTable              = CMD_JAVA_Base + CMD_PM_Base + 0x23;
    private static final int CMD_PM_SaveTable               = CMD_JAVA_Base + CMD_PM_Base + 0x24;
    private static final int CMD_PM_RestoreTable            = CMD_JAVA_Base + CMD_PM_Base + 0x25;

    private static final int CMD_PM_GetDeliveryIDByTpID     = CMD_JAVA_Base + CMD_PM_Base + 0x30;
    private static final int CMD_PM_GetDeliveryByID         = CMD_JAVA_Base + CMD_PM_Base + 0x31;
    private static final int CMD_PM_GetDeliverSystemCount   = CMD_JAVA_Base + CMD_PM_Base + 0x32;
    private static final int CMD_PM_GetDeliverSystemList    = CMD_JAVA_Base + CMD_PM_Base + 0x33;
    private static final int CMD_PM_GetTpByID               = CMD_JAVA_Base + CMD_PM_Base + 0x34;
    private static final int CMD_PM_GetTpIDByChannelID      = CMD_JAVA_Base + CMD_PM_Base + 0x35;
    private static final int CMD_PM_SetDeliveryExtern       = CMD_JAVA_Base + CMD_PM_Base + 0x36;
    private static final int CMD_PM_SetTp                   = CMD_JAVA_Base + CMD_PM_Base + 0x37;

    private static final int CMD_CFG_RestoreDefaultConfig   = CMD_JAVA_Base + CMD_CFG_Base + 0x01;//TODO
    private static final int CMD_CFG_GetConfigFileIntValue  = CMD_JAVA_Base + CMD_CFG_Base + 0x02;//TODO

    private static final int CMD_TMCTL_GetCurrentSystemTime = CMD_JAVA_Base + CMD_TMCTL_Base + 0x01;//TODO
    private static final int CMD_TMCTL_GetSettingTOTStatus  = CMD_JAVA_Base + CMD_TMCTL_Base + 0x02;//TODO
    private static final int CMD_TMCTL_GetSettingTDTStatus  = CMD_JAVA_Base + CMD_TMCTL_Base + 0x03;//TODO
    private static final int CMD_TMCTL_SetTimeToSystem      = CMD_JAVA_Base + CMD_TMCTL_Base + 0x04;//TODO
    private static final int CMD_TMCTL_SetSettingTOTStatus  = CMD_JAVA_Base + CMD_TMCTL_Base + 0x05;//TODO
    private static final int CMD_TMCTL_SetSettingTDTStatus  = CMD_JAVA_Base + CMD_TMCTL_Base + 0x06;//TODO
    //not exist
    private static final int CMD_AV_GetTeletextList         = CMD_JAVA_Base + CMD_TTX_BASE + 0x01;
//not exist end

    // CC
    private static final int CMD_CC_GetCurrentCC            = CMD_JAVA_Base + CMD_CC_BASE + 0x01;
    private static final int CMD_CC_GetCCType               = CMD_JAVA_Base + CMD_CC_BASE + 0x02;
    private static final int CMD_CC_SelectCC                = CMD_JAVA_Base + CMD_CC_BASE + 0x03;
    private static final int CMD_CC_ShowCC                  = CMD_JAVA_Base + CMD_CC_BASE + 0x04;
    private static final int CMD_CC_IsCCVisible             = CMD_JAVA_Base + CMD_CC_BASE + 0x05;

// CMD End

    private static final int DELIVER_ID_IVALIDATE = -1;
    private static final int NOT_IMPLEMENT = -2;
    private int mLocalTunerID = 0;
    private int mLocalID = DELIVER_ID_IVALIDATE;
    private String mLocalString = null;
    private boolean bLocalSelect = false;
    private EventHandler mEventHandler;
    private ArrayList<EventMapType> mlstListenerMap = null;
    private SurfaceView mSubTeletextSurfaceView;
    private SurfaceHolder mSubSurfaceHolder;
    private Context SetSurfaceContext;//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    private ArrayList<List<SimpleChannel>> TotalChannelList = new ArrayList<List<SimpleChannel>>();
    private ArrayList<FavGroupName> AllProgramGroup = null;
    private DTVActivity.ViewUiDisplay ViewUIDisplayManager = null;
    private DTVActivity.EpgUiDisplay EpgUiDisplayManager = null;
    private DTVActivity.BookManager UIBookManager = null;
    private List<BookInfo> UIBookList = null;
    private static final int FRONTEND_STATUS_UNKNOW = 0;
    private static final int FRONTEND_STATUS_UNLOCKED = 1;
    private static final int FRONTEND_STATUS_LOCKED = 3;
    private GposInfo tmpGposInfo = null;

    private int ChannelIsExist = 0;

    public enum EnModulation
    {
        UNDEFINED(0),
        QAM4_NR(0xfe),
        QAM4(0xff),
        QAM16(1),
        QAM32(2),
        QAM64(3),
        QAM128(4),
        QAM256(5),
        QAM512(0x105),
        QAM640(0x106),
        QAM768(0x107),
        QAM896(0x108),
        QAM1024(0x109),
        QPSK(6),
        BPSK(7),
        OQPSK(0x301),
        _8VSB(0x302),
        _16VSB(0x303);

        private int mIndex = 0;

        EnModulation(int nIndex)
        {
            mIndex = nIndex;
        }

        public int getValue()
        {
            return mIndex;
        }

        public static EnModulation valueOf(int value)
        {
            EnModulation ret = UNDEFINED;

            if (value == QAM4_NR.getValue())
            {
                ret = QAM4_NR;
            }
            else if (value == QAM4.getValue())
            {
                ret = QAM4;
            }
            else if (value == QAM16.getValue())
            {
                ret = QAM16;
            }
            else if (value == QAM32.getValue())
            {
                ret = QAM32;
            }
            else if (value == QAM64.getValue())
            {
                ret = QAM64;
            }
            else if (value == QAM128.getValue())
            {
                ret = QAM128;
            }
            else if (value == QAM256.getValue())
            {
                ret = QAM256;
            }
            else if (value == QAM512.getValue())
            {
                ret = QAM512;
            }
            else if (value == QAM640.getValue())
            {
                ret = QAM640;
            }
            else if (value == QAM768.getValue())
            {
                ret = QAM768;
            }
            else if (value == QAM896.getValue())
            {
                ret = QAM896;
            }
            else if (value == QAM1024.getValue())
            {
                ret = QAM1024;
            }
            else if (value == QPSK.getValue())
            {
                ret = QPSK;
            }
            else if (value == BPSK.getValue())
            {
                ret = BPSK;
            }
            else if (value == OQPSK.getValue())
            {
                ret = OQPSK;
            }
            else if (value == _8VSB.getValue())
            {
                ret = _8VSB;
            }
            else if (value == _16VSB.getValue())
            {
                ret = _16VSB;
            }
            else
            {
                ret = UNDEFINED;
            }
            return ret;
        }
    }
    public enum EnTVRadioFilter {
        /**
         * All .<br>
         * CN:?????         */
        ALL(0),
        /**
         * TV include EnServiceType.TV,EnServiceType.MPEG_2_HD ,EnServiceType.ADVANCED_CODEC_SD,
         * EnServiceType.ADVANCED_CODEC_HD, EnServiceType.ADVANCED_CODEC_HD_FRAME_COMPATIBLE.<br>
         * CN:?????????EnServiceType.TV,EnServiceType.MPEG_2_HD ,EnServiceType.ADVANCED_CODEC_SD,
         * EnServiceType.ADVANCED_CODEC_HD, EnServiceType.ADVANCED_CODEC_HD_FRAME_COMPATIBLE.
         */
        TV(1),
        /**
         * Radio include EnServiceType.RADIO, EnServiceType.FM_RADIO,
         * EnServiceType.ADVANCED_CODEC_RADIO. <br>
         * CN:????????EnServiceType.RADIO, EnServiceType.FM_RADIO, EnServiceType.ADVANCED_CODEC_RADIO??         */
        RADIO(2);

        private int mIndex = 0;

        EnTVRadioFilter(int nIndex)
        {
            mIndex = nIndex;
        }

        public int getValue()
        {
            return mIndex;
        }

        public static EnTVRadioFilter valueOf(int ordinal)
        {
            if (ordinal == ALL.getValue())
            {
                return ALL;
            }
            else if (ordinal == TV.getValue())
            {
                return TV;
            }
            else
            {
                return RADIO;
            }
        }

        public String toString()
        {
            String str = " " + mIndex;
            return str;
        }
    }
    public enum EnTagType {
        // Lock, not support as filter CN:???? ,????????????
        LOCK,
        // Hide, when as filter,not show hide channel CN:???? ,??????????hide???????
        HIDE,
        // delete, not support as filter CN:???,????????????
        DEL,
    }

    // device\hisilicon\bigfish\hidolphin\component\dtvappfrm\java\com\hisilicon\dtv\play\EnPlayStatus.java
    public enum EnPlayStatus
    {
        STOP(0),
        LIVEPLAY(1),
        TIMESHIFTPLAY(2),
        PAUSE(3),
        IDLE(4),
        RELEASEPLAYRESOURCE(5),
        PIPPLAY(6),
        EWSPLAY(7),
        INVALID(8);

        private int mIndex = 0;

        EnPlayStatus(int nIndex)
        {
            mIndex = nIndex;
        }

        public int getValue()
        {
            return mIndex;
        }

        public static EnPlayStatus valueOf(int ordinal)
        {
            if (ordinal == STOP.getValue())
            {
                return STOP;
            }
            else if (ordinal == LIVEPLAY.getValue())
            {
                return LIVEPLAY;
            }
            else if (ordinal == TIMESHIFTPLAY.getValue())
            {
                return TIMESHIFTPLAY;
            }
            else if (ordinal == PAUSE.getValue())
            {
                return PAUSE;
            }
            else if (ordinal == IDLE.getValue())
            {
                return IDLE;
            }
            else if (ordinal == RELEASEPLAYRESOURCE.getValue())
            {
                return RELEASEPLAYRESOURCE;
            }
            else if (ordinal == PIPPLAY.getValue())
            {
                return PIPPLAY;
            }
            else if (ordinal == EWSPLAY.getValue())
            {
                return EWSPLAY;
            }
            else
            {
                return INVALID;
            }
        }
    }
    public enum EnHisAudioTrackMode
    {
        AUDIO_TRACK_STEREO(0),
        AUDIO_TRACK_DOUBLE_MONO(1),
        AUDIO_TRACK_DOUBLE_LEFT(2),
        AUDIO_TRACK_DOUBLE_RIGHT(3),
        AUDIO_TRACK_EXCHANGE(4),
        AUDIO_TRACK_ONLY_RIGHT(5),
        AUDIO_TRACK_ONLY_LEFT(6),
        AUDIO_TRACK_MUTED(7),
        AUDIO_TRACK_BUTT(8);
        private int mIndex = 0;

        EnHisAudioTrackMode(int nIndex)
        {
            mIndex = nIndex;
        }

        public int getValue()
        {
         return mIndex;
        }

        public static EnHisAudioTrackMode valueOf(int ordinal)
        {
            if (ordinal == AUDIO_TRACK_STEREO.getValue())
            {
                return AUDIO_TRACK_STEREO;
            }
            else if (ordinal == AUDIO_TRACK_DOUBLE_MONO.getValue())
            {
                return AUDIO_TRACK_DOUBLE_MONO;
            }
            else if (ordinal == AUDIO_TRACK_DOUBLE_LEFT.getValue())
            {
                return AUDIO_TRACK_DOUBLE_LEFT;
            }
            else if (ordinal == AUDIO_TRACK_DOUBLE_RIGHT.getValue())
            {
                return AUDIO_TRACK_DOUBLE_RIGHT;
            }
            else if (ordinal == AUDIO_TRACK_EXCHANGE.getValue())
            {
                return AUDIO_TRACK_EXCHANGE;
            }
            else if (ordinal == AUDIO_TRACK_ONLY_RIGHT.getValue())
            {
                return AUDIO_TRACK_ONLY_RIGHT;
            }
            else if (ordinal == AUDIO_TRACK_ONLY_LEFT.getValue())
            {
                return AUDIO_TRACK_ONLY_LEFT;
            }
            else if (ordinal == AUDIO_TRACK_MUTED.getValue())
            {
                return AUDIO_TRACK_MUTED;
            }
            else
            {
                return AUDIO_TRACK_BUTT;
            }
        }
    }
    public enum DVB_PSISI_STREAM_TYPE_E {
        DTV_PSISI_STREAM_RESERVED(0x00),
        DTV_PSISI_STREAM_VIDEO_MPEG1(0x01),
        DTV_PSISI_STREAM_VIDEO_MPEG2(0x02),
        DTV_PSISI_STREAM_AUDIO_MPEG1(0x03),
        DTV_PSISI_STREAM_AUDIO_MPEG2(0x04),
        DTV_PSISI_STREAM_PRIVATE_SECTIONS(0x05),
        DTV_PSISI_STREAM_PRIVATE_PES(0x06),
        DTV_PSISI_STREAM_MHEG(0x07),
        DTV_PSISI_STREAM_DSM_CC(0x08),
        DTV_PSISI_STREAM_TYPE_H2221(0x09),
        DTV_PSISI_STREAM_TYPE_A(0x0A),
        DTV_PSISI_STREAM_TYPE_B(0x0B),
        DTV_PSISI_STREAM_TYPE_C(0x0C),
        DTV_PSISI_STREAM_TYPE_D(0x0D),
        DTV_PSISI_STREAM_TYPE_AUX(0x0E),
        DTV_PSISI_STREAM_AUDIO_AAC_ADTS(0x0F),
        DTV_PSISI_STREAM_VIDEO_MPEG4(0x10),
        DTV_PSISI_STREAM_AUDIO_AAC_LATM(0x11),
        DTV_PSISI_STREAM_AUDIO_AAC_RAW(0x12),
        DTV_PSISI_STREAM_VIDEO_H264(0x1B),
        DTV_PSISI_STREAM_VIDEO_AVS(0x42),
        DTV_PSISI_STREAM_AUDIO_AVS(0x43),
        DTV_PSISI_STREAM_VIDEO_WM9(0xEA),
        DTV_PSISI_STREAM_AUDIO_WM9(0xE6),
        DTV_PSISI_STREAM_AUDIO_AC3(0x81),
        DTV_PSISI_STREAM_AUDIO_DTS(0x85),
        DTV_PSISI_STREAM_AUDIO_DRA(0x90);

        private int value;

        DVB_PSISI_STREAM_TYPE_E(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }
    public enum EnSubtitleType {
        SUBTITLE("SUBTITLE"),
        TELETEXT("TELETEXT"),
        CC("CC");

        private final String m_szType;
        EnSubtitleType(String szType)
        {
            this.m_szType = szType;
        }
        public String toString()
        {
            return m_szType;
        }
    }
    public enum EnUseGroupType {
        Satellite, Favorite, Common, All
    }
    public enum EnPolarity
    {
        /**
                * Horizontal polarization. <br>
                * CN：水平�??��
                */
        HORIZONTAL,
        /**
                * Vertical polarization. <br>
                * CN：�??��??��
                */
        VERTICAL,
        /**
                * Left-handed circularly polarized. <br>
                * CN：左?��??��??
                */
        LEFTHAND,
        /**
                * Right-handed circularly polarized. <br>
                * CN：右?��??��??
                */
        RIGHTHAND;

        /**
                * Get enumeration type value based on the value of the enumeration index. <br>
                * CN: ?�据?�举索�??�获?��?举类?�值�
                * @return EnPolarity.
                */
        public static EnPolarity valueOf(int ordinal)
        {
            if (ordinal < 0 || ordinal >= values().length)
            {
                throw new IndexOutOfBoundsException("EnPolarity Invalid ordinal=" + ordinal);
            }
            return values()[ordinal];
        }
    }
    public enum EnServiceType
    {
        /** 保�? Reserved */
        RESERVED(0x00),
        /** ?��??��??�务 digital television service */
        TV(0x01),
        /** ?��??�线语音?�务 digital radio sound service */
        RADIO(0x02),
        /** ?��??��?广播 Teletext service */
        TELETEXT(0x03),
        /** NVOD?�考�???NVOD reference service */
        NVOD_REFERENCE(0x04),
        /** NVOD?�移?�务 NVOD time-shifted service */
        NVOD_TIMESHIFT(0x05),
        /** 马�??��???mosaic service */
        MOSAIC(0x06),
        /** 调�?广播?�务 FM radio service */
        FM_RADIO(0x07),
        /** DVB系�??�务 DVB SRM service */
        DVB_SRM(0x08),
        /** 高级编�??��??�线语音?�务 advanced codec digital radio sound service */
        ADVANCED_CODEC_RADIO(0x0A),
        /** 高级编�?马�??��???advanced codec mosaic service */
        ADVANCED_CODEC_MOSAIC(0x0B),
        /** ?�据广播?�务 data broadcast service */
        DATABROADCAST(0x0C),
        /** RCS Map */
        RSC_MAP(0x0E),
        /** RCS FLS */
        RCS_FLS(0x0F),
        /** DVB多�?体家庭平??DVB MHP service */
        DVB_MHP(0x10),
        /** MPEG-2高�??��??��??�务 MPEG-2 HD digital television service */
        MPEG_2_HD(0x11),
        /** 高级编�??��??��??��??�务 advanced codec SD digital television service */
        ADVANCED_CODEC_SD(0x16),
        /** 高级编�??��?NVOD?�移?�务 advanced codec SD NVOD time-shifted service */
        ADVANCED_CODEC_SD_NVOD_TIMESHIFT(0x17),
        /** 高级编�??��?NVOD?�考�???advanced codec SD NVOD reference service */
        ADVANCED_CODEC_SD_NVOD_REFERENCE(0x18),
        /** 高级编�?高�??��??��??�务 advanced codec HD digital television service */
        ADVANCED_CODEC_HD(0x19),
        /** 高级编�?高�?NVOD?�移?�务 advanced codec HD NVOD time-shifted service */
        ADVANCED_CODEC_HD_NVOD_TIMESHIFT(0x1A),
        /** 高级编�?高�?NVOD?�考�???advanced codec HD NVOD reference service */
        ADVANCED_CODEC_HD_NVOD_REFERENCE(0x1B),
        /** advanced codec frame compatible plano-stereoscopic HD digital television service */
        ADVANCED_CODEC_HD_3D(0x1C),
        /** advanced codec frame compatible plano-stereoscopic HD NVOD time-shifted service */
        ADVANCED_CODEC_HD_NVOD_TIMESHIFT_3D(0x1D),
        /** advanced codec frame compatible plano-stereoscopic HD NVOD reference service */
        ADVANCED_CODEC_HD_NVOD_REFERENCE_FRAME_COMPATIBLE(0x1E),

        USER_DEFINE_TYPE(0x98),
        /** 保�?将来使用(0x09,0x12~0x15,0x1F~0x7F,0xFF) */
        RESERVED_FOR_FUTURE(0xFF);

        private int mIndex = 0;

        EnServiceType(int nIndex)
        {
            mIndex = nIndex;
        }

        public int getValue()
        {
            return mIndex;
        }

        /**
         * Get the EnServiceType of television service.<br>
         * CN：获?��?于电视�??�务类�??�表??
         * @return EnServiceType list of television service.<br>
         *         CN:属�??��??��??�类?��?表�?
         */
        public static List<EnServiceType> getTVServiceTypes()
        {
            List<EnServiceType> mTVTypes = new ArrayList<EnServiceType>();
            mTVTypes.add(TV);
            mTVTypes.add(MPEG_2_HD);
            mTVTypes.add(ADVANCED_CODEC_SD);
            mTVTypes.add(ADVANCED_CODEC_HD);
            mTVTypes.add(ADVANCED_CODEC_HD_3D);
            return mTVTypes;
        }

        /**
         * Get the EnServiceType of radio service.<br>
         * CN：获?��?于广?��??�务类�??�表??
         * @return EnServiceType list of radio service.<br>
         *         CN:属�?广播?��??�类?��?表�?
         */
        public static List<EnServiceType> getRadioServiceTypes()
        {
            List<EnServiceType> mRadioTypes = new ArrayList<EnServiceType>();
            mRadioTypes.add(RADIO);
            mRadioTypes.add(FM_RADIO);
            mRadioTypes.add(ADVANCED_CODEC_RADIO);
            return mRadioTypes;
        }

        public static EnServiceType valueOf(int ordinal)
        {
            if (ordinal == RESERVED.getValue())
            {
                return RESERVED;
            }
            else if (ordinal == TV.getValue())
            {
                return TV;
            }
            else if (ordinal == RADIO.getValue())
            {
                return RADIO;
            }
            else if (ordinal == TELETEXT.getValue())
            {
                return TELETEXT;
            }
            else if (ordinal == NVOD_REFERENCE.getValue())
            {
                return NVOD_REFERENCE;
            }
            else if (ordinal == NVOD_TIMESHIFT.getValue())
            {
                return NVOD_TIMESHIFT;
            }
            else if (ordinal == MOSAIC.getValue())
            {
                return MOSAIC;
            }
            else if (ordinal == FM_RADIO.getValue())
            {
                return FM_RADIO;
            }
            else if (ordinal == DVB_SRM.getValue())
            {
                return DVB_SRM;
            }
            else if (ordinal == ADVANCED_CODEC_RADIO.getValue())
            {
                return ADVANCED_CODEC_RADIO;
            }
            else if (ordinal == ADVANCED_CODEC_MOSAIC.getValue())
            {
                return ADVANCED_CODEC_MOSAIC;
            }
            else if (ordinal == DATABROADCAST.getValue())
            {
                return DATABROADCAST;
            }
            else if (ordinal == RSC_MAP.getValue())
            {
                return RSC_MAP;
            }
            else if (ordinal == RCS_FLS.getValue())
            {
                return RCS_FLS;
            }
            else if (ordinal == DVB_MHP.getValue())
            {
                return DVB_MHP;
            }
            else if (ordinal == MPEG_2_HD.getValue())
            {
                return MPEG_2_HD;
            }
            else if (ordinal == ADVANCED_CODEC_SD.getValue())
            {
                return ADVANCED_CODEC_SD;
            }
            else if (ordinal == ADVANCED_CODEC_SD_NVOD_REFERENCE.getValue())
            {
                return ADVANCED_CODEC_SD_NVOD_REFERENCE;
            }
            else if (ordinal == ADVANCED_CODEC_SD_NVOD_TIMESHIFT.getValue())
            {
                return ADVANCED_CODEC_SD_NVOD_TIMESHIFT;
            }
            else if (ordinal == ADVANCED_CODEC_HD.getValue())
            {
                return ADVANCED_CODEC_HD;
            }
            else if (ordinal == ADVANCED_CODEC_HD_NVOD_REFERENCE.getValue())
            {
                return ADVANCED_CODEC_HD_NVOD_REFERENCE;
            }
            else if (ordinal == ADVANCED_CODEC_HD_NVOD_TIMESHIFT.getValue())
            {
                return ADVANCED_CODEC_HD_NVOD_TIMESHIFT;
            }
            else if (ordinal == ADVANCED_CODEC_HD_3D.getValue())
            {
                return ADVANCED_CODEC_HD_3D;
            }
            else if (ordinal == ADVANCED_CODEC_HD_NVOD_TIMESHIFT_3D.getValue())
            {
                return ADVANCED_CODEC_HD_NVOD_TIMESHIFT_3D;
            }
            else if (ordinal == ADVANCED_CODEC_HD_NVOD_REFERENCE_FRAME_COMPATIBLE.getValue())
            {
                return ADVANCED_CODEC_HD_NVOD_REFERENCE_FRAME_COMPATIBLE;
            }
            else if ((ordinal >= 0x12 && ordinal <= 0x15) || (ordinal >= 0x1F && ordinal <= 0x7F)
                    || (ordinal == 0x09) || (ordinal == 0xFF))
            {
                return RESERVED_FOR_FUTURE;
            }
            else if (ordinal >= 0x80 && ordinal <= 0xFE)
            {
                return USER_DEFINE_TYPE;
            }
            else
            {
                throw new IndexOutOfBoundsException("EnServiceType Invalid ordinal=" + ordinal);
            }
        }
    }

    /*
      JNI  -s
      */
//    private static native final void _init();
//    private native final void _setup(Object dtv_this);
//    private native void _setPlugins(String path)
//            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;
//    private native final int _invoke(Parcel request, Parcel reply);
//    private native final void _setVolume(float leftVolume, float rightVolume);
//    public  native final void _disconnect();
//    private native final int _DoSideband(Surface surface, int hWin);// jim 2019/05/29 fix Android P set surface failed, using parcel to deliver Surface has some problem
//    private native final int _ClearSideband(Surface surface);// jim 2019/05/29 fix Android P set surface failed, using parcel to deliver Surface has some problem
    private int mNativeContext;  //jni use this for fieldId, so do not delete this
//    static {
//        System.loadLibrary("hidl-dtv");
//     System.loadLibrary("DtvJni");
//     System.load("/system/lib/libPrimeDtvJni.so"); //System.loadLibrary("dtvjni");
//        _init();
//    }
    //jni use this for get event, so do not change this
    private static void postEventFromNative(Object dtvplayer_ref,
                                            int what, int arg1, int arg2, int arg3, Object obj)
    {
        HiDtvMediaPlayer dp = (HiDtvMediaPlayer)((WeakReference)dtvplayer_ref).get();
        if (dp == null) {
            return;
        }

        if (dp.mEventHandler != null) {
            Message m = dp.mEventHandler.obtainMessage(what, arg1, arg2, obj);
            dp.mEventHandler.sendMessage(m);
        }

    }
    //gary20200807 fix service callback to apk data not correct-s
    public class DtvServiceCallback extends IDtvServiceCallback.Stub {
        @Override
        public void hwNotify(int i, int i1, int i2, int i3, ArrayList<Integer> arrayList) throws RemoteException {
            Log.d(TAG, "DtvServiceCallback coming !!!!!!!!!");
            if (HisDtv.mEventHandler != null) {
                int j;
                Parcel obj = Parcel.obtain();
                byte[] reply_data = new byte[arrayList.size()];
                for(j = 0; j < arrayList.size(); j++) {
//                    Log.d(TAG, "reply_arr[" + j + "] = " + String.format("0x%08X",reply_arr.get(j)));
                    reply_data[j] = (byte) arrayList.get(j).intValue();
//                    Log.d(TAG, "reply_data[" + j + "] = " + String.format("0x%08X",reply_data[j]));
                }
                obj.unmarshall(reply_data,0,arrayList.size());
                obj.setDataPosition(0);

                Message m = HisDtv.mEventHandler.obtainMessage(i, i1, i2, obj);
                HisDtv.mEventHandler.sendMessage(m);
            }
        }
    }
//gary20200807 fix service callback to apk data not correct-e
    /*
      JNI  -s
      */

    // edwin 20201214 add HwBinder.DeathRecipient -s
    OnServiceDiedListener mOnServiceDiedListener;
    public interface OnServiceDiedListener
    {
        void onServiceDied(long cookie);
    }

    final DeathRecipient recipient = new DeathRecipient();
    final class DeathRecipient implements HwBinder.DeathRecipient
    {
        @Override
        public void serviceDied(long cookie)
        {
            Log.e( TAG, "DeathRecipient cookie = " + cookie ) ;
            resetDtvService();
            prepareDTV();
            mOnServiceDiedListener.onServiceDied(cookie);
        }
    }
    // edwin 20201214 add HwBinder.DeathRecipient -e

    private void resetDtvService() // edwin 20201214 add HwBinder.DeathRecipient
    {
        Log.d(TAG, "resetDtvService: ");
        server = null ;
        if ( server == null )
        {
            try {
                server = IDtvService.getService(true);
                if(server != null)
                {
                    mCallback = new DtvServiceCallback();
                    server.setCallback(mCallback);
                    linkDeathNotify();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void linkDeathNotify() // edwin 20201214 add HwBinder.DeathRecipient
    {
        Log.d(TAG, "linkDeathNotify: server = " + server);
        if (server != null)
        {
            try {
                server.linkToDeath(recipient, 0x0 /* cookie */ );
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void unlinkDeathNotify() // edwin 20201214 add HwBinder.DeathRecipient
    {
        Log.d(TAG, "unlinkDeathNotify: server = " + server);
        if (server != null)
        {
            try {
                server.unlinkToDeath(recipient);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void setOnServiceDiedListener(OnServiceDiedListener listener)
    {
        mOnServiceDiedListener = listener;
    }

    public HiDtvMediaPlayer()
    {
        Log.e(TAG, "new HiDtvMediaPlayer");
        Looper looper;
        if ((looper = Looper.myLooper()) != null)
        {
            mEventHandler = new EventHandler(this, looper);
        }
        else if ((looper = Looper.getMainLooper()) != null)
        {
            mEventHandler = new EventHandler(this, looper);
        }
        else
        {
            mEventHandler = null;
        }

        mlstListenerMap = new ArrayList<EventMapType>();
//        _setup(new WeakReference<HiDtvMediaPlayer>(this));
//        try {
//            _setPlugins(mUri);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        if ( server == null ) {
            try {
                server = IDtvService.getService(true);
                if(server != null) {
                    mCallback = new DtvServiceCallback();
                    server.setCallback(mCallback);
                }
            } catch (RemoteException | NoSuchElementException e) {
                e.printStackTrace();
            }
        }
        prepareDTV();
        if(PLATFORM == PLATFORM_HISI)
            SetTmpGposInfo();
        //excuteCommand(CMD_PM_Clear, /*tableType.ordinal()*/2);
    }

    public ArrayList<List<SimpleChannel>> GetProgramManagerTotalChannelList()
    {
        return TotalChannelList;
    }

    public ArrayList<FavGroupName> GetAllProgramGroup()
    {
        if(AllProgramGroup == null)
            AllProgramGroup = new ArrayList<FavGroupName>();

        return AllProgramGroup;
    }

    public void SetViewUiDisplayManager(DTVActivity.ViewUiDisplay viewUiDisplay)
    {
        ViewUIDisplayManager = viewUiDisplay;
    }

    public DTVActivity.ViewUiDisplay GetViewUiDisplayManager()
    {
        return ViewUIDisplayManager;
    }

    public void SetEpgUiDisplayManager(DTVActivity.EpgUiDisplay epgUiDisplay)
    {
        EpgUiDisplayManager = epgUiDisplay;
    }

    public DTVActivity.EpgUiDisplay GetEpgUiDisplayManager(int type)
    {
        return EpgUiDisplayManager;
    }


    public static boolean hasService () // edwin 20200513 add none server function
    {
        return (server != null);
    }

    public final static HiDtvMediaPlayer getInstance(Context context)
    {
        mContext = context;
        Log.e(TAG, "exce getInstance");
        if ( HisDtv == null ) {
            Log.e(TAG, "exce getInstance new HisDtv");
            HisDtv = new HiDtvMediaPlayer();
            HisDtv.InitNetProgramDatabase();//Init and Save NetPrograms DataBase
            HisDtv.UpdateCurPlayChannelList(context,0);//Scoty 20180615 recover get simple channel list function
            HisDtv.SetUsbPort();
        }

        return HisDtv ;
    }

    public final static HiDtvMediaPlayer getInstance()
    {
        Log.e(TAG, "getInstance");
        if ( HisDtv == null ) {
            HisDtv = new HiDtvMediaPlayer();
            HisDtv.UpdateCurPlayChannelList(0);//Scoty 20180615 recover get simple channel list function
            HisDtv.SetUsbPort();
        }

        return HisDtv ;
    }

    private void SetTmpGposInfo()
    {
        tmpGposInfo = new GposInfo();

        tmpGposInfo.setDBVersion("1.1");
        tmpGposInfo.setCurChannelId(0);
        tmpGposInfo.setCurGroupType(ProgramInfo.ALL_TV_TYPE);
        tmpGposInfo.setPasswordValue(0);
        tmpGposInfo.setParentalRate(0);
        tmpGposInfo.setParentalLockOnOff(0);
        tmpGposInfo.setInstallLockOnOff(0);
        tmpGposInfo.setBoxPowerStatus(0);
        tmpGposInfo.setStartOnChannelId(0);
        tmpGposInfo.setStartOnChType(0);
        tmpGposInfo.setVolume(7);
        tmpGposInfo.setAudioTrackMode(0);
        tmpGposInfo.setAutoRegionTimeOffset(0);
        tmpGposInfo.setRegionTimeOffset((float) 0);
        tmpGposInfo.setRegionSummerTime(0);
        tmpGposInfo.setLnbPower(0);
        tmpGposInfo.setScreen16x9(0);
        tmpGposInfo.setConversion(0);
        tmpGposInfo.setResolution(0);
        tmpGposInfo.setOSDLanguage("eng");
        tmpGposInfo.setSearchProgramType(0);
        tmpGposInfo.setSearchMode(0);
        tmpGposInfo.setAudioLanguageSelection(0,"eng");
        tmpGposInfo.setAudioLanguageSelection(1,"eng");
        tmpGposInfo.setSubtitleLanguageSelection(0,"eng");
        tmpGposInfo.setSubtitleLanguageSelection(1,"eng");
        tmpGposInfo.setSortByLcn(0);
        tmpGposInfo.setOSDTransparency(0);
        tmpGposInfo.setBannerTimeout(5);
        tmpGposInfo.setHardHearing(0);
        tmpGposInfo.setAutoStandbyTime(0);
        tmpGposInfo.setDolbyMode(0);
        tmpGposInfo.setHDCPOnOff(0);
        tmpGposInfo.setDeepSleepMode(0);
        tmpGposInfo.setSubtitleOnOff(0);
    }
    /*
          ExcuteCommand -s
      */
    private int excuteCommand(int cmd_id)
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(cmd_id);
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    private int excuteCommand(int cmd_id, long arg1)
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(cmd_id);
        request.writeInt((int) arg1);

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    private int excuteCommand(int cmd_id, long arg1, int arg2)
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(cmd_id);
        request.writeInt((int) arg1);
        request.writeInt(arg2);

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    private int excuteCommand(int cmd_id, int arg1, int arg2, int arg3)
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(cmd_id);
        request.writeInt(arg1);
        request.writeInt(arg2);
        request.writeInt(arg3);

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    private int excuteCommandGetII(int cmd_id)
    {
        int ret = CMD_RETURN_VALUE_FAILAR;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(cmd_id);

        invokeex(request, reply);
        if (CMD_RETURN_VALUE_SUCCESS == reply.readInt())
        {
            ret = reply.readInt();
        }
        request.recycle();
        reply.recycle();
        return ret;
    }

    private int excuteCommandGetII(int cmd_id, long arg1)
    {
        int ret = CMD_RETURN_VALUE_FAILAR;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(cmd_id);
        request.writeInt((int) arg1);

        invokeex(request, reply);
        if (CMD_RETURN_VALUE_SUCCESS == reply.readInt())
        {
            ret = reply.readInt();
        }
        request.recycle();
        reply.recycle();
        return ret;
    }

    private int getReturnValue(int cmdRetValue) {
        int ret;

        if (CMD_RETURN_VALUE_SUCCESS == cmdRetValue) {
            ret = 0;
        }
        else if (CMD_RETURN_VALUE_FAILAR == cmdRetValue) {
            ret = -1;
        }
        else {
            ret = cmdRetValue;
        }
        return ret;
    }

    private void invokeex(Parcel request, Parcel reply)
    {
        Log.d(TAG, "invokeex");
        try {
            if(server == null) {
                Log.e( TAG, "error !! , server = null" ) ;
            }
            else {
                int i = 0 ;
//gary20200507 fix client invoke int value to service not correct-s
                request.setDataPosition(0);

                Parcel tmp = Parcel.obtain();
                tmp.appendFrom(request, request.dataPosition(), request.dataAvail());
                tmp.setDataPosition(0);
                String name = tmp.readString();
                int cmd = tmp.readInt();
                Log.d(TAG, "name = " + name);
                Log.d(TAG, "cmd = " + cmd);
                request.setDataPosition(0);
                byte[] parcel_rawdata = request.marshall() ;
//                for (i = 0; i < parcel_rawdata.length; i++) {
//                    Log.d(TAG, "parcel_rawdata[" + i + "] = " + String.format("0x%08X",parcel_rawdata[i]));
//                }
                ArrayList<Integer> arr = new ArrayList<>();
                for(i = 0; i < parcel_rawdata.length; i++) {
                    arr.add((int) parcel_rawdata[i]);
//                    Log.d(TAG, "ArrayList<Integer>[" + i + "] = " + String.format("0x%08X",arr.get(i)));
                }
//                String rawString = new String(parcel_rawdata);
//                String returnData = server.hwInvoke(rawString) ;

                ArrayList<Integer> reply_arr;
                reply_arr = server.hwInvoke(arr) ;
                byte[] reply_data = new byte[reply_arr.size()];
                for(i = 0; i < reply_arr.size(); i++) {
//                    Log.d(TAG, "reply_arr[" + i + "] = " + String.format("0x%08X",reply_arr.get(i)));
                    reply_data[i] = (byte) reply_arr.get(i).intValue();
//                    Log.d(TAG, "reply_data[" + i + "] = " + String.format("0x%08X",reply_data[i]));
                }
                reply.unmarshall(reply_data,0,reply_arr.size());
//gary20200507 fix client invoke int value to service not correct-e
                reply.setDataPosition(0);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        //Log.e(TAG, "invokeex");
        //int nRet = _invoke(request, reply);

        //if (0 != nRet && NOT_IMPLEMENT != nRet)
//        {
        //    request.recycle();
        //    reply.recycle();
        //    Log.e(TAG, "Dtvserver has crashed!");
//        throw new CInvokeRuntimeException("Dtvserver has crashed!");
//            try
//            {
//                _disconnect();
//                _setup(new WeakReference<HiDtvMediaPlayer>(this));
//                _setPlugins(mUri);
//            }
//            catch (IOException e)
//            {
//                Log.e(TAG, "invokeex exception");
//            }
//        }
    }

    /*
      ExcuteCommand -e
      */

    /*
      MessageCallback -s
      */
    private class EventHandler extends Handler {
        //private HiDtvMediaPlayer mDTV;

        public EventHandler(HiDtvMediaPlayer dp, Looper looper) {
            super(looper);
//            mDTV = dp;
        }

        public void handleMessage(Message msg)
        {
            //try
            {
                List<EventMapType> lstEventMap = getListenerMaps(msg.what);
                for (int i = 0; null != lstEventMap && i < lstEventMap.size(); i++)
                {
                    EventMapType eventMapType = lstEventMap.get(i);
                    if (null != eventMapType)
                    {
                        IDTVListener mEventListener = eventMapType.mEventListener;
                        if (null != mEventListener)
                        {
                            Object object;
                            if (null == msg.obj)
                            {
                                object = new Object();
                            }
                            else
                            {
                                object = msg.obj;
                            }
                            Log.d(TAG, "handleMessage: send notifyMessage " + msg.what);
                            mEventListener.notifyMessage(msg.what, msg.arg1, msg.arg2, object);
                        }
                    }
                }
            }
        }
    }

    private class EventMapType
    {
        int mEventType;
        int mPrivateParam;
        IDTVListener mEventListener;

        EventMapType(int EventType, IDTVListener EventListener, int PrivateParam)
        {
            mEventType = EventType;
            mEventListener = EventListener;
            mPrivateParam = PrivateParam;
        }
    }

    private EventMapType getListenerMap(int eventID, IDTVListener eventListener)
    {
        if ( null == mlstListenerMap || mlstListenerMap.size() <= 0 )
        {
            return null;
        }

        for (int i = 0; i < mlstListenerMap.size(); i++)
        {
            EventMapType eventMapType = mlstListenerMap.get(i);
            if ((eventID == eventMapType.mEventType) && (eventListener == eventMapType.mEventListener))
            {
                return eventMapType;
            }
        }

        return null;
    }

    private List<EventMapType> getListenerMaps(int eventID)
    {
        List<EventMapType> lstEventMap = null;
        if ( null == mlstListenerMap || mlstListenerMap.size() <= 0 )
        {
            return null;
        }

        for (int i = 0; i < mlstListenerMap.size(); i++)
        {
            EventMapType eventMapType = mlstListenerMap.get(i);
            if (eventID == eventMapType.mEventType)
            {
                if (null == lstEventMap)
                {
                    lstEventMap = new ArrayList<EventMapType>();
                }
                lstEventMap.add(eventMapType);
            }
        }

        return lstEventMap;
    }

    public int subScribeEvent(int eventID, IDTVListener eventListener, int privateParam)
    {
//        Log.d(TAG, "JAVA: subScribeEvent(eventID= " + eventID + ")");
        EventMapType eventMapType = getListenerMap(eventID, eventListener);
        if (null != eventMapType)
        {
            return getReturnValue(CMD_RETURN_VALUE_SUCCESS);
        }

//        Log.d(TAG, "add eventMapType to mListnerMap: (eventID= " + eventID + ")");

        mlstListenerMap.add(new EventMapType(eventID, eventListener, privateParam));
        return getReturnValue(CMD_RETURN_VALUE_SUCCESS);
    }


    public int unSubScribeEvent(int eventID, IDTVListener eventListener)
    {
//        Log.d(TAG, "UnSubscribeEvent(" + eventID + ")");

        if ( null == eventListener )
        {
            // compatible for old version
            for (int i = 0; null != mlstListenerMap && i < mlstListenerMap.size(); i++)
            {
                EventMapType eventMapType = mlstListenerMap.get(i);
                if (eventID == eventMapType.mEventType)
                {
                    mlstListenerMap.remove(eventMapType);
                    i --;
                }
            }
        }
        else
        {
            EventMapType eventMapType = getListenerMap(eventID, eventListener);
            if (null != eventMapType)
            {
                //Log.d(TAG, "unSubScribeEvent: " + mlstListenerMap.size());
                mlstListenerMap.remove(eventMapType);
                //Log.d(TAG, "unSubScribeEvent: " + mlstListenerMap.size());
//                if (eventID > DTVMessage.HI_ATV_EVT_START && eventID < DTVMessage.HI_ATV_EVT_END)
//                {
//                    atvUnSubScribeEvent(eventID);
//                }
            }
            else
            {
                Log.d(TAG, "mListnerMap == null");
            }
        }

        return getReturnValue(CMD_RETURN_VALUE_SUCCESS);

    }


    /*
      MessageCallback -e
      */

    private class DtvDate
    {
        int mYear;
        int mMonth;
        int mDay;
        int mHour;
        int mMinute;
        int mSecond;
        DtvDate(int paraYear, int paraMonth, int paraDay, int paraHour, int paraMinute, int paraSecond)
        {
            mYear = paraYear;
            mMonth = paraMonth;
            mDay = paraDay;
            mHour = paraHour;
            mMinute = paraMinute;
            mSecond = paraSecond;
        }
    }

    private DtvDate timeCalendar2DtvDate(Calendar calendar)
    {
        Log.d(TAG, "timeCalendar2DtvDate()");
        DtvDate retDtvDate = new DtvDate(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));
        return retDtvDate;
    }

    private Calendar dtvDate2Calendar(DtvDate dtvDate)
    {
        Calendar ca = null;

        int zoneSecond = 60 * this.getDtvTimeZone();
        int daylightSecond = 60 * 60 * this.getDtvDaylight();

        if (0 == zoneSecond)
        {
            ca = Calendar.getInstance();
        }
        else
        {
            ca = Calendar.getInstance(timeConvertTimeZone(zoneSecond + daylightSecond));
        }

        ca.set(dtvDate.mYear, dtvDate.mMonth - 1, dtvDate.mDay, dtvDate.mHour, dtvDate.mMinute, dtvDate.mSecond);
        return ca;
    }

    /*private DtvDate timeDate2DtvDate(Date myDate)
    {
    *//* Don't use this API again. This just for old version. *//*
        Log.e(TAG, "timeDate2DtvDate() should not be used.");
        DtvDate retDtvDate = new DtvDate(2000, 1, 1, 0, 0, 0);
        return retDtvDate;
    }

    private Date timeDtvDate2date(DtvDate dtvDate)
    {
    *//* Don't use this API again. This just for old version. *//*
        Log.e(TAG, "timeDtvDate2date() should not be used.");
        Date retDate = this.getDtvDate();
        return retDate;
    }*/

    /*
      video playback -s
      */
    public void pipModSetDisplay(Context context,Surface surface, int type ) // 1:TimeShift, 0:View //gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    {
        if ( type == 0 ) // View
        {
            Log.d(TAG, "pipModSetDisplay: View");
            //commClearDisplay(surface);
            commsetDisplay(context,surface, type);//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
        }
        else if ( type == 1 ) // TimeShift
        {
            Log.d(TAG, "pipModSetDisplay: TimeShift surface = "+surface);
            //commClearDisplay(surface);
            commsetDisplay(context,surface, type);//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
        }
    }
    // jim 2019/05/29 fix Android P set surface failed, using parcel to deliver Surface has some problem -s
    public void pipModClearDisplay(Surface surface)
    {
        commClearDisplay(surface);
    }
    private int commClearDisplay ( Surface surface )
    {
        //_ClearSideband(surface);
        return 0 ;
    }

    private int commsetDisplay(Context context,Surface surface, int type )//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    {
        int hwind = 0;
        if ( type == 0 )// Edwin 20181123 to set View's surface
        {
            hwind = commgetWindHandle();
            Log.d(TAG, "commsetDisplay: commgetWindHandle = "+hwind);
        }
        else if ( type == 1 ) // Edwin 20181123 to set TimeShift's surface
        {
            hwind = commgetTimeshiftWindHandle();
            Log.d(TAG, "commsetDisplay: commgetTimeshiftWindHandle = "+hwind);
        }
// gary modify , need porting DoSideband for set surface
//        _DoSideband( surface, hwind ) ;
//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk-s
        Intent intent = new Intent();
        intent.setAction(SET_SURFACE);
        intent.setComponent(new ComponentName("com.prime.pesisystem","com.prime.pesisystem.Receiver"));
//        intent.setPackage("com.prime.pesisystem");
        Bundle bundle = new Bundle();
//        Surface surface = mSurfaceView.getHolder().getSurface();
        bundle.putParcelable("Surface", (Parcelable) surface);
        if ( surface != null ) // jim 2020/12/16 add set surface to RTK_SerVideoSurfaceEx -s
        Log.d(TAG,"onReceive surface = "+ surface.toString());
        bundle.putInt("is_pip",type);
        intent.putExtras(bundle);
        context.sendBroadcast(intent);
//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk-e
        return 0 ;
    }
    // jim 2019/05/29 fix Android P set surface failed, using parcel to deliver Surface has some problem -e
    /*
    private int commClearDisplay ( Surface surface )
    {
        Log.d( TAG, "clear" );
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString( DTV_INTERFACE_NAME );
        request.writeInt( CMD_COMM_ClearDisplay );
        surface.writeToParcel( request, 0 );
        invokeex( request, reply );

        request.recycle();
        reply.recycle();

        return 0;
    }
*/
    private int commgetWindHandle ()
    {
        Log.d( TAG, "getWindHandle" );
        int hwind = 0;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString( DTV_INTERFACE_NAME );
        request.writeInt( CMD_COMM_GetWindHandle );
        invokeex( request, reply );

        hwind = reply.readInt();
        request.recycle();
        reply.recycle();

        return hwind;
    }

    private int commgetTimeshiftWindHandle () // Edwin 20181123 to get time shift window handle
    {
        Log.d( TAG, "getWindHandle" );
        int hwind = 0;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString( DTV_INTERFACE_NAME );
        request.writeInt( CMD_COMM_GetTimeshiftWindHandle );
        invokeex( request, reply );

        hwind = reply.readInt();
        request.recycle();
        reply.recycle();

        return hwind;
    }
/*
    private int commsetDisplay(Surface surface, int type ) // 1:TimeShift, 0:Mpeg
    {
        Log.d(TAG, "setDisplay in");
        int ret = 0;
        int hwind = 0;
        if ( type == 0 )// Edwin 20181123 to set View's surface
        {
            hwind = commgetWindHandle();
            Log.d(TAG, "commsetDisplay: commgetWindHandle = "+hwind);
        }
        else if ( type == 1 ) // Edwin 20181123 to set TimeShift's surface
        {
            hwind = commgetTimeshiftWindHandle();
            Log.d(TAG, "commsetDisplay: commgetTimeshiftWindHandle = "+hwind);
        }
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        if (null != surface)
        {
            Log.d(TAG, "setDisplay");
            request.writeInt(CMD_COMM_SetDisplay);
            request.writeInt(hwind);
            surface.writeToParcel(request, 0);
            invokeex(request, reply);

            ret = reply.readInt();
        }
        else
        {
            Log.d(TAG, "setDisplay no surface");
        }

        request.recycle();
        reply.recycle();

        return ret;
    }
*/
    private int setSurface(Surface surface)
    {

        Log.d(TAG, "setSurface in");
        int ret=0;
//          {
//          //    Log.d(TAG, "dtv no longer need  setSurface");
//              Log.d(TAG, "setSurface in");
//
//              Parcel request = Parcel.obtain();
//              Parcel reply = Parcel.obtain();
//              request.writeString(DTV_INTERFACE_NAME);
//              if (null != surface)
//                  {
//                      Log.d(TAG, "setSurface");
//                      request.writeInt(CMD_COMM_SetSurface);
//                      surface.writeToParcel(request, 0);
//                      invokeex(request, reply);
//                      ret = reply.readInt();
//                  }
//
//              request.recycle();
//              reply.recycle();
//
//              //return getReturnValue(ret);
//          }
        /*
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        if (null != surface)
        {
            Log.d(TAG, "setSurface");
            request.writeInt(CMD_SetSurface);
            surface.writeToParcel(request, 1);
            invokeex(request, reply);
            ret = reply.readInt();
            request.recycle();
            reply.recycle();
        }
        else
        {
            Log.d(TAG, "createSurface");
            request.writeInt(CMD_CreateSurface);
            invokeex(request, reply);
            ret = reply.readInt();
            request.recycle();
            reply.recycle();
        }
        */
        return getReturnValue(ret);

    }

    @SuppressWarnings("deprecation")
    private void initSurfaceView(Context context,SurfaceView surfaceView )//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    {
        Log.d(TAG, "SUB:=================initSurfaceView====================");
        //mSubTeletextSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        SetSurfaceContext = context;//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
        mSubTeletextSurfaceView = surfaceView ;
        mSubTeletextSurfaceView.setVisibility(View.VISIBLE);
        mSubSurfaceHolder = mSubTeletextSurfaceView.getHolder();

        mSubSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        //mSubSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_HISI_TRANSPARENT);
        mSubSurfaceHolder.setType(100);

        Log.d(TAG, "SUB:=================mSubSurfaceHolder = " + mSubSurfaceHolder);

        mSubSurfaceHolder.addCallback(new SurfaceHolder.Callback()
        {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
            {
                Log.d(TAG, "SUB:=================surfaceChanged====================");
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder)
            {

                Log.d(TAG, "SUB:=================surfaceCreated====================");

                mSubTeletextSurfaceView.setVisibility(View.VISIBLE);

                Log.d(TAG, "SUB:=================begin setSurface====================");
                //setSurface(holder.getSurface()) ;
                pipModSetDisplay(SetSurfaceContext,holder.getSurface(),0);//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
                mSubSurfaceHolder = holder;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder)
            {
                Log.d(TAG, "SUB:=================surfaceDestroyed====================");
//                pipModClearDisplay(holder.getSurface());
                pipModSetDisplay(SetSurfaceContext,null,0);// jim 2020/12/16 add set surface to RTK_SerVideoSurfaceEx -s
            }
        });
    }

    public void setSurfaceView(Context context, SurfaceView surfaceView )//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    {
        initSurfaceView(context,surfaceView) ;//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    }
    /*
      video playback -e
      */

    public void getDeliverSystemList()
    {
        int nPos = 0 ;
        int nNum = 100 ;
        String networkName = null ;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetDeliverSystemList);
        request.writeInt(/*networkType.getValue()*/TUNER_TYPE);
        request.writeInt(nPos);
        request.writeInt(nNum);

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int nCount = reply.readInt();

            int nID = reply.readInt();
            int ntunerID = reply.readInt();
            int nSelect = reply.readInt();
            String strName = reply.readString();

            mLocalTunerID = ntunerID;
            mLocalID = nID;
            mLocalString = strName;
            bLocalSelect = 1 == nSelect;
            //Terrestrial terrestrial = new Terrestrial(this, nID, ntunerID, strName, 1 == nSelect);
        }
        request.recycle();
        reply.recycle();
    }

    private boolean getTag(EnTagType tag, int editTag)
    {
        int valueTag = 1 << tag.ordinal();
        return ((editTag & valueTag) > 0);
    }
    private void pm_getProgramByReply(ProgramInfo program, Parcel reply){
        //Log.d(TAG, "pm_getProgramByReply");
        int i=0, tmp=0;
        String tmpString;
        int Lock = 0;
        int Skip = 0;
        int Delete = 0;
        int Move = 0;

        reply.readInt();
        //ChannelId (chNode.channelID)
        program.setChannelId(getUnsignedInt(reply.readInt()));

        //DisplayName (chNode.OrignalServiceName)
        program.setDisplayName(reply.readString());

        //TransportStreamId (chNode.TSID)
        program.setTransportStreamId(reply.readInt());

        //CA (chNode.bCAMode)
        program.setCA(reply.readInt());

        //DisplayNum (chNode.LCN)
        program.setDisplayNum(reply.readInt());

        //Lock
        program.setLock(reply.readInt());
        //Skip
        program.setSkip(reply.readInt());
        Delete = reply.readInt();//delete
        Move = reply.readInt();//move
        tmp = reply.readInt();//chNode.tempFlag = reply.readInt();
        reply.readInt();//bUsrModifyFlag
        reply.readInt();//u16HDFlag
        reply.readInt();//bPUTempFlag

        tmp = reply.readInt();//chNode.favorTag = reply.readInt();

        //OriginalNetworkId (chNode.origNetworkID)
        program.setOriginalNetworkId(reply.readInt());

        //ServiceId (chNode.serviceID)
        program.setServiceId(reply.readInt());

        //Type (chNode.serviceType)
        tmp = reply.readInt();
        if(PLATFORM == PLATFORM_HISI)
        {
            if (tmp == EnTVRadioFilter.TV.getValue())
                program.setType(ProgramInfo.ALL_TV_TYPE);
            else if (tmp == EnTVRadioFilter.RADIO.getValue())
                program.setType(ProgramInfo.ALL_RADIO_TYPE);
            else
                program.setType(ProgramInfo.ALL_TV_TYPE);
        }
        else
            program.setType(tmp);

        //pesi not use
        tmp = reply.readInt();//(chNode.HasScheduleEPG)

        //pesi not use
        tmp = reply.readInt();//(chNode.HasPFEPG)

        //pesi not use
        tmp = reply.readInt();//(chNode.AudPid)

        //pVideo (chNode.VidPid)
        program.pVideo.setPID(reply.readInt());

        //pesi not use
        tmp = reply.readInt();//(chNode.PmtPid)

        //Pcr (chNode.PcrPID)
        program.setPcr(reply.readInt());

        //pVideo (chNode.VidType)
        program.pVideo.setCodec(reply.readInt());

        //pesi not use
        tmp = reply.readInt();//(chNode.AudType)

//Log.d(TAG, "pm_getProgramByReply  program name :" + chNode.OrignalServiceName + " fav :" + chNode.favorTag + " servicetype:" + chNode.serviceType);
    }

    private void pm_getChannelNodeExternByReply(ChannelNode chNode, Parcel reply)
    {
        Log.d(TAG, "pm_getChannelNodeExternByReply");

        chNode.deliveryID = reply.readInt();
        chNode.TPID = reply.readInt();

        chNode.volTrack.Volume = reply.readInt();
        chNode.volTrack.AudioChannel = reply.readInt();
        chNode.volTrack.AudioIndex = reply.readInt();

        chNode.AudioNum = reply.readInt();

        for (int i=0; i<chNode.AudioNum; i++)
        {
            chNode.esidAudioStream[i].Type = reply.readInt();
            chNode.esidAudioStream[i].Pid = reply.readInt();
            chNode.esidAudioStream[i].AudioType = reply.readInt();
            chNode.esidAudioStream[i].TrackMode = reply.readInt();
            chNode.esidAudioStream[i].szLangCode = reply.readString();
        }

        chNode.SubtNum = reply.readInt();

        for (int i=0; i<chNode.SubtNum; i++)
        {
            chNode.esidSubtitleInfo[i].Type = reply.readInt();
            chNode.esidSubtitleInfo[i].Pid = reply.readInt();
            chNode.esidSubtitleInfo[i].szLangCode = reply.readString();
        }

        chNode.TTX_SubtNum = reply.readInt();

        for (int i=0; i<chNode.TTX_SubtNum; i++)
        {
            chNode.esidTeletext[i].Type = reply.readInt();
            chNode.esidTeletext[i].Pid = reply.readInt();
            chNode.esidTeletext[i].szLangCode = reply.readString();
        }

        Log.d(TAG, "pm_getChannelNodeExternByReply ChannelNode name :" + chNode.OrignalServiceName + " fav :" + chNode.favorTag + " servicetype:" + chNode.serviceType);
    }
    private void pm_getProgramExternByReply(ProgramInfo program, Parcel reply)
    {
        Log.d(TAG, "pm_getProgramExternByReply");
        int i=0, tmp=0;

        //SatId (chNode.deliveryID)
        program.setSatId(reply.readInt());

        //TpId (chNode.TpId)
        program.setTpId(reply.readInt());

        //pesi not use
        tmp = reply.readInt();//(chNode.volTrack.Volume)

        //AudioLRSelected (chNode.volTrack.AudioChannel)
        program.setAudioLRSelected(reply.readInt());

        //AudioSelected (chNode.volTrack.AudioIndex)
        program.setAudioSelected(reply.readInt());

        //pAudios
        tmp = reply.readInt(); //(chNode.AudioNum)
        for(i=0; i<tmp; i++){
            int Type, Pid, AudioType, TrackMode;
            String szLangCode;
            Type = reply.readInt();
            Pid = reply.readInt();
            AudioType = reply.readInt();
            TrackMode = reply.readInt();
            szLangCode = reply.readString();
            program.pAudios.add(new ProgramInfo.AudioInfo(Pid, AudioType, szLangCode, szLangCode));
        }

        //pSubtitle
        tmp = reply.readInt();   //(chNode.SubtNum)
        for(i=0; i<tmp; i++){
            int Type, Pid;
            String szLangCode;

            Type = reply.readInt();
            Pid = reply.readInt();
            szLangCode = reply.readString();
            program.pSubtitle.add(new ProgramInfo.SubtitleInfo(Pid, szLangCode, 0, 0));
        }

        //pTeletext
        tmp = reply.readInt(); //(chNode.TTX_SubtNum)
        for(i=0; i<tmp; i++){
            int Type, Pid, magazineNumber, pageNumber;
            String szLangCode;

            Type = reply.readInt();
            Pid = reply.readInt();
            szLangCode = reply.readString();
            magazineNumber = (Pid & (0x0000FF00))>>8;
            pageNumber = Pid & 0xFF;
            program.pTeletext.add(new ProgramInfo.TeletextInfo(Pid, Type, szLangCode, magazineNumber, pageNumber));
        }
        //Log.d(TAG, "pm_getChannelNodeExternByReply ChannelNode name :" + chNode.OrignalServiceName + " fav :" + chNode.favorTag + " servicetype:" + chNode.serviceType);
    }
    private void pm_getChannelNodeByReply(ChannelNode chNode, Parcel reply)
    {
        Log.d(TAG, "pm_getChannelNodeByReply");
        int Lock = 0;
        int Skip = 0;
        int Delete = 0;
        int Move = 0;

        reply.readInt();
        chNode.channelID = getUnsignedInt(reply.readInt());
        chNode.OrignalServiceName = reply.readString();
        chNode.TSID = reply.readInt();
        chNode.bCAMode = reply.readInt();
        chNode.LCN = reply.readInt();

        Lock = reply.readInt();//Lock
        Skip = reply.readInt();//skip
        Delete = reply.readInt();//delete
        Move = reply.readInt();//move
        chNode.editTag = Lock | (Skip << 1) |(Delete << 2) | (Move << 3);
        chNode.tempFlag = reply.readInt();
        reply.readInt();//bUsrModifyFlag
        reply.readInt();//u16HDFlag
        reply.readInt();//bPUTempFlag

        chNode.favorTag = reply.readInt();
        chNode.origNetworkID = reply.readInt();
        chNode.serviceID = reply.readInt();
        chNode.serviceType = reply.readInt();
        chNode.HasScheduleEPG = reply.readInt();
        chNode.HasPFEPG = reply.readInt();
        chNode.AudPid = reply.readInt();
        chNode.VidPid = reply.readInt();
        chNode.PmtPid = reply.readInt();
        chNode.PcrPID = reply.readInt();
        chNode.VidType = reply.readInt();
        chNode.AudType = reply.readInt();

        Log.d(TAG, "pm_getChannelNodeByReply  ChannelNode name :" + chNode.OrignalServiceName + " fav :" + chNode.favorTag + " servicetype:" + chNode.serviceType);

    }

    private int Conv2ServerType(int groupType)
    {
        if(PLATFORM == PLATFORM_HISI) {
            if (0 == groupType) {
                return 6;//HI_SVR_PM_CHANNEL_LIST_ALL
            }

            if (1 == groupType) {
                return 0;//HI_SVR_PM_CHANNEL_LIST_AV
            }

            if (2 == groupType) {
                return 4;//HI_SVR_PM_CHANNEL_LIST_TV
            }

            if (3 == groupType) {
                return 5;//HI_SVR_PM_CHANNEL_LIST_AUDIO
            }

            if ((groupType >= 19) && (groupType <= 35)) {
                return 1;//HI_SVR_PM_CHANNEL_LIST_FAV
            }

            if ((groupType >= 36) && (groupType <= 335)) {
                return 2;//HI_SVR_PM_CHANNEL_LIST_SAT
            }

            if ((groupType >= 336) && (groupType <= 367)) {
                return 3;//HI_SVR_PM_CHANNEL_LIST_BAT
            }

            if ((groupType >= 368) && (groupType <= 375)) {
                return 7;//HI_SVR_PM_CHANNEL_LIST_USER_PRIVATE
            }

            return 0;
        }
        else {
            return groupType;
        }
    }

    private int Conv2ServerPos(int groupType)
    {
        if ((0 == groupType) || (1 == groupType) || (2 == groupType) || (3 == groupType))
        {
            return 0;
        }

        if ((groupType >= 19) && (groupType <= 35))
        {
            return (groupType - 19);//HI_SVR_PM_CHANNEL_LIST_FAV
        }

        if ((groupType >= 36) && (groupType <= 335))
        {
            return (groupType - 36);//HI_SVR_PM_CHANNEL_LIST_SAT
        }

        if ((groupType >= 336) && (groupType <= 367))
        {
            return (groupType - 336);//HI_SVR_PM_CHANNEL_LIST_BAT
        }

        if ((groupType >= 368) && (groupType <= 375))
        {
            return (groupType - 368);//HI_SVR_PM_CHANNEL_LIST_USER_PRIVATE
        }

        return 0;
    }

    public List<ProgramInfo> getProgramList(int groupType, int pos, int num)
    {
        //Log.d(TAG, "getProgramList groupType: " + groupType);
        List<ProgramInfo> programList = new ArrayList<ProgramInfo>();
        //if (pos < 0 || num <= 0 || num > 100) //eric lin 20180209 remove get 100 channal limit
        //{
        //    return channelList;
        //}

        int i = 0;
        int ret = 0;
        int retNum = 0;

        Parcel request1 = Parcel.obtain();
        Parcel reply1 = Parcel.obtain();
        request1.writeString(DTV_INTERFACE_NAME);
        request1.writeInt(CMD_PM_GetChannelList);
        //request1.writeInt(groupType);

        request1.writeInt(Conv2ServerType(groupType));
        request1.writeInt(Conv2ServerPos(groupType));
        request1.writeInt(pos);
        request1.writeInt(num);
        //request1.writeString(mLang);

        invokeex(request1, reply1);
        ret = reply1.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            retNum = reply1.readInt();
            Log.d(TAG, "Channel Num =" + retNum);
            for (i = 0; i < retNum; i++)
            {
                ProgramInfo program = new ProgramInfo();
                pm_getProgramByReply(program, reply1);
                programList.add(program);
            }
            Log.d(TAG, "getProgramList end");
        }

        request1.recycle();
        reply1.recycle();

        Parcel request2 = Parcel.obtain();
        Parcel reply2 = Parcel.obtain();

        request2.writeString(DTV_INTERFACE_NAME);
        request2.writeInt(CMD_PM_GetChannelListExtern);
        request2.writeInt(groupType);
        request2.writeInt(pos);
        request2.writeInt(num);

        invokeex(request2, reply2);

        ret = reply2.readInt();

        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            retNum = reply2.readInt();

            for (i = 0; i < retNum; i++)
            {
                ProgramInfo program = programList.get(i);
                Log.d(TAG, "CMD_PM_GetChannelExternByID ret = " + ret);
                if (CMD_RETURN_VALUE_SUCCESS == ret)
                {
                    pm_getProgramExternByReply(program, reply2);
                }
            }
        }

        request2.recycle();
        reply2.recycle();

        return programList;
    }

    public ChannelNode getChannelByID(long ChannelID)
    {
        Log.d(TAG, "getChannelByID " + ChannelID);
        ChannelNode chNode = null;
        Parcel request1 = Parcel.obtain();
        Parcel reply1 = Parcel.obtain();
        request1.writeString(DTV_INTERFACE_NAME);
        request1.writeInt(CMD_PM_GetChannelByID);
        request1.writeInt((int) ChannelID);
        invokeex(request1, reply1);
        int ret = reply1.readInt();
        Log.d(TAG, "getChannelByID ret = " + ret);
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            chNode = new ChannelNode();
            pm_getChannelNodeByReply(chNode, reply1);
        }

        request1.recycle();
        reply1.recycle();

        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Parcel request2 = Parcel.obtain();
            Parcel reply2 = Parcel.obtain();

            request2.writeString(DTV_INTERFACE_NAME);
            request2.writeInt(CMD_PM_GetChannelExternByID);
            request2.writeInt((int) ChannelID);

            invokeex(request2, reply2);
            ret = reply2.readInt();
            Log.d(TAG, "CMD_PM_GetChannelExternByID ret = " + ret);
            if (CMD_RETURN_VALUE_SUCCESS == ret)
            {
                pm_getChannelNodeExternByReply(chNode, reply2);
            }

            request2.recycle();
            reply2.recycle();
        }

        return chNode;
    }

    public ProgramInfo getProgramByID(long ChannelID)
    {
        //Log.d(TAG, "getProgramByID " + ChannelID);
        ProgramInfo program = null;
        Parcel request1 = Parcel.obtain();
        Parcel reply1 = Parcel.obtain();
        request1.writeString(DTV_INTERFACE_NAME);
        request1.writeInt(CMD_PM_GetChannelByID);
        request1.writeInt((int) ChannelID);
        invokeex(request1, reply1);
        int ret = reply1.readInt();
        //Log.d(TAG, "getProgramByID ret = " + ret);
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            program = new ProgramInfo();
            pm_getProgramByReply(program, reply1);
        }

        request1.recycle();
        reply1.recycle();

        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Parcel request2 = Parcel.obtain();
            Parcel reply2 = Parcel.obtain();

            request2.writeString(DTV_INTERFACE_NAME);
            request2.writeInt(CMD_PM_GetChannelExternByID);
            request2.writeInt((int) ChannelID);

            invokeex(request2, reply2);
            ret = reply2.readInt();
            Log.d(TAG, "CMD_PM_GetChannelExternByID ret = " + ret);
            if (CMD_RETURN_VALUE_SUCCESS == ret)
            {
                pm_getProgramExternByReply(program, reply2);
            }

            request2.recycle();
            reply2.recycle();
        }

        return program;
    }

    private void doScan(TVScanParams sp) {
        int ret;
        if (sp.getScanMode() == TVScanParams.SCAN_MODE_AUTO)
        {
            if(sp.getTpInfo().getTunerType() == TpInfo.DVBC)
            {
                ret = dvbcAutoScan(sp);
                Log.d(TAG, "startScan dvbcAutoScan. Ret = " + ret);
            }
            else if(sp.getTpInfo().getTunerType() == TpInfo.DVBS)
            {
                ret = dvbsScanSelectedSat(sp);
                Log.d(TAG, "startScan dvbsScanSelectedSat. Ret = " + ret);
            }
            else if(sp.getTpInfo().getTunerType() == TpInfo.DVBT)
            {
                ret = dvbtAutoScan(sp);
                Log.d(TAG, "startScan dvbtAutoScan. Ret = " + ret);
            }
            else if(sp.getTpInfo().getTunerType() == TpInfo.ISDBT)
            {
                ret = isdbtAutoScan(sp);
                Log.d(TAG, "startScan isdbtAutoScan. Ret = " + ret);
            }
        }
        else if (sp.getScanMode() == TVScanParams.SCAN_MODE_MANUAL)
        {
            if(sp.getTpInfo().getTunerType() == TpInfo.DVBC)
            {
                ret = dvbcSingleFreqScan(sp);
                Log.d(TAG, "startScan dvbcSingleFreqScan. Ret = " + ret);
            }
            else if(sp.getTpInfo().getTunerType() == TpInfo.DVBS)
            {
                ret = dvbsSearchTp(sp);
                Log.d(TAG, "startScan dvbsSearchTp. Ret = " + ret);
            }
            else if(sp.getTpInfo().getTunerType() == TpInfo.DVBT)
            {
                ret = dvbtSingleFreqScan(sp);
                Log.d(TAG, "startScan dvbtSingleScan. Ret = " + ret);
            }
            else if(sp.getTpInfo().getTunerType() == TpInfo.ISDBT)
            {
                ret = isdbtSingleFreqScan(sp);
                Log.d(TAG, "startScan isdbtSingleScan. Ret = " + ret);
            }
        }
        else if (sp.getScanMode() == TVScanParams.SCAN_MODE_NETWORK)
        {
            if(sp.getTpInfo().getTunerType() == TpInfo.DVBS)
            {
                ret = dvbsNitSearch(sp);
                Log.d(TAG, "startScan dvbsNitSearch. Ret = " + ret);
            }
        }
        else if (sp.getScanMode() == TVScanParams.SCAN_MODE_ALL_SAT)
        {
            if(sp.getTpInfo().getTunerType() == TpInfo.DVBS)
            {
                ret = dvbsSearchAllSat(sp); // Johnny 20180813 modify to search all sat in all cases
                Log.d(TAG, "startScan dvbsSearchAllSat. Ret = " + ret);
            }
        }
        else
        {
            Log.d(TAG, "startScan: unknown scanmode");
        }
    }

    public void startScan(TVScanParams sp) {
        Log.d(TAG, "startScan");
        if (PLATFORM == PLATFORM_HISI) {
            getDeliverSystemList();
            doScan(sp);
        }
        else {
            doScan(sp);
        }
    }


    public int feStopSearch(boolean bSync)
    {
        Log.d(TAG, "feStopSearch:syncFlag(" + bSync + ")");
        if(PLATFORM == PLATFORM_HISI)
            return excuteCommand(CMD_FE_AbortScan, bSync ? 1 : 0);
        else
            return excuteCommand(CMD_CS_StopSearch, bSync ? 1 : 0);
    }

    public void stopScan(boolean store) {
        int stopSearchRet = feStopSearch(store);
        int pmSaveRet = -1;
        if(store == true) {
            //pmSaveRet = excuteCommand(CMD_PM_Save, /*EnTableType.ALL*/2); // 2 = programtable

            //below copy from
            //function: saveScanResult()
            //file: /device/hisilicon/bigfish/hidolphin/component/dtvappfrm/HiDTVPlayer/src/com/hisilicon/dtvui/installtion/ScanProgressActivity.java
            if(PLATFORM == PLATFORM_HISI) {
                rebuildAllGroup();
                delChannelByTag(EnTagType.DEL.ordinal());
                Log.d(TAG, "saveNetworks(): result:"+saveNetworks());

                //pesi test
                List<Integer> allGroupTypes = getUseGroups(EnUseGroupType.All);
                if(allGroupTypes!=null && !allGroupTypes.isEmpty()) {
                    for(int i=0; i<allGroupTypes.size(); i++) {
                        Log.d(TAG, "allGroupTypes: i=" + i + ", value=" + allGroupTypes.get(i));
                    }
                }
            }
        }
        else {
            if(PLATFORM == PLATFORM_HISI) {
                restoreTable(EnTableType.ALL); //rebuildAllGroup();
            }
        }
        Log.d(TAG, "stopScan: stopSearchRet = " + stopSearchRet + " pmSaveRet = " + pmSaveRet);
    }
    
    private int dvbcSingleFreqScanForPesi(TVScanParams sp) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);

        if(sp.getNitSearch() == 1)  // enable nitSearch
        {
            request.writeInt(CMD_CS_NITSearch);
        }
        else
        {
            Log.d(TAG, "dvbcSingleFreqScan: CMD_CS_SingleFreqSearch "+ CMD_CS_SingleFreqSearch);
            Log.d(TAG, "sp.getSatId() : "+ sp.getSatId() + " sp.getTunerId() : " + sp.getTunerId()
            + " EnNetworkType.CABLE.getValue() : "+ EnNetworkType.CABLE.getValue());
            Log.d(TAG, /*tp.getID()*/"sp.getTpInfo().getTpId() : "+sp.getTpInfo().getTpId()
                        +" sp.getTpInfo().CableTp.getSymbol() : "+sp.getTpInfo().CableTp.getSymbol()
                    +" sp.getTpInfo().CableTp.getQam() : "+getEnModFromPesiQam(sp.getTpInfo().CableTp.getQam()));
            request.writeInt(CMD_CS_SingleFreqSearch);
        }

        // scan params
        request.writeInt(/*cabObj.getID()*/sp.getSatId());
        request.writeInt(/*cabObj.getTunerID()*/sp.getTunerId());
        request.writeInt(/*cabObj.getNetworkType().getValue()*/EnNetworkType.CABLE.getValue());
        request.writeInt(/*type.getFTAFilter().ordinal()*/sp.getSearchOptionCaFta());
        request.writeInt(/*type.getTVRadioFilter().ordinal()*/sp.getSearchOptionTVRadio());

        Locale locale = Locale.getDefault();
        Log.e(TAG, "languange :" + locale.getISO3Language());
        String systemLanguage = locale.getISO3Language();
        request.writeString(systemLanguage);

        // scan tp
        request.writeInt(/*tp.getID()*/sp.getTpInfo().getTpId());
        request.writeInt(/*tp.getFrequency()*/sp.getTpInfo().CableTp.getFreq());
        request.writeInt(/*tp.getSymbolRate()*/sp.getTpInfo().CableTp.getSymbol());
        request.writeInt(/*tp.getModulation().getValue()*/getEnModFromPesiQam(sp.getTpInfo().CableTp.getQam()));
        request.writeInt(/*tp.getVersion().ordinal()*/0);

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    private int dvbcSingleFreqScanForHisi(TVScanParams sp) {
        TpInfo tpInfo = sp.getTpInfo();
        int ret;

        if (null == tpInfo)
        {
            Log.e(TAG, "error parameters,the scan tp param errror,tp=" + tpInfo);
            return -2;
        }

        Log.d(TAG, "DvbcSingleFreqScan(:ScanType(" + tpInfo.getTunerType());

        int antennaType = /*cabObj.getNetworkType().getValue()*/EnNetworkType.CABLE.getValue();
        excuteCommand(CMD_FE_SetAntennaType, antennaType);

        tunerLock(TVTunerParams.CreateTunerParamDVBC(
                sp.getTunerId(),
                sp.getSatId(),
                sp.getTpId(),
                tpInfo.CableTp.getFreq(),
                tpInfo.CableTp.getSymbol(),
                tpInfo.CableTp.getQam())
        );

        if(sp.getNitSearch() == 1)  // enable nitSearch
        {
            ret = excuteCommand(CMD_FE_NitScan);
        }
        else
        {
            ret = excuteCommand(CMD_FE_ManualScan);
        }

        return getReturnValue(ret);
    }

    private int dvbcSingleFreqScan(TVScanParams sp)
    {
        int ret;
        if(PLATFORM == PLATFORM_HISI)
            ret = dvbcSingleFreqScanForHisi(sp);
        else
            ret = dvbcSingleFreqScanForPesi(sp);
        return getReturnValue(ret);
    }

    private int dvbcAutoScanForPesi(TVScanParams sp)
    {

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);

        if(sp.getNitSearch() == 1)  // enable nitSearch
        {
            request.writeInt(CMD_CS_NITSearch);
        }
        else
        {
            request.writeInt(CMD_CS_AutoSearch);
        }

        // scan params
        request.writeInt(/*cabObj.getID()*/sp.getSatId());
        request.writeInt(/*cabObj.getTunerID()*/sp.getTunerId());
        request.writeInt(/*cabObj.getNetworkType().getValue()*/EnNetworkType.CABLE.getValue());
        request.writeInt(/*type.getFTAFilter().ordinal()*/sp.getSearchOptionCaFta());
        request.writeInt(/*type.getTVRadioFilter().ordinal()*/sp.getSearchOptionTVRadio());

        Locale locale = Locale.getDefault();
        Log.e(TAG, "languange :" + locale.getISO3Language());
        String systemLanguage = locale.getISO3Language();
        request.writeString(systemLanguage);

        // scan tp
        request.writeInt(/*tp.getID()*/sp.getTpInfo().getTpId());
        request.writeInt(/*tp.getFrequency()*/sp.getTpInfo().CableTp.getFreq());
        request.writeInt(/*tp.getSymbolRate()*/sp.getTpInfo().CableTp.getSymbol());
        request.writeInt(/*tp.getModulation().getValue()*/getEnModFromPesiQam(sp.getTpInfo().CableTp.getQam()));
        request.writeInt(/*tp.getVersion().ordinal()*/0);

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    private int dvbcAutoScanForHisi(TVScanParams sp)
    {
        int antennaType = (/*networkType.getValue()*/EnNetworkType.CABLE.getValue());
        excuteCommand(CMD_FE_SetAntennaType, antennaType);

        return excuteCommand(CMD_FE_AutoScan);
    }

    private int dvbcAutoScan(TVScanParams sp) {
        int ret;
        if(PLATFORM == PLATFORM_HISI)
            ret = dvbcAutoScanForHisi(sp);
        else
            ret = dvbcAutoScanForPesi(sp);
        return getReturnValue(ret);
    }

    private int isdbtSingleFreqScanForPesi(TVScanParams sp)
    {
        /*List<Multiplex> lstTPs = terObj.getScanMultiplexes();

        if ((null == lstTPs) || (1 != lstTPs.size()))
        {
            Log.e(TAG, "error parameters,the scan tiplist param errror,lstTPs=" + lstTPs);
            return -2;
        }*/

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);

        // johnny modify 2080418 for nit search
        // hisi has no nit search in isdbt
        if(/*type.isEnableNit()*/sp.getNitSearch() == 1)
        {
            request.writeInt(CMD_CS_NITSearch);
        }
        else
        {
            request.writeInt(CMD_CS_SingleFreqSearch);
        }

        request.writeInt(/*terObj.getID()*/sp.getSatId());
        request.writeInt(/*terObj.getTunerID()*/sp.getTunerId());
        request.writeInt(/*terObj.getNetworkType().getValue()*/EnNetworkType.ISDB_TER.getValue());
        request.writeInt(/*type.getFTAFilter().ordinal()*/sp.getSearchOptionCaFta());
        request.writeInt(/*type.getTVRadioFilter().ordinal()*/sp.getSearchOptionTVRadio());

        Locale locale = Locale.getDefault();
        Log.e(TAG, "languange :" + locale.getISO3Language());
        String systemLanguage = locale.getISO3Language();
        request.writeString(systemLanguage);

        request.writeInt(/*type.getTransmissionTypeFilter().getValue()*/sp.getOneSegment());
        request.writeInt(/*tp.getID()*/sp.getTpInfo().getTpId());
        request.writeInt(/*tp.getFrequency()*/sp.getTpInfo().TerrTp.getFreq());
        request.writeInt(/*tp.getBandWidth()*/getBandForWrite(sp.getTpInfo().TerrTp.getBand()));
        request.writeInt(/*tp.getModulation().getValue()*/0);   // ignore
        request.writeInt(/*tp.getVersion().ordinal()*/0);
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    private int isdbtSingleFreqScanForHisi(TVScanParams sp)
    {
        int ret = CMD_RETURN_VALUE_FAILAR;
        /*List<Multiplex> lstTPs = terObj.getScanMultiplexes();
        int ret = CMD_RETURN_VALUE_FAILAR;

        if ((null == lstTPs) || (1 != lstTPs.size()))
        {
            Log.e(TAG, "error parameters,the scan tiplist param errror,lstTPs=" + lstTPs);
            return -2;
        }*/

        int antennaType = EnNetworkType.ISDB_TER.getValue();
        excuteCommand(CMD_FE_SetAntennaType, antennaType);

        tunerLock(TVTunerParams.CreateTunerParamISDBT(
                sp.getTunerId(),
                sp.getSatId(),
                sp.getTpId(),
                sp.getTpInfo().TerrTp.getFreq(),
                sp.getTpInfo().TerrTp.getBand())
        );

        ret = excuteCommand(CMD_FE_ManualScan);

        return getReturnValue(ret);
    }

    private int isdbtSingleFreqScan(TVScanParams sp) {
        int ret;
        if(PLATFORM == PLATFORM_HISI)
            ret = isdbtSingleFreqScanForHisi(sp);
        else
            ret = isdbtSingleFreqScanForPesi(sp);
        return getReturnValue(ret);
    }

    private int isdbtAutoScanForPesi(TVScanParams sp)
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);

        request.writeInt(CMD_CS_AutoSearch);

        request.writeInt(/*terObj.getID()*/sp.getSatId());
        request.writeInt(/*terObj.getTunerID()*/sp.getTunerId());
        request.writeInt(/*terObj.getNetworkType().getValue()*/EnNetworkType.ISDB_TER.getValue());
        request.writeInt(/*type.getFTAFilter().ordinal()*/sp.getSearchOptionCaFta());
        request.writeInt(/*type.getTVRadioFilter().ordinal()*/sp.getSearchOptionTVRadio());

        Locale locale = Locale.getDefault();
        Log.e(TAG, "languange :" + locale.getISO3Language());
        String systemLanguage = locale.getISO3Language();
        request.writeString(systemLanguage);

        request.writeInt(/*type.getTransmissionTypeFilter().getValue()*/sp.getOneSegment());
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    private int isdbtAutoScanForHisi(TVScanParams sp)
    {
        int antennaType = /*terObj.getNetworkType().getValue()*/EnNetworkType.ISDB_TER.getValue();
        excuteCommand(CMD_FE_SetAntennaType, antennaType);

        return excuteCommand(CMD_FE_AutoScan);
    }

    private int isdbtAutoScan(TVScanParams sp) {
        int ret;
        if(PLATFORM == PLATFORM_HISI)
            ret = isdbtAutoScanForHisi(sp);
        else
            ret = isdbtAutoScanForPesi(sp);
        return getReturnValue(ret);
    }

    private int dvbtSingleFreqScanForPesi(TVScanParams sp)
    {
        /*List<Multiplex> lstTPs = terObj.getScanMultiplexes();

        if ((null == lstTPs) || (1 != lstTPs.size()))
        {
            Log.e(TAG, "error parameters,the scan tiplist param errror,lstTPs=" + lstTPs);
            return -2;
        }*/

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);

        if(/*type.isEnableNit()*/sp.getNitSearch() == 1)
        {
            request.writeInt(CMD_CS_NITSearch);
            //input freq flag
            //request.writeInt(1);
        }
        else
        {
            request.writeInt(CMD_CS_SingleFreqSearch);
        }

        request.writeInt(/*terObj.getID()*/sp.getSatId());
        request.writeInt(/*terObj.getTunerID()*/sp.getTunerId());
        request.writeInt(/*terObj.getNetworkType().getValue()*/EnNetworkType.TERRESTRIAL.getValue());
        request.writeInt(/*type.getFTAFilter().ordinal()*/sp.getSearchOptionCaFta());
        request.writeInt(/*type.getTVRadioFilter().ordinal()*/sp.getSearchOptionTVRadio());

        Locale locale = Locale.getDefault();
        Log.e(TAG, "languange :" + locale.getISO3Language());
        String systemLanguage = locale.getISO3Language();
        request.writeString(systemLanguage);

        request.writeInt(/*tp.getID()*/sp.getTpInfo().getTpId());
        request.writeInt(/*tp.getFrequency()*/sp.getTpInfo().TerrTp.getFreq());
        request.writeInt(/*tp.getBandWidth()*/getBandForWrite(sp.getTpInfo().TerrTp.getBand()));
        request.writeInt(/*tp.getModulation().getValue()*/0);   // ignore
        request.writeInt(/*tp.getVersion().ordinal()*/0);

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    private int dvbtSingleFreqScanForHisi(TVScanParams sp)
    {
//        List<Multiplex> lstTPs = terObj.getScanMultiplexes();
        int ret = CMD_RETURN_VALUE_FAILAR;

        /*if ((null == lstTPs) || (1 != lstTPs.size()))
        {
            Log.e(TAG, "error parameters,the scan tiplist param errror,lstTPs=" + lstTPs);
            return -2;
        }*/

        int antennaType = /*terObj.getNetworkType().getValue()*/EnNetworkType.TERRESTRIAL.getValue();
        excuteCommand(CMD_FE_SetAntennaType, antennaType);

        tunerLock(TVTunerParams.CreateTunerParamDVBT(
                sp.getTunerId(),
                sp.getSatId(),
                sp.getTpId(),
                sp.getTpInfo().TerrTp.getFreq(),
                sp.getTpInfo().TerrTp.getBand())
        );

        if(/*type.isEnableNit()*/sp.getNitSearch() == 1)
        {
            ret = excuteCommand(CMD_FE_NitScan);
        }
        else
        {
            ret = excuteCommand(CMD_FE_ManualScan);
        }

        return getReturnValue(ret);
    }

    private int dvbtSingleFreqScan(TVScanParams sp) {
        int ret;
        if(PLATFORM == PLATFORM_HISI)
            ret = dvbtSingleFreqScanForHisi(sp);
        else
            ret = dvbtSingleFreqScanForPesi(sp);
        return getReturnValue(ret);
    }

    private int dvbtAutoScanForPesi(TVScanParams sp)
    {
        /*List<Multiplex> lstTPs = terObj.getScanMultiplexes();
        int inputFlag = 0;
        if ((null != lstTPs) && (0 < lstTPs.size()))
        {
            inputFlag = 1;
        }*/

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);

        if(/*type.isEnableNit()*/sp.getNitSearch() == 1)
        {
            request.writeInt(CMD_CS_NITSearch);
            //input freq flag
            //request.writeInt(inputFlag);
        }
        else
        {
            request.writeInt(CMD_CS_AutoSearch);
        }
        request.writeInt(/*terObj.getID()*/sp.getSatId());
        request.writeInt(/*terObj.getTunerID()*/sp.getTunerId());
        request.writeInt(/*terObj.getNetworkType().getValue()*/EnNetworkType.TERRESTRIAL.getValue());
        request.writeInt(/*type.getFTAFilter().ordinal()*/sp.getSearchOptionCaFta());
        request.writeInt(/*type.getTVRadioFilter().ordinal()*/sp.getSearchOptionTVRadio());

        Locale locale = Locale.getDefault();
        Log.e(TAG, "languange :" + locale.getISO3Language());
        String systemLanguage = locale.getISO3Language();
        request.writeString(systemLanguage);

//        if (1 == inputFlag)
//        {
        request.writeInt(/*tp.getID()*/sp.getTpInfo().getTpId());
        request.writeInt(/*tp.getFrequency()*/sp.getTpInfo().TerrTp.getFreq());
        request.writeInt(/*tp.getBandWidth()*/getBandForWrite(sp.getTpInfo().TerrTp.getBand()));
        request.writeInt(/*tp.getModulation().getValue()*/0);   // ignore
        request.writeInt(/*tp.getVersion().ordinal()*/0);
//        }


        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    private int dvbtAutoScanForHisi(/*Terrestrial terObj, ScanType type*/TVScanParams sp)
    {
        int antennaType = /*terObj.getNetworkType().getValue()*/EnNetworkType.TERRESTRIAL.getValue();
        excuteCommand(CMD_FE_SetAntennaType, antennaType);

        return excuteCommand(CMD_FE_AutoScan);
    }

    private int dvbtAutoScan(TVScanParams sp) {
        int ret;
        if(PLATFORM == PLATFORM_HISI)
            ret = dvbtAutoScanForHisi(sp);
        else
            ret = dvbtAutoScanForPesi(sp);
        return getReturnValue(ret);
    }

    // Hisi A8 feSingleSatBlindScan()
    private int singleSatBlindScanForHisi(TVScanParams sp)
    {
        Log.d(TAG, "singleSatBlindScanForHisi(" + sp.getSatId() + ")");

        int antennaType = /*satellite.getNetworkType().getValue()*/EnNetworkType.SATELLITE.getValue();
        excuteCommand(CMD_FE_SetAntennaType, antennaType);

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_FE_SatelliteBlindScan);
        request.writeInt(/*satellite.getTunerID()*/sp.getTunerId());
        request.writeInt(/*satellite.getID()*/sp.getSatId());
        int nIsNit = /*(type.isEnableNit()) ? 1 : 0*/sp.getNitSearch();
        request.writeInt(nIsNit);
        request.writeInt(/*type.getFTAFilter().ordinal()*/sp.getSearchOptionCaFta());
        request.writeInt(/*type.getTVRadioFilter().ordinal()*/sp.getSearchOptionTVRadio());

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        Log.d(TAG, "singleSatBlindScanForHisi = " + ret);
        return getReturnValue(ret);
    }

    // Pesi uses CMD_CS_AutoSearch to do dvbs single Sat search
    private int singleSatelliteScanForPesi(TVScanParams sp)
    {
        Log.d(TAG, "singleSatelliteScan(" + sp.getSatId() + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(/*CMD_CS_SingleSatBlindSearch*/CMD_CS_AutoSearch);    // Johnny 20180813 modify to use CMD_CS_AutoSearch in singleSatelliteScan

        // scan params
        request.writeInt(/*cabObj.getID()*/sp.getSatId());
        request.writeInt(/*cabObj.getTunerID()*/sp.getTunerId());
        request.writeInt(/*cabObj.getNetworkType().getValue()*/EnNetworkType.SATELLITE.getValue());
        request.writeInt(/*type.getFTAFilter().ordinal()*/sp.getSearchOptionCaFta());
        request.writeInt(/*type.getTVRadioFilter().ordinal()*/sp.getSearchOptionTVRadio());

        Locale locale = Locale.getDefault();
        Log.e(TAG, "languange :" + locale.getISO3Language());
        String systemLanguage = locale.getISO3Language();
        request.writeString(systemLanguage);

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        Log.d(TAG, "singleSatelliteScan: " + ret);
        return getReturnValue(ret);
    }

    private int dvbsScanSelectedSat(TVScanParams sp) {
        int ret;
        if(PLATFORM == PLATFORM_HISI)
            ret = singleSatBlindScanForHisi(sp);
        else
            ret = singleSatelliteScanForPesi(sp);
        return getReturnValue(ret);
    }

    // Hisi A8 feSatelliteTPScan()
    private int satelliteTPScanForHisi(TVScanParams sp)
    {
        /*List<Multiplex> lstTPs = satellite.getScanMultiplexes();
        if ((null == lstTPs) || (0 == lstTPs.size()))
        {
            Log.e(TAG, "error parameters,the scan tiplist param errror,lstTPs=" + lstTPs);
            return -2;
        }*/

        int antennaType = /*satellite.getNetworkType().getValue()*/EnNetworkType.SATELLITE.getValue();
        excuteCommand(CMD_FE_SetAntennaType, antennaType);

        excuteCommand(CMD_FE_SetSat, /*satellite.getID()*/sp.getSatId());

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_FE_SatelliteManualScan);

        request.writeInt(/*satellite.getTunerID()*/sp.getTunerId());
        request.writeInt(/*satellite.getID()*/sp.getSatId());
        int nIsNit = /*(type.isEnableNit())? 1 : 0*/sp.getNitSearch();
        request.writeInt(nIsNit); // bNetworkFlag

        request.writeInt(/*type.getFTAFilter().ordinal()*/sp.getSearchOptionCaFta());
        request.writeInt(/*type.getTVRadioFilter().ordinal()*/sp.getSearchOptionTVRadio());

        request.writeInt(/*satellite.getNetworkType().getValue()*/EnNetworkType.SATELLITE.getValue());

        int pesiTpSize = 1; // only scan 1 tp in pesi
        request.writeInt(/*lstTPs.size()*/pesiTpSize);
        for (int i = 0; i < pesiTpSize; i++)
        {
            request.writeInt(/*tp.getID()*/sp.getTpId());
            request.writeInt(/*satellite.getNetworkType().getValue()*/sp.getTpInfo().getTunerType());
            request.writeInt(/*tp.getFrequency()*/sp.getTpInfo().SatTp.getFreq());
            request.writeInt(/*tp.getSymbolRate()*/sp.getTpInfo().SatTp.getSymbol());
            request.writeInt(/*tp.getPolarity().ordinal()*/sp.getTpInfo().SatTp.getPolar());
            request.writeInt(0);
            Log.i(TAG, "=== f=" + sp.getTpInfo().SatTp.getFreq() + ",r=" + sp.getTpInfo().SatTp.getSymbol() + ",p"
                    + sp.getTpInfo().SatTp.getPolar());
        }
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    // Pesi uses CMD_CS_SingleFreqSearch to do dvbs single Tp search
    private int satelliteTPScanForPesi(TVScanParams sp)
    {
        Log.d(TAG, "satelliteTPScanForPesi: ");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(/*CMD_CS_MultiTransponderSearch*/CMD_CS_SingleFreqSearch);  // Johnny 20180813 modify to use CMD_CS_SingleFreqSearch in satelliteTPScan

        // scan params
        request.writeInt(/*cabObj.getID()*/sp.getSatId());
        request.writeInt(/*cabObj.getTunerID()*/sp.getTunerId());
        request.writeInt(/*cabObj.getNetworkType().getValue()*/EnNetworkType.SATELLITE.getValue());
        request.writeInt(/*type.getFTAFilter().ordinal()*/sp.getSearchOptionCaFta());
        request.writeInt(/*type.getTVRadioFilter().ordinal()*/sp.getSearchOptionTVRadio());

        Locale locale = Locale.getDefault();
        Log.e(TAG, "languange :" + locale.getISO3Language());
        String systemLanguage = locale.getISO3Language();
        request.writeString(systemLanguage);

        // scan tp
        request.writeInt(/*tp.getID()*/sp.getTpInfo().getTpId());
        request.writeInt(/*tp.getFrequency()*/sp.getTpInfo().SatTp.getFreq());
        request.writeInt(/*tp.getSymbolRate()*/sp.getTpInfo().SatTp.getSymbol());
        request.writeInt(/*tp.getModulation().getValue()*/sp.getTpInfo().SatTp.getPolar()); // Johnny 20180813 modify DVBS Qam to Polar
        request.writeInt(/*tp.getVersion().ordinal()*/0);

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        Log.d(TAG, "satelliteTPScanForPesi: ret = " + ret);
        return getReturnValue(ret);
    }

    private int dvbsSearchTp(TVScanParams sp) {
        int ret;
        if(PLATFORM == PLATFORM_HISI)
            ret = satelliteTPScanForHisi(sp);
        else
            ret = satelliteTPScanForPesi(sp);
        return getReturnValue(ret);
    }

    private int AllSatScanForHisi(TVScanParams sp) {
        int ret = -1;
        Log.d(TAG, "AllSatScanForHisi not support !!!!!!!!!!");
        return getReturnValue(ret);
    }

    private int AllSatScanForPesi(TVScanParams sp) {
        Log.d(TAG, "dvbsSearchAllSat(" + sp.getSatId() + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_CS_AllSatSearch);

        request.writeInt(/*satellite.getID()*/sp.getSatId());
        request.writeInt(/*satellite.getTunerID()*/sp.getTunerId());

        request.writeInt(/*satellite.getNetworkType().getValue()*/EnNetworkType.SATELLITE.getValue());
        request.writeInt(/*type.getFTAFilter().ordinal()*/sp.getSearchOptionCaFta());
        request.writeInt(/*type.getTVRadioFilter().ordinal()*/sp.getSearchOptionTVRadio());

        Locale locale = Locale.getDefault();
        Log.e(TAG, "languange :" + locale.getISO3Language());
        String systemLanguage = locale.getISO3Language();
        request.writeString(systemLanguage);

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        Log.d(TAG, "dvbsSearchAllSat: " + ret);
        return getReturnValue(ret);
    }

    private int dvbsSearchAllSat(TVScanParams sp) {
        int ret;
        if(PLATFORM == PLATFORM_HISI)
            ret = AllSatScanForHisi(sp);
        else
            ret = AllSatScanForPesi(sp);
        return getReturnValue(ret);
    }

    private int dvbsNitSearchForHisi(TVScanParams sp) {
        int ret = -1;
        Log.d(TAG, "dvbsNitSearchForHisi not support !!!!!!!!!!");
        return getReturnValue(ret);
    }

    private int dvbsNitSearchForPesi(TVScanParams sp) {
        Log.d(TAG, "dvbsNitSearchForPesi(" + sp.getSatId() + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_CS_NITSearch);

        // scan params
        request.writeInt(/*cabObj.getID()*/sp.getSatId());
        request.writeInt(/*cabObj.getTunerID()*/sp.getTunerId());
        request.writeInt(/*cabObj.getNetworkType().getValue()*/EnNetworkType.SATELLITE.getValue());
        request.writeInt(/*type.getFTAFilter().ordinal()*/sp.getSearchOptionCaFta());
        request.writeInt(/*type.getTVRadioFilter().ordinal()*/sp.getSearchOptionTVRadio());

        Locale locale = Locale.getDefault();
        Log.e(TAG, "languange :" + locale.getISO3Language());
        String systemLanguage = locale.getISO3Language();
        request.writeString(systemLanguage);

        // scan tp
        request.writeInt(/*tp.getID()*/sp.getTpInfo().getTpId());
        request.writeInt(/*tp.getFrequency()*/sp.getTpInfo().SatTp.getFreq());
        request.writeInt(/*tp.getSymbolRate()*/sp.getTpInfo().SatTp.getSymbol());
        request.writeInt(/*tp.getModulation().getValue()*/sp.getTpInfo().SatTp.getPolar()); // Johnny 20180813 modify DVBS Qam to Polar
        request.writeInt(/*tp.getVersion().ordinal()*/0);

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        Log.d(TAG, "dvbsNitSearchForPesi: " + ret);
        return getReturnValue(ret);
    }

    private int dvbsNitSearch(TVScanParams sp) {
        int ret;
        if(PLATFORM == PLATFORM_HISI)
            ret = dvbsNitSearchForHisi(sp);
        else
            ret = dvbsNitSearchForPesi(sp);
        return getReturnValue(ret);
    }

    public List<SatInfo> SatInfoGetList(int tunerType,int pos,int num)
    {
        Log.d(TAG, "SatInfoGetList: ");
        return getDeliverSystemList(tunerType, pos, num);
    }

    public SatInfo SatInfoGet(int satId)
    {
        Log.d(TAG, "SatInfoGet: ");
        SatInfo Sat = getDeliverSystemByID(satId);
        if(PLATFORM == PLATFORM_HISI) {
            if (Sat != null) {
                List<TpInfo> TpList = TpInfoGetListBySatId(-1, Sat.getSatId(), -1, -1);  // Johnny modify 20180205

                if (TpList == null)   // johnny add 20171225
                {
                    TpList = new ArrayList<>();
                }

                List<Integer> TpIdList = new ArrayList<>();
                for (int j = 0; j < TpList.size(); j++) {
                    TpIdList.add(TpList.get(j).getTpId());
                }

                Sat.setTpNum(TpIdList.size());  // johnny add 20180205
                Sat.setTps(TpIdList);
            }
        }
        return Sat;
    }

    public int SatInfoAdd(SatInfo pSat)
    {
        Log.d(TAG, "SatInfoAdd: ");
        return addSatellite(pSat);
    }

    public int SatInfoUpdate(SatInfo pSat)
    {
        Log.d(TAG, "SatInfoUpdate: ");
        return editSatellite(pSat, false);   // change in the future (Sat)
    }

    public int SatInfoUpdateList(List<SatInfo> pSats)
    {
        Log.d(TAG, "SatInfoUpdateList: ");
        return updateSatList(pSats);
    }

    public int SatInfoDelete(int satId)
    {
        Log.d(TAG, "SatInfoDelete: ");
        return removeDeliverSystem(satId);
    }

    /*private int calcSatelliteAngle(int nTunerId, int nLongitude)
    {
        int ret = nLongitude;
        if (PLATFORM == PLATFORM_HISI) {
            Log.d(TAG, "calcSatelliteAngle(" + nTunerId + ")longitude(" + nLongitude + ")");
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            request.writeString(DTV_INTERFACE_NAME);
            request.writeInt(CMD_FE_CalUSALSAngle);
            request.writeInt(nTunerId);
            request.writeInt(nLongitude);

            invokeex(request, reply);
            ret = reply.readInt();
            if (CMD_RETURN_VALUE_SUCCESS == ret) {
                ret = reply.readInt();
            } else {
                ret = CMD_RETURN_VALUE_FAILAR;
            }
            request.recycle();
            reply.recycle();
        }
        return getReturnValue(ret);
    }*/

    // AntInfo is under SatInfo
    private int getAntennaData(SatInfo satInfo)
    {
        Log.d(TAG, "getAntennaData");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetSatelliteAntennaInfo);
        request.writeInt(satInfo.getSatId());
        invokeex(request, reply);
        int nRet = reply.readInt();

        if (CMD_RETURN_VALUE_SUCCESS == nRet)
        {
            Log.d(TAG, "getAntennaData: success");
            satInfo.Antenna.setLnbType(reply.readInt());
            satInfo.Antenna.setLnb1(reply.readInt());
            satInfo.Antenna.setLnb2(reply.readInt());
            satInfo.Antenna.setCku(reply.readInt());

            // TODO:For sat
            reply.readInt();//stAntennaInfo.stLNBCfg.u32UNIC_SCRNO
            reply.readInt();//stAntennaInfo.stLNBCfg.u32UNICIFFreqMHz
            reply.readInt();//stAntennaInfo.stLNBCfg.enSatPosn

            // TODO : Set Gpos.LnbPower
            reply.readInt();    // change in the future (Ant)

            satInfo.Antenna.setTone22k(reply.readInt());

//            Antenna.EnToneBurstSwitchType swTone = Antenna.EnToneBurstSwitchType.AUTO;
//            Antenna.EnSwitchType sw12V = Antenna.EnSwitchType.NONE;

            int diseqcType = reply.readInt();
            satInfo.Antenna.setDiseqcType(diseqcType);

            // If diseqctype == 1.0, setDiseqc to this
            if (diseqcType == SatInfo.DISEQC_TYPE_1_0)
            {
                satInfo.Antenna.setDiseqc(reply.readInt());
            }
            else
            {
                reply.readInt();
            }

            // If diseqctype == 1.1, setDiseqc to this
            // No diseqctype1.1 now
            reply.readInt();

//            Motor.EnMotorType nMotorType = Motor.EnMotorType.valueOf(reply.readInt());
            reply.readInt();    // change in the future (Ant)

//            satInfo.setAngle(calcAngleFromLongitude(reply.readInt()));
            reply.readInt();    // angle is already set

            satInfo.setPostionIndex(reply.readInt());

            satInfo.Antenna.setTone22kUse(satInfo.Antenna.getTone22k() == 0 ? 0 : 1);
//            satInfo.Antenna.setDiseqcUse(satInfo.Antenna.getDiseqcType() == 0 ? 0 : 1);
        }

        request.recycle();
        reply.recycle();
        return nRet;
    }

    // AntInfo is under SatInfo
    private int editAntenna(SatInfo satInfo)
    {
        Log.d(TAG, "editAntenna 22");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_SetSatelliteAntennaInfo);
        request.writeInt(satInfo.getSatId());

        request.writeInt(/*lnbData.getLNBTYPE().ordinal()*/satInfo.Antenna.getLnbType());
        request.writeInt(/*lnbData.getLowLO()*/satInfo.Antenna.getLnb1());
        request.writeInt(/*lnbData.getHighLO()*/satInfo.Antenna.getLnb2());
        request.writeInt(/*lnbData.getLNBBAND().ordinal()*/satInfo.Antenna.getCku());

        // TODO:For sat
        request.writeInt(0);//stAntennaInfo.stLNBCfg.u32UNIC_SCRNO
        request.writeInt(0);//stAntennaInfo.stLNBCfg.u32UNICIFFreqMHz
        request.writeInt(0);//stAntennaInfo.stLNBCfg.enSatPosn

        // TODO: Get Gpos.LNBPower
        request.writeInt(/*antenna.getLNBPower().ordinal()*/GposInfoGet().getLnbPower()); // Gpos.LNBPower(will be removed here), change in the future (Ant)

        request.writeInt(/*antenna.get22KSwitch().ordinal()*/satInfo.Antenna.getTone22k());
//        request.writeInt(/*antenna.getToneBurstSwtich().ordinal()*/0);
//        request.writeInt(/*antenna.get12VSwitch().ordinal()*/0);

        request.writeInt(/*antenna.getDiSEqCType().ordinal()*/satInfo.Antenna.getDiseqcType());
        request.writeInt(/*antenna.getDiSEqC10Port().ordinal()*/satInfo.Antenna.getDiseqc());
        request.writeInt(/*antenna.getDiSEqC11Port().ordinal()*/0); // no diseqctype1.1 now
        request.writeInt(/*antenna.getMotorType().ordinal()*/0);    // change in the future (Ant)
        request.writeInt(/*antenna.getLongitude()*/calcLongitudeFromAngle(satInfo.getAngle(), satInfo.getAngleEW()));
        request.writeInt(/*antenna.getMotorPositionID()*/satInfo.getPostionIndex());

        /*Log.e(TAG, "getPolarity=" + antenna.getPolarity());
        Log.e(TAG, "getDiSEqC10Port=" + antenna.getDiSEqC10Port());
        Log.e(TAG, "getMotorType=" + antenna.getMotorType());*/

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        Log.d(TAG, "editAntenna: Ret = " + ret);
        return getReturnValue(ret);
    }

    private List<SatInfo> getDeliverSystemList(int tunerType, int nPos, int nNum)
    {
        return getDeliverSystemList(tunerType, nPos, nNum, null);
    }

    private List<SatInfo> getDeliverSystemList(int tunerType, int nPos, int nNum, String networkName)
    {
        Log.d(TAG, "getDeliverSystemList tunerType= " + tunerType + " nPos=" + nPos + ",nNum=" + nNum);

        List<SatInfo> lstRet = null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetDeliverSystemList);
        request.writeInt(/*networkType.getValue()*/tunerType);
        request.writeInt(/*nPos*/0);
        request.writeInt(/*nNum*/SatInfo.MAX_NUM_OF_SAT);

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            lstRet = new ArrayList<SatInfo>();
            int nCount = reply.readInt();
            Log.d(TAG, "getDeliverSystemList nCount=" + nCount);
            for (int i = 0; i < nCount; i++)
            {
                int nID = reply.readInt();
                int ntunerID = reply.readInt();
                int diseqcType = reply.readInt();
                String strName = reply.readString();

                if ((null != networkName) && (! networkName.equalsIgnoreCase(strName)))
                {
                    continue;
                }

                if(TpInfo.DVBS == tunerType)
                {
                    int nlongitude = reply.readInt();

                    SatInfo satInfo = new SatInfo();
                    satInfo.setSatId(nID);
                    satInfo.setSatName(strName);
//                    satInfo.setPostionIndex(0); // set in getAntennaData(), change in the future (Sat)
                    satInfo.setLocation(0); // change in the future (Sat)
                    satInfo.setAngle(calcAngleFromLongitude(nlongitude));
                    satInfo.setAngleEW(getAngleEWFromLongitude(nlongitude));
                    satInfo.setTunerType(tunerType);
                    satInfo.Antenna.setDiseqcType(diseqcType);
                    lstRet.add(satInfo);

                    int getAntRet = getAntennaData(satInfo);    // set Ant, also set Gpos.LnbPower and Sat.PosIndex
                    Log.d(TAG, "getDeliverSystemList: getAntRet = " + getAntRet);
                }
                else
                {
                    SatInfo satInfo = new SatInfo();
                    satInfo.setSatId(nID);
                    satInfo.setSatName(strName);
//                    satInfo.setPostionIndex(0); // set in getAntennaData(), change in the future (Sat)
                    satInfo.setLocation(0); // change in the future (Sat)
                    satInfo.setAngle(0);    // change in the future (Sat)
                    satInfo.setTunerType(tunerType);
                    lstRet.add(satInfo);

                    int getAntRet = getAntennaData(satInfo);    // set Ant, also set Sat.PosIndex
                    Log.d(TAG, "getDeliverSystemList: getAntRet = " + getAntRet);
                }
            }
        }
        request.recycle();
        reply.recycle();

        return lstRet;
    }

    private SatInfo getDeliverSystemByID(int satID)
    {
        Log.d(TAG, "getDeliverSystemByID: satID = " + satID);
        SatInfo satInfo = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetDeliveryByID);
        request.writeInt(/*deliverID*/satID);

        invokeex(request, reply);
        int ret = reply.readInt();

        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int signalType = reply.readInt();
            int nID = reply.readInt();
            int diseqcType = reply.readInt();
            String strName = reply.readString();

            if (EnNetworkType.SATELLITE == EnNetworkType.valueOf(signalType))
            {
                int nlongitude = reply.readInt();
                Log.d(TAG, "getDeliverSystemByID: nlongitude = " + nlongitude);

                satInfo = new SatInfo();
                satInfo.setSatId(nID);
                satInfo.setSatName(strName);
//                satInfo.setPostionIndex(0); // set in getAntennaData(), change in the future (Sat)
                satInfo.setLocation(0); // change in the future (Sat)
                satInfo.setAngle(calcAngleFromLongitude(nlongitude));
                satInfo.setAngleEW(getAngleEWFromLongitude(nlongitude));
                satInfo.setTunerType(EnNetworkType.SATELLITE.getValue());
                satInfo.Antenna.setDiseqcType(diseqcType);
                getAntennaData(satInfo);    // set Ant, also set Gpos.LnbPower and Sat.PosIndex
                Log.d(TAG, "getDeliverSystemByID: SATELLITE ");
            }
            else
            {
                satInfo = new SatInfo();
                satInfo.setSatId(nID);
                satInfo.setSatName(strName);
//                satInfo.setPostionIndex(0); // set in getAntennaData(), change in the future (Sat)
                satInfo.setLocation(0); // change in the future (Sat)
                satInfo.setAngle(0);    // change in the future (Sat)
                satInfo.setTunerType(EnNetworkType.valueOf(signalType).getValue());
                getAntennaData(satInfo);    // set Ant, also set Gpos.LnbPower and Sat.PosIndex
                Log.d(TAG, "getDeliverSystemByID: OTHER ");
            }
        }
        else
        {
            Log.e(TAG, "getDeliverSystemByID fail ret = " + ret);
        }

        request.recycle();
        reply.recycle();

        return satInfo;
    }

    private int addSatellite(SatInfo satellite)
    {
        Log.d(TAG, "addSatellite");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_AddSatellite);
        request.writeInt(0);
        request.writeString(satellite.getSatName());

        request.writeInt(satellite.Antenna.getLnbType());
        request.writeInt(satellite.Antenna.getLnb1());
        request.writeInt(satellite.Antenna.getLnb2());
        request.writeInt(satellite.Antenna.getCku());
        request.writeInt(0);

        request.writeInt(0);
        request.writeInt(0);
        request.writeInt(0);
        request.writeInt(satellite.Antenna.getTone22k());
        request.writeInt(satellite.Antenna.getDiseqcType());

        request.writeInt(0);
        request.writeInt(0);
        request.writeInt(0);
        request.writeInt(calcLongitudeFromAngle(satellite.getAngle(), satellite.getAngleEW()));
        request.writeInt(satellite.getPostionIndex());


        invokeex(request, reply);
        int ret = reply.readInt();

        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            satellite.setSatId(reply.readInt());
            Log.d(TAG, "addSatellite satID = " + satellite.getSatId());

            // edit ant when add sat
            int editAntRet = editAntenna(satellite);
            Log.d(TAG, "addSatellite: editAntRet = " + editAntRet);
        }

        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    private int editSatellite(SatInfo satInfo, boolean isSelected)
    {
        Log.d(TAG, "editSatellite");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_SetDeliveryExtern);
        request.writeInt(EnNetworkType.SATELLITE.getValue());
        request.writeInt(satInfo.getSatId());
        request.writeInt(calcLongitudeFromAngle(satInfo.getAngle(), satInfo.getAngleEW()));
        request.writeInt(/*isSelected ? 1 : 0*/satInfo.Antenna.getDiseqcType());
        request.writeInt(satInfo.getSatName() == null ? 0 : satInfo.getSatName().length());
        request.writeString(satInfo.getSatName());
        invokeex(request, reply);
        int ret = reply.readInt();

        // edit ant when edit sat
        int editAntRet = editAntenna(satInfo);
        Log.d(TAG, "editSatellite: editAntRet = " + editAntRet);

        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    private int removeDeliverSystem(int satID)
    {
        Log.d(TAG, "removeDeliverSystem");
        return excuteCommand(CMD_PM_DelSatellite, satID);
    }

    private int getAngleEWFromLongitude(int longitude)
    {
        if (longitude > SatInfo.LONGITUDE_VALUE_MAX/2)
        {
            return SatInfo.ANGLE_W;
        }
        else
        {
            return SatInfo.ANGLE_E;
        }
    }

    private float calcAngleFromLongitude(int longitude)
    {
        // if longitude > 1800, angle = (3600 - longitude) / 10;
        // else angle = longitude / 10;

        float angle;
        int tmpLongitude = longitude;
        if (tmpLongitude > SatInfo.LONGITUDE_VALUE_MAX/2) {
            // angle_W
            tmpLongitude = SatInfo.LONGITUDE_VALUE_MAX - tmpLongitude;
        }

        angle = (float)tmpLongitude / SatInfo.LONGITUDE_VALUE_RATE;

        Log.d(TAG, "calcAngleFromLongitude: " + angle);
        return angle;
    }

    private int calcLongitudeFromAngle(float angle, int angleEW)
    {
        int longitude = (int) (angle * SatInfo.LONGITUDE_VALUE_RATE);

        if (angleEW == SatInfo.ANGLE_W)
        {
            longitude = SatInfo.LONGITUDE_VALUE_MAX - longitude;
        }

        return longitude;
    }

    public List<TpInfo> TpInfoGetListBySatId(int tunerType,int satId,int pos,int num)
    {
        Log.d(TAG, "TpInfoGetListBySatId: ");
        return getTPList(tunerType, satId, pos, num);
    }

    public TpInfo TpInfoGet(int tp_id)
    {
        Log.d(TAG, "TpInfoGet: ");
        return getTPByID(tp_id);
    }

    public int TpInfoAdd(TpInfo pTp)
    {
        Log.d(TAG, "TpInfoAdd: ");
        return addTP(pTp);
    }

    public int TpInfoUpdate(TpInfo pTp)
    {
        Log.d(TAG, "TpInfoUpdate: ");
        int ret = -1;
        if(pTp.getTunerType() == TpInfo.DVBC)
        {
            ret =  editCableTP(pTp);
        }
        else if(pTp.getTunerType() == TpInfo.DVBS)
        {
            ret = editSatelliteTP(pTp);
        }
        else if(pTp.getTunerType() == TpInfo.DVBT)
        {
            ret = editTerrestrialTP(pTp);
        }
        else if(pTp.getTunerType() == TpInfo.ISDBT)
        {
            ret = editISDBTTP(pTp);
        }

        return getReturnValue(ret);
    }

    public int TpInfoUpdateList(List<TpInfo> pTps)
    {
        Log.d(TAG, "TpInfoUpdateList: ");
        return updateTpList(pTps);
    }

    public int TpInfoDelete(int tpId)
    {
        Log.d(TAG, "TpInfoDelete: ");
        return removeTPByID(tpId);
    }

    private List<TpInfo> getTPList(int tunerType,int satId, int pos, int num)
    {
        Log.d(TAG, "getTPList");
        List<TpInfo> lstRet = null;
        if(PLATFORM == PLATFORM_HISI)
            getDeliverSystemList();

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetSatelliteTpList);
//        request.writeInt(networkType.getValue()CABLE);
        Log.d(TAG, "getTPList: " + mLocalID);
        if(PLATFORM == PLATFORM_HISI)
            request.writeInt(/*tarNetwork.getID()*/satId == MiscDefine.TpInfo.NONE_SAT_ID ? mLocalID : satId);
        else
            request.writeInt(/*tarNetwork.getID()*/satId == MiscDefine.TpInfo.NONE_SAT_ID ? 0 : satId);
        request.writeInt(/*nPos*/0);
        request.writeInt(/*nNum*/SatInfo.MAX_TP_NUM_IN_ONE_SAT);

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            lstRet = new ArrayList<TpInfo>();
            int nCount = reply.readInt();
            Log.d(TAG, "getTPList nCount=" + nCount);
            for (int i = 0; i < nCount; i++)
            {
                int nTPID = reply.readInt();
                int nType = reply.readInt();
                int nFreq = reply.readInt();
                Log.d(TAG, "getTPList: " + nType);
                TpInfo tp;
                if (nType == EnNetworkType.SATELLITE.getValue())
                {
                    int nRate = reply.readInt();
                    int nPolar = reply.readInt();
                    reply.readInt();    // version of DVBS is now fixed in hisi a8

                    tp = new TpInfo(TpInfo.DVBS);
                    tp.setTpId(nTPID);
                    tp.setSatId(satId);
                    tp.setTuner_id(0);
                    tp.SatTp.setPolar(nPolar);
                    tp.SatTp.setSymbol(nRate);
                    tp.SatTp.setFreq(nFreq);
//                    tp.SatTp.setDrot();
//                    tp.SatTp.setFec();
//                    tp.SatTp.setSpect();
//                    tp.SatTp.setOtherData();
                }
                else if (nType == EnNetworkType.TERRESTRIAL.getValue())
                {
                    int nBandWidth = reply.readInt();
                    int nVersion = reply.readInt();

                    tp = new TpInfo(TpInfo.DVBT);
                    tp.setTpId(nTPID);

                    if(PLATFORM == PLATFORM_HISI) {
                        tp.setSatId(satId == MiscDefine.TpInfo.NONE_SAT_ID ? mLocalID : satId);
                    }
                    else {
                        tp.setSatId(satId == MiscDefine.TpInfo.NONE_SAT_ID ? 0 : satId);
                    }

                    tp.setTuner_id(0);
                    tp.TerrTp.setFreq(nFreq);
                    tp.TerrTp.setBand(getBandFromRead(nBandWidth));
                }
                else if (nType == EnNetworkType.ISDB_TER.getValue())
                {
                    int nBandWidth = reply.readInt();
                    int nMod = reply.readInt();
                    int nVersion = reply.readInt();

                    tp = new TpInfo(TpInfo.ISDBT);
                    tp.setTpId(nTPID);

                    if(PLATFORM == PLATFORM_HISI) {
                        tp.setSatId(satId == MiscDefine.TpInfo.NONE_SAT_ID ? mLocalID : satId);
                    }
                    else {
                        tp.setSatId(satId == MiscDefine.TpInfo.NONE_SAT_ID ? 0 : satId);
                    }

                    tp.setTuner_id(0);
                    tp.TerrTp.setFreq(nFreq);
                    tp.TerrTp.setBand(getBandFromRead(nBandWidth));
                }
                else    // CABLE
                {
                    int nRate = reply.readInt();
                    int nMod = reply.readInt();
//                    EnVersionType version = EnVersionType.Version_1;

                    tp = new TpInfo(TpInfo.DVBC);
                    tp.setTpId(nTPID);

                    if(PLATFORM == PLATFORM_HISI) {
                        tp.setSatId(satId == MiscDefine.TpInfo.NONE_SAT_ID ? mLocalID : satId);
                    }
                    else {
                        tp.setSatId(satId == MiscDefine.TpInfo.NONE_SAT_ID ? 0 : satId);
                    }

                    tp.setTuner_id(0);
                    tp.CableTp.setFreq(nFreq);
                    tp.CableTp.setSymbol(nRate);
                    tp.CableTp.setQam(getPesiQamFromEnMod(nMod));
//                    tp.CableTp.setChannel();
//                    tp.CableTp.setOtherData();
                }

                lstRet.add(tp);
            }
        }

        Log.d(TAG, "getTPList read");
        request.recycle();
        reply.recycle();
        return lstRet;
    }

    private TpInfo getTPByID(int tpID)
    {
        Log.d(TAG, "getTPByID: tpID = " + tpID);
        TpInfo tp = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetTpByID);
        request.writeInt(tpID);

        invokeex(request, reply);
        int ret = reply.readInt();

        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int nSignalType = reply.readInt();
            int nTPID = reply.readInt();
            int nSID = reply.readInt();
            int nFreq = reply.readInt();
            int nRate = reply.readInt();
            int polar = reply.readInt();
            int version = reply.readInt();
            int nBandWidth = reply.readInt();
            int enQAMValue = reply.readInt();

            if (EnNetworkType.SATELLITE == EnNetworkType.valueOf(nSignalType))
            {
                tp = new TpInfo(TpInfo.DVBS);
                tp.setTpId(nTPID);
                tp.setSatId(nSID);
                tp.setTuner_id(0);
                tp.SatTp.setPolar(polar);
                tp.SatTp.setSymbol(nRate);
                tp.SatTp.setFreq(nFreq);
            }
            else if (EnNetworkType.TERRESTRIAL == EnNetworkType.valueOf(nSignalType))
            {
                tp = new TpInfo(TpInfo.DVBT);
                tp.setTpId(nTPID);
                tp.setSatId(nSID);
                tp.setTuner_id(0);
                tp.TerrTp.setFreq(nFreq);
                tp.TerrTp.setBand(getBandFromRead(nBandWidth));
            }
            else if (EnNetworkType.ISDB_TER == EnNetworkType.valueOf(nSignalType))
            {
                tp = new TpInfo(TpInfo.ISDBT);
                tp.setTpId(nTPID);
                tp.setSatId(nSID);
                tp.setTuner_id(0);
                tp.TerrTp.setFreq(nFreq);
                tp.TerrTp.setBand(getBandFromRead(nBandWidth));
            }
            else    // CABLE
            {
                tp = new TpInfo(TpInfo.DVBC);
                tp.setTpId(nTPID);
                tp.setSatId(nSID);
                tp.setTuner_id(0);
                tp.CableTp.setFreq(nFreq);
                tp.CableTp.setSymbol(nRate);
                tp.CableTp.setQam(getPesiQamFromEnMod(enQAMValue));
//                    tp.CableTp.setChannel();
//                    tp.CableTp.setOtherData();
            }
        }
        else
        {
            Log.e(TAG, "getTPByID fail nRet = " + ret);
        }

        request.recycle();
        reply.recycle();
        return tp;
    }

    private int addTP(TpInfo tpInfo)
    {
        Log.d(TAG, "addTP");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_SatelliteAddTp);

        request.writeInt(/*nDeliverID*/tpInfo.getSatId());
        request.writeInt(/*networkType.getValue()*/tpInfo.getTunerType());

        int tunerType = tpInfo.getTunerType();

        if(TpInfo.DVBT == tunerType)
        {
            request.writeInt(/*tempTP.getFrequency()*/tpInfo.TerrTp.getFreq());
            request.writeInt(/*tempTP.getBandWidth()*/getBandForWrite(tpInfo.TerrTp.getBand()));
            request.writeInt(/*tempTP.getModulation().ordinal()*/0);    // ignore
            request.writeInt(/*tempTP.getVersion().ordinal()*/0);
        }
        else if(TpInfo.ISDBT == tunerType)
        {
            request.writeInt(/*tempTP.getFrequency()*/tpInfo.TerrTp.getFreq());
            request.writeInt(/*tempTP.getBandWidth()*/getBandForWrite(tpInfo.TerrTp.getBand()));
            request.writeInt(/*tempTP.getModulation().ordinal()*/0);    // ignore
            request.writeInt(/*tempTP.getVersion().ordinal()*/0);
        }
        else if(TpInfo.DVBS == tunerType)
        {
            request.writeInt(/*tempTP.getFrequency()*/tpInfo.SatTp.getFreq());
            request.writeInt(/*tempTP.getSymbolRate()*/tpInfo.SatTp.getSymbol());
            request.writeInt(/*tempTP.getPolarity().ordinal()*/tpInfo.SatTp.getPolar());
            request.writeInt(0);
        }
        else    // CABLE
        {
            request.writeInt(/*tempTP.getFrequency()*/tpInfo.CableTp.getFreq());
            request.writeInt(/*tempTP.getSymbolRate()*/tpInfo.CableTp.getSymbol());
            request.writeInt(/*tempTP.getModulation().ordinal()*/getEnModFromPesiQam(tpInfo.CableTp.getQam()));
            request.writeInt(0);
        }

        invokeex(request, reply);
        int ret = reply.readInt();

        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            tpInfo.setTpId(reply.readInt());
            Log.d(TAG, "addTP: tpid = " + tpInfo.getTpId());
        }

        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    private int editCableTP(TpInfo tpInfo)
    {
        Log.d(TAG, "editCableTP");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_SetSatelliteTp);
        request.writeInt(/*nTPID*/tpInfo.getTpId());
        request.writeInt(/*EnNetworkType.CABLE.getValue()*/EnNetworkType.CABLE.getValue());
        request.writeInt(/*nFreq*/tpInfo.CableTp.getFreq());
        request.writeInt(/*nRate*/tpInfo.CableTp.getSymbol());
        request.writeInt(/*nMod*/getEnModFromPesiQam(tpInfo.CableTp.getQam()));
        request.writeInt(0);
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        Log.d(TAG, "editCableTP: ret = " + ret);
        return getReturnValue(ret);
    }

    private int editISDBTTP(TpInfo tpInfo)
    {
        Log.d(TAG, "editISDBTTP");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_SetSatelliteTp);
        request.writeInt(/*nTPID*/tpInfo.getTpId());
        request.writeInt(EnNetworkType.ISDB_TER.getValue());
        request.writeInt(/*nFreq*/tpInfo.TerrTp.getFreq());
        request.writeInt(/*nBandWidth*/getBandForWrite(tpInfo.TerrTp.getBand()));
        request.writeInt(/*nMod*/0);    // ignore
        request.writeInt(/*nVer*/0);
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    private int editSatelliteTP(TpInfo tpInfo)
    {
        Log.d(TAG, "editSatelliteTP");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_SetSatelliteTp);
        request.writeInt(/*nTPID*/tpInfo.getTpId());
        request.writeInt(EnNetworkType.SATELLITE.getValue());
        request.writeInt(/*nFreq*/tpInfo.SatTp.getFreq());
        request.writeInt(/*nRate*/tpInfo.SatTp.getSymbol());
        request.writeInt(/*nPol*/tpInfo.SatTp.getPolar());
        request.writeInt(0);
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    private int editTerrestrialTP(TpInfo tpInfo)
    {
        Log.d(TAG, "editTerrestrialTP");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_SetTp);
        request.writeInt(/*nTPID*/tpInfo.getTpId());
        request.writeInt(EnNetworkType.TERRESTRIAL.getValue());
        request.writeInt(/*nFreq*/tpInfo.TerrTp.getFreq());
        request.writeInt(/*nBandWidth*/getBandForWrite(tpInfo.TerrTp.getBand()));
        request.writeInt(/*nMod*/0);    // ignore
        request.writeInt(/*nVer*/0);
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    private int removeTPByID(int tpID)
    {
        Log.d(TAG, "removeTPByID");
        int ret = excuteCommand(CMD_PM_DelSatelliteTp, tpID);

        Log.d(TAG, "removeTPByID: ret = " + ret);
        return getReturnValue(ret);
    }

    private int getPesiQamFromEnMod(int enMod)
    {
        int nQam = TpInfo.Cable.QAM_16;
        if(PLATFORM == PLATFORM_HISI) {
            switch (EnModulation.valueOf(enMod)) {
                case UNDEFINED:
                    break;
                case QAM4_NR:
                    break;
                case QAM4:
                    break;
                case QAM16:
                    nQam = TpInfo.Cable.QAM_16;
                    break;
                case QAM32:
                    nQam = TpInfo.Cable.QAM_32;
                    break;
                case QAM64:
                    nQam = TpInfo.Cable.QAM_64;
                    break;
                case QAM128:
                    nQam = TpInfo.Cable.QAM_128;
                    break;
                case QAM256:
                    nQam = TpInfo.Cable.QAM_256;
                    break;
                case QAM512:
                    break;
                case QAM640:
                    break;
                case QAM768:
                    break;
                case QAM896:
                    break;
                case QAM1024:
                    break;
                case QPSK:
                    break;
                case BPSK:
                    break;
                case OQPSK:
                    break;
                case _8VSB:
                    break;
                case _16VSB:
                    break;
            }
        }
        else {
            nQam = enMod;
        }
        return nQam;
    }

    private int getEnModFromPesiQam(int nQam)
    {
        EnModulation enMod = EnModulation.UNDEFINED;
        if(PLATFORM == PLATFORM_HISI) {
            switch (nQam) {
//                case TpInfo.Cable.QAM_AUTO:
//                    enMod = EnModulation.QAM256; // no AUTO : mantis 0004981
//                    break;
                case TpInfo.Cable.QAM_16:
                    enMod = EnModulation.QAM16;
                    break;
                case TpInfo.Cable.QAM_32:
                    enMod = EnModulation.QAM32;
                    break;
                case TpInfo.Cable.QAM_64:
                    enMod = EnModulation.QAM64;
                    break;
                case TpInfo.Cable.QAM_128:
                    enMod = EnModulation.QAM128;
                    break;
                case TpInfo.Cable.QAM_256:
                    enMod = EnModulation.QAM256;
                    break;
            }
            return enMod.getValue();
        }
        else{
            return nQam;
        }
    }

    private int getBandFromRead(int nBand)
    {
        if(PLATFORM == PLATFORM_HISI) {
            switch (nBand) {
                case 6000:
                    nBand = TpInfo.Terr.BAND_6MHZ;
                    break;
                case 7000:
                    nBand = TpInfo.Terr.BAND_7MHZ;
                    break;
                case 8000:
                    nBand = TpInfo.Terr.BAND_8MHZ;
                    break;
            }
        }

        return nBand;
    }

    private int getBandForWrite(int pesiBand)
    {
        if(PLATFORM == PLATFORM_HISI) {
            switch (pesiBand) {
                case TpInfo.Terr.BAND_6MHZ:
                    pesiBand = 6000;
                    break;
                case TpInfo.Terr.BAND_7MHZ:
                    pesiBand = 7000;
                    break;
                case TpInfo.Terr.BAND_8MHZ:
                    pesiBand = 8000;
                    break;
            }
        }

        return pesiBand;
    }

    public int AvControlPlayByChannelId(int playId,long channelId, int groupType, int show){
        Log.d(TAG, "AvControlPlayByChannelId: channelId="+channelId);
        return avPlayStart(playId, channelId, show);
    }

    public int AvControlPrePlayStop()
    {
        Log.d(TAG, "AvControlPrePlayStop: ");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_PreStart);
        request.writeInt(0);
        request.writeInt(0xFFFFFFFF);
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        Log.d(TAG, "AvControlPrePlayStop(),ret = " + ret);
        return getReturnValue(ret);
    }

    public int AvControlPlayStop(int playId) {
        Log.d(TAG, "AvControlPlayStop: ");
        return avPlayStop(playId);
    }

    public int AvControlChangeRatioConversion(int playId,int ratio,int conversion) {
        Log.d(TAG, "AvControlChangeRatioConversion: ");
        return setRatioConversion(playId,ratio,conversion);
    }
    public int AvControlSetFastChangeChannel(long PreChannelId, long NextChannelId)//Scoty 20180816 add fast change channel
    {
        return setFastChangeChannel(PreChannelId,NextChannelId);
    }
/*
    public int AvControlChangeRatio(int playId,int ratio) {
        return 0;
    }

    public int AvControlChangeConversion(int playId,int conversion) {
        return 0;
    }
*/
    public int AvControlChangeResolution(int playId,int resolution) {
        return 0;
    }

    public int AvControlChangeAudio(int playId, AudioInfo.AudioComponent component) {
        Log.d(TAG, "AvControlChangeAudio: ");
        return selectAudio(playId, component);
    }

    public int AvControlSetVolume(int volume) {
        Log.d(TAG, "AvControlSetMute: ");
        return setVolume(volume);
    }

    public int AvControlGetVolume() {
        Log.d(TAG, "AvControlGetVolume: ");
        return getVolume();
    }

    public int AvControlSetMute(int playId,boolean mute) {
        Log.d(TAG, "AvControlSetMute: ");
        return setMuteStatus(playId, mute);
    }

    public int AvControlSetTrackMode(int playId, EnAudioTrackMode stereo) {
        Log.d(TAG, "AvControlSetTrackMode: ");
        return setTrackMode(playId, stereo);
    }

    public int AvControlAudioOutput(int playId,int byPass) {
        return setAudioOutputMode(playId,byPass);
    }

    public int AvControlClose(int playId) {
        Log.d(TAG, "AvControlClose: ");
        return releasePlayResource(playId, 0);
    }

    public int AvControlOpen(int playId) {
        Log.d(TAG, "AvControlOpen: ");
        return resumePlayResource(playId);
    }

    public int AvControlShowVideo(int playId,boolean show) {
        Log.d(TAG, "AvControlShowVideo: ");
        return showVideo(playId, show);
    }

    public int AvControlFreezeVideo(int playId,boolean freeze) {
        Log.d(TAG, "AvControlFreezeVideo: ");
        return freezeVideo(playId, freeze);
    }

    //return value shoulde be check
    public AudioInfo AvControlGetAudioListInfo(int playId) {
        Log.d(TAG, "AvControlGetAudioListInfo: ");
        return getCurrentAudio(playId);
    }

    /* return status =  LIVEPLAY,TIMESHIFTPLAY.....etc  */
    public int AvControlGetPlayStatus(int playId) {
        Log.d(TAG, "AvControlGetPlayStatus: ");
        return getPlayStatus(playId).getValue();
    }

    public boolean AvControlGetMute(int playId) {
        Log.d(TAG, "AvControlGetMute: ");
        return getMuteStatus(playId);
    }

    public EnAudioTrackMode AvControlGetTrackMode(int playId) {
        Log.d(TAG, "AvControlGetTrackMode: ");
        return getTrackMode(playId);
    }

    public int AvControlGetRatio(int playId){
        Log.d(TAG, "AvControlGetRatio: ");
        return getRatio(playId);
    }

    public int AvControlSetStopScreen(int playId, int stopType) {
        Log.d(TAG, "AvControlSetStopScreen: ");
        return setStopMode(playId, stopType);
    }

    public int AvControlGetStopScreen(int playId) {
        Log.d(TAG, "AvControlGetStopScreen: ");
        return getStopMode(playId);
    }

    public int AvControlGetFPS(int playId) {
        Log.d(TAG, "AvControlGetFPS: ");
        return getFPS(playId);
    }

    public int AvControlEwsActionControl(int playId, boolean enable) {
        Log.d(TAG, "AvControlEwsActionControl: ");
        return ewsActionControl(playId, enable);
    }

    //input value shoulde be check
    public int AvControlSetWindowSize(int playId, Rect rect) {
        Log.d(TAG, "AvControlSetWindowSize: ");
        return setWindowRect(playId, rect);
    }

    //return value shoulde be check
    public Rect AvControlGetWindowSize(int playId) {
        Log.d(TAG, "AvControlGetWindowSize: ");
        return getWindowSize(playId);
    }

    public int AvControlGetVideoResolutionHeight(int playId) {
        Log.d(TAG, "AvControlGetVideoResolutionHeight: ");
        return getVideoResolutionHeight(playId);
    }

    public int AvControlGetVideoResolutionWidth(int playId) {
        Log.d(TAG, "AvControlGetVideoResolutionWidth: ");
        return getVideoResolutionWidth(playId);
    }

    /* 0: dolby digital, 1: dolby digital plus */
    public int AvControlGetDolbyInfoStreamType(int playId) {
        Log.d(TAG, "AvControlGetDolbyInfoStreamType: ");
        return getDolbyInfoStreamType(playId);
    }

    /**
     * get dolby acmod.<br>
     * CN:?��?声�?信息<br>
     *
     *
     * @return 0: "1+1"; 1: "1/0"; 2: "2/0"; 3: "3/0"; 4:"2/1"; 5:"3/1"; 6:"2/2"; 7:"3/2"; other：error<br>
     *          CN:0: "1+1"; 1: "1/0"; 2: "2/0"; 3: "3/0"; 4:"2/1"; 5:"3/1"; 6:"2/2"; 7:"3/2";?��?，�?误�?br>
     */
    public int AvControlGetDolbyInfoAcmod(int playId) {
        Log.d(TAG, "AvControlGetDolbyInfoAcmod: ");
        return getDolbyInfoAcmod(playId);
    }

    public SubtitleInfo.SubtitleComponent AvControlGetCurrentSubtitle(int playId) {
        Log.d(TAG, "AvControlGetCurrentSubtitle: ");
        return getCurrentSubtitle(playId);
    }

    public SubtitleInfo AvControlGetSubtitleList(int playId) {
        Log.d(TAG, "AvControlGetSubtitleList: ");
        return getSubtitleComponents(playId);
    }

    public int AvControlSelectSubtitle(int playId,SubtitleInfo.SubtitleComponent subtitleComponent) {
        Log.d(TAG, "AvControlSelectSubtitle: ");
        return selectSubtitle(playId, subtitleComponent);
    }

    public int AvControlShowSubtitle(int playId,boolean enable) {
        Log.d(TAG, "AvControlShowSubtitle: ");
        return showSubtitle(playId, enable);
    }

    public boolean AvControlIsSubtitleVisible(int playId) {
        Log.d(TAG, "AvControlIsSubtitleVisible: ");
        return isSubtitleVisible(playId);
    }

    public int AvControlSetSubtHiStatus(int playId,boolean HiOn) {
        Log.d(TAG, "AvControlSetSubtHiStatus: ");
        return setSubtitleHiStatus(playId, HiOn);
    }
    
    public int AvControlSetSubtitleLanguage(int playId,int index, String lang) {
        Log.d(TAG, "AvControlSetSubtitleLanguage: ");
        return setSubtitleLanguage(playId, index, lang);
    }

    public TeletextInfo AvControlGetCurrentTeletext(int playId) {
        Log.d(TAG, "AvControlGetCurrentTeletext: ");
        return getCurrentTeletext(playId);
    }

    public List<TeletextInfo> AvControlGetTeletextList(int playId) {
        Log.d(TAG, "AvControlGetTeletextList: ");
        return getTeletectComponents(playId);
    }

    public int AvControlShowTeletext(int playId,boolean enable) {
        Log.d(TAG, "AvControlShowTeletext: ");
        return showTeletext(playId, enable);
    }

    public boolean AvControlIsTeletextVisible(int playId) {
        Log.d(TAG, "AvControlIsTeletextVisible: ");
        return isTeletextVisible(playId);
    }

    public boolean AvControlIsTeletextAvailable(int playId) {
        Log.d(TAG, "AvControlIsTeletextAvailable: ");
        return isTeletextAvailable(playId);
    }

    public int AvControlSetTeletextLanguage(int playId,String primeLang) {
        Log.d(TAG, "AvControlSetTeletextLanguage: ");
        return setTtxLang(playId, primeLang);
    }

    public String AvControlGetTeletextLanguage(int playId) {//eric lin 20180705 get ttx lang
        Log.d(TAG, "AvControlGetTeletextLanguage: ");
        return getTtxLang(playId);
    }

    public int AvControlSetCommand(int playId,int keyCode) {
        Log.d(TAG, "AvControlSetCommand: ");
        return setCommand(playId, keyCode);
    }

    public Date AvControlGetTimeShiftBeginTime(int playId) {
        Log.d(TAG, "AvControlGetTimeShiftBeginTime: ");
        return getTimeShitBeginTime(playId);
    }

    public Date AvControlGetTimeShiftPlayTime(int playId) {
        Log.d(TAG, "AvControlGetTimeShiftPlayTime: ");
        return getTimeShiftPlayTime(playId);
    }

    public int AvControlGetTimeShiftRecordTime(int playId) {
        Log.d(TAG, "AvControlGetTimeShiftRecordTime: ");
        return getTimeShiftRecordTime(playId);
    }

    public int AvControlGetTrickMode(int playId) {
        Log.d(TAG, "AvControlGetTrickMode: ");
        return getCurrentTrickMode(playId);
    }

    public int AvControlTimeshiftTrickPlay(int playId,int trickMode) {
        Log.d(TAG, "AvControlTimeshiftTrickPlay: ");
        return startTrickPlay(playId, trickMode);
    }

    public int AvControlTimeshiftPausePlay(int playId) {
        Log.d(TAG, "AvControlTimeshiftPausePlay: ");
        return pausePlay(playId);
    }

    public int AvControlTimeshiftPlay(int playId) {
        Log.d(TAG, "AvControlTimeshiftPlay: ");
        return play(playId);
    }

    public int AvControlTimeshiftSeekPlay(int playId,long seekTime) {
        Log.d(TAG, "AvControlTimeshiftSeekPlay: ");
        return seekPlay(playId, seekTime);
    }

    public int AvControlStopTimeShift(int playId) {
        Log.d(TAG, "AvControlStopTimeShift: ");
        return stopTimeShift(playId);
    }

    public int UpdateUsbSoftWare(String filename)
    {
        return usbUpdate(filename);
    }
    public int UpdateFileSystemSoftWare(String pathAndFileName , String partitionName)
    {
        return fileSystemUpdate(pathAndFileName,partitionName);
    }

    public int UpdateOTADVBCSoftWare(int tpId, int freq, int symbol, int qam)
    {
        return DVBCOTAUpdate(tpId,freq,symbol,qam);
    }

    public int UpdateOTADVBTSoftWare(int tpId, int freq, int bandwith, int qam, int priority)
    {
        return DVBTOTAUpdate(tpId,freq,bandwith,qam,priority);
    }

    public int UpdateOTADVBT2SoftWare(int tpId, int freq, int bandwith, int qam, int channelmode)
    {
        return DVBT2OTAUpdate(tpId,freq,bandwith,qam,channelmode);
    }

    public int UpdateOTAISDBTSoftWare(int tpId, int freq, int bandwith, int qam, int priority)
    {
        return ISDBTOTAUpdate(tpId,freq,bandwith,qam,priority);
    }

    public int UpdateMtestOTASoftWare()//Scoty 20190410 add Mtest Trigger OTA command
    {
        return MtestOTAUpdate();
    }

    public int PlayById(int playerID, long channelID, int show)
    {
        Log.d(TAG, "playById(" + channelID + ")");

        return excuteCommand(CMD_AV_Start, channelID, show); //  1 means true play, false means send program in play, but not play at once
    }

    private int avPlayStart(int playID, long channelID ,int show)
    {
        Log.d(TAG, "av play start channelID = "+ channelID);
        //resumePlayResource(playID);
        PlayById(playID, channelID, show);
        return 0;
    }

    public int stopLivePlay(int playerID,int mode)
    {
        Log.d(TAG, "stopLivePlay(" + mode + ")");

        // TODO: need new cmd or new interface
        return excuteCommand(CMD_AV_Stop);
    }

    private int avPlayStop(int playID)
    {
        Log.d(TAG, "avPlayStop: ");
        int stopType =0;
        stopType = AvControlGetStopScreen(playID);
        Log.d(TAG, "avPlayStop: stopType="+stopType);
        stopLivePlay(playID, stopType);//excuteCommand(CMD_AV_StopLivePlay,*//*PlayerID*//*playID, *//*EnStopType.BLACKSCREEN*//*stopType);
        //releasePlayResource(playID, 0);//excuteCommand(CMD_AV_ReleasePlayResource, playID, 0);
        Log.d(TAG, "av play stop");
        return 0;
    }

    public int releasePlayResource(int playerID, int resourceType)
    {
        Log.d(TAG, "releasePlayResource(" + playerID + "),type(" + resourceType + ")");
        if (0 != playerID)
    {
            Log.d(TAG, "resumePlayResource() only support one instance.");
        return 0;
    }
        return excuteCommand(CMD_AV_Close);
    }

    private int resumePlayResource(int playerID)
    {
        Log.d(TAG, "resumePlayResource()");
        if (0 != playerID)
        {
            Log.d(TAG, "resumePlayResource() only support one instance.");
        return 0;
    }
        return excuteCommand(CMD_AV_Open);
    }

    private EnPlayStatus getPlayStatus(int playerID)
    {
        Log.d(TAG, "GetPlayStatus()");

        EnPlayStatus playStatus = EnPlayStatus.IDLE;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetAVStatus);
        request.writeInt(0); // 0 means get play satus HI_SVR_AV_GET_PLAY_STATUS

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int status = reply.readInt();
            playStatus = EnPlayStatus.valueOf(status);
        }
        request.recycle();
        reply.recycle();
        Log.d(TAG, "GetPlayStatus(), EnPlayStatus status = " + playStatus);

        return playStatus;
    }

    private int showVideo(int playerID, boolean bShow)
    {
        Log.d(TAG, "showVideo(" + bShow + ")");
        int tmpFlag = (bShow)? 1 : 0;
        return excuteCommand(CMD_AV_ShowVideo, tmpFlag);
    }

    private int freezeVideo(int playerID, boolean bFreeze)
    {
        Log.d(TAG, "freezeVideo(" + bFreeze + ")");
        int tmpFlag = (bFreeze)? 1 : 0;
        return excuteCommand(CMD_AV_FreezeVideo, tmpFlag);
    }

    private int selectAudio(int playerID, AudioInfo.AudioComponent audio)
    {
        int audioPid = audio.getPid();
        int index = 0;
        boolean bGet = false;
        Parcel request1 = Parcel.obtain();
        Parcel reply1 = Parcel.obtain();
        request1.writeString(DTV_INTERFACE_NAME);
        request1.writeInt(CMD_AV_GetMutiAudioInfo);

        invokeex(request1, reply1);
        int ret1 = reply1.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret1)
    {
            int audioCount = reply1.readInt();
            int curIndex = reply1.readInt();
            for (int i = 0 ; i < audioCount; i++ )
    {
                int pid = reply1.readInt();
                int audioType = reply1.readInt();
                int adType = reply1.readInt(); //u32Reserved
                int compTag = reply1.readInt();
                int compType = reply1.readInt();
                int streamContent = reply1.readInt();
                int trackMode = reply1.readInt(); // u8Reserved2
                int reserved3 = reply1.readInt(); // u8Reserved3
                String languageCode = reply1.readString();

                if (pid == audioPid)
                {
                    index = i;
                    bGet = true;
                    break;
                }
            }
        }

        request1.recycle();
        reply1.recycle();

        if (bGet == false)
    {
            Log.e(TAG, "not find this audio track(languageCode = " + audio.getLangCode() + ",pid =" + audio.getPid()
                    + ",type =" + audio.getAudioType() + ")");
            return CMD_RETURN_VALUE_FAILAR;
    }

        Log.d(TAG, "selectAudio(languageCode = " + audio.getLangCode() + ",pid =" + audio.getPid()
                + ",type =" + audio.getAudioType() + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_ChangeAudioTrack);
        request.writeInt(index);

        invokeex(request, reply);
        int ret = reply.readInt();

        request.recycle();
        reply.recycle();
        Log.d(TAG, "ret = " + ret);
        return getReturnValue(ret);
    }

    private AudioInfo getCurrentAudio(int playerID)
    {
        Log.d(TAG, "getCurrentAudio()");

        AudioInfo audioInfo = null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetMutiAudioInfo);

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int audioCount = reply.readInt();
            int curIndex = reply.readInt();

            audioInfo = new AudioInfo();
            audioInfo.setCurPos(curIndex);
            for (int i = 0 ; i < audioCount; i++ )
            {
                int pid = reply.readInt();
                int audioType = reply.readInt();
                int adType = reply.readInt(); //u32Reserved
                int compTag = reply.readInt();
                int compType = reply.readInt();
                int streamContent = reply.readInt();
                int trackMode = reply.readInt(); // u8Reserved2
                int reserved3 = reply.readInt(); // u8Reserved3
                String languageCode = reply.readString();

                AudioInfo.AudioComponent audioComponent = new AudioInfo.AudioComponent();
                audioComponent.setLangCode(languageCode);
                audioComponent.setPid(pid);
                audioComponent.setAudioType(/*EnStreamType.valueOf(audioType)*/audioType);
                audioComponent.setAdType(/*EnAudioType.valueOf(adType)*/adType);
                audioComponent.setTrackMode(/*EnAudioTrackMode.valueOf(trackMode)*/EnAudioTrackMode.valueOf(trackMode).getValue());
                audioComponent.setPos(i);   // need test
                audioInfo.ComponentList.add(audioComponent);

                /*if (curIndex == i)
                {
                    Log.d(TAG, "getCurrentAudio:languageCode = " + languageCode + ",pid = " + pid + ",audioType = " + audioType);
                }*/
            }
        }

        request.recycle();
        reply.recycle();

        return audioInfo;
    }

    private int setMuteStatus(int playerID, boolean bMuteFlag)
    {
        Log.d(TAG, "setMuteStatus(" + bMuteFlag + "),(id=" + playerID + ")");
        int tmp = (bMuteFlag)? 1 : 0;
        return excuteCommand(CMD_AV_SetMute, tmp);
    }


    private boolean getMuteStatus(int playerID)
    {
        Log.d(TAG, "getMuteStatus");
        return 1 == excuteCommandGetII(CMD_AV_GetMute);
    }

    //eric lin a8, a8 not support
    public int setVolume(int volume){
        return 0;
    }
    /*public int setVolume(int volume)
    {
        Log.d(TAG, "setVolume(" + volume + ")");
        if (volume >= 0)
        {
            return excuteCommand(CMD_AV_SetVolume, volume);
        }
        else
        {
            return CMD_RETURN_VALUE_FAILAR;
        }
    }*/

    //eric lin a8, a8 not support
    public int getVolume(){
        return 0;
    }
    /*public int getVolume()
    {
        Log.d(TAG, "getVolume");
        return excuteCommandGetII(CMD_AV_GetVolume);
    }*/

    private int ConverToStackTrack(EnHisAudioTrackMode enTrackMode)
    {
        int svrTrackMode = 0;

        switch (enTrackMode)
        {
            case AUDIO_TRACK_STEREO:
                svrTrackMode = 0;/* HI_SVR_AV_AUDIO_TRACK_MODE_STEREO */
                break;
            case AUDIO_TRACK_DOUBLE_MONO:
                svrTrackMode = 1;/* HI_SVR_AV_AUDIO_TRACK_MODE_MONO */
                break;
            case AUDIO_TRACK_DOUBLE_LEFT:
                svrTrackMode = 4;/* HI_SVR_AV_AUDIO_TRACK_MODE_LEFT */
                break;
            case AUDIO_TRACK_DOUBLE_RIGHT:
                svrTrackMode = 5;/* HI_SVR_AV_AUDIO_TRACK_MODE_RIGHT */
                break;
            case AUDIO_TRACK_EXCHANGE:
                break;
            case AUDIO_TRACK_ONLY_RIGHT:
                svrTrackMode = 5;/* HI_SVR_AV_AUDIO_TRACK_MODE_RIGHT */
                break;
            case AUDIO_TRACK_ONLY_LEFT:
                svrTrackMode = 4;/* HI_SVR_AV_AUDIO_TRACK_MODE_LEFT */
                break;
            case AUDIO_TRACK_MUTED:
                break;
            default:
                break;
        }

        return svrTrackMode;
    }

    private EnHisAudioTrackMode ConverToJavaTrack(int index)
    {
        EnHisAudioTrackMode enTrackMode = EnHisAudioTrackMode.AUDIO_TRACK_STEREO;

        switch (index)
        {
            case 0://stereo
                enTrackMode = EnHisAudioTrackMode.AUDIO_TRACK_STEREO;
                break;
            case 1://mono
                enTrackMode = EnHisAudioTrackMode.AUDIO_TRACK_DOUBLE_MONO;
                break;
            case 2://stereo sap, only for atsc
                enTrackMode = EnHisAudioTrackMode.AUDIO_TRACK_STEREO;
                break;
            case 3://mono sap, only for atsc
                enTrackMode = EnHisAudioTrackMode.AUDIO_TRACK_DOUBLE_MONO;
                break;
            case 4://left
                enTrackMode = EnHisAudioTrackMode.AUDIO_TRACK_DOUBLE_LEFT;
                break;
            case 5://right
                enTrackMode = EnHisAudioTrackMode.AUDIO_TRACK_DOUBLE_RIGHT;
                break;
            default:
                break;
        }

        return enTrackMode;
    }

    private int setTrackMode(int playerID, EnAudioTrackMode enTrackMode)
    {
        Log.d(TAG, "setTrackMode(" + enTrackMode + ")");
        int track = enTrackMode.getValue();
        if(PLATFORM == PLATFORM_HISI) {
            EnHisAudioTrackMode hisTrackMode;
            if (enTrackMode == EnAudioTrackMode.MPEG_AUDIO_TRACK_STEREO)
                hisTrackMode = EnHisAudioTrackMode.AUDIO_TRACK_STEREO;
            else if (enTrackMode == EnAudioTrackMode.MPEG_AUDIO_TRACK_LEFT)
                hisTrackMode = EnHisAudioTrackMode.AUDIO_TRACK_DOUBLE_LEFT;
            else if (enTrackMode == EnAudioTrackMode.MPEG_AUDIO_TRACK_RIGHT)
                hisTrackMode = EnHisAudioTrackMode.AUDIO_TRACK_DOUBLE_RIGHT;
            else
                hisTrackMode = EnHisAudioTrackMode.AUDIO_TRACK_STEREO;

            track = ConverToStackTrack(hisTrackMode);
        }
        return excuteCommand(CMD_AV_SetAudioTrackMode, track);
    }

    private EnAudioTrackMode getTrackMode(int playerID)
    {
        Log.d(TAG, "getTrackMode()");        
        EnHisAudioTrackMode enTrackMode = EnHisAudioTrackMode.AUDIO_TRACK_BUTT;
        EnAudioTrackMode trackMode = EnAudioTrackMode.MPEG_AUDIO_TRACK_BUTT;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetAudioTrackMode);

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int mode = reply.readInt();
            if(PLATFORM == PLATFORM_HISI) {
                enTrackMode = ConverToJavaTrack(mode);
                if(enTrackMode == EnHisAudioTrackMode.AUDIO_TRACK_STEREO)
                    trackMode = EnAudioTrackMode.MPEG_AUDIO_TRACK_STEREO;
                else if(enTrackMode == EnHisAudioTrackMode.AUDIO_TRACK_DOUBLE_LEFT)
                    trackMode = EnAudioTrackMode.MPEG_AUDIO_TRACK_LEFT;
                else if(enTrackMode == EnHisAudioTrackMode.AUDIO_TRACK_DOUBLE_RIGHT)
                    trackMode = EnAudioTrackMode.MPEG_AUDIO_TRACK_RIGHT;
                else
                    trackMode = EnAudioTrackMode.MPEG_AUDIO_TRACK_STEREO;
            }
            else {
                trackMode = EnAudioTrackMode.valueOf(mode);
            }
        }
        request.recycle();
        reply.recycle();
        Log.d(TAG, "getTrackMode(),enTrackMode = " + enTrackMode);
        return trackMode;
    }

    private int getRatio(int playId)
    {
        Log.d(TAG, "getRatio");
        int ratio = 0;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetAspectRatio);

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int index = reply.readInt();
            ratio = index;
        }
        request.recycle();
        reply.recycle();
        Log.d(TAG, "getRatio(),index = " + ratio);

        return ratio;
    }

    private int setRatioConversion(int playId,int ratio,int conversion)
    {
        Log.d(TAG, "setRatioConversion,playId = "+playId+",ratio = "+ratio+",conversion = "+conversion);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_SetDispRatio);
        request.writeInt(ratio);
        request.writeInt(conversion);
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        Log.d(TAG, "setRatioConversion(),ret = " + ret);
        return getReturnValue(ret);
    }

    private int setFastChangeChannel(long PreChannelId, long NextChannelId)//Scoty 20180816 add fast change channel
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_PreStart);
        request.writeInt((int) PreChannelId);
        request.writeInt((int) NextChannelId);
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        Log.d(TAG, "setFastChangeChannel(),ret = " + ret);
        return getReturnValue(ret);
    }

    private int setAudioOutputMode(int playerID, int mode)
    {
        Log.d(TAG, "setAudioOutputMode(" + mode + ")");
        return excuteCommand(CMD_AV_SetAudioOuputMode, mode);
    }

    // device\hisilicon\bigfish\hidolphin\component\dtvappfrm\java\com\hisilicon\dtv\play\EnStopType.java
    private int setStopMode(int playerID, /*EnStopType*/int enStopMode)
    {
        Log.d(TAG, "setStopMode(" + enStopMode + ")");
        return excuteCommand(CMD_AV_SetStopMode, /*enStopMode.getValue()*/enStopMode);
    }

    private /*EnStopType*/int getStopMode(int playerID)
    {
        Log.d(TAG, "getStopMode()");
        /*EnStopType*/int enStopMode = /*EnStopType.FREEZE*/0;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetStopMode);

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int index = reply.readInt();
            enStopMode = /*EnStopType.valueOf(index)*/index;
        }
        request.recycle();
        reply.recycle();
        Log.d(TAG, "getStopMode(),getStopMode = " + enStopMode);

        return enStopMode;
    }

    public int getFPS(int playerID)
    {
        Log.d(TAG, "getFPS("+ playerID + ")");
        int fps = 0;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetAVStatus);
        request.writeInt(1); // 1 means get play satus HI_SVR_AV_GET_STATUS_INFO

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            fps = reply.readInt(); // fps
        }
        request.recycle();
        reply.recycle();
        return fps;
    }

    public int ewsActionControl(int playerID, boolean bEnable)
    {
        Log.d(TAG, "ewsActionControl(" + playerID + "," + bEnable + ")");

        int enbaleFlag = (bEnable)? 1 : 0;
        return excuteCommand(CMD_AV_EwsActionControl, enbaleFlag);
    }

    private int setWindowRect(int playerID, Rect rect)
    {
        Log.d(TAG, "setWindowRect(" + rect.left + "," + rect.top + "," + rect.right + ","+ rect.bottom + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_SetWindowSize);
        request.writeInt(rect.left);
        request.writeInt(rect.top);
        request.writeInt(rect.width());
        request.writeInt(rect.height());

        invokeex(request, reply);
        int ret = reply.readInt();

        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    private Rect getWindowSize(int playerID)
    {
        Log.d(TAG, "getWindowSize()");
        Rect rect = null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetWindowSize);
        request.writeInt(playerID);

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int x = reply.readInt();
            int y = reply.readInt();
            int width = reply.readInt();
            int height = reply.readInt();

            rect = new Rect(x, y, x + width, y + height);
        }

        request.recycle();
        reply.recycle();

        return rect;
    }

    private int getVideoResolutionHeight(int playerID)
    {
        Log.d(TAG, "getVideoResolutionHeight("+ playerID + ")");
        int height = 0;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetAVStatus);
        request.writeInt(1); // 1 means get play satus HI_SVR_AV_GET_STATUS_INFO

        invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "getVideoResolutionHeightret" + ret);

        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            reply.readInt(); // fps
            reply.readInt(); // width
            height = reply.readInt();
        }
        request.recycle();
        reply.recycle();
        return height;
    }

    private int getVideoResolutionWidth(int playerID)
    {
        Log.d(TAG, "getVideoResolutionWidth("+ playerID + ")");
        int width = 0;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetAVStatus);
        request.writeInt(1); // 1 means get play satus HI_SVR_AV_GET_STATUS_INFO

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            reply.readInt(); // fps
            width = reply.readInt(); // width
        }
        request.recycle();
        reply.recycle();
        return width;
    }

    private int getDolbyInfoStreamType(int playerID)
    {
        Log.d(TAG, "getDolbyInfoStreamType("+ playerID + ")");
        int streamType = 0;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetAVStatus);
        request.writeInt(3); // 3 means get play satus HI_SVR_AV_GET_DOLBYPLUS_STREAM_INFO

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            streamType = reply.readInt();  // streamType
        }
        request.recycle();
        reply.recycle();
        return streamType;
    }

    private int getDolbyInfoAcmod(int playerID)
    {
        Log.d(TAG, "getDolbyInfoAcmod("+ playerID + ")");
        int acmod = 0;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetAVStatus);
        request.writeInt(3); // 3 means get play satus HI_SVR_AV_GET_DOLBYPLUS_STREAM_INFO

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            reply.readInt(); // streamType
            acmod = reply.readInt(); // acmod
        }
        request.recycle();
        reply.recycle();
        return acmod;
    }


    private SubtitleInfo.SubtitleComponent getCurrentSubtitle(int playerID)
    {
        Log.d(TAG, "getCurrentSubtitle()");

        SubtitleInfo.SubtitleComponent subtitleComponent = null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_SUBT_GetList);
        int curIndex = 0;

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int cnt = reply.readInt();
            curIndex = reply.readInt();
            if (cnt > 0)
            {
                for (int i = 0; i < cnt; i++)
                {
                    int type = reply.readInt();
                    String languageCode = null;
                    int langCode0 = reply.readInt();
                    int langCode1 = reply.readInt();
                    int langCode2 = reply.readInt();
                    languageCode = String.format("%c%c%c", langCode0, langCode1, langCode2);

                    subtitleComponent = new SubtitleInfo.SubtitleComponent();
                    subtitleComponent.setLangCode(languageCode);
                    subtitleComponent.setPos(curIndex + 1);

                    switch (type)
                    {
                        case 2:
                            subtitleComponent.setType(/*EnSubtitleType.TELETEXT*/2);
                            break;
                        case 0:
                        default:
                            subtitleComponent.setType(/*EnSubtitleType.SUBTITLE*/0);
                            break;
                    }


                    if (i == curIndex)
                    {
                        return subtitleComponent;
                    }
                    Log.d(TAG, "getSubtitleComponents:languageCode = " + languageCode
                            + " type = " + type);
                }
            }
        }
        request.recycle();
        reply.recycle();

        return subtitleComponent;

    }

    private SubtitleInfo.SubtitleComponent getSubtOffItem()
    {
        final String OFF_STATUS = "off";

        SubtitleInfo.SubtitleComponent subtOffItem = new SubtitleInfo.SubtitleComponent();
        subtOffItem.setLangCode(OFF_STATUS);
        subtOffItem.setPid(0);
        subtOffItem.setType(/*EnSubtitleType.SUBTITLE*/0);
//        subtOffItem.setSubtComponentType(EnSubtComponentType.NORMAL);
        subtOffItem.setPos(0);

        return subtOffItem;
    }

    private SubtitleInfo getSubtitleComponents(int playID)
    {
        SubtitleInfo subtitleInfo = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_SUBT_GetList);

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int cnt = reply.readInt();
            reply.readInt();
            if (cnt > 0)
            {
                subtitleInfo = new SubtitleInfo();

                /*add off item*/
                subtitleInfo.Component.add(getSubtOffItem());

                /*add subt item*/
                for (int i = 0; i < cnt; i++)
                {
                    int type = reply.readInt();
                    String languageCode = null;
                    int langCode0 = reply.readInt();
                    int langCode1 = reply.readInt();
                    int langCode2 = reply.readInt();
                    languageCode = String.format("%c%c%c", langCode0, langCode1, langCode2);

                    SubtitleInfo.SubtitleComponent subtComponent = new SubtitleInfo.SubtitleComponent();
                    subtComponent.setLangCode(languageCode);
                    subtComponent.setPos(i+1);

                    switch (type)
                    {
                        case 2:
                            subtComponent.setType(/*EnSubtitleType.TELETEXT*/2);
                            break;
                        case 0:
                        default:
                            subtComponent.setType(/*EnSubtitleType.SUBTITLE*/0);
                            break;
                    }


                    subtitleInfo.Component.add(subtComponent);
                    Log.d(TAG, "getSubtitleComponents:languageCode = " + languageCode
                            + " type = " + type);
                }

                SubtitleInfo.SubtitleComponent currentSubtitle = getCurrentSubtitle(playID);    // Johnny add 20180214 to set curPos of SubInfo
                if (currentSubtitle != null)
                {
                    subtitleInfo.setCurPos(currentSubtitle.getPos());
                }
                else
                {
                    subtitleInfo.setCurPos(0);
                }
            }
        }
        request.recycle();
        reply.recycle();
        return subtitleInfo;
    }

    private int selectSubtitle(int playerID, SubtitleInfo.SubtitleComponent subtitleComponent)
    {
        if(null == subtitleComponent)
        {
            Log.e(TAG,"the param of subitleComponent is null");

            return CMD_RETURN_VALUE_FAILAR;
        }

        if (0 == subtitleComponent.getPos())
        {
            return showSubtitle(playerID,false);
        }

        Log.d(TAG, "selectSubtitle(position = " + subtitleComponent.getPos()+ ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_SUBT_Switch);

        request.writeInt(subtitleComponent.getPos()-1);

        invokeex(request, reply);
        int ret = reply.readInt();

        request.recycle();
        reply.recycle();

        Log.d(TAG, "ret = " + ret);
        return getReturnValue(ret);
    }

    private int showSubtitle(int playerID, boolean bShow)
    {
        Log.d(TAG, "showSubtitle(" + bShow + ")");

        int tmpFlag = (bShow)? 1: 0;

        return excuteCommand(CMD_SUBT_SetMode, tmpFlag);
    }

    private boolean isSubtitleVisible(int playerID)
    {
        Log.d(TAG, "isSubtitleVisible()");
        return 1 == excuteCommandGetII(CMD_SUBT_GetMode);
    }

    private int setSubtitleHiStatus(int playerID, boolean bHiOn)
    {
        Log.d(TAG, "setSubtitleHiStatus()");

        int tmpFlag = (bHiOn)? 1: 0;

        return excuteCommand(CMD_SUBT_SetHohPreferred, tmpFlag);
    }

    private int setSubtitleLanguage(int playerID, int index, String lang)
    {
        Log.d(TAG, "setSubtitleLanguage(" + lang + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_SUBT_SetLang);
        request.writeInt(index);
        request.writeString(lang);


        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        Log.d(TAG, "setSubtitleLanguage(" + lang + ")"+", ret="+ret);
        return getReturnValue(ret);
    }
    
    private TeletextInfo getCurrentTeletext(int playID)
    {
        Log.d(TAG, "getCurrentTeletext()");

        TeletextInfo ttxComponent = null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_TTX_GetLangInfo);//get info

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int magazineNum = reply.readInt();
            int pageNum = reply.readInt();
            reply.readInt();//pageSubCode

            ttxComponent = new TeletextInfo();
            ttxComponent.setMagazingNum(magazineNum);
            ttxComponent.setPageNum(pageNum);

            Log.d(TAG, "getCurrentTeletext() magazineNum = "
                    + magazineNum + ",pageNum = " + pageNum);
        }

        request.recycle();
        reply.recycle();

        return ttxComponent;
    }

    private List<TeletextInfo> getTeletectComponents(int playID) {
        Log.d(TAG, "getTeletextList() not used.");
        return null;

        /*Log.d(TAG, "getTeletextList()");

        List<TeletextInfo> ttxList = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetTeletextList);

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret) {
            int cnt = reply.readInt();
            if (cnt > 0) {
                ttxList = new ArrayList<TeletextInfo>();
                for (int i = 0; i < cnt; i++) {
                    String languageCode = reply.readString();
                    int magazineNum = reply.readInt();
                    int pageNum = reply.readInt();

                    TeletextInfo ttxComponent = new TeletextInfo();
                    ttxComponent.setLangCode(languageCode);
                    ttxComponent.setMagazingNum(magazineNum);
                    ttxComponent.setPageNum(pageNum);

                    ttxList.add(ttxComponent);
                    Log.d(TAG, "getTeletextList:languageCode = " + languageCode + "magazineNum = "
                            + magazineNum + "pageNum = " + pageNum);
                }
            }
        }

        request.recycle();
        reply.recycle();
        return ttxList;*/
    }

    private int showTeletext(int playID, boolean bShow)
    {
        Log.d(TAG, "showTeletext(" + bShow + ")");

        int tmpFlag = (bShow)? 1: 0;

        if (bShow == true)
        {
            return excuteCommand(CMD_TTX_Show);
        }
        else
        {
            return excuteCommand(CMD_TTX_Hide);
        }
    }

    private boolean isTeletextVisible(int playID)
    {
        Log.d(TAG, "isTeletextVisible()");
        return 1 == excuteCommandGetII(CMD_TTX_IsShow);
    }

    private boolean isTeletextAvailable(int playID)
    {
        Log.d(TAG, "isTeletextAvailable()");
        return 1 == excuteCommandGetII(CMD_TTX_IsAvailable);
    }

    private int setTtxLang(int playID, String primaryTTXLang)
    {
        Log.d(TAG, "setTtxLang(" + primaryTTXLang + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_TTX_SetLanguage);

        request.writeString(primaryTTXLang);

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS != ret)
        {
            Log.e(TAG, "SetSubtitleLanguage fail, ret = " + ret);
        }

        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    public String getTtxLang(int playID)//eric lin 20180705 get ttx lang
    {
        String ttxLang ="";
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_TTX_GetLanguage);
        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            ttxLang = reply.readString();
        }
        request.recycle();
        reply.recycle();
        return ttxLang;
    }

    // device\hisilicon\bigfish\hidolphin\component\dtvappfrm\java\com\hisilicon\dtv\play\EnCMDCode.java
    private int setCommand(int playID, /*EnCMDCode*/int code)
    {
        return excuteCommand(CMD_TTX_SetCommand, /*code.getValue()*/code);
    }

    //eric lin a8, need fix
    private Date getTimeShitBeginTime(int playerID){
        return null;
    }
    /*private Date getTimeShitBeginTime(int playerID)
    {
        Log.d(TAG, "getTimeShitBeginTime()");
        Date retDate = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetTimeShiftBeginTime);
        request.writeInt(playerID);
        invokeex(request, reply);
        int ret =  reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS ==ret)
        {
            Calendar ca = Calendar.getInstance();
            int dst = ca.get(Calendar.DST_OFFSET);
            int zone = ca.get(Calendar.ZONE_OFFSET);

            int beginTime = reply.readInt();
            int zoneSecond = this.getDtvTimeZone();
            beginTime += zoneSecond - dst/1000;

            if(this.getDtvTimeZone() != 0)
            {
                beginTime -= zone/1000;
            }

            retDate = this.secondToDate(beginTime);
        }

        request.recycle();
        reply.recycle();

        return retDate;
    }*/

    //eric lin a8, need fix
    private Date getTimeShiftPlayTime(int playerID){
        return null;
    }
    /*private Date getTimeShiftPlayTime(int playerID)
    {
        Log.d(TAG, "getTimeShitBeginTime()");
        Date retDate = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetTimeShiftPlayTime);
        request.writeInt(playerID);
        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int playTime = reply.readInt();
            int zoneSecond = this.getDtvTimeZone();

            Calendar ca = Calendar.getInstance();
            int dst = ca.get(Calendar.DST_OFFSET);
            int zone = ca.get(Calendar.ZONE_OFFSET);
            playTime += zoneSecond - dst/1000;

            if(this.getDtvTimeZone() != 0)
            {
                playTime -= zone/1000;
            }
            retDate = this.secondToDate(playTime);
        }

        request.recycle();
        reply.recycle();

        return retDate;
    }*/


    public TimeZone timeConvertTimeZone(int zoneSecond)
    {
        int zoneMinutes = zoneSecond / 60;
        TimeZone timeZone = null;

        String strZone = "GMT";
        if (zoneMinutes > 0)
        {
            strZone += "+";
            strZone += String.format("%02d:%02d", zoneMinutes / 60, zoneMinutes % 60);
        }
        else if (zoneMinutes < 0)
        {
            strZone += "-";
            strZone += String.format("%02d:%02d", (0 - zoneMinutes) / 60, (0 - zoneMinutes) % 60);
        }

        timeZone = TimeZone.getTimeZone(strZone);
        return timeZone;
    }

    public int getDtvTimeZone()
    {
        //Log.d(TAG, "getDtvTimeZone");
        return excuteCommandGetII(CMD_TMCTL_GetTimeZone);
    }

    public int setDtvTimeZone(int zonesecond)
    {
        //Log.d(TAG, "setDtvTimeZone");
        return excuteCommandGetII(CMD_TMCTL_SetTimeZone, zonesecond);
    }

    public int getDtvDaylight()//value: 0(off) or 1(on)
    {
        //Log.d(TAG, "timeGetDtvDaylight");
        return excuteCommandGetII(CMD_TMCTL_GetDaylightSaving);
    }

    public int setDtvDaylight(int onoff)//value: 0(off) or 1(on)
    {
        Log.d(TAG, "setDtvDaylight()");
        return excuteCommand(CMD_TMCTL_SetDaylightSaving, onoff);
    }

    public int getSettingTDTStatus()
    {
        //Log.d(TAG, "getSettingTDTStatus");
        return excuteCommand(CMD_TMCTL_GetSettingTDTStatus);
    }

    public int setSettingTDTStatus(int onoff)//value: 0(off) or 1(on)
    {
        Log.d(TAG, "setSettingTDTStatus()");
        return excuteCommand(CMD_TMCTL_SetSettingTDTStatus, onoff);
    }

    public int setTimeToSystem(boolean bSetTimeToSystem)
    {
        //Log.d(TAG, "setTimeToSystem bEnable=" + bEnable);
        return excuteCommand(CMD_TMCTL_SetTimeToSystem, bSetTimeToSystem ? 1 : 0);
    }

    public Date secondToDate(int isecond)
    {
        int offset = 0;
        Date getDate = null;
        Calendar ca = Calendar.getInstance();

        if(ADD_SYSTEM_OFFSET) { // connie 20181106 for not add system offset
            int dst = ca.get(Calendar.DST_OFFSET);
            int zone = ca.get(Calendar.ZONE_OFFSET);

            offset = zone / 1000 + dst / 1000;
        }

        isecond += offset;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_TMCTL_SecondToDateTime);
        request.writeInt(isecond);
//    request.writeInt(1);
        invokeex(request, reply);
        if (CMD_RETURN_VALUE_SUCCESS == reply.readInt())
        {
            int year = 0;
            int month = 0;
            int day = 0;
            int hour = 0;
            int min = 0;
            int second = 0;
            year = reply.readInt();
            month = reply.readInt();
            day = reply.readInt();
            hour = reply.readInt();
            min = reply.readInt();
            second = reply.readInt();
            reply.readInt();
            reply.readInt();
            getDate = new Date(year - 1900,month - 1,day,hour,min,second);
        }
        request.recycle();
        reply.recycle();
        return getDate;
    }

    public int dateToSecond(Date date)
    {
        int second = 0;
        if(date == null)
        {
            return 0;
        }

        Calendar ca = Calendar.getInstance();
        int dst = ca.get(Calendar.DST_OFFSET);
        int zone = ca.get(Calendar.ZONE_OFFSET);
        ca.setTime(date);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_TMCTL_DateTimeToSecond);

        /*int year = date.getYear();
        int month = date.getMonth();
        int mday = date.getDate();
        int hour = date.getHours();
        int min = date.getMinutes();
        int isecond = date.getSeconds();*/

        // Johnny 20180223 test to replace outdated date.get...
        int year = ca.get(Calendar.YEAR);
        int month = ca.get(Calendar.MONTH) + 1; // calendar month = 0~11
        int mday = ca.get(Calendar.DATE);
        int hour = ca.get(Calendar.HOUR_OF_DAY);
        int min = ca.get(Calendar.MINUTE);
        int isecond = ca.get(Calendar.SECOND);

        request.writeInt(/*year + 1900*/year);
        request.writeInt(/*month + 1*/month);
        request.writeInt(mday);
        request.writeInt(hour);
        request.writeInt(min);
        request.writeInt(isecond);
        request.writeInt(0);
        request.writeInt(0);
//    request.writeInt(1);
        invokeex(request, reply);
        if (CMD_RETURN_VALUE_SUCCESS == reply.readInt())
        {
            second = reply.readInt();

            if(ADD_SYSTEM_OFFSET) { // connie 20181106 for not add system offset
                int offset = zone / 1000 + dst / 1000;
                second -= offset;
            }
        }
        request.recycle();
        reply.recycle();
        return second;
    }

    //eric lin a8, need fix
    private int getTimeShiftRecordTime(int playerID){
        return 0;
    }
    /*private int getTimeShiftRecordTime(int playerID)
    {
        Log.d(TAG, "getTimeShiftRecordTime()");
        int recTime = CMD_RETURN_VALUE_FAILAR;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetTimeShiftRecordTime);
        request.writeInt(playerID);
        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            recTime = reply.readInt();
        }
        request.recycle();
        reply.recycle();

        Log.d(TAG, "getTimeShiftRecordTime(): recTime= " + recTime);

        return recTime;
    }*/

    //eric lin a8, need fix
    private /*EnTrickMode*/int getCurrentTrickMode(int playerID){
        return 0;
    }
    /*private *//*EnTrickMode*//*int getCurrentTrickMode(int playerID)
    {
        Log.d(TAG, "getCurrentTrickMode()");
        *//*EnTrickMode*//*int enTrickMode = *//*EnTrickMode.INVALID_TRICK_MODE*//*0;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetTrickMode);
        request.writeInt(playerID);
        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int index = reply.readInt();
            enTrickMode = *//*EnTrickMode.valueOf(index)*//*index;
        }
        request.recycle();
        reply.recycle();
        Log.d(TAG, "getCurrentTrickMode(),enTrickMode = " + enTrickMode);

        return enTrickMode;
    }*/

    //eric lin a8, need fix
    private int startTrickPlay(int playerID, /*EnTrickMode*/int mode){
        return 0;
    }
    /*private int startTrickPlay(int playerID, *//*EnTrickMode*//*int mode)
    {
        Log.d(TAG, "startTrickPlay(" + mode + ") value = " + *//*mode.getValue()*//*mode);
        return excuteCommand(CMD_AV_TrickPlay, playerID, *//*mode.getValue()*//*mode);
    }*/

    //eric lin a8, need fix
    private int pausePlay(int playerID){
        return 0;
    }
    /*private int pausePlay(int playerID)
    {
        Log.d(TAG, "pausePlay");
        return excuteCommand(CMD_AV_PausePlay, playerID);
    }*/

    //eric lin a8, need fix
    private int play(int playerID)
    {
        return 0;
    }
    /*private int play(int playerID)
    {
        return excuteCommand(CMD_AV_Play, playerID);
    }*/

    //eric lin a8, need fix
    private int seekPlay(int playerID, long seekTime){
        return 0;
    }
    /*private int seekPlay(int playerID, long seekTime)
    {
        Log.d(TAG, "seekPlay(" + seekTime + ")");
        return excuteCommand(CMD_AV_SeekPlay, playerID, (int) seekTime);
    }*/

    //eric lin a8, need fix
    private int stopTimeShift(int playerID){
        return 0;
    }
    /*private int stopTimeShift(int playerID)
    {
        Log.d(TAG, "stopTimeShift");
        return excuteCommand(CMD_AV_StopTimeShift, playerID);
    }*/

    private int usbUpdate(String filename)
    {
        Log.d(TAG, "usbUpdate()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_SSU_PESI_USB);
        request.writeString(filename);//(DDN82-3796.bin)

        invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);
        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    public int fileSystemUpdate(String pathAndFileName,String partitionName)
    {
        Log.d(TAG, "fileSystemUpdate()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_SSU_PESI_FS);
        request.writeString(pathAndFileName);//(DDN82-3796.bin)
        request.writeString(partitionName);//(userdata)

        invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);
        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    public int DVBCOTAUpdate(int tpId, int freq, int symbol, int qam)
    {
        Log.d(TAG, "OTADVBCUpdate()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_SSU_PESI_OTA_DVB_C);
        request.writeInt(tpId);
        request.writeInt(freq*1000);//Scoty 20180614 modify ota update set freq/symbol/bandwith *1000
        request.writeInt(symbol*1000);///Scoty 20180614 modify ota update set freq/symbol/bandwith *1000
        request.writeInt(qam);

        invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);
        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    public int DVBTOTAUpdate(int tpId, int freq, int bandwith, int qam, int priority)
    {
        Log.d(TAG, "DVBTOTAUpdate()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_SSU_PESI_OTA_DVB_T);
        request.writeInt(tpId);
        request.writeInt(freq*1000);//Scoty 20180614 modify ota update set freq/symbol/bandwith *1000
        request.writeInt(bandwith*1000);//Scoty 20180614 modify ota update set freq/symbol/bandwith *1000
        request.writeInt(qam);
        request.writeInt(priority);

        invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);
        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    public int DVBT2OTAUpdate(int tpId, int freq, int bandwith, int qam, int channelmode)
    {
        Log.d(TAG, "DVBTOTAUpdate()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_SSU_PESI_OTA_DVB_T);
        request.writeInt(tpId);
        request.writeInt(freq*1000);//Scoty 20180614 modify ota update set freq/symbol/bandwith *1000
        request.writeInt(bandwith*1000);//Scoty 20180614 modify ota update set freq/symbol/bandwith *1000
        request.writeInt(qam);
        request.writeInt(channelmode);

        invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);
        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    public int ISDBTOTAUpdate(int tpId, int freq, int bandwith, int qam, int priority)
    {
        Log.d(TAG, "ISDBTOTAUpdate()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_SSU_PESI_OTA_ISDBT);
        request.writeInt(tpId);
        request.writeInt(freq*1000);//Scoty 20180614 modify ota update set freq/symbol/bandwith *1000
        request.writeInt(bandwith*1000);//Scoty 20180614 modify ota update set freq/symbol/bandwith *1000
        request.writeInt(qam);
        request.writeInt(priority);

        invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);
        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    public int MtestOTAUpdate()//Scoty 20190410 add Mtest Trigger OTA command
    {
        Log.d(TAG, "MtestOTAUpdate()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_SSU_PESI_MTEST_OTA);

        invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);
        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    public int MtestEnableOpt(boolean enable)
    {
        Log.d(TAG, "MtestEnableOpt: " + enable);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_SSU_PESI_MTEST_ENABLE_OPT);
        request.writeInt(enable ? 1 : 0);

        invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);
        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    public OTACableParameters DVBGetOTACableParas()
    {
        int result = -1;
        Log.d(TAG, "DVBGetOTACableParas");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_SSU_PESI_GET_OTA_PARAMES);
        request.writeInt(1); //cable
        invokeex(request, reply);
        result = reply.readInt();
        OTACableParameters ota = new OTACableParameters();
        if(result == 0){
            ota.pid = reply.readInt();
            ota.frequency = reply.readInt()/1000; //Mhz 100-900
            ota.symbolRate = reply.readInt()/1000;
            ota.modulation = reply.readInt();
        }
        else {
            ota.pid = 0;
            ota.frequency = 0; //Mhz 100-900
            ota.symbolRate = 0;
            ota.modulation = 0;
        }
        request.recycle();
        reply.recycle();
        return ota;
    }

    public OTATerrParameters DVBGetOTAIsdbtParas()
    {
        int result = -1;
        Log.d(TAG, "DVBGetOTAIsdbtParas");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_SSU_PESI_GET_OTA_PARAMES);
        request.writeInt(16); //isdbt
        invokeex(request, reply);
        result = reply.readInt();
        Log.d(TAG, "DVBGetOTAIsdbtParas:    result = " + result);
        OTATerrParameters ota = new OTATerrParameters();
        if(result == 0) {
            ota.pid = reply.readInt();
            ota.frequency = reply.readInt()/1000; //Mhz 100-900
            ota.bandWidth = reply.readInt()/1000; //Mhz 6-9
            ota.enDVBTPrio = 0;
            ota.modulation = 0;
            ota.enChannelMode = 0;
            Log.d(TAG, "DVBGetOTAIsdbtParas:    pid = " + ota.pid + ", frequency = " +  ota.frequency + ", bandwidth = "+ota.bandWidth);
        }
        else {
            ota.pid = 0;
            ota.frequency = 0; //Mhz 100-900
            ota.bandWidth = 0; //Mhz 6-9
            ota.enDVBTPrio =0;
            ota.modulation = 0;
            ota.enChannelMode = 0;
        }
        request.recycle();
        reply.recycle();
        return ota;
    }


    public OTATerrParameters DVBGetOTATerrestrialParas()
    {
        int result = -1;
        Log.d(TAG, "DVBGetOTArTerrestrialParas");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_SSU_PESI_GET_OTA_PARAMES);
        request.writeInt(4); //Terrestrial
        invokeex(request, reply);
        result = reply.readInt();
        Log.d(TAG, "DVBGetOTATerrestrialParas:    result = " + result);
        OTATerrParameters ota = new OTATerrParameters();
        if(result == 0) {
            ota.pid = reply.readInt();
            ota.frequency = reply.readInt()/1000; //Mhz 100-900
            ota.bandWidth = reply.readInt()/1000; //Mhz 6-9
            ota.enDVBTPrio = reply.readInt();
            ota.modulation = reply.readInt();
            ota.enChannelMode = 0;
            Log.d(TAG, "DVBGetOTAIsdbtParas:    pid = " + ota.pid + ", frequency = " +  ota.frequency + ", bandwidth = "+ ota.bandWidth + ",dvbprio = " +ota.enDVBTPrio + "modulation = " +ota.modulation );
        }
        else {
            ota.pid = 0;
            ota.frequency = 0; //Mhz 100-900
            ota.bandWidth = 0; //Mhz 6-9
            ota.enDVBTPrio =0;
            ota.modulation = 0;
            ota.enChannelMode = 0;
        }
        request.recycle();
        reply.recycle();
        return ota;
    }

    public OTATerrParameters DVBGetOTADVBT2Paras()
    {
        int result = -1;
        Log.d(TAG, "DVBGetOTADVBT2Paras");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_SSU_PESI_GET_OTA_PARAMES);
        request.writeInt(8); //DVB T2
        invokeex(request, reply);
        result = reply.readInt();
        Log.d(TAG, "DVBGetOTATerrestrialParas:    result = " + result);
        OTATerrParameters ota = new OTATerrParameters();
        if(result == 0) {
            ota.pid = reply.readInt();
            ota.frequency = reply.readInt()/1000; //Mhz 100-900
            ota.bandWidth = reply.readInt()/1000; //Mhz 6-9
            ota.enDVBTPrio = 0;
            ota.enChannelMode = reply.readInt();
            ota.modulation = reply.readInt();
            Log.d(TAG, "DVBGetOTADVBT2Paras:    pid = " + ota.pid + ", frequency = " +  ota.frequency + ", bandwidth = "+ota.bandWidth + ", channel mode = " + ota.enChannelMode + ", modulation = "+ota.modulation );
        }
        else {
            ota.pid = 0;
            ota.frequency = 0; //Mhz 100-900
            ota.bandWidth = 0; //Mhz 6-9
            ota.enDVBTPrio =0;
            ota.enChannelMode = 0;
            ota.modulation = 0;
        }
        request.recycle();
        reply.recycle();
        return ota;
    }

    public List<BookInfo> InitUIBookList(){//Init UI Book List after boot
        if(UIBookList == null)
            UIBookList = new ArrayList<BookInfo>();
        UIBookList = BookInfoGetList();

        if(UIBookList == null)
            UIBookList = new ArrayList<BookInfo>();

        return UIBookList;
    }

    public void SetUIBookManager(DTVActivity.BookManager bookmanager)
    {
        UIBookManager = bookmanager;
    }

    public DTVActivity.BookManager GetUIBookManager()
    {
        return UIBookManager;
    }

    public List<BookInfo> GetUIBookList()
    {
        return UIBookList;
    }

    private void clearUIBookManagerList()
    {
        if(UIBookList != null)
            UIBookList.clear();
    }

    public List<BookInfo> BookInfoGetList() {
        return getAllTasks();
    }

    public BookInfo BookInfoGet(int bookId) {
        return getTaskById(bookId);
    }

    public int BookInfoAdd(BookInfo bookInfo) {
        return addTask(bookInfo);
    }

    public int BookInfoUpdate(BookInfo bookInfo) {
        return updateTask(bookInfo);
    }

    public int BookInfoUpdateList(List<BookInfo> bookList) {
        return updateBookList(bookList);
    }

    public int BookInfoDelete(int bookId) {
        return deleteTask(bookId);
    }

    public int BookInfoDeleteAll() {
        return clearAllTasks();
    }

    public BookInfo BookInfoGetComingBook() {
        return getComingTask();
    }

    public List<BookInfo> BookInfoFindConflictBooks(BookInfo bookInfo) {
        return findConflictTasks(bookInfo);
    }

    private List<BookInfo> bookGetTasksByType(int type)
    {
        Log.d(TAG, "bookGetTasksByType() : " + type);
        ArrayList<BookInfo> list = null;
        list = new ArrayList<BookInfo>();

    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
            request.writeInt(CMD_BOOK_GetBookByType);
            request.writeInt(type);//HI_SVR_BOOK_TYPE_REC
            request.writeInt(0);//u32Pos
            request.writeInt(1000);//u32Num

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int num = reply.readInt();
            Log.d(TAG, "num = " + num);
            if (num > 0)
            {
                for (int i = 0; i < num; i++)
                {
                    BookInfo bookInfo = new BookInfo();
                    initialBookData(bookInfo, reply);
                    list.add(bookInfo);
                }
            }
        }
        request.recycle();
        reply.recycle();
        }
        return list;
    }

    private List<BookInfo> getAllTasks()
    {
        Log.d(TAG, "getAllTasks()");
        if(PLATFORM == PLATFORM_HISI) {

            ArrayList<BookInfo> list = null;
            list = new ArrayList<BookInfo>();

            list.addAll(bookGetTasksByType(0));//HI_SVR_BOOK_TYPE_REC
            list.addAll(bookGetTasksByType(1));//HI_SVR_BOOK_TYPE_PLAY
            list.addAll(bookGetTasksByType(2));//HI_SVR_BOOK_TYPE_HINT
            list.addAll(bookGetTasksByType(3));//HI_SVR_BOOK_TYPE_STANDBY
            list.addAll(bookGetTasksByType(4));//HI_SVR_BOOK_TYPE_POWERON

            if (list.size() == 0)
            {
                return null;
            }

            return list;
        }
        else { //pesi
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            request.writeString(DTV_INTERFACE_NAME);
            request.writeInt(CMD_BOOK_GetAllBooks);

            ArrayList<BookInfo> list = null;
            invokeex(request, reply);
            int ret = reply.readInt();
            if (CMD_RETURN_VALUE_SUCCESS == ret) {
                int num = reply.readInt();
                Log.d(TAG, "num = " + num);
                if (num > 0) {
                    list = new ArrayList<BookInfo>();
                    for (int i = 0; i < num; i++) {
                        BookInfo bookInfo = new BookInfo();
                        initialBookData(bookInfo, reply);
                        list.add(bookInfo);
                    }
                }
            }
            request.recycle();
            reply.recycle();

            return list;
        }

    }

    public BookInfo GetTaskByIdFromUIBookList(int id)
    {
        for(int i = 0 ; i < UIBookManager.BookList.size() ; i++) {
            if (UIBookManager.BookList.get(i).getBookId() == id)
            {
                return UIBookManager.BookList.get(i);
            }
        }
        return null;
    }
    
    private BookInfo getTaskById(int id)
    {
        Log.d(TAG, "getTaskById(" + id + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_BOOK_GetBookByRowID);
        request.writeInt(id);

        BookInfo bookInfo = null;
        invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            bookInfo = new BookInfo();
            initialBookData(bookInfo, reply);
        }
        request.recycle();
        reply.recycle();
        return bookInfo;
    }

    private int addTask(BookInfo bookInfo)
    {
        Log.d(TAG, "addTask(" + bookInfo.getBookId() + ")");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_BOOK_AddBook);

        writeBookData(request, bookInfo);

        invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            ret = reply.readInt();
        }

        Log.d(TAG, "ret = " + ret);
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    private int updateTask(BookInfo bookInfo)
    {
        Log.d(TAG, "updateTask(" + bookInfo.getBookId() + ")");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_BOOK_UpdateBook);

        writeBookData(request, bookInfo);

        invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret =" + ret);
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    private void initialBookData(BookInfo bookInfo, Parcel reply)
    {
        int id = reply.readInt();
        bookInfo.setBookId(id);
        Log.d(TAG, "initialBookData:  id =" + id);

        long channelId = getUnsignedInt(reply.readInt());
        bookInfo.setChannelId(channelId);
        Log.d(TAG, "initialBookData: channelId = " + channelId);

        reply.readInt();// status

        reply.readInt();// progType

        int type = reply.readInt();
        bookInfo.setBookType(/*EnTaskType.valueOf(type)*/type);
        Log.d(TAG, "initialBookData: type = " + /*EnTaskType.valueOf(type)*/type);

        int cycle = reply.readInt();
        bookInfo.setBookCycle(/*EnTaskCycle.valueOf(cycle)*/cycle);
        Log.d(TAG, "initialBookData:cycle = " + /*EnTaskCycle.valueOf(cycle)*/cycle);
        reply.readInt();// weektype

        int eventId = reply.readInt();
//        bookInfo.setEventId(eventId); // change in the future (Book)
        Log.d(TAG, "initialBookData: eventId = " + eventId);

        int startTime = reply.readInt();
        Calendar ca = Calendar.getInstance();
        int dst = ca.get(Calendar.DST_OFFSET);
        //int zone = ca.get(Calendar.ZONE_OFFSET);

        Date startDate;
        if(ADD_SYSTEM_OFFSET) // connie 20181106 for not add system offset
            startDate = this.secondToDate(startTime - dst/1000);
        else
            startDate = this.secondToDate(startTime);
        ca.setTime(startDate);
        int pesiStartTime = ca.get(Calendar.HOUR_OF_DAY)*100 + ca.get(Calendar.MINUTE);

        bookInfo.setStartTime(pesiStartTime);
        bookInfo.setDate(ca.get(Calendar.DATE));
        bookInfo.setMonth(ca.get(Calendar.MONTH)+1);
        bookInfo.setYear(ca.get(Calendar.YEAR));
        int weekDay = ca.get(Calendar.DAY_OF_WEEK);
        if(ca.getFirstDayOfWeek() == Calendar.SUNDAY){
            weekDay = weekDay - 1;
            if(weekDay == 0){
                weekDay = 7;
            }
        }
        bookInfo.setWeek(weekDay-1);    // weekDay from java = 1~7, trans to 0~6
        Log.d(TAG, "initialBookData: startDate = " + startDate.toString());
        Log.d(TAG, "initialBookData: startTime = " + startTime);

        int durations = reply.readInt();
        bookInfo.setDuration(/*durations*/durations/3600*100 + durations%3600/60);
        Log.d(TAG, "initialBookData:durations = " + durations);

        String name = reply.readString();
        bookInfo.setEventName(name);
        Log.d(TAG, "initialBookData:name = " + name);

        reply.readInt();//Userdata
        //bookInfo.setGroupType();  // change in the future (Book)

        //task.setEnable(true);
        bookInfo.setEnable(1);  // true = 1
        if(PLATFORM == PLATFORM_PESI) {
            int groupType = reply.readInt();
            bookInfo.setGroupType(groupType);
            Log.d(TAG, "initialBookData:  groupType = " + groupType);
        }
    }

    private void writeBookData(Parcel request, BookInfo bookInfo)
    {
        request.writeInt(bookInfo.getBookId());
        Log.d(TAG, "writeBookData: bookInfo.getId() = " + bookInfo.getBookId());
        request.writeInt((int) bookInfo.getChannelId());
        Log.d(TAG, "writeBookData: bookInfo.getChannelId() = " + bookInfo.getChannelId());

        request.writeInt(0); // status

        //need fix
        /*if (bookInfo.getBookType() == EnTaskType.STANDBY || bookInfo.getBookType() == EnTaskType.POWERON
                || bookInfo.getBookType() == EnTaskType.HINT)
        {
            request.writeInt(3); // HI_SVR_BOOK_PROG_NONE
        }
        else*/
        {
            request.writeInt(0); // HI_SVR_BOOK_PROG_NORMAL
        }

        request.writeInt(/*task.getType().ordinal()*/bookInfo.getBookType());
        Log.v(TAG, "writeBookData: bookInfo.getType() = " + /*task.getType().ordinal()*/bookInfo.getBookType());

        request.writeInt(/*task.getCycle().ordinal()*/bookInfo.getBookCycle());
        Log.v(TAG, "writeBookData: bookInfo.getCycle() = " + /*task.getCycle().ordinal()*/bookInfo.getBookCycle());

        String strDate = String.format(Locale.getDefault(),
                "%d/%02d/%02d %02d:%02d:%02d",
                bookInfo.getYear(), bookInfo.getMonth(), bookInfo.getDate(), bookInfo.getStartTime()/100, bookInfo.getStartTime()%100, 0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        try {
            date = sdf.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

//        date.setTime(date.getTime() - 7200*1000); // uncomment if test book msg in hisi service

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int weekday = cal.get(Calendar.DAY_OF_WEEK);
        request.writeInt(1 << (weekday - 1)); //See HI_SVR_BOOK_WEEK_DAY_E

        request.writeInt(/*bookInfo.getEventId()*/0);   // change in the future (Book)
        Log.v(TAG, "writeBookData: bookInfo.getEventId() = " + /*bookInfo.getEventId()*/0);    // change in the future (Book)

        request.writeInt(this.dateToSecond(date));
        Log.v(TAG, "writeBookData: startTime = " + date);
        Log.v(TAG, "writeBookData: startTimeSecond = " + this.dateToSecond(date));

        int secDuration = /*hour*/bookInfo.getDuration()/100*3600 + /*min*/bookInfo.getDuration()%100*60;
        request.writeInt(secDuration);
        Log.v(TAG, "writeBookData: bookInfo.getDuration() = " + bookInfo.getDuration());
        request.writeString(bookInfo.getEventName());
        Log.v(TAG, "writeBookData: bookInfo.getEventName() = " + bookInfo.getEventName());
        request.writeInt(0);//u32UserData
        if(PLATFORM == PLATFORM_PESI) {
            request.writeInt(bookInfo.getGroupType());
            Log.d(TAG, "writeBookData:  bookInfo.getGroupType() = " + bookInfo.getGroupType());
        }
    }

    private int deleteTask(int bookId)
    {
        Log.d(TAG, "deleteTask(" + bookId + ")");
        return excuteCommand(CMD_BOOK_DeleteBookByRowID, bookId);
    }

    private int clearAllTasks()
    {
        Log.d(TAG, "clearAllTasks()");
        return excuteCommand(CMD_BOOK_ClearAllBooks);
    }

    private BookInfo getComingTask()
    {
        int u32IntervalSecond = Integer.MAX_VALUE;
        Log.d(TAG, "getComingTask()");
        BookInfo bookInfo = null;
        for (int i = 0; i < 5; i++)
        {
            BookInfo tmpbookInfo = null;
            int tmpInterval = Integer.MAX_VALUE;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
            request.writeInt(CMD_BOOK_GetComingBook);

            request.writeInt(i); //HI_SVR_BOOK_TYPE_E
        invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
                tmpInterval = reply.readInt();
                Log.d(TAG, "tmpInterval = " + tmpInterval);
                tmpbookInfo = new BookInfo();
                initialBookData(tmpbookInfo, reply);
                if (tmpInterval < u32IntervalSecond)
                {
                    Log.d(TAG, "u32IntervalSecond = " + u32IntervalSecond);
                    u32IntervalSecond = tmpInterval;
                    bookInfo = tmpbookInfo;
                }
            }
            request.recycle();
            reply.recycle();
        }
        return bookInfo;
    }

    private List<BookInfo> findConflictTasks(BookInfo bookInfo)
    {
        Log.d(TAG, "findConflictTasks()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_BOOK_FindConflictbooks);

        writeBookData(request, bookInfo);

        ArrayList<BookInfo> list = null;
        invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int num = reply.readInt();
            int bookId = 0;
            Log.d(TAG, "num = " + num);
            if (num > 0)
            {
                list = new ArrayList<BookInfo>();
                for (int i = 0; i < num; i++)
                {
                    BookInfo conflictBookInfo = new BookInfo();
                    bookId = reply.readInt();
                    conflictBookInfo = getTaskById(bookId);
                    list.add(conflictBookInfo);
                }
            }
        }

        request.recycle();
        reply.recycle();

        return list;
    }


    public int tunerLock(TVTunerParams tunerParams)
    {
        //Log.d(TAG, "tunerLock:tunerID(" + nTunerID + ")signalType(" + multiplexe.getNetworkType()
        //  + ")freq(" + multiplexe.getFrequency() + ")version(" + multiplexe.getVersion()+ ")");

        Log.d(TAG, "tunerLock: start");
        int antennaType =0;

        //int synConnect = (true == bsynConnect)? 1 : 0;
        int nTunerID = tunerParams.getTunerId(); //0; //mLocalTunerID;
        int nConnectTimeout = 0;
        boolean bMotorUsed = false;
        int nSaltelliteID = tunerParams.getSatId();
        //int fe =0;
        int qam=0;
        int bandwidth=0;
        int version=0;
        int polar = 0;

        if(tunerParams.getFe_type() == TVTunerParams.FE_TYPE_DVBC) {
            Log.d(TAG, "tunerLock: DVBC");
            antennaType = EnNetworkType.CABLE.getValue();
            version = 0; //EnVersionType (Version_1)
            Log.d(TAG, "tunerLock: getQam()=" + tunerParams.getQam());
            qam = getEnModFromPesiQam(tunerParams.getQam());
        }else if (tunerParams.getFe_type() == TVTunerParams.FE_TYPE_DVBT){ //DVBT
            Log.d(TAG, "tunerLock: DVBT");
            antennaType = EnNetworkType.TERRESTRIAL.getValue();
            version = 0; //EnVersionType (Version_1)
            Log.d(TAG, "tunerLock: getBandwith()=" + tunerParams.getBandwith());
            bandwidth = getBandForWrite(tunerParams.getBandwith());
        }else if (tunerParams.getFe_type() == TVTunerParams.FE_TYPE_ISDBT){ //ISDBT
            Log.d(TAG, "tunerLock: ISDBT");
            antennaType = EnNetworkType.ISDB_TER.getValue();
            version = 0; //EnVersionType (Version_1)
            Log.d(TAG, "tunerLock: getBandwith()=" + tunerParams.getBandwith());
            bandwidth = getBandForWrite(tunerParams.getBandwith());
        }else if (tunerParams.getFe_type() == TVTunerParams.FE_TYPE_DVBS){ //DVBS
            Log.d(TAG, "tunerLock: DVBS");
            antennaType = EnNetworkType.SATELLITE.getValue();
            version = 0; //EnVersionType (Version_1)
            Log.d(TAG, "tunerLock: getPolar()=" + tunerParams.getPolar());
            polar = tunerParams.getPolar();
        }
        excuteCommand(CMD_FE_SetAntennaType, antennaType);

        int MotorParamUsed = (bMotorUsed)? 1 : 0;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_FE_SetConnectParam);
        request.writeInt(nTunerID);
        request.writeInt(antennaType);

        if (EnNetworkType.CABLE.getValue() == antennaType) // DVBC
        {
            //DVBCChannelDot multC = (DVBCChannelDot) multiplexe;
            Log.d(TAG, "tunerLock: frequency:"+tunerParams.getFrequency()+", symbolrate:"+tunerParams.getSymbolRate()+", qam:"+qam+", version:"+version);
            request.writeInt(tunerParams.getFrequency());
            request.writeInt(tunerParams.getSymbolRate());
            request.writeInt(qam);
            request.writeInt(version);
        }
        else if (EnNetworkType.TERRESTRIAL.getValue() == antennaType //DVBT
            || EnNetworkType.DTMB.getValue() == antennaType // DTMB
            || EnNetworkType.ISDB_TER.getValue() == antennaType)//ISDBT
        {
            //DVBTChannelDot multT = (DVBTChannelDot) multiplexe;
            Log.d(TAG, "tunerLock: frequency:"+tunerParams.getFrequency()+", bandwidth:"+bandwidth+", qam:"+qam+", version:"+version);
            request.writeInt(/*multT.getFrequency()*/tunerParams.getFrequency());
            request.writeInt(/*multT.getBandWidth()*/bandwidth);
            request.writeInt(/*multT.getModulation().getValue()*/qam);
            request.writeInt(/*multT.getVersion().ordinal()*/version);
        }
        else if (EnNetworkType.SATELLITE.getValue() == antennaType) // DVBS
        {
            //DVBSTransponder tp = (DVBSTransponder) multiplexe;
            Log.d(TAG, "tunerLock: frequency:"+tunerParams.getFrequency()+", SymbolRate:"+tunerParams.getSymbolRate()+", qam:"+qam+", version:"+version + ", polar:" + (polar==1?"H":"V"));
            request.writeInt(/*tp.getFrequency()*/tunerParams.getFrequency());
            request.writeInt(/*tp.getSymbolRate()*/tunerParams.getSymbolRate());
            request.writeInt(/*tp.getPolarity().ordinal()*/polar);
            request.writeInt(/*tp.getVersion().ordinal()*/version);
        }

        Log.d(TAG, "tunerLock: SatID = " + tunerParams.getSatId() + " TpID = " + tunerParams.getTpId());
        request.writeInt(tunerParams.getSatId());   // Johnny 20180814 send satID to service in tunerLock
        request.writeInt(tunerParams.getTpId());    // Johnny 20180814 send tpID to service in tunerLock

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        Log.d(TAG, "tunerLock: ret="+ret);

        return getReturnValue(ret);
    }

    public int getTunerStatus(int tuner_id) {
        int lockStatus=0;
//        Log.d(TAG, "getTunerStatus: tunerID (" + tuner_id + ")");
        lockStatus = excuteCommandGetII(CMD_FE_GetLockStatus, tuner_id);
        //Log.d(TAG, "getTunerStatus: lockStatus="+ lockStatus);
        if(lockStatus == 1)
            return 1;
        else
            return 0;
    }

    public int getSignalStrength(int nTunerID)
    {
        //Log.v(TAG, "getSignalStrength: tunerID (" + nTunerID + ")");
        int signalStrength=0;
        signalStrength = excuteCommandGetII(CMD_FE_GetSignalStrength, nTunerID);
        //Log.d(TAG, "getSignalStrength: signalStrength="+ signalStrength);
        return signalStrength;
    }

    public int getSignalQuality(int nTunerID)
    {
        //Log.v(TAG, "getSignalQuality: tunerID (" + nTunerID + ")");
        int signalQuality=0;
        signalQuality = excuteCommandGetII(CMD_FE_GetSignalQuality, nTunerID);
        //Log.d(TAG, "getSignalQuality: signalQuality="+ signalQuality);
        return signalQuality;
    }

    public int getSignalSNR(int nTunerID)
    {
        Log.d(TAG, "getSignalSNR: tunerID (" + nTunerID + ")");
        int signalSNR=0;
        return CMD_RETURN_VALUE_FAILAR;
        //signalSNR = excuteCommandGetII(CMD_FE_GetSignalSNR, nTunerID);
        //Log.d(TAG, "getSignalSNR: signalSNR="+ signalSNR);
        //return signalSNR;
    }

    public String getSignalBER(int nTunerID)
    {
//        Log.d(TAG, "getSignalBER: tunerID (" + nTunerID + ")");
        String signalBER = "";

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_FE_GetBer);
        request.writeInt(nTunerID);

        invokeex(request, reply);
        if (CMD_RETURN_VALUE_SUCCESS == reply.readInt())
        {
            signalBER = reply.readString();
        }
        request.recycle();
        reply.recycle();
        //Log.d(TAG, "getSignalBER: signalBER="+ signalBER);
        return signalBER;
    }

    public int setFakeTuner(int openFlag)//Scoty 20180809 add fake tuner command
    {
        return excuteCommand(CMD_FE_SetFakeTuner,openFlag);
    }

    public int TunerSetAntenna5V(int tuner_id, int onOff)
    {
        Log.d(TAG, "TunerSetAntenna5V: onOff = " + onOff);
        return excuteCommand(CMD_FE_SetAntennaPower, tuner_id, onOff);
    }

    public int getTunerType()
    {
        return TUNER_TYPE;
    }

    public void setChannelExist(int exist)
    {
        ChannelIsExist = exist;
    }

    public int getChannelExist()
    {
        return ChannelIsExist;
    }

    public int saveTable(EnTableType tableType)
    {
        Log.d(TAG, "saveTable:" + tableType.ordinal());
        return excuteCommand(CMD_PM_SaveTable, tableType.ordinal(), 0);
    }

    public int clearTable(EnTableType tableType)
    {
        Log.d(TAG, "clearTable : " + tableType.ordinal());
        return excuteCommand(CMD_PM_ClearTable, tableType.ordinal());
    }

    public int restoreTable(EnTableType tableType)
    {
        Log.d(TAG, "restoreTable :" + tableType.ordinal());
        return excuteCommand(CMD_PM_RestoreTable, tableType.ordinal());
    }

    public int saveNetworks()
    {
        return saveTable(EnTableType.ALL);
    }

    public int getDefaultOpenGroupTypeForHisi(long channelId)
    {
        ProgramInfo program = getProgramByID(channelId);
        if(program != null)
            return program.getType();
        else
            return 0;
    }

    public int getDefaultOpenGroupType()
    {
        int ret = excuteCommandGetII(CMD_PM_GetDefaultOpenGroupType);
        Log.d(TAG, "getDefaultOpenGroupType ret = " + ret);

        if (ret == -1)
        {
            return 1;
        }
        else
        {
            if(PLATFORM == PLATFORM_HISI) {
                if (ret == EnTVRadioFilter.TV.getValue())
                    ret = ProgramInfo.ALL_TV_TYPE;
                else if (ret == EnTVRadioFilter.RADIO.getValue())
                    ret = ProgramInfo.ALL_RADIO_TYPE;
                else
                    ret = ProgramInfo.ALL_TV_TYPE;
            }
            return getReturnValue(ret);
        }
    }

    public int getChannelNumInGroup(int groupType)
    {
        Log.d(TAG, "getChannelNumInGroup groupType :" + groupType);

        int ret = CMD_RETURN_VALUE_FAILAR;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetChannelNum);
        request.writeInt(Conv2ServerType(groupType));
        request.writeInt(Conv2ServerPos(groupType));

        invokeex(request, reply);
        if (CMD_RETURN_VALUE_SUCCESS == reply.readInt())
        {
            ret = reply.readInt();
            Log.d(TAG, "getChannelNumInGroup groupType :" + groupType+ "num : " + ret);
        }
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int getChannelCount(int groupType)
    {
        final int ATV_GROUP = 372;
        int channelCount = getChannelNumInGroup(/*mGroupType*/groupType);
        Log.v(TAG, "getTestChannelCount() = " + channelCount);
        return channelCount;

        /*
        if (ATV_GROUP == mGroupType)
        {
            List<AtvChannelNode> atvNodeList = getAtvChannelNodeList();
            if (null == atvNodeList)
            {
                return 0;
    }
            else
            {
                return atvNodeList.size();
            }
        }
        else
        {
            return mHiDtvMediaPlayer.getChannelNumInGroup(mGroupType);
        }
        */
    }

    public List<Integer> getUseGroups(EnUseGroupType useGroupType)
    {
        Log.d(TAG, "getUseGroups = " + useGroupType);

        List<Integer> groupTypes = new ArrayList<Integer>();
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetUseGroups);
        request.writeInt(useGroupType.ordinal());
        invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "getUseGroups ret= " + ret);
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int groupCount = reply.readInt();
            Log.d(TAG, "getUseGroups Count= " + groupCount);
            for (int i = 0; i < groupCount; i++)
            {
                int type = reply.readInt();
                groupTypes.add(type);
            }
        }

        request.recycle();
        reply.recycle();
        return groupTypes;
    }

    public int rebuildAllGroup()
    {
        Log.v(TAG, "rebuildAllGroup");
        return excuteCommand(CMD_PM_RebuildAllGroup);
    }

    public String getChannelGroupName(int groupType)
    {
        Log.d(TAG, "getChannelGroupName groupType = " + groupType);
        String groupName ="";
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetChannelGroupName);
        request.writeInt(groupType);
        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            groupName = reply.readString();
        }
        request.recycle();
        reply.recycle();
        return groupName;
    }

    public int setChannelGroupName(int groupType, String name)
    {
        Log.d(TAG, "setChannelGroupName groupType = " + groupType + "name = " + name);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_SetChannelGroupName);
        request.writeInt(groupType);
        request.writeString(name);
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int delChannelByTag(int u32ProgTag)
    {
        Log.d(TAG, "delChannelByTag");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_DelChannelByTag);
        request.writeInt(u32ProgTag);

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        if (ret != CMD_RETURN_VALUE_SUCCESS)
        {
            return CMD_RETURN_VALUE_FAILAR;
    }

        return getReturnValue(ret);
    }

    public int prepareDTV()
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_COMM_PrepareDTV);
        Log.d(TAG, "prepareDTV PLATFORM_PESI");
        request.writeInt(1);
//        Log.d(TAG, "prepareDTV PLATFORM_HISI");
//        request.writeInt(0);
        invokeex(request, reply);
        int ret = reply.readInt();
        if(ret == CMD_RETURN_VALUE_SUCCESS) {
            PLATFORM = reply.readInt();
            Log.d(TAG, "prepareDTV PLATFORM = "+PLATFORM);
            int tunerNum = reply.readInt();
            TUNER_NUM = tunerNum;//Scoty 20181113 add GetTunerNum function
            Log.d(TAG, "prepareDTV tunerNum = "+tunerNum);
            for(int i = 0; i < tunerNum; i++) {
                TUNER_TYPE = reply.readInt();
                Log.d(TAG, "prepareDTV TUNER_TYPE = "+TUNER_TYPE);
                break;
            }
        }
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }
    
    public List<ProgramInfo> GetProgramInfoList(int type,int pos,int num) {
        List<ProgramInfo> programList = null;
        ProgramInfo tmp = null;
        int chCount;

        int defaultOpenGroupType = getDefaultOpenGroupType();
        if(PLATFORM == PLATFORM_HISI) {
            if (defaultOpenGroupType == ProgramInfo.ALL_TV_TYPE)
                defaultOpenGroupType = EnTVRadioFilter.TV.getValue();
            else if (defaultOpenGroupType == ProgramInfo.ALL_RADIO_TYPE)
                defaultOpenGroupType = EnTVRadioFilter.RADIO.getValue();
            else
                defaultOpenGroupType = EnTVRadioFilter.TV.getValue();
        }
        if(type == ProgramInfo.ALL_TV_TYPE) {
            setServiceMode(EnTVRadioFilter.TV.ordinal());
        }else if(type == ProgramInfo.ALL_RADIO_TYPE){
            setServiceMode(EnTVRadioFilter.RADIO.ordinal());
        }

        if(pos == MiscDefine.ProgramInfo.POS_ALL && num == MiscDefine.ProgramInfo.NUM_ALL){
            //Log.d(TAG, "defaultOpenGroupType: "+defaultOpenGroupType);
            //chCount = getChannelCount(defaultOpenGroupType);
            chCount = getChannelCount(type);
            if(chCount > 0) {
                //programList = getProgramList(defaultOpenGroupType, 0, chCount);
                programList = getProgramList(type, 0, chCount);
                if(programList != null && !programList.isEmpty() ) {
                    return programList;
                }
            }
            Log.d(TAG, "GetProgramInfoList: return null");
        }
        return null;
    }

    public int setServiceMode(int mode)
    {
        Log.d(TAG, "setServiceMode = " + mode);
        return excuteCommand(CMD_PM_SetServiceMode, mode);
    }
    public List<SimpleChannel> GetSimpleProgramListForHisi(int type,int IncludeSkipFlag,int IncludePVRSkipFlag) {
        List<SimpleChannel> ChnannelList = new ArrayList<>();
        List<ProgramInfo> allProgramInfo=null;

        allProgramInfo = GetProgramInfoList(type, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.NUM_ALL);
        if(allProgramInfo == null) {
            Log.d(TAG, "GetSimpleProgramList: ===>>> IS NULL");
            return null;
        }
        else
            Log.d(TAG, "GetSimpleProgramList: ===>>> NOT NULL");
        if(allProgramInfo != null) {
            for (int i = 0; i < allProgramInfo.size(); i++) {
                if (allProgramInfo.get(i).getType() == type) {
                    SimpleChannel channel= new SimpleChannel();
                    channel.setChannelId(allProgramInfo.get(i).getChannelId());
                    channel.setChannelName(allProgramInfo.get(i).getDisplayName());
                    channel.setChannelNum(allProgramInfo.get(i).getDisplayNum());
                    channel.setUserLock(allProgramInfo.get(i).getLock());
                    channel.setCA(allProgramInfo.get(i).getCA());
                    channel.setPVRSkip(0);
                    ChnannelList.add(channel);
                }
            }
            return ChnannelList;
        }
        else{
            Log.d(TAG, "allProgramInfo is null");
            return null;
        }
    }

    //eric lin a8, need fix
    //public List<SimpleChannel> GetSimpleProgramListForPesi(int type,int IncludeSkipFlag,int IncludePVRSkipFlag) {
    //    return null;
    //}
    //Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
    public List<SimpleChannel> GetSimpleProgramListForPesifromTotalChannelList(int type,int IncludeSkipFlag,int IncludePVRSkipFlag){
        List<SimpleChannel> simpleList = new ArrayList<SimpleChannel>();

        for(int i = 0 ; i < TotalChannelList.get(type).size() ; i++)
        {
            if(IncludeSkipFlag == 1 && IncludePVRSkipFlag == 1)
                simpleList.add(TotalChannelList.get(type).get(i));
            else if(IncludeSkipFlag == 0 && IncludePVRSkipFlag == 1)
            {
                if(TotalChannelList.get(type).get(i).getChannelSkip() != 1)
                    simpleList.add(TotalChannelList.get(type).get(i));
            }
            else if(IncludeSkipFlag == 1 && IncludePVRSkipFlag == 0)
            {
                if(TotalChannelList.get(type).get(i).getPVRSkip() != 1)
                    simpleList.add(TotalChannelList.get(type).get(i));
            }else{
                if(TotalChannelList.get(type).get(i).getPVRSkip() != 1)
                    simpleList.add(TotalChannelList.get(type).get(i));
                else if(TotalChannelList.get(type).get(i).getChannelSkip() != 1)
                    simpleList.add(TotalChannelList.get(type).get(i));
            }

        }

        return simpleList;
    }

    public List<SimpleChannel> GetSimpleProgramListForPesi(int type,int IncludeSkipFlag,int IncludePVRSkipFlag) {
        List<SimpleChannel> ChannelList = null;
        int ret = -1;
        Log.d(TAG, "ProgramInfoUpdate some param - only AudioSelected,AudioLRSelected.");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetSimpleChannelList);
        request.writeInt(type);
        request.writeInt(IncludeSkipFlag);
        request.writeInt(IncludePVRSkipFlag);
        invokeex(request, reply);
        ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret) {
            ChannelList = new ArrayList<>();
            int NumOfChannel = reply.readInt();
            for(int i = 0; i < NumOfChannel; i++) {
                //Scoty 20180613 change get simplechannel list for PvrSkip rule -s
                SimpleChannel channel = new SimpleChannel();
                channel.setChannelId(getUnsignedInt(reply.readInt()));
                channel.setChannelNum(reply.readInt());
                channel.setChannelName(reply.readString());
                channel.setUserLock(reply.readInt());
                channel.setCA(reply.readInt());
                channel.setTpId(reply.readInt());
                channel.setChannelSkip(reply.readInt());//Scoty 20181109 modify for skip channel
                channel.setPlayStreamType(PROGRAM_PLAY_STREAM_TYPE.DVB_TYPE);//Scoty Add Youtube/Vod Stream

                ChannelList.add(channel);
                //Scoty 20180613 change get simplechannel list for PvrSkip rule -e
            }

            Log.d(TAG, "GetSimpleProgramListForPesi: " + ChannelList.size());
        }
        request.recycle();
        reply.recycle();
        return ChannelList;
    }

    public List<SimpleChannel> GetSimpleProgramList(int type,int IncludeSkipFlag,int IncludePVRSkipFlag) {//Scoty 20180613 change get simplechannel list for PvrSkip rule
        if(PLATFORM == PLATFORM_PESI)
            return GetSimpleProgramListForPesi(type, IncludeSkipFlag, IncludePVRSkipFlag);//Scoty 20180613 change get simplechannel list for PvrSkip rule
        else
            return GetSimpleProgramListForHisi(type, IncludeSkipFlag, IncludePVRSkipFlag);
    }

    public List<SimpleChannel> GetSimpleProgramListfromTotalChannelList(int type,int IncludeSkipFlag,int IncludePVRSkipFlag) {//Scoty 20180613 change get simplechannel list for PvrSkip rule
        if(PLATFORM == PLATFORM_PESI)
            return GetSimpleProgramListForPesifromTotalChannelList(type, IncludeSkipFlag, IncludePVRSkipFlag);//Scoty 20180613 change get simplechannel list for PvrSkip rule
        else
            return GetSimpleProgramListForHisi(type, IncludeSkipFlag, IncludePVRSkipFlag);
    }


    public ProgramInfo GetProgramByLcn(int lcn, int type) {
        List<ProgramInfo> allProgramInfo = null;
        Log.d(TAG, "GetProgramByLcn: lcn,="+lcn+", type="+type);

        allProgramInfo = GetProgramInfoList(type, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.NUM_ALL);
        if(allProgramInfo != null) {
            for (int i = 0; i < allProgramInfo.size(); i++) {
                Log.d(TAG, "GetProgramByLcn: name="+allProgramInfo.get(i).getDisplayName()+", num="+allProgramInfo.get(i).getDisplayNum()+", type="+allProgramInfo.get(i).getType());
                if (allProgramInfo.get(i).getDisplayNum() == lcn && allProgramInfo.get(i).getType() == type)
                    return allProgramInfo.get(i);
            }
        }
        return null;
    }

    public ProgramInfo GetProgramByChnum(int chnum, int type) {
        List<ProgramInfo> allProgramInfo = null;

        allProgramInfo = GetProgramInfoList(type, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.NUM_ALL);

        if(allProgramInfo != null) {
            for (int i = 0; i < allProgramInfo.size(); i++) {
                if (allProgramInfo.get(i).getDisplayNum() == chnum && allProgramInfo.get(i).getType() == type)
                    return allProgramInfo.get(i);
            }
        }
        return null;
    }

    public ProgramInfo GetProgramByChannelId(long channelId) {
        ProgramInfo program = null;

        program = getProgramByID(channelId);

        if(program != null){
            return program;
        }else
            return null;

        /*
        List<ProgramInfo> allTvProgramInfo = null;
        List<ProgramInfo> allRadioProgramInfo = null;
        Log.d(TAG, "GetProgramByChannelId: ===<>> in channelId = " + channelId);
        allTvProgramInfo = GetProgramInfoList(ProgramInfo.ALL_TV_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.NUM_ALL);

        //TV
        if(allTvProgramInfo != null) {
            for (int i = 0; i < allTvProgramInfo.size(); i++) {
                if ( allTvProgramInfo.get(i).getChannelId() == channelId)
                    return allTvProgramInfo.get(i);
            }
        }
        //Radio
        allRadioProgramInfo = GetProgramInfoList(ProgramInfo.ALL_RADIO_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.NUM_ALL);
        if(allRadioProgramInfo != null) {
            for (int i = 0; i < allRadioProgramInfo.size(); i++) {
                if ( allRadioProgramInfo.get(i).getChannelId() == channelId)
                    return allRadioProgramInfo.get(i);
            }
        }
        Log.d(TAG, "GetProgramByChannelId: return null");
        return null;
        */
    }
    public SimpleChannel GetSimpleProgramByChannelIdForHisi(long channelId) {
        ProgramInfo programInfo = GetProgramByChannelId(channelId);
        if(programInfo != null) {
            SimpleChannel simpleChannel = new SimpleChannel();
            simpleChannel.setChannelId(programInfo.getChannelId());
            simpleChannel.setChannelNum(programInfo.getDisplayNum());
            simpleChannel.setChannelName(programInfo.getDisplayName());
            simpleChannel.setUserLock(programInfo.getLock());
            simpleChannel.setCA(programInfo.getCA());
            return simpleChannel;
        }
        return null;
    }

    //eric lin a8, need fix
    //public SimpleChannel GetSimpleProgramByChannelIdForPesi(long channelId) {
    //    return null;
    //}
    public SimpleChannel GetSimpleProgramByChannelIdForPesi(long channelId) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetSimpleChannel);
        request.writeInt((int) channelId);
        invokeex(request, reply);
        int ret = reply.readInt();
        SimpleChannel channel = null;
        if (CMD_RETURN_VALUE_SUCCESS == ret) {
            channel = new SimpleChannel();
            channel.setChannelId(getUnsignedInt(reply.readInt()));
            channel.setChannelNum(reply.readInt());
            channel.setChannelName(reply.readString());
            channel.setUserLock(reply.readInt());
            channel.setCA(reply.readInt());
            channel.setTpId(reply.readInt());
            channel.setChannelSkip(reply.readInt());//Scoty 20181109 modify for skip channel
        }
        request.recycle();
        reply.recycle();
        return channel;
    }

    public SimpleChannel GetSimpleProgramByChannelId(long channelId) {
        if(PLATFORM == PLATFORM_PESI)
            return GetSimpleProgramByChannelIdForPesi(channelId);
        else
        return GetSimpleProgramByChannelIdForHisi(channelId);
    }

    public SimpleChannel GetSimpleProgramByChannelIdfromTotalChannelListByGroup(int groupType, long channelId)
    {
        return GetSimpleProgramByChannelIdForPesifromTotalChannelListandType(groupType,channelId);
    }

    public SimpleChannel GetSimpleProgramByChannelIdfromTotalChannelList( long channelId)
    {
        return GetSimpleProgramByChannelIdForPesifromTotalChannelList(channelId);
    }

    private SimpleChannel GetSimpleProgramByChannelIdForPesifromTotalChannelListandType(int groupType, long channelId) {
        for(int i = 0 ; i < TotalChannelList.get(groupType).size() ; i++) {
            if(channelId == TotalChannelList.get(groupType).get(i).getChannelId()) {
                Log.d(TAG, "GetSimpleProgramByChannelIdForPesifromTotalChannelList: Name = [" + TotalChannelList.get(groupType).get(i).getChannelName()
                        +"] channelId = [" + channelId +"] groupType = [" + groupType + "]");
                return TotalChannelList.get(groupType).get(i);
            }
        }
        return null;

    }

    private SimpleChannel GetSimpleProgramByChannelIdForPesifromTotalChannelList(long channelId) {

        for(int i = 0 ; i < TotalChannelList.size() ; i++) {
            for(int j = 0 ; j < TotalChannelList.get(i).size() ; j++) {
                if (channelId == TotalChannelList.get(i).get(j).getChannelId()) {
                    Log.d(TAG, "GetSimpleProgramByChannelIdForPesifromTotalChannelList: Name = [" + TotalChannelList.get(i).get(j).getChannelName()
                            +"] channelId = [" + channelId +"]");
                    return TotalChannelList.get(i).get(j);
                }
            }
        }
        return null;

    }

    //CMD_PM_EditChannel    //need fix
    /*
    public void Save(ProgramInfo pProgram) {
        if(pProgram != null) {
            int type = pProgram.getType();
            List<ProgramInfo> allProgramInfo = null;
            allProgramInfo = this.testData.GetTestDataProgramInfoArray(type);
            if(allProgramInfo == null) {
                allProgramInfo = this.testData.GetTestDataProgramInforSave(type);
            }
            allProgramInfo.add(pProgram);
        }
    }
    */

    //need fix
    /*
    public void Save(List<ProgramInfo> pPrograms) {
        if(pPrograms != null) {
            int type = ((ProgramInfo)pPrograms.get(0)).getType();
            List<ProgramInfo> allProgramInfo = null;
            this.testData.TestDataProgramInfoListSave(type, pPrograms);
            allProgramInfo = this.testData.GetTestDataProgramInfoArray(type);
            if(allProgramInfo != null) {
                Log.d("TestDataProgramInfoFuncImpl", "Save: allProgramInfo not null, size=" + allProgramInfo.size());
            } else {
                Log.d("TestDataProgramInfoFuncImpl", "Save: allProgramInfo is null");
            }
        }

    }
    */

    //need fix
    /*
    public void Save(List<SimpleChannel> pPrograms, int type) {
        List<ProgramInfo> temp = new ArrayList();
        List<ProgramInfo> allProgramInfo = null;
        allProgramInfo = this.testData.GetTestDataProgramInfoArray(type);

        for(int i = 0; i < pPrograms.size(); ++i) {
            for(int j = 0; j < allProgramInfo.size(); ++j) {
                if(((ProgramInfo)allProgramInfo.get(j)).getChannelId() == ((SimpleChannel)pPrograms.get(i)).getChannelId()) {
                    ((ProgramInfo)allProgramInfo.get(j)).setDisplayName(((SimpleChannel)pPrograms.get(i)).getChannelName());
                    ((ProgramInfo)allProgramInfo.get(j)).setDisplayNum(((SimpleChannel)pPrograms.get(i)).getChannelNum());
                    ((ProgramInfo)allProgramInfo.get(j)).setLock(((SimpleChannel)pPrograms.get(i)).getUserLock());
                    temp.add(allProgramInfo.get(j));
                }
            }
        }

        this.testData.TestDataProgramInfoListSave(type, temp);
        this.testData.GetTestDataProgramInfoArray(type);
    }
    */

    public int delChannelByID(long channelID)
    {
        Log.d(TAG, "delChannelByID");
        return excuteCommand(CMD_PM_DelChannelByID, channelID);
    }

    public void DeleteProgram(long channelId) {
        delChannelByID(channelId);
        //saveTable(EnTableType.PROGRAME);
    }

    /*public int editChannel(ChannelNode channelNode)
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_EditChannel);
        request.writeInt(channelNode.channelID);
        //request.writeInt(channelNode.AudPid);
        //request.writeInt(channelNode.AudType);
        //request.writeInt(channelNode.VidPid);
        //request.writeInt(channelNode.VidType);
        //request.writeInt(channelNode.PcrPID);
        request.writeInt(channelNode.favorTag);
        request.writeInt(channelNode.editTag);
        request.writeString(channelNode.OrignalServiceName);
        request.writeInt(channelNode.TPID);
        //request.writeInt(channelNode.volTrack.Volume);
        //request.writeInt(channelNode.volTrack.AudioChannel);
        request.writeInt(channelNode.LCN);
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        Log.d(TAG, "editChannel ret = " + ret);
        return getReturnValue(ret);
    }*/

    public int setDefaultOpenChannel(long channelId, int groupType)
    {
        Log.d(TAG, "setDefaultOpenChannel groupType = " + groupType+", channelId="+channelId);
        return excuteCommand(CMD_PM_SetDefaultOpenChannel, channelId, groupType);
    }

    public long getDefaultOpenChannel()
    {
        int defOpenChannel=0;

        defOpenChannel = excuteCommandGetII(CMD_PM_GetLastChannelID);
        if(defOpenChannel <= -1)//Scoty 20180529 fixed default open channel get last channel less than -1,UI will get wrong channelId
            defOpenChannel = 0;
        Log.d(TAG, "222 getDefaultOpenChannel: defOpenChannel="+defOpenChannel);
        return defOpenChannel;
    }

    public DefaultChannel getDefaultChannelForPesi() {
        DefaultChannel defChannal = null;
        Log.d(TAG, "getDefaultChannelForPesi : ");
        long defOpenChannelId = getDefaultOpenChannel();
        if(defOpenChannelId != 0) {
            defChannal = new DefaultChannel();
            defChannal.setChanneId(defOpenChannelId);
            defChannal.setGroupType(getDefaultOpenGroupType());
        }
        return defChannal;
    }

    public DefaultChannel getDefaultChannelForHisi() {
        long defOpenChannelId = 0;
        int defOpenGroupType = 0;
        SimpleChannel program=null;
        defOpenChannelId = getDefaultOpenChannel();
        defOpenGroupType = getDefaultOpenGroupTypeForHisi(defOpenChannelId);
        Log.e(TAG,"getDefaultChannelForHisi !!");
        program = GetSimpleProgramByChannelId(defOpenChannelId);
        if(program != null){
            DefaultChannel defChannal = new DefaultChannel();
            defChannal.setChanneId(program.getChannelId());
            defChannal.setGroupType(defOpenGroupType);
            return defChannal;
        }else
            return null;
    }

    public DefaultChannel getDefaultChannel() {
        if(PLATFORM == PLATFORM_HISI)
            return getDefaultChannelForHisi();
        else
            return getDefaultChannelForPesi();
    }
    ///////// for pesi middleware---pesimain /////////
    //eric lin a8, need fix
    //public int saveSatList(List<SatInfo> satInfoList){
    //    return 0;
    //}
    public int updateSatList(List<SatInfo> satInfoList)
    {
        Log.d(TAG, "updateSatList");
        int ret = 0;
        if(satInfoList.size() > 0) {
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            request.writeString(DTV_INTERFACE_NAME);
            request.writeInt(CMD_PM_SaveSatList);
            request.writeInt(satInfoList.size());
            for (int i = 0; i < satInfoList.size(); i++) {
                request.writeInt(satInfoList.get(i).getSatId());
                request.writeString(satInfoList.get(i).getSatName());
                request.writeInt(satInfoList.get(i).getTunerType());
                request.writeInt(satInfoList.get(i).getTpNum());
                request.writeInt(calcLongitudeFromAngle(satInfoList.get(i).getAngle(), satInfoList.get(i).getAngleEW()));
                request.writeInt(satInfoList.get(i).getLocation());
                request.writeInt(satInfoList.get(i).getPostionIndex());
                request.writeInt(satInfoList.get(i).Antenna.getLnb1());
                request.writeInt(satInfoList.get(i).Antenna.getLnb2());
                request.writeInt(satInfoList.get(i).Antenna.getLnbType());
                request.writeInt(satInfoList.get(i).Antenna.getDiseqcType());
                //request.writeInt(satInfoList.get(i).Antenna.getDiseqcUse());
                request.writeInt(satInfoList.get(i).Antenna.getDiseqc());
                request.writeInt(satInfoList.get(i).Antenna.getTone22kUse());
                request.writeInt(satInfoList.get(i).Antenna.getTone22k());
                request.writeInt(satInfoList.get(i).Antenna.getV1418Use());
                request.writeInt(satInfoList.get(i).Antenna.getV1418());
                request.writeInt(satInfoList.get(i).Antenna.getCku());
                for (int j = 0; j < satInfoList.get(i).getTpNum(); j++) {
                    request.writeInt(satInfoList.get(i).getTps().get(j));
                }
            }
            invokeex(request, reply);
            ret = reply.readInt();
            request.recycle();
            reply.recycle();
        }
        return getReturnValue(ret);
    }

    //eric lin a8, need fix
    //public int saveTpList(List<TpInfo> tpInfoList){
    //    return 0;
    //}
    public int updateTpList(List<TpInfo> tpInfoList)
    {
        Log.d(TAG, "updateTpList");
        int ret = 0;
        if(tpInfoList.size() > 0) {
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            request.writeString(DTV_INTERFACE_NAME);
            request.writeInt(CMD_PM_SaveTpList);
            request.writeInt(tpInfoList.size());
            for(int i = 0; i < tpInfoList.size(); i++) {
                request.writeInt(tpInfoList.get(i).getTpId());
                request.writeInt(tpInfoList.get(i).getSatId());
                request.writeInt(tpInfoList.get(i).getTunerType());
                if(tpInfoList.get(i).getTunerType() == TpInfo.DVBT || tpInfoList.get(i).getTunerType() == TpInfo.ISDBT) {
                    //dvb t
                    request.writeInt(tpInfoList.get(i).TerrTp.getChannel());
                    request.writeInt(tpInfoList.get(i).TerrTp.getFreq());
                    request.writeInt(tpInfoList.get(i).TerrTp.getBand());
                }
                else if(tpInfoList.get(i).getTunerType() == TpInfo.DVBS) {
                    //dvb s
                    request.writeInt(tpInfoList.get(i).SatTp.getFreq());
                    request.writeInt(tpInfoList.get(i).SatTp.getSymbol());
                    request.writeInt(tpInfoList.get(i).SatTp.getPolar());
                }
                else if(tpInfoList.get(i).getTunerType() == TpInfo.DVBC) {
                    //dvb c
                    request.writeInt(tpInfoList.get(i).CableTp.getChannel());
                    request.writeInt(tpInfoList.get(i).CableTp.getFreq());
                    request.writeInt(tpInfoList.get(i).CableTp.getSymbol());
                    request.writeInt(tpInfoList.get(i).CableTp.getQam());
                }
                else {
                    continue;
                }
            }
            invokeex(request, reply);
            ret = reply.readInt();
            request.recycle();
            reply.recycle();
        }
        return getReturnValue(ret);
    }

    //eric lin a8, need fix
    //public int saveSimpleChannelList(List<SimpleChannel> simpleChannelList,int type) {
    //   return 0;
    //}

    private int updateTotalChannelList(List<SimpleChannel> simpleChannelList,int type)
    {
        int ret = 0;
        if(simpleChannelList.size() > 0) {
            for (int i = 0; i < simpleChannelList.size(); i++) {
                TotalChannelList.get(type).add(simpleChannelList.get(i));
            }
        }else{
            ret = 1;
        }
        return getReturnValue(ret);
    }

    public int updateSimpleChannelList(List<SimpleChannel> simpleChannelList,int type) {
        Log.d(TAG, "updateSimpleChannelList");
        int ret = 0;
        updateTotalChannelList(simpleChannelList,type);
        if(simpleChannelList.size() > 0) {
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            request.writeString(DTV_INTERFACE_NAME);
            request.writeInt(CMD_PM_SaveSampleChannelList);
            request.writeInt(type);
            request.writeInt(simpleChannelList.size());
            if(type == ProgramInfo.ALL_TV_TYPE || type == ProgramInfo.ALL_RADIO_TYPE) {
                for (int i = 0; i < simpleChannelList.size(); i++) {
                    request.writeInt((int) simpleChannelList.get(i).getChannelId());
                    request.writeInt(simpleChannelList.get(i).getChannelNum());
                    request.writeString(simpleChannelList.get(i).getChannelName());
                    request.writeInt(simpleChannelList.get(i).getUserLock());
                    request.writeInt(simpleChannelList.get(i).getChannelSkip());//Scoty 20181109 modify for skip channel
                }
            }
            else {
                for (int i = 0; i < simpleChannelList.size(); i++) {
                    request.writeInt((int) simpleChannelList.get(i).getChannelId());
                    request.writeInt(simpleChannelList.get(i).getChannelNum());
//gary20200619 fix save fav list not work,only save first channel
//                    request.writeString(simpleChannelList.get(i).getChannelName());
//                    request.writeInt(simpleChannelList.get(i).getUserLock());
//                    request.writeInt(simpleChannelList.get(i).getChannelSkip()); // Edwin 20181129 add these to match CMD
                }
            }
            invokeex(request, reply);
            ret = reply.readInt();
            request.recycle();
            reply.recycle();
        }
        return getReturnValue(ret);
    }

    /* if add new param to save , should modify middleware pesimain related code*/
    //eric lin a8, need fix
    //public int saveProgramInfo(ProgramInfo pProgram) {
    //    return 0;
    //}
    public int updateProgramInfo(ProgramInfo pProgram) {
        int ret = -1;
        Log.d(TAG, "ProgramInfoUpdate some param - only AudioSelected,AudioLRSelected.");
        if(pProgram != null) {
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            request.writeString(DTV_INTERFACE_NAME);
            request.writeInt(CMD_PM_SaveChannel);
            request.writeInt((int) pProgram.getChannelId());
            request.writeInt(pProgram.getAudioSelected());
            request.writeInt(pProgram.getAudioLRSelected());
            invokeex(request, reply);
            ret = reply.readInt();
            request.recycle();
            reply.recycle();
        }
        return getReturnValue(ret);
    }

    //eric lin a8, need fix
    //public int FavInfoSaveList(List<FavInfo> favInfo) {
    //    return 0;
    //}
    public int FavInfoUpdateList(List<FavInfo> favInfo) {
        Log.d(TAG, "FavInfoUpdateList");
        int ret = 0;
        if(favInfo.size() > 0) {
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            request.writeString(DTV_INTERFACE_NAME);
            request.writeInt(CMD_PM_SaveGroupList);
            request.writeInt(favInfo.size());
            for (int i = 0; i < favInfo.size(); i++) {
                request.writeInt(favInfo.get(i).getFavNum());
                request.writeInt((int) favInfo.get(i).getChannelId());
                request.writeInt(favInfo.get(i).getFavMode());
            }
            invokeex(request, reply);
            ret = reply.readInt();
            request.recycle();
            reply.recycle();
        }
        return getReturnValue(ret);
    }

    public int updateBookList(List<BookInfo> bookInfoList) // Need command and implement
    {
        if(bookInfoList != null && bookInfoList.size() > 0) {
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            request.writeString(DTV_INTERFACE_NAME);
            request.writeInt(CMD_BOOK_SaveBookList);
            request.writeInt(bookInfoList.size());
            for(int i = 0; i < bookInfoList.size(); i++) {
                writeBookData(request, bookInfoList.get(i));
            }
            invokeex(request, reply);
            int ret = reply.readInt();
            Log.d(TAG, "ret =" + ret);
            request.recycle();
            reply.recycle();
            return getReturnValue(ret);
        }
        return 0;
    }

    // ========EPG==========

    public int startEpg(long channelID)
    {
        Log.d(TAG, "startEpg(" + channelID  + ")");
        return excuteCommand(CMD_EPG_Start, channelID);
    }


    public EPGEvent getPresentEvent(long channelID)
    {
        Log.d(TAG, "getPresentEvent(" + channelID + ")");
        EPGEvent event = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_EPG_GetPresentEvent);
        request.writeInt((int) channelID);

        invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);

        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            event = new EPGEvent();
            initialEPGEvent(event, reply);
            event.setEventType(EPGEvent.EPG_TYPE_PRESENT);
        }

        request.recycle();
        reply.recycle();

        return event;
    }

    public EPGEvent getFollowEvent(long channelID)
    {
        Log.d(TAG, "getFollowEvent(" + channelID + ")");
        EPGEvent event = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_EPG_GetFollowEvent);
        request.writeInt((int) channelID);

        invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "ret = " + ret);

        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            event = new EPGEvent();
            initialEPGEvent(event, reply);
            event.setEventType(EPGEvent.EPG_TYPE_FOLLOW);
        }

        request.recycle();
        reply.recycle();

        return event;
    }

    public EPGEvent getEpgByEventID(long channelID, int eventId)
    {
        long channelId = channelID;
        Log.d(TAG, "GetEpgByEventID(" + channelId + "," + eventId + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_EPG_GetByEventId);
        request.writeInt((int) channelId);
        request.writeInt(eventId);

        EPGEvent event = null;
        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            event = new EPGEvent();
            initialEPGEvent(event, reply);
        }

        request.recycle();
        reply.recycle();

        return event;
    }

    public List<EPGEvent> getEPGEvents(long channelID, Date startTime, Date endTime, int pos, int reqNum, int addEmpty)
    {
        Log.d(TAG, "getEPGEvents(EPGEventFilter," + pos + "," + reqNum + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_EPG_GetEventByTime);

        /*1. send channel id*/
        long channelId = channelID;

        Log.d(TAG, "channelId = " + channelId);
        request.writeInt((int) channelId);
        //Calendar ca = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

        /*2. send start time*/
        Date startDate = startTime;
        if (null != startDate)
        {
            //DtvDate dtvDate = this.timeDate2DtvDate(startDate);

            /*request.writeInt(dtvDate.mYear);
            request.writeInt(dtvDate.mMonth);
            request.writeInt(dtvDate.mDay);
            request.writeInt(dtvDate.mHour);
            request.writeInt(dtvDate.mMinute);
            request.writeInt(dtvDate.mSecond);*/

            request.writeInt(startTime.getYear() + 1900);
            request.writeInt(startTime.getMonth() +1);
            request.writeInt(startTime.getDate());
            request.writeInt(startTime.getHours());
            request.writeInt(startTime.getMinutes());
            request.writeInt(startTime.getSeconds());

            Log.d(TAG, "EPGTEST getEPGEvents:  startTime = "+startTime.getYear()+"/"+startTime.getMonth()+"/"+startTime.getDate()+", "+startTime.getHours()+":"+startTime.getMinutes()+":"+startTime.getSeconds());//eric lin test

            Log.d(TAG, "getEPGEvents:  Start Date = " + (startDate.getYear()+1900) +"/"+ (startDate.getMonth() + 1) + "/" + startDate.getDate()
                    + "     " + startDate.getHours() + ":" + startDate.getMinutes() + ":" + startDate.getSeconds());
        }
        else
        {
            Log.d(TAG, "getEPGEvents:  Start Date = NULL !!!!!");
            request.writeInt(0);
            request.writeInt(0);
            request.writeInt(0);
            request.writeInt(0);
            request.writeInt(0);
            request.writeInt(0);
        }

        /*3. send end time*/
        Date endDate = endTime;
        if (null != endDate)
        {
            //DtvDate dtvDate = this.timeDate2DtvDate(endDate);

            /*request.writeInt(dtvDate.mYear);
            request.writeInt(dtvDate.mMonth);
            request.writeInt(dtvDate.mDay);
            request.writeInt(dtvDate.mHour);
            request.writeInt(dtvDate.mMinute);
            request.writeInt(dtvDate.mSecond);*/

            request.writeInt(endTime.getYear() + 1900);
            request.writeInt(endTime.getMonth() + 1);
            request.writeInt(endTime.getDate());
            request.writeInt(endTime.getHours());
            request.writeInt(endTime.getMinutes());
            request.writeInt(endTime.getSeconds());

            Log.d(TAG, "EPGTEST getEPGEvents:  endTime = "+endTime.getYear()+"/"+endTime.getMonth()+"/"+endTime.getDate()+", "+endTime.getHours()+":"+endTime.getMinutes()+":"+endTime.getSeconds());//eric lin test
        }
        else
        {
            request.writeInt(0);
            request.writeInt(0);
            request.writeInt(0);
            request.writeInt(0);
            request.writeInt(0);
            request.writeInt(0);
        }

        /*4. send weekday index*/

        /*5. send content level*/

        request.writeInt(pos);
        request.writeInt(reqNum);
        request.writeInt(addEmpty);

        List<EPGEvent> list = null;
        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int retNum = reply.readInt();
            Log.d(TAG, "retNum = " + retNum);
            if (retNum >= 0)
            {
                list = new ArrayList<EPGEvent>();
                for (int i = 0; i < retNum; i++)
                {
                    EPGEvent event = new EPGEvent();
                    initialEPGEvent(event, reply);
                    list.add(event);
                }
            }
        }

        request.recycle();
        reply.recycle();

        return list;
    }

    private void initialEPGEvent(EPGEvent event, Parcel reply)
    {
        long channelId = getUnsignedInt(reply.readInt());
        //event.setChannelId(channelId);
        //Log.d(TAG, "initialEPGEvent:  channel ID = " + channelId);

        int eventId = reply.readInt();
        event.setEventId(eventId);
        //Log.d(TAG, "initialEPGEvent:  Event ID = " + eventId);

        DtvDate dtvDate = new DtvDate(reply.readInt(),reply.readInt(),reply.readInt(),reply.readInt(),reply.readInt(),reply.readInt());

        //Log.d(TAG, String.format("EPG = %d-%d-%d %d:%d:%d" , dtvDate.mYear, dtvDate.mMonth, dtvDate.mDay, dtvDate.mHour, dtvDate.mMinute, dtvDate.mSecond));

        // set start date
        Date startDate = new Date(dtvDate.mYear-1900, dtvDate.mMonth- 1, dtvDate.mDay, dtvDate.mHour, dtvDate.mMinute, dtvDate.mSecond);//Date startDate = this.timeDtvDate2date(dtvDate);
        event.setStartTime(startDate.getTime());

        //int durations = reply.readInt() * 1000;
        int durations = reply.readInt();
//        if(durations % 60 != 0)
//            durations = 60 * (durations/60);
        durations = durations*1000;

        //Log.d(TAG, "initialEPGEvent:  duration = " + durations);
        event.setDuration(durations);

        long endTime = startDate.getTime() + durations;
        event.setEndTime(endTime);

        int freeCA = reply.readInt();
        //Log.d(TAG, "initialEPGEvent:  freeCA = " + freeCA);
        //if (0 == freeCA)
        //{
        //    event.setFreeCA(true);
        //}
        //else
        //{
        //    event.setFreeCA(false);
        //}

        int parentLockLevel = reply.readInt();
        event.setParentalRate(parentLockLevel);
        //Log.d(TAG, "initialEPGEvent:  parentLockLevel = " + parentLockLevel);
        //event.setParentLockLevel(parentLockLevel);

        //String countryCode = reply.readString();
        //Log.d(TAG, "initialEPGEvent:  CoountryCode = " + countryCode);
        //event.setParentCountryCode(countryCode);

        int contentLevel1 = reply.readInt();
        //Log.d(TAG, "initialEPGEvent: contentLevel1 = " + contentLevel1);
        //event.setContentLevel1(contentLevel1);

        int contentLevel2 = reply.readInt();
        //Log.d(TAG, "initialEPGEvent:  contentLevel2 = " + contentLevel2);
        //event.setContentLevel2(contentLevel2);

        int contentLevel3 = reply.readInt();
        //Log.d(TAG, "initialEPGEvent:  contentLevel3 = " + contentLevel3);
        //event.setContentLevel3(contentLevel3);

        int contentLevel4 = reply.readInt();
        //Log.d(TAG, "initialEPGEvent: contentLevel4 = " + contentLevel4);
        //event.setContentLevel4(contentLevel4);

        String name = reply.readString();
        //Log.d(TAG, "initialEPGEvent:  Name = " + name);
        if (null != name)
        {
            event.setEventName(name);
        }
    }

    public String getShortDescription(long channelId, int eventId)
    {
        Log.d(TAG, "getShortDescription(" + channelId + "," + eventId + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_EPG_GetShortDesc);
        request.writeInt((int) channelId);
        request.writeInt(eventId);

        String desc = null;
        invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "getShortDescription:  ret = " + ret);
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            desc = reply.readString();
        }

        request.recycle();
        reply.recycle();
        return desc;
    }

    public String getDetailDescription(long channelId, int eventId)
    {
        Log.d(TAG, "GetDetailDescription(" + channelId + "," + eventId + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_EPG_GetDetailDesc);
        request.writeInt((int) channelId);
        request.writeInt(eventId);

        String detailDescription=null;
        //LocalExtendedDescription localExtendedDescription = null;
        invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "getDetailDescription:  ret = " + ret);
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            //localExtendedDescription = new LocalExtendedDescription();
            int itemNum = reply.readInt();
            Log.v(TAG, "itemNum = " + itemNum);
            if (itemNum > 0)
            {
                //HashMap<String, String> map = new LinkedHashMap<String, String>();
                for (int i = 0; i < itemNum; i++)
                {
                    String key = reply.readString();
                    String value = reply.readString();
                    //Log.v(TAG, "map.put(key = " + key + ",value = " + value + ")");
                    //map.put(key, value);
                }
                //localExtendedDescription.setItemsContent(map);
            }

            detailDescription = reply.readString();
            //localExtendedDescription.setDetailDescription(detailDescription);
        }
        request.recycle();
        reply.recycle();

        //return localExtendedDescription;
        return detailDescription;
    }

    public int setEvtLang(String firstEvtLang, String secondEvtLang)
    {
        Log.d(TAG, "setEvtLang(" + firstEvtLang + " : " + secondEvtLang +  ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_EPG_SetLanguageCode);

        request.writeString(firstEvtLang);
        //request.writeString(secondEvtLang);

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS != ret)
        {
            Log.e(TAG, "setEvtLang fail, ret = " + ret);
        }

        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    public int setTime(Date date)
    {
        long offset = 0;
        int ret = CMD_RETURN_VALUE_FAILAR;

        if(date == null)
        {
            return getReturnValue(ret);
        }

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_TMCTL_SetDateTime);

        Calendar ca = Calendar.getInstance();
        if(ADD_SYSTEM_OFFSET) { // connie 20181101 for not add system offset
            int dst = ca.get(Calendar.DST_OFFSET);
            int zone = ca.get(Calendar.ZONE_OFFSET);
            offset = zone + dst;
        }
        date.setTime(date.getTime() - offset);
        ca.setTime(date);

        int year = date.getYear();
        int month = date.getMonth();
        int mday = date.getDate();
        int hour = date.getHours();
        int min = date.getMinutes();
        int isecond = date.getSeconds();

        request.writeInt(year + 1900);
        request.writeInt(month + 1);
        request.writeInt(mday);
        request.writeInt(hour);
        request.writeInt(min);
        request.writeInt(isecond);
        request.writeInt(0);
        request.writeInt(0);
        invokeex(request, reply);
        if (CMD_RETURN_VALUE_SUCCESS == reply.readInt())
        {
            Log.d(TAG, "setTime: success year="+year+", month="+month+", mday="+mday+", hour="+hour+", min="+min);
            ret = CMD_RETURN_VALUE_SUCCESS;
        }else
            Log.d(TAG, "setTime: fail year="+year+", month="+month+", mday="+mday+", hour="+hour+", min="+min);

        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    public Date getDtvDate()
    {
        //Log.d(TAG, "getDtvDate: ");
        Date getDate = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_TMCTL_GetDateTime);
        invokeex(request, reply);
        int ret = reply.readInt();
        //Log.d(TAG, "getDtvDate:  ret = " + ret);
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int year = 0;
            int month = 0;
            int day = 0;
            int hour = 0;
            int min = 0;
            int second = 0;
            long offset = 0;
            year = reply.readInt();
            month = reply.readInt();
            day = reply.readInt();
            hour = reply.readInt();
            min = reply.readInt();
            second = reply.readInt();
            getDate = new Date(year - 1900,month - 1,day,hour,min,second);

            offset = 0;
            if(ADD_SYSTEM_OFFSET) {  // connie 20181106 for not add system offset
                Calendar ca = Calendar.getInstance();
                int zone = ca.get(Calendar.ZONE_OFFSET);
                int dst = ca.get(Calendar.DST_OFFSET);

                offset = zone + dst;
            }
            getDate.setTime(getDate.getTime() + offset);
        }
        request.recycle();
        reply.recycle();
        return getDate;
    }

    public int syncTime(boolean bEnable) {
        //Log.d(TAG, "syncTime bEnable=" + bEnable);
        return excuteCommand(CMD_TMCTL_AutoSyncTimeFromDtv, bEnable ? 1 : 0);
    }

    //eric lin a8, need fix
    //public FavInfo FavInfoGet(int favMode,int index) {
    //    return null;
    //}
    public FavInfo FavInfoGet(int favMode,int index) {
        Log.d(TAG, "FavInfoGet favMode : " + favMode + " index : " + index);
        int ret;
        FavInfo favInfo = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetGroupChannel);
        request.writeInt(favMode);
        request.writeInt(index);
        invokeex(request, reply);
        ret = reply.readInt();
        if(ret == CMD_RETURN_VALUE_SUCCESS) {
            int favNum = reply.readInt();
            long channelID = getUnsignedInt(reply.readInt());
            favInfo = new FavInfo(favNum,channelID,favMode);
        }
        request.recycle();
        reply.recycle();
        return favInfo;
    }

    //eric lin a8, need fix
    //public List<FavInfo> FavInfoGetList(int favMode) {
    //    return null;
    //}
    public List<FavInfo> FavInfoGetList(int favMode) {
        Log.d(TAG, "FavInfoGetList favMode : " + favMode);
        int ret;
        List<FavInfo> favInfoList = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetGroupChannelList);
        request.writeInt(favMode);
        invokeex(request, reply);
        ret = reply.readInt();
        if(ret == CMD_RETURN_VALUE_SUCCESS) {
            favInfoList = new ArrayList<>();
            int favMaxNum = reply.readInt();
            if(favMaxNum > 0) {
                for(int i = 0; i < favMaxNum; i++) {
                    int favNum = reply.readInt();
                    long channelID = getUnsignedInt(reply.readInt());
                    FavInfo favInfo = new FavInfo(favNum, channelID, favMode);
                    favInfoList.add(favInfo);
                }
            }
        }
        request.recycle();
        reply.recycle();
        return favInfoList;
    }

    //eric lin a8, need fix
    //public int FavInfoDelete(int favMode,long channelId) {
    //    return 0;
    //}
    public int FavInfoDelete(int favMode,long channelId) {
        Log.d(TAG, "FavInfoDelete favMode : " + favMode + " channelId : " + channelId);
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_DelGroupChannel);
        request.writeInt(favMode);
        request.writeInt((int) channelId);
        invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    //eric lin a8, need fix
    //public int FavInfoDeleteAll(int favMode) {
    //    return 0;
    //}
    public int FavInfoDeleteAll(int favMode) {
        Log.d(TAG, "FavInfoDeleteAll favMode : " + favMode);
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_DelGroupAll);
        request.writeInt(favMode);
        invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    //eric lin a8, need fix

    /*public String FavGroupNameGet(int favMode) {
        String string;
        if(favMode == ProgramInfo.ALL_TV_TYPE)
            string = "ALL TV Channels";
        else if(favMode == ProgramInfo.TV_FAV1_TYPE)
            string = "TV Favorite 1";
        else if(favMode == ProgramInfo.TV_FAV2_TYPE)
            string = "TV Favorite 2";
        else if(favMode == ProgramInfo.TV_FAV3_TYPE)
            string = "TV Favorite 3";
        else if(favMode == ProgramInfo.TV_FAV4_TYPE)
            string = "TV Favorite 4";
        else if(favMode == ProgramInfo.TV_FAV5_TYPE)
            string = "TV Favorite 5";
        else if(favMode == ProgramInfo.TV_FAV6_TYPE)
            string = "TV Favorite 6";
        else if(favMode == ProgramInfo.ALL_RADIO_TYPE)
            string = "ALL RADIO Channels";
        else if(favMode == ProgramInfo.RADIO_FAV1_TYPE)
            string = "RADIO Favorite 1";
        else if(favMode == ProgramInfo.RADIO_FAV2_TYPE)
            string = "RADIO Favorite 2";
        else
            string = " ";
        return string;
    }*/
    public String FavGroupNameGet(int favMode) {
        int ret;
        String strName = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetGroupName);
        request.writeInt(favMode);
        invokeex(request, reply);
        ret = reply.readInt();
        if(ret == CMD_RETURN_VALUE_SUCCESS) {
            strName = reply.readString();
        }
        request.recycle();
        reply.recycle();
        return strName;
    }

    //eric lin a8, need fix
    //public int FavGroupNameSave(int favMode, String name) {
    //    return 0;
    //}

    public int FavGroupNameUpdate(int favMode, String name) {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_SaveGroupName);
        request.writeInt(favMode);
        request.writeString(name);
        invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public GposInfo GposInfoGetForPesi() {
        int ret;
        GposInfo gposInfo = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_CFG_GetGpos);
        invokeex(request, reply);
        ret = reply.readInt();
        if(ret == CMD_RETURN_VALUE_SUCCESS) {
            gposInfo = new GposInfo();
            gposInfo.setDBVersion(reply.readString());
            gposInfo.setCurChannelId(getUnsignedInt(reply.readInt()));
            gposInfo.setCurGroupType(reply.readInt());
            gposInfo.setPasswordValue(reply.readInt());
            gposInfo.setParentalRate(reply.readInt());
            gposInfo.setParentalLockOnOff(reply.readInt());
            gposInfo.setInstallLockOnOff(reply.readInt());
            gposInfo.setBoxPowerStatus(reply.readInt());
            gposInfo.setStartOnChannelId(getUnsignedInt(reply.readInt()));
            gposInfo.setStartOnChType(reply.readInt());
            gposInfo.setVolume(reply.readInt());
            gposInfo.setAudioTrackMode(reply.readInt());
            gposInfo.setAutoRegionTimeOffset(reply.readInt());
            float RegionTimeOffset = (float)(reply.readInt()/2);
            gposInfo.setRegionTimeOffset(RegionTimeOffset);
            gposInfo.setRegionSummerTime(reply.readInt());
            gposInfo.setLnbPower(reply.readInt());
            gposInfo.setScreen16x9(reply.readInt());
            gposInfo.setConversion(reply.readInt());
            int resolution = reply.readInt();
            gposInfo.setOSDLanguage(reply.readString());
            gposInfo.setSearchProgramType(reply.readInt());
            gposInfo.setSearchMode(reply.readInt());
            gposInfo.setAudioLanguageSelection(0,reply.readString());
            gposInfo.setAudioLanguageSelection(1,reply.readString());
            gposInfo.setSubtitleLanguageSelection(0,reply.readString());
            gposInfo.setSubtitleLanguageSelection(1,reply.readString());
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
        return gposInfo;
    }

    public GposInfo GposInfoGetForHisi() {
        return tmpGposInfo;
    }

    public GposInfo GposInfoGet() {
        if(PLATFORM == PLATFORM_PESI)
            return GposInfoGetForPesi();
        else
            return GposInfoGetForHisi();
    }

    public void GposInfoUpdateForPesi(GposInfo gPos) {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
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
        int RegionTimeOffset = (int)(gPos.getRegionTimeOffset()*2);
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
        invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
    }

    public void GposInfoUpdateForHisi(GposInfo gPos) {

    }

    public void GposInfoUpdate(GposInfo gPos) {
        if(PLATFORM == PLATFORM_PESI)
            GposInfoUpdateForPesi(gPos);
        else
            GposInfoUpdateForHisi(gPos);
    }
    
    public void GposInfoUpdateByKeyString(String key,String value) {
        Log.d(TAG, "GposInfoUpdateByKeyString key ["+key+"] value["+value+"]");
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_CFG_SetStringValue);
        request.writeString(key);
        request.writeString(value);
        invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
    }

    public void GposInfoUpdateByKeyString(String key,int value) {
        Log.d(TAG, "GposInfoUpdateByKeyString key ["+key+"] value["+value+"]");
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_CFG_SetIntValue);
        request.writeString(key);
        request.writeInt(value);
        invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
    }

    //eric lin a8, need fix
    public void PESI_CMD_CallBackTest(int cmd) {
        /*
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_CallBackTest);
        request.writeInt(cmd);
        invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        */
    }

    public int  ResetFactoryDefault() {
        Log.d(TAG, "restoreDefaultConfig");
        return excuteCommand(CMD_CFG_RestoreDefaultConfig);
    }

    public String GetPesiServiceVersion() {
        int ret;
        String version = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_COMM_GetPesiServiceVersion);
        invokeex(request, reply);
        ret = reply.readInt();
        if(ret == CMD_RETURN_VALUE_SUCCESS) {
            version = reply.readString();
            Log.d(TAG,"GetPesiServiceVersion = " + version);
        }
        request.recycle();
        reply.recycle();
        return version;
    }

    public int MtestGetGPIOStatus(int u32GpioNo) {
        int ret;
        int bHighVolt = 0;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_GetGPIOStatus);
        request.writeInt(u32GpioNo);
        invokeex(request, reply);
        ret = reply.readInt();
        if(ret == CMD_RETURN_VALUE_SUCCESS) {
            bHighVolt = reply.readInt();
            Log.d(TAG,"MtestGetGPIOStatus = " + bHighVolt);
        }
        request.recycle();
        reply.recycle();
        return bHighVolt;
    }

    public int MtestSetGPIOStatus(int u32GpioNo,int bHighVolt) {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_SetGPIOStatus);
        request.writeInt(u32GpioNo);
        request.writeInt(bHighVolt);
        invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int MtestGetATRStatus(int smartCardStatus) {
        int ret;
        int status = -1;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_GetATRStatus);
        request.writeInt(smartCardStatus);
        invokeex(request, reply);
        ret = reply.readInt();
        if(ret == CMD_RETURN_VALUE_SUCCESS) {
            status = reply.readInt();
            Log.d(TAG,"MtestGetATRStatus = " + status);
        }
        request.recycle();
        reply.recycle();
        return status;
    }

    public int MtestGetHDCPStatus() {
        int ret;
        int status = -1;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_GetHDCPStatus);
        invokeex(request, reply);
        ret = reply.readInt();
        if(ret == CMD_RETURN_VALUE_SUCCESS) {
            status = reply.readInt();
            Log.d(TAG,"MtestGetHDCPStatus = " + status);
        }
        request.recycle();
        reply.recycle();
        return status;
    }

    public int MtestGetHDMIStatus() {
        int ret;
        int status = -1;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_GetHDMIStatus);
        invokeex(request, reply);
        ret = reply.readInt();
        if(ret == CMD_RETURN_VALUE_SUCCESS) {
            status = reply.readInt();
            Log.d(TAG,"MtestGetHDMIStatus = " + status);
        }
        request.recycle();
        reply.recycle();
        return status;
    }

    public int MtestPowerSave() {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_PowerSave);
        invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int MtestSevenSegment(int enable) {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_SevenSegment);
        request.writeInt(enable);
        invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int MtestSetAntenna5V(int tunerID, int tunerType, int enable) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIO_SetAntennaPower);

        request.writeInt(tunerID); // tuner id
        request.writeInt(tunerType); // tuner type
        request.writeInt(enable); // enable

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int MtestSetBuzzer(int enable) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIO_SetBuzzer);

        request.writeInt(enable); // enable

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int MtestSetLedRed(int enable) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIO_SetLedRed);

        request.writeInt(enable); // enable

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int MtestSetLedGreen(int enable) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIO_SetLedGreen);

        request.writeInt(enable); // enable

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int MtestSetLedOrange(int enable) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIO_SetLedOrange);

        request.writeInt(enable); // enable

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int MtestSetLedWhite(int enable) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIO_SetLedWhite);

        request.writeInt(enable); // enable

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int MtestSetLedOnOff(int status) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_SetLEDOnOff);
        Log.d(TAG, "MtestSetLedOnOff: "+ status);
        request.writeInt(status); // 0:off 1:green 2:red/orange

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);

    }

    public int MtestGetFrontKey(int key) {
        int key_value = 0;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_FrontKey);
        Log.d(TAG, "MtestGetFrontKey: "+ key);
        request.writeInt(key); // 0:off 1:green 2:red/orange
        invokeex(request, reply);
        if(reply.readInt() == CMD_RETURN_VALUE_SUCCESS)
        {
            Log.d(TAG, "key_value: "+ key_value);
            key_value = reply.readInt();
        }
        request.recycle();
        reply.recycle();
        return key_value;

    }


    public int MtestSetUsbPower(int enable) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIO_SetUsbPower);

        request.writeInt(enable); // enable

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int MtestTestUsbReadWrite(int portNum, String path) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_USB_READ_WRITE);

        request.writeInt(portNum); // portNum by Jim
        request.writeString(path); // path

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    // Johnny 20181221 for mtest split screen
    public int MtestTestAvMultiPlay(int tunerNum, List<Integer> tunerIDs, List<Long> channelIDs) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_AV_MultiPlay);

        request.writeInt(tunerNum); // total multiplay num, 1~4

        for (int i = 0 ; i < tunerNum ; i++) {
            request.writeInt(tunerIDs.get(i)); // tunerID
            request.writeInt((int)(long) channelIDs.get(i)); // channelID
        }

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int MtestTestAvStopByTunerID(int tunerID) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_AV_StopByTunerId);

        request.writeInt(tunerID);

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int MtestMicSetInputGain(int value) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_MIC_SetInputGain);

        request.writeInt(value); // value 0 ~ 7

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int MtestMicSetLRInputGain(int l_r, int value) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_MIC_SetLRInputGain);

        request.writeInt(l_r); // // l_r: 0 = L, 1 = R
        request.writeInt(value); // value 0 ~ 8

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int MtestMicSetAlcGain(int value) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_MIC_SetAlcGain);

        request.writeInt(value); // value 0 ~ 241

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int MtestGetErrorFrameCount(int tunerID) {
        int errorFrameCount = 0;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetVideoErrorFrameCount);

        request.writeInt(tunerID);

        invokeex(request, reply);
        if (CMD_RETURN_VALUE_SUCCESS == reply.readInt())
        {
//            Log.d(TAG, "MtestGetErrorFrameCount: success");
            errorFrameCount = reply.readInt();
        }
        request.recycle();
        reply.recycle();
//        Log.d(TAG, "MtestGetErrorFrameCount: Count = "+ errorFrameCount);
        return errorFrameCount;
    }

    public int MtestGetFrameDropCount(int tunerID) {
        int frameDropCount = 0;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_AV_GetVideoErrorFrameCount);

        request.writeInt(tunerID);

        invokeex(request, reply);
        if (CMD_RETURN_VALUE_SUCCESS == reply.readInt())
        {
//            Log.d(TAG, "MtestGetFrameDropCount: success");
            reply.readInt();
            frameDropCount = reply.readInt();
        }
        request.recycle();
        reply.recycle();
//        Log.d(TAG, "MtestGetFrameDropCount: Count = "+ frameDropCount);
        return frameDropCount;
    }

    public String MtestGetChipID() {
        int[] chipID = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
        StringBuilder strBuilderChipID = new StringBuilder();
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_CFG_GetChipID);

        invokeex(request, reply);

        if (CMD_RETURN_VALUE_SUCCESS == reply.readInt())
        {
            Log.d(TAG, "MtestGetChipID: success");
            chipID[0] = reply.readInt();
            chipID[1] = reply.readInt();
            chipID[2] = reply.readInt();
            chipID[3] = reply.readInt();
            chipID[4] = reply.readInt();
            chipID[5] = reply.readInt();
            chipID[6] = reply.readInt();
            chipID[7] = reply.readInt();

            boolean ignore = true;
            for (int i = 0 ; i < chipID.length ; i++) {
                if (ignore && i < 4 && chipID[i] == 0) { // try to ignore pre 4 ch if it is 0
                    continue;
                }

                strBuilderChipID.append(String.format("%02X", chipID[i]));
                ignore = false;
            }
        }
        request.recycle();
        reply.recycle();
        Log.d(TAG, "MtestGetChipID:  = " + strBuilderChipID.toString());
        return strBuilderChipID.toString();
    }

    public int MtestStartMtest(String version) {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_START_MTEST);

        request.writeString(version); // Johnny 20190909 send mtest apk version to service

        invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int MtestConnectPctool() {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_CONNECT_PCTOOL);
        invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public List<Integer> MtestGetWiFiTxRxLevel(){//Scoty 20190417 add wifi level command
        int ret;
        List<Integer> list = new ArrayList<>();
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_WIFI_TX_RX_LEVEL);
        invokeex(request, reply);

        ret = reply.readInt();
        if(CMD_RETURN_VALUE_SUCCESS == ret)
        {
            list.add(reply.readInt());//RF1
            list.add(reply.readInt());//RF0
        }
        request.recycle();
        reply.recycle();
        return list;
    }

    public int MtestGetWakeUpMode() {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_DEVICE_INFO_WAKEUP_MODE);
        invokeex(request, reply);

        ret = reply.readInt();
        if(CMD_RETURN_VALUE_SUCCESS == ret) {
            ret = reply.readInt();
        }

        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    // Johnny 20190522 check key before OTA
    public Map<String, Integer> MtestGetKeyStatusMap() {
        int ret;
        Map<String, Integer> keyStatusMap = new ArrayMap<>();

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_CHECK_KEY);
        invokeex(request, reply);

        ret = reply.readInt();
        if(CMD_RETURN_VALUE_SUCCESS == ret) {
            keyStatusMap.put("HDCP1.4 Key", reply.readInt()); // hdcp1.4
            keyStatusMap.put("Widevine Key", reply.readInt()); // Widevine
            keyStatusMap.put("Attestation Key", reply.readInt()); // Attestation
            keyStatusMap.put("Playready Key", reply.readInt()); // Playready
        }
        else {
            keyStatusMap.put("HDCP1.4 Key", -1); // hdcp1.4
            keyStatusMap.put("Widevine Key", -1); // Widevine
            keyStatusMap.put("Attestation Key", -1); // Attestation
            keyStatusMap.put("Playready Key", -1); // Playready
        }

        request.recycle();
        reply.recycle();
        return keyStatusMap;
    }

    public int TestSetTvRadioCount(int tvCount, int RadioCout) {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        Log.d(TAG, "TestSetTvRadioCount: ==>>> IN");
        request.writeInt(CMD_TEST_SET_CH);
        request.writeInt(tvCount);
        request.writeInt(RadioCout);
        invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int TestChangeTuner(int tunerTpe)//Scoty 20180817 add Change Tuner Command
    {
        Log.d(TAG, "TestChangeTuner: tunerTpe = " + tunerTpe);
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_TEST_CHANGE_TUNER);
        request.writeInt(tunerTpe);
        invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }
    ///////// for pesi middleware---pesimain /////////
    //PIP -start
    public int PipOpen(int x, int y, int width, int height)
    {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIP_OPEN);
        request.writeInt(x);
        request.writeInt(y);
        request.writeInt(width);
        request.writeInt(height);
        invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int PipClose()
    {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIP_CLOSE);
        invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int PipStart(long channelId, int show)
    {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIP_START);
        request.writeInt((int) channelId);
        request.writeInt(show);
        invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int PipStop()
    {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIP_STOP);
        invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int PipSetWindow(int x, int y, int width, int height)
    {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIP_SET_WINSIZE);
        request.writeInt(x);
        request.writeInt(y);
        request.writeInt(width);
        request.writeInt(height);
        invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int PipExChange()
    {
        int ret;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PIP_EXCHANGE);
        invokeex(request, reply);
        ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }
    //PIP -end

    //Device -start
    public void SetUsbPort()
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_DEVICE_INFO_USB_PORT);
        invokeex(request, reply);
        int ret = reply.readInt();
        if(ret == CMD_RETURN_VALUE_SUCCESS)
        {
            usbPort = new ArrayList<>();
            int usbNum = reply.readInt();
            Log.d(TAG, "SetUsbPort: " + usbNum);
            for(int i = 0 ; i < usbNum ; i++)
            {
                usbPort.add(reply.readInt());
                Log.d(TAG, "SetUsbPort: " + usbPort.get(i));
            }

        }

        request.recycle();
        reply.recycle();
    }

    public List<Integer> GetUsbPortList()
    {
        return usbPort;
    }
    //Device -ecd
    ///////// for pesi get channel list when init or channel list update //////////
    public List<SimpleChannel> GetCurPlayChannelList(int type, int IncludePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
//        List<SimpleChannel> simpleChannelList = null;
//        if(type >= ProgramInfo.ALL_TV_TYPE && type < ProgramInfo.ALL_TV_RADIO_TYPE_MAX) {//eric lin 20180528 add type >= ProgramInfo.ALL_TV_TYPE condition
//            if(TotalChannelList.get(type)!=null)
//                Log.d(TAG, "GetCurPlayChannelList:  totalList   " + type + "   size = " + TotalChannelList.get(type).size());
//            if(TotalChannelList != null && TotalChannelList.get(type) != null && TotalChannelList.get(type).size() > 0)
//                simpleChannelList = new ArrayList<>();
//            else
//                return null;
//            for(int i = 0; i < TotalChannelList.get(type).size(); i++) {
//                    simpleChannelList.add(TotalChannelList.get(type).get(i));
//            }
//        }
//        return simpleChannelList;
        if(TotalChannelList.get(type)!=null)
            return TotalChannelList.get(type);
        else
            return null;
    }

    public int GetCurPlayChannelListCnt(int type) {//eric lin 20180802 check program exist
        int cnt=0;
        if(type >= ProgramInfo.ALL_TV_TYPE && type < ProgramInfo.ALL_TV_RADIO_TYPE_MAX) {
            cnt = TotalChannelList.get(type).size();
            //Log.d(TAG, "GetCurPlayChannelListCnt: type=" + type + "   size=" + cnt);
        }
        return cnt;
    }

    public void ResetTotalChannelList() {
        if (TotalChannelList.size() != 0) {
            for(int i=0; i< TotalChannelList.size(); i++){//eric lin 20180802 check program exist
                TotalChannelList.get(i).clear();
            }
        }
    }

    private SimpleChannel UpdateNetprogramEpgData(int index, SimpleChannel netProgramInfo)
    {
        if (netProgramInfo.getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE) {//VOD
            EPGEvent presentepgEvent = new EPGEvent();
            presentepgEvent.setEventId(parseInt(mContext.getResources().getString(R.string.vod_present_event_id)) + index);
            presentepgEvent.setEventName(mContext.getResources().getString(R.string.vod_present_event_name) + " - " + index);
            presentepgEvent.setEventType(EPGEvent.EPG_TYPE_PRESENT);

            //Set Current Time
            Date date = new Date(); //取时间
            Calendar calendar = Calendar.getInstance();
            long tmpCurTime = getDtvDate().getTime()-60000;// date.getTime();
            presentepgEvent.setStartTime(tmpCurTime);
            calendar.add(calendar.DATE,7); //把日期往后增加一天,整数  往后推,负数往前移动
            date=calendar.getTime(); //这个时间就是日期往后推一天的结果
            presentepgEvent.setEndTime(date.getTime());
            presentepgEvent.setDuration(date.getTime()-tmpCurTime);


            netProgramInfo.setPresentepgEvent(presentepgEvent);
            netProgramInfo.setShortEvent(mContext.getResources().getString(R.string.vod_present_event_name) + index);
            netProgramInfo.setDetailInfo(mContext.getResources().getString(R.string.vod_present_event_name) + index);

            EPGEvent followepgEvent = new EPGEvent();
            followepgEvent.setEventId(parseInt(mContext.getResources().getString(R.string.vod_follow_event_id)) + index);
            followepgEvent.setEventName(mContext.getResources().getString(R.string.vod_follow_event_name) + " - " + index);
            followepgEvent.setEventType(EPGEvent.EPG_TYPE_FOLLOW);

            netProgramInfo.setFollowepgEvent(followepgEvent);
            netProgramInfo.setShortEvent(mContext.getResources().getString(R.string.vod_follow_event_name) + " - " + index);
            netProgramInfo.setDetailInfo(mContext.getResources().getString(R.string.vod_follow_event_name) + " - " + index);

        } else//YOUTUBE
        {
            EPGEvent presentepgEvent = new EPGEvent();
            presentepgEvent.setEventId(parseInt(mContext.getResources().getString(R.string.youtube_present_event_id)) + index);
            presentepgEvent.setEventName(mContext.getResources().getString(R.string.youtube_present_event_name) + " - " + index);
            presentepgEvent.setEventType(EPGEvent.EPG_TYPE_PRESENT);

            //Set Current Time
            Date date = new Date(); //取时间
            Calendar calendar = Calendar.getInstance();
            long tmpCurTime = getDtvDate().getTime();//date.getTime();
            presentepgEvent.setStartTime(tmpCurTime);
            calendar.add(calendar.DATE,7); //把日期往后增加一天,整数  往后推,负数往前移动
            date=calendar.getTime(); //这个时间就是日期往后推一天的结果
            presentepgEvent.setEndTime(date.getTime());
            presentepgEvent.setDuration(date.getTime()-tmpCurTime);

            netProgramInfo.setPresentepgEvent(presentepgEvent);
            netProgramInfo.setShortEvent( mContext.getResources().getString(R.string.youtube_present_event_name) + index);
            netProgramInfo.setDetailInfo(mContext.getResources().getString(R.string.youtube_present_event_name) + index);

            EPGEvent followepgEvent = new EPGEvent();
            followepgEvent.setEventId(parseInt(mContext.getResources().getString(R.string.youtube_follow_event_id)) + index);
            followepgEvent.setEventName(mContext.getResources().getString(R.string.youtube_follow_event_name) + " - " + index);
            followepgEvent.setEventType(EPGEvent.EPG_TYPE_FOLLOW);
            netProgramInfo.setFollowepgEvent(followepgEvent);
            netProgramInfo.setShortEvent(mContext.getResources().getString(R.string.youtube_follow_event_name) + " - " + index);
            netProgramInfo.setDetailInfo(mContext.getResources().getString(R.string.youtube_follow_event_name) + " - " + index);
        }

        return netProgramInfo;
    }

    public void UpdateCurPlayChannelList(Context context, int IncludePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        Log.e(TAG, "exce HIUpdateCurPlayChannelList Net first");
        if (TotalChannelList.size() != 0)
            TotalChannelList.clear();

        DataManager mDataManager = DataManager.getDataManager();
//        DataManager.ProgramDatabase mProgramDatabase = mDataManager.GetProgramDatabase();
        DataManager.NetProgramDatabase mNetProgramDatabase = mDataManager.GetNetProgramDatabase();

        for( int i = 0; i < ProgramInfo.ALL_TV_RADIO_TYPE_MAX;i++)
        {
            ////need change to get simple channel list form service to compatible with now version
            List<SimpleChannel> simpleChannelList = GetSimpleProgramList(i,0,IncludePVRSkipFlag);//Get ProgramInfo List from Server
//            List<SimpleChannel> simpleChannelList = mProgramDatabase.GetSimpleChannelList(context); //Get ProgramInfo List from ProgramInfo Database
            List<SimpleChannel> netSimpleChannelList = mNetProgramDatabase.GetNetSimpleChannelList(context);
            if(simpleChannelList.size() > 0 && i < ProgramInfo.ALL_RADIO_TYPE) {//No Considering Radio Channel
                Log.d(TAG, "exce UpdateCurPlayChannelList: netprogram.Size = [" + netSimpleChannelList.size()+"]");
                for (int j = 0; j < netSimpleChannelList.size(); j++) {
                    simpleChannelList.add(UpdateNetprogramEpgData(j,netSimpleChannelList.get(j)));//add vod/youtube netprogram after program
                }
            }
            TotalChannelList.add(simpleChannelList);
        }

        for(int ii = 0 ; ii < TotalChannelList.size() ; ii++)
            for(int j = 0 ; j < TotalChannelList.get(ii).size() ; j++)
                Log.d(TAG, "exce HIUpdateCurPlayChannelList: i = ["+ii+"] "
                        + "= ["+ TotalChannelList.get(ii).get(j).getChannelName()
                        + "] = ["+ TotalChannelList.get(ii).get(j).getChannelId()
                        + "] = ["+ TotalChannelList.get(ii).get(j).getPlayStreamType()
                        + "] = ["+ TotalChannelList.get(ii).get(j).getChannelNum()
                        +"]");

    }
    //Scoty Add ProgramInfo and NetProgramInfo Get TotalChannelList -e

    public void UpdateCurPlayChannelList(int IncludePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        Log.e(TAG, "UpdateCurPlayChannelList first");
        if(TotalChannelList.size() !=0)
            TotalChannelList.clear();
        for( int i = 0; i < ProgramInfo.ALL_TV_RADIO_TYPE_MAX;i++)
        {
            List<SimpleChannel> simpleChannelList = GetSimpleProgramList(i,0,IncludePVRSkipFlag);
            if (simpleChannelList != null) {
                Log.d(TAG, "UpdateCurPlayChannelList:    TotalChannelList  group " + i + "    size = " + simpleChannelList.size());
                TotalChannelList.add(simpleChannelList);//Scoty 20180615 recover get simple channel list function
            }
            else {
                Log.d(TAG, "UpdateCurPlayChannelList:    TotalChannelList  group " + i + " is NULL !!!!!!!!!!");
                TotalChannelList.add(new ArrayList<SimpleChannel>()); // Johnny 20210414 add empty list to prevent null pointer exception
            }
        }
    }

    public int getPlatform()
    {
        return PLATFORM;
    }

    public int getTempPesiDefaultChannelFlag()
    {
        return tempPesiDefaultChannelFlag;
    }

    public void setTempPesiDefaultChannelFlag(int flag)
    {
        tempPesiDefaultChannelFlag = flag;
    }

    public String GetApkSwVersion()
    {
        return apkSwVersion;
    }

    public int GetTunerNum()//Scoty 20181113 add GetTunerNum function
    {
        return TUNER_NUM;
    }
    //Scoty 20180809 modify dual pvr rule -s//Scoty 20180615 update TV/Radio TotalChannelList -s
    public void UpdatePvrSkipList(int groupType, int IncludePVRSkipFlag, int tpId, List<Integer> pvrTpList)//Scoty 20181113 add for dual tuner pvrList
    {
        for(int i = 0 ; i < TotalChannelList.get(groupType).size() ; i++)
        {
            if((IncludePVRSkipFlag == 0) ||
                    (pvrTpList != null && checkSameTp(groupType,TotalChannelList.get(groupType).get(i).getTpId(),pvrTpList)))//Scoty 20181113 add for dual tuner pvrList
            {
                TotalChannelList.get(groupType).get(i).setPVRSkip(0);
            }
            else
            {
                TotalChannelList.get(groupType).get(i).setPVRSkip(1);
            }
        }
    }

    private SimpleChannel GetPlaySimpleChannelByChannelId(int groupType,long channelId)
    {
        int size = TotalChannelList.get(groupType).size();
        for(int i = 0 ; i < size; i++)
        {
            if(TotalChannelList.get(groupType).get(i).getChannelId() == channelId)
                return TotalChannelList.get(groupType).get(i);
        }
        return null;
    }

    private boolean checkSameTp(int groupType, int tpId, List<Integer> pvrTpList)//Scoty 20181113 add for dual tuner pvrList
    {
        if(pvrTpList == null || (pvrTpList != null && pvrTpList.size() == 0))
            return false;

        for(int i = 0 ; i < pvrTpList.size() ; i++)
        {
            if(tpId == pvrTpList.get(i))
                return true;
        }

        return false;
    }
    //Scoty 20180809 modify dual pvr rule -e//Scoty 20180615 update TV/Radio TotalChannelList -e
    
    ///////// for pesi get channel list when init or channel list update //////////


    // pvr -start
    public int Record_Start_V2_with_Duration(long channelId, int durationSec, boolean doCipher, PVREncryption pvrEncryption)
    {
        Log.d(TAG, "Record_Start_V2_with_Duration(" + channelId + "," + durationSec + "," + doCipher + ")");
        int recID      = CMD_RETURN_VALUE_FAILAR;
        Parcel request = Parcel.obtain();
        Parcel reply   = Parcel.obtain();

        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Record_Start_V2);
        request.writeInt((int) channelId);
        request.writeInt(durationSec); // duration (second)
        request.writeInt(0); // file size (MByte)

        if (doCipher && pvrEncryption != null)
        {
            int keyLength = pvrEncryption.getPvrKey().length();
            int keyType   = pvrEncryption.getPvrEncryptionType();
            String key    = pvrEncryption.getPvrKey();

            if (keyLength > HI_SVR_PVR_MAX_CIPHER_KEY_LEN)
                keyLength = HI_SVR_PVR_MAX_CIPHER_KEY_LEN;

            request.writeInt(1); // do cipher
            request.writeInt(keyType);
            request.writeInt(keyLength);

            for (int i = 0; i < keyLength; i++)
            {
                char cKey = key.charAt(i);
                request.writeInt((int)cKey);
            }
        }
        else
        {
            request.writeInt(0); // no cipher
        }

        invokeex(request, reply);

        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            recID = reply.readInt();
        }
        Log.d(TAG, "Record_Start_V2_with_Duration: ret = " + ret + " , recID = " + recID);

        request.recycle();
        reply.recycle();
        return recID;
    }

    public int Record_Start_V2_with_FileSize(long channelId, int fileSizeMB, boolean doCipher, PVREncryption pvrEncryption)
    {
        Log.d(TAG, "Record_Start_V2_with_FileSize(" + channelId + "," + fileSizeMB + "," + doCipher + ")");
        int recID      = CMD_RETURN_VALUE_FAILAR;
        Parcel request = Parcel.obtain();
        Parcel reply   = Parcel.obtain();

        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Record_Start_V2);
        request.writeInt((int) channelId);
        request.writeInt(0); // duration (second)
        request.writeInt(fileSizeMB); // file size (MByte)

        if (doCipher && pvrEncryption != null)
        {
            int keyLength = pvrEncryption.getPvrKey().length();
            int keyType   = pvrEncryption.getPvrEncryptionType();
            String key    = pvrEncryption.getPvrKey();

            if (keyLength > HI_SVR_PVR_MAX_CIPHER_KEY_LEN)
                keyLength = HI_SVR_PVR_MAX_CIPHER_KEY_LEN;

            request.writeInt(1); // do cipher
            request.writeInt(keyType);
            request.writeInt(keyLength);

            for (int i = 0; i < keyLength; i++)
            {
                char cKey = key.charAt(i);
                request.writeInt((int)cKey);
            }
        }
        else
        {
            request.writeInt(0); // no cipher
        }

        invokeex(request, reply);

        if (CMD_RETURN_VALUE_SUCCESS == reply.readInt())
        {
            recID = reply.readInt();
        }
        Log.d(TAG, "Record_Start_V2_with_FileSize: retID = " + recID);

        request.recycle();
        reply.recycle();
        return recID;
    }

    public int TimeShift_Start_V2(int durationSec, int fileSizeMB, boolean doCipher, PVREncryption pvrEncryption)
    {
        Log.d(TAG, "TimeShift_Start_V2(" + durationSec + "," + fileSizeMB + "," + doCipher + ")");
        Parcel request = Parcel.obtain();
        Parcel reply   = Parcel.obtain();

        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_TimeShift_Start_V2);
        request.writeInt(durationSec); // duration (second)
        request.writeInt(fileSizeMB); // file size (MByte)

        if (doCipher && pvrEncryption != null)
        {
            int keyLength = pvrEncryption.getPvrKey().length();
            int keyType   = pvrEncryption.getPvrEncryptionType();
            String key    = pvrEncryption.getPvrKey();

            if (keyLength > HI_SVR_PVR_MAX_CIPHER_KEY_LEN)
                keyLength = HI_SVR_PVR_MAX_CIPHER_KEY_LEN;

            request.writeInt(1); // do cipher
            request.writeInt(keyType);
            request.writeInt(keyLength);

            for (int i = 0; i < keyLength; i++)
            {
                char cKey = key.charAt(i);
                request.writeInt((int)cKey);
            }
        }
        else
        {
            request.writeInt(0); // no cipher
        }

        invokeex(request, reply);

        int ret = reply.readInt();
        Log.d(TAG, "TimeShift_Start_V2: ret = " + ret);

        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public List<PvrFileInfo> Pvr_Get_Records_File(int startIndex, int total)
    {
        Log.d(TAG, "Pvr_Get_Records_File(" + startIndex + "," + total + ")");
        List<PvrFileInfo> pvrFileInfoList = new ArrayList<>();
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Get_Records_File);
        request.writeInt(startIndex);
        request.writeInt(total);
        invokeex(request, reply);

        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            for (int i = 0; i < total; i++)
            {
                PvrFileInfo fileInfo = new PvrFileInfo();
                fileInfo.recordUniqueId = reply.readString();
                fileInfo.isSeries = (reply.readInt() == 1);
                fileInfo.episode = reply.readInt();
                fileInfo.channelNumber = reply.readInt();
                fileInfo.channelLock = reply.readInt();
                fileInfo.parentalRate = reply.readInt();
                fileInfo.recordStatus = reply.readInt();
                fileInfo.channelId = reply.readInt();
                fileInfo.year = reply.readInt();
                fileInfo.month = reply.readInt();
                fileInfo.date = reply.readInt();
                fileInfo.week = reply.readInt();
                fileInfo.hour = reply.readInt();
                fileInfo.minute = reply.readInt();
                fileInfo.second = reply.readInt();
                fileInfo.durationInMs = reply.readInt();
                fileInfo.serviceType = reply.readInt();
                fileInfo.fileSize = reply.readLong();
                fileInfo.channelName = reply.readString();
                fileInfo.eventName = reply.readString();
                fileInfo.fullNamePath = reply.readString();

                fileInfo.durationSec = fileInfo.durationInMs / 1000;
                if (fileInfo.fullNamePath != null)
                {
                    String[] splitName = fileInfo.fullNamePath.split("/");
                    int index = splitName.length - 1;
                    if (index >= 0)
                        fileInfo.realFileName = splitName[index];
                }
                pvrFileInfoList.add(fileInfo);
            }
        }
        Log.d(TAG, "Pvr_Get_Records_File: ret = " + ret);

        request.recycle();
        reply.recycle();
        return pvrFileInfoList;
    }

    public List<PvrFileInfo> Pvr_Get_Total_One_Series_Records_File(int startIndex, int total, String recordUniqueId)
    {
        Log.d(TAG, "Pvr_Get_Total_One_Series_Records_File: (" + startIndex + "," + total + "," + recordUniqueId + ")");
        List<PvrFileInfo> pvrFileInfoList = new ArrayList<>();
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Get_One_Series_Records_File);
        request.writeInt(startIndex);
        request.writeInt(total);
        request.writeString(recordUniqueId);
        invokeex(request, reply);

        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            for (int i = 0; i < total; i++)
            {
                PvrFileInfo fileInfo = new PvrFileInfo();
                fileInfo.recordUniqueId = reply.readString();
                fileInfo.isSeries = (reply.readInt() == 1);
                fileInfo.episode = reply.readInt();
                fileInfo.channelNumber = reply.readInt();
                fileInfo.channelLock = reply.readInt();
                fileInfo.parentalRate = reply.readInt();
                fileInfo.recordStatus = reply.readInt();
                fileInfo.channelId = reply.readInt();
                fileInfo.year = reply.readInt();
                fileInfo.month = reply.readInt();
                fileInfo.date = reply.readInt();
                fileInfo.week = reply.readInt();
                fileInfo.hour = reply.readInt();
                fileInfo.minute = reply.readInt();
                fileInfo.second = reply.readInt();
                fileInfo.durationInMs = reply.readInt();
                fileInfo.durationSec = fileInfo.durationInMs / 1000;
                fileInfo.serviceType = reply.readInt();
                fileInfo.fileSize = reply.readLong();
                fileInfo.channelName = reply.readString();
                fileInfo.eventName = reply.readString();
                fileInfo.fullNamePath = reply.readString();
                if (fileInfo.fullNamePath != null)
                {
                    String[] splitName = fileInfo.fullNamePath.split("/");
                    int index = splitName.length - 1;
                    if (index >= 0)
                        fileInfo.realFileName = splitName[index];
                }
                pvrFileInfoList.add(fileInfo);
            }
        }
        Log.d(TAG, "Pvr_Get_Total_One_Series_Records_File: ret = " + ret);

        request.recycle();
        reply.recycle();
        return pvrFileInfoList;
    }

    public int Pvr_Get_Total_Rec_Num()
    {
        Log.d(TAG, "Pvr_Get_Total_Rec_Num");
        int totalFileNumber = CMD_RETURN_VALUE_FAILAR;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Get_Total_Rec_Num);
        invokeex(request, reply);

        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            totalFileNumber = reply.readInt();
        }
        Log.d(TAG, "Pvr_Get_Total_Rec_Num: ret = " + ret + " , totalFileNumber = " + totalFileNumber);

        request.recycle();
        reply.recycle();
        return totalFileNumber;
    }

    public int Pvr_Get_Total_One_Series_Rec_Num(String recordUniqueId)
    {
        Log.d(TAG, "Pvr_Get_Total_One_Series_Rec_Num(" + recordUniqueId + ")");
        int totalFileNumber = CMD_RETURN_VALUE_FAILAR;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Get_Total_One_Series_Rec_Num);
        request.writeString(recordUniqueId);
        invokeex(request, reply);

        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            totalFileNumber = reply.readInt();
        }
        Log.d(TAG, "Pvr_Get_Total_One_Series_Rec_Num: ret = " + ret + " , totalFileNumber = " + totalFileNumber);

        request.recycle();
        reply.recycle();
        return totalFileNumber;
    }

    public int Pvr_Delete_Total_Records_File()
    {
        Log.d(TAG, "Pvr_Delete_Total_Records_File");
        int ret = CMD_RETURN_VALUE_FAILAR;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Delete_Total_Records_File);
        invokeex(request, reply);

        ret = reply.readInt();
        Log.d(TAG, "Pvr_Delete_Total_Records_File: ret = " + ret);

        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int Pvr_Delete_One_Series_Folder(String recordUniqueId)
    {
        Log.d(TAG, "Pvr_Delete_One_Series_Folder(" + recordUniqueId + ")");
        int ret = CMD_RETURN_VALUE_FAILAR;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Delete_One_Series_Folder);
        request.writeString(recordUniqueId);
        invokeex(request, reply);

        ret = reply.readInt();
        Log.d(TAG, "Pvr_Delete_One_Series_Folder: ret = " + ret);

        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int Pvr_Delete_Record_File_By_Ch_Id(int channelId)
    {
        Log.d(TAG, "Pvr_Delete_Record_File_By_Ch_Id(" + channelId + ")");
        int ret = CMD_RETURN_VALUE_FAILAR;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Delete_Record_File_By_Ch_Id);
        request.writeInt(channelId);
        invokeex(request, reply);

        ret = reply.readInt();
        Log.d(TAG, "Pvr_Delete_Record_File_By_Ch_Id: ret = " + ret);

        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int Pvr_Delete_Record_File(String recordUniqueId)
    {
        Log.d(TAG, "Pvr_Delete_Record_File(" + recordUniqueId + ")");
        int ret = CMD_RETURN_VALUE_FAILAR;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Delete_Record_File);
        request.writeString(recordUniqueId);
        invokeex(request, reply);

        ret = reply.readInt();
        Log.d(TAG, "Pvr_Delete_Record_File: ret = " + ret);

        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int pvrRecordStart(int pvrPlayerID , long channelID, String recordPath, int duration)
    {
        Log.d(TAG, "pvrRecordStart(" + channelID + "," + recordPath + "," + duration + "," + ")");
        int recid = CMD_RETURN_VALUE_FAILAR;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Record_Start);
        //request.writeInt(pvrPlayerID);
        request.writeInt((int) channelID);
        request.writeInt(duration);
        request.writeInt(0);// file length
        request.writeString(recordPath);
        request.writeInt(0);// no cipher
        invokeex(request, reply);
        if (CMD_RETURN_VALUE_SUCCESS == reply.readInt())
        {
            recid = reply.readInt();
            Log.d(TAG, "pvrRecordStart:  recid = " + recid);
        }
        request.recycle();
        reply.recycle();
        return recid;
    }

    public int pvrRecordStart(int pvrPlayerID , long channelID, String recordPath, int duration, PVREncryption pvrEncryption)
    {
        Log.d(TAG, "recordStart(" + channelID + "," + recordPath + "," + duration + "," + ")");
        int recid = CMD_RETURN_VALUE_FAILAR;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int length = pvrEncryption.getPvrKey().length();
        String strPvrKey = pvrEncryption.getPvrKey();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Record_Start);
        //request.writeInt(pvrPlayerID);
        request.writeInt((int) channelID);
        request.writeInt(duration);
        request.writeInt(256);// file length
        request.writeString(recordPath);
        request.writeInt(1);//  cipher
        //request.writeInt(1);//  andorid
        request.writeInt(pvrEncryption.getPvrEncryptionType());
        if (length > HI_SVR_PVR_MAX_CIPHER_KEY_LEN)
        {
            length = HI_SVR_PVR_MAX_CIPHER_KEY_LEN;
        }
        request.writeInt(length);

        for (int i = 0; i < length; i++)
        {
            char cKey = strPvrKey.charAt(i);
            request.writeInt((int)cKey);
        }
        //request.writeString(pvrEncryption.getPvrKey());

        invokeex(request, reply);
        if (CMD_RETURN_VALUE_SUCCESS == reply.readInt())
        {
            recid = reply.readInt();
            Log.d(TAG, "pvrRecordStart:  recid = " + recid);
        }
        request.recycle();
        reply.recycle();
        return recid;
    }

    public int pvrRecordStop(int pvrPlayerID, int recId)
    {
        Log.d(TAG, "stopRecord() recId="+recId);
        //return excuteCommand(CMD_PVR_Record_Stop,pvrPlayerID);
        return excuteCommand(CMD_PVR_Record_Stop, recId);
    }

    public int pvrRecordGetAlreadyRecTime(int pvrPlayerID, int recId)
    {
        Log.d(TAG, "getAlreadyRecTime() recId="+recId);
        //return excuteCommand(CMD_PVR_Record_GetRecTime,pvrPlayerID);
        return excuteCommandGetII(CMD_PVR_Record_GetRecTime, recId) / 1000;
    }

    public int pvrRecordGetStatus(int pvrPlayerID, int recId)
    {
        Log.d(TAG, "pvrRecordGetStatus() recId="+recId);
        //return excuteCommand(CMD_PVR_Record_GetInfo, pvrPlayerID);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
//        request.writeInt(CMD_PVR_Record_GetInfo);
//        request.writeInt(0);
        request.writeInt(CMD_PVR_Record_GetInfo_TypeStatus);
        request.writeInt(recId);
        invokeex(request, reply);

        int ret = reply.readInt();
        int status = CMD_RETURN_VALUE_FAILAR;
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            status = reply.readInt();
        }

        request.recycle();
        reply.recycle();
        return status;
    }

    public String pvrRecordGetFileFullPath(int pvrPlayerID, int recId)
    {
        Log.d(TAG, "pvrRecordGetFileFullPath() recId="+recId);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Record_GetInfo_TypeFileFullpath);
        request.writeInt(recId);
        invokeex(request, reply);

        int ret = reply.readInt();
        String fullPath = null;
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            fullPath = reply.readString();
        }

        request.recycle();
        reply.recycle();
        return fullPath;
    }

    public int pvrRecordGetProgramId(int pvrPlayerID, int recId)
    {
        Log.d(TAG, "pvrRecordGetProgramId() recId="+recId);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Record_GetInfo_TypeProginfo);
        request.writeInt(recId);
        invokeex(request, reply);

        int ret = reply.readInt();
        int progId = CMD_RETURN_VALUE_FAILAR;
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            progId = reply.readInt();
        }

        request.recycle();
        reply.recycle();
        return progId;
    }

    public int pvrTimeShiftStart(int playerID, int time, int filesize, String filePath)
    {
        Log.d(TAG, "pvrTimeShiftStart time = " + time + "filesize = " + filesize+ "filePath = " + filePath);
        //return excuteCommand(CMD_PVR_TimeShift_Start, time, filesize, 0);

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_TimeShift_Start);
        request.writeInt(time);
        request.writeInt(filesize);
        request.writeInt(0);
        request.writeString(filePath);
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int pvrTimeShiftStop(int playerID)
    {
        Log.d(TAG, "stopTimeShift");
        return excuteCommand(CMD_PVR_TimeShift_Stop, 1);
    }

    public int pvrTimeShiftPlayForLiveChannel(int pvrMode) //Edwin 20181022 TimeShift for Live Channel
    {
        Log.d(TAG, "pvrTimeShiftPlay");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_TimeShift_GetLivePauseTime);
        invokeex(request, reply);
        int ret = reply.readInt();
        int playTime = CMD_RETURN_VALUE_FAILAR;
        if(ret == CMD_RETURN_VALUE_SUCCESS)
        {
            playTime = reply.readInt();
        }
        Log.d(TAG, "pvrTimeShiftPlay: playTime = " + playTime + " pvrMode = "+pvrMode);

        if( pvrMode == PvrInfo.EnPVRMode.PVRMODE_TIMESHIFT_LIVE_PAUSE )
        {
            ret = excuteCommand(CMD_PVR_TimeShift_Play, 0);
        }
        else
        {
            ret = excuteCommand(CMD_PVR_TimeShift_Resume, 0);
        }

        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int pvrTimeShiftPlay(int playerID)
    {
        Log.d(TAG, "pvrTimeShiftPlay");
        //Scoty 20180827 add and modify TimeShift Live Mode -s
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_TimeShift_GetLivePauseTime);
        invokeex(request, reply);
        int ret = reply.readInt();
        int playTime = CMD_RETURN_VALUE_FAILAR;
        if(ret == CMD_RETURN_VALUE_SUCCESS)
        {
            playTime = reply.readInt();
        }
        Log.d(TAG, "pvrTimeShiftPlay: playTime = " + playTime);
        if(playTime == 0)
            {
                ret = excuteCommand(CMD_PVR_TimeShift_Play, 0);
            }
            else
            {
                ret = excuteCommand(CMD_PVR_TimeShift_Resume, 0);
            }

        request.recycle();
        reply.recycle();
        return getReturnValue(ret);

//        Parcel request1 = Parcel.obtain();
//        Parcel reply1 = Parcel.obtain();
//        request1.writeString(DTV_INTERFACE_NAME);
//        request1.writeInt(CMD_PVR_TimeShift_GetInfo_TypeStatus);
////        request1.writeInt(CMD_PVR_TimeShift_GetInfo);
////        request1.writeInt(0); // 0 means HI_SVR_PVR_TIMESHIFT_INFO_TYPE_STATUS
//
//        invokeex(request1, reply1);
//        int ret = reply1.readInt();
//        if (CMD_RETURN_VALUE_SUCCESS == ret)
//        {
//            int status = reply1.readInt();
//            Log.d(TAG, "GetPlayStatus(), status = " + status);
//
//            if (status == EnPVRTimeShiftStatus.START)//HI_SVR_PVR_TIMESHIFT_STATE_START
//            {
//                ret = excuteCommand(CMD_PVR_TimeShift_Play, 0);
//            }
//            else
//            {
//                ret = excuteCommand(CMD_PVR_TimeShift_Resume, 0);
//            }
//        }
//        request1.recycle();
//        reply1.recycle();
//        return getReturnValue(ret);
        //Scoty 20180827 add and modify TimeShift Live Mode -e
    }

    public int pvrTimeShiftResume(int playerID)//Scoty 20181106 add for separate Play and Pause key
    {
        Parcel request1 = Parcel.obtain();
        Parcel reply1 = Parcel.obtain();
        request1.writeString(DTV_INTERFACE_NAME);
        request1.writeInt(CMD_PVR_TimeShift_GetInfo_TypeStatus);
//        request1.writeInt(CMD_PVR_TimeShift_GetInfo);
//        request1.writeInt(0); // 0 means HI_SVR_PVR_TIMESHIFT_INFO_TYPE_STATUS

        invokeex(request1, reply1);
        int ret = reply1.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int status = reply1.readInt();
            Log.d(TAG, "pvrTimeShiftResume GetPlayStatus(), status = " + status);
            if (status != EnPVRTimeShiftStatus.PLAY &&
                    status != EnPVRTimeShiftStatus.STOP)
                ret = excuteCommand(CMD_PVR_TimeShift_Resume, 0);

        }
        request1.recycle();
        reply1.recycle();
        return getReturnValue(ret);
    }

    public int pvrTimeShiftPause(int playerID)//Scoty 20181106 add for separate Play and Pause key
    {
        Parcel request1 = Parcel.obtain();
        Parcel reply1 = Parcel.obtain();
        request1.writeString(DTV_INTERFACE_NAME);
        request1.writeInt(CMD_PVR_TimeShift_GetInfo_TypeStatus);
//        request1.writeInt(CMD_PVR_TimeShift_GetInfo);
//        request1.writeInt(0); // 0 means HI_SVR_PVR_TIMESHIFT_INFO_TYPE_STATUS

        invokeex(request1, reply1);
        int ret = reply1.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int status = reply1.readInt();
            Log.d(TAG, "pvrTimeShiftPause GetPlayStatus(), status = " + status);
            if (status != EnPVRTimeShiftStatus.PAUSE &&
                    status != EnPVRTimeShiftStatus.STOP)
                ret = excuteCommand(CMD_PVR_TimeShift_Pause, 0);
        }
        request1.recycle();
        reply1.recycle();
        return getReturnValue(ret);
    }

    public int pvrTimeShiftLivePause(int playerID)//Scoty 20180827 add and modify TimeShift Live Mode
    {
        Log.d(TAG, "pvrTimeShiftLivePause");
        int ret = excuteCommand(CMD_PVR_TimeShift_Pause, 0);
        return getReturnValue(ret);
    }

    public int pvrTimeShiftFilePause(int playerID)//Scoty 20180827 add and modify TimeShift Live Mode
    {
        Log.d(TAG, "pvrTimeShiftFilePause");

        Parcel request1 = Parcel.obtain();
        Parcel reply1 = Parcel.obtain();
        request1.writeString(DTV_INTERFACE_NAME);
        request1.writeInt(CMD_PVR_TimeShift_GetInfo_TypeStatus);
//        request1.writeInt(CMD_PVR_TimeShift_GetInfo);
//        request1.writeInt(0); // 0 means HI_SVR_PVR_TIMESHIFT_INFO_TYPE_STATUS

        invokeex(request1, reply1);
        int ret = reply1.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int status = reply1.readInt();
            Log.d(TAG, "GetPlayStatus(), status = " + status);

            //Scoty 20180622 modify play/pause status -s
            if (status == EnPVRTimeShiftStatus.PLAY)
            {
                ret = excuteCommand(CMD_PVR_TimeShift_Pause, 0);
            }
            else if(status == EnPVRTimeShiftStatus.PAUSE ||
                    status == EnPVRTimeShiftStatus.FAST_FORWARD ||
                    status == EnPVRTimeShiftStatus.FAST_BACKWARD)
            {
                ret = excuteCommand(CMD_PVR_TimeShift_Resume, 0);
            }
            else if (status == EnPVRTimeShiftStatus.STOP)
            {
                //ret = excuteCommand(CMD_PVR_TimeShift_Start, 900, 0, 0);
                ret = TimeShift_Start_V2(900, 0, false, null);
            }
            else/* HI_SVR_PVR_TIMESHIFT_STATE_START */
            {
                ret = 0;
            }
            //Scoty 20180622 modify play/pause status -e
        }
        request1.recycle();
        reply1.recycle();
        return getReturnValue(ret);
    }

    public int pvrTimeShiftTrickPlay(int playerID, EnTrickMode mode)
    {
        Log.d(TAG, "startTrickPlay(" + mode + ") value = " + mode.getValue());
        return excuteCommand(CMD_PVR_TimeShift_Trick, mode.getValue());
    }

    public int pvrTimeShiftSeekPlay(int playerID, int seekSec)
    {
        Log.d(TAG, "seekPlay(" + seekSec + ")");
        return excuteCommand(CMD_PVR_TimeShift_Seek, seekSec);
    }

    public Date pvrTimeShiftGetPlayedTime(int playerID)
    {
        Log.d(TAG, "pvrTimeShiftGetPlayedTime()");
        Date retDate = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_TimeShift_GetPlayTime);
        request.writeInt(playerID);
        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int playTime = reply.readInt();

            Calendar ca = Calendar.getInstance();

            int dst = 0, zone = 0, dtvZoneSecond = 0, dtvDaylightSecond=0 ;
            if(ADD_SYSTEM_OFFSET) { // connie 20181106 for not add system offset
                dst = ca.get(Calendar.DST_OFFSET);
                zone = ca.get(Calendar.ZONE_OFFSET);
                dtvZoneSecond = this.getDtvTimeZone();
                dtvDaylightSecond = this.getDtvDaylight();
            }

            long timeMs = playTime;

            if ( ADD_SYSTEM_OFFSET && 0 != dtvZoneSecond)
            {
                timeMs -= (zone + dst) / 1000;
                timeMs += (dtvZoneSecond + dtvDaylightSecond);
            }

            timeMs *= 1000;

            retDate = new Date(timeMs);
        }

        request.recycle();
        reply.recycle();

        return retDate;
    }

    public int pvrTimeShiftGetPlaySecond(int playerID)
    {
        Log.d(TAG, "pvrTimeShiftGetPlaySecond()");


        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_TimeShift_GetPlayTime);
        request.writeInt(playerID);
        invokeex(request, reply);
        int ret = reply.readInt();
        int playTime = CMD_RETURN_VALUE_FAILAR;
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            playTime = reply.readInt();
            playTime /= 1000;
        }

        request.recycle();
        reply.recycle();

        return playTime;
    }

    public Date pvrTimeShiftGetBeginTime(int playerID)
    {
        Log.d(TAG, "getTimeShitBeginTime()");
        // TODO: need fix this
        Date retDate = null;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_TimeShift_GetStartTime);
        request.writeInt(playerID);
        invokeex(request, reply);
        int ret =  reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS ==ret)
        {
            int beginTime = reply.readInt();

            Calendar ca = Calendar.getInstance();
            int dst = ca.get(Calendar.DST_OFFSET);
            int zone = ca.get(Calendar.ZONE_OFFSET);
            int dtvZoneSecond = this.getDtvTimeZone();
            int dtvDaylightSecond = this.getDtvDaylight();

            long timeMs = beginTime;

            if (0 != dtvZoneSecond)
            {
                timeMs -= (zone + dst) / 1000;
                timeMs += (dtvZoneSecond + dtvDaylightSecond);
            }

            timeMs *= 1000;
            retDate = new Date(timeMs);
        }

        request.recycle();
        reply.recycle();

        return retDate;
    }

    public int pvrTimeShiftGetBeginSecond(int playerID)
    {
        Log.d(TAG, "pvrTimeShiftGetBeginSecond()");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_TimeShift_GetStartTime);
        request.writeInt(playerID);
        invokeex(request, reply);
        int ret =  reply.readInt();
        int beginTime = CMD_RETURN_VALUE_FAILAR;
        if (CMD_RETURN_VALUE_SUCCESS ==ret)
        {
            beginTime = reply.readInt();
            beginTime /= 1000;
        }

        request.recycle();
        reply.recycle();
        return beginTime;
    }

    public int pvrTimeShiftGetRecordTime(int playerID)
    {
        Log.d(TAG, "getTimeShiftRecordTime()");
        return excuteCommandGetII(CMD_PVR_TimeShift_GetRecTime) / 1000;
    }

    public int pvrTimeShiftGetStatus(int playerID)
    {
        Log.d(TAG, "pvrTimeShiftGetStatus()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
//        request.writeInt(CMD_PVR_TimeShift_GetInfo);
//        request.writeInt(0); // 0 means HI_SVR_PVR_TIMESHIFT_INFO_TYPE_STATUS
        request.writeInt(CMD_PVR_TimeShift_GetInfo_TypeStatus);

        invokeex(request, reply);
        int ret = reply.readInt();
        int pvrTimeShiftStatus = CMD_RETURN_VALUE_FAILAR;
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            pvrTimeShiftStatus = reply.readInt();
            Log.d(TAG, "pvrTimeShiftGetStatus(), status = " + pvrTimeShiftStatus);
        }

        request.recycle();
        reply.recycle();

        return pvrTimeShiftStatus;
    }

    public EnTrickMode pvrTimeShiftGetCurrentTrickMode(int playerID)
    {
        Log.d(TAG, "pvrTimeShiftGetCurrentTrickMode");
        EnTrickMode enTrickMode = EnTrickMode.INVALID_TRICK_MODE;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
//        request.writeInt(CMD_PVR_TimeShift_GetInfo);
//        request.writeInt(0); // 0 means HI_SVR_PVR_TIMESHIFT_INFO_TYPE_STATUS
        request.writeInt(CMD_PVR_TimeShift_GetInfo_TypeStatus);

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            reply.readInt();
            int index = reply.readInt();
            enTrickMode = EnTrickMode.valueOf(index);
        }
        request.recycle();
        reply.recycle();
        return enTrickMode;
    }

    public int pvrPlayStart(String filePath)
    {
        Log.d(TAG, "pvrPlayStart");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Play_Start);
        //Log.i(TAG,"the filepath is:"+filePath);
        request.writeString(filePath);
        request.writeInt(0);//not use cipher
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int pvrPlayStart(String filePath, PVREncryption pvrEncryption)
    {
        Log.d(TAG, "pvrPlayStart");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int length = pvrEncryption.getPvrKey().length();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Play_Start);
        Log.i(TAG,"the filepath is:"+filePath);
        request.writeString(filePath);
        request.writeInt(pvrEncryption.getPvrEncryptionType());
        request.writeString(pvrEncryption.getPvrKey());
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int pvrPlayStop()
    {
        Log.d(TAG, "pvrPlayStop");
        return excuteCommand(CMD_PVR_Play_Stop);
    }

    public int pvrPlayPause()
    {
        Log.d(TAG, "pvrPlayPause");
        return excuteCommand(CMD_PVR_Play_Pause);
    }

    public int pvrPlayResume()
    {
        Log.d(TAG, "pvrPlayResume");
        return excuteCommand(CMD_PVR_Play_Resume);
    }

    public int pvrPlayTrickPlay(EnTrickMode enSpeed)
    {
        Log.d(TAG, "pvrPlayTrickPlay");
        return excuteCommand(CMD_PVR_Play_Trick, enSpeed.getValue());
    }

    public int pvrPlaySeekTo(int sec)
    {
        long msec = sec * 1000;

        Log.d(TAG, "pvrPlaySeekTo");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Play_Seek);

        request.writeInt(0); //HI_SVR_PVR_PLAY_SEEKMODE_SET
        request.writeLong(msec);
        invokeex(request, reply);
        int ret = reply.readInt();

        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
        //return excuteCommand(CMD_PVR_Play_Seek, msec);
    }

    public int pvrPlayGetPlayTime()
    {
        Log.d(TAG, "pvrPlayGetPlayTime");
        return excuteCommandGetII(CMD_PVR_Play_GetPlayTime) / 1000;
    }
	
	public int pvrPlayGetPlayTimeMs()//eric lin 20181026 get play time(ms) for live channel
    {
        Log.d(TAG, "pvrPlayGetPlayTime");
        return excuteCommandGetII(CMD_PVR_Play_GetPlayTime);
    }

    public long pvrPlayGetSize()
    {
        Log.d(TAG, "pvrPlayGetSize");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
//        request.writeInt(CMD_PVR_Play_GetInfo);
//        request.writeInt(0); // get HI_SVR_PVR_PLAY_INFO_TYPE_FILE_ATTR
        request.writeInt(CMD_PVR_Play_GetInfo_TypeFileAttr);

        invokeex(request, reply);
        int ret = reply.readInt();
        long fileSize = CMD_RETURN_LONG_VALUE_FAILAR;
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            reply.readInt();// start time ms
            reply.readInt();// end time ms
            reply.readInt();// duration in ms
            fileSize = reply.readLong();// fileSize in byte
        }
        request.recycle();
        reply.recycle();
        return fileSize;
    }

    public int pvrPlayGetDuration()
    {
        Log.d(TAG, "pvrPlayGetDuration");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
//        request.writeInt(CMD_PVR_Play_GetInfo);
//        request.writeInt(0); // get HI_SVR_PVR_PLAY_INFO_TYPE_FILE_ATTR
        request.writeInt(CMD_PVR_Play_GetInfo_TypeFileAttr);

        invokeex(request, reply);
        int ret = reply.readInt();
        int duration = CMD_RETURN_VALUE_FAILAR;
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            reply.readInt();// start time ms
            reply.readInt();// end time ms
            duration = reply.readInt() / 1000;// duration in ms / 1000
        }
        request.recycle();
        reply.recycle();
        return duration;
    }

    public boolean pvrPlayIsRadio(String fullName)
    {
        Log.d(TAG, "pvrPlayIsRadio fullName="+fullName);
        if(null == fullName)
    {
            Log.e(TAG,"fullName is null");
            return false;
        }
        int serviceType = 0;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_File_GetInfo); //CMD_PVR_Play_GetInfo_TypeFileAttr
        request.writeString(fullName);
        invokeex(request, reply);
        int ret = reply.readInt();
        boolean bRadio = false;
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            reply.readInt();// start time ms
            reply.readInt();// end time ms
            reply.readInt();// duration in ms
            reply.readLong();// fileSize in byte
            serviceType = reply.readInt();
        }

        EnServiceType tmpServiceType = EnServiceType.valueOf(serviceType);
        if ((tmpServiceType == EnServiceType.RADIO)
                || (tmpServiceType == EnServiceType.FM_RADIO))
        {
            bRadio = true;
        }
        request.recycle();
        reply.recycle();
        return bRadio;
    }

    public int pvrPlayGetCurrentStatus()
    {
        Log.d(TAG, "pvrPlayGetCurrentStatus");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
//        request.writeInt(CMD_PVR_Play_GetInfo);
//        request.writeInt(1); // 1 means HI_SVR_PVR_PLAY_INFO_TYPE_STATUS
        request.writeInt(CMD_PVR_Play_GetInfo_TypeStatus);

        invokeex(request, reply);
        int ret = reply.readInt();
        int pvrStatus = CMD_RETURN_VALUE_FAILAR;
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            pvrStatus = reply.readInt();
        }

        request.recycle();
        reply.recycle();
        return pvrStatus;
    }

    public EnTrickMode pvrPlayGetCurrentTrickMode()
    {
        Log.d(TAG, "pvrPlayGetCurrentTrickMode");
        EnTrickMode enTrickMode = EnTrickMode.INVALID_TRICK_MODE;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
//        request.writeInt(CMD_PVR_Play_GetInfo);
//        request.writeInt(1); // 1 means HI_SVR_PVR_PLAY_INFO_TYPE_STATUS
        request.writeInt(CMD_PVR_Play_GetInfo_TypeStatus);
        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            reply.readInt();
            int index = reply.readInt();
            enTrickMode = EnTrickMode.valueOf(index);
        }
        request.recycle();
        reply.recycle();
        return enTrickMode;
    }

    public String pvrPlayGetFileFullPath(int pvrPlayerID)
    {
        Log.d(TAG, "pvrPlayGetFileFullPath()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Play_GetInfo_TypeFileFullpath);
        invokeex(request, reply);

        int ret = reply.readInt();
        String fullPath = null;
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            fullPath = reply.readString();
        }

        request.recycle();
        reply.recycle();
        return fullPath;
    }

    public Resolution pvrPlayGetVideoResolution()
    {
        Log.d(TAG, "pvrPlayGetVideoResolution");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
//        request.writeInt(CMD_PVR_Play_GetInfo_TypeFileAttr);
//        request.writeInt(3); // 3 means for HI_SVR_PVR_PLAY_INFO_TYPE_AVPLAY_STATUS
        request.writeInt(CMD_PVR_Play_GetInfo_TypeAvplayStatus);

        invokeex(request, reply);
        int ret = reply.readInt();
        Resolution resolution = null;
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            resolution = new Resolution();

            reply.readInt();  //fps
            resolution.width = reply.readInt();
            resolution.height = reply.readInt();
        }
        request.recycle();
        reply.recycle();
        return resolution;
    }

    public AudioInfo.AudioComponent pvrPlayGetCurrentAudio()
    {
        Log.d(TAG, "pvrPlayGetCurrentAudio");

        AudioInfo.AudioComponent localAudioInfo = null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Play_GetMutiAudioInfo);
        invokeex(request, reply);
        int ret = reply.readInt();

        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int audioCount = reply.readInt();
            int curIndex = reply.readInt();
            for (int i = 0 ; i < audioCount; i++ )
            {
                int pid = reply.readInt();
                int audioType = reply.readInt();
                int adType = reply.readInt(); //u32Reserved
                int compTag = reply.readInt();
                int compType = reply.readInt();
                int streamContent = reply.readInt();
                int trackMode = reply.readInt(); // u8Reserved2
                int reserved3 = reply.readInt(); // u8Reserved3
                String languageCode = reply.readString();

                if (curIndex == i)
                {
                    localAudioInfo = new AudioInfo.AudioComponent();
                    localAudioInfo.setLangCode(languageCode);
                    localAudioInfo.setPid(pid);
                    localAudioInfo.setAudioType(/*EnStreamType.valueOf(audioType)*/audioType);
                    localAudioInfo.setAdType(/*EnAudioType.valueOf(adType)*/adType);
                    localAudioInfo.setTrackMode(/*ConverToJavaTrack(trackMode)*/EnAudioTrackMode.valueOf(trackMode).getValue());
                    Log.d(TAG, "getCurrentAudio:languageCode = " + languageCode + ",pid = " + pid + ",audioType = " + audioType);
                }
            }
        }

        request.recycle();
        reply.recycle();
        return localAudioInfo;
    }

    public AudioInfo pvrPlayGetAudioComponents()
    {
        AudioInfo audioList = null;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Play_GetMutiAudioInfo);
        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int cnt = reply.readInt();
            int curIndex = reply.readInt();
            if (cnt > 0)
            {
                audioList = new AudioInfo();
                audioList.setCurPos(curIndex);//eric lin 20180720 add pvr play audio's CurPos and component pos
                for (int i = 0; i < cnt; i++)
                {
                    AudioInfo.AudioComponent localAudioInfo = new AudioInfo.AudioComponent();

                    int pid = reply.readInt();
                    int audioType = reply.readInt();
                    int adType = reply.readInt(); //u32Reserved
                    int compTag = reply.readInt();
                    int compType = reply.readInt();
                    int streamContent = reply.readInt();
                    int trackMode = reply.readInt(); // u8Reserved2
                    int reserved3 = reply.readInt(); // u8Reserved3
                    String languageCode = reply.readString();

                    localAudioInfo.setLangCode(languageCode);
                    localAudioInfo.setPid(pid);
                    localAudioInfo.setAudioType(/*EnStreamType.valueOf(audioType)*/audioType);
                    localAudioInfo.setAdType(/*EnAudioType.valueOf(adType)*/adType);
                    localAudioInfo.setTrackMode(/*ConverToJavaTrack(trackMode)*/EnAudioTrackMode.valueOf(trackMode).getValue());
                    localAudioInfo.setPos(i);//eric lin 20180720 add pvr play audio's CurPos and component pos
                    audioList.ComponentList.add(localAudioInfo);
                    Log.d(TAG, "getAudioComponents:languageCode = " + languageCode + "pid = " + pid
                            + "audioType = " + audioType);
                }
            }
        }

        request.recycle();
        reply.recycle();
        return audioList;
    }

    public int pvrPlaySelectAudio(AudioInfo.AudioComponent audio)
    {
        if(null == audio)
        {
            Log.e(TAG,"the param of audio is null");
            return CMD_RETURN_VALUE_FAILAR;
        }

        int audioPid = audio.getPid();
        int index = 0;
        boolean bGet = false;

        Log.d(TAG,"pvrPlaySelectAudio(languageCode = " + audio.getLangCode() + ",pid =" + audio.getPid()
                + ",type =" + audio.getAudioType() + ")");

        Parcel request1 = Parcel.obtain();
        Parcel reply1 = Parcel.obtain();
        request1.writeString(DTV_INTERFACE_NAME);
        request1.writeInt(CMD_PVR_Play_GetMutiAudioInfo);

        invokeex(request1, reply1);
        int ret1 = reply1.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret1)
        {
            int audioCount = reply1.readInt();
            int curIndex = reply1.readInt();
            for (int i = 0 ; i < audioCount; i++ )
            {
                int pid = reply1.readInt();
                int audioType = reply1.readInt();
                int adType = reply1.readInt(); //u32Reserved
                int compTag = reply1.readInt();
                int compType = reply1.readInt();
                int streamContent = reply1.readInt();
                int trackMode = reply1.readInt(); // u8Reserved2
                int reserved3 = reply1.readInt(); // u8Reserved3
                String languageCode = reply1.readString();

                if (pid == audioPid)
                {
                    index = i;
                    bGet = true;
                    break;
                }
            }
        }

        request1.recycle();
        reply1.recycle();

        if (!bGet)
        {
            Log.e(TAG, "not find this audio track(languageCode = " + audio.getLangCode() + ",pid =" + audio.getPid()
                    + ",type =" + audio.getAudioType() + ")");
            return CMD_RETURN_VALUE_FAILAR;
        }

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Play_ChangeAudioTrack);
        request.writeInt(index);

        invokeex(request, reply);
        int ret = reply.readInt();

        request.recycle();
        reply.recycle();
        Log.d(TAG, "ret = " + ret);
        return getReturnValue(ret);
    }

    public int pvrPlaySetWindowRect(Rect rect)
    {
        if(null == rect)
        {
            Log.e(TAG,"the param of rect is null");
            return CMD_RETURN_VALUE_FAILAR;
        }
        Log.d(TAG, "setWindowRect(" + rect.left + "," + rect.top + "," + rect.right + ","+ rect.bottom + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Play_SetWindowSize);
        request.writeInt(rect.left);
        request.writeInt(rect.top);
        request.writeInt(rect.width());
        request.writeInt(rect.height());

        invokeex(request, reply);
        int ret = reply.readInt();

        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    public int pvrPlaySetTrackMode(EnAudioTrackMode enTrackMode)
    {
        Log.d(TAG, "pvrplay setTrackMode(" + enTrackMode + ")");
        int svrTrackMode = 0;
        switch (enTrackMode)
        {
            case /*AUDIO_TRACK_STEREO*/MPEG_AUDIO_TRACK_STEREO:
                svrTrackMode = 0; /*HI_SVR_AV_AUDIO_TRACK_MODE_STEREO*/
                break;
            /*case AUDIO_TRACK_DOUBLE_MONO:
                svrTrackMode = 1; *//*HI_SVR_AV_AUDIO_TRACK_MODE_MONO*//*
                break;*/
            case /*AUDIO_TRACK_DOUBLE_LEFT*/MPEG_AUDIO_TRACK_LEFT:
                svrTrackMode = 4; /*HI_SVR_AV_AUDIO_TRACK_MODE_LEFT*/
                break;
            case /*AUDIO_TRACK_DOUBLE_RIGHT*/MPEG_AUDIO_TRACK_RIGHT:
                svrTrackMode = 5; /*HI_SVR_AV_AUDIO_TRACK_MODE_RIGHT*/
                break;
            /*case AUDIO_TRACK_EXCHANGE:
                break;
            case AUDIO_TRACK_ONLY_RIGHT:
                svrTrackMode = 5; *//*HI_SVR_AV_AUDIO_TRACK_MODE_RIGHT*//*
                break;
            case AUDIO_TRACK_ONLY_LEFT:
                svrTrackMode = 4; *//*HI_SVR_AV_AUDIO_TRACK_MODE_LEFT*//*
                break;*/
            /*case AUDIO_TRACK_MUTED:
                break;*/
            default:
                break;
        }

        return excuteCommand(CMD_PVR_Play_SetAudioTrackMode, svrTrackMode);
    }

    public EnAudioTrackMode pvrPlayGetTrackMode()
    {
        Log.d(TAG, "getTrackMode()");
        EnAudioTrackMode enTrackMode = EnAudioTrackMode./*AUDIO_TRACK_BUTT*/MPEG_AUDIO_TRACK_BUTT;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Play_GetAudioTrackMode);

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            int index = reply.readInt();
            switch (index)
            {
                case 0:
                    enTrackMode = EnAudioTrackMode./*AUDIO_TRACK_STEREO*/MPEG_AUDIO_TRACK_STEREO;
                    break;
               /* case 1:
                    enTrackMode = EnAudioTrackMode.AUDIO_TRACK_DOUBLE_MONO;
                    break;*/
                case 2:
                    enTrackMode = EnAudioTrackMode./*AUDIO_TRACK_STEREO*/MPEG_AUDIO_TRACK_STEREO;
                    break;
                case 3:
                    enTrackMode = EnAudioTrackMode./*AUDIO_TRACK_STEREO*/MPEG_AUDIO_TRACK_STEREO;
                    break;
                case 4:
                    enTrackMode = EnAudioTrackMode./*AUDIO_TRACK_DOUBLE_LEFT*/MPEG_AUDIO_TRACK_LEFT;
                    break;
                case 5:
                    enTrackMode = EnAudioTrackMode./*AUDIO_TRACK_DOUBLE_RIGHT*/MPEG_AUDIO_TRACK_RIGHT;
                    break;
                default:
                    break;
            }
        }
        request.recycle();
        reply.recycle();
        Log.d(TAG, "getTrackMode(),enTrackMode = " + enTrackMode);

        return enTrackMode;
    }

    public int pvrFileRemove(String filePath)
    {
        Log.d(TAG, "pvrFileRemove() filePath = " + filePath);
        if(null == filePath)
        {
            Log.e(TAG,"the filePath is null");
            return CMD_RETURN_VALUE_FAILAR;
        }

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_File_Remove);
        request.writeString(filePath);

        invokeex(request, reply);
        int ret = reply.readInt();

        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    public int pvrFileRename(String oldName, String newName)
    {
        Log.d(TAG, "pvrFileRename()");
        if(null == oldName || null == newName)
        {
            Log.e(TAG,"oldName or newName is null");
            return CMD_RETURN_VALUE_FAILAR;
        }

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_File_Rename);
        request.writeString(oldName);
        request.writeString(newName);

        invokeex(request, reply);
        int ret = reply.readInt();

        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    public int pvrFileGetDuration(String fullName)
    {
        Log.d(TAG, "pvrFileGetDuration()");
        if(null == fullName)
        {
            Log.e(TAG,"fullName is null");
            return CMD_RETURN_VALUE_FAILAR;
        }

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_File_GetInfo);
        request.writeString(fullName);

        invokeex(request, reply);
        int ret = reply.readInt();
        int durationSec = CMD_RETURN_VALUE_FAILAR;
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            reply.readInt();  //startTime in ms
            reply.readInt();  //endTime in ms
            durationSec = reply.readInt() / 1000;
        }

        request.recycle();
        reply.recycle();

        return durationSec;
    }

    public long pvrFileGetSize(String fullName)
    {
        Log.d(TAG, "pvrFileGetSize()");
        if(null == fullName)
        {
            Log.e(TAG,"fullName is null");
            return CMD_RETURN_VALUE_FAILAR;
        }

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_File_GetInfo);
        request.writeString(fullName);

        invokeex(request, reply);
        int ret = reply.readInt();
        long fileSize = CMD_RETURN_LONG_VALUE_FAILAR;
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            reply.readInt();  //startTime in ms
            reply.readInt();  //endTime in ms
            reply.readInt();  //duration in ms
            fileSize = reply.readLong();
        }

        request.recycle();
        reply.recycle();

        return fileSize;
    }

    public PvrFileInfo pvrFileGetAllInfo(String fullName)
    {
        Log.d(TAG, "pvrFileGetAllInfo()");
        if(null == fullName)
        {
            Log.e(TAG,"fullName is null");
            //return CMD_RETURN_VALUE_FAILAR;
            return null;
        }

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_File_GetInfo);
        request.writeString(fullName);

        invokeex(request, reply);
        int ret = reply.readInt();
        PvrFileInfo pvrFileInfo = new PvrFileInfo();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            pvrFileInfo.startTimeInMs = reply.readInt();    // startTime in ms
            pvrFileInfo.endTimeInMs = reply.readInt();      // endTime in ms
            pvrFileInfo.durationInMs = reply.readInt();     // duration in ms
            pvrFileInfo.fileSize = reply.readLong();        // file size
            pvrFileInfo.serviceType = reply.readInt();      // service type
            pvrFileInfo.channelLock = reply.readInt();      // channel lock
            pvrFileInfo.parentalRate = reply.readInt();      // parental lock
        }
        else 
        {
            pvrFileInfo.startTimeInMs = 0;    // startTime in ms
            pvrFileInfo.endTimeInMs = 0;      // endTime in ms
            pvrFileInfo.durationInMs = 0;     // duration in ms
            pvrFileInfo.fileSize = 0;        // file size
            pvrFileInfo.serviceType = 0;      // service type
            pvrFileInfo.channelLock = 0;      // channel lock
            pvrFileInfo.parentalRate = 0;      // parental lock
        }

        request.recycle();
        reply.recycle();

        return pvrFileInfo;
    }

    public PvrFileInfo pvrFileGetExtraInfo(String fullName)
    {
        Log.d(TAG, "pvrFileGetExtraInfo()");
        if(null == fullName)
        {
            Log.e(TAG,"fullName is null");
            //return CMD_RETURN_VALUE_FAILAR;
            return null;
        }

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_File_GetExtraInfo);
        request.writeString(fullName);

        invokeex(request, reply);
        int ret = reply.readInt();
        PvrFileInfo pvrFileInfo = new PvrFileInfo();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            pvrFileInfo.channelName = reply.readString();
            pvrFileInfo.year = reply.readInt();
            pvrFileInfo.month = reply.readInt();
            pvrFileInfo.date = reply.readInt();
            pvrFileInfo.week = reply.readInt();
            pvrFileInfo.hour = reply.readInt();
            pvrFileInfo.minute = reply.readInt();
            pvrFileInfo.second = reply.readInt();
        }
        else
        {
            pvrFileInfo.channelName = null;
            pvrFileInfo.year = 0;
            pvrFileInfo.month = 0;
            pvrFileInfo.date = 0;
            pvrFileInfo.week = 0;
            pvrFileInfo.hour = 0;
            pvrFileInfo.minute = 0;
            pvrFileInfo.second = 0;
        }

        request.recycle();
        reply.recycle();

        return pvrFileInfo;
    }

    public PvrFileInfo pvrFileGetEpgInfo(String fullName, int epgIndex)
    {
        Log.d( TAG, "pvrFileGetEpgInfo: " );

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_File_GetEPGInfo);
        request.writeString(fullName);
        request.writeInt(epgIndex);
        invokeex(request, reply);

        PvrFileInfo fileInfo = new PvrFileInfo();
        int ret = reply.readInt();
        
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            fileInfo.eventName      = reply.readString();
            fileInfo.shortEvent     = reply.readString();
            fileInfo.extendedText   = reply.readString();
            fileInfo.languageCode   = reply.readString();
            fileInfo.extendedLanguageCode = reply.readString();

            fileInfo.year = reply.readInt();
            fileInfo.month = reply.readInt();
            fileInfo.date = reply.readInt();
            fileInfo.hour = reply.readInt();
            fileInfo.minute = reply.readInt();
            fileInfo.second = reply.readInt();
            fileInfo.week = reply.readInt();
            fileInfo.yearEnd = reply.readInt();
            fileInfo.monthEnd = reply.readInt();
            fileInfo.dateEnd = reply.readInt();
            fileInfo.hourEnd = reply.readInt();
            fileInfo.monthEnd = reply.readInt();
            fileInfo.secondEnd = reply.readInt();
            fileInfo.weekEnd = reply.readInt();

            fileInfo.parentalRate       = reply.readInt();
            fileInfo.eventNameCharCode  = reply.readInt();
            fileInfo.shortEventCharCode = reply.readInt();
            fileInfo.recordTimeStamp    = reply.readInt();
        }
        request.recycle();
        reply.recycle();
        return fileInfo;
    }

    public int pvrGetCurrentPvrMode(long channelId)
    {
        Log.d(TAG, "pvrGetCurrentPvrMode()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_GetPesiPVRmode);
        request.writeInt((int) channelId);
        invokeex(request, reply);
        int ret = reply.readInt();
        int pvrMode = CMD_RETURN_VALUE_FAILAR;
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            pvrMode = reply.readInt();
        }

        request.recycle();
        reply.recycle();

        return pvrMode;
    }

    public int PvrSetParentLockOK()  //connie 20180806 for pvr parentalRate
    {
        Log.d(TAG, "PvrSetParentLockOK()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_SetParentLockOK);
        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
        }
        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    // edwin 20180809 add PvrTotalRecordFileXXX -s
    public int pvrTotalRecordFileOpen(String dirPath)
    {
        Log.d( TAG, "PvrTotalRecordFileOpen: " );

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Total_Record_File_Open);
        request.writeString( dirPath );
        invokeex(request, reply);
        int ret = reply.readInt();
        int totalFileNumber = 0; // edwin 20180816 return normal value
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            totalFileNumber = reply.readInt();
        }
        request.recycle();
        reply.recycle();

        return totalFileNumber;
    }

    public int pvrTotalRecordFileClose()
    {
        Log.d( TAG, "PvrTotalRecordFileClose: " );

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Total_Record_File_Close);
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    public int pvrTotalRecordFileSort(int sortType)
    {
        Log.d( TAG, "PvrTotalRecordFileSort: " );

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Total_Record_File_Sort);
        request.writeInt( sortType );
        // PVR_SORT_BY_CHNAME	=0,
        // PVR_SORT_BY_DATETIME	=1,
        // PVR_SORT_BUTT
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    public List<PvrFileInfo> pvrTotalRecordFileGet(int startIndex, int total)
    {
        Log.d( TAG, "PvrTotalRecordFileGet: " );

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Total_Record_File_Get);
        request.writeInt( startIndex );
        request.writeInt( total );
        invokeex(request, reply);
        int ret = reply.readInt();
        List<PvrFileInfo> list = new ArrayList<>();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            for ( int i = 0; i < total; i++ )
            {
                PvrFileInfo fileInfo = new PvrFileInfo();
                fileInfo.channelName = reply.readString();
                fileInfo.realFileName = reply.readString();
                list.add( fileInfo );
            }
        }
        request.recycle();
        reply.recycle();

        return list;
    }

    public int pvrCheckHardDiskOpen(String FilePath)//Scoty 20180827 add HDD Ready command and callback
    {
        Log.d(TAG, "pvrCheckHardDiskOpen()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Open_Hard_Disk);
        request.writeString(FilePath);
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    public int pvrPlayTimeShiftStop()//Scoty 20180827 add and modify TimeShift Live Mode
    {
        Log.d(TAG, "pvrCheckHardDiskOpen()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_TimeShift_PlayStop );
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }

    public int pvrRecordGetLivePauseTime()//Scoty 20180827 add and modify TimeShift Live Mode
    {
        Log.d(TAG, "pvrGetLivePauseTime()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_TimeShift_GetLivePauseTime);
        invokeex(request, reply);
        int ret = reply.readInt();
        int playTime = CMD_RETURN_VALUE_FAILAR;
        if(ret == CMD_RETURN_VALUE_SUCCESS)
        {
            playTime = reply.readInt();
            playTime /= 1000;
        }
        request.recycle();
        reply.recycle();

        return playTime;
    }


    // edwin 20180809 add PvrTotalRecordFileXXX -e

//    public int pvrGetCurrentRecMode()
//    {
//        Log.d(TAG, "pvrGetCurrentRecMode()");
//        Parcel request = Parcel.obtain();
//        Parcel reply = Parcel.obtain();
//        request.writeString(DTV_INTERFACE_NAME);
//        request.writeInt(CMD_PVR_GetPesiAddInfo_PVRmode);
//        invokeex(request, reply);
//        int ret = reply.readInt();
//        int recMode = CMD_RETURN_VALUE_FAILAR;
//        if (CMD_RETURN_VALUE_SUCCESS == ret)
//        {
//            recMode = reply.readInt();
//        }
//
//        request.recycle();
//        reply.recycle();
//
//        return recMode;
//    }

//    public int pvrGetCurrentPlayMode()
//    {
//        Log.d(TAG, "pvrGetCurrentPlayMode()");
//        Parcel request = Parcel.obtain();
//        Parcel reply = Parcel.obtain();
//        request.writeString(DTV_INTERFACE_NAME);
//        request.writeInt(CMD_PVR_GetPesiAddInfo_PVRmode);
//        invokeex(request, reply);
//        int ret = reply.readInt();
//        int playMode = CMD_RETURN_VALUE_FAILAR;
//        if (CMD_RETURN_VALUE_SUCCESS == ret)
//        {
//            reply.readInt(); // recMode
//            playMode = reply.readInt();
//        }
//
//        request.recycle();
//        reply.recycle();
//
//        return playMode;
//    }

    public int pvrGetRatio()
    {
        Log.d(TAG, "pvrGetRatio()");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_GetAspectRatio);
        invokeex(request, reply);
        int ret = reply.readInt();
        int ratio = 0;
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            ratio = reply.readInt();
        }
        Log.d(TAG, "pvrGetRatio: ratio = " + ratio);
        request.recycle();
        reply.recycle();

        return ratio;
    }
    // pvr -end

    public String GetRecordPath()
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_CFG_GetStringValue);
        request.writeString(TAG_CFG_PVR_PATH);
        request.writeString(DefaultRecPath);

        invokeex(request, reply);
        int ret = reply.readInt();
        String path = null;
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            path = reply.readString(); //select usb mount path
        }

        if(path == null || path.equals(""))
        {
            request.writeInt(CMD_CFG_SetStringValue);
            request.writeString(TAG_CFG_PVR_PATH);
            request.writeString(DefaultRecPath);
            invokeex(request, reply);

            saveTable(EnTableType.GPOS); // connie 20180530 for save record path
            path = DefaultRecPath;
        }

        request.recycle();
        reply.recycle();

        return path;
    }

    public void setRecordPath(String path)
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_CFG_SetStringValue);
        request.writeString(TAG_CFG_PVR_PATH);
        request.writeString(path);

        invokeex(request, reply);
        int ret = reply.readInt();

        request.recycle();
        reply.recycle();
    }

    public String getDefaultRecPath()//Scoty 20180525 add get default record path
    {
        return DefaultRecPath;
    }

    public int pvrRecordCheck(long channelID){
        Log.d(TAG, "pvrRecordCheck: channelID="+channelID);
        //return recId
        return excuteCommandGetII(CMD_PVR_Record_Check, channelID);
    }

    public List<PvrInfo>  pvrRecordGetAllInfo(){
        Log.d(TAG, "pvrRecordGetAllInfo: ");

        List<PvrInfo> pvrList = new ArrayList<PvrInfo>();

        int i = 0;
        int ret = 0;
        int retNum = 0;

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Record_GetAllInfo);
        invokeex(request, reply);
        ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            retNum = reply.readInt();
            Log.d(TAG, "pvr Num =" + retNum);
            int rec_id=0;
            int channel_id=0;
            int pvr_mode=0;
            for (i = 0; i < retNum; i++)
            {
                rec_id = reply.readInt();
                channel_id = reply.readInt();
                pvr_mode = reply.readInt();
                PvrInfo pvr = new PvrInfo(rec_id, channel_id, pvr_mode);
                Log.d(TAG, "pvr Num =" + retNum);
                pvrList.add(pvr);
            }
            Log.d(TAG, "getProgramList end");
        }
        request.recycle();
        reply.recycle();

        return pvrList;
    }

    public int pvrRecordGetMaxRecNum(){
        Log.d(TAG, "pvrRecordGetMaxRecNum: ");
        return excuteCommandGetII(CMD_PVR_Record_GetMaxRecNum);
    }

    public int pvrPlayFileCheckLastViewPoint(String fullName){
        Log.d(TAG, "pvrPlayFileCheckLastViewPoint fullName="+fullName);
        if(null == fullName)
        {
            Log.e(TAG,"fullName is null");
            return 0;
        }
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_PlayFile_CheckLastViewPoint);
        request.writeString(fullName);
        invokeex(request, reply);
        int ret = reply.readInt();
        int CheckOk=0;
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            CheckOk = reply.readInt();//CheckOk
        }
        request.recycle();
        reply.recycle();
        return CheckOk;
    }

    public int pvrSetStartPositionFlag(int startPositionFlag){
        return excuteCommand(CMD_PVR_SetStartPositionFlag, startPositionFlag);
    }

    public List<SimpleChannel> getChannelListByFilter(int filterTag, int serviceType, String keyword, int IncludeSkip, int IncludePvrSkip)//Scoty 20181109 modify for skip channel
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PM_GetChannelFilter);
        request.writeInt(filterTag);
        request.writeInt(serviceType);
        request.writeString(keyword);
        request.writeInt(IncludeSkip);
        request.writeInt(IncludePvrSkip);
        invokeex(request, reply);
        List<SimpleChannel> channelList = new ArrayList<>();
        SimpleChannel simpleChannel;

        int ret = reply.readInt();
        if ( CMD_RETURN_VALUE_SUCCESS == ret )
        {
            int count = reply.readInt();
            for ( int i = 0; i < count; i++ )
            {
                simpleChannel = new SimpleChannel();
                simpleChannel.setChannelId(reply.readInt());
                simpleChannel.setChannelNum(reply.readInt());
                simpleChannel.setChannelName(reply.readString());
                channelList.add(simpleChannel);
            }
        }

        request.recycle();
        reply.recycle();

        return channelList;
    }

    public int recordTS_start(int TunerId, String FullName) // connie 20180803 add record ts -s
    {
        if( FullName == null )
        {
            Log.d(TAG, "recordTS_start: FullName is null !!!!!");
            return -1;
        }

        Log.d(TAG, "recordTS_start:  TunerId =" + TunerId + "     FullName =" + FullName);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Record_AllTs_Start);
        request.writeInt(TunerId);
        request.writeString(FullName);
        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "recordTS_start:  recordTS_start Sucess !!!!!!");
        }
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }
    public int recordTS_stop()
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_PVR_Record_AllTs_Stop);
        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "recordTS_stop:  recordTS_stop Sucess !!!!!!");
        }
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }// connie 20180803 add record ts -e

    // Johnny 20180814 add setDiseqc1.0 port -s
    public int setDiSEqC10PortInfo(int nTuerID, int nPort, int n22KSwitch, int nPolarity)
    {
        Log.d(TAG, "setPort4Information:tunerID(" + nTuerID + ")cmdType(" + 0 + ")port(" + nPort
                + ")polar" + nPolarity + ")switch(" + n22KSwitch + ")");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_FE_SetDiSEqC10);
        request.writeInt(nTuerID);
        request.writeInt(nPort);
        request.writeInt(nPolarity);
        request.writeInt(n22KSwitch);
        request.writeInt(0); // diseqc level

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }
    //Scoty add DiSeqC Motor rule -s
    public int setDiSEqC12MoveMotor(int nTunerId, int Direct, int Step)
    {
        Log.d(TAG, "setDiSEqC12MoveMotor: ===>>> Direct = " + Direct + " Step = " + Step);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_FE_MoveMotor);
        request.writeInt(nTunerId);
        request.writeInt(Direct);// 0: Move east, 1: Move west, 2: Invalid value
        request.writeInt(Step);//0 mean running continus; 1~128 mean running steps every time
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);

    }

    public int setDiSEqC12MoveMotorStop(int nTunerId)
    {
        Log.d(TAG, "setDiSEqC12MoveMotorStop: ===>> IN");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_FE_StopMoveMotor);
        request.writeInt(nTunerId);
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int resetDiSEqC12Position(int nTunerId)
    {
        Log.d(TAG, "resetDiSEqC12Position: ===>>> IN");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_FE_ResetMotor);
        request.writeInt(nTunerId);
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int setDiSEqCLimitPos(int nTunerId, int limitType)
    {
        Log.d(TAG, "setDiSEqCLimitPos: limitType = " + limitType);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_FE_SetMotorLimitPosition);
        request.writeInt(nTunerId);
        request.writeInt(limitType);//0:Disable Limits , 1:Set East Limit, 2:Set West Limit, 3:Invalid value
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }
    //Scoty add DiSeqC Motor rule -e
    // Johnny 20180814 add setDiseqc1.0 port -e
    public int SetStandbyOnOff(int onOff) {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_CFG_SetStandbyOnOff);
        request.writeInt(onOff);
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    // for VMX need open/close -s
    public void VMXstartScan(TVScanParams sp, int startTPID, int endTPID) // connie 20180919 add for vmx search
    {
        Log.d(TAG, "startScan");
        int ret;
        if(sp.getTpInfo().getTunerType() == TpInfo.DVBC)
        {
            ret = dvbcVMXScan(sp, startTPID, endTPID);
            Log.d(TAG, "startScan dvbcVMXScan. Ret = " + ret);
        }
        else if(sp.getTpInfo().getTunerType() == TpInfo.DVBS)
        {
            ret = dvbsVMXScan(sp, startTPID, endTPID);
            Log.d(TAG, "startScan dvbsVMXScan. Ret = " + ret);
        }
        else if(sp.getTpInfo().getTunerType() == TpInfo.DVBT)
        {
            ret = dvbtVMXScan(sp, startTPID, endTPID);
            Log.d(TAG, "startScan dvbtVMXScan. Ret = " + ret);
        }
        else if(sp.getTpInfo().getTunerType() == TpInfo.ISDBT)
        {
            ret = isdbtVMXScan(sp, startTPID, endTPID);
            Log.d(TAG, "startScan isdbtVMXScan. Ret = " + ret);
        }
    }

    public LoaderInfo GetLoaderInfo() // connie 20180903 for VMX -s
    {
        Log.d(TAG, "GetLoaderInfo: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_LOADER_INFO);
        invokeex(request, reply);

        int ret = reply.readInt();
        LoaderInfo loaderInfo = new LoaderInfo();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "GetLoaderInfo ok");
            loaderInfo.Software = reply.readString();
            loaderInfo.Hardware = reply.readString();
            loaderInfo.SequenceNumber = reply.readString();
            loaderInfo.BuildDate = reply.readString();
        }
        else
            Log.d(TAG, "GetLoaderInfo failed, ret: " + ret);
        request.recycle();
        reply.recycle();
        return loaderInfo;
    }

    public CaStatus GetCAStatusInfo()
    {
        Log.d(TAG, "GetCAStatusInfo: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_STATUS);
        invokeex(request, reply);

        int ret = reply.readInt();
        CaStatus caInfo = new CaStatus();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "GetCAStatusInfo ok");
            caInfo.CA_status = reply.readString();
            caInfo.Auth = reply.readString();
            caInfo.Deauth = reply.readString();
        }
        request.recycle();
        reply.recycle();
        return caInfo;
    }

    public int GetECMcount()
    {
        Log.d(TAG, "GetECMcount: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_ECM_CNT);
        invokeex(request, reply);

        int ret = reply.readInt();
        int count = 0;
        CaStatus caInfo = new CaStatus();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "GetECMcount ok");
            count = reply.readInt();
        }
        request.recycle();
        reply.recycle();
        return count;
    }

    public int GetEMMcount()
    {
        Log.d(TAG, "GetEMMcount: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_EMM_CNT);
        invokeex(request, reply);

        int ret = reply.readInt();
        int count = 0;
        CaStatus caInfo = new CaStatus();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "GetEMMcount ok");
            count = reply.readInt();
        }
        request.recycle();
        reply.recycle();
        return count;
    }

    public String GetLibDate()
    {
        Log.d(TAG, "GetLibDate: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_LIBDATE);
        invokeex(request, reply);

        int ret = reply.readInt();
        String date = "";
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "GetLibDate ok");
            date = reply.readString();
        }
        request.recycle();
        reply.recycle();
        return date;
    }

    public String GetChipID()
    {
        Log.d(TAG, "GetChipID: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_CHIPID);
        invokeex(request, reply);

        int ret = reply.readInt();
        String chipID = "";
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "GetChipID ok");
            chipID = reply.readString();
        }
        else
            Log.d(TAG, "GetChipID failed, ret = "+ret);
        request.recycle();
        reply.recycle();
        return chipID;
    }

    public String GetSN()
    {
        Log.d(TAG, "GetSN: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_SN);
        invokeex(request, reply);

        int ret = reply.readInt();
        String sn = "";
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "GetSN ok");
            sn = reply.readString();
        }
        request.recycle();
        reply.recycle();
        return sn;
    }

    public String GetCaVersion()
    {
        Log.d(TAG, "GetCaVersion: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_CAVER);
        invokeex(request, reply);

        int ret = reply.readInt();
        String caVer = "";
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "GetCaVersion ok");
            caVer = reply.readString();
        }
        request.recycle();
        reply.recycle();
        return caVer;
    }

    public String GetSCNumber()
    {
        Log.d(TAG, "GetCaVersion: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_SCNUM);
        invokeex(request, reply);

        int ret = reply.readInt();
        String scNum = "";
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "GetSCNumber ok");
            scNum = reply.readString();
        }
        request.recycle();
        reply.recycle();
        return scNum;
    }

    public int GetPairingStatus()
    {
        Log.d(TAG, "GetPairingStatus: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_PAIR);
        invokeex(request, reply);

        int ret = reply.readInt();
        int pair = 0;
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "GetPairingStatus ok");
            pair = reply.readInt();
        }
        request.recycle();
        reply.recycle();
        return pair;
    }

    public String GetPurse()
    {
        Log.d(TAG, "GetPurse: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_PURSE);
        invokeex(request, reply);

        int ret = reply.readInt();
        String purse = "";
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "GetPurse ok");
            purse = reply.readString();
        }
        request.recycle();
        reply.recycle();
        return purse;
    }

    public int GetGroupM()
    {
        Log.d(TAG, "GetGroupM: ");
        int groupM = 0;
        return groupM;
    }

    public int VMX_SetPinCode(String pincode)
    {
        Log.d(TAG, "GetPurse: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_SET_PINCODE);
        request.writeString(pincode);
        invokeex(request, reply);

        int ret = reply.readInt();
        int err = 0;
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "GetPurse ok");
            err = reply.readInt();
        }
        request.recycle();
        reply.recycle();
        return err;
    }


    public String GetLocation()
    {
        Log.d(TAG, "GetLocation: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_PURSE);
        invokeex(request, reply);

        int ret = reply.readInt();
        String purse = "";
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "GetLocation ok");
            purse = reply.readString();
        }
        request.recycle();
        reply.recycle();
        return purse;
    }

    public int SetPinCode(String pinCode, int PinIndex, int TextSelect)
    {
        Log.d(TAG, "SetPinCode: pinCode = " + pinCode);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_SET_PINCODE);
        request.writeString(pinCode);
        request.writeInt(PinIndex);
        request.writeInt(TextSelect);
        invokeex(request, reply);
        int err = 0;
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "SetPinCode ok");
            err = reply.readInt();
        }

        request.recycle();
        reply.recycle();
        return err;
    }

    public int SetPPTV(String pinCode, int pinIndex)
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_SET_PPTV);
        request.writeString(pinCode);
        request.writeInt(pinIndex);

        invokeex(request, reply);
        int err = 0;
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "SetPinCode ok");
            err = reply.readInt();
        }

        request.recycle();
        reply.recycle();
        return err;
    }

    public void SetOMSMok()
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_SET_OSM_OK);

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "SetOMSMok ok");
        }

        request.recycle();
        reply.recycle();
    }

    public void VMXTest(int mode)
    {
        Log.d(TAG, "VMXTest:  mode="+mode);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMX_VMX_TEST);
        //====test data====
        if ( mode == 7 ) // Edwin 20181127 Scramble is same as e16
            request.writeInt(9);
        else
            request.writeInt(mode); // cmd

        if(mode == 0) { // message
            Log.d(TAG, "VMXTest:  Show Mesage TEST !!!!");
            request.writeInt(0); // mode    0:user  1: always
            request.writeInt(5); // duration // 1s
            //request.writeInt(0); // Trigger ID
            //request.writeInt(0); // Trigger Num
            request.writeString("Test Msg : abcdefg hijklm npqrs tuvwxyz 123 456 789 0"); // msg
        }
        else if(mode == 1) 
        { // OTA
            Log.d(TAG, "VMXTest:  OTA TEST !!!!");
            request.writeInt(1); // mode    0: normal   1:force   2: Err
            request.writeInt(0); // Trigger ID
            request.writeInt(0); // Trigger Num
            request.writeInt(0); // Freq Num//Scoty 20181207 modify VMX OTA rule
        }
        else if(mode == 2) { // WaterPrint msg Test
            Log.d(TAG, "VMXTest:  WATER TEST !!!!");
            request.writeInt(0); // mode   0:alwayse  1:flash    2: close
            request.writeInt(100); // duration // 0.1s
            request.writeString("Test Msg : abcdefg hijklm npqrs tuvwxyz 123 456 789 0"); // msg
            request.writeInt(100); // frame X
            request.writeInt(100); // frame Y
            request.writeInt(0); // Trigger
            request.writeInt(0);// Trigger Num;
        }
        else if(mode == 3) // Pin Test
        {
            Log.d(TAG, "VMXTest:  Pin Test");
            request.writeInt(0); // 0:open    1:close
            request.writeInt(-2118123484); // channel ID
            request.writeInt(0); // Pin Index
            request.writeInt(0); // Text Selector
        }

        else if(mode == 4) // IPPV Pin Test
        {
            Log.d(TAG, "VMXTest:  IPPV PIN Test");
            request.writeInt(0);  // 0:open    1:close
            request.writeInt(-2118123484); // channel ID
            request.writeInt(0); // Pin Index
            request.writeString("80"); //cur token
            request.writeString("100"); // cost
        }

        else if(mode == 5) // Card Detect
        {
            request.writeInt(0); // card status
            Log.d(TAG, "VMXTest:  Card Status input = " );
        }

        else if(mode == 6) // Search
        {
            Log.d(TAG, "VMXTest: Block Search Test");
            request.writeInt(0); // search mode   0: all   1: tp
            request.writeInt(659143); // start freq
            request.writeInt(689143); // end freq
            request.writeInt(0); // Trigger
            request.writeInt(0);// Trigger Num
        }

        // Edwin 20181127 Scramble is same as e16
        //else if(mode == 7) // Scramble
        //{
        //    request.writeInt(0); // 0 :close 1: open
        //}

        else if(mode == 8) // ewbs
        {
            request.writeInt(1); // 0: close 1:open
            request.writeInt(0); // signal level
        }

        else if(mode == 9 || mode == 7) // e16
        {
            request.writeInt(0); // 0: open 1:close
        }

        else if(mode == 10) // mail
        {
            request.writeInt(1); // 0: force, 1:normal
            request.writeString("Test Msg : abcdefg hijklm npqrs tuvwxyz 123 456 789 0"); // msg
            request.writeInt(0); // Trigger
            request.writeInt(0); // Trigger Num
        }

        else if(mode == 11) // factory & rescan
        {
            request.writeInt(0); // Trigger
            request.writeInt(0);// Trigger Num
        }

        else if(mode == 12) // block
        {
            request.writeInt(1); // 1:enable, 0:disable
            request.writeInt(0); // Trigger
            request.writeInt(0);// Trigger Num
        }

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "VMXTest ok");
        }
        request.recycle();
        reply.recycle();
    }
//Scoty 20181207 modify VMX OTA rule -s
    public void TestVMXOTA(int mode)
    {
        Log.d(TAG, "TestVMXOTA:  mode="+mode);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMX_VMX_TEST);
        request.writeInt(1);

        if(mode == 1)
        { // OTA
            Log.d(TAG, "TestVMXOTA:  OTA TEST 0 !!!!");
            request.writeInt(0); // mode    0: normal   1:force   2: Err

            request.writeInt(0); // Trigger ID
            request.writeInt(0); // Trigger Num
            request.writeInt(0); // Freq Num
        }
        else if(mode == 2) //Ota Test freqNum == 1
        {
            Log.d(TAG, "TestVMXOTA:  OTA TEST 1 !!!!");
            request.writeInt(0); // mode    0: normal   1:force   2: Err

            request.writeInt(0); // Trigger ID
            request.writeInt(0); // Trigger Num

            request.writeInt(1); // Freq Num
            request.writeInt(659143);
            request.writeInt(0);
        }
        else if(mode == 3) //Ota Test freqNum == 4
        {
            Log.d(TAG, "TestVMXOTA:  OTA TEST 4 !!!!");
            request.writeInt(0); // mode    0: normal   1:force   2: Err

            request.writeInt(0); // Trigger ID
            request.writeInt(0); // Trigger Num
            request.writeInt(4); // Freq Num

            for( int i = 0 ; i < 4; i++)
            {
                //653143,677143,659143,701143
                if(i == 0)
                    request.writeInt(653143);
                else if(i == 1)
                    request.writeInt(677143);
                else if(i == 2)
                    request.writeInt(659143);
                else if(i == 3)
                    request.writeInt(701143);
                request.writeInt(0);
            }
        }

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "TestVMXOTA ok");
        }
        request.recycle();
        reply.recycle();
    }

    public int VMXAutoOTA(int OTAMode, int TriggerID, int TriggerNum, int TunerId, int SatId, int DsmccPid, int FreqNum, ArrayList<Integer> FreqList, ArrayList<Integer> BandwidthList )
    {
        Log.d(TAG, "VMXAutoOTA: ===>> IN OTAMode = " + OTAMode + " SatId = " + SatId + " TunerId = " + TunerId + " TUNER_TYPE = " + TUNER_TYPE
                + " TriggerID = " + TriggerID + " TriggerNum = " + TriggerNum + " DsmccPid = " + DsmccPid + " FreqNum = " + FreqNum );
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_AUTO_OTA);
        request.writeInt(OTAMode);
        request.writeInt(SatId);
        request.writeInt(TunerId);
        request.writeInt(TUNER_TYPE);
        request.writeInt(TriggerID);
        request.writeInt(TriggerNum);
        request.writeInt(DsmccPid);
        request.writeInt(FreqNum);
        for( int i = 0 ; i < FreqNum; i++)
        {
            request.writeInt(FreqList.get(i));
            request.writeInt(BandwidthList.get(i));
        }
        invokeex(request, reply);
        int err = 0;
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "VMXAutoOTA ok");
            err = reply.readInt();
        }

        request.recycle();
        reply.recycle();
        return err;
    }
//Scoty 20181207 modify VMX OTA rule -e
    public String VMXGetBoxID()
    {
        String boxID = "";
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_BOXID);

        invokeex(request, reply);

        boxID = reply.readString();
        Log.d(TAG, "VMXGetBoxID:  boxID = " + boxID);
        request.recycle();
        reply.recycle();

        return boxID;
    }

    public String VMXGetVirtualNumber()
    {
        String virtualNumber = "";
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_GET_VIRTUAL_NUMBER);

        invokeex(request, reply);
        virtualNumber = reply.readString();
        Log.d(TAG, "VMXGetVirtualNumber: virtualNumber =" + virtualNumber);
        request.recycle();
        reply.recycle();

        return virtualNumber;
    }

    public void VMXStopEWBS(int mode)//Scoty 20181225 modify VMX EWBS rule//Scoty 20181218 add stop EWBS
    {
        Log.d(TAG, "VMXStopEWBS: stop mode = " + mode);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_VMX_STOP_EWBS);
        request.writeInt(mode);

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "VMXStopEWBS ok");
        }
        request.recycle();
        reply.recycle();
    }

    public void VMXStopEMM()
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMX_VMX_STOP_EMM);

        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "VMXStopEMM ok");
        }
        request.recycle();
        reply.recycle();
    } // connie 20180903 for VMX -e


    public void VMXOsmFinish(int triggerID, int triggerNum)
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMX_VMX_OSM_FINISH);
        request.writeInt(triggerID);
        request.writeInt(triggerNum);
        invokeex(request, reply);
        int ret = reply.readInt();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "VMXOsmFinish ok");
        }
        request.recycle();
        reply.recycle();
    } // connie 20180903 for VMX -e

    private int dvbcVMXScan(TVScanParams sp, int startTPID, int endTPID) // connie 20180919 add for vmx search -s
    {
        Log.d(TAG, "dvbcVMXScan:    startTPID = " + startTPID + "     endTPID =" + endTPID);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);

        request.writeInt(CMD_CS_BlockSearch);

        // scan params
        request.writeInt(sp.getSatId());
        request.writeInt(sp.getTunerId());
        request.writeInt(EnNetworkType.CABLE.getValue());
        request.writeInt(sp.getSearchOptionCaFta());
        request.writeInt(sp.getSearchOptionTVRadio());

        Log.d(TAG, "dvbcVMXScan:   getSatId() = " + sp.getSatId() + "     getTunerId = " + sp.getTunerId() + "   NetworkType = " + EnNetworkType.CABLE.getValue()
              + "   getSearchOptionCaFta = " + sp.getSearchOptionCaFta() + "    getSearchOptionTVRadio=" + sp.getSearchOptionTVRadio());

        Locale locale = Locale.getDefault();
        Log.e(TAG, "languange :" + locale.getISO3Language());
        String systemLanguage = locale.getISO3Language();
        request.writeString(systemLanguage);

        // scan tp
        request.writeInt(sp.getTpInfo().getTpId());
        request.writeInt(sp.getTpInfo().CableTp.getFreq());
        request.writeInt(sp.getTpInfo().CableTp.getSymbol());
        request.writeInt(getEnModFromPesiQam(sp.getTpInfo().CableTp.getQam()));
        request.writeInt(0);
        request.writeInt(startTPID);
        request.writeInt(endTPID);
        Log.d(TAG, "dvbcVMXScan:  getTpId() = " + sp.getTpInfo().getTpId() + "    getFreq() = " + sp.getTpInfo().CableTp.getFreq() +  "   Qam = " + getEnModFromPesiQam(sp.getTpInfo().CableTp.getQam()));

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        return getReturnValue(ret);
    }
    private int dvbsVMXScan(TVScanParams sp, int startTPID, int endTPID)
    {
        return 0;
    }
    private int dvbtVMXScan(TVScanParams sp, int startTPID, int endTPID)
    {
        return 0;
    }

    private int isdbtVMXScan(TVScanParams sp, int startTPID, int endTPID)
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);

        // johnny modify 2080418 for nit search
        // hisi has no nit search in isdbt
       request.writeInt(CMD_CS_BlockSearch);
        request.writeInt(sp.getSatId());
        request.writeInt(sp.getTunerId());
        request.writeInt(EnNetworkType.ISDB_TER.getValue());
        request.writeInt(sp.getSearchOptionCaFta());
        request.writeInt(sp.getSearchOptionTVRadio());

        Locale locale = Locale.getDefault();
        Log.e(TAG, "languange :" + locale.getISO3Language());
        String systemLanguage = locale.getISO3Language();
        request.writeString(systemLanguage);

        request.writeInt(sp.getOneSegment());
        request.writeInt(sp.getTpInfo().getTpId());
        request.writeInt(sp.getTpInfo().TerrTp.getFreq());
        request.writeInt(getBandForWrite(sp.getTpInfo().TerrTp.getBand()));
        request.writeInt(0);   // ignore
        request.writeInt(0);
        request.writeInt(startTPID);
        request.writeInt(endTPID);

        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();

        return getReturnValue(ret);

    } // connie 20180919 add for vmx search-e

    public VMXProtectData GetProtectData()
    {
        Log.d(TAG, "GetProtectData: ");

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_CFG_GetProtectData);
        invokeex(request, reply);

        int ret = 0;//reply.readInt();
        VMXProtectData data = new VMXProtectData();
        if (CMD_RETURN_VALUE_SUCCESS == ret)
        {
            Log.d(TAG, "GetProtectData ok");
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

    public int SetProtectData(int first, int second, int third)
    {
        Log.d(TAG, "SetProtectData: first =" + first + "      second =" + second + "       third ="+third);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_CFG_SetProtectData);
        request.writeInt(first);
        request.writeInt(second);
        request.writeInt(third);
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }
    // for VMX need open/close -e

    public int EnterViewActivity(int enter)
    {
        Log.d(TAG, "EnterViewActivity:    enter = " + enter);
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_CFG_SetInViewActivity);
        request.writeInt(enter);
        invokeex(request, reply);
        int ret = reply.readInt();
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int EnableMemStatusCheck(int enable)
    {
//        Log.d(TAG, "EnableMemStatusCheck:    enable = " + enable);
//        Parcel request = Parcel.obtain();
//        Parcel reply = Parcel.obtain();
//        request.writeString(DTV_INTERFACE_NAME);
//        request.writeInt(CMD_CFG_EnableMemStatusCheck);
//        request.writeInt(enable);
//        invokeex(request, reply);
//        int ret = reply.readInt();
//        request.recycle();
//        reply.recycle();
        return 0;
    }

    public int LoaderDtvGetJTAG()
    {
        Log.d(TAG, "LoaderDtvGetJTAG");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_LOADERDTV_GET_JTAG);
        invokeex(request, reply);
        int boot_fuse = reply.readInt();
        Log.d(TAG, "LoaderDtvGetBootFuse boot_fuse = "+ boot_fuse );
        request.recycle();
        reply.recycle();
        return boot_fuse;
    }

    public int LoaderDtvSetJTAG(int value)
    {
        Log.d(TAG, "LoaderDtvSetJTAG");
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_LOADERDTV_SET_JTAG);
        request.writeInt(value);
        invokeex(request, reply);
        int result = reply.readInt();
        request.recycle();
        reply.recycle();
        return result;
    }

    public int LoaderDtvCheckISDBTService(OTATerrParameters ota)
    {
        int freq = ota.frequency*1000+143;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        Log.d(TAG, "LoaderDtvCheckISDBTService");
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_LOADERDTV_CHECK_DSMCCS_SERVICE);

        request.writeInt(0); //sat id
        request.writeInt(0); //tuner id
        request.writeInt(EnNetworkType.ISDB_TER.getValue()); //networktype

        request.writeInt(freq);
        request.writeInt(ota.bandWidth);
        request.writeInt(ota.modulation);
        request.writeInt(0); //version
        request.writeInt(ota.pid);
        invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "LoaderDtvCheckISDBTService:    result = " + ret);
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int LoaderDtvCheckTerrestrialService(OTATerrParameters ota)
    {
        int freq = ota.frequency*1000+143;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        Log.d(TAG, "LoaderDtvCheckISDBTService");
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_LOADERDTV_CHECK_DSMCCS_SERVICE);

        request.writeInt(0); //sat id
        request.writeInt(0); //tuner id
        request.writeInt(EnNetworkType.TERRESTRIAL.getValue()); //networktype

        request.writeInt(freq);
        request.writeInt(ota.bandWidth);
        request.writeInt(ota.modulation);
        request.writeInt(0); //version
        request.writeInt(ota.pid);
        invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "LoaderDtvCheckISDBTService:    result = " + ret);
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int LoaderDtvCheckCableService(OTACableParameters ota)
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_LOADERDTV_CHECK_DSMCCS_SERVICE);

        request.writeInt(0); //sat id
        request.writeInt(0); //tuner id
        request.writeInt(EnNetworkType.CABLE.getValue()); //networktype

        request.writeInt( ota.frequency*1000);
        request.writeInt(ota.symbolRate);
        request.writeInt(ota.modulation);
        request.writeInt(0); //version
        request.writeInt(ota.pid);
        invokeex(request, reply);
        int ret = reply.readInt();
        Log.d(TAG, "LoaderDtvCheckCableService:    result = " + ret);
        request.recycle();
        reply.recycle();
        return getReturnValue(ret);
    }

    public int LoaderDtvGetSTBSN()
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_LOADERDTV_GET_STBSN);
        invokeex(request, reply);
        int ret = reply.readInt();
        int stbsn;
        if(ret == 0)
            stbsn = reply.readInt();
        else
            stbsn = 0x0000000F;
        Log.d(TAG, "LoaderDtvGetSTBSN:    result = " + ret + " stbsn = "+ stbsn) ;
        request.recycle();
        reply.recycle();
        return stbsn;
    }

    public int LoaderDtvGetChipSetId()
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_LOADERDTV_GET_CHIPSET_ID);
        invokeex(request, reply);
        int ret = reply.readInt();
        int chip_id;
        if(ret == 0)
            chip_id = reply.readInt();
        else
            chip_id = 0x0000000F;
        Log.d(TAG, "LoaderDtvGetChipSetId:    result = " + ret + " chip_id = "+ chip_id);
        request.recycle();
        reply.recycle();
        return chip_id;
    }

    public int LoaderDtvGetSWVersion()
    {
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_LOADERDTV_GET_SWAREVER);
        invokeex(request, reply);
        int ret = reply.readInt();
        int sw_ver;
        if(ret == 0)
            sw_ver = reply.readInt();
        else
            sw_ver = 0x0000000F;
        Log.d(TAG, "LoaderDtvGetSWVersion:    result = " + ret + " sw_ver = "+ sw_ver);
        request.recycle();
        reply.recycle();
        return sw_ver;
    }

    public static long getUnsignedInt(int signedInt)
    {
        return (signedInt & 0xFFFFFFFFL);
    }

    public int InvokeTest() {
        int result = -1;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_COMM_TestInvoke);
        invokeex(request, reply);
        int ret = reply.readInt();
        if(ret == 0) {
            String interfaceName = reply.readString();
            Log.d(TAG, "InvokeTest:    result = " + ret + " interfaceName = " + interfaceName);
            if (interfaceName.equals(DTV_INTERFACE_NAME))
                result = 0;
        }
        request.recycle();
        reply.recycle();
        return result;
    }
    public void WidevineCasSessionId(int sessionIndex, int sessionId) {//eric lin 20210107 widevine cas
        //int result = -1;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeString(DTV_INTERFACE_NAME);
        request.writeInt(CMD_CA_WIDEVINE_CAS);
        request.writeInt(INVOKE_CA_CMD_READ_SESSION); //cmd
        request.writeInt(sessionIndex); //need fix
        request.writeInt(sessionId); //data
        invokeex(request, reply);
        Log.d(TAG, "WidevineCasSessionId() sessionIndex=" + sessionIndex + ", session id="+sessionId);//eric lin test
        /*
        int ret = reply.readInt();
        Log.d(TAG, "WidevineCasSessionId() session id ="+sessionId);//eric lin test
        if(ret == 0) {
            Log.d(TAG, "WidevineCasSessionId() set session id success!!!");
            result = 0;
        }else
            Log.d(TAG, "WidevineCasSessionId() set session id fail!!!");
         */
        request.recycle();
        reply.recycle();
        //return result;
    }

    public int SetNetStreamInfo(int GroupType, NetProgramInfo netStreamInfo)
    {
        for(int i = 0 ; i < TotalChannelList.get(GroupType).size() ; i++)
        {
            if(TotalChannelList.get(GroupType).get(i).getChannelId() == netStreamInfo.getChannelId()) {
                return 0;
            }
        }

        SimpleChannel tmpNetSimpleChannel = new SimpleChannel();
        tmpNetSimpleChannel.setChannelId(netStreamInfo.getChannelId());
        tmpNetSimpleChannel.setChannelName(netStreamInfo.getChannelName());
        tmpNetSimpleChannel.setChannelNum(netStreamInfo.getChannelNum());
        tmpNetSimpleChannel.setUrl(netStreamInfo.getVideoUrl());
        tmpNetSimpleChannel.setUserLock(netStreamInfo.getUserLock());
        tmpNetSimpleChannel.setPVRSkip(0);
        tmpNetSimpleChannel.setCA(0);
        tmpNetSimpleChannel.setPlayStreamType(netStreamInfo.getPlayStreamType());//0 : DVB; 1 : VOD; 2: Youtube
        tmpNetSimpleChannel.setPresentepgEvent(netStreamInfo.getPresentepgEvent());
        tmpNetSimpleChannel.setFollowepgEvent(netStreamInfo.getFollowepgEvent());
        tmpNetSimpleChannel.setShortEvent(netStreamInfo.getShortEvent());
        tmpNetSimpleChannel.setDetailInfo(netStreamInfo.getDetailInfo());
        TotalChannelList.get(GroupType).add(tmpNetSimpleChannel);
        return 1;
    }

    public void SetExoPlayer(SimpleExoPlayer player)
    {
        mExoPlayer = player;
    }

    public SimpleExoPlayer GetExoPlayer()
    {
        return mExoPlayer;
    }

    public void SetExoplayerSurfaceView(SurfaceView surfaceView)
    {
        mSurfaceViewExoplayer = surfaceView;
    }

    public SurfaceView GetExoplayerSurfaceView()
    {
        return mSurfaceViewExoplayer;
    }

    public void SetYoutubeWebview(WebView webview)
    {
        mWebView = webview;
    }

    public WebView GetYoutubeWebview()
    {
        return mWebView;
    }

    public int ResetProgramDatabase()
    {
        int ret = 0;
        DataManager mDataManager = DataManager.getDataManager();
        DataManager.ProgramDatabase mProgramDatabase = mDataManager.GetProgramDatabase();

        mProgramDatabase.ResetDatabase(mContext);

        return getReturnValue(ret);
    }

    public int ResetNetProgramDatabase()
    {
        int ret = 0;
        DataManager mDataManager = DataManager.getDataManager();
        DataManager.NetProgramDatabase mNetProgramDatabase = mDataManager.GetNetProgramDatabase();

        mNetProgramDatabase.ResetDatabase(mContext);

        return getReturnValue(ret);
    }


    /**
     * First Time Get netprogram.ini File and Add to DataBase
     * After Save Complete Rename to netprogram_already_set.ini
     * In order to not to save database again
     * @return isSuccess : Save DataBase Results
     */
    public boolean InitNetProgramDatabase()
    {
        Log.d(TAG, "exce InitNetProgramDatabase: IN");
        DataManager.NetProgramDatabase mNetProgramDatabase = DataManager.getDataManager().GetNetProgramDatabase();
        if(mNetProgramDatabase == null)
            Log.e(TAG, "InitNetProgramDatabase: mNetProgramDatabase is NULL");

        String VOD_INI_TITLE = "[VOD]", YOUTUBE_INI_TITLE = "[YOUTUBE]", EANABLE_NETPROGRAMS = "[ENABLE_NETPROGRAMS]";
        String VOD_ENABLE = "ENABLE", VOD_TRUE = "TRUE", VOD_PARAMS_FILTER = "==";
        String INI_READ_PATH = "/vendor/etc/dtvplayer_settings/netprogram.ini";
        File readFile = new File(INI_READ_PATH);
        if(!readFile.exists()) {
            Log.d(TAG, "InitNetProgramDatabase: netprogram.ini Not Exist!");
            Pvcfg.SetEnableNetworkPrograms(false);
            return false;
        }

        String line;
        List<NetProgramInfo> netProgramInfoList = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(readFile);
            BufferedReader breader = new BufferedReader(fileReader);
            while ((line=breader.readLine()) != null) {
                NetProgramInfo tmpNetProgramInfo = new NetProgramInfo();
                Log.d(TAG, "InitNetProgramDatabase: get NetProgram Title [" + line +"]");
                if(line.equals(VOD_INI_TITLE) || line.equals(YOUTUBE_INI_TITLE))
                {
                    tmpNetProgramInfo = setNetProgramfromIniFile(breader);
                    netProgramInfoList.add(tmpNetProgramInfo);
                }else if(line.equals(EANABLE_NETPROGRAMS))//Check Add YOUTUBE & VOD or not
                {
                    line=breader.readLine();
                    String[] EnableNetProgramText = line.split(VOD_PARAMS_FILTER);
                    if(EnableNetProgramText[0].toUpperCase().equals(VOD_ENABLE))
                    {
                        // Enable or Disable Netprograms
                        Pvcfg.SetEnableNetworkPrograms(EnableNetProgramText[1].toUpperCase().equals(VOD_TRUE));
                    }

                    if(!Pvcfg.IsEnableNetworkPrograms())// No Need to Save Database if false
                        return false;

                }
            }
            breader.close();

        }catch (IOException e) {
            return false;
        }

        //Check NetPrograms are already saved in dB or not, if exist no need to save again (Reboot to open DTVPlayer)
        if(mNetProgramDatabase != null) {
            if (mNetProgramDatabase.GetNetProgramList(mContext).size() > 0) {
                Log.e(TAG, "InitNetProgramDatabase: NetPrograms are already saved, no need to save again");
                return false;
            }
        }

        boolean isSuccess = SaveNetProgramList(netProgramInfoList);
        Log.d(TAG, "InitNetProgramDatabase: Save Database isSuccess = [" + isSuccess + "]");

        return isSuccess;

    }

    /**
     * Read netprogram.ini File and Save Params to NetProgramInfo Database
     * @param reader Read netprogram.ini
     * @return NetprogramInfo
     */
    private NetProgramInfo setNetProgramfromIniFile(BufferedReader reader)
    {
        Log.d(TAG, "exce setNetProgramfromIniFile: ");
        String iniFileInfo;
        String PARAMS_END = "[END]";
        String NET_SERVICE_ID_TAG = "ServiceId";
        String NET_PLAY_STREAM_TYPE_TAG = "PlayStreamType";
        String NET_VIDEO_URL_TAG = "videoUrl";
        String NET_CHANNEL_NAME_TAG = "ChannelName";
        String NET_CHANNEL_NUM_TAG = "ChannelNum";
        NetProgramInfo netStreamInfo = new NetProgramInfo();
        try {
            while(!(iniFileInfo=reader.readLine()).equals(PARAMS_END))
            {
                Log.d(TAG, "exce setVodProgramDatabase: get = [" + iniFileInfo +"]");
                String[] textsplit = iniFileInfo.split("==");
                if(textsplit[0].equals(NET_SERVICE_ID_TAG))
                {
                    int serviceId = parseInt(textsplit[1]);
//                    int tpSize = TpInfoGetList(getTunerType()).size();
                    int tpSize = getTPList(getTunerType(), MiscDefine.TpInfo.NONE_SAT_ID,MiscDefine.TpInfo.POS_ALL,MiscDefine.TpInfo.NUM_ALL).size();
                    int channelId = (serviceId << 16) + (tpSize + 1);
                    netStreamInfo.setChannelId(channelId);
                }else if(textsplit[0].equals(NET_PLAY_STREAM_TYPE_TAG)){
                    netStreamInfo.setPlayStreamType(parseInt(textsplit[1]));
                }else if(textsplit[0].equals(NET_VIDEO_URL_TAG)){
                    netStreamInfo.setVideoUrl(textsplit[1]);
                }else if(textsplit[0].equals(NET_CHANNEL_NAME_TAG)){
                    netStreamInfo.setChannelName(textsplit[1]);
                }else if(textsplit[0].equals(NET_CHANNEL_NUM_TAG)){
                    netStreamInfo.setChannelNum(parseInt(textsplit[1]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return netStreamInfo;
    }

    /**
     * netProgramlist from netprogram.ini
     * @param netProgramlist Saved NetPrograms List
     * @return isSuccess Save DataBase Results
     */
    private boolean SaveNetProgramList(List<NetProgramInfo> netProgramlist)
    {
//        for(int i = 0 ; i < netProgramlist.size() ; i++)
//            Log.d(TAG, "exce SaveNetProgramList: " + netProgramlist.get(i).ToString());

        //Save NetProgramInfo Database
        DataManager.NetProgramDatabase mNetProgramDatabase = DataManager.getDataManager().GetNetProgramDatabase();
        boolean isSuccess = mNetProgramDatabase.SaveNetProgramListDatabase(mContext,netProgramlist); //Clear and Add
        Log.d(TAG, "SaveNetProgramList: isSuccess = [" + isSuccess +"]");

        return isSuccess;
    }

//////////////////////////////////////////////////////////////////////////
    /**
     //     * After Search complete, add VOD & YOUTUBE Programs to TotalChannelList
     //     * @param groupType
     //     */
//    private void AddNetProgramToTotalChannelList(int groupType)
//    {
//        DataManager.NetProgramDatabase mNetProgramDatabase = DataManager.getDataManager().GetNetProgramDatabase();
//        List<NetProgramInfo> netprogramList = mNetProgramDatabase.GetNetProgramList(mContext);
//        for(int i = 0 ; i < netprogramList.size() ; i++)
//            SetNetStreamInfo(groupType, netprogramList.get(i));
//    }


}
