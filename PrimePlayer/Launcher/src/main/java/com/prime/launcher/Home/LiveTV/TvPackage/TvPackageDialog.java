package com.prime.launcher.Home.LiveTV.TvPackage;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.prime.launcher.HomeActivity;
import com.prime.launcher.R;
import com.prime.launcher.BaseDialog;
import com.prime.launcher.Utils.JsonParser.RecommendPackage;
import com.prime.launcher.Utils.JsonParser.RecommendContent;
import com.prime.datastructure.utils.TVMessage;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/** @noinspection CommentedOutCode*/
public class TvPackageDialog extends BaseDialog {

    String TAG = getClass().getSimpleName();

    public static final int DURATION_CONTENT_LIST_FADE_OUT = 0;
    public static final int DURATION_CONTENT_LIST_FADE_IN = 200;
    public static int TIMEOUT_DISMISS = 5000;

    WeakReference<AppCompatActivity> g_ref;
    Handler g_handler;
    List<RecommendPackage> g_recommendPackages;
    boolean g_isPressKey;

    public TvPackageDialog(AppCompatActivity activity, List<RecommendPackage> recommendPackages) {
        super(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        g_ref = new WeakReference<>(activity);
        g_recommendPackages = recommendPackages;
        g_isPressKey = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(R.color.trans);
            //getWindow().setWindowAnimations(R.style.Theme_Launcher_DialogAnimation);
            //getWindow().setDimAmount(0);
        }
        setContentView(R.layout.dialog_tv_package);
    }

    @Override
    protected void onStart() {
        super.onStart();
        reset_focus_position();

        update_type_list();
        update_content_list();
        reset_timeout_dismiss();
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        TypeListView typeListView = findViewById(R.id.lo_tv_package_type_list);
        ContentListView contentListView = findViewById(R.id.lo_tv_package_content_list);

        reset_timeout_dismiss(); // reset timer

        if (!is_fade_in_finished())
            return true;

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                contentListView.press_key(keyCode);
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP && typeListView.g_position > 0)
                    typeListView.g_position--;
                else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && typeListView.g_position < typeListView.get_count() - 1)
                    typeListView.g_position++;
                typeListView.press_up_down(keyCode, event);
                //g_isPressKey = true;
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    /*@Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        //ContentListView contentListView = findViewById(R.id.lo_bottom_tv_package_content_list);
        TypeListView typeListView = findViewById(R.id.lo_tv_package_type_list);

        if (KeyEvent.KEYCODE_DPAD_UP == keyCode ||
            KeyEvent.KEYCODE_DPAD_DOWN == keyCode) {
            g_isPressKey = false;
            typeListView.press_up_down(keyCode, event);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }*/

    @Override
    public void onMessage(TVMessage msg) {

    }

    public void update_data(List<RecommendPackage> recommendPackages) {
        g_recommendPackages = recommendPackages;
    }

    public void update_type_list() {
        TypeListView typeListView = findViewById(R.id.lo_tv_package_type_list);
        typeListView.init_all(this, get_recommend_packages());
        typeListView.requestFocus();
    }

    public void update_content_list() {
        ContentListView contentListView = findViewById(R.id.lo_tv_package_content_list);
        contentListView.init_all(this, get_recommend_contents());
    }

    public void update_content_list(int pkgIndex) {
        if (pkgIndex < 0 || pkgIndex >= g_recommendPackages.size())
            pkgIndex = 0;
        ContentListView contentListView = findViewById(R.id.lo_tv_package_content_list);
        contentListView.init_all(this, get_recommend_contents(pkgIndex));
    }

    public void set_intro_time(int introMs) {
        TIMEOUT_DISMISS = introMs;
    }

    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    public List<RecommendPackage> get_recommend_packages() {
        return g_recommendPackages;
    }

    public List<RecommendContent> get_recommend_contents() {
        TypeListView typeListView = findViewById(R.id.lo_tv_package_type_list);
        int position = typeListView.get_position();
        if (position < g_recommendPackages.size()) {
            RecommendPackage recommendPkg = g_recommendPackages.get(position);
            return recommendPkg.get_channel_list();
        }
        return new ArrayList<>();
    }

    public List<RecommendContent> get_recommend_contents(int position) {
        if (position < g_recommendPackages.size()) {
            RecommendPackage recommendPkg = g_recommendPackages.get(position);
            return recommendPkg.get_channel_list();
        }
        return new ArrayList<>();
    }

    public boolean is_press_key() {
        return g_isPressKey;
    }

    public boolean is_fade_in_finished() {
        ContentListView contentListView = findViewById(R.id.lo_tv_package_content_list);
        return contentListView.is_fade_in_finished();
    }

    public boolean interrupt_bind_view() {
        return is_press_key();
    }

    public void reset_focus_position() {
        TypeListView typeListView = findViewById(R.id.lo_tv_package_type_list);
        if (null == typeListView)
            return;
        // update first content
        typeListView.g_previousPkgIndex = -1;
        typeListView.scroll_to_top(true);
        // focus first type
        typeListView.focus_first_item();
        reset_timeout_dismiss();
    }

    public void reset_timeout_dismiss() {
        Log.d(TAG, "reset_timeout_dismiss: ");
        if (null == g_handler)
            g_handler = new Handler(Looper.getMainLooper());
        g_handler.removeCallbacksAndMessages(null);
        g_handler.postDelayed(this::dismiss, TIMEOUT_DISMISS);
    }
}
