package com.cn.zhaol.demo.androidtest.ble.otherdemo.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 传统蓝牙帮助类(可以搞个单例模式)
 * Created by zhaolei on 2018/10/10.
 */
public class ClassicBlueToothHelp {

    private final static String TAG = "ClassicBlueToothHelp";
    private BluetoothAdapter mBluetoothAdapter;
    //private volatile boolean isRun = true;    //运行标志位

    private final UUID MY_UUID = UUID.fromString("abcd1234-ab12-ab12-ab12-abcdef123456");//和客户端相同的UUID(随便取)
    private final String NAME = "Bluetooth_Socket";

    private BluetoothSocket clientSocket = null;//客户端
    private BluetoothServerSocket serverSocket = null;//项目需要，服务器只连接一个客户端

    private volatile static ClassicBlueToothHelp instance;//（单例）

    private ClassicBlueToothHelp() {
        // 检查设备是否支持蓝牙
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//此方法用户能够执行基本的蓝牙操作
    }

    /**
     * 双重检查模式的单例
     * @return
     */
    public static ClassicBlueToothHelp getInstance() {
        if(instance == null) {
            synchronized (ClassicBlueToothHelp.class) {
                if(instance == null) {
                    instance = new ClassicBlueToothHelp();
                }
            }
        }
        return instance;
    }

    /**
     * 创建服务器
     * @return
     */
    public boolean createBlueServer() {
        //创建蓝牙服务器
        try {
            serverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            if(clientSocket == null) {
                //项目需要，服务器只能一个客户端连接
                clientSocket = serverSocket.accept();//接受个客户端连接
            }
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            try {
                closeServerAndClient();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return false;
        }
    }

    /**
     * 连接蓝牙服务器
     * @param device 蓝牙设备
     * @return
     */
    public boolean connectService(BluetoothDevice device) {
        //创建客户端蓝牙Socket
        try {
            clientSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            if(!clientSocket.isConnected()){
                //没有连接去连接
                clientSocket.connect();//开始连接蓝牙，如果没有配对则弹出对话框提示我们进行配对
                return clientSocket.isConnected();
            } else {
                //已经连接
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                //关闭
                closeClient();
            } catch (IOException ee) {
                ee.printStackTrace();
            }
            return false;
        }
    }

        /**
         * 打开蓝牙
         */
    public void openBlueTooth() {
        // 打开蓝牙
        if (!mBluetoothAdapter.isEnabled())
        {
            mBluetoothAdapter.enable();//打开蓝牙
            Log.i(TAG,"需要打开蓝牙");
        }
        else
        {
            Log.i(TAG,"已经打开蓝牙，不需要再次打开");
        }
    }

    /**
     * 关闭蓝牙
     */
    public void closeBlueTooth() {
        if (mBluetoothAdapter.isEnabled())
        {
            mBluetoothAdapter.disable();
        }
    }

    /**
     * 扫描设备
     */
    public void scanDevice() {
        if (mBluetoothAdapter.isDiscovering()) { //如果当前在搜索，就先取消搜索
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();//开启搜索
    }

    /**
     * 停止扫描设备
     */
    public void stopScan() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    /**
     * 获取已经配对的蓝牙设备
     * @return 设备集合，可能为null
     */
    public List<BluetoothDevice> getPairedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            List<BluetoothDevice> list = new ArrayList<>(pairedDevices);
            return list;
        } else {
            return null;
        }
    }

    /**
     * 得到蓝牙适配器
     * @return
     */
    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    /**
     * 开始配对(可能弹出提示框，用户输入密码)
     * @param btClass
     * @param device
     * @return
     * @throws Exception
     */
    public boolean createBond(Class btClass, BluetoothDevice device) throws Exception {
        Method createBondMethod = btClass.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        return returnValue.booleanValue();
    }

    /**
     * 自动配对设置Pin值(这个方法测试不成功)
     * @param btClass
     * @param device
     * @param strPin
     * @return
     * @throws Exception
     */
    public boolean autoBond(Class btClass, BluetoothDevice device, String strPin)
            throws Exception {
        Method autoBondMethod = btClass.getDeclaredMethod("setPin", new Class[] { byte[].class });
        Boolean result = (Boolean) autoBondMethod
                .invoke(device, new Object[] { strPin.getBytes("UTF-8") });
        Log.d("returnValue", "setPin result = " + result.booleanValue());
        return result;
    }

    /**
     * 与设备解除配对
     * 参考源码：platform/packages/apps/Settings.git
     * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
     * @param btClass
     * @param btDevice
     * @return
     * @throws Exception
     */
    public boolean removeBond(Class btClass, BluetoothDevice btDevice)
            throws Exception {
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    /**
     * 根据设备地址，连接蓝牙设备
     * @param strAddress 设备地址
     * @return
     */
    public boolean connectServiceByAddress(String strAddress) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(strAddress);
        if(null != device) {
            return connectService(device);
        } else {
            return false;
        }
    }

    /**
     * 得到远处设备
     * @param strAddress
     * @return
     */
    public BluetoothDevice getRemoteDeviceByStrAddress(String strAddress) {
        return mBluetoothAdapter.getRemoteDevice(strAddress);
    }

    //得到连接后的客户端
    public BluetoothSocket getClientSocket() {
        return clientSocket;
    }

    /**
     * 关闭客户端连接
     * @throws IOException
     */
    public void closeClient() throws IOException {
        if (null != clientSocket) {
            clientSocket.close();
            clientSocket = null;
        }
    }

    /**
     * 关闭服务器和客户端
     * @throws IOException
     */
    public void closeServerAndClient() throws IOException {
        if(null != serverSocket) {
            serverSocket.close();
            serverSocket = null;
        }
        if (null != clientSocket) {
            clientSocket.close();
            clientSocket = null;
        }
    }
}
