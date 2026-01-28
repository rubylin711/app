package com.prime.dmg.launcher.Mail;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.prime.dmg.launcher.BaseActivity;
import com.prime.dmg.launcher.HomeApplication;
import com.prime.dmg.launcher.R;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.sysdata.EnTableType;
import com.prime.dtv.utils.TVMessage;

public class MailActivity extends BaseActivity implements PrimeDtv.DTVCallback, MailMenuView.OnMenuItemListener {
    private static final String TAG = "MailActivity";

    public static final int MAIL_INBOX = 0;
    public static final int MAIL_SETTINGS = 1;

    public static final String MSG_NEW_MAIL = "NEW_MAIL";

    private PrimeDtv g_dtv;
    private MailManager g_mail_manager;

    private int g_menu_position;
    private MailMenuView g_mailMenuView;
    private MailInboxView g_mailInboxView;
    private MailSettingsView g_mailSettingsView;
    private Handler g_handler;
    private BroadcastReceiver g_localReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail);

        g_dtv = HomeApplication.get_prime_dtv();
        //g_dtv.register_callbacks();
        g_handler = new Handler(Looper.getMainLooper());
        g_mail_manager = MailManager.GetInstance(null);

        init_inbox();
        init_settings();
        init_menu();
        receiver_register();
        select_menu(MAIL_INBOX);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop:");
        g_mailInboxView.update_mails_read_status(g_mailInboxView.get_mail_data_list());
        g_dtv.save_table(EnTableType.GPOS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //g_dtv.unregister_callbacks();
        g_handler = null;
        receiver_unregister();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Log.e(TAG, "onKeyDown: [menu_position] = " + menu_position);
        if (KeyEvent.KEYCODE_PROG_BLUE == keyCode && g_menu_position == MAIL_INBOX) {
            Log.d(TAG, "onKeyDown: KEYCODE_PROG_BLUE");
            g_mailInboxView.delete_mail();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBroadcastMessage(Context context, Intent intent) {
        super.onBroadcastMessage(context, intent);
    }

    @Override
    public void onMessage(TVMessage tvMessage) {
        super.onMessage(tvMessage);
    }

    @Override
    public void on_click_menu_item(int position) {
        Log.d(TAG, "on_click_menu_item: [menu_position] " + position);
        select_menu(position);
    }

    @Override
    public void on_key_menu_item(View view, int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && g_menu_position == MAIL_INBOX) {
            g_handler.postDelayed(() -> {
                g_mailInboxView.focus_last_item_view();
            }, 50L);
        }
    }

    private void init_inbox() {
        g_mailInboxView = findViewById(R.id.lo_mail_view_mail_inbox);
    }

    public void init_settings() {
        g_mailSettingsView = findViewById(R.id.lo_mail_view_mail_setting);
        g_mailSettingsView.init(g_mail_manager);
    }

    public void init_menu() {
        g_mailMenuView = findViewById(R.id.lo_mail_view_mail_menu);
        g_mailMenuView.add_menu_item(R.drawable.icon_tvmail_mail_rest, R.drawable.icon_tvmail_mail_focus_red, getString(R.string.mail_inbox));
        g_mailMenuView.add_menu_item(R.drawable.icon_tvmail_setting_rest, R.drawable.icon_tvmail_setting_focus_red, getString(R.string.mail_setting));
        g_mailMenuView.genMenuItemLayout();
        g_mailMenuView.update_item_select(0);
        g_mailMenuView.set_menu_item_listener(this);
    }

    private void receiver_register() {
        g_localReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (null == action)
                    return;

                if (action.equals(MailActivity.MSG_NEW_MAIL)) {
                    g_mailInboxView.update_data();
                    g_mailMenuView.set_label_visible(false);
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(MailActivity.MSG_NEW_MAIL);
        LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(g_localReceiver, filter);
    }

    private void receiver_unregister() {
        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(g_localReceiver);
    }

    @Override
    public PrimeDtv get_prime_dtv() {
        return g_dtv;
    }

    public int get_menu_position() {
        return g_menu_position;
    }

    public Handler get_handler() {
        return g_handler;
    }

    public void select_menu(int position) {

        g_menu_position = position;

        if (g_menu_position == MAIL_INBOX) {
            g_mailSettingsView.setVisibility(View.GONE);
            g_mailInboxView.setVisibility(View.VISIBLE);
            g_mailInboxView.requestFocus();
            g_mailInboxView.update_focus();
        }
        else if (g_menu_position == MAIL_SETTINGS) {
            g_mailInboxView.setVisibility(View.GONE);
            g_mailSettingsView.setVisibility(View.VISIBLE);
            g_mailSettingsView.update_focus();
        }

        update_hot_keys_hint();
    }

    private void update_hot_keys_hint() {
        LinearLayout linearLayout = findViewById(R.id.lo_mail_time_hint_layer);

        if (g_menu_position == MAIL_INBOX)
            linearLayout.setVisibility(View.VISIBLE);
        else
            linearLayout.setVisibility(View.GONE);
    }
}
