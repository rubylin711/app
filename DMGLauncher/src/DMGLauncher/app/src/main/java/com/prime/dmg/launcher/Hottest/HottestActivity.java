package com.prime.dmg.launcher.Hottest;

import static com.prime.dmg.launcher.HomeActivity.ACTION_UPDATE_HOT_VIDEO;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.prime.dmg.launcher.ACSDatabase.ACSDataProviderHelper;
import com.prime.dmg.launcher.BaseActivity;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Utils.JsonParser.AppPackage;
import com.prime.dmg.launcher.Utils.JsonParser.JsonParser;
import com.prime.dtv.utils.TVMessage;

import java.util.List;

public class HottestActivity extends BaseActivity {

    String TAG = getClass().getSimpleName();

    List<AppPackage> g_appPackages;
    BroadcastReceiver g_updateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hottest);

        g_appPackages = get_hot_video();
        init_app_list();
        init_video_list();
        register_receiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregister_receiver();
    }

    @Override
    public void onBroadcastMessage(Context context, Intent intent) {
        super.onBroadcastMessage(context, intent);
    }

    @Override
    public void onMessage(TVMessage msg) {
        super.onMessage(msg);
    }

    public void init_app_list() {
        AppListView appListView = findViewById(R.id.lo_hottest_app_list);
        appListView.init(this, g_appPackages);
    }

    public void init_video_list() {
        VideoListView videoListView = findViewById(R.id.lo_hottest_video_list);
        videoListView.init(this);
    }

    public List<AppPackage> get_hot_video() {
        String json = ACSDataProviderHelper.get_acs_provider_data(this, "hot_video");
        List<AppPackage> appPackageList = JsonParser.parse_hot_video(json);
        Log.d(TAG, "get_hot_video: [app package size] " + appPackageList.size());
        return appPackageList;
    }

    public void register_receiver() {
        g_updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (ACTION_UPDATE_HOT_VIDEO.equals(action))
                    update_hot_video();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATE_HOT_VIDEO);
        LocalBroadcastManager.getInstance(this).registerReceiver(g_updateReceiver, filter);
    }

    public void unregister_receiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(g_updateReceiver);
    }

    public void update_hot_video() {
        AppListView appListView = findViewById(R.id.lo_hottest_app_list);

        g_appPackages = get_hot_video();
        appListView.update_hot_video(g_appPackages);
    }

    public void block_detail_focus() {
        VideoListView videoListView = findViewById(R.id.lo_hottest_video_list);
        Button purchaseBtn = findViewById(R.id.lo_hottest_purchase_btn);
        videoListView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        purchaseBtn.setFocusable(false);
    }

    public void unblock_detail_focus() {
        VideoListView videoListView = findViewById(R.id.lo_hottest_video_list);
        Button purchaseBtn = findViewById(R.id.lo_hottest_purchase_btn);
        videoListView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        purchaseBtn.setFocusable(true);
    }
}