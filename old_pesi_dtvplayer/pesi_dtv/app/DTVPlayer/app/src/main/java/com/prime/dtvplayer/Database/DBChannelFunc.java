package com.prime.dtvplayer.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.prime.dtvplayer.Database.DBChannel;
import com.prime.dtvplayer.Database.DBUtils;

import java.util.ArrayList;
import java.util.List;
public class DBChannelFunc {
    private static final String TAG = "DBChannelFunc";
    private Context context;
    DBUtils dbUtils = new DBUtils();

    public static class Channel {
        private long ChannelId;
        private String ChannelName;
        private int ChannelNumber;

        public long getChannelId() {
            return ChannelId;
        }

        public String getChannelName() {
            return ChannelName;
        }

        public int getChannelNumber() {
            return ChannelNumber;
        }

        public void setChannelId(long channelId) {
            ChannelId = channelId;
        }

        public void setChannelName(String channelName) {
            ChannelName = channelName;
        }

        public void setChannelNumber(int channelNumber) {
            ChannelNumber = channelNumber;
        }

        public String ToString(){
            return "[ ChannelId : " + ChannelId + "ChannelName : " + ChannelName + "ChannelNumber : " + ChannelNumber + " ]";
        }

    }

    public DBChannelFunc(Context context) {
        this.context = context;
    }

    public void save(Channel channel) {
        Channel DBChannel = query(channel.getChannelId());
        if(DBChannel == null) {
            add(channel);
        }
        else {
            update(channel);
        }
    }

    public void save(List<Channel> channelList) {
        List<Channel> DBChannelList = query();
        for (int i = 0; i < channelList.size(); i++) {
            int update = 0;
            if(DBChannelList != null) {
                for (int j = 0; j < DBChannelList.size(); j++) {
                    if(channelList.get(i).getChannelId() == DBChannelList.get(j).getChannelId()) { // connie 20190912 fix update channel fail
                        update(channelList.get(i));
                        update = 1;
                        DBChannelList.remove(j);
                        break;
                    }
                }
            }
            if(update == 0) {
                add(channelList.get(i));
            }
        }
        if(DBChannelList != null) {
            for (int j = 0; j < DBChannelList.size(); j++) {
                remove(DBChannelList.get(j).getChannelId());
            }
        }
    }

    public List<Channel> getChannelList() {
        List<Channel> channelList = query();
        return channelList;
    }
    private void add(Channel channel) {
        ContentValues Value = new ContentValues();
        Value.put(DBChannel.CHANNEL_ID, channel.getChannelId());
        Value.put(DBChannel.CHANNEL_NAME, channel.getChannelName());
        Value.put(DBChannel.CHANNEL_NUMBER, channel.getChannelNumber());

        Log.d(TAG, "add "+ channel.ToString());
        context.getContentResolver().insert(DBChannel.CONTENT_URI, Value);
    }

    private void update(Channel channel) {
        int count;
        String where = DBChannel.CHANNEL_ID + " = " + channel.getChannelId();
        ContentValues Value = new ContentValues();
        Value.put(DBChannel.CHANNEL_ID, channel.getChannelId());
        Value.put(DBChannel.CHANNEL_NAME, channel.getChannelName());
        Value.put(DBChannel.CHANNEL_NUMBER, channel.getChannelNumber());

        Log.d(TAG, "update " + channel.ToString());
        count = context.getContentResolver().update(DBChannel.CONTENT_URI, Value, where, null);
    }

    private void remove(long channelId) {
        int count;
        String where = DBChannel.CHANNEL_ID + " = " + channelId;
        Log.d(TAG, "remove channelId "+ channelId);
        count = context.getContentResolver().delete(DBChannel.CONTENT_URI, where, null);
    }

    private List<Channel> query() {
        Cursor cursor = context.getContentResolver().query(DBChannel.CONTENT_URI, null, null, null, null);
        List<Channel> ChannelList = new ArrayList<Channel>();
        Channel channel = null;
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    channel = ParseCursor(cursor);
                    ChannelList.add(channel);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return ChannelList;
        }
        return null;
    }

    private Channel query(long channelId) {
        Channel Channel = null;
        String selection = DBChannel.CHANNEL_ID + " = " + channelId;
        Cursor cursor = context.getContentResolver().query(DBChannel.CONTENT_URI, null, selection, null, null);
        if(cursor != null) {
            if (cursor.moveToFirst())
                Channel = ParseCursor(cursor);
            cursor.close();
            return Channel;
        }
        return null;
    }

    private Channel ParseCursor(Cursor cursor){
        Channel Channel = new Channel();
        Channel.setChannelId(dbUtils.GetLongFromTable(cursor, DBChannel.CHANNEL_ID));
        Channel.setChannelName(dbUtils.GetStringFromTable(cursor, DBChannel.CHANNEL_NAME));
        Channel.setChannelNumber(dbUtils.GetIntFromTable(cursor, DBChannel.CHANNEL_NUMBER));

        Log.i(TAG, "ParseCursor Channel : "+Channel.ToString());
        return Channel;
    }


}
