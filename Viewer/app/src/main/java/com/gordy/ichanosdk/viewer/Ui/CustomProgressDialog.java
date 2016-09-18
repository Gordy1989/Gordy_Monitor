package com.gordy.ichanosdk.viewer.Ui;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

import com.gordy.ichanosdk.viewer.R;

/**
 * Created by Administrator on 2016/9/14.
 */
public class CustomProgressDialog extends Dialog {

    public CustomProgressDialog(Context context, String message){
        this(context, R.style.Dialog,message);
    }

    public CustomProgressDialog(Context context,int theme,String message){
        super(context,R.style.Dialog);
        this.setContentView(R.layout.processdialog_custom);
        TextView textView = (TextView)findViewById(R.id.pdlg_tv_msg);
        if(null != textView && !TextUtils.isEmpty(message)){
            textView.setText(message);
        }
    }
}
