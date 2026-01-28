package com.prime.dtv;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.service.Scan.AutoUpdateManager;
import com.prime.dtv.service.database.dvbdatabasetable.PvrInfoDatabaseTable;
import com.prime.dtv.service.system.TimerThread;
import com.prime.dtv.sysdata.ChannelNode;
import com.prime.dtv.sysdata.PvrDbRecordInfo;
import com.prime.dtv.sysdata.PvrRecFileInfo;
import com.prime.dtv.sysdata.PvrRecIdx;
import com.prime.dtv.sysdata.PvrRecStartParam;
import com.prime.dtv.sysdata.EnTableType;
import com.prime.dtv.sysdata.EnTrickMode;
import com.prime.dtv.sysdata.NetProgramInfo;
import com.prime.dtv.sysdata.PVREncryption;
import com.prime.dtv.sysdata.PvrFileInfo;
import com.prime.dtv.sysdata.Resolution;
import com.prime.dtv.sysdata.OTACableParameters;
import com.prime.dtv.sysdata.OTATerrParameters;
import com.prime.dtv.Interface.PesiDtvFrameworkInterface;
import com.prime.dtv.service.CommandManager.AvCmdManager;
import com.prime.dtv.service.CommandManager.BookCmdManager;
import com.prime.dtv.service.CommandManager.CaCmdManager;
import com.prime.dtv.service.CommandManager.CfgCmdManager;
import com.prime.dtv.service.CommandManager.EpgCmdManager;
import com.prime.dtv.service.CommandManager.FrontendCmdManager;
import com.prime.dtv.service.CommandManager.MtestCmdManager;
import com.prime.dtv.service.CommandManager.OTACmdManager;
import com.prime.dtv.service.CommandManager.PmCmdManager;
import com.prime.dtv.service.CommandManager.PvrCmdManager;
import com.prime.dtv.service.CommandManager.ScanCmdManager;
import com.prime.dtv.service.CommandManager.SsuCmdManager;
import com.prime.dtv.service.CommandManager.TimeControlCmdManager;
import com.prime.dtv.service.CommandManager.VMXCmdManager;
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
import com.prime.dtv.utils.LogUtils;
import com.prime.dtv.utils.TVScanParams;
import com.prime.dtv.utils.TVTunerParams;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PesiDtvFramework extends PesiDtvFrameworkInterface {
    private static final String TAG = "PesiDtvFramework";
    private static final int PLATFORM_HISI = 0;
    private static final int PLATFORM_PESI = 1;
    private static PesiDtvFramework mPesiDtvFramework = null ;
    private Context SetSurfaceContext = null;
    private SurfaceView mSubTeletextSurfaceView = null;
    private SurfaceHolder mSubSurfaceHolder = null;
    //private SurfaceView mSurfaceView = null;
    //private SurfaceView mPipSurface = null;
    private CallbackManager mCallbackManager = null ;
    //private TableManager mTableManager = null ;
    private PmCmdManager mPmManager = null;
    private AvCmdManager mAvCmdManager = null;
    private BookCmdManager mBookCmdManager = null;
    private CaCmdManager mCaCmdManager = null;
    private CfgCmdManager mCfgCmdManager = null;
    private EpgCmdManager mEpgCmdManager = null;
    private FrontendCmdManager mFrontendCmdManager = null;
    private OTACmdManager mOTACmdManager = null;
    private ScanCmdManager mScanCmdManager = null;
    private SsuCmdManager mSsuCmdManager = null;
    private TimeControlCmdManager mTimeControlCmdManager = null;
    private VMXCmdManager mVMXCmdManager = null;
    private PvrCmdManager mPvrCmdManager = null;
    private MtestCmdManager mMtestCmdManager = null;
    private AutoUpdateManager mAutoUpdateManager = null;

    private TimerThread mTimerThrad = null;

    private static boolean isTunerInited = false;

    public static final String PESI_DTV_FRAMEWORK_VERSION = "PESI TV Framework V1.0.0";

    /*
    system
     */
    public final static PesiDtvFrameworkInterface getInstance(Context context)
    {
        Log.e(TAG, "getInstance");
        if ( mPesiDtvFramework == null ) {
            mPesiDtvFramework = new PesiDtvFramework(context);
        }

        return mPesiDtvFramework ;
    }

    public PesiDtvFramework(Context context )
    {
        mCallbackManager = new CallbackManager() ;
        //mTableManager = new TableManager(context, mCallbackManager.getCallbackHandler());
        mPmManager = new PmCmdManager(context, mCallbackManager.getCallbackHandler());
        mAvCmdManager = new AvCmdManager(context, mCallbackManager.getCallbackHandler());
        mBookCmdManager = new BookCmdManager(context, mCallbackManager.getCallbackHandler());
        mCaCmdManager = new CaCmdManager(context, mCallbackManager.getCallbackHandler());
        mCfgCmdManager = new CfgCmdManager(context, mCallbackManager.getCallbackHandler());
        mEpgCmdManager = new EpgCmdManager(context, mCallbackManager.getCallbackHandler());
        mFrontendCmdManager = new FrontendCmdManager(context, mCallbackManager.getCallbackHandler());
        mOTACmdManager = new OTACmdManager(context, mCallbackManager.getCallbackHandler());
        mScanCmdManager = new ScanCmdManager(context, mCallbackManager.getCallbackHandler());
        mSsuCmdManager = new SsuCmdManager(context, mCallbackManager.getCallbackHandler());
        mTimeControlCmdManager = new TimeControlCmdManager(context, mCallbackManager.getCallbackHandler());
        mVMXCmdManager = new VMXCmdManager(context, mCallbackManager.getCallbackHandler());
        mPvrCmdManager = new PvrCmdManager(context, mCallbackManager.getCallbackHandler());
        mMtestCmdManager = new MtestCmdManager(context, mCallbackManager.getCallbackHandler());
        mTimerThrad = new TimerThread(context, mCallbackManager.getCallbackHandler());
        mTimerThrad.start();
    }

    public final static void destroy() {
        if(mPesiDtvFramework != null) {
            if(mPesiDtvFramework.mPmManager != null)
                mPesiDtvFramework.mPmManager.destroy();
            if(mPesiDtvFramework.mAvCmdManager != null)
                mPesiDtvFramework.mAvCmdManager.destroy();
            if(mPesiDtvFramework.mBookCmdManager != null)
                mPesiDtvFramework.mBookCmdManager.destroy();
            if(mPesiDtvFramework.mCaCmdManager != null)
                mPesiDtvFramework.mCaCmdManager.destroy();
            if(mPesiDtvFramework.mCfgCmdManager != null)
                mPesiDtvFramework.mCfgCmdManager.destroy();
            if(mPesiDtvFramework.mEpgCmdManager != null)
                mPesiDtvFramework.mEpgCmdManager.destroy();
            if(mPesiDtvFramework.mFrontendCmdManager != null)
                mPesiDtvFramework.mFrontendCmdManager.destroy();
            if(mPesiDtvFramework.mOTACmdManager != null)
                mPesiDtvFramework.mOTACmdManager.destroy();
            if(mPesiDtvFramework.mScanCmdManager != null)
                mPesiDtvFramework.mScanCmdManager.destroy();
            if(mPesiDtvFramework.mSsuCmdManager != null)
                mPesiDtvFramework.mSsuCmdManager.destroy();
            if(mPesiDtvFramework.mTimeControlCmdManager != null)
                mPesiDtvFramework.mTimeControlCmdManager.destroy();
            if(mPesiDtvFramework.mVMXCmdManager != null)
                mPesiDtvFramework.mVMXCmdManager.destroy();
            if(mPesiDtvFramework.mPvrCmdManager != null)
                mPesiDtvFramework.mPvrCmdManager.destroy();
            if(mPesiDtvFramework.mMtestCmdManager != null)
                mPesiDtvFramework.mMtestCmdManager.destroy();
            if(mPesiDtvFramework.mTimerThrad != null){
                try {
                    mPesiDtvFramework.mTimerThrad.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                mPesiDtvFramework.mTimerThrad = null;
            }
            mPesiDtvFramework = null;
        }
    }

    @Override
    public void unlinkDeathNotify() {

    }

    @Override
    public void linkDeathNotify() {

    }

    @Override
    public void setOnServiceDiedListener(OnServiceDiedListener listener) {

    }

    @Override
    public int subScribeEvent(int eventID, IDTVListener eventListener, int privateParam) {
        return mCallbackManager.subScribeEvent(eventID,eventListener,privateParam);
    }

    @Override
    public int unSubScribeEvent(int eventID, IDTVListener eventListener) {
        return mCallbackManager.unSubScribeEvent(eventID,eventListener);
    }

    @Override
    public void ResetFactoryDefault() {
        mCfgCmdManager.ResetFactoryDefault();
    }

    @Override
    public int getPlatform() {
        return PLATFORM_PESI;
    }

    /*
    Gpos
    */
    @Override
    public String GetPesiServiceVersion() {
        return mCfgCmdManager.GetPesiServiceVersion();
    }

    @Override
    public String GetApkSwVersion() {
        return null;
    }

    @Override
    public int SetStandbyOnOff(int onOff) {
        return 0;
    }

    @Override
    public void backupDatabase(boolean force) {
        mCfgCmdManager.backupDatabase(force);
    }

    @Override
    public GposInfo GposInfoGet() {
        return mCfgCmdManager.GposInfoGet();
    }

    @Override
    public void GposInfoUpdate(GposInfo gPos) {
        mCfgCmdManager.GposInfoUpdate(gPos);
    }

    @Override
    public void GposInfoUpdateByKeyString(String key, String value) {
        mCfgCmdManager.GposInfoUpdateByKeyString(key, value);
    }

    @Override
    public void GposInfoUpdateByKeyString(String key, int value) {
        mCfgCmdManager.GposInfoUpdateByKeyString(key, value);
    }

    /*
    channel & program
    */
    @Override
    public ArrayList<List<SimpleChannel>> getProgramManagerTotalChannelList() {
        return mPmManager.getProgramManagerTotalChannelList();
    }

    @Override
    public ArrayList<FavGroupName> getAllProgramGroup() {
        return mPmManager.getAllProgramGroup();
    }

    @Override
    public List<ProgramInfo> getProgramInfoList(int type, int pos, int num) {
        return mPmManager.getProgramInfoList(type, pos, num);
    }

    @Override
    public ProgramInfo getProgramByChannelId(long channelId) {
        return mPmManager.getProgramByChannelId(channelId);
    }

    @Override
    public ProgramInfo getProgramByChnum(int chnum, int type) {
        return mPmManager.getProgramByChnum(chnum, type);
    }

    @Override
    public ProgramInfo getProgramByLcn(int lcn, int type) {
        return mPmManager.getProgramByLcn(lcn, type);
    }

    @Override
    public ProgramInfo getProgramByServiceId(int service_id) {
        return mPmManager.getProgramByServiceId(service_id);
    }

    @Override
    public ProgramInfo getProgramByServiceIdTransportStreamId(int service_id, int ts_id) {
        return mPmManager.getProgramByServiceIdTransportStreamId(service_id,ts_id);
    }

    @Override
    public int updateProgramInfo(ProgramInfo pProgram) {
        return mPmManager.updateProgramInfo(pProgram);
    }

    @Override
    public int updateSimpleChannelList(List<SimpleChannel> pPrograms, int type) {
        return mPmManager.updateSimpleChannelList(pPrograms, type);
    }

    @Override
    public void deleteProgram(long channelId) {
        mPmManager.deleteProgram(channelId);
    }

    @Override
    public int saveTable(EnTableType option) {
        return mPmManager.saveTable(option);
    }

    @Override
    public void saveGposKeyValue(String key, String value) {
        mPmManager.saveGposKeyValue(key,value);
    }

    @Override
    public void saveGposKeyValue(String key, int value) {
        mPmManager.saveGposKeyValue(key,value);
    }

    @Override
    public void saveGposKeyValue(String key, long value) {
        mPmManager.saveGposKeyValue(key,value);
    }

    @Override
    public int setDefaultOpenChannel(long channelId, int groupType) {
        mPmManager.setDefaultOpenChannel(channelId, groupType);
        return mPmManager.saveGposKeyValue(GposInfo.GPOS_CUR_CHANNELID, channelId);
    }

    @Override
    public DefaultChannel getDefaultChannel() {
        return mPmManager.getDefaultChannel();
    }

    @Override
    public ChannelNode getChannelByID(long channelId) {
        return mPmManager.getChannelByID(channelId);
    }

    @Override
    public void updateCurPlayChannelList(Context context, int includePVRSkipFlag) {
        mPmManager.updateCurPlayChannelList(context, includePVRSkipFlag);
    }

    @Override
    public void updateCurPlayChannelList(int includePVRSkipFlag) {
        mPmManager.updateCurPlayChannelList(includePVRSkipFlag);
    }

    @Override
    public void resetTotalChannelList() {
        mPmManager.resetTotalChannelList();
    }

    @Override
    public List<SimpleChannel> getCurPlayChannelList(int type, int includePVRSkipFlag) {
        return mPmManager.getCurPlayChannelList(type, includePVRSkipFlag);
    }

    @Override
    public int getCurPlayChannelListCnt(int type) {
        return mPmManager.getCurPlayChannelListCnt(type);
    }

    @Override
    public List<SimpleChannel> getSimpleProgramList(int type, int includeSkipFlag, int includePVRSkipFlag) {
        return mPmManager.getSimpleProgramList(type, includeSkipFlag, includePVRSkipFlag);
    }

    @Override
    public List<SimpleChannel> getSimpleProgramListfromTotalChannelList(int type, int includeSkipFlag, int includePVRSkipFlag) {
        return mPmManager.getSimpleProgramListFromTotalChannelList(type, includeSkipFlag, includePVRSkipFlag);
    }

    @Override
    public SimpleChannel getSimpleProgramByChannelId(long channelId) {
        return mPmManager.getSimpleProgramByChannelId(channelId);
    }

    @Override
    public SimpleChannel getSimpleProgramByChannelIdfromTotalChannelList(long channelId) {
        return mPmManager.getSimpleProgramByChannelIdFromTotalChannelList(channelId);
    }

    @Override
    public SimpleChannel getSimpleProgramByChannelIdfromTotalChannelListByGroup(int groupType, long channelId) {
        return mPmManager.getSimpleProgramByChannelIdFromTotalChannelListByGroup(groupType, channelId);
    }

    @Override
    public List<SimpleChannel> getChannelListByFilter(int filterTag, int serviceType, String keyword, int includeSkip, int includePvrSkip) {
        return mPmManager.getChannelListByFilter(filterTag, serviceType, keyword, includeSkip, includePvrSkip);
    }

    /*
    fav
    */
    @Override
    public List<FavInfo> favInfoGetList(int favMode) {
        return mPmManager.favInfoGetList(favMode);
    }

    @Override
    public FavInfo favInfoGet(int favMode, int index) {
        return mPmManager.favInfoGet(favMode, index);
    }

    @Override
    public int favInfoUpdateList(int favMode, List<FavInfo> favInfoList) {
        return mPmManager.favInfoUpdateList(favMode, favInfoList);
    }

    @Override
    public int favInfoDelete(int favMode, long channelId) {
        return mPmManager.favInfoDelete(favMode, channelId);
    }

    @Override
    public int favInfoDeleteAll(int favMode) {
        return mPmManager.favInfoDeleteAll(favMode);
    }

    @Override
    public int favInfoSaveDb(FavInfo favInfo) {
        return mPmManager.favInfoSaveDb(favInfo);
    }

    @Override
    public String favGroupNameGet(int favMode) {
        return mPmManager.favGroupNameGet(favMode);
    }

    @Override
    public int favGroupNameUpdate(int favMode, String name) {
        return mPmManager.favGroupNameUpdate(favMode, name);
    }

    /*
    Timer
     */
    @Override
    public List<BookInfo> BookInfoGetList() {
        return mBookCmdManager.BookInfoGetList();
    }

    @Override
    public BookInfo BookInfoGet(int bookId) {
        return mBookCmdManager.BookInfoGet(bookId);
    }

    @Override
    public int BookInfoAdd(BookInfo bookInfo) {
        return mBookCmdManager.BookInfoAdd(bookInfo);
    }

    @Override
    public int BookInfoUpdate(BookInfo bookInfo) {
        return mBookCmdManager.BookInfoUpdate(bookInfo);
    }

    @Override
    public int BookInfoUpdateList(List<BookInfo> bookList) {
        return mBookCmdManager.BookInfoUpdateList(bookList);
    }

    @Override
    public int BookInfoDelete(int bookId) {
        return mBookCmdManager.BookInfoDelete(bookId);
    }

    @Override
    public int BookInfoDeleteAll() {
        return mBookCmdManager.BookInfoDeleteAll();
    }

    @Override
    public BookInfo BookInfoGetComingBook() {
        return mBookCmdManager.BookInfoGetComingBook();
    }

    @Override
    public List<BookInfo> BookInfoFindConflictBooks(BookInfo bookInfo) {
        return mBookCmdManager.BookInfoFindConflictBooks(bookInfo);
    }

    @Override
    public void BookInfoSave(BookInfo bookInfo) {
        mBookCmdManager.BookInfoSave(bookInfo);
    }

    /*
    EPG
     */
    @Override
    public EPGEvent getPresentEvent(long channelId) {
        return mEpgCmdManager.getPresentEvent(channelId);
    }

    @Override
    public EPGEvent getFollowEvent(long channelId) {
        return mEpgCmdManager.getFollowEvent(channelId);
    }

    @Override
    public EPGEvent getEpgByEventID(long channelId, int eventId) {
        return mEpgCmdManager.getEpgByEventID(channelId, eventId);
    }

    @Override
    public List<EPGEvent> getEPGEvents(long channelId, Date startDate, Date endDate, int offset, int onetimeObtainedNumber, int addEmpty) {
        return mEpgCmdManager.getEPGEvents(channelId, startDate, endDate, offset, onetimeObtainedNumber, addEmpty);
    }

    @Override
    public String getShortDescription(long channelId, int eventId) {
        return mEpgCmdManager.getShortDescription(channelId, eventId);
    }

    @Override
    public String getDetailDescription(long channelId, int eventId) {
        return mEpgCmdManager.getDetailDescription(channelId, eventId);
    }

    @Override
    public int setEvtLang(String firstLangCode, String secLangCode) {
        return mEpgCmdManager.setEvtLang(firstLangCode, secLangCode);
    }

    @Override
    public Date getDtvDate() {
        return mEpgCmdManager.getDtvDate();
    }

    @Override
    public void startScheduleEit() {
        mEpgCmdManager.startScheduleEit();
    }

    @Override
    public void stopScheduleEit() {
        mEpgCmdManager.stopScheduleEit();
    }

    /*
    time zone
     */
    @Override
    public int setTime(Date date) {
        return mTimeControlCmdManager.setTime(date);
    }

    @Override
    public int getDtvTimeZone() {
        return mTimeControlCmdManager.getDtvTimeZone();
    }

    @Override
    public int setDtvTimeZone(int zonesecond) {
        return mTimeControlCmdManager.setDtvTimeZone(zonesecond);
    }

    @Override
    public int getSettingTDTStatus() {
        return mTimeControlCmdManager.getSettingTDTStatus();
    }

    @Override
    public int setSettingTDTStatus(int onoff) {
        return mTimeControlCmdManager.setSettingTDTStatus(onoff);
    }

    @Override
    public int setTimeToSystem(boolean bSetTimeToSystem) {
        return mTimeControlCmdManager.setTimeToSystem(bSetTimeToSystem);
    }

    @Override
    public int syncTime(boolean bEnable) {
        return mTimeControlCmdManager.syncTime(bEnable);
    }

    @Override
    public int getDtvDaylight() {
        return mTimeControlCmdManager.getDtvDaylight();
    }

    @Override
    public int setDtvDaylight(int onoff) {
        return mTimeControlCmdManager.setDtvDaylight(onoff);
    }

    /*
    UI
     */
    @Override
    public void pipModSetDisplay(Context context, Surface surface, int type) {
        /*not use*/
    }

    @Override
    public void pipModClearDisplay(Surface surface) {
        /*not use*/
    }

    private void initSurfaceView(Context context,SurfaceView surfaceView )//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    {
        Log.d(TAG, "SUB:=================initSurfaceView====================");
        //mSubTeletextSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        SetSurfaceContext = context;//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
        mSubTeletextSurfaceView = surfaceView ;
        mSubTeletextSurfaceView.setVisibility(View.VISIBLE);
        mSubSurfaceHolder = mSubTeletextSurfaceView.getHolder();

//        mSubSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
//        mSubSurfaceHolder.setType(100);

        Log.d(TAG, "SUB:=================mSubSurfaceHolder = " + mSubSurfaceHolder);

        mSubSurfaceHolder.addCallback(new SurfaceHolder.Callback()
        {
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
            {
                LogUtils.d("SUB:=================surfaceChanged====================");
                LogUtils.d("Surface = "+holder.getSurface());
                mAvCmdManager.setSurface(holder.getSurface());
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
                mAvCmdManager.setSurface(holder.getSurface());
                LogUtils.d("Surface = "+holder.getSurface());
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder)
            {
                Log.d(TAG, "SUB:=================surfaceDestroyed====================");
//                pipModClearDisplay(holder.getSurface());
                pipModSetDisplay(SetSurfaceContext,null,0);// jim 2020/12/16 add set surface to RTK_SerVideoSurfaceEx -s
                mAvCmdManager.setSurface(null);
            }
        });
    }

    @Override
    public void setSurfaceView(Context context, SurfaceView surface )//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    {
//        initSurfaceView(context,surface) ;//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
        LogUtils.d("setSurfaceView");
        mAvCmdManager.setSurfaceHolder(surface.getHolder());
    }
    @Override
    public void setSurfaceView(Context context, SurfaceView surface,int index)//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    {
