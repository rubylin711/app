package com.prime.dtv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.prime.dmg.launcher.Home.BlockChannel.BlockedChannel;
import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.Utils.ActivityUtils;
import com.prime.dtv.sysdata.GposInfo;
import com.prime.dtv.utils.LogUtils;

import java.lang.ref.WeakReference;

public class PrimeBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = PrimeBroadcastReceiver.class.getSimpleName();
    private static boolean g_screen_on_flag = true;
    private static boolean g_play_ad = false;
    private static boolean g_check_standby_redirect = false;

    WeakReference<AppCompatActivity> g_ref;

    public interface Callback {
        void on_power_off();
    }
    private static Callback gCallback;

    public PrimeBroadcastReceiver(AppCompatActivity homeActivity) {
        g_ref = new WeakReference<>(homeActivity);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtils.d("action = "+action);
        if (action != null) {
            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                // power, standby and wifi led will be handled by OTAService
//            get().g_dtv.set_power_led(1);
//            get().g_dtv.set_standby_led(0);
                g_screen_on_flag = true;
                g_check_standby_redirect = false;
                if (ActivityUtils.get_top_activity(context).contains("com.netflix.ninja"))
                {
                    g_play_ad = false;
                    LogUtils.d("The Top is Netflix, return");
                    return;
                }
                if ( ActivityUtils.isRunningCtsTest(context) ) {
                    LogUtils.e("[EN0502] test 1111");
                    g_play_ad = false;
                    return;
                }

                // do not start activity if top activity is HomeActivity, or HomeActivity will onResume() twice
                // if top_activity != HomeActivity => standby on -> start Home here -> onNewIntent() -> handle standby redirect
                // if top_activity == HomeActivity => standby on -> system call Home onResume() -> handle standby redirect
                if (!ActivityUtils.get_top_activity(context).equals(HomeActivity.CLS_HOME_ACTIVITY)) {
                    Intent it = new Intent();
                    it.setClassName(HomeActivity.HOME_PACKAGE_NAME, HomeActivity.CLS_HOME_ACTIVITY);
                    it.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    it.putExtra(GposInfo.GPOS_STANDBY_REDIRECT, get().g_dtv.gpos_info_get().getStandbyRedirect());
                    context.startActivity(it);
                }

                PrimeVolumeReceiver.init_volume(context);
                get().get_live_tv_manager().reset_pin_code_status();
            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                if (gCallback != null)
                    gCallback.on_power_off();
                g_screen_on_flag = false;
                g_play_ad = true;
                g_check_standby_redirect = true;
                BlockedChannel blockedChannel = BlockedChannel.get_instance(null,null);
                if(blockedChannel != null)
                    blockedChannel.reset_pin_code_status();
                // power, standby and wifi led will be handled by OTAService
//            get().g_dtv.set_power_led(0);
//            get().g_dtv.set_standby_led(1);
            }
        }
    }
    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    public static boolean get_screen_on_status() {
        return g_screen_on_flag;
    }

    public static boolean get_play_ad_flag() {
        return g_play_ad;
    }
    public static void set_play_ad_flag(boolean flag) {
        g_play_ad = flag;
    }

    public static boolean get_check_standby_redirect() {
        return g_check_standby_redirect;
    }

    public static void set_callback(Callback callback) {
        gCallback = callback;
    }
}
