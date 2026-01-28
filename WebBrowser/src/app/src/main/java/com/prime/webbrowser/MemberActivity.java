package com.prime.webbrowser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Locale;

public class MemberActivity extends Activity {
    private static final String ANDROID_ASSET_PATH = "file:///android_asset" ;
    private static final String ERROR_PAGE_EN = ANDROID_ASSET_PATH + "/provision_error_en.html";
    private static final String ERROR_PAGE_ZH = ANDROID_ASSET_PATH + "/provision_error_zh.html";
    private static final String WEB_URL_KEY = "WEB_URL_KEY";
    private static final String USER_AGENT_DESKTOP = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
    private ProgressBar g_loadingbar;
    private WebView g_webview;
    private Context g_context ;
    private void initWebView(WebView webView) {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setNeedInitialFocus(false);
        webView.getSettings().setUserAgentString(USER_AGENT_DESKTOP);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView webView, String url) {
                super.onPageFinished(webView, url);
                g_loadingbar.setVisibility(View.GONE);
                if (url == null || !url.contains(ANDROID_ASSET_PATH)) {
                    g_webview.setFocusable(true);
                } else {
                    g_webview.setFocusable(false);
                }
            }

            @Override
            public void onPageStarted(WebView webView, String url, Bitmap bitmap) {
                super.onPageStarted(webView, url, bitmap);
                g_loadingbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedError(WebView webView, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
                super.onReceivedError(webView, webResourceRequest, webResourceError);
                loadErrorPage();
            }

            @Override
            public void onReceivedHttpError(WebView webView, WebResourceRequest webResourceRequest, WebResourceResponse webResourceResponse) {
                super.onReceivedHttpError(webView, webResourceRequest, webResourceResponse);
                if (webResourceRequest == null || webResourceRequest.getUrl() == null || !webResourceRequest.getUrl().toString().contains("favicon.ico")) {
                    loadErrorPage();
                }
            }

            @Override
            public void onReceivedSslError(WebView webView, final SslErrorHandler sslErrorHandler, SslError sslError) {
                AlertDialog.Builder builder = new AlertDialog.Builder(g_context);
                builder.setMessage(R.string.ssl_title);
                builder.setPositiveButton(getText(R.string.ssl_agree), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        sslErrorHandler.proceed();
                    }
                });
                builder.setNegativeButton(getText(R.string.ssl_disagree), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        sslErrorHandler.cancel();
                    }
                });
                builder.create().show();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
                return webResourceRequest == null || webResourceRequest.getUrl() == null || TextUtils.isEmpty(webResourceRequest.getUrl().toString()) || !webResourceRequest.getUrl().toString().contains("ErrorPages");
            }
        });
        webView.requestFocus(View.FOCUS_DOWN);
        String web_url = getIntent().getStringExtra(WEB_URL_KEY);
        if ( web_url != null ) {
            loadUrl(web_url);
            return;
        }
        Toast.makeText(this, getText(R.string.url_error), Toast.LENGTH_SHORT);
        finish();
    }

    public void loadErrorPage() {
        if (Locale.TRADITIONAL_CHINESE.getLanguage().equals(Locale.getDefault().getLanguage())) {
            loadUrl(ERROR_PAGE_ZH) ;
        } else {
            loadUrl(ERROR_PAGE_EN) ;
        }
    }

    private void loadUrl(String url) {
        g_webview.setFocusable(false);
        g_webview.stopLoading();
        g_webview.loadUrl(url);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_member);
        g_context = this ;
        g_loadingbar = (ProgressBar) findViewById(R.id.loadingbar);
        g_webview = (WebView) findViewById(R.id.member_webview);
        initWebView(g_webview);
    }
}
