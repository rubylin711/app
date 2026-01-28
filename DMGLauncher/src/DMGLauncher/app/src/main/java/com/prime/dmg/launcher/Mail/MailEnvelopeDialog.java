package com.prime.dmg.launcher.Mail;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.BaseDialog;
import com.prime.dtv.utils.TVMessage;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MailEnvelopeDialog extends BaseDialog {
    private static final String TAG = "MailEnvelopeDialog";

    private WeakReference<Context> g_ref;
    private MailManager g_mailManager;
    private ArrayList<MailDetail> g_mail_detail_list;
    private Mail g_mail;

    //ui
    private ImageView g_icon;
    private LinearLayout g_button_content;
    private RelativeLayout g_content_layer;

    public MailEnvelopeDialog(Context context) {
        super(context, R.style.Theme_DMGLauncher_MailEnvelopeDialog);
        g_ref = new WeakReference<>(context);
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
        setContentView(R.layout.view_mail_envelop_dialog);

        g_content_layer = findViewById(R.id.lo_mail_envelop_dialog_content_layer);
        g_icon = findViewById(R.id.lo_mail_envelop_dialog_icon);
        g_button_content = findViewById(R.id.lo_mail_envelop_dialog_button_content);
    }

    public void show_promotion_content(Mail mail) {
        if (mail == null)
            return;

        /*Mail mail = new Mail();
        mail.setEnvelope_location(1);
        mail.setEnvelope_icon("");
        mail.setQrcode("https://mwps.tbc.net.tw/product/ott/{STB_SC_ID}/{STB_CA_SN}/{BAT_ID}/hbogo");
        mail.setContent_icon("https://epgstore.tbc.net.tw/acs-api//uploads/icon/1676883894708.jpg");
        mail.setTitle("國家防災日演練預先宣導-正式演練");
        mail.setContent("社群粉絲多達百萬名的三津家貴也教練解釋，「放鬆跑」是一種介於走路與跑步之間的運動方式，能讓人輕鬆不費力、持續地養成運動習慣，自然而然瘦下來，許多35至45歲的女性，經過育兒階段後開始有時間能夠專注自己的體態，很多人都利用「放鬆跑」順利在幾個月內瘦下5到10公斤！\\n\" + \"\\n\" + \"「放鬆跑」運動的關鍵在於運用臀部和大腿後側的肌肉，這些部位是腿部中最大的肌肉群，善用這些肌群、運用正確的跑步姿勢，不僅可以減少疲勞感，運動不容易疲累，還能有效預防受傷。日本奧運跑步教練青山剛就形容，臀部肌群像是跑步的「加速器」與「穩定器」，掌握這些肌肉的運用，能大幅減少腿部負擔，降低受傷風險。\\n\" + \"\\n\" + \"有別於大家對於「跑步傷膝蓋」的印象，其實適量、動作正確的跑步不但不會傷害到膝關節，反而能預防退化，有研究也顯示，跑步的好處除了瘦身外，還包含降血脂、增加好膽固醇、減低心臟病及癌症死亡率。");
        mail.setUpdated_time("2024-08-22 17:39:19");*/
        g_mail = mail;

        set_position(mail.getEnvelope_location());
        set_envelope_icon(mail.getEnvelope_icon());
        create_single_mail_content(mail.get_mail_detail_envelope());
        create_button("back", get_context().getString(R.string.mail_close));
    }

    @SuppressLint("RtlHardcoded")
    private void set_position(int envelopeLocation) {
        FrameLayout.LayoutParams layoutParams = new  FrameLayout.LayoutParams(get_context().getResources().getDimensionPixelSize(R.dimen.lo_mail_envelop_dialog_width), get_context().getResources().getDimensionPixelSize(R.dimen.lo_mail_envelop_dialog_height));
        switch (envelopeLocation) {
            case 2:
                layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                break;
            case 3:
                layoutParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
                break;
            case 4:
                layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
                break;
            case 5:
                layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
                break;
            case 6:
                layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                break;
            case 7:
                layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
                break;
            case 8:
                layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
                break;
            default:
                layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                break;
        }
        g_content_layer.setBackgroundResource(R.drawable.mail_icon_shadow);
        g_content_layer.setLayoutParams(layoutParams);
    }

    private AppCompatActivity get() {
        return (AppCompatActivity) g_ref.get();
    }

    private void set_envelope_icon(String iconUri) {
        if (!get().isFinishing() && !get().isDestroyed())
            Glide.with(get())
                    .load(iconUri)
                    .error(R.drawable.tvmail_icon_s)
                    .apply((BaseRequestOptions<?>) new RequestOptions()
                    .override(102, 102)
                    .fitCenter())
                    .into(g_icon);
    }

    private void create_single_mail_content(ArrayList<MailDetail> mailDetailList) {
        if (mailDetailList.size() == 0)
            return;

        for (int i = 0; i < mailDetailList.size(); i++) {
            create_button(mailDetailList.get(i).getHot_key(), mailDetailList.get(i).getTitle());
        }

        g_mail_detail_list = mailDetailList;
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (set_channel(event))
            return false;

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (set_channel(event))
            return false;

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            Log.d(TAG, "onKeyDown: CENTER");
            start_event(get_promotion_event("ok"));
        }
        else if (keyCode == KeyEvent.KEYCODE_PROG_RED) {
            Log.d(TAG, "onKeyDown: RED");
            start_event(get_promotion_event("red"));
        }
        else if (keyCode == KeyEvent.KEYCODE_PROG_GREEN) {
            Log.d(TAG, "onKeyDown: GREEN");
            start_event(get_promotion_event("green"));
        }
        else if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW) {
            Log.d(TAG, "onKeyDown: YELLOW");
            start_event(get_promotion_event("yellow"));
        }
        else if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
            Log.d(TAG, "onKeyDown: BLUE");
            start_event(get_promotion_event("blue"));
        }
        else if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(TAG, "onKeyDown: BACK");
            ((HomeActivity) get_context()).g_liveTvMgr.set_mail_is_running(false);
            if(g_mail.getRepeat_type() == Mail.TYPE_CLOSE_REPEAT)
                update_mail_read_status();
        }

        return super.onKeyDown(keyCode, event);
    }

    @SuppressLint("RtlHardcoded")
    private void create_button(String hotkey, String title) {
        Log.d(TAG, "create_button: hotkey = " + hotkey + " title = " + title);

        // item icon imageview
        ImageView icon = new ImageView(getContext());
        LinearLayout.LayoutParams iconLayoutParams = new LinearLayout.LayoutParams(get_context().getResources().getDimensionPixelSize(R.dimen.lo_mail_envelop_dialog_hint_icon_width), get_context().getResources().getDimensionPixelSize(R.dimen.lo_mail_envelop_dialog_hint_icon_height));
        iconLayoutParams.leftMargin = get_context().getResources().getDimensionPixelSize(R.dimen.lo_mail_envelop_dialog_hint_icon_marginstart);
        iconLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        icon.setLayoutParams(iconLayoutParams);
        icon.setImageResource(get_hot_key_icon(hotkey));

        // item text textview
        TextView text = new TextView(getContext());
        LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textLayoutParams.leftMargin = get_context().getResources().getDimensionPixelSize(R.dimen.lo_mail_envelop_dialog_hint_text_marginstart);
        textLayoutParams.rightMargin = get_context().getResources().getDimensionPixelSize(R.dimen.lo_mail_envelop_dialog_hint_text_marginend);
        textLayoutParams.gravity = Gravity.END;
        text.setLayoutParams(textLayoutParams);
        text.setGravity(Gravity.CENTER);
        text.setTextColor(Color.WHITE);
        text.setTextSize(12.0f);
        text.setText(title);


        //item liner layout layer, contain icon and text;
        int width = (int)text.getPaint().measureText(text.getText().toString())
                + textLayoutParams.leftMargin + textLayoutParams.rightMargin
                + icon.getLayoutParams().width
                + iconLayoutParams.leftMargin;

        LinearLayout itemLinearLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams itemLayoutParams = new LinearLayout.LayoutParams(width, get_context().getResources().getDimensionPixelSize(R.dimen.lo_mail_envelop_dialog_hint_content_height));
        itemLayoutParams.gravity = Gravity.CENTER;
        itemLayoutParams.bottomMargin = get_context().getResources().getDimensionPixelSize(R.dimen.lo_mail_envelop_dialog_hint_content_marginbottom);
        itemLinearLayout.setBackgroundResource(R.drawable.tvmail_label);
        itemLinearLayout.setLayoutParams(itemLayoutParams);
        itemLinearLayout.addView(icon);
        itemLinearLayout.addView(text);
        //Log.d(TAG, "create_button: item LinearLayout = " + itemLinearLayout.getLayoutParams().width);
        g_button_content.addView(itemLinearLayout);
    }

    private int get_hot_key_icon(String hotkey) {
        return hotkey.equals("ok") ? R.drawable.hint_ok :
               hotkey.equals("red") ? R.drawable.hint_red :
               hotkey.equals("green") ? R.drawable.hint_green :
               hotkey.equals("yellow") ? R.drawable.hint_yellow :
               hotkey.equals("blue") ? R.drawable.hint_blue :
               hotkey.equals("back") ? R.drawable.hint_back : R.drawable.hint_ok;
    }

    private void start_event(MailDetail mailDetail) {
        if (mailDetail == null) {
            Log.e(TAG, "start_event: mailDetail == null");
            return;
        }

        ((HomeActivity) get_context()).g_liveTvMgr.set_mail_is_running(false);
        g_mailManager.start_mail_dialog_event(g_mail, mailDetail);
        update_mail_read_status();
        dismiss();
    }

    private MailDetail get_promotion_event(String hotkey) {
        if (g_mail_detail_list.size() == 0)
            return null;

        for (MailDetail mailDetail: g_mail_detail_list) {
            if (mailDetail.getHot_key().equalsIgnoreCase(hotkey))
                return mailDetail;
        }

        return null;
    }

    private void update_mail_read_status() {
        if (g_mailManager != null) {
            g_mailManager.set_mail_read_status(get_context(), g_mail);
        }
    }

    private Context get_context() {
        return g_ref.get();
    }

    private boolean set_channel(KeyEvent event) {
        return ((HomeActivity) get_context()).g_liveTvMgr.set_channel(event);
    }

    public boolean is_the_same_mail(Mail mail){
        if(mail.getId() == g_mail.getId())
            return true;
        return false;
    }
}
