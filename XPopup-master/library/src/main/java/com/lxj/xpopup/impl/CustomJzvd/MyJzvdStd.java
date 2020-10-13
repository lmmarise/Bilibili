package com.lxj.xpopup.impl.CustomJzvd;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.lxj.xpopup.R;
import com.lxj.xpopup.impl.ConfirmPopupView;

import org.tsb.bilibili.pojo.VideoItem;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import cn.jzvd.JzvdStd;

/**
 * 这里可以监听到视频播放的生命周期和播放状态
 * 所有关于视频的逻辑都应该写在这里
 * Created by Nathen on 2017/7/2.
 */
public class MyJzvdStd extends JzvdStd {
    // 将由NormalRecyclerViewAdapter, 将弹窗的句柄存到这里
    public static SoftReference<ConfirmPopupView> confirmPopupView = null;

    // 观察者模式, 记录打开的弹窗
    public static List<MyJzvdStd> jzvdStdList = new ArrayList<>(1);

    public MyJzvdStd(Context context) {
        super(context);
    }

    public MyJzvdStd(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void init(Context context) {
        super.init(context);
    }

    /**
     * 监听播放界面上的控件点击事件
     */
    @Override
    public void onClick(View v) {
        super.onClick(v);
        int i = v.getId();
        if (i == cn.jzvd.R.id.fullscreen) {
            Log.i(TAG, "onClick: 全屏控件");
        } else if (i == R.id.start) {
            Log.i(TAG, "onClick: 播放控件");
        }
    }

    /**
     * 捕获全屏状态下的返回按键
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "onTouch: 点击--------------====================++++++++++++");
        super.onTouch(v, event);
        int id = v.getId();
        if (id == cn.jzvd.R.id.surface_container) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    if (mChangePosition) {
                        Log.i(TAG, "Touch screen seek position");
                    }
                    if (mChangeVolume) {
                        Log.i(TAG, "Touch screen change volume");
                    }
                    break;
            }
        }
        return false;
    }

    @Override
    public int getLayoutId() {
        return R.layout.jz_layout_std;
    }


    @Override
    public void startVideo() {
        super.startVideo();
        Log.i(TAG, "startVideo");
        // 取出要播放的视频对象
        ConfirmPopupView popupView = MyJzvdStd.confirmPopupView.get();
        if (popupView != null) {
            // 该视频已转码
            VideoItem videoItem = (VideoItem) popupView.getTag();
            String videoPath = videoItem.getMergedVideoPath();
            if (videoPath != null) {
                File file = new File(videoPath);
                // 视频文件已合并
                if (file.exists() && file.isFile() && file.length() > 0) {
                    setUp(videoPath, null);
                } else {    // 防止已合并的视频播放到一半时被删除
                    // 视频文件未合并
                    Toast.makeText(confirmPopupView.get().getContext(),
                            "您正在播放未合并的视频", Toast.LENGTH_SHORT).show();
                    setUp(videoItem.getVideoPath(), null);
                }
            } else {
                // 视频文件未合并
                Toast.makeText(confirmPopupView.get().getContext(),
                        "您正在播放未合并的视频", Toast.LENGTH_SHORT).show();
                setUp(videoItem.getVideoPath(), null);
            }
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        super.onStopTrackingTouch(seekBar);
        Log.i(TAG, "Seek position ");
    }


    /**
     * 视频播放器进入全屏
     */
    @Override
    public void gotoFullscreen() {
        super.gotoFullscreen();
        Log.i(TAG, "goto Fullscreen");
        // 获取弹窗对象
        if (confirmPopupView.get() != null) {
            // 关闭弹窗,但不关闭视频播放器
            confirmPopupView.get().myDismiss(false);
        }
        jzvdStdList.add(this);
    }

    /**
     * 重写播放器全屏控件点击事件
     */
    @Override
    protected void clickFullscreen() {
        Log.i(TAG, "fullscreen控件被点击 [" + this.hashCode() + "] ");
        if (state == STATE_AUTO_COMPLETE) return;
        // 在全屏状态退出全屏
        if (isFullScreen()) {
            Log.d(TAG, "退出全屏 [" + this.hashCode() + "] ");
            gotoNormalScreen();
        } else {
            Log.d(TAG, "进入全屏 [" + this.hashCode() + "] ");
            gotoFullscreen();
        }
    }

    /**
     * 判断播放器是否在全屏状态
     */
    public boolean isFullScreen() {
        return screen == SCREEN_FULLSCREEN;
    }

    /**
     * 全屏后按返回键
     */
    @Override
    public void gotoNormalScreen() {
        super.gotoNormalScreen();
        Log.i(TAG, "quit Fullscreen");
        if (confirmPopupView.get() != null) {
            // 展示弹窗
            confirmPopupView.get().show();
        }
        jzvdStdList.remove(this);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyUp: 你点击了返回键" + keyCode);
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void autoFullscreen(float x) {
        super.autoFullscreen(x);
        Log.i(TAG, "auto Fullscreen");
    }

    @Override
    public void onClickUiToggle() {
        super.onClickUiToggle();
    }

    //onState 代表了播放器引擎的回调，播放视频各个过程的状态的回调
    @Override
    public void onStateNormal() {
        super.onStateNormal();
    }

    @Override
    public void onStatePreparing() {
        super.onStatePreparing();
    }

    @Override
    public void onStatePlaying() {
        super.onStatePlaying();
    }

    @Override
    public void onStatePause() {
        super.onStatePause();
    }

    @Override
    public void onStateError() {
        super.onStateError();
    }

    @Override
    public void onStateAutoComplete() {
        super.onStateAutoComplete();
        Log.i(TAG, "Auto complete");
    }

    //changeUiTo 真能能修改ui的方法
    @Override
    public void changeUiToNormal() {
        super.changeUiToNormal();
    }

    @Override
    public void changeUiToPreparing() {
        super.changeUiToPreparing();
    }

    @Override
    public void changeUiToPlayingShow() {
        super.changeUiToPlayingShow();
    }

    @Override
    public void changeUiToPlayingClear() {
        super.changeUiToPlayingClear();
    }

    @Override
    public void changeUiToPauseShow() {
        super.changeUiToPauseShow();
    }

    @Override
    public void changeUiToPauseClear() {
        super.changeUiToPauseClear();
    }

    @Override
    public void changeUiToComplete() {
        super.changeUiToComplete();
    }

    @Override
    public void changeUiToError() {
        super.changeUiToError();
    }

    @Override
    public void onInfo(int what, int extra) {
        super.onInfo(what, extra);
    }

    @Override
    public void onError(int what, int extra) {
        super.onError(what, extra);
    }

}