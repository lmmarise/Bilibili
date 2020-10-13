package org.tsb.bilibili.merge.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tbruyelle.rxpermissions2.RxPermissions;

import org.tsb.bilibili.merge.R;
import org.tsb.bilibili.merge.constant.BING;
import org.tsb.bilibili.merge.constant.ENV;
import org.tsb.bilibili.merge.utils.ActivityUtil;
import org.tsb.bilibili.merge.utils.BingUtils;
import org.tsb.bilibili.merge.utils.HttpUtil;
import org.tsb.bilibili.pojo.AppVersion;
import org.tsb.bilibili.pojo.BingPictureInfo;
import org.tsb.bilibili.util.Utils;
import org.tsb.bilibili.util.image.ImageUtil;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

import static org.tsb.bilibili.merge.utils.ActivityUtil.hideStatusBarTransparent;

public class StartActivity extends AppCompatActivity {
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    //权限
    private RxPermissions rxPermissions = null;
    //需要申请的权限，必须先在AndroidManifest.xml有声明，才可以动态获取权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    // 主页面
    private RelativeLayout start_activity_bg;
    // 当前activity TAG
    private final String TAG = "StartActivity";
    // 屏幕宽,高
    // public static int ScreenWidth = 0;
    // public static int ScreenHeight = 0;
    SharedPreferences sharedPreferences = null;

