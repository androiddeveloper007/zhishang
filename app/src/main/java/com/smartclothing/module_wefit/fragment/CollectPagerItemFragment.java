package com.smartclothing.module_wefit.fragment;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartclothing.module_wefit.R;
import com.smartclothing.module_wefit.adapter.CollectRvAdapter;
import com.smartclothing.module_wefit.bean.Collect;
import com.smartclothing.module_wefit.widget.dialog.DepositDialog;
import com.vondear.rxtools.fragment.FragmentLazy;
import com.vondear.rxtools.utils.RxLogUtils;
import com.vondear.rxtools.view.RxToast;
import com.yanzhenjie.recyclerview.swipe.SwipeItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import lab.dxythch.com.netlib.net.RetrofitService;
import lab.dxythch.com.netlib.rx.NetManager;
import lab.dxythch.com.netlib.rx.RxManager;
import lab.dxythch.com.netlib.rx.RxNetSubscriber;
import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.header.ClassicHeader;

/**
 * "我的收藏中viewPager的填充fragment"
 */
public class CollectPagerItemFragment extends FragmentLazy {


    private SwipeMenuRecyclerView rv;
    private CollectRvAdapter adapter;
    private DepositDialog dialog1;
    private SmoothRefreshLayout mRefreshLayout;
    private int pageNumber =1;
    private int pageSize = 20;
    private List<Collect> firstPageDatas;
    private List<Collect> nextPageData;
    private Handler mHandler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View meLayout = inflater.inflate(R.layout.fragment_collect, container, false);
        initView(meLayout);
        initData();
        return meLayout;
    }

    private void initView(View v) {
        rv = v.findViewById(R.id.rv_collect);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRefreshLayout = v.findViewById(R.id.smoothRefreshLayout);
        adapter = new CollectRvAdapter(getActivity());
        setOnClickListener();

        /*侧滑删除*/
        rv.setSwipeItemClickListener(mItemClickListener);
        rv.setSwipeMenuCreator(mSwipeMenuCreator);
        rv.setSwipeMenuItemClickListener(mMenuItemClickListener);

        //下拉刷新和上啦加载
        ClassicHeader classicHeader = new ClassicHeader(getActivity());
        classicHeader.setLastUpdateTimeKey("header_last_update_time");
        mRefreshLayout.setHeaderView(classicHeader);
        mRefreshLayout.setEnableKeepRefreshView(true);
        mRefreshLayout.setDisableLoadMore(false);
        mRefreshLayout.setOnRefreshListener(new RefreshingListenerAdapter() {
            @Override
            public void onRefreshBegin(final boolean isRefresh) {
                pageNumber=1;
                initData();
            }
        });
        adapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                pageNumber++;
                loadNextPageData();
            }
        }, rv);
        mRefreshLayout.autoRefresh(true);

        rv.setOverScrollMode(View.OVER_SCROLL_NEVER);
        rv.setAdapter(adapter);
    }

    /**
     * RecyclerView的Item中的Menu点击监听。
     */
    private SwipeMenuItemClickListener mMenuItemClickListener = new SwipeMenuItemClickListener() {
        @Override
        public void onItemClick(SwipeMenuBridge menuBridge) {
            menuBridge.closeMenu();
            int direction = menuBridge.getDirection(); // 左侧还是右侧菜单。
            if (direction == SwipeMenuRecyclerView.RIGHT_DIRECTION) {
                int adapterPosition = menuBridge.getAdapterPosition(); // RecyclerView的Item的position。
                //删除item的操作
                dialog1 = new DepositDialog(getContext(), "请求中");
                deleteItemById(((Collect)adapter.getData().get(0)).getGid(), adapterPosition);
            }
        }
    };

    /**
     * 菜单创建器，在Item要创建菜单的时候调用。
     */
    private SwipeMenuCreator mSwipeMenuCreator = new SwipeMenuCreator() {
        @Override
        public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
            int width = getResources().getDimensionPixelSize(R.dimen.dimen_70);
            // 1. MATCH_PARENT 自适应高度，保持和Item一样高;
            // 2. 指定具体的高，比如80;
            // 3. WRAP_CONTENT，自身高度，不推荐;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            SwipeMenuItem closeItem = new SwipeMenuItem(getActivity())
                    .setBackground(R.drawable.swipe_item_selector)
                    .setImage(R.mipmap.icon_shanchu)
                    .setWidth(width)
                    .setHeight(height);
            swipeRightMenu.addMenuItem(closeItem); // 添加菜单到右侧。
        }
    };

    /**
     * RecyclerView的Item点击监听。
     */
    private SwipeItemClickListener mItemClickListener = new SwipeItemClickListener() {
        @Override
        public void onItemClick(View itemView, int position) {

        }
    };

    private void deleteItemById(String gid, final int position) {
        RetrofitService dxyService = NetManager.getInstance().createString(
                RetrofitService.class
        );
        RxManager.getInstance().doNetSubscribe(dxyService.removeCollection("c82e9e7612a447358c2a82ef437f3d11", gid))
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(final Disposable disposable) throws Exception {
                        RxLogUtils.d("doOnSubscribe");
                        dialog1.show();
                    }
                })
                .doFinally(
                        new Action() {
                            @Override
                            public void run() throws Exception {
                                RxLogUtils.d("结束");
                                dialog1.dismiss();
                            }
                        })
                .subscribe(new RxNetSubscriber<String>() {
                    @Override
                    protected void _onNext(String s) {
                        RxLogUtils.d("结束" + s);
                        try {
                            JSONObject obj = new JSONObject(s);
                            RxToast.showToast(obj.getString("msg"));
                            //如果成功，刷新列表，不加载数据
                            if(obj.getString("code") == "0") {
                                adapter.remove(position);
                                adapter.notifyItemChanged(position);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected void _onError(String error) {
                        RxToast.error(error);
                    }
                });
    }

    private void setOnClickListener() {

    }

    public void initData() {
        RetrofitService dxyService = NetManager.getInstance().createString(
                RetrofitService.class
        );
        RxManager.getInstance().doNetSubscribe(dxyService.collectionList("c82e9e7612a447358c2a82ef437f3d11", pageNumber, pageSize))
                .subscribe(new RxNetSubscriber<String>() {
                    @Override
                    protected void _onNext(String s) {
                        mRefreshLayout.refreshComplete();
                        RxLogUtils.d("结束" + s);
                        try {
                            JSONObject object = new JSONObject(s);
                            Gson gson = new Gson();
                            firstPageDatas = gson.fromJson(object.getString("list"), new TypeToken<List<Collect>>() {}.getType());
                            if(firstPageDatas!=null && firstPageDatas.size()>0) {
                                adapter.setNewData(firstPageDatas);
                            }
                            boolean hasNextPage = object.getBoolean("hasNextPage");
                            mRefreshLayout.setEnableNoMoreData(hasNextPage);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void _onError(String e) {
                        mRefreshLayout.refreshComplete();
                        RxLogUtils.d("结束_onError" + e);
                    }
                });
    }

    private void loadNextPageData() {
        RetrofitService dxyService = NetManager.getInstance().createString(
                RetrofitService.class
        );
        RxManager.getInstance().doNetSubscribe(dxyService.collectionList("c82e9e7612a447358c2a82ef437f3d11", pageNumber, pageSize))
                .subscribe(new RxNetSubscriber<String>() {
                    @Override
                    protected void _onNext(String s) {
                        adapter.loadMoreEnd();
                        RxLogUtils.d("结束" + s);
                        try {
                            JSONObject object = new JSONObject(s);
                            Gson gson = new Gson();
                            nextPageData = gson.fromJson(object.getString("list"), new TypeToken<List<Collect>>() {}.getType());
                            adapter.addData(nextPageData);
                            boolean hasNextPage = object.getBoolean("hasNextPage");
                            mRefreshLayout.setEnableNoMoreData(hasNextPage);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void _onError(String e) {
                        adapter.loadMoreEnd();
                        RxLogUtils.d("结束_onError" + e);
                    }
                });
    }
}