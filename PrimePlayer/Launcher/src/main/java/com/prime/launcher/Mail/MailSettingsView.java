package com.prime.launcher.Mail;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.textclassifier.Log;
import android.widget.RelativeLayout;
import android.widget.Switch;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.prime.launcher.EPG.MiddleFocusRecyclerView;
import com.prime.launcher.HomeApplication;
import com.prime.launcher.Utils.Utils;
import com.prime.launcher.R;
import com.prime.launcher.PrimeDtv;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.MailSetting;

public class MailSettingsView extends RelativeLayout {
    private static final String TAG = "MailActivity";
    private static final int MAIL_SETTINGS_SHOPPING = 1001;
    private static final int MAIL_SETTINGS_NEWS     = 1002;
    private static final int MAIL_SETTINGS_POPULAR  = 1003;
    private static final int MAIL_SETTINGS_COUPON   = 1004;
    private static final int MAIL_SETTINGS_SERVICE  = 1005;

    private Handler g_handler;
    private MiddleFocusRecyclerView g_switch_mail_settings;
    private SparseArray<Boolean> g_mail_settings_data;
    private MailSettingsAdapter g_adpt_mail_settings;
    private MailManager g_mail_manager;

    public MailSettingsView(Context context) {
        super(context);
        init();
    }

    public MailSettingsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MailSettingsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_mail_settings, this);
    }

    public void init(MailManager mailManager) {
        g_mail_manager = mailManager;
        g_mail_settings_data = new SparseArray<>();
        g_handler = new Handler();
        g_switch_mail_settings =  findViewById(R.id.lo_mail_settings_rcv_switch);
        init_settings_switch();
    }

    private void init_settings_switch() {
        g_switch_mail_settings.setHasFixedSize(true);
        g_switch_mail_settings.setLayoutManager(new LinearLayoutManager(getContext()));
        g_switch_mail_settings.addItemDecoration(new MailSettingsAdapter.MyItemDecoration(getContext()));
        g_adpt_mail_settings = new MailSettingsAdapter(getContext(), get_mail_settings(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update_mail_settings(Utils.get_tag_id((String) v.getTag()), v);
            }
        });
        g_switch_mail_settings.setAdapter(g_adpt_mail_settings);
        update_focus();
    }

    private SparseArray<Boolean> get_mail_settings() {
        MailSetting mailSettingsFromPrefs = get_mail_settings_status_from_gpos();
        g_mail_settings_data.put(0, mailSettingsFromPrefs.get_shopping());
        g_mail_settings_data.put(1, mailSettingsFromPrefs.get_news());
        g_mail_settings_data.put(2, mailSettingsFromPrefs.get_popular());
        g_mail_settings_data.put(3, mailSettingsFromPrefs.get_coupon());
        g_mail_settings_data.put(4, mailSettingsFromPrefs.get_service());
        return g_mail_settings_data;
    }

    private MailSetting get_mail_settings_status_from_gpos() {
        if (g_mail_manager == null) {
            Log.d(TAG, "g_mail_manager == null");
        }

        MailActivity activity = (MailActivity) getContext();
        PrimeDtv primeDtv = HomeApplication.get_prime_dtv();
        GposInfo mailSettingsGposInfo = primeDtv.gpos_info_get(); //GposInfo mailSettingsGposInfo = g_mail_manager.gpos_info_get();

        Log.d(TAG, "Shopping = " + mailSettingsGposInfo.getMailSettingsShopping() +
                        " News = " + mailSettingsGposInfo.getMailSettingsNews());

        if (mailSettingsGposInfo != null)
            return new MailSetting(mailSettingsGposInfo.getMailSettingsShopping() != 0,
                                    mailSettingsGposInfo.getMailSettingsNews() != 0,
                                    mailSettingsGposInfo.getMailSettingsPopular() != 0,
                                    mailSettingsGposInfo.getMailSettingsCoupon() != 0,
                                    mailSettingsGposInfo.getMailSettingsService() != 0);
        return new MailSetting(true, true, true, true, true);
    }

    public void update_mail_settings(int index, View view) {
        RelativeLayout relativeLayout = (RelativeLayout) view;
        boolean isChecked = ((Switch) relativeLayout.getChildAt(1)).isChecked();
        ((Switch) relativeLayout.getChildAt(1)).setChecked(!isChecked);
        switch (index) {
            case MAIL_SETTINGS_SHOPPING:
                Log.d(TAG, "update_mail_settings MAIL_SETTINGS_SHOPPING = " + !isChecked);
                g_mail_manager.gpos_info_update_by_key_string(GposInfo.GPOS_MAIL_SETTINGS_SHOPPING, !isChecked ?1:0);
                break;
            case MAIL_SETTINGS_NEWS:
                Log.d(TAG, "update_mail_settings MAIL_SETTINGS_NEWS = " + !isChecked);
                g_mail_manager.gpos_info_update_by_key_string(GposInfo.GPOS_MAIL_SETTINGS_NEWS, !isChecked ?1:0);
                break;
            case MAIL_SETTINGS_POPULAR:
                Log.d(TAG, "update_mail_settings MAIL_SETTINGS_POPULAR = " + !isChecked);
                g_mail_manager.gpos_info_update_by_key_string(GposInfo.GPOS_MAIL_SETTINGS_POPULAR, !isChecked ?1:0);
                break;
            case MAIL_SETTINGS_COUPON:
                Log.d(TAG, "update_mail_settings MAIL_SETTINGS_COUPON = " + !isChecked);
                g_mail_manager.gpos_info_update_by_key_string(GposInfo.GPOS_MAIL_SETTINGS_COUPON, !isChecked ?1:0);
                break;
            case MAIL_SETTINGS_SERVICE:
                Log.d(TAG, "update_mail_settings MAIL_SETTINGS_SERVICE = " + !isChecked);
                g_mail_manager.gpos_info_update_by_key_string(GposInfo.GPOS_MAIL_SETTINGS_SERVICE, !isChecked ?1:0);
                break;
        }
    }

    public void update_focus() {
        focus_first_child(g_switch_mail_settings);
    }

    private void focus_first_child(final MiddleFocusRecyclerView view) {
        g_handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (view.isComputingLayout() || view.getChildAt(0) == null) {
                    g_handler.postDelayed(this, 50L);
                } else {
                    view.getChildAt(0).requestFocus();
                }
            }
        }, 50L);
    }
}