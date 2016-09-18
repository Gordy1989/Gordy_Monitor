package com.gordy.ichanosdk.viewer.System;

import android.app.Application;

/**
 * Created by Administrator on 2016/9/9.
 */
public class ViewerApplication extends Application{

    private static ViewerApplication viewerApplication;
    private static boolean isPrintLog;


    public static ViewerApplication getInstance(){
        if(null == viewerApplication){
            viewerApplication = new ViewerApplication();
        }
        return viewerApplication;
    }

    public static boolean getLogSwitch(){
        return isPrintLog;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        isPrintLog = true;
    }
}
