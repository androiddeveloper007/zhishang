package lab.dxythch.com.netlib.net;


import java.util.List;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * 项目名称：Bracelet
 * 类描述：
 * 创建人：Jack
 * 创建时间：2017/5/24
 */
public interface RetrofitService {
    String BASE_URL = ServiceAPI.BASE_URL;


    ///////////////////////////////////////////////////////////////////////////
    // 瘦身（热量）
    ///////////////////////////////////////////////////////////////////////////

    @POST("slim/getSlimHistoryInfo")
    Observable<String> getHeatHistory(@Body RequestBody body);

    @POST("slim/getFoodInfo")
    Observable<String> getFoodInfo(@Body RequestBody body);


    @POST("slim/searchFoodInfo")
    Observable<String> searchFoodInfo(@Body RequestBody body);

    @POST("slim/getKeyWord")
    Observable<String> getKeyWord(@Body RequestBody body);

    @POST("slim/addHeatInfo")
    Observable<String> addHeatInfo(@Body RequestBody body);

    @POST("slim/removeHeatInfo")
    Observable<String> removeHeatInfo(@Body RequestBody body);


    ///////////////////////////////////////////////////////////////////////////
    // 体重
    ///////////////////////////////////////////////////////////////////////////

    @POST("slim/getWeightInfo")
    Observable<String> getWeightInfo(@Body RequestBody body);

    @POST("slim/getWeightList")
    Observable<String> getWeightList(@Body RequestBody body);

    @POST("slim/addWeightInfo")
    Observable<String> addWeightInfo(@Body RequestBody body);


    ///////////////////////////////////////////////////////////////////////////
    // 运动
    ///////////////////////////////////////////////////////////////////////////


    @POST("slim/getAthleticsInfo")
    Observable<String> getAthleticsInfo(@Body RequestBody body);

    @POST("slim/getAthleticsList")
    Observable<String> getAthleticsList(@Body RequestBody body);

    @POST("slim/addAthleticsInfo")
    Observable<String> addAthleticsInfo(@Body RequestBody body);

    /*我的模块：获取用户信息*/
    @Headers({
            "userId:c82e9e7612a447358c2a82ef437f3d11"
    })
    @POST("/user/userCenter")
    Observable<String> userCenter(@Body RequestBody body);


    /*获取用户个人资料*/
    @Headers({
            "userId:c82e9e7612a447358c2a82ef437f3d11"
    })
    @POST("/user/userInfo")
    Observable<String> userInfo(@Body RequestBody body);

    /*保存用户信息*/
    @Headers({
            "userId:c82e9e7612a447358c2a82ef437f3d11"
    })
    @POST("/user/saveUserInfo")
    Observable<String> saveUserInfo(@Body RequestBody body);

    /*上传用户头像*/
    @Headers({
            "userId:c82e9e7612a447358c2a82ef437f3d11"
    })
    @Multipart
    @POST("/user/uploadUserImg")
    Observable<String> uploadUserImg(@Part MultipartBody.Part file);

    /*我的收藏*/
    @FormUrlEncoded
    @POST("/user/collectionList")
    Observable<String> collectionList(@Field("userId") String userId, @Field("pageNum") int pageNum, @Field("pageSize") int pageSize);

    /*消息*/
    @FormUrlEncoded
    @POST("/user/message")
    Observable<String> message(@Field("userId") String userId, @Field("msgType") int msgType, @Field("pageNum") int pageNum, @Field("pageSize") int pageSize);

    /*查看并更新已读*/
    @FormUrlEncoded
    @POST("/user/readed")
    Observable<String> readed(@Field("userId") String userId, @Field("gid") String gid);

    /*全部更新已读*/
    @FormUrlEncoded
    @POST("/user/readedAll")
    Observable<String> readedAll(@Field("userId") String userId, @Field("msgType") int msgType);

    /*获取用户绑定设备信息列表*/
    @FormUrlEncoded
    @POST("/user/deviceList")
    Observable<String> deviceList(@Field("userId") String userId);

    /*问题反馈*/
    @Headers({
            "userId:c82e9e7612a447358c2a82ef437f3d11"
    })
    @POST("/user/feedback")
    Observable<String> feedback(@Body RequestBody body);

    /*问题反馈上传用图像*/
    @Headers({
            "userId:c82e9e7612a447358c2a82ef437f3d11"
    })
    @Multipart
    @POST("/user/feedbackImg")
    Observable<String> feedbackImg(@Part() List<MultipartBody.Part> parts);

    /*删除指定id的设备*/
    @FormUrlEncoded
    @POST("/user/removeBind")
    Observable<String> removeBind(@Field("userId") String userId, @Field("gid") String git);

    /*删除指定id的收藏*/
    @FormUrlEncoded
    @POST("/user/removeCollection")
    Observable<String> removeCollection(@Field("userId") String userId, @Field("gid") String git);

    /*退出登录*/
    @POST("/login/logout")
    Observable<String> logout();
}
