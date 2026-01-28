package com.prime.dtv.service.CommandManager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.PrimeDtvMediaPlayer;
import com.prime.dtv.service.Dvr.DvrDevManager;
import com.prime.dtv.service.Dvr.DvrRecorderController;
import com.prime.dtv.service.Player.AvCmdMiddle;
import com.prime.dtv.service.database.PvrContentProvider;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.dtv.service.datamanager.PvrDataManager;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.PvrDbRecordInfo;
import com.prime.datastructure.sysdata.PvrRecFileInfo;
import com.prime.datastructure.sysdata.PvrRecIdx;
import com.prime.datastructure.sysdata.PvrRecStartParam;
import com.prime.datastructure.sysdata.EPGEvent;
import com.prime.dtv.Interface.BaseManager;
import com.prime.dtv.Interface.PesiDtvFrameworkInterfaceCallback;
import com.prime.datastructure.sysdata.AudioInfo;
import com.prime.datastructure.sysdata.PvrInfo;
import com.prime.dtv.utils.UsbUtils;

import java.util.ArrayList;
import java.util.List;

public class PvrCmdManager extends BaseManager {
    private static final String TAG = "PvrCmdManager" ;
    private static final String PVR_FOLDER = DvrRecorderController.PVR_FOLDER ;
    private AvCmdMiddle mAvCmdMiddle;
    private DataManager mDataManager;
    private Context mContext;
    private PesiDtvFrameworkInterfaceCallback mPesiDtvCallback;
    private String PVR_PATH = "";

    private long mLastActionTime = 0;
    private static final long MIN_ACTION_INTERVAL_MS = 100;

    public ProgramInfo.VideoInfo mVideoInfo = null;
    public List<ProgramInfo.AudioInfo> mAudioInfos = new ArrayList<ProgramInfo.AudioInfo>();

    public PvrCmdManager(Context context, Handler handler) {
        super(context, TAG, handler, PvrCmdManager.class);
        mContext = context;
        mDataManager = DataManager.getDataManager(context);
        mAvCmdMiddle = AvCmdMiddle.get_instance(getApplicationContext(), getPesiDtvFrameworkInterfaceCallback());
        mPesiDtvCallback = getPesiDtvFrameworkInterfaceCallback();
    }

    public PvrCmdManager(Context context, Handler handler, Handler shareHandler) {
        super(context, TAG, handler, PvrCmdManager.class, shareHandler);
        mContext = context;
        mDataManager = DataManager.getDataManager(context);
        mAvCmdMiddle = AvCmdMiddle.get_instance(getApplicationContext(), getPesiDtvFrameworkInterfaceCallback());
        mPesiDtvCallback = getPesiDtvFrameworkInterfaceCallback();
    }

    private int PveDoCommand(Message msg){
        long now = System.nanoTime()/1_000_000L;
        if (now - mLastActionTime >= MIN_ACTION_INTERVAL_MS) {
            mLastActionTime = now;
            DoCommand(msg);
            return 0;
        } else {
            Log.d(TAG, "Commands throttled to prevent overload");
            return -1;
        }
    }

    //api--start
    public int pvrInit(String usbMountPath) {//input:Hdinfo object
        //1.pvr init
        //2.openDataBase
        //PvrContentProvider.initialize(mContext, usbMountPath);
        Log.d(TAG,"usbMountPath = "+usbMountPath+" PVR_PATH = "+PVR_PATH);
        PVR_PATH = usbMountPath + PVR_FOLDER;
        UsbUtils.set_mount_usb_path(usbMountPath);
        PvrContentProvider.setUsbPathAndInit(mContext, PVR_PATH);
        //mContext.getContentResolver().query(PvrContentProvider.CONTENT_URI, null, null, null, null);
        return 0;
    }
    public int pvrDeinit() {
        Log.d(TAG,"pvrDeinit");
        PvrContentProvider.closeDatabase();
        UsbUtils.unmount_usb_path();
        //1.pvr init
        //2.openDataBase
        return 0;
    }

