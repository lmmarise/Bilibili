package org.tsb.bilibili.merge.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.tsb.bilibili.merge.R;

import butterknife.BindView;

public class SimpleViewHolder extends BaseViewHolder<String> {
    // 一个普通的可点击的TextView
    @BindView(R.id.item_tv)
    TextView mIsTvContent;

    public SimpleViewHolder(Context context, ViewGroup root) {
        // 修改了构造方法，在这里显式指定Layout ID
        super(context, root, R.layout.view_rv_item);
    }

    @Override
    protected void bindData(final String itemValue, final int position, final OnItemClickListener listener) {
        // 在这里完成控件的初始化，将其与数据绑定
        if (itemValue != null) {
            mIsTvContent.setText(itemValue);
        }
        // 如果需要有点击事件，就通过listener把它传递出去
        mIsTvContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 如果外界没有调用BaseAdapter.setOnClickListener()，
                // listener就为null
                if (listener == null) {
                    return;
                }
                // listener不为null就将这个事件传递给外界处理
                listener.onItemClick(itemValue, v.getId(), position);
            }
        });
    }
}