package com.cn.zhaol.demo.androidtest.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


import com.cn.zhaol.demo.androidtest.MainActivity;

import java.util.List;

/**
 * BLE的低能耗蓝牙类
 * Created by zhaolei on 2017/7/5.
 */
public class BluetoothHelpBLE {

    private final static String TAG = "BluetoothHelpBLE";
    private BluetoothAdapter mAdapter = null;
    private Context mContext;
    private BluetoothGatt mBluetoothGatt = null;

    public BluetoothHelpBLE(Context context) {
        this.mContext = context;
    }

    /**
     * 打开蓝牙并搜索周围蓝牙设备
     */
    public void openBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);//这里与标准蓝牙略有不同
        mAdapter = bluetoothManager.getAdapter();
        /*隐式打开蓝牙*/
        if(!mAdapter.isEnabled()) {
            mAdapter.enable();
        }
        //通过startScan()方法扫描周围的BLE设备。当搜索到目标设备或者搜索一定时间后通过BluetoothScanner的stopScan()方法停止搜索。
        startScanBlueTooth();
        Log.i(TAG,"开启蓝牙，开始扫描周边设备");
    }

    /**
     * 开始扫描周边蓝牙
     */
    public void startScanBlueTooth() {
        BluetoothLeScanner scanner = mAdapter.getBluetoothLeScanner();
        scanner.startScan(mScanCallback);
    }

    /**
     * 关闭扫描蓝牙
     */
    public void stopScanBlueTooth() {
        BluetoothLeScanner scanner = mAdapter.getBluetoothLeScanner();
        scanner.stopScan(mScanCallback);
    }

    /**
     * 得到所有的服务类
     * @return
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getServices();
    }

    /**
     * 关闭蓝牙连接
     */
    public void disconnect() {
        if (mAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * 关闭蓝牙
     */
    public void close() {
        if (mBluetoothGatt == null) {
            Log.w(TAG, "mBluetoothGatt not initialized");
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * 连接远程蓝牙
     * @param address
     * @return
     */
    public boolean connect(String address) {
        if (mAdapter == null || address == null) {
            Log.e(TAG, "BluetoothAdapter == null or address == null");
            return false;
        }

        if (mBluetoothGatt.connect()) {
            //蓝牙已经连接了
            Log.e(TAG, "蓝牙已经连接了");
            return true;
        }
          /*获取远端的蓝牙设备*/
        BluetoothDevice device = mAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.e(TAG, "蓝牙设备没找到，无法连接！");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        /*调用device中的connectGatt连接到远程设备*/
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        System.out.println("device.getBondState=="+device.getBondState());
        return true;
    }

    /**
     * 通过onScanResult()把每次搜索到的设备添加到本地。
     */
    private ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            if (!MainActivity.BlueDevices.contains(device)) {  //判断是否已经添加
                String address = device.getAddress();
                String name = device.getName();
                Log.w(TAG,"扫描结果: "+address + " - " + name);
                broadcastUpdate(MainActivity.add_devices);//通知页面，有新的蓝牙设备----刷新页面
                MainActivity.BlueDevices.add(device);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            //批量处理搜索到的结果
            Log.e(TAG,"onBatchScanResults size = " + results.size());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    /*连接远程设备的回调函数*/
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //连接成功回调
                /*通过广播更新连接状态*/
                gatt.discoverServices();
                //broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");

            }else if(newState == BluetoothProfile.STATE_DISCONNECTED) {
                //连接失败回调
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //搜索到服务回调
                //broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                Log.i(TAG, "--onServicesDiscovered called--");
            } else {
                //未搜索到服务回调
                Log.e(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };

    /**
     * 发送广播
     * @param action 广播说明
     */
    private void broadcastUpdate(String action) {
        Intent intent = new Intent(action);
        //intent.putExtra(EXTRA_DATA, String.valueOf(rssi));
        mContext.sendBroadcast(intent);
    }

}
