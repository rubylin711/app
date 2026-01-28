package com.prime.dmg.launcher.Settings;

import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.prime.dmg.launcher.HomeApplication;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Settings.fragment.DMGSettingsFragment;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.sysdata.EnTableType;
import com.prime.dtv.sysdata.GposInfo;
import com.prime.dtv.utils.TVMessage;


public class DMGSettingsActivity extends FragmentActivity implements PrimeDtv.DTVCallback {
    private final static String TAG = "DMGSettingsActivity";
    private PrimeDtv g_prime_dtv;
    private GposInfo g_gpos_info;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        g_prime_dtv = HomeApplication.get_prime_dtv();
        //g_prime_dtv.register_callbacks();
        g_gpos_info = g_prime_dtv.gpos_info_get();
        setContentView(R.layout.activity_dmg_settings);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.lo_tv_settings_container, new DMGSettingsFragment())
                .commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        g_prime_dtv.save_table(EnTableType.GPOS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //g_prime_dtv.unregister_callbacks();
    }

    @Override
    public void onMessage(TVMessage tvMessage) {
        /*switch (tvMessage.getMsgType()) {
            case 0:
                break;
        }*/
    }

    public PrimeDtv get_prime_dtv() {
        return g_prime_dtv;
    }

    public GposInfo get_gpos_info() {
        return g_gpos_info;
    }
}
