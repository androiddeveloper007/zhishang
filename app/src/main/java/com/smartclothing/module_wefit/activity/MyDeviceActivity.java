package com.smartclothing.module_wefit.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.smartclothing.module_wefit.R;
import com.smartclothing.module_wefit.adapter.DeviceRvAdapter;
import com.smartclothing.module_wefit.bean.Device;
import com.smartclothing.module_wefit.bean.UserCenterBean;
import com.smartclothing.module_wefit.widget.dialog.DepositDialog;
import com.vondear.rxtools.utils.RxLogUtils;
import com.vondear.rxtools.utils.RxUtils;
import com.vondear.rxtools.view.RxToast;

import org.json.JSONArray;
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

import static com.qmuiteam.qmui.widget.dialog.QMUITipDialog.Builder.ICON_TYPE_LOADING;

/*我的设备*/

public class MyDeviceActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout iv_back;
    private RecyclerView rv_device;
    private DeviceRvAdapter rvAdapter;
    private DepositDialog dialog1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_device0);
        initView();
        initData();
    }

    private void initView() {
        iv_back = findViewById(R.id.iv_back);
        rv_device = findViewById(R.id.rv_device);
        initRecyclerView();
        listener();
        dialog1 = new DepositDialog(this, "加载中...");
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

    private void initRecyclerView() {
        rvAdapter = new DeviceRvAdapter();
        rv_device.setLayoutManager(new LinearLayoutManager(this));
        rv_device.setAdapter(rvAdapter);
        rvAdapter.setUpdateClickListener(new DeviceRvAdapter.deleteClickListener() {
            @Override
            public void onUpdateClick(final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MyDeviceActivity.this)
                        .setTitle("确定删除该设备？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteDeviceById(rvAdapter.getData().get(position).getGid());
                                dialog1.show();
                            }
                        }).setNegativeButton(R.string.cancel, null);
                final AlertDialog dialog = builder.create();
                Window window = dialog.getWindow();
                window.setDimAmount(0.3f);
                window.setWindowAnimations(R.style.dialogWindowAnim);
                dialog.show();
            }
        });
        //默认加一条添加设备的item
        List<Device> emptyList = new ArrayList<>();
        emptyList.add(new Device(0));
        rvAdapter.setNewData(emptyList);
        rvAdapter.setAddDeviceClickListener(new DeviceRvAdapter.addDeviceClickListener(){
            @Override
            public void onAddDeviceClick() {
                //添加绑定设备，这里实在不会
                RxToast.showToast("添加绑定设备");
            }
        });
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
                            for(Device device : beanList) {
                                device.setType(1);
                            }
                            if(beanList.size()>0)
                                rvAdapter.setNewData(beanList);
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

    private void deleteDeviceById(String gid) {
        RetrofitService dxyService = NetManager.getInstance().createString(
                RetrofitService.class
        );
        RxManager.getInstance().doNetSubscribe(dxyService.removeBind("c82e9e7612a447358c2a82ef437f3d11", gid))
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

}
