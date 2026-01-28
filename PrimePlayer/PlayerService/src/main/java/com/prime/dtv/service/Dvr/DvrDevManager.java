package com.prime.dtv.service.Dvr;

import static android.media.tv.tuner.filter.RecordSettings.INDEX_TYPE_SC_HEVC;
import static android.media.tv.tuner.filter.RecordSettings.SC_INDEX_I_FRAME;
import static android.media.tv.tuner.filter.RecordSettings.SC_INDEX_I_SLICE;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCas;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.tv.tuner.filter.AvSettings;
import android.media.tv.tuner.filter.Filter;
import android.media.tv.tuner.filter.FilterCallback;
import android.media.tv.tuner.filter.FilterConfiguration;
import android.media.tv.tuner.filter.FilterEvent;
import android.media.tv.tuner.filter.RecordSettings;
import android.media.tv.tuner.filter.Settings;
import android.media.tv.tuner.filter.TsFilterConfiguration;
import android.media.tv.tuner.filter.TsRecordEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemProperties;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.prime.android.audiotrack.PrimeAudioTrack;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.AudioInfo;
import com.prime.datastructure.sysdata.DTVMessage;
import com.prime.datastructure.sysdata.EPGEvent;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.PvrDbRecordInfo;
import com.prime.datastructure.sysdata.PvrInfo;
import com.prime.datastructure.sysdata.PvrRecFileInfo;
import com.prime.datastructure.sysdata.PvrRecIdx;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.dtv.Interface.PesiDtvFrameworkInterfaceCallback;
import com.prime.dtv.service.Player.CasSession;
import com.prime.dtv.service.Table.StreamType;
import com.prime.dtv.service.Tuner.TunerInterface;
import com.prime.dtv.service.Util.MediaUtils;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.dtv.service.datamanager.PvrDataManager;
import com.prime.dtv.utils.UsbUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;


public class DvrDevManager {
    private static final String TAG = "DVR DvrDevManager";

    private boolean DEBUG;

    private static final String PERSIST_PVR_DEBUG = "persist.sys.prime.pvr.debug";
    public static final int SUCCESS = 0;
    public static final int FAILURE = 1;

    public static final int RECORD = 0;
    public static final int PLAYBACK = 1;
    public static final int TIMESHIFT = 2;
    public static final int PLAYRECORDING = 3;


    private static final int AUDIO_SAMPLE_RATE = 48000;//44100;//48000;
    private static final int AUDIO_AAC_PROFILE = 2; /* OMX_AUDIO_AACObjectLC */
    private static final int AUDIO_CHANNEL_COUNT = 2; // mono
    private static final int AUDIO_BIT_RATE = 192000;
    private static final int THREAD_COUNT = 1;
    private static final int RECORD_TIME_DIFF_TOLERANCE_SEC = 10;

    private Context mContext = null;
    private PesiDtvFrameworkInterfaceCallback mPesiDtvCallback = null;
    private DataManager mDatamanager = null;
    private ExecutorService mExecutor;
    private TunerInterface mTunerInterface;
    private MediaFormat mVideoMediaFormat;
    private MediaCodec mVideoCodec;
    private MediaCodec.OnFrameRenderedListener mFrameRenderedListener;
    private AudioTrack mAudioTrack;

    private int mTunerId=-1;
    private int mTunerIdForTimeshiftRecord=-1;
    private int mTunerIdForTimeshiftPlayback=-1;
    private int mRecMode;
    private PvrRecIdx mPvrRecIdx;
    private DvrRecorderController mDvrRecorderController ;
    private DvrPlaybackController mDvrPlaybackController ;
    private CasSession mCasSession ;

    private int mPvrAvSyncHwId;

    private DvrRecorderController mDvrRecorderControllerTmp =null;

    public Filter mVideoFilter;
    public int mCurrentAudioPid;
    public int mCurrentSubtitlePid;
    public int mCurrentTeltextPid;
    public List<Integer> mAudioPidList;
    public List<Filter> mAudioFilterList;
    public List<Filter> mSubtitleFilterList;
    public List<Filter> mTeltextFilterList;
    private Filter mRecorderPATFilter;
    private Filter mRecorderPMTFilter;
    public Surface mSurface;
    private String mFileInfoWriteTmp;
    private BaseFilterCallback mRecorderBaseFilterCallback=null;
    private long mChannelId;
    //PVR VBM Agent 6 (Value 0 ~ Value 5)
    private String[] mCurrentPVRVbmBaseData = null;

    private String[] mCurrentTimeShiftVbmBaseData = null;
    private long mTsPauseStartTime = 0;
    public DvrDevManager(int recMode,Context context,PesiDtvFrameworkInterfaceCallback callback, ExecutorService executor){
        mContext = context;
        mPesiDtvCallback = callback;
        mDatamanager = DataManager.getDataManager(context);
        mTunerInterface = TunerInterface.getInstance(context);
        mExecutor = executor;
        mRecMode = recMode;
        //mTunerId = tunerId;
        //if(recMode == TIMESHIFT)
            //mTunerIdForTimeshiftRecord = tunerId;
        mAudioPidList  = new ArrayList<>();
        mAudioFilterList = new ArrayList<>();
        mSubtitleFilterList = new ArrayList<>();
        mTeltextFilterList = new ArrayList<>();
        mPvrRecIdx = new PvrRecIdx(-1,-1);
        mCurrentAudioPid =0x1FFF;
        mCurrentSubtitlePid = 0x1FFF;
        mCurrentTeltextPid = 0x1FFF;
        mPvrAvSyncHwId=-1;
        mFileInfoWriteTmp=null;
        DEBUG = SystemProperties.getBoolean(PERSIST_PVR_DEBUG, false);
    }

    public void setTunerIdForTimeshiftPlayback(int tunerId){
        mTunerIdForTimeshiftPlayback = tunerId;
    }

    public void setSurface(Surface surface){
        mSurface = surface;
    }

    public int getTunerId(){
        return mTunerId;
    }

    public int getTimeshiftRecordTunerId(){
        return mTunerIdForTimeshiftRecord;
    }

    public int getTimeshiftPlaybackTunerId(){
        return mTunerIdForTimeshiftPlayback;
    }

    public int getRecMode(){
        return mRecMode;
    }

    public DvrRecorderController getDvrRecorderController(){
        return mDvrRecorderController;
    }

    public DvrRecorderController getDvrRecorderControllerTmp(){
        return mDvrRecorderControllerTmp;
    }

    public void setDvrRecorderControllerTmp(DvrRecorderController recHandle){
        mDvrRecorderControllerTmp = recHandle;
        Log.d(TAG,"setDvrRecorderControllerTmp="+mDvrRecorderControllerTmp);
    }

    public DvrPlaybackController getDvrPlaybackController(){
        return mDvrPlaybackController;
    }

    public int recordStart(PvrDbRecordInfo mDbRecordInfo, int recordTunerId){
        int result;
        String fullPath,fullPathTmp;
        String keyword= DvrRecorderController.PVR_FOLDER;
        int index;
        Log.d(TAG,"recordstart start, recordTunerId="+recordTunerId);

        fullPath=mDbRecordInfo.getFullNamePath();
        mPvrRecIdx.setMasterIdx(mDbRecordInfo.getMasterIdx());
        mPvrRecIdx.setSeriesIdx(mDbRecordInfo.getSeriesIdx());
        mTunerId = recordTunerId;

        index = mDbRecordInfo.getFullNamePath().indexOf(keyword);
        if (index != -1) {
            fullPathTmp= mDbRecordInfo.getFullNamePath().substring(index);
            mDbRecordInfo.setFullNamePath(fullPathTmp);
        } else {
            Log.e(TAG,"The default directory could not be found");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_REC_ERROR, recordTunerId, (int)mDbRecordInfo.getChannelId(), null);
            return FAILURE;
        }

        if(DvrRecorderController.checkRecorderPath(fullPath,DvrRecorderController.RECORDERMODE_RECORD) == false){
            Log.e(TAG,"The checkRecorderPath fail");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_REC_ERROR, recordTunerId, (int)mDbRecordInfo.getChannelId(), null);
            return FAILURE;
        }

        if(startRecorderTunerTune(mDbRecordInfo.getChannelId()) == false){
            Log.e(TAG,"startRecorderTunerTune fail");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_REC_ERROR, recordTunerId, (int)mDbRecordInfo.getChannelId(), null);
            return FAILURE;
        }

