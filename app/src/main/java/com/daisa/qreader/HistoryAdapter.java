package com.daisa.qreader;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class HistoryAdapter extends BaseAdapter {

    private LayoutInflater layoutinflater;
    private Context context;
    private ArrayList<HistoryElement> elements;
    Database db;

    HistoryAdapter(Context context, ArrayList<HistoryElement> elements) {
        this.context = context;
        this.elements = elements;
        layoutinflater = LayoutInflater.from(context);
        db = new Database(context);
    }

    static class ViewHolder {
        TextView text;
        TextView date;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewholder;

        if (convertView == null) {
            convertView = layoutinflater.inflate(R.layout.item_history, null);
            viewholder = new ViewHolder();

            convertView.setTag(viewholder);

        } else
            viewholder = (ViewHolder) convertView.getTag();

        viewholder.text = detail(convertView, R.id.tvLinkText, elements.get(position).getText());
        viewholder.date = detail(convertView, R.id.tvScanDate, elements.get(position).getDate());

        ((ImageView) convertView.findViewById(R.id.btnIsFavorite)).setImageResource(elements.get(position).isFavorite() ? R.drawable.favorite_on : R.drawable.favorite_off);

        final View finalConvertView = convertView;
        //if we click on the ImageView, we change the link's favorite status.
        convertView.findViewById(R.id.btnIsFavorite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                elements.get(position).setFavorite(!elements.get(position).isFavorite());

                if (!db.updateFavoriteStatus(elements.get(position).getID(), elements.get(position).isFavorite())) {
                    Log.d("DEBUG getView", elements.get(position).getID() + " not updated.");
                    Toast.makeText(context, R.string.error_making_favorite, Toast.LENGTH_SHORT).show();
                }

                ((ImageView) finalConvertView.findViewById(R.id.btnIsFavorite)).setImageResource(db.getFavoriteStatus(elements.get(position).getText()) ? R.drawable.favorite_on : R.drawable.favorite_off);

                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    /**
     * {@link TextView} initialization.
     * @param v View to be initialized.
     * @param resId Id of the view.
     * @param text Text to be initialized with.
     * @return A {@link TextView} initialized.
     */
    private TextView detail(View v, int resId, String text) {
        TextView tv = (TextView) v.findViewById(resId);
        tv.setText(text);
        return tv;
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
