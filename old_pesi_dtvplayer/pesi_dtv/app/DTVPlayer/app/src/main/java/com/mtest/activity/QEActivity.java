package com.mtest.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.mtest.config.MtestConfig;
import com.mtest.module.TunerModule;
import com.mtest.module.UsbModule;
import com.mtest.utils.PesiStorageHelper;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.PvrInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;
import com.prime.dtvplayer.utils.TVMessage;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QEActivity extends DTVActivity {
    final String TAG = getClass().getSimpleName();
    private final static int MAX_USB_NUM = 2;
    private final static int CHECK_STATUS_DELAY = 3000; // ms
    private final static int REC_TIME_MILLISEC = 0; // inf
    private final static String REC_FILE_NAME = "qe_test.ts";
    private final static String REC_FILE_FOLDER = "Records";

    private final static int STATE_IDLE = 0;
    private final static int STATE_RECORDING = 1;
    private final static int STATE_PLAYING = 2;

    private TextView mTvStatus;
    private TextView mTvStorage;

    private int mCurUSBIndex = 0;
    private int mCurState = STATE_IDLE;
    private int mCurRecID = -1;
    private boolean mIsPreMounted = false;
    private String mCurRecPath;

    private Global_Variables mGlobalVars;
    private SimpleChannel mCurChannel;
    private UsbModule mUsbModule;

    private final TunerModule mModTuner = new TunerModule(this);
    private final Handler mHandler = new Handler();
    private final Runnable mCheckStatusRunnable = new Runnable() {
        @Override
        public void run() {
            boolean isMounted = mUsbModule.checkIsMounted(mCurUSBIndex);

            if (mCurState == STATE_IDLE && mIsPreMounted != isMounted) {
                if (isMounted) {
                    mTvStatus.setText(getString(R.string.str_qe_mount_ok));
                    mTvStatus.setBackground(getDrawable(R.drawable.shape_rectangle_pass));
                } else {
                    mTvStatus.setText(getString(R.string.str_qe_mount_fail));
                    mTvStatus.setBackground(getDrawable(R.drawable.shape_rectangle_fail));
                }
            }
            else if (mCurState == STATE_RECORDING) {
                //Toast.makeText(getBaseContext(), "Rec Time = " + PvrRecordGetAlreadyRecTime(0, mCurRecID), Toast.LENGTH_SHORT).show();
            }
            else if (mCurState == STATE_PLAYING) {
                //Toast.makeText(getBaseContext(), "Play Time = " + PvrPlayGetPlayTime(), Toast.LENGTH_SHORT).show();
            }

            if (isMounted) {
                mTvStorage.setBackground(getDrawable(R.drawable.shape_rectangle_pass));
                mCurRecPath = getUSBRecPath(mCurUSBIndex);
            }
            else {
                mTvStorage.setBackground(getDrawable(R.drawable.shape_rectangle_fail));
            }

            mIsPreMounted = isMounted;
            mHandler.postDelayed(mCheckStatusRunnable, CHECK_STATUS_DELAY);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qe);

        mGlobalVars = (Global_Variables) getApplicationContext();
        mCurChannel = ViewHistory.getCurChannel();
        mUsbModule = new UsbModule(this);

        initViews();
        setCurUSB(0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mHandler.post(mCheckStatusRunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mCheckStatusRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mHandler.removeCallbacksAndMessages(null);
        mtestQEStopAllRecords();
        mtestQEPvrPlayStop();
        mUsbModule = null; // edwin 20201217 fix NullPointer
    }

    @Override
    public void onMessage(TVMessage tvMessage)
    {
        super.onMessage(tvMessage);

        int msg = tvMessage.getMsgType();
        Log.d(TAG, "onMessage:  msg = " + msg);

        switch (msg) {
            case TVMessage.TYPE_PVR_PLAY_EOF:
                Log.d(TAG, "onMessage: play end");

                mtestQEPvrPlayStop();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int tmpCurUSBIndex = mCurUSBIndex;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                if (tmpCurUSBIndex < MAX_USB_NUM-1) {
                    tmpCurUSBIndex++;
                }
                else {
                    tmpCurUSBIndex = 0;
                }

                setCurUSB(tmpCurUSBIndex);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (tmpCurUSBIndex > 0) {
                    tmpCurUSBIndex--;
                }
                else {
                    tmpCurUSBIndex = MAX_USB_NUM-1;
                }

                setCurUSB(tmpCurUSBIndex);
                break;
            case KeyEvent.KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                // record
                if (mCurChannel != null) {
                    mtestQEPvrPlayStop();
                    mtestQERecord(mCurChannel.getChannelId(), mCurRecPath);
                }
                break;
            case KeyEvent.KEYCODE_PROG_YELLOW:
            case ExtKeyboardDefine.KEYCODE_PROG_YELLOW: // Johnny 20181210 for keyboard control
                // play/stop
                if (mCurState == STATE_PLAYING) {
                    mtestQEPvrPlayStop();
                }
                else {
                    mtestQEStopAllRecords();
                    setCurState(STATE_IDLE);
                    mtestQEPvrPlayStart(mCurRecPath);
                }
                break;
            case KeyEvent.KEYCODE_PROG_BLUE:
            case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
                // hdd format (only delete all files now)
                if (mCurState == STATE_IDLE) {
                    delAllFiles(mCurRecPath);
                }
                break;
            case KeyEvent.KEYCODE_INFO:
            case ExtKeyboardDefine.KEYCODE_INFO:    // Johnny 20181210 for keyboard control
            case KeyEvent.KEYCODE_PROG_GREEN:     // QE for kbro remote control
            case ExtKeyboardDefine.KEYCODE_PROG_GREEN: // Johnny 20181210 for keyboard control
                // Hw QE
                //Intent intent = new Intent(QEActivity.this, QEForHWActivity.class);
                //startActivity(intent);
                //finish();
                break;
            default:
                break;

        }
        return super.onKeyDown(keyCode, event);
    }

    private void initViews() {
        mTvStatus = (TextView) findViewById(R.id.tv_status);
        mTvStorage = (TextView) findViewById(R.id.tv_storage_value);
    }

    private void setCurUSB(int usbIndex) {
        if (mCurState != STATE_IDLE) {
            return;
        }

        if (usbIndex < 0 || usbIndex >= MAX_USB_NUM) {
            return;
        }

        mCurUSBIndex = usbIndex;
        mTvStorage.setText(String.format(Locale.getDefault(), "USB %d", usbIndex+1));
        mCurRecPath = "";
        setCurState(STATE_IDLE);
    }

    private void setCurState(int state) {
        if (state < STATE_IDLE || state > STATE_PLAYING) {
            return;
        }

        boolean isMounted = mUsbModule.checkIsMounted(mCurUSBIndex);

        if (state == STATE_PLAYING) {
            mTvStatus.setText(getString(R.string.str_qe_playing));
            mTvStatus.setBackground(getDrawable(R.drawable.shape_rectangle_pass));
        }
        else if (state == STATE_RECORDING) {
            mTvStatus.setText(getString(R.string.str_qe_recording));
            mTvStatus.setBackground(getDrawable(R.drawable.shape_rectangle_pass));
        }
        else { // idle
            if (isMounted) {
                mTvStatus.setText(getString(R.string.str_qe_mount_ok));
                mTvStatus.setBackground(getDrawable(R.drawable.shape_rectangle_pass));
            }
            else {
                mTvStatus.setText(getString(R.string.str_qe_mount_fail));
                mTvStatus.setBackground(getDrawable(R.drawable.shape_rectangle_fail));
            }
        }

        if (isMounted) {
            mTvStorage.setBackground(getDrawable(R.drawable.shape_rectangle_pass));
            mCurRecPath = getUSBRecPath(mCurUSBIndex);
        }
        else {
            mTvStorage.setBackground(getDrawable(R.drawable.shape_rectangle_fail));
        }

        mCurState = state;
    }

    /*private boolean checkIsMounted(int usbIndex) {
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        boolean mounted = false;

        if (storageManager != null) {
            List<VolumeInfo> volumeInfoList = storageManager.getVolumes();
            List<Integer> pesiUsbPortList = GetUsbPortList();
            if (pesiUsbPortList == null) {
                pesiUsbPortList = new ArrayList<>();
            }

            for (VolumeInfo volumeInfo : volumeInfoList) {
                int portNo = volumeInfo.usbPortNumber;
                int index = pesiUsbPortList.indexOf(portNo);
                if (index == usbIndex) {
                    mounted = true;
                    break;
                }
            }
        }

        return mounted;
    }*/

    private String getUSBRecPath(int usbIndex) {
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        String path = "";
        PesiStorageHelper pesiStorageHelper = new PesiStorageHelper(storageManager);

        if (storageManager != null) {
            List<Object> volumeInfoList = pesiStorageHelper.getVolumes();
//            List<Integer> pesiUsbPortList = GetUsbPortList();
//            if (pesiUsbPortList == null) {
//                pesiUsbPortList = new ArrayList<>();
//            }
            List<Integer> pesiUsbPortList = mUsbModule.getPortList();

            for (Object volumeInfo : getVolumes()) {
                int portNo = pesiStorageHelper.getUsbPortNum(volumeInfo);
                int index = pesiUsbPortList.indexOf(portNo);
                if (index == usbIndex) {
                    path = pesiStorageHelper.getInternalPath(volumeInfo);
                    break;
                }
            }
        }

        return path;
    }

    private void mtestQERecord(final long channelID, final String recPath) {
        if (channelID <= 0) {
            Toast.makeText(this, "Invalid ChannelID!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(recPath)) {
            Toast.makeText(this, "No Rec Path!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mGlobalVars.isRecAvailable()) {
            Toast.makeText(this, "Rec is not available, please try again later...", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(new Runnable() { // new thread to stop all recs & start rec
            @Override
            public void run() {
                mGlobalVars.setRecAvailable(false);
                List<PvrInfo> pvrList = PvrRecordGetAllInfo();
                for(int i = 0 ; i < pvrList.size() ; i++)
                {
                    int recId = pvrList.get(i).getRecId();
                    PvrRecordStop(0, recId);
                }
                mGlobalVars.setRecAvailable(true);

                // make rec dir
                String path = recPath + "/" + REC_FILE_FOLDER;
                File dirFile = new File(path);
                if(!dirFile.exists() && !dirFile.mkdirs()) {
                    showToastInUiThread("Make dir ' "+ path + "' Fail!");
                    return;
                }

                path = path + "/" + REC_FILE_NAME;
                mCurRecID = PvrRecordStart(0, channelID, path, REC_TIME_MILLISEC);

                if (mCurRecID >= 0) {
//                    showToastInUiThread("Record Start!");
                    setCurState(STATE_RECORDING);
                }
                else {
                    showToastInUiThread("Record Fail!");
                }
            }
        }).start();
    }

    private void mtestQEStopAllRecords() {
        new Thread(new Runnable() { // new thread to stop all recs
            @Override
            public void run() {
                mGlobalVars.setRecAvailable(false);

                // stop all recordings
                List<PvrInfo> pvrList = PvrRecordGetAllInfo();
                for(int i = 0 ; i < pvrList.size() ; i++)
                {
                    int recId = pvrList.get(i).getRecId();
                    PvrRecordStop(0, recId);
                }

                mGlobalVars.setRecAvailable(true);
                //showToastInUiThread("All Recs Stopped");
            }
        }).start();
    }

    private void mtestQEPvrPlayStart(final String recPath) {
        if (TextUtils.isEmpty(recPath)) {
            Toast.makeText(this, "No Rec Path!", Toast.LENGTH_SHORT).show();
            return;
        }

        mModTuner.AvStopByTunerID(0);//MtestTestAvStopByTunerID(0);
        mModTuner.AvStopByTunerID(1);//MtestTestAvStopByTunerID(1);
        mModTuner.AvStopByTunerID(2);//MtestTestAvStopByTunerID(2);
        mModTuner.AvStopByTunerID(3);//MtestTestAvStopByTunerID(3);

        int ret = PvrPlayStart(recPath + "/" + REC_FILE_FOLDER + "/" + REC_FILE_NAME);
        if (ret == 0) {
            //Toast.makeText(this, "Play Start!!", Toast.LENGTH_SHORT).show();
            setCurState(STATE_PLAYING);
        }
        else {
            Toast.makeText(this, "Play Start Fail!!", Toast.LENGTH_SHORT).show();
            // replay live
            if (mCurChannel != null) {
                List<Integer> tunerIDs = new ArrayList<>();
                List<Long> channelIDs = new ArrayList<>();
                tunerIDs.add(0);
                channelIDs.add(mCurChannel.getChannelId());
                mModTuner.AvMultiPlay(1, tunerIDs, channelIDs); //MtestTestAvMultiPlay(1, tunerIDs, channelIDs);
            }
        }
    }

    private void mtestQEPvrPlayStop() {
        int ret = PvrPlayStop();
        if (ret == 0) {
            //Toast.makeText(this, "Play Stop!!", Toast.LENGTH_SHORT).show();

            // replay live
            if (mCurChannel != null) {
                List<Integer> tunerIDs = new ArrayList<>();
                List<Long> channelIDs = new ArrayList<>();
                tunerIDs.add(0);
                channelIDs.add(mCurChannel.getChannelId());
                mModTuner.AvMultiPlay(1, tunerIDs, channelIDs); //MtestTestAvMultiPlay(1, tunerIDs, channelIDs);
            }

            setCurState(STATE_IDLE);
        }
    }

    private void showToastInUiThread(final String toastMsg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getBaseContext(), toastMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void delAllFiles(String path) {
        //Toast.makeText(getBaseContext(), path, Toast.LENGTH_SHORT).show();
        File dir = new File(path);
        try {
            FileUtils.cleanDirectory(dir);
            mTvStatus.setText(getString(R.string.str_qe_format_ok));
            mTvStatus.setBackground(getDrawable(R.drawable.shape_rectangle_pass));
        } catch (Exception e) {
            e.printStackTrace();
            mTvStatus.setText(getString(R.string.str_qe_format_fail));
            mTvStatus.setBackground(getDrawable(R.drawable.shape_rectangle_fail));
            Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