    public int pvrRecordStart(PvrRecStartParam startParam,int tunerId){
        Log.d(TAG,"pvrRecordStart, tunerId = "+tunerId+" Duration="+startParam.getDuration());
        int recId = 0;
        String recPath=null;
        PvrDbRecordInfo dbRecordInfo = new PvrDbRecordInfo();
        Message msg = new Message();

        if(startParam.getSeries() == true)
            dbRecordInfo.setIsSeries(true);
        else
            dbRecordInfo.setIsSeries(false);
        dbRecordInfo.setEpisode(startParam.getEpisode());
        dbRecordInfo.setChannelNo(startParam.getmProgramInfo().getDisplayNum());
        dbRecordInfo.setAdult(startParam.getmProgramInfo().getAdultFlag());
        dbRecordInfo.setChannelLock(startParam.getmProgramInfo().getLock());
        if(startParam.getEPGEvent() != null)
            dbRecordInfo.setRating(startParam.getEPGEvent().get_parental_rate());
        dbRecordInfo.setRecordStatus(0);
        dbRecordInfo.setPlayStopPos(-1);
        if(startParam.getEpisodeLast() == true)
            dbRecordInfo.setTotalEpisode(startParam.getEpisode());
        //dbRecordInfo.setTotalRecordTime(int TotalRecordTime);
        dbRecordInfo.setChannelId(startParam.getmProgramInfo().getChannelId());
        //dbRecordInfo.setStartTime((System.currentTimeMillis() / 1000L));
        dbRecordInfo.setStartTime(System.currentTimeMillis());
        //dbRecordInfo.setPlayTime(long PlayTime);
        dbRecordInfo.setDurationSec(startParam.getDuration());
        dbRecordInfo.setServiceType(startParam.getmProgramInfo().getType());
        //dbRecordInfo.getFileSize(int FileSize);
        dbRecordInfo.setChName(startParam.getmProgramInfo().getDisplayName());
        if(startParam.getEPGEvent() != null)
            dbRecordInfo.setEventName(startParam.getEPGEvent().get_event_name());
        else
            dbRecordInfo.setEventName("");

        dbRecordInfo.setSeriesKey(startParam.getSeriesKey());
        dbRecordInfo.setVideoInfo(startParam.getmProgramInfo().pVideo);

        dbRecordInfo.setAudiosInfoList(startParam.getmProgramInfo().pAudios);
        dbRecordInfo.setSubtitleInfo(startParam.getmProgramInfo().pSubtitle);
        dbRecordInfo.setTeletextList(startParam.getmProgramInfo().pTeletext);
        dbRecordInfo.setEpgInfo(startParam.getEPGEvent());

        dbRecordInfo.setRecordStatus(0);

        // MasterIdx, SeriesIdx, FullNamePath will be set in PVR_START_RECORD
        // or we will get same index if pvrRecordStart is called twice at the same time
        msg.what = AvCmdMiddle.PVR_START_RECORD;
        msg.obj = dbRecordInfo;
        msg.arg1 = tunerId;
        DoCommand(msg);
        recId=tunerId;


        mVideoInfo=startParam.getmProgramInfo().pVideo;
        mAudioInfos = startParam.getmProgramInfo().pAudios;

//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            Log.w(TAG,"sleep interrupted");
//        }

        return recId;
    }
    public int pvrRecordStop(int recId){
        Log.d(TAG,"pvrRecordStop, recId = "+recId);
        Message msg = new Message();
        msg.what = AvCmdMiddle.PVR_STOP_RECORD;
        msg.arg1 = recId;
        DoCommand(msg);
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            Log.w(TAG,"sleep interrupted");
//        }
        return recId;
    }
    public void pvrRecordStopAll(){
        Log.d(TAG,"pvrRecordStopAll");
        Message msg = new Message();
        msg.what = AvCmdMiddle.PVR_STOP_ALLRECORD;
        DoCommand(msg);
    }

    public int pvrRecordGetRecTimeByRecId(int recId){
        Log.d(TAG,"pvrRecordGetRecTimeByRecId, recId="+recId);
        int i;
        DvrDevManager[] dvrDevManagerList;
        dvrDevManagerList =mAvCmdMiddle.getDvrDevManagerList();
        for(i=0;i<dvrDevManagerList.length;i++){
            if(dvrDevManagerList[i] != null) {
                if (dvrDevManagerList[i].getTunerId() == recId && dvrDevManagerList[i].getDvrRecorderController() != null) {
                    return dvrDevManagerList[i].getDvrRecorderController().getRecordTime();
                }
            }
        }
        return 0;
    }

    public long pvrRecordGetRecStartTimeByRecId(int recId){
        Log.d(TAG,"pvrRecordGetRecTimeByRecId, recId="+recId);
        int i;
        DvrDevManager[] dvrDevManagerList;
        dvrDevManagerList =mAvCmdMiddle.getDvrDevManagerList();
        for(i=0;i<dvrDevManagerList.length;i++){
            if(dvrDevManagerList[i] != null) {
                if (dvrDevManagerList[i].getTunerId() == recId && dvrDevManagerList[i].getDvrRecorderController() != null) {
                    return dvrDevManagerList[i].getDvrRecorderController().getStartRecTimestamp();
                }
            }
        }
        return 0;
    }

    public boolean pvrPlayCheckLastPositionPoint(PvrRecIdx recIdx) {
        Log.d(TAG,"pvrPlayCheckLastPositionPoint, recIdx="+recIdx);
        PvrDbRecordInfo pvrDbRecordInfo = new PvrDataManager(mContext).queryDataFromTable(recIdx);
        if(pvrDbRecordInfo != null){
            int playStopPos = pvrDbRecordInfo.getPlayStopPos();
            int duration = pvrDbRecordInfo.getDurationSec();
            if((playStopPos == 0xFFFFFFFF) || (playStopPos == 0)){
                return true;
            }

            if (playStopPos > duration -3) {
                //Log.e(TAG, "pvrPlayCheckLastPositionPoint: false");
                return true;
            }
        }
        return false;
    }

    public int pvrPlayFileStart(PvrRecIdx recIdx, boolean lastPositionFlag, int tunerId){
        Log.d(TAG,"pvrPlayFileStart, recIdx="+recIdx+" lastPositionFlag="+lastPositionFlag+" tunerId="+tunerId);
        int playId = tunerId,startPlayFromLastEnd=0;
        //String recPath = "/REC_0/rec_0.ts";
        Message msg = new Message();

        PvrRecFileInfo pvrRecFileInfo = pvrGetFileInfoByIndex(recIdx);
        if(lastPositionFlag == true)
            startPlayFromLastEnd = 1;

        if(pvrRecFileInfo == null){
            Log.d(TAG,"Unable to find file information by recId");
            return -1;
        }

        msg.what = AvCmdMiddle.PVR_START_PLAYBACK;
        msg.obj = pvrRecFileInfo;
        msg.arg1 = tunerId;
        msg.arg2 = startPlayFromLastEnd;
        DoCommand(msg);
        playId=tunerId;

        //LogUtils.d("pvrPlayFileStart playId="+playId);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            Log.w(TAG,"sleep interrupted");
//        }
        //LogUtils.d("pvrPlayFileStart playId="+playId);
        return playId;
    }