//        if(SystemProperties.get("persist.sys.tunerhal.source.cas.type").equals("ENC") ) {
//            SystemProperties.set("persist.sys.tunerhal.source.cas.type", "WV");
//            AudioManager audioManager = mContext.getSystemService(AudioManager.class);
//            audioManager.setParameters("rtk_tunerhal_set_prop_cas_type=WV");
//            mTunerInterface.requestTuner(recordTunerId, TunerInterface.TUNER_TYPE_LIVE);
//        }

        startRealDVRRecorder(mDbRecordInfo,recordTunerId,fullPath);

        mDbRecordInfo.setFullNamePath(fullPathTmp);
        result=new PvrDataManager(mContext).addDataToTable(mDbRecordInfo);
        if(result != 0){
            Log.e(TAG,"addDataToTable fail");
        }

        PvrRecIdx pvrRecIdx = new PvrRecIdx(mDbRecordInfo.getMasterIdx(), mDbRecordInfo.getSeriesIdx());
        mPesiDtvCallback.sendCallbackMessage(
                DTVMessage.PESI_SVR_EVT_PVR_RECORDING_SUCCESS,
                recordTunerId,
                (int)mDbRecordInfo.getChannelId(),
                pvrRecIdx.toCombinedString());

        Log.d(TAG,"recordstart success,channelId="+mDbRecordInfo.getChannelId());
        mChannelId = mDbRecordInfo.getChannelId();
        return SUCCESS;
    }

    public void recordStop(int recordTunerId) {
        int recordTime=0,recordSizeKb;
        int i,result;
        long channelId=0;
        Log.d(TAG,"recordStop start, recordTunerId="+recordTunerId);

        if(mDvrRecorderController != null){
            recordTime = mDvrRecorderController.getRecordTime();
            recordSizeKb =(int)(mDvrRecorderController.getTotalRecordSize() / 1000);
            PvrDataManager pvrDataManager = new PvrDataManager(mContext);
            pvrDataManager.modifyDurationSec(mPvrRecIdx,recordTime);
            pvrDataManager.modifyFileSizeKbyte(mPvrRecIdx,recordSizeKb);

            PvrDbRecordInfo pvrDbRecordInfo = pvrDataManager.queryDataFromTable(mPvrRecIdx);
            EPGEvent epgEvent = pvrDbRecordInfo.getEpgInfo();
            int expectRecordTime = epgEvent != null ?
                    (int)epgEvent.get_duration()/1000 : mDvrRecorderController.getDurationSec();
            if (expectRecordTime - recordTime > RECORD_TIME_DIFF_TOLERANCE_SEC) {
                pvrDataManager.modifyRecordStatus(mPvrRecIdx, 2); // 2 = record success but incomplete
            } else {
                pvrDataManager.modifyRecordStatus(mPvrRecIdx, 1); // 1 = record success and complete
            }

            deConfigureRecorderFilter();
            mDvrRecorderController.stopDVRRecorder();
            mDvrRecorderController = null;
            //wv2RecordStopCasPlay();
            stopWvCas2Record();
            //PvrDataManager = null;
        }
        mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_RECORDING_STOP_SUCCESS, recordTunerId, (int)mChannelId, null);
        Log.d(TAG,"recordStop success,channelId="+channelId+" recordTime="+recordTime);

    }
    private String[] getPVRVbmDataWithAction(int action) {
        if (mCurrentPVRVbmBaseData == null) {
            return null; // 若沒有 Start 過，則無資料
        }
        try {
            // 複製一份新的陣列，避免汙染原始資料或多執行緒問題
            String[] data = mCurrentPVRVbmBaseData.clone();
            data[0] = "0";//0:LPVR : local pvr, 目前只有此設定,  1:HNPVR : 子母雞pvr, 已無此設定
            // 設定 Action (Value_6)
            data[6] = String.valueOf(action);
            return data;
        } catch (Exception e) {
            Log.e(TAG, "getPVRVbmDataWithAction error: " + e.getMessage());
            return null;
        }
    }
    private String[] getTimeShiftVbmDataWithAction(int action) {
        if (mCurrentTimeShiftVbmBaseData == null) {
            return null; // 若沒有 Start 過，則無資料
        }
        try {
            // 複製一份新的陣列，避免汙染原始資料或多執行緒問題
            String[] data = mCurrentTimeShiftVbmBaseData.clone();
            // 設定 Action (Value_3)
            data[3] = String.valueOf(action);
            return data;
        } catch (Exception e) {
            Log.e(TAG, "getPVRVbmDataWithAction error: " + e.getMessage());
            return null;
        }
    }
    private int getCurrentTimestamp() {
        if (mDvrPlaybackController != null) {
            return mDvrPlaybackController.getCurrentPlayTimeSec();
        }
        return 0;
    }
    public int playbackStart(PvrRecFileInfo pvrRecFileInfo, int lastPositionFlag, int playTunerId){
        int index=0xff,i, result;
        //String usbRootPath="/storage/7F13-9710/DMG";
        String fullPath = "";
        Log.d(TAG,"playbackStart start, playTunerId="+playTunerId);

        mPvrRecIdx.setMasterIdx(pvrRecFileInfo.getMasterIdx());
        mPvrRecIdx.setSeriesIdx(pvrRecFileInfo.getSeriesIdx());
        mTunerId = playTunerId;

        SystemProperties.set("persist.sys.tunerhal.source.cas.type", "ENC");
        AudioManager audioManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            audioManager = mContext.getSystemService(AudioManager.class);
        }
        audioManager.setParameters("rtk_tunerhal_set_prop_cas_type=ENC");
        mTunerInterface.requestTuner(playTunerId, TunerInterface.TUNER_TYPE_PLAYBACK);

        //fullPath = pvrRecFileInfo.getFullNamePath();

        fullPath = UsbUtils.get_mount_usb_path()+pvrRecFileInfo.getFullNamePath();

        //VBM Agent 6
        mCurrentPVRVbmBaseData = new String[7];

        try {
            // Value_0: Request from (0: Master)
            mCurrentPVRVbmBaseData[0] = "0";
            // Value_1: Launch from (0: Portal)
            mCurrentPVRVbmBaseData[1] = "0";

            // Value_2: Service ID
            ProgramInfo programInfo = mDatamanager.getProgramInfo(pvrRecFileInfo.getChannelId());
            mCurrentPVRVbmBaseData[2] = (programInfo != null) ? String.valueOf(programInfo.getServiceId()) : "0";

            // Value_3: Event Name
            mCurrentPVRVbmBaseData[3] = pvrRecFileInfo.getEventName();

            // Value_4 & 5: Date & Time
            long startTimeMs = pvrRecFileInfo.getStartTime() * 1000L;
            long durationMs = pvrRecFileInfo.getDurationSec() * 1000L;
            long endTimeMs = startTimeMs + durationMs;

            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault());
            java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HHmm", java.util.Locale.getDefault());
            java.util.Date startDate = new java.util.Date(startTimeMs);
            java.util.Date endDate = new java.util.Date(endTimeMs);

            mCurrentPVRVbmBaseData[4] = dateFormat.format(startDate);
            mCurrentPVRVbmBaseData[5] = timeFormat.format(startDate) + "-" + timeFormat.format(endDate);

            // Value_6: Action 在 getPVRVbmDataWithAction 中設定

            Log.d(TAG, "VBM Base Data Init: " + mCurrentPVRVbmBaseData[3]);

        } catch (Exception e) {
            Log.e(TAG, "Prepare VBM Data error: " + e.getMessage());
            mCurrentPVRVbmBaseData = null; // 發生錯誤則置空
        }

        // 準備發送用的資料 (Action 0: Play)
        String[] vbmData = getPVRVbmDataWithAction(0);
        if(startRealDVRPlayback(pvrRecFileInfo, playTunerId,lastPositionFlag,fullPath) == 1){
            Log.e(TAG,"startRealDVRPlayback fail");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_ERROR, 0, 0, null);
            return FAILURE;
        }
        result=new PvrDataManager(mContext).addDataToPlaybackTable(pvrRecFileInfo);
        if(result != 0){
            Log.e(TAG,"addDataToPlaybackTable fail");
        }

        mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAYBACK_SUCCESS, 0, 0, vbmData);

        Log.d(TAG,"playbackStart success");
        return SUCCESS;
    }

    public void playbackResume(int playTunerId) {
        Log.d(TAG,"playbackResume start, playTunerId="+playTunerId);
        String[] vbmData = getPVRVbmDataWithAction(0);
        if(mDvrPlaybackController == null){
            Log.e(TAG,"mDvrPlaybackController is null");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_RESUME_ERROR, 0, 0, null);
        }
        else {
            mDvrPlaybackController.playerResume();
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_RESUME_SUCCESS, 0, 0, vbmData);
        }
        Log.d(TAG,"playbackResume end");
    }

    public void playbackPause(int playTunerId) {
        Log.d(TAG,"playbackPause start, playTunerId="+playTunerId);
        String[] vbmData = getPVRVbmDataWithAction(1);
        if(mDvrPlaybackController == null){
            Log.e(TAG,"mDvrPlaybackController is null");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_PAUSE_ERROR, 0, 0, null);
        }
        else {
            mDvrPlaybackController.playerPause();
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_PAUSE_SUCCESS, 0, 0, vbmData);
        }
        Log.d(TAG,"playbackPause success");
    }

    public void playbackFastForward(int playTunerId) {
        int recordTime;
        float speed;
        PvrInfo.EnPlaySpeed retSpeed ;
        String[] vbmData = getPVRVbmDataWithAction(3);
        Log.d(TAG,"fastForward start, playTunerId="+playTunerId);
        if(mDvrPlaybackController == null){
            Log.e(TAG,"mDvrPlaybackController is null");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_FF_ERROR, 0, 0, null);
        }
        else {
            if (mDvrPlaybackController.getVideoMediaCodec() == null) {
                Log.e(TAG, "The Record file of radio do not support this function");
                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_NOT_SUPPORT, 0, 0, null);
                return;
            }
            mDvrPlaybackController.fastForward();
            speed = mDvrPlaybackController.getPlaySpeed();
            retSpeed = convertSpeedUnits(speed);
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_FF_SUCCESS, retSpeed.getValue(), 0, vbmData);
        }
        Log.d(TAG,"fastForward success");
    }

    public void playbackRewind(int playTunerId) {
        int recordTime;
        float speed;
        PvrInfo.EnPlaySpeed retSpeed ;
        String[] vbmData = getPVRVbmDataWithAction(4);
        Log.d(TAG,"playbackRewind start, playTunerId="+playTunerId);
        if(mDvrPlaybackController == null){
            Log.e(TAG,"mDvrPlaybackController is null");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_RW_ERROR, 0, 0, null);
        }
        else {
            if (mDvrPlaybackController.getVideoMediaCodec() == null) {
                Log.e(TAG, "The Record file of radio do not support this function");
                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_NOT_SUPPORT, 0, 0, null);
                return;
            }
            mDvrPlaybackController.backForward();
            speed = mDvrPlaybackController.getPlaySpeed();
            retSpeed = convertSpeedUnits(speed);
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_RW_SUCCESS, retSpeed.getValue(), 0, vbmData);
        }
        Log.d(TAG,"playbackRewind success");
    }

    public void playbackSeek(int playTunerId,int seekTime) {
        int SeekTimeSec=seekTime;
        String[] vbmData = getPVRVbmDataWithAction(2);
        Log.d(TAG,"playbackSeek start, playTunerId="+playTunerId+" seekTime="+seekTime);
        if(mDvrPlaybackController == null){
            Log.e(TAG,"mDvrPlaybackController is null");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_SEEK_ERROR, 0, 0, null);
        }
        else {
            if (false/*mDvrPlaybackController.getVideoMediaCodec() == null*/) {
                Log.e(TAG, "The Record file of radio do not support this function");
                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_NOT_SUPPORT, 0, 0, null);
                return;
            }
            mDvrPlaybackController.setLastPosition(seekTime);
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_SEEK_SUCCESS, 0, 0, vbmData);
        }
        Log.d(TAG,"playbackSeek success");
    }

    public void playbackSetSpeed(int playTunerId,float speed) {
        float speedTmp;
        PvrInfo.EnPlaySpeed retSpeed ;
        Log.d(TAG,"playbackSetSpeed start, playTunerId="+playTunerId);
        if(mDvrPlaybackController == null){
            Log.e(TAG,"mDvrPlaybackController is null");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_SET_SPEED_ERROR, 0, 0, null);
        }
        else {
            mDvrPlaybackController.playerSetSpeed(speed);
            speedTmp = mDvrPlaybackController.getPlaySpeed();
            retSpeed = convertSpeedUnits(speedTmp);
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_SET_SPEED_SUCCESS, retSpeed.getValue(), 0, null);
        }
        Log.d(TAG,"playbackSetSpeed success");
    }

    public void playbackStop(int playTunerId){
        Log.d(TAG,"playbackStop start, playTunerId="+playTunerId);

        int index=0xff,i;
        int  playStopTime,recordTime;
        PvrDbRecordInfo pvrDbRecordInfo;

        playStopTime = mDvrPlaybackController.getCurrentPlayTimeSec();

        mDvrPlaybackController.playerStop();

        pvrDbRecordInfo = new PvrDataManager(mContext).queryDataFromTable(mPvrRecIdx);
        recordTime = pvrDbRecordInfo.getDurationSec();

//        if((recordTime - playStopTime) > 10)
//            new PvrDataManager(mContext).modifyPlayStopPos(mPvrRecIdx,playStopTime);
//        else
//            new PvrDataManager(mContext).modifyPlayStopPos(mPvrRecIdx,0);

        if(playStopTime < recordTime)
            new PvrDataManager(mContext).modifyPlayStopPos(mPvrRecIdx,playStopTime);
        else
           new PvrDataManager(mContext).modifyPlayStopPos(mPvrRecIdx,recordTime);

        playbackClose();

        mDvrPlaybackController=null;
        mDvrRecorderControllerTmp=null;
        SystemProperties.set("persist.sys.tunerhal.source.cas.type", "WV");
        AudioManager audioManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            audioManager = mContext.getSystemService(AudioManager.class);
        }
        audioManager.setParameters("rtk_tunerhal_set_prop_cas_type=WV");
        mTunerInterface.requestTuner(playTunerId, TunerInterface.TUNER_TYPE_LIVE);

        mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAYBACK_STOP_SUCCESS, 0, 0, null);
        Log.d(TAG,"playbackStop success");
    }

    public int timeshiftStart(PvrDbRecordInfo mDbRecordInfo, int recordTunerId, int playTunerId) {
        Log.d(TAG,"timeshiftStart start, recordTunerId="+recordTunerId+" playTunerId="+playTunerId);
        int index=0xff,i,result;

        String fullPath;
        PvrRecFileInfo pvrRecFileInfo = new PvrRecFileInfo();

        fullPath=mDbRecordInfo.getFullNamePath();

        mTunerId=recordTunerId;
        mTunerIdForTimeshiftRecord=recordTunerId;
        mTunerIdForTimeshiftPlayback=playTunerId;

        //LogUtils.d(TAG+"fullPath = "+fullPath);
        if(DvrRecorderController.checkRecorderPath(fullPath,DvrRecorderController.RECORDERMODE_TIMEShIFT) == false){
            Log.e(TAG,"Check Recorder Path fail, fullPath="+fullPath);
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_ERROR, 0, 0, null);
            return FAILURE;
        }

        if(startRecorderTunerTune(mDbRecordInfo.getChannelId()) == false){
            Log.e(TAG,"startRecorderTunerTune fail");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_ERROR, 0, 0, null);
            return FAILURE;
        }

        startRealDVRRecorder(mDbRecordInfo,recordTunerId,fullPath);

        SystemProperties.set("persist.sys.tunerhal.source.cas.type", "ENC");
        AudioManager audioManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            audioManager = mContext.getSystemService(AudioManager.class);
        }
        audioManager.setParameters("rtk_tunerhal_set_prop_cas_type=ENC");
        mTunerInterface.requestTuner(mTunerIdForTimeshiftPlayback, TunerInterface.TUNER_TYPE_PLAYBACK);

        pvrRecFileInfo.setVideoInfo(mDbRecordInfo.getVideoInfo());
        pvrRecFileInfo.setAudiosInfoList(mDbRecordInfo.getAudiosInfoList());
        pvrRecFileInfo.setSubtitleInfo(mDbRecordInfo.getSubtitleInfo());
        pvrRecFileInfo.setTeletextList(mDbRecordInfo.getTeletextList());
        pvrRecFileInfo.setTotalRecordTime(0);
        //LogUtils.d("fullPath = "+fullPath);

        mVideoFilter=null;
        mAudioFilterList.clear();
        mSubtitleFilterList.clear();
        mTeltextFilterList.clear();
        //VBM Agent 6
        mCurrentTimeShiftVbmBaseData = new String[10]; // 預留到 Value_9
        try {
            // Value_0: Request from (0: Master)
            mCurrentTimeShiftVbmBaseData[0] = "0";

            // Value_1: Service Id
            // 使用 mDbRecordInfo 取得 Channel ID 再查 Service ID
            ProgramInfo programInfo = mDatamanager.get_service_by_channelId(mDbRecordInfo.getChannelId());
            if (programInfo == null) {
                // 如果上面查不到，嘗試用 getProgramInfo
                programInfo = mDatamanager.getProgramInfo(mDbRecordInfo.getChannelId());
            }
            mCurrentTimeShiftVbmBaseData[1] = (programInfo != null) ? String.valueOf(programInfo.getServiceId()) : "0";

            // Value_2: Event Name
            // 嘗試從 EPG 取得，若無則用 DB 紀錄的名稱
            if (mDbRecordInfo.getEpgInfo() != null) {
                mCurrentTimeShiftVbmBaseData[2] = mDbRecordInfo.getEventName();
            } else {
                mCurrentTimeShiftVbmBaseData[2] = "Timeshift Event";
            }

            // Value_3: Action (在 helper 中填入)
            // Value_4: How much time (預設 0)
            mCurrentTimeShiftVbmBaseData[4] = "0";
            // Value_5: Playback type (0: Manual playback)
            mCurrentTimeShiftVbmBaseData[5] = "0";

            Log.d(TAG, "TS VBM Init: SrvID=" + mCurrentTimeShiftVbmBaseData[1] + ", Name=" + mCurrentTimeShiftVbmBaseData[2]);

        } catch (Exception e) {
            Log.e(TAG, "Prepare TS VBM Data error: " + e.getMessage());
            mCurrentTimeShiftVbmBaseData = null;
        }
        // ===============================================================

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Log.d(TAG,"sleep interrupted");
        }

        // Action 0: Play
        String[] vbmData = getTimeShiftVbmDataWithAction(0);

        // try to avoid vendor/bin/hw/android.hardware.tv.tuner-service.rtk crash by sleep
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Log.d(TAG,"sleep interrupted");
        }

        if(startRealDVRPlayback(pvrRecFileInfo, playTunerId,0,fullPath) == 1){
            Log.e(TAG,"startRealDVRPlayback fail, playTunerId="+playTunerId+" fullPath="+fullPath);
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_PLAY_ERROR, 0, 0, null);
            return FAILURE;
        }

