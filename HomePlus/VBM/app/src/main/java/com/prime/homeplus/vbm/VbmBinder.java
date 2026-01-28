package com.prime.homeplus.vbm;

import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.inspur.cnsvbm.IVbmBridge; // 新的 AIDL

import java.lang.ref.WeakReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.google.gson.JsonSyntaxException;
public class VbmBinder extends IVbmBridge.Stub {
    private final WeakReference<MainService> mServiceRef;
    private static final String TAG = "VbmBinder";
    private static final String DEFAULT_VALUE = "N/A";

    private static final Gson VBM_GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    public VbmBinder(MainService service) {
        mServiceRef = new WeakReference<>(service);
    }
    // record-only POJO（欄位增減就改這裡；兩端需一致）
    private static final class VbmRecord {
        String agentId;
        String eventType;

        @JsonAdapter(FlexibleTimestampAdapter.class)
        String timestamp;

        String[] values;
    }

    private static final class FlexibleTimestampAdapter extends TypeAdapter<String> {
        @Override
        public void write(JsonWriter out, String value) throws java.io.IOException {
            out.value(value == null ? "" : value);
        }

        @Override
        public String read(JsonReader in) throws java.io.IOException {
            JsonToken t = in.peek();
            if (t == JsonToken.NULL) { in.nextNull(); return ""; }
            if (t == JsonToken.NUMBER) return in.nextString();
            if (t == JsonToken.STRING) return in.nextString();
            in.skipValue();
            return "";
        }
    }

    private static String[] toFixedValues(String[] values, int fixedSize) {
        String[] out = new String[fixedSize];
        for (int i = 0; i < fixedSize; i++) out[i] = DEFAULT_VALUE;
        if (values == null) return out;

        int n = Math.min(values.length, fixedSize);
        for (int i = 0; i < n; i++) {
            String v = values[i];
            if (v == null) {
                out[i] = DEFAULT_VALUE;
            } else if ("NA".equalsIgnoreCase(v)) {
                out[i] = DEFAULT_VALUE;
            } else {
                out[i] = v;
            }
        }
        return out;
    }
    @Override
    public void sendVbmJson(String jsonPayload) throws RemoteException {
        MainService service = mServiceRef.get();
        if (service == null || TextUtils.isEmpty(jsonPayload)) return;

        try {
            // 唯一一次解析：fromJson
            VbmRecord r = VBM_GSON.fromJson(jsonPayload, VbmRecord.class);
            if (r == null || TextUtils.isEmpty(r.agentId)) return;

            String agentId = r.agentId;
            String eventType = TextUtils.isEmpty(r.eventType) ? "0" : r.eventType;
            String timestamp = TextUtils.isEmpty(r.timestamp)
                    ? String.valueOf(System.currentTimeMillis())
                    : r.timestamp;

            String[] fixed = toFixedValues(r.values, 10);
            service.addRecord(agentId, eventType, timestamp, fixed);

        } catch (JsonSyntaxException e) {
            // payload 非 record-only 或格式壞掉：直接丟棄並記 log
            Log.e(TAG, "Invalid VBM JSON payload, drop. payload=" + jsonPayload, e);
        } catch (Exception e) {
            Log.e(TAG, "sendVbmJson failed", e);
        }
    }
}