package com.prime.dtvplayer.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dolphin.dtv.EnPVRPlayStatus;
import com.dolphin.dtv.EnTableType;
import com.dolphin.dtv.EnTrickMode;
import com.dolphin.dtv.PvrFileInfo;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.AudioInfo;
import com.prime.dtvplayer.Sysdata.EnAudioTrackMode;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.SubtitleInfo;
import com.prime.dtvplayer.Sysdata.TeletextInfo;
import com.prime.dtvplayer.View.ActivityHelpView;
import com.prime.dtvplayer.View.ActivityTitleView;
import com.prime.dtvplayer.View.AudioDialogView;
import com.prime.dtvplayer.View.GridRecyclerView;
import com.prime.dtvplayer.View.MessageDialogView;
import com.prime.dtvplayer.View.PasswordDialogView;
import com.prime.dtvplayer.View.PvrBannerView;
import com.prime.dtvplayer.View.SubtitleDialogView;
import com.prime.dtvplayer.View.TeletextDialogView;
import com.prime.dtvplayer.utils.TVMessage;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_CAPTIONS;
import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
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

public class RecordGridActivity extends DTVActivity {

    private static final String TAG = "RecordGridActivity";
    private final Context mContext = RecordGridActivity.this;//eric lin 20180720 add file play audio dialog
    private ActivityTitleView actTitle;
    private ActivityHelpView actHelp;
    private GridRecyclerView rvList;
    //private TextView txvRateTag;
    //private TextView txvProgName;
    //private TextView txvFileSize, txvFileSizeTag;
    private TextView txvTime, txvTimeTag;
    private TextView txvDate, txvDateTag;
    private TextView txvRate;
    private TextView txvRatePlus;
    private TextView txvSpeed;
    private TextView txvEmptyFile;
    private ConstraintLayout screen;
    private ConstraintLayout actLayout;
    private LinearLayout recListLayout;//eric lin 20180720 add file play audio dialog
    private SurfaceView surfaceView;
    private PvrBannerView progressBanner;
    private PasswordDialogView passwordDialog = null;

    private final boolean FULL_SCREEN = true;
    private final boolean NORMAL_SCREEN = false;
    private final int PLAY_NEXT_FILE = 0;
    private final int PLAY_PREV_FILE = 1;
    private final int LOADING_FINISH = 0;
    private final int LOADING_FAIL = -1;
    private final int POINT_ZERO = 0;
    private final int POINT_LAST_TIME = 1;

    private List<PvrFileInfo> mFileList;
    private String mPlayingFile;
    private int mPlayingFileDuration = 0;    // Johnny 20180802 fix calling pvrFileGetDuration too often
    private int mDelay;

