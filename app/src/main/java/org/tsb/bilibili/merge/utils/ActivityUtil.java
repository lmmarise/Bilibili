package org.tsb.bilibili.merge.utils;

import android.app.Activity;
import android.view.View;
import android.view.WindowManager;

/**
 * @author Arise
 * @date 2020/10/13 0:21
 * @Email lmmarise.j@gmail.com
 */
public class ActivityUtil {

    private static boolean isStatusBarVisible(Activity activity) {
        int uiOptions = activity.getWindow().getDecorView().getSystemUiVisibility();
        boolean isStatusBarHide = ((uiOptions | View.SYSTEM_UI_FLAG_FULLSCREEN) == uiOptions);
        return !isStatusBarHide;
    }

    /**
     * 隐藏状态栏, 显示黑空缺
     */
    public static void hideStatusBar(Activity activity) {
        if (isStatusBarVisible(activity)) {
            int uiOptions = activity.getWindow().getDecorView().getSystemUiVisibility();
            uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            activity.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        }
    }

    /**
     * 完全隐藏状态栏
     */
    public static void hideStatusBarTransparent(Activity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

}
