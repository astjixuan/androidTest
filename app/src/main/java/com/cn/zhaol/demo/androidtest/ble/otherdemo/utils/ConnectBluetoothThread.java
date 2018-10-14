package com.cn.zhaol.demo.androidtest.ble.otherdemo.utils;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;

import com.cn.zhaol.demo.androidtest.ble.tools.MyConstant;

/**
 * 客户端连接蓝牙的线程
 * Created by zhaolei on 2018/10/11.
 */
public class ConnectBluetoothThread extends Thread{

    private BluetoothDevice mDevice;
    private Handler serviceHandler;//用于向客户端Service回传消息的handler
    private ClassicBlueToothHelp blueToothHelp;

    public ConnectBluetoothThread(Handler handler,BluetoothDevice device) {
        this.serviceHandler = handler;
        this.mDevice = device;
        blueToothHelp = ClassicBlueToothHelp.getInstance();
    }

    @Override
    public void run() {
        if(blueToothHelp.connectService(mDevice)) {
            //成功，关闭搜索
            blueToothHelp.stopScan();
            //提示公户
            Message msg = serviceHandler.obtainMessage();
            msg.what = MyConstant.MESSAGE_CONNECT_SUCCESS;
            serviceHandler.sendMessage(msg);

        } else {
            //失败，提示用户
            Message msg = serviceHandler.obtainMessage();
            msg.what = MyConstant.MESSAGE_CONNECT_ERROR;
            serviceHandler.sendMessage(msg);
        }
    }
}