    //below two flags control play symbol showing
    //private int mPrePlayPos=-1, mCurPlayPos=-1;
    private boolean parentalLock = false;
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
    private Handler hdrProgress = new Handler();
    private Runnable updateProgress = new Runnable() {
        @Override
        public void run() {

            Log.d(TAG, "run: updateProgress");

            // Video Stop
            if (PvrPlayGetCurrentStatus() == EnPVRPlayStatus.INIT)
            {
                return;
            }

            // Update Time
            int playTime = PvrPlayGetPlayTime();

            progressBanner.updateTimeStatus(RecordGridActivity.this, mPlayingFileDuration,false);//Scoty 20180802 fixed cur time not update when focus on Pip   // Johnny 20180802 fix calling pvrFileGetDuration too often

            if (playTime <= mPlayingFileDuration)
            {
                hdrProgress.postDelayed(updateProgress, 1000);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.pvr_rec_grid);

        Log.d(TAG, "onCreate: ");

        CheckRecordPath(GetRecordPath());
        InitViewById();
        RvInit();
        AvControlPlayStop(ViewHistory.getPlayId());
        setSurfaceView(surfaceView);
        mDtv = this;
        mDelay = getResources().getInteger( R.integer.MESSAGE_DIALOG_DELAY );
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

                if (IsFullScreen())
                {
                    SetScreen(NORMAL_SCREEN);
                    progressBanner.setVisibility(INVISIBLE);
                }
                break;
            }

            case TVMessage.TYPE_PVR_PLAY_PARENTAL_LOCK: //connie 20180806 for pvr parentalRate
            {
                if(passwordDialog!=null && passwordDialog.isShowing())
                    break;
                parentalLock = true;
                ShowPasswordDialog(mPlayingFile);

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
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        int playStatus = PvrPlayGetCurrentStatus();
        EnTrickMode trickMode = PvrPlayGetCurrentTrickMode();

        Log.d(TAG, "onKeyDown: playStatus = "+playStatus);
        Log.d(TAG, "onKeyDown: keyCode = "+keyCode);

        if (HasNoFile(mFileList))
        {
            return super.onKeyDown(keyCode, event);
        }

        switch (keyCode) {

            case KEYCODE_DPAD_CENTER:
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

            case KEYCODE_BACK:
                if (IsFullScreen())
                {
                    MediaStop();
                    SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());
                    return true;
                }
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
                if (!IsFullScreen())
                {
                    boolean isScroll = rvList.moveDown();
                    MediaPlayNext();
                    return isScroll;
                }
                break;

            case KEYCODE_MEDIA_PREVIOUS:
                //case KEYCODE_DPAD_LEFT:
                if (!IsFullScreen())
                {
                    boolean isScroll = rvList.moveUp();
                    MediaPlayPre();
                    return isScroll;
                }
                break;

            case KEYCODE_DPAD_UP:
                if (!IsFullScreen())
                {
                    return rvList.moveUp();
                }
                break;

            case KEYCODE_DPAD_DOWN:
                if (!IsFullScreen())
                {
                    return rvList.moveDown();
                }
                break;

            case KEYCODE_DPAD_LEFT:
                if (!IsFullScreen())
                {
                    return rvList.moveLeft();
                }
                break;

            case KEYCODE_DPAD_RIGHT:
                if (!IsFullScreen())
                {
                    return rvList.moveRight();
                }
                break;

            case KEYCODE_PAGE_UP:
                if ( !IsFullScreen() )
                {
                    rvList.moveNextPage();
                }
                break;

            case KEYCODE_PAGE_DOWN:
                if ( !IsFullScreen() )
                {
                    rvList.movePrevPage();
                }
                break;

            case KEYCODE_INFO:
            case ExtKeyboardDefine.KEYCODE_INFO:    // Johnny 20181210 for keyboard control
                if (IsFullScreen())
                {
                    ShowBanner();
                }
                break;

            case KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                if (!IsFullScreen())
                {
                    DeleteRec();
                    SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());
                }
                break;

            case KEYCODE_MEDIA_AUDIO_TRACK:
            case ExtKeyboardDefine.KEYCODE_MEDIA_AUDIO_TRACK:   // Johnny 20181210 for keyboard control
                if ( IsFullScreen() )
                {
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
                break;

            case KEYCODE_CAPTIONS: // subtitle key
            case ExtKeyboardDefine.KEYCODE_CAPTIONS:    // Johnny 20181210 for keyboard control
                if ( IsFullScreen() )
                {
                    final SubtitleInfo Subtitle = AvControlGetSubtitleList( ViewHistory.getPlayId() );
                    if ( Subtitle == null )
                    {
                        new MessageDialogView( mContext,
                                getString( R.string.STR_NO_SUBTITLE ),
                                mDelay )
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
                if ( IsFullScreen() )
                {
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
            new MessageDialogView(mContext,
                    getString(R.string.STR_TELETEXT_IS_NOT_AVAILABLE),
                    mDelay)
            {
                public void dialogEnd() {
                }
            }.show();
        }
    }

    private void MediaPlay ( final String filePath )
    {
        Log.d( TAG, "MediaPlay: " );

        this.mPlayingFile = filePath;
        this.mPlayingFileDuration = PvrFileGetDuration( filePath );

        if ( parentalLock )
        {
            ShowPasswordDialog( filePath );
        }
        else
        {
            CheckLastPoint( filePath );
        }
    }

    private void CheckLastPoint ( final String filePath )
    {
        Log.d( TAG, "CheckLastPoint: " );

        int point = PvrPlayFileCheckLastViewPoint( filePath );
        if ( point == POINT_LAST_TIME )
        {
            Log.d( TAG, "CheckLastPoint: IsFullScreen() = "+IsFullScreen() );
            new SureDialog( this, getString( R.string.STR_DO_YOU_WANT_TO_PLAY_THE_FILE_FROM_THE_LAST_STOP_TIME ) )
            {
                @Override
                void OnClickYes ()
                {
                    PvrSetStartPositionFlag( POINT_LAST_TIME );
                    PlayFile( filePath );
                    SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());//Scoty 20180815 modify play icon show wrong
                }

                @Override
                void OnClickNo ()
                {
                    PvrSetStartPositionFlag( POINT_ZERO );
                    PlayFile( filePath );
                    SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());//Scoty 20180815 modify play icon show wrong
                }
            }.show();
        }
        else
        {
            PlayFile( filePath );
        }
    }

    private void PlayFile ( String filePath ) //eric lin 20180720 play last stop time
    {
        Log.d( TAG, "PlayFile: " );

        SetScreen(FULL_SCREEN);

        ////eric lin 20180725 records list play and lock symbol,-start
        //mCurPlayPos = rvList.getCurPos();
        //if ( mPrePlayPos != -1 )
        //{
        //    if ( mPrePlayPos != mCurPlayPos ) //clear prePlayPos's playSymbol
        //    {
        //        rvList.getAdapter().notifyItemChanged( mPrePlayPos );
        //        mPrePlayPos = mCurPlayPos;
        //    }
        //}
        //else
        //{
        //    mPrePlayPos = mCurPlayPos;
        //}
        //rvList.getAdapter().notifyItemChanged( mCurPlayPos );//show curPlayPos's playSymbol
        ////eric lin 20180725 records list play and lock symbol,-end

        PvrPlayStart( filePath );
        hdrProgress.post(updateProgress);
    }

    private void MediaPause()
    {
        Log.d(TAG, "MediaPause: ");

        if (CurrentFilePath().equals(mPlayingFile)) // same file
        {
            PvrPlayPause();

            if (IsFullScreen())
            {
                progressBanner.removeCallbacks(hideBanner); // edwin 20180622 banner disappear
                progressBanner.setVisibility(this, VISIBLE, mPlayingFileDuration,false);//Scoty 20180802 fixed cur time not update when focus on Pip // Johnny 20180802 fix calling pvrFileGetDuration too often
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

        if (CurrentFilePath().equals(mPlayingFile)) // same file
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

        mPlayingFile = "";
        mPlayingFileDuration = 0;    // Johnny 20180802 fix calling pvrFileGetDuration too often
        progressBanner.setVisibility(INVISIBLE);
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
                || keyCode == ExtKeyboardDefine.KEYCODE_MEDIA_FAST_FORWARD) // Johnny 20181210 for keyboard control
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
            progressBanner.setVisibility(this, VISIBLE, mPlayingFileDuration,false);//Scoty 20180802 fixed cur time not update when focus on Pip // Johnny 20180802 fix calling pvrFileGetDuration too often
            progressBanner.updateTimeStatus(RecordGridActivity.this, PvrPlayGetDuration(),false);//Scoty 20180802 fixed cur time not update when focus on Pip
        }
    }

    private void DeleteRec() {

        Log.d(TAG, "DeleteRec: ");

        String msg = getString(R.string.STR_DO_YOU_WANT_TO_DELETE_THE_RECORDING);

        SureDialog deleteDialog = new SureDialog(this, msg) {
            @Override
            void OnClickYes()
            {
                Log.d(TAG, "DeleteRec: OnClickYes: Delete Record Pos = "+rvList.getCurPos());

                String filePath = CurrentFilePath();

                if (filePath.equals(""))
                {
                    return;
                }

                if (filePath.equals(mPlayingFile)) // edwin 20180621 fix delete when playing file
                {
                    PvrPlayStop();
                    SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());
                }

                DeleteFile(filePath);
            }

            @Override
            void OnClickNo() {
                dismiss();
            }
        };

        if ( ! CurrentFilePath().equals("") )
        {
            deleteDialog.show();
        }
    }

