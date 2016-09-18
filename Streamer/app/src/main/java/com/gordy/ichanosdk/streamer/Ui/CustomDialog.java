package com.gordy.ichanosdk.streamer.Ui;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gordy.ichanosdk.streamer.R;

/**
 * Created by Administrator on 2016/9/13.
 */
public class CustomDialog extends Dialog {

    public CustomDialog(Context context){
        super(context);
    }

    public CustomDialog(Context context, int theme){
        super(context,theme);
    }

    public static class Builder{
        private Context context;
        private String title;
        private String message;
        private String positiveButtonText;
        private String negativeButtonText;
        private View contentView;
        private OnClickListener positiveButtonClickListener;
        private OnClickListener negativeButtonClickListener;

        public Builder(Context context) {
            this.context = context;
        }

        public  Builder setMessage(String message){
            this.message = message;
            return this;
        }

        public Builder setMessage(int message){
            this.message =(String)context.getText(message);
            return this;
        }

        public Builder setTitle(String title){
            this.title = title;
            return this;
        }

        public Builder setTitle(int title){
            this.title = (String)context.getText(title);
            return this;
        }

        public Builder setContentView(View view){
            this.contentView = view;
            return this;
        }

        public Builder setPositiveButton(int positiveButtonText,OnClickListener listener){
            this.positiveButtonText = (String)context.getText(positiveButtonText);
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setPositiveButton(String positiveButtonText,OnClickListener listener){
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(int negativeButtonText,OnClickListener listener){
            this.negativeButtonText = (String)context.getText(negativeButtonText);
            this.negativeButtonClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(String negativeButtonText,OnClickListener listener){
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }


        public CustomDialog create(){

            LayoutInflater inflater  = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final CustomDialog customDialog = new CustomDialog(context, R.style.Dialog);
            View layout = inflater.inflate(R.layout.dialog_custom,null);
            customDialog.addContentView(layout,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT));

            ((TextView)layout.findViewById(R.id.tv_title_dlg)).setText(title);

            if(null != positiveButtonText){
                ((Button)layout.findViewById(R.id.btn_positive)).setText(positiveButtonText);
                if(null != positiveButtonClickListener){
                    ((Button)layout.findViewById(R.id.btn_positive)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            positiveButtonClickListener.onClick(customDialog,DialogInterface.BUTTON_POSITIVE);
                        }
                    });
                }

            }else{
                layout.findViewById(R.id.btn_positive).setVisibility(View.GONE);
            }

            if(null != negativeButtonText){
                ((Button)layout.findViewById(R.id.btn_negative)).setText(negativeButtonText);
                if(null != negativeButtonClickListener){
                    ((Button)layout.findViewById(R.id.btn_negative)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            negativeButtonClickListener.onClick(customDialog,DialogInterface.BUTTON_NEGATIVE);
                        }
                    });
                }

            }else{
                layout.findViewById(R.id.btn_negative).setVisibility(View.GONE);
            }

            if(null != message){
                ((TextView)layout.findViewById(R.id.tv_message_dlg)).setText(message);
            }else{
                ((LinearLayout)layout.findViewById(R.id.ly_content)).removeAllViews();
                ((LinearLayout)layout.findViewById(R.id.ly_content)).addView(contentView,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT));
            }
            customDialog.setContentView(layout);
            return customDialog;
        }

    }

}
