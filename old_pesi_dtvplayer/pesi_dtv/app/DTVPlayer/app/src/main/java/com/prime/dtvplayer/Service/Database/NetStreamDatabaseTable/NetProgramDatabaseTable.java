package com.prime.dtvplayer.Service.Database.NetStreamDatabaseTable;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.prime.dtvplayer.Database.DBUtils;
import com.prime.dtvplayer.Service.DataManager.DataManager;
import com.prime.dtvplayer.Service.Database.DVBContentProvider;
import com.prime.dtvplayer.Sysdata.NetProgramInfo;
import com.prime.dtvplayer.Sysdata.ProgramInfo;

import java.util.ArrayList;
import java.util.List;

public class NetProgramDatabaseTable {
    private static final String TAG = "NetProgramDatabaseTable";
    public static final String TABLE_NAME = "NetProgram";

    public static final Uri CONTENT_URI = Uri.parse("content://" + DVBContentProvider.AUTHORITY + "/" + TABLE_NAME);

    //Type
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.netprogram";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.netprogram";

    private Context mContext;
    DBUtils mDbUtils = new DBUtils();

    public NetProgramDatabaseTable(Context context) {
        mContext = context;
    }


    /**
     * Set NetProgramInfo Table Column
     * @return cmd : NetProgramInfo Table Column
     */
    public static String TableCreate() {
        String cmd = "create table "
                + NetProgramDatabaseTable.TABLE_NAME + " ("
                + "_ID" + " integer primary key autoincrement,"
                + NetProgramInfo.CHANNEL_ID + ","
                + NetProgramInfo.PLAY_STREAM_TYPE + ","
                + NetProgramInfo.GROUP_TYPE + ","
                + NetProgramInfo.DISPLAY_NUM + ","
                + NetProgramInfo.DISPLAY_NAME + ","
                + NetProgramInfo.LOCK + ","
                + NetProgramInfo.SKIP + ","
                + NetProgramInfo.VIDEO_URL
                + ");";
        return cmd;
    }

    /**
     * @return cmd: Exist NetProgramInfo Table
     */
    public static String TableDrop() {
        String cmd = "DROP TABLE IF EXISTS " + NetProgramDatabaseTable.TABLE_NAME + ";";
        return cmd;
    }


    /**
     * Clear and Save Table
     * @param netprogramInfoList : NetProgramList
     */
    public boolean save(List<NetProgramInfo> netprogramInfoList) {
        removeAll();
        for (int i = 0; i < netprogramInfoList.size(); i++) {
            Uri resultUri = add(netprogramInfoList.get(i));
            if(resultUri == null)
                return false;//add error
        }
        return true;//add success
    }

    /**
     * Add NetProgramList To Table
     * @param netprogramInfoList : NetProgramList
     */
    public void add(List<NetProgramInfo> netprogramInfoList) {
        //removeAll();
        for (int i = 0; i < netprogramInfoList.size(); i++) {
            add(netprogramInfoList.get(i));
        }
    }

    /**
     * Update by NetProgramList
     * @param netprogramInfoList : NetProgramList
     */
    public void update(List<NetProgramInfo> netprogramInfoList) {
        for (int i = 0; i < netprogramInfoList.size(); i++) {
            update(netprogramInfoList.get(i));
        }
    }

    /**
     * Update single NetProgramInfo
     * @param netprogramInfo : NetProgramInfo
     */
    public void updateNetProgramInfo(NetProgramInfo netprogramInfo) {
        update(netprogramInfo);
    }


    /**
     * Get NetProgramInfoList from DataBase
     * @return NetProgramInfoLIst
     */
    public List<NetProgramInfo> load() {
        List<NetProgramInfo> netprogramInfoList = queryList();
        return netprogramInfoList;
    }

    public void UpdateNotify(NetProgramInfo netProgramInfo) {
        update(netProgramInfo);

    }

