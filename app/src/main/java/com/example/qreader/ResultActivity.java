package com.example.qreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

//TODO improve interface
public class ResultActivity extends AppCompatActivity {

    TextView linkResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        linkResult = findViewById(R.id.linkResult);

        Intent intent = getIntent();

        linkResult.setText(intent.getStringExtra("link"));
    }
}