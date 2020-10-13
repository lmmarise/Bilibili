package io.microshow.rxffmpeg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 执行完成之后的回掉
 */
public interface ProcessingComplete {
    /**
     * 若是涉及到主线程更新UI请用广播, 这里发送广播
     */
    void done();
    void error();
}
