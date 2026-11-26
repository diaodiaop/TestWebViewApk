package com.diaodiaop.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String HOME_URL = "http://192.168.4.115:5173/demo/";
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(url);
            }
        });
        webView.setWebChromeClient(new WebChromeClient());

        String target = getTargetUrl(getIntent());
        webView.loadUrl(target == null ? HOME_URL : target);
    }

    private boolean handleUrl(String url) {
        if (url == null) return true;

        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();
        if (scheme == null) return false;

        // Normal web pages remain inside this WebView.
        if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
            return false;
        }

        // Do not allow web content to execute local scripts or open local files.
        if ("javascript".equalsIgnoreCase(scheme)
                || "file".equalsIgnoreCase(scheme)
                || "content".equalsIgnoreCase(scheme)
                || "data".equalsIgnoreCase(scheme)) {
            return true;
        }

        // Any other scheme (e.g. mangomis://, tel:, mailto:) is handed to Android.
        try {
            Intent intent;
            if (url.startsWith("intent://")) {
                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                    return true;
                }
                String fallback = intent.getStringExtra("browser_fallback_url");
                if (fallback != null && (fallback.startsWith("http://") || fallback.startsWith("https://"))) {
                    webView.loadUrl(fallback);
                }
                return true;
            }

            intent = new Intent(Intent.ACTION_VIEW, uri);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "没有找到能够打开此链接的应用：" + scheme, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            Toast.makeText(this, "无法打开外部应用：" + scheme, Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String target = getTargetUrl(intent);
        if (target != null && webView != null) {
            webView.loadUrl(target);
        }
    }

    private String getTargetUrl(Intent intent) {
        if (intent == null) return null;
        Uri data = intent.getData();
        String candidate = null;

        if (data != null && "client".equalsIgnoreCase(data.getHost())
                && data.getPath() != null && data.getPath().startsWith("/action/pages")) {
            candidate = data.getQueryParameter("url");
        } else if (Intent.ACTION_SEND.equals(intent.getAction())) {
            candidate = intent.getStringExtra(Intent.EXTRA_TEXT);
        }

        if (candidate == null) return null;
        candidate = candidate.trim();
        Uri parsed = Uri.parse(candidate);
        String scheme = parsed.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            Toast.makeText(this, "仅支持 http/https 网页地址", Toast.LENGTH_SHORT).show();
            return null;
        }
        return candidate;
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
