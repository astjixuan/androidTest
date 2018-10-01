package com.cn.zhaol.demo.androidtest;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;


import com.cn.zhaol.demo.androidtest.ble.BluetoothLeService;

import java.util.List;

/**
 * BLE低功耗蓝牙页面
 */
public class BLEActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private ListView mListView = null;
    private LeDeviceListAdapter adapter = null;
    private BluetoothLeService bluetoothService = null;

    //广播
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(MainActivity.add_devices)) {
                //添加新的蓝牙设备
                adapter.setLeDevices(MainActivity.BlueDevices);
                adapter.notifyDataSetChanged();
                Log.w("BLEActivity","更新listview！！！,size = " + MainActivity.BlueDevices.size());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_activity);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.add_devices);
        //intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        registerReceiver(receiver, intentFilter);

        //检查用户权限 很重要
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //可以添加别的权限请求
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }

        findViewById(R.id.ble_scanBtn).setOnClickListener(this);
        findViewById(R.id.ble_startBtn).setOnClickListener(this);
        findViewById(R.id.ble_stopBtn).setOnClickListener(this);
        mListView = (ListView) findViewById(R.id.ble_listview);
        adapter = new LeDeviceListAdapter(getApplicationContext());
        mListView.setAdapter(adapter);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // TODO request success
                    //接收权限是否请求的请求状态,提示用户,返回确认
                    Log.e("BLEActivity","onRequestPermissionsResult 接收权限是否请求的请求状态");
                }
                break;
        }
    }

    // 蓝牙service
    private void bluetoothBindService() {
        Intent intent_bindService = new Intent();
        intent_bindService.setClass(BLEActivity.this,BluetoothLeService.class);
        bindService(intent_bindService, mServiceConnection, BIND_AUTO_CREATE);
    }


    // connection 专门做工单的蓝牙服务器
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder service) {
            // service连接成功
            // 强制转换成BluetoothService.MyBinder类型的binder
            bluetoothService = ((BluetoothLeService.MyBinder) service)
                    .getService();
            // 得到对应的binder之后可以调用其内部函数
//			Log.i("activity_connect", "service_handler" + serviceHandler);
        }

        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothService != null) {
            unbindService(mServiceConnection);
        }
        unregisterReceiver(receiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ble_scanBtn:
                bluetoothService.startScan();
                break;

            case R.id.ble_stopBtn:
                bluetoothService.stopScan();
                break;

            default:
                //绑定蓝牙服务
                bluetoothBindService();
                break;
        }
    }
}
