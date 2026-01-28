package com.prime.dtv.service.Dvr;

import android.content.Context;
import android.media.PlaybackParams;
import android.os.Bundle;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.tv.tuner.Tuner;
import android.media.tv.tuner.dvr.DvrPlayback;
import android.media.tv.tuner.dvr.DvrSettings;
import android.media.tv.tuner.dvr.OnPlaybackStatusChangedListener;

import android.os.ParcelFileDescriptor;

import android.util.Log;


import com.prime.dtv.Interface.PesiDtvFrameworkInterfaceCallback;
import com.prime.dtv.sysdata.DTVMessage;
import com.prime.dtv.utils.LogUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DvrPlaybackController {
    private static final String TAG = "DVR DvrPlaybackController";

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

    private long readSize = 314524; //188*n
    private volatile boolean dvrStop = true;
    Thread readThread = null;

    private long mFilelength=0;
    private long mTotalReadSize=0;

    private static int mSleepTime=10;
    private boolean mIsPause = false;
    private float speed = 1;
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
    private int mLastPosition=0;
    private int mTunerId;
    private volatile long mPlayStartUs = 0;
    private volatile long mPlayCurrentUs = 0;
    private volatile long mScanCurrentUs = 0;
    private int mRecordMode = DvrRecorderController.RECORDERMODE_RECORD;
    private int mTotalRecTimeSec=0;
    private long mStartPlayTimestamp = 0;
    private int seekFlag;

    public volatile long mTimeshiftPresentationTimeUs = 0;
    public volatile long mTimeshiftStartTimeUs = 0;
    public volatile long mTimeshiftOriginalStartTimeUs = 0;
    private int mWrapAroundCnt = 0;
    private int mWrapAroundMax = 0;
    private volatile int mDiscardpFlag  = 0;
    private long mPrePlayUs = 0;

    private Lock playbackOptionLock = new ReentrantLock();

    private DvrRecorderController mTimeshiftReocrd=null;

    public DvrPlaybackController(PesiDtvFrameworkInterfaceCallback callback,Context context,int tunerId) {
        mIsPause =false;
        //mCurrentFileIndex = 0;
        mPesiDtvCallback = callback;
        mTunerId = tunerId;
        mExecutor = Executors.newFixedThreadPool(THREAD_COUNT);
        mContext = context;
        //mPlayStartUs = 0;
        //mPlayCurrentUs = 0;
        mDiscardpFlag=0;
    }

    public int getWrapAroundMax(){
        return mWrapAroundMax;
    }

    public int getWrapAroundCnt(){
        return mWrapAroundCnt;
    }

    public void setTimeshiftReocrd(DvrRecorderController dvrRecorderController){
        mTimeshiftReocrd = dvrRecorderController;
    }

    public void setPresentationTimeUs(long presentationTimeUs){
        if(speed > 0 && speed <= 2) {
            mPresentationTimeUs = presentationTimeUs;
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
            mPlayStartUs = playStartUs;
            mStartPlayTimestamp = DvrRecorderController.gettime();
            setPresentationTimeUs(playStartUs);
        }
    }

    public void setPlayCurrentUs(long playCurrentUs){
        if (speed > 0 && speed <= 2 && mDiscardpFlag == 0) {
            long playCurrentUsTmp = playCurrentUs;
            if (mPrePlayUs == 0) {
                Log.d(TAG, "mPrePlayUs="+mPrePlayUs+" playCurrentUs="+playCurrentUs);
                mPrePlayUs = playCurrentUs;
                if(mRecordMode == DvrRecorderController.RECORDERMODE_RECORD)
                    playCurrentUsTmp = mScanCurrentUs;
                if(mRecordMode == DvrRecorderController.RECORDERMODE_TIMEShIFT)
                    playCurrentUsTmp = mTimeshiftPresentationTimeUs;
                Log.d(TAG, "playCurrentUsTmp="+playCurrentUsTmp+" mWrapAroundCnt="+mWrapAroundCnt);
            } else {
                Log.d(TAG, "playCurrentUs="+playCurrentUs+" mPrePlayUs="+mPrePlayUs);
                if (playCurrentUs < mPrePlayUs && Math.abs(mPrePlayUs-playCurrentUs) >= 72000000000L) {
                    Log.d(TAG, "playCurrentUs="+playCurrentUs+" mPrePlayUs="+mPrePlayUs+" mWrapAroundCnt="+mWrapAroundCnt);
                    mWrapAroundCnt++;
                    playCurrentUsTmp = playCurrentUs + ((long) mWrapAroundCnt * DvrRecorderController.PTS_MAX);
                    mPrePlayUs = playCurrentUs;
                    Log.d(TAG, "playCurrentUsTmp="+playCurrentUsTmp+" mPrePlayUs="+mPrePlayUs+" mWrapAroundCnt="+mWrapAroundCnt);
                } else {
                    Log.d(TAG, "playCurrentUs="+playCurrentUs+" mPrePlayUs="+mPrePlayUs);
                    if(Math.abs(mPrePlayUs-playCurrentUs) >= 72000000000L){
                        Log.d(TAG, "The time is unreasonable, give up this playCurrentUs");
                        playCurrentUsTmp = mPlayCurrentUs;
                    }
                    else {
                    playCurrentUsTmp = playCurrentUs + ((long) mWrapAroundCnt * DvrRecorderController.PTS_MAX);
                    mPrePlayUs = playCurrentUs;
                    }
                    Log.d(TAG, "playCurrentUsTmp="+playCurrentUsTmp+" mPrePlayUs="+mPrePlayUs+" mWrapAroundCnt="+mWrapAroundCnt);
                }
            }
            mPlayCurrentUs = playCurrentUsTmp;
            Log.d(TAG, "mPlayCurrentUs="+mPlayCurrentUs);
            setPresentationTimeUs(mPlayCurrentUs);
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
            if(((speed > 0) && (speed <= 2)) && (seekFlag == 0)){
                retTimeSec = (int) ((mPlayCurrentUs - (mPlayStartUs + ((long) mTimeshiftReocrd.getTimeshiftRecStartTime() * 1000000L))) / 1000000L);
                Log.d(TAG, "getCurrentPlayTimeSec mPlayCurrentUs=" + mPlayCurrentUs + " mPlayStartUs=" + mPlayStartUs);
                Log.d(TAG, "getCurrentPlayTimeSec mTimeshiftReocrd.getTimeshiftRecStartTime=" + mTimeshiftReocrd.getTimeshiftRecStartTime());
                Log.d(TAG, "getCurrentPlayTimeSec retTimeSec( ((mPlayCurrentUs - (mPlayStartUs + ((long) mTimeshiftReocrd.getTimeshiftRecStartTime() * 1000000L))) / 1000000L) )=" + retTimeSec);
                if (retTimeSec < 0)
                    retTimeSec = 0;
                Log.d(TAG, "getCurrentPlayTimeSec mTimeshiftReocrd.getTimeShiftBufferSec()=" + mTimeshiftReocrd.getTimeShiftBufferSec());
                if (retTimeSec > mTimeshiftReocrd.getTimeShiftBufferSec()) {//Just to avoid display errors
                    retTimeSec = mTimeshiftReocrd.getTimeShiftBufferSec();
            }
                Log.d(TAG, "getCurrentPlayTimeSec retTimeSec=" + retTimeSec);
                }
                else {
                retTimeSec = (int) ((mTimeshiftPresentationTimeUs - mTimeshiftStartTimeUs) / 1000000);
                Log.d(TAG, "getCurrentPlayTimeSec mTimeshiftPresentationTimeUs=" + mTimeshiftPresentationTimeUs + " mTimeshiftStartTimeUs=" + mTimeshiftStartTimeUs);
                Log.d(TAG, "getCurrentPlayTimeSec retTimeSec((mTimeshiftPresentationTimeUs - mTimeshiftStartTimeUs)/1000000)=" + retTimeSec);
                if (retTimeSec < 0)
                    retTimeSec = 0;
                Log.d(TAG, "getCurrentPlayTimeSec retTimeSec=" + retTimeSec);
            }
        }
        else if(mRecordMode == DvrRecorderController.RECORDERMODE_RECORD) {
            if((speed>2) || (speed<0)) {
                Log.d(TAG,"speed="+speed+" mPlayStartUs="+mPlayStartUs+" mScanCurrentUs="+mScanCurrentUs);
                retTimeSec = (int) ((mScanCurrentUs - mPlayStartUs) / 1000000);
            }
            else{
                Log.d(TAG,"speed="+speed+" mPlayStartUs="+mPlayStartUs+" mPlayCurrentUs="+mPlayCurrentUs);
                retTimeSec = (int) ((mPlayCurrentUs - mPlayStartUs) / 1000000);
            }
        }
        else {
            retTimeSec = 0;
        }

        if(retTimeSec < 0) {
            retTimeSec = 1;
        }
        Log.d(TAG,"getCurrentPlayTimeSec retTimeSec = "+retTimeSec);
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
            Log.d(TAG, "mTuner is null");
        }
    }

    public void startDvrPlay(){
        dvrStop = false;
    }

    public int getTotalRecTimeSec(){
        return mTotalRecTimeSec;
    }

    public void setTotalRecTimeSec(int totalTime){
        Log.d(TAG, "TotalRecTimeSec = " + totalTime);
        mTotalRecTimeSec = totalTime;
    }

    public void setLastPosition(int lastPosition ){
        mLastPosition = lastPosition;
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
                    Log.d(TAG, "PlaybackStatus = "+status+", mSleepTime = "+mSleepTime );
                }
            };

            if(DvrListener == null) {
                Log.d(TAG, "DvrListener is null");
                return 1;
            }
            //mTuner.cancelTuning();
            //mTuner.closeFrontend();
            if(mTuner == null) {
                Log.d(TAG, "mTuner is null");
                return 1;
            }

            mDvrPlayback = mTuner.openDvrPlayback(bufferSize, mExecutor, DvrListener);

            if(mDvrPlayback == null) {
                Log.d(TAG, "mDvrPlayback is null");
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
                else{
                    readThread = new PvrReadThread();
                }
            }

            readThread.start();
            return 0;
            //startVideoMediaCodec();
    }

    private void flushDvrPlayback() {
        Log.d(TAG,"flushDvrPlayback in");
        if (mDvrPlayback != null) {
            Log.d(TAG,"flushDvrPlayback playbackOptionLock start");
            playbackOptionLock.lock();

            mDvrPlayback.stop();
            //Log.d(TAG,"flushDvrPlayback mDvrPlayback.flush start");
            mDvrPlayback.flush();
            //Log.d(TAG,"flushDvrPlayback mDvrPlayback.flush end");
            mDvrPlayback.start();

            playbackOptionLock.unlock();
            Log.d(TAG,"flushDvrPlayback playbackOptionLock end");
        }
        Log.d(TAG,"flushDvrPlayback out");
    }

    private class TimeShiftReadThread extends Thread {
        private     String fileInfoPath = filePath+DvrRecordInfo.FileInfoExtension;
        private     ParcelFileDescriptor fileInfoPathFd =  openFile(fileInfoPath);
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

        public void run() {
            Log.d(TAG,"TimeShiftReadThread timeshift read Thread enter..");
            File checkFile=null;
            seekFlag = 0;
            mWrapAroundMax = 0;
            mWrapAroundCnt = 0;
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
                Log.d(TAG,"TimeShiftReadThread mTunerId="+mTunerId+" mCurrentFileIndex="+mCurrentFileIndex+" filePath="+filePath);
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
                    Log.d(TAG,"TimeShiftReadThread getTimeshiftRecStartTime()="+mTimeshiftReocrd.getTimeshiftRecStartTime());
                    mTimeshiftStartTimeUs = mTimeshiftOriginalStartTimeUs + tmp;
                    Log.d(TAG,"TimeShiftReadThread Update mTimeshiftStartTimeUs: mTimeshiftOriginalStartTimeUs="+mTimeshiftOriginalStartTimeUs+" mTimeshiftStartTimeUs="+mTimeshiftStartTimeUs);
                }

                if(mIsPause == true && mTimeshiftReocrd.getTimeshiftRecStartTime() != 0){
                    int currentPlayTime=getCurrentPlayTimeSec();
                    Log.d(TAG,"TimeShiftReadThread mIsPause=true,Check Auto resume: currentPlayTime="+currentPlayTime);
                    if(currentPlayTime <= 2){
                        mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_SOF, 0, 0, null);
                        mPlayCurrentUs = mTimeshiftPresentationTimeUs;
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
                        Log.d(TAG,"TimeShiftReadThread Auto resume(PESI_SVR_EVT_PVR_PLAY_SOF) mPlayCurrentUs="+mPlayCurrentUs);
                    }
                }

                if(mDvrPlayback != null && mIsPause == false){
                    if(currentTimeMillis+upTimeMs<getElapsedMillis()){
                        Log.d(TAG,"TimeShiftReadThread untilUpdateTimeShiftInfo per second");
                        untilUpdateTimeShiftInfo(fileDataListManager);
                        Log.d(TAG,"TimeShiftReadThread untilUpdateTimeShiftInfo end");
                        //currentTimeMillis = System.currentTimeMillis();
                        currentTimeMillis = getElapsedMillis();
                    }
                    if(mSeekMs!=0){
                        // untilUpdateTimeShiftInfo(fileDataListManager);
                        Log.d(TAG,"TimeShiftReadThread mSeekMs != 0, mSeekMs="+mSeekMs);
                        currentTimeMillis = getElapsedMillis();
                        targetPts = mSeekMs*1000;
                        manager1 = fileDataListManager.findNearestPtsInData(targetPts,true);
                        Log.d(TAG,"TimeShiftReadThread mSeekMs: "+mSeekMs+" manager1:FileName"+manager1.getFileName());
                        if(!manager1.getFileName().equals(fileInfoPath)){
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
                            mTotalReadSize = tmpFileParameters.getIbase();
                            PlaybackReadSize =  readSize;
                            tmpFileParameters = null;
                            flushDvrPlayback();
                            Log.d(TAG,"TimeShiftReadThread mSeekMs="+mSeekMs+" mTotalReadSize="+mTotalReadSize+" mTimeshiftStartTimeUs="+mTimeshiftStartTimeUs);
                        }else{
                            Log.d(TAG,"TimeShiftReadThread tmpFileParameters == null");
                        }
                        mSeekMs= 0;
                    }
                    else if(speed>2||speed<0){
                        long currentPts;
                        if(targetPts != 0 && speed == useSpeed){
                            Log.d(TAG,"TimeShiftReadThread targetPts != 0 && speed == useSpeed");
                            currentPts = targetPts;
                            targetPts = currentPts + (long)(fastspeedMs*useSpeed*1000);
                        }else{//roll
                            if(targetPts ==0) {
                                currentPts = mPlayCurrentUs;//mPresentationTimeUs;//getPresentationTimeUs();
                                mTimeshiftPresentationTimeUs = mPlayCurrentUs;//mPresentationTimeUs;
                            }
                            else
                                currentPts = mTimeshiftPresentationTimeUs;
                            writeTimeMillis = getElapsedMillis();
                            targetPts = currentPts;
                            mTotalReadSize = 0;
                            delay_ms = 0;
                            Log.d(TAG,"TimeShiftReadThread targetPts="+targetPts+" writeTimeMillis="+writeTimeMillis);
                            Log.d(TAG,"TimeShiftReadThread mCurrentFileIndex="+mCurrentFileIndex+" fileInfoPath="+fileInfoPath);
                            manager1 = fileDataListManager.findNearestPtsInData(targetPts,true);
                            Log.d(TAG,"TimeShiftReadThread manager1.getFileName()="+manager1.getFileName());
                            if(!manager1.getFileName().equals(fileInfoPath)){
                                Log.d(TAG,"TimeShiftReadThread need to change file, speed="+speed);
                                fileInfoPath = manager1.getFileName();
                                closeFileFile();
                                filePath = manager1.getbaseName();
                                dvrFile = null ;
                                mTotalReadSize = 0;
                                mFilelength = 0;
                                Log.d(TAG,"TimeShiftReadThread new filePath="+filePath+" fileInfoPath="+fileInfoPath);
                            }
                            updateCurrentFileIndex(fileInfoPath);
                            tmpFileParameters = manager1.findNearestPtsInData(targetPts,true);

                            playPts = tmpFileParameters.getiPts();
                            Log.d(TAG,"TimeShiftReadThread mCurrentFileIndex="+mCurrentFileIndex+" playPts="+playPts);
                        }

                        mWrapAroundCnt = (int) (playPts / DvrRecorderController.PTS_MAX);

                        useSpeed = speed;
                        Boolean tmpNext = false;
                        Boolean finish = false;
                        //playPts = tmpFileParameters.getiPts();
                        int loop = 1;
                        long tmpdelay_ms = 0;
                        long diffPtsMs = 0;
                        Log.d(TAG,"TimeShiftReadThread loop start");
                        for(int i=0;i<loop;i++)
                        {
                            if(useSpeed>2){
                                finish = fileDataListManager.checkIsLastFileDataManager(manager1);
                                if(finish == true)
                                    Log.d(TAG,"TimeShiftReadThread It is Last FileDataManager");
                                tmpFileParameters1 = manager1.findNextFileParameters(tmpFileParameters,true);
                                if(tmpFileParameters1 == null)
                                    Log.d(TAG,"TimeShiftReadThread It FileParameters is Last");
                                tmpNext = true;
                            }
                            else {
                                if(mTimeshiftReocrd.getTimeshiftRecStartTime() == 0) {
                                    Log.d(TAG,"TimeShiftReadThread mTimeshiftRecStartSec = 0");
                                finish = fileDataListManager.checkIsFirstFileDataManager(manager1);
                                    if(finish == true)
                                        Log.d(TAG,"TimeShiftReadThread It is First FileDataManager, mTimeshiftStartTimeUs="+mTimeshiftStartTimeUs);
                                tmpFileParameters1 = manager1.findPreviousFileParameters(tmpFileParameters,true);
                                    if(tmpFileParameters1 == null)
                                        Log.d(TAG,"TimeShiftReadThread It FileParameters is First, mTimeshiftStartTimeUs="+mTimeshiftStartTimeUs);
                                }
                                else {
                                    DvrRecordInfo.FileDataManager managerTmp;
                                    long headTimeUs,tailTimeUs;
                                    Log.d(TAG,"TimeShiftReadThread mTimeshiftRecStartSec="+mTimeshiftReocrd.getTimeshiftRecStartTime());

                                    headTimeUs = manager1.getFirstNearestPtsInData(false).getiPts();
                                    tailTimeUs = manager1.getLastNearestPtsInData(false).getiPts();
                                    if(mTimeshiftStartTimeUs >= headTimeUs && mTimeshiftStartTimeUs <= tailTimeUs)
                                        finish = true;

                                    if(finish == true)
                                        Log.d(TAG,"TimeShiftReadThread It is First FileDataManager, mTimeshiftStartTimeUs="+mTimeshiftStartTimeUs);

                                    tmpFileParameters1 = manager1.findPreviousFileParameters(tmpFileParameters,true);
                                    if(tmpFileParameters1 != null){
                                        Log.d(TAG,"TimeShiftReadThread tmpFileParameters1.getiPts="+tmpFileParameters1.getiPts()+" mTimeshiftStartTimeUs="+mTimeshiftStartTimeUs);
                                        if(tmpFileParameters1.getiPts() < mTimeshiftStartTimeUs) {
                                            Log.d(TAG, "TimeShiftReadThread It FileParameters is First");
                                            tmpFileParameters1 = null;
                                        }
                                    }
                                    else {
                                        Log.d(TAG,"TimeShiftReadThread tmpFileParameters1 is null==>something error, mTimeshiftStartTimeUs="+mTimeshiftStartTimeUs);
                                    }
                                }
                                tmpNext = false;
                            }
                            if(tmpFileParameters1 == null){
                                Log.d(TAG,"TimeShiftReadThread tmpFileParameters error!");
                                if(finish){
                                    Log.d(TAG,"TimeShiftReadThread tmpFileParameters1 is null, finish is true, tmpNext="+tmpNext);
                                    break;
                                }
                                else{
                                    if(tmpNext){//Switch to the next dat file && get the first pts
                                        manager1 = fileDataListManager.findNextNearestPtsInData(true);
                                        tmpFileParameters = manager1.getFirstNearestPtsInData(true);
                                        Log.d(TAG,"TimeShiftReadThread Switch to the next dat file && get the first pts");
                                    }else{//Switch to the previous dat file && get the last data
                                        manager1 = fileDataListManager.findPreviousNearestPtsInData(true);
                                        tmpFileParameters = manager1.getLastNearestPtsInData(true);
                                        Log.d(TAG,"TimeShiftReadThread Switch to the previous dat file && get the last data");
                                    }
                                    fileInfoPath = manager1.getFileName();
                                    closeFileFile();
                                    filePath = manager1.getbaseName();
                                    Log.d(TAG,"TimeShiftReadThread next(previous) filePath="+filePath+" fileInfoPath="+fileInfoPath);
                                    dvrFile = null ;
                                    mTotalReadSize = 0;
                                    mFilelength = 0;
                                }
                            }
                            else{
                                tmpFileParameters = tmpFileParameters1;
                            }

                            Log.d(TAG,"TimeShiftReadThread tmpFileParameters.getiPts()="+tmpFileParameters.getiPts());
                            diffPtsMs = (tmpFileParameters.getiPts() - playPts)/1000;
                            Log.d(TAG,"TimeShiftReadThread playPts="+playPts+" diffPtsMs="+diffPtsMs);
                            long  endTimeMillis = getElapsedMillis();
                            Log.d(TAG,"TimeShiftReadThread writeTimeMillis="+writeTimeMillis+" endTimeMillis="+endTimeMillis);
                            tmpdelay_ms = Math.abs((long)diffPtsMs/(long)useSpeed) - (endTimeMillis - writeTimeMillis);
                            Log.d(TAG,"TimeShiftReadThread tmpdelay_ms="+tmpdelay_ms+" delay_ms="+delay_ms);
                            if((tmpdelay_ms-delay_ms < 2000 || delay_ms == 0) && (tmpdelay_ms < 2000)){
                                delay_ms = (int)tmpdelay_ms;
                                Log.d(TAG,"TimeShiftReadThread delay_ms="+delay_ms);
                            }else{
                                Log.d(TAG,"TimeShiftReadThread tmpdelay_ms error: "+tmpdelay_ms+" playPts: "+playPts+" endTimeMillis: "+endTimeMillis+"writeTimeMillis: "+writeTimeMillis);
                                break;
                            }
                            Log.d(TAG,"TimeShiftReadThread delay_ms="+delay_ms+" finish="+finish+" tmpFileParameters1="+tmpFileParameters1);
                            if(delay_ms<100 && !(tmpFileParameters1 == null && finish)) {
                                loop++;
                                Log.d(TAG,"TimeShiftReadThread loop++, loop="+loop);
                            }
                        }
                        Log.d(TAG,"TimeShiftReadThread loop end, loop="+loop);
                        if(tmpFileParameters1 == null && finish){
                            Log.d(TAG,"TimeShiftReadThread End point, no data sent"+tmpFileParameters.toString());
                            if(tmpNext == true){
                                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_EOF, 0, 0, null);
                                mTimeshiftPresentationTimeUs=tmpFileParameters.getiPts()-1000000L;;
                                mPlayCurrentUs=mTimeshiftPresentationTimeUs;
                                useSpeed =1;
                                seekFlag=10;
                                playerResume();
                                //flushDvrPlayback();
                                mSeekMs = (mPlayCurrentUs/1000);
                                Log.d(TAG,"TimeShiftReadThread mPlayCurrentUs="+mPlayCurrentUs+" mTimeshiftPresentationTimeUs="+mTimeshiftPresentationTimeUs);
                                Log.d(TAG,"TimeShiftReadThread PESI_SVR_EVT_PVR_PLAY_EOF mSeekMs="+mSeekMs+" seekFlag="+seekFlag);
                            }
                            else{
                                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_SOF, 0, 0, null);
                                //Log.d(TAG,"PESI_SVR_EVT_PVR_PLAY_EOF, speed="+speed);
                                mPlayCurrentUs=mTimeshiftStartTimeUs+1000000L;
                                mTimeshiftPresentationTimeUs=mPlayCurrentUs;
                                useSpeed =1;
                                seekFlag=10;
                                playerResume();
                                //flushDvrPlayback();
                                mSeekMs = (mPlayCurrentUs/1000) + 2000;
                                Log.d(TAG,"TimeShiftReadThread mPlayCurrentUs="+mPlayCurrentUs+" mTimeshiftPresentationTimeUs="+mTimeshiftPresentationTimeUs);
                                Log.d(TAG,"TimeShiftReadThread PESI_SVR_EVT_PVR_PLAY_SOF mSeekMs="+mSeekMs+" seekFlag="+seekFlag);
                            }
                            continue;
                        }
                        try {
                            Log.d(TAG,"TimeShiftReadThread diffPts: "+diffPtsMs+" playPts: "+playPts+" iPts: "+tmpFileParameters.getiPts()+"delay_ms "+delay_ms);
                            //Log.d(TAG, "Thread.currentThread().sleep, delay_ms ="+delay_ms);
                            Thread.currentThread().sleep(delay_ms);
                            fastspeedMs += delay_ms;
                            // Log.d(TAG,"Thread.currentThread().sleep"+" mTotalReadSize:"+mTotalReadSize+" mFilelength:"+mFilelength+"fastspeedMs:"+fastspeedMs);
                        } catch (Exception e) {
                            // TODO: handle exception
                        }
                        if(tmpFileParameters != null ){
                            Log.d(TAG,"TimeShiftReadThread speed:"+speed+" mTotalReadSize:"+mTotalReadSize+" currentPts:"+currentPts+" targetPts: "+targetPts+" fileInfoPath: "+fileInfoPath+" tmpFileParameters"+tmpFileParameters.toString());
                            updateCurrentFileIndex(fileInfoPath);
                            PlaybackReadSize = tmpFileParameters.getiFrameSize();
                            mTotalReadSize = tmpFileParameters.getIbase();
                            mTimeshiftPresentationTimeUs = tmpFileParameters.getiPts();
                        }
                    }
                    else{
                        useSpeed = 1;
                        targetPts = 0;
                        if(mDiscardpFlag == 2){
                            mDiscardpFlag = 0;
                            Log.d(TAG, "TimeShiftReadThread mDiscardpFlag=2 to mDiscardpFlag=0");
                        }
                        if(speed == 1){
                            int currentPlayTime=getCurrentPlayTimeSec();
                            if(mTimeshiftReocrd.getTimeshiftRecStartTime() != 0 && currentPlayTime <= 1 && seekFlag == 0){
                                mSeekMs = (mPlayCurrentUs/1000)+3000;
                                Log.d(TAG,"TimeShiftReadThread speed == 1, currentPlayTime="+currentPlayTime+"mSeekMs="+mSeekMs);
                                seekFlag=10;
                                    continue;
                                }
                            }
                        if(speed == 2) {
                            int currentPlayTime=getCurrentPlayTimeSec();
                            int currentRecTime = mTimeshiftReocrd.getTimeshiftRecEndTime() - mTimeshiftReocrd.getTimeshiftRecStartTime();
                            Log.d(TAG, "TimeShiftReadThread PESI_SVR_EVT_PVR_PLAY_EOF, No seek required");
                            if(currentRecTime - currentPlayTime <= 2) {
                                mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_EOF, 0, 0, null);
                                useSpeed = 1;
                                playerResume();
                                Log.d(TAG, "TimeShiftReadThread PESI_SVR_EVT_PVR_PLAY_EOF, No seek required");
                            }
                        }
                        //manager1 = fileDataListManager.findFirstNearestPtsInData(false);
                        //tmpFileParameters = manager1.getFirstNearestPtsInData(false);
                        //mTimeshiftStartTimeUs = tmpFileParameters.getiPts();
                        //tmpFileParameters = null;
                    }
                    fileReadNumber = openFileRead(dvr_buff, mTotalReadSize, filePath);
                    Log.d(TAG,"TimeShiftReadThread openFileRead speed="+speed+" fileReadNumber="+fileReadNumber+" mTotalReadSize="+mTotalReadSize+" filePath="+filePath);
                    if(fileReadNumber>0) {
                        playbackOptionLock.lock();
                        if(fileReadNumber >= PlaybackReadSize)
                            ret = mDvrPlayback.read(dvr_buff, 0, PlaybackReadSize);
                        else
                            ret = mDvrPlayback.read(dvr_buff, 0, (fileReadNumber/188)*188);
                        playbackOptionLock.unlock();
                        if(ret > 0){
                            mTotalReadSize = mTotalReadSize+ret;
                            fastspeedMs = 0;
                        }
                        //Log.d(TAG,"speed="+speed+" ret="+ret+" PlaybackReadSize="+PlaybackReadSize+" mTotalReadSize="+mTotalReadSize);
                        Log.d(TAG,"TimeShiftReadThread mTotalReadSize:"+mTotalReadSize+" ret:"+ret+" PlaybackReadSize:"+PlaybackReadSize+" fileReadNumber:"+fileReadNumber);
                    }
                    else{
                        if(fileReadNumber == -2){
                            Log.d(TAG,"TimeShiftReadThread openFileRead fail, file does not exist, filePath"+filePath);
                        }
                        Log.d(TAG,"TimeShiftReadThread openFileRead fail, mTotalReadSize: "+mTotalReadSize+"filePath"+filePath);
                    }
                }

                if(mTotalReadSize >= mFilelength && speed <= 2 && speed>0){
                    Log.d(TAG,"TimeShiftReadThread mTotalReadSize="+mTotalReadSize+" mFilelength="+mFilelength+" speed="+speed);
                    if(untilNeedChangeFile(true) == true){
                        closeFileFile();
                        //filePath = RtkApplication.getDVRRecorderFilePath(mRecordMode)+"_INDEX_"+mCurrentFileIndex;
                        filePath = originalFilePath+"_INDEX_"+mCurrentFileIndex;
                        //filePath = originalFilePath+"_INDEX_"+mCurrentFileIndex;
                        dvrFile = null ;
                        mTotalReadSize = 0;
                        mFilelength = 0;
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
        private List<Long[]> recoordHeadTailList = null;
        String ptsFilePath;
        Long[] pairTmp=null;
        long ibaseTmp;
        long tmpPts;
        boolean switchingToScanMode=false;
        int tmpData;
        @Override
        public void run() {
            Log.d(TAG,"PvrReadThread pvr read Thread enter..");
            InitPlaybackInfo(fileDataListManager);
            while (dvrStop) {
                try {
                    Thread.currentThread().sleep(100);////40);
                    //fastspeedMs+=mSleepTime;
                } catch (InterruptedException e) {
                    // TODO: handle exception
                    Log.d(TAG,"PvrReadThread InterruptedException, e="+e);
                }
            }
            /*
            recoordHeadTailList[0]  recoordHeadTailList[1]  recoordHeadTailList[2]  recoordHeadTailList[3]  recoordHeadTailList[4]
            Head:getIbase	        Tail:getIbase           fileSize                Head:getiPts            Tail:getiPts
             */

            //InitPlaybackInfo(fileDataListManager);

            recoordHeadTailList=InitHeadTailPtsList(fileDataListManager);
            ibaseTmp=0;
            tmpData = recoordHeadTailList.size();
            if(tmpData == 0) {
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

            mWrapAroundMax = (int) (recoordHeadTailList.get(tmpData-1)[4]/ DvrRecorderController.PTS_MAX);

            //mLastPosition = 38*60*60;

            if(mLastPosition == 0) {
                manager1 = fileDataListManager.findNearestPtsInData(recoordHeadTailList.get(0)[3], true);
                mWrapAroundCnt = 0;
            }
            else{
                int i;
                tmpPts=(mLastPosition* 1000L * 1000L)+recoordHeadTailList.get(0)[3];
                for(i=0;i<recoordHeadTailList.size();i++){
                    ibaseTmp=ibaseTmp+recoordHeadTailList.get(i)[2];
                    if((tmpPts >= recoordHeadTailList.get(i)[3]) && (tmpPts <= recoordHeadTailList.get(i)[4]))
                        break;
                }
                if(i==recoordHeadTailList.size()) {
                    mLastPosition = 0;
                    manager1 = fileDataListManager.findNearestPtsInData(recoordHeadTailList.get(0)[3], true);
                    ibaseTmp=0;
                    mCurrentFileIndex=0;
                }
                else{
                    mCurrentFileIndex=i;
                    manager1 = fileDataListManager.findNearestPtsInData(tmpPts, true);
                    //mLastPosition=(int)(tmpPts/1000000);
                    ibaseTmp=ibaseTmp-recoordHeadTailList.get(i)[2];
                    mPlayStartUs = recoordHeadTailList.get(0)[3];
                }
                mWrapAroundCnt = (int) (tmpPts / DvrRecorderController.PTS_MAX);
            }

            if(mCurrentFileIndex == 0) {
                filePath = originalFilePath;
                fileInfoPath = filePath+DvrRecordInfo.FileInfoExtension;
            }
            else{
                filePath = originalFilePath+"_INDEX_"+mCurrentFileIndex;
                fileInfoPath = filePath+DvrRecordInfo.FileInfoExtension;
            }

            DvrRecordInfo.FileParameters tmpFileParameters = null;
            Log.d(TAG,"PvrReadThread dvrStop:"+dvrStop+" mIsPause:"+mIsPause+" mDvrPlayback:"+mDvrPlayback);
            while (!dvrStop) {
                //Log.d(TAG, "PvrReadThread mSleepTime="+mSleepTime);
                if(mDvrPlayback != null && mIsPause == false){
                    if((SeekNumber!=0 || mLastPosition!=0) && mPlayStartUs !=0){
                        if(mLastPosition != 0 && mPlayStartUs !=0 ) {
                            targetPts = mPlayStartUs + (mLastPosition)  * 1000 * 1000;
                            //Log.d(TAG,"targetPts="+targetPts+" mPlayStartUs="+mPlayStartUs+" mPresentationTimeUs="+mPresentationTimeUs+" ibaseTmp="+ibaseTmp);
                            mLastPosition = 0;
                        }
                        else {
                            targetPts = mPresentationTimeUs + (SeekNumber) * 5 * 1000 * 1000;
                        }
                        tmpFileParameters = manager1.findNearestPtsInData(targetPts,true);
                        if(tmpFileParameters != null){
                            mTotalReadSize = tmpFileParameters.getIbase()-ibaseTmp;
                            PlaybackReadSize =  readSize;
                            tmpFileParameters = null;
                            flushDvrPlayback();
                            //Log.d(TAG,"SeekNumber:"+SeekNumber+" mTotalReadSize:"+mTotalReadSize);
                        }
                        SeekNumber = 0;
                        targetPts = 0;
                    }
                    //else if(speed>2){
                    else if(speed>2 || speed<0){
                        long currentPts;
                        //Log.d(TAG,"targetPts="+targetPts+" mScanCurrentUs="+mScanCurrentUs);
                        if(targetPts != 0){
                            currentPts = targetPts;
                            targetPts = currentPts + (long)(fastspeedMs*speed*1000);
                            delayTimeMs = System.currentTimeMillis();
                            fastspeedMs=0;
                            targetPts = currentPts + (long)((delayTimeMs-startTimeMs)*speed*1000);
                            startTimeMs=delayTimeMs;
                        }
                        else{//roll
                            currentPts = mScanCurrentUs;//mPresentationTimeUs;
                            targetPts = currentPts;
                            startTimeMs = System.currentTimeMillis();
                            fastspeedMs = 0;
                            switchingToScanMode=true;
                        }

                        //Log.d(TAG, "Tail:getiPts="+recoordHeadTailList.get(mCurrentFileIndex)[4]);
                        if(recoordHeadTailList.size() > 0 ) {
                            if (((speed > 2) && (targetPts > recoordHeadTailList.get(mCurrentFileIndex)[4])) ||
                                    ((speed < 0) && (targetPts < recoordHeadTailList.get(mCurrentFileIndex)[3]))) {
                                int tmpFileIndex;
                                if(speed > 2 && mCurrentFileIndex == (recoordHeadTailList.size() - 1)){
                                    mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_EOF, 0, 0, null);
                                    Log.d(TAG,"PvrReadThread PESI_SVR_EVT_PVR_PLAY_EOF, speed="+speed);
                                    targetPts = recoordHeadTailList.get(mCurrentFileIndex)[4];
                                    mScanCurrentUs = targetPts;
                                    //playerResume();
                                    flushDvrPlayback();
                                    while (!dvrStop) {
                                        Log.d(TAG, "PvrReadThread PESI_SVR_EVT_PVR_PLAY_EOF");
                                        try {
                                            Thread.currentThread().sleep(100);////40);
                                            fastspeedMs=0;
                                        } catch (InterruptedException e) {
                                            // TODO: handle exception
                                        }
                                    }
                                    break;
                                }
                                else if(speed < 0 && mCurrentFileIndex == 0){
                                    mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_SOF, 0, 0, null);
                                    Log.d(TAG,"PvrReadThread PESI_SVR_EVT_PVR_PLAY_SOF, speed="+speed);
                                    mTotalReadSize=0;
                                    playerResume();
                                    flushDvrPlayback();
                                    Log.d(TAG,"PvrReadThread PESI_SVR_EVT_PVR_PLAY_SOF, mTotalReadSize="+mTotalReadSize);
                                    continue;
                                }
                                else {
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
                                    Log.d(TAG, "PvrReadThread tmpFileIndex=" + tmpFileIndex + " mCurrentFileIndex=" + mCurrentFileIndex);
                                    if (tmpFileIndex != mCurrentFileIndex) {
                                        long fileLengthTmp=0;
                                        if (mCurrentFileIndex > tmpFileIndex) {
                                            ibaseTmp = ibaseTmp + mFilelength;
                                        }
                                        else {
                                            if(mCurrentFileIndex == 0)
                                                fileLengthTmp=getFileLength(originalFilePath);
                                            else
                                                fileLengthTmp=getFileLength(originalFilePath + "_INDEX_" + mCurrentFileIndex);
                                            ibaseTmp = ibaseTmp - fileLengthTmp;
                                        }
                                        closeFileFile();
                                        dvrFile = null;
                                        mTotalReadSize = 0;
                                        mFilelength = 0;
                                        if (mCurrentFileIndex == 0) {
                                            filePath = originalFilePath;
                                        }
                                        else {
                                            filePath = originalFilePath + "_INDEX_" + mCurrentFileIndex;
                                        }
                                        if(speed < 0){
                                            mTotalReadSize=fileLengthTmp;
                                        }
                                        Log.d(TAG, "PvrReadThread need change filePath=" + filePath + " fileInfoPath=" + fileInfoPath);
                                        Log.d(TAG, "PvrReadThread ibaseTmp=" + ibaseTmp);
                                        manager1 = fileDataListManager.findNearestPtsInData(targetPts, true);
                                    }
                                }
                            }
                        }
                        tmpFileParameters = manager1.findNearestPtsInData(targetPts,true);


                        Log.d(TAG,"PvrReadThread mTotalReadSize="+mTotalReadSize+" tmpFileParameters.getIbase()="+tmpFileParameters.getIbase()+" ibaseTmp="+ibaseTmp);
                        if(switchingToScanMode == true && speed == 4){
                            if(mTotalReadSize > (tmpFileParameters.getIbase()-ibaseTmp)){
                                mTotalReadSize = (tmpFileParameters.getIbase()-ibaseTmp);
                                switchingToScanMode=false;
                                flushDvrPlayback();
                                Log.d(TAG,"PvrReadThread switchingToScanMode set to false, speed="+speed+" mTotalReadSize="+mTotalReadSize);
                            }
                        }
                        if(tmpFileParameters != null &&
                                ((speed >2 && (tmpFileParameters.getIbase()-ibaseTmp) >= mTotalReadSize) ||
                                        (speed <0 && (tmpFileParameters.getIbase()-ibaseTmp) <= mTotalReadSize))
                        ){
                            PlaybackReadSize = tmpFileParameters.getiFrameSize();
                            if(mCurrentFileIndex == 0)
                                mTotalReadSize = tmpFileParameters.getIbase();
                            else
                                mTotalReadSize = tmpFileParameters.getIbase()-ibaseTmp;
                            mScanCurrentUs = targetPts;

                            if(mSleepTime == 150){
                                Log.d(TAG, "PvrReadThread mSleepTime is 150==>flushDvrPlayback()");
                                flushDvrPlayback();
                                mSleepTime=100;
                            }
                            //Log.d(TAG,"mScanCurrentUs="+mScanCurrentUs+" targetPts="+targetPts);
                            Log.d(TAG,"PvrReadThread tmpFileParameters.getIbase()="+tmpFileParameters.getIbase()+" ibaseTmp="+ibaseTmp);
                            Log.d(TAG,"PvrReadThread need read file PlaybackReadSize="+PlaybackReadSize+" mTotalReadSize="+mTotalReadSize);
                        }
                        else{
                            try {
                                int delay_us = mSleepTime;
                                //long aaaTime=System.currentTimeMillis();
                                Thread.currentThread().sleep(delay_us);
                                //aaaTime = System.currentTimeMillis() - aaaTime;
                                //Log.d(TAG,"mSleepTime="+mSleepTime+" aaaTime="+aaaTime);
                                fastspeedMs += delay_us;
                                //Log.d(TAG,"delay_us="+delay_us+" fastspeedMs="+fastspeedMs);
                            } catch (InterruptedException e) {
                                // TODO: handle exception
                            }
                            continue;
                        }
                    }
                    else{
                        targetPts = 0;
                        if(PlaybackReadSize != readSize) {
                            PlaybackReadSize = readSize;
                        }
                        if(mDiscardpFlag == 2){
                            flushDvrPlayback();
                            mDiscardpFlag = 0;
                            Log.d(TAG, "PvrReadThread mDiscardpFlag=2 to mDiscardpFlag=0");
                        }
                    }
                    fileReadNumber = openFileRead(dvr_buff, mTotalReadSize, filePath);
                    Log.d(TAG,"PvrReadThread openFileRead, mTotalReadSize="+mTotalReadSize+" fileReadNumber="+fileReadNumber+" filePath="+filePath);
                    if(fileReadNumber>0) {
                        Log.d(TAG,"PvrReadThread playbackOptionLock start");
                        playbackOptionLock.lock();
                        if(fileReadNumber >= PlaybackReadSize) {
                            ret = mDvrPlayback.read(dvr_buff, 0, PlaybackReadSize);
                            if(speed < 0) {
                                //Log.d(TAG, "mread,PlaybackReadSize=" + PlaybackReadSize + " targetPts=" + targetPts + " mCurrentFileIndex=" + mCurrentFileIndex);
                                //Log.d(TAG, "mread,ibaseTmp="+ibaseTmp+" mTotalReadSize="+mTotalReadSize);
                                //Log.d(TAG, "mread,getIbase()="+tmpFileParameters.getIbase()+" getiFrameSize()="+tmpFileParameters.getiFrameSize());
                            }
                        }
                        else {
                            ret = mDvrPlayback.read(dvr_buff, 0, (fileReadNumber / 188) * 188);
                            if(speed < 0) {
                                //Log.d(TAG, "read,(fileReadNumber / 188) * 188=" + (fileReadNumber / 188) * 188 + " targetPts=" + targetPts + " mCurrentFileIndex=" + mCurrentFileIndex);
                                //Log.d(TAG, "mread,ibaseTmp="+ibaseTmp+" mTotalReadSize="+mTotalReadSize);
                                //Log.d(TAG, "mread,getIbase()="+tmpFileParameters.getIbase()+" getiFrameSize()="+tmpFileParameters.getiFrameSize());
                            }
                        }
                        playbackOptionLock.unlock();
                        Log.d(TAG,"PvrReadThread playbackOptionLock end");
                        if(ret > 0){
                            mTotalReadSize = mTotalReadSize+ret;
                            fastspeedMs = 0;
                            //Log.d(TAG,"fastspeedMs set to 0, ret="+ret);
                        }
                        Log.d(TAG,"PvrReadThread mDvrPlayback.read, ret="+ret+" mTotalReadSize="+mTotalReadSize+" mFilelength="+mFilelength);
                    }
                    else{
                        //if(speed < 0)
                            //Log.d(TAG,"fileReadNumber < 0");
                    }
                }
                //Log.d(TAG,"mFilelength="+mFilelength+" mTotalReadSize="+mTotalReadSize);
                //Log.d(TAG,"check change file");
                if(mRecordMode == DvrRecorderController.RECORDERMODE_RECORD){//&&(mTotalReadSize >= mFilelength - 187) && (isNeedRepeat) && (speed>0)){
                    if(speed > 0) {
                        boolean changeFile = false;
                        if((isNeedRepeat) && (mTotalReadSize >= mFilelength - 187)) {
                            if(speed <= 2){
                                int recoordHeadTailListSize = recoordHeadTailList.size();
                                if ((recoordHeadTailListSize > 0) && mCurrentFileIndex < (recoordHeadTailListSize - 1))
                                    changeFile = true;
                                if (changeFile) {
                                    Log.d(TAG,"PvrReadThread need change file, speed="+speed);
                                    closeFileFile();
                                    mCurrentFileIndex++;
                                    ibaseTmp = ibaseTmp + mFilelength;
                                    dvrFile = null;
                                    mTotalReadSize = 0;
                                    mFilelength = 0;
                                    filePath = originalFilePath + "_INDEX_" + mCurrentFileIndex;
                                    manager1 = fileDataListManager.findNearestPtsInData(recoordHeadTailList.get(mCurrentFileIndex)[3], true);
                                    //Log.d(TAG, "need change1 file mCurrentFileIndex=" + mCurrentFileIndex + " filePath=" + filePath + " fileInfoPath=" + fileInfoPath);
                                }
                                else{
                                    if((mCurrentFileIndex == (recoordHeadTailList.size() - 1)) && (mTotalRecTimeSec-getCurrentPlayTimeSec() <= 3)){
                                        mTotalReadSize = 0;
                                        fastspeedMs = 0;
                                        try {
                                            if(speed == 2)
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
                                            Log.d(TAG, "PvrReadThread PESI_SVR_EVT_PVR_PLAY_EOF");
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
                            /*
                            else {
                                if(mCurrentFileIndex == (recoordHeadTailList.size() - 1)) {
                                    Log.d(TAG,"need change file, speed="+speed);
                                    mTotalReadSize = 0;
                                    fastspeedMs = 0;
                                    playerResume();
                                    flushDvrPlayback();
                                    mPesiDtvCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_PVR_PLAY_EOF, 0, 0, null);
                                    while (!dvrStop) {
                                        Log.d(TAG, "PESI_SVR_EVT_PVR_PLAY_EOF");
                                        try {
                                            Thread.currentThread().sleep(100);////40);
                                            fastspeedMs=0;
                                        } catch (InterruptedException e) {
                                            // TODO: handle exception
                                        }
                                    }
                                    break;
                                }
                            }
                            */
                        }
                    }
                }
                try {
                    Thread.currentThread().sleep(mSleepTime);////40);
                    fastspeedMs+=mSleepTime;
                    Log.d(TAG,"PvrReadThread mSleepTime="+mSleepTime+" fastspeedMs="+fastspeedMs);
                } catch (InterruptedException e) {
                    // TODO: handle exception
                    Log.d(TAG,"PvrReadThread InterruptedException, e="+e);
                }

            }
            Log.d(TAG,"PvrReadThread pvr read Thread exit..");
        }
    }

    /*
    private class FileReadThread extends Thread {
        @Override
        public void run() {
            Log.d(TAG, "ReadThread start mChannelID =" + mChannelID);
            if(Cas_Do()){
                startPlayMediacodec();
            }
            long  ret = -1;
            int err =-1;
            boolean isNeedRepeat= false;
            int fastspeedMs = 0;
            isNeedRepeat = SettingSp.getInstance().getBoolean(SettingSp.DVR_REPEAT_MODE, false);
            while (!dvrStop) {
                if(mDvrPlayback !=null)
                    if(mIsPause ==false){
                        ret =-1;
                        if(SeekNumber!=0||(speed>2&&fastspeedMs>=fixedIntervals)){
                            flushDvrPlayback();
                            long SeekOffset = 0;
                            if(SeekNumber!=0){
                                SeekOffset = SeekEstimatedTimeFileTime((SeekNumber)*5*1000);//seek 5s
                                if(SeekOffset==0){
                                    SeekOffset =SeekNumber*((((mFilelength*5)/100)/188)*188);//seek 5%
                                }
                            }else if(speed>2){
                                //   SeekOffset = SeekEstimatedTimeFileTime((int) ((speed)*5*1000));

                                SeekOffset = SeekEstimatedTimeFileTime((int)speed*fixedIntervals);
                                Log.d(TAG, " speed = "+speed +"SeekOffset = " +SeekOffset);
                                fastspeedMs =0;
                            }
                            if(mTotalReadSize+SeekOffset<mFilelength&&mTotalReadSize+SeekOffset>=0){
                                mTotalReadSize+=SeekOffset;
                            }
                            if(mTotalReadSize>mFilelength){
                                mTotalReadSize = mFilelength;
                            }else if(mTotalReadSize<0){
                                mTotalReadSize =0;
                            }else {
                                //  mTotalReadSize =(mTotalReadSize/188)*188;
                                err = openFileRead(dvr_buff, mTotalReadSize, filePath);
                                //    Log.d(TAG, "Seek mDvrPlayback.read SeekOffset= "+SeekOffset+" dvr_buff[0] ="+dvr_buff[0]);

                                if(err>0) {
                                    if(err >= readSize)
                                        ret = mDvrPlayback.read(dvr_buff, 0, readSize);
                                    else
                                        ret = mDvrPlayback.read(dvr_buff, 0, (err/188)*188);
                                    mTotalReadSize = mTotalReadSize+ret;
                                }
                            }
                            SeekNumber = 0;
                        }else {
                            //setSpeed(speed);
                            err = openFileRead(dvr_buff,mTotalReadSize,filePath);
                            //    Log.d(TAG, "dvr_buff[0] ="+dvr_buff[0] +"mTotalReadSize =" +mTotalReadSize );
                            if(err>0) {
                                if(err >= readSize)
                                    ret = mDvrPlayback.read(dvr_buff, 0, readSize);
                                else
                                    ret = mDvrPlayback.read(dvr_buff, 0, (err/188)*188);
                                if(readSize!=ret ) {
                                    estimatedTimeAdd(ret, mSleepTime);
                                }
                                mTotalReadSize = mTotalReadSize+ret;
                            }
                        }
                    }
                    else{
                        ret =0;
                    }
                // Log.d(TAG, " read file ret =" + ret+" mTotalReadSize =" +mTotalReadSize+" mFilelength =" +mFilelength+" isNeedRepeat = "+isNeedRepeat+" mIsPause ="+mIsPause);
                if((mRecordMode != RtkApplication.RECORDERMODE_TIMEShIFT)&&(mTotalReadSize >= mFilelength - 187) && isNeedRepeat){
                    Log.d(TAG, " SEEK to 0 success... ");
                    flushDvrPlayback();
                    mTotalReadSize = 0;
                }else if(mRecordMode == RtkApplication.RECORDERMODE_TIMEShIFT&&(mTotalReadSize >= mFilelength)){
                    //Log.d(TAG,"need change File mTotalReadSize: "+mTotalReadSize+ "mFilelength: "+mFilelength);
                    if(untilNeedChangeFile() == true){
                        closeFileFile();
                        filePath = RtkApplication.getDVRRecorderFilePath(mRecordMode)+"_INDEX_"+mCurrentFileIndex;
                        dvrFile = null ;
                        mTotalReadSize = 0;
                        mFilelength = 0;
                    }
                }

                try {
                    Thread.currentThread().sleep(mSleepTime);////40);
                    fastspeedMs +=mSleepTime;
                } catch (InterruptedException e) {
                    // TODO: handle exception
}
            }

            Log.d(TAG,"drv read thread exit..");

        }
    }
    */

    public ParcelFileDescriptor openFile(String filePath) {
        Log.d(TAG, "start play... openFile");
        File file = new File(filePath);
        if(!file.exists()){
            //Toast.makeText(RtkApplication.getApplication(), "the file not exist ,return!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "the file not exist ,return!");
            return null;
        }
        ParcelFileDescriptor pfd;
        try {
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return pfd;
    }

    public void closeFileFile(){
        try {
            Log.d(TAG,"stop  file...");
            if(InputStream !=null) {
                InputStream.close();
            }
            InputStream = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getFileLength(String filePath){
        File dvrFileTmp =null;
        long dvrFileTmpLen=0;
        dvrFileTmp = new File(filePath);
        if(dvrFileTmp.exists() == true){
            dvrFileTmpLen =dvrFileTmp.length();
        }
        dvrFileTmp=null;
        return dvrFileTmpLen;
    }

    public int  openFileRead(byte b[],long Offset,String filePath) {
        int err =-1;
        if(dvrFile==null) {
            Log.d(TAG, "openFileRead dvrFile is null,new File=>filePath: "+filePath);
            dvrFile = new File(filePath);
        }
        if(dvrFile.exists() ==false){
            Log.d(TAG, "the file not exist ,return! + "+dvrFile.getAbsolutePath().toString());
            return -2;
        }
        mFilelength = dvrFile.length();
        //Log.d(TAG, "openFileRead  filePath: "+filePath+" mFilelength: "+mFilelength);
        if(InputStream ==null) {
            Log.d(TAG, "InputStream ==null");
            try {
                InputStream = new RandomAccessFile(dvrFile,"r");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        try {
            if(mFilelength>Offset){
                InputStream.seek(Offset);
                err= InputStream.read(b,0,1024*1024*5);
            }else{
                Log.d(TAG, "mFilelength<=Offset,don't InputStream.read, mFilelength: "+mFilelength+" Offset: "+Offset);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return err;
    }

    private boolean untilNeedChangeFile(Boolean next){
        boolean err = false;
        if(next)
        err = untilCheckNeedUseNextPath();
        else
            err = untilCheckNeedUsePreviousPath();
        return err;
    }

    private boolean untilCheckNeedUseNextPath(){
        boolean err = false;
        Log.d(TAG,"untilCheckNeedUseNextPath DvrRecorderController.mLockFileIndexLock.lock()");
        DvrRecorderController.mLockFileIndexLock.lock();

        if(DvrRecorderController.mInfosFileIndexList != null && DvrRecorderController.mInfosFileIndexList.size()>0){
            if(mCurrentFileIndex == DvrRecorderController.mInfosFileIndexList.get(DvrRecorderController.mInfosFileIndexList.size()-1)){
                Log.d(TAG,"file mCurrentFileIndex is end:: "+mCurrentFileIndex);
                err = false;
            }else if(DvrRecorderController.mInfosFileIndexList.contains(mCurrentFileIndex+1)
                    &&dvrFile!=null&&mTotalReadSize >= dvrFile.length()){
                mCurrentFileIndex++;
                Log.d(TAG,"find next file mCurrentFileIndex::"+mCurrentFileIndex);
                err = true;
            }else if(mCurrentFileIndex+1<=DvrRecorderController.mInfosFileIndexList.get(0)){
                Log.d(TAG,"find next file error! use index 0 mCurrentFileIndex::"+mCurrentFileIndex);
                mCurrentFileIndex = DvrRecorderController.mInfosFileIndexList.get(0);
                err = true;
            }
            DvrRecorderController.mLockFileIndex = mCurrentFileIndex;
        }
        DvrRecorderController.mLockFileIndexLock.unlock();
        Log.d(TAG,"untilCheckNeedUseNextPath DvrRecorderController.mLockFileIndexLock.unlock()");
        return err;
    }

    private boolean untilCheckNeedUsePreviousPath(){
        boolean err = false;
        DvrRecorderController.mLockFileIndexLock.lock();
        if(DvrRecorderController.mInfosFileIndexList != null &&DvrRecorderController.mInfosFileIndexList.size()>0){
            if(mCurrentFileIndex == DvrRecorderController.mInfosFileIndexList.get(0)){
                Log.d(TAG,"file mCurrentFileIndex is end:: "+mCurrentFileIndex);
                err = false;
            }else if(DvrRecorderController.mInfosFileIndexList.contains(mCurrentFileIndex-1)){
                mCurrentFileIndex--;
                Log.d(TAG,"find Previous file mCurrentFileIndex::"+mCurrentFileIndex);
                err = true;
            }else if(mCurrentFileIndex-1>=DvrRecorderController.mInfosFileIndexList.get(DvrRecorderController.mInfosFileIndexList.size()-1)){
                Log.d(TAG,"find Previous file error! use index 0 mCurrentFileIndex::"+mCurrentFileIndex);
                mCurrentFileIndex = DvrRecorderController.mInfosFileIndexList.get(DvrRecorderController.mInfosFileIndexList.size()-1);
                err = true;
            }
            DvrRecorderController.mLockFileIndex = mCurrentFileIndex;
        }
        DvrRecorderController.mLockFileIndexLock.unlock();
        return err;
    }

    private boolean untilFindFileCurrentFileIndex(long ms){
        DvrRecorderController.mLockFileIndexLock.lock();
        if(!(DvrRecorderController.mInfosFileIndexList!=null&&DvrRecorderController.mInfosFileIndexList.size()>2)){
            DvrRecorderController.mLockFileIndexLock.unlock();
            return false;
        }

        for (Map.Entry<Integer, Long> entry : DvrRecorderController.mFileIndexMapTime.entrySet()) {
            int key = entry.getKey();
            long value = entry.getValue();
            Log.d(TAG,"Key: " + key + ", Value: " + value);
        }
        Log.d(TAG," untilFindFileCurrentFileInde DvrRecorderController.mInfosFileIndexList.get(0)::"+DvrRecorderController.mInfosFileIndexList.get(0));

        DvrRecorderController.mLockFileIndex = DvrRecorderController.mInfosFileIndexList.get(0);
        long fileStartTime = DvrRecorderController.mFileIndexMapTime.get(DvrRecorderController.mInfosFileIndexList.get(0));
        long fileEndTime = DvrRecorderController.mFileIndexMapTime.get(DvrRecorderController.mInfosFileIndexList.get(DvrRecorderController.mInfosFileIndexList.size()-1));
        long time = 0;
        if(ms>=fileStartTime&&ms<=fileEndTime){
            for (Integer index : DvrRecorderController.mInfosFileIndexList) {
                time = DvrRecorderController.mFileIndexMapTime.get(index);
                if(time>=ms){
                    Log.d(TAG,"seek to mCurrentFileIndex:: "+mCurrentFileIndex);
                    mCurrentFileIndex = index;
                    break;
                }
            }
        }else{
            Log.d(TAG,"seek ms error seek to 0");
            mCurrentFileIndex = DvrRecorderController.mInfosFileIndexList.get(0);
        }
        DvrRecorderController.mLockFileIndex = mCurrentFileIndex;
        DvrRecorderController.mLockFileIndexLock.unlock();
        return true;
    }

    private boolean untilSetFileCurrentFileIndex(String path){
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

    private boolean untilUpdateTimeShiftInfo(DvrRecordInfo.FileDataListManager manger){
        if(manger.FileDataManagerList == null)
            return false;

        DvrRecorderController.mLockFileIndexLock.lock();
        Log.d(TAG,"untilUpdateTimeShiftInfo start process");
        try {
            if (manger.FileDataManagerList.isEmpty()) {
                //init
                Log.d(TAG,"untilUpdateTimeShiftInfo manger.FileDataManagerList.isEmpty() == true");
                manger.FileDataManagerList.clear();
                for (Integer list_index : DvrRecorderController.mInfosFileIndexList) {
                    String basePath = originalFilePath + "_INDEX_" + list_index;
                    String filePath = basePath + DvrRecordInfo.FileInfoExtension;
                    DvrRecordInfo.FileDataManager manager = new DvrRecordInfo.FileDataManager(filePath, basePath);
                    ParcelFileDescriptor fd = openFile(filePath);
                    manager.parseFileContent(fd);
                    manger.FileDataManagerList.add(manager);
                }
                Log.d(TAG,"Initialized list size: " + manger.FileDataManagerList.size());
            } else {
                Log.d(TAG,"untilUpdateTimeShiftInfo mcurrentPaths handle");
                Set<String> currentPaths = new LinkedHashSet<>();
                // Set<String> currentPaths = new HashSet<>();
                for (Integer index : DvrRecorderController.mInfosFileIndexList) {
                    String basePath = originalFilePath + "_INDEX_" + index;
                    currentPaths.add(basePath + DvrRecordInfo.FileInfoExtension);
                }

                // remove
                Log.d(TAG,"untilUpdateTimeShiftInfo check manger.FileDataManagerList, remove outdated");
                Iterator<DvrRecordInfo.FileDataManager> iterator = manger.FileDataManagerList.iterator();
                while (iterator.hasNext()) {
                    DvrRecordInfo.FileDataManager manager = iterator.next();
                    if (!currentPaths.contains(manager.getFileName())) {
                        Log.d(TAG,"Removing outdated: " + manager.getFileName());
                        iterator.remove();
                    }
                }

                // add
                Log.d(TAG,"untilUpdateTimeShiftInfo check new file info, Added new");
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
                        Log.d(TAG,"Added new: " + filePath);
                    }
                }

                Log.d(TAG,"untilUpdateTimeShiftInfo update last file info");
                DvrRecordInfo.FileDataManager manager = manger.FileDataManagerList.get(manger.FileDataManagerList.size() - 1);
                try (ParcelFileDescriptor fd = openFile(manager.getFileName())) {
                    manager.parseFileContent(fd);
                    Log.d(TAG,"fine name: "+manager.getFileName()+" size: "+manager.fileParametersList.size());
                } catch (IOException e) {
                    Log.e(TAG," error!!" + manager.getFileName(), e);
                }

//                for (DvrRecordInfo.FileDataManager manager : manger.FileDataManagerList) {
//                    try (ParcelFileDescriptor fd = openFile(manager.getFileName())) {
//                        manager.parseFileContent(fd);
//                        Log.d(TAG,"fine name: "+manager.getFileName()+" size: "+manager.fileParametersList.size());
//                    } catch (IOException e) {
//                        Log.e(TAG," error!!" + manager.getFileName(), e);
//                    }
//                }

            }
            Log.d(TAG,"size: "+manger.FileDataManagerList.size());

        } finally {
            Log.d(TAG,"untilUpdateTimeShiftInfo end process");
            DvrRecorderController.mLockFileIndexLock.unlock();
        }
        return true;
    }

    Boolean updateCurrentFileIndex(String fileName){
        Boolean error = true;
        int number = 0;
        Pattern pattern = Pattern.compile("_INDEX_(\\d+)\\.dat");
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.find()) {
            number = Integer.parseInt(matcher.group(1));
            DvrRecorderController.mLockFileIndex = number;
            mCurrentFileIndex = number;
        } else {
            LogUtils.d("fileName is error! "+fileName);
            error = false;
        }
        return error;
    }

    public void playerStop(){

        dvrStop = true;
        mIsPause =false;

        try {
            if (readThread != null) {
                readThread.join();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeFileFile();
        if(speed != 1){
            resetaudioParams();//speed set 0.audio reset
        }
        if(mDvrPlayback !=null){
            mDvrPlayback.stop();
            mDvrPlayback.close();
            mDvrPlayback =null;
        }
        if(dvrFile!=null){
            dvrFile =null;
        }
        mCurrentFileIndex = 0;
        speed = 1;
        estimatedTimeClear();

        mTotalReadSize=0;
    }


    public void playerScan() {

    }


    public void playerPlay() {

    }


    public void playerPause() {
        Log.d(TAG,"playerPause start");
        if ((mVideoMediaCodec != null) || (mAudioTrack != null)) {
            if (mIsPause != true) {
                if (mVideoMediaCodec != null) {
                    Bundle mParamsTrick = new Bundle();
                    mParamsTrick.putInt("vendor.tis.DecodeIFrameOnly.value", 0);
                    mParamsTrick.putInt("vendor.tis.dvr-playback-speed.value", (int)(1*1000));
                    mVideoMediaCodec.setParameters(mParamsTrick);
                }
                Log.d(TAG,"playerPause setSpeed");
                setSpeed(0.0f);
                flushDvrPlayback();
                mIsPause = true;
            }
        }
        Log.d(TAG,"playerPause end");
    }

    public void playerResume() {
        Log.d(TAG,"playerResume start");
        if ((mVideoMediaCodec != null) || (mAudioTrack != null)) {
            if(mAudioTrack != null) {
                Log.d(TAG,"playerResume setSpeed, speed="+speed);
                if(speed<0||speed>2.0){
                    Bundle mParamsTrick = new Bundle();
                    if(mRecordMode == DvrRecorderController.RECORDERMODE_TIMEShIFT) {
                        mPlayCurrentUs = mTimeshiftPresentationTimeUs;
                    }
                    if(mRecordMode == DvrRecorderController.RECORDERMODE_RECORD) {
                        mPlayCurrentUs = mScanCurrentUs;
                    }
                    mWrapAroundCnt = (int) (mPlayCurrentUs / DvrRecorderController.PTS_MAX);
                    mParamsTrick.putInt("vendor.tis.DecodeIFrameOnly.value", 0);
                    //Log.d(TAG,"vendor.tis.DecodeIFrameOnly 0");
                    mParamsTrick.putInt("vendor.tis.dvr-playback-speed.value", (int)(1*1000));
                    //Log.d(TAG,"vendor.tis.dvr-playback-speed 1*1000");
                    mVideoMediaCodec.setParameters(mParamsTrick);
                    AudioManager audioManager = mContext.getSystemService(AudioManager.class);
                    audioManager.setParameters("clear_video_pts=0");
                    //Log.d(TAG,"set clear_video_pts 0");
                    audioManager.setParameters("rtk_mute=0");
                    //Log.d(TAG,"set rtk_mute 0");
                }
                PlaybackParams aparams = mAudioTrack.getPlaybackParams();
                aparams.setSpeed(1.0f);

                speed = 1;
                mAudioTrack.setPlaybackParams(aparams);
                flushDvrPlayback();
            }
            mIsPause =false;
            if(mDiscardpFlag == 1 && speed == 1){
                if(mRecordMode == DvrRecorderController.RECORDERMODE_TIMEShIFT) {
                    mWrapAroundCnt = (int) (mTimeshiftPresentationTimeUs / DvrRecorderController.PTS_MAX);
                }
                if(mRecordMode == DvrRecorderController.RECORDERMODE_RECORD) {
                    mWrapAroundCnt = (int) (mScanCurrentUs / DvrRecorderController.PTS_MAX);
                }
                Log.d(TAG, "mDiscardpFlag=1 to mDiscardpFlag=2, speed="+speed+" mWrapAroundCnt="+mWrapAroundCnt);
                mDiscardpFlag = 2;
            }
        }
        Log.d(TAG,"playerResume end");
    }

    public void fastForward() {
        float tmpSpeed = speed;
        Log.d(TAG,"fastForward start, speed="+speed);
        if (mVideoMediaCodec != null) {
            if(speed >= 1.0){
                if(speed == 2.0) {
                    mDiscardpFlag = 1;
                    mPrePlayUs = 0;
                    Log.d(TAG, "mDiscardpFlag set to 1");
                    if(mRecordMode == DvrRecorderController.RECORDERMODE_TIMEShIFT) {
                        mTimeshiftPresentationTimeUs = mPlayCurrentUs;
                    }
                    if(mRecordMode == DvrRecorderController.RECORDERMODE_RECORD) {
                        mScanCurrentUs = mPlayCurrentUs;
                    }
                }
                speed*=2;
                if(speed>32){
                    if(mRecordMode == DvrRecorderController.RECORDERMODE_TIMEShIFT) {
                        mPlayCurrentUs = mTimeshiftPresentationTimeUs;
                    }
                    if(mRecordMode == DvrRecorderController.RECORDERMODE_RECORD) {
                        mPlayCurrentUs = mScanCurrentUs;
                    }
                    mWrapAroundCnt = (int) (mPlayCurrentUs / DvrRecorderController.PTS_MAX);
                    Log.d(TAG,"The speed will be set to 1x, mPlayCurrentUs="+mPlayCurrentUs+" mWrapAroundCnt="+mWrapAroundCnt);
                    speed = 1.0f;
                }
            }
            else{
                speed = 1.0f;
            }
            //Log.d(TAG,"speed = "+speed);
            Bundle mParamsTrick = new Bundle();
            Log.d(TAG,"new Bundle, speed="+speed);
            //if(speed> 2){
            if(speed > 2||speed < 0){
                //flushDvrPlayback();
                mParamsTrick.putInt("vendor.tis.DecodeIFrameOnly.value", 1);
                mParamsTrick.putInt("vendor.tis.dvr-playback-speed.value", (int)(Math.abs(speed)*1000));
                Log.d(TAG,"vendor.tis.DecodeIFrameOnly.value 1");
                mVideoMediaCodec.setParameters(mParamsTrick);
                setSpeed(speed);
                flushDvrPlayback();
            }else{
                mParamsTrick.putInt("vendor.tis.DecodeIFrameOnly.value", 0);
                mParamsTrick.putInt("vendor.tis.dvr-playback-speed.value", (int)(1*1000));
                Log.d(TAG,"vendor.tis.DecodeIFrameOnly.value 0");
                mVideoMediaCodec.setParameters(mParamsTrick);
                setSpeed(speed);
                if(tmpSpeed > 2 || tmpSpeed < 0)
                    flushDvrPlayback();
            }
//            setSpeed(speed);
//            flushDvrPlayback();
            //Toast.makeText(RtkApplication.getApplication(), "speek =" + speed, Toast.LENGTH_SHORT).show();
            if(mDiscardpFlag == 1 && speed == 1){
                if(mRecordMode == DvrRecorderController.RECORDERMODE_TIMEShIFT) {
                    mWrapAroundCnt = (int) (mTimeshiftPresentationTimeUs / DvrRecorderController.PTS_MAX);
                }
                if(mRecordMode == DvrRecorderController.RECORDERMODE_RECORD) {
                    mWrapAroundCnt = (int) (mScanCurrentUs / DvrRecorderController.PTS_MAX);
                }
                mDiscardpFlag = 2;
                Log.d(TAG, "mDiscardpFlag=1 to mDiscardpFlag=2, speed="+speed+" mWrapAroundCnt="+mWrapAroundCnt);
            }
        }
        else{
            Log.d(TAG,"mVideoMediaCodec is null");
        }
        Log.d(TAG,"fastForward end, speed="+speed);
    }

    public void backForward() {
        float tmpSpeed = speed;
        Log.d(TAG,"backForward start, speed="+speed);
        if (mVideoMediaCodec != null) {
            if(speed >1.0){
                if(mRecordMode == DvrRecorderController.RECORDERMODE_TIMEShIFT) {
                    mPlayCurrentUs = mTimeshiftPresentationTimeUs;
                }
                if(mRecordMode == DvrRecorderController.RECORDERMODE_RECORD) {
                    mPlayCurrentUs = mScanCurrentUs;
                }
                mWrapAroundCnt = (int) (mPlayCurrentUs / DvrRecorderController.PTS_MAX);
                speed = 1.0f;
            }
            else if(speed < 0.0){
                speed*=2;
                if(speed < -32.0f)
                    speed = -2.0f;
            }
            else{
                if(speed == 1.0) {
                    mDiscardpFlag = 1;
                    mPrePlayUs = 0;
                    Log.d(TAG, "mDiscardpFlag set to 1");
                    if(mRecordMode == DvrRecorderController.RECORDERMODE_TIMEShIFT) {
                        mTimeshiftPresentationTimeUs = mPlayCurrentUs;
                    }
                    if(mRecordMode == DvrRecorderController.RECORDERMODE_RECORD) {
                        mScanCurrentUs = mPlayCurrentUs;
                    }
                }
                //flushDvrPlayback();
                speed = -2.0f;
            }
            Bundle mParamsTrick = new Bundle();
            Log.d(TAG,"new Bundle, speed="+speed);
            //if(speed> 2){
            if(speed > 2||speed < 0){
                //flushDvrPlayback();
                mParamsTrick.putInt("vendor.tis.DecodeIFrameOnly.value", 1);
                mParamsTrick.putInt("vendor.tis.dvr-playback-speed.value", (int)-(Math.abs(speed)*1000));
                mVideoMediaCodec.setParameters(mParamsTrick);
                Log.d(TAG,"vendor.tis.DecodeIFrameOnly.value 1");
                setSpeed(speed);
                flushDvrPlayback();
            }else{
                mParamsTrick.putInt("vendor.tis.DecodeIFrameOnly.value", 0);
                mParamsTrick.putInt("vendor.tis.dvr-playback-speed.value", (int)-(1*1000));
                mVideoMediaCodec.setParameters(mParamsTrick);
                Log.d(TAG,"vendor.tis.DecodeIFrameOnly.value 0");
                setSpeed(speed);
                if(tmpSpeed > 2 || tmpSpeed < 0)
                    flushDvrPlayback();
            }
//            setSpeed(speed);
//            flushDvrPlayback();
            if(mDiscardpFlag == 1 && speed == 1){
                if(mRecordMode == DvrRecorderController.RECORDERMODE_TIMEShIFT) {
                    mWrapAroundCnt = (int) (mTimeshiftPresentationTimeUs / DvrRecorderController.PTS_MAX);
                }
                if(mRecordMode == DvrRecorderController.RECORDERMODE_RECORD) {
                    mWrapAroundCnt = (int) (mScanCurrentUs / DvrRecorderController.PTS_MAX);
                }
                mDiscardpFlag = 2;
                Log.d(TAG, "mDiscardpFlag=1 to mDiscardpFlag=2, speed="+speed+" mWrapAroundCnt="+mWrapAroundCnt);
            }
            //Toast.makeText(RtkApplication.getApplication(), " speek =" + speed, Toast.LENGTH_SHORT).show();
////            mIsPause =false;
        }
        else{
            Log.d(TAG,"mVideoMediaCodec is null");
        }
        Log.d(TAG,"backForward end, speed="+speed);
    }

    public void resetaudioParams() {
        LogUtils.d("enter ");
        if (mVideoMediaCodec != null) {
            Bundle mParamsTrick = new Bundle();
            mParamsTrick.putInt("vendor.tis.DecodeIFrameOnly.value", 0);
            mParamsTrick.putInt("vendor.tis.dvr-playback-speed.value", (int)(1*1000));
            mVideoMediaCodec.setParameters(mParamsTrick);
        }
        AudioManager audioManager = mContext.getSystemService(AudioManager.class);
        audioManager.setParameters("clear_video_pts=0");
        LogUtils.d("set clear_video_pts 0");
        audioManager.setParameters("rtk_mute=0");
        LogUtils.d("set rtk_mute 0");
        flushDvrPlayback();
    }

    public synchronized void setSpeed(float speed){
        Log.d(TAG,"setSpeed in, speed="+speed);
        if(mIsPause) {
            Log.d(TAG,"mIsPause is true, return");
            return;
        }
        if(speed < 0.0 || speed > 2.0) {
            if (mVideoMediaCodec != null){
            AudioManager audioManager = mContext.getSystemService(AudioManager.class);
            audioManager.setParameters("clear_video_pts=1");
            //Log.d(TAG,"set clear_video_pts 1");
            audioManager.setParameters("rtk_mute=1");
                Log.d(TAG,"set clear_video_pts 1, set rtk_mute 1");
            //Log.d(TAG,"set rtk_mute 1");
            }
        } else if(speed >= 0.0 && speed <= 2.0) {
            if (mVideoMediaCodec != null) {
            AudioManager audioManager = mContext.getSystemService(AudioManager.class);
            audioManager.setParameters("clear_video_pts=0");
            //Log.d(TAG,"set clear_video_pts 0");
            audioManager.setParameters("rtk_mute=0");
                Log.d(TAG,"set clear_video_pts 0, set rtk_mute 0");
            //Log.d(TAG,"set rtk_mute 0");
        }
        }

        if(mAudioTrack!=null) {
            PlaybackParams aparams =  mAudioTrack.getPlaybackParams();

            if(speed>=0.0f&&speed <=2.0f){
                Log.d(TAG,"mAudioTrack set speed "+speed);
                aparams.setSpeed(speed);
                mAudioTrack.setPlaybackParams(aparams);
            }
            mIsPause =false;
        }
        else{
            Log.d(TAG,"mAudioTrack is null");
        }

        /*
        if(mVideoMediaCodec != null && mAudioTrack !=null) {
            PlaybackParams aparams =  mAudioTrack.getPlaybackParams();
            if(speed<0){
                // speed = 1/(-speed);
                //speed =-speed;
                float tmpSpeed = 1.0f;
                //LogUtils.d("speed "+speed +"Math.abs(speed)"+Math.abs(speed)+"tmpSpeed:"+tmpSpeed);
                if(aparams.getSpeed()!=Math.abs(tmpSpeed)) {
                    aparams.setSpeed(Math.abs(tmpSpeed));
                    mAudioTrack.setPlaybackParams(aparams);
                }
            }
            else if (speed <= 2) {
                if(aparams.getSpeed()!=speed) {
                    aparams.setSpeed(speed);
                    mAudioTrack.setPlaybackParams(aparams);
                }
            }
            else {
                //   if(aparams.getSpeed()!=2) {
                //       aparams.setSpeed(2);
                mAudioTrack.setPlaybackParams(aparams);
                //    }
            }
            mIsPause =false;
        }
        else{
            if(mAudioTrack !=null){
                PlaybackParams aparams =  mAudioTrack.getPlaybackParams();
                if (speed <= 2.0) {
                    if(aparams.getSpeed()!=speed) {
                        aparams.setSpeed(speed);
                        mAudioTrack.setPlaybackParams(aparams);
                    }
                }
                mIsPause =false;
            }
        }
        */
    }

    public void rewind() {
        if(dvrStop || mISStartPlaying)
            return;
        SeekNumber =-1;
    }


    public void playerSeek() {
        if(dvrStop || mISStartPlaying)
            return;
        SeekNumber =1;
    }
    public void seekMs(long ms){
        mSeekMs = ms;
    }

    private void estimatedTimeAdd(long size ,int timeMs){
        if(size ==0||timeMs==0){
            return;
        }
        estimatedTimeFileSize+=size;
        estimatedTimeFileMs+=timeMs;
    }
    private void estimatedTimeClear(){
        Log.d(TAG,"clear estimatedTime ");
        estimatedTimeFileSize =0;
        estimatedTimeFileMs =0;
    }

    private long SeekEstimatedTimeFileTime(int seekTimeMs){
        if(estimatedTimeFileSize==0||estimatedTimeFileMs==0){
            return 0;
        }else {
            return (((estimatedTimeFileSize / estimatedTimeFileMs) * seekTimeMs)/188)*188;
        }
    }

    private int findCurrentFileIndex(String fileName){
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

    private static int countDatFiles(String directoryPath,String extension) {
        File dir = new File(directoryPath);
        if (!dir.exists() || !dir.isDirectory()) {
            Log.d(TAG,"Directory is not exist,directoryPath="+directoryPath);
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
            Log.d(TAG,"Directory is not exist,filePath="+filePath);
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
            Log.d(TAG,"File does not exist or is not a file: " + filePath);
            return 0;
        }
    }

    private List<Long[]> InitHeadTailPtsList(DvrRecordInfo.FileDataListManager manger) {
        List<Long[]> resultList = new ArrayList<>();
        int i,j;
        if (manger.FileDataManagerList.size() == 0) {
            return resultList;
        }

        for(i=0;i<manger.FileDataManagerList.size();i++){
            long head=0,tail=0,filesize=0,headPts=0,tailPts=0;
            for(j=0;j<manger.FileDataManagerList.get(i).fileParametersList.size();j++) {
                if(j == 0) {
                    head = manger.FileDataManagerList.get(i).fileParametersList.get(0).getIbase();
                    headPts = manger.FileDataManagerList.get(i).fileParametersList.get(0).getiPts();
                }
                if(j+1 == manger.FileDataManagerList.get(i).fileParametersList.size()) {
                    tail = manger.FileDataManagerList.get(i).fileParametersList.get(j).getIbase();
                    tailPts=manger.FileDataManagerList.get(i).fileParametersList.get(j).getiPts();
                }
            }
            filesize = getFileSize(manger.FileDataManagerList.get(i).getbaseName());
            resultList.add(new Long[]{head, tail, filesize, headPts, tailPts});
        }
        return resultList;
    }

    private boolean InitPlaybackInfo(DvrRecordInfo.FileDataListManager manger){
        long startTime,endTime,startTmpTime;

        if(manger.FileDataManagerList == null) {
            Log.d(TAG,"manger.FileDataManagerList == null");
            return false;
        }
        String directoryPath;
        String playBackInfoFilePath;
        String playBackInfoFileBasePath;
        DvrRecordInfo.FileDataManager manager1;
        ParcelFileDescriptor fileInfoPathFd;
        int infoCnt,index;

        startTime=DvrRecorderController.gettime();
        directoryPath=getDirectoryPath(originalFilePath);
        if(directoryPath == null)
            return false;
        infoCnt=countDatFiles(directoryPath,"dat");
        if(infoCnt == 0)
            return false;

        if(manger.FileDataManagerList.isEmpty() == false)
            manger.FileDataManagerList.clear();
        Log.d(TAG,"manger.FileDataManagerList="+manger.FileDataManagerList.size());
        DvrRecorderController.mLockFileIndexLock.lock();
        manger.FileDataManagerList.clear();
        Log.d(TAG,"duratioTime="+(DvrRecorderController.gettime()-startTime));
        for (index=0;index<infoCnt;index++) {
            startTmpTime=DvrRecorderController.gettime();
            if(index == 0){
                playBackInfoFileBasePath = originalFilePath ;
            }
            else {
                playBackInfoFileBasePath = originalFilePath + "_INDEX_" + index;
            }
            playBackInfoFilePath = playBackInfoFileBasePath + DvrRecordInfo.FileInfoExtension;
            Log.d(TAG,"playBackInfoFileBasePath="+playBackInfoFileBasePath+" playBackInfoFilePath"+playBackInfoFilePath);
            manager1 = new DvrRecordInfo.FileDataManager(playBackInfoFilePath,playBackInfoFileBasePath);
            fileInfoPathFd =  openFile(playBackInfoFilePath);
            manager1.parseFileContent(fileInfoPathFd);
            manger.FileDataManagerList.add(manager1);
            //Log.d(TAG,"playBackInfoFileBasePath="+playBackInfoFileBasePath+" duratioTime="+(DvrRecorderController.gettime()-startTmpTime));
        }
        DvrRecorderController.mLockFileIndexLock.unlock();

        endTime=DvrRecorderController.gettime();
        Log.d(TAG,"manger.FileDataManagerList="+manger.FileDataManagerList.size()+" endTime-startTime="+(endTime-startTime));
        /*
        for(int i=0;i<manger.FileDataManagerList.size();i++) {
            Log.d(TAG, "manger.FileDataManagerList.get(" + i + ").getFileName()=" + manger.FileDataManagerList.get(i).getFileName());
            Log.d(TAG, "manger.FileDataManagerList.get(" + i + ").getFileName().fileParametersList.size()=" + manger.FileDataManagerList.get(i).fileParametersList.size());
        }
        */
        return true;
    }

    public static long getElapsedMillis() {
        long currentNano = System.nanoTime();
        return currentNano / 1_000_000;
    }

}




