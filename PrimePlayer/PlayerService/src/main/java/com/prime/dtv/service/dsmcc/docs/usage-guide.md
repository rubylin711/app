DSM-CC 模組使用指南（AD / TICKER）

本模組負責從 DSM-CC Carousel（DII/DDB）解析 BIOP，將檔案原子寫入至快照路徑，並在整輪模組都存檔完成時，透過介面回呼通知上層 UI。

解析執行緒：HandlerThread（高優先序，跟得上 section）

BIOP 解析：背景 ThreadPool（降到 THREAD_PRIORITY_BACKGROUND）

寫檔：序列化 sinkHandler，避免資料競爭

快照：/data/vendor/dtvdata/<SERVICE>/sessions/<txid-hex>/...
當前生效快照寫在 active.txt（內容為 sessions/<txid-hex>）

1. 專案結構（重點）
service/dsmcc/
├─ ActiveSnapshotManager.java          # 快照/發布/GC
├─ DsmccModule.java                    # 對外入口（start/stop，UiListener）
├─ DsmccBiopCollector.java             # DSM-CC→BIOP→落地核心
├─ DsmccCollectorBase.java             # Collector 基底
├─ parse/                              # DII/DSI/DDB/BIOP 解析
└─ Table/DsmccTable.java               # 以 Demux+HandlerThread 解析 section


文件與工具

docs/
  ├─ readme.md
  ├─ dsmcc-flow.mmd / .png             # 整體流程圖
  └─ dsmcc-sequence-biop.mmd / .png    # BIOP 序列圖

2. 快速整合步驟
2.1 加入與初始化
// 例如在你的 Service 中
public class TestDsmccService extends Service implements DsmccModule.UiListener {
    private DsmccModule dsmcc;

    @Override public void onCreate() {
        super.onCreate();
        dsmcc = new DsmccModule();
        dsmcc.setUiListener(this); // ★ 接收完成回呼

        int tunerId   = 0;
        int tickerPid = 0x0820;    // 依 PMT 調整
        int adPid     = 0x0884;    // 依 PMT 調整

        dsmcc.startTicker(tunerId, tickerPid); // 啟動 TICKER
        dsmcc.startAd(tunerId, adPid);         // 啟動 AD
    }

    @Override public void onDestroy() {
        if (dsmcc != null) dsmcc.stopAll();
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent i) { return null; }
}


PID 來源：請從 SDT/PMT 解析取得 DSM-CC ES PID（常見 stream_type：0x0B/0x0D）。

2.2 UI 回呼（整輪可用時通知）

實作 DsmccModule.UiListener：

@Override
public void onTickerAllSaved(int pid, int downloadId, long txid, String relPath, String absPath) {
    // TICKER 全部檔案已落地並發布
    // relPath = active.txt 內容，如 "sessions/82460002"
    // absPath = /data/vendor/dtvdata/TICKER/sessions/82460002
    // TODO: 通知 UI / 更新 DB / 發廣播
}

@Override
public void onAdAllSaved(int pid, int downloadId, long txid, String relPath, String absPath) {
    // AD 全部檔案已落地並發布
    // 其餘同上
}


建議：UI 直接使用 absPath 作為根目錄載入檔案（圖片/XML/JSON 等）。

3. 路徑與檔案說明

以服務名稱（AD / TICKER）區分根目錄：

/data/vendor/dtvdata/AD/
└─ active.txt              # 內容為 "sessions/<txid-hex>"
   sessions/
     └─ <txid-hex>/
         ├─ ... (carousel 檔案)


active.txt：標示目前生效的快照子目錄。

UI 讀法：absPath = baseDir + "/" + readFile(active.txt)
例如：/data/vendor/dtvdata/AD/sessions/82460002

4. 啟停方式
4.1 以 Service 啟動（建議）

AndroidManifest.xml：

<service
    android:name=".service.dsmcc.TestDsmccService"
    android:exported="false" />


ADB 測試：

adb shell am start-foreground-service --user 0 -n com.prime.launcher/com.prime.dtv.service.dsmcc.TestDsmccService
adb shell am stopservice --user 0 -n com.prime.launcher/com.prime.dtv.service.dsmcc.TestDsmccService


4.2 程式內控制
DsmccModule m = new DsmccModule();
m.setUiListener(listener);
m.startTicker(tunerId, tickerPid);
m.startAd(tunerId, adPid);

// 停止
m.stopTicker();
m.stopAd();
m.stopAll();