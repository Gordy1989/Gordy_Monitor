package com.gordy.ichanosdk.viewer.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.ichano.rvs.viewer.constant.LoginError;
import com.ichano.rvs.viewer.constant.LoginState;
import com.ichano.rvs.viewer.constant.RvsSessionState;
import com.ichano.rvs.viewer.constant.StreamerConfigState;
import com.ichano.rvs.viewer.constant.StreamerInfoType;
import com.ichano.rvs.viewer.constant.StreamerPresenceState;
import com.ichano.rvs.viewer.ui.ViewerInitHelper;

/**
 * Created by Administrator on 2016/9/12.
 */
public class MyViewerInitHelper extends ViewerInitHelper{

    private static MyViewerInitHelper myViewerInitHelper;
    private LoginListener mloginListener;

    public  static MyViewerInitHelper getInstance(Context applicationContext){
        if(null == myViewerInitHelper){
            myViewerInitHelper = new MyViewerInitHelper(applicationContext);
        }
        return myViewerInitHelper;
    }

    public MyViewerInitHelper(Context applicationContext){
        super(applicationContext);
    }


    @Override
    public String getAppID() {
        return "111bd6ef121143e8aa99b64ff0617ace14";
    }

    @Override
    public String getLicense() {
            return "";
    }

    @Override
    public long getCompanyKey() {
        return 1473227260800L;
    }

    @Override
    public String getCompanyID() {
        return "fc9326080caa4b46a30e272541c5dad4";
    }


    @Override
    public void onUpdateCID(long l) {

    }

    @Override
    public void onSessionStateChange(long l, RvsSessionState rvsSessionState) {
        Intent intent = new Intent(Constants.SESSIONS_STATE);
        intent.putExtra("streamerCid",l);
        if(RvsSessionState.CONNECTED == rvsSessionState){
            intent.putExtra("session",true);
        }else if(RvsSessionState.DISCONNECTED == rvsSessionState){
            intent.putExtra("session",false);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public void onLoginResult(LoginState loginState, int i, LoginError loginError) {
        if(LoginState.CONNECTED == loginState){
            if(null != mloginListener){
                mloginListener.onLoginResult(true);
            }
        }else if(LoginState.DISCONNECT == loginState){
            if(null != mloginListener){
                mloginListener.onLoginResult(false);
            }
        }
    }

    @Override
    public void onStreamerConfigState(long l, StreamerConfigState streamerConfigState) {

    }

    @Override
    public void onStreamerInfoUpdate(long l, StreamerInfoType streamerInfoType) {

    }

    @Override
    public void onStreamerPresenceState(long l, StreamerPresenceState streamerPresenceState) {
        Intent intent = new Intent(Constants.STREAMER_STATE);
        intent.putExtra("streamerCid",l);
        if(streamerPresenceState == StreamerPresenceState.ONLINE) {
            intent.putExtra("online", true);
            //Toast.makeText(context, "收到在线", Toast.LENGTH_SHORT).show();
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }else if(StreamerPresenceState.OFFLINE == streamerPresenceState){
            intent.putExtra("online", false);
            //Toast.makeText(context, "收到离线", Toast.LENGTH_SHORT).show();
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }

    }

    public interface LoginListener{
        public void onLoginResult(boolean success);
    }

    public void setLoginListener(LoginListener loginListener){
        mloginListener = loginListener;
    }

    public LoginListener getLoginListener(){
        return mloginListener;
    }

}
