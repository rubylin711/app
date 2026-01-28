package com.mtest.activity;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.storage.StorageManager;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dolphin.dtv.EnTableType;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory;
import com.google.android.exoplayer2.extractor.ts.TsExtractor;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.UdpDataSource;
import com.google.android.exoplayer2.util.TimestampAdjuster;
import com.google.android.exoplayer2.util.Util;
import com.mtest.config.MtestConfig;
import com.mtest.module.PesiSharedPreference;
import com.mtest.module.TunerModule;
import com.mtest.utils.PesiStorageHelper;
import com.mtest.utils.TunerView;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.utils.TVMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.mtest.activity.MainActivity.mExoPlayer;
import static com.mtest.activity.MainActivity.mMediaSource;
import static com.mtest.activity.MainActivity.mExoPlayerIsPlaying;

public class NTSCPAL_INFOActivity extends DTVActivity {
    public static final String TAG = "NTSCPAL_INFOActivity";

    private TextView mTvIpTvIdx;
    private EditText mEtIpTVSrc;
    private TextView mTvSignalStregth;
    private TextView mTvSignalQuality;
//    private TextView mTvSignalBER;
    private TextView mTvErrorStatus;
    private ProgressBar mProgbarStrength;
    private ProgressBar mProgbarQuality;
    private ConstraintLayout mMainLayout;

    private List<TunerView> mTunerViewList;
    private Global_Variables mGlobalVars;

    private boolean mIsSearching = false;
    private boolean mIsRunningStableTest = false;
    private int mCurTunerIdx;
    private int mCurStableTestTp = 0;
    private List<ProgramManagerImpl> ProgramManagerList = null;

    private int[][] mStableTestTpsDVBC = {  // {freq, symbol, qam}, qam: 0 = 16, 1 = 32, ..., 4 = 256
            {402 ,6875, 4},
            {482 ,6875, 4},
            {666 ,6875, 4},
            {858 ,6875, 4},
    };
    private int[][] mStableTestTpsISDBT = {  // {freq, band}, band: 0 = 6M, 1 = 7M, 2 = 8M
            {485143, 0},
            {491143, 0},
            {635143, 0},
    };
    private int[][] mStableTestTpsDVBS = {  // {freq, symbol}
            {1000, 27500},
            {1100, 27500},
            {1200, 27500},
    };

    private TunerModule mTuner = new TunerModule(this);
    private final Handler mHandler = new Handler();
    private final Runnable mCheckStatusRunnable = new Runnable() {
        @Override
        public void run() {
            if (mGlobalVars.m_GetIPTVActive()) {
                if (mExoPlayer != null && mExoPlayerIsPlaying) { // edwin 20200429 for playing stream
                    mTvIpTvIdx.setBackgroundResource(R.drawable.shape_rectangle_pass);
                } else {
                    mTvIpTvIdx.setBackgroundResource(R.drawable.shape_rectangle_fail);
                }
            } else if (!mIsSearching) {    // not do until scan complete
                if (mTuner.isLock(mCurTunerIdx) && mTuner.isLivePlay(0))
                    mTunerViewList.get(mCurTunerIdx).setStatus(TunerView.STATUS_PASS);
                else
                    mTunerViewList.get(mCurTunerIdx).setStatus(TunerView.STATUS_FAIL);
            }

            mHandler.postDelayed(mCheckStatusRunnable, 3000);
        }
    };

