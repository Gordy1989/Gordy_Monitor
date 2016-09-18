package com.gordy.ichanosdk.viewer.System;

import android.graphics.Bitmap;

/**
 * Created by Administrator on 2016/9/13.
 */
public class StreamInfo {
    private String cid;
    private String userName;
    private String password;
    private String status;
    private String deviceName;
    private String status_con;
    private Bitmap snapBitmap;
    private int index;

    public StreamInfo(String cid) {
        this.cid = cid;
    }

    public StreamInfo(String cid,String userName,String password) {
        this.cid = cid;
        this.userName = userName;
        this.password = password;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getStatus_con() {
        return status_con;
    }

    public void setStatus_con(String status_con) {
        this.status_con = status_con;
    }

    public Bitmap getSnapBitmap() {
        return snapBitmap;
    }

    public void setSnapBitmap(Bitmap snapBitmap) {
        this.snapBitmap = snapBitmap;
    }

    public StreamInfo clone(){
        StreamInfo streamInfoNew = new StreamInfo(cid);
        streamInfoNew.setStatus(this.getStatus());
        streamInfoNew.setStatus_con(this.getStatus_con());
        streamInfoNew.setUserName(this.getUserName());
        streamInfoNew.setPassword(this.getPassword());
        streamInfoNew.setDeviceName(this.getDeviceName());
        streamInfoNew.setSnapBitmap(this.getSnapBitmap());
        streamInfoNew.setIndex(this.index);
        return streamInfoNew;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
