package com.prime.dtvplayer.TestMiddleware;

import android.content.Context;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dolphin.dtv.AvSetRatio;
import com.dolphin.dtv.HiDtvMediaPlayer;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.AudioInfo;
import com.prime.dtvplayer.Sysdata.EnAudioTrackMode;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.SubtitleInfo;
import com.prime.dtvplayer.Sysdata.TeletextInfo;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.MessageDialogView;
import com.prime.dtvplayer.View.TeletextDialogView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.view.View.INVISIBLE;
import static com.prime.dtvplayer.TestMiddleware.TestAvControlActivity.TEST_TTX_SET.TEST_TTX_KEY123;

public class TestAvControlActivity extends DTVActivity {
    private final String TAG = getClass().getSimpleName();
    private final Context mContext = TestAvControlActivity.this;
    private ActivityHelpView help;
    private TextView txvInput;
    private TextView txvOutput;
    private Button firstButton;
    private SurfaceView surfaceView ;
    private List<ProgramManagerImpl> ProgramManagerList = null;

    final int mTestTotalFuncCount = 43;    // 43 functions
    private int mPosition;  // position of testMidMain
    private Set<Integer> mErrorIndexSet = new HashSet<>();
    private Set<Integer> mTestedFuncSet = new HashSet<>();
    private static boolean mshow=false;
    private static boolean mfreeze=false;
    private static boolean mmute=true;
    private AudioManager mAudioManager;
    private int mVolume=0;
    private int mstopType=0;
    private boolean mShowSubtitle=true;
    private boolean mSubtHiStatus=true;
    private boolean mShowTtx=true;
    private TEST_TTX_SET mTtxKey = TEST_TTX_KEY123;
    private int mRatio=0;
    private int mConversion=0;
    private int mAudioOutput=0;
    private DTVActivity mDTVActivity = null;


    public enum EnStopType
    {
        /**
                * freeze frame.<br>
                * CN:静帧。<br>
                * */
                /**
                * black screen.<br>
                * CN:黑屏。<br>
                * */

        FREEZE(0),
        BLACKSCREEN(1),
        STOP_TYPE_BUTT(8);

        private int mIndex = 0;

        EnStopType(int nIndex)
        {
            mIndex = nIndex;
        }

        public int getValue()
        {
            return mIndex;
        }

        public static EnStopType valueOf(int ordinal)
        {
            if (ordinal == FREEZE.getValue())
            {
                return FREEZE;
            }
            else if (ordinal == BLACKSCREEN.getValue())
            {
                return BLACKSCREEN;
            }
            else
            {
                return STOP_TYPE_BUTT;
            }
        }
    }

    public enum TEST_TTX_SET{
        TEST_TTX_KEY123(0),
        TEST_TTX_KEY456(1),
        TEST_TTX_KEY789(2),
        TEST_TTX_KEY100(3),
        TEST_TTX_KEY_NEXT_PAGE(4),
        TEST_TTX_KEY_PREVIOUS_PAGE(5),
        TEST_TTX_KEY_RED(6),
        TEST_TTX_KEY_GREEN(7),
        TEST_TTX_KEY_YELLOW(8),
        TEST_TTX_KEY_CYAN(9);
        /*
        TEST_TTX_KEY123(0),
        TEST_TTX_KEY456(1),
        TEST_TTX_KEY789(2),
        TEST_TTX_KEY100(3),
        TEST_TTX_KEY_PREVIOUS_PAGE(4),
        TEST_TTX_KEY_NEXT_PAGE(5),
        TEST_TTX_KEY_PREVIOUS_SUBPAGE(6),
        TEST_TTX_KEY_NEXT_SUBPAGE(7),
        TEST_TTX_KEY_PREVIOUS_MAGAZINE(8),
        TEST_TTX_KEY_NEXT_MAGAZINE(9),
        TEST_TTX_KEY_RED(10),
        TEST_TTX_KEY_GREEN(11),
        TEST_TTX_KEY_YELLOW(12),
        TEST_TTX_KEY_CYAN(13),
        TEST_TTX_KEY_INDEX(14),
        TEST_TTX_KEY_REVEAL(15),
        TEST_TTX_KEY_HOLD(16),
        TEST_TTX_KEY_MIX(17),
        TEST_TTX_KEY_UPDATE(18),
        TEST_TTX_KEY_ZOOM(19),
        TEST_TTX_KEY_SUBPAGE(20),
        TEST_TTX_KEY_BUTT(21);
        */

