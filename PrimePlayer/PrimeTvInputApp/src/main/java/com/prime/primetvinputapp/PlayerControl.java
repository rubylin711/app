package com.prime.primetvinputapp;

import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_ID;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.media.tv.TvRecordingClient;
import android.media.tv.TvTrackInfo;
import android.media.tv.TvView;
import android.net.Uri;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.prime.datastructure.CommuincateInterface.AvModule;
import com.prime.datastructure.TIF.TIFEpgData;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.utils.LogUtils;
import com.prime.dtv.database.DBChannelFunc;

import java.util.List;
import java.util.Map;

public class PlayerControl {
    public static final String TAG = "PlayerControl";

    private String mTifServiceName = "com.prime.tvinputframework.PrimeTvInputService" ;

    private Context mContext;
    private TvView mTvView;
    private TvInputManager mManager;
    private TvInputInfo mStubInfo;
    private MockCallback mCallback = new MockCallback();
    private Uri mCurrentTIFChannelDataUri ;
    private static PlayerControl mPlayerControl = null;
    private TvRecordingClient mTvRecordingClient;

    public static PlayerControl getInstance(Context context) {
        if(mPlayerControl == null) {
            mPlayerControl = new PlayerControl(context);
        }
        return mPlayerControl;
    }

    public PlayerControl(Context context) {
        mContext = context;
    }

    public void play(ProgramInfo programInfo) {

    }

    public void av_control_play_by_channel_id(int playId, Uri uri, long channelId, int groupType, int show) {
        Bundle requestBundle = new Bundle();
        requestBundle.putInt(COMMAND_ID, AvModule.CMD_ServicePlayer_AV_PlayByChannelId);
        requestBundle.putInt(AvModule.PlayId_string,playId);
        requestBundle.putLong(ProgramInfo.CHANNEL_ID,channelId);
        requestBundle.putInt(ProgramInfo.TYPE,groupType);
        requestBundle.putInt(AvModule.Show_string,show);
        LogUtils.d("playercontrol "+"111 av_control_play_by_channel_id channelId = "+channelId);
        startTune(requestBundle,uri);
    }

    public void pvr_record_start(Uri channelUri) {
        if (mTvRecordingClient != null) {
            mTvRecordingClient.release();
        }

        mTvRecordingClient = new TvRecordingClient(
                mContext,
                "recording test",
                new TvRecordingClient.RecordingCallback() {
                    @Override
                    public void onConnectionFailed(String inputId) {
                        Log.d(TAG, "onConnectionFailed: " + inputId);
                    }

                    @Override
                    public void onDisconnected(String inputId) {
                        Log.d(TAG, "onDisconnected: " + inputId);
                    }

                    @Override
                    public void onError(int error) {
                        Log.d(TAG, "onError: " + error);
                    }

                    @Override
                    public void onRecordingStopped(Uri recordedProgramUri) {
                        Log.d(TAG, "onRecordingStopped: " + recordedProgramUri);
                        mTvRecordingClient.release();
                    }

                    @Override
                    public void onTuned(Uri channelUri) {
                        Log.d(TAG, "onTuned: " + channelUri);
                        long channelId = ContentUris.parseId(channelUri);
                        long timeMillis = System.currentTimeMillis();
                        Uri programUri = TIFEpgData.getProgramUri(mContext, channelId, timeMillis);
                        mTvRecordingClient.startRecording(programUri);
                    }
                },
                null
        );

        mTvRecordingClient.tune(mStubInfo.getId(), channelUri);
    }

    public void pvr_record_stop() {
        if (mTvRecordingClient != null) {
            mTvRecordingClient.stopRecording();
        }
    }

    private static class MockCallback extends TvView.TvInputCallback {
        private final Map<String, Boolean> mVideoAvailableMap = new ArrayMap<>();
        private final Map<String, SparseIntArray> mSelectedTrackGenerationMap = new ArrayMap<>();
        private final Map<String, Integer> mTracksGenerationMap = new ArrayMap<>();
        private final Object mLock = new Object();
        private volatile int mConnectionFailedCount;
        private volatile int mDisconnectedCount;

        public boolean isVideoAvailable(String inputId) {
            synchronized (mLock) {
                Boolean available = mVideoAvailableMap.get(inputId);
                return available == null ? false : available.booleanValue();
            }
        }

        public int getSelectedTrackGeneration(String inputId, int type) {
            synchronized (mLock) {
                SparseIntArray selectedTrackGenerationMap =
                        mSelectedTrackGenerationMap.get(inputId);
                if (selectedTrackGenerationMap == null) {
                    return 0;
                }
                return selectedTrackGenerationMap.get(type, 0);
            }
        }

        public void resetCount() {
            mConnectionFailedCount = 0;
            mDisconnectedCount = 0;
        }

        public int getConnectionFailedCount() {
            return mConnectionFailedCount;
        }

        public int getDisconnectedCount() {
            return mDisconnectedCount;
        }

        @Override
        public void onConnectionFailed(String inputId) {
            Log.e(TAG, "Connection failed for inputId: " + inputId);
            mConnectionFailedCount++;
        }

        @Override
        public void onDisconnected(String inputId) {
            Log.w(TAG, "Disconnected from inputId: " + inputId);
            mDisconnectedCount++;
        }

