package com.gordy.ichanosdk.streamer.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.ichano.rvs.streamer.constant.LoginError;
import com.ichano.rvs.streamer.constant.LoginState;
import com.ichano.rvs.streamer.constant.RvsSessionState;
import com.ichano.rvs.streamer.ui.AvsInitHelper;

/**
 * Created by Administrator on 2016/9/9.
 */
public class MyAvsInitHelper extends AvsInitHelper {

    private static MyAvsInitHelper myAvsInitHelper;
    private static final String MY_COMPLANY_ID = "fc9326080caa4b46a30e272541c5dad4";
    private static final long MY_COMPLANY_KEY = 1473227260800L;
    private static final String MY_APP_ID = "111bd6ef121143e8aa99b64ff0617ace04";
    private static final String MY_LICENSE = "gy0001";
    private static final String DEFAULT_USER_NAME = "admin";
    private static final String DEFAULT_USER_PWD = "admin";

    private String[] mUserNameAndPwd;       //用户名称和密码
    private String mDeviceName;             //设备名称
    private boolean mLoginSuccess;          //登录成功
    private String mCID;                    //CID


    //构造函数
    public MyAvsInitHelper(Context applicationContext){
        super(applicationContext);
    }

    //获取实例
    public static MyAvsInitHelper getInstance(Context applicationContext){
        if(null == myAvsInitHelper){
            myAvsInitHelper = new MyAvsInitHelper(applicationContext);
        }
        return myAvsInitHelper;
    }

    //获取用户名称
    public String getUserName(){
        if(null == mUserNameAndPwd)
            return "";
        else
            return mUserNameAndPwd[0];
    }

    //获取用户密码
    public String getUserPwd(){
        if(null == mUserNameAndPwd)
            return "";
        else
            return mUserNameAndPwd[1];
    }

    //获取设备名称
    public String getDeviceName(){
        return mDeviceName;
    }

    //获取CID
    public String getCID(){
        return mCID;
    }

    //获取连接状态
    public String getStatus(){
        return mLoginSuccess?"已连接":"未连接";
    }

    //获取公司ID
    @Override
    public String getCompanyID() {
        return MY_COMPLANY_ID;
    }

    //获取公司密钥
    @Override
    public long getCompanyKey() {
        return MY_COMPLANY_KEY;
    }

    //获取应用程序编号
    @Override
    public String getAppID() {
        return MY_APP_ID;
    }

    //获取授权编号
    @Override
    public String getLicense() {
        return MY_LICENSE;
    }

    //获取最大观看端数量
    @Override
    public int getMaxSessionNum() {
        return 6;
    }

    //启用调试
    @Override
    public boolean enableDebug() {
        return true;
    }

    //修改设备名称
    @Override
    public void onDeviceNameChange(String s) {
        MyLog.i("DeviceName: "+s);
        this.mDeviceName = s;
    }

    //登录结果返回
    @Override
    public void onLoginResult(LoginState loginState, int i, LoginError loginError) {
        MyLog.i("LoginState: "+loginState + " , progressRate: "+ i + " , LoginError: "+loginError);
        if(LoginState.CONNECTED == loginState){
            mLoginSuccess = true;
            mCID = streamer.getCID();
            if(null == streamer.getUserNameAndPwd()){
                streamer.setUserNameAndPwd(DEFAULT_USER_NAME,DEFAULT_USER_PWD);
                mUserNameAndPwd = new String[]{DEFAULT_USER_NAME,DEFAULT_USER_PWD};
            }else{
                mUserNameAndPwd = streamer.getUserNameAndPwd();
            }
            mDeviceName = streamer.getDeviceName();
        }else if(LoginState.CONNECTING == loginState){
            //正在连接

            mLoginSuccess = false;
        }else if(LoginState.DISCONNECT == loginState){
            //连接失败
            mLoginSuccess = false;

        }

        Intent intent = new Intent("com.gordy.ichanosdk,streamer.loginresult");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public void onPushStateChange(boolean pushEnable) {
        super.onPushStateChange(pushEnable);
    }

    @Override
    public void onSessionStateChange(long l, RvsSessionState rvsSessionState) {

    }

    @Override
    public void onUpdateCID(long l) {
        this.mCID = String.valueOf(l);
    }

    @Override
    public void onUpdateUserName() {
        this.mUserNameAndPwd = streamer.getUserNameAndPwd();
    }



}