    public int pvrPlayFileStop(int playId){
        Log.d(TAG,"pvrPlayFileStop, playId = "+playId);
        Message msg = new Message();
        msg.what = AvCmdMiddle.PVR_STOP_PLAYBACK;
        msg.arg1 = playId;
        DoCommand(msg);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            Log.w(TAG,"sleep interrupted");
//        }
        return playId;
    }

    public int pvrPlayPlay(int playId){
        Log.d(TAG,"pvrPlayPlay, playId = "+playId);
        Message msg = new Message();
        msg.what = AvCmdMiddle.PVR_PLAYBACK_PLAY;
        msg.arg1 = playId;
        DoCommand(msg);
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            Log.w(TAG,"sleep interrupted");
//        }
        return playId;
    }

    public int pvrPlayPause(int playId){
        Log.d(TAG,"pvrPlayPause, playId = "+playId);
        Message msg = new Message();
        msg.what = AvCmdMiddle.PVR_PLAYBACK_PAUSE;
        msg.arg1 = playId;
        DoCommand(msg);
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            Log.w(TAG,"sleep interrupted");
//        }
        return playId;
    }

    public int pvrPlayFastForward(int playId){
        Log.d(TAG,"pvrPlayFastForward, playId = "+playId);
        Message msg = new Message();
        msg.what = AvCmdMiddle.PVR_PLAYBACK_FF;
        msg.arg1 = playId;
        DoCommand(msg);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            Log.w(TAG,"sleep interrupted");
//        }
        return playId;
    }

    public int pvrPlayRewind(int playId){
        Log.d(TAG,"pvrPlayFastForward, playId = "+playId);
        Message msg = new Message();
        msg.what = AvCmdMiddle.PVR_PLAYBACK_RW;
        msg.arg1 = playId;
        DoCommand(msg);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            Log.w(TAG,"sleep interrupted");
//        }
        return playId;
    }

    public int pvrPlaySeek(int playId, PvrInfo.EnSeekMode seekMode, int offsetSec){
        Log.d(TAG,"pvrPlaySeek, playId="+playId+" seekMode="+seekMode+" offsetSec="+offsetSec);
        int seek;
        if (seekMode == PvrInfo.EnSeekMode.PLAY_SEEK_SET) {
            seek = 0;
        }
        else if (seekMode == PvrInfo.EnSeekMode.PLAY_SEEK_CUR) {
            seek = 1;
        }
        else if (seekMode == PvrInfo.EnSeekMode.PLAY_SEEK_END){
            seek = 2;
        }
        else {
            seek = 3;
        }
        Message msg = new Message();
        msg.what = AvCmdMiddle.PVR_PLAYBACK_SEEK;
        msg.arg1 = playId;
        msg.arg2 = offsetSec;
        DoCommand(msg);
        return playId;
    }

    public int pvrPlaySetSpeed(int playId, PvrInfo.EnPlaySpeed playSpeed){
        Log.d(TAG,"pvrPlaySetSpeed, playId = "+playId);
        Message msg = new Message();
        msg.what = AvCmdMiddle.PVR_PLAYBACK_SET_SPEED;
        msg.arg1 = playId;
        msg.arg2 = playSpeed.getValue();
        DoCommand(msg);
        return playId;
    }

    public PvrInfo.EnPlaySpeed pvrPlayGetSpeed(int playId){
        Log.d(TAG,"pvrPlayGetSpeed, playId = "+playId);
        int i;
        float speed;
        PvrInfo.EnPlaySpeed retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FAILD;
        DvrDevManager[] dvrDevManagerList;
        dvrDevManagerList =mAvCmdMiddle.getDvrDevManagerList();
        for(i=0;i<dvrDevManagerList.length;i++){
            if(dvrDevManagerList[i] != null) {
                if (dvrDevManagerList[i].getTunerId() == playId && dvrDevManagerList[i].getDvrPlaybackController() != null) {
                    break;
                }
            }
        }
        if(i >= dvrDevManagerList.length) {
            Log.d(TAG,"pvrPlayGetSpeed fail");
            return PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FAILD;
        }
        speed = dvrDevManagerList[i].getDvrPlaybackController().getPlaySpeed();
        if(speed == 1.0){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD01;
        }
        else if(speed == 2.0){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD02;
        }
        else if(speed == 4.0){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD04;
        }
        else if(speed == 8.0){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD08;
        }
        else if(speed == 16.0){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD16;
        }
        else if(speed == 32.0){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD32;
        }
        else if(speed == -1.0){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV01;
        }
        else if(speed == -2.0){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV02;
        }
        else if(speed == -4.0){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV04;
        }
        else if(speed == -8.0){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV08;
        }
        else if(speed == -16.0){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV16;
        }
        else if(speed == -32.0){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV32;
        }
        else if(speed == 0.5){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_SLOW_FWD1;
        }
        else if(speed == 0.25){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_SLOW_FWD2;
        }
        else if(speed == 0.125){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_SLOW_FWD4;
        }
        else if(speed == -0.5){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_SLOW_REV1;
        }
        else if(speed == -0.25){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_SLOW_REV2;
        }
        else if(speed == -0.125){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_SLOW_REV4;
        }
        else if(speed == 0.0){
            if(dvrDevManagerList[i].getDvrPlaybackController().checkPlayIsPause() == true)
                retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_ZERO;
        }
        else {
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FAILD;
        }

        return retSpeed;
    }

