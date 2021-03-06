package com.smartclothing.module_wefit.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartclothing.module_wefit.BuildConfig;
import com.smartclothing.module_wefit.R;
import com.smartclothing.module_wefit.bean.Sex;
import com.smartclothing.module_wefit.bean.UserInfo;
import com.smartclothing.module_wefit.tools.CircleImageView;
import com.smartclothing.module_wefit.tools.ClipImageActivity;
import com.smartclothing.module_wefit.tools.ConvertUtils;
import com.smartclothing.module_wefit.widget.dialog.DepositDialog;
import com.smartclothing.module_wefit.widget.wheelpicker.picker.DatePicker;
import com.smartclothing.module_wefit.widget.wheelpicker.picker.SinglePicker;
import com.vondear.rxtools.utils.RxLogUtils;
import com.vondear.rxtools.view.RxToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
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

/*个人资料*/

public class PersonalDataActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout iv_back;
    private TextView tv_data_save;
    private RelativeLayout rl_data_icon;
    private RelativeLayout rl_data_name;
    private RelativeLayout rl_data_sex;
    private RelativeLayout rl_data_birth;
    private RelativeLayout rl_data_height;
    private RelativeLayout rl_data_weight;
    private RelativeLayout rl_data_phone;
    private RelativeLayout rl_data_sign;
    private View parentView;
    private CircleImageView iv_data_icon;
    private TextView tv_data_name;
    private TextView tv_data_sex;
    private TextView tv_data_birth;
    private TextView tv_data_height;
    private TextView tv_data_weight;
    private TextView tv_data_phone;
    private TextView tv_data_sign;

    //请求相机
    private static final int REQUEST_CAPTURE = 100;
    //请求相册
    private static final int REQUEST_PICK = 101;
    //请求截图
    private static final int REQUEST_CROP_PHOTO = 102;
    //请求访问外部存储
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 103;
    //请求写入外部存储
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 104;
    //请求摄像头
    private static final int CAMERA_REQUEST_CODE = 105;
    private static final int REQUEST_CODE_NAME = 106;
    private static final int RESULT_CODE_NAME = 107;
    private static final int REQUEST_CODE_PHONE = 108;
    private static final int RESULT_CODE_PHONE = 109;
    private static final int REQUEST_CODE_SING = 110;
    private static final int RESULT_CODE_SIGN = 111;
    //调用照相机返回图片临时文件
    private File tempFile;
    private String cropImagePath;
    private UserInfo user;
    private String name;//用于保存修改的用户名
    private boolean hasUpdateUserData;
    private DepositDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        parentView = LayoutInflater.from(this).inflate(R.layout.activity_personal_data, null);

        setContentView(parentView);

        initView();

        //创建拍照存储的临时文件
        createCameraTempFile(savedInstanceState);

        initData();
    }

    private void initView() {
        iv_back = findViewById(R.id.iv_back);
        tv_data_save = findViewById(R.id.tv_data_save);
        rl_data_icon = findViewById(R.id.rl_data_icon);
        rl_data_name = findViewById(R.id.rl_data_name);
        rl_data_sex = findViewById(R.id.rl_data_sex);
        rl_data_birth = findViewById(R.id.rl_data_birth);
        rl_data_height = findViewById(R.id.rl_data_height);
        rl_data_weight = findViewById(R.id.rl_data_weight);
        rl_data_phone = findViewById(R.id.rl_data_phone);
        rl_data_sign = findViewById(R.id.rl_data_sign);
        iv_data_icon = findViewById(R.id.iv_data_icon);
        tv_data_name = findViewById(R.id.tv_data_name);
        tv_data_sex = findViewById(R.id.tv_data_sex);
        tv_data_birth = findViewById(R.id.tv_data_birth);
        tv_data_height = findViewById(R.id.tv_data_height);
        tv_data_weight = findViewById(R.id.tv_data_weight);
        tv_data_phone = findViewById(R.id.tv_data_phone);
        tv_data_sign = findViewById(R.id.tv_data_sign);
        listener();
    }

    /**
     * 获取用户资料
     * */
    private void initData() {
        JSONObject jsonObject = new JSONObject();
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8")
                , jsonObject.toString());
        RetrofitService dxyService = NetManager.getInstance().createString(
                RetrofitService.class
        );
        RxManager.getInstance().doNetSubscribe(dxyService.userInfo(body))
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
                        Gson gson = new Gson();
                        user = gson.fromJson(s, new TypeToken<UserInfo>() {}.getType());
                        if(user!=null) {
                            if(user.getUserName()!=null) {
                                tv_data_name.setText(user.getUserName());
                            }
                            if(user.getSex()!=null) {
                                tv_data_sex.setText(user.getSex().equals("0") ? "男":"女");
                            }
                            if(user.getBirthday()!=null) {
                                tv_data_birth.setText(user.getBirthday());
                            }
                            if(user.getHeight()!=null) {
                                tv_data_height.setText(user.getHeight());
                            }
                            if(user.getTargetWeight()!=null) {
                                tv_data_weight.setText(user.getTargetWeight());
                            }
                            if(user.getPhone()!=null) {
                                tv_data_phone.setText(user.getPhone());
                            }
                            if(user.getSign()!=null) {
                                tv_data_sign.setText(user.getSign());
                            }
                            if(user.getUserImg()!=null) {
                                String url = user.getUserImg().replace("\\", "");
                                Glide.with(PersonalDataActivity.this).load(url).into(iv_data_icon);//.placeholder(R.mipmap.img_touxiang)
                            }
                        }
                    }

                    @Override
                    protected void _onError(String error) {
                        RxToast.error(error);
                    }
                });
    }

    private void listener() {
        iv_back.setOnClickListener(this);
        tv_data_save.setOnClickListener(this);
        rl_data_icon.setOnClickListener(this);
        rl_data_name.setOnClickListener(this);
        rl_data_sex.setOnClickListener(this);
        rl_data_birth.setOnClickListener(this);
        rl_data_height.setOnClickListener(this);
        rl_data_weight.setOnClickListener(this);
        rl_data_phone.setOnClickListener(this);
        rl_data_sign.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.tv_data_save:
                requestSaveUserInfo();
                break;
            case R.id.rl_data_icon:
                selectImage();
                break;
            case R.id.rl_data_name:
                startActivityForResult(new Intent(PersonalDataActivity.this, NameEditActivity.class), REQUEST_CODE_NAME);
                break;
            case R.id.rl_data_sex:
                List<Sex> data = new ArrayList<>();
                data.add(new Sex(1, "男"));
                data.add(new Sex(2, "女"));
                SinglePicker<Sex> picker = new SinglePicker<>(this, data);
                picker.setTextColor(getResources().getColor(R.color.picker_text_color));
                picker.setLineColor(getResources().getColor(R.color.picker_line_color));
                picker.setTopBackgroundColor(getResources().getColor(R.color.white));
                picker.setTopLineColor(getResources().getColor(R.color.white));
                picker.setTitleTextColor(getResources().getColor(R.color.picker_title_text_color));
                picker.setCancelTextColor(getResources().getColor(R.color.picker_title_text_color));
                picker.setSubmitTextColor(getResources().getColor(R.color.picker_title_text_color));
                picker.setCanceledOnTouchOutside(true);
                picker.setSelectedIndex(Integer.parseInt(user.getSex()));
                picker.setCycleDisable(true);
                picker.setOnItemPickListener(new SinglePicker.OnItemPickListener<Sex>() {
                    @Override
                    public void onItemPicked(int index, Sex item) {
                        user.setSex(item.getId()+"");
                        RxToast.showToast(item.getId()+"");
                        tv_data_sex.setText(item.getName());
                    }
                });
                picker.show();
                break;
            case R.id.rl_data_birth:
                final DatePicker picker0 = new DatePicker(this);
                picker0.setTextColor(getResources().getColor(R.color.picker_text_color));
                picker0.setLineColor(getResources().getColor(R.color.picker_line_color));
                picker0.setTopBackgroundColor(getResources().getColor(R.color.white));
                picker0.setTopLineColor(getResources().getColor(R.color.white));
                picker0.setTitleTextColor(getResources().getColor(R.color.picker_title_text_color));
                picker0.setCancelTextColor(getResources().getColor(R.color.picker_title_text_color));
                picker0.setSubmitTextColor(getResources().getColor(R.color.picker_title_text_color));
                picker0.setLabelTextColor(getResources().getColor(R.color.picker_title_text_color));
                picker0.setCanceledOnTouchOutside(true);
                picker0.setUseWeight(true);
                picker0.setTopPadding(ConvertUtils.toPx(this, 10));
                Calendar c = Calendar.getInstance();
                picker0.setRangeEnd(c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH));
                picker0.setRangeStart(1940, 1, 1);
                picker0.setSelectedItem(1980, 1, 1);
                picker0.setResetWhileWheel(false);
                picker0.setOnDatePickListener(new DatePicker.OnYearMonthDayPickListener() {
                    @Override
                    public void onDatePicked(String year, String month, String day) {
                        user.setBirthday(year+month+day);
                        RxToast.showToast(year+month+day);
                        tv_data_birth.setText(year+month+day);
                    }
                });
                picker0.setOnWheelListener(new DatePicker.OnWheelListener() {
                    @Override
                    public void onYearWheeled(int index, String year) {
                        picker0.setTitleText(year + "-" + picker0.getSelectedMonth() + "-" + picker0.getSelectedDay());
                    }

                    @Override
                    public void onMonthWheeled(int index, String month) {
                        picker0.setTitleText(picker0.getSelectedYear() + "-" + month + "-" + picker0.getSelectedDay());
                    }

                    @Override
                    public void onDayWheeled(int index, String day) {
                        picker0.setTitleText(picker0.getSelectedYear() + "-" + picker0.getSelectedMonth() + "-" + day);
                    }
                });
                picker0.show();
                break;
            case R.id.rl_data_height:
                String[] array = new String[81];
                for(int i=0;i<81;i++){ array[i] = 120+i+"cm"; }
                SinglePicker<String> picker1 = new SinglePicker<>(this, array);
                picker1.setTextColor(getResources().getColor(R.color.picker_text_color));
                picker1.setLineColor(getResources().getColor(R.color.picker_line_color));
                picker1.setTopBackgroundColor(getResources().getColor(R.color.white));
                picker1.setTopLineColor(getResources().getColor(R.color.white));
                picker1.setTitleTextColor(getResources().getColor(R.color.picker_title_text_color));
                picker1.setCancelTextColor(getResources().getColor(R.color.picker_title_text_color));
                picker1.setSubmitTextColor(getResources().getColor(R.color.picker_title_text_color));
                picker1.setCanceledOnTouchOutside(true);
                picker1.setSelectedIndex(Integer.parseInt(user.getHeight())-120);
                picker1.setCycleDisable(true);
                picker1.setOnItemPickListener(new SinglePicker.OnItemPickListener<String>() {
                    @Override
                    public void onItemPicked(int index, String item) {
                        user.setHeight(item.substring(0,3));
                        tv_data_height.setText(item.substring(0,3));
                    }
                });
                picker1.show();
                break;
            case R.id.rl_data_weight:
                String[] array1 = new String[46];
                for(int i=0;i<46;i++){
                    array1[i] = 45+i+"kg";
                }
                SinglePicker<String> picker2 = new SinglePicker<>(this, array1);
                picker2.setCanceledOnTouchOutside(true);
                picker2.setTextColor(getResources().getColor(R.color.picker_text_color));
                picker2.setLineColor(getResources().getColor(R.color.picker_line_color));
                picker2.setTopBackgroundColor(getResources().getColor(R.color.white));
                picker2.setTopLineColor(getResources().getColor(R.color.white));
                picker2.setTitleTextColor(getResources().getColor(R.color.picker_title_text_color));
                picker2.setCancelTextColor(getResources().getColor(R.color.picker_title_text_color));
                picker2.setSubmitTextColor(getResources().getColor(R.color.picker_title_text_color));
                picker2.setSelectedIndex(Integer.parseInt(user.getTargetWeight())-45);
                picker2.setCycleDisable(true);
                picker2.setOnItemPickListener(new SinglePicker.OnItemPickListener<String>() {
                    @Override
                    public void onItemPicked(int index, String item) {
                        user.setTargetWeight(item.substring(0,2));
                        tv_data_weight.setText(item.substring(0,2));
                    }
                });
                picker2.show();
                break;
            case R.id.rl_data_phone:
                startActivityForResult(new Intent(PersonalDataActivity.this, PhoneEditActivity.class), REQUEST_CODE_PHONE);
                break;
            case R.id.rl_data_sign:
                startActivityForResult(new Intent(PersonalDataActivity.this, SignEditActivity.class), REQUEST_CODE_SING);
                break;
        }
    }

    /*保存按钮，提交用户数据*/
    private void requestSaveUserInfo() {
        if(dialog==null)
            dialog = new DepositDialog(this, "提交中");
        if(user==null) RxToast.showToast("用户数据不能为空");
        JSONObject jsonObject = new JSONObject();
        try {
            Gson gson = new Gson();
            jsonObject.put("userInfo", gson.toJson(user));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8")
                , jsonObject.toString());
        RetrofitService dxyService = NetManager.getInstance().createString(RetrofitService.class);
        RxManager.getInstance().doNetSubscribe(dxyService.saveUserInfo(body))
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(final Disposable disposable) throws Exception {
                        dialog.show();
                        RxLogUtils.d("doOnSubscribe");
                    }
                })
                .doFinally(
                        new Action() {
                            @Override
                            public void run() throws Exception {
                                RxLogUtils.d("结束");
                                dialog.hideDialog();
                            }
                        })
                .subscribe(new RxNetSubscriber<String>() {
                    @Override
                    protected void _onNext(String s) {
                        RxLogUtils.d("结束" + s);
                        RxToast.success("保存成功", 2000);
                        hasUpdateUserData=true;
                    }

                    @Override
                    protected void _onError(String error) {
                        RxToast.error(error);
                    }
                });
    }

    /*弹出选择图片的方式popupWindow*/
    private void selectImage() {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_popupwindow, null);
        TextView btnCamera = view.findViewById(R.id.btn_camera);
        TextView btnPhoto = view.findViewById(R.id.btn_photo);
        TextView btnCancel = view.findViewById(R.id.btn_cancel);
        final PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(android.R.color.transparent));
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAtLocation(tv_data_sign, Gravity.BOTTOM, 0, 0);
        //popupWindow在弹窗的时候背景半透明
        final WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 0.5f;
        getWindow().setAttributes(params);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                params.alpha = 1.0f;
                getWindow().setAttributes(params);
            }
        });
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //权限判断
                if (ContextCompat.checkSelfPermission(PersonalDataActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请WRITE_EXTERNAL_STORAGE权限
                    ActivityCompat.requestPermissions(PersonalDataActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                } else {
                    //跳转到调用系统相机
                    gotoCamera();
                }
                popupWindow.dismiss();
            }
        });
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //权限判断
                if (ContextCompat.checkSelfPermission(PersonalDataActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请READ_EXTERNAL_STORAGE权限
                    ActivityCompat.requestPermissions(PersonalDataActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            READ_EXTERNAL_STORAGE_REQUEST_CODE);
                } else {
                    //跳转到调用系统图库
                    gotoPhoto();
                }
                popupWindow.dismiss();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    /*跳到相机*/
    private void gotoCamera() {
        //权限判断
        if (ContextCompat.checkSelfPermission(PersonalDataActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PersonalDataActivity.this, new String[]{Manifest.permission.CAMERA},
                    CAMERA_REQUEST_CODE);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    tempFile);// getPackageName()
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, REQUEST_CAPTURE);
        }
    }

    /**
     * 跳转到相册
     */
    private void gotoPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "请选择图片"), REQUEST_PICK);
    }

    /**
     * 创建调用系统照相机待存储的临时文件
     *
     * @param savedInstanceState
     */
    private void createCameraTempFile(Bundle savedInstanceState) {

        if (savedInstanceState != null && savedInstanceState.containsKey("tempFile")) {
            tempFile = (File) savedInstanceState.getSerializable("tempFile");
        } else {
            tempFile = new File(checkDirPath(Environment.getExternalStorageDirectory().getPath() + "/image/"),
                    System.currentTimeMillis() + ".jpg");
        }
    }

    /**
     * 检查文件是否存在
     */
    private static String checkDirPath(String dirPath) {
        if (TextUtils.isEmpty(dirPath)) {
            return "";
        }
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dirPath;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //权限判断
                if (ContextCompat.checkSelfPermission(PersonalDataActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(PersonalDataActivity.this, new String[]{Manifest.permission.CAMERA},
                            CAMERA_REQUEST_CODE);
                }
            } else {

            }
        } else if (requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                gotoPhoto();
            } else {

            }
        } else if(requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                gotoCamera();
            } else {
                RxToast.showToast("请允许请求摄像头权限，否则无法使用该功能");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case REQUEST_CAPTURE: //调用系统相机返回
                if (resultCode == RESULT_OK)
                    gotoClipActivity(Uri.fromFile(tempFile));
                break;
            case REQUEST_PICK:  //调用系统相册返回
                if (resultCode == RESULT_OK)
                    gotoClipActivity(intent.getData());
                break;
            case REQUEST_CROP_PHOTO:  //剪切图片返回
                if (resultCode == RESULT_OK) {
                    final Uri uri = intent.getData();
                    if (uri == null) return;
                    //得到的剪切后的图片uri
                    cropImagePath = getRealFilePathFromUri(getApplicationContext(), uri);
                    Bitmap bitMap = BitmapFactory.decodeFile(cropImagePath);
                    iv_data_icon.setImageBitmap(bitMap);
                    //此处后面可以将bitMap转为二进制上传后台网络
                    uploadImage(cropImagePath);
                }
                break;
            case REQUEST_CODE_NAME://保存用户名
                if(requestCode == RESULT_CODE_NAME){
                    user.setUserName(intent.getStringExtra("name"));
                    tv_data_name.setText(intent.getStringExtra("name"));
                }
                break;
            case REQUEST_CODE_PHONE://保存联系电话
                if(requestCode == RESULT_CODE_PHONE){
                    user.setPhone(intent.getStringExtra("phone"));
                    tv_data_phone.setText(intent.getStringExtra("phone"));
                }
                break;
            case REQUEST_CODE_SING://保存签名
                if(requestCode == RESULT_CODE_SIGN){
                    user.setSign(intent.getStringExtra("sign"));
                    tv_data_sign.setText(intent.getStringExtra("sign"));
                }
                break;
        }
    }

    private void uploadImage(String cropImagePath) {
        RequestBody requestFile  = RequestBody.create(MediaType.parse("multipart/form-data"), new File(cropImagePath));
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", cropImagePath, requestFile);
        RetrofitService dxyService = NetManager.getInstance().createString(RetrofitService.class);
        RxManager.getInstance().doNetSubscribe(dxyService.uploadUserImg(body))
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
                        RxToast.success("保存成功", 2000);
                        //成功后将本地图片设置到imageView中，并在退回到个人中心时，刷新贴图url
                        hasUpdateUserData=true;
                    }

                    @Override
                    protected void _onError(String error) {
                        RxToast.error(error);
                    }
                });
    }

    /**
     * 打开截图界面
     */
    public void gotoClipActivity(Uri uri) {
        if (uri == null) {
            return;
        }
        Intent intent = new Intent();
        intent.setClass(this, ClipImageActivity.class);
        intent.putExtra("type", 1);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_CROP_PHOTO);
    }

    public static String getRealFilePathFromUri(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(hasUpdateUserData)
            setResult(RESULT_OK);
    }
}
