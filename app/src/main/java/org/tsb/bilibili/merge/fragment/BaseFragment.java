package org.tsb.bilibili.merge.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import org.tsb.bilibili.merge.constant.ENV;
import org.tsb.bilibili.merge.utils.BackHandlerHelper;

/**
 * @author Arise
 * @date 2020/10/12 22:26
 * @Email lmmarise.j@gmail.com
 */
public abstract class BaseFragment extends Fragment implements View.OnClickListener, FragmentBackHandler {
    private boolean isDebug;
    private String APP_NAME;
    protected final String TAG = this.getClass().getSimpleName();
    private View mContextView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isDebug = ENV.isDebug;
        APP_NAME = ENV.APP_NAME;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContextView = inflater.inflate(bindLayout(), container, false);
        initView(mContextView);
        doBusiness(getActivity());
        return mContextView;
    }

    /**
     * [绑定布局]
     */
    public abstract int bindLayout();

    /**
     * [初始化控件]
     */
    public abstract void initView(final View view);

    /**
     * [业务操作]
     */
    public abstract void doBusiness(Context mContext);

    /**
     * View点击
     **/
    public abstract void widgetClick(View v);

    /**
     * 没有处理back键需求的Fragment不用重写
     */
    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onClick(View v) {
        if (fastClick())
            widgetClick(v);
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T $(View view, int resId) {
        return (T) view.findViewById(resId);
    }

    /**
     * [日志输出]
     */
    protected void $Log(String msg) {
        if (isDebug) {
            Log.d(APP_NAME, msg);
        }
    }

    /**
     * [防止快速点击]
     */
    private boolean fastClick() {
        long lastClick = 0;
        if (System.currentTimeMillis() - lastClick <= 1000) {
            return false;
        }
        lastClick = System.currentTimeMillis();
        return true;
    }
}