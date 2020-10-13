package org.tsb.bilibili.merge.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class BaseAdapter<V> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    /**
     * 装载了每个Item的Value的列表
     */
    private List<V> mValueList;

    /**
     * 我写的一个接口，通过回调分发点击事件
     */
    private OnItemClickListener<V> mOnItemClickListener;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return createViewHolder(parent.getContext(), parent);
    }

    @Override
    @SuppressWarnings("unchecked")//一定会是BaseViewHolder的子类，因为createViewHolder()的返回值
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //BaseViewHolder是我抽象出来的RecyclerView.ViewHolder的基类，下面会有详细讲解
        ((BaseViewHolder) holder).setData(mValueList.get(position), position, mOnItemClickListener);
    }

    /**
     * 设置每个Item的点击事件
     */
    public void setOnClickListener(OnItemClickListener<V> listener) {
        this.mOnItemClickListener = listener;
    }

    /**
     * 刷新数据
     *
     * @param valueList 新的数据列表
     */
    public void refreshData(List<V> valueList) {
        this.mValueList = valueList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mValueList == null ? 0 : mValueList.size();
    }

    /**
     * 生成ViewHolder
     */
    protected abstract BaseViewHolder createViewHolder(Context context, ViewGroup parent);
}
