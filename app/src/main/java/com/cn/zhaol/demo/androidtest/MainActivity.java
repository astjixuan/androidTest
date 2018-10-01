package com.cn.zhaol.demo.androidtest;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.cn.zhaol.demo.androidtest.bottomview.Main2Activity;
import com.cn.zhaol.demo.androidtest.swipe.mydemo.DemoActivity;
import com.cn.zhaol.demo.androidtest.swipe.mydemo.Hua1Activity;
import com.cn.zhaol.demo.androidtest.swipe.mydemo.Second2Activity;
import com.cn.zhaol.demo.androidtest.swipe.mydemo.SecondActivity;
import com.cn.zhaol.demo.androidtest.vlayout.RootActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static List<BluetoothDevice> BlueDevices = new ArrayList<BluetoothDevice>();
    public final static String add_devices = "add_devices";//添加蓝牙设备的广播
    private boolean hasBLE = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.ble_btn).setOnClickListener(this);
        findViewById(R.id.classic_btn).setOnClickListener(this);
        findViewById(R.id.huaDong_btn1).setOnClickListener(this);
        findViewById(R.id.huaDong_btn2).setOnClickListener(this);
        findViewById(R.id.huaDong_btn3).setOnClickListener(this);
        findViewById(R.id.huaDong_btn4).setOnClickListener(this);
        findViewById(R.id.huaDong_btn5).setOnClickListener(this);
        findViewById(R.id.huaDong_btn6).setOnClickListener(this);
        findViewById(R.id.huaDong_btn7).setOnClickListener(this);
        findViewById(R.id.huaDong_btn8).setOnClickListener(this);

        //手机硬件支持蓝牙
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(this, "Not support BLE", Toast.LENGTH_SHORT).show();
            hasBLE = false;
        }

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.ble_btn:
                if(hasBLE) {
                    BlueDevices.clear();
                    intent.setClass(MainActivity.this,BLEActivity.class);
                    startActivity(intent);
                }
                break;

            case R.id.huaDong_btn1:
                //滑动测试
                intent.setClass(MainActivity.this,Hua1Activity.class);
                startActivity(intent);
                break;

            case R.id.huaDong_btn2:
                //滑动测试2
                intent.setClass(MainActivity.this,SecondActivity.class);
                startActivity(intent);
                break;

            case R.id.huaDong_btn3:
                //例子二的改造
                intent.setClass(MainActivity.this,Second2Activity.class);
                startActivity(intent);
                break;

            case R.id.huaDong_btn4:
                //第三方框架(SwipeBackLayout)
                intent.setClass(MainActivity.this,DemoActivity.class);
                startActivity(intent);
                break;

            case R.id.huaDong_btn5:
            case R.id.huaDong_btn6:
                //okHttp例子
                //Retrofit例子
                intent.setClass(MainActivity.this,OkHttpActivity.class);
                startActivity(intent);
                break;

            case R.id.huaDong_btn7:
                //vlayout(alibaba)框架
                intent.setClass(MainActivity.this,RootActivity.class);
                startActivity(intent);
                break;

            case R.id.huaDong_btn8:
                //tabLayout框架(flyco.tablayout例子)
                intent.setClass(MainActivity.this,Main2Activity.class);
                startActivity(intent);
                break;

            default:
                BlueDevices.clear();
                intent.setClass(MainActivity.this,ClassicBlueActivity.class);
                startActivity(intent);
                break;
        }
    }
}
