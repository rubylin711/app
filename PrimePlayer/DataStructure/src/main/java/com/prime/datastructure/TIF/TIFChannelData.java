package com.prime.datastructure.TIF;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.FavGroup;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.TpInfo;

import androidx.tvprovider.media.tv.Channel;
import androidx.tvprovider.media.tv.TvContractCompat;
import androidx.tvprovider.media.tv.TvContractCompat.Channels;

import android.media.tv.TvContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TIFChannelData {
    public static final String TAG = "TIFChannelData";
    public static boolean DEBUG = true;
    final static String[] projection = {
            Channels._ID,
            Channels.COLUMN_INPUT_ID,
            Channels.COLUMN_DISPLAY_NUMBER,
            Channels.COLUMN_DISPLAY_NAME,
            Channels.COLUMN_DESCRIPTION,
            Channels.COLUMN_SERVICE_ID,
            Channels.COLUMN_ORIGINAL_NETWORK_ID,
            Channels.COLUMN_TRANSPORT_STREAM_ID,
    };

    private Uri channelUri;

    private long channelId;
    private String inputId;
    private long displayNumber;
    private String displayName;
    private String descprition;
    private long serviceId;
    private long onId;
    private long tsId;

    public void setChannelUri(Uri value) {
        channelUri = value;
    }

    public void setChannelId(long value) {
        channelId = value;
    }

    public void setInputId(String value) {
        inputId = value;
    }

    public void setDisplayNumber(long value) {
        displayNumber = value;
    }

    public void setDisplayName(String value) {
        displayName = value;
    }

    public void setDescprition(String value) {
        descprition = value;
    }

    public void setServiceId(long value) {
        serviceId = value;
    }

    public void setOnId(long value) {
        onId = value;
    }

    public void setTsId(long value) {
        tsId = value;
    }

    public long getChannelId() {
        return channelId;
    }

    public String getInputId() {
        return inputId;
    }

    public long getDisplayNumber() {
        return displayNumber;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescprition() {
        return descprition;
    }

    public long getServiceId() {
        return serviceId;
    }

    public long getOnId() {
        return onId;
    }

    public long getTsId() {
        return tsId;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("{")
                .append("\n channelUri : ").append(channelUri)
                .append("\n channelId : ").append(channelId)
                .append("\n inputId : ").append(inputId)
                .append("\n displayNumber : ").append(displayNumber)
                .append("\n displayName : ").append(displayName)
                .append("\n descprition : ").append(descprition)
                .append("\n serviceId : ").append(serviceId)
                .append("\n onId : ").append(onId)
                .append("\n tsId : ").append(tsId)
                .append("\n }").toString();
    }

    private static String getTIFTunerType(int tunerType) {
        String tif_type = null;

        if (tunerType == TpInfo.DVBC)
            tif_type = Channels.TYPE_DVB_C;
        else if (tunerType == TpInfo.DVBS)
            tif_type = Channels.TYPE_DVB_S;
        else if (tunerType == TpInfo.DVBT)
            tif_type = Channels.TYPE_DVB_T;
        else if (tunerType == TpInfo.ISDBT)
            tif_type = Channels.TYPE_ISDB_T;
        else
            tif_type = Channels.TYPE_OTHER;
        return tif_type;
    }

    public static Uri getChannelUri(Context context, ProgramInfo programInfo, String tvInputId) {
        if (programInfo != null && tvInputId != null) {
            String[] projection = { TvContract.Channels._ID, TvContract.Channels.COLUMN_SERVICE_ID,
                    TvContract.Channels.COLUMN_TRANSPORT_STREAM_ID, TvContract.Channels.COLUMN_ORIGINAL_NETWORK_ID,
                    TvContract.Channels.COLUMN_INPUT_ID };
            String selection = TvContract.Channels.COLUMN_SERVICE_ID + " = ? AND "
                    + TvContract.Channels.COLUMN_TRANSPORT_STREAM_ID + " = ?"
                    + " AND " + TvContract.Channels.COLUMN_ORIGINAL_NETWORK_ID + " = ?" + " AND "
                    + TvContract.Channels.COLUMN_INPUT_ID + " = ?";
            String[] selectionArgs = { String.valueOf(programInfo.getServiceId()),
                    String.valueOf(programInfo.getTransportStreamId()),
                    String.valueOf(programInfo.getOriginalNetworkId()), tvInputId };
            try (Cursor cursor = context.getContentResolver().query(TvContract.Channels.CONTENT_URI, projection,
                    selection, selectionArgs, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idColumnIndex = cursor.getColumnIndex(TvContract.Channels._ID);
                    long id = cursor.getLong(idColumnIndex);
                    int sidColumnIndex = cursor.getColumnIndex(TvContract.Channels.COLUMN_SERVICE_ID);
                    int sid = cursor.getInt(sidColumnIndex);
                    int tsidColumnIndex = cursor.getColumnIndex(TvContract.Channels.COLUMN_TRANSPORT_STREAM_ID);
                    int tsid = cursor.getInt(tsidColumnIndex);
                    int onidColumnIndex = cursor.getColumnIndex(TvContract.Channels.COLUMN_ORIGINAL_NETWORK_ID);
                    int onid = cursor.getInt(onidColumnIndex);
                    int inputIdColumnIndex = cursor.getColumnIndex(TvContract.Channels.COLUMN_INPUT_ID);
                    String inputId = cursor.getString(inputIdColumnIndex);
                    // Log.d(TAG, "channel found: " + "_id: " +id + " sid: " + sid+ " tsid: "+
                    // tsid+ " onid: "+onid+ " inputId: "+inputId+ " name:
                    // "+programInfo.getDisplayName());
                    Uri ChannelUri = ContentUris.withAppendedId(TvContract.Channels.CONTENT_URI, id);
                    return ChannelUri;
                } else {
                    // Log.w(TAG, "channel not found for name: " + programInfo.getDisplayName());
                    return null;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error querying program: " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    public static void insertChannels(Context context, ProgramInfo programInfo, String tvInputId, int tunerType) {
        if (programInfo == null) {
            Log.e(TAG, "No channels to insert after scan");
            return;
        }

        ContentValues cv = createChannelContentValues(context, programInfo, tvInputId, tunerType);

        // Try to find existing channel by triplet
        Uri channelUri = findChannelByOnidTsidSid(context.getContentResolver(), tvInputId,
                programInfo.getOriginalNetworkId(), programInfo.getTransportStreamId(), programInfo.getServiceId());
        programInfo.setTvInputChannelUri(channelUri);
        ContentResolver cr = context.getContentResolver();
        if (channelUri != null) {
            UpdateChannels(context, programInfo, tvInputId, tunerType);
//            int rows = cr.update(channelUri, cv, null, null);
        } else {
            channelUri = cr.insert(Channels.CONTENT_URI, cv);
            int rows = cr.update(channelUri, cv, null, null);
            Log.d(TAG, " insert channel (" + programInfo.getOriginalNetworkId() + ","
                    + programInfo.getTransportStreamId() + "," + programInfo.getServiceId() + ") uri=" + channelUri);
        }

        programInfo.setTvInputChannelUri(channelUri);
    }

    private static ContentValues createChannelContentValues(Context context, ProgramInfo programInfo, String tvInputId,
            int tunerType) {
        ContentValues cv = new ContentValues();
        cv.put(Channels.COLUMN_INPUT_ID, tvInputId);
        String ownerPackage = "com.prime.tvinputframework";
        cv.put(TvContract.Channels.COLUMN_PACKAGE_NAME, ownerPackage);
        cv.put(Channels.COLUMN_DISPLAY_NUMBER, programInfo.getDisplayNum());
        cv.put(Channels.COLUMN_DISPLAY_NAME, programInfo.getDisplayName());
        cv.put(Channels.COLUMN_TYPE, getTIFTunerType(tunerType));
        cv.put(Channels.COLUMN_SEARCHABLE, 1);

        if (programInfo.getType() == ProgramInfo.PROGRAM_TV) {
            cv.put(Channels.COLUMN_SERVICE_TYPE, Channels.SERVICE_TYPE_AUDIO_VIDEO);
//            Log.i(TAG, programInfo.getDisplayName() + " COLUMN_SERVICE_TYPE: " + Channels.SERVICE_TYPE_AUDIO_VIDEO);
        } else if (programInfo.getType() == ProgramInfo.PROGRAM_RADIO) {
            cv.put(Channels.COLUMN_SERVICE_TYPE, Channels.SERVICE_TYPE_AUDIO);
//            Log.i(TAG, programInfo.getDisplayName() + " COLUMN_SERVICE_TYPE: " + Channels.SERVICE_TYPE_AUDIO);
        } else {
            cv.put(Channels.COLUMN_SERVICE_TYPE, Channels.SERVICE_TYPE_OTHER);
//            Log.i(TAG, programInfo.getDisplayName() + " COLUMN_SERVICE_TYPE: " + Channels.SERVICE_TYPE_OTHER);
        }

        cv.put(Channels.COLUMN_TRANSPORT_STREAM_ID, programInfo.getTransportStreamId());
        cv.put(Channels.COLUMN_ORIGINAL_NETWORK_ID, programInfo.getOriginalNetworkId());
        cv.put(Channels.COLUMN_SERVICE_ID, programInfo.getServiceId());
        cv.put(Channels.COLUMN_INTERNAL_PROVIDER_ID, programInfo.getOriginalNetworkId() + "-"
                + programInfo.getTransportStreamId() + "-" + programInfo.getServiceId());
        cv.put(Channels.COLUMN_INTERNAL_PROVIDER_FLAG3, programInfo.getAdultFlag()); // set adult channel flag for cns
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(ProgramInfo.AUDIO_SELECTED, programInfo.getAudioSelected());
            jsonObject.put(ProgramInfo.AUDIO_LR_SELECTED, programInfo.getAudioLRSelected());
            jsonObject.put("channel_lock", programInfo.getLock());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cv.put(Channels.COLUMN_INTERNAL_PROVIDER_DATA, jsonObject.toString().getBytes());
        return cv;
    }

    public static Uri findChannelByOnidTsidSid(ContentResolver cr, String inputId,
            int onid, int tsid, int sid) {
        Uri uri = TvContractCompat.buildChannelsUriForInput(inputId);
        String[] projection = {
                Channels._ID,
                Channels.COLUMN_ORIGINAL_NETWORK_ID,
                Channels.COLUMN_TRANSPORT_STREAM_ID,
                Channels.COLUMN_SERVICE_ID
        };

        try (Cursor c = cr.query(uri, projection, null, null, null)) {
            if (c == null)
                return null;
            int idxId = c.getColumnIndex(Channels._ID);
            int idxOnid = c.getColumnIndex(Channels.COLUMN_ORIGINAL_NETWORK_ID);
            int idxTsid = c.getColumnIndex(Channels.COLUMN_TRANSPORT_STREAM_ID);
            int idxSid = c.getColumnIndex(Channels.COLUMN_SERVICE_ID);

            while (c.moveToNext()) {
                // 有些裝置欄位可能為 NULL，保護一下
                int _onid = c.isNull(idxOnid) ? -1 : c.getInt(idxOnid);
                int _tsid = c.isNull(idxTsid) ? -1 : c.getInt(idxTsid);
                int _sid = c.isNull(idxSid) ? -1 : c.getInt(idxSid);

                if (_onid == onid && _tsid == tsid && _sid == sid) {
                    long id = c.getLong(idxId);
                    return TvContractCompat.buildChannelUri(id);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "findChannelByOnidTsidSid failed: " + e);
        }
        return null;
    }

    public static void insertChannelList(Context context, List<ProgramInfo> programInfoList, String tvInputId,
            int tunerType) {
        Log.d(TAG, "insertChannelList programInfoList.size = " + programInfoList.size());
        if (programInfoList == null || programInfoList.isEmpty()) {
            Log.e(TAG, "No channels to insert after scan");
            return;
        }
        for (ProgramInfo programInfo : programInfoList) {
            insertChannels(context, programInfo, tvInputId, tunerType);
        }
        // Log.d(TAG,"insertChannelList done");
    }

    public static void UpdateChannels(Context context, ProgramInfo programInfo, String tvInputId, int tunerType) {
        if (programInfo == null || programInfo.getTvInputChannelUri() == null) {
            Log.e(TAG, "No channels to update");
            return;
        }
        Uri channelUri = programInfo.getTvInputChannelUri();
        Log.d(TAG, "UpdateChannels programInfo " + programInfo.getDisplayName() + " tvInputId = " + tvInputId);
        String[] projection = { Channels.COLUMN_INTERNAL_PROVIDER_DATA };
        try (Cursor cursor = context.getContentResolver().query(channelUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(cursor.getColumnIndex(Channels.COLUMN_INTERNAL_PROVIDER_DATA));
                if (data != null && data.length > 0) {
                    try {
                        JSONObject jsonObject = new JSONObject(new String(data));
                        if (jsonObject.has("channel_lock")) {
                            int lock = jsonObject.getInt("channel_lock");
                            Log.d(TAG, "Sync lock from TIF: " + lock + " for " + programInfo.getDisplayName());
                            programInfo.setLock(lock);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing TIF internal provider data: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying channel URI: " + e.getMessage());
        }
        ContentValues cv = createChannelContentValues(context, programInfo, tvInputId, tunerType);

        Log.d(TAG,
                "UpdateChannels programInfo " + programInfo.getDisplayName() + " channel id = "
                        + programInfo.getChannelId() +
                        " channelUri = " + channelUri);
        if (channelUri != null) {
            int rows = context.getContentResolver().update(channelUri, cv, null, null);
            Log.d(TAG,
                    " update channel (" + programInfo.getOriginalNetworkId() + "," + programInfo.getTransportStreamId()
                            + "," + programInfo.getServiceId() + ") rows=" + rows
                            + " uri=" + channelUri);
        }
    }

    public static void UpdateOrInsertChannels(Context context, ProgramInfo programInfo, String tvInputId,
            int tunerType) {
        if (programInfo == null) {
            Log.e(TAG, "No channels to update");
            return;
        }

        Uri channelUri = programInfo.getTvInputChannelUri();
        if (channelUri != null) {
            UpdateChannels(context, programInfo, tvInputId, tunerType);
        } else {
            insertChannels(context, programInfo, tvInputId, tunerType);
        }
        programInfo.setTvInputChannelUri(channelUri);
    }

    public static Uri getTIFChannelUri(Context context, Uri tvInputUri, long channelId) {
        String mSelectionClause = Channels._ID + " = ?";
        String[] mSelectionArgs = { String.valueOf(channelId) };
        Cursor cursor = context.getContentResolver().query(
                Channels.CONTENT_URI, TIFChannelData.projection, mSelectionClause, mSelectionArgs, null);
        if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
            int idColumnIndex = cursor.getColumnIndex(Channels._ID);
            long id = cursor.getLong(idColumnIndex);
            return TvContractCompat.buildChannelUri(id);
        }
        return null;
    }

    public static Uri getTIFChannelUri(Context context, Uri tvInputUri, int serviceId) {
        String mSelectionClause = Channels.COLUMN_SERVICE_ID + " = ?";
        String[] mSelectionArgs = { String.valueOf(serviceId) };
        Cursor cursor = context.getContentResolver().query(
                Channels.CONTENT_URI, TIFChannelData.projection, mSelectionClause, mSelectionArgs, null);
        if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
            int idColumnIndex = cursor.getColumnIndex(Channels._ID);
            int serviceIdIndex = cursor.getColumnIndex(Channels.COLUMN_SERVICE_ID);
            long id = cursor.getLong(idColumnIndex);
            int pServiceId = cursor.getInt(serviceIdIndex);
            Log.d(TAG, "found Service ID : " + serviceId + " channel");
            return TvContractCompat.buildChannelUri(id);
        }
        return null;
    }

    public static void PrintTIFChannelData(Context context, Uri tvInputUri, String tvInputId) {
        Log.d(TAG, "PrintTIFChannelData tvInputUri = " + tvInputUri + " tvInputId = " + tvInputId);
        String mSelectionClause = Channels.COLUMN_INPUT_ID + " =?";
        String[] mSelectionArgs = { tvInputId };
        Cursor cursor = context.getContentResolver().query(
                Channels.CONTENT_URI, TIFChannelData.projection, null, null, null);
        Log.d(TAG, "cursor = " + cursor + " cursor count = " + cursor.getCount());
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Log.d(TAG,
                        "===============================" + cursor.getPosition() + "===============================");
                int idColumnIndex = cursor.getColumnIndex(Channels._ID);
                int tvinputidIndex = cursor.getColumnIndex(Channels.COLUMN_INPUT_ID);
                int displayNameIndex = cursor.getColumnIndex(Channels.COLUMN_DISPLAY_NAME);
                int serviceidIndex = cursor.getColumnIndex(Channels.COLUMN_SERVICE_ID);

                if (idColumnIndex != -1) {
                    long channelId = cursor.getLong(idColumnIndex);
                    String tvinputid = cursor.getString(tvinputidIndex);
                    String displayName = cursor.getString(displayNameIndex);
                    int serviceid = cursor.getInt(serviceidIndex);
                    Log.d(TAG, "Found channel ID: " + channelId + " tvinputid = " + tvinputid +
                            " name : " + displayName + " serviceid = " + serviceid);
                } else {
                    Log.e(TAG, "Column _ID not found in cursor");
                }
            } while (cursor.moveToNext());
        }
    }

    public static void deleteTIFChannelDataByTvInputId(Context context, String tvInputId) {
        Log.d(TAG, "deleteTIFChannelDataByTvInputId tvInputId = " + tvInputId);
        String mSelectionClause = Channels.COLUMN_INPUT_ID + " =?";
        String[] mSelectionArgs = { tvInputId };
        int cursor = context.getContentResolver().delete(Channels.CONTENT_URI, mSelectionClause, mSelectionArgs);
    }

    public static void deleteTIFChannelDataByChannelUri(Context context, Uri tvInputUri) {
        Log.d(TAG, "deleteTIFChannelDataByChannelUri tvInputUri = " + tvInputUri);
        int cursor = context.getContentResolver().delete(tvInputUri, null, null);
    }

    public static Bundle getChannelSIdOnIdTsIdFromUri(Context context, Uri channelUri) {
        int sid = 0, onid = 0, tsid = 0;
        if (channelUri == null) {
            Log.e(TAG, "Channel URI is null");
            return new Bundle();
        }

        // 定義要查詢的欄位
        String[] projection = {
                Channels.COLUMN_SERVICE_ID,
                Channels.COLUMN_ORIGINAL_NETWORK_ID,
                Channels.COLUMN_TRANSPORT_STREAM_ID
        };

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            // 查詢特定 URI 的頻道資料
            cursor = resolver.query(
                    channelUri, // 查詢的 URI
                    projection, // 要返回的欄位
                    null, // 選擇條件（這裡不需要，因為 URI 已指定單個頻道）
                    null, // 選擇參數
                    null // 排序
            );

            // 檢查查詢結果
            if (cursor != null && cursor.moveToFirst()) {
                int SidColumnIndex = cursor.getColumnIndex(Channels.COLUMN_SERVICE_ID);
                if (SidColumnIndex != -1)
                    sid = cursor.getInt(SidColumnIndex);
                int OnidColumnIndex = cursor.getColumnIndex(Channels.COLUMN_ORIGINAL_NETWORK_ID);
                if (OnidColumnIndex != -1)
                    onid = cursor.getInt(OnidColumnIndex);
                int TsidColumnIndex = cursor.getColumnIndex(Channels.COLUMN_TRANSPORT_STREAM_ID);
                if (TsidColumnIndex != -1)
                    tsid = cursor.getInt(TsidColumnIndex);
            } else {
                Log.e(TAG, "No channel found for URI: " + channelUri);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying channel URI: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Bundle bundle = new Bundle();
        bundle.putInt(ProgramInfo.SERVICE_ID, sid);
        bundle.putInt(ProgramInfo.ORIGINAL_NETWORK_ID, onid);
        bundle.putInt(ProgramInfo.TRANSPORT_STREAM_ID, tsid);
        return bundle;
    }

    public static long getChannelIdFromUri(Context context, Uri channelUri) {
        if (channelUri == null) {
            Log.e(TAG, "Channel URI is null");
            return -1;
        }

        // 定義要查詢的欄位
        String[] projection = {
                Channels._ID
        };

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            // 查詢特定 URI 的頻道資料
            cursor = resolver.query(
                    channelUri, // 查詢的 URI
                    projection, // 要返回的欄位
                    null, // 選擇條件（這裡不需要，因為 URI 已指定單個頻道）
                    null, // 選擇參數
                    null // 排序
            );

            // 檢查查詢結果
            if (cursor != null && cursor.moveToFirst()) {
                int idColumnIndex = cursor.getColumnIndex(Channels._ID);
                if (idColumnIndex != -1) {
                    long channelId = cursor.getLong(idColumnIndex);
                    Log.d(TAG, "Found channel ID: " + channelId);
                    return channelId;
                } else {
                    Log.e(TAG, "Column _ID not found in cursor");
                }
            } else {
                Log.e(TAG, "No channel found for URI: " + channelUri);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying channel URI: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return -1; // 失敗時返回 -1
    }

    public static TIFChannelData getTIFChannelDataFromUri(Context context, Uri channelUri) {
        // Uri channelUri = TvContract.buildChannelUri(channelId);
        if (channelUri == null) {
            Log.e(TAG, "Channel URI is null");
            return null;
        }

        // // 定義要查詢的欄位
        // String[] projection = {
        // TvContractCompat.Channels._ID
        // };

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            // 查詢特定 URI 的頻道資料
            cursor = resolver.query(
                    channelUri, // 查詢的 URI
                    projection, // 要返回的欄位
                    null, // 選擇條件（這裡不需要，因為 URI 已指定單個頻道）
                    null, // 選擇參數
                    null // 排序
            );

            // 檢查查詢結果
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(Channels._ID);
                int inputIdIndex = cursor.getColumnIndex(Channels.COLUMN_INPUT_ID);
                int displayNumberIndex = cursor.getColumnIndex(Channels.COLUMN_DISPLAY_NUMBER);
                int displayNameIndex = cursor.getColumnIndex(Channels.COLUMN_DISPLAY_NAME);
                int descpritionIndex = cursor.getColumnIndex(Channels.COLUMN_DESCRIPTION);
                int serviceIdIndex = cursor.getColumnIndex(Channels.COLUMN_SERVICE_ID);
                int onIdIndex = cursor.getColumnIndex(Channels.COLUMN_ORIGINAL_NETWORK_ID);
                int tsIdIndex = cursor.getColumnIndex(Channels.COLUMN_TRANSPORT_STREAM_ID);

                long channelId = cursor.getLong(idIndex);
                String inputId = cursor.getString(inputIdIndex);
                long displayNumber = cursor.getLong(displayNumberIndex);
                String displayName = cursor.getString(displayNameIndex);
                String descprition = cursor.getString(descpritionIndex);
                long serviceId = cursor.getLong(serviceIdIndex);
                long onId = cursor.getLong(onIdIndex);
                long tsId = cursor.getLong(tsIdIndex);
                if (DEBUG == true) {
                    Log.d(TAG, "===============================" + cursor.getPosition()
                            + "===============================");
                    Log.d(TAG, "channelId = " + channelId);
                    Log.d(TAG, "channelUri = " + channelUri);
                    Log.d(TAG, "inputId = " + inputId);
                    Log.d(TAG, "displayNumber = " + displayNumber);
                    Log.d(TAG, "displayName = " + displayName);
                    Log.d(TAG, "descprition = " + descprition);
                    Log.d(TAG, "serviceId = " + serviceId);
                    Log.d(TAG, "onId = " + onId);
                    Log.d(TAG, "tsId = " + tsId);
                }
                TIFChannelData node = new TIFChannelData();
                node.setChannelUri(channelUri);
                node.setChannelId(channelId);
                node.setInputId(inputId);
                node.setDisplayNumber(displayNumber);
                node.setDisplayName(displayName);
                node.setDescprition(descprition);
                node.setServiceId(serviceId);
                node.setOnId(onId);
                node.setTsId(tsId);
                return node;
            } else {
                Log.e(TAG, "No channel found for URI: " + channelUri);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error querying channel URI: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    public static long getChannelIdFromUriWithQuery(Context context, Uri channelUri) {
        if (channelUri == null || context == null) {
            throw new IllegalArgumentException("Invalid input: URI or ContentResolver is null");
        }

        // 查詢特定 URI 的頻道
        String[] projection = { TvContract.Channels._ID };
        try (Cursor cursor = context.getContentResolver().query(channelUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(TvContract.Channels._ID));
            } else {
                throw new IllegalArgumentException("Channel not found for URI: " + channelUri);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error querying channel: " + e.getMessage());
        }
    }

    public static String getStringFromUri(Context context, Uri channelUri, String columnName) {
        return getStringFromUri(context, channelUri, columnName, "");
    }

    public static String getStringFromUri(Context context, Uri channelUri, String columnName, String defaultValue) {
        if (context == null || channelUri == null) {
            Log.e(TAG, "getStringFromUri: null Context or ChannelUri.");
            return defaultValue;
        }

        ContentResolver contentResolver = context.getContentResolver();
        String[] projection = { columnName };
        try (Cursor cursor = contentResolver.query(
                channelUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(columnName);
                if (index >= 0 && !cursor.isNull(index)) {
                    return cursor.getString(index);
                }
            }
        }

        return defaultValue;
    }

    public static int getIntFromUri(Context context, Uri channelUri, String columnName) {
        return getIntFromUri(context, channelUri, columnName, 0);
    }

    public static int getIntFromUri(Context context, Uri channelUri, String columnName, int defaultValue) {
        if (context == null || channelUri == null) {
            Log.e(TAG, "getIntFromUri: null Context or ChannelUri.");
            return defaultValue;
        }

        ContentResolver contentResolver = context.getContentResolver();
        String[] projection = { columnName };
        try (Cursor cursor = contentResolver.query(
                channelUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(columnName);
                if (index >= 0 && !cursor.isNull(index)) {
                    return cursor.getInt(index);
                }
            }
        }

        return defaultValue;
    }

    public static long getLongFromUri(Context context, Uri channelUri, String columnName) {
        return getLongFromUri(context, channelUri, columnName, 0L);
    }

    public static long getLongFromUri(Context context, Uri channelUri, String columnName, long defaultValue) {
        if (context == null || channelUri == null) {
            Log.e(TAG, "getLongFromUri: null Context or ChannelUri.");
            return defaultValue;
        }

        ContentResolver contentResolver = context.getContentResolver();
        String[] projection = { columnName };
        try (Cursor cursor = contentResolver.query(
                channelUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(columnName);
                if (index >= 0 && !cursor.isNull(index)) {
                    return cursor.getLong(index);
                }
            }
        }

        return defaultValue;
    }

}