    public int pvrPlayChangeAudioTrack(int playId, AudioInfo.AudioComponent audioComponent){
        Log.d(TAG,"pvrPlayChangeAudioTrack playId="+playId+" audioPid="+audioComponent.getPid()+" audioCodec="+audioComponent.getAudioType());
        Message msg = new Message();
        msg.what = AvCmdMiddle.PVR_PLAYBACK_CHANGE_AUDIO_TRACK;
        msg.obj = audioComponent;
        msg.arg1 = playId;
        DoCommand(msg);
        return playId;
    }

    public int pvrPlayGetCurrentAudioIndex(int playId){
        Log.d(TAG,"pvrPlayGetCurrentAudioIndex is not support");
        return 0;
    }

    public int pvrPlayGetPlayTime(int playId){
        Log.d(TAG,"pvrPlayGetPlayTime, playId = "+playId);
        int i;
        DvrDevManager[] dvrDevManagerList;
        dvrDevManagerList =mAvCmdMiddle.getDvrDevManagerList();
        for(i=0;i<dvrDevManagerList.length;i++){
            if(dvrDevManagerList[i] != null) {
                if (dvrDevManagerList[i].getTunerId() == playId && dvrDevManagerList[i].getDvrPlaybackController() != null) {
                    return dvrDevManagerList[i].getDvrPlaybackController().getCurrentPlayTimeSec();
                }
            }
        }
        return -1;
    }

    public PvrInfo.EnPlayStatus pvrPlayGetPlayStatus(int playId){
        Log.d(TAG,"pvrPlayGetPlayStatus, playId = "+playId);
        int i;
        float speed;
        PvrInfo.EnPlayStatus retStatus = PvrInfo.EnPlayStatus.PLAY_STATUS_FAILD;
        DvrDevManager[] dvrDevManagerList;
        dvrDevManagerList =mAvCmdMiddle.getDvrDevManagerList();
        for(i=0;i<dvrDevManagerList.length;i++){
            if(dvrDevManagerList[i] != null) {
                if (dvrDevManagerList[i].getTunerId() == playId && dvrDevManagerList[i].getDvrPlaybackController() != null) {
                    break;
                }
            }
        }

        if(i >= dvrDevManagerList.length) {
            Log.d(TAG,"pvrPlayGetPlayStatus fail");
        return PvrInfo.EnPlayStatus.PLAY_STATUS_STOP;
    }

        if(dvrDevManagerList[i].getDvrPlaybackController().checkPlayIsPause() == true){
            retStatus = PvrInfo.EnPlayStatus.PLAY_STATUS_PAUSE;
        }
        else{
            if(dvrDevManagerList[i].getDvrPlaybackController().getPlaySpeed() == 1.0){
                retStatus = PvrInfo.EnPlayStatus.PLAY_STATUS_PLAY;
            }
            else {
                retStatus = PvrInfo.EnPlayStatus.PLAY_STATUS_SCAN;
            }
        }

        return retStatus;
    }

    public PvrInfo.PlayTimeInfo pvrPlayGetPlayTimeInfo(int playId){
        Log.d(TAG,"pvrPlayGetPlayTime, playId = "+playId);
        PvrInfo.PlayTimeInfo playTimeInfo= new PvrInfo.PlayTimeInfo();
        DvrDevManager[] dvrDevManagerList;
        int i;
        dvrDevManagerList =mAvCmdMiddle.getDvrDevManagerList();
        for(i=0;i<dvrDevManagerList.length;i++){
            if(dvrDevManagerList[i] != null) {
                if (dvrDevManagerList[i].getTunerId() == playId && dvrDevManagerList[i].getDvrPlaybackController() != null) {
                    break;
                }
            }
        }
        if(i<dvrDevManagerList.length){
            playTimeInfo.mStartTime=0;
            playTimeInfo.mCurrentTime=dvrDevManagerList[i].getDvrPlaybackController().getCurrentPlayTimeSec();
            playTimeInfo.mEndTime=dvrDevManagerList[i].getDvrPlaybackController().getTotalRecTimeSec();
        }
        else{
            playTimeInfo.mCurrentTime=-1;
            playTimeInfo.mEndTime=-1;
            playTimeInfo.mStartTime=-1;
        }
        return playTimeInfo;
    }

