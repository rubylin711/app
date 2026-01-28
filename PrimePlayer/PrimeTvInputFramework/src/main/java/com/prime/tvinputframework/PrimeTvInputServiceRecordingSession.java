package com.prime.tvinputframework;

import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_FAIL;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_SUCCESS;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.media.tv.TvContract;
import android.media.tv.TvInputManager;
import android.media.tv.TvInputService;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceInterface;
import com.prime.datastructure.CommuincateInterface.PvrModule;
import com.prime.datastructure.TIF.TIFChannelData;
import com.prime.datastructure.TIF.TIFEpgData;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.PvrDbRecordInfo;
import com.prime.datastructure.sysdata.PvrRecFileInfo;
import com.prime.datastructure.sysdata.PvrRecIdx;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.utils.TVMessage;
import com.prime.datastructure.utils.TVTunerParams;
import com.prime.dtv.service.datamanager.PvrDataManager;
import com.prime.tvinputframework.Utils.UsbUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PrimeTvInputServiceRecordingSession extends TvInputService.RecordingSession {
    private static final String TAG = "PrimeTvInputServiceRecordingSession";
    private static final Map<String, PrimeTvInputServiceRecordingSession> sActiveRecordingSessions = new ConcurrentHashMap<>();
    private static final String ACTION_ENABLE_SERIES_RECORDING = PvrModule.ACTION_ENABLE_SERIES_RECORDING;
    private static final String ACTION_UPDATE_PROGRESS_WHILE_RECORDING = PvrModule.ACTION_UPDATE_PROGRESS_WHILE_RECORDING;
    private static final long UPDATE_PROGRESS_INTERVAL_MS = 3000;

    private final PrimeDtvServiceInterface mPrimeDtvService;
    private final Context mContext;
    private final String mInputId;
    private final int mTunerId;
    private Uri mChannelUri;
    private Uri mProgramUri;
    private ProgramInfo mPesiProgramInfo;
    private long mRecordStartTime;
    private Uri mRecordedProgramUri;
    private File mRecordingFile;
    private boolean mIsSeries = false;
    private boolean mUpdateProgress = false;
    private String mUsbMountPath;
    private PvrRecIdx mPvrRecIdx;
    private String mRecordFolderName;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final Runnable mUpdateProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mRecordedProgramUri != null) {
                ContentValues contentValues = buildRecordProgressUpdateValues();
                updateRecordedProgram(contentValues);
                mHandler.postDelayed(this, UPDATE_PROGRESS_INTERVAL_MS);
            }
        }
    };

    public PrimeTvInputServiceRecordingSession(Context context, String inputId, int tunerId) {
        super(context);
        mContext = context;
        mInputId = inputId;
        mTunerId = tunerId;
        mPrimeDtvService = PrimeTvInputFrameworkApplication.get_prime_dtv_service();

        String key = getSessionKey();
        sActiveRecordingSessions.put(key, this);
    }

    @Override
    public void onTune(Uri channelUri) {
        Log.d(TAG, "onTune: channelUri = " + channelUri);
        mChannelUri = channelUri;
        int result = COMMAND_REPLY_FAIL;

        ProgramInfo programInfo = getPesiProgramInfoFromChannelUri(channelUri);
        if (programInfo != null) {
            TpInfo tpInfo = mPrimeDtvService.tp_info_get(programInfo.getTpId());
            if (tpInfo != null) {
                TVTunerParams tvTunerParams = TVTunerParams.CreateTunerParam(mTunerId, tpInfo);
                result = mPrimeDtvService.tuner_lock(tvTunerParams);
            }
        }

        if (result == COMMAND_REPLY_SUCCESS) {
            notifyTuned(channelUri);
            mPesiProgramInfo = programInfo;
        } else {
            notifyError(TvInputManager.RECORDING_ERROR_UNKNOWN);
        }
    }

    @Override
    public void onStartRecording(@Nullable Uri programUri) {
        Log.d(TAG, "onStartRecording: programUri = " + programUri);
        mProgramUri = programUri;
        if (mPrimeDtvService.pvr_change_channel_manager_is_full_fecording()) {
            Log.e(TAG, "onStartRecording: Recording limit reached.");
            notifyError(TvInputManager.RECORDING_ERROR_RESOURCE_BUSY);
            return;
        }

        if (mPrimeDtvService.pvr_change_channel_manager_is_timeshift_start() &&
                mPrimeDtvService.pvr_change_channel_manager_get_rec_num() >= Pvcfg.NUM_OF_RECORDING - 1) {
            Log.e(TAG, "onStartRecording: Time-shift + Recording limit reached.");
            notifyError(TvInputManager.RECORDING_ERROR_RESOURCE_BUSY);
            return;
        }

        if (mPesiProgramInfo == null) { // this condition should not happen
            mPesiProgramInfo = getPesiProgramInfoFromChannelUri(mChannelUri);
            if (mPesiProgramInfo == null) {
                Log.e(TAG, "onStartRecording: Null pesi program info.");
                notifyError(TvInputManager.RECORDING_ERROR_UNKNOWN);
                return;
            }
        }

        if (mPrimeDtvService.pvr_change_channel_manager_is_channel_recording(mPesiProgramInfo.getChannelId())) {
            Log.e(TAG, "onStartRecording: channel is already recording.");
            notifyError(TvInputManager.RECORDING_ERROR_RESOURCE_BUSY);
            return;
        }

        mUsbMountPath = mPrimeDtvService.pvr_get_usb_mount_path();
        if (!UsbUtils.check_usb_size(mUsbMountPath)) {
            Log.e(TAG, "onStartRecording: check usb fail.");
            notifyError(TvInputManager.RECORDING_ERROR_INSUFFICIENT_SPACE);
            return;
        }

        long pesiChannelId = mPesiProgramInfo.getChannelId();
        int eventId = TIFEpgData.getIntFromUri(mContext, programUri, TvContract.Programs.COLUMN_EVENT_ID, -1);
        Log.d(TAG, "onStartRecording: pesiChannelId = " + pesiChannelId + ", eventId = " + eventId);
        // should receive TYPE_PVR_RECORDING_START_SUCCESS/ERROR later if command sent successfully
        boolean success = mPrimeDtvService.pvr_change_channel_manager_record_start(
                pesiChannelId, eventId, 0, mIsSeries);

        if (!success) {
            Log.e(TAG, "onStartRecording: start recording fails.");
            notifyError(TvInputManager.RECORDING_ERROR_UNKNOWN);
        }
    }

    @Override
    public void onStopRecording() {
        Log.d(TAG, "onStopRecording: ");
        if (mRecordedProgramUri != null) {
            // should receive TYPE_PVR_RECORDING_STOP_SUCCESS/ERROR later
            mPrimeDtvService.pvr_change_channel_manager_record_stop(mPesiProgramInfo.getChannelId());
        } else {
            Log.e(TAG, "onStopRecording: mRecordedProgramUri is null, recording likely failed to start or already stopping.");
            notifyError(TvInputManager.RECORDING_ERROR_UNKNOWN);
        }
    }

    @Override
    public void onAppPrivateCommand(@NonNull String action, Bundle data) {
        super.onAppPrivateCommand(action, data);

        switch (action) {
            case ACTION_ENABLE_SERIES_RECORDING -> {
                Log.d(TAG, "onAppPrivateCommand: series recording enabled");
                mIsSeries = true;
            }
            case ACTION_UPDATE_PROGRESS_WHILE_RECORDING -> {
                Log.d(TAG, "onAppPrivateCommand: update progress while recording in TIF database");
                mUpdateProgress = true;
            }
            default -> Log.w(TAG, "onAppPrivateCommand: unsupported action = " + action);
        }
    }

    @Override
    public void onRelease() {
        Log.d(TAG, "onRelease: ");

        if (mRecordedProgramUri != null && mPesiProgramInfo != null) {
            Log.w(TAG, "onRelease: Still recording. Forcing PVR stop.");
            mPrimeDtvService.pvr_change_channel_manager_record_stop(mPesiProgramInfo.getChannelId());

            ContentValues contentValues = buildRecordStopContentValuesWithError();
            updateRecordedProgram(contentValues);
        }

        mHandler.removeCallbacksAndMessages(null);
        sActiveRecordingSessions.remove(getSessionKey());
    }

    private ProgramInfo getPesiProgramInfoFromChannelUri(Uri channelUri) {
        ProgramInfo programInfo = null;

        Bundle bundle = TIFChannelData.getChannelSIdOnIdTsIdFromUri(mContext, channelUri);
        if (bundle != null) {
            int sid = bundle.getInt(ProgramInfo.SERVICE_ID,0);
            int onid = bundle.getInt(ProgramInfo.ORIGINAL_NETWORK_ID,0);
            int tsid = bundle.getInt(ProgramInfo.TRANSPORT_STREAM_ID,0);
            programInfo = mPrimeDtvService.get_program_by_SId_OnId_TsId(sid, onid, tsid);
        }

        return programInfo;
    }

    private Uri insertRecordedProgram(ContentValues contentValues) {
        Log.d(TAG, "insertRecordedProgram: " + contentValues);
        return mContext.getContentResolver().insert(
                TvContract.RecordedPrograms.CONTENT_URI, contentValues);
    }

    private void updateRecordedProgram(ContentValues contentValues) {
        Log.d(TAG, "updateRecordedProgram: " + contentValues);
        mContext.getContentResolver().update(
                mRecordedProgramUri, contentValues, null, null);
    }

    private String getSessionKey() {
        return mInputId + "_" + mTunerId;
    }

    public static void handleTvMessageFromPrime(TVMessage msg) {
        if (!sActiveRecordingSessions.isEmpty()) {
            for (PrimeTvInputServiceRecordingSession session : sActiveRecordingSessions.values()) {
                if (session != null) {
                    session.onInternalMessage(msg);
                }
            }
        } else {
            Log.w(TAG, "handleTvMessageFromPrime: No active RecordingSession to deliver message.");
        }
    }

    private void onInternalMessage(TVMessage msg) {
        if (msg.getMsgFlag() != TVMessage.FLAG_PVR) {
            return;
        }

        int msgType = msg.getMsgType();
        Log.d(TAG, "onInternalMessage session = " + this + " MsgType = " + msgType);
        int msgRecTunerId = msg.getRecId();
        switch (msgType) {
            case TVMessage.TYPE_PVR_RECORDING_START_SUCCESS -> {
                if (msgRecTunerId != mTunerId) {
                    return;
                }
                Log.d(TAG, "onInternalMessage: TYPE_PVR_RECORDING_START_SUCCESS");
                mPvrRecIdx = msg.getPvrRecIdx();
                PvrDbRecordInfo pvrDbRecordInfo = new PvrDataManager(mContext).queryDataFromTable(mPvrRecIdx);
                mRecordingFile = new File(mUsbMountPath + pvrDbRecordInfo.getFullNamePath());
                mRecordFolderName = mRecordingFile.getParent();

                Log.d(TAG, "mRecordFolderName="+mRecordFolderName);
                mRecordStartTime = System.currentTimeMillis();
                ContentValues contentValues = buildRecordStartContentValues();
                mRecordedProgramUri = insertRecordedProgram(contentValues);

                if (mUpdateProgress) {
                    mHandler.removeCallbacks(mUpdateProgressRunnable);
                    mHandler.postDelayed(mUpdateProgressRunnable, UPDATE_PROGRESS_INTERVAL_MS);
                }
            }
            case TVMessage.TYPE_PVR_RECORDING_STOP_SUCCESS -> {
                if (msgRecTunerId != mTunerId) {
                    return;
                }
                Log.d(TAG, "onInternalMessage: TYPE_PVR_RECORDING_STOP_SUCCESS");
                mHandler.removeCallbacks(mUpdateProgressRunnable);

                if (mRecordedProgramUri != null) {
                    ContentValues contentValues = buildRecordStopContentValuesWithSuccess();
                    updateRecordedProgram(contentValues);
                    notifyRecordingStopped(mRecordedProgramUri);
                    mRecordedProgramUri = null;
                } else {
                    Log.e(TAG, "STOP_SUCCESS received but mRecordedProgramUri is null. Skipping update.");
                    notifyRecordingStopped(null);
                }
            }
            case TVMessage.TYPE_PVR_RECORDING_START_ERROR -> {
                if (msgRecTunerId != mTunerId) {
                    return;
                }
                Log.d(TAG, "onInternalMessage: TYPE_PVR_RECORDING_START_ERROR");
                notifyError(TvInputManager.RECORDING_ERROR_UNKNOWN);
            }
            case TVMessage.TYPE_PVR_RECORDING_STOP_ERROR -> {
                if (msgRecTunerId != mTunerId) {
                    return;
                }
                Log.d(TAG, "onInternalMessage: TYPE_PVR_RECORDING_STOP_ERROR");
                mHandler.removeCallbacks(mUpdateProgressRunnable);

                if (mRecordedProgramUri != null) {
                    ContentValues contentValues = buildRecordStopContentValuesWithError();
                    updateRecordedProgram(contentValues);

                    notifyError(TvInputManager.RECORDING_ERROR_UNKNOWN);
                    notifyRecordingStopped(mRecordedProgramUri);
                    mRecordedProgramUri = null;
                } else {
                    Log.e(TAG, "STOP_ERROR received but mRecordedProgramUri is null. Skipping update.");
                    notifyError(TvInputManager.RECORDING_ERROR_UNKNOWN);
                    notifyRecordingStopped(null);
                }
            }
            case TVMessage.TYPE_PVR_REC_DISK_FULL -> {
                Log.d(TAG, "onInternalMessage: TYPE_PVR_REC_DISK_FULL");
                if (mRecordedProgramUri != null) {
                    notifyError(TvInputManager.RECORDING_ERROR_INSUFFICIENT_SPACE);

                    mPrimeDtvService.pvr_change_channel_manager_record_stop(mPesiProgramInfo.getChannelId());
                    ContentValues contentValues = buildRecordStopContentValuesWithError();
                    updateRecordedProgram(contentValues);

                    notifyRecordingStopped(mRecordedProgramUri);
                    mRecordedProgramUri = null; // set to null so STOP_SUCCESS/ERROR won't update recorded program again
                }
            }
            default -> {}
        }
    }

    private ContentValues buildRecordStartContentValues() {
        ContentValues contentValues = new ContentValues();

        // input id
        contentValues.put(TvContract.RecordedPrograms.COLUMN_INPUT_ID, mInputId);

        // channel id
        long tifChannelId = ContentUris.parseId(mChannelUri);
        contentValues.put(TvContract.RecordedPrograms.COLUMN_CHANNEL_ID, tifChannelId);

        // data uri
        String dataUri = Uri.fromFile(mRecordingFile).toString();
        contentValues.put(TvContract.RecordedPrograms.COLUMN_RECORDING_DATA_URI, dataUri);

        // record start time
        contentValues.put(TvContract.RecordedPrograms.COLUMN_START_TIME_UTC_MILLIS, mRecordStartTime);

        if (mProgramUri == null) {
            Uri programUriByTime = TIFEpgData.getProgramUri(mContext, tifChannelId, mRecordStartTime);

            // title - use channel display name if no program
            String title = TIFChannelData.getStringFromUri(
                    mContext,
                    mChannelUri,
                    TvContract.Channels.COLUMN_DISPLAY_NAME);
            contentValues.put(TvContract.RecordedPrograms.COLUMN_TITLE, title);

            // content ratings - rating from the program at the start of recording
            String contentRatings = TIFEpgData.getStringFromUri(
                    mContext, programUriByTime, TvContract.Programs.COLUMN_CONTENT_RATING);
            contentValues.put(TvContract.Programs.COLUMN_CONTENT_RATING, contentRatings);

            // short description - description from the program at the start of recording
            String shortDescription = TIFEpgData.getStringFromUri(
                    mContext, programUriByTime, TvContract.Programs.COLUMN_SHORT_DESCRIPTION);
            contentValues.put(
                    TvContract.RecordedPrograms.COLUMN_SHORT_DESCRIPTION,
                    shortDescription);

            // long description - description from the program at the start of recording
            String longDescription = TIFEpgData.getStringFromUri(
                    mContext, programUriByTime, TvContract.Programs.COLUMN_LONG_DESCRIPTION);
            contentValues.put(
                    TvContract.RecordedPrograms.COLUMN_LONG_DESCRIPTION,
                    longDescription);
        } else {
            // title
            String title = TIFEpgData.getStringFromUri(
                    mContext,
                    mProgramUri,
                    TvContract.Programs.COLUMN_TITLE);
            contentValues.put(TvContract.RecordedPrograms.COLUMN_TITLE, title);

            // put episode title and display num for series recording
            if (mIsSeries) {
                String episodeTitle = TIFEpgData.getStringFromUri(
                        mContext, mProgramUri, TvContract.Programs.COLUMN_EPISODE_TITLE);
                contentValues.put(TvContract.RecordedPrograms.COLUMN_EPISODE_TITLE, episodeTitle);

                String episodeDisplayNum = TIFEpgData.getStringFromUri(
                        mContext, mProgramUri, TvContract.Programs.COLUMN_EPISODE_DISPLAY_NUMBER);
                contentValues.put(
                        TvContract.RecordedPrograms.COLUMN_EPISODE_DISPLAY_NUMBER,
                        episodeDisplayNum);
            }

            // content ratings
            String contentRatings = TIFEpgData.getStringFromUri(
                    mContext, mProgramUri, TvContract.Programs.COLUMN_CONTENT_RATING);
            contentValues.put(TvContract.Programs.COLUMN_CONTENT_RATING, contentRatings);

            // short description
            String shortDescription = TIFEpgData.getStringFromUri(
                    mContext, mProgramUri, TvContract.Programs.COLUMN_SHORT_DESCRIPTION);
            contentValues.put(
                    TvContract.RecordedPrograms.COLUMN_SHORT_DESCRIPTION,
                    shortDescription);

            // long description
            String longDescription = TIFEpgData.getStringFromUri(
                    mContext, mProgramUri, TvContract.Programs.COLUMN_LONG_DESCRIPTION);
            contentValues.put(
                    TvContract.RecordedPrograms.COLUMN_LONG_DESCRIPTION,
                    longDescription);
        }

        // use COLUMN_INTERNAL_PROVIDER_ID to store our pvr rec index
        String combinedIdxString = mPvrRecIdx.toCombinedString();
        contentValues.put(
                TvContract.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_ID,
                combinedIdxString);

        // set flag1 to RECORD_STATUS_RECORDING
        contentValues.put(
                TvContract.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_FLAG1,
                PvrRecFileInfo.RECORD_STATUS_RECORDING);
        return contentValues;
    }

    private ContentValues buildRecordStopContentValuesForUpdate() {
        ContentValues contentValues = new ContentValues();

        // record file size
        //long dataBytes = mRecordingFile == null ? 0 : mRecordingFile.length();
        long dataBytes = mRecordingFile == null ? 0 : getTotalFilesSize(mRecordFolderName);
        Log.d(TAG, "buildRecordStopContentValuesForUpdate dataBytes="+dataBytes);
        contentValues.put(TvContract.RecordedPrograms.COLUMN_RECORDING_DATA_BYTES, dataBytes);

        // record end time
        long endTime = System.currentTimeMillis();
        contentValues.put(TvContract.RecordedPrograms.COLUMN_END_TIME_UTC_MILLIS, endTime);

        // record duration
        long duration = endTime - mRecordStartTime;
        contentValues.put(TvContract.RecordedPrograms.COLUMN_RECORDING_DURATION_MILLIS, duration);

        // TODO: update highest content rating and descriptions if mProgramUri = null?

        return contentValues;
    }

    private ContentValues buildRecordStopContentValuesWithSuccess() {
        ContentValues contentValues = buildRecordStopContentValuesForUpdate();

        // set flag1 to RECORD_STATUS_SUCCESS
        contentValues.put(
                TvContract.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_FLAG1,
                PvrRecFileInfo.RECORD_STATUS_SUCCESS);

        return contentValues;
    }

    private ContentValues buildRecordStopContentValuesWithError() {
        ContentValues contentValues = buildRecordStopContentValuesForUpdate();

        // set flag1 to RECORD_STATUS_FAILED
        contentValues.put(
                TvContract.RecordedPrograms.COLUMN_INTERNAL_PROVIDER_FLAG1,
                PvrRecFileInfo.RECORD_STATUS_FAILED);

        return contentValues;
    }

    private ContentValues buildRecordProgressUpdateValues() {
        ContentValues contentValues = new ContentValues();

        // record file size
        //long dataBytes = mRecordingFile == null ? 0 : mRecordingFile.length();
        long dataBytes = mRecordingFile == null ? 0 : getTotalFilesSize(mRecordFolderName);
        Log.d(TAG, "buildRecordProgressUpdateValues dataBytes="+dataBytes);
        contentValues.put(TvContract.RecordedPrograms.COLUMN_RECORDING_DATA_BYTES, dataBytes);

        // record current end time
        long currentTime = System.currentTimeMillis();
        contentValues.put(TvContract.RecordedPrograms.COLUMN_END_TIME_UTC_MILLIS, currentTime);

        // record duration
        long duration = currentTime - mRecordStartTime;
        contentValues.put(TvContract.RecordedPrograms.COLUMN_RECORDING_DURATION_MILLIS, duration);
        
        return contentValues;
    }

    private static long getTotalFilesSize(String baseNamePath) {
        if (baseNamePath == null || baseNamePath.isEmpty()) return 0L;

        File targetDir = new File(baseNamePath);

        if (!targetDir.exists() || !targetDir.isDirectory()) {
            Log.d(TAG, "getTotalFilesSize: Target directory error [ baseNamePath : "+baseNamePath+"]");
            return 0L;
        }

        File[] tsFiles = getTsFiles(targetDir);

        if (tsFiles == null || tsFiles.length == 0) {
            Log.d(TAG, "getTotalFilesSize: No files match");
            return 0L;
        }

        long totalBytes = 0;
        for (File f : tsFiles) {
            totalBytes += f.length();
        }
        return totalBytes;
    }

    private static File[] getTsFiles(File targetDir) {
        final String folderName = targetDir.getName();
        final String tsPrefix = folderName + ".ts";

        return targetDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name == null) return false;
                // 匹配 REC_x.ts 或 REC_x.ts_INDEX_... 且不是 .dat
                return (name.equals(tsPrefix) || name.startsWith(tsPrefix + "_INDEX_"))
                        && !name.endsWith(".dat");
            }
        });
    }
}
