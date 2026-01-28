package com.prime.dtvplayer.TestMiddleware;

import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestPVRRecordActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();

    private static final String DEFAULT_RECORD_TESTFILE = Environment.getExternalStorageDirectory().getPath() + "/test.ts";

    private ActivityHelpView help;

    final int mTestTotalFuncCount = 10;    // 10 PVR Record functions
    private int mPosition;  // position of testMidMain

    private Set<Integer> mErrorIndexSet = new HashSet<>();
    private Set<Integer> mTestedFuncSet = new HashSet<>();

    private Toast mToast;

    private SurfaceView mPlaySurfaceView;
    private List<SimpleChannel> mSimpleChannelList;
    private int mRecId;
    private long mChannelId=0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_pvr_record);
        mSimpleChannelList = ProgramInfoGetSimpleChannelList(ProgramInfo.ALL_TV_TYPE, 1, 1);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        if (mSimpleChannelList == null || mSimpleChannelList.isEmpty())
        {
            ShowToast("No Program, Scan First !");
            finish();
        }

        registCallback ( DTVMessage.HI_SVR_EVT_PVR_CALLBACK_START, DTVMessage.HI_SVR_EVT_PVR_CALLBACK_END, gPVRListener ) ;
        Init();

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
        PvrRecordStop(0, mRecId);
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
                    int recId;
                    recId = param1;                    
                    ShowToast("HI_SVR_EVT_PVR_REC_OVER_FIX recId="+recId);
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
                case DTVMessage.HI_SVR_EVT_PVR_OPEN_HD_FINISH: {
                    Log.d(TAG, "HI_SVR_EVT_PVR_OPEN_HD_FINISH") ;
                    Log.d(TAG, "notifyMessage:  param1 = " + param1 + "    param2 =" + parm2);
                    if(param1 != -1)
                        ShowToast("HI_SVR_EVT_PVR_OPEN_HD_FINISH Success !!!!");
                    else
                        ShowToast("HI_SVR_EVT_PVR_OPEN_HD_FINISH Fail !!!!");
                }
                default:
                    break;
            }
        }
    };

    public void PvrRecordStart_OnClick(View view)
    {
        final int btnIndex = 0; // it is first button

        final int duration = 60; // 20180508 min 60s
        try
        {
            AvControlOpen(0);
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

            int recId = PvrRecordStart(0, mSimpleChannelList.get(0).getChannelId(), DEFAULT_RECORD_TESTFILE, duration);
            mRecId = recId;            
            if (recId != -1)
            {
                TestPass(view, "Rec Start for " + duration +" Secs" + ", recId="+recId);
            }
            else
            {
                GotError(view, "Fail : PvrRecordStart recId=" + recId, btnIndex);
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

    public void PvrRecordStop_OnClick(View view)
    {
        final int btnIndex = 1;

        try
        {
            int ret = PvrRecordStop(0, mRecId);
            AvControlPlayStop(0);
            AvControlClose(0);

            if (ret == 0)
            {
                TestPass(view, "Rec stop, recId="+mRecId);
            }
            else
            {
                GotError(view, "Fail : PvrRecordStop return " + ret+", recId="+mRecId, btnIndex);
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

    public void PvrRecordGetAlreadyRecTime_OnClick(View view)
    {
        final int btnIndex = 2;

        try
        {
            int recTime = PvrRecordGetAlreadyRecTime(0, mRecId);
            if (recTime != -1)
            {
                TestPass(view, "Recorded " + recTime + " Secs" + ", recId="+mRecId);
            }
            else
            {
                GotError(view, "Fail : PvrRecordGetAlreadyRecTime return -1, recId=%d"+mRecId, btnIndex);
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

    public void PvrRecordGetStatus_OnClick(View view)
    {
        final int btnIndex = 3;

        try
        {
            int recStatus = PvrRecordGetStatus(0, mRecId);
            if (recStatus != -1)
            {
                TestPass(view, "RecStatus  = " + recStatus + ", recId="+mRecId);
            }
            else
            {
                GotError(view, String.format("Fail : PvrRecordGetStatus return -1, recId=%d", mRecId), btnIndex);
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

    public void PvrRecordGetFileFullPath_OnClick(View view)
    {
        final int btnIndex = 4;

        try
        {
            String fileFullPath = PvrRecordGetFileFullPath(0, mRecId);
            if (fileFullPath != null)
            {
                TestPass(view, "Rec File FullPath  = " + fileFullPath+ ", recId="+mRecId);
            }
            else
            {
                GotError(view, String.format("Fail : PvrRecordGetFileFullPath return NULL, recId=%d", mRecId), btnIndex);
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

    public void PvrRecordGetProgramId_OnClick(View view)
    {
        final int btnIndex = 5;

        try
        {
            int progID = PvrRecordGetProgramId(0, mRecId);
            if (progID != -1)
            {
                TestPass(view, "Rec ProgramID  = " + progID+ ", recId="+mRecId);
            }
            else
            {
                GotError(view, String.format("Fail : PvrRecordGetProgramId return -1 recId=%d", mRecId), btnIndex);
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
        final int btnIndex = 6;

        try
        {
            int pvrMode = PvrGetCurrentPvrMode(mChannelId);
            if (pvrMode != -1)
            {
                TestPass(view, "PvrGetCurrentPvrMode  pvrMode=" + pvrMode);
            }
            else
            {
                GotError(view, "Fail : PvrGetCurrentPvrMode return -1", btnIndex);
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

    public void PvrRecordCheck_OnClick(View view)
    {
        final int btnIndex = 7;

        try
        {
            int recId = PvrRecordCheck(mChannelId);
            if (recId != -1)
            {
                TestPass(view, "PvrRecordCheck: recId=" + recId);
            }
            else
            {
                GotError(view, "Fail : PvrRecordCheck recId="+recId, btnIndex);
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



    public void PvrRecordGetAllInfo_OnClick(View view)
    {
        final int btnIndex = 8; // it is first button

        try
        {
            List<PvrInfo> pvrList= new ArrayList<PvrInfo> ();
            pvrList = PvrRecordGetAllInfo();

            if (pvrList != null)
                Log.d(TAG, "PvrRecordGetAllInfo_OnClick: pvrList != null, size="+pvrList.size());

            if (pvrList != null)
            {
                if(pvrList.size()==0)
                    TestPass(view, "PvrRecordGetAllInfo: pvrList.size()==0");
                if(pvrList.size()==1)
                    TestPass(view, "PvrRecordGetAllInfo: pvrList.size()==1" +
                        " (recId="+pvrList.get(0).getRecId()+", chId="+pvrList.get(0).getChannelId()+", pvrMode="+pvrList.get(0).getPvrMode()+")");
                else if(pvrList.size()==2)
                    TestPass(view, "PvrRecordGetAllInfo: pvrList.size()==2" +
                            " (recId="+pvrList.get(0).getRecId()+", chId="+pvrList.get(0).getChannelId()+", pvrMode="+pvrList.get(0).getPvrMode()+")"
                            +", (recId="+pvrList.get(1).getRecId()+", chId="+pvrList.get(1).getChannelId()+", pvrMode="+pvrList.get(1).getPvrMode()+")");
                else if(pvrList.size()==3)
                    TestPass(view, "PvrRecordGetAllInfo: pvrList.size()==3" +
                            " (recId="+pvrList.get(0).getRecId()+", chId="+pvrList.get(0).getChannelId()+", pvrMode="+pvrList.get(0).getPvrMode()+")"
                            +", (recId="+pvrList.get(1).getRecId()+", chId="+pvrList.get(1).getChannelId()+", pvrMode="+pvrList.get(1).getPvrMode()+")"
                            +", (recId="+pvrList.get(2).getRecId()+", chId="+pvrList.get(2).getChannelId()+", pvrMode="+pvrList.get(2).getPvrMode()+")");
            }
            else
            {
                GotError(view, "Fail : PvrRecordStart pvrList==null", btnIndex);
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

    public void PvrRecordGetMaxRecNum_OnClick(View view)
    {
        final int btnIndex = 9; // it is first button

        try
        {
            int maxRecNum = PvrRecordGetMaxRecNum();

            if (true)
            {
                TestPass(view, "PvrRecordGetMaxRecNum: maxRecNum" +maxRecNum);
            }
            else
            {
                GotError(view, "Fail : PvrRecordStart pvrList==null or pvrList.size()!=2", btnIndex);
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

    static int testFileIndex = 0;
    public void PvrRecordForRecordFile_OnClick(View view)
    {
        final int btnIndex = 10; // it is first button
        final int duration = 60; // 20180508 min 60s
        String DateStr = String.format("%02d", testFileIndex);
        String FilePath = Environment.getExternalStorageDirectory().getPath() + "/TestFile_" + Integer.toString(testFileIndex)+ "_2018-Apr-" + DateStr+"-10-00"+".ts";
        //String FilePath = "/storage/sda1/Records/"+ "TestFile_" + Integer.toString(testFileIndex)+".ts";

        testFileIndex++;
        try
        {
            AvControlOpen(0);
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

            int recId = PvrRecordStart(0, mSimpleChannelList.get(0).getChannelId(), FilePath, duration);
            Log.d(TAG, "PvrRecordForRecordFile_OnClick:  recId = " + recId + "     FilePath = " + FilePath);
            mRecId = recId;
            if (recId != -1)
            {
                TestPass(view, "Rec FilePath " + FilePath + "       recId =" +recId);
            }
            else
            {
                GotError(view, "Fail : PvrRecordForRecordFile ="+FilePath+ "   recId=" + recId, btnIndex);
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

    public void pvrCheckHardDiskOpen_OnClick(View view)
    {
        final int btnIndex = 11; // it is first button

        try
        {
            pvrCheckHardDiskOpen("/storage/sda1/Records/");
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }

    public void GetRecordPath_OnClick(View view)
    {
        final int btnIndex = 12;

        try
        {
            String path = GetRecordPath();
            if (path != null)
            {
                TestPass(view, "GetRecordPath : Path = " + path);
            }
            else
            {
                GotError(view, "Fail : GetRecordPath    Path ="+ path , btnIndex);
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

    public void SetRecordPath_OnClick(View view)
    {
        final int btnIndex = 13;

        String Path = Environment.getExternalStorageDirectory().getPath();
        String NewPath = "" ;
        try
        {
            SetRecordPath(Path);

            NewPath = GetRecordPath();
            if (NewPath.equals(Path))
            {
                TestPass(view, "SetRecordPath : New Path = " + NewPath);
            }
            else
            {
                GotError(view, "Fail : SetRecordPath    Path ="+ NewPath , btnIndex);
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
        title.setTitleView("TestMiddlewareMain > TestPVR_Record");

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
