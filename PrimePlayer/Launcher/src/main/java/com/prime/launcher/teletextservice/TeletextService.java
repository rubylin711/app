package com.prime.launcher.teletextservice;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import com.prime.launcher.HomeActivity;
import com.prime.launcher.HomeApplication;
import com.prime.launcher.R;
import com.prime.launcher.teletextservice.decoder.TeletextManager;
import com.prime.launcher.teletextservice.fullpageteletext.FttxNavigationBase;
import com.prime.launcher.teletextservice.fullpageteletext.FttxNavigationTop;
import com.prime.launcher.teletextservice.fullpageteletext.FttxPacket0;
import com.prime.launcher.teletextservice.fullpageteletext.FttxPacket1_25;
import com.prime.launcher.teletextservice.fullpageteletext.FttxPacket26;
import com.prime.launcher.teletextservice.fullpageteletext.FttxPacket27;
import com.prime.launcher.teletextservice.fullpageteletext.FttxPage;
import com.prime.launcher.teletextservice.pagestable.PagesDatabase;

public class TeletextService extends Service implements KeyEventAwareFrameLayout.KeyEventListener {
    private static final String TAG = "TeletextService";
    private static final boolean DEBUG = false;
    private static final int TELETEXT_INDEX_PAGE = 100;
    private static final int TELETEXT_MAX_PAGE = 899;
    private static final boolean TOP_NAVIGATION_ENABLED = true;
    private static int NAVIGATION_TIME_IN_MSEC = 5000; // In milliseconds
    private static final int INDEX_PAGE_QUERY_TIMEOUT = 30000; // In milliseconds
    private static final int INDEX_PAGE_SHOW_MSG_TIMEOUT = 5000; // In milliseconds
    private static final int PAGE_HEADER_DATA_BYTE = 13;
    private static final int PACKET1_25_DATABYTEPOS = 6;
    private static final int PACKET26_DATABYTEPOS = 7;
    private static final int PACKET27_DATABYTEPOS = 6;
    private static final int PKT0_DATABYTEPOS = 37;
    private KeyEventAwareFrameLayout mRootContainer;
    private BroadcastReceiver mHomeReceiver;

    public static boolean isRunning = false;
    WindowManager.LayoutParams params;
    WindowManager windowManager;
    FttxPage mPage;
    FttxPacket0 mPacket0;
    FttxPacket1_25 mPacket1_25;
    FttxPacket26 mPacket26;
    FttxPacket27 mPacket27;
    FttxNavigationBase mNavigation;
    boolean mHasPacket27 = false;
    boolean mHasPacket24 = false;
    TeletextPainter mTeletext;
    int packet1_25Index = 46;
    int page_number_db = 100;
    int TeletextPid=8191;
    private Integer pageKey = -1;
    boolean isExecuted = false;
    Messenger mMessenger;
    int curPageNum;
    int mPrevPageNo = -1;
    Cursor dbCursor;
    boolean mIsIndexPage;
    boolean freezeKeyPressed;
    boolean pageUp;
    boolean pageDown;
    boolean isDecoderStarted;
    Surface mRenderingSurface;
    PageSettings mPageSettings;
    int mTransparency;
    String[] packetItems;
    Context mContext; // for instrumentation testing
    Bitmap mBitmap; // for instrumentation testing
    boolean mInstrumentationTesting;
    Handler mHandler;
    boolean subtitlePage;
    TeletextHandler mMainHandler;
    ServiceStatus mServiceStatus;

    private String pageNumberString;
    private int nbKeysPressed;
    private TeletextManager teletextManager;
    private Surface mSurface;
    private volatile boolean isWaitingForPage = false;

    /**
     * Command to the service to display a message
     */
    static final int MSG_SHOW_TELETEXT_PAGE = 1;
    static final int MSG_UPDATE_CLOCK = 10000;
    static final int MSG_UPDATE_FLASH_DATA = 10001;
    static final int MSG_UPDATE_PAGE_NUMBER = 10002;
    static final int MSG_CANCEL_UPDATE_PAGE_NUMBER = 10003;
    static final int VALID_TELETEXT_TRACK = 1;
    public static TeletextService instance;
    public TeletextService() {
        Log.d(TAG, "TeletextService");
        mInstrumentationTesting = false;
        mServiceStatus = new ServiceStatus(this);
    }

