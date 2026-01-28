/*
 * Copyright 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.TvInput;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Point;
import android.media.PlaybackParams;
import android.media.tv.TvContentRating;
import android.media.tv.TvInputManager;
import android.media.tv.TvTrackInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.storage.StorageManager;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.CaptioningManager;

import com.dolphin.dtv.HiDtvMediaPlayer;
import com.dolphin.dtv.PvrFileInfo;
//import com.google.android.exoplayer.text.CaptionStyleCompat;
//import com.google.android.exoplayer.text.SubtitleLayout;
//import com.google.android.exoplayer.util.Util;
import com.google.android.exoplayer2.text.CaptionStyleCompat;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.media.tv.companionlibrary.BaseTvInputService;
import com.google.android.media.tv.companionlibrary.TvPlayer;
import com.google.android.media.tv.companionlibrary.model.Advertisement;
import com.google.android.media.tv.companionlibrary.model.Channel;
import com.google.android.media.tv.companionlibrary.model.InternalProviderData;
import com.google.android.media.tv.companionlibrary.model.Program;
import com.google.android.media.tv.companionlibrary.model.RecordedProgram;
import com.google.android.media.tv.companionlibrary.utils.TvContractUtils;
import com.mtest.utils.PesiStorageHelper;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.PvrInfo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.TvInput.PesiPlayer.Listener;
import static com.TvInput.PesiPlayer.MDOE_RECORD;
import static com.TvInput.PesiPlayer.MODE_PLAYBACK;
import static com.TvInput.PesiPlayer.MODE_TIMESHIFT;

/**
 * TvInputService which provides a full implementation of EPG, subtitles, multi-audio, parental
 * controls, and overlay view.
 */
