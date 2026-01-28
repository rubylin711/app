package com.prime.dtv.service.Dvr;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.PlaybackParams;
import android.media.tv.tuner.Tuner;
import android.media.tv.tuner.dvr.DvrPlayback;
import android.media.tv.tuner.dvr.DvrSettings;
import android.media.tv.tuner.dvr.OnPlaybackStatusChangedListener;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.SystemProperties;
import android.util.Log;

import com.prime.datastructure.sysdata.DTVMessage;
import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.Interface.PesiDtvFrameworkInterfaceCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DvrPlaybackController {
    private static final String TAG = "DVR DvrPlaybackController";
    private static final int OFFSET_TOLERANCE = (188*100);
    private static final boolean USE_ACCURATE_DATA=true;
    private static final int TS_PACKET_SIZE = 188;
    //String usbRootPath="/storage/7F13-9710/DMG";
    private PesiDtvFrameworkInterfaceCallback mPesiDtvCallback = null;

    //private DvrPlayback mDvrPlayback = null;
    private long bufferSize;
    private OnPlaybackStatusChangedListener mOnPlaybackStatusChangedListener = null;

    private Tuner mTuner = null;
    private Context mContext = null;

    private String originalFilePath = "";
    private String filePath = "";
    private String fileInfoPath = filePath+DvrRecordInfo.FileInfoExtension;

    private DvrPlayback mDvrPlayback = null;
    private MediaCodec mVideoMediaCodec = null;
    private AudioTrack mAudioTrack = null;
    private int mAudioFilterId = -1;


    private long readSize = 314524; //188*n
    private volatile boolean dvrStop = true;
    Thread readThread = null;

    private long mFilelength=0;
    private long mTotalReadSize=0;

    private boolean mIsPlayingRecording = false;

    private static int mSleepTime=10;
    private boolean mIsPause = false;
    private float speed = 1;
    private float ignore = 0;
    private static final int fixedIntervals =200;
    private byte[] dvr_buff= new byte[1024*1024*5];
    private int SeekNumber =0;
    private long estimatedTimeFileSize =0;
    private long estimatedTimeFileMs   =0;
    private File dvrFile =null;
    private RandomAccessFile InputStream =null;
    private int  mCurrentFileIndex = 0;
    private long mSeekMs = 0;
    private static final int THREAD_COUNT = 1;

    protected ExecutorService mExecutor;
    private volatile long mPresentationTimeUs = 0;
    protected boolean mISStartPlaying = false;
    private long mLastPresentationTimeUs = 0;
    private long mNowPresentationTimeUs = 0;
    private long mDiffpresentationTimeUs = 0;
    private long mSeekTimeUs = 0;
    private long mPtsFlagUs = 0;
    public final Object mLockPts = new Object();

    private int mLastPosition=0;
    private int mTunerId;
    private volatile long mPlayStartUs = 0;
    private volatile long mPlayCurrentUs = 0;
    private volatile long mScanCurrentUs = 0;
    private int mRecordMode = DvrRecorderController.RECORDERMODE_RECORD;
    private int mTotalRecTimeSec=0;
    private long mStartPlayTimestamp = 0;
    private int seekFlag;
    private int mStepForward;
    private int mCheckCount;
    public volatile long mTimeshiftPresentationTimeUs = 0;
    public volatile long mTimeshiftStartTimeUs = 0;
    public volatile long mTimeshiftOriginalStartTimeUs = 0;
//    private int mWrapAroundCnt = 0;
//    private int mWrapAroundMax = 0;
//    private volatile int mDiscardpFlag  = 0;
//    private long mPrePlayUs = 0;

    private Lock playbackOptionLock = new ReentrantLock();

    private DvrRecorderController mTimeshiftReocrd=null;
    private DvrRecorderController mNormalReocrd=null;

    private boolean DEBUG;
    private static final String PERSIST_PVR_DEBUG = "persist.sys.prime.pvr.debug";

    public DvrPlaybackController(PesiDtvFrameworkInterfaceCallback callback,Context context,int tunerId) {
        mIsPause =false;
        //mCurrentFileIndex = 0;
        mPesiDtvCallback = callback;
        mTunerId = tunerId;
        mExecutor = Executors.newFixedThreadPool(THREAD_COUNT);
        mContext = context;
        mPlayStartUs = 0;
        mPlayCurrentUs = 0;
        //mDiscardpFlag=0;
        mStepForward = 0;
        DEBUG = SystemProperties.getBoolean(PERSIST_PVR_DEBUG, false);
    }

    public void setPlayRecordingFile(boolean isPlayingRecording){
        mIsPlayingRecording = isPlayingRecording;
    }

    public void setNormalReocrd(DvrRecorderController dvrRecorderController){
        mNormalReocrd = dvrRecorderController;
    }
//    public int getWrapAroundMax(){
//        return mWrapAroundMax;
//    }
//
//    public int getWrapAroundCnt(){
//        return mWrapAroundCnt;
//    }

    public void setAudioFilterId(int audioFilterId) {
        mAudioFilterId = audioFilterId;
    }

    public void setTimeshiftReocrd(DvrRecorderController dvrRecorderController){
        mTimeshiftReocrd = dvrRecorderController;
    }

    public void setPresentationTimeUs(long presentationTimeUs){
        if(speed > 0 && speed <= 2) {
            mPresentationTimeUs = presentationTimeUs;
            mCheckCount++;
            if (DEBUG)
            Log.d(TAG, "presentationTimeUs="+presentationTimeUs+" mPresentationTimeUs = " + mPresentationTimeUs);
        }
    }

    public long getPresentationTimeUs(){
        return mPresentationTimeUs;
    }

    public long getTimeshiftPresentationTimeUs(){
        return mTimeshiftPresentationTimeUs;
    }

    public long getTimeshiftStartTimeUs(){
        return mTimeshiftStartTimeUs;
    }

    public boolean checkPlayIsPause(){
        return mIsPause;
    }

    public float getPlaySpeed(){
        return speed;
    }

    public void setPlayStartUs(long playStartUs){
        //LogUtils.d("setPlayStartUs playStartUs="+playStartUs+" mPlayStartUs="+mPlayStartUs);
        if(mPlayStartUs == 0) {
            mCheckCount=0;
            mPlayStartUs = playStartUs;
            mStartPlayTimestamp = DvrRecorderController.gettime();
            setPresentationTimeUs(playStartUs);
        }
    }

    public void checkSeekIsFinishNew() {
        if(seekFlag == 0) {
            mSeekTimeUs = 0;
            synchronized (mLockPts) {
                mPlayCurrentUs = mPtsFlagUs;
                if (mRecordMode == DvrRecorderController.RECORDERMODE_TIMEShIFT)
                    mTimeshiftPresentationTimeUs = mPtsFlagUs;
            }
            if (DEBUG)
                Log.d(TAG, "checkSeekIsFinish mPtsFlagUs=" + mPtsFlagUs + " mPlayCurrentUs=" + mPlayCurrentUs);
        }
    }

//    public void checkSeekIsFinish(long playCurrentUs) {
//        long maxDiff = 4 * 1000 * 1000;
//        long tmpDiffTime;
//        int dontAdd = 0;
//        if (DEBUG)
//            Log.d(TAG, "checkSeekIsFinish mPtsFlagUs=" + mPtsFlagUs + " playCurrentUs=" + playCurrentUs);
//        mNowPresentationTimeUs = playCurrentUs;
//        tmpDiffTime = (mNowPresentationTimeUs - mLastPresentationTimeUs);
//        if (DEBUG)
//            Log.d(TAG, "checkSeekIsFinish mNowPresentationTimeUs=" + mNowPresentationTimeUs + " mLastPresentationTimeUs=" + mLastPresentationTimeUs);
//        if (DEBUG)
//            Log.d(TAG, "checkSeekIsFinish tmpDiffTime=" + tmpDiffTime + " mDiffpresentationTimeUs=" + mDiffpresentationTimeUs);
//        if (mNowPresentationTimeUs != mLastPresentationTimeUs && (mLastPresentationTimeUs == 0 || Math.abs(tmpDiffTime) < maxDiff)) { //MAX 4s
//            mDiffpresentationTimeUs = mNowPresentationTimeUs - mLastPresentationTimeUs;
//        }
//        if (DEBUG)
//            Log.d(TAG, "checkSeekIsFinish mDiffpresentationTimeUs=" + mDiffpresentationTimeUs);
//        if (Math.abs(tmpDiffTime) > maxDiff) {
//            if ((Math.abs(tmpDiffTime) > (PTS_MAX / 2)) && (mLastPresentationTimeUs != 0))
//                dontAdd = 1;  //表示回頭
//            else
//                dontAdd = 2;  //目標到了
//        }
//        if (DEBUG) Log.d(TAG, "checkSeekIsFinish dontAdd=" + dontAdd);
//        if (dontAdd == 0) {
//            synchronized (mLockPts) {
//                mPlayCurrentUs += mDiffpresentationTimeUs;
//            }
//            mSeekTimeUs -= mDiffpresentationTimeUs;
//        }
//        if (DEBUG) Log.d(TAG, "checkSeekIsFinish mSeekTimeUs=" + mSeekTimeUs);
//        if (Math.abs(mSeekTimeUs) <= 2 || dontAdd == 2) {
//            mSeekTimeUs = 0;
//            synchronized (mLockPts) {
//                mPlayCurrentUs = mPtsFlagUs;
//            }
//            mPtsFlagUs = 0;
//            if (DEBUG) Log.d(TAG, "checkSeekIsFinish finish, mPtsFlagUs=" + mPtsFlagUs);
//            if (DEBUG) Log.d(TAG, "checkSeekIsFinish finish, mPlayCurrentUs=" + mPlayCurrentUs);
//        }
//        mLastPresentationTimeUs = mNowPresentationTimeUs;
//        setPresentationTimeUs(mPlayCurrentUs);
//    }

    public void setPlayCurrentUs(long playCurrentUs){
        long maxDiff = 4 * 1000 * 1000;
        long tmpDiffTime;
        int dontAdd = 0;

        if(speed < 0 || speed > 2) {
            ignore = 1;
            return;
        }
        else{
            if(ignore == 1){
                ignore=0;
                return;
            }
        }

        if (mStepForward != 0) {
            if (DEBUG)
                Log.d(TAG, "mStepForward=" + mStepForward + " playCurrentUs=" + playCurrentUs + " speed=" + speed);
        }

        if (mSeekTimeUs != 0 && mStepForward == 0) {
            if (DEBUG) Log.d(TAG, "setPlayCurrentUs mSeekTimeUs=" + mSeekTimeUs + " mStepForward=" + mStepForward);
            checkSeekIsFinishNew();
            } else {
            if (playCurrentUs != 0 && mStepForward == 0) {
                mNowPresentationTimeUs = playCurrentUs;
                if (DEBUG) Log.d(TAG, "setPlayCurrentUs playCurrentUs="+playCurrentUs+" speed="+speed);
                tmpDiffTime = (mNowPresentationTimeUs - mLastPresentationTimeUs);
                if (DEBUG)
                    Log.d(TAG, "setPlayCurrentUs mNowPresentationTimeUs=" + mNowPresentationTimeUs + " mLastPresentationTimeUs=" + mLastPresentationTimeUs);
                if (DEBUG)
                    Log.d(TAG, "setPlayCurrentUs tmpDiffTime=" + tmpDiffTime + " maxDiff=" + maxDiff);
                if ((mNowPresentationTimeUs != mLastPresentationTimeUs) && (mLastPresentationTimeUs == 0 || Math.abs(tmpDiffTime) < maxDiff)) { //MAX 4s
                    mDiffpresentationTimeUs = mNowPresentationTimeUs - mLastPresentationTimeUs;
                    if (DEBUG)
                        Log.d(TAG, "setPlayCurrentUs Update mDiffpresentationTimeUs=" + mDiffpresentationTimeUs);
                }
                if (DEBUG)
                    Log.d(TAG, "setPlayCurrentUs mDiffpresentationTimeUs=" + mDiffpresentationTimeUs);
                if (mDiffpresentationTimeUs > maxDiff && tmpDiffTime < 0) { //
                    dontAdd = 1;
                    if (DEBUG)
                        Log.d(TAG, "setPlayCurrentUs (mDiffpresentationTimeUs > maxDiff && tmpDiffTime < 0)");
                    }
                if(mLastPresentationTimeUs == 0 && mPlayCurrentUs != 0){ //Play the file from last position
                    dontAdd = 1;
                    if (DEBUG)
                        Log.d(TAG, "setPlayCurrentUs (mLastPresentationTimeUs == 0 && mPlayCurrentUs != 0)");
                    }
                if (DEBUG)
                    Log.d(TAG, "setPlayCurrentUs dontAdd="+dontAdd+" mPlayCurrentUs="+mPlayCurrentUs);
                if (dontAdd == 0) {
                    synchronized (mLockPts) {
                        mPlayCurrentUs += mDiffpresentationTimeUs;
                }
            }
                mLastPresentationTimeUs = mNowPresentationTimeUs;
                if (DEBUG)
                    Log.d(TAG, "setPlayCurrentUs(CheckTime) mNowPresentationTimeUs="+mNowPresentationTimeUs+" mLastPresentationTimeUs="+mLastPresentationTimeUs+" mPlayCurrentUs="+mPlayCurrentUs);
            setPresentationTimeUs(mPlayCurrentUs);
                if(mCheckCount > 100){
                    long Quotient;
                    Quotient = mPlayCurrentUs/DvrRecorderController.PTS_MAX;
                    if((Quotient == 0) && (Math.abs(mPlayCurrentUs-DvrRecorderController.PTS_MAX) < 4000000L))
                        if((mNowPresentationTimeUs < mPlayCurrentUs) && (Math.abs(mPlayCurrentUs-mNowPresentationTimeUs) > DvrRecorderController.PTS_MAX/2))
                            Quotient = 1;
                    synchronized (mLockPts) {
                        mPlayCurrentUs = mNowPresentationTimeUs + DvrRecorderController.PTS_MAX * Quotient;
                    }
                    mCheckCount=0;
                    if (DEBUG)
                        Log.d(TAG, "setPlayCurrentUs(CheckTime) mNowPresentationTimeUs="+mNowPresentationTimeUs+" mPlayCurrentUs="+mPlayCurrentUs);
                }
            }
        }
    }

    public void setISStartPlaying(boolean iSStartPlaying){
        mISStartPlaying = iSStartPlaying;
    }
    
    public boolean getISStartPlaying(){
        return mISStartPlaying;
    }

    public int getCurrentPlayTimeSec(){
        int retTimeSec;
        if(mRecordMode == DvrRecorderController.RECORDERMODE_TIMEShIFT) {
            if ((((speed >= 0) && (speed <= 2)) && (seekFlag == 0)) || (mStepForward != 0)) {
                retTimeSec = (int) ((mPlayCurrentUs - (mPlayStartUs + ((long) mTimeshiftReocrd.getTimeshiftRecStartTime() * 1000000L))) / 1000000L);
                if (DEBUG)
                Log.d(TAG, "getCurrentPlayTimeSec mPlayCurrentUs=" + mPlayCurrentUs + " mPlayStartUs=" + mPlayStartUs);
                if (DEBUG)
                Log.d(TAG, "getCurrentPlayTimeSec mTimeshiftReocrd.getTimeshiftRecStartTime=" + mTimeshiftReocrd.getTimeshiftRecStartTime());
                if (DEBUG)
                Log.d(TAG, "getCurrentPlayTimeSec retTimeSec( ((mPlayCurrentUs - (mPlayStartUs + ((long) mTimeshiftReocrd.getTimeshiftRecStartTime() * 1000000L))) / 1000000L) )=" + retTimeSec);
                if (retTimeSec < 0)
                    retTimeSec = 0;
                if (DEBUG)
                Log.d(TAG, "getCurrentPlayTimeSec mTimeshiftReocrd.getTimeShiftBufferSec()=" + mTimeshiftReocrd.getTimeShiftBufferSec());
                if (retTimeSec > mTimeshiftReocrd.getTimeShiftBufferSec()) {//Just to avoid display errors
                    retTimeSec = mTimeshiftReocrd.getTimeShiftBufferSec();
            }
                if (DEBUG) Log.d(TAG, "getCurrentPlayTimeSec retTimeSec=" + retTimeSec);
            } else {
                retTimeSec = (int) ((mTimeshiftPresentationTimeUs - mTimeshiftStartTimeUs) / 1000000);
                if (DEBUG)
                Log.d(TAG, "getCurrentPlayTimeSec mTimeshiftPresentationTimeUs=" + mTimeshiftPresentationTimeUs + " mTimeshiftStartTimeUs=" + mTimeshiftStartTimeUs);
                if (DEBUG)
                Log.d(TAG, "getCurrentPlayTimeSec retTimeSec((mTimeshiftPresentationTimeUs - mTimeshiftStartTimeUs)/1000000)=" + retTimeSec);
                if (retTimeSec < 0)
                    retTimeSec = 0;
                if (DEBUG) Log.d(TAG, "getCurrentPlayTimeSec retTimeSec=" + retTimeSec);
                synchronized (mLockPts) {
                    mPlayCurrentUs = mTimeshiftPresentationTimeUs;
                }
                if (DEBUG) Log.d(TAG, "getCurrentPlayTimeSec mPlayCurrentUs="+mPlayCurrentUs+" mTimeshiftPresentationTimeUs="+mTimeshiftPresentationTimeUs);
            }
        } else if (mRecordMode == DvrRecorderController.RECORDERMODE_RECORD) {
            if (mStepForward != 0) {
                if (DEBUG)
                    Log.d(TAG, "getCurrentPlayTimeSec mStepForward=" + mStepForward + " speed=" + speed + " mScanCurrentUs=" + mScanCurrentUs + " mPlayCurrentUs=" + mPlayCurrentUs);
        }
            if ((speed > 2) || (speed < 0) || (mStepForward != 0)) {
                if (DEBUG)
                    Log.d(TAG, "getCurrentPlayTimeSec speed=" + speed + " mPlayStartUs=" + mPlayStartUs + " mScanCurrentUs=" + mScanCurrentUs);
                retTimeSec = (int) ((mScanCurrentUs - mPlayStartUs) / 1000000);
                synchronized (mLockPts) {
                    mPlayCurrentUs = mScanCurrentUs;
            }
            } else {
                if (DEBUG)
                    Log.d(TAG, "getCurrentPlayTimeSec speed=" + speed + " mPlayStartUs=" + mPlayStartUs + " mPlayCurrentUs=" + mPlayCurrentUs);
                retTimeSec = (int) ((mPlayCurrentUs - mPlayStartUs) / 1000000);
            }
        } else {
            retTimeSec = 0;
        }

        if(retTimeSec < 0) {
            retTimeSec = 1;
        }
        if (DEBUG) Log.d(TAG, "getCurrentPlayTimeSec retTimeSec = " + retTimeSec);

        return retTimeSec;
    }

    public void setVideoMediaCodec(MediaCodec videoMediaCodec){

        mVideoMediaCodec = videoMediaCodec;
    }

    public MediaCodec getVideoMediaCodec(){

        return mVideoMediaCodec;
    }

    public void setAudioTrack(AudioTrack audioTrack){

        mAudioTrack = audioTrack;
    }

    public AudioTrack getAudioTrack(){

        return mAudioTrack;
    }

    public void setFilePath(String path){
        originalFilePath = path;
        filePath = path;
    }

    public void setTuner(Tuner tuner ){
        mTuner = tuner;
        if(mTuner == null){
            if (DEBUG) Log.d(TAG, "mTuner is null");
        }
    }

    public void startDvrPlay(){
        dvrStop = false;
    }

    public int getTotalRecTimeSec(){
        return mTotalRecTimeSec;
    }

    public void setTotalRecTimeSec(int totalTime){
        if (DEBUG) Log.d(TAG, "TotalRecTimeSec = " + totalTime);
        mTotalRecTimeSec = totalTime;
    }

    public void setLastPosition(int lastPosition ){
        long tmp;
        if (mRecordMode == DvrRecorderController.RECORDERMODE_TIMEShIFT) {
            tmp = lastPosition * 1000L;
            if (lastPosition != 0) {
                if (tmp > 0) {
                    mSeekMs = (mPlayCurrentUs + tmp) / 1000L;
                } else {
                    tmp = mPlayCurrentUs + tmp;
                    if (tmp <= (mTimeshiftStartTimeUs + 1000000L))
                        if(mTimeshiftStartTimeUs == mTimeshiftOriginalStartTimeUs)
                            tmp = mTimeshiftStartTimeUs + 1000000L;
                        else
                            tmp = mTimeshiftStartTimeUs + 2000000L;
                    mSeekMs = tmp / 1000L;
                }
                if(mIsPause == true) {
                    mStepForward = 1;
                }
            } else
                mSeekMs = 0;
            if (DEBUG) Log.d(TAG, "setLastPosition lastPosition="+lastPosition+" mSeekMs="+mSeekMs);
            if (DEBUG) Log.d(TAG, "setLastPosition mTimeshiftStartTimeUs="+mTimeshiftStartTimeUs+" mPlayCurrentUs="+mPlayCurrentUs);
        } else {
            if (mIsPause == false) {
                mLastPosition = lastPosition;
            } else {
                mLastPosition = lastPosition;
                mStepForward = 1;
            }

            // if seek over the end time of record, set to 3s before
            // according to 'if ((mTotalRecTimeSec - getCurrentPlayTimeSec() <= 3))'
            if (mLastPosition >= mTotalRecTimeSec) {
                mLastPosition = mTotalRecTimeSec - 3;
            }

            if (DEBUG) Log.d(TAG, "setLastPosition mLastPosition="+mLastPosition+" mTotalRecTimeSec="+mTotalRecTimeSec+" mStepForward="+mStepForward);
        }
    }

    public void setRecordMode(int mode){
        mRecordMode = mode;
    }

    public int getRecordMode(){
        return mRecordMode;
    }

    public int startTunerTuneDo() {
        Log.d(TAG, "startTunerTuneDo start, filePath="+filePath+" mTuner="+mTuner);
            //dvrStop = false;
            bufferSize = 4194280; //188*n

            //mOnPlaybackStatusChangedListener = new OnPlaybackStatusChangedListener();
            OnPlaybackStatusChangedListener DvrListener = new OnPlaybackStatusChangedListener() {
                @Override
                public void onPlaybackStatusChanged(int status) {
                int preSleepTime = mSleepTime;
                    /*
                    enum PlaybackStatus {
                        SPACE_EMPTY = 1,
                        SPACE_ALMOST_EMPTY = 2,
                        SPACE_ALMOST_FULL = 4,
                        SPACE_FULL = 8,
                    }
                    */
                    // a customized way to consume data efficiently by using status as a hint.
                    //Log.d(TAG, "onPlaybackStatusChanged status = " + status);
                    mSleepTime=10;
                    if(status ==4) //PLAYBACK_STATUS_ALMOST_FULL
                        mSleepTime=30;
                    else if(status ==8) ////PLAYBACK_STATUS_FULL
                        mSleepTime=60;
                    else if(status <4)
                        mSleepTime=10;

                    if(mRecordMode == DvrRecorderController.RECORDERMODE_RECORD) {
                        if(speed > 2 || speed < 0){
                            if(status == 8) {
                                if(Math.abs(speed) < 16)
                                mSleepTime = 150;
                                else
                                    mSleepTime = 120;
                            }
                            else {
                                if(Math.abs(speed) < 16)
                                mSleepTime = 100;
                                else
                                    mSleepTime = 85;
                        }
                    }
                    }
                if (DEBUG)
                    Log.d(TAG, "onPlaybackStatusChanged Status=" + status + " mSleepTime=" + mSleepTime + " preSleepTime=" + preSleepTime);
                }
            };

            if(DvrListener == null) {
            Log.e(TAG, "DvrListener is null");
                return 1;
            }
            //mTuner.cancelTuning();
            //mTuner.closeFrontend();
            if(mTuner == null) {
            Log.e(TAG, "mTuner is null");
                return 1;
            }

            mDvrPlayback = mTuner.openDvrPlayback(bufferSize, mExecutor, DvrListener);

            if(mDvrPlayback == null) {
            Log.e(TAG, "mDvrPlayback is null");
                return 1;
            }

            int mStatusMask = 1;
            long mLowThreshold = 1258291;
            long mHighThreshold = 3774873;
            long mPacketSize = 188;
            int mDataFormat = DvrSettings.DATA_FORMAT_TS;
            DvrSettings dvrSettings = DvrSettings.builder()
                    .setStatusMask(mStatusMask).setLowThreshold(mLowThreshold).setHighThreshold(mHighThreshold).setPacketSize(mPacketSize).setDataFormat(mDataFormat).build();

            mDvrPlayback.configure(dvrSettings);

            //START DVR
            mDvrPlayback.start();
            //Log.d(TAG,"startTunerTuneDo mDvrPlayback.flush start");
            mDvrPlayback.flush();
            //Log.d(TAG,"startTunerTuneDo mDvrPlayback.flush end");

            //mDvrPlayback.read(readSize);

            //start filter...
            if(mRecordMode == DvrRecorderController.RECORDERMODE_TIMEShIFT){
            Log.d(TAG, "new TimeShiftReadThread");
                readThread = new TimeShiftReadThread();
            }
            else {
            Log.d(TAG, "new PvrReadThread");
                String fileInfoPath = filePath+DvrRecordInfo.FileInfoExtension;
                Log.d(TAG, "fileInfoPath = " + fileInfoPath);
                File file = new File(fileInfoPath);
                if(!file.exists()){
                    Log.e(TAG,"fileInfoPath is not exists");
                    return 1;
                }
                else {
                    readThread = new PvrReadThread();
                }
            }

            readThread.start();
            return 0;
            //startVideoMediaCodec();
    }

    private void flushDvrPlayback() {
        if (DEBUG) Log.d(TAG, "flushDvrPlayback in");
        if (mDvrPlayback != null) {
            if (DEBUG) Log.d(TAG, "flushDvrPlayback playbackOptionLock.lock()");
            playbackOptionLock.lock();

            mDvrPlayback.stop();
            //Log.d(TAG,"flushDvrPlayback mDvrPlayback.flush start");
            mDvrPlayback.flush();
            //Log.d(TAG,"flushDvrPlayback mDvrPlayback.flush end");
            mDvrPlayback.start();

            playbackOptionLock.unlock();
            if (DEBUG) Log.d(TAG, "flushDvrPlayback playbackOptionLock.unlock()");
        }
        if (DEBUG) Log.d(TAG, "flushDvrPlayback out");
    }
    private void StepIframeStart() {
        flushDvrPlayback();
        PlaybackParams aparams = mAudioTrack.getPlaybackParams();
        aparams.setSpeed(1.0f);
        mAudioTrack.setPlaybackParams(aparams);
        Bundle mParamsTrick = new Bundle();
        mParamsTrick.putInt("vendor.tis.DecodeIFrameOnly.value", 1);
        LogUtils.d("vendor.tis.DecodeIFrameOnly.value 1");
        mParamsTrick.putInt("vendor.tis.dvr-playback-speed.value", (int)(1*1000));
        LogUtils.d("vendor.tis.dvr-playback-speed 1*1000");
        mVideoMediaCodec.setParameters(mParamsTrick);
        AudioManager audioManager = mContext.getSystemService(AudioManager.class);
        audioManager.setParameters("clear_video_pts=1");
        LogUtils.d("set clear_video_pts 1");
        audioManager.setParameters("rtk_mute=1");
        LogUtils.d("set rtk_mute 1");
        flushDvrPlayback();

        //playerResume();
    }

    private void StepIframeStop() {
        Bundle mParamsTrick = new Bundle();
        mParamsTrick.putInt("vendor.tis.DecodeIFrameOnly.value", 0);
        mParamsTrick.putInt("vendor.tis.dvr-playback-speed.value", (int) (1 * 1000));
        mVideoMediaCodec.setParameters(mParamsTrick);
        AudioManager audioManager = mContext.getSystemService(AudioManager.class);
        audioManager.setParameters("clear_video_pts=0");
        //Log.d(TAG,"set clear_video_pts 0");
        audioManager.setParameters("rtk_mute=0");

        PlaybackParams aparams = mAudioTrack.getPlaybackParams();

        aparams.setSpeed(0.0f);
        mAudioTrack.setPlaybackParams(aparams);
        flushDvrPlayback();
        //playerPause();
    }

    private class TimeShiftReadThread extends Thread {
        private String fileInfoPath;//= filePath+DvrRecordInfo.FileInfoExtension;
        //private     ParcelFileDescriptor fileInfoPathFd =  openFile(fileInfoPath);
        private     DvrRecordInfo.FileDataListManager fileDataListManager = new DvrRecordInfo.FileDataListManager();
        private     DvrRecordInfo.FileDataManager manager1;
        private     long PlaybackReadSize = readSize;
        private     long targetPts = 0;
        private     int fastspeedMs = 0;
        private     int fileReadNumber =-1;
        private     long  ret = -1;
        private     int   upTimeMs = 1000;
        //private     long  currentTimeMillis= System.currentTimeMillis();
        private     long  currentTimeMillis;// = getElapsedMillis();
        private     long  writeTimeMillis;// = currentTimeMillis;
        private     long  playPts = 0;
        private     int   delay_ms;
        private     float useSpeed = 1;
        int pauseLoop = 0;
        private long PlaybackReadSizeTmp;
        public void run() {
            Log.d(TAG,"TimeShiftReadThread timeshift read Thread enter..");
            File checkFile=null;
            seekFlag = 0;
//            mWrapAroundMax = 0;
//            mWrapAroundCnt = 0;
            while (dvrStop) {
                try {
                    Thread.currentThread().sleep(100);////40);
                    //fastspeedMs+=mSleepTime;
                } catch (InterruptedException e) {
                    // TODO: handle exception
                    Log.d(TAG,"InterruptedException, e="+e);
                }
            }

            currentTimeMillis = getElapsedMillis();
            writeTimeMillis = currentTimeMillis;
            untilUpdateTimeShiftInfo(fileDataListManager);
            filePath=fileDataListManager.FileDataManagerList.get(0).getbaseName();
            fileInfoPath = fileDataListManager.FileDataManagerList.get(0).getFileName();
            DvrRecordInfo.FileParameters tmpFileParameters = null;
            DvrRecordInfo.FileParameters tmpFileParameters1 = null;
            Log.d(TAG,"TimeShiftReadThread TimeShiftReadThread Start: filePath="+filePath+" fileInfoPath="+fileInfoPath);
            while (!dvrStop) {
                if (DEBUG)
                Log.d(TAG,"TimeShiftReadThread mTunerId="+mTunerId+" mCurrentFileIndex="+mCurrentFileIndex+" filePath="+filePath);
                if (currentTimeMillis + upTimeMs < getElapsedMillis()) {
                    if (DEBUG)
                        Log.d(TAG, "TimeShiftReadThread untilUpdateTimeShiftInfo per second");
                    untilUpdateTimeShiftInfo(fileDataListManager);
                    if (DEBUG)
                        Log.d(TAG, "TimeShiftReadThread untilUpdateTimeShiftInfo end");
                    //currentTimeMillis = System.currentTimeMillis();
                    currentTimeMillis = getElapsedMillis();
                }
                if (mStepForward == 2) {
                    try {
                        Thread.currentThread().sleep(1000);////40);
                    } catch (InterruptedException e) {
                        // TODO: handle exception
                    }
                    if (DEBUG)
                        Log.d(TAG, "PvrReadThread mStepForward=2 ==> StepIframeStop()");
                    StepIframeStop();
                    mStepForward = 0;
                    mPtsFlagUs = 0;
                    mSeekTimeUs = 0;
                    pauseLoop=0;
                }
                if(mTimeshiftOriginalStartTimeUs == 0) {
                    manager1 = fileDataListManager.findFirstNearestPtsInData(false);
                    if (manager1 != null) {
                        tmpFileParameters = manager1.getFirstNearestPtsInData(false);
                        if (tmpFileParameters != null) {
                            mTimeshiftOriginalStartTimeUs = tmpFileParameters.getiPts();
                            mTimeshiftStartTimeUs = mTimeshiftOriginalStartTimeUs;
                        }
                    }
                }
                if(mTimeshiftReocrd.getTimeshiftRecStartTime() != 0) {
                    long tmp = (long) mTimeshiftReocrd.getTimeshiftRecStartTime() * 1000000L;
                    if (DEBUG)
                    Log.d(TAG,"TimeShiftReadThread getTimeshiftRecStartTime()="+mTimeshiftReocrd.getTimeshiftRecStartTime());
                    mTimeshiftStartTimeUs = mTimeshiftOriginalStartTimeUs + tmp;
                    if (DEBUG)
                    Log.d(TAG,"TimeShiftReadThread Update mTimeshiftStartTimeUs: mTimeshiftOriginalStartTimeUs="+mTimeshiftOriginalStartTimeUs+" mTimeshiftStartTimeUs="+mTimeshiftStartTimeUs);
                }

                if(mIsPause == true && mTimeshiftReocrd.getTimeshiftRecStartTime() != 0){
                    int currentPlayTime=getCurrentPlayTimeSec();
                    if (DEBUG)
                    Log.d(TAG,"TimeShiftReadThread mIsPause=true,Check Auto resume: currentPlayTime="+currentPlayTime);
                    if(currentPlayTime <= 2){
                        mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_SOF, 0, 0, null);
                        synchronized (mLockPts) {
                        mPlayCurrentUs = mTimeshiftPresentationTimeUs;
                        }
                        playerResume();
                        flushDvrPlayback();
                        try {
                            //Log.d(TAG,"hread.currentThread().sleep, mSleepTime="+mSleepTime);
                            Thread.currentThread().sleep(mSleepTime);
                            fastspeedMs = 0;
                        } catch (InterruptedException e) {
                            // TODO: handle exception
                        }
                        //mSeekMs = (mTimeshiftStartTimeUs/1000) + 2000;
                        //Log.d(TAG,"PESI_SVR_EVT_PVR_PLAY_SOF mSeekMs="+mSeekMs);
                        if (DEBUG)
                        Log.d(TAG,"TimeShiftReadThread Auto resume(PESI_SVR_EVT_PVR_PLAY_SOF) mPlayCurrentUs="+mPlayCurrentUs);
                    }
                }

                if (mDvrPlayback != null && (mIsPause == false || mStepForward == 1)) {
                    if(mSeekMs!=0){
                        // untilUpdateTimeShiftInfo(fileDataListManager);
                        if (DEBUG)
                        Log.d(TAG,"TimeShiftReadThread mSeekMs != 0, mSeekMs="+mSeekMs);
                        currentTimeMillis = getElapsedMillis();
                        targetPts = mSeekMs * 1000L;
                        manager1 = fileDataListManager.findNearestPtsInData(targetPts,true);
                        if (DEBUG)
                            Log.d(TAG, "TimeShiftReadThread targetPts: " + targetPts + " manager1:FileName" + manager1.getFileName());
                        if (manager1 == null) {
                            if (DEBUG) Log.d(TAG, "manager1 == null, mSeekMs: " + mSeekMs);
                            mSeekMs = 0;
                            continue;
                        }
                        if(!manager1.getFileName().equals(fileInfoPath)){
                            if (DEBUG)
                            Log.d(TAG,"TimeShiftReadThread !manager1.getFileName().equals(fileInfoPath)");
                            fileInfoPath = manager1.getFileName();
                            closeFileFile();
                            filePath = manager1.getbaseName();
                            dvrFile = null ;
                            mTotalReadSize = 0;
                            mFilelength = 0;
                        }
                        updateCurrentFileIndex(fileInfoPath);
                        tmpFileParameters = manager1.findNearestPtsInData(targetPts,true);
                        if(tmpFileParameters != null){
                            if (mStepForward == 1){
                                mTimeshiftPresentationTimeUs = tmpFileParameters.getiPts();
                                StepIframeStart();
                                PlaybackReadSize = tmpFileParameters.getiFrameSize();
                                pauseLoop = 20;
                                if (DEBUG)
                                    Log.d(TAG, "TimeShiftReadThread(mStepForward=1) mTimeshiftPresentationTimeUs="+mTimeshiftPresentationTimeUs+" PlaybackReadSize="+PlaybackReadSize+" pauseLoop="+pauseLoop);
                            }
                            else {
                                mPtsFlagUs = tmpFileParameters.getiPts();
                                mSeekTimeUs = mPtsFlagUs - mPlayCurrentUs;
                                if (DEBUG)
                                    Log.d(TAG, "TimeShiftReadThread mSeekTimeUs=" + mSeekTimeUs + " mPtsFlagUs=" + mPtsFlagUs);
                                PlaybackReadSize = readSize;
                            }
                            mTotalReadSize = tmpFileParameters.getIbase();
                            if (DEBUG)
                                Log.d(TAG, "TimeShiftReadThread mTotalReadSize="+mTotalReadSize);
                            tmpFileParameters = null;
                            flushDvrPlayback();
                            if (DEBUG)
                            Log.d(TAG,"TimeShiftReadThread mSeekMs="+mSeekMs+" mTotalReadSize="+mTotalReadSize+" mTimeshiftStartTimeUs="+mTimeshiftStartTimeUs);
                        }else{
                            if (DEBUG)
                            Log.d(TAG,"TimeShiftReadThread tmpFileParameters == null");
                        }
                        mSeekMs= 0;
                        seekFlag = 5;
                    } else if (speed > 2 || speed < 0) {
                        long currentPts;
                        if(targetPts != 0 && speed == useSpeed){
                            if (DEBUG)
                            Log.d(TAG,"TimeShiftReadThread targetPts != 0 && speed == useSpeed");
                            currentPts = targetPts;
                            targetPts = currentPts + (long)(fastspeedMs*useSpeed*1000);
                        }else{//roll
                            flushDvrPlayback();
                            if(targetPts ==0) {
                                currentPts = mPlayCurrentUs;//mPresentationTimeUs;//getPresentationTimeUs();
                                mTimeshiftPresentationTimeUs = mPlayCurrentUs;//mPresentationTimeUs;
                            } else
                                currentPts = mTimeshiftPresentationTimeUs;
                            writeTimeMillis = getElapsedMillis();
                            targetPts = currentPts;
                            mTotalReadSize = 0;
                            delay_ms = 0;
                            if (DEBUG)
                            Log.d(TAG,"TimeShiftReadThread targetPts="+targetPts+" writeTimeMillis="+writeTimeMillis);
                            if (DEBUG)
                            Log.d(TAG,"TimeShiftReadThread mCurrentFileIndex="+mCurrentFileIndex+" fileInfoPath="+fileInfoPath);
                            manager1 = fileDataListManager.findNearestPtsInData(targetPts,true);
                            if (DEBUG)
                            Log.d(TAG,"TimeShiftReadThread manager1.getFileName()="+manager1.getFileName());
                            if(!manager1.getFileName().equals(fileInfoPath)){
                                if (DEBUG)
                                Log.d(TAG,"TimeShiftReadThread need to change file, speed="+speed);
                                fileInfoPath = manager1.getFileName();
                                closeFileFile();
                                filePath = manager1.getbaseName();
                                dvrFile = null ;
                                mTotalReadSize = 0;
                                mFilelength = 0;
                                if (DEBUG)
                                Log.d(TAG,"TimeShiftReadThread new filePath="+filePath+" fileInfoPath="+fileInfoPath);
                            }
                            updateCurrentFileIndex(fileInfoPath);
                            tmpFileParameters = manager1.findNearestPtsInData(targetPts,true);

                            playPts = tmpFileParameters.getiPts();
                            if (DEBUG)
                            Log.d(TAG,"TimeShiftReadThread mCurrentFileIndex="+mCurrentFileIndex+" playPts="+playPts);
                        }

                        //mWrapAroundCnt = (int) (playPts / DvrRecorderController.PTS_MAX);

                        useSpeed = speed;
                        Boolean tmpNext = false;
                        Boolean finish = false;
                        //playPts = tmpFileParameters.getiPts();
                        int loop = 1;
                        long tmpdelay_ms = 0;
                        long diffPtsMs = 0;
                        if (DEBUG) Log.d(TAG, "TimeShiftReadThread loop start/end, speed=" + speed);
                        for (int i = 0; i < loop; i++) {
                            if(useSpeed>2){
                                finish = fileDataListManager.checkIsLastFileDataManager(manager1);
                                if(finish == true)
                                    if (DEBUG)
                                    Log.d(TAG,"TimeShiftReadThread It is Last FileDataManager");
                                tmpFileParameters1 = manager1.findNextFileParameters(tmpFileParameters,true);
                                if(tmpFileParameters1 == null)
                                    if (DEBUG)
                                    Log.d(TAG,"TimeShiftReadThread It FileParameters is Last");
                                tmpNext = true;
                            } else {
                                if(mTimeshiftReocrd.getTimeshiftRecStartTime() == 0) {
                                    if (DEBUG)
                                    Log.d(TAG,"TimeShiftReadThread mTimeshiftRecStartSec = 0");
                                finish = fileDataListManager.checkIsFirstFileDataManager(manager1);
                                    if(finish == true)
                                        if (DEBUG)
                                        Log.d(TAG,"TimeShiftReadThread It is First FileDataManager, mTimeshiftStartTimeUs="+mTimeshiftStartTimeUs);
                                tmpFileParameters1 = manager1.findPreviousFileParameters(tmpFileParameters,true);
                                    if(tmpFileParameters1 == null)
                                        if (DEBUG)
                                        Log.d(TAG,"TimeShiftReadThread It FileParameters is First, mTimeshiftStartTimeUs="+mTimeshiftStartTimeUs);
                                } else {
                                    DvrRecordInfo.FileDataManager managerTmp;
                                    long headTimeUs,tailTimeUs;
                                    if (DEBUG)
                                    Log.d(TAG,"TimeShiftReadThread mTimeshiftRecStartSec="+mTimeshiftReocrd.getTimeshiftRecStartTime());

                                    headTimeUs = manager1.getFirstNearestPtsInData(false).getiPts();
                                    tailTimeUs = manager1.getLastNearestPtsInData(false).getiPts();
                                    if(mTimeshiftStartTimeUs >= headTimeUs && mTimeshiftStartTimeUs <= tailTimeUs)
                                        finish = true;

                                    if(finish == true)
                                        if (DEBUG)
                                        Log.d(TAG,"TimeShiftReadThread It is First FileDataManager, mTimeshiftStartTimeUs="+mTimeshiftStartTimeUs);

                                    tmpFileParameters1 = manager1.findPreviousFileParameters(tmpFileParameters,true);
                                    if(tmpFileParameters1 != null){
                                        if (DEBUG)
                                        Log.d(TAG,"TimeShiftReadThread tmpFileParameters1.getiPts="+tmpFileParameters1.getiPts()+" mTimeshiftStartTimeUs="+mTimeshiftStartTimeUs);
                                        if(tmpFileParameters1.getiPts() < mTimeshiftStartTimeUs) {
                                            if (DEBUG)
                                            Log.d(TAG, "TimeShiftReadThread It FileParameters is First");
                                            tmpFileParameters1 = null;
                                        }
                                    } else {
                                        if (DEBUG)
                                        Log.d(TAG,"TimeShiftReadThread tmpFileParameters1 is null==>something error, mTimeshiftStartTimeUs="+mTimeshiftStartTimeUs);
                                    }
                                }
                                tmpNext = false;
                            }
                            if(tmpFileParameters1 == null){
                                if (DEBUG)
                                Log.d(TAG,"TimeShiftReadThread tmpFileParameters error!");
                                if(finish){
                                    if (DEBUG)
                                    Log.d(TAG,"TimeShiftReadThread tmpFileParameters1 is null, finish is true, tmpNext="+tmpNext);
                                    break;
                                } else {
                                    if(tmpNext){//Switch to the next dat file && get the first pts
                                        manager1 = fileDataListManager.findNextNearestPtsInData(true);
                                        tmpFileParameters = manager1.getFirstNearestPtsInData(true);
                                        if (DEBUG)
                                        Log.d(TAG,"TimeShiftReadThread Switch to the next dat file && get the first pts");
                                    }else{//Switch to the previous dat file && get the last data
                                        manager1 = fileDataListManager.findPreviousNearestPtsInData(true);
                                        tmpFileParameters = manager1.getLastNearestPtsInData(true);
                                        if (DEBUG)
                                        Log.d(TAG,"TimeShiftReadThread Switch to the previous dat file && get the last data");
                                    }
                                    fileInfoPath = manager1.getFileName();
                                    closeFileFile();
                                    filePath = manager1.getbaseName();
                                    if (DEBUG)
                                    Log.d(TAG,"TimeShiftReadThread next(previous) filePath="+filePath+" fileInfoPath="+fileInfoPath);
                                    dvrFile = null ;
                                    mTotalReadSize = 0;
                                    mFilelength = 0;
                                }
                            } else {
                                tmpFileParameters = tmpFileParameters1;
                            }
                            if(tmpFileParameters == null){
                                Log.e(TAG, "Unexpected error occurred, tmpFileParameters is null==>break");
                                break;
                            }
                            if (DEBUG)
                            Log.d(TAG,"TimeShiftReadThread tmpFileParameters.getiPts()="+tmpFileParameters.getiPts());
                            diffPtsMs = (tmpFileParameters.getiPts() - playPts)/1000;
                            if (DEBUG)
                            Log.d(TAG,"TimeShiftReadThread playPts="+playPts+" diffPtsMs="+diffPtsMs);
                            long  endTimeMillis = getElapsedMillis();
                            if (DEBUG)
                            Log.d(TAG,"TimeShiftReadThread writeTimeMillis="+writeTimeMillis+" endTimeMillis="+endTimeMillis);
                            tmpdelay_ms = Math.abs((long)diffPtsMs/(long)useSpeed) - (endTimeMillis - writeTimeMillis);
                            if (DEBUG)
                            Log.d(TAG,"TimeShiftReadThread tmpdelay_ms="+tmpdelay_ms+" delay_ms="+delay_ms);
                            if((tmpdelay_ms-delay_ms < 2000 || delay_ms == 0) && (tmpdelay_ms < 2000)){
                                delay_ms = (int)tmpdelay_ms;
                                if (DEBUG) Log.d(TAG, "TimeShiftReadThread delay_ms=" + delay_ms);
                            }else{
                                Log.d(TAG,"TimeShiftReadThread tmpdelay_ms error: "+tmpdelay_ms+" playPts: "+playPts+" endTimeMillis: "+endTimeMillis+"writeTimeMillis: "+writeTimeMillis);
                                break;
                            }
                            if (DEBUG)
                            Log.d(TAG,"TimeShiftReadThread delay_ms="+delay_ms+" finish="+finish+" tmpFileParameters1="+tmpFileParameters1);
                            if (delay_ms < 100 && !(tmpFileParameters1 == null && finish) && loop <= 32) {
                                loop++;
                                Log.d(TAG,"TimeShiftReadThread loop++, loop="+loop);
                            }
                        }
                        if (DEBUG) Log.d(TAG, "TimeShiftReadThread loop start/end, loop=" + loop);
                        if(tmpFileParameters == null){
                            Log.e(TAG, "Unexpected error occurred, tmpFileParameters is null==>continue");
                            continue;
                        }
                        if(tmpFileParameters1 == null && finish){
                            if (DEBUG)
                            Log.d(TAG,"TimeShiftReadThread End point, no data sent"+tmpFileParameters.toString());
                            if(tmpNext == true){
                                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_EOF, 0, 0, null);
                                mTimeshiftPresentationTimeUs = tmpFileParameters.getiPts() - 1000000L;
                                synchronized (mLockPts) {
                                mPlayCurrentUs=mTimeshiftPresentationTimeUs;
                                }
                                useSpeed =1;
                                //seekFlag = 10;
                                playerResume();
                                //flushDvrPlayback();
                                mSeekMs = (mPlayCurrentUs/1000);
                                if (DEBUG)
                                Log.d(TAG,"TimeShiftReadThread mPlayCurrentUs="+mPlayCurrentUs+" mTimeshiftPresentationTimeUs="+mTimeshiftPresentationTimeUs);
                                if (DEBUG)
                                Log.d(TAG,"TimeShiftReadThread PESI_SVR_EVT_PVR_PLAY_EOF mSeekMs="+mSeekMs+" seekFlag="+seekFlag);
                            } else {
                                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_SOF, 0, 0, null);
                                //Log.d(TAG,"PESI_SVR_EVT_PVR_PLAY_EOF, speed="+speed);
                                synchronized (mLockPts) {
                                    if(mTimeshiftStartTimeUs == mTimeshiftOriginalStartTimeUs)
                                mPlayCurrentUs=mTimeshiftStartTimeUs+1000000L;
                                    else
                                        mPlayCurrentUs = mTimeshiftStartTimeUs + 3000000L;
                                }
                                mTimeshiftPresentationTimeUs=mPlayCurrentUs;
                                useSpeed =1;
                                //seekFlag = 10;
                                playerResume();
                                //flushDvrPlayback();
                                mSeekMs = (mPlayCurrentUs/1000) + 2000;
//                                if(mTimeshiftStartTimeUs == mTimeshiftOriginalStartTimeUs) {
//                                    mSeekMs = (mPlayCurrentUs / 1000) + 2000;
//                                    if (DEBUG) Log.d(TAG, "Allen_test@@@ TimeShiftReadThread buffer is not full, mSeekMs = (mPlayCurrentUs / 1000) + 2000");
//                                }
//                                else {
//                                    mSeekMs = (mPlayCurrentUs / 1000) + 4000;
//                                    if (DEBUG) Log.d(TAG, "Allen_test@@@ TimeShiftReadThread buffer is full, mSeekMs = (mPlayCurrentUs / 1000) + 4000");
//                                }

                                if (DEBUG)
                                Log.d(TAG,"TimeShiftReadThread mPlayCurrentUs="+mPlayCurrentUs+" mTimeshiftPresentationTimeUs="+mTimeshiftPresentationTimeUs);
                                if (DEBUG)
                                Log.d(TAG,"TimeShiftReadThread PESI_SVR_EVT_PVR_PLAY_SOF mSeekMs="+mSeekMs+" seekFlag="+seekFlag);
                            }
                            continue;
                        }
                        try {
                            if (DEBUG)
                            Log.d(TAG,"TimeShiftReadThread diffPts: "+diffPtsMs+" playPts: "+playPts+" iPts: "+tmpFileParameters.getiPts()+"delay_ms "+delay_ms);
                            //Log.d(TAG, "Thread.currentThread().sleep, delay_ms ="+delay_ms);
                            Thread.currentThread().sleep(delay_ms);
                            fastspeedMs += delay_ms;
                            // Log.d(TAG,"Thread.currentThread().sleep"+" mTotalReadSize:"+mTotalReadSize+" mFilelength:"+mFilelength+"fastspeedMs:"+fastspeedMs);
                        } catch (Exception e) {
                            // TODO: handle exception
                        }
                        if(tmpFileParameters != null ){
                            if (DEBUG)
                            Log.d(TAG,"TimeShiftReadThread speed:"+speed+" mTotalReadSize:"+mTotalReadSize+" currentPts:"+currentPts+" targetPts: "+targetPts+" fileInfoPath: "+fileInfoPath+" tmpFileParameters"+tmpFileParameters.toString());
                            updateCurrentFileIndex(fileInfoPath);
                            PlaybackReadSize = tmpFileParameters.getiFrameSize();
                            mTotalReadSize = tmpFileParameters.getIbase();
                            mTimeshiftPresentationTimeUs = tmpFileParameters.getiPts();
                        }
                    } else {
                        useSpeed = 1;
                        targetPts = 0;
//                        if(mDiscardpFlag == 2){
//                            mDiscardpFlag = 0;
//                            Log.d(TAG, "TimeShiftReadThread mDiscardpFlag=2 to mDiscardpFlag=0");
//                        }
                        if(speed == 1){
                            int currentPlayTime=getCurrentPlayTimeSec();
                            if(mTimeshiftReocrd.getTimeshiftRecStartTime() != 0 && currentPlayTime <= 1 && seekFlag == 0){
                                mSeekMs = (mPlayCurrentUs/1000)+3000;
                                if (DEBUG)
                                    Log.d(TAG, "TimeShiftReadThread speed == 1, mTimeshiftReocrd.getTimeshiftRecStartTime()=" + mTimeshiftReocrd.getTimeshiftRecStartTime());
                                if (DEBUG)
                                Log.d(TAG,"TimeShiftReadThread speed == 1, currentPlayTime="+currentPlayTime+"mSeekMs="+mSeekMs);
                                //seekFlag = 10;
                                    continue;
                                }
                            }
                        if(speed == 2) {
                            int currentPlayTime=getCurrentPlayTimeSec();
                            int currentRecTime = mTimeshiftReocrd.getTimeshiftRecEndTime() - mTimeshiftReocrd.getTimeshiftRecStartTime();
                            if (DEBUG)
                            Log.d(TAG, "TimeShiftReadThread PESI_SVR_EVT_PVR_PLAY_EOF, No seek required");
                            if(currentRecTime - currentPlayTime <= 2) {
                                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_EOF, 0, 0, null);
                                useSpeed = 1;
                                playerResume();
                                if (DEBUG)
                                Log.d(TAG, "TimeShiftReadThread PESI_SVR_EVT_PVR_PLAY_EOF, No seek required");
                            }
                        }
                        mTimeshiftPresentationTimeUs = mPlayCurrentUs;
                        //manager1 = fileDataListManager.findFirstNearestPtsInData(false);
                        //tmpFileParameters = manager1.getFirstNearestPtsInData(false);
                        //mTimeshiftStartTimeUs = tmpFileParameters.getiPts();
                        //tmpFileParameters = null;
                    }

                    if (DEBUG) Log.d(TAG, "TimeShiftReadThread openFileRead start");
                    fileReadNumber = openFileRead(dvr_buff, mTotalReadSize, filePath, (int) PlaybackReadSize, speed);
                    if (DEBUG)
                        Log.d(TAG, "TimeShiftReadThread openFileRead speed=" + speed + " fileReadNumber=" + fileReadNumber + " filePath=" + filePath);
                    if (DEBUG)
                        Log.d(TAG, "TimeShiftReadThread openFileRead mTotalReadSize(Ibase)=" + mTotalReadSize + " PlaybackReadSize(FrameSize)=" + PlaybackReadSize);
                    if (DEBUG)
                        Log.d(TAG, "TimeShiftReadThread dvr_buff[0]=" + String.format("0x%X", dvr_buff[0]) + " dvr_buff[1]=" + String.format("0x%X", dvr_buff[1]));

                    PlaybackReadSizeTmp=PlaybackReadSize;
                    if (speed > 2.0 || speed <= 0) {
                        PlaybackReadSizeTmp = fileReadNumber;
                    }

                    if(fileReadNumber>0) {
                        playbackOptionLock.lock();
                        if (fileReadNumber >= PlaybackReadSize) {
                            if(pauseLoop != 0) {
                                while(pauseLoop > 0){
                                    if (DEBUG) Log.d(TAG, "TimeShiftReadThread mDvrPlayback.read start, PlaybackReadSize=" + PlaybackReadSize+ "pauseLoop="+pauseLoop);
                                    ret = mDvrPlayback.read(dvr_buff, 0, PlaybackReadSizeTmp);
                                    pauseLoop --;
                                }
                            }
                            else {
                                ret = mDvrPlayback.read(dvr_buff, 0, PlaybackReadSizeTmp);
                            }
                        }
                        else {
                            ret = mDvrPlayback.read(dvr_buff, 0, (fileReadNumber/188)*188);
                            PlaybackReadSizeTmp = 0;
                        }
                        playbackOptionLock.unlock();
                        if(ret > 0){
                            if(PlaybackReadSizeTmp == fileReadNumber && (speed > 2.0 || speed <= 0))
                                mTotalReadSize = mTotalReadSize + PlaybackReadSize;
                            else
                            mTotalReadSize = mTotalReadSize+ret;
                            fastspeedMs = 0;
                        }
                        //Log.d(TAG,"speed="+speed+" ret="+ret+" PlaybackReadSize="+PlaybackReadSize+" mTotalReadSize="+mTotalReadSize);
                        if (DEBUG)
                        Log.d(TAG,"TimeShiftReadThread mTotalReadSize:"+mTotalReadSize+" ret:"+ret+" PlaybackReadSize:"+PlaybackReadSize+" fileReadNumber:"+fileReadNumber);
                    } else {
                        if(fileReadNumber == -2){
                            if (DEBUG)
                            Log.d(TAG,"TimeShiftReadThread openFileRead fail, file does not exist, filePath"+filePath);
                        }
                        if (DEBUG)
                        Log.d(TAG,"TimeShiftReadThread openFileRead fail, mTotalReadSize: "+mTotalReadSize+"filePath"+filePath);
                    }
                }
                if (mStepForward == 1) {
                    mStepForward = 2;
                }
                if (DEBUG) Log.d(TAG, "TimeShiftReadThread other start, mSleepTime=" + mSleepTime);
                if(mTotalReadSize >= mFilelength && speed <= 2 && speed>0){
                    if (DEBUG)
                    Log.d(TAG,"TimeShiftReadThread mTotalReadSize="+mTotalReadSize+" mFilelength="+mFilelength+" speed="+speed);
                    if(untilNeedChangeFile(true) == true){
                        closeFileFile();
                        //filePath = RtkApplication.getDVRRecorderFilePath(mRecordMode)+"_INDEX_"+mCurrentFileIndex;
                        filePath = originalFilePath+"_INDEX_"+mCurrentFileIndex;
                        fileInfoPath = filePath + DvrRecordInfo.FileInfoExtension;
                        //filePath = originalFilePath+"_INDEX_"+mCurrentFileIndex;
                        dvrFile = null ;
                        mTotalReadSize = 0;
                        mFilelength = 0;
                        if (DEBUG)
                        Log.d(TAG,"TimeShiftReadThread ChangeFile, filePath="+filePath);
                    }
                }
                try {
                    //Log.d(TAG, "Thread.currentThread().sleep mSleepTime = "+mSleepTime+" speed="+speed);
                    Thread.currentThread().sleep(mSleepTime);////40);
                    if(mIsPause == true){
                        fastspeedMs = 0;
                    }else if(speed >2 || speed< 0){
                        fastspeedMs += mSleepTime;
                    }
                    if(seekFlag != 0)
                        seekFlag--;
                } catch (InterruptedException e) {
                    // TODO: handle exception
                    Log.d(TAG,"TimeShiftReadThread InterruptedException");
                }
                if (DEBUG) Log.d(TAG, "TimeShiftReadThread other end");
            }
            Log.d(TAG,"TimeShiftReadThread timeshift read Thread exit.., dvrStop="+dvrStop);
        }
    }

    private class PvrReadThread extends Thread {
        private String fileInfoPath = filePath+DvrRecordInfo.FileInfoExtension;
        private boolean isNeedRepeat = true;//SettingSp.getInstance().getBoolean(SettingSp.DVR_REPEAT_MODE, false);
        private ParcelFileDescriptor fileInfoPathFd;// =  openFile(fileInfoPath);
        private DvrRecordInfo.FileDataListManager fileDataListManager = new DvrRecordInfo.FileDataListManager();
        private DvrRecordInfo.FileDataManager manager1;// = new DvrRecordInfo.FileDataManager(fileInfoPath,filePath);
        private long  ret = -1;
        private int fileReadNumber =-1;
        private long PlaybackReadSize = readSize;
        private long targetPts = 0;
        private int fastspeedMs = 0;
        private long startTimeMs,delayTimeMs = 0;
        private     int   upTimeMs = 1000;
        private     long  currentTimeMillis;
        private List<long[]> recoordHeadTailList = null;
        String ptsFilePath;
        Long[] pairTmp = null;
        long ibaseTmp;
        long tmpPts;
        boolean switchingToScanMode = false;
        int tmpData;
        private long RadioReadSize = (TS_PACKET_SIZE*20);
        private long PlaybackReadSizeTmp;
        int pauseLoop = 0;

        private long prePlayCurrentUs = 0;
        private int checkAudioPtsCount = 0;

        boolean isMusicEnded(long currentPts){
            long endPts;
            int i;
            if(mIsPause == true)
                return false;
            if (recoordHeadTailList.size() > 0) {
                endPts = recoordHeadTailList.get(recoordHeadTailList.size()-1)[4];
                if (DEBUG)
                    Log.d(TAG,"isMusicEnded  currentPts="+currentPts+" endPts="+endPts+" getCurrentPlayTimeSec="+getCurrentPlayTimeSec());
                if(Math.abs(currentPts - endPts) <= 1000000){
                    if (DEBUG)
                        Log.d(TAG,"isMusicEnded  currentPts="+currentPts+" endPts="+endPts);
                    if (DEBUG)
                        Log.d(TAG,"Math.abs(currentPts - endPts) <= 1000000, Math.abs(currentPts-endPts)="+Math.abs(currentPts-endPts));
                    return true;
                }
                if(prePlayCurrentUs == currentPts) {
                    checkAudioPtsCount++;
                    if(checkAudioPtsCount == 10){
                        if (DEBUG)
                            Log.d(TAG,"isMusicEnded  currentPts="+currentPts+" prePlayCurrentUs="+prePlayCurrentUs);
                        if (DEBUG)
                            Log.d(TAG,"Checked mPlayCurrentUs 10 times, no change");
                        return true;
                    }
                }
                else {
                    checkAudioPtsCount = 0;
                    prePlayCurrentUs = currentPts;
                }

            }
            return false;
        }

        long getAudioPts(){
            long pts;
            String[] parts ;
            AudioManager audioManager = mContext.getSystemService(AudioManager.class);
            String tmp=audioManager.getParameters("get_audio_pts_by_filter_id_"+mAudioFilterId);
//            for (i = 0; i < recoordHeadTailList.size(); i++) {
//                if(i ==0){
//                    Log.d(TAG,"Allen_test### Start Pts="+recoordHeadTailList.get(i)[3]);
//                }
//                if(i == (recoordHeadTailList.size()-1)){
//                    Log.d(TAG,"Allen_test### End Pts="+recoordHeadTailList.get(i)[4]);
//                }
//            }
            if (tmp == null || tmp.isEmpty()) {
                if (DEBUG)
                    Log.w(TAG, "tmp is null or empty!");
                return 0;
            }

            if (!tmp.contains("=")) {
                if (DEBUG)
                    Log.w(TAG, "tmp format error, no '=' found!");
                return 0;
            }

            if (DEBUG)
                Log.d(TAG,"Audio mAudioFilterId="+mAudioFilterId+" tmp="+tmp);
            parts = tmp.split("="); // 以 "=" 拆分字串
            if (parts.length > 1) {
                try {
                    pts = Long.parseLong(parts[1].trim());
                    pts = (long)((pts / 90000.0) * 1000000);
                } catch (NumberFormatException e) {
                    if (DEBUG)
                        Log.d(TAG,"Not a valid number format");
                    pts = 0;
                }
            }
            else
                pts = 0;
            if (DEBUG)
                Log.d(TAG,"Audio pts="+pts);
            return pts;
        }


        int CheckChangeFileBySeek(long pts) {
            if (DEBUG)
                Log.d(TAG, "PvrReadThread pts =" + pts + " pts=" + pts);
            if (DEBUG)
                Log.d(TAG, "PvrReadThread recoordHeadTailList.get(mCurrentFileIndex)[3])=" + recoordHeadTailList.get(mCurrentFileIndex)[3]);
            if (DEBUG)
                Log.d(TAG, "PvrReadThread recoordHeadTailList.get(mCurrentFileIndex)[4])=" + recoordHeadTailList.get(mCurrentFileIndex)[4]);
            if ((pts < recoordHeadTailList.get(mCurrentFileIndex)[3]) && (pts != 0)) {
                return -1;
            }
            if ((pts > recoordHeadTailList.get(mCurrentFileIndex)[4]) && (mCurrentFileIndex < (recoordHeadTailList.size() - 1))) {
                return 1;
            }
            return 0;
        }

        void ChangePlayFile(boolean isNext, long pts) {
            long fileLengthTmp = 0;
            if (isNext == true) {
                //ibaseTmp = ibaseTmp + mFilelength;
                mCurrentFileIndex++;
                if (DEBUG)
                    Log.d(TAG, "PvrReadThread Switch to the next file");
            } else {
                if ((mCurrentFileIndex - 1) == 0)
                    fileLengthTmp = getFileLength(originalFilePath);
                else
                    fileLengthTmp = getFileLength(originalFilePath + "_INDEX_" + mCurrentFileIndex);
                //ibaseTmp = ibaseTmp - fileLengthTmp;
                mCurrentFileIndex--;
                if (DEBUG)
                    Log.d(TAG, "PvrReadThread switch to the previous file");
            }

            closeFileFile();
            dvrFile = null;
            mTotalReadSize = 0;
            mFilelength = 0;
            if (mCurrentFileIndex == 0) {
                filePath = originalFilePath;
            } else {
                filePath = originalFilePath + "_INDEX_" + mCurrentFileIndex;
            }
            if (speed < 0) {
                mTotalReadSize = fileLengthTmp;
            }
            if (DEBUG)
                Log.d(TAG, "PvrReadThread need change file, mCurrentFileIndex=" + mCurrentFileIndex + " filePath=" + filePath + " fileInfoPath=" + fileInfoPath);
            //if(DEBUG) Log.d(TAG, "PvrReadThread ibaseTmp=" + ibaseTmp);
            manager1 = fileDataListManager.findNearestPtsInData(pts, true);
        }

        @Override
        public void run() {
            Log.d(TAG, "PvrReadThread pvr read Thread enter..");
            InitPlaybackInfo(fileDataListManager);
            while (dvrStop) {
                try {
                    Thread.currentThread().sleep(100);////40);
                    //fastspeedMs+=mSleepTime;
                } catch (InterruptedException e) {
                    // TODO: handle exception
                    Log.d(TAG, "PvrReadThread InterruptedException, e=" + e);
                }
            }
            /*
            recoordHeadTailList[0]  recoordHeadTailList[1]  recoordHeadTailList[2]  recoordHeadTailList[3]  recoordHeadTailList[4]
            Head:getIbase	        Tail:getIbase           fileSize                Head:getiPts            Tail:getiPts
             */

            //InitPlaybackInfo(fileDataListManager);

            recoordHeadTailList = InitHeadTailPtsList(fileDataListManager);
            //ibaseTmp=0;
            tmpData = recoordHeadTailList.size();
            if (tmpData == 0) {
                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_ERROR, 0, 0, null);
                //Log.d(TAG,"PESI_SVR_EVT_PVR_PLAY_EOF, speed="+speed);
                while (!dvrStop) {
                    Log.d(TAG, "PvrReadThread recoordHeadTailList.size()=0");
                    try {
                        Thread.currentThread().sleep(500);////40);
                    } catch (InterruptedException e) {
                        // TODO: handle exception
                    }
                }
                return;
            }

            //mWrapAroundMax = (int) (recoordHeadTailList.get(tmpData-1)[4]/ DvrRecorderController.PTS_MAX);

            //mLastPosition = 38*60*60;

            if (mLastPosition == 0) {
                manager1 = fileDataListManager.findNearestPtsInData(recoordHeadTailList.get(0)[3], true);
                //mWrapAroundCnt = 0;
            } else {
                int i;
                tmpPts = (mLastPosition * 1000L * 1000L) + recoordHeadTailList.get(0)[3];
                Log.d(TAG, "PvrReadThread mLastPosition="+mLastPosition+" recoordHeadTailList.get(0)[3]="+recoordHeadTailList.get(0)[3]);

                for (i = 0; i < recoordHeadTailList.size(); i++) {
                    //ibaseTmp=ibaseTmp+recoordHeadTailList.get(i)[2];
                    if ((tmpPts >= recoordHeadTailList.get(i)[3]) && (tmpPts <= recoordHeadTailList.get(i)[4])) {
                        break;
                }
                }
                if (i == recoordHeadTailList.size()) {
                    Log.d(TAG, "PvrReadThread tmpPts out of range, tmpPts="+tmpPts);
                    mLastPosition = 0;
                    manager1 = fileDataListManager.findNearestPtsInData(recoordHeadTailList.get(0)[3], true);
                    //ibaseTmp=0;
                    mCurrentFileIndex = 0;
                } else {
                    mCurrentFileIndex = i;
                    manager1 = fileDataListManager.findNearestPtsInData(tmpPts, true);
                    Log.d(TAG, "PvrReadThread (tmpPts >= recoordHeadTailList.get("+i+")[3]) && (tmpPts <= recoordHeadTailList.get("+i+")[4])");
                    Log.d(TAG, "PvrReadThread tmpPts="+tmpPts+" mCurrentFileIndex="+mCurrentFileIndex);
                    Log.d(TAG, "PvrReadThread recoordHeadTailList.get("+i+")[3])"+recoordHeadTailList.get(0)[3]);
                    Log.d(TAG, "PvrReadThread recoordHeadTailList.get("+i+")[4])"+recoordHeadTailList.get(0)[4]);

                    //mLastPosition=(int)(tmpPts/1000000);
                    //ibaseTmp=ibaseTmp-recoordHeadTailList.get(i)[2];
                    setPlayStartUs(recoordHeadTailList.get(0)[3]);
                }
                //mWrapAroundCnt = (int) (tmpPts / DvrRecorderController.PTS_MAX);
            }

            if (mCurrentFileIndex == 0) {
                filePath = originalFilePath;
                fileInfoPath = filePath + DvrRecordInfo.FileInfoExtension;
            } else {
                filePath = originalFilePath + "_INDEX_" + mCurrentFileIndex;
                fileInfoPath = filePath + DvrRecordInfo.FileInfoExtension;
            }

            DvrRecordInfo.FileParameters tmpFileParameters = null;

            if (mVideoMediaCodec != null)
                PlaybackReadSize = readSize;
            else
                PlaybackReadSize = RadioReadSize;

            if (DEBUG)
                Log.d(TAG, "PvrReadThread dvrStop:" + dvrStop + " mIsPause:" + mIsPause + " mDvrPlayback:" + mDvrPlayback);
            long StartTimeMillis = 0;
            StartTimeMillis = getElapsedMillis();
            while (!dvrStop) {
                if(mIsPlayingRecording == true) {
                    if (currentTimeMillis + upTimeMs < getElapsedMillis()) {
                        if (DEBUG)
                            Log.d(TAG, "PvrReadThread untilUpdatePlaybackInfo start");
                        untilUpdatePlaybackInfo(fileDataListManager,recoordHeadTailList);
                        Log.d(TAG, "PvrReadThread fileDataListManager="+fileDataListManager);
                        Log.d(TAG, "PvrReadThread recoordHeadTailList="+recoordHeadTailList);
                        if (DEBUG)
                            Log.d(TAG, "PvrReadThread untilUpdatePlaybackInfo end");
                        //currentTimeMillis = System.currentTimeMillis();
                        if(mIsPlayingRecording == true && speed > 2){
                            manager1 = fileDataListManager.findNearestPtsInData(targetPts, true);
                        }
                        currentTimeMillis = getElapsedMillis();
                    }
                }
                if (mStepForward != 0) {
                    if (DEBUG)
                        Log.d(TAG, "PvrReadThread mStepForward=" + mStepForward + " mIsPause:" + mIsPause + " mLastPosition:" + mLastPosition);
                }
                if (mStepForward == 2) {
                    try {
                        Thread.currentThread().sleep(1000);////40);
                    } catch (InterruptedException e) {
                        // TODO: handle exception
                    }
                    if (DEBUG)
                        Log.d(TAG, "PvrReadThread mStepForward=2 ==> StepIframeStop()");
                    StepIframeStop();
                    mStepForward = 0;
                    mPtsFlagUs = 0;
                    mSeekTimeUs = 0;
                    pauseLoop=0;
                }
                if(mVideoMediaCodec == null && mIsPause == false) {
                    long AudioPts;
                    AudioPts = getAudioPts();
                    if(AudioPts == 0) {
                        if (DEBUG)
                            Log.d(TAG, "PvrReadThread First Pts=" + recoordHeadTailList.get(0)[3] + " Final Pts=" + recoordHeadTailList.get(0)[4]);
                    }
                    if(AudioPts != 0){
                        if(mPlayStartUs == 0) {
                            mPlayStartUs = AudioPts;
                        }
                        mPlayCurrentUs = AudioPts;
                        if(isMusicEnded(AudioPts)){
                            mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_EOF, 0, 0, null);
                            if (DEBUG)
                                Log.d(TAG, "PvrReadThread Music PESI_SVR_EVT_PVR_PLAY_EOF");
                            try {
                                Thread.currentThread().sleep(100);////40);
                                fastspeedMs = 0;
                            } catch (InterruptedException e) {
                                // TODO: handle exception
                            }
                        }
                    }
                }

                if (mDvrPlayback != null && (mIsPause == false || (mStepForward == 1 && mLastPosition != 0))) {
                    if ((SeekNumber != 0 || mLastPosition != 0) && mPlayStartUs != 0) {
                        if (mLastPosition != 0 && mPlayStartUs != 0) {
                            targetPts = mPlayStartUs + (mLastPosition) * 1000L * 1000L;
                            if (DEBUG)
                                Log.d(TAG, "PvrReadThread mLastPosition =" + mLastPosition + " mPlayStartUs=" + mPlayStartUs + " targetPts=" + targetPts);
                            mLastPosition = 0;
                        } else {
                            //targetPts = mPresentationTimeUs + (SeekNumber) * 5 * 1000 * 1000;
                            continue;
                        }

                        switch (CheckChangeFileBySeek(targetPts)) {
                            case 1:
                                if (DEBUG)
                                    Log.d(TAG, "PvrReadThread CheckChangeFileBySeek(targetPts) = 1");
                                ChangePlayFile(true, targetPts);
                                break;
                            case -1: // 負數作為 case 標籤
                                if (DEBUG)
                                    Log.d(TAG, "PvrReadThread CheckChangeFileBySeek(targetPts) = -1");
                                ChangePlayFile(false, targetPts);
                                break;
                            default:
                                if (DEBUG)
                                    Log.d(TAG, "PvrReadThread CheckChangeFileBySeek(targetPts) = 0");
                                break;
                        }
                        if(manager1 == null){
                            PlaybackReadSize = readSize;
                            mTotalReadSize = 0;
                            Log.e(TAG, "PvrReadThread manager1 is null, don't seek");
                            continue;
                        }
                        tmpFileParameters = manager1.findNearestPtsInData(targetPts, true);
                        if (tmpFileParameters != null) {
                            mPtsFlagUs = tmpFileParameters.getiPts();
                            mSeekTimeUs = mPtsFlagUs - mPlayCurrentUs;
                            if (DEBUG)
                                Log.d(TAG, "PvrReadThread mSeekTimeUs=" + mSeekTimeUs + " mPtsFlagUs=" + mPtsFlagUs);
                            mTotalReadSize = tmpFileParameters.getIbase();
                            if (mVideoMediaCodec != null) {
                                if (mStepForward == 1) {
                                    if (DEBUG)
                                        Log.d(TAG, "PvrReadThread mStepForward=1 ==> StepIframeStart()");
                                    mScanCurrentUs = tmpFileParameters.getiPts();
                                    StepIframeStart();
                                    PlaybackReadSize = tmpFileParameters.getiFrameSize();
                                    pauseLoop = 20;
                                    if (DEBUG)
                                        Log.d(TAG, "PvrReadThread mScanCurrentUs="+targetPts+" PlaybackReadSize="+PlaybackReadSize+" pauseLoop="+pauseLoop);
                                } else
                                    PlaybackReadSize = readSize;
                            } else {
                                PlaybackReadSize = RadioReadSize;
                            }
                            tmpFileParameters = null;
                            flushDvrPlayback();
                        } else {
                            if (DEBUG)
                                Log.d(TAG, "PvrReadThread tmpFileParameters is null, targetPts=" + targetPts);
                        }
                        SeekNumber = 0;
                        targetPts = 0;
                        if (DEBUG)
                            Log.d(TAG, "PvrReadThread Seek mTotalReadSize=" + mTotalReadSize + " PlaybackReadSize=" + PlaybackReadSize);
                    }
                    //else if(speed>2){
                    else if (speed > 2 || speed < 0) {
                        long currentPts;
                        if (DEBUG)
                            Log.d(TAG, "PvrReadThread Scarch targetPts start, speed=" + speed);
                        if (targetPts != 0) {
                            currentPts = targetPts;
                            targetPts = currentPts + (long) (fastspeedMs * speed * 1000);
                            delayTimeMs = System.currentTimeMillis();
                            fastspeedMs = 0;
                            targetPts = currentPts + (long) ((delayTimeMs - startTimeMs) * speed * 1000);
                            startTimeMs = delayTimeMs;
                        } else {//roll
                            currentPts = mScanCurrentUs;//mPresentationTimeUs;
                            targetPts = currentPts;
                            startTimeMs = System.currentTimeMillis();
                            fastspeedMs = 0;
                            switchingToScanMode = true;
                        }

                        //Log.d(TAG, "Tail:getiPts="+recoordHeadTailList.get(mCurrentFileIndex)[4]);
                        if (DEBUG)
                            Log.d(TAG, "check EOF or SOF or Change File start");
                        if (recoordHeadTailList.size() > 0) {
                            if (((speed > 2) && (targetPts > recoordHeadTailList.get(mCurrentFileIndex)[4])) ||
                                    ((speed < 0) && (targetPts < recoordHeadTailList.get(mCurrentFileIndex)[3]))) {
                                int tmpFileIndex;
                                if (speed > 2 && mCurrentFileIndex == (recoordHeadTailList.size() - 1)) {
                                    if(mIsPlayingRecording == true){
                                        targetPts = recoordHeadTailList.get(mCurrentFileIndex)[4];
                                        mScanCurrentUs = targetPts;
                                        playerResume();
                                        mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_EOF, 0, 0, null);
                                        Log.d(TAG, "PvrReadThread PESI_SVR_EVT_PVR_PLAY_EOF");
                                        //flushDvrPlayback();
                                        try {
                                            Thread.currentThread().sleep(1000);////40);
                                            fastspeedMs = 0;
                                        } catch (InterruptedException e) {
                                            // TODO: handle exception
                                        }
                                        continue;
                                    }
                                    mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_EOF, 0, 0, null);
                                    if (DEBUG)
                                        Log.d(TAG, "PvrReadThread PESI_SVR_EVT_PVR_PLAY_EOF, speed=" + speed);
                                    targetPts = recoordHeadTailList.get(mCurrentFileIndex)[4];
                                    mScanCurrentUs = targetPts;
                                    //playerResume();
                                    flushDvrPlayback();
                                    while (!dvrStop) {
                                        if (DEBUG)
                                        Log.d(TAG, "PvrReadThread PESI_SVR_EVT_PVR_PLAY_EOF");
                                        try {
                                            Thread.currentThread().sleep(100);////40);
                                            fastspeedMs = 0;
                                        } catch (InterruptedException e) {
                                            // TODO: handle exception
                                        }
                                    }
                                    break;
                                } else if (speed < 0 && mCurrentFileIndex == 0) {
                                    mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_SOF, 0, 0, null);
                                    if (DEBUG)
                                        Log.d(TAG, "PvrReadThread PESI_SVR_EVT_PVR_PLAY_SOF, speed=" + speed);
                                    mTotalReadSize = 0;
                                    playerResume();
                                    flushDvrPlayback();
                                    if (DEBUG)
                                        Log.d(TAG, "PvrReadThread PESI_SVR_EVT_PVR_PLAY_SOF, mTotalReadSize=" + mTotalReadSize);
                                    continue;
                                } else {
                                    tmpFileIndex = mCurrentFileIndex;
                                    if ((speed > 2) && (mCurrentFileIndex < (recoordHeadTailList.size() - 1))) {
                                        mCurrentFileIndex++;
                                        if (targetPts < recoordHeadTailList.get(mCurrentFileIndex)[3]) {
                                            targetPts = recoordHeadTailList.get(mCurrentFileIndex)[3];
                                        }
                                    } else if ((speed < 0) && mCurrentFileIndex >= 1) {
                                        mCurrentFileIndex--;
                                        if (targetPts > recoordHeadTailList.get(mCurrentFileIndex)[4]) {
                                            targetPts = recoordHeadTailList.get(mCurrentFileIndex)[4];
                                        }
                                    }
                                    if (DEBUG)
                                        Log.d(TAG, "PvrReadThread tmpFileIndex=" + tmpFileIndex + " mCurrentFileIndex=" + mCurrentFileIndex);
                                    if (tmpFileIndex != mCurrentFileIndex) {
                                        closeFileFile();
                                        dvrFile = null;
                                        mTotalReadSize = 0;
                                        mFilelength = 0;
                                        if (mCurrentFileIndex == 0) {
                                            filePath = originalFilePath;
                                        } else {
                                            filePath = originalFilePath + "_INDEX_" + mCurrentFileIndex;
                                        }
                                        if(speed < 0){
                                            mTotalReadSize=recoordHeadTailList.get(mCurrentFileIndex)[2];
                                        }

                                        if (DEBUG)
                                            Log.d(TAG, "PvrReadThread need change filePath=" + filePath + " fileInfoPath=" + fileInfoPath);
                                        //if(DEBUG) Log.d(TAG, "PvrReadThread ibaseTmp=" + ibaseTmp);
                                        manager1 = fileDataListManager.findNearestPtsInData(targetPts, true);
                                    }
                                }
                            }
                        }
                        if (DEBUG) Log.d(TAG, "check EOF or SOF or Change File end");
                        if (DEBUG)
                            Log.d(TAG, "manager1.findNearestPtsInData start, targetPts=" + targetPts);
                        if(manager1 == null){
                            manager1 = fileDataListManager.findNearestPtsInData(targetPts, true);
                            if (DEBUG)
                                Log.e(TAG, "PvrReadThread manager1 is null, Regain this variable and continue");
                            continue;
                        }
                        tmpFileParameters = manager1.findNearestPtsInData(targetPts, true);
                        if (DEBUG)
                            Log.d(TAG, "manager1.findNearestPtsInData end, tmpFileParameters=" + tmpFileParameters);


                        if (DEBUG)
                            Log.d(TAG, "PvrReadThread mTotalReadSize=" + mTotalReadSize + " tmpFileParameters.getIbase()=" + tmpFileParameters.getIbase());
                        if (switchingToScanMode == true && speed == 4) {
                            if (mTotalReadSize > tmpFileParameters.getIbase()) {
                                mTotalReadSize = tmpFileParameters.getIbase();
                                switchingToScanMode = false;
                                flushDvrPlayback();
                                if (DEBUG)
                                    Log.d(TAG, "PvrReadThread switchingToScanMode set to false, speed=" + speed + " mTotalReadSize=" + mTotalReadSize);
                            }
                        }
                        if (tmpFileParameters != null &&
                                ((speed > 2 && (tmpFileParameters.getIbase()) >= mTotalReadSize) ||
                                        (speed < 0 && (tmpFileParameters.getIbase()) <= mTotalReadSize))) {

                            PlaybackReadSize = tmpFileParameters.getiFrameSize();

                            mTotalReadSize = tmpFileParameters.getIbase();
                            mScanCurrentUs = targetPts;

                            if (mSleepTime == 150) {
                                if (DEBUG)
                                    Log.d(TAG, "PvrReadThread mSleepTime is 150==>flushDvrPlayback()");
                                flushDvrPlayback();
                                mSleepTime = 100;
                            }
                            if (DEBUG)
                                Log.d(TAG, "PvrReadThread need read file PlaybackReadSize=" + PlaybackReadSize + " mTotalReadSize=" + mTotalReadSize);
                        } else {
                            if (DEBUG)
                                Log.d(TAG, "PvrReadThread tmpFileParameters is null, need to sleep, mSleepTime=" + mSleepTime);
                            try {
                                int delay_us = mSleepTime;
                                Thread.currentThread().sleep(delay_us);
                                fastspeedMs += delay_us;
                            } catch (InterruptedException e) {
                                // TODO: handle exception
                            }
                            continue;
                        }
                    } else {
                        targetPts = 0;
                        if (mVideoMediaCodec == null) {
                            PlaybackReadSize = RadioReadSize;
                        } else {
                            if (PlaybackReadSize != readSize) {
                                PlaybackReadSize = readSize;
                            }
                        }
                    }
                    if (DEBUG)
                        Log.d(TAG, "PvrReadThread openFileRead start, mTotalReadSize=" + mTotalReadSize + " filePath=" + filePath);
                    fileReadNumber = openFileRead(dvr_buff, mTotalReadSize, filePath, (int) PlaybackReadSize, speed);

                    PlaybackReadSizeTmp=PlaybackReadSize;
                    if (speed > 2.0 || speed <= 0) {
                        PlaybackReadSizeTmp = fileReadNumber;
                    }

                    if (DEBUG)
                        Log.d(TAG, "PvrReadThread PvrReadThread openFileRead end, fileReadNumber=" + fileReadNumber);

                    if (DEBUG)
                        Log.d(TAG, "PvrReadThread openFileRead, mTotalReadSize=" + mTotalReadSize + " fileReadNumber=" + fileReadNumber + " filePath=" + filePath);

                    if (fileReadNumber > 0) {
                        if (DEBUG) Log.d(TAG, "PvrReadThread playbackOptionLock start");
                        playbackOptionLock.lock();
                        if (fileReadNumber >= PlaybackReadSize) {
                            if (DEBUG)
                                Log.d(TAG, "PvrReadThread mDvrPlayback.read start, PlaybackReadSize=" + PlaybackReadSize);
                            if(pauseLoop != 0) {
                                while(pauseLoop > 0){
                                    if (DEBUG) Log.d(TAG, "PvrReadThread mDvrPlayback.read start, PlaybackReadSize=" + PlaybackReadSize+ "pauseLoop="+pauseLoop);
                                    ret = mDvrPlayback.read(dvr_buff, 0, PlaybackReadSizeTmp);
                                    pauseLoop --;
                                }
                            }
                            else {
                                ret = mDvrPlayback.read(dvr_buff, 0, PlaybackReadSizeTmp);
                            }
                            if (DEBUG)
                                Log.d(TAG, "PvrReadThread mDvrPlayback.read end, ret=" + ret);
                        } else {
                            if (DEBUG)
                                Log.d(TAG, "PvrReadThread mDvrPlayback.read start, fileReadNumber=" + fileReadNumber);
                            ret = mDvrPlayback.read(dvr_buff, 0, (fileReadNumber / 188) * 188);

                            if (DEBUG)
                                Log.d(TAG, "PvrReadThread mDvrPlayback.read end, ret=" + ret);
                        }
                        playbackOptionLock.unlock();
                        if (DEBUG) Log.d(TAG, "PvrReadThread playbackOptionLock end");
                        if (ret > 0) {
                            if(PlaybackReadSizeTmp == fileReadNumber && (speed > 2.0 || speed <= 0))
                                mTotalReadSize = mTotalReadSize + PlaybackReadSize;
                            else
                                mTotalReadSize = mTotalReadSize + ret;
                            fastspeedMs = 0;
                            //Log.d(TAG,"fastspeedMs set to 0, ret="+ret);
                        }
                        if (DEBUG)
                            Log.d(TAG, "PvrReadThread mDvrPlayback.read, ret=" + ret + " mTotalReadSize=" + mTotalReadSize + " mFilelength=" + mFilelength);
                    } else {
                        //if(speed < 0)
                        //Log.d(TAG,"fileReadNumber < 0");
                        if (DEBUG)
                            Log.d(TAG, "PvrReadThread fileReadNumber=" + fileReadNumber + " mTotalReadSize=" + mTotalReadSize + " mFilelength=" + mFilelength);
                    }
                    if (mStepForward == 1) {
                        mStepForward = 2;
                    }
                }

                if (DEBUG)
                    Log.d(TAG, "PvrReadThread check EOF or Change File start");
                if (mRecordMode == DvrRecorderController.RECORDERMODE_RECORD) {//&&(mTotalReadSize >= mFilelength - 187) && (isNeedRepeat) && (speed>0)){
                    if (speed > 0) {
                        boolean changeFile = false;
                        if ((isNeedRepeat) && (mTotalReadSize >= mFilelength - 187)) {
                            if (speed <= 2) {
                                int recoordHeadTailListSize = recoordHeadTailList.size();
                                if ((recoordHeadTailListSize > 0) && mCurrentFileIndex < (recoordHeadTailListSize - 1))
                                    changeFile = true;
                                if (changeFile) {
                                    if (DEBUG)
                                        Log.d(TAG, "PvrReadThread need change file, speed=" + speed);
                                    closeFileFile();
                                    mCurrentFileIndex++;
                                    dvrFile = null;
                                    mTotalReadSize = 0;
                                    mFilelength = 0;
                                    filePath = originalFilePath + "_INDEX_" + mCurrentFileIndex;
                                    manager1 = fileDataListManager.findNearestPtsInData(recoordHeadTailList.get(mCurrentFileIndex)[3], true);
                                }
                                else {
                                     if (mCurrentFileIndex == (recoordHeadTailList.size() - 1)) {//mVideoMediaCodec != null
                                        if (mVideoMediaCodec == null) {
                                            if (DEBUG)
                                                Log.d(TAG, "PvrReadThread Radio PESI_SVR_EVT_PVR_PLAY_EOF");
                                            try {
                                                Thread.currentThread().sleep(100);////40);
                                            } catch (InterruptedException e) {
                                                // TODO: handle exception
                                            }
                                        }
                                        else if(mIsPlayingRecording == true){
                                            if ((mNormalReocrd.getRecordTime() - getCurrentPlayTimeSec() <= 3)) {
                                                playerResume();
                                                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_EOF, 0, 0, null);
                                                Log.d(TAG, "PvrReadThread Video PESI_SVR_EVT_PVR_PLAY_EOF");
                                                try {
                                                    Thread.currentThread().sleep(1000);////40);
                                                } catch (InterruptedException e) {
                                                    // TODO: handle exception
                                                }
                                                continue;
                                            }
                                        }
                                        else {
                                            if ((mTotalRecTimeSec - getCurrentPlayTimeSec() <= 3)) {
                                                mTotalReadSize = 0;
                                                fastspeedMs = 0;
                                                try {
                                                    if (speed == 2)
                                                        Thread.currentThread().sleep(2000);
                                                    else
                                                        Thread.currentThread().sleep(3000);
                                                } catch (InterruptedException e) {
                                                    // TODO: handle exception
                                                }
                                                playerResume();
                                                flushDvrPlayback();
                                                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_EOF, 0, 0, null);
                                                while (!dvrStop) {
                                                    Log.d(TAG, "PvrReadThread Video PESI_SVR_EVT_PVR_PLAY_EOF");
                                                    try {
                                                        Thread.currentThread().sleep(100);////40);
                                                    } catch (InterruptedException e) {
                                                        // TODO: handle exception
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (DEBUG) Log.d(TAG, "PvrReadThread check EOF or Change File end");
                if (DEBUG) Log.d(TAG, "PvrReadThread Thread sleep start, mSleepTime=" + mSleepTime);
                try {
                    Thread.currentThread().sleep(mSleepTime);////40);
                    fastspeedMs += mSleepTime;
                    if (DEBUG)
                        Log.d(TAG, "PvrReadThread mSleepTime=" + mSleepTime + " fastspeedMs=" + fastspeedMs);
                } catch (InterruptedException e) {
                    // TODO: handle exception
                    Log.d(TAG, "PvrReadThread InterruptedException, e=" + e);
                }
                if (DEBUG) Log.d(TAG, "PvrReadThread Thread sleep end");
            }

            Log.d(TAG,"PvrReadThread pvr read Thread exit..");
        }
    }

    public ParcelFileDescriptor openFile(String filePath) {
        if (DEBUG)
            Log.d(TAG, "start play... openFile");
        File file = new File(filePath);
        if (!file.exists()) {
            //Toast.makeText(RtkApplication.getApplication(), "the file not exist ,return!", Toast.LENGTH_SHORT).show();
            if (DEBUG)
                Log.d(TAG, "the file not exist ,return!");
            return null;
        }
        ParcelFileDescriptor pfd;
        try {
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "ParcelFileDescriptor open fail: " + e.getMessage());
            //e.printStackTrace();
            return null;
        }
        return pfd;
    }

    public void closeFileFile() {
        try {
            if (DEBUG) Log.d(TAG, "stop  file...");
            if (InputStream != null) {
                InputStream.close();
            }
            InputStream = null;
        } catch (Exception e) {
            Log.e(TAG, "InputStream close fail: " + e.getMessage());
            //e.printStackTrace();
        }
    }

    public long getFileLength(String filePath) {
        File dvrFileTmp = null;
        long dvrFileTmpLen = 0;
        dvrFileTmp = new File(filePath);
        if (dvrFileTmp.exists() == true) {
            dvrFileTmpLen = dvrFileTmp.length();
        }
        dvrFileTmp = null;
        return dvrFileTmpLen;
    }

    public static byte[] findClosestPusiStart(byte[] dvr_buff, int PesLength, int base_index) {
        //final int TS_PACKET_SIZE = 188;

        //Log.e(TAG, "dvr_buff.length="+dvr_buff.length+" PesLength="+PesLength+" base_index="+base_index+"dvr_buff[base_index]="+dvr_buff[base_index]);
        if (dvr_buff == null || dvr_buff.length < TS_PACKET_SIZE || PesLength <= 0) {
            Log.e(TAG, "dvr_buff == null || dvr_buff.length < TS_PACKET_SIZE || PesLength <= 0");
            return null;
        }

        if (PesLength % TS_PACKET_SIZE != 0) {
            Log.e(TAG, "PesLength is not a multiple of 188, the actual value=" + PesLength);
            return null;
        }

        // 保護 base_index
        if (base_index < 0) base_index = 0;
        if (base_index >= dvr_buff.length) base_index = dvr_buff.length - 1;

        // 若 base_index 本身位於一個 packet header 且 PUSI = 1 (且 header 在範圍內)
        if (base_index + 1 < dvr_buff.length && (dvr_buff[base_index] & 0xFF) == 0x47
                && ((dvr_buff[base_index + 1] & 0x40) != 0)) {
            // 確認剩餘長度足夠複製 PesLength
            if (dvr_buff.length - base_index >= PesLength) {
                //Log.d(TAG, "current base_index is PUSI, base_index="+base_index);
                byte[] result = new byte[PesLength];
                System.arraycopy(dvr_buff, base_index, result, 0, PesLength);
                return result;
            } else {
                Log.e(TAG, "dvr_buff.length="+dvr_buff.length+" base_index="+base_index+" PesLength="+PesLength);
                return null;
        }
            }

        // 向前找上一個 PUSI=1
        int prevPusi = -1;
        for (int i = base_index; i >= 0; i -= TS_PACKET_SIZE) {
            if ((dvr_buff[i] & 0xFF) != 0x47) continue; // 不是 sync byte，跳過
            // 檢查是否為可能的 packet header 並且 PUSI=1
            if (i + 1 < dvr_buff.length && ((dvr_buff[i + 1] & 0x40) != 0)) {
                prevPusi = i;
                //Log.d(TAG, "find previous, prevPusi="+prevPusi);
                break;
                }
            }

        // 向後找下一個 PUSI=1
        int nextPusi = -1;
        for (int i = base_index; i <= dvr_buff.length - TS_PACKET_SIZE; i += TS_PACKET_SIZE) {
            if ((dvr_buff[i] & 0xFF) != 0x47) continue;
            if (i + 1 < dvr_buff.length && ((dvr_buff[i + 1] & 0x40) != 0)) {
                nextPusi = i;
                //Log.d(TAG, "find next, nextPusi="+nextPusi);
                break;
            }
        }

        // 選擇最近的
        int chosen = -1;
        if (prevPusi == -1 && nextPusi == -1) {
            Log.e(TAG, "No PUSI found");
            return null; // 找不到任何 PUSI
        } else if (prevPusi == -1) {
            chosen = nextPusi;
            Log.d(TAG, "AllenTest### chosen = nextPusi");
        } else if (nextPusi == -1) {
            chosen = prevPusi;
            Log.d(TAG, "chosen = prevPusi");
        } else {
            long distPrev = Math.abs((long) base_index - prevPusi);
            long distNext = Math.abs((long) nextPusi - base_index);
            chosen = (distPrev <= distNext) ? prevPusi : nextPusi; // 距離相等時優先前方 (可改)
            }

        // 複製 PesLength
        if (chosen != -1) {
            int remaining = dvr_buff.length - chosen;
            if (remaining < PesLength) {
                // 如果你想要允許回傳短於 PesLength 的資料，可以改為取最小值
                Log.e(TAG, "remaining < PesLength");
                return null;
            }
            byte[] result = new byte[PesLength];
            System.arraycopy(dvr_buff, chosen, result, 0, PesLength);
            //Log.d(TAG, "chosen="+chosen);
            return result;
        }
        Log.e(TAG, "chosen == -1");
        return null;
    }

    public static byte[] findPusiStart(byte[] dvr_buff, int PesLength) {
        if (dvr_buff == null || dvr_buff.length < 188 || PesLength <= 0) {
            return null;
        }

        final int TS_PACKET_SIZE = 188;

        if (PesLength % TS_PACKET_SIZE != 0) {
            Log.e(TAG, "PesLength is not a multiple of 188, the actual value=" + PesLength);
            return null;  // 或 return 空陣列，看你需求
        }

        int index = 0;

        while (index <= dvr_buff.length - TS_PACKET_SIZE) {
            // 尋找 sync byte 0x47
            if ((dvr_buff[index] & 0xFF) != 0x47) {
                index++;
                continue;
            }

            // 嘗試判斷這是不是一個有效的 TS packet 開頭
            boolean isValidPacket = true;
            int nextIndex = index + TS_PACKET_SIZE;
            if (nextIndex < dvr_buff.length && (dvr_buff[nextIndex] & 0xFF) != 0x47) {
                // 下一個 TS packet 不對，說明這個 0x47 可能是資料
                isValidPacket = false;
            }

            if (isValidPacket) {
                // 判斷 PUSI
                boolean pusi = (dvr_buff[index + 1] & 0x40) != 0;
                boolean isScrambled = (dvr_buff[index + 3] & 0xC0) != 0x00;
                Log.d(TAG, "TimeShiftReadThread PUSI="+pusi+" Scrambled="+isScrambled);
                if (pusi) {
                    // 找到 PUSI=1
                    int remaining = dvr_buff.length - index;
                    int lengthToCopy = Math.min(PesLength+188, remaining);
                    byte[] result = new byte[lengthToCopy];
                    System.arraycopy(dvr_buff, index, result, 0, lengthToCopy);
                    return result;
                } else {
                    // 不是 PUSI=1，跳到下一個 packet 開頭
                    index += TS_PACKET_SIZE;
                }
            } else {
                // 這個 0x47 不是 packet 開頭，繼續從下一個 byte 搜尋
                index++;
            }
        }
        return null; // 找不到 PUSI=1
    }

    public static byte[] findPusiStartAuto(byte[] dvr_buff) {
        if (dvr_buff == null || dvr_buff.length < 188) {
            return null;
        }

        final int TS_PACKET_SIZE = 188;
        int index = 0;
        int startIndex = -1;

        while (index <= dvr_buff.length - TS_PACKET_SIZE) {
            // 找 sync byte 0x47
            if ((dvr_buff[index] & 0xFF) != 0x47) {
                index++;
                continue;
            }

            // 確認這真的是TS packet (下一個+188可能是0x47)
            boolean isValidPacket = true;
            int nextIndex = index + TS_PACKET_SIZE;
            if (nextIndex < dvr_buff.length && (dvr_buff[nextIndex] & 0xFF) != 0x47) {
                isValidPacket = false;
            }

            if (isValidPacket) {
                boolean pusi = (dvr_buff[index + 1] & 0x40) != 0;
                if (pusi) {
                    if (startIndex == -1) {
                        // 第一次找到 PUSI=1 → 這是 PES 起點
                        startIndex = index;
                    } else {
                        // 第二次找到 PUSI=1 → 結束點
                        int pesLength = index - startIndex;
                        if (pesLength > 0) {
                            byte[] result = new byte[pesLength];
                            System.arraycopy(dvr_buff, startIndex, result, 0, pesLength);
                            return result;
                        }
                    }
                }
                index += TS_PACKET_SIZE;
            } else {
                index++;
            }
        }

        // 若只找到一個 PUSI=1 而沒找到下一個，就取到 buffer 結尾
        if (startIndex != -1) {
            int remaining = dvr_buff.length - startIndex;
            byte[] result = new byte[remaining];
            System.arraycopy(dvr_buff, startIndex, result, 0, remaining);
            return result;
        }

        return null;
    }

    public int openFileRead(byte b[], long Offset, String filePath, int ReadSize, float speed) {
        int err = -1;
        int readSize = 1024 * 1024 * 5;
        long tmpOffset = 0,finalOffset=0;
        boolean useRealData=false;
        byte realData[];
        if (DEBUG)
            Log.d(TAG, "openFileRead start, Offset="+Offset+" ReadSize="+ReadSize+" speed="+speed+" filePath=" + filePath);

        if (mRecordMode == DvrRecorderController.RECORDERMODE_RECORD && mVideoMediaCodec == null){
            readSize = (TS_PACKET_SIZE*20);
        }

        if (speed > 2.0 || speed <= 0) {
            if (mRecordMode == DvrRecorderController.RECORDERMODE_TIMEShIFT) {
                readSize = ReadSize + OFFSET_TOLERANCE * 2;
                if (Offset > OFFSET_TOLERANCE)
                    tmpOffset = Offset - OFFSET_TOLERANCE;
                else
                    tmpOffset = Offset;
                if (USE_ACCURATE_DATA == true)
                    useRealData = true;
            }
            else{
                tmpOffset = 0;
                readSize = ReadSize;
            }
        }
        if (dvrFile == null) {
            if (DEBUG) Log.d(TAG, "openFileRead dvrFile is null,new File=>filePath: " + filePath);
            dvrFile = new File(filePath);
        }
        if (dvrFile.exists() == false) {
            if (DEBUG)
                Log.d(TAG, "the file not exist ,return! + " + dvrFile.getAbsolutePath().toString());
            return -2;
        }
        mFilelength = dvrFile.length();
        //Log.d(TAG, "openFileRead  filePath: "+filePath+" mFilelength: "+mFilelength);
        if (InputStream == null) {
            if (DEBUG) Log.d(TAG, "openFileRead InputStream ==null");
            try {
                if (DEBUG) Log.d(TAG, "openFileRead RandomAccessFile start");
                InputStream = new RandomAccessFile(dvrFile, "r");
                if (DEBUG) Log.d(TAG, "openFileRead RandomAccessFile end");
            } catch (FileNotFoundException e) {
                Log.e(TAG, "RandomAccessFile fail: " + e.getMessage());
                //e.printStackTrace();
            }
        }

        finalOffset = (tmpOffset != 0) ? tmpOffset : Offset;

        try {
            if (mFilelength > finalOffset) {
                if (DEBUG)
                    Log.d(TAG, "openFileRead InputStream.seek start, finalOffset=" + finalOffset + " speed=" + speed);
                InputStream.seek(finalOffset);
                if (DEBUG) Log.d(TAG, "openFileRead InputStream.seek end");
                if (DEBUG)
                    Log.d(TAG, "openFileRead InputStream.read start readSize=" + readSize);
                err = InputStream.read(b, 0, readSize);
                if (DEBUG)
                    Log.d(TAG, "openFileRead InputStream.read end, err=" + err);
            } else {
                if (DEBUG)
                    Log.d(TAG, "openFileRead mFilelength <= finalOffset, don't InputStream.read. mFilelength: " + mFilelength + " finalOffset: " + finalOffset);
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException fail: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException fail: " + e.getMessage());
        }

        if (DEBUG) Log.d(TAG, "openFileRead end");
        if(useRealData && err!=-1){
            //realData = findPusiStart(b,ReadSize);
            //realData = findPusiStartAuto(b);

            if(tmpOffset == Offset) {
                realData = findClosestPusiStart(b, ReadSize, 0);
            }
            else {
                realData = findClosestPusiStart(b, ReadSize, OFFSET_TOLERANCE);
            }

            if(realData != null){
                err=realData.length;
                System.arraycopy(realData, 0, b, 0, realData.length);
                realData=null;
            }
            else {
                err=0;
                if (DEBUG) Log.d(TAG, "openFileRead findClosestPusiStart failed, Can't find data, speed="+speed);
            }
        }

        return err;
    }

    private boolean untilNeedChangeFile(Boolean next) {
        boolean err = false;
        if (next)
            err = untilCheckNeedUseNextPath();
        else
            err = untilCheckNeedUsePreviousPath();
        return err;
    }

    private boolean untilCheckNeedUseNextPath() {
        boolean err = false;
        if (DEBUG)
            Log.d(TAG, "untilCheckNeedUseNextPath DvrRecorderController.mLockFileIndexLock.lock()");
        DvrRecorderController.mLockFileIndexLock.lock();

        if (DvrRecorderController.mInfosFileIndexList != null && DvrRecorderController.mInfosFileIndexList.size() > 0) {
            if (mCurrentFileIndex == DvrRecorderController.mInfosFileIndexList.get(DvrRecorderController.mInfosFileIndexList.size() - 1)) {
                if (DEBUG) Log.d(TAG, "file mCurrentFileIndex is end:: " + mCurrentFileIndex);
                err = false;
            } else if (DvrRecorderController.mInfosFileIndexList.contains(mCurrentFileIndex + 1)
                    && dvrFile != null && mTotalReadSize >= dvrFile.length()) {
                mCurrentFileIndex++;
                if (DEBUG) Log.d(TAG, "find next file mCurrentFileIndex::" + mCurrentFileIndex);
                err = true;
            } else if (mCurrentFileIndex + 1 <= DvrRecorderController.mInfosFileIndexList.get(0)) {
                if (DEBUG)
                    Log.d(TAG, "find next file error! use index 0 mCurrentFileIndex::" + mCurrentFileIndex);
                mCurrentFileIndex = DvrRecorderController.mInfosFileIndexList.get(0);
                err = true;
            }
            DvrRecorderController.mLockFileIndex = mCurrentFileIndex;
        }
        DvrRecorderController.mLockFileIndexLock.unlock();
        if (DEBUG)
            Log.d(TAG, "untilCheckNeedUseNextPath DvrRecorderController.mLockFileIndexLock.unlock()");
        return err;
    }

    private boolean untilCheckNeedUsePreviousPath() {
        boolean err = false;
        DvrRecorderController.mLockFileIndexLock.lock();
        if (DvrRecorderController.mInfosFileIndexList != null && DvrRecorderController.mInfosFileIndexList.size() > 0) {
            if (mCurrentFileIndex == DvrRecorderController.mInfosFileIndexList.get(0)) {
                if (DEBUG) Log.d(TAG, "file mCurrentFileIndex is end:: " + mCurrentFileIndex);
                err = false;
            } else if (DvrRecorderController.mInfosFileIndexList.contains(mCurrentFileIndex - 1)) {
                mCurrentFileIndex--;
                if (DEBUG) Log.d(TAG, "find Previous file mCurrentFileIndex::" + mCurrentFileIndex);
                err = true;
            } else if (mCurrentFileIndex - 1 >= DvrRecorderController.mInfosFileIndexList.get(DvrRecorderController.mInfosFileIndexList.size() - 1)) {
                if (DEBUG)
                    Log.d(TAG, "find Previous file error! use index 0 mCurrentFileIndex::" + mCurrentFileIndex);
                mCurrentFileIndex = DvrRecorderController.mInfosFileIndexList.get(DvrRecorderController.mInfosFileIndexList.size() - 1);
                err = true;
            }
            DvrRecorderController.mLockFileIndex = mCurrentFileIndex;
        }
        DvrRecorderController.mLockFileIndexLock.unlock();
        return err;
    }

    private boolean untilFindFileCurrentFileIndex(long ms) {
        DvrRecorderController.mLockFileIndexLock.lock();
        if (!(DvrRecorderController.mInfosFileIndexList != null && DvrRecorderController.mInfosFileIndexList.size() > 2)) {
            DvrRecorderController.mLockFileIndexLock.unlock();
            return false;
        }

        for (Map.Entry<Integer, Long> entry : DvrRecorderController.mFileIndexMapTime.entrySet()) {
            int key = entry.getKey();
            long value = entry.getValue();
            if (DEBUG) Log.d(TAG, "Key: " + key + ", Value: " + value);
        }
        if (DEBUG)
            Log.d(TAG, " untilFindFileCurrentFileInde DvrRecorderController.mInfosFileIndexList.get(0)::" + DvrRecorderController.mInfosFileIndexList.get(0));

        DvrRecorderController.mLockFileIndex = DvrRecorderController.mInfosFileIndexList.get(0);
        long fileStartTime = DvrRecorderController.mFileIndexMapTime.get(DvrRecorderController.mInfosFileIndexList.get(0));
        long fileEndTime = DvrRecorderController.mFileIndexMapTime.get(DvrRecorderController.mInfosFileIndexList.get(DvrRecorderController.mInfosFileIndexList.size() - 1));
        long time = 0;
        if (ms >= fileStartTime && ms <= fileEndTime) {
            for (Integer index : DvrRecorderController.mInfosFileIndexList) {
                time = DvrRecorderController.mFileIndexMapTime.get(index);
                if (time >= ms) {
                    if (DEBUG) Log.d(TAG, "seek to mCurrentFileIndex:: " + mCurrentFileIndex);
                    mCurrentFileIndex = index;
                    break;
                }
            }
        } else {
            if (DEBUG) Log.d(TAG, "seek ms error seek to 0");
            mCurrentFileIndex = DvrRecorderController.mInfosFileIndexList.get(0);
        }
        DvrRecorderController.mLockFileIndex = mCurrentFileIndex;
        DvrRecorderController.mLockFileIndexLock.unlock();
        return true;
    }

    private boolean untilSetFileCurrentFileIndex(String path) {
        boolean ret;
        DvrRecorderController.mLockFileIndexLock.lock();
        try {
            int startIndex = path.indexOf("_INDEX_");
            String afterIndex = path.substring(startIndex + "_INDEX_".length());
            mCurrentFileIndex = Integer.parseInt(afterIndex);
            DvrRecorderController.mLockFileIndex = mCurrentFileIndex;
            ret = true;
        } catch (Exception e) {
            ret = false;
            e.printStackTrace();
        }

        DvrRecorderController.mLockFileIndexLock.unlock();
        return ret;
    }

    private boolean untilUpdateTimeShiftInfo(DvrRecordInfo.FileDataListManager manger) {
        if (manger.FileDataManagerList == null)
            return false;

        DvrRecorderController.mLockFileIndexLock.lock();
        if (DEBUG) Log.d(TAG, "untilUpdateTimeShiftInfo start process");
        try {
            if (manger.FileDataManagerList.isEmpty()) {
                //init
                if (DEBUG)
                    Log.d(TAG, "untilUpdateTimeShiftInfo manger.FileDataManagerList.isEmpty() == true");
                manger.FileDataManagerList.clear();
                for (Integer list_index : DvrRecorderController.mInfosFileIndexList) {
                    String basePath = originalFilePath + "_INDEX_" + list_index;
                    String filePath = basePath + DvrRecordInfo.FileInfoExtension;
                    DvrRecordInfo.FileDataManager manager = new DvrRecordInfo.FileDataManager(filePath, basePath);
                    ParcelFileDescriptor fd = openFile(filePath);
                    manager.parseFileContent(fd);
                    manger.FileDataManagerList.add(manager);
                    if (fd != null) {
                        try {
                            fd.close();
                            if (DEBUG) Log.d(TAG, "ParcelFileDescriptor is closed.");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (DEBUG)
                    Log.d(TAG, "Initialized list size: " + manger.FileDataManagerList.size());
            } else {
                if (DEBUG) Log.d(TAG, "untilUpdateTimeShiftInfo mcurrentPaths handle");
                Set<String> currentPaths = new LinkedHashSet<>();
                // Set<String> currentPaths = new HashSet<>();
                for (Integer index : DvrRecorderController.mInfosFileIndexList) {
                    String basePath = originalFilePath + "_INDEX_" + index;
                    currentPaths.add(basePath + DvrRecordInfo.FileInfoExtension);
                }

                // remove
                if (DEBUG)
                    Log.d(TAG, "untilUpdateTimeShiftInfo check manger.FileDataManagerList, remove outdated");
                Iterator<DvrRecordInfo.FileDataManager> iterator = manger.FileDataManagerList.iterator();
                while (iterator.hasNext()) {
                    DvrRecordInfo.FileDataManager manager = iterator.next();
                    if (!currentPaths.contains(manager.getFileName())) {
                        if (DEBUG) Log.d(TAG, "Removing outdated: " + manager.getFileName());
                        iterator.remove();
                    }
                }

                // add
                if (DEBUG) Log.d(TAG, "untilUpdateTimeShiftInfo check new file info, Added new");
                for (String filePath : currentPaths) {
                    boolean exists = false;
                    for (DvrRecordInfo.FileDataManager manager : manger.FileDataManagerList) {
                        if (manager.getFileName().equals(filePath)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        String basePath = filePath.replace(DvrRecordInfo.FileInfoExtension, "");
                        DvrRecordInfo.FileDataManager newManager = new DvrRecordInfo.FileDataManager(filePath, basePath);
                        ParcelFileDescriptor fd = openFile(filePath);
                        newManager.parseFileContent(fd);
                        manger.FileDataManagerList.add(newManager);
                        if (fd != null) {
                            fd.close();
                            if (DEBUG) Log.d(TAG, "ParcelFileDescriptor is closed.");
                        }
                        if (DEBUG) Log.d(TAG, "Added new: " + filePath);
                    }
                }


                if (DEBUG) Log.d(TAG, "untilUpdateTimeShiftInfo update last file info");

                DvrRecordInfo.FileDataManager manager = manger.FileDataManagerList.get(manger.FileDataManagerList.size() - 1);
                ParcelFileDescriptor fd = openFile(manager.getFileName());
                manager.parseFileContent(fd);
                if (fd != null) {
                    fd.close();
                    if (DEBUG) Log.d(TAG, "ParcelFileDescriptor is closed.");
                }
                if (DEBUG)
                    Log.d(TAG, "fine name: " + manager.getFileName() + " size: " + manager.fileParametersList.size());

//                for (DvrRecordInfo.FileDataManager manager : manger.FileDataManagerList) {
//                    try (ParcelFileDescriptor fd = openFile(manager.getFileName())) {
//                        manager.parseFileContent(fd);
//                        Log.d(TAG,"fine name: "+manager.getFileName()+" size: "+manager.fileParametersList.size());
//                    } catch (IOException e) {
//                        Log.e(TAG," error!!" + manager.getFileName(), e);
//                    }
//                }

            }
            if (DEBUG) Log.d(TAG, "size: " + manger.FileDataManagerList.size());

        } catch (IOException e) {

        } finally {
            if (DEBUG) Log.d(TAG, "untilUpdateTimeShiftInfo end process");
            DvrRecorderController.mLockFileIndexLock.unlock();
        }
        return true;
    }

    Boolean updateCurrentFileIndex(String fileName) {
        Boolean error = true;
        int number = 0;
        Pattern pattern = Pattern.compile("_INDEX_(\\d+)\\.dat");
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.find()) {
            number = Integer.parseInt(matcher.group(1));
            DvrRecorderController.mLockFileIndex = number;
            mCurrentFileIndex = number;
        } else {
            if (DEBUG) LogUtils.d("fileName is error! " + fileName);
            error = false;
        }
        return error;
    }

    public void playerStop() {

        dvrStop = true;
        mIsPause = false;

        try {
            if (readThread != null) {
                readThread.join();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeFileFile();
        if (speed != 1) {
            resetaudioParams();//speed set 0.audio reset
        }
        if (mDvrPlayback != null) {
            mDvrPlayback.stop();
            mDvrPlayback.close();
            mDvrPlayback = null;
        }
        if (dvrFile != null) {
            dvrFile = null;
        }
        mCurrentFileIndex = 0;
        speed = 1;
        estimatedTimeClear();
        if (mExecutor != null) {
            mExecutor.shutdown();
            mExecutor = null;
        }
        mTotalReadSize = 0;
    }


    public void playerScan() {

    }


    public void playerPlay() {

    }

    public void playerSetSpeed(float newSpeed) {
        Log.d(TAG, "setSpeed enter, speed=" + newSpeed);
        if (mVideoMediaCodec == null) {
            Log.d(TAG, "This is not supported if mVideoMediaCodec is null.");
            return;
        }

        if(speed == newSpeed)
        {
            Log.d(TAG, "The speed did not change.");
            return;
        }

        if (mVideoMediaCodec != null) {
            if(speed >= 0 && speed <= 2.0){
                if(newSpeed > 2 || newSpeed < 0){
                    if (mRecordMode == DvrRecorderController.RECORDERMODE_TIMEShIFT) {
                        mTimeshiftPresentationTimeUs = mPlayCurrentUs;
                    }
                    if (mRecordMode == DvrRecorderController.RECORDERMODE_RECORD) {
                        mScanCurrentUs = mPlayCurrentUs;
                    }
                }
            }
            else{
                if(newSpeed >= 0 && newSpeed <= 2){
                    if (mRecordMode == DvrRecorderController.RECORDERMODE_TIMEShIFT) {
                        synchronized (mLockPts) {
                            mPlayCurrentUs = mTimeshiftPresentationTimeUs;
                        }
                    }
                    if (mRecordMode == DvrRecorderController.RECORDERMODE_RECORD) {
                        synchronized (mLockPts) {
                            mPlayCurrentUs = mScanCurrentUs;
                        }
                    }
                }
            }

            setSpeedNew(newSpeed);
        }
    }

    private synchronized void setSpeedNew(float s){

        Log.d(TAG,"setSpeed"+s);
        if(mIsPause) {
            return;
        }
        if((speed>2.0f&&s>2.0f)||speed<0.0f&&s<0.0f){
            Log.d(TAG,"skip set speed Params:"+s+" speed:"+speed);
            speed = s;
            return;
        }else{
            speed = s;
        }

        if(speed < 0.0 || speed > 2.0) {

            Bundle mParamsTrick = new Bundle();
            if (mVideoMediaCodec != null) {
                AudioManager audioManager = mContext.getSystemService(AudioManager.class);
                audioManager.setParameters("clear_video_pts=1");
                LogUtils.d("set clear_video_pts 1");
                audioManager.setParameters("rtk_mute=1");
                LogUtils.d("set rtk_mute 1");
                mParamsTrick.putInt("vendor.tis.DecodeIFrameOnly", 1);
                mParamsTrick.putInt("vendor.tis.DecodeIFrameOnly.value",1);
                mParamsTrick.putInt("vendor.tis.dvr-playback-speed.value", (int)((speed)*1000));
                mVideoMediaCodec.setParameters(mParamsTrick);
                flushDvrPlayback();
            }
        } else if(speed >= 0.0 && speed <= 2.0) {
            if (mVideoMediaCodec != null) {
                Bundle mParamsTrick = new Bundle();
                // flushDvrPlayback();
                mParamsTrick.putInt("vendor.tis.DecodeIFrameOnly.value", 0);
                mParamsTrick.putInt("vendor.tis.dvr-playback-speed.value", (int)(1*1000));
                mVideoMediaCodec.setParameters(mParamsTrick);

                AudioManager audioManager = mContext.getSystemService(AudioManager.class);
                audioManager.setParameters("clear_video_pts=0");
                LogUtils.d("set clear_video_pts 0");
                audioManager.setParameters("rtk_mute=0");
                LogUtils.d("set rtk_mute 0");
            }
        }

        if(/*mVideoMediaCodec != null && */mAudioTrack!=null) {
            PlaybackParams aparams =  mAudioTrack.getPlaybackParams();

            if(speed>=0.0f&&speed <=2.0f){
                Log.d(TAG,"mAudioTrack set speed "+speed);
                aparams.setSpeed(speed);
                mAudioTrack.setPlaybackParams(aparams);
            }
            mIsPause =false;
        }
    }

    public void playerPause() {
        Log.d(TAG, "playerPause start");
        if ((mVideoMediaCodec != null) || (mAudioTrack != null)) {
            if (mIsPause != true || mStepForward != 0) {
                if (mVideoMediaCodec != null) {
                    Bundle mParamsTrick = new Bundle();
                    mParamsTrick.putInt("vendor.tis.DecodeIFrameOnly.value", 0);
                    mParamsTrick.putInt("vendor.tis.dvr-playback-speed.value", (int) (1 * 1000));
                    mVideoMediaCodec.setParameters(mParamsTrick);
                }
                Log.d(TAG, "playerPause setSpeed");
                setSpeed(0.0f);
                speed=0.0f;
                //flushDvrPlayback();
                mIsPause = true;
            }
        }
        Log.d(TAG, "playerPause end");
    }

    public void playerResume() {
        Log.d(TAG, "playerResume start");
        if ((mVideoMediaCodec != null) || (mAudioTrack != null)) {
            if (mAudioTrack != null) {
                Log.d(TAG, "playerResume setSpeed, speed=" + speed);
                if (speed < 0 || speed > 2.0) {
                    Bundle mParamsTrick = new Bundle();
                    if (mRecordMode == DvrRecorderController.RECORDERMODE_TIMEShIFT) {
                        synchronized (mLockPts) {
                            mPlayCurrentUs = mTimeshiftPresentationTimeUs;
                        }
                    }
                    if (mRecordMode == DvrRecorderController.RECORDERMODE_RECORD) {
                        synchronized (mLockPts) {
                            mPlayCurrentUs = mScanCurrentUs;
                        }
                    }
                    //mWrapAroundCnt = (int) (mPlayCurrentUs / DvrRecorderController.PTS_MAX);
                    if (mStepForward != 0)
                        mParamsTrick.putInt("vendor.tis.DecodeIFrameOnly.value", 1);
                    else
                        mParamsTrick.putInt("vendor.tis.DecodeIFrameOnly.value", 0);
                    //Log.d(TAG,"vendor.tis.DecodeIFrameOnly 0");
                    mParamsTrick.putInt("vendor.tis.dvr-playback-speed.value", (int) (1 * 1000));
                    //Log.d(TAG,"vendor.tis.dvr-playback-speed 1*1000");
                    mVideoMediaCodec.setParameters(mParamsTrick);
                    AudioManager audioManager = mContext.getSystemService(AudioManager.class);
                    audioManager.setParameters("clear_video_pts=0");
                    audioManager.setParameters("rtk_mute=0");
                }
                PlaybackParams aparams = mAudioTrack.getPlaybackParams();
                aparams.setSpeed(1.0f);

                speed = 1;
                mAudioTrack.setPlaybackParams(aparams);
                //flushDvrPlayback();
            }
            mIsPause = false;
        }
        Log.d(TAG, "playerResume end");
    }

    public void fastForward() {
        float tmpSpeed = speed;
        Log.d(TAG, "fastForward start, speed=" + speed);
        if (mVideoMediaCodec != null) {
            if (speed >= 1.0) {
                if (speed == 2.0) {
                    if (mRecordMode == DvrRecorderController.RECORDERMODE_TIMEShIFT) {
                        mTimeshiftPresentationTimeUs = mPlayCurrentUs;
                    }
                    if (mRecordMode == DvrRecorderController.RECORDERMODE_RECORD) {
                        mScanCurrentUs = mPlayCurrentUs;
                    }
                }
                speed *= 2;
                if (speed > 32) {
                    if (mRecordMode == DvrRecorderController.RECORDERMODE_TIMEShIFT) {
                        synchronized (mLockPts) {
                            mPlayCurrentUs = mTimeshiftPresentationTimeUs;
                        }
                        speed = 2.0f;
                    }
                    if (mRecordMode == DvrRecorderController.RECORDERMODE_RECORD) {
                        synchronized (mLockPts) {
                            mPlayCurrentUs = mScanCurrentUs;
                        }
                        speed = 1.0f;
                    }
                }
            } else {
                speed = 1.0f;
            }
            //Log.d(TAG,"speed = "+speed);
            Bundle mParamsTrick = new Bundle();
            Log.d(TAG, "new Bundle, speed=" + speed);
            //if(speed> 2){
            if (speed > 2 || speed < 0) {
                //flushDvrPlayback();
                mParamsTrick.putInt("vendor.tis.DecodeIFrameOnly.value", 1);
                mParamsTrick.putInt("vendor.tis.dvr-playback-speed.value", (int) (Math.abs(speed) * 1000));
                Log.d(TAG, "vendor.tis.DecodeIFrameOnly.value 1");
                mVideoMediaCodec.setParameters(mParamsTrick);
                setSpeed(speed);
                flushDvrPlayback();
            } else {
                mParamsTrick.putInt("vendor.tis.DecodeIFrameOnly.value", 0);
                mParamsTrick.putInt("vendor.tis.dvr-playback-speed.value", (int) (1 * 1000));
                Log.d(TAG, "vendor.tis.DecodeIFrameOnly.value 0");
                mVideoMediaCodec.setParameters(mParamsTrick);
                setSpeed(speed);
                if (tmpSpeed > 2 || tmpSpeed < 0)
                    flushDvrPlayback();
            }
        } else {
            Log.d(TAG, "mVideoMediaCodec is null");
        }
        Log.d(TAG, "fastForward end, speed=" + speed);
    }

        public void backForward() {
        float tmpSpeed = speed;
        Log.d(TAG, "backForward start, speed=" + speed);
        if (mVideoMediaCodec != null) {
            if (speed > 1.0) {
                if (speed > 2.0) {
                    if (mRecordMode == DvrRecorderController.RECORDERMODE_TIMEShIFT) {
                        synchronized (mLockPts) {
                            mPlayCurrentUs = mTimeshiftPresentationTimeUs;
                        }
                    }
                    if (mRecordMode == DvrRecorderController.RECORDERMODE_RECORD) {
                        synchronized (mLockPts) {
                            mPlayCurrentUs = mScanCurrentUs;
                        }
                    }
                }
                //mWrapAroundCnt = (int) (mPlayCurrentUs / DvrRecorderController.PTS_MAX);
                speed = 1.0f;
            } else if (speed < 0.0) {
                speed *= 2;
                if (speed < -32.0f)
                    speed = -2.0f;
            } else {
                if (speed == 1.0) {
                    if (mRecordMode == DvrRecorderController.RECORDERMODE_TIMEShIFT) {
                        mTimeshiftPresentationTimeUs = mPlayCurrentUs;
                    }
                    if (mRecordMode == DvrRecorderController.RECORDERMODE_RECORD) {
                        mScanCurrentUs = mPlayCurrentUs;
                    }
                }
                speed = -2.0f;
            }
            Bundle mParamsTrick = new Bundle();
            Log.d(TAG, "new Bundle, speed=" + speed);
            if (speed > 2 || speed < 0) {
                //flushDvrPlayback();
                mParamsTrick.putInt("vendor.tis.DecodeIFrameOnly.value", 1);
                mParamsTrick.putInt("vendor.tis.dvr-playback-speed.value", (int) -(Math.abs(speed) * 1000));
                mVideoMediaCodec.setParameters(mParamsTrick);
                Log.d(TAG, "vendor.tis.DecodeIFrameOnly.value 1");
                setSpeed(speed);
                flushDvrPlayback();
            } else {
                mParamsTrick.putInt("vendor.tis.DecodeIFrameOnly.value", 0);
                mParamsTrick.putInt("vendor.tis.dvr-playback-speed.value", (int) -(1 * 1000));
                mVideoMediaCodec.setParameters(mParamsTrick);
                Log.d(TAG, "vendor.tis.DecodeIFrameOnly.value 0");
                setSpeed(speed);
                if (tmpSpeed > 2 || tmpSpeed < 0)
                    flushDvrPlayback();
            }
        } else {
            Log.d(TAG, "mVideoMediaCodec is null");
        }
        Log.d(TAG, "backForward end, speed=" + speed);
    }

    public void resetaudioParams() {
        LogUtils.d("enter ");
        if (mVideoMediaCodec != null) {
            Bundle mParamsTrick = new Bundle();
            mParamsTrick.putInt("vendor.tis.DecodeIFrameOnly.value", 0);
            mParamsTrick.putInt("vendor.tis.dvr-playback-speed.value", (int) (1 * 1000));
            mVideoMediaCodec.setParameters(mParamsTrick);
        }
        AudioManager audioManager = mContext.getSystemService(AudioManager.class);
        audioManager.setParameters("clear_video_pts=0");
        LogUtils.d("set clear_video_pts 0");
        audioManager.setParameters("rtk_mute=0");
        LogUtils.d("set rtk_mute 0");
        flushDvrPlayback();
    }

    public synchronized void setSpeed(float speed) {
        Log.d(TAG, "setSpeed in, speed=" + speed);
        if (mIsPause) {
            Log.d(TAG, "mIsPause is true, return");
            return;
        }
        if (speed < 0.0 || speed > 2.0) {
            if (mVideoMediaCodec != null) {
                AudioManager audioManager = mContext.getSystemService(AudioManager.class);
                audioManager.setParameters("clear_video_pts=1");
                //Log.d(TAG,"set clear_video_pts 1");
                audioManager.setParameters("rtk_mute=1");
                Log.d(TAG, "set clear_video_pts 1, set rtk_mute 1");
                //Log.d(TAG,"set rtk_mute 1");
            }
        } else if (speed >= 0.0 && speed <= 2.0) {
            if (mVideoMediaCodec != null) {
                AudioManager audioManager = mContext.getSystemService(AudioManager.class);
                audioManager.setParameters("clear_video_pts=0");
                //Log.d(TAG,"set clear_video_pts 0");
                audioManager.setParameters("rtk_mute=0");
                Log.d(TAG, "set clear_video_pts 0, set rtk_mute 0");
                //Log.d(TAG,"set rtk_mute 0");
            }
        }

        if (mAudioTrack != null) {
            PlaybackParams aparams = mAudioTrack.getPlaybackParams();

            if (speed >= 0.0f && speed <= 2.0f) {
                Log.d(TAG, "mAudioTrack set speed " + speed);
                aparams.setSpeed(speed);
                mAudioTrack.setPlaybackParams(aparams);
            }
            mIsPause = false;
        } else {
            Log.d(TAG, "mAudioTrack is null");
        }
    }

    public void rewind() {
        if (dvrStop || mISStartPlaying)
            return;
        SeekNumber = -1;
    }

    public void playerSeek() {
        if (dvrStop || mISStartPlaying)
            return;
        SeekNumber = 1;
    }

    public void seekMs(long ms) {
        mSeekMs = ms;
    }

    private void estimatedTimeAdd(long size, int timeMs) {
        if (size == 0 || timeMs == 0) {
            return;
        }
        estimatedTimeFileSize += size;
        estimatedTimeFileMs += timeMs;
    }

    private void estimatedTimeClear() {
        Log.d(TAG, "clear estimatedTime ");
        estimatedTimeFileSize = 0;
        estimatedTimeFileMs = 0;
    }

    private long SeekEstimatedTimeFileTime(int seekTimeMs) {
        if (estimatedTimeFileSize == 0 || estimatedTimeFileMs == 0) {
            return 0;
        } else {
            return (((estimatedTimeFileSize / estimatedTimeFileMs) * seekTimeMs) / 188) * 188;
        }
    }

    private int findCurrentFileIndex(String fileName) {
        int number = 0;
        Pattern pattern = Pattern.compile("_INDEX_(\\d+)\\.dat");
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            number = Integer.parseInt(matcher.group(1));
        } else {
            //Log.d(TAG,"fileName is error! "+fileName);
        }
        return number;
    }

    private static int countDatFiles(String directoryPath, String extension) {
        File dir = new File(directoryPath);
        if (!dir.exists() || !dir.isDirectory()) {
            Log.d(TAG, "Directory is not exist,directoryPath=" + directoryPath);
            return 0;
        }

        String[] datFiles = dir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(extension.toLowerCase());
            }
        });

        return datFiles == null ? 0 : datFiles.length;
    }

    private static String getDirectoryPath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            Log.d(TAG, "Directory is not exist,filePath=" + filePath);
            return null;
        }

        int lastSeparatorIndex = filePath.lastIndexOf('/');

        if (lastSeparatorIndex == -1) {
            return null;
        }

        return filePath.substring(0, lastSeparatorIndex + 1);
    }

    private static long getFileSize(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return file.length();
        } else {
            Log.d(TAG, "File does not exist or is not a file: " + filePath);
            return 0;
        }
    }

    private List<long[]> InitHeadTailPtsList(DvrRecordInfo.FileDataListManager manger) {
        List<long[]> resultList = new ArrayList<>();
        int i, j;
        if (manger.FileDataManagerList.size() == 0) {
            return resultList;
        }

        for (i = 0; i < manger.FileDataManagerList.size(); i++) {
            long head = 0, tail = 0, filesize = 0, headPts = 0, tailPts = 0;
            for (j = 0; j < manger.FileDataManagerList.get(i).fileParametersList.size(); j++) {
                if (j == 0) {
                    head = manger.FileDataManagerList.get(i).fileParametersList.get(0).getIbase();
                    headPts = manger.FileDataManagerList.get(i).fileParametersList.get(0).getiPts();
                }
                if (j + 1 == manger.FileDataManagerList.get(i).fileParametersList.size()) {
                    tail = manger.FileDataManagerList.get(i).fileParametersList.get(j).getIbase();
                    tailPts = manger.FileDataManagerList.get(i).fileParametersList.get(j).getiPts();
                }
            }
            filesize = getFileSize(manger.FileDataManagerList.get(i).getbaseName());
            resultList.add(new long[]{head, tail, filesize, headPts, tailPts});
        }
        return resultList;
    }

    private boolean InitPlaybackInfo(DvrRecordInfo.FileDataListManager manger) {
        long startTime, endTime, startTmpTime;

        if (manger.FileDataManagerList == null) {
            Log.d(TAG, "manger.FileDataManagerList == null");
            return false;
        }
        String directoryPath;
        String playBackInfoFilePath;
        String playBackInfoFileBasePath;
        DvrRecordInfo.FileDataManager manager1;
        ParcelFileDescriptor fileInfoPathFd;
        int infoCnt, index;

        startTime = DvrRecorderController.gettime();
        directoryPath = getDirectoryPath(originalFilePath);
        if (directoryPath == null)
            return false;
        infoCnt = countDatFiles(directoryPath, "dat");
        if (infoCnt == 0)
            return false;

        if (manger.FileDataManagerList.isEmpty() == false)
            manger.FileDataManagerList.clear();
        Log.d(TAG, "manger.FileDataManagerList=" + manger.FileDataManagerList.size());
        DvrRecorderController.mLockFileIndexLock.lock();
        try {
        manger.FileDataManagerList.clear();
        Log.d(TAG, "duratioTime=" + (DvrRecorderController.gettime() - startTime));
        for (index = 0; index < infoCnt; index++) {
            startTmpTime = DvrRecorderController.gettime();
                if (index == 0) {
                    playBackInfoFileBasePath = originalFilePath;
                } else {
                    playBackInfoFileBasePath = originalFilePath + "_INDEX_" + index;
                }
                playBackInfoFilePath = playBackInfoFileBasePath + DvrRecordInfo.FileInfoExtension;
                manager1 = new DvrRecordInfo.FileDataManager(playBackInfoFilePath, playBackInfoFileBasePath);
                fileInfoPathFd = openFile(playBackInfoFilePath);
                manager1.parseFileContent(fileInfoPathFd);
                manger.FileDataManagerList.add(manager1);
                if (fileInfoPathFd != null) {
                    try {
                        fileInfoPathFd.close();
                    } catch (IOException e) {
                        //throw new RuntimeException(e);
                    }
                    fileInfoPathFd = null;
                }
                //Log.d(TAG,"playBackInfoFileBasePath="+playBackInfoFileBasePath+" duratioTime="+(DvrRecorderController.gettime()-startTmpTime));
            }
        } catch (Exception e) {
            Log.e(TAG, "InitPlaybackInfo failed!", e);
        }finally {
            DvrRecorderController.mLockFileIndexLock.unlock();
        }

        endTime = DvrRecorderController.gettime();
        Log.d(TAG, "Allen_AAAAAAAAAA manger.FileDataManagerList=" + manger.FileDataManagerList.size() + " endTime-startTime=" + (endTime - startTime));
        /*
        for(int i=0;i<manger.FileDataManagerList.size();i++) {
            Log.d(TAG, "manger.FileDataManagerList.get(" + i + ").getFileName()=" + manger.FileDataManagerList.get(i).getFileName());
            Log.d(TAG, "manger.FileDataManagerList.get(" + i + ").getFileName().fileParametersList.size()=" + manger.FileDataManagerList.get(i).fileParametersList.size());
        }
        */
        return true;
    }

    private boolean untilUpdatePlaybackInfo(DvrRecordInfo.FileDataListManager manger, List<long[]> recoordHeadTailList) {
        int currentIdx,size;
        long filesize,ibaseLast,ibaseFirst,iPtsLast,iPtsFirst;
        DvrRecordInfo.FileDataManager manager1=null;
        String playInfoFilePath;
        String playInfoFileBasePath;
        ParcelFileDescriptor fileInfoPathFd=null;
        if (DEBUG) Log.d(TAG, "untilUpdatePlaybackInfo start process");

        if(mNormalReocrd == null) {
            Log.e(TAG, "untilUpdatePlaybackInfo mNormalReocrd is null");
            return false;
        }
        if (manger.FileDataManagerList == null) {
            Log.e(TAG, "untilUpdatePlaybackInfo manger.FileDataManagerList is null");
            return false;
        }
        if (recoordHeadTailList == null) {
            Log.e(TAG, "untilUpdatePlaybackInfo recoordHeadTailList is null");
            return false;
        }
        //Log.d(TAG, "untilUpdatePlaybackInfo manger.FileDataManagerList.size() = " + manger.FileDataManagerList.size());
        //Log.d(TAG, "untilUpdatePlaybackInfo recoordHeadTailList.size() = " + recoordHeadTailList.size());
        currentIdx = mNormalReocrd.getCurrentFileIndex();
        size = manger.FileDataManagerList.size();

        if (currentIdx == 0) {
            playInfoFileBasePath = originalFilePath;
        } else {
            playInfoFileBasePath = originalFilePath + "_INDEX_" + currentIdx;
        }
        playInfoFilePath = playInfoFileBasePath + DvrRecordInfo.FileInfoExtension;
        Log.d(TAG, "Allen_BBBBB untilUpdatePlaybackInfo playInfoFileBasePath=" + playInfoFileBasePath);
        Log.d(TAG, "Allen_BBBBB untilUpdatePlaybackInfo playInfoFilePath=" + playInfoFilePath);


        manager1 = new DvrRecordInfo.FileDataManager(playInfoFilePath, playInfoFileBasePath);
        fileInfoPathFd = openFile(playInfoFilePath);
        manager1.parseFileContent(fileInfoPathFd);
        filesize = getFileSize(playInfoFileBasePath);
        ibaseFirst = manager1.fileParametersList.get(0).getIbase();
        iPtsFirst = manager1.fileParametersList.get(0).getiPts();
        ibaseLast = manager1.fileParametersList.get(manager1.fileParametersList.size()-1).getIbase();
        iPtsLast = manager1.fileParametersList.get(manager1.fileParametersList.size()-1).getiPts();
        Log.d(TAG, "Allen_BBBBB ibaseFirst="+ibaseFirst+" iPtsFirst="+iPtsFirst+" ibaseLast="+ibaseLast+" iPtsLast="+iPtsLast);
        Log.d(TAG, "Allen_BBBBB ibaseFirst="+manager1.fileParametersList);
        /*
            recoordHeadTailList[0]  recoordHeadTailList[1]  recoordHeadTailList[2]  recoordHeadTailList[3]  recoordHeadTailList[4]
            Head:getIbase	        Tail:getIbase           fileSize                Head:getiPts            Tail:getiPts
         */
        if(size < currentIdx){
            Log.d(TAG, "Allen_BBBBB manger.FileDataManagerList.add");
            manger.FileDataManagerList.add(manager1);
            recoordHeadTailList.add(new long[]{ibaseFirst, ibaseLast, filesize, iPtsFirst, iPtsLast});
        }
        else{
            Log.d(TAG, "Allen_BBBBB manger.FileDataManagerList.set");
            manger.FileDataManagerList.set(currentIdx, manager1);
            recoordHeadTailList.get(currentIdx)[2]=filesize;
            recoordHeadTailList.get(currentIdx)[1]=ibaseLast;
            recoordHeadTailList.get(currentIdx)[4]=iPtsLast;
        }

        if (fileInfoPathFd != null) {
            try {
                fileInfoPathFd.close();
            } catch (IOException e) {
                Log.e(TAG, "untilUpdatePlaybackInfo IOException", e);
            }
            fileInfoPathFd = null;
        }
        int tmptmp=manger.FileDataManagerList.get(currentIdx).fileParametersList.size()-1;
        //Log.d(TAG, "Allen_CCCCC manger==>fileParametersList.size()="+manger.FileDataManagerList.get(currentIdx).fileParametersList.size());
        //Log.d(TAG, "Allen_CCCCC manger==>getIbase(n-2)="+manger.FileDataManagerList.get(currentIdx).fileParametersList.get(tmptmp-2).getIbase());
        //Log.d(TAG, "Allen_CCCCC manger==>getIPts(n-2)="+manger.FileDataManagerList.get(currentIdx).fileParametersList.get(tmptmp-2).getiPts());
        //Log.d(TAG, "Allen_CCCCC manger==>getIbase(n-1)="+manger.FileDataManagerList.get(currentIdx).fileParametersList.get(tmptmp-1).getIbase());
        //Log.d(TAG, "Allen_CCCCC manger==>getIPts(n-1)="+manger.FileDataManagerList.get(currentIdx).fileParametersList.get(tmptmp-1).getiPts());
        //Log.d(TAG, "Allen_CCCCC manger==>getIbase(n)="+manger.FileDataManagerList.get(currentIdx).fileParametersList.get(tmptmp).getIbase());
        //Log.d(TAG, "Allen_CCCCC manger==>getIPts(n)="+manger.FileDataManagerList.get(currentIdx).fileParametersList.get(tmptmp).getiPts());

        //Log.d(TAG, "Allen_CCCCC recoordHeadTailList.get("+currentIdx+")[0](getHeadIbase)="+recoordHeadTailList.get(currentIdx)[0]);
        //Log.d(TAG, "Allen_CCCCC recoordHeadTailList.get("+currentIdx+")[1](getLastIbase)="+recoordHeadTailList.get(currentIdx)[1]);
        //Log.d(TAG, "Allen_CCCCC recoordHeadTailList.get("+currentIdx+")[3](getHeadiPts)="+recoordHeadTailList.get(currentIdx)[3]);
        //Log.d(TAG, "Allen_CCCCC recoordHeadTailList.get("+currentIdx+")[4](getLastPts)="+recoordHeadTailList.get(currentIdx)[4]);


        return true;
    }


    public static long getElapsedMillis() {
        long currentNano = System.nanoTime();
        return currentNano / 1_000_000;
    }

}



   
