package com.prime.homeplus.settings.data;

import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import org.json.JSONObject;

import java.io.Serializable;

import static android.database.Cursor.FIELD_TYPE_BLOB;

public class Channel implements Serializable {
    private static final String TAG = "Channel";
    private static final boolean DEBUG = false;

    public static Channel fromCursor(Cursor cursor) {
        // Columns read must match the order of {@link #PROJECTION}
        Channel channel = new Channel();
        int totalColumn = cursor.getColumnCount();
        if (DEBUG) Log.d(TAG, "fromCursor(): get totalColumn:" + totalColumn);
        int index = 0;
        if (DEBUG) Log.d(TAG, "fromCursor(): loop start.");
        channel.JSONData = new JSONObject();
        for (int i = 0; i < totalColumn; i++) {
            if (cursor.getColumnName(i) != null) {
                try {
                    if (cursor.getType(i) == FIELD_TYPE_BLOB) {
                        byte[] vByteArray = cursor.getBlob(i);
                        String blobName = cursor.getColumnName(i);
                        if (DEBUG)
                            Log.d(TAG, "fromCursor():item:" + i + ", is BLOB, Name" + blobName);
                        channel.JSONInternalProviderData = new JSONObject(new String(vByteArray));
                        if (DEBUG)
                            Log.d(TAG, "new JSONI Data::item:" + i + ", get name:" + blobName + ",string:" + channel.JSONInternalProviderData.toString());
                    } else {
                        if (DEBUG)
                            Log.d(TAG, "fromCursor():item:" + i + ", get name:" + cursor.getColumnName(i) + ", data:" + cursor.getString(i));
                        channel.JSONData.put(cursor.getColumnName(i), cursor.getString(i));
                    }

                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        }
        String sTemp;
        try {
            if (DEBUG) Log.d(TAG, "fromCursor():: start pass data.");
            channel.mId = channel.JSONData.getLong("_id");
            if (channel.JSONData.isNull("package_name") == true) {
                if (DEBUG) Log.d(TAG, "pass channel package_name == null, set null.");
                channel.mPackageName = null;
            } else {
                channel.mPackageName = channel.JSONData.getString("package_name");
            }

            channel.mInputId = channel.JSONData.getString("input_id");

            //channel.mType = cursor.getString(index++);
            if (channel.JSONData.isNull("type") == true) {
                if (DEBUG) Log.d(TAG, "pass channel type == null, set null.");
                channel.mType = null;
            } else {
                channel.mType = channel.JSONData.getString("type");
            }

            channel.mDisplayNumber = channel.JSONData.getString("display_number");
            if (DEBUG) Log.d(TAG, "fromCursor():add mDisplayNumber:" + channel.mDisplayNumber);

            channel.mDisplayName = channel.JSONData.getString("display_name");
            if (DEBUG) Log.d(TAG, "fromCursor():add dispaly name:" + channel.mDisplayName);

            // TvContract.Channels.COLUMN_ORIGINAL_NETWORK_ID,
            channel.mOriginalNetworkId = channel.JSONData.getString("original_network_id");
            if (DEBUG)
                Log.d(TAG, "fromCursor():add original_network_id:" + channel.mOriginalNetworkId);

            // TvContract.Channels.COLUMN_SERVICE_ID,
            channel.mServiceId = channel.JSONData.getInt("service_id");
            if (DEBUG) Log.d(TAG, "fromCursor():add service_id:" + channel.mServiceId);

            //COLUMN_SERVICE_TYPE
            channel.mServiceType = channel.JSONData.getString("service_type");
            if (DEBUG) Log.d(TAG, "fromCursor():add service_type:" + channel.mServiceType);

            // TvContract.Channels.COLUMN_TRANSPORT_STREAM_ID,
            channel.mTransportStreamId = channel.JSONData.getInt("transport_stream_id");
            if (DEBUG)
                Log.d(TAG, "fromCursor():add transport_stream_id:" + channel.mTransportStreamId);

            // TvContract.Channels.COLUMN_TRANSIENT
            if (channel.JSONData.isNull("transient") == true) {
                if (DEBUG) Log.d(TAG, "pass channel transient == null, set null.");
                channel.mTransient = false;
            } else {
                sTemp = channel.JSONData.getString("transient");
                channel.mTransient = Boolean.valueOf(sTemp);
                if (DEBUG) Log.d(TAG, "fromCursor():add mTransient:" + channel.mTransient);
            }

            if (channel.JSONData.isNull("browsable") == true) {
                if (DEBUG) Log.d(TAG, "pass channel browsable == null, set null.");
                channel.mBrowsable = false;
            } else {
                sTemp = channel.JSONData.getString("browsable");
                channel.mBrowsable = Boolean.valueOf(sTemp);
                if (DEBUG) Log.d(TAG, "fromCursor():add mBrowsable:" + channel.mBrowsable);
            }

            if (channel.JSONData.isNull("locked") == true) {
                if (DEBUG) Log.d(TAG, "pass channel locked == null, set null.");
                channel.mLocked = false;
            } else {
                sTemp = channel.JSONData.getString("locked");
                channel.mLocked = Boolean.valueOf(sTemp);
                if (DEBUG) Log.d(TAG, "fromCursor():add mLocked:" + channel.mLocked);
            }
            if (DEBUG) Log.d(TAG, "fromCursor()::pass data end");
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            channel = null;
        }
        if (DEBUG) Log.d(TAG, "fromCursor():: return channel data.");
        return channel;
    }

    /**
     * Creates a {@link com.prime.dtv.hwControlPackage.Channel} object from the DVR database.
     */
    public static Channel fromDvrCursor(Cursor c) {
        Channel channel = new Channel();
        int index = -1;
        channel.mDvrId = c.getLong(++index);
        return channel;
    }

    public boolean isLocked() {
        if (JSONInternalProviderData != null && JSONInternalProviderData.has("channel_lock")) {
            try {
                return "1".equals(JSONInternalProviderData.getString("channel_lock"));
            } catch (Exception e) {
                Log.d(TAG, "isLocked error: " + e.getMessage());
            }
        }
        return false;
    }

    public void setLocked(boolean locked) {
        if (JSONInternalProviderData == null) {
            JSONInternalProviderData = new JSONObject();
        }
        try {
            JSONInternalProviderData.put("channel_lock", locked ? "1" : "0");
        } catch (Exception e) {
            Log.d(TAG, "setLocked error: " + e.getMessage());
        }
    }

    /**
     * ID of this channel. Matches to BaseColumns._ID.
     */
    private long mId;
    private String mPackageName;
    private String mInputId;
    private String mType;
    private String mDisplayNumber;
    private String mDisplayName;
    private String mDescription;
    private String mVideoFormat;
    private boolean mBrowsable;
    private boolean mLocked;
    private boolean mIsPassthrough;
    private String mAppLinkText;
    private int mAppLinkColor;
    private String mAppLinkIconUri;
    private String mAppLinkPosterArtUri;
    private String mAppLinkIntentUri;
    private Intent mAppLinkIntent;
    private int mAppLinkType;
    private long mDvrId;
    private String[] CanonicalGenres = {"NEWS", "MOVIES"};
    private byte[] internalProviderData;
    private JSONObject JSONInternalProviderData, JSONData;
    private boolean mHavefavorite = false;
    private String mOriginalNetworkId;
    private long mServiceId;
    private long mTransportStreamId;
    private String mServiceType;
    private boolean mTransient;

    private Channel() {
        // Do nothing.
    }

    @Override
    public String toString() {
        return "Channel{"
                + "id=" + mId
                + ", packageName=" + mPackageName
                + ", inputId=" + mInputId
                + ", type=" + mType
                + ", displayNumber=" + mDisplayNumber
                + ", displayName=" + mDisplayName
                + ", description=" + mDescription
                + ", videoFormat=" + mVideoFormat
                + ", isPassthrough=" + mIsPassthrough
                + ", browsable=" + mBrowsable
                + ", locked=" + mLocked
                + ", ServiceType=" + mServiceType
                + ", appLinkText=" + mAppLinkText + "}";
    }

    public long getChannelId() {
        return mId;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getInputId() {
        return mInputId;
    }

    public String getServiceType() {
        return mServiceType;
    }


    public String getOriginalNetworkId() {
        return mOriginalNetworkId;
    }

    public long getServiceId() {
        return mServiceId;
    }

    public long getTransportStreamId() {
        return mTransportStreamId;
    }

    public boolean getmTransient() {
        return mTransient;
    }

    public String getType() {
        return mType;
    }

    public String getDisplayNumber() {
        return mDisplayNumber;
    }

    public String getChannelName() {
        return mDisplayName;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getVideoFormat() {
        return mVideoFormat;
    }

    public String[] getGenres() {
        return CanonicalGenres;
    }

    public int getChannelNumber() {
        int iId = 0;
        if (mDisplayNumber == null) {
            //if (DEBUG)
            Log.d(TAG, "getChannelNumber(): no DisplayNumber.");
            return 0;
        }
        try {
            iId = Integer.parseInt(mDisplayNumber);
        } catch (NumberFormatException e) {
            System.out.println(mDisplayNumber + " is not a valid float number");
            return 0;
        }
        return iId;
    }

    public String StringGetChannelNumber() {
        int iId = 0;
        if (mDisplayNumber == null) {
            //if (DEBUG)
            Log.d(TAG, "getChannelNumber(): no DisplayNumber.");
            return null;
        }
        //Log.d(TAG, "getChannelNumber():return mDisplayNumber:" +  mDisplayNumber);
        return mDisplayNumber;
    }
}
