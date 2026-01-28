package com.prime.homeplus.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.prime.datastructure.config.Pvcfg;
import com.prime.homeplus.settings.data.Channel;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.RequiresApi;

import static android.database.Cursor.FIELD_TYPE_BLOB;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ChannelControlManager {
    private String TAG = "ChannelControlManager";
    boolean DEBUG = false;
    private Context context;

    public ChannelControlManager(Context context) {
        this.context = context;
    }

    private ContentResolver mContentResolver;

    public ArrayList getChannelList() {
        String input_id = Pvcfg.getTvInputId();

        String selection = "";
        LogUtils.d("input_id = "+input_id);
        //selection = "input_id = '" + input_id + "' AND service_type != ' SERVICE_TYPE_OTHER' AND  CAST(display_number  AS INT) != 0 AND CAST(browsable AS INT) != 0  AND CAST(display_number AS INT) <  8000";
        selection = "input_id = '" + input_id + "' AND service_type != ' SERVICE_TYPE_OTHER' AND  CAST(display_number  AS INT) != 0 AND CAST(display_number AS INT) <  "+Pvcfg.MAX_DISPLAY_NUMBER;
        String sortOrder = "CAST(display_number  AS INT)";

        return getChannelListFromProvider(context, null, selection, null, sortOrder);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ArrayList<Channel> getChannelListFromProvider(Context inContext, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        if (DEBUG) Log.i(TAG, " getChannelListFromProvider()");
        ArrayList<Channel> list = new ArrayList<>();
        Uri mUri = TvContract.Channels.CONTENT_URI;
        String[] mProjection = null;
        if (projection == null) {
            //mProjection = CHANNEL_LIST_PROJECTION;
        } else {
            mProjection = projection;
        }
        if (DEBUG) Log.i(TAG, " hwChannel mUri:" + mUri);
        if (DEBUG) Log.i(TAG, "  hwChannel mProjection:" + Arrays.toString(mProjection));
        if (mContentResolver == null) {
            if (inContext == null) {
                Log.i(TAG, " inContext == null return.");
                return null;
            }
            mContentResolver = inContext.getContentResolver();
            if (mContentResolver == null) {
                Log.i(TAG, " mContentResolver == null return.");
                return null;
            }
        }
        try (Cursor c = mContentResolver.query(mUri, mProjection, selection, selectionArgs, sortOrder)) {
            if (c != null) {
                if (DEBUG) Log.d(TAG, "query() ! = NULL, go onQuery");
                int itmesCounter = c.getCount();
                if (DEBUG) Log.d(TAG, "itmesCounter:" + itmesCounter);

                //c.moveToFirst();
                boolean dd = c.moveToNext();
                if (DEBUG) Log.d(TAG, "query() moveToNext:" + dd);
                while (dd) {
                    if (DEBUG) Log.d(TAG, "query() moveToNext:" + dd);
                    if (DEBUG) Log.d(TAG, "query() c String:" + c.toString());
                    // for (int i =1; i<itmesCounter; i++) {
                    Channel myChannel = Channel.fromCursor(c);
                    //myChannel.dump();
                    if (myChannel == null) {
                        Log.d(TAG, "XXXX query() get null channel data droup it.XXXX");
                    } else {
                        list.add(myChannel);
                    }
                    //if (DEBUG) Log.d(TAG, "query() myChannel:" + myChannel.toString() );

                    int totalColumn = c.getColumnCount();
                    for (int i = 0; i < totalColumn; i++) {
                        if (c.getColumnName(i) != null) {
                            try {
                                if (c.getType(i) == FIELD_TYPE_BLOB) {
                                    if (DEBUG) Log.d(TAG, "fromCursor():item:" + i + ", is BLOB");

                                } else {
                                    if (DEBUG)
                                        Log.d(TAG, "fromCursor2:item:" + i + ", get name:" + c.getColumnName(i) + ", data:" + c.getString(i));
                                }
                            } catch (Exception e) {
                                Log.d(TAG, e.getMessage());
                            }
                        }
                    }

                    dd = c.moveToNext();
                }
                //Result result = onQuery(c);
                if (DEBUG) Log.v(TAG, "Finished query for ");
                return list;
            } else {
                Log.d(TAG, "query() == NULL");
                if (c == null) {
                    Log.e(TAG, "Unknown query error for ");
                } else {
                    if (DEBUG) Log.d(TAG, "Canceled query for ");
                }
                return list;
            }
        } catch (Exception e) {
            Log.d(TAG, "Exception");
            return list;
        }

    }
    
    public void updateChannelLockStatus(Channel channel, boolean locked) {
        if (context == null) return;

        Uri mUri = TvContract.Channels.CONTENT_URI;
        long channelId = channel.getChannelId();

        // Fetch current internal_provider_data
        String[] projection = {TvContract.Channels.COLUMN_INTERNAL_PROVIDER_DATA};
        String selection = TvContract.Channels._ID + " = ?";
        String[] selectionArgs = {String.valueOf(channelId)};

        try (Cursor cursor = context.getContentResolver().query(mUri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                org.json.JSONObject jsonObject = null;
                byte[] blob = cursor.getBlob(0);
                if (blob != null) {
                    try {
                        jsonObject = new org.json.JSONObject(new String(blob));
                    } catch (Exception e) {
                        Log.d(TAG, "Parse internal_provider_data error: " + e.getMessage());
                    }
                }

                if (jsonObject == null) {
                    jsonObject = new org.json.JSONObject();
                }

                try {
                    jsonObject.put("channel_lock", locked ? "1" : "0");
                    // Sync in-memory channel object
                    channel.setLocked(locked);
                } catch (Exception e) {
                    Log.d(TAG, "Put channel_lock error: " + e.getMessage());
                }

                android.content.ContentValues values = new android.content.ContentValues();
                values.put(TvContract.Channels.COLUMN_INTERNAL_PROVIDER_DATA, jsonObject.toString().getBytes());

                context.getContentResolver().update(mUri, values, selection, selectionArgs);
            }
        } catch (Exception e) {
             Log.d(TAG, "updateChannelLockStatus error: " + e.getMessage());
        }
    }
}
