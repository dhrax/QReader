package com.daisa.qreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

//TODO implement favorites
public class HistoryAdapter extends BaseAdapter {

    private LayoutInflater layoutinflater;
    private Context context;
    private ArrayList<HistoryElement> elements;

    HistoryAdapter(Context context, ArrayList<HistoryElement> elements) {
        this.context = context;
        this.elements = elements;
        layoutinflater = LayoutInflater.from(context);
    }

    static class ViewHolder {
        TextView text;
        TextView date;
        ImageButton favorite;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewholder;

        if (convertView == null) {
            convertView = layoutinflater.inflate(R.layout.item_history, null);
            viewholder = new ViewHolder();
            //viewholder.imagen = convertView.findViewById(R.id.imgEvento);
            viewholder.text = convertView.findViewById(R.id.tvLinkText);
            viewholder.date = convertView.findViewById(R.id.tvScanDate);
            viewholder.favorite = convertView.findViewById(R.id.btnIsFavorite);
            convertView.setTag(viewholder);

        } else
            viewholder = (ViewHolder) convertView.getTag();

        HistoryElement element = elements.get(position);
        viewholder.text.setText(element.getText());
        viewholder.date.setText(element.getDate());
        //viewholder.favorite.set(evento.getFecha());

        return convertView;
    }

    @Override
    public int getCount() {
        return elements.size();
    }

    @Override
    public Object getItem(int position) {
        return elements.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


}
