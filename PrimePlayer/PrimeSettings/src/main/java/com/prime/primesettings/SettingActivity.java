package com.prime.primesettings;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;


public class SettingActivity extends FragmentActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prime_settings);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.lo_tv_settings_container, new SettingFragment())
                .commit();
//        getFragmentManager() // 這呼叫原生 FragmentManager
//                .beginTransaction()
//                // DMGSettingsFragment 繼承 android.app.Fragment，與 getFragmentManager() 匹配
//                .replace(R.id.lo_tv_settings_container, new SettingFragment())
//                .commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //g_prime_dtv.unregister_callbacks();
    }
}
