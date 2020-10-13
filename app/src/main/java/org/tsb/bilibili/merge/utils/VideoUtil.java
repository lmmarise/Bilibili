package org.tsb.bilibili.merge.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import io.microshow.rxffmpeg.ProcessingComplete;
import io.microshow.rxffmpeg.RxFFmpegCommandList;
import io.microshow.rxffmpeg.RxFFmpegInvoke;

/**
 * ffmpeg命令执行工具
 */
public class VideoUtil {
    private static final String TAG = "VideoUtil";

    /**
     * 视频模糊算法进行过滤
     *
     * @param scale 5:1
     */
    public static String[] getBoxBlur(String inVideoPath, String outVideoPath, String scale) {
        RxFFmpegCommandList cmdlist = new RxFFmpegCommandList();
        cmdlist.append("-i");
        cmdlist.append(inVideoPath);
        cmdlist.append("-vf");
        cmdlist.append("boxblur=" + (scale == null ? "5:1" : scale));
        cmdlist.append("-preset");
        cmdlist.append("superfast");
        cmdlist.append(outVideoPath);
        return cmdlist.build();
    }

    /**
     * 截取视频缩略图
     */
    public static Bitmap captureVideoThumbnail(String inVideoFile, String outImageFile) {
        String[] ffmpegCmd = {"ffmpeg", "-y", "-i", inVideoFile, "-f", "image2", "-ss", "00:00:03",
                "-vframes", "1", "-preset", "superfast", "-s", "780x450", outImageFile};
        // String cmdStr = "ffmpeg -i " + inVideoFile + " -y -f image2 -ss 00:00:03 -t 0.001 -s 352x240" + outImageFile;
        // 执行ffmpeg命令 todo 改异步
        RxFFmpegInvoke.getInstance().runCommand(ffmpegCmd, null);
        // 图片转Bitmap
        try {
            return BitmapFactory.decodeStream(new FileInputStream(new File(outImageFile)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * ffmpeg获取指定视频码率  不知道为什么RxFFmpegInvoke封装的ffmpeg native接口获取不了
     */
    public String getBitrate(String videoPath) {
//        System.out.println(videoPath+"-------------->");
//        String[] ffmpegCmd = {"ffmpeg","-i", videoPath};
//        Log.d(TAG, "getBitrate --> videoPath: " + videoPath);
//        String streamVideoInfo = getStreamVideoInfo(videoPath);
//        String mediaInfo = RxFFmpegInvoke.getInstance().getMediaInfo(videoPath);
//        Log.d(TAG, "getBitrate --> mediaInfo: " + mediaInfo);
        return "0";
    }

    /**
     * 直接将音频合并到源文件上
     * @param mffmpegListener IFFmpegListener的回调
     */
    public static void audioAttache2Video(String audioPath, String videoPath,
                                          @Nullable RxFFmpegInvoke.IFFmpegListener mffmpegListener) {
        String[] ffmpegCmd = {"ffmpeg", "-i", audioPath, "-vcodec", "cpoy", "-an", videoPath};
        // 执行ffmpeg命令
        RxFFmpegInvoke.getInstance().runCommand(ffmpegCmd, mffmpegListener);
    }

    /**
     * 将音视频合并后输出到另一个文件
     *
     * @param inVideoFile
     * @param inAudioFile
     * @param outFile
     * @param mffmpegListener ffmpeg处理事件监听
     */
    @Deprecated // 请用ProcessingComplete参数那个
    public static void mergeVideoAndAudio(String inVideoFile, String inAudioFile, String outFile,
                                  RxFFmpegInvoke.IFFmpegListener mffmpegListener) {
        StringBuilder sb = new StringBuilder();
        /*sb.append("ffmpeg -y -i ");
        sb.append(inVideoFile);
        sb.append(" -i ");
        sb.append(inAudioFile);
        sb.append(" -filter_complex [0:a]aformat=sample_fmts=fltp:sample_rates=44100:" +
                "channel_layouts=stereo,volume=0.2[a0];[1:a]aformat=sample_fmts=fltp:" +
                "sample_rates=44100:channel_layouts=stereo,volume=1[a1];[a0][a1]amix=inputs=2:" +
                "duration=first[aout] -map [aout] -ac 2 -c:v copy -map 0:v:0 -preset superfast ");
        sb.append(outFile);*/
        String[] ffmpegCmd = {"ffmpeg", "-i", inVideoFile, "-i",
                inAudioFile, "-vcodec", "copy", "-acodec", "copy", outFile};
        RxFFmpegInvoke.getInstance().runCommandAsync(ffmpegCmd, mffmpegListener);
    }

    /**
     * 更新UI请实现ProcessingComplete使用广播
     */
    public static void mergeVideoAndAudio(String inVideoFile, String inAudioFile, String outFile,
                                          ProcessingComplete processingComplete) {
        StringBuilder sb = new StringBuilder();
        /*sb.append("ffmpeg -y -i ");
        sb.append(inVideoFile);
        sb.append(" -i ");
        sb.append(inAudioFile);
        sb.append(" -filter_complex [0:a]aformat=sample_fmts=fltp:sample_rates=44100:" +
                "channel_layouts=stereo,volume=0.2[a0];[1:a]aformat=sample_fmts=fltp:" +
                "sample_rates=44100:channel_layouts=stereo,volume=1[a1];[a0][a1]amix=inputs=2:" +
                "duration=first[aout] -map [aout] -ac 2 -c:v copy -map 0:v:0 -preset superfast ");
        sb.append(outFile);*/
        String[] ffmpegCmd = {"ffmpeg", "-i", inVideoFile, "-i",
                inAudioFile, "-vcodec", "copy", "-acodec", "copy", outFile};
        RxFFmpegInvoke.getInstance().runCommandAsync(ffmpegCmd, processingComplete);
    }

    /*
     * 开启/关闭 debug 模式，建议在 Application 初始化调用
     * */
    public static void startDebug(boolean debug) {
        RxFFmpegInvoke.getInstance().setDebug(debug);
    }

    /*
     * FFmpeg 命令执行 (同步方式)
     * */
    public static void executeSync(String[] command) {
        RxFFmpegInvoke.getInstance().runCommand(command, null);
    }

    /*
     * 中断 FFmpeg 命令
     * */
    public static void executeBreak() {
        RxFFmpegInvoke.getInstance().exit();
    }

    /*
     * 获取媒体文件信息
     * */
    public String getStreamVideoInfo(String filePath) {
        return RxFFmpegInvoke.getInstance().getMediaInfo(filePath);
    }
}
