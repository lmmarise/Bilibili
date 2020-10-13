package org.tsb.bilibili.merge.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.CustomJzvd.MyJzvdStd;
import com.lxj.xpopup.interfaces.OnCancelListener;
import com.lxj.xpopup.interfaces.OnConfirmListener;

import org.tsb.bilibili.merge.R;
import org.tsb.bilibili.merge.constant.BILIBILI;
import org.tsb.bilibili.merge.utils.VideoUtil;
import org.tsb.bilibili.pojo.VideoInfo;
import org.tsb.bilibili.pojo.VideoItem;
import org.tsb.bilibili.util.FileSizeUtil;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.microshow.rxffmpeg.AudioVideoUtils;
import io.microshow.rxffmpeg.ProcessingComplete;
import io.microshow.rxffmpeg.RxFFmpegInvoke;

import static io.microshow.rxffmpeg.player.Helper.isFastClick;


/**
 * MainActivity RecyclerView的适配器
 */
public class BilibiliRecyclerViewAdapter extends RecyclerView.Adapter<BilibiliRecyclerViewAdapter.NormalTextViewHolder> {
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;
    private ArrayList<VideoItem> mVideoItems;
    private Handler mHandlerT = new Handler();

    /**
     * @param videoItems 传给适配器数据
     */
    public BilibiliRecyclerViewAdapter(Context context, ArrayList<VideoItem> videoItems) {
        mContext = context;
        mVideoItems = videoItems;
        mLayoutInflater = LayoutInflater.from(context);
    }

    /**
     * 添加数据
     */
    public void addData(VideoItem videoItem) {
        // 在list中添加数据，并通知条目加入一条
        mVideoItems.add(videoItem);
        //添加动画
        notifyItemInserted(mVideoItems.indexOf(videoItem));
    }

    /**
     * 修改适配器数据
     */
    public BilibiliRecyclerViewAdapter updateAdapterData(ArrayList<VideoItem> videoItems) {
        this.mVideoItems = videoItems;
        return this;
    }

