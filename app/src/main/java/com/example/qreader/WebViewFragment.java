package com.example.qreader;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class WebViewFragment extends Fragment {

    public static WebView linkWebView;
    ProgressBar progressBar;
    TextView loadingTextView;
    private String linkReceived;

    public WebViewFragment(String linkReceived) {
        this.linkReceived = linkReceived;
    }

    public static WebViewFragment newInstance(String linkReceived) {
        return new WebViewFragment(linkReceived);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_web_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        linkWebView = view.findViewById(R.id.linkWebView);
        progressBar = view.findViewById(R.id.progressBar);
        loadingTextView = view.findViewById(R.id.loadingTextView);
        webViewCharacteristics();
    }

    /**
     * WebView and settings and characteristics, ProgressBar updates and visibility settings.
     */
    private void webViewCharacteristics() {
        linkWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100 && progressBar.getVisibility() == ProgressBar.GONE) {
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                    loadingTextView.setVisibility(View.VISIBLE);
                    linkWebView.setVisibility(View.INVISIBLE);
                }

                progressBar.setProgress(progress);

                if (progress == 100) {
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