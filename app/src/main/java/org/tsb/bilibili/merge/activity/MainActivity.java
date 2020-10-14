package org.tsb.bilibili.merge.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.jetbrains.annotations.NotNull;
import org.tsb.bilibili.merge.R;
import org.tsb.bilibili.merge.constant.ENV;
import org.tsb.bilibili.merge.fragment.BilibiliFragment;
import org.tsb.bilibili.merge.fragment.BlankFragment;
import org.tsb.bilibili.pojo.AppVersion;

import java.util.ArrayList;
import java.util.List;

import per.goweii.actionbarex.ActionBarEx;

/**
 * @author Arise
 * @date 2020/10/12 22:31
 * @Email lmmarise.j@gmail.com
 */
public class MainActivity extends BaseActivity {

    private ViewPager mViewPager;
    private RadioGroup mTabRadioGroup;
    private List<Fragment> mFragments;
    private FragmentPagerAdapter mAdapter;
    // 联网获取的当前app版本信息
    public static AppVersion appVersion = null;


    /*========================================activity生命周期========================================*/
    @Override
    public void initParams(Bundle params) {
    }

    @Override
    public int bindLayout() {
        return R.layout.activity_main;
    }

    /**
     * 初始化ActionBar
     */
    private void intiActionBar(View view) {
        ActionBarEx actionBarEx = view.findViewById(R.id.action_bar_ex);
        actionBarEx.getView(R.id.iv_back).setOnClickListener(v -> finish());
    }


    @Override
    public void initView(View view) {
        // 状态栏
        intiActionBar(view);
        // 底部导航栏
        mViewPager = findViewById(R.id.fragment_vp);
        mTabRadioGroup = findViewById(R.id.tabs_rg);
        // 创建帧布局实例
        mFragments = new ArrayList<>(4);
        mFragments.add(BilibiliFragment.newInstance());
        mFragments.add(BlankFragment.newInstance("音乐"));
        mFragments.add(BlankFragment.newInstance("唇脂"));
        mFragments.add(BlankFragment.newInstance("我的"));
        // 初始化适配器
        mAdapter = new MainFragmentPagerAdapter(getSupportFragmentManager(), mFragments);
        mViewPager.setAdapter(mAdapter);
        // 给导航栏和帧布局容器注册监听器
        mViewPager.addOnPageChangeListener(mPageChangeListener);
        mTabRadioGroup.setOnCheckedChangeListener(mOnCheckedChangeListener);
        // 设置viewPager的缓存个数
        mViewPager.setOffscreenPageLimit(mFragments.size());
    }

    @Override
    public void doBusiness(Context context) {
        // 检查app更新
        appUpdate(context);
        // 如果应用是第一次启动, 提示用户是最新版本
        appFirstStart(context);
        // 是否更新成功
        appUpdateSuccess(context);
    }

    @Override
    public void widgetClick(View v) {
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    /*========================================业务逻辑========================================*/

    /**
     * 用户对软件进行了更新操作, 提示用户更新成功了
     */
    private void appUpdateSuccess(Context context) {
        if (appVersion == null) return;
        SharedPreferences preferences = context.getSharedPreferences(ENV.SP_main_activity, Context.MODE_PRIVATE);
        // 获取到了不为0(说明是更新过的用户), 没有获取到为0
        boolean haveNewVersion = preferences.getBoolean(ENV.SP_have_new_version, false);
        int newVersionCode = appVersion.getNewVersion();
        // 用户根据更新提示, 更新到了最新版本
        if (haveNewVersion && newVersionCode == ENV.VERSION_CODE) {
            // 提示用户更新成功了
            new AlertDialog.Builder(context).setTitle("更新成功啦!")
                    .setIcon(R.drawable.ic_congratulate)
                    .setMessage(appVersion.getUpdateDescription())
                    .setPositiveButton("好的", (dialog, which) -> dialog.dismiss())
                    .show();
            // 下次不再展示
            preferences.edit().putBoolean(ENV.SP_have_new_version, false).apply();
        }
    }

    /**
     * 应用第一次启动
     */
    void appFirstStart(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(ENV.SP_main_activity, Context.MODE_PRIVATE);
        if (preferences.getBoolean(ENV.SP_is_first, true)) {
            // 向用户展示本次app的更新信息
            new AlertDialog.Builder(context).setTitle("第一次启动")
                    .setIcon(R.drawable.ic_yes)
                    .setMessage(appVersion.getUpdateDescription())
                    .setPositiveButton("了解了", (dialog, which) -> dialog.dismiss())
                    .show();
            // 切换状态
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(ENV.SP_is_first, false).apply();
        }
    }

    /**
     * 软件更新
     */
    private void appUpdate(Context context) {
        // 数据持久化
        SharedPreferences preferences = context.getSharedPreferences(ENV.SP_main_activity, Context.MODE_PRIVATE);
        if (appVersion != null && appVersion.getNewVersion() > ENV.VERSION_CODE) {
            // 有更新
            preferences.edit().putBoolean(ENV.SP_have_new_version, true).apply();
            preferences.edit().putInt(ENV.SP_new_version_code, appVersion.getNewVersion()).apply();
            // 对话框
            new AlertDialog.Builder(context)
                    .setIcon(R.drawable.ic_new)
                    .setTitle("有更新!")
                    .setMessage("新版本:" + appVersion.getVersionName() + "\n当前版本:" + ENV.VERSION_NAME + "\n" + appVersion.getUpdateDescription())
                    .setNegativeButton("取消", (dialog, which) -> {
                        // 判断本次更新是否为强制更新 -> 强制更新将结束应用
                        if (appVersion.isForceUpdate()) {
                            // 3种退出应用的方法:
                            // ((Activity) this).finish();
                            // android.os.Process.killProcess(android.os.Process.myPid());    // 获取PID
                            System.exit(0);   // 常规java、c#的标准退出法，返回值为0代表正常退出
                        } else {
                            dialog.dismiss();
                        }
                    })
                    // String confirm = appVersion.isHavePassword() ? "(" + appVersion.getApkUrlPassword() + ")" : "";
                    .setPositiveButton("前往下载更新", (dialog, which) -> {
                        openInBrowser(appVersion.getApkUrl());
                    })
                    .setCancelable(false)   // 禁止触摸边缘+返回键
                    .show();
        }
        // else {
        //     preferences.edit().putBoolean(ENV.SP_have_new_version, false).apply();
        // }
    }


    /*========================================私有方法========================================*/

    /**
     * 内容页
     */
    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            RadioButton radioButton = (RadioButton) mTabRadioGroup.getChildAt(position);
            radioButton.setChecked(true);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    /**
     * 底部导航栏
     */
    private RadioGroup.OnCheckedChangeListener mOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            for (int i = 0; i < group.getChildCount(); i++) {
                if (group.getChildAt(i).getId() == checkedId) {
                    mViewPager.setCurrentItem(i);
                    return;
                }
            }
        }
    };

    /**
     * 从浏览器打开网页
     */
    private void openInBrowser(String url) {
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        intent.setAction(Intent.ACTION_VIEW);
        this.startActivity(intent);
    }

    /*========================================内部类========================================*/

    /**
     * 内容页适配器
     */
    private class MainFragmentPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> mList;

        public MainFragmentPagerAdapter(FragmentManager fm, List<Fragment> list) {
            super(fm);
            this.mList = list;
        }

        @NotNull
        @Override
        public Fragment getItem(int position) {
            return this.mList == null ? null : this.mList.get(position);
        }

        @Override
        public int getCount() {
            return this.mList == null ? 0 : this.mList.size();
        }
    }
}
