package com.prime.launcher.Settings.fragment;

import static com.prime.launcher.Settings.UnlockChannelActivity.ACTIVITY_UNLOCK_CHANNEL;
import static com.prime.launcher.Settings.UnlockChannelActivity.DMG_SETTINGS_REQUEST_CODE;
import static com.prime.launcher.Settings.UnlockChannelActivity.LOCK_TYPE_WORKER;
import static com.prime.launcher.Settings.fragment.DMGSettingsFragment.PACKAGE_LAUNCHER;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.prime.launcher.HomeApplication;
import com.prime.launcher.R;
import com.prime.launcher.PrimeDtv;
import com.prime.datastructure.sysdata.EnTableType;
import com.prime.datastructure.sysdata.GposInfo;

public class EngineeringSettingFragment extends LeanbackPreferenceFragmentCompat {
    private static final String TAG = "EngineeringSettingFragment";
    private static final String ACTIVITY_CHANNEL_PRE_SCAN = "com.prime.launcher.Settings.ChannelPreScanningActivity";
    private static final String ACTIVITY_RESET_CA = "com.prime.launcher.Settings.ResetCAActivity";
    private static final String ACTIVITY_RESET_CA_LICENSE = "com.prime.launcher.Settings.ResetCALicenseActivity";

    // ota updater
    public static final String PACKAGE_OTA_UPDATER = "com.prime.otaupdater";
    public static final String ACTIVITY_OTA_UPDATER = "com.prime.otaupdater.MainActivity";
    public static final String OTA_UPDATER_ACTION = "com.prime.otaupdater.ACTION_HTTP_OTA_UPDATE";
    public static final String EXTRA_HTTP_PAYLOAD_BIN  = "com.prime.otaupdater.EXTRA_HTTP_OTA_PAYLOAD";
    public static final String EXTRA_HTTP_PAYLOAD_PROP = "com.prime.otaupdater.EXTRA_HTTP_OTA_PROPERTY";
    public static final String EXTRA_HTTP_METADATA     = "com.prime.otaupdater.EXTRA_HTTP_OTA_METADATA";
    public static final String EXTRA_HTTP_UPDATE_IMAGE_VERSION  = "prime.abupdate.image.version";
    public static final String EXTRA_HTTP_FORCE        = "com.prime.otaupdater.EXTRA_HTTP_OTA_FORCE";
    public static final String EXTRA_CALLER        = "com.prime.otaupdater.EXTRA_CALLER";
    public static final String EXTRA_UPDATE_MODE        = "com.prime.otaupdater.EXTRA_UPDATE_MODE";
    public static final String EXTRA_UPDATE_ZIP_URL     = "com.prime.otaupdater.EXTRA_UPDATE_ZIP_URL";
    public static final String EXTRA_UPDATE_ZIP_OFFSET  = "com.prime.otaupdater.EXTRA_ZIP_OFFSET";
    public static final String EXTRA_UPDATE_ZIP_SIZE    = "com.prime.otaupdater.EXTRA_ZIP_SIZE";
    public static final String EXTRA_UPDATE_PROPERTIES  = "com.prime.otaupdater.EXTRA_PROPERTIES";

    private static final String EXTRA_CALLER_FROM_SETTINGS = "EXTRA_CALLER_FROM_SETTINGS";

    private static final String KEY_ENGINEERING_SETTING = "key_engineering_setting";
    private static final String KEY_CHANNEL_SCAN = "key_channel_scan";
    private static final String KEY_ERROR_LOG = "key_error_log";
    private static final String KEY_INFO_HINT = "key_info_hint";
    private static final String KEY_RESET_NAGRA = "key_reset_nagra";
    private static final String KEY_RESET_NAGRA_LICENSE = "key_reset_nagra_license";
    private static final String KEY_USB_UPDATE = "key_usb_update";
    private static final String KEY_FIRMWARE_VERSION = "ro.firmwareVersion";
    private static final String KEY_PROVISION_INFO = "provision_info";

    public static final String INFO_HINT_ON_VALUE = "1";
    public static final String INFO_HINT_OFF_VALUE = "2";
    private final int INFO_HINT_ON_INDEX = 0;
    private final int INFO_HINT_OFF_INDEX = 1;

