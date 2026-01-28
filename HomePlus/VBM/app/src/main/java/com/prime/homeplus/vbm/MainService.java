package com.prime.homeplus.vbm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import androidx.annotation.Nullable;

import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceInterface;
import com.prime.homeplus.vbm.util.PostData;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MainService extends Service {
    private static final String TAG = "HOMEPLUS_VBM";

    // --- Singleton 支援 (供 Application 轉發使用) ---
    private static MainService sInstance;
    public static boolean isRunning = false;
    public static synchronized MainService getInstance() {
        return sInstance;
    }

    // --- 成員變數 ---
    private List<VBMData> mVBMdataList;
    private PostData postData;
    private String mStbId;

    // [新增] 3.11 Drop Counter 相關定義
    private int mDropCount = 0; 
    private static final int MAX_QUEUE_SIZE = 256; // 規範要求超過 256 筆即開始丟棄
    private static final String AGENT_DROP_COUNTER = "99";

    // --- Spec v1.29: Agent 9 behaviors ---
    private static final String AGENT_STB_INFO = "9";
    private static final String EVT_SIGNAL_LEVEL = "0";   // 3.10.1
    private static final String EVT_PRODUCT_STATUS = "1"; // 3.10.2
    private static final long SIGNAL_LEVEL_INTERVAL_MS = 12L * 60L * 60L * 1000L; // 12 hours
    private static final String PREFS_NAME = "vbm_prefs";
    private static final String KEY_LAST_SIGNAL_LEVEL_MS = "agent9_last_signal_ms";

    // 背景執行緒與 Handler (核心改變)
    private HandlerThread mWorkerThread;
    private Handler mHandler;
    private final Object mLock = new Object(); // 資料鎖
    // Retry/Inflight（對齊 Benchmark 的 needResend + 5min retry）
    private static final long RETRY_DELAY_MS = 300000L; // 5 minutes
    private boolean mUploading = false;
    private boolean mNeedResend = false;

    private final List<VBMData> mInFlightList = new ArrayList<>();
    private int mInFlightDropCount = 0;
    // 獨立的 AIDL 實作物件
    private VbmBinder mVbmBinder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind called");
        return mVbmBinder; // 回傳 Binder 允許外部連線
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        Log.d(TAG, "onCreate");
        sInstance = this;

        // 啟動前景服務保活
        startMyOwnForeground();

        // 初始化背景執行緒
        mWorkerThread = new HandlerThread("VbmWorker");
        mWorkerThread.start();
        mVBMdataList = new ArrayList<>();

        mStbId = Utils.getStbId();
        if (mStbId == null) mStbId = "";

        // 初始化 Handler (保留 999 邏輯)
        // 注意：這個 Handler 綁定的是 mWorkerThread 的 Looper，所以 handleMessage 會在背景執行
        mHandler = new Handler(mWorkerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                // --- 保留原有的 Callback 清除邏輯 ---
                if (msg.what == 999) {
                    synchronized (mLock) {
                        // 這裡建議不要再依賴 server message 的 "count:"
                        // 先以 inflight 全部成功為主（最保守且符合目前搬移策略）
                        mInFlightList.clear();
                        mInFlightDropCount = 0;

                        mNeedResend = false;
                        mUploading = false;

                        // 取消 retry 排程
                        mHandler.removeCallbacks(mRetryRunnable);

                        // 若 queue 已累積到門檻，立刻再送下一批
                        if (mVBMdataList.size() >= 128) {
                            performUploadLocked();
                        }
                    }
                    return;
                }
                if (msg.what == -1) {
                    synchronized (mLock) {
                        mNeedResend = true;
                        mUploading = true;

                        // 避免重複排程
                        mHandler.removeCallbacks(mRetryRunnable);
                        mHandler.postDelayed(mRetryRunnable, RETRY_DELAY_MS);
                    }
                    return;
                }
            }
        };
        // 初始化 PostData
        // 關鍵點：將 mHandler (綁定背景執行緒) 傳給 PostData
        // 這樣 PostData 回傳的 999 訊息會回到我們的 Worker Thread 處理，不會卡 UI
        postData = new PostData(this, mHandler);

        // 初始化 AIDL Binder
        mVbmBinder = new VbmBinder(this);

        // 啟動定時上傳任務
        scheduleNextUpload();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        Log.d(TAG, "onDestroy");
        if (mWorkerThread != null) mWorkerThread.quitSafely();
        sInstance = null;
    }
    private final Runnable mRetryRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (mLock) {
                if (!mNeedResend) return;
                if (mInFlightList.isEmpty()) {
                    // 沒有 inflight 代表已無需重送
                    mNeedResend = false;
                    mUploading = false;
                    return;
                }

                String soId = Utils.getSoId(true);
                if (soId == null || soId.isEmpty()) {
                    mInFlightList.clear();
                    mInFlightDropCount = 0;
                    mNeedResend = false;
                    mUploading = false;
                    return;
                }

                Log.w(TAG, "Retry upload (5min). inflight=" + mInFlightList.size() + ", drop=" + mInFlightDropCount);
                postData.send(mStbId, soId, mInFlightList, mInFlightList.size(), mInFlightDropCount);
            }
        }
    };
    // ===============================================================
    //  核心功能：加入資料 (供 VbmBinder 與 Application 呼叫)
    // ===============================================================

    public void addRecord(String agentId, String eventType, String timestamp, String... values) {
        if (mHandler != null) {
            if (AGENT_DROP_COUNTER.equals(agentId)) {
                // Drop counter 只允許由 PostData append，避免污染 queue
                return;
            }
            mHandler.post(() -> {
                synchronized (mLock) {

                    // drop oldest
                    if (mVBMdataList.size() >= MAX_QUEUE_SIZE) {
                        mVBMdataList.remove(0);
                        mDropCount++;
                        Log.w(TAG, "VBM Queue full, drop oldest. dropCount=" + mDropCount);
                    }

                    VBMData data = new VBMData(mStbId, agentId, eventType, timestamp, values);
                    mVBMdataList.add(data);
                    Log.d(TAG, "Record added. Agent:" + agentId + " Size:" + mVBMdataList.size());

                    // 128 觸發（但 inflight/needResend 期間不觸發新上傳）
                    if (!mUploading && !mNeedResend && mVBMdataList.size() >= 128) {
                        performUploadLocked();
                    }
                    // 加入這行 Log 驗證終點
                    //if ("99".equals(agentId)) {
                    //    Log.i(TAG, ">>> [TEST SUCCESS] Data arrived at HomePlus VBM! Queue Size: " + mVBMdataList.size());
                    //    Log.i(TAG, ">>> Content: " + (values.length > 0 ? values[0] : ""));
                    //}
                }
            });
        }
    }

    // ===============================================================
    //  核心功能：上傳邏輯 (配合 999 機制)
    // ===============================================================

    private void performUploadLocked() {
        if (mVBMdataList == null || mVBMdataList.isEmpty()) return;
        if (mUploading || mNeedResend) return;

        String soId = Utils.getSoId(true);
        if (soId == null || soId.isEmpty()) {
            // Benchmark 行為：SO 取不到就直接清 queue，避免無限累積
            mVBMdataList.clear();
            return;
        }

        // 把本次要送的 batch 移到 inflight（讓 queue 可繼續收新資料）
        mInFlightList.clear();
        mInFlightList.addAll(mVBMdataList);
        mVBMdataList.clear();

        // 依 v1.29 規格：
        // - 3.10.1 Signal level：在開始上傳時附帶回報；並限制每 12 小時一次以降低 GetHfcMac load
        // - 3.10.2 Product status：在上傳任何資料時附帶回報
        appendAgent9OnUploadLocked(mInFlightList);

        mInFlightDropCount = mDropCount;
        mDropCount = 0;

        mUploading = true;

        Log.d(TAG, "Trigger upload. inflight=" + mInFlightList.size() + ", drop=" + mInFlightDropCount);
        postData.send(mStbId, soId, mInFlightList, mInFlightList.size(), mInFlightDropCount);
    }

    private void appendAgent9OnUploadLocked(List<VBMData> inflight) {
        // 防呆：inflight 為本次要上傳的資料集合
        if (inflight == null) return;

        final long now = System.currentTimeMillis();
        final String smartCard = Utils.getSmartCardNumber(this);

        // 3.10.1：12 小時一次
        long last = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getLong(KEY_LAST_SIGNAL_LEVEL_MS, 0L);
        if (last <= 0L || (now - last) >= SIGNAL_LEVEL_INTERVAL_MS) {
            VBMData s = buildAgent9SignalLevel(smartCard, now);
            if (s != null) {
                inflight.add(s);
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        .edit()
                        .putLong(KEY_LAST_SIGNAL_LEVEL_MS, now)
                        .apply();
            }
        }

        // 3.10.2：上傳任何資料時回報（無法取得到期日則填 N/A）
        inflight.add(new VBMData(mStbId, AGENT_STB_INFO, EVT_PRODUCT_STATUS,
                String.valueOf(now), smartCard, "20", "N/A"));
        inflight.add(new VBMData(mStbId, AGENT_STB_INFO, EVT_PRODUCT_STATUS,
                String.valueOf(now), smartCard, "21", "N/A"));
    }

    @Nullable
    private VBMData buildAgent9SignalLevel(String smartCard, long now) {
        try {
            PrimeDtvServiceInterface svc = PrimeHomeplusVBMApplication.get_prime_dtv_service();

            String tuner1 = buildTunerStatus(svc, 0);
            String tuner2 = buildTunerStatus(svc, 1);
            String tuner3 = buildTunerStatus(svc, 2);
            String tuner4 = buildTunerStatus(svc, 3);

            String stbMac = "N/A";
            String wifiMac = "N/A";
            if (svc != null) {
                String[] macs = svc.get_mac();
                if (macs != null && macs.length > 0 && macs[0] != null && !macs[0].trim().isEmpty()) {
                    stbMac = macs[0].trim();
                }
                if (macs != null && macs.length > 1 && macs[1] != null && !macs[1].trim().isEmpty()) {
                    wifiMac = macs[1].trim();
                }
            }

            String fw = (Build.ID == null ? "" : Build.ID) + " " + Build.VERSION.INCREMENTAL;
            fw = fw.trim().isEmpty() ? Build.DISPLAY : fw.trim();

            String hfcMac = "N/A";

            // Value_0 ~ Value_9
            String[] values = new String[] {
                    safeNA(smartCard),   // Value_0
                    tuner1,              // Value_1
                    tuner2,              // Value_2
                    tuner3,              // Value_3
                    tuner4,              // Value_4
                    safeNA(stbMac),      // Value_5
                    safeNA(hfcMac),      // Value_6
                    safeNA(fw),          // Value_7
                    safeNA(wifiMac),     // Value_8
                    "N/A"                // Value_9 (Reserved/補齊用)
            };

            return new VBMData(
                    mStbId,
                    AGENT_STB_INFO,
                    EVT_SIGNAL_LEVEL,
                    String.valueOf(now),
                    values
            );
        } catch (Throwable t) {
            Log.w(TAG, "buildAgent9SignalLevel failed", t);
            return null;
        }
    }

    private String buildTunerStatus(@Nullable PrimeDtvServiceInterface svc, int tunerId) {
        // Spec format: {3-digit MHz}/{BER}/{SNR}db/{LEVEL}dBuV
        // 若無法取得 frequency，使用 0 做為 placeholder
        int freqMHz = 0;
        String berSci = "0.0E-0";
        int snr = 0;
        int level = 0;

        try {
            if (svc != null && svc.get_tuner_status(tunerId)) {
                int berRaw = svc.get_signal_ber(tunerId);
                // Align to benchmark style: BER scaled to scientific notation.
                double ber = berRaw / 1.0E7d;
                berSci = formatScientific(ber);
                snr = svc.get_signal_snr(tunerId);
                level = svc.get_signal_strength(tunerId);
            }
        } catch (Throwable t) {
            // ignore
        }

        return String.format(Locale.US, "%03d/%s/%ddb/%ddBuV", freqMHz, berSci, snr, level);
    }

    private static String formatScientific(double value) {
        DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.US);
        DecimalFormat df = new DecimalFormat("0.0E0", sym);
        // Benchmark uses upper-case E
        return df.format(value);
    }

    private static String safeNA(String s) {
        if (s == null) return "N/A";
        String t = s.trim();
        return t.isEmpty() ? "N/A" : t;
    }


    private void scheduleNextUpload() {
        // v1.29 規格：
        // - 第一次：random(0~600000ms) + 1 hour
        // - 後續：固定每 1 hour
        long delayMs;
        if (mFirstSchedule) {
            int randomDelayMs = new Random().nextInt(600000); // 0~600000 ms
            delayMs = 3600_000L + randomDelayMs;
            mFirstSchedule = false;
        } else {
            delayMs = 3600_000L;
        }

        if (mHandler != null) {
            mHandler.postDelayed(() -> {
                synchronized (mLock) {
                    if (!mUploading && !mNeedResend) {
                        performUploadLocked();
                    }
                }
                scheduleNextUpload();
            }, delayMs);
        }
    }

    private boolean mFirstSchedule = true;

    // ===============================================================
    //  其他保活與廣播邏輯
    // ===============================================================

    private void startMyOwnForeground() {
        if (Build.VERSION.SDK_INT >= 26) {
            String NOTIFICATION_CHANNEL_ID = "com.prime.homeplus.vbm.nid1";
            String channelName = "HOMEPLUS_VBM_SERVICE";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) manager.createNotificationChannel(chan);

            Notification notification = new Notification.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID)
                    .setContentText(channelName)
                    .setSmallIcon(R.drawable.notification_icon_background)
                    .setWhen(System.currentTimeMillis())
                    .build();
            startForeground(2, notification);
        } else {
            startForeground(1, new Notification());
        }
    }

}