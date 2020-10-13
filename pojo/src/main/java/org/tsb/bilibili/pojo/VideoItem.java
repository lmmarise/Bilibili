package org.tsb.bilibili.pojo;

import android.graphics.Bitmap;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Getter
@Setter
public class VideoItem {
    // 视频的主要dir, 绝对路径
    private String dirPath;
    private String videoName;
    private String videoPath;
    private String audioPath;
    private String bulletPath;
    private Bitmap iconTemp;
    private boolean isCheck;
    // 视频信息, item控件的text控件被点击时调用ffmpeg进行计算
    private VideoInfo videoInfo;
    private String mergedVideoPath = null;

    public VideoItem() {
    }

    public VideoItem(String dirPath,
                     String videoName,
                     String videoPath,
                     String audioPath,
                     String bulletPath,
                     Bitmap iconTemp) {
        this.dirPath = dirPath;
        this.videoName = videoName;
        this.videoPath = videoPath;
        this.audioPath = audioPath;
        this.bulletPath = bulletPath;
        this.iconTemp = iconTemp;
    }

    @Override
    public String toString() {
        return "\nVideoItem{\n" +
                "\tdirPath = '" + dirPath + '\'' + ",\n" +
                "\tvideoName = '" + videoName + '\'' + ",\n" +
                "\tvideoPath = '" + videoPath + '\'' + ",\n" +
                "\taudioPath = '" + audioPath + '\'' + ",\n" +
                "\tbulletPath = '" + bulletPath + '\'' + ",\n" +
                "\ticonTemp = " + iconTemp + ",\n" +
                "\tisCheck = " + isCheck + "\n" +
                '}';
    }
}
