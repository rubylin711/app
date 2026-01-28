package com.prime.dtvplayer.Activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.webkit.WebView;

import com.dolphin.dtv.ChannelNode;
import com.dolphin.dtv.DTVMessage;
import com.dolphin.dtv.EnTableType;
import com.dolphin.dtv.EnTrickMode;
import com.dolphin.dtv.HiDtvMediaPlayer;
import com.dolphin.dtv.IDTVListener;
import com.dolphin.dtv.PVREncryption;
import com.dolphin.dtv.PvrFileInfo;
import com.dolphin.dtv.Resolution;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.loader.structure.OTACableParameters;
import com.loader.structure.OTATerrParameters;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.AudioInfo;
import com.prime.dtvplayer.Sysdata.BookInfo;
import com.prime.dtvplayer.Sysdata.CaInfo;
import com.prime.dtvplayer.Sysdata.CaStatus;
import com.prime.dtvplayer.Sysdata.DefaultChannel;
import com.prime.dtvplayer.Sysdata.EPGEvent;
import com.prime.dtvplayer.Sysdata.EnAudioTrackMode;
import com.prime.dtvplayer.Sysdata.FavGroupName;
import com.prime.dtvplayer.Sysdata.FavInfo;
import com.prime.dtvplayer.Sysdata.GposInfo;
import com.prime.dtvplayer.Sysdata.LoaderInfo;
import com.prime.dtvplayer.Sysdata.NetProgramInfo;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.PvrInfo;
import com.prime.dtvplayer.Sysdata.SatInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;
import com.prime.dtvplayer.Sysdata.SubtitleInfo;
import com.prime.dtvplayer.Sysdata.TeletextInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.Sysdata.VMXProtectData;
import com.prime.dtvplayer.utils.TVMessage;
import com.prime.dtvplayer.utils.TVMessage.widevineMsg;
import com.prime.dtvplayer.utils.TVScanParams;
import com.prime.dtvplayer.utils.TVTunerParams;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.dolphin.dtv.HiDtvMediaPlayer.getUnsignedInt;
import static com.prime.dtvplayer.Activity.DTVActivity.CALLBACK_EVENT_ECM;
import static com.prime.dtvplayer.Activity.DTVActivity.CALLBACK_EVENT_PRIVATE_DATA;//eric lin 20210112 widevine scheme data

/**
 * Created by ethan_lin on 2017/10/26.
 */

public abstract class HisiTVActivity extends Activity {
    private static final String TAG="HisiTVActivity";

    // HiDtvPlayer service type def, see /device/hisilicon/bigfish/hidolphin/component/dtvappfrm/java/com/hisilicon/dtv/network/service/EnServiceType.java
    private static final int HISI_SERVICETYPE_TV = 1;
    private static final int HISI_SERVICETYPE_RADIO = 2;

    int mTvChCount;
    int mRadioChCount;
    int mAlreadySearchTpCount;

    private static final int ONETIME_OBTAINED_NUMBER = 1000;

    abstract public void onConnected();

    abstract public void onDisconnected();

    abstract public void onMessage(TVMessage msg);

    private HiDtvMediaPlayer mDtv = null ;

