package com.gordy.ichanosdk.viewer.Ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.gordy.ichanosdk.viewer.R;
import com.gordy.ichanosdk.viewer.System.BaseActivity;
import com.gordy.ichanosdk.viewer.Utils.Constants;
import com.gordy.ichanosdk.viewer.Utils.DateUtils;
import com.gordy.ichanosdk.viewer.Utils.FileUtils;
import com.ichano.rvs.viewer.Viewer;
import com.ichano.rvs.viewer.ui.AbstractMediaView;
import com.ichano.rvs.viewer.ui.GLMediaView;

import java.io.File;


public class WatchActivity extends BaseActivity {

    private TextView tv_title;
    private ImageView img_back;
    private GLMediaView mGLMediaView;
    private FrameLayout contentView;

    private String cid;
    private String mRecordVideoPath;        //录像路径
    private String mCaptureImgPath;         //截图路径

    private CustomProgressDialog loadingDialog;
    private CustomDialog failedDialog;
    private CustomDialog exitDialog;

    private Handler mHandler = new Handler();

    private static final int DEFAULT_CAMERA_INDEX = 0;
    private static final String TIME_UP_ERROR = "TIME_UP";

    private ToggleButton btn_sound;
    private ToggleButton btn_talk;
    private ToggleButton btn_record;
    private Button btn_capture;
    private Button btn_cmd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);

        tv_title = (TextView)findViewById(R.id.watch_tv_title);
        img_back = (ImageView)findViewById(R.id.watch_img_back);
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mGLMediaView = (GLMediaView)findViewById(R.id.watch_media);
        contentView = (FrameLayout)findViewById(R.id.surface_contain);


        loadingDialog = new CustomProgressDialog(WatchActivity.this,"正在加载，请稍后...");
        loadingDialog.setCancelable(false);

        init();

        btn_sound = (ToggleButton)findViewById(R.id.watch_btn_sound);
        btn_sound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    mGLMediaView.soundOn();
                }else{
                    mGLMediaView.soundOff();
                }
            }
        });

        btn_talk = (ToggleButton)findViewById(R.id.watch_btn_talk);
        btn_talk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    if(!mGLMediaView.isSendRevAudio()){
                        mGLMediaView.startSendRevAudio();
                        mGLMediaView.soundOff();
                        btn_sound.setChecked(false);
                        btn_sound.setClickable(false);
                    }
                }else{
                    if(mGLMediaView.isSendRevAudio()){
                        mGLMediaView.stopSendRevAudio();
                        btn_sound.setClickable(true);
                    }
                }
            }
        });

        btn_record = (ToggleButton)findViewById(R.id.watch_btn_record);
        btn_record.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    if(!mGLMediaView.isRecordingVideo()){
                        if(FileUtils.hasSDCard()){
                            String path = mRecordVideoPath + "/" + DateUtils.getTime() + Constants.VIDEO_MP4;
                            mGLMediaView.startRecordVideo(path);
                        }
                    }
                }else{
                    if(mGLMediaView.isRecordingVideo()){
                        if(mGLMediaView.stopRecordVideo()){
                            Toast.makeText(WatchActivity.this, "视频文件保存在："+ mRecordVideoPath, Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(WatchActivity.this, "视频文件保存失败！", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });

        btn_capture = (Button)findViewById(R.id.watch_btn_capture);
        btn_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(FileUtils.hasSDCard()) {
                    String path = mCaptureImgPath + "/" + DateUtils.getTime() + Constants.IMG_JPG;
                    if (mGLMediaView.takeCapture(path)) {
                        Toast.makeText(WatchActivity.this, "图片文件保存在："+mCaptureImgPath, Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(WatchActivity.this, "图片文件保存失败！", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btn_cmd = (Button)findViewById(R.id.watch_btn_cmd);
        btn_cmd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Viewer.getViewer().getCommand().sendCustomCommand(Long.valueOf(cid),2001,"gordy command!");
                Toast.makeText(WatchActivity.this, "命令发送成功！", Toast.LENGTH_SHORT).show();
            }
        });

    }


    //启动界面
    public static void startWatchActivity(Context context, String cid){
        Intent intent = new Intent(context,WatchActivity.class);
        intent.putExtra("cid",cid);
        context.startActivity(intent);
    }

    private void init(){
        cid = getIntent().getExtras().getString("cid");
        tv_title.setText("CID:" + cid);

        mRecordVideoPath = FileUtils.mkdirsOnSDCard(Constants.RECORD_VIDEO_PATH).getAbsolutePath();
        mCaptureImgPath = FileUtils.mkdirsOnSDCard(Constants.CAPTURE_IAMGE_PATH).getAbsolutePath();

        mGLMediaView.bindCid(Long.valueOf(cid),DEFAULT_CAMERA_INDEX);
        mGLMediaView.soundOff();
        mGLMediaView.setOnLinkCameraStatusListener(new GLMediaView.LinkCameraStatusListener() {
            @Override
            public void startToLink() {
                loadingDialog.show();
            }

            @Override
            public void linkSucces() {
                loadingDialog.dismiss();
            }

            @Override
            public void linkFailed(AbstractMediaView.LinkCameraError linkCameraError) {

                if(TIME_UP_ERROR.equals(linkCameraError)){
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(WatchActivity.this, "只能收看2分钟！", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            loadingDialog.dismiss();
                            showFailedDialog();
                        }
                    });
                }


            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        if(contentView.getChildCount()==0&&mGLMediaView==null){
            mGLMediaView = new GLMediaView(WatchActivity.this);
            contentView.addView(mGLMediaView);
            mGLMediaView.bindCid(Long.valueOf(cid),DEFAULT_CAMERA_INDEX);
            mGLMediaView.soundOff();
            mGLMediaView.setOnLinkCameraStatusListener(new GLMediaView.LinkCameraStatusListener() {
                @Override
                public void startToLink() {
                    loadingDialog.show();
                }

                @Override
                public void linkSucces() {
                    loadingDialog.dismiss();
                }

                @Override
                public void linkFailed(AbstractMediaView.LinkCameraError linkCameraError) {

                    if(TIME_UP_ERROR.equals(linkCameraError)){
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(WatchActivity.this, "只能收看2分钟！", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else{
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                loadingDialog.dismiss();
                                showFailedDialog();
                            }
                        });
                    }
                }
            });

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(contentView.getChildCount()>0){
            contentView.removeView(mGLMediaView);
            mGLMediaView = null;
        }
    }

    @Override
    public void onBackPressed() {
        if(null != exitDialog){
            exitDialog.show();
        }else{
            exitDialog = new CustomDialog.Builder(WatchActivity.this)
                    .setTitle("提示")
                    .setMessage("您确定要退出视频观看？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            exitDialog.dismiss();
                            WatchActivity.this.finish();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            exitDialog.dismiss();
                        }
                    })
                    .create();
            exitDialog.show();
        }
    }

    private void showFailedDialog(){
        if(null != failedDialog){
            failedDialog.show();
        }else{
            failedDialog = new CustomDialog.Builder(WatchActivity.this)
                    .setTitle("提示")
                    .setMessage("连接失败！")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            failedDialog.dismiss();
                            WatchActivity.this.finish();
                        }
                    })
                    .create();
            failedDialog.setCancelable(false);
            failedDialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
