package com.prime.dtv.service.CNS;

import android.graphics.Color; // 記得 import Color
import android.text.TextUtils;
import android.util.Log;

import com.prime.datastructure.sysdata.CNSEasData;

import java.nio.charset.StandardCharsets;

public class CNSEasParser {
    private static final String TAG = "CNSEasParser";

    public static CNSEasData parse(byte[] rawData) {
        if (rawData == null || rawData.length == 0) {
            return null;
        }

        // 1. 轉成 UTF-8 字串
        String dataString = new String(rawData, StandardCharsets.UTF_8);
        Log.d(TAG, "Parsing data: " + dataString);

        // 2. 檢查 EAS 開頭
        if (!dataString.startsWith("EAS")) {
            Log.e(TAG, "Invalid format: Not starting with EAS");
            return null;
        }

        // 3. 切割字串 (使用 | 分隔)
        // 注意：如果最後面的欄位是空的，split 預設會丟棄，使用 limit -1 可以保留空字串
        String[] parts = dataString.split("\\|", -1);

        // 格式參考:
        // Index 0: EAS
        // Index 1: paramLength (9 or 10)
        // Index 2: repeatCount
        // Index 3: color (#FG#BG)
        // Index 4: alertLevel
        // Index 5: alertType
        // Index 6: alertSound
        // Index 7: startTime
        // Index 8: endTime
        // Index 9: alertTitle
        // Index 10: alertMessage
        // Index 11: forceTune (Optional, only if paramLength == 10)

        if (parts.length < 11) {
            Log.e(TAG, "Invalid format: Insufficient parts, length=" + parts.length);
            return null;
        }

        try {
            CNSEasData eas = new CNSEasData();

            // 解析參數長度
            int paraLength = Integer.parseInt(parts[1]);
            // Log.d(TAG, "paraLength=" + paraLength); // Debug 用，正式版可拿掉

            eas.setRepeatCount(Integer.parseInt(parts[2]));

            // --- (1) 顏色解析開始 ---
            String colorPart = parts[3];
            // 格式為 #FFFF00#0000FF80，split("#") 會得到 ["", "FFFF00", "0000FF80"]
            String[] colors = colorPart.split("#");
            Log.d(TAG, "colors.length="+colors.length);//eric lin test
            for(int i=0;i<colors.length;i++)//eric lin test
                Log.d(TAG, "colors="+colors[i]);//eric lin test
            if (colors.length >= 3) {
                // 前景 (Foreground): 通常是 RGB (6碼)
                String fgHex = "#" + colors[1];
                try {
                    Log.d(TAG, "fgHex="+fgHex+
                                ", fgHex="+Color.parseColor(fgHex));//eric lin test
                    eas.setForegroundColor(Color.parseColor(fgHex));
                } catch (IllegalArgumentException e) {
                    eas.setForegroundColor(Color.WHITE); // 解析失敗給預設值
                    Log.w(TAG, "Invalid FG color: " + fgHex);
                }

                // 背景 (Background): 規格書說是 RGBA (8碼, ex: RRGGBBAA)
                // Android Color.parseColor 吃的是 ARGB (AARRGGBB)
                // 所以需要搬移 Alpha 值
                String bgRaw = colors[2];
                String bgHex;
                Log.d(TAG, "bgRaw.length()="+bgRaw.length()+
                        ", bgRaw="+bgRaw);//eric lin test
                if (bgRaw.length() == 8) {
                    // RRGGBBAA -> AARRGGBB
                    // 01234567
                    String rgb = bgRaw.substring(0, 6);
                    String alpha = bgRaw.substring(6, 8);
                    Log.d(TAG, "rgb="+rgb+
                            ", alpha="+alpha);//eric lin test
                    bgHex = "#" + alpha + rgb;
                    Log.d(TAG, "bgHex="+bgHex);//eric lin test
                } else {
                    bgHex = "#" + bgRaw; // 如果不是8碼，就照原本的嘗試解析
                }

                try {
                    Log.d(TAG, "bgHex="+bgHex+
                            ", bgHex="+Color.parseColor(bgHex));//eric lin test
                    eas.setBackgroundColor(Color.parseColor(bgHex));
                } catch (IllegalArgumentException e) {
                    eas.setBackgroundColor(Color.TRANSPARENT); // 解析失敗給透明
                    Log.w(TAG, "Invalid BG color: " + bgHex);
                }
            }
            // --- 顏色解析結束 ---

            eas.setAlertLevel(Integer.parseInt(parts[4]));
            eas.setAlertType(Integer.parseInt(parts[5]));
            eas.setAlertSound(Integer.parseInt(parts[6]));

            // 使用 Long 解析時間，比較安全
            eas.setStartTime(Long.parseLong(parts[7]));
            eas.setEndTime(Long.parseLong(parts[8]));

            eas.setAlertTitle(parts[9]);
            eas.setAlertMessage(parts[10]);

            // --- (2) Force Tune 解析開始 ---
            // 只有當 paramLength 為 10 且 陣列長度足夠時才解析
            if (paraLength == 10 && parts.length > 11) {
                String ftString = parts[11]; // 直接拿 string，不用 new String
                if (!TextUtils.isEmpty(ftString)) {
                    // 格式: on_id/ts_id/service_id (例如: 1/310/25)
                    String[] ftParts = ftString.split("/");
                    if (ftParts.length >= 3) {
                        eas.setForceTune_on_id(Integer.parseInt(ftParts[0]));
                        eas.setForceTune_ts_id(Integer.parseInt(ftParts[1]));
                        eas.setForceTune_service_id(Integer.parseInt(ftParts[2]));
                    }
                }
            }
            // --- Force Tune 解析結束 ---

            // 設定接收時間 (System time)
            eas.setReceiveTime(System.currentTimeMillis());
            eas.setRead(0); // 預設未讀
            eas.setAlreadyShown(0); // 預設未顯示

            // 印出完整的 Log 檢查
            Log.d(TAG, "Parsed EAS Data: " + eas.toString());

            return eas;

        } catch (Exception e) {
            // 捕捉 NumberFormatException 或 IndexOutOfBoundsException
            Log.e(TAG, "Parse error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}