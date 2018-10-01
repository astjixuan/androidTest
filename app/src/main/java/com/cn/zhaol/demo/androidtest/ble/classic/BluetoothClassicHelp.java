package com.cn.zhaol.demo.androidtest.ble.classic;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;


import com.cn.zhaol.demo.androidtest.ble.tools.MyConstant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 经典蓝牙工具类
 * Created by zhaolei on 2017/7/10.
 */
public class BluetoothClassicHelp {

    public static boolean isConnected = false;// 判断手持设备（扫描枪）是否连接，默认false，连上为true
    private final static String TAG = "BluetoothClassicHelp";
    private Context mContext;
    private BluetoothAdapter mAdapter;
    private volatile InputStream mInputStream = null;
    private volatile OutputStream mOutputStream = null;
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket clientSocket = null;
    private volatile byte cmd = 0;
    private volatile byte[] byteData;

    //crc16的库文件
    public static  int crc16_ccitt_table[] ={
            0x0000, 0x1189, 0x2312, 0x329b, 0x4624, 0x57ad, 0x6536, 0x74bf,
            0x8c48, 0x9dc1, 0xaf5a, 0xbed3, 0xca6c, 0xdbe5, 0xe97e, 0xf8f7,
            0x1081, 0x0108, 0x3393, 0x221a, 0x56a5, 0x472c, 0x75b7, 0x643e,
            0x9cc9, 0x8d40, 0xbfdb, 0xae52, 0xdaed, 0xcb64, 0xf9ff, 0xe876,
            0x2102, 0x308b, 0x0210, 0x1399, 0x6726, 0x76af, 0x4434, 0x55bd,
            0xad4a, 0xbcc3, 0x8e58, 0x9fd1, 0xeb6e, 0xfae7, 0xc87c, 0xd9f5,
            0x3183, 0x200a, 0x1291, 0x0318, 0x77a7, 0x662e, 0x54b5, 0x453c,
            0xbdcb, 0xac42, 0x9ed9, 0x8f50, 0xfbef, 0xea66, 0xd8fd, 0xc974,
            0x4204, 0x538d, 0x6116, 0x709f, 0x0420, 0x15a9, 0x2732, 0x36bb,
            0xce4c, 0xdfc5, 0xed5e, 0xfcd7, 0x8868, 0x99e1, 0xab7a, 0xbaf3,
            0x5285, 0x430c, 0x7197, 0x601e, 0x14a1, 0x0528, 0x37b3, 0x263a,
            0xdecd, 0xcf44, 0xfddf, 0xec56, 0x98e9, 0x8960, 0xbbfb, 0xaa72,
            0x6306, 0x728f, 0x4014, 0x519d, 0x2522, 0x34ab, 0x0630, 0x17b9,
            0xef4e, 0xfec7, 0xcc5c, 0xddd5, 0xa96a, 0xb8e3, 0x8a78, 0x9bf1,
            0x7387, 0x620e, 0x5095, 0x411c, 0x35a3, 0x242a, 0x16b1, 0x0738,
            0xffcf, 0xee46, 0xdcdd, 0xcd54, 0xb9eb, 0xa862, 0x9af9, 0x8b70,
            0x8408, 0x9581, 0xa71a, 0xb693, 0xc22c, 0xd3a5, 0xe13e, 0xf0b7,
            0x0840, 0x19c9, 0x2b52, 0x3adb, 0x4e64, 0x5fed, 0x6d76, 0x7cff,
            0x9489, 0x8500, 0xb79b, 0xa612, 0xd2ad, 0xc324, 0xf1bf, 0xe036,
            0x18c1, 0x0948, 0x3bd3, 0x2a5a, 0x5ee5, 0x4f6c, 0x7df7, 0x6c7e,
            0xa50a, 0xb483, 0x8618, 0x9791, 0xe32e, 0xf2a7, 0xc03c, 0xd1b5,
            0x2942, 0x38cb, 0x0a50, 0x1bd9, 0x6f66, 0x7eef, 0x4c74, 0x5dfd,
            0xb58b, 0xa402, 0x9699, 0x8710, 0xf3af, 0xe226, 0xd0bd, 0xc134,
            0x39c3, 0x284a, 0x1ad1, 0x0b58, 0x7fe7, 0x6e6e, 0x5cf5, 0x4d7c,
            0xc60c, 0xd785, 0xe51e, 0xf497, 0x8028, 0x91a1, 0xa33a, 0xb2b3,
            0x4a44, 0x5bcd, 0x6956, 0x78df, 0x0c60, 0x1de9, 0x2f72, 0x3efb,
            0xd68d, 0xc704, 0xf59f, 0xe416, 0x90a9, 0x8120, 0xb3bb, 0xa232,
            0x5ac5, 0x4b4c, 0x79d7, 0x685e, 0x1ce1, 0x0d68, 0x3ff3, 0x2e7a,
            0xe70e, 0xf687, 0xc41c, 0xd595, 0xa12a, 0xb0a3, 0x8238, 0x93b1,
            0x6b46, 0x7acf, 0x4854, 0x59dd, 0x2d62, 0x3ceb, 0x0e70, 0x1ff9,
            0xf78f, 0xe606, 0xd49d, 0xc514, 0xb1ab, 0xa022, 0x92b9, 0x8330,
            0x7bc7, 0x6a4e, 0x58d5, 0x495c, 0x3de3, 0x2c6a, 0x1ef1, 0x0f78
    };

