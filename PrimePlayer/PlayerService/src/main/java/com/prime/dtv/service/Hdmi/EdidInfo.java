package com.prime.dtv.service.Hdmi;

import android.util.Log;

public class EdidInfo {
    public static final String TAG = "EdidInfo";
    private String mEdid;
    private String mManufactureName = "";
    private String mModelName;
    private String mSerialNumber;

    public EdidInfo(String edid) {
        mEdid = edid;
        parseEdid();
    }

    private void parseEdid() {
        if(mEdid == null || mEdid.isEmpty())
            return;
        String[] lines = mEdid.split("\n");

        for (String line : lines) {
            // 移除行首尾多餘的空白
            String trimmedLine = line.trim();

            if (trimmedLine.isEmpty()) {
                continue; // 跳過空行
            }

            // 尋找第一個冒號 (:) 的位置，將其作為 Key 和 Value 的分隔符
            int colonIndex = trimmedLine.indexOf(':');

            if (colonIndex > 0 && colonIndex < trimmedLine.length() - 1) {

                // 3. 提取 Key (冒號前)
                String key = trimmedLine.substring(0, colonIndex).trim();

                // 4. 提取 Value (冒號後)
                // 由於冒號後面可能有多個空白，使用 trim() 清理
                String value = trimmedLine.substring(colonIndex + 1).trim();

                // 5. 儲存結果
                if (!key.isEmpty()) {
                    if(key.equals("Manufacture Name")) {
                        mManufactureName = value;
                    }
                }
            } else {
                Log.w(TAG, "Skipping unparseable line: " + trimmedLine);
            }
        }
    };

    public String getManufactureName() {
        Log.d(TAG, "getManufactureName: " + mManufactureName);
        return mManufactureName;
    }
}
