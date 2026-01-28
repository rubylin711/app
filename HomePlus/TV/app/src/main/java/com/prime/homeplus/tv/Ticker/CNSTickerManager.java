package com.prime.homeplus.tv.Ticker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.tvprovider.media.tv.TvContractCompat;

import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.ticker.CNS.CNSTickerData;
import com.prime.dtv.service.dsmcc.DsmccService;
import com.prime.homeplus.tv.utils.PrimeUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CNSTickerManager {
    public static final String TAG = "CNSTickerManager";

    private static final long HEARTBEAT_INTERVAL_MS = 1000L; // 心跳間

    public static final String root_path = "/data/vendor/dtvdata/TICKER/sessions/";
    public static final String ACTION_REMOVE_TICKER_SESSION = "com.prime.dtv.ACTION_REMOVE_TICKER_SESSION";
    public static final String ACTION_KEEP_ONLY_TICKER_SESSION = "com.prime.dtv.ACTION_KEEP_ONLY_TICKER_SESSION";
    private static final String PERMISSION_TICKER = "com.prime.permission.TICKER";
    private String mTransactionId = "";
    private String mRawTickerData = "";
    private WeakReference<Context> gRef = null;
    private static CNSTickerManager mCnsTickerManager = null;
    private List<CNSTickerData> mCNSTickerData = new ArrayList<>();
    private TickerView mTickerView;
    private Map<TickerView.TickerPosition, TickerView> mTickerViews = new HashMap<>(); // 管理多個 TickerView
    private Boolean mIsRunning = false;
    private Handler mMainHandler = null;
    private Handler mBackgroundHandler = null;
    private HandlerThread mHandlerThread = null;
    private String mCurrentChannelDisplayNumber = "";
    private CNSTickerData mActiveTickerData = null;
    private boolean mForceRestart = false;
    private int mTestModeStep = 0; // 0: Off, 1: TOP R2L, 2: TOP L2R, 3: LEFT, 4: RIGHT

    public void setCurrentChannelDisplayNumber(String displayNumber) {
        this.mCurrentChannelDisplayNumber = displayNumber;

        if (mTestModeStep > 0)
            return; // 測試模式下不處理轉台邏輯

        // 每次轉台都強制停止當前 Ticker，以便重新開始播放
        if (mActiveTickerData != null) {
            if (mActiveTickerData.matchesChannel(displayNumber)) {
                mActiveTickerData.setLastPlayTimeMillis(0);
            }
            stopActiveTicker();
            mForceRestart = true;
        }

        if (mIsRunning) {
            mBackgroundHandler.removeCallbacks(mTickerHeartbeat);
            mBackgroundHandler.post(mTickerHeartbeat);
        }
    }

    public void toggleTestMode() {
        mTestModeStep = (mTestModeStep + 1) % 5;
        Log.d(TAG, "toggleTestMode: step = " + mTestModeStep);

        stopActiveTicker();
        mForceRestart = true;

        if (mTestModeStep == 0) {
            Log.d(TAG, "Test mode OFF, resuming normal operation.");
            mBackgroundHandler.post(mTickerHeartbeat);
            return;
        }

        // 立即觸發測試播放
        mBackgroundHandler.removeCallbacks(mTickerHeartbeat);
        mBackgroundHandler.post(mTickerHeartbeat);
    }

    private void stopActiveTicker() {
        mActiveTickerData = null;
        for (TickerView view : mTickerViews.values()) {
            if (view != null) {
                mMainHandler.post(view::stopScrolling);
            }
        }
    }

    public CNSTickerManager(Context context) {
        gRef = new WeakReference<>(context);
        mMainHandler = new Handler(context.getMainLooper());
        mHandlerThread = new HandlerThread("CNSTickerManager");
        mHandlerThread.start();
        mBackgroundHandler = new Handler(mHandlerThread.getLooper());
    }

    public static CNSTickerManager getInstance() {
        return mCnsTickerManager;
    }

    public static CNSTickerManager getInstance(Context context) {
        if (mCnsTickerManager == null) {
            synchronized (CNSTickerManager.class) {
                if (mCnsTickerManager == null) {
                    mCnsTickerManager = new CNSTickerManager(context);
                }
            }
        }
        return mCnsTickerManager;
    }

    public boolean IsRunning() {
        return mIsRunning;
    }

    public void setTickerView(TickerView.TickerPosition position, TickerView tickerView) {
        if (tickerView != null) {
            mTickerViews.put(position, tickerView);
            Log.d(TAG, "TickerView registered for position: " + position);
        }
    }

    public void resetTransactionId() {
        // Log.d("TAG, "old ticker TransactionId = " + mTransactionId);
        String newTxId = Pvcfg.get_cns_ticker_transaction_id();
        if (!mTransactionId.equals(newTxId)) {
            mTransactionId = newTxId;
        }
        // Log.d(TAG, "new ticker TransactionId = " + mTransactionId);
        if (!mTransactionId.isEmpty()) {
            Intent intent = new Intent(ACTION_KEEP_ONLY_TICKER_SESSION);
            intent.putExtra("transaction_id", mTransactionId);
            if (gRef.get() != null) {
                Log.d(TAG, "send ACTION_KEEP_ONLY_TICKER_SESSION ticker TransactionId = " + mTransactionId);
                gRef.get().sendBroadcast(intent, PERMISSION_TICKER);
            }
        }
    }

    public void start() {
        if (mIsRunning) {
            Log.w(TAG, "Manager is already running.");
            return;
        }
        Log.i(TAG, "CNSTickerManager starting...");
        mTransactionId = Pvcfg.get_cns_ticker_transaction_id();
        if (loadTickerData()) {
            // 在後台線程開始心跳循環
            mIsRunning = true;
            mBackgroundHandler.post(mTickerHeartbeat);
        }
    }

    public void stop() {
        if (!mIsRunning) {
            return;
        }
        Log.i(TAG, "CNSTickerManager stopping...");
        mIsRunning = false;
        // 清除後台和主線程的所有待處理消息
        mBackgroundHandler.removeCallbacksAndMessages(null);
        mMainHandler.removeCallbacksAndMessages(null);

        // 確保所有 TickerView 都停止並隱藏
        for (TickerView view : mTickerViews.values()) {
            if (view != null) {
                // 在主線程停止 View
                mMainHandler.post(view::stopScrolling);
            }
        }
    }

    private boolean check_ticker_exist() {
        String path = root_path + mTransactionId + "/resources/" + "cnsSO" + PrimeUtils.getSoId(gRef.get()) + ".dat";
        String areaCodePath = root_path + mTransactionId + "/resources/" + "cnsSO" + PrimeUtils.getSoId(gRef.get())
                + "_" +
                PrimeUtils.getAreaCode(gRef.get()) + ".dat";
        if (check_ticker_file(areaCodePath))
            return true;
        else
            return check_ticker_file(path);
    }

    private boolean check_ticker_file(String path) {
        // Log.i(TAG, "check_ticker_file : " + path);
        File file = new File(path);
        if (file.exists()) {
            Log.i(TAG, "Ticker file found: " + path);
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains(".dat")) {
                        Log.i(TAG, "Ticker file point to another file : " + path);
                        String newPath = root_path + mTransactionId + "/resources/" + line.replace("#", "");
                        return check_ticker_file(newPath);
                    }
                    sb.append(line).append("\n"); // 手動補回換行符號
                }
                mRawTickerData = sb.toString();
                Log.i(TAG, "Ticker data get  : " + mRawTickerData);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        } else {
            return false;
        }
    }

    private boolean loadTickerData() {
        // For now, create some dummy data.
        // In a real application, this would load from a file or network.
        // Log.i(TAG, "Loading ticker data... mRawTickerData = " + mRawTickerData);
        if (check_ticker_exist() && mRawTickerData != null && !mRawTickerData.isEmpty()) {
            List<CNSTickerData> parsedData = CNSTickerData.parseData(mRawTickerData);
            if (parsedData != null) {
                mCNSTickerData = parsedData;
                return true;
            }
        }
        return false;
    }

    public TickerView.TickerPosition getTickerPosition(int position) {
        if (position == 3)
            return TickerView.TickerPosition.LEFT;
        else if (position == 4)
            return TickerView.TickerPosition.RIGHT;
        else
            return TickerView.TickerPosition.TOP;
    }

    private final Runnable mTickerHeartbeat = new Runnable() {
        @Override
        public void run() {
            if (!mIsRunning) {
                return;
            }

            // 0. 檢查當前是否有 Ticker 正在播放
            boolean isAnyViewVisible = false;
            for (TickerView view : mTickerViews.values()) {
                if (view != null && view.getVisibility() == View.VISIBLE) {
                    isAnyViewVisible = true;
                    break;
                }
            }

            if (!isAnyViewVisible && !mForceRestart) {
                mActiveTickerData = null;
            }

            boolean forceRestart = mForceRestart;
            mForceRestart = false;

            // 1. 尋找當前最適合播放的 Ticker
            CNSTickerData tickerToShow = findEligibleTicker();
            // if(tickerToShow != null)
            // Log.d(TAG,"tickerToShow = "+tickerToShow.ToString());
            if (tickerToShow != null) {
                TickerView.TickerPosition targetPosition = getTickerPosition(tickerToShow.getDisplayLocation());
                Log.d(TAG, "targetPosition = " + targetPosition);
                TickerView targetView = mTickerViews.get(targetPosition);

                // 2. 檢查目標 TickerView 是否存在且空閒
                if (targetView != null && (forceRestart
                        || (mTickerViews.get(TickerView.TickerPosition.TOP).getVisibility() == View.GONE
                                && mTickerViews.get(TickerView.TickerPosition.LEFT).getVisibility() == View.GONE
                                && mTickerViews.get(TickerView.TickerPosition.RIGHT).getVisibility() == View.GONE))) {
                    // 3. 播放它 (playTicker 內部會切換到主線程)
                    playTicker(targetView, tickerToShow);
                }
            }
            // 4. 安排下一次心跳
            mBackgroundHandler.postDelayed(this, HEARTBEAT_INTERVAL_MS);
        }
    };

    private CNSTickerData findEligibleTicker() {
        if (mTestModeStep > 0) {
            return generateTestTickerData(mTestModeStep);
        }

        long now = System.currentTimeMillis(); // 獲取當前時間戳
        List<CNSTickerData> candidates = new ArrayList<>();
        for (CNSTickerData data : mCNSTickerData) {
            // 1. 檢查是否在啟動時間範圍內
            // (這需要 CNSTickerData 中有 startTime 和 endTime 屬性)
            if (!data.isActiveAt(now)) {
                // Log.d(TAG,"data not active , "+data.ToString());
                continue; // 不在時間範圍內，跳過
            }

            candidates.add(data);
            // 4. 檢查頻道是否匹配
            if (!data.matchesChannel(mCurrentChannelDisplayNumber)) {
                // Log.d(TAG, "Ticker not matches channel: " + mCurrentChannelDisplayNumber + ",
                // data: " + data.ToString());
                candidates.remove(data);
                continue;
            }

            // 如果所有條件都滿足，這就是我們要找的 Ticker！
        }
        // 如果沒有任何候選者，直接返回 null
        if (candidates.isEmpty()) {
            return null;
        }

        // 如果只有一個候選者，直接返回它，無需排序
        Log.d(TAG, "candidates.size() = " + candidates.size());
        Log.d(TAG, "1111 candidates.get(0) = " + candidates.get(0).ToString());
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        // 如果循環結束都沒有找到，說明當前沒有可播放的 Ticker
        candidates.sort((tickerA, tickerB) -> {
            // 優先級規則 1: 比較上次播放時間，越久沒播的優先級越高
            // lastPlayTimeMillis 越小 -> 越靠前
            Log.d(TAG, "tickerA getLastPlayTimeMillis = " + tickerA.getLastPlayTimeMillis());
            Log.d(TAG, "tickerB getLastPlayTimeMillis = " + tickerB.getLastPlayTimeMillis());
            int timeCompare = Long.compare(tickerA.getLastPlayTimeMillis(), tickerB.getLastPlayTimeMillis());
            if (timeCompare != 0) {
                return timeCompare;
            }
            // 您可以在此加入更多自訂的排序規則，例如靜態優先級
            // int priorityCompare = Integer.compare(tickerA.getPriority(),
            // tickerB.getPriority());
            // return priorityCompare;

            return 0; // 如果所有條件都相同，則順序不變
        });
        Log.d(TAG, "2222 candidates.get(0) = " + candidates.get(0).ToString());
        return candidates.get(0);
    }

    private CNSTickerData generateTestTickerData(int step) {
        CNSTickerData data = new CNSTickerData();
        data.setStartTime(0);
        data.setEndTime(2359);
        data.setPeriod(0);
        data.setTimesOfPeriod(1);
        data.setFontSize("32");
        data.setScrollSpeed(3);
        data.setFontColor("#FFFFFF");
        data.setBgColor("#80000000");
        data.setChannelList(new ArrayList<>());
        data.setActionOfChannels(new ArrayList<>());
        data.setRegion("na");

        List<CNSTickerData.TextData> texts = new ArrayList<>();
        CNSTickerData.TextData t1 = new CNSTickerData.TextData();
        t1.fontColor = "#FF0000";
        t1.fontRealColor = Color.RED;

        String directionStr = "";
        if (step == 1) {
            data.setDisplayLocation(1); // TOP R2L
            directionStr = "【頂部橫向測試 (右->左)】";
        } else if (step == 2) {
            data.setDisplayLocation(2); // TOP L2R
            directionStr = "【頂部橫向測試 (左->右)】";
        } else if (step == 3) {
            data.setDisplayLocation(3); // LEFT
            directionStr = "【左側垂直測試】";
        } else if (step == 4) {
            data.setDisplayLocation(4); // RIGHT
            directionStr = "【右側垂直測試】";
        }

        t1.text = directionStr + " 這是一段測試跑馬燈文字，用於驗證起始位置是否從螢幕外開始，並在完全離開後結束。1234567890 ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        texts.add(t1);
        data.setTexts(texts);

        return data;
    }

    private void playTicker(TickerView tickerView, CNSTickerData data) {
        // 在後台線程準備好所有參數
        Log.d(TAG, "playTicker " + data.ToString() + " tickerView = " + tickerView);
        TickerView.Direction direction = TickerView.Direction.fromInt(data.getDisplayLocation());
        float fontSize = Float.parseFloat(data.getFontSize()) * 2;
        int bgColor = Color.parseColor(data.getBgColor());
        float speed = data.getScrollSpeed();
        int playCount = data.getTimesOfPeriod();
        int repeatCount = (playCount > 0) ? playCount - 1 : 0;
        List<CNSTickerData.TextData> texts = data.getTexts();

        // 步驟 3: 切換到主線程來安全地更新UI
        mMainHandler.post(() -> {
            Log.d(TAG, "Executing play on UI thread for Ticker display location: " + data.getDisplayLocation());

            // 根據方向調整佈局（如果需要）
            // 注意：這個邏輯假設XML佈局已經大致正確，此處做微調
            // setupLayoutParams(tickerView, direction);

            // 設定 TickerView 的所有屬性
            tickerView.setDirection(direction);
            tickerView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
            tickerView.setTextBackgroundColor(bgColor);
            tickerView.setSpeed(speed);
            tickerView.setRepeatCount(repeatCount);
            tickerView.setVisibility(View.VISIBLE);
            // 最終，設置內容並觸發動畫
            tickerView.setTexts(texts);
            data.setLastPlayTimeMillis(System.currentTimeMillis());
            mActiveTickerData = data;
        });
    }
}
