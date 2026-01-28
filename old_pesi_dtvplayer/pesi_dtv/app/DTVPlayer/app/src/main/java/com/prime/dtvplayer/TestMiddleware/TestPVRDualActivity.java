package com.prime.dtvplayer.TestMiddleware;

import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dolphin.dtv.DTVMessage;
import com.dolphin.dtv.HiDtvMediaPlayer;
import com.dolphin.dtv.IDTVListener;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.PvrInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestPVRDualActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();

    private static final String DEFAULT_RECORD_TESTFILE = Environment.getExternalStorageDirectory().getPath() + "/test.ts";
    private static final String DEFAULT_RECORD_TESTFILE2 = Environment.getExternalStorageDirectory().getPath() + "/test2.ts";
    private static final String DEFAULT_TIMESHIFT_TESTFILE = Environment.getExternalStorageDirectory().getPath() + "/test_timeshift.ts";


    private ActivityHelpView help;

    final int mTestTotalFuncCount = 17;    // 9 PVR Record functions
    private int mPosition;  // position of testMidMain

    private Set<Integer> mErrorIndexSet = new HashSet<>();
    private Set<Integer> mTestedFuncSet = new HashSet<>();

    private Toast mToast;

    private SurfaceView mPlaySurfaceView;
    private List<SimpleChannel> mSimpleChannelList;
    private int[] mRecIdArray = new int[2];
    private long mChannelId=0;
    private boolean mIsAvOpen = false;
    TextView txvChIdA,txvChIdB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_pvrdual);

        mSimpleChannelList = ProgramInfoGetSimpleChannelList(ProgramInfo.ALL_TV_TYPE, 1, 1);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        if (mSimpleChannelList == null || mSimpleChannelList.isEmpty())
        {
            ShowToast("No Program, Scan First !");
            finish();
        }

        registCallback ( DTVMessage.HI_SVR_EVT_PVR_CALLBACK_START, DTVMessage.HI_SVR_EVT_PVR_CALLBACK_END, gPVRListener ) ;
        Init();
        for(int i=0; i<mRecIdArray.length ; i++)
            mRecIdArray[i] = -1;
    }

    @Override
    public void onStart()
    {
        super.onStart();
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
    public  void onDestroy() {
        super.onDestroy();
        unregistCallback ( DTVMessage.HI_SVR_EVT_PVR_CALLBACK_START, DTVMessage.HI_SVR_EVT_PVR_CALLBACK_END, gPVRListener ) ;
        PvrRecordStop(0, mRecIdArray[0]);
        PvrRecordStop(0, mRecIdArray[1]);
        AvControlPlayStop(0);
        AvControlClose(0);
    }

    private void registCallback( int callbackCmdStart, int callbackCmdEnd, IDTVListener scanListener)
    {
        for ( int i = callbackCmdStart ; i < callbackCmdEnd ; i++ )
            HiDtvMediaPlayer.getInstance().subScribeEvent(i,scanListener,0) ;
    }

    private void unregistCallback( int callbackCmdStart, int callbackCmdEnd, IDTVListener scanListener)
    {
        for ( int i = callbackCmdStart ; i < callbackCmdEnd ; i++ )
            HiDtvMediaPlayer.getInstance().unSubScribeEvent(i,scanListener) ;
    }

    IDTVListener gPVRListener = new IDTVListener()
    {
        private static final String CB_TAG="PVRListener";
        @Override
        public void notifyMessage(int messageID, int param1, int parm2, Object obj)
        {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            switch (messageID)
            {
                case DTVMessage.HI_SVR_EVT_PVR_PLAY_EOF:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_PLAY_EOF") ;
                    ShowToast("HI_SVR_EVT_PVR_PLAY_EOF");
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PVR_PLAY_SOF:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_PLAY_SOF");
                    ShowToast("HI_SVR_EVT_PVR_PLAY_SOF");
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PVR_PLAY_ERROR:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_PLAY_ERROR") ;
                    ShowToast("HI_SVR_EVT_PVR_PLAY_ERROR");
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PVR_PLAY_REACH_REC:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_PLAY_REACH_REC") ;
                    ShowToast("HI_SVR_EVT_PVR_PLAY_REACH_REC");
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PVR_REC_DISKFULL:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_REC_DISKFULL");
                    ShowToast("HI_SVR_EVT_PVR_REC_DISKFULL");
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PVR_REC_ERROR:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_REC_ERROR") ;
                    ShowToast("HI_SVR_EVT_PVR_REC_ERROR");
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PVR_REC_OVER_FIX:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_REC_OVER_FIX") ;
                    ShowToast("HI_SVR_EVT_PVR_REC_OVER_FIX");
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PVR_REC_REACH_PLAY:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_REC_REACH_PLAY");
                    ShowToast("HI_SVR_EVT_PVR_REC_REACH_PLAY");
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PVR_REC_DISK_SLOW:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_REC_DISK_SLOW") ;
                    ShowToast("HI_SVR_EVT_PVR_REC_DISK_SLOW");
                    break;
                }
                default:
                    break;
            }
        }
    };


    public void PvrRecordGetAllInfo_OnClick(View view)
    {
        final int btnIndex = 0; // it is first button
        try
        {
            List<PvrInfo> pvrList= new ArrayList<PvrInfo>();
            pvrList = PvrRecordGetAllInfo();

            if (pvrList != null)
                Log.d(TAG, "PvrRecordGetAllInfo_OnClick: pvrList != null, size="+pvrList.size());

            if (pvrList != null)
            {
                if(pvrList.size()==0)
                    TestPass(view, "PvrRecordGetAllInfo: no record info");
                if(pvrList.size()==1)
                    TestPass(view, "PvrRecordGetAllInfo: one record info" +
                            " (recId="+pvrList.get(0).getRecId()+", chId="+pvrList.get(0).getChannelId()+", pvrMode="+pvrList.get(0).getPvrMode()+")");
                else if(pvrList.size()==2)
                    TestPass(view, "PvrRecordGetAllInfo: two record info" +
                            " (recId="+pvrList.get(0).getRecId()+", chId="+pvrList.get(0).getChannelId()+", pvrMode="+pvrList.get(0).getPvrMode()+")"
                            +", (recId="+pvrList.get(1).getRecId()+", chId="+pvrList.get(1).getChannelId()+", pvrMode="+pvrList.get(1).getPvrMode()+")");
                else if(pvrList.size()==3)
                    TestPass(view, "PvrRecordGetAllInfo: three record info" +
                            " (recId="+pvrList.get(0).getRecId()+", chId="+pvrList.get(0).getChannelId()+", pvrMode="+pvrList.get(0).getPvrMode()+")"
                            +", (recId="+pvrList.get(1).getRecId()+", chId="+pvrList.get(1).getChannelId()+", pvrMode="+pvrList.get(1).getPvrMode()+")"
                            +", (recId="+pvrList.get(2).getRecId()+", chId="+pvrList.get(2).getChannelId()+", pvrMode="+pvrList.get(2).getPvrMode()+")");
            }
            else
            {
                GotError(view, "Fail : PvrRecordGetAllInfo pvrList==null or pvrList.size()!=2", btnIndex);
            }
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }

    public void PvrRecordA_OnClick(View view)
    {
        final int btnIndex = 1; // it is first button

        final int duration = 60; // 20180508 min 60s
        int recId;
        try
        {
            if(mIsAvOpen==false) {
                Log.d(TAG, "PvrRecordA_OnClick: AvControlOpen");
                AvControlOpen(0);
                mIsAvOpen = true;
            }
            AvControlPlayByChannelId(0, mSimpleChannelList.get(0).getChannelId(), ProgramInfo.ALL_TV_TYPE,1);
            mChannelId = mSimpleChannelList.get(0).getChannelId();
            Rect rect = new Rect();
            int location[] = new int[2];

            mPlaySurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
            mPlaySurfaceView.getLocationOnScreen(location);
            rect.left = location[0];
            rect.top = location[1];
            rect.right = rect.left + mPlaySurfaceView.getWidth();
            rect.bottom = rect.top + mPlaySurfaceView.getHeight();
            AvControlSetWindowSize(0, rect);

            recId = PvrRecordStart(0, mSimpleChannelList.get(0).getChannelId(), DEFAULT_RECORD_TESTFILE, duration);
            if(recId != -1)
                mRecIdArray[0] = recId;
            Log.d(TAG, "PvrRecordGetAllInfo_OnClick: mRecIdArray[0]="+mRecIdArray[0]);

            if (recId != -1)
            {
                TestPass(view, "Rec Start for " + duration +" Secs" +
                        ", (recId_A="+recId+")");
            }
            else
            {
                GotError(view, "Fail : PvrRecordA recId_A="+recId, btnIndex);
            }
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }
    public void PvrRecordB_OnClick(View view)
    {
        final int btnIndex = 2; // it is first button

        final int duration = 60; // 20180508 min 60s
        int recId;
        try
        {
            if(mIsAvOpen==false) {
                AvControlOpen(0);
                mIsAvOpen = true;
            }
            AvControlPlayByChannelId(0, mSimpleChannelList.get(1).getChannelId(), ProgramInfo.ALL_TV_TYPE,1);
            mChannelId = mSimpleChannelList.get(1).getChannelId();
            Rect rect = new Rect();
            int location[] = new int[2];

            mPlaySurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
            mPlaySurfaceView.getLocationOnScreen(location);
            rect.left = location[0];
            rect.top = location[1];
            rect.right = rect.left + mPlaySurfaceView.getWidth();
            rect.bottom = rect.top + mPlaySurfaceView.getHeight();
            AvControlSetWindowSize(0, rect);

            recId = PvrRecordStart(0, mSimpleChannelList.get(1).getChannelId(), DEFAULT_RECORD_TESTFILE2, duration);
            if(recId != -1)
                mRecIdArray[1] = recId;
            Log.d(TAG, "PvrRecordGetAllInfo_OnClick: mRecIdArray[1]="+mRecIdArray[1]);


            if (recId != -1)
            {
                TestPass(view, "Rec Start for " + duration +" Secs" +
                        ", (recId_B="+recId+")");
            }
            else
            {
                GotError(view, "Fail : PvrRecordB recId_B="+recId, btnIndex);
            }
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }



    public void PvrRecordStopA_OnClick(View view)
    {
        final int btnIndex = 4;

        try
        {
            int ret = PvrRecordStop(0, mRecIdArray[0]);


            AvControlPlayStop(0);
            AvControlClose(0);
            mIsAvOpen = false;

            if (ret == 0)
            {
                TestPass(view, "Rec stop, recId_A="+mRecIdArray[0]);
            }
            else
            {
                GotError(view, "Fail : PvrRecordStop (recId_A="+mRecIdArray[0]+",ret="+ret+")", btnIndex);
            }

        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }
    public void PvrRecordStopB_OnClick(View view)
    {
        final int btnIndex = 5;
        try
        {
            Log.d(TAG, "PvrRecordStopB_OnClick: mRecIdArray[1]="+mRecIdArray[1]);
            int ret = PvrRecordStop(0, mRecIdArray[1]);


            AvControlPlayStop(0);
            AvControlClose(0);
            mIsAvOpen = false;

            if (ret == 0)
            {
                TestPass(view, "Rec stop, recId_B="+mRecIdArray[1]);
            }
            else
            {
                GotError(view, "Fail : PvrRecordStop (recId_B="+mRecIdArray[1]+",ret="+ret+")", btnIndex);
            }

        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }



    //Delete file A
    public void DelFileA_OnClick(View view)
    {
        final int btnIndex = 7; // first button
        try {
            File testFile = new File(DEFAULT_RECORD_TESTFILE);
            if (!testFile.exists()) {
                ShowToast("No Record_A TestFile, Record in PVR_Record First !");
                GotError(view, "Fail : DelFileA no test file path=" + DEFAULT_RECORD_TESTFILE, btnIndex);
                return;
            } else {
                //
                if (testFile.delete()) {
                    TestPass(view, "Delete FileA Success!");
                } else {
                    GotError(view, "Delete FileA operation is failed.", btnIndex);
                }
            }
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }

    //Delete file B
    public void DelFileB_OnClick(View view)
    {
        final int btnIndex = 8; // first button
        try {
            File testFile = new File(DEFAULT_RECORD_TESTFILE2);
            if (!testFile.exists()) {
                ShowToast("No Record_B TestFile, Record in PVR_Record First !");
                GotError(view, "Fail : DelFileB no test file path=" + DEFAULT_RECORD_TESTFILE2, btnIndex);
                return;
            } else {
                //
                if (testFile.delete()) {
                    TestPass(view, "Delete FileB Success!");
                } else {
                    GotError(view, "Delete FileB operation is failed.", btnIndex);
                }
            }
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }

    //Play file A
    public void PvrPlayStartA_OnClick(View view)
    {
        final int btnIndex = 9; // first button
        Rect rect = new Rect();
        int location[] = new int[2];

        try
        {
            File testFile = new File(DEFAULT_RECORD_TESTFILE);
            if (!testFile.exists())
            {
                ShowToast("No Record_A TestFile, Record in PVR_Record First !");
                GotError(view, "Fail : PvrPlayStart no test file path=" + DEFAULT_RECORD_TESTFILE, btnIndex);
                return;
            }

            AvControlPlayStop(0);
            AvControlClose(0);
            mIsAvOpen = false;

            int ret = PvrPlayStart(DEFAULT_RECORD_TESTFILE);
            mPlaySurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
            mPlaySurfaceView.getLocationOnScreen(location);
            rect.left = location[0];
            rect.top = location[1];
            rect.right = rect.left + mPlaySurfaceView.getWidth();
            rect.bottom = rect.top + mPlaySurfaceView.getHeight();
            PvrPlaySetWindowRect(rect);

            if (ret == 0)
            {
                TestPass(view, "PlayStart, Record_A File  = " + DEFAULT_RECORD_TESTFILE);
            }
            else
            {
                GotError(view, "Fail : PvrPlayStart Record_A return " + ret, btnIndex);
            }
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }

    //Play file B
    public void PvrPlayStartB_OnClick(View view)
    {
        final int btnIndex = 10; // first button
        Rect rect = new Rect();
        int location[] = new int[2];

        try
        {
            File testFile = new File(DEFAULT_RECORD_TESTFILE2);
            if (!testFile.exists())
            {
                ShowToast("No Record_B TestFile, Record in PVR_Record First !");
                GotError(view, "Fail : PvrPlayStart no test file path=" + DEFAULT_RECORD_TESTFILE2, btnIndex);
                return;
            }

            AvControlPlayStop(0);
            AvControlClose(0);
            mIsAvOpen = false;

            int ret = PvrPlayStart(DEFAULT_RECORD_TESTFILE2);
            mPlaySurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
            mPlaySurfaceView.getLocationOnScreen(location);
            rect.left = location[0];
            rect.top = location[1];
            rect.right = rect.left + mPlaySurfaceView.getWidth();
            rect.bottom = rect.top + mPlaySurfaceView.getHeight();
            PvrPlaySetWindowRect(rect);

            if (ret == 0)
            {
                TestPass(view, "PlayStart, Record_B File  = " + DEFAULT_RECORD_TESTFILE);
            }
            else
            {
                GotError(view, "Fail : PvrPlayStart Record_B return " + ret, btnIndex);
            }
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }

    //Stop play file
    public void PvrPlayStop_OnClick(View view)
    {
        final int btnIndex = 11;

        try
        {
            int ret = PvrPlayStop();
            if (ret == 0)
            {
                TestPass(view, "PlayStop");
            }
            else
            {
                GotError(view, "Fail : PvrPlayStop return -1", btnIndex);
            }
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }



    public void PvrTimeShiftStart_OnClick(View view)
    {
        final int btnIndex = 13; // first btn

        // default value by
        // http://10.1.4.201:8080/o8/xref/device/hisilicon/bigfish/hidolphin/component/dtv/dtvjava/java/com/dolphin/dtv/play/LocalPlayer.java#647
        int time = 900;
        int fileSize = 0;

        if(mIsAvOpen==false) {
            AvControlOpen(0);
            mIsAvOpen = true;
        }
        AvControlPlayByChannelId(0, mSimpleChannelList.get(2).getChannelId(), ProgramInfo.ALL_TV_TYPE,1);

        mChannelId = mSimpleChannelList.get(2).getChannelId();
        Rect rect = new Rect();
        int location[] = new int[2];

        mPlaySurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        mPlaySurfaceView.getLocationOnScreen(location);
        rect.left = location[0];
        rect.top = location[1];
        rect.right = rect.left + mPlaySurfaceView.getWidth();
        rect.bottom = rect.top + mPlaySurfaceView.getHeight();
        AvControlSetWindowSize(0, rect);

        try
        {
            int ret = PvrTimeShiftStart(0, time, fileSize, DEFAULT_TIMESHIFT_TESTFILE);
            if (ret == 0)
            {
                TestPass(view, "PvrTimeShiftStart , time = " + time);
            }
            else
            {
                GotError(view, "Fail : PvrTimeShiftStart return " + ret, btnIndex);
            }
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }

    public void PvrTimeShiftStop_OnClick(View view)
    {
        final int btnIndex = 14;

        try
        {
            int ret = PvrTimeShiftStop(0);
            if (ret == 0)
            {
                TestPass(view, "PvrTimeShiftStop");
            }
            else
            {
                GotError(view, "Fail : PvrTimeShiftStop return " + ret, btnIndex);
            }
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }

    public void PvrTimeShiftPlay_OnClick(View view)
    {
        final int btnIndex = 15;

        try
        {
            int ret = PvrTimeShiftPlay(0);
            if (ret == 0)
            {
                TestPass(view, "PvrTimeShiftPlay");
            }
            else
            {
                GotError(view, "Fail : PvrTimeShiftPlay return " + ret, btnIndex);
            }
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }

    public void PvrTimeShiftPause_OnClick(View view)
    {
        final int btnIndex = 16;

        try
        {
            int ret = pvrTimeShiftFilePause(0);//Scoty 20180827 add and modify TimeShift Live Mode
            if (ret == 0)
            {
                TestPass(view, "pvrTimeShiftFilePause");
            }
            else
            {
                GotError(view, "Fail : pvrTimeShiftFilePause return " + ret, btnIndex);
            }
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }

    private void Init()
    {
        ActivityTitleView title = (ActivityTitleView) findViewById(R.id.ID_TESTPVR_LAYOUT_TITLE);
        help = (ActivityHelpView) findViewById(R.id.ID_TESTPVR_LAYOUT_HELP);

        // init position
        Bundle bundle = this.getIntent().getExtras();
        if ( bundle != null )
        {
            mPosition = bundle.getInt("position", 0);
        }

        // init title
        title.setTitleView("TestMiddlewareMain > TestPVR_Dual");

        // init help
        help.setHelpInfoText(null, null);
        help.setHelpRedText(null);
        help.setHelpGreenText(null);
        help.setHelpBlueText(null);
        help.setHelpYellowText(null);

        mPlaySurfaceView = (SurfaceView) findViewById(R.id.ID_TESTPVR_SURFACEVIEW_PLAY);

        //init text view
        txvChIdA = (TextView)findViewById(R.id.TxvChIdA);
        txvChIdB = (TextView)findViewById(R.id.TxvChIdB);
        String strA = "chIdA:" +Long.toString(mSimpleChannelList.get(0).getChannelId());
        String strB = "chIdB:" +Long.toString(mSimpleChannelList.get(1).getChannelId());
        txvChIdA.setText(strA);
        txvChIdB.setText(strB);
    }

    private void GotError(View view, String errorMsg, int btnIndex)
    {
        Button button = (Button) findViewById(view.getId());

        help.setHelpInfoText( null, errorMsg );
        button.setTextColor(0xFFFF0000);    // red
        mErrorIndexSet.add(btnIndex);
        mTestedFuncSet.add(view.getId());
    }

    private void TestPass(View view, String msg)
    {
        Button button = (Button) findViewById(view.getId());

        help.setHelpInfoText( null, msg );
        button.setTextColor(0xFF00FF00);    // green
        mTestedFuncSet.add(view.getId());
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

    /*public void PvrRecordTwo_OnClick(View view)
    {
        final int btnIndex = 3; // it is first button

        final int duration = 60; // 20180508 min 60s
        try
        {
            if(mIsAvOpen==false) {
                AvControlOpen(0);
                mIsAvOpen = true;
            }
            AvControlPlayByChannelId(0, mSimpleChannelList.get(0).getChannelId(), ProgramInfo.ALL_TV_TYPE,1);
            mChannelId = mSimpleChannelList.get(0).getChannelId();
            Rect rect = new Rect();
            int location[] = new int[2];

            mPlaySurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
            mPlaySurfaceView.getLocationOnScreen(location);
            rect.left = location[0];
            rect.top = location[1];
            rect.right = rect.left + mPlaySurfaceView.getWidth();
            rect.bottom = rect.top + mPlaySurfaceView.getHeight();
            AvControlSetWindowSize(0, rect);

            mRecIdArray[0] = PvrRecordStart(0, mSimpleChannelList.get(0).getChannelId(), DEFAULT_RECORD_TESTFILE, duration);
            Log.d(TAG, "PvrRecordGetAllInfo_OnClick: mRecIdArray[0]="+mRecIdArray[0]);
            mRecIdArray[1] = PvrRecordStart(0, mSimpleChannelList.get(1).getChannelId(), DEFAULT_RECORD_TESTFILE2, duration);
            Log.d(TAG, "PvrRecordGetAllInfo_OnClick: mRecIdArray[1]="+mRecIdArray[1]);


            if (mRecIdArray[0] != -1 && mRecIdArray[1] != -1)
            {
                TestPass(view, "Rec Start for " + duration +" Secs" +
                        ", (recId="+mRecIdArray[0]+")"+
                        ", (recId="+mRecIdArray[1]+")");
            }
            else
            {
                GotError(view, "Fail : record two. recId1="+mRecIdArray[0]+", recId2="+mRecIdArray[1], btnIndex);
            }
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }*/

    /*public void PvrRecordStopAll_OnClick(View view)
    {
        final int btnIndex = 6;

        try
        {
            int ret1 = PvrRecordStop(0, mRecIdArray[0]);
            int ret2 = PvrRecordStop(0, mRecIdArray[1]);
//            PvrRecordStop(0, 0);
//            PvrRecordStop(0, 1);
//            PvrRecordStop(0, 2);
//            PvrRecordStop(0, 3);
//            PvrRecordStop(0, 4);
//            PvrRecordStop(0, 5);
//            PvrRecordStop(0, 6);
//            PvrRecordStop(0, 7);

            AvControlPlayStop(0);
            AvControlClose(0);
            mIsAvOpen = false;

            if (ret1 == 0 && ret2 == 0)
            {
                TestPass(view, "Rec stop, recId1="+mRecIdArray[0]+", recId2="+mRecIdArray[1]);
            }
            else
            {
                GotError(view, "Fail : PvrRecordStop (recId1="+mRecIdArray[0]+",ret="+ret1+"), "+
                        "(recId2="+mRecIdArray[1]+",ret="+ret2+")", btnIndex);
            }

        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }*/

    /*public void PvrRecordTwoTimeshiftOne_OnClick(View view)
    {
        final int btnIndex = 12; // it is first button

        final int duration = 60; // 20180508 min 60s
        try
        {
            if(mIsAvOpen==false) {
                AvControlOpen(0);
                mIsAvOpen = true;
            }
            AvControlPlayByChannelId(0, mSimpleChannelList.get(2).getChannelId(), ProgramInfo.ALL_TV_TYPE,1);

            mChannelId = mSimpleChannelList.get(2).getChannelId();
            Rect rect = new Rect();
            int location[] = new int[2];

            mPlaySurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
            mPlaySurfaceView.getLocationOnScreen(location);
            rect.left = location[0];
            rect.top = location[1];
            rect.right = rect.left + mPlaySurfaceView.getWidth();
            rect.bottom = rect.top + mPlaySurfaceView.getHeight();
            AvControlSetWindowSize(0, rect);

            mRecIdArray[0] = PvrRecordStart(0, mSimpleChannelList.get(0).getChannelId(), DEFAULT_RECORD_TESTFILE, duration);
            Log.d(TAG, "PvrRecordGetAllInfo_OnClick: mRecIdArray[0]="+mRecIdArray[0]);
            mRecIdArray[1] = PvrRecordStart(0, mSimpleChannelList.get(1).getChannelId(), DEFAULT_RECORD_TESTFILE2, duration);
            Log.d(TAG, "PvrRecordGetAllInfo_OnClick: mRecIdArray[1]="+mRecIdArray[1]);



            if (mRecIdArray[0] != -1 && mRecIdArray[0] != -1)
            {
                TestPass(view, "Rec Start for " + duration +" Secs" +
                        ", (recId="+mRecIdArray[0]+")"+
                        ", (recId="+mRecIdArray[1]+")");
            }
            else
            {
                GotError(view, "Fail : record two. recId1="+mRecIdArray[0]+", recId2="+mRecIdArray[1], btnIndex);
            }
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }*/
}
