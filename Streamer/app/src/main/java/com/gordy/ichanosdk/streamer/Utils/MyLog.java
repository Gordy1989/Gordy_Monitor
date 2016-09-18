package com.gordy.ichanosdk.streamer.Utils;

import android.util.Log;

import com.gordy.ichanosdk.streamer.System.StreamerApplication;

/**
 * Created by Administrator on 2016/9/9.
 */
public class MyLog {

    private static final String TAG = "MyStreamer";       //日志标签

    //普通日志
    public static void i(String logMsg){
        if(!StreamerApplication.getLogSwitch()){
            return;
        }
        Log.i(TAG,logMsg);
    }

    //调试日志
    public static void d(String logMsg){
        if(!StreamerApplication.getLogSwitch()){
            return;
        }
        Log.d(TAG,logMsg);
    }

    //错误日志
    public static void e(String logMsg){
        if(!StreamerApplication.getLogSwitch()){
            return;
        }
        Log.e(TAG,logMsg);
    }

}