//      mDvrPlaybackController.playerPause();

        mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_SUCCESS, 0, 0, vbmData);
        Log.d(TAG,"timeshiftStart success");
        return SUCCESS;
    }


    public void timeshiftPlayResume(int playTunerId) {
        Log.d(TAG,"timeshiftPlayResume start, playTunerId="+playTunerId);
        // Action 0: Play (Resume)
        String[] vbmData = getTimeShiftVbmDataWithAction(0);
        // 計算暫停時間 (Value_4)
        if (mTsPauseStartTime > 0 && vbmData != null) {
            long diffSeconds = (System.currentTimeMillis() - mTsPauseStartTime) / 1000;
            if (diffSeconds > 7200) diffSeconds = 7200; // Max 7200
            vbmData[4] = String.valueOf(diffSeconds);
            mTsPauseStartTime = 0; // 重置
        }
        if(mDvrPlaybackController == null){
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_RESUME_ERROR, 0, 0, null);
        }
        else {
            mDvrPlaybackController.playerResume();
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_RESUME_SUCCESS, 0, 0, vbmData);
        }
        Log.d(TAG,"timeshiftPlayResume success");

    }

    public void timeshiftPlayPause(int playTunerId) {
        Log.d(TAG,"timeshiftPlayPause start, playTunerId="+playTunerId);
        mTsPauseStartTime = System.currentTimeMillis();
        // Action 1: Pause
        String[] vbmData = getTimeShiftVbmDataWithAction(1);
        if(mDvrPlaybackController == null){
            Log.e(TAG,"mDvrPlaybackController is null");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_PAUSE_ERROR, 0, 0, null);
        }
        else {
            mDvrPlaybackController.playerPause();
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_PAUSE_SUCCESS, 0, 0, vbmData);
        }
        Log.d(TAG,"timeshiftPlayPause success");
    }

    public void timeshiftPlayFastForward(int playTunerId) {
        float speed;
        PvrInfo.EnPlaySpeed retSpeed ;
        Log.d(TAG,"timeshiftPlayFastForward start, playTunerId"+playTunerId);
        // Action 3: Fast forward
        String[] vbmData = getTimeShiftVbmDataWithAction(3);
        if(mDvrPlaybackController == null){
            Log.e(TAG,"mDvrPlaybackController is null");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_FF_ERROR, 0, 0, null);
        }
        else {
            if (mDvrPlaybackController.getVideoMediaCodec() == null) {
                Log.e(TAG, "The Record file of radio do not support this function");
                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_NOT_SUPPORT, 0, 0, null);
                return;
            }
            mDvrPlaybackController.fastForward();
            speed = mDvrPlaybackController.getPlaySpeed();
            retSpeed = convertSpeedUnits(speed);
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_FF_SUCCESS, retSpeed.getValue(), 0, vbmData);
        }
        Log.d(TAG,"timeshiftPlayFastForward success");
    }

    public void timeshiftPlayRewind(int playTunerId) {
        float speed;
        PvrInfo.EnPlaySpeed retSpeed ;
        Log.d(TAG,"timeshiftPlayRewind start, playTunerId="+playTunerId);
        // Action 4: Rewind
        String[] vbmData = getTimeShiftVbmDataWithAction(4);
        if(mDvrPlaybackController == null){
            Log.e(TAG,"mDvrPlaybackController is null");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_RW_ERROR, 0, 0, null);
        }
        else {
            if (mDvrPlaybackController.getVideoMediaCodec() == null) {
                Log.e(TAG, "The Record file of radio do not support this function");
                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_NOT_SUPPORT, 0, 0, null);
                return;
            }
            mDvrPlaybackController.backForward();
            speed = mDvrPlaybackController.getPlaySpeed();
            retSpeed = convertSpeedUnits(speed);
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_RW_SUCCESS, retSpeed.getValue(), 0, vbmData);
        }
        Log.d(TAG,"timeshiftPlayRewind success");
    }

    public void timeshiftPlaySeek(int playTunerId,int seekTime) {
        int SeekTimeSec=seekTime;
        // Action 2: Seeking
        String[] vbmData = getTimeShiftVbmDataWithAction(2);
        Log.d(TAG,"timeshiftPlaySeek start, playTunerId="+playTunerId+" seekTime="+seekTime);
        if(mDvrPlaybackController == null){
            Log.e(TAG,"mDvrPlaybackController is null");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_SEEK_ERROR, 0, 0, null);
        }
        else {
            if (mDvrPlaybackController.getVideoMediaCodec() == null) {
                Log.e(TAG, "The Record file of radio do not support this function");
                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_NOT_SUPPORT, 0, 0, null);
                return;
            }
            mDvrPlaybackController.setLastPosition(seekTime);
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_SEEK_SUCCESS, 0, 0, vbmData);
        }
        Log.d(TAG,"timeshiftPlaySeek success");
    }

    public void timeshiftStop() {
        Log.d(TAG,"timeshiftStop start");
        int recordTunerId=-1,playTunerId=-1;
        int result;

        if(mTunerIdForTimeshiftPlayback != -1){
            int tunerIdTmp=mTunerIdForTimeshiftPlayback;
            if(mDvrPlaybackController != null) {
            mDvrPlaybackController.playerStop();
                mDvrPlaybackController = null;
            }

            playbackClose();

            SystemProperties.set("persist.sys.tunerhal.source.cas.type", "WV");
            AudioManager audioManager = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                audioManager = mContext.getSystemService(AudioManager.class);
            }
            audioManager.setParameters("rtk_tunerhal_set_prop_cas_type=WV");
            mTunerInterface.requestTuner(tunerIdTmp, TunerInterface.TUNER_TYPE_LIVE);
        }
        if(mTunerIdForTimeshiftRecord != -1){
            deConfigureRecorderFilter();
            if(mDvrRecorderController != null) {
            mDvrRecorderController.stopDVRRecorder();
            mDvrRecorderController = null;
            }
            //wv2RecordStopCasPlay();
            stopWvCas2Record();
        }

        mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_STOP_SUCCESS, 0, 0, null);
        Log.d(TAG,"timeshiftStop success");
    }

    public void changeAuidoTrack(int playTunerId,AudioInfo.AudioComponent audioComponent,int recMode) {
        Log.d(TAG,"changeAuidoTrack start, playTunerId="+playTunerId);

        int index=0xff,i,j;
        Filter audioFilter;
        int audioPid,audioCodec;
        audioPid = audioComponent.getPid();
        audioCodec = audioComponent.getAudioType();
        if(mDvrPlaybackController == null){
            Log.e(TAG,"mDvrPlaybackController is null");
            return;
        }
        if(mDvrPlaybackController.getVideoMediaCodec() == null){
            Log.e(TAG,"The Record file of radio do not support this function");
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_NOT_SUPPORT, 0, 0, null);
            return ;
        }

        if(mCurrentAudioPid == audioPid || audioPid == 0x1FFF){
            if(audioPid == 0x1FFF)
                Log.d(TAG,"The value of audioPid is incorrect, audioPid="+audioPid);
            else
                Log.d(TAG,"The values of audioPid and currentAudioPid are the same, mCurrentAudioPid="+mCurrentAudioPid+" audioPid="+audioPid);
            if(recMode == PLAYBACK)
                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_CHANGE_AUDIO_TRACK_ERROR, 0, 0, null);
            else
                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_CHANGE_AUDIO_TRACK_ERROR, 0, 0, null);
            return ;
        }

        for(i=0;i<mAudioPidList.size();i++){
            if(mAudioPidList.get(i) == audioPid)
                break;
        }
        if(i >= mAudioPidList.size()){
            if(recMode == PLAYBACK)
                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_CHANGE_AUDIO_TRACK_ERROR, 0, 0, null);
            else
                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_CHANGE_AUDIO_TRACK_ERROR, 0, 0, null);
            return ;
        }

        if(mAudioTrack != null){
            mAudioTrack.stop();
            mAudioTrack.release();
        }

        for(j=0;j<mAudioPidList.size();j++){
            if(mAudioPidList.get(j) == mCurrentAudioPid) {
                mAudioFilterList.get(j).stop();
            }
        }

        Log.d(TAG,"audioPid="+audioPid+" audioCodec="+audioCodec);
        audioFilter = mAudioFilterList.get(i);
        mCurrentAudioPid=audioPid;
        startAudioTrack(audioCodec,audioFilter);
        Log.d(TAG,"mAudioTrack="+mAudioTrack);

        mDvrPlaybackController.setAudioTrack(mAudioTrack);
        if(recMode == PLAYBACK)
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_CHANGE_AUDIO_TRACK_SUCCESS, 0, 0, null);
        else
            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_CHANGE_AUDIO_TRACK_SUCCESS, 0, 0, null);

        Log.d(TAG,"changeAuidoTrack end");

    }

    private void playbackClose(){
        int i;
        if (mVideoCodec != null) {
            mVideoCodec.stop();
            mVideoCodec.setCallback(null);
            mVideoCodec.release();

            mVideoCodec = null;
        }

        if(mAudioTrack != null){
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack=null;
        }

        if(mVideoFilter != null){
            mVideoFilter.stop();
            mVideoFilter.close();
            mVideoFilter=null;
        }

        for(Filter tmp : mAudioFilterList){
            tmp.stop();
            tmp.close();
        }
        mAudioFilterList.clear();

        for(Filter tmp : mSubtitleFilterList){
            tmp.stop();
            tmp.close();
        }
        mSubtitleFilterList.clear();

        for(Filter tmp : mTeltextFilterList){
            tmp.stop();
            tmp.close();
        }

        mTeltextFilterList.clear();

        mPvrAvSyncHwId=-1;
    }

    private int startRealDVRPlayback(PvrRecFileInfo pvrRecFileInfo, int playTunerId, int lastPositionFlag, String realFullPath) {

        mDvrPlaybackController = new DvrPlaybackController(mPesiDtvCallback,mContext,playTunerId);
        mDvrPlaybackController.setFilePath(realFullPath);
        Log.d(TAG,"startRealDVRPlayback lastPositionFlag="+lastPositionFlag+" pvrRecFileInfo.getPlayStopPos()="+pvrRecFileInfo.getPlayStopPos());
        if(mRecMode == TIMESHIFT){
            Log.d(TAG,"startRealDVRPlayback mTunerIdForTimeshiftPlayback="+mTunerIdForTimeshiftPlayback);
            if (Pvcfg.isUseShareTuner())
                mDvrPlaybackController.setTuner(mTunerInterface.getMainTuner(mTunerIdForTimeshiftPlayback));
            else
            mDvrPlaybackController.setTuner(mTunerInterface.getTuner(mTunerIdForTimeshiftPlayback));
            mDvrPlaybackController.setRecordMode(DvrRecorderController.RECORDERMODE_TIMEShIFT);
        }
        else {
            if (Pvcfg.isUseShareTuner())
                mDvrPlaybackController.setTuner(mTunerInterface.getMainTuner(mTunerId));
            else
            mDvrPlaybackController.setTuner(mTunerInterface.getTuner(mTunerId));
            mDvrPlaybackController.setRecordMode(DvrRecorderController.RECORDERMODE_RECORD);
            if(mRecMode == PLAYRECORDING)
                mDvrPlaybackController.setPlayRecordingFile(true);
            else
                mDvrPlaybackController.setPlayRecordingFile(false);
        }
        mDvrPlaybackController.setTotalRecTimeSec(pvrRecFileInfo.getDurationSec());
        if(lastPositionFlag == 1){
            mDvrPlaybackController.setLastPosition(pvrRecFileInfo.getPlayStopPos());
        }
        else{
            mDvrPlaybackController.setLastPosition(0);
        }

        if(mDvrPlaybackController.startTunerTuneDo() != 0 ){
            Log.e(TAG,"mDvrPlaybackController.startTunerTuneDo() != 0");
            return 1;
        }

        startPlayMediacodec(pvrRecFileInfo);
        //mDvrPlaybackController.startDvrPlay();
        if(mRecMode == TIMESHIFT)
        mDvrPlaybackController.setTimeshiftReocrd(mDvrRecorderController);
        if(mRecMode == PLAYRECORDING)
            mDvrPlaybackController.setNormalReocrd(mDvrRecorderControllerTmp);
        mDvrPlaybackController.setVideoMediaCodec(mVideoCodec);
        mDvrPlaybackController.setAudioTrack(mAudioTrack);
        //mDvrPlaybackController.setTotalRecTimeSec(pvrRecFileInfo.getDurationSec());
        mDvrPlaybackController.startDvrPlay();
        return 0;

    }

    private void startPlayMediacodec(PvrRecFileInfo pvrRecFileInfo) {
        long channelId = pvrRecFileInfo.getChannelId();

        int videoPid = 0, videoCodec = 0, pidTmp, i, audioPid = 0, audioCodec=0;
        Filter filterTmp;
        List<Integer> audioPidList = new ArrayList<>();
        List<Integer> audioCodecList = new ArrayList<>();
        List<Integer> subtitlePidList = new ArrayList<>();
        List<Integer> teletextPidList = new ArrayList<>();

        //startWvCas2Record();
        int tunerId;
        if(mRecMode == TIMESHIFT){
            tunerId=mTunerIdForTimeshiftPlayback;
        }
        else{
            tunerId=mTunerId;
        }

        if (pvrRecFileInfo.getVideoInfo() != null) {
            videoPid = pvrRecFileInfo.getVideoInfo().getPID();
            videoCodec = pvrRecFileInfo.getVideoInfo().getCodec();
        }

        if (pvrRecFileInfo.getAudiosInfoList().size() > 0) {
            for (ProgramInfo.AudioInfo audio : pvrRecFileInfo.getAudiosInfoList()) {
                pidTmp = audio.getPid();
                if (((pidTmp != -1) && pidTmp != 0x1FFF) && (pidTmp != 0)) {
                    audioPidList.add(pidTmp);
                    audioCodecList.add(audio.getCodec());
                }
            }
            audioPid = audioPidList.get(0);
            audioCodec = pvrRecFileInfo.getAudiosInfoList().get(0).getCodec();
        }
        if (pvrRecFileInfo.getSubtitleInfo().size() > 0) {
            for (ProgramInfo.SubtitleInfo subTitle : pvrRecFileInfo.getSubtitleInfo()) {
                pidTmp = subTitle.getPid();
                subtitlePidList.add(pidTmp);
            }
        }
        if (pvrRecFileInfo.getTeletextList().size() > 0) {
            for (ProgramInfo.TeletextInfo teleText : pvrRecFileInfo.getTeletextList()) {
                pidTmp = teleText.getPid();
                teletextPidList.add(pidTmp);
            }
        }

        Log.d(TAG,"videoPid="+videoPid+", videoCodec="+videoCodec+", audioPid="+audioPid+", audioCodec="+audioCodec);

        mDvrPlaybackController.setISStartPlaying(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        mFrameRenderedListener = new MediaCodec.OnFrameRenderedListener() {
            @Override
            public void onFrameRendered(@NonNull MediaCodec codec, long presentationTimeUs, long nanoTime){
                // a customized way to consume data efficiently by using status as a hint.
                //mDvrPlaybackController.setPresentationTimeUs(presentationTimeUs);
                if(mDvrPlaybackController.getISStartPlaying() == true){
                    mDvrPlaybackController.setISStartPlaying(false);
                    mDvrPlaybackController.setPlayStartUs(presentationTimeUs);
                }
                else {
                    mDvrPlaybackController.setPlayCurrentUs(presentationTimeUs);
    //Audio pts test
    //                    AudioManager audioManager = mContext.getSystemService(AudioManager.class);
    //                    String tmp=audioManager.getParameters("get_audio_pts_by_filter_id_"+mAudioFilterList.get(0).getId());
    //                    Log.d(TAG,"Allen_test Audio Pts="+tmp+" mAudioFilterList.get(0).getId()="+mAudioFilterList.get(0).getId());

                    //if(SettingUtils.isSubtitleON())
                    //showSubtitle(presentationTimeUs);
                    //Log.d(TAG, "presentationTimeUs = "+presentationTimeUs+",  nanoTime = "+nanoTime);
                }
            }
        };
        }

        if ((videoPid != -1) && (videoPid != 0x1FFF) && (videoPid != 0)) {
            if (Pvcfg.isUseShareTuner()) {
                mVideoFilter = mTunerInterface.getMainTuner(tunerId).openFilter(Filter.TYPE_TS, Filter.SUBTYPE_VIDEO, 1024 * 1024, null, null);
                mPvrAvSyncHwId = mTunerInterface.getMainTuner(tunerId).getAvSyncHwId(mVideoFilter);
            }
            else{
            mVideoFilter = mTunerInterface.getTuner(tunerId).openFilter(Filter.TYPE_TS, Filter.SUBTYPE_VIDEO, 1024 * 1024, null, null);
            mPvrAvSyncHwId = mTunerInterface.getTuner(tunerId).getAvSyncHwId(mVideoFilter);
            }
            Log.d(TAG,"startVideoMediaCodec, mPvrAvSyncHwId="+mPvrAvSyncHwId);
            startVideoMediaCodec(videoCodec,mVideoFilter,mSurface);
            startVideoFilter(videoPid,mVideoFilter);
        }


        if (pvrRecFileInfo.getAudiosInfoList().size() > 0){
            for(i=0;i<audioPidList.size();i++) {
                audioPid = audioPidList.get(i);
                if (Pvcfg.isUseShareTuner())
                    filterTmp = mTunerInterface.getMainTuner(tunerId).openFilter(Filter.TYPE_TS, Filter.SUBTYPE_AUDIO, 1024 * 1024, null, null);
                else
                filterTmp = mTunerInterface.getTuner(tunerId).openFilter(Filter.TYPE_TS, Filter.SUBTYPE_AUDIO, 1024 * 1024, null, null);
                mAudioFilterList.add(filterTmp);
                mAudioPidList.add(audioPid);
                startAudioFilter(audioPid,filterTmp);
            }

            audioPid = -1;
            if(audioPidList.size()>0) {
                audioPid = audioPidList.get(0);
                audioCodec = audioCodecList.get(0);
            }

            if (((audioPid != -1) && audioPid != 0x1FFF) && (audioPid != 0)) {
                Filter audioFilter=mAudioFilterList.get(0);
                if(mPvrAvSyncHwId == -1) {
                    if (Pvcfg.isUseShareTuner())
                        mPvrAvSyncHwId = mTunerInterface.getMainTuner(tunerId).getAvSyncHwId(audioFilter);
                    else
                    mPvrAvSyncHwId = mTunerInterface.getTuner(tunerId).getAvSyncHwId(audioFilter);
                }
                mCurrentAudioPid=audioPid;
                startAudioTrack(audioCodec,audioFilter);
            }
            mDvrPlaybackController.setAudioFilterId(mAudioFilterList.get(0).getId());
        }
        //startCCDataFilter();
        /*
        Log.d(TAG, "startPesFilter... mSubtitleCount =" + mChannelInfos.get(mChannelID).mSubtitleCount );
        if(mChannelInfos.get(mChannelID).mSubtitleCount > 0 && SettingUtils.isSubtitleON()){
            Log.d(TAG, "startPesFilter... getSubtitlePid =" + mChannelInfos.get(mChannelID).mSubtitleDescriptors[0].getSubtitlePid());
            startPesFilter(mChannelInfos.get(mChannelID).mSubtitleDescriptors[0].getSubtitlePid());
            mDvbSubtitleCodec = new DvbSubtitleCodec();
            mDvbSubtitleCodec.setPageIds(mChannelInfos.get(mChannelID).mSubtitleDescriptors[0].getCompositionPageId()
                    ,mChannelInfos.get(mChannelID).mSubtitleDescriptors[0].getAncillaryPageId());
            mDvbSubtitleCodec.setCallback(mSubtitleCallback, mHandler);
            mDvbSubtitleCodec.start();
        }
        */
        if ((videoPid == -1) || (videoPid == 0x1FFF) || (videoPid == 0)){
            mDvrPlaybackController.setISStartPlaying(false);
        }
    }

    public void startVideoMediaCodec(int videoCode,Filter videoFilter, Surface surface) {

        String videoMediaFormatStr = MediaUtils.getVideoMediaFormatStr(videoCode);
        //LogUtils.d(TAG+"startVideoMediaCodec videoMediaFormatStr =  "+videoMediaFormatStr);
        /* Create a MediaFormat & MediaCodec instance to Handle VIDEO */
        mVideoMediaFormat = MediaFormat.createVideoFormat(videoMediaFormatStr, 1920, 1080);
        /* Set AV Sync HW ID from a Pcr Filter by call to getAvSyncHwId() API in tuner Object */
        /*If there is no audio, mAvSyncHwId should be -1, then video will be master*/
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.R){
            //LogUtils.d(TAG+"Build.VERSION.SDK_INT > Build.VERSION_CODES.R");
            mVideoMediaFormat.setInteger("audio-hw-sync", mPvrAvSyncHwId);
        }
        else{
            //LogUtils.d(TAG+"Build.VERSION.SDK_INT <= Build.VERSION_CODES.R");
            mVideoMediaFormat.setInteger(MediaFormat.KEY_HARDWARE_AV_SYNC_ID, mPvrAvSyncHwId);
        }

        mVideoMediaFormat.setInteger("feature-tunneled-playback", 1);

        if(false){//if(Build.VERSION.SDK_INT < VersionCodes.TIRAMISU || SettingUtils.isAndroidUpgrade()){
            String videoCodeName ;
            videoCodeName = MediaUtils.getVideoCodeNameSecure(videoCode);

            try {
                mVideoCodec = MediaCodec.createByCodecName(videoCodeName);
                //Log.d(TAG, "createByCodecName = " + videoCodeName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            /* Shell be set null because no need to retrieve any MediaCodec Event in passthrough mode */
            mVideoCodec.setCallback(null);

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.R){
                mVideoMediaFormat.setInteger("vendor.tis.videofilterId", videoFilter.getId());
            }
        }
        else{

            String videoCodeName = MediaUtils.getVideoCodeName(videoCode);
            String videoMimeType = MediaUtils.getVideoMediaFormatStr(videoCode);
            try {
                mVideoCodec = MediaCodec.createDecoderByType(videoMimeType);
                videoCodeName = mVideoCodec.getName();
            } catch (IOException e) {
                e.printStackTrace();
            }
            StringBuilder sb = new StringBuilder(videoCodeName);
            sb.append(".secure");
            videoCodeName = sb.toString();
            mVideoCodec.release();
            try {
                mVideoCodec = MediaCodec.createByCodecName(videoCodeName);
            } catch (IOException e) {
                e.printStackTrace();
            }
            /* Shell be set null because no need to retrieve any MediaCodec Event in passthrough mode */
            mVideoCodec.setCallback(null);

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.R){
                mVideoMediaFormat.setInteger("vendor.tis.videofilterId", videoFilter.getId());
                mVideoMediaFormat.setInteger("vendor.tis.videofilterId.value", videoFilter.getId()); //For Codec2.0
            }

        }

        /* Display the decoded video frame to below given surface by MediaCodec Object */

        mVideoCodec.configure(mVideoMediaFormat, surface, null, 0);
        /* Set video Filter ID by call to getId from VideoFilter Object */

        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.R){
            Bundle params = new Bundle();
            params.putInt("videofilterId", videoFilter.getId());

            params.putInt("avsync_mode", 1);
            mVideoCodec.setParameters(params);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        mVideoCodec.setOnFrameRenderedListener(mFrameRenderedListener, null);
        }

        mVideoCodec.start();
    }

    public void startVideoFilter(int videoPid, Filter videoFilter) {
        // mVideoFilter = mTuner.openFilter(Filter.TYPE_TS, Filter.SUBTYPE_VIDEO, 1024 * 1024, null, null);
        Settings videoSettings = AvSettings
                .builder(Filter.TYPE_TS, false)
                .setPassthrough(true)
                .build();

        FilterConfiguration videoConfig = TsFilterConfiguration
                .builder()
                .setTpid(videoPid)
                .setSettings(videoSettings)
                .build();
        videoFilter.configure(videoConfig);
        videoFilter.start();
    }

    public void startAudioFilter(int audioPid, Filter audioFilter) {

        Settings audioSettings = AvSettings
                .builder(Filter.TYPE_TS, true)
                .setPassthrough(true)
                .build();

        FilterConfiguration audioConfig = TsFilterConfiguration
                .builder()
                .setTpid(audioPid)
                .setSettings(audioSettings)
                .build();
        audioFilter.configure(audioConfig);

    }

    public void startAudioTrack(int audioCodec,Filter audioFilter) {
        try {
            int flags = AudioAttributes.FLAG_AUDIBILITY_ENFORCED | AudioAttributes.FLAG_HW_AV_SYNC | AudioAttributes.FLAG_LOW_LATENCY;
            AudioAttributes mAudioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setFlags(flags)
                    .build();
            int mEncoding=0;


            if(audioCodec == StreamType.STREAM_MPEG2_AUDIO ||audioCodec == StreamType.STREAM_MPEG1_AUDIO )
                mEncoding = AudioFormat.ENCODING_MP3;
            else if(audioCodec == StreamType.STREAM_AC3_AUDIO)
                mEncoding = AudioFormat.ENCODING_AC3;
            else if(audioCodec == StreamType.STREAM_AAC_AUDIO)
                mEncoding = AudioFormat.ENCODING_AAC_LC;
//            else if (audioCodec == MediaUtils.RTK_CODEC_AUDIO_AC4)
//                mEncoding = AudioFormat.ENCODING_AC4;
            else if (audioCodec == StreamType.STREAM_DDPLUS_AUDIO)
                mEncoding = AudioFormat.ENCODING_E_AC3;
            else if(audioCodec == StreamType.STREAM_HEAAC_AUDIO)
                mEncoding = AudioFormat.ENCODING_AAC_HE_V1;


            AudioFormat mAudioFormat = new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .setSampleRate(AUDIO_SAMPLE_RATE)
                    .setEncoding(mEncoding)
                    //.setEncoding(AudioFormat.ENCODING_AC3)
                    // .setEncoding(AudioFormat.ENCODING_AAC_LC)
                    .build();

            PrimeAudioTrack.TunerConfiguration tunerConfig = new PrimeAudioTrack.TunerConfiguration(audioFilter.getId(),
                    mPvrAvSyncHwId);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mAudioTrack = new PrimeAudioTrack.Builder()
                    .setTunerConfiguration(tunerConfig)
                    .setAudioAttributes(mAudioAttributes)
                    .setAudioFormat(mAudioFormat)
                    .setEncapsulationMode(AudioTrack.ENCAPSULATION_MODE_ELEMENTARY_STREAM)
                    .setBufferSizeInBytes(1024 * 1024)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .setSessionId(0)
                    .build();
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ) {
                AudioManager audioManager = mContext.getSystemService(AudioManager.class);
                int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                audioManager.setParameters("tunerAudioTrackVolume=" + volume);
            }

            mAudioTrack.play();
            audioFilter.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startRealDVRRecorder(PvrDbRecordInfo mDbRecordInfo, int recordTunerId,String realFullPath){
        int i,videoCodec;
        //ProgramInfo programInfo = mDatamanager.getProgramInfo(mDbRecordInfo.getChannelId());
        //int pmtPid = programInfo.getServiceId();
        //Log.d(TAG,"Pesi_Monitor_PMT Pmt Pid="+pmtPid+" Ch name="+programInfo.getDisplayName());
        configureRecorderFilter(mDbRecordInfo);
        //configureRecorderPMTFilter(pmtPid);
        //configureRecorderPATFilter(0);

        mDvrRecorderController = new DvrRecorderController(mPesiDtvCallback,mContext,mTunerId,mDbRecordInfo.getDurationSec());

        mDvrRecorderController.setTuner(mTunerInterface.getTuner(mTunerId));
        if(mRecMode == RECORD)
            mDvrRecorderController.setRecordMode(DvrRecorderController.RECORDERMODE_RECORD);
        else
            mDvrRecorderController.setRecordMode(DvrRecorderController.RECORDERMODE_TIMEShIFT);
        mDvrRecorderController.setFilePath(realFullPath);

        if(mDbRecordInfo.getVideoInfo() != null)
            videoCodec = PesiVideoCodecTransformRtkVideoCodec(mDbRecordInfo.getVideoInfo().getCodec());
        else
            videoCodec = -1;

        mDvrRecorderController.initDvrRecorder(videoCodec);

        if(mVideoFilter != null) {
            mDvrRecorderController.setAttachFilter(mVideoFilter);
        }

        if(mAudioFilterList.size() > 0) {
            for(i=0;i<mAudioFilterList.size();i++) {
                if(mAudioFilterList.get(i) != null) {
                    mDvrRecorderController.setAttachFilter(mAudioFilterList.get(i));
                }
            }
        }

        if(mSubtitleFilterList.size() > 0) {
            for(i=0;i<mSubtitleFilterList.size();i++) {
                if(mSubtitleFilterList.get(i) != null) {
                    mDvrRecorderController.setAttachFilter(mSubtitleFilterList.get(i));
                }
            }
        }
        if(mTeltextFilterList.size() > 0) {
            for(i=0;i<mTeltextFilterList.size();i++) {
                if(mTeltextFilterList.get(i) != null) {
                    mDvrRecorderController.setAttachFilter(mTeltextFilterList.get(i));
                }
            }
        }

        //mDvrRecorderController.setAttachFilter(mRecorderPATFilter);
        //mDvrRecorderController.setAttachFilter(mRecorderPMTFilter);

        mDvrRecorderController.startDvrRecorder();

        if(mVideoFilter != null) {
            mVideoFilter.start();
        }

        for(i=0;i<mAudioFilterList.size();i++) {
            if(mAudioFilterList.get(i) != null) {
                mAudioFilterList.get(i).start();
            }
        }

        for(i=0;i<mSubtitleFilterList.size();i++) {
            if(mSubtitleFilterList.get(i) != null) {
                mSubtitleFilterList.get(i).start();
            }
        }

        for(i=0;i<mTeltextFilterList.size();i++) {
            if(mTeltextFilterList.get(i) != null) {
                mTeltextFilterList.get(i).start();
            }
        }

        //mRecorderPATFilter.start();
        //mRecorderPMTFilter.start();
        //mISRecordering=true;

    }

    private void configureRecorderFilter(PvrDbRecordInfo dbRecordInfo) {

        int videoPid=0,videoCodec=0,pidTmp,i,audioPid=0,audioCodec=0;
        Filter filterTmp;
        List<Integer> audioPidList = new ArrayList<>();
        List<Integer> subtitlePidList = new ArrayList<>();
        List<Integer> teletextPidList = new ArrayList<>();

        startWvCas2Record(dbRecordInfo);
        if(dbRecordInfo.getVideoInfo().getPID() != 0) {
            videoPid = dbRecordInfo.getVideoInfo().getPID();
            videoCodec = dbRecordInfo.getVideoInfo().getCodec();
        }
        if(dbRecordInfo.getAudiosInfoList().size() > 0) {
            for (ProgramInfo.AudioInfo audio : dbRecordInfo.getAudiosInfoList()) {
                pidTmp = audio.getPid();
                audioPidList.add(pidTmp);
            }
            audioPid=audioPidList.get(0);
            audioCodec=dbRecordInfo.getAudiosInfoList().get(0).getCodec();
        }
        if(dbRecordInfo.getSubtitleInfo().size() > 0) {
            for (ProgramInfo.SubtitleInfo subTitle : dbRecordInfo.getSubtitleInfo()) {
                pidTmp = subTitle.getPid();
                subtitlePidList.add(pidTmp);
            }
        }
        if(dbRecordInfo.getTeletextList().size() > 0) {
            for (ProgramInfo.TeletextInfo teleText : dbRecordInfo.getTeletextList()) {
                pidTmp = teleText.getPid();
                teletextPidList.add(pidTmp);
            }
        }

        Log.d(TAG,"configureRecorderFilter audioPid= " + PesiAudioCodecTransformRtkAudioCodec(audioCodec));


        SystemProperties.set("persist.sys.tunerhal.source.enc",videoPid+","+audioPid+",");

        Log.d(TAG,"configureRecorderFilter video pid " + videoPid + " audioPid = "+audioPid);

        //PesiVideoCodecTransformRtkVideoCodec(videoCodec);
        if(videoPid > 0) {
            Log.d(TAG,"persist.sys.tunerhal.source.videocodec ==> AV(Video)");
        SystemProperties.set("persist.sys.tunerhal.source.videocodec",videoPid+","+PesiVideoCodecTransformRtkVideoCodec(videoCodec)+",");
        }
        else {
            Log.d(TAG,"persist.sys.tunerhal.source.videocodec ==> Radio(Audio)");
            Log.d(TAG,"configureRecorderFilter audioCodec= " + PesiAudioCodecTransformRtkAudioCodec(audioCodec));
            SystemProperties.set("persist.sys.tunerhal.source.videocodec", audioPid + "," + PesiAudioCodecTransformRtkAudioCodec(audioCodec) + ",");
        }
        //Video
        if(videoPid > 0) {
            mRecorderBaseFilterCallback = new BaseFilterCallback(BaseFilterCallback.FilterType_Record);
            mVideoFilter = mTunerInterface.getTuner(mTunerId).openFilter(Filter.TYPE_TS, Filter.SUBTYPE_RECORD, 1024 * 1024 * 10, mExecutor, mRecorderBaseFilterCallback);
            //LogUtils.d("Video openFilter Filer, Filer = "+pvrDevInfo.mVideoFilter.getId());
            RecordSettings.Builder recordSettingsBuilder = RecordSettings.builder(Filter.TYPE_TS);
            int tsIndexMask = RecordSettings.TS_INDEX_PAYLOAD_UNIT_START_INDICATOR | RecordSettings.TS_INDEX_DISCONTINUITY_INDICATOR;
            int scIndexType ;//= RecordSettings.INDEX_TYPE_SC;
            int scIndexMask ;//= SC_INDEX_I_SLICE;

            if(PesiVideoCodecTransformRtkVideoCodec(dbRecordInfo.getVideoInfo().getCodec()) == MediaUtils.RTK_CODEC_VIDEO_H265){
                scIndexType = INDEX_TYPE_SC_HEVC;
                scIndexMask =  BaseFilterCallback.SC_HEVC_INDEX_SLICE_I_MASK;
                Log.d(TAG,"h265 scIndexType: "+scIndexType+" scIndexMask: "+scIndexMask);
            }
            else{
                scIndexType = RecordSettings.INDEX_TYPE_SC;
                scIndexMask = SC_INDEX_I_SLICE;
                Log.d(TAG,"default scIndexType: "+scIndexType+" scIndexMask: "+scIndexMask);
            }

            recordSettingsBuilder.setTsIndexMask(tsIndexMask);
            recordSettingsBuilder.setScIndexType(scIndexType);
            recordSettingsBuilder.setScIndexMask(scIndexMask);
            RecordSettings recordSettings = recordSettingsBuilder.build();

            FilterConfiguration recordConfig = TsFilterConfiguration
                    .builder()
                    .setTpid(videoPid)
                    .setSettings(recordSettings)
                    .build();

            mVideoFilter.configure(recordConfig);
        }

        if(audioPidList.size()>0) {
            for (i = 0; i < audioPidList.size(); i++) {
                if(videoPid == 0 && audioPidList.size() == 1){//(mRecorderBaseFilterCallback == null) && (i == 0)) {
                    Log.d(TAG,"new BaseFilterCallback");
                    mRecorderBaseFilterCallback = new BaseFilterCallback(BaseFilterCallback.FilterType_Record);
                    Log.d(TAG,"openFilter");
                    filterTmp = mTunerInterface.getTuner(mTunerId).openFilter(Filter.TYPE_TS, Filter.SUBTYPE_RECORD, 1024 * 1024, mExecutor, mRecorderBaseFilterCallback);
                    RecordSettings.Builder recordSettingsBuilder = RecordSettings.builder(Filter.TYPE_TS);
                    int tsIndexMask = RecordSettings.TS_INDEX_PAYLOAD_UNIT_START_INDICATOR | RecordSettings.TS_INDEX_DISCONTINUITY_INDICATOR;
                    int scIndexType ;//= RecordSettings.INDEX_TYPE_SC;
                    int scIndexMask ;//= SC_INDEX_I_SLICE;

                    scIndexType = RecordSettings.INDEX_TYPE_SC;
                    scIndexMask = SC_INDEX_I_SLICE;
                    Log.d(TAG,"Audio openFilter Filer("+i+"), Filer = "+filterTmp.getId());
                    Log.d(TAG,"default scIndexType: "+scIndexType+" scIndexMask: "+scIndexMask);


                    recordSettingsBuilder.setTsIndexMask(tsIndexMask);
                    recordSettingsBuilder.setScIndexType(scIndexType);
                    recordSettingsBuilder.setScIndexMask(scIndexMask);
                    RecordSettings recordSettings = recordSettingsBuilder.build();
                    Log.d(TAG,"Set record config");
                    FilterConfiguration recordConfig = TsFilterConfiguration
                            .builder()
                            .setTpid(audioPidList.get(0))
                            .setSettings(recordSettings)
                            .build();
                    filterTmp.configure(recordConfig);
                    mAudioFilterList.add(filterTmp);
                }
                else {
                filterTmp = mTunerInterface.getTuner(mTunerId).openFilter(Filter.TYPE_TS, Filter.SUBTYPE_RECORD, 1024 * 1024, null, null);
                Log.d(TAG,"Audio openFilter Filer("+i+"), Filer = "+filterTmp.getId());
                Settings recordSettings = RecordSettings
                        .builder(Filter.TYPE_TS)
                        .build();

                FilterConfiguration recordConfig = TsFilterConfiguration
                        .builder()
                        .setTpid(audioPidList.get(i))
                        .setSettings(recordSettings)
                        .build();
                filterTmp.configure(recordConfig);
                mAudioFilterList.add(filterTmp);
            }
        }
        }

        if(subtitlePidList.size()>0) {
            for (i = 0; i < subtitlePidList.size(); i++) {
                filterTmp = mTunerInterface.getTuner(mTunerId).openFilter(Filter.TYPE_TS, Filter.SUBTYPE_RECORD, 1024 * 1024, null, null);
                Settings recordSettings = RecordSettings
                        .builder(Filter.TYPE_TS)
                        .build();

                FilterConfiguration recordConfig = TsFilterConfiguration
                        .builder()
                        .setTpid(subtitlePidList.get(i))
                        .setSettings(recordSettings)
                        .build();
                filterTmp.configure(recordConfig);
                mSubtitleFilterList.add(filterTmp);
            }
        }

        if(teletextPidList.size()>0) {
            for (i = 0; i < teletextPidList.size(); i++) {
                filterTmp = mTunerInterface.getTuner(mTunerId).openFilter(Filter.TYPE_TS, Filter.SUBTYPE_RECORD, 1024 * 1024, null, null);
                Settings recordSettings = RecordSettings
                        .builder(Filter.TYPE_TS)
                        .build();

                FilterConfiguration recordConfig = TsFilterConfiguration
                        .builder()
                        .setTpid(teletextPidList.get(i))
                        .setSettings(recordSettings)
                        .build();
                filterTmp.configure(recordConfig);
                mTeltextFilterList.add(filterTmp);
            }
        }
    }
/*
    public void configureRecorderPATFilter(int patPid) {
        mRecorderPATFilter = mTunerInterface.getTuner(mTunerId).openFilter(Filter.TYPE_TS, Filter.SUBTYPE_RECORD, 1024 * 1024, null, null);
        LogUtils.d("666888555 configureRecorderPATFilter pat pid " + patPid);
        Settings recordSettings = RecordSettings
                .builder(Filter.TYPE_TS)
                .build();

        FilterConfiguration recordConfig = TsFilterConfiguration
                .builder()
                .setTpid(patPid)
                .setSettings(recordSettings)
                .build();
        mRecorderPATFilter.configure(recordConfig);
        LogUtils.d("666888555 configureRecorderPATFilter  patPid = "+patPid);
    }

    public void configureRecorderPMTFilter(int pmtPid) {
        mRecorderPMTFilter = mTunerInterface.getTuner(mTunerId).openFilter(Filter.TYPE_TS, Filter.SUBTYPE_RECORD, 1024 * 1024, null, null);
        LogUtils.d("666888555 configureRecorderPMTFilter pmt pid " + pmtPid);
        Settings recordSettings = RecordSettings
                .builder(Filter.TYPE_TS)
                .build();

        FilterConfiguration recordConfig = TsFilterConfiguration
                .builder()
                .setTpid(pmtPid)
                .setSettings(recordSettings)
                .build();
        mRecorderPMTFilter.configure(recordConfig);
        LogUtils.d("666888555 configureRecorderPMTFilter  pmtPid = "+pmtPid);
    }
*/
    private void deConfigureRecorderFilter(){
        int i;
        Filter filterTmp;

        List<Filter> filterList=mDvrRecorderController.getAllFilterInfo();

        if (mRecorderBaseFilterCallback != null){
            mRecorderBaseFilterCallback.releaseWriteThread();
            mRecorderBaseFilterCallback = null;
            Log.d(TAG, "mRecorderBaseFilterCallback set null");
        }

        if(mRecMode == TIMESHIFT) {
            for (i = filterList.size() - 1; i >= 0; i--) {
                filterTmp = filterList.get(i);
                mDvrRecorderController.setdetachFilter(filterTmp);
                filterTmp.stop();
                filterTmp.close();
            }
        }
        else {
            if (mVideoFilter != null) {
                mDvrRecorderController.setdetachFilter(mVideoFilter);
            }
            if (mAudioFilterList.size() > 0) {
                for (i = 0; i < mAudioFilterList.size(); i++) {
                    filterTmp = mAudioFilterList.get(i);
                    mDvrRecorderController.setdetachFilter(filterTmp);
                }
            }
            if (mSubtitleFilterList.size() > 0) {
                for (i = 0; i < mSubtitleFilterList.size(); i++) {
                    filterTmp = mSubtitleFilterList.get(i);
                    mDvrRecorderController.setdetachFilter(filterTmp);
                }
            }
            if (mTeltextFilterList.size() > 0) {
                for (i = 0; i < mTeltextFilterList.size(); i++) {
                    filterTmp = mTeltextFilterList.get(i);
                    mDvrRecorderController.setdetachFilter(filterTmp);
                }
            }
            /*
            if(mRecorderPMTFilter != null)
                mDvrRecorderController.setdetachFilter(mRecorderPMTFilter);
            if(mRecorderPATFilter != null)
                mDvrRecorderController.setdetachFilter(mRecorderPATFilter);
             */
            //wv2RecordStopCasFilter();

            if (mVideoFilter != null) {
                mVideoFilter.stop();
                mVideoFilter.close();
                mVideoFilter = null;
            }

            if (mAudioFilterList.size() > 0) {
                for (i = mAudioFilterList.size() - 1; i >= 0; i--) {
                    filterTmp = mAudioFilterList.get(i);
                    filterTmp.stop();
                    filterTmp.close();
                }
                mAudioFilterList.clear();
            }

            if (mSubtitleFilterList.size() > 0) {
                for (i = mSubtitleFilterList.size() - 1; i >= 0; i--) {
                    filterTmp = mSubtitleFilterList.get(i);
                    filterTmp.stop();
                    filterTmp.close();
                }
                mSubtitleFilterList.clear();
            }

            if (mTeltextFilterList.size() > 0) {
                for (i = mTeltextFilterList.size() - 1; i >= 0; i--) {
                    filterTmp = mTeltextFilterList.get(i);
                    filterTmp.stop();
                    filterTmp.close();
                }
                mTeltextFilterList.clear();
            }
        }
/*
        if(mRecorderPATFilter != null){
            mRecorderPATFilter.stop();
            mRecorderPATFilter.close();
            mRecorderPATFilter=null;
        }
        if(mRecorderPMTFilter != null){
            mRecorderPMTFilter.stop();
            mRecorderPMTFilter.close();
            mRecorderPMTFilter=null;
        }
*/
    }

    private boolean  startRecorderTunerTune(long channelId) {

        ProgramInfo programInfo = mDatamanager.get_service_by_channelId(channelId);
        if(programInfo == null) {
            Log.e(TAG,"programInfo is null");
            return false;
        }

        TpInfo tp = mDatamanager.getTpInfo(programInfo.getTpId());

        if(tp == null) {
            Log.e(TAG,"Tp is null");
            return false;
        }
        boolean lock = mTunerInterface.tune(mTunerId, tp);
        //LogUtils.d("tunerInterface.tune tuner_id = " + tunerId + " tp = " + tp.ToString() + " lock = " + lock);

        if (mTunerInterface.isLock(mTunerId) == false) {
            Log.e(TAG,"Tuner NO lock");
            return false;
        }
        return true;
    }

    PvrInfo.EnPlaySpeed convertSpeedUnits(float speed){
        PvrInfo.EnPlaySpeed retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FAILD;
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
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_ZERO;
        }
        else {
            retSpeed = PvrInfo.EnPlaySpeed.PLAY_SPEED_ID_FAILD;
        }
        return retSpeed;
    }

    private int PesiVideoCodecTransformRtkVideoCodec(int codec){

        switch (codec) {
            case StreamType.STREAM_MPEG1_VIDEO: // 0x01
                return MediaUtils.RTK_CODEC_VIDEO_MPEG1;
            case StreamType.STREAM_MPEG2_VIDEO: //0x02
                return MediaUtils.RTK_CODEC_VIDEO_MPEG2;
            case StreamType.STREAM_MPEG4_VIDEO:
                return MediaUtils.RTK_CODEC_VIDEO_MPEG4_PART2;
            case StreamType.STREAM_MPEG4_H264_VIDEO:
                return MediaUtils.RTK_CODEC_VIDEO_H264;
            case StreamType.STREAM_HEVC_VIDEO:
                return MediaUtils.RTK_CODEC_VIDEO_H265;
            default:
                return MediaUtils.RTK_CODEC_VIDEO_H264;
        }
    }

    private int PesiAudioCodecTransformRtkAudioCodec(int codec){

        switch (codec) {
            case StreamType.STREAM_MPEG1_AUDIO: // 0x01
                return MediaUtils.RTK_CODEC_AUDIO_MPEG1;
            case StreamType.STREAM_MPEG2_AUDIO: //0x02
                return MediaUtils.RTK_CODEC_AUDIO_MPEG2;
            case StreamType.STREAM_AC3_AUDIO:
                return MediaUtils.RTK_CODEC_AUDIO_AC3;
            case StreamType.STREAM_DTS_AUDIO:
                return MediaUtils.RTK_CODEC_AUDIO_DTS;
            case StreamType.STREAM_AAC_AUDIO:
                return MediaUtils.RTK_CODEC_AUDIO_AAC;
            case StreamType.STREAM_HEAAC_AUDIO:
                return MediaUtils.RTK_CODEC_AUDIO_AACPLUS;
            case StreamType.STREAM_DDPLUS_AUDIO:
                return MediaUtils.RTK_CODEC_AUDIO_EAC3;
            default:
                return MediaUtils.RTK_CODEC_AUDIO_MPEG1;
        }
    }

    private void startWvCas2Record(PvrDbRecordInfo dbRecordInfo){

        CasSession casSession = null;
        int i;
        List<ProgramInfo.AudioInfo> audioList = dbRecordInfo.getAudiosInfoList();
        int videoCaSystemId=0;
        int audioCaSystemId=0;
        int caSystemId=0;
        int ecmPid=0;
        byte[] privateData = new byte[0];

        videoCaSystemId = findSupportedCaSystemId(dbRecordInfo.getVideoInfo().CaInfoList);
        if(videoCaSystemId == 0) {
            for (ProgramInfo.AudioInfo tmp : audioList) {
                if (findSupportedCaSystemId(tmp.CaInfoList) != 0) {
                    audioCaSystemId = findSupportedCaSystemId(tmp.CaInfoList);
                    caSystemId = audioCaSystemId;
                    ecmPid = tmp.getEcmPid(audioCaSystemId);
                    privateData = tmp.getPrivateData(caSystemId);
                    break;
                }
            }
        }
        if(videoCaSystemId > 0) {
            caSystemId = videoCaSystemId;
            ecmPid = dbRecordInfo.getVideoInfo().getEcmPid(videoCaSystemId);
            privateData = dbRecordInfo.getVideoInfo().getPrivateData(caSystemId);
        }


        Log.d(TAG,"videoCaSystemId="+videoCaSystemId+" audioCaSystemId="+audioCaSystemId);
        Log.d(TAG,"caSystemId="+caSystemId+" ecmPid="+ecmPid+" tunerId="+mTunerId);

        if (caSystemId != 0) {
            MediaCas.PluginDescriptor[] plugins = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            plugins = MediaCas.enumeratePlugins();
            }
            for (MediaCas.PluginDescriptor p : plugins) {
                Log.d(TAG,"MediaCas plugin available : " + p);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!MediaCas.isSystemIdSupported(caSystemId)) {
                Log.d(TAG,"MediaCas CaSystemId not supported, " + Integer.toHexString(caSystemId));
                return;
            }
            }

            casSession = new CasSession(mTunerInterface.getTuner(mTunerId), caSystemId, ecmPid);
            casSession.setPrivateData(privateData);
            if(dbRecordInfo.getVideoInfo().getPID() > 0) {
            casSession.addPid(dbRecordInfo.getVideoInfo().getPID());
                Log.d(TAG,"casSession.addPid(video)="+dbRecordInfo.getVideoInfo().getPID());
            }
            for (ProgramInfo.AudioInfo tmp : audioList) {
                if(findSupportedCaSystemId(tmp.CaInfoList) == caSystemId) {
                    Log.d(TAG,"casSession.addPid(audio)="+tmp.getPid());
                    casSession.addPid(tmp.getPid());
                }
            }
            casSession.open();
            mCasSession=casSession;
        }
    }

    private int findSupportedCaSystemId(List<ProgramInfo.CaInfo> caInfoList) {
        int supportedCaSystemId = 0;
        for (ProgramInfo.CaInfo caInfo : caInfoList) {
            int caSystemId = caInfo.getCaSystemId();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (MediaCas.isSystemIdSupported(caSystemId)) {
                // find first supported
                supportedCaSystemId = caSystemId;
                break;
            }
        }
        }

        return supportedCaSystemId;
    }

    private void stopWvCas2Record(){
        if(mCasSession != null){
            mCasSession.close();
            mCasSession = null;
        }
    }

    private void SetCasTypeToMV(int tunerId){
        SystemProperties.set("persist.sys.tunerhal.source.cas.type", "WV");
        AudioManager audioManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            audioManager = mContext.getSystemService(AudioManager.class);
        }
        audioManager.setParameters("rtk_tunerhal_set_prop_cas_type=WV");
        mTunerInterface.requestTuner(tunerId, TunerInterface.TUNER_TYPE_LIVE);
    }

    protected final FilterCallback mFilterCallback = new BaseFilterCallback();
    class BaseFilterCallback implements FilterCallback {
        private static final int FilterType_Normail = 0;
        private static final int FilterType_CCData = 1;
        private static final int FilterType_Subtitle = 2;
        private static final int FilterType_Record = 3;
        public static final int SC_INDEX_FRAME_MASK = 0b1111;
        public static final int SC_INDEX_SLICE_MASK = 0b11111 << 4;
        public static final int SC_HEVC_INDEX_SLICE_I_MASK = 0b111111 << 2;

        private HandlerThread mWriteThread;
        private Handler mWriteHandler;

        protected int filterType = 0;
        protected long Ibase = -1;
        protected long ISize = 0;
        protected long IPts = 0;
        protected long mLastIPts = 0;
        protected long mDiffPts = 0;
        protected long mNowPts = 0;
        protected long PreviousDataLength = -1;

        public BaseFilterCallback(int filterType){
            this.filterType = filterType;
            if(FilterType_Record == filterType)
                initWriteThread();
        }
        public BaseFilterCallback(){
            this.filterType =FilterType_Normail;
        }
        @Override
        public void onFilterEvent(@NonNull Filter filter, @NonNull FilterEvent[] events) {
            try {
                //LogUtils.d(TAG+"onFilterEvent, events.length = "+events.length);
                ArrayList<TsRecordEvent> TsRecordEvents = new ArrayList<>();
                for (FilterEvent event : events) {
                    if (event instanceof TsRecordEvent) {
                        TsRecordEvent recordEvent = (TsRecordEvent) event;
                        TsRecordEvents.add(recordEvent);
                    }
                }
                if(TsRecordEvents.size()>0){
                    if (DEBUG) Log.d(TAG,"TsRecordEvents.size()="+TsRecordEvents.size());
                    asyncWrite(TsRecordEvents);
                    /*
                    for(TsRecordEvent tsRecordEvent : TsRecordEvents){
                        try{
                            dump(tsRecordEvent);
                        }
                        catch(Exception e){
                            Log.e(TAG,"mediaevent error " + e.getMessage());
                        }
                    }
                    TsRecordEvents.clear();
                    */
                }
            }
            catch (IllegalStateException e) {
                Log.e(TAG,"errors on handling filter: "+ e);
            }
        }

        @Override
        public void onFilterStatusChanged(@NonNull Filter filter, int status) {
            Log.d(TAG,"onFilterStatusChanged, status = "+status);
        }

        private void dump(TsRecordEvent recordEvent) {
            int iFlag = 0;
            iFlag = recordEvent.getScIndexMask()&SC_INDEX_I_FRAME;

            if(DEBUG)  Log.d(TAG,"RECORD len:" + recordEvent.getDataLength() +
             ", ts index: " + recordEvent.getTsIndexMask() +
             ", sc index: " + recordEvent.getScIndexMask() +
             ", pts: " + recordEvent.getPts() + "," + ptsToUs(recordEvent.getPts())/1000 +
             ",IFlag: "+iFlag);

            if(PreviousDataLength > 0){
                if(PreviousDataLength > recordEvent.getDataLength()){
                    Log.d(TAG,"Worng: PreviousDataLength="+PreviousDataLength+" recordEvent.getDataLength()="+recordEvent.getDataLength());
                }
            }
            PreviousDataLength=recordEvent.getDataLength();

            if(iFlag != 0){
                if(Ibase == -1){
                    Ibase = recordEvent.getDataLength();
                    mNowPts = ptsToUs(recordEvent.getPts());//us
                    if(mNowPts > mLastIPts&&(mLastIPts==0||(mNowPts - mLastIPts)<4*1000*1000)){ //MAX 4s
                        mDiffPts = mNowPts - mLastIPts;
                    }else{
                        if (DEBUG) Log.d(TAG,"mNowPts < LastIPts "+mNowPts+" : "+mLastIPts+" mDiffPts: "+mDiffPts);
                    }
                    IPts += mDiffPts;
                    if (DEBUG) Log.d(TAG,"Ibase="+Ibase+" mNowPts="+mNowPts+" IPts="+IPts);
                }
                else{
                    //Continuous I-frames
                    ISize = recordEvent.getDataLength()-Ibase;
                    mLastIPts = mNowPts;
                    if (DEBUG) Log.d(TAG,"ISize="+ISize+" mLastIPts="+mLastIPts);
                    FileInfoWrite(Ibase,IPts,ISize);
                    Ibase = recordEvent.getDataLength();
                    mNowPts = ptsToUs(recordEvent.getPts());//us
                    if(mNowPts > mLastIPts&&(mLastIPts==0||(mNowPts - mLastIPts)<4*1000*1000)){//MAX 4s
                        mDiffPts = mNowPts - mLastIPts;
                    }else{
                        if (DEBUG) Log.d(TAG,"mNowPts < LastIPts "+mNowPts+" : "+mLastIPts+" mDiffPts: "+mDiffPts);
                    }
                    IPts += mDiffPts;
                    ISize = 0;
                    if (DEBUG) Log.d(TAG,"Ibase="+Ibase+" mNowPts="+mNowPts+" IPts="+IPts);
                }
                //LogUtils.d( "I-Frame Ibase: "+Ibase+" TimeShiftrecordingFileSize: "+mDvrRecorderController.getTimeShiftrecordingFileSize());
            }
            else{
                if(Ibase != -1){
                    // IPts = ptsToUs(recordEvent.getPts());
                    ISize = recordEvent.getDataLength()-Ibase;
                }
                //LogUtils.d( "b-Frame");
            }

            if((ISize != 0) && (IPts != 0) && (Ibase != -1)){
                mLastIPts = mNowPts;
                if (DEBUG) Log.d(TAG,"ISize="+ISize+" mLastIPts="+mLastIPts);
                FileInfoWrite(Ibase,IPts,ISize);
                // LogUtils.d( "Ibase:" + Ibase + "I-Pts:" +IPts + "I-Frame size "+ISize);
                ISize = 0;
                Ibase = -1;
            }
            // LogUtils.d( "end of dump\n");
        }

        public  long ptsToUs(long pts) {
            return (pts * 1000000L) / 90000;
        }

        public void initWriteThread() {
            mWriteThread = new HandlerThread(getClass().getName());
            mWriteThread.start();
            mWriteHandler = new Handler(mWriteThread.getLooper());
            Log.d(TAG, "initWriteThread: "+mWriteThread);
        }
        public void asyncWrite(ArrayList<TsRecordEvent> TsRecordEvents) {
            if(DEBUG) {
            for(TsRecordEvent tsRecordEvent : TsRecordEvents){
                try{
                        Log.d(TAG, "RECORD len:" + tsRecordEvent.getDataLength() +
                            ", pts: " + ptsToUs(tsRecordEvent.getPts())/1000 +
                            ",IFlag: "+(tsRecordEvent.getScIndexMask()&SC_INDEX_I_FRAME));
                }catch(Exception e){
                    Log.e(TAG,"mediaevent error " + e.getMessage());
                }
            }
            }

            if (mWriteHandler == null) return;
// 將這個副本的地址傳給非同步任務
            final ArrayList<TsRecordEvent> eventsToWrite = new ArrayList<>(TsRecordEvents);
            mWriteHandler.post(() -> {
                if (DEBUG) Log.d(TAG,"TsRecordEvents size" + eventsToWrite.size());
                try {
                    for(TsRecordEvent tsRecordEvent : eventsToWrite){
                        try{
                            dump(tsRecordEvent);
                        }catch(Exception e){
                            if(DEBUG) Log.e(TAG,"mediaevent error " + e.getMessage());
                        }
                    }
                    eventsToWrite.clear();
                } catch (Exception e) {
                    Log.e(TAG, "Write error", e);
                }
            });


//            mWriteHandler.post(() -> {
//                LogUtils.d("TsRecordEvents size" + TsRecordEvents.size());
//                try {
//                    for(TsRecordEvent tsRecordEvent : TsRecordEvents){
//                        try{
//                            dump(tsRecordEvent);
//                        }catch(Exception e){
//                            LogUtils.e("mediaevent error " + e.getMessage());
//                        }
//                    }
//                    TsRecordEvents.clear();
//                } catch (Exception e) {
//                    Log.e(TAG, "Write error", e);
//                }
//            });
        }
        public void releaseWriteThread() {
            if (mWriteThread != null) {
                if(DEBUG) Log.d(TAG, "quitSafely "+mWriteThread);
                mWriteThread.quitSafely();
                mWriteHandler = null;
                mWriteThread = null;
            }
        }

        public void  FileInfoWrite(long Ibase ,long IPts,long ISize){
                try{
                if(mDvrRecorderController != null)
                    mDvrRecorderController.checkWriterFileInfo(Ibase,IPts,ISize);
            }catch (Exception e){
                Log.d(TAG, " FileInfoWrite fail ");
                e.printStackTrace();
            }
        }
    };

    public PvrRecIdx getPvrRecIdx() {
        return mPvrRecIdx;
    }
}
