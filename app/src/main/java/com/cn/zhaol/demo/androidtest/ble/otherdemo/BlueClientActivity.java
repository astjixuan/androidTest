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

import java.util.ArrayList;
import java.util.List;

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
    private volatile boolean isRunning = false;//是否正在运行

    private List<String> devices = new ArrayList<>();
    private DeviceListAdapter listAdapter;
    private Intent serviceIntent;//蓝牙service
    private ClassicBlueToothClientService.MyBinder serviceBinder;

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
        registerReceiver(receiver, filter);

        scanBtn = (Button) findViewById(R.id.client_start_searchBtn);
        scanBtn.setOnClickListener(this);
        findViewById(R.id.client_sendMsgBtn).setOnClickListener(this);
        findViewById(R.id.client_stopBtn).setOnClickListener(this);
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
                    serviceBinder.sendMsg(Integer.parseInt(val));
                }
                break;

            default:
                //断开连接
                serviceBinder.closeClient();
                break;
        }
    }

    private void startScan() {
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
        String[] addr = val.split(":");//"addr:name"
        Log.w("***","点击了：position = "+position+",设备："+ addr[0] + " - "+addr[1]);
        //去连接设备，分2步骤
        //1，如果没有配对，先进行配对
        //2，配对成功后，连接蓝牙
        serviceBinder.stopScan();//因为消耗资源，开始连接后，关闭搜索
        if(!isRunning) {
            isRunning = true;
            DoTask task = new DoTask();
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,addr[0]);
        } else {
            Toast.makeText(BlueClientActivity.this,"正在配对，请稍后",Toast.LENGTH_SHORT).show();
        }
    }




    /**
     * 蓝牙连接处理类
     */
    private class DoTask extends AsyncTask<String,String,String> {

        private BluetoothDevice mDevice;

        @Override
        protected String doInBackground(String... params) {
            BluetoothDevice device = serviceBinder.getDeviceByAddress(params[0]);
            int state = device.getBondState();
            String result = "";
            try {
                switch (state)
                {
                    case BluetoothDevice.BOND_NONE:// 未配对状态
                    case BluetoothDevice.BOND_BONDING:
                        // 正在配对
                        Log.w("***","没配对，开始配对");
                        //TODO 设置PIN码目前不成功
                        //开始配对(打开配对页面)
                        if(serviceBinder.createBond(device.getClass(), device)) {
                            //成功
                            result = "1";
                            mDevice = device;
                        } else {
                            result = "2";
                        }

                        break;

                    case BluetoothDevice.BOND_BONDED:
                        // 已配对状态
                        //成功
                        result = "1";
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if("1".equals(result)) {
                Toast.makeText(BlueClientActivity.this,"配对设备成功！,准备连接",Toast.LENGTH_SHORT).show();
                serviceBinder.connectService(mDevice);//启动连接线程

            } else if("2".equals(result)) {
                Toast.makeText(BlueClientActivity.this,"配对设备连接失败,需要重新配对！",Toast.LENGTH_SHORT).show();
            }
            isRunning = false;
        }
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
            String device = devices.get(position);//"addr:name"
            String[] vals = device.split(":");
            deviceAddress.setText(vals[0]);
            deviceName.setText(vals[1]);
            return view;
        }
    }
}
