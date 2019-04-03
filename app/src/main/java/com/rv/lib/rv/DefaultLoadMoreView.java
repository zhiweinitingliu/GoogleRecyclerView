package com.rv.lib.rv;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rv.lib.R;

/**
 * @Description :默认的加载更多样式
 * @Author : wdk
 * @CretaTime : 2019/4/3 15:04
 * @LastModify(最终修改人) :wdk
 * @LastModifyTime(最终修改时间) : 2019/4/3 15:04
 * @LastCheckBy :wdk
 */
public class DefaultLoadMoreView extends LinearLayout implements GoogleRecyclerView.LoadMoreView, View.OnClickListener {

    private ProgressBar progress_bar;
    private TextView tv_message;

    private GoogleRecyclerView.LoadMoreListener loadMoreListener;

    public DefaultLoadMoreView(Context context) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
        setGravity(Gravity.CENTER);
        setVisibility(GONE);

        inflate(context, R.layout.layout_fotter_loadmore, null);
        progress_bar = findViewById(R.id.progress_bar);
        tv_message = findViewById(R.id.tv_message);

        setOnClickListener(this);
    }

    @Override
    public void onLoading() {
        setVisibility(VISIBLE);
        progress_bar.setVisibility(VISIBLE);
        tv_message.setVisibility(VISIBLE);
        tv_message.setText("loading...");
    }

    @Override
    public void onLoadFinish(boolean dataEmpty, boolean hasMore, boolean isCanFillScreen) {

        if (hasMore) {
            setVisibility(INVISIBLE);

        } else {
            //没有更多数据了

            if (isCanFillScreen) {
                //能够显示满一屏
                setVisibility(VISIBLE);
            } else {
                //不能显示满一屏
                setVisibility(INVISIBLE);
            }

            if (dataEmpty) {
                progress_bar.setVisibility(GONE);
                tv_message.setVisibility(VISIBLE);
                tv_message.setText("暂时没有数据");
            } else {
                progress_bar.setVisibility(GONE);
                tv_message.setVisibility(VISIBLE);
                tv_message.setText("没有更多数据啦");
            }
        }
    }

    @Override
    public void onWaitToLoadMore(GoogleRecyclerView.LoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
        setVisibility(VISIBLE);
        progress_bar.setVisibility(VISIBLE);
        tv_message.setVisibility(VISIBLE);
        tv_message.setText("点我加载更多");
    }

    @Override
    public void onLoadError(int errorCode, String errorMessage) {
        setVisibility(VISIBLE);
        progress_bar.setVisibility(GONE);
        tv_message.setVisibility(VISIBLE);
        tv_message.setText(errorMessage);
    }

    /**
     * foot 的点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (loadMoreListener != null) {
            loadMoreListener.onLoadMore();
        }
    }
}