    @NonNull
    @Override
    public NormalTextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.view_rv_item, parent, false);
        return new NormalTextViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NormalTextViewHolder holder, int position) {
        VideoItem videoItem = mVideoItems.get(position);
        // 将当前item的Java对象数据存起来
        holder.itemView.setTag(videoItem);
        // 缩略图图片
        holder.mImageView.setImageBitmap(videoItem.getIconTemp());
        // item标题
        holder.mTextView.setText(videoItem.getVideoName());
        // item是否选中
        holder.mCheckBox.setChecked(videoItem.isCheck());
        // 合并后文件存储位置
        File outSaveDir = new File(BILIBILI.VIDEO_MERGE_DIR);// 本app的视频输出目录
        // 如果视频转码输出目录不存在就新建
        if (!outSaveDir.exists()) outSaveDir.mkdirs();
        // 视频是否已合并了
        File mergedFile = new File(BILIBILI.VIDEO_MERGE_DIR + "/" + videoItem.getVideoName() + ".mp4");
        if (mergedFile.exists()) {
            // 垃圾文件,删除!!!
            if (mergedFile.isDirectory() || mergedFile.length() <= 0) {
                mergedFile.delete();
                videoItem.setMergedVideoPath(null);
            } else {
                // 已经正确合并了的文件
                videoItem.setMergedVideoPath(BILIBILI.VIDEO_MERGE_DIR + "/" + videoItem.getVideoName() + ".mp4");
            }
        }
        // 设置点击text的事件
        holder.mTextView.setOnClickListener(getItemClickListener(videoItem, holder));
        // 设置当前item下的cb变化事件
        holder.mCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mVideoItems.get(position).setCheck(isChecked);
            // Log.d("NormalTextViewHolder", "[" + position + "]:" + isChecked);
        });
    }

    /**
     * ffmpeg获取视频信息
     * RxFFmpegInvoke.getInstance().getMediaInfo(videoItem.getAudioPath()),
     */
    private View.OnClickListener getItemClickListener(VideoItem videoItem, NormalTextViewHolder holder) {
        return v -> {
            if (!isFastClick()) {
                // 计算视频信息
                if (videoItem.getVideoInfo() == null)
                    videoItem.setVideoInfo(calculateVideoInfo(videoItem));
                // 展示弹窗 存储弹窗句柄
                MyJzvdStd.confirmPopupView = new SoftReference<>(new XPopup.Builder(mContext).asConfirm(holder.mTextView.getText(),
                        videoItem.getVideoInfo().toString(),// Java获取视频详细信息
                        "取消", "转为MP4",
                        onConfirmListener(videoItem), onCancelListener(),
                        false, videoItem));
                // 弹出对话框
                MyJzvdStd.confirmPopupView.get().show();
                // 将播放视频文件对象给他传过去
                MyJzvdStd.confirmPopupView.get().setTag(videoItem);
            }
        };
    }

    /**
     * 点击按钮的取消
     *
     * @return
     */
    private OnCancelListener onCancelListener() {
        return null;
    }

    /**
     * 弹出框的`转为MP4`按钮点击事件,
     * 将audio和video进行合并逻辑
     */
    public OnConfirmListener onConfirmListener(VideoItem videoItem) {
        return () -> {
            // 视频是否已经合并
            String outputPath = BILIBILI.VIDEO_MERGE_DIR + "/" + videoItem.getVideoName() + ".mp4";
            // 如果视频已经合并
            if (new File(outputPath).exists()) {
                String msg = "视频已被合并到:" + BILIBILI.getVideoMergeRelativePath();
                Toast.makeText(mContext, msg,
                        Toast.LENGTH_LONG).show();
                return;
            }
            // 使用ffmpeg执行合并视频逻辑... 在视频处理回调中若是要使用异步toast则需要使用baseContext
            /*VideoUtil.mergeVideoAndAudio(
                    videoItem.getVideoPath(),
                    videoItem.getAudioPath(),
                    outputPath,
                    new NRV_FFmpegListener(((MainActivity) mContext).getBaseContext(), videoItem.getVideoName(), mHandlerT));*/
            Toast.makeText(mContext, "开始合并视频", Toast.LENGTH_SHORT).show();
            VideoUtil.mergeVideoAndAudio(
                    videoItem.getVideoPath(),
                    videoItem.getAudioPath(),
                    outputPath,
                    new ProcessingComplete() {
                        // 视频处理成功
                        @Override
                        public void done() {
                            /*使用广播异步toast暂时还有bug*/
                            /*Intent intent = new Intent(BILIBILI.FFMPEG_ACTION);
                            intent.putExtra("isDone", BILIBILI.FFMPEG_DONE);
                            // 发送无序广播,并设置一个权限
                            mContext.sendBroadcast(intent);*/
                            if (mHandlerT != null) {
                                mHandlerT.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String sb = "视频\"" + videoItem.getVideoName() +
                                                ".mp4\"已成功合并到目录" +
                                                BILIBILI.getVideoMergeRelativePath() + "下";
                                        Toast.makeText(
                                                ((Activity) mContext).getBaseContext(),
                                                sb,
                                                Toast.LENGTH_LONG
                                        ).show();
                                    }
                                });
                            }
                        }

                        // 视频处理失败
                        @Override
                        public void error() {
                            if (mHandlerT != null) {
                                mHandlerT.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(((Activity) mContext).getBaseContext(),
                                                "视频\"" + videoItem.getVideoName() + "\"合并出现错误",
                                                Toast.LENGTH_LONG).show();
                                        Toast.makeText(((Activity) mContext).getBaseContext(),
                                                "请检查B站缓存的视频是否已损坏",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
            // 给视频对象转码后地址
            videoItem.setMergedVideoPath(outputPath);
        };
    }



    /**
     * FFmpeg处理视频的回调
     */
    private static class NRV_FFmpegListener implements RxFFmpegInvoke.IFFmpegListener {
        private Context context;
        private String videoName;
        private Handler handler;

        public NRV_FFmpegListener(Context context, String videoName, Handler handler) {
            this.context = context;
            this.videoName = videoName;
            this.handler = handler;
        }

        @Override
        public void onFinish() {
            if (handler != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, videoName + "合并完成\n保存在" + BILIBILI.VIDEO_MERGE_DIR, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        @Override
        public void onProgress(int progress, long progressTime) {
            Toast.makeText(context, "视频处理中...", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            Toast.makeText(context, "视频处理取消", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(String message) {
            Toast.makeText(context, "视频处理出现错误,视频是否已存在?", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 根据传入对象的视频 filePath计算视频信息, 并存入传入对象
     *
     * @param videoItem 传入对象
     */
    private VideoInfo calculateVideoInfo(VideoItem videoItem) {
        // 源数据
        VideoInfo videoInfo = new VideoInfo();
        String videoPath = videoItem.getVideoPath();
        String audioPath = videoItem.getAudioPath();

        // ffmpeg获取信息
        int vWidth = AudioVideoUtils.getVideoWidth(videoPath);
        int vHeight = AudioVideoUtils.getVideoHeight(videoPath);
        int vDuration = AudioVideoUtils.getVideoDuration(videoPath);// 时长 s
        int vRotation = AudioVideoUtils.getVideoRotation(videoPath);// 旋转方向
        int[] videoAndAudioTrack = AudioVideoUtils.getVideoAndAudioTrack(videoPath, audioPath);// 音视频轨道
        boolean vHorizontal = AudioVideoUtils.isHorizontalVideo(videoPath);// 是否横屏
        String vSize = FileSizeUtil.getAutoFileOrFilesSize(videoPath);// 视频大小
        String aSize = FileSizeUtil.getAutoFileOrFilesSize(audioPath);// 音频大小
        // 1为B、2为KB、3为MB、4为GB
        long videoSize = (long) FileSizeUtil.getFileOrFilesSize(videoPath, BILIBILI.B);// KB单位的文件大小
        long vFitBitRate = AudioVideoUtils.getBitRate(vDuration, videoSize);// 获取视频码率


        // 存入信息对象
        videoInfo.setVWidth(vWidth)
                .setVHeight(vHeight)
                .setVBitRate(vFitBitRate)
                .setVDuration(vDuration)
                .setVRotation(vRotation)
                .setVIsHorizontal(vHorizontal)
                .setASize(aSize)
                .setVSize(vSize)
                .setVTrack(videoAndAudioTrack[0])
                .setATrack(videoAndAudioTrack[1]);

        return videoInfo;
    }

    @Override
    public int getItemCount() {
        return mVideoItems == null ? 0 : mVideoItems.size();
    }

    public static class NormalTextViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_tv)
        TextView mTextView;
        @BindView(R.id.item_cb)
        CheckBox mCheckBox;
        @BindView(R.id.item_image)
        ImageView mImageView;

        NormalTextViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            // 设置当前item的点击事件
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Log.d("NormalTextViewHolder", "--> " + getLayoutPosition());
                }
            });
        }
    }


}