    private void DeleteFile( final String filePath )
    {
        Log.d(TAG, "DeleteFile: ");

        rvList.post( new Runnable()
        {
            @Override
            public void run ()
            {
                PvrFileRemove(filePath);
            }
        } );
        rvList.remove( mFileList, rvList.getCurPos() );

        if (mFileList.size() == 0)
        {
            txvEmptyFile.setVisibility(VISIBLE);
            rvList.setVisibility(INVISIBLE);
            txvTimeTag.setVisibility(INVISIBLE);
            txvTime.setVisibility(INVISIBLE);
            txvDateTag.setVisibility(INVISIBLE);
            txvDate.setVisibility(INVISIBLE);
            txvRate.setVisibility(INVISIBLE);
            txvRatePlus.setVisibility( INVISIBLE );
            //txvFileSize.setText(R.string.STR_0MB);
            //txvRateTag.setVisibility(INVISIBLE);
            //txvProgName.setVisibility(INVISIBLE);
        }
    }

    private String CurrentFilePath()
    {
        String dir = GetRecordPath().concat( getString(R.string.STR_RECORD_DIR) );
        String currentFile = dir + mFileList.get( rvList.getCurPos() ).realFileName;

        Log.d( TAG, "CurrentFilePath: "+currentFile );

        return currentFile;
    }

