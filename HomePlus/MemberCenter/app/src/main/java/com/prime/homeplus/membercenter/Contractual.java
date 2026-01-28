package com.prime.homeplus.membercenter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class Contractual extends Activity {
    String TAG = "HomePlus-Contractual";
    private TextView tvTitle;
    private ImageView ivImage;
    private String DEFAULT_MC_DATA_URL_PREFIX = "https://cnsatv.totaltv.com.tw:8093";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_channel_list);

        tvTitle = (TextView) findViewById(R.id.tvTitle);
        ivImage = (ImageView) findViewById(R.id.ivImage);

        tvTitle.setText(getString(R.string.subscription_onlinepayment_contract));

        StringBuilder stringBuilder = new StringBuilder();
        String serverPrefix = DEFAULT_MC_DATA_URL_PREFIX;
        stringBuilder.append(serverPrefix + "/contract/EPG-SO");
        String mSO = Utils.getSoId(true);
        stringBuilder.append(mSO);
        stringBuilder.append("_contract.jpg");
        Log.d(TAG, "stringBuilder"+stringBuilder.toString());
        Glide.with((Activity) this).load(stringBuilder.toString()).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(ivImage);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}