        private int value;
        private static TEST_TTX_SET[] vals = values();
        private TEST_TTX_SET(int value){
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public TEST_TTX_SET next()
        {
            return vals[(this.ordinal()+1) % vals.length];
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_av_control);

        Init();

        surfaceView = (SurfaceView)findViewById(R.id.TestsurfaceView) ;
        setSurfaceView(surfaceView);

        firstButton = (Button) findViewById(R.id.ID_TESTAVCONTROL_BTN_AvControlPlayByChannelId);
        firstButton.requestFocus();

        mDTVActivity = this;
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

    private void Init()
    {
        ActivityTitleView title = (ActivityTitleView) findViewById(R.id.ID_TESTAVCONTROL_LAYOUT_TITLE);
        help = (ActivityHelpView) findViewById(R.id.ID_TESTAVCONTROL_LAYOUT_HELP);
        txvInput = (TextView) findViewById(R.id.ID_TESTAVCONTROL_TXV_INPUT);
        txvOutput = (TextView) findViewById(R.id.ID_TESTAVCONTROL_TXV_OUTPUT);


        txvInput.setMovementMethod(new ScrollingMovementMethod());
        txvInput.scrollTo(0,0);
        txvOutput.setMovementMethod(new ScrollingMovementMethod());
        txvOutput.scrollTo(0,0);

        clearInputOuputContext(true,true);

        // init position
        Bundle bundle = this.getIntent().getExtras();
        if ( bundle != null )
        {
            mPosition = bundle.getInt("position", 0);
        }

        // init title
        title.setTitleView("TestMiddlewareMain > TestAvControl");

        // init help
        help.setHelpInfoText(null, null);
        help.setHelpRedText(null);
        help.setHelpGreenText(null);
        help.setHelpBlueText(null);
        help.setHelpYellowText(null);

        //Rect  rect = new Rect(150,75,260,120);
        //AvControlSetWindowSize(ViewHistory.getPlayId(), rect );


    }
    private void clearInputOuputContext(boolean input, boolean output){
        if(input == true)
            txvInput.setText("Input:");
        if(output == true)
            txvOutput.setText("Output:");
    }
    private void setAVWindowSize(){
        Rect  rect = new Rect(940,140,1660,520);
        //new Rect(920,130,1700,530);
        //Rect  rect = new Rect(980,120,1720,420);
        AvControlSetWindowSize(ViewHistory.getPlayId(), rect );
    }

    public void BtnAvControlPlayByChannelId_OnClick(View view)
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

        //AvControlOpen(ViewHistory.getPlayId()); // connie 20181017 remove for test AvControlOpen
        return_code = AvControlPlayByChannelId(ViewHistory.getPlayId(), channelId, groupType,1);
        Log.d(TAG, "BtnAvControlPlayByChannelId_OnClick: ViewHistory.getPlayId()="+ViewHistory.getPlayId());//eric lin test

        setAVWindowSize();
        clearInputOuputContext(true, false);
        txvOutput.setText("Output:\nreturn code="+return_code);

    }

    public void BtnAvControlPlayUp_OnClick(View view)
    {
        int return_code=0;
        return_code = AvControlPlayUp(ViewHistory.getPlayId());
        setAVWindowSize();
        clearInputOuputContext(true, false);
        txvOutput.setText("Output:\nreturn code="+return_code);
    }

    public void BtnAvControlPlayDown_OnClick(View view)
    {
        int return_code=0;
        return_code = AvControlPlayDown(ViewHistory.getPlayId());
        setAVWindowSize();
        clearInputOuputContext(true, false);
        txvOutput.setText("Output:\nreturn code="+return_code);
    }

    public void BtnAvControlPlayStop_OnClick(View view)
    {
        int return_code=0;
        return_code = AvControlPlayStop(ViewHistory.getPlayId());
        clearInputOuputContext(true, false);
        txvOutput.setText("Output:\nreturn code="+return_code);
    }

    // get service define (AvSetRatio) by index of string.xml
    public int GetRatioByIndex(int index)
    {
        int ratio;
        if(index == 0)
            ratio = AvSetRatio.ASPECT_RATIO_16TO9.getValue();
        else
            ratio = AvSetRatio.ASPECT_RATIO_4TO3.getValue();
        return ratio;
    }

    private void setRatioConversionInputContext(int ratio, int conversion){
//<item>16:9</item> //0
//<item>4:3</item>  //1
//        <item>Widescreen</item> //0
//        <item>Pillarbox</item>  //1
//        <item>Combined</item>   //2
        if(ratio == 0 && conversion == 0)
            txvInput.setText("Input:\n16:9 Widescreen");
        else if(ratio == 0 && conversion == 1)
            txvInput.setText("Input:\n16:9 Pillarbox");
        else if(ratio == 0 && conversion == 2)
            txvInput.setText("Input:\n16:9 Combined");
        else if(ratio == 1 && conversion == 0)
            txvInput.setText("Input:\n4:3 Widescreen");
        else if(ratio == 1 && conversion == 1)
            txvInput.setText("Input:\n4:3 Pillarbox");
        else if(ratio == 1 && conversion == 2)
            txvInput.setText("Input:\n4:3 Combined");
        else{
            txvInput.setText("Input:\n 16:9 Widescreen");
            Log.d(TAG, "setRatioConversionInputContext default: 16:9 Widescreen");
        }
    }

    public void BtnAvControlChangeRatio_OnClick(View view)
    {
        int return_code=0;
        if(mRatio == 0)
            mRatio = 1;
        else
            mRatio = 0;

        int ratio = GetRatioByIndex(mRatio);
        setRatioConversionInputContext(mRatio, mConversion);
        return_code = AvControlChangeRatioConversion(ViewHistory.getPlayId(), ratio, mConversion);
        txvOutput.setText("Output:\nreturn code="+return_code);
    }

    public void BtnAvControlChangeConversion_OnClick(View view)
    {
        int return_code=0;
        if(mConversion == 0)
            mConversion = 1;
        else if(mConversion == 1)
            mConversion = 2;
        else
            mConversion = 0;
        int ratio = GetRatioByIndex(mRatio);
        setRatioConversionInputContext(mRatio, mConversion);
        return_code = AvControlChangeRatioConversion(ViewHistory.getPlayId(), ratio, mConversion);
        txvOutput.setText("Output:\nreturn code="+return_code);
    }