    private void SetScreen(boolean setFullScreen)
    {
        boolean isSmallScreen = ! IsFullScreen();
        int loc[] = new int[2];
        screen.getLocationOnScreen(loc);

        if (setFullScreen)
        {
            Log.d(TAG, "SetScreen: set full screen");

            SetALL(INVISIBLE);

            if ( isSmallScreen )
            {
                ShowBanner();
            }
        }
        else
        {
            Log.d(TAG, "SetFullScreen: set small screen");

            if ( mAudioDialogView != null && mAudioDialogView.isShowing() )
            {
                mAudioDialogView.dismiss();
            }

            SetALL(VISIBLE);
            rvList.setSelection(rvList.getCurPos());
            progressBanner.setVisibility(INVISIBLE);
        }
    }

    private void SetALL(int visibility)
    {
        Log.d(TAG, "SetALL: ");

        if (visibility == INVISIBLE)
        {
            actLayout.setBackgroundColor(Color.TRANSPARENT);
        }
        else
        {
            actLayout.setBackgroundResource(R.drawable.bg);
        }

        recListLayout.setVisibility(visibility);//eric lin 20180720 add file play audio dialog
        rvList.setVisibility(visibility);
        txvDate.setVisibility(visibility);
        txvTime.setVisibility(visibility);
        txvRate.setVisibility(visibility);
        txvRatePlus.setVisibility(visibility);
        txvSpeed.setVisibility(visibility);
        actTitle.setVisibility(visibility);
        actHelp.setVisibility(visibility);
        txvTimeTag.setVisibility(visibility);
        txvDateTag.setVisibility(visibility);
        //txvFileSize.setVisibility(visibility);
        //txvFileSizeTag.setVisibility(visibility);
        //txvProgName.setVisibility(visibility);
        //txvRateTag.setVisibility(visibility);
    }

    private void SetPlayStatus(int playStatus, EnTrickMode trickMode)
    {
        Log.d(TAG, "SetPlayStatus: playStatus = "+playStatus+" trickMode = "+trickMode);

        if (playStatus == PLAY)                 Log.d(TAG, "SetPlayStatus: playStatus = "+playStatus);//pvrPlay.setImageResource(R.drawable.pvr_play_f);
        else if (playStatus == PAUSE)           Log.d(TAG, "SetPlayStatus: playStatus = "+playStatus);//pvrPause.setImageResource(R.drawable.pvr_pause_f);
        else if (playStatus == FAST_FORWARD)    Log.d(TAG, "SetPlayStatus: playStatus = "+playStatus);//pvrForward.setImageResource(R.drawable.pvr_foward_f);
        else if (playStatus == FAST_BACKWARD)   Log.d(TAG, "SetPlayStatus: playStatus = "+playStatus);//pvrRewind.setImageResource(R.drawable.pvr_backward_f);
        else
        {
            Log.d(TAG, "SetPlayStatus: playStatus = "+playStatus);
            //pvrStop.setImageResource(R.drawable.pvr_stop_f);
            //ResetProgress();
        }
        // playStatus == INIT || playStatus == NOT_INIT || playStatus == INVALID || playStatus == STOP

        txvSpeed.setVisibility(VISIBLE);

        if (trickMode == FAST_FORWARD_TWO)              txvSpeed.setText(getString(R.string.STR_2X_FF));
        else if (trickMode == FAST_FORWARD_FOUR)        txvSpeed.setText(getString(R.string.STR_4X_FF));
        else if (trickMode == FAST_FORWARD_EIGHT)       txvSpeed.setText(getString(R.string.STR_8X_FF));
        else if (trickMode == FAST_FORWARD_SIXTEEN)     txvSpeed.setText(getString(R.string.STR_16X_FF));
        else if (trickMode == FAST_FORWARD_THIRTYTWO)   txvSpeed.setText(getString(R.string.STR_32X_FF));
        else if (trickMode == FAST_BACKWARD_TWO)        txvSpeed.setText(getString(R.string.STR_2X_BF));
        else if (trickMode == FAST_BACKWARD_FOUR)       txvSpeed.setText(getString(R.string.STR_4X_BF));
        else if (trickMode == FAST_BACKWARD_EIGHT)      txvSpeed.setText(getString(R.string.STR_8X_BF));
        else if (trickMode == FAST_BACKWARD_SIXTEEN)    txvSpeed.setText(getString(R.string.STR_16X_BF));
        else if (trickMode == FAST_BACKWARD_THIRTYTWO)  txvSpeed.setText(getString(R.string.STR_32X_BF));
        else                                            txvSpeed.setText(null);
    }

