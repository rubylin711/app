/*
 * Copyright 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.prime.tvinputframework;

import android.content.Context;
import android.media.tv.TvInputService;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceConnection;
import com.prime.datastructure.CommuincateInterface.PrimeDtvServiceInterface;
import com.prime.datastructure.config.Pvcfg;
import com.prime.tvinputframework.Utils.UsbUtils;

public class PrimeTvInputService extends TvInputService {
    private String mInputId;
    private Uri mChannelUri = Uri.parse("content://android.media.tv/channel");
    private PrimeTvInputServiceSession mSession;
    private Surface mSurface;
    private PrimeDtvServiceInterface mPrimeDtvService = null;
    //    private SubtitlePainter mSubtitle;
//    private PlayController mPlayController;
//    private DVRPlayController mDVRPlayController;
//    private DVRPlayController mTimeShiftPlayController;
//    private IPTVPlayController mIPTVPlayController;
//    private BasePlayController mCurrentPlayController;
    private static final String TAG = PrimeTvInputService.class.getSimpleName();

//    private Handler mHandler = new Handler(){
//        @Override
//        public void handleMessage(@NonNull Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what){
//                case PlayerManager.MSG_SUBTITLE_DATA_INFO:
//                    break;
//                case PlayerManager.MSG_SUBTITLE_BITMAP_INFO:
//                    break;
//                case PlayerManager.MSG_CC_DATA_INFO:
//                    break;
//                case PlayerManager.MSG_AUDIOTRACK_LIST_INFO:
//                    Log.d(TAG, "MSG_AUDIOTRACK_LIST_INFO ");
//                    break;
//            }
//        }
//    };
//    private PrimeDtvServiceInterface gPrimeDtvServiceInterface = null;
//    private IPrimeDtvService gPrimeDtvService = null;
    private Context mContext = null;
    private PrimeDtvServiceConnection gPrimeDtvServiceConn;
    private String mSessionId = null;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this ;
        Log.d(TAG, "onCreate V2");
//        gPrimeDtvServiceInterface = PrimeDtvServiceInterface.getInstance(this);
        mPrimeDtvService = PrimeTvInputFrameworkApplication.get_prime_dtv_service();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSession.onRelease();
        mSession = null;
        Log.d(TAG, "onDestroy");
    }

    @Override
    public Session onCreateSession(String inputId, String sessionId) {
        Log.d(TAG, "PlayerControl tuner onCreateSession() inputId = "+inputId+" sessionId = "+sessionId);
        mSession = new PrimeTvInputServiceSession(this);
        mSessionId = sessionId;
        return mSession;
     }

    @Override
    public Session onCreateSession(String inputId) {
        Log.d(TAG, "PlayerControl tuner onCreateSession() inputId = "+inputId);
        mInputId = inputId;
        PrimeTvInputFrameworkApplication.setTvInputId(inputId);
        mSession = new PrimeTvInputServiceSession(this);
        return mSession;
    }

    @Nullable
    @Override
    public RecordingSession onCreateRecordingSession(@NonNull String inputId) {
        Log.d(TAG, "onCreateRecordingSession: inputId = " + inputId);
        if (mPrimeDtvService.pvr_change_channel_manager_is_full_fecording()) {
            Log.e(TAG, "onCreateRecordingSession: Recording limit reached.");
            return null;
        }

        if (mPrimeDtvService.pvr_change_channel_manager_is_timeshift_start() &&
                mPrimeDtvService.pvr_change_channel_manager_get_rec_num() >= Pvcfg.NUM_OF_RECORDING - 1) {
            Log.e(TAG, "onCreateRecordingSession: Time-shift + Recording limit reached.");
            return null;
        }

        String usbPath = mPrimeDtvService.pvr_get_usb_mount_path();
        if (TextUtils.isEmpty(usbPath)) {
            Log.e(TAG, "onCreateRecordingSession: No usb storage set in prime dtv service.");
            return null;
        }

        int recTunerId = mPrimeDtvService.pvr_change_channel_manager_get_rec_tuner_id();
        return new PrimeTvInputServiceRecordingSession(this, inputId, recTunerId);
    }
}

