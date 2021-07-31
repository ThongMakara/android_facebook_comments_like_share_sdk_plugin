package com.mango_byte.facebookcommentplugin;

import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

public class FbCommentsActivity extends AppCompatActivity {

    private static final int NUMBER_OF_COMMENTS = 10;

    private String postUrl;

    private WebView commentsView;
    private WebView webViewPopup;

    private FrameLayout containerWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fb_comments);

        postUrl = "https://web.facebook.com/mango.byte.kh/photos/a.169565376954745/883827608861848/";

        commentsView = (WebView) findViewById(R.id.commentsView);
        containerWebView = (FrameLayout) findViewById(R.id.webViewContainer);

        loadComments();
    }

    private void loadComments() {
        if (commentsView == null)
            return;

        commentsView.setWebViewClient(new UriWebViewClient());
        commentsView.setWebChromeClient(new UriChromeClient());
        commentsView.setMinimumHeight(200);
        configureWebSettings(commentsView);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        if (Build.VERSION.SDK_INT >= 21) {
            WebSettings settings = commentsView.getSettings();
            if (settings != null) {
                settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
            }
            cookieManager.setAcceptThirdPartyCookies(commentsView, true);
        }

        // Get the Facebook Widget with post Url.
        String html = createFacebookCommentWidget();
        commentsView.getSettings().setJavaScriptEnabled(true);
        commentsView.loadDataWithBaseURL(postUrl, html, "text/html", "UTF-8", null);
    }

    private String createFacebookCommentWidget() {
        return "<div id=\"fb-root\"></div>\n" +
                "<div class=\"fb-like\" data-href=\"https://www.facebook.com/sharer/sharer.php?u=" + postUrl + "&amp;amp;src=sdkpreparse\" data-width=\"\" data-layout=\"standard\" data-action=\"like\" data-size=\"large\" data-share=\"true\" data-mobile=true></div>\n" +
                "<script async defer crossorigin=\"anonymous\" src=\"https://connect.facebook.net/km_KH/sdk.js#xfbml=1&version=v11.0&appId=931697904072889&autoLogAppEvents=1\" nonce=\"yXareYkp\"></script>\n" +
                "<div id=\"fb-root\"></div>\n" +
                "<div class=\"fb-comments\" data-href=\"" + postUrl + "\" data-width=\"100\" data-numposts=\"5\" data-mobile=true></div>";
    }

    private void configureWebSettings(WebView webView) {
        if (webView != null) {
            WebSettings settings = webView.getSettings();
            if (settings != null) {
                settings.setJavaScriptEnabled(true);
                settings.setDomStorageEnabled(true);
                settings.setSupportZoom(false);
                settings.setBuiltInZoomControls(false);
                settings.setSupportMultipleWindows(true);
                settings.setAppCacheEnabled(true);
                settings.setJavaScriptCanOpenWindowsAutomatically(true);
            }
        }
    }

    private WebView createWebView(WebChromeClient webChromeClient, WebViewClient webViewClient) {
        WebView newWebView = new WebView(getApplicationContext());
        newWebView.setVerticalScrollBarEnabled(false);
        newWebView.setHorizontalScrollBarEnabled(false);
        newWebView.setWebViewClient(webViewClient);
        newWebView.setWebChromeClient(webChromeClient);
        newWebView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return newWebView;
    }

    private class UriWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            String host = Uri.parse(url).getHost();
            return !host.equals("m.facebook.com");
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            boolean isRemovePopup = url.contains("/plugins/close_popup.php?reload");
            if (isRemovePopup) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Code to load comments and remove dynamically created webView.
                        containerWebView.removeView(webViewPopup);
                        loadComments();
                    }
                }, 600);
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        }
    }

    class UriChromeClient extends WebChromeClient {

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {

            webViewPopup = createWebView(this, new WebViewClient());
            configureWebSettings(webViewPopup);
            webViewPopup.loadUrl(postUrl);
            containerWebView.addView(webViewPopup);

            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(webViewPopup);
            resultMsg.sendToTarget();
            return true;
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage cm) {
            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {
            containerWebView.removeView(webViewPopup);
        }
    }

}
