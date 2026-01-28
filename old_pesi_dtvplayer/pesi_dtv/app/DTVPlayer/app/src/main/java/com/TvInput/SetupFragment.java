package com.TvInput;

import android.app.Activity;
import android.content.ComponentName;
import android.os.Bundle;
import android.util.Log;

import com.google.android.media.tv.companionlibrary.ChannelSetupFragment;

/**
 * Fragment which shows a sample UI for registering channels and setting up SampleJobService to
 * provide program information in the background.
 */
public class SetupFragment extends ChannelSetupFragment {
    private static final String TAG = "SetupFragment";
    public static final long FULL_SYNC_FREQUENCY_MILLIS = 1000 * 60 * 60 * 24;  // 24 hour
    private static final long FULL_SYNC_WINDOW_SEC = 1000 * 60 * 60 * 24 * 14;  // 2 weeks
    private String mInputId = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        //mInputId = getActivity().getIntent().getStringExtra(TvInputInfo.EXTRA_INPUT_ID);
        mInputId = TvInputActivity.TvInputServiceID; // Edwin 20181214 live tv cannot scan channel
    }

    @Override
    public void onScanStarted() {
        Log.d(TAG, "onScanStarted: mInputId = "+mInputId);
        EpgSyncService.cancelAllSyncRequests(getActivity());
        EpgSyncService.requestImmediateSync(getActivity(), mInputId,
                new ComponentName(getActivity(), EpgSyncService.class));
    }

    @Override
    public String getInputId() {
        Log.d(TAG, "getInputId: ");
        return mInputId;
    }

    @Override
    public void onScanFinished() {
        Log.d(TAG, "onScanFinished: ");
        EpgSyncService.cancelAllSyncRequests(getActivity());
        EpgSyncService.setUpPeriodicSync(getActivity(), mInputId,
                new ComponentName(getActivity(), EpgSyncService.class),
                FULL_SYNC_FREQUENCY_MILLIS, FULL_SYNC_WINDOW_SEC);
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish() ;
    }

    @Override
    public void onScanError(int reason) {
        System.out.println( "error!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
    }
}
