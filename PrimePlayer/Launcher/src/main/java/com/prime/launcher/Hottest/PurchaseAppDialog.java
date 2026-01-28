package com.prime.launcher.Hottest;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.prime.launcher.R;
import com.prime.launcher.Utils.ActivityUtils;
import com.prime.launcher.Utils.JsonParser.AppPackage;
import com.prime.launcher.Utils.URLUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** @noinspection CommentedOutCode*/
public class PurchaseAppDialog extends Dialog {

    String TAG = getClass().getSimpleName();

    WeakReference<AppCompatActivity> g_ref;
    AppPackage g_appPackage;

    public PurchaseAppDialog(AppCompatActivity activity, AppPackage appPackage) {
        super(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        g_ref = new WeakReference<>(activity);
        g_appPackage = appPackage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.color.trans);
            //getWindow().setWindowAnimations(R.style.Theme_Launcher_DialogAnimation);
            //getWindow().setDimAmount(0);
        }
        setContentView(R.layout.dialog_purchase_app);
        set_app_banner();
        set_plan_hint();
        set_qrcode_hint();
        set_qrcode_view();
    }

    public HottestActivity get() {
        return (HottestActivity) g_ref.get();
    }

    public void set_app_banner() {
        ImageView bannerView = findViewById(R.id.lo_purchase_app_banner);
        String pkgName = g_appPackage.get_package_name();

        if (TextUtils.isEmpty(pkgName)) {
            bannerView.setVisibility(View.GONE);
            return;
        }

        Drawable bannerDrawable = ActivityUtils.get_app_icon(get(), pkgName, true);

        if (!get().isFinishing() && !get().isDestroyed())
            Glide.with(get()).load(null == bannerDrawable ? g_appPackage.get_icon_url() : bannerDrawable)
                    .error(R.drawable.internet_error)
                    .placeholder(R.drawable.default_photo)
                    .fitCenter()
                    .into(bannerView);
    }

    public void set_plan_hint() {
        TextView planHintView = findViewById(R.id.lo_purchase_app_plan_hint);
        planHintView.setText(g_appPackage.get_description());
    }

    public void set_qrcode_hint() {
        TextView qrcodeHint = findViewById(R.id.lo_purchase_app_qrcode_hint);
        qrcodeHint.setText(g_appPackage.get_qrcode_hint());
    }

    public void set_qrcode_view() {
        ImageView qrcodeView = findViewById(R.id.lo_purchase_app_qrcode_view);
        int qrcodeSize = get().getResources().getDimensionPixelSize(R.dimen.purchase_app_QrCodeView_160dp);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            String ottUrl = URLUtils.generate_ott_url(get(), g_appPackage.get_cp_code());
            Bitmap qrcodeBitmap = URLUtils.generate_qr_code(ottUrl, qrcodeSize);
            get().runOnUiThread(() -> {
                Log.d(TAG, "set_qrcode_view: [ott url] " + ottUrl);
                qrcodeView.setImageBitmap(qrcodeBitmap);
            });
        });
    }
/*
    private void setContent(ContentBean data) {
        this.mAppBanner.setVisibility(0);
        if (TextUtils.isEmpty(data.getPackageName())) {
            this.mAppBanner.setVisibility(8);
        } else {
            Drawable appIcon = UtilsApp.getAppIcon(this, data.getPackageName(), true);
            if (appIcon != null) {
                this.mAppBanner.setImageDrawable(appIcon);
            } else {
                Glide.with((FragmentActivity) this)
                        .mo869load(data.getBanner())
                        .placeholder((int) C1622R.mipmap.default_photo
                                .error(C1622R.mipmap.internet_error)
                                .mo793apply((BaseRequestOptions<?>) new RequestOptions()
                                        .override(this.mAppBanner.getWidth(), this.mAppBanner.getHeight())
                                        .fitCenter())
                                .into(this.mAppBanner);
            }
        }
        this.mPlanHint.setText(data.getPlanHint());
        this.mQrCodeHint.setText(data.getQrCodeHint());
        generateQRCode(data.getCpId());
    }

    private void generateQRCode(String contentUrl) {
        try {
            final Bitmap encodeAsBitmap = QrCodeHelper.encodeAsBitmap(this, contentUrl, BarcodeFormat.QR_CODE, 320, 320, 30);
            runOnUiThread(new Runnable() { // from class: com.vasott.tbc.hybrid.HottestQRcodeActivity.1
                @Override // java.lang.Runnable
                public void run() {
                    if (encodeAsBitmap != null) {
                        HottestQRcodeActivity.this.mQrCodeView.setImageBitmap(encodeAsBitmap);
                    } else {
                        HottestQRcodeActivity.this.mQrCodeView.setBackgroundResource(C1622R.mipmap.default_photo);
                    }
                }
            });
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }*/
}
