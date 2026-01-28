package com.prime.dtv.service.CNS;

import android.util.Log;

import com.prime.datastructure.sysdata.CNSMailData;

import java.nio.charset.StandardCharsets;

public class CNSMailParser {
    private static final String TAG = "CNSMailParser";

    public static CNSMailData parse(byte[] rawData) {
        if (rawData == null || rawData.length == 0) {
            return null;
        }

        // 1. 轉成 UTF-8 字串
        String dataString = new String(rawData, StandardCharsets.UTF_8);
        Log.d(TAG, "Parsing data: " + dataString);

        // 2. 檢查 CAM 開頭
        if (!dataString.startsWith("CAM")) {
            Log.e(TAG, "Invalid format: Not starting with CAM");
            return null;
        }

        // 3. 切割字串
        String[] parts = dataString.split("\\|");
        // 格式: CAM | 3 | importance | title | body
        if (parts.length < 5) {
            Log.e(TAG, "Invalid format: Insufficient parts");
            return null;
        }

        try {
            CNSMailData mail = new CNSMailData();
            // 注意：這裡假設您的 MailData 有這些欄位。如果沒有，請去 MailData.java 新增。
            mail.setImportance(Integer.parseInt(parts[2]));
            mail.setTitle(parts[3]);
            mail.setBody(parts[4]);
            // 如果 MailData 欄位是 private，請改用 mail.setTitle(...) 等方法
            Log.d(TAG, "Importance=" + mail.getImportance()
                            +"Title=" + mail.getTitle()
                            +"Body=" + mail.getBody());

            return mail;
        } catch (NumberFormatException e) {
            Log.e(TAG, "Parse error: " + e.getMessage());
            return null;
        }
    }
}