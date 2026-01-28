package com.prime.dmg.launcher.Mail;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Utils.URLUtils;
import com.prime.dtv.sysdata.MailInfo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MailDetailView extends RelativeLayout {
    private static final String TAG = "MailDetailView";

    //ui
    private ImageView g_imgv_ad;
    private TextView g_textv_content;
    private View g_view_last_item;
    private RelativeLayout g_rltvl_mail_content_image;
    private LinearLayout g_lnrl_mail_content_layer;
    private ImageView g_imgv_qrcode;
    private ScrollView g_sclv_content;
    private TextView g_textv_time;
    private TextView g_textv_title;
    private RelativeLayout g_rltvl_title_layer;

    private MailManager g_mailManager;

    public MailDetailView(Context context) {
        super(context);
        init();
    }

    public MailDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MailDetailView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_mail_detail, this);
        g_mailManager = MailManager.GetInstance((AppCompatActivity) getContext());

        //mQrCodeHelper = new QrCodeHelper(getContext());
        g_rltvl_title_layer = findViewById(R.id.lo_mail_detail_rltvl_title_layer);
        g_lnrl_mail_content_layer = findViewById(R.id.lo_mail_detail_lnrl_content_layer);
        g_rltvl_mail_content_image = findViewById(R.id.lo_mail_detail_rltvl_content_image);
        g_imgv_ad = findViewById(R.id.lo_mail_detail_imgv_photo);
        g_imgv_qrcode = findViewById(R.id.lo_mail_detail_imgv_qrcode);
        g_sclv_content = findViewById(R.id.lo_mail_detail_sclv_content);
        g_sclv_content.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    if (g_view_last_item == null) {
                        return true;
                    }
                    g_view_last_item.requestFocus();
                    return true;
                }
                return false;
            }
        });
        g_textv_content = findViewById(R.id.lo_mail_detail_textv_content);
        g_textv_title = findViewById(R.id.lo_mail_detail_textv_title);
        g_textv_time =findViewById(R.id.lo_mail_detail_textv_time);
    }

    private AppCompatActivity get() {
        return (AppCompatActivity) getContext();
    }

    public void set_content(Mail mail) {
        post(new Runnable() {
            @Override
            public void run() {
                //MailInfo mailInfo = get_mail_info(mailId);
                if (mail == null) {
                    return;
                }

                if (TextUtils.isEmpty(mail.getContent_icon())) {
                    Log.d(TAG, "getContent_icon empty");
                    g_rltvl_mail_content_image.setVisibility(View.GONE);
                    g_imgv_ad.setVisibility(View.GONE);
                    update_scroll_content_position(false);
                } else {
                    Log.d(TAG, "getContent_icon not empty");
                    g_rltvl_mail_content_image.setVisibility(View.VISIBLE);
                    g_imgv_ad.setVisibility(View.VISIBLE);
                    update_scroll_content_position(true);
                    if (!get().isFinishing() && !get().isDestroyed())
                        Glide.with(get())
                                .load(mail.getContent_icon())
                                .placeholder((int) R.drawable.default_photo)
                                .error(R.drawable.internet_error)
                                .apply((BaseRequestOptions<?>) new RequestOptions().override(g_imgv_ad.getWidth(), g_imgv_ad.getHeight()).fitCenter())
                                .into(g_imgv_ad);
                }
                if (TextUtils.isEmpty(mail.getQrcode())) {
                    Log.d(TAG, "getQrcode empty");
                    g_imgv_qrcode.setVisibility(View.GONE);
                } else {
                    Log.d(TAG, "getQrcode empty not empty");
                    generate_qrcode(mail.getQrcode());
                    g_imgv_qrcode.setVisibility(View.VISIBLE);
                }
                g_textv_title.setText(mail.getTitle());
                g_textv_time.setText(mail.getUpdated_time());
                g_textv_content.setText(mail.getContent());
            }
        });
    }

    public void update_title_layer_background(boolean status) {
        if (status) {
            g_rltvl_title_layer.setVisibility(View.VISIBLE);
            g_lnrl_mail_content_layer.setVisibility(View.VISIBLE);
            return;
        }
        g_rltvl_title_layer.setVisibility(View.GONE);
        g_lnrl_mail_content_layer.setVisibility(View.GONE);
    }

    public void update_scroll_content_position(boolean status) {
        Log.d(TAG, "update_scroll_content_position status = " + status);
        RelativeLayout.LayoutParams sclvLayoutParams = (RelativeLayout.LayoutParams) g_sclv_content.getLayoutParams();
        if (status) {
            sclvLayoutParams.topMargin = getContext().getResources().getDimensionPixelSize(R.dimen.lo_mail_detail_sclv_content_margintop);
        } else {
            sclvLayoutParams.topMargin = getContext().getResources().getDimensionPixelSize(R.dimen.lo_mail_detail_sclv_content_margintop_no_ad);
        }
        sclvLayoutParams.bottomMargin = getContext().getResources().getDimensionPixelSize(R.dimen.lo_mail_detail_sclv_content_marginbottom);;
        g_sclv_content.setLayoutParams(sclvLayoutParams);

        RelativeLayout.LayoutParams qrcodeLayoutParams2 = (RelativeLayout.LayoutParams) g_imgv_qrcode.getLayoutParams();
        if (status) {
            qrcodeLayoutParams2.topMargin = getContext().getResources().getDimensionPixelSize(R.dimen.lo_mail_detail_sclv_content_margintop);;
        } else {
            qrcodeLayoutParams2.topMargin = sclvLayoutParams.topMargin = getContext().getResources().getDimensionPixelSize(R.dimen.lo_mail_detail_sclv_content_margintop_no_ad);
        }
        g_imgv_qrcode.setLayoutParams(qrcodeLayoutParams2);
    }

    public void set_last_item_view(View view) {
        this.g_view_last_item = view;
    }

    public void generate_qrcode(final String uri) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            String qrcodeUrl = URLUtils.generate_url(getContext(), uri);

            Bitmap qrCodeBitmap = URLUtils.generate_qr_code(qrcodeUrl, 600);
            Log.d(TAG, "draw_qrcode: packageUrl = " + qrcodeUrl);

            ((MailActivity) getContext()).runOnUiThread(() -> {
                ImageView QRCodeView = findViewById(R.id.lo_mail_detail_imgv_qrcode);
                if (!get().isFinishing() && !get().isDestroyed())
                    Glide.with(get())
                            .load(qrCodeBitmap)
                            .centerCrop()
                            .into(QRCodeView);
            });
        });
    }

    private MailInfo get_mail_info(int mailId) {
        MailInfo mailInfo = new MailInfo();
        mailInfo.set_dialog_photo_uri("http://10.1.4.157/weather_hint_p1.png");
        mailInfo.set_title("語音助理 " + mailId);
        mailInfo.set_message("測試語音助理 \n 123123132 \n 123123132 \n 123123123 \n 123132132 \n " +mailId);
        mailInfo.set_update_timestamp(12345678);
        mailInfo.set_internal_flag_1("1");
        return mailInfo;
    }
}
