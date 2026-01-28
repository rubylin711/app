# 📺 Teletext Subtitle System

本模組負責解析與顯示 DVB Full-Page Teletext 字幕，遵循 ETSI EN 300 472 / EN 300 706 標準設計。適用於 Android TV / STB 整合字幕顯示需求。

---

## 📁 檔案結構說明

```
.
├── DtvContract.java
├── TeletextCodec.java
├── TeletextContentProvider.java
├── TeletextDecoder.java
├── TeletextManager.java
└── TeletextPageCollector.java
```

---

## 📄 各檔案用途

### `DtvContract.java`
定義 Teletext 資料表的欄位結構與 URI，作為 ContentProvider 存取介面基礎。

- `Pages.CONTENT_URI`
- `COLUMN_PAGE`, `COLUMN_PAGE_SUBPAGE`, `COLUMN_DATA`

---

### `TeletextCodec.java`
Teletext 解碼基底類別，負責：

- InputBuffer 管理
- 非同步解析機制（HandlerThread）
- 提供 `parse()` 抽象方法給子類實作

---

### `TeletextContentProvider.java`
實作 Android ContentProvider，提供 SQLite 資料庫 CRUD 功能，讓外部可透過 URI 存取已解碼的 Teletext 頁面資料。

---

### `TeletextDecoder.java`
Teletext 解碼控制器，接收 PES 資料並注入 TeletextCodec 處理流程。

- `feedTeletextPes(byte[] pes)`
- 將資料分配至 InputBuffer，觸發解析流程

---

### `TeletextManager.java`
字幕控制總管，負責與播放器同步、控制顯示/隱藏字幕，調用 Decoder 與 Renderer。

---

### `TeletextPageCollector.java`
✅ Teletext 解碼主力模組，實作：

- Packet0~31 解碼（Header、SubPage、Data Block）
- Parity / Hamming 解析（Odd Parity、Hamming 8/4）
- 將頁面資料組成 `int[]` 並儲存進 DB
- 完整支援 Clock Page (0:0)、Index Page (100)

---

## 📌 使用流程簡述

```
PES byte[] →
TeletextDecoder.feedTeletextPes →
TeletextCodec.InputBuffer →
TeletextPageCollector.parse →
DtvContract.Pages insert →
TeletextRenderer 顯示頁面內容
```

---

## ✅ 採用標準

- ETSI EN 300 472 (Teletext in DVB bitstreams)
- ETSI EN 300 706 (Enhanced Teletext Specification)

---

如需整合 UI 顯示、子頁控制、Renderer 請洽閱 `TeletextRenderer.java` 搭配使用。