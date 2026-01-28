package com.mtest.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.storage.StorageManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.system.Os;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory;
import com.google.android.exoplayer2.extractor.ts.TsExtractor;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.mtest.config.HwTestConfig;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.UdpDataSource;
import com.google.android.exoplayer2.util.TimestampAdjuster;
import com.mtest.config.MtestConfig;
import com.mtest.module.BluetoothModule;
import com.mtest.module.DDRModule;
import com.mtest.module.EthernetModule;
import com.mtest.module.FlashModule;
import com.mtest.module.HdcpModule;
import com.mtest.module.LedModule;
import com.mtest.module.OtaModule;
import com.mtest.module.PesiSharedPreference;
import com.mtest.module.PowerSavingModule;
import com.mtest.module.SDcardModule;
import com.mtest.module.SmartCardModule;
import com.mtest.module.TunerModule;
import com.mtest.module.UsbModule;
import com.mtest.module.WifiModule;
import com.mtest.utils.LocaleHelper;
import com.mtest.utils.PesiStorageHelper;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.Config.PcToolcfg;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.SatInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.View.MessageDialogView;
import com.prime.dtvplayer.utils.TVMessage;
import com.prime.dtvplayer.utils.TVScanParams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;


public class MainActivity extends DTVActivity {
    private static final String TAG = "MainActivity";

    public static final int DO_RUN_BOOT_STABLE_TEST_DELAY = 7000; // 7 sec to get mtest_ini.dat
    public static final String DEFAULT_URL = "asset:///splash.mp4";//"udp://@238.1.1.1:1234";
    public static final String DEFAULT_PING_IP = "10.1.4.200";

    private static final int LIST_ITEM_COUNT = 15;  // count of left/right list
    private static final int CHECK_STATUS_DELAY = 3000; // ms, better not less than 3 sec because eth test needs 3 sec
    private static final int LONG_TERN_10_MINS = 600000; // ms, 10 mins to write log
    private static final int HIDDEN_INPUT_TIMEOUT = 3000; // ms, reset mHiddenInput // Johnny 20190613 add timeout to reset hidden input
    private static final int REQUEST_CODE_HIDDEN_INPUT = 1;
    private static final int TIME_INTERVAL_BACK = 2000; // milliseconds, time between two back pressed

    private static final String LOCALE_EN = "en";
    private static final String LOCALE_ZH = "zh";
    private static final String ALL_TEST_PASS = "All_Test_ITem_Pass";//Scoty 20190417 should check all test item pass and then trigger OTA
    private static final String FINISH_PCTOOL = "finish.pctool.pass";//Scoty 20190419 save write pctool finish pass/fail to SharedPreferences
    private static final String BOOT_STABLE_PASS = "PASS";
    private static final String BOOT_STABLE_FAIL = "FAIL";

    public static boolean mExoPlayerIsPlaying = false; // edwin 20200429 for playing stream
    public static SimpleExoPlayer mExoPlayer; // edwin 20200429 for playing stream
    public static MediaSource mMediaSource; // edwin 20200429 for playing stream

    private int mRedKeyCount = 0; // Johnny 20190506 fix RedKeyCount not reset after destroy
    private int mDurabilityLedStatus = 0;
    private long mBackPressedTime = 0;
    private long mLongTermTotalTime = 0;
    private boolean mSmartCardAtrFlag = false; // false = no card | atr fail, true = has card & atr ok // assume no card at beginning
    private boolean mIsSearching = false, mIsShowNoCHToastOnce = false;//Scoty 20190422 modify show no channel only once
    private boolean mEnableOpt = true;
    private boolean mPowerSavingTested = false;

    private String mHiddenInput = "";

    private int[] mBootStableTestTpDVBC = {482, 5200, 4}; // {freq, symbol, qam}, qam: 0 = 16, 1 = 32, ..., 4 = 256
    private int[] mBootStableTestTpISDBT = {485143, 0};  // {freq, band}, band: 0 = 6M, 1 = 7M, 2 = 8M
    private int[] mBootStableTestTpDVBS = {1000, 27500};  // {freq, symbol}

    private List<String> mEnableItemList = new ArrayList<>();
    private List<String> mSelectableItemList = new ArrayList<>();
    private HashMap<String, Class<?>> mActivityMap = new HashMap<>();

    private Handler mHandler = new Handler();
    private Toast mExitToast;   //Scoty 20190422 modify show no channel only once// Johnny 20180912 add press back again to exit

    private ConstraintLayout mMainLayout;
    private ListView mListViewL;
    private ListView mListViewR;
    private TextView mTvModel;
    private TextView mTvVer;
    private TextView mTvErrorStatus;
    private TextView mTvBuildInfo;
    private SurfaceView mSurfaceView;
    private SurfaceView mSurfaceViewExoplayer;
//    private PlayerView mPlayerView;

    private PesiSharedPreference mPesiSharedPreference;
    private PesiSharedPreference mHwConfigSharedPreference;
    private Global_Variables mGlobalVars;
    private HwTestConfig mHwTestConfig;//gary20200504 add HW test config get from usb

    // module
    private UsbModule mUsbModule;
    private BluetoothModule mBluetoothModule;
    private WifiModule mWifiModule;
    private TunerModule mTunerModule;
    private SDcardModule mSDcardModule;
    private DDRModule mDDRModule;
    private EthernetModule mEthernetModule;
    private FlashModule mFlashModule;
    private LedModule mLedModule;

