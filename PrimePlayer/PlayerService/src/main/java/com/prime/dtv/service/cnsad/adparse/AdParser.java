package com.prime.dtv.service.cnsad.adparse;

import android.util.Log;
import android.util.Xml;

import androidx.annotation.Nullable;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * CNS-AD data.xml 解析器（支援 block/children/child 結構；放寬 <video> 規則）
 *
 * 對齊重點：
 * - playMode：type/subtype/value 必填（value 為秒）
 * - duration：value 必填（秒）
 * - asset：type/value 必填；weights 可選；type ∈ image|media|app|webpage
 * - action（區塊層級）：type/value 必填；code ∈ {red|green|yellow|blue|ok}，預設 "blue"
 * - video：僅要求 value，footage 改為可選（因實務多用兄弟 <duration>）
 *
 * 新增能力：
 * - 若出現 <children><child>...，每個 child 視為一個獨立 Block 實例（同一 block 名）
 * - 目前忽略「資產內 action」（<asset> 內的 <action>），避免誤綁到整個 block
 */
public final class AdParser {

    private AdParser() {}

    private static final String TAG = "CNS-AD/Parser";

    // 允許的時間格式（entry@time）
    private static final String[] TIME_PATTERNS = new String[] {
            "yyyy/MM/dd kk:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss"
    };