    /**
     * Add Single NetProgramInfo
     * @param netProgramInfo : NetProgramInfo
     */
    private Uri add(NetProgramInfo netProgramInfo) {
        ContentValues Value = new ContentValues();
        Value.put(NetProgramInfo.CHANNEL_ID, netProgramInfo.getChannelId());
        Value.put(NetProgramInfo.PLAY_STREAM_TYPE, netProgramInfo.getPlayStreamType());
        Value.put(NetProgramInfo.GROUP_TYPE, netProgramInfo.getGroupType());
        Value.put(NetProgramInfo.DISPLAY_NUM, netProgramInfo.getChannelNum());
        Value.put(NetProgramInfo.DISPLAY_NAME, netProgramInfo.getChannelName());
        Value.put(NetProgramInfo.LOCK, netProgramInfo.getUserLock());
        Value.put(NetProgramInfo.SKIP, netProgramInfo.getSkip());
        Value.put(NetProgramInfo.VIDEO_URL, netProgramInfo.getVideoUrl());

        Log.d(TAG, "add "+ netProgramInfo.ToString());
        Uri resultUri = mContext.getContentResolver().insert(NetProgramDatabaseTable.CONTENT_URI, Value);

        return resultUri;
    }


    /**
     * Update Single NetProgramInfo by ChannelId
     * @param netProgramInfo : NetProgramInfo
     */
    private void update(NetProgramInfo netProgramInfo) {
        int count;
        String where = NetProgramInfo.CHANNEL_ID + " = " + netProgramInfo.getChannelId();
        ContentValues Value = new ContentValues();
        Value.put(NetProgramInfo.CHANNEL_ID, netProgramInfo.getChannelId());
        Value.put(NetProgramInfo.PLAY_STREAM_TYPE, netProgramInfo.getPlayStreamType());
        Value.put(NetProgramInfo.GROUP_TYPE, netProgramInfo.getGroupType());
        Value.put(NetProgramInfo.DISPLAY_NUM, netProgramInfo.getChannelNum());
        Value.put(NetProgramInfo.DISPLAY_NAME, netProgramInfo.getChannelName());
        Value.put(NetProgramInfo.LOCK, netProgramInfo.getUserLock());
        Value.put(NetProgramInfo.SKIP, netProgramInfo.getSkip());
        Value.put(NetProgramInfo.VIDEO_URL, netProgramInfo.getVideoUrl());

        Log.d(TAG, "update " + netProgramInfo.ToString());
        count = mContext.getContentResolver().update(NetProgramDatabaseTable.CONTENT_URI, Value, where, null);

        mContext.getContentResolver().notifyChange(NetProgramDatabaseTable.CONTENT_URI,null);
    }


    /**
     * Delete NetProgramInfo Table
     */
    public int removeAll() {
        int count;
        count = mContext.getContentResolver().delete(NetProgramDatabaseTable.CONTENT_URI,null,null);

        return count;
    }

//    public void remove(ProgramInfo programInfo) {
//        int count;
//        String where = ProgramInfo.CHANNEL_ID + " = " + programInfo.getChannelId();
//        Log.d(TAG, "remove programInfo.getChannelId() "+ programInfo.getChannelId());
//        count = mContext.getContentResolver().delete(ProgramDatabaseTable.CONTENT_URI, where, null);
//    }

    /**
     * Query NetProgramInfoList from DataBase
     * @return NetProgramInfoList
     */
    private List<NetProgramInfo> queryList() {
//        SQLiteDatabase db_open = null;
//        try {
//            db_open = SQLiteDatabase.openDatabase("/data/vendor/dtvdata/DVB_DB.db", null, SQLiteDatabase.OPEN_READONLY);
//            if(db_open.isOpen())
//                db_open.close();
//        } catch (SQLiteCantOpenDatabaseException e) {
//            e.printStackTrace();
//        }
//        db_open.execSQL(NetProgramDatabaseTable.TableDrop());


        ////////////
//        Intent SearchIntent = new Intent();
//        SearchIntent.setAction("com.prime.netprogram.database.update");
//        mContext.sendBroadcast(SearchIntent, "android.permission.NETPROGRAM_BROADCAST");
        ////////////
        Cursor cursor = mContext.getContentResolver().query(NetProgramDatabaseTable.CONTENT_URI, null, null, null, null);
        List<NetProgramInfo> netprogramInfoList = new ArrayList<>();
        NetProgramInfo netprogramInfo = null;
        Log.d(TAG, "TestDataBase queryList: ");
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    netprogramInfo = ParseCursor(cursor);
                    Log.d(TAG, "TestDataBase queryList: url = [" + netprogramInfo.getVideoUrl()+"]");
                    netprogramInfoList.add(netprogramInfo);
                } while (cursor.moveToNext());
            }
            cursor.close();
            //Scoty Test ContentObserver
            //mContext.getContentResolver().notifyChange(NetProgramDatabaseTable.CONTENT_URI,new NetProgramContentObserver(new Handler()));
            ////////
            return netprogramInfoList;
        }
        return null;
    }

