package com.prime.dtvplayer.TestMiddleware;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ProgramInfo;

import android.app.Instrumentation; // change channel test !!!!
import android.os.Looper;// change channel test !!!!
import android.os.Message;// change channel test !!!!
import android.util.Log;

import java.util.List;


public class TestAVBurnActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();
    private final Context mContext = TestAVBurnActivity.this;
    //========    change channel test !!!!  =========
    private int TEST_MODE = 1 ;
    private static Handler ChangeChTest_handler;
    private Thread t =null ;
    private List<ProgramManagerImpl> ProgramManagerList = null;
    //===============================

    //========   change channel test !!!!  =========
    private void createMessageHandleThread(){
        //need start a thread to raise looper, otherwise it will be blocked
        t = new Thread() {
            public void run() {
                Log.i( TAG,"Creating handler ..." );
                Looper.prepare();
                ChangeChTest_handler = new Handler(){
                    public void handleMessage(Message msg) {
                        //process incoming messages here
                    }
                };
                Looper.loop();
                Log.i( TAG, "Looper thread ends" );
            }
        };
        t.start();
    }
    private final Runnable Key_Test_Run = new Runnable() {
        @Override
        public void run() {
            Instrumentation inst=new Instrumentation();
            Log.d(TAG, "Auto Test:  send UP");
            //inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
            AvControlPlayUp(ViewHistory.getPlayId());
            ChangeChTest_handler.postDelayed(Key_Test_Run, 2000);
        }
    };
    //==============================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_avburn);

        //========  change channel test !!!!  =========
        if(TEST_MODE == 1) {
            Log.d(TAG, "onCreate:  Change channel Test !!!!");
            createMessageHandleThread();
        }
        //===============================

        playByChannelId();
        Rect rect = new Rect(0,0,1920,1080);
        AvControlSetWindowSize(ViewHistory.getPlayId(), rect );


    }

    @Override
    public void onResume()
    {
        super.onResume();
        //TEST---
        if(GetChannelExist() == 1) {

            //========  change channel test !!!!  =========
            if(TEST_MODE == 1)
                ChangeChTest_handler.post(Key_Test_Run);
            //===============================

        }
        //
    }

    @Override
    public void onPause()
    {
        super.onPause();


        //========   change channel test !!!!  =========
        if( TEST_MODE == 1) {
            if (t != null) {
                if (ChangeChTest_handler != null && ChangeChTest_handler.getLooper() != null) {
                    ChangeChTest_handler.getLooper().quit();
                    t = null;
                    Log.d(TAG, "on stop : EitUpdateThread  quitLoop()!!!");
                }
            }
            if (ChangeChTest_handler != null)
                ChangeChTest_handler.removeCallbacks(Key_Test_Run);
        }
        //===============================
    }

    public void playByChannelId()
    {
        int chIndex=0; //index of program list
        long channelId=0;
        int groupType = 0;
        int return_code=0;

        for(int i=0; i<2; i++){
            if(i==0)
                groupType = ProgramInfo.ALL_TV_TYPE;
            else
                groupType = ProgramInfo.ALL_RADIO_TYPE;
            //ProgramManagerInit(groupType);
            ProgramManagerList = GetProgramManager(groupType);
            if(ProgramManagerList != null) {
                if (ProgramManagerList.get(0).ProgramManagerInfoList.size() >= 0) {
                    Log.d(TAG, "BtnAvControlPlayByChannelId_OnClick:"+"i="+i+", list size=" + ProgramManagerList.get(0).ProgramManagerInfoList.size());//eric lin test
                    channelId = ProgramManagerList.get(0).ProgramManagerInfoList.get(chIndex).getChannelId();
                    break;
                }
            }
        }

        AvControlOpen(ViewHistory.getPlayId());
        return_code = AvControlPlayByChannelId(ViewHistory.getPlayId(), channelId, groupType,1);
        Log.d(TAG, "BtnAvControlPlayByChannelId_OnClick: ViewHistory.getPlayId()="+ViewHistory.getPlayId());//eric lin test

        //setAVWindowSize();
        //clearInputOuputContext(true, false);
        //txvOutput.setText("Output:\nreturn code="+return_code);

    }


}
