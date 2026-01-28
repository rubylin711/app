package com.prime.dtvplayer.Database;


import android.net.Uri;


public class DBChannel {
    public static final String TABLE_NAME = "dtv_channel";
    public static final String AUTHORITY = "com.prime.dtvplayer.Database.DTVContentProvider";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
    public static final int ITEM = 1;
    public static final int ITEM_ID = 2;

    //Type
    public static final String CONTENT_TYPE ="vnd.android.cursor.dir/vnd.dtv.channel";
    public static final String CONTENT_ITEM_TYPE="vnd.android.cursor.item/vnd.dtv.channel";

    public static final String CHANNEL_ID = "ChannelID";
    public static final String CHANNEL_NAME = "ChannelName";
    public static final String CHANNEL_NUMBER = "ChannelNumber";

    public static String TableCreate(){
        String cmd = "create table "
                + DBChannel.TABLE_NAME + " ("
                + "_ID" + " integer primary key autoincrement,"
                + DBChannel.CHANNEL_ID + ","
                + DBChannel.CHANNEL_NAME + ","
                + DBChannel.CHANNEL_NUMBER + ");";
        return cmd;
    }
}
