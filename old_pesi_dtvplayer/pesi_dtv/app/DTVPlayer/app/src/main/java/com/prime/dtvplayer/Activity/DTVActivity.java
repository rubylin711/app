package com.prime.dtvplayer.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import com.dolphin.dtv.AvSetRatio;
import com.dolphin.dtv.EnTableType;
import com.dolphin.dtv.EnTrickMode;
import com.dolphin.dtv.HiDtvMediaPlayer;
import com.dolphin.dtv.PVREncryption;
import com.dolphin.dtv.PvrFileInfo;
import com.dolphin.dtv.Resolution;
import com.dolphin.dtv.UsbPortNum;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.loader.structure.OTACableParameters;
import com.loader.structure.OTATerrParameters;
import com.mtest.config.HwTestConfig;
import com.mtest.utils.LocaleHelper;
import com.mtest.utils.PesiStorageHelper;
import com.prime.dtvplayer.Config.Pvcfg;
import com.prime.dtvplayer.Database.DatabaseHandler;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Service.DataManager.DataManager;
import com.prime.dtvplayer.Sysdata.AudioInfo;
import com.prime.dtvplayer.Sysdata.BookInfo;
import com.prime.dtvplayer.Sysdata.CaStatus;
import com.prime.dtvplayer.Sysdata.ChannelHistory;
import com.prime.dtvplayer.Sysdata.DefaultChannel;
import com.prime.dtvplayer.Sysdata.EPGEvent;
import com.prime.dtvplayer.Sysdata.EnAudioTrackMode;
import com.prime.dtvplayer.Sysdata.FavGroupName;
import com.prime.dtvplayer.Sysdata.FavInfo;
import com.prime.dtvplayer.Sysdata.GposInfo;
import com.prime.dtvplayer.Sysdata.LoaderInfo;
import com.prime.dtvplayer.Sysdata.MailInfo;
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
import com.prime.dtvplayer.View.MessageDialogView;
import com.prime.dtvplayer.View.PasswordDialogView;
import com.prime.dtvplayer.View.VMXPinDialogView;
import com.prime.dtvplayer.utils.TVMessage;
import com.prime.dtvplayer.utils.TVScanParams;
import com.prime.dtvplayer.utils.TVTunerParams;
import android.media.MediaCas;//eric lin 20210107 widevine cas
import android.webkit.WebView;
import android.widget.Toast;

import com.prime.dtvplayer.cas.CasInitData;//eric lin 20210107 widevine cas
import com.prime.dtvplayer.cas.CasSessionManager;//eric lin 20210107 widevine cas
import com.prime.dtvplayer.cas.DefaultCasSessionManager;//eric lin 20210107 widevine cas
import com.prime.dtvplayer.cas.ExoMediaCas;//eric lin 20210107 widevine cas
import com.prime.dtvplayer.cas.FrameworkMediaCas;//eric lin 20210107 widevine cas
import com.prime.dtvplayer.cas.HttpMediaCasCallback;//eric lin 20210107 widevine cas
import com.prime.dtvplayer.cas.UnsupportedCasException;//eric lin 20210107 widevine cas
import com.prime.dtvplayer.cas.WidevineCasSession;//eric lin 20210107 widevine cas
import com.prime.dtvplayer.cas.WidevineCasSessionManager;//eric lin 20210107 widevine cas
import com.prime.dtvplayer.cas.CasSession;//eric lin 20210107 widevine cas
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;//eric lin 20210107 widevine cas
import com.google.android.exoplayer2.upstream.HttpDataSource;//eric lin 20210107 widevine cas
import com.google.android.exoplayer2.util.Util;//eric lin 20210107 widevine cas

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.prime.dtvplayer.utils.TVMessage.TYPE_CA_MV_MSG;//eric lin 20210107 widevine cas
import static java.lang.Integer.parseInt;
//import static com.android.internal.util.HexDump.hexStringToByteArray;//eric lin 20210107 widevine cas

/**
 * Created by gary_hsu on 2017/10/31.
 */

@SuppressWarnings("LossyEncoding")
public class DTVActivity extends HisiTVActivity { //TVActivity or HisiTVActivity
    private static final String TAG = "DTVActivity";
    public ChannelHistory ViewHistory = ChannelHistory.GetInstance();
    //public List<FavGroupName> AllProgramGroup = new ArrayList<FavGroupName>();
    //public List<ProgramManagerImpl> ProgramManagerList = new ArrayList<ProgramManagerImpl>(); //save in HiDtvMediaPlayer
    //public List<OkListManagerImpl> OkListManagerList = new ArrayList<OkListManagerImpl>();
    //public ViewUiDisplay viewUiDisplay = null;
    //public EpgUiDisplay epgUiDisplay = null;
    //public BookManager bookManager = null;
    //public int CurTunerType = 0;
    //public int ChannelExistFlag = 0;//program exist = 1; program not exist = 0;
    //private StorageManager mStorageManager ;
    //private MessageDialogView usbMountDialog = null;
    //public  MessageDialogView usbUnMountDialog = null; // Edwin 20181211 add detect of usb in Storage Settings
    //private static final long MINUTE_IN_MILLIS = TimeUnit.MINUTES.toMillis(1);
    //public int mRecID;//eric lin 20180712 pesi pvr for one rec
    //public UsbPortNum mUsbPort = new UsbPortNum();//Scoty 20181024 add for 3796/3798 usb port model

    public static final int STATUS_CH_BLOCK = 1;
    public static final int STATUS_CH_NO_BLOCK = 0;
    private static int VMXBlockFlag = 0; // for VMX need open/close
    private static int VMXLocationSetting = 1; // for VMX need open/close
    private static VMXPinDialogView VMXPinDialog = null;
    private static DatabaseHandler MailHandler = null;
    public static List<MailInfo> DataBaseMailList = new ArrayList<>();
    private String MailTableName = "mailTable" ;
    //public static final int DSMCC_PID = 7000;//Scoty 20181207 modify VMX OTA rule
    private HwTestConfig.MountCallback mtestMountCallback;//gary20200504 add HW test config get from usb
    //private static final String DTV_LAUNCHER_PACKAGE_NAME = "com.pesilauncher.pesilauncher";
    //private static final String DTV_SET_LAUNCHER_PLAYCHANNEL = "com.prime.launcher.playchannel";
    static private int CAS_SESSION_MAX=10;//eric lin 20210107 widevine cas
    static private CasSession[] casSessionArray;//eric lin 20210107 widevine cas
    static private int esSessionCount;//eric lin 20210107 widevine cas
    static private sessionRecord[] sessionRec;//eric lin 20210107 widevine cas
    static private CasSessionManager casSessionManager = null;//eric lin 20210107 widevine cas
    static protected String userAgent;//eric lin 20210107 widevine cas    
    private static TVMessage.widevineMsg msgOpenSessionData;//eric lin 20210107 widevine cas
    byte[] privateData;//eric lin 20210112 widevine scheme data
    byte[] caPID;//eric lin 20210112 widevine scheme data

    protected static long playingChannelId; // Johnny 20210423 temp for RTK avplay issue

