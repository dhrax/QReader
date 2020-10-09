package com.daisa.qreader;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

/**
 * {@link android.app.Activity} that shows the link decoded as well as a preview of the site.
 */
//TODO improve interface
public class ResultActivity extends AppCompatActivity implements View.OnClickListener {

    Database db;
    ImageView btnResFav;
    String linkReceived;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        db = new Database(this);

        initInterfaceElements();
    }

    @Override
    public void onBackPressed() {
        //If we have visited a link inside a WebView, we go back to the previous site-
        //if we can't go back anymore inside the browser, we go back to CameraPreviewActivity.
        if (WebViewFragment.linkWebView != null && WebViewFragment.linkWebView.canGoBack()) {
            WebViewFragment.linkWebView.goBack();
        } else {
            Intent intent = new Intent(this, CameraPreviewActivity.class);
            startActivity(intent);
        }

    }

    /**
     * Interface elements initialization and receiving link decoded.
     */
    private void initInterfaceElements() {
        TextView linkResult = findViewById(R.id.linkResult);
        btnResFav = findViewById(R.id.btnResFav);

        Intent intent = getIntent();

        linkReceived = intent.getStringExtra("link");

        linkResult.setText(String.format("%s%s", linkResult.getText().toString(), linkReceived));

        db.insertLinkToHistory(linkReceived);

        Fragment fg = WebViewFragment.newInstance(linkReceived);
        getSupportFragmentManager().beginTransaction().add(R.id.relativeLayout, fg).commit();

        btnResFav.setImageResource(db.getFavoriteStatus(linkReceived) ? R.drawable.is_favorite : R.drawable.not_favorite);
        btnResFav.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnResFav:
                if(db.updateFavoriteStatus(linkReceived, !db.getFavoriteStatus(linkReceived))){
                    if(db.getFavoriteStatus(linkReceived)){
                        btnResFav.setImageResource(R.drawable.is_favorite);
                        Toast.makeText(this, "Link added to favorites.", Toast.LENGTH_SHORT).show();
                    }else{
                        btnResFav.setImageResource(R.drawable.not_favorite);
                        Toast.makeText(this, "Link deleted from favorites.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(this, "Couldn't add link to favorites.", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}