    public int pvrTimeShiftStart(ProgramInfo programInfo,int recordTunerId,int playTunerId){
        Log.d(TAG,"pvrTimeShiftStart recordTunerId="+recordTunerId+" playTunerId="+playTunerId+" ChannelId="+programInfo.getChannelId());
        int playId=playTunerId;
        int recId=recordTunerId;
        String recPath;//"/storage/7F13-9710/DMG/Timeshift/timeShitf.ts";
        PvrDbRecordInfo dbRecordInfo = new PvrDbRecordInfo();
        Message msg = new Message();
        EPGEvent epgData = PrimeDtvMediaPlayer.get_instance(mContext).get_present_event(programInfo.getChannelId());

        recPath = PVR_PATH+"/Timeshift/timeShitf.ts";
        Log.d(TAG,"recPath = "+ recPath);

        dbRecordInfo.setChannelId(programInfo.getChannelId());
        dbRecordInfo.setServiceType(programInfo.getType());
        dbRecordInfo.setChName(programInfo.getDisplayName());
        if(epgData != null)
            dbRecordInfo.setEventName(epgData.get_event_name());
        else
            dbRecordInfo.setEventName("");
        dbRecordInfo.setFullNamePath(recPath);

        dbRecordInfo.setVideoInfo(programInfo.pVideo);

        dbRecordInfo.setAudiosInfoList(programInfo.pAudios);
        dbRecordInfo.setSubtitleInfo(programInfo.pSubtitle);
        dbRecordInfo.setTeletextList(programInfo.pTeletext);
        dbRecordInfo.setEpgInfo(epgData);


        msg.what = AvCmdMiddle.PVR_START_TIMESHIFT;
        msg.obj = dbRecordInfo;
        msg.arg1 = recId;
        msg.arg2 = playId;
        DoCommand(msg);

        mVideoInfo=programInfo.pVideo;
        mAudioInfos = programInfo.pAudios;
        //ChannelChangeManager.setFCCVisible(playTunerId);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            Log.w(TAG,"sleep interrupted");
//        }
        return playId;
    }

    public int pvrTimeShiftStop(){
        Log.d(TAG,"pvrTimeShiftStop");
        Message msg = new Message();
        msg.what = AvCmdMiddle.PVR_STOP_TIMESHIFT;
        DoCommand(msg);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            Log.w(TAG,"sleep interrupted");
//        }
        return 0;
    }

    public int pvrTimeShiftPlayStart(int playId){
        Log.d(TAG,"pvrTimeShiftPlayStart is not support");
        return -1;
    }

    public int pvrTimeShiftPlayPause(int playId){
        Log.d(TAG,"pvrTimeShiftPlayPause, playId = "+playId);
        Message msg = new Message();
        msg.what = AvCmdMiddle.PVR_TIMESHIFT_PLAY_PAUSE;
        msg.arg1 = playId;
        DoCommand(msg);
        return playId;
    }

    public int pvrTimeShiftPlayResume(int playId){
        Log.d(TAG,"pvrTimeShiftPlayResume, playId = "+playId);
        Message msg = new Message();
        msg.what = AvCmdMiddle.PVR_TIMESHIFT_PLAY_RESUME;
        msg.arg1 = playId;
        DoCommand(msg);
        return playId;
    }

    public int pvrTimeShiftPlayFastForward(int playId){
        Log.d(TAG,"pvrTimeShiftPlayFastForward, playId = "+playId);
        Message msg = new Message();
        msg.what = AvCmdMiddle.PVR_TIMESHIFT_PLAY_FF;
        msg.arg1 = playId;
        DoCommand(msg);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            Log.w(TAG,"sleep interrupted");
//        }
        return playId;
    }

    public int pvrTimeShiftPlayRewind(int playId){
        Log.d(TAG,"pvrTimeShiftPlayRewind, playId = "+playId);
        Message msg = new Message();
        msg.what = AvCmdMiddle.PVR_TIMESHIFT_PLAY_RW;
        msg.arg1 = playId;
        DoCommand(msg);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            Log.w(TAG,"sleep interrupted");
//        }
        return playId;
    }

    public int pvrTimeShiftPlaySeek(int playId, PvrInfo.EnSeekMode seekMode, int startPosition){
        Log.d(TAG,"pvrTimeShiftPlaySeek, playId = " + playId);
        int seek;
        if (seekMode == PvrInfo.EnSeekMode.PLAY_SEEK_SET) {
            seek = 0;
        }
        else if (seekMode == PvrInfo.EnSeekMode.PLAY_SEEK_CUR) {
            seek = 1;
        }
        else if (seekMode == PvrInfo.EnSeekMode.PLAY_SEEK_END){
            seek = 2;
        }
        else {
            seek = 3;
        }
        Message msg = new Message();
        msg.what = AvCmdMiddle.PVR_TIMESHIFT_PLAY_SEEK;
        msg.arg1 = playId;
        msg.arg2 = startPosition;
        DoCommand(msg);
        return playId;
    }

    public int pvrTimeShiftPlaySetSpeed(int playId, PvrInfo.EnPlaySpeed playSpeed){
        Log.d(TAG,"pvrTimeShiftPlaySetSpeed is not support");
        return -1;
    }