    public void BtnAvControlChangeResolution_OnClick(View view)
    {
        //not support now
        clearInputOuputContext(true, false);
        txvOutput.setText("Output:\nNot Support Now!!!");
    }

    public void BtnAvControlChangeAudio_OnClick(View view)
    {
        AudioInfo AudioComp = AvControlGetAudioListInfo(ViewHistory.getPlayId());
        int curPos =0;
        int return_code=0;
        if(AudioComp.getComponentCount() > 0) {
            curPos = AudioComp.getCurPos();
            Log.d(TAG, "BtnAvControlChangeAudio_OnClick: Before curPos="+curPos);
            if((curPos+1) >= AudioComp.getComponentCount())
                curPos =0;
            else
                curPos++;
            AudioComp.setCurPos(curPos);
            Log.d(TAG, "BtnAvControlChangeAudio_OnClick: After curPos="+curPos);
            txvInput.setText("Input:\ncurPos="+curPos);

            return_code = AvControlChangeAudio(ViewHistory.getPlayId(), AudioComp.getComponent(AudioComp.getCurPos()));
            txvOutput.setText("Output:\nreturn_code="+return_code);
        }else
            txvOutput.setText("Output:\nno audio list");
    }

    public void BtnAvControlSetVolumn_OnClick(View view)
    {
        int maxVolume=100,return_code=0;


        return_code = AvControlSetVolume( mVolume);
        txvInput.setText("Input:\nVolume="+mVolume);
        txvOutput.setText("Output:\nreturn code="+return_code);

        Log.d(TAG, "mVolume="+mVolume+", AvControlGetVolume()="+AvControlGetVolume());
        if(mVolume == maxVolume)
            mVolume = 0;
        else
            mVolume++;
    }

    public void BtnAvControlSetMute_OnClick(View view)
    {
        int return_code=0;

        return_code = AvControlSetMute(ViewHistory.getPlayId(),mmute);
        if(mmute == false)
            txvInput.setText("Input:\nmute=false");
        else
            txvInput.setText("Input:\nmute=true");
        txvOutput.setText("Output:\nreturn code="+return_code);
        if(mmute==false)
            mmute = true;
        else
            mmute = false;
    }

    public void BtnAvControlSetTrackMode_OnClick(View view)
    {
        EnAudioTrackMode curTrackMode = AvControlGetTrackMode(ViewHistory.getPlayId());
        int return_code=0;

        //stero->right only->left only->stero->....
        if (curTrackMode == EnAudioTrackMode.MPEG_AUDIO_TRACK_LEFT)
        {
            curTrackMode = EnAudioTrackMode.MPEG_AUDIO_TRACK_STEREO;
            txvInput.setText("Input:\nset track mode=Stero");
        }
        else if (curTrackMode == EnAudioTrackMode.MPEG_AUDIO_TRACK_RIGHT)
        {
            curTrackMode = EnAudioTrackMode.MPEG_AUDIO_TRACK_LEFT;
            txvInput.setText("Input:\nset track mode=Left only");
        }
        else if (curTrackMode == EnAudioTrackMode.MPEG_AUDIO_TRACK_STEREO)
        {
            curTrackMode = EnAudioTrackMode.MPEG_AUDIO_TRACK_RIGHT;
            txvInput.setText("Input:\nset track mode=Right only");
        }else{
            curTrackMode = EnAudioTrackMode.MPEG_AUDIO_TRACK_STEREO;
            txvInput.setText("Input:\nset track mode=Right only");
        }

        AvControlSetTrackMode(ViewHistory.getPlayId(), curTrackMode);
        txvOutput.setText("Output:\nreturn_code="+return_code);

    }

    public void BtnAvControlAudioOutput_OnClick(View view)
    {
//        <item>@string/STR_PCM</item>
//        <item>@string/STR_BITSTREAM</item>
        int return_code=0;
        if(mAudioOutput == 0) {
            mAudioOutput = 1;
            txvInput.setText("Input:\nBitStream");
        }
        else {
            mAudioOutput = 0;
            txvInput.setText("Input:\nPCM");
        }

        return_code = AvControlAudioOutput(ViewHistory.getPlayId(), mAudioOutput);
        txvOutput.setText("Output:\nreturn code="+return_code);
    }

    public void BtnAvControlClose_OnClick(View view)
    {
        int return_code=0;
        return_code = AvControlClose(ViewHistory.getPlayId());
        clearInputOuputContext(true, false);
        txvOutput.setText("Output:\nreturn code="+return_code);
    }

    public void BtnAvControlOpen_OnClick(View view)
    {
        int return_code=0;
        return_code = AvControlOpen(ViewHistory.getPlayId());
        clearInputOuputContext(true, false);
        txvOutput.setText("Output:\nreturn code="+return_code);
    }
    //new
    public void BtnAvControlShowVideo_OnClick(View view)
    {
        int return_code=0;

        return_code = AvControlShowVideo(ViewHistory.getPlayId(),mshow);
        if(mshow == false)
            txvInput.setText("Input:\nshow=false");
        else
            txvInput.setText("Input:\nshow=true");
        txvOutput.setText("Output:\nreturn code="+return_code);
        if(mshow==false)
            mshow = true;
        else
            mshow = false;
    }

