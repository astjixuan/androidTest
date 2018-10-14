package com.cn.zhaol.demo.androidtest.ble.otherdemo.utils.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.cn.zhaol.demo.androidtest.ble.otherdemo.utils.ClassicBlueToothHelp;
import com.cn.zhaol.demo.androidtest.ble.otherdemo.utils.ClassicBlueToothRunThread;
import com.cn.zhaol.demo.androidtest.ble.otherdemo.utils.ServerConnThread;
import com.cn.zhaol.demo.androidtest.ble.tools.MyConstant;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * 经典蓝牙服务器的服务
 * @author zhaolei
 */
public class ClassicBlueToothServerService extends Service {

    private ClassicBlueToothHelp blueToothHelp;
    private ClassicBlueToothRunThread runThread;
    private ServerBinder mBinder = new ServerBinder();
    private Handler serverHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        blueToothHelp = ClassicBlueToothHelp.getInstance();
        //打开蓝牙
        blueToothHelp.openBlueTooth();
        serverHandler = new MyHandler(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //每次启动服务器都触发
        Log.w("Service *****","onStartCommand() ***");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        closeServer();
        serverHandler.removeCallbacksAndMessages(null);
        serverHandler = null;
        super.onDestroy();
    }

    private void closeServer() {
        try {
            blueToothHelp.closeServerAndClient();
            if(runThread != null) {
                runThread.shutdownThread();
                runThread.closeAll();
            }
            runThread = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class ServerBinder extends Binder {

        //开始蓝牙服务器（接听客户端）
        public void startBlueServer() {
            createServer();
        }

        //关闭服务器
        public void shutdownServer() {
            closeServer();
        }
    }

    private static class MyHandler extends Handler{
        private WeakReference<ClassicBlueToothServerService> serviceClass;

        public MyHandler(ClassicBlueToothServerService ser) {
            serviceClass = new WeakReference<>(ser);
        }

        @Override
        public void handleMessage(Message msg) {

            ClassicBlueToothServerService mClass = serviceClass.get();
            switch (msg.what)
            {
                case MyConstant.MESSAGE_CONNECT_ERROR:
                    //连接失败
                    Toast.makeText(mClass.getApplicationContext(), "连接蓝牙失败!",Toast.LENGTH_SHORT).show();

                    break;

                case MyConstant.MESSAGE_CONNECT_SUCCESS:
                    //连接成功
                    Toast.makeText(mClass.getApplicationContext(), "服务器有蓝牙连接成功!",Toast.LENGTH_SHORT).show();
                    //读取数据的线程启动
                    mClass.runThread = new ClassicBlueToothRunThread(this,
                            mClass.blueToothHelp.getClientSocket());
                    mClass.runThread.start();

                    break;

                case MyConstant.MESSAGE_READ_OBJECT:
                    //蓝牙收到数据
                    int val = msg.getData().getInt("intVal");
                    //发送给UI
                    Intent intentqueryMachine = new Intent();
                    intentqueryMachine.setAction("to_ui_act");
                    intentqueryMachine.putExtra("byte_arr", val);
                    mClass.getApplicationContext().sendBroadcast(intentqueryMachine);

                    break;
            }

        }
    }

    private void createServer() {
        new Thread(new ServerConnThread(serverHandler)).start();
    }
}