//        initSurfaceView(context,surface) ;//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
        LogUtils.d("setSurfaceView "+index);
        mAvCmdManager.setSurfaceHolder(surface.getHolder(), index);
    }
    public void setSurfaceToPlayer(int index,Surface surface) {
        mAvCmdManager.setSurfaceToPlayer(index,surface);
    }


    /*
    scan
     */
    @Override
    public void startScan(TVScanParams sp) {
        if(isTunerInited == false)
            init_tuner();
        mScanCmdManager.startScan(sp);
    }

    public void startMonitorTable(long channelId, int isFcc, int tuner_id){
        mScanCmdManager.startMonitorTable(channelId, isFcc, tuner_id);
    }

    @Override
    public void UpdatePMT(long channel_id) {
		mScanCmdManager.UpdatePMT(channel_id);

    }

    public void stopMonitorTable(long channelId, int tuner_id){
        mScanCmdManager.stopMonitorTable(channelId, tuner_id);
    }

    public void StopDVBSubtitle(){
        mAvCmdManager.AvControlStopDVBSubtitle();

    }

    @Override
    public void VMXstartScan(TVScanParams sp, int startTPID, int endTPID) {
        mScanCmdManager.VMXstartScan(sp, startTPID, endTPID);
    }

    @Override
    public void stopScan(boolean store) {
        mScanCmdManager.stopScan(store);
    }

    /*
     tuner hw
      */
    @Override
    public int tunerLock(TVTunerParams tvTunerParams) {
        return mFrontendCmdManager.tunerLock(tvTunerParams);
    }

    @Override
    public int getTunerNum() {
        return mFrontendCmdManager.getTunerNum();
    }

    @Override
    public int getTunerType() {
        return mFrontendCmdManager.getTunerType();
    }

    @Override
    public int getSignalStrength(int nTunerID) {
        return mFrontendCmdManager.getSignalStrength(nTunerID);
    }

    @Override
    public int getSignalQuality(int nTunerID) {
        return mFrontendCmdManager.getSignalQuality(nTunerID);
    }

    @Override
    public int getSignalSNR(int nTunerID) {
        return mFrontendCmdManager.getSignalSNR(nTunerID);
    }

    @Override
    public int getSignalBER(int nTunerID) {
        return mFrontendCmdManager.getSignalBER(nTunerID);
    }

    @Override
    public boolean getTunerStatus(int tuner_id) {
        return mFrontendCmdManager.getTunerStatus(tuner_id);
    }

    @Override
    public int setFakeTuner(int openFlag) {
        return mFrontendCmdManager.setFakeTuner(openFlag);
    }

    @Override
    public int tunerSetAntenna5V(int tuner_id, int onOff) {
        return mFrontendCmdManager.tunerSetAntenna5V(tuner_id, onOff);
    }

    @Override
    public int setDiSEqC10PortInfo(int nTuerID, int nPort, int n22KSwitch, int nPolarity) {
        return mFrontendCmdManager.setDiSEqC10PortInfo(nTuerID, nPort, n22KSwitch, nPolarity);
    }

    @Override
    public int setDiSEqC12MoveMotor(int nTunerId, int direct, int step) {
        return mFrontendCmdManager.setDiSEqC12MoveMotor(nTunerId, direct, step);
    }

    @Override
    public int setDiSEqC12MoveMotorStop(int nTunerId) {
        return mFrontendCmdManager.setDiSEqC12MoveMotorStop(nTunerId);
    }

    @Override
    public int resetDiSEqC12Position(int nTunerId) {
        return mFrontendCmdManager.resetDiSEqC12Position(nTunerId);
    }

    @Override
    public int setDiSEqCLimitPos(int nTunerId, int limitType) {
        return mFrontendCmdManager.setDiSEqCLimitPos(nTunerId, limitType);
    }

    /*
    tp info
     */
    @Override
    public List<SatInfo> satInfoGetList(int tunerType, int pos, int num) {
        return mPmManager.satInfoGetList(tunerType,pos,num);
    }

    @Override
    public SatInfo satInfoGet(int satId) {
        return mPmManager.satInfoGet(satId);
    }

    @Override
    public int satInfoAdd(SatInfo pSat) {
        return mPmManager.satInfoAdd(pSat);
    }

    @Override
    public int satInfoUpdate(SatInfo pSat) {
        return mPmManager.satInfoUpdate(pSat);
    }

    @Override
    public int satInfoUpdateList(List<SatInfo> pSats) {
        return mPmManager.satInfoUpdateList(pSats);
    }

    @Override
    public int satInfoDelete(int satId) {
        return mPmManager.satInfoDelete(satId);
    }

    @Override
    public List<TpInfo> tpInfoGetListBySatId(int tunerType, int satId, int pos, int num) {
        return mPmManager.tpInfoGetListBySatId(tunerType,satId,pos,num);
    }

    @Override
    public TpInfo tpInfoGet(int tp_id) {
        return mPmManager.tpInfoGet(tp_id);
    }

    @Override
    public int tpInfoAdd(TpInfo pTp) {
        return mPmManager.tpInfoAdd(pTp);
    }

    @Override
    public int tpInfoUpdate(TpInfo pTp) {
        return mPmManager.tpInfoUpdate(pTp);
    }

    @Override
    public int tpInfoUpdateList(List<TpInfo> pTps) {
        return mPmManager.tpInfoUpdateList(pTps);
    }

    @Override
    public int tpInfoDelete(int tpId) {
        return mPmManager.tpInfoDelete(tpId);
    }

    /*
        AvControl
         */
    @Override
    public int AvControlPlayByChannelIdFCC(int mode, List<Long> ch_id_list, List<Integer> tuner_id_list, boolean channelBlocked) {
        if(isTunerInited == false) {
            init_tuner();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return mAvCmdManager.AvControlPlayByChannelIdFCC(mode, ch_id_list, tuner_id_list , channelBlocked);
    }
    @Override
    public int AvControlPlayByChannelId(int playId, long channelId, int groupType, int show) {
        if(isTunerInited == false) {
            init_tuner();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return mAvCmdManager.AvControlPlayByChannelId(playId, channelId, groupType, show);
    }

    @Override
    public int AvControlPrePlayStop() {
        return mAvCmdManager.AvControlPrePlayStop();
    }



    public int AvControlPlayStop(int tunerId, int mode, int stop_monitor_table) {
        return mAvCmdManager.AvControlPlayStop(tunerId, mode,stop_monitor_table);
    }
    public int AvControlPlayStopAll(){
        return mAvCmdManager.AvControlPlayStopAll();
    }

    @Override
    public int AvControlChangeRatioConversion(int playId, int ratio, int conversion) {
        return mAvCmdManager.AvControlChangeRatioConversion(playId, ratio, conversion);
    }

    @Override
    public int AvControlSetFastChangeChannel(int tunerId, long chId) {
        return mAvCmdManager.AvControlSetFastChangeChannel(tunerId, chId);
    }

    @Override
    public int AvControlClearFastChangeChannel(int tunerId, long chId) {
        return mAvCmdManager.AvControlClearFastChangeChannel(tunerId, chId);
    }

    @Override
    public int AvControlChangeResolution(int playId, int resolution) {
        return mAvCmdManager.AvControlChangeResolution(playId, resolution);
    }

    @Override
    public int AvControlChangeAudio(int playId, AudioInfo.AudioComponent component) {
        return mAvCmdManager.AvControlChangeAudio(playId, component);
    }

    @Override
    public int AvControlSetVolume(int volume) {
        return mAvCmdManager.AvControlSetVolume(volume);
    }

    @Override
    public int AvControlGetVolume() {
        return mAvCmdManager.AvControlGetVolume();
    }

    @Override
    public int AvControlSetMute(int playId, boolean mute) {
        return mAvCmdManager.AvControlSetMute(playId, mute);
    }

    @Override
    public int AvControlSetTrackMode(int playId, EnAudioTrackMode stereo) {
        return mAvCmdManager.AvControlSetTrackMode(playId, stereo);
    }

    @Override
    public int AvControlAudioOutput(int playId, int byPass) {
        return mAvCmdManager.AvControlAudioOutput(playId, byPass);
    }

    @Override
    public int AvControlClose(int playId) {
        return mAvCmdManager.AvControlClose(playId);
    }

    @Override
    public int AvControlOpen(int playId) {
        return mAvCmdManager.AvControlOpen(playId);
    }

    @Override
    public int AvControlShowVideo(int playId, boolean show) {
        return mAvCmdManager.AvControlShowVideo(playId, show);
    }

    @Override
    public int AvControlFreezeVideo(int playId, boolean freeze) {
        return mAvCmdManager.AvControlFreezeVideo(playId, freeze);
    }

    @Override
    public AudioInfo AvControlGetAudioListInfo(int playId) {
        return mAvCmdManager.AvControlGetAudioListInfo(playId);
    }

    @Override
    public int AvControlGetPlayStatus(int playId) {
        return mAvCmdManager.AvControlGetPlayStatus(playId);
    }

    @Override
    public int AvControlSetPlayStatus(int status) {
        return mAvCmdManager.AvControlSetPlayStatus(status);
    }

    @Override
    public boolean AvControlGetMute(int playId) {
        return mAvCmdManager.AvControlGetMute(playId);
    }

    @Override
    public EnAudioTrackMode AvControlGetTrackMode(int playId) {
        return mAvCmdManager.AvControlGetTrackMode(playId);
    }

    @Override
    public int AvControlGetRatio(int playId) {
        return mAvCmdManager.AvControlGetRatio(playId);
    }

    @Override
    public int AvControlSetStopScreen(int playId, int stopType) {
        return mAvCmdManager.AvControlSetStopScreen(playId, stopType);
    }

    @Override
    public int AvControlGetStopScreen(int playId) {
        return mAvCmdManager.AvControlGetStopScreen(playId);
    }

    @Override
    public int AvControlGetFPS(int playId) {
        return mAvCmdManager.AvControlGetFPS(playId);
    }

    @Override
    public int AvControlEwsActionControl(int playId, boolean enable) {
        return mAvCmdManager.AvControlEwsActionControl(playId, enable);
    }

    @Override
    public int AvControlSetWindowSize(int playId, Rect rect) {
        return mAvCmdManager.AvControlSetWindowSize(playId, rect);
    }

    @Override
    public Rect AvControlGetWindowSize(int playId) {
        return mAvCmdManager.AvControlGetWindowSize(playId);
    }

    @Override
    public int AvControlGetVideoResolutionHeight(int playId) {
        return mAvCmdManager.AvControlGetVideoResolutionHeight(playId);
    }

    @Override
    public int AvControlGetVideoResolutionWidth(int playId) {
        return mAvCmdManager.AvControlGetVideoResolutionWidth(playId);
    }

    @Override
    public int AvControlGetDolbyInfoStreamType(int playId) {
        return mAvCmdManager.AvControlGetDolbyInfoStreamType(playId);
    }

    @Override
    public int AvControlGetDolbyInfoAcmod(int playId) {
        return mAvCmdManager.AvControlGetDolbyInfoAcmod(playId);
    }

    @Override
    public SubtitleInfo.SubtitleComponent AvControlGetCurrentSubtitle(int playId) {
        return mAvCmdManager.AvControlGetCurrentSubtitle(playId);
    }

    @Override
    public SubtitleInfo AvControlGetSubtitleList(int playId) {
        return mAvCmdManager.AvControlGetSubtitleList(playId);
    }

    @Override
    public int AvControlSelectSubtitle(int playId, SubtitleInfo.SubtitleComponent subtitleComponent) {
        return mAvCmdManager.AvControlSelectSubtitle(playId, subtitleComponent);
    }

    @Override
    public int AvControlShowSubtitle(int playId, boolean enable) {
        return mAvCmdManager.AvControlShowSubtitle(playId, enable);
    }

    @Override
    public boolean AvControlIsSubtitleVisible(int playId) {
        return mAvCmdManager.AvControlIsSubtitleVisible(playId);
    }

    @Override
    public int AvControlSetSubtHiStatus(int playId, boolean hiOn) {
        return mAvCmdManager.AvControlSetSubtHiStatus(playId, hiOn);
    }

    @Override
    public int AvControlSetSubtitleLanguage(int playId, int index, String lang) {
        return mAvCmdManager.AvControlSetSubtitleLanguage(playId, index, lang);
    }

    @Override
    public TeletextInfo AvControlGetCurrentTeletext(int playId) {
        return mAvCmdManager.AvControlGetCurrentTeletext(playId);
    }

    @Override
    public List<TeletextInfo> AvControlGetTeletextList(int playId) {
        return mAvCmdManager.AvControlGetTeletextList(playId);
    }

    @Override
    public int AvControlShowTeletext(int playId, boolean enable) {
        return mAvCmdManager.AvControlShowTeletext(playId, enable);
    }

    @Override
    public boolean AvControlIsTeletextVisible(int playId) {
        return mAvCmdManager.AvControlIsTeletextVisible(playId);
    }

    @Override
    public boolean AvControlIsTeletextAvailable(int playId) {
        return mAvCmdManager.AvControlIsTeletextAvailable(playId);
    }

    @Override
    public int AvControlSetTeletextLanguage(int playId, String primeLang) {
        return mAvCmdManager.AvControlSetTeletextLanguage(playId, primeLang);
    }

    @Override
    public String AvControlGetTeletextLanguage(int playId) {
        return mAvCmdManager.AvControlGetTeletextLanguage(playId);
    }

    @Override
    public int AvControlSetCommand(int playId, int keyCode) {
        return mAvCmdManager.AvControlSetCommand(playId, keyCode);
    }

    @Override
    public Date AvControlGetTimeShiftBeginTime(int playId) {
        return mAvCmdManager.AvControlGetTimeShiftBeginTime(playId);
    }

    @Override
    public Date AvControlGetTimeShiftPlayTime(int playId) {
        return mAvCmdManager.AvControlGetTimeShiftPlayTime(playId);
    }

    @Override
    public int AvControlGetTimeShiftRecordTime(int playId) {
        return mAvCmdManager.AvControlGetTimeShiftRecordTime(playId);
    }

    @Override
    public int AvControlGetTrickMode(int playId) {
        return mAvCmdManager.AvControlGetTrickMode(playId);
    }

    @Override
    public int AvControlTimeshiftTrickPlay(int playId, int trickMode) {
        return mAvCmdManager.AvControlTimeshiftTrickPlay(playId, trickMode);
    }

    @Override
    public int AvControlTimeshiftPausePlay(int playId) {
        return mAvCmdManager.AvControlTimeshiftPausePlay(playId);
    }

    @Override
    public int AvControlTimeshiftPlay(int playId) {
        return mAvCmdManager.AvControlTimeshiftPlay(playId);
    }

    @Override
    public int AvControlTimeshiftSeekPlay(int playId, long seekTime) {
        return mAvCmdManager.AvControlTimeshiftSeekPlay(playId, seekTime);
    }

    @Override
    public int AvControlStopTimeShift(int playId) {
        return mAvCmdManager.AvControlStopTimeShift(playId);
    }

    /*
    PVR
     */
    //== Allen Start - Series Record
    public int PvrInit(String usbMountPath)  { return mPvrCmdManager.pvrInit(usbMountPath); }
    public int PvrDeinit() { return mPvrCmdManager.pvrDeinit(); }
    public int PvrRecordStart(PvrRecStartParam startParam,int tunerId) { return mPvrCmdManager.pvrRecordStart(startParam, tunerId); }
    public int PvrRecordStop(int recId) { return mPvrCmdManager.pvrRecordStop(recId); }
    public void PvrRecordStopAll() { mPvrCmdManager.pvrRecordStopAll(); }
    public int PvrRecordGetRecTimeByRecId(int recId) { return  mPvrCmdManager.pvrRecordGetRecTimeByRecId(recId); }
    public boolean PvrPlayCheckLastPositionPoint(PvrRecIdx recIdx) { return mPvrCmdManager.pvrPlayCheckLastPosiitonPoint(recIdx); }
    public int PvrPlayFileStart(PvrRecIdx recIdx, boolean lastPositionFlag, int tunerId){ return mPvrCmdManager.pvrPlayFileStart(recIdx, lastPositionFlag, tunerId);}
    public int PvrPlayFileStop(int playId){
        return mPvrCmdManager.pvrPlayFileStop(playId);
    }
    public int PvrPlayPlay(int playId){ return mPvrCmdManager.pvrPlayPlay(playId); }
    public int PvrPlayPause(int playId){ return mPvrCmdManager.pvrPlayPause(playId); }
    public int PvrPlayFastForward(int playId){ return mPvrCmdManager.pvrPlayFastForward(playId); }
    public int PvrPlayRewind(int playId){ return mPvrCmdManager.pvrPlayRewind(playId); }
    public int PvrPlaySeek(int playId, PvrInfo.EnSeekMode seekMode, int offsetSec){ return mPvrCmdManager.pvrPlaySeek(playId,seekMode,offsetSec); }
    public int PvrPlaySetSpeed(int playId, PvrInfo.EnPlaySpeed playSpeed){ return mPvrCmdManager.pvrPlaySetSpeed(playId, playSpeed); }
    public PvrInfo.EnPlaySpeed PvrPlayGetSpeed(int playId){ return mPvrCmdManager.pvrPlayGetSpeed(playId); }
    public int PvrPlayChangeAudioTrack(int playId, AudioInfo.AudioComponent audioComponent){ return mPvrCmdManager.pvrPlayChangeAudioTrack(playId, audioComponent); }
    public int PvrPlayGetCurrentAudioIndex(int playId){ return mPvrCmdManager.pvrPlayGetCurrentAudioIndex(playId); }
    public int PvrPlayGetPlayTime(int playId){
        return mPvrCmdManager.pvrPlayGetPlayTime(playId);
    }
    public PvrInfo.EnPlayStatus PvrPlayGetPlayStatus(int playId){ return mPvrCmdManager.pvrPlayGetPlayStatus(playId); }
    public PvrInfo.PlayTimeInfo PvrPlayGetPlayTimeInfo(int playId){ return mPvrCmdManager.pvrPlayGetPlayTimeInfo(playId); }
    public int PvrTimeShiftStart(ProgramInfo programInfo,int recordTunerId,int playTunerId){
        mAvCmdManager.closeVideoCountCheck();
        return mPvrCmdManager.pvrTimeShiftStart(programInfo, recordTunerId,playTunerId);
    }
    public int PvrTimeShiftStop(){ return mPvrCmdManager.pvrTimeShiftStop(); }
    public int PvrTimeShiftPlayStart(int tunerId){  return mPvrCmdManager.pvrTimeShiftPlayStart(tunerId); }
    public int PvrTimeShiftPlayPause(int tunerId){  return mPvrCmdManager.pvrTimeShiftPlayPause(tunerId); }
    public int PvrTimeShiftPlayResume(int tunerId){ return mPvrCmdManager.pvrTimeShiftPlayResume(tunerId); }
    public int PvrTimeShiftPlayFastForward(int playId){ return mPvrCmdManager.pvrTimeShiftPlayFastForward(playId);  }
    public int PvrTimeShiftPlayRewind(int playId){ return mPvrCmdManager.pvrTimeShiftPlayRewind(playId); }
    public int PvrTimeShiftPlaySeek(int playId, PvrInfo.EnSeekMode seekMode, int startPosition){ return  mPvrCmdManager.pvrTimeShiftPlaySeek(playId,seekMode,startPosition); }
    public int PvrTimeShiftPlaySetSpeed(int playId, PvrInfo.EnPlaySpeed playSpeed){ return mPvrCmdManager.pvrTimeShiftPlaySetSpeed(playId,playSpeed); }
    public PvrInfo.EnPlaySpeed PvrTimeShiftPlayGetSpeed(int playId){  return mPvrCmdManager.pvrTimeShiftPlayGetSpeed(playId);  }
    public int PvrTimeShiftPlayChangeAudioTrack(int playId, AudioInfo.AudioComponent audioComponent){ return mPvrCmdManager.pvrTimeShiftPlayChangeAudioTrack(playId,audioComponent); }
    public int PvrTimeShiftPlayGetCurrentAudioIndex(int playId){ return mPvrCmdManager.pvrTimeShiftPlayGetCurrentAudioIndex(playId); }
    public PvrInfo.EnPlayStatus PvrTimeShiftPlayGetStatus(int playId){ return mPvrCmdManager.pvrTimeShiftPlayGetStatus(playId); }
    public PvrInfo.PlayTimeInfo PvrTimeShiftGetTimeInfo(int playId){ return mPvrCmdManager.pvrTimeShiftGetTimeInfo(playId); }
    public PvrRecFileInfo PvrGetFileInfoByIndex(PvrRecIdx tableKeyInfo){ return mPvrCmdManager.pvrGetFileInfoByIndex(tableKeyInfo); }//應該不用
    public int PvrGetRecCount(){ return mPvrCmdManager.pvrGetRecCount(); }
    public int PvrGetSeriesRecCount(int masterIndex){ return mPvrCmdManager.pvrGetSeriesRecCount(masterIndex); }
    public List<PvrRecFileInfo> PvrGetRecLink(int startIndex, int count){ return mPvrCmdManager.pvrGetRecLink(startIndex,count); }
    public List<PvrRecFileInfo> PvrGetPlaybackLink(int startIndex, int count){ return mPvrCmdManager.pvrGetPlaybackLink(startIndex, count); }
    public List<PvrRecFileInfo> PvrGetSeriesRecLink(PvrRecIdx tableKeyInfo, int count){ return mPvrCmdManager.pvrGetSeriesRecLink(tableKeyInfo, count); }
    public int PvrDelAllRecs(){ return mPvrCmdManager.pvrDelAllRecs(); }
    public int PvrDelSeriesRecs(int masterIndex){ return mPvrCmdManager.pvrDelSeriesRecs(masterIndex); }
    public int PvrDelRecsByChId(int channelId){ return mPvrCmdManager.pvrDelRecsByChId(channelId); }
    public int PvrDelOneRec(PvrRecIdx tableKeyInfo){ return mPvrCmdManager.pvrDelOneRec(tableKeyInfo); }
    public int PvrDelOnePlayback(PvrRecIdx tableKeyInfo){ return mPvrCmdManager.pvrDelOnePlayback(tableKeyInfo); }
    public int PvrCheckSeriesEpisode(byte[] seriesKey, int episode){ return mPvrCmdManager.pvrCheckSeriesEpisode(seriesKey, episode); }
    public int PvrCheckSeriesComplete(byte[]  seriesKey){ return mPvrCmdManager.pvrCheckSeriesComplete(seriesKey); }
    public boolean PvrIsIdxInUse(PvrRecIdx pvrRecIdx) {
        return mPvrCmdManager.pvrIsIdxInUse(pvrRecIdx);
    }
    //allen_add api--end
    //== Allen End - Series Record
    /*
    @Override
    public void UpdatePvrSkipList(int groupType, int includePVRSkipFlag, int tpId, List<Integer> pvrTpList) {
        mPvrCmdManager.UpdatePvrSkipList(groupType, includePVRSkipFlag, tpId, pvrTpList);
    }

    @Override
    public int Record_Start_V2_with_Duration(long channelId, int durationSec, boolean doCipher, PVREncryption pvrEncryption) {
        return mPvrCmdManager.Record_Start_V2_with_Duration(channelId, durationSec, doCipher, pvrEncryption);
    }

    @Override
    public int Record_Start_V2_with_FileSize(long channelId, int fileSizeMB, boolean doCipher, PVREncryption pvrEncryption) {
        return mPvrCmdManager.Record_Start_V2_with_FileSize(channelId, fileSizeMB, doCipher, pvrEncryption);
    }

    @Override
    public int TimeShift_Start_V2(int durationSec, int fileSizeMB, boolean doCipher, PVREncryption pvrEncryption) {
        return mPvrCmdManager.TimeShift_Start_V2(durationSec, fileSizeMB, doCipher, pvrEncryption);
    }

    @Override
    public List<PvrFileInfo> Pvr_Get_Records_File(int startIndex, int total) {
        return mPvrCmdManager.Pvr_Get_Records_File(startIndex, total);
    }

    @Override
    public int Pvr_Get_Total_Rec_Num() {
        return mPvrCmdManager.Pvr_Get_Total_Rec_Num();
    }

    @Override
    public int Pvr_Get_Total_One_Series_Rec_Num(String recordUniqueId) {
        return mPvrCmdManager.Pvr_Get_Total_One_Series_Rec_Num(recordUniqueId);
    }

    @Override
    public List<PvrFileInfo> Pvr_Get_Total_One_Series_Records_File(int startIndex, int total, String recordUniqueId) {
        return mPvrCmdManager.Pvr_Get_Total_One_Series_Records_File(startIndex, total, recordUniqueId);
    }

    @Override
    public int Pvr_Delete_Total_Records_File() {
        return mPvrCmdManager.Pvr_Delete_Total_Records_File();
    }

    @Override
    public int Pvr_Delete_One_Series_Folder(String recordUniqueId) {
        return mPvrCmdManager.Pvr_Delete_One_Series_Folder(recordUniqueId);
    }

    @Override
    public int Pvr_Delete_Record_File_By_Ch_Id(int channelId) {
        return mPvrCmdManager.Pvr_Delete_Record_File_By_Ch_Id(channelId);
    }

    @Override
    public int Pvr_Delete_Record_File(String recordUniqueId) {
        return mPvrCmdManager.Pvr_Delete_Record_File(recordUniqueId);
    }

    @Override
    public int pvrRecordStart(PvrRecStartParam startParam, int tunerId) {
        return mPvrCmdManager.pvrRecordStart(startParam, tunerId);
    }

    @Override
    public int pvrRecordStop(int pvrPlayerID, int recId) {
        return mPvrCmdManager.pvrRecordStop(pvrPlayerID, recId);
    }

    @Override
    public int pvrRecordGetAlreadyRecTime(int pvrPlayerID, int recId) {
        return mPvrCmdManager.pvrRecordGetAlreadyRecTime(pvrPlayerID, recId);
    }

    @Override
    public int pvrRecordGetStatus(int pvrPlayerID, int recId) {
        return mPvrCmdManager.pvrRecordGetStatus(pvrPlayerID, recId);
    }

    @Override
    public String pvrRecordGetFileFullPath(int pvrPlayerID, int recId) {
        return mPvrCmdManager.pvrRecordGetFileFullPath(pvrPlayerID, recId);
    }

    @Override
    public int pvrRecordGetProgramId(int pvrPlayerID, int recId) {
        return mPvrCmdManager.pvrRecordGetProgramId(pvrPlayerID, recId);
    }

    @Override
    public int pvrRecordCheck(long channelID) {
        return mPvrCmdManager.pvrRecordCheck(channelID);
    }

    @Override
    public List<PvrInfo> pvrRecordGetAllInfo() {
        return mPvrCmdManager.pvrRecordGetAllInfo();
    }

    @Override
    public int pvrRecordGetMaxRecNum() {
        return mPvrCmdManager.pvrRecordGetMaxRecNum();
    }

    @Override
    public int pvrPlayFileCheckLastViewPoint(String fullName) {
        return mPvrCmdManager.pvrPlayFileCheckLastViewPoint(fullName);
    }

    @Override
    public int pvrSetStartPositionFlag(int startPositionFlag) {
        return mPvrCmdManager.pvrSetStartPositionFlag(startPositionFlag);
    }

    @Override
    public int pvrTimeShiftStart(int playerID, int time, int filesize, String filePath) {
        return mPvrCmdManager.pvrTimeShiftStart(playerID, time, filesize, filePath);
    }

    @Override
    public int pvrTimeShiftStop(int playerID) {
        return mPvrCmdManager.pvrTimeShiftStop(playerID);
    }

    @Override
    public int pvrTimeShiftPlay(int playerID) {
        return mPvrCmdManager.pvrTimeShiftPlay(playerID);
    }

    @Override
    public int pvrTimeShiftResume(int playerID) {
        return mPvrCmdManager.pvrTimeShiftResume(playerID);
    }

    @Override
    public int pvrTimeShiftPause(int playerID) {
        return mPvrCmdManager.pvrTimeShiftPause(playerID);
    }

    @Override
    public int pvrTimeShiftLivePause(int playerID) {
        return mPvrCmdManager.pvrTimeShiftLivePause(playerID);
    }

    @Override
    public int pvrTimeShiftFilePause(int playerID) {
        return mPvrCmdManager.pvrTimeShiftFilePause(playerID);
    }

    @Override
    public int pvrTimeShiftTrickPlay(int playerID, EnTrickMode mode) {
        return mPvrCmdManager.pvrTimeShiftTrickPlay(playerID, mode);
    }

    @Override
    public int pvrTimeShiftSeekPlay(int playerID, int seekSec) {
        return mPvrCmdManager.pvrTimeShiftSeekPlay(playerID, seekSec);
    }

    @Override
    public Date pvrTimeShiftGetPlayedTime(int playerID) {
        return mPvrCmdManager.pvrTimeShiftGetPlayedTime(playerID);
    }

    @Override
    public int pvrTimeShiftGetPlaySecond(int playerID) {
        return mPvrCmdManager.pvrTimeShiftGetPlaySecond(playerID);
    }

    @Override
    public Date pvrTimeShiftGetBeginTime(int playerID) {
        return mPvrCmdManager.pvrTimeShiftGetBeginTime(playerID);
    }

    @Override
    public int pvrTimeShiftGetBeginSecond(int playerID) {
        return mPvrCmdManager.pvrTimeShiftGetBeginSecond(playerID);
    }

    @Override
    public int pvrTimeShiftGetRecordTime(int playerID) {
        return mPvrCmdManager.pvrTimeShiftGetRecordTime(playerID);
    }

    @Override
    public int pvrTimeShiftGetStatus(int playerID) {
        return mPvrCmdManager.pvrTimeShiftGetStatus(playerID);
    }

    @Override
    public EnTrickMode pvrTimeShiftGetCurrentTrickMode(int playerID) {
        return mPvrCmdManager.pvrTimeShiftGetCurrentTrickMode(playerID);
    }

    @Override
    public int pvrPlayStart(String filePath) {
        return mPvrCmdManager.pvrPlayStart(filePath);
    }

    @Override
    public int pvrPlayStart(String filePath, PVREncryption pvrEncryption) {
        return mPvrCmdManager.pvrPlayStart(filePath, pvrEncryption);
    }

    @Override
    public int pvrPlayStop() {
        return mPvrCmdManager.pvrPlayStop();
    }

    @Override
    public int pvrPlayPause() {
        return mPvrCmdManager.pvrPlayPause();
    }

    @Override
    public int pvrPlayResume() {
        return mPvrCmdManager.pvrPlayResume();
    }

    @Override
    public int pvrPlayTrickPlay(EnTrickMode enSpeed) {
        return mPvrCmdManager.pvrPlayTrickPlay(enSpeed);
    }

    @Override
    public int pvrPlaySeekTo(int sec) {
        return mPvrCmdManager.pvrPlaySeekTo(sec);
    }

    @Override
    public int pvrPlayGetPlayTime() {
        return mPvrCmdManager.pvrPlayGetPlayTime();
    }

    @Override
    public EnTrickMode pvrPlayGetCurrentTrickMode() {
        return mPvrCmdManager.pvrPlayGetCurrentTrickMode();
    }

    @Override
    public int pvrPlayGetCurrentStatus() {
        return mPvrCmdManager.pvrPlayGetCurrentStatus();
    }

    @Override
    public long pvrPlayGetSize() {
        return mPvrCmdManager.pvrPlayGetSize();
    }

    @Override
    public int pvrPlayGetDuration() {
        return mPvrCmdManager.pvrPlayGetDuration();
    }

    @Override
    public Resolution pvrPlayGetVideoResolution() {
        return mPvrCmdManager.pvrPlayGetVideoResolution();
    }

    @Override
    public boolean pvrPlayIsRadio(String fullName) {
        return mPvrCmdManager.pvrPlayIsRadio(fullName);
    }

    @Override
    public String pvrPlayGetFileFullPath(int pvrPlayerID) {
        return mPvrCmdManager.pvrPlayGetFileFullPath(pvrPlayerID);
    }

    @Override
    public AudioInfo.AudioComponent pvrPlayGetCurrentAudio() {
        return mPvrCmdManager.pvrPlayGetCurrentAudio();
    }

    @Override
    public AudioInfo pvrPlayGetAudioComponents() {
        return mPvrCmdManager.pvrPlayGetAudioComponents();
    }

    @Override
    public int pvrPlaySelectAudio(AudioInfo.AudioComponent audio) {
        return mPvrCmdManager.pvrPlaySelectAudio(audio);
    }

    @Override
    public int pvrPlaySetWindowRect(Rect rect) {
        return mPvrCmdManager.pvrPlaySetWindowRect(rect);
    }

    @Override
    public int pvrPlaySetTrackMode(EnAudioTrackMode enTrackMode) {
        return mPvrCmdManager.pvrPlaySetTrackMode(enTrackMode);
    }

    @Override
    public EnAudioTrackMode pvrPlayGetTrackMode() {
        return mPvrCmdManager.pvrPlayGetTrackMode();
    }

    @Override
    public int pvrFileRemove(String filePath) {
        return mPvrCmdManager.pvrFileRemove(filePath);
    }

    @Override
    public int pvrFileRename(String oldName, String newName) {
        return mPvrCmdManager.pvrFileRename(oldName, newName);
    }

    @Override
    public int pvrFileGetDuration(String fullName) {
        return mPvrCmdManager.pvrFileGetDuration(fullName);
    }

    @Override
    public long pvrFileGetSize(String fullName) {
        return mPvrCmdManager.pvrFileGetSize(fullName);
    }

    @Override
    public PvrFileInfo pvrFileGetAllInfo(String fullName) {
        return mPvrCmdManager.pvrFileGetAllInfo(fullName);
    }

    @Override
    public PvrFileInfo pvrFileGetExtraInfo(String fullName) {
        return mPvrCmdManager.pvrFileGetExtraInfo(fullName);
    }

    @Override
    public PvrFileInfo pvrFileGetEpgInfo(String fullName, int epgIndex) {
        return mPvrCmdManager.pvrFileGetEpgInfo(fullName, epgIndex);
    }

    @Override
    public int pvrGetCurrentPvrMode(long channelId) {
        return mPvrCmdManager.pvrGetCurrentPvrMode(channelId);
    }

    @Override
    public int PvrSetParentLockOK() {
        return mPvrCmdManager.PvrSetParentLockOK();
    }

    @Override
    public int pvrTotalRecordFileOpen(String dirPath) {
        return mPvrCmdManager.pvrTotalRecordFileOpen(dirPath);
    }

    @Override
    public int pvrTotalRecordFileClose() {
        return mPvrCmdManager.pvrTotalRecordFileClose();
    }

    @Override
    public int pvrTotalRecordFileSort(int sortType) {
        return mPvrCmdManager.pvrTotalRecordFileSort(sortType);
    }

    @Override
    public List<PvrFileInfo> pvrTotalRecordFileGet(int startIndex, int total) {
        return mPvrCmdManager.pvrTotalRecordFileGet(startIndex, total);
    }

    @Override
    public int pvrCheckHardDiskOpen(String filePath) {
        return mPvrCmdManager.pvrCheckHardDiskOpen(filePath);
    }

    @Override
    public int pvrPlayTimeShiftStop() {
        return mPvrCmdManager.pvrPlayTimeShiftStop();
    }

    @Override
    public int pvrRecordGetLivePauseTime() {
        return mPvrCmdManager.pvrRecordGetLivePauseTime();
    }

    @Override
    public int pvrGetRatio() {
        return mPvrCmdManager.pvrGetRatio();
    }

    @Override
    public String GetRecordPath() {
        return mPvrCmdManager.GetRecordPath();
    }

    @Override
    public void setRecordPath(String path) {
        mPvrCmdManager.setRecordPath(path);
    }

    @Override
    public String getDefaultRecPath() {
        return mPvrCmdManager.getDefaultRecPath();
    }

    @Override
    public int recordTS_start(int tunerId, String fullName) {
        return mPvrCmdManager.recordTS_start(tunerId, fullName);
    }

    @Override
    public int recordTS_stop() {
        return mPvrCmdManager.recordTS_stop();
    }
    */
    /*
    OTA
     */
    @Override
    public int UpdateUsbSoftWare(String filename) {
        return mOTACmdManager.UpdateUsbSoftWare(filename);
    }

    @Override
    public int UpdateFileSystemSoftWare(String pathAndFileName, String partitionName) {
        return mOTACmdManager.UpdateFileSystemSoftWare(pathAndFileName, partitionName);
    }

    @Override
    public int UpdateOTADVBCSoftWare(int tpId, int freq, int symbol, int qam) {
        return mOTACmdManager.UpdateOTADVBCSoftWare(tpId, freq, symbol, qam);
    }

    @Override
    public int UpdateOTADVBTSoftWare(int tpId, int freq, int bandwith, int qam, int priority) {
        return mOTACmdManager.UpdateOTADVBTSoftWare(tpId, freq, bandwith, qam, priority);
    }

    @Override
    public int UpdateOTADVBT2SoftWare(int tpId, int freq, int symbol, int qam, int channelmode) {
        return mOTACmdManager.UpdateOTADVBT2SoftWare(tpId, freq, symbol, qam, channelmode);
    }

    @Override
    public int UpdateOTAISDBTSoftWare(int tpId, int freq, int bandwith, int qam, int priority) {
        return mOTACmdManager.UpdateOTAISDBTSoftWare(tpId, freq, bandwith, qam, priority);
    }

    /*
    MTest
     */
    @Override
    public int UpdateMtestOTASoftWare() {
        return mMtestCmdManager.UpdateMtestOTASoftWare();
    }

    @Override
    public OTACableParameters DVBGetOTACableParas() {
        return mMtestCmdManager.DVBGetOTACableParas();
    }

    @Override
    public OTATerrParameters DVBGetOTAIsdbtParas() {
        return mMtestCmdManager.DVBGetOTAIsdbtParas();
    }

    @Override
    public OTATerrParameters DVBGetOTATerrestrialParas() {
        return mMtestCmdManager.DVBGetOTATerrestrialParas();
    }

    @Override
    public OTATerrParameters DVBGetOTADVBT2Paras() {
        return mMtestCmdManager.DVBGetOTADVBT2Paras();
    }

    @Override
    public void PESI_CMD_CallBackTest(int hiSvrEvtAvPlaySuccess) {
        mMtestCmdManager.PESI_CMD_CallBackTest(hiSvrEvtAvPlaySuccess);
    }

    @Override
    public int getTempPesiDefaultChannelFlag() {
        return mMtestCmdManager.getTempPesiDefaultChannelFlag();
    }

    @Override
    public void setTempPesiDefaultChannelFlag(int flag) {
        mMtestCmdManager.setTempPesiDefaultChannelFlag(flag);
    }

    @Override
    public int MtestGetGPIOStatus(int u32GpioNo) {
        return mMtestCmdManager.MtestGetGPIOStatus(u32GpioNo);
    }

    @Override
    public int MtestSetGPIOStatus(int u32GpioNo, int bHighVolt) {
        return mMtestCmdManager.MtestSetGPIOStatus(u32GpioNo, bHighVolt);
    }

    @Override
    public int MtestGetATRStatus(int smartCardStatus) {
        return mMtestCmdManager.MtestGetATRStatus(smartCardStatus);
    }

    @Override
    public int MtestGetHDCPStatus() {
        return mMtestCmdManager.MtestGetHDCPStatus();
    }

    @Override
    public int MtestGetHDMIStatus() {
        return mMtestCmdManager.MtestGetHDMIStatus();
    }

    @Override
    public int MtestPowerSave() {
        return mMtestCmdManager.MtestPowerSave();
    }

    @Override
    public int MtestSevenSegment(int enable) {
        return mMtestCmdManager.MtestSevenSegment(enable);
    }

    @Override
    public int MtestSetAntenna5V(int tunerID, int tunerType, int enable) {
        return mMtestCmdManager.MtestSetAntenna5V(tunerID, tunerType, enable);
    }

    @Override
    public int MtestSetBuzzer(int enable) {
        return mMtestCmdManager.MtestSetBuzzer(enable);
    }

    @Override
    public int MtestSetLedRed(int enable) {
        return mMtestCmdManager.MtestSetLedRed(enable);
    }

    @Override
    public int MtestSetLedGreen(int enable) {
        return mMtestCmdManager.MtestSetLedGreen(enable);
    }

    @Override
    public int MtestSetLedOrange(int enable) {
        return mMtestCmdManager.MtestSetLedOrange(enable);
    }

    @Override
    public int MtestSetLedWhite(int enable) {
        return mMtestCmdManager.MtestSetLedWhite(enable);
    }

    @Override
    public int MtestSetLedOnOff(int status) {
        return mMtestCmdManager.MtestSetLedOnOff(status);
    }

    @Override
    public int MtestGetFrontKey(int key) {
        return mMtestCmdManager.MtestGetFrontKey(key);
    }

    @Override
    public int MtestSetUsbPower(int enable) {
        return mMtestCmdManager.MtestSetUsbPower(enable);
    }

    @Override
    public int MtestTestUsbReadWrite(int portNum, String path) {
        return mMtestCmdManager.MtestTestUsbReadWrite(portNum, path);
    }

    @Override
    public int MtestTestAvMultiPlay(int tunerNum, List<Integer> tunerIDs, List<Long> channelIDs) {
        return mMtestCmdManager.MtestTestAvMultiPlay(tunerNum, tunerIDs, channelIDs);
    }

    @Override
    public int MtestTestAvStopByTunerID(int tunerID) {
        return mMtestCmdManager.MtestTestAvStopByTunerID(tunerID);
    }

    @Override
    public int MtestMicSetInputGain(int value) {
        return mMtestCmdManager.MtestMicSetInputGain(value);
    }

    @Override
    public int MtestMicSetLRInputGain(int l_r, int value) {
        return mMtestCmdManager.MtestMicSetLRInputGain(l_r, value);
    }

    @Override
    public int MtestMicSetAlcGain(int value) {
        return mMtestCmdManager.MtestMicSetAlcGain(value);
    }

    @Override
    public int MtestGetErrorFrameCount(int tunerID) {
        return mMtestCmdManager.MtestGetErrorFrameCount(tunerID);
    }

    @Override
    public int MtestGetFrameDropCount(int tunerID) {
        return mMtestCmdManager.MtestGetFrameDropCount(tunerID);
    }

    @Override
    public String MtestGetChipID() {
        return mMtestCmdManager.MtestGetChipID();
    }

    @Override
    public int MtestStartMtest(String version) {
        return mMtestCmdManager.MtestStartMtest(version);
    }

    @Override
    public int MtestConnectPctool() {
        return mMtestCmdManager.MtestConnectPctool();
    }

    @Override
    public List<Integer> MtestGetWiFiTxRxLevel() {
        return mMtestCmdManager.MtestGetWiFiTxRxLevel();
    }

    @Override
    public int MtestGetWakeUpMode() {
        return mMtestCmdManager.MtestGetWakeUpMode();
    }

    @Override
    public Map<String, Integer> MtestGetKeyStatusMap() {
        return mMtestCmdManager.MtestGetKeyStatusMap();
    }

    @Override
    public int MtestEnableOpt(boolean enable) {
        return mMtestCmdManager.MtestEnableOpt(enable);
    }

    @Override
    public void TestSetTvRadioCount(int tvCount, int radioCount) {
        mMtestCmdManager.TestSetTvRadioCount(tvCount, radioCount);
    }

    @Override
    public int TestChangeTuner(int tunerType) {
        return mMtestCmdManager.TestChangeTuner(tunerType);
    }

    /*
    PIP
     */
    @Override
    public int PipOpen(int x, int y, int width, int height) {
        return mAvCmdManager.PipOpen(x, y, width, height);
    }

    @Override
    public int PipClose() {
        return mAvCmdManager.PipClose();
    }

    @Override
    public int PipStart(long channelId, int show) {
        return mAvCmdManager.PipStart(channelId, show);
    }

    @Override
    public int PipStop() {
        return mAvCmdManager.PipStop();
    }

    @Override
    public int PipSetWindow(int x, int y, int width, int height) {
        return mAvCmdManager.PipSetWindow(x, y, width, height);
    }

    @Override
    public int PipExChange() {
        return mAvCmdManager.PipExChange();
    }

    /*
    USB
     */
    @Override
    public List<Integer> GetUsbPortList() {
        return mCfgCmdManager.GetUsbPortList();
    }

    /*
    VMX
     */
    @Override
    public LoaderInfo GetLoaderInfo() {
        return mVMXCmdManager.GetLoaderInfo();
    }

    @Override
    public CaStatus GetCAStatusInfo() {
        return mVMXCmdManager.GetCAStatusInfo();
    }

    @Override
    public int GetECMcount() {
        return mVMXCmdManager.GetECMcount();
    }

    @Override
    public int GetEMMcount() {
        return mVMXCmdManager.GetEMMcount();
    }

    @Override
    public String GetLibDate() {
        return mVMXCmdManager.GetLibDate();
    }

    @Override
    public String GetChipID() {
        return mVMXCmdManager.GetChipID();
    }

    @Override
    public String GetSN() {
        return mVMXCmdManager.GetSN();
    }

    @Override
    public String GetCaVersion() {
        return mVMXCmdManager.GetCaVersion();
    }

    @Override
    public String GetSCNumber() {
        return mVMXCmdManager.GetSCNumber();
    }

    @Override
    public int GetPairingStatus() {
        return mVMXCmdManager.GetPairingStatus();
    }

    @Override
    public String GetPurse() {
        return mVMXCmdManager.GetPurse();
    }

    @Override
    public int GetGroupM() {
        return mVMXCmdManager.GetGroupM();
    }

    @Override
    public int SetPinCode(String pinCode, int pinIndex, int textSelect) {
        return mVMXCmdManager.SetPinCode(pinCode, pinIndex, textSelect);
    }

    @Override
    public int SetPPTV(String pinCode, int pinIndex) {
        return mVMXCmdManager.SetPPTV(pinCode, pinIndex);
    }

    @Override
    public void SetOMSMok() {
        mVMXCmdManager.SetOMSMok();
    }

    @Override
    public void VMXTest(int mode) {
        mVMXCmdManager.VMXTest(mode);
    }

    @Override
    public void TestVMXOTA(int mode) {
        mVMXCmdManager.TestVMXOTA(mode);
    }

    @Override
    public void VMXAutoOTA(int otaMode, int triggerID, int triggerNum, int tunerId, int satId, int dsmccPid, int freqNum, ArrayList<Integer> freqList, ArrayList<Integer> bandwidthList) {
        mVMXCmdManager.VMXAutoOTA(otaMode, triggerID, triggerNum, tunerId, satId, dsmccPid, freqNum, freqList, bandwidthList);
    }

    @Override
    public String VMXGetBoxID() {
        return mVMXCmdManager.VMXGetBoxID();
    }

    @Override
    public String VMXGetVirtualNumber() {
        return mVMXCmdManager.VMXGetVirtualNumber();
    }

    @Override
    public void VMXStopEWBS(int mode) {
        mVMXCmdManager.VMXStopEWBS(mode);
    }

    @Override
    public void VMXStopEMM() {
        mVMXCmdManager.VMXStopEMM();
    }

    @Override
    public void VMXOsmFinish(int triggerID, int triggerNum) {
        mVMXCmdManager.VMXOsmFinish(triggerID, triggerNum);
    }

    @Override
    public VMXProtectData GetProtectData() {
        return mVMXCmdManager.GetProtectData();
    }

    @Override
    public int SetProtectData(int first, int second, int third) {
        return mVMXCmdManager.SetProtectData(first, second, third);
    }

    /*
    widevine cas
     */
    @Override
    public void WidevineCasSessionId(int sessionIndex, int sessionId) {
        mCaCmdManager.WidevineCasSessionId(sessionIndex, sessionId);
    }

    /*
    other
    */
    @Override
    public void EnterViewActivity(int enter) {
        mCfgCmdManager.EnterViewActivity(enter);
    }

    @Override
    public void EnableMemStatusCheck(int enable) {
        mCfgCmdManager.EnableMemStatusCheck(enable);
    }

    @Override
    public int LoaderDtvGetJTAG() {
        return mSsuCmdManager.LoaderDtvGetJTAG();
    }

    @Override
    public int LoaderDtvSetJTAG(int value) {
        return mSsuCmdManager.LoaderDtvSetJTAG(value);
    }

    @Override
    public int LoaderDtvCheckISDBTService(OTATerrParameters ota) {
        return mSsuCmdManager.LoaderDtvCheckISDBTService(ota);
    }

    @Override
    public int LoaderDtvCheckTerrestrialService(OTATerrParameters ota) {
        return mSsuCmdManager.LoaderDtvCheckTerrestrialService(ota);
    }

    @Override
    public int LoaderDtvCheckCableService(OTACableParameters ota) {
        return mSsuCmdManager.LoaderDtvCheckCableService(ota);
    }

    @Override
    public int LoaderDtvGetSTBSN() {
        return mSsuCmdManager.LoaderDtvGetSTBSN();
    }

    @Override
    public int LoaderDtvGetChipSetId() {
        return mSsuCmdManager.LoaderDtvGetChipSetId();
    }

    @Override
    public int LoaderDtvGetSWVersion() {
        return mSsuCmdManager.LoaderDtvGetSWVersion();
    }

    @Override
    public int InvokeTest() {
        return mCfgCmdManager.InvokeTest();
    }

    @Override
    public void update_pvr_skip_list(int groupType, int IncludePVRSkipFlag, int tpId, List<Integer> pvrTpList){
        mPmManager.update_pvr_skip_list(groupType, IncludePVRSkipFlag, tpId, pvrTpList);
    }

    @Override
    public int set_net_stream_info(int GroupType, NetProgramInfo netStreamInfo) {
        return mPmManager.set_net_stream_info(GroupType, netStreamInfo);
    }

    @Override
    public int reset_net_program_database() {
        return mPmManager.reset_program_database();
    }

    @Override
    public boolean init_net_program_database() {
        return mPmManager.init_net_program_database(getTunerType());
    }

    @Override
    public void ResetCheckAD() {
        if(mTimerThrad != null)
            mTimerThrad.ResetCheckADDuration();
    }

    private void init_tuner(){
        LogUtils.d("");
        isTunerInited = true;
        mFrontendCmdManager.init_tuner();
        //if(Pvcfg.getModuleType() == Pvcfg.MODULE_DMG)
        startScheduleEit();
    }
}
