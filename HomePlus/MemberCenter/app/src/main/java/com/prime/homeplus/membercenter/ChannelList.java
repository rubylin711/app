package com.prime.homeplus.membercenter;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;
import java.io.FileOutputStream;

public class ChannelList extends Activity {
    String TAG = "HomePlus-ChannelList";
    private ImageView ivImage;
    private String DEFAULT_MC_DATA_URL_PREFIX = "https://cnsatv.totaltv.com.tw:8093";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_channel_list);

        ivImage = (ImageView) findViewById(R.id.ivImage);

        StringBuilder stringBuilder = new StringBuilder();
        // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
        int cmMode = Integer.parseInt(Utils.getSoId(true));
//        Log.d(TAG, "cmMode: " + cmMode + " so:" + Utils.getSoId(true));

        if (cmMode == 0) {
            // TODO(STB_Vendor): Implement the custom feature for STB_Vendor
            String filePath = Settings.System.getString(getContentResolver(), "channel_data_fale_path");
//            Log.d(TAG, "Raw filePath: " + filePath);
            if (!TextUtils.isEmpty(filePath)) {
                try {
                    File file = new File(filePath);
                    String canonicalPath = file.getCanonicalPath();
//                    Log.d(TAG, "Canonical path: " + canonicalPath);

                    if (file.exists()) {
                        Log.d(TAG, "File exists: true, Can read: " + file.canRead() + ", Size: " + file.length());
                        ivImage.setVisibility(android.view.View.VISIBLE);
                        Glide.with((Activity) this).load(file).diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true).into(ivImage);
                    } else {
                        Log.e(TAG, "File does NOT exist at path: " + canonicalPath);
                        ivImage.setVisibility(android.view.View.GONE);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing file path: " + e.getMessage());
                    e.printStackTrace();
                    ivImage.setVisibility(android.view.View.GONE);
                }
            } else {
                ivImage.setVisibility(android.view.View.GONE);
            }
        } else {
            String serverPrefix = DEFAULT_MC_DATA_URL_PREFIX;
            stringBuilder.append(serverPrefix + "/channel-list/");
            String mSO = Utils.getSoId(true);
            stringBuilder.append(mSO);
            stringBuilder.append("_channellist.jpg");
            Log.d(TAG, "url: " + stringBuilder.toString());
            Glide.with((Activity) this).load(stringBuilder.toString()).diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).into(ivImage);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}