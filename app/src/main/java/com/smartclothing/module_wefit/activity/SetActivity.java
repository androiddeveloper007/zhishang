package com.smartclothing.module_wefit.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartclothing.module_wefit.R;
import com.smartclothing.module_wefit.widget.MClearEditText;
import com.smartclothing.module_wefit.widget.dialog.DepositDialog;
import com.vondear.rxtools.utils.RxLogUtils;
import com.vondear.rxtools.view.RxToast;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import lab.dxythch.com.netlib.net.RetrofitService;
import lab.dxythch.com.netlib.rx.NetManager;
import lab.dxythch.com.netlib.rx.RxManager;
import lab.dxythch.com.netlib.rx.RxNetSubscriber;

/*设置*/

public class SetActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout iv_back;
    private TextView tv_set_exit;
    private RelativeLayout rl_set_clean;
    private DepositDialog dialog1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_set);

        initView();

    }

    private void initView() {
        iv_back = findViewById(R.id.iv_back);
        rl_set_clean = findViewById(R.id.rl_set_clean);
        tv_set_exit = findViewById(R.id.tv_set_exit);
        listener();
    }

    private void listener() {
        iv_back.setOnClickListener(this);
        rl_set_clean.setOnClickListener(this);
        tv_set_exit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.rl_set_clean:
                //清楚数据
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("清楚缓存？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setNegativeButton(R.string.cancel, null);
                final AlertDialog dialog = builder.create();
                Window window = dialog.getWindow();
                window.setDimAmount(0.3f);
                window.setWindowAnimations(R.style.dialogWindowAnim);
                dialog.show();
                break;
            case R.id.tv_set_exit:
                //退出登录
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this)
                        .setTitle("退出登录？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                logout();
                            }
                        }).setNegativeButton(R.string.cancel, null);
                final AlertDialog dialog1 = builder1.create();
                Window window1 = dialog1.getWindow();
                window1.setDimAmount(0.3f);
                window1.setWindowAnimations(R.style.dialogWindowAnim);
                dialog1.show();
                break;
        }
    }

    private void logout() {
        dialog1 = new DepositDialog(SetActivity.this, "请求中");
        RetrofitService dxyService = NetManager.getInstance().createString(
                RetrofitService.class
        );
        RxManager.getInstance().doNetSubscribe(dxyService.logout())
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
                        finish();
                    }

                    @Override
                    protected void _onError(String error) {
                        RxToast.error(error);
                    }
                });
    }
}
