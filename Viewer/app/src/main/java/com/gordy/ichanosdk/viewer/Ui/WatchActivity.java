package com.gordy.ichanosdk.viewer.Ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.io.FileInputStream;


public class WatchActivity extends BaseActivity {

    private TextView tv_title;
    private ImageView img_back;
    private GLMediaView mGLMediaView;           //视频播放
    private FrameLayout contentView;            //视频所在布局
    private RelativeLayout snapcontentView;
    private ImageView img_snap;                 //视频截图

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


    private int mediaViewWidth;
    private int mediaViewHeight;
    private TouchListener touchListener;

    private Animation animLarge,animSmall;

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
        snapcontentView = (RelativeLayout) findViewById(R.id.ly_snap);
        img_snap = (ImageView)findViewById(R.id.img_snap);

        animLarge = AnimationUtils.loadAnimation(this,R.anim.medialarge);
        animSmall = AnimationUtils.loadAnimation(this,R.anim.mediasmall);

        touchListener = new TouchListener();
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

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(loadingDialog.isShowing())
                loadingDialog.dismiss();
            switch(msg.what){
                case 1:
                    String picpath = msg.getData().getString("path");
                    if(!TextUtils.isEmpty(picpath)){
                        Bitmap bitmap = FileUtils.getLoacalBitmap(picpath);
                        img_snap.setImageBitmap(FileUtils.zoomImage(bitmap,mediaViewWidth,mediaViewHeight));
                        img_snap.setOnTouchListener(touchListener);
                        contentView.startAnimation(animSmall);
                        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) contentView.getLayoutParams();
                        lp.width = mediaViewWidth/3;
                        lp.height = mediaViewHeight/3;
                        contentView.setLayoutParams(lp);
                        snapcontentView.startAnimation(animLarge);
//                        FrameLayout.LayoutParams lp2 = (FrameLayout.LayoutParams) snapcontentView.getLayoutParams();
//                        lp2.width = lp2.MATCH_PARENT;
//                        lp2.height = mediaViewHeight;
//                        snapcontentView.setLayoutParams(lp2);
                        snapcontentView.setVisibility(View.VISIBLE);
                    }
                    break;
                default:
                    break;
            }
        }
    };


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


        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //判断图片是否可见
                if(snapcontentView.getVisibility() == View.VISIBLE){
                    snapcontentView.startAnimation(animSmall);
                    contentView.startAnimation(animLarge);
                    img_snap.setOnTouchListener(null);
                    BitmapDrawable drawable = (BitmapDrawable)img_snap.getDrawable();
                    Bitmap bmp = drawable.getBitmap();
                    if (null != bmp && !bmp.isRecycled()){
                        bmp.recycle();
                        bmp = null;
                    }
//                    FrameLayout.LayoutParams lp2 = (FrameLayout.LayoutParams) snapcontentView.getLayoutParams();
//                    lp2.width = 0;
//                    lp2.height = 0;
//                    snapcontentView.setLayoutParams(lp2);
                    snapcontentView.setVisibility(View.GONE);

                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) contentView.getLayoutParams();
                    lp.width = lp.MATCH_PARENT;
                    lp.height = mediaViewHeight;
                    contentView.setLayoutParams(lp);


                }else{



                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(FileUtils.hasSDCard()) {
                                String path = mCaptureImgPath + "/" + DateUtils.getTime() + Constants.IMG_JPG;
                                if (mGLMediaView.takeCapture(path)) {
                                    Message msg = new Message();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("path",path);
                                    msg.setData(bundle);
                                    msg.what = 1;
                                    handler.sendMessage(msg);
                                }
                            }
                        }
                    }).start();
                }
            }
        });
        mGLMediaView.bindCid(Long.valueOf(cid),DEFAULT_CAMERA_INDEX);
        mGLMediaView.soundOff();
        mGLMediaView.setOnLinkCameraStatusListener(new GLMediaView.LinkCameraStatusListener() {
            @Override
            public void startToLink() {
                loadingDialog.show();
            }

            @Override
            public void linkSucces() {
                mediaViewWidth = contentView.getMeasuredWidth();
                mediaViewHeight = contentView.getMeasuredHeight();
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


    private final class TouchListener implements View.OnTouchListener {

        /** 记录是拖拉照片模式还是放大缩小照片模式 */
        private int mode = 0;// 初始状态
        /** 拖拉照片模式 */
        private static final int MODE_DRAG = 1;
        /** 放大缩小照片模式 */
        private static final int MODE_ZOOM = 2;

        /** 用于记录开始时候的坐标位置 */
        private PointF startPoint = new PointF();
        /** 用于记录拖拉图片移动的坐标位置 */
        private Matrix matrix = new Matrix();
        /** 用于记录图片要进行拖拉时候的坐标位置 */
        private Matrix currentMatrix = new Matrix();

        /** 两个手指的开始距离 */
        private float startDis;
        /** 两个手指的中间点 */
        private PointF midPoint;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            /** 通过与运算保留最后八位 MotionEvent.ACTION_MASK = 255 */
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                // 手指压下屏幕
                case MotionEvent.ACTION_DOWN:
                    mode = MODE_DRAG;
                    // 记录ImageView当前的移动位置
                    currentMatrix.set(img_snap.getImageMatrix());
                    startPoint.set(event.getX(), event.getY());
                    break;
                // 手指在屏幕上移动，改事件会被不断触发
                case MotionEvent.ACTION_MOVE:
                    // 拖拉图片
                    if (mode == MODE_DRAG) {
                        float dx = event.getX() - startPoint.x; // 得到x轴的移动距离
                        float dy = event.getY() - startPoint.y; // 得到x轴的移动距离
                        // 在没有移动之前的位置上进行移动
                        matrix.set(currentMatrix);
                        matrix.postTranslate(dx, dy);
                    }
                    // 放大缩小图片
                    else if (mode == MODE_ZOOM) {
                        float endDis = distance(event);// 结束距离
                        if (endDis > 10f) { // 两个手指并拢在一起的时候像素大于10
                            float scale = endDis / startDis;// 得到缩放倍数
                            matrix.set(currentMatrix);
                            matrix.postScale(scale, scale,midPoint.x,midPoint.y);
                        }
                    }
                    break;
                // 手指离开屏幕
                case MotionEvent.ACTION_UP:
                    // 当触点离开屏幕，但是屏幕上还有触点(手指)
                case MotionEvent.ACTION_POINTER_UP:
                    mode = 0;
                    break;
                // 当屏幕上已经有触点(手指)，再有一个触点压下屏幕
                case MotionEvent.ACTION_POINTER_DOWN:
                    mode = MODE_ZOOM;
                    /** 计算两个手指间的距离 */
                    startDis = distance(event);
                    /** 计算两个手指间的中间点 */
                    if (startDis > 10f) { // 两个手指并拢在一起的时候像素大于10
                        midPoint = mid(event);
                        //记录当前ImageView的缩放倍数
                        currentMatrix.set(img_snap.getImageMatrix());
                    }
                    break;
            }
            img_snap.setImageMatrix(matrix);
            return true;
        }

        /** 计算两个手指间的距离 */
        private float distance(MotionEvent event) {
            float dx = event.getX(1) - event.getX(0);
            float dy = event.getY(1) - event.getY(0);
            /** 使用勾股定理返回两点之间的距离 */
            return  (float)Math.sqrt(dx * dx + dy * dy);
        }

        /** 计算两个手指间的中间点 */
        private PointF mid(MotionEvent event) {
            float midX = (event.getX(1) + event.getX(0)) / 2;
            float midY = (event.getY(1) + event.getY(0)) / 2;
            return new PointF(midX, midY);
        }

    }
}
