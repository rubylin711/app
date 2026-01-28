# DSM-CC / BIOP Carousels – PrimePlayer PlayerService

本模組負責從 DSM-CC carousel（PID from PMT）接收 DSI/DII/DDB，完成：
- DDB 多 section 組裝與去重
- 模組（module）重組與 BIOP 解析（ServiceGateway / Directory / File）
- 原子落地（snapshot -> publish active）與差異 GC
- 針對 AD、TICKER 兩條 carousel 並行處理（不同 PID）

## 目錄結構
app/PrimePlayer/PlayerService/src/main/java/com/prime/dtv/service/dsmcc
├── ActiveSnapshotManager.java # 管理 sessions/<txid>/ 與 active 指標、差異 GC
├── DsmccBiopCollector.java # 上層整合：接 DsmccCollectorBase→BIOP→落地→publish/gc
├── DsmccCollectorBase.java # 共同的 section 派送、去重與 DSI/DII/DDB 生命週期管理
├── DsmccModule.java # 封裝 AD/TICKER 兩個 collector 的啟停
├── TestDsmccService.java # 測試 Service：同時啟動 AD/TICKER
├── docs/
│ ├── dsmcc-flow.mmd # 高層流程圖（Mermaid）
│ ├── dsmcc-flow.png # PNG（由 mmd 轉出）
│ ├── dsmcc-sequence-biop.mmd # BIOP 事件時序圖（Mermaid）
│ ├── dsmcc-sequence-biop.png # PNG（由 mmd 轉出）
│ └── readme.md # 本檔
├── gen_png.sh # Mermaid → PNG 批次轉圖腳本（需 mmdc）
└── parse/
├── BiopReader.java # 解析 BIOP Directory/File，回呼 BiopSink
├── BiopSink.java # BIOP 事件回呼介面（onServiceGateway / onDirectory / onFile）
├── DsiMessage.java / DiiMessage.java / DdbMessage.java # 三種 message 的解析
├── DownloadSessionManager.java # DDB 模組聚合、模組完成回呼、下載 session 管理
├── ModuleAssembler.java # 模組 assembler（由 DDB 填入）
└── 其他解析輔助類別…


## 核心流程（簡述）

1. **Demux → DsmccCollectorBase**
   - 依 table_id 分派 DSI(0x1006) / DII(0x1002) / DDB(0x1003)
   - DDB 先做「多 section 組裝」，再做「(dl,mod,ver,block) 去重」
   - 模組完成後呼叫 `onModuleComplete(downloadId, moduleId, version, payload)`

2. **DsmccBiopCollector**
   - `onModuleComplete` 將 payload 丟到 **biopExecutor**（背景 thread pool）交由 `BiopReader.parse()` 解析
   - BIOP 解析後透過 **sinkHandler（序列化單執行緒）** 執行：
     - `onServiceGateway`：建立樹根與 mount
     - `onDirectory`：建立 parent/child 映射
     - `onFile`：原子寫檔（tmp → fsync → rename），寫入 `sessions/<txid>/<path>`
   - **publish + gc**：當 snapshot 足夠完整 → `ActiveSnapshotManager.publish(txid)` 更新 `active.txt` 指到最新 session，並對前一版做差異 GC

3. **多 carousel（AD/TICKER）**
   - 兩個 `DsmccBiopCollector` 分別以不同 PID 監聽，互不干擾
   - 每個 collector 自己的 biopExecutor / sinkHandler，避免互相阻塞

## 事件/回呼（給上層 UI 或業務）

`DsmccBiopCollector` 提供可選的進度回呼介面（簡述，實作在程式內）：
- **onModuleSaved**(downloadId, moduleId, version, savePath)
- **onCarouselAllSaved**(downloadId, activeDir, activeFile)
  - `activeFile`: `<baseDir>/active.txt`
  - `activeDir` : `<baseDir>/sessions/<txid-hex>/`

上層可在 `onCarouselAllSaved` 時讀取 `active.txt` 並載入最新資源；或直接使用 `activeDir`。

## Active 目錄結構與發布

