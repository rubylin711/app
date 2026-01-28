// 檔名: AdJsonAdapter.java
package com.prime.dtv.service.cnsad.adparse;

import android.util.JsonWriter;
import androidx.annotation.Nullable;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/** 將 AdModel 轉成客戶 Launcher 需要的 ADDateBean JSON（快速、零縮排、不產生中間物件）。 */
public final class AdJsonAdapter {

    private AdJsonAdapter() {}

    /** 執行緒區域的時間格式器（避免重複建立與鎖競爭） */
    private static final ThreadLocal<SimpleDateFormat> TL_FMT =
            ThreadLocal.withInitial(() -> {
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
                fmt.setTimeZone(TimeZone.getTimeZone("Asia/Taipei"));
                return fmt;
            });

    /**
     * 產出單一 block 的 ADDateBean JSON 字串。
     * @param model      解析後的資料模型
     * @param blockType  要輸出的區塊型別（portal/epg/miniepg/channels）
     * @param pathPrefix 供客戶端拼檔案路徑的前綴，可為 null
     */
    public static String toInspurADDateBeanJson(AdModel model, BlockType blockType, @Nullable String pathPrefix)
            throws Exception {

        // 預估 4KB，避免動態擴容
        StringWriter sw = new StringWriter(4096);
        JsonWriter jw = new JsonWriter(sw);
        // ★ 不能給 null；空字串 = 無縮排、最小輸出
        jw.setIndent("");

        jw.beginObject();
        jw.name("blockName").value(blockTypeToName(blockType));
        jw.name("entryTime").value(epochToLocalTime(model.getEntryEpochSec()));
        jw.name("pathPrefix").value(pathPrefix == null ? "" : pathPrefix);

        jw.name("children").beginArray();

        // 僅輸出指定 block 的 child 列表
        for (Block b : model.getBlocks()) {
            if (b.getName() != blockType) continue;

            // ---- 抽取欄位（全部走 getter；避免 null）----
            PlayMode pm = b.getPlayMode();
            final String pmType = (pm != null && pm.getType() == PlayModeType.RANDOM) ? "random" : "interval";
            String pmSubtype = "bytime";
            int pmValue = 10;

            if (pm != null) {
                String sub = pm.getSubtype();
                if (sub != null && sub.length() > 0) pmSubtype = sub;
                Integer val = pm.getValueSec();
                if (val != null) pmValue = val;
            }

            int duration = 0;
            Integer d = b.getDurationSec();
            if (d != null) duration = d;

            // 第一個 MEDIA 當作 videoValue
            String videoValue = "";
            for (Asset a : b.getAssets()) {
                if (a.getType() == AssetType.MEDIA) {
                    String vv = a.getValue();
                    if (vv != null) videoValue = vv;
                    break;
                }
            }

            // block-level action（若有，複製到每張圖）
            String actionType = "";
            String actionValue = "";
            ActionDef act = b.getAction();
            if (act != null) {
                actionType = safe(act.getType());
                actionValue = safe(act.getValue());
            }

            // ---- 寫出 child ----
            jw.beginObject();
            jw.name("childName").value(""); // XML 未提供 childName → 空字串

            jw.name("playModeType").value(pmType);
            jw.name("playModeSubType").value(pmSubtype);
            jw.name("playModeValue").value(Integer.toString(pmValue)); // 客戶端期待字串
            jw.name("durationValue").value(Integer.toString(duration)); // 客戶端期待字串
            jw.name("videoValue").value(videoValue);

            // assetType.image[]
            jw.name("assetType").beginObject();
            jw.name("image").beginArray();

            for (Asset a : b.getAssets()) {
                if (a.getType() != AssetType.IMAGE) continue;
                    jw.beginObject();
                    jw.name("assetValue").value(safe(a.getValue()));
                    jw.name("actionType").value(actionType);
                    jw.name("actionValue").value(actionValue);
                    jw.endObject();
                }

            jw.endArray();    // image
            jw.endObject();   // assetType

            jw.endObject();   // child
            }

        jw.endArray();   // children
        jw.endObject();  // root
        jw.flush();
        // 不必關閉 StringWriter；JsonWriter.close() 也可，但 flush 已足夠
        return sw.toString();
    }

    // ---------- helpers ----------

    private static String blockTypeToName(BlockType t) {
        switch (t) {
            case PORTAL:   return "portal";
            case EPG:      return "epg";
            case MINIEPG:  return "miniepg";
            case CHANNELS: return "channels";
            default:       return "unknown";
        }
    }

    private static String epochToLocalTime(long epochSec) {
        return TL_FMT.get().format(epochSec * 1000L);
    }

    private static String safe(@Nullable String s) {
        return s == null ? "" : s;
    }
}
