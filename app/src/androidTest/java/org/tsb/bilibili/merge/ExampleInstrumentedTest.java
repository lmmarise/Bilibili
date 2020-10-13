package org.tsb.bilibili.merge;

import android.os.Environment;

import androidx.core.app.ActivityCompat;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void testBing() throws Exception{
        // 获取图片信息json
        File file = Environment.getDataDirectory();
        String absolutePath = file.getAbsolutePath();
        // /storage/emulated/0
        System.out.println(absolutePath);
        // Environment.getPackageManager().getApplicationInfo("com.uc.addon.qrcodegenerator", 0).sourceDir();
    }
}