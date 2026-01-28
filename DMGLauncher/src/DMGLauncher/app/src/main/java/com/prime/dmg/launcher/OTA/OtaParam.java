package com.prime.dmg.launcher.OTA;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import java.util.Iterator;

public class OtaParam {
    static String TAG = "OtaParam";
    public static final String OTA_PAYLOAD_BIN_NAME = "payload.bin";
    public static final String OTA_PAYLOAD_PROPERTIES_NAME = "payload_properties.txt";
    public static final String OTA_METADATA_NAME = "metadata";

    private String payload_bin;
    private String payload_properties;
    private String metadata;
    private boolean is_force_update;
    private String download_url;
    private String md5_checksum;
    private int file_size;
    private String ota_version;
    private String release_note;
    private String model_check_name;
    private String full_intent;

    private static OtaParam g_ota_param = null;

    public String ToString() {
        String str = "payload_bin : " + payload_bin + " " + "payload_properties : "+ payload_properties + " " +
                "metadata : " + metadata + " " + "is_force_update : "+ is_force_update + " " +
                "download_url : "+ download_url + " md5_checksum : "+ md5_checksum + " " +
                "file_size : "+ file_size + " ota_version : "+ ota_version + " " +
                "release_note : "+ release_note + " model_check_name : "+ model_check_name + " " + " full_intent : " + full_intent;
        return str;
    }

    public static OtaParam get_instance() {
        if(g_ota_param == null)
            g_ota_param = new OtaParam();
        return g_ota_param;
    }
    public static OtaParam parser_from_acs_data(Intent intent) {
        OtaParam otaParam = OtaParam.get_instance();
        otaParam.is_force_update = intent.getBooleanExtra("is_force_update",false);
        otaParam.download_url = intent.getStringExtra("download_url");
        otaParam.metadata = intent.getStringExtra("download_url"); // will use download url + file name became real path
        otaParam.payload_bin = intent.getStringExtra("download_url"); // will use download url + file name became real path
        otaParam.payload_properties = intent.getStringExtra("download_url"); // will use download url + file name became real path
        otaParam.md5_checksum = intent.getStringExtra("md5_checksum");
        otaParam.file_size = intent.getIntExtra("file_size",0);
        otaParam.ota_version = intent.getStringExtra("ota_version");
        otaParam.release_note = intent.getStringExtra("release_note");
        otaParam.model_check_name = intent.getStringExtra("model_check_name");
        otaParam.full_intent = intentToJSONString(intent);
        Log.d(TAG,"ota param : "+otaParam.ToString());
        return otaParam;
    }

    public String getPayload_bin() {
        return payload_bin;
    }

    public void setPayload_bin(String payload_bin) {
        this.payload_bin = payload_bin;
    }

    public String getPayload_properties() {
        return payload_properties;
    }

    public void setPayload_properties(String payload_properties) {
        this.payload_properties = payload_properties;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public boolean getIs_force_update() {
        return is_force_update;
    }

    public void setIs_force_update(boolean is_force_update) {
        this.is_force_update = is_force_update;
    }

    public String getDownload_url() {
        return download_url;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    public String getMd5_checksum() {
        return md5_checksum;
    }

    public void setMd5_checksum(String md5_checksum) {
        this.md5_checksum = md5_checksum;
    }

    public int getFile_size() {
        return file_size;
    }

    public void setFile_size(int file_size) {
        this.file_size = file_size;
    }

    public String getOta_version() {
        return ota_version;
    }

    public void setOta_version(String ota_version) {
        this.ota_version = ota_version;
    }

    public String getRelease_note() {
        return release_note;
    }

    public void setRelease_note(String release_note) {
        this.release_note = release_note;
    }

    public String getModel_check_name() {
        return model_check_name;
    }

    public void setModel_check_name(String model_check_name) {
        this.model_check_name = model_check_name;
    }

    public void setFull_intent(String intent)
    {
        full_intent = intent;
    }

    public String getFull_intent_str()
    {
        return full_intent;
    }

    private static String intentToJSONString(Intent intent) {
        JSONObject jsonObject = new JSONObject();
        try {
            // 保存 Intent 的 Action
            if (intent.getAction() != null) {
                jsonObject.put("action", intent.getAction());
            }

            // 保存 Intent 的 Package
            if (intent.getPackage() != null) {
                jsonObject.put("package", intent.getPackage());
            }

            // 保存 Intent 的 Extras
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                JSONObject extrasObject = new JSONObject();
                for (String key : bundle.keySet()) {
                    try {
                        extrasObject.put(key, bundle.get(key));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                jsonObject.put("extras", extrasObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject.toString(); // 返回 JSON 字符串
    }

    public static Intent jsonStringToIntent(String jsonString) {
        if (jsonString != null) {
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                Intent intent = new Intent();

                // 恢复 Action
                if (jsonObject.has("action")) {
                    intent.setAction(jsonObject.getString("action"));
                }

                // 恢复 Package
                if (jsonObject.has("package")) {
                    intent.setPackage(jsonObject.getString("package"));
                }

                // 恢复 Extras
                if (jsonObject.has("extras")) {
                    JSONObject extrasObject = jsonObject.getJSONObject("extras");
                    for (Iterator<String> it = extrasObject.keys(); it.hasNext();) {
                        String key = it.next();
                        Object value = extrasObject.get(key);

                        // 根据类型恢复
                        if (value instanceof Boolean) {
                            intent.putExtra(key, (Boolean) value);
                        } else if (value instanceof String) {
                            intent.putExtra(key, (String) value);
                        } else if (value instanceof Integer) {
                            intent.putExtra(key, (Integer) value);
                        } else if (value instanceof Long) {
                            intent.putExtra(key, (Long) value);
                        } else if (value instanceof Double) {
                            intent.putExtra(key, (Double) value);
                        } // 可扩展其他类型
                    }
                }
                return intent;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
