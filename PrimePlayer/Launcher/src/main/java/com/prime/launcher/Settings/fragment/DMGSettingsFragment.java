package com.prime.launcher.Settings.fragment;


import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.leanback.preference.LeanbackPreferenceFragmentCompat;
import androidx.leanback.preference.LeanbackSettingsFragmentCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.prime.launcher.HomeApplication;
import com.prime.launcher.HomeBackgroundService;
import com.prime.launcher.R;
import com.prime.launcher.Settings.ChannelPreScanningActivity;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.TpInfo;

public class DMGSettingsFragment extends LeanbackSettingsFragmentCompat {
    private static final String TAG = "DMGSettingsFragment";

    public static final String ACTIVITY_CHANNEL_SCAN = "com.prime.launcher.Settings.ChannelScanningActivity";
    public static final String ACTIVITY_SPEED_TEST = "com.prime.launcher.Settings.TBCSpeedTestActivity";
    public static final String PACKAGE_LAUNCHER = "com.prime.launcher";
    public static final String AUTO_DETECT              = "signal_auto_detect";
    public static final String SCANNING_BW              = "scanning_bw";
    public static final String SCANNING_FREQ            = "scanning_freq";
    public static final String SCANNING_MODULATION      = "scanning_modulation";
    public static final String SCANNING_MODULATION_TEXT = "scanning_modulation_text";
    public static final String SCANNING_PAGE            = "scanning_page";
    public static final String SCANNING_SR              = "scanning_sr";
    public static final String SCANNING_TUNERID         = "scanning_tunerid";
    public static final String SCAN_FROM_ACS            = "scan_from_acs";
    public static final String SINGLE_FREQ              = "single_freq";
    private static final String KEY_CHANNEL_SCAN = "channel_scan";
    private static final String KEY_CONNECT_TEST = "connection_test";
    private static final String KEY_INTRODUCTION_TIME = "introduction_time";
    private static final String KEY_RATINGS_SURVEY = "ratings_survey";
    private static final String KEY_RMS_CLIENT = "rms_client";//conten provider key
    private static final String KEY_SIGNAL_DETECT = "signal_detect";
    private static final String TABLE_PROJECT_NAME = "project_name";

    // must sync with pesi TpInfo define
    private static final int QAM_16 = 0;
    private static final int QAM_32 = 1;
    private static final int QAM_64 = 2;
    private static final int QAM_128 = 3;
    private static final int QAM_256 = 4;
    private static final int QAM_AUTO = 5;

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
        private Preference g_channel_scan;
        private Preference g_connect_test;
        private ListPreference g_introduction_time;
        private Preference g_ratings_survey;
        private Preference g_signal_detection;
        private GposInfo g_gpos_info;

        public PrefFragment() {}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            g_gpos_info = HomeApplication.get_prime_dtv().gpos_info_get();
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // 加载 Preferences XML
            setPreferencesFromResource(R.xml.dmg_settings, rootKey);

            g_ratings_survey = findPreference(KEY_RATINGS_SURVEY);

