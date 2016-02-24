package com.example.jake.maps;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Jake on 2/23/2016.
 */
public class TextSubTextAdapter extends BaseAdapter {
    private static LayoutInflater inflater=null;
    ArrayList<String> text;
    ArrayList<String> subText;
    //ArrayList<Integer> images;
    Context context;
    public TextSubTextAdapter(Activity activity, ArrayList<String> text, ArrayList<String> subText) {
        this.text=text;
        this.subText = subText;
        context=activity;
        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return text.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //POPULATES VIEW WITH HOLDER CLASS DATA
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.nearby_multi_spinner, null);  // USE MY CLASS HERE!
        // Get all views
        holder.text=(TextView) rowView.findViewById(R.id.nearby_multi_spinner_title);
        holder.subText=(TextView) rowView.findViewById(R.id.nearby_multi_spinner_sub_title);

        // Handle 0 values
        if (text.size() > 0)
            holder.text.setText(text.get(position));
        else
            holder.text.setText("");

        if (subText.size() > 0)
            holder.subText.setText(subText.get(position));
        else
            holder.subText.setText("");
        return rowView;
    }

    // CLASS THAT HOLDS DATA FOR VIEW
    public class Holder
    {
        TextView text;
        TextView subText;
    }

}
