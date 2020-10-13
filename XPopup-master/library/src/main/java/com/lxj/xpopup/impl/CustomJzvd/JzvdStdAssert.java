package com.lxj.xpopup.impl.CustomJzvd;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import cn.jzvd.JzvdStd;

public class JzvdStdAssert extends JzvdStd {
    Context mContext;

    public JzvdStdAssert(Context context) {
        super(context);
        mContext = context;
    }

    public JzvdStdAssert(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public void onPrepared() {
        state = STATE_PREPARED;
        if (!preloading) {
            mediaInterface.start();
            preloading = false;
        }
        onStatePlaying();
    }

    @Override
    public void gotoFullscreen() {
        super.gotoFullscreen();
    }


}
