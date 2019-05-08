package com.linkplay.actionota;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class LogAdapter extends BaseAdapter {

    private final Context context;
    private ArrayList<String> data = new ArrayList<>();

    public LogAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView =  new TextView(context);
        }
        ((TextView)convertView).setText(data.get(position));
        return convertView;
    }

    private int count = 0;
    private StringBuilder temp = new StringBuilder();
    public void addOneData(String str) {
        if (count > 20){
            count=0;
            data.add(temp.toString());
            temp.setLength(0);
            notifyDataSetChanged();
        }else{
            temp.append(str).append("\n").append("\n");
            count++;
        }

    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

}