    public static int CurSubtitleIndex = 0;

    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        ProgramGroupInit();
        ViewHistoryInit();
        //TunerTypeInit();
        SubBroadcast();
//        InitNetProgramDatabase();
        //mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        if(Pvcfg.getCAType() == Pvcfg.CA_VMX && MailHandler == null) // connie 20181116 for vmx mail
            MailInit();
        userAgent = Util.getUserAgent(this, "ExoPlayerDemo");//eric lin 20210107 widevine cas
        casSessionArray = new CasSession[CAS_SESSION_MAX];//eric lin 20210107 widevine cas
        sessionRec = new sessionRecord[CAS_SESSION_MAX];//eric lin 20210107 widevine cas
        caPID = new byte[2];//eric lin 20210112 widevine scheme data
    }

    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        unSubBroadcast();
    }

    public void pipModSetDisplay(Surface surface, int type)
    {
        super.pipModSetDisplay(surface, type);
    }

    public void pipModClearDisplay(Surface surface)
    {
        super.pipModClearDisplay(surface);
    }

    public void setSurfaceView(SurfaceView surface)
    {
        super.setSurfaceView(surface);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        onDisconnected();
        super.onPause();
    }

    @Override
    protected  void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        onConnected();

    }

    @Override
    public void onConnected() {
        Log.d(TAG, "DTV onConnected");
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "DTV onDisconnected");
    }

    @Override //?????call super.onMessage ?????????
    public void onMessage(TVMessage tvMessage)
    {
        //Log.d("WidevineCasSessionMgr", "DTVA onMessage() MsgType=="+tvMessage.getMsgType());
        switch (tvMessage.getMsgType()) {
          case TVMessage.TYPE_MTEST_SERVICE_DIED: // edwin 20201214 add HwBinder.DeathRecipient
          {
//            runOnUiThread(() -> {
//                new MessageDialogView(DTVActivity.this, "Service Died !!!", 0) {
//                    @Override
//                    public void dialogEnd() {}
//                }.show();
//            });

              Toast.makeText(this, "Service Died !!!", Toast.LENGTH_SHORT);
              Log.d(TAG, "onMessage: Service Died !!!");
          }break;
            case TYPE_CA_MV_MSG: {//eric lin 20210107 widevine cas , add by gary
                //Log.d(TAG, "DTVA onMessage() TYPE_CA_MV_MSG:");
                if(mHandler != null) {
                    Log.d(TAG, "DTVA onMessage() TYPE_CA_MV_MSG: 1 getWvEventId()="+tvMessage.getWvEventId());
                    //byte[] val = tvMessage.getWvMsg();
                    //Log.d(TAG, "DTVA onMessage() handleMessage() 2 data[0]="+String.format("%02X ", val[0])+", last data="+String.format("%02X ", val[val.length-1]));
                    sendMessage(mHandler, tvMessage.getWvEventId(), 0, tvMessage.getWvMsgData());
                }
                //Log.d(TAG, "DTVA onMessage() TYPE_CA_MV_MSG: end");
            }break;
        }
    }

    /**
     * Johnny 20201220 set default locale of Mtest to "zh"
     * The locale of Mtest is persist and stored in sharedPreference.
     *
     * Clear app data in android app setting or remove SELECTED_LANGUAGE in sharedPreference
     * if you change the default locale.
     *
     * Remove this if you want to change locale based on system locale
     * but note that values-zh(simplified chinese) does not work for zh-TW.
     * see https://issuetracker.google.com/issues/37102249
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "zh"));
    }

    public int InvokeTest() {
        return super.InvokeTest();
    }

//    public StorageManager getStorageManager ()
//    {
//        return mStorageManager;
//    }

//    private void ChannelExitFlagInit()
//    {
//        ChannelExistFlag = GetChannelExist();
//    }

    public void SetChannelExist(int exist)
    {
        super.setChannelExist(exist);
    }

    public int GetChannelExist()
    {
        return super.getChannelExist();
    }


    public ArrayList<FavGroupName> GetAllProgramGroup()
    {
        return super.GetAllProgramGroup();
    }

//    private void TunerTypeInit()
//    {
//        CurTunerType = GetCurTunerType();
//    }

    public int GetCurTunerType()
    {
        return super.getTunerType();
    }

    private void ProgramGroupInit() {
        if(GetAllProgramGroup().size() != ProgramInfo.ALL_TV_RADIO_TYPE_MAX) {
            for(int i = ProgramInfo.ALL_TV_TYPE; i < ProgramInfo.ALL_TV_RADIO_TYPE_MAX; i++) {
                FavGroupName GroupName = new FavGroupName();
                GroupName.setGroupType(i);
                GroupName.setGroupName(GroupNameGet(i));
                GetAllProgramGroup().add(GroupName);
            }
        }
    }

    private int getplatForm()
    {
        return super.getPlatform();
    }

    public int getTempPesiDefaultChannelFlag()
    {
        return super.getTempPesiDefaultChannelFlag();
    }

    public void setTempPesiDefaultChannelFlag(int flag)
    {
        super.setTempPesiDefaultChannelFlag(flag);
    }

    private void ViewHistoryInit() {
        if(ViewHistory == null)
            Log.d(TAG, "ViewHistoryInit:NULL ");
        //super.ProgramInfoPlaySimpleChannelListInit(0);
//        if(ViewHistory.getCurChannel() == null) {
        {
            if(getplatForm() == 1) {//pesi platform
                if (getTempPesiDefaultChannelFlag() == 1)
                    SetChannelExist(0);
                else
                {
                    SimpleChannel channel = null;
                    List<SimpleChannel> channelList = null;
                    List<ProgramInfo> progList;
                    ProgramInfo program;

                    if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798 && Pvcfg.getCAType() == Pvcfg.CA_VMX)
                    {
                        //Scoty 20181228 fixed VMX after boot change wrong channel -s
                        program = ProgramInfoGetByLcn(1, ProgramInfo.ALL_TV_TYPE);
                        if(program != null) {
                            channel = GetSimpleProgramByChannelIdfromTotalChannelList(program.getChannelId());
                            channelList = ProgramInfoGetPlaySimpleChannelList(program.getType(), 0);
                            SetChannelExist(1);
                            ViewHistory.SetCurChannel(channel, channelList, program.getType());
                            AvControlAudioOutput(ViewHistory.getPlayId(), GposInfoGet().getDolbyMode());
                            return;
                        }
                        else
                        {
                            progList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE); // Edwin 20181204 change to first channel
                            program = ( progList == null ) ? null : progList.get(0);
                            if(program != null)
                            {
                                channel = GetSimpleProgramByChannelIdfromTotalChannelList(program.getChannelId());
                                channelList = ProgramInfoGetPlaySimpleChannelList(program.getType(), 0);
                                SetChannelExist(1);
                                ViewHistory.SetCurChannel(channel, channelList, program.getType());
                                AvControlAudioOutput(ViewHistory.getPlayId(), GposInfoGet().getDolbyMode());
                                return;
                            }
                        }
                        //Scoty 20181228 fixed VMX after boot change wrong channel -e
                    }

                    //Scoty Add Youtube/Vod Stream -s
                    if (Pvcfg.IsEnableNetworkPrograms()) {
                        final DefaultChannel DefaultChannel = getDefaultOpenChannel();
//                        final DefaultChannel DefaultChannel = getDefaultOpenChannelfromProgramDataBase();//Scoty Add Youtube/Vod Stream Default channel from database
                        if (DefaultChannel == null) {
                            Log.e(TAG, "ViewHistoryInit: DataBase Programs Not Exist");
                            SetChannelExist(0);
                        } else {
                            Log.d(TAG, "ViewHistoryInit: DataBase Programs Exist");
                            channelList = ProgramInfoGetPlaySimpleChannelList(DefaultChannel.getGroupType(), 0);
                            channel = GetSimpleProgramByChannelIdfromTotalChannelList(DefaultChannel.getChanneId());
                            SetChannelExist(1);
                            ViewHistory.SetCurChannel(channel, channelList, DefaultChannel.getGroupType());
                            AvControlAudioOutput(ViewHistory.getPlayId(), GposInfoGet().getDolbyMode());//Scoty 20180806 should set audio output when boot
                        }
                    }
                    //Scoty Add Youtube/Vod Stream -e
                    else {
                        final DefaultChannel DefaultChannel = getDefaultOpenChannel();
                        if (DefaultChannel == null) {
                            Log.d(TAG, "ViewHistoryInit: AA DefaultChannel is null " + getplatForm());
                            SetChannelExist(0);
                        } else {
                            Log.d(TAG, "ViewHistoryInit: BB DefaultChannel is Not null " + getplatForm());

                            if (DefaultChannel.getChanneId() > 0) {
                                long channelId = DefaultChannel.getChanneId();
                                int groupType = DefaultChannel.getGroupType();
                                Log.d(TAG, "ViewHistoryInit:  groupType = " + groupType);
                                program = ProgramInfoGetByChannelId(channelId); // scoty fix crash when delete all tv channel
                                if (program != null) {  // Johnny 20180821 add protection
                                    int serviceType = program.getType();
                                    if (serviceType != groupType) {
                                        groupType = serviceType;
                                    }

                                    Log.d(TAG, "ViewHistoryInit: 11 groupType = " + serviceType + "groupType = " + groupType);
                                    SetChannelExist(1);
                                    channel = GetSimpleProgramByChannelIdfromTotalChannelList(program.getChannelId());//Scoty 20180529 fixed after boot should get channel and list
                                }
                                channelList = ProgramInfoGetPlaySimpleChannelList(groupType, 0);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule//Scoty 20180529 fixed after boot should get channel and list
                                if (channel == null) {
                                    SetChannelExist(0);
                                } else {
                                    SetChannelExist(1);
                                    ViewHistory.SetCurChannel(channel, channelList, groupType);
                                    AvControlAudioOutput(ViewHistory.getPlayId(), GposInfoGet().getDolbyMode());//Scoty 20180806 should set audio output when boot
                                }
                                //AvControlOpen(ViewHistory.getPlayId());
                                //ViewHistory.getCurChannel().setChannelId(DefaultChannel.getChanneId());
                                //ViewHistory.setCurGroupType(DefaultChannel.getGroupType());
                                //AvControlPlayByChannelId(ViewHistory.getPlayId(), DefaultChannel.getChanneId(), DefaultChannel.getGroupType(),1);
                            } else
                                SetChannelExist(0);

                        }
                    }

                }
            }
            else {//Hisillion platform
                final DefaultChannel DefaultChannel = getDefaultOpenChannel();
                int show = 1;
                if(DefaultChannel == null) {
                    Log.d(TAG, "ViewHistoryInit: DefaultChannel is null + "+ getplatForm());
                    SetChannelExist(0);
                } else {
                    Log.d(TAG, "ViewHistoryInit: DefaultChannel is Not null " + getplatForm());
                    SetChannelExist(1);
                    AvControlOpen(ViewHistory.getPlayId());
                    AvControlPlayByChannelId(ViewHistory.getPlayId(), DefaultChannel.getChanneId(), DefaultChannel.getGroupType(),1);
                }
            }
//        }else{
//            SetChannelExist(1);
        }
    }

    public int ScanResultSetChannel() {
        List<SimpleChannel> simpleChannelList = null;
        SimpleChannel simpleChannel = null;
        int groupType = ProgramInfo.ALL_TV_TYPE;
        Log.d(TAG,"ScanResultSetChannel !!");
        if(ViewHistory.getCurChannelList() == null || ViewHistory.getCurChannelList().size() == 0) {
            if(Pvcfg.IsEnableNetworkPrograms()) {
                ProgramInfoPlaySimpleChannelListUpdate(this,1);//Scoty 20181109 modify for skip channel//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
                //////////////
                AddNetProgramToTotalChannelList(ProgramInfo.ALL_TV_TYPE);
            }else{
                ProgramInfoPlaySimpleChannelListUpdate( 1);
            }
            simpleChannelList = ProgramInfoGetPlaySimpleChannelList(ProgramInfo.ALL_TV_TYPE, 0);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
            if(simpleChannelList == null) {
                groupType = ProgramInfo.ALL_RADIO_TYPE;
                simpleChannelList = ProgramInfoGetPlaySimpleChannelList(ProgramInfo.ALL_RADIO_TYPE, 0);//Scoty 20180613 change get simplechannel list for PvrSkip rule
            }
            if(simpleChannelList != null && !simpleChannelList.isEmpty()) {
                simpleChannel = simpleChannelList.get(0);
                ViewHistory.SetCurChannel(simpleChannel, simpleChannelList, groupType);
                setTempPesiDefaultChannelFlag(0);
                return 1;
            }
            else
                return 0;
        }
        else {
            if(Pvcfg.IsEnableNetworkPrograms()) {
                ProgramInfoPlaySimpleChannelListUpdate(this, 1);//Scoty 20181109 modify for skip channel//Scoty 20180613 change get simplechannel list for PvrSkip rule
                AddNetProgramToTotalChannelList(ProgramInfo.ALL_TV_TYPE);
            }else {
                ProgramInfoPlaySimpleChannelListUpdate(1);//Scoty 20181109 modify for skip channel//Scoty 20180613 change get simplechannel list for PvrSkip rule
            }
            simpleChannelList = ProgramInfoGetPlaySimpleChannelList(ProgramInfo.ALL_TV_TYPE, 0);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
            if (simpleChannelList == null) {
                groupType = ProgramInfo.ALL_RADIO_TYPE;
                simpleChannelList = ProgramInfoGetPlaySimpleChannelList(ProgramInfo.ALL_RADIO_TYPE, 0);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
            }
            for (int i = 0; i < simpleChannelList.size(); i++) {
                if (ViewHistory.getCurChannel().getChannelId() == simpleChannelList.get(i).getChannelId()) {
                    ViewHistory.SetCurChannel(simpleChannelList.get(i), simpleChannelList, groupType);
                }
            }
            return 1;
        }
    }

    public GposInfo GposInfoGet() {
        return super.GposInfoGet();
    }

    public void GposInfoUpdate(GposInfo gPos) {
        super.GposInfoUpdate(gPos);
    }

    public int AvControlPlayByChannelId(int playId,long channelId, int groupType, int show) {

        SimpleChannel channel = null;
        List<SimpleChannel> channelList = null;
        String DTV_SET_LAUNCHER_PLAYCHANNEL = "com.prime.launcher.playchannel";
        String DTV_LAUNCHER_PACKAGE_NAME = "com.pesilauncher.pesilauncher";
        GposInfo gpos = GposInfoGet();
        long fastPreChId = 0, fastNextChId = 0;//Scoty 20180816 add fast change channel
        Log.d(TAG, "AvControlPlayByChannelId: channelId == "
                + channelId + "groupType == " + groupType + " his group == " + ViewHistory.getCurGroupType());

        if(ViewHistory.getCurChannel() == null || channelId != ViewHistory.getCurChannel().getChannelId()) {
            channel = GetSimpleProgramByChannelIdfromTotalChannelListByGroup(groupType, channelId);
        }
        if(groupType != ViewHistory.getCurGroupType())
            channelList = ProgramInfoGetPlaySimpleChannelList(groupType,0);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        ViewHistory.SetCurChannel(channel, channelList, groupType);
        Intent PesiLauncherIntent = new Intent(DTV_SET_LAUNCHER_PLAYCHANNEL);
        PesiLauncherIntent.putExtra("channelnum",ViewHistory.getCurChannel().getChannelNum());
        PesiLauncherIntent.setPackage(DTV_LAUNCHER_PACKAGE_NAME);// jim 2019/09/04 fix send update EPG broadcast to launcher failed
        sendBroadcast(PesiLauncherIntent);
        if( Pvcfg.getCAType() == Pvcfg.CA_WIDEVINE_CAS) {//eric lin 20210107 widevine cas
            if (mHandler != null) {
                if (casSessionManager != null && ((casSessionArray[0] != null) || (casSessionArray[1] != null))) {
                Log.d("WidevineCasSessionMgr", "DTVA AvControlPlayByChannelId() casSessionManager!= null && ((casSessionArray[0] != null ) || (casSessionArray[1] != null ))=> call disable_widevineCas()");
                disable_widevineCas();
            }
            Log.d("WidevineCasSessionMgr", "DTVA AvControlPlayByChannelId() sendMessage(WV_TEST)");
            sendMessage(mHandler, WV_TEST, 0, 0);
        }
        }
        AvControlPlayStop(playId);
        fastPreChId = ViewHistory.getFastPreChId();//Scoty 20180816 add fast change channel
        fastNextChId = ViewHistory.getFastNextChId();//Scoty 20180816 add fast change channel
        gpos.setCurGroupType(groupType);
        GposInfoUpdate(gpos);
        playingChannelId = channelId; // Johnny 20210423 temp for RTK avplay issue
        super.AvControlPlayByChannelId(playId,channelId,groupType,show);
        return AvControlSetFastChangeChannel(fastPreChId, fastNextChId);//if not fast zapping then should remark//Scoty 20180816 add fast change channel
    }

    public int AvControlPlayUp(int playId) {
        ViewHistory.setChannelUp();
        return AvControlPlayByChannelId(playId,ViewHistory.getCurChannel().getChannelId(),ViewHistory.getCurGroupType(),1);
    }

    public int AvControlPlayDown(int playId) {
        ViewHistory.setChannelDown();
        return AvControlPlayByChannelId(playId,ViewHistory.getCurChannel().getChannelId(),ViewHistory.getCurGroupType(),1);

    }

    public int AvControlPrePlayStop() {
        return super.AvControlPrePlayStop();
    }

    public int AvControlPlayStop(int playId) {
        return super.AvControlPlayStop(playId);
    }

    public int AvControlChangeRatioConversion(int playId,int ratio,int conversion) {
        return super.AvControlChangeRatioConversion(playId, ratio, conversion);
    }
    public int AvControlSetFastChangeChannel(long PreChannelId, long NextChannelId)//Scoty 20180816 add fast change channel
    {
        return super.AvControlSetFastChangeChannel(PreChannelId,NextChannelId);
    }
/*
    public int AvControlChangeRatio(int playId,int ratio) {
        return super.AvControlChangeRatio(playId,ratio);
    }

    public int AvControlChangeConversion(int playId,int conversion) {
        return super.AvControlChangeConversion(playId,conversion);
    }
*/
    public int AvControlChangeResolution(int playId,int resolution) {//??????
        return super.AvControlChangeResolution(playId,resolution);
    }

    public int AvControlChangeAudio(int playId,AudioInfo.AudioComponent component) {
        return super.AvControlChangeAudio(playId,component);
    }

    public int AvControlSetVolume(int volume) {//??????
        return super.AvControlSetVolume(volume);
    }

    public int AvControlGetVolume() {//??????
        return super.AvControlGetVolume();
    }

    public int AvControlSetMute(int playId,boolean mute) {//??????
        return super.AvControlSetMute(playId,mute);
    }

    public int AvControlSetTrackMode(int playId,EnAudioTrackMode mode) {
        return super.AvControlSetTrackMode(playId,mode);
    }

    public int AvControlAudioOutput(int playId,int byPass) {
        return super.AvControlAudioOutput(playId,byPass);
    }

    public int AvControlClose(int playId) {
        return super.AvControlClose(playId);
    }

    public int AvControlOpen(int playId) {
        return super.AvControlOpen(playId);
    }

    public int AvControlShowVideo(int playId,boolean show) {
        return super.AvControlShowVideo(playId,show);
    }

    public int AvControlFreezeVideo(int playId,boolean freeze) {
        return super.AvControlFreezeVideo(playId,freeze);
    }

    //return value shoulde be check
    public AudioInfo AvControlGetAudioListInfo(int playId) {
        return super.AvControlGetAudioListInfo(playId);
    }

    /* return status =  LIVEPLAY,TIMESHIFTPLAY.....etc  */
    public int AvControlGetPlayStatus(int playId) {
        return super.AvControlGetPlayStatus(playId);
    }

    public boolean AvControlGetMute(int playId) {
        return super.AvControlGetMute(playId);
    }

    public EnAudioTrackMode AvControlGetTrackMode(int playId) {
        return super.AvControlGetTrackMode(playId);
    }

    public int AvControlGetRatio(int playId){
        return super.AvControlGetRatio(playId);
    }

    public int AvControlSetStopScreen(int playId,int stopType) {//?????????????????????
        return super.AvControlSetStopScreen(playId,stopType);
    }

    public int AvControlGetStopScreen(int playId) {
        return super.AvControlGetStopScreen(playId);
    }

    public int AvControlGetFPS(int playId) {
        return super.AvControlGetFPS(playId);
    }

    public int AvControlEwsActionControl(int playId, boolean enable) {
        return super.AvControlEwsActionControl(playId, enable);
    }

    public int AvControlSetWindowSize(int playId, Rect rect) {
        return super.AvControlSetWindowSize(playId,rect);
    }

    public Rect AvControlGetWindowSize(int playId) {
        return super.AvControlGetWindowSize(playId);
    }

    public int AvControlGetVideoResolutionHeight(int playId) {
        return super.AvControlGetVideoResolutionHeight(playId);
    }

    public int AvControlGetVideoResolutionWidth(int playId) {
        return super.AvControlGetVideoResolutionWidth(playId);
    }

    /* 0: dolby digital, 1: dolby digital plus */
    public int AvControlGetDolbyInfoStreamType(int playId) {
        return super.AvControlGetDolbyInfoStreamType(playId);
    }

    public int AvControlGetDolbyInfoAcmod(int playId) {
        return super.AvControlGetDolbyInfoAcmod(playId);
    }

    public SubtitleInfo.SubtitleComponent AvControlGetCurrentSubtitle(int playId) {
        return super.AvControlGetCurrentSubtitle(playId);
    }

    public SubtitleInfo AvControlGetSubtitleList(int playId) {
        return super.AvControlGetSubtitleList(playId);
    }

    public int AvControlSelectSubtitle(int playId,SubtitleInfo.SubtitleComponent subtitleComponent) {
        return super.AvControlSelectSubtitle(playId,subtitleComponent);
    }

    public int AvControlShowSubtitle(int playId,boolean enable) {
        return super.AvControlShowSubtitle(playId,enable);
    }

    public boolean AvControlIsSubtitleVisible(int playId) {
        return super.AvControlIsSubtitleVisible(playId);
    }

    public int AvControlSetSubtitleLanguage(int playId,int index, String lang) {
        return super.AvControlSetSubtitleLanguage(playId, index, lang);
    }

    public int AvControlSetSubtHiStatus(int playId,boolean HiOn) {//?????
        return super.AvControlSetSubtHiStatus(playId,HiOn);
    }

    public TeletextInfo AvControlGetCurrentTeletext(int playId) {
        return super.AvControlGetCurrentTeletext(playId);
    }

    public List<TeletextInfo> AvControlGetTeletextList(int playId) {
        return super.AvControlGetTeletextList(playId);
    }

    public int AvControlShowTeletext(int playId,boolean enable) {
        return super.AvControlShowTeletext(playId,enable);
    }

    public boolean AvControlIsTeletextVisible(int playId) {
        return super.AvControlIsTeletextVisible(playId);
    }

    public boolean AvControlIsTeletextAvailable(int playId) {
        return super.AvControlIsTeletextAvailable(playId);
    }

    public int AvControlSetTeletextLanguage(int playId,String primeLang) {
        return super.AvControlSetTeletextLanguage(playId,primeLang);
    }

    public String AvControlGetTeletextLanguage(int playId) {//eric lin 20180705 get ttx lang
        return super.AvControlGetTeletextLanguage(playId);
    }

    public int AvControlSetTeletextCommand(int playId,int keyCode) {
        return super.AvControlSetCommand(playId,keyCode);
    }

    public Date AvControlGetTimeShiftBeginTime(int playId) {
        return super.AvControlGetTimeShiftBeginTime(playId);
    }

    public Date AvControlGetTimeShiftPlayTime(int playId) {
        return super.AvControlGetTimeShiftPlayTime(playId);
    }

    public int AvControlGetTimeShiftRecordTime(int playId) {
        return super.AvControlGetTimeShiftRecordTime(playId);
    }

    public int AvControlGetTrickMode(int playId) {
        return super.AvControlGetTrickMode(playId);
    }

    public int AvControlTimeshiftTrickPlay(int playId,int trickMode) {
        return super.AvControlTimeshiftTrickPlay(playId,trickMode);
    }

    public int AvControlTimeshiftPausePlay(int playId) {
        return super.AvControlTimeshiftPausePlay(playId);
    }

    public int AvControlTimeshiftPlay(int playId) {
        return super.AvControlTimeshiftPlay(playId);
    }

    public int AvControlTimeshiftSeekPlay(int playId,long seekTime) {
        return super.AvControlTimeshiftSeekPlay(playId,seekTime);
    }

    public int AvControlStopTimeShift(int playId) {
        return super.AvControlStopTimeShift(playId);
    }

    public int UpdateUsbSoftWare(String filename){
        return super.UpdateUsbSoftWare(filename);
    }

    public int UpdateFileSystemSoftWare(String pathAndFileName , String partitionName){
        return super.UpdateFileSystemSoftWare(pathAndFileName,partitionName);
    }

    public int UpdateOTADVBCSoftWare(int tpId, int freq, int symbol, int qam){
        return super.UpdateOTADVBCSoftWare(tpId,freq,symbol,qam);
    }
    public int UpdateOTADVBTSoftWare(int tpId, int freq, int bandwith, int qam, int priority){
        return super.UpdateOTADVBTSoftWare(tpId,freq,bandwith,qam,priority);
    }
    public int UpdateOTADVBT2SoftWare(int tpId, int freq, int symbol, int qam, int channelmode){
        return super.UpdateOTADVBT2SoftWare(tpId,freq,symbol,qam,channelmode);
    }

    public int UpdateOTAISDBTSoftWare(int tpId, int freq, int symbol, int qam, int channelmode){
        return super.UpdateOTAISDBTSoftWare(tpId,freq,symbol,qam,channelmode);
    }

    public int UpdateMtestOTASoftWare()//Scoty 20190410 add Mtest Trigger OTA command
    {
        return super.UpdateMtestOTASoftWare();
    }

    public boolean TunerIsLock(int tunerId) {
//        Log.i(TAG, "TunerIsLock: ");
        return super.TunerGetLockStatus(tunerId) == 1;
    }

    public int TunerGetLockStatus(int tunerId) {
//        Log.i(TAG, "TunerGetLockStatus: ");
        return super.TunerGetLockStatus(tunerId);
    }

    public int TunerGetStrength(int tunerId) {
//        Log.i(TAG, "TunerGetStrenth: ");
        return super.TunerGetStrength(tunerId);
    }

    public int TunerGetQuality(int tunerId) {
//        Log.i(TAG, "GetTunerQuality: ");
        return super.TunerGetQuality(tunerId);
    }

    public String TunerGetBER(int tunerId){
//        Log.d(TAG, "TunerGetBER");
        return super.TunerGetBER(tunerId);
    }

    public int TunerGetSNR(int tunerId){
        Log.d(TAG, "TunerGetSNR");
        return super.TunerGetSNR(tunerId);
    }

    public int setFakeTuner(int openFlag)//Scoty 20180809 add fake tuner command
    {
        return super.setFakeTuner(openFlag);
    }

    public void TunerTuneDVBT(int tunerId, int tpId, int frequency, int bandwith) {
        List<TpInfo> tpList = new ArrayList<>(); //eric lin 20171227 check TunerTune param
        TVTunerParams Tune;
        int sat_id;
        Log.i(TAG, "TunerTuneDVBT: " + "tuner_id = " + tunerId + " tpId = " + tpId + " frequency = " + frequency
                + " bandwith = " + bandwith);
        sat_id = TpInfoGet(tpId).getSatId();
        Tune = TVTunerParams.CreateTunerParamDVBT(tunerId, sat_id, tpId, frequency, bandwith);
        TuneFrontEnd(Tune);
    }

    public void TunerTuneISDBT(int tunerId, int tpId, int frequency, int bandwith) {
        List<TpInfo> tpList = new ArrayList<>(); //eric lin 20171227 check TunerTune param
        TVTunerParams Tune;
        int sat_id;
        Log.i(TAG, "TunerTuneISDBT: " + "tuner_id = " + tunerId + " tpId = " + tpId + " frequency = " + frequency
                + " bandwith = " + bandwith);
        sat_id = TpInfoGet(tpId).getSatId();
        Tune = TVTunerParams.CreateTunerParamISDBT(tunerId, sat_id, tpId, frequency, bandwith);
        TuneFrontEnd(Tune);
    }

    public void TunerTuneDVBS(int tunerId, int tpId, int frequency, int symbolRate, int polar) {
        List<TpInfo> tpList = new ArrayList<>(); //eric lin 20171227 check TunerTune param
        TVTunerParams Tune;
        int sat_id;
        Log.i(TAG, "TunerTuneDVBS: " + "tuner_id = " + tunerId + " tpId = " + tpId + " frequency = " + frequency
                + " symbolRate = " + symbolRate + " polar = " + polar);
        sat_id = TpInfoGet(tpId).getSatId();
        Tune = TVTunerParams.CreateTunerParamDVBS(tunerId, sat_id, tpId, frequency, symbolRate, polar);
        TuneFrontEnd(Tune);
    }

    public void TunerTuneDVBC(int tunerId, int tpId, int frequency, int symbolRate, int Qam) {
        List<TpInfo> tpList = new ArrayList<>(); //eric lin 20171227 check TunerTune param
        TVTunerParams Tune;
        int sat_id;
        Log.i(TAG, "TunerTuneDVBC: " + "tuner_id = " + tunerId + " tpId = " + tpId + " frequency = " + frequency
                + " symbolRate = " + symbolRate + " Qam = " + Qam);
        sat_id = TpInfoGet(tpId).getSatId();
        Tune = TVTunerParams.CreateTunerParamDVBC(tunerId, sat_id, tpId, frequency, symbolRate, Qam);
        TuneFrontEnd(Tune);
    }

    public int TunerSetAntenna5V(int tunerId,int onOff){
        Log.d(TAG, "TunerSetAntenna5V");
        return super.TunerSetAntenna5V(tunerId,onOff);
    }

    public void TunerTuneByExistTp(int tunerId,int tpId) {
        TpInfo Tp = TpInfoGet(tpId);
        if(Tp == null)//eric lin 20171227 check TunerTune param
            return;
        if(Tp.getTunerType() == TpInfo.DVBT) {
            TunerTuneDVBT(tunerId, tpId, Tp.TerrTp.getFreq(), Tp.TerrTp.getBand());
        }
        else if (Tp.getTunerType() == TpInfo.ISDBT) {
            TunerTuneISDBT(tunerId, tpId, Tp.TerrTp.getFreq(), Tp.TerrTp.getBand());
        }
        else if(Tp.getTunerType() == TpInfo.DVBS) {
            TunerTuneDVBS(tunerId, tpId, Tp.SatTp.getFreq(), Tp.SatTp.getSymbol(), Tp.SatTp.getPolar());
        }
        else if(Tp.getTunerType() == TpInfo.DVBC) {
            TunerTuneDVBC(tunerId, tpId, Tp.CableTp.getFreq(), Tp.CableTp.getSymbol(), Tp.CableTp.getQam());
        }
    }

    public void ScanParamsStartScan(int tunerId, int tpId,int satId,int scanMode, int searchOptionTVRadio, int searchOptionCaFta,int nitSearch,int oneSegment) {
        TpInfo tpInfo = TpInfoGet(tpId);
        Log.d(TAG,"ScanParamsStartScan !!! ");
//        Log.d(TAG,"Scan tp: freq["+tpInfo.CableTp.getFreq()+"] sym["+tpInfo.CableTp.getSymbol()+"] qam["+tpInfo.CableTp.getQam()+"]");
        TVScanParams sp = new TVScanParams(tunerId,tpInfo,satId,scanMode,searchOptionTVRadio,searchOptionCaFta,nitSearch,oneSegment);
        super.startScan(sp);
    }

    // for VMX need open/close -s
    public void ScanParamsVMXStartScan(int tunerId, int tpId,int satId,int scanMode, int searchOptionTVRadio, int searchOptionCaFta,int nitSearch,int oneSegment, int startTPID, int endTPID ) {
        Log.d(TAG, "ScanParamsVMXStartScan:  startTPID = " + startTPID + "    endTPID = "+ endTPID);
        TpInfo tpInfo = TpInfoGet(tpId);
        TVScanParams sp = new TVScanParams(tunerId,tpInfo,satId,scanMode,searchOptionTVRadio,searchOptionCaFta,nitSearch,oneSegment);
        super.VMXstartScan(sp, startTPID, endTPID);
    }// for VMX need open/close -e

    public void ScanParamsStopScan(boolean store) {
        super.stopScan(store);
    }

    private boolean isAllTvRadio(int type) {
        if(type == ProgramInfo.ALL_TV_TYPE || type == ProgramInfo.ALL_RADIO_TYPE)
            return true;
        else
            return false;
    }

    private int GetAllTvRadioType(int type) {
        if(isAllTvRadio(type))
            return type;
        else if(type >= ProgramInfo.TV_FAV1_TYPE && type < ProgramInfo.ALL_RADIO_TYPE)
            return ProgramInfo.ALL_TV_TYPE;
        else if(type >= ProgramInfo.RADIO_FAV1_TYPE && type < ProgramInfo.ALL_TV_RADIO_TYPE_MAX)
            return ProgramInfo.ALL_RADIO_TYPE;
        else
            return ProgramInfo.ALL_TV_RADIO_TYPE_MAX;
    }

    private boolean isSameProgramInfo(ProgramInfo program1,ProgramInfo program2) {
        if(program1.getServiceId() == program2.getServiceId() && program1.getTransportStreamId() == program2.getTransportStreamId()
                && program1.getOriginalNetworkId() == program2.getOriginalNetworkId())
            return true;
        else
            return false;
    }

    public List<SimpleChannel> ProgramInfoGetPlaySimpleChannelList(int type, int IncludePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        return super.ProgramInfoGetPlaySimpleChannelList(type,IncludePVRSkipFlag);
    }

    public int ProgramInfoGetPlaySimpleChannelListCnt(int type) {//eric lin 20180802 check program exist
        return super.ProgramInfoGetPlaySimpleChannelListCnt(type);
    }

    public List<SimpleChannel> ProgramInfoGetSimpleChannelList(int type,int IncludeSkipFlag,int IncludePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        return super.ProgramInfoGetSimpleChannelList(type,IncludeSkipFlag,IncludePVRSkipFlag);
    }

    public List<ProgramInfo> ProgramInfoGetList(int type) {
        return super.ProgramInfoGetList(GetAllTvRadioType(type),MiscDefine.ProgramInfo.POS_ALL,MiscDefine.ProgramInfo.NUM_ALL);
    }

    public ProgramInfo ProgramInfoGetByLcn(int lcn, int type) {
        return super.ProgramInfoGetByLcn(lcn, GetAllTvRadioType(type));
    }

    public ProgramInfo ProgramInfoGetByChannelId(long channelId) {
        return super.ProgramInfoGetByChannelId(channelId);
    }

    public SimpleChannel ProgramInfoGetSimpleChannelByChannelId(long channelId) {
        return super.ProgramInfoGetSimpleChannelByChannelId(channelId);
    }

    public SimpleChannel GetSimpleProgramByChannelIdfromTotalChannelList(long channelId){
        return super.GetSimpleProgramByChannelIdfromTotalChannelList(channelId);
    }

    public SimpleChannel GetSimpleProgramByChannelIdfromTotalChannelListByGroup(int groupType, long channelId)
    {
        return super.GetSimpleProgramByChannelIdfromTotalChannelListByGroup(groupType,channelId);
    }
    public SimpleChannel ProgramInfoGetSimpleChannelByChNum(int chNum, int type) {
        List<SimpleChannel> simpleChannelList = ProgramInfoGetPlaySimpleChannelList(type, 0);
        if (simpleChannelList == null)
        {
            return null;
        }

        SimpleChannel simpleChannel = null;
        for(int i = 0 ; i < simpleChannelList.size() ; i++)
        {
            SimpleChannel tmpSimpleChannel = simpleChannelList.get(i);
            if (chNum == tmpSimpleChannel.getChannelNum())
            {
                simpleChannel = tmpSimpleChannel;
                break;
            }
        }

        return simpleChannel;
    }

    public ProgramInfo ProgramInfoGetByChnum(int chnum,int type) { //???fav???program, Chnum = fav index
        Log.i(TAG, "ProgramInfoGet: chnum="+chnum+", type="+type);
        if(type >= ProgramInfo.ALL_TV_RADIO_TYPE_MAX)
            return null;
        if(isAllTvRadio(type))
            return super.ProgramInfoGetByChnum(chnum,type);
        else {
            FavInfo favInfo = FavInfoGet(type,chnum);
            if(favInfo != null)//eric lin fix crash
                return ProgramInfoGetByChannelId(favInfo.getChannelId());
            else
                return null;
        }
    }

    public int ProgramInfoUpdate(ProgramInfo program) {
        return super.ProgramInfoUpdate(program);
    }

    public int ProgramInfoUpdateList(List<ProgramInfo> programList) {
        return super.ProgramInfoUpdateList(programList);
    }

    public int ProgramInfoUpdateList(List<SimpleChannel> programList,int type) {
        if(programList != null)//eric lin 20180122 fix ProgramInfoUpdateList(List<SimpleChannel> programList,int type) null param crash
            return super.ProgramInfoUpdateList(programList,type);
        else
            return -1;
    }

    public List<SimpleChannel> ProgramInfoGetListByFilter (int filterTag, int serviceType, String keyword, int IncludeSkip, int IncludePvrSkip)//Scoty 20181109 modify for skip channel
    {
        return super.ProgramInfoGetListByFilter(filterTag, serviceType, keyword, IncludeSkip, IncludePvrSkip);//Scoty 20181109 modify for skip channel
    }

    public int ProgramInfoDeleteNotReSortChannelId(long channelId) {
        return super.ProgramInfoDelete(channelId);
    }

    public String GroupNameGet(int GroupType) {
        return super.FavGroupNameGet(GroupType);
    }

    public void GroupNameUpdate(int GroupType, String name) {
        super.FavGroupNameUpdate(GroupType,name);
    }
/*
    public SimpleChannel FavInfoGetProgram(int GroupType,int position) {
        SimpleChannel Channel = null;
        FavInfo favInfo = super.FavInfoGet(GroupType,position);
        if(favInfo != null) {//eric lin fix crash
            //Channel = ProgramInfoGetSimpleChannelByChannelId(favInfo.getChannelId());
            channel = GetSimpleProgramByChannelIdfromTotalChannelListByGroup(ViewHistory.getCurGroupType(), favInfo.getChannelId());
        return Channel;
        }else
            return null;
    }

    private List<SimpleChannel> FavInfoGetSimpleChannelList(int GroupType) {
        List<FavInfo> FavList = super.FavInfoGetList(GroupType);
        List<SimpleChannel> ChannelList = null;
        if(FavList != null) {
            ChannelList = new ArrayList<>();    // Johnny modify 20180119
            for(int i = 0; i < FavList.size(); i++) {
                //SimpleChannel simpleChannel = ProgramInfoGetSimpleChannelByChannelId(FavList.get(i).getChannelId());
                SimpleChannel simpleChannel = GetSimpleProgramByChannelIdfromTotalChannelListByGroup(ViewHistory.getCurGroupType(), favInfo.getChannelId());
                simpleChannel.setChannelNum(FavList.get(i).getFavNum());
                ChannelList.add(simpleChannel);
            }
        }
        return ChannelList;
    }

    public void FavInfoSave(SimpleChannel channel, int GroupType, int position) {
        FavInfo FavInfo = new FavInfo();
        FavInfo.setFavNum(position);
        FavInfo.setChannelId(channel.getChannelId());
        FavInfo.setFavMode(GroupType);
        super.FavInfoSave(FavInfo);
    }

    public void FavInfoSaveList(List<SimpleChannel> channelList, int GroupType) {
        List<FavInfo> FavList = new ArrayList<FavInfo>();
        for(int i = 0; i < channelList.size(); i++) {
            FavInfo fav = new FavInfo();
            fav.setFavNum(i+1);
            fav.setChannelId(channelList.get(i).getChannelId());
            fav.setFavMode(GroupType);
            FavList.add(fav);
        }
        super.FavInfoSaveList(FavList);
    }

    public int FavInfoSaveList(List<FavInfo> favList) {
        return super.FavInfoSaveList(favList);
    }

    public int FavInfoDelete(int GroupType, long channelId) {
        return super.FavInfoDelete(GroupType,channelId);
    }
*/

    public List<FavInfo> FavInfoGetList(int favMode) {
        return super.FavInfoGetList(favMode);
    }

    public FavInfo FavInfoGet(int favMode,int index) {
        return super.FavInfoGet(favMode,index);
    }

    public int FavInfoUpdate(FavInfo favInfo) {
        return super.FavInfoUpdate(favInfo);
    }

    public int FavInfoUpdateList(List<FavInfo> favInfo) {
        return super.FavInfoUpdateList(favInfo);
    }

    public int FavInfoDelete(int favMode, long channelId) {
        return super.FavInfoDelete(favMode,channelId);
    }

    public int FavInfoDeleteAll(int GroupType) {
        return super.FavInfoDeleteAll(GroupType);
    }

    public OkListManagerImpl newOkListManagerImpl(int group,List<SimpleChannel> simpleChannelList) {
        return new OkListManagerImpl(group,simpleChannelList);
    }
    public class OkListManagerImpl {
        private int CurGroupType;
        public List<SimpleChannel> ProgramInfoList = new ArrayList<>();

        public OkListManagerImpl(int GroupType) {
            CurGroupType = GroupType;
            ProgramInfoList = ProgramInfoGetPlaySimpleChannelList(GroupType,0);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        }

        public OkListManagerImpl(int GroupType,List<SimpleChannel> ProgramFilterList) {
            CurGroupType = GroupType;
            ProgramInfoList = ProgramFilterList;
        }

        public int getGroupType() {
            return CurGroupType;
        }

        public void ChangeProgram(int position) {
            final int pos = position;
            if(ViewHistory.getCurChannel() != null) {
                //Scoty 20180613 fixed same channel but different group not change group -s
                if (ViewHistory.getCurChannel().getChannelId() != ProgramInfoList.get(pos).getChannelId()) {
                    AvControlPlayByChannelId(ViewHistory.getPlayId(), ProgramInfoList.get(pos).getChannelId(), CurGroupType,1);
                }
                else
                {
                    //Scoty 20180620 modify same channel change program should check av status -s
                    int AVplayStatus = AvControlGetPlayStatus(ViewHistory.getPlayId());
                    if(AVplayStatus == HiDtvMediaPlayer.EnPlayStatus.STOP.getValue())
                        AvControlPlayByChannelId(ViewHistory.getPlayId(), ProgramInfoList.get(pos).getChannelId(), CurGroupType, 1);
                    else
                    {
                        GposInfo gpos = GposInfoGet();
                        SimpleChannel channel = GetSimpleProgramByChannelIdfromTotalChannelListByGroup(ViewHistory.getCurGroupType(), ViewHistory.getCurChannel().getChannelId());
                        List<SimpleChannel> channelList = ProgramInfoGetPlaySimpleChannelList(CurGroupType, 0);//Scoty 20180615 recover get simple channel list function
                        ViewHistory.SetCurChannel(channel, channelList, CurGroupType);
                        gpos.setCurGroupType(CurGroupType);
                        GposInfoUpdate(gpos);
                    }
                    //Scoty 20180620 modify same channel change program should check av status -e
                }
                //Scoty 20180613 fixed same channel but different group not change group -e
            }
        }
        //Scoty 20180712 for pip
        public void ChangePipProgram(int position)
        {
            final int pos = position;
            if (ViewHistory.getCurPipChannel().getChannelId() != ProgramInfoList.get(pos).getChannelId()) {
                ViewHistory.setCurPipIndex(pos);
                PipStart(ProgramInfoList.get(pos).getChannelId(),1);
            }
        }
    }

    public List<OkListManagerImpl> GetOkList()
    {
        List<OkListManagerImpl> okList = new ArrayList<OkListManagerImpl>();
        for(int i = 0; i < GetAllProgramGroup().size(); i++) {
            OkListManagerImpl okListManager = new OkListManagerImpl(GetAllProgramGroup().get(i).getGroupType());
            okList.add(okListManager);
        }
        return okList;
    }

