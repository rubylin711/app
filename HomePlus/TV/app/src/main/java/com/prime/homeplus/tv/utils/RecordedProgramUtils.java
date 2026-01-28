package com.prime.homeplus.tv.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.tvprovider.media.tv.Channel;
import androidx.tvprovider.media.tv.TvContractCompat;

import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceInterface;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.PvrDbRecordInfo;
import com.prime.datastructure.sysdata.PvrRecFileInfo;
import com.prime.datastructure.sysdata.PvrRecIdx;
import com.prime.dtv.service.datamanager.PvrDataManager;
import com.prime.homeplus.tv.PrimeHomeplusTvApplication;
import com.prime.homeplus.tv.data.RecordedProgramData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecordedProgramUtils {
    private static final String TAG = "RecordedProgramUtils";

    private static List<RecordedProgramData> debugRecordedPrograms = new ArrayList<>();
    private static boolean isInit = false;

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

    private static List<RecordedProgramData> getTestRecordedData() {
        List<RecordedProgramData> list = new ArrayList<>();
        for (int i = 0; i <= 5; i++) {
            RecordedProgramData data = new RecordedProgramData();
            data.setInputId(Pvcfg.getTvInputId());
            data.setId(i + 1);
            data.setChannelId(i);
            data.setRecordingDataUri("testuri:" + i);
            data.setLongDescription("long description : " + i);

            String contentRatings = "com.android.tv/DVB/DVB_6";
            if (i == 2) {
                contentRatings = "com.android.tv/DVB/DVB_12";
            } else if (i == 3) {
                contentRatings = "com.android.tv/DVB/DVB_18";
            }
            data.setContentRatings(contentRatings);

            data.setChannelNumber("" + i);
            data.setChannelName("TEST_REC_CHANNEL_" + i);
            data.setTitle("TEST_REC_TITLE_" + i);

            if (i == 0) {
                data.setEpisodeDisplayNumber("1");
                data.setEpisodeTitle("TEST_REC_EPISODE");
            }

            data.setStartTimeUtcMillis(1557385200000L);
            data.setEndTimeUtcMillis(1557388800000L);

            long recordStatus = PvrRecFileInfo.RECORD_STATUS_SUCCESS;
            if (i == 4) {
                recordStatus = PvrRecFileInfo.RECORD_STATUS_FAILED;
            } else if (i == 5) {
                recordStatus = PvrRecFileInfo.RECORD_STATUS_RECORDING;
            }

            data.setRecordingDurationMillis((i + 1) * 60000);

            data.setInternalProviderFlag1(recordStatus); // use flag1 for record status

            list.add(data);
        }
        return list;
    }

    public static List<RecordedProgramData> getRecordedPrograms(Context context) {
//        // TODO: if debugRecordedPrograms is empty, create dummy data for UI testing
//        if (debugRecordedPrograms != null && debugRecordedPrograms.isEmpty() && !isInit) {
//            isInit = true;
//            debugRecordedPrograms = getTestRecordedData();
//        }
//
//        return debugRecordedPrograms;

        List<RecordedProgramData> recordedProgramDataList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();

        String selection = TvContractCompat.RecordedPrograms.COLUMN_INPUT_ID + " = ?";
        String[] selectionArgs = new String[] {
                Pvcfg.getTvInputId()
        };
        String sortOrder = TvContractCompat.RecordedPrograms.COLUMN_START_TIME_UTC_MILLIS + " DESC";
        try (Cursor cursor = contentResolver.query(
                TvContractCompat.RecordedPrograms.CONTENT_URI,
                PROJECTION,
                selection,
                selectionArgs,
                sortOrder)) {

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    RecordedProgramData recordedProgramData = fromCursor(cursor);
                    // TIF RecordedPrograms do not have ch name and number columns
                    // get them from TIF Channel by channel id
                    // or from pesi database by pvrRecIdx
                    Channel channel = ChannelUtils.getChannelFullData(
                            context,
                            TvContractCompat.buildChannelUri(recordedProgramData.getChannelId()));
                    if (channel != null) {
                        recordedProgramData.setChannelName(channel.getDisplayName());
                        recordedProgramData.setChannelNumber(channel.getDisplayNumber());
                    } else {
                        // get from pesi database if no matched channel found in TIF database
                        PvrRecIdx pvrRecIdx = recordedProgramData.getPesiPvrRecIdx();
                        PvrDbRecordInfo pvrDbRecordInfo =
                                new PvrDataManager(context).queryDataFromTable(pvrRecIdx);
                        if (pvrDbRecordInfo != null) {
                            recordedProgramData.setChannelName(pvrDbRecordInfo.getChName());
                            recordedProgramData.setChannelNumber(
                                    String.valueOf(pvrDbRecordInfo.getChannelNo()));
                        }
                    }
                    recordedProgramDataList.add(recordedProgramData);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "getRecordedPrograms: failed", e);
        }

        return recordedProgramDataList;
    }

    public static Uri insertRecordedProgram(Context context, RecordedProgramData data) {
//        if (data == null) {
//            Log.e(TAG, "Cannot insert null RecordedProgramData");
//            return null;
//        }
//
//        if (debugRecordedPrograms != null && debugRecordedPrograms.isEmpty() && !isInit) {
//            isInit = true;
//            debugRecordedPrograms = getTestRecordedData();
//        }
//
//        if (debugRecordedPrograms != null) {
//            debugRecordedPrograms.add(data);
//        }

        Log.e(TAG, "insertRecordedProgram: RecordedProgram is inserted by TV input service\n" +
                "TV input app should not insert it");
        return null;
    }

    public static boolean deleteRecordedProgram(Context context, long recordingId) {
//        if (debugRecordedPrograms != null && !debugRecordedPrograms.isEmpty()) {
//            for (int i = 0; i <= debugRecordedPrograms.size(); i++) {
//                if (recordingId == debugRecordedPrograms.get(i).getId()) {
//                    debugRecordedPrograms.remove(i);
//                    return true;
//                }
//            }
//        }

        RecordedProgramData recordedProgramData = getRecordedProgram(context, recordingId);
        if (recordedProgramData == null) {
            Log.e(TAG, "deleteRecordedProgram: recordingId not exist");
            return false;
        }

        // delete from primeDtv
        PrimeDtvServiceInterface primeDtv = PrimeHomeplusTvApplication.get_prime_dtv_service();
        int pesiDeleteCount = -1;
        if (primeDtv != null) {
            PvrRecIdx pvrRecIdx = recordedProgramData.getPesiPvrRecIdx();
            pesiDeleteCount = primeDtv.pvr_delete_one_rec(pvrRecIdx);
        }

        // primeDtv delete fail
        if (pesiDeleteCount < 0) {
            Log.e(TAG, "deleteRecordedProgram: primeDtv delete failed, aborting TIF deletion.");
            return false;
        }

        // primeDtv delete 0 but TIF has a record
        if (pesiDeleteCount == 0) {
            Log.w(TAG, "deleteRecordedProgram: TIF has a record but primeDtv deleted 0 files. " +
                    "Potential error or mismatch.");
            if (isRecordingFileExist(recordedProgramData.getRecordingDataUri())) {
                Log.e(TAG, "deleteRecordedProgram: file still exist, aborting TIF deletion.");
                return false;
            }
        }

        // primeDtv delete more than 1 file
        if (pesiDeleteCount > 1) {
            Log.w(TAG, "deleteRecordedProgram: primeDtv deleted more than 1 files, " +
                    "pesiDeleteCount = " + pesiDeleteCount);
        }

        // delete from TIF
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = TvContractCompat.buildRecordedProgramUri(recordingId);
        int tifDeleteCount = contentResolver.delete(uri, null, null);
        if (tifDeleteCount > 0) {
            Log.d(TAG, "deleteRecordedProgram: Successfully delete " +
                    pesiDeleteCount + " records from primeDtv and " +
                    tifDeleteCount + " records from TIF");
            return true;
        } else {
            Log.e(TAG, "deleteRecordedProgram: primeDtv might have deleted file, " +
                    "but TIF record removal failed.");
            return false;
        }
    }

    public static void deleteAllRecordedProgram(Context context) {
//        if (debugRecordedPrograms != null && !debugRecordedPrograms.isEmpty()) {
//            debugRecordedPrograms.clear();
//        }

        ContentResolver contentResolver = context.getContentResolver();

        // get expected delete count from TIF
        String[] projection = new String[] {
                TvContractCompat.RecordedPrograms._ID
        };
        String selection = TvContractCompat.RecordedPrograms.COLUMN_INPUT_ID + " = ?";
        String[] selectionArgs = new String[] {
                Pvcfg.getTvInputId()
        };
        int expectedCount = 0;
        try (Cursor cursor = contentResolver.query(TvContractCompat.RecordedPrograms.CONTENT_URI,
                projection, selection, selectionArgs, null)) {
            if (cursor != null) {
                expectedCount = cursor.getCount();
            }
        }
        Log.d(TAG, "deleteAllRecordedProgram: expected delete count = " + expectedCount);

        // delete from primeDtv
        PrimeDtvServiceInterface primeDtv = PrimeHomeplusTvApplication.get_prime_dtv_service();
        int pesiDeleteCount = -1;
        if (primeDtv != null) {
            pesiDeleteCount = primeDtv.pvr_delete_all_recs();
        }

        // primeDtv delete fail
        if (pesiDeleteCount < 0) {
            Log.e(TAG, "deleteAllRecordedProgram: primeDtv delete failed, aborting TIF deletion.");
            return;
        }

        // primeDtv delete 0 but TIF has records
        if (expectedCount > 0 && pesiDeleteCount == 0) {
            Log.w(TAG, "deleteAllRecordedProgram: TIF has records but primeDtv deleted 0 files. " +
                    "Potential error or mismatch.");
            List<RecordedProgramData> recordedProgramDataList = getRecordedPrograms(context);
            for (RecordedProgramData data : recordedProgramDataList) {
                if (isRecordingFileExist(data.getRecordingDataUri())) {
                    Log.e(TAG, "deleteAllRecordedProgram: file still exist, aborting TIF deletion.");
                    return;
                }
            }
        }

        // delete from TIF
        int tifDeleteCount = contentResolver.delete(
                TvContractCompat.RecordedPrograms.CONTENT_URI,
                selection,
                selectionArgs);
        Log.d(TAG, "deleteAllRecordedProgram: Successfully delete " +
                pesiDeleteCount + " records from primeDtv and " +
                tifDeleteCount + " records from TIF");
    }

    public static RecordedProgramData getRecordedProgram(Context context, long recordingId) {
//        RecordedProgramData data = new RecordedProgramData();
//        if (debugRecordedPrograms != null && !debugRecordedPrograms.isEmpty()) {
//            for (int i = 0; i < debugRecordedPrograms.size(); i++) {
//                if (recordingId == debugRecordedPrograms.get(i).getId()) {
//                    data = debugRecordedPrograms.get(i);
//                    return data;
//                }
//            }
//        }

        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = TvContractCompat.buildRecordedProgramUri(recordingId);
        try (Cursor cursor = contentResolver.query(
                uri,
                PROJECTION,
                null,
                null,
                null)) {

            if (cursor != null && cursor.moveToFirst()) {
                RecordedProgramData recordedProgramData = fromCursor(cursor);
                // TIF RecordedPrograms do not have ch name and number columns
                // get them from TIF Channel by channel id
                Channel channel = ChannelUtils.getChannelFullData(
                        context,
                        TvContractCompat.buildChannelUri(recordedProgramData.getChannelId()));
                if (channel != null) {
                    recordedProgramData.setChannelName(channel.getDisplayName());
                    recordedProgramData.setChannelNumber(channel.getDisplayNumber());
                }

//                Log.d(TAG, "getRecordedProgram: " + recordedProgramData);
                return recordedProgramData;
            }
        } catch (Exception e) {
            Log.e(TAG, "getRecordedProgram: failed", e);
        }

        return null;
    }

    public static List<RecordedProgramData> getSeriesRecordedPrograms(Context context, String episodeName) {
//        List<RecordedProgramData> list = new ArrayList<>();
//        if (debugRecordedPrograms != null) {
//            for (RecordedProgramData oldRecordedProgram : debugRecordedPrograms) {
//                if (episodeName.equals(oldRecordedProgram.getEpisodeTitle())) {
//                    list.add(oldRecordedProgram);
//                }
//            }
//        }
//        return list;

        List<RecordedProgramData> recordedProgramDataList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();

        String selection =
                TvContractCompat.RecordedPrograms.COLUMN_INPUT_ID + " = ? AND " +
                TvContractCompat.RecordedPrograms.COLUMN_EPISODE_TITLE + " = ?";
        String[] selectionArgs = new String[] {
                Pvcfg.getTvInputId(),
                episodeName
        };
        String sortOrder = TvContractCompat.RecordedPrograms.COLUMN_EPISODE_DISPLAY_NUMBER + " ASC";
        try (Cursor cursor = contentResolver.query(
                TvContractCompat.RecordedPrograms.CONTENT_URI,
                PROJECTION,
                selection,
                selectionArgs,
                sortOrder)) {

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    RecordedProgramData recordedProgramData = fromCursor(cursor);
                    // TIF RecordedPrograms do not have ch name and number columns
                    // get them from TIF Channel by channel id
                    Channel channel = ChannelUtils.getChannelFullData(
                            context,
                            TvContractCompat.buildChannelUri(recordedProgramData.getChannelId()));
                    if (channel != null) {
                        recordedProgramData.setChannelName(channel.getDisplayName());
                        recordedProgramData.setChannelNumber(channel.getDisplayNumber());
                    }
                    recordedProgramDataList.add(recordedProgramData);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "getSeriesRecordedPrograms: failed", e);
        }

        return recordedProgramDataList;
    }

    private static RecordedProgramData fromCursor(Cursor cursor) {
        RecordedProgramData recordedProgramData = new RecordedProgramData();
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

    private static boolean isRecordingFileExist(String dataUriString) {
        if (TextUtils.isEmpty(dataUriString)) {
            return false;
        }

        try {
            Uri uri = Uri.parse(dataUriString);
            String path = null;

            if ("file".equals(uri.getScheme())) {
                path = uri.getPath();
            }

            if (path != null) {
                File file = new File(path);
                return file.exists() && file.isFile();
            }
        } catch (Exception e) {
            Log.e("isRecordingFileExist", "Error checking file existence: " + dataUriString, e);
        }
        return false;
    }

    public static long getCurrentRecordedProgramStatus(Context context, long recordingId) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = TvContractCompat.buildRecordedProgramUri(recordingId);
        String[] projection = new String[] {
                TvContractCompat.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_FLAG1
        };

        try (Cursor cursor = contentResolver.query(
                uri,
                projection,
                null,
                null,
                null)) {

            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(
                        TvContractCompat.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_FLAG1);
                if (index >= 0 && !cursor.isNull(index)) {
                    return cursor.getLong(index);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getCurrentRecordedProgramStatus: failed", e);
        }

        return PvrRecFileInfo.RECORD_STATUS_FAILED;
    }

    public static long getCurrentRecordedProgramDuration(Context context, long recordingId) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = TvContractCompat.buildRecordedProgramUri(recordingId);
        String[] projection = new String[] {
                TvContractCompat.RecordedPrograms.COLUMN_RECORDING_DURATION_MILLIS
        };

        try (Cursor cursor = contentResolver.query(
                uri,
                projection,
                null,
                null,
                null)) {

            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(
                        TvContractCompat.RecordedPrograms.COLUMN_RECORDING_DURATION_MILLIS);
                if (index >= 0 && !cursor.isNull(index)) {
                    return cursor.getLong(index);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getCurrentRecordedProgramDuration: failed", e);
        }

        return 0;
    }
}