    public void BtnAvControlFreezeVideo_OnClick(View view)
    {
        int return_code=0;

        return_code = AvControlFreezeVideo(ViewHistory.getPlayId(),mfreeze);
        if(mfreeze == false)
            txvInput.setText("Input:\nfreeze=false");
        else
            txvInput.setText("Input:\nfreeze=true");
        txvOutput.setText("Output:\nreturn code="+return_code);
        if(mfreeze==false)
            mfreeze = true;
        else
            mfreeze = false;
    }

    public void BtnAvControlGetAudioListInfo_OnClick(View view)
    {
        StringBuilder zText = new StringBuilder ();
        AudioInfo AudioComp = AvControlGetAudioListInfo(ViewHistory.getPlayId());
        //EnAudioTrackMode curTrackMode = AvControlGetTrackMode(ViewHistory.getPlayId());

        // dialog will set curPos of AudioComp and pass selectTrackMode back, use these to do AvControl
        /*
        if (AudioComp != null ) {
            new AudioDialogView(
                    mContext,
                    curTrackMode,
                    AudioComp,
                    new AudioDialogView.OnAudioClickListener() {
                        public void AudioClicked(EnAudioTrackMode selectTrackMode) {
                            AvControlChangeAudio(ViewHistory.getPlayId(), AudioComp.getComponent(AudioComp.getCurPos()));
                            AvControlSetTrackMode(ViewHistory.getPlayId(), selectTrackMode);
                        }
                    }
            ).show();
        }
        */


        clearInputOuputContext(true, false);
        fillString(zText, "Output:\n");
        if(AudioComp.getComponentCount() > 0) {
            fillString(zText, "getCurPos()="+AudioComp.getCurPos()+"\n\n");
            for (int i = 0; i < AudioComp.getComponentCount(); i++) {
                fillString(zText, "i="+i+"\n");
                fillString(zText, "Pid="+AudioComp.getComponent(i).getPid() +
                ", AudioType="+AudioComp.getComponent(i).getAudioType() +
                ", AdType="+AudioComp.getComponent(i).getAdType() +
                ", TrackMode="+AudioComp.getComponent(i).getTrackMode() +
                ", LangCode="+AudioComp.getComponent(i).getLangCode() +
                ", Pos="+AudioComp.getComponent(i).getPos() +
                "\n\n");
            }
        }else
            fillString(zText, "no audio list");

        txvOutput.setText(zText.toString());


    }


    public void BtnAvControlGetPlayStatus_OnClick(View view)
    {
        int playStatus=0;
        playStatus = AvControlGetPlayStatus(ViewHistory.getPlayId());
        Log.d(TAG, "BtnAvControlGetPlayStatus_OnClick: playStatus="+playStatus);

        clearInputOuputContext(true, false);
        if(HiDtvMediaPlayer.EnPlayStatus.valueOf(playStatus) == HiDtvMediaPlayer.EnPlayStatus.STOP)
            txvOutput.setText("Output:\nPlayStatus=STOP");
        else if(HiDtvMediaPlayer.EnPlayStatus.valueOf(playStatus) == HiDtvMediaPlayer.EnPlayStatus.LIVEPLAY)
            txvOutput.setText("Output:\nPlayStatus=LIVEPLAY");
        else if(HiDtvMediaPlayer.EnPlayStatus.valueOf(playStatus) == HiDtvMediaPlayer.EnPlayStatus.TIMESHIFTPLAY)
            txvOutput.setText("Output:\nPlayStatus=TIMESHIFTPLAY");
        else if(HiDtvMediaPlayer.EnPlayStatus.valueOf(playStatus) == HiDtvMediaPlayer.EnPlayStatus.PAUSE)
            txvOutput.setText("Output:\nPlayStatus=PAUSE");
        else if(HiDtvMediaPlayer.EnPlayStatus.valueOf(playStatus) == HiDtvMediaPlayer.EnPlayStatus.IDLE)
            txvOutput.setText("Output:\nPlayStatus=IDLE");
        else if(HiDtvMediaPlayer.EnPlayStatus.valueOf(playStatus) == HiDtvMediaPlayer.EnPlayStatus.RELEASEPLAYRESOURCE)
            txvOutput.setText("Output:\nPlayStatus=RELEASEPLAYRESOURCE");
        else if(HiDtvMediaPlayer.EnPlayStatus.valueOf(playStatus) == HiDtvMediaPlayer.EnPlayStatus.PIPPLAY)
            txvOutput.setText("Output:\nPlayStatus=PIPPLAY");
        else if(HiDtvMediaPlayer.EnPlayStatus.valueOf(playStatus) == HiDtvMediaPlayer.EnPlayStatus.EWSPLAY)
            txvOutput.setText("Output:\nPlayStatus=EWSPLAY");
        else if(HiDtvMediaPlayer.EnPlayStatus.valueOf(playStatus) == HiDtvMediaPlayer.EnPlayStatus.INVALID)
            txvOutput.setText("Output:\nPlayStatus=INVALID");
        else
            txvOutput.setText("Output:\nPlayStatus: Error!!! unknown status");
    }

