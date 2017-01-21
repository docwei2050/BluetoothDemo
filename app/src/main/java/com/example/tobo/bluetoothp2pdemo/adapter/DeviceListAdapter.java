package com.example.tobo.bluetoothp2pdemo.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.tobo.bluetoothp2pdemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tobo on 17/1/17.
 */

public class DeviceListAdapter extends BaseAdapter {
    private List<BluetoothDevice> mBluetoothDeviceList;
    private Context mContext;

    public DeviceListAdapter(Context context) {
        mContext = context;
        mBluetoothDeviceList=new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mBluetoothDeviceList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView==null){
            convertView=View.inflate(mContext, R.layout.item_devicelist,null);
            holder=new ViewHolder(convertView);
            convertView.setTag(holder);
        }else{
            holder= (ViewHolder) convertView.getTag();
        }
        BluetoothDevice device=mBluetoothDeviceList.get(position);


        //区分配对的和未配对的
        if(device.getBondState()==BluetoothDevice.BOND_BONDED){
            holder.tv_name.setTextColor(Color.parseColor("#1abc9c"));
            holder.tv_address.setTextColor(Color.parseColor("#1abc9c"));
        }else{
            holder.tv_name.setTextColor(Color.parseColor("#99000000"));
            holder.tv_address.setTextColor(Color.parseColor("#99000000"));
        }
        holder.tv_name.setText(TextUtils.isEmpty(device.getName())?"NoName":device.getName());
        holder.tv_address.setText(device.getAddress());
        return convertView;

    }

    public void add(BluetoothDevice device){
        mBluetoothDeviceList.add(device);
        notifyDataSetChanged();

    }
    public void clear(){
        mBluetoothDeviceList.clear();
        notifyDataSetChanged();
    }

    public List<BluetoothDevice> getBluetoothDeviceList() {
        return mBluetoothDeviceList;
    }
    public String getAddress(int position){
        return mBluetoothDeviceList.get(position).getAddress();
    }
    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
    static class ViewHolder{
      TextView tv_name,tv_address;
      public ViewHolder(View view){
          tv_name= (TextView) view.findViewById(R.id.device_name);
          tv_address= (TextView) view.findViewById(R.id.device_address);
      }
    }

}
