package com.prime.dmg.launcher.Home.LiveTV.Recommendation;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.prime.dmg.launcher.Home.Recommend.List.RecommendItem;
import com.prime.dmg.launcher.HomeActivity;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.BaseDialog;
import com.prime.dmg.launcher.Utils.JsonParser.AdPage;
import com.prime.dmg.launcher.Utils.JsonParser.AdPageItem;
import com.prime.dtv.utils.TVMessage;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/** @noinspection CommentedOutCode*/
public class RecommendationDialog extends BaseDialog {

    String TAG = getClass().getSimpleName();

    public static int TIMEOUT_DISMISS = 5000;

    WeakReference<AppCompatActivity> g_ref;
    Handler g_handler;
    PopularListAdapter g_popularAdapter;
    AdListAdapter g_adListAdapter;

    public RecommendationDialog(AppCompatActivity activity) {
        super(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        g_ref = new WeakReference<>(activity);
        g_popularAdapter = new PopularListAdapter(this, get_popular_programs());
        g_adListAdapter = new AdListAdapter(this, get_ad_page_items());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.color.trans);
            //getWindow().setWindowAnimations(R.style.Theme_DMGLauncher_DialogAnimation);
            //getWindow().setDimAmount(0);
        }
        setContentView(R.layout.dialog_recommendation);

        update_popular_list();
        update_ad_list();
        reset_timeout_dismiss();
    }

    @Override
    public void onMessage(TVMessage msg) {

    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        reset_timeout_dismiss(); // reset timer
        return super.onKeyDown(keyCode, event);
    }

    public void update_popular_list() {
        PopularListView popularListView = findViewById(R.id.lo_recommendation_view_popular_list);
        popularListView.init_list(this, g_popularAdapter);
    }

    public void update_ad_list() {
        AdListView adListView = findViewById(R.id.lo_recommendation_view_ad_list);
        adListView.init_list(this, g_adListAdapter);
    }

    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    public List<RecommendItem> get_popular_programs() {
        return get().g_listMgr.get_popular_programs();
    }

    public List<AdPageItem> get_ad_page_items() {
        List<AdPage> adPageList = get().g_pagerMgr.get_ad_pages();
        List<AdPageItem> adPageItemList = new ArrayList<>();

        for (AdPage adPage : adPageList) {
            adPageItemList.addAll(adPage.get_items());
        }
        return adPageItemList;
    }

    public void set_intro_time(int introMs) {
        TIMEOUT_DISMISS = introMs;
    }

    public void reset_timeout_dismiss() {
        Log.d(TAG, "reset_timeout_dismiss: ");
        if (null == g_handler)
            g_handler = new Handler(Looper.getMainLooper());
        g_handler.removeCallbacksAndMessages(null);
        g_handler.postDelayed(this::dismiss, TIMEOUT_DISMISS);
    }

    public void show_dialog() {
        View dialog = findViewById(R.id.lo_recommendation_dialog);
        dialog.setVisibility(View.VISIBLE);
    }

    public void hide_dialog() {
        View dialog = findViewById(R.id.lo_recommendation_dialog);
        dialog.setVisibility(View.INVISIBLE);
    }
}
