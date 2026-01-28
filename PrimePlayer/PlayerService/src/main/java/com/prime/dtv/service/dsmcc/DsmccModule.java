package com.prime.dtv.service.dsmcc;

import android.util.Log;

public class DsmccModule {
    private static final String TAG = "DsmccModule";
    public interface UiListener {
        default void onAdAllSaved(int pid, int downloadId, long txid, String relPath, String absPath) {}
        default void onTickerAllSaved(int pid, int downloadId, long txid, String relPath, String absPath) {}
    }

    private UiListener uiListener;
    public void setUiListener(UiListener l) { this.uiListener = l; }

    private DsmccBiopCollector adCollector;
    private DsmccBiopCollector tickerCollector;

    public void startAd(int tunerId, int pid) {
        if (adCollector == null) {
            adCollector = new DsmccBiopCollector(tunerId, pid, "AD");
            // 轉拋 collector 的完成事件到 UiListener
            adCollector.setModuleStateListener(new DsmccBiopCollector.ModuleStateListener() {
                @Override public void onCarouselAllSaved(int dl, long tx, String rel, String abs) {
                    if (uiListener != null) uiListener.onAdAllSaved(pid, dl, tx, rel, abs);
                }
            });
        }
        adCollector.start();
    }
    
    public void startTicker(int tunerId, int pid) {
        if (tickerCollector == null) {
            tickerCollector = new DsmccBiopCollector(tunerId, pid, "TICKER");
            tickerCollector.setModuleStateListener(new DsmccBiopCollector.ModuleStateListener() {
                @Override public void onCarouselAllSaved(int dl, long tx, String rel, String abs) {
                    if (uiListener != null) uiListener.onTickerAllSaved(pid, dl, tx, rel, abs);
                }
            });
        }
        tickerCollector.start();
    }

    public void stopAd() {
        if (adCollector != null) {
            adCollector.stop();
        }
    }

    
    public void stopTicker() {
        if (tickerCollector != null) {
            tickerCollector.stop();
        }
    }

    public void stopAll() {
        stopAd();
        stopTicker();
    }
}