    public void BtnAvControlGetMute_OnClick(View view)
    {
        boolean mute=false;

        clearInputOuputContext(true, false);
        mute = AvControlGetMute(ViewHistory.getPlayId());
        if(mute == false)
            txvOutput.setText("Output:\nmute=false");
        else
            txvOutput.setText("Output:\nmute=true");
    }

    public void BtnAvControlGetTrackMode_OnClick(View view)
    {
        EnAudioTrackMode curTrackMode = AvControlGetTrackMode(ViewHistory.getPlayId());

        clearInputOuputContext(true, false);
        if(curTrackMode == EnAudioTrackMode.MPEG_AUDIO_TRACK_STEREO)
            txvOutput.setText("Output:\nTrackMode=Stereo");
        else if(curTrackMode == EnAudioTrackMode.MPEG_AUDIO_TRACK_LEFT)
            txvOutput.setText("Output:\nTrackMode=Left only");
        else if(curTrackMode == EnAudioTrackMode.MPEG_AUDIO_TRACK_RIGHT)
            txvOutput.setText("Output:\nTrackMode=Righjt only");
        else if(curTrackMode == EnAudioTrackMode.MPEG_AUDIO_TRACK_BUTT)
            txvOutput.setText("Output:\nTrackMode=butt ????");
        else
            txvOutput.setText("Output:\nError!!! unknown track mode");
    }

    public void BtnAvControlSetStopScreen_OnClick(View view)
    {
        int return_code=0;

        return_code = AvControlSetStopScreen(ViewHistory.getPlayId(),mstopType);
        if(mstopType == EnStopType.FREEZE.getValue())
            txvInput.setText("Input:\nstop type=FREEZE\nAfter set stop screen, you can first call PlayStop() and PlayUp() to test");
        else if(mstopType == EnStopType.BLACKSCREEN.getValue())
            txvInput.setText("Input:\nstop type=BLACKSCREEN\nAfter set stop screen, you can first call PlayStop() and PlayUp() to test");
        txvOutput.setText("Output:\nreturn code="+return_code);
        if(mstopType==EnStopType.FREEZE.getValue())
            mstopType = EnStopType.BLACKSCREEN.getValue();
        else if(mstopType==EnStopType.BLACKSCREEN.getValue())
            mstopType = EnStopType.FREEZE.getValue();
    }

    public void BtnAvControlGetStopScreen_OnClick(View view)
    {
        int stopType=0;

        clearInputOuputContext(true, false);
        stopType = AvControlGetStopScreen(ViewHistory.getPlayId());
        if(stopType == EnStopType.FREEZE.getValue())
            txvOutput.setText("Output:\nstop type=FREEZE");
        else if(stopType == EnStopType.BLACKSCREEN.getValue())
            txvOutput.setText("Output:\nstop type=BLACKSCREEN");
    }

    public void BtnAvControlGetFramePerSecond_OnClick(View view)
    {
        int fps=0;

        clearInputOuputContext(true, false);
        fps = AvControlGetFPS(ViewHistory.getPlayId());
        txvOutput.setText("Output:\nfps="+fps);
    }

    public void BtnAvControlEwsActionControl_OnClick(View view)
    {
        //future test, not support now?
        clearInputOuputContext(true, false);
        txvOutput.setText("Output:\nfuture to test");
    }

    public void BtnAvControlSetWindowSize_OnClick(View view)
    {
        int return_code=0;
        Rect  rect = new Rect(0,0,1920,1080);
        return_code = AvControlSetWindowSize(ViewHistory.getPlayId(), rect );
        txvInput.setText("Input:\nRect(0,0,1920,1080)");
        txvOutput.setText("Output:\nreturn code="+return_code);
    }

    public void BtnAvControlGetWindowSize_OnClick(View view)
    {
        Rect rect=null;

        clearInputOuputContext(true, false);
        rect = AvControlGetWindowSize(ViewHistory.getPlayId());
        txvOutput.setText("Output:\nrect(" + rect.left + "," + rect.top + "," + rect.right + ","+ rect.bottom + ")");
        Log.d(TAG, "rect(" + rect.left + "," + rect.top + "," + rect.right + ","+ rect.bottom + ")");
    }

    public void BtnAvControlGetVideoResolutionHeight_OnClick(View view)
    {
        int height=0;

        clearInputOuputContext(true, false);
        height = AvControlGetVideoResolutionHeight(ViewHistory.getPlayId());
        txvOutput.setText("Output:\nheight="+height);
    }

    public void BtnAvControlGetVideoResolutionWidth_OnClick(View view)
    {
        int width=0;

        clearInputOuputContext(true, false);
        width = AvControlGetVideoResolutionWidth(ViewHistory.getPlayId());
        txvOutput.setText("Output:\nwidth="+width);
    }

    public void BtnAvControlGetDolbyInfoStreamType_OnClick(View view)
    {
        int streamType=0;

        clearInputOuputContext(true, false);
        streamType = AvControlGetDolbyInfoStreamType(ViewHistory.getPlayId());
        txvOutput.setText("Output:\nstream type="+streamType);

    }

    public void BtnAvControlGetDolbyInfoAcmod_OnClick(View view)
    {
        int Acmod=0;

        clearInputOuputContext(true, false);
        Acmod = AvControlGetDolbyInfoAcmod(ViewHistory.getPlayId());
        txvOutput.setText("Output:\nAcmod="+Acmod);

    }