    public BluetoothClassicHelp(Context context) {
        this.mContext = context;
    }

    /**
     * 打开蓝牙
     */
    public void openBluetooth() {
        // 检查设备是否支持蓝牙
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        // 打开蓝牙
        if (!mAdapter.isEnabled())
        {
            //不能在工具类中使用
//            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            // 设置蓝牙可见性，最多300秒
//            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//            mContext.startActivity(intent);
            mAdapter.enable();//打开蓝牙
            Log.w(TAG,"需要打开蓝牙");
        }
        else
        {
            Log.w(TAG,"已经打开蓝牙，不需要再次打开");
        }
    }

    /**
     * 获得已经配对的蓝牙设备
     * @return
     */
    public List<BluetoothDevice> getBondedDevices() {
        if(null != mAdapter) {
            Set<BluetoothDevice> s = mAdapter.getBondedDevices();
            List<BluetoothDevice> list = new ArrayList<BluetoothDevice>(s);
            for(BluetoothDevice d : list) {
                Log.w(TAG,"已经配对的: "+d.getName()+"-"+d.getAddress());
            }
            return list;
        }
        return null;
    }

    /**
     * 得到蓝牙适配器
     * @return
     */
    public BluetoothAdapter getBluetoothAdapter() {
        return mAdapter;
    }

    /**
     * 自动配对设置Pin值
     * @param btClass
     * @param device
     * @param strPin
     * @return
     * @throws Exception
     */
    public static boolean autoBond(Class btClass, BluetoothDevice device, String strPin)
            throws Exception {
        Method autoBondMethod = btClass.getDeclaredMethod("setPin", new Class[] { byte[].class });
        Boolean result = (Boolean) autoBondMethod
                .invoke(device, new Object[] { strPin.getBytes("UTF-8") });
        Log.d("returnValue", "setPin result = " + result.booleanValue());
        return result;
    }

