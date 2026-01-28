package com.prime.dtv.service.dsmcc;

import android.util.Log;

import com.prime.dtv.service.Table.DsmccTable;
import com.prime.dtv.service.dsmcc.parse.DdbMessage;
import com.prime.dtv.service.dsmcc.parse.DiiMessage;
import com.prime.dtv.service.dsmcc.parse.DsiMessage;

/**
 * Table 版 DSM-CC Collector 的基底適配器。
 * - 不再直接操作 Demux/Filter
 * - 內部持有 DsmccTable（extends Table），並把解析事件回拋給子類
 */
public abstract class DsmccCollectorBase {

    protected static final String TAG = "DsmccCollectorBase(Table)";
    protected final int tunerId;
    protected int carouselPid;
    protected String serviceName;

    private final Object stateLock = new Object();
    private volatile boolean isRunning = false;
    private volatile DsmccTable table; // volatile: 其他執行緒可見性

    public DsmccCollectorBase(int tunerId, int carouselPid, String serviceName) {
        this.tunerId = tunerId;
        this.carouselPid = carouselPid;
        this.serviceName = serviceName;
    }

    /** 啟動：建立 DsmccTable 並開始 monitor */
    public void start() {
        synchronized (stateLock) {
            if (isRunning) {
                Log.w(TAG, serviceName + " already running");
                return;
            }
            Log.i(TAG, "[start] " + serviceName + " tuner=" + tunerId + " pid=" + carouselPid);
            try {
                DsmccTable t = new DsmccTable(tunerId, carouselPid, new DsmccTable.Listener() {
                    @Override public void onDsi(DsiMessage dsi) { DsmccCollectorBase.this.onDsi(dsi); }
                    @Override public void onDii(DiiMessage dii) { DsmccCollectorBase.this.onDii(dii); }
                    @Override public void onDdb(DdbMessage ddb) { DsmccCollectorBase.this.onDdb(ddb); }
                    @Override public void onPrivate3C(byte[] raw) { DsmccCollectorBase.this.onPrivate3C(raw); }
                    @Override public void onObservedOther(byte[] raw, int len, int tableId) {
                        DsmccCollectorBase.this.onObservedOther(raw, len, tableId);
                    }
                    @Override public void onParseError(Throwable t) { DsmccCollectorBase.this.onParseError(t); }
                });
                this.table = t;          // 成功建立才賦值
                this.isRunning = true;
                onStarted();             // 可讓子類做額外設定
            } catch (Throwable e) {
                this.table = null;
                this.isRunning = false;
                Log.e(TAG, "[start] failed for " + serviceName, e);
                onStartFailed(e);
            }
        }
    }
    
    /** 停止：釋放 Table / HandlerThread */
    public void stop() {
        synchronized (stateLock) {
            if (!isRunning) return;
            Log.i(TAG, "[stop] " + serviceName);
            try {
                DsmccTable t = this.table;
                if (t != null) {
                    try { t.cleanup(); } catch (Throwable ignore) {}
                }
                onStopped();
            } finally {
                this.table = null;
                this.isRunning = false;
            }
        }
    }
    
    /** 熱切換到另一個 PID/服務名稱（會先 stop 再 start） */
    public void switchTo(int newPid, String newServiceName) {
        synchronized (stateLock) {
            boolean samePid = (newPid == this.carouselPid);
            boolean sameName = (newServiceName == null || newServiceName.equals(this.serviceName));
            if (samePid && sameName) {
                Log.i(TAG, "[switchTo] same pid/name; skip");
                return;
            }
        boolean wasRunning = isRunning;
        stop();
        this.carouselPid = newPid;
        if (newServiceName != null) this.serviceName = newServiceName;
            if (wasRunning) start();
        }
    }
  
    public boolean isRunning() { return isRunning; }

    /** 提供給子類安全取得 Table；可能回傳 null */
    protected final DsmccTable getTable() {
        return table;
    }

    // ============ 子類回拋 ============
    protected abstract void onDsi(DsiMessage dsi);
    protected abstract void onDii(DiiMessage dii);
    protected abstract void onDdb(DdbMessage ddb);

    protected void onPrivate3C(byte[] raw) {}
    protected void onObservedOther(byte[] raw, int len, int tableId) {
        Log.w(TAG, serviceName + " observed table_id=0x" + Integer.toHexString(tableId) + " len=" + len);
    }
    protected void onParseError(Throwable t) {
        Log.e(TAG, serviceName + " parse error", t);
    }
    protected void onStarted() {}
    protected void onStartFailed(Throwable t) {}
    protected void onStopped() {}

    // ===== 3C 控制 =====
    /** 關閉 3C（回傳是否真的有動到狀態） */
    protected final boolean disableDsmcc3C() {
        DsmccTable t = table;
        if (t == null) return false;
        try {
            t.disableDsmcc3C();
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "disableDsmcc3C error", e);
            return false;
        }
    }

    /** 開啟 3C（回傳是否真的有動到狀態） */
    protected final boolean enableDsmcc3C() {
        DsmccTable t = table;
        if (t == null) return false;
        try {
            t.enableDsmcc3C();
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "enableDsmcc3C error", e);
            return false;
        }
    }
}
