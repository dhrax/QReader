package com.daisa.qreader;

import android.app.ListActivity;
import android.os.Bundle;

import java.util.ArrayList;

public class FavoriteActivity extends ListActivity {

    private HistoryAdapter adapter;
    private ArrayList<HistoryElement> elements = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        adapter = new HistoryAdapter(FavoriteActivity.this,  elements);
        setListAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Database db = new Database(this);
        elements.clear();
        elements.addAll(db.getLinks("favorites"));
        adapter.notifyDataSetChanged();
    }

}