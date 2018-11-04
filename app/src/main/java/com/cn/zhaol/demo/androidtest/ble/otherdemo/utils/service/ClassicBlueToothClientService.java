package com.cn.zhaol.demo.androidtest.ble.otherdemo.utils.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;


import com.cn.zhaol.demo.androidtest.MainActivity;
import com.cn.zhaol.demo.androidtest.ble.otherdemo.utils.ClassicBlueToothHelp;
import com.cn.zhaol.demo.androidtest.ble.otherdemo.utils.ClassicBlueToothRunThread;
import com.cn.zhaol.demo.androidtest.ble.otherdemo.utils.ConnectBluetoothThread;
import com.cn.zhaol.demo.androidtest.ble.tools.MyConstant;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 蓝牙客户端服务
 * Created by zhaolei on 2018/10/10.
 */
public class ClassicBlueToothClientService extends Service {

    private ClassicBlueToothHelp blueToothHelp;
    private MyBinder mBinder = new MyBinder();
    private Handler serviceHandler;
    private ClassicBlueToothRunThread runThread;

    /**
     * 定义广播接收器
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                Log.i("receiver ******", "onReceive:  扫描开始");
            } else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                Log.i("receiver ******", "onReceive:  扫描完成");
                //TODO 通知页面
                Intent in = new Intent("change_my_btn");
                sendBroadcast(in);

            } else if(action.equals(BluetoothDevice.ACTION_FOUND)) {
                //扫描的结果
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.e("addrs",bluetoothDevice.getAddress()+";"+bluetoothDevice.getName());
                //去页面更新设备列表
                Intent intent1 = new Intent(MainActivity.add_devices);
                intent1.putExtra("addrs",bluetoothDevice.getAddress()+";"+bluetoothDevice.getName());
                sendBroadcast(intent1);

            } else if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                //状态改变的广播
                //用户输入pin码去配对，如果配对成功后返回状态
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int connectState = device.getBondState();
                String address = device.getAddress();
                String name = device.getName();
                Log.w("receiver *****","设备状态改变！connectState = " + connectState);
                Log.w("receiver *****","设备状态改变！name = " + name + ",address = " + address);
                switch (connectState)
                {
                    case BluetoothDevice.BOND_BONDED:
                        //已经配对成功
                        Toast.makeText(getApplicationContext(),"设备："+name+"已经配对成功！",Toast.LENGTH_SHORT).show();
                        //尝试连接蓝牙
                        Log.w("BluetoothClassicService","尝试连接蓝牙~");
                        connectBlueService(device);

                        break;

                    case BluetoothDevice.BOND_NONE:
                        //没有绑定
                        Toast.makeText(getApplicationContext(),"设备："+name+"没有配对！",Toast.LENGTH_SHORT).show();

                        break;

                    case BluetoothDevice.BOND_BONDING:
                        //正在配对
                        Toast.makeText(getApplicationContext(),"设备："+name+"配对ing！",Toast.LENGTH_SHORT).show();

                        break;

                }

            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        //第一次创建
        Log.w("ClientService *****","onCreate() ***");
        initBlueService();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(receiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //服务执行的操作
        //每次启动服务器都触发
        Log.w("ClientService *****","onStartCommand() ***");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        //销毁
        Log.w("ClientService *****","onDestroy() ***");
        unregisterReceiver(receiver);
        closeClientService();
        serviceHandler.removeCallbacksAndMessages(null);
        serviceHandler = null;
        super.onDestroy();
    }

    /**
     * 关闭客户端服务
     */
    private void closeClientService() {
        try {
            blueToothHelp.closeClient();
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
        //bindService()。启动方式才会用到onBind()方法。
        //如果用startService()，可以暂时忽略onBind()方法。return null
        return mBinder;
    }

    //MyBinder类，继承Binder：让里面放外界调用该Service的方法
    public class MyBinder extends Binder {

        //关闭客户端
        public void closeClient()
        {
            closeClientService();
        }

        //开始扫描设备
        public void scanDevice()
        {
            blueToothHelp.scanDevice();
        }

        //停止扫描设备
        public void stopScan()
        {
            blueToothHelp.stopScan();
        }

        //关闭蓝牙
        public void closeBlueTooth()
        {
            blueToothHelp.closeBlueTooth();
        }

        //已经配对的设备
        public List<BluetoothDevice> getPairedDevices()
        {
            return blueToothHelp.getPairedDevices();
        }

        //客户端连接服务器
        public void connectService(BluetoothDevice mDevice)
        {
            connectBlueService(mDevice);
        }

        public BluetoothDevice getDeviceByAddress(String address) {
            return blueToothHelp.getRemoteDeviceByStrAddress(address);
        }

//        public boolean createBond(Class btClass, BluetoothDevice device) throws Exception {
//            //老版本方法
//            return blueToothHelp.createBond(btClass,device);
//        }

        public boolean createBond(BluetoothDevice device) {
            return blueToothHelp.createBond(device);
        }

        public boolean setPin(BluetoothDevice device,String pin) throws UnsupportedEncodingException {
            return blueToothHelp.setPin(device,pin);
        }

        public void sendMsg(int i) {
            if(null != runThread) {
                runThread.sendInt(i);
            }
        }

        public void sendMsg(byte[] arr) {
            if(null != runThread) {
                runThread.sendMessageToPower(arr);
            }
        }
    }

    //初始化蓝牙服务器
    private void initBlueService() {
        blueToothHelp = ClassicBlueToothHelp.getInstance();
        //打开蓝牙
        blueToothHelp.openBlueTooth();
        serviceHandler = new MyHandler(this);
    }

    //自己定义的Handler，接受消息(线程给的信息)
    private static class MyHandler extends Handler{

        private WeakReference<ClassicBlueToothClientService> serviceClass;

        public MyHandler(ClassicBlueToothClientService service) {
            serviceClass = new WeakReference<ClassicBlueToothClientService>(service);
        }

        @Override
        public void handleMessage(Message msg) {

            ClassicBlueToothClientService mService = serviceClass.get();
            switch (msg.what)
            {
                case MyConstant.MESSAGE_CONNECT_ERROR:
                    //连接失败
                    Toast.makeText(mService.getApplicationContext(), "连接蓝牙失败!",Toast.LENGTH_SHORT).show();

                    break;

                case MyConstant.MESSAGE_CONNECT_SUCCESS:
                    //连接成功
                    Toast.makeText(mService.getApplicationContext(), "连接蓝牙成功!",Toast.LENGTH_SHORT).show();
                    //读取数据的线程启动
                    mService.runThread = new ClassicBlueToothRunThread(this,
                            mService.blueToothHelp.getClientSocket());
                    mService.runThread.start();

                    break;

                case MyConstant.MESSAGE_READ_OBJECT:
                    //蓝牙收到数据
                    int val = msg.getData().getInt("intVal");
                    //发送给UI
                    Intent intentqueryMachine = new Intent();
                    intentqueryMachine.setAction("to_ui_act");
                    intentqueryMachine.putExtra("byte_arr", val);
                    mService.getApplicationContext().sendBroadcast(intentqueryMachine);

                    break;
            }
        }
    }

    //连接服务器
    private void connectBlueService(BluetoothDevice mDevice) {
//        if(blueToothHelp.connectService(mDevice)) {
//            //成功，关闭搜索
//            blueToothHelp.stopScan();
//            return true;
//        } else {
//            //失败，提示用户
//            Toast.makeText(getApplicationContext(),"设备："+mDevice.getName()+",连接蓝牙失败!",Toast.LENGTH_SHORT).show();
//            return false;
//        }
        //启动线程，连接服务器
        new ConnectBluetoothThread(serviceHandler,mDevice).start();
        Log.w("ClientService *****","connectBlueService 尝试连接蓝牙 ***");
    }

}
