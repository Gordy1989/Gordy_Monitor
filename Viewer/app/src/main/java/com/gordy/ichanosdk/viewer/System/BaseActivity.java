package com.gordy.ichanosdk.viewer.System;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;

/**
 * Created by Administrator on 2016/9/12.
 */
public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityController.addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ActivityController.removeActivity(this);
    }
}