//    public void OkListInit() {
//        OkListManagerList.clear();
//        for(int i = 0; i < GetAllProgramGroup().size(); i++) {
//            OkListManagerImpl okListManager = new OkListManagerImpl(GetAllProgramGroup().get(i).getGroupType());
//            OkListManagerList.add(okListManager);
//        }
//    }

    public class ProgramManagerImpl {
        private int curGroupType;
        public List<ProgramManagerInfo> ProgramManagerInfoList = new ArrayList<ProgramManagerInfo>();

        public int getCurGroupType() {
            return curGroupType;
        }

        public class ProgramManagerInfo extends SimpleChannel {
            private int FavIcon;
            private int Delete;
            private int MoveIcon;
            private int OriDisplayNum;

            public ProgramManagerInfo(SimpleChannel programInfo) {
                setChannelId(programInfo.getChannelId());
                setType(programInfo.getType());
                setChannelNum(programInfo.getChannelNum());
                setChannelName(programInfo.getChannelName());
                setUserLock(programInfo.getUserLock());
                setCA(programInfo.getCA());
                setOriDisplayNum(programInfo.getChannelNum());
                setFavIcon(0);
                setDelete(0);
                setMoveIcon(0);
                setChannelSkip(programInfo.getChannelSkip());//Scoty 20181109 modify for skip channel
                setPlayStreamType(programInfo.getPlayStreamType());//Scoty Add Youtube/Vod Stream
                if(programInfo.getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE || programInfo.getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.YOUTUBE_TYPE) {
                    setPresentepgEvent(programInfo.getPresentepgEvent());
                    setFollowepgEvent(programInfo.getFollowepgEvent());
                    setShortEvent(programInfo.getShortEvent());
                    setDetailInfo(programInfo.getDetailInfo());
                }
            }

            SimpleChannel TransferToProgramInfo() {
                SimpleChannel programInfo = new SimpleChannel();
                programInfo.setType(this.getType());
                programInfo.setChannelId(this.getChannelId());
                programInfo.setChannelNum(this.getChannelNum());
                programInfo.setChannelName(this.getChannelName());
                programInfo.setUserLock(this.getUserLock());
                programInfo.setCA(this.getCA());
                programInfo.setChannelSkip(this.getChannelSkip());//Scoty 20181109 modify for skip channel
                programInfo.setPlayStreamType(this.getPlayStreamType());//Scoty Add Youtube/Vod Stream
                if(this.getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE || this.getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.YOUTUBE_TYPE)
                {
                    programInfo.setPresentepgEvent(this.getPresentepgEvent());
                    programInfo.setFollowepgEvent(this.getFollowepgEvent());
                    programInfo.setShortEvent(this.getShortEvent());
                    programInfo.setDetailInfo(this.getDetailInfo());
                }
                return programInfo;
            }

            public int getFavIcon() {
                return FavIcon;
            }

            public void setFavIcon(int favIcon) {
                FavIcon = favIcon;
            }

            public int getDelete() {
                return Delete;
            }

            public void setDelete(int delete) {
                Delete = delete;
            }

            public int getMoveIcon() {
                return MoveIcon;
            }

            public void setMoveIcon(int moveIcon) {
                MoveIcon = moveIcon;
            }

            public int getOriDisplayNum() {
                return OriDisplayNum;
            }

            public void setOriDisplayNum(int oriDisplayNum) {
                OriDisplayNum = oriDisplayNum;
            }
        }

        public ProgramManagerImpl(int GroupType) {
            curGroupType = GroupType;
            List<SimpleChannel> programInfoList = null;
            programInfoList = ProgramInfoGetSimpleChannelListfromTotalChannel(GroupType,1,1);
                //programInfoList = ProgramInfoGetSimpleChannelList(GroupType,1,1);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
            //ProgramInfoGetPlaySimpleChannelList(GroupType,1); //
            if(ProgramManagerInfoList.size() != 0)
                ProgramManagerInfoList.clear();
            if(programInfoList != null) {
                for(int i = 0; i < programInfoList.size(); i++) {
                    ProgramManagerInfo pProgramManagerInfo = new ProgramManagerInfo(programInfoList.get(i));
                    ProgramManagerInfoList.add(pProgramManagerInfo);
                }
            }
        }

        /* not use */
        public void ResetProgramListChnum(List<ProgramManagerInfo> list) {
            Log.i(TAG, "ResetProgramListChnum: ");
            //for(int i=0;i<list.size();i++){
            //    list.get(i).setChannelNum(i+1);
            //}
        }

        public void MoveProgram(List<ProgramManagerImpl> ProgramManagerList, int cur, int dest){
            Log.i(TAG,"MoveProgram: cur_pos="+cur+"-----"+"dest_pos="+dest);
            ProgramManagerInfo mTemp;
            int size=0;//eric lin 20171225 check move program's cur/dest

            size = ProgramManagerList.get(0).ProgramManagerInfoList.size();//eric lin 20171225 check move program's cur/dest
            if(size == 0 || !(cur >=0 && cur<size) || !(dest >=0 && dest<size)) {//eric lin 20171225 check move program's cur/dest
                return;
            }

            mTemp = ProgramManagerInfoList.get(cur);
            ProgramManagerInfoList.remove(cur);
            ProgramManagerInfoList.add(dest,mTemp);
        }

        public void DelProgram(List<ProgramManagerImpl> ProgramManagerList, int index,int del) {
            Log.i(TAG, "DelProgram: index="+index);
            int size=0;//eric lin 20171225 check del program's index

            if(curGroupType >= ProgramInfo.ALL_TV_RADIO_TYPE_MAX)
                return;

            size = ProgramManagerList.get(0).ProgramManagerInfoList.size();//eric lin 20171225 check del program's index
            if(size == 0 || !(index >=0 && index<size) ) {//eric lin 20171225 check del program's index
                return;
            }
            if(isAllTvRadio(curGroupType)) {
                ProgramManagerInfoList.get(index).setDelete(del);
                for(int i = 1; i < ProgramManagerList.size(); i++) {
                    for(int j = 0; j < ProgramManagerList.get(i).ProgramManagerInfoList.size(); j++) {
                        if (ProgramManagerInfoList.get(index).getChannelId() ==
                                ProgramManagerList.get(i).ProgramManagerInfoList.get(j).getChannelId()) {
                            ProgramManagerList.get(i).ProgramManagerInfoList.get(j).setDelete(del);
                        }
                    }
                }
            }
            else {
                int favIocn = 0;
                for(int i = 1; i < ProgramManagerList.size(); i++) {
                    int favModeIndex = 0;
                    if(curGroupType > ProgramInfo.ALL_RADIO_TYPE)
                        favModeIndex = curGroupType - ProgramInfo.ALL_RADIO_TYPE;
                    else
                        favModeIndex = curGroupType - ProgramInfo.ALL_TV_TYPE;
                    Log.d(TAG,"favModeIndex ====>>" + favModeIndex);
                    if(i != favModeIndex) {

                        for(int j = 0; j < ProgramManagerList.get(i).ProgramManagerInfoList.size(); j++) {
                            ProgramManagerInfo favProgramother = ProgramManagerList.get(i).ProgramManagerInfoList.get(j);
                            if(favProgramother.getChannelId() == ProgramManagerInfoList.get(index).getChannelId()) {
                                favIocn = 1;
                                break;
                            }
                        }
                    }
                    if(favIocn == 1)
                        break;
                }
                if(favIocn == 0) {
                    for(int j = 0; j < ProgramManagerList.get(0).ProgramManagerInfoList.size(); j++) {
                        ProgramManagerInfo programInfo = ProgramManagerList.get(0).ProgramManagerInfoList.get(j);
                        if(programInfo.getChannelId() == ProgramManagerInfoList.get(index).getChannelId()) {
                            ProgramManagerList.get(0).ProgramManagerInfoList.get(j).setFavIcon(0);
                            break;
                        }
                    }
                }
                ProgramManagerInfoList.remove(index);
            }

        }

        public void AddProgramToFav(List<ProgramManagerImpl> ProgramManagerList, int GroupType,int srcIndex) {
            Log.i(TAG, "AddProgramToFav: GroupType = " + GroupType);
            int size=0;//eric lin 20171225 check add fav's srcIndex
            if(curGroupType >= ProgramInfo.ALL_TV_RADIO_TYPE_MAX) {
                return;
            }
            if(isAllTvRadio(GroupType)) {
                return;
            }
            else {
                size = ProgramManagerList.get(0).ProgramManagerInfoList.size();//eric lin 20171225 check add fav's srcIndex
                if(size == 0 || !(srcIndex >=0 && srcIndex<size)) {//eric lin 20171225 check add fav's srcIndex
                    return;
                }
                ProgramManagerInfo programInfo = new ProgramManagerInfo(ProgramManagerList.get(0).ProgramManagerInfoList.get(srcIndex)); // connie 20180524 modify for fav group channel number show index
                boolean add = true;
                for(int i = 0; i < ProgramManagerList.get(GroupType).ProgramManagerInfoList.size(); i++) {
                    if(programInfo.getChannelId() == ProgramManagerList.get(GroupType).ProgramManagerInfoList.get(i).getChannelId()) {
                        add = false;
                        break;
                    }
                }
                if(add) { // connie 20180524 modify for fav group channel number show index
                    programInfo.setChannelNum(ProgramManagerList.get(GroupType).ProgramManagerInfoList.size()+1);
                    programInfo.setFavIcon(1);//Scoty 20180620 fixed fav icon at fav list not show
                    ProgramManagerList.get(GroupType).ProgramManagerInfoList.add(ProgramManagerList.get(GroupType).ProgramManagerInfoList.size(), programInfo);
                }
            }
        }

        public void ResetFavChannelNum(List<ProgramManagerImpl> ProgramManagerList, int FavGroup, int startPos, int endPos) // connie 20180524 modify for fav group channel number show index
        {
            int listSize = ProgramManagerList.get(FavGroup).ProgramManagerInfoList.size();
            int start = startPos, end = endPos;
            if (listSize == 0 || startPos >= listSize || endPos >= listSize)
                return;
            for(int i = start;i <= end ; i++)
            {
                Log.d(TAG, "ResetFavChannelNum:  i = " + i + "     " + ProgramManagerList.get(FavGroup).ProgramManagerInfoList.get(i).getChannelName());
                ProgramManagerList.get(FavGroup).ProgramManagerInfoList.get(i).setChannelNum(i+1);
            }
        }

        public void DelAllProgram(int del) {

            for(int i = 0; i < ProgramManagerInfoList.size(); i++) {
                ProgramManagerInfoList.get(i).setDelete(del);
            }
        }

        public int GetCurPosByChannelId(long channelId)//Scoty 20180801 fixed startonchannel search fail crash
        {
            for(int i = 0; i < ProgramManagerInfoList.size(); i++)
            {
                if(channelId == ProgramManagerInfoList.get(i).getChannelId())
                    return i;
            }
            return -1;
        }

        public void Save() {
            if(curGroupType >= ProgramInfo.ALL_TV_RADIO_TYPE_MAX)
                return;
            BookManager bookManager = null;
            bookManager = GetUIBookManager();
            int bookSave = 0;
            int j = 0;
            List<SimpleChannel> pList = new ArrayList<>();
            for(int i = 0; i < ProgramManagerInfoList.size(); i++) {
                if(ProgramManagerInfoList.get(i).getDelete() == 0) {
                    if(!isAllTvRadio(curGroupType))//Scoty 20180620 fixed after delete channel, fav list channel num not reset
                        ProgramManagerInfoList.get(i).setChannelNum(++j);
                    //Log.d(TAG,"chname ==> " + ProgramManagerInfoList.get(i).getChannelName());
                    pList.add(ProgramManagerInfoList.get(i).TransferToProgramInfo());
                }
                else {
                    if(isAllTvRadio(curGroupType)) {
                        //Scoty 20180523 modify check timer channel exist or not
                        bookSave = bookManager.DelBookInfoByChannelId(ProgramManagerInfoList.get(i).getChannelId());
                        ProgramInfoDeleteNotReSortChannelId(ProgramManagerInfoList.get(i).getChannelId());
                    }
                }
            }
            if(pList.size() > 0)
                ProgramInfoUpdateList(pList,curGroupType);
            else if(!isAllTvRadio(curGroupType)){
                FavInfoDeleteAll(curGroupType);
            }
            if(bookSave == 1)
                bookManager.Save();
        }
    }

    public ArrayList<List<SimpleChannel>> GetProgramManagerTotalChannelList()
    {
        return super.GetProgramManagerTotalChannelList();
    }

    public List<ProgramManagerImpl> GetProgramManager(int type)
    {
        List<ProgramManagerImpl> ProgramManagerList = new ArrayList<ProgramManagerImpl>();// = GetProgramManagerList();//Scoty-Test ProgramList

        if(type == ProgramInfo.ALL_TV_TYPE) {
            for(int i = ProgramInfo.ALL_TV_TYPE; i < ProgramInfo.ALL_RADIO_TYPE; i++) {
                ProgramManagerImpl programManagerImpl = new ProgramManagerImpl(i);
                ProgramManagerList.add(programManagerImpl);
            }
        }
        if(type == ProgramInfo.ALL_RADIO_TYPE) {
            for(int i = ProgramInfo.ALL_RADIO_TYPE; i < ProgramInfo.ALL_TV_RADIO_TYPE_MAX; i++) {
                ProgramManagerImpl programManagerImpl = new ProgramManagerImpl(i);
                ProgramManagerList.add(programManagerImpl);
            }
        }
        for(int k = 0; k < ProgramManagerList.get(0).ProgramManagerInfoList.size(); k ++) {
            for (int i = 1; i < ProgramManagerList.size(); i++) {
                for (int j = 0; j < ProgramManagerList.get(i).ProgramManagerInfoList.size(); j++) {
                    SimpleChannel allProgramInfo = ProgramManagerList.get(0).ProgramManagerInfoList.get(k),
                            favProgramInfo = ProgramManagerList.get(i).ProgramManagerInfoList.get(j);

                    if (allProgramInfo.getChannelId() == favProgramInfo.getChannelId()) {
                        ProgramManagerList.get(0).ProgramManagerInfoList.get(k).setFavIcon(1);
                        break;
                    }
                }
            }
        }

        return ProgramManagerList;
    }

/*
    public void ProgramManagerInit(int type) { // all tv or all radio
        if(ProgramManagerList.size() != 0)
            ProgramManagerList.clear();
        if(type == ProgramInfo.ALL_TV_TYPE) {
            for(int i = ProgramInfo.ALL_TV_TYPE; i < ProgramInfo.ALL_RADIO_TYPE; i++) {
                ProgramManagerImpl programManagerImpl = new ProgramManagerImpl(i);
                ProgramManagerList.add(programManagerImpl);
            }
        }
        if(type == ProgramInfo.ALL_RADIO_TYPE) {
            for(int i = ProgramInfo.ALL_RADIO_TYPE; i < ProgramInfo.ALL_TV_RADIO_TYPE_MAX; i++) {
                ProgramManagerImpl programManagerImpl = new ProgramManagerImpl(i);
                ProgramManagerList.add(programManagerImpl);
            }
        }
        for(int k = 0; k < ProgramManagerList.get(0).ProgramManaterInfoList.size(); k ++) {
            for (int i = 1; i < ProgramManagerList.size(); i++) {
                for (int j = 0; j < ProgramManagerList.get(i).ProgramManaterInfoList.size(); j++) {
                    SimpleChannel allProgramInfo = ProgramManagerList.get(0).ProgramManaterInfoList.get(k),
                            favProgramInfo = ProgramManagerList.get(i).ProgramManaterInfoList.get(j);

                    if (allProgramInfo.getChannelId() == favProgramInfo.getChannelId()) {
                        ProgramManagerList.get(0).ProgramManaterInfoList.get(k).setFavIcon(1);
                        break;
                    }
                }
            }
        }
        BookManagerInit();
    }
*/


    public int SaveTable(EnTableType otpion) {
        return super.SaveTable(otpion);
    }

    public int setDefaultOpenChannel(long channelId, int groupType) {
        return super.setDefaultOpenChannel(channelId, groupType);
    }

    public DefaultChannel getDefaultOpenChannel() {
        return super.getDefaultOpenChannel();
    }

    public void ProgramInfoPlaySimpleChannelListUpdate(int IncludePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        super.ProgramInfoPlaySimpleChannelListUpdate(IncludePVRSkipFlag);
    }

    public void ProgramInfoPlaySimpleChannelListUpdate(Context context, int IncludePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        super.ProgramInfoPlaySimpleChannelListUpdate(context,IncludePVRSkipFlag);
    }

    private void ProgramManagerUpdateHistory() {
        SimpleChannel curChannel = null;
        SimpleChannel preChannel = null;
        if(Pvcfg.IsEnableNetworkPrograms())
            ProgramInfoPlaySimpleChannelListUpdate(this,1);//Scoty 20181109 modify for skip channel//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        else
            ProgramInfoPlaySimpleChannelListUpdate(1);

        if(ViewHistory.getCurChannel() != null) {
            long curChannelId = ViewHistory.getCurChannel().getChannelId();
            curChannel = GetSimpleProgramByChannelIdfromTotalChannelListByGroup(ViewHistory.getCurGroupType(), curChannelId);
        }else {
            curChannel = null;
        }
        if(ViewHistory.getPreChannel() != null)
            preChannel = GetSimpleProgramByChannelIdfromTotalChannelListByGroup(ViewHistory.getCurGroupType(), ViewHistory.getCurChannel().getChannelId());
        List<SimpleChannel> preChannelList = null;
        List<SimpleChannel> curChannelList = null;

        if(preChannel != null) {
            preChannelList = ProgramInfoGetPlaySimpleChannelList(ViewHistory.getPreGroupType(),0);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
            ViewHistory.SetPreChannel(preChannel,preChannelList,ViewHistory.getPreGroupType());
        }
        else {
            ViewHistory.SetPreChannel(null,null,-1);
        }

        curChannelList = ProgramInfoGetPlaySimpleChannelList(ViewHistory.getCurGroupType(),0);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        if(curChannel != null) {
            //Scoty 20180629 fixed set userlock/unlock current channel back to view lock/unlock show wrong -s
            if(ViewHistory.getCurChannel().getUserLock() == curChannel.getUserLock()) {
                ViewHistory.SetCurChannel(curChannel, curChannelList, ViewHistory.getCurGroupType());
            }
            else {
                AvControlPlayStop(ViewHistory.getPlayId());
                AvControlPlayByChannelId(ViewHistory.getPlayId(), ViewHistory.getCurChannel().getChannelId(), ViewHistory.getCurGroupType(), 1);
            }
            ViewHistory.getCurChannel().setUserLock(curChannel.getUserLock());
            //Scoty 20180629 fixed set userlock/unlock current channel back to view lock/unlock show wrong -e
        }
        else {
            if(curChannelList != null && !curChannelList.isEmpty()) {//if(curChannelList.size() != 0) { //eric lin fix crash
                curChannel = curChannelList.get(0);
                ViewHistory.SetCurChannel(curChannel,curChannelList,ViewHistory.getCurGroupType());
            }
            else {
                int type = ProgramInfo.ALL_TV_TYPE;
                if(ViewHistory.getCurGroupType() > ProgramInfo.ALL_TV_TYPE && ViewHistory.getCurGroupType() < ProgramInfo.ALL_RADIO_TYPE) {
                    curChannelList = ProgramInfoGetPlaySimpleChannelList(ProgramInfo.ALL_TV_TYPE,0);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
                    type = ProgramInfo.ALL_TV_TYPE;
                }
                else if (ViewHistory.getCurGroupType() > ProgramInfo.ALL_RADIO_TYPE
                        && ViewHistory.getCurGroupType() < ProgramInfo.ALL_TV_RADIO_TYPE_MAX) {
                    curChannelList = ProgramInfoGetPlaySimpleChannelList(ProgramInfo.ALL_RADIO_TYPE,0);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
                    type = ProgramInfo.ALL_RADIO_TYPE;
                }
                else {
                    Log.d(TAG,"ProgramManagerUpdateHistory error!!!!!!");
                    if(ViewHistory.getCurGroupType() == ProgramInfo.ALL_TV_TYPE) {
                        curChannelList = ProgramInfoGetPlaySimpleChannelList(ProgramInfo.ALL_RADIO_TYPE, 0);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
                        type = ProgramInfo.ALL_RADIO_TYPE;
                    }
                    else {
                        curChannelList = ProgramInfoGetPlaySimpleChannelList(ProgramInfo.ALL_TV_TYPE, 0);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
                        type = ProgramInfo.ALL_TV_TYPE;
                    }

                    if(curChannelList == null || curChannelList.size() <= 0)
                    {
                        AvControlPlayStop(ViewHistory.getPlayId());
                        ViewHistory.SetCurChannel(null,null,-1);
                        SetChannelExist(0);
                        if(getplatForm() == 1)
                            setTempPesiDefaultChannelFlag(1);
                        return;
                    }

                }
                curChannel = curChannelList.get(0);
                ViewHistory.SetCurChannel(curChannel,curChannelList,type);
            }
            AvControlPlayStop(ViewHistory.getPlayId());
            AvControlPlayByChannelId(ViewHistory.getPlayId(),ViewHistory.getCurChannel().getChannelId(),ViewHistory.getCurGroupType(),1);
        }
    }

    public void ProgramManagerSave(List<ProgramManagerImpl> ProgramManagerList) {
        for(int i = 0; i < ProgramManagerList.size(); i++) {
            ProgramManagerList.get(i).Save();
        }
        ProgramManagerUpdateHistory();//update ViewHistory preChannel and CurChannel
        SaveTable(EnTableType.PROGRAME);
    }
