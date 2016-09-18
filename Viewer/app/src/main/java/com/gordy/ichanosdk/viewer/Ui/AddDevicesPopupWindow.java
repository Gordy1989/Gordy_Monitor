package com.gordy.ichanosdk.viewer.Ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.view.menu.MenuView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.gordy.ichanosdk.viewer.R;

/**
 * Created by Administrator on 2016/9/12.
 */
public class AddDevicesPopupWindow extends PopupWindow {

    private View mMenuView;
    private Button btn_Manual;
    private Button btn_QRCode;
    private Button btn_Lan;

    public AddDevicesPopupWindow(Activity context, View.OnClickListener addDevicesItemOnClick){
        super(context);

        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = layoutInflater.inflate(R.layout.popmenu,null);

        btn_Manual = (Button) mMenuView.findViewById(R.id.btn_Manual);
        btn_QRCode = (Button)mMenuView.findViewById(R.id.btn_QRCode);
        btn_Lan = (Button)mMenuView.findViewById(R.id.btn_lan);


        btn_Manual.setOnClickListener(addDevicesItemOnClick);
        btn_Lan.setOnClickListener(addDevicesItemOnClick);
        btn_QRCode.setOnClickListener(addDevicesItemOnClick);

        setContentView(mMenuView);
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setTouchable(true);
        setAnimationStyle(R.style.popup_animation);

        ColorDrawable dw = new ColorDrawable(0xb0000000);
        setBackgroundDrawable(dw);
    }

}