    private void ShowBanner()
    {
        Log.d(TAG, "ShowBanner: ");

        progressBanner.removeCallbacks(hideBanner);
        progressBanner.setVisibility(this, VISIBLE, mPlayingFileDuration,false);//Scoty 20180802 fixed cur time not update when focus on Pip // Johnny 20180802 fix calling pvrFileGetDuration too often
        progressBanner.postDelayed(hideBanner, bannerTimeout);
    }

    private void ShowPlayStatus(int playStatus)
    {
        Log.d(TAG, "ShowPlayStatus: ");

        Handler hdrStatus = new Handler();

        switch (playStatus)
        {
            case PLAY_NEXT_FILE:
                //pvrNextFile.setImageResource(R.drawable.pvr_next_file_f);
                break;

            case PLAY_PREV_FILE:
                //pvrPreFile.setImageResource(R.drawable.pvr_previous_file_f);
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

    private void InitViewById() {

        Log.d(TAG, "InitViewById: ");

        actTitle        = (ActivityTitleView) findViewById(R.id.pvrTitle);
        actHelp         = (ActivityHelpView) findViewById(R.id.pvrHelp);
        screen          = (ConstraintLayout) findViewById(R.id.pvr_video_frame);
        actLayout       = (ConstraintLayout) findViewById(R.id.pvr_main);
        recListLayout   = (LinearLayout) findViewById(R.id.rec_list_layout);
        rvList          = (GridRecyclerView) findViewById(R.id.pvr_rec_list);
        progressBanner  = (PvrBannerView) findViewById(R.id.pvr_banner);
        surfaceView     = (SurfaceView) findViewById(R.id.pvr_video);
        txvDate         = (TextView) findViewById(R.id.rec_date);
        txvDateTag      = (TextView) findViewById(R.id.rec_date_str);
        txvTime         = (TextView) findViewById(R.id.pvr_time);
        txvTimeTag      = (TextView) findViewById(R.id.pvr_time_str);
        txvRate         = (TextView) findViewById(R.id.rateTXV);
        txvRatePlus     = (TextView) findViewById(R.id.plus);
        txvSpeed        = (TextView) findViewById(R.id.pvr_speed);
        txvEmptyFile    = (TextView) findViewById(R.id.empty_file_list);
        //txvRateTag      = (TextView) findViewById(R.id.partentTXV);
        //txvProgName     = (TextView) findViewById(R.id.pvr_program_name);
        //txvFileSize     = (TextView) findViewById(R.id.file_size);
        //txvFileSizeTag  = (TextView) findViewById(R.id.file_size_str);

        // Title, Help
        actTitle.setTitleView(getString(R.string.STR_RECORDS_LIST));
        actHelp.setHelpInfoTextBySplit(getString(R.string.STR_OK_TO_PLAY_RECORD));
        actHelp.resetHelp(1, R.drawable.help_red, getString(R.string.STR_DELETE));
        actHelp.resetHelp(2, 0, null);
        actHelp.resetHelp(3, 0, null);
        actHelp.resetHelp(4, 0, null);

        actHelp.setHelpIconClickListener(1, new View.OnClickListener() {    // Johnny 20181228 for mouse control
            @Override
            public void onClick(View view) {
                DeleteRec();
                SetPlayStatus(PvrPlayGetCurrentStatus(), PvrPlayGetCurrentTrickMode());
            }
        });

        // Banner
        progressBanner.setVisibility(INVISIBLE);
        bannerTimeout = GposInfoGet().getBannerTimeout() * 1000;
    }

    private void RvInit()
    {
        Log.d(TAG, "InitRecList: ");

        final Handler hdrLoadRec = new InitRvHandler(this);

        new Thread( new Runnable()
        {
            @Override
            public void run ()
            {
                mFileList = GetFileList();
                for ( PvrFileInfo info : mFileList )
                {
                    Log.d( TAG, "run: info.fileName = "+info.channelName+
                            " info.realFileName = "+info.realFileName );
                }

                if ( HasNoFile(mFileList) )
                {
                    Log.d( TAG, "run: send message(LOADING_FAIL) to handler(hdrLoadRec)" );
                    hdrLoadRec.sendEmptyMessage( LOADING_FAIL );
                    return;
                }

                Log.d( TAG, "run: send message(LOADING_FINISH) to handler(hdrLoadRec)" );
                hdrLoadRec.sendEmptyMessage( LOADING_FINISH );
            }
        } ).start();
    }

    private void RvMsgHandle( Message msg ) // edwin 20180816 fix handler warning
    {
        Log.d( TAG, "InitRvHandle: " );

        switch ( msg.what )
        {
            case LOADING_FINISH:
                rvList.post( new Runnable()
                {
                    @Override
                    public void run ()
                    {
                        rvList.setAdapter( new RecAdapter( mFileList ) );
                    }
                } );
                break;

            case LOADING_FAIL:
                break;
        }

        ProgressBar prgListLoading = (ProgressBar) findViewById( R.id.pvr_rec_list_prgLoading );
        TextView txvListLoading = (TextView) findViewById( R.id.pvr_rec_list_txvLoading );
        prgListLoading.setVisibility( GONE );
        txvListLoading.setVisibility( GONE );
    }

    private List<PvrFileInfo> GetFileList()
    {
        String dir = GetRecordPath() + getString(R.string.STR_RECORD_DIR);
        int total = PvrTotalRecordFileOpen( dir );

        Log.d( TAG, "GetFileList: total = "+total );

        PvrTotalRecordFileSort( 0 ); // PVR_SORT_BY_CHNAME = 0
        List<PvrFileInfo> list = PvrTotalRecordFileGet( 0, total );

        for ( PvrFileInfo info : list )
        {
            //Log.d( TAG, "GetFileList:" +
            //        " filePath = "+dir + info.realFileName+
            //        " fileSize = "+PvrFileGetSize( dir + info.realFileName ) );
            if ( PvrFileGetSize( dir + info.realFileName ) == 0 )
            {
                list.remove( info );
            }
        }

        return list;
    }

    //private void SetListNum(final GridRecyclerView targetView, final View topView, final View bottomView)
    //{
    //    Log.d(TAG, "SetListNum: ");
    //
    //    int locBottom[] = new int[2];
    //    int locTop[] = new int[2];
    //    int rvItemHeight = (int) getResources().getDimension(R.dimen.PVR_LIST_ITEM_HEIGHT);
    //
    //    if (targetView.getChildAt(0) != null) // edwin 20180704 avoid null item view
    //    {
    //        rvItemHeight = targetView.getChildAt(0).getMeasuredHeight();
    //    }
    //
    //    bottomView.getLocationOnScreen(locBottom);
    //    topView.getLocationOnScreen(locTop);
    //
    //    int rvHeight = locBottom[1] - locTop[1] - topView.getMeasuredHeight();
    //
    //    Log.d(TAG, "run: rvHeight = "+rvHeight+
    //            ", rvItemHeight = "+rvItemHeight+
    //            ", rvHeight/rvItemHeight = "+rvHeight/rvItemHeight);
    //
    //    targetView.setListNum(rvHeight/rvItemHeight);
    //}

    private boolean IsFullScreen()
    {
        Log.d(TAG, "IsFullScreen: "+(actTitle.getVisibility() == INVISIBLE));

        if (actTitle.getVisibility() == INVISIBLE)
            return FULL_SCREEN;
        else
            return NORMAL_SCREEN;
    }

    private boolean HasNoFile(List<PvrFileInfo> files)
    {
        boolean empty = ( files == null ) || ( files.size() == 0 );

        if (empty)
        {
            txvEmptyFile.setVisibility(VISIBLE);
            rvList.setVisibility(INVISIBLE);
            txvTimeTag.setVisibility(INVISIBLE);
            txvTime.setVisibility(INVISIBLE);
            txvDateTag.setVisibility(INVISIBLE);
            txvDate.setVisibility(INVISIBLE);
            txvRate.setVisibility(INVISIBLE);
            txvRatePlus.setVisibility(INVISIBLE);
            //txvRateTag.setVisibility(INVISIBLE);
            //txvProgName.setVisibility(INVISIBLE);
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
                new MessageDialogView(this,
                        getString(R.string.STR_RECORD_PATH_NOT_AVAILABLE),
                        mDelay) // edwin 20170626 change meaning
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
        final WeakReference<RecordGridActivity> mActivity;

        InitRvHandler ( RecordGridActivity activity )
        {
            mActivity = new WeakReference<>( activity );
        }

        @Override
        public void handleMessage ( Message msg )
        {
            RecordGridActivity activity = mActivity.get();
            if ( activity != null )
            {
                Log.d( TAG, "handleMessage: wait Thread's message(LOADING_FINISH)" );

                activity.RvMsgHandle( msg );
                super.handleMessage( msg );
            }
        }
    }

    private class RecAdapter extends RecyclerView.Adapter<RecAdapter.ViewHolder> {

        List<PvrFileInfo> mFileList;
        //Drawable img1;
        //Drawable img2;
        //Drawable img3;
        //Drawable img4;
        //Drawable img5;
        ValueAnimator animator;

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView recImg;
            TextView recName;

            public ViewHolder(View itemView) {
                super(itemView);
                recImg = (ImageView) itemView.findViewById(R.id.rec_img);
                recName = (TextView) itemView.findViewById( R.id.rec_name );
            }
        }

        RecAdapter(final List<PvrFileInfo> list) {
            this.mFileList = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.pvr_rec_grid_item, parent, false);

            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder ( final ViewHolder holder, int position )
        {
            PvrFileInfo curFileInfo = mFileList.get( position );
            float down = 0.9f; 
            
            holder.itemView.animate().scaleX( down ).scaleY( down ).setDuration( 0 ).start();
            holder.itemView.setOnFocusChangeListener( onFocusChangeInfo( holder ) );
            holder.recName.setText( curFileInfo.channelName );
            //holder.recName.animate().scaleX( down ).scaleY( down ).setDuration( 0 ).start();
            holder.recImg.animate().scaleX( down ).scaleY( down ).setDuration( 0 ).start();
        }

        @Override
        public int getItemCount() {
            return mFileList.size();
        }

        private View.OnFocusChangeListener onFocusChangeInfo( final ViewHolder holder)
        {
            final int time = 100;
            final float down = 0.9f;
            final float up = 1.1f;
            
            return new View.OnFocusChangeListener()
            {
                @Override
                public void onFocusChange ( View v, boolean hasFocus )
                {
                    //img1 = ContextCompat.getDrawable( mContext, R.drawable.img1 );
                    //img2 = ContextCompat.getDrawable( mContext, R.drawable.img2 );
                    //img3 = ContextCompat.getDrawable( mContext, R.drawable.img3 );
                    //img4 = ContextCompat.getDrawable( mContext, R.drawable.img4 );
                    //img5 = ContextCompat.getDrawable( mContext, R.drawable.img5 );

                    if ( ! hasFocus )
                    {
                        Log.d( TAG, "onFocusChange: ! hasFocus" );
                        
                        holder.itemView.animate().scaleX( down ).scaleY( down ).setDuration( time ).start();
                        holder.recImg.animate().scaleX( down ).scaleY( down ).setDuration( time ).start();
                        holder.recName.animate().scaleX( down ).scaleY( down ).setDuration( time ).start();
                        //holder.recName.setTextScaleX( down );
                        holder.recName.setSelected( false );
                        //holder.recName.setTypeface( Typeface.defaultFromStyle( Typeface.NORMAL ) );
                        animator.end();
                        return;
                    }

                    Log.d( TAG, "onFocusChange: Change File Info Listener" );
                    
                    holder.itemView.animate().scaleX( up ).scaleY( up ).setDuration( time ).start();
                    holder.recImg.animate().scaleX( up ).scaleY( up ).setDuration( time ).start();
                    holder.recName.animate().scaleX( up ).scaleY( up ).setDuration( time ).start();
                    //holder.recName.setTextScaleX( 1 );
                    holder.recName.setSelected( true );
                    //holder.recName.setTypeface( Typeface.defaultFromStyle( Typeface.BOLD ) );

                    //animator = ObjectAnimator.ofFloat( holder.recImg, "rotation", 0 );
                    animator = ValueAnimator.ofInt( 0, 4 );
                    animator.setRepeatCount( ObjectAnimator.INFINITE );
                    animator.setRepeatMode( ObjectAnimator.RESTART );
                    //animator.setInterpolator( new AccelerateInterpolator( 2.5f ) );
                    animator.setDuration( 1000 );
                    animator.start();
                    animator.addUpdateListener( new ValueAnimator.AnimatorUpdateListener()
                    {
                        @Override
                        public void onAnimationUpdate ( ValueAnimator animation )
                        {
                            int value = Integer.valueOf( animation.getAnimatedValue().toString() );
                            Log.d( TAG, "onAnimationUpdate: "+animation.getAnimatedValue() );
                            //if ( value == 0 )
                            //    holder.recImg.setBackground( img1 );
                            //else if ( value == 1 )
                            //    holder.recImg.setBackground( img2 );
                            //else if ( value == 2 )
                            //    holder.recImg.setBackground( img3 );
                            //else if ( value == 3 )
                            //    holder.recImg.setBackground( img4 );
                            //else if ( value == 4 )
                            //    holder.recImg.setBackground( img5 );
                        }
                    } );
                    animator.addListener( new AnimatorListenerAdapter()
                    {
                        @Override
                        public void onAnimationEnd ( Animator animation )
                        {
                            super.onAnimationEnd( animation );
                            //holder.recImg.setBackground( img1 );
                        }
                    } );

                    String filePath = CurrentFilePath();
                    PvrFileInfo fileInfo = PvrFileGetAllInfo( filePath );
                    PvrFileInfo extraInfo = PvrFileGetExtraInfo( filePath );

                    SetTextViewInfo( FileSize(),
                            GetRecDate( extraInfo ),
                            extraInfo.channelName,
                            GetTime( (fileInfo.durationInMs / 1000) ),
                            String.valueOf( fileInfo.parentalRate ) );
                }
            };
        }

        private void SetTextViewInfo(String fileSize, String date, String chName, String time, String rate)
        {
            Log.d( TAG, "SetTextViewInfo:" +
                    " fileSize = "+fileSize+
                    " date = "+date+
                    " chName = "+chName+
                    " time = "+time+
                    " rate = "+rate );

            txvDate.setText( date );
            txvTime.setText( time );
            txvRate.setText( rate );
            //txvProgName.setText( chName );
            //txvFileSize.setText( fileSize );
        }
        
        private String GetRecDate(PvrFileInfo extraInfo)
        {
            Log.d(TAG, "GetRecDate: ");

            String hour;
            String min;
            String year;
            String month;
            String date;
            String zero = getString(R.string.STR_0);
            String space = getString(R.string.STR_SPACE);
            String sep = getString(R.string.STR_BOOK_ALARM_SEPARATE);
            String slash = getString(R.string.STR_SLASH);

            hour = (extraInfo.hour < 10) ? zero+extraInfo.hour : String.valueOf(extraInfo.hour);
            hour += sep;
            min = (extraInfo.minute < 10) ? zero+extraInfo.minute : String.valueOf(extraInfo.minute);
            min += space;
            year = (extraInfo.year < 10) ? zero+extraInfo.year : String.valueOf(extraInfo.year);
            year += slash;
            month = (extraInfo.month < 10) ? zero+extraInfo.month : String.valueOf(extraInfo.month);
            month += slash;
            date = (extraInfo.date < 10) ? zero+extraInfo.date : String.valueOf(extraInfo.date);

            return hour + min + year + month + date;
        }

        private String FileSize() {

            String file = CurrentFilePath();
            String fileSize = String.format(  // edwin 20180810 format file size to 0.N MB
                    Locale.getDefault(),
                    "%.1f"
                    , ( double ) PvrFileGetSize(file) / 1024 / 1024 ).concat(" MB");

            Log.d(TAG, "FileSize: fileSize = "+fileSize);

            return fileSize;
        }

        private String GetTime(int duration) {

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
        passwordDialog = new PasswordDialogView(RecordGridActivity.this, GposInfoGet().getPasswordValue(), //passwordDialog =new PasswordDialogView(ViewActivity.this, GposInfoGet().getPasswordValue(),
                PasswordDialogView.TYPE_PINCODE,0) {
            public void onCheckPasswordIsRight() {
                Log.d(TAG, ">>>>>PASSWORD IS RIGHT!<<<<<");
                parentalLock = false;

                if(PvrPlayGetCurrentStatus() != INIT) //connie 20180806 for pvr parentalRate
                    PvrSetParentLockOK();
                else
                    CheckRecordPath(filePath);
            }

            public void onCheckPasswordIsFalse() {
                Log.d(TAG, ">>>>>PASSWORD IS False!<<<<<");
                new MessageDialogView(RecordGridActivity.this,
                        getString(R.string.STR_INVALID_PASSWORD),
                        mDelay) {
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

    public abstract class SureDialog extends Dialog {

        private final String TAG = "Activity.SureDialog";

        private TextView yes;
        private TextView no;
        private TextView msg;
        private String message;

        SureDialog(@NonNull Context context, String message)
        {
            super(context, R.style.transparentDialog);
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
}