/data/vendor/dtvdata/<SERVICE>/
├── sessions/
│ └── <txid-hex>/... # 本輪完整內容
└── active.txt # 內容為 "sessions/<txid-hex>"


UI/播放器只要拼接 `<baseDir>/<active.txt內容>/…` 即可讀到最新版檔案。


效能與優先序
DSM-CC HandlerThread：使用較高優先序（例如 THREAD_PRIORITY_DISPLAY）以確保 section 不積壓。

biopExecutor：解析重活於背景（THREAD_PRIORITY_BACKGROUND），避免壓住 handler。

sinkHandler：序列化單執行緒，避免 HashMap 等非 thread-safe 結構的競爭，同時將 I/O 聚集，降低磁碟抖動。

測試
透過 TestDsmccService 同時啟動 AD / TICKER。

觀察 log：

MODULE DONE → 模組重組完

BiopReader.parse done → BIOP 解析時間/速度

save OK → 檔案落地

publish / gc → 指標更新與差異清理

# DsmccTable – Demux + Handler 設計

`Table/DsmccTable.java` 以「Table 風格」監聽 DSM-CC（0x3B/0x3C），並把解析重活移出 callback，避免阻塞 demux。

## 角色與責任

- 建立 demux channel：
  - **0x3B**：承載 DSI/DII
  - **0x3C**：承載 DDB
- demux callback 僅拷貝 section 後 **投遞到 HandlerThread**
- `HandlerThread` 解析 section header，依 `message_id` 分流：
  - **0x1006 (DSI)**：解析 DSI；抽出 `tapTransactionId` 作為本輪 tx 標識
  - **0x1002 (DII)**：解析模組清單；建立 `(dl,mod,ver)` catalog 與去重位圖
  - **0x1003 (DDB)**：
    1. 多 section 組裝（同 `(dl,mod,ver,blockNo)`）
    2. 依 DII catalog 推算最後一段長度
    3. 合成單段 DDB，再交由上層（`mListener.onDdb()`）

## 去重與輪次切換

- 以 `tapTransactionId`（若無則 header.transaction_id）判斷輪次
- DII 變更（version 或 tx 改變）→ 清空 `ddbAssemblies` / 位圖 / catalog，切換至新的 `downloadId`
- DDB 若見到新 `downloadId` 也可保守切換（實務上 DII 應先到）

## 優先序

- `HandlerThread` 以 **`THREAD_PRIORITY_DISPLAY`** 啟動，並透過 `Process.setThreadPriority(threadId, …)` 再確保一次
- 減少 `pending` 堆積、縮短 demux 回壓

## 介面（Listener）

