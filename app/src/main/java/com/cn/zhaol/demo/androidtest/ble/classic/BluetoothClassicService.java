package com.cn.zhaol.demo.androidtest.ble.classic;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;


import com.cn.zhaol.demo.androidtest.MainActivity;
import com.cn.zhaol.demo.androidtest.ble.tools.MyConstant;

import java.io.IOException;
import java.util.List;

/**
 * 经典蓝牙服务
 * Created by zhaolei on 2017/7/10.
 */
public class BluetoothClassicService extends Service {

    private MyBinder myBinder = new MyBinder();
    private BluetoothClassicHelp blueHelper = null;
    private BluetoothAdapter mAdapter;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //扫描的结果
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!MainActivity.BlueDevices.contains(device)) {  //判断是否已经添加
                    String address = device.getAddress();
                    String name = device.getName();
                    Log.w("BluetoothClassicService","扫描结果: "+address + " - " + name);
                    broadcastUpdate(MainActivity.add_devices);//通知页面，有新的蓝牙设备----刷新页面
                    MainActivity.BlueDevices.add(device);
                }
            } else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                // 状态改变的广播
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int connectState = device.getBondState();
                String address = device.getAddress();
                String name = device.getName();
                Log.w("BluetoothClassicService","设备状态改变！connectState = " + connectState);
                Log.w("BluetoothClassicService","设备状态改变！name = " + name + ",address = " + address);
                if(BluetoothDevice.BOND_BONDED == connectState) {
                    //已经配对成功
                    Toast.makeText(getApplicationContext(),"设备："+name+"已经配对成功！",Toast.LENGTH_SHORT).show();
                    //尝试连接蓝牙
                    boolean f = connectBluetooth(device);
                    Log.w("BluetoothClassicService","蓝牙设备连接状态 f = " + f);
                }
            } else if(BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                //TODO 不用了
                //自己在收到广播时处理并将预先输入的密码设置进去
                BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = btDevice.getName();
                Log.e("BluetoothClassicService","设置蓝牙配对！name = " + name);
                try {
                    boolean f1 = BluetoothClassicHelp.autoBond(btDevice.getClass(), btDevice, "0000"); // 手机和蓝牙采集器配对
                    boolean f2 = BluetoothClassicHelp.createBond(btDevice.getClass(), btDevice);
                    boolean f3 = BluetoothClassicHelp.cancelPairingUserInput(btDevice.getClass(), btDevice);//取消用户输入
                    Log.e("BluetoothClassicService","f1="+f1+",f2="+f2+",f3="+f3);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if(MyConstant.Read_ToShort_Data.equals(action)) {
                Log.e("BluetoothClassicService","数据过短，请重新尝试！");
                Toast.makeText(getApplicationContext(),"数据过短，请重新尝试！",Toast.LENGTH_SHORT).show();
            } else if(MyConstant.Read_Qrcode_Wrong_Data.equals(action)) {
                Log.e("BluetoothClassicService","读取的标签类型有误！");
                Toast.makeText(getApplicationContext(),"读取的标签类型有误！",Toast.LENGTH_SHORT).show();
            } else if(MyConstant.Read_F6_Data.equals(action)) {
                Log.e("BluetoothClassicService","读取数据有误(F6)！");
                Toast.makeText(getApplicationContext(),"读取数据有误(F6)！",Toast.LENGTH_SHORT).show();
            } else if(MyConstant.Read_CRC_Wrong_Data.equals(action)) {
                Log.e("BluetoothClassicService","读取数据crc错误！");
                Toast.makeText(getApplicationContext(),"读取数据crc错误！",Toast.LENGTH_SHORT).show();
            } else if(MyConstant.Read_Order_Wrong_Data.equals(action)) {
                Log.e("BluetoothClassicService","返回数据错误，不符合要求(命令符不对)！");
                Toast.makeText(getApplicationContext(),"返回数据错误，不符合要求(命令符不对)！",Toast.LENGTH_SHORT).show();
            } else if(MyConstant.Read_Wronging_Data.equals(action)) {
                Log.e("BluetoothClassicService","读取过程中出错，请重新连接蓝牙!");
                Toast.makeText(getApplicationContext(),"读取过程中出错，请重新连接蓝牙!",Toast.LENGTH_SHORT).show();
            } else if(MyConstant.Read_Blue_Data.equals(action)) {
               //读取到蓝牙数据
                byte[] data = intent.getByteArrayExtra("data");
                //发送给UI
                Message msg = serviceHandler.obtainMessage();
                msg.what = 2;
                Bundle bundle = new Bundle();
                bundle.putByteArray("data", data);
                msg.setData(bundle);
                serviceHandler.sendMessage(msg);

            } else if(MyConstant.sendToEidBluetooth.equals(action)) {
                //UI发过来的命令---给蓝牙
                Bundle bundel = intent.getExtras();
                byte[] arr = bundel.getByteArray(MyConstant.byteArray);
                blueHelper.sendMessageToPower(arr);
            }
        }
    };

    private Handler serviceHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // UI给Service发送消息
                case 1:
                    break;

                // Service(扫码枪)给UI发送消息
                case 2:
                    byte[] arrIn = msg.getData().getByteArray("data");
                    //命令
                    byte command = arrIn[18]; //主命令
                    String ascii = "";
                    if(command == (byte)0x31) {
                        //二维码命令
                        byte command2 = arrIn[17];
                        if((byte)0x03 == command2 || (byte)0x04 == command2 || (byte)0x05 == command2 ||
                                (byte)0x06 == command2 || (byte)0x07 == command2 || (byte)0x01 == command2) {
                            //都是二维码命令
                            StringBuffer sb_asciiInfo = new StringBuffer();
                            for (int i = 0; i < arrIn.length; i++) {
                                if (i >= 20 && i <= arrIn.length -4) {
                                    //从第20位开始才是二维码---真正数据体
                                    int temp = arrIn[i] & 0xff;
                                    sb_asciiInfo.append((char)temp);
                                }
                            }
                            ascii = sb_asciiInfo.toString();
                        }
                    }

                    switch (command) {//arrIn[18]为命令

                        case (byte)0x31:
                            //二维码命令
                            switch (arrIn[17]) {
                                //TODO
                            }
                            break;

                        //以下是电子设备命令
                        case (byte)0x11:
                            Intent intentqueryMachine = new Intent();
                            intentqueryMachine.setAction("to_ui_act");
                            intentqueryMachine.putExtra("byte_arr", arrIn);
                            sendBroadcast(intentqueryMachine);

                            break;

                    }

                    break;
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class MyBinder extends Binder {
        public BluetoothClassicService getService() {
            return BluetoothClassicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 设置广播信息过滤
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        //intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);//
        //intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(MyConstant.Read_ToShort_Data);
        intentFilter.addAction(MyConstant.Read_Qrcode_Wrong_Data);
        intentFilter.addAction(MyConstant.Read_F6_Data);
        intentFilter.addAction(MyConstant.Read_CRC_Wrong_Data);
        intentFilter.addAction(MyConstant.Read_Order_Wrong_Data);
        intentFilter.addAction(MyConstant.Read_Wronging_Data);
        intentFilter.addAction(MyConstant.Read_Blue_Data);
        intentFilter.addAction(MyConstant.sendToEidBluetooth);

        // 注册广播接收器，接收并处理搜索结果
        registerReceiver(receiver, intentFilter);

        initAdapter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        blueHelper.close();
    }

    /**
     * 初始化蓝牙，并打开蓝牙
     */
    private void initAdapter() {
        blueHelper = new BluetoothClassicHelp(getApplicationContext());
        blueHelper.openBluetooth();//打开蓝牙
        mAdapter = blueHelper.getBluetoothAdapter();
    }

    /**
     * 开始扫描设备
     */
    public void startScan() {
        mAdapter.startDiscovery();
    }

    /**
     * 停止扫描设备
     */
    public void stopScan() {
        mAdapter.cancelDiscovery();
    }

    /**
     * 获得已经配对的蓝牙设备
     * @return
     */
    public List<BluetoothDevice> getBondedDevices() {
        return blueHelper.getBondedDevices();
    }

    /**
     * 根据设备地址，连接蓝牙设备
     * @param strAddress 设备地址
     * @return
     */
    public boolean connectBluetooth(String strAddress) {
        BluetoothDevice device = mAdapter.getRemoteDevice(strAddress);
        boolean flag = false;
        try {
            flag = blueHelper.connect(device);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 根据设备，连接蓝牙
     * @param device
     * @return
     */
    public boolean connectBluetooth(BluetoothDevice device) {
        boolean flag = false;
        try {
            flag = blueHelper.connect(device);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 发送广播
     */
    private void broadcastUpdate(String action) {
        Intent intent = new Intent(action);
        //intent.putExtra(EXTRA_DATA, String.valueOf(rssi));
        sendBroadcast(intent);
    }
}