public class PesiTvInputService extends BaseTvInputService {
    private static final String TAG = PesiTvInputService.class.getSimpleName();
    private static final long EPG_SYNC_DELAYED_PERIOD_MS = 1000 * 2; // 2 Seconds
    private static final String KEY_CHANNEL_ID = "Channel ID";
    private static final String KEY_RECORD_PATH = "Record Path";//eric lin 20181025 20181026 pesi player, add
    private long mPrimeChannelID;//eric lin 20181016 pesi player, add
    private String mCurrentChannelName;//eric lin 20181017 pesi player record, add
    private static String mInputId = "";// Edwin 20181108 update EPG or clean EPG
    private static long mOldTime = 0; // Edwin 20181115 check EPG sync period
    public static final String PESI_TVINPUT_SERVICE_NAME = "com.prime.dtvplayer/com.TvInput.PesiTvInputService";
    private CaptioningManager mCaptioningManager;
    private Context mContext;
    private PesiTvInputSessionImpl mSession;
    private PesiTvInputService.RecordingSession mRecordingSession=null;//eric lin 20181116 live tv record disk full
    //private PesiPlayer mPesiPlayer=null;//eric lin 20181107 record A watch B issue, mark //eric lin 20181026 modify pesi player
    public static boolean LIVE_TV_MODE = false; // Edwin 20181102 fix home key cause Live Channel black screen
    private boolean mEnablePVR=false;
    // Edwin 20181113 sync EPG every 1 hour -s
    private SyncEpgThread mSyncEpgThread = null;//eric lin 20181116 avoid record A and change back to A cause crash
    private static boolean mFirstBoot = true ;
    private class SyncEpgThread extends Thread
    {
        @Override
        public void run ()
        {
            super.run();
            while ( true )
            {
                long period = EpgSyncService.getEpgSyncPeriod(); // Edwin 20181115 update Period
                long localTime = System.currentTimeMillis();
                long syncPeriod = localTime - mOldTime;

                try {
                    Log.d(TAG, "mSyncEpgThread: period = "+period+" localtime = "+localTime+" oldTime = "+mOldTime+" syncPeriod = "+syncPeriod);
                    if ( syncPeriod > period || (mOldTime == 0) ) { // Edwin 20181115 check EPG sync period
                        Log.d(TAG, "mSyncEpgThread: sync EPG");
                        updateEPG(mContext, "onCreateSession"); // Edwin 20181108 update EPG or clean EPG
                        mOldTime = localTime;
                    }
                    Thread.sleep(period);
                } catch ( InterruptedException e ) {
                    Log.d(TAG, "mSyncEpgThread: interrupt EPG sync");
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
    // Edwin 20181113 sync EPG every 1 hour -e

    /**
     * Gets the track id of the track type and track index.
     *
     * @param trackType  the type of the track e.g. TvTrackInfo.TYPE_AUDIO
     * @param trackIndex the index of that track within the media. e.g. 0, 1, 2...
     * @return the track id for the type & index combination.
     */
    private static String getTrackId(int trackType, int trackIndex) {
        Log.d(TAG, "getTrackId: ");
        return trackType + "-" + trackIndex;
    }

    /**
     * Gets the index of the track for a given track id.
     *
     * @param trackId the track id.
     * @return the track index for the given id, as an integer.
     */
    private static int getIndexFromTrackId(String trackId) {
        Log.d(TAG, "getIndexFromTrackId: ");
        return Integer.parseInt(trackId.split("-")[1]);
    }

    // Edwin 20181108 update EPG or clean EPG
    public static void updateEPG(Context context, String source)
    {
        Log.d(TAG, "updateEPG: source = "+source);
        if ( mInputId.isEmpty() )//gary20190830 modify for pesilauncher TvView
            mInputId = PESI_TVINPUT_SERVICE_NAME ;//gary20190830 fix pesilaucher preview not work//gary20190830 modify for pesilauncher TvView
        Log.d(TAG, "mInputId = "+mInputId);
        EpgSyncService.cancelAllSyncRequests(context);
        EpgSyncService.requestImmediateSync(context, mInputId,
                EpgSyncService.DEFAULT_EPG_DURATION,
                new ComponentName(context, EpgSyncService.class));
    }

    private void syncStartTime( final Context context )
    {
        final HiDtvMediaPlayer HiDtv = HiDtvMediaPlayer.getInstance();
        final Handler handler = new Handler();
        new Thread(new Runnable()
        {
            @Override
            public void run ()
            {
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Date startTime = HiDtv.getDtvDate();

                Log.d(TAG, "syncStartTime: startTime str = "+startTime.toString());
                Log.d(TAG, "syncStartTime: startTime long = "+startTime.getTime());
                if ( alarmManager != null )
                {
                    Log.d(TAG, "syncStartTime: alarm manager sync start time");
                    alarmManager.setTime(startTime.getTime());
                }
            }
        }).start();
    }

    private boolean isDtvPlayerOnTop(){//eric lin 20181121 is dtvplayer on top
        String PESI_DTVPLAYER_PACKAGE = "com.prime.dtvplayer";
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService( Context.ACTIVITY_SERVICE );
        List<ActivityManager.RunningTaskInfo> infos = activityManager.getRunningTasks(100);
        Log.d( TAG, "top Activity PackageName = " + infos.get(0).baseActivity.getPackageName() ) ;
        return infos.get(0).baseActivity.getPackageName().equals(PESI_DTVPLAYER_PACKAGE) ;
    }

    @Override
    public void onDestroy ()
    {
        Log.d(TAG, "onDestroy: mSession = " + mSession);
        super.onDestroy();
        LIVE_TV_MODE = false; // Edwin 20181102 fix home key cause Live Channel black screen
        mSession = null ;
        // jim 2019/09/10 fix multi app call tvinputservice and  no video when av close -s
        if ( isDtvPlayerOnTop() == false )
        {
            HiDtvMediaPlayer.getInstance().AvControlPlayStop(0);
            //HiDtvMediaPlayer.getInstance().AvControlClose(0);
        }
        // jim 2019/09/10 fix multi app call tvinputservice and  no video when av close -e
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();
        mContext = PesiTvInputService.this;
        mCaptioningManager = (CaptioningManager) getSystemService(Context.CAPTIONING_SERVICE);
        mSession = null ;
        LIVE_TV_MODE = true; // Edwin 20181102 fix home key cause Live Channel black screen
        //syncStartTime(mContext);
    }

    @Override
    public final Session onCreateSession( final String inputId) {
        // jim 2019/09/10 fix first time boot, launcher will play dtv in full screen size -s
        if ( mFirstBoot == true )
        {
            HiDtvMediaPlayer.getInstance().AvControlPlayStop(0);
            //HiDtvMediaPlayer.getInstance().AvControlClose(0);
            mFirstBoot = false ;
        }
        // jim 2019/09/10 fix first time boot, launcher will play dtv in full screen size -e
        mSession = new PesiTvInputSessionImpl(this, inputId);
        mSession.setOverlayViewEnabled(true);
        mInputId = inputId;
        mSyncEpgThread = new SyncEpgThread();//eric lin 20181116 avoid record A and change back to A cause crash
        mSyncEpgThread.start(); // Edwin 20181113 sync EPG every 1 hour

        Log.d(TAG, "onCreateSession: mSession = " + mSession);
        return mSession;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public PesiTvInputService.RecordingSession onCreateRecordingSession(String inputId) {
        Log.d(TAG, "onCreateRecordingSession: ");
        mRecordingSession = new PesiRecordingSession(this, inputId);//eric lin 20181116 live tv record disk full
        return mRecordingSession;//eric lin 20181116 live tv record disk full
        //return new PesiRecordingSession(this, inputId);
    }

    //class PesiTvInputSessionImpl extends BaseTvInputService.Session implements
    //        DemoPlayer.Listener, DemoPlayer.CaptionListener {
    class PesiTvInputSessionImpl extends BaseTvInputService.Session implements
           Listener{//eric lin 20181016 pesi player
        private static final float CAPTION_LINE_HEIGHT_RATIO = 0.0533f;
        private static final int TEXT_UNIT_PIXELS = 0;
        private static final String UNKNOWN_LANGUAGE = "und";

        private int mSelectedSubtitleTrackIndex;
        private /*SubtitleLayout*/SubtitleView mSubtitleView; // edwin 20200429 for playing stream
        //private DemoPlayer mPlayer;//eric lin 20181016 pesi player, mark
        //private HiDtvMediaPlayer mHiPlayer;//eric lin 20181016 pesi player, mark
        //private PesiPlayer mPesiPlayer=null;//eric lin 20181016 pesi player, add
        private boolean mCaptionEnabled;
        private String mInputId;
        //private  int mPrimeChannelID;//eric lin 20181016 pesi player, add
        private PesiPlayer mPesiPlayer=null;//eric lin 20181107 record A watch B issue
        private Surface mSurface = null;

        PesiTvInputSessionImpl(Context context, String inputId) {
            super(context, inputId);
            Log.d(TAG, "PesiTvInputSessionImpl: ");
            mCaptionEnabled = mCaptioningManager.isEnabled();
            mInputId = inputId;
        }

        @Override
        public View onCreateOverlayView() {
            Log.d(TAG, "onCreateOverlayView: ");
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            mSubtitleView = /*(SubtitleLayout)*/(SubtitleView) inflater.inflate(R.layout.subtitleview, null); // edwin 20200429 for playing stream

            // Configure the subtitle view.
            CaptionStyleCompat captionStyle;
            float captionTextSize = getCaptionFontSize();
            captionStyle = CaptionStyleCompat
                    .createFromCaptionStyle(mCaptioningManager.getUserStyle());
            captionTextSize *= mCaptioningManager.getFontScale();
            mSubtitleView.setStyle(captionStyle);
            mSubtitleView.setFixedTextSize(TEXT_UNIT_PIXELS, captionTextSize);
            mSubtitleView.setVisibility(View.VISIBLE);

            return mSubtitleView;
        }

        private List<TvTrackInfo> getAllTracks() {
            Log.d(TAG, "getAllTracks: ");
            String trackId;
            List<TvTrackInfo> tracks = new ArrayList<>();

            int[] trackTypes = {
                    DemoPlayer.TYPE_AUDIO,
                    DemoPlayer.TYPE_VIDEO,
                    DemoPlayer.TYPE_TEXT
            };
            
            //eric lin 20181102 live tv track,-start 
            for (int trackType : trackTypes) {
                int count = mPesiPlayer.getTrackCount(trackType);
                Log.d(TAG, "getAllTracks: XKK trackType="+trackType+", count="+count);//eric lin test
                for (int i = 0; i < count; i++) {
                    Log.d(TAG, "getAllTracks: XKK trackType="+trackType+", index="+i);//eric lin test
                    PesiMediaFormat format = mPesiPlayer.getTrackFormat(trackType, i);
                    trackId = getTrackId(trackType, i);
                    TvTrackInfo.Builder builder = new TvTrackInfo.Builder(trackType, trackId);


                    if (trackType == DemoPlayer.TYPE_VIDEO) {
//                        if (format.maxWidth != MediaFormat.NO_VALUE) {
//                            builder.setVideoWidth(format.maxWidth);
//                        }
                        if (format.width != PesiMediaFormat.NO_VALUE) {
                            builder.setVideoWidth(format.width);
                        }
//                        if (format.maxHeight != MediaFormat.NO_VALUE) {
//                            builder.setVideoHeight(format.maxHeight);
//            }
                        if (format.height != PesiMediaFormat.NO_VALUE) {
                            builder.setVideoHeight(format.height);
                        }
                    } else if (trackType == DemoPlayer.TYPE_AUDIO) {
                        builder.setAudioChannelCount(format.channelCount);
                        builder.setAudioSampleRate(format.sampleRate);
                        if (format.language != null && !UNKNOWN_LANGUAGE.equals(format.language)) {
                            // TvInputInfo expects {@code null} for unknown language.
                            builder.setLanguage(format.language);
                        }
                    } else if (trackType == DemoPlayer.TYPE_TEXT) {
                        if (format.language != null && !UNKNOWN_LANGUAGE.equals(format.language)) {
                            // TvInputInfo expects {@code null} for unknown language.
                            builder.setLanguage(format.language);
                        }
                    }

                    tracks.add(builder.build());
                }
            }
            //eric lin 20181102 live tv track,-end 
            return tracks;
        }

        @Override
        public void onPlayChannel(Channel channel) {
            super.onPlayChannel(channel);

            Log.d(TAG, "onPlayChannel: ");
            //eric lin 20181016 pesi player, mark
//            if (mHiPlayer == null) {
//                mHiPlayer = HiDtvMediaPlayer.getInstance();
//                mHiPlayer.AvControlOpen(0);
//            }

            try {
                // Edwin 20181205 service return unsigned int but java has signed int
                mPrimeChannelID = Long.parseLong(channel
                        .getInternalProviderData()
                        .get(KEY_CHANNEL_ID)
                        .toString());
            }
            catch ( InternalProviderData.ParseException e ) {
                e.printStackTrace();
            }
            mCurrentChannelName = channel.getDisplayName();//eric lin 20181017 pesi player record, add
            Log.d(TAG, "onPlayChannel: " + mPrimeChannelID+", chName="+mCurrentChannelName);

        }

        @Override
        public boolean onPlayProgram(Program program, long startPosMs) {
            Log.d(TAG, "onPlayProgram: program = "+program);

            // Edwin 20181016 don't check program
            //if (program == null) {
            //    requestEpgSync(getCurrentChannelUri());
            //    notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING);
            //    return false;
            //}

            // Edwin 20181016 sync Epg
            //EpgSyncService.cancelAllSyncRequests(PesiTvInputService.this);
            //EpgSyncService.requestImmediateSync(PesiTvInputService.this, mInputId,
            //        EpgSyncService.DEFAULT_EPG_DURATION,
            //        new ComponentName(PesiTvInputService.this, EpgSyncService.class));

            if(mPesiPlayer == null) {//eric lin 20181026 modify pesi player
                Log.d(TAG, "onPlayProgram: EKK new PesiPlayer()");
                mPesiPlayer = new PesiPlayer(mContext);
                mPesiPlayer.addListenerPrime(this);
                //mPesiPlayer.registerCallback(this);
                //mPesiPlayer.addListener(this);
                //mPesiPlayer.mDtv.AvControlOpen(0);//eric lin 20181204 adjust live tv, mark // Edwin 20181121 open AV once at setSurface()
            }

            // Edwin 20181121 disable Time Shift when usb is mounted -s
            if ( isUsbMounted() ) {
                notifyTimeShiftStatusChanged(TvInputManager.TIME_SHIFT_STATUS_UNSUPPORTED); // Edwin 20181112 disable TimeShift
                //notifyTimeShiftStatusChanged(TvInputManager.TIME_SHIFT_STATUS_AVAILABLE);
            } else {
                notifyTimeShiftStatusChanged(TvInputManager.TIME_SHIFT_STATUS_UNSUPPORTED);
            }
            // Edwin 20181114 disable record in tv_input_source.xml
            // Edwin 20181121 disable Time Shift when usb is mounted -e

            // Edwin 20181016 create player without program
            //createPlayer(program.getInternalProviderData().getVideoType(), Uri.parse(""));
            createPlayer(/*Util.TYPE_DASH*/0, Uri.parse("")); // edwin 20200429 for playing stream

            //if (startPosMs > 0) {
            //    mPesiPlayer.seekTo(startPosMs);
            //}

            //eric lin 20181016 pesi player,-start           
            //Log.d(TAG, "onPlayProgram: mChannelId="+mChannelId);
            Log.d(TAG, "onPlayProgram: channelId="+mPrimeChannelID);
            if(mPrimeChannelID != 0) {                
                mPesiPlayer.setPlayMode(MODE_TIMESHIFT);//eric lin 20181025 modify pesi player
                mPesiPlayer.setChannelId(mPrimeChannelID);
                mPesiPlayer.mDtv.AvControlPlayByChannelId(0, mPrimeChannelID, 0, 1);
                mPesiPlayer.setSession(mSession); // edwin 20181026 set session with notify function for player
                //test playback
                //mPesiPlayer.mDtv.AvControlPlayStop(0);
                //mPesiPlayer.mDtv.AvControlClose(0);
                //String filePath = "/storage/sda1/Records/24 ABC2003-Jul-09-22-47.ts";
                //int err = mPesiPlayer.mDtv.pvrPlayStart(filePath);
                //Log.d(TAG, "onPlayProgram: err="+err);
                //notifyVideoAvailable();

                //mChannelId = 0;
            }
            else{
                return false;
            }
            //eric lin 20181016 pesi player,-end
            mPesiPlayer.setPlayWhenReady(true);

            return true;
        }

        @Override
        public boolean onSetSurface(Surface surface) {
            Log.d(TAG, "onSetSurface: surface = "+surface + " mSurface = " + mSurface);
            if (mPesiPlayer == null) {
                Log.d(TAG, "onSetSurface: EKK new PesiPlayer()");
                mPesiPlayer = new PesiPlayer(mContext);
                mPesiPlayer.addListenerPrime(this); 
                //mHiPlayer = HiDtvMediaPlayer.getInstance();
                //mPesiPlayer.mDtv.AvControlClose(0);//eric lin test mark, it's johnny code
            }

            if (surface != null)
            {
                mSurface = surface;
                mPesiPlayer.mDtv.pipModSetDisplay(mContext,surface, 0);//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk  // jim 2019/09/05 change set surface timming to fix fast continually zapping cause video black
                // setSurface() in HiDtvMediaPLayer not working
                //mPesiPlayer.mDtv.pipModSetDisplay(surface);

            }else{//eric lin 20181204 adjust live tv, add
                if (mPesiPlayer != null && mSurface != null) {
                    Log.d(TAG, "onSetSurface: pip clear surface OK mSurface = " + mSurface);
                    mPesiPlayer.mDtv.pipModClearDisplay(mSurface);
                    mSurface = null ; // jim 2019/09/05 change set surface timming to fix fast continually zapping cause video black
                }
            }
            return super.onSetSurface(surface);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public boolean onPlayRecordedProgram(RecordedProgram recordedProgram) {
            Log.d(TAG, "onPlayRecordedProgram: ");
            createPlayer(/*Util.TYPE_DASH*/0, Uri.parse("")); // edwin 20200429 for playing stream //eric lin 20181026 modify pesi player, modify
                    //(recordedProgram.getInternalProviderData().getVideoType(),
                    //Uri.parse(recordedProgram.getInternalProviderData().getVideoUrl()));

            long recordingStartTime = recordedProgram.getInternalProviderData()
                    .getRecordedProgramStartTime();
            //playback--start
            //eric lin 20181026 modify pesi player,-start
            String filePath=null;
            long duration=0;
            try {
                filePath = recordedProgram
                        .getInternalProviderData()
                        .get(KEY_RECORD_PATH)
                        .toString();
                duration = recordedProgram.getRecordingDurationMillis();
            }
            catch ( InternalProviderData.ParseException e ) {
                e.printStackTrace();
            }

            mPesiPlayer.mDtv.AvControlPlayStop(0);
            //mPesiPlayer.mDtv.AvControlClose(0); // Edwin 20181114 fix silent video
            //String filePath = "/storage/sda1/Records/24 ABC2003-Jul-09-22-47.ts";

            if(duration != 0){
                Log.d(TAG, "onPlayRecordedProgram: duration="+duration);//eric lin test
                //mPesiPlayer.setPlayFileDuration(duration);
            }else{
                Log.d(TAG, "onPlayRecordedProgram: duration=0");//eric lin test
                return false;
            }
            mPesiPlayer.setPlayMode(MODE_PLAYBACK);
            mPesiPlayer.setRecordingDuration(recordedProgram.getRecordingDurationMillis());//eric lin 20181031 fix rewind/forward issue
            mPrimeChannelID=0;//eric lin 20181109 when play file to reset globe channel id
            int err = mPesiPlayer.mDtv.pvrPlayStart(filePath);
            Log.d(TAG, "onPlayRecordedProgram: EKK filePath="+filePath+", err="+err);
            //notifyVideoAvailable();

            //eric lin 20181026 modify pesi player,-end
            //playback--end

            Log.d(TAG, "onPlayRecordedProgram: recordingStartTime="+recordingStartTime+", recordedProgram.getStartTimeUtcMillis()="+recordedProgram.getStartTimeUtcMillis());

//            long seekTime=0;
//            if(recordedProgram.getStartTimeUtcMillis()<0)//eric lin test
//                seekTime=0;
//            else
//                seekTime=recordingStartTime - recordedProgram.getStartTimeUtcMillis();
            Log.d(TAG, "onPlayRecordedProgram: recordingStartTime="+recordingStartTime+", recordedProgram.getStartTimeUtcMillis()="+recordedProgram.getStartTimeUtcMillis());//+", seekTime="+seekTime);
            //mPesiPlayer.seekTo(recordingStartTime - recordedProgram.getStartTimeUtcMillis());//eric lin 20181030 mark no need
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notifyTimeShiftStatusChanged(TvInputManager.TIME_SHIFT_STATUS_AVAILABLE);
            }
            mPesiPlayer.setPlayWhenReady(true);
            return true;
        }

        public TvPlayer getTvPlayer() {
            //Log.d(TAG, "getTvPlayer: " + mPlayer);
            return mPesiPlayer;
        }

        @Override
        public boolean onTune(Uri channelUri) {
            Log.d(TAG, "onTune: ");
            notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING);
            //releasePlayer(); //eric lin 20181026 modify pesi player, mark
            //eric lin 20181016 pesi player, mark
//            if (mHiPlayer != null) {
//                mHiPlayer.AvControlPlayStop(0);
//            }
            //eric lin 20181016 pesi player, add
            if (mPesiPlayer != null) {
                //Log.d(TAG, "onTune: LKK playMode="+mPesiPlayer.getPlayMode());
                //eric lin 20181109 live tv timeshift stop,-start
                if(mEnablePVR == true) {
                int curPvrMode = mPesiPlayer.mDtv.pvrGetCurrentPvrMode(mPrimeChannelID);
                if(curPvrMode == PvrInfo.EnPVRMode.TIMESHIFT_LIVE ||  curPvrMode==PvrInfo.EnPVRMode.TIMESHIFT_FILE)
                    mPesiPlayer.stop(true); // Edwin 20181107 stop timeshift when change channel
                else
                    mPesiPlayer.stop(false);
                }
                //eric lin 20181109 live tv timeshift stop,-end
                mPesiPlayer.mDtv.AvControlOpen(0);//eric lin 20181204 adjust live tv, add
                mPesiPlayer.mDtv.AvControlPlayStop(0);
            }

            return super.onTune(channelUri);
        }

        @Override
        public void onPlayAdvertisement(Advertisement advertisement) {
            Log.d(TAG, "onPlayAdvertisement: ");
            createPlayer(TvContractUtils.SOURCE_TYPE_HTTP_PROGRESSIVE,
                    Uri.parse(advertisement.getRequestUrl()));
        }

        private void createPlayer(int videoType, Uri videoUrl) {
            Log.d(TAG, "createPlayer: ");
            releasePlayer();
            //eric lin 20181016 pesi player, mark
//            mPlayer = new DemoPlayer(RendererBuilderFactory.createRendererBuilder(
//                    mContext, videoType, videoUrl));
//            mPlayer.addListener(this);

            if(mPesiPlayer != null) {//eric lin 20181026 modify pesi player, add
                Log.d(TAG, "createPlayer: EKK addListener");
                mPesiPlayer.addListener(this);
            }else
                Log.d(TAG, "createPlayer: EKK addListener not success");
//            mPlayer.setCaptionListener(this);
//            mPlayer.prepare();
            //eric lin 20181016 pesi player, add
            //mPesiPlayer.addListener(this);//eric lin 20181026 modify pesi player, mark
        }

        @Override
        public void onSetCaptionEnabled(boolean enabled) {
            Log.d(TAG, "onSetCaptionEnabled: enabled = "+enabled);
            mCaptionEnabled = enabled;
            if (mPesiPlayer != null) {
                if (mCaptionEnabled) {
                        mPesiPlayer.setSelectedTrack(TvTrackInfo.TYPE_SUBTITLE,
                                mSelectedSubtitleTrackIndex);
                } else {
                    mPesiPlayer.setSelectedTrack(TvTrackInfo.TYPE_SUBTITLE, -1/*DemoPlayer.TRACK_DISABLED*/);
                }
            }
        }

        @Override
        public boolean onSelectTrack(int type, String trackId) {
            Log.d(TAG, "onSelectTrack: ");
            if (trackId == null) {
                return true;
            }

            int trackIndex = getIndexFromTrackId(trackId);
            //eric lin 20181016 pesi player, mark
            if (mPesiPlayer != null) {
                if (type == TvTrackInfo.TYPE_SUBTITLE) {
                    if (! mCaptionEnabled) {
                        return false;
                    }
                    mSelectedSubtitleTrackIndex = trackIndex;
                    Log.d(TAG, "onSelectTrack: YKK mSelectedSubtitleTrackIndex="+mSelectedSubtitleTrackIndex);
                }

                mPesiPlayer.setSelectedTrack(type, trackIndex);
                notifyTrackSelected(type, trackId);
                return true;
            }
            return false;
        }

        private void releasePlayer() {
            Log.d(TAG, "releasePlayer: ");
            //eric lin 20181016 pesi player, mark
//            if (mPlayer != null) {
//                mPlayer.removeListener(this);
//                mPlayer.setSurface(null);
//                mPlayer.stop();
//                mPlayer.release();
//                mPlayer = null;
//            }
            //eric lin 20181016 pesi player, add
            if(mPesiPlayer != null){
                mPesiPlayer.removeListener(this);
                mPesiPlayer.stop(true);
            }
        }

        public int getCurrentPvrMode(long channelId){
            int pvrMode=0;
            if(mPesiPlayer != null) {
                pvrMode = mPesiPlayer.mDtv.pvrGetCurrentPvrMode(channelId);
            }
            return pvrMode;
        }

        public boolean CheckPvrMode(int pvrMode) //Scoty 20180827 add and modify TimeShift Live Mode
        {
            if(pvrMode==PvrInfo.EnPVRMode.TIMESHIFT_LIVE || pvrMode==PvrInfo.EnPVRMode.TIMESHIFT_FILE
                    || pvrMode==PvrInfo.EnPVRMode.PVRMODE_TIMESHIFT_LIVE_PAUSE)
                return true;
            else
                return false;
        }

        // Edwin 20181123 move thread to onRelease() -s
        //eric lin 20181109 modify on release flow,-start
//        private Runnable mutiThread = new Runnable() {
//            public void run() {
//            //eric lin 20181026 modify pesi player,-start
//
//            if(mPesiPlayer != null) {//eric lin 20181102 avoid crash
//                mPesiPlayer.mDtv.pipModClearDisplay(mSurface);
//                int curPvrMode = -1;
//                curPvrMode = mPesiPlayer.mDtv.pvrGetCurrentPvrMode(mPrimeChannelID);
//                if ( curPvrMode == PvrInfo.EnPVRMode.PLAY_RECORD_FILE )
//                {
//                    Log.d(TAG, "onRelease: EKK pvrPlayStop()");
//                    mPesiPlayer.mDtv.pvrPlayStop();//eric lin test
//                }
//
//                //Log.d(TAG, "onRelease: session subtitle visiable=" + mPesiPlayer.mDtv.AvControlIsSubtitleVisible(0 /*ViewHistory.getPlayId()*/));//eric lin test
//                //if (mPesiPlayer.mDtv.AvControlIsSubtitleVisible(0 /*ViewHistory.getPlayId()*/) == true) {
//                //    Log.d(TAG, "CloseCurSubtitle !!!!!!!");
//                //    mPesiPlayer.mDtv.AvControlShowSubtitle(0 /*ViewHistory.getPlayId()*/, false);
//                //}
//
//                //if (mPesiPlayer.mDtv.AvControlGetCurrentSubtitle(0) != null) {
//                //    Log.d(TAG, "onRelease: session get current subtittle != null, and set false to show subtitle");
//                //    mPesiPlayer.mDtv.AvControlShowSubtitle(0 /*ViewHistory.getPlayId()*/, false);
//                //}
//
//                //Log.d(TAG, "onRelease: LKK playMode="+mPesiPlayer.getPlayMode()+", curPvrMode="+curPvrMode+", mPrimeChannelID="+mPrimeChannelID);
//                if(curPvrMode == PvrInfo.EnPVRMode.TIMESHIFT_LIVE ||  curPvrMode==PvrInfo.EnPVRMode.TIMESHIFT_FILE)
//                    mPesiPlayer.stop(true); // Edwin 20181030 stop timeshift when app exit
//                else
//                    mPesiPlayer.stop(false);
//
//                if(mPesiPlayer.mDtv.GetOpenViewActivity() == false)//eric lin 20181119 add view activity flag
//                    mPesiPlayer.mDtv.AvControlPlayStop(0);
//                //mPesiPlayer.mDtv.AvControlClose(0);//eric lin 20181115 test for no video signal
//
//                mPesiPlayer.setPlayMode(0);
//                //mPesiPlayer.removeListenerPrime(getThis()/*this*/);//eric lin test, add
//                mPesiPlayer = null;//eric lin 20181107 record A watch B issue, add
//            }
//
//            //eric lin 20181026 modify pesi player,-end
//            }
//        };
        //eric lin 20181109 modify on release flow,-end
        // Edwin 20181123 move thread to onRelease() -e

        private Listener getThis(){//eric lin 20181109 modify on release flow
            return this;
        }

        @Override
        public void onRelease() {
            Log.d(TAG, "onRelease:  mSession = " + mSession + " mPesiPlayer = " + mPesiPlayer );
            super.onRelease();
           //releasePlayer();

            // Edwin 20181123 to simplify mutiThread's run -s
            //onSetCaptionEnabled(false);//eric lin 20181204 adjust live tv, mark
            if(mPesiPlayer != null) {
                if (mPesiPlayer.mDtv.AvControlIsSubtitleVisible(0 /*ViewHistory.getPlayId()*/) == true) {//if (mPesiPlayer.mDtv.AvControlGetCurrentSubtitle(0) != null) {//eric lin test
                    Log.d(TAG, "onRelease: session get current subtittle != null, and set false to show subtitle");
                    mPesiPlayer.mDtv.AvControlShowSubtitle(0 /*ViewHistory.getPlayId()*/, false);
                }
                //if(isDtvPlayerOnTop()==false) // jim 2019/09/10 fix multi app call tvinputservice and  no video when av close
                {
                    if(mEnablePVR==true) {
                        int curPvrMode = mPesiPlayer.mDtv.pvrGetCurrentPvrMode(mPrimeChannelID);

                        if (curPvrMode == PvrInfo.EnPVRMode.PVRMODE_TIMESHIFT_LIVE_PAUSE
                                || curPvrMode == PvrInfo.EnPVRMode.TIMESHIFT_FILE
                                || curPvrMode == PvrInfo.EnPVRMode.TIMESHIFT_LIVE) {
                             mPesiPlayer.stop(true); // Edwin 20181030 stop timeshift when app exit
                        } else if (PvrInfo.EnPVRMode.PLAY_RECORD_FILE == curPvrMode) {
                            mPesiPlayer.mDtv.pvrPlayStop();
                            mPesiPlayer.stop(false);
                        }
                    }
                    //mPesiPlayer.mDtv.AvControlPlayStop(0); // jim 2019/09/10 fix multi app call tvinputservice and  no video when av close
                    //mPesiPlayer.mDtv.AvControlClose(0);//eric lin 20181204 adjust live tv // jim 2019/09/10 fix multi app call tvinputservice and  no video when av close
                    // Edwin 20181123 to simplify mutiThread's run -e
                }
                mPesiPlayer.setPlayMode(0);
                mPesiPlayer.removeListenerPrime(getThis()); // Edwin 20181121 fix unregister broacast delay cause exception
            }
            mSyncEpgThread.interrupt(); // Edwin 20181113 sync EPG every 1 hour
            mPesiPlayer = null;//eric lin 20181107 record A watch B issue, add
        }

        @Override
        public void onBlockContent(TvContentRating rating) {
            Log.d(TAG, "onBlockContent: ");
            super.onBlockContent(rating);
            releasePlayer();

            //eric lin 20181016 pesi player, mark
//            if (mHiPlayer != null) {
//                mHiPlayer.AvControlPlayStop(0);
//            }
            //eric lin 20181016 pesi player, add
            if (mPesiPlayer != null) {
                mPesiPlayer.mDtv.AvControlPlayStop(0);
            }
        }

        private float getCaptionFontSize() {
            Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay();
            Point displaySize = new Point();
            display.getSize(displaySize);
            return Math.max(getResources().getDimension(R.dimen.TEXT_SIZE),
                    CAPTION_LINE_HEIGHT_RATIO * Math.min(displaySize.x, displaySize.y));
        }

        @Override
        public void onStateChanged(boolean playWhenReady, int playbackState) {
            Log.d(TAG, "onStateChanged: " + mPesiPlayer+
                    " playWhenReady = "+playWhenReady+
                    " playbackState = "+playbackState);
            if (mPesiPlayer == null) {
                return;
            }

            //eric lin 20181016 pesi player, add
            if(playbackState == 0)
                notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_UNKNOWN);//VIDEO_UNAVAILABLE_REASON_BUFFERING);
            else {
                if(playWhenReady == true && playbackState == 1) {
                    notifyVideoAvailable();
                }
            }
            // notifyVideoAvailable anyway
            //notifyVideoAvailable();//eric lin 20181016 pesi player, mark

            // exoPlayer here will not be ready because no file or stream
            //eric lin 20181102 live tv track,-start
            if (playWhenReady && playbackState==1){//playbackState == ExoPlayer.STATE_READY) {
                notifyTracksChanged(getAllTracks());
                String audioId = getTrackId(TvTrackInfo.TYPE_AUDIO,
                        mPesiPlayer.getSelectedTrack(TvTrackInfo.TYPE_AUDIO));
                //String videoId = getTrackId(TvTrackInfo.TYPE_VIDEO,
                //        mPesiPlayer.getSelectedTrack(TvTrackInfo.TYPE_VIDEO));
                Log.d(TAG, "onStateChanged: YKK type=text getSelectedTrack="+mPesiPlayer.getSelectedTrack(TvTrackInfo.TYPE_SUBTITLE)
                        +", mCaptionEnabled="+mCaptionEnabled+", mSelectedSubtitleTrackIndex="+mSelectedSubtitleTrackIndex);


                if(mPesiPlayer.getSelectedTrack(TvTrackInfo.TYPE_SUBTITLE) == 0){//eric lin test
                    Log.d(TAG, "onStateChanged: YKK 123 set false");
                    //mCaptionEnabled = false;
                    //mSelectedSubtitleTrackIndex = 0;
                }
                String textId = getTrackId(TvTrackInfo.TYPE_SUBTITLE,
                        mPesiPlayer.getSelectedTrack(TvTrackInfo.TYPE_SUBTITLE));

                notifyTrackSelected(TvTrackInfo.TYPE_AUDIO, audioId);
                //notifyTrackSelected(TvTrackInfo.TYPE_VIDEO, videoId);
                notifyTrackSelected(TvTrackInfo.TYPE_SUBTITLE, textId);
                notifyVideoAvailable();
            }
            //eric lin 20181102 live tv track,-end 
//            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
//                    Math.abs(mPlayer.getPlaybackSpeed() - 1) < 0.1 &&
//                    playWhenReady && playbackState == ExoPlayer.STATE_BUFFERING) {
//                notifyVideoUnavailable(TvInputManager.VIDEO_UNAVAILABLE_REASON_BUFFERING);
//            }
        }

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                       float pixelWidthHeightRatio) {
            // Do nothing.
            Log.d(TAG, "onVideoSizeChanged: width = "+width);
            Log.d(TAG, "onVideoSizeChanged: height = "+height);
            Log.d(TAG, "onVideoSizeChanged: unappliedRotationDegrees = "+unappliedRotationDegrees);
            Log.d(TAG, "onVideoSizeChanged: pixelWidthHeightRatio = "+pixelWidthHeightRatio);
        }

        @Override
        public void onError(Exception e) {
            Log.e(TAG, e.getMessage());
        }

        //eric lin 20181016 pesi player, mark
//        @Override
//        public void onCues(List<Cue> cues) {
//            Log.d(TAG, "onCues: ");
//            mSubtitleView.setCues(cues);
//        }

        public void requestEpgSync(final Uri channelUri) {
            Log.d(TAG, "requestEpgSync: ");
            EpgSyncService.requestImmediateSync(PesiTvInputService.this, mInputId,
                    new ComponentName(PesiTvInputService.this, EpgSyncService.class));
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    onTune(channelUri);
                }
            }, EPG_SYNC_DELAYED_PERIOD_MS);
        }