    /**
     * 开始配对
     * @param btClass
     * @param device
     * @return
     * @throws Exception
     */
    public static boolean createBond(Class btClass, BluetoothDevice device) throws Exception {
        Method createBondMethod = btClass.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        return returnValue.booleanValue();
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
    public static boolean removeBond(Class btClass, BluetoothDevice btDevice)
            throws Exception
    {
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    /**
     * 取消用户输入
     * @param btClass
     * @param device
     * @return
     * @throws Exception
     */
    public static boolean cancelPairingUserInput(Class btClass,
                                                 BluetoothDevice device)
            throws Exception
    {
        Method createBondMethod = btClass.getMethod("cancelPairingUserInput");
        // cancelBondProcess()
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        return returnValue.booleanValue();
    }

    /**
     * 取消配对
     * @param btClass
     * @param device
     * @return
     * @throws Exception
     */
    public static boolean cancelBondProcess(Class btClass,
                                            BluetoothDevice device)
            throws Exception
    {
        Method createBondMethod = btClass.getMethod("cancelBondProcess");
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        return returnValue.booleanValue();
    }

    /**
     * 连接蓝牙设备
     * @param device
     * @throws IOException
     */
    public boolean connect(BluetoothDevice device) throws IOException {
        // 固定的UUID
        clientSocket = device.createRfcommSocketToServiceRecord(uuid);
        if(!clientSocket.isConnected()){
            //没有连接去连接
            clientSocket.connect();
        }
        mInputStream = clientSocket.getInputStream();//打开IO流
        mOutputStream = clientSocket.getOutputStream();
        boolean f = clientSocket.isConnected();
        isConnected = f;
        Log.w(TAG,"是否连接成功 = " +f);
        //监听数据
        if(null != mInputStream) {
            ReadThread readThread = new ReadThread();
            readThread.start();
        }
        return f;
//        if(null != mInputStream) {
//            Log.w(TAG,"连接成功！");
//            return true;
//        } else {
//            return false;
//        }
    }

    // 关闭开启的套接字和流
    public void close() {
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

    /**
     * 以下是与服务端（扫描抢）的7种通信情况的实现
     * @param cmd 命令
     */
    public void read_TwoDimensionDode(byte cmd) {
        if (mOutputStream == null || mInputStream == null) {
            return;
        }
        this.cmd = cmd;
        byte[] arrCmd = scanOrder(cmd);
        //
        StringBuffer str = new StringBuffer();
        for(int i = 0; i < arrCmd.length; i++) {
            str.append(Integer.toHexString(getUnsignedByte(arrCmd[i])) + " ");
        }
        Log.v("params = ",str.toString());

        try {
            //变成华为格式
            byte[] p = transformByHuaWei(arrCmd);
            mOutputStream.write(p);
            mOutputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
            close();
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
        //命令
        byte command = arrData[17];
        if((byte)0x09 != command) {
            //告警回单排除在外
            this.cmd = command;
            this.byteData = arrData;
        }
        try {
            mOutputStream.write(arrData);
            mOutputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    /**
     * 读取数据
     */
    private class ReadThread extends Thread {

        @Override
        public void run() {
            try {
                boolean wrongFlag = false;//错误数据
                while (true) {

                    if (mInputStream == null) {
                        Log.e("readThread_run", "输入输出流为空");
                        return;
                    }

                    //先读一个字段
                    int temp = mInputStream.read();

                    Log.e("helper_监听数据", "---------------temp = " + temp);

                    //如果没发命令，读取到流中的数据要去除
                    if((byte) 0x7E != temp) {
                        continue;
                    }

                    //TODO start
                    if(wrongFlag) {
                        //遇到7e，7e数据，丢掉刚开始的7e
                        //重置flag
                        wrongFlag = false;
                        continue;
                    }
                    //TODO end

                    int i = 1;
                    List<Byte> list = new ArrayList<Byte>();
                    list.add((byte)temp);

                    while((temp = mInputStream.read())!=-1)
                    {
                        list.add((byte)temp);
                        i++;
                        //如果结尾是7e就退出
                        if(temp == 0x7E) {
                            break;
                        }
                        if(i == 1400) {
                            //超过最大长度，退出
                            break;
                        }
                    }

                    //转换成真实数据
                    List<Byte> realData = transformByRealData(getDataByte(list));

                    //TODO 测试用
                    StringBuffer buffer = new StringBuffer();
                    for(int g = 0; g < realData.size(); g++) {
                        buffer.append(Integer.toHexString(getUnsignedByte(realData.get(g))) + " ");
                    }
                    Log.e("ReadThread", "list(转换后) = " + buffer.toString());
                    Log.e("ReadThread", "list.size(转换后) = " + realData.size());

                    //Message arr_msg = new Message();
                    //返回数据错误，不符合要求
                    //arr_msg.what = 3;

                    //判断数据长度，小于23肯定出错
                    int size = realData.size();
                    if(size < 23) {
                        //TODO start
                        if(size == 2 && realData.get(0)==(byte)0x7e && realData.get(1)==(byte)0x7e) {
                            //遇到7e，7e数据时，下个7e也不能接受
                            wrongFlag = true;
                        }
                        //TODO end

                        Log.e("ReadThread", "数据长度不对，小于23个！！,size = " + size);
                        broadcastUpdate(MyConstant.Read_ToShort_Data);
                        continue;
                    }

                    if (realData.get(18) == (byte)0x31 && realData.get(17) == (byte)0x08) {
                        //如果是扫描枪的命令，就不重新发送
                        //标签类型错误
                        broadcastUpdate(MyConstant.Read_Qrcode_Wrong_Data);
                        continue;
                    }

                    //读取到F6命令,返回上条数据
                    if (realData.get(17) == (byte)0xF6) {
                        //第17位是命令 (标签类型错误)
                        Log.e("ReadThread", "读取类型错误 0xF6");
                        //可能需要重新发送命令
                        //电子相关,重新发送命令
                        Log.e("ReadThread", "读取类型错误,并且重新发送！数据长度为：" + byteData.length);
                        broadcastUpdate(MyConstant.Read_F6_Data);
                        mOutputStream.write(byteData);
                        mOutputStream.flush();
                        continue;
                    }


                    //命令验证
                    if (realData.get(17) == cmd || realData.get(17) == (byte)0x09) {
                        //读取设备告警信息--一直在接收
                        byte[] actual_data = getDataByte(realData);
                        //crc验证 2个字节
                        byte crcFromService1 = actual_data[actual_data.length -2];
                        byte crcFromService2 = actual_data[actual_data.length -3];

                        byte[] crcFromMy = crcMethod(actual_data, 1 ,actual_data.length -3);

                        if (crcFromMy[0] == crcFromService2 && crcFromMy[1] == crcFromService1) {
                            Log.e("helper_run", "数据相等-------");
                            //发送给UI(BluetoothService)
                            broadcastUpdate(MyConstant.Read_Blue_Data,actual_data);

                        } else {
                            //crc验证失败,需要发0xF6给设备
                            mOutputStream.write(wrongByte());
                            mOutputStream.flush();
                            Log.e("ReadThread", "crc验证失败！！");
                            broadcastUpdate(MyConstant.Read_CRC_Wrong_Data);
                        }
                    } else {
                        Log.e("ReadThread", "命令符传输失败！！");
                        broadcastUpdate(MyConstant.Read_Order_Wrong_Data);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                broadcastUpdate(MyConstant.Read_Wronging_Data);
                clientSocket = null;
                isConnected = false;
            }
        }
    }

    /**
     * 将data字节型数据转换为0~255 (0xFF 即BYTE)。
     * @param data
     * @return
     */
    private int getUnsignedByte(byte data){
        return data&0x0FF ;
    }

    /**
     * list转换成byte[]
     * @param list
     * @return
     */
    private byte[] getDataByte(List<Byte> list) {
        byte[] arr = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }

    /**
     * 二维码命令生成
     * @param order
     * @return
     */
    private byte[] scanOrder(byte order) {
        byte[] params = new byte[23];
        params[0] = (byte)0x7E;
        params[1] = (byte)0x00;
        params[2] = (byte)0x10;
        params[3] = (byte)0x00;
        params[4] = (byte)0x00;
        //预留字段全部0
        for(int i = 0; i < 12; i++) {
            params[5 + i] = (byte)0x00;
        }
        params[17] = (byte)order;//命令码
        params[18] = (byte)0x31;
        params[19] = (byte)0xff;//状态码
        //crc校验
        byte[] crcArr = crcMethod(params, 1, params.length -3);
        params[20] = crcArr[0];
        params[21] = crcArr[1];
        //包尾
        params[22] = (byte)0x7e;
        return params;
    }

    /**
     * 错误数据提示
     * @return 数据
     */
    private byte[] wrongByte() {
        byte[] params = new byte[25];
        params[0] = (byte)0x7E;
        params[1] = (byte)0x00;
        params[2] = (byte)0x10;
        params[3] = (byte)0x00;
        params[4] = (byte)0x00;
        //预留字段全部0
        for(int i = 0; i < 12; i++) {
            params[5 + i] = (byte)0x00;
        }
        params[17] = (byte)0xf6;//命令码
        params[18] = (byte)0x11;
        params[19] = (byte)0xff;//状态码
        params[20] = (byte)0x00;//消息体(预留)
        params[21] = (byte)0x00;//预留
        //crc校验
        byte[] crcArr = crcMethod(params, 1, params.length -3);
        params[22] = crcArr[0];
        params[23] = crcArr[1];
        //包尾
        params[24] = (byte)0x7e;
        return params;
    }

    /**
     * 按照华为的规则转换数组(华为格式==>真实数据)
     * @param b 原始数据(华为格式)
     * @return 真实数据
     */
    private List<Byte> transformByRealData(byte[] b) {
        List<Byte> list = new ArrayList<Byte>();
        list.add(b[0]);
        //第0位不转换，和最后位不转换
        for(int i = 1; i < b.length-1; i++) {
            if(b[i] == 0x7d && b[i+1] == 0x5e) {
                //0x7d,0x5e ==> 0x7e
                list.add((byte)0x7e);
                i++;
            } else if(b[i] == 0x7d && b[i+1] == 0x5d) {
                //0x7d,0x5d ==> 0x7d
                list.add((byte)0x7d);
                i++;
            } else {
                list.add(b[i]);
            }
        }
        list.add(b[b.length-1]);
        return list;
    }

    /**
     * 转换为华为需要的格式(真实数据==>华为格式)
     * @param b 真实数据
     * @return 华为格式数据
     */
    private byte[] transformByHuaWei(byte[] b) {
        List<Byte> list_result = new ArrayList<Byte>();
        list_result.add(b[0]);
        for (int i = 1; i < b.length -1; i++) {
            if (b[i] == 0x7e) {
                list_result.add((byte) 0x7d);
                list_result.add((byte) 0x5e);
            } else if (b[i] == 0x7d) {
                list_result.add((byte) 0x7d);
                list_result.add((byte) 0x5d);
            } else {
                list_result.add(b[i]);
            }
        }
        list_result.add(b[b.length-1]);

        byte [] arr = new byte[list_result.size()];
        for(int i = 0; i < list_result.size(); i++) {
            arr[i] = list_result.get(i);
        }
        return arr;
    }

    /**
     * 计算crc16
     * @param b 任何一个命令
     * @param start 去掉头 0x7e
     * @param end   去掉尾3个字节 [crc1 crc2 0x7e]
     * @return  的到crc两个字节，填入原来的[crc1 crc2]里 (低字节,高字节)
     */
    private byte[] crcMethod(byte[]b, int start,int end){
        byte[] data = new byte[end-start];
        int j = 0;
        for (int i = start; i < end; i++) {
            data[j] = b[i];
            j++;
        }
        int result = crc_calByByte(data);
        byte[] arr = new byte[2];
        arr[0] = (byte)(result & 0xff);//低字节
        arr[1]= (byte) ((result>>8 )& 0xff);//高字节
        return arr;
    }

    /**
     * @param b 去掉头尾，真正要计算的数据
     * @return
     */
    public int crc_calByByte(byte[] b){
        int crc_reg = 0x0000;//数据反转 lsb first
        return  do_crc(crc_reg, b);
    }

    /**
     * crc16 ccit欧版  查表法 多项式1021
     * @param reg_init
     * @param message
     * @return
     */
    private static int do_crc(int reg_init, byte[] message) {
        int crc_reg = reg_init;
        for (int i = 0; i < message.length; i++) {
            crc_reg = (crc_reg >> 8) ^ crc16_ccitt_table[(crc_reg ^ message[i]) & 0xff];
        }
        return crc_reg;
    }

    /**
     * 发送广播
     */
    private void broadcastUpdate(String action) {
        Intent intent = new Intent(action);
        //intent.putExtra(EXTRA_DATA, String.valueOf(rssi));
        mContext.sendBroadcast(intent);
    }

    /**
     * 发送广播
     */
    private void broadcastUpdate(String action,byte[] data) {
        Intent intent = new Intent(action);
        intent.putExtra("data", data);
        mContext.sendBroadcast(intent);
    }
    /**
     * 最新版蓝牙读取数据
     * @author Administrator
     *
     */
//    private class NewReadCodeThreadAsyncTask extends AsyncTask<String, String, String> {
//
//        @Override
//        protected String doInBackground(String... params) {
//            try {
//                boolean wrongFlag = false;//错误数据
//                while (true) {
//
//                    if (mInputStream == null) {
//                        Log.e("readThread_run", "输入输出流为空");
//                        return null;
//                    }
//
//                    //先读一个字段
//                    int temp = mInputStream.read();
//
//                    Log.e("helper_监听数据", "---------------temp = " + temp);
//
//                    //如果没发命令，读取到流中的数据要去除
//                    if((byte) 0x7E != temp) {
//                        continue;
//                    }
//
//                    //TODO start
//                    if(wrongFlag) {
//                        //遇到7e，7e数据，丢掉刚开始的7e
//                        //重置flag
//                        wrongFlag = false;
//                        continue;
//                    }
//                    //TODO end
//
//                    int i = 1;
//                    List<Byte> list = new ArrayList<Byte>();
//                    list.add((byte)temp);
//
//                    while((temp = mInputStream.read())!=-1)
//                    {
//                        list.add((byte)temp);
//                        i++;
//                        //如果结尾是7e就退出
//                        if(temp == 0x7E) {
//                            break;
//                        }
//                        if(i == 1400) {
//                            //超过最大长度，退出
//                            break;
//                        }
//                    }
//
//                    //转换成真实数据
//                    List<Byte> realData = transformByRealData(getDataByte(list));
//
//                    //TODO 测试用
//                    StringBuffer buffer = new StringBuffer();
//                    for(int g = 0; g < realData.size(); g++) {
//                        buffer.append(Integer.toHexString(getUnsignedByte(realData.get(g))) + " ");
//                    }
//                    Log.e("ReadThread", "list(转换后) = " + buffer.toString());
//                    Log.e("ReadThread", "list.size(转换后) = " + realData.size());
//
//                    Message arr_msg = new Message();
//                    //返回数据错误，不符合要求
//                    arr_msg.what = 3;
//
//                    //判断数据长度，小于23肯定出错
//                    int size = realData.size();
//                    if(size < 23) {
//                        //TODO start
//                        if(size == 2 && realData.get(0)==(byte)0x7e && realData.get(1)==(byte)0x7e) {
//                            //遇到7e，7e数据时，下个7e也不能接受
//                            wrongFlag = true;
//                        }
//                        //TODO end
//
//                        Log.e("ReadThread", "数据长度不对，小于23个！！,size = " + size);
//                        mHandler.sendMessage(arr_msg);
//                        continue;
//                    }
//
//                    if (realData.get(18) == (byte)0x31 && realData.get(17) == (byte)0x08) {
//                        //如果是扫描枪的命令，就不重新发送
//                        //标签类型错误
//                        mHandler.sendMessage(mHandler.obtainMessage(5));
//                        continue;
//                    }
//
//                    //读取到F6命令,返回上条数据
//                    if (realData.get(17) == (byte)0xF6) {
//                        //第17位是命令 (标签类型错误)
//                        Log.e("ReadThread", "读取类型错误 0xF6");
//                        //可能需要重新发送命令
//                        //电子相关,重新发送命令
//                        Log.e("ReadThread", "读取类型错误,并且重新发送！数据长度为：" + byteData.length);
//                        mHandler.sendMessage(mHandler.obtainMessage(5));
//                        mOutputStream.write(byteData);
//                        mOutputStream.flush();
//                        continue;
//                    }
//
//
//                    //命令验证
//                    if (realData.get(17) == cmd || realData.get(17) == (byte)0x09) {
//                        //读取设备告警信息--一直在接收
//                        byte[] actual_data = getDataByte(realData);
//                        //crc验证 2个字节
//                        byte crcFromService1 = actual_data[actual_data.length -2];
//                        byte crcFromService2 = actual_data[actual_data.length -3];
//
//                        byte[] crcFromMy = crcMethod(actual_data, 1 ,actual_data.length -3);
//
//                        if (crcFromMy[0] == crcFromService2 && crcFromMy[1] == crcFromService1) {
//                            Log.e("helper_run", "数据相等-------");
//                            //发送给UI(BluetoothService_su)
//                            Message msg = serviceHandler.obtainMessage();
//                            msg.what = 2;
//
//                            Bundle data = new Bundle();
//                            data.putByteArray("data", actual_data);
//                            msg.setData(data);
//                            serviceHandler.sendMessage(msg);
//
//                            Log.v("ReadThread", "cmd=" + cmd);
//
//                        } else {
//                            //crc验证失败,需要发0xF6给设备
//                            arr_msg.what = 5;
//                            mOutputStream.write(wrongByte());
//                            mOutputStream.flush();
//                            Log.e("ReadThread", "crc验证失败！！");
//                            mHandler.sendMessage(arr_msg);
//                        }
//                    } else {
//                        Log.e("ReadThread", "命令符传输失败！！");
//                        mHandler.sendMessage(arr_msg);
//                    }
//
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                mHandler.sendMessage(mHandler.obtainMessage(6));
//                clientSocket = null;
//            }
//            return null;
//        }
//    }

}
