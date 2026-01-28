package com.prime.dmg.launcher.Home.Recommend;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.http.SslError;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.prime.dmg.launcher.HomeActivity;

import java.lang.ref.WeakReference;

public class BaseWebView extends WebView {

    String TAG = getClass().getSimpleName();

    public static final String URL_ABOUT_BLANK = "about:blank";
    public static final int ANIMATE_DURATION = 500;

    WeakReference<AppCompatActivity> g_ref;

    public interface Callback {
        void on_close_web_view();
    }
    Callback g_callback;

    public BaseWebView(@NonNull Context context) {
        super(context);
        init_web_view();
    }

    public BaseWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init_web_view();
    }

    public BaseWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init_web_view();
    }

    public BaseWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init_web_view();
    }

    public BaseWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(context, attrs, defStyleAttr, privateBrowsing);
        init_web_view();
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void init_web_view() {
        g_ref = new WeakReference<>((AppCompatActivity) getContext());
        WebSettings webSettings = getSettings();
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setUseWideViewPort(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setTextZoom(100);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setSavePassword(false);
        webSettings.setUserAgentString("DMGLauncher");
        webSettings.setSupportZoom(false);

        removeJavascriptInterface("searchBoxJavaBridge_");
        removeJavascriptInterface("accessibility");
        removeJavascriptInterface("accessibilityTraversal");

        WebViewClient webViewClient = new WebViewClient();
        setWebViewClient(webViewClient);
        loadUrl(URL_ABOUT_BLANK);
        animate().setDuration(0).alpha(0).start();
    }

    public HomeActivity get() {
        return (HomeActivity) g_ref.get();
    }

    public void set_callback(Callback callback) {
        g_callback = callback;
    }

    public boolean is_visible() {
        return VISIBLE == getVisibility();
    }

    public void fade_in() {
        animate().setDuration(ANIMATE_DURATION)
                .alpha(1)
                .start();
    }

    public void fade_out() {
        animate().setDuration(ANIMATE_DURATION)
                .alpha(0)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(@NonNull Animator animation) {}
                    @Override
                    public void onAnimationEnd(@NonNull Animator animation) {
                        setVisibility(View.INVISIBLE);
                        animate().setListener(null);
                    }
                    @Override
                    public void onAnimationCancel(@NonNull Animator animation) {}
                    @Override
                    public void onAnimationRepeat(@NonNull Animator animation) {}
                })
                .start();
    }

    public boolean control_web_view(KeyEvent keyEvent) {
        if (HomeActivity.ENABLE_WEBVIEW) {
            int keyCode = keyEvent.getKeyCode();

            if (is_visible()) {
                if (KeyEvent.KEYCODE_BACK == keyCode) {
                    Log.d(TAG, "control_web_view: press back");
                    clearHistory();
                    loadUrl(BaseWebView.URL_ABOUT_BLANK);
                    fade_out();
                    get().g_listMgr.allow_focus_list();
                    if (g_callback != null)
                        g_callback.on_close_web_view();
                }
                else {
                    Log.d(TAG, "control_web_view: dispatch KeyEvent to WebView");
                    dispatchKeyEvent(keyEvent);
                }
                return true;
            }
        }
        return false;
    }

}