    public void BtnAvControlGetCurrentSubtitle_OnClick(View view)
    {
        SubtitleInfo.SubtitleComponent curSubttile;
        StringBuilder zText = new StringBuilder ();

        curSubttile = AvControlGetCurrentSubtitle(ViewHistory.getPlayId());

        clearInputOuputContext(true, false);
        fillString(zText, "Output:\n");
        if(curSubttile != null) {
            fillString(zText, "Pid=" + curSubttile.getPid() +
                    ", Type=" + curSubttile.getType() +
                    ", MagazingNum=" + curSubttile.getMagazingNum() +
                    ", PageNum=" + curSubttile.getPageNum() +
                    ", LangCode=" + curSubttile.getLangCode() +
                    ", Pos=" + curSubttile.getPos() +
                    "\n\n");
        }else
            fillString(zText, "no current subttile");
        txvOutput.setText(zText.toString());
    }


    public void BtnAvControlGetSubtitleList_OnClick(View view)
    {
        SubtitleInfo SubtitleComp = AvControlGetSubtitleList(ViewHistory.getPlayId());
        StringBuilder zText = new StringBuilder ();

        clearInputOuputContext(true, false);
        fillString(zText, "Output:\n");
        if(SubtitleComp != null && SubtitleComp.getComponentCount() > 0) { // connie 20181015 fix crash when no subtitle
            fillString(zText, "getCurPos()="+SubtitleComp.getCurPos()+"\n\n");
            for (int i = 0; i < SubtitleComp.getComponentCount(); i++) {
                fillString(zText, "i="+i+"\n");
                fillString(zText, "Pid="+SubtitleComp.getComponent(i).getPid() +
                        ", Type="+SubtitleComp.getComponent(i).getType() +
                        ", MagazingNum="+SubtitleComp.getComponent(i).getMagazingNum() +
                        ", PageNum="+SubtitleComp.getComponent(i).getPageNum() +
                        ", LangCode="+SubtitleComp.getComponent(i).getLangCode() +
                        ", Pos="+SubtitleComp.getComponent(i).getPos() +
                        "\n\n");
            }
        }else
            fillString(zText, "no subttile list");
        txvOutput.setText(zText.toString());
    }

    public void BtnAvControlSelectSubtitle_OnClick(View view)
    {
        int return_code=0;
        SubtitleInfo SubtileComp = AvControlGetSubtitleList(ViewHistory.getPlayId());
        int curPos =0;
        if(SubtileComp != null && SubtileComp.getComponentCount() > 0) {  // connie 20181015 fix crash when no subtitle
            curPos = SubtileComp.getCurPos();
            Log.d(TAG, "BtnAvControlSelectSubtitle_OnClick: Before curPos="+curPos);
            if((curPos+1) >= SubtileComp.getComponentCount())
                curPos =0;
            else
                curPos++;
            SubtileComp.setCurPos(curPos);
            txvInput.setText("Input:\ncurPos="+curPos);
            Log.d(TAG, "BtnAvControlSelectSubtitle_OnClick: After curPos="+curPos);

            return_code = AvControlSelectSubtitle(ViewHistory.getPlayId(), SubtileComp.getComponent(SubtileComp.getCurPos()));
            txvOutput.setText("Output:\nreturn code="+return_code);

        }else
            txvOutput.setText("Output:\nno subtitle list");
    }

    public void BtnAvControlShowSubtitle_OnClick(View view)
    {
        int return_code=0;

        return_code = AvControlShowSubtitle(ViewHistory.getPlayId(), mShowSubtitle);
        if(mShowSubtitle == false) {
            txvInput.setText("Input:\nshow subtitle=false");
        }
        else {
            txvInput.setText("Input:\nshow subtitle=true");
            //eric lin test
            //SubtitleInfo SubtileComp = AvControlGetSubtitleList(ViewHistory.getPlayId());
            //SubtileComp.setCurPos(1);
            //AvControlSelectSubtitle(ViewHistory.getPlayId(), SubtileComp.getComponent(SubtileComp.getCurPos()));
            //eric lin test
        }
        txvOutput.setText("Output:\nreturn code="+return_code);
        if(mShowSubtitle==false)
            mShowSubtitle = true;
        else
            mShowSubtitle = false;
    }

    public void BtnAvControlIsSubtitleVisible_OnClick(View view)
    {
        boolean return_code=false;

        return_code = AvControlIsSubtitleVisible(ViewHistory.getPlayId());
        txvOutput.setText("Output:\nis subtile visible="+return_code);
    }

    public void BtnAvControlSetSubtHiStatus_OnClick(View view)
    {
        int return_code=0;

        return_code = AvControlSetSubtHiStatus(ViewHistory.getPlayId(), mSubtHiStatus);
        if(mSubtHiStatus == false)
            txvInput.setText("Input:\nsubtitle  hi status=false");
        else
            txvInput.setText("Input:\nsubtitle  hi status=true");
        txvOutput.setText("Output:\nreturn code="+return_code);
        if(mSubtHiStatus==false)
            mSubtHiStatus = true;
        else
            mSubtHiStatus = false;
    }

