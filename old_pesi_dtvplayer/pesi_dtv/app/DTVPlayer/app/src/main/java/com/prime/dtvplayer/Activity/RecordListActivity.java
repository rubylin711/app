package com.prime.dtvplayer.Activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.dolphin.dtv.EnPVRPlayStatus;
import com.dolphin.dtv.EnTableType;
import com.dolphin.dtv.EnTrickMode;
import com.dolphin.dtv.PvrFileInfo;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.SureDialog;
import com.prime.dtvplayer.Sysdata.AudioInfo;
import com.prime.dtvplayer.Sysdata.EnAudioTrackMode;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.SubtitleInfo;
import com.prime.dtvplayer.Sysdata.TeletextInfo;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.AudioDialogView;
import com.prime.dtvplayer.View.MessageDialogView;
import com.prime.dtvplayer.View.PasswordDialogView;
import com.prime.dtvplayer.View.PvrBannerView;
import com.prime.dtvplayer.View.PvrRecyclerView;
import com.prime.dtvplayer.View.SubtitleDialogView;
import com.prime.dtvplayer.View.TeletextDialogView;
import com.prime.dtvplayer.utils.TVMessage;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_CAPTIONS;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;
import static android.view.KeyEvent.KEYCODE_INFO;
import static android.view.KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK;
import static android.view.KeyEvent.KEYCODE_MEDIA_FAST_FORWARD;
import static android.view.KeyEvent.KEYCODE_MEDIA_NEXT;
import static android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
import static android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS;
import static android.view.KeyEvent.KEYCODE_MEDIA_REWIND;
import static android.view.KeyEvent.KEYCODE_MEDIA_STOP;
import static android.view.KeyEvent.KEYCODE_PAGE_DOWN;
import static android.view.KeyEvent.KEYCODE_PAGE_UP;
import static android.view.KeyEvent.KEYCODE_PROG_RED;
import static android.view.KeyEvent.KEYCODE_TV_TELETEXT;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.dolphin.dtv.EnPVRPlayStatus.FAST_BACKWARD;
import static com.dolphin.dtv.EnPVRPlayStatus.FAST_FORWARD;
import static com.dolphin.dtv.EnPVRPlayStatus.INIT;
import static com.dolphin.dtv.EnPVRPlayStatus.PAUSE;
import static com.dolphin.dtv.EnPVRPlayStatus.PLAY;
import static com.dolphin.dtv.EnTrickMode.FAST_BACKWARD_EIGHT;
import static com.dolphin.dtv.EnTrickMode.FAST_BACKWARD_FOUR;
import static com.dolphin.dtv.EnTrickMode.FAST_BACKWARD_SIXTEEN;
import static com.dolphin.dtv.EnTrickMode.FAST_BACKWARD_THIRTYTWO;
import static com.dolphin.dtv.EnTrickMode.FAST_BACKWARD_TWO;
import static com.dolphin.dtv.EnTrickMode.FAST_FORWARD_EIGHT;
import static com.dolphin.dtv.EnTrickMode.FAST_FORWARD_FOUR;
import static com.dolphin.dtv.EnTrickMode.FAST_FORWARD_NORMAL;
import static com.dolphin.dtv.EnTrickMode.FAST_FORWARD_SIXTEEN;
import static com.dolphin.dtv.EnTrickMode.FAST_FORWARD_THIRTYTWO;
import static com.dolphin.dtv.EnTrickMode.FAST_FORWARD_TWO;

public class RecordListActivity extends DTVActivity {

    private static final String TAG = "RecordListActivity";
    private final Context mContext = RecordListActivity.this;//eric lin 20180720 add file play audio dialog
    private ActivityTitleView title;
    private ActivityHelpView help;
    private PvrRecyclerView rvList;
    private TextView fileSize;
    private TextView recDate,recTitleStr;
    private TextView time;
    private TextView progName;
    private TextView pvrSpeed;
    private TextView emptyFile;
    private TextView fileList;
    private TextView fileSizeTag;
    private TextView timeTag;
    private TextView parentalRate;
    private TextView parentalTitle;
    //private TextView freeSizeTag;
    //private TextView totalSizeTag;
    //private TextView hdFreeSize;
    //private TextView hdTotalSize;
    private ImageView pvrPreFile;
    private ImageView pvrRewind;
    private ImageView pvrPlay;
    private ImageView pvrPause;
    private ImageView pvrStop;
    private ImageView pvrForward;
    private ImageView pvrNextFile;
    private ConstraintLayout smallScreen;
    private ConstraintLayout fullLayout;
    private ConstraintLayout panel;
    private LinearLayout rec_list_layout;//eric lin 20180720 add file play audio dialog
    private ProgressBar progressBar;
    private SurfaceView surfaceView;
    private PvrBannerView progressBanner;
    private PasswordDialogView passwordDia=null;

    // detail info
    private ConstraintLayout detailLayout;
    ScrollView scrollView;
    private TextView txvEpgTime;
    private TextView txvEpgName;
    private TextView txvEpgDetail;
    private TextView txvEpgNotFound;

    private boolean finishInit = false; // init RecyclerView

    private static final boolean FULL_SCREEN = true;
    private static final boolean NORMAL_SCREEN = false;
    private static final int PLAY_NEXT_FILE = 0;
    private static final int PLAY_PREV_FILE = 1;
    private static final int LOADING_FINISH = 0;
    private static final int LOADING_FAIL = -1;
    private static final int EPG_PRESENT = 1;

    private List<PvrFileInfo> mFileList;
    private String playingFile;
    private int playingFileDuration = 0;    // Johnny 20180802 fix calling pvrFileGetDuration too often

    //eric lin 20180725 records list play and lock symbol,-start
    //below two flags control play symbol showing
    private int mPrePlayPos=-1, mCurPlayPos=-1;
    //below two flags control pincode dialog showing
    private int mCurPosStatus=0;//focus item status, 0 means can direct play, 1 means need ask pincode
    private int mPlayPosStatus=0;//play item status, 0 means can direct play, 1 means need ask pincode
    //eric lin 20180725 records list play and lock symbol,-end
    private AudioDialogView mAudioDialogView=null;//eric lin 20180801 fix after playback finish,audio dialog not auto closed
    private DTVActivity mDtv = null;

    // ======== Hide Banner after a few seconds ========
    private int bannerTimeout = 0;
    private Runnable hideBanner = new Runnable() {
        @Override
        public void run() {
            progressBanner.setVisibility(INVISIBLE);
        }
    };

