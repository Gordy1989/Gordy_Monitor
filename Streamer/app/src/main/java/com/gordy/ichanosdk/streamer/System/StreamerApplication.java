package com.gordy.ichanosdk.streamer.System;

import android.app.Application;

import com.gordy.ichanosdk.streamer.Utils.MyAvsInitHelper;

/**
 * Created by Gordy on 2016/9/9.
 */
public class StreamerApplication extends Application {

    private static StreamerApplication streamerApplication;     //程序
    private static boolean isPrintLog;                          //日志打印开关



    //获取应用程序实例
    public static StreamerApplication getInstance(){
        if(null == streamerApplication){
            streamerApplication = new StreamerApplication();
        }
        return streamerApplication;
    }


    //获取日志开关
    public static boolean getLogSwitch(){
        return isPrintLog;
    }

    //程序启动
    @Override
    public void onCreate() {
        super.onCreate();
        isPrintLog = true;          //默认开启日志

    }


}
