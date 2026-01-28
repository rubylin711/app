package com.prime.dtv.service.Player;


import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.tv.TvInputManager;
import android.media.tv.tuner.Tuner;
import android.media.tv.tuner.filter.MediaEvent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import com.prime.android.audiotrack.PrimeAudioTrack;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.utils.LogUtils;
//import com.prime.dtv.ChannelChangeManager;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.ServiceApplication;
import com.prime.datastructure.config.Pvcfg;
import com.prime.dtv.ServiceInterface;
import com.prime.dtv.service.Tuner.TunerBase;
import com.prime.dtv.service.Tuner.TunerInterface;
import com.prime.dtv.service.Util.MediaUtils;
import com.prime.dtv.service.subtitle.SubtitleManager;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.dtv.service.Table.StreamType;
import com.prime.datastructure.sysdata.SubtitleInfo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class Player {
    private static final String TAG = "Player";
    private static final int DEFAULT_TUNER_ID = 0;
    private AudioManager mAudioManager = null;
    private Context mContext = null;
    private int mAudioSessionId;
    private boolean mPassthrough;
    private Tuner mTuner;
    private int mTunerId;
    private MediaExtractor mMediaExtractor;
    private Surface mSurface = null;
    private SurfaceHolder mSurfaceHolder = null;
    private ProgramInfo mProgramInfo = null;
    private int mVideoTrackIndex;
    private Thread mVideoThread;
    private boolean mVideoThreadStop;
    private MediaFormat mVideoFormat;
    private MediaCodec mVideoCodec = null;
    private boolean isVideoCodeStart = false;
    private Handler mVideoDecoderHandler;
    private HandlerThread mVideoDecoderHandlerThread = new HandlerThread("VideoDecoder");

    protected List<AudioTrack> mAudioTrackList = new ArrayList<>();
    private int mAudioTrackIndex;
    private Thread mAudioThread;
    private boolean mAudioThreadStop;
    private Thread mAudioOutputThread;
    private boolean mAudioOutputThreadStop;
    private LinkedBlockingQueue<Integer> mAudioOutputQueue;
    private LinkedBlockingQueue<MediaCodec.BufferInfo> mAudioOutputInfoQueue;
    private long mVideoCount = 0;
    private long mPreVideoCount = 0;
    private int mVideoCountSame = 0;
    private SubtitleManager mSubtitleManager = null;
    private Callback mCallback = null;
    private static final long MICROS_PER_SECOND = 1000000L;
    private boolean mIsVideoRender = false;
    private boolean isSetVisibleCompleted = false;
    private long mVideoDecoderStartMs = -1;
    private boolean mFirstInputLogged = false;
    private boolean mFirstFrameLogged = false;
    private boolean mPreplayFiltersPaused = false;
    private int mCodecToken = 0;
    private int mCurrentCodecToken = 0;
    private volatile long mLastCodecReleaseMs = -1;
    private volatile int mLastCodecReleaseToken = -1;

    private boolean mCasSessionError = false;
    private long mChannelId;
    private boolean mIsPrePlay = false;
    private WeakReference<AvCmdMiddle> g_avCmdMiddle_ref;
    private static final Object mSurfaceLock = new Object();

    private static int surfaceId(Surface surface) {
        return surface == null ? 0 : System.identityHashCode(surface);
    }
    private final Semaphore mMedicodcSemaphore = new Semaphore(1);
	private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private void logCodecToken(String reason) {
        LogUtils.d("FCC_LOG codec token reason=" + reason
                + " token=" + mCurrentCodecToken
                + " tunerId=" + mTunerId
                + " channelId=" + mChannelId
                + " preplay=" + mIsPrePlay
                + " surface=" + mSurface);
    }
    // Player.java 內新增：欄位
    private TunerBase mBoundTunerBase;
    private final TunerBase.FrontendLockListener mFrontendLockListener =
        (locked, strength, quality, snr, ber) -> {
            LogUtils.d("[FE_LOCK] tunerId=" + mTunerId +
                    " locked=" + locked + " str=" + strength +
                    " ql=" + quality + " snr=" + snr + " ber=" + ber);

            // 這裡依你的需求做事：
            // 1) 若解鎖→可通知外層 UI 顯示「訊號弱 / 調台中」
            if (!locked && mCallback != null) {
                // 例如：告知 UI（你也可另外在 Callback 多加一個 onFrontendLockChanged）
                // mCallback.onDecoderError(); // 或自訂一個 onFrontendLockChanged(...)
                mCallback.onVideoUnavailable(mTunerId, TvInputManager.VIDEO_UNAVAILABLE_REASON_WEAK_SIGNAL);
            }

            // 2) 若剛鎖定且尚未回報可視，可在這裡觸發可視（若你不想等第一幀）
            // if (locked && !IsVideoRender()) { setVideoRender(true); }
        };

        // Player.java 內新增：公開方法
        public void bindFrontendLock(@NonNull TunerBase tunerBase) {
            // 先解掉舊的以避免交叉綁定
            if (mBoundTunerBase != null) {
                try { mBoundTunerBase.setFrontendLockListener(null); } catch (Exception ignored) {}
            }
            mBoundTunerBase = tunerBase;
            mBoundTunerBase.setFrontendLockListener(mFrontendLockListener);
        }

        public void unbindFrontendLock() {
            if (mBoundTunerBase != null) {
                try { mBoundTunerBase.setFrontendLockListener(null); } catch (Exception ignored) {}
                mBoundTunerBase = null;
            }
        }

    private CasSession.Callback getCasSessionCallback() {
        return new CasSession.Callback() {
            @Override
            public void onHttpError(int responseCode, String contentId) {
                LogUtils.d("[CheckErrorMsg] mChannelId = "+mChannelId+" CasSession-Error callback: onHttpError responseCode = "+responseCode);
                mCasSessionError = true;
                if (mCallback != null) {
                    mCallback.onHttpError(responseCode, mChannelId ,contentId);
                }
            }

            @Override
            public void onCasError(String msg) {
                LogUtils.d("[CheckErrorMsg] msg = "+msg);
                mCasSessionError = true;
                if (mCallback != null) {
                    mCallback.onCasError(msg, mProgramInfo.getChannelId());
                }
            }

            @Override
            public void onRemoveLicense(String licenseId) {
                LogUtils.d("onRemoveLicense mChannelId ="+mProgramInfo.getChannelId()+" licenseId = "+licenseId);
                if (mCallback != null && licenseId != null) {
                    mCallback.onRemoveLicense(mProgramInfo.getChannelId(),licenseId);
                }
            }

            @Override
            public void onConnectTimeout(String msg) {
                LogUtils.d("CasSession-Error callback: onConnectTimeout");
                mCasSessionError = true;
                if (mCallback != null) {
                    mCallback.onConnectTimeout(msg, mProgramInfo.getChannelId());
                }
            }
        };
    }

    public Player(Context context, AvCmdMiddle avCmdMiddle, Surface surface, int tunerId, TunerInterface tunerInterface,ProgramInfo programInfo, boolean passthrough, Callback callback) {
        g_avCmdMiddle_ref = new WeakReference<>(avCmdMiddle);
        mContext = context;
        mSurface = surface;
        mProgramInfo = programInfo;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mAudioSessionId = mAudioManager.generateAudioSessionId();
        mPassthrough = passthrough;
        mAudioTrackIndex = -1;
        mVideoTrackIndex = -1;
        mTunerId = tunerId;
        mTuner = tunerInterface.getTuner(tunerId);
        mCallback = callback;
        MediaExtractorCreate(tunerId,tunerInterface,mProgramInfo);
        mSubtitleManager = SubtitleManager.getSubtitleManager(context);
        mChannelId = mProgramInfo.getChannelId();
//        Log.d(TAG,"player["+mTunerId+"] create surface = "+surface);
    }

    public Player(Context context, AvCmdMiddle avCmdMiddle, SurfaceHolder surfaceHolder, int tunerId, TunerInterface tunerInterface, ProgramInfo programInfo, boolean passthrough, Callback callback) {
        g_avCmdMiddle_ref = new WeakReference<>(avCmdMiddle);
        mContext = context;
        mSurfaceHolder = surfaceHolder;
        mProgramInfo = programInfo;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mAudioSessionId = mAudioManager.generateAudioSessionId();
        mPassthrough = passthrough;
        mAudioTrackIndex = -1;
        mVideoTrackIndex = -1;
        mTunerId = tunerId;
        mTuner = tunerInterface.getTuner(tunerId);
        mSubtitleManager = SubtitleManager.getSubtitleManager(context);

        mSurface = mSurfaceHolder.getSurface();

        LogUtils.d("AvCmdMiddle mSurface = "+ mSurface);
        LogUtils.d("AvCmdMiddle surfaceHolder["+tunerId+"] = "+ surfaceHolder);
        mCallback = callback;
        MediaExtractorCreate(tunerId,tunerInterface,mProgramInfo);
        mChannelId = mProgramInfo.getChannelId();
    }

//    public void SetCallback(PesiDtvFrameworkInterfaceCallback callback){
//        mCallback = callback;
//    }
    public Object getSurfaceLock(){
        return mSurfaceLock;
    }
    public AvCmdMiddle get() {
        return (AvCmdMiddle) g_avCmdMiddle_ref.get();
    }

    public void stop_now(){
        LogUtils.d("FCC_LOG stop_now start tunerId=" + mTunerId +
                " channelId=" + mChannelId +
                " " + AvCmdMiddle.surfaceIndexDebug(mSurface));
        try {
            mPreplayFiltersPaused = false;
            mMedicodcSemaphore.acquire();
            mainHandler.removeCallbacks(firstFrameTimeout);
//            if(mSurface != null && mSurfaceHolder != null) {
//                mSurfaceHolder.removeCallback(null);
//            }
            if(Pvcfg.isFccV3Enable()){
                audio_track_skip_focus(0);
            }
            if (mMediaExtractor != null) {
                mMediaExtractor.stop();
//                mMediaExtractor.close();
//                mMediaExtractor = null;
            }
//            mAudioThreadStop = true;
//            mVideoThreadStop = true;
//            if (mAudioThread != null) {
//                mAudioThread.join();
//                mAudioThread = null;
//            }
//            if (mVideoThread != null) {
//                mVideoThreadStop = true;
//                mVideoThread.join();
//                mVideoThread = null;
//            }
            if( mVideoCodec != null ) {
                logCodecToken("stop_now");
                if(isVideoCodeStart) {
                    mVideoCodec.stop();
                }
                mVideoCodec.setCallback(null);
                mVideoCodec.release();
                mVideoCodec = null;
                isVideoCodeStart = false;
                long releaseEndMs = SystemClock.elapsedRealtime();
                mLastCodecReleaseMs = releaseEndMs;
                mLastCodecReleaseToken = mCurrentCodecToken;
                LogUtils.d("FCC_LOG stop_now release tunerId=" + mTunerId +
                        " token=" + mLastCodecReleaseToken);
            }
            for(AudioTrack a: mAudioTrackList){
                a.stop();
                a.release();
            }
            mAudioTrackList.clear();
        } catch(Exception ex) {
            Log.e(TAG, "surfaceDestroyed() exception", ex);
//            ex.printStackTrace();
        } finally {
            mMedicodcSemaphore.release();
        }
        LogUtils.d("FCC_LOG stop_now end tunerId=" + mTunerId);
    }
    private Thread stop_player_thread() {
        Runnable runnable = () -> {
            stop_now();
        };
        Thread thread = new Thread(runnable,"stop_player_thread");
        return thread;
    }
    public void stop() {
        Thread thread = stop_player_thread();
        thread.start();
    }

    public void close(){
//        Log.d(TAG,"player["+mTunerId+"]close");
        try {
            mainHandler.removeCallbacks(firstFrameTimeout);
            unbindFrontendLock();
            if (mMediaExtractor != null) {
                mMediaExtractor.close();
                mMediaExtractor = null;
            }
        } catch(Exception ex) {
            //Log.e(TAG, "surfaceDestroyed() exception ex: " + ex);
            ex.printStackTrace();
        }
    }

    public void forcue_audio(){
        audio_track_skip_focus(0);
        LogUtils.d("[FCC3] mAudioTrackIndex = "+mAudioTrackIndex);
        switchAudioTrackByFilterID(mMediaExtractor.trackGetFilterId(mAudioTrackIndex));
    }

    public void SwitchAudio(int AudioPid){
        int tuner_id = mProgramInfo.getTunerId();
        int audio_codec = -1;
        boolean found_pid = false;
        for(int i=0 ; i<mProgramInfo.pAudios.size(); i++){
            if(mProgramInfo.pAudios.get(i).getPid() == AudioPid) {
                LogUtils.d("SwitchAudio Found Audio");
                found_pid = true;
                mProgramInfo.setAudioSelected(i);
                audio_codec = mProgramInfo.pAudios.get(i).getCodec();

            }
        }
        LogUtils.d("SwitchAudio AudioPid = "+AudioPid+" audio_codec = "+audio_codec);
        if(found_pid) {
            if(Pvcfg.getMultiAudio_Enable()) {
                mAudioTrackIndex=-1;
                getAVTrackIndex(mProgramInfo);
                switchAudioTrackByFilterID(mMediaExtractor.trackGetFilterId(mAudioTrackIndex));
            }
            else{
                stopAudioTrack();
                getAVTrackIndex(mProgramInfo);
                openAndConfigAudioFilter(mProgramInfo);
                startAudioTrack(audio_codec, tuner_id, mAudioTrackIndex);
                start_audio();

                if(Pvcfg.isFccV3Enable())
                    switchAudioTrackByFilterID(mMediaExtractor.trackGetFilterId(mAudioTrackIndex));
            }
        }
    }
    public void startVideoCodec() {
        //synchronized(mSurfaceLock) {
            //while(mVideoCodec)
            //mVideoCodec.start();
            //Log.d( TAG, "VideoDecoderThread: mMediaExtractor video filter trackStart" ) ;
            //mMediaExtractor.trackStart(mVideoTrackIndex, 0/*tunerid*/);
        try {
            mMedicodcSemaphore.acquire();
            long startMs = SystemClock.elapsedRealtime();
            mVideoFormat = mMediaExtractor.trackGetFormat(mVideoTrackIndex);
            int avSyncHwId = mMediaExtractor.trackGetAvSyncHwId(mVideoTrackIndex);
            int filterId = mMediaExtractor.trackGetFilterId(mVideoTrackIndex);

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
                mVideoFormat.setInteger("audio-hw-sync", avSyncHwId);
                mVideoFormat.setInteger("vendor.tis.videofilterId", filterId);
                mVideoFormat.setInteger("vendor.tis.videofilterId.value", filterId); //For Codec2.0
            } else {
                mVideoFormat.setInteger(MediaFormat.KEY_HARDWARE_AV_SYNC_ID, avSyncHwId);
            }

            if(mVideoCodec == null){
                createVideoCodec();
            }
            if (mVideoCodec != null) {
//           if(!mVideoDecoderHandlerThread.isAlive())
//                mVideoDecoderHandlerThread.start();
//            mVideoDecoderHandler = new Handler(mVideoDecoderHandlerThread.getLooper());
//            mVideoThread = new Thread(VideoDecoderRunnable);
//            mVideoThread.start();

                // start directly to fix crash if change channel rapidly
                VideoDecoderThread();
            }
            LogUtils.d("FCC_LOG startVideoCodec dt=" + (SystemClock.elapsedRealtime() - startMs) + "ms" +
                    " tunerId=" + mTunerId +
                    " token=" + mCurrentCodecToken);
        } catch (InterruptedException e) {
            Log.e(TAG, "startVideoCodec: ", e);
//            throw new RuntimeException(e);
        }finally {
            mMedicodcSemaphore.release();
        }

    }

    public void stopAudioTrack(){
        try {
//            mAudioThreadStop = true;
//            if (mAudioThread != null) {
//                mAudioThread.join();
//                mAudioThread = null;
//            }
            for(AudioTrack a: mAudioTrackList){
                a.stop();
                a.release();
            }
            mAudioTrackList.clear();
            mMediaExtractor.trackStop(mAudioTrackIndex);
            mMediaExtractor.trackClose(mAudioTrackIndex);
            mAudioTrackIndex = -1;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void start_audio() {

        int tunerid = mProgramInfo.getTunerId();
        //Start track
        for (AudioTrack a : mAudioTrackList) {
            a.play();
        }
        //Start Filter
        if (Pvcfg.getMultiAudio_Enable()){
            for (int i = 0; i < mMediaExtractor.getTrackCount(); i++) {
                if (mMediaExtractor.trackIsAudio(i)) {
                    mMediaExtractor.trackStart(i, tunerid);
                }
            }
        }
        else {
            if(mAudioTrackIndex != -1)
                mMediaExtractor.trackStart(mAudioTrackIndex, tunerid);
        }
    }
    public void CreateAudioTrack(){
        int tuner_id = mProgramInfo.getTunerId();
        if(Pvcfg.getMultiAudio_Enable()) {
            for (int i = 0; i < mProgramInfo.pAudios.size(); i++) {
                int audioCodec = mProgramInfo.pAudios.get(i).getCodec();
                startAudioTrack(audioCodec, tuner_id, i + 1);
            }
        }
        else{
            int index = mProgramInfo.getAudioSelected();
            if (mProgramInfo.pAudios.size() > index) {
                int audioCodec = mProgramInfo.pAudios.get(index).getCodec();
                LogUtils.d(" startAudioTrack before mProgramInfo.pAudios.get("+index+") pid = " + mProgramInfo.pAudios.get(index).getPid()
                    + " audioCodec = "+audioCodec+" getLeftIsoLang = "+mProgramInfo.pAudios.get(index).getLeftIsoLang()+"");
                startAudioTrack(audioCodec, tuner_id, mAudioTrackIndex);
            }
        }
    }
    public void startAudioTrack(int audioCodec, int tunerid, int index) {
        try {
            LogUtils.d(" startAudioTrack  start audioCodec = " + audioCodec+" mAudioTrackIndex = "+mAudioTrackIndex
                + " mMediaExtractor.trackPid = "+mMediaExtractor.trackPid(index));

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

            Log.d(TAG," startAudioTrack  start mEncoding = " + mEncoding);

            MediaFormat mAudioFormat = mMediaExtractor.trackGetFormat(index);
            int sampleRate = mAudioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            int channelCount = mAudioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            int channelConfig = (channelCount == 1 ? AudioFormat.CHANNEL_OUT_MONO
                    : AudioFormat.CHANNEL_OUT_STEREO);
            AudioFormat audioFormat = new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(channelConfig)
                    .setSampleRate(sampleRate)
                    .setEncoding(mEncoding)
                    //.setEncoding(AudioFormat.ENCODING_AC3)
                    // .setEncoding(AudioFormat.ENCODING_AAC_LC)
                    .build();
            int avSyncHwId;
            if(mVideoTrackIndex != -1){
                avSyncHwId = mMediaExtractor.trackGetAvSyncHwId(mVideoTrackIndex);
            }
            else{
                avSyncHwId = mMediaExtractor.trackGetAvSyncHwId(mAudioTrackIndex);
            }
            LogUtils.d("avSyncHwId = "+avSyncHwId+" mVideoTrackIndex = "+mVideoTrackIndex);
            PrimeAudioTrack.TunerConfiguration tunerConfig
                    = new PrimeAudioTrack.TunerConfiguration(mMediaExtractor.trackGetFilterId(index), avSyncHwId) ;

            AudioTrack audioTrack = new PrimeAudioTrack.Builder()
                    .setTunerConfiguration(tunerConfig)
                    .setAudioAttributes(mAudioAttributes)
                    .setAudioFormat(audioFormat)
                    .setEncapsulationMode(AudioTrack.ENCAPSULATION_MODE_ELEMENTARY_STREAM)
                    .setBufferSizeInBytes(1024 * 1024)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .setSessionId(0)
                    .build();
            mAudioTrackList.add(audioTrack);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ) {
                AudioManager audioManager = mContext.getSystemService(AudioManager.class);
                int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                audioManager.setParameters("tunerAudioTrackVolume=" + volume);
            }
            Log.d(TAG," startAudioTrack play start 0000 ");
            //audioTrack.play();
            //mMediaExtractor.trackStart(index, tunerid);
            Log.d(TAG," startAudioTrack play end ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getAVTrackIndex(ProgramInfo programInfo){
        int selected_audio_index = programInfo.getAudioSelected();
        LogUtils.d(" selected_audio_index = "+selected_audio_index);
        if (programInfo.pAudios.size() > selected_audio_index) {
            LogUtils.d("Audio Pid = " + programInfo.pAudios.get(selected_audio_index).getPid());
        }
        LogUtils.d("mMediaExtractor.getTrackCount() = "+mMediaExtractor.getTrackCount());
        for (int i = 0; i < mMediaExtractor.getTrackCount(); i++) {
            //LogUtils.d(" Pid = "+mMediaExtractor.trackPid(i)+" "+programInfo.pAudios.get(selected_audio_index).getPid());
            //LogUtils.d("mMediaExtractor.trackIsAudio(i) "+mMediaExtractor.trackIsAudio(i));
            //LogUtils.d("programInfo.pAudios.size() "+programInfo.pAudios.size()+" "+selected_audio_index);
            if (mMediaExtractor.trackIsVideo(i) && (mVideoTrackIndex == -1)) {
                mVideoTrackIndex = i;
            } else if (mMediaExtractor.trackIsAudio(i) &&
                    (programInfo.pAudios.size() > selected_audio_index) &&
                    (mMediaExtractor.trackPid(i) == programInfo.pAudios.get(selected_audio_index).getPid())&&
                    (mAudioTrackIndex == -1)) {
                mAudioTrackIndex = i;
            }
        }
        LogUtils.d("mAudioTrackIndex = "+mAudioTrackIndex);
    }

    private void MediaExtractorCreate(int tunerId,TunerInterface tunerInterface,ProgramInfo programInfo) {
        mMediaExtractor = new MediaExtractor();
        Log.d(TAG,"programInfo = "+programInfo);
        get().check_e213(programInfo);
        mMediaExtractor.setCasSessionCallback(getCasSessionCallback());
        mMediaExtractor.setDataSource(tunerId,tunerInterface,programInfo);
        getAVTrackIndex(programInfo);

        // video open filter and configure
        openAndConfigVideoFilter();
        // audio open filter and configure
        openAndConfigAudioFilter(mProgramInfo);
        // AudioTrack
        //CreateAudioTrack();
        // video create codec
        createVideoCodec();
    }

    private void openAndConfigVideoFilter() {
        if (mVideoTrackIndex < 0) {
            Log.e(TAG, "openAndConfigVideoFilter: invalid mVideoTrackIndex = " + mVideoTrackIndex);
            return;
        }

        // video open filter and configure
        LogUtils.d(" AvCmdMiddle mVideoTrackIndex = "+mVideoTrackIndex);
        mMediaExtractor.trackOpenFilter(mVideoTrackIndex, mPassthrough);
        mVideoFormat = mMediaExtractor.trackGetFormat(mVideoTrackIndex);

        if (mPassthrough) {
            int filterId = mMediaExtractor.trackGetFilterId(mVideoTrackIndex);
            mVideoFormat.setInteger("vendor.tunerhal.video-filter-id", filterId);
        }

        mVideoFormat.setInteger("feature-tunneled-playback", 1);
        mMediaExtractor.trackConfigure(mVideoTrackIndex, mPassthrough);
    }

    private void openAndConfigAudioFilter(ProgramInfo programInfo) {
        if (mAudioTrackIndex < 0) {
            Log.e(TAG, "openAndConfigAudioFilter: invalid mAudioTrackIndex = " + mAudioTrackIndex);
            return;
        }

        if(Pvcfg.getMultiAudio_Enable()) {
            for (int i = 0; i < mMediaExtractor.getTrackCount(); i++) {
                if (mMediaExtractor.trackIsAudio(i)) {
                    LogUtils.d("OpenFilter and Config " + i);
                    mMediaExtractor.trackOpenFilter(i, mPassthrough);
                    mMediaExtractor.trackConfigure(i, mPassthrough);
                }
            }
        }
        else {
            // audio open filter and configure
            int selectedAudio = programInfo.getAudioSelected();
            mAudioTrackIndex = -1;
            if(selectedAudio < programInfo.pAudios.size())
                mAudioTrackIndex = mMediaExtractor.trackIndex(programInfo.pAudios.get(selectedAudio).getPid());
            mAudioTrackIndex = mAudioTrackIndex == -1 ? 1 : mAudioTrackIndex;
            LogUtils.d("AvCmdMiddle selectedAudio = "+selectedAudio+ " pid = "+programInfo.pAudios.get(selectedAudio).getPid()+
                    " mAudioTrackIndex = "+mAudioTrackIndex);
            mMediaExtractor.trackOpenFilter(mAudioTrackIndex, mPassthrough);
            mMediaExtractor.trackConfigure(mAudioTrackIndex, mPassthrough);
        }
    }

    public void createVideoCodec() {
        // video create codec
        int videoCodec = mProgramInfo.pVideo.getCodec();
        String videoCodeName;
        Log.d(TAG, " AvCmdMiddle createVideoCodec: videoCodec = " + mProgramInfo.pVideo.getCodec());

        if(SystemProperties.getBoolean("persist.sys.tunerhal.source.smp",true))
            videoCodeName = MediaUtils.getVideoCodeNameSecure(videoCodec);
        else
            videoCodeName = MediaUtils.getVideoCodeName(videoCodec);
//       String videoMimeType = MediaUtils.getVideoMediaFormatStr(videoCodec);
        try {
            if(mVideoCodec != null){
                if(isVideoCodeStart){
                    logCodecToken("createVideoCodec.stop_old");
                    long stopStartMs = SystemClock.elapsedRealtime();
                    mVideoCodec.stop();
                    isVideoCodeStart = false;
                }
                mVideoCodec.setCallback(null);
                logCodecToken("createVideoCodec.release_old");
                mVideoCodec.release();
                mVideoCodec = null;
            }
//            mVideoCodec = MediaCodec.createDecoderByType(videoMimeType);
//            videoCodeName = mVideoCodec.getName();
//            videoCodeName = videoCodeName + ".secure";
//            mVideoCodec.release();
            mCurrentCodecToken = ++mCodecToken;
            LogUtils.d("FCC_LOG createVideoCodec token=" + mCurrentCodecToken
                    + " tunerId=" + mTunerId
                    + " channelId=" + mChannelId
                    + " preplay=" + mIsPrePlay
                    + " surface=" + mSurface
                    + " surfaceId=" + surfaceId(mSurface)
                    + " " + AvCmdMiddle.surfaceIndexDebug(mSurface));
            LogUtils.d("videoCodeName = "+ videoCodeName);
            mVideoCodec = MediaCodec.createByCodecName(videoCodeName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void autoStartSubtitle() {
        String default_lang = "eng";
        if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG) || Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_TBC))
            default_lang = "chi";
        PrimeDtv primeDtv = ServiceInterface.get_prime_dtv();
        SubtitleInfo subtitleInfo = primeDtv.av_control_get_subtitle_list(DEFAULT_TUNER_ID);
        if(subtitleInfo.getComponentCount() > 0) {
            for(SubtitleInfo.SubtitleComponent subtitleComponent : subtitleInfo.Component) {
                if(subtitleComponent.getLangCode().equals(default_lang)) {
                    primeDtv.av_control_select_subtitle(DEFAULT_TUNER_ID,subtitleComponent);
                    //Log.d(TAG,"av_control_select_subtitle !!!!!!!!!!!");
                    return;
                }
            }
            primeDtv.av_control_select_subtitle(DEFAULT_TUNER_ID, subtitleInfo.Component.get(0));
            //Log.d(TAG,"av_control_select_subtitle !!!!!!!!!!!");
        }
    }
    private void notifyFirstFrameToTifOnce() {
        if (!mIsVideoRender) {   // 只通知一次
            setVideoRender(true);
            if (mCallback != null) {
                    mainHandler.post(() -> mCallback.onFirstFrameRendered(mTunerId, mChannelId));
            }
            // 取消兜底計時（下面會加）
            mainHandler.removeCallbacks(firstFrameTimeout);
        }
    }

    private final Runnable firstFrameTimeout = () -> {
        if (!mIsVideoRender && mCallback != null) {
            mCallback.onVideoUnavailable(mTunerId, TvInputManager.VIDEO_UNAVAILABLE_REASON_UNKNOWN);
        }
    };

    MediaCodec.OnFrameRenderedListener mFrameRenderedListener = new MediaCodec.OnFrameRenderedListener() {
        @Override
        public void onFrameRendered(@NonNull MediaCodec codec, long presentationTimeUs, long nanoTime) {
            //Log.d( TAG, " onFrameRendered codec = " + codec + " presentationTimeUs = " + presentationTimeUs ) ;
            mPreVideoCount = mVideoCount;
            mVideoCount = mVideoCount+1;
            //LogUtils.d("mVideoCount = "+mVideoCount+" mPreVideoCount = "+mPreVideoCount);
            if(mSubtitleManager.GetisStartSubtitle())
                mSubtitleManager.showSubtitle(presentationTimeUs);
            if (mVideoCodec == codec && !mIsVideoRender) {
                if (!mFirstFrameLogged) {
                    mFirstFrameLogged = true;
                    long dtMs = mVideoDecoderStartMs > 0
                            ? (SystemClock.elapsedRealtime() - mVideoDecoderStartMs)
                            : -1;
                }
                //Log.d( TAG, "ggggg onFrameRendered codec = " + codec + " presentationTimeUs = " + presentationTimeUs ) ;
                notifyFirstFrameToTifOnce();   // ★ 加這行
                if (mIsPrePlay) {
                    Log.d( TAG, "FCC_LOG preplay  first FrameRendered ") ;
                    //mainHandler.post(Player.this::pausePreplayFilters);
                }
            }
//            PrimeDtv primeDtv = ServiceApplication.get_prime_dtv();
//            if(primeDtv.gpos_info_get().getSubtitleOnOff() == 1 && !mSubtitleManager.GetisStartSubtitle()) {
//                autoStartSubtitle();
//            }
        }
    };
    private void VideoDecoderThread() {
        final int localToken = mCurrentCodecToken;
        LinkedBlockingQueue<Integer> inputQueue = new LinkedBlockingQueue<>();
        long msTimeout = 15;
        int inIndex = -1;
        MediaEvent e;

        try {
            mVideoDecoderStartMs = SystemClock.elapsedRealtime();
            mFirstInputLogged = false;
            mFirstFrameLogged = false;
            mVideoCodec.setCallback(new MediaCodec.Callback() {
                @Override
                public void onInputBufferAvailable(MediaCodec codec, int index) {
                    if (codec != mVideoCodec) {
                        return;
                    }
                    Log.d(TAG, "onInputBufferAvailable");
                    if (!mFirstInputLogged) {
                        mFirstInputLogged = true;
                        long dtMs = mVideoDecoderStartMs > 0
                                ? (SystemClock.elapsedRealtime() - mVideoDecoderStartMs)
                                : -1;
                    }
                    mVideoCount++;
                    if(mVideoCount > 10000)
                        mVideoCount = 1;
                    inputQueue.offer(index);
                }
                @Override
                public void onOutputBufferAvailable(
                        MediaCodec codec, int index, MediaCodec.BufferInfo info) {
                    if (codec != mVideoCodec) {
                        return;
                    }
                    if (mVideoThreadStop) return;
                    switch (index) {
                        case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                            Log.d(TAG, "V INFO_OUTPUT_BUFFERS_CHANGED");
                            break;
                        case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                            MediaFormat format = mVideoCodec.getOutputFormat();
                            Log.d(TAG, "V New format " + format);
                            break;
                        case MediaCodec.INFO_TRY_AGAIN_LATER:
                            break;
                        default:
                            Log.d(TAG, "V INFO_OUTPUT_BUFFER available index=" + index);
                            if (index >= 0) {
                                mVideoCodec.releaseOutputBuffer(index, true);
                            }
                            break;
                    }
                }
                @Override
                public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
                    if (codec != mVideoCodec) {
                        return;
                    }
                }
                @Override
                public void onError(MediaCodec codec, MediaCodec.CodecException ex) {
                    if (codec != mVideoCodec) {
                        return;
                    }
                    Log.d(TAG,"onError ex = "+ex);
//                    mCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_DECODER_ERROR, 0,0,null);
                    mCallback.onDecoderError();
                    if (mCallback != null) {
                        mainHandler.post(() ->
                                mCallback.onVideoUnavailable(mTunerId, TvInputManager.VIDEO_UNAVAILABLE_REASON_UNKNOWN));

                    }
                }
            }, mVideoDecoderHandler);

            // wait for surface if null or not valid
            // wait 100ms*10 = about 1000 ms
            int waitCount = 0;
            for (; (mSurface == null || !mSurface.isValid()) && waitCount < 10 ; waitCount++) {
                Log.d(TAG, "VideoDecoderThread: mSurface = " + mSurface);
                Thread.sleep(100);
                if (localToken != mCurrentCodecToken) {
                    return;
                }
            }

            if (mSurface == null || !mSurface.isValid()) {
                LogUtils.d("FCC_LOG surface invalid skip configure tunerId=" + mTunerId +
                        " surface=" + mSurface +
                        " surfaceId=" + surfaceId(mSurface) +
                        " " + AvCmdMiddle.surfaceIndexDebug(mSurface));
                return;
            }
            WaitSetVisibleCompleted();
            if (localToken != mCurrentCodecToken) {
                return;
            }
//            ChannelChangeManager.WaitSetVisibleCompleted();
            long configureStartMs = SystemClock.elapsedRealtime();
            LogUtils.d("FCC_LOG codec_configure: token=" + mCurrentCodecToken +
                    " tunerId=" + mTunerId +
                    " surface=" + mSurface +
                    " surfaceId=" + surfaceId(mSurface) +
                    " " + AvCmdMiddle.surfaceIndexDebug(mSurface));
            mVideoCodec.configure(mVideoFormat, mSurface, null, MediaCodec.CONFIGURE_FLAG_USE_BLOCK_MODEL);
            mVideoCodec.setOnFrameRenderedListener(mFrameRenderedListener, null);
            mVideoCodec.start();
            isVideoCodeStart = true;
            mMediaExtractor.trackStart(mVideoTrackIndex, 0/*tunerid*/);
            // 先等一下第一個事件（例如 300ms）；有事件＝demux 在供料
            if (mCallback != null) {
                mainHandler.post(() ->
                        mCallback.onVideoUnavailable(mTunerId, TvInputManager.VIDEO_UNAVAILABLE_REASON_BUFFERING));

            }

            mainHandler.postDelayed(firstFrameTimeout, 5000);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void WaitSetVisibleCompleted(){
        int count = 0;
        if(Pvcfg.isWaitSurfaceReady() == false)
            return;
//        LogUtils.d("[DB_Surface] 1111 isSetVisibleCompleted = "+isSetVisibleCompleted);
        while(!isSetVisibleCompleted){
            try {
//                LogUtils.d("[DB_Surface] 2222 Player["+mTunerId+"] isSetVisibleCompleted = "+isSetVisibleCompleted);
                Thread.sleep(50);
                count++;
                if(count >= 30) {//wait 1000ms
                    LogUtils.d("FCC_LOG setVisibleCompleted timeout tunerId=" + mTunerId);
                    break;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void set_surface(SurfaceHolder surfaceHolder){
        Surface surface = surfaceHolder == null ? null : surfaceHolder.getSurface();
        LogUtils.d("FCC_LOG set_surface: tunerId=" + mTunerId +
                " holder=" + surfaceHolder +
                " surface=" + surface +
                " surfaceId=" + surfaceId(surface) +
                " " + AvCmdMiddle.surfaceIndexDebug(surface));
        mSurfaceHolder = surfaceHolder;
        mSurface = surface;
    }
    public void set_new_surface(Surface surface) {
        //synchronized(mSurfaceLock) {
        try {
            mMedicodcSemaphore.acquire();
            // ★關鍵：先更新成新 surface，後續 restart 才會 configure 到正確的那顆
            mSurface = surface;
            LogUtils.d("FCC_LOG set_new_surface: tunerId=" + mTunerId +
                    " surface=" + surface +
                    " surfaceId=" + surfaceId(surface) +
                    " " + AvCmdMiddle.surfaceIndexDebug(surface) +
                    " videoCodec=" + mVideoCodec +
                    " isVideoCodeStart=" + isVideoCodeStart +
                    " token=" + mCurrentCodecToken);
            if (mVideoCodec != null && isVideoCodeStart) {        
                LogUtils.d("set new surface = " + surface);
                mVideoCodec.setOutputSurface(surface);    
            }
            //mSurface = surface;
        } catch (InterruptedException e) {
            Log.e(TAG, "set_new_surface: ", e);
//            throw new RuntimeException(e);
        }finally {
            mMedicodcSemaphore.release();
        }
        //}
    }
    //0 no Video
    //1 Video OK
    //2 skip
    public int isNoVideo() {
        Log.d(TAG, "mPreVideoCount CH "+mProgramInfo.getDisplayNum()+" "+mProgramInfo.getDisplayName());
        Log.d(TAG,"mPreVideoCount = "+mPreVideoCount + " mVideoCount = " + mVideoCount+" mVideoCountSame = "+mVideoCountSame);
        if(mVideoTrackIndex == -1)
            return 2;
        if(mPreVideoCount == mVideoCount) {
            mVideoCountSame ++;
//            if(mVideoCountSame > Pvcfg.getE213CheckTime())
//                return 0;
//            else
                return 2;
        }
        else {
            mPreVideoCount = mVideoCount;
            mVideoCountSame = 0;
            return 1;
        }
    }

    public void play(int preplay) {
		LogUtils.d("start");
        LogUtils.d("FCC_LOG play surface: tunerId=" + mTunerId +
                " surface=" + mSurface +
                " surfaceId=" + surfaceId(mSurface) +
                " valid=" + (mSurface != null && mSurface.isValid()) +
                " " + AvCmdMiddle.surfaceIndexDebug(mSurface));
        //LogUtils.d("AvCmdMiddle play 00000000000 createVideoCodec");
        if(mVideoTrackIndex != -1) {
            //createVideoCodec();
        //LogUtils.d("AvCmdMiddle play 1111111111111 startVideoCodec");
            startVideoCodec();
        }
        mainHandler.removeCallbacks(firstFrameTimeout);
        setVideoRender(false);//chucktif
        //LogUtils.d("AvCmdMiddle play 222222222222222 mMediaExtractor.start");
        //mMediaExtractor.start();
        //LogUtils.d("AvCmdMiddle play 33333333333333333 CreateAudioTrack");
        if(Pvcfg.isFccV3Enable()){
            LogUtils.d("[FCC3] mTunerId = "+mTunerId+" preplay = "+preplay);
            audio_track_skip_focus(preplay);
            setPrePlay((preplay==1)?true:false);
        }
        mPreplayFiltersPaused = false;
        CreateAudioTrack();
        //LogUtils.d("AvCmdMiddle play 4444444444444444 start_audio");
        start_audio();
        //LogUtils.d("AvCmdMiddle play 555555555555555");
        if(Pvcfg.getMultiAudio_Enable() || Pvcfg.isFccV3Enable()) {
            if (mAudioTrackIndex < 0) {
                LogUtils.e("dtvplay audio switch skipped: invalid mAudioTrackIndex = " + mAudioTrackIndex);
            } else {
                int filterId = -1;
                try {
                    filterId = mMediaExtractor.trackGetFilterId(mAudioTrackIndex);
                    LogUtils.d("dtvplay switchAudioTrackByFilterID: mAudioTrackIndex="
                            + mAudioTrackIndex + ", filterId=" + filterId);
                } catch (IndexOutOfBoundsException e) {
                    // 第二個參數要丟 Throwable
                    LogUtils.e("play trackGetFilterId IndexOutOfBounds: mAudioTrackIndex="
                            + mAudioTrackIndex, e);
                } catch (Exception e) {
                    LogUtils.e("dtvplay trackGetFilterId unexpected error: mAudioTrackIndex="
                            + mAudioTrackIndex, e);
                }
    
                if (filterId >= 0) {
                    switchAudioTrackByFilterID(filterId);
                } else {
                    LogUtils.e("dtvplay audio switch skipped: invalid filterId = " + filterId
                            + " (mAudioTrackIndex=" + mAudioTrackIndex + ")");
                }
            }
        }
        else{
            //switchAudioTrackByFilterID(mMediaExtractor.trackGetFilterId(mAudioTrackIndex));
        }
        if(preplay == 0) {
            PrimeDtv primeDtv = ServiceInterface.get_prime_dtv();
            LogUtils.d("getSubtitleOnOff = "+ GposInfo.getSubtitleOnOff(mContext)+" GetisStartSubtitle = "+mSubtitleManager.GetisStartSubtitle());
            if (GposInfo.getSubtitleOnOff(mContext) == 1 && !mSubtitleManager.GetisStartSubtitle() &&
                !Pvcfg.isPrimeTvInputEnable()) {
                autoStartSubtitle();
            }
        }
        //LogUtils.d("AvCmdMiddle play 6666666666666");
		LogUtils.d("end");
    }

    public void audio_track_skip_focus(int value){
        AudioManager audioManager = mContext.getSystemService(AudioManager.class);
        LogUtils.d("[FCC3] mTunerId = "+mTunerId+" audio_track_skip_focus = "+value);
        audioManager.setParameters("audio_track_skip_focus="+value);
    }

    public void switchAudioTrackByFilterID(int id){
        LogUtils.d("switchAudioTrack id = " +  id);
        if (id == -1)
            return;
        AudioManager audioManager = mContext.getSystemService(AudioManager.class);
        audioManager.setParameters("switch_audiotrack_by_filterid="+id);
    }

    public ProgramInfo getProgramInfo() {
        return mProgramInfo;
    }

    public boolean IsVideoRender() {
        return mIsVideoRender;
    }

    public void setVideoRender(boolean mIsVideoRender) {
        this.mIsVideoRender = mIsVideoRender;
    }

    public boolean hasCasSessionError() {
        return mCasSessionError;
    }

    public long getChannelId() {
        return mChannelId;
    }

    public int getTunerId() {
        return mTunerId;
    }

    public boolean IsPrePlay() {
        LogUtils.d("[FCC3] mTunerId = "+mTunerId+" IsPrePlay = "+mIsPrePlay);
        return mIsPrePlay;
    }

    public boolean isPrePlay() {
        return mIsPrePlay;
    }

    public void setPrePlay(boolean mIsPrePlay) {
        LogUtils.d("[FCC3] mTunerId = "+mTunerId+" IsPrePlay = "+mIsPrePlay);
        this.mIsPrePlay = mIsPrePlay;
        if (!mIsPrePlay) {
            mPreplayFiltersPaused = false;
        }
    }

    public void pausePreplayFilters() {
        if (!mIsPrePlay || mPreplayFiltersPaused) {
            return;
        }
        LogUtils.d("FCC_LOG preplay pause filters tunerId=" + mTunerId +
                " channelId=" + mChannelId);
        if (mMediaExtractor != null) {
            mMediaExtractor.stop();
        }
        mPreplayFiltersPaused = true;
    }

    public void resumePreplayFilters() {
        if (!mPreplayFiltersPaused) {
            return;
        }
        LogUtils.d("FCC_LOG preplay resume filters tunerId=" + mTunerId +
                " channelId=" + mChannelId);
        if (mMediaExtractor != null) {
            //mMediaExtractor.startPcrFilter();
            if (mVideoTrackIndex >= 0) {
                mMediaExtractor.trackStart(mVideoTrackIndex, 0);
            }
        }
        start_audio();
        mPreplayFiltersPaused = false;
    }

    public void setSetVisibleCompleted(boolean setVisibleCompleted) {
        isSetVisibleCompleted = setVisibleCompleted;
    }

    public interface Callback {
        void onHttpError(int responseCode, long channelId, String contentId);
        void onCasError(String msg, long channelId);
        void onConnectTimeout(String msg, long channelId);
        void onDecoderError();
        void onRemoveLicense(long channelId,String contentId);
        // ★ 新增：告訴 TIF「第一幀已顯示」
        void onFirstFrameRendered(int tunerId, long channelId);

        // ★ 新增：告訴 TIF「影像不可用（原因由 TIF 常數對應）」
        void onVideoUnavailable(int tunerId, int reason);
    }
}
