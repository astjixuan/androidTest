package com.cn.zhaol.demo.androidtest.ble;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * 低功耗蓝牙服务
 * Created by zhaolei on 2017/7/7.
 */
public class BluetoothLeService extends Service{

    private MyBinder myBinder = new MyBinder();
    private BluetoothHelpBLE bluetoothHelper = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class MyBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initAdapter();
        Log.i("BluetoothLeService","低功耗蓝牙服务开启");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeBlue();
        Log.i("BluetoothLeService","低功耗蓝牙服务销毁");
    }

    // 初始化蓝牙帮助类
    private void initAdapter() {
        bluetoothHelper = new BluetoothHelpBLE(getApplicationContext());
        bluetoothHelper.openBluetooth();//打开扫描设备
    }

    public void stopScan() {
        bluetoothHelper.stopScanBlueTooth();
    }

    public void startScan() {
        bluetoothHelper.startScanBlueTooth();
    }

    public void closeBlue() {
        bluetoothHelper.disconnect();
        bluetoothHelper.close();
    }
}
