package com.smartclothing.module_wefit.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartclothing.module_wefit.R;
import com.smartclothing.module_wefit.adapter.AboutRvAdapter;
import com.smartclothing.module_wefit.adapter.DeviceRvAdapter;
import com.smartclothing.module_wefit.bean.AboutDeviceBean;
import com.smartclothing.module_wefit.bean.Collect;
import com.smartclothing.module_wefit.bean.Device;
import com.smartclothing.module_wefit.widget.dialog.AboutUpdateDialog;
import com.smartclothing.module_wefit.widget.dialog.DepositDialog;
import com.smartclothing.module_wefit.widget.progressbar.UpdateProgressBar;
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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import lab.dxythch.com.netlib.net.RetrofitService;
import lab.dxythch.com.netlib.rx.NetManager;
import lab.dxythch.com.netlib.rx.RxManager;
import lab.dxythch.com.netlib.rx.RxNetSubscriber;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/*关于*/

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout iv_back;
    private TextView tv_version;
    private RecyclerView rv_about;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        initView();

        initData();
    }

    private void initData() {
        RetrofitService dxyService = NetManager.getInstance().createString(
                RetrofitService.class
        );
        RxManager.getInstance().doNetSubscribe(dxyService.deviceList("c82e9e7612a447358c2a82ef437f3d11"))
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(final Disposable disposable) throws Exception {
                        RxLogUtils.d("doOnSubscribe");
                    }
                })
                .doFinally(
                        new Action() {
                            @Override
                            public void run() throws Exception {
                                RxLogUtils.d("结束");
                            }
                        })
                .subscribe(new RxNetSubscriber<String>() {
                    @Override
                    protected void _onNext(String s) {
                        RxLogUtils.d("结束" + s);
                        try {
                            Gson gson = new Gson();
                            JSONObject jsonObject = new JSONObject(s);
                            Type typeList = new TypeToken<List<Device>>() {}.getType();
                            List<Device> beanList = gson.fromJson(jsonObject.getString("list"), typeList);
                            RxLogUtils.e("PP", "beanList'size "+ beanList.size());
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

    private void initView() {
        iv_back = findViewById(R.id.iv_back);
        tv_version = findViewById(R.id.tv_version);
        //版本号
        PackageManager manager = this.getPackageManager();
        String version = "1.0.0";
        try {
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            version = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        tv_version.setText("版本号v"+version);

        listener();

        initRv();
    }

    private void initRv() {
        rv_about = findViewById(R.id.rv_about);

        rv_about.setLayoutManager(new LinearLayoutManager(this));

        final AboutRvAdapter rvAdapter = new AboutRvAdapter(this);

        rv_about.setAdapter(rvAdapter);

        List<AboutDeviceBean> list = new ArrayList<>();
        AboutDeviceBean bean = new AboutDeviceBean("","","");
        list.add(bean);
        list.add(bean);
        rvAdapter.setNewData(list);
        rvAdapter.setUpdateClickListener(new AboutRvAdapter.updateClickListener() {
            @Override
            public void onUpdateClick(int position) {
                AboutUpdateDialog dialog = new AboutUpdateDialog(AboutActivity.this, 0.9f);
                dialog.setProgressWithFloat(100);
                dialog.show();
            }
        });
    }

    private void listener() {
        iv_back.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
        }
    }
}