    public PvrInfo.EnPlaySpeed pvrTimeShiftPlayGetSpeed(int playId){
        Log.d(TAG,"pvrTimeShiftPlayGetSpeed, playId = "+playId);
        int i;
        float speed;
        PvrInfo.EnPlaySpeed retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FAILD;
        DvrDevManager[] dvrDevManagerList;
        dvrDevManagerList =mAvCmdMiddle.getDvrDevManagerList();
        for(i=0;i<dvrDevManagerList.length;i++){
            if(dvrDevManagerList[i] != null) {
                if (dvrDevManagerList[i].getTimeshiftPlaybackTunerId() == playId && dvrDevManagerList[i].getDvrPlaybackController() != null) {
                    break;
                }
            }
        }
        if(i >= dvrDevManagerList.length) {
            Log.d(TAG,"pvrPlayGetSpeed fail");
            return PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FAILD;
        }
        speed = dvrDevManagerList[i].getDvrPlaybackController().getPlaySpeed();
        if(speed == 1.0){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD01;
        }
        else if(speed == 2.0){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD02;
        }
        else if(speed == 4.0){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD04;
        }
        else if(speed == 8.0){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD08;
        }
        else if(speed == 16.0){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD16;
        }
        else if(speed == 32.0){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FWD32;
        }
        else if(speed == -1.0){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV01;
        }
        else if(speed == -2.0){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV02;
        }
        else if(speed == -4.0){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV04;
        }
        else if(speed == -8.0){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV08;
        }
        else if(speed == -16.0){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV16;
        }
        else if(speed == -32.0){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_REV32;
        }
        else if(speed == 0.5){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_SLOW_FWD1;
        }
        else if(speed == 0.25){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_SLOW_FWD2;
        }
        else if(speed == 0.125){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_SLOW_FWD4;
        }
        else if(speed == -0.5){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_SLOW_REV1;
        }
        else if(speed == -0.25){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_SLOW_REV2;
        }
        else if(speed == -0.125){
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_SLOW_REV4;
        }
        else if(speed == 0.0){
            if(dvrDevManagerList[i].getDvrPlaybackController().checkPlayIsPause() == true)
                retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_ZERO;
        }
        else {
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FAILD;
        }
        return retSpeed;
    }

    public int pvrTimeShiftPlayChangeAudioTrack(int playId, AudioInfo.AudioComponent audioComponent){
        Log.d(TAG,"pvrTimeShiftPlayChangeAudioTrack, playId="+playId+" audioPid="+audioComponent.getPid()+" audioCodec="+audioComponent.getAudioType());
        Message msg = new Message();
        msg.what = AvCmdMiddle.PVR_TIMESHIFT_PLAY_CHANGE_AUDIO_TRACK;
        msg.obj = audioComponent;
        msg.arg1 = playId;
        DoCommand(msg);
        return playId;
    }

    public int pvrTimeShiftPlayGetCurrentAudioIndex(int playId){
        Log.d(TAG,"pvrTimeShiftPlayGetCurrentAudioIndex is not support");
        return 0;
    }

    public PvrInfo.EnPlayStatus pvrTimeShiftPlayGetStatus(int playId){
        Log.d(TAG,"pvrTimeShiftPlayGetStatus, playId = "+playId);
        int i;
        float speed;
        PvrInfo.EnPlayStatus retStatus = PvrInfo.EnPlayStatus.PLAY_STATUS_FAILD;
        DvrDevManager[] dvrDevManagerList;
        dvrDevManagerList =mAvCmdMiddle.getDvrDevManagerList();
        for(i=0;i<dvrDevManagerList.length;i++){
            if(dvrDevManagerList[i] != null && dvrDevManagerList[i].getRecMode() == DvrDevManager.TIMESHIFT) {
                if (dvrDevManagerList[i].getTimeshiftPlaybackTunerId() == playId && dvrDevManagerList[i].getDvrPlaybackController() != null) {
                    break;
                }
            }
        }

        if(i >= dvrDevManagerList.length) {
            Log.d(TAG,TAG+"pvrTimeShiftPlayGetStatus fail");
        return PvrInfo.EnPlayStatus.PLAY_STATUS_STOP;
    }
        LogUtils.d("pvrDevInfo["+i+"].mDvrPlaybackController.getISStartPlaying()="+dvrDevManagerList[i].getDvrPlaybackController().getISStartPlaying());

        if(dvrDevManagerList[i].getDvrPlaybackController().checkPlayIsPause() == true){
            retStatus = PvrInfo.EnPlayStatus.PLAY_STATUS_PAUSE;
            Log.d(TAG,"retStatus = PvrInfo.EnPlayStatus.PLAY_STATUS_PAUSE");
        }
        else{
            if(dvrDevManagerList[i].getDvrPlaybackController().getPlaySpeed() == 1.0){
                retStatus = PvrInfo.EnPlayStatus.PLAY_STATUS_PLAY;
                Log.d(TAG,"retStatus = PvrInfo.EnPlayStatus.PLAY_STATUS_PLAY");
            }
            else {
                retStatus = PvrInfo.EnPlayStatus.PLAY_STATUS_SCAN;
                Log.d(TAG,"retStatus = PvrInfo.EnPlayStatus.PLAY_STATUS_SCAN");
            }
        }
        return retStatus;
    }