    // ======== Increase Progress when playing video ========
    private Handler hdrUpdateProgress;
    private Runnable updateProgress = new Runnable() {
        @Override
        public void run() {

            Log.d(TAG, "run: updateProgress");

            // Video Stop
            if (PvrPlayGetCurrentStatus() == EnPVRPlayStatus.INIT)
            {
                ResetProgress();
                return;
            }

            // Update Time
            int playTime = PvrPlayGetPlayTime();

            progressBanner.updateTimeStatus(RecordListActivity.this, playingFileDuration,false);//Scoty 20180802 fixed cur time not update when focus on Pip   // Johnny 20180802 fix calling pvrFileGetDuration too often
            progressBar.setProgress(playTime, true);

            if (playTime <= progressBar.getMax())
            {
                progressBar.postDelayed(updateProgress, 1000);
            }
        }
    };
    private Thread loadFile = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.pvr_rec_list);

        Log.d(TAG, "onCreate: ");
        
        CheckRecordPath(GetRecordPath());

        InitViewById();
        InitRvList();
        setControlPanelClickListener(); // Johnny 20181228 for mouse control

        AvControlPlayStop(ViewHistory.getPlayId());

        setSurfaceView(surfaceView);
        mDtv = this;
    }

    @Override
    public void onWindowFocusChanged ( boolean hasFocus )
    {
        super.onWindowFocusChanged( hasFocus );

        if ( ! hasFocus )
        {
            return;
        }
        SetListNum(rvList, fileList, fileSizeTag);
    }

    @Override
    public void onMessage(TVMessage tvMessage)
    {
        super.onMessage(tvMessage);

        int msg = tvMessage.getMsgType();
        Log.d(TAG, "onMessage:  msg = " + msg);

        switch (msg) {

            case TVMessage.TYPE_PVR_PLAY_TO_BEGIN:
            {
                Log.d(TAG, "onMessage: replay");

                PvrPlayResume();
                SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());

                if (IsFullScreen())
                {
                    ShowBanner();
                }
                break;
            }

            case TVMessage.TYPE_PVR_PLAY_EOF:
            {
                Log.d(TAG, "onMessage: stop video");

                PvrPlayStop();
                SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());
                InitHelp(R.string.STR_OK_TO_PLAY_RECORD);

                if (IsFullScreen())
                {
                    SetScreen(NORMAL_SCREEN);
                    progressBanner.setVisibility(INVISIBLE);
                }
                break;
            }

            case TVMessage.TYPE_PVR_PLAY_PARENTAL_LOCK: //connie 20180806 for pvr parentalRate
            {
                if(passwordDia!=null && passwordDia.isShowing())
                    break;
                mPlayPosStatus = 1;
                mCurPosStatus = 1;
                ShowPasswordDialog(playingFile);

            }break;
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        Log.d(TAG, "onPause: ");
        
        PvrPlayStop();
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        loadFile.interrupt();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        int playStatus = PvrPlayGetCurrentStatus();
        EnTrickMode trickMode = PvrPlayGetCurrentTrickMode();

        Log.d(TAG, "onKeyDown: playStatus = "+playStatus);
        Log.d(TAG, "onKeyDown: keyCode = "+keyCode);

        if ( HasNoFile( mFileList ) )
            return super.onKeyDown(keyCode, event);

        if ( detailLayout.isShown() &&
                KEYCODE_BACK != keyCode &&
                KEYCODE_INFO != keyCode &&
                ExtKeyboardDefine.KEYCODE_INFO != keyCode &&    // Johnny 20181210 for keyboard control
                KEYCODE_DPAD_DOWN != keyCode &&
                KEYCODE_DPAD_UP != keyCode)
            return true;

        switch (keyCode) {

            // Johnny 20181219 for mouse control, is handled in holder.itemView.setOnClickListener
//            case KEYCODE_DPAD_CENTER:
//                if (playStatus == INIT)                 MediaPlay(CurrentFilePath());
//                else if (playStatus == PLAY)            MediaFullScreen();
//                else if (playStatus == PAUSE)           MediaFullScreen();
//                else if (playStatus == FAST_FORWARD)    MediaFullScreen();
//                else if (playStatus == FAST_BACKWARD)   MediaFullScreen();
//                else // NOT_INIT, STOP, INVALID
//                {
//                    Log.d(TAG, "onKeyDown: playStatus = "+playStatus);
//                    Log.d(TAG, "onKeyDown: key OK failed!");
//                }
//                SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());
//                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY: {//Scoty 20181106 add for separate Play and Pause key
                switch (playStatus)
                {
                    case PAUSE:
                    case FAST_FORWARD:
                    case FAST_BACKWARD:
                        MediaResume();
                        SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());
                    break;
                }
            }break;
            case KeyEvent.KEYCODE_MEDIA_PAUSE: {//Scoty 20181106 add for separate Play and Pause key
                switch (playStatus) {
                    case PLAY:
                    case FAST_FORWARD:
                    case FAST_BACKWARD:
                        MediaPause();
                        SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());
                        break;
                }
            }break;
            case KEYCODE_MEDIA_PLAY_PAUSE:
            case ExtKeyboardDefine.KEYCODE_MEDIA_PLAY_PAUSE:    // Johnny 20181210 for keyboard control
                if (playStatus == INIT)                 MediaPlay(CurrentFilePath());
                else if (playStatus == PLAY)            MediaPause();
                else if (playStatus == PAUSE)           MediaResume();
                else if (playStatus == FAST_FORWARD)    MediaResume();
                else if (playStatus == FAST_BACKWARD)   MediaResume();
                else // NOT_INIT, STOP, INVALID
                {
                    Log.d(TAG, "onKeyDown: playStatus = "+playStatus);
                    Log.d(TAG, "onKeyDown: key PLAY/PAUSE failed!");
                }
                SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());
                break;

            case KEYCODE_MEDIA_STOP:
            case ExtKeyboardDefine.KEYCODE_MEDIA_STOP:  // Johnny 20181210 for keyboard control
                MediaStop();
                SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());
                break;

            case KEYCODE_MEDIA_FAST_FORWARD:
            case ExtKeyboardDefine.KEYCODE_MEDIA_FAST_FORWARD:  // Johnny 20181210 for keyboard control
            case KEYCODE_MEDIA_REWIND:
            case ExtKeyboardDefine.KEYCODE_MEDIA_REWIND:  // Johnny 20181210 for keyboard control
                MediaPlayTrick(trickMode, keyCode);
                SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());
                break;

            case KEYCODE_MEDIA_NEXT:
            //case KEYCODE_DPAD_RIGHT:
                if (!IsFullScreen()) {
                    boolean isScroll = rvList.moveDown();
                    MediaPlayNext();
                    return isScroll;
                }
                break;

            case KEYCODE_MEDIA_PREVIOUS:
            //case KEYCODE_DPAD_LEFT:
                if (!IsFullScreen()) {
                    boolean isScroll = rvList.moveUp();
                    MediaPlayPre();
                    return isScroll;
                }
                break;

            case KEYCODE_DPAD_UP:
                if ( detailLayout.isShown() ) {
                    scrollView.smoothScrollBy( 0, -scrollView.getMeasuredHeight() );
                    return true;
                }
                if (!IsFullScreen())
                    return rvList.moveUp(); // edwin 20180716 return true if cursor move from top to bottom
                break;

            case KEYCODE_DPAD_DOWN:
                if ( detailLayout.isShown() ) {
                    scrollView.smoothScrollBy( 0, scrollView.getMeasuredHeight() );
                    return true;
                }
                if (!IsFullScreen())
                    return rvList.moveDown(); // edwin 20180716 return true if cursor move from bottom to top
                break;

            case KEYCODE_PAGE_UP:
                if ( !IsFullScreen() )
                    rvList.pageNext();
                break;

            case KEYCODE_PAGE_DOWN:
                if ( !IsFullScreen() )
                    rvList.pagePrev();
                break;

            case KEYCODE_INFO:
            case ExtKeyboardDefine.KEYCODE_INFO:    // Johnny 20181210 for keyboard control
                if (IsFullScreen())
                    ShowBanner();
                else if ( ! detailLayout.isShown() )
                    SetDetailInfo(true);
                else if ( detailLayout.isShown() )
                    SetDetailInfo( false );
                break;

            case KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                if ( !IsFullScreen() ) {
                    DeleteOneDialog();
                    SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());
                }
                break;

            /*case KEYCODE_PROG_GREEN:
            case ExtKeyboardDefine.KEYCODE_PROG_GREEN:
                if ( !IsFullScreen() ) {
                    DeleteAllDialog();
                    SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());
                }
                break;*/

            case KEYCODE_BACK:
                if ( IsFullScreen() ) {
                    SetScreen(NORMAL_SCREEN);
                    return true;
                }
                else if ( detailLayout.isShown() ) {
                    SetDetailInfo( false );
                    return true;
                }
                break;

            case KEYCODE_MEDIA_AUDIO_TRACK:
            case ExtKeyboardDefine.KEYCODE_MEDIA_AUDIO_TRACK:   // Johnny 20181210 for keyboard control
                //eric lin 20180720 add file play audio dialog
                if ( IsFullScreen() ) {
                    final AudioInfo AudioComp = PvrPlayGetAudioComponents();
                    EnAudioTrackMode curTrackMode = PvrPlayGetTrackMode();
                    if ( AudioComp != null )
                    {
                        mAudioDialogView = new AudioDialogView(//eric lin 20180801 fix after playback finish,audio dialog not auto closed
                                mContext, 1, curTrackMode, AudioComp, new AudioDialogView.OnAudioClickListener()
                        {
                            public void AudioClicked ()
                            {
                                PvrPlaySelectAudio( AudioComp.getComponent( AudioComp.getCurPos() ) );
                            }
                        } );
                        new Handler().postDelayed(new Runnable() { // Edwin 20190509 fix dialog not focus
                            @Override
                            public void run () {
                                mAudioDialogView.show();//eric lin 20180801 fix after playback finish,audio dialog not auto closed
                            }
                        }, 200); // Edwin 20190515 prevent from dialog has no focus
                    }
                }
                else {
                    //Log.d(TAG, "onKeyDown: KEYCODE_MEDIA_AUDIO_TRACK not full screen");
                    return true;
                }
                break;

            case KEYCODE_CAPTIONS: // subtitle key
            case ExtKeyboardDefine.KEYCODE_CAPTIONS:    // Johnny 20181210 for keyboard control
                if ( IsFullScreen() )
                {
                    final SubtitleInfo Subtitle = AvControlGetSubtitleList( ViewHistory.getPlayId() );
                    if ( Subtitle == null )
                    {
                        new MessageDialogView( mContext, getString( R.string.STR_NO_SUBTITLE ), 3000 )
                        {
                            public void dialogEnd ()
                            {
                            }
                        }.show();
                        break;
                    }
                    final SubtitleDialogView dialog = new SubtitleDialogView( mContext, Subtitle, new SubtitleDialogView.OnSubtitleClickedListener()
                    {
                        @Override
                        public void SubtitleClicked ()
                        {
                            AvControlSelectSubtitle( ViewHistory.getPlayId(), Subtitle.getComponent( Subtitle.getCurPos() ) );
                        }
                    } );//.show();
                    new android.os.Handler().postDelayed(new Runnable() { // Edwin 20190509 fix dialog not focus
                        @Override
                        public void run () {
                            dialog.show();
                        }
                    }, 150);
                }
                break;

            case KEYCODE_TV_TELETEXT:
            case ExtKeyboardDefine.KEYCODE_TV_TELETEXT:   // Johnny 20181210 for keyboard control
                if ( IsFullScreen() ) {
                    showTeletextDialog();
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void showTeletextDialog()
    {
        final TeletextInfo TeletextInfo = AvControlGetCurrentTeletext(ViewHistory.getPlayId());


        if (TeletextInfo != null)
        {
            new TeletextDialogView(this,mDtv,ViewHistory.getPlayId());
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

    private void MediaFullScreen()
    {
        Log.d(TAG, "MediaFullScreen: ");

        if (CurrentFilePath().equals(playingFile)) // same file
        {
            SetScreen(FULL_SCREEN);
        }
        else // play new file
        {
            PvrPlayStop();
            MediaPlay(CurrentFilePath());
        }
    }

    private void MediaPlay ( final String filePath )
    {
        Log.d( TAG, "MediaPlay: set normal screen" );

        this.playingFile = CurrentFilePath();
        this.playingFileDuration = PvrFileGetDuration( filePath );    // Johnny 20180802 fix calling pvrFileGetDuration too often
        InitHelp( R.string.STR_OK_TO_FULL_SCREEN );

        //        int lastTime = 0;
        //        if (lastTime != 0)
        //        {
        //            PlayLastTime(FileNameGet());
        //        }
        //        else
        {
            //eric lin 20180725 records list play and lock symbol,-start 
            if ( mCurPosStatus == 1 )
            {
                mPlayPosStatus = 1;
                ShowPasswordDialog( filePath );
            }
            else
            {
                playFile( filePath );
            }
            //eric lin 20180725 records list play and lock symbol,-end  
        }
    }

    private void playFile ( final String filePath )
    {
        Log.d( TAG, "playFile: " );

        //eric lin 20180720 play last stop time,-start
        int point = PvrPlayFileCheckLastViewPoint( filePath );
        if ( point == 1 )
        {
            new SureDialog( this )
            {
                public void onSetMessage ( View v )
                {
                    ( (TextView) v ).setText( getString( R.string.STR_DO_YOU_WANT_TO_PLAY_THE_FILE_FROM_THE_LAST_STOP_TIME ) );
                }

                public void onSetNegativeButton ()
                {
                    PvrSetStartPositionFlag( 0 );
                    play( filePath );
                    SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());//Scoty 20180815 modify play icon show wrong
                }

                public void onSetPositiveButton ()
                {
                    PvrSetStartPositionFlag( 1 );
                    play( filePath );
                    SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());//Scoty 20180815 modify play icon show wrong
                }
            };
        }
        else
        {
            play( filePath );
        }
        //eric lin 20180720 play last stop time,-end
    }

    private void play ( String filePath ) //eric lin 20180720 play last stop time
    {
        Log.d( TAG, "play: " );

        //eric lin 20180725 records list play and lock symbol,-start
        mCurPlayPos = rvList.getCurPos();
        if ( mPrePlayPos != -1 )
        {
            if ( mPrePlayPos != mCurPlayPos ) //clear prePlayPos's playSymbol
            {
                rvList.getAdapter().notifyItemChanged( mPrePlayPos );
                mPrePlayPos = mCurPlayPos;
            }
        }
        else
        {
            mPrePlayPos = mCurPlayPos;
        }
        rvList.getAdapter().notifyItemChanged( mCurPlayPos );//show curPlayPos's playSymbol
        //eric lin 20180725 records list play and lock symbol,-end

        PvrPlayStart( filePath );
        PlayProgress( filePath );
        SetScreen( NORMAL_SCREEN ); // for TV playing to PVR playing
    }

    private void MediaPause()
    {
        Log.d(TAG, "MediaPause: ");

        if (CurrentFilePath().equals(playingFile)) // same file
        {
            PvrPlayPause();

            if (IsFullScreen())
            {
                progressBanner.removeCallbacks(hideBanner); // edwin 20180622 banner disappear
                progressBanner.setVisibility(this, VISIBLE, playingFileDuration,false);//Scoty 20180802 fixed cur time not update when focus on Pip // Johnny 20180802 fix calling pvrFileGetDuration too often
            }
        }
        else // play new file
        {
            PvrPlayStop();
            MediaPlay(CurrentFilePath());
        }
    }

    private void MediaResume() {

        Log.d(TAG, "MediaResume: ");

        if (CurrentFilePath().equals(playingFile)) // same file
        {
            PvrPlayResume();

            if (IsFullScreen())
            {
                ShowBanner();
            }
        }
        else // Play new file
        {
            PvrPlayStop();
            MediaPlay(CurrentFilePath());
        }
    }

    private void MediaStop() {

        Log.d(TAG, "MediaStop: ");

        PvrPlayStop();

        playingFile = "";
        playingFileDuration = 0;    // Johnny 20180802 fix calling pvrFileGetDuration too often
        progressBanner.setVisibility(INVISIBLE);
        InitHelp(R.string.STR_OK_TO_PLAY_RECORD);
        SetScreen(NORMAL_SCREEN);
    }

    private void MediaPlayNext() {

        Log.d(TAG, "MediaPlayNext: ");

        PvrPlayStop();
        MediaPlay(CurrentFilePath());
        ShowPlayStatus(PLAY_NEXT_FILE);
    }

    private void MediaPlayPre() {

        Log.d(TAG, "MediaPlayPre: ");

        PvrPlayStop();
        MediaPlay(CurrentFilePath());
        ShowPlayStatus(PLAY_PREV_FILE);
    }

    private void MediaPlayTrick(EnTrickMode trickMode, int keyCode) {

        Log.d(TAG, "MediaPlayTrick: keyCode = "+keyCode+" trickMode = "+trickMode);

        if (keyCode == KEYCODE_MEDIA_FAST_FORWARD
                || keyCode == ExtKeyboardDefine.KEYCODE_MEDIA_FAST_FORWARD) // Johnny 20181210 for keyboard controlv
        {
            if (trickMode == FAST_FORWARD_NORMAL)           PvrPlayTrickPlay(FAST_FORWARD_TWO);
            else if (trickMode == FAST_FORWARD_TWO)         PvrPlayTrickPlay(FAST_FORWARD_FOUR);
            else if (trickMode == FAST_FORWARD_FOUR)        PvrPlayTrickPlay(FAST_FORWARD_EIGHT);
            else if (trickMode == FAST_FORWARD_EIGHT)       PvrPlayTrickPlay(FAST_FORWARD_SIXTEEN);
            else if (trickMode == FAST_FORWARD_SIXTEEN)     PvrPlayTrickPlay(FAST_FORWARD_THIRTYTWO);
            else if (trickMode == FAST_FORWARD_THIRTYTWO)   PvrPlayTrickPlay(FAST_FORWARD_TWO);
            else if (trickMode == FAST_BACKWARD_TWO)        PvrPlayTrickPlay(FAST_FORWARD_TWO);
            else if (trickMode == FAST_BACKWARD_FOUR)       PvrPlayTrickPlay(FAST_FORWARD_TWO);
            else if (trickMode == FAST_BACKWARD_EIGHT)      PvrPlayTrickPlay(FAST_FORWARD_TWO);
            else if (trickMode == FAST_BACKWARD_SIXTEEN)    PvrPlayTrickPlay(FAST_FORWARD_TWO);
            else if (trickMode == FAST_BACKWARD_THIRTYTWO)  PvrPlayTrickPlay(FAST_FORWARD_TWO);
        }
        else if (keyCode == KEYCODE_MEDIA_REWIND
                || keyCode == ExtKeyboardDefine.KEYCODE_MEDIA_REWIND)   // Johnny 20181210 for keyboard control
        {
            if (trickMode == FAST_FORWARD_NORMAL)           PvrPlayTrickPlay(FAST_BACKWARD_TWO);
            else if (trickMode == FAST_BACKWARD_TWO)        PvrPlayTrickPlay(FAST_BACKWARD_FOUR);
            else if (trickMode == FAST_BACKWARD_FOUR)       PvrPlayTrickPlay(FAST_BACKWARD_EIGHT);
            else if (trickMode == FAST_BACKWARD_EIGHT)      PvrPlayTrickPlay(FAST_BACKWARD_SIXTEEN);
            else if (trickMode == FAST_BACKWARD_SIXTEEN)    PvrPlayTrickPlay(FAST_BACKWARD_THIRTYTWO);
            else if (trickMode == FAST_BACKWARD_THIRTYTWO)  PvrPlayTrickPlay(FAST_BACKWARD_TWO);
            else if (trickMode == FAST_FORWARD_TWO)         PvrPlayTrickPlay(FAST_BACKWARD_TWO);
            else if (trickMode == FAST_FORWARD_FOUR)        PvrPlayTrickPlay(FAST_BACKWARD_TWO);
            else if (trickMode == FAST_FORWARD_EIGHT)       PvrPlayTrickPlay(FAST_BACKWARD_TWO);
            else if (trickMode == FAST_FORWARD_SIXTEEN)     PvrPlayTrickPlay(FAST_BACKWARD_TWO);
            else if (trickMode == FAST_FORWARD_THIRTYTWO)   PvrPlayTrickPlay(FAST_BACKWARD_TWO);
        }

        if (IsFullScreen())
        {
            progressBanner.removeCallbacks(hideBanner); // edwin 20180622 banner disappear
            progressBanner.setVisibility(this, VISIBLE, playingFileDuration,false);//Scoty 20180802 fixed cur time not update when focus on Pip // Johnny 20180802 fix calling pvrFileGetDuration too often

            // Edwin 20190515 fix mantis 5582
            //progressBanner.updateTimeStatus(RecordListActivity.this, PvrPlayGetDuration(),false);//Scoty 20180802 fixed cur time not update when focus on Pip
        }
    }

    private void DeleteOneDialog() {

        Log.d(TAG, "DeleteOneDialog: ");

        String msg = getString(R.string.STR_DO_YOU_WANT_TO_DELETE_THE_RECORDING);

        final PvrDeleteDialog deleteDialog = new PvrDeleteDialog(this, msg) {
            @Override
            void OnClickYes()
            {
                Log.d(TAG, "DeleteOneDialog: OnClickYes: Delete Record Pos = "+rvList.getCurPos());

                String filePath = CurrentFilePath();

                if (filePath.equals(""))
                {
                    return;
                }

                if (filePath.equals(playingFile)) // edwin 20180621 fix delete when playing file
                {
                    PvrPlayStop();
                    SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());
                }

                //DeleteOneFile(filePath);
                DeleteOneFile();
            }

            @Override
            void OnClickNo() {
                dismiss();
            }
        };

        if ( ! CurrentFilePath().equals("") )
        {
            new Handler().postDelayed(new Runnable() { // Edwin 20190510 fix dialog has no focus
                @Override
                public void run () {
                    deleteDialog.show();
                }
            }, 150);
        }
    }

    private void DeleteAllDialog() {

        Log.d(TAG, "DeleteAllDialog: ");

        String msg = getString(R.string.STR_DO_YOU_WANT_TO_DELETE_All_RECORD_FILES);

        final PvrDeleteDialog deleteDialog = new PvrDeleteDialog(this, msg) {
            @Override
            void OnClickYes()
            {
                Log.d(TAG, "DeleteAllDialog: OnClickYes: Delete Record Pos = "+rvList.getCurPos());

                String filePath = CurrentFilePath();

                if (filePath.equals(""))
                {
                    return;
                }

                if (filePath.equals(playingFile)) // edwin 20180621 fix delete when playing file
                {
                    PvrPlayStop();
                    SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());
                }

                DeleteAllFile();
            }

            @Override
            void OnClickNo() {
                dismiss();
            }
        };

        if ( ! CurrentFilePath().equals("") )
        {
            new Handler().postDelayed(new Runnable() { // Edwin 20190510 fix dialog has no focus
                @Override
                public void run () {
                    deleteDialog.show();
                }
            }, 150);
        }
    }

    private void DeleteOneFile()
    {
        Log.d(TAG, "DeleteOneFile: ");

        // edwin 20180815 speed up removing file
        PvrFileInfo fileInfo = mFileList.get( rvList.getCurPos() );
        if (fileInfo.isSeries)
            Pvr_Delete_One_Series_Folder ( fileInfo.recordUniqueId );
        else
            Pvr_Delete_Record_File       ( fileInfo.recordUniqueId );

        rvList.remove( mFileList, rvList.getCurPos() ); // edwin 20180815 fix file removing error

        if (mFileList.size() == 0)
        {
            emptyFile.setVisibility(VISIBLE);
            rvList.setVisibility(INVISIBLE);
            timeTag.setVisibility(INVISIBLE); // edwin 20180622 hide "Time: 00:00:00"
            time.setVisibility(INVISIBLE);
            fileSize.setText(R.string.STR_0MB);
            //Scoty 20180815 hide some information when no file -s
            recTitleStr.setVisibility(INVISIBLE);
            progName.setVisibility(INVISIBLE);
            recDate.setVisibility(INVISIBLE);
            parentalTitle.setVisibility(INVISIBLE);
            parentalRate.setVisibility(INVISIBLE);
            //Scoty 20180815 hide some information when no file -e
        }
    }

    private void DeleteAllFile()
    {
        Log.d(TAG, "DeleteAllFile: ");

        // edwin 20180815 speed up removing file
        Pvr_Delete_Total_Records_File();

        mFileList = new ArrayList<>();
        RecAdapter recAdapter = (RecAdapter) rvList.getAdapter();
        if ( recAdapter != null )
            recAdapter.notifyDataSetChanged();

        if (mFileList.size() == 0)
        {
            emptyFile.setVisibility(VISIBLE);
            rvList.setVisibility(INVISIBLE);
            timeTag.setVisibility(INVISIBLE); // edwin 20180622 hide "Time: 00:00:00"
            time.setVisibility(INVISIBLE);
            fileSize.setText(R.string.STR_0MB);
            //Scoty 20180815 hide some information when no file -s
            recTitleStr.setVisibility(INVISIBLE);
            progName.setVisibility(INVISIBLE);
            recDate.setVisibility(INVISIBLE);
            parentalTitle.setVisibility(INVISIBLE);
            parentalRate.setVisibility(INVISIBLE);
            //Scoty 20180815 hide some information when no file -e
        }
    }

    private String CurrentFilePath()
    {
        String dir = GetRecordPath().concat( getString(R.string.STR_RECORD_DIR) );
        String currentFile;

        if (mFileList == null || mFileList.size() == 0)
            currentFile = "";
        else
            currentFile = dir + mFileList.get( rvList.getCurPos() ).realFileName;

        Log.d( TAG, "CurrentFilePath: "+currentFile );

        return currentFile;
    }

