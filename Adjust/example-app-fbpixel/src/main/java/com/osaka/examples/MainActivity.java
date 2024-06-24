package com.osaka.examples;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.osaka.sdk.webbridge.AdjustBridge;
import com.osaka.sdk.webbridge.AdjustBridgeInstance;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView = (WebView) findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                String message = String.format("%s - %d: %s", consoleMessage.sourceId(), consoleMessage.lineNumber(), consoleMessage.message());
                Log.d("AdjustExample", message);
                return super.onConsoleMessage(consoleMessage);
            }
        });

        AdjustBridgeInstance defaultInstance = AdjustBridge.registerAndGetInstance(getApplication(), webView);
        defaultInstance.registerFacebookSDKJSInterface();

        try {
            webView.loadUrl("file:///android_asset/AdjustExample-FbPixel.html");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        AdjustBridge.unregister();

        super.onDestroy();
    }
}
