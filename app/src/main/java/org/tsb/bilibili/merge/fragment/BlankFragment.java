package org.tsb.bilibili.merge.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.tsb.bilibili.merge.R;

/**
 * @author Arise
 * @date 2020/10/12 22:41
 * @Email lmmarise.j@gmail.com
 */
public class BlankFragment extends BaseFragment {

    private static final String ARG_SHOW_TEXT = "text";

    private String mContentText;


    public BlankFragment() {
    }

    public static BlankFragment newInstance(String param1) {
        BlankFragment fragment = new BlankFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SHOW_TEXT, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int bindLayout() {
        return R.layout.fragment_blank;
    }

    @Override
    public void initView(View view) {
        if (getArguments() != null) {
            mContentText = getArguments().getString(ARG_SHOW_TEXT);
        }
        TextView contentTv = view.findViewById(R.id.content_tv);
        contentTv.setText(mContentText);
    }

    @Override
    public void doBusiness(Context mContext) {

    }

    @Override
    public void widgetClick(View v) {

    }

}
