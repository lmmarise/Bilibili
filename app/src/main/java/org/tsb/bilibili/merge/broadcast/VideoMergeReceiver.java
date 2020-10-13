package org.tsb.bilibili.merge.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import org.tsb.bilibili.merge.constant.BILIBILI;

/**
 * 接收视频处理完成用的广播接收器
 */
public class VideoMergeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getIntExtra("isDone", 0)) {
            // 视频合并成功与否的状态判断
            case BILIBILI.FFMPEG_DONE:
                Toast.makeText(context, "视频已被合并到目录" + BILIBILI.VIDEO_MERGE_DIR + "下",
                        Toast.LENGTH_LONG).show();
                break;
            case BILIBILI.FFMPEG_ERROR:
                Toast.makeText(context, "视频已被合出现了问题", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }
}
