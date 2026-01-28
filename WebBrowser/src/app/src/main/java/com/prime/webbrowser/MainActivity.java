package com.prime.webbrowser;

import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private WebView g_webView;
//    private static final String USER_AGENT_DESKTOP = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
    private static final String USER_AGENT_DESKTOP = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    private void init() {
        g_webView = (WebView) findViewById(R.id.main_webview);
        g_webView.setWebViewClient(new WebViewClient());
        g_webView.getSettings().setJavaScriptEnabled(true);
        g_webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        g_webView.getSettings().setUseWideViewPort(true);
        g_webView.getSettings().setLoadWithOverviewMode(true);
        g_webView.getSettings().setNeedInitialFocus(false);
        g_webView.getSettings().setUserAgentString(USER_AGENT_DESKTOP);
        g_webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void loadUri(String url) {
        g_webView.loadUrl(url);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);
        init();
        String url = getIntent().getStringExtra("WEB_URL_KEY") ;
        boolean permit = getIntent().getBooleanExtra("PERMIT", false);
        if (!permit) {
            finish();
        } else {
            loadUri(url);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        finish();
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent keyEvent) {
        if (keycode != KeyEvent.KEYCODE_BACK || !g_webView.canGoBack()) {
            return super.onKeyDown(keycode, keyEvent);
        }

        g_webView.goBack();
        return true;
    }
}