            g_connect_test = findPreference(KEY_CONNECT_TEST);
            g_connect_test.setTitle(R.string.dmg_settings_connection_test);
            g_connect_test.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.i(TAG, "g_connect_test on click");
                    Log.d(TAG, "onPreferenceClick: open TBCSpeedTestActivity");
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(PACKAGE_LAUNCHER, ACTIVITY_SPEED_TEST));
                    startActivity(intent);
                    return true;
                }
            });

            g_signal_detection = findPreference(KEY_SIGNAL_DETECT);
            g_signal_detection.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.i(TAG, "ChannelScan-SignalDetect on click");
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(PACKAGE_LAUNCHER, ACTIVITY_CHANNEL_SCAN));
                    int feq = 0, sym = 0, qam = 0;
                    TpInfo tpInfo = ChannelPreScanningActivity.get_tpInfo(getContext());

                    Bundle bundle = new Bundle();
                    bundle.putBoolean(SCAN_FROM_ACS, false);
                    bundle.putBoolean(AUTO_DETECT, true);
                    bundle.putBoolean(SCANNING_PAGE, true);
                    bundle.putBoolean(SINGLE_FREQ, false);
                    if (tpInfo != null) {
                        feq = tpInfo.CableTp.getFreq() / 1000;
                        sym = tpInfo.CableTp.getSymbol();
                        qam = tpInfo.CableTp.getQamRealValue();
                    }
                    else {
                        if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG)) {
                            feq = 615;
                            sym = 5057;
                            qam = 64;
                        }
                        else {
                            feq = 303;
                            sym = 5200;
                            qam = 256;
                        }
                    }
                    bundle.putInt(SCANNING_FREQ, feq);
                    bundle.putInt(SCANNING_SR, sym);
                    bundle.putInt(SCANNING_BW, 6);
                    bundle.putInt(SCANNING_MODULATION, HomeBackgroundService.get_qam(String.valueOf(qam)));
                    bundle.putString(SCANNING_MODULATION_TEXT, String.valueOf(qam));
                    bundle.putInt(SCANNING_TUNERID, 3); // SIGNAL_DETECT use last tuner
                    intent.putExtras(bundle);
                    startActivity(intent);
                    return true;
                }
            });

            g_channel_scan = findPreference(KEY_CHANNEL_SCAN);
            g_channel_scan.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.i(TAG, "ChannelScan on click");
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName(PACKAGE_LAUNCHER, ACTIVITY_CHANNEL_SCAN));
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(SCAN_FROM_ACS, false);
                    bundle.putBoolean(SCANNING_PAGE, false);
                    bundle.putBoolean(SINGLE_FREQ, false);
                    if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_DMG)) {
                        bundle.putInt(SCANNING_FREQ, 615);
                        bundle.putInt(SCANNING_SR, 5057);
                        bundle.putInt(SCANNING_BW, 6);
                        bundle.putInt(SCANNING_MODULATION, QAM_64);
                        bundle.putString(SCANNING_MODULATION_TEXT, "64");
                    }
                    else if(Pvcfg.getModuleType().equals(Pvcfg.LAUNCHER_MODULE.LAUNCHER_MODULE_CNS)) {
                        bundle.putInt(SCANNING_FREQ, 405);
                        bundle.putInt(SCANNING_SR, 5217);
                        bundle.putInt(SCANNING_BW, 6);
                        bundle.putInt(SCANNING_MODULATION, QAM_256);
                        bundle.putString(SCANNING_MODULATION_TEXT, "256");
                    }
                    else {
                        bundle.putInt(SCANNING_FREQ, 303);
                        bundle.putInt(SCANNING_SR, 5200);
                        bundle.putInt(SCANNING_BW, 6);
                        bundle.putInt(SCANNING_MODULATION, QAM_256);
                        bundle.putString(SCANNING_MODULATION_TEXT, "256");
                    }
                    bundle.putInt(SCANNING_TUNERID, 0);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    return true;
                }
            });

            g_introduction_time = (ListPreference) findPreference(KEY_INTRODUCTION_TIME);
            g_introduction_time.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Log.d(TAG, "onPreferenceChange: Object o = " + o.toString());//click page's value
                    int index = g_introduction_time.findIndexOfValue(o.toString());//click page's value
                    Log.d(TAG, "onPreferenceChange: index = " + String.valueOf(index));
                    Log.d(TAG, "onPreferenceChange: getEntries()[index] = " +  String.valueOf(g_introduction_time.getEntries()[index]));//click page's value
                    Log.d(TAG, "onPreferenceChange: getEntry = " +  String.valueOf(g_introduction_time.getEntry()));//enter page's value.
                    Log.d(TAG, "onPreferenceChange: getEntryValues()[index] = " + g_introduction_time.getEntryValues()[index]);//click page's value
                    Log.d(TAG, "onPreferenceChange: getValue = " + g_introduction_time.getValue());//enter page's value.
                    Log.d(TAG, "onPreferenceChange: getSummary = " +  String.valueOf(g_introduction_time.getSummary()));//enter page's value.
                    int secMs = 5000;
                    if (index == 0)
                        secMs = 3000;
                    else if (index == 1)
                        secMs = 5000;
                    else if (index == 2)
                        secMs = 10000;
                    g_gpos_info.setIntroductionTime(secMs);
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
            update_rms_switch();
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        private String get_rms_status(String type) {
            String name;
            Uri CONTENT_URI = Uri.parse("content://com.vasott.acsclient.DataProvider/provider_table1");
            String[] proj = {"name", "value"};
            String[] selectionArgs = {type};
            Cursor nCursor = getContext().getContentResolver().query(CONTENT_URI, proj, "name=?", selectionArgs, null);
            if (nCursor != null) {
                int cnt = nCursor.getCount();
                if (cnt > 0) {
                    nCursor.moveToFirst();
                    nCursor.getString(0);
                    String value = nCursor.getString(1);
                    name = value;
                } else {
                    name = "BuildConfig.FLAVOR";
                }
                nCursor.close();
            } else {
                name = "BuildConfig.FLAVOR";
            }
            Log.i(TAG, "get test(Server):" + name);
            return name;
        }

        private void update_rms_switch() {
            boolean rms_server_state;
            boolean rms_client_state;
            get_rms_status("rms_status");
            get_rms_status(KEY_RMS_CLIENT);
            if (get_rms_status("rms_status").contentEquals("0") && get_rms_status(KEY_RMS_CLIENT).contentEquals("2")) {
                if (null == g_ratings_survey)
                    return;
                g_ratings_survey.setSummary(R.string.dmg_settings_rms_switch_opening);
                return;
            }
            if (get_rms_status("rms_status").contentEquals("0") || get_rms_status("rms_status").contentEquals("BuildConfig.FLAVOR")) {
                rms_server_state = false;
            } else {
                rms_server_state = true;
            }
            if (get_rms_status(KEY_RMS_CLIENT).contentEquals("0") || get_rms_status(KEY_RMS_CLIENT).contentEquals("BuildConfig.FLAVOR")) {
                rms_client_state = false;
            } else {
                rms_client_state = true;
            }
            if (rms_server_state && rms_client_state) {
                if (null == g_ratings_survey)
                    return;
                g_ratings_survey.setSummary(R.string.dmg_settings_rms_switch_on);
            } else {
                if (null == g_ratings_survey)
                    return;
                g_ratings_survey.setSummary(R.string.dmg_settings_rms_switch_off);
            }
        }
        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            return super.onPreferenceTreeClick(preference);
        }
    }
}
