package com.mtest.activity;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.storage.StorageManager;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dolphin.dtv.EnTableType;
import com.mtest.module.SmartCardModule;
import com.mtest.module.TunerModule;
import com.mtest.module.UsbModule;
import com.mtest.utils.PesiStorageHelper;
import com.mtest.utils.TunerView;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.PvrInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;
import com.prime.dtvplayer.utils.TVMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class QEForHWActivity extends DTVActivity {
    final String TAG = getClass().getSimpleName();

    private final static int REC_TIME_MILLISEC = 4*60*60*1000; // 4 hours re rec

    private TextView mTvSignalStregth;
    private TextView mTvSignalQuality;
//    private TextView mTvSignalBER;
    private TextView mTvErrorStatus;
    private ProgressBar mProgbarStrength;
    private ProgressBar mProgbarQuality;
    private ConstraintLayout mMainLayout;

    private List<TunerView> mTunerViewList;
    private List<TextView> mTvTunerChannelList;
    private Global_Variables mGlobalVars;
    private UsbModule mUsbModule;

    private boolean mIsSearching = false;
    private int mRecID = -1;
    private int mCurTunerIdx;
    private long mRecChannelID = 0;

    private long mPlayingChIDs[] = {0, 0};

    private final TunerModule mModTuner = new TunerModule(QEForHWActivity.this);
    private final SmartCardModule mModSmartCard = new SmartCardModule(QEForHWActivity.this);
    private final Handler mHandler = new Handler();
    private final Runnable mCheckStatusRunnable = new Runnable() {
        @Override
        public void run() {
            if (mGlobalVars.m_GetIPTVActive())
            {
                // TODO
            }
            else if (!mIsSearching)    // not do until scan complete
            {
                /*if (TunerGetLockStatus(0) == 1
                        && AvControlGetPlayStatus(0) == HiDtvMediaPlayer.EnPlayStatus.LIVEPLAY.getValue())*/
                if (mModTuner.isLock(0) && mModTuner.isLivePlay(0))
                    mTunerViewList.get(0).setStatus(TunerView.STATUS_PASS);
                else
                    mTunerViewList.get(0).setStatus(TunerView.STATUS_FAIL);

                /*if (TunerGetLockStatus(1) == 1
                        && AvControlGetPlayStatus(0) == HiDtvMediaPlayer.EnPlayStatus.LIVEPLAY.getValue())*/
                if (mModTuner.isLock(1) && mModTuner.isLivePlay(0))
                    mTunerViewList.get(1).setStatus(TunerView.STATUS_PASS);
                else
                    mTunerViewList.get(1).setStatus(TunerView.STATUS_FAIL);

                if (mModTuner.isLock(2)) //if (TunerGetLockStatus(2) == 1) {
                    mTunerViewList.get(2).setStatus(TunerView.STATUS_PASS);
                else
                    mTunerViewList.get(2).setStatus(TunerView.STATUS_FAIL);

                if (mModTuner.isLock(3))//if (TunerGetLockStatus(3) == 1) {
                    mTunerViewList.get(3).setStatus(TunerView.STATUS_PASS);
                else
                    mTunerViewList.get(3).setStatus(TunerView.STATUS_FAIL);
            }

            mHandler.postDelayed(mCheckStatusRunnable, 3000);
        }
    };

    private Runnable mCheckSignalRunnable = new Runnable() {
        @Override
        public void run() {
            updateSignalLevel(mCurTunerIdx);
            mHandler.postDelayed(mCheckSignalRunnable, 1000);
        }
    };

    private Runnable mRecRunnable = new Runnable() {
        @Override
        public void run() { // re rec every REC_TIME_MILLISEC
            mtestQERecord(mRecChannelID);
            mHandler.postDelayed(mRecRunnable, REC_TIME_MILLISEC);
        }
    };

    private Runnable mCheckAtrRunnable = new Runnable() {
        @Override
        public void run() {
            mModSmartCard.getATRStatus(0); //MtestGetATRStatus(0);
            mHandler.postDelayed(mCheckAtrRunnable, 1000);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qe_for_hw);

        mGlobalVars = (Global_Variables) getApplicationContext();
        mCurTunerIdx = mGlobalVars.mtunerID;
        mUsbModule = new UsbModule(this);

        initViews();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();

        // close all av
        mModTuner.AvStopByTunerID(0);//MtestTestAvStopByTunerID(0);
        mModTuner.AvStopByTunerID(1);//MtestTestAvStopByTunerID(1);
        mModTuner.AvStopByTunerID(2);//MtestTestAvStopByTunerID(2);
        mModTuner.AvStopByTunerID(3);//MtestTestAvStopByTunerID(3);

        mHandler.post(mCheckStatusRunnable);
        mHandler.post(mCheckSignalRunnable);
        mHandler.post(mCheckAtrRunnable);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();

        mHandler.removeCallbacks(mCheckStatusRunnable);
        mHandler.removeCallbacks(mCheckSignalRunnable);
        mHandler.removeCallbacks(mCheckAtrRunnable);

        mHandler.removeCallbacks(mRecRunnable);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();

        mHandler.removeCallbacksAndMessages(null);
        SaveTable(EnTableType.PROGRAME);

        new Thread(new Runnable() { // new thread to stop all recs
            @Override
            public void run() {
                stopAllRecords();
            }
        }).start();
        mUsbModule = null;
    }

    @Override
    public void onMessage(TVMessage tvMessage) {
        super.onMessage(tvMessage);
        switch (tvMessage.getMsgType()) {
            case TVMessage.TYPE_SCAN_BEGIN:
                Log.d("TAG", "onMessage:    TVMessage.TYPE_SCAN_BEGIN!");
                break;
            case TVMessage.TYPE_SCAN_PROCESS:
                Log.d("TAG", "onMessage:    TVMessage.TYPE_SCAN_PROCESS!");
                break;
            case TVMessage.TYPE_SCAN_END:
                Log.d("TAG", "onMessage:    TVMessage.TYPE_SCAN_END tvnum = " + tvMessage.getTotalTVNumber() + " radio " + tvMessage.getTotalRadioNumber());
                if (!mIsSearching) {
                    break;
                }

                if (tvMessage.getTotalTVNumber() != 0 || tvMessage.getTotalRadioNumber() != 0) {
                    mModTuner.ScanParamsStopScan(true);
                    mModTuner.ScanResultSetChannel();

                    int tpID = mTunerViewList.get(mCurTunerIdx).getTpID();
                    long channelID = getPlayableChannelIDByTpID(tpID);

                    mTvTunerChannelList.get(mCurTunerIdx).setText(getString(R.string.str_init_ch_index));
                    mtestQEPlay(mCurTunerIdx, channelID);
                    mTunerViewList.get(mCurTunerIdx).setStatus(TunerView.STATUS_PASS);
                }
                else {
                    Toast.makeText(this, "No Channels Found!", Toast.LENGTH_SHORT).show();
                    mTunerViewList.get(mCurTunerIdx).setStatus(TunerView.STATUS_FAIL);
                }

                mIsSearching = false;
                break;
            default:
                break;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean isActionDown = event.getAction() == KeyEvent.ACTION_DOWN;
        if (isActionDown && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) { // intercept ok key
            mTunerViewList.get(mCurTunerIdx).stopEditParams();
            tunerSearch(mCurTunerIdx);
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean osdVisible;
        boolean errorStatusVisible;

        switch (keyCode) {
            case KeyEvent.KEYCODE_INFO:
            case ExtKeyboardDefine.KEYCODE_INFO:    // Johnny 20181210 for keyboard control
            case KeyEvent.KEYCODE_PROG_GREEN:  // for kbro remote control
                mTunerViewList.get(mCurTunerIdx).startEditParams();
                break;
            case KeyEvent.KEYCODE_PROG_YELLOW:
            case ExtKeyboardDefine.KEYCODE_PROG_YELLOW: // Johnny 20181210 for keyboard control
                // osd
                osdVisible = mMainLayout.getVisibility() == View.VISIBLE;
                setOSDVisibility(!osdVisible);
                break;
            case KeyEvent.KEYCODE_PROG_BLUE:
            case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
                // show/hide ber & VideoErrorFrameCount when OSD is hide
                osdVisible = mMainLayout.getVisibility() == View.VISIBLE;
                if (!osdVisible) {
                    // show/hide ber & VideoErrorFrameCount when OSD is hide
                    errorStatusVisible = mTvErrorStatus.getVisibility() == View.VISIBLE;
                    setErrorStatusVisibility(!errorStatusVisible);
                }
                break;
            case KeyEvent.KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                // record & usb read write
                if (mCurTunerIdx == 0 || mCurTunerIdx == 1) { // can only rec tuner 0~1
                    mRecChannelID = mPlayingChIDs[mCurTunerIdx];
                    mHandler.removeCallbacks(mRecRunnable);
                    mHandler.post(mRecRunnable);
                    testUsbReadWrite();
                }
                else {
                    Toast.makeText(this, "Can't rec this tuner!", Toast.LENGTH_SHORT).show();
                }
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_CHANNEL_UP:
                mtestQEChannelUp(mCurTunerIdx);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_CHANNEL_DOWN:
                mtestQEChannelDown(mCurTunerIdx);
                break;
        }

        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {   // between 0~9
            selectTunerByKeyCode(keyCode);
        }

        return super.onKeyDown(keyCode, event);
    }

    private void initViews() {
        initTunerView();

        mMainLayout = (ConstraintLayout) findViewById(R.id.layout_qe_main);
        mTvSignalStregth = (TextView) findViewById(R.id.tv_signal_strength_value);
        mTvSignalQuality = (TextView) findViewById(R.id.tv_signal_quality_value);
//        mTvSignalBER = (TextView) findViewById(R.id.tv_signal_ber_value);
        mProgbarStrength = (ProgressBar) findViewById(R.id.progbar_signal_strength);
        mProgbarQuality = (ProgressBar) findViewById(R.id.progbar_signal_quality);
        mTvErrorStatus = (TextView) findViewById(R.id.tv_error_status);

        mTvTunerChannelList = new ArrayList<>();
        mTvTunerChannelList.add((TextView) findViewById(R.id.tv_tuner01_channel));
        mTvTunerChannelList.add((TextView) findViewById(R.id.tv_tuner02_channel));
    }

    private void initTunerView() {
        int tunerNum = mModTuner.getTunerNum();//GetTunerNum();
//        List<Integer> tunerTypeList = GetTunerTypeList();
        mTunerViewList = new ArrayList<>();
        for (int i = 0 ; i < tunerNum && i < 4 ; i++) { // only support 4 tuners now
            View tunerView = findViewById(getTunerViewResID(i));
            mTunerViewList.add(new TunerView(tunerView, i, /*tunerTypeList.get(i)*/GetCurTunerType(), this));
        }
    }

    // find playable TV channel
    // for now it means first TV channel of a tp
    private long getPlayableChannelIDByTpID(int tpID) {
        long channelID = 0;

        List<SimpleChannel> simpleChannelList = MtestGetSimpleChannelListByTpID(tpID);
        if (!simpleChannelList.isEmpty()) {
            channelID = simpleChannelList.get(0).getChannelId(); // first channel
        }

        return channelID;
    }

    // search & play if tuner 0~1
    // tune if tuner 2~3
    private void tunerSearch(int tunerID) {
        if (tunerID == 0 || tunerID == 1) {
            delProgramsByTpID(mTunerViewList.get(tunerID).getTpID());
            mIsSearching = mTunerViewList.get(tunerID).tunerSearch();
        }
        else if (tunerID == 2 || tunerID == 3) {
            mtestQETune(tunerID);
        }
    }

    private void delProgramsByTpID(int tpID) {
        List<SimpleChannel> simpleChannelList = ProgramInfoGetSimpleChannelList(ProgramInfo.ALL_TV_TYPE, 1, 1);
        if (simpleChannelList == null) {
            simpleChannelList = new ArrayList<>();
        }

        for (SimpleChannel simpleChannel : simpleChannelList) {
            if (simpleChannel.getTpId() == tpID) {
                ProgramInfoDelete(simpleChannel.getChannelId());
            }
        }
    }

    private void updateSignalLevel(int tunerID)
    {
        int quality;
        int strength;
        String ber;
        int lock;
        int barColor ;
        String str;
        int errorFrameCount;
        int frameDropCount;
        int recTime;

        if (mMainLayout.getVisibility() == View.VISIBLE) { //  for service overloading
            lock = mModTuner.getLockStatus(tunerID);//TunerGetLockStatus(tunerID);
            quality = mModTuner.getQuality(tunerID);//TunerGetQuality(tunerID);
            strength = mModTuner.getStrength(tunerID);//TunerGetStrength(tunerID);

            str = Integer.toString(strength)+" %";
            mTvSignalStregth.setText(str);
            str = Integer.toString(quality)+" %";
            mTvSignalQuality.setText(str);

            barColor = lock == 1 ?  Color.GREEN : Color.RED;

            mProgbarStrength.setProgressTintList(ColorStateList.valueOf(barColor));
            mProgbarQuality.setProgressTintList(ColorStateList.valueOf(barColor));

            mProgbarStrength.setProgress(strength);
            mProgbarQuality.setProgress(quality);

//            mTvSignalBER.setText(ber);

//            Log.d(TAG, "updateSignalLevel  osd visible, tunerID = " + tunerID + " Strengh = " + strength + " Quality = " + quality + " BER = " + ber);
        }
        else {
            errorFrameCount = mModTuner.getErrorFrameCount(tunerID);//MtestGetErrorFrameCount(tunerID);
            frameDropCount = mModTuner.getFrameDropCount(tunerID);//MtestGetFrameDropCount(tunerID);
            ber = mModTuner.getBER(tunerID);//TunerGetBER(tunerID);
            recTime = PvrRecordGetAlreadyRecTime(0, mRecID); // for user to check is recording

            mTvErrorStatus.setText(String.format(Locale.getDefault(), "BER = %s\nErrorFrameCount = %d\nFrameDropCount = %d" + "\nRec Time = %d", ber, errorFrameCount, frameDropCount, recTime));

//            Log.d(TAG, "updateSignalLevel  osd hide, tunerID = " + tunerID + " BER = " + ber + " ErrFrameCount = " + errorFrameCount + " FrameDropCount = " + frameDropCount/* + " Rec Time = " + recTime*/);
        }
    }

    private void mtestQEPlay(int tunerID, long channelID) {  // play tuner 0, 1
        List<Integer> tunerIDs = new ArrayList<>();
        List<Long> channelIDs = new ArrayList<>();
        int tunerNum = mModTuner.getTunerNum();//GetTunerNum();

        for (int i = 0 ; i < tunerNum && i < 2 ; i++) { // find channelID of tuner 0, 1
            tunerIDs.add(i);
            if (tunerID == i) {
                channelIDs.add(channelID);
                mPlayingChIDs[i] = channelID; // save playing channelID for rec
            }
            else {
                channelIDs.add((long) 0); // 0 = do nothing
            }
        }

        mModTuner.AvMultiPlay(2, tunerIDs, channelIDs);//MtestTestAvMultiPlay(2, tunerIDs, channelIDs); // if channelID = 0 , service will not play
    }

    private void mtestQETune(int tunerID) { // tunerID = tunerIdx
        mTunerViewList.get(tunerID).updateTpInfo();
        TunerTuneByExistTp(tunerID, mTunerViewList.get(tunerID).getTpID());
    }

    private void mtestQERecord(final long channelID) {
        Log.d(TAG, "mtestQERecord: def rec path = " + getDefaultRecPath());
        Log.d(TAG, "mtestQERecord: rec path = " + GetRecordPath());

        if (channelID <= 0) {
            Toast.makeText(this, "No Channel!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (getDefaultRecPath().equals(GetRecordPath())) {
            Toast.makeText(this, "No USB!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mGlobalVars.isRecAvailable()) {
            Toast.makeText(this, "Rec is not available, please try again later...", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(new Runnable() { // new thread to stop all recs & start rec
            @Override
            public void run() {
                stopAllRecords();

                // make rec dir
                String path = GetRecordPath() + "/Records";
                File dirFile = new File(path);
                if(!dirFile.exists() && !dirFile.mkdirs()) {
                    showToastInUiThread("Make dir ' "+ path + "' Fail!");
                    return;
                }

                path = path + "/qe_test.ts";
                mRecID = PvrRecordStart(0, channelID, path, REC_TIME_MILLISEC);

                if (mRecID >= 0) {
                    showToastInUiThread("Record Start!");
                }
                else {
                    showToastInUiThread("Record Fail!(Tuner 0 & 1 same freq?)");
                }
            }
        }).start();

    }

    private void stopAllRecords() {
        mGlobalVars.setRecAvailable(false);
        // stop all recordings
        List<PvrInfo> pvrList = PvrRecordGetAllInfo();
        for(int i = 0 ; i < pvrList.size() ; i++)
        {
            int recId = pvrList.get(i).getRecId();
            PvrRecordStop(0, recId);
        }

        mGlobalVars.setRecAvailable(true);
//        showToastInUiThread("All Recs Stopped");
    }

    private void setOSDVisibility(boolean visible) {
        if (visible) {
            mMainLayout.setVisibility(View.VISIBLE);
            mTvErrorStatus.setVisibility(View.INVISIBLE);
        } else {
            mMainLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void setErrorStatusVisibility(boolean visible) {
        if (visible) {
            mTvErrorStatus.setVisibility(View.VISIBLE);
        } else {
            mTvErrorStatus.setVisibility(View.INVISIBLE);
        }
    }

    private void showToastInUiThread(final String toastMsg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getBaseContext(), toastMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getTunerViewResID(int index) {
        int[] ids = {
                R.id.tuner_view01,
                R.id.tuner_view02,
                R.id.tuner_view03,
                R.id.tuner_view04
        };

        if (index < ids.length) {
            return ids[index];
        }
        else {
            return -1;
        }
    }


    private void mtestQEChannelUp(int tunerID) {
        if (tunerID < 0 || tunerID > 1) { // only tuner 0~1 can change channel
            return;
        }

        List<SimpleChannel> simpleChannelList = MtestGetSimpleChannelListByTpID(mTunerViewList.get(tunerID).getTpID());
        if (simpleChannelList.isEmpty()) { // no channel
            return;
        }

        String channelIdx = mTvTunerChannelList.get(tunerID).getText().toString().replaceAll("\\D", "");
        int nextChIdx = Integer.parseInt(channelIdx) + 1;
        if (nextChIdx >= simpleChannelList.size()) {
            nextChIdx = 0;
        }

        mTvTunerChannelList.get(tunerID).setText(String.format(Locale.getDefault(), "CH:%d", nextChIdx));
        mtestQEPlay(tunerID, simpleChannelList.get(nextChIdx).getChannelId()); // play next ch
    }

    private void mtestQEChannelDown(int tunerID) {
        if (tunerID < 0 || tunerID > 1) { // only tuner 0~1 can change channel
            return;
        }

        List<SimpleChannel> simpleChannelList = MtestGetSimpleChannelListByTpID(mTunerViewList.get(tunerID).getTpID());
        if (simpleChannelList.isEmpty()) { // no channel
            return;
        }

        String channelIdx = mTvTunerChannelList.get(tunerID).getText().toString().replaceAll("\\D", "");
        int nextChIdx = Integer.parseInt(channelIdx) - 1;
        if (nextChIdx < 0) {
            nextChIdx = simpleChannelList.size() - 1;
        }

        mTvTunerChannelList.get(tunerID).setText(String.format(Locale.getDefault(), "CH:%d", nextChIdx));
        mtestQEPlay(tunerID, simpleChannelList.get(nextChIdx).getChannelId()); // play next ch
    }

    private void selectTunerByKeyCode(int keyCode) {
        if (keyCode < KeyEvent.KEYCODE_0 || keyCode > KeyEvent.KEYCODE_9) {
            return;
        }

        if (mIsSearching) {
            return;
        }

        int indexFromKeyCode = keyCode - KeyEvent.KEYCODE_0;

        if (indexFromKeyCode < mTunerViewList.size()) { // tuner
            mCurTunerIdx = indexFromKeyCode;
            mGlobalVars.mtunerID = mCurTunerIdx;
//            mGlobalVars.mtpID = mTunerViewList.get(mCurTunerIdx).getTpID();
            mTunerViewList.get(mCurTunerIdx).setStatus(TunerView.STATUS_SELECT);
        }
    }

    private void testUsbReadWrite() {
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        PesiStorageHelper pesiStorageHelper = new PesiStorageHelper(storageManager);

//        List<Integer> pesiUsbPortList = GetUsbPortList();
//        if (pesiUsbPortList == null) {
//            pesiUsbPortList = new ArrayList<>();
//        }
        List<Integer> pesiUsbPortList = mUsbModule.getPortList();

        String recPath = GetRecordPath();
        for (Object volumeInfo : getVolumes()) {
            int usbPortNumber = pesiStorageHelper.getUsbPortNum(volumeInfo);
            String path = pesiStorageHelper.getPath(volumeInfo);
            String internalPath = pesiStorageHelper.getInternalPath(volumeInfo);
            if (pesiUsbPortList.contains(usbPortNumber) && !internalPath.equals(recPath)) {
//                MtestTestUsbReadWrite(volumeInfo.usbPortNumber, volumeInfo.path);
                mUsbModule.readWriteUSB(usbPortNumber, path);
                Toast.makeText(this, "UsbReadWrite Start! Path : " + path, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
