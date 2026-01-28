package com.prime.dtv.service.Table;

import android.media.tv.tuner.Tuner;
import android.media.tv.tuner.filter.Filter;
import android.media.tv.tuner.filter.FilterConfiguration;
import android.media.tv.tuner.filter.SectionSettingsWithSectionBits;
import android.media.tv.tuner.filter.Settings;
import android.media.tv.tuner.filter.TsFilterConfiguration;
import android.os.Process;

import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.service.Demux.DemuxSectionCallback;
import com.prime.dtv.service.Demux.Demux;
import com.prime.dtv.service.Tuner.TunerInterface;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 專管 DSM-CC 0x3C：動態 open/close，獨立 callback executor，並支援 idle 自動關閉。
 */
public class ThreeCFilterController {

    private static final String TAG = "ThreeCFilterController";

    private final int tunerId;
    private final long idleMs; // >0 則啟用 idle auto-close

    private final Object lock = new Object();
    private volatile Filter filter3C;
    private volatile ExecutorService exec3C;
    private volatile long lastTouch = 0;

    public ThreeCFilterController(int tunerId, long idleMs) {
        this.tunerId = tunerId;
        this.idleMs = idleMs;
    }

    /** 建立單執行緒 executor，並把 thread priority 設高一點（DISPLAY）。 */
    private static ExecutorService newSingleSvc(String name, int prio) {
        return Executors.newSingleThreadExecutor(r -> new Thread(() -> {
            try { Process.setThreadPriority(prio); } catch (Throwable ignore) {}
            r.run();
        }, name));
    }

    /** 以 DemuxChannel 參數動態開啟 3C filter；重複呼叫會關掉舊的再重開。 */
    public void enable(Demux.DemuxChannel ch3C) throws Exception {
        if (ch3C == null) return;

        synchronized (lock) {
            // 先關舊的
            safeCloseLocked();

            // 取 Tuner
            Tuner tuner = TunerInterface.getInstance().getTuner(tunerId);
            if (tuner == null) throw new IllegalStateException("Tuner is null for id=" + tunerId);

            // 準備 executor（單執行緒）
            exec3C = newSingleSvc("TsFilterCb-3C", Process.THREAD_PRIORITY_DISPLAY);

            // 開 Filter
            final byte[] filterData = ch3C.getFilter().getFilterData();
            final byte[] filterMask = ch3C.getFilter().getFilterMask();
            final boolean crcEnable = ch3C.getCrcEnable();
            final boolean repeat    = ch3C.getRepeat();
            final int pid           = ch3C.getPid();
            final DemuxSectionCallback cb = ch3C.getDemuxSectionCallback();

            int bufSize = (int) Math.max(256 * 1024, ch3C.getFilterBufferSize());

            Filter f = tuner.openFilter(
                    Filter.TYPE_TS,
                    Filter.SUBTYPE_SECTION,
                    bufSize,
                    exec3C,
                    cb.getFilterCallback());
            if (f == null) throw new IllegalStateException("openFilter(3C) returns null");

            byte[] mode = new byte[filterData != null ? filterData.length : 0];
            Settings settings = SectionSettingsWithSectionBits.builder(Filter.TYPE_TS)
                    .setFilter(filterData)
                    .setMask(filterMask)
                    .setMode(mode)
                    .setCrcEnabled(crcEnable)
                    .setRaw(false)
                    .setRepeat(repeat)
                    .build();

            FilterConfiguration cfg = TsFilterConfiguration.builder()
                    .setTpid(pid)
                    .setSettings(settings)
                    .build();

            f.configure(cfg);
            f.start();

            filter3C = f;
            lastTouch = System.currentTimeMillis();
            LogUtils.d(TAG + "chuck enable(): 3C filter opened, pid=0x" + Integer.toHexString(pid));
        }
    }

    /** 關閉 3C filter（安全可重入）。 */
    public void disable() {
        synchronized (lock) {
            safeCloseLocked();
        }
    }

    /** 每當收到 3C 資料時呼叫，刷新 idle 計時。 */
    public void touch() {
        lastTouch = System.currentTimeMillis();
    }

    /** 在你覺得適合的時機呼叫（例如每次 handleMessage 結尾），判斷是否 idle 太久自動關閉。 */
    public void maybeAutoClose() {
        if (idleMs <= 0) return; // 關閉 idle 功能
        Filter f = filter3C;
        if (f == null) return;

        long now = System.currentTimeMillis();
        long last = lastTouch;
        if (now - last > idleMs) {
            LogUtils.d(TAG + " idle timeout -> auto disable 3C");
            disable();
        }
    }

    private void safeCloseLocked() {
        if (filter3C != null) {
            LogUtils.d(TAG + "chuck  3C filter close " );
            try { filter3C.stop(); } catch (Throwable ignore) {}
            try { filter3C.close(); } catch (Throwable ignore) {}
            filter3C = null;
        }
        if (exec3C != null) {
            exec3C.shutdownNow();
            exec3C = null;
        }
    }
}