/*
    public void ProgramManagerSave() {
        for(int i = 0; i < ProgramManagerList.size(); i++) {
            ProgramManagerList.get(i).Save();
        }
        ProgramManagerUpdateHistory();//update ViewHistory preChannel and CurChannel
        SaveTable(EnTableType.PROGRAME);
    }
*/
    public class ViewUiDisplay{
        //public ChannelHistory History = ChannelHistory.GetInstance();
        public EPGEvent EpgPreData = null;
        public EPGEvent EpgFolData = null;

        public ViewUiDisplay() {
            if(ViewHistory.getCurChannel() != null)
            {
                UpdateEpgPF();
            }
        }

    public void UpdateEpgPF() {
        if(ViewHistory.getCurChannel() != null) {
            Log.d(TAG, "UpdateEpgPF:  ==>> " + ViewHistory.getCurChannel().getChannelId());
            //Scoty Add Youtube/Vod Stream -s
            if (ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE) {
                Log.d(TAG, "VOD UpdateEpgPF: " + ViewHistory.getCurChannel().getPresentepgEvent());
                EpgPreData = ViewHistory.getCurChannel().getPresentepgEvent();
                EpgFolData = ViewHistory.getCurChannel().getFollowepgEvent();
            } else if (ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.YOUTUBE_TYPE) {
                Log.d(TAG, "YOUTUBE UpdateEpgPF: " + ViewHistory.getCurChannel().getPresentepgEvent());
                EpgPreData = ViewHistory.getCurChannel().getPresentepgEvent();
                EpgFolData = ViewHistory.getCurChannel().getFollowepgEvent();
            } else {
                EpgPreData = EpgEventGetPresentEvent(ViewHistory.getCurChannel().getChannelId());
                EpgFolData = EpgEventGetFollowEvent(ViewHistory.getCurChannel().getChannelId());
            }
            //Scoty Add Youtube/Vod Stream -e
        }
    }

        public boolean ChangeChannelByDigi(int displayNum) {
            for(int i = 0; i < ViewHistory.getCurChannelList().size(); i++) {
                if(ViewHistory.getCurChannelList().get(i).getChannelNum() == displayNum) {
                    if(ViewHistory.getCurChannelList().get(i).getChannelId() != ViewHistory.getCurChannel().getChannelId())//Scoty 20180801 fixed after unlock the channel and then digit set to the same channel no need show password dialog
                    AvControlPlayByChannelId(ViewHistory.getPlayId(),ViewHistory.getCurChannelList().get(i).getChannelId(),ViewHistory.getCurGroupType(),1);
                    return true;
                }
            }
            return false;
        }

        public void ChangeProgram()//change channel
        {
            if(ViewHistory.getCurChannel() != null)
            {
                AvControlPlayByChannelId(ViewHistory.getPlayId(), ViewHistory.getCurChannel().getChannelId(), ViewHistory.getCurGroupType(),1);

                // use MtestTestAvMultiPlay to change program, AvControlPlayByChannelID has problem after split play
//                List<Integer> tunerIDs = new ArrayList<>();
//                List<Long> channelIDs = new ArrayList<>();
//
//                tunerIDs.add(0);
//                channelIDs.add(ViewHistory.getCurChannel().getChannelId());
//                MtestTestAvMultiPlay(1, tunerIDs, channelIDs);
            }
        }

        public void ChangeBannerInfoUp()
        {
            if(ViewHistory.getCurChannelList().size() > 1)
            {
                ViewHistory.setChannelUp();
                UpdateEpgPF();
            }
        }

        public void ChangeBannerInfoDown()
        {
            if(ViewHistory.getCurChannelList().size() > 1)
            {
                ViewHistory.setChannelDown();
                UpdateEpgPF();
            }
        }

        public void ChangePreProgram() {
            if(ViewHistory.getPreChannel() != null &&
                    (ViewHistory.getPreChannel().getChannelId() != ViewHistory.getCurChannel().getChannelId() &&
                            ViewHistory.getCurGroupType() == ViewHistory.getPreGroupType())) {
                AvControlPlayByChannelId (ViewHistory.getPlayId(),ViewHistory.getPreChannel().getChannelId(),ViewHistory.getPreGroupType(),1);
                UpdateEpgPF();
            }
        }

        public boolean ChangeGroup(List<SimpleChannel> channelList, int groupType, long channelId) {
            if (channelList == null)    // Johnny add 20180122
            {
                channelList = new ArrayList<>();
            }

            if(channelList.size() > 0) {
                AvControlPlayByChannelId(ViewHistory.getPlayId(),channelId, groupType,1);
                UpdateEpgPF();
                return true;
            }
            return false;
        }

        public boolean CheckNewMail() {
            return false;
        }

        public String GetShortEvent(long channelID, int eventID)
        {
            return EpgEventGetShortDescription(channelID, eventID);
        }
        public String GetDetailInfo(long channelID, int eventID)
        {
            return EpgEventGetDetailDescription(channelID, eventID);
        }

        //Pip -Start
        public void OpenPipScreen(int x , int y, int width, int height)
        {
            PipOpen(x+4,y+4,width-4,height-4);
            PipStart(ViewHistory.getCurPipChannel().getChannelId(),1);
        }

        public void ClosePipScreen()
        {
            PipStop();
            PipClose();
            ViewHistory.setCurPipIndex(-1);
        }

        public void SetPipWindow(int x, int y, int width, int height)
        {
            PipSetWindow(x+4,y+4,width - 4,height - 4);
        }

        public void SetAvPipExChange()
        {
            ViewHistory.setAvPipExchange();
            PipExChange();
        }

        public void ChangePipChannelUp()
        {
            if(ViewHistory.getCurChannelList().size() > 1)
            {
                ViewHistory.setPipChannelUp();
            }
        }

        public void ChangePipChannelDown()
        {
            if(ViewHistory.getCurChannelList().size() > 1)
            {
                ViewHistory.setPipChannelDown();
            }
        }

        public void ChangePipProgram()
        {
            PipStart(ViewHistory.getCurPipChannel().getChannelId(),1);
        }

        public void UpdatePipEpgPF() {
            if(ViewHistory.getCurPipChannel() != null) {
                Log.d(TAG, "UpdatePipEpgPF:  ==>> " + ViewHistory.getCurPipChannel().getChannelId());

                EpgPreData = EpgEventGetPresentEvent(ViewHistory.getCurPipChannel().getChannelId());
                EpgFolData = EpgEventGetFollowEvent(ViewHistory.getCurPipChannel().getChannelId());
            }
        }

        public boolean ChangePipChannelByDigi(int displayNum, int tpId) {
            for(int i = 0; i < ViewHistory.getCurChannelList().size(); i++) {
                if(ViewHistory.getCurChannelList().get(i).getChannelNum() == displayNum) {
                    PipStart(ViewHistory.getCurChannelList().get(i).getChannelId(),1);
                    return true;
                }
            }
            return false;
        }

        //Pip -End
    }

//    public void SetViewUiDisplayManager(ViewUiDisplay viewUiDisplay)
//    {
//        super.SetViewUiDisplayManager(viewUiDisplay);
//    }
//
//    public ViewUiDisplay GetViewUiDisplayManager()
//    {
//        return super.GetViewUiDisplayManager();
//    }

    public ViewUiDisplay GetViewUiDisplay()
    {
        if(GetViewUiDisplayManager() == null)
            SetViewUiDisplayManager(new ViewUiDisplay());

        return GetViewUiDisplayManager();
    }

//    public void ViewUiDisplayInit() {
//        viewUiDisplay = new ViewUiDisplay();
//    }

    private static Object lock = new Object();
    int one_hour_in_sec = 60*60;
    public class EpgUiDisplay {
        private int GroupType;
        public List<SimpleChannel> programInfoList = null;
        public List<EPGEvent> epgDisplayData = new ArrayList<>();
        public List<EPGEvent> epgUpdateData = new ArrayList<>();

        public EpgUiDisplay(int groupType) {
            GroupType = groupType;
            programInfoList = ProgramInfoGetPlaySimpleChannelList(groupType,0);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        }

        private List<EPGEvent> EpgPFDataUpdate(int position) {
            SimpleChannel programInfo = programInfoList.get(position);
            List<EPGEvent> tempData = new ArrayList<>();
            EPGEvent present = EpgEventGetPresentEvent(programInfo.getChannelId());
            EPGEvent follow = EpgEventGetFollowEvent(programInfo.getChannelId());
            if(present != null)
                tempData.add(present);
            if(follow != null)
                tempData.add(follow);
            if(tempData.size() > 0)
                return tempData;
            else
                return null;
        }

        private List<EPGEvent> EpgScheduleDataUpdate(int position,long startTime, long endTime, int addEmpty) {
            SimpleChannel programInfo = programInfoList.get(position);
            //should remove in hisillion !!-s
//            List<EPGEvent> tempData = new ArrayList<>();
//            List<EPGEvent> epgEventList = EpgEventGetEPGEventList(programInfo.getChannelId(),startTime,endTime);
//            if((epgEventList == null) || (epgEventList.size() == 0))
//                return null;
//            else {
//                for(int i = 0; i < epgEventList.size(); i++) {
//                long EpgStartTime = GetStartTime(epgEventList.get(i));
//                long EpgEndTime = GetEndTime(epgEventList.get(i));
//                if((startTime <= EpgStartTime && endTime >= EpgEndTime) || (endTime > EpgStartTime && endTime < EpgEndTime)
//                        || (startTime < EpgEndTime && startTime > EpgStartTime) || (startTime > EpgStartTime && endTime < EpgEndTime)) {
//                tempData.add(epgEventList.get(i));
//                }
//                }
//                return tempData;
                //return epgEventList; // speed up epg
//            }
            //should remove in hisillion !!-e
            //should use in hisillion !!-s
            List<EPGEvent> epgEventList = EpgEventGetEPGEventList(programInfo.getChannelId(),startTime,endTime, addEmpty);
            return epgEventList;
            //should use in hisillion !!-e
        }

        public void EitDataUpdate(int position,long startTime, long endTime, int addEmpty) {
            synchronized (lock) {
                if(epgUpdateData != null)
                    epgUpdateData.clear();
                epgUpdateData = EpgScheduleDataUpdate(position, startTime, endTime, addEmpty);
                if(epgUpdateData == null || epgUpdateData.size() == 0) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String srcDate = sdf.format(new Date(startTime));
                    List<EPGEvent> tempEventList = EpgPFDataUpdate(position);
                    if(tempEventList != null && tempEventList.size()!=0) {
                        String EpgDate = sdf.format(tempEventList.get(0).getStartTime());   // johnny modify 20171227
                        if(srcDate.equals(EpgDate) == true) {
                            epgUpdateData = EpgPFDataUpdate(position);
                        }
                    }
                }
            }
        }

        public void EitDataDisplayUpdate() {
            epgDisplayData.clear();
            if(epgUpdateData != null && epgUpdateData.size() != 0) {
                for(int i = 0; i < epgUpdateData.size(); i++) {
                    epgDisplayData.add(epgUpdateData.get(i));
                }
            }
        }

        public void ChangeProgram(int position) {
            Log.d(TAG, "ChangeProgram: position == " + position);
            final int pos = position;
            if(ViewHistory.getCurChannel() != null) {
                if (ViewHistory.getCurChannel().getChannelId() != programInfoList.get(pos).getChannelId())
                {
                    AvControlPlayByChannelId(ViewHistory.getPlayId(), programInfoList.get(pos).getChannelId(), GroupType,1);
                }
            }
        }

        public boolean ChangeGroup(int groupType) {
            List<SimpleChannel> tempList = null;
            tempList = ProgramInfoGetPlaySimpleChannelList(groupType,0);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
            if(tempList != null) {
                GroupType = groupType;
                programInfoList = tempList;
                return true;
            }
            return false;
        }

        public String GetShortEvent(long channelID, int eventID) {
            return EpgEventGetShortDescription(channelID, eventID);
        }

        public String GetDetailInfo(long channelID, int eventID)
        {
            return EpgEventGetDetailDescription(channelID, eventID);
        }
    }

//    public void SetEpgUiDisplayManager(EpgUiDisplay epgUiDisplay)
//    {
//        super.SetEpgUiDisplayManager(epgUiDisplay);
//    }
//
//    public EpgUiDisplay GetEpgUiDisplayManager(int type)
//    {
//        return super.GetEpgUiDisplayManager(type);
//    }

    public EpgUiDisplay GetEpgUiDisplay(int type)
    {
        EpgUiDisplay mEpgUiDisplay = null;

        if(GetEpgUiDisplayManager(type) == null)
            SetEpgUiDisplayManager(new EpgUiDisplay(type));

        mEpgUiDisplay = GetEpgUiDisplayManager(type);
        mEpgUiDisplay.GroupType = type;
        mEpgUiDisplay.programInfoList = ProgramInfoGetPlaySimpleChannelList(type,0);

        return mEpgUiDisplay;
        //GetEpgUiDisplay(type).GroupType = type;
        //GetEpgUiDisplay(type).programInfoList = ProgramInfoGetPlaySimpleChannelList(type,0);


        //BookManagerInit();
        //return new EpgUiDisplay(type);
    }

//    public void EpgUiDisplayInit(int type) {
//        epgUiDisplay = new EpgUiDisplay(type);
//        BookManagerInit();
//    }

    public int BcdToBin (int bcd)
    {
        return (((bcd & 0xf0000000) >> 28) * 10000000 +
                ((bcd & 0xf000000) >> 24) * 1000000 +
                ((bcd & 0xf00000) >> 20) * 100000 +
                ((bcd & 0xf0000) >> 16) * 10000 +
                ((bcd & 0xf000) >> 12) * 1000 +
                ((bcd & 0xf00) >> 8) * 100 +
                ((bcd & 0xf0) >> 4) * 10 +
                (bcd & 0xf));
    }

    public Date GetLocalTime() {
        Date Time= getLocalTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(Time == null)
        {
            Log.d(TAG, "GetLocalTime:  TIME = NULL !!!!!!!!!");
            Time = new Date();
        }
        String str = format.format(Time.getTime());
        return Time;
    }

    public Date TmGetDateTime() {
        Date Time= getLocalTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(Time == null)
        {            
            Time = new Date();
        }
        String str = format.format(Time.getTime());        
        return Time;
    }

    public int TmSetDateTime(Date date){
        return setTime(date);
    }

 public int TmGetCurrentTimeZone(){
        int TimeZone = getDtvTimeZone();
        return TimeZone;
    }

    public int TmSetTimeZone(int zonesecond){
        return setDtvTimeZone(zonesecond);
    }

    public int TmGetSettingTDTStatus(){
        int TDTStatus = getSettingTDTStatus();
        return TDTStatus;
    }

    public int TmSetSettingTDTStatus(int onoff){
        Log.d(TAG, "EEE TmSetSettingTDTStatus: onoff="+onoff);
        return setSettingTDTStatus(onoff);        
    }

    public int TmSetTimeToSystem(boolean bSetTimeToSystem){
        return setTimeToSystem(bSetTimeToSystem);
    }

    public int SyncTime(boolean value)
    {
        return syncTime(value);
    }

    public int  TmGetDaylightSaving(){
        int dayLight = getDtvDaylight();
        return dayLight;
    }

    public int TmSetDaylightSaving(int onoff)
    {
        return setDtvDaylight(onoff);
    }

    public List<BookInfo> InitUIBookList(){
        return super.InitUIBookList();
    }

    public List<BookInfo> GetUIBookList()
    {
        return super.GetUIBookList();
    }

//    public void SetUIBookManager(BookManager bookmanager)
//    {
//        super.SetUIBookManager(bookmanager);
//    }

    public BookManager GetBookManager()
    {
        if(GetUIBookManager() == null)
            SetUIBookManager(new BookManager());

        return GetUIBookManager();
    }

