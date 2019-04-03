package com.rv.lib.rv;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * @Description : recyclerview adapter 的包装adapter
 * @Author : wdk
 * @CretaTime : 2019/4/2 17:15
 * @LastModify(最终修改人) :wdk
 * @LastModifyTime(最终修改时间) : 2019/4/2 17:15
 * @LastCheckBy :wdk
 */
public class AdapterWrapper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater mInflater;
    private RecyclerView.Adapter mAdapter;

    AdapterWrapper(Context context, RecyclerView.Adapter adapter) {
        this.mInflater = LayoutInflater.from(context);
        this.mAdapter = adapter;
    }

    /**
     * 获取到项目中传给recyclerview的adapter
     *
     * @return adapter
     */
    public RecyclerView.Adapter getOriginAdapter() {
        return mAdapter;
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }

    /**
     * 触发事件
     */
    private GoogleRecyclerView.ItemClickListener itemClickListener;

    public void setOnItemClickListener(GoogleRecyclerView.ItemClickListener itemClickListener) {
        this.itemClickListener=itemClickListener;
    }
}