//    private ArrayList<ProgramInfo> queryList() {
//        String selection = DtvChannelTable.RECOMMEND_NAME + " = " + recommendTitle;
//        Cursor cursor = mContext.getContentResolver().query(DtvChannelTable.CONTENT_URI, null, selection, null, null);
//        ArrayList<Channel> ChannelList = new ArrayList<Channel>();
//        Channel channel = null;
//        if(cursor != null) {
//            if (cursor.moveToFirst()) {
//                do {
//                    channel = ParseCursor(cursor);
//                    ChannelList.add(channel);
//                } while (cursor.moveToNext());
//            }
//            cursor.close();
//            return ChannelList;
//        }
//        return null;
//    }

    /**
     * Query NetProgramInfo by ChannelId
     * @param channelId
     * @return
     */
    private NetProgramInfo query(long channelId) {
        NetProgramInfo netprogramInfo = null;
        String selection = NetProgramInfo.CHANNEL_ID + " = " + channelId;
        Cursor cursor = mContext.getContentResolver().query(NetProgramDatabaseTable.CONTENT_URI, null, selection, null, null);
        Log.d(TAG,"query cursor = "+cursor);
        if(cursor != null) {
            if (cursor.moveToFirst())
                netprogramInfo = ParseCursor(cursor);
            cursor.close();
            return netprogramInfo;
        }
        return null;
    }

    /**
     * Get NetprogramInfo from Cursor
     * @param cursor : NetProgramInfo DataBase Cursor
     * @return
     */
    private NetProgramInfo ParseCursor(Cursor cursor){
        NetProgramInfo netprogramInfo = new NetProgramInfo();

        netprogramInfo.setChannelId(mDbUtils.GetLongFromTable(cursor, NetProgramInfo.CHANNEL_ID));
        netprogramInfo.setPlayStreamType(mDbUtils.GetIntFromTable(cursor, NetProgramInfo.PLAY_STREAM_TYPE));
        netprogramInfo.setGroupType(mDbUtils.GetIntFromTable(cursor, NetProgramInfo.GROUP_TYPE));
        netprogramInfo.setChannelNum(mDbUtils.GetIntFromTable(cursor, NetProgramInfo.DISPLAY_NUM));
        netprogramInfo.setChannelName(mDbUtils.GetStringFromTable(cursor, NetProgramInfo.DISPLAY_NAME));
        netprogramInfo.setUserLock(mDbUtils.GetIntFromTable(cursor, NetProgramInfo.LOCK));
        netprogramInfo.setSkip(mDbUtils.GetIntFromTable(cursor, NetProgramInfo.SKIP));
        netprogramInfo.setVideoUrl(mDbUtils.GetStringFromTable(cursor, NetProgramInfo.VIDEO_URL));
        Log.i(TAG, "TestDataBase mDbUtils.GetIntFromTable(cursor, _ID) = "+mDbUtils.GetIntFromTable(cursor, "_ID") +
                " channel id = ["+netprogramInfo.getChannelId() +"] VideoUrl =[" + netprogramInfo.getVideoUrl()+"]\n origin = [" + mDbUtils.GetStringFromTable(cursor, NetProgramInfo.VIDEO_URL) +"]");
        return netprogramInfo;
    }
}