package com.prime.dmg.launcher.Home.LiveTV.TvPackage;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.prime.dmg.launcher.BaseActivity;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Utils.URLUtils;
import com.prime.dtv.utils.TVMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PurchaseActivity extends BaseActivity {

    String TAG = getClass().getSimpleName();

    public static final String KEY_PACKAGE_ID = "KEY_PACKAGE_ID";
    public static final String KEY_PACKAGE_NAME = "KEY_PACKAGE_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        Intent intent = getIntent();
        String packageId = intent.getStringExtra(KEY_PACKAGE_ID);
        String packageName = intent.getStringExtra(KEY_PACKAGE_NAME);

        TextView purchaseText = findViewById(R.id.lo_package_purchase);
        purchaseText.setText(String.format(getString(R.string.package_hint), packageName));

        draw_qrcode(packageId);
    }

    @Override
    public void onBroadcastMessage(Context context, Intent intent) {
        super.onBroadcastMessage(context, intent);
    }

    @Override
    public void onMessage(TVMessage msg) {
        super.onMessage(msg);
    }

    void draw_qrcode(String packageId) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            String packageUrl = URLUtils.generate_package_url(this, packageId);
            Bitmap qrCodeBitmap = URLUtils.generate_qr_code(packageUrl, 600);
            Log.d(TAG, "draw_qrcode: packageUrl = " + packageUrl);

            runOnUiThread(() -> {
                ImageView QRCodeView = findViewById(R.id.lo_package_qr_code);
                QRCodeView.setImageBitmap(qrCodeBitmap);
                /*Glide.with(PurchaseActivity.this)
                        .load(qrCodeBitmap)
                        .centerCrop()
                        .into(QRCodeView);*/
            });
        });
    }
}
