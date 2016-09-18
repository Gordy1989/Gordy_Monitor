package com.gordy.ichanosdk.viewer.Utils;

import android.os.Environment;

import java.io.File;

/**
 * Created by Administrator on 2016/9/14.
 */
public class FileUtils {

    public static boolean hasSDCard() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)){
            return true;
        }else{
            return false;
        }
    }

    public static File mkdirsOnSDCard(String pathName){
        File file = new File(Environment.getExternalStorageDirectory(), pathName);
        if (!file.exists()){
            file.mkdirs();
        }
        return file;
    }
}
