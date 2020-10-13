package com.lxj.xpopup.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.lxj.xpopup.R;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.CenterPopupView;
import com.lxj.xpopup.impl.CustomJzvd.MyJzvdStd;
import com.lxj.xpopup.interfaces.OnCancelListener;
import com.lxj.xpopup.interfaces.OnConfirmListener;

import org.tsb.bilibili.pojo.VideoItem;

import cn.jzvd.Jzvd;

import static android.view.KeyEvent.KEYCODE_BACK;

/**
 * Description: 确定和取消的对话框
 * Create by dance, at 2018/12/16
 */
public class ConfirmPopupView extends CenterPopupView implements View.OnClickListener {
    private static final String TAG = "ConfirmPopupView";
    OnCancelListener cancelListener;
    OnConfirmListener confirmListener;
    TextView tv_title, tv_content, tv_cancel, tv_confirm;
    CharSequence title, content, hint, cancelText, confirmText;
    public boolean isHideCancel = false;
    MyJzvdStd mVideoPlayer;// 视频播放器控件
    Context mContext;
    public Object[] args;

    public ConfirmPopupView(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    /**
     * 绑定已有布局
     *
     * @param layoutId 要求布局中必须包含的TextView以及id有：tv_title，tv_content，tv_cancel，tv_confirm
     */
    public ConfirmPopupView bindLayout(int layoutId) {
        bindLayoutId = layoutId;
        return this;
    }

    @Override
    protected int getImplLayoutId() {
        return bindLayoutId != 0 ? bindLayoutId : R.layout._xpopup_center_impl_confirm;
    }

    @Override
    protected void initPopupContent() {
        super.initPopupContent();
        tv_title = findViewById(R.id.tv_title);
        tv_content = findViewById(R.id.tv_content);
        tv_cancel = findViewById(R.id.tv_cancel);
        tv_confirm = findViewById(R.id.tv_confirm);
        mVideoPlayer = findViewById(R.id.assert_path);

        if (bindLayoutId == 0) applyPrimaryColor();

        tv_cancel.setOnClickListener(this);
        tv_confirm.setOnClickListener(this);

        if (!TextUtils.isEmpty(title)) {
            tv_title.setText(title);
        } else {
            tv_title.setVisibility(GONE);
        }

        if (!TextUtils.isEmpty(content)) {
            tv_content.setText(content);
        } else {
            tv_content.setVisibility(GONE);
        }
        if (!TextUtils.isEmpty(cancelText)) {
            tv_cancel.setText(cancelText);
        }
        if (!TextUtils.isEmpty(confirmText)) {
            tv_confirm.setText(confirmText);
        }
        if (isHideCancel) {
            tv_cancel.setVisibility(GONE);
            View divider = findViewById(R.id.xpopup_divider_h);
            if (divider != null) divider.setVisibility(GONE);
        }
        if (bindItemLayoutId == 0 && popupInfo.isDarkTheme) {
            applyDarkTheme();
        }

        // 初始化视频控件的数据
        initVideoPlayer();
    }

    /*
     * 配置播放器、添加旋转外部旋转支持、增加监听
     * */
    private void initVideoPlayer() {
        VideoItem videoItem = (VideoItem) args[0];
        // Log.d(TAG, "initFFmpegPlayerView: " + videoItem);
        // 设置播放器播放播放地址
        if (videoItem.getMergedVideoPath() != null) {
            // 有合并好的视频就使用合并好的视频
            mVideoPlayer.setUp(videoItem.getMergedVideoPath(), null);
        } else {
            // 没有就使用没有合并的视频
            mVideoPlayer.setUp(videoItem.getVideoPath(), null);
        }
        // 设置视频缩略图
        mVideoPlayer.posterImageView.setImageBitmap(videoItem.getIconTemp());
    }

    protected void applyPrimaryColor() {
//        tv_cancel.setTextColor(XPopup.getPrimaryColor());
        if (bindItemLayoutId == 0) {
            tv_confirm.setTextColor(XPopup.getPrimaryColor());
        }
    }

    @Override
    protected void applyDarkTheme() {
        super.applyDarkTheme();
        tv_title.setTextColor(getResources().getColor(R.color._xpopup_white_color));
        tv_content.setTextColor(getResources().getColor(R.color._xpopup_white_color));
        tv_cancel.setTextColor(getResources().getColor(R.color._xpopup_white_color));
        tv_confirm.setTextColor(getResources().getColor(R.color._xpopup_white_color));
        findViewById(R.id.xpopup_divider).setBackgroundColor(getResources().getColor(R.color._xpopup_dark_color));
        findViewById(R.id.xpopup_divider_h).setBackgroundColor(getResources().getColor(R.color._xpopup_dark_color));
        ((ViewGroup) tv_title.getParent()).setBackgroundResource(R.drawable._xpopup_round3_dark_bg);
    }

    /*
     * 4 设置确认按钮和取消按钮的点击监听器
     * */
    public ConfirmPopupView setListener(OnConfirmListener confirmListener, OnCancelListener cancelListener) {
        this.cancelListener = cancelListener;
        this.confirmListener = confirmListener;
        return this;
    }

    /*
     * 1 设置标题
     * */
    public ConfirmPopupView setTitleContent(CharSequence title, CharSequence content, CharSequence hint) {
        this.title = title;
        this.content = content;
        this.hint = hint;
        return this;
    }

    /*
     * 2 设置取消按钮内容
     * */
    public ConfirmPopupView setCancelText(CharSequence cancelText) {
        this.cancelText = cancelText;
        return this;
    }

    /*
     * 3 设置确认按钮内容
     * */
    public ConfirmPopupView setConfirmText(CharSequence confirmText) {
        this.confirmText = confirmText;
        return this;
    }

    /*
     * 5 设置额外参数
     * */
    public void setArgs(Object[] args) {
        this.args = args;
    }

    /*
     * 点击弹出窗口的确认和取消按钮都会出发本方法
     * */
    @Override
    public void onClick(View v) {
        if (v == tv_cancel) {
            if (cancelListener != null) cancelListener.onCancel();
            // 关闭所有视频
            Jzvd.releaseAllVideos();
            dismiss();
        } else if (v == tv_confirm) {
            if (confirmListener != null) confirmListener.onConfirm();
            // 关闭所有视频
            Jzvd.releaseAllVideos();
            if (popupInfo.autoDismiss) dismiss();
        }
    }

    /**
     * 重写本方法, 增加一个默认参数, 直接调用本方法不会有任何改变
     */
    @Override
    public void dismiss() {
        // 其他人掉本方法都会关闭视频
        myDismiss(true);
    }

    /**
     * 重写dismiss(), 在不改变dismiss()方法的情况下, 给这个方法增加了一个参数
     * 自定义弹窗关闭, 决定是否要同时关闭视频播放器
     */
    public void myDismiss(boolean closeVideoPlayer) {
        if (closeVideoPlayer) {
            // 关闭所有视频
            Jzvd.releaseAllVideos();
        }
        super.dismiss();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 按下返回键
        if (event.getKeyCode() == KEYCODE_BACK) {
            // 关闭所有视频
            Jzvd.releaseAllVideos();
        }
        return super.onKeyDown(keyCode, event);
    }
}
