package org.tsb.bilibili.merge.constant;

import android.os.Environment;

public interface ENV {
    // 手机外部存储路径
    String SD_Path = Environment.getExternalStorageDirectory().getAbsolutePath();
    int START_ACTIVITY_DELAY = 2600; // 启动页延迟时长
    String PACKAGE_NAME = "org.tsb.bilibili.merge";
    String VERSION_INFO_URL = "http://tbeau.oicp.io/bilibili/merge/app/versions";
    int VERSION_CODE = 10;           // 当前应用的版本号
    String VERSION_NAME = "0.4.6";  // 当前应用的版本名

    String SP_main_activity = "main_activity";
    String SP_is_first =  "is_first";   // SharedPreferences的key判断app是否是第一次启动
    String SP_new_version_code =  "new_version_code";   // 新版本号
    String SP_have_new_version = "have_new_version";    // 有新版本
    boolean isDebug = false;
    String APP_NAME = "Bilibili缓存合并";
}
