package com.gordy.ichanosdk.viewer.System;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gordy.ichanosdk.viewer.R;

import java.util.List;

/**
 * Created by Administrator on 2016/9/13.
 */
public class StreamAdapter extends ArrayAdapter<StreamInfo> {

    private int resourceId;
    private List<StreamInfo> list;

    public StreamAdapter(Context context, int resource, List<StreamInfo> objects){
        super(context,resource,objects);
        resourceId = resource;
        list = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        StreamInfo streamInfo = getItem(position);
        View view;
        ViewHolder viewHolder;
        if(null == convertView){
            view = LayoutInflater.from(getContext()).inflate(resourceId,null);
            viewHolder = new ViewHolder();
            viewHolder.tv_devicename = (TextView)view.findViewById(R.id.tv_devicename);
            viewHolder.tv_cid = (TextView)view.findViewById(R.id.tv_cid);
            viewHolder.tv_status = (TextView)view.findViewById(R.id.tv_status);
            viewHolder.tv_status_con = (TextView)view.findViewById(R.id.tv_status_con);

            if(null != streamInfo.getDeviceName())
                viewHolder.tv_devicename.setText(streamInfo.getDeviceName());
            if(null != streamInfo.getCid())
                viewHolder.tv_cid.setText(streamInfo.getCid());
            if(null != streamInfo.getStatus()) {
                viewHolder.tv_status.setText(streamInfo.getStatus());

                if (streamInfo.getStatus().equals("在线")) {
                    viewHolder.tv_status.setTextColor(Color.GREEN);
                } else if (streamInfo.getStatus().equals("离线")) {
                    viewHolder.tv_status.setTextColor(Color.RED);
                } else {
                    viewHolder.tv_status.setTextColor(Color.BLUE);
                }
            }
            if(null != streamInfo.getStatus_con()){
                viewHolder.tv_status_con.setText(streamInfo.getStatus_con());
                if(streamInfo.getStatus_con().equals("已连通")){
                    viewHolder.tv_status_con.setTextColor(Color.GREEN);
                }else{
                    viewHolder.tv_status_con.setTextColor(Color.RED);
                }
            }
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }
        return view;
    }

    public int getOnlineNum(){
        int onlineNum = 0;
        for(StreamInfo streamInfo : list){
            if(streamInfo.getStatus().equals("在线")){
                onlineNum++;
            }
        }
        return onlineNum;
    }

    public void setDataList(List<StreamInfo> list){
        this.list = list;
    }


    public List<StreamInfo> getDataList(){
        return list;
    }

    class ViewHolder{
        ImageView img_snap;
        TextView tv_devicename;
        TextView tv_cid;
        TextView tv_status;
        TextView tv_status_con;
    }
}
