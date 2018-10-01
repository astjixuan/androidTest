package com.cn.zhaol.demo.androidtest;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * 蓝牙设备显示
 * Created by zhaolei on 2017/7/7.
 */
public class LeDeviceListAdapter extends BaseAdapter {

    private Context mContext = null;
    private List<BluetoothDevice> mLeDevices;

    public LeDeviceListAdapter(Context context) {
        this.mContext = context;
    }

    public void setLeDevices(List<BluetoothDevice> mLeDevices) {
        this.mLeDevices = mLeDevices;
    }

    public void addDevice(BluetoothDevice device) {
        if(null != mLeDevices && mLeDevices.size() > 0) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }
    }

    public void clear() {
        if(null != mLeDevices && mLeDevices.size() > 0) {
            mLeDevices.clear();
        }
    }

    @Override
    public int getCount() {
        if(null != mLeDevices && mLeDevices.size() > 0) {
            return mLeDevices.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return mLeDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.device_list_item, null);
        TextView deviceAddress = (TextView) view.findViewById(R.id.tv_deviceAddr);
        TextView deviceName = (TextView) view.findViewById(R.id.tv_deviceName);
        BluetoothDevice device = mLeDevices.get(position);
        deviceAddress.setText(device.getAddress());
        deviceName.setText(device.getName());
        return view;
    }
}
