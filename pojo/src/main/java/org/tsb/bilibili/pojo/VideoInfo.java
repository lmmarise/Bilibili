package org.tsb.bilibili.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class VideoInfo {
    // 视频轨道 int
    private int vTrack;
    // 音频轨道 int
    private int aTrack;
    // 时长 s
    private int vDuration;
    // 视频宽 int
    private int vWidth;
    // 视频高 int
    private int vHeight;
    // 视频旋转方向 int
    private int vRotation;
    // 是横屏的视频 true -> 横屏
    private boolean vIsHorizontal;
    // 视频大小
    private String vSize;
    // 音频大小
    private String aSize;
    // 视频码率 单位为B
    private long vBitRate;

    @Override
    public String toString() {
        return "视频轨道===========>" + vTrack + "\n" +
                "音频轨道==========>" + aTrack + "\n" +
                "时长==============>" + TimeUtil.formatSecond(vDuration) + "\n" +
                "分辨率============>" + vWidth + "x" + vHeight + "\n" +
                "视频方向==========>" + (vIsHorizontal ? "横向" : "竖向") + "\n" +
                "视频大小==========>" + vSize + "\n" +
                "音频大小==========>" + aSize + "\n" +
                "视频码率==========>" + (vBitRate / 1000) + "." + (vBitRate / 10 % 100) + "Kbps";
    }
}
