package com.cn.zhaol.demo.androidtest.networks;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Retrofit将 Http请求 抽象成 Java接口：采用 注解 描述网络请求参数 和配置网络请求参数
 * Created by zhaolei on 2018/8/9.
 */
public interface NetworkService {

    //--------------------GET请求--------------------//

    @GET("PortInfoLikeQueryAction?portInfo=222&pageType=noId&page=0&rows=0")
    Call<ResponseBody> doQueryMsg1();

    // @GET注解的作用:采用Get方法发送网络请求(后面是请求的部分url)
    //网络请求的完整 Url =在创建Retrofit实例时通过.baseUrl()设置 +网络请求接口的注解设置(框号里面的url)

    // doQueryMsg() = 接收网络请求数据的方法
    // 其中返回类型为Call<*>，*是接收数据的类（即上面定义的Translation类）
    // 如果想直接获得Responsebody中的内容，可以定义网络请求返回值为Call<ResponseBody>

    @GET("PortInfoLikeQueryAction?portInfo=222&pageType=noId&page=0&rows=0")
    Call<ResponseBody> doQueryMsg2();


    //--------------------POST请求，传递键值对--------------------//

    @FormUrlEncoded
    @POST("PortInfoLikeQueryAction")
    Call<ResponseBody> doPostTest1(@Field("portInfo") int val, @Field("pageType") String type,
                                   @Field("page") int page, @Field("rows") int rows);

}
