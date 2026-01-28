package com.prime.dmg.launcher.Settings.fragment;

import static com.prime.dmg.launcher.Settings.TimeLockActivity.TIME_LOCK_PERIOD;
import static com.prime.dmg.launcher.Settings.UnlockChannelActivity.DMG_SETTINGS_REQUEST_CODE;
import static com.prime.dmg.launcher.Settings.fragment.DMGSettingsFragment.PACKAGE_LAUNCHER;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.leanback.preference.LeanbackPreferenceFragmentCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.prime.dmg.launcher.HomeApplication;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Settings.UnlockChannelActivity;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.service.datamanager.FavGroup;
import com.prime.dtv.sysdata.EnTableType;
import com.prime.dtv.sysdata.GposInfo;
import com.prime.dtv.sysdata.MiscDefine;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.utils.LogUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChannelLockFragment extends LeanbackPreferenceFragmentCompat {
    private static final String TAG = "ChannelLockFragment";
    public static final String ACTIVITY_PASSWORD = "com.prime.dmg.launcher.Settings.PasswordActivity";
    public static final String ACTIVITY_CHANNEL_LOCK = "com.prime.dmg.launcher.Settings.ChannelLockActivity";
    public static final String ACTIVITY_TIME_LOCK = "com.prime.dmg.launcher.Settings.TimeLockActivity";

    private static final String KEY_CHANNEL = "key_channel";
    private static final String KEY_CHANNEL_LOCK = "key_channel_lock";
    private static final String KEY_PARENTAL = "key_parental";
    private static final String KEY_PASSWORD = "key_password";
    private static final String KEY_TIME = "key_time";
    private static final int RATING_LEVEL_0_INDEX  = 4;
    private static final int RATING_LEVEL_6_INDEX  = 0;
    private static final int RATING_LEVEL_12_INDEX = 1;
    private static final int RATING_LEVEL_15_INDEX = 2;
    private static final int RATING_LEVEL_18_INDEX = 3;

    private Preference g_channel_lock;
    private PreferenceScreen g_channel_lock_screen;
    private ListPreference g_parental_lock;
    private Preference g_set_password;
    private Preference g_time_lock;

    private boolean g_enable = false;
    private CharSequence g_pre_title;
    private PrimeDtv g_prime_dtv;

    //time lock
    private List<String> g_lock_channel_by_time;
    private List<Integer> g_time_lock_index;

    public static ChannelLockFragment newInstance() {
        return new ChannelLockFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        g_prime_dtv = HomeApplication.get_prime_dtv();
    }

    @Override
    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        super.setPreferenceScreen(preferenceScreen);

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.channel_lock, null);
        Log.d(TAG, "onCreatePreferences" );

        start_activity_for_result(UnlockChannelActivity.ACTIVITY_UNLOCK_CHANNEL);
        g_enable = false;
        init_disable();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DMG_SETTINGS_REQUEST_CODE && resultCode == -1 && data != null) {
            Log.d(TAG, "get result = " + data.getBooleanExtra("KEY_PASS_RESULT", false));
            //        + " password = " + data.getStringExtra("KEY_PASS_TEXT") );

            g_enable = true;
            init();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (g_enable) {
            g_channel_lock.setSummary(get_lock_channel_count() + " " + getResources().getString(R.string.dmg_settings_channel_lock_channel_lock_count_no_number));

            g_lock_channel_by_time = get_lock_channel_by_time();
            Log.d(TAG, "onResume: g_lock_channel_by_time = " + g_lock_channel_by_time);
            Log.d(TAG, "onResume: g_time_lock_index = " + g_time_lock_index);
            if (g_lock_channel_by_time.size() >= 3)
                g_time_lock.setTitle(R.string.dmg_settings_channel_lock_time_lock_full);
            else
                g_time_lock.setTitle(R.string.dmg_settings_channel_lock_time_lock);

            if (g_time_lock != null)
                update_lock_channel_by_time();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        g_prime_dtv.save_table(EnTableType.GPOS);
    }

    private void init() {
        Log.d(TAG, "init ");
        g_channel_lock_screen = (PreferenceScreen) findPreference(KEY_CHANNEL_LOCK); //g_channel_lock_screen
        for (int i = 0; i < g_channel_lock_screen.getPreferenceCount(); i++) {
            g_channel_lock_screen.getPreference(i).setEnabled(true);
        }

        set_parental_lock();

        g_channel_lock = findPreference(KEY_CHANNEL);
        g_channel_lock.setTitle(R.string.dmg_settings_channel_lock_channel_lock);
        g_channel_lock.setOrder(1);
        g_channel_lock.setSummary(get_lock_channel_count() + " " +getResources().getString(R.string.dmg_settings_channel_lock_channel_lock_count_no_number));

        g_time_lock = findPreference(KEY_TIME);
        g_time_lock.setTitle(R.string.dmg_settings_channel_lock_time_lock);
        g_time_lock.setOrder(2);

        g_set_password = findPreference(KEY_PASSWORD);
        g_set_password.setTitle(R.string.dmg_settings_channel_lock_set_password);
        g_set_password.setOrder(3);
    }

    private void init_disable() {
        Log.d(TAG, "init_disable" );
        g_channel_lock_screen = (PreferenceScreen) findPreference(KEY_CHANNEL_LOCK);
        for (int i = 0; i < g_channel_lock_screen.getPreferenceCount(); i++) {
            if (i == 1) {
                //此時拿到的為mChannelLock = findPreference(KEY_CHANNEL);
                g_channel_lock_screen.getPreference(i).setTitle(R.string.dmg_settings_channel_lock_password_hint);
                g_channel_lock_screen.getPreference(i).setSummary(R.string.dmg_settings_channel_lock_password_hint_detail);
            } else {
                g_channel_lock_screen.getPreference(i).setTitle("");
                g_channel_lock_screen.getPreference(i).setSummary("");
                g_channel_lock_screen.getPreference(i).setEnabled(false);
            }
        }

        g_parental_lock = (ListPreference) findPreference(KEY_PARENTAL);
        g_parental_lock.setOrder(1);
        g_channel_lock = findPreference(KEY_CHANNEL);
        g_channel_lock.setOrder(0);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        switch (preference.getKey()) {
            case KEY_CHANNEL:
                Log.i(TAG, "g_channel_lock on click");
                if (g_enable)
                    start_activity(ACTIVITY_CHANNEL_LOCK);
                else
                    start_activity_for_result(UnlockChannelActivity.ACTIVITY_UNLOCK_CHANNEL);
                break;
            case KEY_TIME:
                Log.i(TAG, "g_channel_lock on click");
                g_lock_channel_by_time = get_lock_channel_by_time();
                if (g_lock_channel_by_time.size() >= 3) {
                    Log.i(TAG, "onPreferenceTreeClick: you can't add more 3 time lock");
                    return true;
                }
                start_activity(ACTIVITY_TIME_LOCK, -1, null);
                break;
            case KEY_PASSWORD:
                Log.i(TAG, "g_set_password on click");
                start_activity(ACTIVITY_PASSWORD);
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }

    public int get_data_from_gpos(String dataName) {
        Log.d(TAG, "get_data_from_gpos dataName = " + dataName);
        GposInfo gposInfo = g_prime_dtv.gpos_info_get();

        if (dataName.equals(KEY_PARENTAL))
            return gposInfo.getParentalRate();
        else if (dataName.equals(KEY_CHANNEL))
            return gposInfo.getChannelLockCount();
        return -1;
    }

    public void set_data_to_gpos(String keyName, int dataValue) {
        Log.d(TAG, "set_data_to_gpos keyName = " + keyName + " dataValue = " + dataValue);

        GposInfo gposInfo = g_prime_dtv.gpos_info_get();
        if (keyName.equals(KEY_PARENTAL)) {
            gposInfo.setParentalRate(dataValue);
        }

    }

    private void set_parental_lock() {
        g_parental_lock = (ListPreference) findPreference(KEY_PARENTAL);
        g_parental_lock.setOrder(0);
        g_parental_lock.setTitle(R.string.dmg_settings_channel_lock_parental_lock);
        int parental = get_data_from_gpos(KEY_PARENTAL);
        Log.d(TAG, "set_parental_lock parental = " + parental);
        switch (parental) {
            case GposInfo.PARENTALRATE_SIX:
                g_parental_lock.setSummary(R.string.dmg_settings_channel_lock_parental_PG);
                g_parental_lock.setValue("1");
                break;
            case GposInfo.PARENTALRATE_TWELVE:
                g_parental_lock.setSummary(R.string.dmg_settings_channel_lock_parental_PG_13);
                g_parental_lock.setValue("2");
                break;
            case GposInfo.PARENTALRATE_SIXTEEN:
                g_parental_lock.setSummary(R.string.dmg_settings_channel_lock_parental_R);
                g_parental_lock.setValue("3");
                break;
            case GposInfo.PARENTALRATE_EIGHTEEN:
                g_parental_lock.setSummary(R.string.dmg_settings_channel_lock_parental_NC_17);
                g_parental_lock.setValue("4");
                break;
            case GposInfo.PARENTALRATE_DISABLE:
                g_parental_lock.setSummary("");
                g_parental_lock.setValue("5");
                break;
        }

        g_parental_lock.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                int index = g_parental_lock.findIndexOfValue(o.toString());
                if (index != -1) {
                    switch (index) {
                        case RATING_LEVEL_6_INDEX:
                            g_parental_lock.setSummary(R.string.dmg_settings_channel_lock_parental_PG);
                            g_parental_lock.setValue("1");
                            break;
                        case RATING_LEVEL_12_INDEX:
                            g_parental_lock.setSummary(R.string.dmg_settings_channel_lock_parental_PG_13);
                            g_parental_lock.setValue("2");
                            break;
                        case RATING_LEVEL_15_INDEX:
                            g_parental_lock.setSummary(R.string.dmg_settings_channel_lock_parental_R);
                            g_parental_lock.setValue("3");
                            break;
                        case RATING_LEVEL_18_INDEX:
                            g_parental_lock.setSummary(R.string.dmg_settings_channel_lock_parental_NC_17);
                            g_parental_lock.setValue("4");
                            break;
                        case RATING_LEVEL_0_INDEX:
                            g_parental_lock.setSummary("");
                            g_parental_lock.setValue("5");
                            break;
                    }
                    update_parental_grading(index);
                    return true;
                }
                return true;
            }
        });
    }

    private void update_parental_grading(int index) {
        Log.d(TAG, "update_parental_grading index = " + index);
        switch (index) {
            case RATING_LEVEL_6_INDEX:
                set_data_to_gpos(KEY_PARENTAL, GposInfo.PARENTALRATE_SIX);
                return;
            case RATING_LEVEL_12_INDEX:
                set_data_to_gpos(KEY_PARENTAL, GposInfo.PARENTALRATE_TWELVE);
                return;
            case RATING_LEVEL_15_INDEX:
                set_data_to_gpos(KEY_PARENTAL, GposInfo.PARENTALRATE_SIXTEEN);
                return;
            case RATING_LEVEL_18_INDEX:
                set_data_to_gpos(KEY_PARENTAL, GposInfo.PARENTALRATE_EIGHTEEN);
                return;
            case RATING_LEVEL_0_INDEX:
                set_data_to_gpos(KEY_PARENTAL, GposInfo.PARENTALRATE_DISABLE);
                return;
            default:
                return;
        }
    }

    private void start_activity(String targetActivity) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(PACKAGE_LAUNCHER, targetActivity));
        startActivity(intent);
    }

    private void start_activity(String targetActivity, int index, String timeLockPeriod) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(PACKAGE_LAUNCHER, targetActivity));
        intent.putExtra("index", index);
        intent.putExtra(TIME_LOCK_PERIOD, timeLockPeriod);
        startActivity(intent);
    }

    private void start_activity_for_result(String targetActivity) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(DMGSettingsFragment.PACKAGE_LAUNCHER, targetActivity));
        intent.putExtra("KEY_LOCK_TYPE", 0);
        startActivityForResult(intent, DMG_SETTINGS_REQUEST_CODE);
    }

    private int get_lock_channel_count() {
        return get_data_from_gpos(KEY_CHANNEL);
    }

