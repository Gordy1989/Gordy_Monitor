package com.gordy.ichanosdk.streamer.Ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gordy.ichanosdk.streamer.R;
import com.gordy.ichanosdk.streamer.System.ActivityController;
import com.gordy.ichanosdk.streamer.System.BaseActivity;
import com.gordy.ichanosdk.streamer.System.StreamerApplication;
import com.gordy.ichanosdk.streamer.Utils.MyAvsInitHelper;
import com.ichano.rvs.streamer.Streamer;
import com.ichano.rvs.streamer.callback.CustomCommandListener;
import com.ichano.rvs.streamer.ui.MediaSurfaceView;

public class CameraActivity extends BaseActivity {

    private MyAvsInitHelper myAvsInitHelper;
    private CustomDialog alertDialog;


    private TextView tv_CID;
    private TextView tv_UserName;
    private TextView tv_UserPwd;
    private TextView tv_Status;
    private TextView tv_DeviceName;
    private MediaSurfaceView mMediaSurfaceView;

    private LocalBroadcastManager localBroadcastManager;
    private LoginResultReciever loginResultReciever;
    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        tv_CID = (TextView)findViewById(R.id.tv_CID);
        tv_UserName = (TextView)findViewById(R.id.tv_UserName);
        tv_UserPwd = (TextView)findViewById(R.id.tv_UserPwd);
        tv_Status = (TextView)findViewById(R.id.tv_Status);
        tv_DeviceName = (TextView)findViewById(R.id.tv_Device);
        mMediaSurfaceView = (MediaSurfaceView)findViewById(R.id.mv_Camera);

        //注册本地广播接收器
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter("com.gordy.ichanosdk,streamer.loginresult");
        loginResultReciever = new LoginResultReciever();
        localBroadcastManager.registerReceiver(loginResultReciever,intentFilter);

        //初始化SDK
        myAvsInitHelper = MyAvsInitHelper.getInstance(this);
        myAvsInitHelper.login();

        //打开摄像头
        mMediaSurfaceView.openCamera(Configuration.ORIENTATION_LANDSCAPE);
        final int[] size = mMediaSurfaceView.getVideoSize();

        ViewTreeObserver viewTreeObserver = mMediaSurfaceView.getViewTreeObserver();
        viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if(isFirst){
                    int height = mMediaSurfaceView.getMeasuredHeight();
                    int width = mMediaSurfaceView.getMeasuredWidth();
                    float r = (float)height/(float)width;
                    float r2 = (float)size[1]/(float)size[0];
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)mMediaSurfaceView.getLayoutParams();
                    if(r > r2){
                        layoutParams.height = (int)(width*r2);
                    }else{
                        layoutParams.width = (int)(height/r2);
                    }

                    isFirst = false;
                }
                return true;
            }
        });

        Streamer.getStreamer().getCommand().setOnCustomCommandListener(new CustomCommandListener() {
            @Override
            public void onCustomCommandListener(long l, int i, String s) {
                Toast.makeText(CameraActivity.this, "CID："+String.valueOf(l) + " 命令编号："+String.valueOf(i) + " 命令内容："+s, Toast.LENGTH_SHORT).show();
            }
        });

    }

    //登录结果
    class LoginResultReciever extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            tv_CID.setText(myAvsInitHelper.getCID());
            tv_UserName.setText(myAvsInitHelper.getUserName());
            tv_UserPwd.setText(myAvsInitHelper.getUserPwd());
            tv_Status.setText(myAvsInitHelper.getStatus());
            tv_DeviceName.setText(myAvsInitHelper.getDeviceName());

        }
    }


    @Override
    public void onBackPressed() {
        if(null == alertDialog){
            alertDialog = new CustomDialog.Builder(CameraActivity.this)
                    .setTitle("提示")
                    .setMessage("您是否要退出采集端？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            alertDialog.dismiss();
                            ActivityController.exitApplication();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            alertDialog.dismiss();
                        }
                    })
                    .create();
            alertDialog.show();
        }else{
            alertDialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(loginResultReciever);
        //关闭摄像头
        mMediaSurfaceView.closeCamera();
        //注销
        myAvsInitHelper.logout();
    }
}