        @Override
        public void onTimeShiftResume ()
        {
            Log.d(TAG, "onTimeShiftResume: ");
            //super.onTimeShiftResume();
            if(mPesiPlayer != null){
                int playMode = mPesiPlayer.getPlayMode();
                if(playMode == MODE_PLAYBACK) {//playback
                    Log.d(TAG, "onTimeShiftResume: PesiPlayer MODE_PLAYBACK");
                    super.onTimeShiftResume();
                }else if(playMode == MODE_TIMESHIFT) {
                    mPesiPlayer.play();
                }
            }
            // Resume and make sure media is playing at regular speed.
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //    PlaybackParams normalParams = new PlaybackParams();
            //    normalParams.setSpeed(1);
            //    onTimeShiftSetPlaybackParams(normalParams);
            //}
        }

        @Override
        public void onTimeShiftPause ()
        {
            Log.d(TAG, "onTimeShiftPause: ");
            //super.onTimeShiftPause();
            if ( mPesiPlayer != null ) {
                int playMode = mPesiPlayer.getPlayMode();
                if(playMode == MODE_PLAYBACK) {//playback
                    Log.d(TAG, "onTimeShiftPause: PesiPlayer MODE_PLAYBACK");
                    super.onTimeShiftPause();
                }else if(playMode == MODE_TIMESHIFT) {
                    mPesiPlayer.pause();
                }
            }
        }

