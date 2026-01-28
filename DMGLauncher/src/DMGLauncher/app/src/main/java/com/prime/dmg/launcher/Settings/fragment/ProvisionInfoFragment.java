package com.prime.dmg.launcher.Settings.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.leanback.preference.LeanbackPreferenceFragmentCompat;
import androidx.preference.Preference;

import com.prime.dmg.launcher.HomeApplication;
import com.prime.dmg.launcher.R;
import com.prime.dtv.PrimeDtv;
import com.prime.dtv.sysdata.GposInfo;
import com.prime.dtv.utils.LogUtils;

public class ProvisionInfoFragment extends LeanbackPreferenceFragmentCompat {

    private static final String TAG = "ProvisionInfoFragment";
    private static final String KEY_SO = "persist.sys.prime.so";
    private static final String KEY_HOME_ID = "persist.sys.prime.home_id";
    private static final String KEY_NIT = "persist.sys.prime.nit";
    private static final String KEY_SI_NIT = "persist.sys.prime.si_nit";
    private static final String KEY_MOBILE = "persist.sys.prime.mobile";
    private static final String KEY_BAT_ID = "persist.sys.prime.bat_id";
    private static final String KEY_ZIP_CODE = "persist.sys.prime.zip_code";

    PrimeDtv primeDtv;
    GposInfo gposInfo;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.fragment_provision_info, null);

        // Get GPOS info
        primeDtv = HomeApplication.get_prime_dtv();
        if (primeDtv != null)
            gposInfo = primeDtv.gpos_info_get();

        // SO
        Preference preference_SO = findPreference(KEY_SO);
        if (preference_SO != null) {
            preference_SO.setTitle(R.string.provision_info_SO);
            preference_SO.setSummary(value_SO());
        }

        // Home ID
        Preference preference_Home_ID = findPreference(KEY_HOME_ID);
        if (preference_Home_ID != null) {
            preference_Home_ID.setTitle(R.string.provision_info_Home_ID);
            preference_Home_ID.setSummary(value_Home_ID());
        }

        // NIT
        Preference preference_NIT = findPreference(KEY_NIT);
        if (preference_NIT != null) {
            preference_NIT.setTitle(R.string.provision_info_NIT);
            preference_NIT.setSummary(value_NIT());
        }
        // SI NIT
        Preference preference_SI_NIT = findPreference(KEY_SI_NIT);
        if (preference_SI_NIT != null) {
            preference_SI_NIT.setTitle(R.string.provision_info_SI_NIT);
            preference_SI_NIT.setSummary(value_SI_NIT());
        }
        // Mobile
        Preference preference_MOBILE = findPreference(KEY_MOBILE);
        if (preference_MOBILE != null) {
            preference_MOBILE.setTitle(R.string.provision_info_MOBILE);
            preference_MOBILE.setSummary(value_MOBILE());
        }

        // BAT ID
        Preference preference_BAT_ID = findPreference(KEY_BAT_ID);
        if (preference_BAT_ID != null) {
            preference_BAT_ID.setTitle(R.string.provision_info_BAT_ID);
            preference_BAT_ID.setSummary(value_BAT_ID());
        }

        // ZIP CODE
        Preference preference_ZIP_CODE = findPreference(KEY_ZIP_CODE);
        if (preference_ZIP_CODE != null) {
            preference_ZIP_CODE.setTitle(R.string.provision_info_ZIP_CODE);
            preference_ZIP_CODE.setSummary(value_ZIP_CODE());
        }
    }

    private String value_SO() {
        if (gposInfo == null || gposInfo.getSo() == null || gposInfo.getSo().isEmpty())
            return "NULL";
        return gposInfo.getSo().toUpperCase();
    }

    private String value_Home_ID() {
        if (gposInfo == null || gposInfo.getHomeId() == null || gposInfo.getHomeId().isEmpty())
            return "NULL";
        return gposInfo.getHomeId().toUpperCase();
    }

    private String value_NIT() {
        if (gposInfo == null)
            return "NULL";
        return String.valueOf(gposInfo.getNitId()).toUpperCase();
    }

    private String value_SI_NIT() {
        if (gposInfo == null)
            return "NULL";
        return String.valueOf(gposInfo.getSINitNetworkId()).toUpperCase();
    }

    private String value_MOBILE() {
        if (gposInfo == null || gposInfo.getMobile() == null || gposInfo.getMobile().isEmpty())
            return "NULL";
        LogUtils.d(" "+gposInfo.getMobile().toUpperCase());
        return gposInfo.getMobile().toUpperCase();
    }

    private String value_BAT_ID() {
        if (gposInfo == null)
            return "NULL";
        return String.valueOf(gposInfo.getBatId()).toUpperCase();
    }

    private String value_ZIP_CODE() {
        if (gposInfo == null || gposInfo.getZipCode() == null || gposInfo.getZipCode().isEmpty())
            return "NULL";
        return gposInfo.getZipCode().toUpperCase();
    }
}
