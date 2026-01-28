package com.prime.datastructure.TIF;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.net.Uri;
import android.util.Log;

import androidx.tvprovider.media.tv.TvContractCompat;

import com.prime.datastructure.sysdata.PvrRecIdx;

public class TIFRecordedProgramData {
    private static final String TAG = "TIFRecordedProgramData";

    @SuppressLint("RestrictedApi")
    private static final String[] PROJECTION = new String[] {
            TvContractCompat.RecordedPrograms._ID,
            TvContractCompat.RecordedPrograms.COLUMN_PACKAGE_NAME,
            TvContractCompat.RecordedPrograms.COLUMN_INPUT_ID,
            TvContractCompat.RecordedPrograms.COLUMN_CHANNEL_ID,
            TvContractCompat.RecordedPrograms.COLUMN_TITLE,
            TvContractCompat.RecordedPrograms.COLUMN_EPISODE_DISPLAY_NUMBER,
            TvContractCompat.RecordedPrograms.COLUMN_EPISODE_TITLE,
            TvContractCompat.RecordedPrograms.COLUMN_START_TIME_UTC_MILLIS,
            TvContractCompat.RecordedPrograms.COLUMN_END_TIME_UTC_MILLIS,
            TvContractCompat.RecordedPrograms.COLUMN_LONG_DESCRIPTION,
            TvContractCompat.RecordedPrograms.COLUMN_CONTENT_RATING,
            TvContractCompat.RecordedPrograms.COLUMN_RECORDING_DATA_URI,
            TvContractCompat.RecordedPrograms.COLUMN_RECORDING_DATA_BYTES,
            TvContractCompat.RecordedPrograms.COLUMN_RECORDING_DURATION_MILLIS,
            TvContractCompat.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_DATA,
            TvContractCompat.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_FLAG1,
            TvContractCompat.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_FLAG2,
            TvContractCompat.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_FLAG3,
            TvContractCompat.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_FLAG4,
            TvContractCompat.RecordedPrograms.COLUMN_VERSION_NUMBER,
            TvContract.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_ID
    };

    private long id;
    private String packageName;
    private String inputId;
    private long channelId;
    private String title;
    private String episodeDisplayNumber;
    private String episodeTitle;
    private long startTimeUtcMillis;
    private long endTimeUtcMillis;
    private String longDescription;
    private String contentRatings;
    private String recordingDataUri;
    private long recordingDataBytes;
    private long recordingDurationMillis;
    private byte[] internalProviderData;
    private long internalProviderFlag1;
    private long internalProviderFlag2;
    private long internalProviderFlag3;
    private long internalProviderFlag4;
    private String internalProviderId;
    private int versionNumber;

    public TIFRecordedProgramData() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getInputId() {
        return inputId;
    }

