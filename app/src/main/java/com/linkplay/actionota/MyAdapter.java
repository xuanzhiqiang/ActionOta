package com.linkplay.actionota;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.linkplay.bluetooth_utils2.LP_BLEDevice;


import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends BaseAdapter {

    private Context context;
    private List<LP_BLEDevice> data = new ArrayList<>();

    public MyAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public LP_BLEDevice getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder vh;

        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
            vh = new ViewHolder(convertView);
            convertView.setTag(vh);
        }else{
            vh = (ViewHolder) convertView.getTag();
        }

        vh.tvName.setText(data.get(position).getName());
        vh.tvState.setText(state2String(data.get(position).getState()));

        return convertView;
    }

    String state2String(int state){
        switch (state){
            case 0:
                return "已链接";
            case 1:
                return "未链接";
            case 2:
                return "链接中";
        }
        return "";
    }

    private class ViewHolder{
        TextView tvName;
        TextView tvState;
        ViewHolder(View view) {
            tvName = view.findViewById(R.id.text_name);
            tvState = view.findViewById(R.id.text_state);
        }
    }

    public void clearData() {
        data.clear();
        notifyDataSetChanged();
    }

    public void addDevice(LP_BLEDevice bleDevice) {
        if (!data.contains(bleDevice)) {
            data.add(bleDevice);
            notifyDataSetChanged();
        }
    }

    public void updateDevice(LP_BLEDevice device) {
        for (LP_BLEDevice d: data) {
            if (d.getAddress().equals(device.getAddress())){
                d.setState(device.getState());
            }
        }
        notifyDataSetChanged();
    }


    public void removeDevice(LP_BLEDevice device) {
        LP_BLEDevice temp = null;
        for (LP_BLEDevice d: data) {
            if (d.getAddress().equals(device.getAddress())){
                temp = d;
            }
        }
        if (temp != null){
            data.remove(temp);
            notifyDataSetChanged();
        }
    }




}