    public void BtnAvControlGetCurrentTeletext_OnClick(View view)
    {
        TeletextInfo ttx=null;
        StringBuilder zText = new StringBuilder ();

        clearInputOuputContext(true, false);
        ttx = AvControlGetCurrentTeletext(ViewHistory.getPlayId());
        if(ttx != null){
            fillString(zText, "MagazingNum="+ttx.getMagazingNum() +
                    ", PageNum="+ttx.getPageNum() +
                    ", LangCode="+ttx.getLangCode() +
                    "\n\n");
        }else
            fillString(zText, "no teletext");
        txvOutput.setText(zText.toString());
    }

    /*public void BtnAvControlGetTeletextList_OnClick(View view)
    {
        List<com.prime.dtvplayer.Sysdata.TeletextInfo> ttxList = AvControlGetTeletextList(ViewHistory.getPlayId());
        StringBuilder zText = new StringBuilder ();

        clearInputOuputContext(true, false);
        fillString(zText, "Output:\n");
        if(ttxList != null) {
            for (int i = 0; i < ttxList.size(); i++) {
                fillString(zText, "i="+i+"\n");
                fillString(zText, "MagazingNum="+ttxList.get(i).getMagazingNum() +
                        ", PageNum="+ttxList.get(i).getPageNum() +
                        ", LangCode="+ttxList.get(i).getLangCode() +
                        "\n\n");
            }
        }else
            fillString(zText, "no teletext");
        txvOutput.setText(zText.toString());
    }*/

    public void BtnAvControlShowTeletext_OnClick(View view)
    {
//        int return_code=0;
//
//        return_code = AvControlShowTeletext(ViewHistory.getPlayId(), mShowTtx);
//        if(mShowTtx == false) {
//            txvInput.setText("Input:\nshow teletext=false");
//        }
//        else {
//            txvInput.setText("Input:\nshow teletext=true");
//        }
//        txvOutput.setText("Output:\nreturn code="+return_code);
//        if(mShowTtx==false)
//            mShowTtx = true;
//        else
//            mShowTtx = false;
        TestshowTeletextDialog();
    }

    public void BtnAvControlIsTeletextVisible_OnClick(View view)
    {
        boolean return_code=false;

        return_code = AvControlIsTeletextVisible(ViewHistory.getPlayId());
        txvOutput.setText("Output:\nis teletext visible="+return_code);
    }

    public void BtnAvControlIsTeletextAvailable_OnClick(View view)
    {
        boolean return_code=false;

        return_code = AvControlIsTeletextAvailable(ViewHistory.getPlayId());
        txvOutput.setText("Output:\nis teletext available="+return_code);
    }

    public void BtnAvControlSetTeletextLanguage_OnClick(View view)
    {
        TeletextInfo curTtx=null;
        StringBuilder zText = new StringBuilder ();
        int return_code=0;
        String ttxLang = "swe";
        clearInputOuputContext(true, false);
        curTtx = AvControlGetCurrentTeletext(ViewHistory.getPlayId());
        fillString(zText, "Output:\n");
        if(curTtx != null){
            return_code = AvControlSetTeletextLanguage(ViewHistory.getPlayId(), ttxLang); //curTtx.getLangCode());
            txvInput.setText("Input:\nLang code="+ttxLang); //curTtx.getLangCode());
            fillString(zText, "return code="+return_code);
        }else
            fillString(zText, "no teletext");
        txvOutput.setText(zText.toString());
    }

    public void BtnAvControlGetTeletextLanguage_OnClick(View view)
    {
        TeletextInfo curTtx=null;
        StringBuilder zText = new StringBuilder ();
        int return_code=0;

        clearInputOuputContext(true, true);
        curTtx = AvControlGetCurrentTeletext(ViewHistory.getPlayId());
        //fillString(zText, "Output:\n");
        if(curTtx != null){
            String ttxLang = AvControlGetTeletextLanguage(ViewHistory.getPlayId());
            txvOutput.setText("Output:\nLang code="+ttxLang);
        }else {
            fillString(zText, "no teletext");
            txvOutput.setText(zText.toString());
        }
    }