    /**
     * for instrumentation testing
     */
    public void setContext(Context context) {
        initTeletext(context, true);
    }
    private void startTeletextDecoder(int pid) {
        Log.d(TAG, "startTeletextDecoder called");
        if (teletextManager != null) {
            teletextManager.stopTeletext();
        }
        // 建立 TeletextManager 並啟動（與 AvCmdMiddle 中用法一致）
        teletextManager = TeletextManager.getInstance(getApplicationContext());
        teletextManager.startTeletext(0, pid); // 用你的 demux_id 和 pid
    }
    private void stopTeletextDecoder() {
        
        if (teletextManager != null) {
            teletextManager.stopTeletext();
            teletextManager = null; // ⭐️ optional: 讓 GC 收走
        } else {
            Log.w(TAG, "⚠️ stopTeletextDecoder: teletextManager is null");
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
        isRunning=true;
        Log.d(TAG, "onCreate");
        // ✅ 加這段，確保 handler 不為 null
        if (mMainHandler == null) {
            mMainHandler = new TeletextHandler(this);
            mMessenger = new Messenger(mMainHandler);
        }
        initTeletext(null, false);


        // 加入 Home 鍵 receiver（Android 12+ 需要標示 EXPORTED/NOT_EXPORTED）
        mHomeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
                    String reason = intent.getStringExtra("reason");
                    if ("homekey".equals(reason)) {
                        Log.d(TAG, "🏠 HOME key pressed → exitTeletext");
                        exitTeletext(); // 🔥 關閉 Teletext
                    }
                }
            }
        };
        IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeReceiver, homeFilter, Context.RECEIVER_NOT_EXPORTED);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        mRootContainer = (KeyEventAwareFrameLayout) inflater.inflate(R.layout.overlay, null);
        mRootContainer.setKeyEventListener(this);
        mServiceStatus.readStatus();
        //IntentFilter filter = new IntentFilter("com.prime.TELE_TEXT_PAGE_UPDATED");
        //registerReceiver(pageUpdateReceiver, filter);
        
        
    }
    private void waitForInitialTeletextPage(int pageNum) {
        isWaitingForPage = true;  // ✅ 啟動時設 true

        new Thread(() -> {
            int retry = 0;
            int timeout = 100;
            while (retry < timeout && isWaitingForPage) { // 🔥 每次檢查是否還要等
                Cursor c = requirePageUpdate(pageNum);
                if (c != null && c.getCount() > 0) {
                    Log.d(TAG, "📄 Initial page " + pageNum + " found in DB, start rendering");
                    fetchPagesData(pageNum);
                    c.close();
                    break;
                } else {
                    retry++;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Log.w(TAG, "⚠️ Interrupted while waiting for page " + pageNum);
                        break;
                    }
                }
            }
            if (retry >= timeout && isWaitingForPage) {
                Log.w(TAG, "❌ Timeout: page " + pageNum + " not available after " + timeout / 10 + " seconds");
            }
            isWaitingForPage = false;  // ✅ 結束時設 false
        }).start();
    }
    private void startForegroundWithNotification() {
        NotificationChannel channel = new NotificationChannel(
            "teletext_channel_id",
            "Teletext 顯示服務",
            NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }

        Notification notification = new Notification.Builder(this, "teletext_channel_id")
            .setContentTitle("Teletext 顯示中")
            .setContentText("按紅鍵可關閉")
            .setSmallIcon(R.drawable.hint_red) // ⚠️ 請換成你有的 icon
            .build();

        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int pid = intent.getIntExtra("TeletextPid",8191 );
        int page = intent.getIntExtra("PageNumber", 100); // 預設 index 頁
        int subpage = intent.getIntExtra("SubPageNumber", 0); // 預設第 0 子頁
        if (pid==8191)
            return START_NOT_STICKY;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundWithNotification();
        }
        TeletextPid=pid;
        page_number_db = page;  // ⬅️ 指定目前 page
        curPageNum = page;
        mPrevPageNo = -1;       // 清除前一頁，確保重新載入
        pageUp = false;
        pageDown = false;

        // 🔁 直接抓 lo_home_teletext_overlay
        Activity topActivity = HomeApplication.get_current_activity(); // 自行實作
        if (topActivity != null && topActivity instanceof HomeActivity) {
            SurfaceView overlayView = topActivity.findViewById(R.id.lo_home_teletext_overlay);
            if (overlayView != null && overlayView.getHolder() != null) {
                overlayView.setVisibility(View.VISIBLE);
                overlayView.setZOrderOnTop(true);
                overlayView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

                overlayView.getHolder().addCallback(surfaceCallback);
                HomeApplication.cacheOverlayView(overlayView);

                mSurface = overlayView.getHolder().getSurface();
                if (mSurface != null && mSurface.isValid()) {
                    Log.d(TAG, "✔️ TeletextService got valid Surface");
                    if (!isDecoderStarted) {  // ⭐️避免重複啟動
                        mRenderingSurface=mSurface;
                        // TODO: 使用 mSurface 畫圖 or 播放 Teletext 畫面
                        //fetchPagesData(page_number_db); // ✅ 顯示頁面
                        //renderPagesData();
                        PageTracker.getInstance().setCurrentPageNumber(page_number_db);
                        startTeletextDecoder(TeletextPid);
                        waitForInitialTeletextPage(page_number_db); // ✅ 等 page 出現
                        isDecoderStarted=true;
                    }
                        
                } else {
                    Log.e(TAG, "❌ Surface invalid"+mSurface);
                }


            } else {
                Log.e(TAG, "\u274c SurfaceHolder not found");
            }
        } else {
            Log.e(TAG, "\u274c Top activity is null");
        }

        return START_NOT_STICKY;
    }
    private void initTeletext(Context context, boolean instrumentationTesting) {
        int fontId = R.font.bedstead;
        Typeface typeface = ResourcesCompat.getFont(getApplicationContext(), fontId);
        Typeface regular = Typeface.create(typeface, Typeface.NORMAL);

        mContext = context;
        mInstrumentationTesting = instrumentationTesting;
        mPage = new FttxPage(fontId);
        mPacket0 = new FttxPacket0();
        mPacket1_25 = new FttxPacket1_25();
        mPacket26 = new FttxPacket26();
        mPacket27 = new FttxPacket27();
        mPageSettings = new PageSettings();
        mPageSettings.setFontFace(regular);
        mPageSettings.setGXCharset(mPage.getCharset());
        mTeletext = new TeletextPainter(mInstrumentationTesting, 1.0F);
    }

    public void addView() {
        try {
            windowManager.addView(mRootContainer, params);
        } catch (WindowManager.BadTokenException e) {
            Log.e(TAG, "Screen overlay is not granted for Teletext service yet" + e);
            Toast.makeText(getApplicationContext(),
                    "Screen overlay is not granted for Teletext service yet",
                    Toast.LENGTH_SHORT).show();
        }
    }
    private final SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mSurface = holder.getSurface();
            if (mSurface != null && mSurface.isValid()) {
                Log.d(TAG, "✔️ TeletextService  surfaceCreated got valid overlay Surface");
                // ⭐️ 新增這個判斷：避免 service 已經 stop，還啟動
                if (!TeletextService.isRunning) {
                    Log.w(TAG, "⚠️ surfaceCreated but TeletextService already stopped, skip startTeletextDecoder");
                    return;
                }
                if (!isDecoderStarted) {  // ⭐️避免重複啟動
                    mRenderingSurface = mSurface;
                    PageTracker.getInstance().setCurrentPageNumber(page_number_db);
                    startTeletextDecoder(TeletextPid); // 或傳 pid
                    waitForInitialTeletextPage(page_number_db);
                    isDecoderStarted=true;
                }
            } else {
                Log.e(TAG, "❌ Surface invalid in callback");
            }
        }
    
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
    
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, "⚠️ Surface destroyed");
        }
    };
    private void clearTeletextPagesFromDatabase() {
        try {
            int deletedRows = getContentResolver().delete(PagesDatabase.Pages.CONTENT_URI, null, null);
            Log.d(TAG, "🗑️ Cleared Teletext Pages from DB: " + deletedRows + " rows deleted.");
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to clear Teletext Pages DB", e);
        }
    }

    private void cleanupTeletextResources() {
        Log.d(TAG, "cleanupTeletextResources");

        // 1. 移除 Handler 任務
        if (mMainHandler != null) {
            mMainHandler.finishing = true;
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacks(runnable);
            mHandler = null;
        }

        // 2. 清除 Surface 畫面
        if (mRenderingSurface != null) {
            mTeletext.clearPage(mPageSettings, mRenderingSurface);
            try {
                mRenderingSurface.release();
            } catch (Exception ignored) {}
            mRenderingSurface = null;
        }

        // 3. 安全移除 callback，即使不是 HomeActivity 也能處理
        SurfaceView overlayView = HomeApplication.getCachedOverlayView();
        if (overlayView == null) {
            Activity topActivity = HomeApplication.get_current_activity();
            if (topActivity != null && topActivity.getClass().getSimpleName().equals("HomeActivity")) {
                overlayView = topActivity.findViewById(R.id.lo_home_teletext_overlay);
            }
        }

        if (overlayView != null) {
            Log.w(TAG, "Surface removeCallback cleanupTeletextResources");
            overlayView.getHolder().removeCallback(surfaceCallback);
            overlayView.setVisibility(View.GONE);
            HomeApplication.cacheOverlayView(null); // ❗清掉 cached
        }

        // 4. 移除浮動 View
        if (mRootContainer != null && (mRootContainer.getParent() != null || mRootContainer.isShown())) {
            try {
                windowManager.removeView(mRootContainer);
            } catch (Exception e) {
                Log.w(TAG, "⚠️ mRootContainer already removed");
            }
        }

        // 5. 清掉 page 狀態
        if (mPageSettings != null) {
            mPageSettings.reset();
            mPageSettings.setFlashData(false);
            mPageSettings.setBlink(false);
        }

        // 6. 停止解碼器
        stopTeletextDecoder();
        isDecoderStarted = false;

        // 7. 清空 DB
        clearTeletextPagesFromDatabase();
        isWaitingForPage = false;

        // 8. 儲存狀態、清空 static 實例
        mServiceStatus.saveStatus(ServiceStatus.RENDERING_STATE_STOPPED);
        isRunning = false;
        instance = null;
    }

    private void exitTeletext() {
        Log.d(TAG, "🛑 exitTeletext called");
        cleanupTeletextResources();
        // 停止 Service
        stopSelf();
    }
    
    @Override
    public void onTeletextKeyPressed(KeyEvent event) {
        Log.d(TAG, "*****onTeletextKeyPressed****** " + event.getKeyCode());
        pageUp = false;
        pageDown = false;
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_TV_TELETEXT:
                exitTeletext();
                break;
            //case KeyEvent.KEYCODE_PAGE_UP:  //rcu no this key
            case KeyEvent.KEYCODE_CHANNEL_UP:
                pageUp = true;
                page_number_db = page_number_db + 1;
                fetchPagesData(page_number_db);
                break;
            //case KeyEvent.KEYCODE_PAGE_DOWN:
            case KeyEvent.KEYCODE_CHANNEL_DOWN:
                pageDown = true;
                page_number_db = page_number_db - 1;
                fetchPagesData(page_number_db);
                break;
            case KeyEvent.KEYCODE_PROG_RED:
                if (mNavigation != null && mNavigation.exists()) {
                    page_number_db = mPage.getPageLink(FttxNavigationBase.NAVIGATION_POS_RED);
                    fetchPagesData(page_number_db);
                } else if (mHasPacket27 && mHasPacket24) {
                    page_number_db = mPacket27.pageno[0];
                    fetchPagesData(page_number_db);
                }
                break;
            case KeyEvent.KEYCODE_PROG_GREEN:
                if (mNavigation != null && mNavigation.exists()) {
                    page_number_db = mPage.getPageLink(FttxNavigationBase.NAVIGATION_POS_GREEN);
                    fetchPagesData(page_number_db);
                } else if (mHasPacket27 && mHasPacket24) {
                    page_number_db = mPacket27.pageno[1];
                    fetchPagesData(page_number_db);
                }
                break;
            case KeyEvent.KEYCODE_PROG_YELLOW:
                if (mNavigation != null && mNavigation.exists()) {
                    page_number_db = mPage.getPageLink(FttxNavigationBase.NAVIGATION_POS_YELLOW);
                    fetchPagesData(page_number_db);
                } else if (mHasPacket27 && mHasPacket24) {
                    page_number_db = mPacket27.pageno[2];
                    fetchPagesData(page_number_db);
                }
                break;
            case KeyEvent.KEYCODE_PROG_BLUE:
                if (mNavigation != null && mNavigation.exists()) {
                    page_number_db = mPage.getPageLink(FttxNavigationBase.NAVIGATION_POS_CYAN);
                    fetchPagesData(page_number_db);
                } else if (mHasPacket27 && mHasPacket24) {
                    page_number_db = mPacket27.pageno[3];
                    fetchPagesData(page_number_db);
                }
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (freezeKeyPressed) {
                    freezeKeyPressed = false;
                    handlerTask();
                } else {
                    freezeKeyPressed = true;
                }
                break;
            case KeyEvent.KEYCODE_TV_ZOOM_MODE:
                mPageSettings.nextZoomMode();
                renderPagesData();
                break;
            default:
                if (event.getKeyCode() >= KeyEvent.KEYCODE_0 &&
                        event.getKeyCode() <= KeyEvent.KEYCODE_9) {
                    mMainHandler.postKeyNumberNavigation(event.getKeyCode());
                }
                break;
        }
    }

    public void handleChannelNumberNavigation(int keyCode) {
        keyCode = keyCode - 7;
        Log.d(TAG, "*****handleChannelNumberNavigation****** keyCode: " + keyCode);

        if (pageKey == -1) {
            if(keyCode == 0) {
                return;
            }
            // Remove any pending cancel messages
            mMainHandler.removeMessages(MSG_CANCEL_UPDATE_PAGE_NUMBER);
            // Navigation just started post a cancel message
            mMainHandler.postCancelKeyNumberNavigation(4000L);
            pageKey = keyCode;
            pageNumberString = pageKey + "--";
            nbKeysPressed = 1;
        } else if (nbKeysPressed < 3){
            pageKey = Integer.valueOf(pageKey + String.valueOf(keyCode));
            if(nbKeysPressed == 1) {
                pageNumberString = pageKey + "-";
                nbKeysPressed = 2;
            } else if (nbKeysPressed == 2) {
                pageNumberString = pageKey + "";
                nbKeysPressed = 3;
            }
        } else {
            return;
        }

        Log.d(TAG, "Setting page selection [" + pageNumberString + "]");
        if(mPacket0 != null) {
            mPacket0.updatePageNumberSelection(pageNumberString.toCharArray());
            if(mPage.isHeaderParsed()) {
                mPacket0.setPageNumberSelection(mPage);
            }
        }

        if (nbKeysPressed == 3) {
            mMainHandler.removeMessages(MSG_CANCEL_UPDATE_PAGE_NUMBER);
            Cursor c = requirePageUpdate(pageKey);
            if (c.getCount() > 0) { // Page exists
                page_number_db = pageKey;
                fetchPagesData(page_number_db);
                mMainHandler.postCancelKeyNumberNavigation(1000L);
            } else { // Page don't exist, cancel selection immediately
                mMainHandler.postCancelKeyNumberNavigation(0L);
            }
            pageKey = -1;
            nbKeysPressed = 0;
            pageNumberString = null;
            c.close();
        } else {
            renderPagesData();
        }
    }

    public void handleCancelUpdatePageNavigation() {
        pageKey = -1;
        nbKeysPressed = 0;
        pageNumberString = null;
        mPacket0.updatePageNumberSelection(null);
        mPacket0.setPageNumberSelection(mPage);
        renderPagesData();
    }

    /**
     * Handler of incoming messages from clients.
     */
    class TeletextHandler extends Handler {
        boolean finishing;
        int idxPgRetryCounter = 0;
        long idxPgRetryDelayMs = 100L;

        TeletextHandler(Context context) {
            finishing = false;
        }

        @Override
        public void handleMessage(Message msg) {
            if (DEBUG) {
                Log.d(TAG, "TeletextHandler_handleMessage: " + msg.what);
            }
            switch (msg.what) {
                case MSG_SHOW_TELETEXT_PAGE:
                    if (!(msg.obj instanceof String)) {
                        Log.d(TAG, "Last running state [" +
                                mServiceStatus.getRenderingStateString() + "] is rebind[" +
                                mServiceStatus.getRebindState() + "]");
                        if (!mServiceStatus.getRebindState() && (mServiceStatus.getRenderingState()
                                == ServiceStatus.RENDERING_STATE_STOPPED)) {
                            mServiceStatus.saveStatus(false);
                            return;
                        }
                        mServiceStatus.saveStatus(false);
                    }

                    if (msg.arg2 == VALID_TELETEXT_TRACK) {
                        mIsIndexPage = true;
                        mTransparency = msg.arg1;
                        mPageSettings.setTransparencyValue(mTransparency);
                        if (!fetchPagesData(page_number_db)) {
                            if ((idxPgRetryCounter * idxPgRetryDelayMs)
                                    == INDEX_PAGE_SHOW_MSG_TIMEOUT) {
                                // Show message
                                Toast.makeText(TeletextService.this,
                                        "Loading...Wait for few seconds",
                                        Toast.LENGTH_LONG).show();
                            } else if ((idxPgRetryCounter * idxPgRetryDelayMs) >
                                    INDEX_PAGE_QUERY_TIMEOUT) {
                                return;
                            }
                            postRetryIndexPage(msg, idxPgRetryDelayMs);
                            idxPgRetryCounter++;
                            return;
                        } else {
                            idxPgRetryCounter = 0;
                            postUpdateClock(100L);
                        }
                        mServiceStatus.saveStatus(ServiceStatus.RENDERING_STATE_RUNNING);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "There is no valid teletext track",
                                Toast.LENGTH_LONG).show();
                    }
                    break;
                case MSG_UPDATE_CLOCK:
                    boolean updated = handleUpdateClock();
                    if (!finishing) {
                        postUpdateClock(updated ? 500L : 100L);
                    }
                    break;
                case MSG_UPDATE_FLASH_DATA:
                    if (!finishing && mPageSettings.isFlashData()) {
                        postUpdateFlashData(500L);
                    }
                    break;
                case MSG_UPDATE_PAGE_NUMBER:
                    if (!finishing) {
                        handleChannelNumberNavigation(msg.arg1);
                    }
                    break;
                case MSG_CANCEL_UPDATE_PAGE_NUMBER:
                    if (!finishing) {
                        handleCancelUpdatePageNavigation();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

        private void postUpdateClock(long delayMIllis) {
            Message message = new Message();
            message.what = MSG_UPDATE_CLOCK;
            sendMessageDelayed(message, delayMIllis);
        }

        private void postUpdateFlashData(long delayMIllis) {
            removeMessages(MSG_UPDATE_FLASH_DATA);
            Message message = new Message();
            message.what = MSG_UPDATE_FLASH_DATA;
            sendMessageDelayed(message, delayMIllis);
        }

        private void postKeyNumberNavigation(int keyNumber) {
            Message message = new Message();
            message.what = MSG_UPDATE_PAGE_NUMBER;
            message.arg1 = keyNumber;
            mMainHandler.sendMessage(message);
        }

        private void postCancelKeyNumberNavigation(long delayMillis) {
            Message message = new Message();
            message.what = MSG_CANCEL_UPDATE_PAGE_NUMBER;
            sendMessageDelayed(message, delayMillis);
        }

        private void postRetryIndexPage(Message msg, long delayMillis) {
            Message retryMsg = new Message();
            retryMsg.what = msg.what;
            retryMsg.arg1 = msg.arg1;
            retryMsg.arg2 = msg.arg2;
            retryMsg.obj = "retry";
            sendMessageDelayed(retryMsg, delayMillis);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "*****onBind******");
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        if (mMainHandler != null) {
            mMainHandler.removeMessages(MSG_UPDATE_CLOCK);
        }
        mMainHandler = new TeletextHandler(this);
        mMessenger = new Messenger(mMainHandler);

        Bundle extras = intent.getExtras();
        if (extras != null) {
            Object surface = extras.get("Surface");
            if (surface instanceof Surface) {
                if (mRenderingSurface != null) {
                    mRenderingSurface.release();
                }
                mRenderingSurface = (Surface) surface;
                if (!mRenderingSurface.isValid()) {
                    Log.e(TAG, "Passed surface is not valid");
                }
            }
        }
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "*****onUnbind******");
        mServiceStatus.saveStatus(true);
        return super.onUnbind(intent);
    }

    public int switchedToNextAvailablePage(int pageNum) {
        Log.d(TAG, "*****switchedToNextAvailablePage******");
        Cursor cursorData = null;
        if ((pageUp || pageDown) && (pageNum < TELETEXT_INDEX_PAGE
                || pageNum > TELETEXT_MAX_PAGE)) {
            if (pageNum > TELETEXT_MAX_PAGE && pageUp) {
                pageNum = TELETEXT_INDEX_PAGE;
            } else {
                if (pageNum < TELETEXT_INDEX_PAGE && pageDown) {
                    pageNum = TELETEXT_MAX_PAGE;
                }
            }
        }
        if (pageUp) {
            cursorData = doAscending(pageNum);
            if (!(cursorData.getCount() > 0)) {
                page_number_db = TELETEXT_INDEX_PAGE;
                pageNum = page_number_db;
            }
        } else if (pageDown) {
            cursorData = doDescending(pageNum);
        }
        if (cursorData.getCount() > 0) {
            cursorData.moveToNext();
            // To get the page number info
            String strPageNo = cursorData.getString(1);
            page_number_db = Integer.parseInt(strPageNo);
            pageNum = page_number_db;
        }
        return pageNum;
    }

    public Cursor doAscending(int pageNum) {
        ContentResolver cr = getContentResolver();
        String[] projection =
                {PagesDatabase.Pages.COLUMN_PAGE_SUBPAGE, PagesDatabase.Pages.COLUMN_PAGE,
                        PagesDatabase.Pages.COLUMN_DATA,};
        String dbselection = PagesDatabase.Pages.COLUMN_PAGE + ">=?";
        String pageNumber = Integer.toString(pageNum);
        Cursor cursor = cr.query(PagesDatabase.Pages.CONTENT_URI,
                projection,
                dbselection,
                new String[]{pageNumber}, (PagesDatabase.Pages.COLUMN_PAGE + " asc"));
        return cursor;
    }

    public Cursor doDescending(int pageNum) {
        ContentResolver cr = getContentResolver();
        String[] projection =
                {PagesDatabase.Pages.COLUMN_PAGE_SUBPAGE, PagesDatabase.Pages.COLUMN_PAGE,
                        PagesDatabase.Pages.COLUMN_DATA,};
        String dbselection = PagesDatabase.Pages.COLUMN_PAGE + "<=?";
        String pageNumber = Integer.toString(pageNum);
        Cursor cursor = cr.query(PagesDatabase.Pages.CONTENT_URI,
                projection,
                dbselection,
                new String[]{pageNumber}, (PagesDatabase.Pages.COLUMN_PAGE + " desc"));
        return cursor;
    }

    /**
     * Returns false only if index page does not exist
     */
    public boolean fetchPagesData(int pageNum) {
        Log.d(TAG, "FetchPagesData entered for [" + pageNum + "]");
        Cursor cursor = getPage(Integer.toString(pageNum));
        if (cursor == null) {
            Log.e(TAG, "❌ fetchPagesData: getPage returned null for page " + pageNum);
            return false;
        }
        if (!(cursor.getCount() > 0)) {
            if (mIsIndexPage && pageNum == TELETEXT_INDEX_PAGE) {
                return false;
            } else {
                if (!pageUp && !pageDown) {
                    Log.w(TAG, "Direct Key/color key Pressed");
                    page_number_db = mPrevPageNo;
                    return true;
                }
                pageNum = switchedToNextAvailablePage(pageNum);

                if (mPrevPageNo == pageNum) {
                    return true;
                }
                cursor = requirePageUpdate(pageNum);
            }
        }

        if (cursor.getCount() > 0) {
            if (mPrevPageNo != pageNum) {
                mPage.clear();
                mPageSettings.setFlashData(false);
                mPageSettings.setBlink(false);
                mPacket26.clear();
                if (mIsIndexPage) {
                    // Add view for the overlay
                    addView();
                    mIsIndexPage = false;
                }
            }
            dbCursor = cursor;
        }

        if (mPrevPageNo != pageNum && dbCursor.getCount() > 0) {
            mPrevPageNo = pageNum;
            handlerTask();
        }
        return true;
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            runTask();
            mHandler.postDelayed(runnable, NAVIGATION_TIME_IN_MSEC);
        }
    };

    public void handlerTask() {
        if (mHandler == null) {
            mHandler = new Handler(getMainLooper());
        } else  {
            mHandler.removeCallbacks(runnable);
        }
        mHandler.post(runnable);
    }

    public void runTask() {
        if (dbCursor != null && dbCursor.getCount() > 0 && !freezeKeyPressed) {
            Log.e(TAG, "runTask ...1 " );
            dbCursor.moveToNext();
            if (dbCursor.isAfterLast()) {
                dbCursor = requirePageUpdate(curPageNum);
                dbCursor.moveToNext();
            }
            if (dbCursor.getCount() > 0) {
                // This is to clear previous page data
                if (dbCursor.getCount() > 1) {
                    mPage.clear();
                    mPageSettings.setFlashData(false);
                    mPageSettings.setBlink(false);
                }
                mPacket26.clear();
                String strSubPageNo = dbCursor.getString(0);
                String strPageNo = dbCursor.getString(1);
                curPageNum = Integer.parseInt(strPageNo);
                getCursorData(strSubPageNo);
                parsePagesData(packetItems);
                // packet_26 handlings
                if (mPage.isHeaderParsed()) {
                    mPacket26.apply(mPage);
                }
                if (mPageSettings.isFlashData()) {
                    mPageSettings.setFlashData(false);
                }
                renderPagesData();
                runTaskForFlash();
            }
        }
    }

    public Cursor requirePageUpdate(int pageNum) {
        ContentResolver cr = getContentResolver();
        String[] projection =
                {PagesDatabase.Pages.COLUMN_PAGE_SUBPAGE, PagesDatabase.Pages.COLUMN_PAGE,
                        PagesDatabase.Pages.COLUMN_DATA,};
        String selection = PagesDatabase.Pages.COLUMN_PAGE + "=?";
        String pageNumber = Integer.toString(pageNum);
        Cursor cursor = cr.query(PagesDatabase.Pages.CONTENT_URI,
                projection,
                selection,
                new String[]{pageNumber}, null);
        return cursor;
    }

    private Cursor getPage(String page) {
        ContentResolver cr = mContext == null ? getContentResolver() : mContext.getContentResolver();
        String[] projection = {
            PagesDatabase.Pages.COLUMN_PAGE_SUBPAGE,
            PagesDatabase.Pages.COLUMN_PAGE,
            PagesDatabase.Pages.COLUMN_DATA,
        };

        String selection = PagesDatabase.Pages.COLUMN_PAGE + "=?";
        long t1 = System.currentTimeMillis();
        Cursor cursor = cr.query(PagesDatabase.Pages.CONTENT_URI,
            projection, selection, new String[]{page}, null);
        Log.d(TAG, "️ getPage query() time spend: " + (System.currentTimeMillis() - t1) + " ms");
        // 防止返回 null（Android 官方文檔說明某些情況可能會 null）
        //Log.d(TAG, "ContentProvider authority: " + PagesDatabase.Pages.CONTENT_URI.getAuthority());
        //Log.d(TAG, "Resolver Class: " + cr.getClass().getName());
        if (cursor == null) {
            Log.e(TAG, "❌ getPage() cursor is null for page: " + page);
            return null;
        }
    
        return cursor;
    }

    public void getCursorData(String strSubPageNo) {
        ContentResolver cr = null;
        if (mContext == null) {
            cr = getContentResolver();
        } else {
            cr = mContext.getContentResolver(); // for instrumentation testing
        }
        String[] projection =
                {PagesDatabase.Pages.COLUMN_PAGE_SUBPAGE, PagesDatabase.Pages.COLUMN_PAGE,
                        PagesDatabase.Pages.COLUMN_DATA,};
        String selection = PagesDatabase.Pages.COLUMN_PAGE_SUBPAGE + "=?";
        /*To get the current data everytime from db, instead of from the dbCursor*/
        Cursor cursor = cr.query(PagesDatabase.Pages.CONTENT_URI,
                projection,
                selection,
                new String[]{strSubPageNo}, null);
        /*To get the data*/
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            String strPacketData = cursor.getString(2);
            packetItems = convertData(strPacketData);
        }
    }

    public void parsePagesData(String[] items) {
        Log.d(TAG, "*****parsePagesData****** Start");
        int packetNumber_0 = Integer.parseInt(items[0]);
        if (packetNumber_0 == 0) {
            // Need integer of hex string representation
            int pageNumber_0 = Integer.valueOf(Integer.toHexString(Integer.parseInt(items[6])));
            int subPageNumber_0 = Integer.parseInt(items[7]);
            //rem byte starts from (index7,byte8),(index8,byte9),(index9, byte10) .... 38times
            int byte9 = Integer.parseInt(items[8]);
            int byte11 = Integer.parseInt(items[10]);
            int byte12 = Integer.parseInt(items[11]);
            int byte13 = Integer.parseInt(items[12]);

            int returnVal = mPacket0.parse(PAGE_HEADER_DATA_BYTE, items, mPage, pageNumber_0,
                    subPageNumber_0,
                    byte9, byte11,
                    byte12, byte13);
            if (returnVal == 1) {
                NAVIGATION_TIME_IN_MSEC = 500;
                subtitlePage = true;
            } else {
                NAVIGATION_TIME_IN_MSEC = 5000;
                subtitlePage = false;
            }
        }

        //Packet 1 to 25
        for (int i = packet1_25Index - 1; i < items.length; i += packet1_25Index) {
            int packetNumber1_25 = Integer.parseInt(items[i]);
            if (packetNumber1_25 > 0 && packetNumber1_25 <= 25) {
                if (packetNumber1_25 == 24) {
                    if (mPacket27.Controlbit == 0) {
                        mHasPacket24 = false;
                        return;
                    } else {
                        mHasPacket24 = true;
                    }
                }
                int dataBytePos = i + PACKET1_25_DATABYTEPOS;
                mPacket1_25.parse(dataBytePos, items, mPage, packetNumber1_25, subtitlePage);
            } else if (26 == packetNumber1_25) {
                int dataBytePos = i + PACKET26_DATABYTEPOS;
                mPacket26.parse(dataBytePos, items, mPage);
            } else if (27 == packetNumber1_25) {
                int dataBytePos = i + PACKET27_DATABYTEPOS; //referring from designation code Byte6
                mHasPacket27 = true;
                mPacket27.parse(dataBytePos, items);
            } else {
                Log.d(TAG, "Invalid page number");
            }
        }
        if (!subtitlePage) {
            createNavigationLine();
        }
    }

    public void renderPagesData() {
        if (!freezeKeyPressed) {
            Log.d(TAG, "*****renderPagesData******");
            mPageSettings.setSubtitle(subtitlePage);


            if (mRenderingSurface != null) {
                mTeletext.paintPage(mPage, mPageSettings, mRenderingSurface);
            }
            else{
                Log.d(TAG, "renderPagesData(): surface null  ");
            }
            if (mPageSettings.isFlashData()) {
                mPageSettings.setBlink(!mPageSettings.isBlink());
            }
        }
        setBitmap(mTeletext.getLastBitmap()); // for instrumentation testing
    }

    public void runTaskForFlash() {
        if (mPageSettings.isFlashData()) {
            mMainHandler.postUpdateFlashData(500L);
        }
    }

    /**
     * for instrumentation testing
     */
    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    /**
     * for instrumentation testing
     */
    public Bitmap getBitmap() {
        return mBitmap;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        // ✨ 確保解除 home receiver
        if (mHomeReceiver != null) {
            try {
                unregisterReceiver(mHomeReceiver);
            } catch (Exception e) {
                Log.w(TAG, "⚠️ Failed to unregister mHomeReceiver", e);
            }
            mHomeReceiver = null;
        }
        cleanupTeletextResources();

    }

    private Bitmap createTransparentBitmap() {
        int width = windowManager.getCurrentWindowMetrics().getBounds().width();
        int height = windowManager.getCurrentWindowMetrics().getBounds().height();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.TRANSPARENT);
        return bitmap;
    }

    private boolean handleUpdateClock() {
        boolean updated = false;
        Cursor c = requirePageUpdate(0);
        if (c.getCount() > 0) {
            c.moveToNext();
            String strPacketData = c.getString(2);
            String[] dataItems = convertData(strPacketData);

            if (mPacket0 != null) {
                String[] clockItems = new String[8];
                for (int i = 0; i < 8; ++i) {
                    clockItems[i] = dataItems[PKT0_DATABYTEPOS + i];
                }

                if (mPacket0.updateClock() && !mPacket0.isClockDataUpToDate(clockItems)) {
                    mPacket0.updateClockData(clockItems);
                    if (mPage.isHeaderParsed()) {
                        mPacket0.setClockDataToPage(mPage);
                        renderPagesData();
                        updated = true;
                    }
                }
            }
        }
        c.close();
        return updated;
    }

    @SuppressLint("Range")
    private void createNavigationLine() {
        if (mNavigation == null) {
            if (TOP_NAVIGATION_ENABLED) {
                mNavigation = new FttxNavigationTop(new PageProvider());
            }
        }

        if (mNavigation != null) {
            mNavigation.build(mPage);
            mPageSettings.setNavigationExists(mNavigation.exists());
        } else {
            mPageSettings.setNavigationExists(false);
        }
    }

    class PageProvider implements FttxNavigationBase.PageReader {
        @SuppressLint("Range")
        @Override
        public String[] readPage(int page) {
            String[] data = null;
            Cursor c = getPage(Integer.toHexString(page));
            if (c.getCount() > 0) {
                c.moveToFirst();
                data = convertData(c.getString(c.getColumnIndex(PagesDatabase.Pages.COLUMN_DATA)));
            }
            c.close();
            return data;
        }
    }

    private String[] convertData(String data) {
        return data.replaceAll("\\[", "").replaceAll("\\]", "").
                replaceAll("\\s", "").split(",");
    }
    public void handleKeyFromLauncher(int keyCode, KeyEvent event) {
        //Log.d(TAG, "handleKeyFromLauncher: " + keyCode);
        onTeletextKeyPressed(event); // 呼叫你原本的處理方法
    }
}
