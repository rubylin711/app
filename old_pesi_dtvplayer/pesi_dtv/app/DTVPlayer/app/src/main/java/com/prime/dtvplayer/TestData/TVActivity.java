package com.prime.dtvplayer.TestData;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import com.dolphin.dtv.EnTableType;
import com.dolphin.dtv.EnTrickMode;
import com.prime.dtvplayer.Sysdata.CaStatus;
import com.prime.dtvplayer.Sysdata.LoaderInfo;
import com.dolphin.dtv.PVREncryption;
import com.dolphin.dtv.PvrFileInfo;
import com.dolphin.dtv.Resolution;
import com.prime.dtvplayer.Sysdata.AudioInfo;
import com.prime.dtvplayer.Sysdata.BookInfo;
import com.prime.dtvplayer.Sysdata.DefaultChannel;
import com.prime.dtvplayer.Sysdata.EPGEvent;
import com.prime.dtvplayer.Sysdata.EnAudioTrackMode;
import com.prime.dtvplayer.Sysdata.FavInfo;
import com.prime.dtvplayer.Sysdata.GposInfo;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.PvrInfo;
import com.prime.dtvplayer.Sysdata.SatInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;
import com.prime.dtvplayer.Sysdata.SubtitleInfo;
import com.prime.dtvplayer.Sysdata.TeletextInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.TestData.tvclient.TestDataTVClient;
import com.prime.dtvplayer.utils.TVMessage;
import com.prime.dtvplayer.utils.TVScanParams;
import com.prime.dtvplayer.utils.TVTunerParams;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by ethan_lin on 2017/10/26.
 */

public abstract class TVActivity extends Activity {
    private static final String TAG="TVActivity";
    private static int ChannelIsExist = 0;
    private static int TUNER_TYPE = TpInfo.DVBC;
    private static int PLATFORM = 0;
    private static int tempPesiDefaultChannelFlag = 0;
    private static String RecordPath = "/mnt/sdcard";
    abstract public void onConnected();

    abstract public void onDisconnected();

    abstract public void onMessage(TVMessage msg);


    //private TVClient client = new TVClient() {
    private TestDataTVClient client = new TestDataTVClient() {
        @Override
        public void onConnected() {
            TVActivity.this.onConnected();
        }

        @Override
        public void onDisconnected() {
            TVActivity.this.onDisconnected();
        }

        @Override
        public void onMessage(TVMessage message) {
            TVActivity.this.onMessage(message);
        }
    };


    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        client.ConnectService(this);
        client.Program.UpdateCurPlayChannelList(0);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
    }
    protected void onDestroy() {
        super.onDestroy();
    }


    public void setSurfaceView(SurfaceView surface)
    {

    }

    public void startScan(TVScanParams sp) {
        Log.d(TAG, "startScan");
        client.StartScan(sp);
    }

    public void stopScan(boolean store) {
        Log.d(TAG, "stopScan");
        client.StopScan(store);
    }
	//ethan 20171201 modify TuneFrontEnd
    public int TuneFrontEnd(TVTunerParams tvTunerParams){
        Log.d(TAG, "TundFrontEnd: ");
        return client.TuneFrontEnd(tvTunerParams);
    }
    public int TunerGetStrength(int tuner_id){
        Log.d(TAG, "TeunrGetStrength");
        int strength = client.getStrength(tuner_id);

        return strength;
    }
    public int TunerGetQuality(int tuner_id){
        Log.d(TAG, "TeunrGetQuality");
        int Quality = client.getQuality(tuner_id);

        return Quality;
    }
    public int TunerGetLockStatus(int tuner_id){
        //Log.d(TAG, "TeunrGetLockStatus");
        int lockStatus = client.getLockStatus(tuner_id);

        return lockStatus;
    }
    public int TunerGetBER(int tuner_id){
        Log.d(TAG, "TunerGetBER");
        int BER = client.getBER(tuner_id);

        return BER;
    }
    public int TunerGetSNR(int tuner_id){
        Log.d(TAG, "TunerGetSNR");
        int SNR = client.getSNR(tuner_id);

        return SNR;
    }
    public int setFakeTuner(int openFlag)//Scoty 20180809 add fake tuner command
    {
        return 1;
    }
    public int TunerSetAntenna5V(int tuner_id,int onOff){
        Log.d(TAG, "TunerSetAntenna5V");
        return 0;
    }

    public void ResetFactoryDefault(){
        Log.d(TAG, "ResetFactoryDefault: ");
        client.ResetFactoryDefault();
    }

    public GposInfo GposInfoGet() {
        return client.Gpos.GetGposInfo();
    }

    public void GposInfoUpdate(GposInfo gPos) {
        client.Gpos.Save(gPos);
    }

    /*public List<AntInfo> AntInfoGetList() {
        return client.Ant.GetAntInfoList();
    }

    public AntInfo AntInfoGet(int antId) {
        return client.Ant.GetAntInfo(antId);
    }

    public void AntInfoSave(AntInfo pant) {
        client.Ant.Save(pant);
    }

    public void AntInfoSaveList(List<AntInfo> pants) {
        client.Ant.Save(pants);
    }

    public void AntInfoDelete(int antId) {
        client.Ant.Delete(antId);
    }*/

    public List<SatInfo> SatInfoGetList(int tunerType,int pos,int num) {
        List<SatInfo> SatList = client.Sat.GetSatinfoList();

        if ( SatList == null )  // johnny add 20171219
        {
            return null;
        }

        for(int i = 0; i < SatList.size(); i++) {
            List<TpInfo> TpList = TpInfoGetListBySatId(tunerType,SatList.get(i).getSatId(),0,0);

            if ( TpList == null )   // johnny add 20171225
            {
                TpList = new ArrayList<>();
            }

            List<Integer> TpIdList = new ArrayList<>();
            for(int j = 0; j < TpList.size(); j++) {
                TpIdList.add(TpList.get(j).getTpId());
            }
            SatList.get(i).setTps(TpIdList);
        }
        return SatList;
    }

    public SatInfo SatInfoGet(int satId) {
        SatInfo Sat = client.Sat.GetSatInfo(satId);

        if ( Sat == null )  // johnny add 20171219
        {
            return null;
        }

        List<TpInfo> TpList = TpInfoGetListBySatId(-1,Sat.getSatId(),0,0);  // Johnny modify 20180119

        if ( TpList == null )   // johnny add 20171225
        {
            TpList = new ArrayList<>();
        }

        List<Integer> TpIdList = new ArrayList<>();
        for(int j = 0; j < TpList.size(); j++) {
            TpIdList.add(TpList.get(j).getTpId());
        }

        Sat.setTps(TpIdList);
        return Sat;
    }