```java
public interface Listener {
    void onDsi(DsiMessage dsi);
    void onDii(DiiMessage dii);
    void onDdb(DdbMessage ddb);
    default void onPrivate3C(byte[] raw) {}
    default void onObservedOther(byte[] raw, int len, int tableId) {}
    default void onParseError(Throwable t) {}
}
通常上層會把 onDdb() 交由 DownloadSessionManager → onModuleComplete() → DsmccBiopCollector。

與 DsmccBiopCollector 的關係
DsmccTable 僅處理 section 層級 與 DDB 組裝

之後的：模組完成 → BIOP 解析 → 檔案落地/發佈/GC，全由 DsmccBiopCollector 及其 ActiveSnapshotManager 完成

故障與健壯性
超時清理：DDB 組裝以 DDB_ASSEMBLY_TIMEOUT_MS（預設 3000ms）淘汰殘缺

位圖去重：避免重送/亂序造成的重複解析

解析錯誤：不影響 handler loop，錯誤交由 onParseError() 回報



DSM-CC Table + BIOP Collector — README

本專案實作了 DSM-CC section 解析（DSI/DII/DDB）、DDB 重組與去重、以及 BIOP 模組解析與檔案落地，在高負載的 STB 環境中仍維持低延遲。

架構總覽

DsmccTable

以 HandlerThread(THREAD_PRIORITY_DISPLAY) 解析 DSM-CC section（0x3B、0x3C）。

不做重活：只做 header 解析、DDB block 組裝、去重與回拋上層。

具備 DII 目錄（catalog）與 DDB block 多 section 組裝、位圖去重。

DsmccBiopCollector

收到 DsmccTable.Listener 回拋的 onDsi/onDii/onDdb 事件。

將 BIOP 解析（重活）丟入 背景 ThreadPool（THREAD_PRIORITY_BACKGROUND）。

將 檔案落地與 快照管理序列化在單一 HandlerThread(THREAD_PRIORITY_BACKGROUND)（sinkHandler）上執行，避免資料競爭。

透過 ActiveSnapshotManager 管理 sessions/<txid>/... 與 active.txt 發佈與 GC。

DownloadSessionManager

管理 DDB block 聚合、模組完成回拋 onModuleComplete()。

關鍵設計
1) 執行緒與優先序

DSM-CC 解析：HandlerThread(..., THREAD_PRIORITY_DISPLAY)
讓 section 解析在 UI 繪製等級之上，避免被背景任務拖慢。

BIOP 解析：ThreadPoolExecutor + Process.setThreadPriority(THREAD_PRIORITY_BACKGROUND)
重活降到背景，避免搶 CPU。

檔案落地：sinkHandler（HandlerThread(THREAD_PRIORITY_BACKGROUND)）
序列化處理 onServiceGateway/onDirectory/onFile/saveImpl/publish/gc，避免非 thread-safe 結構競爭。

2) DDB 多 section 組裝與去重

同一個 (dl, moduleId, moduleVersion, blockNo) 建立一個 DdbAssembly 聚合器（位圖/大小雙條件）。

以 BitSet 追蹤收到的 section；expectedLen 由 DII 的 blockSize / moduleSize 推導最後一塊大小。

組裝完成後合成單段 section，再 DdbMessage.parse()。

每個 (dl, mod, ver) 維護 ModuleBitmap，只接收一次相同 block；0 號 block 到時清位圖並啟動。

3) Session 切換與發佈

以 DSI.ConnBinder.tapTransactionId（若無則用 header.transactionId）為 txid。

DSI、DII 變更時：

resetCarouselState(dl) 清理記憶體索引（索引/暫存檔/已發佈 txid）

snapshotMgr.ensureSessionRoot(txid) 建立 sessions/<txid>

maybePublishAndGc() 計算 live set、發佈 active.txt、清理舊快照（延遲 tombstone）。

主要類別 / 檔案

DsmccTable

DSM-CC section 解析、DDB 組裝、去重、回拋 Listener。

調整點：HandlerThread(..., Process.THREAD_PRIORITY_DISPLAY)。

DsmccBiopCollector

實作 DownloadSessionManager.Listener 與 BiopSink。

biopExecutor（背景 ThreadPool）處理 BiopReader.parse()。

sinkHandler 序列化 onFile/saveImpl/publish/gc。

模組完成訊號：onModuleComplete() 內以 barrier 方式在 sinkHandler 串上「檔案已落地」通知，並在 所有模組 Save 完畢時觸發 onCarouselAllSaved(downloadId, activePath)。

ActiveSnapshotManager

sessions/<txid>/ 下寫檔、active.txt 發佈與 GC 實作。

事件流程（簡）

Demux 送進 DsmccTable.parsing() → 轉交 HandlerThread。

DsmccTable.handleMessage()

DSI：解析 txid/version，回拋 onDsi()。

DII：解析 module 目錄，建立 (dl,mod,ver) 去重位圖與 catalog → 回拋 onDii()。

DDB：組裝、去重後回拋 onDdb()。

DownloadSessionManager 聚合 module → onModuleComplete(dl, mod, ver, payload)。

DsmccBiopCollector.onModuleComplete()：

丟 BiopReader.parse() 到 biopExecutor。

BiopReader 解析過程會呼叫 BiopSink.onServiceGateway/onDirectory/onFile → 這些都被丟到 sinkHandler 串行化。

解析完成在 sinkHandler 上 barrier：

標記該 moduleId 已 save；若 所有模組皆 save 且 inflight==1 → 觸發：

maybePublishAndGc()（產生/更新 active.txt）

stateListener.onCarouselAllSaved(downloadId, activePath)（可用於通知 UI）

如何接收「模組完成 / 全部完成」事件
1) 在 DsmccBiopCollector 註冊 ModuleStateListener
public interface ModuleStateListener {
    void onModuleSaved(int downloadId, int moduleId, int moduleVersion);
    void onCarouselAllSaved(int downloadId, String activePath);
}


onModuleSaved：單個模組的檔案都落地後（已經過 sinkHandler barrier）。

onCarouselAllSaved：所有模組皆 save 完成、且完成一次 publish+gc 後回調，並提供 activePath（通常是 <baseDir>/active.txt 內指向的 sessions/<txid>）。

UI 層可在 onCarouselAllSaved(...) 中讀取 active.txt，即拿到「穩定可用」的目錄根。

2) Service / Module 端串接

TestDsmccService 啟動 DsmccModule.startTicker()、startAd()。

在 DsmccModule 建立各自的 DsmccBiopCollector 時，呼叫 setModuleStateListener(...) 把回調丟回 Service 或 UI。

效能與容量建議

DsmccTable 的 HandlerThread 請維持 DISPLAY 等級，避免 high FPS UI 競爭時被餓死。

biopExecutor 建議 全域共用（避免兩個 PID 各自建池互相搶 CPU）；或把 maxPoolSize == corePoolSize 以限制並發。

ArrayBlockingQueue<>(32) 可視流量調整至 64~128，避免短時間尖峰丟任務。

DDB_ASSEMBLY_TIMEOUT_MS（預設 3000ms）可依訊號品質調整。

估算快取記憶體：

DDB 組裝 buffer ≈ 每個活躍 block 的 expectedLen；

70+ modules、每塊 ~4KB~100KB，尖峰期可抓 數 MB 等級 的暫存。

若需要上限，對 ddbAssemblies 加上 LRU/數量上限 與更積極的超時清除。

設定點

BUF_3B/BUF_3C：Demux filter buffer（DDB 流量大可放大 BUF_3B）。

THREAD_PRIORITY_*：可視機型調整，確保 DSM-CC thread 不被背景工作影響。

PENDING_LIMIT：BIOP 檔案先到、目錄未到時暫存筆數上限。

TOMBSTONE_GRACE_MS：快照舊檔延遲清除時間。

偵錯建議

觀察 pending（DsmccTable queue 深度）是否升高 → 調整 thread 優先序或 BIOP 池並發/佇列。

打開 pending > 50 的詳細 log，有助分析處理壓力。

查看 active.txt 與 sessions/<txid>/ 目錄，確認發佈與落地行為。

若兩個 PID 併跑導致延遲，考慮共用 BIOP 池、降低最大並發、提高 DSM-CC thread 優先序。

常見問答

Q：sinkHandler 和 DSM-CC HandlerThread 在同一 CPU 上會互相影響嗎？
A：Android 無法固定 CPU，可能排到同一顆 core。但因為 sinkHandler 是 BACKGROUND，排程時 DSM-CC（DISPLAY）會優先，因此影響有限。若仍受影響，可：

降低 BIOP 池並發、增大隊列（換取延遲降低丟失）。

減少 sinkHandler 的 I/O 次數（例如合併小檔寫入、使用批次 publish）。

Q：怎麼知道檔案已可給 UI 使用？
A：等到 onCarouselAllSaved(downloadId, activePath)。此時本輪所有檔案都在 sessions/<txid>，且 active.txt 已更新，UI 讀取 active.txt 指向路徑即可安全使用。

最小整合步驟

由 PMT 解析出 DSM-CC PID（AD/Ticker）。

建立 DsmccModule，呼叫 startAd() / startTicker()。

在 DsmccBiopCollector 設定 ModuleStateListener，接到

onModuleSaved(...)：可做增量更新

onCarouselAllSaved(downloadId, activePath)：通知 UI 讀取 active.txt 指向的 sessions/<txid>

需要停止時呼叫 stopAll()；或個別 stopAd()/stopTicker()。

版本管理與日誌

每次 DSI/DII 變更會在 log 中標註 dl/txid/ver。

BIOP 解析完成 log：BiopReader.parse done ... MB/s，可用於觀察 parse/I/O 速度。

DDB 位圖去重可在需要時打開 debug 印出（目前預設關）。