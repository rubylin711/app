package com.prime.dtvplayer.TestMiddleware;

import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dolphin.dtv.DTVMessage;
import com.dolphin.dtv.EnTrickMode;
import com.dolphin.dtv.HiDtvMediaPlayer;
import com.dolphin.dtv.IDTVListener;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestPVRTimeShiftActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();

    // private static final String DEFAULT_RECORD_TESTFILE = Environment.getExternalStorageDirectory().getPath() + "/test.ts";

    private ActivityHelpView help;

    final int mTestTotalFuncCount = 14;    // 14 PVR_TimeShift functions
    private int mPosition;  // position of testMidMain

    private Set<Integer> mErrorIndexSet = new HashSet<>();
    private Set<Integer> mTestedFuncSet = new HashSet<>();

    private Toast mToast;
    private SurfaceView mPlaySurfaceView;
    private List<SimpleChannel> mSimpleChannelList;
    private List<ProgramInfo> mProgramInfoList;
    private EnTrickMode mEnTrickMode = EnTrickMode.FAST_FORWARD_NORMAL;
    private String mFilePath = Environment.getExternalStorageDirectory().getPath() + "/test_timeshift.ts";
    private static final String DEFAULT_RECORD_TESTFILE = Environment.getExternalStorageDirectory().getPath() + "/test.ts";
    private long mChannelId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_pvr_time_shift);

        /*mProgramInfoList = ProgramInfoGetList(ProgramInfo.ALL_TV_TYPE);
        if (mProgramInfoList == null || mProgramInfoList.isEmpty())
        {
            ShowToast("No Program, Scan First !");
            finish();
        }*/

        mSimpleChannelList = ProgramInfoGetSimpleChannelList(ProgramInfo.ALL_TV_TYPE, 1, 1);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        if (mSimpleChannelList == null || mSimpleChannelList.isEmpty())
        {
            ShowToast("No Program, Scan First !");
            finish();
        }

        registCallback ( DTVMessage.HI_SVR_EVT_PVR_CALLBACK_START, DTVMessage.HI_SVR_EVT_PVR_CALLBACK_END, gPVRListener ) ;
        Init();

        AvControlOpen(0);
