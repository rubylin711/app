package com.prime.launcher.Settings.fragment;

import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.Process;
import android.util.Log;

import androidx.leanback.preference.LeanbackPreferenceFragmentCompat;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.prime.launcher.R;

public class DMGRMSSwitchFragment extends LeanbackPreferenceFragmentCompat {
    private final static String TAG = "DMGRMSSwitchFragment";

    private static final String ACTIVITY_RMS_COLLECT = "com.prime.launcher.Settings.RmsCollectActivity";
    public static final Uri CONTENT_URI = Uri.parse("content://com.vasott.acsclient.DataProvider/provider_table1");
    private static final boolean DEBUG = true;
    private static final String KEY_RMS_SWITCH = "rms_switch";
    private static final long ONEDAY_SECOND = 86400;
    private static final String PROPERTY_RMSCLIENT_TRIGGET_TIME = "persist.vendor.rtk.rms_triggertime";

    private final int REQUEST_NUM = 4321;
    private final int RESPOND_AGREE = 9696;
    private final int RESPOND_DISAGREE = 881;
    private SwitchPreference g_rms_switch;
    private CharSequence g_pre_title;

    /*public interface data_columns {
        public static final String ID = "_id";
        public static final String NAME = "name";
        public static final String VALUE = "value";
    }*/

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.dmg_setting_rms_switch, null);
        init();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void init() {
        boolean rms_server_state;
        boolean rms_client_state;
        this.g_rms_switch = (SwitchPreference) findPreference(KEY_RMS_SWITCH);
        this.g_rms_switch.setEnabled(true);
        if (get_rms_status("rms_status").contentEquals("0") && get_rms_status("rms_client").contentEquals("2")) {
            if (rms_check_locktime()) {
                this.g_rms_switch.setTitle(R.string.dmg_settings_rms_switch_opening);
                this.g_rms_switch.setSummary(R.string.dmg_settings_rms_switch_opening);
                this.g_rms_switch.setEnabled(false);
            } else {
                this.g_rms_switch.setTitle(R.string.dmg_settings_rms_switch_off);
                this.g_rms_switch.setSummary(R.string.dmg_settings_rms_switch_off);
            }
            this.g_rms_switch.setChecked(false);
        } else {
            if (get_rms_status("rms_status").contentEquals("0") || get_rms_status("rms_status").contentEquals("BuildConfig.FLAVOR")) {
                rms_server_state = false;
            } else {
                rms_server_state = true;
            }
            if (get_rms_status("rms_client").contentEquals("0") || get_rms_status("rms_client").contentEquals("BuildConfig.FLAVOR")) {
                rms_client_state = false;
            } else {
                rms_client_state = true;
            }
            if (rms_server_state && rms_client_state) {
                this.g_rms_switch.setTitle(R.string.dmg_settings_rms_switch_on);
                this.g_rms_switch.setSummary(R.string.dmg_settings_rms_switch_on);
                this.g_rms_switch.setChecked(true);
            } else {
                this.g_rms_switch.setTitle(R.string.dmg_settings_rms_switch_off);
                this.g_rms_switch.setSummary(R.string.dmg_settings_rms_switch_off);
                this.g_rms_switch.setChecked(false);
            }
        }
        this.g_rms_switch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String RMSServer = "0";//TBCRMSSwitchFragment.this.get_rms_status("rms_status");
                String RMSClient = "2";//TBCRMSSwitchFragment.this.get_rms_status("rms_client");
                Log.i(TAG, "RMSServer: " + RMSServer + " RMSClient: " + RMSClient);
                Log.d(TAG, "onPreferenceChange: g_rms_switch.isChecked = " + g_rms_switch.isChecked());
                Log.d(TAG, "onPreferenceChange: o.booleanValue = " + String.valueOf((Boolean) o));
                if ((!RMSServer.contentEquals("0") || !RMSClient.contentEquals("1")) && (!RMSServer.contentEquals("0") || !RMSClient.contentEquals("2"))) {
                    Log.d(TAG, "onPreferenceChange: (!server.equals('0') || !client.equals('1')) && (!server.equals('0') || !client.equals('2'))");
                    if (g_rms_switch.isChecked() != ((Boolean) o).booleanValue()) {
                        if (((Boolean) o).booleanValue()) {
                            set_rms_client(1);
                            g_rms_switch.setTitle(R.string.dmg_settings_rms_switch_on);
                            g_rms_switch.setSummary(R.string.dmg_settings_rms_switch_on);
                        } else {
                            set_rms_client(0);
                            g_rms_switch.setTitle(R.string.dmg_settings_rms_switch_off);
                            g_rms_switch.setSummary(R.string.dmg_settings_rms_switch_off);
                        }
                    }
                    return true;
                }
                if (!g_rms_switch.isChecked()) {
                    show_rms_collect_view();
                }
                return true;
            }
        });
    }

    /*private void init_rms_pref() {
        get_rms_status("rms_client");
        get_rms_status("rms_status");
    }*/

    public String get_rms_status(String type) {
        String ret;
        String[] proj = {"name", "value"};
        String[] selectionArgs = {type};
        Cursor nCursor = getContext().getContentResolver().query(CONTENT_URI, proj, "name=?", selectionArgs, null);
        if (nCursor != null) {
            int cnt = nCursor.getCount();
            if (cnt > 0) {
                nCursor.moveToFirst();
                nCursor.getString(0);
                String value = nCursor.getString(1);
                ret = value;
            } else {
                Log.d(TAG, "get_rms_status: cnt <= 0");
                ret = "BuildConfig.FLAVOR";
            }
            nCursor.close();
        } else {
            Log.d(TAG, "get_rms_status: nCursor == null");
            ret = "BuildConfig.FLAVOR";
        }
        Log.i(TAG, "get_rms_status get test(Server):" + ret);
        return ret;
    }

    public void set_rms_client(int value) {
        String rms = String.valueOf(value);
        Log.i(TAG, "set test:" + rms);
        Intent it = new Intent("com.vasott.acsclient.rms_client_change_by_box");
        it.putExtra("rmsstatus", rms);
        it.setPackage("com.vasott.acsclient");
        if (Build.VERSION.SDK_INT < 19) {
            getContext().sendBroadcast(it);
        } else {
            getContext().sendBroadcastAsUser(it, Process.myUserHandle());
        }
    }

    public void show_rms_collect_view() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(DMGSettingsFragment.PACKAGE_LAUNCHER, ACTIVITY_RMS_COLLECT));
        //intent.putExtra("showCheckBox", true);
        startActivityForResult(intent, REQUEST_NUM);
    }

    private boolean rms_check_locktime() {
        long LcurTime = System.currentTimeMillis() / 1000;
        String StriggerTime = SystemProperties.get(PROPERTY_RMSCLIENT_TRIGGET_TIME, "0");
        if ("0".equals(StriggerTime)) {
            SystemProperties.set(PROPERTY_RMSCLIENT_TRIGGET_TIME, Long.toString(LcurTime));
            Log.d(TAG, "rms_check_locktime: \"0\".equals(StriggerTime) return true");
            return true;
        }
        try {
            long LtriggerTime = Long.parseLong(StriggerTime);
            if (LcurTime - LtriggerTime > ONEDAY_SECOND) {
                Log.d(TAG, "rms_check_locktime: LcurTime - LtriggerTime > ONEDAY_SECOND return false");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            Log.e(TAG, "Parse long fail," + e.toString());
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_NUM) {
            if (resultCode == RESPOND_AGREE) {
                Log.i(TAG, ">>> INTENT_RMS_COLLECT_AGREE");
                this.g_rms_switch.setTitle(R.string.dmg_settings_rms_switch_opening);
                this.g_rms_switch.setSummary(R.string.dmg_settings_rms_switch_opening);
                this.g_rms_switch.setEnabled(false);
            } else if (resultCode == RESPOND_DISAGREE) {
                Log.i(TAG, ">>> INTENT_RMS_COLLECT_DISAGREE");
                this.g_rms_switch.setTitle(R.string.dmg_settings_rms_switch_off);
                this.g_rms_switch.setSummary(R.string.dmg_settings_rms_switch_off);
                this.g_rms_switch.setEnabled(true);
                this.g_rms_switch.setChecked(false);
            }
        }
    }
}
