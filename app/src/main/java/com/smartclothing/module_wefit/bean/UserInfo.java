package com.smartclothing.module_wefit.bean;

import java.io.Serializable;

/**
 * Created by ZZP on 2018/5/20.
 */

public class UserInfo implements Serializable{
    /*{"userImg":null,"userName":"哈哈哈哈","sex":1,"birthday":631123200000,"height":170,"targetWeight":50,"phone":"133*****8789"}*/
    private String birthday;
    private String height;
    private String phone;
    private String sex;
    private String targetWeight;
    private String userImg;
    private String userName;

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    private String sign;

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getTargetWeight() {
        return targetWeight;
    }

    public void setTargetWeight(String targetWeight) {
        this.targetWeight = targetWeight;
    }

    public String getUserImg() {
        return userImg;
    }

    public void setUserImg(String userImg) {
        this.userImg = userImg;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