    private Runnable mStableTestRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mGlobalVars.m_GetIPTVActive()) {
                runStableTest();
            }
        }
    };

    private Runnable mCheckSignalRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mGlobalVars.m_GetIPTVActive()) {
                updateSignalLevel(mCurTunerIdx);
            }

            mHandler.postDelayed(mCheckSignalRunnable, 1000);
        }
    };

    private View.OnFocusChangeListener mEtVideoSrcFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            EditText editText = (EditText) view;

            if (hasFocus) {
                editText.setBackgroundResource(R.drawable.shape_rectangle_edit);
            } else {
                mGlobalVars.m_SetCurVideoUrl(editText.getText().toString());
                editText.setBackgroundResource(R.drawable.shape_rectangle_orange);
                editText.setFocusable(false);
            }
        }
    };

    private View.OnKeyListener mEtVideoSrcKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keycode, KeyEvent keyEvent) {
            boolean isActionDown = keyEvent.getAction() == KeyEvent.ACTION_DOWN;
            EditText editText = (EditText) v;

            if (isActionDown) {
                switch (keycode) {
                    case KeyEvent.KEYCODE_DPAD_UP:  // Delete one char if enable delete func
                        int selectionStart = editText.getSelectionStart();
                        if (selectionStart > 0) {
                            editText.getText().delete(selectionStart - 1, selectionStart);
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_DOWN:    // Block dpad_down
                        return true;
                    case KeyEvent.KEYCODE_BACK:
                        mTvIpTvIdx.setFocusable(true);
                        mTvIpTvIdx.requestFocus();
                        return true;
                }
            }

            return false;
        }
    };

    private EditText.OnEditorActionListener mEtVideoSrcActionListener = new EditText.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            EditText editText = (EditText) v;
            if  ((actionId == EditorInfo.IME_ACTION_DONE)) {
                InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }

                mTvIpTvIdx.setFocusable(true);
                editText.clearFocus();
                tunerSearch();
                return true;
            }
            return false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ntscpal__info);

        mGlobalVars = (Global_Variables) getApplicationContext();
        mCurTunerIdx = mGlobalVars.mtunerID;

        initViews();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();

        mHandler.post(mCheckStatusRunnable);
        mHandler.post(mCheckSignalRunnable);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();

        mHandler.removeCallbacks(mCheckStatusRunnable);
        mHandler.removeCallbacks(mStableTestRunnable);
        mHandler.removeCallbacks(mCheckSignalRunnable);
        mTuner = null;
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();

        mHandler.removeCallbacksAndMessages(null);
        SaveTable(EnTableType.PROGRAME);
    }

    @Override
    public void onMessage(TVMessage tvMessage) {
        super.onMessage(tvMessage);
        switch (tvMessage.getMsgType()) {
            case TVMessage.TYPE_SCAN_BEGIN:
                Log.d("TAG", "onMessage:    TVMessage.TYPE_SCAN_BEGIN!");
                break;
            case TVMessage.TYPE_SCAN_PROCESS:
                Log.d("TAG", "onMessage:    TVMessage.TYPE_SCAN_PROCESS!");
                break;
            case TVMessage.TYPE_SCAN_END:
                Log.d("TAG", "onMessage:    TVMessage.TYPE_SCAN_END tvnum = " + tvMessage.getTotalTVNumber() + " radio " + tvMessage.getTotalRadioNumber());
                if (!mIsSearching || mGlobalVars.m_GetIPTVActive()) {
                    break;
                }

                if (tvMessage.getTotalTVNumber() != 0 || tvMessage.getTotalRadioNumber() != 0) {
                    mTuner.ScanParamsStopScan(true);
                    mTuner.ScanResultSetChannel();

                    int tpID = mTunerViewList.get(mCurTunerIdx).getTpID();
                    long channelID = getPlayableChannelIDByTpID(tpID);

                    mTuner.AvSinglePlay(mCurTunerIdx, channelID); //mtestAvSinglePlay(mCurTunerIdx, channelID);
                    mTunerViewList.get(mCurTunerIdx).setStatus(TunerView.STATUS_PASS);

                }
                else {
                    Toast.makeText(this, "No Channels Found!", Toast.LENGTH_SHORT).show();
                    mTunerViewList.get(mCurTunerIdx).setStatus(TunerView.STATUS_FAIL);
                }

                mIsSearching = false;
                if (mIsRunningStableTest) {
                    mTuner.AvSplitPlay(mTunerViewList); //mtestAvSplitPlay();
                    mHandler.postDelayed(mStableTestRunnable, 5000);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean isActionDown = event.getAction() == KeyEvent.ACTION_DOWN;
        if (isActionDown && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) { // intercept ok key
            tunerSearch();
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        PesiSharedPreference pesiSharedPreference = new PesiSharedPreference(this);
        boolean osdVisible;
        boolean errorStatusVisible;

        switch (keyCode) {
            case KeyEvent.KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                pesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_ntsc_pal_info), MtestConfig.TEST_RESULT_FAIL);
                pesiSharedPreference.save();
                finish();
                break;
            case KeyEvent.KEYCODE_PROG_BLUE:
            case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
                osdVisible = mMainLayout.getVisibility() == View.VISIBLE;
                if (osdVisible) {
                    pesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_ntsc_pal_info), MtestConfig.TEST_RESULT_PASS);
                    pesiSharedPreference.save();
                    finish();
                }
                else if (!mGlobalVars.m_GetIPTVActive()) {
                    // show/hide ber & VideoErrorFrameCount when OSD is hide
                    errorStatusVisible = mTvErrorStatus.getVisibility() == View.VISIBLE;
                    setErrorStatusVisibility(!errorStatusVisible);
                }

                break;
            case KeyEvent.KEYCODE_PROG_GREEN:
            case ExtKeyboardDefine.KEYCODE_PROG_GREEN: // Johnny 20181210 for keyboard control
                if (mGlobalVars.isStableTestEnabled()) {
                    Toast.makeText(this, "Stable Test Start!", Toast.LENGTH_SHORT).show();
                    mHandler.removeCallbacks(mStableTestRunnable);
                    mHandler.post(mStableTestRunnable);
                    mIsRunningStableTest = true;
                }
                else { // for kbro remote control
                    if (mGlobalVars.m_GetIPTVActive()) {
                        mEtIpTVSrc.setFocusable(true);
                        mEtIpTVSrc.requestFocus();
                    } else {
                        mTunerViewList.get(mCurTunerIdx).startEditParams();
                    }
                }
                break;
            case KeyEvent.KEYCODE_PROG_YELLOW:
            case ExtKeyboardDefine.KEYCODE_PROG_YELLOW: // Johnny 20181210 for keyboard control
                // osd
                osdVisible = mMainLayout.getVisibility() == View.VISIBLE;
                setOSDVisibility(!osdVisible);
                break;
            case KeyEvent.KEYCODE_INFO:
            case ExtKeyboardDefine.KEYCODE_INFO:    // Johnny 20181210 for keyboard control
                if (mGlobalVars.m_GetIPTVActive()) {
                    mEtIpTVSrc.setFocusable(true);
                    mEtIpTVSrc.requestFocus();
                } else {
                    mTunerViewList.get(mCurTunerIdx).startEditParams();
                }
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                // multi play
                mTuner.AvSplitPlay(mTunerViewList); //mtestAvSplitPlay();
                break;
            case KeyEvent.KEYCODE_CHANNEL_UP:
            case KeyEvent.KEYCODE_CHANNEL_DOWN:
                // replay
                int tpID = mTunerViewList.get(mCurTunerIdx).getTpID();
                long channelID = getPlayableChannelIDByTpID(tpID);
                mTuner.AvSinglePlay(mCurTunerIdx, channelID); //mtestAvSinglePlay(mCurTunerIdx, channelID);
                break;
        }

        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {   // between 0~9
            selectTunerByKeyCode(keyCode);
        }

        return super.onKeyDown(keyCode, event);
    }

    private void initViews() {
        mMainLayout = (ConstraintLayout) findViewById(R.id.layout_ntscpalinfo_main);
        mTvIpTvIdx = (TextView) findViewById(R.id.tv_iptv_index);
        mEtIpTVSrc = (EditText) findViewById(R.id.et_iptv_videosource);
        mEtIpTVSrc.setOnFocusChangeListener(mEtVideoSrcFocusChangeListener);
        mEtIpTVSrc.setOnKeyListener(mEtVideoSrcKeyListener);
        mEtIpTVSrc.setOnEditorActionListener(mEtVideoSrcActionListener);
        mEtIpTVSrc.setText(mGlobalVars.m_GetCurVideoUrl());

        initTunerView();
        mTvIpTvIdx.setText(String.valueOf(mTunerViewList.size()));

        mTvSignalStregth = (TextView) findViewById(R.id.tv_signal_strength_value);
        mTvSignalQuality = (TextView) findViewById(R.id.tv_signal_quality_value);
//        mTvSignalBER = (TextView) findViewById(R.id.tv_signal_ber_value);
        mProgbarStrength = (ProgressBar) findViewById(R.id.progbar_signal_strength);
        mProgbarQuality = (ProgressBar) findViewById(R.id.progbar_signal_quality);

        mTvErrorStatus = (TextView) findViewById(R.id.tv_error_status);
    }

    private void initTunerView() {
        int tunerNum = mTuner.getTunerNum();//GetTunerNum();
//        List<Integer> tunerTypeList = GetTunerTypeList();
        mTunerViewList = new ArrayList<>();
        for (int i = 0 ; i < tunerNum && i < 4 ; i++) { // only support 4 tuners now
            View tunerView = findViewById(getTunerViewResID(i));
            mTunerViewList.add(new TunerView(tunerView, i, /*tunerTypeList.get(i)*/GetCurTunerType(), this));
        }
    }

    private int getTunerViewResID(int index) {
         int[] ids = {
                 R.id.tuner_view01,
                 R.id.tuner_view02,
                 R.id.tuner_view03,
                 R.id.tuner_view04
         };

         if (index < ids.length) {
             return ids[index];
         }
         else {
             return -1;
         }
    }

    private void tunerSearch() {
        if (mGlobalVars.m_GetIPTVActive()) {
            String url = mEtIpTVSrc.getText().toString();
            mediaPlayerPlay(url);
        } else {
//            delAllPrograms();
            mTunerViewList.get(mCurTunerIdx).stopEditParams();
            delProgramsByTpID(mTunerViewList.get(mCurTunerIdx).getTpID());
            mIsSearching = mTunerViewList.get(mCurTunerIdx).tunerSearch();
        }
    }

    private void mediaPlayerPlay(String url) {
        // TODO: add player module to handle media?
        // edwin 20200429 for playing stream -s
        playExoplayer(url); // Edwin 20200507 put code to a method
        // edwin 20200429 for playing stream -e
    }

    // Edwin 20200507 put code to a method -s
    private void playExoplayer (String url) {
        Uri videoUri = Uri.parse(url);
        /*videoUri = Uri.parse("udp://@10.1.4.183:1234");
        videoUri = Uri.parse("http://10.1.4.180/test/data/test_wen.m3u8");*/

        if (url.contains("usb://")) //centaur 20200805 add apk player test
        {
            Uri uri = null;
            String filename = url.substring(6);
            StorageManager mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
            PesiStorageHelper pesiStorageHelper = new PesiStorageHelper(mStorageManager);
            Log.d(TAG, "playExoplayer filename = " + filename);
            for (Object volumeInfo : getVolumes()) {
                File file = new File(pesiStorageHelper.getPath(volumeInfo), filename);
                Log.d(TAG, "volumeInfo.internalPath = " + pesiStorageHelper.getInternalPath(volumeInfo) +" file exists= "+ file.exists());
                if(file.exists()) {
                    uri = Uri.parse(pesiStorageHelper.getInternalPath(volumeInfo) + "/" + filename);
                    break;
                }
            }
            DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder().build();
            Log.d(TAG, "bandwidthMeter = " + bandwidthMeter);
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "app_name"), bandwidthMeter);
            Log.d(TAG, "dataSourceFactory = " + dataSourceFactory);
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            Log.d(TAG, "extractorsFactory = " + extractorsFactory);
            if(uri != null) {
                mMediaSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
                LoopingMediaSource loopMediaSouce = new LoopingMediaSource(mMediaSource);
                mExoPlayer.prepare(loopMediaSouce);
            }
        }
        else if (url.contains("asset:///"))
        {
            DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder().build();
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "app_name"), bandwidthMeter);
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            mMediaSource = new ExtractorMediaSource(videoUri, dataSourceFactory, extractorsFactory, null, null);
            LoopingMediaSource loopMediaSouce = new LoopingMediaSource(mMediaSource);
            mExoPlayer.prepare(loopMediaSouce);
        }
        else
        {
            if (true)
            {
                // -- UDP (VLC 3.0.8 streaming server)
                DataSource.Factory factory = () -> new UdpDataSource(3000, 100000);
                ExtractorsFactory tsExtractorFactory = () -> new TsExtractor[]{new TsExtractor(TsExtractor.MODE_SINGLE_PMT,
                        new TimestampAdjuster(0), new DefaultTsPayloadReaderFactory())};
                mMediaSource = new ExtractorMediaSource(videoUri, factory, tsExtractorFactory, null, null);
                mExoPlayer.prepare(mMediaSource);
            }
            else
            {
                // -- HLS server (ifconfig eth0 10.1.4.xxx, virtualbox internet bridge)
                videoUri = Uri.parse("http://10.1.4.180/test/data/test_wen.m3u8");
                DataSource.Factory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(getApplicationContext(), "MainActivity"));
                HlsMediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                        .setAllowChunklessPreparation(true)
                        .createMediaSource(videoUri);
                mExoPlayer.prepare(mediaSource);
            }
        }

        mExoPlayer.setPlayWhenReady(true);
        mExoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.d(TAG, "onPlayerStateChanged: playWhenReady = "+playWhenReady+" playbackState = "+playbackState);

                if (!playWhenReady)
                    return;

                if (playbackState == Player.STATE_IDLE)
                    mExoPlayerIsPlaying = false;
                else if (playbackState == Player.STATE_BUFFERING)
                    mExoPlayerIsPlaying = false;
                else if (playbackState == Player.STATE_READY)
                    mExoPlayerIsPlaying = true;
                else
                    Log.d(TAG, "onPlayerStateChanged: unknown playbackState = " + playbackState);
            }
        });
    }
    // Edwin 20200507 put code to a method -e

    /**
     * select tuner or iptv by keycode
     */
    private void selectTunerByKeyCode(int keyCode)
    {
        if (keyCode < KeyEvent.KEYCODE_0 || keyCode > KeyEvent.KEYCODE_9) {
            return;
        }

        if (mTuner.getTunerNum() <= 0) { // no tuner, only iptv, do nothing
            return;
        }

        if (mIsSearching) {
            return;
        }

        int indexFromKeyCode = keyCode - KeyEvent.KEYCODE_0;
        if (!mGlobalVars.m_GetIPTVActive() && indexFromKeyCode == mCurTunerIdx) {   // same tuner
            return;
        }

        if (indexFromKeyCode < mTunerViewList.size()) { // tuner
            mTvIpTvIdx.setFocusable(false);
            if (mTuner.isLivePlay(0))
            {
                //AvControlPlayStop(0);
                mTuner.AvStopAll(0);
            }

            if (mExoPlayer != null && mExoPlayerIsPlaying) {
                mExoPlayer.stop(true);
            }

//            delAllPrograms();
            mTvIpTvIdx.setBackgroundResource(R.drawable.shape_rectangle_blue_bright);
            mTunerViewList.get(mCurTunerIdx).setStatus(TunerView.STATUS_NONE);


            mGlobalVars.m_SetIPTVActive(false);
            mCurTunerIdx = indexFromKeyCode;
            mGlobalVars.mtunerID = mCurTunerIdx;
//            mGlobalVars.mtpID = mTunerViewList.get(mCurTunerIdx).getTpID();
            mTunerViewList.get(mCurTunerIdx).setStatus(TunerView.STATUS_FAIL);

        }
        else if (indexFromKeyCode == mTunerViewList.size()) {   // iptv
            if (mTuner.isLivePlay(0))
            {
                //AvControlPlayStop(0);
                mTuner.AvStopAll(0);
                //AvControlClose(0);
            }
            mTunerViewList.get(mCurTunerIdx).setStatus(TunerView.STATUS_NONE);

            mGlobalVars.m_SetIPTVActive(true);
            mTvIpTvIdx.setBackgroundResource(R.drawable.shape_rectangle_fail);

            resetSignalLevel();
            mTvErrorStatus.setVisibility(View.INVISIBLE);
        }
    }

    private void delAllPrograms() {
        //ProgramManagerInit(ProgramInfo.ALL_TV_TYPE);
        ProgramManagerList = GetProgramManager(ProgramInfo.ALL_TV_TYPE);
        if (ProgramManagerList.get(0).ProgramManagerInfoList.size() > 0) {
            for (int i = 0; i < ProgramManagerList.get(0).ProgramManagerInfoList.size(); i++)
                ProgramManagerList.get(0).DelAllProgram(1);
            ProgramManagerSave(ProgramManagerList);
        }
        //ProgramManagerInit(ProgramInfo.ALL_RADIO_TYPE);
        ProgramManagerList = GetProgramManager(ProgramInfo.ALL_RADIO_TYPE);
        if (ProgramManagerList.get(0).ProgramManagerInfoList.size() > 0) {
            for (int i = 0; i < ProgramManagerList.get(0).ProgramManagerInfoList.size(); i++)
                ProgramManagerList.get(0).DelAllProgram(1);
            ProgramManagerSave(ProgramManagerList);
        }
    }

    private void delProgramsByTpID(int tpID) {
        List<SimpleChannel> simpleChannelList = ProgramInfoGetSimpleChannelList(ProgramInfo.ALL_TV_TYPE, 1, 1);
        if (simpleChannelList == null) {
            simpleChannelList = new ArrayList<>();
        }

        for (SimpleChannel simpleChannel : simpleChannelList) {
            if (simpleChannel.getTpId() == tpID) {
                ProgramInfoDelete(simpleChannel.getChannelId());
            }
        }
    }

    // find playable TV channel
    // for now it means first TV channel of a tp
    private long getPlayableChannelIDByTpID(int tpID) {
        long channelID = 0;

        List<SimpleChannel> simpleChannelList = MtestGetSimpleChannelListByTpID(tpID);
        if (!simpleChannelList.isEmpty()) {
            channelID = simpleChannelList.get(0).getChannelId(); // first channel
        }

        return channelID;
    }

    /*private void mtestAvSplitPlay() {
        boolean canPlay = false;
        List<Integer> tunerIDs = new ArrayList<>();
        List<Long> channelIDs = new ArrayList<>();

        int tunerNum = GetTunerNum();
        for (int i = 0 ; i < tunerNum && i < 4 ; i++) { // only support 4 tuners now
            long channelID = getPlayableChannelIDByTpID(mTunerViewList.get(i).getTpID());
            if (!canPlay && channelID >= 0) {    // can play if one of the tuners has a playable channelID
                canPlay = true;
            }

            tunerIDs.add(i);
            channelIDs.add(channelID);
        }

        if (canPlay) {
            MtestTestAvStopByTunerID(0);
            MtestTestAvStopByTunerID(1);
            MtestTestAvStopByTunerID(2);
            MtestTestAvStopByTunerID(3);
            MtestTestAvMultiPlay(tunerNum, tunerIDs, channelIDs);
        }
    }*/

    // mtest av play must use this
    // because AvControlPlayByChannelID may have problem after split play
    /*private void mtestAvSinglePlay(int tunerID, long channelID) {
        if (tunerID >= 0 && channelID >= 0) {
            List<Integer> tunerIDs = new ArrayList<>();
            List<Long> channelIDs = new ArrayList<>();

            tunerIDs.add(tunerID);
            channelIDs.add(channelID);

            MtestTestAvStopByTunerID(0);
            MtestTestAvStopByTunerID(1);
            MtestTestAvStopByTunerID(2);
            MtestTestAvStopByTunerID(3);
            MtestTestAvMultiPlay(1, tunerIDs, channelIDs);
        }
    }*/

    private void runStableTest() {
        if (!mGlobalVars.m_GetIPTVActive()) {
            // change tuner
            if (mCurTunerIdx >= 3) { // select tuner 0
                selectTunerByKeyCode(KeyEvent.KEYCODE_0);
            }
            else { // select next tuner
                selectTunerByKeyCode(KeyEvent.KEYCODE_0 + mCurTunerIdx + 1);
            }

            TunerView curTunerView = mTunerViewList.get(mCurTunerIdx);
            if (curTunerView.getTunerType() == TpInfo.DVBC) {
                if (mCurStableTestTp >= mStableTestTpsDVBC.length) {
                    mCurStableTestTp = 0;
                }

                int freq = mStableTestTpsDVBC[mCurStableTestTp][0];
                int symbol = mStableTestTpsDVBC[mCurStableTestTp][1];
                int qam = mStableTestTpsDVBC[mCurStableTestTp][2];

                curTunerView.setFreq(freq);
                curTunerView.setSymbolRate(symbol);
                curTunerView.setQam(qam);
            }
            else if (curTunerView.getTunerType() == TpInfo.ISDBT) {
                if (mCurStableTestTp >= mStableTestTpsISDBT.length) {
                    mCurStableTestTp = 0;
                }

                int freq = mStableTestTpsISDBT[mCurStableTestTp][0];
                int band = mStableTestTpsISDBT[mCurStableTestTp][1];

                curTunerView.setFreq(freq);
                curTunerView.setBandwidth(band);
            }
            else if (curTunerView.getTunerType() == TpInfo.DVBS) {
                if (mCurStableTestTp >= mStableTestTpsDVBS.length) {
                    mCurStableTestTp = 0;
                }

                int freq = mStableTestTpsDVBS[mCurStableTestTp][0];
                int symbol = mStableTestTpsDVBS[mCurStableTestTp][1];

                curTunerView.setFreq(freq);
                curTunerView.setSymbolRate(symbol);
            }

            tunerSearch();
            mCurStableTestTp++;
        }
    }

    private void updateSignalLevel(int tunerID)
    {
        int quality;
        int strength;
        String ber;
        int lock;
        int barColor ;
        String str;
        int errorFrameCount;
        int frameDropCount;

        if (mMainLayout.getVisibility() == View.VISIBLE) //  for service overloading
        {
            lock = mTuner.getLockStatus(tunerID);
            quality = mTuner.getQuality(tunerID);
            strength = mTuner.getStrength(tunerID);

            str = strength +" %";
            mTvSignalStregth.setText(str);
            str = quality +" %";
            mTvSignalQuality.setText(str);

            barColor = lock == 1 ?  Color.GREEN : Color.RED;

            mProgbarStrength.setProgressTintList(ColorStateList.valueOf(barColor));
            mProgbarQuality.setProgressTintList(ColorStateList.valueOf(barColor));

            mProgbarStrength.setProgress(strength);
            mProgbarQuality.setProgress(quality);
        }
        else
        {
            errorFrameCount = mTuner.getErrorFrameCount(tunerID);
            frameDropCount = mTuner.getFrameDropCount(tunerID);
            ber = mTuner.getBER(tunerID);
            mTvErrorStatus.setText(String.format(Locale.getDefault(), "BER = %s\nErrorFrameCount = %d\nFrameDropCount = %d", ber, errorFrameCount, frameDropCount));
        }
    }

    private void resetSignalLevel() {
        mTvSignalStregth.setText("0 %");
        mTvSignalQuality.setText("0 %");
//        mTvSignalBER.setText("");
        mTvErrorStatus.setText(String.format(Locale.getDefault(), "BER = %s\nErrorFrameCount = %d\nFrameDropCount = %d", "", 0, 0));

        mProgbarStrength.setProgress(0);
        mProgbarQuality.setProgress(0);
    }

    private void setOSDVisibility(boolean visible) {
        if (visible) {
            mMainLayout.setVisibility(View.VISIBLE);
            mTvErrorStatus.setVisibility(View.INVISIBLE);
        } else {
            mMainLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void setErrorStatusVisibility(boolean visible) {
        if (visible) {
            mTvErrorStatus.setVisibility(View.VISIBLE);
        } else {
            mTvErrorStatus.setVisibility(View.INVISIBLE);
        }
    }
}
