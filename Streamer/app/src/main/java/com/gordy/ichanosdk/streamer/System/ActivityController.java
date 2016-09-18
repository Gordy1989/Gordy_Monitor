package com.gordy.ichanosdk.streamer.System;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/12.
 */
public class ActivityController {

    private static List<Activity> activityList = new ArrayList<Activity>();

    public static void addActivity(Activity activity){
        if(null != activity){
            activityList.add(activity);
        }
    }

    public static void removeActivity(Activity activity){
        if(null != activity){
            activityList.remove(activity);
        }
    }

    public static void exitApplication(){
        for(Activity activity: activityList) {
            if(!activity.isFinishing())
                activity.finish();
        }
    }
}
