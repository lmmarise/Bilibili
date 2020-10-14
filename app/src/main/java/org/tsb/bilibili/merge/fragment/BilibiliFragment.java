package org.tsb.bilibili.merge.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lxj.xpopup.impl.CustomJzvd.MyJzvdStd;

import org.tsb.bilibili.merge.R;
import org.tsb.bilibili.merge.adapter.BilibiliRecyclerViewAdapter;
import org.tsb.bilibili.merge.constant.BILIBILI;
import org.tsb.bilibili.merge.utils.BackHandlerHelper;
import org.tsb.bilibili.merge.utils.VideoUtil;
import org.tsb.bilibili.pojo.VideoItem;
import org.tsb.bilibili.util.FileUtils;
import org.tsb.bilibili.util.HashKit;
import org.tsb.bilibili.util.image.ImageUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.jzvd.Jzvd;

public class BilibiliFragment extends BaseFragment {

    private static final String TAG = BilibiliFragment.class.getSimpleName();
    // SD卡上缓存有的视频
    private ArrayList<VideoItem> mVideoItemList = new ArrayList<>();
    private RecyclerView mRecyclerView = null;
    private BilibiliRecyclerViewAdapter mAdapter = null;
    // 避免点击当前页面造成recycle重新加载
    private boolean firstIn = true;

    /*==========================================静态方法=============================================*/

    public static BilibiliFragment newInstance() {
        return new BilibiliFragment();
    }

    /*======================================帧布局生命周期============================================*/
    @Override
    public int bindLayout() {
        return R.layout.fragment_bilibili;
    }

    @Override
    public void initView(View view) {
        mRecyclerView = view.findViewById(R.id.bilibili_rv);
        setStyle();
    }

    /**
     * 业务逻辑
     *
     * @param context {@link androidx.fragment.app.FragmentActivity}
     */
    @Override
    public void doBusiness(Context context) {
        // 初始化rv
        initRecycle(context);
    }

    @Override
    public void widgetClick(View v) {
    }

    @SuppressLint("WrongConstant")
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            // 修改actionBar
            getActivity().findViewById(R.id.iv_back).setVisibility(View.INVISIBLE);
            getActivity().findViewById(R.id.iv_download).setVisibility(View.INVISIBLE);
            TextView tvTitle = getActivity().findViewById(R.id.tv_title);
            tvTitle.setText("缓存的视频");