    /** Parse data.xml stream into AdModel. Caller closes the stream. */
    public static AdModel parse(InputStream ins) throws Exception {
        if (ins == null) throw new IllegalArgumentException("InputStream is null");

        XmlPullParser p = Xml.newPullParser();
        p.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        p.setInput(ins, null);

        long entryEpoch = 0L;
        String readFolder = null;
        List<Block> blocks = new ArrayList<>();

        // 當前 block 名稱（外層 <block name="...">）
        BlockType currentBlockName = null;

        // 每個 block/child 的「子狀態」
        PlayMode curPlayMode = null;
        Integer curDuration = null;
        ActionDef curAction = null;
        List<Asset> curAssets = new ArrayList<>();
        // ★ 新增：childName
        String curChildName = null;
        // 巢狀控制
        boolean insideChildren = false;   // 是否在 <children> 節點內
        boolean insideAsset = false;      // 是否在 <asset> 節點內（用來忽略 asset-level action）
        boolean sawAnyChildInThisBlock = false; // 當前 block 是否出現過 child

        int event = p.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                String tag = lower(p.getName());
                switch (tag) {
                    case "entry": {
                        String t = attr(p, "time");
                        if (!isBlank(t)) entryEpoch = parseEpochSec(t.trim());
                        break;
                    }
                    case "readfolder": {
                        readFolder = attr(p, "value");
                        break;
                    }
                    case "block": {
                        String n = attr(p, "name");
                        if (isBlank(n)) {
                            throw new IllegalArgumentException("block@name is required by DTD");
                        }
                        String name = lower(n);
                        if ("portal".equals(name))        currentBlockName = BlockType.PORTAL;
                        else if ("epg".equals(name))      currentBlockName = BlockType.EPG;
                        else if ("miniepg".equals(name))  currentBlockName = BlockType.MINIEPG;
                        else if ("channels".equals(name)) currentBlockName = BlockType.CHANNELS;
                        else {
                            // 若不想報錯可改為：Log.w(TAG, "Skip unknown block@name: " + n); currentBlockName = null;
                            throw new IllegalArgumentException("Unknown block@name: " + n);
                        }
                        // 新 block：重置 flag
                        insideChildren = false;
                        sawAnyChildInThisBlock = false;
                        // 子狀態也清一次（無 children 的情況會在 </block> 收斂）
                        curPlayMode = null;
                        curDuration = null;
                        curAction = null;
                        curAssets = new ArrayList<>();
                        break;
                    }
                    case "children": {
                        insideChildren = true;
                        break;
                    }
                    case "child": {
                        sawAnyChildInThisBlock = true;
                        curPlayMode = null;
                        curDuration = null;
                        curAction = null;
                        curAssets = new ArrayList<>();
                        // ★ 讀取 child@name
                        String n = attr(p, "name");
                        curChildName = (n == null) ? null : n;
                        break;
                    }
                    case "playmode": {
                        String typeStr = attr(p, "type");
                        String subtype = attr(p, "subtype");
                        String vStr = attr(p, "value");
                        if (isBlank(typeStr) || isBlank(subtype) || isBlank(vStr)) {
                            throw new IllegalArgumentException("playMode@type/subtype/value are required by DTD");
                        }
                        PlayModeType tp = "random".equalsIgnoreCase(typeStr)
                                ? PlayModeType.RANDOM : PlayModeType.INTERVAL;
                        Integer v = toInt(vStr);
                        if (v == null) {
                            throw new IllegalArgumentException("playMode@value must be integer seconds");
                        }
                        curPlayMode = new PlayMode(tp, subtype, v);
                        break;
                    }
                    case "duration": {
                        String v = attr(p, "value");
                        if (isBlank(v)) {
                            throw new IllegalArgumentException("duration@value is required by DTD");
                        }
                        curDuration = toInt(v);
                        if (curDuration == null) {
                            throw new IllegalArgumentException("duration@value must be integer seconds");
                        }
                        break;
                    }
                    case "assets": {
                        // 容器
                        break;
                    }
                    case "asset": {
                        insideAsset = true;
                        String ts = attr(p, "type");
                        String value = attr(p, "value");
                        if (isBlank(ts) || isBlank(value)) {
                            throw new IllegalArgumentException("asset@type and asset@value are required by DTD");
                        }
                        AssetType at = parseAssetType(ts);
                        Integer dur = toInt(attr(p, "duration")); // 可選
                        String footage = attr(p, "footage");       // 可選
                        curAssets.add(new Asset(at, value, dur, footage));
                        break;
                    }
                    case "video": {
                        // 放寬：只要 value；footage 改為可選
                        String value = attr(p, "value");
                        String footage = attr(p, "footage"); // nullable
                        if (isBlank(value)) {
                            throw new IllegalArgumentException("video@value is required by spec");
                        }
                        curAssets.add(new Asset(AssetType.MEDIA, value, null, footage));
                        break;
                    }
                    case "action": {
                        String type = attr(p, "type");
                        String code = attr(p, "code");
                        String value = attr(p, "value");
                        String parameter = attr(p, "parameter");
                        if (isBlank(type) || isBlank(value)) {
                            throw new IllegalArgumentException("action@type and action@value are required by DTD");
                        }
                        code = normalizeActionCode(code);
                        ActionDef act = new ActionDef(type, code, value, parameter);
    
                        if (insideAsset) {
                            // ★ 將 action 掛到最近一個 asset（資產層級）
                            if (!curAssets.isEmpty()) {
                                curAssets.get(curAssets.size() - 1).setAction(act);
                            } else {
                                Log.w(TAG, "<action> inside <asset> but no asset collected yet");
                            }
                        } else {
                            // 區塊層級
                            curAction = act;
                        }
                        break;
                    }
                    default:
                        // 其它容器標籤（root、entry 的內層等）忽略
                        break;
                }
            } else if (event == XmlPullParser.END_TAG) {
                String tag = lower(p.getName());
                switch (tag) {
                    case "asset":
                        insideAsset = false;
                        break;
                        case "child": {
                            if (currentBlockName != null) {
                                // ★ 建立 Block 時帶上 childName
                                blocks.add(new Block(
                                        currentBlockName,
                                        (curPlayMode != null ? curPlayMode : new PlayMode(PlayModeType.INTERVAL, "bytime", 10)),
                                        curDuration,
                                        curAssets,
                                        curAction,
                                        curChildName
                                ));
                            }
                            // 重置
                            curPlayMode = null;
                            curDuration = null;
                            curAction = null;
                            curAssets = new ArrayList<>();
                            curChildName = null; // ★
                            break;
                        }
                    case "children":
                        insideChildren = false;
                        break;
                    case "block": {
                        if (!sawAnyChildInThisBlock && currentBlockName != null) {
                            blocks.add(new Block(
                                    currentBlockName,
                                    (curPlayMode != null ? curPlayMode : new PlayMode(PlayModeType.INTERVAL, "bytime", 10)),
                                    curDuration,
                                    curAssets,
                                    curAction,
                                    curChildName   // 沒 child 就多半是 null
                            ));
                        }
                        currentBlockName = null;
                        curPlayMode = null;
                        curDuration = null;
                        curAction = null;
                        curAssets = new ArrayList<>();
                        curChildName = null; // ★
                        sawAnyChildInThisBlock = false;
                        break;
                    }
                }
            }
            event = p.next();
        }

        return new AdModel(entryEpoch, readFolder, blocks);
    }

    // -------------------- helpers --------------------

    private static boolean isBlank(@Nullable String s) {
        return s == null || s.trim().isEmpty();
    }

    @Nullable
    private static Integer toInt(@Nullable String s) {
        if (isBlank(s)) return null;
        try {
            return Integer.valueOf(s.trim());
        } catch (Throwable ignore) {
            return null;
        }
    }

    private static String lower(@Nullable String s) {
        return s == null ? null : s.toLowerCase(Locale.US);
    }

    private static String attr(XmlPullParser p, String name) {
        return p.getAttributeValue(null, name);
    }

    private static long parseEpochSec(String text) throws ParseException {
        TimeZone tz = TimeZone.getTimeZone("Asia/Taipei");
        for (String pat : TIME_PATTERNS) {
            try {
                SimpleDateFormat f = new SimpleDateFormat(pat, Locale.US);
                f.setLenient(true);
                f.setTimeZone(tz);
                return f.parse(text).getTime() / 1000L;
            } catch (Exception ignore) {}
        }
        throw new ParseException("Unsupported time format: " + text, 0);
    }

    private static AssetType parseAssetType(String ts) {
        String t = lower(ts);
        if ("media".equals(t)) return AssetType.MEDIA;
        if ("app".equals(t)) return AssetType.APP;
        if ("webpage".equals(t)) return AssetType.WEBPAGE;
        if ("image".equals(t)) return AssetType.IMAGE;
        throw new IllegalArgumentException("Unknown asset@type: " + ts);
    }

    /** code ∈ {red|green|yellow|blue|ok}，可缺省為 "blue" */
    private static String normalizeActionCode(@Nullable String code) {
        String c = isBlank(code) ? "blue" : lower(code);
        switch (c) {
            case "red":
            case "green":
            case "yellow":
            case "blue":
            case "ok":
                return c;
            default:
                throw new IllegalArgumentException("action@code must be one of red|green|yellow|blue|ok (got: " + code + ")");
        }
    }
}
