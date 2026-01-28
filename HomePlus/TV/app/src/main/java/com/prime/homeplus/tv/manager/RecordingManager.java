package com.prime.homeplus.tv.manager;

import android.content.Context;
import android.media.tv.TvRecordingClient;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.tvprovider.media.tv.TvContractCompat;

import com.prime.datastructure.CommuincateInterface.PvrModule;
import com.prime.datastructure.config.Pvcfg;
import com.prime.homeplus.tv.data.ScheduledProgramData;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

public class RecordingManager {
    private static final String TAG = "RecordingManager";
    private static final int MAX_RECORDING_COUNT = Pvcfg.NUM_OF_RECORDING;

    private static volatile RecordingManager sInstance;
    private final Context mContext;

    // Key: id (Long), Value: TvRecordingClient
    private final Map<Long, TvRecordingClient> mActiveClients = new ConcurrentHashMap<>();
    private final Set<OnRecordingStateChangeListener> mListeners = new CopyOnWriteArraySet<>();

    public interface OnRecordingStateChangeListener {
        void onRecordingStopped(long bookId);
    }

    private RecordingManager(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public static RecordingManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (RecordingManager.class) {
                if (sInstance == null) {
                    sInstance = new RecordingManager(context);
                }
            }
        }
        return sInstance;
    }

    /**
     * start recording
     * @param scheduledProgram ScheduledProgramData include id, inputId, channelId, ...
     */
    public synchronized boolean startRecording(ScheduledProgramData scheduledProgram) {
        Log.d(TAG, "startRecording: ");
        if (scheduledProgram == null) return false;

        long scheduledProgramId = scheduledProgram.getId();

        if (mActiveClients.size() >= MAX_RECORDING_COUNT) {
            Log.w(TAG, "max recording reached, max = " + MAX_RECORDING_COUNT);
            return false;
        }

        if (mActiveClients.containsKey(scheduledProgramId)) {
            Log.w(TAG, "client already exist, ID: " + scheduledProgramId);
            return false;
        }

        try {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            TvRecordingClient client = new TvRecordingClient(mContext,
                    "TvRecordingClient_" + scheduledProgramId,
                    new RecordingCallbackImpl(scheduledProgram), mainHandler);

            Uri channelUri = TvContractCompat.buildChannelUri(scheduledProgram.getChannelId());

            client.tune(scheduledProgram.getInputId(), channelUri);

            mActiveClients.put(scheduledProgramId, client);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "startRecording fail, ID: " + scheduledProgramId, e);
            mActiveClients.remove(scheduledProgramId); // for safety
            return false;
        }
    }

    /**
     * stop recording
     */
    public synchronized void stopRecording(long scheduledProgramId) {
        TvRecordingClient client = mActiveClients.get(scheduledProgramId);
        if (client != null) {
            client.stopRecording();
        }
    }

    /**
     * stop all recordings
     */
    public synchronized void stopAllRecordings() {
        for (TvRecordingClient client : mActiveClients.values()) {
            client.stopRecording();
        }
    }

    public synchronized int getRecordingCount() {
        return mActiveClients.size();
    }

    public synchronized boolean isRecording(long scheduledProgramId) {
        return mActiveClients.containsKey(scheduledProgramId);
    }

    /**
     * callback
     */
    private class RecordingCallbackImpl extends TvRecordingClient.RecordingCallback {
        private final ScheduledProgramData scheduledProgramData;

        RecordingCallbackImpl(ScheduledProgramData data) {
            this.scheduledProgramData = data;
        }

        @Override
        public void onConnectionFailed(String inputId) {
            super.onConnectionFailed(inputId);
            Log.d(TAG, "onConnectionFailed: ");
            handleCleanup();
        }

        @Override
        public void onDisconnected(String inputId) {
            super.onDisconnected(inputId);
            Log.d(TAG, "onDisconnected: ");
            handleCleanup();
        }

        @Override
        public void onTuned(Uri channelUri) {
            super.onTuned(channelUri);
            TvRecordingClient client = mActiveClients.get(scheduledProgramData.getId());
            if (client != null) {
                if (!TextUtils.isEmpty(scheduledProgramData.getEpisodeTitle())) {
                    client.sendAppPrivateCommand(PvrModule.ACTION_ENABLE_SERIES_RECORDING, null);
                }
                client.sendAppPrivateCommand(PvrModule.ACTION_UPDATE_PROGRESS_WHILE_RECORDING, null);
                client.startRecording(scheduledProgramData.getProgramUri());
                Log.d(TAG, "onTuned: record start: " + scheduledProgramData.getTitle());
            }
        }

        @Override
        public void onRecordingStopped(Uri recordedProgramUri) {
            super.onRecordingStopped(recordedProgramUri);
            Log.d(TAG, "onRecordingStopped: ID = " + scheduledProgramData.getId());
            handleCleanup();
        }

        @Override
        public void onError(int error) {
            super.onError(error);
            Log.d(TAG, "onError: " + error + " ID: " + scheduledProgramData.getId());
            handleCleanup();
        }

        private synchronized void handleCleanup() {
            long id = scheduledProgramData.getId();
            TvRecordingClient client = mActiveClients.remove(id);
            if (client != null) {
                client.release();
                for (OnRecordingStateChangeListener listener : mListeners) {
                    try {
                        listener.onRecordingStopped(id);
                    } catch (Exception e) {
                        Log.e(TAG, "Error notifying listener", e);
                    }
                }
            }
        }
    }

    public void addOnRecordingStateChangeListener(OnRecordingStateChangeListener listener) {
        if (listener != null) {
            mListeners.add(listener);
        }
    }

    public void removeOnRecordingStateChangeListener(OnRecordingStateChangeListener listener) {
        mListeners.remove(listener);
    }
}
