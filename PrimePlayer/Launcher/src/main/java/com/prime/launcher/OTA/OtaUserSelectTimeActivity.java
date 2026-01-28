package com.prime.launcher.OTA;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.view.View;
import android.widget.Button;

import com.prime.launcher.BaseActivity;
import com.prime.launcher.HomeApplication;
import com.prime.launcher.R;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.utils.TVMessage;

public class OtaUserSelectTimeActivity extends BaseActivity {
    public static int OTA_USER_SELECT_TIME = 0x123456 ;
    private Button g_2_hours_remind_btn = null ;
    private Button g_1_day_remind_btn = null ;
    private Button g_install_now_btn = null ;
    private Button g_never_remind_btn = null ;
    public static boolean g_user_select_time_activity_is_showing = false ;
    private Context g_context = null ;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ota_select_time);
        g_2_hours_remind_btn = findViewById(R.id.lo_remind_in_2_hours_btn);
        g_1_day_remind_btn = findViewById(R.id.lo_remind_in_1_day_btn);
        g_install_now_btn = findViewById(R.id.lo_update_now_btn);
        g_never_remind_btn = findViewById(R.id.lo_do_not_remind_btn);
        g_context = this ;

        g_2_hours_remind_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OtaUtils.start_Ota_Reminder(g_context, OtaUtils.get_Increase_Hours_Calendar(2));
                finish();
            }
        });

        g_1_day_remind_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OtaUtils.start_Ota_Reminder(g_context, OtaUtils.get_Increase_Hours_Calendar(24));
                finish();
            }
        });

        g_install_now_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeApplication.get_prime_dtv().gpos_info_update_by_key_string(GposInfo.GPOS_OTA_MD5, "0");
                HomeApplication.get_prime_dtv().saveGposKeyValue(GposInfo.GPOS_OTA_MD5, "0");
                SystemProperties.set("persist.sys.prime.ota_download_status", "0");
                SystemProperties.set("persist.sys.prime.ota_last_download_version", "");
                SystemProperties.set("persist.sys.prime.zipfile.download.status", "0");
                OtaUtils.start_Ota_Reminder(g_context,null);
                OtaUtils.send_Start_Ota(g_context);
                finish();
            }
        });

        g_never_remind_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OtaUtils.start_Ota_Reminder(g_context,null);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        g_user_select_time_activity_is_showing = true ;
    }
    @Override
    public void onPause()
    {
        super.onPause();
        g_user_select_time_activity_is_showing = false ;
    }

    @Override
    public void onBroadcastMessage(Context context, Intent intent) {
        super.onBroadcastMessage(context, intent);
    }

    @Override
    public void onMessage(TVMessage msg) {
        super.onMessage(msg);
    }
}
