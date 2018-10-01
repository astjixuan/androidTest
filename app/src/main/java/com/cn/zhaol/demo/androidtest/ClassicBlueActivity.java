package com.cn.zhaol.demo.androidtest;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.cn.zhaol.demo.androidtest.ble.classic.BluetoothClassicHelp;
import com.cn.zhaol.demo.androidtest.ble.classic.BluetoothClassicService;
import com.cn.zhaol.demo.androidtest.ble.tools.MyConstant;
import com.cn.zhaol.demo.androidtest.ble.tools.TestCrc;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 经典蓝牙画面
 */
public class ClassicBlueActivity extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemClickListener{

    private final static String TAG = "ClassicBlueActivity";
    private ListView mListView = null;
    private LeDeviceListAdapter adapter = null;
    private BluetoothClassicService bluetoothService = null;
    private TestCrc crcTools = null;
    private ProgressDialog progressDialog;

    //广播
    BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(MainActivity.add_devices)) {
                //搜索到蓝牙结果，刷新页面
                adapter.setLeDevices(MainActivity.BlueDevices);
                adapter.notifyDataSetChanged();
                Log.w("ClassicBlueActivity","更新listview！！！,size = " + MainActivity.BlueDevices.size());
            } else if(action.equals("to_ui_act")) {
                //显示蓝牙数据
                dismissProgress();
                byte[] data = intent.getByteArrayExtra("byte_arr");
                byte command = data[17];
                if((byte)0x01 == command) {
                    //读取名称
                    byte[] arrName = new byte[80];
                    for(int i = 0; i < 80; i++) {
                        arrName[i] = data[20 + i];
                    }
                    try {
                        String machineName = new String(arrName,"utf-8").trim();
                        showDataDialog(machineName);

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    //读取版本号
                    StringBuffer software = new StringBuffer();
                    StringBuffer hardware = new StringBuffer();
                    for(int i = 0; i < 48; i++) {
                        if(i < 24) {
                            byte soft = data[i + 20];
                            software.append(soft + ",");
                        } else {
                            byte hard = data[i + 20];
                            hardware.append(hard + ",");
                        }
                    }
                    String val = "软件版本号：" + software.toString() + ",硬件版本号：" + hardware.toString();
                    showDataDialog(val);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.classic_blue_act);
        IntentFilter intentFilter = new IntentFilter("to_ui_act");
        intentFilter.addAction(MainActivity.add_devices);
        registerReceiver(receiver, intentFilter);


        findViewById(R.id.cl_scanBtn).setOnClickListener(this);
        findViewById(R.id.cl_stopBtn).setOnClickListener(this);
        findViewById(R.id.cl_savesBtn).setOnClickListener(this);
        findViewById(R.id.read_nameBtn).setOnClickListener(this);
        findViewById(R.id.read_verBtn).setOnClickListener(this);
        mListView = (ListView) findViewById(R.id.classic_listview);
        adapter = new LeDeviceListAdapter(getApplicationContext());
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);//listview点击事件
        bluetoothBindService();

        crcTools = new TestCrc();
    }


    // 蓝牙service
    private void bluetoothBindService() {
        Intent intent_bindService = new Intent();
        intent_bindService.setClass(ClassicBlueActivity.this,BluetoothClassicService.class);
        bindService(intent_bindService, mServiceConnection, BIND_AUTO_CREATE);
    }


    // connection 专门做工单的蓝牙服务器
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder service) {
            // service连接成功
            // 强制转换成BluetoothService.MyBinder类型的binder
            bluetoothService = ((BluetoothClassicService.MyBinder) service)
                    .getService();
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cl_scanBtn:
                //开始扫描蓝牙设备
                bluetoothService.startScan();
                break;

            case R.id.cl_stopBtn:
                //停止扫描
                bluetoothService.stopScan();
                break;

            case R.id.cl_savesBtn:
                //得到已经配对保存的蓝牙设备
                List<BluetoothDevice> list = bluetoothService.getBondedDevices();
                if(null != list && list.size()>0) {
                    adapter.setLeDevices(list);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ClassicBlueActivity.this,"没有保存的蓝牙设备！",Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.read_nameBtn:
                //读取设备名称
                if(!BluetoothClassicHelp.isConnected) {
                    Toast.makeText(ClassicBlueActivity.this,"没连接蓝牙，请重试！",Toast.LENGTH_SHORT).show();
                    return;
                }
                byte[] p_name = getNameParams();
                showProgress();
                sendMesToBlue(p_name);

                break;

            case R.id.read_verBtn:
                //读取主控版本号
                if(!BluetoothClassicHelp.isConnected) {
                    Toast.makeText(ClassicBlueActivity.this,"没连接蓝牙，请重试！",Toast.LENGTH_SHORT).show();
                    return;
                }
                byte[] p = getSoftwareParams(1, "", "");
                showProgress();
                sendMesToBlue(p);

                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BluetoothDevice device = (BluetoothDevice) parent.getItemAtPosition(position);
        String addr =device.getAddress();
        String name = device.getName();
        Log.w(TAG,"点击了：position = "+position+",设备："+ addr + " - "+name);
        //选择该设备连接蓝牙
        showMessageDialog(name,device);
    }


    /**
     * 提示用户
     * @param name 设备名称
     * @param device 蓝牙设备
     */
    private void showMessageDialog(String name,final BluetoothDevice device) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(ClassicBlueActivity.this);
        dialog.setTitle("选择蓝牙设备");
        dialog.setMessage("是否选择("+name+")蓝牙设备？");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //搜索蓝牙设备的过程占用资源比较多，一旦找到需要连接的设备后需要及时关闭搜索
                bluetoothService.stopScan();
                //连接配对蓝牙设备
                DoTask task = new DoTask();
                task.setmDevice(device);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                dialog.dismiss();
            }
        });
        dialog.create().show();
    }

    /**
     * 显示结果
     * @param val
     */
    private void showDataDialog(String val) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(ClassicBlueActivity.this);
        dialog.setMessage("结果如下："+val);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.create().show();
    }

    /**
     * 蓝牙连接处理类
     */
    private class DoTask extends AsyncTask<Void,String,String> {

        private BluetoothDevice mDevice;

        public void setmDevice(BluetoothDevice mDevice) {
            this.mDevice = mDevice;
        }

        @Override
        protected String doInBackground(Void... params) {
            int state = mDevice.getBondState();
            String result = "";
            try {
                switch (state)
                {
                    case BluetoothDevice.BOND_NONE:
                        // 未配对状态
                    case BluetoothDevice.BOND_BONDING:
                        // 正在配对
                        Log.w(TAG,"没配对，开始配对");
                        //TODO 设置PIN码目前不成功
                        //boolean flag = BluetoothClassicHelp.autoBond(mDevice.getClass(), mDevice, "0000");//设置pin值
                        boolean flag2 = BluetoothClassicHelp.createBond(mDevice.getClass(), mDevice);//开始配对(打开配对页面)
                        //boolean flag3 = BluetoothClassicHelp.cancelPairingUserInput(mDevice.getClass(), mDevice);//取消用户输入
                        //Log.w(TAG,"flag = " + flag+",flag2 = " +flag2+",flag3="+flag3);
                        //boolean flag2 = bluetoothService.connectBluetooth(mDevice);
                        if(flag2) {
                            //成功
                            result = "1";
                        } else {
                            result = "2";
                        }

                        break;

                    case BluetoothDevice.BOND_BONDED:
                        // 已配对状态
                        Log.w(TAG,"已经配对过了，直接尝试连接");
                        boolean flag = bluetoothService.connectBluetooth(mDevice);
                        if(flag) {
                            //成功
                            result = "3";
                        } else {
                            result = "4";
                        }
                        Log.w(TAG,"连接状态 flag = "+flag);
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
            if("3".equals(result)) {
                Toast.makeText(ClassicBlueActivity.this,"配对设备连接成功！",Toast.LENGTH_SHORT).show();
            } else if("4".equals(result)) {
                Toast.makeText(ClassicBlueActivity.this,"配对设备连接失败,需要重新配对！",Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 得到读取设备名称命令
     * @return params
     */
    private byte[] getNameParams() {
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
        params[17] = (byte)0x01;//命令码
        params[18] = (byte)0x11;
        params[19] = (byte)0xff;//状态码
        params[20] = (byte)0xaa;//消息体
        params[21] = (byte)0x01;
        //crc校验
        byte[] crcArr = crcTools.crcMethod(params, 1,params.length -3);
        params[22] = crcArr[0];
        params[23] = crcArr[1];
        //包尾
        params[24] = (byte)0x7e;
        StringBuffer str = new StringBuffer();
        for(int i = 0; i < params.length; i++) {
            str.append(Integer.toHexString(getUnsignedByte(params[i])) + " ");
        }
        Log.v("params = ",str.toString());
        return params;
    }

    /**
     * 得到版本信息
     * @param softwareTypeInt (1：主控，2：接口板，3：单元板)
     * @param caseNo
     * @param salverNo
     * @return
     */
    public byte[] getSoftwareParams(int softwareTypeInt,String caseNo,String salverNo) {
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
        params[17] = (byte)0x06;//命令码
        params[18] = (byte)0x11;
        params[19] = (byte)0xff;//状态码
        if(1 == softwareTypeInt) {
            params[20] = (byte)0x00;//消息体(框号)
            params[21] = (byte)0x00;//盘号
        } else if(2 == softwareTypeInt) {
            params[20] = (byte)Integer.parseInt(caseNo);//消息体(框号)
            params[21] = (byte)0x00;//盘号
        } else {
            params[20] = (byte)Integer.parseInt(caseNo);//消息体(框号)
            params[21] = (byte)Integer.parseInt(salverNo);//盘号
        }
        //crc校验
        byte[] crcArr = crcTools.crcMethod(params, 1,params.length -3);
        params[22] = crcArr[0];
        params[23] = crcArr[1];
        //包尾
        params[24] = (byte)0x7e;
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < params.length; i++) {
            str.append(Integer.toHexString(getUnsignedByte(params[i])) + " ");
        }
        Log.d("params = ", "getSoftwareParams() returned: " + str.toString());
        return params;
    }

    /**
     * 发送蓝牙数据
     * @param p3
     */
    private void sendMesToBlue(byte[] p3) {
        byte[] pp = crcTools.transformByHuaWei(p3);
        //发送给BluetoothService
        Intent intentF = new Intent();
        intentF.setAction(MyConstant.sendToEidBluetooth);
        Bundle extras = new Bundle();
        extras.putByteArray(MyConstant.byteArray, pp);
        intentF.putExtras(extras);
        sendBroadcast(intentF);
    }

    /**
     * 将data字节型数据转换为0~255 (0xFF 即BYTE)。
     * @param data
     * @return
     */
    private int getUnsignedByte (byte data){
        return data&0x0FF ;
    }

    /**
     * 开启进度dialog
     */
    private void showProgress() {
        if (null == progressDialog) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在写入数据，请稍微！");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // indicator
            progressDialog.setCanceledOnTouchOutside(false);
            //progressDialog.setCancelable(false);
            progressDialog.show();
        } else {
            progressDialog.show();
        }
        //progressDialog.setOnKeyListener(onKeyListener);
    }

    /**
     * dismiss dialog
     */
    private void dismissProgress() {
        if (isFinishing()) {
            return;
        }
        if (null != progressDialog && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if (bluetoothService != null) {
            unbindService(mServiceConnection);
        }
    }
}
