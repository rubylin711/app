package com.prime.launcher.member;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.prime.launcher.BaseActivity;
import com.prime.launcher.R;
import com.prime.launcher.Utils.URLUtils;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.utils.TVMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MemberActivity extends BaseActivity {
    private final static String TAG = "MemberActivity";

    private ImageView g_qrcode, g_icon;
    private TextView g_item_hint, g_label;
    private LinearLayout g_account_item_layer, g_time_hint_layer;
    private ConstraintLayout g_root_layer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);

        init();
    }

    @Override
    public void onBroadcastMessage(Context context, Intent intent) {
        super.onBroadcastMessage(context, intent);
    }

    @Override
    public void onMessage(TVMessage msg) {
        super.onMessage(msg);
    }

    private void init() {
        g_qrcode = findViewById(R.id.lo_member_qrcode);
        g_item_hint = findViewById(R.id.lo_member_item_hint);
        g_icon = findViewById(R.id.lo_member_icon);
        g_label = findViewById(R.id.lo_member_label);

        g_root_layer = findViewById(R.id.lo_member_Layer);
        g_time_hint_layer = findViewById(R.id.lo_member_time_hint_layer);
        //g_account_item_layer = findViewById(R.id.lo_member_account_item_layer);

        /*g_account_item_layer.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                g_icon.setBackgroundResource(R.drawable.button_member_focus);
                g_label.setTextColor(Color.WHITE);
            }
            else {
                g_icon.setBackgroundResource(R.drawable.button_member_rest);
                g_label.setTextColor(Color.GRAY);
            }
        });*/

        String memberHint = getString(R.string.member_hint);
        memberHint = String.format(memberHint, getString(R.string.member_sub_payment), getString(R.string.member_sub_payment));
        g_item_hint.setText(memberHint);

        if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG)) {
            g_root_layer.setBackgroundResource(R.drawable.member_wallpaper_dmg);
            g_time_hint_layer.setVisibility(View.GONE);
            g_item_hint.append(getString(R.string.dmg_service_number));
        }
        else {
            g_root_layer.setBackgroundResource(R.drawable.member_wallpaper);
            g_item_hint.append(getString(R.string.tbc_service_number));
        }

        draw_qrcode();
    }

    private void draw_qrcode() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            String paymentUrl;
            if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG))
                paymentUrl = URLUtils.generate_payment_url(this);
            else
                paymentUrl = URLUtils.generate_payment_url(this, "payment");
            Bitmap qrCodeBitmap = URLUtils.generate_qr_code(paymentUrl, 600);
            Log.d(TAG, "draw_qrcode: paymentUrl = " + paymentUrl);

            runOnUiThread(() -> {
                g_qrcode.setImageBitmap(qrCodeBitmap);
            });
        });
    }
}
