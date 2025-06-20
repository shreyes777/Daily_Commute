package com.example.dailycommute;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class Chatbox extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();  // Hide Action Bar
        }

        setContentView(R.layout.activity_chatbox);

        WebView webView = findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);   // Enable JavaScript for UI changes
        webSettings.setDomStorageEnabled(true);   // Enable local storage
        webSettings.setAllowFileAccess(false);    // Disable file access for security
        webSettings.setAllowContentAccess(false);
        webSettings.setSafeBrowsingEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // Hide "Log in" button
                String js = "var loginButton = document.querySelector('button');" +
                        "if (loginButton && loginButton.innerText.includes('Log in')) {" +
                        "    loginButton.style.display='none';" +
                        "}";

                webView.evaluateJavascript(js, null);
            }
        });

        webView.loadUrl("https://chat.openai.com");
    }
}
