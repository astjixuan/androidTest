package com.cn.zhaol.demo.androidtest.ble.otherdemo.utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.cn.zhaol.demo.androidtest.ble.tools.MyConstant;

/**
 * 服务器等待连接
 */
public class ServerConnThread implements Runnable{

    private Handler serviceHandler;//用于向客户端Service回传消息的handler
    private ClassicBlueToothHelp blueToothHelp;

    public ServerConnThread(Handler handler) {
        blueToothHelp = ClassicBlueToothHelp.getInstance();
        this.serviceHandler = handler;
    }

    @Override
    public void run() {
        Log.i("ServerConnThread","蓝牙服务器开启中，等待设备连接。。。");
        if(blueToothHelp.createBlueServer()) {
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
