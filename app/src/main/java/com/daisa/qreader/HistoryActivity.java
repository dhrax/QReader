package com.daisa.qreader;

import android.app.ListActivity;
import android.os.Bundle;

import java.util.ArrayList;

public class HistoryActivity extends ListActivity{

    private HistoryAdapter adapter;
    private ArrayList<HistoryElement> elements = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        adapter = new HistoryAdapter(HistoryActivity.this,  elements);
        setListAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Database db = new Database(this);
        elements.clear();
        elements.addAll(db.getLinks("all"));
        adapter.notifyDataSetChanged();
    }
}