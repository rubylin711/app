package com.prime.launcher.Mail;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.prime.launcher.ACSDatabase.ACSDataProviderHelper;
import com.prime.launcher.Utils.Utils;
import com.prime.launcher.HomeActivity;
import com.prime.launcher.R;
import com.prime.launcher.BaseDialog;
import com.prime.datastructure.utils.TVMessage;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MailDialog extends BaseDialog {
    private static final String TAG = "MailDialog";

    public static final int EVENT_CLOSE = 7;

    int QR_CODE_SIZE = 400;
    private WeakReference<Context> g_ref;
    private ExecutorService g_executor;
    private List<MailDetail> g_mail_detail_list;
    private Mail g_mail;
    private MailManager g_mailManager;

    //ui
    private ImageView g_ad_image, g_qr_code;
    private TextView g_title, g_time, g_content;
    private LinearLayout g_dialog_button_content;
    private ConstraintLayout g_dialog_content_image;
    private ScrollView g_scroll_content;

    private View.OnClickListener g_event_click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int tagPosition = Utils.get_tag_position((String) v.getTag());

            if (tagPosition == EVENT_CLOSE) {
                ((HomeActivity) get_context()).g_liveTvMgr.set_mail_is_running(false);
                dismiss();
                return;
            }

            int type = g_mail_detail_list.get(tagPosition).getEvent_type();
            String value = g_mail_detail_list.get(tagPosition).getEvent_value();
            Log.d(TAG, "onClick: index= " + tagPosition + " type = " + type + " value = " + value);
            if (tagPosition < 0 ||  value.isEmpty() || value.equalsIgnoreCase("null") || g_mail_detail_list.size() == 0) {
                return;
            }

            if (g_mailManager == null) {
                Log.e(TAG, "onClick: g_mailManager == null");
                return;
            }

            MailDetail mailDetail = g_mail_detail_list.get(tagPosition);
            ((HomeActivity) get_context()).g_liveTvMgr.set_mail_is_running(false);
            g_mailManager.start_mail_dialog_event(g_mail, mailDetail);
            dismiss();
        }
    };

    public MailDialog(Context context) {
        super(context, R.style.Theme_Launcher_MailDialog);
        g_ref = new WeakReference<>(context);
        g_executor = Executors.newSingleThreadExecutor();
        g_mailManager = MailManager.GetInstance((AppCompatActivity) context);
        ((HomeActivity) get_context()).g_liveTvMgr.set_mail_is_running(true);
        init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onMessage(TVMessage msg) {

    }

    private void init() {
        setCancelable(true);
        setContentView(R.layout.view_mail_dialog);
        g_ad_image = findViewById(R.id.lo_mail_dialog_photo);
        g_qr_code = findViewById(R.id.lo_mail_dialog_qrcode);
        g_title = findViewById(R.id.lo_mail_dialog_title);
        g_time = findViewById(R.id.lo_mail_dialog_time);
        g_content = findViewById(R.id.lo_mail_dialog_content);
        g_dialog_button_content = findViewById(R.id.lo_mail_dialog_button_content);
        g_dialog_content_image = findViewById(R.id.lo_mail_dialog_content_image);
        g_scroll_content = findViewById(R.id.lo_mail_dialog_scroll_content);
    }

    private AppCompatActivity get() {
        return (AppCompatActivity) get_context();
    }

    private Context get_context() {
        return g_ref.get();
    }

    public void set_content(Mail mail) {
        if (mail == null)
             return;

        g_mail = mail;
        if (mail.getContent_icon().isEmpty() && mail.getQrcode().isEmpty()) {
            g_dialog_content_image.setVisibility(View.GONE);
            update_scroll_content_position(false);
        }
        else {
            g_dialog_content_image.setVisibility(View.VISIBLE);
            update_scroll_content_position(true);
            if (mail.getContent_icon().isEmpty())
                g_ad_image.setVisibility(View.GONE);
            else {
                g_ad_image.setVisibility(View.VISIBLE);
                if (!get().isFinishing() && !get().isDestroyed())
                    Glide.with(get())
                            .load(mail.getContent_icon())
                            .placeholder(R.drawable.default_photo)
                            .error(R.drawable.internet_error)
                            .apply((BaseRequestOptions<?>) new RequestOptions().override(g_ad_image.getWidth(), g_ad_image.getHeight()).fitCenter())
                            .into(g_ad_image);
            }

            if (mail.getQrcode().isEmpty())
                g_qr_code.setVisibility(View.GONE);
            else {
                g_qr_code.setVisibility(View.VISIBLE);
                draw_qrcode(g_qr_code, get_qrcode_url(mail.getQrcode()));
            }
        }
        /*
        update_scroll_content_position(true);
        g_ad_image.setVisibility(View.VISIBLE);
        Glide.with(MailDialog.this.getContext())
                .load("https://epgstore.tbc.net.tw/acs-api//uploads/icon/1676883894708.jpg")
                .placeholder(R.drawable.default_photo)
                .error(R.drawable.internet_error)
                .apply((BaseRequestOptions<?>) new RequestOptions().override(g_ad_image.getWidth(), g_ad_image.getHeight()).fitCenter())
                .into(g_ad_image);
        g_qr_code.setVisibility(View.VISIBLE);
        draw_qrcode(g_qr_code, get_qrcode_url("https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/hbogo"));
        g_title.setText("國家防災日演練預先宣導-正式演練");
        g_time.setText("02/04 12:05 a");
        g_content.setText("社群粉絲多達百萬名的三津家貴也教練解釋，「放鬆跑」是一種介於走路與跑步之間的運動方式，能讓人輕鬆不費力、持續地養成運動習慣，自然而然瘦下來，許多35至45歲的女性，經過育兒階段後開始有時間能夠專注自己的體態，很多人都利用「放鬆跑」順利在幾個月內瘦下5到10公斤！\n" + "\n" + "「放鬆跑」運動的關鍵在於運用臀部和大腿後側的肌肉，這些部位是腿部中最大的肌肉群，善用這些肌群、運用正確的跑步姿勢，不僅可以減少疲勞感，運動不容易疲累，還能有效預防受傷。日本奧運跑步教練青山剛就形容，臀部肌群像是跑步的「加速器」與「穩定器」，掌握這些肌肉的運用，能大幅減少腿部負擔，降低受傷風險。\n" + "\n" + "有別於大家對於「跑步傷膝蓋」的印象，其實適量、動作正確的跑步不但不會傷害到膝關節，反而能預防退化，有研究也顯示，跑步的好處除了瘦身外，還包含降血脂、增加好膽固醇、減低心臟病及癌症死亡率。");*/
        g_title.setText(g_mail.getTitle());
        g_content.setText(g_mail.getContent());
        g_time.setText(g_mail.getUpdated_time());
        set_button_detail(g_mail.get_mail_detail_dialog());
    }

    private void update_scroll_content_position(boolean status) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) g_scroll_content.getLayoutParams();
        if (status) {
            layoutParams.topMargin = get_context().getResources().getDimensionPixelSize(R.dimen.lo_mail_dialog_scroll_content_margintop);
            g_scroll_content.setLayoutParams(layoutParams);
            return;
        }

        layoutParams.height = get_context().getResources().getDimensionPixelSize(R.dimen.lo_mail_dialog_scroll_content_height_false);
        layoutParams.topMargin = get_context().getResources().getDimensionPixelSize(R.dimen.lo_mail_dialog_scroll_content_margintop_false);
        g_scroll_content.setLayoutParams(layoutParams);
    }

    private void set_button_detail(List<MailDetail> mailDetailList) {
        if (mailDetailList.size() == 0) {
            create_close_button();
            return;
        }

        for(int i = 0; i < mailDetailList.size(); i++) {
            create_button(i, mailDetailList.get(i).getTitle());
        }
        g_mail_detail_list = mailDetailList;
        create_close_button();
    }

    private void create_button(int index, String title) {
        final Button button = new Button(getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-2, -2);
        layoutParams.setMargins(10, 30, 10, 30);
        button.setTag(Integer.toString(index));
        button.setText(title);
        button.setPadding(5, 5, 5, 5);
        button.setLayoutParams(layoutParams);
        button.setGravity(Gravity.CENTER);
        button.setBackgroundResource(R.drawable.dialog_button_bg);
        button.setOnClickListener(g_event_click);
        g_dialog_button_content.addView(button);
        if (index == 0) {
            button.requestFocus();
        }
    }

    private void create_close_button() {
        Button button = new Button(getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 30, 10, 30);
        button.setTag(Integer.toString(EVENT_CLOSE));
        button.setText(R.string.mail_close);
        button.setPadding(5, 5, 5, 5);
        button.setLayoutParams(layoutParams);
        button.setGravity(Gravity.CENTER);
        button.setBackgroundResource(R.drawable.dialog_button_bg);
        button.setOnClickListener(g_event_click);
        g_dialog_button_content.addView(button);
    }

    String get_qrcode_url(String url) {
        // BAT_ID => ACS Server
        // STB_SC_ID => ro.serialno
        // STB_CA_SN => ro.boot.cstmsnno
        String STB_SC_ID = Build.getSerial(); //SystemProperties.get("ro.serialno");
        String STB_CA_SN = SystemProperties.get("ro.boot.cstmsnno");
        String BAT_ID = ACSDataProviderHelper.get_acs_provider_data(get_context(), "bat_id");

        Log.d(TAG, "get_qrcode_url: STB_SC_ID = " + STB_SC_ID);
        Log.d(TAG, "get_qrcode_url: STB_CA_SN = " + STB_CA_SN);
        Log.d(TAG, "get_qrcode_url: BAT_ID = " + BAT_ID);

        if (BAT_ID == null)
            BAT_ID = "0";

        return url.replace("{STB_SC_ID}", STB_CA_SN)
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
            ((HomeActivity)get_context()).runOnUiThread(() -> {
                //QRCodeView.setImageBitmap(bitmap);
                if (!get().isFinishing() && !get().isDestroyed())
                    Glide.with(get())
                            .load(bitmap)
                            .into(QRCodeView);
            });
        });
    }
    @Override
    public void dismiss(){
        super.dismiss();
        ((HomeActivity) get_context()).g_liveTvMgr.set_mail_is_running(false);
    }
}
