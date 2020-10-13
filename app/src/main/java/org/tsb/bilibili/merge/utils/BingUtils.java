package org.tsb.bilibili.merge.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.tsb.bilibili.merge.constant.ENV;
import org.tsb.bilibili.pojo.BingPictureInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 获取必应每日一图的工具类
 */
public class BingUtils {
    /**
     * 获得bing picture API的json, 解析其中的图片信息, 转化为Java对象
     *
     * @param picAPIUrl 图片API的地址
     * @return 包含图片信息的Java对象
     */
    public static BingPictureInfo.ImagesBean getPicInfoBean(String picAPIUrl) throws Exception {
        try {
            // 获取图片信息json
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(ENV.START_ACTIVITY_DELAY, TimeUnit.SECONDS)
                    .build();
            Request request = new Request.Builder().url(picAPIUrl).build();
            Response response = client.newCall(request).execute();
            String jsonStr = response.body().string();
            // json转Java对象
            BingPictureInfo pictureInfo = json2Object(jsonStr, BingPictureInfo.class);
            // 获取第一张图片的信息
            return pictureInfo.getImages().get(0);
        } catch (Exception e) {
            throw new Exception("解析bing图片API失败: " + e.fillInStackTrace());
        }
    }

    /**
     * 判断文件是否已经下载了
     * @param picName   图片名称
     * @param appPicDownloadDir    本软件的图片下载目录
     */
    public static boolean isPicDownloaded(String picName, String appPicDownloadDir) {
        return new File(appPicDownloadDir + "/" + picName).isFile();
    }

    /**
     * 将输入流存为文件
     *
     * @param inputStream 输入流
     * @param outfile     文件对象, 流要输出到的磁盘文件
     */
    public static void saveInputStreamAsFile(InputStream inputStream, File outfile) throws Exception {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outfile);
            byte[] b = new byte[1024];
            int length;
            while ((length = inputStream.read(b)) > 0) {
                fos.write(b, 0, length);
            }
        } catch (Exception e) {
            throw new Exception("将输入流存为文件失败: " + e.fillInStackTrace());
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * okHttp下载图片为Bitmap
     *
     * @param picUrl 图片地址
     * @return 安卓的图对象
     */
    public static InputStream downloadPicAsInputStream(String picUrl) throws Exception {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(picUrl).build();
            ResponseBody body = client.newCall(request).execute().body();
            return body.byteStream();
        } catch (Exception e) {
            throw new Exception("okHttp获取图片为InputStream失败: " + e.fillInStackTrace());
        }
    }

    /**
     * okHttp下载图片为Bitmap
     *
     * @param picUrl 图片地址
     * @return 安卓的图对象
     */
    public static Bitmap downloadPicAsBitmap(String picUrl) throws Exception {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(picUrl).build();
            ResponseBody body = client.newCall(request).execute().body();
            InputStream in = body.byteStream();
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            return bitmap;
        } catch (Exception e) {
            throw new Exception("okHttp下载图片为Bitmap失败: " + e.fillInStackTrace());
        }
    }

    /**
     * 将传入的对象转成json格式字符串
     */
    public static String object2Json(Object object) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        // 漂亮的打印
        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();
        return gson.toJson(object);
    }

    /**
     * 将json转为对象
     *
     * @param jsonStr json str
     * @param clazz   要被转为的对象的Class对象
     * @param <T>     模板
     * @return 返回被转换的对象
     */
    public static <T> T json2Object(String jsonStr, Class<T> clazz) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.fromJson(jsonStr, clazz);
    }
}
