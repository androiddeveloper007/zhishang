package com.smartclothing.module_wefit.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.smartclothing.module_wefit.R;
import com.smartclothing.module_wefit.bean.Device;
import com.smartclothing.module_wefit.tools.GlideImageLoader;
import com.smartclothing.module_wefit.tools.ImageLoaderUtil;
import com.smartclothing.module_wefit.widget.ImageUploadView;
import com.smartclothing.module_wefit.widget.MClearEditText;
import com.smartclothing.module_wefit.widget.NiceSpinner;
import com.smartclothing.module_wefit.widget.ImageClickListener;
import com.smartclothing.module_wefit.widget.ImageModel;
import com.smartclothing.module_wefit.widget.dialog.DepositDialog;
import com.vondear.rxtools.utils.RxLogUtils;
import com.vondear.rxtools.view.RxToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import lab.dxythch.com.netlib.net.RetrofitService;
import lab.dxythch.com.netlib.rx.NetManager;
import lab.dxythch.com.netlib.rx.RxManager;
import lab.dxythch.com.netlib.rx.RxNetSubscriber;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/*问题与建议*/

public class ProblemSuggestActivity extends AppCompatActivity implements View.OnClickListener, ImageClickListener {

    private LinearLayout iv_back;
    private NiceSpinner spinner_problem_type, spinner_frequency;
    private MClearEditText et_problem_call;
    private EditText et_data_sign;
    private ImageUploadView iv_load;
    private ImagePicker imagePicker;
    private TextView tv_problem_commit;
    
    private ArrayList<ImageModel> imgList = new ArrayList<>();

    private List<String> dataset0;
    private List<String> dataset1;
    private DepositDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_problem_suggest);
        initView();
    }

    private void initView() {
        iv_back = findViewById(R.id.iv_back);
        spinner_problem_type = findViewById(R.id.spinner_problem_type);
        spinner_frequency = findViewById(R.id.spinner_frequency);
        et_data_sign = findViewById(R.id.et_data_sign);
        et_problem_call = findViewById(R.id.et_problem_call);
        iv_load = findViewById(R.id.iv_load);
        tv_problem_commit = findViewById(R.id.tv_problem_commit);
        
        iv_load.setImageClickListener(this).setImageLoaderInterface(
                new ImageLoaderUtil()).setShowDel(true).setOneLineShowNum(4)
                .setmPicSize(getResources().getDimensionPixelSize(R.dimen.dimen_70));//ImageUploadView.dp2px(this, 80)
        listener();
        initImagePick();
        dataset0 = new LinkedList<>(Arrays.asList("设备质量", "蓝牙连接", "App闪退", "其它"));
        dataset1 = new LinkedList<>(Arrays.asList("经常出现", "偶尔出现", "很少出现", "老是出现"));
        spinner_problem_type.attachDataSource(dataset0);
        spinner_frequency.attachDataSource(dataset1);
    }

    private void initImagePick() {
        imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());   //设置图片加载器
        imagePicker.setShowCamera(true);  //显示拍照按钮
        imagePicker.setMultiMode(true);
        imagePicker.setSelectLimit(4);    //选中数量限制
        imagePicker.setFocusWidth(100);   //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.setFocusHeight(100);  //裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker.setOutPutX(100);//保存文件的宽度。单位像素
        imagePicker.setOutPutY(100);//保存文件的高度。单位像素
    }

    private void listener() {
        iv_back.setOnClickListener(this);
        tv_problem_commit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.tv_problem_commit:
                //提交表单
                feedbackImg();
                break;
        }
    }

    /*提交反馈，文字部分*/
    private void commitData(String imgUrl) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("ariseFreq", spinner_frequency.getSelectedIndex());
            jsonObject.put("contactInfo", et_problem_call.getText().toString());
            jsonObject.put("dealStatus", spinner_problem_type.getSelectedIndex());
            jsonObject.put("feedbackDesc", et_data_sign.getText().toString());
            jsonObject.put("feedbackImg", imgUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8")
                , jsonObject.toString());
        RetrofitService dxyService = NetManager.getInstance().createString(
                RetrofitService.class
        );
        RxManager.getInstance().doNetSubscribe(dxyService.feedback(body))
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

    /*上传图片*/
    public void feedbackImg() {
        dialog = new DepositDialog(ProblemSuggestActivity.this, "图片上传中，请稍等");
        List<File> files = new ArrayList<>();
        if (imgList.size() > 0) {
            for (int i = 0; i < imgList.size(); i++) {
                File  file = new File(imgList.get(i).getImg());
                files.add(file);
            }
        }
        List<MultipartBody.Part> body = filesToMultipartBodyParts(files);
        RetrofitService dxyService = NetManager.getInstance().createString(RetrofitService.class);
        RxManager.getInstance().doNetSubscribe(dxyService.feedbackImg(body))
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(final Disposable disposable) throws Exception {
                        RxLogUtils.d("doOnSubscribe");
                        dialog.show();
                    }
                })
                .doFinally(
                        new Action() {
                            @Override
                            public void run() throws Exception {
                                RxLogUtils.d("结束");
                                dialog.dismiss();
                            }
                        })
                .subscribe(new RxNetSubscriber<String>() {
                    @Override
                    protected void _onNext(String s) {
                        RxLogUtils.d("结束" + s);
                        RxToast.success("上传多张图片成功,返回的结果：  "+s, 2000);
                        commitData("");
                    }

                    @Override
                    protected void _onError(String error) {
                        RxToast.error(error);
                    }
                });
    }

    public static List<MultipartBody.Part> filesToMultipartBodyParts(List<File> files) {
        List<MultipartBody.Part> parts = new ArrayList<>(files.size());
        for (File file : files) {
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/png"), file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
            parts.add(part);
        }
        return parts;
    }

    public static MultipartBody filesToMultipartBody(List<File> files) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        for (File file : files) {
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/png"), file);//multipart/form-data
            builder.addFormDataPart("img", file.getName(), requestBody);
        }
        builder.setType(MultipartBody.FORM);
        MultipartBody multipartBody = builder.build();
        return multipartBody;
    }

    private static final int REQUEST_CODE = 1024;
    @Override
    public void addImageClickListener() {
        imagePicker.setSelectLimit(4-iv_load.getImageList().size());    //选中数量限制
        Intent intent = new Intent(ProblemSuggestActivity.this, ImageGridActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void showImageClickListener(ArrayList<ImageModel> list, int position) {

    }

    @Override
    public void delImageClickListener(int position) {
        iv_load.delImage(position);
        imgList.remove(position);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (requestCode == REQUEST_CODE&&data!=null) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                if (images != null && images.size() > 0) {
                    imgList.clear();
                    for (ImageItem item : images){
                        imgList.add(new ImageModel(item.path));
                        RxLogUtils.d("图片路径：" + item.path);
                    }
                    iv_load.addImage(imgList);
                }
            }
        }
    }
}
