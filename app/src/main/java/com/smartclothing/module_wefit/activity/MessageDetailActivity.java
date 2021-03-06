package com.smartclothing.module_wefit.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartclothing.module_wefit.R;
import com.smartclothing.module_wefit.bean.Device;
import com.smartclothing.module_wefit.bean.Message;
import com.smartclothing.module_wefit.tools.DateUtils;
import com.vondear.rxtools.dateUtils.RxTimeUtils;
import com.vondear.rxtools.utils.RxLogUtils;
import com.vondear.rxtools.view.RxToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import lab.dxythch.com.netlib.net.RetrofitService;
import lab.dxythch.com.netlib.rx.NetManager;
import lab.dxythch.com.netlib.rx.RxManager;
import lab.dxythch.com.netlib.rx.RxNetSubscriber;

/*消息详情*/

public class MessageDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout iv_back;
    private TextView tv_title;
    private TextView tv_message_detail_title;
    private TextView tv_message_detail_time;
    private TextView tv_message_detail_content;
    private Message messageBean;
    private int msgType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);
        initView();
        //查看并更新已读
        initData();
    }

    private void initView() {
        iv_back = findViewById(R.id.iv_back);
        tv_title = findViewById(R.id.tv_title);
        tv_message_detail_title = findViewById(R.id.tv_message_detail_title);
        tv_message_detail_time = findViewById(R.id.tv_message_detail_time);
        tv_message_detail_content = findViewById(R.id.tv_message_detail_content);
        if (getIntent() != null) {
            Intent intentData = getIntent();
            if (intentData.hasExtra("messageBean")) {
                messageBean = (Message) intentData.getSerializableExtra("messageBean");
                if(messageBean.getTitle()!=null)
                    tv_message_detail_title.setText(messageBean.getTitle());
                if(messageBean.getPushTime()>0)
                    tv_message_detail_time.setText(RxTimeUtils.milliseconds2String(messageBean.getPushTime(), new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())));
                if(messageBean.getGid()!=null)
                    tv_message_detail_content.setText(messageBean.getContent());
                if (intentData.hasExtra("msgType")) {
                    msgType = intentData.getIntExtra("msgType", 2);
                    tv_title.setText(msgType == 2 ? "提醒":"通知");
                }
            }
        }
        listener();
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

    private void initData() {
        RetrofitService dxyService = NetManager.getInstance().createString(
                RetrofitService.class
        );
        RxManager.getInstance().doNetSubscribe(dxyService.readed("c82e9e7612a447358c2a82ef437f3d11",messageBean.getGid()))
                .subscribe(new RxNetSubscriber<String>() {
                    @Override
                    protected void _onNext(String s) {
                        RxLogUtils.d("结束" + s);
                    }

                    @Override
                    public void _onError(String e) {
                        RxToast.showToast(e);
                    }
                });
    }
}
