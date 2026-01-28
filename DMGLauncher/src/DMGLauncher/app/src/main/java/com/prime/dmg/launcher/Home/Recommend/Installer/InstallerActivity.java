package com.prime.dmg.launcher.Home.Recommend.Installer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.prime.dmg.launcher.BaseActivity;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Utils.PackageUtils;
import com.prime.dtv.utils.TVMessage;

public class InstallerActivity extends BaseActivity {

    static String TAG = "InstallerActivity";

    public static final String KEY_APP_NAME = "KEY_APP_NAME";
    public static final String KEY_APP_PATH = "KEY_APP_PATH";
    public static final String KEY_PKG_NAME = "KEY_PKG_NAME";
    public static final String KEY_ICON_URL = "KEY_ICON_URL";
    public static final String KEY_FULL_TEXT = "KEY_FULL_TEXT";
    public static final String KEY_VERSIONCODE = "KEY_VERSIONCODE";
    public static final String KEY_DESCRIPTION = "KEY_DESCRIPTION";
    public static final String KEY_FORCE_UPDATE = "KEY_FORCE_UPDATE";
    public static final String KEY_SCREEN_SHOTS = "KEY_SCREEN_SHOTS";

    public static final int MSG_APP_INSTALLED = 1000;
    public static final int DOWNLOAD_DELAY_MS = 5000;
    public static final boolean FLAG_FOR_TEST = false;

    private static Handler g_handler;
    private InstallData g_installData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install);

        TAG = getClass().getSimpleName();
        init_install_data(getIntent());
        init_page_view();
        init_handler();
    }

    @Override
    public void onBroadcastMessage(Context context, Intent intent) {
        super.onBroadcastMessage(context, intent);
    }

    @Override
    public void onMessage(TVMessage msg) {
        super.onMessage(msg);
    }

    public void on_app_installed() {
        PageListView pageListView = findViewById(R.id.lo_page_list_view);
        PageListAdapter adapter = (PageListAdapter) pageListView.getAdapter();

        if (null == adapter)
            return;

        Log.d(TAG, "on_app_installed: hide progress & button");
        adapter.set_progress(PageListAdapter.PROGRESS_INSTALLED);
        adapter.set_first_button(R.string.detail_open);
        adapter.hide_open_button();
    }

    public void init_install_data(Intent intent) {
        if (null == intent) {
            Log.w(TAG, "init_install_data: intent is null");
            finish();
            return;
        }

        // for INSTALL or UPDATE
        if (null == g_installData)
            g_installData = new InstallData();
        g_installData.g_Title = intent.getStringExtra(KEY_APP_NAME);
        g_installData.g_AppPath = intent.getStringExtra(KEY_APP_PATH);
        g_installData.g_IconUrl = intent.getStringExtra(KEY_ICON_URL);
        g_installData.g_PkgName = intent.getStringExtra(KEY_PKG_NAME);
        g_installData.g_FullText = intent.getStringExtra(KEY_FULL_TEXT);
        g_installData.g_Description = intent.getStringExtra(KEY_DESCRIPTION);
        g_installData.g_VersionCode = Long.parseLong(intent.getStringExtra(KEY_VERSIONCODE));
        g_installData.g_ForceUpdate = intent.getBooleanExtra(KEY_FORCE_UPDATE, false);
        g_installData.g_ScreenCaptures = intent.getStringArrayListExtra(KEY_SCREEN_SHOTS);
        g_installData.g_isInstalled = PackageUtils.is_installed(this, g_installData.getPkgName());
        g_installData.g_isUpdate = PackageUtils.is_update(this, g_installData);

        if (FLAG_FOR_TEST) { // for DEBUG
            g_installData.g_PkgName = "org.videolan.vlc";
            g_installData.g_VersionCode = 13030405;
            g_installData.g_ForceUpdate = false;
        }

        Log.d(TAG, "init_install_data: [app name] " + g_installData.getAppName() +
                ", [installed] " + g_installData.isInstalled() +
                ", [is update] " + g_installData.isUpdate() +
                ", [force update] " + g_installData.isForceUpdate());
    }

    public void init_page_view() {
        PageListView pageListView = findViewById(R.id.lo_page_list_view);
        pageListView.init_list(this, g_installData);
    }

    public void init_handler() {
        g_handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
        switch (msg.what) {
        case MSG_APP_INSTALLED:
            on_app_installed();
            break;
        }}};
    }

    public static Handler get_handler() {
        return g_handler;
    }

    /*public void set_progress(int state) {
        PageListView pageListView = findViewById(R.id.lo_page_list_view);
        PageListAdapter adapter = (PageListAdapter) pageListView.getAdapter();
        if (null == adapter)
            return;
        adapter.set_progress(state);
    }

    public void set_first_button(int textId) {
        PageListView pageListView = findViewById(R.id.lo_page_list_view);
        PageListAdapter adapter = (PageListAdapter) pageListView.getAdapter();
        if (null == adapter)
            return;
        adapter.set_first_button(textId);
    }*/

    public static void send_message(int what) {
        if (null == get_handler())
            return;
        get_handler().sendEmptyMessage(what);
        BaseActivity.send_message(what);
    }
}