    public PvrInfo.PlayTimeInfo pvrTimeShiftGetTimeInfo(int playId){
        Log.d(TAG,"pvrPlayGetPlayTime, playId = "+playId);
        PvrInfo.PlayTimeInfo playTimeInfo= new PvrInfo.PlayTimeInfo();
        DvrDevManager[] dvrDevManagerList;
        int i,timeshiftBuffer;
        dvrDevManagerList =mAvCmdMiddle.getDvrDevManagerList();
        for(i=0;i<dvrDevManagerList.length;i++){
            if(dvrDevManagerList[i] != null) {
                if (dvrDevManagerList[i].getTimeshiftPlaybackTunerId() == playId &&
                        dvrDevManagerList[i].getDvrPlaybackController() != null &&
                        dvrDevManagerList[i].getDvrRecorderController() != null) {
                    break;
                }
            }
        }
        if(i<dvrDevManagerList.length){
            playTimeInfo.mStartTime=0;
            playTimeInfo.mCurrentTime=dvrDevManagerList[i].getDvrPlaybackController().getCurrentPlayTimeSec();
            playTimeInfo.mEndTime=dvrDevManagerList[i].getDvrRecorderController().getRecordTime();
            timeshiftBuffer = dvrDevManagerList[i].getDvrRecorderController().getTimeShiftBufferSec();
            if(playTimeInfo.mEndTime >= timeshiftBuffer)
                playTimeInfo.mEndTime = timeshiftBuffer;
        }
        else{
            playTimeInfo.mCurrentTime=-1;
            playTimeInfo.mEndTime=-1;
            playTimeInfo.mStartTime=-1;
        }
        return playTimeInfo;
    }

    public PvrRecFileInfo pvrGetFileInfoByIndex(PvrRecIdx tableKeyInfo){
        Log.d(TAG,"pvrGetFileInfoByIndex, tableKeyInfo = "+tableKeyInfo);
        PvrRecFileInfo pvrRecFileInfo = null;//new PvrRecFileInfo();
        PvrDbRecordInfo pvrDbRecordInfo = null;
        pvrDbRecordInfo = new PvrDataManager(mContext).queryDataFromTable(tableKeyInfo);
        pvrRecFileInfo = new PvrRecFileInfo(pvrDbRecordInfo);
        return pvrRecFileInfo;
    }

    public int pvrGetRecCount(){
        Log.d(TAG,"pvrGetRecCount");
        int totalRecCount;
        totalRecCount = new PvrDataManager(mContext).getMasterTotalSuccessRecordCount();
        return totalRecCount;
    }

    public int pvrGetSeriesRecCount(int masterIndex){
        Log.d(TAG,"pvrGetSeriesRecCount, masterIndex="+masterIndex);
        int seriesRecCount;
        seriesRecCount = new PvrDataManager(mContext).getSeriesSuccessRecordTotalCount(masterIndex);
        return seriesRecCount;
    }

    public int pvrGetPlaybackRecCount(){
        Log.d(TAG,"pvrGetPlaybackRecCount");
        int totalRecCount;
        totalRecCount = new PvrDataManager(mContext).getPlaybackTotalRecordCount();
        return totalRecCount;
    }

    public List<PvrRecFileInfo> pvrGetRecLink(int startIndex, int count){
        Log.d(TAG,"pvrGetSeriesRecCount, startIndex="+startIndex+" count"+count);
        List<PvrRecFileInfo> pvrRecFileInfoList = new ArrayList<>();
        List<PvrDbRecordInfo>  pvrDbRecordInfoLlist;
        pvrDbRecordInfoLlist = new PvrDataManager(mContext)
                .getMasterSuccessFilesInfoList(startIndex,count);
        for (PvrDbRecordInfo tmpDbRecordInfo : pvrDbRecordInfoLlist) {
            PvrRecFileInfo tmpRecFileInfo= new PvrRecFileInfo(tmpDbRecordInfo);
            pvrRecFileInfoList.add(tmpRecFileInfo);
        }

        // delete error recs in master table
        Thread thread = new Thread(this::pvrDelErrMasterRecs);
        thread.start();

        return pvrRecFileInfoList;
    }

    public List<PvrRecFileInfo> pvrGetPlaybackLink(int startIndex, int count){
        Log.d(TAG,"pvrGetPlaybackLink, startIndex="+startIndex+" count"+count);
        List<PvrRecFileInfo>   pvrRecFileInfoList = new ArrayList<PvrRecFileInfo>();
        List<PvrDbRecordInfo>  pvrDbRecordInfoLlist;
        pvrDbRecordInfoLlist = new PvrDataManager(mContext).getPlaybackFilesInfoList(startIndex,count);
        for (PvrDbRecordInfo tmpDbRecordInfo : pvrDbRecordInfoLlist) {
            PvrRecFileInfo tmpRecFileInfo= new PvrRecFileInfo(tmpDbRecordInfo);
            pvrRecFileInfoList.add(tmpRecFileInfo);
        }
        return pvrRecFileInfoList;
    }

    public List<PvrRecFileInfo> pvrGetSeriesRecLink(PvrRecIdx tableKeyInfo, int count){
        Log.d(TAG,"pvrGetSeriesRecLink, tableKeyInfo="+tableKeyInfo+" count"+count);
        List<PvrRecFileInfo> pvrRecFileInfoList = new ArrayList<>();
        List<PvrDbRecordInfo>  pvrDbRecordInfoLlist;
        pvrDbRecordInfoLlist = new PvrDataManager(mContext)
                .getSeriesSuccessFilesInfoList(
                        tableKeyInfo.getMasterIdx(),
                        tableKeyInfo.getSeriesIdx(),
                        count);
        for (PvrDbRecordInfo tmpDbRecordInfo : pvrDbRecordInfoLlist) {
            PvrRecFileInfo tmpRecFileInfo= new PvrRecFileInfo(tmpDbRecordInfo);
            pvrRecFileInfoList.add(tmpRecFileInfo);
        }

        // delete error recs in series table
        Thread thread = new Thread(() -> pvrDelErrSeriesRecs(tableKeyInfo.getMasterIdx()));
        thread.start();

        return pvrRecFileInfoList;
    }

