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
import com.dolphin.dtv.EnTrickMode;
import com.dolphin.dtv.HiDtvMediaPlayer;
import com.dolphin.dtv.IDTVListener;
import com.dolphin.dtv.Resolution;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.AudioInfo;
import com.prime.dtvplayer.Sysdata.EnAudioTrackMode;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

public class TestPVRPlayActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();

    private static final String DEFAULT_RECORD_TESTFILE = Environment.getExternalStorageDirectory().getPath() + "/test.ts";

    private ActivityHelpView help;

    final int mTestTotalFuncCount = 24;    // 23 PVR_Play functions
    private int mPosition;  // position of testMidMain

    private Set<Integer> mErrorIndexSet = new HashSet<>();
    private Set<Integer> mTestedFuncSet = new HashSet<>();

    private Toast mToast;

    private SurfaceView mPlaySurfaceView;
    private EnTrickMode mEnTrickMode = EnTrickMode.FAST_FORWARD_NORMAL;
    private int mSelectAudioIndex = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_pvr_play);

        File testFile = new File(DEFAULT_RECORD_TESTFILE);
        if (!testFile.exists())
        {
            ShowToast("No TestFile, Record in PVR_Record First !");
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
        PvrPlayStop();
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

    public void PvrPlayStart_OnClick(View view)
    {
        final int btnIndex = 0; // first button
        Rect rect = new Rect();
        int location[] = new int[2];

        try
        {
            AvControlPlayStop(0);
            AvControlClose(0);

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
                TestPass(view, "PlayStart, File  = " + DEFAULT_RECORD_TESTFILE);
            }
            else
            {
                GotError(view, "Fail : PvrPlayStart return " + ret, btnIndex);
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

    public void PvrPlayStop_OnClick(View view)
    {
        final int btnIndex = 1;

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

    public void PvrPlayPause_OnClick(View view)
    {
        final int btnIndex = 2;

        try
        {
            int ret = PvrPlayPause();
            if (ret == 0)
            {
                TestPass(view, "PvrPlayPause");
            }
            else
            {
                GotError(view, "Fail : PvrPlayPause return " + ret, btnIndex);
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

    public void PvrPlayResume_OnClick(View view)
    {
        final int btnIndex = 3;

        try
        {
            int ret = PvrPlayResume();
            if (ret == 0)
            {
                TestPass(view, "PvrPlayResume");
            }
            else
            {
                GotError(view, "Fail : PvrPlayResume return " + ret, btnIndex);
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

    public void PvrPlayTrickPlay_OnClick(View view)
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

            int ret = PvrPlayTrickPlay(mEnTrickMode);
            if (ret == 0)
            {
                TestPass(view, "PvrPlayTrickPlay mode = " + mEnTrickMode);
            }
            else
            {
                GotError(view, "Fail : PvrPlayTrickPlay return " + ret+", mode="+mEnTrickMode, btnIndex);
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

    public void PvrPlaySeekTo_OnClick(View view)
    {
        final int btnIndex = 5;

        int sec = 5;
        try
        {
            int ret = PvrPlaySeekTo(sec);
            if (ret == 0)
            {
                TestPass(view, "PvrPlaySeekTo , sec = " + sec);
            }
            else
            {
                GotError(view, "Fail : PvrPlaySeekTo return " + ret, btnIndex);
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

    public void PvrPlayGetPlayTime_OnClick(View view)
    {
        final int btnIndex = 6;

        try
        {
            int ret = PvrPlayGetPlayTime();
            if (ret != -1)
            {
                TestPass(view, "PvrPlayGetPlayTime  = " + ret);
            }
            else
            {
                GotError(view, "Fail : PvrPlayGetPlayTime return -1", btnIndex);
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

    public void PvrPlayGetSize_OnClick(View view)
    {
        final int btnIndex = 7;

        try
        {
            long ret = PvrPlayGetSize();
            if (ret != -1)
            {
                TestPass(view, "PvrPlayGetSize  = " + ret);
            }
            else
            {
                GotError(view, "Fail : PvrPlayGetPlayTime return -1", btnIndex);
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

    public void PvrPlayGetDuration_OnClick(View view)
    {
        final int btnIndex = 8;

        try
        {
            int ret = PvrPlayGetDuration();
            if (ret != -1)
            {
                TestPass(view, "PvrPlayGetDuration  = " + ret);
            }
            else
            {
                GotError(view, "Fail : PvrPlayGetDuration return -1", btnIndex);
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

    public void PvrPlayIsRadio_OnClick(View view)
    {
        final int btnIndex = 9;

        try
        {
            boolean ret = PvrPlayIsRadio(DEFAULT_RECORD_TESTFILE);
            TestPass(view, "PvrPlayIsRadio  = " + ret);
        }
        catch (Exception e)
        {
            Writer writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            String errorMsg = writer.toString();
            GotError(view, errorMsg, btnIndex);
        }
    }

    public void PvrPlayGetCurrentStatus_OnClick(View view)
    {
        final int btnIndex = 10;

        try
        {
            int ret = PvrPlayGetCurrentStatus();
            if (ret != -1)
            {
                TestPass(view, "PvrPlayGetCurrentStatus  = " + ret);
            }
            else
            {
                GotError(view, "Fail : PvrPlayGetCurrentStatus return -1", btnIndex);
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

    public void PvrPlayGetCurrentTrickMode_OnClick(View view)
    {
        final int btnIndex = 11;

        try
        {
            EnTrickMode ret = PvrPlayGetCurrentTrickMode();
            if (ret != EnTrickMode.INVALID_TRICK_MODE)
            {
                TestPass(view, "PvrPlayGetCurrentTrickMode  = " + ret);
            }
            else
            {
                GotError(view, "Fail : PvrPlayGetCurrentTrickMode return EnTrickMode.INVALID_TRICK_MODE", btnIndex);
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

    public void PvrPlayGetFileFullPath_OnClick(View view)
    {
        final int btnIndex = 12;

        try
        {
            String ret = PvrPlayGetFileFullPath(0);
            if (ret != null)
            {
                TestPass(view, "PvrPlayGetFileFullPath  = " + ret);
            }
            else
            {
                GotError(view, "Fail : PvrPlayGetFileFullPath return NULL", btnIndex);
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

    public void PvrPlayGetVideoResolution_OnClick(View view)
    {
        final int btnIndex = 13;

        try
        {
            Resolution ret = PvrPlayGetVideoResolution();
            if (ret != null)
            {
                TestPass(view, "PvrPlayGetVideoResolution, Height = " + ret.height + " Width = " + ret.width);
            }
            else
            {
                GotError(view, "Fail : PvrPlayGetVideoResolution return NULL", btnIndex);
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

    public void PvrPlayGetCurrentAudio_OnClick(View view)
    {
        final int btnIndex = 14;

        try
        {
            AudioInfo.AudioComponent ret = PvrPlayGetCurrentAudio();
            if (ret != null)
            {
                TestPass(view, "PvrPlayGetCurrentAudio, Pid = " + ret.getPid() + " LangCode = " + ret.getLangCode());
            }
            else
            {
                GotError(view, "Fail : PvrPlayGetCurrentAudio return NULL", btnIndex);
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

    public void PvrPlayGetAudioComponents_OnClick(View view)
    {
        final int btnIndex = 15;

        try
        {
            AudioInfo ret = PvrPlayGetAudioComponents();
            if (ret != null)
            {
                TestPass(view, "PvrPlayGetAudioComponents, ComponentCount = " + ret.getComponentCount());
            }
            else
            {
                GotError(view, "Fail : PvrPlayGetAudioComponents return NULL", btnIndex);
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

    public void PvrPlaySelectAudio_OnClick(View view)
    {
        final int btnIndex = 16;

        int ret = -1;
        try
        {
            AudioInfo audioInfo = PvrPlayGetAudioComponents();


            if (audioInfo == null || audioInfo.getComponentCount() == 0)
            {
                GotError(view, "Fail : PvrPlayGetAudioComponents got null/empty AudioInfo", btnIndex);
            }
            else if (audioInfo.getComponentCount() == 1)
            {
                ret = PvrPlaySelectAudio(audioInfo.getComponent(0));
            }
            else
            {
                if (mSelectAudioIndex == 0)
                {
                    mSelectAudioIndex = 1;
                }
                else
                {
                    mSelectAudioIndex = 0;
                }

                ret = PvrPlaySelectAudio(audioInfo.getComponent(mSelectAudioIndex));
            }

            if (ret == 0)
            {
                TestPass(view, "PvrPlaySelectAudio, Pid = " + audioInfo.getComponent(mSelectAudioIndex).getPid() +
                        " LangCode = " + audioInfo.getComponent(mSelectAudioIndex).getLangCode());
            }
            else
            {
                GotError(view, "Fail : PvrPlaySelectAudio return " + ret, btnIndex);
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

    public void PvrPlaySetWindowRect_OnClick(View view)
    {
        final int btnIndex = 17;

        try
        {
            Rect rect = new Rect();
            int location[] = new int[2];

            mPlaySurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
            mPlaySurfaceView.getLocationOnScreen(location);
            rect.left = location[0];
            rect.top = location[1];
            rect.right = rect.left + mPlaySurfaceView.getWidth()/2;
            rect.bottom = rect.top + mPlaySurfaceView.getHeight()/2;

            int ret = PvrPlaySetWindowRect(rect);
            if (ret == 0)
            {
                TestPass(view, "PvrPlaySetWindowRect = 1/4");
            }
            else
            {
                GotError(view, "Fail : PvrPlaySetWindowRect return " + ret, btnIndex);
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

    public void PvrPlayGetTrackMode_OnClick(View view)
    {
        final int btnIndex = 18;

        try
        {
            EnAudioTrackMode ret = PvrPlayGetTrackMode();
            if (ret != EnAudioTrackMode.MPEG_AUDIO_TRACK_BUTT)
            {
                TestPass(view, "PvrPlayGetTrackMode = " + ret);
            }
            else
            {
                GotError(view, "Fail : PvrPlayGetTrackMode return EnAudioTrackMode.MPEG_AUDIO_TRACK_BUTT", btnIndex);
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

    public void PvrPlaySetTrackMode_OnClick(View view)
    {
        final int btnIndex = 19;

        try
        {
            EnAudioTrackMode audioTrackMode = PvrPlayGetTrackMode();

            if (audioTrackMode == EnAudioTrackMode.MPEG_AUDIO_TRACK_STEREO)
            {
                audioTrackMode = EnAudioTrackMode.MPEG_AUDIO_TRACK_LEFT;
            }
            else if (audioTrackMode == EnAudioTrackMode.MPEG_AUDIO_TRACK_LEFT)
            {
                audioTrackMode = EnAudioTrackMode.MPEG_AUDIO_TRACK_RIGHT;
            }
            else if (audioTrackMode == EnAudioTrackMode.MPEG_AUDIO_TRACK_RIGHT)
            {
                audioTrackMode = EnAudioTrackMode.MPEG_AUDIO_TRACK_BUTT;
            }
            else
            {
                audioTrackMode = EnAudioTrackMode.MPEG_AUDIO_TRACK_STEREO;
            }

            int ret = PvrPlaySetTrackMode(audioTrackMode);
            if (ret == 0)
            {
                TestPass(view, "PvrPlaySetTrackMode = " + audioTrackMode);
            }
            else
            {
                GotError(view, "Fail : PvrPlaySetTrackMode return " + ret, btnIndex);
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
        final int btnIndex = 20;

        try
        {
            int pvrMode = PvrGetCurrentPvrMode(0);
            if (pvrMode != -1)
            {
                TestPass(view, "PvrGetCurrentPvrMode  pvrMode=" + pvrMode);
            }
            else
            {
                GotError(view, "Fail : PvrGetCurrentPvrMode pvrMode=-1", btnIndex);
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
//        final int btnIndex = 20;
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

    public void PvrPlayFileCheckLastViewPoint_OnClick(View view)
    {
        final int btnIndex = 21;

        try
        {
            int point = PvrPlayFileCheckLastViewPoint(DEFAULT_RECORD_TESTFILE);
            if (point==1)
            {
                TestPass(view, "PvrPlayFileCheckLastViewPoint:  point=" + point);
            }
            else
            {
                GotError(view, "Fail : PvrPlayFileCheckLastViewPoint point="+point, btnIndex);
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

    public void PvrSetStartPositionFlag_OnClick(View view)
    {
        final int btnIndex = 22;

        try
        {
            int startPositionFlagf=1;
            int ret = PvrSetStartPositionFlag(startPositionFlagf);
            if (ret == 0)
            {
                TestPass(view, "PvrSetStartPositionFlag  startPositionFlag=1, ret="+ret);
            }
            else
            {
                GotError(view, "Fail : PvrSetStartPositionFlag return="+ret, btnIndex);
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

    public void PvrSetParentLockOK_OnClick(View view)
    {
        final int btnIndex = 23;

        try
        {

            int ret = PvrSetParentLockOK();
            if (ret == 0)
            {
                TestPass(view, "PvrSetParentLockOK Pass   ret ="+ret);
            }
            else
            {
                GotError(view, "Fail : PvrSetParentLockOK ret="+ret, btnIndex);
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
        title.setTitleView("TestMiddlewareMain > TestPVR_Play");

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
