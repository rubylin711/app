package com.prime.dtv.service.Player;

import android.media.MediaCas;
import android.media.MediaFormat;
import android.media.tv.tuner.dvr.OnPlaybackStatusChangedListener;
import android.media.tv.tuner.filter.Filter;
import android.media.tv.tuner.filter.FilterCallback;
import android.media.tv.tuner.filter.FilterConfiguration;
import android.media.tv.tuner.filter.FilterEvent;
import android.media.tv.tuner.filter.TsFilterConfiguration;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.CasRefreshHelper;
import com.prime.datastructure.config.Pvcfg;
import com.prime.dtv.service.Tuner.TunerInterface;
import com.prime.datastructure.sysdata.CasData;
import com.prime.datastructure.sysdata.ProgramInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public class MediaExtractor {
    private static final String TAG = "MediaExtractor";
    private static final int mVideoWidth = 1920;
    private static final int mVideoHeight = 1080;
    private static final int mAudioSampleRate = 48000;

    private TunerInterface mTunerInterface;
    private ArrayList<Track> mTrackList;
    private Map<Integer, CasSession> mCasSessionMap;
    private Filter mPcrFilter;
//    private DvrPlayback mDvrPlayback;
    private int mDvrBufferSize = 188 * 16384;
    private boolean mPlaybackThreadStop;
    private Thread mPlaybackThread;
    private ParcelFileDescriptor mParcelFd;
    private CasSession.Callback mCallback;

    private Executor getExecutor() {
        return Runnable::run;
    }

    private OnPlaybackStatusChangedListener getPlaybackListener() {
        return new OnPlaybackStatusChangedListener() {
            @Override
            public void onPlaybackStatusChanged(int status) {
                //Log.d(TAG, "getPlaybackListener status = " + status);
            }
        };
    }

//    Runnable PlaybackRunnable = new Runnable() {
//        @Override
//        public void run() {
//            PlaybackThread();
//        }
//    };

//    private void PlaybackThread() {
//        long total = 0;
//        long readSize = mDvrBufferSize / 4;
//        int msDelay = 50;
//
//        while (!mPlaybackThreadStop) {
//            //Log.d(TAG, "TOTAL WRITTEN = " + total);
//            try {
//                long count = mDvrPlayback.read(readSize);
//                total += count;
//            } catch (Exception ex) {
//            }
//            try {
//                Thread.sleep(msDelay);
//            } catch (Exception ex) {
//            }
//        }
//    }

    public MediaExtractor() {
        mTrackList = new ArrayList<Track>();
        mCasSessionMap = new HashMap<>();
    }

    private FilterCallback getPcrFilterCallback() {
        return new FilterCallback() {
            @Override
            public void onFilterEvent(Filter filter, FilterEvent[] events) {}
            @Override
            public void onFilterStatusChanged(Filter filter, int status) {}
        };
    }

    public void setDataSource(int tunerId,TunerInterface tunerInterface,ProgramInfo programInfo) {
        int tuner_id = tunerId;//programInfo.getTunerId();
        mTunerInterface = tunerInterface;


        if(mTunerInterface == null){
            Log.d(TAG, "setDataSource: mTunerInterface not Init !!!!!! ");
            return;
        }
        if(mTunerInterface.getTuner(tuner_id) == null){
            Log.d(TAG, "[Ethan] setDataSource: can't get tuner !!!!!! ");
            return;
        }
        Log.d(TAG, "[Ethan]setDataSource: IN !!!!!! ");
        //mTunerInterface.Tune(tunerId, tpInfo); // tune outside
        Log.d(TAG, "mTunerInterface.getTuner() = "+mTunerInterface.getTuner(tuner_id));
        if(Pvcfg.getSyncMode() == Pvcfg.AVSYNC_MODE.PCR) {
            Log.d(TAG, "getExecutor() = " + getExecutor());
            Log.d(TAG, "getPcrFilterCallback() = " + getPcrFilterCallback());
            int pcrPid = programInfo.getPcr();
            Log.d(TAG, "pcrPid = " + pcrPid);
            if (pcrPid > 0) { // if pcr pid valid, should not be 0
                Log.d(TAG, "Open pcr filter  ");
                // comment out because mPcrFilter is not used and seems slowing down fcc
                mPcrFilter = mTunerInterface.getTuner(tuner_id).openFilter(Filter.TYPE_TS, Filter.SUBTYPE_PCR,
                        1000, getExecutor(), getPcrFilterCallback());
                if (mPcrFilter != null) {
                    FilterConfiguration config = TsFilterConfiguration
                            .builder()
                            .setTpid(pcrPid)
                            .build();
                    mPcrFilter.configure(config);
                    mPcrFilter.start();
                }
            }
        }

        // TODO parse PAT/PMT to create Tracks
        MediaFormat videoFormat = new MediaFormat();
        videoFormat.setString(MediaFormat.KEY_MIME, programInfo.pVideo.getMime());
        videoFormat.setInteger(MediaFormat.KEY_WIDTH, mVideoWidth);
        videoFormat.setInteger(MediaFormat.KEY_HEIGHT, mVideoHeight);
//        int videoCaSystemId = programInfo.pVideo.getCaSystemId(0); // tmp use first id
        // use first supported id
        int videoCaSystemId = findSupportedCaSystemId(programInfo.pVideo.CaInfoList);
//        Log.d(TAG, "setDataSource: videoCaSystemId = " + videoCaSystemId);
//        Log.d(TAG, "setDataSource: ecm pid = " + programInfo.pVideo.getEcmPid(videoCaSystemId));
        if(programInfo.pVideo.getPID() != 0) {
        mTrackList.add(
                new Track(
                        mTunerInterface.getTuner(tuner_id),
                        videoFormat,
                        programInfo.pVideo.getPID(),
                        videoCaSystemId,
                        programInfo.pVideo.getEcmPid(videoCaSystemId),
                        programInfo.pVideo.getPrivateData(videoCaSystemId))
        );
        }
        for(int i=0 ; i<programInfo.pAudios.size(); i++) {
            MediaFormat audioFormat = new MediaFormat();
            String mime = programInfo.pAudios.get(/*programInfo.getAudioSelected()*/i).getMime();
            audioFormat.setString(MediaFormat.KEY_MIME, mime);
            audioFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, mAudioSampleRate);
            if (mime.equals("audio/mp4a-latm")) {
                audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
                audioFormat.setInteger(MediaFormat.KEY_IS_ADTS, 1);
            } else {
                audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 2);
            }

//            int audioCaSystemId = programInfo.pAudios.get(/*programInfo.getAudioSelected()*/i).getCaSystemId(0); // tmp use first id
            // use first supported id
            int audioCaSystemId = findSupportedCaSystemId(programInfo.pAudios.get(i).CaInfoList);
//        Log.d(TAG, "setDataSource: audioCaSystemId = " + audioCaSystemId);
//        Log.d(TAG, "setDataSource: ecm pid = " + programInfo.pAudios.get(programInfo.getAudioSelected()).getEcmPid(audioCaSystemId));
            mTrackList.add(new Track(mTunerInterface.getTuner(tuner_id), audioFormat, programInfo.pAudios.get(/*programInfo.getAudioSelected()*/i).getPid(),
                    audioCaSystemId, programInfo.pAudios.get(/*programInfo.getAudioSelected()*/i).getEcmPid(audioCaSystemId),
                    programInfo.pAudios.get(/*programInfo.getAudioSelected()*/i).getPrivateData(audioCaSystemId)));
        }
        // Open MediaCas instance if necessary
        if(Pvcfg.isProcessECM()) {
            for (Track t : mTrackList) {
                int caSystemId = t.getCaSystemId();
                Log.d(TAG, "setDataSource: caSystemId = " + caSystemId);
                if (caSystemId != 0) {
                    MediaCas.PluginDescriptor[] plugins = null;
                    plugins = MediaCas.enumeratePlugins();
                    for (MediaCas.PluginDescriptor p : plugins) {
                        Log.d(TAG, "MediaCas plugin available : " + p);
                    }
                    if (!MediaCas.isSystemIdSupported(caSystemId)) {
                        Log.e(TAG, "MediaCas CaSystemId not supported, " + Integer.toHexString(caSystemId));
                        return;
                    }

                    // if try_license_if_entitled_channel == 1
                    // open CAS session only when the channel is one of the entitled channels.

                    if (Pvcfg.isTryLicenseEntitledOnly()) {
                        CasRefreshHelper casRefreshHelper = CasRefreshHelper.get_instance();
                        CasData casData = casRefreshHelper.get_cas_data();
                        String contentId = CasRefreshHelper.parse_content_id(t.getPrivateData());
                        if (!casData.getEntitledChannelIds().contains(contentId)) {
                            Log.w(TAG, "MediaCas skip unentitled channel, content id = " + contentId);
                            mCallback.onHttpError(401,contentId);
                            return;
                        }
                    }


                    try {
                        int ecmPid = t.getEcmPid();
                        Log.d(TAG, "setDataSource: ecmpid = " + ecmPid);
                        if (ecmPid != 0) {
                            CasSession casSession = mCasSessionMap.get(ecmPid);

                            if (casSession == null) {
                                // CAS session doesn't already exist, create a new one
                                //if(programInfo != null)
                                    Log.d(TAG, "setDataSource: new cas session, CH "+programInfo.getDisplayNum()+" "+programInfo.getDisplayName());
                                Log.d(TAG, "setDataSource: new cas session, tuner id = " + tuner_id);
                                casSession = new CasSession(mTunerInterface.getTuner(tuner_id), caSystemId, ecmPid);
                                mCasSessionMap.put(ecmPid, casSession);
                                LogUtils.d("mCasSessionMap ="+mCasSessionMap);
                                casSession.setCallback(mCallback);

                                Log.d(TAG, "setDataSource: set privatedata = " + Arrays.toString(t.getPrivateData()));
                                casSession.setPrivateData(t.getPrivateData());
                                for (Track track : mTrackList) {
                                    casSession.addPid(track.getPid());
                                }

                                casSession.open();
                                //casSession.run();
                            }
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "MediaCas exception : " + ex);
                        ex.printStackTrace();
                        return;
                    }
                    break; // assume same CA system ID for all tracks
                }
            }
        }
    }

    public void start() {
//        if (mDvrPlayback != null) {
//            mPlaybackThreadStop = false;
//            mDvrPlayback.start();
//            mPlaybackThread = new Thread(PlaybackRunnable);
//            mPlaybackThread.start();
//        }
    }

    public void stop() {
        LogUtils.d(" Start ");
//        if (mDvrPlayback != null) {
//            mDvrPlayback.stop();
//            if (!mPlaybackThreadStop) {
//                try {
//                    mPlaybackThreadStop = true;
//                    mPlaybackThread.join();
//                    mPlaybackThread = null;
//                } catch(Exception ex){}
//            }
//        }
        LogUtils.d(" Stop Filter ");
        for (Track t : mTrackList) {
            t.stop();
        }
        LogUtils.d(" stop PCR Filter ");
        if (mPcrFilter != null) {
            mPcrFilter.stop();
        }
        LogUtils.d(" End ");
    }

    public void close() {
        stop();
        LogUtils.d(" Start ");
        LogUtils.d(" Close AV Filter ");
        for (Track t : mTrackList) {
            t.close();
        }
        mTrackList.clear();
        LogUtils.d(" Close  casSession mCasSessionMap ="+mCasSessionMap);
        for (Integer i : mCasSessionMap.keySet()) {
            CasSession casSession = mCasSessionMap.get(i);
            if (casSession != null) {
                casSession.close();
            }
        }
        mCasSessionMap.clear();

//        LogUtils.d(" Close mDvrPlayback");
//        if (mDvrPlayback != null) {
//            mDvrPlayback.close();
//            mDvrPlayback = null;
//        }
        LogUtils.d(" Close mPcrFilter");
        if (mPcrFilter != null) {
            mPcrFilter.close();
            mPcrFilter = null;
        }
        LogUtils.d(" End ");
    }

    public int getTrackCount() {
        return mTrackList.size();
    }

    public int trackGetFilterId(int index) {
        return mTrackList.get(index).getFilterId();
    }

    public int trackGetAvSyncHwId(int index) {
        return mTrackList.get(index).getAvSyncHwId();
    }

    public int trackPid(int index){
        return mTrackList.get(index).getPid();
    }
    public boolean trackIsAudio(int index) {
        return mTrackList.get(index).isAudio();
    }

    public boolean trackIsVideo(int index) {
        return mTrackList.get(index).isVideo();
    }

    public MediaFormat trackGetFormat(int index) {
        return mTrackList.get(index).getFormat();
    }

    public void trackConfigure(int index, boolean passthrough) {
        mTrackList.get(index).configure(passthrough);
    }

    public void trackOpenFilter(int index, boolean passthrough)
    {
        LogUtils.d(" index "+index);
        mTrackList.get(index).openFilter(passthrough);
    }
    public void trackStart(int index, int tuner_id) {
        Log.d(TAG, "trackStart: trackStart index = " + index);
        if (index >= 0 && mTrackList.size() > index) {
            int ecmPid = mTrackList.get(index).getEcmPid();
            mTrackList.get(index).start(mCasSessionMap.get(ecmPid));
        }
    }

    public void trackStop(int index) {
        mTrackList.get(index).stop();
    }

    public void trackClose(int index) {
        mTrackList.get(index).close();
    }

//    public int trackGetEventCount(int index) {
//        return mTrackList.get(index).getEventCount();
//    }
//
//    public MediaEvent trackPopEvent(int index) {
//        return mTrackList.get(index).popEvent();
//    }

    private int findSupportedCaSystemId(List<ProgramInfo.CaInfo> caInfoList) {
        int supportedCaSystemId = 0;
        for (ProgramInfo.CaInfo caInfo : caInfoList) {
            int caSystemId = caInfo.getCaSystemId();
            if (MediaCas.isSystemIdSupported(caSystemId)) {
                // find first supported
                supportedCaSystemId = caSystemId;
                break;
            }
        }

        return supportedCaSystemId;
    }

    public void setCasSessionCallback(CasSession.Callback callback){
        mCallback = callback;
    }

    public int trackIndex(int pid) {
        for(int i = 0; i < mTrackList.size(); i++) {
            if(mTrackList.get(i).getPid() == pid)
                return i;
        }
        return -1;
    }
}
