package com.prime.dtv.service.Player;

import static com.prime.dmg.launcher.Home.LiveTV.LiveTvManager.DEFAULT_TUNER_ID;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.tv.tuner.Tuner;
import android.media.tv.tuner.filter.MediaEvent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemProperties;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import com.prime.android.audiotrack.PrimeAudioTrack;
import com.prime.dmg.launcher.HomeApplication;
import com.prime.dtv.ChannelChangeManager;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.service.Tuner.TunerInterface;
import com.prime.dtv.service.Util.MediaUtils;
import com.prime.dtv.service.subtitle.SubtitleManager;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.service.Table.StreamType;
import com.prime.dtv.sysdata.SubtitleInfo;
import com.prime.dtv.utils.LogUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class Player {
    private static final String TAG = "Player";
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

    private boolean mCasSessionError = false;
    private long mChannelId;
    private boolean mIsPrePlay = false;
    private WeakReference<AvCmdMiddle> g_avCmdMiddle_ref;
    private static final Object mSurfaceLock = new Object();
    private final Semaphore mMedicodcSemaphore = new Semaphore(1);
    private CasSession.Callback getCasSessionCallback() {
        return new CasSession.Callback() {
            @Override
            public void onHttpError(int responseCode, String contentId) {
                LogUtils.d("CasSession-Error callback: onHttpError");
                mCasSessionError = true;
                if (mCallback != null) {
                    mCallback.onHttpError(responseCode, mProgramInfo.getChannelId(), contentId);
                }
            }

            @Override
            public void onCasError(String msg) {
                LogUtils.d("CasSession-Error callback: onCasError");
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
//        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
//            @Override
//            public void surfaceCreated(@NonNull SurfaceHolder holder) {
//                Log.d(TAG, "AvCmdMiddle mTunerId["+mTunerId+"] surfaceCreated: " + holder.getSurface());
//                mSurface = holder.getSurface();
//            }
//
//            @Override
//            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
//                Log.d(TAG, "AvCmdMiddle mTunerId["+mTunerId+"] surfaceChanged: SurfaceHolder = " + holder);
//                mSurface = holder.getSurface();
//                if(mVideoCodec != null){
//                    mVideoCodec.setOutputSurface(mSurface);
//                }
//            }
//
//            @Override
//            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
//                Log.d(TAG, "AvCmdMiddle mTunerId["+mTunerId+"] surfaceDestroyed: SurfaceHolder = " + holder);
////                mSurface = null;
//            }
//        });
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
        LogUtils.d("AvCmdMiddle stop start !!! mChannelId = "+mChannelId);
        try {
            mMedicodcSemaphore.acquire();
//            if(mSurface != null && mSurfaceHolder != null) {
//                mSurfaceHolder.removeCallback(null);
//            }
            if(Pvcfg.isFccV3Enable()){
                audio_track_skip_focus(0);
            }
            if (mMediaExtractor != null) {
                LogUtils.d(  "[FCC_DEBUG] AV Filter stop start" ) ;
                mMediaExtractor.stop();
//                mMediaExtractor.close();
//                mMediaExtractor = null;
                LogUtils.d(  "[FCC_DEBUG] AV Filter stop end" ) ;
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
                LogUtils.d(  "[FCC_DEBUG] isVideoCodeStart = "+ isVideoCodeStart ) ;
                if(isVideoCodeStart) {
                    LogUtils.d(  "[FCC_DEBUG] mVideoCodec.stop start" ) ;
                    mVideoCodec.stop();
                    LogUtils.d(  "[FCC_DEBUG] mVideoCodec.stop end" ) ;
                }
                mVideoCodec.setCallback(null);
                LogUtils.d(  "[FCC_DEBUG] MediaCodec release start" ) ;
                mVideoCodec.release();
                mVideoCodec = null;
                isVideoCodeStart = false;
                LogUtils.d(  "[FCC_DEBUG] MediaCodec release end" ) ;
            }
            LogUtils.d(  "[FCC_DEBUG] Audio Track stop => start" ) ;
            for(AudioTrack a: mAudioTrackList){
                a.stop();
                a.release();
            }
            mAudioTrackList.clear();
            LogUtils.d(  "[FCC_DEBUG] Audio Track stop => end" ) ;
        } catch(Exception ex) {
            Log.e(TAG, "surfaceDestroyed() exception", ex);
//            ex.printStackTrace();
        } finally {
            mMedicodcSemaphore.release();
        }
        LogUtils.d("AvCmdMiddle stop end !!!");
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
        try {
            if (mMediaExtractor != null) {
                LogUtils.d(  "[FCC_DEBUG] AV Filter close start" ) ;
                mMediaExtractor.close();
                mMediaExtractor = null;
                LogUtils.d(  "[FCC_DEBUG] AV Filter close end" ) ;
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
            mVideoFormat = mMediaExtractor.trackGetFormat(mVideoTrackIndex);
            int avSyncHwId = mMediaExtractor.trackGetAvSyncHwId(mVideoTrackIndex);

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
                mVideoFormat.setInteger("audio-hw-sync", avSyncHwId);
                mVideoFormat.setInteger("vendor.tis.videofilterId", mMediaExtractor.trackGetFilterId(mVideoTrackIndex));
                mVideoFormat.setInteger("vendor.tis.videofilterId.value", mMediaExtractor.trackGetFilterId(mVideoTrackIndex)); //For Codec2.0
            } else {
                mVideoFormat.setInteger(MediaFormat.KEY_HARDWARE_AV_SYNC_ID, avSyncHwId);
            }

            Log.d(TAG, "startVideoCodec: mVideoCodec = " + mVideoCodec);
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
                startAudioTrack(audioCodec, tuner_id, mAudioTrackIndex);
            }
        }
    }
    public void startAudioTrack(int audioCodec, int tunerid, int index) {
        try {
            LogUtils.d(" startAudioTrack  start audioCodec = " + audioCodec+" mAudioTrackIndex = "+mAudioTrackIndex);

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
        LogUtils.d("[FCC_DEBUG] open and config Video Filter start");
        openAndConfigVideoFilter();
        LogUtils.d("[FCC_DEBUG] open and config Video Filter end");
        // audio open filter and configure
        LogUtils.d("[FCC_DEBUG] open and config Audio Filter start");
        openAndConfigAudioFilter(mProgramInfo);
        LogUtils.d("[FCC_DEBUG] open and config Audio Filter end");
        // AudioTrack
        //CreateAudioTrack();
        // video create codec
        LogUtils.d("[FCC_DEBUG] createVideoCodec  start");
        createVideoCodec();
        LogUtils.d("[FCC_DEBUG] createVideoCodec  end");
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
                    mVideoCodec.stop();
                    isVideoCodeStart = false;
                }
                mVideoCodec.setCallback(null);
                mVideoCodec.release();
                mVideoCodec = null;
            }
//            mVideoCodec = MediaCodec.createDecoderByType(videoMimeType);
//            videoCodeName = mVideoCodec.getName();
//            videoCodeName = videoCodeName + ".secure";
//            mVideoCodec.release();
            LogUtils.d("videoCodeName = "+ videoCodeName);
            mVideoCodec = MediaCodec.createByCodecName(videoCodeName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void autoStartSubtitle() {
        String default_lang = "eng";
        if(Pvcfg.getModuleType() == Pvcfg.MODULE_DMG || Pvcfg.getModuleType() == Pvcfg.MODULE_TBC)
            default_lang = "chi";
        PrimeDtv primeDtv = HomeApplication.get_prime_dtv();
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

    MediaCodec.OnFrameRenderedListener mFrameRenderedListener = new MediaCodec.OnFrameRenderedListener() {
        @Override
        public void onFrameRendered(@NonNull MediaCodec codec, long presentationTimeUs, long nanoTime) {
            //Log.d( TAG, "onFrameRendered codec = " + codec + " presentationTimeUs = " + presentationTimeUs ) ;
            mPreVideoCount = mVideoCount;
            mVideoCount = mVideoCount+1;
            //LogUtils.d("mVideoCount = "+mVideoCount+" mPreVideoCount = "+mPreVideoCount);
            if(mSubtitleManager.GetisStartSubtitle())
                mSubtitleManager.showSubtitle(presentationTimeUs);
            if (mVideoCodec == codec && !mIsVideoRender) {
                setVideoRender(true);
            }
//            PrimeDtv primeDtv = HomeApplication.get_prime_dtv();
//            if(primeDtv.gpos_info_get().getSubtitleOnOff() == 1 && !mSubtitleManager.GetisStartSubtitle()) {
//                autoStartSubtitle();
//            }
        }
    };
    private void VideoDecoderThread() {
        LinkedBlockingQueue<Integer> inputQueue = new LinkedBlockingQueue<>();
        long msTimeout = 15;
        int inIndex = -1;
        MediaEvent e;

        try {
            mVideoCodec.setCallback(new MediaCodec.Callback() {
                @Override
                public void onInputBufferAvailable(MediaCodec codec, int index) {
                    Log.d(TAG, "onInputBufferAvailable");
                    mVideoCount++;
                    if(mVideoCount > 10000)
                        mVideoCount = 1;
                    inputQueue.offer(index);
                }
                @Override
                public void onOutputBufferAvailable(
                        MediaCodec codec, int index, MediaCodec.BufferInfo info) {
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
                }
                @Override
                public void onError(MediaCodec codec, MediaCodec.CodecException ex) {
                    Log.d(TAG,"onError ex = "+ex);
//                    mCallback.sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_DECODER_ERROR, 0,0,null);
                    mCallback.onDecoderError();
                }
            }, mVideoDecoderHandler);

            // wait for surface if null or not valid
            // wait 100ms*10 = about 1000 ms
            Log.d(TAG, "VideoDecoderThread: mSurface = " + mSurface);
            for (int i = 0 ; (mSurface == null || !mSurface.isValid()) && i < 10 ; i++) {
                Log.d(TAG, "VideoDecoderThread: mSurface = " + mSurface);
                Thread.sleep(100);
            }

            if (mSurface == null || !mSurface.isValid()) {
                Log.e(TAG, "VideoDecoderThread: mSurface is null or not valid");
                //return;
            }
            ChannelChangeManager.WaitSetVisibleCompleted();
            Log.d(TAG, " [FCC_DEBUG] [DB_Surface] VideoDecoderThread: mVideoCodec.configure mSurface = "+mSurface);
            mVideoCodec.configure(mVideoFormat, mSurface, null, MediaCodec.CONFIGURE_FLAG_USE_BLOCK_MODEL);
            mVideoCodec.setOnFrameRenderedListener(mFrameRenderedListener, null);
            Log.d( TAG, " [FCC_DEBUG] VideoDecoderThread: mVideoCodec.start" ) ;
            mVideoCodec.start();
            isVideoCodeStart = true;
            Log.d( TAG, "[FCC_DEBUG] VideoDecoderThread: mMediaExttractor video filter trackStart" ) ;
            mMediaExtractor.trackStart(mVideoTrackIndex, 0/*tunerid*/);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void set_surface(SurfaceHolder surfaceHolder){
        mSurfaceHolder = surfaceHolder;
        mSurface = surfaceHolder.getSurface();
    }
    public void set_new_surface(Surface surface) {
        //synchronized(mSurfaceLock) {
        try {
            mMedicodcSemaphore.acquire();
            if (mVideoCodec != null && isVideoCodeStart) {
                LogUtils.d("set new surface = " + surface);
                mVideoCodec.setOutputSurface(surface);
            }
            mSurface = surface;
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
        //LogUtils.d("AvCmdMiddle play 00000000000 createVideoCodec");
        if(mVideoTrackIndex != -1) {
            //createVideoCodec();
        //LogUtils.d("AvCmdMiddle play 1111111111111 startVideoCodec");
            LogUtils.d("[FCC_DEBUG] startVideoCodec  start ");
            startVideoCodec();
            LogUtils.d("[FCC_DEBUG] startVideoCodec  end ");
        }
        //LogUtils.d("AvCmdMiddle play 222222222222222 mMediaExtractor.start");
        //LogUtils.d("[FCC_DEBUG] startVideoCodec  start ");
        //mMediaExtractor.start();
        //LogUtils.d("[FCC_DEBUG] startVideoCodec  start ");
        //LogUtils.d("AvCmdMiddle play 33333333333333333 CreateAudioTrack");
        if(Pvcfg.isFccV3Enable()){
            LogUtils.d("[FCC3] mTunerId = "+mTunerId+" preplay = "+preplay);
            audio_track_skip_focus(preplay);
            setPrePlay((preplay==1)?true:false);
        }
        LogUtils.d("[FCC_DEBUG] CreateAudioTrack  start ");
        CreateAudioTrack();
        LogUtils.d("[FCC_DEBUG] CreateAudioTrack  end ");
        //LogUtils.d("AvCmdMiddle play 4444444444444444 start_audio");
        LogUtils.d("[FCC_DEBUG] start_audio  start ");
        start_audio();
        LogUtils.d("[FCC_DEBUG] start_audio  end ");
        //LogUtils.d("AvCmdMiddle play 555555555555555");
        if(Pvcfg.getMultiAudio_Enable() || Pvcfg.isFccV3Enable()) {
            if(preplay == 0) {
                switchAudioTrackByFilterID(mMediaExtractor.trackGetFilterId(mAudioTrackIndex));
            }
        }
        else{
            //switchAudioTrackByFilterID(mMediaExtractor.trackGetFilterId(mAudioTrackIndex));
        }
        if(preplay == 0) {
            PrimeDtv primeDtv = HomeApplication.get_prime_dtv();
            LogUtils.d("getSubtitleOnOff = "+primeDtv.gpos_info_get().getSubtitleOnOff()+" GetisStartSubtitle = "+mSubtitleManager.GetisStartSubtitle());
            if (primeDtv.gpos_info_get().getSubtitleOnOff() == 1 && !mSubtitleManager.GetisStartSubtitle()) {
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

    public void setPrePlay(boolean mIsPrePlay) {
        LogUtils.d("[FCC3] mTunerId = "+mTunerId+" IsPrePlay = "+mIsPrePlay);
        this.mIsPrePlay = mIsPrePlay;
    }

    public interface Callback {
        void onHttpError(int responseCode, long channelId, String contentId);
        void onCasError(String msg, long channelId);
        void onConnectTimeout(String msg, long channelId);
        void onDecoderError();
        void onRemoveLicense(long channelId,String contentId);
    }
}
