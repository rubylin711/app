package com.prime.dmg.launcher.Home.Recommend.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.prime.dmg.launcher.BaseActivity;
import com.prime.dmg.launcher.Home.Recommend.List.ListManager;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Home.Recommend.List.RecommendListView;
import com.prime.dtv.PrimeAppReceiver;
import com.prime.dtv.utils.TVMessage;

public class SideMenuActivity extends BaseActivity implements PrimeAppReceiver.Callback {

    private static final String TAG = SideMenuActivity.class.getSimpleName();

    public static final String APP_NAME = "ALL APPs";

    RecommendListView g_rcvAppsGames;
    RecommendListView g_rcvApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_side_menu);

        init_list_view();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PrimeAppReceiver.register_side_menu_callback(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PrimeAppReceiver.unregister_side_menu_callback();
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //g_rcvAppsGames.key_down(keyCode);
        //g_rcvApps.key_down(keyCode);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onMessage(TVMessage msg) {
        super.onMessage(msg);
    }

    @Override
    public void onBroadcastMessage(Context context, Intent intent) {
        super.onBroadcastMessage(context, intent);
    }

    @Override
    public void on_app_install(String pkgName) {
        Log.i(TAG, "on_app_install: for Side Menu, [pkgName] " + pkgName);
        g_rcvAppsGames.add_local_app(pkgName);
    }

    @Override
    public void on_app_uninstall(String pkgName) {
        Log.i(TAG, "on_app_uninstall: for Side Menu, [pkgName] " + pkgName);
        g_rcvAppsGames.remove_local_app(pkgName);
    }

    public void init_list_view() {
        // Apps & Games
        g_rcvAppsGames = findViewById(R.id.lo_side_menu_app_game_grid);
        g_rcvAppsGames.init_list_view(this, ListManager.g_localApps);
        // Applications
        g_rcvApps = findViewById(R.id.lo_side_menu_app_list);
        g_rcvApps.init_list_view(this, ListManager.g_recommendApps);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_to_left, R.anim.slide_out_to_right);
    }
}