        @Override
        public void onTimeShiftSeekTo ( long timeMs )
        {
            Log.d(TAG, "onTimeShiftSeekTo: ");
            //super.onTimeShiftSeekTo(timeMs);
            mPesiPlayer.seekTo(timeMs);
        }

        @Override
        public void onTimeShiftSetPlaybackParams ( PlaybackParams params )
        {
            Log.d(TAG, "onTimeShiftSetPlaybackParams: "+params.getSpeed());
            //super.onTimeShiftSetPlaybackParams(params);

            if (mPesiPlayer != null) {
                int playMode = mPesiPlayer.getPlayMode();
                if(playMode == MODE_PLAYBACK) {//playback
                    Log.d(TAG, "onTimeShiftSetPlaybackParams: PesiPlayer MODE_PLAYBACK");
                    super.onTimeShiftSetPlaybackParams(params);
                }else if(playMode == MODE_TIMESHIFT) {
                    mPesiPlayer.setPlaybackParams(params);
                }
            }
        }

        @Override
        public long onTimeShiftGetCurrentPosition ()
        {
            if (mPesiPlayer != null)
            {
                int playMode = mPesiPlayer.getPlayMode();
                if(playMode == MODE_PLAYBACK) {//playback
                    Log.d(TAG, "onTimeShiftGetCurrentPosition: PesiPlayer MODE_PLAYBACK");
                    //super.onTimeShiftGetCurrentPosition();
                    return mPesiPlayer.getCurrentPosition();
                }else if(playMode == MODE_TIMESHIFT) {//Timeshift
                    //long currentPos = super.onTimeShiftGetCurrentPosition();
                    long currentPos = mPesiPlayer.getCurrentPosition();
                    //Log.d(TAG, "onTimeShiftGetCurrentPosition: currentPos = "+currentPos);
                    return currentPos;
                }
                else if ( playMode == MDOE_RECORD )
                {
                    Log.d(TAG, "onTimeShiftGetStartPosition: PesiPlayer MDOE_RECORD");
                    return System.currentTimeMillis();
                }
            }
            return TvInputManager.TIME_SHIFT_INVALID_TIME;
        }

