package com.rv.lib.rv;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description :自定义RecyclerView
 * @Author : wdk
 * @CretaTime : 2019/4/2 16:30
 * @LastModify(最终修改人) :wdk
 * @LastModifyTime(最终修改时间) : 2019/4/2 16:30
 * @LastCheckBy :wdk
 */
public class GoogleRecyclerView extends RecyclerView {

    /**
     * 因为流行的recyclerview 功能都十分强大，但是强大的背后都隐藏的很多适配问题，所以我想简化他们的功能
     * 1、让我们需要的基础功能不会有那么多的附加的无用的功能
     * 2、将功能写成插件，需要的时候添加进去，满足我们的需求
     * <p>
     * 我参考的recyclerview 是严振杰封装的，我觉得他封装的入侵性小，能够满足我的需求
     */


    private AdapterWrapper mAdapterWrapper;


    public GoogleRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public GoogleRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GoogleRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        initViewCanvasListener();
    }


    boolean isViewChanged = false;

    /**
     * 添加view绘制的监听
     */
    private void initViewCanvasListener() {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                isViewChanged = true;
                Log.e("GoogleRecycler:", "布局变化：  " + isViewChanged);
            }
        });
    }

    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        //如果adapter 已经设置过，就取消注册它的观察者
        if (mAdapterWrapper != null) {
            mAdapterWrapper.getOriginAdapter().unregisterAdapterDataObserver(adapterDataObserver);
        }

        if (adapter == null) {
            mAdapterWrapper = null;
        } else {
            adapter.registerAdapterDataObserver(adapterDataObserver);
            //View中提供了getContext()方法
            mAdapterWrapper = new AdapterWrapper(getContext(), adapter);
            //先把事件的引用设置进去，当调用recyclerview的setOnItemClickListener时adapter中的itemClickListener就赋值了
            mAdapterWrapper.setOnItemClickListener(itemClickListener);
        }
        super.setAdapter(mAdapterWrapper);
    }

    /**
     * recyclerview 提供的数据变化的观察者
     */
    private AdapterDataObserver adapterDataObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            mAdapterWrapper.notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mAdapterWrapper.notifyItemChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            mAdapterWrapper.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mAdapterWrapper.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mAdapterWrapper.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mAdapterWrapper.notifyItemMoved(fromPosition, toPosition);
        }
    };

    /**
     * 条目点击事件
     *
     * @param onItemClickListener 监听
     */
    private ItemClickListener itemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        if (onItemClickListener == null) return;
        checkAdapterExit("adapter is null");
        itemClickListener = new ItemClickListener(this, onItemClickListener);
    }

    public static class ItemClickListener implements OnItemClickListener {

        //用于获取recyclerview中的变量数据
        private GoogleRecyclerView googleRecyclerView;
        //点击事件的监听
        private OnItemClickListener onItemClickListener;

        public ItemClickListener(GoogleRecyclerView googleRecyclerView, OnItemClickListener onItemClickListener) {
            this.googleRecyclerView = googleRecyclerView;
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public void onItemClick(View itemView, int position) {
            //如果添加头布局的时候需要减position
            position -= googleRecyclerView.getHeaderCount();
            if (position >= 0) onItemClickListener.onItemClick(itemView, position);
        }
    }

    /**
     * 检查adapter是否为null
     *
     * @param msg 异常信息
     */
    private void checkAdapterExit(String msg) {
        if (mAdapterWrapper == null) throw new IllegalStateException(msg);
    }


    /**
     * 加载更多
     */

    private int mScrollState = -1;
    private boolean isLoadMore = false;
    private boolean isAutoLoadMore = true;
    private boolean isLoadError = false;

    private boolean mDataEmpty = true;
    private boolean mHasMore = false;

    private LoadMoreView mLoadMoreView;

    public void setAutoLoadMore(boolean autoLoadMore) {
        isAutoLoadMore = autoLoadMore;
    }

    /**
     * Use the default to load more View.
     */
    public void useDefaultLoadMore() {
        DefaultLoadMoreView defaultLoadMoreView = new DefaultLoadMoreView(getContext());
        addFooterView(defaultLoadMoreView);
        setLoadMoreView(defaultLoadMoreView);
    }

    /**
     * Load more view.
     */
    public void setLoadMoreView(LoadMoreView view) {
        mLoadMoreView = view;
    }

    @Override
    public void onScrollStateChanged(int state) {
        this.mScrollState = state;
    }

    @Override
    public void onScrolled(int dx, int dy) {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager llManager = (LinearLayoutManager) layoutManager;

            int itemCount = llManager.getItemCount();
            if (itemCount <= 0) return;

            int lastItemPosition = llManager.findLastVisibleItemPosition();
            if (lastItemPosition + 1 == llManager.getItemCount() && (mScrollState == SCROLL_STATE_DRAGGING || mScrollState == SCROLL_STATE_SETTLING)) {
                //加载更多
                dispatchLoadMore();
            }


        } else if (layoutManager instanceof StaggeredGridLayoutManager) {


        }
    }

    /**
     * 执行加载更多
     */
    private void dispatchLoadMore() {
        if (mLoadMoreView != null) mLoadMoreView.show();

        if (!isAutoLoadMore) {
            if (mLoadMoreView != null) mLoadMoreView.onWaitToLoadMore(loadMoreListener);

        } else {
            if (isLoadMore || mDataEmpty || !mHasMore) return;

            //用于加载是的判断，正在加载中就不能再加载
            isLoadMore = true;

            if (mLoadMoreView != null) mLoadMoreView.onLoading();

            if (loadMoreListener != null) loadMoreListener.onLoadMore();
        }

    }

    /**
     * 加载更多完成
     *
     * @param dataEmpty 列表是否为空
     * @param hasMore   是否有加载更多
     */
    public void loadMoreFinish(final boolean dataEmpty, final boolean hasMore, LoadState loadState) {
        isLoadError = false;
        isLoadMore = false;

        //用于加载是的判断，空数据或没有更多数据就不再加载
        mDataEmpty = dataEmpty;
        mHasMore = hasMore;

//        post(new Runnable() {
//            @Override
//            public void run() {
//
//                boolean isCanFillScreen = isCanFillScreen();
//                if (mLoadMoreView != null) {
                    mLoadMoreView.onLoadFinish(dataEmpty, hasMore, loadState);
//                }
//            }
//        });
    }

    /**
     * 数据是否够显示满屏幕
     *
     * @return
     */
    public boolean isCanFillScreen() {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;

            int itemCount = layoutManager.getItemCount();
            if (itemCount <= 0) return false;

            int lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();
            if (itemCount == lastVisiblePosition + 1) return false;

            return true;

        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;

            int itemCount = layoutManager.getItemCount();
            if (itemCount <= 0) return false;

            int[] lastVisiblePositionArray = staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(null);
            int lastVisiblePosition = lastVisiblePositionArray[lastVisiblePositionArray.length - 1];

            if (itemCount == lastVisiblePosition + 1) return false;

            return true;
        }
        return false;
    }

    enum LoadState {
        firstLoadState, refreshLoadState, LoadMoreState;
    }

    public interface LoadMoreView {

        void show();

        /**
         * Show progress.
         */
        void onLoading();

        /**
         * Load finish, handle result.
         */
        void onLoadFinish(boolean dataEmpty, boolean hasMore, LoadState loadState);

        /**
         * Non-auto-loading mode, you can to click on the item to load.
         */
        void onWaitToLoadMore(LoadMoreListener loadMoreListener);

        /**
         * Load error.
         */
        void onLoadError(int errorCode, String errorMessage);
    }

    private LoadMoreListener loadMoreListener;

    public void setLoadMoreListener(LoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    public interface LoadMoreListener {

        /**
         * More data should be requested.
         */
        void onLoadMore();
    }


    private List<View> mHeaderViewList = new ArrayList<>();
    private List<View> mFooterViewList = new ArrayList<>();

    /**
     * Add view at the headers.
     */
    public void addHeaderView(View view) {
        mHeaderViewList.add(view);
        if (mAdapterWrapper != null) {
            mAdapterWrapper.addHeaderViewAndNotify(view);
        }
    }

    /**
     * Remove view from header.
     */
    public void removeHeaderView(View view) {
        mHeaderViewList.remove(view);
        if (mAdapterWrapper != null) {
            mAdapterWrapper.removeHeaderViewAndNotify(view);
        }
    }

    /**
     * Add view at the footer.
     */
    public void addFooterView(View view) {
        mFooterViewList.add(view);
        if (mAdapterWrapper != null) {
            mAdapterWrapper.addFooterViewAndNotify(view);
        }
    }

    public void removeFooterView(View view) {
        mFooterViewList.remove(view);
        if (mAdapterWrapper != null) {
            mAdapterWrapper.removeFooterViewAndNotify(view);
        }
    }

    /**
     * Get size of headers.
     */
    public int getHeaderCount() {
        if (mAdapterWrapper == null) return 0;
        return mAdapterWrapper.getHeaderCount();
    }

    /**
     * Get size of footer.
     */
    public int getFooterCount() {
        if (mAdapterWrapper == null) return 0;
        return mAdapterWrapper.getFooterCount();
    }

}