    public int pvrDelAllRecs(){
        Log.d(TAG,"pvrDelAllRecs");
        return new PvrDataManager(mContext).deleteAllRecordFiles();
    }

    public int pvrDelSeriesRecs(int masterIndex){
        Log.d(TAG,"pvrDelSeriesRecs, masterIndex="+masterIndex);
        return new PvrDataManager(mContext).deleteSeriesRecordFolder(masterIndex);
    }

    public int pvrDelRecsByChId(int channelId){
        Log.d(TAG,"pvrDelRecsByChId, channelId="+channelId);
        return new PvrDataManager(mContext).deleteRecordFilesByChannelId(channelId);
    }

    public int pvrDelOneRec(PvrRecIdx tableKeyInfo){
        Log.d(TAG,"pvrDelOneRec, tableKeyInfo="+tableKeyInfo);
        return new PvrDataManager(mContext).deleteRecordFile(tableKeyInfo);
    }

    public int pvrDelOnePlayback(PvrRecIdx tableKeyInfo){
        Log.d(TAG,"pvrDelOnePlayback, tableKeyInfo="+tableKeyInfo);
        return new PvrDataManager(mContext).deleteOnePlaybackInList(tableKeyInfo);
    }

    public int pvrCheckSeriesEpisode(byte[] seriesKey, int episode){
        Log.d(TAG,"pvrCheckSeriesEpisode, episode="+episode+" seriesKey="+seriesKey);
        return new PvrDataManager(mContext).querySeriesEpisode(seriesKey,episode);
    }

    public boolean pvrCheckSeriesComplete(byte[]  seriesKey){
        Log.d(TAG,"pvrCheckSeriesEpisode, seriesKey="+seriesKey);
        return new PvrDataManager(mContext).checkSeriesComplete(seriesKey);
    }

    // return whether input pvrRecIdx is recording/playing
    public boolean pvrIsIdxInUse(PvrRecIdx pvrRecIdx) {
        DvrDevManager[] dvrDevManagers = mAvCmdMiddle.getDvrDevManagerList();
        for (DvrDevManager dvrDevManager : dvrDevManagers) {
            if (dvrDevManager != null && dvrDevManager.getPvrRecIdx().equals(pvrRecIdx)) {
                return true;
            }
        }

        return false;
    }

    public int pvrDelErrMasterRecs() {
        PvrDataManager pvrDataManager = new PvrDataManager(mContext);
        // get error recs in master table
        List<PvrDbRecordInfo> masterErrRecInfoList = pvrDataManager.getMasterErrorFilesInfoList(
                0,
                pvrDataManager.getMasterTotalErrorRecordCount());

        int count = 0;
        for (PvrDbRecordInfo masterErrRecInfo : masterErrRecInfoList) {
            int masterIdx = masterErrRecInfo.getMasterIdx();
            if (masterErrRecInfo.getIsSeries()) { // is series master
                count += pvrDelErrSeriesRecs(masterIdx);
            } else { // normal recs
                PvrRecIdx pvrRecIdx = new PvrRecIdx(masterIdx, masterErrRecInfo.getSeriesIdx());
                // do not delete playing or recording recs
                if (!pvrIsIdxInUse(pvrRecIdx)) {
                    pvrDataManager.deleteRecordFile(pvrRecIdx);
                    count++;
                }
            }
        }

        return count;
    }

    public int pvrDelErrSeriesRecs(int masterIdx) {
        PvrDataManager pvrDataManager = new PvrDataManager(mContext);
        // get error recs in series table
        List<PvrDbRecordInfo> seriesErrRecInfoList = pvrDataManager
                .getSeriesErrorFilesInfoList(
                        masterIdx,
                        0,
                        pvrDataManager.getSeriesErrorRecordTotalCount(masterIdx));

        int count = 0;
        for (PvrDbRecordInfo seriesErrRecInfo : seriesErrRecInfoList) {
            int seriesIdx = seriesErrRecInfo.getSeriesIdx();
            PvrRecIdx pvrRecIdx = new PvrRecIdx(masterIdx, seriesIdx);
            // do not delete playing or recording recs
            if (!pvrIsIdxInUse(pvrRecIdx)) {
                pvrDataManager.deleteRecordFile(pvrRecIdx);
                count++;
            }
        }

        return count;
    }

    public int pvrModifyPlayStopPos(PvrRecIdx pvrRecIdx, int position) {
        Log.d(TAG,"pvrModifyPlayStopPos, pvrRecIdx master = " + pvrRecIdx.getMasterIdx() + " series = " + pvrRecIdx.getSeriesIdx());
        return new PvrDataManager(mContext).modifyPlayStopPos(pvrRecIdx, position);
    }
    //api--end

    public int recordTS_start(int tunerId, String fullName) {
        Log.d(TAG,"recordTS_start is not support");
        return 0;
    }

    public int recordTS_stop() {
        Log.d(TAG,"recordTS_stop is not support");
        return 0;
    }

    @Override
    public void BaseHandleMessage(Message msg) {
        mAvCmdMiddle.BaseHandleMessage(msg,getHandlerThreadHandler());
    }

    @Override
    public void destroy() {
        mAvCmdMiddle.destroy();
        super.destroy();
    }
}
