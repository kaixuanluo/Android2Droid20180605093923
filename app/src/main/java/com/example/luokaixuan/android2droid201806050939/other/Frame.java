package com.example.luokaixuan.android2droid201806050939.other;

/**
 * Created by luokaixuan
 * Created Date 2018/6/5.
 * Description TODO
 */
public class Frame {

    public byte[] mData;
    public int offset;
    public int length;
    public Frame(byte[] data,int offset,int size){
        mData=data;
        this.offset=offset;
        this.length=size;
    }
    public void setFrame(byte[] data,int offset,int size){
        mData=data;
        this.offset=offset;
        this.length=size;
    }

}