            ImageView ivDelete = getActivity().findViewById(R.id.iv_delete);
            ImageView ivMenu = getActivity().findViewById(R.id.iv_menu);
            // 判断是否有视频被勾选
            if (BilibiliRecyclerViewAdapter.mCheckedVideoItemList.size() > 0) {
                ivDelete.setVisibility(View.VISIBLE);
                ivMenu.setVisibility(View.VISIBLE);
            } else {
                ivDelete.setVisibility(View.INVISIBLE);
                ivMenu.setVisibility(View.INVISIBLE);
            }
        }

    }

    @Override
    public boolean onBackPressed() {
        Log.d(TAG, "onBackPressed: B站Fragment监听到了返回事件");
        // 将back事件分发给Fragment中的子Fragment
        if (BackHandlerHelper.handleBackPress(this)) {
            return true;
        }
        // 当前Fragment来处理
        else {
            // 将全屏播放转为半屏播放
            for (MyJzvdStd myJzvdStd : MyJzvdStd.jzvdStdList) {
                if (myJzvdStd.isFullScreen()) {
                    myJzvdStd.gotoNormalScreen();
                    return true;
                }
            }
        }
        return false;
    }

    /*=========================================业务逻辑===============================================*/

    /**
     * 准备循环列表的数据
     */
    private void initRecycle(Context context) {
        // 设置rv适配器
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = new BilibiliRecyclerViewAdapter(context, mVideoItemList);
        // 设置videoItem勾选事件
        mAdapter.setMCheckVideoItemListener(new BilibiliRecyclerViewAdapter.OnCheckedVideoItemListener() {
            @Override
            public void check() {
                actionBar();
            }

            @Override
            public void uncheck() {
                actionBar();
            }

            private void actionBar() {
                FragmentActivity activity = BilibiliFragment.this.getActivity();
                if (activity != null) {
                    ImageView ivDelete = activity.findViewById(R.id.iv_delete);
                    ImageView ivMenu = activity.findViewById(R.id.iv_menu);
                    if (BilibiliRecyclerViewAdapter.mCheckedVideoItemList.size() > 0) {
                        ivDelete.setVisibility(View.VISIBLE);
                        ivMenu.setVisibility(View.VISIBLE);
                    } else {
                        ivDelete.setVisibility(View.INVISIBLE);
                        ivMenu.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        // rv添加数据
        addData(context);
    }

    /**
     * 第一次启动activity就向recycle中插入数据
     */
    private void addData(Context context) {
        if (firstIn) {
            insertVideoItemIntoAdapter(getContext());
            firstIn = false;    // 不再是第一次启动activity了
        } else if (mAdapter.getItemCount() <= 0) {
            tipNoData(getContext());
        }
    }

    /**
     * 读取B站app目录下的视频缓存, 放入适配器
     */
    private void insertVideoItemIntoAdapter(Context context) {
        new Thread(() -> {
            prepareRecycleViewData(BILIBILI.DEFAULT_DOWNLOAD_PATH, context).size();// 标准版
        }).start();
        new Thread(() -> {
            prepareRecycleViewData(BILIBILI.DEFAULT_DOWNLOAD_BLUE_PATH, context).size();// 概念版
        }).start();
    }

    /**
     * 读取B站APP下载目录下的文件夹数量
     *
     * @param downloadPath
     * @return 新增的数据集合
     */
    private ArrayList<VideoItem> prepareRecycleViewData(String downloadPath, Context context) {
        ArrayList<VideoItem> list = new ArrayList<>();
        // B站下载目录
        File biliAppDownFile = new File(downloadPath);
        // 如果不是目录,直接退出
        if (!biliAppDownFile.exists() || !biliAppDownFile.isDirectory()) return list;
        // B站的缓存视频目录有两层     .../tv.danmaku.bili/download/371286000+/1+/64/video.m4s
        for (File firstFloor : Objects.requireNonNull(biliAppDownFile.listFiles())) {   // 获取当前目录下的第一层子目录, 字符串路径
            // 来到371286000这层目录
            if (firstFloor.isFile()) continue;
            for (File secondFloor : Objects.requireNonNull(firstFloor.listFiles())) {   // 获取当前目录下的第一层子目录, 字符串路径
                // 来到1这层目录
                if (secondFloor.isFile()) continue;
                // 搜索当前目录下的视频,并将视频信息封装为Java对象
                VideoItem videoItem = encapsulateVideoInfo(secondFloor, context);// 传入的是 .../tv.danmaku.bili/download/371286000+/1+/
                // 视频信息存入Map, 目录数字名作为key, 视频信息对象作为value
                if (videoItem != null) {
                    // list.add(videoItem);
                    // 使用主线程向recycle插入数据
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> mAdapter.addData(videoItem));
                    } else {
                        ((Activity) context).runOnUiThread(() -> mAdapter.addData(videoItem));
                    }
                }
            }
        }
        return list;
    }

    /**
     * 将一个文件夹下的视频信息封装为一个videoItem对象
     */
    public VideoItem encapsulateVideoInfo(File dir, Context context) {
        try {
            // 设置视频名称
            File jsonFile = FileUtils.searchFiles(dir, BILIBILI.ENTRY_JSON_FILENAME, true).get(0);// 搜索路径下的保存本视频信息的json文件
            String videoName = null;        // 解析视频信息获取videoName
            if (jsonFile.exists()) {        // 解析json获取视频的`标题`
                // TODO
                // 获取到json文件,包含父视频信息+可能存在的子视频信息
                HashMap<String, String> fatherJSONMap = parseJSONFile2Map(jsonFile.getAbsolutePath());
                // 判断当前视频是否能播放(下载完成?),不能直接返回
                if (Objects.equals(fatherJSONMap.get(BILIBILI.ENTRY_JSON_KEY_CAN_PLAY_IN_ADVANCE), "false"))
                    return null;
                // 有子视频信息  "ep"
                if (fatherJSONMap.containsKey(BILIBILI.ENTRY_JSON_KEY_EP)) {
                    Object subJSONStr = (Object) fatherJSONMap.get(BILIBILI.ENTRY_JSON_KEY_EP);
                    JSONObject jsonObject = (JSONObject) subJSONStr;
                    if (jsonObject != null) {
                        videoName = (String) jsonObject.get(BILIBILI.ENTRY_JSON_KEY_INDEX_TITLE);
                        videoName = fatherJSONMap.get(BILIBILI.ENTRY_JSON_KEY_TITLE) + "：" + videoName;
                    }
                } else {
                    videoName = fatherJSONMap.get(BILIBILI.ENTRY_JSON_KEY_TITLE);
                }
            }
            // 如果获取JSON里面的title作为标题失败,就使用文件名作为标题
            if (videoName == null) {
                // 获取到的应该是: "371286000/1"
                videoName = dir.getAbsolutePath().split(BILIBILI.DEFAULT_DOWNLOAD_PATH)[1];
            }

            // 搜索路径下的视频文件
            File video = FileUtils.searchFiles(dir, BILIBILI.VIDEO_FILENAME, true).get(0);
            // 如果视频都不存在直接跳过当前视频
            if (!video.isFile()) return null;
            // 搜索路径下的音频文件
            File audio = FileUtils.searchFiles(dir, BILIBILI.AUDIO_FILENAME, true).get(0);
            // 搜索弹幕文件
            File bullet = FileUtils.searchFiles(dir, BILIBILI.BULLET_FILENAME, true).get(0);
            VideoItem videoItem = new VideoItem();
            // 准备item缩略图 item图片
            Bitmap icon = setVideoTempIcon(video, context);
            // 保存item数据
            videoItem.setVideoName(removeFileNotSupportChar(videoName))
                    .setDirPath(dir.getAbsolutePath())
                    .setAudioPath(audio.getAbsolutePath())
                    .setVideoPath(video.getAbsolutePath())
                    .setBulletPath(bullet.getAbsolutePath())
                    .setIconTemp(icon);
            return videoItem;
        } catch (Exception e) {
            return null;
        }
    }

    /*=======================================重写顶层父类==============================================*/

    @Override
    public void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos();
    }

    /*========================================私有方法================================================*/

    /**
     * 设置当前帧的样式
     */
    private void setStyle() {
    }

    /**
     * 将指定路径的JSON文件转为HashMap对象
     */
    private HashMap<String, String> parseJSONFile2Map(String jsonFilePath) {
        String jsonStr = null;
        try {
            jsonStr = FileUtils.readFileAsString(jsonFilePath, null);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "解析视频信息文件失败!", Toast.LENGTH_LONG).show();
        }
        return JSON.parseObject(jsonStr, HashMap.class);
    }

    /**
     * 获取视频缩略图
     * 判断视频文件是否有缩略图缓存, 有直接读取缓存
     * 没有就截取视频某一帧作为缩略图, 并存为缓存
     *
     * @param videoFile 视频文件
     */
    private Bitmap setVideoTempIcon(File videoFile, Context context) {
        // 根据视频全路径+视频名计算md5值, 将md5值作为缓存文件后缀
        String tempIconFilepath = context.getCacheDir().getAbsolutePath() + "/" +
                BILIBILI.TEMP_FILE_PREFIX +
                HashKit.md5(videoFile.getAbsolutePath());
        File icon = new File(tempIconFilepath);
        if (icon.exists()) {    // 缩略图存在, 直接转Bitmap
            try {
                FileInputStream stream = new FileInputStream(icon);
                return BitmapFactory.decodeStream(stream);
            } catch (FileNotFoundException e) {     // 视频中获取失败就使资源目录下的图片来代替
                e.printStackTrace();
                return ImageUtil.res2Bitmap(context, R.drawable.image_icon);
            }
        } else {
            // 使用ffmpeg从视频中截取
            return VideoUtil.captureVideoThumbnail(videoFile.getAbsolutePath(), tempIconFilepath);
        }
    }

    /**
     * 替换文件名中非法字符
     */
    private String removeFileNotSupportChar(String fileName) {
        Pattern pattern = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]");
        Matcher matcher = pattern.matcher(fileName);
        return matcher.replaceAll("-");
    }

    /**
     * 没有数据弹出提示框
     */
    private void tipNoData(Context context) {
        new AlertDialog.Builder(context)
                .setIcon(R.drawable.ic_tips)
                .setTitle("Tips")
                .setMessage("你还没有缓存视频, 快去B站App缓存吧")
                .setPositiveButton("确认", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
