package com.example.qreader;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * {@link android.app.Activity} that shows the link decoded as well as a preview of the site.
 */
//TODO improve interface
public class ResultActivity extends AppCompatActivity {

    private TextView loadingTextView;
    private WebView linkWebView;
    private ProgressBar progressBar;
    private String linkReceived;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        initInterfaceElements();

        webViewCharacteristics();
    }

    @Override
    public void onBackPressed() {
        //If we have visited a link inside a WebView, we can go back to the previous site
        //if we can't, we go back to CameraPreviewActivity
        if(linkWebView.canGoBack()){
            linkWebView.goBack();
        }else{
            Intent intent = new Intent(this, CameraPreviewActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Interface elements initialization and receiving link decoded.
     */
    private void initInterfaceElements() {
        TextView linkResult = findViewById(R.id.linkResult);
        linkWebView = findViewById(R.id.linkWebView);
        progressBar = findViewById(R.id.progressBar);
        loadingTextView =findViewById(R.id.loadingTextView);

        Intent intent = getIntent();

        linkReceived = intent.getStringExtra("link");

        linkResult.setText(String.format("%s%s", linkResult.getText().toString(), linkReceived));
    }

    /**
     * WebView and settings and characteristics, ProgressBar updates and visibility settings.
     */
    private void webViewCharacteristics() {
        linkWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if(progress < 100 && progressBar.getVisibility() == ProgressBar.GONE){
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                    loadingTextView.setVisibility(View.VISIBLE);
                    linkWebView.setVisibility(View.INVISIBLE);
                }

                progressBar.setProgress(progress);

                if(progress == 100){
                    progressBar.setVisibility(ProgressBar.GONE);
                    loadingTextView.setVisibility(View.GONE);
                    linkWebView.setVisibility(View.VISIBLE);
                }
            }
        });
        linkWebView.setWebViewClient(new WebViewClient());
        linkWebView.loadUrl(linkReceived);

        WebSettings webSettings = linkWebView.getSettings();
        webSettings.setJavaScriptEnabled(false);
    }
}