package com.prime.tvactivity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import com.prime.sysdata.AudioInfo;
import com.prime.sysdata.BookInfo;
import com.prime.sysdata.DefaultChannel;
import com.prime.sysdata.EPGEvent;
import com.prime.sysdata.FavInfo;
import com.prime.sysdata.GposInfo;
import com.prime.sysdata.ProgramInfo;
import com.prime.sysdata.Rect;
import com.prime.sysdata.SatInfo;
import com.prime.sysdata.SimpleChannel;
import com.prime.sysdata.SubtitleInfo;
import com.prime.sysdata.TeletextInfo;
import com.prime.sysdata.TpInfo;
import com.prime.tvclient.TestDataTVClient;
import com.prime.utils.TVMessage;
import com.prime.utils.TVScanParams;
import com.prime.utils.TVTunerParams;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ethan_lin on 2017/10/26.
 */

public abstract class TVActivity extends Activity {
    private static final String TAG="TVActivity";
    private int ChannelIsExist = 0;

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

        @Override
        public void ConnectDB(Context context) {
        }
    };


    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        client.ConnectService(this);
    }
    protected void onDestroy() {
        client.ServiceDisconnect(this);

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
        Log.d(TAG, "TeunrGetLockStatus");
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

    public void GposInfoSave(GposInfo gPos) {
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

    public int SatInfoSaveList(List<SatInfo> pSats) {
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

    public int TpInfoSaveList(List<TpInfo> pTps) {
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

    public ProgramInfo ProgramInfoGetByChannelId(int channelId) {
        return client.Program.GetProgramByChannelId(channelId);
    }

    public ProgramInfo ProgramInfoGetByChnum(int chnum, int type) {
        return client.Program.GetProgramByChnum(chnum,type);
    }

    public ProgramInfo ProgramInfoGetByLcn(int lcn, int type) {
        return client.Program.GetProgramByLcn(lcn,type);
    }

    public int ProgramInfoSave(ProgramInfo pProgram) {
        client.Program.Save(pProgram);
        return 0;
    }

    public int ProgramInfoSaveList(List<ProgramInfo> pPrograms) {
        client.Program.Save(pPrograms);
        return 0;
    }

    public int ProgramInfoSaveList(List<SimpleChannel> pPrograms,int type) {
        client.Program.Save(pPrograms,type);
        return 0;
    }

    public int ProgramInfoDelete(int channelId) {
        client.Program.Delete(channelId);
        return 0;
    }

    public int ProgramInfoDeleteAllByType(int type) {
        client.Program.DeleteAll(type);
        return 0;
    }

    public int setDefaultOpenChannel(int channelId, int groupType)
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

    public int ProgramInfoGetChannelNO(int channelId) {
        return 0;
    }

    public List<SimpleChannel> ProgramInfoGetSimpleChannelList(int type) {
        return client.Program.GetSimpleProgramList(type);
    }

    public SimpleChannel ProgramInfoGetSimpleChannelByChannelId(int channelId) {
        return client.Program.GetSimpleProgramByChannelId(channelId);
    }

    public List<FavInfo> FavInfoGetList(int favMode) {
        return client.Fav.GetFavInfoList(favMode);
    }

    public FavInfo FavInfoGet(int favMode,int index) {
        return client.Fav.GetFavInfo(favMode,index);
    }

    public int FavInfoSave(FavInfo favInfo) {
        client.Fav.Save(favInfo);
        return 0;
    }

    public int FavInfoSaveList(List<FavInfo> favInfo) {
        client.Fav.Save(favInfo);
        return 0;
    }

    public int FavInfoDelete(int favMode, int index) {
        client.Fav.Delete(favMode,index);
        return 0;
    }

    public int FavInfoDeleteAll(int favMode) {
        client.Fav.DeleteAll(favMode);
        return 0;
    }

    public String FavGroupNameGet(int favMode) {
        return client.FavName.GetFavGroupName(favMode);
    }

    public int FavGroupNameSave(int favMode, String name) {
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

    public int BookInfoSaveList(List<BookInfo> bookInfo) {
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

    public List<BookInfo> BookInfoFindConflictBooks() {
        List<BookInfo> bookInfoList = null;
        return bookInfoList;
    }

    public EPGEvent EpgEventGetPresentEvent(int channelId) {
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

    public EPGEvent EpgEventGetFollowEvent(int channelId) {
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

    public EPGEvent EpgEventGetEPGByEventId(int channelId, int eventId) {
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

    public List<EPGEvent> EpgEventGetEPGEventList(int channelId, long startTime, long endTime) {
        //should remove in hisillion !!-s
        int sid = client.Program.GetProgramByChannelId(channelId).getServiceId();
        int tsid = client.Program.GetProgramByChannelId(channelId).getTransportStreamId();
        int onid = client.Program.GetProgramByChannelId(channelId).getOriginalNetworkId();
        List<EPGEvent> eventList = client.EpgEvent.getEventSchedule(sid,tsid,onid);
        if((eventList == null) || (eventList.size() == 0))
            return null;
        else
            return eventList;
        //should remove in hisillion !!-e
    }

    public String EpgEventGetShortDescription(int channelId, int eventId) {
        return null;
    }

    public String EpgEventGetDetailDescription(int channelId, int eventId) {
        return null;
    }

    public int EpgEventSetLanguageCode(String langCode) {
        return 0;
    }

    public int EpgEventStartEPG(int channelId) {
        return 0;
    }

    public int AvControlPlayByChannelId(int playId,int channelId, int groupType) {
        return 0;
    }

    public int AvControlPlayStop(int playId) {
        return 0;
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

    public int AvControlSetTrackMode(int playId,int stereo) {
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

    public int AvControlGetTrackMode(int playId) {
        return 0;
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

    public TeletextInfo AvControlGetCurrentTeletext() {
        return null;
    }

    public List<TeletextInfo> AvControlGetTeletextList() {
        return null;
    }

    public int AvControlShowTeletext(boolean enable) {
        return 0;
    }

    public boolean AvControlIsTeletextVisible() {
        return false;
    }

    public boolean AvControlIsTeletextAvailable() {
        return false;
    }

    public int AvControlSetTeletextLanguage(String primeLang) {
        return 0;
    }

    public int AvControlSetCommand(int keyCode) {
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

    public void setChannelExist(int exist)
    {
        ChannelIsExist = exist;
    }

}
