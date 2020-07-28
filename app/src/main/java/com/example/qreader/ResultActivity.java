package com.example.qreader;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * {@link android.app.Activity} that shows the link decoded as well as a preview of the site.
 */
//TODO improve interface
public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        initInterfaceElements();
    }

    /*@Override
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
     */

    /**
     * Interface elements initialization and receiving link decoded.
     */
    private void initInterfaceElements() {
        TextView linkResult = findViewById(R.id.linkResult);

        Intent intent = getIntent();

        String linkReceived = intent.getStringExtra("link");

        linkResult.setText(String.format("%s%s", linkResult.getText().toString(), linkReceived));

        if (linkReceived.endsWith(".pdf")) {
            Fragment fg = PdfRendererBasicFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.relativeLayout, fg).commit();
        } else {
            Fragment fg = WebViewFragment.newInstance(linkReceived);
            getSupportFragmentManager().beginTransaction().add(R.id.relativeLayout, fg).commit();
        }
    }

}