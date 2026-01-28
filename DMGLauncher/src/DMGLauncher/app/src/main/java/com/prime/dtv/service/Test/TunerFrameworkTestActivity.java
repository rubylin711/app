package com.prime.dtv.service.Test;

import static android.view.KeyEvent.KEYCODE_CHANNEL_DOWN;
import static android.view.KeyEvent.KEYCODE_CHANNEL_UP;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.prime.dtv.Interface.PesiDtvFrameworkInterface;
import com.prime.dtv.PesiDtvFramework;
//import com.prime.dtv.R;
import com.prime.dtv.service.Player.Player;
import com.prime.dtv.service.Table.Eit;
import com.prime.dtv.service.Table.EitData;
import com.prime.dtv.service.Table.Nit;
import com.prime.dtv.service.Table.Pat;
import com.prime.dtv.service.Table.Pmt;
import com.prime.dtv.service.Table.Sdt;
import com.prime.dtv.service.Table.Table;
import com.prime.dtv.service.Table.Tdt;
import com.prime.dtv.service.Table.Tot;
import com.prime.dtv.service.Tuner.TunerInterface;
import com.prime.dtv.sysdata.EPGEvent;
import com.prime.dtv.sysdata.TpInfo;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TunerFrameworkTestActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final String TAG = "TunerFrameworkTestActivity";

    public static final int HANDLER_TEST_AV_PLAY = 1;

    private TunerInterface mTuneInterface;
    private Player mPlayer;
    private PesiDtvFrameworkInterface mDtv = null;
    private AvCmdTest mAvCmdTest = null;
    private FrontendCmdTest mFrontendCmdTest = null;
    private PmCmdTest mPmCmdTest = null;
    private CfgCmdTest mCfgCmdTest = null;
    public int changeChannelTest = 0;

    private TestHandler mHandler = new TestHandler(this);
    public static class TestHandler extends Handler {
        private WeakReference<TunerFrameworkTestActivity> reference;

        public TestHandler(TunerFrameworkTestActivity activity) {
            reference = new WeakReference<TunerFrameworkTestActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: msg = "+msg.what);
            switch (msg.what) {
                case HANDLER_TEST_AV_PLAY: {
                    TunerFrameworkTestActivity mainActivity = (TunerFrameworkTestActivity) reference.get();
                    if(msg.arg1 == 1) {
                        mainActivity.mAvCmdTest.playAv();
                        Message message = new Message();
                        message.copyFrom(msg);
                        message.arg1 = 0;
                        if(mainActivity.changeChannelTest == 1)
                            mainActivity.mHandler.sendMessageDelayed(message, 10000);
                    }
                    else {
                        mainActivity.mAvCmdTest.channelUp();
                        Message message = new Message();
                        message.copyFrom(msg);
                        message.arg1 = 0;
                        mainActivity.mHandler.sendMessageDelayed(message, 10000);
                    }
                }break;
                default:
                    break;
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        //marked , need porting code
        //setContentView(R.layout.activity_tuner_framework_test);
        //marked , need porting code
        //SurfaceView surfaceView = findViewById(R.id.testSurfaceView);

//        SurfaceHolder holder = surfaceView.getHolder();
//        holder.setKeepScreenOn(true);
//        holder.addCallback(this);
        mDtv = PesiDtvFramework.getInstance(this) ;
        //marked , need porting code
//        mDtv.setSurfaceView(this,surfaceView);
        mAvCmdTest = new AvCmdTest(this,mDtv);
        mFrontendCmdTest = new FrontendCmdTest(mDtv);
        mPmCmdTest = new PmCmdTest(this, mDtv);
        mCfgCmdTest = new CfgCmdTest(mDtv);
    }

    public void startAvTest() {
        Message msg = Message.obtain();
        msg.what = HANDLER_TEST_AV_PLAY;
        msg.arg1 = 1;
        mHandler.sendMessageDelayed(msg,3000);
    }

    private void startFrontendTest() {
        mFrontendCmdTest.startTest();
    }

    private void stopFrontendTest() {
        mFrontendCmdTest.stopTest();
    }

    private void startPmTest() {
        if (!mPmCmdTest.startProgramInfoTest() || !mPmCmdTest.startSimpleChannelTest()) {
            Log.e(TAG, "startPmTest: pm cmd test fail");
        }
    }

    private void startCfgTest() {
        if (!mCfgCmdTest.startTest()) {
            Log.e(TAG, "startCfgTest: cfg cmd test fail");
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: ");
        super.onStart();

        startTest();
//        startAvTest();
//        startFrontendTest();
//        startPmTest();
//        startCfgTest();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();

        stopTest();
        stopFrontendTest();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    private void startTest() {
        Log.d(TAG, "startTest: ");
        /*
        Log.d(TAG, "SdtData Test Start");
        SdtData sdtData;
        sdtData=new SdtData();
        sdtData.SdtTestFuntion();
        Log.d(TAG, "SdtData Test End");
        */

        /*
        Log.d(TAG, "PatData Test Start");
        PatTest pattest;
        pattest=new PatTest();
        pattest.PatTestFuntion();
        Log.d(TAG, "PatData Test End");
        */

        /*
        Log.d(TAG, "PmtData Test Start");
        PmtTest pmttest;
        pmttest=new PmtTest();
        pmttest.PmtTestFuntion();
        Log.d(TAG, "PmtData Test End");

        */

        /*
        Log.d(TAG, "NitData Test Start");
        NitTest nittest;
        nittest=new NitTest();
        nittest.NitTestFuntion();
        Log.d(TAG, "NitData Test End");
        */

        /*
        DataManagerTest dataManagerTest = new DataManagerTest(this);
        dataManagerTest.DataManagerTestStart();
        */


        mTuneInterface = TunerInterface.getInstance(this);
        Pat pat;
        Pmt pmt;
        Nit nit;
        Sdt sdt;
        Tdt tdt;
        Tot tot;
        Eit eit;

        TpInfo tpInfo = new TpInfo(TpInfo.DVBC);
        tpInfo.CableTp.setFreq(633000);
        tpInfo.CableTp.setSymbol(6875);
        tpInfo.CableTp.setQam(TpInfo.Cable.QAM_256);

        if (mTuneInterface.tune(0, tpInfo)) {
            Log.d(TAG, "startTest: tune and locked");
//            pat = new Pat(mTuneInterface.getTuner(), 0);
//            pat.processWait();
//
//            pmt = new Pmt(mTuneInterface.getTuner(), 0, pat.getPatData());
//            pmt.processWait();
//
//            // true = actual NIT, false = other NIT
//            // TODO: NIT_OTHER
//            nit = new Nit(mTuneInterface.getTuner(), 0, true);
//            nit.processWait();
//
//            // true = actual SDT, false = other SDT
//            // TODO: SDT_OTHER
//            sdt = new Sdt(mTuneInterface.getTuner(), 0, true);
//            sdt.processWait();

//            tdt = new Tdt(mTuneInterface.getTuner(), 0);
//            tdt.processWait();
//
//            tot = new Tot(mTuneInterface.getTuner(), 0);
//            tot.processWait();

            EitData.EitListener eitListener = new EitData.EitListener() {
//                @Override
//                public void onParsed(int serviceId, List<EitData.Event> epgEvents) {
//                    Log.d(TAG, "onParsed: serviceId = " + serviceId);
//                    Log.d(TAG, "onParsed: epgEvents.size() = " + epgEvents.size());
//                }

                @Override
                public void onParsed(int serviceId, List<EPGEvent> epgEvents) {
//                    Log.d(TAG, "onParsed: sid = " + serviceId);
                    for (EPGEvent epgEvent : epgEvents) {
                        Log.d(TAG, "onParsed: " + epgEvent.get_event_id());
                        Log.d(TAG, "onParsed: " + epgEvent.get_event_name());
                        Log.d(TAG, "onParsed: " + epgEvent.get_event_name_lang_codec());
                        Log.d(TAG, "onParsed: " + epgEvent.get_short_event());
                        Log.d(TAG, "onParsed: " + epgEvent.get_extended_event());
                        Log.d(TAG, "onParsed: " + epgEvent.get_start_time());
                        Log.d(TAG, "onParsed: " + epgEvent.get_duration());
                        Log.d(TAG, "onParsed: " + epgEvent.get_end_time());
                    }
                }
            };
            eit = new Eit(0,Table.EIT_SCHEDULING_MIN_TABLE_ID, eitListener, 0);

            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (eit.getEitData() != null) {
                        List<EPGEvent> epgEventList = eit.getEitData().getAllEventList();
                        Log.d(TAG, "run: size = " + epgEventList.size());
                        for (EPGEvent epgEvent : epgEventList) {
//                            Log.d(TAG, "run: " + epgEvent.getEventId());
//                            Log.d(TAG, "run: " + epgEvent.getEventName());
//                            Log.d(TAG, "run: " + epgEvent.getEventNameLangCodec());
//                            Log.d(TAG, "run: " + epgEvent.getShortEvent());
//                            Log.d(TAG, "run: " + epgEvent.getExtendedEvent());
//                            Log.d(TAG, "run: " + epgEvent.getStartTime());
//                            Log.d(TAG, "run: " + epgEvent.getDuration());
//                            Log.d(TAG, "run: " + epgEvent.getEndTime());
                        }
                    }
                }
            };

            timer.schedule(timerTask, 0, 5000);
        }

        //TODO:Scan Test
//        ScanTest scanTest = new ScanTest(this);
    }

    private void stopTest() {
        Log.d(TAG, "stopTest: ");
        if (mTuneInterface != null) {
            mTuneInterface.close(0);
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
    }

    @Override
    public boolean onKeyDown(final int keyCode, KeyEvent event) {

        System.out.println( "keycoode=" + keyCode );
        switch (keyCode) {
            case KEYCODE_CHANNEL_UP:
            case KEYCODE_DPAD_UP: {
                this.mAvCmdTest.channelUp();
                return true;
            }
            case KEYCODE_CHANNEL_DOWN:
            case KEYCODE_DPAD_DOWN: {
                return true;
            }
            default: {

            }break;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");

        if (mPlayer != null) {
            mPlayer.stop();
        }
    }
}