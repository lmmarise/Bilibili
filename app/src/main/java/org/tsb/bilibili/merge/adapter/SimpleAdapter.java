package org.tsb.bilibili.merge.adapter;

import android.content.Context;
import android.view.ViewGroup;

public class SimpleAdapter extends BaseAdapter<String> {
    @Override
    protected BaseViewHolder createViewHolder(Context context, ViewGroup parent) {
        //SampleViewHolder继承自BaseViewHolder
        return new SimpleViewHolder(context, parent);
    }
}