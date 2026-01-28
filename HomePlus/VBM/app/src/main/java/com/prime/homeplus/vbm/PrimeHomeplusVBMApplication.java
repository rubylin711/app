package com.prime.homeplus.vbm;

import static com.prime.datastructure.utils.Utils.getPid;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StatFs;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceConnection;
import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceInterface;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.utils.TVMessage;

// [新增] 引用 VBM AIDL 與 JSON 處理
import com.inspur.cnsvbm.IVbmBridge;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 全域 Application：啟動時綁 PrimeDTV 服務、註冊回呼、初始化 EPG/Channel，
 * 並維護目前前台 Activity、ChannelChangeManager 等共用元件。
 */
public class PrimeHomeplusVBMApplication extends Application
        implements PrimeDtvServiceInterface.onMessageListener,
                   PrimeDtvServiceConnection.PrimeDtvServiceConnectionCallback {

    private static final String TAG = "PrimeHomeplusTvApplication";

    // ---- Singleton / Globals ----
    private static PrimeHomeplusVBMApplication sInstance;

    // 與 PrimeDTV AIDL 封裝橋接
    @Nullable private static PrimeDtvServiceInterface sPrimeDtvService;

    // TIF inputId（由 PrimeTvInputService.onCreateSession 傳入後更新）
    @Nullable private static String sTvInputId = Pvcfg.getTvInputId();

    // 前台 Activity 追蹤（僅做 onMessage 投遞）
    @Nullable private static Activity sCurrentResumedActivity;

    // 與 Service 綁定的連線器
    @Nullable private PrimeDtvServiceConnection mPrimeDtvServiceConn;


    // 可選：讓外部註冊 Handler 與 ServiceInterface 溝通
    @Nullable private Handler mRegisteredHandler;

    // ------------------------------------------------------------
    // Application lifecycle
    // ------------------------------------------------------------
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        sInstance = this;

        // 取得單例 PrimeDtvServiceInterface 並註冊回呼
        sPrimeDtvService = PrimeDtvServiceInterface.getInstance(this);
        if (sPrimeDtvService == null) {
            Log.e(TAG, "PrimeDtvServiceInterface.getInstance() returned null!");
        } else {
            sPrimeDtvService.register_callbacks(this);
        }

        // 監聽 Activity 生命週期（用於 onMessage 投遞給 BaseActivity）
        registerLifecycleCallback();

        // 綁定 PrimeDTV 服務
        bindPrimeDtvService();
    }

    // ------------------------------------------------------------
    // Activity lifecycle tracking
    // ------------------------------------------------------------
    private void registerLifecycleCallback() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(@NonNull Activity a, @Nullable Bundle b) {
                Log.d(TAG, a.getLocalClassName() + " Created");
            }

            @Override public void onActivityStarted(@NonNull Activity a) {
                Log.d(TAG, a.getLocalClassName() + " Started");
            }

            @Override public void onActivityResumed(@NonNull Activity a) {
                Log.d(TAG, a.getLocalClassName() + " Resumed");
                sCurrentResumedActivity = a;
            }

            @Override public void onActivityPaused(@NonNull Activity a) {
                Log.d(TAG, a.getLocalClassName() + " Paused");
                if (sCurrentResumedActivity == a) {
                    sCurrentResumedActivity = null;
                }
            }

            @Override public void onActivityStopped(@NonNull Activity a) {
                Log.d(TAG, a.getLocalClassName() + " Stopped");
            }

            @Override public void onActivitySaveInstanceState(@NonNull Activity a, @NonNull Bundle outState) {
                Log.d(TAG, a.getLocalClassName() + " SaveInstanceState");
            }

            @Override public void onActivityDestroyed(@NonNull Activity a) {
                Log.d(TAG, a.getLocalClassName() + " Destroyed");
                if (sCurrentResumedActivity == a) {
                    sCurrentResumedActivity = null;
                }
            }
        });

        //啟動 VBM 相關功能
        initVbmPeriodicTask();
    }

    // ------------------------------------------------------------
    // Service binding
    // ------------------------------------------------------------
    private void bindPrimeDtvService() {
        int pid = getPid(this);
        mPrimeDtvServiceConn = new PrimeDtvServiceConnection(
                /* context      */ this,
                /* wrapper      */ get_prime_dtv_service(),
                /* appContext   */ getInstance(),
                /* pkg          */ PrimeDtvServiceConnection.PRIME_DTV_SERVICE_PKGNAME,
                /* action       */ PrimeDtvServiceConnection.PRIME_DTV_SERVICE_ACTION,
                /* tag          */ MainActivity.class.getSimpleName() + " PID: " + pid
        );
        bindService(mPrimeDtvServiceConn);
    }

    private void bindService(@NonNull PrimeDtvServiceConnection conn) {
        Log.d(TAG, "bindService pkg:" + conn.getPkg() + " action:" + conn.getAction());
        Intent intent = new Intent();
        intent.setPackage(conn.getPkg());
        intent.setAction(conn.getAction());
        try {
            // BIND_AUTO_CREATE：若服務未啟動則自動啟動
            boolean ok = getApplicationContext().bindService(intent, conn, Context.BIND_AUTO_CREATE);
            Log.d(TAG, "bindService result=" + ok);
        } catch (Throwable t) {
            Log.e(TAG, "bindService exception", t);
        }
    }

    private void unbindPrimeDtvService() {
        if (mPrimeDtvServiceConn != null) {
            try {
                getApplicationContext().unbindService(mPrimeDtvServiceConn);
            } catch (Throwable t) {
                Log.w(TAG, "unbindPrimeDtvService ignored", t);
            } finally {
                mPrimeDtvServiceConn = null;
            }
        }
    }

    // ------------------------------------------------------------
    // PrimeDtvServiceConnection callbacks
    // ------------------------------------------------------------
    @Override
    public void onServiceConnected() {
        Log.d(TAG, "onServiceConnected");
        if (sPrimeDtvService != null)
            sPrimeDtvService.init_service();
    }

    @Override
    public void onServiceDisconnected() {
        Log.d(TAG, "onServiceDisconnected");
        // 服務掉線時可在此清理／標記狀態
    }

    // ------------------------------------------------------------
    // PrimeDtvServiceInterface.onMessageListener
    // ------------------------------------------------------------
    @Override
    public void onMessage(@NonNull TVMessage msg) {
        // 將訊息轉交給目前在前台且繼承 BaseActivity 的畫面

        final int flag = msg.getMsgFlag();
        if (msg.getMsgFlag() == TVMessage.FLAG_SYSTEM && msg.getMsgType() == TVMessage.TYPE_SYSTEM_INIT) {
//            Log.d("dtvService", "TVMessage.TYPE_SYSTEM_INIT callback "+ PrimeHomeplusVBMApplication.class.getSimpleName());
            // 把目前 TIF inputId 同步給後端 Service（若已知）
            if (sPrimeDtvService != null && !sPrimeDtvService.isPrimeDtvServiceReady()) {
                sPrimeDtvService.check_lib_correct(com.prime.datastructure.BuildConfig.VERSION_NAME,
                        com.prime.dtv.BuildConfig.VERSION_NAME);
            }
            return;
        }
        // 其它非 AV：你原本的前景 Activity 分派
        Activity a = sCurrentResumedActivity;
        Log.d(TAG, "onMessage dropped (no foreground BaseActivity)");
    }

    // ------------------------------------------------------------
    // Public static accessors
    // ------------------------------------------------------------
    @MainThread
    public static PrimeHomeplusVBMApplication getInstance() {
        return sInstance;
    }

    /** 取得 AIDL 封裝服務入口（可為 null，使用前請判斷） */
    @Nullable
    public static PrimeDtvServiceInterface get_prime_dtv_service() {
        return sPrimeDtvService;
    }

    @Nullable
    public static String getTvInputId() {
        return sTvInputId;
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        // [新增] 清除排程
        if (mScheduler != null) mScheduler.shutdownNow();
        if (mIsVbmBound) unbindService(mVbmConnection);
    }

    // =====================================================================================
    // [新增區塊] VBM 專用功能 (排程器、JSON 封裝、資料抓取)
    // 放置在原始架構最下方，避免改動上方邏輯
    // =====================================================================================

    private IVbmBridge mVbmService;
    private boolean mIsVbmBound = false;
    private ScheduledExecutorService mScheduler;

    private final ServiceConnection mVbmConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mVbmService = IVbmBridge.Stub.asInterface(service);
            mIsVbmBound = true;
            Log.d(TAG, "VBM Bridge Connected");
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mVbmService = null;
            mIsVbmBound = false;
        }
    };

    private void initVbmPeriodicTask() {
        // 綁定 VBM MainService
        Intent intent = new Intent(this, com.prime.homeplus.vbm.MainService.class);
        bindService(intent, mVbmConnection, Context.BIND_AUTO_CREATE);

        // 啟動排程任務
        mScheduler = Executors.newSingleThreadScheduledExecutor();
        mScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                vbmCollectAndSendAll();
            }
        }, 1, 60, TimeUnit.MINUTES);
    }

    private void vbmCollectAndSendAll() {
        // 依 v1.29 規格：
        // - 3.10.3 System Resource Usage：每小時回報
        // - 3.10.1 Signal level / 3.10.2 Product status：應在「上傳時」附帶回報（Signal level 需 12 小時一次）
        // 因此此處僅保留 3.10.3，避免多餘上報與欄位格式不一致。
        Log.d(TAG, "VBM: Executing Periodic Agent 9 Task (type=2)...");
        vbmSystemResource(); // 3.10.3
    }

    private void sendVbmPacket(String agentId, String eventType, String[] values) {
        if (mVbmService == null) return;
        try {
            JSONObject json = new JSONObject();
            json.put("agentId", agentId);
            json.put("eventType", eventType);
            json.put("timestamp", String.valueOf(System.currentTimeMillis()));
            JSONArray valArr = new JSONArray();
            if (values != null) {
                for (String v : values) {
                    if (v == null) {
                        valArr.put("N/A");
                    } else {
                        String t = v.trim();
                        valArr.put(t.isEmpty() ? "N/A" : t);
                    }
                }
            }
            json.put("values", valArr);
            mVbmService.sendVbmJson(json.toString());
        } catch (Exception e) { Log.e(TAG, "VBM send fail: " + e.getMessage()); }
    }

    private void vbmSignalLevel() {
        if (sPrimeDtvService == null) return;
        try {
            String[] tunerData = new String[4];
            for (int i = 0; i < 4; i++) {
                if (sPrimeDtvService.get_tuner_status(i)) {
                    tunerData[i] = String.format(Locale.US, "0/%d/%ddb/%ddBuV",
                            sPrimeDtvService.get_signal_ber(i),
                            sPrimeDtvService.get_signal_snr(i),
                            sPrimeDtvService.get_signal_strength(i));
                } else tunerData[i] = "N/A";
            }
            String[] macs = sPrimeDtvService.get_mac();
            String[] values = new String[10]; Arrays.fill(values, "N/A");
            values[0] = "N/A"; values[1] = tunerData[0]; values[2] = tunerData[1];
            values[3] = tunerData[2]; values[4] = tunerData[3];
            if (macs != null && macs.length >= 2) { values[5] = macs[0]; values[8] = macs[1]; }
            values[7] = Build.DISPLAY;
            sendVbmPacket("9", "0", values);
        } catch (Exception e) {}
    }

    private void vbmProductStatus() {
        String[] v20 = new String[10]; Arrays.fill(v20, "N/A");
        v20[0] = "N/A"; v20[1] = "20"; sendVbmPacket("9", "1", v20);
        String[] v21 = new String[10]; Arrays.fill(v21, "N/A");
        v21[0] = "N/A"; v21[1] = "21"; sendVbmPacket("9", "1", v21);
    }

    private void vbmSystemResource() {
        String[] values = new String[10]; Arrays.fill(values, "N/A");
        values[0] = "N/A";
        values[1] = vbmGetCpu();
        values[2] = vbmGetMem();
        values[3] = vbmGetFlash();
        values[4] = vbmGetRam();
        sendVbmPacket("9", "2", values);
    }

    private String vbmGetCpu() {
        try {
            Process p = Runtime.getRuntime().exec("top -n 1");
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line; List<Map.Entry<String, Double>> list = new ArrayList<>();
            boolean found = false;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.contains("PID") && line.contains("ARGS")) { found = true; continue; }
                if (found) {
                    String[] tokens = line.split("\\s+");
                    if (tokens.length >= 12) {
                        try {
                            double val = Double.parseDouble(tokens[8].replace("%", ""));
                            if (!tokens[11].equals("top")) list.add(new java.util.AbstractMap.SimpleEntry<>(tokens[11], val));
                        } catch (Exception e) {}
                    }
                }
            }
            Collections.sort(list, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
            StringBuilder sb = new StringBuilder();
            for (int i=0; i<Math.min(5, list.size()); i++) {
                if (i>0) sb.append(";");
                sb.append(i+1).append(".").append(list.get(i).getKey()).append("|").append(String.format("%.1f", list.get(i).getValue())).append("%");
            }
            return sb.length() > 0 ? sb.toString() : "N/A";
        } catch (Exception e) { return "N/A"; }
    }

    private String vbmGetMem() {
        try {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> apps = am.getRunningAppProcesses();
            if (apps == null) return "N/A";
            Map<String, Integer> map = new HashMap<>();
            for (ActivityManager.RunningAppProcessInfo a : apps) {
                android.os.Debug.MemoryInfo[] mi = am.getProcessMemoryInfo(new int[]{a.pid});
                map.put(a.processName, mi[0].getTotalPss());
            }
            List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
            Collections.sort(list, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
            ActivityManager.MemoryInfo ami = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(ami);
            long total = ami.totalMem / 1024;
            StringBuilder sb = new StringBuilder();
            for (int i=0; i<Math.min(5, list.size()); i++) {
                if (i>0) sb.append(";");
                sb.append(i+1).append(".").append(list.get(i).getKey()).append("|").append(String.format("%.1f", (list.get(i).getValue()/(double)total)*100)).append("%");
            }
            return sb.length() > 0 ? sb.toString() : "N/A";
        } catch (Exception e) { return "N/A"; }
    }

    private String vbmGetFlash() {
        try {
            StatFs s = new StatFs(Environment.getDataDirectory().getPath());
            long total = (s.getBlockCountLong() * s.getBlockSizeLong()) / (1024 * 1024);
            long free = (s.getAvailableBlocksLong() * s.getBlockSizeLong()) / (1024 * 1024);
            return free + "M/" + total + "M";
        } catch (Exception e) { return "N/A"; }
    }

    private String vbmGetRam() {
        try {
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getMemoryInfo(mi);
            return (mi.availMem / (1024*1024)) + "M/" + (mi.totalMem / (1024*1024)) + "M";
        } catch (Exception e) { return "N/A"; }
    }
}
