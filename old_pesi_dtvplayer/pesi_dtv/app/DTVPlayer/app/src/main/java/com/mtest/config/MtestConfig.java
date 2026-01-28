package com.mtest.config;

import android.content.Context;
import android.content.Intent;

import com.mtest.module.PesiSharedPreference;

public class MtestConfig {
    public static final int TEST_RESULT_NONE = 0;
    public static final int TEST_RESULT_PASS = 1;
    public static final int TEST_RESULT_FAIL = 2;
    public static final int TEST_RESULT_WAIT_CARD_OUT = 3;

    // for shared preference put and get
    public static final String KEY_BOOT_COUNT = "BOOT.STABLE.COUNT";
    public static final String KEY_BOOT_PASS_COUNT = "BOOT.STABLE.PASS.COUNT";
    public static final String KEY_USB_STABLE_TOTAL_COUNT = "USB.STABLE.TOTAL.COUNT";
    public static final String KEY_USB_STABLE_FAIL_COUNT = "USB.STABLE.FAIL.COUNT";
    public static final String KEY_EMI = "EMI";
    public static final String KEY_BOOT_STABLE_TEST = "BOOT.STABLE.TEST";
    public static final String KEY_USB_STABLE_TEST = "USB.STABLE.TEST";

    // hidden input
    public static final String HIDDEN_INPUT_HIDDEN_ACTIVITY = "6152";
    public static final String HIDDEN_INPUT_HIDDEN_INPUT_ACTIVITY = "6153";
    public static final String HIDDEN_INPUT_STABLE_TEST_MODE = "6154";
    public static final String HIDDEN_INPUT_EMI = "0205";
    public static final String HIDDEN_INPUT_MTEST_ALL_PASS = "8484";//Scoty 20190417 add key to pass all test item
    public static final String HIDDEN_INPUT_MTEST_DISABLE_OPT = "0243";
    public static final String HIDDEN_INPUT_CLEAR_WIFI = "0303";
    public static final String HIDDEN_INPUT_HW_TEST = "9120"; //hw test config show //gary20200504 add HW test config get from usb
    public static final String HIDDEN_INPUT_HW_COPY_REPORT_FILE = "9122"; //copy hw test report to usb
    public static final String HIDDEN_INPUT_HW_RESET = "9121"; //Reset test flag to factory mode
    public static final String HIDDEN_INPUT_HW_DELETE_REPORT_FILE = "9123"; //delete hw test report
    public static final String HIDDEN_INPUT_HW_SHOW_HELP = "9124"; //show help info
    public static final String HIDDEN_INPUT_ALL_ITEM_SELECTABLE = "0724"; // Johnny 20201202 add hidden input to make all items selectable
    public static final String HIDDEN_INPUT_START_POWER_SAVING = "1853";
    public static final String HIDDEN_INPUT_SWITCH_LOCALE = "0898"; // Johnny 20201211 switch locale by hidden input
    public static final String HIDDEN_INPUT_DURABILITY_TEST = "5555";

    public static boolean isHiddenFunctionEnable(Context context, String key) {
        PesiSharedPreference pesiSharedPreference = new PesiSharedPreference(context);
        return pesiSharedPreference.getBoolean(key, false);  // default false
    }

    public static void setHiddenFunctionEnable(Context context, String key, boolean enable) {
        PesiSharedPreference pesiSharedPreference = new PesiSharedPreference(context);

        pesiSharedPreference.putBoolean(key, enable);

        // extra setups
        if (key.equals(KEY_BOOT_STABLE_TEST)) {
            // clear count when enable/disable
            pesiSharedPreference.putInt(KEY_BOOT_COUNT, 0);
            pesiSharedPreference.putInt(KEY_BOOT_PASS_COUNT, 0);
        }
        else if (key.equals(KEY_USB_STABLE_TEST)) {
            // clear count when enable/disable
            pesiSharedPreference.putInt(KEY_USB_STABLE_TOTAL_COUNT, 0);
            pesiSharedPreference.putInt(KEY_USB_STABLE_FAIL_COUNT, 0);
        }

        pesiSharedPreference.save();
    }

    public static void switchHiddenFunctionEnable(Context context, String key) {
        PesiSharedPreference pesiSharedPreference = new PesiSharedPreference(context);

        if (pesiSharedPreference.getBoolean(key, false)) {   // if has enabled, disable it
            pesiSharedPreference.putBoolean(key, false);
        }
        else {  // if not enabled, enable it
            pesiSharedPreference.putBoolean(key, true);
        }

        // extra setups
        if (key.equals(KEY_BOOT_STABLE_TEST)) {
            // clear count when enable/disable
            pesiSharedPreference.putInt(KEY_BOOT_COUNT, 0);
            pesiSharedPreference.putInt(KEY_BOOT_PASS_COUNT, 0);
        }
        else if (key.equals(KEY_USB_STABLE_TEST)) {
            // clear count when enable/disable
            pesiSharedPreference.putInt(KEY_USB_STABLE_TOTAL_COUNT, 0);
            pesiSharedPreference.putInt(KEY_USB_STABLE_FAIL_COUNT, 0);
        }

        pesiSharedPreference.save();
    }

    // restart Mtest
    public static void restart(Context context) {
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());

        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            context.startActivity(intent);
        }
    }
}