//    private String CurrentFileName() {
//        return recFileList[rvList.getCurPos()].getName();
//    }

    private int getCurPos()//eric lin 20180725 records list play and lock symbol
    {
        return rvList.getCurPos();
    }

//    private File FileDir()
//    {
//        Log.d(TAG, "FileDir: ");
//
//        File fileDir = new File(GetRecordPath()+getString(R.string.STR_RECORD_DIR));
//        if( ! fileDir.exists() )
//        {
//            if ( ! fileDir.mkdir() )
//            {
//                Log.d(TAG, "FileDir: make directory fail");
//            }
//        }
//        return fileDir;
//    }
//
//    private FileFilter Filter()
//    {
//        Log.d(TAG, "Filter: ");
//
//        return new FileFilter()
//        {
//            @Override
//            public boolean accept ( File pathname )
//            {
//                String fileName = pathname.getName();
//                String absFilePath = pathname.getAbsolutePath();
//                Log.d( TAG, "++++++++++++++++++++++++++++++++++++++++++"
//                        +"\n"+"accept: fileName = "+fileName+" absFilePath = "+absFilePath
//                        +"\n"+"accept: endsWith .ts = "+fileName.endsWith( getString( R.string.STR_TS ) )
//                        +"\n"+"accept: file size = "+PvrFileGetSize( absFilePath )
//                        +"\n"+"accept: idx.exists() = "+new File( absFilePath.concat( ".idx" ) ).exists()
//                        +"\n"+"accept: 000x.ts = "+fileName.matches( "^.+\\.\\d{4}\\.ts" ) );
//                return fileName.endsWith( getString( R.string.STR_TS ) )
//                       && new File( absFilePath.concat( ".idx" ) ).exists()    // edwin 20180806 ".ts" must has its ".idx"
//                       && !fileName.matches( "^.+\\.\\d{4}\\.ts" );      // edwin 20180803 filter ".000X.ts"
//                       //&& PvrFileGetSize( absFilePath ) != -1;               // edwin 20180803 remove service call
//            }
//        };
//    }

    private void ResetPlayStatus ()
    {
        Log.d( TAG, "ResetPlayStatus: " );

        pvrStop.setImageResource( R.drawable.pvr_stop_n );
        pvrPlay.setImageResource( R.drawable.pvr_play_n );
        pvrPause.setImageResource( R.drawable.pvr_pause_n );
        pvrForward.setImageResource( R.drawable.pvr_foward_n );
        pvrRewind.setImageResource( R.drawable.pvr_backward_n );
        pvrNextFile.setImageResource( R.drawable.pvr_next_file_n );
        pvrPreFile.setImageResource( R.drawable.pvr_previous_file_n );
    }

    private void ResetProgress()
    {
        Log.d(TAG, "ResetProgress: ");

        progressBar.setProgress(2, false); // edwin 20180626
        progressBar.setProgress(1, false); // fix for android bug
        progressBar.setProgress(0, false); // unknown problem
    }

    private String GetEpgDate(PvrFileInfo info)
    {
        Log.d( TAG, "GetEpgDate: " );

        int sHour = info.hour;
        int sMin = info.minute;
        int eHour = info.hourEnd;
        int eMin = info.minuteEnd;
        String startHour;
        String sartMin;
        String endHour;
        String endMin;
        String zero = getString(R.string.STR_0);
        String space = getString(R.string.STR_SPACE);
        String sep = getString(R.string.STR_BOOK_ALARM_SEPARATE);
        String dash = getString(R.string.STR_DASH);

        if ( sHour == 0 && sMin == 0 && eHour == 0 && eMin == 0 )
            return null;

        startHour = sHour < 10 ?
                zero + sHour :
                String.valueOf( sHour );
        startHour += sep;
        sartMin = sMin < 10 ?
                zero + sMin :
                String.valueOf( sMin );
        sartMin = sartMin + space + dash + space;
        endHour = eHour < 10 ?
                zero + eHour :
                String.valueOf( eHour );
        endHour += sep;
        endMin = eMin < 10 ?
                zero + eMin :
                String.valueOf( eMin );

        return startHour + sartMin + endHour + endMin;
    }

    private void SetScreen(boolean setFullScreen)
    {
        Log.d(TAG, "SetFullScreen: ");

        boolean isSmallScreen = ! IsFullScreen();
        int loc[] = new int[2];
        smallScreen.getLocationOnScreen(loc);
        Rect smallRect = new Rect(loc[0], loc[1], loc[0]+smallScreen.getWidth(), loc[1]+smallScreen.getHeight());
        Rect fullRect = new Rect(0, 0, fullLayout.getWidth(), fullLayout.getHeight());

        if (setFullScreen)
        {
            Log.d(TAG, "SetFullScreen: set FULL SCREEN");

            PvrPlaySetWindowRect(fullRect);
            SetALL(INVISIBLE);

            if ( isSmallScreen )
            {
                ShowBanner();
            }
        }
        else 
        {
            Log.d(TAG, "SetFullScreen: set small screen");
            if(mAudioDialogView!=null && mAudioDialogView.isShowing()) {//eric lin 20180801 fix after playback finish,audio dialog not auto closed
                //Log.d(TAG, "SetScreen: mAudioDialogView.dismiss()");
                mAudioDialogView.dismiss();
            }

            SetALL(VISIBLE);
            rvList.setSelection(rvList.getCurPos());
            PvrPlaySetWindowRect(smallRect);
            progressBanner.setVisibility(INVISIBLE);
        }
    }

    private void SetALL(int visibility)
    {
        Log.d(TAG, "SetALL: ");

        if (visibility == INVISIBLE)
        {
            fullLayout.setBackgroundColor(Color.TRANSPARENT);
        }
        else
        {
            fullLayout.setBackgroundResource(R.drawable.bg);
        }

        rec_list_layout.setVisibility(visibility);//eric lin 20180720 add file play audio dialog
        fileList.setVisibility(visibility);
        rvList.setVisibility(visibility);
        recDate.setVisibility(visibility);
        time.setVisibility(visibility);
        parentalRate.setVisibility(visibility); //connie 20180806 for pvr parentalRate
        parentalTitle.setVisibility(visibility); //connie 20180806 for pvr parentalRate
        pvrSpeed.setVisibility(visibility);
        progressBar.setVisibility(visibility);
        pvrPreFile.setVisibility(visibility);
        pvrRewind.setVisibility(visibility);
        pvrPlay.setVisibility(visibility);
        pvrPause.setVisibility(visibility);
        pvrStop.setVisibility(visibility);
        pvrForward.setVisibility(visibility);
        pvrNextFile.setVisibility(visibility);
        smallScreen.setVisibility(visibility);
        title.setVisibility(visibility);
        help.setVisibility(visibility);
        fileSizeTag.setVisibility(visibility);
        timeTag.setVisibility(visibility);
        panel.setVisibility(visibility);
        progName.setVisibility(visibility);
        fileSize.setVisibility(visibility);
        recTitleStr.setVisibility(visibility);
        //hdFreeSize.setVisibility(visibility);
        //hdTotalSize.setVisibility(visibility);
        //freeSizeTag.setVisibility(visibility);
        //totalSizeTag.setVisibility(visibility);
        //surfaceView.setVisibility(visibility);
    }

    @SuppressLint("SetTextI18n")
    private void SetPlayStatus(int playStatus, EnTrickMode trickMode) {

        ResetPlayStatus();

        Log.d(TAG, "SetPlayStatus: playStatus = "+playStatus);
        if (playStatus == PLAY)                 pvrPlay.setImageResource(R.drawable.pvr_play_f);
        else if (playStatus == PAUSE)           pvrPause.setImageResource(R.drawable.pvr_pause_f);
        else if (playStatus == FAST_FORWARD)    pvrForward.setImageResource(R.drawable.pvr_foward_f);
        else if (playStatus == FAST_BACKWARD)   pvrRewind.setImageResource(R.drawable.pvr_backward_f);
        else
        {
            pvrStop.setImageResource(R.drawable.pvr_stop_f);
            ResetProgress();
        }
        // playStatus == INIT || playStatus == NOT_INIT || playStatus == INVALID || playStatus == STOP

        pvrSpeed.setVisibility(VISIBLE);

        Log.d(TAG, "SetPlayStatus: trickMode = "+trickMode);
        if (trickMode == FAST_FORWARD_TWO)              pvrSpeed.setText(getString(R.string.STR_2X_FF));
        else if (trickMode == FAST_FORWARD_FOUR)        pvrSpeed.setText(getString(R.string.STR_4X_FF));
        else if (trickMode == FAST_FORWARD_EIGHT)       pvrSpeed.setText(getString(R.string.STR_8X_FF));
        else if (trickMode == FAST_FORWARD_SIXTEEN)     pvrSpeed.setText(getString(R.string.STR_16X_FF));
        else if (trickMode == FAST_FORWARD_THIRTYTWO)   pvrSpeed.setText(getString(R.string.STR_32X_FF));
        else if (trickMode == FAST_BACKWARD_TWO)        pvrSpeed.setText(getString(R.string.STR_2X_BF));
        else if (trickMode == FAST_BACKWARD_FOUR)       pvrSpeed.setText(getString(R.string.STR_4X_BF));
        else if (trickMode == FAST_BACKWARD_EIGHT)      pvrSpeed.setText(getString(R.string.STR_8X_BF));
        else if (trickMode == FAST_BACKWARD_SIXTEEN)    pvrSpeed.setText(getString(R.string.STR_16X_BF));
        else if (trickMode == FAST_BACKWARD_THIRTYTWO)  pvrSpeed.setText(getString(R.string.STR_32X_BF));
        else                                            pvrSpeed.setText(null);
    }

    private void SetDetailInfo ( boolean show )
    {
        Log.d(TAG, "SetDetailInfo: ");

        if ( ! show ) {
            detailLayout.setVisibility( GONE );
            InitHelp( R.string.STR_OK_TO_PLAY_RECORD );
            return;
        }
        PvrFileInfo fileInfo = PvrFileGetEpgInfo( CurrentFilePath(), EPG_PRESENT );

        detailLayout.setVisibility( VISIBLE );
        txvEpgTime.setText( GetEpgDate( fileInfo ) );
        txvEpgName.setText( fileInfo.eventName );

        if ( fileInfo.extendedText == null )
            txvEpgNotFound.setVisibility( VISIBLE );
        else
            txvEpgNotFound.setVisibility( INVISIBLE );

        txvEpgDetail.setText( fileInfo.extendedText );

        help.setHelpInfoTextBySplit(getString(R.string.STR_BACK_TO_CLOSE_DETAIL));
        help.resetHelp(1, 0, null);
        help.resetHelp(2, 0, null);
        help.resetHelp(3, 0, null);
        help.resetHelp(4, 0, null);
    }

    private void ShowBanner()
    {
        Log.d(TAG, "ShowBanner: ");

        progressBanner.removeCallbacks(hideBanner);
        progressBanner.setVisibility(this, VISIBLE, playingFileDuration,false);//Scoty 20180802 fixed cur time not update when focus on Pip // Johnny 20180802 fix calling pvrFileGetDuration too often
        progressBanner.postDelayed(hideBanner, bannerTimeout);
    }

    private void ShowPlayStatus(int playStatus)
    {
        Log.d(TAG, "ShowPlayStatus: ");

        Handler hdrStatus = new Handler();

        ResetPlayStatus();

        switch (playStatus)
        {
            case PLAY_NEXT_FILE:
                pvrNextFile.setImageResource(R.drawable.pvr_next_file_f);
                break;

            case PLAY_PREV_FILE:
                pvrPreFile.setImageResource(R.drawable.pvr_previous_file_f);
                break;
        }

        hdrStatus.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());
            }
        }, 500);
    }

