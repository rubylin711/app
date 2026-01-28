package com.prime.dtv.service.Player;

import android.media.MediaFormat;
import android.media.tv.tuner.Tuner;
import android.media.tv.tuner.filter.AvSettings;
import android.media.tv.tuner.filter.Filter;
import android.media.tv.tuner.filter.FilterCallback;
import android.media.tv.tuner.filter.FilterConfiguration;
import android.media.tv.tuner.filter.FilterEvent;
import android.media.tv.tuner.filter.MediaEvent;
import android.media.tv.tuner.filter.TsFilterConfiguration;
import android.util.Log;

import com.prime.dtv.utils.LogUtils;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.Executor;

public class Track {
    private static final String TAG = "Track";

    private Tuner mTuner;
    private MediaFormat mFormat;
    private int mPid;
    private int mCaSystemId;
    private int mEcmPid;
    private byte[] mPrivateData;
    private boolean mIsStarted;
    private final Deque<MediaEvent> mEventQueue;
    private int mMediaBufferSize;
    private Filter mMediaFilter;
    private boolean mWaitForEcm;
    private CasSession mCasSession;

    private Executor getExecutor() {
        return Runnable::run;
    }

    private FilterCallback getMediaFilterCallback() {
        return new FilterCallback() {
            @Override
            public void onFilterEvent(Filter filter, FilterEvent[] events) {
                for (FilterEvent e : events) {
                    if (e instanceof MediaEvent) {
                        int sampleSize = (int) ((MediaEvent) e).getDataLength();
                        int offset = (int) ((MediaEvent) e).getOffset();
                        if ((sampleSize == 0) ||
                                ((sampleSize + offset) > mMediaBufferSize)) {
                            Log.e(TAG, "invalid MediaEvent sample size " + sampleSize + " at offset " + offset);
                        } else {
                            synchronized(mEventQueue) {
                                mEventQueue.addLast((MediaEvent) e);
                            }
                        }
                    }
                }
            }
            @Override
            public void onFilterStatusChanged(Filter filter, int status) {}
        };
    }

    public Track(Tuner tuner, MediaFormat format, int pid, int caSystemId, int ecmPid) {
        mTuner = tuner;
        mFormat = format;
        mPid = pid;
        mCaSystemId = caSystemId;
        mEcmPid = ecmPid;
        mIsStarted = false;
        mEventQueue = new LinkedList<MediaEvent>();
    }

    public Track(Tuner tuner, MediaFormat format, int pid, int caSystemId, int ecmPid, byte[] privateData) {
        mTuner = tuner;
        mFormat = format;
        mPid = pid;
        mCaSystemId = caSystemId;
        mEcmPid = ecmPid;
        mIsStarted = false;
        mEventQueue = new LinkedList<MediaEvent>();
        mPrivateData = privateData;
    }

    public int getFilterId() {
        int id = -1;
        if (mMediaFilter == null) {
            Log.e(TAG, "Filter getFilterId mPid[" + mPid + "] is not configured");
            return 0;
        }
        try {
            id = mMediaFilter.getId();
        }catch (Exception e){
            mMediaFilter = null;
        }
        return id;
    }

    public int getPid(){
        return mPid;
    }
    public int getAvSyncHwId() {
        int id = -1;
        if (mMediaFilter == null) {
            Log.e(TAG, "Filter getAvSyncHwId mPid[" + mPid + "] is not configured");
            return 0;
        }
        try {
            LogUtils.d("mMediaFilter id ="+mMediaFilter.getId());
            id = mTuner.getAvSyncHwId(mMediaFilter);
        }catch (Exception e){
            mMediaFilter = null;
        }
        return id;
    }

    public boolean isVideo() {
        String mime = mFormat.getString(MediaFormat.KEY_MIME);
        return (mime.startsWith("video/"));
    }

    public boolean isAudio() {
        String mime = mFormat.getString(MediaFormat.KEY_MIME);
        return (mime.startsWith("audio/"));
    }

    public MediaFormat getFormat() {
        return mFormat;
    }

    public int getCaSystemId() {
        return mCaSystemId;
    }

    public int getEcmPid() {
        return mEcmPid;
    }

    public byte[] getPrivateData() {
        return mPrivateData;
    }

//    public int getEventCount() {
//        int count = 0;
//        synchronized(mEventQueue) {
//            if (mWaitForEcm) {
//                int ecmCount = mCasSession.getEcmCount();
//                if (ecmCount != 0) {
//                    mMediaFilter.start();
//                    mWaitForEcm = false;
//                }
//            }
//            count = mEventQueue.size();
//        }
//        return count;
//    }
//
//    public MediaEvent popEvent() {
//        MediaEvent e;
//        if (!mIsStarted) return null;
//        synchronized(mEventQueue) {
//            e = mEventQueue.poll();
//        }
//        return e;
//    }

    public void openFilter(boolean passthrough)
    {
        if (mMediaFilter == null)
        {
            if (isAudio()) {
                mMediaBufferSize = 2 * 1024 * 1024;
            } else { // video
                mMediaBufferSize = 4 * 1024 * 1024;
            }
            LogUtils.d("mTuner = "+mTuner);
            mMediaFilter = mTuner.openFilter(Filter.TYPE_TS,
                    isAudio() ? Filter.SUBTYPE_AUDIO : Filter.SUBTYPE_VIDEO,
                    mMediaBufferSize, /*getExecutor()*/null, /*getMediaFilterCallback()*/null);
            if(mMediaFilter != null) {
                String tmp = isVideo() ? "Video" : "Audio";
                LogUtils.d(tmp + " openFilter filterid = " + mMediaFilter.getId());
            }
        }
    }

    public void configure(boolean passthrough) {
        try{
            String tmp = isVideo()?"Video":"Audio";
            LogUtils.d(tmp+" mPid = "+mPid+" filterid = "+mMediaFilter.getId());
            if (mMediaFilter != null) {
                AvSettings settings = AvSettings
                        .builder(Filter.TYPE_TS, isAudio())
                        .setPassthrough(passthrough)
                        .build();
                FilterConfiguration config = TsFilterConfiguration
                        .builder()
                        .setTpid(mPid)
                        .setSettings(settings)
                        .build();
                mMediaFilter.configure(config);
            }
        }catch (Exception e){
            mMediaFilter = null;
        }
    }

    public void start(CasSession casSession) {
        try{
            if(mMediaFilter == null){
                LogUtils.e("mMediaFilter open fial....Can't start");
                return;
            }
            if (!mIsStarted) {
                String tmp = isVideo()?"Video":"Audio";
                mCasSession = casSession;

                LogUtils.d("start "+tmp+": media filter start,filterid = " + mMediaFilter.getId());
                mMediaFilter.start();
                mIsStarted = true;
            }
        }catch (Exception e){
            mMediaFilter = null;
        }
    }

    public void stop() {
        try{
            if (mIsStarted) {
                String tmp = isVideo()?"Video":"Audio";
                LogUtils.d(tmp+" Stop filter id"+getFilterId());
                mMediaFilter.stop();
                mIsStarted = false;
            }
        }catch (Exception e){
            mMediaFilter = null;
        }
    }

    public void close() {
        try {
            if (mMediaFilter != null) {
                stop();
                String tmp = isVideo()?"Video":"Audio";
                LogUtils.d(tmp+" close filter id"+getFilterId());
                mMediaFilter.close();
                mMediaFilter = null;
            }
        }catch (Exception e){
            mMediaFilter = null;
        }
    }
}
