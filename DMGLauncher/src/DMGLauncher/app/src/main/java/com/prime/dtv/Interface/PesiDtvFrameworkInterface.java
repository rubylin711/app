package com.prime.dtv.Interface;

import static com.prime.dtv.PrimeDtvMediaPlayer.CMD_RETURN_VALUE_SUCCESS;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import com.prime.dtv.sysdata.ChannelNode;
import com.prime.dtv.sysdata.PvrRecFileInfo;
import com.prime.dtv.sysdata.PvrRecIdx;
import com.prime.dtv.sysdata.PvrRecStartParam;
import com.prime.dtv.sysdata.EnTableType;
import com.prime.dtv.sysdata.EnTrickMode;
import com.prime.dtv.IDTVListener;
import com.prime.dtv.sysdata.NetProgramInfo;
import com.prime.dtv.sysdata.PVREncryption;
import com.prime.dtv.sysdata.PvrFileInfo;
import com.prime.dtv.sysdata.Resolution;
import com.prime.dtv.sysdata.OTACableParameters;
import com.prime.dtv.sysdata.OTATerrParameters;
//import com.prime.dtv.Activity.DTVActivity;
import com.prime.dtv.sysdata.AudioInfo;
import com.prime.dtv.sysdata.BookInfo;
import com.prime.dtv.sysdata.CaStatus;
import com.prime.dtv.sysdata.DefaultChannel;
import com.prime.dtv.sysdata.EPGEvent;
import com.prime.dtv.sysdata.EnAudioTrackMode;
import com.prime.dtv.sysdata.FavGroupName;
import com.prime.dtv.sysdata.FavInfo;
import com.prime.dtv.sysdata.GposInfo;
import com.prime.dtv.sysdata.LoaderInfo;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.sysdata.PvrInfo;
import com.prime.dtv.sysdata.SatInfo;
import com.prime.dtv.sysdata.SimpleChannel;
import com.prime.dtv.sysdata.SubtitleInfo;
import com.prime.dtv.sysdata.TeletextInfo;
import com.prime.dtv.sysdata.TpInfo;
import com.prime.dtv.sysdata.VMXProtectData;
import com.prime.dtv.utils.TVScanParams;
import com.prime.dtv.utils.TVTunerParams;
import com.prime.dtv.PrimeDtv;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PesiDtvFrameworkInterface {
    private static final String TAG = "PesiDtvFrameworkInterface";
    private int ChannelIsExist = 0;

    private PrimeDtv.ViewUiDisplay ViewUIDisplayManager = null;
    private PrimeDtv.EpgUiDisplay EpgUiDisplayManager = null;
    private PrimeDtv.BookManager UIBookManager = null;
    private List<BookInfo> UIBookList = null;


    public abstract void UpdatePMT(long channel_id);

    public abstract void StopDVBSubtitle();

    public abstract void setSurfaceView(Context context, SurfaceView surfaceView, int index);

    public abstract int AvControlPlayStopAll();

    public abstract void stopMonitorTable(long channel_id, int tuner_id);

    public abstract void startMonitorTable(long channel_id, int isFcc, int tuner_id);

    public abstract int AvControlPlayStop(int tunerId, int mode, int stop_monitor_table);

    public abstract void setSurfaceToPlayer(int index, Surface surface);

    public abstract void ResetCheckAD();


    public class CallbackManager {
        private ArrayList<EventMapType> mlstListenerMap = null;
        private CallbackHandler mCallbackHandler = null ;
        public CallbackHandler getCallbackHandler()
        {
            return mCallbackHandler ;
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

        public CallbackManager ()
        {
            mlstListenerMap = new ArrayList<EventMapType>();
            Looper looper;
            if ((looper = Looper.myLooper()) != null)
            {
                mCallbackHandler = new CallbackHandler(looper);
            }
            else if ((looper = Looper.getMainLooper()) != null)
            {
                mCallbackHandler = new CallbackHandler(looper);
            }
            else
            {
                mCallbackHandler = null;
            }
        }

        private class CallbackHandler extends Handler {
            public CallbackHandler(Looper looper) {
                super(looper);
            }

            public void handleMessage(Message msg)
            {
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
//                                Log.d(TAG, "CallbackManager handleMessage: send notifyMessage " + msg.what);
                                mEventListener.notifyMessage(msg.what, msg.arg1, msg.arg2, object);
                            }
                        }
                    }
                }
            }
        }

        private EventMapType getListenerMap(int eventID, IDTVListener eventListener) {
            if (null == mlstListenerMap || mlstListenerMap.size() <= 0) {
                return null;
            }

            for (int i = 0; i < mlstListenerMap.size(); i++) {
                EventMapType eventMapType = mlstListenerMap.get(i);
                if ((eventID == eventMapType.mEventType) && (eventListener == eventMapType.mEventListener)) {
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

        public int subScribeEvent(int eventID, IDTVListener eventListener, int privateParam) {
            EventMapType eventMapType = getListenerMap(eventID, eventListener);
            if (null != eventMapType)
            {
                return CMD_RETURN_VALUE_SUCCESS;
            }

//        Log.d(TAG, "add eventMapType to mListnerMap: (eventID= " + eventID + ")");

            mlstListenerMap.add(new EventMapType(eventID, eventListener, privateParam));
            return CMD_RETURN_VALUE_SUCCESS;
        }

        public int unSubScribeEvent(int eventID, IDTVListener eventListener) {
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
                }
                else
                {
                    Log.d(TAG, "mListnerMap == null");
                }
            }

            return CMD_RETURN_VALUE_SUCCESS;
        }
    }


    public interface OnServiceDiedListener
    {
        void onServiceDied(long cookie);
    }

    /*
    system
     */
    public abstract void unlinkDeathNotify() ;
    public abstract void linkDeathNotify() ;
    public abstract void setOnServiceDiedListener(OnServiceDiedListener listener) ;

    public abstract int subScribeEvent(int eventID, IDTVListener eventListener, int privateParam);
    public abstract int unSubScribeEvent(int eventID, IDTVListener eventListener);

    public abstract void ResetFactoryDefault();
    public abstract int getPlatform();
    public abstract String GetPesiServiceVersion();
    public abstract String GetApkSwVersion();
    public abstract int SetStandbyOnOff(int onOff);
    public abstract void backupDatabase(boolean force);

    /*
    Gpos
     */
    public abstract GposInfo GposInfoGet();
    public abstract void GposInfoUpdate(GposInfo gPos);
    public abstract void GposInfoUpdateByKeyString(String key, String value);
    public abstract void GposInfoUpdateByKeyString(String key,int value);
    public abstract void saveGposKeyValue(String key, String value);
    public abstract void saveGposKeyValue(String key,int value);
    public abstract void saveGposKeyValue(String key,long value);

    /*
    channel & program
     */
    public abstract ArrayList<List<SimpleChannel>> getProgramManagerTotalChannelList() ;
    public abstract ArrayList<FavGroupName> getAllProgramGroup() ;
    public abstract List<ProgramInfo> getProgramInfoList(int type, int pos, int num);
    public abstract ProgramInfo getProgramByChannelId(long channelId);
    public abstract ProgramInfo getProgramByChnum(int chnum, int type);
    public abstract ProgramInfo getProgramByLcn(int lcn, int type);
    public abstract ProgramInfo getProgramByServiceId(int service_id);
    public abstract ProgramInfo getProgramByServiceIdTransportStreamId(int service_id,int ts_id);
    public abstract int updateProgramInfo(ProgramInfo pProgram);
    public abstract int updateSimpleChannelList(List<SimpleChannel> pPrograms, int type);
    public abstract void deleteProgram(long channelId);
    public abstract int saveTable(EnTableType option);
    public abstract int setDefaultOpenChannel(long channelId, int groupType);
    public abstract DefaultChannel getDefaultChannel();
    public abstract ChannelNode getChannelByID(long channelId);
    public abstract void updateCurPlayChannelList(Context context, int includePVRSkipFlag);
    public abstract void updateCurPlayChannelList(int includePVRSkipFlag);
    public abstract void resetTotalChannelList();
    public abstract List<SimpleChannel> getCurPlayChannelList(int type, int includePVRSkipFlag);
    public abstract int getCurPlayChannelListCnt(int type);
    public abstract List<SimpleChannel> getSimpleProgramList(int type, int includeSkipFlag, int includePVRSkipFlag);
    public abstract List<SimpleChannel> getSimpleProgramListfromTotalChannelList(int type, int includeSkipFlag, int includePVRSkipFlag);
    public abstract SimpleChannel getSimpleProgramByChannelId(long channelId);
    public abstract SimpleChannel getSimpleProgramByChannelIdfromTotalChannelList(long channelId);
    public abstract SimpleChannel getSimpleProgramByChannelIdfromTotalChannelListByGroup(int groupType, long channelId);
    public abstract List<SimpleChannel> getChannelListByFilter(int filterTag, int serviceType, String keyword, int includeSkip, int includePvrSkip);
    public abstract void update_pvr_skip_list(int groupType, int IncludePVRSkipFlag, int tpId, List<Integer> pvrTpList);

    public void setChannelExist(int exist)
    {
        ChannelIsExist = exist;
    }

    public int getChannelExist()
    {
        return ChannelIsExist;
    }

    /*
    fav
     */
    public abstract List<FavInfo> favInfoGetList(int favMode);
    public abstract FavInfo favInfoGet(int favMode, int index);
    public abstract int favInfoUpdateList(int favMode, List<FavInfo> favInfoList);
    public abstract int favInfoDelete(int favMode, long channelId);
    public abstract int favInfoDeleteAll(int favMode);
    public abstract int favInfoSaveDb(FavInfo favInfo);
    public abstract String favGroupNameGet(int favMode);
    public abstract int favGroupNameUpdate(int favMode, String name);

    /*
    Timer
     */
    public List<BookInfo> InitUIBookList(){//Init UI Book List after boot
        if(UIBookList == null)
            UIBookList = new ArrayList<BookInfo>();
        UIBookList = BookInfoGetList();

        if(UIBookList == null)
            UIBookList = new ArrayList<BookInfo>();

        return UIBookList;
    }

    public void SetUIBookManager(PrimeDtv.BookManager bookmanager) {
        UIBookManager = bookmanager;
    }

    public PrimeDtv.BookManager GetUIBookManager() {
        return UIBookManager;
    }

    public List<BookInfo> GetUIBookList() {
        return UIBookList;
    }

    private void clearUIBookManagerList()
    {
        if(UIBookList != null)
            UIBookList.clear();
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

    public abstract List<BookInfo> BookInfoGetList();
    public abstract BookInfo BookInfoGet(int bookId);
    public abstract int BookInfoAdd(BookInfo bookInfo);
    public abstract int BookInfoUpdate(BookInfo bookInfo);
    public abstract int BookInfoUpdateList(List<BookInfo> bookList);
    public abstract int BookInfoDelete(int bookId);
    public abstract int BookInfoDeleteAll();
    public abstract BookInfo BookInfoGetComingBook();
    public abstract List<BookInfo> BookInfoFindConflictBooks(BookInfo bookInfo);
    public abstract void BookInfoSave(BookInfo bookInfo);

    /*
    EPG
     */
    public abstract EPGEvent getPresentEvent(long channelId);
    public abstract EPGEvent getFollowEvent(long channelId);
    public abstract EPGEvent getEpgByEventID(long channelId, int eventId);
    public abstract List<EPGEvent> getEPGEvents(long channelId, Date startDate, Date endDate, int offset, int onetimeObtainedNumber, int addEmpty);
    public abstract String getShortDescription(long channelId, int eventId);
    public abstract String getDetailDescription(long channelId, int eventId);
    public abstract int setEvtLang(String firstLangCode, String secLangCode);
    public abstract Date getDtvDate();
    public abstract void startScheduleEit();
    public abstract void stopScheduleEit();

    /*
    time zone
     */
    public abstract int setTime(Date date);
    public abstract int getDtvTimeZone();
    public abstract int setDtvTimeZone(int zonesecond);
    public abstract int getSettingTDTStatus();
    public abstract int setSettingTDTStatus(int onoff);
    public abstract int setTimeToSystem(boolean bSetTimeToSystem);
    public abstract int syncTime(boolean bEnable);
    public abstract int getDtvDaylight();
    public abstract int setDtvDaylight(int onoff);

    /*
    UI
     */
    public abstract void pipModSetDisplay(Context context, Surface surface, int type) ;
    public abstract void pipModClearDisplay(Surface surface) ;
    public abstract void setSurfaceView(Context context, SurfaceView surface) ;

    public void SetViewUiDisplayManager(PrimeDtv.ViewUiDisplay viewUiDisplay)
    {
        ViewUIDisplayManager = viewUiDisplay;
    }

    public PrimeDtv.ViewUiDisplay GetViewUiDisplayManager()
    {
        return ViewUIDisplayManager;
    }

    public void SetEpgUiDisplayManager(PrimeDtv.EpgUiDisplay epgUiDisplay)
    {
        EpgUiDisplayManager = epgUiDisplay;
    }

    public PrimeDtv.EpgUiDisplay GetEpgUiDisplayManager(int type)
    {
        return EpgUiDisplayManager;
    }

    /*
    scan
     */
    public abstract void startScan(TVScanParams sp) ;
    public abstract void VMXstartScan(TVScanParams sp, int startTPID, int endTPID) ;
    public abstract void stopScan(boolean store) ;

    /*
    tuner hw
     */
    public abstract int tunerLock(TVTunerParams tvTunerParams) ;
    public abstract int getTunerNum();
    public abstract int getTunerType() ;
    public abstract int getSignalStrength(int nTunerID);
    public abstract int getSignalQuality(int nTunerID);
    public abstract int getSignalSNR(int nTunerID);
    public abstract int getSignalBER(int nTunerID);
    public abstract boolean getTunerStatus(int tuner_id) ;
    public abstract int setFakeTuner(int openFlag);
    public abstract int tunerSetAntenna5V(int tuner_id, int onOff);
    public abstract int setDiSEqC10PortInfo(int nTuerID, int nPort, int n22KSwitch, int nPolarity);
    public abstract int setDiSEqC12MoveMotor(int nTunerId, int direct, int step);
    public abstract int setDiSEqC12MoveMotorStop(int nTunerId);
    public abstract int resetDiSEqC12Position(int nTunerId);
    public abstract int setDiSEqCLimitPos(int nTunerId, int limitType);

    /*
    tp info
     */
    public abstract List<SatInfo> satInfoGetList(int tunerType, int pos, int num);
    public abstract SatInfo satInfoGet(int satId);
    public abstract int satInfoAdd(SatInfo pSat);
    public abstract int satInfoUpdate(SatInfo pSat);
    public abstract int satInfoUpdateList(List<SatInfo> pSats);
    public abstract int satInfoDelete(int satId);
    public abstract List<TpInfo> tpInfoGetListBySatId(int tunerType, int satId, int pos, int num);
    public abstract TpInfo tpInfoGet(int tp_id);
    public abstract int tpInfoAdd(TpInfo pTp);
    public abstract int tpInfoUpdate(TpInfo pTp);
    public abstract int tpInfoUpdateList(List<TpInfo> pTps);
    public abstract int tpInfoDelete(int tpId);

    /*
    AvControl
     */
    public abstract int AvControlPlayByChannelIdFCC(int mode, List<Long> ch_id_list, List<Integer> tuner_id_list, boolean channelBlocked);
    public abstract int AvControlPlayByChannelId(int playId, long channelId, int groupType, int show);
    public abstract int AvControlPrePlayStop();
    public abstract int AvControlChangeRatioConversion(int playId, int ratio, int conversion);
    public abstract int AvControlSetFastChangeChannel(int tunerId, long chId);
    public abstract int AvControlClearFastChangeChannel(int tunerId, long chId);
    public abstract int AvControlChangeResolution(int playId, int resolution);
    public abstract int AvControlChangeAudio(int playId, AudioInfo.AudioComponent component);
    public abstract int AvControlSetVolume(int volume);
    public abstract int AvControlGetVolume();
    public abstract int AvControlSetMute(int playId, boolean mute);
    public abstract int AvControlSetTrackMode(int playId, EnAudioTrackMode stereo);
    public abstract int AvControlAudioOutput(int playId, int byPass);
    public abstract int AvControlClose(int playId);
    public abstract int AvControlOpen(int playId);
    public abstract int AvControlShowVideo(int playId, boolean show);
    public abstract int AvControlFreezeVideo(int playId, boolean freeze);
    public abstract AudioInfo AvControlGetAudioListInfo(int playId);
    public abstract int AvControlGetPlayStatus(int playId);
    public abstract int AvControlSetPlayStatus(int status);
    public abstract boolean AvControlGetMute(int playId);
    public abstract EnAudioTrackMode AvControlGetTrackMode(int playId);
    public abstract int AvControlGetRatio(int playId);
    public abstract int AvControlSetStopScreen(int playId, int stopType);
    public abstract int AvControlGetStopScreen(int playId);
    public abstract int AvControlGetFPS(int playId);
    public abstract int AvControlEwsActionControl(int playId, boolean enable);
    public abstract int AvControlSetWindowSize(int playId, Rect rect);
    public abstract Rect AvControlGetWindowSize(int playId);
    public abstract int AvControlGetVideoResolutionHeight(int playId);
    public abstract int AvControlGetVideoResolutionWidth(int playId);
    public abstract int AvControlGetDolbyInfoStreamType(int playId);
    public abstract int AvControlGetDolbyInfoAcmod(int playId);
    public abstract SubtitleInfo.SubtitleComponent AvControlGetCurrentSubtitle(int playId);
    public abstract SubtitleInfo AvControlGetSubtitleList(int playId);
    public abstract int AvControlSelectSubtitle(int playId, SubtitleInfo.SubtitleComponent subtitleComponent);
    public abstract int AvControlShowSubtitle(int playId, boolean enable);
    public abstract boolean AvControlIsSubtitleVisible(int playId);
    public abstract int AvControlSetSubtHiStatus(int playId, boolean hiOn);
    public abstract int AvControlSetSubtitleLanguage(int playId, int index, String lang);
    public abstract TeletextInfo AvControlGetCurrentTeletext(int playId);
    public abstract List<TeletextInfo> AvControlGetTeletextList(int playId);
    public abstract int AvControlShowTeletext(int playId, boolean enable);
    public abstract boolean AvControlIsTeletextVisible(int playId);
    public abstract boolean AvControlIsTeletextAvailable(int playId);
    public abstract int AvControlSetTeletextLanguage(int playId, String primeLang);
    public abstract String AvControlGetTeletextLanguage(int playId);
    public abstract int AvControlSetCommand(int playId, int keyCode);
    public abstract Date AvControlGetTimeShiftBeginTime(int playId);
    public abstract Date AvControlGetTimeShiftPlayTime(int playId);
    public abstract int AvControlGetTimeShiftRecordTime(int playId);
    public abstract int AvControlGetTrickMode(int playId);
    public abstract int AvControlTimeshiftTrickPlay(int playId, int trickMode);
    public abstract int AvControlTimeshiftPausePlay(int playId);
    public abstract int AvControlTimeshiftPlay(int playId);
    public abstract int AvControlTimeshiftSeekPlay(int playId, long seekTime);
    public abstract int AvControlStopTimeShift(int playId);

    /*
    PVR
     */
    //Start - Series Record
    public abstract int PvrInit(String usbMountPath);
    public abstract int PvrDeinit();
    public abstract int PvrRecordStart(PvrRecStartParam startParam,int tunerId);
    public abstract int PvrRecordStop(int recId);
    public abstract void PvrRecordStopAll();
    public abstract int PvrRecordGetRecTimeByRecId(int recId);
    public abstract boolean PvrPlayCheckLastPositionPoint(PvrRecIdx recIdx);
    public abstract int PvrPlayFileStart(PvrRecIdx recIdx, boolean lastPositionFlag, int tunerId);
    public abstract int PvrPlayFileStop(int playId);
    public abstract int PvrPlayPlay(int playId);
    public abstract int PvrPlayPause(int playId);
    public abstract int PvrPlayFastForward(int playId);
    public abstract int PvrPlayRewind(int playId);
    public abstract int PvrPlaySeek(int playId, PvrInfo.EnSeekMode seekMode, int offsetSec);
    public abstract int PvrPlaySetSpeed(int playId, PvrInfo.EnPlaySpeed playSpeed);
    public abstract PvrInfo.EnPlaySpeed PvrPlayGetSpeed(int playId);
    public abstract int PvrPlayChangeAudioTrack(int playId, AudioInfo.AudioComponent audioComponent);
    public abstract int PvrPlayGetCurrentAudioIndex(int playId);
    public abstract int PvrPlayGetPlayTime(int playId);
    public abstract PvrInfo.EnPlayStatus PvrPlayGetPlayStatus(int playId);
    public abstract PvrInfo.PlayTimeInfo PvrPlayGetPlayTimeInfo(int playId);
    public abstract int PvrTimeShiftStart(ProgramInfo programInfo,int recordTunerId,int playTunerId);
    public abstract int PvrTimeShiftStop();
    public abstract int PvrTimeShiftPlayStart(int tunerId);
    public abstract int PvrTimeShiftPlayPause(int tunerId);
    public abstract int PvrTimeShiftPlayResume(int tunerId);
    public abstract int PvrTimeShiftPlayFastForward(int playId);
    public abstract int PvrTimeShiftPlayRewind(int playId);
    public abstract int PvrTimeShiftPlaySeek(int playId, PvrInfo.EnSeekMode seekMode, int startPosition);
    public abstract int PvrTimeShiftPlaySetSpeed(int playId, PvrInfo.EnPlaySpeed playSpeed);
    public abstract PvrInfo.EnPlaySpeed PvrTimeShiftPlayGetSpeed(int playId);
    public abstract int PvrTimeShiftPlayChangeAudioTrack(int playId, AudioInfo.AudioComponent audioComponent);
    public abstract int PvrTimeShiftPlayGetCurrentAudioIndex(int playId);
    public abstract PvrInfo.EnPlayStatus PvrTimeShiftPlayGetStatus(int playId);
    public abstract PvrInfo.PlayTimeInfo PvrTimeShiftGetTimeInfo(int playId);
    public abstract PvrRecFileInfo PvrGetFileInfoByIndex(PvrRecIdx tableKeyInfo);
    public abstract int PvrGetRecCount();
    public abstract int PvrGetSeriesRecCount(int masterIndex);
    public abstract List<PvrRecFileInfo> PvrGetRecLink(int startIndex, int count);
    public abstract List<PvrRecFileInfo> PvrGetPlaybackLink(int startIndex, int count);
    public abstract List<PvrRecFileInfo> PvrGetSeriesRecLink(PvrRecIdx tableKeyInfo, int count);
    public abstract int PvrDelAllRecs();
    public abstract int PvrDelSeriesRecs(int masterIndex);
    public abstract int PvrDelRecsByChId(int channelId);
    public abstract int PvrDelOneRec(PvrRecIdx tableKeyInfo);
    public abstract int PvrDelOnePlayback(PvrRecIdx tableKeyInfo);
    public abstract int PvrCheckSeriesEpisode(byte[] seriesKey, int episode);
    public abstract int PvrCheckSeriesComplete(byte[]  seriesKey);
    public abstract boolean PvrIsIdxInUse(PvrRecIdx pvrRecIdx);
    // End - Series Record


    /*
    OTA
     */
    public abstract int UpdateUsbSoftWare(String filename);
    public abstract int UpdateFileSystemSoftWare(String pathAndFileName, String partitionName);
    public abstract int UpdateOTADVBCSoftWare(int tpId, int freq, int symbol, int qam);
    public abstract int UpdateOTADVBTSoftWare(int tpId, int freq, int bandwith, int qam, int priority);
    public abstract int UpdateOTADVBT2SoftWare(int tpId, int freq, int symbol, int qam, int channelmode);
    public abstract int UpdateOTAISDBTSoftWare(int tpId, int freq, int bandwith, int qam, int priority);
    public abstract int UpdateMtestOTASoftWare();
    public abstract OTACableParameters DVBGetOTACableParas();
    public abstract OTATerrParameters DVBGetOTAIsdbtParas();
    public abstract OTATerrParameters DVBGetOTATerrestrialParas();
    public abstract OTATerrParameters DVBGetOTADVBT2Paras();

    /*
    MTest
     */
    public abstract void PESI_CMD_CallBackTest(int hiSvrEvtAvPlaySuccess);
    public abstract int getTempPesiDefaultChannelFlag();
    public abstract void setTempPesiDefaultChannelFlag(int flag);
    public abstract int MtestGetGPIOStatus(int u32GpioNo);
    public abstract int MtestSetGPIOStatus(int u32GpioNo, int bHighVolt);
    public abstract int MtestGetATRStatus(int smartCardStatus);
    public abstract int MtestGetHDCPStatus();
    public abstract int MtestGetHDMIStatus();
    public abstract int MtestPowerSave();
    public abstract int MtestSevenSegment(int enable);
    public abstract int MtestSetAntenna5V(int tunerID, int tunerType, int enable);
    public abstract int MtestSetBuzzer(int enable);
    public abstract int MtestSetLedRed(int enable);
    public abstract int MtestSetLedGreen(int enable);
    public abstract int MtestSetLedOrange(int enable);
    public abstract int MtestSetLedWhite(int enable);
    public abstract int MtestSetLedOnOff(int status);
    public abstract int MtestGetFrontKey(int key);
    public abstract int MtestSetUsbPower(int enable);
    public abstract int MtestTestUsbReadWrite(int portNum, String path);
    public abstract int MtestTestAvMultiPlay(int tunerNum, List<Integer> tunerIDs, List<Long> channelIDs);
    public abstract int MtestTestAvStopByTunerID(int tunerID);
    public abstract int MtestMicSetInputGain(int value);
    public abstract int MtestMicSetLRInputGain(int l_r, int value);
    public abstract int MtestMicSetAlcGain(int value);
    public abstract int MtestGetErrorFrameCount(int tunerID);
    public abstract int MtestGetFrameDropCount(int tunerID);
    public abstract String MtestGetChipID();
    public abstract int MtestStartMtest(String version);
    public abstract int MtestConnectPctool();
    public abstract List<Integer> MtestGetWiFiTxRxLevel();
    public abstract int MtestGetWakeUpMode();
    public abstract Map<String, Integer> MtestGetKeyStatusMap();
    public abstract int MtestEnableOpt(boolean enable);
    public abstract void TestSetTvRadioCount(int tvCount, int radioCount);
    public abstract int TestChangeTuner(int tunerType);

    /*
    PIP
     */
    public abstract int PipOpen(int x, int y, int width, int height);
    public abstract int PipClose();
    public abstract int PipStart(long channelId, int show);
    public abstract int PipStop();
    public abstract int PipSetWindow(int x, int y, int width, int height);
    public abstract int PipExChange();

    /*
    USB
     */
    public abstract List<Integer> GetUsbPortList();

    /*
    VMX
     */
    public abstract LoaderInfo GetLoaderInfo();
    public abstract CaStatus GetCAStatusInfo();
    public abstract int GetECMcount();
    public abstract int GetEMMcount();
    public abstract String GetLibDate();
    public abstract String GetChipID();
    public abstract String GetSN();
    public abstract String GetCaVersion();
    public abstract String GetSCNumber();
    public abstract int GetPairingStatus();
    public abstract String GetPurse();
    public abstract int GetGroupM();
    public abstract int SetPinCode(String pinCode, int pinIndex, int textSelect);
    public abstract int SetPPTV(String pinCode, int pinIndex);
    public abstract void SetOMSMok();
    public abstract void VMXTest(int mode);
    public abstract void TestVMXOTA(int mode);
    public abstract void VMXAutoOTA(int otaMode, int triggerID, int triggerNum, int tunerId, int satId, int dsmccPid, int freqNum, ArrayList<Integer> freqList, ArrayList<Integer> bandwidthList);
    public abstract String VMXGetBoxID();
    public abstract String VMXGetVirtualNumber();
    public abstract void VMXStopEWBS(int mode);
    public abstract void VMXStopEMM();
    public abstract void VMXOsmFinish(int triggerID, int triggerNum);
    public abstract VMXProtectData GetProtectData();
    public abstract int SetProtectData(int first, int second, int third);

    /*
    widevine cas
     */
    public abstract void WidevineCasSessionId(int sessionIndex, int sessionId);

    /*
    other
    */
    public abstract void EnterViewActivity(int enter);
    public abstract void EnableMemStatusCheck(int enable);
    public abstract int LoaderDtvGetJTAG();
    public abstract int LoaderDtvSetJTAG(int value);
    public abstract int LoaderDtvCheckISDBTService(OTATerrParameters ota);
    public abstract int LoaderDtvCheckTerrestrialService(OTATerrParameters ota);
    public abstract int LoaderDtvCheckCableService(OTACableParameters ota);
    public abstract int LoaderDtvGetSTBSN();
    public abstract int LoaderDtvGetChipSetId();
    public abstract int LoaderDtvGetSWVersion();
    public abstract int InvokeTest();
    public abstract int set_net_stream_info(int groupType, NetProgramInfo netStreamInfo);
    public abstract int reset_net_program_database();
    public abstract boolean init_net_program_database();
}
