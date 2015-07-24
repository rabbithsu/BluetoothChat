package com.example.android.bluetoothchat;

/**
 * Created by Rabbit徐 on 2015/7/22.
 */
public class CheckMessage {

    public static final int MessageType_Time=0;
    public static final int MessageType_From=1;
    public static final int MessageType_To=2;

    public CheckMessage(int Type,String Content)
    {
        this.mType=Type;
        this.mContent=Content;
    }


    private int mType;
    private String mContent;

    public int getType() {
        return mType;
    }

    public void setType(int mType) {
        this.mType = mType;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String mContent) {
        this.mContent = mContent;
    }
}
