package com.daisa.qreader;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private ListView historyListView;
    private HistoryAdapter adapter;
    private ArrayList<HistoryElement> elements = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyListView = findViewById(R.id.historyListView);
        adapter = new HistoryAdapter(this, elements);
        historyListView.setAdapter(adapter);
        //historyListView.setOnItemClickListener(this);
        //registerForContextMenu(historyListView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
*/
        Database db = new Database(this);
        elements.clear();
        elements.addAll(db.getLinks());
        adapter.notifyDataSetChanged();
    }
}