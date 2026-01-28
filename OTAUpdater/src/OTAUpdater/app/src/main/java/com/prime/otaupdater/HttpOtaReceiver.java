package com.prime.otaupdater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.HashMap;

public class HttpOtaReceiver extends BroadcastReceiver {
    String TAG = getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        switch (action) {
            case MainActivity.ACTION_HTTP_OTA_UPDATE:
                start_MainActivity(context, intent);
                break;
        }
    }

    void start_MainActivity(Context context, Intent intent) {
        int updateMode = intent.getIntExtra(MainActivity.EXTRA_UPDATE_MODE, 0);

        if (updateMode == MainActivity.ABUPDATE_STREAM_PAYLOAD_MODE)
            update_by_payload(context, intent);
        else if (updateMode == MainActivity.ABUPDATE_STREAM_ZIP_MODE)
            update_by_zip(context, intent);
        else Log.e(TAG, "start_MainActivity: updateMode not match stream_payload or stream_zip");
    }

    private void update_by_payload(Context context, Intent intent) {
        String url_payload_bin   = intent.getStringExtra(MainActivity.EXTRA_HTTP_PAYLOAD_BIN);
        String url_payload_prop  = intent.getStringExtra(MainActivity.EXTRA_HTTP_PAYLOAD_PROP);
        String url_metadata      = intent.getStringExtra(MainActivity.EXTRA_HTTP_METADATA);
        String image_version     = intent.getStringExtra(MainActivity.ABUPDATE_BROADCAST_UPDATE_IMAGE_VERSION);
        boolean countdown_update = intent.getBooleanExtra(MainActivity.EXTRA_HTTP_FORCE, false);
        String caller            = intent.getStringExtra(MainActivity.EXTRA_CALLER);
        int updateMode           = intent.getIntExtra(MainActivity.EXTRA_UPDATE_MODE, 0);

        Log.d(TAG, "start_MainActivity: url_payload_bin  = " + url_payload_bin);
        Log.d(TAG, "start_MainActivity: url_payload_prop = " + url_payload_prop);
        Log.d(TAG, "start_MainActivity: url_metadata     = " + url_metadata);
        Log.d(TAG, "start_MainActivity: image_version = " + image_version);
        Log.d(TAG, "start_MainActivity: countdown_update = " + countdown_update);
        Log.d(TAG, "start_MainActivity: caller = " + caller);
        Log.d(TAG, "start_MainActivity: updateMode = " + updateMode);

        // check SetupWraith
        if (!MountReceiver.setupCompleted(TAG, context)) {
            Log.e(TAG, "[Error] SetupWraith is not completed");
            return;
        }

        // check URL
        if ((url_payload_bin == null) || (url_payload_prop == null) || (url_metadata == null)) {
            Log.e(TAG, "[Error] file not found");
            return;
        }

        // check timestamp
        new Thread(() -> {
            HashMap<String, String> metaMap = MainActivity.readMetadata(TAG, url_metadata);

            // "countdown_update" for ignore timestamp check
            if (MainActivity.timestampOK(TAG, metaMap) || countdown_update) {
                // start MainActivity
                Log.d(TAG, "start_MainActivity: start MainActivity");
                Intent i = new Intent(Intent.ACTION_RUN);
                i.setClass(context, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                i.putExtra(MainActivity.EXTRA_PAYLOAD_BIN,  url_payload_bin);
                i.putExtra(MainActivity.EXTRA_PAYLOAD_PROP, url_payload_prop);
                i.putExtra(MainActivity.EXTRA_METADATA,     url_metadata);
                i.putExtra(MainActivity.EXTRA_IMAGE_VERSION,     image_version);
                i.putExtra(MainActivity.EXTRA_FORCE,        countdown_update);
                i.putExtra(MainActivity.EXTRA_IS_HTTP_OTA,  true);
                i.putExtra(MainActivity.EXTRA_CALLER, caller);
                i.putExtra(MainActivity.EXTRA_UPDATE_MODE, updateMode);
                context.startActivity(i);
            }
            else
                Log.w(TAG, "start_MainActivity: timestamp not OK");

        }).start();
    }

    private void update_by_zip(Context context, Intent intent) {
        String url_zip_file = intent.getStringExtra(MainActivity.EXTRA_UPDATE_ZIP_URL);
        long offset_zip_file = intent.getLongExtra(MainActivity.EXTRA_UPDATE_ZIP_OFFSET, 0);
        long size_zip_file = intent.getLongExtra(MainActivity.EXTRA_UPDATE_ZIP_SIZE, 0);
        String [] string_array_properties = intent.getStringArrayExtra(MainActivity.EXTRA_UPDATE_PROPERTIES);
        String image_version     = intent.getStringExtra(MainActivity.ABUPDATE_BROADCAST_UPDATE_IMAGE_VERSION);
        boolean countdown_update = intent.getBooleanExtra(MainActivity.EXTRA_HTTP_FORCE, false);
        String caller            = intent.getStringExtra(MainActivity.EXTRA_CALLER);
        int updateMode           = intent.getIntExtra(MainActivity.EXTRA_UPDATE_MODE, 0);

        Log.d(TAG, "start_MainActivity: url_zip_file  = " + url_zip_file);
        Log.d(TAG, "start_MainActivity: offset_zip_file = " + offset_zip_file);
        Log.d(TAG, "start_MainActivity: size_zip_file = " + size_zip_file);
        assert string_array_properties != null;
        Log.d(TAG, "start_MainActivity: string_array_properties = " + string_array_properties.toString());
        Log.d(TAG, "start_MainActivity: image_version = " + image_version);
        Log.d(TAG, "start_MainActivity: countdown_update = " + countdown_update);
        Log.d(TAG, "start_MainActivity: caller = " + caller);
        Log.d(TAG, "start_MainActivity: updateMode = " + updateMode);

        if ((url_zip_file == null)) {
            Log.e(TAG, "[Error] file not found");
            return;
        }

        Log.d(TAG, "start_MainActivity: start MainActivity");
        Intent i = new Intent(Intent.ACTION_RUN);
        i.setClass(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra(MainActivity.EXTRA_UPDATE_ZIP_URL,  url_zip_file);
        i.putExtra(MainActivity.EXTRA_UPDATE_ZIP_OFFSET, offset_zip_file);
        i.putExtra(MainActivity.EXTRA_UPDATE_ZIP_SIZE, size_zip_file);
        i.putExtra(MainActivity.EXTRA_UPDATE_PROPERTIES, string_array_properties);
        i.putExtra(MainActivity.EXTRA_IMAGE_VERSION,     image_version);
        i.putExtra(MainActivity.EXTRA_FORCE,        countdown_update);
        i.putExtra(MainActivity.EXTRA_IS_HTTP_OTA,  true);
        i.putExtra(MainActivity.EXTRA_CALLER, caller);
        i.putExtra(MainActivity.EXTRA_UPDATE_MODE, updateMode);
        context.startActivity(i);
    }
}
