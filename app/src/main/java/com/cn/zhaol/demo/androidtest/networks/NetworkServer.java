package com.cn.zhaol.demo.androidtest.networks;

import android.util.Log;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit服务类
 * Created by zhaolei on 2018/8/9.
 */
public class NetworkServer {

    private Retrofit retrofit;

    public NetworkServer() {
        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.30.45:8080/iodn_new/")
                .build();
    }

    //同步get方法
    public void doGet01() throws IOException {
        // 第2部分：在创建Retrofit实例时通过.baseUrl()设置
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.30.45:8080/iodn_new/") //设置网络请求的Url地址
                //.addConverterFactory(GsonConverterFactory.create()) //设置json数据解析器
                .build();

        NetworkService service = retrofit.create(NetworkService.class);
        Call<ResponseBody> call = service.doQueryMsg1();

        Response<ResponseBody> response = call.execute();
        Log.e("NetworkServer","response.code() == "+response.code());
        if (response.isSuccessful()) {
            Log.d("NetworkServer","response.message() == "+response.message());
            Log.d("NetworkServer","res.string() == "+response.body().string());
            //此时的代码执行在子线程，修改UI的操作请使用handler跳转到UI线程。
        }
    }

    //异步get方法
    public void doGet02() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.30.45:8080/iodn_new/")
                //.addConverterFactory(GsonConverterFactory.create()) //设置json数据解析器
                .build();

        NetworkService service = retrofit.create(NetworkService.class);
        Call<ResponseBody> call = service.doQueryMsg2();//完整的url
        Log.e("NetworkServer","service.doQueryMsg2()");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    Log.e("NetworkServer","response.code()=="+response.code());
                    if(response.isSuccessful()) {//回调的方法执行在子线程。
                        Log.d("NetworkServer","获取数据成功了");
                        Log.d("NetworkServer","response.body().string()=="+response.body().string());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    //同步post方法
    public void doPost01() throws IOException {
        NetworkService service = retrofit.create(NetworkService.class);

        Call<ResponseBody> call = service.doPostTest1(222,"noId",0,0);
        Log.e("NetworkServer","service.doPost01()");

        Response<ResponseBody> response = call.execute();
        Log.e("NetworkServer","response.code() == "+response.code());
        if (response.isSuccessful()) {
            Log.d("NetworkServer","response.message() == "+response.message());
            Log.d("NetworkServer","res.string() == "+response.body().string());
            //此时的代码执行在子线程，修改UI的操作请使用handler跳转到UI线程。
        }
    }

    //异步post方法
    public void doPost02(){
        NetworkService service = retrofit.create(NetworkService.class);

        Call<ResponseBody> call = service.doPostTest1(222,"noId",0,0);
        Log.e("NetworkServer","service.doPost02()");

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    Log.e("NetworkServer","response.code()=="+response.code());
                    if(response.isSuccessful()) {//回调的方法执行在子线程。
                        Log.d("NetworkServer","获取数据成功了");
                        Log.d("NetworkServer","response.body().string()=="+response.body().string());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }
}
