package com.prime.dtvplayer.TestMiddleware;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ChannelHistory;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestPipActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();
    private ActivityHelpView help;
    private int mPosition;  // position of testMidMain
    final int mTestTotalFuncCount = 16;    // 16 functions
    private Rect SubRect;
    private Set<Integer> mErrorIndexSet = new HashSet<>();
    private Set<Integer> mTestedFuncSet = new HashSet<>();
    private List<SimpleChannel> mSimpleChannelList;
    private SurfaceView mainSurfaceView;
    private PipChannelChangeReceiver mPipChannelChangeReceiver = null;
    private Intent startPipChannelChangeIntent = null;
    private ChannelHistory History = ChannelHistory.GetInstance();
    private Toast mToast;
    private int isPipOpen = 0;
    private TextView MainTxt;
    private TextView SubTxt;
    private List<ProgramManagerImpl> ProgramManagerList = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_pip);
        mainSurfaceView = (SurfaceView)findViewById(R.id.ID_TESTPIP_MAIN_SURFACEVIEW) ;
        MainTxt = (TextView)findViewById(R.id.ID_TESTPIP_MAIN_PROGRAM);
        SubTxt = (TextView)findViewById(R.id.ID_TESTPIP_SUB_PROGRAM);
        setSurfaceView(mainSurfaceView);
        subBroadcast();
        Init();
        mSimpleChannelList = ProgramInfoGetSimpleChannelList(ProgramInfo.ALL_TV_TYPE, 1, 1);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        if (mSimpleChannelList == null || mSimpleChannelList.isEmpty())
        {
            ShowToast("No Program, Scan First !");
            finish();
        }
    }

    @Override
    public void onBackPressed() {

        TestMidMain tm = new TestMidMain();
        // if all funcs are tested, set checked = true
        if ( mTestedFuncSet.size() == mTestTotalFuncCount )
        {
            int result = 0;
            tm.getTestInfoByIndex(mPosition).setChecked(true);

            // send error item to testMidMain
            for (int index : mErrorIndexSet) {
                result = tm.bitwiseLeftShift(result, index, false);    //fail item
            }

            tm.getTestInfoByIndex(mPosition).setResult(result);
        }

        super.onBackPressed();
    }
    @Override
    public void onPause() {
        super.onPause();
        //AvControlPlayStop(History.getPlayId());
        //AvControlClose(History.getPlayId());
        if(CheckPipOpen() == 1) {
            Log.d(TAG,"onPause pip stop,close !");
            //PipStop();
            //PipClose();
            //isPipOpen = 0;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //AvControlPlayStop(History.getPlayId());
        //AvControlClose(History.getPlayId());
        if(CheckPipOpen() == 1) {
            Log.d(TAG,"onStop pip stop,close !");
            //PipStop();
            //PipClose();
            //isPipOpen = 0;
        }
    }
    public void onDestroy() {
        Log.d(TAG, "onDestroy:");
        super.onDestroy();
        unSubBroadcast();
        stopPipChannelChangeService();
    }
    private void Init() {
        int ret;
        ActivityTitleView title = (ActivityTitleView) findViewById(R.id.ID_TESTPIP_LAYOUT_TITLE);
        help = (ActivityHelpView) findViewById(R.id.ID_TESTPIP_LAYOUT_HELP);
        // init position
        Bundle bundle = this.getIntent().getExtras();
        if ( bundle != null )
        {
            mPosition = bundle.getInt("position", 0);
        }

        // init title
        title.setTitleView("TestMiddlewareMain > TestPip");

        // init help
        help.setHelpInfoText(null, null);
        help.setHelpRedText(null);
        help.setHelpGreenText(null);
        help.setHelpBlueText(null);
        help.setHelpYellowText(null);
//        PipStop();
//        PipClose();
//        try {
//            Thread.sleep(200);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        if(AvControlGetPlayStatus(History.getPlayId()) != HiDtvMediaPlayer.EnPlayStatus.STOP.getValue()) {
//            ret = AvControlPlayStop(History.getPlayId());
//            Log.d(TAG, "AvControlPlayStop(" + History.getPlayId() + ")ret = " + ret);
//            AvControlClose(History.getPlayId());
//            Log.d(TAG, "AvControlClose(" + History.getPlayId() + ")ret = " + ret);
//        }
        if(History.getCurChannel() != null)
            MainTxt.setText("Main Program : "+History.getCurChannel().getChannelName());
        else
            MainTxt.setText("Main Program : None");
        if(History.getCurPipChannel() != null) {
            SubTxt.setText("Sub Program : " + History.getCurPipChannel().getChannelName());
            isPipOpen = 0;
        }
        else
            SubTxt.setText("Sub Program : None");

        setPipWindowSize();
        Log.d(TAG,"init isPipOpen = "+isPipOpen);
    }

    public int CheckPipOpen() {
        return isPipOpen;
    }

    public int CheckPipNeedClose() {
        int ret = 0;
        if(History.getCurPipChannel() != null) {
            if (History.getCurPipChannel().getChannelId() == History.getCurChannel().getChannelId()) {
                ShowToast("main channel is same as pip channel, so close pip now!!!");
                ret = 1;
            }
            if (History.getCurPipChannel().getTpId() != History.getCurChannel().getTpId()) {
                ShowToast("main channel's Tp not same as pip channel's Tp, so close pip now!!!");
                ret = 1;
            }
        }
        return ret;
    }

    public void setAVWindowSize() {
        Rect rect = new Rect(796,130,1812,724);
        AvControlSetWindowSize(ViewHistory.getPlayId(), rect );
    }

    public void setPipWindowSize() {
        SubRect = new Rect(1450,500,300,180);
        //SubRect = new Rect(0,0,0,0);
    }

    public void BtnPipMainPlayByChannelId(View view)
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
                    Log.d(TAG, "TestPipPlayByChannelId:"+"i="+i+", list size=" + ProgramManagerList.get(0).ProgramManagerInfoList.size());//eric lin test
                    channelId = ProgramManagerList.get(0).ProgramManagerInfoList.get(chIndex).getChannelId();
                    break;
                }
            }
        }
        //setAVWindowSize();
        Log.d(TAG,"BtnPipMainPlayByChannelId channel id : "+channelId+" channel num : "+ViewHistory.getCurChannel().getChannelNum());
        if(GetChannelExist()==1) {
            Log.d(TAG,"GetChannelExist()="+GetChannelExist());
            AvControlOpen(History.getPlayId());
        }
        History.SetCurChannel(GetSimpleProgramByChannelIdfromTotalChannelListByGroup(History.getCurGroupType(),channelId),ProgramInfoGetSimpleChannelList(ProgramInfo.ALL_TV_TYPE,0,0),ProgramInfo.ALL_TV_TYPE);
        if(History.getCurChannel() != null)
        {
            AvControlPlayByChannelId(History.getPlayId(), History.getCurChannel().getChannelId(), History.getCurGroupType(),1);
            MainTxt.setText("Main Program : "+History.getCurChannel().getChannelName());
        }
        Log.d(TAG, "TestPipPlayByChannelId: ViewHistory.getPlayId()="+ViewHistory.getPlayId());//eric lin test
        setAVWindowSize();
    }

    public void BtnPipMainPlayUp_OnClick(View view)
    {
        int ret;
        History.setChannelUp();
        if(CheckPipNeedClose() == 1) {
            PipStop();
            PipClose();
            History.setCurPipIndex(-1);
            isPipOpen = 0;
            SubTxt.setText("Sun Program : None");
        }
        MainTxt.setText("Main Program : "+History.getCurChannel().getChannelName());
        ret = super.AvControlPlayByChannelId(History.getPlayId(),History.getCurChannel().getChannelId(),History.getCurGroupType(),1);
    }

    public void BtnPipMainPlayDown_OnClick(View view)
    {
        int ret;
        History.setChannelDown();
        if(CheckPipNeedClose() == 1) {
            PipStop();
            PipClose();
            History.setCurPipIndex(-1);
            isPipOpen = 0;
            SubTxt.setText("Sun Program : None");
        }
        MainTxt.setText("Main Program : "+History.getCurChannel().getChannelName());
        ret = super.AvControlPlayByChannelId(History.getPlayId(),History.getCurChannel().getChannelId(),History.getCurGroupType(),1);
    }

    public void BtnPipMainPlayStop_OnClick(View view)
    {
        int ret = AvControlPlayStop(History.getPlayId());
        MainTxt.setText("Main Program : None");
    }

    public void BtnPipMainClose_OnClick(View view)
    {
        int ret = AvControlClose(History.getPlayId());
        MainTxt.setText("Main Program : None");
    }

    public void BtnPipOpen_OnClick(View view) {
        int ret;
        Log.d(TAG,"SubRect.left : "+SubRect.left);
        Log.d(TAG,"SubRect.top : "+SubRect.top);
        Log.d(TAG,"SubRect.right : "+SubRect.right);
        Log.d(TAG,"SubRect.bottom : "+SubRect.bottom);
        ret = PipOpen(SubRect.left,SubRect.top,SubRect.right,SubRect.bottom);
        if(ret == 0)
            isPipOpen = 1;
    }

    public void BtnPipClose_OnClick(View view)
    {
        int ret = PipClose();
        SubTxt.setText("Sun Program : None");
        History.setCurPipIndex(-1);
        isPipOpen = 0;
    }

    public void BtnPipStart_OnClick(View view)
    {
        int ret;
        History.setCurPipChannel(this);//Scoty 20180724 modify open pip rule first: record channel, second: previous watched channel, third: next channel
        SimpleChannel channel = History.getCurPipChannel();
        Log.d(TAG,"BtnPipPlay_OnClick channel id : "+channel.getChannelId()+" channel num : "+channel.getChannelNum());
        if(CheckPipOpen() == 0) {
            ShowToast("[ERROR] Pip not open !!!");
        }
        else {
            ret = PipStart(channel.getChannelId(),1);
            ret = PipSetWindow(SubRect.left,SubRect.top,SubRect.right,SubRect.bottom);
            SubTxt.setText("Sub Program : "+channel.getChannelName());
        }
    }

    public void BtnPipPlayUp_OnClick(View view)
    {
        History.setPipChannelUp();
        int ret = PipStart(History.getCurPipChannel().getChannelId(),1);
        SubTxt.setText("Sub Program : "+History.getCurPipChannel().getChannelName());
    }

    public void BtnPipPlayDown_OnClick(View view)
    {
        History.setPipChannelDown();
        int ret = PipStart(History.getCurPipChannel().getChannelId(),1);
        SubTxt.setText("Sub Program : "+History.getCurPipChannel().getChannelName());
    }

    public void BtnPipStop_OnClick(View view)
    {
        int ret = PipStop();
        SubTxt.setText("Sun Program : None");
    }

    public void BtnPipSetWindow_OnClick(View view)
    {
        int ret = PipSetWindow(SubRect.left-400,SubRect.top,SubRect.right,SubRect.bottom);
    }

    public void BtnPipExChange_OnClick(View view)
    {
        History.setAvPipExchange();
        int ret = PipExChange();
        MainTxt.setText("Main Program : "+History.getCurChannel().getChannelName());
        SubTxt.setText("Sub Program : "+History.getCurPipChannel().getChannelName());
        //setPipWindowSize();
    }

    public void BtnPipExChange100Times_OnClick(View view)
    {
        int ret = 0;
        for(int i = 0; i < 100 ; i++) {
            BtnPipExChange_OnClick(null);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(ret != 0) {
            ShowToast("PipExChange fail !!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }
    public void BtnPipChannelChangeStart_OnClick(View view)
    {
        startPipChannelChangeService();
    }
    public void BtnPipChannelChangeStop_OnClick(View view)
    {
        stopPipChannelChangeService();
    }

    private void ShowToast(String string)
    {
        if (mToast == null)
        {
            mToast = Toast.makeText(this, string, Toast.LENGTH_SHORT);
        }
        else
        {
            mToast.setText(string);
        }

        mToast.show();
    }

    private void startPipChannelChangeService()
    {
        startPipChannelChangeIntent = new Intent(this, TestPipChannelChangeService.class);
        startPipChannelChangeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startService(startPipChannelChangeIntent);
    }

    private void stopPipChannelChangeService()
    {
        if(startPipChannelChangeIntent != null) {
            stopService(startPipChannelChangeIntent);
            startPipChannelChangeIntent = null;
        }
    }

    private class PipChannelChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent data) {
            BtnPipPlayUp_OnClick(null);
        }
    }
    private void subBroadcast()
    {
        IntentFilter changeAVFilter = new IntentFilter(TestPipChannelChangeService.TEST_PIP_ALARM_REMIND);
        mPipChannelChangeReceiver = new PipChannelChangeReceiver();
        registerReceiver(mPipChannelChangeReceiver, changeAVFilter, TestPipChannelChangeService.PERMISSION_DTV_BROADCAST, null);
    }

    private void unSubBroadcast()
    {
        unregisterReceiver(mPipChannelChangeReceiver);
        mPipChannelChangeReceiver = null;
    }
}