/*
    public void SatInfoSave(SatInfo pSat) {
        client.Sat.Save(pSat);
    }
*/

    public int SatInfoAdd(SatInfo pSat) {
        client.Sat.Add(pSat);
        return 0;
    }

    public int SatInfoUpdate(SatInfo pSat) {
        client.Sat.Update(pSat);
        return 0;
    }

    public int SatInfoUpdateList(List<SatInfo> pSats) {
        client.Sat.Save(pSats);
        return 0;
    }

    public int SatInfoDelete(int satId) {
        client.Sat.Delete(satId);
        return 0;
    }
/*
    public List<TpInfo> TpInfoGetList(int tunerType) {
        return client.Tp.GetTpInfoList(tunerType);
    }
*/
    public List<TpInfo> TpInfoGetListBySatId(int tunerType,int satId,int pos,int num) {
        if(tunerType == -1) // Johnny modify 20180119
        return client.Tp.GetTpInfoListBySatId(satId);
        else
            return client.Tp.GetTpInfoList(tunerType);
    }

    public TpInfo TpInfoGet(int tp_id) {
        return client.Tp.GetTpInfo(tp_id);
    }

    public int TpInfoAdd(TpInfo pTp) {
        client.Tp.Add(pTp);
        return 0;
    }

    public int TpInfoUpdate(TpInfo pTp) {
        client.Tp.Update(pTp);
        return 0;
    }

    public int TpInfoUpdateList(List<TpInfo> pTps) {
        client.Tp.Save(pTps);
        return 0;
    }

    public int TpInfoDelete(int tpId) {
        client.Tp.Delete(tpId);
        return 0;
    }

    public List<ProgramInfo> ProgramInfoGetList(int type,int pos,int num) {
        return client.Program.GetProgramInfoList(type);
    }

    public ProgramInfo ProgramInfoGetByChannelId(long channelId) {
        return client.Program.GetProgramByChannelId(channelId);
    }

    public ProgramInfo ProgramInfoGetByChnum(int chnum, int type) {
        return client.Program.GetProgramByChnum(chnum,type);
    }

    public ProgramInfo ProgramInfoGetByLcn(int lcn, int type) {
        return client.Program.GetProgramByLcn(lcn,type);
    }

    public int ProgramInfoUpdate(ProgramInfo pProgram) {
        client.Program.Save(pProgram);
        return 0;
    }

    public int ProgramInfoUpdateList(List<ProgramInfo> pPrograms) {
        client.Program.Save(pPrograms);
        return 0;
    }

    public int ProgramInfoUpdateList(List<SimpleChannel> pPrograms,int type) {
        return client.Program.saveSimpleChannelList(pPrograms,type);
//        client.Program.Save(pPrograms,type);
//        return 0;
    }

    public int ProgramInfoDelete(long channelId) {
        client.Program.Delete(channelId);
        return 0;
    }

    public int ProgramInfoDeleteAllByType(int type) {
        client.Program.DeleteAll(type);
        return 0;
    }

    public int setDefaultOpenChannel(long channelId, int groupType)
    {
        client.DefaultChannel.SetDefaultChannel(channelId,groupType);
        return 0;
    }

    public DefaultChannel getDefaultOpenChannel()
    {
        return client.DefaultChannel.GetDefaultChannel();
    }

    public int ProgramInfoGetChannelNumInGroup(int type) {
        return 0;
    }

    public int ProgramInfoDelChannelByTag(int tag) {
        return 0;
    }

    public int ProgramInfoGetChannelNO(long channelId) {
        return 0;
    }

    public List<SimpleChannel> ProgramInfoGetSimpleChannelList(int type,int IncludeSkipFlag,int IncludePVRSkipFlag) {
        return client.Program.GetSimpleProgramList(type);
    }

    public SimpleChannel ProgramInfoGetSimpleChannelByChannelId(long channelId) {
        return client.Program.GetSimpleProgramByChannelId(channelId);
    }

    public List<SimpleChannel> ProgramInfoGetListByFilter(int filterTag, int serviceType, String keyword) {
        List<SimpleChannel> test = new ArrayList<>();
        return test;
    }

    public List<FavInfo> FavInfoGetList(int favMode) {
        return client.Fav.GetFavInfoList(favMode);
    }

    public FavInfo FavInfoGet(int favMode,int index) {
        return client.Fav.GetFavInfo(favMode,index);
    }

    public int FavInfoUpdate(FavInfo favInfo) {
        client.Fav.Save(favInfo);
        return 0;
    }

    public int FavInfoUpdateList(List<FavInfo> favInfo) {
        client.Fav.Save(favInfo);
        return 0;
    }

    public int FavInfoDelete(int favMode, long channelId) {
        client.Fav.Delete(favMode,channelId);
        return 0;
    }

    public int FavInfoDeleteAll(int favMode) {
        client.Fav.DeleteAll(favMode);
        return 0;
    }

    public String FavGroupNameGet(int favMode) {
        return client.FavName.GetFavGroupName(favMode);
    }

    public int FavGroupNameUpdate(int favMode, String name) {
        client.FavName.Save(favMode,name);
        return 0;
    }

    public List<BookInfo> BookInfoGetList() {
        return client.Book.GetBookInfoList();
    }

    public BookInfo BookInfoGet(int bookId) {
        return client.Book.GetBookInfo(bookId);
    }

    public int BookInfoAdd(BookInfo bookInfo) {
        client.Book.Add(bookInfo);
        return 0;
    }

    public int BookInfoUpdate(BookInfo bookInfo) {
        client.Book.Update(bookInfo);
        return 0;
    }

    public int BookInfoUpdateList(List<BookInfo> bookInfo) {
        client.Book.Save(bookInfo);
        return 0;
    }

    public int BookInfoDelete(int bookId) {
        client.Book.Delete(bookId);
        return 0;
    }

    public int BookInfoDeleteAll() {
        client.Book.DeleteAll();
        return 0;
    }

    public BookInfo BookInfoGetComingBook() {
        BookInfo bookInfo = null;
        return bookInfo;
    }

    public List<BookInfo> BookInfoFindConflictBooks(BookInfo bookInfo) {
        List<BookInfo> bookInfoList = null;
        return bookInfoList;
    }

    public EPGEvent EpgEventGetPresentEvent(long channelId) {
        //should remove in hisillion !!-s
        int sid = client.Program.GetProgramByChannelId(channelId).getServiceId();
        int tsid = client.Program.GetProgramByChannelId(channelId).getTransportStreamId();
        int onid = client.Program.GetProgramByChannelId(channelId).getOriginalNetworkId();
        List<EPGEvent> eventList = client.EpgEvent.getEventPF(sid,tsid,onid);

        if((eventList == null) || (eventList.size() == 0))
            return null;
        else {
            for (int i = 0; i < eventList.size(); i++) {
                if (eventList.get(i).getEventType() == EPGEvent.EPG_TYPE_PRESENT)
                    return eventList.get(i);
            }
            return null;
        }
        //should remove in hisillion !!-e
    }

    public EPGEvent EpgEventGetFollowEvent(long channelId) {
        //should remove in hisillion !!-s
        int sid = client.Program.GetProgramByChannelId(channelId).getServiceId();
        int tsid = client.Program.GetProgramByChannelId(channelId).getTransportStreamId();
        int onid = client.Program.GetProgramByChannelId(channelId).getOriginalNetworkId();
        List<EPGEvent> eventList = client.EpgEvent.getEventPF(sid,tsid,onid);

        if((eventList == null) || (eventList.size() == 0))
            return null;
        else
        {
            for (int i = 0; i < eventList.size(); i++) {
                if (eventList.get(i).getEventType() == EPGEvent.EPG_TYPE_FOLLOW)
                    return eventList.get(i);
            }
            return null;
        }
        //should remove in hisillion !!-e
    }

    public EPGEvent EpgEventGetEPGByEventId(long channelId, int eventId) {
        //should remove in hisillion !!-s
        int sid = client.Program.GetProgramByChannelId(channelId).getServiceId();
        int tsid = client.Program.GetProgramByChannelId(channelId).getTransportStreamId();
        int onid = client.Program.GetProgramByChannelId(channelId).getOriginalNetworkId();
        List<EPGEvent> eventList = client.EpgEvent.getEventSchedule(sid,tsid,onid);

        if((eventList == null) || (eventList.size() == 0))
            return null;
        else {
            for (EPGEvent event : eventList) {
                if (event.getEventId() == eventId) {
                    return event;
                }
            }
            return null;
        }
        //should remove in hisillion !!-e
    }

    public List<EPGEvent> EpgEventGetEPGEventList(long channelId, long startTime, long endTime, int addEmpty) {
        //should remove in hisillion !!-s
        int sid = client.Program.GetProgramByChannelId(channelId).getServiceId();
        int tsid = client.Program.GetProgramByChannelId(channelId).getTransportStreamId();
        int onid = client.Program.GetProgramByChannelId(channelId).getOriginalNetworkId();
        List<EPGEvent> eventList = client.EpgEvent.getEventSchedule(sid,tsid,onid, startTime, endTime, addEmpty);
        if((eventList == null) || (eventList.size() == 0))
            return null;
        else
            return eventList;
        //should remove in hisillion !!-e
    }

    public String EpgEventGetShortDescription(long channelId, int eventId) {
        return null;
    }

    public String EpgEventGetDetailDescription(long channelId, int eventId) {
        return null;
    }

    public int EpgEventSetLanguageCode(String FirstLangCode, String SecLangCode) {
        return 0;
    }

    public int EpgEventStartEPG(long channelId) {
        return 0;
    }

    public int AvControlPlayByChannelId(int playId,long channelId, int groupType, int show) {
        return 0;
    }

    public int AvControlPlayStop(int playId) {
        return 0;
    }

    public int AvControlChangeRatioConversion(int playId,int ratio,int conversion) {
        return 1;
    }

    public int AvControlChangeRatio(int playId,int ratio) {
        return 0;
    }

    public int AvControlChangeConversion(int playId,int conversion) {
        return 0;
    }

    public int AvControlChangeResolution(int playId,int resolution) {
        return 0;
    }

    public int AvControlChangeAudio(int playId,AudioInfo.AudioComponent component) {
        return 0;
    }

    public int AvControlSetVolumn(int playId,int volumn) {
        return 0;
    }

    public int AvControlSetMute(int playId,boolean mute) {
        return 0;
    }

    public int AvControlSetTrackMode(int playId, EnAudioTrackMode mode) {
        return 0;
    }

    public int AvControlAudioOutput(int playId,int byPass) {
        return 0;
    }

    public int AvControlClose(int playId) {
        return 0;
    }

    public int AvControlOpen(int playId) {
        return 0;
    }

    public int AvControlShowVideo(int playId,boolean show) {
        return 0;
    }

    public int AvControlFreezeVideo(int playId,boolean freeze) {
        return 0;
    }

    //return value shoulde be check
    public AudioInfo AvControlGetAudioListInfo(int playId) {
        return null;
    }

    /* return status =  LIVEPLAY,TIMESHIFTPLAY.....etc  */
    public int AvControlGetPlayStatus(int playId) {
        return 0;
    }

    public boolean AvControlGetMute(int playId) {
        return false;
    }

    public EnAudioTrackMode AvControlGetTrackMode(int playId) {
        return EnAudioTrackMode.MPEG_AUDIO_TRACK_STEREO;
    }

    public int AvControlSetStopScreen(int playId,int stopType) {
        return 0;
    }

    public int AvControlGetStopScreen(int playId) {
        return 0;
    }

    public int AvControlGetFPS(int playId) {
        return 0;
    }

    public int AvControlEwsActionControl(int playId) {
        return 0;
    }

    //input value shoulde be check
    public int AvControlSetWindowSize(int playId,Rect rect) {
        return 0;
    }

    //return value shoulde be check
    public Rect AvControlGetWindowSize(int playId) {
        return null;
    }
        
    public int AvControlGetVideoResolutionHeight(int playId) {
        return 0;
    }

    public int AvControlGetVideoResolutionWidth(int playId) {
        return 0;
    }

    /* 0: dolby digital, 1: dolby digital plus */
    public int AvControlGetDolbyInfoStreamType(int playId) {
        return 0;
    }

    public int AvControlGetDolbyInfoAcmod(int playId) {
        return 0;
    }

    public SubtitleInfo.SubtitleComponent AvControlGetCurrentSubtitle(int playId) {
        SubtitleInfo.SubtitleComponent subtitleInfo = null;
        return subtitleInfo;
    }

    public SubtitleInfo AvControlGetSubtitleList(int playId) {
        SubtitleInfo subtitleInfo = null;
        return subtitleInfo;
    }

    public int AvControlSelectSubtitle(int playId,SubtitleInfo.SubtitleComponent subtitleComponent) {
        return 0;
    }

    public int AvControlShowSubtitle(int playId,boolean enable) {
        return 0;
    }

    public boolean AvControlIsSubtitleVisible(int playId) {
        return false;
    }

    public int AvControlSetSubtHiStatus(int playId,boolean HiOn) {
        return 0;
    }

    public TeletextInfo AvControlGetCurrentTeletext(int playId) {
        return null;
    }

    public List<TeletextInfo> AvControlGetTeletextList(int playId) {
        return null;
    }

    public int AvControlShowTeletext(int playId,boolean enable) {
        return 0;
    }

    public boolean AvControlIsTeletextVisible(int playId) {
        return false;
    }

    public boolean AvControlIsTeletextAvailable(int playId) {
        return false;
    }

    public int AvControlSetTeletextLanguage(int playId,String primeLang) {
        return 0;
    }
    public String AvControlGetTeletextLanguage(int playId) {//eric lin 20180705 get ttx lang
        return "";
    }

    public int AvControlSetCommand(int playId,int keyCode) {
        return 0;
    }

    public Date AvControlGetTimeShiftBeginTime(int playId) {
        return null;
    }

    public Date AvControlGetTimeShiftPlayTime(int playId) {
        return null;
    }

    public int AvControlGetTimeShiftRecordTime(int playId) {
        return 0;
    }

    public int AvControlGetTrickMode(int playId) {
        return 0;
    }

    public int AvControlTimeshiftTrickPlay(int playId,int trickMode) {
        return 0;
    }

    public int AvControlTimeshiftPausePlay(int playId) {
        return 0;
    }

    public int AvControlTimeshiftPlay(int playId) {
        return 0;
    }

    public int AvControlTimeshiftSeekPlay(int playId,long seekTime) {
        return 0;
    }

    public int AvControlStopTimeShift(int playId) {
        return 0;
    }

    public int UpdateUsbSoftWare(String filename){
        return 0;
    }

    public void setChannelExist(int exist)
    {
        ChannelIsExist = exist;
    }
    public int getChannelExist()
    {
        return ChannelIsExist;
    }
    public void pipModSetDisplay(Surface surface)
    {
    }
    public int getTunerType()
    {
        return TUNER_TYPE;
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
    public void ProgramInfoPlaySimpleChannelListUpdate(int IncludePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        client.Program.UpdateCurPlayChannelList(IncludePVRSkipFlag);
    }
    public int UpdateUsbSoftWare()
    {
        return usbUpdate("DDN82-3796.bin");
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
    private int usbUpdate(String filename)
    {
        return 0;
    }
    public int DVBCOTAUpdate(int tpId, int freq, int symbol, int qam)
    {
        return 0;
    }
    public int DVBTOTAUpdate(int tpId, int freq, int bandwith, int qam, int priority){
        return 0;
    }
    public int DVBT2OTAUpdate(int tpId, int freq, int bandwith, int qam, int channelmode){
        return 0;
    }
    public int AvControlSetVolume(int volume) {
        return 0;
    }
    public int AvControlGetVolume() {
        return 0;
    }
    public int AvControlGetRatio(int playId){
        return 0;
    }
    public int AvControlEwsActionControl(int playId, boolean enable) {
        return 0;
    }
    public List<SimpleChannel> ProgramInfoGetPlaySimpleChannelList(int type, int IncludePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function
        return client.Program.GetCurPlayChannelList(type, IncludePVRSkipFlag);
    }
    public int ProgramInfoGetPlaySimpleChannelListCnt(int type) {//eric lin 20180802 check program exist
        return client.Program.GetSimpleProgramList(type,0,0).size();
    }
    public List<SimpleChannel> ProgramInfoGetSimpleChannelList(int type,int IncludeSkipFlag,int IncludePVRSkipFlag,int tpId) {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        return client.Program.GetSimpleProgramList(type,IncludeSkipFlag,IncludePVRSkipFlag);
    }
    public int SaveTable(EnTableType tableType)
    {
        return 0;
    }
    public Date getLocalTime() {
        return Calendar.getInstance().getTime();
    }
    public int setTime(Date date){
        return 0;
    }
    public int getDtvTimeZone()
    {
        //Log.d(TAG, "getDtvTimeZone");
        return 0;
    }

    public int setDtvTimeZone(int zonesecond)
    {
        //Log.d(TAG, "setDtvTimeZone");
        return 0;
    }

    public int getDtvDaylight()//value: 0(off) or 1(on)
    {
        //Log.d(TAG, "timeGetDtvDaylight");
        return 0;
    }

    public int setDtvDaylight(int onoff)//value: 0(off) or 1(on)
    {
        Log.d(TAG, "setDtvDaylight()");
        return 0;
    }

    public int getSettingTDTStatus()
    {
        //Log.d(TAG, "getSettingTDTStatus");
        return 0;
    }

    public int setSettingTDTStatus(int onoff)//value: 0(off) or 1(on)
    {
        Log.d(TAG, "setSettingTDTStatus()");
        return 0;
    }
    public int setTimeToSystem(boolean bSetTimeToSystem)
    {
        //Log.d(TAG, "setTimeToSystem bEnable=" + bEnable);
        return 0;
    }
    public int syncTime(boolean bEnable) {
        //Log.d(TAG, "syncTime bEnable=" + bEnable);
        return 0;
    }
    public String GetPesiServiceVersion() {
        return "TestData";
    }
    public String GetApkSwVersion()
    {
        return "TestData_DDN_V1.0.0";
    }
    public void UpdatePvrSkipList(int groupType, int IncludePVRSkipFlag, int tpId, List<PvrInfo> pvrList)//Scoty 20180615 update TV/Radio TotalChannelList
    {
    }
    public int MtestGetGPIOStatus(int u32GpioNo) {
        return 0;
    }
    public int MtestSetGPIOStatus(int u32GpioNo,int bHighVolt) {
        return 0;
    }
    public int MtestGetATRStatus() {
        return 0;
    }
    public int MtestGetHDCPStatus() {
        return 0;
    }
    public int MtestGetHDMIStatus() {
        return 0;
    }
    public int MtestPowerSave() {
        return 0;
    }
    public int MtestSevenSegment(int enable) {
        return 0;
    }
    public int PvrRecordStart(int pvrPlayerID , long channelID, String recordPath, int duration){
        return 0;
    }
    public int PvrRecordStart(int pvrPlayerID , long channelID, String recordPath, int duration, PVREncryption pvrEncryption){
        return 0;
    }
    public int PvrRecordStop(int pvrPlayerID, int recid)
    {
        return 0;
    }
    public int PvrRecordGetAlreadyRecTime(int pvrPlayerID, int recId){
        return 0;
    }
    public int PvrRecordGetStatus(int pvrPlayerID, int recId){
        return 0;
    }
    public int PvrTimeShiftStart(int playerID, int time, int fiesize, String filePath){
        return 0;
    }
    public String PvrRecordGetFileFullPath(int pvrPlayerID, int recId)
    {
        return "";
    }
    public int PvrRecordGetProgramId(int pvrPlayerID, int recId)
    {
        return 0;
    }
    public int PvrRecordCheck(long channelID)
    {
        return 0;
    }

    public List<PvrInfo> PvrRecordGetAllInfo()
    {
        List<PvrInfo> test = new ArrayList<>();
        return test;
    }

    public int PvrRecordGetMaxRecNum()
    {
        return 0;
    }

    public int PvrPlayFileCheckLastViewPoint(String fullName)
    {
        return 0;
    }

    public int PvrSetStartPositionFlag(int startPositionFlag)
    {
        return 0;
    }

    public int PvrTimeShiftStop(int playerID){
        return 0;
    }
    public int PvrTimeShiftPlay(int playerID){
        return 0;
    }
    public int pvrTimeShiftFilePause(int playerID){//Scoty 20180827 add and modify TimeShift Live Mode
        return 0;
    }
    public int pvrTimeShiftLivePause(int playerID){//Scoty 20180827 add and modify TimeShift Live Mode
        return 0;
    }
    public int PvrTimeShiftTrickPlay(int playerID, EnTrickMode mode){
        return 0;
    }
    public int PvrTimeShiftSeekPlay(int playerID, long seekTime){
        return 0;
    }
    public Date PvrTimeShiftGetPlayedTime(int playerID){
        return null;
    }
    public int PvrTimeShiftGetPlaySecond(int playerID){
        return 0;
    }
    public Date PvrTimeShiftGetBeginTime(int playerID){
        return null;
    }
    public int PvrTimeShiftGetBeginSecond(int playerID){
        return 0;
    }
    public int PvrTimeShiftGetRecordTime(int playerID){
        return 0;
    }
    public int PvrTimeShiftGetStatus(int playerID)
    {
        return 0;
    }
    public int PvrPlayStart(String filePath){
        return 0;
    }
    public int PvrPlayStart(String filePath, PVREncryption pvrEncryption){
        return 0;
    }
    public int PvrPlayStop(){
        return 0;
    }
    public int PvrPlayPause(){
        return 0;
    }
    public int PvrPlayResume(){
        return 0;
    }
    public int PvrPlayTrickPlay(EnTrickMode enSpeed){
        return 0;
    }
    public int PvrPlaySeekTo(int msec){
        return 0;
    }
    public int PvrPlayGetPlayTime()
    {
        return 0;
    }
    public int PvrPlayGetCurrentPosition(){
        return 0;
    }
    public EnTrickMode PvrPlayGetCurrentTrickMode(){
        return EnTrickMode.FAST_BACKWARD_EIGHT;
    }
    public int PvrPlayGetCurrentStatus()
    {
        return 0;
    }
    public long PvrPlayGetSize(){
        return 0;
    }
    public int PvrPlayGetDuration(){
        return 0;
    }
    public Resolution PvrPlayGetVideoResolution(){
        return null;
    }
    public boolean PvrPlayIsRadio(String fullName){
        return false;
    }
    public String PvrPlayGetFileFullPath(int pvrPlayerID)
    {
        return "";
    }
    public EnTrickMode PvrTimeShiftGetCurrentTrickMode(int playerID){
        return EnTrickMode.FAST_BACKWARD_EIGHT;
    }
    public AudioInfo.AudioComponent PvrPlayGetCurrentAudio(){
        return null;
    }
    public AudioInfo PvrPlayGetAudioComponents(){
        return null;
    }
    public int PvrPlaySelectAudio(AudioInfo.AudioComponent audio){
        return 0;
    }
    public int PvrPlaySetWindowRect(Rect rect){
        return 0;
    }
    public int PvrPlaySetTrackMode(EnAudioTrackMode enTrackMode){
        return 0;
    }
    public EnAudioTrackMode PvrPlayGetTrackMode(){
        return EnAudioTrackMode.MPEG_AUDIO_TRACK_STEREO;
    }
    public int PvrFileRemove(String filePath)
    {
        return 0;
    }
    public int PvrFileRename(String oldName, String newName)
    {
        return 0;
    }
    public int PvrFileGetDuration(String fullName)
    {
        return 0;
    }

    public long PvrFileGetSize(String fullName)
    {
        return 0;
    }
    public PvrFileInfo PvrFileGetAllInfo(String fullName)
    {
        return null;
    }

    public PvrFileInfo PvrFileGetExtraInfo(String fullName)
    {
        PvrFileInfo test = new PvrFileInfo();
        return test;
    }

    public int PvrSetParentLockOK()  //connie 20180806 for pvr parentalRate
    {
        return 0;
    }

    public int PvrGetCurrentPvrMode(long channelId)
    {
        return 0;
    }

    public int pvrTotalRecordFileOpen(String dirPath)
    {
        return 0;
    }

    public int pvrTotalRecordFileClose()
    {
        return 0;
    }

    public int pvrTotalRecordFileSort(int sortType)
    {
        return 0;
    }

    public List<PvrFileInfo> pvrTotalRecordFileGet(int startIndex, int total)
    {
        List<PvrFileInfo> test = new ArrayList<>();
        return test;
    }

    public int PvrGetRatio()
    {
        return 0;
    }
//    public int PvrGetCurrentRecMode()
//    {
//        return 0;
//    }

//    public int PvrGetCurrentPlayMode()
//    {
//        return 0;
//    }

    public void ResetTotalChannelList() {

    }
    public String GetRecordPath()
    {
        return RecordPath;
    }

    public void SetRecordPath(String path)
    {
        RecordPath = path;
    }

    public String getDefaultRecPath()//Scoty 20180525 add get default record path
    {
        return "/mnt/sdcard";
    }

    public int recordTS_start(int TunerId, String FullName) // connie 20180803 add record ts -s
    {
        return 0;
    }

    public int recordTS_stop()
    {
        return 0;
    }// connie 20180803 add record ts -e

    public void TestSetTvRadioCount(int tvCount, int RadioCount)
    {
    }

    public int TestChangeTuner(int tunerType)//Scoty 20180817 add Change Tuner Command
    {
        return 0;
    }
    //PIP -start
    public int PipOpen(int x, int y, int width, int height)
    {
        return 0;
    }

    public int PipClose()
    {
        return 0;
    }

    public int PipStart(long channelId, int show)
    {
        return 0;
    }

    public int PipStop()
    {
        return 0;
    }

    public int PipSetWindow(int x, int y, int width, int height)
    {
        return 0;
    }

    public int PipExChange()
    {
        return 0;
    }

    public int FrontEndSetDiSEqC10PortInfo(int nTuerID, int nPort, int n22KSwitch, int nPolarity)   // Johnny 20180814 add setDiseqc1.0 port
    {
        return 0;
    }
    //PIP -end

    // for VMX need open/close -s
    public LoaderInfo GetLoaderInfo() // connie 20180903 for VMX-s
    {
        LoaderInfo loaderInfo = new LoaderInfo();
        loaderInfo.Hardware = "1.0.0";
        loaderInfo.Software = "1.0.0";
        loaderInfo.SequenceNumber = "1";
        loaderInfo.BuildDate = "2018/01/01";
        return loaderInfo;
    }

    public CaStatus GetCAStatusInfo()
    {
        CaStatus caInfo = new CaStatus();
        caInfo.CA_status = "None";
        caInfo.Auth = "None";
        caInfo.Deauth = "None";
        return caInfo;
    }

    public int GetECMcount()
    {
        return 0;
    }

    public int GetEMMcount()
    {
        return 0;
    }

    public String GetLibDate()
    {
        return "2018/08/24";
    }

    public String GetChipID()
    {
        return "A10400000000B46F";
    }

    public String GetSN()
    {
        return "LMES0000000002";
    }

    public String GetCaVersion()
    {
        return "VC3.100_ncSs_l";
    }

    public String GetSCNumber()
    {
        return "N 7161 8017 17 3";
    }

    public int GetPairingStatus()
    {
        return 0;
    }

    public String GetPurse()
    {
        return "NoPurse";
    }

    public int SetPinCode(String pinCode)
    {
        return 1;
    }

    public int SetPPTV(String pinCode)
    {
        return 1;
    }

    public void SetOMSMok()
    {
    }

    public void VMXTest(int mode)
    {
    }

    public void VMXStopEMM()
    {
    }// connie 20180903 for VMX-e

    public void VMXOsmFinish(int triggerID, int triggerNum)
    {

    }// for VMX need open/close -e
}