    public void BtnAvControlSetTeletextCommand_OnClick(View view)
    {
        int return_code = 0;
        switch(mTtxKey) {
            case TEST_TTX_KEY123:
                return_code = AvControlSetTeletextCommand(ViewHistory.getPlayId(), TeletextDialogView.TTX.TTX_KEY_1.getValue());
                return_code |= AvControlSetTeletextCommand(ViewHistory.getPlayId(), TeletextDialogView.TTX.TTX_KEY_2.getValue());
                return_code |= AvControlSetTeletextCommand(ViewHistory.getPlayId(), TeletextDialogView.TTX.TTX_KEY_3.getValue());
                txvInput.setText("Input:\nset 123");
                break;
            case TEST_TTX_KEY456:
                return_code = AvControlSetTeletextCommand(ViewHistory.getPlayId(), TeletextDialogView.TTX.TTX_KEY_4.getValue());
                return_code |= AvControlSetTeletextCommand(ViewHistory.getPlayId(), TeletextDialogView.TTX.TTX_KEY_5.getValue());
                return_code |= AvControlSetTeletextCommand(ViewHistory.getPlayId(), TeletextDialogView.TTX.TTX_KEY_6.getValue());
                txvInput.setText("Input:\nset 456");
                break;
            case TEST_TTX_KEY789:
                return_code = AvControlSetTeletextCommand(ViewHistory.getPlayId(), TeletextDialogView.TTX.TTX_KEY_7.getValue());
                return_code |= AvControlSetTeletextCommand(ViewHistory.getPlayId(), TeletextDialogView.TTX.TTX_KEY_8.getValue());
                return_code |= AvControlSetTeletextCommand(ViewHistory.getPlayId(), TeletextDialogView.TTX.TTX_KEY_9.getValue());
                txvInput.setText("Input:\nset 789");
                break;
            case TEST_TTX_KEY100:
                return_code = AvControlSetTeletextCommand(ViewHistory.getPlayId(), TeletextDialogView.TTX.TTX_KEY_1.getValue());
                return_code |= AvControlSetTeletextCommand(ViewHistory.getPlayId(), TeletextDialogView.TTX.TTX_KEY_0.getValue());
                return_code |= AvControlSetTeletextCommand(ViewHistory.getPlayId(), TeletextDialogView.TTX.TTX_KEY_0.getValue());
                txvInput.setText("Input:\nset 100");
                break;
            case TEST_TTX_KEY_PREVIOUS_PAGE:
                return_code = AvControlSetTeletextCommand(ViewHistory.getPlayId(), TeletextDialogView.TTX.TTX_KEY_PREVIOUS_PAGE.getValue());
                txvInput.setText("Input:\nset previous page");
                break;
            case TEST_TTX_KEY_NEXT_PAGE:
                return_code = AvControlSetTeletextCommand(ViewHistory.getPlayId(), TeletextDialogView.TTX.TTX_KEY_NEXT_PAGE.getValue());
                txvInput.setText("Input:\nset next page");
                break;
            case TEST_TTX_KEY_RED:
                return_code = AvControlSetTeletextCommand(ViewHistory.getPlayId(), TeletextDialogView.TTX.TTX_KEY_RED.getValue());
                txvInput.setText("Input:\nset red");
                break;
            case TEST_TTX_KEY_GREEN:
                return_code = AvControlSetTeletextCommand(ViewHistory.getPlayId(), TeletextDialogView.TTX.TTX_KEY_GREEN.getValue());
                txvInput.setText("Input:\nset green");
                break;
            case TEST_TTX_KEY_YELLOW:
                return_code = AvControlSetTeletextCommand(ViewHistory.getPlayId(), TeletextDialogView.TTX.TTX_KEY_YELLOW.getValue());
                txvInput.setText("Input:\nset yellow");
                break;
            case TEST_TTX_KEY_CYAN:
                return_code = AvControlSetTeletextCommand(ViewHistory.getPlayId(), TeletextDialogView.TTX.TTX_KEY_CYAN.getValue());
                txvInput.setText("Input:\nset cyan");
                break;
            default:
                return_code = AvControlSetTeletextCommand(ViewHistory.getPlayId(), TeletextDialogView.TTX.TTX_KEY_NEXT_PAGE.getValue());
                txvInput.setText("Input:\nset default next page");
                break;
        }
        mTtxKey = mTtxKey.next();
        txvOutput.setText("Output:\nreturn code="+return_code);
    }

    public void BtnAvControlGetRatio_OnClick(View view)
    {
        int ratio =0;

        clearInputOuputContext(true, true);
        ratio = AvControlGetRatio(ViewHistory.getPlayId());
        if(ratio != -1){
            if(ratio == 1)
                txvOutput.setText("Output:\nAvControlGetRatio   ratio="+ ratio + "     4:3");
            else if( ratio == 2)
                txvOutput.setText("Output:\nAvControlGetRatio   ratio="+ ratio + "     16:9");
            else
                txvOutput.setText("Output:\nAvControlGetRatio   ratio"+ ratio);
        }
        else {
            txvOutput.setText("AvControlGetRatio Get Fail    ratio = " + ratio);
        }
    }

    public void BtnAvControlSetSubtitleLanguage_OnClick(View view)
    {
        int ret =0;

        clearInputOuputContext(true, true);
        String fistLang = "eng";
        String SecLang = "swe";
        ret = AvControlSetSubtitleLanguage(ViewHistory.getPlayId(), 0 , fistLang);
        if(ret == 0) {
            ret = AvControlSetSubtitleLanguage(ViewHistory.getPlayId(), 1, SecLang);
            if(ret == 0)
                txvOutput.setText("Output:\nAvControlSetSubtitleLanguage   index=0    lang = " + fistLang + "     index = 1   lang =" + SecLang);
        }
        if(ret != 0)
            txvOutput.setText("Output:\nAvControlGetRatio  Fail");
    }

    public void TestshowTeletextDialog()
    {
        final TeletextInfo TeletextInfo = AvControlGetCurrentTeletext(ViewHistory.getPlayId());
        final List<TeletextInfo> TeletextInfoList = AvControlGetTeletextList(ViewHistory.getPlayId());



        if (TeletextInfo != null)
        {
            new TestTeletextDialogView(this,mDTVActivity,ViewHistory.getPlayId());
        }
        else
        {
            new MessageDialogView(mContext,getString(R.string.STR_TELETEXT_IS_NOT_AVAILABLE),3000)
            {
                public void dialogEnd() {
                }
            }.show();
        }
    }

    void fillString(StringBuilder zText) { zText.append ("foo"); }
    void fillString(StringBuilder zText, String str){
        zText.append (str);
    }
}