;
    private List<String> get_lock_channel_by_time() {
        GposInfo gposInfo = g_prime_dtv.gpos_info_get();
        List<String> listString = new ArrayList<>();
        g_time_lock_index  = new ArrayList<>();

        String tempStringStart, tempStringEnd;
        for (int i = 0; i < 3; i++) {
            tempStringStart = parse_time_lock_int_to_string(gposInfo.getTimeLockPeriodStart(i));
            tempStringEnd = parse_time_lock_int_to_string(gposInfo.getTimeLockPeriodEnd(i));
            if (tempStringStart != null && tempStringEnd != null) {
                listString.add(tempStringStart+"-"+tempStringEnd);
                g_time_lock_index.add(i);
            }
        }
        return listString;
    }

    private void update_lock_channel_by_time() {
        while (g_channel_lock_screen.getPreferenceCount() > 4)
            g_channel_lock_screen.removePreference(g_channel_lock_screen.getPreference(3));
        if (g_lock_channel_by_time.size() == 0)
            return;
        for (int i = 0; i < g_lock_channel_by_time.size(); i++) {
            Preference preFerence = new Preference(getPreferenceManager().getContext());
            preFerence.setTitle(getResources().getString(R.string.dmg_settings_channel_lock_time_lock_detail) + ": " + g_lock_channel_by_time.get(i));
            String lockChannelByTime = get_lock_channel_by_time(g_time_lock_index.get(i));
            String[] tmpString = lockChannelByTime.split("=");

            String LockChannelCount = tmpString[0] + " " + getResources().getString(R.string.dmg_settings_channel_lock_channel_lock_count_no_number);;
            String LockChannelList;
            if (tmpString.length > 1)
                LockChannelList = " (" + tmpString[1] + ")";
            else
                LockChannelList = " ()";

            //Log.d(TAG, "update_lock_channel_by_time: LockChannelCount = " + LockChannelCount + " LockChannelList = " + LockChannelList);
            preFerence.setSummary("    " + LockChannelCount + LockChannelList);
            preFerence.setKey("key_time_" + String.valueOf(i));
            preFerence.setIconSpaceReserved(false);
            preFerence.setOrder(i + 3);

            final int index = g_time_lock_index.get(i);
            preFerence.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    int currentIndex = g_time_lock_index.indexOf(index);
                    Log.d(TAG, "onPreferenceClick: current index = " + currentIndex + ", index = " + index);
                    start_activity(ACTIVITY_TIME_LOCK, index, g_lock_channel_by_time.get(currentIndex));
                    return true;
                }
            });
            g_channel_lock_screen.addPreference(preFerence);
            g_set_password = findPreference(KEY_PASSWORD);
            g_set_password.setOrder(i + 4);
        }
    }

    @SuppressLint("DefaultLocale")
    private String parse_time_lock_int_to_string(int time) {
        if (time == -1)
            return null;

        StringBuilder timeLockStingBuffer = new StringBuilder(String.format("%04d", Integer.valueOf(time)));
        timeLockStingBuffer.insert(2, ":");
        return timeLockStingBuffer.toString();
    }

    private String get_lock_channel_by_time(int index) {
        List<ProgramInfo> allChannels, TypeTvChannels, TypeRadioChannels;
        TypeTvChannels    = g_prime_dtv.get_program_info_list(FavGroup.ALL_TV_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.POS_ALL);
        LogUtils.d("TV number: "+ TypeTvChannels.size());
        TypeRadioChannels = g_prime_dtv.get_program_info_list(FavGroup.ALL_RADIO_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.POS_ALL);
        LogUtils.d("Radio number: "+ TypeRadioChannels.size());

        allChannels = new ArrayList<>();
        allChannels.addAll(TypeTvChannels);
        allChannels.addAll(TypeRadioChannels);
        allChannels.sort(Comparator.comparingInt(ProgramInfo::getDisplayNum));

        StringBuilder channelLockList = new StringBuilder();
        int channelLockCount = 0;
        for (int i = 0; i < allChannels.size(); i ++) {
            if (allChannels.get(i).getTimeLockFlag(index) == 1) {
                if (channelLockCount == 0)
                    channelLockList.append(allChannels.get(i).getDisplayNum());
                else
                    channelLockList.append(",").append(allChannels.get(i).getDisplayNum());
                channelLockCount++;
            }
        }

        Log.d(TAG, "get_lock_channel_by_time: " + channelLockCount + " = " + channelLockList);
        return channelLockCount + "=" + channelLockList;
    }
}
