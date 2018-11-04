package com.cn.zhaol.demo.androidtest.ble.otherdemo;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cn.zhaol.demo.androidtest.R;
import com.cn.zhaol.demo.androidtest.ble.otherdemo.utils.service.ClassicBlueToothClientService;
import com.cn.zhaol.demo.androidtest.ble.otherdemo.utils.service.ClassicBlueToothServerService;
import com.cn.zhaol.demo.androidtest.ble.tools.TestCrc;

/**
 * 传统蓝牙demo2,既创建客户端，也创建服务器
 * （客户端，手机主动查询模式；服务器，手机被动给其他设备查询）
 */
public class ClassicBlueMainActivity extends AppCompatActivity implements View.OnClickListener{

    private ClassicBlueToothServerService.ServerBinder serverBinder;
    private TextView showVal;
    private TestCrc crcTools = null;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if("to_ui_act".equals(intent.getAction())) {
                //蓝牙发过来的数据
                int i = intent.getIntExtra("byte_arr",-1);
                showVal.append(i+" ");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blue_demo2_main_act);

        IntentFilter filter = new IntentFilter();
        filter.addAction("to_ui_act");
        registerReceiver(receiver, filter);

        findViewById(R.id.startServerBtn).setOnClickListener(this);
        findViewById(R.id.startClientBtn).setOnClickListener(this);
        findViewById(R.id.closeServerBtn).setOnClickListener(this);
        Button btn  = (Button) findViewById(R.id.base_button);
        btn.setText("发生数据测试（11）");
        btn.setOnClickListener(this);
        showVal = (TextView) findViewById(R.id.show_clientval);

        startBlueServer();
        crcTools = new TestCrc();
    }

    @Override
    protected void onDestroy() {
        if(conn != null) {
            unbindService(conn);
            conn = null;
        }
        unregisterReceiver(receiver);
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

            case R.id.closeServerBtn:
                //关闭服务器
                serverBinder.shutdownServer();

                break;

            default:
                //发送数据
                serverBinder.sendMsg(getChangeModeParams(1));

                break;
        }
    }

    /**
     * 蓝牙服务器的服务
     */
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

    /**
     * 主被动切换
     * @param modeType 主动模式2/被动模式1
     * @return
     */
    private byte[] getChangeModeParams(int modeType) {
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
        params[17] = (byte)0xff;//命令码
        params[18] = (byte)0x11;
        params[19] = (byte)0xff;//状态码
        params[20] = (byte)0x06;//消息体
        params[21] = (byte)modeType;
        //crc校验
        byte[] crcArr = crcTools.crcMethod(params, 1,params.length -3);
        params[22] = crcArr[0];
        params[23] = crcArr[1];
        //包尾
        params[24] = (byte)0x7e;
        return params;
    }
}