    /**
     * activity创建
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        try {
//            Thread.sleep(1000); //线程休眠1s，使出现白屏时的效果更加明显
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        super.onCreate(savedInstanceState);
        // 启动页隐藏虚拟按键
        hideBarAndBottomUIMenu();
        // 初始化成员变量 initProperty();
        sharedPreferences = getSharedPreferences("start_activity", Context.MODE_MULTI_PROCESS);
        setContentView(R.layout.activity_start);
        // 找到启动页控件
        start_activity_bg = findViewById(R.id.start_layout);
        // 保存启动页背景图, 修改失败了方便回滚
        Drawable background = start_activity_bg.getBackground();
        // 在主线程中试图进行网络操作会抛出异常: 修改系统策略，放开所有的权限
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        // 验证权限
        allowPermission();
        // 修改启动页画面
        try {
            // 修改UI需要多线程
            updateStartPicture(canModifyBG());
        } catch (Exception e) {
            e.printStackTrace();
            // 修改启动页画面失败, 回复启动页画面
            start_activity_bg.setBackground(background);
            // 出现异常了, 下次进入程序也需要换背景, 直接给它置空
            sharedPreferences.edit().putString("today", "").apply();
        }
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected void hideBarAndBottomUIMenu(){
        //隐藏状态栏
        hideStatusBarTransparent(this);
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
            decorView.setSystemUiVisibility(uiOptions);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }


    /**
     * 是否能修改背景图
     * 实现背景图每日一换效果
     */
    private boolean canModifyBG() {
        Date date = new Date();
        @SuppressLint("SimpleDateFormat") String today = new SimpleDateFormat("dd").format(date);
        String yesterday = sharedPreferences.getString("today", "");
        if ("".equals(yesterday) || !today.equals(yesterday)) {
            // 如果没有或者是今天日期和上次的不一样, 那就更新存储的日期
            sharedPreferences.edit().putString("today", today).apply();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 初始化本类的成员变量
     */
    /*private void initProperty() {
        //context的方法，获取windowManager
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        //获取屏幕对象
        Display defaultDisplay = windowManager.getDefaultDisplay();
        //获取屏幕的宽、高，单位是像素
        ScreenWidth = defaultDisplay.getWidth();
        ScreenHeight = defaultDisplay.getHeight();
    }*/

    /**
     * 修改启动页画面
     *
     * @param b 是否需要修改背景 实现一日一换效果
     * @return
     */
    public void updateStartPicture(boolean b) {
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
        try {
            String cache_picture_path = null;
            BingPictureInfo.ImagesBean imagesBean = getPicFromBing();
            if (b) {
                // 需要换, 就走更新, 去读取必应上今日的图片
                parseImageJsonAndSetBg(imagesBean);
            } else {
                // 不需要换, 就去读取缓存作为启动背景
                cache_picture_path = sharedPreferences.getString("cache_picture_path", "");
                if (cache_picture_path != null && !"".equals(cache_picture_path)) {
                    if (new File(cache_picture_path).isFile()) {
                        // 读取缓存文件作为启动图
                        updateStartActivityBGPicture(BitmapFactory.decodeFile(cache_picture_path)).join();
                    } else {
                        sharedPreferences.edit().putString("today", "").apply();
                        parseImageJsonAndSetBg(imagesBean);
                    }
                } else {
                    parseImageJsonAndSetBg(imagesBean);
                    Log.e(TAG, "updateStartPicture: cache_picture_path没有被存储");
                }
            }
            if (imagesBean != null) {
                // 修改图片完成后 -> 修改启动页图片信息 todo
                TextView textView = findViewById(R.id.start_bg_info);
                StringBuilder sb = new StringBuilder(imagesBean.getCopyright());
                sb.insert(sb.indexOf("©") - 2, "\n");
                textView.setText(sb.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "onCreate: 修改启动页图片失败: ", e.fillInStackTrace());
        }
    }
//        });
//        thread.start();
//        return thread;
//    };

    /**
     * 读取必应上今日的图片信息JSON对象
     *
     * @throws Exception
     */
    private BingPictureInfo.ImagesBean getPicFromBing() throws Exception {
        // 解析必应提供的图片API
        return BingUtils.getPicInfoBean(BING.IMAGE_API_URL);
    }

    /**
     * 解析从图片接口获取的图片json信息, 并设置为启动页的背景图片, 还会将图片缓存到SD卡-data下
     *
     * @param imagesBean 图片信息json
     * @throws Exception
     */
    private void parseImageJsonAndSetBg(BingPictureInfo.ImagesBean imagesBean) throws Exception {
        // 图片完整下载路径
        String picUrl = BING.BASE_URL + imagesBean.getUrl();
        // 图片名称
        String picName = imagesBean.getPictureName();
        // 磁盘上的图片
        File picFile = new File(BING.PIC_DOWNLOAD_PATH + picName);
        // 这张图片已下载就直接使用
        if (picFile.isFile()) {
            // 更新启动页面显示的图片
            updateStartActivityBGPicture(BitmapFactory.decodeFile(picFile.getAbsolutePath())).join();
        }
        // 该文件是目录
        else if (picFile.isDirectory()) {
            throw new Exception("图片名与文件夹名重复: " + picFile.getAbsolutePath());
        }
        // 图片没有下载就下载再使用
        else {
            // 获取网络图片的输入流
            InputStream is = BingUtils.downloadPicAsInputStream(picUrl);
            // 将网络流转Bitmap
            Bitmap rawBitmap = BitmapFactory.decodeStream(is);
            // 将Bitmap根据屏幕大小进行一个裁剪
            Bitmap cutBitmap = new ImageUtil(getApplicationContext()).cutPicFitOnScreen(rawBitmap);
            // 将输入流存到本地磁盘
            BingUtils.saveInputStreamAsFile(ImageUtil.bitmap2InputStream(cutBitmap), picFile);
            // 更新启动页面显示的图片
            updateStartActivityBGPicture(BitmapFactory.decodeFile(picFile.getAbsolutePath())).join();
        }
        // 今日的图片路径缓存起来
        sharedPreferences.edit().putString("cache_picture_path", picFile.getAbsolutePath()).apply();
    }

    /**
     * 修改启动页背景图
     */
    public Thread updateStartActivityBGPicture(Bitmap bm) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // 修改图片
                start_activity_bg.setBackground(new BitmapDrawable(bm));
            }
        });
        thread.start();
        return thread;
    }

    /**
     * app请求用户授予必须要有的权限
     */
    private void allowPermission() {
        //使用兼容库就无需判断系统版本
        rxPermissions = new RxPermissions(this);
        mCompositeDisposable.add(rxPermissions.request(PERMISSIONS_STORAGE).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                if (aBoolean) {// 用户同意了权限
                    // 延迟启动主页面
                    gotoMainAct();
                } else {//用户拒绝了权限
                    Toast.makeText(StartActivity.this, "您拒绝了权限，请往设置里开启权限", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }));
    }

    /**
     * 将本页面展示指定秒数后再前往主页
     */
    private void gotoMainAct() {
        // 在这里检查app是否有新版本
        fetchVersionInfo();
        // 延迟启动MainActivity
        mCompositeDisposable.add(Flowable.timer(ENV.START_ACTIVITY_DELAY, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(next -> {
                    startActivity(new Intent(StartActivity.this, MainActivity.class));
                    finish();
                }));
    }

    /**
     * 获取新版本信息存入与MainActivity绑定的静态变量
     */
    private void fetchVersionInfo() {
        new Thread(() -> {
            try {
                MainActivity.appVersion = HttpUtil
                        .request_JSON_API_2_POJO(
                                ENV.VERSION_INFO_URL,
                                AppVersion.class,
                                5);
            } catch (Exception e) {
                Log.d(TAG, "checkForUpdate: 获取更新数据失败" + e.fillInStackTrace());
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.fixInputMethodManagerLeak(this);
        mCompositeDisposable.clear();
    }
}