    public void setInputId(String inputId) {
        this.inputId = inputId;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEpisodeDisplayNumber() {
        return episodeDisplayNumber;
    }

    public void setEpisodeDisplayNumber(String episodeDisplayNumber) {
        this.episodeDisplayNumber = episodeDisplayNumber;
    }

    public String getEpisodeTitle() {
        return episodeTitle;
    }

    public void setEpisodeTitle(String episodeTitle) {
        this.episodeTitle = episodeTitle;
    }

    public long getStartTimeUtcMillis() {
        return startTimeUtcMillis;
    }

    public void setStartTimeUtcMillis(long startTimeUtcMillis) {
        this.startTimeUtcMillis = startTimeUtcMillis;
    }

    public long getEndTimeUtcMillis() {
        return endTimeUtcMillis;
    }

    public void setEndTimeUtcMillis(long endTimeUtcMillis) {
        this.endTimeUtcMillis = endTimeUtcMillis;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getContentRatings() {
        return contentRatings;
    }

    public void setContentRatings(String contentRatings) {
        this.contentRatings = contentRatings;
    }

    public String getRecordingDataUri() {
        return recordingDataUri;
    }

    public void setRecordingDataUri(String recordingDataUri) {
        this.recordingDataUri = recordingDataUri;
    }

    public long getRecordingDataBytes() {
        return recordingDataBytes;
    }

    public void setRecordingDataBytes(long recordingDataBytes) {
        this.recordingDataBytes = recordingDataBytes;
    }

    public long getRecordingDurationMillis() {
        return recordingDurationMillis;
    }

    public void setRecordingDurationMillis(long recordingDurationMillis) {
        this.recordingDurationMillis = recordingDurationMillis;
    }

    public byte[] getInternalProviderData() {
        return internalProviderData;
    }

    public void setInternalProviderData(byte[] internalProviderData) {
        this.internalProviderData = internalProviderData;
    }

    public long getInternalProviderFlag1() {
        return internalProviderFlag1;
    }

    public void setInternalProviderFlag1(long internalProviderFlag1) {
        this.internalProviderFlag1 = internalProviderFlag1;
    }

    public long getInternalProviderFlag2() {
        return internalProviderFlag2;
    }

    public void setInternalProviderFlag2(long internalProviderFlag2) {
        this.internalProviderFlag2 = internalProviderFlag2;
    }

    public long getInternalProviderFlag3() {
        return internalProviderFlag3;
    }

    public void setInternalProviderFlag3(long internalProviderFlag3) {
        this.internalProviderFlag3 = internalProviderFlag3;
    }

    public long getInternalProviderFlag4() {
        return internalProviderFlag4;
    }

    public void setInternalProviderFlag4(long internalProviderFlag4) {
        this.internalProviderFlag4 = internalProviderFlag4;
    }

    public String getInternalProviderId() {
        return internalProviderId;
    }

    public void setInternalProviderId(String internalProviderId) {
        this.internalProviderId = internalProviderId;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public PvrRecIdx getPesiPvrRecIdx() {
        // we use COLUMN_INTERNAL_PROVIDER_ID for our pvr index
        return PvrRecIdx.fromCombinedString(this.internalProviderId);
    }


    @SuppressLint("RestrictedApi")
    private static TIFRecordedProgramData fromCursor(Cursor cursor) {
        TIFRecordedProgramData recordedProgramData = new TIFRecordedProgramData();
        int index;

        if ((index = cursor.getColumnIndex(TvContractCompat.RecordedPrograms._ID)) >= 0
                && !cursor.isNull(index)) {
            recordedProgramData.setId(cursor.getLong(index));
        }
        if ((index = cursor.getColumnIndex(TvContractCompat.RecordedPrograms.COLUMN_PACKAGE_NAME)) >= 0
                && !cursor.isNull(index)) {
            recordedProgramData.setPackageName(cursor.getString(index));
        }
        if ((index = cursor.getColumnIndex(TvContractCompat.RecordedPrograms.COLUMN_INPUT_ID)) >= 0
                && !cursor.isNull(index)) {
            recordedProgramData.setInputId(cursor.getString(index));
        }
        if ((index = cursor.getColumnIndex(TvContractCompat.RecordedPrograms.COLUMN_CHANNEL_ID)) >= 0
                && !cursor.isNull(index)) {
            recordedProgramData.setChannelId(cursor.getLong(index));
        }
        if ((index = cursor.getColumnIndex(TvContractCompat.RecordedPrograms.COLUMN_TITLE)) >= 0
                && !cursor.isNull(index)) {
            recordedProgramData.setTitle(cursor.getString(index));
        }
        if ((index = cursor.getColumnIndex(TvContractCompat.RecordedPrograms.COLUMN_EPISODE_DISPLAY_NUMBER)) >= 0
                && !cursor.isNull(index)) {
            recordedProgramData.setEpisodeDisplayNumber(cursor.getString(index));
        }
        if ((index = cursor.getColumnIndex(TvContractCompat.RecordedPrograms.COLUMN_EPISODE_TITLE)) >= 0
                && !cursor.isNull(index)) {
            recordedProgramData.setEpisodeTitle(cursor.getString(index));
        }
        if ((index = cursor.getColumnIndex(TvContractCompat.RecordedPrograms.COLUMN_START_TIME_UTC_MILLIS)) >= 0
                && !cursor.isNull(index)) {
            recordedProgramData.setStartTimeUtcMillis(cursor.getLong(index));
        }
        if ((index = cursor.getColumnIndex(TvContractCompat.RecordedPrograms.COLUMN_END_TIME_UTC_MILLIS)) >= 0
                && !cursor.isNull(index)) {
            recordedProgramData.setEndTimeUtcMillis(cursor.getLong(index));
        }
        if ((index = cursor.getColumnIndex(TvContractCompat.RecordedPrograms.COLUMN_LONG_DESCRIPTION)) >= 0
                && !cursor.isNull(index)) {
            recordedProgramData.setLongDescription(cursor.getString(index));
        }
        if ((index = cursor.getColumnIndex(TvContractCompat.RecordedPrograms.COLUMN_CONTENT_RATING)) >= 0
                && !cursor.isNull(index)) {
            recordedProgramData.setContentRatings(cursor.getString(index));
        }
        if ((index = cursor.getColumnIndex(TvContractCompat.RecordedPrograms.COLUMN_RECORDING_DATA_URI)) >= 0
                && !cursor.isNull(index)) {
            recordedProgramData.setRecordingDataUri(cursor.getString(index));
        }
        if ((index = cursor.getColumnIndex(TvContractCompat.RecordedPrograms.COLUMN_RECORDING_DATA_BYTES)) >= 0
                && !cursor.isNull(index)) {
            recordedProgramData.setRecordingDataBytes(cursor.getLong(index));
        }
        if ((index = cursor.getColumnIndex(TvContractCompat.RecordedPrograms.COLUMN_RECORDING_DURATION_MILLIS)) >= 0
                && !cursor.isNull(index)) {
            recordedProgramData.setRecordingDurationMillis(cursor.getLong(index));
        }
        if ((index = cursor.getColumnIndex(TvContractCompat.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_DATA)) >= 0
                && !cursor.isNull(index)) {
            recordedProgramData.setInternalProviderData(cursor.getBlob(index));
        }
        if ((index = cursor.getColumnIndex(TvContractCompat.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_FLAG1)) >= 0
                && !cursor.isNull(index)) {
            recordedProgramData.setInternalProviderFlag1(cursor.getLong(index));
        }
        if ((index = cursor.getColumnIndex(TvContractCompat.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_FLAG2)) >= 0
                && !cursor.isNull(index)) {
            recordedProgramData.setInternalProviderFlag2(cursor.getLong(index));
        }
        if ((index = cursor.getColumnIndex(TvContractCompat.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_FLAG3)) >= 0
                && !cursor.isNull(index)) {
            recordedProgramData.setInternalProviderFlag3(cursor.getLong(index));
        }
        if ((index = cursor.getColumnIndex(TvContractCompat.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_FLAG4)) >= 0
                && !cursor.isNull(index)) {
            recordedProgramData.setInternalProviderFlag4(cursor.getLong(index));
        }
        if ((index = cursor.getColumnIndex(TvContractCompat.RecordedPrograms.COLUMN_VERSION_NUMBER)) >= 0
                && !cursor.isNull(index)) {
            recordedProgramData.setVersionNumber(cursor.getInt(index));
        }
        if ((index = cursor.getColumnIndex(TvContract.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_ID)) >= 0
                && !cursor.isNull(index)) {
            recordedProgramData.setInternalProviderId(cursor.getString(index));
        }

        return recordedProgramData;
    }

    public static TIFRecordedProgramData fromUri(Context context, Uri recordedProgramUri) {
        if (TvContractCompat.isRecordedProgramUri(recordedProgramUri)) {
            ContentResolver contentResolver = context.getContentResolver();
            try (Cursor cursor = contentResolver.query(
                    recordedProgramUri,
                    PROJECTION,
                    null,
                    null,
                    null)) {

                if (cursor != null && cursor.moveToFirst()) {
                    return fromCursor(cursor);
                }
            } catch (Exception e) {
                Log.e(TAG, "fromUri: failed", e);
            }
        }

        return null;
    }
}
