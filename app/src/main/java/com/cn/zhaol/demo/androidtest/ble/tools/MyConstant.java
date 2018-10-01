package com.cn.zhaol.demo.androidtest.ble.tools;

/**
 * 一些常量
 * Created by zhaolei on 2017/7/20.
 */
public class MyConstant {

    public final static String Read_ToShort_Data = "Read_ToShort_Data";//读取数据过短--错误
    public final static String Read_Qrcode_Wrong_Data = "Read_Qrcode_Wrong_Data";//读取二维码类型--错误
    public final static String Read_F6_Data = "Read_F6_Data";//读取到F6数据--错误
    public final static String Read_CRC_Wrong_Data = "Read_CRC_Wrong_Data";//读取数据的crc--错误
    public final static String Read_Order_Wrong_Data = "Read_Order_Wrong_Data";//读取的命令--错误
    public final static String Read_Wronging_Data = "Read_Wronging_Data";//读取过程中--错误
    public final static String Read_Blue_Data = "Read_Blue_Data";//读取到的数据
    //发送给移动电源（eid蓝牙）
    public static final String sendToEidBluetooth = "send_to_eid_bluetooth";

    //传递字节数组用
    public static final String byteArray = "byte_array";

}