        @Override
        public long onTimeShiftGetStartPosition ()
        {
            if ( mPesiPlayer != null )
            {
                int playMode = mPesiPlayer.getPlayMode();
                if(playMode == MODE_PLAYBACK) {//playback
                    Log.d(TAG, "onTimeShiftGetStartPosition: PesiPlayer MODE_PLAYBACK");
                    return 0;//super.onTimeShiftGetStartPosition();
                }else if(playMode == MODE_TIMESHIFT) {//Timeshift
                    //long startPos = super.onTimeShiftGetStartPosition();
                    long startPos = mPesiPlayer.getStartPosition();
                    //Log.d(TAG, "onTimeShiftGetStartPosition: startPos = " + startPos);
                    return startPos;
                }
                else if ( playMode == MDOE_RECORD )
                {
                    Log.d(TAG, "onTimeShiftGetStartPosition: PesiPlayer MDOE_RECORD");
                    return System.currentTimeMillis();
                }
            }
            return TvInputManager.TIME_SHIFT_INVALID_TIME;
        }

        @Override
        public void onOverlayViewSizeChanged ( int width, int height )
        {
            int resWidth = getResources().getDisplayMetrics().widthPixels;
            int resHeight = getResources().getDisplayMetrics().heightPixels;
            Log.d(TAG, "onOverlayViewSizeChanged: width = "+width+" height = "+height);
            Log.d(TAG, "onOverlayViewSizeChanged: resWidth = "+resWidth+" resHeight = "+resHeight);

            // Edwin 20181123 to disable subtitle when enable pip
            if ( width < resWidth && height < resHeight )
                onSetCaptionEnabled(false);
            else
                onSetCaptionEnabled(true);
            super.onOverlayViewSizeChanged(width, height);
        }