    private PreferenceScreen g_engineering_screen;
    private Preference g_pref_channel_scan;
    private Preference g_pref_error_log;
    private ListPreference g_pref_info_hint;
    private Preference g_pref_reset_nagra;
    private Preference g_pref_reset_nagra_license;
    private Preference g_pref_usb_update;

    private int g_enable = 0;
    private PrimeDtv g_prime_dtv;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Log.d(TAG, "onCreatePreferences" );
        setPreferencesFromResource(R.xml.dmg_settings_engineering_settings, null);
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(PACKAGE_LAUNCHER, ACTIVITY_UNLOCK_CHANNEL));
        intent.putExtra("KEY_LOCK_TYPE", LOCK_TYPE_WORKER);
        startActivityForResult(intent, DMG_SETTINGS_REQUEST_CODE);

        init_disable();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        g_prime_dtv = HomeApplication.get_prime_dtv();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DMG_SETTINGS_REQUEST_CODE && resultCode == -1 && data != null) {
            Log.d(TAG, "get result = " + data.getBooleanExtra("KEY_PASS_RESULT", false));
            g_enable = 1;
            init();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        g_prime_dtv.save_table(EnTableType.GPOS);
    }

    private void init_disable() {
        g_engineering_screen = findPreference(KEY_ENGINEERING_SETTING);
        for (int i = 0; i < g_engineering_screen.getPreferenceCount(); i++) {
            if (i == 0) {
                g_engineering_screen.getPreference(i).setTitle(R.string.dmg_settings_factory_lock_password_hint);
                g_engineering_screen.getPreference(i).setSummary(R.string.dmg_settings_factory_lock_password_hint_detail);
            } else {
                g_engineering_screen.getPreference(i).setTitle("");
                g_engineering_screen.getPreference(i).setSummary("");
                g_engineering_screen.getPreference(i).setEnabled(false);
            }
        }
        g_pref_usb_update = findPreference(KEY_USB_UPDATE);
    }

    private void init() {
       g_engineering_screen = findPreference(KEY_ENGINEERING_SETTING);

        for (int i = 0; i < g_engineering_screen.getPreferenceCount(); i++) {
            g_engineering_screen.getPreference(i).setEnabled(true);
            g_engineering_screen.getPreference(i).setSummary("");
        }
        g_pref_usb_update = findPreference(KEY_USB_UPDATE);
        g_pref_usb_update.setTitle(R.string.dmg_settings_engineering_setting_usb_update);

        g_pref_channel_scan = findPreference(KEY_CHANNEL_SCAN);
        g_pref_channel_scan.setTitle(R.string.dmg_settings_engineering_setting_channel_scan);

//        g_pref_error_log = findPreference(KEY_ERROR_LOG);
//        g_pref_error_log.setTitle(R.string.dmg_settings_engineering_setting_error_log);
//        g_pref_error_log.setVisible(false);

//        set_reset_nagra(); // remove for wvcas

        set_info_int();

        set_reset_nagra_license();

        // update ro.firmwareVersion
        Preference firmware_version = findPreference(KEY_FIRMWARE_VERSION);
        if (firmware_version != null) {
            firmware_version.setTitle(R.string.firmware_version_title);
            firmware_version.setSummary(SystemProperties.get("ro.firmwareVersion", "ro.firmwareVersion"));
        }

        Preference provision_info = findPreference(KEY_PROVISION_INFO);
        if (provision_info != null) {
            provision_info.setTitle(R.string.provision_info);
            provision_info.setFragment(ProvisionInfoFragment.class.getName());
        }
    }

    @Override
    public boolean onPreferenceTreeClick(@NonNull Preference preference) {
        switch (preference.getKey()) {
            case KEY_CHANNEL_SCAN:
                Log.i(TAG, "g_pref_channel_scan on click");
                start_channel_pre_scan();
                break;
            case KEY_USB_UPDATE:
                Log.i(TAG, "g_pref_usb_update on click");
                Intent intent = new Intent();
                if (g_enable == 0) {
                    intent.setComponent(new ComponentName(PACKAGE_LAUNCHER, ACTIVITY_UNLOCK_CHANNEL));
                    intent.putExtra("KEY_LOCK_TYPE", LOCK_TYPE_WORKER);
                    startActivityForResult(intent, DMG_SETTINGS_REQUEST_CODE);
                }
                else {
                    intent.setComponent(new ComponentName(PACKAGE_OTA_UPDATER, ACTIVITY_OTA_UPDATER));
                    intent.putExtra(EXTRA_CALLER_FROM_SETTINGS, true);
                    startActivity(intent);
                }
                break;
            case KEY_PROVISION_INFO:
                Log.i(TAG, "g_pref_provision_info on click");
                break;
        }

        return super.onPreferenceTreeClick(preference);
    }

    private void start_channel_pre_scan() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(PACKAGE_LAUNCHER, ACTIVITY_CHANNEL_PRE_SCAN));
        startActivity(intent);
    }

    private void set_info_int() {
        g_pref_info_hint = findPreference(KEY_INFO_HINT);
        g_pref_info_hint.setTitle(R.string.dmg_settings_engineering_setting_info_hint);

        String value = Integer.toString(get_info_int());
        Log.d(TAG, "set_info_int: value = " + value);
        if (value.equals(INFO_HINT_OFF_VALUE)) {
            Log.d(TAG, "set_info_int: value = 2");
            g_pref_info_hint.setSummary(R.string.dmg_settings_rms_switch_off);
            g_pref_info_hint.setValue(INFO_HINT_OFF_VALUE);
        } else {
            Log.d(TAG, "set_info_int: value = else");
            g_pref_info_hint.setSummary(R.string.dmg_settings_rms_switch_on);
            g_pref_info_hint.setValue(INFO_HINT_ON_VALUE);
        }
        g_pref_info_hint.setOnPreferenceChangeListener((preference, object) -> {
            int index = g_pref_info_hint.findIndexOfValue((String) object);
            switch (index) {
                case INFO_HINT_ON_INDEX:
                    g_pref_info_hint.setSummary(R.string.dmg_settings_rms_switch_on);
                    g_pref_info_hint.setValue(INFO_HINT_ON_VALUE);
                    set_data_to_gpos(GposInfo.GPOS_MAIL_NOTIFY_STATUS, Integer.parseInt(INFO_HINT_ON_VALUE));
                    break;
                case INFO_HINT_OFF_INDEX:
                    g_pref_info_hint.setSummary(R.string.dmg_settings_rms_switch_off);
                    g_pref_info_hint.setValue(INFO_HINT_OFF_VALUE);
                    set_data_to_gpos(GposInfo.GPOS_MAIL_NOTIFY_STATUS, Integer.parseInt(INFO_HINT_OFF_VALUE));
                    break;
            }
            return true;
        });
    }

    private void set_reset_nagra() {
        g_pref_reset_nagra = findPreference(KEY_RESET_NAGRA);
        g_pref_reset_nagra.setTitle(R.string.tv_nagra_reset_title);
        g_pref_reset_nagra.setOnPreferenceClickListener((preference -> {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(PACKAGE_LAUNCHER, ACTIVITY_RESET_CA));
            startActivity(intent);
            return true;
        }));
    }

    private void set_reset_nagra_license() {
        g_pref_reset_nagra_license = findPreference(KEY_RESET_NAGRA_LICENSE);
        g_pref_reset_nagra_license.setTitle(R.string.tv_nagra_license_reset_title);
        g_pref_reset_nagra_license.setOnPreferenceClickListener((preference -> {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(PACKAGE_LAUNCHER, ACTIVITY_RESET_CA_LICENSE));
            startActivity(intent);
            return true;
        }));
    }

    private void set_data_to_gpos(String keyName, int dataValue) {
        Log.d(TAG, "set_data_to_gpos keyName = " + keyName + " dataValue = " + dataValue);

        GposInfo gposInfo = g_prime_dtv.gpos_info_get();
        if (keyName.equals(GposInfo.GPOS_MAIL_NOTIFY_STATUS))
            gposInfo.setMailNotifyStatus(dataValue);
    }

    private int get_data_from_gpos(String dataName) {
        Log.d(TAG, "get_data_from_gpos dataName = " + dataName);
        GposInfo gposInfo = g_prime_dtv.gpos_info_get();

        if (dataName.equals(GposInfo.GPOS_MAIL_NOTIFY_STATUS))
            return gposInfo.getMailNotifyStatus();
        return -1;
    }

    private int get_info_int() {
        return get_data_from_gpos(GposInfo.GPOS_MAIL_NOTIFY_STATUS);
    }
}