    private CaInfo Ca_Message = null;
    private ArrayList<CaInfo> CaMessageList = null;

    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mDtv = HiDtvMediaPlayer.getInstance(getApplicationContext()) ;
        Ca_Message = new CaInfo();
        CaMessageList = new ArrayList<>();
        for (int i = 0; i < CaPriority.ERR_MAX.getValue(); i++)
        {
            CaMessageList.add(Ca_Message);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Edwin 20200504 keep screen on
    }
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        UnSubScribeEvent();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        UnSubScribeEvent();
        mDtv.unlinkDeathNotify(); // edwin 20201216 re-link death notify & listener // edwin 20201214 add HwBinder.DeathRecipient
    }

    @Override
    protected  void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        //mDtv.prepareDTV() ;
        SubScribeEvent();
        // edwin 20201216 re-link death notify & listener -s
        mDtv.linkDeathNotify(); // edwin 20201214 add HwBinder.DeathRecipient
        mDtv.setOnServiceDiedListener(cookie -> { // edwin 20201214 add HwBinder.DeathRecipient
            Log.d(TAG, "onServiceDied: cookie = " + cookie);
            onMessage(TVMessage.SetMtestServiceDied());
        });
        // edwin 20201216 re-link death notify & listener -e
    }

    /*
        regist cmd start end value
        public static final int HI_SVR_EVT_AV_CALLBACK_START = 0x00B40002 ;
        public static final int HI_SVR_EVT_AV_CALLBACK_END = 0x00B4000C ;

        public static final int HI_SVR_EVT_EPG_CALLBACK_START = 0x00B60002 ;
        public static final int HI_SVR_EVT_EPG_CALLBACK_END = 0x00B6000B ;

        public static final int HI_SVR_EVT_SRCH_CALLBACK_START = 0x00B80002 ;
        public static final int HI_SVR_EVT_SRCH_CALLBACK_END = 0x00B8000B ;

        public static final int HI_SVR_EVT_BOOK_CALLBACK_START = 0x00BA0002 ;
        public static final int HI_SVR_EVT_BOOK_CALLBACK_END = 0x00BA0005 ;

        public static final int HI_SVR_EVT_PVR_CALLBACK_START = 0x00BC0002 ;
        public static final int HI_SVR_EVT_PVR_CALLBACK_END = 0x00BC000B ;

        public static final int HI_SVR_EVT_TBM_CALLBACK_START = 0x00BC0010 ;
        public static final int HI_SVR_EVT_TBM_CALLBACK_END = 0x00BC0013 ;

        public static final int HI_SVR_CI_EVT_CALLBACK_START = 0x008A0002 ;
        public static final int HI_SVR_CI_EVT_CALLBACK_END = 0x008A0009 ;

        public static final int HI_SVR_EVT_EWS_CALLBACK_START = 0x00BE0002 ;
        public static final int HI_SVR_EVT_EWS_CALLBACK_END = 0x00BE0004 ;
     */
    private void SubScribeEvent()
    {
        registCallback ( DTVMessage.HI_SVR_EVT_AV_CALLBACK_START, DTVMessage.HI_SVR_EVT_AV_CALLBACK_END, gAVListener ) ;
        registCallback ( DTVMessage.HI_SVR_EVT_EPG_CALLBACK_START, DTVMessage.HI_SVR_EVT_EPG_CALLBACK_END, gEPGListener ) ;
        registCallback ( DTVMessage.HI_SVR_EVT_SRCH_CALLBACK_START, DTVMessage.HI_SVR_EVT_SRCH_CALLBACK_END, gScanListener ) ;
        registCallback ( DTVMessage.HI_SVR_EVT_BOOK_CALLBACK_START, DTVMessage.HI_SVR_EVT_BOOK_CALLBACK_END, gBOOKListener ) ;
        registCallback ( DTVMessage.HI_SVR_EVT_PVR_CALLBACK_START, DTVMessage.HI_SVR_EVT_PVR_CALLBACK_END, gPVRListener ) ;
        registCallback ( DTVMessage.HI_SVR_EVT_TBM_CALLBACK_START, DTVMessage.HI_SVR_EVT_TBM_CALLBACK_END, gTBMListener ) ;
        registCallback ( DTVMessage.HI_SVR_CI_EVT_CALLBACK_START, DTVMessage.HI_SVR_CI_EVT_CALLBACK_END, gCIListener ) ;
        registCallback ( DTVMessage.HI_SVR_EVT_EWS_CALLBACK_START, DTVMessage.HI_SVR_EVT_EWS_CALLBACK_END, gEWSListener ) ;
        registCallback ( DTVMessage.HI_SVR_EVT_VMX_CALLBACK_START, DTVMessage.HI_SVR_EVT_VMX_CALLBACK_END, gVMXListener ) ;//Scoty 20180831 add vmx callback event
        registCallback ( DTVMessage.HI_SVR_EVT_LOADERDTV_CALLBACK_START, DTVMessage.HI_SVR_EVT_LOADERDTV_CALLBACK_END, gLoaderDtvListener ) ;
        registCallback ( DTVMessage.PESI_EVT_PIO_CALLBACK_START, DTVMessage.PESI_EVT_PIO_CALLBACK_END, gPIOListener ) ;
        registCallback ( DTVMessage.PESI_EVT_SYSTEM_CALLBACK_START, DTVMessage.PESI_EVT_SYSTEM_CALLBACK_END, gSystemListener ) ;
        registCallback ( DTVMessage.PESI_EVT_MTEST_START, DTVMessage.PESI_EVT_MTEST_END, gMtestListener ) ;//Scoty 20190410 add Mtest Pc Tool callback
        registCallback ( DTVMessage.PESI_EVT_PT_DEBUG_CALLBACK_TEST, DTVMessage.PESI_EVT_PT_DEBUG_END, gTestListener ) ;
        registCallback ( DTVMessage.PESI_EVT_CA_WIDEVINE, DTVMessage.PESI_EVT_CA_END, gCaListener ) ;//eric lin 20210107 widevine cas
    }

    private void UnSubScribeEvent()
    {
        unregistCallback ( DTVMessage.HI_SVR_EVT_AV_CALLBACK_START, DTVMessage.HI_SVR_EVT_AV_CALLBACK_END, gAVListener ) ;
        unregistCallback ( DTVMessage.HI_SVR_EVT_EPG_CALLBACK_START, DTVMessage.HI_SVR_EVT_EPG_CALLBACK_END, gEPGListener ) ;
        unregistCallback ( DTVMessage.HI_SVR_EVT_SRCH_CALLBACK_START, DTVMessage.HI_SVR_EVT_SRCH_CALLBACK_END, gScanListener ) ;
        unregistCallback ( DTVMessage.HI_SVR_EVT_BOOK_CALLBACK_START, DTVMessage.HI_SVR_EVT_BOOK_CALLBACK_END, gBOOKListener ) ;
        unregistCallback ( DTVMessage.HI_SVR_EVT_PVR_CALLBACK_START, DTVMessage.HI_SVR_EVT_PVR_CALLBACK_END, gPVRListener ) ;
        unregistCallback ( DTVMessage.HI_SVR_EVT_TBM_CALLBACK_START, DTVMessage.HI_SVR_EVT_TBM_CALLBACK_END, gTBMListener ) ;
        unregistCallback ( DTVMessage.HI_SVR_CI_EVT_CALLBACK_START, DTVMessage.HI_SVR_CI_EVT_CALLBACK_END, gCIListener ) ;
        unregistCallback ( DTVMessage.HI_SVR_EVT_EWS_CALLBACK_START, DTVMessage.HI_SVR_EVT_EWS_CALLBACK_END, gEWSListener ) ;
        unregistCallback ( DTVMessage.HI_SVR_EVT_VMX_CALLBACK_START, DTVMessage.HI_SVR_EVT_VMX_CALLBACK_END, gVMXListener ) ;//Scoty 20180831 add vmx callback event
        unregistCallback ( DTVMessage.HI_SVR_EVT_LOADERDTV_CALLBACK_START, DTVMessage.HI_SVR_EVT_LOADERDTV_CALLBACK_END, gLoaderDtvListener ) ;
        unregistCallback ( DTVMessage.PESI_EVT_PIO_CALLBACK_START, DTVMessage.PESI_EVT_PIO_CALLBACK_END, gPIOListener ) ;
        unregistCallback ( DTVMessage.PESI_EVT_SYSTEM_CALLBACK_START, DTVMessage.PESI_EVT_SYSTEM_CALLBACK_END, gSystemListener ) ;
        unregistCallback ( DTVMessage.PESI_EVT_MTEST_START, DTVMessage.PESI_EVT_MTEST_END, gMtestListener ) ;//Scoty 20190410 add Mtest Pc Tool callback
        unregistCallback ( DTVMessage.PESI_EVT_PT_DEBUG_CALLBACK_TEST, DTVMessage.PESI_EVT_PT_DEBUG_END, gTestListener ) ;
        unregistCallback ( DTVMessage.PESI_EVT_CA_WIDEVINE, DTVMessage.PESI_EVT_CA_END, gCaListener ) ;//eric lin 20210107 widevine cas
    }

    public int getTunerType()
    {
        return mDtv.getTunerType();
    }

    public void setChannelExist(int exist)
    {
        mDtv.setChannelExist(exist);
    }

    public ArrayList<List<SimpleChannel>> GetProgramManagerTotalChannelList()
    {
        return mDtv.GetProgramManagerTotalChannelList();
    }

    public ArrayList<FavGroupName> GetAllProgramGroup()
    {
        return mDtv.GetAllProgramGroup();
    }

    public void SetViewUiDisplayManager(DTVActivity.ViewUiDisplay viewUiDisplay)
    {
        mDtv.SetViewUiDisplayManager(viewUiDisplay);
    }

    public DTVActivity.ViewUiDisplay GetViewUiDisplayManager()
    {
        return mDtv.GetViewUiDisplayManager();
    }

    public void SetEpgUiDisplayManager(DTVActivity.EpgUiDisplay epgUiDisplay)
    {
        mDtv.SetEpgUiDisplayManager(epgUiDisplay);
    }

    public DTVActivity.EpgUiDisplay GetEpgUiDisplayManager(int type)
    {
        return mDtv.GetEpgUiDisplayManager(type);
    }

    public int getChannelExist()
    {
        return mDtv.getChannelExist();
    }

    public void pipModSetDisplay(Surface surface, int type)
    {
        mDtv.pipModSetDisplay(this,surface, type);//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    }

    public void pipModClearDisplay(Surface surface)
    {
        mDtv.pipModClearDisplay(surface);
    }

    public void setSurfaceView(SurfaceView surface)
    {
        mDtv.setSurfaceView(this,surface);//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    }

    public void startScan(TVScanParams sp) {
        Log.d(TAG, "startScan");
        mTvChCount = 0;
        mRadioChCount = 0;
        mAlreadySearchTpCount = 0;
        mDtv.startScan(sp);
    }

    public void VMXstartScan(TVScanParams sp, int startTPID, int endTPID) { // connie 20180919 add for vmx search
        Log.d(TAG, "startScan");
        mTvChCount = 0;
        mRadioChCount = 0;
        mAlreadySearchTpCount = 0;
        mDtv.VMXstartScan(sp, startTPID, endTPID);
    }

    public void stopScan(boolean store) {
        Log.d(TAG, "stopScan");
        mDtv.stopScan(store);
        //client.StopScan(store);
    }

    //ethan 20171201 modify TuneFrontEnd
    public int TuneFrontEnd(TVTunerParams tvTunerParams){
        // Log.d(TAG, "TundFrontEnd: ");
        return mDtv.tunerLock(tvTunerParams);
    }

    public int TunerGetStrength(int tuner_id){
        //Log.d(TAG, "TeunrGetStrength");
        return mDtv.getSignalStrength(tuner_id);
    }

    public int TunerGetQuality(int tuner_id){
        //Log.d(TAG, "TeunrGetQuality");
        return mDtv.getSignalQuality(tuner_id);
    }

    public int TunerGetLockStatus(int tuner_id){
        //Log.d(TAG, "TeunrGetLockStatus");
        return mDtv.getTunerStatus(tuner_id);
    }

    public String TunerGetBER(int tuner_id){
//        Log.d(TAG, "TunerGetBER");
        return mDtv.getSignalBER(tuner_id);
    }

    public int TunerGetSNR(int tuner_id){
        Log.d(TAG, "TunerGetSNR");
        return mDtv.getSignalSNR(tuner_id);
    }

    public int setFakeTuner(int openFlag)//Scoty 20180809 add fake tuner command
    {
        return mDtv.setFakeTuner(openFlag);
    }

    public int TunerSetAntenna5V(int tuner_id,int onOff){
        Log.d(TAG, "TunerSetAntenna5V");
        return mDtv.TunerSetAntenna5V(tuner_id,onOff);
    }

    public void ResetFactoryDefault(){
        Log.d(TAG, "ResetFactoryDefault: ");
        mDtv.ResetFactoryDefault();
    }

    public GposInfo GposInfoGet() {
        return mDtv.GposInfoGet();
    }

    public void GposInfoUpdate(GposInfo gPos) {
        mDtv.GposInfoUpdate(gPos);
    }

    public void GposInfoUpdateByKeyString(String key,String value) {
        mDtv.GposInfoUpdateByKeyString(key,value);
    }

    public void GposInfoUpdateByKeyString(String key,int value) {
        mDtv.GposInfoUpdateByKeyString(key,value);
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
        List<SatInfo> SatList = mDtv.SatInfoGetList(tunerType, pos, num);

        if ( SatList == null )  // johnny add 20171219
        {
            return null;
        }

        for(int i = 0; i < SatList.size(); i++) {
            List<TpInfo> TpList = TpInfoGetListBySatId(tunerType,SatList.get(i).getSatId(),-1,-1);  // johnny modify 20180205

            if ( TpList == null )   // johnny add 20171225
            {
                TpList = new ArrayList<>();
            }

            List<Integer> TpIdList = new ArrayList<>();
            for(int j = 0; j < TpList.size(); j++) {
                TpIdList.add(TpList.get(j).getTpId());
            }

            SatList.get(i).setTpNum(TpIdList.size());   // johnny add 20180205
            SatList.get(i).setTps(TpIdList);
        }
        return SatList;
    }

    public SatInfo SatInfoGet(int satId) {
        SatInfo Sat = mDtv.SatInfoGet(satId);

        if ( Sat == null )  // johnny add 20171219
        {
            return null;
        }
        return Sat;
    }
/*
    public void SatInfoSave(SatInfo pSat) {
        client.Sat.Save(pSat);
    }
*/

    public int SatInfoAdd(SatInfo pSat) {
        return mDtv.SatInfoAdd(pSat);
    }

    public int SatInfoUpdate(SatInfo pSat) {
        return mDtv.SatInfoUpdate(pSat);
    }

    public int SatInfoUpdateList(List<SatInfo> pSats) {
        return mDtv.SatInfoUpdateList(pSats);
    }

    public int SatInfoDelete(int satId) {
        return mDtv.SatInfoDelete(satId);
    }
    /*
        public List<TpInfo> TpInfoGetList(int tunerType) {
            return client.Tp.GetTpInfoList(tunerType);
        }
    */
    public List<TpInfo> TpInfoGetListBySatId(int tunerType,int satId,int pos,int num) {
        return mDtv.TpInfoGetListBySatId(tunerType, satId, pos, num);
    }

    public TpInfo TpInfoGet(int tp_id) {
        return mDtv.TpInfoGet(tp_id);
    }

    public int TpInfoAdd(TpInfo pTp) {
        return mDtv.TpInfoAdd(pTp);
    }

    public int TpInfoUpdate(TpInfo pTp) {
        return mDtv.TpInfoUpdate(pTp);
    }

    public int TpInfoUpdateList(List<TpInfo> pTps) {
        return mDtv.TpInfoUpdateList(pTps);
    }

    public int TpInfoDelete(int tpId) {
        return mDtv.TpInfoDelete(tpId);
    }

    public List<ProgramInfo> ProgramInfoGetList(int type,int pos,int num) {
        return mDtv.GetProgramInfoList(type, pos, num);
    }

    public ProgramInfo ProgramInfoGetByChannelId(long channelId) {
        return mDtv.GetProgramByChannelId(channelId);
    }

    public ProgramInfo ProgramInfoGetByChnum(int chnum, int type) {
        return mDtv.GetProgramByChnum(chnum,type);
    }

    public ProgramInfo ProgramInfoGetByLcn(int lcn, int type) {
        return mDtv.GetProgramByLcn(lcn,type);
    }

    /* if add new param to save , should modify HiDtvMediaPlayer.java and middleware pesimain related code*/
    public int ProgramInfoUpdate(ProgramInfo pProgram) {
        return mDtv.updateProgramInfo(pProgram);
    }

    public int ProgramInfoUpdateList(List<ProgramInfo> pPrograms) {
        //client.Program.Save(pPrograms); //need fix
        return 0;
    }

    public int ProgramInfoUpdateList(List<SimpleChannel> pPrograms,int type) {
        return mDtv.updateSimpleChannelList(pPrograms,type);
    }

    public int ProgramInfoDelete(long channelId) {
        mDtv.DeleteProgram(channelId);
        return 0;
    }

    public int ProgramInfoDeleteAllByType(int type) {
        //client.Program.DeleteAll(type); //need fix
        return 0;
    }

    public int SaveTable(EnTableType option) {
        return mDtv.saveTable(option);
    }

    public int setDefaultOpenChannel(long channelId, int groupType) {
        return mDtv.setDefaultOpenChannel(channelId, groupType);
    }

    public DefaultChannel getDefaultOpenChannel() {
        return mDtv.getDefaultChannel();
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

    public void ProgramInfoPlaySimpleChannelListInit(int IncludePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        ProgramInfoPlaySimpleChannelListUpdate(IncludePVRSkipFlag);
    }

    public void ProgramInfoPlaySimpleChannelListUpdate(Context context, int IncludePVRSkipFlag)
    {
        mDtv.UpdateCurPlayChannelList(context,IncludePVRSkipFlag);
    }

    public void ProgramInfoPlaySimpleChannelListUpdate(int IncludePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        mDtv.UpdateCurPlayChannelList(IncludePVRSkipFlag);
    }

    public List<SimpleChannel> ProgramInfoGetPlaySimpleChannelList(int type,int IncludePVRSkipFlag) {///Scoty 20180615 recover get simple channel list function/Scoty 20180613 change get simplechannel list for PvrSkip rule
        return mDtv.GetCurPlayChannelList(type,IncludePVRSkipFlag);
    }

    public int ProgramInfoGetPlaySimpleChannelListCnt(int type) {//eric lin 20180802 check program exist
        return mDtv.GetCurPlayChannelListCnt(type);
    }

    public List<SimpleChannel> ProgramInfoGetSimpleChannelList(int type,int IncludeSkipFlag,int IncludePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        return mDtv.GetSimpleProgramList(type,IncludeSkipFlag,IncludePVRSkipFlag);
    }

    public List<SimpleChannel> ProgramInfoGetSimpleChannelListfromTotalChannel(int type,int IncludeSkipFlag,int IncludePVRSkipFlag)
    {
        return mDtv.GetSimpleProgramListfromTotalChannelList(type,IncludeSkipFlag,IncludePVRSkipFlag);
    }

    public SimpleChannel ProgramInfoGetSimpleChannelByChannelId(long channelId) {
        return mDtv.GetSimpleProgramByChannelId(channelId);
    }

    public SimpleChannel GetSimpleProgramByChannelIdfromTotalChannelList(long channelId){
        return mDtv.GetSimpleProgramByChannelIdfromTotalChannelList(channelId);
    }

    public SimpleChannel GetSimpleProgramByChannelIdfromTotalChannelListByGroup(int groupType, long channelId)
    {
        return mDtv.GetSimpleProgramByChannelIdfromTotalChannelListByGroup(groupType,channelId);
    }

    public List<SimpleChannel> ProgramInfoGetListByFilter(int filterTag, int serviceType, String keyword, int IncludeSkip, int IncludePvrSkip) {//Scoty 20181109 modify for skip channel
        return mDtv.getChannelListByFilter(filterTag, serviceType, keyword, IncludeSkip, IncludePvrSkip);//Scoty 20181109 modify for skip channel
    }

    public List<FavInfo> FavInfoGetList(int favMode) {
        return mDtv.FavInfoGetList(favMode);
    }

    public FavInfo FavInfoGet(int favMode,int index) {
        return mDtv.FavInfoGet(favMode,index);
    }

    public int FavInfoUpdate(FavInfo favInfo) {
        return 0;
    }

    public int FavInfoUpdateList(List<FavInfo> favInfo) {
        return mDtv.FavInfoUpdateList(favInfo);
    }

    public int FavInfoDelete(int favMode, long channelId) {
        return mDtv.FavInfoDelete(favMode,channelId);
    }

    public int FavInfoDeleteAll(int favMode) {
        return mDtv.FavInfoDeleteAll(favMode);
    }

    public String FavGroupNameGet(int favMode) {
        return mDtv.FavGroupNameGet(favMode);
    }

    public int FavGroupNameUpdate(int favMode, String name) {
        return mDtv.FavGroupNameUpdate(favMode, name);
    }

    public List<BookInfo> InitUIBookList(){
        return mDtv.InitUIBookList();
    }

    public void SetUIBookManager(DTVActivity.BookManager bookmanager)
    {
        mDtv.SetUIBookManager(bookmanager);
    }

    public DTVActivity.BookManager GetUIBookManager()
    {
        return mDtv.GetUIBookManager();
    }

    public List<BookInfo> GetUIBookList()
    {
        return mDtv.GetUIBookList();
    }

    public List<BookInfo> BookInfoGetList() {
        return mDtv.BookInfoGetList();
    }

    public BookInfo GetTaskByIdFromUIBookList(int id)
    {
        return mDtv.GetTaskByIdFromUIBookList(id);
    }

    public BookInfo BookInfoGet(int bookId) {
        return mDtv.BookInfoGet(bookId);
    }

    public int BookInfoAdd(BookInfo bookInfo) {
        return mDtv.BookInfoAdd(bookInfo);
    }

    public int BookInfoUpdate(BookInfo bookInfo) {
        return mDtv.BookInfoUpdate(bookInfo);
    }

    public int BookInfoUpdateList(List<BookInfo> bookList) {
        return mDtv.BookInfoUpdateList(bookList);
    }

    public int BookInfoDelete(int bookId) {
        return mDtv.BookInfoDelete(bookId);
    }

    public int BookInfoDeleteAll() {
        return mDtv.BookInfoDeleteAll();
    }

    public BookInfo BookInfoGetComingBook() {
        return mDtv.BookInfoGetComingBook();
    }

    public List<BookInfo> BookInfoFindConflictBooks(BookInfo bookInfo) {
        return mDtv.BookInfoFindConflictBooks(bookInfo);
    }

    public EPGEvent EpgEventGetPresentEvent(long channelId) {
        EPGEvent epgEvent = null;
        if (channelId >= 0)
        {
            Log.d(TAG, "EpgEventGetPresentEvent:  channelId = " + channelId);
            epgEvent = mDtv.getPresentEvent(channelId);
        }
        else {
            Log.d(TAG, "getPresentEPGEvent:channel parameter is invalid.");
        }

        return epgEvent;
    }

    public EPGEvent EpgEventGetFollowEvent(long channelId)
    {
        EPGEvent epgEvent = null;
        // obtain event information while parameter is valid,otherwise return null
        if (channelId >= 0)
        {
            Log.d(TAG, "getFollowingEPGEvent(ChannelID = " + channelId);
            epgEvent = mDtv.getFollowEvent(channelId);
        }
        else
        {
            Log.d(TAG, "getFollowingEPGEvent:channel parameter is invalid.");
        }

        return epgEvent;
    }

    public EPGEvent EpgEventGetEPGByEventId(long channelId, int eventId) {

        // obtain event information while parameter is valid,otherwise return null
        if (((channelId >= 0)) && (eventId >= 0))
        {
            Log.d(TAG, "getEpgEventByEventID(ChannelID = " + channelId + ",eventID = "+ eventId + ")");
            return mDtv.getEpgByEventID(channelId, eventId);
        }
        else
        {
            Log.d(TAG, "getEpgEventByEventID:one or more parameter is invalid.");
            return null;
        }
    }

    public List<EPGEvent> EpgEventGetEPGEventList(long channelId, long startTime, long endTime, int addEmpty) {
        ArrayList<EPGEvent> list = null;

        Log.d(TAG, "EpgEventGetEPGEventList:  start = " + startTime + "         endTime = " + endTime);
        if (( channelId >= 0) && (startTime >= 0)  && (endTime-startTime > 0))
        {
            Log.d(TAG, "getEPGEvents(ChannelID = " + channelId + ",startTime = " + startTime
                    + ",duration = " + (startTime - endTime) + ")");

            int i = 0;
            list = new ArrayList<EPGEvent>();
            List<EPGEvent> secondaryList = null;
            Date startDate = new Date(startTime);
            Date endDate = new Date(endTime);
            while (true)
            {
                // update start offset position
                int offset = ONETIME_OBTAINED_NUMBER * (i++);

                // obtain event list by filter with channel and time enabled

                secondaryList = mDtv.getEPGEvents(channelId, startDate, endDate, offset, ONETIME_OBTAINED_NUMBER, addEmpty);
                if (null == secondaryList)
                {
                    // while no event is obtained
                    break;
                }

                list.addAll(secondaryList);
                int count = secondaryList.size();
                if ((count >= 0) && (count < ONETIME_OBTAINED_NUMBER))
                {
                    // while events by filter are all obtained
                    break;
                }

                EPGEvent lastEvent = secondaryList.get(count - 1);
                if (null == lastEvent)
                {
                    // while the last event is invalid
                    break;
                }

                // Date lastEndDate = lastEvent.getEndTime();
                Date lastEndDate = new Date(lastEvent.getEndTime());

                if (lastEndDate.after(endDate))
                {
                    // while endTime of last event is after endTime of filter
                    break;
                }

                // update endTime of filter by endTime of last event as startTime of filter to next
                // time
                startTime = lastEvent.getEndTime();
                startDate = new Date(startTime);
            }
        }
        else
        {
            Log.d(TAG, "getEPGEvents:one or more parameter is invalid.");
        } // if(((null != channel) && (channel.getChannelID() >= 0)) && (startTime >= 0) &&
        // (duration > 0) )
        return list;
    }

    public String EpgEventGetShortDescription(long channelId, int eventId) {
        Log.d(TAG, "EpgEventGetShortDescription:  channelId = " + channelId + "      eventId = " + eventId);
        return mDtv.getShortDescription(channelId, eventId);
    }

    public String EpgEventGetDetailDescription(long channelId, int eventId) {
        Log.d(TAG, "EpgEventGetDetailDescription: channelId ="+channelId+"    eventId = " +eventId);
        return mDtv.getDetailDescription(channelId, eventId);
    }

    public int EpgEventSetLanguageCode(String FirstLangCode, String SecLangCode) {
        return mDtv.setEvtLang(FirstLangCode, SecLangCode);
    }

    public int EpgEventStartEPG(long channelId) {
        return 0;
    }

    public Date getLocalTime()
    {
        return mDtv.getDtvDate();
    }

    public int setTime(Date date)
    {
        return mDtv.setTime(date);
    }

    public int getDtvTimeZone()
    {
        return mDtv.getDtvTimeZone();
    }

    public int setDtvTimeZone(int zonesecond)
    {
        return mDtv.setDtvTimeZone(zonesecond);
    }

    public int getSettingTDTStatus()
    {
        return mDtv.getSettingTDTStatus();
    }

    public int setSettingTDTStatus(int onoff)
    {
        return mDtv.setSettingTDTStatus(onoff);
    }

    public int setTimeToSystem(boolean bSetTimeToSystem)
    {
        return mDtv.setTimeToSystem(bSetTimeToSystem);
    }

    public int syncTime(boolean bEnable)
    {
        return mDtv.syncTime(bEnable);
    }

    public int getDtvDaylight()
    {
        return mDtv.getDtvDaylight();
    }

    public int setDtvDaylight(int onoff)
    {
        return mDtv.setDtvDaylight(onoff);
    }

    public int AvControlPlayByChannelId(int playId,long channelId, int groupType, int show) {
        return mDtv.AvControlPlayByChannelId(playId, channelId, groupType, show);
    }

    public int AvControlPrePlayStop(){
        return mDtv.AvControlPrePlayStop();
    }

    public int AvControlPlayStop(int playId) {
        return mDtv.AvControlPlayStop(playId);
    }

    public int AvControlChangeRatioConversion(int playId,int ratio,int conversion) {
        return mDtv.AvControlChangeRatioConversion(playId, ratio, conversion);
    }
    public int AvControlSetFastChangeChannel(long PreChannelId, long NextChannelId)//Scoty 20180816 add fast change channel
    {
        return mDtv.AvControlSetFastChangeChannel(PreChannelId,NextChannelId);
    }
/*
    public int AvControlChangeRatio(int playId,int ratio) {
        return mDtv.AvControlChangeRatio(playId, ratio);
    }

    public int AvControlChangeConversion(int playId,int conversion) {
        return mDtv.AvControlChangeConversion(playId, conversion);
    }
*/
    public int AvControlChangeResolution(int playId,int resolution) {
        return mDtv.AvControlChangeResolution(playId, resolution);
    }

    public int AvControlChangeAudio(int playId,AudioInfo.AudioComponent component) {
        return mDtv.AvControlChangeAudio(playId, component);
    }

    public int AvControlSetVolume(int volume) {
        return mDtv.AvControlSetVolume(volume);
    }

    public int AvControlGetVolume() {
        return mDtv.AvControlGetVolume();
    }

    public int AvControlSetMute(int playId,boolean mute) {
        return mDtv.AvControlSetMute(playId, mute);
    }

    public int AvControlSetTrackMode(int playId, EnAudioTrackMode stereo) {
        return mDtv.AvControlSetTrackMode(playId, stereo);
    }

    public int AvControlAudioOutput(int playId,int byPass) {
        return mDtv.AvControlAudioOutput(playId, byPass);
    }

    public int AvControlClose(int playId) {
        return mDtv.AvControlClose(playId);
    }

    public int AvControlOpen(int playId) {
        return mDtv.AvControlOpen(playId);
    }

    public int AvControlShowVideo(int playId,boolean show) {
        return mDtv.AvControlShowVideo(playId, show);
    }

    public int AvControlFreezeVideo(int playId,boolean freeze) {
        return mDtv.AvControlFreezeVideo(playId, freeze);
    }

    //return value should be check
    public AudioInfo AvControlGetAudioListInfo(int playId) {
        return mDtv.AvControlGetAudioListInfo(playId);
    }

    /* return status =  LIVEPLAY,TIMESHIFTPLAY.....etc  */
    public int AvControlGetPlayStatus(int playId) {
        return mDtv.AvControlGetPlayStatus(playId);
    }

    public boolean AvControlGetMute(int playId) {
        return mDtv.AvControlGetMute(playId);
    }

    public EnAudioTrackMode AvControlGetTrackMode(int playId) {
        return mDtv.AvControlGetTrackMode(playId);
    }

    public int AvControlGetRatio(int playId){
        return mDtv.AvControlGetRatio(playId);
    }

    public int AvControlSetStopScreen(int playId,int stopType) {
        return mDtv.AvControlSetStopScreen(playId, stopType);
    }

    public int AvControlGetStopScreen(int playId) {
        return mDtv.AvControlGetStopScreen(playId);
    }

    public int AvControlGetFPS(int playId) {
        return mDtv.AvControlGetFPS(playId);
    }

    public int AvControlEwsActionControl(int playId, boolean enable) {
        return mDtv.AvControlEwsActionControl(playId, enable);
    }

    //input value should be check
    public int AvControlSetWindowSize(int playId, Rect rect) {
        return mDtv.AvControlSetWindowSize(playId, rect);
    }

    //return value should be check
    public Rect AvControlGetWindowSize(int playId) {
        return mDtv.AvControlGetWindowSize(playId);
    }

    public int AvControlGetVideoResolutionHeight(int playId) {
        return mDtv.AvControlGetVideoResolutionHeight(playId);
    }

    public int AvControlGetVideoResolutionWidth(int playId) {
        return mDtv.AvControlGetVideoResolutionWidth(playId);
    }

    /* 0: dolby digital, 1: dolby digital plus */
    public int AvControlGetDolbyInfoStreamType(int playId) {
        return mDtv.AvControlGetDolbyInfoStreamType(playId);
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
        return mDtv.AvControlGetDolbyInfoAcmod(playId);
    }

    public SubtitleInfo.SubtitleComponent AvControlGetCurrentSubtitle(int playId) {
        SubtitleInfo.SubtitleComponent subtitleInfo = mDtv.AvControlGetCurrentSubtitle(playId);
        return subtitleInfo;
    }

    public SubtitleInfo AvControlGetSubtitleList(int playId) {
        SubtitleInfo subtitleInfo = mDtv.AvControlGetSubtitleList(playId);
        return subtitleInfo;
    }

    public int AvControlSelectSubtitle(int playId,SubtitleInfo.SubtitleComponent subtitleComponent) {
        return mDtv.AvControlSelectSubtitle(playId, subtitleComponent);
    }

    public int AvControlShowSubtitle(int playId,boolean enable) {
        return mDtv.AvControlShowSubtitle(playId, enable);
    }

    public boolean AvControlIsSubtitleVisible(int playId) {
        return mDtv.AvControlIsSubtitleVisible(playId);
    }

    public int AvControlSetSubtHiStatus(int playId,boolean HiOn) {
        return mDtv.AvControlSetSubtHiStatus(playId, HiOn);
    }

    public int AvControlSetSubtitleLanguage(int playId,int index, String lang) {
        return mDtv.AvControlSetSubtitleLanguage(playId, index, lang);
    }

    public TeletextInfo AvControlGetCurrentTeletext(int playId) {
        return mDtv.AvControlGetCurrentTeletext(playId);
    }

    public List<TeletextInfo> AvControlGetTeletextList(int playId) {
        return mDtv.AvControlGetTeletextList(playId);
    }

    public int AvControlShowTeletext(int playId,boolean enable) {
        return mDtv.AvControlShowTeletext(playId, enable);
    }

    public boolean AvControlIsTeletextVisible(int playId) {
        return mDtv.AvControlIsTeletextVisible(playId);
    }

    public boolean AvControlIsTeletextAvailable(int playId) {
        return mDtv.AvControlIsTeletextAvailable(playId);
    }

    public int AvControlSetTeletextLanguage(int playId,String primeLang) {
        return mDtv.AvControlSetTeletextLanguage(playId, primeLang);
    }

    public String AvControlGetTeletextLanguage(int playId) {//eric lin 20180705 get ttx lang
        return mDtv.AvControlGetTeletextLanguage(playId);
    }

    public int AvControlSetCommand(int playId,int keyCode) {
        return mDtv.AvControlSetCommand(playId, keyCode);
    }

    public Date AvControlGetTimeShiftBeginTime(int playId) {
        return mDtv.AvControlGetTimeShiftBeginTime(playId);
    }

    public Date AvControlGetTimeShiftPlayTime(int playId) {
        return mDtv.AvControlGetTimeShiftPlayTime(playId);
    }

    public int AvControlGetTimeShiftRecordTime(int playId) {
        return mDtv.AvControlGetTimeShiftRecordTime(playId);
    }

    public int AvControlGetTrickMode(int playId) {
        return mDtv.AvControlGetTrickMode(playId);
    }

    public int AvControlTimeshiftTrickPlay(int playId,int trickMode) {
        return mDtv.AvControlTimeshiftTrickPlay(playId, trickMode);
    }

    public int AvControlTimeshiftPausePlay(int playId) {
        return mDtv.AvControlTimeshiftPausePlay(playId);
    }

    public int AvControlTimeshiftPlay(int playId) {
        return mDtv.AvControlTimeshiftPlay(playId);
    }

    public int AvControlTimeshiftSeekPlay(int playId,long seekTime) {
        return mDtv.AvControlTimeshiftSeekPlay(playId, seekTime);
    }

    public int AvControlStopTimeShift(int playId) {
        return mDtv.AvControlStopTimeShift(playId);
    }

    public int UpdateUsbSoftWare(String filename)
    {
        return mDtv.UpdateUsbSoftWare(filename);
    }

    public int UpdateFileSystemSoftWare(String pathAndFileName , String partitionName)
    {
        return mDtv.UpdateFileSystemSoftWare(pathAndFileName , partitionName);
    }

    public int UpdateOTADVBCSoftWare(int tpId, int freq, int symbol, int qam)
    {
        return mDtv.UpdateOTADVBCSoftWare(tpId,freq,symbol,qam);
    }

    public int UpdateOTADVBTSoftWare(int tpId, int freq, int bandwith, int qam, int priority)
    {
        return mDtv.UpdateOTADVBTSoftWare(tpId,freq,bandwith,qam,priority);
    }

    public int UpdateOTADVBT2SoftWare(int tpId, int freq, int symbol, int qam, int channelmode)
    {
        return mDtv.UpdateOTADVBT2SoftWare(tpId,freq,symbol,qam,channelmode);
    }

    public int UpdateOTAISDBTSoftWare(int tpId, int freq, int bandwith, int qam, int priority)
    {
        return mDtv.UpdateOTAISDBTSoftWare(tpId,freq,bandwith,qam,priority);
    }

    public int UpdateMtestOTASoftWare()//Scoty 20190410 add Mtest Trigger OTA command
    {
        return mDtv.UpdateMtestOTASoftWare();
    }

    public OTACableParameters DVBGetOTACableParas() { return mDtv.DVBGetOTACableParas(); }

    public OTATerrParameters DVBGetOTAIsdbtParas() {return mDtv.DVBGetOTAIsdbtParas(); }

    public OTATerrParameters DVBGetOTATerrestrialParas() {return mDtv.DVBGetOTATerrestrialParas(); }

    public OTATerrParameters DVBGetOTADVBT2Paras() {return mDtv.DVBGetOTADVBT2Paras(); }

    private void registCallback( int callbackCmdStart, int callbackCmdEnd, IDTVListener scanListener)
    {
        for ( int i = callbackCmdStart ; i < callbackCmdEnd ; i++ )
            mDtv.subScribeEvent(i,scanListener,0) ;
    }

    private void unregistCallback( int callbackCmdStart, int callbackCmdEnd, IDTVListener scanListener)
    {
        for ( int i = callbackCmdStart ; i < callbackCmdEnd ; i++ )
            mDtv.unSubScribeEvent(i,scanListener) ;
    }

    public enum CaPriority
    {
        ERR_E48_52(0),
        ERR_E38(1),
        ERR_CA(2),
        ERR_E44(3),
        ERR_PROG_LOCK(4),
        ERR_PESI_DEFINE(5),
        ERR_E42(6),
        ERR_MAX(7);

        private int value;

        private CaPriority(int value){
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    };

    private void SetErrorMessage(int CAPriority ,String ErrorCodeString ,String CaMsgString)
    {
        for (int i = 0; i < CaPriority.ERR_MAX.getValue(); i++) {
            if (i == CAPriority) {
                CaMessageList.get(i).setErrcode(ErrorCodeString);
                CaMessageList.get(i).setErrstr(getResources().getString(R.string.STR_NO_SIGNAL));
                break;
            }
        }
    }

    IDTVListener gAVListener = new IDTVListener()
    {
        private static final String CB_TAG="AVListener";
        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj)
        {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            switch (messageID)
            {
                case DTVMessage.HI_SVR_EVT_AV_STOP:
                {
                    Log.d(TAG, "HI_SVR_EVT_AV_STOP") ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_PLAY_SUCCESS:
                {
                    Log.d(TAG, "HI_SVR_EVT_AV_PLAY_SUCCESS");
                    TVMessage msg = TVMessage.SetPlayMsg(1);
                    onMessage(msg) ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_PLAY_FAILURE:
                {
                    Log.d(TAG, "HI_SVR_EVT_AV_PLAY_FAILURE Error Code = " + param1);
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_CA:
                {
                    Log.d(TAG, "HI_SVR_EVT_AV_CA is " + (param1==1?"CA ":"NOT CA") + " prog" );
                    if(param1 == 1) {
                        int err_priority = CaPriority.ERR_E48_52.getValue();
                        String errorCode = "E48_32";
                        SetErrorMessage(err_priority,errorCode,getResources().getString(R.string.STR_NO_SIGNAL));
                        TVMessage msg = TVMessage.SetCAMsg
                                (param1,CaMessageList.get(err_priority).getErrCode(),CaMessageList.get(err_priority).getErrstr());
                        onMessage(msg);
                    }
                    else
                    {
                        TVMessage msg = TVMessage.SetCAMsg(param1,null,null);
                        onMessage(msg) ;
                    }
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_FRONTEND_STOP:
                {
                    Log.d(TAG, "HI_SVR_EVT_AV_FRONTEND_STOP");
                    TVMessage msg = TVMessage.SetVideoStatus(1, param1);//eric lin 20180803 no video signal
                    onMessage(msg) ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_FRONTEND_RESUME:
                {
                    Log.d(TAG, "HI_SVR_EVT_AV_FRONTEND_RESUME");
                    TVMessage msg = TVMessage.SetVideoStatus(0, param1);//eric lin 20180803 no video signal
                    onMessage(msg) ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_SIGNAL_STAUTS:
                {
                    Log.d(TAG, "HI_SVR_EVT_AV_SIGNAL_STAUTS signal" + (param1==1?"LOCK!!!!":"UNLOCK!!!!") );
                    TVMessage msg = TVMessage.SetTunerLockStatus(param1);
                    onMessage(msg) ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_MOTOR_MOVING:
                {
                    Log.d(TAG, "HI_SVR_EVT_AV_MOTOR_MOVING");
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_MOTOR_STOP:
                {
                    Log.d(TAG, "HI_SVR_EVT_AV_MOTOR_STOP");
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_CHANNEL_SCRAMBLED:
                {
                    Log.d(TAG, "HI_SVR_EVT_AV_CHANNEL_SCRAMBLED " + (param1==1?"audio":(param1==2?"video":"nothing")) + "is scramble!!!" );
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_CHANNEL_LOCKED:
                {
                    Log.d(TAG, "HI_SVR_EVT_AV_CHANNEL_LOCKED: "
                            + " param1 = " + param1 + " param2 = " + param2);

                    int AvLockStatus= ((Parcel) obj).readInt();
                    long ChannelId = ((Parcel) obj).readInt() & 0xFFFFFFFFL;
                    int rating = ((Parcel) obj).readInt();
                    Log.d(TAG, "notifyMessage: ChannelId = " + ChannelId + " AvLockStatus = " + AvLockStatus + " rating = " + rating);
                    TVMessage msg = TVMessage.SetAVLockStatus(AvLockStatus,ChannelId,rating);
                    onMessage(msg);
                    break;
                }
                default:
                    break;
            }
        }
    };

    IDTVListener gEPGListener = new IDTVListener()
    {
        private static final String CB_TAG="EPGListener";
        @Override
        public void notifyMessage(int messageID, int param1, int parm2, Object obj)
        {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            long channelID = 0;
            switch (messageID)
            {
                case DTVMessage.HI_SVR_EVT_EPG_PF_VERSION_CHANGED:
                {
                    Log.d(TAG, "HI_SVR_EVT_EPG_PF_VERSION_CHANGED rowID = " + param1) ;
                    channelID = getUnsignedInt(param1);
                    TVMessage msg = TVMessage.SetEPGUpdate(channelID);
                    onMessage(msg) ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_EPG_PF_CURR_PROGRAM_FINISH:
                {
                    Log.d(TAG, "HI_SVR_EVT_EPG_PF_CURR_PROGRAM_FINISH rowID = " + param1);
                    channelID = getUnsignedInt(param1);
                    TVMessage msg = TVMessage.SetEPGUpdate(channelID);
                    onMessage(msg) ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_EPG_PF_CURR_FREQ_FINISH:
                {
                    Log.d(TAG, "HI_SVR_EVT_EPG_PF_CURR_FREQ_FINISH rowID = " + param1) ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_EPG_PF_ALL_FINISH:
                {
                    Log.d(TAG, "HI_SVR_EVT_EPG_PF_ALL_FINISH");
                    break;
                }
                case DTVMessage.HI_SVR_EVT_EPG_SCH_VERSION_CHANGED:
                {
                    Log.d(TAG, "HI_SVR_EVT_EPG_SCH_VERSION_CHANGED rowID = " + param1) ;
                    channelID = getUnsignedInt(param1);
                    TVMessage msg = TVMessage.SetEPGUpdate(channelID);
                    onMessage(msg) ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_EPG_SCH_CURR_PROGRAM_FINISH:
                {
                    Log.d(TAG, "HI_SVR_EVT_EPG_SCH_CURR_PROGRAM_FINISH rowID = " + param1) ;
                    channelID = getUnsignedInt(param1);
                    TVMessage msg = TVMessage.SetEPGUpdate(channelID);
                    onMessage(msg) ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_EPG_SCH_CURR_FREQ_FINISH:
                {
                    Log.d(TAG, "HI_SVR_EVT_EPG_SCH_CURR_FREQ_FINISH rowID = " + param1) ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_EPG_SCH_ALL_FINISH:
                {
                    Log.d(TAG, "HI_SVR_EVT_EPG_SCH_ALL_FINISH" );
                    break;
                }
                case DTVMessage.HI_SVR_EVT_EPG_PARENTAL_RATING:
                {
                    Log.d(TAG, "HI_SVR_EVT_EPG_PARENTAL_RATING rowID = " + param1);
                    break;
                }
                default:
                    break;
            }
        }
    };

    IDTVListener gScanListener = new IDTVListener()
    {
        private static final String CB_TAG="ScanListener";
        @Override
        public void notifyMessage(int messageID, int param1, int parm2, Object obj)
        {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            switch (messageID)
            {
                case DTVMessage.HI_SVR_EVT_SRCH_BEGIN:
                {
                    Log.d(TAG, "notifyMessage Search begin") ;
                    TVMessage msg = TVMessage.SetScanBegin();
                    onMessage(msg) ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_SRCH_LOCK_START:
                {
//                    Log.d(TAG, " notifyMessage START lockfreq=" + param1 + " symbol = " + parm2 ); // hisi pass freq & symbol rate
                    Log.d(TAG, " notifyMessage START lockfreq=" + param1 + " tpId = " + parm2 ); // pesi pass freq & tpId
                    mAlreadySearchTpCount++;
                    TVMessage msg = TVMessage.SetScanTP(mAlreadySearchTpCount, parm2);
                    onMessage(msg) ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_SRCH_LOCK_STATUS:
                {
                    Log.d(TAG, " notifyMessage STATUS lockstate=" + param1);
                    break;
                }
                case DTVMessage.HI_SVR_EVT_SRCH_CUR_FREQ_TABLE_FINISH:
                {
                    Log.d(TAG, "notiftMessage Cur freq table finish");
                    break;
                }
                case DTVMessage.HI_SVR_EVT_SRCH_CUR_FREQ_INFO:
                {
                    Log.d(TAG, "notifyMessage FREQ_INFO freq=" + param1 + ",tpid=" + parm2);
                    break;
                }
                case DTVMessage.HI_SVR_EVT_SRCH_CUR_SCHEDULE:
                {
                    int percent = param1 % 100;
                    if ((0 == percent) && (param1 > 99))
                    {
                        percent = 100;
                    }
                    Log.d(TAG, " notifyMessage SCHEDULE percent=" + percent);

                    TVMessage msg = TVMessage.SetScanScheduleUpdate(percent);
                    onMessage(msg);
                    break;
                }
                case DTVMessage.HI_SVR_EVT_SRCH_FINISH:
                {
                    Log.d(TAG, "scan  freq finished" + param1 + " " + parm2);
                    TVMessage msg = TVMessage.SetScanEnd(mTvChCount, mRadioChCount);
                    onMessage(msg) ;
                }
                break;
                case DTVMessage.HI_SVR_EVT_SRCH_ONE_FREQ_FINISH:
                {
                    Log.d(TAG, "scan one freq finished" + param1 + " " + parm2);
                    break;
                }
                case DTVMessage.HI_SVR_EVT_SRCH_GET_PROG:
                {
                    long channelId = getUnsignedInt(parm2);
                    Log.d(TAG, "find the channel : tpid=" + param1 + " ,channelId=" + channelId);

                    ChannelNode chData = mDtv.getChannelByID(channelId);
                    if (null != chData)
                    {
                        if (HISI_SERVICETYPE_TV == chData.serviceType)
                        {
                            mTvChCount++;
                            chData.LCN = mTvChCount;
                        }
                        else if (HISI_SERVICETYPE_RADIO == chData.serviceType)
                        {
                            mRadioChCount++;
                            chData.LCN = mRadioChCount;
                        }

                        TVMessage msg = TVMessage.SetScanResultUpdate(
                                chData.serviceID,
                                chData.serviceType == HISI_SERVICETYPE_RADIO ? ProgramInfo.ALL_RADIO_TYPE : ProgramInfo.ALL_TV_TYPE,
                                chData.LCN,
                                chData.OrignalServiceName,
                                chData.bCAMode) ;
                        onMessage(msg) ;
                    }
                    break;
                }
                case DTVMessage.HI_SVR_EVT_SRCH_GET_PROG_PESI: {
                    long channelId = getUnsignedInt(parm2);
                    Log.d(TAG, "find the channel : tpid=" + param1 + " ,channelId=" + channelId);
                    if(obj != null) {
                        int count = 0;
                        ChannelNode chData = new ChannelNode();
                        chData.serviceID = ((Parcel) obj).readInt();
                        chData.serviceType = ((Parcel) obj).readInt();
                        chData.LCN = ((Parcel) obj).readInt();
                        chData.OrignalServiceName = ((Parcel) obj).readString();
                        chData.bCAMode = ((Parcel) obj).readInt();
                        if (ProgramInfo.ALL_TV_TYPE == chData.serviceType) {
                            mTvChCount++;
                            count = mTvChCount;
                            Log.d(TAG,"HI_SVR_EVT_SRCH_GET_PROG_PESI get channel !! TV : [channel name] : " + chData.OrignalServiceName +" ca " + chData.bCAMode);
                        } else if (ProgramInfo.ALL_RADIO_TYPE == chData.serviceType) {
                            mRadioChCount++;
                            count = mRadioChCount;
                            Log.d(TAG,"HI_SVR_EVT_SRCH_GET_PROG_PESI get channel !! Radio : [channel name] : " + chData.OrignalServiceName);
                        }
                        TVMessage msg = TVMessage.SetScanResultUpdate(
                                chData.serviceID,
                                chData.serviceType,
                                count,
                                chData.OrignalServiceName,
                                chData.bCAMode);
                        onMessage(msg);
                    }
                    break;
                }
                default:
                    break;
            }
        }
    };

    IDTVListener gBOOKListener = new IDTVListener()
    {
        private static final String CB_TAG="BOOKListener";
        @Override
        public void notifyMessage(int messageID, int param1, int parm2, Object obj)
        {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            switch (messageID)
            {
                case DTVMessage.HI_SVR_EVT_BOOK_REMIND:
                {
                    Log.d(TAG, "HI_SVR_EVT_BOOK_REMIND ID = " + param1) ;
                    BookInfo bookInfo = BookInfoGetComingBook();
                    String name = bookInfo == null ? "" : bookInfo.getEventName();
                    Log.d(TAG, "incoming bookinfo = " + name);
                    break;
                }
                case DTVMessage.HI_SVR_EVT_BOOK_TIME_ARRIVE:
                {
                    Log.d(TAG, "HI_SVR_EVT_BOOK_TIME_ARRIVE ID = " + param1);
                    BookInfo bookInfo = BookInfoGetComingBook();
                    String name = bookInfo == null ? "" : bookInfo.getEventName();
                    Log.d(TAG, "incoming bookinfo = " + name);
                    break;
                }
                case DTVMessage.HI_SVR_EVT_BOOK_TIME_END:
                {
                    Log.d(TAG, "HI_SVR_EVT_BOOK_TIME_END ID = " + param1) ;
                    break;
                }
                default:
                    break;
            }
        }
    };

    IDTVListener gPVRListener = new IDTVListener()
    {
        private static final String CB_TAG="PVRListener";
        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj)
        {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            switch (messageID)
            {
                case DTVMessage.HI_SVR_EVT_PVR_PLAY_EOF:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_PLAY_EOF") ;
                    TVMessage msg = TVMessage.SetPvrEOF();
                    onMessage(msg);
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PVR_PLAY_SOF:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_PLAY_SOF");
                    TVMessage msg = TVMessage.SetPvrPlaytoBegin();
                    onMessage(msg) ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PVR_PLAY_ERROR:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_PLAY_ERROR") ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PVR_PLAY_REACH_REC:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_PLAY_REACH_REC") ;
                    TVMessage msg = TVMessage.SetPvrPlayReachRec();
                    onMessage(msg) ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PVR_REC_DISKFULL:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_REC_DISKFULL");
                    TVMessage msg = TVMessage.SetPvrDISKFULL();
                    onMessage(msg) ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PVR_REC_ERROR:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_REC_ERROR") ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PVR_REC_OVER_FIX:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_REC_OVER_FIX param1 = " + param1 + "param2 = " + param2) ;
                    TVMessage msg = TVMessage.SetPvrOverFix(param1,param2);//Scoty 20180720 add HI_SVR_EVT_PVR_REC_OVER_FIX send recId
                    onMessage(msg) ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PVR_REC_REACH_PLAY:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_REC_REACH_PLAY");
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PVR_REC_DISK_SLOW:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_REC_DISK_SLOW") ;
                    break;
                }
                case DTVMessage.PESI_EVT_PVR_RECORD_START ://eric lin 20180713 pvr msg,-start
                {
                    Log.d(TAG, "PESI_EVT_PVR_RECORD_START") ;
                    TVMessage msg = TVMessage.SetPvrRecordStart(param1, param2);
                    onMessage(msg) ;
                    break;
                }
                case DTVMessage.PESI_EVT_PVR_RECORD_STOP :
                {
                    Log.d(TAG, "PESI_EVT_PVR_RECORD_STOP") ;
                    TVMessage msg = TVMessage.SetPvrRecordStop();
                    onMessage(msg) ;
                    break;
                }
                case DTVMessage.PESI_EVT_PVR_TIMESHIFT_START :
                {
                    Log.d(TAG, "PESI_EVT_PVR_TIMESHIFT_START") ;
                    TVMessage msg = TVMessage.SetPvrTimeshiftStart();
                    onMessage(msg) ;
                    break;
                }
                case DTVMessage.PESI_EVT_PVR_TIMESHIFT_PLAY_START :
                {
                    Log.d(TAG, "PESI_EVT_PVR_RECORD_STOP") ;
                    TVMessage msg = TVMessage.SetPvrTimeshiftPlayStart();
                    onMessage(msg) ;
                    break;
                }
                case DTVMessage.PESI_EVT_PVR_TIMESHIFT_STOP :
                {
                    Log.d(TAG, "PESI_EVT_PVR_RECORD_STOP") ;
                    TVMessage msg = TVMessage.SetPvrTimeshiftStop();
                    onMessage(msg) ;
                    break;
                }
                case DTVMessage.PESI_EVT_PVR_FILE_PLAY_START :
                {
                    Log.d(TAG, "PESI_EVT_PVR_RECORD_STOP") ;
                    TVMessage msg = TVMessage.SetPvrFilePlayStart();
                    onMessage(msg) ;
                    break;
                }
                case DTVMessage.PESI_EVT_PVR_FILE_PLAY_STOP :
                {
                    Log.d(TAG, "PESI_EVT_PVR_RECORD_STOP") ;
                    TVMessage msg = TVMessage.SetPvrFilePlayStop();
                    onMessage(msg) ;
                    break;
                }//eric lin 20180713 pvr msg,-end
                case DTVMessage.PESI_EVT_PVR_PLAY_PARENTAL_LOCK:  //connie 20180806 for pvr parentalRate
                {
                    Log.d(TAG, "PESI_EVT_PVR_PLAY_PARENTAL_LOCK") ;
                    TVMessage msg = TVMessage.SetPvrPlayParanentLock();
                    onMessage(msg) ;
                }break;
                case DTVMessage.HI_SVR_EVT_PVR_OPEN_HD_FINISH:  //Scoty 20180827 add HDD Ready command and callback
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_OPEN_HD_FINISH") ;
                    TVMessage msg = TVMessage.SetPvrHardDiskReady();
                    onMessage(msg) ;
                }break;
                case DTVMessage.HI_SVR_EVT_PVR_TIMESHIFT_PLAY_STOP:  //Scoty 20180827 add HDD Ready command and callback
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_TIMESHIFT_PLAY_STOP") ;
                }break;
                case DTVMessage.HI_SVR_EVT_PVR_NOT_SUPPORT: //Scoty 20180827 add HDD Ready command and callback
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_NOT_SUPPORT") ;
                    TVMessage msg = TVMessage.SetPvrNotSupport();
                    onMessage(msg) ;
                }break;
                default:
                    break;
            }
        }
    };

    IDTVListener gTBMListener = new IDTVListener()
    {
        private static final String CB_TAG="TBMListener";
        @Override
        public void notifyMessage(int messageID, int param1, int parm2, Object obj)
        {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            switch (messageID)
            {
                case DTVMessage.HI_SVR_EVT_TBM_UPDATE:
                {
                    Log.d(TAG, "HI_SVR_EVT_TBM_UPDATE table id = " + param1 + " table size = " + parm2) ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_TIME_UPDATE:
                {
                    Log.d(TAG, "HI_SVR_EVT_TIME_UPDATE pls check!!!!!!");
                    break;
                }
                case DTVMessage.HI_SVR_EVT_TIMEZONE_UPDATE:
                {
                    Log.d(TAG, "HI_SVR_EVT_TIMEZONE_UPDATE timezone sec = " + param1 + " timezonesec size = " + parm2) ;
                    break;
                }
                default:
                    break;
            }
        }
    };

    IDTVListener gCIListener = new IDTVListener()
    {
        private static final String CB_TAG="CIListener";
        @Override
        public void notifyMessage(int messageID, int param1, int parm2, Object obj)
        {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            switch (messageID)
            {
                case DTVMessage.HI_SVR_CI_EVT_CARD_INSERT:
                {
                    Log.d(TAG, "HI_SVR_CI_EVT_CARD_INSERT") ;
                    break;
                }
                case DTVMessage.HI_SVR_CI_EVT_CARD_REMOVE:
                {
                    Log.d(TAG, "HI_SVR_CI_EVT_CARD_REMOVE") ;
                    break;
                }
                case DTVMessage.HI_SVR_CI_EVT_MMI_MESSAGE:
                {
                    Log.d(TAG, "HI_SVR_CI_EVT_MMI_MESSAGE") ;
                    break;
                }
                case DTVMessage.HI_SVR_CI_EVT_MMI_MENU:
                {
                    Log.d(TAG, "HI_SVR_CI_EVT_MMI_MENU") ;
                    break;
                }
                case DTVMessage.HI_SVR_CI_EVT_MMI_LIST:
                {
                    Log.d(TAG, "HI_SVR_CI_EVT_MMI_LIST") ;
                    break;
                }
                case DTVMessage.HI_SVR_CI_EVT_MMI_ENQ:
                {
                    Log.d(TAG, "HI_SVR_CI_EVT_MMI_ENQ") ;
                    break;
                }
                case DTVMessage.HI_SVR_CI_EVT_CLOSE_MMI:
                {
                    Log.d(TAG, "HI_SVR_CI_EVT_CLOSE_MMI") ;
                    break;
                }
                default:
                    break;
            }
        }
    };

    IDTVListener gEWSListener = new IDTVListener()
    {
        private static final String CB_TAG="EWSListener";
        @Override
        public void notifyMessage(int messageID, int param1, int parm2, Object obj)
        {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            switch (messageID)
            {
                case DTVMessage.HI_SVR_EVT_EWS_START:
                {
                    Log.d(TAG, "HI_SVR_EVT_EWS_START  need change to channelID = " + parm2) ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_EWS_STOP:
                {
                    Log.d(TAG, "HI_SVR_EVT_EWS_STOP need change back channelID = " + parm2) ;
                    break;
                }
                default:
                    break;
            }
        }
    };

    IDTVListener gVMXListener = new IDTVListener()//Scoty 20180831 add vmx callback event
    {
        private static final String CB_TAG="VMXListener";
        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj)
        {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            switch (messageID)
            {
                // for VMX need open/close -s
//                case DTVMessage.HI_SVR_EVT_VMX_SHOW_MSG: { // connie 20180903 for VMX -s
//                    Log.d(TAG, "HI_SVR_EVT_VMX_SHOW_MSG") ;
//                    int mode = 0, duration = 0, triggerID = 0, triggerNum =0;
//                    String CAmsg="";
//
//                    mode = param1;
//                    duration = param2;
//                    if(obj != null) {
//                        CAmsg = ((Parcel) obj).readString();
//                        triggerID = ((Parcel) obj).readInt();
//                        triggerNum = ((Parcel) obj).readInt();
//                    }
//                    Log.d(TAG, CB_TAG + "      mode = " + mode + "     CAmsg = " + CAmsg + "     duration = " + duration + "    triggerID ="+ triggerID + "   triggerNum = "+ triggerNum);
//
//                    TVMessage msg = TVMessage.SetVMXShowMsg(mode, CAmsg, duration, triggerID, triggerNum);
//                    onMessage(msg);
//                }
//                break;
                case DTVMessage.HI_SVR_EVT_VMX_OTA_START:
                {
                    Log.d(TAG, "HI_SVR_EVT_VMX_OTA_START") ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_VMX_OTA_ERR:
                {
                    Log.d(TAG, "HI_SVR_EVT_VMX_OTA_ERR") ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_VMX_WATERMARK:
                {
                    Log.d(TAG, "HI_SVR_EVT_VMX_WATERMARK") ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_VMX_WATERMARK_CLOSE:
                {
                    Log.d(TAG, "HI_SVR_EVT_VMX_WATERMARK_CLOSE") ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_VMX_PIN:
                {
                    Log.d(TAG, "HI_SVR_EVT_VMX_PIN") ;
                    int enable = param1;
                    long channelID = getUnsignedInt(param2);
                    int pinIndex = 0, textSelector = 0;
                    if(obj != null) {
                        pinIndex = ((Parcel) obj).readInt();
                        textSelector = ((Parcel) obj).readInt();
                    }
                    Log.d(TAG, CB_TAG + "        enable = " + enable + "    channelID = " + channelID+ "    pinIndex =" + pinIndex + "    textSelector = " +textSelector);
                    TVMessage msg = TVMessage.VMXSetPin(enable, channelID, pinIndex, textSelector);
                    onMessage(msg);
                    break;
                }
                case DTVMessage.HI_SVR_EVT_VMX_IPPV:
                {
                    Log.d(TAG, "HI_SVR_EVT_VMX_IPPV") ;
                    int enable =param1;
                    long channelID = getUnsignedInt(param2);
                    int pinIndex = 0;
                    String curToken = "";
                    String cost = "";
                    if(obj != null) {
                        pinIndex =  ((Parcel) obj).readInt();
                        curToken = ((Parcel) obj).readString();
                        cost = ((Parcel) obj).readString();
                    }
                    Log.d(TAG, CB_TAG + "        enable = " + enable + "    channelID = " + channelID + "    pinIndex = " + pinIndex + "    curToken = " + curToken + "    cost = " + cost);
                    TVMessage msg = TVMessage.VMXIPPV(enable, channelID, pinIndex, curToken, cost);
                    onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_EVT_VMX_BCIO_NOTIFY: // connie 20180925 add for ippv/pin bcio notify
                {
                    int errType =param1;
                    TVMessage msg = TVMessage.VMXBcioNotify(errType);
                    onMessage(msg);
                }break;
                case DTVMessage.HI_SVR_EVT_CARD_DETECT:
                {
                    Log.d(TAG, "HI_SVR_EVT_CARD_DETECT") ;
                    int cardStatus = param1;
                    Log.d(TAG, CB_TAG + "        cardStatus = " + cardStatus);
                    TVMessage msg = TVMessage.VMXCardDetect(cardStatus);
                    onMessage(msg);
                    break;// for VMX need open/close -e
                }
                //case DTVMessage.HI_SVR_EVT_VMX_SEARCH:
                //{
                //    Log.d(TAG, "HI_SVR_EVT_VMX_SEARCH") ;
                //    break;
                //}
                // connie 20180903 for VMX -e
                default:
                    break;
            }
        }
    };

    IDTVListener gLoaderDtvListener = new IDTVListener()
    {
        private static final String CB_TAG="LoaderDtvListener";
        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj)
        {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            switch (messageID)
            {
                case DTVMessage.PESI_LOADERDVT_RETURN_DSMCC_STATUS:
                {
                    Log.d(TAG, "PESI_LOADERDVT_RETURN_DSMCC_STATUS") ;
                    int is_locked = 0, dsi_size = 0;
                    is_locked =  param1;
                    dsi_size = param2;
                    Log.d(TAG, "PESI_LOADERDVT_RETURN_DSMCC_STATUS is_locked = "+ is_locked+ " dsi_size" + dsi_size) ;
                    TVMessage msg = TVMessage.SetDownloadServiceStatus( is_locked, dsi_size);
                    onMessage(msg) ;
                    break;
                }
                default:
                    break;
            }
        }
    };

    IDTVListener gPIOListener = new IDTVListener()
    {
        private static final String CB_TAG="PIOListener";
        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj)
        {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            TVMessage msg;
            switch (messageID)
            {
                case DTVMessage.PESI_EVT_PIO_FRONT_PANEL_KEY_CH_DOWN:
                    Log.d(TAG, "notifyMessage: KEY_CH_DOWN");
                    msg = new TVMessage(TVMessage.FLAG_PIO, TVMessage.TYPE_PIO_FRONT_PANEL_KEY_CH_DOWN);
                    onMessage(msg) ;
                    break;
                case DTVMessage.PESI_EVT_PIO_FRONT_PANEL_KEY_POWER:
                    Log.d(TAG, "notifyMessage: KEY_POWER");
                    msg = new TVMessage(TVMessage.FLAG_PIO, TVMessage.TYPE_PIO_FRONT_PANEL_KEY_POWER);
                    onMessage(msg) ;
                    break;
                case DTVMessage.PESI_EVT_PIO_FRONT_PANEL_KEY_CH_UP:
                    Log.d(TAG, "notifyMessage: KEY_CH_UP");
                    msg = new TVMessage(TVMessage.FLAG_PIO, TVMessage.TYPE_PIO_FRONT_PANEL_KEY_CH_UP);
                    onMessage(msg) ;
                    break;
                case DTVMessage.PESI_EVT_PIO_USB_OVERLOAD:
                    Log.d(TAG, "notifyMessage: USB_OVERLOAD, port = " + param1);
                    msg = TVMessage.SetPioUsbOverload(param1);
                    onMessage(msg) ;
                    break;
                case DTVMessage.PESI_EVT_PIO_ANTENNA_OVERLOAD:
                    Log.d(TAG, "notifyMessage: ANTENNA_OVERLOAD");
                    msg = new TVMessage(TVMessage.FLAG_PIO, TVMessage.TYPE_PIO_ANTENNA_OVERLOAD);
                    onMessage(msg) ;
                    break;
                default:
                    break;
            }
        }
    };

    IDTVListener gSystemListener = new IDTVListener()
    {
        private static final String CB_TAG="SystemListener";
        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj)
        {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            TVMessage msg;
            switch (messageID)
            {
                case DTVMessage.PESI_EVT_SYSTEM_DQA_AUTO_TEST:
                    Log.d(TAG, "notifyMessage: PESI_EVT_SYSTEM_DQA_AUTO_TEST");
                    msg = TVMessage.SetDqaAutoTest(param1);
                    onMessage(msg) ;
                    break;

                default:
                    break;
            }
        }
    };

    //Scoty 20190410 add Mtest Pc Tool callback -s
    IDTVListener gMtestListener = new IDTVListener()
    {
        private static final String CB_TAG="MtestListener";
        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj)
        {
            Log.d(TAG, CB_TAG + " messageID = " + messageID +" param1 = " + param1 + " param2 = " + param2);
            TVMessage msg;
            switch (messageID)
            {
                case DTVMessage.PESI_EVT_MTEST_PCTOOL:
                    Log.d(TAG, "notifyMessage: PESI_EVT_MTEST_PCTOOL");
                    msg = TVMessage.SetMtestPcTool(param1,param2);
                    onMessage(msg);
                    break;
                case DTVMessage.PESI_EVT_MTEST_FPKEY_RESET:
                    Log.d(TAG, "notifyMessage: PESI_EVT_MTEST_FPKEY_RESET");
                    msg = new TVMessage(TVMessage.FLAG_MTEST, TVMessage.TYPE_MTEST_FRONT_PANEL_KEY_RESET);
                    onMessage(msg) ;
                    break;
                case DTVMessage.PESI_EVT_MTEST_FPKEY_WPS:
                    Log.d(TAG, "notifyMessage: PESI_EVT_MTEST_FPKEY_WPS");
                    msg = new TVMessage(TVMessage.FLAG_MTEST, TVMessage.TYPE_MTEST_FRONT_PANEL_KEY_WPS);
                    onMessage(msg) ;
                    break;
                default:
                    break;
            }
        }
    };
    //Scoty 20190410 add Mtest Pc Tool callback -e
    IDTVListener gTestListener = new IDTVListener() {
        private static final String CB_TAG="TestListener";
        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj)
        {
            Log.d(TAG, CB_TAG + " messageID = " + messageID +" param1 = " + param1 + " param2 = " + param2);
            TVMessage msg;
            switch (messageID)
            {
                case DTVMessage.PESI_EVT_PT_DEBUG_CALLBACK_TEST:
                    Log.d(TAG, "notifyMessage: PESI_EVT_PT_DEBUG_CALLBACK_TEST");
                    int ret = 0;
                    String name = null;
                    if(obj != null) {
                        ret =  ((Parcel) obj).readInt();
                        name = ((Parcel) obj).readString();
                    }
                    msg = TVMessage.SetTestCallbackTest(param1,param2, name);
                    onMessage(msg);
                    break;
                default:
                    break;
            }
        }
    };

    final IDTVListener gCaListener = new IDTVListener() {
        private static final String CB_TAG = "CaListener";

        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj) {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            TVMessage msg = null;
            switch (messageID) {
                case DTVMessage.PESI_EVT_CA_WIDEVINE://eric lin 20210107 widevine cas
                    Log.d(TAG, "notifyMessage: PESI_EVT_CA_WIDEVINE");
                    Parcel in = ((Parcel) obj);                    
                    byte[] val = null;
                    widevineMsg wvMsg = new widevineMsg();
                    if (obj != null) {
                        int lens = in.readInt();
                        Log.d(TAG, "notifyMessage: PESI_EVT_CA_WIDEVINE lens="+lens);
                        wvMsg.ecmDataLen = lens;
                        if(lens > 0) {
                            val = new byte[lens];
                            int sessionIndex = -1;
                            in.readByteArray(val);
                            //Log.d(TAG, "notifyMessage: PESI_EVT_CA_WIDEVINE handleMessage() 1 data[0]="+String.format("%02X ", val[0])+", last data="+String.format("%02X ", val[lens-1]));
                            wvMsg.ecmData = val;
                            if(param1 == CALLBACK_EVENT_ECM)
                            {
                                sessionIndex = in.readInt();
                                //Log.d(TAG, "notifyMessage: CALLBACK_EVENT_ECM sessionIndex="+sessionIndex+ ", data[0]="+String.format("%02X ", val[0]));
                                msg = TVMessage.SetCaWVMsg(param1, val, sessionIndex);
                            }else if(param1 == CALLBACK_EVENT_PRIVATE_DATA)
                            {
                                Log.d(TAG, "notifyMessage: CALLBACK_EVENT_PRIVATE_DATA lens="+lens);
                                msg = TVMessage.SetCaWVMsg(param1, val);//eric lin 20210112 widevine scheme data
                        }
                            //for(int i=0; i<lens; i++)
                            //{
                            //    Log.d(TAG, "notifyMessage: PESI_EVT_CA_WIDEVINE data["+i+"]="+String.format("%02X ", val[i]));
                            //}

                        }else{
                            int sessionMode;
                            int wv_index;
                            int es_pid;
                            int ecm_pid;

                            sessionMode = in.readInt();
                            wv_index = in.readInt();
                            es_pid = in.readInt();
                            ecm_pid = in.readInt();
                            Log.d(TAG, "notifyMessage: sessionMode=" + sessionMode + ", wv_index=" + wv_index
                                    + ", es_pid=" + es_pid + ", ecm_pid=" + ecm_pid);
                            wvMsg.sessionMode = sessionMode;
                            wvMsg.wv_index = wv_index;
                            wvMsg.es_pid = es_pid;
                            wvMsg.ecm_pid = ecm_pid;
                            msg = TVMessage.SetCaWVMsg(param1, sessionMode, wv_index, es_pid, ecm_pid);
                        }                        
                    }
                    onMessage(msg);
                    break;
                default:
                    break;
            }
        }
    };
    public void PESI_CMD_CallBackTest() {
        mDtv.PESI_CMD_CallBackTest(DTVMessage.HI_SVR_EVT_AV_PLAY_SUCCESS);
    }

    public int getPlatform()
    {
        return mDtv.getPlatform();
    }

    public int getTempPesiDefaultChannelFlag()
    {
        return mDtv.getTempPesiDefaultChannelFlag();
    }

    public void setTempPesiDefaultChannelFlag(int flag)
    {
        mDtv.setTempPesiDefaultChannelFlag(flag);
    }

    public String GetPesiServiceVersion() {
        return mDtv.GetPesiServiceVersion();
    }

    public String GetApkSwVersion()
    {
        return mDtv.GetApkSwVersion();
    }

    public int GetTunerNum()//Scoty 20181113 add GetTunerNum function
    {
        return mDtv.GetTunerNum();
    }

    public void UpdatePvrSkipList(int groupType, int IncludePVRSkipFlag, int tpId, List<Integer> pvrTpList)//Scoty 20181113 add for dual tuner pvrList//Scoty 20180615 update TV/Radio TotalChannelList
    {
        mDtv.UpdatePvrSkipList(groupType,IncludePVRSkipFlag,tpId,pvrTpList);
    }

    public int MtestGetGPIOStatus(int u32GpioNo) {
        return mDtv.MtestGetGPIOStatus(u32GpioNo);
    }

    public int MtestSetGPIOStatus(int u32GpioNo,int bHighVolt) {
        return mDtv.MtestSetGPIOStatus(u32GpioNo,bHighVolt);
    }

    public int MtestGetATRStatus(int smartCardStatus) {
        return mDtv.MtestGetATRStatus(smartCardStatus);
    }

    public int MtestGetHDCPStatus() {
        return mDtv.MtestGetHDCPStatus();
    }

    public int MtestGetHDMIStatus() {
        return mDtv.MtestGetHDMIStatus();
    }
    public int MtestPowerSave() {
        return mDtv.MtestPowerSave();
    }

    public int MtestSevenSegment(int enable) {
        return mDtv.MtestSevenSegment(enable);
    }

    public int MtestSetAntenna5V(int tunerID, int tunerType, int enable) {
        return mDtv.MtestSetAntenna5V(tunerID, tunerType, enable);
    }

    public int MtestSetBuzzer(int enable) {
        return mDtv.MtestSetBuzzer(enable);
    }

    public int MtestSetLedRed(int enable) {
        return mDtv.MtestSetLedRed(enable);
    }

    public int MtestSetLedGreen(int enable) {
        return mDtv.MtestSetLedGreen(enable);
    }

    public int MtestSetLedOrange(int enable) {
        return mDtv.MtestSetLedOrange(enable);
    }

    public int MtestSetLedWhite(int enable) {
        return mDtv.MtestSetLedWhite(enable);
    }

    public int MtestSetLedOnOff(int status) {
        return mDtv.MtestSetLedOnOff(status);
    }

    public int MtestGetFrontKey(int key) {
        return mDtv.MtestGetFrontKey(key);
    }

    public int MtestSetUsbPower(int enable) {
        return mDtv.MtestSetUsbPower(enable);
    }

    public int MtestTestUsbReadWrite(int portNum, String path) {
        return mDtv.MtestTestUsbReadWrite(portNum, path);
    }

    public int MtestTestAvMultiPlay(int tunerNum, List<Integer> tunerIDs, List<Long> channelIDs) {
        return mDtv.MtestTestAvMultiPlay(tunerNum, tunerIDs, channelIDs);
    }

    public int MtestTestAvStopByTunerID(int tunerID) {
        return mDtv.MtestTestAvStopByTunerID(tunerID);
    }

    public int MtestMicSetInputGain(int value) {
        return mDtv.MtestMicSetInputGain(value);
    }

    public int MtestMicSetLRInputGain(int l_r, int value) {
        return mDtv.MtestMicSetLRInputGain(l_r, value);
    }

    public int MtestMicSetAlcGain(int value) {
        return mDtv.MtestMicSetAlcGain(value);
    }

    public int MtestGetErrorFrameCount(int tunerID) {
        return mDtv.MtestGetErrorFrameCount(tunerID);
    }

    public int MtestGetFrameDropCount(int tunerID) {
        return mDtv.MtestGetFrameDropCount(tunerID);
    }

    public String MtestGetChipID() {
        return mDtv.MtestGetChipID();
    }

    public int MtestStartMtest(String version) {
        return mDtv.MtestStartMtest(version);
    }

    public int MtestConnectPctool() {
        return mDtv.MtestConnectPctool();
    }

    public List<Integer> MtestGetWiFiTxRxLevel() {//Scoty 20190417 add wifi level command
        return mDtv.MtestGetWiFiTxRxLevel();
    }

    public int MtestGetWakeUpMode() {
        return mDtv.MtestGetWakeUpMode();
    }

    public Map<String, Integer> MtestGetKeyStatusMap() {
        return mDtv.MtestGetKeyStatusMap();
    }

    public int MtestEnableOpt(boolean enable) {
        return mDtv.MtestEnableOpt(enable);
    }

    public int Record_Start_V2_with_Duration(long channelId, int durationSec, boolean doCipher, PVREncryption pvrEncryption)
    {
        return mDtv.Record_Start_V2_with_Duration(channelId, durationSec, doCipher, pvrEncryption);
    }

    public int Record_Start_V2_with_FileSize(long channelId, int fileSizeMB, boolean doCipher, PVREncryption pvrEncryption)
    {
        return mDtv.Record_Start_V2_with_FileSize(channelId, fileSizeMB, doCipher, pvrEncryption);
    }

    public int TimeShift_Start_V2(int durationSec, int fileSizeMB, boolean doCipher, PVREncryption pvrEncryption)
    {
        return mDtv.TimeShift_Start_V2(durationSec, fileSizeMB, doCipher, pvrEncryption);
    }

    public List<PvrFileInfo> Pvr_Get_Records_File(int startIndex, int total)
    {
        return mDtv.Pvr_Get_Records_File(startIndex, total);
    }

    public int Pvr_Get_Total_Rec_Num()
    {
        return mDtv.Pvr_Get_Total_Rec_Num();
    }

    public int Pvr_Get_Total_One_Series_Rec_Num(String recordUniqueId)
    {
        return mDtv.Pvr_Get_Total_One_Series_Rec_Num(recordUniqueId);
    }

    public List<PvrFileInfo> Pvr_Get_Total_One_Series_Records_File(int startIndex, int total, String recordUniqueId)
    {
        return mDtv.Pvr_Get_Total_One_Series_Records_File(startIndex, total, recordUniqueId);
    }

    public int Pvr_Delete_Total_Records_File()
    {
        return mDtv.Pvr_Delete_Total_Records_File();
    }

    public int Pvr_Delete_One_Series_Folder(String recordUniqueId)
    {
        return mDtv.Pvr_Delete_One_Series_Folder(recordUniqueId);
    }

    public int Pvr_Delete_Record_File_By_Ch_Id(int channelId)
    {
        return mDtv.Pvr_Delete_Record_File_By_Ch_Id(channelId);
    }

    public int Pvr_Delete_Record_File(String recordUniqueId)
    {
        return mDtv.Pvr_Delete_Record_File(recordUniqueId);
    }

    public int PvrRecordStart(int pvrPlayerID , long channelID, String recordPath, int duration)
    {
        return mDtv.pvrRecordStart(pvrPlayerID, channelID, recordPath, duration);
    }

    public int PvrRecordStart(int pvrPlayerID , long channelID, String recordPath, int duration, PVREncryption pvrEncryption)
    {
        return mDtv.pvrRecordStart(pvrPlayerID, channelID, recordPath, duration, pvrEncryption);
    }

    public int PvrRecordStop(int pvrPlayerID, int recId)
    {
        return mDtv.pvrRecordStop(pvrPlayerID, recId);
    }

    public int PvrRecordGetAlreadyRecTime(int pvrPlayerID, int recId)
    {
        return mDtv.pvrRecordGetAlreadyRecTime(pvrPlayerID, recId);
    }

    public int PvrRecordGetStatus(int pvrPlayerID, int recId)
    {
        return mDtv.pvrRecordGetStatus(pvrPlayerID, recId);
    }

    public String PvrRecordGetFileFullPath(int pvrPlayerID, int recId)
    {
        return mDtv.pvrRecordGetFileFullPath(pvrPlayerID, recId);
    }

    public int PvrRecordGetProgramId(int pvrPlayerID, int recId)
    {
        return mDtv.pvrRecordGetProgramId(pvrPlayerID, recId);
    }

    public int PvrRecordCheck(long channelID)
    {
        return mDtv.pvrRecordCheck(channelID);
    }

    public List<PvrInfo> PvrRecordGetAllInfo()
    {
        return mDtv.pvrRecordGetAllInfo();
    }

    public int PvrRecordGetMaxRecNum()
    {
        return mDtv.pvrRecordGetMaxRecNum();
    }

    public int PvrPlayFileCheckLastViewPoint(String fullName)
    {
        return mDtv.pvrPlayFileCheckLastViewPoint(fullName);
    }

    public int PvrSetStartPositionFlag(int startPositionFlag)
    {
        return mDtv.pvrSetStartPositionFlag(startPositionFlag);
    }

    public int PvrTimeShiftStart(int playerID, int time, int filesize, String filePath)
    {
        return mDtv.pvrTimeShiftStart(playerID, time, filesize, filePath);
    }

    public int PvrTimeShiftStop(int playerID)
    {
        return mDtv.pvrTimeShiftStop(playerID);
    }

    public int PvrTimeShiftPlay(int playerID)
    {
        return mDtv.pvrTimeShiftPlay(playerID);
    }

    public int pvrTimeShiftResume(int playerID)//Scoty 20181106 add for separate Play and Pause key
    {
        return mDtv.pvrTimeShiftResume(playerID);
    }

    public int pvrTimeShiftPause(int playerID)//Scoty 20181106 add for separate Play and Pause key
    {
        return mDtv.pvrTimeShiftPause(playerID);
    }

    public int pvrTimeShiftLivePause(int playerID)//Scoty 20180827 add and modify TimeShift Live Mode
    {
        return mDtv.pvrTimeShiftLivePause(playerID);
    }

    public int pvrTimeShiftFilePause(int playerID)//Scoty 20180827 add and modify TimeShift Live Mode
    {
        return mDtv.pvrTimeShiftFilePause(playerID);
    }

    public int PvrTimeShiftTrickPlay(int playerID, EnTrickMode mode)
    {
        return mDtv.pvrTimeShiftTrickPlay(playerID, mode);
    }

    public int PvrTimeShiftSeekPlay(int playerID, int seekSec)
    {
        return mDtv.pvrTimeShiftSeekPlay(playerID, seekSec);
    }

    public Date PvrTimeShiftGetPlayedTime(int playerID)
    {
        return mDtv.pvrTimeShiftGetPlayedTime(playerID);
    }

    public int PvrTimeShiftGetPlaySecond(int playerID)
    {
        return mDtv.pvrTimeShiftGetPlaySecond(playerID);
    }

    public Date PvrTimeShiftGetBeginTime(int playerID)
    {
        return mDtv.pvrTimeShiftGetBeginTime(playerID);
    }

    public int PvrTimeShiftGetBeginSecond(int playerID)
    {
        return mDtv.pvrTimeShiftGetBeginSecond(playerID);
    }

    public int PvrTimeShiftGetRecordTime(int playerID)
    {
        return mDtv.pvrTimeShiftGetRecordTime(playerID);
    }

    public int PvrTimeShiftGetStatus(int playerID)
    {
        return mDtv.pvrTimeShiftGetStatus(playerID);
    }

    public EnTrickMode PvrTimeShiftGetCurrentTrickMode(int playerID)
    {
        return mDtv.pvrTimeShiftGetCurrentTrickMode(playerID);
    }

    public int PvrPlayStart(String filePath)
    {
        return mDtv.pvrPlayStart(filePath);
    }

    public int PvrPlayStart(String filePath, PVREncryption pvrEncryption)
    {
        return mDtv.pvrPlayStart(filePath, pvrEncryption);
    }

    public int PvrPlayStop()
    {
        return mDtv.pvrPlayStop();
    }

    public int PvrPlayPause()
    {
        return mDtv.pvrPlayPause();
    }

    public int PvrPlayResume()
    {
        return mDtv.pvrPlayResume();
    }

    public int PvrPlayTrickPlay(EnTrickMode enSpeed)
    {
        return mDtv.pvrPlayTrickPlay(enSpeed);
    }

    public int PvrPlaySeekTo(int sec)
    {
        return mDtv.pvrPlaySeekTo(sec);
    }

    public int PvrPlayGetPlayTime()
    {
        return mDtv.pvrPlayGetPlayTime();
    }

    public EnTrickMode PvrPlayGetCurrentTrickMode()
    {
        return mDtv.pvrPlayGetCurrentTrickMode();
    }

    public int PvrPlayGetCurrentStatus()
    {
        return mDtv.pvrPlayGetCurrentStatus();
    }

    public long PvrPlayGetSize()
    {
        return mDtv.pvrPlayGetSize();
    }

    public int PvrPlayGetDuration()
    {
        return mDtv.pvrPlayGetDuration();
    }

    public Resolution PvrPlayGetVideoResolution()
    {
        return mDtv.pvrPlayGetVideoResolution();
    }

    public boolean PvrPlayIsRadio(String fullName)
    {
        return mDtv.pvrPlayIsRadio(fullName);
    }

    public String PvrPlayGetFileFullPath(int pvrPlayerID)
    {
        return mDtv.pvrPlayGetFileFullPath(pvrPlayerID);
    }

    public AudioInfo.AudioComponent PvrPlayGetCurrentAudio()
    {
        return mDtv.pvrPlayGetCurrentAudio();
    }

    public AudioInfo PvrPlayGetAudioComponents()
    {
        return mDtv.pvrPlayGetAudioComponents();
    }

    public int PvrPlaySelectAudio(AudioInfo.AudioComponent audio)
    {
        return mDtv.pvrPlaySelectAudio(audio);
    }

    public int PvrPlaySetWindowRect(Rect rect)
    {
        return mDtv.pvrPlaySetWindowRect(rect);
    }

    public int PvrPlaySetTrackMode(EnAudioTrackMode enTrackMode)
    {
        return mDtv.pvrPlaySetTrackMode(enTrackMode);
    }

    public EnAudioTrackMode PvrPlayGetTrackMode()
    {
        return mDtv.pvrPlayGetTrackMode();
    }

    public int PvrFileRemove(String filePath)
    {
        return mDtv.pvrFileRemove(filePath);
    }

    public int PvrFileRename(String oldName, String newName)
    {
        return mDtv.pvrFileRename(oldName, newName);
    }

    public int PvrFileGetDuration(String fullName)
    {
        return mDtv.pvrFileGetDuration(fullName);
    }

    public long PvrFileGetSize(String fullName)
    {
        return mDtv.pvrFileGetSize(fullName);
    }

    public PvrFileInfo PvrFileGetAllInfo(String fullName)
    {
        return mDtv.pvrFileGetAllInfo(fullName);
    }

    public PvrFileInfo PvrFileGetExtraInfo(String fullName)
    {
        return mDtv.pvrFileGetExtraInfo(fullName);
    }

    public PvrFileInfo pvrFileGetEpgInfo(String fullName, int epgIndex)
    {
        return mDtv.pvrFileGetEpgInfo(fullName, epgIndex);
    }
//    public int PvrGetCurrentRecMode()
//    {
//        return mDtv.pvrGetCurrentRecMode();
//    }

    public int PvrGetCurrentPvrMode(long channelId)
    {
        return mDtv.pvrGetCurrentPvrMode(channelId);
    }

    public int PvrSetParentLockOK()  //connie 20180806 for pvr parentalRate
    {
        return mDtv.PvrSetParentLockOK();
    }

    // edwin 20180809 add PvrTotalRecordFileXXX -s
    public int pvrTotalRecordFileOpen(String dirPath)
    {
        return mDtv.pvrTotalRecordFileOpen(dirPath);
    }

    public int pvrTotalRecordFileClose()
    {
        return mDtv.pvrTotalRecordFileClose();
    }

    public int pvrTotalRecordFileSort(int sortType)
    {
        // PVR_SORT_BY_CHNAME	=0,
        // PVR_SORT_BY_DATETIME	=1,
        // PVR_SORT_BUTT
        return mDtv.pvrTotalRecordFileSort(sortType);
    }

    public List<PvrFileInfo> pvrTotalRecordFileGet(int startIndex, int total)
    {
        return mDtv.pvrTotalRecordFileGet(startIndex, total);
    }
    // edwin 20180809 add PvrTotalRecordFileXXX -e

    public int pvrCheckHardDiskOpen(String FilePath)//Scoty 20180827 add HDD Ready command and callback
    {
        return mDtv.pvrCheckHardDiskOpen(FilePath);
    }

    public int pvrPlayTimeShiftStop()//Scoty 20180827 add and modify TimeShift Live Mode
    {
        return mDtv.pvrPlayTimeShiftStop();
    }

    public int pvrRecordGetLivePauseTime()//Scoty 20180827 add and modify TimeShift Live Mode
    {
        return mDtv.pvrRecordGetLivePauseTime();
    }

    //    public int PvrGetCurrentPlayMode()
//    {
//        return mDtv.pvrGetCurrentPlayMode();
//    }

    public int PvrGetRatio()
    {
        return mDtv.pvrGetRatio();
    }

    public void ResetTotalChannelList() {
        mDtv.ResetTotalChannelList();
    }

    public String GetRecordPath()
    {
        return mDtv.GetRecordPath();
    }

    public void SetRecordPath(String path)
    {
        mDtv.setRecordPath(path);
    }

    public String getDefaultRecPath()//Scoty 20180525 add get default record path
    {
       return mDtv.getDefaultRecPath();
    }

    public int recordTS_start(int TunerId, String FullName) // connie 20180803 add record ts -s
    {
        return mDtv.recordTS_start(TunerId, FullName);
    }

    public int recordTS_stop()
    {
        return mDtv.recordTS_stop();
    }// connie 20180803 add record ts -e

    public void TestSetTvRadioCount(int tvCount, int RadioCount)
    {
        mDtv.TestSetTvRadioCount(tvCount,RadioCount);
    }

    public int TestChangeTuner(int tunerType)//Scoty 20180817 add Change Tuner Command
    {
        return mDtv.TestChangeTuner(tunerType);
    }

    //PIP -start
    public int PipOpen(int x, int y, int width, int height)
    {
        return mDtv.PipOpen(x,y,width,height);
    }

    public int PipClose()
    {
        return mDtv.PipClose();
    }

    public int PipStart(long channelId, int show)
    {
        return mDtv.PipStart(channelId,show);
    }

    public int PipStop()
    {
        return mDtv.PipStop();
    }

    public int PipSetWindow(int x, int y, int width, int height)
    {
        return mDtv.PipSetWindow(x,y,width,height);
    }

    public int PipExChange()
    {
        return mDtv.PipExChange();
    }
    //PIP -end

    //Device -start
    public List<Integer> GetUsbPortList()
    {
        return mDtv.GetUsbPortList();
    }
    //Device -ecd

    public int FrontEndSetDiSEqC10PortInfo(int nTuerID, int nPort, int n22KSwitch, int nPolarity)   // Johnny 20180814 add setDiseqc1.0 port
    {
        return mDtv.setDiSEqC10PortInfo(nTuerID, nPort, n22KSwitch, nPolarity);
    }
    //Scoty add DiSeqC Motor rule -s
    public int setDiSEqC12MoveMotor(int nTunerId, int Direct, int Step)
    {
        return mDtv.setDiSEqC12MoveMotor(nTunerId, Direct, Step);
    }

    public int setDiSEqC12MoveMotorStop(int nTunerId)
    {
        return mDtv.setDiSEqC12MoveMotorStop(nTunerId);
    }

    public int resetDiSEqC12Position(int nTunerId)
    {
        return mDtv.resetDiSEqC12Position(nTunerId);
    }

    public int setDiSEqCLimitPos(int nTunerId, int limitType)
    {
        return mDtv.setDiSEqCLimitPos(nTunerId,limitType);
    }
    //Scoty add DiSeqC Motor rule -e
    public int SetStandbyOnOff(int onOff)//Scoty 20180831 add standby function
    {
        return mDtv.SetStandbyOnOff(onOff);
    }

    // for VMX need open/close -s
    public LoaderInfo GetLoaderInfo()
    {
        return mDtv.GetLoaderInfo();
    } // connie 20180903 for VMX -s

    public CaStatus GetCAStatusInfo()
    {
        return mDtv.GetCAStatusInfo();
    }

    public int GetECMcount()
    {
        return mDtv.GetECMcount();
    }

    public int GetEMMcount()
    {
        return mDtv.GetEMMcount();
    }

    public String GetLibDate()
    {
        return mDtv.GetLibDate();
    }

    public String GetChipID()
    {
        return mDtv.GetChipID();
    }

    public String GetSN()
    {
        return mDtv.GetSN();
    }

    public String GetCaVersion()
    {
        return mDtv.GetCaVersion();
    }

    public String GetSCNumber()
    {
        return mDtv.GetSCNumber();
    }

    public int GetPairingStatus()
    {
        return mDtv.GetPairingStatus();
    }

    public String GetPurse()
    {
        return mDtv.GetPurse();
    }

    public int GetGroupM()
    {
        return mDtv.GetGroupM();
    }


    public int SetPinCode(String pinCode, int PinIndex, int TextSelect)
    {
        return mDtv.SetPinCode(pinCode, PinIndex, TextSelect);
    }

    public int SetPPTV(String pinCode, int pinIndex)
    {
        return mDtv.SetPPTV(pinCode, pinIndex);
    }

    public void SetOMSMok()
    {
        mDtv.SetOMSMok();
    }

    public void VMXTest(int mode)
    {
        Log.d(TAG, "VMXTest: ");
        mDtv.VMXTest(mode);
    }
    //Scoty 20181207 modify VMX OTA rule -s
    public void TestVMXOTA(int mode)
    {
        Log.d(TAG, "TestVMXOTA: ");
        mDtv.TestVMXOTA(mode);
    }

    public void VMXAutoOTA(int OTAMode, int TriggerID, int TriggerNum, int TunerId, int SatId, int DsmccPid, int FreqNum, ArrayList<Integer> FreqList, ArrayList<Integer> BandwidthList ) // connie 20180919 add for vmx search
    {
        mDtv.VMXAutoOTA(OTAMode, TriggerID, TriggerNum, TunerId, SatId, DsmccPid, FreqNum, FreqList, BandwidthList);
    }
    //Scoty 20181207 modify VMX OTA rule -e
    public String VMXGetBoxID()
    {
        return mDtv.VMXGetBoxID();
    }

    public String VMXGetVirtualNumber()
    {
        return mDtv.VMXGetVirtualNumber();
    }

    public void VMXStopEWBS(int mode)//Scoty 20181225 modify VMX EWBS rule//Scoty 20181218 add stop EWBS
    {
        mDtv.VMXStopEWBS(mode);
    }

    public void VMXStopEMM()
    {
        mDtv.VMXStopEMM();
    } // connie 20180903 for VMX -e

    public void VMXOsmFinish(int triggerID, int triggerNum)
    {
        mDtv.VMXOsmFinish(triggerID, triggerNum);
    }

    public VMXProtectData GetProtectData()
    {
        return mDtv.GetProtectData();
    }

    public int SetProtectData(int first, int second, int third)
    {
        return mDtv.SetProtectData(first, second, third);
    }
    // for VMX need open/close -e

    public void SetEnterViewActivity(int enter) {
        mDtv.EnterViewActivity(enter);
    }

    public void EnableMemStatusCheck(int enable) {
        mDtv.EnableMemStatusCheck(enable);
    }

    public int LoaderDtvGetJTAG() { return mDtv.LoaderDtvGetJTAG(); }

    public int LoaderDtvSetJTAG(int value) { return mDtv.LoaderDtvSetJTAG(value); }

    public int LoaderDtvCheckISDBTService(OTATerrParameters ota) {return mDtv.LoaderDtvCheckISDBTService(ota); }

    public int LoaderDtvCheckTerrestrialService(OTATerrParameters ota) {return mDtv.LoaderDtvCheckTerrestrialService(ota); }

    public int LoaderDtvCheckCableService(OTACableParameters ota) {return mDtv.LoaderDtvCheckCableService(ota); }

    public int LoaderDtvGetSTBSN() {return mDtv.LoaderDtvGetSTBSN(); }

    public int LoaderDtvGetChipSetId() {return mDtv.LoaderDtvGetChipSetId(); }

    public int LoaderDtvGetSWVersion() {return mDtv.LoaderDtvGetSWVersion(); }

    public int InvokeTest() {
        return mDtv.InvokeTest();
    }

    public void WidevineSetSessionId(int sessionIndex, int sessionId){//eric lin 20210107 widevine cas
//        Log.d(TAG, "WidevineSetSessionId");
        mDtv.WidevineCasSessionId(sessionIndex, sessionId);
    }

    public int SetNetStreamInfo(int GroupType, NetProgramInfo netStreamInfo)
    {
        return mDtv.SetNetStreamInfo(GroupType,netStreamInfo);
    }

    public void SetExoPlayer(SimpleExoPlayer player)
    {
        mDtv.SetExoPlayer(player);
    }

    public SimpleExoPlayer GetExoPlayer()
    {
        return mDtv.GetExoPlayer();
    }

    public void SetExoplayerSurfaceView(SurfaceView surfaceView)
    {
        mDtv.SetExoplayerSurfaceView(surfaceView);
    }

    public SurfaceView GetExoplayerSurfaceView()
    {
        return mDtv.GetExoplayerSurfaceView();
    }

    public void SetYoutubeWebview(WebView webview)
    {
        mDtv.SetYoutubeWebview(webview);
    }

    public WebView GetYoutubeWebview()
    {
        return mDtv.GetYoutubeWebview();
    }

    public int ResetProgramDatabase()
    {
        return mDtv.ResetProgramDatabase();
    }

    public int ResetNetProgramDatabase()
    {
        return mDtv.ResetNetProgramDatabase();
    }
}