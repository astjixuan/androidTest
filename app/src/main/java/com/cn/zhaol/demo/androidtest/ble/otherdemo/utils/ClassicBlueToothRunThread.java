package com.cn.zhaol.demo.androidtest.ble.otherdemo.utils;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.cn.zhaol.demo.androidtest.ble.tools.MyConstant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 蓝牙通讯线程
 * 里面包括数据交互
 * Created by zhaolei on 2018/10/11.
 */
public class ClassicBlueToothRunThread extends Thread{

    public volatile boolean isRun = false;//是否运行
    private Handler serviceHandler;//用于向客户端Service回传消息的handler(ClassicBlueToothClientService)
    private BluetoothSocket clientSocket;
    private InputStream mInputStream = null;
    private OutputStream mOutputStream = null;

    public ClassicBlueToothRunThread(Handler handler,BluetoothSocket client) {
        this.serviceHandler = handler;
        this.clientSocket = client;
        try {
            mInputStream = clientSocket.getInputStream();//打开IO流
            mOutputStream = clientSocket.getOutputStream();
            isRun = true;
        } catch (IOException e) {
            e.printStackTrace();
            closeAll();
            //错误数据
            Message m = serviceHandler.obtainMessage(MyConstant.MESSAGE_CONNECT_ERROR);
            serviceHandler.sendMessage(m);
        }
    }

    @Override
    public void run() {

        Log.i("ClassicRunThread","****开始接受数据*****");
        while (isRun) {

            if (mInputStream == null || mOutputStream == null) {
                Log.e("readThread_run", "输入输出流为空");
                return;
            }

            try {
                //先读一个字段
                int temp = mInputStream.read();

                Log.i("helper_监听数据", "---------------temp = " + temp);

                //读的数据返回给界面
                Message msg = serviceHandler.obtainMessage();
                msg.what = MyConstant.MESSAGE_READ_OBJECT;
                //发送给UI
                Bundle bundle = new Bundle();
                //bundle.putByteArray("data", data);
                bundle.putInt("intVal",temp);
                msg.setData(bundle);
                serviceHandler.sendMessage(msg);

            } catch (IOException e) {
                e.printStackTrace();
                //结束所有
                shutdownThread();
                closeAll();
            }
        }
    }


    /**
     * 以下是与电源蓝牙通信情况的实现
     * @param arrData 数据
     */
    public void sendMessageToPower(byte[] arrData) {
        if (mOutputStream == null || mInputStream == null) {
            return;
        }
        try {
            mOutputStream.write(arrData);
            mOutputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
            shutdownThread();
            closeAll();
        }
    }

    public void sendInt(int i) {
        if (mOutputStream == null || mInputStream == null) {
            return;
        }
        try {
            mOutputStream.write(i);
            mOutputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
            shutdownThread();
            closeAll();
        }
    }

    //退出循环
    public void shutdownThread() {
        isRun = false;
    }

    //关闭所以
    public void closeAll() {
        try {
            if (clientSocket != null) {
                clientSocket.close();
                clientSocket = null;
            }
            if (mOutputStream != null) {
                mOutputStream.close();
                mOutputStream = null;
            }
            if (mInputStream != null) {
                mInputStream.close();
                mInputStream = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
