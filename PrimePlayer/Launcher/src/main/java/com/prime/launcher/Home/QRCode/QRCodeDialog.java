package com.prime.launcher.Home.QRCode;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.prime.launcher.ACSDatabase.ACSDataProviderHelper;
import com.prime.launcher.HomeActivity;
import com.prime.launcher.Mail.MailDetail;
import com.prime.launcher.BaseDialog;
import com.prime.launcher.Utils.JsonParser.AdPageItem;
import com.prime.launcher.R;
import com.prime.datastructure.sysdata.MusicAdInfo;
import com.prime.datastructure.utils.TVMessage;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QRCodeDialog extends BaseDialog {

    String TAG = getClass().getSimpleName();

    static final int QR_CODE_SIZE = 200;

    WeakReference<AppCompatActivity> g_ref;
    AdPageItem g_pageItem;
    MusicAdInfo g_musicAdInfo = null;
    MailDetail g_mailDetail = null;
    ExecutorService g_executor;

    public QRCodeDialog(AppCompatActivity activity, AdPageItem pageItem) {
        super(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        g_ref = new WeakReference<>(activity);
        g_pageItem = pageItem;
        g_executor = Executors.newSingleThreadExecutor();
    }

    public QRCodeDialog(AppCompatActivity activity, MusicAdInfo musicAdInfo) {
        super(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        g_ref = new WeakReference<>(activity);
        g_musicAdInfo = musicAdInfo;
        g_executor = Executors.newSingleThreadExecutor();
    }

    public QRCodeDialog(AppCompatActivity activity, MailDetail mailDetail) {
        super(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        g_ref = new WeakReference<>(activity);
        g_mailDetail = mailDetail;
        g_executor = Executors.newSingleThreadExecutor();
        ((HomeActivity)get()).g_liveTvMgr.set_mail_is_running(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.color.trans);
            getWindow().setWindowAnimations(R.style.Theme_Launcher_DialogAnimation);
            getWindow().setDimAmount(0);
        }
        setContentView(R.layout.dialog_qr_code);

        ImageView QRCodeView = findViewById(R.id.lo_qr_code_dialog_image);
        draw_qrcode(QRCodeView, get_qrcode_url());
    }

    @Override
    public void onMessage(TVMessage msg) {

    }

    public AppCompatActivity get() {
        return (AppCompatActivity) g_ref.get();
    }

    String get_qrcode_url() {
        // BAT_ID => ACS Server
        // STB_SC_ID => ro.serialno
        // STB_CA_SN => ro.boot.cstmsnno
        String STB_SC_ID = Build.getSerial(); //SystemProperties.get("ro.serialno");
        String STB_CA_SN = SystemProperties.get("ro.boot.cstmsnno");
        String BAT_ID = ACSDataProviderHelper.get_acs_provider_data(get(), "bat_id");

        Log.d(TAG, "get_qrcode_url: STB_SC_ID = " + STB_SC_ID);
        Log.d(TAG, "get_qrcode_url: STB_CA_SN = " + STB_CA_SN);
        Log.d(TAG, "get_qrcode_url: BAT_ID = " + BAT_ID);

        if (BAT_ID == null)
            BAT_ID = "0";

        if (g_musicAdInfo != null)
            return g_musicAdInfo.get_url()
                .replace("{STB_SC_ID}", STB_CA_SN)
                .replace("{STB_CA_SN}", STB_CA_SN)
                .replace("{BAT_ID}", BAT_ID);
        else if (g_mailDetail != null)
            return g_mailDetail.getUrl()
                    .replace("{STB_SC_ID}", STB_CA_SN)
                    .replace("{STB_CA_SN}", STB_CA_SN)
                    .replace("{BAT_ID}", BAT_ID);
        else
            return g_pageItem.get_url()
                .replace("{STB_SC_ID}", STB_CA_SN)
                .replace("{STB_CA_SN}", STB_CA_SN)
                .replace("{BAT_ID}", BAT_ID);
    }

    Bitmap create_qrcode_bitmap(String QR_CODE_URL) {
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        Bitmap QR_Code_Bitmap = Bitmap.createBitmap(QR_CODE_SIZE, QR_CODE_SIZE, Bitmap.Config.RGB_565);

        try {
            // method 1
            if (false) {
                BitMatrix bitMatrix = barcodeEncoder.encode(QR_CODE_URL, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);
                for (int x = 0; x < QR_CODE_SIZE; x++) {
                    for (int y = 0; y < QR_CODE_SIZE; y++) {
                        QR_Code_Bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                    }
                }
            }
            // method 2
            else {
                QR_Code_Bitmap = barcodeEncoder.encodeBitmap(QR_CODE_URL, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return QR_Code_Bitmap;
    }

    void draw_qrcode(ImageView QRCodeView, String QR_CODE_URL) {
        Log.d(TAG, "draw_qrcode: QR_CODE_URL = " + QR_CODE_URL);
        g_executor.execute(() -> {
            Bitmap bitmap = create_qrcode_bitmap(QR_CODE_URL);
            get().runOnUiThread(() -> {
                //QRCodeView.setImageBitmap(bitmap);
                if (!get().isFinishing() && !get().isDestroyed())
                    Glide.with(get())
                            .load(bitmap)
                            .into(QRCodeView);
            });
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, "onKeyDown: BACK");
            if (get() instanceof HomeActivity)
                ((HomeActivity)get()).g_liveTvMgr.set_mail_is_running(false);
        }

        return super.onKeyDown(keyCode, event);
    }
}
