package com.gordy.ichanosdk.viewer.Ui;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.gordy.ichanosdk.viewer.R;
import com.gordy.ichanosdk.viewer.System.ActivityController;
import com.gordy.ichanosdk.viewer.System.BaseActivity;
import com.gordy.ichanosdk.viewer.System.StreamAdapter;
import com.gordy.ichanosdk.viewer.System.StreamInfo;
import com.gordy.ichanosdk.viewer.Utils.Constants;
import com.gordy.ichanosdk.viewer.Utils.MyViewerInitHelper;
import com.ichano.rvs.viewer.Viewer;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private TextView tv_Title;
    private ImageView img_Load;
    private ImageView img_AddDevices;
    private TextView tv_MyDevicesNum;
    private TextView tv_OnlineNum;
    private LinearLayout ly_Title;

    private Animation loadingAnim;
    private CustomDialog exitDialog;
    private CustomDialog loginDialog;
    private CustomDialog addCidDialog;
    private CustomDialog deleteDialog;

    private AddDevicesPopupWindow addDevicesPopupWindow;
    private MyViewerInitHelper myViewerInitHelper;

    private Handler initHandler;
    private SharedPreferences cidPref;
    private ListView lv_streamer;
    private StreamAdapter streamAdapter;
    private LocalBroadcastManager localBroadcastManager;
    private Viewer mViewer;
    private List<StreamInfo> streamInfoList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cidPref = getSharedPreferences("CID",MODE_PRIVATE);

        tv_Title = (TextView)findViewById(R.id.tv_Title);
        tv_MyDevicesNum = (TextView)findViewById(R.id.tv_MyDevicesNum);
        tv_OnlineNum = (TextView)findViewById(R.id.tv_OnlineNum);
        img_AddDevices = (ImageView)findViewById(R.id.img_AddDevices);
        img_Load = (ImageView)findViewById(R.id.img_Load);
        ly_Title = (LinearLayout)findViewById(R.id.ly_Title);
        lv_streamer = (ListView)findViewById(R.id.lv_streamer);

        img_Load.setVisibility(View.GONE);

        localBroadcastManager = LocalBroadcastManager.getInstance(MainActivity.this);
        localBroadcastManager.registerReceiver(streamerStateReceiver,new IntentFilter(Constants.STREAMER_STATE));
        localBroadcastManager.registerReceiver(sessionStateReceiver,new IntentFilter(Constants.SESSIONS_STATE));

        loadingAnim = AnimationUtils.loadAnimation(this,R.anim.tips);
        loadingAnim.setInterpolator(new LinearInterpolator());

        initHandler = new Handler();
        mViewer = Viewer.getViewer();
        streamInfoList = new ArrayList<StreamInfo>();

        img_AddDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null == addDevicesPopupWindow){
                    addDevicesPopupWindow = new AddDevicesPopupWindow(MainActivity.this,addDevicesItemOnClick);
                    addDevicesPopupWindow.setOnDismissListener(dismissListener);
                }

                if(!addDevicesPopupWindow.isShowing()){
                    int[] position = new int[2];
                    ly_Title.findViewById(R.id.ly_Title).getLocationOnScreen(position);
                    int y = position[1] + ly_Title.getMeasuredHeight();
                    addDevicesPopupWindow.showAtLocation(ly_Title, Gravity.NO_GRAVITY,0,y);
                    img_AddDevices.setImageResource(R.mipmap.close);
                }
            }
        });


        lv_streamer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                StreamInfo streamInfo = streamAdapter.getItem(i);
                if(!streamInfo.getStatus().equals("在线")){
                    Toast.makeText(MainActivity.this, "当前设备不在线不能进行观看！", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!streamInfo.getStatus_con().equals("已连通")){
                    Toast.makeText(MainActivity.this, "当前设备尚未连通不能进行观看！", Toast.LENGTH_SHORT).show();
                    return;
                }
                WatchActivity.startWatchActivity(MainActivity.this,streamInfo.getCid());
            }
        });
        lv_streamer.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(null != deleteDialog){
                    deleteDialog.show();
                }else{
                    final int _position = i;
                    deleteDialog = new CustomDialog.Builder(MainActivity.this)
                            .setTitle("提示")
                            .setMessage("您确定要删除吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    StreamInfo streamInfo = streamAdapter.getItem(_position);
                                    mViewer.disconnectStreamer(Long.valueOf(streamInfo.getCid()));
                                    cidPref.edit().clear().commit();
                                    streamAdapter.remove(streamInfo);
                                    streamAdapter.notifyDataSetChanged();
                                    tv_MyDevicesNum.setText(String.valueOf(streamAdapter.getCount()));
                                    tv_OnlineNum.setText(String.valueOf(streamAdapter.getOnlineNum()));
                                    deleteDialog.dismiss();
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    deleteDialog.dismiss();
                                }
                            })
                            .create();
                    deleteDialog.show();
                }

                return true;
            }
        });

        initHandler.post(initRunnable);
    }

    private Runnable initRunnable = new Runnable() {
        @Override
        public void run() {
            img_Load.setVisibility(View.VISIBLE);
            if(null != loadingAnim){
                img_Load.startAnimation(loadingAnim);
            }
            tv_Title.setText("连接中...");

            //初始化SDK
            if(null == myViewerInitHelper){
                try{
                    myViewerInitHelper = MyViewerInitHelper.getInstance(MainActivity.this);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            if(null == myViewerInitHelper.getLoginListener())
                myViewerInitHelper.setLoginListener(loginListener);
            myViewerInitHelper.login();
        }
    };

    private View.OnClickListener addDevicesItemOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            addDevicesPopupWindow.dismiss();
            switch(view.getId()){
                case R.id.btn_Manual:
                    if(null != addCidDialog){
                        addCidDialog.show();
                    }else{
                        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                        final View contentview =layoutInflater.inflate(R.layout.layout_cid_manual,null);
                        addCidDialog = new CustomDialog.Builder(MainActivity.this)
                                .setTitle("手动添加")
                                .setContentView(contentview)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        EditText[] editTexts = new EditText[3];
                                        editTexts[0] = (EditText)contentview.findViewById(R.id.edt_cid);
                                        editTexts[1] = (EditText)contentview.findViewById(R.id.edt_username);
                                        editTexts[2] = (EditText)contentview.findViewById(R.id.edt_password);
                                        //检测是否都填写
                                        for(EditText editText:editTexts){
                                            if(TextUtils.isEmpty(editText.getText().toString())){
                                                Toast.makeText(MainActivity.this,editText.getHint(),Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                        }
                                        if(!mViewer.connectStreamer(Long.valueOf(editTexts[0].getText().toString()),
                                                editTexts[1].getText().toString(),
                                                editTexts[2].getText().toString())){
                                            Toast.makeText(MainActivity.this,"连接采集端失败！",Toast.LENGTH_SHORT).show();
                                        }else{
                                            //添加客户端
                                            StreamInfo streamInfo = new StreamInfo(editTexts[0].getText().toString(),
                                                    editTexts[1].getText().toString(),
                                                    editTexts[2].getText().toString());
                                            streamInfo.setDeviceName(mViewer.getStreamerInfoMgr()
                                                    .getStreamerInfo(Long.valueOf(streamInfo.getCid())).getDeviceName());
                                            cidPref.edit().putString("cid",editTexts[0].getText().toString())
                                                    .putString("username",editTexts[1].getText().toString())
                                                    .putString("password",editTexts[2].getText().toString())
                                                    .putString("devicename",streamInfo.getDeviceName()).commit();
                                            streamInfo.setIndex(streamAdapter.getCount());
                                            streamAdapter.add(streamInfo);
                                            streamAdapter.notifyDataSetChanged();
                                            tv_MyDevicesNum.setText(String.valueOf(streamAdapter.getCount()));
                                            addCidDialog.dismiss();
                                        }
                                    }
                                })
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        addCidDialog.dismiss();
                                    }
                                })
                                .create();
                        addCidDialog.show();

                    }
                    break;
                case R.id.btn_QRCode:
                    Toast.makeText(MainActivity.this, "btn_QRCode", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.btn_lan:
                    Toast.makeText(MainActivity.this, "btn_lan", Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    };

    private MyViewerInitHelper.LoginListener loginListener = new MyViewerInitHelper.LoginListener() {
        @Override
        public void onLoginResult(boolean success) {
            //停止动画
            if(null != loadingAnim){
                img_Load.clearAnimation();
            }
            if(success){
                img_Load.setVisibility(View.GONE);
                tv_Title.setText("Gordy观看端");
                myViewerInitHelper.setLoginListener(null);

                //加载设备
                if(cidPref.contains("cid")){
                    StreamInfo streamInfo = new StreamInfo(cidPref.getString("cid",""));
                    streamInfo.setUserName(cidPref.getString("username",""));
                    streamInfo.setPassword(cidPref.getString("password",""));
                    streamInfo.setDeviceName(cidPref.getString("devicename",""));
                    streamInfo.setStatus("未知");
                    streamInfo.setIndex(streamInfoList.size());
                    streamInfoList.add(streamInfo);
                    if(!mViewer.connectStreamer(Long.valueOf(streamInfo.getCid()),streamInfo.getUserName(),streamInfo.getPassword())){
                        Toast.makeText(MainActivity.this,"连接采集端失败！",Toast.LENGTH_SHORT).show();
                    }
                }
                streamAdapter = new StreamAdapter(MainActivity.this,R.layout.item_streamer,streamInfoList);
                tv_MyDevicesNum.setText(String.valueOf(streamAdapter.getCount()));
                lv_streamer.setAdapter(streamAdapter);

            }else{
                tv_Title.setText("Gordy观看端");
                if(null == loginDialog){
                    loginDialog = new CustomDialog.Builder(MainActivity.this)
                            .setTitle("提示")
                            .setMessage("SDK初始化失败！")
                            .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    loginDialog.dismiss();
                                    finish();
                                }
                            })
                            .setNegativeButton("重试", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    initHandler.post(initRunnable);
                                }
                            })
                            .create();
                    loginDialog.setCancelable(false);
                    loginDialog.show();
                }else{
                    loginDialog.show();
                }
            }
        }
    };

    private PopupWindow.OnDismissListener dismissListener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            img_AddDevices.setImageResource(R.mipmap.adddevices);
        }
    };

    private BroadcastReceiver streamerStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(null != intent){

                long streamcid = intent.getLongExtra("streamerCid",-1);
                boolean online = intent.getBooleanExtra("online",false);
                boolean isChange = false;
                for(StreamInfo streamInfo :streamInfoList){
                    if(streamInfo.getCid().equals(String.valueOf(streamcid))){
                        if(TextUtils.isEmpty(streamInfo.getDeviceName())){
                            streamInfo.setDeviceName(mViewer
                                    .getStreamerInfoMgr()
                                    .getStreamerInfo(Long.valueOf(streamInfo.getCid())).getDeviceName());
                            ((TextView)((LinearLayout)lv_streamer.getChildAt(streamInfo.getIndex())).findViewById(R.id.tv_devicename)).setText(streamInfo.getDeviceName());
                            cidPref.edit().putString("devicename",streamInfo.getDeviceName());
                            streamAdapter.notifyDataSetChanged();
                        }
                        if(!TextUtils.isEmpty(streamInfo.getStatus())
                                && streamInfo.getStatus().equals(online?"在线":"离线")){
                            return;
                        }
                        LinearLayout layout = (LinearLayout)lv_streamer.getChildAt(streamInfo.getIndex());
                        TextView textView = (TextView)layout.findViewById(R.id.tv_status);
                        if(online){
                            streamInfo.setStatus("在线");
                            textView.setText("在线");
                            textView.setTextColor(Color.GREEN);
                        }else{
                            streamInfo.setStatus("离线");
                            textView.setText("离线");
                            textView.setTextColor(Color.RED);
                        }
                        isChange = true;
                        break;
                    }
                }
                if(isChange){
                    streamAdapter.notifyDataSetChanged();
                    tv_OnlineNum.setText(String.valueOf(streamAdapter.getOnlineNum()));
                }
            }
        }
    };

    private BroadcastReceiver sessionStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long streamcid = intent.getLongExtra("streamerCid",-1);
            boolean session = intent.getBooleanExtra("session",false);
            boolean isChange = false;
            for(StreamInfo streamInfo :streamInfoList){
                if(streamInfo.getCid().equals(String.valueOf(streamcid))){
                    if(!TextUtils.isEmpty(streamInfo.getStatus_con()) && streamInfo.getStatus_con().equals(session?"已连通":"未连通")){
                        return;
                    }
                    LinearLayout layout = (LinearLayout)lv_streamer.getChildAt(streamInfo.getIndex());
                    TextView textView = (TextView)layout.findViewById(R.id.tv_status_con);
                    if(session){
                        streamInfo.setStatus_con("已连通");
                        textView.setText("已连通");
                        textView.setTextColor(Color.GREEN);
                    }else{
                        streamInfo.setStatus_con("未连通");
                        textView.setText("未连通");
                        textView.setTextColor(Color.RED);
                    }
                    isChange = true;
                    break;
                }
            }
            if(isChange){
                streamAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(null != img_Load.getAnimation()){
            img_Load.clearAnimation();
        }

        if(null != myViewerInitHelper){
            myViewerInitHelper.logout();
        }

        initHandler.removeCallbacks(initRunnable);

        localBroadcastManager.unregisterReceiver(streamerStateReceiver);
        localBroadcastManager.unregisterReceiver(sessionStateReceiver);
    }

    @Override
    public void onBackPressed() {
        if(null != exitDialog){
            exitDialog.show();
        }else{
            exitDialog = new CustomDialog.Builder(MainActivity.this)
                    .setTitle("提示")
                    .setMessage("您是否要退出观看端？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            exitDialog.dismiss();
                            ActivityController.exitApplication();
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
}
