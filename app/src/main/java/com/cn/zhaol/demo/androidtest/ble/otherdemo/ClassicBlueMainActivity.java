package com.cn.zhaol.demo.androidtest.ble.otherdemo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.cn.zhaol.demo.androidtest.R;
import com.cn.zhaol.demo.androidtest.ble.otherdemo.utils.service.ClassicBlueToothClientService;
import com.cn.zhaol.demo.androidtest.ble.otherdemo.utils.service.ClassicBlueToothServerService;

/**
 * 传统蓝牙demo2,既创建客户端，也创建服务器
 * （客户端，手机主动查询模式；服务器，手机被动给其他设备查询）
 */
public class ClassicBlueMainActivity extends AppCompatActivity implements View.OnClickListener{

    private ClassicBlueToothServerService.ServerBinder serverBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blue_demo2_main_act);

        findViewById(R.id.startServerBtn).setOnClickListener(this);
        findViewById(R.id.startClientBtn).setOnClickListener(this);
        findViewById(R.id.base_button).setVisibility(View.GONE);

        startBlueServer();
    }

    @Override
    protected void onDestroy() {
        unbindService(conn);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startServerBtn:
                //开启服务器（被动模式）
                serverBinder.startBlueServer();
                break;

            case R.id.startClientBtn:
                //开启客户端(主动模式)
                Intent clientIntent = new Intent(ClassicBlueMainActivity.this,BlueClientActivity.class);
                startActivity(clientIntent);
                break;

            default:

                break;
        }
    }

    private void startBlueServer() {
        //开启蓝牙客户端服务
//        serviceIntent = new Intent(ClassicBlueMainActivity.this,
//                ClassicBlueToothServerService.class);
//        startService(serviceIntent);

        //然后在绑定服务，这样才可以操作服务
        Intent intent_bindService = new Intent();
        intent_bindService.setClass(ClassicBlueMainActivity.this,ClassicBlueToothServerService.class);
        bindService(intent_bindService, conn, BIND_AUTO_CREATE);
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //连接成功,返回可操作的service
            serverBinder = (ClassicBlueToothServerService.ServerBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //断开连接
        }
    };
}