//    public BookManager GetBookList()
//    {
//        return new BookManager();
//
//    }
//    public void BookManagerInit() {
//        bookManager = new BookManager();
//    }

    //bookinfo ????channelId???channel number
    public class BookManager {
        public List<BookInfo> BookList = null;
        public BookManager() {
            //BookList = BookInfoGetList();
            BookList = GetUIBookList();
            if(BookList == null)
                BookList = new ArrayList<>();
        }

        public void UpdateUIBookList()
        {
            BookList = GetUIBookList();
        }

        public BookInfo CheckExist(long channelId, int groupType,long startTime,long duration) {
            long MINUTE_IN_MILLIS = TimeUnit.MINUTES.toMillis(1);
            long checkBookStartTime = startTime - startTime%MINUTE_IN_MILLIS; // connie 20180530 fix check time exist fail
            long checkBookDuration = duration - duration%MINUTE_IN_MILLIS;// connie 20180530 fix check time exist fail
            for(int i = 0; i < BookList.size(); i++) {
                long BookStartTime = 0,BookDuration = 0;
                String BookStartTimeString = BookList.get(i).getYear() + "-" + String.format("%02d",BookList.get(i).getMonth()) + "-"
                        + String.format("%02d",BookList.get(i).getDate())
                        + " " +  String.format("%02d",BookList.get(i).getStartTime()/100) + ":" + String.format("%02d",BookList.get(i).getStartTime()%100)
                        + ":" + "00";
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date BookDate = null;
                try {
                    BookDate = sdf.parse(BookStartTimeString);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return null;    // johnny add 20171221
                }
                BookStartTime = BookDate.getTime();
                BookDuration = BookList.get(i).getDuration()/100*60*60*1000 + BookList.get(i).getDuration()%100*60*1000;
                if(BookList.get(i).getChannelId() == channelId /*&& BookList.get(i).getGroupType() == groupType*/
                        && BookStartTime == checkBookStartTime
                        && BookDuration == checkBookDuration)// connie 20180530 fix check time exist fail
                {
                    return BookList.get(i);
                }
            }
            return null;
        }

        public boolean CheckFull() {
            if(BookList.size() >= BookInfo.MAX_NUM_OF_BOOKINFO)
                return true;
            else
                return false;
        }

        public int GetEmptyBookId() {
            if(BookList.size() >= BookInfo.MAX_NUM_OF_BOOKINFO)
                return BookInfo.MAX_NUM_OF_BOOKINFO;
            else
                return BookList.size();
        }

        public void AddBookInfo(BookInfo bookInfo) {
            BookList.add(bookInfo);
        }

        public void DelBookInfo(BookInfo bookInfo) {
            for(int i = 0; i < BookList.size(); i++) {
                if(bookInfo.getBookId() == BookList.get(i).getBookId())
                    BookList.remove(i);
            }
            for(int i = 0; i < BookList.size(); i++) {
                BookList.get(i).setBookId(i);
            }
        }

        // Johnny add 20180316, use this to del if you want to del several books.
        // Rearrange BookId after del the whole bookList rather than del one book.
        public void DelBookInfoList(List<BookInfo> bookInfoList) {
            if (bookInfoList == null)
            {
                return;
            }

            for(int i = 0; i < bookInfoList.size(); i++) {
                for (int j = 0 ; j < BookList.size() ; j++)
                {
                    if(bookInfoList.get(i).getBookId() == BookList.get(j).getBookId()) {
                        BookList.remove(j);
                        break;
                    }
                }
            }

            for(int i = 0; i < BookList.size(); i++) {
                BookList.get(i).setBookId(i);
            }
        }

        public int DelBookInfoByChannelId(long channelId) {
            List<BookInfo> found = new ArrayList<>();
            int foundChannel = 0;//Scoty 20180523 modify check timer channel exist or not
            for(BookInfo bookInfo : BookList) {
                if(channelId == bookInfo.getChannelId())
                {
                    found.add(bookInfo);
                    foundChannel = 1;//Scoty 20180523 modify check timer channel exist or not
                }
            }

            if(foundChannel == 1) {//Scoty 20180523 modify check timer channel exist or not
                BookList.removeAll(found);

                for (int i = 0; i < BookList.size(); i++) {
                    BookList.get(i).setBookId(i);
                }
            }
            return foundChannel;
        }
        public Date GetEndTime(BookInfo bookInfo){
            long BookStartTime = 0,BookDuration = 0;
            String BookStartTimeString = bookInfo.getYear() + "-" + String.format("%02d",bookInfo.getMonth()) + "-"
                    + String.format("%02d",bookInfo.getDate())
                    + " " +  String.format("%02d",bookInfo.getStartTime()/100) + ":" + String.format("%02d",bookInfo.getStartTime()%100)
                    + ":" + "00";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date BookDate = null;
            try {
                BookDate = sdf.parse(BookStartTimeString);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
            BookStartTime = BookDate.getTime();
            BookDuration = bookInfo.getDuration()/100*60*60*1000 + bookInfo.getDuration()%100*60*1000;
            Date EndTime = new Date(BookStartTime+BookDuration);
            //Log.d(TAG,"endtime ==> startTime " + startTime + " duration ==>> " + duration + "hour " + hour + " min " + min);
            return EndTime;
        }

        public boolean CheckBookAfterNow(BookInfo bookInfo) {
            Date date = GetLocalTime();
            String timeString = bookInfo.getYear() + "-" + String.format("%02d",bookInfo.getMonth()) + "-" + String.format("%02d",bookInfo.getDate())
                    + " " +  String.format("%02d",bookInfo.getStartTime()/100) + ":" + String.format("%02d",bookInfo.getStartTime()%100) + ":"
                    + "00";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date BookDate=null;
            try {
                BookDate = sdf.parse(timeString);
                if((BookDate.getTime()) > date.getTime()+60*1000)
                    return true;
                else
                    return false;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return false;
        }

//        public void Save(List<BookInfo> bookList) {
//            if(bookList.size() > 0) {
//                BookInfoUpdateList(bookList);
//            }
//            else {
//                BookInfoDeleteAll();
//            }
//
//            SaveTable(EnTableType.TIMER);
//        }

        public void Save() {
            Log.d(TAG, "Save: size = [" + BookList.size() +"]");
            if(BookList.size() > 0) {
                BookInfoUpdateList(BookList);
            }
            else {
                BookInfoDeleteAll();
            }
            //BookList = GetUIBookList();
            SaveTable(EnTableType.TIMER);
        }
    }

    public BookInfo GetTaskByIdFromUIBookList(int id)
    {
        return super.GetTaskByIdFromUIBookList(id);
    }

    public BookInfo BookInfoGet(int bookId) {
        return super.BookInfoGet(bookId);
    }

    public List<BookInfo> BookInfoGetList() {
        return super.BookInfoGetList();
    }

    public int BookInfoAdd(BookInfo book) {
        return super.BookInfoAdd(book);
    }

    public int BookInfoUpdate(BookInfo book) {
        return super.BookInfoUpdate(book);
    }

    public int BookInfoUpdateList(List<BookInfo> bookList) {
        return super.BookInfoUpdateList(bookList);
    }

    public int BookInfoDelete(int bookId) {
        return super.BookInfoDelete(bookId);
    }

    public int BookInfoDeleteAll() {
        return super.BookInfoDeleteAll();
    }

    public BookInfo BookInfoGetComingBook() {
        return super.BookInfoGetComingBook();
    }

    public List<BookInfo> BookInfoFindConflictBooks(BookInfo bookInfo) {
        return super.BookInfoFindConflictBooks(bookInfo);
    }

    public List<SatInfo> SatInfoGetList(int tunerType) {
        Log.i(TAG, "GetSatInfoList: tunerType = " + tunerType);
        return super.SatInfoGetList(tunerType,MiscDefine.SatInfo.POS_ALL,MiscDefine.SatInfo.NUM_ALL);
    }

    public SatInfo SatInfoGet(int satId) {
        Log.i(TAG, "GetSatInfoBySatId: satId="+satId);
        return super.SatInfoGet(satId);
    }

    public int SatInfoAdd(SatInfo sat) {
        return super.SatInfoAdd(sat);
    }

    public int SatInfoUpdate(SatInfo sat) {
        return super.SatInfoUpdate(sat);
    }

    public int SatInfoUpdateList(List<SatInfo> satList) {
        return super.SatInfoUpdateList(satList);
    }

    public int SatInfoDelete(int satId) {
        return super.SatInfoDelete(satId);
    }

    public List<TpInfo> TpInfoGetList(int tunerType) {
        Log.i(TAG, "GetTpInfoList: ");
        return super.TpInfoGetListBySatId(tunerType,MiscDefine.TpInfo.NONE_SAT_ID,MiscDefine.TpInfo.POS_ALL,MiscDefine.TpInfo.NUM_ALL);
    }

    public List<TpInfo> TpInfoGetListBySatId(int satId) {
        Log.i(TAG, "TpInfoGetListBySatId: ");
        return super.TpInfoGetListBySatId(MiscDefine.TpInfo.NONE_TUNER_TYPE,satId,MiscDefine.TpInfo.POS_ALL,MiscDefine.TpInfo.NUM_ALL);
    }

    public TpInfo TpInfoGet(int tpId) {
        Log.i(TAG, "TpInfoGetByTpId: tpId="+tpId);
        return super.TpInfoGet(tpId);
    }

    public int TpInfoAdd(TpInfo tp) {
        return super.TpInfoAdd(tp);
    }

    public int TpInfoUpdate(TpInfo tp) {
        return super.TpInfoUpdate(tp);
    }

    public int TpInfoUpdateList(List<TpInfo> tpList) {
        return super.TpInfoUpdateList(tpList);
    }

    public int TpInfoDelete(int tpId) {
        return super.TpInfoDelete(tpId);
    }

    public EPGEvent EpgEventGetPresentEvent(long channdlId) {
        Log.d(TAG, "EpgEventGetPresentEvent: channdlId == " + channdlId);
        return super.EpgEventGetPresentEvent(channdlId);
    }

    public EPGEvent EpgEventGetFollowEvent(long channelId) {
        Log.d(TAG, "EpgEventGetFollowEvent: channdlId == " + channelId);
        return super.EpgEventGetFollowEvent(channelId);
    }

    public EPGEvent EpgEventGetEPGByEventId(long channelId, int eventId) {
        return super.EpgEventGetEPGByEventId(channelId, eventId);
    }


    public List<EPGEvent> EpgEventGetEPGEventList(long channelId, long startTime, long endTime, int addEmpty) {
        return super.EpgEventGetEPGEventList(channelId, startTime, endTime, addEmpty);
    }

    public String EpgEventGetShortDescription(long channelId, int eventId) {
        return super.EpgEventGetShortDescription(channelId, eventId);
    }

    public String EpgEventGetDetailDescription(long channelId, int eventId) {
        return super.EpgEventGetDetailDescription(channelId, eventId);
    }

    public int EpgEventSetLanguageCode(String FirstLangCode, String SecLangCode) {
        return super.EpgEventSetLanguageCode(FirstLangCode, SecLangCode);
    }

    public int EpgEventStartEPG(long channelId) {
        return super.EpgEventStartEPG(channelId);
    }

    public void ResetDefault() {
        Log.d(TAG, "ResetDefault");
        ResetFactoryDefault();
    }

    public String GetPesiServiceVersion() {
        return super.GetPesiServiceVersion();
    }

    public String GetApkSwVersion() {
        return super.GetApkSwVersion();
    }

    public int GetTunerNum()//Scoty 20181113 add GetTunerNum function
    {
        return super.GetTunerNum();
    }

    public void UpdatePvrSkipList(int groupType, int IncludePVRSkipFlag, int tpId, List<Integer> pvrTpList)//Scoty 20181113 add for dual tuner pvrList//Scoty 20180615 update TV/Radio TotalChannelList
    {
        super.UpdatePvrSkipList(groupType,IncludePVRSkipFlag,tpId,pvrTpList);
    }

    public int MtestGetGPIOStatus(int u32GpioNo) {
        return super.MtestGetGPIOStatus(u32GpioNo);
    }

    public int MtestSetGPIOStatus(int u32GpioNo,int bHighVolt) {
        return super.MtestSetGPIOStatus(u32GpioNo,bHighVolt);
    }

    public int MtestGetATRStatus(int smartCardStatus) {
        return super.MtestGetATRStatus(smartCardStatus);
    }

    public int MtestGetHDCPStatus() {
        return super.MtestGetHDCPStatus();
    }

    public int MtestGetHDMIStatus() {
        return super.MtestGetHDMIStatus();
    }

    public int MtestPowerSave() {
        return super.MtestPowerSave();
    }

    public int MtestSevenSegment(int enable) {
        return super.MtestSevenSegment(enable);
    }

    public int MtestSetAntenna5V(int tunerID, int tunerType, int enable) {
        return super.MtestSetAntenna5V(tunerID, tunerType, enable);
    }

    public int MtestSetBuzzer(int enable) {
        return super.MtestSetBuzzer(enable);
    }

    public int MtestSetLedRed(int enable) {
        return super.MtestSetLedRed(enable);
    }

    public int MtestSetLedGreen(int enable) {
        return super.MtestSetLedGreen(enable);
    }

    public int MtestSetLedOrange(int enable) {
        return super.MtestSetLedOrange(enable);
    }

    public int MtestSetLedWhite(int enable) {
        return super.MtestSetLedWhite(enable);
    }

    public int MtestSetLedOnOff(int status) {
        return super.MtestSetLedOnOff(status);
    }

    public int MtestGetFrontKey(int key) {
        return super.MtestGetFrontKey(key);
    }

    public int MtestSetUsbPower(int enable) {
        return super.MtestSetUsbPower(enable);
    }

    public int MtestTestUsbReadWrite(int portNum, String path) {
        return super.MtestTestUsbReadWrite(portNum, path);
    }

    public int MtestTestAvMultiPlay(int tunerNum, List<Integer> tunerIDs, List<Long> channelIDs) {
        return super.MtestTestAvMultiPlay(tunerNum, tunerIDs, channelIDs);
    }

    public int MtestTestAvStopByTunerID(int tunerID) {
        return super.MtestTestAvStopByTunerID(tunerID);
    }

    public int MtestMicSetInputGain(int value) {
        return super.MtestMicSetInputGain(value);
    }

    public int MtestMicSetLRInputGain(int l_r, int value) {
        return super.MtestMicSetLRInputGain(l_r, value);
    }

    public int MtestMicSetAlcGain(int value) {
        return super.MtestMicSetAlcGain(value);
    }

    public int MtestGetErrorFrameCount(int tunerID) {
        return super.MtestGetErrorFrameCount(tunerID);
    }

    public int MtestGetFrameDropCount(int tunerID) {
        return super.MtestGetFrameDropCount(tunerID);
    }

    public String MtestGetChipID() {
        return super.MtestGetChipID();
    }

    public int MtestStartMtest(String version) {
        return super.MtestStartMtest(version);
    }

    public int MtestConnectPctool() {
        return super.MtestConnectPctool();
    }

    public List<Integer> MtestGetWiFiTxRxLevel(){//Scoty 20190417 add wifi level command
        return super.MtestGetWiFiTxRxLevel();
    }

    public int MtestGetWakeUpMode(){//Scoty 20190417 add wifi level command
        return super.MtestGetWakeUpMode();
    }

    public Map<String, Integer> MtestGetKeyStatusMap() {
        return super.MtestGetKeyStatusMap();
    }

    public int MtestEnableOpt(boolean enable) {
        return super.MtestEnableOpt(enable);
    }

    public List<SimpleChannel> MtestGetSimpleChannelListByTpID(int tpID) {
        List<SimpleChannel> simpleChannelList = ProgramInfoGetSimpleChannelList(ProgramInfo.ALL_TV_TYPE, 1, 1);
        if (simpleChannelList == null) {
            simpleChannelList = new ArrayList<>();
        }

        // find tpID's prog
        List<SimpleChannel> matchTpChannelList = new ArrayList<>();
        for (SimpleChannel simpleChannel : simpleChannelList) {
            if (simpleChannel.getTpId() == tpID) {
                matchTpChannelList.add(simpleChannel);
            }
        }

        return matchTpChannelList;
    }

    public int Record_Start_V2_with_Duration(long channelId, int durationSec, boolean doCipher, PVREncryption pvrEncryption)
    {
        if (Pvcfg.getPVR_PJ())
            return super.Record_Start_V2_with_Duration(channelId, durationSec, doCipher, pvrEncryption);
        else
            return HiDtvMediaPlayer.CMD_RETURN_VALUE_FAILAR;
    }

    public int Record_Start_V2_with_FileSize(long channelId, int fileSizeMB, boolean doCipher, PVREncryption pvrEncryption)
    {
        if (Pvcfg.getPVR_PJ())
            return super.Record_Start_V2_with_FileSize(channelId, fileSizeMB, doCipher, pvrEncryption);
        else
            return HiDtvMediaPlayer.CMD_RETURN_VALUE_FAILAR;
    }

    public int TimeShift_Start_V2(int durationSec, int fileSizeMB, boolean doCipher, PVREncryption pvrEncryption)
    {
        if (Pvcfg.getPVR_PJ())
            return super.TimeShift_Start_V2(durationSec, fileSizeMB, doCipher, pvrEncryption);
        else
            return HiDtvMediaPlayer.CMD_RETURN_VALUE_FAILAR;
    }

    public List<PvrFileInfo> Pvr_Get_Records_File(int startIndex, int total)
    {
        if (Pvcfg.getPVR_PJ())
            return super.Pvr_Get_Records_File(startIndex, total);
        else
            return new ArrayList<>();
    }

    public List<PvrFileInfo> Pvr_Get_Total_One_Series_Records_File(int startIndex, int total, String recordUniqueId)
    {
        if (Pvcfg.getPVR_PJ())
            return super.Pvr_Get_Total_One_Series_Records_File(startIndex, total, recordUniqueId);
        else
            return new ArrayList<>();
    }

    public int Pvr_Get_Total_Rec_Num()
    {
        if (Pvcfg.getPVR_PJ())
            return super.Pvr_Get_Total_Rec_Num();
        else
            return HiDtvMediaPlayer.CMD_RETURN_VALUE_FAILAR;
    }

    public int Pvr_Get_Total_One_Series_Rec_Num(String recordUniqueId)
    {
        if (Pvcfg.getPVR_PJ())
            return super.Pvr_Get_Total_One_Series_Rec_Num(recordUniqueId);
        else
            return HiDtvMediaPlayer.CMD_RETURN_VALUE_FAILAR;
    }

    public int Pvr_Delete_Total_Records_File()
    {
        if (Pvcfg.getPVR_PJ())
            return super.Pvr_Delete_Total_Records_File();
        else
            return HiDtvMediaPlayer.CMD_RETURN_VALUE_FAILAR;
    }

    public int Pvr_Delete_One_Series_Folder(String recordUniqueId)
    {
        if (Pvcfg.getPVR_PJ())
            return super.Pvr_Delete_One_Series_Folder(recordUniqueId);
        else
            return HiDtvMediaPlayer.CMD_RETURN_VALUE_FAILAR;
    }

    public int Pvr_Delete_Record_File_By_Ch_Id(int channelId)
    {
        if (Pvcfg.getPVR_PJ())
            return super.Pvr_Delete_Record_File_By_Ch_Id(channelId);
        else
            return HiDtvMediaPlayer.CMD_RETURN_VALUE_FAILAR;
    }

    public int Pvr_Delete_Record_File(String recordUniqueId)
    {
        if (Pvcfg.getPVR_PJ())
            return super.Pvr_Delete_Record_File(recordUniqueId);
        else
            return HiDtvMediaPlayer.CMD_RETURN_VALUE_FAILAR;
    }

    public int PvrRecordStart(int pvrPlayerID , long channelID, String recordPath, int duration)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
            return super.PvrRecordStart(pvrPlayerID, channelID, recordPath, duration);
        }else
            return -1;
    }

    public int PvrRecordStart(int pvrPlayerID , long channelID, String recordPath, int duration, PVREncryption pvrEncryption)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrRecordStart(pvrPlayerID, channelID, recordPath, duration, pvrEncryption);
        }else
            return -1;
    }

    public int PvrRecordStop(int pvrPlayerID, int recId)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
            return super.PvrRecordStop(pvrPlayerID, recId);
        }else
            return -1;
    }

    public int PvrRecordGetAlreadyRecTime(int pvrPlayerID, int recId)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
            return super.PvrRecordGetAlreadyRecTime(pvrPlayerID, recId);
        }else
            return -1;
    }

    public int PvrRecordGetStatus(int pvrPlayerID, int recId)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
            return super.PvrRecordGetStatus(pvrPlayerID, recId);
        }else
            return -1;
    }

    public int PvrTimeShiftStart(int playerID, int time, int filesize, String filePath)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
            return super.PvrTimeShiftStart(playerID, time, filesize, filePath);
        }else
            return -1;
    }

    public String PvrRecordGetFileFullPath(int pvrPlayerID, int recId)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
            return super.PvrRecordGetFileFullPath(pvrPlayerID, recId);
        }else
            return null;
    }

    public int PvrRecordGetProgramId(int pvrPlayerID, int recId)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
            return super.PvrRecordGetProgramId(pvrPlayerID, recId);
        }else
            return -1;
    }

    public int PvrRecordCheck(long channelID)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
            return super.PvrRecordCheck(channelID);
        }else
            return -1;
    }

    public List<PvrInfo> PvrRecordGetAllInfo()
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
            return super.PvrRecordGetAllInfo();
        }else
            return null;
    }

    public int PvrRecordGetMaxRecNum()
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
            return super.PvrRecordGetMaxRecNum();
        }else
            return 0;
    }

    public int PvrPlayFileCheckLastViewPoint(String fullName)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
            return super.PvrPlayFileCheckLastViewPoint(fullName);
        }else
            return 0;
    }

    public int PvrSetStartPositionFlag(int startPositionFlag)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
            return super.PvrSetStartPositionFlag(startPositionFlag);
        }else
            return 0;
    }

    public int PvrTimeShiftStop(int playerID)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrTimeShiftStop(playerID);
        }else
            return -1;
    }

    public int PvrTimeShiftPlay(int playerID)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrTimeShiftPlay(playerID);
        }else
            return -1;
    }

    public int PvrTimeShiftResume(int playerID)//Scoty 20181106 add for separate Play and Pause key
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
            return super.pvrTimeShiftResume(playerID);
        }else
            return -1;
    }

    public int PvrTimeShiftPause(int playerID)//Scoty 20181106 add for separate Play and Pause key
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
            return super.pvrTimeShiftPause(playerID);
        }else
            return -1;
    }

    public int pvrTimeShiftLivePause(int playerID)//Scoty 20180827 add and modify TimeShift Live Mode
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
            return super.pvrTimeShiftLivePause(playerID);
        }else
            return -1;
    }

    public int pvrTimeShiftFilePause(int playerID)//Scoty 20180827 add and modify TimeShift Live Mode
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.pvrTimeShiftFilePause(playerID);
        }else
            return -1;
    }

    public int PvrTimeShiftTrickPlay(int playerID, EnTrickMode mode)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrTimeShiftTrickPlay(playerID, mode);
        }else
            return -1;
    }

    public int PvrTimeShiftSeekPlay(int playerID, int seekSec)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrTimeShiftSeekPlay(playerID, seekSec);
        }else
            return -1;
    }

    public Date PvrTimeShiftGetPlayedTime(int playerID)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrTimeShiftGetPlayedTime(playerID);
        }else
            return null;
    }

    public int PvrTimeShiftGetPlaySecond(int playerID)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrTimeShiftGetPlaySecond(playerID);
        }else
            return -1;
    }

    public Date PvrTimeShiftGetBeginTime(int playerID)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrTimeShiftGetBeginTime(playerID);
        }else
            return null;
    }

    public int PvrTimeShiftGetBeginSecond(int playerID)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrTimeShiftGetBeginSecond(playerID);
        }else
            return -1;
    }

    public int PvrTimeShiftGetRecordTime(int playerID)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrTimeShiftGetRecordTime(playerID);
        }else
            return -1;
    }

    public int PvrTimeShiftGetStatus(int playerID)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrTimeShiftGetStatus(playerID);
        }else
            return -1;
    }

    public EnTrickMode PvrTimeShiftGetCurrentTrickMode(int playerID)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrTimeShiftGetCurrentTrickMode(playerID);
        }else
            return EnTrickMode.FAST_FORWARD_NORMAL;
    }

    public int PvrPlayStart(String filePath)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrPlayStart(filePath);
        }else
            return -1;
    }

    public int PvrPlayStart(String filePath, PVREncryption pvrEncryption)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrPlayStart(filePath, pvrEncryption);
        }else
            return -1;
    }

    public int PvrPlayStop()
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrPlayStop();
        }else
            return -1;
    }

    public int PvrPlayPause()
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrPlayPause();
        }else
            return -1;
    }

    public int PvrPlayResume()
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrPlayResume();
        }else
            return -1;
    }

    public int PvrPlayTrickPlay(EnTrickMode enSpeed)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrPlayTrickPlay(enSpeed);
        }else
            return -1;
    }

    public int PvrPlaySeekTo(int sec)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrPlaySeekTo(sec);
        }else
            return -1;
    }

    public int PvrPlayGetPlayTime()
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrPlayGetPlayTime();
        }else
            return -1;
    }

    public EnTrickMode PvrPlayGetCurrentTrickMode()
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrPlayGetCurrentTrickMode();
        }else
            return EnTrickMode.FAST_FORWARD_NORMAL;
    }

    public int PvrPlayGetCurrentStatus()
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrPlayGetCurrentStatus();
        }else
            return -1;
    }

    public long PvrPlayGetSize()
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrPlayGetSize();
        }else
            return -1;
    }

    public int PvrPlayGetDuration()
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrPlayGetDuration();
        }else
            return -1;
    }

    public Resolution PvrPlayGetVideoResolution()
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrPlayGetVideoResolution();
        }else
            return null;
    }

    public boolean PvrPlayIsRadio(String fullName)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
            return super.PvrPlayIsRadio(fullName);
        }else
            return false;
    }

    public String PvrPlayGetFileFullPath(int pvrPlayerID)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrPlayGetFileFullPath(pvrPlayerID);
        }else
            return null;
    }

    public AudioInfo.AudioComponent PvrPlayGetCurrentAudio()
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrPlayGetCurrentAudio();
        }else
            return null;
    }

    public AudioInfo PvrPlayGetAudioComponents()
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrPlayGetAudioComponents();
        }else
            return null;
    }

    public int PvrPlaySelectAudio(AudioInfo.AudioComponent audio)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrPlaySelectAudio(audio);
        }else
            return -1;
    }

    public int PvrPlaySetWindowRect(Rect rect)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrPlaySetWindowRect(rect);
        }else
            return -1;
    }

    public int PvrPlaySetTrackMode(EnAudioTrackMode enTrackMode)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrPlaySetTrackMode(enTrackMode);
        }else
            return -1;
    }

    public EnAudioTrackMode PvrPlayGetTrackMode()
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrPlayGetTrackMode();
        }else
            return EnAudioTrackMode.MPEG_AUDIO_TRACK_STEREO;
    }

    public int PvrFileRemove(String filePath)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrFileRemove(filePath);
        }else
            return -1;
    }

    public int PvrFileRename(String oldName, String newName)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrFileRename(oldName, newName);
        }else
            return -1;

    }

    public int PvrFileGetDuration(String fullName) {
        if (Pvcfg.getPVR_PJ() == true) {//eric lin 20180703 add pvcfg
        return super.PvrFileGetDuration(fullName);
        }else
            return -1;
    }

    public long PvrFileGetSize(String fullName)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrFileGetSize(fullName);
        }else
            return 0;
    }

    public PvrFileInfo PvrFileGetAllInfo(String fullName)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
            return super.PvrFileGetAllInfo(fullName);
        }else
        {
            PvrFileInfo pvrFileInfo = new PvrFileInfo();
            pvrFileInfo.startTimeInMs = 0;    // startTime in ms
            pvrFileInfo.endTimeInMs = 0;      // endTime in ms
            pvrFileInfo.durationInMs = 0;     // duration in ms
            pvrFileInfo.fileSize = 0;        // file size
            pvrFileInfo.serviceType = 0;      // service type
            pvrFileInfo.channelLock = 0;      // channel lock
            pvrFileInfo.parentalRate = 0;      // parental lock
            return pvrFileInfo;
        }
    }

    public PvrFileInfo PvrFileGetExtraInfo(String fullName)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
            return super.PvrFileGetExtraInfo(fullName);
        }else
        {
            PvrFileInfo pvrFileInfo = new PvrFileInfo();
            pvrFileInfo.channelName = null;
            pvrFileInfo.year = 0;
            pvrFileInfo.month = 0;
            pvrFileInfo.date = 0;
            pvrFileInfo.week = 0;
            pvrFileInfo.hour = 0;
            pvrFileInfo.minute = 0;
            pvrFileInfo.second = 0;
            return pvrFileInfo;
        }
    }

    public PvrFileInfo PvrFileGetEpgInfo ( String fullName, int epgIndex )
    {
        if ( Pvcfg.getPVR_PJ() )
        {
            return super.pvrFileGetEpgInfo( fullName, epgIndex );
        }
        else
        {
            PvrFileInfo fileInfo = new PvrFileInfo();
            fileInfo.eventName      = "";
            fileInfo.shortEvent     = "";
            fileInfo.extendedText   = "";
            fileInfo.languageCode   = "";
            fileInfo.extendedLanguageCode = "";

            fileInfo.year = 0;
            fileInfo.month = 0;
            fileInfo.date = 0;
            fileInfo.hour = 0;
            fileInfo.minute = 0;
            fileInfo.second = 0;
            fileInfo.week = 0;
            fileInfo.yearEnd = 0;
            fileInfo.monthEnd = 0;
            fileInfo.dateEnd = 0;
            fileInfo.hourEnd = 0;
            fileInfo.monthEnd = 0;
            fileInfo.secondEnd = 0;
            fileInfo.weekEnd = 0;

            fileInfo.parentalRate       = 0;
            fileInfo.eventNameCharCode  = 0;
            fileInfo.shortEventCharCode = 0;
            fileInfo.recordTimeStamp    = 0;
            return fileInfo;
        }
    }

    public int PvrGetCurrentPvrMode(long channelId)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
            return super.PvrGetCurrentPvrMode(channelId);
        }else
            return -1;
    }

    public int PvrSetParentLockOK()  //connie 20180806 for pvr parentalRate
    {
        if(Pvcfg.getPVR_PJ()==true) {
            return super.PvrSetParentLockOK();
        }else
            return -1;
    }

    // edwin 20180809 add PvrTotalRecordFileXXX -s
    public int PvrTotalRecordFileOpen(String dirPath)
    {
        if(Pvcfg.getPVR_PJ()==true)
        {
            return super.pvrTotalRecordFileOpen(dirPath);
        }
        else
        {
            return -1;
        }
    }

    public int PvrTotalRecordFileClose()
    {
        if(Pvcfg.getPVR_PJ()==true)
        {
            return super.pvrTotalRecordFileClose();
        }
        else
        {
            return -1;
        }
    }

    public int PvrTotalRecordFileSort(int sortType)
    {
        if(Pvcfg.getPVR_PJ()==true)
        {
            // PVR_SORT_BY_CHNAME	=0,
            // PVR_SORT_BY_DATETIME	=1,
            // PVR_SORT_BUTT
            return super.pvrTotalRecordFileSort(sortType);
        }
        else
        {
            return -1;
        }
    }

    public List<PvrFileInfo> PvrTotalRecordFileGet(int startIndex, int total)
    {
        if(Pvcfg.getPVR_PJ()==true)
        {
            return super.pvrTotalRecordFileGet(startIndex, total);
        }
        else
        {
            return null;
        }
    }
    public int pvrCheckHardDiskOpen(String FilePath)//Scoty 20180827 add HDD Ready command and callback
    {
        return super.pvrCheckHardDiskOpen(FilePath);
    }

    public int pvrPlayTimeShiftStop()//Scoty 20180827 add and modify TimeShift Live Mode
    {
        return super.pvrPlayTimeShiftStop();
    }

    public int pvrRecordGetLivePauseTime()//Scoty 20180827 add and modify TimeShift Live Mode
    {
        return super.pvrRecordGetLivePauseTime();
    }
    // edwin 20180809 add PvrTotalRecordFileXXX -e

//    public int PvrGetCurrentRecMode()
//    {
//        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
//        return super.PvrGetCurrentRecMode();
//        }else
//            return -1;
//    }