//    private void PlayLastTime(String fullFileName) {
//
//        Log.d(TAG, "PlayLastTime: ");
//
//        PvrLastTimeDialog pvrLastTimeDialog = new PvrLastTimeDialog(this, fullFileName) {
//            @Override
//            void OnClickYes(String fullFile)
//            {
//                int exampleTime = 10;
//                PvrPlaySeekTo(exampleTime);
//            }
//
//            @Override
//            void OnClickNo(String fullFile)
//            {
//
//            }
//        };
//        pvrLastTimeDialog.show();
//    }
    
    private void PlayProgress(final String fullPath) {

        Log.d(TAG, "PlayProgress: fullPath = "+fullPath);

//      final  int maxDuration = PvrFileGetDuration(fullPath);   // Johnny 20180802 fix calling pvrFileGetDuration too often
        ResetProgress();
        progressBar.setMax(playingFileDuration);    // Johnny 20180802 fix calling pvrFileGetDuration too often
        hdrUpdateProgress.post(updateProgress);
    }

    private void InitViewById() {

        Log.d(TAG, "InitViewById: ");

        rec_list_layout = (LinearLayout) findViewById(R.id.rec_list_layout);//eric lin 20180720 add file play audio dialog
        title       = (ActivityTitleView) findViewById(R.id.pvrTitle);
        help        = (ActivityHelpView) findViewById(R.id.pvrHelp);
        smallScreen = (ConstraintLayout) findViewById(R.id.pvr_video_frame);
        fullLayout  = (ConstraintLayout) findViewById(R.id.pvr_main);
        rvList      = (PvrRecyclerView) findViewById(R.id.pvr_rec_list);
        progressBar = (ProgressBar) findViewById(R.id.pvr_progressBar);
        surfaceView = (SurfaceView) findViewById(R.id.pvr_video);
        fileSize    = (TextView) findViewById(R.id.file_size);
        recDate     = (TextView) findViewById(R.id.rec_date);
        time        = (TextView) findViewById(R.id.pvr_time);
        progName    = (TextView) findViewById(R.id.pvr_program_name);
        pvrSpeed    = (TextView) findViewById(R.id.pvr_speed);
        emptyFile   = (TextView) findViewById(R.id.empty_file_list);
        fileList    = (TextView) findViewById(R.id.file_list_title);
        recTitleStr = (TextView) findViewById(R.id.rec_date_str);
        parentalRate = (TextView) findViewById(R.id.rateTXV);
        parentalTitle = (TextView) findViewById(R.id.partentTXV);
        pvrPreFile  = (ImageView) findViewById(R.id.pre_file);
        pvrRewind   = (ImageView) findViewById(R.id.backward);
        pvrPlay     = (ImageView) findViewById(R.id.play);
        pvrPause    = (ImageView) findViewById(R.id.pause);
        pvrStop     = (ImageView) findViewById(R.id.stop);
        pvrForward  = (ImageView) findViewById(R.id.foward);
        pvrNextFile = (ImageView) findViewById(R.id.next_file);
        fileSizeTag     = (TextView) findViewById(R.id.file_size_str);
        timeTag         = (TextView) findViewById(R.id.pvr_time_str);
        panel           = (ConstraintLayout) findViewById(R.id.pvr_control_panel);
        progressBanner  = (PvrBannerView) findViewById(R.id.pvr_banner);
        //hdFreeSize  = (TextView) findViewById(R.id.pvr_hd_free_size);
        //hdTotalSize = (TextView) findViewById(R.id.pvr_hd_total_size);
        //freeSizeTag = (TextView) findViewById(R.id.hd_free_size_str);
        //totalSizeTag = (TextView) findViewById(R.id.hd_total_size_str);
        //bannerInfo  = (PvrInfoBannerView) findViewById(R.id.pvr_info_banner);

        // detail info
        detailLayout    = (ConstraintLayout) findViewById( R.id.epg_info_layout );
        scrollView = (ScrollView) findViewById( R.id.epg_scroll );
        txvEpgTime      = (TextView) findViewById( R.id.epg_time );
        txvEpgName      = (TextView) findViewById( R.id.epg_event_name );
        txvEpgDetail    = (TextView) findViewById( R.id.epg_detail );
        txvEpgNotFound  = (TextView) findViewById( R.id.epg_not_found );

        InitHelp(R.string.STR_OK_TO_PLAY_RECORD);

        // View
        title.setTitleView(getString(R.string.STR_RECORDS_LIST));
        //hdFreeSize.setText(HdFreeSize());
        //hdTotalSize.setText(HdTotalSize());
        pvrStop.setImageResource(R.drawable.pvr_stop_f);

        // Progress Bar
        hdrUpdateProgress = new Handler();
        progressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));

        // Banner
        progressBanner.setVisibility(INVISIBLE);
        bannerTimeout = GposInfoGet().getBannerTimeout() * 1000;
    }

    private void InitHelp(int resourceStrId)
    {
        Log.d(TAG, "InitHelp: ");

        help.setHelpInfoTextBySplit(getString(resourceStrId));
        help.resetHelp(1, R.drawable.help_red, getString(R.string.STR_DELETE));
        help.resetHelp(2, 0, null);
        help.resetHelp(3, 0, null);
        help.resetHelp(4, 0, null);

        help.setHelpIconClickListener(1, new View.OnClickListener() {   // Johnny 20181228 for mouse control
            @Override
            public void onClick(View view) {
                DeleteOneDialog();
                SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());
            }
        });
    }

    private void InitRvList()
    {
        Log.d(TAG, "InitRecList: ");

        final Handler hdrLoadRec = new InitRvHandler(this);
        loadFile = new Thread( new Runnable()
        {
            @Override
            public void run ()
            {
                int total = Pvr_Get_Total_Rec_Num();
                Log.d(TAG, "InitRvList: record files total count = " + total);
                //mFileList = GetFileList();
                mFileList = Pvr_Get_Records_File(0, total);
                for ( PvrFileInfo info : mFileList )
                {
                    Log.d( TAG, "run: info.channelName = "+info.channelName+" info.realFileName = "+info.realFileName );
                }

                if ( HasNoFile(mFileList) )
                {
                    Log.d( TAG, "run: send message(LOADING_FAIL) to handler(hdrLoadRec)" );
                    hdrLoadRec.sendEmptyMessage( LOADING_FAIL );
                    return;
                }

                Log.d( TAG, "InitRvList: send message(LOADING_FINISH) to handler(hdrLoadRec)" );
                hdrLoadRec.sendEmptyMessage( LOADING_FINISH );
            }
        } );
        loadFile.start();
    }

    private void InitRvHandle( Message msg ) // edwin 20180816 fix handler warning
    {
        Log.d( TAG, "InitRvHandle: " );

        switch ( msg.what )
        {
            case LOADING_FINISH:
                rvList.setAdapter( new RecAdapter( /*mFileList*/ ) );
                break;

            case LOADING_FAIL:
                break;
        }

        ProgressBar prgListLoading = (ProgressBar) findViewById( R.id.pvr_rec_list_prgLoading );
        TextView txvListLoading = (TextView) findViewById( R.id.pvr_rec_list_txvLoading );
        prgListLoading.setVisibility( GONE );
        txvListLoading.setVisibility( GONE );
    }

    /*private List<PvrFileInfo> GetFileList()
    {
        List<PvrFileInfo> fileInfoList = new ArrayList<>();
        List<Integer> removeList = new ArrayList<>();
        String dir = GetRecordPath() + getString(R.string.STR_RECORD_DIR);
        //int total = PvrTotalRecordFileOpen( dir );
        int total = Pvr_Get_Total_Rec_Num();

        Log.d( TAG, "GetFileList: dir = " + dir + " , total = "+total );
        if (total > 0)
        {
            //Scoty 20180912 modify Record list sort by date
            PvrTotalRecordFileSort(1); // PVR_SORT_BY_CHNAME = 0 ,PVR_SORT_BY_DATETIME = 1
            //fileInfoList = PvrTotalRecordFileGet(0, total);
            fileInfoList = Pvr_Get_Records_File(0, total);

            for (PvrFileInfo info : fileInfoList)
            {
                long fileSize = PvrFileGetSize(dir + info.realFileName);
                Log.d(TAG, "GetFileList: filePath = " + dir + info.realFileName + " , fileSize = " + fileSize);
                if (fileSize == 0)
                {
                    int index = fileInfoList.indexOf(info);
                    removeList.add(index); // fix operate fileInfoList in for loop
                }
            }
            for (Integer index : removeList)
            {
                fileInfoList.remove(index.intValue());
            }
        }

        return fileInfoList;
    }*/

    private void SetListNum(final PvrRecyclerView rvList, final View topView, final View bottomView)
    {
        Log.d( TAG, "SetListNum: Finish Init = "+finishInit );
        if ( finishInit )
            return;

        int locBottom[] = new int[2];
        int locTop[] = new int[2];
        int rvItemHeight = (int) getResources().getDimension(R.dimen.PVR_LIST_ITEM_HEIGHT);

        bottomView.getLocationOnScreen(locBottom);
        topView.getLocationOnScreen(locTop);

        int rvHeight = locBottom[1] - locTop[1] - topView.getMeasuredHeight();

        rvList.setListNum(rvHeight/rvItemHeight);
        finishInit = true;
    }

