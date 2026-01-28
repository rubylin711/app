package com.prime.dtv.service.dsmcc;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.inspur.adservice.IADCallback;
import com.inspur.adservice.IADManager;
import com.prime.datastructure.config.Pvcfg;
import com.prime.dtv.service.cnsad.adparse.AdJsonAdapter;
import com.prime.dtv.service.cnsad.adparse.AdModel;
import com.prime.dtv.service.cnsad.adparse.AdParser;
import com.prime.dtv.service.cnsad.adparse.BlockType;
import com.prime.dtv.service.cnsad.tools.DtdValidator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DsmccService extends Service implements DsmccModule.UiListener {
    private static final String TAG = "DsmccService";

    // === Action / Extra for start-stop control ===
    public static final String CLASS_NAME = "com.prime.dtv.service.dsmcc.DsmccService";
    public static final String ACTION_DSMCC_START = "com.prime.dtv.ACTION_DSMCC_START";
    public static final String ACTION_DSMCC_STOP = "com.prime.dtv.ACTION_DSMCC_STOP";
    public static final String ACTION_TICKER_READY = "com.prime.dtv.ACTION_TICKER_READY";
    public static final String KEY_CNS_TICKER_PID = "KEY_CNS_TICKER_PID";
    public static final String KEY_CNS_AD_PID = "KEY_CNS_AD_PID";
    public static final String ACTION_REMOVE_TICKER_SESSION = "com.prime.dtv.ACTION_REMOVE_TICKER_SESSION";
    public static final String ACTION_KEEP_ONLY_TICKER_SESSION = "com.prime.dtv.ACTION_KEEP_ONLY_TICKER_SESSION";
    public static final String ACTION_REMOVE_AD_SESSION = "com.prime.dtv.ACTION_REMOVE_AD_SESSION";
    public static final String ACTION_KEEP_ONLY_AD_SESSION = "com.prime.dtv.ACTION_KEEP_ONLY_AD_SESSION";
    public static final String KEY_TRANSACTION_ID = "transaction_id";

    public static final String EXTRA_TEST_DIR = "dir";
    public static final String EXTRA_TEST_SESSION = "sessionDir";

    // === Keys for launcher side ===
    public static final String KEY_EPG = "epg";
    public static final String KEY_PORTAL = "portal";
    public static final String KEY_CHANNELS = "channels";
    public static final String KEY_MINIEPG = "miniEpg";

    // === Session path hint（沿用你的習慣）===
    private static final String SO_PATH = "SO01";
    private static final String AreaCode = "";
    private static final String TICKER_SESSION_BASE_PATH = "/data/vendor/dtvdata/TICKER/sessions/";
    private static final String AD_SESSION_BASE_PATH = "/data/vendor/dtvdata/AD/sessions/";

    private DsmccModule dsmccModule;
    private String mCnsTickerPid = "";
    private String mCnsAdPid = "";
    private int mTunerId = 0;
    private static boolean isRunning = false;
    private HandlerThread mWorkThread;
    private Handler mWork;

    // === JSON cache & listeners ===
    private final ConcurrentHashMap<String, String> mLatestJson = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<IADCallback>> mListeners = new ConcurrentHashMap<>();

    // === GSON single instance ===
    private static final Gson GSON = new Gson();

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // Log.d(TAG, "DsmccService BroadcastReceiver action = " + action);
            if (ACTION_REMOVE_TICKER_SESSION.equals(action)) {
                String txid = intent.getStringExtra(KEY_TRANSACTION_ID);
                Log.i(TAG, "Received broadcast to remove ticker session: " + txid);
                removeTickerSession(txid);
            } else if (ACTION_KEEP_ONLY_TICKER_SESSION.equals(action)) {
                String txid = intent.getStringExtra(KEY_TRANSACTION_ID);
                Log.i(TAG, "Received broadcast to keep only ticker session: " + txid);
                keepOnlyTickerSession(txid);
            } else if (ACTION_REMOVE_AD_SESSION.equals(action)) {
                String txid = intent.getStringExtra(KEY_TRANSACTION_ID);
                Log.i(TAG, "Received broadcast to remove AD session: " + txid);
                removeAdSession(txid);
            } else if (ACTION_KEEP_ONLY_AD_SESSION.equals(action)) {
                String txid = intent.getStringExtra(KEY_TRANSACTION_ID);
                Log.i(TAG, "Received broadcast to keep only AD session: " + txid);
                keepOnlyAdSession(txid);
            }

        }
    };

    // ===== AIDL Service (server) =====
    private final IADManager.Stub mIADManagerService = new IADManager.Stub() {
        @Override
        public String getADJsonData(String key) throws RemoteException {
            String v = mLatestJson.get(key);
            return v == null ? "" : v;
        }

        @Override
        public void registerListener(IADCallback cb, String type) throws RemoteException {
            Log.i(TAG, "IADManager registerListener type = " + type);
            mListeners.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(cb);

            String json = mLatestJson.get(type);
            if (json != null) {
                try {
                    cb.dataInfrom(json);
                } catch (RemoteException ignore) {
                }
            }
        }

        @Override
        public void unregisterListener(IADCallback cb) throws RemoteException {
            Log.i(TAG, "IADManager unregisterListener cb=" + cb);
            IBinder b = cb.asBinder();
            for (Map.Entry<String, CopyOnWriteArrayList<IADCallback>> e : mListeners.entrySet()) {
                removeCallbackByBinder(e.getKey(), b);
            }
        }
    };

    private void removeCallbackByBinder(String type, IBinder binder) {
        CopyOnWriteArrayList<IADCallback> list = mListeners.get(type);
        if (list == null)
            return;
        for (IADCallback it : list) {
            if (it != null && it.asBinder().equals(binder)) {
                list.remove(it);
                try {
                    binder.unlinkToDeath(null, 0);
                } catch (Throwable ignore) {
                }
                Log.i(TAG, "unregister: removed type=" + type);
                break;
            }
        }
        if (list.isEmpty())
            mListeners.remove(type);
    }

    // ===== Service lifecycle =====
    @Override
    public void onCreate() {
        super.onCreate();
        dsmccModule = new DsmccModule();
        dsmccModule.setUiListener(this);
        isRunning = true;

        mWorkThread = new HandlerThread("DsmccWork");
        mWorkThread.start();
        mWork = new Handler(mWorkThread.getLooper());

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_REMOVE_TICKER_SESSION);
        filter.addAction(ACTION_KEEP_ONLY_TICKER_SESSION);
        filter.addAction(ACTION_REMOVE_AD_SESSION);
        filter.addAction(ACTION_KEEP_ONLY_AD_SESSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // TIRAMISU 是 Android 13
            registerReceiver(mReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            // 對於 Android 13 以下的版本，不需要這個標誌
            registerReceiver(mReceiver, filter);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null)
            return START_STICKY;

        final String action = intent.getAction();
        Log.d(TAG, "onStartCommand action=" + action);

        // 1) 由 PrimeDtvService 直接下指令：START
        if (ACTION_DSMCC_START.equals(action)) {
            String tickerPid = intent.getStringExtra(KEY_CNS_TICKER_PID);
            String adPid = intent.getStringExtra(KEY_CNS_AD_PID);

            if (tickerPid != null && !tickerPid.equalsIgnoreCase(mCnsTickerPid)) {
                mCnsTickerPid = tickerPid;
                if (!mCnsTickerPid.isEmpty()) {
                    dsmccModule.stopTicker();
                    dsmccModule.startTicker(mTunerId, parseHexOrDec(mCnsTickerPid));
                    Log.i(TAG, "✅ DSMCC Ticker started on PID " + mCnsTickerPid);
                }
            }

            if (adPid != null && !adPid.equalsIgnoreCase(mCnsAdPid)) {
                mCnsAdPid = adPid;
                if (!mCnsAdPid.isEmpty()) {
                    dsmccModule.stopAd();
                    dsmccModule.startAd(mTunerId, parseHexOrDec(mCnsAdPid));
                    Log.i(TAG, "✅ DSMCC AD started on PID " + mCnsAdPid);
                }
            }
            return START_STICKY;
        }

        // 2) STOP
        if (ACTION_DSMCC_STOP.equals(action)) {
            String tickerPid = intent.getStringExtra(KEY_CNS_TICKER_PID);
            String adPid = intent.getStringExtra(KEY_CNS_AD_PID);

            if (TextUtils.isEmpty(tickerPid))
                dsmccModule.stopTicker();
            if (TextUtils.isEmpty(adPid))
                dsmccModule.stopAd();
            return START_STICKY;
        }

        // 3) 保留你原本 test dir/session（但建議丟背景執行，下面第 3 點）
        String dir = intent.getStringExtra(EXTRA_TEST_DIR);
        if (!TextUtils.isEmpty(dir)) {
            File f = new File(dir);
            Log.i(TAG, "[TEST] onStartCommand dir=" + f.getAbsolutePath());
            if (mWork != null)
                mWork.post(() -> handleAdReady(f));
            else
                handleAdReady(f);
            return START_STICKY;
        }

        String session = intent.getStringExtra(EXTRA_TEST_SESSION);
        if (!TextUtils.isEmpty(session)) {
            File f = new File(session, SO_PATH + AreaCode);
            Log.i(TAG, "[TEST] onStartCommand session=" + session + " -> " + f.getAbsolutePath());
            if (mWork != null)
                mWork.post(() -> handleAdReady(f));
            else
                handleAdReady(f);
            return START_STICKY;
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mIADManagerService;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "🛑 DSMCC stopped");
        if (dsmccModule != null)
            dsmccModule.stopAll();
        if (mWorkThread != null)
            mWorkThread.quitSafely();
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            Log.w(TAG, "unregisterReceiver failed: " + e.getMessage());
        }
        isRunning = false;
        super.onDestroy();
    }

    public static boolean isRunning() {
        return isRunning;
    }

    private static int parseHexOrDec(String s) {
        if (TextUtils.isEmpty(s))
            return 0;
        try {
            String t = s.trim().toLowerCase(Locale.US);
            if (t.startsWith("0x"))
                return Integer.parseInt(t.substring(2), 16);
            return Integer.parseInt(t, 10);
        } catch (Throwable ignore) {
            return 0;
        }
    }

    @Override
    public void onTickerAllSaved(int pid, int downloadId, long txid, String relPath, String absPath) {
        Log.i(TAG, String.format(
                "chuck1 Ticker ready. pid=0x%X dl=0x%08X tx=0x%08X rel=%s abs=%s",
                pid, downloadId, txid, relPath, absPath));
        // 你可以在這裡通知 UI / 發廣播 / 更新資料庫等：
        // sendBroadcast(new Intent(ACTION_TICKER_READY).putExtra(...));
        String transaction_id = DsmccBiopCollector.hex8Lower(txid);
        // Log.i(TAG, "ticker transaction_id: " + transaction_id +
        // " Pvcfg.get_cns_ticker_transaction_id():
        // "+Pvcfg.get_cns_ticker_transaction_id());
        if (!transaction_id.equals(Pvcfg.get_cns_ticker_transaction_id())) {
            Pvcfg.set_cns_ticker_transaction_id(transaction_id);
            Log.i(TAG, "set_cns_ticker_transaction_id(): " + Pvcfg.get_cns_ticker_transaction_id());
        }
        Intent intent = new Intent(ACTION_TICKER_READY);
        sendBroadcast(intent);
    }

    @Override
    public void onAdAllSaved(int pid, int downloadId, long txid, String relPath, String absPath) {
        Log.i(TAG, String.format(
                "chuck1 AD ready. pid=0x%X dl=0x%08X tx=0x%08X rel=%s abs=%s",
                pid, downloadId, txid, relPath, absPath));
        // 依你慣例：SO01 + AreaCode
        if (mWork != null)
            mWork.post(() -> handleAdReady(new File(absPath + "/" + SO_PATH + AreaCode)));
        else
            handleAdReady(new File(absPath + "/" + SO_PATH + AreaCode));

        String transaction_id = DsmccBiopCollector.hex8Lower(txid);
        if (!transaction_id.equals(Pvcfg.get_cns_ad_transaction_id()))
            Pvcfg.set_cns_ad_transaction_id(transaction_id);
    }

    // ===== Core: parse → convert → cache → notify =====
    private void handleAdReady(File sessionDir) {
        if (sessionDir == null || !sessionDir.isDirectory()) {
            Log.e(TAG, "[AD] sessionDir invalid: " + sessionDir);
            return;
        }

        // 外層 data.xml
        File workDir = sessionDir;
        File dataXml = new File(workDir, "data.xml");
        if (!dataXml.exists()) {
            Log.e(TAG, "[AD] data.xml not found: " + dataXml);
            return;
        }

        // 嗅探 <readfolder value="...">（僅讀頭幾 KB）
        String rf = fastExtractReadFolder(dataXml);
        if (rf != null) {
            // 先試子資料夾
            File childDir = new File(workDir, rf);
            File childData = new File(childDir, "data.xml");
            // 再試同層兄弟
            File parent = workDir.getParentFile();
            File sibDir = (parent != null) ? new File(parent, rf) : null;
            File sibData = (sibDir != null) ? new File(sibDir, "data.xml") : null;

            if (childData.exists()) {
                Log.i(TAG, "[readfolder] -> child: " + childDir.getAbsolutePath());
                workDir = childDir;
                dataXml = childData;
            } else if (sibData != null && sibData.exists()) {
                Log.i(TAG, "[readfolder] -> sibling: " + sibDir.getAbsolutePath());
                workDir = sibDir;
                dataXml = sibData;
            } else {
                Log.w(TAG, "[readfolder] target not found: " + rf + " (stay at " + workDir + ")");
            }
        }

        // DTD（可選：失敗不擋流程）
        File dataDtd = new File(workDir, "data.dtd");
        try {
            boolean dtdOk = DtdValidator.validate(dataXml, dataDtd.exists() ? dataDtd : null);
            Log.i(TAG, "[PHASE:DTD] " + (dtdOk ? "OK" : "FAIL") + " file=" + dataXml.getAbsolutePath());
        } catch (Throwable t) {
            Log.w(TAG, "[PHASE:DTD] threw, continue parsing anyway", t);
        }

        // 解析 → 轉 JSON → 快取＋推播
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(dataXml), 64 * 1024)) {
            AdModel model = AdParser.parse(bis);
            Log.i(TAG, "[PHASE:PARSE] parsed OK, blocks=" + (model.getBlocks() == null ? 0 : model.getBlocks().size()));

            String prefix = workDir.getAbsolutePath();
            String portalJson = AdJsonAdapter.toInspurADDateBeanJson(model, BlockType.PORTAL, prefix);
            String epgJson = AdJsonAdapter.toInspurADDateBeanJson(model, BlockType.EPG, prefix);
            String miniepgJson = AdJsonAdapter.toInspurADDateBeanJson(model, BlockType.MINIEPG, prefix);
            String channelsJson = AdJsonAdapter.toInspurADDateBeanJson(model, BlockType.CHANNELS, prefix);

            logLong(TAG, "chuck portal   => ", portalJson);
            logLong(TAG, "epg(full)=> ", epgJson);
            logLong(TAG, "chuck miniepg  => ", miniepgJson);
            logLong(TAG, "chuck channels => ", channelsJson);

            // 關鍵：更新快取並通知 listener
            updateBlockJson(KEY_PORTAL, portalJson);
            updateBlockJson(KEY_CHANNELS, channelsJson);
            updateBlockJson(KEY_EPG, epgJson);
            updateBlockJson(KEY_MINIEPG, miniepgJson);
            // 如果未來客戶端也監聽 "miniepg"，可另外：
            // updateBlockJson("miniepg", miniepgJson);

            /*
            ADData epgAdData      = parseBlockJsonToADData("miniepg",  miniepgJson);
            ADData portalAdData   = parseBlockJsonToADData("portal",   portalJson);
            ADData channelsAdData = parseBlockJsonToADData("channels", channelsJson);
            if (epgAdData != null && !TextUtils.isEmpty(epgAdData.pathPrefix)
                    && !epgAdData.playMode.isEmpty()
                    && !epgAdData.playModeSubType.isEmpty()
                    && !epgAdData.durationValue.isEmpty()
                    && !epgAdData.adImageList.isEmpty()) {
                Log.d(TAG, "compat epg(miniepg): " + GSON.toJson(epgAdData));
            }
            if (portalAdData != null && !TextUtils.isEmpty(portalAdData.pathPrefix)
                    && !portalAdData.playMode.isEmpty()
                    && !portalAdData.playModeSubType.isEmpty()
                    && !portalAdData.durationValue.isEmpty()
                    && !portalAdData.adImageList.isEmpty()) {
                Log.d(TAG, "compat portal: " + GSON.toJson(portalAdData));
            }
            if (channelsAdData != null && !TextUtils.isEmpty(channelsAdData.pathPrefix)
                    && !channelsAdData.playMode.isEmpty()
                    && !channelsAdData.playModeSubType.isEmpty()
                    && !channelsAdData.durationValue.isEmpty()
                    && !channelsAdData.adImageList.isEmpty()) {
                Log.d(TAG, "compat channels: " + GSON.toJson(channelsAdData));
            }

             */
        } catch (Throwable t) {
            Log.e(TAG, "[AD] parse/convert failed", t);
        }
    }

    // === 嗅探 <readfolder value="...">（只讀前幾 KB；找不到回 null）===
    private static @Nullable String fastExtractReadFolder(File dataXml) {
        final int MAX = 8 * 1024;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(dataXml);
            int cap = (int) Math.min(Math.max(1024, dataXml.length()), MAX);
            byte[] buf = new byte[cap];
            int n = fis.read(buf);
            if (n <= 0)
                return null;

            String head = new String(buf, 0, n, StandardCharsets.UTF_8);
            int tagPos = head.toLowerCase(Locale.US).indexOf("<readfolder");
            if (tagPos < 0)
                return null;
            int vPos = head.indexOf("value", tagPos);
            if (vPos < 0)
                return null;
            int eq = head.indexOf('=', vPos);
            if (eq < 0)
                return null;

            int q1 = -1, q2 = -1;
            for (int i = eq + 1; i < head.length(); i++) {
                char c = head.charAt(i);
                if (c == '"' || c == '\'') {
                    q1 = i;
                    break;
                } else if (!Character.isWhitespace(c))
                    break;
            }
            if (q1 < 0)
                return null;
            char quote = head.charAt(q1);
            q2 = head.indexOf(quote, q1 + 1);
            if (q2 < 0)
                return null;

            String v = head.substring(q1 + 1, q2).trim();
            return v.isEmpty() ? null : v;
        } catch (Throwable ignore) {
            return null;
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (Throwable ignore) {
            }
        }
    }

    // === 存快取並通知所有該 key 的 listeners ===
    private void updateBlockJson(String key, String json) {
        if (json == null)
            json = "";
        mLatestJson.put(key, json);
        CopyOnWriteArrayList<IADCallback> list = mListeners.get(key);
        if (list != null && !list.isEmpty()) {
            for (IADCallback cb : list) {
                try {
                    cb.dataInfrom(json);
                } catch (RemoteException ignore) {
                }
            }
        }
    }

    /*
    private static ADData parseBlockJsonToADData(String block, String json) {
        if (TextUtils.isEmpty(json)) {
            Log.w(TAG, "[compat] " + block + " json empty");
            return null;
        }
        try {
            ADDateBean data = GSON.fromJson(json, ADDateBean.class);
            if (data == null || data.getChildren() == null || data.getChildren().isEmpty()) {
                Log.w(TAG, "[compat] " + block + " no children");
                return null;
            }
            ADData out = new ADData();
            out.entryTime  = data.getEntryTime();
            out.pathPrefix = data.getPathPrefix();

            for (ADDateBean.ChildrenBean ch : data.getChildren()) {
                if (ch == null) continue;

                out.playMode.add(ch.getPlayModeType());
                out.playModeSubType.add(ch.getPlayModeSubType());
                out.playModeValue.add(safeParseInt(ch.getPlayModeValue(), 10));
                out.durationValue.add(safeParseInt(ch.getDurationValue(), 0));

                java.util.List<ADDateBean.ChildrenBean.AssetTypeBean.ImageBean> imgs =
                        (ch.getAssetType() == null) ? null : ch.getAssetType().getImage();
                if (imgs != null && !imgs.isEmpty()) {
                    java.util.List<ADImage> adImages = new java.util.ArrayList<>(imgs.size());
                    for (ADDateBean.ChildrenBean.AssetTypeBean.ImageBean im : imgs) {
                        if (im == null) continue;
                        ADImage ai = new ADImage();
                        ai.assetValue = im.getAssetValue();
                        ai.actionType = im.getActionType();
                        ai.actionValue = im.getActionValue();
                        adImages.add(ai);
                    }
                    out.adImageList.add(adImages);
                } else {
                    out.adImageList.add(new java.util.ArrayList<>());
                }
            }

            Log.d(TAG, "[compat] " + block + " parsed: "
                    + "children=" + data.getChildren().size()
                    + " playModes=" + out.playMode.size()
                    + " durations=" + out.durationValue.size()
                    + " imageGroups=" + out.adImageList.size());
            return out;
        } catch (Throwable t) {
            Log.e(TAG, "[compat] " + block + " parse fail: " + t.getMessage());
            return null;
        }
    }
*/
    private static int safeParseInt(String s, int def) {
        if (TextUtils.isEmpty(s)) return def;
        try { return Integer.parseInt(s.trim()); } catch (Throwable ignore) { return def; }
    }

    private static void logLong(String tag, String prefix, String big) {
        if (big == null) {
            Log.i(tag, prefix + " <null>");
            return;
        }
        final int MAX = 2000;
        int i = 0, n = big.length();
        while (i < n) {
            int end = Math.min(n, i + MAX);
            Log.i(tag, prefix + big.substring(i, end));
            i = end;
            prefix = "";
        }
    }

    public void removeTickerSession(String transactionId) {
        if (TextUtils.isEmpty(transactionId)) {
            Log.w(TAG, "removeTickerSession: transactionId is empty");
            return;
        }
        File sessionDir = new File(TICKER_SESSION_BASE_PATH + transactionId);
        if (sessionDir.exists()) {
            Log.i(TAG, "Removing ticker session directory: " + sessionDir.getAbsolutePath());
            deleteRecursive(sessionDir);
        } else {
            Log.w(TAG, "Ticker session directory does not exist: " + sessionDir.getAbsolutePath());
        }
    }

    private static void deleteRecursive(File fileOrDirectory) {
        // Log.i(TAG,"fileOrDirectory "+fileOrDirectory.getAbsolutePath()+" "+
        // fileOrDirectory.getName()
        // + " fileOrDirectory.isDirectory() = "+fileOrDirectory.isDirectory());
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children) {
                    // Log.i(TAG,"child "+child.getAbsolutePath()+" "+ child.getName());
                    deleteRecursive(child);
                }
            }
        }
        String str = "deleteRecursive " + fileOrDirectory.getAbsoluteFile() + " " + fileOrDirectory.getName();
        // fileOrDirectory.delete();
        try {
            Files.delete(fileOrDirectory.toPath());
            Log.i(TAG, str + " 刪除成功");
        } catch (NoSuchFileException x) {
            Log.e(TAG, str + " 檔案不存在");
        } catch (DirectoryNotEmptyException x) {
            Log.e(TAG, str + " 資料夾不為空，不能刪除");
        } catch (IOException x) {
            // 這裡通常會告訴你：檔案正在被另一個程序使用 (AccessDeniedException)
            Log.e(TAG, str + " 權限不足或檔案被鎖定: " + x);
            // Runtime.getRuntime().exec("rm -rf
            // /data/vendor/dtvdata/TICKER/sessions/93e10003/resources");
        }
    }

    public void keepOnlyTickerSession(String transactionId) {
        File baseDir = new File(TICKER_SESSION_BASE_PATH);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            Log.w(TAG, "keepOnlyTickerSession: baseDir does not exist or is not a directory");
            return;
        }

        File[] sessions = baseDir.listFiles();
        if (sessions == null)
            return;

        for (File session : sessions) {
            if (session.isDirectory() && !session.getName().equals(transactionId)) {
                Log.i(TAG, "Cleaning up old ticker session: " + session.getAbsolutePath());
                deleteRecursive(session);
            }
        }
    }

    public void removeAdSession(String transactionId) {
        if (TextUtils.isEmpty(transactionId)) {
            Log.w(TAG, "removeAdSession: transactionId is empty");
            return;
        }
        File sessionDir = new File(AD_SESSION_BASE_PATH + transactionId);
        if (sessionDir.exists()) {
            Log.i(TAG, "Removing AD session directory: " + sessionDir.getAbsolutePath());
            deleteRecursive(sessionDir);
        } else {
            Log.w(TAG, "AD session directory does not exist: " + sessionDir.getAbsolutePath());
        }
    }

    public void keepOnlyAdSession(String transactionId) {
        File baseDir = new File(AD_SESSION_BASE_PATH);
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            Log.w(TAG, "keepOnlyAdSession: baseDir does not exist or is not a directory");
            return;
        }

        File[] sessions = baseDir.listFiles();
        if (sessions == null)
            return;

        for (File session : sessions) {
            if (session.isDirectory() && !session.getName().equals(transactionId)) {
                Log.i(TAG, "Cleaning up old AD session: " + session.getAbsolutePath());
                deleteRecursive(session);
            }
        }
    }

    public static void resetTickerAndAdData() {
        Log.i(TAG, "resetTickerAndAdData: Clearing all AD and TICKER data");
        Pvcfg.set_cns_ad_transaction_id("");
        Pvcfg.set_cns_ticker_transaction_id("");
        File adDir = new File("/data/vendor/dtvdata/AD");
        if (adDir.exists() && adDir.isDirectory()) {
            File[] children = adDir.listFiles();
            if (children != null) {
                for (File child : children) {
                    Log.i(TAG, "Deleting AD child: " + child.getAbsolutePath());
                    deleteRecursive(child);
                }
            }
        } else {
            Log.w(TAG, "AD directory not found: " + adDir.getAbsolutePath());
        }

        File tickerDir = new File("/data/vendor/dtvdata/TICKER");
        if (tickerDir.exists() && tickerDir.isDirectory()) {
            File[] children = tickerDir.listFiles();
            if (children != null) {
                for (File child : children) {
                    Log.i(TAG, "Deleting TICKER child: " + child.getAbsolutePath());
                    deleteRecursive(child);
                }
            }
        } else {
            Log.w(TAG, "TICKER directory not found: " + tickerDir.getAbsolutePath());
        }
    }
}