//    public int PvrGetCurrentPlayMode()
//    {
//        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
//        return super.PvrGetCurrentPlayMode();
//        }else
//            return -1;
//    }

    public int PvrGetRatio()
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        return super.PvrGetRatio();
        }else
            return -1;
    }

    public void ResetTotalChannelList() {
        super.ResetTotalChannelList();
    }

    //PIP -start
    public int PipOpen(int x, int y, int width, int height)
    {
        return super.PipOpen(x,y,width,height);
    }

    public int PipClose()
    {
        return super.PipClose();
    }

    public int PipStart(long channelId, int show)
    {
        return super.PipStart(channelId,show);
    }

    public int PipStop()
    {
        return super.PipStop();
    }

    public int PipSetWindow(int x, int y, int width, int height)
    {
        return super.PipSetWindow(x,y,width,height);
    }

    public int PipExChange()
    {
        return super.PipExChange();
    }
    //PIP -end

    //Device -start
    public List<Integer> GetUsbPortList()
    {
        return super.GetUsbPortList();
    }
    //Device -ecd

    private void SubBroadcast(){
        IntentFilter usbIntentFilter = new IntentFilter();
        usbIntentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        usbIntentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        usbIntentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        usbIntentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        usbIntentFilter.addDataScheme("file");
        registerReceiver(mUsbRemoveBroadCastReceiver, usbIntentFilter);
    }

    private void unSubBroadcast(){
        unregisterReceiver(mUsbRemoveBroadCastReceiver);
        mUsbRemoveBroadCastReceiver = null;
    }

    public void InitRecordPath() // edwin 20180814 init record path // connie 20181024 for USB Path wrong
    {
        setRecPath();
    }

    // edwin 20200513 add none server function -s
    private void setRecPath ()
    {
        // edwin 20210113 fix cannot detect USB devices
        StorageManager mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        PesiStorageHelper helper = new PesiStorageHelper(mStorageManager);
        List<Object> volList = getVolumes(); // Edwin 20181205 simplify getVolumes()

        if (isSameRecPath())
            Log.d(TAG, "setRecPath: same rec path");
        else
        {
            for (Object vol : volList)
            {
                String internalPath = helper.getInternalPath(vol);
                if (helper.isUsb(vol))
                {
                    SetRecordPath(internalPath);
                    SaveTable(EnTableType.GPOS);
                    Log.d(TAG, "setRecPath: is USB , curPath = " + GetRecordPath() + " , internalPath = " + internalPath);
                    return;
                }
            }

            SetRecordPath(getDefaultRecPath());
            SaveTable(EnTableType.GPOS);
            Log.d(TAG, "setRecPath: not USB , curPath = " + GetRecordPath());
        }
    }

    private boolean isSameRecPath ()
    {
        StorageManager mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        PesiStorageHelper helper = new PesiStorageHelper(mStorageManager);
        String curRecPath = GetRecordPath();

        for ( Object vol : getVolumes() )
        {
            String internalPath = helper.getInternalPath(vol);
            if (helper.isUsb(vol) && curRecPath.equals(internalPath))
            {
                Log.d(TAG, "isSameRecPath: path unchanged,  curPath = " + curRecPath);
                return true;
            }
        }
        return false;
    }
    // edwin 20200513 add none server function -e

    private BroadcastReceiver mUsbRemoveBroadCastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String uMountPath = intent.getData().getPath();
            String action = intent.getAction();
            String curRecInternalPath = GetRecordPath();


            Log.d(TAG, "action = " + action + "  uMountPath = " + uMountPath);
            if (action == null)
            {
                return;
            }

            List<Object> storageVolumes = getVolumes(); // Edwin 20181205 simplify getVolumns()

            if (action.equals(Intent.ACTION_MEDIA_EJECT)) // prepare to remove
            {
                usbEject(storageVolumes, uMountPath, curRecInternalPath);
            }

            if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED))
            {
                InitRecordPath(); // connie 20181024 for USB Path wrong
            }

            if(action.equals(Intent.ACTION_MEDIA_MOUNTED))
            {
                usbMount(storageVolumes, action, uMountPath);
            }
        }
    };

    private void usbMount(List<Object> storageVolumes, String action, String uMountPath)
    {
        // edwin 20210113 fix cannot detect USB devices
        StorageManager mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        Log.d(TAG, "usbMount: action = " + action + "  uMountPath = " + uMountPath);
        PesiStorageHelper pesiStorageHelper = new PesiStorageHelper(mStorageManager);

        for (final Object vol : storageVolumes)
        {
            boolean isUsb = pesiStorageHelper.isUsb(vol);
            String usbPath = pesiStorageHelper.getPath(vol);
            Log.d(TAG,"usbMount: isUsb = " + isUsb + " , path = "+usbPath);

            if (isUsb && usbPath.equals(uMountPath) )
            {
                InitRecordPath(); // connie 20181024 for USB Path wrong

                MessageDialogView UsbDialog = MessageDialogView.GetMessageDialogView();
                String UsbMsg = getString(R.string.STR_USB_DISK_READY);
                if (UsbDialog == null || !UsbDialog.isShowing())
                {
                    if(UsbDialog == null ) {
                        UsbDialog = new MessageDialogView(DTVActivity.this
                                , UsbMsg
                                , getResources().getInteger(R.integer.MESSAGE_DIALOG_DELAY)) {
                            public void dialogEnd() {
                            }
                        };
                        MessageDialogView.SetMessageInstance(UsbDialog);
                    }
                    else {
                        UsbDialog.setText(UsbMsg); //if previous use unmount msg, need to change to mount
                    }
                    UsbDialog.show();//casper20200817 RLK mtest don't show dialog
                }

                /*
                if (usbMountDialog == null || !usbMountDialog.isShowing())
                {
                    usbMountDialog = new MessageDialogView(DTVActivity.this
                            , getString(R.string.STR_USB_DISK_READY)
                            , getResources().getInteger(R.integer.MESSAGE_DIALOG_DELAY))
                    {
                        public void dialogEnd() {
                        }
                    };
                    usbMountDialog.show();//casper20200817 RLK mtest don't show dialog
                }
                 */
            }
        }
    }

    private void usbEject (List<Object> storageVolumes, String uMountPath, String curRecPath)
    {
        // edwin 20210113 fix cannot detect USB devices
        StorageManager mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        PesiStorageHelper helper = new PesiStorageHelper(mStorageManager);
        Object ejectVol = null;

        for (Object vol : storageVolumes)
        {
            String path = helper.getPath(vol);

            if (path.equals(uMountPath))
            {
                Log.d(TAG, "usbEject: path = "+path);
                ejectVol = vol;
                break;
            }
        }

        final boolean isCurRecordPath = (ejectVol != null && helper.getInternalPath(ejectVol).equals(curRecPath));

        //Scoty 20180809 modify dual pvr rule -s
        // stop all recordings
        if(isCurRecordPath)
        {
            List<PvrInfo> pvrList = PvrRecordGetAllInfo();
            if(pvrList.size() > 0)
            {
                Log.d(TAG, "usbEject: remove record USB");
                for (int i = 0; i < pvrList.size(); i++)
                {
                    int recId = pvrList.get(i).getRecId();
                    int pvrMod = pvrList.get(i).getPvrMode();
                    stopPVR(pvrMod, recId);//eric lin 20180712 pesi pvr for one rec
                }
                TVMessage msg = TVMessage.SetRemoveUsbStopRec();//Scoty 20180802 add message when recording and then remove usb not clean rec icon
                onMessage(msg);//Scoty 20180802 add message when recording and then remove usb not clean rec icon
            }
        }
        //Scoty 20180809 modify dual pvr rule -e

        // show STR_USB_DISK_NOT_READY msg & close RecordListActivity if is curRecordPath

        boolean isUSBPort = (ejectVol != null && helper.isUsb(ejectVol));

        MessageDialogView UsbDialog = MessageDialogView.GetMessageDialogView();
        if(isUSBPort && (UsbDialog == null || !UsbDialog.isShowing()))
        {
            String usbMsgStr = getString(R.string.STR_USB_DISK_NOT_READY); //USB Device Not Ready Msg
            if(UsbDialog == null) {
                UsbDialog = new MessageDialogView(DTVActivity.this
                        , usbMsgStr
                        , getResources().getInteger(R.integer.MESSAGE_DIALOG_DELAY)) {
                    public void dialogEnd() {
                        Log.d(TAG, "usbEject: getClassName = " + getComponentName().getClassName()
                                + " , name = " + RecordListActivity.class.getName());
                        if (getComponentName().getClassName().equals(RecordListActivity.class.getName())
                                && isCurRecordPath) // Johnny 20180802 finish Record List when unplug using usb
                        {
                            finish();
                        }
                    }
                };
                MessageDialogView.SetMessageInstance(UsbDialog);
            }
            else {
                UsbDialog.setText(usbMsgStr);//if previous use mount msg, need to change to unmount
            }
            UsbDialog.show();
        }
/*
        if (isUSBPort && (usbUnMountDialog == null || !usbUnMountDialog.isShowing()))
        {
            usbUnMountDialog = new MessageDialogView(DTVActivity.this
                    , getString(R.string.STR_USB_DISK_NOT_READY)
                    , getResources().getInteger(R.integer.MESSAGE_DIALOG_DELAY))
            {
                public void dialogEnd()
                {
                    Log.d(TAG, "usbEject: getClassName = " + getComponentName().getClassName()
                            + " , name = " + RecordListActivity.class.getName());
                    if (getComponentName().getClassName().equals(RecordListActivity.class.getName())
                            && isCurRecordPath) // Johnny 20180802 finish Record List when unplug using usb
                    {
                        finish();
                    }
                }
            };
            usbUnMountDialog.show();//casper20200817 RLK mtest don't show dialog
        }*/
    }

    public String GetRecordPath()
    {
        return super.GetRecordPath();
    }

    public void SetRecordPath(String path)
    {
        super.SetRecordPath(path);
    }

    public boolean CheckUsbPathAvailable(String path) // connie 20181024 for USB Path wrong
    {
        String defaultPath = getDefaultRecPath();
        Log.d(TAG, "CheckUsbPathAvailable:  defaultPath = " + defaultPath);
        Log.d(TAG, "CheckUsbPathAvailable: path ="+ path);
        if(defaultPath.equals(path))
            return false;
        else
            return true;
    }

    public void stopPVR( int pvrMode, int recId)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
            //Scoty 20180716 fixed timeshft/record not stop -s
            if(CheckPvrMode(pvrMode))//TimeShift //Scoty 20180827 add and modify TimeShift Live Mode
            {
                Log.d(TAG, "stopPVR: TIMESHIFT");
                PvrTimeShiftStop(ViewHistory.getPlayId());
            }
            else if (pvrMode == PvrInfo.EnPVRMode.RECORD) {
                PvrRecordStop(ViewHistory.getPlayId(), recId);
                UpdatePvrSkipList(ViewHistory.getCurGroupType(), 0, 0,null);//Scoty 20180809 modify dual pvr rule
            }

            //Scoty 20180716 fixed timeshft/record not stop -e
        }
    }

    //Scoty 20180801 add for check all recording is exist or not
    public boolean IsRecExist()
    {
        boolean PvrSkipFlag = false;
        List<PvrInfo> pvrList= new ArrayList<PvrInfo>();
        pvrList = PvrRecordGetAllInfo();
        for(int i = 0 ; i < pvrList.size() ; i++)
        {
            if(pvrList.get(i).getPvrMode() == PvrInfo.EnPVRMode.RECORD)
            {
                PvrSkipFlag = true;
                break;
            }
        }
        return PvrSkipFlag;
    }

    // get service define (AvSetRatio) by index of string.xml
    public int GetRatioByIndex(int index)
    {
        int ratio;
        if(index == 0)
            ratio = AvSetRatio.ASPECT_RATIO_16TO9.getValue();
        else
            ratio = AvSetRatio.ASPECT_RATIO_4TO3.getValue();
        return ratio;
    }

    public String getDefaultRecPath()//Scoty 20180525 add get default record path
    {
        return super.getDefaultRecPath();
    }

    public List<Object> getVolumes() // Edwin 20181205 get storage device list
    {
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        PesiStorageHelper pesiStorageHelper = new PesiStorageHelper(storageManager);
        return pesiStorageHelper.getVolumes();
    }

    public void TestSetTvRadioCount(int tvCount, int RadioCount)
    {
        super.TestSetTvRadioCount(tvCount,RadioCount);
    }

    public int TestChangeTuner(int tunerType)//Scoty 20180817 add Change Tuner Command
    {
        return super.TestChangeTuner(tunerType);
    }

    // Johnny 20180530 move from timer and epg reserve -s
    public Date GetDateOfNextDaily(Date oriDate)
    {
        Date dateOfNextDaily = new Date(oriDate.getTime());
        Date now = new Date();//GetLocalTime();
        now.setTime(GetLocalTime().getTime()+60*1000);

        int dayFromOri;
        Log.d(TAG, "getDateOfNextDaily: " + oriDate);
        Log.d(TAG, "getDateOfNextDaily: " + now);
        if (oriDate.after(now)) // booktime is after now
        {
            dayFromOri = 0;
        }
        else    // booktime is before now
        {
            dayFromOri = 1;
        }

        long nextDaily = oriDate.getTime() + dayFromOri*24*3600*1000; // day to milisec
        dateOfNextDaily.setTime(nextDaily);

        Log.d(TAG, "getDateOfNextDaily: " + dateOfNextDaily);
        return dateOfNextDaily;
    }

    public Date GetDateOfNextWeekly(Date oriDate, int selectWeek)
    {
        Date dateOfNextWeekly = new Date(oriDate.getTime());
        Date now = new Date();//GetLocalTime();
        now.setTime(GetLocalTime().getTime()+60*1000);
        SimpleDateFormat sdf = new SimpleDateFormat("u", Locale.getDefault());
        int week = Integer.valueOf(sdf.format(oriDate))-1;//get weekday => 0~6 Monday~Sunday
        int dayFromOri;

        int closestSelectWeekFromOri = selectWeek - week;
        oriDate.setTime(oriDate.getTime() + closestSelectWeekFromOri*24*3600*1000);

        if (oriDate.after(now)) // booktime is after now
        {
            dayFromOri = 0;
        }
        else    // booktime is before now
        {
            dayFromOri = 7;
        }

        long nextWeekly = oriDate.getTime() + dayFromOri*24*3600*1000; // day to milisec
        dateOfNextWeekly.setTime(nextWeekly);

        Log.d(TAG, "getDateOfNextWeekly: " + dateOfNextWeekly);
        return dateOfNextWeekly;
    }

    public Date GetDateOfNextWeekEnd(Date oriDate)
    {
        Date dateOfNextWeekEnd = new Date(oriDate.getTime());
        Date now = new Date();//GetLocalTime();
        now.setTime(GetLocalTime().getTime()+60*1000);
        SimpleDateFormat sdf = new SimpleDateFormat("u", Locale.getDefault());
        int week = Integer.valueOf(sdf.format(oriDate))-1;//get weekday => 0~6 Monday~Sunday
        int dayFromOri;
        if (oriDate.after(now)) // booktime is after now
        {
            if (week == 5 || week == 6)  // saturday or sunday
            {
                dayFromOri = 0;
            }
            else    // monday~friday
            {
                dayFromOri = 5 - week;
            }
        }
        else    // booktime is before now
        {
            if (week == 5)  // saturday
            {
                dayFromOri = 1;
            }
            else if (week == 6) // sunday
            {
                dayFromOri = 6;
            }
            else    // monday~friday
            {
                dayFromOri = 5 - week;
            }
        }

        long nextWeekend = oriDate.getTime() + dayFromOri*24*3600*1000; // day to milisec
        dateOfNextWeekEnd.setTime(nextWeekend);

        Log.d(TAG, "getDateOfNextWeekEnd: " + dateOfNextWeekEnd);
        return dateOfNextWeekEnd;
    }

    public Date GetDateOfNextWeekDays(Date oriDate)
    {
        Date dateOfNextWeekDays = new Date(oriDate.getTime());
        Date now = new Date();//GetLocalTime();
        now.setTime(GetLocalTime().getTime()+60*1000);
        SimpleDateFormat sdf = new SimpleDateFormat("u", Locale.getDefault());
        int week = Integer.valueOf(sdf.format(oriDate))-1;//get weekday => 0~6 Monday~Sunday
        int dayFromOri;
        if (oriDate.after(now)) // booktime is after now
        {
            if (week == 5 || week == 6)  // saturday or sunday
            {
                dayFromOri = 7 - week;
            }
            else    // monday~friday
            {
                dayFromOri = 0;
            }
        }
        else    // booktime is before now
        {
            if (week >= 4)  // friday~sunday
            {
                dayFromOri = 7 - week;
            }
            else    // monday~thursday
            {
                dayFromOri = 1;
            }
        }

        long nextWeekDays = oriDate.getTime() + dayFromOri*24*3600*1000; // day to milisec
        dateOfNextWeekDays.setTime(nextWeekDays);

        Log.d(TAG, "getDateOfNextWeekDays: " + dateOfNextWeekDays);
        return dateOfNextWeekDays;
    }
    // Johnny 20180530 move from timer and epg reserve -e

    //Scoty 20180613 add Help Text Split function
    public SpannableStringBuilder SetSplitText(String string)
    {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        //String string = mContext.getString(R.string.STR_OKLIST_HELP_OK_SELECT);
        final String[] textsplit = string.split(getString(R.string.STR_HELP_FILTER_SPACE));
        if(!string.isEmpty()) {
        for (String splitText : textsplit) {
            SpannableString Spannable;
            if (splitText.equals(getString(R.string.STR_HELP_FILTER_LEFT_RIGHT))
                    || splitText.equals(getString(R.string.STR_HELP_FILTER_OK))
                    || splitText.equals(getString(R.string.STR_HELP_FILTER_EXIT))
                    || splitText.equals(getString(R.string.STR_HELP_FILTER_INFO))
                    || splitText.equals(getString(R.string.STR_HELP_FILTER_BACK))
                    || splitText.equals(getString(R.string.STR_HELP_FILTER_UP_DOWN))
                    || splitText.equals(getString(R.string.STR_HELP_FILTER_ZERO_TO_NINE))
                    || splitText.equals(getString(R.string.STR_HELP_FILTER_NEXTLINE_LEFT_RIGHT))
                    || splitText.equals(getString(R.string.STR_HELP_FILTER_OK_KEY))
                    || splitText.equals(getString(R.string.STR_HELP_FILTER_PAGE_UP_DOWN))
                    || splitText.equals(getString(R.string.STR_HELP_FILTER_PAGE_UP_DOWN_NEXT))) {
                //Log.d(TAG, "setHelpInfoTextBySplit: ==>> yellow " + "[" + splitText + "]");
                splitText += " ";
                Spannable = new SpannableString(splitText);
                Spannable.setSpan(new ForegroundColorSpan(Color.argb(255,69,146,207)), 0, splitText.length(), 0);
            } else {
                //Log.d(TAG, "setHelpInfoTextBySplit: ==>> white " + "[" + splitText + "]");
                splitText += " ";
                Spannable = new SpannableString(splitText);
                Spannable.setSpan(new ForegroundColorSpan(Color.WHITE), 0, splitText.length(), 0);
            }
            builder.append(Spannable);
        }
        return builder;
        }else
            return null;
    }

    //Scoty 20180613 add pvr rewind/fast forward function -s
    public EnTrickMode PvrSetRewindTrickMode(EnTrickMode FBtrickMode)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        //FF
            if (FBtrickMode == EnTrickMode.FAST_FORWARD_THIRTYTWO) {
            FBtrickMode = EnTrickMode.FAST_BACKWARD_TWO;
            } else if (FBtrickMode == EnTrickMode.FAST_FORWARD_SIXTEEN) {
            FBtrickMode = EnTrickMode.FAST_BACKWARD_TWO;
            } else if (FBtrickMode == EnTrickMode.FAST_FORWARD_EIGHT) {
            FBtrickMode = EnTrickMode.FAST_BACKWARD_TWO;
            } else if (FBtrickMode == EnTrickMode.FAST_FORWARD_FOUR) {
            FBtrickMode = EnTrickMode.FAST_BACKWARD_TWO;
            } else if (FBtrickMode == EnTrickMode.FAST_FORWARD_TWO) {
            FBtrickMode = EnTrickMode.FAST_BACKWARD_TWO;
            } else if (FBtrickMode == EnTrickMode.FAST_FORWARD_NORMAL) {
            FBtrickMode = EnTrickMode.FAST_BACKWARD_TWO;
        }
        //FB
            else if (FBtrickMode == EnTrickMode.FAST_BACKWARD_NORMAL) {
            FBtrickMode = EnTrickMode.FAST_BACKWARD_TWO;
            } else if (FBtrickMode == EnTrickMode.FAST_BACKWARD_TWO) {
            FBtrickMode = EnTrickMode.FAST_BACKWARD_FOUR;
            } else if (FBtrickMode == EnTrickMode.FAST_BACKWARD_FOUR) {
            FBtrickMode = EnTrickMode.FAST_BACKWARD_EIGHT;
            } else if (FBtrickMode == EnTrickMode.FAST_BACKWARD_EIGHT) {
            FBtrickMode = EnTrickMode.FAST_BACKWARD_SIXTEEN;
            } else if (FBtrickMode == EnTrickMode.FAST_BACKWARD_SIXTEEN) {
            FBtrickMode = EnTrickMode.FAST_BACKWARD_THIRTYTWO;
            } else if (FBtrickMode == EnTrickMode.FAST_BACKWARD_THIRTYTWO) {
            FBtrickMode = EnTrickMode.FAST_BACKWARD_TWO;
            } else
            FBtrickMode = EnTrickMode.FAST_BACKWARD_TWO;

        return FBtrickMode;
        }else
            return EnTrickMode.FAST_FORWARD_NORMAL;
    }

    public EnTrickMode PvrSetFastForwardTrickMode(EnTrickMode FFtrickMode)
    {
        if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
        //FF
            if (FFtrickMode == EnTrickMode.FAST_FORWARD_NORMAL) {
            FFtrickMode = EnTrickMode.FAST_FORWARD_TWO;
            } else if (FFtrickMode == EnTrickMode.FAST_FORWARD_TWO) {
            FFtrickMode = EnTrickMode.FAST_FORWARD_FOUR;
            } else if (FFtrickMode == EnTrickMode.FAST_FORWARD_FOUR) {
            FFtrickMode = EnTrickMode.FAST_FORWARD_EIGHT;
            } else if (FFtrickMode == EnTrickMode.FAST_FORWARD_EIGHT) {
            FFtrickMode = EnTrickMode.FAST_FORWARD_SIXTEEN;
            } else if (FFtrickMode == EnTrickMode.FAST_FORWARD_SIXTEEN) {
            FFtrickMode = EnTrickMode.FAST_FORWARD_THIRTYTWO;
            } else if (FFtrickMode == EnTrickMode.FAST_FORWARD_THIRTYTWO) {
            FFtrickMode = EnTrickMode.FAST_FORWARD_TWO;
        }
        //FB
            else if (FFtrickMode == EnTrickMode.FAST_BACKWARD_SIXTYFOUR) {
            FFtrickMode = EnTrickMode.FAST_FORWARD_TWO;
            } else if (FFtrickMode == EnTrickMode.FAST_BACKWARD_THIRTYTWO) {
            FFtrickMode = EnTrickMode.FAST_FORWARD_TWO;
            } else if (FFtrickMode == EnTrickMode.FAST_BACKWARD_SIXTEEN) {
            FFtrickMode = EnTrickMode.FAST_FORWARD_TWO;
            } else if (FFtrickMode == EnTrickMode.FAST_BACKWARD_EIGHT) {
            FFtrickMode = EnTrickMode.FAST_FORWARD_TWO;
            } else if (FFtrickMode == EnTrickMode.FAST_BACKWARD_FOUR) {
            FFtrickMode = EnTrickMode.FAST_FORWARD_TWO;
            } else if (FFtrickMode == EnTrickMode.FAST_BACKWARD_TWO) {
            FFtrickMode = EnTrickMode.FAST_FORWARD_TWO;
            } else
            FFtrickMode = EnTrickMode.FAST_FORWARD_TWO;

        return FFtrickMode;
        }else
            return EnTrickMode.FAST_FORWARD_NORMAL;
    }
    //Scoty 20180613 add pvr rewind/fast forward function -e

    public int getCurrentPvrMode(){
        int pvrMode;
        if(ViewHistory.getCurChannel() != null)
            pvrMode = PvrGetCurrentPvrMode(ViewHistory.getCurChannel().getChannelId());
        else
            pvrMode = PvrGetCurrentPvrMode(0);
        return pvrMode;
    }

    //Scoty 20180801 modify focus on Pip should show Pip information
    public int getCurrentPipPvrMode(){
        int pvrMode;
        if(ViewHistory.getCurPipChannel() != null)
            pvrMode = PvrGetCurrentPvrMode(ViewHistory.getCurPipChannel().getChannelId());
        else
            pvrMode = PvrGetCurrentPvrMode(0);
        return pvrMode;
    }

    public boolean isRecord(){
        List<PvrInfo> pvrList= new ArrayList<PvrInfo> ();
        pvrList = PvrRecordGetAllInfo();
        if (pvrList != null){
            for(int i=0; i< pvrList.size(); i++){
                if(pvrList.get(i).getPvrMode() == PvrInfo.EnPVRMode.RECORD){//Scoty 20180725 fixed oklist focus wrong when recording
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isRecord(List<PvrInfo> pvrList){
        if (pvrList != null){
            for(int i=0; i< pvrList.size(); i++){
                if(pvrList.get(i).getPvrMode() == PvrInfo.EnPVRMode.RECORD){//Scoty 20180725 fixed oklist focus wrong when recording
                    return true;
                }
            }
        }
        return false;
    }

    //type: 0 means all tv/radio, 1 means all tv, 2 means all radio
    public boolean checkProgramExist(int type){//eric lin 20180802 check program exist
        if(type == 0) {//check all tv/radio
            if(ProgramInfoGetPlaySimpleChannelListCnt(ProgramInfo.ALL_TV_TYPE) != 0) {
                Log.d(TAG, "checkProgramExist: type=0 tv cnt !=0");
                return true;
            }
            if(ProgramInfoGetPlaySimpleChannelListCnt(ProgramInfo.ALL_RADIO_TYPE) != 0) {
                Log.d(TAG, "checkProgramExist: type=0 radio cnt !=0");
                return true;
            }
        }else if(type == 1) {//check all tv
            if(ProgramInfoGetPlaySimpleChannelListCnt(ProgramInfo.ALL_TV_TYPE) != 0) {
                Log.d(TAG, "checkProgramExist: type=1 tv cnt !=0");
                return true;
            }
        }else if(type == 2) {//check all radio
            if(ProgramInfoGetPlaySimpleChannelListCnt(ProgramInfo.ALL_RADIO_TYPE) != 0) {
                Log.d(TAG, "checkProgramExist: type=2 radio cnt !=0");
                return true;
            }
        }

        return false;
    }

    public int recordTS_start(int TunerId, String FullName) // connie 20180803 add record ts -s
    {
        Log.d(TAG, "recordTS_start:  TunerId = " + TunerId + "     FullName =" +FullName);
        return super.recordTS_start(TunerId, FullName);
    }

    public int recordTS_stop()
    {
        return super.recordTS_stop();
    }// connie 20180803 add record ts -e

    public int FrontEndSetDiSEqC10PortInfo(int diseqc10Port, SatInfo satInfo)    // Johnny 20180814 add setDiseqc1.0 port
    {
        List<TpInfo> tpInfoList = TpInfoGetListBySatId(satInfo.getSatId());
        if (tpInfoList == null || tpInfoList.isEmpty())
        {
            Log.d(TAG, "FrontEndSetDiSEqC10PortInfo: TpList of sat  is empty!");
            return -1;
        }

        TpInfo tpInfo = tpInfoList.get(0);    // can only get tunerID in tpinfo now...

        if (satInfo.getTunerType() != TpInfo.DVBS
                || satInfo.Antenna.getDiseqcType() != SatInfo.DISEQC_TYPE_1_0
                /*|| satInfo.Antenna.getDiseqcUse() != 1*/)
        {
            Log.d(TAG, "FrontEndSetDiSEqC10PortInfo: Type error!");
            return -1;
        }

        // Hisi use EnFilterPolarity.AUTO which we don't have to set polarity, so we use polar in Tpinfo instead
        return super.FrontEndSetDiSEqC10PortInfo(tpInfo.getTuner_id(), diseqc10Port, satInfo.Antenna.getTone22k(), /*EnFilterPolarity.AUTO*/tpInfo.SatTp.getPolar());
    }
    //Scoty add DiSeqC Motor rule -s
    public int setDiSEqC12MoveMotor(int nTunerId, int Direct, int Step)
    {
        return super.setDiSEqC12MoveMotor(nTunerId, Direct, Step);
    }

    public int setDiSEqC12MoveMotorStop(int nTunerId)
    {
        return super.setDiSEqC12MoveMotorStop(nTunerId);
    }

    public int resetDiSEqC12Position(int nTunerId)
    {
        return super.resetDiSEqC12Position(nTunerId);
    }

    public int setDiSEqCLimitPos(int nTunerId, int limitType)
    {
        return super.setDiSEqCLimitPos(nTunerId,limitType);
    }
    //Scoty add DiSeqC Motor rule -e
    public int SetStandbyOnOff(int onOff)//Scoty 20180831 add standby function
    {
        return super.SetStandbyOnOff(onOff);
    }

    public boolean CheckPvrMode(int pvrMode) //Scoty 20180827 add and modify TimeShift Live Mode
    {
        if(pvrMode==PvrInfo.EnPVRMode.TIMESHIFT_LIVE || pvrMode==PvrInfo.EnPVRMode.TIMESHIFT_FILE
                || pvrMode==PvrInfo.EnPVRMode.PVRMODE_TIMESHIFT_LIVE_PAUSE)
            return true;
        else
            return false;
    }

    // for VMX need open/close -s
    public LoaderInfo GetLoaderInfo() // connie 20180903 for VMX -s
    {
        return super.GetLoaderInfo();
    }

    public CaStatus GetCAStatusInfo()
    {
        return super.GetCAStatusInfo();
    }

    public int GetECMcount()
    {
        return super.GetECMcount();
    }

    public int GetEMMcount()
    {
        return super.GetEMMcount();
    }

    public String GetLibDate()
    {
        return super.GetLibDate();
    }

    public String GetChipID()
    {
        return super.GetChipID();
    }

    public String GetSN()
    {
        return super.GetSN();
    }

    public String GetCaVersion()
    {
        return super.GetCaVersion();
    }

    public String GetSCNumber()
    {
        return super.GetSCNumber();
    }

    public int GetPairingStatus()
    {
        return super.GetPairingStatus();
    }

    public String GetPurse()
    {
        return super.GetPurse();
    }

    public int GetGroupM()
    {
        return super.GetGroupM();
    }

    public int SetPinCode(String pinCode, int PinIndex, int TextSelect)
    {
        return super.SetPinCode(pinCode, PinIndex, TextSelect);
    }

    public int SetPPTV(String pinCode, int pinIndex)
    {
        return super.SetPPTV(pinCode, pinIndex);
    }

    public void SetOMSMok()
    {
        super.SetOMSMok();
    }

    public void VMXTest(int mode) // MESSAGE :0    OTA:1    EATERMARK:2    PIN:3    IPPV:4
    {
        Log.d(TAG, "VMXTest: ");
        super.VMXTest(mode);
    }
    //Scoty 20181207 modify VMX OTA rule -s
    public void TestVMXOTA(int mode)
    {
        Log.d(TAG, "TestVMXOTA: ");
        super.TestVMXOTA(mode);
    }

    public void VMXAutoOTA(int OTAMode, int TriggerID, int TriggerNum, int TunerId, int SatId, int DsmccPid, int FreqNum, ArrayList<Integer> FreqList, ArrayList<Integer> BandwidthList ) // connie 20180919 add for vmx search
    {
        super.VMXAutoOTA(OTAMode, TriggerID, TriggerNum, TunerId, SatId, DsmccPid, FreqNum, FreqList, BandwidthList);
    }
    //Scoty 20181207 modify VMX OTA rule -e
    public String VMXGetBoxID()
    {
        return super.VMXGetBoxID();
    }

    public String VMXGetVirtualNumber()
    {
        return super.VMXGetVirtualNumber();
    }

    public void VMXStopEWBS(int mode)//Scoty 20181225 modify VMX EWBS rule//Scoty 20181218 add stop EWBS
    {
        super.VMXStopEWBS(mode);
    }

    public void VMXStopEMM()
    {
        super.VMXStopEMM();
    } // connie 20180903 for VMX -e

    public void VMXOsmFinish(int triggerID, int triggerNum)
    {
        Log.d(TAG, "VMXOsmFinish: triggerID = " + triggerID + "   triggerNum =" + triggerNum);
        return; // connie 20181120 avoid crash ! Need Modify !!!!!!
        //super.VMXOsmFinish(triggerID, triggerNum);
    }

    public VMXProtectData GetProtectData()
    {
        return super.GetProtectData();
    }

    public int SetProtectData(int first, int second, int third)
    {
        return super.SetProtectData(first, second, third);
    }

    public void VMXCheckPin(Context Activity, int PinType, int disable, long channelID, String msg, int pinIndex, int textSelector)
    {
        if(disable == 1)
        {
            if(ViewHistory.getCurChannel().getChannelId() == channelID)
                closVMXPinDialog();
        }
        else
        {
            if(channelID == ViewHistory.getCurChannel().getChannelId())
            {
                showVMX_IPPV_Dialog(Activity, PinType, msg, pinIndex, textSelector);
            }
        }
    }

    public void closVMXPinDialog() // connie 20180903 for VMX -s
    {
        if(VMXPinDialog != null && VMXPinDialog.isShowing())
            VMXPinDialog.dismissDialog();
    }

    public void showVMX_IPPV_Dialog(final Context Activity, int PinType, String msg, int pinIndex, int textSelector) // connie 20180903 for VMX -s
    {
        if(VMXPinDialog != null && VMXPinDialog.isShowing())
        {
            if(PinType == VMXPinDialogView.PIN_TYPE_IPPV && VMXPinDialog.GetPinType() == VMXPinDialogView.PIN_TYPE_PINCDOE)
                closVMXPinDialog();
            else if(PinType == VMXPinDialogView.PIN_TYPE_PINCDOE && VMXPinDialog.GetPinType() == VMXPinDialogView.PIN_TYPE_IPPV)
                return;
            else
            {
                VMXPinDialog.SetPinIndex(pinIndex);
                VMXPinDialog.SetTextSelector(textSelector);
                return;
            }
        }
        Log.d(TAG, "showVMX_IPPV_Dialog:     new VMXPinDialogView !!!!!!!");
        VMXPinDialog =new VMXPinDialogView(this, PinType,1, msg, this, pinIndex, textSelector)
        {
            public void onCheckPasswordIsRight() {
                Log.d(TAG, ">>>>>PASSWORD IS RIGHT!<<<<<");
            }

            public void onCheckPasswordIsFalse() {
                Log.d(TAG, ">>>>>PASSWORD IS False!<<<<<");
                new MessageDialogView(Activity,
                        getString(R.string.STR_INVALID_PASSWORD), 3000) {
                    public void dialogEnd() {
                    }
                }.show();
            }

            public void onBCIO_Notyfy(String msg) // connie 20180925 add for ippv/pin bcio notify
            {
                Log.d(TAG, "onBCIO_Notyfy:   msg ="+  msg);

                new MessageDialogView(Activity,
                        msg, 3000) {
                    public void dialogEnd() {
                    }
                }.show();
            }

            public boolean onDealUpDownKey() {
                return false;
            }
        };
    }

    public void VMXBcioNotify( int type) // connie 20180925 add for ippv/pin bcio notify
    {
        if(VMXPinDialog != null && VMXPinDialog.isShowing())
        {
            VMXPinDialog.BCIO_Notify(type);
        }
    }

    public void SetVMXBlockFlag(int flag) {
        VMXBlockFlag = flag;
    }
    public int GetVMXBlockFlag() {
        return VMXBlockFlag;
    }

    public void SetVMXLocationFlag(int flag) {
        VMXLocationSetting = flag;
    }
    public int GetVMXLocationFlag() {
        return VMXLocationSetting;
    }

    public boolean NeedLocationSetting()
    {
        VMXProtectData protectData = GetProtectData();
        int locationFirst = protectData.getLocationFirst();
        int locationSecond = protectData.getLocationSecond();
        int locationThird = protectData.getLocationThird();
        Log.d(TAG, "NeedLocationSetting:  GetVMXLocationFlag() = " +GetVMXLocationFlag() + "   locationFirst = " + locationFirst + "   locationSecond = " + locationSecond + "   locationThird = " + locationThird);
        if(/*GetVMXLocationFlag() == 1 &&*/ locationFirst == 0 && locationSecond == 0 && locationThird == 0)//Scoty 20181218 modify VMX location rule
            return true;
        else
            return false;
    }
    // for VMX need open/close -e

    public void SetEnterViewActivity(int enter) {
        super.SetEnterViewActivity(enter);
    }

    public void EnableMemStatusCheck(int enable) {
        super.EnableMemStatusCheck(enable);
    }

    public int LoaderDtvGetJTAG() {
        return super.LoaderDtvGetJTAG();
    }

    public int LoaderDtvSetJTAG(int value){
        return super.LoaderDtvSetJTAG(value);
    }

    public OTACableParameters DVBGetOTACableParas(){
        return super.DVBGetOTACableParas();
    }

    public OTATerrParameters DVBGetOTAIsdbtParas(){ return super.DVBGetOTAIsdbtParas(); }

    public OTATerrParameters DVBGetOTATerrestrialParas(){ return super.DVBGetOTATerrestrialParas(); }

    public OTATerrParameters DVBGetOTADVBT2Paras(){ return super.DVBGetOTADVBT2Paras(); }

    public int LoaderDtvCheckISDBTService(OTATerrParameters ota) {
        return super.LoaderDtvCheckISDBTService(ota);
    }

    public int LoaderDtvCheckTerrestrialService(OTATerrParameters ota) {
        return super.LoaderDtvCheckTerrestrialService(ota);
    }

    public int LoaderDtvCheckCableService(OTACableParameters ota){
        return super.LoaderDtvCheckCableService(ota);
    }

    public int LoaderDtvGetSTBSN() {return super.LoaderDtvGetSTBSN(); }

    public int LoaderDtvGetChipSetId() {return super.LoaderDtvGetChipSetId(); }

    public int LoaderDtvGetSWVersion() {return super.LoaderDtvGetSWVersion(); }

    public void MailInit() { // connie 20181116 for vmx mail -s
        MailHandler = new DatabaseHandler(this, "mail.sql");
        int ret = MailHandler.openDatabase();
        Log.d(TAG, "MailInit:  ret = " + ret);
        if (ret == -1)
            Log.d(TAG, "MailInit:  openDataBase Fail !!!!!");
        else {
            MailHandler.addColumn(MailTableName, getString(R.string.STR_MAIL_UUID), DatabaseHandler.TYPE_STRING, true);
            MailHandler.addColumn(MailTableName, getString(R.string.STR_MAIL), DatabaseHandler.TYPE_STRING, false);
            MailHandler.addColumn(MailTableName, getString(R.string.STR_READ), DatabaseHandler.TYPE_INTEGER, false);
            MailHandler.createTable();

            //============= ADD FAKE DATA ====================
            /*
            List<DatabaseHandler.Column> row = new ArrayList<>();
            String uuID = UUID.randomUUID().toString();
            row.add(new DatabaseHandler.Column(getString(R.string.STR_MAIL_UUID), uuID));
            row.add(new DatabaseHandler.Column(getString(R.string.STR_MAIL), "AAA BBB CCC DDD 123456789....."));
            row.add(new DatabaseHandler.Column(getString(R.string.STR_READ), 0));
            MailHandler.append(MailTableName, row);
            */
            //============================================================
            DataBaseMailList = UpdateMailInfoFromDataBase();
        }
    }

    private List<MailInfo> UpdateMailInfoFromDataBase()
    {
        List<MailInfo> list = new ArrayList<>();

        if(MailHandler != null && MailHandler.checkDatabase() != -1)
        {
            List<List<DatabaseHandler.Column>> rowList = MailHandler.readAll(MailTableName);
            String read = "";
            MailInfo info;
            for (List<DatabaseHandler.Column> rowA : rowList) {
                info = new MailInfo();
                for (DatabaseHandler.Column column : rowA) {
                    if (column.name.equals(getString(R.string.STR_MAIL_UUID)))
                        info.setMailID(column.value);
                    else if (column.name.equals(getString(R.string.STR_MAIL)))
                        info.setMailMsg(column.value);
                    else
                        info.setMailRead(Integer.valueOf(column.value));
                    read = column.name + "=" + column.value;
                    //Log.d(TAG, "UpdateMailInfoFromDataBase:  read =" + read);
                }
                list.add(info);
            }
        }
        //for(int i = 0; i < list.size(); i++)
        //    Log.d(TAG, "UpdateMailInfoFromDataBase: i =" + i + "    ID = " + list.get(i).getMailID() + "   msg = " + list.get(i).getMailMsg() + "     read = " + list.get(i).getMailRead());
        return list ;
    }

    public List<MailInfo> GetMailList()
    {
        DataBaseMailList = UpdateMailInfoFromDataBase();
        return DataBaseMailList;
    }

    public DatabaseHandler GetMailHandler()
    {
        return MailHandler;
    }

    public String getMailTableName()
    {
        return MailTableName;
    }

    public boolean CheckMailAllRead()
    {
        Log.d(TAG, "CheckMailAllRead: ");
        if(MailHandler != null && MailHandler.checkDatabase() != -1) {
            List<List<DatabaseHandler.Column>> FindList = MailHandler.read(MailTableName, getString(R.string.STR_READ), MailInfo.MAILUNREAD);

            if (FindList == null || FindList.size() == 0)
                return true;
            else
                return false;
        }
        else
        {
            return true;
        }

    }// connie 20181116 for vmx mail -e

    public void ChangeProgrmaToABSchannel()
    {
        if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798 && Pvcfg.getCAType() == Pvcfg.CA_VMX)
        {
            ProgramInfo program = ProgramInfoGetByChannelId(ViewHistory.getCurChannel().getChannelId());
            ProgramInfo newprogram = null;

            if(program == null)
                Log.d(TAG, "ChangeProgrmaToABSchannel:  program is NULL !!!!!!!!!");
            if(program != null && program.getOriginalNetworkId() != 8800)
            {
                newprogram = ProgramInfoGetByLcn(1, ProgramInfo.ALL_TV_TYPE);
                if(newprogram != null) {
                    Log.d(TAG, "ChangeProgrmaToABSchannel: new Program Name = " + newprogram.getDisplayName() + "    networkID =" + newprogram.getOriginalNetworkId() + "   num =" + newprogram.getDisplayNum());
                    setDefaultOpenChannel(newprogram.getChannelId(), newprogram.getType());
                    AvControlPlayByChannelId(ViewHistory.getPlayId(), newprogram.getChannelId(), ProgramInfo.ALL_TV_TYPE,1);
                }
            }
        }
    }

    // Edwin 20181214 live tv cannot scan channel -s
    protected void CheckMenuLockAndStartActivity ( final Context context, final Intent it )
    {
        if (GposInfoGet().getInstallLockOnOff() == 0)
        {
            startActivity(it);
        }
        else
        {
            new PasswordDialogView(context, GposInfoGet().getPasswordValue(), PasswordDialogView.TYPE_PINCODE,0) {
                public void onCheckPasswordIsRight() {
                    Log.d(TAG, ">>>>>PASSWORD IS RIGHT!<<<<<");
                    startActivity(it);
                }

                public void onCheckPasswordIsFalse() {
                    Log.d(TAG, ">>>>>PASSWORD IS False!<<<<<");

                    new MessageDialogView(context
                            , getString(R.string.STR_INVALID_PASSWORD)
                            , getResources().getInteger( R.integer.MESSAGE_DIALOG_DELAY )) {
                        public void dialogEnd() {
                        }
                    }.show();
                }
                public boolean onDealUpDownKey() {
                    return false;
                }
            };
        }
    }
    // Edwin 20181214 live tv cannot scan channel -e
    public void setMtestMountCallback(HwTestConfig.MountCallback callback) {//gary20200504 add HW test config get from usb
        mtestMountCallback = callback;
    }
 
    //eric lin 20210107 widevine cas, -start
    public void WidevineSetSessionId(int sessionIndex, int sessionId){
//        Log.d(TAG, "WidevineSetSessionId");
        super.WidevineSetSessionId(sessionIndex, sessionId);
    }
    public final static int CALLBACK_EVENT_EMM = 0x01;
    public final static int CALLBACK_EVENT_ECM = 0x02;
    public final static int CALLBACK_EVENT_PRIVATE_DATA = 0x03;
    public final static int CALLBACK_EVENT_OPEN_SSESION = 0x04;
    public final static int WV_TEST = 0xF0;//0x04;
    public final static int WV_TEST_CAS_READY = 0xF1;//0x05;
    public final static int WV_TEST_SET_SESSION_ID = 0xF2;//0x06;

    private final MainHandler mHandler = new MainHandler(this);
    static byte [] preECM;
    static int preSessionIndex;

    public byte[] toBytes(int i)//eric lin 20210112 widevine scheme data
    {
        byte[] result = new byte[4];

        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i /*>> 0*/);

        return result;
    }

    public static String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    public class MainHandler extends Handler
    {
        private WeakReference<DTVActivity> weakReference;

        public MainHandler(DTVActivity activity)
        {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            DTVActivity activity = weakReference.get();
            switch(msg.what) {
                case CALLBACK_EVENT_OPEN_SSESION: {
                    Log.d("WidevineCasSessionMgr", "DTVA handleMessage() CALLBACK_EVENT_OPEN_SSESION:");
                    msgOpenSessionData = (TVMessage.widevineMsg) msg.obj;
                    byte[] tmp = toBytes(msgOpenSessionData.ecm_pid);//eric lin 20210112 widevine scheme data
                    caPID[0] = tmp[2];//eric lin 20210112 widevine scheme data
                    caPID[1] = tmp[3];//eric lin 20210112 widevine scheme data
                    //Log.d("WidevineCasSessionMgr", "DTVA handleMessage() caPID[0]=" + caPID[0]);
                    //Log.d("WidevineCasSessionMgr", "DTVA handleMessage() caPID[1]=" + caPID[1]);

                    if(msgOpenSessionData == null)
                        Log.d("WidevineCasSessionMgr", "DTVA handleMessage() CALLBACK_EVENT_OPEN_SSESION: msgOpenSessionData is null xxx");
                    if(casSessionManager == null) {
                        Log.d("WidevineCasSessionMgr", "DTVA handleMessage() CALLBACK_EVENT_OPEN_SSESION: casSessionManager is null");
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //Write whatever to want to do after delay specified (1 sec)
                                Log.d("WidevineCasSessionMgr", "DTVA handleMessage() CALLBACK_EVENT_OPEN_SSESION: casSessionManager is null, after delay 2 sec, send msg");

                                if(msgOpenSessionData == null){
                                    Log.d("WidevineCasSessionMgr", "DTVA handleMessage() CALLBACK_EVENT_OPEN_SSESION: after delay 2 sec, msgData == null xxx");
                                }else {
                                    //open session
                                    //TVMessage.widevineMsg msgData = (TVMessage.widevineMsg)msg.obj;
                                    Log.d("WidevineCasSessionMgr", "1 sessionMode=" + msgOpenSessionData.sessionMode
                                            + ", wv_index=" + msgOpenSessionData.wv_index + ", es_pid=" + msgOpenSessionData.es_pid + ", ecm_pid=" + msgOpenSessionData.ecm_pid);
                                    widevineAcquireSession(msgOpenSessionData.sessionMode, msgOpenSessionData.wv_index, msgOpenSessionData.es_pid, msgOpenSessionData.ecm_pid);
                                }
                            }
                        }, 2000);
                    }else{
                        TVMessage.widevineMsg msgData = (TVMessage.widevineMsg) msg.obj;
                        //open session
                        //TVMessage.widevineMsg msgData = (TVMessage.widevineMsg)msg.obj;
                        Log.d("WidevineCasSessionMgr", "2 sessionMode=" + msgData.sessionMode
                                + ", wv_index=" + msgData.wv_index + ", es_pid=" + msgData.es_pid + ", ecm_pid=" + msgData.ecm_pid);
                        widevineAcquireSession(msgData.sessionMode, msgData.wv_index, msgData.es_pid, msgData.ecm_pid);
                    }
                }break;
                case CALLBACK_EVENT_EMM:
                    Log.d("WidevineCasSessionMgr", "DTVA handleMessage() CALLBACK_EVENT_EMM:");
                    break;
                case WV_TEST_CAS_READY:
                    Log.d("WidevineCasSessionMgr", "DTVA handleMessage() WV_TEST_CAS_READY: 1");
                    if(preECM != null) {
                        Log.d("WidevineCasSessionMgr", "DTVA handleMessage() WV_TEST_CAS_READY: 2 casSessionManager.getReadyState()="+casSessionManager.getReadyState());
                        if(casSessionManager != null && casSessionManager.getReadyState()
                                && ((casSessionArray[0] != null ) || (casSessionArray[1] != null ))){
                            Log.d("WidevineCasSessionMgr", "DTVA handleMessage() WV_TEST_CAS_READY: 3 call processDtvEcm(), preSessionIndex="+ preSessionIndex+", preECM len=" + preECM.length);
                            //Log.d("WidevineCasSessionMgr", "data="+ toHexString(preECM));
                            for(int i=0; i<CAS_SESSION_MAX; i++) {
                                if(casSessionArray[i] != null) {
                                    Log.d("WidevineCasSessionMgr", "DTVA handleMessage() WV_TEST_CAS_READY: 4 casSessionArray["+i+"] != null, call processDtvEcm()");
                                    casSessionArray[i].processDtvEcm(preECM);
                                }
                            }
                        }
                    }
                    break;
                case CALLBACK_EVENT_ECM: {
                    TVMessage.widevineMsg msgData = (TVMessage.widevineMsg) msg.obj;
                    //cas ready send, cas not ready keep
                    //byte[] val= (byte[])msg.obj;
                    byte[] val = msgData.ecmData;
                    int sessionIndex = msgData.wv_index;
                    boolean needProcessECM = false;
                    Log.d("WidevineCasSessionMgr", "DTVA handleMessage() CALLBACK_EVENT_ECM: sessionIndex="+sessionIndex);
                    if (preECM == null) {
                        preECM = val;
                        preSessionIndex = sessionIndex;
                    }
                    /*
                    if (preECM == null) {
                        //val = (byte[])msg.obj;
                        Log.d("WidevineCasSessionMgr", "DTVA handleMessage() CALLBACK_EVENT_ECM: preECM == null");
                        //Log.d("WidevineCasSessionMgr", "DTVA handleMessage() CALLBACK_EVENT_ECM: msg.obj length=" + ((byte[]) msg.obj).length);
                        //Log.d("WidevineCasSessionMgr", "DTVA handleMessage() data[0]=" + String.format("%02X ", val[0]) + ", last data=" + String.format("%02X ", val[val.length - 1]));
                        preECM = val;
                        preSessionIndex = sessionIndex;
                        //Log.d("WidevineCasSessionMgr", "DTVA handleMessage() preECM data[0]=" + String.format("%02X ", preECM[0]) + ", last data=" + String.format("%02X ", preECM[preECM.length - 1]));
                        needProcessECM = true;
                    } else {
                        //val =(byte[])msg.obj;
                        Log.d("WidevineCasSessionMgr", "DTVA handleMessage() CALLBACK_EVENT_ECM: preECM != null");
                        //Log.d("WidevineCasSessionMgr", "DTVA handleMessage() CALLBACK_EVENT_ECM: msg.obj length=" + ((byte[]) msg.obj).length + ", preECM length=" + preECM.length);
                        //Log.d("WidevineCasSessionMgr", "DTVA handleMessage() preECM data[0]=" + String.format("%02X ", preECM[0]) + ", last data=" + String.format("%02X ", preECM[preECM.length - 1]));
                        if (!Arrays.equals(val, preECM)) {
                            Log.d("WidevineCasSessionMgr", "DTVA handleMessage() CALLBACK_EVENT_ECM: val != preECM");
                            //Log.d("WidevineCasSessionMgr", "DTVA handleMessage() data[0]=" + String.format("%02X ", val[0]) + ", last data=" + String.format("%02X ", val[val.length - 1]));
                            preECM = val;
                            needProcessECM = true;
                        } else
                            Log.d("WidevineCasSessionMgr", "DTVA handleMessage() CALLBACK_EVENT_ECM: val = preECM");
                    }
                    */
                    needProcessECM = true; 

                    if (needProcessECM == true) {
                        if (casSessionManager != null && casSessionManager.getReadyState()
                                && ((casSessionArray[0] != null ) || (casSessionArray[1] != null ))) {
                            Log.d("WidevineCasSessionMgr", "DTVA handleMessage() CALLBACK_EVENT_ECM: want to call processDtvEcm(), sessionIndex="+sessionIndex+", ecm len="+val.length);
                            //Log.d("WidevineCasSessionMgr", "data="+ toHexString(val));
                            for(int i=0; i<CAS_SESSION_MAX; i++) {
                                if(casSessionArray[i] != null) {
                                    Log.d("WidevineCasSessionMgr", "DTVA handleMessage() CALLBACK_EVENT_ECM: casSessionArray["+i+"] != null, call processDtvEcm()");
                                    casSessionArray[i].processDtvEcm(val);
                                }
                            }
                        }
                    }
                    //Log.d("WidevineCasSessionMgr", "DTVA handleMessage() 3 data[0]="+String.format("%02X ", val[0])+", last data="+String.format("%02X ", val[val.length-1]));
                    //Log.d("WidevineCasSessionMgr", "DTVA handleMessage() msg.obj=");
                    //Log.d("WidevineCasSessionMgr", WidevineCasSession.toHexString((byte[]) msg.obj));
                }break;
                case CALLBACK_EVENT_PRIVATE_DATA:
                    Log.d("WidevineCasSessionMgr", "DTVA handleMessage() CALLBACK_EVENT_PRIVATE_DATA:");
                    TVMessage.widevineMsg msgData = (TVMessage.widevineMsg) msg.obj;//eric lin 20210112 widevine scheme data
                    privateData = msgData.ecmData;//eric lin 20210112 widevine scheme data

                    HiDtvMediaPlayer.getInstance().privateData = msgData.ecmData;//eric lin 20210312 set private data from service
                    Log.d("WidevineCasSessionMgr", "DTVA handleMessage() CALLBACK_EVENT_PRIVATE_DATA: data length="+ HiDtvMediaPlayer.getInstance().privateData.length);
                    Log.d("WidevineCasSessionMgr", "data="+ toHexString(HiDtvMediaPlayer.getInstance().privateData));

                    break;
                case WV_TEST: {
                    //Log.d("WidevineCasSessionMgr", "DTVA handleMessage() WV_TEST");
                    new Thread(new Runnable() {
                    public void run() {
                        Log.d("WidevineCasSessionMgr", "DTVA handleMessage() WV_TEST: init_widevineCas() start");
                        init_widevineCas();
                        Log.d("WidevineCasSessionMgr", "DTVA handleMessage() WV_TEST: init_widevineCas() end");

                    }
                    }).start();
                    //init_widevineCas();

                }break;
                case WV_TEST_SET_SESSION_ID:
                    Log.d("WidevineCasSessionMgr", "DTVA handleMessage() WV_TEST_SET_SESSION_ID: sessionIdx="+ msg.arg1 +", sessionId="+msg.arg2);
                    WidevineSetSessionId(msg.arg1, msg.arg2);//need fix
                    break;
            }
        }
    }

    private void sendMessage(Handler handler, int what, int arg1, Object obj)
    {
        Message msg = new Message();
        msg.what = what;
        msg.arg1 = arg1;
        msg.obj = obj;
        handler.sendMessage(msg);
    }

    //eric lin widevine, -start
    static public HttpDataSource.Factory buildHttpDataSourceFactory() {
        return new DefaultHttpDataSourceFactory(userAgent);
    }

    static private CasSessionManager buildCasSessionManagerV18(int casSchemeId, String casVendor, String casProvisioning,
                                                        String casLicenseUrl, String casContentId, String casProvider)
            throws UnsupportedCasException {
        //android.util.Log.d("WidevineCasSessionMgr", "DTVA buildCasSessionManagerV182 FrameworkMediaCas.newInstance(casSchemeId)");
        //android.util.Log.d("WidevineCasSessionMgr", "DTVA buildCasSessionManagerV182 casSchemeId="+casSchemeId);
        //android.util.Log.d("WidevineCasSessionMgr", "DTVA buildCasSessionManagerV182 casVendor="+casVendor);
        //android.util.Log.d("WidevineCasSessionMgr", "DTVA buildCasSessionManagerV182 casProvisioning="+casProvisioning);
        //android.util.Log.d("WidevineCasSessionMgr", "DTVA buildCasSessionManagerV182 casLicenseUrl="+casLicenseUrl);
        //android.util.Log.d("WidevineCasSessionMgr", "DTVA buildCasSessionManagerV182 casContentId="+casContentId);
        //android.util.Log.d("WidevineCasSessionMgr", "DTVA buildCasSessionManagerV182 casProvider="+casProvider);
        ExoMediaCas<MediaCas.Session> mediaCas = FrameworkMediaCas.newInstance(casSchemeId);
        if (casVendor.equals("widevine")) {
            //android.util.Log.d("WidevineCasSessionMgr", "DTVA buildCasSessionManagerV182 casVendor = widevine");

            HttpDataSource.Factory licenseDataSourceFactory =
                    //((DemoApplication) getApplication()).buildHttpDataSourceFactory();
                    buildHttpDataSourceFactory();
            HttpMediaCasCallback casCallback =
                    new HttpMediaCasCallback(casLicenseUrl, true, licenseDataSourceFactory);

            return new WidevineCasSessionManager(casSchemeId, casProvisioning, casContentId, casProvider, casCallback, mediaCas);
        } else {
            android.util.Log.d("afaaexo", "DTVA buildCasSessionManagerV182 casVendor != widevine");
            return new DefaultCasSessionManager(casSchemeId, mediaCas);
        }
    }

    /*
    private void sendCasReady()
    {
        if(mHandler != null) {//gary eric
            sendMessage(mHandler, WV_TEST_CAS_READY, 0, 0);
        }
    }
     */

    public void init_widevineCas()
    {
        Log.d("WidevineCasSessionMgr", "init_widevineCas()--start");

        String casVendor = "widevine";
        String casProvisioning = "https://www.googleapis.com/certificateprovisioning/v1/devicecertificates/create?key=AIzaSyB-5OLKTx2iU5mko18DfdwK5611JIjbUhE";
        String casLicenseUrl = "http://58.140.89.88:12100/fortress/wvcas/licenseproxy/getlicense";//"https://proxy.uat.widevine.com/proxy";
        String casContentId = "21140844";
        String casProvider = "widevine_test";
        int casSchemeId = 19156; //Util.getCasId(casVendor); //eric lin widevine, need porting

        try {
            casSessionManager = buildCasSessionManagerV18(casSchemeId, casVendor, casProvisioning, casLicenseUrl,
                    casContentId, casProvider);
            casSessionManager.setHandler(mHandler);
        }catch (UnsupportedCasException e) {
            Log.d("WidevineCasSessionMgr", "init_widevine_cas() UnsupportedCasException e.reason="+e.reason);
            /*
            errorStringId = e.reason == UnsupportedCasException.REASON_UNSUPPORTED_SCHEME
                    ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown;
             */
        }
        if (casSessionManager == null) {
            Log.d("WidevineCasSessionMgr", "init_widevine_cas() casSessionManager == null, error");
            //showToast(errorStringId);
            //finish();
            //return;
        }
        //start_provision();//change caller to get openSession callback
        //sendCasReady();
        Log.d("WidevineCasSessionMgr", "init_widevineCas()--end");
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    public void widevineAcquireSession(int sessionMode, int sessionIndex, int es_pid, int ecm_pid)
    {
        Log.d("WidevineCasSessionMgr", "widevineAcquireSession()--start");
        int curSessionArrayIdx=0;
        int casSchemeId = 19156; //Util.getCasId(casVendor); //eric lin widevine, need porting

        //copy from log "mcr want to call acquireSession()--2"
        //eric lin 20210112 widevine scheme data,-start
        byte[] data = hexStringToByteArray("4ad400950a087769646576696e651209436173547346616b65");
        int newDataLen = 4 + privateData.length; //ca system id(2) + ca pid(2)
        Log.d("WidevineCasSessionMgr", "widevineAcquireSession() newDataLenx=" + newDataLen);
        byte[] newData = new byte[newDataLen];
        newData[0] = (byte) 0x4a;
        newData[1] = (byte) 0xd4;
        newData[2] = (byte) caPID[0];
        newData[3] = (byte) caPID[1];
        for(int i=0 ;i<privateData.length; i++)
        {
            newData[4+i] = privateData[i];
        }
        //Log.d("WidevineCasSessionMgr", "widevineAcquireSession() newData=" + toHexString(newData));
        CasInitData.SchemeData schemeData = new CasInitData.SchemeData(casSchemeId, newData);
        //eric lin 20210112 widevine scheme data,-end
        CasInitData casInitData = new CasInitData(schemeData);
        //format = format.copyWithCasInitData(casInitData);
        //Log.d("WidevineCasSessionMgr", "init_widevine_cas() new casInitData data=");
        //Log.d("WidevineCasSessionMgr", toHexString(casInitData.get().data));
        CasSession casSessionTmp;
        if(sessionMode == 0) {//SESSION_MODE_EXTRACTOR
            curSessionArrayIdx = 0;
            //sessionRec[curSessionArrayIdx].sessionIndex = sessionIndex;
            Log.d("WidevineCasSessionMgr", "widevineAcquireSession() extractor: curSessionArrayIdx=" + curSessionArrayIdx + ", sessionIndex="+sessionIndex);
            //casSessionArray[curSessionArrayIdx] = casSessionManager.acquireSession(casInitData/* format. casInitData*/, CasSessionManager.SESSION_MODE_EXTRACTOR, sessionIndex);//eric lin need fix
            casSessionTmp = casSessionManager.acquireSession(casInitData/* format. casInitData*/, CasSessionManager.SESSION_MODE_EXTRACTOR, sessionIndex);//eric lin need fix
        }else{
            //Log.d("WidevineCasSessionMgr", "widevineAcquireSession() AAA 1 esSessionCount=" + esSessionCount +", want to ++");
            curSessionArrayIdx = ++esSessionCount;
            //sessionRec[curSessionArrayIdx].sessionIndex = sessionIndex;
            Log.d("WidevineCasSessionMgr", "widevineAcquireSession() render: curSessionArrayIdx=" + curSessionArrayIdx + ", sessionIndex="+sessionIndex);
            //casSessionArray[curSessionArrayIdx] = casSessionManager.acquireSession(casInitData/* format. casInitData*/, CasSessionManager.SESSION_MODE_RENDERER, sessionIndex);//eric lin need fix
            casSessionTmp = casSessionManager.acquireSession(casInitData/* format. casInitData*/, CasSessionManager.SESSION_MODE_RENDERER, sessionIndex);//eric lin need fix
        }
        Log.d("WidevineCasSessionMgr", "widevineAcquireSession() 1 new sessionRecord() sessionIndex="+sessionIndex+", es_pid=" + es_pid + ", ecm_pid="+ecm_pid);
        sessionRec[curSessionArrayIdx] = new sessionRecord(sessionIndex, es_pid, ecm_pid);
        casSessionArray[curSessionArrayIdx] = casSessionTmp;
        Log.d("WidevineCasSessionMgr", "widevineAcquireSession() 2 sessionIndex="+sessionIndex+", es_pid=" + es_pid + ", ecm_pid="+ecm_pid);
        Log.d("WidevineCasSessionMgr", "widevineAcquireSession() 3 sessionRec index="+curSessionArrayIdx+", sessionIndex="+sessionRec[curSessionArrayIdx].sessionIndex+", es_pid=" + sessionRec[curSessionArrayIdx].es_pid + ", ecm_pid="+sessionRec[curSessionArrayIdx].ecm_pid);
        //sessionRec[curSessionArrayIdx].ecm_pid = ecm_pid;


        //if (pendingCasSession == casSession) {
        //    casSessionManager.releaseSession(pendingCasSession);
        //}
        Log.d("WidevineCasSessionMgr", "widevineAcquireSession()--end");
    }
    public void disable_widevineCas()
    {
        Log.d("WidevineCasSessionMgr", "disable_widevineCas()--start");
        for(int i=0; i<CAS_SESSION_MAX; i++)
        {
            if(casSessionArray[i]!=null) {
                Log.d("WidevineCasSessionMgr", "disable_widevineCas() casSessionArray["+i+"] != null ZZZ, release--start");
                casSessionManager.releaseSession(casSessionArray[i]);
                Log.d("WidevineCasSessionMgr", "disable_widevineCas() casSessionArray["+i+"] != null ZZZ, release--end");
                casSessionArray[i] = null;
                sessionRec[i].clear();
            }else
                Log.d("WidevineCasSessionMgr", "disable_widevineCas() casSessionArray["+i+"] is null, do nothing");
        }

        //casSessionManager.releaseSession(casSessionPes);
        //casSession = null;//eric lin add
        preECM = null;//eric lin add
        //casSessionPes = null;//eric lin add
        esSessionCount = 0;
        Log.d("WidevineCasSessionMgr", "disable_widevineCas()--end");
    }

    //eric lin widevine, -end

    public class sessionRecord {
        public int sessionIndex;
        public int es_pid;
        public int ecm_pid;
        sessionRecord(int sessionIndex, int es_pid, int ecm_pid)
        {
            this.sessionIndex = sessionIndex;
            this.es_pid = es_pid;
            this.ecm_pid = ecm_pid;
        }
        public void clear()
        {
            sessionIndex = 0;
            es_pid = 0;
            ecm_pid = 0;
        }
    }
    //eric lin 20210107 widevine cas, -start

    /**
     * After Search complete, add VOD & YOUTUBE Programs to TotalChannelList
     * @param groupType
     */
    private void AddNetProgramToTotalChannelList(int groupType)
    {
        DataManager.NetProgramDatabase mNetProgramDatabase = DataManager.getDataManager().GetNetProgramDatabase();
        List<NetProgramInfo> netprogramList = mNetProgramDatabase.GetNetProgramList(this);
        for(int i = 0 ; i < netprogramList.size() ; i++)
            SetNetStreamInfo(groupType, netprogramList.get(i));
    }


    public int SetNetStreamInfo(int GroupType, NetProgramInfo netStreamInfo)
    {
        return super.SetNetStreamInfo(GroupType,netStreamInfo);
    }

    public SimpleExoPlayer GetExoPlayer()
    {
        return super.GetExoPlayer();
    }

    public void SetExoplayerSurfaceView(SurfaceView surfaceView)
    {
        super.SetExoplayerSurfaceView(surfaceView);
    }

    public SurfaceView GetExoplayerSurfaceView()
    {
        return super.GetExoplayerSurfaceView();
    }

    public void SetYoutubeWebview(WebView webview)
    {
        super.SetYoutubeWebview(webview);
    }

    public WebView GetYoutubeWebview()
    {
        return super.GetYoutubeWebview();
    }

    public int ResetProgramDatabase()
    {
        return super.ResetProgramDatabase();
    }

    public int ResetNetProgramDatabase()
    {
        return super.ResetNetProgramDatabase();
    }

    public void ProgramInfoPlaySImpleChannelListUpdate(Context context, int IncludePVRSkipFlag)
    {
        super.ProgramInfoPlaySimpleChannelListUpdate(context,IncludePVRSkipFlag);
    }


////////////////////////////////////////////////////////////////////////////////////
    /**
     * First Time Get netprogram.ini File and Add to DataBase
     * After Save Complete Rename to netprogram_already_set.ini
     * In order to not to save database again
     * @return isSuccess : Save DataBase Results
     */
//    public boolean InitNetProgramDatabase()
//    {
//        Log.d(TAG, "InitNetProgramDatabase: exce IN");
////        boolean isSaveDataBase = true;
//        DataManager.NetProgramDatabase mNetProgramDatabase = DataManager.getDataManager().GetNetProgramDatabase();
//        if(mNetProgramDatabase == null)
//            Log.d(TAG, "InitNetProgramDatabase: is NULL");
//
////        List<NetProgramInfo> netList = mNetProgramDatabase.GetNetProgramList(this);
////        Log.d(TAG, "InitNetProgramDatabase: size = [" + netList.size() +"]");
////        if(netList.size() > 0) {
////            Log.d(TAG, "InitNetProgramDatabase: No Need to Save NetProgram Database again");
////            isSaveDataBase = false;
////        }
//
//        String VOD_INI_TITLE = "[VOD]", YOUTUBE_INI_TITLE = "[YOUTUBE]", EANABLE_NETPROGRAMS = "[ENABLE_NETPROGRAMS]";
//        String INI_READ_PATH = "/vendor/etc/dtvplayer_settings/netprogram.ini";
//        File readFile = new File(INI_READ_PATH);
//        if(!readFile.exists()) {
//            Log.d(TAG, "InitNetProgramDatabase: netprogram.ini Not Exist!");
//            Pvcfg.SetEnableNetworkPrograms(false);
//            return false;
//        }
//
//        String line;
//        List<NetProgramInfo> netProgramInfoList = new ArrayList<>();
//        try {
//            FileReader fileReader = new FileReader(readFile);
//            BufferedReader breader = new BufferedReader(fileReader);
//            while ((line=breader.readLine()) != null) {
//                NetProgramInfo tmpNetProgramInfo = new NetProgramInfo();
//                Log.d(TAG, "InitNetProgramDatabase: get NetProgram Title [" + line +"]");
//                if(line.equals(VOD_INI_TITLE) || line.equals(YOUTUBE_INI_TITLE))
//                {
//                    tmpNetProgramInfo = setNetProgramfromIniFile(breader);
//                    netProgramInfoList.add(tmpNetProgramInfo);
//                }else if(line.equals(EANABLE_NETPROGRAMS))//Check Add YOUTUBE & VOD or not
//                {
//                    line=breader.readLine();
//                    String[] EnableNetProgramText = line.split("==");
//                    if(EnableNetProgramText[0].toUpperCase().equals("ENABLE"))
//                    {
//                        // Enable or Disable Netprograms
//                        Pvcfg.SetEnableNetworkPrograms(EnableNetProgramText[1].toUpperCase().equals("TRUE"));
//                    }
//
//                    if(!Pvcfg.IsEnableNetworkPrograms())// No Need to Save Database if false
//                        return false;
//                }
//            }
//            breader.close();
//
//        }catch (IOException e) {
//            return false;
//        }
//
////        boolean isSuccess = true;
////        if(isSaveDataBase) {
//            boolean isSuccess = SaveNetProgramList(netProgramInfoList);
//            Log.d(TAG, "InitNetProgramDatabase: isSuccess = [" + isSuccess + "]");
////        }
////        return isSuccess;
//
//    }

    //    /**
//     * Read netprogram.ini File and Save Params to NetProgramInfo Database
//     * @param reader Read netprogram.ini
//     * @return NetprogramInfo
//     */
//    private NetProgramInfo setNetProgramfromIniFile(BufferedReader reader)
//    {
//        Log.d(TAG, "exce setNetProgramfromIniFile: ");
//        String iniFileInfo;
//        String PARAMS_END = "[END]";
//        String NET_SERVICE_ID_TAG = "ServiceId";
//        String NET_PLAY_STREAM_TYPE_TAG = "PlayStreamType";
//        String NET_VIDEO_URL_TAG = "videoUrl";
//        String NET_CHANNEL_NAME_TAG = "ChannelName";
//        String NET_CHANNEL_NUM_TAG = "ChannelNum";
//        NetProgramInfo netStreamInfo = new NetProgramInfo();
//        try {
//            while(!(iniFileInfo=reader.readLine()).equals(PARAMS_END))
//            {
//                Log.d(TAG, "exce setVodProgramDatabase: get = [" + iniFileInfo +"]");
//                String[] textsplit = iniFileInfo.split("==");
//                if(textsplit[0].equals(NET_SERVICE_ID_TAG))
//                {
//                    int serviceId = parseInt(textsplit[1]);
//                    int tpSize = TpInfoGetList(GetCurTunerType()).size();
//                    int channelId = (serviceId << 16) + (tpSize + 1);
//                    netStreamInfo.setChannelId(channelId);
//                }else if(textsplit[0].equals(NET_PLAY_STREAM_TYPE_TAG)){
//                    netStreamInfo.setPlayStreamType(parseInt(textsplit[1]));
//                }else if(textsplit[0].equals(NET_VIDEO_URL_TAG)){
//                    netStreamInfo.setVideoUrl(textsplit[1]);
//                }else if(textsplit[0].equals(NET_CHANNEL_NAME_TAG)){
//                    netStreamInfo.setChannelName(textsplit[1]);
//                }else if(textsplit[0].equals(NET_CHANNEL_NUM_TAG)){
//                    netStreamInfo.setChannelNum(parseInt(textsplit[1]));
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return netStreamInfo;
//    }

//    /**
//     * netProgramlist from netprogram.ini
//     * @param netProgramlist Saved NetPrograms List
//     * @return isSuccess Save DataBase Results
//     */
//    private boolean SaveNetProgramList(List<NetProgramInfo> netProgramlist)
//    {
////        for(int i = 0 ; i < netProgramlist.size() ; i++)
////            Log.d(TAG, "exce SaveNetProgramList: " + netProgramlist.get(i).ToString());
//
//        //Save NetProgramInfo Database
//        DataManager.NetProgramDatabase mNetProgramDatabase = DataManager.getDataManager().GetNetProgramDatabase();
//        boolean isSuccess = mNetProgramDatabase.SaveNetProgramListDatabase(this,netProgramlist); //Clear and Add
//        Log.d(TAG, "SaveNetProgramList: isSuccess = [" + isSuccess +"]");
//
//        return isSuccess;
//    }
}
