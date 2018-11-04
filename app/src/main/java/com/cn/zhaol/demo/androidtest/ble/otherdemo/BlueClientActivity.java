package com.cn.zhaol.demo.androidtest.ble.otherdemo;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cn.zhaol.demo.androidtest.MainActivity;
import com.cn.zhaol.demo.androidtest.R;
import com.cn.zhaol.demo.androidtest.ble.otherdemo.utils.service.ClassicBlueToothClientService;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import com.cn.zhaol.demo.androidtest.ble.tools.TestCrc;

/**
 * 传统蓝牙客户端
 * 搜索蓝牙列表，连接，发送消息
 */
public class BlueClientActivity extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemClickListener{

    private ListView mListView;
    private TextView showMsgTV;
    private EditText editText;
    private Button scanBtn;
    private boolean isScan = false;//是否开启搜索
    //private volatile boolean isRunning = false;//是否正在运行

    private List<String> devices = new ArrayList<>();//(addr;name)
    private DeviceListAdapter listAdapter;
    private Intent serviceIntent;//蓝牙service
    private ClassicBlueToothClientService.MyBinder serviceBinder;
    private TestCrc crcTools = null;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(MainActivity.add_devices)) {
                //设备列表刷新
                String addrs = intent.getStringExtra("addrs");
                if(!devices.contains(addrs)) {
                    //添加设备
                    devices.add(addrs);
                    listAdapter.notifyDataSetChanged();
                }

            } else if(action.equals("to_ui_act")) {
                //蓝牙发过来的数据
                int i = intent.getIntExtra("byte_arr",-1);
                showMsgTV.append(i+" ");
            } else if(action.equals("change_my_btn")) {
                //扫描完成
                Toast.makeText(getApplicationContext(),"蓝牙扫描结束！",Toast.LENGTH_SHORT).show();
                scanBtn.setText("开始搜索蓝牙");
                isScan = false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_client);

        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.add_devices);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction("to_ui_act");
        filter.addAction("change_my_btn");
        registerReceiver(receiver, filter);

        scanBtn = (Button) findViewById(R.id.client_start_searchBtn);
        scanBtn.setOnClickListener(this);
        findViewById(R.id.client_sendMsgBtn).setOnClickListener(this);
        findViewById(R.id.client_stopBtn).setOnClickListener(this);
        findViewById(R.id.client_savedAddrBtn).setOnClickListener(this);
        mListView = (ListView) findViewById(R.id.client_listview);
        showMsgTV = (TextView) findViewById(R.id.client_showVal_TV);
        editText = (EditText) findViewById(R.id.client_editVal);

        listAdapter = new DeviceListAdapter();
        mListView.setAdapter(listAdapter);
        mListView.setOnItemClickListener(this);

        //开启蓝牙客户端服务
        serviceIntent = new Intent(BlueClientActivity.this,
                ClassicBlueToothClientService.class);
        startService(serviceIntent);

        //然后在绑定服务，这样才可以操作服务
        Intent intent_bindService = new Intent();
        intent_bindService.setClass(BlueClientActivity.this,ClassicBlueToothClientService.class);
        bindService(intent_bindService, conn, BIND_AUTO_CREATE);

        crcTools = new TestCrc();

    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //连接成功,返回可操作的service
            serviceBinder = (ClassicBlueToothClientService.MyBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //断开连接
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.client_start_searchBtn:
                //开始搜索蓝牙
                if(!isScan) {
                    startScan();
                } else {
                    stopScan();
                }
                break;

            case R.id.client_sendMsgBtn:
                //发送消息
                String val = editText.getText().toString().trim();
                if(!"".equals(val)) {
                    //serviceBinder.sendMsg(getChangeModeParams(Integer.parseInt(val)));
                    serviceBinder.sendMsg(11);
                }

                break;

            case R.id.client_savedAddrBtn:
                //读取已保存的配对设备
                List<BluetoothDevice> list = serviceBinder.getPairedDevices();
                if(null != list && list.size() > 0) {
                    devices.clear();
                    for(BluetoothDevice d : list) {
                        devices.add(d.getAddress()+";"+d.getName());
                    }
                    listAdapter.notifyDataSetChanged();
                }

                //serviceBinder.sendMsg(setBluePassParams("8888"));

                break;

            default:
                //断开连接
                serviceBinder.closeClient();
                break;
        }
    }

    private void startScan() {
        devices.clear();
        scanBtn.setText("停止搜索蓝牙");
        isScan = true;
        serviceBinder.scanDevice();
    }

    private void stopScan() {
        scanBtn.setText("开始搜索蓝牙");
        isScan = false;
        serviceBinder.stopScan();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        devices.clear();
        devices = null;
        //解除绑定后，结束服务，不然无法结束服务
        if (conn != null) {
            unbindService(conn);
        }
        stopService(serviceIntent);
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String val = (String) parent.getItemAtPosition(position);
        String[] addr = val.split(";");//"addr;name"
        //去连接设备，分2步骤
        //1，如果没有配对，先进行配对
        //2，配对成功后，连接蓝牙
        serviceBinder.stopScan();//因为消耗资源，开始连接后，关闭搜索
        bondBlueDevice(addr[0]);
    }

    /**
     * 配对设备，建立连接
     * @param addr
     */
    private void bondBlueDevice(String addr)
    {
        BluetoothDevice device = serviceBinder.getDeviceByAddress(addr);
        int state = device.getBondState();
        switch (state) {
            case BluetoothDevice.BOND_NONE:// 未配对状态
            case BluetoothDevice.BOND_BONDING:// 正在配对
                Log.w("***","没配对，开始配对");
                //进行配对，这个本身就是异步操作，等待 ACTION_BOND_STATE_CHANGED 返回
                if(!serviceBinder.createBond(device)){
                    //false，失败
                    Toast.makeText(BlueClientActivity.this,"配对设备连接失败,需要重新配对！",Toast.LENGTH_SHORT).show();
                }

                break;

            case BluetoothDevice.BOND_BONDED:
                // 已配对状态
                Log.w("***","已经配对，开始连接！");
                //准备连接
                Toast.makeText(BlueClientActivity.this,"配对设备成功！,准备连接",Toast.LENGTH_SHORT).show();
                serviceBinder.connectService(device);//启动连接线程
                break;
        }
    }

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

    /**
     * 得到蓝牙版本号
     * @return params
     */
    private byte[] getBlueVersionParams() {
        byte[] params = new byte[24];
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
        params[20] = (byte)0x01;//消息体
        //crc校验
        byte[] crcArr = crcTools.crcMethod(params, 1,params.length -3);
        params[21] = crcArr[0];
        params[22] = crcArr[1];
        //包尾
        params[23] = (byte)0x7e;
        return params;
    }

    /**
     * 获得电量
     * @return
     */
    private byte[] getBluePowerParams() {
        byte[] params = new byte[24];
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
        params[20] = (byte)0x02;//消息体
        //crc校验
        byte[] crcArr = crcTools.crcMethod(params, 1,params.length -3);
        params[21] = crcArr[0];
        params[22] = crcArr[1];
        //包尾
        params[23] = (byte)0x7e;
        return params;
    }

    /**
     * 设置蓝牙名称
     * @param name 名称
     * @return
     */
    private byte[] setBlueNameParams(String name) {
        byte[] params = new byte[104];
        try {
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
            params[20] = (byte)0x03;//消息体
            //名字
            byte[] nameArr = name.getBytes("utf-8");
            for(int i = 0; i < 80; i++) {
                //不足80补0
                if(i >= nameArr.length) {
                    params[21 + i] = (byte)0x00;
                } else {
                    params[21 + i] =  nameArr[i];
                }
            }
            //crc校验
            byte[] crcArr = crcTools.crcMethod(params, 1,params.length -3);
            params[101] = crcArr[0];
            params[102] = crcArr[1];
            //包尾
            params[103] = (byte)0x7e;
        } catch (UnsupportedEncodingException e) {
            Log.e("setBlueNameParams","setBlueNameParams() failed",e);
        }
        return params;
    }

    /**
     * 设置蓝牙pin码
     * @param password
     * @return
     */
    private byte[] setBluePassParams(String password) {
        byte[] params = new byte[28];
        try {
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
            params[20] = (byte)0x04;//消息体
            //名字
            byte[] nameArr = password.getBytes("utf-8");
            if(nameArr.length != 4) {
                return null;
            }
            for(int i = 0; i < 4; i++) {
                params[21 + i] =  nameArr[i];
            }
            //crc校验
            byte[] crcArr = crcTools.crcMethod(params, 1,params.length -3);
            params[25] = crcArr[0];
            params[26] = crcArr[1];
            //包尾
            params[27] = (byte)0x7e;
        } catch (UnsupportedEncodingException e) {
            Log.e("setBluePassParams","setBluePassParams() failed",e);
        }
        return params;
    }

    /**
     * 设备适配器(简单写)
     */
    private class DeviceListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if(null != devices && devices.size() > 0) {
                return devices.size();
            } else {
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.device_list_item, null);
            TextView deviceAddress = (TextView) view.findViewById(R.id.tv_deviceAddr);
            TextView deviceName = (TextView) view.findViewById(R.id.tv_deviceName);
            String device = devices.get(position);//"addr;name"
            String[] vals = device.split(";");
            deviceAddress.setText(vals[0]);
            deviceName.setText(vals[1]);
            return view;
        }
    }
}
