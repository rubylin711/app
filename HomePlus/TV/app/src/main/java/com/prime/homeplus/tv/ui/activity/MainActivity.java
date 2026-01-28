package com.prime.homeplus.tv.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.tvprovider.media.tv.Channel;
import androidx.tvprovider.media.tv.Program;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.media.tv.TvContentRating;
import android.media.tv.TvInputManager;
import android.media.tv.TvTrackInfo;
import android.media.tv.TvView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceInterface;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.SurfaceView;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.FavGroup;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.utils.LogUtils;
import com.prime.datastructure.utils.TVMessage;
import com.prime.homeplus.tv.BuildConfig;
import com.prime.homeplus.tv.PrimeHomeplusTvApplication;
import com.prime.homeplus.tv.R;
import com.prime.homeplus.tv.Ticker.CNSTickerManager;
import com.prime.homeplus.tv.Ticker.TickerView;
import com.prime.homeplus.tv.adapter.ChannelSearchAdapter;
import com.prime.homeplus.tv.adapter.LeftChannelListAdapter;
import com.prime.homeplus.tv.adapter.NumberKeyChannelAdapter;
import com.prime.homeplus.tv.data.GlobalState;
import com.prime.homeplus.tv.data.ScheduledProgramData;
import com.prime.homeplus.tv.data.network.VDC;
import com.prime.homeplus.tv.error.ErrorMessageResolver;
import com.prime.homeplus.tv.manager.CurrentChannelListManager;
import com.prime.homeplus.tv.manager.LockManager;
import com.prime.homeplus.tv.manager.NowPlayingManager;
import com.prime.homeplus.tv.data.GenreData;
import com.prime.homeplus.tv.manager.UnlockStateManager;
import com.prime.homeplus.tv.manager.VoiceSearchManager;
import com.prime.homeplus.tv.utils.PrimeUtils;
import com.prime.homeplus.tv.utils.ProgramReminderUtils;
import com.prime.homeplus.tv.utils.ScheduledProgramUtils;
import com.prime.homeplus.tv.service.ProgramReminderService;
import com.prime.homeplus.tv.ui.component.SimpleRecordingSettingPopup;
import com.prime.homeplus.tv.ui.fragment.EpgFragment;
import com.prime.homeplus.tv.ui.fragment.ParentalPinDialogFragment;
import com.prime.homeplus.tv.ui.fragment.LiveSignalInfoFragment;
import com.prime.homeplus.tv.utils.ChannelUtils;
import com.prime.homeplus.tv.utils.ProgramUtils;
import com.prime.homeplus.tv.utils.QrCodeUtils;
import com.prime.homeplus.tv.utils.ReportExporter;
import com.prime.homeplus.tv.utils.TimeUtils;
import com.prime.homeplus.tv.utils.StringUtils;
import com.prime.homeplus.tv.utils.TvViewUtils;
import com.prime.homeplus.tv.utils.ViewUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.prime.homeplus.tv.manager.ADHelper;
import com.prime.homeplus.tv.event.EventEpgUpdate;
import com.prime.homeplus.tv.data.ADData;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.File;
import android.net.Uri;

public class MainActivity extends AppCompatActivity implements EpgFragment.EpgPlaybackManager {
    private static final String TAG = "HOMEPLUS_TV";

    private static final String MAIN_PARENTAL_PIN_DIALOG_TAG = "ParentalPinDialog";

    private CurrentChannelListManager currentChannelListManager;
    private NowPlayingManager nowPlayingManager;
    private UnlockStateManager currentUnlockStateManager;
    private LockManager lockManager;

    private ChannelSearchAdapter channelSearchAdapter;
    private NumberKeyChannelAdapter numberKeyChannelAdapter;
    private LeftChannelListAdapter leftChannelListAdapter;

    private Handler handler = new Handler();

    private String inputNumberBuffer = "";
    private static final int MAX_INPUT_NUMBER_LENGTH = 3;
    private static final long INPUT_NUMBER_TIMEOUT_MS = 3 * 1000;
    private static final long AUTO_HIDE_MINIEPG_TIMEOUT_MS = 5 * 1000;
    private static final long REFRESH_LEFT_CHANNEL_LIST_PERIOD_MS = 10 * 1000;
    private static final long UPDATE_TIMER_PERIOD_MS = 10 * 1000;

    // 000blue signal info page
    private static final int[] SIGNAL_INFO_SEQUENCE = {
            KeyEvent.KEYCODE_0,
            KeyEvent.KEYCODE_0,
            KeyEvent.KEYCODE_0,
            KeyEvent.KEYCODE_PROG_BLUE
    };
    private List<Integer> keySequenceBuffer = new ArrayList<>();

    private final Runnable updateTimerRunnable = this::updateTimer;
    private final Runnable hideMiniEpgRunnable = this::hideMiniEpg;
    private final Runnable inputNumberBufferTimeoutRunnable = this::inputNumberBufferTimeout;
    private final Runnable refreshLeftChannelListRunnable = this::refreshLeftChannelList;
    private static final long FCC_SURFACE_RETRY_MS = 300;
    private static final int FCC_SURFACE_COUNT = 3;
    private boolean mFccSurfaceRetryScheduled = false;
    private final Runnable mFccSurfaceRetryRunnable = this::resendFccSurfacesIfReady;

    // TvView
    private String lastInputId = null;
    private int lastReason = -1;
    private long lastTime = 0;
    private static final long MIN_INTERVAL_MS = 50;
    private List<TvTrackInfo> audioTracks = new ArrayList<>();
    private List<TvTrackInfo> subtitleTracks = new ArrayList<>();
    private String selectedAudioTrackId = "", currentAudioTrackId = "", selectSettingLang = "",
            currentSelectSettingLang = "";

    // TV Frame
    private TvView mTvView;
    private ConstraintLayout clTvFrame, include_clEpgTvFrame;
    private ImageView ivEpgTvFrameBlock, ivEpgTvFrameRecordingStatus;
    private TextView tvEpgTvFrameProgramName;
    private int mCurrentSubtitleIndex = -1, selectedSubtitleTrackIndex = -1;

    // FCC Z-Order
    private FrameLayout mFccContainer;
    private final SurfaceView[] mFccSurfaceViews = new SurfaceView[FCC_SURFACE_COUNT];

    private static final class FccSurfaceSlot {
        private Surface surface;
        private boolean valid;
        private int width;
        private int height;
        private int surfaceId;
        private long lastChangedUptime;
    }

    private final FccSurfaceSlot[] mFccSlots = new FccSurfaceSlot[FCC_SURFACE_COUNT];
    private int mPendingPromoteIdx = -1;
    private String mPendingPromoteReason = null;
    private long mPendingPromoteUptime = 0;
    
    // Main - Overlay (QrEvent / ChannelBlock / Music)
    public enum LiveOverlayState {
        ALL,
        MUSIC,
        QR_ERROR_EVENT,
        CHANNEL_BLOCK
    }

    private ConstraintLayout include_clLiveOverlay, clLiveOverlayMusic, clLiveOverlayQrErrorEvent,
            clLiveOverlayChannelBlock;
    private ImageView ivLiveOverlayMusicAnimation, ivLiveOverlayQrCode, ivLiveOverlayPadlockAnimation;
    private AnimationDrawable adLiveOverlayMusicAnimation, adLiveOverlayPadlockAnimation;
    private TextView tvLiveOverlayQrErrorCode, tvLiveOverlayQrErrorMessage;

    // Main - RecordingStatus
    private ConstraintLayout include_clLiveRecordingStatus;
    private TextView tvLiveRecordingStatusChannel1, tvLiveRecordingStatusPipe, tvLiveRecordingStatusChannel2;

    // Main - ChannelSearch
    private RelativeLayout include_rlLiveChannelSearch, rlLiveChannelSearchVoice, rlLiveChannelSearchBottomMessage;
    private LinearLayout llLiveChannelSearch, llLiveChannelSearchMessage;
    private Button btnLiveChannelSearchVoice, btnLiveChannelSearch;
    private EditText etLiveChannelSearch, etLiveChannelSearchVoice;
    private RecyclerView rvLiveChannelSearchList;

    // Main - MiniEpg
    public enum MiniEpgState {
        MAIN,
        MUSIC,
        ADULT_CHANNEL_LOCK,
        PARENTAL_CHANNEL_LOCK,
        PARENTAL_PROGRAM_LOCK,
        WORK_HOUR_LOCK
    }

    private EnumMap<MiniEpgState, View> miniEpgViewMap = new EnumMap<>(MiniEpgState.class);

    private ConstraintLayout include_clMiniEpg;
    private LinearLayout llMiniEpgNowProgramParentalControlLock, llMiniEpgNowProgramWorkHourLock,
            llMiniEpgNowProgramChannelLock, llMiniEpgNowProgramAdultChannel,
            llMiniEpgNowMusic, llMiniEpgNowProgramMain, llMiniEpgNowProgram, llMiniEpgGreenKey,
            llMiniEpgBlueKey;
    private TextView tvMiniEpgChannelNumberTop, tvMiniEpgChannelNumber, tvMiniEpgChannelName, tvMiniEpgDate,
            tvMiniEpgNowProgram,
            tvMiniEpgNowProgramTime, tvMiniEpgBilingual, tvMiniEpgSubtitle, tvMiniEpgNextProgram,
            tvMiniEpgRedKey, tvMiniEpgBilingualNow, tvMiniEpgYellowKey, tvMiniEpgSubtitleNow,
            tvMiniEpgOk;
    private ImageView ivMiniEpgLock, ivMiniEpgMusic, ivMiniEpgStar, ivMiniEpgQuality,
            ivMiniEpgDolby, ivMiniEpgNowRating, ivMiniEpgNowRecord, ivMiniEpgNext,
            ivMiniEpgNextReminder, ivMiniEpgNextRecord, ivMiniEpgAd;
    private ProgressBar pbMiniEpgProgram;

    // Main - MiniEpgDescription
    private ConstraintLayout include_clMiniEpgDescription;
    private TextView tvMiniEpgDescriptionNumber, tvMiniEpgDescriptionChannelName, tvMiniEpgDescriptionProgramName,
            tvMiniEpgDescriptionBilingual, tvMiniEpgDescriptionProgramStart, tvMiniEpgDescriptionProgramEnd,
            tvMiniEpgDescription;
    private ImageView ivMiniEpgDescriptionStar, ivMiniEpgDescriptionQuality, ivMiniEpgDescriptionRating;
    private LinearLayout llMiniEpgDescriptionProgressBar;
    private ProgressBar pbMiniEpgDescriptionProgram;
    private ScrollView svMiniEpgDescription;

    // Main - NumberKeyChannelList
    private ConstraintLayout include_clNumberKeyChannelList;
    private RecyclerView rvNumberKeyChannelList;
    private TextView tvNumberKeyChannelNumberTop;

    // Main - LeftChannelList
    private ConstraintLayout include_clLeftChannelList;
    private RecyclerView rvLeftChannelList;
    private LinearLayout llLeftChannelListNoChannelList;
    private TextView tvLeftChannelListTitle, tvLeftChannelListFavorite,
            tvPreferredSettingAudioLang, tvPreferredSettingAudioLangSelect,
            tvPreferredSettingSubtitle, tvPreferredSettingSubtitleSelect,
            tvPreferredSettingLang, tvPreferredSettingLangSelect;

    // Main - LivePreferredSetting
    private RelativeLayout include_rlLivePreferredSetting;
    private Button btnPreferredSettingAudioLang, btnPreferredSettingSubtitle, btnPreferredSettingLang,
            btnPreferredSettingSave, btnPreferredSettingCancel;

    // Main - LiveSignalInfo
    // Refactored to LiveSignalInfoFragment

    // Main - NoChannelsAvailable
    private ConstraintLayout include_clNoChannelsAvailable;
    private Button btnNoChannelsAvailable;

    // EPG
    private String EPG_FRAGMENT_TAG = "EPG_FRAGMENT";

    private FrameLayout epgFrameLayout;

    // PVR
    private SimpleRecordingSettingPopup recordingSettingPopup;

    private VDC mVDC;

    private VoiceSearchManager voiceSearchManager;

    private TickerView mTickerViewTop, mTickerViewLeft, mTickerViewRight;
    private CNSTickerManager mCnsTickerManager;

    private final android.content.BroadcastReceiver mTickerReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, android.content.Intent intent) {
            if ("com.prime.dtv.ACTION_TICKER_READY".equals(intent.getAction())) {
                Log.d(TAG, "Received ACTION_TICKER_READY, starting ticker manager");
                if (mCnsTickerManager != null) {
                    mCnsTickerManager.stop();
                    mCnsTickerManager.resetTransactionId();
                    mCnsTickerManager.start();
                }
            }
        }
    };

    // Override
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate - BUILD_TIME:" + BuildConfig.BUILD_TIME);

        setContentView(R.layout.activity_main);

        // 1. 初始化 VoiceHelper
        voiceSearchManager = new VoiceSearchManager(this, new VoiceSearchManager.VoiceCallback() {
            @Override
            public void onResult(String text) {
                // [成功] 收到文字;
                Log.d(TAG, "voiceSearch onResult: " + text);
                etLiveChannelSearchVoice.setText(text);
            }

            @Override
            public void onError(String errorMsg) {
                // [失敗]
                if (!errorMsg.equals("No match") && !errorMsg.equals("Client side error"))
                    Log.d(TAG, "voiceSearch onError: " + errorMsg);
            }

            @Override
            public void onListeningStart() {
                Log.d(TAG, "voiceSearch onListeningStart...");
            }

            @Override
            public void onListeningEnd() {
                Log.d(TAG, "voiceSearch onListeningEnd...");
            }
        });

        // 2. 檢查權限 (Android 6.0+)
        checkPermission();

        initMainViews();
        initEpgViews();

        mVDC = new VDC();

        if (!ProgramReminderService.isRunning) {
            Log.d(TAG, "start ProgramReminderService");
            handler.post(() -> {
                Intent serviceIntent = new Intent(MainActivity.this, ProgramReminderService.class);
                if (Build.VERSION.SDK_INT >= 26) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
            });
        }

        mTickerViewTop = findViewById(R.id.lo_ticker_view_top);
        Log.d("CNSTickerManager", "homeactivity mTickerViewTop = " + mTickerViewTop);
        mTickerViewLeft = findViewById(R.id.lo_ticker_view_left);
        Log.d("CNSTickerManager", "homeactivity mTickerViewLeft = " + mTickerViewLeft);
        mTickerViewRight = findViewById(R.id.lo_ticker_view_right);
        Log.d("CNSTickerManager", "homeactivity mTickerViewRight = " + mTickerViewRight);
        mCnsTickerManager = CNSTickerManager.getInstance(this);
        mCnsTickerManager.setTickerView(TickerView.TickerPosition.TOP, mTickerViewTop);
        mCnsTickerManager.setTickerView(TickerView.TickerPosition.LEFT, mTickerViewLeft);
        mCnsTickerManager.setTickerView(TickerView.TickerPosition.RIGHT, mTickerViewRight);

        android.content.IntentFilter filter = new android.content.IntentFilter("com.prime.dtv.ACTION_TICKER_READY");
        registerReceiver(mTickerReceiver, filter, "com.prime.permission.TICKER", null);

        ADHelper.getInstance().bindADService(this);
        EventBus.getDefault().register(this);
    }

    // 處理權限回調
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onRequestPermissionsResult 錄音權限已取得");
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.RECORD_AUDIO }, 100);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        requestPromoteAndTune(0, "onResume");

        handler.postDelayed(updateTimerRunnable, 100);

        if (mTvView != null) {
            mTvView.setVisibility(View.VISIBLE);
        }

        initLockManager();

