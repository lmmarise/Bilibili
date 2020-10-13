package org.tsb.bilibili.merge.utils;

import com.google.gson.Gson;

import org.tsb.bilibili.merge.constant.ENV;
import org.tsb.bilibili.pojo.AppVersion;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpUtil {
    /**
     * 请求API返回的json数据
     *
     * @param url
     * @return
     * @throws Exception
     */
    public static String request_JSON_API(String url) throws Exception {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (Exception e) {
            throw new Exception("请求" + url + "失败: " + e.fillInStackTrace());
        }
    }

    /**
     * 根据json数据和传入的pojo对象类型, 将string类型的json数据转为Java对象
     */
    public static <T> T request_JSON_API_2_POJO(String url, Class<T> clazz, int SECONDS_timeOut) throws Exception {
        try {
            OkHttpClient client = new OkHttpClient()
                    .newBuilder()
                    .callTimeout(SECONDS_timeOut / 2, TimeUnit.SECONDS)
                    .readTimeout(SECONDS_timeOut / 2, TimeUnit.SECONDS)
                    .build();
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            String data = response.body().string();
            return new Gson().fromJson(data, clazz);
        } catch (Exception e) {
            throw new Exception("请求url的数据转换Java对象失败: " + e.fillInStackTrace());
        }
    }
}