    private Runnable mCheckStatusRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "run: mCheckStatusRunnable");
            // POWER_TEST           0:off           1:Tool, Production mode
            //                      2:SW reboot     3:STR       4:Reset Default
            // THERMAL_TEST         0:off           1:add loading , Ex UI redraw
            // LONG_TERM_TEST       0:off           1:Wait 10 mins, show PASS/FAIL
            if (mHwTestConfig.getPOWER_TEST() == 0) {
                if (mHwTestConfig.getTHERMAL_TEST() == 1 || mHwTestConfig.getLONG_TERM_TEST() == 1)
                    runThermalTest(); // Thermal Test, Long Term Test
                else
                    runAutoTests();
                updateTestResultDisplay();
                mHandler.postDelayed(mCheckStatusRunnable, CHECK_STATUS_DELAY);
            }
        }
    };

    private Runnable mDoRunBootStableTest = new Runnable() {
        @Override
        public void run() {
            // update hw config from usb
            if (updateHwTestFlag()) {
                // updateHwTestFlag return true, hw config is updated
                // system should reboot, do nothing
                return;
            }

            int powerTestFlag = mHwTestConfig.getPOWER_TEST();
            Log.d(TAG, "mDoRunBootStableTest: POWER_TEST = " + powerTestFlag + " , HW_TEST_CONFIG_ONOFF = " + mHwTestConfig.getHW_TEST_CONFIG_ONOFF());
            /* POWER_TEST
            0:off
            1:Tool, Production mode
            2:SW reboot
            3:STR
            4:Reset Default */
            if (powerTestFlag > 0 && powerTestFlag <= 3) {
                mHandler.removeCallbacks(mCheckStatusRunnable);
                runBootStableTest();
            }
            else if (powerTestFlag == HwTestConfig.RUN_RESET_DEFAULT_TEST) {
                mHandler.removeCallbacks(mCheckStatusRunnable);
                runResetDefaultTest();
            }
        }
    };

    // Johnny 20190613 add timeout to reset hidden input
    private Runnable mResetHiddenInputRunnable = new Runnable() {
        @Override
        public void run() {
            mHiddenInput = "";
        }
    };

    private Runnable mCheckSignalRunnable = new Runnable() {
        @Override
        public void run() {
            updateSignalLevel(mGlobalVars.mtunerID);

            mHandler.postDelayed(mCheckSignalRunnable, 1000);
        }
    };

    /**
     * durability test
     * turn led on and off every delayMillis
     */
    private Runnable mDurabilityLedRunnable = new Runnable() {
        @Override
        public void run() {
            if (mDurabilityLedStatus == LedModule.STATUS_ALL_OFF) {
                mDurabilityLedStatus = LedModule.STATUS_ALL_ON;
            }
            else {
                mDurabilityLedStatus = LedModule.STATUS_ALL_OFF;
            }

            mLedModule.setOnOff(mDurabilityLedStatus);
            mHandler.postDelayed(mDurabilityLedRunnable, 2000);
        }
    };

    /**
     * durability test
     * play and call mDurabilityStopRunnable to stop playing after delayMillis
     */
    private Runnable mDurabilityPlayRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "run: mDurabilityPlayRunnable");
            runExoPlayer();
            mHandler.postDelayed(mDurabilityStopRunnable, 60*60*1000);
        }
    };

    /**
     * durability test
     * stop playing and call mDurabilityPlayRunnable to play again after delayMillis
     */
    private Runnable mDurabilityStopRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "run: mDurabilityStopRunnable");
            mExoPlayer.stop(true);
            mHandler.postDelayed(mDurabilityPlayRunnable, 10*60*1000);
        }
    };

    private ListView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            String key = ((TextView) view.findViewById(R.id.textView_name)).getText().toString();
            Class<?> intentActivity = mActivityMap.get(key); // get target activity from mActivityMap
            if (intentActivity != null) {
                Intent intent = new Intent(getBaseContext(), intentActivity);
                startActivity(intent);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        int bootComplete = intent.getIntExtra("bootComplete", 0);
        setContentView(R.layout.activity_main);

        CheckVersion(); // check service & apk version, copy from DTVPlayer ViewActivity
        MtestStartMtest(getMtestApkVersion()); // service need // Johnny 20190909 send mtest apk version to service
        InitRecordPath(); // prevent rec path wrong
        AvControlAudioOutput(ViewHistory.getPlayId(), GposInfoGet().getDolbyMode()); // sync audio output between Gpos & service

        mGlobalVars = (Global_Variables) getApplicationContext();
        mPesiSharedPreference = new PesiSharedPreference(getApplicationContext());
        mHwConfigSharedPreference = new PesiSharedPreference(getApplicationContext(), PesiSharedPreference.NAME_HARDWARE_CONFIG);
        mExitToast = Toast.makeText(this, getString(R.string.STR_PRESS_BACK_AGAIN), Toast.LENGTH_SHORT);  // Johnny 20180912 add press back again to exit

        initViews();
        initTitleAndBuildInfo();
        initMenuItems();
        initModule();
        setSurfaceView(mSurfaceView); // set surface to service

        // init hw config
        mHwTestConfig = HwTestConfig.getInstance(this,bootComplete);
        mHwTestConfig.setTvModelTextView(mTvModel);

        // init exoplayer
        initExoPlayer(); // edwin 20200507 play at start

        //casper20200818 save btaddress for 1319 mtest
        try {
            File file = new File("/sdcard/Download/", "bt_info.dat");
            testWrite(file, mBluetoothModule.getAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // if no tuner, set iptv
        if (mTunerModule.getTunerNum() == 0) {
            mGlobalVars.m_SetIPTVActive(true);
            // hw boot stable tests
            mHandler.postDelayed(mDoRunBootStableTest, DO_RUN_BOOT_STABLE_TEST_DELAY);
        }
        else {
            mGlobalVars.m_SetIPTVActive(false);
            // hw boot stable tests will start after SCAN_END
        }

        // tuner search or ip tv play
        runBootTunerSearch();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");

        //runHiddenTests();
        mBluetoothModule.registerReceiver(); //Scoty 20190410 add bt and wifi auto test
        //MtestMicSetInputGain(0); // set mic to 0dB when start mtest
        //MtestMicSetAlcGain(0); // set alc to mute when start mtest
    }

    @Override
    protected void onResume() {
        super.onResume();

        mHiddenInput = "";
        updateTestResultDisplay();
        if(mHwTestConfig.getPOWER_TEST() == 0) // POWER_TEST off
            mHandler.postDelayed(mCheckStatusRunnable, CHECK_STATUS_DELAY);
        if(!mGlobalVars.m_GetIPTVActive())
            mHandler.post(mCheckSignalRunnable);
        mMainLayout.setVisibility(View.VISIBLE);
        openwifi();//Scoty 20190417 open wifi when open mtest page
    }

    @Override
    protected void onPause() {
        super.onPause();

        mHandler.removeCallbacks(mCheckSignalRunnable);
        mHandler.removeCallbacks(mCheckStatusRunnable);

        mMainLayout.setVisibility(View.INVISIBLE);
        mTvErrorStatus.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");

        if (mTunerModule.isLivePlay(0)) {
//            AvControlPlayStop(0);
            mTunerModule.AvStopByTunerID(0);//MtestTestAvStopByTunerID(0);
            mTunerModule.AvStopByTunerID(1);//MtestTestAvStopByTunerID(1);
            mTunerModule.AvStopByTunerID(2);//MtestTestAvStopByTunerID(2);
            mTunerModule.AvStopByTunerID(3);//MtestTestAvStopByTunerID(3);
        }

        mBluetoothModule.unregisterReceiver(); //Scoty 20190410 add bt and wifi auto test

        Log.d(TAG, "onStop: stop exoplayer = " + mExoPlayer);
        if (mExoPlayer != null) // Edwin 20200430 fix mantis 6700
            mExoPlayer.stop(true);

//        // reset mic gain, should be changed if service changes default value
//        MtestMicSetInputGain(6);             // 6 = +30dB
//        MtestMicSetLRInputGain(0, 4);   // 4 = 0dB
//        MtestMicSetLRInputGain(1, 4);   // 4 = 0dB
//        MtestMicSetAlcGain(153);             // 153 = +3dB
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        releaseModule();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onBackPressed() {
        // Johnny 20190521 modify flow of pressing back again to exit
        long currentTime = System.currentTimeMillis();
        if (mBackPressedTime + TIME_INTERVAL_BACK < currentTime) {
            mExitToast.show();
            mBackPressedTime = currentTime;
        }
        else {
            mExitToast.cancel();
            super.onBackPressed();
        }
    }

    /**
     * TODO: use new Activity Results API in the future?
     */
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_HIDDEN_INPUT) {
            checkHiddenInputByStr(data.getStringExtra(HiddenInputActivity.KEY_RESULT_HIDDEN_INPUT));
        }

    }

    @Override
    public void onMessage(TVMessage tvMessage) {
        super.onMessage(tvMessage);

        switch (tvMessage.getMsgType()) {
            case TVMessage.TYPE_PIO_ANTENNA_OVERLOAD:
                overloadTuner();
                break;
            case TVMessage.TYPE_PIO_USB_OVERLOAD:
                overloadUSB(tvMessage.GetPioUsbOverloadPort());
                break;
            case TVMessage.TYPE_PIO_FRONT_PANEL_KEY_POWER:
                mPesiSharedPreference.putInt(getString(R.string.str_front_panel_standby), MtestConfig.TEST_RESULT_PASS);
                mPesiSharedPreference.save();
                updateTestResultDisplay();
                break;
            case TVMessage.TYPE_PIO_FRONT_PANEL_KEY_CH_UP:
                mPesiSharedPreference.putInt(getString(R.string.str_front_panel_ch_up), MtestConfig.TEST_RESULT_PASS);
                mPesiSharedPreference.save();
                updateTestResultDisplay();
                break;
            case TVMessage.TYPE_PIO_FRONT_PANEL_KEY_CH_DOWN:
                mPesiSharedPreference.putInt(getString(R.string.str_front_panel_ch_down), MtestConfig.TEST_RESULT_PASS);
                mPesiSharedPreference.save();
                updateTestResultDisplay();
                break;
            case TVMessage.TYPE_MTEST_FRONT_PANEL_KEY_RESET:
                mPesiSharedPreference.putInt(getString(R.string.str_front_panel_reset), MtestConfig.TEST_RESULT_PASS);
                mPesiSharedPreference.save();
                updateTestResultDisplay();
                break;
            case TVMessage.TYPE_MTEST_FRONT_PANEL_KEY_WPS:
                mPesiSharedPreference.putInt(getString(R.string.str_front_panel_wps), MtestConfig.TEST_RESULT_PASS);
                mPesiSharedPreference.save();
                updateTestResultDisplay();
                break;
            case TVMessage.TYPE_SYSTEM_DQA_AUTO_TEST:
                int dqaAutoTestFlag = tvMessage.GetDqaAutoTestFlag();
                if (dqaAutoTestFlag == 1) { // is receive dqa auto test flag, hide OSD
                    setOSDVisibility(false);
                }
                else {
                    setOSDVisibility(true);
                }
                break;
            case TVMessage.TYPE_SCAN_END: // for boot stable test func : runBootTunerSearch
                Log.d("TAG", "onMessage:    TVMessage.TYPE_SCAN_END tvnum = " + tvMessage.getTotalTVNumber() + " radio " + tvMessage.getTotalRadioNumber());
                if (!mIsSearching) {
                    break;
                }

                mIsSearching = false;//Scoty 20190422 continue re-search channel if no channel found
                if (tvMessage.getTotalTVNumber() != 0 || tvMessage.getTotalRadioNumber() != 0)
                {
                    mTunerModule.ScanParamsStopScan(true);
                    mTunerModule.ScanResultSetChannel();

                    long channelID = getPlayableChannelIDByTpID(mGlobalVars.mtpID);
                    mTunerModule.AvSinglePlay(mGlobalVars.mtunerID, channelID); // mtestAvSinglePlay(mGlobalVars.mtunerID, channelID);
                }
                else {
                    if(!mIsShowNoCHToastOnce)//Scoty 20190422 modify show no channel only once
                    {
                        Toast.makeText(this, getString(R.string.STR_NO_CHANNELS_FOUND), Toast.LENGTH_SHORT).show();
                        mIsShowNoCHToastOnce = true;
                    }
                    runBootTunerSearch();//Scoty 20190422 continue re-search channel if no channel found
                }

                // hw boot stable tests
                mHandler.postDelayed(mDoRunBootStableTest, DO_RUN_BOOT_STABLE_TEST_DELAY);

                break;
            case TVMessage.TYPE_MTEST_PCTOOL:{//Scoty 20190410 add Mtest Pc Tool callback
                int cmdId = tvMessage.GetMtestCmdId(), errCode = tvMessage.GetMtestErrCode();
                Log.d(TAG,"TYPE_MTEST_PCTOOL CMD ID = " + cmdId + " Err Code = " + errCode);

                // connect pc tool
                if(cmdId == PcToolcfg.CMD_CONNECT_TEST && errCode == PcToolcfg.PcToolErr.PCTOOL_NO_ERROR) {
                    //Scoty 20190417 should check all test item pass and then trigger OTA -s
                    Bundle bundle = new Bundle();
                    Intent intent = new Intent(MainActivity.this, ConnectPcToolActivity.class);
                    bundle.putBoolean(ALL_TEST_PASS,checkAllItemPass());
                    bundle.putBoolean(OtaModule.KEY_ENABLE_OPT, mEnableOpt);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    //Scoty 20190417 should check all test item pass and then trigger OTA -e
                }

            }break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown: keyCode = " + keyCode);
        Intent intent;
        boolean osdVisible;
        boolean errorStatusVisible;

        // reset ota red key count if press other key
        if (keyCode != KeyEvent.KEYCODE_PROG_RED && keyCode != ExtKeyboardDefine.KEYCODE_PROG_RED) {
            mRedKeyCount = 0;
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                // OTA
                mRedKeyCount++;
                if(mRedKeyCount >= 3) {
                    if (!checkAllItemPass()) {
                        showOTAErrorDialog(getString(R.string.str_ota_msg_pass_all)); // Johnny 20190503 show ota error msg
                    }
                    else if (!getPcToolWriteFinish()) {
                        showOTAErrorDialog(getString(R.string.str_ota_msg_pass_pctool)); // Johnny 20190503 show ota error msg
                    }
                    else {
                        OtaModule otaModule = new OtaModule(this);
                        otaModule.enableOpt(mEnableOpt);//MtestEnableOpt(mEnableOpt);
                        // Johnny 20190620 show ota error msg by return value
                        int ret = otaModule.triggerOTASoftWare();//UpdateMtestOTASoftWare();
                        if (ret != 99) { // 99 = success
                            showOTAErrorDialog(ret);
                        }
                        else {
                            PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
                            powerManager.reboot(null);
                        }
                    }

                    mRedKeyCount = 0;
                }
                break;
            case KeyEvent.KEYCODE_PROG_GREEN:
            case ExtKeyboardDefine.KEYCODE_PROG_GREEN: // Johnny 20181210 for keyboard control
                // QE
                intent = new Intent(MainActivity.this, QEActivity.class);
                startActivity(intent);
                break;
            case KeyEvent.KEYCODE_PROG_YELLOW:
            case ExtKeyboardDefine.KEYCODE_PROG_YELLOW: // Johnny 20181210 for keyboard control
                // osd show/hide
                osdVisible = mMainLayout.getVisibility() == View.VISIBLE;
                setOSDVisibility(!osdVisible);
                break;
            case KeyEvent.KEYCODE_PROG_BLUE:
            case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
                osdVisible = mMainLayout.getVisibility() == View.VISIBLE;
                if (osdVisible) {
                    // connect pc tool
                    //Scoty 20190417 should check all test item pass and then trigger OTA -s
                    Bundle bundle = new Bundle();
                    intent = new Intent(MainActivity.this, ConnectPcToolActivity.class);
                    bundle.putBoolean(ALL_TEST_PASS, checkAllItemPass());
                    bundle.putBoolean(OtaModule.KEY_ENABLE_OPT, mEnableOpt);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    //Scoty 20190417 should check all test item pass and then trigger OTA -e
                }
                else {
                    // show/hide ber & VideoErrorFrameCount when OSD is hide
                    errorStatusVisible = mTvErrorStatus.getVisibility() == View.VISIBLE;
                    setErrorStatusVisibility(!errorStatusVisible);
                }
                break;
            case KeyEvent.KEYCODE_0:
                    // Clear test result
                    clearTestResult();
                    updateTestResultDisplay();
                    resetModule();
                break;
            /* reven add for reset wifi test to connect ap again*/
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                mPesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_wifi), MtestConfig.TEST_RESULT_FAIL);
                mPesiSharedPreference.save();
                updateTestResultDisplay();
                break;
        }

        if (isDigitKeyCode(keyCode)) {   // between 0~9
            checkHiddenInput(keyCode);
        }

        return super.onKeyDown(keyCode, event);
    }

    private void initViews() {
        mMainLayout = (ConstraintLayout) findViewById(R.id.layout_mainactivity_main);
        mListViewL = (ListView) findViewById(R.id.list_left);
        mListViewR = (ListView) findViewById(R.id.list_right);
        mTvModel = (TextView) findViewById(R.id.tv_Model);
        mTvVer = (TextView) findViewById(R.id.tv_Ver);
        mTvErrorStatus = (TextView) findViewById(R.id.tv_error_status);
        mTvBuildInfo = (TextView) findViewById(R.id.tv_build_info);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mSurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        mSurfaceViewExoplayer = (SurfaceView) findViewById(R.id.surfaceView_exoplayer);
        mSurfaceViewExoplayer.getHolder().setFormat(PixelFormat.TRANSPARENT);
//        mPlayerView = (PlayerView) findViewById(R.id.exo_player_view);
//        ((SurfaceView)mPlayerView.getVideoSurfaceView()).getHolder().setFormat(PixelFormat.TRANSPARENT);
    }

    private void initTitleAndBuildInfo() {
        mTvVer.setText(getString(R.string.str_mtest_ver));
        mTvModel.setText(getString(R.string.str_model));

        //mTvModel.append(String.format(Locale.getDefault(), " / ChipID : %s", MtestGetChipID())); // chipID
        if (MtestConfig.isHiddenFunctionEnable(getApplicationContext(), MtestConfig.KEY_EMI)) {
            mTvModel.append(" -EMI");
        }

        // build info
        String buildInfo = "Release: " + Os.uname().release + "\tVersion: " + Os.uname().version;
        mTvBuildInfo.setText(buildInfo);
    }

    /**
     * test items vary by model
     * adjust addTestItem() to edit test items in main menu
     */
    private void initMenuItems()
    {
        // manual test
        // test in a specific activity
//        addTestItem(getString(R.string.str_test_item_seven_segment), SevenSegmentActivity.class, true);
        addTestItem(getString(R.string.str_test_item_led), com.mtest.activity.LedOnOffActivity.class, true);
//        addTestItem(getString(R.string.str_test_item_slow_blanking), SlowBlankingActivity.class, true);
//        addTestItem(getString(R.string.str_test_item_slow_blanking), FastBlankingActivity.class, true);
//        addTestItem(getString(R.string.str_test_item_antenna5v), Ant_5vActivity.class, true);
//        addTestItem(getString(R.string.str_test_item_lnb), LnbOnOffActivity.class, true);
//        addTestItem(getString(R.string.str_test_item_modem), ModemActivity.class, true);
//        addTestItem(getString(R.string.str_test_item_rf_modulator), RFModulatorActivity.class, true);
//        addTestItem(getString(R.string.str_test_item_hdd), HDDTestActivity.class, true);
        addTestItem(getString(R.string.str_test_item_power_saving), Power_SavingActivity.class, true);
//        addTestItem(getString(R.string.str_test_item_buzzer), BuzzerOnOffActivity.class, true);

        // auto test
        // test in runAutoTest() if implemented
        addTestItem(getString(R.string.str_test_item_ntsc_pal_info), NTSCPAL_INFOActivity.class, true);
//        addTestItem(getString(R.string.str_test_item_diseqc), DiseqC_1X_20.class, false);
//        addTestItem(getString(R.string.str_test_item_cable_modem), CableModemActivity.class, false);
//        addTestItem(getString(R.string.str_test_item_smartcard), SmartcardActivity.class, false);
//        addTestItem(getString(R.string.str_test_item_ci), CITestActivity.class, false);
        addTestItem(getString(R.string.str_test_item_hdcp1x), HDCP1xActivity.class, false);
        addTestItem(getString(R.string.str_test_item_hdcp2x), HDCP2xActivity.class, false);
        addTestItem(getString(R.string.str_test_item_usb1), USB_Activity.class, false);
        addTestItem(getString(R.string.str_test_item_usb2), USB_Activity.class, false);
        addTestItem(getString(R.string.str_test_item_sdcard), SD_Card_Activity.class, false);
        addTestItem(getString(R.string.str_test_item_wifi), WiFi_Activity.class, false);
        addTestItem(getString(R.string.str_test_item_bluetooth), BlueTooth_Activity.class, false);
//        addTestItem(getString(R.string.str_test_item_ethernet), Ethernet_Activity.class, false);

        // front panel key
        // test in onMessage() when receive callback from service
//        addTestItem(getString(R.string.str_front_panel_standby), null, false);
//        addTestItem(getString(R.string.str_front_panel_ch_up), null, false);
//        addTestItem(getString(R.string.str_front_panel_ch_down), null, false);
        addTestItem(getString(R.string.str_front_panel_wps), null, false);
        addTestItem(getString(R.string.str_front_panel_reset), null, false);

        // for other testing
//        addTestItem(getString(R.string.str_test_item_microphone), MicrophoneActivity.class, true);

        initMenuList();
    }

    /**
     * testItem:
     * display name and is used for saving and updating test result
     * startActivity:
     * activity if you click the test item in menu, can be null
     * selectable:
     * whether the test item is selectable in menu
     */
    private void addTestItem(@NonNull String testItem, @Nullable Class<?> startActivity, boolean selectable) {
        mEnableItemList.add(testItem);
        mActivityMap.put(testItem, startActivity);
        if (selectable) {
            mSelectableItemList.add(testItem);
        }
    }

    // find the view in main menu by test item
    private View findViewInMenuByTestItem(String testItem) {
        int childCount = mListViewL.getChildCount();
        for (int i = 0 ; i < childCount ; i++) { // find in L List
            View itemView = mListViewL.getChildAt(i);
            if (itemView != null) {
                TextView textViewName = itemView.findViewById(R.id.textView_name);
                String itemName = textViewName.getText().toString();
                if (itemName.equals(testItem)) {
                    return itemView;
                }
            }
        }

        childCount = mListViewR.getChildCount();
        for (int i = 0 ; i < childCount ; i++) { // find in R List
            View itemView = mListViewR.getChildAt(i);
            if (itemView != null) {
                TextView textViewName = itemView.findViewById(R.id.textView_name);
                String itemName = textViewName.getText().toString();
                if (itemName.equals(testItem)) {
                    return itemView;
                }
            }
        }

        return null;
    }

    private void initMenuList() {
        int enableItemListSize = mEnableItemList.size();
        final List<HashMap<String, Object>> itemsL = new ArrayList<>();
        final List<HashMap<String, Object>> itemsR = new ArrayList<>();
        boolean hasSelectableItemL = false;
        boolean hasSelectableItemR = false;

        for (int i = 0; i < LIST_ITEM_COUNT; i++) { // L
            HashMap<String, Object> itemL = new HashMap<>();
            itemL.put("no", i + 1);
            itemL.put("status", "");
            itemL.put("result", "");
            if (i < enableItemListSize) {
                String item = mEnableItemList.get(i);
                itemL.put("name", item);
                itemL.put("enable", mSelectableItemList.contains(item));
            } else {
                itemL.put("name", "");
                itemL.put("enable", false);
            }
            itemsL.add(itemL);

            if (!hasSelectableItemL && (boolean) itemL.get("enable")) {
                hasSelectableItemL = true;
            }
        }

        for (int i = LIST_ITEM_COUNT; i < LIST_ITEM_COUNT * 2; i++) {    // R
            HashMap<String, Object> itemR = new HashMap<>();
            itemR.put("no", i + 1);
            itemR.put("status", "");
            itemR.put("result", "");
            if (i < enableItemListSize) {
                String item = mEnableItemList.get(i);
                itemR.put("name", item);
                itemR.put("enable", mSelectableItemList.contains(item));
            } else {
                itemR.put("name", "");
                itemR.put("enable", false);
            }
            itemsR.add(itemR);

            if (!hasSelectableItemR && (boolean) itemR.get("enable")) {
                hasSelectableItemR = true;
            }
        }

        SimpleAdapter adapterL = new SimpleAdapter(
                this,
                itemsL,
                R.layout.mainactivity_listview_item,
                new String[]{"no", "name", "status", "result"},
                new int[]{R.id.textView_no, R.id.textView_name, R.id.textView_status, R.id.textView_result}) {
            @Override
            public boolean areAllItemsEnabled() {
                return false;
            }

            @Override
            public boolean isEnabled(int position) {
                return (boolean) itemsL.get(position).get("enable");
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View itemView = super.getView(position, convertView, parent);
                TextView textViewResult = itemView.findViewById(R.id.textView_result);

                // fix background wrong when scroll because listview reuse the old view
                textViewResult.setBackground(getDrawable(R.drawable.shape_rectangle_disable));

                // update test result immediately when the view is shown to user
                updateItemDisplay(itemView);
                return itemView;
            }
        };

        SimpleAdapter adapterR = new SimpleAdapter(
                this,
                itemsR,
                R.layout.mainactivity_listview_item,
                new String[]{"no", "name", "status", "result"},
                new int[]{R.id.textView_no, R.id.textView_name, R.id.textView_status, R.id.textView_result}) {
            @Override
            public boolean areAllItemsEnabled() {
                return false;
            }

            @Override
            public boolean isEnabled(int position) {
                return (boolean) itemsR.get(position).get("enable");
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View itemView = super.getView(position, convertView, parent);
                TextView textViewResult = itemView.findViewById(R.id.textView_result);

                // fix background wrong when scroll because listview reuse the old view
                textViewResult.setBackground(getDrawable(R.drawable.shape_rectangle_disable));

                // update test result immediately when the view is shown to user
                updateItemDisplay(itemView);
                return itemView;
            }
        };

        mListViewL.setAdapter(adapterL);
        mListViewR.setAdapter(adapterR);

        mListViewL.setOnItemClickListener(mOnItemClickListener);
        mListViewR.setOnItemClickListener(mOnItemClickListener);

        // the focus won't display if we focus on a listview without any selectable item
        // so we only set listview focusable if it has any selectable item
        mListViewL.setFocusable(hasSelectableItemL);
        mListViewR.setFocusable(hasSelectableItemR);
    }

    // Johnny 20201202 add hidden input to make all items selectable
    // Set all items in our menu list selectable
    private void setMenuListItemsSelectable(ListView listView) {
        SimpleAdapter adapter = (SimpleAdapter) listView.getAdapter();
        for (int i = 0 ; i < adapter.getCount() ; i++) {
            // we know our adapter item is HashMap<String, Object>
            // so just suppress the unchecked cast warning
            @SuppressWarnings("unchecked")
            HashMap<String, Object> item = (HashMap<String, Object>) adapter.getItem(i);
            item.put("enable", true);
        }

        listView.setAdapter(adapter);
        listView.setFocusable(true);
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

    // update all test results
    private void updateTestResultDisplay() {
        Log.d(TAG, "updateTestResultDisplay: ");
        updateListDisplay(mListViewL);
        updateListDisplay(mListViewR);
    }

    // update test results in the given listview
    private void updateListDisplay(ListView listView) {
        Log.d(TAG, "updateListDisplay: ");
        int childCount = listView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View itemView = listView.getChildAt(i);
            updateItemDisplay(itemView);
        }
    }

    // update test result of the given item view
    private void updateItemDisplay(View itemView) {
//        Log.d(TAG, "updateItemDisplay: ");
        if (itemView != null) {
            TextView textViewName = itemView.findViewById(R.id.textView_name);
            TextView textViewResult = itemView.findViewById(R.id.textView_result);
            String itemName = textViewName.getText().toString();

            if (!itemName.isEmpty()) {
                int result = mPesiSharedPreference.getInt(itemName, MtestConfig.TEST_RESULT_NONE);
                if (result == MtestConfig.TEST_RESULT_PASS) {
                    textViewResult.setText("V");
                    textViewResult.setBackgroundResource(R.drawable.shape_rectangle_pass);
                } else if (result == MtestConfig.TEST_RESULT_FAIL) {
                    textViewResult.setText("X");
                    textViewResult.setBackgroundResource(R.drawable.shape_rectangle_fail);
                } else if (result == MtestConfig.TEST_RESULT_WAIT_CARD_OUT) {
                    textViewResult.setText("O");
                    textViewResult.setBackgroundResource(R.drawable.shape_rectangle_focus); // yellow
                } else {
                    textViewResult.setText("");
                    textViewResult.setBackgroundResource(R.drawable.shape_rectangle_disable);
                }
            }
        }
    }

    private void clearTestResult() {
        for (String enableItem : mEnableItemList) {
            mPesiSharedPreference.remove(enableItem);
        }

        // remove power saving flag
        mPesiSharedPreference.remove(PowerSavingModule.POWER_SAVING_FLAG_STRING);
        mPesiSharedPreference.save();
    }

    private void runThermalTest () // Thermal Test
    {
        // Edwin 20200602 for long term test -s
        boolean checkSuccess = false;
        boolean allPass = true;
        String log = "================================\n";

        // Tuner
        checkVideo();
        log += "IP Player ................... ";//centaur 20200619 fix mtest
        checkSuccess = (mPesiSharedPreference.getInt(getString(R.string.str_test_item_ntsc_pal_info), MtestConfig.TEST_RESULT_NONE) == MtestConfig.TEST_RESULT_PASS);
        log = checkTotalLog(checkSuccess, log);
        allPass = checkTotalPass(checkSuccess, allPass);

        // Smart card
        /*checkSmartCard();
        log += "Smart card .............. ";
        checkSuccess = (mPesiSharedPreference.getInt(getString(R.string.str_test_item_smartcard), TEST_RESULT_NONE) == TEST_RESULT_PASS);
        log = checkTotalLog(checkSuccess, log);
        allPass = checkTotalPass(checkSuccess, allPass);*/

        // USB 1
        checkUSB(true, UsbModule.PORT_1);
        log += "Usb 1 ................... ";
        checkSuccess = (mPesiSharedPreference.getInt(getString(R.string.str_test_item_usb1), MtestConfig.TEST_RESULT_NONE) == MtestConfig.TEST_RESULT_PASS);
        log = checkTotalLog(checkSuccess, log);
        allPass = checkTotalPass(checkSuccess, allPass);

        // USB 2
        checkUSB(true, UsbModule.PORT_2);
        log += "Usb 2 ................... ";
        checkSuccess = (mPesiSharedPreference.getInt(getString(R.string.str_test_item_usb2), MtestConfig.TEST_RESULT_NONE) == MtestConfig.TEST_RESULT_PASS);
        log = checkTotalLog(checkSuccess, log);
        allPass = checkTotalPass(checkSuccess, allPass);

        // Wifi
        // edwin 20201203 add Wifi module -s
        mWifiModule.startWifiScan();
        //checkWifi();
        mPesiSharedPreference.putInt(getString(R.string.str_test_item_wifi), mWifiModule.checkWifiByLongTerm());//checkWifi_by_longterm();//centaur 20200619 fix mtest
        mPesiSharedPreference.save();
        boolean wifiPass = (mPesiSharedPreference.getInt(getString(R.string.str_test_item_wifi), MtestConfig.TEST_RESULT_NONE) == MtestConfig.TEST_RESULT_PASS);
        log += "Wifi .................... ";
        checkSuccess = wifiPass;
        log = checkTotalLog(checkSuccess, log);
        allPass = checkTotalPass(checkSuccess, allPass);
        List<ScanResult> wifiList = new ArrayList<>();
        if (wifiPass) {
            log += "SSID: ";
            wifiList = mWifiModule.getWifiList();
            if (wifiList != null)
            {
                for (ScanResult scResult : wifiList)
                    log = log.concat(scResult.SSID) + ", ";
            }
            log += "\n";
        }
        // edwin 20201203 add Wifi module -e

        // Bluetooth
//        mBluetoothModule.checkBt(2, mPesiSharedPreference);
        checkBt(2);
        boolean btPass = (mPesiSharedPreference.getInt(getString(R.string.str_test_item_bluetooth), MtestConfig.TEST_RESULT_NONE) == MtestConfig.TEST_RESULT_PASS);
        log += "Bluetooth ............... ";
        checkSuccess = btPass;
        log = checkTotalLog(checkSuccess, log);
        allPass = checkTotalPass(checkSuccess, allPass);
        if (btPass) {
            log += "Name/Address: ";
            for (BluetoothModule.BtItem item : mBluetoothModule.getBtItemList()) {
                String name = item.getName();
                String address = item.getAddress();
                if(name != null)//centaur 20200619 fix mtest
                    log = log.concat(name) + "/";
                if(address != null)//centaur 20200619 fix mtest
                    log = log.concat(address) + ", ";
            }
            log += "\n";
        }

        // SD
        checkSdCard(2);
        log += "SD card ................. ";
        checkSuccess = (mPesiSharedPreference.getInt(getString(R.string.str_test_item_sdcard), MtestConfig.TEST_RESULT_NONE) == MtestConfig.TEST_RESULT_WAIT_CARD_OUT);
        log = checkTotalLog(checkSuccess, log);
        allPass = checkTotalPass(checkSuccess, allPass);

        // Ethernet
        checkETH(2);
        log += "Ethernet ................ ";
        checkSuccess = (mPesiSharedPreference.getInt(getString(R.string.str_test_item_ethernet), MtestConfig.TEST_RESULT_NONE) == MtestConfig.TEST_RESULT_PASS);
        log = checkTotalLog(checkSuccess, log);
        allPass = checkTotalPass(checkSuccess, allPass);

        if(allPass) log += "Total : PASS\n";
        else        log += "Total : FAIL\n";
        mLongTermTotalTime += CHECK_STATUS_DELAY;
        if ((mHwTestConfig.getLONG_TERM_TEST() == 1) && (mLongTermTotalTime == LONG_TERN_10_MINS))
        {
            /*  LONG_TERM_TEST
            0:off
            1:Wait 10 mins, show PASS/FAIL */
            mLongTermTotalTime = 0;
            wifiList = new ArrayList<>();
            mBluetoothModule.setBtItemListEmpty();
//            btItemList = new ArrayList<>();
            mHwTestConfig.writeLogLongTerm(log, allPass);
        }
        // Edwin 20200602 for long term test -e
    }

    private void runAutoTests() {
        Log.d(TAG, "runAutoTests: ");
        String item;
        int testResult;

        //Scoty 20190422 modify check lock and play success once
        item = getString(R.string.str_test_item_ntsc_pal_info);//Tuner
        testResult = mPesiSharedPreference.getInt(item, MtestConfig.TEST_RESULT_NONE);
        if (testResult != MtestConfig.TEST_RESULT_PASS) {
            if (mGlobalVars.m_GetIPTVActive()) {
                checkIPTV();
            } else {
                checkTuner();
            }
        }

        item = getString(R.string.str_test_item_hdcp1x);  // HDCP1.x
        if (mEnableItemList.contains(item)) {     // if test exist
            testResult = mPesiSharedPreference.getInt(item, MtestConfig.TEST_RESULT_NONE);
            if (testResult != MtestConfig.TEST_RESULT_PASS) {
                checkHDCP(0);
            }
        }

        item = getString(R.string.str_test_item_hdcp2x);  // HDCP2.2
        if (mEnableItemList.contains(item)) {     // if test exist
            testResult = mPesiSharedPreference.getInt(item, MtestConfig.TEST_RESULT_NONE);
            if (testResult != MtestConfig.TEST_RESULT_PASS) {
                checkHDCP(1);
            }
        }

        item = getString(R.string.str_test_item_usb1);  // USB 1
        if (mEnableItemList.contains(item)) {     // if test exist
            testResult = mPesiSharedPreference.getInt(item, MtestConfig.TEST_RESULT_NONE);
            /* USB_TEST
            0:off
            1:R/W one time,Production mode
            2:Continuous R/W */
            if (testResult != MtestConfig.TEST_RESULT_PASS || mHwTestConfig.getUSB_TEST() == 2) {
                checkUSB(true, UsbModule.PORT_1); // edwin 20200514 implement USB_TEST flag function
            }
        }

        item = getString(R.string.str_test_item_usb2);  // USB 2
        if (mEnableItemList.contains(item)) {     // if test exist
            testResult = mPesiSharedPreference.getInt(item, MtestConfig.TEST_RESULT_NONE);
            /* USB_TEST
            0:off
            1:R/W one time,Production mode
            2:Continuous R/W */
            if (testResult != MtestConfig.TEST_RESULT_PASS || mHwTestConfig.getUSB_TEST() == 2) {
                checkUSB(true, UsbModule.PORT_2); // edwin 20200514 implement USB_TEST flag function
            }
        }

        // Edwin 20200507 add SD Card Test -s
        item = getString(R.string.str_test_item_sdcard);  // SD Card
        if (mEnableItemList.contains(item)) // if test exist
        {
            testResult = mPesiSharedPreference.getInt(item, MtestConfig.TEST_RESULT_NONE);
            /* SD_TEST
            0:off
            1:R/W one time,Production mode
            2:Continuous R/W */
            if (testResult != MtestConfig.TEST_RESULT_PASS || mHwTestConfig.getSD_TEST() == 2) // edwin 20200515 implement SD_TEST flag function
            {
                checkSdCard(mHwTestConfig.getSD_TEST());
            }
        }
        // Edwin 20200507 add SD Card Test -e

        // edwin 20200519 implement ETHERNET_TEST -s
        item = getString(R.string.str_test_item_ethernet);  // ETH
        if (mEnableItemList.contains(item)) {     // if test exist
            testResult = mPesiSharedPreference.getInt(item, MtestConfig.TEST_RESULT_NONE);
            /* ETHERNET_TEST
            0:off
            1:ping one time,Production mode
            2:Continuous ping */
            if (testResult != MtestConfig.TEST_RESULT_PASS || mHwTestConfig.getETHERNET_TEST() == 2) {
                new Thread(new Runnable() { // new thread to ping
                    @Override
                    public void run() {
                        checkETH(mHwTestConfig.getETHERNET_TEST());
                    }
                }).start();
            }
        }
        // edwin 20200519 implement ETHERNET_TEST -e

        //Scoty 20190410 add bt and wifi auto test -s
        item = getString(R.string.str_test_item_bluetooth);//BT
        if(mEnableItemList.contains(item)) {
            testResult = mPesiSharedPreference.getInt(item, MtestConfig.TEST_RESULT_NONE);
            /* BT_TEST
            0:off
            1:connect AP,Production mode
            2:Continuous scan */
            // if 2(Continuous scan) always check
            // others check if not pass
            if (testResult != MtestConfig.TEST_RESULT_PASS || mHwTestConfig.getBT_TEST() == 2) {
                checkBt(mHwTestConfig.getBT_TEST());
            }
        }
        //Scoty 20190410 add bt and wifi auto test -e

        // edwin 20201203 add Wifi module -s
        item = getString(R.string.str_test_item_wifi);  // WIFI
        if (mEnableItemList.contains(item)) {     // if test exist
            testResult = mPesiSharedPreference.getInt(item, MtestConfig.TEST_RESULT_NONE);
            /* WIFI_TEST
            0:off
            1:connect AP,Production mode
            2:Continuous scan */
            if (mHwTestConfig.getWIFI_TEST() == 2)
                mWifiModule.startWifiScan();//startWifiScan();
            if (testResult != MtestConfig.TEST_RESULT_PASS &&  mHwTestConfig.getWIFI_TEST() == 1) { // Edwin 20200504 fix checkWifi slow down mtest//centaur 20200619 fix mtest
                StringBuilder wifiLevel = new StringBuilder();
                int result = mWifiModule.checkWifi(wifiLevel); //checkWifi();

                View WifiView = findViewInMenuByTestItem(getString(R.string.str_test_item_wifi));
                if (WifiView != null) {
                    TextView WifiStatus = (TextView) WifiView.findViewById(R.id.textView_status);
                    WifiStatus.setText(wifiLevel);
                }

                if (result == MtestConfig.TEST_RESULT_PASS)
                    mWifiModule.removeNetwork();

                mPesiSharedPreference.putInt(getString(R.string.str_test_item_wifi), result);
                mPesiSharedPreference.save();
            }
        }
        // edwin 20201203 add Wifi module -e

        item = getString(R.string.str_test_item_smartcard);  // SmartCard
        if (mEnableItemList.contains(item)) {     // if test exist
            testResult = mPesiSharedPreference.getInt(item, MtestConfig.TEST_RESULT_NONE);
            if (testResult != MtestConfig.TEST_RESULT_PASS) {
                checkSmartCard();
            }
        }

        item = getString(R.string.str_test_item_power_saving);  // Power saving
        if (mEnableItemList.contains(item)) {     // if test exist
            testResult = mPesiSharedPreference.getInt(item, MtestConfig.TEST_RESULT_NONE);
            if (!mPowerSavingTested && testResult != MtestConfig.TEST_RESULT_PASS) { // test only once
                checkPowerSaving();
                mPowerSavingTested = true;
            }
        }

        // edwin 20200527 implement Flash Test -s
        /* FLASH_TEST
        0:off
        1:Continuous R/W */
        if (mHwTestConfig.getFLASH_TEST() == 1)//centaur 20200619 fix mtest
        {
            int count = mHwConfigSharedPreference.getInt(HwTestConfig.HW_TEST_CONFIG_FLASH_TOTAL_COUNT, 0);
            int show = mHwConfigSharedPreference.getInt("showdialog", 0);
            Log.d(TAG, "==========flashCount = " + count + "\n");
            if(count > 500) {
                if(show == 0) {
                    String Text = "Flash Test finish";
                    new MessageDialogView(this, Text, 0) {
                        @Override
                        public void dialogEnd() {
                        }
                    }.show();
                    mHwConfigSharedPreference.putInt("showdialog", 1);
                }
            }
            else {
                if(count==1)
                {
                    String Text = "Flash Test start";
                    new MessageDialogView(this, Text, 0) {
                        @Override
                        public void dialogEnd() {
                        }
                    }.show();
                }
                mHwConfigSharedPreference.putInt("showdialog", 0);
                boolean pass = mFlashModule.checkFlash() == MtestConfig.TEST_RESULT_PASS;
                String log = null;

                log = "Flash Test ........ ";
                if (pass) log += "PASS\n";
                else log += "FAIL\n";
                mHwTestConfig.writeFlashTestLog(log, pass);
            }

            mHwConfigSharedPreference.save();
        }
        // edwin 20200527 implement Flash Test -e
        /* DDR_TEST
        0:off
        1:Continuous R/W */
        if (mHwTestConfig.getDDR_TEST() == 1) // edwin 20200527 implement DDR Test
        {
            boolean pass = mDDRModule.checkDDR() == MtestConfig.TEST_RESULT_PASS ;
            String log = null;
            int count = mHwConfigSharedPreference.getInt(HwTestConfig.HW_TEST_CONFIG_DDR_TOTAL_COUNT, 0);
            int show = mHwConfigSharedPreference.getInt("showdialog", 0);
            Log.d(TAG, "\n\n\n==========DDRCount = " + count + "\n");
            if(count > 500) {
                if(show == 0) {
                    Log.d(TAG, "\n\n\n==========DDrCount = " + count + "\n");
                    String Text = "DDR Test finish";
                    new MessageDialogView(this, Text, 0) {
                        @Override
                        public void dialogEnd() {
                        }
                    }.show();
                    mHwConfigSharedPreference.putInt("showdialog", 1);
                }
            }else {
                if(count==1)
                {
                    String Text = "DDR Test start";
                    new MessageDialogView(this, Text, 0) {
                        @Override
                        public void dialogEnd() {
                        }
                    }.show();
                }
                mHwConfigSharedPreference.putInt("showdialog", 0);
                log = "DDR Test ........ ";
                if (pass) log += "PASS\n";
                else log += "FAIL\n";
                mHwTestConfig.writeDDRTestLog(log, pass);
            }

            mHwConfigSharedPreference.save();
        }
    }

    /**
     * @param btTestFlag
     * 0 = off
     * 1 = Connect AP(only find target bt and check RSSI now)
     * 2 = Continuous scan
     */
    private void checkBt (int btTestFlag) // edwin 20200518 implement BT_TEST
    {
        if (btTestFlag == 0) {
            // check we can get bt and is enabled

            if (mBluetoothModule.isBtEnabled())
                mPesiSharedPreference.putInt(getString(R.string.str_test_item_bluetooth), MtestConfig.TEST_RESULT_PASS);
            else
                mPesiSharedPreference.putInt(getString(R.string.str_test_item_bluetooth), MtestConfig.TEST_RESULT_FAIL);

            mPesiSharedPreference.save();
        }
        else if (btTestFlag == 1) {
            // check bt pass
            int testResult = mBluetoothModule.checkBt();

            // show RSSI on UI
            View btView = findViewInMenuByTestItem(getString(R.string.str_test_item_bluetooth));
            if (btView != null) {
                TextView btStatus = (TextView) btView.findViewById(R.id.textView_status);
                String btLevelStr = "RSSI: " + mBluetoothModule.getBtRssi();
                btStatus.setText(btLevelStr);
            }

            // update test result
            if (testResult == MtestConfig.TEST_RESULT_PASS) {
                mPesiSharedPreference.putInt(getString(R.string.str_test_item_bluetooth), MtestConfig.TEST_RESULT_PASS);
            }
            else {
                mPesiSharedPreference.putInt(getString(R.string.str_test_item_bluetooth), MtestConfig.TEST_RESULT_FAIL);
            }

            mPesiSharedPreference.save();
        }
        else if (btTestFlag == 2) {
            // check bt can discovery

            boolean canDiscovery;
            if (!mBluetoothModule.isBtDiscovering()) {
                canDiscovery = mBluetoothModule.startDiscovery();
            }
            else {
                // already discovering
                canDiscovery = true;
            }

            if (canDiscovery) {
                Log.d(TAG, "checkBt: success");
                mPesiSharedPreference.putInt(getString(R.string.str_test_item_bluetooth), MtestConfig.TEST_RESULT_PASS);
            }
            else {
                Log.d(TAG, "checkBt: fail");
                mPesiSharedPreference.putInt(getString(R.string.str_test_item_bluetooth), MtestConfig.TEST_RESULT_FAIL);
            }

            mPesiSharedPreference.save();
        }
    }

    private boolean checkTuner()
    {
        int result = mTunerModule.checkTuner(mGlobalVars.mtunerID, 0);
        mPesiSharedPreference.putInt(getString(R.string.str_test_item_ntsc_pal_info), result);
        mPesiSharedPreference.save();
        return (result == MtestConfig.TEST_RESULT_PASS);
    }

    // TODO: add player module to handle media?
    private boolean checkIPTV() {
        boolean pass = false;

        if (mExoPlayer != null && mExoPlayerIsPlaying) {
            pass = true;
            mPesiSharedPreference.putInt(getString(R.string.str_test_item_ntsc_pal_info), MtestConfig.TEST_RESULT_PASS);
        } else {
            mPesiSharedPreference.putInt(getString(R.string.str_test_item_ntsc_pal_info), MtestConfig.TEST_RESULT_FAIL);
        }

        mPesiSharedPreference.save();
        return pass;
    }

    private void checkHDCP(int version) {
        HdcpModule hdcpModule = new HdcpModule(this);
        int result;
        if (version == 0) {
            result = hdcpModule.checkHdcp1x();
            mPesiSharedPreference.putInt(getString(R.string.str_test_item_hdcp1x), result);
        } else {
            result = hdcpModule.checkHdcp2x();
            mPesiSharedPreference.putInt(getString(R.string.str_test_item_hdcp2x), result);
        }

        mPesiSharedPreference.save();
    }

    /**
     * Test USB by check device mounted or read write file
     * @param rwMode true for read/write test
     * @param port {@link UsbModule#PORT_1} or {@link UsbModule#PORT_2}
     * @return true if pass the test
     * USB_TEST 0 : off
     *          1 : R/W one time,Production mode
     *          2 : Continuous R/W
     */
    private boolean checkUSB (boolean rwMode, int port) // edwin 20200514 implement USB_TEST flag function
    {
        int usbTestFlag = mHwTestConfig.getUSB_TEST();
        int testResult = MtestConfig.TEST_RESULT_FAIL;

        Log.d(TAG, "checkUSB: usbTestFlag = " + usbTestFlag + " , rwMode = " + rwMode + " , port = " + port);
        if (usbTestFlag == 0 || !rwMode)
            testResult = mUsbModule.checkUsbMounted(port);
        else if (usbTestFlag == 1 || usbTestFlag == 2)
            testResult = mUsbModule.checkUsbReadWrite(port);
        else
            Log.d(TAG, "checkUSB: incorrect case , usbTestFlag = " + usbTestFlag + " , rwMode = " + rwMode + " , port = " + port);

        if (port == UsbModule.PORT_1)
            mPesiSharedPreference.putInt(getString(R.string.str_test_item_usb1), testResult);
        else if (port == UsbModule.PORT_2)
            mPesiSharedPreference.putInt(getString(R.string.str_test_item_usb2), testResult);
        mPesiSharedPreference.save();

        return testResult == MtestConfig.TEST_RESULT_PASS;
    }

    private boolean checkETH (int ethTest) // edwin 20200519 implement ETHERNET_TEST
    {
        int result = mEthernetModule.checkEthConnect();

        mPesiSharedPreference.putInt(getString(R.string.str_test_item_ethernet), result);
        mPesiSharedPreference.save();

        return (result == MtestConfig.TEST_RESULT_PASS);
    }

    private void checkSmartCard()
    {
        SmartCardModule scMod = new SmartCardModule(this);
        int result;

        if (mSmartCardAtrFlag)
            result = scMod.checkSmartCard(1);//MtestGetATRStatus(1);
        else
            result = scMod.checkSmartCard(0);//MtestGetATRStatus(0);

        mSmartCardAtrFlag = (result == MtestConfig.TEST_RESULT_WAIT_CARD_OUT);
        mPesiSharedPreference.putInt(getString(R.string.str_test_item_smartcard), result);
        mPesiSharedPreference.save();
    }

    private void checkPowerSaving() {
        PowerSavingModule powerModule = new PowerSavingModule(this);
        int result = powerModule.checkPowerSaving();
        mPesiSharedPreference.putInt(getString(R.string.str_test_item_power_saving), result);
        mPesiSharedPreference.save();
    }

    private void runHiddenTests() {
        if (MtestConfig.isHiddenFunctionEnable(getApplicationContext(), MtestConfig.KEY_EMI)) {
            testEMI();
        }
    }

    private void testEMI() {
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        PesiStorageHelper pesiStorageHelper = new PesiStorageHelper(storageManager);

        List<Integer> pesiUsbPortList = mUsbModule.getPortList();

        for (Object volumeInfo : getVolumes()) {
            int usbPortNumber = pesiStorageHelper.getUsbPortNum(volumeInfo);
            String path = pesiStorageHelper.getPath(volumeInfo);
            int index = pesiUsbPortList.indexOf(usbPortNumber);
            if (index == 0) {   // if first port in port list
                mUsbModule.readWriteUSB(usbPortNumber, path);
                Toast.makeText(this, "Port1 EMI test. Path : " + path, Toast.LENGTH_SHORT).show();
            } else if (index == 1) {  // if second port in port list
                mUsbModule.readWriteUSB(usbPortNumber, path);
                Toast.makeText(this, "Port2 EMI test. Path : " + path, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void overloadUSB(int port) {
        List<Integer> pesiUsbPortList = mUsbModule.getPortList();

        int index = pesiUsbPortList.indexOf(port);
        Toast tUSBError;
        if (index >= 0) {
            tUSBError = Toast.makeText(
                    this,
                    String.format(Locale.getDefault(), "Warning!\nUSB%d overload......", ++index),
                    Toast.LENGTH_SHORT);
        } else {
            tUSBError = Toast.makeText(this,
                    "Warning!\nUSB overload......",
                    Toast.LENGTH_SHORT);
        }

        tUSBError.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        tUSBError.show();
    }

    private void overloadTuner() {
        Toast tTunerError = Toast.makeText(this, "Warning!\nTuner overload......", Toast.LENGTH_SHORT);
        tTunerError.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        tTunerError.show();
    }

    private boolean isDigitKeyCode(int keyCode) {
        return keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9;
    }

    private void checkHiddenInput(int keyCode) {
        String text;

        if (mHiddenInput.length() < 4 && isDigitKeyCode(keyCode)) {
            mHiddenInput += String.valueOf(keyCode - KeyEvent.KEYCODE_0);
        }

        Log.d(TAG, "checkHiddenInput : "+mHiddenInput);
        switch (mHiddenInput) {
            case MtestConfig.HIDDEN_INPUT_HIDDEN_ACTIVITY:
                Intent intentHidden = new Intent(MainActivity.this, HiddenActivity.class);
                startActivity(intentHidden);
                Toast.makeText(this, mHiddenInput, Toast.LENGTH_SHORT).show();
                mHiddenInput = "";
                break;
            case MtestConfig.HIDDEN_INPUT_MTEST_ALL_PASS:
                //Scoty 20190417 add key to pass all test item
                setPcToolWriteFinish(true);//Scoty 20190419 save write pctool finish pass/fail to SharedPreferences
                setAllTestItemPass();
                Toast.makeText(this, mHiddenInput, Toast.LENGTH_SHORT).show();
                mHiddenInput = "";
                break;
            //gary20200504 add HW test config get from usb -s
            case MtestConfig.HIDDEN_INPUT_HW_TEST:
                mHwTestConfig = HwTestConfig.getInstance(this, 0);
                text =
                        HwTestConfig.HW_TEST_CONFIG_ONOFF_STRING + " = " + mHwTestConfig.getHW_TEST_CONFIG_ONOFF() + "\n" +
                                HwTestConfig.HW_TEST_CONFIG_UDP_URL_STRING + " = " + mHwTestConfig.getUDP_URL() + "\n" +
                                //HwTestConfig.STB_IP_STRING + " = " + mHwTestConfig.getSTB_IP() + "\n" + // Edwin 20200511 disable stb ip
                                HwTestConfig.HW_TEST_CONFIG_DEST_IP_STRING + " = " + mHwTestConfig.getDEST_IP() + "\n" +
                                HwTestConfig.HW_TEST_CONFIG_TUNER_TEST_STRING + " = " + mHwTestConfig.getTUNER_TEST() + "\n" +
                                HwTestConfig.HW_TEST_CONFIG_SC_TEST_STRING + " = " + mHwTestConfig.getSC_TEST() + "\n" +
                                HwTestConfig.HW_TEST_CONFIG_USB_TEST_STRING + " = " + mHwTestConfig.getUSB_TEST() + "\n" +
                                HwTestConfig.HW_TEST_CONFIG_WIFI_TEST_STRING + " = " + mHwTestConfig.getWIFI_TEST() + "\n" +
                                HwTestConfig.HW_TEST_CONFIG_BT_TEST_STRING + " = " + mHwTestConfig.getBT_TEST() + "\n" +
                                HwTestConfig.HW_TEST_CONFIG_POWER_TEST_STRING + " = " + mHwTestConfig.getPOWER_TEST() + "\n" +
                                HwTestConfig.HW_TEST_CONFIG_SATA_TEST_STRING + " = " + mHwTestConfig.getSATA_TEST() + "\n" +
                                HwTestConfig.HW_TEST_CONFIG_SD_TEST_STRING + " = " + mHwTestConfig.getSD_TEST() + "\n" +
                                HwTestConfig.HW_TEST_CONFIG_ETHERNET_TEST_STRING + " = " + mHwTestConfig.getETHERNET_TEST() + "\n" +
                                HwTestConfig.HW_TEST_CONFIG_DISEQC_TEST_STRING + " = " + mHwTestConfig.getDISEQC_TEST() + "\n" +
                                HwTestConfig.HW_TEST_CONFIG_LNB_TEST_STRING + " = " + mHwTestConfig.getLNB_TEST() + "\n" +
                                HwTestConfig.HW_TEST_CONFIG_LONG_TERM_TEST_STRING + " = " + mHwTestConfig.getLONG_TERM_TEST() + "\n" +
                                HwTestConfig.HW_TEST_CONFIG_THERMAL_TEST_STRING + " = " + mHwTestConfig.getTHERMAL_TEST() + "\n" +
                                HwTestConfig.HW_TEST_CONFIG_FLASH_TEST_STRING + " = " + mHwTestConfig.getFLASH_TEST() + "\n" +
                                HwTestConfig.HW_TEST_CONFIG_DDR_TEST_STRING + " = " + mHwTestConfig.getDDR_TEST() + "\n";
                new MessageDialogView(this, text, 0) {

                    @Override
                    public void dialogEnd() {

                    }
                }.show();
                mHiddenInput = "";
                break;
            case MtestConfig.HIDDEN_INPUT_HW_COPY_REPORT_FILE:
                mHwTestConfig.CopyReportToUsb();
                mHiddenInput = "";
                Toast.makeText(this, "wait a second", Toast.LENGTH_SHORT).show();
                mHandler.postDelayed(() ->
                                Toast.makeText(MainActivity.this, "copy report to usb", Toast.LENGTH_SHORT).show(),
                        3000);
                break;
            case MtestConfig.HIDDEN_INPUT_HW_RESET:
                mHwTestConfig.HwtestConfigSetNotHWConfig();
                mHiddenInput = "";
                Toast.makeText(this, "reset test flag to factory mode", Toast.LENGTH_SHORT).show();
                break;
            case MtestConfig.HIDDEN_INPUT_HW_DELETE_REPORT_FILE:
                mHwTestConfig.DeleteReportFileInFlash();
                mHiddenInput = "";
                Toast.makeText(this, "delete hw test log", Toast.LENGTH_SHORT).show();
                break;
            case MtestConfig.HIDDEN_INPUT_HW_SHOW_HELP:
                //Toast.makeText(this, "Show Count test", Toast.LENGTH_SHORT).show();
                text = "";
                text += HwTestConfig.HW_TEST_CONFIG_ONOFF_STRING + " : " + mHwTestConfig.getHW_TEST_CONFIG_ONOFF() + "\n";
                text += MtestConfig.HIDDEN_INPUT_HW_COPY_REPORT_FILE + " : copy report to usb\n";
                text += MtestConfig.HIDDEN_INPUT_HW_RESET + " : reset test flag to factory mode\n";
                text += MtestConfig.HIDDEN_INPUT_HW_DELETE_REPORT_FILE + " : delete hw test log\n";
                text += mHwTestConfig.getTestCount();
                new MessageDialogView(this, text, 0) {

                    @Override
                    public void dialogEnd() {

                    }
                }.show();
                mHiddenInput = "";
                break;
            //gary20200504 add HW test config get from usb -e
            case MtestConfig.HIDDEN_INPUT_MTEST_DISABLE_OPT:
                Toast.makeText(this, mHiddenInput, Toast.LENGTH_SHORT).show();
                if (mEnableOpt) {
                    mEnableOpt = false;
                    String newVer = mTvVer.getText().toString().replace("Sec", "NonSec");
                    mTvVer.setText(newVer);
                }

                mHiddenInput = "";
                break;
            case MtestConfig.HIDDEN_INPUT_CLEAR_WIFI:
                Toast.makeText(this, mHiddenInput + " Clear Wifi Config", Toast.LENGTH_SHORT).show();
                WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
                if (wifiManager != null) {
                    List<WifiConfiguration> configList = wifiManager.getConfiguredNetworks();
                    if (configList != null) {
                        for (WifiConfiguration config : configList) {
                            wifiManager.removeNetwork(config.networkId);
                        }
                    }
                }

                mHiddenInput = "";
                break;
            // Johnny 20201202 add hidden input to make all items selectable
            case MtestConfig.HIDDEN_INPUT_ALL_ITEM_SELECTABLE:
                Toast.makeText(this, mHiddenInput + " All items selectable", Toast.LENGTH_SHORT).show();
                setMenuListItemsSelectable(mListViewL);
                setMenuListItemsSelectable(mListViewR);
                mHiddenInput = "";
                break;
            case MtestConfig.HIDDEN_INPUT_START_POWER_SAVING:
                Toast.makeText(this, mHiddenInput + " Start Power Saving", Toast.LENGTH_SHORT).show();
                // Decide how to go power save in module
                PowerSavingModule powerModule = new PowerSavingModule(this);
                powerModule.powerSave();
                mHiddenInput = "";
                break;
            // Johnny 20201211 switch locale by hidden input
            case MtestConfig.HIDDEN_INPUT_SWITCH_LOCALE:
                Toast.makeText(this, mHiddenInput + " Switch Locale", Toast.LENGTH_SHORT).show();
                // switch locale
                String curLangCode = LocaleHelper.getLanguage(getApplicationContext());
                if (curLangCode.equals(LOCALE_EN)) {
                    LocaleHelper.setLocale(this, LOCALE_ZH);
                } else {
                    LocaleHelper.setLocale(this, LOCALE_EN);
                }

                mHiddenInput = "";
                recreate();
                break;
            case MtestConfig.HIDDEN_INPUT_HIDDEN_INPUT_ACTIVITY:
                Toast.makeText(this, mHiddenInput + " Show hidden inputs", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, HiddenInputActivity.class);
                // TODO: use new Activity Results API in the future?
                startActivityForResult(intent, REQUEST_CODE_HIDDEN_INPUT);

                mHiddenInput = "";
                break;
            case MtestConfig.HIDDEN_INPUT_DURABILITY_TEST:
                if (mGlobalVars.m_GetIPTVActive()) {
                    Toast.makeText(this, mHiddenInput + " Durability test start", Toast.LENGTH_SHORT).show();
                    // remove all runnable related to durability test
                    mHandler.removeCallbacks(mDurabilityLedRunnable);
                    mHandler.removeCallbacks(mDurabilityPlayRunnable);
                    mHandler.removeCallbacks(mDurabilityStopRunnable);

                    // start led and play
                    mHandler.post(mDurabilityLedRunnable);
                    mHandler.post(mDurabilityPlayRunnable);
                }
                else {
                    Toast.makeText(this, mHiddenInput + " Durability test only support IPTV for now", Toast.LENGTH_SHORT).show();
                }

                mHiddenInput = "";
                break;
            default:  // Johnny 20190613 add timeout to reset hidden input
                mHandler.removeCallbacks(mResetHiddenInputRunnable);
                mHandler.postDelayed(mResetHiddenInputRunnable, HIDDEN_INPUT_TIMEOUT);
                break;
        }

        if (mHiddenInput.length() >= 4)
        {
            Toast.makeText(this, "Error Hidden Input: " + mHiddenInput, Toast.LENGTH_SHORT).show();
            mHiddenInput = "";
        }
    }

    private void checkHiddenInputByStr(String hiddenInput) {
        if (hiddenInput != null) {
            mHiddenInput = hiddenInput;
            checkHiddenInput(KeyEvent.KEYCODE_UNKNOWN); // KEYCODE_UNKNOWN will be ignored
        }
    }

    private String checkTotalLog(boolean itemCheck,String log) {
        String NewLineStr = "\n";
        if(itemCheck) {
            log += BOOT_STABLE_PASS;
        }
        else {
            log += BOOT_STABLE_FAIL;
        }
        log += NewLineStr;
        return log;
    }
    private boolean checkTotalPass(boolean itemCheck,boolean oldTotalPass) {
        boolean pass = true;
        if(itemCheck) {
            pass = true;
        }
        else {
            pass = false;
        }
        return (pass & oldTotalPass);
    }

    private void testWrite(File file, String str)
    {
        OutputStream os;
        try {
            os = new FileOutputStream(file, false);
            os.write(str.getBytes());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test SD card by check device mounted or read write file
     * @param sdFlag 0:off
     *               1:R/W one time,Production mode
     *               2:Continuous R/W
     * @return true if pass SD card test
     */
    private boolean checkSdCard(int sdFlag)
    {
        Log.d(TAG, "checkSdCard: sdFlag = " + sdFlag);

        if (sdFlag == 0)
        {
            int testResult = mSDcardModule.checkStatus();
            mPesiSharedPreference.putInt(getString(R.string.str_test_item_sdcard), testResult);
            mPesiSharedPreference.save();
            return testResult == MtestConfig.TEST_RESULT_WAIT_CARD_OUT;
        }
        else if (sdFlag == 1 || sdFlag == 2)
            return checkSdReadWrite(sdFlag);//centaur 20200619 fix mtest
        else
            return false;
    }

    private boolean checkSdReadWrite(int sdFlag) // edwin 20201216 fix wrong return value //centaur 20200619 fix mtest
    {
        int count = mHwConfigSharedPreference.getInt(HwTestConfig.HW_TEST_CONFIG_SD_TOTAL_COUNT, 0);
        int result = mSDcardModule.checkSdCardRW();

        boolean sdCardPass = result == MtestConfig.TEST_RESULT_PASS ||
                result == MtestConfig.TEST_RESULT_WAIT_CARD_OUT;

        if (count == 1 && sdFlag == 2)
        {
            String Text = "sdcard Test start";
            new MessageDialogView(this, Text, 0) {
                @Override
                public void dialogEnd() {
                }
            }.show();
        }
        String log = null;
        log = "SD Test ........ ";
        if (sdCardPass) log += "PASS\n";
        else log += "FAIL\n";
        mHwTestConfig.writeSDTestLog(log, sdCardPass);

        mPesiSharedPreference.putInt(getString(R.string.str_test_item_sdcard), result);
        mPesiSharedPreference.save();
        return result == MtestConfig.TEST_RESULT_PASS;
    }
    // edwin 20200515 implement SD_TEST flag function -e

    private void runBootStableTest() {
        String NewLineStr = "\n";
        boolean totalResultPass = true;
        boolean TunerSuccess = true, checkSuccess = true;

        if (mGlobalVars.m_GetIPTVActive()) {
            TunerSuccess = checkIPTV();
        } else {
            TunerSuccess = checkTuner();
        }

        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//        EthernetManager mEthManager = (EthernetManager) getSystemService(Context.ETHERNET_SERVICE); // @hide, find another method

//        String str = String.valueOf(bootCount) + "================================";
        String str = "================================";
        String itemPass = "";
        str += NewLineStr;
        //tuner
//        str += "Tuner ................... ";
//        str = checkTotalLog(TunerSuccess,str);
//        totalResultPass = checkTotalPass(TunerSuccess,totalResultPass);
        //usb
        checkUSB(false, UsbModule.PORT_1); // edwin 20200514 implement USB_TEST flag function
        int usb1testResult = mPesiSharedPreference.getInt(getString(R.string.str_test_item_usb1), MtestConfig.TEST_RESULT_NONE);
        str += "Usb 1 ................... ";
        checkSuccess = usb1testResult == MtestConfig.TEST_RESULT_PASS;
        str = checkTotalLog(checkSuccess,str);
        totalResultPass = checkTotalPass(checkSuccess,totalResultPass);
        checkUSB(false, UsbModule.PORT_2);
        int usb2testResult = mPesiSharedPreference.getInt(getString(R.string.str_test_item_usb2), MtestConfig.TEST_RESULT_NONE);
        str += "Usb 2 ................... ";
        checkSuccess = usb2testResult == MtestConfig.TEST_RESULT_PASS;
        str = checkTotalLog(checkSuccess,str);
        totalResultPass = checkTotalPass(checkSuccess,totalResultPass);
        //sdcard
        str += "SD Card ................. ";
        checkSuccess = checkSdCard(0); // edwin 20200515 implement SD_TEST flag function
        str = checkTotalLog(checkSuccess,str);
        totalResultPass = checkTotalPass(checkSuccess,totalResultPass);
        //bt
        str += "Bt ...................... ";
        checkSuccess = mBluetoothModule.isBtEnabled();
        str = checkTotalLog(checkSuccess,str);
        totalResultPass = checkTotalPass(checkSuccess,totalResultPass);
        //wifi
        str += "Wifi .................... ";
        checkSuccess = wifi.isWifiEnabled();
        str = checkTotalLog(checkSuccess,str);
        totalResultPass = checkTotalPass(checkSuccess,totalResultPass);
        //ethernet
        str += "Ethernet ................ ";
        checkSuccess = /*mEthManager.isAvailable() &*/ checkETH(1); // @hide, find another method
        str = checkTotalLog(checkSuccess,str);
        totalResultPass = checkTotalPass(checkSuccess,totalResultPass);
        //HDMI
        str += "Hdmi .................... ";
        checkSuccess = isHdmiSwitchSet();
        str = checkTotalLog(checkSuccess,str);
        totalResultPass = checkTotalPass(checkSuccess,totalResultPass);
        if(totalResultPass)//Scoty 20190417 modify boot stable test should write all test results in report
            str += "Total : " + BOOT_STABLE_PASS;
        else
            str += "Total : " + BOOT_STABLE_FAIL;
        str += NewLineStr;
        mHwTestConfig.writeLogBootStable(str,totalResultPass);
        Toast.makeText(this, "runBootStableTest done !!", Toast.LENGTH_SHORT).show();
        /* POWER_TEST
        0:off
        1:Tool, Production mode
        2:SW reboot
        3:STR
        4:Reset Default */
        if(mHwTestConfig.getPOWER_TEST() == 2)
            mHwTestConfig.reboot();
    }

    private void runBootTunerSearch()
    {
        // Edwin 20200511 play video at tuner search -s
        if(mGlobalVars.m_GetIPTVActive())
        {
            if(mCheckSignalRunnable != null)
                mHandler.removeCallbacks(mCheckSignalRunnable);

            runExoPlayer(); // edwin 20200507 play at start
        }
        else
        {
            TpInfo tpInfo = mTunerModule.TpInfoGet(mGlobalVars.mtpID); // TpInfoGet(mGlobalVars.mtpID);
            if (GetCurTunerType() == TpInfo.DVBC)
            {
                tpInfo.CableTp.setFreq(mBootStableTestTpDVBC[0] * 1000);
                tpInfo.CableTp.setSymbol(mBootStableTestTpDVBC[1]);
                tpInfo.CableTp.setQam(mBootStableTestTpDVBC[2]);
            }
            else if (GetCurTunerType() == TpInfo.ISDBT)
            {
                tpInfo.TerrTp.setFreq(mBootStableTestTpISDBT[0]);
                tpInfo.TerrTp.setBand(mBootStableTestTpISDBT[1]);
            }
            else if (GetCurTunerType() == TpInfo.DVBS)
            {
                SatInfo satInfo = SatInfoGet(tpInfo.getSatId());
                int actualFreqDVBS = getActualFreqDVBS(mBootStableTestTpDVBS[0], satInfo);

                tpInfo.SatTp.setFreq(actualFreqDVBS);
                tpInfo.SatTp.setSymbol(mBootStableTestTpDVBS[1]);
            }

            mTunerModule.TpInfoUpdate(tpInfo); //TpInfoUpdate(tpInfo);
            mTunerModule.ScanParamsStartScan(
                    mGlobalVars.mtunerID,                   // tunerID is tunerIndex for now
                    tpInfo.getTpId(),
                    tpInfo.getSatId(),
                    TVScanParams.SCAN_MODE_MANUAL,
                    TVScanParams.SEARCH_OPTION_TV_ONLY,     // scan only TV program
                    TVScanParams.SEARCH_OPTION_FTA_ONLY,    // scan no ca program
                    0,
                    0
            );
            mIsSearching = true;
        }
        // Edwin 20200511 play video at tuner search -e
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

    // convert from input to actual freq accepted for service ch search
    // work in lnb type = universal
    private int getActualFreqDVBS(int inputFreq, SatInfo satInfo) {
        if (satInfo == null) {
            Log.d(TAG, "getActualFreq: Null SatInfo!");
            return -1;
        }

        int lnb1 = satInfo.Antenna.getLnb1();
        int lnb2 = satInfo.Antenna.getLnb2();
        int actualFreq = inputFreq + lnb1;

        if (actualFreq >= 11700) {   // by Hisi code, type = universal
            actualFreq = inputFreq + lnb2;
        }

        return actualFreq;
    }

    private void updateSignalLevel(int tunerID)
    {
        String ber;
        int errorFrameCount;
        int frameDropCount;

        ber = mTunerModule.getBER(tunerID); //TunerGetBER(tunerID);
        errorFrameCount = mTunerModule.getErrorFrameCount(tunerID); //MtestGetErrorFrameCount(tunerID);
        frameDropCount = mTunerModule.getFrameDropCount(tunerID); //MtestGetFrameDropCount(tunerID);

//        Log.d(TAG, "updateSignalLevel  tunerID = " + tunerID + " BER = " + ber + " ErrFrameCount = " + errorFrameCount + " FrameDropCount = " + frameDropCount);

        mTvErrorStatus.setText(String.format(Locale.getDefault(), "BER = %s\nErrorFrameCount = %d\nFrameDropCount = %d", ber, errorFrameCount, frameDropCount));
    }

    private void CheckVersion() {//Scoty 20181123 add check Apk and Service Version
//        if (!HiDtvMediaPlayer.hasService()) // edwin 20200513 add none server function//centaur 20200619 fix mtest
//            return;
//
//        String[] apkVersion = GetApkSwVersion().split("\\.");
//        String[] serviceVersionSplit = GetPesiServiceVersion().split("V");
//        String[] serviceVersion = serviceVersionSplit[1].split("\\.");
//
//        if (apkVersion[1].equals(serviceVersion[0]) ||
//                Integer.valueOf(apkVersion[1]) == 0)//check service version
//        {
//            if (Integer.valueOf(serviceVersion[1]) <= Integer.valueOf(apkVersion[2]))//check api version
//                return;
//        }
//
//        new MessageDialog(this, 0) {
//            public void onSetMessage(View v) {
//                String Text = getString(R.string.STR_PLEASE_CHECK_APK_AND_SERVICE_VERSION) + "\n" +
//                        getString(R.string.STR_SW_VERSION) + " : " + GetApkSwVersion() + "\n" +
//                        getString(R.string.STR_SERVICE_VERSION) + " : " + GetPesiServiceVersion();
//                ((TextView) v).setText(Text);
//            }
//
//            public void onSetNegativeButton() {
//                finish();
//            }
//
//            public void onSetPositiveButton(int status) {
//                finish();
//            }
//
//            public void dialogEnd(int status) {
//                finish();
//            }
//        };
    }

    // Johnny 20190503 show ota error msg
    private void showOTAErrorDialog(final String message) {
        new MessageDialogView(this, message, getResources().getInteger(R.integer.MESSAGE_DIALOG_DELAY)) {
            public void dialogEnd() {
            }
        }.show();
    }

    // Johnny 20190620 show ota error msg by return value
    private void showOTAErrorDialog(final int otaReturnValue) {
        // modify STR_ARRAY_OTA_RETURN_ERROR_MSG if error codes in service are changed
        String[] msgArray = getResources().getStringArray(R.array.STR_ARRAY_OTA_RETURN_ERROR_MSG);
        String message = "OTA fail error code: " + otaReturnValue + "\n";

        if (otaReturnValue >= 0 && otaReturnValue < msgArray.length) {
            message += msgArray[otaReturnValue];
        }
        else {
            message += getString(R.string.str_ota_unknown_err);
        }

        new MessageDialogView(this, message, getResources().getInteger(R.integer.MESSAGE_DIALOG_DELAY)) {
            public void dialogEnd() {
            }
        }.show();
    }

    private boolean isHdmiSwitchSet() {

//        // if pesi service supports, use this
//        int status = MtestGetHDMIStatus();
//        Log.d(TAG, "isHdmiSwitchSet: " + status);
//        if (status == 1)
//            return true;
//        else
//            return false;

        // The file '/sys/devices/virtual/switch/hdmi/state' holds an int -- if it's 1 then an HDMI device is connected.
        // An alternative file to check is '/sys/class/switch/hdmi/state' which exists instead on certain devices.
        File switchFile = new File("/sys/devices/virtual/switch/hdmi/state");
        Log.d(TAG, "1switchFile.exists(): "+switchFile.exists());
        if (!switchFile.exists()) {
            switchFile = new File("/sys/class/switch/hdmi/state");
            Log.d(TAG, "2switchFile.exists(): "+switchFile.exists());
        }
        try {
            Scanner switchFileScanner = new Scanner(switchFile);
            int switchValue = switchFileScanner.nextInt();
            Log.d(TAG, "switchValue: "+ switchValue);
            switchFileScanner.close();
            return switchValue > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private String getMtestApkVersion() {
        String apkVersion = getString(R.string.str_mtest_ver);
        if (apkVersion.contains(":")) {
            apkVersion = apkVersion.split(":")[1];
        }

        return apkVersion;
    }

    private void checkVideo() // Tuner Test
    {
        if (mGlobalVars.m_GetIPTVActive()) {
            String orgUrl = mGlobalVars.m_GetCurVideoUrl();
            Log.d(TAG, "checkVideo: orgUrl = " + orgUrl);
            if (mHwTestConfig == null || mExoPlayer == null) {
                Log.d(TAG, "checkVideo: mHwTestConfig = " + mHwTestConfig
                        + " , exoPlayer = " + mExoPlayer);
                return;
            }

            mGlobalVars.m_SetCurVideoUrl(mHwTestConfig.getUDP_URL());
            if (!orgUrl.equals(mGlobalVars.m_GetCurVideoUrl())) {
                Log.d(TAG, "checkVideo: url is changed");
                runExoPlayer();
            }

            checkIPTV();
        } else {
            Log.d(TAG, "checkVideo: tuner !!! ");
            checkTuner();
        }
    }

    // TODO: add player module to handle media?
    // edwin 20200507 play at start -s
    private void initExoPlayer()
    {
        Log.d(TAG, "initExoPlayer: isFileFound = " + mHwTestConfig.isFileFound() + " , UDP_URL = " + mHwTestConfig.getUDP_URL());
        mGlobalVars.m_SetCurVideoUrl(mHwTestConfig.getUDP_URL());

        // edwin 20200429 for playing stream -s
        // edwin 20200429 for playing stream
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory());
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        mExoPlayer.setVideoSurfaceView(mSurfaceViewExoplayer);
//        mPlayerView.setPlayer(mExoPlayer);
//        mPlayerView.setKeepContentOnPlayerReset(true);
//        mPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
//        mPlayerView.hideController();
//        mPlayerView.setControllerAutoShow(false);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // edwin 20200429 for playing stream -e
    }

    // TODO: add player module to handle media?
    private void runExoPlayer()
    {
        Uri videoUri = Uri.parse(mGlobalVars.m_GetCurVideoUrl());
        Log.d(TAG, "runExoPlayer: Url = " + mGlobalVars.m_GetCurVideoUrl() +
                " , udp exist = " + mGlobalVars.m_GetCurVideoUrl().contains("udp"));

        if (mGlobalVars.m_GetCurVideoUrl().contains("usb://")) //centaur 20200805 add apk player test
        {
            Uri uri = null;
            String filename = null;
            StorageManager mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
            filename = mGlobalVars.m_GetCurVideoUrl().substring(6 , mGlobalVars.m_GetCurVideoUrl().length());
            PesiStorageHelper pesiStorageHelper = new PesiStorageHelper(mStorageManager);
            Log.d(TAG, "runExoPlayer filename = " + filename);
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
        else if (mGlobalVars.m_GetCurVideoUrl().contains("asset:///"))
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
    // edwin 20200507 play at start -e

    private void initModule()
    {
        mUsbModule = new UsbModule(this);
        mBluetoothModule = new BluetoothModule(this);
        mWifiModule = new WifiModule(this);
        mTunerModule = new TunerModule(this);
        mSDcardModule = new SDcardModule(this);
        mDDRModule = new DDRModule();
        mEthernetModule = new EthernetModule(this);
        mFlashModule = new FlashModule(this);
        mLedModule = new LedModule(this);
    }

    private void releaseModule()
    {
        WifiModule.destroy(this);
        mUsbModule = null;
        mBluetoothModule = null;
        mWifiModule = null;
        mTunerModule = null;
        mSDcardModule = null;
        mDDRModule = null;
        mEthernetModule = null;
        mFlashModule = null;
        mLedModule = null;
    }

    private void resetModule()
    {
        Log.d(TAG, "resetModule: ");
        mSDcardModule.reset(); // edwin 20201216 fix wrong return value
    }

    private boolean updateHwTestFlag()
    {
        boolean updated = false;
        try {
            for (Object vol : getVolumes())
                updated |= mHwTestConfig.getHwTestConfigFromUSB(vol);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return updated;
    }

    private void runResetDefaultTest()
    {
        int RESET_DEFAULT_DELAY = 15000;

        // STOP Reset Default by OK key
        int successCount = mHwTestConfig.writeLogByResetDefault();
        new MessageDialogView(this,
                "start Reset Default Test after " + (RESET_DEFAULT_DELAY/1000) + " seconds ...\n" +
                        "success count = " + successCount + "\n" +
                        "press OK to stop", RESET_DEFAULT_DELAY)
        {
            Runnable mRunnable;
            int mDelay = RESET_DEFAULT_DELAY;

            @Override
            public void dialogEnd()
            {
                // DO NOT REBOOT
            }

            @Override
            public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
                {
                    Toast.makeText(MainActivity.this, "stop Reset Default Test ...", Toast.LENGTH_LONG).show();
                    mHandler.removeCallbacks(mRunnable);
                    dismiss();
                    return true;
                }
                return super.onKeyDown(keyCode, event);
            }

            @Override
            protected void onStart()
            {
                super.onStart();
                mRunnable = () -> {
                    setText("start Reset Default Test after " + ((mDelay -= 1000) / 1000) + " seconds ...\n" +
                            "success count = " + successCount + "\n" +
                            "press OK to stop");

                    if (mDelay == 0)
                    {
                        Log.d(TAG, "runResetDefaultTest: start Reset Default Test ...");
                        mHwTestConfig.resetDefault();
                    }
                    else
                    {
                        Log.d(TAG, "runResetDefaultTest: mDelay = " + mDelay);
                        mHandler.postDelayed(mRunnable, 1000);
                    }
                };
                runOnUiThread(mRunnable);
            }
        }.show();
    }

    private void openwifi()//Scoty 20190417 open wifi when open mtest page
    {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        if(wifiManager != null) {
            if (!wifiManager.isWifiEnabled())
                wifiManager.setWifiEnabled(true);
        }
    }

    private void setAllTestItemPass()//Scoty 20190417 add key to pass all test item
    {
        for (String item : mEnableItemList) {
            mPesiSharedPreference.putInt(item, MtestConfig.TEST_RESULT_PASS);
        }

        mPesiSharedPreference.save();

        mHandler.removeCallbacks(mCheckStatusRunnable);
        mCheckStatusRunnable = null;
        updateTestResultDisplay();
    }

    private boolean checkAllItemPass()
    {
        boolean pass = true;

        for (String item : mEnableItemList) {
            int testResult = mPesiSharedPreference.getInt(item, MtestConfig.TEST_RESULT_NONE);
            if (testResult != MtestConfig.TEST_RESULT_PASS) {
                pass = false;
                break;
            }
        }

        return pass;
    }

    //Scoty 20190419 save write pctool finish pass/fail to SharedPreferences -s
    private void setPcToolWriteFinish(boolean value)
    {
        mPesiSharedPreference.putBoolean(FINISH_PCTOOL, value);
        mPesiSharedPreference.save();
    }

    private boolean getPcToolWriteFinish()
    {
        return mPesiSharedPreference.getBoolean(FINISH_PCTOOL, false);
    }
    //Scoty 20190419 save write pctool finish pass/fail to SharedPreferences -e
}
