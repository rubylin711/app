package com.prime.primesettings;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.leanback.preference.LeanbackPreferenceFragmentCompat;
import androidx.leanback.preference.LeanbackSettingsFragmentCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

public class SettingFragment extends LeanbackSettingsFragmentCompat {
    private static final String TAG = "SettingFragment";

    private static final String KEY_USB_UPDATE = "key_usb_update";
    public static final String PACKAGE_OTA_UPDATER = "com.prime.otaupdater";
    public static final String ACTIVITY_OTA_UPDATER = "com.prime.otaupdater.MainActivity";
    private static final String EXTRA_CALLER_FROM_SETTINGS = "EXTRA_CALLER_FROM_SETTINGS";

    @Override
    public void onPreferenceStartInitialScreen() {
        startPreferenceFragment(buildPreferenceFragment(null));
    }
    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat preferenceFragment,
                                             Preference preference) {
        return false;
    }
    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat preferenceFragment,
                                           PreferenceScreen preferenceScreen) {
        PreferenceFragmentCompat frag = buildPreferenceFragment(preferenceScreen.getKey());
        frag.setTargetFragment(preferenceFragment, 0);
        startPreferenceFragment(frag);
        return true;
    }

    private PreferenceFragmentCompat buildPreferenceFragment(String rootKey) {
        PreferenceFragmentCompat fragment = new PrefFragment();
        Bundle args = new Bundle();
        args.putString(PreferenceFragment.ARG_PREFERENCE_ROOT, rootKey);
        fragment.setArguments(args);
        return fragment;
    }

    public static class PrefFragment extends LeanbackPreferenceFragmentCompat {
        private Preference g_usb_update;

        public PrefFragment() {}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // 加载 Preferences XML
            setPreferencesFromResource(R.xml.prime_settings, rootKey);

            g_usb_update = findPreference(KEY_USB_UPDATE);
            g_usb_update.setTitle(R.string.setting_usb_update);
            g_usb_update.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                // ... (內容與原代碼相同) ...
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.i(TAG, "g_usb_update on click");
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(PACKAGE_OTA_UPDATER, ACTIVITY_OTA_UPDATER));
                    intent.putExtra(EXTRA_CALLER_FROM_SETTINGS, true);
                    startActivity(intent);
                    return true;
                }
            });
        }

        @Override
        public void onStart() {
            super.onStart();
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public void onStop() {
            super.onStop();
        }


        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            return super.onPreferenceTreeClick(preference);
        }
    }
}