        @Override
        public boolean handleMessage ( Message msg ) {
            Log.d(TAG, "handleMessage: "+msg.what);
            if ( msg.what == 1001 ) // Edwin 20181024 remove handle BaseTvInputService.MSG_PLAY_AD
                return false;
            return super.handleMessage(msg);
        }

        private boolean isUsbMounted ()
        {
            StorageManager storageManager = (StorageManager)getSystemService(Context.STORAGE_SERVICE);
            PesiStorageHelper pesiStorageHelper = new PesiStorageHelper(storageManager);
            for (Object volumeInfo : PesiPlayer.getVolumes()) {
                String devType = pesiStorageHelper.getDevType(volumeInfo);
                if ( devType == null )
                    continue;
                if ( devType.equals("USB2.0") || devType.equals("USB3.0") ) {
                    Log.d(TAG, "isUsbMounted: volumeInfo.path = "+pesiStorageHelper.getPath(volumeInfo));
                    return true;
                }
            }
            return false;
        }

    } // Session

    @RequiresApi(api = Build.VERSION_CODES.N)
    private class PesiRecordingSession extends BaseTvInputService.RecordingSession {
        private static final String TAG = "RecordingSession";
        private String mInputId;
        private long mStartTimeMs;
        //private PesiPlayer mPesiPlayer=null;//eric lin 20181017 pesi player record, add
        private int DEFAULT_MIN_REC_SPACE = 100*1024*1024;//eric lin 20181017 pesi player record, add
        private int mRecordDuration=60*60;//eric lin 20181017 pesi player record, add
        private int mRecID;//eric lin 20181017 pesi player record, add
        private String mRecPath;//eric lin 20181026 modify pesi player
        private PesiPlayer mPesiPlayer=null;//eric lin 20181107 record A watch B issue

        public PesiRecordingSession(Context context, String inputId) {
            super(context, inputId);
            mInputId = inputId;
            Log.d(TAG, "PesiRecordingSession: EKK inputId="+inputId);
            if(mPesiPlayer == null) {//eric lin 20181017 pesi player record, add
                mPesiPlayer = new PesiPlayer(mContext, 1);//eric lin 20181107 record A watch B issue
            }
        }

        @Override
        public void onTune(Uri uri) {
            Log.d(TAG, "onTune: EKK uri="+uri);
            super.onTune(uri);
            // By default, the number of tuners for this service is one. When a channel is being
            // recorded, no other channel from this TvInputService will be accessible. Developers
            // should call notifyError(TvInputManager.RECORDING_ERROR_RESOURCE_BUSY) to alert
            // the framework that this recording cannot be completed.
            // Developers can update the tuner count in xml/richtvinputservice or programmatically
            // by adding it to TvInputInfo.updateTvInputInfo.
            notifyTuned(uri);
        }

        private Runnable mutiThread = new Runnable() {//eric lin 20181026 modify pesi player
            public void run() {
                int result = startRecord();
                if(result == -1) {//eric lin 20181112 record fail to notify error
                    Log.d(TAG, "run: startRecord(), result = -1");
                    notifyError(TvInputManager.RECORDING_ERROR_UNKNOWN);
                }else if(result == -2) {//eric lin 20181112 record fail to notify error
                    Log.d(TAG, "run: startRecord(), result = -2");
                    notifyError(TvInputManager.RECORDING_ERROR_INSUFFICIENT_SPACE);
                }else if(result == -3) {//eric lin 20181112 record fail to notify error
                    Log.d(TAG, "run: startRecord(), result = -3");
                    notifyError(TvInputManager.RECORDING_ERROR_RESOURCE_BUSY);
                }else if(result == 0){//eric lin 20181116 live tv record disk full
                    mPesiPlayer.setRecordingSession(mRecordingSession);
                    Log.d(TAG, "run: startRecord(), result = 0"+", OKK");
                }
            }
        };

        @Override
        public void onStartRecording(final Uri uri) {
            super.onStartRecording(uri);
            mStartTimeMs = System.currentTimeMillis();
            
            //startRecord();//eric lin 20181017 pesi player record, add
            mPesiPlayer.setPlayMode(MDOE_RECORD);//eric lin 20181026 modify pesi player
            Thread thread = new Thread(mutiThread);//eric lin 20181026 modify pesi player
            thread.start();//eric lin 20181026 modify pesi player
            //int RecID = mPesiPlayer.mDtv.pvrRecordStart(0, mPrimeChannelID, "/storage/sda1/Records/test06.ts", 60);
            Log.d(TAG, "onStartRecording: EKK uri="+uri+", mStartTimeMs="+mStartTimeMs+", mRecID="+mRecID);
        }

        @Override
        public void onStopRecording(Program programToRecord) {
            long duration=0;//eric lin 20181026 modify LiveChannel record start/end/duration

            mPesiPlayer.setPlayMode(MODE_TIMESHIFT); // Edwin 20181029 back to TimeShift
            //int recId = mPesiPlayer.mDtv.pvrRecordCheck(mPrimeChannelID);
            int err=-1;
            if(mRecID != -1) {//eric lin 20181017 pesi player record, add
                err = mPesiPlayer.mDtv.pvrRecordStop(0, mRecID);
            }
            Log.d(TAG, "onStopRecording: EKK err="+err);
            mRecID = -1;


            Log.d(TAG, "onStopRecording: mRecPath="+mRecPath);
            PvrFileInfo fileInfo = mPesiPlayer.mDtv.pvrFileGetAllInfo(mRecPath);
            if(fileInfo != null){
                Log.d(TAG, "onStopRecording: fileInfo.durationInMs="+fileInfo.durationInMs);
                if(fileInfo.durationInMs==0){//eric lin 20181029 record fail to notify error
                    Log.d(TAG, "onStopRecording: fileInfo.durationInMs=0");
                    notifyError(TvInputManager.RECORDING_ERROR_UNKNOWN);
                    return;
                }
                duration = ((fileInfo.durationInMs-1000)/1000)*1000;//(fileInfo.durationInMs/1000)*1000; //eric lin -1000 to fix when playback end, get play time not equal duration

                Log.d(TAG, "onStopRecording: duration="+duration);
            }else{
                Log.d(TAG, "onStopRecording: fileInfo=null");
                notifyError(TvInputManager.RECORDING_ERROR_UNKNOWN);//eric lin 20181029 record fail to notify error
                return;
            }
            // In this sample app, since all of the content is VOD, the video URL is stored.
            // If the video was live, the start and stop times should be noted using
            // RecordedProgram.Builder.setStartTimeUtcMillis and .setEndTimeUtcMillis.
            // The recordingstart time will be saved in the InternalProviderData.
            // Additionally, the stream should be recorded and saved as
            // a new file.

            Program p = new Program.Builder()
                    .setChannelId(/*channelId*/programToRecord.getChannelId())
                    .setTitle(/*event.getEventName()*/programToRecord.getTitle())
                    .setDescription(/*description*/programToRecord.getDescription())
                    .setStartTimeUtcMillis(0)//eric lin 20181026 modify LiveChannel record start/end/duration
                    .setEndTimeUtcMillis(fileInfo.durationInMs/*duration*/)//eric lin 20181026 modify LiveChannel record start/end/duration
                    .setCanonicalGenres(/*new String[]{TvContract.Programs.Genres.MOVIES}*/programToRecord.getCanonicalGenres())
                    .setPosterArtUri(/*TEARS_OF_STEEL_ART*/programToRecord.getPosterArtUri())
                    .setThumbnailUri(/*TEARS_OF_STEEL_ART*/programToRecord.getThumbnailUri())
                    .setInternalProviderData(/*internalProviderData*/programToRecord.getInternalProviderData())
                    .build();

            long currentTime = System.currentTimeMillis();
            InternalProviderData internalProviderData = p.getInternalProviderData();//programToRecord.getInternalProviderData();//eric lin 20181026 modify LiveChannel record start/end/duration
            internalProviderData.setRecordingStartTime(0/*mStartTimeMs*/);//eric lin 20181026 modify LiveChannel record start/end/duration
            try {//eric lin 20181026 modify pesi player
                internalProviderData.put(KEY_RECORD_PATH, mRecPath);
            }
            catch ( InternalProviderData.ParseException e )
            {
                e.printStackTrace();
            }
            //Log.d(TAG, "onStopRecording: EKK mRecPath="+mRecPath);//eric lin test
            RecordedProgram recordedProgram = new RecordedProgram.Builder(p/*programToRecord*/)
                    .setInputId(mInputId)
                    .setRecordingDataUri(
                            programToRecord.getInternalProviderData().getVideoUrl())
                    .setRecordingDurationMillis(/*fileInfo.durationInMs*/duration/*currentTime - mStartTimeMs*/)//eric lin 20181026 modify LiveChannel record start/end/duration
                    .setInternalProviderData(internalProviderData)
                    .build();
            notifyRecordingStopped(recordedProgram);
            Log.d(TAG, "onStopRecording: EKK currentTime="+currentTime+", duration="+(currentTime - mStartTimeMs)+", mRecID="+mRecID);
        }

        @Override
        public void onStopRecordingChannel(Channel channelToRecord) {
            Log.d(TAG, "onStopRecordingChannel: EKK");
            
            //int recId = mPesiPlayer.mDtv.pvrRecordCheck(mPrimeChannelID);
            if(mRecID != -1)//eric lin 20181017 pesi player record, add
                mPesiPlayer.mDtv.pvrRecordStop(0, mRecID);
            mRecID = -1;

            // Program sources in this sample always include program info, so execution here
            // indicates an error.
            notifyError(TvInputManager.RECORDING_ERROR_UNKNOWN);
            return;
        }

        @Override
        public void onRelease() {
            Log.d(TAG, "onRelease: EKK");
            mPesiPlayer.setPlayMode(MODE_TIMESHIFT); // Edwin 20181029 change to timeShift mode; //eric lin 20181026 modify pesi player
            //mPesiPlayer.mDtv.AvControlPlayByChannelId(0, mPrimeChannelID, 0, 1);//eric lin test
        }

        //eric lin 20181017 pesi player record,-start
        public String GetRecordPath()
        {
            if(mPesiPlayer != null)
                return mPesiPlayer.mDtv.GetRecordPath();
            else
                return null;
        }
        public void SetRecordPath(String path)
        {
            if(mPesiPlayer != null)
                mPesiPlayer.mDtv.setRecordPath(path);
        }
        public String getDefaultRecPath()
        {
            if(mPesiPlayer != null)
                return mPesiPlayer.mDtv.getDefaultRecPath();
            else
                return null;
        }
        public List<PvrInfo> PvrRecordGetAllInfo()
        {
            if(mPesiPlayer != null)
                return mPesiPlayer.mDtv.pvrRecordGetAllInfo();
            else
                return null;
        }
        public int PvrRecordGetMaxRecNum()
        {
            if(mPesiPlayer != null)
                return mPesiPlayer.mDtv.pvrRecordGetMaxRecNum();
            else
                return 0;
        }
        public Date getLocalTime()
        {
            if(mPesiPlayer != null)
                return mPesiPlayer.mDtv.getDtvDate();
            else
                return null;
        }
        public Date GetLocalTime() {
            Date Time= getLocalTime();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if(Time == null)
            {
                Log.d(TAG, "GetLocalTime:  TIME = NULL !!!!!!!!!");
                Time = new Date();
            }
            String str = format.format(Time.getTime());
            return Time;
        }
        public int PvrRecordStart(int pvrPlayerID , long channelID, String recordPath, int duration)
        {
            if(mPesiPlayer != null)
                return mPesiPlayer.mDtv.pvrRecordStart(pvrPlayerID, channelID, recordPath, duration);
            else
                return -1;
        }
        public int startRecord()
        {
            int ret = 0;
            long PvrFreeSize=0;
            String RecMountPath = GetRecordPath();
            String RecPath, RecFileName;
            SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MMM-dd-HH-mm");
            boolean updateChannelFlag = false;//Scoty 20180809 modify dual pvr rule

            if(!CheckUsbPathAvailable(RecMountPath)){//eric lin 20181121 live tv not use sdcard record path
                Log.d(TAG, "startRecord: path is not usb record path, RecMountPath="+RecMountPath);
                return -1;
            }

            File file = new File(RecMountPath);
            if (!file.exists())
            {
                Log.d(TAG, "startRecord:  Record Path Not Exit ! path = " + GetRecordPath() );
                SetRecordPath(getDefaultRecPath()); // connie 20180525 get path by getDefaultRecPath()
                file = new File(getDefaultRecPath());
                if (!file.exists()) {
                    Log.d(TAG, "startRecord:  Record Path Not Exit ! path = " + GetRecordPath() );
                    return -1;
                }
            }

            PvrFreeSize = file.getUsableSpace();
            Log.d(TAG, "startRecord:  RecPath = " + RecMountPath + "       PvrFreeSize =" + PvrFreeSize);
            if(PvrFreeSize <= DEFAULT_MIN_REC_SPACE)//100 MB
            {
                Log.d(TAG, "startRecord: There is no Free Space to Record !  PvrFreeSize = " + PvrFreeSize);
                return -2;
            }

            //Scoty 20180716 add max record num -s
            List<PvrInfo> pvrList= new ArrayList<PvrInfo>();
            pvrList = PvrRecordGetAllInfo();
            Log.d(TAG, "startRecord: record_num = " + pvrList.size() + " max Record Num = " + PvrRecordGetMaxRecNum());
            if(pvrList.size() >= PvrRecordGetMaxRecNum())//check max record num
            {
                Log.d(TAG, "startRecord: reach or bigger than max record num !");
                mPesiPlayer.mDtv.pvrRecordStop(0, pvrList.get(0).getRecId());
                return -3;
            }
            else if((pvrList.size()+1) == (PvrRecordGetMaxRecNum()))//Scoty 20180809 modify dual pvr rule
            {
                updateChannelFlag = true;
            }
            //Scoty 20180716 add max record num -e

            Date beginTime = GetLocalTime();
            if (beginTime == null)
            {
                Log.d(TAG, "startRecord:  Can't get current time !");
            }

            RecPath = RecMountPath + "/Records" ;
            File dirFile = new File(RecPath);
            if(!dirFile.exists()) {
                Log.d(TAG, "startRecord:  Add Dir ==> RecPath =" +RecPath);
                dirFile.mkdir();
            }

            String recordName = mCurrentChannelName; //ViewHistory.getCurChannel().getChannelName();
            recordName = recordName.replace('/', '_');//Scoty 20180529 fixed rec name replace '/' to '_'
            RecFileName = recordName + formatter.format(beginTime.getTime())+".ts";
            RecPath = RecPath + "/" + RecFileName;

            Log.d(TAG, "startRecord:  Rec Full Path =" + RecPath);
            //int startRecRet = PvrRecordStart( ViewHistory.getPlayId() , ViewHistory.getCurChannel().getChannelId(), RecPath, RecordDuration);
            int startRecRet = 0;
            mRecPath = RecPath;//eric lin 20181026 modify pesi player
            Log.d(TAG, "startRecord: mRecPath="+mRecPath);
            mRecID = PvrRecordStart( 0 , mPrimeChannelID, RecPath, mRecordDuration);//Scoty 20180809 modify dual pvr rule//eric lin 20180712 pesi pvr for one rec
            Log.d(TAG, "startRecord: RecPath="+RecPath+", RecordDuration="+mRecordDuration+", mPrimeChannelID="+mPrimeChannelID+", mRecID="+mRecID);//eric lin test
            if (-1 != mRecID)//if (0 == startRecRet)//eric lin 20180712 pesi pvr for one rec
            {
//                new MessageDialogView(mContext,getString(R.string.STR_START_RECORDING),3000)
//                {
//                    public void dialogEnd() {
//                        ShowBanner(1);
//                    }
//                }.show();
            }
            else
            {
                return -1;
            }

//            //Scoty 20180615 move set pvrSkip channel here for timer not work -s
//            ProgramInfo Program = ProgramInfoGetByChannelId(ViewHistory.getCurChannel().getChannelId());
//            if(updateChannelFlag) {//Scoty 20180809 modify dual pvr rule
//                pvrList = PvrRecordGetAllInfo();
//                UpdatePvrSkipList(ViewHistory.getCurGroupType(), 1, Program.getTpId(), pvrList);
//            }
            //List<SimpleChannel> simpleChannelList =ProgramInfoGetPlaySimpleChannelList(ViewHistory.getCurGroupType(), 1);
            //Scoty 20180615 move set pvrSkip channel here for timer not work -e
            //mRecImageView = new RecImageView(ViewActivity.this);//Scoty 20180629 show Rec Icon on top
//            if(GposInfoGet().getRecordIconOnOff() == 1)//Scoty 20180806 check record Icon show or not by gpos
//                mRecImageView.SetVisibility(true);

            return 0;
        }
        //eric lin 20181017 pesi player record,-end

        private boolean CheckUsbPathAvailable(String path) // connie 20181024 for USB Path wrong
        {
            String defaultPath = mPesiPlayer.mDtv.getDefaultRecPath();
            Log.d(TAG, "CheckUsbPathAvailable:  defaultPath = " + defaultPath);
            Log.d(TAG, "CheckUsbPathAvailable: path ="+ path);
            if(defaultPath.equals(path))
                return false;
            else
                return true;
        }
    }

    private class DemoPlayer
    {
        private final static int TYPE_AUDIO = 0;
        private final static int TYPE_VIDEO = 1;
        private final static int TYPE_TEXT = 2;
    }
}