//    private String HdFreeSize() {
//
//        Log.d(TAG, "HdFreeSize: ");
//
//        // connie 20180530 add protect-s
//        if( ! FileDir().exists() )
//        {
//            return null;
//        }
//        // connie 20180530 add protect-e
//
//        StatFs statFs = new StatFs(GetRecordPath());
//
//        long freeSizeByte = statFs.getBlockSizeLong() * statFs.getAvailableBlocksLong();
//        long freeSizeMB = freeSizeByte / 1024 / 1024;
//
//        return String.valueOf(freeSizeMB).concat(" MB");
//    }

//    private String HdTotalSize() {
//
//        Log.d(TAG, "HdTotalSize: ");
//
//        // connie 20180530 add protect-s
//        StatFs statFs = new StatFs(GetRecordPath());
//
//        if( ! FileDir().exists() )
//        {
//            return null;
//        }
//        // connie 20180530 add protect-e
//
//        long totalSizeByte = statFs.getBlockSizeLong() * statFs.getBlockCountLong();
//        long totalSizeMB = totalSizeByte / 1024 / 1024;
//
//        return String.valueOf(totalSizeMB).concat(" MB");
//    }

    private boolean IsFullScreen()
    {
        Log.d(TAG, "isFullScreen: ");

        if (title.getVisibility() == INVISIBLE)
            return FULL_SCREEN;
        else
            return NORMAL_SCREEN;
    }

    private boolean HasNoFile(List<PvrFileInfo> files)
    {
        boolean empty = ( files == null ) || ( files.size() == 0 );

        if (empty)
        {
            emptyFile.setVisibility(VISIBLE);
            rvList.setVisibility(INVISIBLE);
            //Scoty 20180815 hide some information when no file -s
            timeTag.setVisibility(INVISIBLE);
            time.setVisibility(INVISIBLE);
            recTitleStr.setVisibility(INVISIBLE);
            progName.setVisibility(INVISIBLE);
            recDate.setVisibility(INVISIBLE);
            parentalTitle.setVisibility(INVISIBLE);
            parentalRate.setVisibility(INVISIBLE);
            //Scoty 20180815 hide some information when no file -e
        }

        Log.d(TAG, "HasNoFile: "+empty);

        return empty;
    }

    private void CheckRecordPath(String recordPath)
    {
        Log.d(TAG, "CheckRecordPath: recordPath = "+recordPath);

        // connie 20180530 modify for rec path unmount-s
        File file = new File(recordPath);
        if (!file.exists())
        {
            SetRecordPath(getDefaultRecPath()); // connie 20180525 get path by getDefaultRecPath()
            SaveTable(EnTableType.GPOS);
            file = new File(getDefaultRecPath());
            if (!file.exists()) {
                new MessageDialogView(this, getString(R.string.STR_RECORD_PATH_NOT_AVAILABLE),3000) // edwin 20170626 change meaning
                {
                    public void dialogEnd() {
                        finish();
                    }
                }.show();
            }
        }// connie 20180530 modify for rec path unmount-e
    }

    private static class InitRvHandler extends Handler // edwin 20180816 fix handler warning
    {
        final WeakReference<RecordListActivity> mActivity;

        InitRvHandler ( RecordListActivity activity )
        {
            mActivity = new WeakReference<>( activity );
        }

        @Override
        public void handleMessage ( Message msg )
        {
            RecordListActivity activity = mActivity.get();
            if ( activity != null )
            {
                Log.d( TAG, "handleMessage: wait Thread's message(LOADING_FINISH)" );

                activity.InitRvHandle( msg );
                super.handleMessage( msg );
            }
        }
    }

    private class RecAdapter extends RecyclerView.Adapter<RecAdapter.ViewHolder> {

        //List<PvrFileInfo> mFileList;
        //OnFocusChangeListener showRecInfo;//eric lin 20180725 records list play and lock symbol, mark

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView recItem;
            TextView playSymbol;//eric lin 20180725 records list play and lock symbol
            ImageView lock;//eric lin 20180725 records list play and lock symbol

            public ViewHolder(View itemView) {
                super(itemView);
                recItem = (TextView) itemView.findViewById(R.id.rec_item);
                playSymbol = (TextView) itemView.findViewById(R.id.CurrentPlayTXV);//eric lin 20180725 records list play and lock symbol,-start
                lock = (ImageView) itemView.findViewById(R.id.lockIGV);//eric lin 20180725 records list play and lock symbol,-start //Lock icon
            }
        }

        RecAdapter(/*final List<PvrFileInfo> list*/) {
            //mFileList = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.pvr_rec_list_item, parent, false);

            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            //Log.d(TAG, "onBindViewHolder: pos="+position);

            if ( mFileList == null )
                return;

            if ( position >= mFileList.size() )
                return;

            PvrFileInfo curFileInfo = mFileList.get(position);
            //String filePath = GetRecordPath() + getString(R.string.STR_RECORD_DIR) + curFileInfo.realFileName;

            holder.recItem.setText(curFileInfo.channelName); // edwin 20180815 show channel name

            //eric lin 20180725 records list play and lock symbol,-start
            if (position == mCurPlayPos )
            {
                //Log.d(TAG, "onBindViewHolder: pos="+position+", mCurPlayPos="+mCurPlayPos+", set");
                holder.playSymbol.setText( R.string.STR_PLAY_SYMBOL );
            }
            else
            {
                //Log.d(TAG, "onBindViewHolder: pos="+position+", mCurPlayPos="+mCurPlayPos+", set null");
                holder.playSymbol.setText( "" );
            }

            if( curFileInfo.channelLock == 1 || curFileInfo.parentalRate != 0 )
            {
                //connie 20180806 for pvr parentalRate
                holder.lock.setBackgroundResource( R.drawable.lock );
            }
            else
            {
                holder.lock.setBackgroundResource( android.R.color.transparent );//clean icon
            }
            //eric lin 20180725 records list play and lock symbol,-end

            // Edwin 20190508 fix recyclerView not focus -s
            if (finishInit && (position == 0))
            {
                holder.itemView.requestFocus();
                holder.recItem.setSelected(true);

                // Edwin 20190510 fix rec info -s
                fileSize.setText( FileSize( curFileInfo.fileSize ) );
                //recDate.setText( GetRecDate( filePath ) );
                recDate.setText( GetRecDate( curFileInfo ) );
                progName.setText( curFileInfo.channelName );
                time.setText( Time( curFileInfo.durationSec ) );
                parentalRate.setText( String.valueOf(curFileInfo.parentalRate) );
                // Edwin 20190510 fix rec info -e

                finishInit = false;
            }
            // Edwin 20190508 fix recyclerView not focus -e

            holder.itemView.setOnFocusChangeListener( new View.OnFocusChangeListener()
            {
                @Override
                public void onFocusChange ( View v, boolean hasFocus )
                {
                    if ( !hasFocus )
                    {
                        //holder.itemView.animate().scaleX(1).scaleY(1).setDuration(300).start();
                        holder.recItem.setSelected( false );//eric lin 20180725 records list play and lock symbol
                        return;
                    }

                    if (mFileList == null)
                        return;

                    if (mFileList.size() == 0)
                        return;

                    //holder.itemView.animate().scaleX(1.1f).scaleY(1.1f).setDuration(300).start();
                    holder.recItem.setSelected( true );//eric lin 20180725 records list play and lock symbol

                    //eric lin 20180725 records list play and lock symbol,-start
                    String filePath = CurrentFilePath();
                    //PvrFileInfo fileInfo = PvrFileGetAllInfo( filePath );
                    PvrFileInfo fileInfo = mFileList.get( rvList.getCurPos() );
                    if ( fileInfo.channelLock == 1 )
                    {
                        if ( mCurPlayPos != getCurPos() )
                        {
                            mCurPosStatus = 1;
                        }
                        else
                        {
                            mCurPosStatus = mPlayPosStatus;
                        }
                    }
                    else
                    {
                        mCurPosStatus = 0;
                    }
                    //eric lin 20180725 records list play and lock symbol,-end

                    //Log.d(TAG, "onFocusChange: recFilePath = "+recFilePath+
                    //        ", duration = "+duration+
                    //        ", Time(duration) = "+Time(duration)
                    //);

                    if ( filePath.equals( "" ) )
                    {
                        return;
                    }
                    if ( filePath.equals( playingFile ) )
                    {
                        InitHelp( R.string.STR_OK_TO_FULL_SCREEN );
                    }
                    else
                    {
                        InitHelp( R.string.STR_OK_TO_PLAY_RECORD );
                    }

                    fileSize.setText( FileSize( fileInfo.fileSize ) );
                    recDate.setText( GetRecDate( fileInfo ) );
                    //progName.setText( PvrFileGetExtraInfo( filePath ).channelName );
                    progName.setText( fileInfo.eventName );
                    time.setText( Time( fileInfo.durationSec ) );
                    parentalRate.setText( String.valueOf(fileInfo.parentalRate) ); //connie 20180806 for pvr parentalRate
                }
            } );

            // Johnny 20181219 for mouse control -s
            holder.itemView.setFocusableInTouchMode(true);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int playStatus = PvrPlayGetCurrentStatus();
                    if (playStatus == INIT)                 MediaPlay(CurrentFilePath());
                    else if (playStatus == PLAY)            MediaFullScreen();
                    else if (playStatus == PAUSE)           MediaFullScreen();
                    else if (playStatus == FAST_FORWARD)    MediaFullScreen();
                    else if (playStatus == FAST_BACKWARD)   MediaFullScreen();
                    else // NOT_INIT, STOP, INVALID
                    {
                        Log.d(TAG, "onKeyDown: playStatus = "+playStatus);
                        Log.d(TAG, "onKeyDown: key OK failed!");
                    }
                    SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());
                }
            });

            holder.itemView.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    int selectPos = rvList.getChildAdapterPosition(view);
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN && selectPos != rvList.getCurPos()) {
                        rvList.setSelection(selectPos);
                        return true;
                    }
                    return false;
                }
            });
            // Johnny 20181219 for mouse control -e
        }

        @Override
        public int getItemCount() {
            return mFileList.size();
        }

        private String GetRecDate(PvrFileInfo info)
        {
            Log.d(TAG, "GetRecDate: hour = " + info.hour
                    + ", min = " + info.minute
                    + ", year = " + info.year
                    + ", month = " + info.month
                    + ", date = " + info.date );

            String hour;
            String min;
            String year;
            String month;
            String date;
            String zero = getString(R.string.STR_0);
            String space = getString(R.string.STR_SPACE);
            String sep = getString(R.string.STR_BOOK_ALARM_SEPARATE);
            String slash = getString(R.string.STR_SLASH);

            //PvrFileInfo info = PvrFileGetExtraInfo(filePath);
            hour = (info.hour < 10) ? zero+info.hour : String.valueOf(info.hour);
            hour += sep;
            min = (info.minute < 10) ? zero+info.minute : String.valueOf(info.minute);
            min += space;
            year = (info.year < 10) ? zero+info.year : String.valueOf(info.year);
            year += slash;
            month = (info.month < 10) ? zero+info.month : String.valueOf(info.month);
            month += slash;
            date = (info.date < 10) ? zero+info.date : String.valueOf(info.date);

            return hour + min + year + month + date;
        }

        private String FileSize( double fileSizeInByte ) { // edwin 20180810 format file size to 0.N MB
            Log.d(TAG, "FileSize: fileSizeInByte = " + fileSizeInByte);
            double sizeMB = fileSizeInByte/1048576;
            String fileSize;

            if (sizeMB == (long) sizeMB)
                fileSize = String.format( Locale.getDefault(), "%d", (long) sizeMB );
            else
                fileSize = String.format( Locale.getDefault(), "%.1f", sizeMB );
            fileSize = fileSize.concat(" MB");

            Log.d(TAG, "FileSize: fileSize = "+fileSize);

            return fileSize;
        }

        private String Time(int duration) {

            if (duration <= 0)
            {
                return "00:00:00";
            }

            String time = String.format(Locale.ENGLISH, "%02d:%02d:%02d",
                    (duration / 3600) % 24,
                    (duration / 60) % 60,
                    (duration % 60));

            Log.d(TAG, "Time: time = "+time);

            return time;
        }
    }

    private void ShowPasswordDialog(final String filePath)//eric lin 20180725 records list play and lock symbol
    {
        passwordDia = new PasswordDialogView(RecordListActivity.this, GposInfoGet().getPasswordValue(), //passwordDialog =new PasswordDialogView(ViewActivity.this, GposInfoGet().getPasswordValue(),
                PasswordDialogView.TYPE_PINCODE,0) {
            public void onCheckPasswordIsRight() {
                Log.d(TAG, ">>>>>PASSWORD IS RIGHT!<<<<<");
                mCurPosStatus = 0;
                mPlayPosStatus = 0;

                if(PvrPlayGetCurrentStatus() != INIT) //connie 20180806 for pvr parentalRate
                    PvrSetParentLockOK();
                else
                    playFile(filePath);
            }

            public void onCheckPasswordIsFalse() {
                Log.d(TAG, ">>>>>PASSWORD IS False!<<<<<");
                new MessageDialogView(RecordListActivity.this,
                        getString(R.string.STR_INVALID_PASSWORD), 3000) {
                    public void dialogEnd() {
                    }
                }.show();
            }

            public boolean onDealUpDownKey() {
                if(PvrPlayGetCurrentStatus() != INIT) { //connie 20180806 for pvr parentalRate
                    MediaStop();
                    SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());
                }
                return false;
            }
        };
    }

    public abstract class PvrDeleteDialog extends Dialog {

        private final String TAG = "PVRActivity.DeleteRecDialog";

        //private Context context;
        private TextView yes;
        private TextView no;
        private TextView msg;
        private String message;

        PvrDeleteDialog(@NonNull Context context, String message)
        {
            super(context, R.style.transparentDialog);
            //this.context = context;
            this.message = message;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.pvr_delete_dialog);

            msg = (TextView) findViewById(R.id.message_txv);
            yes = (TextView) findViewById(R.id.message_yes_txv);
            no = (TextView) findViewById(R.id.message_no_txv);

            msg.setText(message);

            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: YES");
                    OnClickYes();
                    dismiss();
                }
            });

            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: NO");
                    OnClickNo();
                    dismiss();
                }
            });
        }

        abstract void OnClickYes();
        abstract void OnClickNo();

    } // Confirm Dialog

    //    public abstract class PvrLastTimeDialog extends Dialog {
    //
    //        private final String TAG = "PVRActivity.PlayConfirmDialog";
    //
    //        private Context context;
    //        private TextView yes;
    //        private TextView no;
    //        private TextView msg;
    //        private String message;
    //        private String fullFile;
    //
    //        PvrLastTimeDialog(@NonNull Context context, String fullFile)
    //        {
    //            super(context, R.style.transparentDialog);
    //            this.context = context;
    //            this.message = getString(R.string.STR_DO_YOU_WANT_TO_PLAY_THE_FILE_FROM_LAST_TIME);
    //            this.fullFile = fullFile;
    //        }
    //
    //        @Override
    //        protected void onCreate(Bundle savedInstanceState) {
    //
    //            super.onCreate(savedInstanceState);
    //            setContentView(R.layout.pvr_delete_dialog);
    //
    //            msg = (TextView) findViewById(R.id.message_txv);
    //            yes = (TextView) findViewById(R.id.message_yes_txv);
    //            no = (TextView) findViewById(R.id.message_no_txv);
    //
    //            msg.setText(message);
    //
    //            yes.setOnClickListener(new View.OnClickListener() {
    //                @Override
    //                public void onClick(View v) {
    //
    //                    if ( ! isErrorFormat(fullFile) )
    //                    {
    //                        OnClickYes(fullFile);
    //                    }
    //                    dismiss();
    //                }
    //            });
    //
    //            no.setOnClickListener(new View.OnClickListener() {
    //                @Override
    //                public void onClick(View v) {
    //
    //                    if ( ! isErrorFormat(fullFile) )
    //                    {
    //                        OnClickNo(fullFile);
    //                    }
    //                    dismiss();
    //                }
    //            });
    //        }
    //
    //        private boolean isErrorFormat(String fullFile) {
    //
    //            Log.d(TAG, "isFormatError: ");
    //            boolean isFormatError = false;
    //
    //            if ( ! fullFile.equals("SVT1-20030709_14-43-15.ts") )
    //                isFormatError = true;
    //
    //            if ( isFormatError )
    //            {
    //                new FormatErrorDialog(context, 10).show();
    //            }
    //            return isFormatError;
    //        }
    //
    //        private class FormatErrorDialog extends Dialog {
    //
    //            String msg;
    //            int delay;
    //
    //            FormatErrorDialog(@NonNull Context context, int delay) {
    //                super(context, R.style.transparentDialog);
    //                setContentView(R.layout.message_dialog);
    //                msg = getString(R.string.STR_REC_MADE_IN_ANOTHER_RECEIVER);
    //                this.delay = delay*1000;
    //            }
    //
    //            @Override
    //            protected void onCreate(Bundle savedInstanceState) {
    //                super.onCreate(savedInstanceState);
    //                TextView msgTxv = (TextView) findViewById(R.id.contentTXV);
    //                TextView okBtn = (TextView) findViewById(R.id.yesBTN);
    //                msgTxv.setText(msg);
    //                okBtn.setOnClickListener(new View.OnClickListener() {
    //                    @Override
    //                    public void onClick(View v) {
    //                        dismiss();
    //                    }
    //                });
    //            }
    //
    //            @Override
    //            public void show() {
    //                super.show();
    //                Handler handler = new Handler();
    //                handler.postDelayed(new Runnable() {
    //                    @Override
    //                    public void run() {
    //                        dismiss();
    //                    }
    //                }, delay);
    //            }
    //        }
    //
    //        abstract void OnClickYes(String record);
    //        abstract void OnClickNo(String record);
    //
    //    } // Confirm Dialog
    //    public abstract class PvrLastTimeDialog extends Dialog {
    //
    //        private final String TAG = "PVRActivity.PlayConfirmDialog";
    //
    //        private Context context;
    //        private TextView yes;
    //        private TextView no;
    //        private TextView msg;
    //        private String message;
    //        private String fullFile;
    //
    //        PvrLastTimeDialog(@NonNull Context context, String fullFile)
    //        {
    //            super(context, R.style.transparentDialog);
    //            this.context = context;
    //            this.message = getString(R.string.STR_DO_YOU_WANT_TO_PLAY_THE_FILE_FROM_LAST_TIME);
    //            this.fullFile = fullFile;
    //        }
    //
    //        @Override
    //        protected void onCreate(Bundle savedInstanceState) {
    //
    //            super.onCreate(savedInstanceState);
    //            setContentView(R.layout.pvr_delete_dialog);
    //
    //            msg = (TextView) findViewById(R.id.message_txv);
    //            yes = (TextView) findViewById(R.id.message_yes_txv);
    //            no = (TextView) findViewById(R.id.message_no_txv);
    //
    //            msg.setText(message);
    //
    //            yes.setOnClickListener(new View.OnClickListener() {
    //                @Override
    //                public void onClick(View v) {
    //
    //                    if ( ! isErrorFormat(fullFile) )
    //                    {
    //                        OnClickYes(fullFile);
    //                    }
    //                    dismiss();
    //                }
    //            });
    //
    //            no.setOnClickListener(new View.OnClickListener() {
    //                @Override
    //                public void onClick(View v) {
    //
    //                    if ( ! isErrorFormat(fullFile) )
    //                    {
    //                        OnClickNo(fullFile);
    //                    }
    //                    dismiss();
    //                }
    //            });
    //        }
    //
    //        private boolean isErrorFormat(String fullFile) {
    //
    //            Log.d(TAG, "isFormatError: ");
    //            boolean isFormatError = false;
    //
    //            if ( ! fullFile.equals("SVT1-20030709_14-43-15.ts") )
    //                isFormatError = true;
    //
    //            if ( isFormatError )
    //            {
    //                new FormatErrorDialog(context, 10).show();
    //            }
    //            return isFormatError;
    //        }
    //
    //        private class FormatErrorDialog extends Dialog {
    //
    //            String msg;
    //            int delay;
    //
    //            FormatErrorDialog(@NonNull Context context, int delay) {
    //                super(context, R.style.transparentDialog);
    //                setContentView(R.layout.message_dialog);
    //                msg = getString(R.string.STR_REC_MADE_IN_ANOTHER_RECEIVER);
    //                this.delay = delay*1000;
    //            }
    //
    //            @Override
    //            protected void onCreate(Bundle savedInstanceState) {
    //                super.onCreate(savedInstanceState);
    //                TextView msgTxv = (TextView) findViewById(R.id.contentTXV);
    //                TextView okBtn = (TextView) findViewById(R.id.yesBTN);
    //                msgTxv.setText(msg);
    //                okBtn.setOnClickListener(new View.OnClickListener() {
    //                    @Override
    //                    public void onClick(View v) {
    //                        dismiss();
    //                    }
    //                });
    //            }
    //
    //            @Override
    //            public void show() {
    //                super.show();
    //                Handler handler = new Handler();
    //                handler.postDelayed(new Runnable() {
    //                    @Override
    //                    public void run() {
    //                        dismiss();
    //                    }
    //                }, delay);
    //            }
    //        }
    //
    //        abstract void OnClickYes(String record);
    //        abstract void OnClickNo(String record);
    //
    //    } // Confirm Dialog

    // Johnny 20181228 for mouse control -s
    private void setControlPanelClickListener() {
        pvrPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int playStatus = PvrPlayGetCurrentStatus();
                switch (playStatus) {
                    case INIT:
                    case PAUSE:
                    case FAST_FORWARD:
                    case FAST_BACKWARD:
                        MediaResume();
                        SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());
                        break;
                }
            }
        });

        pvrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int playStatus = PvrPlayGetCurrentStatus();

                switch (playStatus) {
                    case PLAY:
                    case FAST_FORWARD:
                    case FAST_BACKWARD:
                        MediaPause();
                        SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());
                        break;
                }
            }
        });

        pvrStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaStop();
                SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());
            }
        });

        pvrForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EnTrickMode trickMode = PvrPlayGetCurrentTrickMode();
                MediaPlayTrick(trickMode, KEYCODE_MEDIA_FAST_FORWARD);
                SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());
            }
        });

        pvrRewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EnTrickMode trickMode = PvrPlayGetCurrentTrickMode();
                MediaPlayTrick(trickMode, KEYCODE_MEDIA_REWIND);
                SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());
            }
        });

        pvrNextFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rvList.moveDown();
                MediaPlayNext();
            }
        });

        pvrPreFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rvList.moveUp();
                MediaPlayPre();
            }
        });
    }
    // Johnny 20181228 for mouse control -e
}