//        Log.d(TAG, "onResume: mTvView=" + (mTvView != null ? mTvView.getVisibility() : "null") +
//                ", clTvFrame=" + (clTvFrame != null ? clTvFrame.getVisibility() : "null") +
//                ", include_clLiveOverlay="
//                + (include_clLiveOverlay != null ? include_clLiveOverlay.getVisibility() : "null"));

        // 這一行就統一交給 handleIntent 處理
        handleIntent(getIntent());
        if (mCnsTickerManager != null) {
            mCnsTickerManager.start();
        }
    }

    // 處理從外部（Launcher / GUIDE key）送進來的 Intent
    private void handleIntent(Intent intent) {
        boolean initSuccess;
        String channelDisplayNumber = "";

        if (intent != null && intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();

            // 1) 頻道號（可選）
            channelDisplayNumber = bundle.getString("CHANNEL_DISPLAY_NUMBER", "");
            if (TextUtils.isEmpty(channelDisplayNumber)) {
                channelDisplayNumber = "";
            }

            // 2) 初始化 Channel list（跟你原本 onResume 一樣的邏輯）
            if (!TextUtils.isEmpty(channelDisplayNumber)) {
                initSuccess = initChannels(channelDisplayNumber);
            } else {
                initSuccess = initChannels(""); // select last channel
            }

            if (!initSuccess) {
                Log.d(TAG, "handleIntent: initChannels failed");
                return;
            }

            // 3) 判斷是不是 EPG 模式
            String action = bundle.getString("Action");
            if (!TextUtils.isEmpty(action)) {
                Log.d(TAG, "handleIntent Action = " + action);

                // 用完就清掉，避免之後 onResume 再次誤觸
                intent.removeExtra("Action");

                if ("EPG".equals(action)) {
                    showEpg();
                    return; // 已經進 EPG，就不要再 showLiveTV()
                }
            }

            boolean SHOW_EPG = bundle.getBoolean("SHOW_EPG", false);
            // 沒有特別指定 Action → 正常 LiveTV
            if (SHOW_EPG) {
                showEpg();
                return;
            }
            showLiveTV();
        } else {
            // 沒有任何 Extra / 是一般從 Launcher 打開
            initSuccess = initChannels(""); // select last channel
            if (!initSuccess) {
                Log.d(TAG, "handleIntent: initChannels failed (no extras)");
                return;
            }
            showLiveTV();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent");

        if (intent != null) {
            // 更新 Activity 內部的 getIntent()
            setIntent(intent);

            // comment out because onResume() will be triggered after onNewIntent()
            // and handleIntent will be called in onResume()
            // // 馬上依新的 Intent 決定要不要 showEpg()
            // handleIntent(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        if (currentUnlockStateManager != null) {
            currentUnlockStateManager.reset(); // re-lock
        }

        ViewUtils.dismissDialogIfExists(getSupportFragmentManager(), MAIN_PARENTAL_PIN_DIALOG_TAG);

        // Layout that can coexist with other UIs
        hideAllLiveTvUiExceptView(null);

        if (isVisible(epgFrameLayout)) {
            hideEpg();
        }

        // Exclusive layout that cannot coexist with other UIs
        hideLiveOverlay(LiveOverlayState.ALL);
        hideLiveRecordingStatus();
        hideLiveChannelSearch();

        if (nowPlayingManager != null) {
            nowPlayingManager.cancelPendingTune();
        }

        if (mTvView != null) {
            mTvView.reset(); // used to un-tune the current TvView.
            mTvView.setVisibility(View.GONE);
        }

        if (mCnsTickerManager != null) {
            mCnsTickerManager.stop();
        }
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceSearchManager != null) {
            voiceSearchManager.destroy();
        }
        unregisterReceiver(mTickerReceiver);
        ADHelper.getInstance().unbindADService(this);
        EventBus.getDefault().unregister(this);
        Log.d(TAG, "onDestroy");
    }

    // EpgPlaybackManager Override
    @Override
    public void tuneEpgChannel(Channel ch) {
        Log.d(TAG, "tuneEpgChannel");
        tuneChannel(ch);
    }

    @Override
    public List<String> getCurrentAudioLanguageList() {
        return TvViewUtils.getAudioLanguageList(mTvView);
    }

    @Override
    public List<String> getCurrentSubtitleLanguages() {
        return TvViewUtils.getSubtitleLanguages(mTvView);
    }

    @Override
    public String getCurrentVideoResolutionLabel() {
        return TvViewUtils.getVideoResolutionLabel(mTvView);
    }

    @Override
    public UnlockStateManager getCurrentUnlockStateManager() {
        return currentUnlockStateManager;
    }

    @Override
    public void backToLiveFromEpg() {
        boolean needReTune = false;
        boolean initSuccess = false;
        if (isVisible(ivEpgTvFrameBlock)) {
            needReTune = true;
        }

        hideEpg();
        initSuccess = initChannels(""); // load full channel list
        if (!initSuccess) {
            Log.d(TAG, "initChannels failed");
            return;
        }

        TvViewUtils.resetTvViewToFullscreen(mTvView);
        resetFccContainerToFullscreen();

        if (needReTune) {
            mTvView.reset();
            tuneChannel(currentChannelListManager.getCurrentChannel());
        }

        updateLiveRecordingStatus();
    }

    @Override
    public void showParentalPinDialog() {
        ParentalPinDialogFragment dialog = new ParentalPinDialogFragment();
        dialog.setOnPinEnteredListener(pin -> {
            // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
            GposInfo gpos = PrimeHomeplusTvApplication.get_prime_dtv_service().gpos_info_get();
            String parentalPin = "0000";// default value
            if (gpos != null)
                parentalPin = String.format("%04d", GposInfo.getPasswordValue(this.getApplicationContext()));
            // Log.d(TAG,"parentalPin = "+ parentalPin + " pin = "+pin);
            if (!TextUtils.isEmpty(pin) && pin.equals(parentalPin)) {
                Program nowProgram = ProgramUtils.getCurrentProgram(this.getApplicationContext(),
                        nowPlayingManager.getCurrentChannel().getId());
                int lockFlag = lockManager.getHighestPriorityLockFlag(nowPlayingManager.getCurrentChannel(), nowProgram,
                        nowPlayingManager.getCurrentContentBlockedRating());
                if (isVisible(epgFrameLayout) && lockFlag == LockManager.LOCK_NONE) {
                    // unlock EPG program info if the focused EPG item is not channel-locked
                    lockFlag = LockManager.LOCK_EPG_PROGRAM_INFO;
                }

                // set unlock level based on current channel lock flag
                currentUnlockStateManager.unlock(lockFlag);
                dialog.dismiss();

                EpgFragment epgFragment = getEpgFragment();
                if (epgFragment != null) {
                    epgFragment.updateEpgChannelAndProgram();
                }

                // re-tune current channel
                // tuneChannel(currentChannelListManager.getCurrentChannel());
                tuneChannel(nowPlayingManager.getCurrentChannel());
            } else {
                dialog.showErrorMessage();
            }
        });
        dialog.show(getSupportFragmentManager(), MAIN_PARENTAL_PIN_DIALOG_TAG);
    }

    // Private
    private void initMainViews() {
        Log.d(TAG, "initMainViews");
        initFccSurfaces();

        initTvFrameViews();
        initMainOverlayViews();
        initMainRecordingStatusViews();
        initMainChannelSearchViews();
        initMainMiniEpgViews();
        initMainMiniEpgDescriptionViews();
        initMainNumberKeyChannelListViews();
        initMainLeftChannelListViews();
        initPreferredSettingViews();
        initRecordingSettingPopup();

        initMainNoChannelsAvailableViews();
    }

    private void initFccSurfaces() {
        mFccContainer = findViewById(R.id.fl_fcc_container);
        mFccSurfaceViews[0] = findViewById(R.id.sv_fcc_0);
        mFccSurfaceViews[1] = findViewById(R.id.sv_fcc_1);
        mFccSurfaceViews[2] = findViewById(R.id.sv_fcc_2);
        for (int i = 0; i < mFccSlots.length; i++) {
            if (mFccSlots[i] == null) {
                mFccSlots[i] = new FccSurfaceSlot();
            }
        }

        for (int i = 0; i < mFccSurfaceViews.length; i++) {
            final int index = i;
            SurfaceView sv = mFccSurfaceViews[i];
            if (sv == null) {
                LogUtils.d("FCC_LOG missing SurfaceView index=" + index);
                continue;
            }
            sv.setZOrderMediaOverlay(true);
            sv.setVisibility(View.VISIBLE);
            sv.setAlpha(1.0f);
            sv.setSurfaceLifecycle(SurfaceView.SURFACE_LIFECYCLE_FOLLOWS_ATTACHMENT);
            sv.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    //Surface surface = holder.getSurface();
                    //updateSlot(index, surface, isValid(surface), 0, 0, "created");
                    //sendFccSurface(index, surface);
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    Surface surface = holder.getSurface();
                    updateSlot(index, surface, isValid(surface), width, height, "changed");
                    sendFccSurface(index, surface);
                    if (width > 0 && height > 0 && isValid(surface)) {
                        maybePromoteAndTune("surfaceChanged");
                    }
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    updateSlot(index, null, false, 0, 0, "destroyed");
                    sendFccSurface(index, null);
                    clearPendingPromote(index, "surfaceDestroyed");
                    setSetVisibleCompleted(false, "surfaceDestroyed");
                }
            });
        }

        PrimeHomeplusTvApplication app = PrimeHomeplusTvApplication.getInstance();
        if (app != null) {
            app.addServiceConnectionListener(new PrimeHomeplusTvApplication.ServiceConnectionListener() {
                @Override
                public void onServiceConnected() {
                    runOnUiThread(MainActivity.this::resendFccSurfacesIfReady);
                    runOnUiThread(() -> maybePromoteAndTune("serviceConnected"));
                }

                @Override
                public void onServiceDisconnected() {
                }
            });
        }
    }

    private FccSurfaceSlot getSlot(int index) {
        if (index < 0 || index >= mFccSlots.length) {
            return null;
        }
        if (mFccSlots[index] == null) {
            mFccSlots[index] = new FccSurfaceSlot();
        }
        return mFccSlots[index];
    }

    private boolean slotReady(int index) {
        FccSurfaceSlot slot = getSlot(index);
        return slot != null
                && slot.surface != null
                && slot.valid
                && slot.width > 0
                && slot.height > 0;
    }

    private void updateSlot(int index, Surface surface, boolean valid, int width, int height, String reason) {
        FccSurfaceSlot slot = getSlot(index);
        if (slot == null) {
            return;
        }
        slot.surface = surface;
        slot.valid = valid;
        slot.width = width;
        slot.height = height;
        slot.surfaceId = surfaceId(surface);
        slot.lastChangedUptime = SystemClock.uptimeMillis();
        LogUtils.d("FCC_LOG slotChanged idx=" + index
                + " valid=" + valid
                + " size=" + width + "x" + height
                + " surfaceId=" + slot.surfaceId
                + " surface=" + surface
                + " reason=" + reason);
    }

    private void requestPromoteAndTune(int index, String reason) {
        if (index < 0 || index >= mFccSurfaceViews.length) {
            LogUtils.d("FCC_LOG requestPromoteTune invalid idx=" + index + " reason=" + reason);
            return;
        }
        FccSurfaceSlot slot = getSlot(index);
        boolean ready = slotReady(index);
        int width = slot == null ? 0 : slot.width;
        int height = slot == null ? 0 : slot.height;
        LogUtils.d("FCC_LOG requestPromoteTune idx=" + index
                + " reason=" + reason
                + " ready=" + ready
                + " size=" + width + "x" + height);
        mPendingPromoteIdx = index;
        mPendingPromoteReason = reason;
        mPendingPromoteUptime = SystemClock.uptimeMillis();
        if (!ready) {
            setSetVisibleCompleted(false, "requestPromoteTune");
        }
        maybePromoteAndTune("request:" + reason);
    }

    private void maybePromoteAndTune(String reason) {
        if (mPendingPromoteIdx < 0) {
            return;
        }
        if (Looper.myLooper() != Looper.getMainLooper()) {
            runOnUiThread(() -> maybePromoteAndTune(reason));
            return;
        }
        FccSurfaceSlot slot = getSlot(mPendingPromoteIdx);
        boolean ready = slotReady(mPendingPromoteIdx);
        if (!ready) {
            LogUtils.d("FCC_LOG delayPromoteTune idx=" + mPendingPromoteIdx
                    + " reason=" + reason
                    + " valid=" + (slot != null && slot.valid)
                    + " size=" + (slot == null ? 0 : slot.width) + "x" + (slot == null ? 0 : slot.height)
                    + " surface=" + (slot == null ? null : slot.surface));
            return;
        }
        LogUtils.d("FCC_LOG DO promoteTune idx=" + mPendingPromoteIdx
                + " reason=" + reason
                + " surfaceId=" + slot.surfaceId);
        promoteFccSurface(mPendingPromoteIdx);
        setSetVisibleCompleted(true, "promoteTune:" + reason);
        mPendingPromoteIdx = -1;
        mPendingPromoteReason = null;
        mPendingPromoteUptime = 0;
    }

    private void clearPendingPromote(int index, String reason) {
        if (mPendingPromoteIdx != index) {
            return;
        }
        LogUtils.d("FCC_LOG pendingPromoteCleared idx=" + index + " reason=" + reason);
        mPendingPromoteIdx = -1;
        mPendingPromoteReason = null;
        mPendingPromoteUptime = 0;
    }

    private void setSetVisibleCompleted(boolean completed, String reason) {
        PrimeDtvServiceInterface service = PrimeHomeplusTvApplication.get_prime_dtv_service();
        if (service == null || !service.isPrimeDtvServiceReady()) {
            LogUtils.d("FCC_LOG setVisibleCompleted skipped completed=" + completed
                    + " reason=" + reason + " serviceReady=false");
            return;
        }
        LogUtils.d("FCC_LOG setVisibleCompleted completed=" + completed + " reason=" + reason);
        service.setSetVisibleCompleted(completed);
    }

    private void promoteFccSurface(int activeIndex) {
        LogUtils.d("FCC_LOG promote activeIndex=" + activeIndex);
        for (int i = 0; i < mFccSurfaceViews.length; i++) {
            SurfaceView sv = mFccSurfaceViews[i];
            if (sv == null) {
                continue;
            }
            try {
                boolean isActive = (i == activeIndex);
				sv.setVisibility(isActive ? View.VISIBLE : View.INVISIBLE);
                float alpha = isActive ? 1f : 0.01f;
                //sv.setAlpha(alpha);
                if (isActive) {
                    sv.bringToFront();
                }
                Surface surface = (sv.getHolder() == null) ? null : sv.getHolder().getSurface();
                LogUtils.d("FCC_LOG promote index=" + i
                        + " active=" + isActive
                        + " " + describeViewForFcc(sv)
                        + " surface=" + surface
                        + " surfaceId=" + surfaceId(surface)
                        + " valid=" + isValid(surface)
						+ " visibility=" + (isActive ? "VISIBLE" : "INVISIBLE"));
                        //+ " alpha=" + alpha);
            } catch (RuntimeException e) {
                LogUtils.d("FCC_LOG promote error index=" + i
                        + " activeIndex=" + activeIndex
                        + " view=" + sv
                        + " thread=" + Thread.currentThread().getName(), e);
            }
        }
    }

    private String describeViewForFcc(SurfaceView sv) {
        if (sv == null) {
            return "view=null";
        }
        String resourceName = "unknown";
        int viewId = sv.getId();
        if (viewId != View.NO_ID) {
            try {
                resourceName = getResources().getResourceName(viewId);
            } catch (RuntimeException ignored) {
            }
        }
        Rect rect = new Rect();
        boolean hasRect = sv.getGlobalVisibleRect(rect);
        float z = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? sv.getZ() : 0f;
        return "viewId=" + viewId
                + " res=" + resourceName
                + " size=" + sv.getWidth() + "x" + sv.getHeight()
                + " globalRect=" + (hasRect ? rect.toShortString() : "none")
                + " alpha=" + sv.getAlpha()
                + " vis=" + sv.getVisibility()
                + " z=" + z;
    }

    private void sendFccSurface(int index, Surface surface) {
        if (!slotReady(index)) {
            FccSurfaceSlot slot = getSlot(index);
            LogUtils.d("FCC_LOG gate set_surface idx=" + index +
                    " ready=false size=" + (slot == null ? 0 : slot.width) + "x" + (slot == null ? 0 : slot.height) +
                    " surface=" + surface +
                    " surfaceId=" + surfaceId(surface));
            scheduleFccSurfaceRetry();
            return;
        }
        handler.post(() -> doSetFccSurface(index, surface));
    }

    private void doSetFccSurface(int index, Surface surface) {
        FccSurfaceSlot slot = getSlot(index);
        int width = slot == null ? 0 : slot.width;
        int height = slot == null ? 0 : slot.height;
        boolean valid = slot == null ? isValid(surface) : slot.valid;
        PrimeDtvServiceInterface service = PrimeHomeplusTvApplication.get_prime_dtv_service();
        if (service == null || !service.isPrimeDtvServiceReady()) {
            LogUtils.d("FCC_LOG gate set_surface serviceReady=false idx=" + index +
                    " surface=" + surface +
                    " surfaceId=" + surfaceId(surface) +
                    " valid=" + valid +
                    " size=" + width + "x" + height);
            scheduleFccSurfaceRetry();
            return;
        }
        try {
            LogUtils.d("FCC_LOG set_surface: index=" + index +
                    " surface=" + surface +
                    " surfaceId=" + surfaceId(surface) +
                    " valid=" + valid +
                    " size=" + width + "x" + height +
                    " alpha=" + getFccAlpha(index));
            service.set_surface(getApplicationContext(), surface, index);
        } catch (Throwable t) {
            LogUtils.d("FCC_LOG set_surface failed index=" + index, t);
        }
    }

    private void scheduleFccSurfaceRetry() {
        if (mFccSurfaceRetryScheduled) {
            return;
        }
        mFccSurfaceRetryScheduled = true;
        handler.postDelayed(mFccSurfaceRetryRunnable, FCC_SURFACE_RETRY_MS);
    }

    private void resendFccSurfacesIfReady() {
        mFccSurfaceRetryScheduled = false;
        if (PrimeHomeplusTvApplication.get_prime_dtv_service() == null
                || !PrimeHomeplusTvApplication.get_prime_dtv_service().isPrimeDtvServiceReady()) {
            LogUtils.d("FCC_LOG resend skipped: serviceReady=false");
            scheduleFccSurfaceRetry();
            return;
        }
        for (int i = 0; i < mFccSlots.length; i++) {
            FccSurfaceSlot slot = mFccSlots[i];
            if (slot == null || slot.surface == null || !slot.valid) {
                continue;
            }
            LogUtils.d("FCC_LOG resend index=" + i +
                    " surface=" + slot.surface +
                    " surfaceId=" + slot.surfaceId +
                    " valid=" + slot.valid +
                    " size=" + slot.width + "x" + slot.height +
                    " alpha=" + getFccAlpha(i));
            sendFccSurface(i, slot.surface);
        }
    }

    private float getFccAlpha(int index) {
        if (index < 0 || index >= mFccSurfaceViews.length) {
            return -1f;
        }
        SurfaceView view = mFccSurfaceViews[index];
        return view == null ? -1f : view.getAlpha();
    }

    private boolean isValid(Surface surface) {
        return surface != null && surface.isValid();
    }

    private int surfaceId(Surface surface) {
        return surface == null ? 0 : System.identityHashCode(surface);
    }

    private void initTvFrameViews() {
        mTvView = findViewById(R.id.TvView);
        mTvView.setCallback(mCallBack);

        clTvFrame = findViewById(R.id.clTvFrame);

        include_clEpgTvFrame = findViewById(R.id.include_clEpgTvFrame);
        ivEpgTvFrameBlock = findViewById(R.id.ivEpgTvFrameBlock);
        ivEpgTvFrameRecordingStatus = findViewById(R.id.ivEpgTvFrameRecordingStatus);
        tvEpgTvFrameProgramName = findViewById(R.id.tvEpgTvFrameProgramName);

    }

    private void initMainOverlayViews() {
        Log.d(TAG, "initMainOverlayViews");

        include_clLiveOverlay = findViewById(R.id.include_clLiveOverlay);
        clLiveOverlayQrErrorEvent = findViewById(R.id.clLiveOverlayQrErrorEvent);
        clLiveOverlayChannelBlock = findViewById(R.id.clLiveOverlayChannelBlock);
        clLiveOverlayMusic = findViewById(R.id.clLiveOverlayMusic);

        ivLiveOverlayPadlockAnimation = findViewById(R.id.ivLiveOverlayPadlockAnimation);
        ivLiveOverlayMusicAnimation = findViewById(R.id.ivLiveOverlayMusicAnimation);
        ivLiveOverlayQrCode = findViewById(R.id.ivLiveOverlayQrCode);

        adLiveOverlayPadlockAnimation = (AnimationDrawable) ivLiveOverlayPadlockAnimation.getDrawable();
        adLiveOverlayMusicAnimation = (AnimationDrawable) ivLiveOverlayMusicAnimation.getDrawable();

        tvLiveOverlayQrErrorCode = findViewById(R.id.tvLiveOverlayQrErrorCode);
        tvLiveOverlayQrErrorMessage = findViewById(R.id.tvLiveOverlayQrErrorMessage);
    }

    private void initMainRecordingStatusViews() {
        Log.d(TAG, "initMainRecordingStatusViews");

        include_clLiveRecordingStatus = findViewById(R.id.include_clLiveRecordingStatus);
        tvLiveRecordingStatusChannel1 = findViewById(R.id.tvLiveRecordingStatusChannel1);
        tvLiveRecordingStatusPipe = findViewById(R.id.tvLiveRecordingStatusPipe);
        tvLiveRecordingStatusChannel2 = findViewById(R.id.tvLiveRecordingStatusChannel2);
    }

    private void initMainChannelSearchViews() {
        Log.d(TAG, "initMainChannelSearchViews");

        include_rlLiveChannelSearch = findViewById(R.id.include_rlLiveChannelSearch);
        rlLiveChannelSearchVoice = findViewById(R.id.rlLiveChannelSearchVoice);
        rlLiveChannelSearchBottomMessage = findViewById(R.id.rlLiveChannelSearchBottomMessage);
        llLiveChannelSearch = findViewById(R.id.llLiveChannelSearch);
        llLiveChannelSearchMessage = findViewById(R.id.llLiveChannelSearchMessage);
        btnLiveChannelSearchVoice = findViewById(R.id.btnLiveChannelSearchVoice);
        btnLiveChannelSearch = findViewById(R.id.btnLiveChannelSearch);
        etLiveChannelSearch = findViewById(R.id.etLiveChannelSearch);
        etLiveChannelSearchVoice = findViewById(R.id.etLiveChannelSearchVoice);
        rvLiveChannelSearchList = findViewById(R.id.rvLiveChannelSearchList);
        rvLiveChannelSearchList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        channelSearchAdapter = new ChannelSearchAdapter(rvLiveChannelSearchList, null);
        rvLiveChannelSearchList.setAdapter(channelSearchAdapter);

        channelSearchAdapter.setOnItemClickListener(channel -> {
            Log.d(TAG, "channelSearchAdapter setOnItemClickListener ch:" + channel.getDisplayNumber());
            hideLiveChannelSearch();
            currentChannelListManager.setCurrentChannelIndexByDisplayNumber(channel.getDisplayNumber());
            tuneChannel(currentChannelListManager.getCurrentChannel());
        });

        btnLiveChannelSearchVoice.setOnKeyListener((v, keyCode, event) -> {
            Log.d(TAG, "btnLiveChannelSearch setOnKeyListener");
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    hideLiveChannelSearch();
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    Log.d(TAG, "btnLiveChannelSearch setOnKeyListener KEYCODE_DPAD_CENTER");
                    voiceSearchManager.startListening();
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    Log.d(TAG, "btnLiveChannelSearch setOnKeyListener KEYCODE_DPAD_RIGHT");
                    etLiveChannelSearchVoice.requestFocus();
                }
                return true;
            }
            return false;
        });

        btnLiveChannelSearchVoice.setOnFocusChangeListener((v, hasFocus) -> {
            Log.d(TAG, "btnLiveChannelSearchVoice setOnFocusChangeListener");
            if (hasFocus) {
                etLiveChannelSearchVoice.setHint(getString(R.string.live_search_msg_3));
            } else {
                voiceSearchManager.stopListening();
            }
        });

        btnLiveChannelSearch.setOnFocusChangeListener((v, hasFocus) -> {
            Log.d(TAG, "btnLiveChannelSearch setOnFocusChangeListener");
            if (hasFocus) {
                if (etLiveChannelSearch.getText() != null &&
                        !etLiveChannelSearch.getText().toString().trim().isEmpty()) {
                    llLiveChannelSearchMessage.setVisibility(View.GONE);
                } else {
                    llLiveChannelSearchMessage.setVisibility(View.VISIBLE);
                }

                rlLiveChannelSearchBottomMessage.setVisibility(View.VISIBLE);
                etLiveChannelSearch.setHint("");
            } else {
                llLiveChannelSearchMessage.setVisibility(View.GONE);
                rlLiveChannelSearchBottomMessage.setVisibility(View.GONE);
                etLiveChannelSearch.setHint(getString(R.string.live_search_msg_2));
            }
        });

        btnLiveChannelSearch.setOnClickListener((v) -> {
            Log.d(TAG, "btnLiveChannelSearch setOnClickListener");
            llLiveChannelSearch.setVisibility(View.GONE);
            rlLiveChannelSearchVoice.setVisibility(View.VISIBLE);
            btnLiveChannelSearchVoice.requestFocus();

            // TODO: voice search
        });

        btnLiveChannelSearch.setOnKeyListener((v, keyCode, event) -> {
            Log.d(TAG, "btnLiveChannelSearch setOnKeyListener");
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    etLiveChannelSearch.requestFocus();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                    hideLiveChannelSearch();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
                        keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    Log.d(TAG, "btnLiveChannelSearch setOnKeyListener KEYCODE_DPAD_CENTER");
                    voiceSearchManager.startListening();
                }
            }
            return false;
        });

        etLiveChannelSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLiveChannelSearchResult(s.toString());
            }
        });

        etLiveChannelSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        getApplicationContext().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        etLiveChannelSearch.setOnKeyListener((v, keyCode, event) -> {
            Log.d(TAG, "etLiveChannelSearch setOnKeyListener");
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    btnLiveChannelSearch.requestFocus();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                    hideLiveChannelSearch();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                        keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    return true;
                }
                return false;
            }
            return false;
        });

        etLiveChannelSearchVoice.setOnFocusChangeListener((v, hasFocus) -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    getApplicationContext().INPUT_METHOD_SERVICE);
            if (!hasFocus) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            } else {
                if (imm != null) {
                    etLiveChannelSearchVoice.setHint("");
                    imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        etLiveChannelSearchVoice.setOnKeyListener((v, keyCode, event) -> {
            Log.d(TAG, "etLiveChannelSearchVoice setOnKeyListener");
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    btnLiveChannelSearchVoice.requestFocus();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                    hideLiveChannelSearch();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                        keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    return true;
                }
                return false;
            }
            return false;
        });

        etLiveChannelSearchVoice.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLiveChannelSearchResult(s.toString());
            }
        });

    }

    private void initMainMiniEpgViews() {
        Log.d(TAG, "initMainMiniEpgViews");

        include_clMiniEpg = findViewById(R.id.include_clMiniEpg);

        llMiniEpgNowProgramParentalControlLock = findViewById(R.id.llMiniEpgNowProgramParentalControlLock);
        llMiniEpgNowProgramWorkHourLock = findViewById(R.id.llMiniEpgNowProgramWorkHourLock);
        llMiniEpgNowProgramChannelLock = findViewById(R.id.llMiniEpgNowProgramChannelLock);
        llMiniEpgNowProgramAdultChannel = findViewById(R.id.llMiniEpgNowProgramAdultChannel);
        llMiniEpgNowMusic = findViewById(R.id.llMiniEpgNowMusic);
        llMiniEpgNowProgramMain = findViewById(R.id.llMiniEpgNowProgramMain);
        llMiniEpgNowProgram = findViewById(R.id.llMiniEpgNowProgram);
        llMiniEpgGreenKey = findViewById(R.id.llMiniEpgGreenKey);
        llMiniEpgBlueKey = findViewById(R.id.llMiniEpgBlueKey);

        tvMiniEpgChannelNumberTop = findViewById(R.id.tvMiniEpgChannelNumberTop);
        tvMiniEpgChannelNumber = findViewById(R.id.tvMiniEpgChannelNumber);
        tvMiniEpgChannelName = findViewById(R.id.tvMiniEpgChannelName);
        tvMiniEpgDate = findViewById(R.id.tvMiniEpgDate);
        tvMiniEpgNowProgram = findViewById(R.id.tvMiniEpgNowProgram);
        tvMiniEpgNowProgramTime = findViewById(R.id.tvMiniEpgNowProgramTime);
        tvMiniEpgBilingual = findViewById(R.id.tvMiniEpgBilingual);
        tvMiniEpgSubtitle = findViewById(R.id.tvMiniEpgSubtitle);
        tvMiniEpgNextProgram = findViewById(R.id.tvMiniEpgNextProgram);
        tvMiniEpgRedKey = findViewById(R.id.tvMiniEpgRedKey);
        tvMiniEpgBilingualNow = findViewById(R.id.tvMiniEpgBilingualNow);
        tvMiniEpgYellowKey = findViewById(R.id.tvMiniEpgYellowKey);
        tvMiniEpgSubtitleNow = findViewById(R.id.tvMiniEpgSubtitleNow);
        tvMiniEpgOk = findViewById(R.id.tvMiniEpgOk);

        ivMiniEpgLock = findViewById(R.id.ivMiniEpgLock);
        ivMiniEpgMusic = findViewById(R.id.ivMiniEpgMusic);
        ivMiniEpgStar = findViewById(R.id.ivMiniEpgStar);
        ivMiniEpgQuality = findViewById(R.id.ivMiniEpgQuality);
        ivMiniEpgDolby = findViewById(R.id.ivMiniEpgDolby);
        ivMiniEpgNowRating = findViewById(R.id.ivMiniEpgNowRating);
        ivMiniEpgNowRecord = findViewById(R.id.ivMiniEpgNowRecord);
        ivMiniEpgNext = findViewById(R.id.ivMiniEpgNext);
        ivMiniEpgNextReminder = findViewById(R.id.ivMiniEpgNextReminder);
        ivMiniEpgNextRecord = findViewById(R.id.ivMiniEpgNextRecord);
        ivMiniEpgAd = findViewById(R.id.ivMiniEpgAd);

        pbMiniEpgProgram = findViewById(R.id.pbMiniEpgProgram);

        initMiniEpgViewMap();
    }

    private void initMiniEpgViewMap() {
        miniEpgViewMap.put(MiniEpgState.MAIN, llMiniEpgNowProgramMain);
        miniEpgViewMap.put(MiniEpgState.MUSIC, llMiniEpgNowMusic);
        miniEpgViewMap.put(MiniEpgState.ADULT_CHANNEL_LOCK, llMiniEpgNowProgramAdultChannel);
        miniEpgViewMap.put(MiniEpgState.PARENTAL_CHANNEL_LOCK, llMiniEpgNowProgramChannelLock);
        miniEpgViewMap.put(MiniEpgState.PARENTAL_PROGRAM_LOCK, llMiniEpgNowProgramParentalControlLock);
        miniEpgViewMap.put(MiniEpgState.WORK_HOUR_LOCK, llMiniEpgNowProgramWorkHourLock);
    }

    private void initMainMiniEpgDescriptionViews() {
        Log.d(TAG, "initMainMiniEpgDescriptionViews");

        include_clMiniEpgDescription = findViewById(R.id.include_clMiniEpgDescription);
        tvMiniEpgDescriptionNumber = findViewById(R.id.tvMiniEpgDescriptionNumber);
        tvMiniEpgDescriptionChannelName = findViewById(R.id.tvMiniEpgDescriptionChannelName);
        tvMiniEpgDescriptionProgramName = findViewById(R.id.tvMiniEpgDescriptionProgramName);
        tvMiniEpgDescriptionBilingual = findViewById(R.id.tvMiniEpgDescriptionBilingual);
        tvMiniEpgDescriptionProgramStart = findViewById(R.id.tvMiniEpgDescriptionProgramStart);
        tvMiniEpgDescriptionProgramEnd = findViewById(R.id.tvMiniEpgDescriptionProgramEnd);
        tvMiniEpgDescription = findViewById(R.id.tvMiniEpgDescription);
        ivMiniEpgDescriptionStar = findViewById(R.id.ivMiniEpgDescriptionStar);
        ivMiniEpgDescriptionQuality = findViewById(R.id.ivMiniEpgDescriptionQuality);
        ivMiniEpgDescriptionRating = findViewById(R.id.ivMiniEpgDescriptionRating);
        llMiniEpgDescriptionProgressBar = findViewById(R.id.llMiniEpgDescriptionProgressBar);
        pbMiniEpgDescriptionProgram = findViewById(R.id.pbMiniEpgDescriptionProgram);
        svMiniEpgDescription = findViewById(R.id.svMiniEpgDescription);
    }

    private void initMainNumberKeyChannelListViews() {
        Log.d(TAG, "initMainNumberKeyChannelListViews");

        include_clNumberKeyChannelList = findViewById(R.id.include_clNumberKeyChannelList);
        tvNumberKeyChannelNumberTop = findViewById(R.id.tvNumberKeyChannelNumberTop);
        rvNumberKeyChannelList = findViewById(R.id.rvNumberKeyChannelList);

        rvNumberKeyChannelList.setLayoutManager(new LinearLayoutManager(this));
        numberKeyChannelAdapter = new NumberKeyChannelAdapter(rvNumberKeyChannelList, null); // TODO: TBC
        rvNumberKeyChannelList.setAdapter(numberKeyChannelAdapter);

        numberKeyChannelAdapter.setOnItemFocusedListener(() -> {
            Log.d(TAG, "reset inputNumberBufferTimeoutRunnable");
            resetHandlerDelay(handler, inputNumberBufferTimeoutRunnable, INPUT_NUMBER_TIMEOUT_MS,
                    "inputNumberBufferTimeout");
        });

        numberKeyChannelAdapter.setOnItemClickListener(channel -> {
            currentChannelListManager.setCurrentChannelIndexByDisplayNumber(channel.getDisplayNumber());
            tuneChannel(currentChannelListManager.getCurrentChannel());
        });
    }

    private void initMainLeftChannelListViews() {
        Log.d(TAG, "initMainLeftChannelListViews");

        include_clLeftChannelList = findViewById(R.id.include_clLeftChannelList);
        rvLeftChannelList = findViewById(R.id.rvLeftChannelList);
        llLeftChannelListNoChannelList = findViewById(R.id.llLeftChannelListNoChannelList);
        tvLeftChannelListTitle = findViewById(R.id.tvLeftChannelListTitle);
        tvLeftChannelListFavorite = findViewById(R.id.tvLeftChannelListFavorite);

        rvLeftChannelList.setLayoutManager(new LinearLayoutManager(this));
        leftChannelListAdapter = new LeftChannelListAdapter(rvLeftChannelList, tvLeftChannelListFavorite, null); // TODO:
                                                                                                                 // TBC
        rvLeftChannelList.setAdapter(leftChannelListAdapter);

        leftChannelListAdapter.setOnItemFocusedListener(() -> {
            leftChannelListAdapter.refreshFocusedProgramInfo();
        });

        leftChannelListAdapter.setOnItemClickListener(channel -> {
            currentChannelListManager.setCurrentChannelIndexByDisplayNumber(channel.getDisplayNumber());
            tuneChannel(currentChannelListManager.getCurrentChannel());
        });
    }

    private void initPreferredSettingViews() {
        Log.d(TAG, "initPreferredSettingViews");

        include_rlLivePreferredSetting = findViewById(R.id.include_rlLivePreferredSetting);
        btnPreferredSettingAudioLang = findViewById(R.id.btnPreferredSettingAudioLang);
        btnPreferredSettingSubtitle = findViewById(R.id.btnPreferredSettingSubtitle);
        btnPreferredSettingLang = findViewById(R.id.btnPreferredSettingLang);
        btnPreferredSettingSave = findViewById(R.id.btnPreferredSettingSave);
        btnPreferredSettingCancel = findViewById(R.id.btnPreferredSettingCancel);

        tvPreferredSettingAudioLang = findViewById(R.id.tvPreferredSettingAudioLang);
        tvPreferredSettingAudioLangSelect = findViewById(R.id.tvPreferredSettingAudioLangSelect);
        tvPreferredSettingSubtitle = findViewById(R.id.tvPreferredSettingSubtitle);
        tvPreferredSettingSubtitleSelect = findViewById(R.id.tvPreferredSettingSubtitleSelect);
        tvPreferredSettingLang = findViewById(R.id.tvPreferredSettingLang);
        tvPreferredSettingLangSelect = findViewById(R.id.tvPreferredSettingLangSelect);

        ViewUtils.applyButtonFocusTextEffect(btnPreferredSettingSave, 17, 16, true);
        ViewUtils.applyButtonFocusTextEffect(btnPreferredSettingCancel, 17, 16, true);

        currentSelectSettingLang = GposInfo.getCNSMenuSettingLang(this);
        View.OnKeyListener commonKeyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        hideLivePreferredSetting();
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP &&
                            v.getId() == R.id.btnPreferredSettingAudioLang) {
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN &&
                            (v.getId() == R.id.btnPreferredSettingSave
                                    || v.getId() == R.id.btnPreferredSettingCancel)) {
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT &&
                            v.getId() != R.id.btnPreferredSettingCancel) {
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT &&
                            v.getId() != R.id.btnPreferredSettingSave) {
                        return true;
                    }
                }
                return false;
            }
        };

        btnPreferredSettingAudioLang.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    hideLivePreferredSetting();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    showRightAudioTrack();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    showLeftAudioTrack();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    btnPreferredSettingSubtitle.requestFocus();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    selectAudioTrack();
                    return true;
                }
            }
            return false;
        });

        btnPreferredSettingSubtitle.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    hideLivePreferredSetting();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    showRightSubtitleTrack();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    showLeftSubtitleTrack();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    btnPreferredSettingAudioLang.requestFocus();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    btnPreferredSettingLang.requestFocus();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    selectSubtitleTrack();
                    return true;
                }
            }
            return false;
        });

        btnPreferredSettingLang.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    hideLivePreferredSetting();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
                        keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    showDiffSettingLang();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    btnPreferredSettingSubtitle.requestFocus();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    btnPreferredSettingSave.requestFocus();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    selectSettingLang();
                    return true;
                }
            }
            return false;
        });

        btnPreferredSettingSave.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    hideLivePreferredSetting();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
                        keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    dobtnPreferredSettingSave();
                    hideLivePreferredSetting();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    btnPreferredSettingLang.requestFocus();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    btnPreferredSettingCancel.requestFocus();
                    return true;
                }
            }
            return false;
        });

        btnPreferredSettingCancel.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    hideLivePreferredSetting();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ||
                        keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    btnPreferredSettingLang.requestFocus();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    btnPreferredSettingSave.requestFocus();
                    return true;
                }
            }
            return false;
        });

        btnPreferredSettingCancel.setOnClickListener((v) -> {
            hideLivePreferredSetting();
        });
    }

    private void initRecordingSettingPopup() {
        recordingSettingPopup = new SimpleRecordingSettingPopup(getApplicationContext(), getWindow().getDecorView(),
                handler);
        recordingSettingPopup.setOnRecordingSettingChangedListener(() -> {
            Log.d(TAG, "setOnRecordingSettingChangedListener");
            updateLiveRecordingStatus();
        });
    }

    private void initMainNoChannelsAvailableViews() {
        Log.d(TAG, "initMainNoChannelsAvailableViews");

        include_clNoChannelsAvailable = findViewById(R.id.include_clNoChannelsAvailable);
        btnNoChannelsAvailable = findViewById(R.id.btnNoChannelsAvailable);

        btnNoChannelsAvailable.setOnClickListener((v) -> {
            finish();
        });

        btnNoChannelsAvailable.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                    finish();
                }
                return true;
            }
            return false;
        });
    }

    private void initEpgViews() {
        Log.d(TAG, "initEpgViews");

        epgFrameLayout = findViewById(R.id.epg_fragment_container);
    }

    private void initLockManager() {
        currentUnlockStateManager = new UnlockStateManager();
        lockManager = new LockManager(this.getApplicationContext());
    }

    private boolean initChannels(String specificDisplayNumber) {
        currentChannelListManager = new CurrentChannelListManager("all", ChannelUtils.getAllChannels(this));
        if (currentChannelListManager == null || currentChannelListManager.getCurrentChannelList().isEmpty()) {
            include_clNoChannelsAvailable.setVisibility(View.VISIBLE);
            btnNoChannelsAvailable.requestFocus();
            return false;
        }

        String channelDisplayNumber = specificDisplayNumber;
        if (TextUtils.isEmpty(channelDisplayNumber)) {
            if (!TextUtils.isEmpty(GlobalState.lastWatchedChannelNumber)) {
                channelDisplayNumber = GlobalState.lastWatchedChannelNumber;
            } else {
                String lastChannelId = GposInfo.getLastPlayChannelId(this);
                if (!TextUtils.isEmpty(lastChannelId)) {
                    String[] parts = lastChannelId.split("#");
                    if (parts.length >= 2) {
                        long sid = Long.parseLong(parts[0].trim());
                        long tsid = Long.parseLong(parts[1].trim());
                        long onid = -1;
                        if (parts.length >= 3) {
                            onid = Long.parseLong(parts[2].trim());
                        }
                        for (Channel ch : currentChannelListManager.getCurrentChannelList()) {
                            if (ch.getServiceId() == sid && ch.getTransportStreamId() == tsid
                                    && ch.getOriginalNetworkId() == onid) {
                                channelDisplayNumber = ch.getDisplayNumber();
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (TextUtils.isEmpty(channelDisplayNumber)) {
            if (!currentChannelListManager.getCurrentChannelList().isEmpty()) {
                currentChannelListManager.setCurrentChannelIndex(0);
            }
        } else {
            currentChannelListManager.setCurrentChannelIndexByDisplayNumber(channelDisplayNumber);
        }
        Log.d(TAG, "CurrentChannelList size:" + currentChannelListManager.getCurrentChannelList().size());

        if (nowPlayingManager == null) {
            nowPlayingManager = new NowPlayingManager(this.getApplicationContext());
        }

        return true;
    }

    private void showLiveTV() {
//        Log.d(TAG, "showLiveTV");
        epgFrameLayout.setVisibility(View.GONE);
        tuneChannel(currentChannelListManager.getCurrentChannel());
        TvViewUtils.resetTvViewToFullscreen(mTvView);
        resetFccContainerToFullscreen();
        updateLiveRecordingStatus();
    }

    private void showLiveOverlay(LiveOverlayState state) {
        Log.d(TAG, "showLiveOverlay state: " + state);

        if (state == LiveOverlayState.MUSIC) {
            clLiveOverlayMusic.setVisibility(View.VISIBLE);
            adLiveOverlayMusicAnimation.start();
        }

        if (state == LiveOverlayState.QR_ERROR_EVENT) {
            clLiveOverlayQrErrorEvent.setVisibility(View.VISIBLE);
        }

        if (state == LiveOverlayState.CHANNEL_BLOCK) {
            clLiveOverlayChannelBlock.setVisibility(View.VISIBLE);
            adLiveOverlayPadlockAnimation.start();
        }

        include_clLiveOverlay.setVisibility(View.VISIBLE);
    }

    private void hideLiveOverlay(LiveOverlayState state) {
        if (state == LiveOverlayState.QR_ERROR_EVENT && isVisible(clLiveOverlayQrErrorEvent)) {
            clLiveOverlayQrErrorEvent.setVisibility(View.GONE);
        } else if (state == LiveOverlayState.CHANNEL_BLOCK && isVisible(clLiveOverlayChannelBlock)) {
            clLiveOverlayChannelBlock.setVisibility(View.GONE);
            adLiveOverlayPadlockAnimation.stop();
        } else if (state == LiveOverlayState.MUSIC && isVisible(clLiveOverlayMusic)) {
            clLiveOverlayMusic.setVisibility(View.GONE);
            adLiveOverlayMusicAnimation.stop();
        } else { // LiveOverlayState.ALL
            clLiveOverlayQrErrorEvent.setVisibility(View.GONE);

            clLiveOverlayChannelBlock.setVisibility(View.GONE);
            adLiveOverlayPadlockAnimation.stop();

            clLiveOverlayMusic.setVisibility(View.GONE);
            adLiveOverlayMusicAnimation.stop();

            include_clLiveOverlay.setVisibility(View.GONE);
        }
    }

    private void showLiveRecordingStatus() {
        Log.d(TAG, "showLiveRecordingStatus");
        if (!Pvcfg.get_hideLauncherPvr()) {// eric lin 20251224 hide launcher pvr
            include_clLiveRecordingStatus.setVisibility(View.VISIBLE);
        }
    }

    private void hideLiveRecordingStatus() {
        include_clLiveRecordingStatus.setVisibility(View.GONE);
    }

    private void updateLiveRecordingStatus() {
        Log.d(TAG, "updateLiveRecordingStatus");
        if (!Pvcfg.get_hideLauncherPvr()) {// eric lin 20251224 hide launcher pvr
            List<ScheduledProgramData> nowRecordingPrograms = ScheduledProgramUtils
                    .getNowRecordingPrograms(getApplicationContext());

            if (!nowRecordingPrograms.isEmpty()) {
                tvLiveRecordingStatusChannel1.setText(nowRecordingPrograms.get(0).getChannelNumber());
                if (nowRecordingPrograms.size() > 1) {
                    tvLiveRecordingStatusChannel2.setText(nowRecordingPrograms.get(1).getChannelNumber());
                    tvLiveRecordingStatusPipe.setVisibility(View.VISIBLE);
                    tvLiveRecordingStatusChannel2.setVisibility(View.VISIBLE);
                } else {
                    tvLiveRecordingStatusPipe.setVisibility(View.GONE);
                    tvLiveRecordingStatusChannel2.setVisibility(View.GONE);
                }

                include_clLiveRecordingStatus.setVisibility(View.VISIBLE);
            } else {
                include_clLiveRecordingStatus.setVisibility(View.GONE);
            }
        }
    }

    private void showLiveChannelSearch() {
        Log.d(TAG, "showLiveChannelSearch");
        include_rlLiveChannelSearch.setVisibility(View.VISIBLE);
        llLiveChannelSearch.setVisibility(View.VISIBLE);
        rlLiveChannelSearchVoice.setVisibility(View.GONE);

        btnLiveChannelSearch.requestFocus();
    }

    private void hideLiveChannelSearch() {
        include_rlLiveChannelSearch.setVisibility(View.GONE);
        etLiveChannelSearch.setText("");
    }

    private void updateLiveChannelSearchResult(String searchKeyword) {
        Log.d(TAG, "updateLiveChannelSearchResult searchKeyword:" + searchKeyword);
        List<Channel> channelSearchResultList = new ArrayList<>();
        if (!TextUtils.isEmpty(searchKeyword) && !currentChannelListManager.getCurrentChannelList().isEmpty()) {
            List<Channel> channelSearchSourceList = currentChannelListManager.getCurrentChannelList();
            for (Channel ch : channelSearchSourceList) {
                if (ch.getDisplayName().toLowerCase().contains(searchKeyword.toLowerCase())) {
                    channelSearchResultList.add(ch);
                }
            }
        }

        Log.d(TAG, "updateLiveChannelSearchResult channelSearchResultList:" + channelSearchResultList.size());
        channelSearchAdapter.updateList(channelSearchResultList);
    }

    private void showMiniEpg(Channel ch, Program pg, int lockFlag) {
        hideAllLiveTvUiExceptView(include_clMiniEpg);

        if (ch == null) {
            Log.d(TAG, "showMiniEpg channel is null, return !");
            return;
        }

        String msg = "showMiniEpg channelId:" + ch.getId() + ", displayNum:" + ch.getDisplayNumber() + ", displayName:"
                + ch.getDisplayName();
        Log.d(TAG, msg);

        scheduleHideMiniEpg();

        boolean isRecording = false, hasRedKey = false;
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        boolean isFavorite = ChannelUtils.isChannelFavorite(this, ch);

        boolean isUnlocked = currentUnlockStateManager.isUnlocked(lockFlag);
        Log.d(TAG, "showMiniEpg highest priority lockFlag:" + lockFlag);
        Log.d(TAG, "showMiniEpg isUnlocked:" + isUnlocked);

        // UI: top
        tvMiniEpgChannelNumberTop.setText(StringUtils.padToNDigits(ch.getDisplayNumber(), 3));

        // UI: left side
        tvMiniEpgChannelNumber.setText(StringUtils.padToNDigits(ch.getDisplayNumber(), 3));

        if ((lockFlag == LockManager.LOCK_NONE) ||
                (lockFlag == LockManager.LOCK_WORK_HOUR && isUnlocked)) {
            ivMiniEpgLock.setVisibility(View.GONE);
        } else {
            ivMiniEpgLock.setVisibility(View.VISIBLE);
            if (isUnlocked) {
                // except LOCK_WORK_HOUR
                ivMiniEpgLock.setImageResource(R.drawable.icon_ch_unlock);
            } else {
                ivMiniEpgLock.setImageResource(R.drawable.icon_ch_lock);
            }
        }

        if ("SERVICE_TYPE_AUDIO".equals(ch.getServiceType())) {
            ivMiniEpgMusic.setVisibility(View.VISIBLE);
        } else {
            ivMiniEpgMusic.setVisibility(View.GONE);
        }

        ivMiniEpgStar.setVisibility(isFavorite ? View.VISIBLE : View.GONE);
        tvMiniEpgChannelName.setText(ch.getDisplayName());

        ViewUtils.setQualityIcon(ivMiniEpgQuality, TvViewUtils.getVideoResolutionLabel(mTvView));

        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        currentAudioTrackId = mTvView.getSelectedTrack(TvTrackInfo.TYPE_AUDIO);
        List<TvTrackInfo> audioTracks = mTvView.getTracks(TvTrackInfo.TYPE_AUDIO);
        boolean isDolby = false;
        if (audioTracks != null && !audioTracks.isEmpty())
            for (TvTrackInfo track : audioTracks) {
                if (track.getId().equalsIgnoreCase(currentAudioTrackId)) {
                    isDolby = isDolby(track);
                }
            }

        if (isDolby) {
            ivMiniEpgDolby.setVisibility(View.VISIBLE);
        } else {
            ivMiniEpgDolby.setVisibility(View.GONE);
        }

        // UI: center
        switch (isUnlocked ? LockManager.LOCK_NONE : lockFlag) {
            case LockManager.LOCK_ADULT_CHANNEL:
                setMiniEPGNowProgramState(MiniEpgState.ADULT_CHANNEL_LOCK);
                tvMiniEpgNowProgram.setText(getString(R.string.miniepg_adult_channel));
                break;
            case LockManager.LOCK_PARENTAL_CHANNEL:
                setMiniEPGNowProgramState(MiniEpgState.PARENTAL_CHANNEL_LOCK);
                tvMiniEpgNowProgram.setText(getString(R.string.miniepg_locked_channel));
                break;
            case LockManager.LOCK_PARENTAL_PROGRAM:
                setMiniEPGNowProgramState(MiniEpgState.PARENTAL_PROGRAM_LOCK);
                tvMiniEpgNowProgram.setText(getString(R.string.miniepg_parental_rated_program));
                break;
            case LockManager.LOCK_WORK_HOUR:
                setMiniEPGNowProgramState(MiniEpgState.WORK_HOUR_LOCK);
                tvMiniEpgNowProgram.setText(R.string.miniepg_locked_channel);
                break;
            case LockManager.LOCK_NONE:
            default:
                setMiniEPGNowProgramState(MiniEpgState.MAIN);

                if (pg != null) {
                    tvMiniEpgNowProgram.setText(pg.getTitle());
                    tvMiniEpgNowProgramTime.setText(
                            TimeUtils.formatToLocalTime(pg.getStartTimeUtcMillis(), "HH:mm") +
                                    " - " +
                                    TimeUtils.formatToLocalTime(pg.getEndTimeUtcMillis(), "HH:mm"));
                    ViewUtils.setRatingIcon(ivMiniEpgNowRating, pg.getContentRatings());

                    hasRedKey = true;
                    isRecording = (ScheduledProgramUtils.getScheduledProgram(getApplicationContext(),
                            pg.getId()) != null);
                    if (isRecording) {
                        if (!Pvcfg.get_hideLauncherPvr()) {// eric lin 20251224 hide launcher pvr
                            ivMiniEpgNowRecord.setVisibility(View.VISIBLE);
                        }
                    } else {
                        ivMiniEpgNowRecord.setVisibility(View.GONE);
                    }

                    ViewUtils.setProgressWithMillisMax(pbMiniEpgProgram, pg.getStartTimeUtcMillis(),
                            pg.getEndTimeUtcMillis());
                } else {
                    tvMiniEpgNowProgram.setText(getString(R.string.no_program_info));
                    tvMiniEpgNowProgramTime.setText("00:00 - 00:00");
                    ivMiniEpgNowRating.setVisibility(View.GONE);
                    ivMiniEpgNowRecord.setVisibility(View.GONE);
                    pbMiniEpgProgram.setProgress(0);
                }

                List<String> audioLanguageList = TvViewUtils.getAudioLanguageList(mTvView);
                tvMiniEpgBilingual.setVisibility((audioLanguageList.size() > 1) ? View.VISIBLE : View.GONE);

                List<String> subtitleLanguageList = TvViewUtils.getSubtitleLanguages(mTvView);
                tvMiniEpgSubtitle.setVisibility((subtitleLanguageList.size() > 1) ? View.VISIBLE : View.GONE);
                refreshMiniEpgLanguageStatus();

                // next program
                Program nextProgram = ProgramUtils.getNextProgram(this.getApplicationContext(), ch.getId());
                if (nextProgram != null) {
                    tvMiniEpgNextProgram.setText(nextProgram.getTitle());

                    if (ProgramReminderUtils.doesReminderExist(getApplicationContext(), nextProgram.getId())) {
                        if (!Pvcfg.get_hideLauncherPvr()) {// eric lin 20251229 hide reminder
                            ivMiniEpgNextReminder.setVisibility(View.VISIBLE);
                        }
                    } else {
                        ivMiniEpgNextReminder.setVisibility(View.GONE);
                    }

                    if (ScheduledProgramUtils.getScheduledProgram(getApplicationContext(),
                            nextProgram.getId()) != null) {
                        if (!Pvcfg.get_hideLauncherPvr()) {// eric lin 20251224 hide launcher pvr
                            ivMiniEpgNextRecord.setVisibility(View.VISIBLE);
                        }
                    } else {
                        ivMiniEpgNextRecord.setVisibility(View.GONE);
                    }
                } else {
                    tvMiniEpgNextProgram.setText(getString(R.string.no_program_info));
                    ivMiniEpgNextRecord.setVisibility(View.GONE);
                }
                break;
        }

        if (Pvcfg.get_hideLauncherPvr()) {// eric lin 20251224 hide launcher pvr
            tvMiniEpgRedKey.setVisibility(View.GONE);
        } else {
            tvMiniEpgRedKey.setVisibility(hasRedKey ? View.VISIBLE : View.GONE);
            tvMiniEpgRedKey.setText(isRecording ? getString(R.string.recording_setting_cancel_record)
                    : getString(R.string.caption_startrecording));
        }
        tvMiniEpgYellowKey
                .setText(isFavorite ? getString(R.string.delete_favourite) : getString(R.string.add_favourite));

        include_clMiniEpg.setVisibility(View.VISIBLE);
    }

    private void hideMiniEpg() {
        include_clMiniEpg.setVisibility(View.GONE);
    }

    private void refreshMiniEpgFavoriteStatus(Channel ch) {
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        boolean isFavorite = ChannelUtils.isChannelFavorite(this, ch);
        if (isFavorite)
            ChannelUtils.setFavorite(this, nowPlayingManager.getCurrentChannel(), 0);
        else
            ChannelUtils.setFavorite(this, nowPlayingManager.getCurrentChannel(), 1);
        isFavorite = !isFavorite;
        nowPlayingManager.updateCurrentChannel();
        ivMiniEpgStar.setVisibility(isFavorite ? View.VISIBLE : View.GONE);
        tvMiniEpgYellowKey
                .setText(isFavorite ? getString(R.string.delete_favourite) : getString(R.string.add_favourite));
    }

    private void showMiniEpgDescription(Channel ch) {
        if (ch == null) {
            Log.d(TAG, "showMiniEpgDescription channel is null, return !");
            return;
        }

        hideAllLiveTvUiExceptView(include_clMiniEpgDescription);

        String msg = "showMiniEpgDescription channelId:" + ch.getId() + ", displayNum:" + ch.getDisplayNumber()
                + ", displayName:" + ch.getDisplayName();
        Log.d(TAG, msg);

        tvMiniEpgDescriptionNumber.setText(ch.getDisplayNumber());
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        if (ChannelUtils.isChannelFavorite(this, ch))
            ivMiniEpgDescriptionStar.setVisibility(View.VISIBLE);
        else
            ivMiniEpgDescriptionStar.setVisibility(View.GONE);
        tvMiniEpgDescriptionChannelName.setText(ch.getDisplayName());
        ViewUtils.setQualityIcon(ivMiniEpgDescriptionQuality, TvViewUtils.getVideoResolutionLabel(mTvView));

        Program nowProgram = ProgramUtils.getCurrentProgram(this.getApplicationContext(), ch.getId());
        if (nowProgram != null) {
            tvMiniEpgDescriptionProgramName.setText(nowProgram.getTitle());
            ViewUtils.setRatingIcon(ivMiniEpgDescriptionRating, nowProgram.getContentRatings());

            List<String> audioLanguageList = TvViewUtils.getAudioLanguageList(mTvView);
            tvMiniEpgDescriptionBilingual.setVisibility((audioLanguageList.size() > 1) ? View.VISIBLE : View.GONE);

            llMiniEpgDescriptionProgressBar.setVisibility(View.VISIBLE);
            tvMiniEpgDescriptionProgramStart
                    .setText(TimeUtils.formatToLocalTime(nowProgram.getStartTimeUtcMillis(), "HH:mm"));
            tvMiniEpgDescriptionProgramEnd
                    .setText(TimeUtils.formatToLocalTime(nowProgram.getEndTimeUtcMillis(), "HH:mm"));
            ViewUtils.setProgressWithMillisMax(pbMiniEpgDescriptionProgram, nowProgram.getStartTimeUtcMillis(),
                    nowProgram.getEndTimeUtcMillis());
            tvMiniEpgDescription.setText(nowProgram.getLongDescription());
        } else {
            tvMiniEpgDescriptionProgramName.setText(getString(R.string.no_program_info));
            ivMiniEpgDescriptionRating.setVisibility(View.GONE);
            tvMiniEpgDescriptionBilingual.setVisibility(View.GONE);
            llMiniEpgDescriptionProgressBar.setVisibility(View.GONE);
            tvMiniEpgDescription.setText(getString(R.string.none));
        }

        svMiniEpgDescription.scrollTo(0, 0);
        svMiniEpgDescription.requestFocus();
        include_clMiniEpgDescription.setVisibility(View.VISIBLE);
    }

    private void hideMiniEpgDescription() {
        include_clMiniEpgDescription.setVisibility(View.GONE);
    }

    private void showLeftChannelList() {
        hideAllLiveTvUiExceptView(include_clLeftChannelList);
        int genreIndex = GenreData.getLiveTvGenreIndexById(GenreData.ID_FAVORITE_CHANNELS);
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        List<Channel> channelList = ChannelUtils.getAllFavoriteChannels(this);
        int favoriteChannelsCount = channelList != null ? channelList.size() : 0;
        Log.d("gary", "favoriteChannelsCount = " + favoriteChannelsCount);
        // If the favorite channel list is empty, show the all channel list by default
        if (favoriteChannelsCount == 0) {
            genreIndex = GenreData.getLiveTvGenreIndexById(GenreData.ID_ALL_CHANNELS);
        }
        switchLeftChannelListGenreByIndex(genreIndex);
        include_clLeftChannelList.setVisibility(View.VISIBLE);
        resetHandlerDelay(handler, refreshLeftChannelListRunnable, REFRESH_LEFT_CHANNEL_LIST_PERIOD_MS,
                "refreshLeftChannelList");
    }

    private void hideLeftChannelList() {
        leftChannelListAdapter.clearList();
        include_clLeftChannelList.setVisibility(View.GONE);
        handler.removeCallbacks(refreshLeftChannelListRunnable);
    }

    private void refreshLeftChannelList() {
        // LeftChannelList - Refresh Visible Program Info
        if (isVisible(rvLeftChannelList) && leftChannelListAdapter != null) {
            leftChannelListAdapter.refreshVisibleProgramInfo();
        }

        resetHandlerDelay(handler, refreshLeftChannelListRunnable, REFRESH_LEFT_CHANNEL_LIST_PERIOD_MS,
                "refreshLeftChannelList");
    }

    private void switchLeftChannelListGenreByIndex(int genreIndex) {
        List<GenreData.GenreInfo> genreList = GenreData.getAllLiveTvGenres();
        if (!genreList.isEmpty()) {
            if (genreIndex >= genreList.size()) {
                genreIndex = 0;
            } else if (genreIndex < 0) {
                genreIndex = genreList.size() - 1;
            }

            List<Channel> channelList;
            if (genreList.get(genreIndex).id == GenreData.ID_FAVORITE_CHANNELS) {
                // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
                // favorite
                channelList = ChannelUtils.getAllFavoriteChannels(this);
            } else if (genreList.get(genreIndex).id == GenreData.ID_ALL_CHANNELS) {
                channelList = ChannelUtils.getAllChannels(this);
            } else {
                // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
                // genre list
                channelList = ChannelUtils.getAllChannelsByGenre(this, genreList.get(genreIndex).id);
            }
            tvLeftChannelListTitle.setText(genreList.get(genreIndex).getName(this));
            leftChannelListAdapter.updateList(genreIndex, channelList, llLeftChannelListNoChannelList,
                    currentChannelListManager.getCurrentChannel());
        }
    }

    private int getAudioTrackIndex(String lang) {
        if (audioTracks != null) {
            for (int i = 0; i < audioTracks.size(); i++) {
                String audioLang = audioTracks.get(i).getLanguage();
                if (audioLang.equalsIgnoreCase("zho"))
                    audioLang = "chi";
                if (audioLang.equals(lang)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void showLivePreferredSetting() {
        if (mTvView != null) {
            selectedAudioTrackId = mTvView.getSelectedTrack(TvTrackInfo.TYPE_AUDIO);
            currentAudioTrackId = mTvView.getSelectedTrack(TvTrackInfo.TYPE_AUDIO);
            audioTracks.clear();
            if (!mTvView.getTracks(TvTrackInfo.TYPE_AUDIO).isEmpty())
                audioTracks.addAll(mTvView.getTracks(TvTrackInfo.TYPE_AUDIO));
            if (audioTracks != null && selectedAudioTrackId != null) {
                tvPreferredSettingAudioLangSelect.setText("  ✓  ");
                for (TvTrackInfo track : audioTracks) {
                    if (track.getId().equals(selectedAudioTrackId)) {
                        String lang = track.getLanguage();
                        if (lang != null && !lang.trim().isEmpty()) {
                            java.util.Locale locale = StringUtils.getJavaLocal(lang);
                            tvPreferredSettingAudioLang
                                    .setText(locale.getDisplayLanguage(java.util.Locale.getDefault()));
                        } else {
                            tvPreferredSettingAudioLang.setText(getString(R.string.undefined_language));
                        }
                        break;
                    }
                }
            }

            String currentSubtitleTrackId = mTvView.getSelectedTrack(TvTrackInfo.TYPE_SUBTITLE);
            subtitleTracks.clear();
            subtitleTracks.addAll(mTvView.getTracks(TvTrackInfo.TYPE_SUBTITLE));
            if (subtitleTracks != null && !subtitleTracks.isEmpty()) {
                tvPreferredSettingSubtitleSelect.setText("  ✓  ");
                String default_lang = "";
                for (int i = 0; i < subtitleTracks.size(); i++) {
                    if (default_lang.isEmpty() || subtitleTracks.get(i).getLanguage().equalsIgnoreCase("chi")) {
                        default_lang = subtitleTracks.get(i).getLanguage();
                        selectedSubtitleTrackIndex = i;
                    }
                    if (subtitleTracks.get(i).getId().equals(currentSubtitleTrackId)) {
                        default_lang = subtitleTracks.get(i).getLanguage();
                        selectedSubtitleTrackIndex = i;
                        break;
                    }
                }
                if (default_lang != null && !default_lang.trim().isEmpty()) {
                    // Log.d(TAG, "subtitle track.getLanguage = " + default_lang);
                    java.util.Locale locale = StringUtils.getJavaLocal(default_lang);
                    tvPreferredSettingSubtitle
                            .setText(locale.getDisplayLanguage(java.util.Locale.getDefault()));
                } else {
                    tvPreferredSettingSubtitle.setText(getString(R.string.undefined_language));
                }
            } else {
                selectedSubtitleTrackIndex = 0;
                tvPreferredSettingSubtitleSelect.setText("  ✓  ");
                java.util.Locale locale = StringUtils.getJavaLocal("chi");
                tvPreferredSettingSubtitle
                        .setText(locale.getDisplayLanguage(java.util.Locale.getDefault()));
            }
        }

        tvPreferredSettingLangSelect.setText("  ✓  ");
        if (currentSelectSettingLang.equals("zh"))
            tvPreferredSettingLang.setText(R.string.text_Chinese);
        else
            tvPreferredSettingLang.setText(R.string.text_English);
        selectSettingLang = currentSelectSettingLang;
        include_rlLivePreferredSetting.setVisibility(View.VISIBLE);
        btnPreferredSettingCancel.requestFocus();

        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        // implement preferred setting
    }

    private void hideLivePreferredSetting() {
        include_rlLivePreferredSetting.setVisibility(View.GONE);
    }

    private void showLiveSignalInfo() {
        long channelId = -1;

        if (currentChannelListManager != null) {
            Channel currentChannel = currentChannelListManager.getCurrentChannel();
            if (currentChannel != null) {
                channelId = currentChannel.getId();
            }
        }

        findViewById(R.id.live_signal_info_fragment_container).setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.live_signal_info_fragment_container,
                        LiveSignalInfoFragment.newInstance(channelId))
                .commit();
    }

    public void hideLiveSignalInfo() {
        findViewById(R.id.live_signal_info_fragment_container).setVisibility(View.GONE);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.live_signal_info_fragment_container);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }

    private void showEpg() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment = fm.findFragmentByTag(EPG_FRAGMENT_TAG);
        if (fragment != null) {
            ft.show(fragment).commit();
        } else {
            EpgFragment newFragment = EpgFragment.newInstance("epg_1");
            ft.add(R.id.epg_fragment_container, newFragment, EPG_FRAGMENT_TAG).commit();
        }
        epgFrameLayout.setVisibility(View.VISIBLE);
        include_clEpgTvFrame.setVisibility(View.VISIBLE);
        TvViewUtils.moveAndResizeTvView(this.getApplicationContext(), mTvView, 60, 76, 267, 151);
        moveAndResizeFccContainer(60, 76, 267, 151);
    }

    private void hideEpg() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment = fm.findFragmentByTag(EPG_FRAGMENT_TAG);
        if (fragment != null) {
            // ft.hide(fragment).commit();
            ft.remove(fragment).commit();
        }
        epgFrameLayout.setVisibility(View.GONE);
        include_clEpgTvFrame.setVisibility(View.GONE);
        resetFccContainerToFullscreen();
    }

    private void moveAndResizeFccContainer(int xDp, int yDp, int widthDp, int heightDp) {
        if (mFccContainer == null) {
            return;
        }
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mFccContainer.getLayoutParams();
        params.width = dpToPx(widthDp);
        params.height = dpToPx(heightDp);

        params.topToTop = ConstraintLayout.LayoutParams.UNSET;
        params.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
        params.startToStart = ConstraintLayout.LayoutParams.UNSET;
        params.endToEnd = ConstraintLayout.LayoutParams.UNSET;

        mFccContainer.setLayoutParams(params);
        mFccContainer.setTranslationX(dpToPx(xDp));
        mFccContainer.setTranslationY(dpToPx(yDp));
    }

    private void resetFccContainerToFullscreen() {
        if (mFccContainer == null) {
            return;
        }
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
        );

        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        params.setMargins(0, 0, 0, 0);

        mFccContainer.setLayoutParams(params);
        mFccContainer.setTranslationX(0);
        mFccContainer.setTranslationY(0);
    }

    private int dpToPx(int dpValue) {
        return (int) (dpValue * getResources().getDisplayMetrics().density + 0.5f);
    }

    private EpgFragment getEpgFragment() {
        return (EpgFragment) getSupportFragmentManager().findFragmentByTag(EPG_FRAGMENT_TAG);
    }

    private void inputNumberBufferTimeout() {
        handler.removeCallbacks(inputNumberBufferTimeoutRunnable);
        if (currentChannelListManager
                .setCurrentChannelIndexByDisplayNumber(StringUtils.normalizeInputNumber(inputNumberBuffer))) {
            tuneChannel(currentChannelListManager.getCurrentChannel());
        }

        clearInputNumberBuffer();
    }

    private void clearInputNumberBuffer() {
        include_clNumberKeyChannelList.setVisibility(View.GONE);
        numberKeyChannelAdapter.clearChannels();
        inputNumberBuffer = "";
        tvNumberKeyChannelNumberTop.setText("");
    }

    private void handleNumberInput(int number) {
        hideAllLiveTvUiExceptView(include_clNumberKeyChannelList);
        include_clNumberKeyChannelList.setVisibility(View.VISIBLE);

        inputNumberBuffer += number;
        if (inputNumberBuffer.length() >= MAX_INPUT_NUMBER_LENGTH) {
            if (currentChannelListManager
                    .setCurrentChannelIndexByDisplayNumber(StringUtils.normalizeInputNumber(inputNumberBuffer))) {
                clearInputNumberBuffer();
                tuneChannel(currentChannelListManager.getCurrentChannel());
                return;
            }
        } else {
            List<Channel> filteredChannels = filterChannelsByNumber(currentChannelListManager.getCurrentChannelList(),
                    StringUtils.normalizeInputNumber(inputNumberBuffer));
            if (!filteredChannels.isEmpty() || StringUtils.isOnlyZeros(inputNumberBuffer)) {
                if (!filteredChannels.isEmpty()) {
                    numberKeyChannelAdapter.updateList(filteredChannels);
                }
                tvNumberKeyChannelNumberTop.setText(inputNumberBuffer);
                resetHandlerDelay(handler, inputNumberBufferTimeoutRunnable, INPUT_NUMBER_TIMEOUT_MS,
                        "inputNumberBufferTimeout");
                return;
            }
        }

        if (!StringUtils.isOnlyZeros(inputNumberBuffer)) {
            // channel not found
            ViewUtils.showToast(this, Toast.LENGTH_LONG,
                    getString(R.string.format_toast_no_such_channel, inputNumberBuffer));
        }
        clearInputNumberBuffer();
    }

    private List<Channel> filterChannelsByNumber(List<Channel> channelList, String input) {
        List<Channel> result = new ArrayList<>();
        for (Channel channel : channelList) {
            if (String.valueOf(channel.getDisplayNumber()).startsWith(input)) {
                result.add(channel);
            }
        }
        return result;
    }

    private void scheduleHideMiniEpg() {
        GposInfo gposInfo = PrimeHomeplusTvApplication.get_prime_dtv_service().gpos_info_get();
        int bannaerTimeout = gposInfo != null ? GposInfo.getBannerTimeout(getApplicationContext()) * 1000
                : (int) AUTO_HIDE_MINIEPG_TIMEOUT_MS;
        handler.removeCallbacks(hideMiniEpgRunnable);
        handler.postDelayed(hideMiniEpgRunnable, bannaerTimeout);
    }

    private void setMiniEPGNowProgramState(MiniEpgState stateToShow) {
        for (Map.Entry<MiniEpgState, View> entry : miniEpgViewMap.entrySet()) {
            View view = entry.getValue();
            if (view != null) {
                view.setVisibility(entry.getKey() == stateToShow ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void channelUp() {
        Log.d(TAG, "channelUp");
        currentChannelListManager.channelIndexUp();
        tuneChannel(currentChannelListManager.getCurrentChannel());
    }

    private void channelDown() {
        Log.d(TAG, "channelDown");
        currentChannelListManager.channelIndexDown();
        tuneChannel(currentChannelListManager.getCurrentChannel());
    }

    private void tuneChannel(Channel ch) {
        if (ch != null) {
            String msg = "tuneChannel channelId:" + ch.getId() + ", displayNum:" + ch.getDisplayNumber()
                    + ", displayName:" + ch.getDisplayName();
            Log.d(TAG, msg);
            if (mCnsTickerManager != null) {
                mCnsTickerManager.setCurrentChannelDisplayNumber(ch.getDisplayNumber());
            }

            Program nowProgram = ProgramUtils.getCurrentProgram(this.getApplicationContext(), ch.getId());
            int lockFlag = lockManager.getHighestPriorityLockFlag(ch, nowProgram, null);
            // Log.d(TAG, "1111 ch : " + ch.getDisplayName() + " lockFlag : " + lockFlag);
            // If adult channel lock already unlocked, relock the adult channel lock when
            // switching to a non-adult channel
            currentUnlockStateManager.relockAdultChannelIfNeeded(lockFlag);
            currentUnlockStateManager.relockParentalProgramIfNeeded(lockFlag);
            boolean isUnlocked = currentUnlockStateManager.isUnlocked(lockFlag);
            // Log.d(TAG, "2222 ch : " + ch.getDisplayName() + " lockFlag : " + lockFlag);

            if (isVisible(epgFrameLayout)) {
                EpgFragment epgFragment = getEpgFragment();
                if (epgFragment != null) {
                    epgFragment.updateEpgChannelInfo(ch, lockFlag);
                    epgFragment.updateEpgProgramInfo(nowProgram, lockFlag);
                }

                if (nowProgram != null &&
                        (ScheduledProgramUtils.getScheduledProgram(getApplicationContext(),
                                nowProgram.getId()) != null)) {
                    if (!Pvcfg.get_hideLauncherPvr()) {// eric lin 20251224 hide launcher pvr
                        ivEpgTvFrameRecordingStatus.setVisibility(View.VISIBLE);
                    }
                } else {
                    ivEpgTvFrameRecordingStatus.setVisibility(View.GONE);
                }

                tvEpgTvFrameProgramName
                        .setText((nowProgram != null) ? nowProgram.getTitle() : getString(R.string.no_program_info));
            } else {
                showMiniEpg(ch, nowProgram, lockFlag);
            }

            if (lockFlag == LockManager.LOCK_NONE || isUnlocked) {
                if (isVisible(epgFrameLayout)) {
                    ivEpgTvFrameBlock.setVisibility(View.GONE);
                } else {
                    // clear MUSIC and LOCK, reset QR error code event
                    hideLiveOverlay(LiveOverlayState.ALL);
                    if ("SERVICE_TYPE_AUDIO".equals(ch.getServiceType())) {
                        showLiveOverlay(LiveOverlayState.MUSIC);
                    }
                }
                nowPlayingManager.tune(mTvView, ch);
            } else {
                if (isVisible(epgFrameLayout)) {
                    ivEpgTvFrameBlock.setVisibility(View.VISIBLE);
                } else {
                    showLiveOverlay(LiveOverlayState.CHANNEL_BLOCK);
                }
                nowPlayingManager.cancelPendingTune();
                nowPlayingManager.setCurrentChannel(ch);
                mTvView.reset();
            }
        }
    }

    private void updateTimer() {
        // MiniEpg - Date Info
        String time = new SimpleDateFormat("MM/dd(E)HH:mm").format(new Date());
        tvMiniEpgDate.setText(time);

        handler.postDelayed(updateTimerRunnable, UPDATE_TIMER_PERIOD_MS);
    }

    private void resetHandlerDelay(Handler h, Runnable runnable, long delayMillis, String tag) {
        Log.d(TAG, "resetHandlerDelay [" + tag + "] delay: " + delayMillis + " ms");
        h.removeCallbacks(runnable);
        h.postDelayed(runnable, delayMillis);
    }

    private void handleErrorCode(String errorCode) {
        Log.d(TAG, "handleErrorCode error code: " + errorCode);
        // Automatic Error Reporting Mechanism (AERM)
        if (ReportExporter.AERM_REPORT_CODES.contains(errorCode)) {
            ReportExporter.exportAermReport(errorCode, nowPlayingManager.getCurrentChannel());
        }

        // QrErrorEvent
        String errorMsg = ErrorMessageResolver.getErrorMessage(getApplicationContext(), errorCode);
        String qrErrorReport = ReportExporter.exportQrErrorReport(getApplicationContext(), errorCode,
                nowPlayingManager.getCurrentChannel());
        Bitmap qrErrorBitmap = QrCodeUtils.generateQRCode(qrErrorReport, 240);

        if (isVisible(epgFrameLayout)) {
            ivEpgTvFrameBlock.setVisibility(View.VISIBLE);
        } else {
            ivLiveOverlayQrCode.setImageBitmap(qrErrorBitmap);
            tvLiveOverlayQrErrorCode.setText(errorCode);
            tvLiveOverlayQrErrorMessage.setText(errorMsg);
            showLiveOverlay(LiveOverlayState.QR_ERROR_EVENT);
        }
    }

    private void setDefaultSubtitle() {
        subtitleTracks = mTvView.getTracks(TvTrackInfo.TYPE_SUBTITLE);
        Log.d(TAG, "setDefaultSubtitle subtitleTracks.size() = " + subtitleTracks.size());
        if (subtitleTracks != null && !subtitleTracks.isEmpty()) {
            mCurrentSubtitleIndex = 0;
            for (int i = 0; i < subtitleTracks.size(); i++) {
                String lang = subtitleTracks.get(i).getLanguage();
                Log.d(TAG, "setDefaultSubtitle subtitleTracks.get(" + i + ") = " + subtitleTracks.get(i));
                if (lang == null || lang.trim().isEmpty()) {
                    lang = "und"; // undefined
                } else {
                    lang = lang.trim().toLowerCase();
                    if (lang.equalsIgnoreCase(PrimeUtils.getDefaultSubtitleLang(getApplicationContext()))) {
                        mCurrentSubtitleIndex = i;
                        break;
                    }
                }
            }
            handleSubtitle();
        }
    }

    private void handleVideoAvailable() {
        Log.d(TAG, "handleVideoAvailable: mTvView visibility=" + mTvView.getVisibility());
        if (isVisible(clLiveOverlayQrErrorEvent) &&
                "E200".equals(tvLiveOverlayQrErrorCode.getText().toString())) {
            hideLiveOverlay(LiveOverlayState.QR_ERROR_EVENT);
        } else if (isVisible(epgFrameLayout) && isVisible(ivEpgTvFrameBlock)) {
            ivEpgTvFrameBlock.setVisibility(View.GONE);
        }
        if (PrimeUtils.getSubtitleOnOff(getApplicationContext()) == 1)
            setDefaultSubtitle();
    }

    private TvView.TvInputCallback mCallBack = new TvView.TvInputCallback() {
        @Override
        public void onTracksChanged(String inputId, List<TvTrackInfo> tracks) {
            Log.d(TAG, "TvView onTracksChanged: " + inputId);
            refreshMiniEpgLanguageStatus();
        }

        @Override
        public void onTrackSelected(String inputId, int type, String trackId) {
            Log.d(TAG, "TvView onTrackSelected: " + inputId + ", type=" + type + ", trackId=" + trackId);
            refreshMiniEpgLanguageStatus();
        }

        @Override
        public void onContentBlocked(String inputId, TvContentRating rating) {
            Log.d(TAG, "TvView onContentBlocked: " + inputId + ", "
                    + (null != rating ? rating.flattenToString() : "null"));
            super.onContentBlocked(inputId, rating);

            nowPlayingManager.setCurrentContentBlockedRating(rating);
            boolean isUnlocked = currentUnlockStateManager.isUnlocked(LockManager.LOCK_PARENTAL_PROGRAM);
            if (!isUnlocked) {
                if (isVisible(epgFrameLayout)) {
                    ivEpgTvFrameBlock.setVisibility(View.VISIBLE);
                } else {
                    showLiveOverlay(LiveOverlayState.CHANNEL_BLOCK);
                }
                mTvView.reset();
            }
        }

        public void onContentAllowed(String inputId) {
            Log.d(TAG, "TvView onContentAllowed:" + inputId);
        }

        @Override
        public void onVideoSizeChanged(String inputId, int width, int height) {
            super.onVideoSizeChanged(inputId, width, height);
            Log.d(TAG, "TvView onVideoSizeChanged: " + width + "x" + height);
        }

        @Override
        public void onVideoUnavailable(String inputId, int reason) {
            long now = System.currentTimeMillis();
            if (inputId.equals(lastInputId) && reason == lastReason && (now - lastTime < MIN_INTERVAL_MS)) {
                Log.d(TAG, "Duplicate onVideoUnavailable call ignored");
                return;
            } else {
                lastInputId = inputId;
                lastReason = reason;
                lastTime = now;
            }
            LogUtils.d("[CheckErrorMsg] reason = " + reason);
            super.onVideoUnavailable(inputId, reason);
            String reasonMsg = "none";
            if (reason == TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING) {
                reasonMsg = "TvInputManager.VIDEO_UNAVAILABLE_REASON_TUNING";
            } else if (reason == TvInputManager.VIDEO_UNAVAILABLE_REASON_WEAK_SIGNAL) {
                reasonMsg = "TvInputManager.VIDEO_UNAVAILABLE_REASON_WEAK_SIGNAL";
                handleErrorCode("E200");
            } else if (reason == TvInputManager.VIDEO_UNAVAILABLE_REASON_BUFFERING) {
                reasonMsg = "TvInputManager.VIDEO_UNAVAILABLE_REASON_BUFFERING";
            } else if (reason == TvInputManager.VIDEO_UNAVAILABLE_REASON_AUDIO_ONLY) {
                reasonMsg = "TvInputManager.VIDEO_UNAVAILABLE_REASON_AUDIO_ONLY";
            } else {
                String cns_error_code = PrimeUtils.convertErrorCode(getApplicationContext(), reason);
                if (cns_error_code != null && !cns_error_code.isEmpty()) {
                    LogUtils.d("[CheckErrorMsg] cns_error_code = " + cns_error_code);
                    handleErrorCode(cns_error_code);
                }
            }

            Log.d(TAG, "TvView onVideoUnavailable inputId: " + inputId + ", reason no: " + reason + ", reason msg: "
                    + reasonMsg);
        }

        public void onVideoAvailable(String inputId) {
            Log.d(TAG, "TvView onVideoAvailable inputId=" + inputId);
            handleVideoAvailable();
            refreshMiniEpgLanguageStatus();
        }
    };

    // AD Logic
    private int epgPosition = 0;
    private int position = -1;
    private int curEpgTime = -1;
    private int curAssetEpgTime = -1;
    private boolean isEpgPlayModeInterval = true;
    private boolean isEpgPlaySubModeByTime = true;
    private ADData miniEpgADData;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateEpg(EventEpgUpdate event) {
        try {
            if (event == null || event.getmADData() == null) {
                return;
            }
            miniEpgADData = event.getmADData();
            String etrTime = miniEpgADData.getEntryTime();
            Log.d(TAG, "epg etrTime : " + etrTime);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            try {
                date = simpleDateFormat.parse(etrTime);
            } catch (java.text.ParseException e) {
                throw new RuntimeException(e);
            }
            long startTime = date.getTime();
            long currentTime = System.currentTimeMillis();
            Log.d(TAG, "epg startTime : " + startTime + ", currentTime : " + currentTime);
            long realEpgPlayedTime = currentTime > startTime ? (currentTime - startTime) : 0;
            Log.d(TAG, "realEpgPlayedTime : " + realEpgPlayedTime);
            int totalEpgTime = 0;
            // 计算一轮广告总时间
            for (int index = 0; index < miniEpgADData.getDurationValue().size(); index++) {
                totalEpgTime += miniEpgADData.getDurationValue().get(index);
            }
            Log.d(TAG, "每一轮epg广告的播放总时间 : " + totalEpgTime);
            // 当前轮广告已播放时间
            int curEpgPlayedTime = Math.abs((int) (realEpgPlayedTime) % (totalEpgTime * 1000) / 1000);
            Log.d(TAG, "当前轮epg广告已播放时间 : " + curEpgPlayedTime);
            int tempTime = 0;
            for (int index = 0; index < miniEpgADData.getDurationValue().size(); index++) {
                if (tempTime + miniEpgADData.getDurationValue().get(index) > curEpgPlayedTime) {
                    // 当前轮广告播放节点下标
                    epgPosition = index;
                    Log.d(TAG, "当前轮epg广告图片数量: " + miniEpgADData.getAdImageList().get(index).size() + ", 下标："
                            + epgPosition);
                    isEpgPlayModeInterval = "interval".equals(miniEpgADData.getPlayMode().get(epgPosition));
                    isEpgPlaySubModeByTime = "bytime".equals(miniEpgADData.getPlayModeSubType().get(epgPosition));
                    Log.d(TAG, "isEpgPlayModeInterval: " + isEpgPlayModeInterval + ", isEpgPlaySubModeByTime: "
                            + isEpgPlaySubModeByTime);
                    if (isEpgPlaySubModeByTime) {
                        // 当前轮广告播放图片下标
                        int positionDiff = ((curEpgPlayedTime - tempTime) / miniEpgADData.getPlayModeValue().get(index))
                                % miniEpgADData.getAdImageList().get(index).size();
                        Log.d(TAG, "positionDiff：" + positionDiff);
                        position = positionDiff; // Simplified: assume start from 0
                        // 当前轮当前节点广告剩余播放时间
                        curEpgTime = tempTime + miniEpgADData.getDurationValue().get(index) - curEpgPlayedTime;
                        // 当前轮当前节点的当前广告图片剩余播放时间
                        curAssetEpgTime = miniEpgADData.getPlayModeValue().get(index)
                                - ((curEpgPlayedTime - tempTime) % miniEpgADData.getPlayModeValue().get(index));
                        if (curAssetEpgTime == 0) {
                            Log.d(TAG, "当前轮epg广告的图片顺播下一张，播放时间更新");
                            curAssetEpgTime = miniEpgADData.getPlayModeValue().get(index);
                        }
                    } else {
                        position = 0; // Simplified
                        curAssetEpgTime = miniEpgADData.getPlayModeValue().get(index);
                        curEpgTime = miniEpgADData.durationValue.get(epgPosition);
                    }
                    Log.d(TAG, "当前轮epg广告播放节点下标: " + epgPosition + ", 播放广告图片下标: " + position + ", 当前广告图片仍需要播放时间: "
                            + curAssetEpgTime);
                    break;
                } else {
                    tempTime += miniEpgADData.getDurationValue().get(index);
                }
            }
            if (position >= miniEpgADData.getAdImageList().get(epgPosition).size()) {
                position = (position - miniEpgADData.getAdImageList().get(epgPosition).size())
                        % miniEpgADData.getAdImageList().get(epgPosition).size();
            }
            Log.d(TAG, " updateEpg position : " + position);
            String filePath = miniEpgADData.getPathPrefix()
                    + miniEpgADData.getAdImageList().get(epgPosition).get(position).getAssetValue();
            Log.d(TAG, " updateEpg filePath : " + filePath);
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {
                if (null != ivMiniEpgAd) {
                    ivMiniEpgAd.setImageURI(Uri.fromFile(file));
                }
            } else {
                Log.d(TAG, "updateEpg file is not exist!");
            }
            if (miniEpgADData.playModeValue.get(epgPosition) > 0) {
                handler.removeCallbacks(updateEpgRunnable);
                handler.postDelayed(updateEpgRunnable, curAssetEpgTime * 1000);
            }
        } catch (Exception ignored) {
            Log.e(TAG, "updateEpg failed: " + ignored.getLocalizedMessage(), ignored);
        }
    }

    private Runnable updateEpgRunnable = new Runnable() {
        @Override
        public void run() {
            position = position + 1;
            if (position >= miniEpgADData.getAdImageList().get(epgPosition).size()) {
                position = (position - miniEpgADData.getAdImageList().get(epgPosition).size())
                        % miniEpgADData.getAdImageList().get(epgPosition).size();
            }
            String filePath = miniEpgADData.getPathPrefix()
                    + miniEpgADData.getAdImageList().get(epgPosition).get(position).getAssetValue();
            Log.d(TAG, "position : " + position + ",epgADData size : "
                    + miniEpgADData.getAdImageList().get(epgPosition).size());
            Log.d(TAG, " updateEpgRunnable filePath : " + filePath);
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {
                if (null != ivMiniEpgAd) {
                    ivMiniEpgAd.setImageURI(Uri.fromFile(file));
                }
            } else {
                Log.d(TAG, "updateEpgRunnable file is not exist!");
            }
            if (miniEpgADData.playModeValue.get(epgPosition) > 0) {
                handler.removeCallbacks(updateEpgRunnable);
                handler.postDelayed(updateEpgRunnable, miniEpgADData.playModeValue.get(epgPosition) * 1000);
            }
        }
    };

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "keyCode = " + keyCode);
        if (isVisible(epgFrameLayout)) {
            Log.d(TAG, "pass keyCode " + keyCode + " to epg");
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(EPG_FRAGMENT_TAG);
            if (fragment instanceof EpgFragment) {
                boolean handled = ((EpgFragment) fragment).handleKeyEvent(event);
                if (handled) {
                    return true;
                }
            }
            return false;
        }

        // 000blue signal info page
        handleKeySequence(keyCode);

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            handleBackKey();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            handleDpadCenterKey();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (handleDpadUpKey()) {
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            handleDpadLeftKey();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            handleDpadRightKey();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_CHANNEL_UP) {
            channelUp();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN) {
            channelDown();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            handleMenuKey();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_PROG_RED) { // RED
            handleRedKey();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_PROG_GREEN) { // GREEN
            handleGreenKeyAudio();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW) { // YELLOW
            handleYellowKey();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) { // BLUE
            handleBlueKey();
            return true;
        } else if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            handleNumberInput(keyCode - KeyEvent.KEYCODE_0);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
            handleMediaRewindKey(); // tune to previous channel
            return true;
        }
        return false;
    }

    private void handleGreenKeyAudio() {
        // 視 UI 狀態決定要不要處理，這裡先跟字幕一樣，只在 Live TV / MiniEPG 處理
        if (isVisible(include_clMiniEpgDescription)) {
            return;
        } else if (isVisible(include_clNumberKeyChannelList)) {
            return;
        } else if (isVisible(include_clLeftChannelList)) {
            return;
        }

        if (mTvView == null) {
            Log.w(TAG, "handleGreenKeyAudio: mTvView is null");
            return;
        }

        // 取得所有音軌
        audioTracks = mTvView.getTracks(TvTrackInfo.TYPE_AUDIO);
        if (audioTracks == null || audioTracks.isEmpty()) {
            Log.d(TAG, "handleGreenKeyAudio: no audio tracks");
            Toast.makeText(this, "無可用音軌", Toast.LENGTH_SHORT).show();
            return;
        }
        currentAudioTrackId = mTvView.getSelectedTrack(TvTrackInfo.TYPE_AUDIO);
        int audioIndex = findNextAudioTrackIndex();

        // ---------- 決定下一個 index（0..N-1 循環，不做 OFF 狀態） ----------
        if (audioIndex < 0 || audioIndex >= audioTracks.size()) {
            // 越界，從 0 開始
            audioIndex = 0;
        }

        TvTrackInfo track = audioTracks.get(audioIndex);
        if (track == null) {
            Log.w(TAG, "handleGreenKeyAudio: track is null at index " + audioIndex);
            return;
        }

        String id = track.getId();
        String lang = track.getLanguage();
        if (lang == null || lang.trim().isEmpty()) {
            lang = "und";
        } else {
            lang = lang.trim().toLowerCase();
        }

        Log.d(TAG, "handleGreenKeyAudio: switch audio index=" + audioIndex
                + " id=" + id + " lang=" + lang);
        currentAudioTrackId = id;
        // 切換到指定音軌
        mTvView.selectTrack(TvTrackInfo.TYPE_AUDIO, currentAudioTrackId);

        // 顯示簡單語言 label
        String label;
        switch (lang) {
            case "zh":
            case "zh_tw":
            case "zh-hant":
            case "chi":
                label = "中文";
                break;
            case "en":
            case "eng":
                label = "英文";
                break;
            case "jpn":
            case "ja":
                label = "日文";
                break;
            default:
                label = lang; // 其它語言就直接顯示 code
                break;
        }

        Toast.makeText(this, "音軌：" + label, Toast.LENGTH_SHORT).show();

        refreshMiniEpgLanguageStatus();
    }

    private void handleSubtitle() {
        // 若超過最後一條 → 關閉字幕
        if (mCurrentSubtitleIndex >= subtitleTracks.size()) {
            Log.d(TAG, "handleBlueKey: turn subtitle OFF");
            mTvView.selectTrack(TvTrackInfo.TYPE_SUBTITLE, null);
            mCurrentSubtitleIndex = -1;

            Toast.makeText(this, "字幕：關閉", Toast.LENGTH_SHORT).show();
            // 這裡就 return，因為已經處理完「關閉」
            return;
        }

        // ---------- 根據 mCurrentSubtitleIndex 選字幕 ----------
        TvTrackInfo track = subtitleTracks.get(mCurrentSubtitleIndex);
        if (track == null) {
            Log.w(TAG, "handleBlueKey: track is null at index " + mCurrentSubtitleIndex);
            mTvView.setCaptionEnabled(false);
            mTvView.selectTrack(TvTrackInfo.TYPE_SUBTITLE, null);
            mCurrentSubtitleIndex = -1;
            Toast.makeText(this, "字幕：關閉", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = track.getId();
        String lang = track.getLanguage();
        if (lang == null || lang.trim().isEmpty()) {
            lang = "und";
        } else {
            lang = lang.trim().toLowerCase();
        }

        Log.d(TAG, "handleBlueKey: switch subtitle index=" + mCurrentSubtitleIndex
                + " id=" + id + " lang=" + lang);
        mTvView.setCaptionEnabled(true);
        mTvView.selectTrack(TvTrackInfo.TYPE_SUBTITLE, id);

        // 顯示語言名稱（你可以再做 mapping）
        String label;
        switch (lang) {
            case "zh":
            case "zh_tw":
            case "zh-hant":
            case "chi":
                label = "中文";
                break;
            case "en":
            case "eng":
                label = "英文";
                break;
            default:
                label = lang; // 其它語言就直接顯示 code
                break;
        }

        Toast.makeText(this, "字幕：" + label, Toast.LENGTH_SHORT).show();

        refreshMiniEpgLanguageStatus();
    }

    private void handleBlueKey() {
        // 視 UI 狀態決定要不要處理，這裡我只在 Live TV / MiniEPG 處理
        if (isVisible(include_clMiniEpgDescription)) {
            // 目前不在這裡處理，有需要可以自己加
            return;
        } else if (isVisible(include_clNumberKeyChannelList)) {
            return;
        } else if (isVisible(include_clLeftChannelList)) {
            return;
        }

        if (mTvView == null) {
            Log.w(TAG, "handleBlueKey: mTvView is null");
            return;
        }

        subtitleTracks = mTvView.getTracks(TvTrackInfo.TYPE_SUBTITLE);

        if (subtitleTracks == null || subtitleTracks.isEmpty()) {
            Log.d(TAG, "handleBlueKey: no subtitle tracks");
            Toast.makeText(this, "無可用字幕", Toast.LENGTH_SHORT).show();
            // 確保 index 也歸零
            mCurrentSubtitleIndex = -1;
            return;
        }

        // ---------- 決定下一個 index ----------
        if (mCurrentSubtitleIndex == -1) {
            // 現在是「關閉字幕」→ 第一次按，切到第 0 條
            mCurrentSubtitleIndex = 0;
        } else {
            // 已經有選某一條字幕 → 下一次按就 +1
            mCurrentSubtitleIndex++;
        }

        handleSubtitle();
    }

    private void refreshMiniEpgLanguageStatus() {
        if (mTvView == null || llMiniEpgGreenKey == null || llMiniEpgBlueKey == null) {
            return;
        }

        List<TvTrackInfo> audioTrackList = mTvView.getTracks(TvTrackInfo.TYPE_AUDIO);
        boolean showAudio = audioTrackList != null && audioTrackList.size() > 1;
        llMiniEpgGreenKey.setVisibility(showAudio ? View.VISIBLE : View.GONE);
        if (showAudio) {
            TvTrackInfo audioTrack = getTrackById(audioTrackList,
                    mTvView.getSelectedTrack(TvTrackInfo.TYPE_AUDIO));
            tvMiniEpgBilingualNow.setText(getTrackDisplayLanguage(audioTrack));
        }

        List<TvTrackInfo> subtitleTrackList = mTvView.getTracks(TvTrackInfo.TYPE_SUBTITLE);
        boolean showSubtitle = subtitleTrackList != null && subtitleTrackList.size() > 1;
        llMiniEpgBlueKey.setVisibility(showSubtitle ? View.VISIBLE : View.GONE);
        if (showSubtitle) {
            String subtitleId = mTvView.getSelectedTrack(TvTrackInfo.TYPE_SUBTITLE);
            if (subtitleId == null || subtitleId.trim().isEmpty()) {
                tvMiniEpgSubtitleNow.setText(getString(R.string.close));
            } else {
                TvTrackInfo subtitleTrack = getTrackById(subtitleTrackList, subtitleId);
                tvMiniEpgSubtitleNow.setText(getTrackDisplayLanguage(subtitleTrack));
            }
        }
    }

    private TvTrackInfo getTrackById(List<TvTrackInfo> tracks, String trackId) {
        if (tracks == null || tracks.isEmpty()) {
            return null;
        }
        if (trackId == null) {
            return tracks.get(0);
        }
        for (TvTrackInfo track : tracks) {
            if (trackId.equals(track.getId())) {
                return track;
            }
        }
        return tracks.get(0);
    }

    private String getTrackDisplayLanguage(TvTrackInfo track) {
        if (track == null) {
            return getString(R.string.undefined_language);
        }
        String lang = track.getLanguage();
        if (lang == null || lang.trim().isEmpty()) {
            return getString(R.string.undefined_language);
        }
        java.util.Locale locale = StringUtils.getJavaLocal(lang.trim());
        String display = locale.getDisplayLanguage(java.util.Locale.getDefault());
        return (display == null || display.trim().isEmpty()) ? lang : display;
    }

    private boolean handleKeySequence(int keyCode) {
        keySequenceBuffer.add(keyCode);

        if (keySequenceBuffer.size() > SIGNAL_INFO_SEQUENCE.length) {
            keySequenceBuffer.remove(0);
        }

        if (keySequenceBuffer.size() == SIGNAL_INFO_SEQUENCE.length) {
            boolean matched = true;
            for (int i = 0; i < SIGNAL_INFO_SEQUENCE.length; i++) {
                if (keySequenceBuffer.get(i) != SIGNAL_INFO_SEQUENCE[i]) {
                    matched = false;
                    break;
                }
            }

            if (matched) {
                keySequenceBuffer.clear();
                // show signal info
                showLiveSignalInfo();
                return true;
            }
        }

        return false;
    }

    private void handleBackKey() {
        if (isVisible(include_clMiniEpgDescription)) {
            hideMiniEpgDescription();
        } else if (isVisible(include_clMiniEpg)) {
            hideMiniEpg();
        } else if (isVisible(include_clNumberKeyChannelList)) {
            clearInputNumberBuffer();
        } else if (isVisible(include_clLeftChannelList)) {
            hideLeftChannelList();
        } else {
            finish();
        }
    }

    private void handleDpadCenterKey() {
        if (isVisible(include_clMiniEpgDescription)) {
            hideMiniEpgDescription();
        } else if (isVisible(include_clMiniEpg)) {
            Program nowProgram = ProgramUtils.getCurrentProgram(this.getApplicationContext(),
                    nowPlayingManager.getCurrentChannel().getId());
            int lockFlag = lockManager.getHighestPriorityLockFlag(nowPlayingManager.getCurrentChannel(), nowProgram,
                    nowPlayingManager.getCurrentContentBlockedRating());
            boolean isUnlocked = currentUnlockStateManager.isUnlocked(lockFlag);
            if (lockFlag != LockManager.LOCK_NONE && !isUnlocked) {
                showParentalPinDialog();
            } else {
                hideMiniEpg();
                showMiniEpgDescription(nowPlayingManager.getCurrentChannel());
            }
        } else if (isVisible(include_clNumberKeyChannelList)) {
        } else if (isVisible(include_clLeftChannelList)) {
        } else {
            if (nowPlayingManager == null) {
                Log.d(TAG, "nowPlayingManager null 1");
            }

            if (nowPlayingManager.getCurrentChannel() == null) {
                Log.d(TAG, "nowPlayingManager null 2");
            }
            Program nowProgram = ProgramUtils.getCurrentProgram(this.getApplicationContext(),
                    nowPlayingManager.getCurrentChannel().getId());
            int lockFlag = lockManager.getHighestPriorityLockFlag(nowPlayingManager.getCurrentChannel(), nowProgram,
                    nowPlayingManager.getCurrentContentBlockedRating());
            showMiniEpg(nowPlayingManager.getCurrentChannel(), nowProgram, lockFlag);
        }
    }

    private boolean handleDpadUpKey() {
        if (isVisible(include_clMiniEpgDescription)) {
        } else if (isVisible(include_clMiniEpg)) {
            hideMiniEpg();
            showLiveChannelSearch();
        } else if (isVisible(include_clNumberKeyChannelList)) {
        } else if (isVisible(include_clLeftChannelList)) {
            return false;
        } else {
            showLiveChannelSearch();
        }

        return true;
    }

    private void handleDpadLeftKey() {
        if (isVisible(include_clMiniEpgDescription)) {
        } else if (isVisible(include_clMiniEpg)) {
            hideMiniEpg();
            showLeftChannelList();
        } else if (isVisible(include_clNumberKeyChannelList)) {
        } else if (isVisible(include_clLeftChannelList)) {
            switchLeftChannelListGenreByIndex(leftChannelListAdapter.getCurrentGenreIndex() - 1);
        } else {
            showLeftChannelList();
        }
    }

    private void handleDpadRightKey() {
        if (isVisible(include_clMiniEpgDescription)) {
        } else if (isVisible(include_clMiniEpg)) {
        } else if (isVisible(include_clNumberKeyChannelList)) {
        } else if (isVisible(include_clLeftChannelList)) {
            switchLeftChannelListGenreByIndex(leftChannelListAdapter.getCurrentGenreIndex() + 1);
        }
    }

    private void handleMenuKey() {
        if (isVisible(include_clMiniEpgDescription)) {
        } else if (isVisible(include_clNumberKeyChannelList)) {
        } else if (isVisible(include_clLeftChannelList)) {
        } else { // include isVisible(include_clMiniEpg)
            showLivePreferredSetting();
        }
    }

    private void handleRedKey() {
        // for ticker view test
        boolean TICKER_VIEW_TEST = false;
        if (TICKER_VIEW_TEST && mCnsTickerManager != null) {
            mCnsTickerManager.toggleTestMode();
        }

        if (isVisible(include_clMiniEpgDescription)) {
        } else if (isVisible(include_clNumberKeyChannelList)) {
        } else if (isVisible(include_clLeftChannelList)) {
        } else { // include isVisible(include_clMiniEpg)
            Program nowProgram = ProgramUtils.getCurrentProgram(this.getApplicationContext(),
                    nowPlayingManager.getCurrentChannel().getId());
            int lockFlag = lockManager.getHighestPriorityLockFlag(nowPlayingManager.getCurrentChannel(), nowProgram,
                    nowPlayingManager.getCurrentContentBlockedRating());
            boolean isUnlocked = currentUnlockStateManager.isUnlocked(lockFlag);
            if (lockFlag != LockManager.LOCK_NONE && !isUnlocked) {
            } else {
                if (!Pvcfg.get_hideLauncherPvr()) {// eric lin 20251224 hide launcher pvr
                    if ((nowPlayingManager.getCurrentChannel() != null) && (nowProgram != null)) {
                        recordingSettingPopup.setScheduledProgramInfo(nowProgram,
                                nowPlayingManager.getCurrentChannel());

                        recordingSettingPopup.showBottomBar(true);
                    }
                }
            }
        }
    }

    private void handleYellowKey() {
        if (isVisible(include_clMiniEpgDescription)) {
        } else if (isVisible(include_clMiniEpg)) {
            // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
            // set favorite
            refreshMiniEpgFavoriteStatus(nowPlayingManager.getCurrentChannel());
        } else if (isVisible(include_clNumberKeyChannelList)) {
        } else if (isVisible(include_clLeftChannelList)) {
        } else {
            // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
            // set favorite
            refreshMiniEpgFavoriteStatus(nowPlayingManager.getCurrentChannel());
        }
    }

    private void handleMediaRewindKey() {
        if (nowPlayingManager != null) {
            Channel ch = nowPlayingManager.getPreviousChannel();
            if (ch != null) {
                currentChannelListManager.setCurrentChannelIndexByDisplayNumber(ch.getDisplayNumber());
                tuneChannel(currentChannelListManager.getCurrentChannel());
            }
        }
    }

    private static boolean isVisible(View view) {
        return view.getVisibility() == View.VISIBLE;
    }

    private void hideAllLiveTvUiExceptView(View reservedView) {
        Set<View> reservedViews = new HashSet<>();
        if (reservedView != null) {
            reservedViews.add(reservedView);
        }
        hideAllLiveTvUiExceptViews(reservedViews);
    }

    private void hideAllLiveTvUiExceptViews(Set<View> reservedViews) {
        tryCloseUiExcept(include_clMiniEpg, this::hideMiniEpg, reservedViews);
        tryCloseUiExcept(include_clMiniEpgDescription, this::hideMiniEpgDescription, reservedViews);
        tryCloseUiExcept(include_clNumberKeyChannelList, this::clearInputNumberBuffer, reservedViews);
        tryCloseUiExcept(include_clLeftChannelList, this::hideLeftChannelList, reservedViews);
    }

    private void tryCloseUiExcept(View view, Runnable action, Set<View> reservedViews) {
        if (view != null && view.getVisibility() == View.VISIBLE && !containsView(reservedViews, view)) {
            action.run();
        }
    }

    private boolean containsView(Set<View> views, View target) {
        for (View v : views) {
            if (v != null && v.getId() == target.getId()) {
                return true;
            }
        }
        return false;
    }

    private void updateAudioTrackUI(TvTrackInfo track) {
        selectedAudioTrackId = track.getId();
        if (currentAudioTrackId.equals(selectedAudioTrackId))
            tvPreferredSettingAudioLangSelect.setText("  ✓  ");
        else
            tvPreferredSettingAudioLangSelect.setText("     ");
        String lang = track.getLanguage();
        Log.d("gary", "modify track.getLanguage = " + lang);
        if (lang != null && !lang.trim().isEmpty()) {
            java.util.Locale locale = StringUtils.getJavaLocal(lang);
            tvPreferredSettingAudioLang.setText(locale.getDisplayLanguage(java.util.Locale.getDefault()));
        } else {
            tvPreferredSettingAudioLang.setText(getString(R.string.undefined_language));
        }
    }

    private void showLeftAudioTrack() {
        if (audioTracks != null && !audioTracks.isEmpty()) {
            for (int i = 0; i < audioTracks.size(); i++) {
                if (audioTracks.get(i).getId().equals(selectedAudioTrackId)) {
                    int newIndex = (i - 1 + audioTracks.size()) % audioTracks.size();
                    updateAudioTrackUI(audioTracks.get(newIndex));
                    break;
                }
            }
        }
    }

    private void showRightAudioTrack() {
        if (audioTracks != null && !audioTracks.isEmpty()) {
            for (int i = 0; i < audioTracks.size(); i++) {
                if (audioTracks.get(i).getId().equals(selectedAudioTrackId)) {
                    int newIndex = (i + 1) % audioTracks.size();
                    updateAudioTrackUI(audioTracks.get(newIndex));
                    break;
                }
            }
        }
    }

    private int findNextAudioTrackIndex() {
        if (audioTracks != null && !audioTracks.isEmpty()) {
            for (int i = 0; i < audioTracks.size(); i++) {
                if (audioTracks.get(i).getId().equals(currentAudioTrackId)) {
                    return ((i + 1) % audioTracks.size());
                }
            }
        }
        return -1;
    }

    private void selectAudioTrack() {
        if (!selectedAudioTrackId.equals(currentAudioTrackId)) {
            tvPreferredSettingAudioLangSelect.setText("  ✓  ");
            currentAudioTrackId = selectedAudioTrackId;
            mTvView.selectTrack(TvTrackInfo.TYPE_AUDIO, selectedAudioTrackId);
        }
    }

    private String getSubtitleLang(int index) {
        if (!subtitleTracks.isEmpty() && index < subtitleTracks.size()) {
            return subtitleTracks.get(index).getLanguage();
        } else {
            if (index == 0)
                return "chi";
            else
                return "eng";
        }
    }

    private void updateSubtitleTrackUI() {
        if (mCurrentSubtitleIndex == selectedSubtitleTrackIndex)
            tvPreferredSettingSubtitleSelect.setText("  ✓  ");
        else
            tvPreferredSettingSubtitleSelect.setText("     ");
        String lang = getSubtitleLang(selectedSubtitleTrackIndex);
        Log.d(TAG, "modify track.getLanguage = " + lang);
        if (lang != null && !lang.trim().isEmpty()) {
            java.util.Locale locale = StringUtils.getJavaLocal(lang);
            tvPreferredSettingSubtitle.setText(locale.getDisplayLanguage(java.util.Locale.getDefault()));
        } else {
            tvPreferredSettingSubtitle.setText(getString(R.string.undefined_language));
        }
    }

    private void showLeftSubtitleTrack() {
        if (subtitleTracks != null && !subtitleTracks.isEmpty()) {
            if (selectedSubtitleTrackIndex <= 0)
                selectedSubtitleTrackIndex = subtitleTracks.size() - 1;
            else
                selectedSubtitleTrackIndex--;
            updateSubtitleTrackUI();
        } else {
            selectedSubtitleTrackIndex = (selectedSubtitleTrackIndex - 1 + 2) % 2;
            updateSubtitleTrackUI();
        }
    }

    private void showRightSubtitleTrack() {
        if (subtitleTracks != null && !subtitleTracks.isEmpty()) {
            if (selectedSubtitleTrackIndex < (subtitleTracks.size() - 1))
                selectedSubtitleTrackIndex++;
            else
                selectedSubtitleTrackIndex = 0;
            updateSubtitleTrackUI();
        } else {
            selectedSubtitleTrackIndex = (selectedSubtitleTrackIndex + 1) % 2;
            updateSubtitleTrackUI();
        }
    }

    private void selectSubtitleTrack() {
        if (mCurrentSubtitleIndex != selectedSubtitleTrackIndex) {
            tvPreferredSettingSubtitleSelect.setText("  ✓  ");
            mCurrentSubtitleIndex = selectedSubtitleTrackIndex;
            if (!subtitleTracks.isEmpty() && mCurrentSubtitleIndex < subtitleTracks.size()) {
                String selectedAudioTrackId = subtitleTracks.get(mCurrentSubtitleIndex).getId();
                mTvView.selectTrack(TvTrackInfo.TYPE_SUBTITLE, selectedAudioTrackId);
            }
        }
    }

    private void dobtnPreferredSettingSave() {
        if (!currentSelectSettingLang.equalsIgnoreCase(selectSettingLang)) {
            selectSettingLang();
        }

        if (!selectedAudioTrackId.equals(currentAudioTrackId)) {
            mTvView.selectTrack(TvTrackInfo.TYPE_AUDIO, selectedAudioTrackId);
        }

        mCurrentSubtitleIndex = selectedSubtitleTrackIndex;
        handleSubtitle();
    }

    private void showDiffSettingLang() {
        if (selectSettingLang.equals("zh")) {
            tvPreferredSettingLang.setText(R.string.text_English);
            selectSettingLang = "en";
        } else {
            tvPreferredSettingLang.setText(R.string.text_Chinese);
            selectSettingLang = "zh";
        }
        if (currentSelectSettingLang.equals(selectSettingLang))
            tvPreferredSettingLangSelect.setText("  ✓  ");
        else
            tvPreferredSettingLangSelect.setText("     ");
    }

    private void selectSettingLang() {
        currentSelectSettingLang = selectSettingLang;
        tvPreferredSettingLangSelect.setText("  ✓  ");
        String lang = "";
        if (currentSelectSettingLang.equals("zh")) {
            int audioTrackIndex = getAudioTrackIndex("chi");
            lang = "chi";
            if (audioTrackIndex != -1) {
                updateAudioTrackUI(audioTracks.get(audioTrackIndex));
                selectAudioTrack();
            }
        } else if (currentSelectSettingLang.equals("en")) {
            int audioTrackIndex = getAudioTrackIndex("eng");
            lang = "eng";
            if (audioTrackIndex != -1) {
                updateAudioTrackUI(audioTracks.get(audioTrackIndex));
                selectAudioTrack();
            }
        }
        int ret = PrimeHomeplusTvApplication.get_prime_dtv_service().av_control_reset_audio_default_language(lang);
    }

    private boolean isDolby(TvTrackInfo tvTrackInfo) {
        if (tvTrackInfo == null || tvTrackInfo.getEncoding() == null)
            return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            return (tvTrackInfo.getEncoding().equals("audio/ac3") || tvTrackInfo.getEncoding().equals("audio/eac3"));
        return false;
    }

    public void handleTvMessage(@NonNull TVMessage msg) {
//        Log.d(TAG, "handleTvMessage: " + msg.getMsgType());
        // 在這裡處理來自 Application 的訊息
        switch (msg.getMsgType()) {
            case TVMessage.TYPE_AV_FCC_VISIBLE: {
                int tunerId = msg.getAvTunerId();
                LogUtils.d("FCC_LOG handleTvMessage TYPE_AV_FCC_VISIBLE tunerId=" + tunerId);
                if (tunerId < 0) {
                    promoteFccSurface(-1);
                } else {
                    requestPromoteAndTune(tunerId, "TYPE_AV_FCC_VISIBLE");
                }
            }
                break;
            case TVMessage.TYPE_SYSTEM_SI_UPDATE_CHANNEL_LIST: {
                Log.d(TAG, "HomePlus TV - call UI to update channel list");
                initChannels("");
                PrimeHomeplusTvApplication.get_prime_dtv_service()
                        .av_control_change_channel_manager_list_update(FavGroup.ALL_TV_TYPE);
            }
                break;
            case TVMessage.TYPE_SYSTEM_SI_UPDATE_SET_CHANNEL: {
                Log.d(TAG, "HomePlus TV - TYPE_SYSTEM_SI_UPDATE_SET_CHANNEL");
                // g_liveTvMgr.close_ca_message();
                // g_liveTvMgr.g_musicTv.hide_error_code();
                // g_liveTvMgr.g_chChangeMgr.change_channel_stop(0, 0);
                // g_liveTvMgr.g_chChangeMgr.change_channel_by_id(msg.getChannelId(), true);
                currentChannelListManager.setCurrentChannel(PrimeUtils.getProgramInfo(msg.getChannelId()));
                tuneChannel(currentChannelListManager.getCurrentChannel());
            }
                break;
            case TVMessage.TYPE_SYSTEM_SI_UPDATE_SET_1ST_CHANNEL: {
                Log.d(TAG, "HomePlus TV - call UI to set channel");
                currentChannelListManager.setCurrentChannelIndex(0);
                tuneChannel(currentChannelListManager.getCurrentChannel());
            }
                break;
        }
    }
}
