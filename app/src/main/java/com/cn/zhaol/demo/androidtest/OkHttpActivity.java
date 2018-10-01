package com.cn.zhaol.demo.androidtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


import com.cn.zhaol.demo.androidtest.networks.NetworkServer;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ok_http);

        findViewById(R.id.okhttp_get01_btn).setOnClickListener(this);
        findViewById(R.id.okhttp_get02_btn).setOnClickListener(this);
        findViewById(R.id.okhttp_post01_btn).setOnClickListener(this);
        findViewById(R.id.okhttp_post02_btn).setOnClickListener(this);
        findViewById(R.id.okhttp_get03_btn).setOnClickListener(this);
        findViewById(R.id.okhttp_get04_btn).setOnClickListener(this);
        findViewById(R.id.okhttp_post03_btn).setOnClickListener(this);
        findViewById(R.id.okhttp_post04_btn).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //-------------OkHttp--------------------//

            case R.id.okhttp_get01_btn:
                //同步get方法
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doGetHttp01();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                break;

            case R.id.okhttp_get02_btn:
                //异步get方法
                doGetHttp02();

                break;

            case R.id.okhttp_post01_btn:
                //同步post方法
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doPostHttp01();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                break;

            case R.id.okhttp_post02_btn:
                //异步post方法
                doPostHttp02();

                break;

            //------------------Retrofit-------------------------//

            case R.id.okhttp_get03_btn:
                //同步get方法
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            NetworkServer ser = new NetworkServer();
                            ser.doGet01();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                break;

            case R.id.okhttp_get04_btn:
                //异步get方法
                NetworkServer ser = new NetworkServer();
                ser.doGet02();

                break;

            case R.id.okhttp_post03_btn:
                //同步post方法

                break;

            case R.id.okhttp_post04_btn:
                //异步post方法

                break;

        }
    }

    //同步get方法
    private void doGetHttp01() throws IOException {
        OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象
        Request.Builder builder = new Request.Builder();
        builder.url("http://192.168.30.45:8080/iodn_new/PortInfoLikeQueryAction?portInfo=222&pageType=noId&page=0&rows=0");
        builder.method("GET",null);//默认是get方法
        Request request = builder.build();
        Call mCall = client.newCall(request);
        Response response = mCall.execute();//得到Response 对象(执行网络请求)
        //得到服务器回复数据，必须在线程中
        if (response.isSuccessful()) {
            Log.d("OkHttpActivity","response.code() == "+response.code());
            Log.d("OkHttpActivity","response.message() == "+response.message());
            Log.d("OkHttpActivity","res.string() == "+response.body().string());
            //此时的代码执行在子线程，修改UI的操作请使用handler跳转到UI线程。
        }
    }

    //异步get
    private void doGetHttp02(){
        OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象
        Request request = new Request.Builder()
                .url("http://192.168.30.45:8080/iodn_new/PortInfoLikeQueryAction?portInfo=222&pageType=noId&page=0&rows=0")
                .build();
        Call mCall = client.newCall(request);
        //异步执行网络请求，自带线程
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //得到结果，注意这个并非在ui线程上
                if(response.isSuccessful()){//回调的方法执行在子线程。
                    Log.d("kwwl","获取数据成功了");
                    Log.d("kwwl","response.code()=="+response.code());
                    Log.d("kwwl","response.body().string()=="+response.body().string());
                }
            }
        });
    }

    //同步post(参数是普通类型)   FromBody传递的是字符串型的键值对
    private void doPostHttp01() throws IOException {
        OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
        formBody.add("portInfo","222");//传递键值对参数
        formBody.add("pageType","noId");//参数
        formBody.add("page","0");//参数
        formBody.add("rows","0");//参数
        Request.Builder builder = new Request.Builder();
        builder.url("http://192.168.30.45:8080/iodn_new/PortInfoLikeQueryAction");
        builder.post(formBody.build());//参数导入
        Call mCall = client.newCall(builder.build());

        Response response = mCall.execute();//得到Response 对象(执行网络请求)
        //得到服务器回复数据，必须在线程中
        Log.d("OkHttpActivity","response.code() == "+response.code());
        if (response.isSuccessful()) {
            Log.e("OkHttpActivity","response post01  ");
            Log.d("OkHttpActivity","response.message() == "+response.message());
            Log.d("OkHttpActivity","res.string() == "+response.body().string());
            //此时的代码执行在子线程，修改UI的操作请使用handler跳转到UI线程。
        }
    }

    //异步post(参数是json)  RequestBody传递的是多媒体,json等
    private void doPostHttp02() {
        OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象
        MediaType JSONTYPE = MediaType.parse("application/json; charset=utf-8");//数据类型为json格式
        String jsonStr = "{\"username\":\"lisi\",\"nickname\":\"李四\"}";//json数据

        RequestBody body = RequestBody.create(JSONTYPE, jsonStr);
        Request request = new Request.Builder()
                .url("http://192.168.30.45:8080/iodn_new/PortInfoLikeQueryAction")
                .post(body)
                .build();

        Call mCall = client.newCall(request);

        //异步执行网络请求，自带线程
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //得到结果，注意这个并非在ui线程上
                Log.e("OkHttpActivity","response post02  ");
                Log.d("kwwl","response.code()=="+response.code());
                if(response.isSuccessful()){//回调的方法执行在子线程。
                    Log.d("kwwl","获取数据成功了");
                    Log.d("kwwl","response.body().string()=="+response.body().string());
                }
            }
        });
    }
}
