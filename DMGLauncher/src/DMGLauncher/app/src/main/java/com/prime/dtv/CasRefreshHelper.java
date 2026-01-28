package com.prime.dtv;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.service.Util.HttpURLConnectionUtil;
import com.prime.dtv.service.Util.Utils;
import com.prime.dtv.service.datamanager.DataManager;
import com.prime.dtv.sysdata.CasData;
import com.prime.dtv.utils.LogUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CasRefreshHelper {
    private static final String TAG = "CasRefreshHelper";
    private static final String LICENSE_MAP_PATH = "/data/vendor/dtvdata/Launcher/licMap.json";
    private static final String BACKUP_LICENSE_MAP_PATH = "/data/vendor/dtvdata/Launcher/licMap_backup.json";
    private static final String CAS_DATA_PATH = "/data/vendor/dtvdata/Launcher/casData.json";
    private static final String BACKUP_CAS_DATA_PATH = "/data/vendor/dtvdata/Launcher/casData_backup.json";
    private static CasRefreshHelper g_cas_refresh_manager;

    private final Gson g_gson;

    private ConcurrentHashMap<String, String> g_license_map;
    private CasData g_cas_data;
    private final Handler g_save_handler;
    private final Runnable g_save_license_runnable;
    private final Runnable g_save_cas_data_runnable;
    private final DataManager mDatamanager;

    private CasRefreshHelper() {
        g_gson = new Gson();
        mDatamanager = DataManager.getDataManager();

        HandlerThread save_handler_thread = new HandlerThread("CasRefreshHelper-save-thread");
        save_handler_thread.start();
        g_save_handler = new Handler(save_handler_thread.getLooper());
        g_save_license_runnable = () -> {
            Log.d(TAG, "g_save_license_runnable: save to " + LICENSE_MAP_PATH + " and " + BACKUP_LICENSE_MAP_PATH);
            try (JsonWriter writer = new JsonWriter(new BufferedWriter(new FileWriter(LICENSE_MAP_PATH)));
                 JsonWriter backup_writer = new JsonWriter(new BufferedWriter(new FileWriter(BACKUP_LICENSE_MAP_PATH)))) {
                g_gson.toJson(g_license_map, ConcurrentHashMap.class, writer);
                g_gson.toJson(g_license_map, ConcurrentHashMap.class, backup_writer);
            } catch (IOException | JsonIOException e) {
                Log.e(TAG, "g_save_license_runnable: save failed", e);
            }
        };

        g_save_cas_data_runnable = () -> {
            Log.d(TAG, "g_save_cas_data_runnable: save to " + CAS_DATA_PATH + " and " + BACKUP_CAS_DATA_PATH);
            try (JsonWriter writer = new JsonWriter(new BufferedWriter(new FileWriter(CAS_DATA_PATH)));
                 JsonWriter backup_writer = new JsonWriter(new BufferedWriter(new FileWriter(BACKUP_CAS_DATA_PATH)))) {
                g_gson.toJson(g_cas_data.getRawJsonString(), String.class, writer);
                g_gson.toJson(g_cas_data.getRawJsonString(), String.class, backup_writer);
            } catch (IOException | JsonIOException e) {
                Log.e(TAG, "g_save_cas_data_runnable: save failed", e);
            }
        };

        load_license_map();
        load_cas_data();
    }

    public static CasRefreshHelper get_instance() {
        if(g_cas_refresh_manager == null) {
            g_cas_refresh_manager = new CasRefreshHelper();
        }

        return g_cas_refresh_manager;
    }

    private String get_entitlement_url(){
        String url;
        if(mDatamanager != null) {
            url = mDatamanager.getGposInfo().getWVCasLicenseURL();
            LogUtils.d("handleCasRefresh url = "+url);
            if(url.isEmpty()){
                url = Pvcfg.getWvcasEntitlementUrl();
            }
            else{
                url += "/fortress/wvcas/entitlements";
            }
        }else {
            LogUtils.e("mDatamanager is null !!!!!!!!!!!!!!!!!!");
            url = Pvcfg.getWvcasEntitlementUrl();
        }
        return url;
    }

    public CasData get_cas_data() {
        return g_cas_data;
    }

    private void load_license_map() {
        boolean need_save = false;
        ConcurrentHashMap<String, String> new_license_map = null;
        Log.d(TAG, "load_license_map: load data from file");
        try (JsonReader reader = new JsonReader(new BufferedReader(new FileReader(LICENSE_MAP_PATH)))) {
            new_license_map = g_gson.fromJson(reader, ConcurrentHashMap.class);
        } catch (IOException | JsonIOException | JsonSyntaxException e) {
            Log.w(TAG, "load_license_map: load data from file fail", e);
        }

        if (new_license_map == null) {
            Log.d(TAG, "load_license_map: load data from backup file");
            try (JsonReader reader = new JsonReader(new BufferedReader(new FileReader(BACKUP_LICENSE_MAP_PATH)))) {
                new_license_map = g_gson.fromJson(reader, ConcurrentHashMap.class);
                need_save = true; // load from backup success, should save correct data to file
            } catch (IOException | JsonIOException | JsonSyntaxException e) {
                Log.w(TAG, "load_license_map: load data from backup file fail", e);
            }

            if (new_license_map == null) {
                Log.d(TAG, "load_license_map: new license map");
                new_license_map = new ConcurrentHashMap<>();
            }
        }

        g_license_map = new_license_map;
        if (need_save) {
            save_license_map();
        }

        Log.d(TAG, "load_license_map: done, map = " + g_license_map);
    }

    private void load_cas_data() {
        Log.d(TAG, "load_cas_data: load data from server");
        boolean success = request_new_cas_data();

        if (!success) {
            Log.w(TAG, "load_cas_data: load data from server fail");
            Log.d(TAG, "load_cas_data: load data from file");
            try (JsonReader reader = new JsonReader(new BufferedReader(new FileReader(CAS_DATA_PATH)))) {
                String json_string = g_gson.fromJson(reader, String.class);
                g_cas_data = new CasData(json_string);
                success = true;
            } catch (IOException | JsonIOException | JsonSyntaxException e) {
                Log.w(TAG, "load_cas_data: load data from file fail");
            }

            if (!success) {
                Log.d(TAG, "load_cas_data: load data from backup file");
                try (JsonReader reader = new JsonReader(new BufferedReader(new FileReader(BACKUP_CAS_DATA_PATH)))) {
                    String json_string = g_gson.fromJson(reader, String.class);
                    g_cas_data = new CasData(json_string);
                    save_cas_data(); // load from backup success, save correct data to file
                } catch (IOException | JsonIOException | JsonSyntaxException e) {
                    Log.w(TAG, "load_cas_data: load data from backup file fail");
                    Log.d(TAG, "load_cas_data: new cas data");
                    g_cas_data = new CasData();
                }
            }
        }

        Log.d(TAG, "load_cas_data: done, data = " + g_cas_data);
    }

    public boolean request_new_cas_data() {
        String json_string = HttpURLConnectionUtil.doGet(get_entitlement_url());
        if (!json_string.isEmpty()) {
            g_cas_data = new CasData(json_string);
            save_cas_data();
            return true;
        }

        return false;
    }

    public void clear_cas_data() {
        g_cas_data = new CasData();
        save_cas_data();
    }

    public void update_license_mapping(String content_id, String license_id) {
        Log.d(TAG, "update_license_mapping: content id = " + content_id + ", license id = " + license_id);
        String pre_license_id = g_license_map.put(content_id, license_id);

        // only save license map if new license or license changed
        if (pre_license_id == null || !pre_license_id.equals(license_id)) {
            save_license_map();
        }

        //Log.d(TAG, "update_license_mapping: done, map = " + g_license_map);
    }

    public boolean remove_license_mapping(String content_id, String license_id) {
        Log.d(TAG, "remove_license_mapping: content id = " + content_id + ", license id = " + license_id);
        boolean is_removed = g_license_map.remove(content_id, license_id);

        if (is_removed) {
            save_license_map();
        }

        //Log.d(TAG, "remove_license_mapping: done, map = " + g_license_map);
        return is_removed;
    }

    public void clear_license_mapping() {
        Log.d(TAG, "clear_license_mapping: ");
        g_license_map.clear();

        save_license_map();

        //Log.d(TAG, "clear_license_mapping: done, map = " + g_license_map);
    }

    private void save_license_map() {
        // only save once if save_license_map() is called rapidly
        g_save_handler.removeCallbacks(g_save_license_runnable);
        g_save_handler.postDelayed(g_save_license_runnable, 1000);
    }

    private void save_cas_data() {
        // only save once if save_cas_data() is called rapidly
        g_save_handler.removeCallbacks(g_save_cas_data_runnable);
        g_save_handler.postDelayed(g_save_cas_data_runnable, 1000);
    }

    public static String parse_content_id(byte[] private_data) {
        if (private_data == null || private_data.length == 0) {
            return "";
        }

        String content_id = "";
        try {
            // assume private_data is valid if not empty
            int content_id_length_pos = 1/*header*/ + 1/*provider length*/ +
                    Byte.toUnsignedInt(private_data[1])/*provider string*/ + 1/*non-printable char*/;
            int content_id_length = Byte.toUnsignedInt(private_data[content_id_length_pos]);
            int content_id_string_pos = content_id_length_pos + 1;

            content_id = new String(
                    private_data,
                    content_id_string_pos,
                    content_id_length,
                    StandardCharsets.UTF_8
            );
        } catch (Exception e) {
            Log.e(TAG,"private_data = "+ Utils.bytesToHex(private_data));
            Log.e(TAG, "parse_content_id: parse failed", e);
        }

        return content_id;
    }

    public List<String> get_unentitled_content_ids() {
        List<String> untitled_content_ids = new ArrayList<>();
        for (String content_id : g_license_map.keySet()) {
            if (!g_cas_data.getEntitledChannelIds().contains(content_id)) {
                untitled_content_ids.add(content_id);
            }
        }

        return untitled_content_ids;
    }

    public String get_license_id(String content_id) {
       return g_license_map.get(content_id);
    }

    public static byte[] generate_private_data(String cas_provider, String content_id) {
        if (cas_provider == null || content_id == null) {
            Log.e(TAG, "generate_private_data: invalid parameters");
            return new byte[0];
        }

        int cas_provider_length = cas_provider.length();
        int content_id_length = content_id.length();
        byte[] cas_provider_bytes = cas_provider.getBytes(StandardCharsets.UTF_8);
        byte[] content_id_bytes = content_id.getBytes(StandardCharsets.UTF_8);

        // private data length = 1(header) + 1(cas provider length) + cas provider string +
        // 1(non-printable characters) + 1(content id length) + content id string
        byte[] private_data = new byte[1 + 1 + cas_provider_length + 1 + 1 + content_id_length];

        private_data[0] = 0x0A; // header
        private_data[1] = (byte) cas_provider_length; // cas provider length
        // cas provider string
        System.arraycopy(cas_provider_bytes, 0, private_data, 2, cas_provider_length);
        private_data[2 + cas_provider_length] = 0x12; // non-printable characters
        private_data[2 + cas_provider_length + 1] = (byte) content_id_length; // content id length
        // content id string
        System.arraycopy(
                content_id_bytes,
                0,
                private_data,
                2 + cas_provider_length + 2,
                content_id_length
        );

//        Log.d(TAG, "generate_private_data: " + Arrays.toString(private_data));
        return private_data;
    }

    public void remove_license(String contentID){
        String licenseId = get_license_id(contentID);


    }
}