//        AvControlPlayByChannelId(0, mProgramInfoList.get(0).getChannelId(), ProgramInfo.ALL_TV_TYPE,1);
        AvControlPlayByChannelId(0, mSimpleChannelList.get(0).getChannelId(), ProgramInfo.ALL_TV_TYPE,1);
        mChannelId = mSimpleChannelList.get(0).getChannelId();
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onWindowFocusChanged (boolean hasFocus) {
        if (hasFocus)
        {
            Rect rect = new Rect();
            int location[] = new int[2];
            mPlaySurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
            mPlaySurfaceView.getLocationOnScreen(location);
            rect.left = location[0];
            rect.top = location[1];
            rect.right = rect.left + mPlaySurfaceView.getWidth();
            rect.bottom = rect.top + mPlaySurfaceView.getHeight();
            AvControlSetWindowSize(0, new Rect(rect));
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
    public  void onDestroy() {
        super.onDestroy();
        unregistCallback ( DTVMessage.HI_SVR_EVT_PVR_CALLBACK_START, DTVMessage.HI_SVR_EVT_PVR_CALLBACK_END, gPVRListener ) ;
        PvrTimeShiftStop(0);
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

    public void PvrTimeShiftStart_OnClick(View view)
    {
        final int btnIndex = 0; // first btn

        // default value by
        // http://10.1.4.201:8080/o8/xref/device/hisilicon/bigfish/hidolphin/component/dtv/dtvjava/java/com/dolphin/dtv/play/LocalPlayer.java#647
        int time = 900;
        int fileSize = 0;

        try
        {
            int ret = PvrTimeShiftStart(0, time, fileSize, DEFAULT_RECORD_TESTFILE);
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
        final int btnIndex = 1;

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
        final int btnIndex = 2;

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
        final int btnIndex = 3;

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

    public void PvrTimeShiftTrickPlay_OnClick(View view)
    {
        final int btnIndex = 4;

        try
        {
            if (mEnTrickMode == EnTrickMode.INVALID_TRICK_MODE)
            {
                mEnTrickMode = EnTrickMode.FAST_FORWARD_NORMAL;
            }
            else if (mEnTrickMode == EnTrickMode.FAST_FORWARD_THIRTYTWO)
            {
                mEnTrickMode = EnTrickMode.FAST_BACKWARD_NORMAL;
            }
            else if (mEnTrickMode == EnTrickMode.FAST_BACKWARD_THIRTYTWO)
            {
                mEnTrickMode = EnTrickMode.SLOW_FORWARD_THIRTYTWO;
            }
            else if (mEnTrickMode == EnTrickMode.SLOW_FORWARD_TWO)
            {
                mEnTrickMode = EnTrickMode.SLOW_BACKWARD_THIRTYTWO;
            }
            else if (mEnTrickMode == EnTrickMode.SLOW_BACKWARD_TWO)
            {
                mEnTrickMode = EnTrickMode.INVALID_TRICK_MODE;
            }
            else
            {
                mEnTrickMode = EnTrickMode.valueOf(mEnTrickMode.getValue() * 2);
            }


            int ret = PvrTimeShiftTrickPlay(0, mEnTrickMode);
            if (ret == 0)
            {
                TestPass(view, "PvrTimeShiftTrickPlay  mode = " + mEnTrickMode);
            }
            else
            {
                GotError(view, "Fail : PvrTimeShiftTrickPlay return " + ret, btnIndex);
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

    public void PvrTimeShiftSeekPlay_OnClick(View view)
    {
        final int btnIndex = 5;

        int seekSec = 5;
        try
        {
            int ret = PvrTimeShiftSeekPlay(0, seekSec);
            if (ret == 0)
            {
                TestPass(view, "PvrTimeShiftSeekPlay, seekSec = " + seekSec);
            }
            else
            {
                GotError(view, "Fail : PvrTimeShiftSeekPlay return " + ret, btnIndex);
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

    public void PvrTimeShiftGetPlayedTime_OnClick(View view)
    {
        final int btnIndex = 6;

        try
        {
            Date ret = PvrTimeShiftGetPlayedTime(0);
            if (ret != null)
            {
                TestPass(view, "PvrTimeShiftGetPlayedTime = " + ret);
            }
            else
            {
                GotError(view, "Fail : PvrTimeShiftGetPlayedTime return NULL", btnIndex);
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

    public void PvrTimeShiftGetPlaySecond_OnClick(View view)
    {
        final int btnIndex = 7;

        try
        {
            int ret = PvrTimeShiftGetPlaySecond(0);
            if (ret != -1)
            {
                TestPass(view, "PvrTimeShiftGetPlaySecond = " + ret);
            }
            else
            {
                GotError(view, "Fail : PvrTimeShiftGetPlaySecond return -1", btnIndex);
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

    public void PvrTimeShiftGetBeginTime_OnClick(View view)
    {
        final int btnIndex = 8;

        try
        {
            Date ret = PvrTimeShiftGetBeginTime(0);
            if (ret != null)
            {
                TestPass(view, "PvrTimeShiftGetBeginTime = " + ret);
            }
            else
            {
                GotError(view, "Fail : PvrTimeShiftGetBeginTime return NULL", btnIndex);
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

    public void PvrTimeShiftGetBeginSecond_OnClick(View view)
    {
        final int btnIndex = 9;

        try
        {
            int ret = PvrTimeShiftGetBeginSecond(0);
            if (ret != -1)
            {
                TestPass(view, "PvrTimeShiftGetBeginSecond = " + ret);
            }
            else
            {
                GotError(view, "Fail : PvrTimeShiftGetBeginSecond return -1", btnIndex);
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

    public void PvrTimeShiftGetRecordTime_OnClick(View view)
    {
        final int btnIndex = 10;

        try
        {
            int ret = PvrTimeShiftGetRecordTime(0);
            if (ret != -1)
            {
                TestPass(view, "PvrTimeShiftGetRecordTime = " + ret);
            }
            else
            {
                GotError(view, "Fail : PvrTimeShiftGetRecordTime return -1", btnIndex);
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

    public void PvrTimeShiftGetStatus_OnClick(View view)
    {
        final int btnIndex = 11;

        try
        {
            int ret = PvrTimeShiftGetStatus(0);
            if (ret != -1)
            {
                TestPass(view, "PvrTimeShiftGetStatus = " + ret);
            }
            else
            {
                GotError(view, "Fail : PvrTimeShiftGetStatus return -1", btnIndex);
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

    public void PvrTimeShiftGetCurrentTrickMode_OnClick(View view)
    {
        final int btnIndex = 12;

        try
        {
            EnTrickMode ret = PvrTimeShiftGetCurrentTrickMode(0);
            if (ret != EnTrickMode.INVALID_TRICK_MODE)
            {
                TestPass(view, "PvrTimeShiftGetCurrentTrickMode = " + ret);
            }
            else
            {
                GotError(view, "Fail : PvrTimeShiftGetCurrentTrickMode return EnTrickMode.INVALID_TRICK_MODE", btnIndex);
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

    public void PvrGetCurrentPvrMode_OnClick(View view)
    {
        final int btnIndex = 13;

        try
        {
            int recMode = PvrGetCurrentPvrMode(mChannelId);
            if (recMode != -1)
            {
                TestPass(view, "PvrGetCurrentRecMode  = " + recMode);
            }
            else
            {
                GotError(view, "Fail : PvrGetCurrentRecMode return -1", btnIndex);
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

    public void PvrTimeShiftLivePause_OnClick(View view)
    {
        final int btnIndex = 14;
        try
        {
            int ret = pvrTimeShiftLivePause( 0 );
            Log.d(TAG, "PvrTimeShiftLivePause_OnClick:  ret =" + ret);
            if (ret == 0)
            {
                TestPass(view, "pvrTimeShiftLivePause");
            }
            else
            {
                GotError(view, "Fail : pvrTimeShiftLivePause return " + ret, btnIndex);
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

    public void pvrPlayTimeShiftStop_OnClick(View view)
    {
        final int btnIndex = 15;
        try
        {
            int ret = pvrPlayTimeShiftStop();
            Log.d(TAG, "pvrPlayTimeShiftStop:  ret =" + ret);
            if (ret == 0)
            {
                TestPass(view, "pvrPlayTimeShiftStop  ret =" + ret);
            }
            else
            {
                GotError(view, "Fail : pvrPlayTimeShiftStop_OnClick " + ret, btnIndex);
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

    public void pvrRecordGetLivePauseTime_OnClick(View view)
    {
        final int btnIndex = 16;
        try
        {
            int ret = pvrRecordGetLivePauseTime();
            Log.d(TAG, "pvrRecordGetLivePauseTime_OnClick:  ret =" + ret);
            if (ret != -1)
            {
                TestPass(view, "pvrRecordGetLivePauseTime_OnClick : time = " + ret);
            }
            else
            {
                GotError(view, "Fail : pvrRecordGetLivePauseTime_OnClick " + ret, btnIndex);
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

//    public void PvrGetCurrentPlayMode_OnClick(View view)
//    {
//        final int btnIndex = 14;
//
//        try
//        {
//            int playMode = PvrGetCurrentPlayMode();
//            if (playMode != -1)
//            {
//                TestPass(view, "PvrGetCurrentPlayMode  = " + playMode);
//            }
//            else
//            {
//                GotError(view, "Fail : PvrGetCurrentPlayMode return -1", btnIndex);
//            }
//        }
//        catch (Exception e)
//        {
//            Writer writer = new StringWriter();
//            e.printStackTrace(new PrintWriter(writer));
//            String errorMsg = writer.toString();
//            GotError(view, errorMsg, btnIndex);
//        }
//    }

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
        title.setTitleView("TestMiddlewareMain > TestPVR_TimeShift");

        // init help
        help.setHelpInfoText(null, null);
        help.setHelpRedText(null);
        help.setHelpGreenText(null);
        help.setHelpBlueText(null);
        help.setHelpYellowText(null);

        mPlaySurfaceView = (SurfaceView) findViewById(R.id.ID_TESTPVR_SURFACEVIEW_PLAY);
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
}