        @Override
        public void onChannelRetuned(String inputId, Uri channelUri) {
            Log.d(TAG, "Channel retuned: " + channelUri);
        }

        @Override
        public void onVideoAvailable(String inputId) {
            Log.d(TAG, "Video available for inputId: " + inputId);
            synchronized (mLock) {
                mVideoAvailableMap.put(inputId, true);
            }
        }

        @Override
        public void onVideoUnavailable(String inputId, int reason) {
            Log.e(TAG, "Video unavailable for inputId: " + inputId + ", reason: " + reason);
            synchronized (mLock) {
                mVideoAvailableMap.put(inputId, false);
            }
        }

        @Override
        public void onTrackSelected(String inputId, int type, String trackId) {
            synchronized (mLock) {
                SparseIntArray selectedTrackGenerationMap =
                        mSelectedTrackGenerationMap.get(inputId);
                if (selectedTrackGenerationMap == null) {
                    selectedTrackGenerationMap = new SparseIntArray();
                    mSelectedTrackGenerationMap.put(inputId, selectedTrackGenerationMap);
                }
                int currentGeneration = selectedTrackGenerationMap.get(type, 0);
                selectedTrackGenerationMap.put(type, currentGeneration + 1);
            }
        }

        @Override
        public void onTracksChanged(String inputId, List<TvTrackInfo> trackList) {
            synchronized (mLock) {
                Integer tracksGeneration = mTracksGenerationMap.get(inputId);
                mTracksGenerationMap.put(inputId,
                        tracksGeneration == null ? 1 : (tracksGeneration + 1));
            }
        }
    }

    public void initTvInputManager() {
        mManager = (TvInputManager) mContext.getSystemService(Context.TV_INPUT_SERVICE);
        Log.d( TAG, "ggggg mDisplayTifServiceName = " + mTifServiceName ) ;
        for (TvInputInfo info : mManager.getTvInputList()) {
            Log.d( TAG, "ggggg info.getServiceInfo().name = " + info.getServiceInfo().name ) ;
            if (info.getServiceInfo().name.equals(mTifServiceName)) {
                mStubInfo = info;
                PrimeTvInputAppApplication.setTvInputId(mStubInfo.getId());
                Log.d( TAG, "ggggg mStubInfo = " + mStubInfo.getServiceInfo().name + " mStubInfo.getId() = "+mStubInfo.getId()) ;
            }

            if (mStubInfo != null) {
                break;
            }
        }
    }

    public boolean setTvView(TvView tvView)
    {
        Log.d(TAG,"setTvView");
        mTvView = tvView;
        if ( mStubInfo == null ) {
            return false;
        }
        mTvView.setCallback(mCallback);
        return true;
    }

    private static String stateToString(int state) {
        switch (state) {
            case TvInputManager.INPUT_STATE_CONNECTED:
                return "CONNECTED";
            case TvInputManager.INPUT_STATE_DISCONNECTED:
                return "DISCONNECTED";
            case TvInputManager.INPUT_STATE_CONNECTED_STANDBY:
                return "CONNECTED_STANDBY";
            default:
                return "UNKNOWN";
        }
    }

    private void startTune(Bundle params,Uri uri)
    {
        if ( mCurrentTIFChannelDataUri != null && mCurrentTIFChannelDataUri.equals(uri) && mCallback.isVideoAvailable(mStubInfo.getId()) )
            return ;

        stopTIFCHannel() ;
        mCallback = new MockCallback();
        mTvView.setVisibility(View.VISIBLE);
        mTvView.setCallback(mCallback);
        TvInputManager tvInputManager = (TvInputManager) mContext.getSystemService(Context.TV_INPUT_SERVICE);
        if (mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LIVE_TV)) {
            Log.d(TAG, "Device supports Live TV");
        } else {
            Log.e(TAG, "Device does not support Live TV");
        }
        for (TvInputInfo input : tvInputManager.getTvInputList()) {
            Log.d(TAG, "Input ID: " + input.getId());
            int state = tvInputManager.getInputState(input.getId());
            Log.d(TAG, "Input state: " + stateToString(state));
        }
        if (params != null) {
            Log.d(TAG,"playercontrol startTune mStubInfo.getId() = "+mStubInfo.getId()+ " uri = "+uri+
                    " params = "+params);
//            ContentValues values = new ContentValues();
//            values.put(TvContract.Channels.COLUMN_DISPLAY_NUMBER, "1");
//            mContext.getContentResolver().update(uri,values, TvContract.Channels._ID+" = ?", new String[]{"4007557"});
//            mTvView.tune(mStubInfo.getId(), uri);
            mTvView.tune(mStubInfo.getId(), uri, params);
        } else {
            mTvView.tune(mStubInfo.getId(), uri);
        }
        mCurrentTIFChannelDataUri = uri;
    }

    public void stopTIFCHannel()
    {
        Log.d(TAG,"setTvView reset");
        mTvView.reset(); // cacel tune tif channel
    }

    public Uri getTvInputUri() {
        Log.d(TAG,"ggggg getTvInputUri mStubInfo = "+mStubInfo);
        if(mStubInfo != null) {
            Uri uri = TvContract.buildChannelsUriForInput(mStubInfo.getId());
            Log.d(TAG,"ggggg uri = "+uri);
            return uri;
        }
        else {
            return null;
        }
    }
}
