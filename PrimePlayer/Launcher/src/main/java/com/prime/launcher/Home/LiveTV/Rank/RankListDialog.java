package com.prime.launcher.Home.LiveTV.Rank;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.prime.launcher.HomeActivity;
import com.prime.launcher.R;
import com.prime.launcher.Utils.JsonParser.RankInfo;

import java.lang.ref.WeakReference;
import java.util.List;

public class RankListDialog extends Dialog {

    String TAG = getClass().getSimpleName();

    public static int TIMEOUT_DISMISS = 5000;
    
    WeakReference<AppCompatActivity> g_ref;
    Handler g_handler;
    List<RankInfo> g_rankInfos;

    public RankListDialog(AppCompatActivity activity, List<RankInfo> rankInfos) {
        super(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        g_ref = new WeakReference<>(activity);
        g_rankInfos = rankInfos;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.color.trans);
        //getWindow().setWindowAnimations(R.style.Theme_Launcher_DialogAnimation);
        //getWindow().setDimAmount(0);
        setContentView(R.layout.dialog_rank_list);

        RankListView rankListView = findViewById(R.id.lo_live_tv_rank_list);
        rankListView.init_all(this, get_rank_infos());

        reset_timeout_dismiss();
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        reset_timeout_dismiss();
        return super.onKeyDown(keyCode, event);
    }

    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    public List<RankInfo> get_rank_infos() {
        return g_rankInfos;
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
}
