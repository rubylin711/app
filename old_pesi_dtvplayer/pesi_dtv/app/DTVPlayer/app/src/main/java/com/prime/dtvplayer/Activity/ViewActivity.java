package com.prime.dtvplayer.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.tv.TvContentRating;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.tvprovider.media.tv.PreviewProgram;
import androidx.tvprovider.media.tv.TvContractCompat;
import androidx.core.app.ActivityCompat;
import androidx.webkit.WebViewAssetLoader;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.TvInput.PesiTvInputService;
import com.dolphin.dtv.CallbackService;
import com.dolphin.dtv.EnTableType;
import com.dolphin.dtv.EnTrickMode;
import com.dolphin.dtv.HiDtvMediaPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.media.tv.companionlibrary.model.Channel;
import com.prime.dtvplayer.Config.Pvcfg;
import com.prime.dtvplayer.Database.DBChannelFunc;//gary20190815 add pesi db provider
import com.prime.dtvplayer.Database.DatabaseHandler;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.SureDialog;
import com.prime.dtvplayer.Sysdata.AudioInfo;
import com.prime.dtvplayer.Sysdata.BookInfo;
import com.prime.dtvplayer.Sysdata.ChannelHistory;
import com.prime.dtvplayer.Sysdata.DefaultChannel;
import com.prime.dtvplayer.Sysdata.EPGEvent;
import com.prime.dtvplayer.Sysdata.EnAudioTrackMode;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.GposInfo;
import com.prime.dtvplayer.Sysdata.MailInfo;
import com.prime.dtvplayer.Sysdata.MiscDefine;
import com.prime.dtvplayer.Sysdata.PROGRAM_PLAY_STREAM_TYPE;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.PvrInfo;
import com.prime.dtvplayer.Sysdata.RecommendChannel;
import com.prime.dtvplayer.Sysdata.SimpleChannel;
import com.prime.dtvplayer.Sysdata.SubtitleInfo;
import com.prime.dtvplayer.Sysdata.TeletextInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;
import com.prime.dtvplayer.View.AudioDialogView;
import com.prime.dtvplayer.View.CaMessageView;
import com.prime.dtvplayer.View.EWSDialog;
import com.prime.dtvplayer.View.FingerPrintView;
import com.prime.dtvplayer.View.InfoBannerView;
import com.prime.dtvplayer.View.InfoDetailView;
import com.prime.dtvplayer.View.MailInfoDialog;
import com.prime.dtvplayer.View.MessageDialogView;
import com.prime.dtvplayer.View.NormalView;
import com.prime.dtvplayer.View.OTADialogView;
import com.prime.dtvplayer.View.OkListDialogView;
import com.prime.dtvplayer.View.PasswordDialogView;
import com.prime.dtvplayer.View.PipFrameView;
import com.prime.dtvplayer.View.PvrBannerView;
import com.prime.dtvplayer.View.RatioDialogView;
import com.prime.dtvplayer.View.RecDurationSettingView;
import com.prime.dtvplayer.View.RecImageView;
import com.prime.dtvplayer.View.StopMultiRecDialogView;
import com.prime.dtvplayer.View.StopRecDialogView;
import com.prime.dtvplayer.View.SubtitleDialogView;
import com.prime.dtvplayer.View.TeletextDialogView;
import com.prime.dtvplayer.View.VMXLocationDialog;
import com.prime.dtvplayer.View.VolumeView;
import com.prime.dtvplayer.View.caMsgLayout;
import com.prime.dtvplayer.View.e16MsgLayout;
import com.prime.dtvplayer.utils.TVMessage;
import com.prime.dtvplayer.utils.TVScanParams;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.ACTION_UP;
import static android.view.KeyEvent.KEYCODE_0;
import static android.view.KeyEvent.KEYCODE_9;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_CAPTIONS;
import static android.view.KeyEvent.KEYCODE_CHANNEL_DOWN;
import static android.view.KeyEvent.KEYCODE_CHANNEL_UP;
import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;
import static android.view.KeyEvent.KEYCODE_INFO;
import static android.view.KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK;
import static android.view.KeyEvent.KEYCODE_MEDIA_FAST_FORWARD;
import static android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
import static android.view.KeyEvent.KEYCODE_MEDIA_RECORD;
import static android.view.KeyEvent.KEYCODE_MEDIA_STOP;
import static android.view.KeyEvent.KEYCODE_MENU;
import static android.view.KeyEvent.KEYCODE_PAGE_DOWN;
import static android.view.KeyEvent.KEYCODE_PAGE_UP;
import static android.view.KeyEvent.KEYCODE_PROG_BLUE;
import static android.view.KeyEvent.KEYCODE_PROG_GREEN;
import static android.view.KeyEvent.KEYCODE_PROG_RED;
import static android.view.KeyEvent.KEYCODE_PROG_YELLOW;
import static android.view.KeyEvent.KEYCODE_TV_RADIO_SERVICE;
import static android.view.KeyEvent.KEYCODE_TV_TELETEXT;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.google.android.exoplayer2.Player.REPEAT_MODE_ONE;


/**
 * Created by jim_huang on 2017/10/26.
 */

public class ViewActivity extends DTVActivity implements RecognitionListener
{
    private final String TAG = getClass().getSimpleName();
    private final Context mContext = ViewActivity.this;
    public final static int KEY_FAV = 288;//Scoty 20180613 fixed fav key number worng
    public final static int FINISH_APK = 0xffff;
    
    private SurfaceView surfaceView ;
    private NormalView viewNormalView ;
    private ViewUiDisplay viewUiDisplay;
    private caMsgLayout caMsgView;
    private InfoBannerView bannerView ;
    private InfoDetailView detailView ;
    private Handler CheckSignalHandler=null;
    private CountDownTimer autoCloseBanner = null ,autoCloseVolume = null;
    private CountDownTimer countDownTimer = null;
    private TextView chNumTxv,errMsgTxv;
    private ProgramInfo bannerProgInfo = null ;
    private EPGData epgHandle = null;
    private List<EPGEvent> epgEventGetPF = null;
    private String digitNumStr = "";
    private AudioDialogView audioDialogView;
    private SubtitleDialogView subtitleDialog;
    private TeletextDialogView teletextDialogView;
    private VolumeView mvolumeView;
    private static int volumeValue;
    private int maxVolume,minVolume=0;
    private AudioManager mAudioManager;
    private ScaleAnimation volumeOpenScale,volumeCloseScale;
    private ImageView muteIcon;
    private DTVActivity mDTVActivity = null;
    private int repeatKeyLatency = 150;
    private BookAlarmReceiver mBookAlarmReceiver = null;
    private BookArriveReceiver mBookArriveReceiver = null;
    private BookEndReceiver mBookEndReceiver = null;//Scoty 20180615 fixed timer not stop
    private HomeKeyReceiver mHomeKeyReceiver = null;
    private OkListDialogView okListDialog = null;
    private final int  KEYCODE_GUIDE_PESI = 297 ;
    private PasswordDialogView passwordDialog = null;
    private PvrBannerView pvrBanner;
    private RecDurationSettingView recDurDialog = null;
    private int RecordDuration = 60; // 1Minute
    private int DEFAULT_MIN_REC_SPACE = 100*1024*1024;
    private StopRecDialogView stopRecDialog = null;
   // private int CurSubtitleIndex = 0;//Scoty 20180621 modify when subtitle showing and then open oklist/menu/Guide should close subtitle; back to ViewActivity show subtitle
    private RecImageView mRecImageView = null;//Scoty 20180629 show Rec Icon on top
    private CaMessageView mCaMessagView = null;//Scoty 20180629 show ca message on top
    private PipFrameView pipFrame; // edwin 20180705 add PipFrame
    private pipFramRect mPipFrameRect = new pipFramRect();//Scoty 20180712 for pip
    private final String timeshiftFileName = "/timeshift.ts";
    private StopMultiRecDialogView mStopMultiRecDialog = null;//Scoty 20180720 modify stop multi rec dialog
    private List<OkListManagerImpl> showOkListManager = new ArrayList<>();
    // jim 2018/07/13 add start speechrecognizer in dtvplayer -s
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private static final String START_SPEECHRECOGNIZER_BROADCAST = "pesi.VoiceKeyDectect.broadcast.action.startSpeechRecognizer" ;
    // jim 2018/07/13 add start speechrecognizer in dtvplayer -e
    private MessageDialogView StorageNotAvailableDialog; // edwin 20180802 add dialog

    // === for VMX need open/close -s===
    private VMXOtaStartReceiver mVMXOtaStartReceiver = null;
    private VMXOtaProcReceiver mVMXOtaProcReceiver = null;
    private vmxWaterMarkReceiver mVMXWatermarkReceiver = null;
    private vmxShowMsgReceiver mVMXShowMsgReceiver = null;
    private vmxSearchReceiver mVMXSearchReceiver = null;
    private vmxE16MsgReceiver mVMXE16MsgReceiver = null;
    private vmxEWBSReceiver mVMXEWBSReceiver = null;
    private vmxFactoryReceiver mVMXFactoryReceiver = null;
    private vmxChBlockReceiver mVMXBlockReceiver = null;
    private vmxMailReceiver mVMXMailReceiver = null;
    private vmxEWBSStopReceiver mVMXEWBSStopReceiver = null;//Scoty 20181225 modify VMX EWBS rule
    FingerPrintView mFingerPringView = null;
    private ImageView mailIcon;
    private List<ProgramManagerImpl> ProgramManagerList = null;

    private MessageDialogView CardDetectMsg = null;
    private EWSDialog ewsDialog = null;
    private OTADialogView OTAMsg = null;
    private e16MsgLayout E16Msg;
    private ConstraintLayout VMXBLOCK;
    // === for VMX need open/close -e===

    private static boolean firstBoot = true; // connie 20181106 for not show no signal when first boot

    // Johnny 20190521 modify flow of pressing back again to exit
    private static final int TIME_INTERVAL_BACK = 2000; // milliseconds, time between two back pressed
    private long mBackPressedTime = 0;
    private Toast mExitToast;

    private int sCreenOffTime = 0;//Scoty 20181129 set Screen Saver Time
    //========    change channel test !!!!  =========
    private int TEST_MODE = 0 ;
    private static Handler ChangeChTest_handler;
    private int CHANGE_CHANNEL_TEST_DELAY = 6000;
    private Thread t =null ;
    //===============================
    //private RecommandChannel mRecommandChannel = new RecommandChannel(ViewActivity.this);
    //==================== Save Tvprovider Channel and Program Uri
    private SharedPreferences TvSharedPreference = null;//Save Favorite and Recently Watched Channel Row Uri
    private SharedPreferences.Editor tvEditor = null;
    //===================

    // Johnny 20210423 temp for RTK avplay issue -s
    private boolean waitPlaySuccess = false;
    private final long TIMEOUT_WAIT_PLAY_SUCCESS = 1000;
    // Johnny 20210423 temp for RTK avplay issue -e


    //Scoty 20210728 add for RTK A11 1319 prepareDTV get Service timeout, show request reboot dialog
    private MessageDialogView mDTVServiceCheckMessage;

    //=================== Test IP VOD Youtube
    public boolean mExoPlayerIsPlaying = false; // edwin 20200429 for playing stream
    public SimpleExoPlayer mExoPlayer; // edwin 20200429 for playing stream
    public MediaSource mMediaSource; // edwin 20200429 for playing stream
    private SurfaceView mSurfaceViewExoplayer;
    private WebView mWebView;
    //===================

    Handler BootAVPlayHandler;
    final Runnable BootAVPlayRunnable = new Runnable() {
        public void run() {
            // TODO Auto-generated method stub
            Log.d(TAG, "ethan Runnable =>ChangeProgram");

            AvControlOpen(ViewHistory.getPlayId());
            BootAVPlayHandler.postDelayed(new Runnable() {//eric lin 20181127 fix live tv pip manual stop and manual open dtvplayer, windows size still pip size issue
                    @Override
                    public void run() {
                        AvControlSetWindowSize(0, new Rect(0, 0, 1920, 1080));
                    }
                }, 500);

            // Johnny add 20180524 for setting ratio conversion, need to be called after AvControlOpen
            GposInfo gposInfo = GposInfoGet();
            AvControlChangeRatioConversion(ViewHistory.getPlayId(), GetRatioByIndex(gposInfo.getScreen16x9()), gposInfo.getConversion());

            Log.d(TAG, "run: ChannelId = ["+ViewHistory.getCurChannel().getChannelId() + "] Type = ["
                    + ViewHistory.getCurChannel().getPlayStreamType()+"]");
            if(ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE )
            {
                PlayExoPlayerVideo(ViewHistory.getCurChannel());
                ShowBanner(1);
            }else if(ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.YOUTUBE_TYPE)
            {
                StartPlayYoutubeVideo(ViewHistory.getCurChannel());
                ShowBanner(1);
            }else {
                viewUiDisplay.ChangeProgram();
                if (Pvcfg.getCAType() == Pvcfg.CA_VMX && GetVMXBlockFlag() == 1) { // for VMX need open/close -s
                    AvControlShowVideo(ViewHistory.getPlayId(), false);
                    AvControlSetMute(ViewHistory.getPlayId(), true);
                } // for VMX need open/close -e
            }
        }
    };

    @Override
    public void onConnected() {
        Log.d(TAG, "Test msg  onConnected");
        if(CheckSignalHandler==null) {
            Log.d(TAG, "onConnected:  ADD HANDLER !!!!");
            CheckSignalHandler = new Handler();
            CheckSignalHandler.post(CheckStatusRunnable);
        }
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "Test msg  onDisConnected");
        if(CheckSignalHandler!=null)
        {
            CheckSignalHandler.removeCallbacks(CheckStatusRunnable);
            CheckSignalHandler = null;
        }
    }

    @Override
    public void onMessage(TVMessage tvMessage) {
        super.onMessage(tvMessage);
        switch (tvMessage.getMsgType()) {
            case TVMessage.TYPE_EPG_UPDATE: {
                long channelID = tvMessage.getChannelId();
                if(detailView.GetVisibility() == VISIBLE) {
                    if ( !isFocusPip() && ViewHistory.getCurChannel().getChannelId() == channelID )
                    {
                        viewUiDisplay.UpdateEpgPF();//first update viewUiDisplay preset/follow event
                        int eventType = epgHandle.GetShowEventType(); // present / follow
                        String info = epgHandle.GetCurPorgramDetail(eventType);//second assign detail
                        detailView.UpdateDetailInfo(info);
                    }
                    else if( isFocusPip() // Edwin 20181205 cur pip is null when open pip not yet
                            && ViewHistory.getCurPipChannel().getChannelId() == channelID )
                    {
                        viewUiDisplay.UpdatePipEpgPF();//first update viewUiDisplay preset/follow event
                        int eventType = epgHandle.GetShowEventType(); // present / follow
                        String info = epgHandle.GetCurPorgramDetail(eventType);//second assign detail
                        detailView.UpdateDetailInfo(info);
                    }
                }

            }break;
            case TVMessage.TYPE_AV_PLAY_STATUS:
            {
                Log.d(TAG, "onMessage: TYPE_AV_PLAY_STATUS  IN => " + tvMessage.getAvFinish());
                if (bannerView.GetVisibility() == View.VISIBLE) {// up/down key change channel no need show banner again
                    if(isFocusPip())
                        bannerView.UpdateBanner(GposInfoGet(), bannerProgInfo, epgEventGetPF, GetLocalTime(), ViewHistory.getCurPipChannel().getChannelNum(), mDTVActivity);
                    else
                        bannerView.UpdateBanner(GposInfoGet(), bannerProgInfo, epgEventGetPF, GetLocalTime(), ViewHistory.getCurChannel().getChannelNum(), mDTVActivity); // connie 20180524 fix channel num show wrong in fav group
                }
                else//after change channel show banner
                    ShowBanner(1);

//                Log.d(TAG, "johnny onMessage: av play success");
                stopWaitPlaySuccess();
            }break;
            case TVMessage.TYPE_CA_MSG_UPDATE:
            {
                Log.d(TAG, "onMessage: TYPE_CA_MSG_UPDATE IN => " + tvMessage.getCA_Msg_String());
                /* //open to show ca error message
                if(tvMessage.getCAFlag() == 1) {
                    String errorCode = tvMessage.getCA_ErrorCode();
                    if(errorCode.charAt(0) != 'D' && errorCode.charAt(0) != 'I')
                        viewNormalView.ViewErrMsg(View.VISIBLE, tvMessage.getCA_Msg_String());
                    else
                        viewNormalView.ViewErrMsg(View.INVISIBLE, null);
                }
                else {
                    viewNormalView.ViewErrMsg(View.INVISIBLE, null);
                }*/
                /*//Scoty 20180629 add fingerprint example
                show fingerprint example:
                                int txtColor = Color.argb(255, 255, 255, 255);
                int bgColor = Color.argb(255, 255, 0, 0);
                FingerPrintView mFingerPringView = new FingerPrintView(ViewActivity.this,"PIONEER_10123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ" , 30000,1,0,
                        300,300,600,50,txtColor,bgColor);
                mFingerPringView.setFpTextGravity(Gravity.TOP|Gravity.START);
                mFingerPringView.setBlink(1,500);
                mFingerPringView.setRandom(1,2000);
                mFingerPringView.setPercentScreen(50);
                mFingerPringView.setExitBtn(200,200,50,50, 10,Gravity.CENTER);
                mFingerPringView.setExitBtnParams(getString(R.string.STR_BACK),Color.argb(255, 0, 0, 0),
                        Color.argb(255, 255, 255, 255),Gravity.CENTER);
                mFingerPringView.show();
                */
            }break;
            case TVMessage.TYPE_TUNER_LOCK_STATUS:
            {
                Log.d(TAG, "onMessage: TYPE_TUNER_LOCK_STATUS ==>> " + tvMessage.getTunerLockStatus());
                if(tvMessage.getTunerLockStatus() == 0) {
//                    errMsgTxv.setText(getString(R.string.STR_NO_SIGNAL));
//                    errMsgTxv.setVisibility(View.VISIBLE);
//                    errMsgTxv.requestLayout();
                    if(mCaMessagView == null)//Scoty 20180725 fixed no signal message can not disappear
                    mCaMessagView = new CaMessageView(ViewActivity.this,getString(R.string.STR_NO_SIGNAL));//Scoty 20180629 show ca message on top
                    if(isFocusPip()) {
                        PipStop();
                    }
                    else if( Pvcfg.getCAType() == Pvcfg.CA_VMX && GetVMXBlockFlag() == STATUS_CH_NO_BLOCK ) {
                        AvControlShowVideo(ViewHistory.getPlayId(), false);
                        AvControlSetMute(ViewHistory.getPlayId(), true);
                    }
                    detailView.UpdateLockStatus(0);
                    if(detailView.GetVisibility() == View.VISIBLE)
                        detailView.UpdateTunerStatus( TunerGetStrength(0), TunerGetQuality(0), TunerGetBER(0));
                }
                else {
//                    errMsgTxv.setVisibility(View.INVISIBLE);
//                    errMsgTxv.requestLayout();
                    if(mCaMessagView != null) {//Scoty 20180629 show ca message on top
                        mCaMessagView.remove();
                        mCaMessagView = null;//Scoty 20180725 fixed no signal message can not disappear
                    }
                    if(isFocusPip()) {
                        PipStart(ViewHistory.getCurPipChannel().getChannelId(),1);
                    }
                    // Edwin 20181129 fix AvControlShowVideo when VMX blocking channel
                    else if( Pvcfg.getCAType() == Pvcfg.CA_VMX && GetVMXBlockFlag() == STATUS_CH_NO_BLOCK )
                    {
                        AvControlShowVideo(ViewHistory.getPlayId(), true);
                        AvControlSetMute(ViewHistory.getPlayId(), false);
                    }
                    detailView.UpdateLockStatus(1);
                    if(detailView.GetVisibility() == View.VISIBLE)
                        detailView.UpdateTunerStatus( TunerGetStrength(0), TunerGetQuality(0), TunerGetBER(0));
                }
            }break;
            case TVMessage.TYPE_AV_LOCK_STATUS:
            {
                Log.d(TAG, "onMessage: tvMessage.getAvLockStatus() = " + tvMessage.getAvLockStatus() +" ch1 "+ ViewHistory.getCurChannel().getChannelId() +" ch "+ tvMessage.getChannelId() );
                switch(tvMessage.getAvLockStatus())//lock status 0(no lock), 1(user lock), 2(parental lock)
                {
                    case 0://no lock
                    {
                        closPassWordDialog();
                    }break;
                    case 1://user lock
                    {
                        Log.d(TAG, "notifyMessage: ChannelId = " + tvMessage.getChannelId()
                                + " tvMessage.getRating() = " + tvMessage.getRating() + " rating = " + GposInfoGet().getParentalRate());
                        if(isFocusPip())
                            PipStop();
                        else
                            AvControlPlayStop(ViewHistory.getPlayId());
                        if (mCaMessagView != null) // connie 20181101 fixed no signal message can not disappear
                            mCaMessagView.SetVisibility(View.INVISIBLE);
                        showPasswordDialog(ViewHistory.getPlayId(), tvMessage.getChannelId(), ViewHistory.getCurGroupType());
                    }break;
                    case 2://parental lock
                    {
                        Log.d(TAG, "notifyMessage: ChannelId = " + tvMessage.getChannelId()
                                + " tvMessage.getRating() = " + tvMessage.getRating() + " rating = " + GposInfoGet().getParentalRate());
                            if(isFocusPip())
                                PipStop();
                            else
                                AvControlPlayStop(ViewHistory.getPlayId());
                            showPasswordDialog(ViewHistory.getPlayId(), tvMessage.getChannelId(), ViewHistory.getCurGroupType());
                    }break;
                }
            }break;
            case TVMessage.TYPE_AV_FRAME_PLAY_STATUS:
            {
                int status = tvMessage.getAvFrameStatus();
                //eric lin 20180803 no video signal,-start
                long channelId = tvMessage.getAvFrameChannelId();
                if(status == 1)
                {
//                    errMsgTxv.setText(getString(R.string.STR_NO_VIDEO_SIGNAL));
//                    errMsgTxv.setVisibility(View.VISIBLE);
//                    errMsgTxv.requestLayout();
                    boolean show=false;
                    if(isFocusPip()) {
                        if (channelId == ViewHistory.getCurPipChannel().getChannelId()) {
                            show=true;
                            //Log.d(TAG, "TOST TYPE_AV_FRAME_PLAY_STATUS: status == 1, match_p, chId=" + channelId);
                            //PipStop();
                        }
                    else {
                            //Log.d(TAG, "TOST TYPE_AV_FRAME_PLAY_STATUS: status == 1, not match_p, chId=" + channelId + ", view_id=" + ViewHistory.getCurPipChannel().getChannelId());
                        }
                    }
                    else {
                        if(ViewHistory.getCurChannel() != null) {
                            if (channelId == ViewHistory.getCurChannel().getChannelId()) {
                                show=true;
                                //Log.d(TAG, "TOST TYPE_AV_FRAME_PLAY_STATUS: status == 1, match, chId=" + channelId);
                                //AvControlShowVideo(ViewHistory.getPlayId(), false);
                                //AvControlSetMute(ViewHistory.getPlayId(), true);
                            }
                            else {
                                //Log.d(TAG, "TOST TYPE_AV_FRAME_PLAY_STATUS: status == 1, not match, chId=" + channelId + ", view_id=" + ViewHistory.getCurChannel().getChannelId());
                            }
                        }
                    }
                    if(show) {
                        if (mCaMessagView == null)//Scoty 20180725 fixed no signal message can not disappear
                        {
                            if(TunerGetLockStatus(0) != 0) { // connie 20181106 for not show no signal when first boot
                                mCaMessagView = new CaMessageView(ViewActivity.this, getString(R.string.STR_NO_VIDEO_SIGNAL));
                            }
                        }
                    }
                }
                else
                {
//                    errMsgTxv.setVisibility(View.INVISIBLE);
//                    errMsgTxv.requestLayout();
                    clearNoVideoSignal();
                    /*if(isFocusPip()) {
                        //Log.d(TAG, "TYPE_AV_FRAME_PLAY_STATUS: status == 1, isFocusPip()==true");
                        if(ViewHistory.getCurPipChannel() != null) {
                            if (channelId == ViewHistory.getCurPipChannel().getChannelId()) {
                                //Log.d(TAG, "TOST TYPE_AV_FRAME_PLAY_STATUS: status == 0, match_p, chId=" + channelId);
                            }else {
                                //Log.d(TAG, "TOST TYPE_AV_FRAME_PLAY_STATUS: status == 0, not match_p, chId=" + channelId + ", view_id=" + ViewHistory.getCurPipChannel().getChannelId());
                            }
                        }
                        //PipStart(ViewHistory.getCurPipChannel().getChannelId(), 1);
                    }
                    else {
                        if(ViewHistory.getCurChannel() != null) {
                            if (channelId == ViewHistory.getCurChannel().getChannelId()) {
                                //Log.d(TAG, "TOST TYPE_AV_FRAME_PLAY_STATUS: status == 0, match, chId=" + channelId);
                            }else {
                                //Log.d(TAG, "TOST TYPE_AV_FRAME_PLAY_STATUS: status == 0, not match, chId=" + channelId + ", view_id=" + ViewHistory.getCurChannel().getChannelId());
                            }
                    }
                        //AvControlShowVideo(ViewHistory.getPlayId(), true);
                        //AvControlSetMute(ViewHistory.getPlayId(), false);
                    }*/
                }
                //eric lin 20180803 no video signal,-end
            }break;

            case TVMessage.TYPE_PVR_REC_OVER_FIX:
            {
                //Scoty 20180720 add HI_SVR_EVT_PVR_REC_OVER_FIX send recId -s//Scoty 20180716 fixed timeshft/record not stop -s
                Log.d(TAG, "onMessage:  TYPE_PVR_REC_OVER_FIX RecId = " + tvMessage.getRecId() + " pvrMode = " + tvMessage.getPvrMode());
                int pvrMode = tvMessage.getPvrMode();//getCurrentPvrMode(); //int pvrMode = PvrGetCurrentRecMode();
                int recId = tvMessage.getRecId();//Scoty 20180809 modify dual pvr rule

                if(mStopMultiRecDialog != null)
                    mStopMultiRecDialog.remove();

                if(CheckPvrMode(pvrMode)) {//Scoty 20180827 add and modify TimeShift Live Mode
                    //Log.d(TAG, "onMessage: TOST TYPE_PVR_REC_OVER_FIX-- TIMESHIFT");
                    PvrTimeShiftPlay(ViewHistory.getPlayId());
                }
                else if(pvrMode == PvrInfo.EnPVRMode.RECORD) {//if(pvrMode == REC) {
                    //Log.d(TAG, "onMessage: TOST TYPE_PVR_REC_OVER_FIX-- REC");
                    stopPVRMode(pvrMode, recId,0);//Scoty 20180809 modify dual pvr rule//eric lin 20180629 stop pvr and av play
                }
                //Scoty 20180720 add HI_SVR_EVT_PVR_REC_OVER_FIX send recId -e//Scoty 20180716 fixed timeshft/record not stop -e
            }break;

            case TVMessage.TYPE_PVR_REC_DISK_FULL:
            {
                Log.d(TAG, "onMessage: TYPE_PVR_REC_DISK_FULL");
                //int pvrMode = getCurrentPvrMode();//int pvrMode = PvrGetCurrentRecMode();
                //stopPVRMode(pvrMode, 0);//eric lin 20180629 stop pvr and av play
                stopAllRec();//Scoty 20180720 add stop all records/timeshift
                new MessageDialogView(mContext, getString(R.string.STR_HD_SPACE_FULL), 3000) {
                    public void dialogEnd() {
                        ShowBanner(1);
                    }
                }.show();
            }break;

            case TVMessage.TYPE_PVR_PLAY_TO_BEGIN:
            {
                Log.d(TAG, "onMessage: TYPE_PVR_PLAY_TO_BEGIN");
                PvrTimeShiftPlay(ViewHistory.getPlayId());
            }break;

            case TVMessage.TYPE_PVR_PLAY_REACH_REC://Timeshift
            {
                Log.d(TAG, "onMessage: TYPE_PVR_PLAY_REACH_REC");
                pvrPlayTimeShiftStop();//Scoty 20180827 add and modify TimeShift Live Mode
                /*
                if(stopRecDialog == null || !stopRecDialog.isShowing()) {
                    final int pvrMode = getCurrentPvrMode();//final int pvrMode = PvrGetCurrentRecMode();
                    PvrTimeShiftPlay(ViewHistory.getPlayId());//eric lin 20180929 add by allen suggestion
                    PvrTimeShiftPause(ViewHistory.getPlayId());
                    if (pvrMode != PvrInfo.EnPVRMode.NO_ACTION) {//if (pvrMode != NO_ACTION) {
                        stopRecDialog = new StopRecDialogView(ViewActivity.this, mDTVActivity) {
                            @Override
                            public void onStopPVR() {
                                // stop PVR
                                //Log.d(TAG, "onMessage: TOST TYPE_PVR_PLAY_REACH_REC want stop");
                                int recId = PvrRecordCheck(ViewHistory.getCurChannel().getChannelId());//Scoty 20180809 modify dual pvr rule
                                stopPVRMode(pvrMode,recId, 1);//Scoty 20180809 modify dual pvr rule//eric lin 20180629 stop pvr and av play
                            }
                        };
                    }
                }
                */
            }break;
            //Scoty 20180802 add message when recording and then remove usb not clean rec icon
            case TVMessage.TYPE_REMOVE_USB_STOP_REC://for remove hdd when recording
            {
                Log.d(TAG, "onMessage: =====>>>> TYPE_REMOVE_USB_STOP_REC");
                if(GposInfoGet().getRecordIconOnOff() == 1) {//Scoty 20180806 check record Icon show or not by gpos
                    if (mRecImageView.getVisibility() == View.VISIBLE) {
                        mRecImageView.SetVisibility(false);
                    }
                }
                if(mStopMultiRecDialog != null) {
                    mStopMultiRecDialog.remove();
                }
            }break;
            case TVMessage.TYPE_PVR_NOT_SUPPORT://Scoty 20180827 add and modify TimeShift Live Mode
            {
                new MessageDialogView(ViewActivity.this,
                        getString(R.string.STR_NOT_SUPPORT), 1000) {
                    public void dialogEnd() {
                    }
                }.show();
            }break;

            // for VMX need open/close -s
            case TVMessage.TYPE_VMX_SET_PIN:
            {
                int vmxPinOnOff = tvMessage.GetVMXPinOnOff();
                long vmxPinChannelID = tvMessage.GetVMXPinChannelID();
                int vmxPinIndex = tvMessage.GetVMXPinIndex();
                int vmxTextSelector = tvMessage.GetVMXPinTextSelector();

                Log.d(TAG, "onMessage: vmxPinOnOff = " + vmxPinOnOff + "     vmxPinChannelID = " + vmxPinChannelID);
                VMXCheckPin(this, 1, vmxPinOnOff, vmxPinChannelID, "", vmxPinIndex, vmxTextSelector);

            }break;

            case TVMessage.TYPE_VMX_IPPV:
            {
                int vmxIPPVOnOff = tvMessage.GetIPPV_OnOff();
                long vmxIPPVChannelID = tvMessage.GetIPPV_ChannelID();
                String vmxIPPVcurToken = tvMessage.GetIPPV_CurToken();
                String vmxIPPVCost = tvMessage.GetIPPV_cost();
                int vmxPinIndex = tvMessage.GetIPPV_PinIndex();

                Log.d(TAG, "onMessage:  vmxIPPVOnOff =  " + vmxIPPVOnOff + "  vmxIPPVChannelID ="+ vmxIPPVChannelID+ "   vmxIPPVcurToken = " + vmxIPPVcurToken + "    vmxIPPVCost = "+vmxIPPVCost);
                String ippvMsg = getString(R.string.STR_AVAILABLE_CREDIT) + vmxIPPVcurToken + "\n"
                        +  getString(R.string.STR_COST_OF_EVENT) + vmxIPPVCost;
                VMXCheckPin(this, 2, vmxIPPVOnOff, vmxIPPVChannelID, ippvMsg, vmxPinIndex, 0);
            }break;

            case TVMessage.TYPE_VMX_BCIO_NOTIFY:// connie 20180925 add for ippv/pin bcio notify
            {
                int vmxIPPVOnOff = tvMessage.GetPCIONotify_type();
                VMXBcioNotify(vmxIPPVOnOff);
            }break;

            case TVMessage.TYPE_VMX_CARD_DETECT:
            {
                int cardStatus = tvMessage.GetCardDetect();
                VMXSmartCardInOut(cardStatus);
            }break; // connie 20180903 for VMX -e
            // for VMX need open/close -e
            default:
                break;
        }
    }

    public void showPasswordDialog(final int playId, final long channelId, final int groupType)
    {
        passwordDialog =new PasswordDialogView(ViewActivity.this, GposInfoGet().getPasswordValue(),
                PasswordDialogView.TYPE_PINCODE,1) {
            public void onCheckPasswordIsRight() {
                Log.d(TAG, ">>>>>PASSWORD IS RIGHT!<<<<<");
                if(isFocusPip())
                    PipStart(channelId,2);
                else
                    AvControlPlayByChannelId(playId, channelId, groupType, 2);
            }

            public void onCheckPasswordIsFalse() {
                Log.d(TAG, ">>>>>PASSWORD IS False!<<<<<");
                new MessageDialogView(ViewActivity.this,
                        getString(R.string.STR_INVALID_PASSWORD), 3000) {
                    public void dialogEnd() {
                    }
                }.show();
            }

            public boolean onDealUpDownKey() {
                return false;
            }
        };
    }

    public void closPassWordDialog()
    {
        if(passwordDialog != null && passwordDialog.isShowing())
            passwordDialog.dismissDialog();
    }

    @Override
    protected void onNewIntent(Intent intent) { // connie 20190819 modify for change channel by pesi launcher
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent: ");
        setIntent(intent);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"view onResume");

        long channelID = 0; // connie 20190819 modify for change channel by pesi launcher -s
        String openActivity = ""; // connie 20190830 for open activity from pesi laucner
        Bundle launcher_bundle = this.getIntent().getExtras();
        if(launcher_bundle!= null)
        {
            channelID = launcher_bundle.getLong(this.getString(R.string.STR_CHANGE_PROGRAM_CHANNEL_ID));
            openActivity = launcher_bundle.getString(getString(R.string.STR_OPEN_ACTIVITY));
            Log.d(TAG, "onResume: channelID = " + channelID + "   openActivity = " + openActivity);
            this.getIntent().removeExtra(getString(R.string.STR_CHANGE_PROGRAM_CHANNEL_ID));
            this.getIntent().removeExtra(getString(R.string.STR_OPEN_ACTIVITY));
            if ( channelID == 0 )
            {
                channelID = ViewHistory.getCurChannel().getChannelId(); // jim 2019/09/10 fix entry dtvplayer from live channel will can not change channel
            }
        }
        else
            Log.d(TAG, "onResume: bundle is NULL !!!!!!!"); // connie 20190819 modify for change channel by pesi launcher -e

        sCreenOffTime = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 0);//Scoty 20181129 set Screen Saver Time
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 2147483647);//Scoty 20181129 set Screen Saver Time
        BootAVPlayHandler = new Handler();
        SetEnterViewActivity(1);
        ShowCurSubtitle();//Scoty 20180621 modify when subtitle showing and then open oklist/menu/Guide should close subtitle; back to ViewActivity show subtitle
        if(GetChannelExist() == 1) {
            int AVplayStatus = AvControlGetPlayStatus(ViewHistory.getPlayId());
            Log.d(TAG, "onResume: AVplayStatus = "+AVplayStatus+" LIVE_TV_MODE = "+PesiTvInputService.LIVE_TV_MODE);
            mDTVActivity = this;
            viewUiDisplay = GetViewUiDisplay();
            //ViewUiDisplayInit();

            if(channelID != 0) // connie 20190819 modify for change channel by pesi launcher
            {
                ProgramInfo service = ProgramInfoGetByChannelId(channelID);
                if(service != null )
                    AvControlPlayByChannelId(ViewHistory.getPlayId(), channelID, service.getType(), 0);
                else
                    AvControlPlayByChannelId(ViewHistory.getPlayId(), ViewHistory.getCurChannel().getChannelId(), ViewHistory.getCurGroupType(), 0);
                ShowBanner(1);
            }
            if( AVplayStatus == HiDtvMediaPlayer.EnPlayStatus.IDLE.getValue()
                    || AVplayStatus == HiDtvMediaPlayer.EnPlayStatus.STOP.getValue()
                    || AVplayStatus == HiDtvMediaPlayer.EnPlayStatus.RELEASEPLAYRESOURCE.getValue()
                    || PesiTvInputService.LIVE_TV_MODE ) { // Edwin 20181105 fix black screen when using home key in Live Channel
                if ( PesiTvInputService.LIVE_TV_MODE )
                {
                    // Edwin 20181123 to stop LiveTV in pip mode -s
                    try {
                        // Edwin 20181113 fix wrong timeshift banner when TimeShift in LiveTV
                        //PvrTimeShiftStop(ViewHistory.getPlayId());
                        Process process = Runtime.getRuntime().exec("am force-stop com.google.android.tv");
                    }
                    catch ( IOException e ) {
                        e.printStackTrace();
                    }
                    // Edwin 20181123 to stop LiveTV in pip mode -e
                    AvControlPlayStop(MiscDefine.AvControl.AV_STOP_ALL);//eric lin 20181204 adjust live tv
                    AvControlClose(0);//eric lin 20181204 adjust live tv
                }
                BootAVPlayHandler.post(BootAVPlayRunnable); // connie 20181106 for timer start but video not show when focus not on apk
                ShowBanner(0);
                if(TunerGetLockStatus(0) != 1)//Scoty 20180801 fixed error message not clean when exit apk
                {
                    if (mCaMessagView != null) {
                        mCaMessagView.remove();
                        mCaMessagView = null;
                    }
                    //if(!firstBoot)  // connie 20181106 for not show no signal when first boot
                    //    mCaMessagView = new CaMessageView(ViewActivity.this,getString(R.string.STR_NO_SIGNAL));
                    firstBoot = false;
                }

                //========  change channel test !!!!  =========
                if(TEST_MODE == 1)
                    ChangeChTest_handler.postDelayed(Key_Test_Run,CHANGE_CHANNEL_TEST_DELAY);//Scoty 20181001 fixed auto change channel show no video signal
                //===============================
            }
            else if(AVplayStatus== HiDtvMediaPlayer.EnPlayStatus.LIVEPLAY.getValue())//Scoty 20180725 fixed no signal message can not disappear
            {
                if (mCaMessagView != null) { // connie 20181106 for not show no signal when first boot
                    mCaMessagView.remove();
                    mCaMessagView = null;
                }
                if( Pvcfg.getCAType() == Pvcfg.CA_VMX && GetVMXBlockFlag() !=1) // for VMX need open/close
                {
                    AvControlShowVideo(ViewHistory.getPlayId(), true);
                    AvControlSetMute(ViewHistory.getPlayId(), false);
                }
                else
                {
                    //if(TunerGetLockStatus(0) != 1)//Scoty 20181113 fixed back to ViewActivity should not always show no signal
                    //mCaMessagView = new CaMessageView(ViewActivity.this,getString(R.string.STR_NO_SIGNAL)); // connie 20181106 for not show no signal when first boot
                }
            }
            if(Pvcfg.getCAType() == Pvcfg.CA_VMX && NeedLocationSetting()) { // for VMX need open/close
                //SetVMXLocationFlag(0);//Scoty 20181218 modify VMX location rule
                new VMXLocationDialog(this, mDTVActivity);
            }
            LuncherOpenActivity(openActivity);  // connie 20190830 for open activity from pesi laucner
        }
        else
        {
            if(!openActivity.equals(""))  // connie 20190830 for open activity from pesi laucner
                LuncherOpenActivity(openActivity);
            else {
                Intent it = new Intent();
                openMainMenu();
            }
            //if(errMsgTxv.getVisibility()==View.VISIBLE)
             //   errMsgTxv.setVisibility(View.INVISIBLE);
        }
        SaveTable(EnTableType.ALL);

        //InitSpeechRecognizer(); // Edwin 20190510 disable speech recognizer
        if(Pvcfg.getCAType() != Pvcfg.CA_NONE)// connie 20181116 for vmx mail -s
        {
            if (!CheckMailAllRead()) {
                mailIcon.setVisibility(VISIBLE);
                mailIcon.requestLayout();
            } else {
                mailIcon.setVisibility(INVISIBLE);
                mailIcon.requestLayout();
            }
        }// connie 20181116 for vmx mail -e

    }

    private void ShowRebootMsg()
    {
        Log.d(TAG, "ShowRebootMsg: IN");
        String Text = "Please Reboot the Set-Top-Box !";
        mDTVServiceCheckMessage = new MessageDialogView(ViewActivity.this, Text, 0) {
            public void dialogEnd() {
             }
        };
        mDTVServiceCheckMessage.show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        checkWebView();
        setContentView(R.layout.view);

        //////Check TpList is Ready -s
        List<TpInfo> tpList = TpInfoGetList(0);
        if(tpList != null) {
            Log.d(TAG, "CheckTpList: size = [" + tpList.size() + "]");
            if(tpList.size() == 0) {
                ShowRebootMsg();
            }
        }
        else {
            Log.d(TAG, "onCreate: tpList is Null");
            ShowRebootMsg();
        }
        //////Check TpList is Ready -e

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        bannerView = (InfoBannerView) findViewById(R.id.infoBannerViewLayout);
        detailView = (InfoDetailView) findViewById(R.id.infoDetailViewLayout);
        viewNormalView = (NormalView) findViewById(R.id.viewNormalViewLayout);
        caMsgView = (caMsgLayout) findViewById(R.id.caMsgLayout);
        E16Msg = (e16MsgLayout) findViewById(R.id.E16MsgLayout);
        epgHandle = new EPGData();
        chNumTxv = (TextView) findViewById(R.id.chChange);
        muteIcon = (ImageView) findViewById(R.id.mute);
        errMsgTxv = (TextView) findViewById(R.id.errorMsg);
        pvrBanner = (PvrBannerView) findViewById(R.id.viewPvrBanner);
        VMXBLOCK = (ConstraintLayout) findViewById(R.id.VMXChBlock);
        mailIcon = (ImageView) findViewById(R.id.mailIGV); // connie 20181116 for vmx mail

        CheckVersion();//Scoty 20181123 add check Apk and Service Version
        SetAnttena5V(); // connie 20181106 for init anttena 5V
        InitRecordPath(); // connie 20181024 for USB Path wrong
        CloseBanner();//Scoty 20180621 modify close banner as function// connie 20180524 fix channel num show wrong in fav group
        detailView.SetVisibility(View.INVISIBLE, null, 0, 0, 0, "");
        viewNormalView.SetVisibility(View.INVISIBLE);
        caMsgView.SetDTVActivity(mDTVActivity);
        caMsgView.SetVisibility(View.INVISIBLE);
        E16Msg.SetVisibility(mDTVActivity, View.INVISIBLE);
        VMXBLOCK.setVisibility(View.INVISIBLE);
        setSurfaceView(surfaceView);
        InitUIBookList(); //Init and Set BookManagerList for UI, from pesi service
        startBookService(); // Johnny add 20180307 for book //If use Android Emulator must mark
        subBroadcast(); // Johnny add 20180313 for book   ///If use Android Emulator must mark
        //========  change channel test !!!!  =========
        if (TEST_MODE == 1) {
            Log.d(TAG, "onCreate:  Change channel Test !!!!");
            createMessageHandleThread();
        }
        //===============================

        // edwin 20180705 add PipFrame
        {
            //viewLayout = (ConstraintLayout) findViewById(R.id.viewLayout);
            pipFrame = (PipFrameView) findViewById(R.id.pipFrameViewLayout);//Scoty 20180712 for pip
            //pipFrame = new PipFrame(this);
            //pipFrame.SetVisibility(View.INVISIBLE);
            //viewLayout.addView(pipFrame);
        }
        mRecImageView = new RecImageView(ViewActivity.this);
        mRecImageView.SetVisibility(false);
        pvrBanner.setVisibility(mDTVActivity, INVISIBLE, 0,isFocusPip());//Scoty 20180802 fixed cur time not update when focus on Pip

        StorageNotAvailableDialog = new MessageDialogView( mContext // edwin 20180802 add Storage Not Available Dialog
                , getString( R.string.STR_STORAGE_DEVICE_IS_NOT_AVAILABLE )
                , getResources().getInteger( R.integer.MESSAGE_DIALOG_DELAY ) ) {public void dialogEnd () { }};

        if ( Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798 )
        {
            try // Edwin 20181207 android display area is too large
            {
                Runtime.getRuntime().exec("wm overscan 35,20,0,0");
            }
            catch ( IOException e ) { e.printStackTrace(); }
        }

        mExitToast = Toast.makeText(this, getString(R.string.STR_PRESS_BACK_AGAIN), Toast.LENGTH_SHORT);  // Johnny 20180912 add press back again to exit

        InitExoPlayerVideo();
        InitWebView();

    }


    private void InitWebView()
    {
        String USERAGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36";
        mWebView = (WebView) findViewById(R.id.videoWebView);
        SetYoutubeWebview(mWebView);
        mWebView = GetYoutubeWebview();

        mWebView.setVisibility(INVISIBLE);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAllowContentAccess(true);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        mWebView.getSettings().setUserAgentString(USERAGENT);
        mWebView.getSettings().setUseWideViewPort(true); //將圖片調整到適合webview的大小
        mWebView.getSettings().setLoadWithOverviewMode(true); // 縮放至螢幕的大小
        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.getSettings().setBuiltInZoomControls(true);

        //mWebView.setWebChromeClient(new WebChromeClient());
        //mWebView.setWebViewClient(new WebViewClient()); //不調用系統瀏覽器

        //ConstraintLayout.setLayoutParams(new LinearLayout.LayoutParams(500, 500));
        //mWebView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onReceivedTitle(WebView view, String title) {
                Log.d(TAG, "webview onReceivedTitle: \n=[" + view.getUrl() +"] \ntitle = [" + title+"]");
            }
        });


        WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        //mWebView.scrollTo(0,0);
        mWebView.setWebViewClient(new WebViewClient() {
            private WebView view;
            private WebResourceRequest request;

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view,
                                                              WebResourceRequest request) {
                Log.d(TAG, "webview shouldOverrideUrlLoading: Url = [" + request.getUrl()+"]");
                this.view = view;
                this.request = request;
                view.setBackgroundColor(getColor(android.R.color.holo_blue_dark));
                return assetLoader.shouldInterceptRequest(request.getUrl());

            }

            @Override //Always ACTION_UP
            public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
                Log.d(TAG, "onUnhandledKeyEvent: action = [" + event.getAction() +"] Keycode = [" + event.getKeyCode() + "]");
                int keyCode = event.getKeyCode();
                int action = event.getAction();

                if ((keyCode >= KEYCODE_0) && (keyCode <= KEYCODE_9) && (action == ACTION_DOWN))
                {
                    if(detailView.GetVisibility() == View.INVISIBLE)
                    {
                        OpenDigitalFilterList(keyCode);
                    }
                }
                else if(keyCode == KEYCODE_DPAD_DOWN || keyCode == KEYCODE_DPAD_UP)
                {
                    Log.d(TAG, "onUnhandledKeyEvent: curTpe =[" + ViewHistory.getCurChannel().getPlayStreamType()+"]"
                            +"preType = ["+ ViewHistory.getPreChannel().getPlayStreamType() + "]");

                    if(event.getKeyCode() == KEYCODE_DPAD_UP)
                    {
                        ViewHistory.setChannelUp();
                    }
                    else{
                        ViewHistory.setChannelDown();
                    }

                    if(ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.DVB_TYPE) {
                        StopPlayYoutubeandPlayDownChannelAV();
                    }
                    else if(ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE) {
                        StopPlayYoutubeVideoAndPlayVod();
                    }

                    Log.d(TAG, "onUnhandledKeyEvent: curTpe =[" + ViewHistory.getCurChannel().getPlayStreamType()+"]"
                            +"preType = ["+ ViewHistory.getPreChannel().getPlayStreamType() + "]");
                }
                else if(keyCode == KEYCODE_DPAD_LEFT)
                {
                    if (bannerView.getVisibility() == View.VISIBLE) {
                        if (epgEventGetPF == null)
                            return;
                        if ((detailView.GetVisibility() == INVISIBLE) && (epgEventGetPF.size() == 2)) {
                            bannerView.ChangeBannerEventTo(
                                    viewUiDisplay.EpgPreData.getEventName(),
                                    viewUiDisplay.EpgPreData.getEventType()
                            );
                            StartBannerTick();
                            Log.d(TAG, "onKeyDown: show present event");
                        }
                    }
                }else if(keyCode == KEYCODE_DPAD_RIGHT)
                {
                    if (bannerView.getVisibility() == View.VISIBLE) {
                        if (epgEventGetPF == null)
                            return;
                        if ((detailView.GetVisibility() == INVISIBLE) && (epgEventGetPF.size() == 2)) {
                            Log.d(TAG, "onKeyDown:" +
                                    " event name = " + viewUiDisplay.EpgFolData.getEventName() +
                                    " event type = " + viewUiDisplay.EpgFolData.getEventType()
                            );
                            bannerView.ChangeBannerEventTo(
                                    viewUiDisplay.EpgFolData.getEventName(),
                                    viewUiDisplay.EpgFolData.getEventType()
                            );
                            StartBannerTick();
                            Log.d(TAG, "onKeyDown: show follow event");
                        }
                    }
                }
                else if(keyCode == KEYCODE_INFO)
                {
                    if(bannerView.getVisibility() == VISIBLE)
                    {
                        viewUiDisplay.UpdateEpgPF();
                        String strInfo = ViewHistory.getCurChannel().getDetailInfo();
                        detailView.SetVisibility(View.VISIBLE, strInfo, 0, 100, 100, "");

                    }else {
                        ShowBanner(1);
                    }
                }
                else if((keyCode == KEYCODE_DPAD_CENTER) && (action == ACTION_UP))
                {
                    OpenOkFavList(KEYCODE_DPAD_CENTER);
                }
                return;
            }
        });

    }

    private void StartPlayYoutubeVideo(SimpleChannel channel)
    {
        String mimeType = "text/html";
        String encoding = "UTF-8";//"base64";
        //String USERAGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36";
        //String frameVideo = "<iframe width=\"" + mWebView.getWidth()+"\""
        //       +"height=\"" + mWebView.getHeight() +"\" " +
        //        "src=\"https://www.youtube.com/embed/lu_BJKxqGnk?&autoplay=1\" title=\"YouTube video player\" frameborder=\"0\" allow=\"autoplay; \" allowfullscreen></iframe>";
        //String frameVideo = "<iframe width=\"1080\" height=\"720\" src=\"https://www.youtube.com/embed/lu_BJKxqGnk?&autoplay=1\" frameborder=\"0\" allow=\"autoplay; \" allowfullscreen></iframe>";
        //String frameVideo = "<iframe width=\"1000\" height=\"600\" src=\"https://www.youtube.com/embed/lu_BJKxqGnk?&autoplay=1\" frameborder=\"0\" allow=\"autoplay; \" allowfullscreen></iframe>";

        //InitWebView();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;

        String frameVideo = channel.getUrl();
        Log.d(TAG, "webview StartPlayYoutubeVideo: Width = [" + width + "] height = [" + height +"]"
                + "x = [" + displaymetrics.xdpi +"] y = [" + displaymetrics.ydpi +"]");
        if(mWebView.getVisibility() == INVISIBLE) {
            Log.d(TAG, "webview Start StartPlayYoutubeVideo: ");
            mWebView.setVisibility(VISIBLE);

            mWebView.loadUrl(frameVideo);
            mWebView.loadDataWithBaseURL("", frameVideo, mimeType, encoding, "");
            mWebView.onResume();
        }

        playingChannelId = channel.getChannelId();
        setDefaultOpenChannel(channel.getChannelId(),channel.getType());
        DefaultChannel DefaultChannel = getDefaultOpenChannel();
        Log.e(TAG, "StartPlayYoutubeVideo: " + DefaultChannel.getChanneId());
    }

    private void StopPlayYoutubeVideoAndPlayVod()
    {
        Log.d(TAG, "webview StopPlayYoutubeVideoAndPlayVod: ");
        mWebView.setVisibility(INVISIBLE);
        mWebView.stopLoading();
        mWebView.onPause();

        PlayExoPlayerVideo(ViewHistory.getCurChannel());

    }

    private void StopPlayYoutubeVideo()
    {
        Log.d(TAG, "webview StopPlayYoutubeVideo: ");
        mWebView.setVisibility(INVISIBLE);
        mWebView.stopLoading();
        mWebView.onPause();

    }

    private void StopPlayYoutubeandPlayDownChannelAV()
    {
        Log.d(TAG, "webview StopPlayYoutubeandPlayDownChannelAV: ");
        mWebView.setVisibility(INVISIBLE);
        mWebView.stopLoading();
        mWebView.onPause();

        viewUiDisplay.ChangeProgram();
        ShowBanner(1);
    }


    private void InitExoPlayerVideo()
    {
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory());
        //mExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        SetExoPlayer(ExoPlayerFactory.newSimpleInstance(this, trackSelector));
        mExoPlayer = GetExoPlayer();
        mSurfaceViewExoplayer = (SurfaceView) findViewById(R.id.surfaceView_exoplayer);
        SetExoplayerSurfaceView(mSurfaceViewExoplayer);
        mSurfaceViewExoplayer = GetExoplayerSurfaceView();
        mSurfaceViewExoplayer.getHolder().setFormat(PixelFormat.TRANSPARENT);
        //mExoPlayer.setVideoSurfaceView(mSurfaceViewExoplayer);

//        PlayerView playerView = new PlayerView(this);
//        playerView.setKeepContentOnPlayerReset(true);

    }

    private void PlayExoPlayerVideo(SimpleChannel channel)
    {
        Log.d(TAG, "PlayExoPlayerVideo: ");
        //String VideoPath = ViewHistory.getCurChannel().getUrl();//VOD_VIDEO_URL;
        String VideoPath = channel.getUrl();
        Log.d(TAG, "PlayExoPlayerVideo: VideoPath = [" + VideoPath + "]");
        Uri videoUri = Uri.parse(VideoPath);
        Log.d(TAG, "PlayExoPlayerVideo: VideoPath = [" + VideoPath + "]");

        mSurfaceViewExoplayer.setVisibility(VISIBLE);
        DataSource.Factory dataSourceFactory = new DefaultHttpDataSourceFactory(Util.getUserAgent(getApplicationContext(), "ViewActivity"));
        HlsMediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                .setAllowChunklessPreparation(true)
                .createMediaSource(videoUri);
        mExoPlayer.setRepeatMode(REPEAT_MODE_ONE);
        mExoPlayer.setVideoSurfaceView(mSurfaceViewExoplayer);
        mExoPlayer.prepare(mediaSource);



        mExoPlayer.setPlayWhenReady(true);
        mExoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.d(TAG, "onPlayerStateChanged: playWhenReady = "+playWhenReady+" playbackState = "+playbackState);
                if(playbackState == Player.STATE_READY) {
                    if(bannerView.getVisibility() == INVISIBLE)
                        ShowBanner(1);
                }
            }

        });

        playingChannelId = channel.getChannelId();
        setDefaultOpenChannel(channel.getChannelId(),channel.getType());
        DefaultChannel DefaultChannel = getDefaultOpenChannel();
        Log.e(TAG, "StartPlayVOD: " + DefaultChannel.getChanneId());
    }

    /**
     * 避免系統檢查拋出異常
     */
    public static void checkWebView() {
        int sdkInt = Build.VERSION.SDK_INT;
        try {
//拿到 WebViewFactory 類
            Class<?> factoryClass = Class.forName("android.webkit.WebViewFactory");
//拿到類對應的 field
            Field field = factoryClass.getDeclaredField("sProviderInstance");
//field為private，設置為可訪問的
            field.setAccessible(true);
//拿到 WebViewFactory 的 sProviderInstance 實例
//sProviderInstance 是 static 類型，不需要傳入具體對象
            Object sProviderInstance = field.get(null);
            if (sProviderInstance != null) {
                return;
            }
            Method getProviderClassMethod;
            if (sdkInt > 22) {
                getProviderClassMethod = factoryClass.getDeclaredMethod("getProviderClass");
            } else if (sdkInt == 22) {
                getProviderClassMethod = factoryClass.getDeclaredMethod("getFactoryClass");
            } else {
                return;
            }
            getProviderClassMethod.setAccessible(true);
            Class<?> providerClass = (Class<?>) getProviderClassMethod.invoke(factoryClass);
            Class<?> delegateClass = Class.forName("android.webkit.WebViewDelegate");
            Constructor<?> providerConstructor = providerClass.getConstructor(delegateClass);
            if (providerConstructor != null) {
                providerConstructor.setAccessible(true);
                Constructor<?> declaredConstructor = delegateClass.getDeclaredConstructor();
                declaredConstructor.setAccessible(true);
//利用反射創建了 sProviderInstance
                sProviderInstance = providerConstructor.newInstance(declaredConstructor.newInstance());
//完成 sProviderInstance 賦值
                field.set("sProviderInstance", sProviderInstance);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void CheckVersion() {//Scoty 20181123 add check Apk and Service Version
        return;
// gary modify ,need porting software version check rule 
//        String[] apkVersion = GetApkSwVersion().split("\\.");
//        String[] serviceVersionSplit = GetPesiServiceVersion().split("V");
//        String[] serviceVersion = serviceVersionSplit[1].split("\\.");
//
//        if (apkVersion[1].equals(serviceVersion[0]) ||
//                Integer.valueOf(apkVersion[1]) == 0)//check service version
//        {
//            if (Integer.valueOf(serviceVersion[1]) <= Integer.valueOf(apkVersion[2]))//check api version
//                return;
//        }
//
//        new MessageDialog(this, 0) {
//            public void onSetMessage(View v) {
//                String Text = getString(R.string.STR_PLEASE_CHECK_APK_AND_SERVICE_VERSION) + "\n" +
//                        getString(R.string.STR_SW_VERSION) + " : " + GetApkSwVersion() + "\n" +
//                        getString(R.string.STR_SERVICE_VERSION) + " : " + GetPesiServiceVersion();
//                ((TextView) v).setText(Text);
//            }
//
//            public void onSetNegativeButton() {
//                finish();
//            }
//
//            public void onSetPositiveButton(int status) {
//                finish();
//            }
//
//            public void dialogEnd(int status) {
//                finish();
//            }
//        };
    }
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
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
//            if(ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.DVB_TYPE)
//                ChangeChTest_handler.postDelayed(Key_Test_Run, 1500);
//            else
                ChangeChTest_handler.postDelayed(Key_Test_Run, CHANGE_CHANNEL_TEST_DELAY);
        }
    };
    //==============================

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: = " + requestCode + " => resultCode " + resultCode);
        if (resultCode == FINISH_APK) {
            stopAllRec();//Scoty 20180720 add stop all records/timeshift
            AvControlPlayStop(MiscDefine.AvControl.AV_STOP_ALL);
            AvControlClose(ViewHistory.getPlayId());
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, sCreenOffTime);//Scoty 20181129 set Screen Saver Time

            finish();
            //super.onBackPressed();
        }
    }
/*
@Override
public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        Log.d( "jim test", "isInPictureInPictureMode = " + isInPictureInPictureMode + " config = " + newConfig.toString() ) ;
    if (isInPictureInPictureMode) {

    }
    else
    {

    }
}
*/
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: ");
        openMainMenu();
    }

    @Override
    public void onPause()
    {
        Log.d(TAG,"onPause !!!");
        super.onPause();
/*
        if (isInPictureInPictureMode()) { // jim test pip
            pipModSetDisplay(surfaceView.getHolder().getSurface());
            return ;
        }
*/
        SetEnterViewActivity(0);
        CloseCurSubtitle();//Scoty 20180621 modify when subtitle showing and then open oklist/menu/Guide should close subtitle; back to ViewActivity show subtitle
        //========   change channel test !!!!  =========
        if( TEST_MODE == 1) {
            removeAutoChangeCh();
        }
        //===============================
        //deinit_SpeechRecognizer(); // Edwin 20190510 disable speech recognizer
        mailIcon.setVisibility(INVISIBLE); // connie 20181116 for vmx mail
    }

    @Override
    public void onStop()
    {
        super.onStop();
        Log.d(TAG, "onStop: ");

        if(ViewHistory.getCurChannel() != null) {
            if(TvSharedPreference == null)
                setPersistData(this);

            AddNewFavoriteChannel();
            //UpdateNewFavoriteChannel();
            AddNewRecentlyWatchedChannel();
            AddNewWatchNextProgram();
            //GetFavoriteChannelInfofromDB();
            //GetFavoriteProgramInfofromDB();

        }
    }

    @Override
    public void onDestroy()
    {
        Log.d(TAG, "onDestroy:");
        super.onDestroy();

        //RecommendChannel Recommend = new RecommendChannel(ViewActivity.this);
        //Recommend.SetPesiLauncher(ViewHistory); //gary20190808 add set pesi launcher recommand data
        unSubBroadcast();   // Johnny add 20180313 for book //If use Android Emulator must mark
        if(CheckSignalHandler!=null)
        {
            CheckSignalHandler.removeCallbacks(CheckStatusRunnable);
            CheckSignalHandler = null;
        }
		
		if(BootAVPlayHandler!=null)
		{
			stopWaitPlaySuccess();
			BootAVPlayHandler.removeCallbacks(BootAVPlayRunnable);
			BootAVPlayHandler = null;
		}
    }

    static int preKeyCode;
    static long oldUpKeytime = 0;
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        long newUpKeyTime;
        newUpKeyTime = System.currentTimeMillis();
        if( Pvcfg.getCAType() == Pvcfg.CA_VMX && GetVMXBlockFlag() == 1) // for VMX need open/close
            return true;
        switch (keyCode) {
            case KEYCODE_CHANNEL_DOWN:
            case KEYCODE_CHANNEL_UP:
            case KEYCODE_DPAD_UP:
            case KEYCODE_DPAD_DOWN:
            {
                int pvrMode = getCurrentPvrMode();
                if (detailView.GetVisibility() == View.INVISIBLE) {
                    if(preKeyCode == keyCode && (newUpKeyTime - oldUpKeytime) <= repeatKeyLatency) {
                        //Log.d(TAG, "onKeyUp: return true");
                        return true;
                    }
                    // Johnny 20210423 temp for RTK avplay issue -s
                    // no video if we play two av too fast
                    // remove all "// Johnny 20210423 temp for RTK avplay issue" when fixed
                    else if (waitPlaySuccess || (ViewHistory.getCurChannel() != null && playingChannelId == ViewHistory.getCurChannel().getChannelId())) {
//                        Log.d(TAG, "johnny onKeyUp: av not ready or same channel, stop keyup");
                        return true;
                    }
                    // Johnny 20210423 temp for RTK avplay issue -e
                    else {
                        //Log.d(TAG, "onKeyUp: set channel ");
                        //Scoty 20180712 for pip -s//Scoty 20180620 modify same channel change program should check av status -s
                        //if(getCurrentPvrMode() == PvrInfo.EnPVRMode.NO_ACTION)//if(PvrGetCurrentRecMode() == NO_ACTION)
                        //{
                            if(ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.YOUTUBE_TYPE) {//Play Webview Youtube
                                //AvControlPlayStop(0);
                                if(ViewHistory.getPreChannel() != null && ViewHistory.getPreChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE) {
                                    mExoPlayer.stop(true);
                                    mExoPlayer.clearVideoSurfaceView(mSurfaceViewExoplayer);
                                    mSurfaceViewExoplayer.setVisibility(INVISIBLE);
                                }else if(ViewHistory.getPreChannel() != null && ViewHistory.getPreChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.YOUTUBE_TYPE) {
                                    StopPlayYoutubeVideo();
                                }
                                else{
                                    AvControlPlayStop(ViewHistory.getPlayId());
                                }
                                //PlayExoPlayerVideo();
                                StartPlayYoutubeVideo(ViewHistory.getCurChannel());
                                ShowBanner(1);
                            }else if(ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE) //Play HLS
                            {
                                if(ViewHistory.getPreChannel() != null && ViewHistory.getPreChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.YOUTUBE_TYPE) {
                                    StopPlayYoutubeVideo();
                                }else if(ViewHistory.getPreChannel() != null && ViewHistory.getPreChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE) {
                                    mExoPlayer.stop(true);
                                    mExoPlayer.clearVideoSurfaceView(mSurfaceViewExoplayer);
                                    mSurfaceViewExoplayer.setVisibility(INVISIBLE);
                                }
                                else{
                                    AvControlPlayStop(ViewHistory.getPlayId());
                                }
                                PlayExoPlayerVideo(ViewHistory.getCurChannel());
                                ShowBanner(1);
                            }
                            else
                            {
                                //if(mExoPlayer.getPlaybackState() == Player.STATE_BUFFERING)
                                //    mExoPlayer.stop();
                                //if(mWebView)
                                //    StopPlayYoutubeVideo();
//
//                            if(ViewHistory.getPreChannel().getChannelId() == 1234567899) {
//                                mExoPlayer.stop(true);
//                                mExoPlayer.clearVideoSurfaceView(mSurfaceViewExoplayer);
//                                mSurfaceViewExoplayer.setVisibility(INVISIBLE);
//                            }

//                            Log.d(TAG, "onKeyUp: cur = ["+ViewHistory.getCurChannel().getChannelId()+"] pre = ["
//                                    + ViewHistory.getPreChannel().getChannelId()+"] playState = [" + mExoPlayer.getPlaybackState()+"]");
                                if((ViewHistory.getPreChannel() != null && ViewHistory.getPreChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE)
                                        || mExoPlayer.getPlaybackState() == Player.STATE_READY)
                                {
                                    mExoPlayer.stop(true);
                                    mExoPlayer.clearVideoSurfaceView(mSurfaceViewExoplayer);
                                    mSurfaceViewExoplayer.setVisibility(INVISIBLE);
                                }else if(ViewHistory.getPreChannel() != null && ViewHistory.getPreChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.YOUTUBE_TYPE)
                                {
                                    StopPlayYoutubeVideo();
                                }

                                if(ViewHistory.getCurPipChannel() != null && pipFrame.getVisibility() == View.VISIBLE) {
                                viewUiDisplay.ChangePipProgram();
                                SetPipColor();
                            }
                            else {
                                int AVplayStatus = AvControlGetPlayStatus(ViewHistory.getPlayId());

                                if(ViewHistory.getCurPipChannel() != null)
                                {
                                    if((ViewHistory.getCurPipChannel().getChannelId() == ViewHistory.getCurChannel().getChannelId()) ||
                                       (ViewHistory.getCurPipChannel().getTpId() != ViewHistory.getCurChannel().getTpId()))
                                    {
                                        viewUiDisplay.ClosePipScreen();
                                        pipFrame.SetVisibility(View.INVISIBLE);
                                    }
                                }
                                //Scoty 20180713 modify when current channel Recording show rec icon
//                                if(mRecImageView != null) {
//                                    if (getCurrentPvrMode() == PvrInfo.EnPVRMode.NO_ACTION)
//                                        mRecImageView.SetVisibility(false);
//                                    else
//                                        mRecImageView.SetVisibility(true);
//                                }

                                if (ViewHistory.getCurChannelList() != null && ViewHistory.getCurChannelList().size() > 1) // Johnny 20180912 fix crash when no cur channel
                                {
                                    if(!CheckPvrMode(pvrMode)) //Scoty 20180827 add and modify TimeShift Live Mode//Scoty 20180713 fixed timeshift with av play at the same time
                                    {
                                        E16Msg.SetVisibility(this, View.INVISIBLE); // Edwin 20181129 disable e16 message after change channel
                                        viewUiDisplay.ChangeProgram();
                                    }
                                }
                                else
                                {
                                    if (AVplayStatus == HiDtvMediaPlayer.EnPlayStatus.STOP.getValue())
                                    {
                                        E16Msg.SetVisibility(this, View.INVISIBLE); // Edwin 20181129 disable e16 message after change channel
                                        viewUiDisplay.ChangeProgram();
                                    }
                                }
                                CurSubtitleIndex = 0;//Scoty 20180621 modify when subtitle showing and then open oklist/menu/Guide should close subtitle; back to ViewActivity show subtitle
                            }

                            startWaitPlaySuccess(); // Johnny 20210423 temp for RTK avplay issue
                        }
                        //Scoty 20180712 for pip -e//Scoty 20180620 modify same channel change program should check av status -e
                        return true;
                    }
                }
            }break;
        }
        return super.onKeyUp(keyCode,event);
    }

    static long oldDownKeytime = 0;
    @Override
    public boolean onKeyDown(final int keyCode, KeyEvent event) {
        System.out.println( "keycoode=" + keyCode );
        Log.d(TAG, "onKeyDown: keyCode = "+ keyCode);
        long newDownKeyTime;
        preKeyCode = keyCode;
        newDownKeyTime = System.currentTimeMillis();

        if( Pvcfg.getCAType() == Pvcfg.CA_VMX && GetVMXBlockFlag() == 1) // for VMX need open/close
        {
            if(keyCode != KeyEvent.KEYCODE_MENU && keyCode != KeyEvent.KEYCODE_BACK)
            {
                return true;
            }
        }
        if(keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN ||
                keyCode == KeyEvent.KEYCODE_CHANNEL_UP ||keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KEYCODE_GUIDE_PESI ||
                keyCode == KeyEvent.KEYCODE_TV_RADIO_SERVICE ||
                keyCode == ExtKeyboardDefine.KEYCODE_GUIDE_PESI ||
                keyCode == ExtKeyboardDefine.KEYCODE_TV_RADIO_SERVICE) {  // Johnny 20181210 for keyboard control

            if ((keyCode == KEYCODE_GUIDE_PESI) || (keyCode == KeyEvent.KEYCODE_MENU) || keyCode == ExtKeyboardDefine.KEYCODE_GUIDE_PESI) {  // Johnny 20181210 for keyboard control
                //Scoty 20180712 for pip
                if (ViewHistory.getCurPipChannel() != null) {
                    viewUiDisplay.ClosePipScreen();
                    pipFrame.SetVisibility(View.INVISIBLE);
                } else if (mCaMessagView != null)//Scoty 20180725 fixed no signal message can not disappear
                {
                    mCaMessagView.SetVisibility(View.INVISIBLE);
                }
            }
            //Scoty 20180809 modify dual pvr rule -s
            final int PvrMode = getCurrentPvrMode();//final int tmpPvrMode = PvrGetCurrentRecMode();
            if (Pvcfg.getPVR_PJ()) { // connie 20181024 for can't goto menu when pvr flag not open
                if (PvrMode != PvrInfo.EnPVRMode.NO_ACTION) {
                    if (detailView.getVisibility() == View.VISIBLE) // connie 20180525 fix don't show stop record msg in detail dialog
                        return true;
                    final int tmpKeyCode = keyCode;
                    final KeyEvent tmpEvent = event;
                    if ((keyCode == KEYCODE_GUIDE_PESI) || (keyCode == KeyEvent.KEYCODE_MENU) || (keyCode == KeyEvent.KEYCODE_TV_RADIO_SERVICE)
                            || keyCode == ExtKeyboardDefine.KEYCODE_GUIDE_PESI
                            || keyCode == ExtKeyboardDefine.KEYCODE_TV_RADIO_SERVICE) {   // Johnny 20181210 for keyboard control
                        new SureDialog(this) {
                            public void onSetMessage(View v) {
                                ((TextView) v).setText(getString(R.string.STR_DO_YOU_WANT_TO_STOP_ALL_RECORDS_AND_TIMESHIFT));
                            }

                            public void onSetNegativeButton() {
                            }

                            public void onSetPositiveButton() {
                                stopAllRec();
                                NextKeyEvent(tmpKeyCode, tmpEvent);//eric lin 20180716 delay for timeshift status changed

                            }
                        };

                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN || keyCode == KeyEvent.KEYCODE_CHANNEL_UP ||
                            keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (CheckPvrMode(PvrMode))//Scoty 20180827 add and modify TimeShift Live Mode
                        {
                            new SureDialog(this) {
                                public void onSetMessage(View v) {
                                    ((TextView) v).setText(getString(R.string.STR_DO_YOU_WANT_TO_STOP_TIMESHIFT));
                                }

                                public void onSetNegativeButton() {
                                }

                                public void onSetPositiveButton() {
                                    stopPVRMode(PvrMode, 0, 1);
                                    NextKeyEvent(tmpKeyCode, tmpEvent);//eric lin 20180716 delay for timeshift status changed
                                }
                            };
                            return true;
                        }
                    }

                }
            }
        }
        //Scoty 20180809 modify dual pvr rule -e
        if(detailView.GetVisibility() == View.INVISIBLE) {
            if ((keyCode >= KEYCODE_0) && (keyCode <= KEYCODE_9))
            {
                OpenDigitalFilterList(keyCode);
            }
        }

        switch (keyCode) {
            case KEYCODE_MENU:
                Log.d(TAG, "onKeyDown: MENU");
                if(detailView.GetVisibility() == View.INVISIBLE) {
                    if (bannerView.GetVisibility() == View.VISIBLE) {
                        CloseBanner();//Scoty 20180621 modify close banner as function// connie 20180524 fix channel num show wrong in fav group
                    }

                    openMainMenu();
                }
                break;
            case KEYCODE_CHANNEL_DOWN:
            case KEYCODE_CHANNEL_UP:
            case KEYCODE_DPAD_UP:
            case KEYCODE_DPAD_DOWN:
                Log.d(TAG, "onKeyDown: UP or DOWN");
                if (detailView.GetVisibility() == View.INVISIBLE) {
                    //Log.d("TAG", "KEYCODE_DPAD_UP: latency == " + (newDownKeyTime - oldDownKeytime));
                    //if repeat key less than key latency, no need update banner
                    //Scoty 20180712 for pip -s
                    if(preKeyCode == keyCode && (newDownKeyTime - oldDownKeytime) <= repeatKeyLatency) {
                        return true;
                    }
                    // Johnny 20210423 temp for RTK avplay issue -s
                    else if (waitPlaySuccess) {
//                        Log.d(TAG, "johnny onKeyDown: av not ready, stop keydown");
                        return true;
                    }
                    // Johnny 20210423 temp for RTK avplay issue -e
                    else
                    {
                        if (keyCode == KEYCODE_DPAD_UP || keyCode == KEYCODE_CHANNEL_UP) {
                            if(ViewHistory.getCurPipChannel() != null && pipFrame.getVisibility() == View.VISIBLE) {
                                viewUiDisplay.ChangePipChannelUp();
                            }
                            else
                                viewUiDisplay.ChangeBannerInfoUp();
                        }
                        else {
                            if(ViewHistory.getCurPipChannel() != null && pipFrame.getVisibility() == View.VISIBLE)
                                viewUiDisplay.ChangePipChannelDown();
                            else
                                viewUiDisplay.ChangeBannerInfoDown();
                        }
                        oldDownKeytime = newDownKeyTime;
                    }
                    //Scoty 20180620 modify change to same channel should show full banner info -s
                    if(ViewHistory.getCurPipChannel() != null && pipFrame.getVisibility() == View.VISIBLE) {
                        ShowBanner(1);
                    }
                    else {
                        if(ViewHistory.getCurChannelList().size() > 1)
                            ShowBanner(1);
                        else {
                            int AVplayStatus = AvControlGetPlayStatus(ViewHistory.getPlayId());
                            if (AVplayStatus == HiDtvMediaPlayer.EnPlayStatus.STOP.getValue())
                                ShowBanner(0);
                            else
                                ShowBanner(1);
                        }
                    }
                    //Scoty 20180712 for pip -e//Scoty 20180712 for pip -e//Scoty 20180620 modify change to same channel should show full banner info -e
                    return true;
                }
                break;
            case KEYCODE_DPAD_LEFT:
                Log.d(TAG, "onKeyDown: LEFT");
                if (bannerView.getVisibility() == View.VISIBLE) {
                    if (epgEventGetPF == null)
                        break;
                    if((detailView.GetVisibility() == INVISIBLE) && (epgEventGetPF.size() == 2)) {
                        bannerView.ChangeBannerEventTo(
                                viewUiDisplay.EpgPreData.getEventName(),
                                viewUiDisplay.EpgPreData.getEventType()
                        );
                        StartBannerTick();
                        Log.d(TAG, "onKeyDown: show present event");
                        return true;
                    }
                    else if (detailView.GetVisibility() == VISIBLE)
                    {
                        String strInfo = null;
                        if (epgHandle.GetShowEventType() != EPGEvent.EPG_TYPE_PRESENT) {
                            strInfo = epgHandle.GetCurPorgramDetail(EPGEvent.EPG_TYPE_PRESENT);
                            epgHandle.SetShowEventType(EPGEvent.EPG_TYPE_PRESENT);
                            detailView.UpdateDetailInfo(strInfo);
                        }
                    }
                }
                else {
                    int PvrMode = getCurrentPvrMode();
                    if (detailView.GetVisibility() == View.INVISIBLE &&
                            (!CheckPvrMode(PvrMode)))
                    {//Scoty 20180827 add and modify TimeShift Live Mode//Scoty 20180801 fixed subtitle can not open when no Timeshift//eric lin 20180629 when timeshfit not execute aud/txt/sub key function
                        Log.d(TAG, "onKeyDown: Subtitle Dialog");
                        //Scoty 20180712 for pip

                        if (ViewHistory.getCurPipChannel() != null && pipFrame.getVisibility() == View.VISIBLE)
                            return true;

                        final SubtitleInfo Subtitle = AvControlGetSubtitleList(ViewHistory.getPlayId());
                        if (Subtitle == null) {
                            new MessageDialogView(mContext, getString(R.string.STR_NO_SUBTITLE), 3000) {
                                public void dialogEnd() {
                                }
                            }.show();
                            break;
                        }
                        subtitleDialog = new SubtitleDialogView(
                                mContext,
                                Subtitle,
                                new SubtitleDialogView.OnSubtitleClickedListener() {
                                    @Override
                                    public void SubtitleClicked() {
                                        AvControlSelectSubtitle(ViewHistory.getPlayId(), Subtitle.getComponent(Subtitle.getCurPos()));
                                        CurSubtitleIndex = Subtitle.getCurPos();//Scoty 20180621 modify when subtitle showing and then open oklist/menu/Guide should close subtitle; back to ViewActivity show subtitle
                                    }
                                }
                        );
                        //subtitleDialog.show();
                        new android.os.Handler().postDelayed(new Runnable() { // Edwin 20190509 fix dialog not focus
                            @Override
                            public void run () {
                                subtitleDialog.show();
                            }
                        }, 150);

                    }
                }
                Log.d(TAG, "onKeyDown: LEFT key");
                break;
            case KEYCODE_DPAD_RIGHT:
                Log.d(TAG, "onKeyDown: RIGHT");
                if (bannerView.getVisibility() == View.VISIBLE) {
                    if (epgEventGetPF == null)
                        break;
                    if ((detailView.GetVisibility() == INVISIBLE) && (epgEventGetPF.size() == 2)) {
                        Log.d(TAG, "onKeyDown:" +
                                " event name = " + viewUiDisplay.EpgFolData.getEventName() +
                                " event type = " + viewUiDisplay.EpgFolData.getEventType()
                        );
                        bannerView.ChangeBannerEventTo(
                                viewUiDisplay.EpgFolData.getEventName(),
                                viewUiDisplay.EpgFolData.getEventType()
                        );
                        StartBannerTick();
                        Log.d(TAG, "onKeyDown: show follow event");
                        return true;
                    }
                    else if (detailView.GetVisibility() == View.VISIBLE) {
                        String strInfo = null;
                        if(epgHandle.GetShowEventType() != EPGEvent.EPG_TYPE_FOLLOW) {
                            strInfo = epgHandle.GetCurPorgramDetail(EPGEvent.EPG_TYPE_FOLLOW);
                            epgHandle.SetShowEventType(EPGEvent.EPG_TYPE_FOLLOW);
                            detailView.UpdateDetailInfo(strInfo);
                        }
                    }
                }
                else {
                    int pvrMode = getCurrentPvrMode();
                    if (detailView.GetVisibility() == View.INVISIBLE &&
                            (!CheckPvrMode(pvrMode))) {//Scoty 20180827 add and modify TimeShift Live Mode//Scoty 20180801 fixed audio should not open when Timeshift//eric lin 20180629 when timeshfit not execute aud/txt/sub key function
                        Log.d(TAG, "onKeyDown: Audio Dialog");
                        //Scoty 20180712 for pip
                        if (ViewHistory.getCurPipChannel() != null && pipFrame.getVisibility() == View.VISIBLE)
                            return true;

                        final AudioInfo AudioComp = AvControlGetAudioListInfo(ViewHistory.getPlayId());
                        EnAudioTrackMode curTrackMode = AvControlGetTrackMode(ViewHistory.getPlayId());

                        // dialog will set curPos of AudioComp and pass selectTrackMode back, use these to do AvControl
                        if (AudioComp != null) {
                            audioDialogView = new AudioDialogView(
                                    mContext,
                                    0,//eric lin 20180720 add file play audio dialog
                                    curTrackMode,
                                    AudioComp,
                                    new AudioDialogView.OnAudioClickListener() {
                                        public void AudioClicked() {
                                            AvControlChangeAudio(ViewHistory.getPlayId(), AudioComp.getComponent(AudioComp.getCurPos()));
                                        }
                                    }
                            );

                            // Edwin 20190508 fix dialog not focus -s
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run () {
                                    audioDialogView.show();
                                }
                            }, 200); // Edwin 20190515 prevent from dialog has no focus
                            // Edwin 20190508 fix dialog not focus -e
                        }
                    }
                }

                Log.d(TAG, "onKeyDown: RIGHT key");
                break;
            case KEYCODE_INFO:
            case ExtKeyboardDefine.KEYCODE_INFO:    // Johnny 20181210 for keyboard control
                Log.d(TAG, "onKeyDown: INFO");
                if (bannerView.GetVisibility() == View.INVISIBLE) {
                    ShowBanner(1);
                    Log.d(TAG, "onKeyDown: show banner");
                }
                else { // bannerView.GetVisibility() == View.VISIBLE
                    boolean focusPipFlag = isFocusPip();
                    int pvrMode = -1;
                    if (focusPipFlag)
                        pvrMode = getCurrentPipPvrMode();
                    else
                        pvrMode = getCurrentPvrMode();

                    if (detailView.GetVisibility() == View.VISIBLE) { // close Epg Detail Dialog
                        detailView.SetVisibility(View.INVISIBLE, null, 0, 0, 0, "");
                        if (Pvcfg.getPVR_PJ() == true && pvrMode != PvrInfo.EnPVRMode.NO_ACTION)//if( PvrGetCurrentRecMode() != NO_ACTION) // connie 20181101 for pvr banner cant show when pvr not support
                            pvrBanner.setVisibility(mDTVActivity, VISIBLE, RecordDuration, focusPipFlag);//Scoty 20180802 fixed cur time not update when focus on Pip
                        StartBannerTick();
                    } else { // Show Epg Detail Dialog
                        //Scoty 20180712 for pip
                        if (focusPipFlag)
                            viewUiDisplay.UpdatePipEpgPF();
                        else
                            viewUiDisplay.UpdateEpgPF();
                        String strInfo = epgHandle.GetCurPorgramDetail(EPGEvent.EPG_TYPE_PRESENT);
                        epgHandle.SetShowEventType(EPGEvent.EPG_TYPE_PRESENT);
                        if (pvrBanner.getVisibility() == VISIBLE)
                            pvrBanner.setVisibility(mDTVActivity, INVISIBLE, 0, focusPipFlag);//Scoty 20180802 fixed cur time not update when focus on Pip

                        int streamtype = ViewHistory.getCurChannel().getPlayStreamType();
                        if(streamtype == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE)
                        {
                            detailView.SetVisibility(View.VISIBLE, strInfo, 0, 100, 100, "NO BER");
                        }
                        else if(streamtype == PROGRAM_PLAY_STREAM_TYPE.YOUTUBE_TYPE)
                        {
                            detailView.SetVisibility(View.VISIBLE, strInfo, 0, 100, 100, "No BER");
                        }
                        else
                        detailView.SetVisibility(View.VISIBLE, strInfo, TunerGetLockStatus(0), TunerGetStrength(0), TunerGetQuality(0), TunerGetBER(0));
                    }
                }
                return true;
//            case KeyEvent.KEYCODE_VOLUME_MUTE:
//                VolumeMute();
//                Log.d(TAG, "onKeyDown: MUTE key");
//                break;
            case KEYCODE_TV_RADIO_SERVICE:
            case ExtKeyboardDefine.KEYCODE_TV_RADIO_SERVICE:   // Johnny 20181210 for keyboard control
                if (detailView.getVisibility() == View.INVISIBLE) {
                    //Scoty 20180712 for pip
                    if (ViewHistory.getCurPipChannel() != null) {
                        if (pipFrame.getVisibility() == View.VISIBLE)
                            return true;
                        else
                            {
                            viewUiDisplay.ClosePipScreen();
                            pipFrame.SetVisibility(View.INVISIBLE);
                        }
                    }
                    TvRadioSwitch();
                }
                Log.d(TAG, "onKeyDown: TV/Radio key");
                break;
            case -2:
                Log.d(TAG, "onKeyDown: Ratio Dialog");
                final GposInfo gpos = GposInfoGet();
                new RatioDialogView(ViewActivity.this, gpos.getScreen16x9(), gpos.getResolution(), gpos.getConversion(), gpos.getDolbyMode()) {
                    public void onSetNegativeButton() {
                    }

                    public void onSetPositiveButton(int ratio, int conversion, int audoutput/*, int resolution*/) {
                        AvControlChangeRatioConversion(ViewHistory.getPlayId(), ratio, conversion);
                        //AvControlChangeRatio(viewUiDisplay.History.getPlayId(),ratio);
                        //AvControlChangeConversion(viewUiDisplay.History.getPlayId(),conversion);
                        AvControlAudioOutput(ViewHistory.getPlayId(), audoutput);
                        gpos.setScreen16x9(ratio);
                        gpos.setResolution(conversion);
                        gpos.setDolbyMode(audoutput);
                        GposInfoUpdate(gpos);
                    }
                };
                break;
            case KEYCODE_MEDIA_AUDIO_TRACK:
            case ExtKeyboardDefine.KEYCODE_MEDIA_AUDIO_TRACK:   // Johnny 20181210 for keyboard control
            {
                if(ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE
                        || ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.YOUTUBE_TYPE)
                    return true;

                int pvrMode = getCurrentPvrMode();
                if (detailView.GetVisibility() == View.INVISIBLE &&
                        (!CheckPvrMode(pvrMode))) {//Scoty 20180827 add and modify TimeShift Live Mode//Scoty 20180801 fixed audio should not open when Timeshift//eric lin 20180629 when timeshfit not execute aud/txt/sub key function
                    Log.d(TAG, "onKeyDown: Audio Dialog");
                    //Scoty 20180712 for pip
                    if (ViewHistory.getCurPipChannel() != null && pipFrame.getVisibility() == View.VISIBLE)
                        return true;

                    final AudioInfo AudioComp = AvControlGetAudioListInfo(ViewHistory.getPlayId());
                    EnAudioTrackMode curTrackMode = AvControlGetTrackMode(ViewHistory.getPlayId());

                    // dialog will set curPos of AudioComp and pass selectTrackMode back, use these to do AvControl
                    if (AudioComp != null) {
                        audioDialogView = new AudioDialogView(
                                mContext,
                                0,//eric lin 20180720 add file play audio dialog
                                curTrackMode,
                                AudioComp,
                                new AudioDialogView.OnAudioClickListener() {
                                    public void AudioClicked() {
                                        AvControlChangeAudio(ViewHistory.getPlayId(), AudioComp.getComponent(AudioComp.getCurPos()));
                                    }
                                }
                        );

                        // Edwin 20190508 fix dialog not focus -s
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run () {
                                audioDialogView.show();
                            }
                        }, 200); // Edwin 20190515 prevent from dialog has no focus
                        // Edwin 20190508 fix dialog not focus -e
                    }
                }
            }break;

            case KEYCODE_TV_TELETEXT:
            case ExtKeyboardDefine.KEYCODE_TV_TELETEXT:   // Johnny 20181210 for keyboard control
            {
                int PvrMode = getCurrentPvrMode();
                //Scoty 20180712 for pip
                if (ViewHistory.getCurPipChannel() != null && pipFrame.getVisibility() == View.VISIBLE)
                    return true;
                if (!CheckPvrMode(PvrMode))//Scoty 20180827 add and modify TimeShift Live Mode//eric lin 20180629 when timeshfit not execute aud/txt/sub key function
                    showTeletextDialog();
            }break;

            case KEYCODE_CAPTIONS:
            case ExtKeyboardDefine.KEYCODE_CAPTIONS:    // Johnny 20181210 for keyboard control
            { // subtitle key
                if(ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE
                        || ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.YOUTUBE_TYPE)
                    return true;

                int PvrMode = getCurrentPvrMode();
                if (detailView.GetVisibility() == View.INVISIBLE &&
                        (!CheckPvrMode(PvrMode))) {//Scoty 20180827 add and modify TimeShift Live Mode//Scoty 20180801 fixed subtitle can not open when no Timeshift//eric lin 20180629 when timeshfit not execute aud/txt/sub key function
                    Log.d(TAG, "onKeyDown: Subtitle Dialog");
                    //Scoty 20180712 for pip
                    if (ViewHistory.getCurPipChannel() != null && pipFrame.getVisibility() == View.VISIBLE)
                        return true;
                    final SubtitleInfo Subtitle = AvControlGetSubtitleList(ViewHistory.getPlayId());
                    if (Subtitle == null) {
                        new MessageDialogView(mContext, getString(R.string.STR_NO_SUBTITLE), 3000) {
                            public void dialogEnd() {
                            }
                        }.show();
                        break;
                    }
                    subtitleDialog = new SubtitleDialogView(
                            mContext,
                            Subtitle,
                            new SubtitleDialogView.OnSubtitleClickedListener() {
                                @Override
                                public void SubtitleClicked() {
                                    AvControlSelectSubtitle(ViewHistory.getPlayId(), Subtitle.getComponent(Subtitle.getCurPos()));
                                    CurSubtitleIndex = Subtitle.getCurPos();//Scoty 20180621 modify when subtitle showing and then open oklist/menu/Guide should close subtitle; back to ViewActivity show subtitle
                                }
                            }
                    );
                    //subtitleDialog.show();
                    new android.os.Handler().postDelayed(new Runnable() { // Edwin 20190509 fix dialog not focus
                        @Override
                        public void run () {
                            subtitleDialog.show();
                        }
                    }, 150);
                }
            }break;

            case -3:
                Log.d(TAG, "onKeyDown: start EPG");
                Intent itEpg = new Intent();
                itEpg.setClass(mContext, EpgActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("type", ViewHistory.getCurGroupType());
                itEpg.putExtras(bundle);
                startActivity(itEpg, bundle);
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY://Scoty 20181106 add for separate Play and Pause key
            {
                if (Pvcfg.getPVR_PJ()) {
                    final int PvrMode = getCurrentPvrMode();
                    if (CheckPvrMode(PvrMode))
                    {
                        if (PvrMode == PvrInfo.EnPVRMode.PVRMODE_TIMESHIFT_LIVE_PAUSE)
                        {
                            PvrTimeShiftPlay(ViewHistory.getPlayId());
                        }
                        else if (PvrMode == PvrInfo.EnPVRMode.TIMESHIFT_FILE ||
                                PvrMode == PvrInfo.EnPVRMode.TIMESHIFT_LIVE)
                        {
                            PvrTimeShiftResume(ViewHistory.getPlayId());
                        }
                        ShowBanner(1);
                    }
                }
            }break;
            case KeyEvent.KEYCODE_MEDIA_PAUSE://Scoty 20181106 add for separate Play and Pause key
            {
                if (Pvcfg.getPVR_PJ()) {
                    final int PvrMode = getCurrentPvrMode();

                    if ( ! TunerIsLock(0) )
                        break;

                    if (!CheckUsbPathAvailable(GetRecordPath())) // connie 20181024 for USB Path wrong
                    {
                        StorageNotAvailableDialog.show();
                        break;
                    }

                    if (PvrMode == PvrInfo.EnPVRMode.NO_ACTION)
                    {
                        //String path = GetRecordPath() + timeshiftFileName;
                        //PvrTimeShiftStart(ViewHistory.getPlayId(), GposInfoGet().getTimeshiftDuration(), 0, path);
                        TimeShift_Start_V2(GposInfoGet().getTimeshiftDuration(), 0, false, null);
                        new MessageDialogView(mContext, getString(R.string.STR_TIMESHIFT_MODE_START), 3000)
                        {
                            public void dialogEnd()
                            {
                                ShowBanner(1);
                            }
                        }.show();
                    }
                    else if (CheckPvrMode(PvrMode)) {
                        if (PvrMode == PvrInfo.EnPVRMode.TIMESHIFT_FILE)
                        {
                            PvrTimeShiftPause(ViewHistory.getPlayId());
                        }
                        else if (PvrMode == PvrInfo.EnPVRMode.TIMESHIFT_LIVE)
                        {
                            pvrTimeShiftLivePause(ViewHistory.getPlayId());
                        }
                        ShowBanner(1);
                    }
                    else if (PvrMode == PvrInfo.EnPVRMode.RECORD) {
                        new SureDialog(ViewActivity.this)
                        {
                            public void onSetMessage(View v)
                            {
                                ((TextView) v).setText(getString(R.string.STR_DO_YOU_WANT_TO_STOP_RECORD));
                            }

                            public void onSetNegativeButton()
                            {
                            }

                            public void onSetPositiveButton()
                            {
                                int recId = PvrRecordCheck(ViewHistory.getCurChannel().getChannelId());//Scoty 20180809 modify dual pvr rule
                                stopPVRMode(PvrMode, recId, 1);//Scoty 20180809 modify dual pvr rule//eric lin 20180629 stop pvr and av play //why 1, because enter invalid channel number not call AvControlPlayByChannelId
                                //String path = GetRecordPath() + timeshiftFileName;
                                //PvrTimeShiftStart(ViewHistory.getPlayId(), GposInfoGet().getTimeshiftDuration(), 0, path);
                                TimeShift_Start_V2(GposInfoGet().getTimeshiftDuration(), 0, false, null);
                                ShowBanner(1);
                            }
                        };
                    }
                }

            }break;
            case KEYCODE_MEDIA_PLAY_PAUSE:
            case ExtKeyboardDefine.KEYCODE_MEDIA_PLAY_PAUSE:    // Johnny 20181210 for keyboard control
                Log.d(TAG, "onKeyDown: PLAY/PAUSE");
                //Scoty 20180712 for pip
                if (ViewHistory.getCurPipChannel() != null /*&& pipFrame.getVisibility() == View.VISIBLE*/)//Scoty 20180802 modify not Timeshift when Pip
                    return true;
                //Scoty 20180713 start record/timeshift should stop pre rec/timeshift when recording/timeshift on the same channel -s
                final int PvrMode = getCurrentPvrMode();
                if (Pvcfg.getPVR_PJ()) //eric lin 20180703 add pvcfg
                {
                    if ( ! TunerIsLock(0) )
                        break;

                    // edwin 20180802 block time shift -s
                    if ( !CheckUsbPathAvailable(GetRecordPath()) ) // connie 20181024 for USB Path wrong
                    {
                        StorageNotAvailableDialog.show();
                        break;
                    }
                    // edwin 20180802 block time shift -e

                    if (PvrMode == PvrInfo.EnPVRMode.NO_ACTION)//if(PvrGetCurrentRecMode() == NO_ACTION)
                    {
                        //String path = GetRecordPath() + timeshiftFileName;
                        //PvrTimeShiftStart(ViewHistory.getPlayId(), GposInfoGet().getTimeshiftDuration(), 0, path);
                        TimeShift_Start_V2(GposInfoGet().getTimeshiftDuration(), 0, false, null);
                        new MessageDialogView(mContext, getString(R.string.STR_TIMESHIFT_MODE_START), 3000)
                        {
                            public void dialogEnd()
                            {
                                ShowBanner(1);
                            }
                        }.show();
                        //mRecImageView = new RecImageView(ViewActivity.this);//Scoty 20180629 show Rec Icon on top
                        //mRecImageView.SetVisibility(true);
                    }
                    else if (CheckPvrMode(PvrMode))//Scoty 20180827 add and modify TimeShift Live Mode -s
                    {
                        if (PvrMode == PvrInfo.EnPVRMode.PVRMODE_TIMESHIFT_LIVE_PAUSE)
                        {
                            PvrTimeShiftPlay(ViewHistory.getPlayId());
                        }
                        else if (PvrMode == PvrInfo.EnPVRMode.TIMESHIFT_FILE)
                        {
                            pvrTimeShiftFilePause(ViewHistory.getPlayId());
                        }
                        else if (PvrMode == PvrInfo.EnPVRMode.TIMESHIFT_LIVE)
                        {
                            pvrTimeShiftLivePause(ViewHistory.getPlayId());
                        }
                        ShowBanner(1);
                        //Scoty 20180827 add and modify TimeShift Live Mode -e
                    }
                    else if (PvrMode == PvrInfo.EnPVRMode.RECORD)
                    {
                        new SureDialog(ViewActivity.this)
                        {
                            public void onSetMessage(View v)
                            {
                                ((TextView) v).setText(getString(R.string.STR_DO_YOU_WANT_TO_STOP_RECORD));
                            }

                            public void onSetNegativeButton()
                            {
                            }

                            public void onSetPositiveButton()
                            {
                                //Log.d(TAG, "onSetPositiveButton: TOST chnum OK key");
                                int recId = PvrRecordCheck(ViewHistory.getCurChannel().getChannelId());//Scoty 20180809 modify dual pvr rule
                                stopPVRMode(PvrMode, recId, 1);//Scoty 20180809 modify dual pvr rule//eric lin 20180629 stop pvr and av play //why 1, because enter invalid channel number not call AvControlPlayByChannelId
                                //String path = GetRecordPath() + timeshiftFileName;
                                //PvrTimeShiftStart(ViewHistory.getPlayId(), GposInfoGet().getTimeshiftDuration(), 0, path);
                                TimeShift_Start_V2(GposInfoGet().getTimeshiftDuration(), 0, false, null);
                                ShowBanner(1);
                            }
                        };
                    }
                }
                //Scoty 20180713 start record/timeshift should stop pre rec/timeshift when recording/timeshift on the same channel -e
                //
                break;
            case KEYCODE_MEDIA_STOP:
            case ExtKeyboardDefine.KEYCODE_MEDIA_STOP:  // Johnny 20181210 for keyboard control
            {
                Log.d(TAG, "onKeyDown: KEYCODE_MEDIA_STOP");
                if (ViewHistory.getCurPipChannel() != null && pipFrame.getVisibility() == View.VISIBLE)
                    return true;
                if (Pvcfg.getPVR_PJ())
                {
                    // edwin 20180802 block "stop key" when USB is not available -s
                    if (!CheckUsbPathAvailable(GetRecordPath())) // connie 20181024 for USB Path wrong
                    {
                        break;
                    }
                    // edwin 20180802 block "stop key" when USB is not available -e

                    List<PvrInfo> pvrList = new ArrayList<PvrInfo>();
                    pvrList = PvrRecordGetAllInfo();
                    Log.d(TAG, "onKeyDown: pvrList => " + pvrList.size());
                    if (pvrList.size() > 0)
                    {
                        mStopMultiRecDialog = new StopMultiRecDialogView(this, pvrList, null)
                        {//Scoty 20180720 modify stop multi rec dialog
                            @Override
                            public void onStopPVR(int pvrMode, int recId)
                            {
                                stopPVRMode(pvrMode, recId, 1);//Scoty 20180809 modify dual pvr rule
                            }
                        };

                        // Edwin 20190508 fix dialog has no focus -s
                        Handler fixDialogNoFocus = new Handler();
                        fixDialogNoFocus.postDelayed(new Runnable() {
                            @Override
                            public void run () {
                                mStopMultiRecDialog.show();
                            }
                        }, 200);
                        // Edwin 20190508 fix dialog has no focus -e
                    }
                }
                //Log.d(TAG, "onKeyDown: STOP    " + PvrGetCurrentRecMode());
                //Scoty 20180712 for pip
//                if(ViewHistory.getCurPipChannel() != null && pipFrame.getVisibility() == View.VISIBLE)
//                    return true;
//                Log.d(TAG, "onKeyDown: STOP    " + getCurrentPvrMode());
//                if(Pvcfg.getPVR_PJ()==true) {//eric lin 20180703 add pvcfg
//                    final int pvrMode = getCurrentPvrMode();//final int pvrMode = PvrGetCurrentRecMode();
//                    if ( pvrMode != PvrInfo.EnPVRMode.NO_ACTION ) {
//                    new StopRecDialogView(ViewActivity.this, mDTVActivity) {
//                        @Override
//                        public void onStopPVR() {
//                            // stop PVR
//                            //Log.d(TAG, "onStopPVR: TOST KEYCODE_MEDIA_STOP");
//                            stopPVRMode(pvrMode, 1);//eric lin 20180629 stop pvr and av play
//                        }
//                    };
//                }
//                }
            }break;
            case KEYCODE_MEDIA_RECORD:
            case ExtKeyboardDefine.KEYCODE_MEDIA_RECORD:    // // Johnny 20181210 for keyboard control
            {
                Log.d(TAG, "onKeyDown: REC");
                //Scoty 20180712 for pip
                if (ViewHistory.getCurPipChannel() != null && pipFrame.getVisibility() == View.VISIBLE)
                    return true;
                //Scoty 20180713 start record/timeshift should stop pre rec/timeshift when recording/timeshift on the same channel -s
                final int pvrmode = getCurrentPvrMode();
                if (Pvcfg.getPVR_PJ()) //eric lin 20180703 add pvcfg
                {
                    if ( ! TunerIsLock(0) )
                        break;

                    // edwin 20180802 block "Record" when USB is not available -s
                    if (!CheckUsbPathAvailable(GetRecordPath())) // connie 20181024 for USB Path wrong
                    {
                        StorageNotAvailableDialog.show();
                        break;
                    }
                    // edwin 20180802 block "Record" when USB is not available -e

                    if (pvrmode == PvrInfo.EnPVRMode.NO_ACTION)
                    {
                        recDurDialog = new RecDurationSettingView(ViewActivity.this)
                        {

                            public void onSetPositiveButton(int durationValue)
                            {
                                RecordDuration = durationValue;
                                Log.d(TAG, "onSetPositiveButton:  RecordDuration =" + RecordDuration);
                                startRecord();
                            }
                        };

                        // Edwin 20190508 fix dialog not focus -s
                        Handler fixDialogNoFocus = new Handler();
                        fixDialogNoFocus.postDelayed(new Runnable() {
                            @Override
                            public void run () {
                                recDurDialog.show();
                            }
                        }, 200);
                        // Edwin 20190508 fix dialog not focus -e
                    }
                    else if (CheckPvrMode(pvrmode))//Scoty 20180827 add and modify TimeShift Live Mode
                    {
                        new SureDialog(ViewActivity.this)
                        {
                            public void onSetMessage(View v)
                            {
                                ((TextView) v).setText(getString(R.string.STR_DO_YOU_WANT_TO_STOP_TIMESHIFT));
                            }

                            public void onSetNegativeButton()
                            {
                            }

                            public void onSetPositiveButton()
                            {
                                int recId = PvrRecordCheck(ViewHistory.getCurChannel().getChannelId());//Scoty 20180809 modify dual pvr rule
                                stopPVRMode(pvrmode, recId, 1);//Scoty 20180809 modify dual pvr rule//eric lin 20180629 stop pvr and av play //why 1, because enter invalid channel number not call AvControlPlayByChannelId
                                recDurDialog = new RecDurationSettingView(ViewActivity.this)
                                {

                                    public void onSetPositiveButton(int durationValue)
                                    {
                                        RecordDuration = durationValue;
                                        Log.d(TAG, "onSetPositiveButton:  RecordDuration =" + RecordDuration);
                                        startRecord();
                                    }
                                };

                                // Edwin 20190508 fix dialog not focus -s
                                Handler fixDialogNoFocus = new Handler();
                                fixDialogNoFocus.postDelayed(new Runnable() {
                                    @Override
                                    public void run () {
                                        recDurDialog.show();
                                    }
                                }, 200);
                                // Edwin 20190508 fix dialog not focus -e
                            }
                        };

                    }
                }
                //Scoty 20180713 start record/timeshift should stop pre rec/timeshift when recording/timeshift on the same channel -e
            }break;
            case KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                Log.d(TAG, "onKeyDown: RED");
                Intent intent = new Intent();
                intent.setClass(this, VoiceSearchActivity.class);
                startActivity(intent);
                // Edwin 20190510 disable pip -s
//                int pvrMode = getCurrentPvrMode();
//                //Scoty 20180712 for pip -s
//                if (ViewHistory.getCurGroupType() == ProgramInfo.ALL_TV_TYPE &&
//                        (!CheckPvrMode(pvrMode)))//Scoty 20180827 add and modify TimeShift Live Mode//Scoty 20180717 modify timeshift and play not open Pip
//                {
//                    if (ViewHistory.getCurPipChannel() == null) {
//                        pipFrame.setPosition(mPipFrameRect.x, mPipFrameRect.y, mPipFrameRect.width, mPipFrameRect.height);
//                        pipFrame.SetVisibility(View.VISIBLE);
//                        ViewHistory.setCurPipChannel(this);//Scoty 20180724 modify open pip rule first: record channel, second: previous watched channel, third: next channel
//                        //Scoty 20180713 modify pip frame color when record -s
//                        SetPipColor();
//                        //Scoty 20180713 modify pip frame color when record -e
//                        if ((ViewHistory.getCurPipChannel().getChannelId() != ViewHistory.getCurChannel().getChannelId())
//                                && (ViewHistory.getCurPipChannel().getTpId() == ViewHistory.getCurChannel().getTpId())) {
//                            viewUiDisplay.OpenPipScreen(mPipFrameRect.x, mPipFrameRect.y, mPipFrameRect.width, mPipFrameRect.height);
//                            ShowBanner(1);
//                        }
//                    }
//                    else
//                    {
//                        viewUiDisplay.ClosePipScreen();
//                        pipFrame.SetVisibility(View.INVISIBLE);
//                        CloseBanner();
//                    }
//                }
                // Edwin 20190510 disable pip -e
                //Scoty 20180712 for pip -e
                //
                break;
            case KEYCODE_PROG_GREEN:
            case ExtKeyboardDefine.KEYCODE_PROG_GREEN: // Johnny 20181210 for keyboard control
                Log.d(TAG, "onKeyDown: GREEN");
                // Edwin 20190510 disable pip -s
//                //Scoty 20180712 for pip
//                if (ViewHistory.getCurPipChannel() != null) {
//                    pipFrame.setSwitch();
//                    clearNoVideoSignal();//eric lin 20180803 no video signal
//                }
//                ShowBanner(1);
//                //
                // Edwin 20190510 disable pip -e
                break;
            case KEYCODE_PROG_YELLOW:
            case ExtKeyboardDefine.KEYCODE_PROG_YELLOW: // Johnny 20181210 for keyboard control
                Log.d(TAG, "onKeyDown: YELLOW");
                // Edwin 20190510 disable pip -s
//                //Scoty 20180801 fixed switch Pip Screen not set Red Frame -s//Scoty 20180712 for pip
//                if (ViewHistory.getCurPipChannel() != null) {
//                    viewUiDisplay.SetPipWindow(mPipFrameRect.x, mPipFrameRect.y, mPipFrameRect.width, mPipFrameRect.height);
//                    pipFrame.setSwitch();
//                    viewUiDisplay.SetAvPipExChange();
//                    SetPipColor();
//                }
//                //Scoty 20180801 fixed switch Pip Screen not set Red Frame -e
//                //
                // Edwin 20190510 disable pip -e
                break;
            case KEYCODE_PROG_BLUE:
            case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
            {
                Log.d(TAG, "onKeyDown: BLUE");
                //TestVMXOTA(1); //Scoty 20181207 modify VMX OTA rule
                //Scoty 20181204 add VMX scan/signal/mail/sleep key events -s
                 /*//VMX SIGNAL KEY
                if (Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798)
                    new SignalDialogView(ViewActivity.this);
                */

                /*//VMX TIMER KEY
                if (Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798)
                    new SleepTimerSettingDialog(ViewActivity.this);
                */

                /*//VMX Scan Key
                if (Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798)
                    GoToAutoSearch();
                */

                /*//VMX MAIL KEY
                Intent mail = new Intent();
                mail.setClass(ViewActivity.this,MailActivity.class);
                startActivity(mail);*/
                //Scoty 20181204 add VMX scan/signal/mail/sleep key events -e

                /*
                if (Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798) // connie 20181116 for  Revisions-20170526 report
                {
                    String boxID = VMXGetBoxID();// Test Need Modify !!!!!!!!!!!!; // Edwin 20181127 fix 3798 test
                    String virtualNo = VMXGetVirtualNumber();// Test Need Modify !!!!!!!!!!!!;
                    String msg = getString(R.string.STR_BOX_ID) + " : " + boxID;
                    if (virtualNo != null && !virtualNo.equals(""))
                        msg = msg + "\n" + getString(R.string.STR_VIRTUAL_NO) + " : " + virtualNo;
                    new MessageDialogView(mContext, msg, 0) {
                        public void dialogEnd() {
                        }
                    }.show();
                }
                */
                //
//                ArrayList<String> s = new ArrayList<>();
//                s.add("SVT");
//                s.add("TV");
//                s.add("AB");
//                s.add("AB");
//                s.add("r");
//                openChannelNameFilterList(s);

        }break;
            case KeyEvent.KEYCODE_MEDIA_REWIND:
            case ExtKeyboardDefine.KEYCODE_MEDIA_REWIND:  // Johnny 20181210 for keyboard control
                Log.d(TAG, "onKeyDown: REWIND");
                //Scoty 20180712 for pip
                if(ViewHistory.getCurPipChannel() != null && pipFrame.getVisibility() == View.VISIBLE)
                    return true;
                //Scoty 20180622 On normal view, press Rewind/Fast Forward key should show banner only on timeshift play -s

                //if((PvrGetCurrentRecMode() == TIMESHIFT) &&
                //        (PvrGetCurrentPlayMode() == EnPVRPlayMode.TIMESHIFT_PLAY))
                int curPvrMode = getCurrentPvrMode();//Scoty 20180827 add and modify TimeShift Live Mode
                if(curPvrMode == PvrInfo.EnPVRMode.TIMESHIFT_FILE || curPvrMode == PvrInfo.EnPVRMode.TIMESHIFT_LIVE)//Scoty 20180827 add and modify TimeShift Live Mode
                {
                    EnTrickMode FBtrickMode = PvrTimeShiftGetCurrentTrickMode(ViewHistory.getPlayId());

                    FBtrickMode = PvrSetRewindTrickMode(FBtrickMode);//Scoty 20180613 add pvr rewind/fast forward function
                    PvrTimeShiftTrickPlay(ViewHistory.getPlayId(), FBtrickMode);
                    ShowBanner(1);
                }
                //
                break;
            case KEYCODE_MEDIA_FAST_FORWARD:
            case ExtKeyboardDefine.KEYCODE_MEDIA_FAST_FORWARD:  // Johnny 20181210 for keyboard control
                Log.d(TAG, "onKeyDown: FAST FORWARD");
                //Scoty 20180712 for pip
                if(ViewHistory.getCurPipChannel() != null && pipFrame.getVisibility() == View.VISIBLE)
                    return true;
                //Scoty 20180622 On normal view, press Rewind/Fast Forward key should show banner only on timeshift play -s

                //if((PvrGetCurrentRecMode() == TIMESHIFT) &&
                //        (PvrGetCurrentPlayMode() == EnPVRPlayMode.TIMESHIFT_PLAY))
                int CurPvrMode = getCurrentPvrMode();//Scoty 20180827 add and modify TimeShift Live Mode
                if(CurPvrMode == PvrInfo.EnPVRMode.TIMESHIFT_FILE || CurPvrMode == PvrInfo.EnPVRMode.TIMESHIFT_LIVE)//Scoty 20180827 add and modify TimeShift Live Mode
                {
                    EnTrickMode FFtrickMode = PvrTimeShiftGetCurrentTrickMode(ViewHistory.getPlayId());

                    FFtrickMode = PvrSetFastForwardTrickMode(FFtrickMode);//Scoty 20180613 add pvr rewind/fast forward function
                    PvrTimeShiftTrickPlay(ViewHistory.getPlayId(), FFtrickMode);
                    ShowBanner(1);
                }

                break;
            case KEYCODE_PAGE_DOWN:
                Log.d(TAG, "onKeyDown: PAGE-");
                //
                break;
            case KEYCODE_PAGE_UP:
                Log.d(TAG, "onKeyDown: PAGE+");
                //
                break;
            case KEYCODE_BACK: {
                //Log.d(TAG, "onKeyDown: BACK or EXIT");
                boolean focusPipFlag = isFocusPip();
                int pvrMod = -1;
                if (focusPipFlag)
                    pvrMod = getCurrentPipPvrMode();
                else
                    pvrMod = getCurrentPvrMode();

                if (detailView.GetVisibility() == View.VISIBLE) {
                    detailView.SetVisibility(View.INVISIBLE, null, 0, 0, 0, "");
                    if (Pvcfg.getPVR_PJ() == true && pvrMod != PvrInfo.EnPVRMode.NO_ACTION)//if( PvrGetCurrentRecMode() != NO_ACTION)  // connie 20181101 for pvr banner cant show when pvr not support
                        pvrBanner.setVisibility(mDTVActivity, VISIBLE, RecordDuration, focusPipFlag);//Scoty 20180802 fixed cur time not update when focus on Pip
                    StartBannerTick();
                    return true;
                }
                // for VMX need open/close -s
                else if(caMsgView.GetVisibility() == View.VISIBLE && caMsgView.GetCAMsgMode() == caMsgLayout.TYPE_USER) // user mode  // connie 20180903 for VMX
                {
                    caMsgView.SetVisibility(View.INVISIBLE);
                    Log.d(TAG, "onKeyDown:  Close User VMX Msg  ID = " + caMsgView.GetTriggerID() + "   Num =" + caMsgView.GetTriggerNum());
                    VMXOsmFinish(caMsgView.GetTriggerID(), caMsgView.GetTriggerNum());
                    return true;
                }// for VMX need open/close -e
                else if (bannerView.GetVisibility() == View.VISIBLE) {
                    CloseBanner();//Scoty 20180621 modify close banner as function// connie 20180524 fix channel num show wrong in fav group
                    if (pvrBanner.getVisibility() == VISIBLE)
                        pvrBanner.setVisibility(mDTVActivity, INVISIBLE, 0, focusPipFlag);//Scoty 20180802 fixed cur time not update when focus on Pip

                    return true;
                }

//                // Johnny 20190521 modify flow of pressing back again to exit
//                long currentTime = System.currentTimeMillis();
//                if (mBackPressedTime + TIME_INTERVAL_BACK < currentTime) {
//                    mExitToast.show();
//                    mBackPressedTime = currentTime;
//                    return true;
//                }
//                else {
//                    mExitToast.cancel();
//                }

                //Scoty 20180821 fixed exit DtvPlayer not stop video,close ca message,close pip, stop record -s
                if(ViewHistory.getCurPipChannel() != null)
                {
                    viewUiDisplay.ClosePipScreen();
                    pipFrame.SetVisibility(View.INVISIBLE);
                }
                if(mCaMessagView != null) {//Scoty 20180801 fixed error message not clean when exit apk
                    mCaMessagView.remove();
                    mCaMessagView = null;
                }
                /*if(BootAVPlayHandler!=null)
                {
                    BootAVPlayHandler.removeCallbacks(BootAVPlayRunnable);
                    BootAVPlayHandler = null;
                }*/

                if( TEST_MODE == 1) {//Scoty 20181001 fixed auto change channel show no video signal
                    removeAutoChangeCh();
                }

//                stopAllRec();//Scoty 20180720 add stop all records/timeshift
//                AvControlPlayStop(MiscDefine.AvControl.AV_STOP_ALL);
//                AvControlClose(ViewHistory.getPlayId());
//                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, sCreenOffTime);//Scoty 20181129 set Screen Saver Time
                //Scoty 20180821 fixed exit DtvPlayer not stop video,close ca message,close pip, stop record -e

                // for VMX need open/close -s
                if(mFingerPringView!=null ) { // connie 20180903 for VMX
                    mFingerPringView.FingerPrintFinish();
                    mFingerPringView.remove();
                    mFingerPringView = null;
                } // for VMX need open/close -e
/*
                else
                { // jim test pip
                    PictureInPictureParams.Builder mPictureInPictureParamsBuilder =  new PictureInPictureParams.Builder() ;
                    mPictureInPictureParamsBuilder.setAspectRatio(new Rational(16,9));
                    this.enterPictureInPictureMode(mPictureInPictureParamsBuilder.build());
                }
*/
                //return true;
//                AvControlPlayStop(ViewHistory.getPlayId());
//                AvControlClose(ViewHistory.getPlayId());
//                Log.d(TAG, "onKeyDown: back key");
//                else if(mvolumeView.getVisibility() == View.VISIBLE)
//                {
//                    GposInfo gposinfo = GposInfoGet();
//                    gposinfo.setVolume(volumeValue);
//                    GposInfoSave(gposinfo);
//                    AvControlSetVolumn(viewUiDisplay.History.getPlayId(),volumeValue);
//                    mvolumeView.startAnimation(volumeCloseScale);
//                    volumeCloseScale.setAnimationListener(new closeVolumebarAnimation());
//                    return true;
//                }
                /*else //add set back channel
                {
                    viewUiDisplay.ChangePreProgram();
                    ShowBanner();
                    return true;
                }*/

                //break;
                
//                finish();  // Johnny 20181219 for mouse control, prevent onTouchEvent still triggered
            }break;

            case KEYCODE_DPAD_CENTER:  //add ok list dialog
                {
                    if( detailView.GetVisibility() == View.INVISIBLE &&
                            chNumTxv.getVisibility() == View.INVISIBLE &&
                            pipFrame.getVisibility() == View.INVISIBLE ) //Scoty 20180712 for pip
                    {
                        OpenOkFavList(KEYCODE_DPAD_CENTER);
                    }
                }break;

            case KEY_FAV:
            case ExtKeyboardDefine.KEYCODE_FAV:    // Johnny 20181210 for keyboard control
                Log.d(TAG, "onKeyDown: FAV");
                if ( detailView.GetVisibility() == View.INVISIBLE )
                {
                    //Scoty 20180712 for pip
                    if(ViewHistory.getCurPipChannel() != null && pipFrame.getVisibility() == View.VISIBLE)
                        return true;
                    OpenOkFavList(KEY_FAV);
                }
                break;

//            case KEYCODE_VOLUME_UP:
//            case KEYCODE_VOLUME_DOWN:
//                Log.d(TAG, "onKeyDown: Volume Up or Volume Down");
//                VolumeUpDown(keyCode);
//                return true;//不走default volume layout

            case KEYCODE_GUIDE_PESI:
            case ExtKeyboardDefine.KEYCODE_GUIDE_PESI:  // Johnny 20181210 for keyboard control
            {
                if(detailView.GetVisibility() == View.INVISIBLE) {
                    Log.d(TAG, "Dimension_EPG()");
                    int curListPosition = ViewHistory.getCurListPos(ViewHistory.getCurChannel().getChannelId());
                    Intent it = new Intent();
                    it.setClass(this, DimensionEPG.class);
                    bundle = new Bundle();
                    if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798) {
                        if (Pvcfg.getCAType() == Pvcfg.CA_VMX && GetVMXBlockFlag() == 1)//Scoty 20181129 modify VMX enter Epg rule
                        {
                            new MessageDialogView(mContext,getString(R.string.STR_VMX_CHBLOCK_MSG),3000)
                            {
                                public void dialogEnd() {
                                }
                            }.show();
                            return true;
                        }

                        if(ViewHistory.getCurGroupType() >= ProgramInfo.ALL_TV_TYPE && ViewHistory.getCurGroupType() < ProgramInfo.ALL_RADIO_TYPE)
                            bundle.putInt("type", ProgramInfo.ALL_TV_TYPE);
                        else
                            bundle.putInt("type", ProgramInfo.ALL_RADIO_TYPE);
                    }
                    else
                    {
                        bundle.putInt("type", ViewHistory.getCurGroupType());
                    }
                    bundle.putInt("cur_channel", curListPosition );
                    if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798)//Scoty 20181129 modify VMX enter Epg rule
                        bundle.putBoolean("changeCH",true);
                    it.putExtras(bundle);
                    startActivity(it, bundle);
                }
            }break;
            case KeyEvent.KEYCODE_ENVELOPE:{//Scoty 20181204 add VMX scan/signal/mail/sleep key events
                Intent mail = new Intent();
                mail.setClass(ViewActivity.this,MailActivity.class);
                startActivity(mail);
            }break;
            default:
                Log.d(TAG, "onKeyDown: keycode(" + keyCode + ") in default case");
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void TvRadioSwitch() {
        //Scoty 20180810 change save Tv/Radio channel by channelId -s 
        Log.d(TAG, "TvRadioSwitch: group = " + ViewHistory.getCurGroupType());
        long TvRadioChannelId = 0;
        int serviceType = ViewHistory.getCurGroupType();

        if (serviceType >= ProgramInfo.ALL_TV_TYPE && serviceType < ProgramInfo.ALL_RADIO_TYPE) {
            List<SimpleChannel> channelList = ProgramInfoGetPlaySimpleChannelList(ProgramInfo.ALL_RADIO_TYPE,0);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule

            if(channelList != null && !channelList.isEmpty()) {
                if(ViewHistory.getRadioChannelId() != 0) {
                    TvRadioChannelId = ViewHistory.getRadioChannelId();
                }
                else {
                    TvRadioChannelId = channelList.get(0).getChannelId();
                }

                bannerProgInfo = ProgramInfoGetByChannelId(/*channelList.get(TvRadioChannel).getChannelId()*/TvRadioChannelId);
                viewUiDisplay.ChangeGroup(channelList, ProgramInfo.ALL_RADIO_TYPE, /*channelList.get(TvRadioChannel).getChannelId()*/TvRadioChannelId);
            }
            Log.d(TAG, "onKeyDown: change to radio");
        }
        else if (serviceType >= ProgramInfo.ALL_RADIO_TYPE && serviceType < ProgramInfo.ALL_TV_RADIO_TYPE_MAX) {
            List<SimpleChannel> channelList = ProgramInfoGetPlaySimpleChannelList(ProgramInfo.ALL_TV_TYPE,0);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
            if(ViewHistory.getTvChannelId() != 0)
                TvRadioChannelId = ViewHistory.getTvChannelId();
            else
                TvRadioChannelId = channelList.get(0).getChannelId();
            if(channelList != null && channelList.size() > 0) {
                bannerProgInfo = ProgramInfoGetByChannelId(/*channelList.get(TvRadioChannel).getChannelId()*/TvRadioChannelId);
                viewUiDisplay.ChangeGroup(channelList, ProgramInfo.ALL_TV_TYPE, /*channelList.get(TvRadioChannel).getChannelId()*/TvRadioChannelId);
            }
            Log.d(TAG, "onKeyDown: change to tv");
        }
        else {
            Toast.makeText(this,
                    "onKeyDown: unknown service type = "+ serviceType,
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onKeyDown: unknown service type = "+ serviceType);
        }
        ShowBanner(0);//Scoty 20180620 fixed change tv/radio not show correct banner info
        //Scoty 20180810 change save Tv/Radio channel by channelId -e
    }

    // edwin 20180730 add -s
    private void ChangeChannelByChNum(String chNum)
    {
        digitNumStr = chNum;

        final int pvrMode = getCurrentPvrMode();//final int pvrMode = PvrGetCurrentRecMode();
        final int targetChPvrSkipFlag = GetPvrSkipFlagByChNum(Integer.valueOf(chNum), ViewHistory.getCurGroupType()); // pvrSkipFlag of the chNum

        if (CheckPvrMode(pvrMode)) {//Scoty 20180827 add and modify TimeShift Live Mode
            new SureDialog(ViewActivity.this) {
                public void onSetMessage(View v) {
                    ((TextView) v).setText(getString(R.string.STR_DO_YOU_WANT_TO_STOP_TIMESHIFT));
                }
                public void onSetNegativeButton() {
                    digitNumStr = "";
                    chNumTxv.setVisibility(INVISIBLE);
                }
                public void onSetPositiveButton() {
                    //Log.d(TAG, "onSetPositiveButton: TOST ChangeChannelByChannelNum");
                    final int recId = PvrRecordCheck(ViewHistory.getCurChannel().getChannelId());//Scoty 20180809 modify dual pvr rule
                    stopPVRMode(pvrMode,recId, 1);//Scoty 20180809 modify dual pvr rule//eric lin 20180629 stop pvr and av play
                    if (targetChPvrSkipFlag == 1)
                    {
                        new SureDialog(ViewActivity.this) {
                            public void onSetMessage(View v) {
                                ((TextView) v).setText(getString(R.string.STR_DO_YOU_WANT_TO_STOP_RECORD));
                            }
                            public void onSetNegativeButton() {
                                digitNumStr = "";
                                chNumTxv.setVisibility(INVISIBLE);
                            }
                            public void onSetPositiveButton() {
                                //Log.d(TAG, "onSetPositiveButton: TOST ChangeChannelByChannelNum");
                                stopAllRec();//Scoty 20180809 modify dual pvr rule
                                DigitSetChannel();
                            }
                        };
                    }
                    else
                    {
                        DigitSetChannel();
                    }
                }
            };
        }
        else if (targetChPvrSkipFlag == 1)
        {
            new SureDialog(ViewActivity.this) {
                public void onSetMessage(View v) {
                    ((TextView) v).setText(getString(R.string.STR_DO_YOU_WANT_TO_STOP_RECORD));
                }
                public void onSetNegativeButton() {
                    digitNumStr = "";
                    chNumTxv.setVisibility(INVISIBLE);
                }
                public void onSetPositiveButton() {
                    //Log.d(TAG, "onSetPositiveButton: TOST ChangeChannelByChannelNum");
                    stopAllRec();//Scoty 20180809 modify dual pvr rule
                    DigitSetChannel();
                }
            };
        }
        else {
            DigitSetChannel();
        }
    }
    // edwin 20180730 add -e

    private void DigitSetChannel()
    {
        int digitNum = Integer.valueOf(digitNumStr);
        boolean channelFound = false;

        //check channel exist
        channelFound = viewUiDisplay.ChangeChannelByDigi(digitNum);
        ShowBanner(0);

        chNumTxv.setVisibility(INVISIBLE);
        digitNumStr = "";
        Log.d(TAG, "onFinish:" +
                " digitNum = "+ digitNum
        );

        // channel not found
        if (!channelFound) {
            new MessageDialogView(mContext,getString(R.string.STR_ERROR_CHANNEL_NUMBER),3000)
            {
                public void dialogEnd() {
                }
            }.show();
        }
        else
        {
            //Scoty 20180712 for pip
            if(ViewHistory.getCurPipChannel() != null) {//eric lin 20180716 fix getCurPipChannel null cause crash
                if (ViewHistory.getCurPipChannel().getChannelId() == ViewHistory.getCurChannel().getChannelId()) {
                viewUiDisplay.ClosePipScreen();
                pipFrame.SetVisibility(View.INVISIBLE);
            }
        }
    }
    }

    private void ShowBanner(int showIconFlag) {
        //Scoty 20180802 modify pvr banner rule -s//Scoty 20180712 for pip -s
        SimpleChannel channel = null;
        int pvrMode = -1;
        boolean focusPipFlag = isFocusPip();
        if(focusPipFlag) {
            channel = ViewHistory.getCurPipChannel();
            viewUiDisplay.UpdatePipEpgPF();
            pvrMode = getCurrentPipPvrMode();
        }
        else {
            channel = ViewHistory.getCurChannel();
            viewUiDisplay.UpdateEpgPF();
            pvrMode = getCurrentPvrMode();
        }

        Log.d(TAG, "ShowBanner: channelId " + channel.getChannelId());
        CloseCurSubtitle();//Scoty 20180621 modify when subtitle showing and then open oklist/menu/Guide should close subtitle; back to ViewActivity show subtitle
        // Johnny add 2080326, not showing banner when okList is showing
        if (okListDialog != null && okListDialog.isShowing())
        {
            return;
        }

        bannerProgInfo = ProgramInfoGetByChannelId(/*ViewHistory.getCurChannel().getChannelId()*/channel.getChannelId());
        int channelNumber = channel.getChannelNum();//ViewHistory.getCurChannel().getChannelNum(); // connie 20180524 fix channel num show wrong in fav group

        epgEventGetPF = new ArrayList<>();
        if(viewUiDisplay.EpgPreData != null) {
            epgEventGetPF.add(viewUiDisplay.EpgPreData);
        }
        if(viewUiDisplay.EpgFolData != null) {
            epgEventGetPF.add(viewUiDisplay.EpgFolData);
        }


        if (showIconFlag == 1) {
            bannerView.SetVisibility(VISIBLE, GposInfoGet(), bannerProgInfo, epgEventGetPF, GetLocalTime(), channelNumber ,mDTVActivity);// connie 20180524 fix channel num show wrong in fav group
        }
        else {
            bannerView.SetVisibility(VISIBLE, GposInfoGet(),bannerProgInfo,epgEventGetPF,GetLocalTime(), channelNumber,null);// connie 20180524 fix channel num show wrong in fav group
        }

        int recId = PvrRecordCheck(channel.getChannelId());//Scoty 20180809 modify dual pvr rule
        if (recId != -1)//Scoty 20180809 modify dual pvr rule
            pvrBanner.setVisibility(mDTVActivity, VISIBLE, RecordDuration,focusPipFlag);
        else if (Pvcfg.getPVR_PJ() == true && CheckPvrMode(pvrMode)) {//Scoty 20180827 add and modify TimeShift Live Mode  // connie 20181101 for pvr banner cant show when pvr not support
            pvrBanner.setVisibility(mDTVActivity, VISIBLE, GposInfoGet().getTimeshiftDuration(),focusPipFlag);
        }else
            pvrBanner.setVisibility(mDTVActivity, INVISIBLE, 0,focusPipFlag);

        //Scoty 20180802 modify pvr banner rule -e
        StartBannerTick();
    }
    //Scoty 20180621 modify when subtitle showing and then open oklist/menu/Guide should close subtitle; back to ViewActivity show subtitle -s
    public void CloseBanner()//Scoty 20180621 modify close banner as function
    {
        ShowCurSubtitle();
        bannerView.SetVisibility(INVISIBLE, null, null, null, null, 0, null);
        if(pvrBanner.getVisibility() == View.VISIBLE)
            pvrBanner.setVisibility(mDTVActivity, INVISIBLE, 0,isFocusPip()) ;
    }

    public void ShowCurSubtitle()
    {
        SubtitleInfo Subtitle = AvControlGetSubtitleList(ViewHistory.getPlayId());
        GposInfo gposInfo = GposInfoGet();
        if(CurSubtitleIndex != 0 )
            AvControlSelectSubtitle(ViewHistory.getPlayId(), Subtitle.getComponent(CurSubtitleIndex));
        else if(gposInfo.getSubtitleOnOff() == 1) {
            Log.d(TAG,"ShowCurSubtitle !!!!!!!");
            AvControlShowSubtitle(ViewHistory.getPlayId(), true);
        }
    }

    public void CloseCurSubtitle()
    {
        if(CurSubtitleIndex != 0 || AvControlIsSubtitleVisible(ViewHistory.getPlayId()) == true) {
            Log.d(TAG,"CloseCurSubtitle !!!!!!!");
            AvControlShowSubtitle(ViewHistory.getPlayId(), false);
        }
    }

    //Scoty 20180621 modify when subtitle showing and then open oklist/menu/Guide should close subtitle; back to ViewActivity show subtitle -e
    public void StartBannerTick()
    {
        Log.d(TAG, "StartBannerTick: ");

        if ( autoCloseBanner != null ) {
            autoCloseBanner.cancel();
        }

        autoCloseBanner = new CountDownTimer(
                GposInfoGet().getBannerTimeout()*1000, 1000)
        {
            @Override
            public void onTick(long l) {
                if ( pvrBanner.getVisibility() == VISIBLE)
                    pvrBanner.updateTimeStatus(mDTVActivity, RecordDuration,isFocusPip());//Scoty 20180802 fixed cur time not update when focus on Pip
            }
            public void onFinish() {
                if ( detailView.GetVisibility() == INVISIBLE  ) {
                    CloseBanner();//Scoty 20180621 modify close banner as function// connie 20180524 fix channel num show wrong in fav group
                }
                if ( pvrBanner.getVisibility() == VISIBLE )
                    pvrBanner.setVisibility(mDTVActivity, INVISIBLE, 0,isFocusPip());//Scoty 20180802 fixed cur time not update when focus on Pip
            }
        }.start();
    }

    public void StartVolumeTick()
    {
        Log.d(TAG, "StartVolumeTick: ");

        if ( autoCloseVolume != null ) {
            autoCloseVolume.cancel();
        }

        autoCloseVolume = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long l) {

            }
            public void onFinish() {
                if ( mvolumeView.getVisibility() == View.VISIBLE  ) {
                    Log.d(TAG,"volumeValue ===>>> 11111 >>> " + volumeValue);
                    GposInfo gpos = GposInfoGet();
                    gpos.setVolume(volumeValue);
                    AvControlSetVolume(volumeValue);
                    GposInfoUpdate(gpos);
                    volumeCloseScale.setDuration(500);
                    mvolumeView.startAnimation(volumeCloseScale);
                    volumeCloseScale.setAnimationListener(new closeVolumebarAnimation());
                }
            }
        }.start();
    }

    private void VolumeUpDown(int keyCode) {
        Log.d(TAG, "VolumeUpDown: ");
        //int curVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
        //final ImageView muteIcon = (ImageView) findViewById(R.id.mute);

        mvolumeView.setVisibility(View.VISIBLE);
        mvolumeView.requestFocus();
        StartVolumeTick();//auto close volume item
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)//volume up
        {
            volumeValue++;
            if (volumeValue > maxVolume)
                volumeValue = maxVolume;
        } else//volume down
        {
            volumeValue--;
            if (volumeValue < minVolume)
                volumeValue = minVolume;
        }

        mvolumeView.setProgressValue(volumeValue);//set mute icon or value
        //set current volume;flag == 0, not show default volume UI
        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, volumeValue, 0);
        mvolumeView.getVolumebar().setProgress(volumeValue);//current volume
        mvolumeView.getVolumeValue().setText(String.valueOf(volumeValue));//show current volume value
        mvolumeView.getVolumebar().setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                //SeekBar profress
                mvolumeView.setProgressValue(progress);
                mvolumeView.getVolumeValue().setText(String.valueOf(progress));
                mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, progress, 0);//flag == 0, not show default volume UI
                volumeValue = progress;
                if(volumeValue != 0)
                    muteIcon.setVisibility(View.INVISIBLE);
                else
                    muteIcon.setVisibility(View.VISIBLE);
                StartVolumeTick();//auto close volume item
            }
        });
    }

    private void VolumeMute() {
        Log.d(TAG, "VolumeMute: ");

        boolean mute = (muteIcon.getVisibility() == VISIBLE);

        // android
        if (mute)
            muteIcon.setVisibility(INVISIBLE);
        else
            muteIcon.setVisibility(VISIBLE);

        // tvmiddleware
        AvControlSetMute(ViewHistory.getPlayId(),mute);

        /* android audio manager */
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager != null) {
            if(mute) {
                audioManager.adjustVolume(AudioManager.ADJUST_MUTE,
                        AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            }
            else {
                mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, volumeValue, 0);
                mvolumeView.getVolumebar().setProgress(volumeValue);//current volume
                mvolumeView.getVolumeValue().setText(String.valueOf(volumeValue));//show current volume value
            }
        }
    }

    public void VolumebarInit()
    {
        Log.d(TAG, "volumebarInit: ");

        int curVolume;
        GposInfo gpos = GposInfoGet();
        mvolumeView = (VolumeView)findViewById(R.id.viewvolumeSEEKBAR);
        volumeValue = gpos.getVolume();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        setVolumeControlStream(AudioManager.STREAM_SYSTEM);//set audio type
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        curVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);

        if(volumeValue >= maxVolume)
            volumeValue = maxVolume;
        mvolumeView.getVolumebar().setMax(maxVolume);

        volumeOpenScale  = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        volumeOpenScale.setDuration(500);

        volumeCloseScale  = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        volumeCloseScale.setDuration(500);
    }

    private class closeVolumebarAnimation implements ScaleAnimation.AnimationListener{
        @Override
        public void onAnimationStart(Animation animation) {
            // TODO Auto-generated method stub

        }
        @Override
        public void onAnimationRepeat(Animation animation) {
            // TODO Auto-generated method stub

        }
        @Override
        public void onAnimationEnd(Animation animation) {//animation finish
            // TODO Auto-generated method stub
            mvolumeView.setVisibility(View.INVISIBLE);
        }
    }

    private class EPGData
    {
        private int eventType = EPGEvent.EPG_TYPE_PRESENT ; // present / follow

        private String GetCurPorgramDetail(int eventType)
        {
            String strInfo=null;
            String eventTime=null, startTime=null, endTime=null, eventName=null, eventDetailInfo=null, shortEvent = null;
            EPGEvent EventInfo = null;
            //Scoty 20180712 for pip -s
            SimpleChannel channel = null;
            if(ViewHistory.getCurPipChannel() != null && pipFrame.getVisibility() == View.VISIBLE)
                channel = ViewHistory.getCurPipChannel();
            else
                channel = ViewHistory.getCurChannel();
            //Scoty 20180712 for pip -e
            if(eventType == EPGEvent.EPG_TYPE_PRESENT)
                EventInfo = viewUiDisplay.EpgPreData;
            else
                EventInfo = viewUiDisplay.EpgFolData;

            if(EventInfo!=null)
            {
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                Date EventTime = new Date(EventInfo.getStartTime());

                startTime = formatter.format(EventTime);

                EventTime = new Date(EventInfo.getEndTime());
                endTime = formatter.format(EventTime);
                eventTime = startTime + "  -  " + endTime;

                eventName = EventInfo.getEventName();
                //Scoty Add Youtube/Vod Stream -s
                if(ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE
                        || ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.YOUTUBE_TYPE)
                {
                    eventDetailInfo = ViewHistory.getCurChannel().getDetailInfo();
                    shortEvent = ViewHistory.getCurChannel().getShortEvent();

                }
                else {
                    eventDetailInfo = viewUiDisplay.GetDetailInfo(channel.getChannelId(), EventInfo.getEventId());//Scoty 20180712 for pip
                    shortEvent = viewUiDisplay.GetShortEvent(channel.getChannelId(), EventInfo.getEventId());//Scoty 20180712 for pip
                }
                //Scoty Add Youtube/Vod Stream -e
                strInfo = eventTime + "     " + eventName;
                if(shortEvent!= null)
                    strInfo = strInfo + "\n\n" + shortEvent;
                if(eventDetailInfo!= null)
                    strInfo = strInfo + "\n\n" + eventDetailInfo;
            }
            else
                strInfo = getString(R.string.STR_NO_INFO_AVAILABLE);

            return strInfo;
        }

        private void SetShowEventType(int type)
        {
            eventType = type;
        }

        private int GetShowEventType()
        {
            return eventType;
        }
    }

    public void ShowOkList(List<DTVActivity.OkListManagerImpl> OkListManagerList,
                           int curServiceType,
                           int curListPosition,
                           int keycode,
                           int mode,
                           List<String> matchList)
    {
        okListDialog = new OkListDialogView(ViewActivity.this,
                OkListManagerList,
                GetAllProgramGroup(),
                curServiceType,
                curListPosition,
                mDTVActivity ,
                keycode,
                mode,
                matchList )
        {
            public void onSetNegativeButton() {
                ShowCurSubtitle();//Scoty 20180621 modify when subtitle showing and then open oklist/menu/Guide should close subtitle; back to ViewActivity show subtitle
            }

            public void onSetPositiveButton(List<OkListManagerImpl> okList,int curFavGroup, int position) {
                //Log.d(TAG, "openOkFavList  ==>> onSetPositiveButton: ===>>> " + position);
                //Scoty 20180712 for pip -s
                showOkListManager.clear();
                for(int i=0;i<okList.size();i++) {
                    showOkListManager.add(okList.get(i));
                }
                // channel not found


                if (position < 0) {
                    new MessageDialogView(mContext,getString(R.string.STR_ERROR_CHANNEL_NUMBER),3000)
                    {
                        public void dialogEnd() {
                        }
                    }.show();
                    return;
                }
                Log.d( TAG, "onSetPositiveButton: curFavGroup = "+curFavGroup+
                        " position = "+position+
                        " showOkListManager.size() = "+showOkListManager.size()+
                        " .ProgramInfoList.size() = "+showOkListManager.get(curFavGroup).ProgramInfoList.size()
                        + "program number = [" +  showOkListManager.get(curFavGroup).ProgramInfoList.get(position).getChannelNum()
                        + "program stream type = [" +showOkListManager.get(curFavGroup).ProgramInfoList.get(position).getPlayStreamType()
                );
                final SimpleChannel okListChannel = showOkListManager.get(curFavGroup).ProgramInfoList.get(position);
                if(ViewHistory.getCurPipChannel() != null)
                {
                    if(okListChannel.getChannelId() == ViewHistory.getCurPipChannel().getChannelId())
                    {
                        viewUiDisplay.ClosePipScreen();
                        pipFrame.SetVisibility(View.INVISIBLE);
                    }
                    showOkListManager.get(curFavGroup).ChangeProgram(position);
                    ShowBanner(1);
                }
                //else {
                final int pvrMode = getCurrentPvrMode();//final int pvrMode = PvrGetCurrentRecMode();
                final int FavGroup = curFavGroup;
                final int Pos = position;
                final int targetChPvrSkipFlag = GetPvrSkipFlagByChNum(okListChannel.getChannelNum(), curFavGroup); // pvrSkipFlag of the chNum
                if (CheckPvrMode(pvrMode))//Scoty 20180827 add and modify TimeShift Live Mode
                {
                    //Scoty 20180622 modify change channel to the same channel when record/timshift no need show stop record dialog
                    if (showOkListManager.get(curFavGroup).ProgramInfoList.get(position).getChannelId()
                            != ViewHistory.getCurChannel().getChannelId())
                    {
                        new SureDialog(ViewActivity.this) {
                            public void onSetMessage(View v) {
                                ((TextView) v).setText(getString(R.string.STR_DO_YOU_WANT_TO_STOP_TIMESHIFT));
                            }

                            public void onSetNegativeButton() {
                            }

                            public void onSetPositiveButton() {
                                //Log.d(TAG, "onSetPositiveButton: TOST OkListDialogView");
                                int recId = PvrRecordCheck(ViewHistory.getCurChannel().getChannelId());//Scoty 20180809 modify dual pvr rule
                                stopPVRMode(pvrMode,recId, 1);//Scoty 20180809 modify dual pvr rule//eric lin 20180629 stop pvr and av play
                                if (targetChPvrSkipFlag == 1)
                                {
                                    new SureDialog(ViewActivity.this) {
                                        public void onSetMessage(View v) {
                                            ((TextView) v).setText(getString(R.string.STR_DO_YOU_WANT_TO_STOP_RECORD));
                                        }
                                        public void onSetNegativeButton() {
                                        }
                                        public void onSetPositiveButton() {
                                            //Log.d(TAG, "onSetPositiveButton: TOST ChangeChannelByChannelNum");
                                            stopAllRec();//Scoty 20180809 modify dual pvr rule
                                            showOkListManager.get(FavGroup).ChangeProgram(Pos);//save and set channel
                                        }
                                    };
                                }
                            }
                        };
                    }
                    else
                    {
                        ShowBanner(1);  // Johnny 20180802 show banner when change to same channel in timeShift mode
                    }
                }
                else if (targetChPvrSkipFlag == 1)
                {
                    new SureDialog(ViewActivity.this) {
                        public void onSetMessage(View v) {
                            ((TextView) v).setText(getString(R.string.STR_DO_YOU_WANT_TO_STOP_RECORD));
                        }
                        public void onSetNegativeButton() {
                        }
                        public void onSetPositiveButton() {
                            //Log.d(TAG, "onSetPositiveButton: TOST ChangeChannelByChannelNum");
                            stopAllRec();//Scoty 20180809 modify dual pvr rule
                            showOkListManager.get(FavGroup).ChangeProgram(Pos);//save and set channel
                        }
                    };
                }
                //Scoty Add Youtube/Vod Stream -s
                else if(showOkListManager.get(curFavGroup).ProgramInfoList.get(position).getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE)
                {
                    SimpleChannel CurChannel = showOkListManager.get(curFavGroup).ProgramInfoList.get(position);
                    List<SimpleChannel> programInfoList = showOkListManager.get(curFavGroup).ProgramInfoList;
                    if(ViewHistory.getCurChannel() != null && ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.YOUTUBE_TYPE) {
//                        Log.d(TAG, "cur youtube onSetPositiveButton: Stop Exo Streamtype =[" + ViewHistory.getPreChannel().getPlayStreamType()
//                                +"] Pre channelId = [" + ViewHistory.getPreChannel().getChannelId()
//                                +"] Cur channelId = [" + ViewHistory.getCurChannel().getChannelId());
                        StopPlayYoutubeVideo();
                    }else  if(ViewHistory.getCurChannel() != null && ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE) {
//                        Log.d(TAG, "onSetPositiveButton: Stop Exo Streamtype =[" + ViewHistory.getPreChannel().getPlayStreamType()
//                                +"] Pre channelId = [" + ViewHistory.getPreChannel().getChannelId()
//                        +"] Cur channelId = [" + ViewHistory.getCurChannel().getChannelId());
                        if(CurChannel.getChannelId() != ViewHistory.getCurChannel().getChannelId()) {//Same Vod Stream no need stop and play again
                            mExoPlayer.stop(true);
                            mExoPlayer.clearVideoSurfaceView(mSurfaceViewExoplayer);
                            mSurfaceViewExoplayer.setVisibility(INVISIBLE);
                        }
                    }
                    else{//Stop AV Play
//                        Log.d(TAG, "cur youtube onSetPositiveButton: Stop Av Streamtype =[" + ViewHistory.getPreChannel().getPlayStreamType()
//                                +"] Pre channelId = [" + ViewHistory.getPreChannel().getChannelId()
//                                +"] Cur channelId = [" + ViewHistory.getCurChannel().getChannelId());
//                        AvControlPrePlayStop();
                        AvControlPrePlayStop();
                        AvControlPlayStop(MiscDefine.AvControl.AV_STOP_ALL);
                    }
                    //Change to different program
                    if(CurChannel.getChannelId() != ViewHistory.getCurChannel().getChannelId()) {
                        PlayExoPlayerVideo(CurChannel);
                        List<OkListManagerImpl> oklist = GetOkList();
                        Log.d(TAG, "onSetPositiveButton: size = [" + oklist.get(curFavGroup).ProgramInfoList.size() + "]");
                        ViewHistory.SetCurChannel(CurChannel, oklist.get(curFavGroup).ProgramInfoList, curFavGroup);
                    }

                    ShowBanner(1);
                    //}
                }else if(showOkListManager.get(curFavGroup).ProgramInfoList.get(position).getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.YOUTUBE_TYPE)
                {
                    SimpleChannel CurChannel = showOkListManager.get(curFavGroup).ProgramInfoList.get(position);
                    List<SimpleChannel> programInfoList = showOkListManager.get(curFavGroup).ProgramInfoList;
                    if(ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE) {
//                        Log.d(TAG, "onSetPositiveButton: Stop Exo Streamtype =[" + ViewHistory.getPreChannel().getPlayStreamType()
//                                +"] Pre channelId = [" + ViewHistory.getPreChannel().getChannelId()
//                        +"] Cur channelId = [" + ViewHistory.getCurChannel().getChannelId());
                        mExoPlayer.stop(true);
                        mExoPlayer.clearVideoSurfaceView(mSurfaceViewExoplayer);
                        mSurfaceViewExoplayer.setVisibility(INVISIBLE);
                    }else if(ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.YOUTUBE_TYPE) {
                        if(CurChannel.getChannelId() != ViewHistory.getCurChannel().getChannelId())
                            StopPlayYoutubeVideo();
                    }
                    else{
//                        Log.d(TAG, "onSetPositiveButton: Stop Av Streamtype =[" + ViewHistory.getPreChannel().getPlayStreamType()
//                                +"] Pre channelId = [" + ViewHistory.getPreChannel().getChannelId()
//                                +"] Cur channelId = [" + ViewHistory.getCurChannel().getChannelId());
//                        AvControlPrePlayStop();
                        AvControlPrePlayStop();
                        AvControlPlayStop(MiscDefine.AvControl.AV_STOP_ALL);
                    }
                    //Change to different program
                    if(CurChannel.getChannelId() != ViewHistory.getCurChannel().getChannelId()) {
                        StartPlayYoutubeVideo(CurChannel);
                        List<OkListManagerImpl> oklist = GetOkList();
                        Log.d(TAG, "onSetPositiveButton: size = [" + oklist.get(curFavGroup).ProgramInfoList.size() + "]");
                        ViewHistory.SetCurChannel(CurChannel, oklist.get(curFavGroup).ProgramInfoList, curFavGroup);
                    }
                    ShowBanner(1);
                }
                else
                {
                    if(ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE) {
                        mExoPlayer.stop(true);
                        mExoPlayer.clearVideoSurfaceView(mSurfaceViewExoplayer);
                        mSurfaceViewExoplayer.setVisibility(INVISIBLE);
                    }else if(ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.YOUTUBE_TYPE) {
                        StopPlayYoutubeVideo();
                    }
//Scoty Add Youtube/Vod Stream -e
                    Log.d(TAG,"oklist change channel : " + showOkListManager.get(curFavGroup).ProgramInfoList.get(position).getChannelName());
                    showOkListManager.get(curFavGroup).ChangeProgram(position);//save and set channel
                    startWaitPlaySuccess(); // Johnny 20210423 temp for RTK avplay issue
//                    if(mRecImageView != null) {
//                        if (getCurrentPvrMode() == PvrInfo.EnPVRMode.NO_ACTION)//Scoty 20180713 modify when current channel Recording show rec icon
//                            mRecImageView.SetVisibility(false);
//                        else
//                            mRecImageView.SetVisibility(true);
//                    }
                    //Scoty 20180620 modify change to same channel should show full banner info -s
                    if (okListChannel.getChannelId() != ViewHistory.getCurChannel().getChannelId()) {
                        ShowBanner(0);
                        CurSubtitleIndex = 0;//Scoty 20180621 modify when subtitle showing and then open oklist/menu/Guide should close subtitle; back to ViewActivity show subtitle
                    }
                    else {
                        int AVplayStatus = AvControlGetPlayStatus(ViewHistory.getPlayId());
                        if (AVplayStatus == HiDtvMediaPlayer.EnPlayStatus.STOP.getValue())
                            ShowBanner(0);
                        else
                            ShowBanner(1);
                    }
                    //Scoty 20180620 modify change to same channel should show full banner info -e
                }
            }
        };

        // Edwin 20190508 fix dialog not focus -s
        Handler fixDialogNoFocus = new Handler();
        fixDialogNoFocus.postDelayed(new Runnable() {
            @Override
            public void run () {
                okListDialog.show();
            }
        }, 100);
        // Edwin 20190508 fix dialog not focus -e

    }

    public void OpenOkFavList(int keycode)
    {
        int curServiceType, curListPosition;
        List<OkListManagerImpl> OkList = new ArrayList<OkListManagerImpl>();

        //OkListInit();
        OkList = GetOkList();
        curListPosition = ViewHistory.getCurListPos(ViewHistory.getCurChannel().getChannelId());//current service num
        curServiceType = ViewHistory.getCurGroupType();//current service type
        Log.d(TAG, "openOkFavList: " + ViewHistory.getCurChannel().getChannelId() + "position = " + curListPosition + "curGroup = "
                + curServiceType);

        if (bannerView.GetVisibility() == View.VISIBLE) {
            CloseBanner();//Scoty 20180621 modify close banner as function// connie 20180524 fix channel num show wrong in fav group
        }

        if(keycode == KEY_FAV) {
            if (curServiceType == ProgramInfo.ALL_TV_TYPE) {
                curServiceType = ProgramInfo.TV_FAV1_TYPE;
            } else if (curServiceType == ProgramInfo.ALL_RADIO_TYPE) {
                curServiceType = ProgramInfo.RADIO_FAV1_TYPE;
            }
        }

        CloseCurSubtitle();//Scoty 20180621 modify when subtitle showing and then open oklist/menu/Guide should close subtitle; back to ViewActivity show subtitle
        ShowOkList(OkList,curServiceType, curListPosition ,keycode,OkListDialogView.MODE_NORMAL,null);
    }

    public void OpenChannelNameFilterList(ArrayList<String> matches, int mode)
    {
        int curServiceType = GetServiceType();
        int curListPosition = 0;
        List<SimpleChannel> simpleChannelList = new ArrayList<>();
        OkListManagerImpl tmp = new OkListManagerImpl(ViewHistory.getCurGroupType(),simpleChannelList);
        List<OkListManagerImpl> OkList = new ArrayList<OkListManagerImpl>();
        OkList = GetOkList();
        OkList.clear();
        OkList.add(tmp);

        Log.d(TAG, "openChannelNameFilterList: " + ViewHistory.getCurChannel().getChannelId() +
                "position = " + curListPosition +
                "curGroup = " + curServiceType);

        if ( bannerView.GetVisibility() == View.VISIBLE )
        {
            CloseBanner();//Scoty 20180621 modify close banner as function// connie 20180524 fix channel num show wrong in fav group
        }

        CloseCurSubtitle();//Scoty 20180621 modify when subtitle showing and then open oklist/menu/Guide should close subtitle; back to ViewActivity show subtitle
        ShowOkList ( OkList, curServiceType, curListPosition, KEYCODE_DPAD_CENTER, mode, matches );
    }

    public void OpenDigitalFilterList(int keycode)
    {
        int curServiceType, curListPosition;
        List<SimpleChannel> simpleChannelList = new ArrayList<>();
        OkListManagerImpl tmp = new OkListManagerImpl(ViewHistory.getCurGroupType(),simpleChannelList);
        List<OkListManagerImpl> OkList = new ArrayList<OkListManagerImpl>();
        OkList = GetOkList();
        OkList.clear();
        OkList.add(tmp);

        curListPosition = 0;//current service num
        curServiceType = ViewHistory.getCurGroupType();//current service type
        Log.d(TAG, "OpenDigitalFilterList: " + ViewHistory.getCurChannel().getChannelId() + "position = " + curListPosition + "curGroup = "
                + curServiceType);

        if (bannerView.GetVisibility() == View.VISIBLE) {
            CloseBanner();//Scoty 20180621 modify close banner as function// connie 20180524 fix channel num show wrong in fav group
        }

        CloseCurSubtitle();//Scoty 20180621 modify when subtitle showing and then open oklist/menu/Guide should close subtitle; back to ViewActivity show subtitle
        ShowOkList( OkList,
                curServiceType,
                curListPosition,
                keycode,
                OkListDialogView.MODE_DIGITAL,
                null );
    }

    public void showTeletextDialog()
    {
        final TeletextInfo TeletextInfo = AvControlGetCurrentTeletext(ViewHistory.getPlayId());

        if (bannerView.GetVisibility() == View.VISIBLE) {
            CloseBanner();//Scoty 20180621 modify close banner as function// connie 20180524 fix channel num show wrong in fav group
        }

        if (TeletextInfo != null)
        {
            teletextDialogView = new TeletextDialogView(this,mDTVActivity,ViewHistory.getPlayId());
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

    private void startBookService() // Johnny add 20180307 for book time start
    {
        Intent startSrvIntent = new Intent(this, CallbackService.class);
        startSrvIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startService(startSrvIntent);
//        CommonDef.startServiceEx(this, startSrvIntent);
    }

    private void stopAllRec()//Scoty 20180720 add stop all records/timeshift
    {
        if (Pvcfg.getPVR_PJ() == false) // connie 20181101 for crash when pvr not support
            return ;
        List<PvrInfo> pvrList = new ArrayList<PvrInfo>();
        pvrList = PvrRecordGetAllInfo();
        if (pvrList.size() > 0) {
            for(int i = 0 ; i < pvrList.size() ; i++)
            {
                int recId =pvrList.get(i).getRecId();//Scoty 20180809 modify dual pvr rule
                int pvrMode = pvrList.get(i).getPvrMode();
                stopPVRMode(pvrMode, recId,1);//Scoty 20180809 modify dual pvr rule
            }
        }
    }

    private class pipFramRect
    {
        int x = 1166;
        int y = 76;
        int width = 664;
        int height = 384;
    }

    private class PipFrame extends androidx.appcompat.widget.AppCompatTextView // edwin 20180705 add PipFrame
    {
        final int width = 3;
        GradientDrawable border = new GradientDrawable(); // set yellow frame

        public PipFrame (Context context)
        {
            super(context);

            Log.d(TAG, "PipFrame: ");

            border.setShape(GradientDrawable.RECTANGLE);
            border.setStroke(width, Color.WHITE);
            border.setCornerRadius(3);
            setBackground(border);
            setVisibility(INVISIBLE);
            setLayoutParams(new LinearLayout.LayoutParams(500, 500));
            setFocusable(false);
            //            setOnFocusChangeListener(new OnFocusChangeListener()
            //            {
            //                @Override
            //                public void onFocusChange (View v, boolean hasFocus)
            //                {
            //                    if ( hasFocus )
            //                        setColor(Color.RED);
            //                    else
            //                        setColor(Color.YELLOW);
            //                }
            //            });
        }

        private void setColor(int color)
        {
            Log.d(TAG, "setColor: ");

            border.setStroke(width, color);
            setBackground(border);
        }

        private void setSwitch()
        {
            Log.d(TAG, "setSwitch: ");

            if ( getVisibility() == VISIBLE )
            {
                setVisibility(INVISIBLE);
            }
            else
            {
                setVisibility(VISIBLE);
            }
        }

        private void setPosition(float x, float y, int width, int height)
        {
            Log.d(TAG, "setPosition: ");

            setX(x);
            setY(y);
            setLayoutParams(new ConstraintLayout.LayoutParams(width, height));
        }
    }

    private class BookAlarmReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context arg0, Intent data)
        {
//            Bundle bundle = data.getExtras();
//            if (bundle == null)
//            {
//                return;
//            }
//
//            long channelID = bundle.getInt(CallbackService.DTV_BOOK_CHANNEL_ID);
//            ProgramInfo bookedChannel = ProgramInfoGetByChannelId(channelID);
//            if (null == bookedChannel)
//            {
//                return;
//            }
//
////            dismissView();    // release source, stop what is doing ...etc
//
//            Log.d(TAG, "onReceive: BookAlarmReceiver change channel : chID = " + channelID + "groupType = "+ groupType);
//            AvControlPlayByChannelId(ViewHistory.getPlayId(), channelID, groupType,1);
        }
    }

    private class BookArriveReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context arg0, Intent data)
        {
            Bundle bundle = data.getExtras();
            if (bundle == null)
            {
                return;
            }

            int playId = ViewHistory.getPlayId();//Scoty 20180810 fixed video not show when Timer start
            int playStatus = AvControlGetPlayStatus(playId);//Scoty 20180810 fixed video not show when Timer start
            long channelID = bundle.getLong(CallbackService.DTV_BOOK_CHANNEL_ID);
            int type = bundle.getInt(CallbackService.DTV_BOOK_TYPE);
            int duration = bundle.getInt(CallbackService.DTV_BOOK_DURATION);
            int mBookTaskID = bundle.getInt(CallbackService.DTV_BOOK_ID);
            ProgramInfo bookedChannel = ProgramInfoGetByChannelId(channelID);
            if (null == bookedChannel)
            {
                return;
            }
            viewUiDisplay.ClosePipScreen();
            pipFrame.SetVisibility(View.INVISIBLE);
            PipStop();
            PipClose();
            stopAllRec();//Scoty 20180720 add stop all records/timeshift
            CloseAllView(); // Johnny 20180803 close views when timer arrived
            Log.d(TAG, "onReceive: BookArriveReceiver change channel : chID = " + channelID + "type = " + type);
            if (ViewHistory.getCurChannel().getChannelId() != channelID ||
                    playStatus == HiDtvMediaPlayer.EnPlayStatus.IDLE.getValue()
                    || playStatus == HiDtvMediaPlayer.EnPlayStatus.STOP.getValue()
                    || playStatus == HiDtvMediaPlayer.EnPlayStatus.RELEASEPLAYRESOURCE.getValue())//Scoty 20180810 fixed video not show when Timer start
            {
                AvControlPlayByChannelId(ViewHistory.getPlayId(), channelID, bookedChannel.getType(),1);
            }

            if (BookInfo.BOOK_TYPE_RECORD == type)
            {
                int hour = duration/100;
                int min = duration%100;
                //Log.d(TAG, "onReceive:  duration =" + duration);
                RecordDuration = hour*60*60+min*60;

                if ( !CheckUsbPathAvailable(GetRecordPath())) // connie 20181024 for USB Path wrong
                {
                    StorageNotAvailableDialog.show();
                    return;
                }
                startRecord();
            }
        }
    }
    //Scoty 20180615 fixed timer not stop -s
    private class BookEndReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context arg0, Intent data)
        {
            Log.d(TAG, "onReceive: Stop Timer");
            //Scoty 20180720 add timer end rec id -s
            Bundle bundle = data.getExtras();
            if (bundle == null)
            {
                return;
            }
            int bookId = bundle.getInt(CallbackService.DTV_BOOK_ID);
            int recId = bundle.getInt(CallbackService.DTV_BOOK_REC_ID);

            stopPVRMode(PvrInfo.EnPVRMode.RECORD, recId,0);//Scoty 20180809 modify dual pvr rule//eric lin 20180629 stop pvr and av play
            //Scoty 20180720 add timer end rec id -e

            // Johnny 20180803 close oklist & stop multi rec dialog when book end -s
            if (okListDialog != null && okListDialog.isShowing())
            {
                okListDialog.dismiss();
            }

            if (mStopMultiRecDialog != null && mStopMultiRecDialog.isShowing())
            {
                mStopMultiRecDialog.dismiss();
            }
            // Johnny 20180803 close oklist & stop multi rec dialog when book end -e
        }
    }
    //Scoty 20180615 fixed timer not stop -e

    // for VMX need open/close -s
    public class VMXOtaStartReceiver extends BroadcastReceiver   // connie 20180903 for VMX -s
    {
        @Override
        public void onReceive(Context arg0, Intent data) {
            Bundle bundle = data.getExtras();
            if (bundle == null) {
                return;
            }
            int otaMode = bundle.getInt(CallbackService.DTV_VMX_OTA_MODE);
            int triggerID = bundle.getInt(CallbackService.DTV_VMX_TRIGGER_ID);
            int triggerNum = bundle.getInt(CallbackService.DTV_VMX_TRIGGER_NUM);
            int otaFreqNum = bundle.getInt(CallbackService.DTV_VMX_OTA_FREQ_NUM);
            ArrayList<Integer> freqList = bundle.getIntegerArrayList(CallbackService.DTV_VMX_OTA_FREQ_PARAM);//Scoty 20181207 modify VMX OTA rule
            ArrayList<Integer> bandwidthList = bundle.getIntegerArrayList(CallbackService.DTV_VMX_OTA_BANDWIDTH_PARAM);//Scoty 20181207 modify VMX OTA rule

            if (okListDialog != null && okListDialog.isShowing())
            {
                okListDialog.dismiss();
            }

            if (mStopMultiRecDialog != null && mStopMultiRecDialog.isShowing())
            {
                mStopMultiRecDialog.dismiss();
            }
            OTAstart(otaMode, triggerID, triggerNum, otaFreqNum, freqList, bandwidthList);
        }
    }

    public class VMXOtaProcReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent data) {
            Bundle bundle = data.getExtras();
            if (bundle == null)
                return;

            int otaErr = bundle.getInt(CallbackService.DTV_VMX_OTA_PROC_ERR);
            int otaSchedule = bundle.getInt(CallbackService.DTV_VMX_OTA_PROC_SCHEDULE);
            if (OTAMsg != null && OTAMsg.isShowing())
                OTAMsg.updateStatus(otaErr, otaSchedule);
        }
    }

    public class vmxWaterMarkReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context arg0, Intent data) {
            Bundle bundle = data.getExtras();
            if (bundle == null) {
                return;
            }
            int mode = bundle.getInt(CallbackService.DTV_VMX_WATERMARK_MODE);
            int dur = bundle.getInt(CallbackService.DTV_VMX_WATERMARK_DUR);
            String msg = bundle.getString(CallbackService.DTV_VMX_WATERMARK_MSG);
            int frameX = bundle.getInt(CallbackService.DTV_VMX_WATERMARK_FRAMEX);
            int frameY = bundle.getInt(CallbackService.DTV_VMX_WATERMARK_FRAMEY);
            int triggerID = bundle.getInt(CallbackService.DTV_VMX_TRIGGER_ID);
            int triggerNum = bundle.getInt(CallbackService.DTV_VMX_TRIGGER_NUM);
            dur = dur*100;// 0.1s -> s
            showWaterMark(mode, msg, dur, frameX, frameY, triggerID, triggerNum);
        }
    }

    public class vmxShowMsgReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context arg0, Intent data) {
            Bundle bundle = data.getExtras();
            if (bundle == null) {
                return;
            }

            int mode = bundle.getInt(CallbackService.DTV_VMX_SHOW_MSG_MODE);
            int dur = bundle.getInt(CallbackService.DTV_VMX_SHOW_MSG_DUR);
            String msg = bundle.getString(CallbackService.DTV_VMX_SHOW_MSG_MSG);
            //int triggerID = bundle.getInt(CallbackService.DTV_VMX_TRIGGER_ID);
            //int triggerNum = bundle.getInt(CallbackService.DTV_VMX_TRIGGER_NUM);

            Log.d(TAG, "onReceive: mode =" + mode + "   dur =" + dur + "    msg =" + msg);
            dur = dur*1000;// 0.1s -> s
            showCAMsg( mode,  msg,  dur);
        }
    }

    public class vmxSearchReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context arg0, Intent data) {
            Bundle bundle = data.getExtras();
            if (bundle == null) {
                return;
            }
            int mode = bundle.getInt(CallbackService.DTV_VMX_SEARCH_MODE);
            int startFreq = bundle.getInt(CallbackService.DTV_VMX_SEARCH_START_FREQ);
            int endFreq = bundle.getInt(CallbackService.DTV_VMX_SEARCH_END_FREQ);
            int triggerID = bundle.getInt(CallbackService.DTV_VMX_TRIGGER_ID);
            int triggerNum = bundle.getInt(CallbackService.DTV_VMX_TRIGGER_NUM);
            Log.d(TAG, "onReceive:  Search Mode = " + mode + "    startFreq =" + startFreq + "  endFreq = "+ endFreq);
            mode = TVScanParams.SCAN_VMX_SEARCH;
            VMX_SearchProc(mode, startFreq, endFreq, triggerID, triggerNum);
        }
    } // connie 20180903 for VMX -e

    public class vmxE16MsgReceiver extends BroadcastReceiver // connie 20180925 for VMX callback-s
    {
        @Override
        public void onReceive(Context arg0, Intent data) {
            Bundle bundle = data.getExtras();
            if (bundle == null) {
                return;
            }

            int enable = bundle.getInt(CallbackService.DTV_VMX_E16_ENABLE);

            Log.d(TAG, "onReceive: enable =" + enable );
            if(enable == 0) {
                if(mCaMessagView != null) {//Scoty 20180801 fixed error message not clean when exit apk
                    mCaMessagView.remove();
                    mCaMessagView = null;
                }

                E16Msg.SetVisibility(mDTVActivity, VISIBLE);
            }
            else
                E16Msg.SetVisibility(mDTVActivity, INVISIBLE);
        }
    }

    public class vmxEWBSReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context arg0, Intent data) {
            Bundle bundle = data.getExtras();
            if (bundle == null) {
                return;
            }
//Scoty 20181225 modify VMX EWBS rule -s//Scoty 20181218 add VMX EWBS mode -s
            int ewbs_mode = bundle.getInt(CallbackService.DTV_VNX_EWBS_MODE);
            int ewbs_start_end = bundle.getInt(CallbackService.DTV_VMX_EWBS_START_END);
            int ewbs_signal_level = bundle.getInt(CallbackService.DTV_VMX_EWBS_SIGNAL_LEVEL);
            int ewbs_channel_id = bundle.getInt(CallbackService.DTV_VMX_EWBS_CHANNEL_ID, 0);
            Log.d(TAG, "onReceive: ewbs_start_end =" + ewbs_start_end + "   ewbs_signal_level="+ewbs_signal_level);

            // Edwin 20190509 fix dialog not focus -s
            Handler handler = new Handler();
            Runnable showDialog = new Runnable() {
                @Override
                public void run () {
                    ewsDialog.show();
                }
            };

            if(ewbs_channel_id == ViewHistory.getCurChannel().getChannelId()) {
                if (ewbs_mode == 0) {
                    if (ewbs_start_end == 1 && ewsDialog == null) // 0: close  1: open
                    {
                        ewsDialog = new EWSDialog(mDTVActivity, ViewActivity.this, ewbs_signal_level, ewbs_mode) {
                            public void dialogEnd() {
                                //VMX_EWBS_MODE = -1;
                                ewsDialog = null;
                            }
                        };
                        handler.post(showDialog);
                    } else if (ewbs_start_end == 0 && ewsDialog != null) {
                        ewsDialog.dismiss();
                        ewsDialog = null;
                    }
                } else {
                    ewsDialog = new EWSDialog(mDTVActivity, ViewActivity.this, ewbs_signal_level, ewbs_mode) {
                        public void dialogEnd() {
                            ewsDialog = null;
                        }
                    };
                    handler.post(showDialog);
                }
            }
            // Edwin 20190509 fix dialog not focus -e
//Scoty 20181225 modify VMX EWBS rule -e//Scoty 20181218 add VMX EWBS mode -e            //return;
        }
    }

    public class vmxChBlockReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context arg0, Intent data) {
            Bundle bundle = data.getExtras();
            if (bundle == null) {
                return;
            }

            int enable = bundle.getInt(CallbackService.DTV_VMX_CHBLOCK_MODE);
            int triggerID = bundle.getInt(CallbackService.DTV_VMX_TRIGGER_ID);
            int triggerNum = bundle.getInt(CallbackService.DTV_VMX_TRIGGER_NUM);
            Log.d(TAG, "onReceive: enable =" + enable + "   triggerID = " + triggerID + "  triggerNum =" + triggerNum );

            if(enable == 0) // close block
            {
                SetVMXBlockFlag(0);
                VMXBLOCK.setVisibility(View.INVISIBLE);
                //caMsgView.ViewCAMsg(View.INVISIBLE, 0, null, 0);
                AvControlShowVideo(ViewHistory.getPlayId(), true);
                AvControlSetMute(ViewHistory.getPlayId(), false);
                VMXOsmFinish(triggerID, triggerNum);
            }
            else // open block
            {
                SetVMXBlockFlag(1);
                VMXBLOCK.setVisibility(View.VISIBLE);
                //caMsgView.ViewCAMsg(View.VISIBLE, 0, getString(R.string.STR_VMX_CHBLOCK_MSG), 0);
                AvControlShowVideo(ViewHistory.getPlayId(), false);
                AvControlSetMute(ViewHistory.getPlayId(), true);
                VMXOsmFinish(triggerID, triggerNum);
            }
        }
    }

    public class vmxMailReceiver extends BroadcastReceiver // connie 20181116 for vmx mail
    {
        @Override
        public void onReceive(Context arg0, Intent data) {
            Bundle bundle = data.getExtras();
            if (bundle == null) {
                return;
            }

            DatabaseHandler mailHandle = GetMailHandler();
            String mailTableName = getMailTableName();
            int MailType = bundle.getInt(CallbackService.DTV_VMX_MAIL_TYPE);
            String MailMsg = bundle.getString(CallbackService.DTV_VMX_MAIL_MSG);
            int triggerID = bundle.getInt(CallbackService.DTV_VMX_TRIGGER_ID);
            int triggerNum = bundle.getInt(CallbackService.DTV_VMX_TRIGGER_NUM);
            Log.d(TAG, "onReceive: MailMsg =" + MailMsg + "   MailType = " + MailType + "     triggerID =" + triggerID + "    triggerNum =" + triggerNum);
            String uuID = UUID.randomUUID().toString();
            if(MailType == MailInfo.NORMAL)
            {
                mailIcon.setVisibility(VISIBLE);
                mailIcon.requestLayout();
                long mailCount = mailHandle.getRowCount(mailTableName);
                if(mailCount >= 10)
                    mailHandle.deleteFrom(mailTableName, 1);

                List<DatabaseHandler.Column> info = new ArrayList<>();
                info.add(new DatabaseHandler.Column(getString(R.string.STR_MAIL_UUID), uuID));
                info.add(new DatabaseHandler.Column(getString(R.string.STR_MAIL), MailMsg));
                info.add(new DatabaseHandler.Column(getString(R.string.STR_READ), MailInfo.MAILUNREAD));
                mailHandle.append(mailTableName, info);
            }
            else // force
            {
                MailInfo mail = new MailInfo();
                mail.setMailMsg(MailMsg);
                // Edwin 20190509 fix dialog not focus -s
                final MailInfoDialog dialog = new MailInfoDialog(ViewActivity.this, mail);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run () {
                        dialog.mDialog.show();
                    }
                }, 150);
                // Edwin 20190509 fix dialog not focus -e
            }
            VMXOsmFinish(triggerID, triggerNum);
        }
    }

    public class vmxFactoryReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context arg0, Intent data) {
            Bundle bundle = data.getExtras();
            int triggerID = bundle.getInt(CallbackService.DTV_VMX_TRIGGER_ID);
            int triggerNum = bundle.getInt(CallbackService.DTV_VMX_TRIGGER_NUM);
            Log.d(TAG, "onReceive: triggerID =" + triggerID + "    triggerNum=" + triggerNum);

            stopAllRec();
            // ===== Stop PIP ======
            if (ViewHistory.getCurPipChannel() != null) {
                if (pipFrame.getVisibility() == View.VISIBLE) {
                    viewUiDisplay.ClosePipScreen();
                    pipFrame.SetVisibility(View.INVISIBLE);
                }
            }
            // ==== Close Banner & Detail =====
            if (detailView.GetVisibility() == View.VISIBLE)
                detailView.SetVisibility(View.INVISIBLE, null, 0, 0, 0, "");
            CloseBanner();


            ResetDefault();
            AvControlPlayStop(MiscDefine.AvControl.AV_STOP_ALL);
            AvControlClose(ViewHistory.getPlayId());
            SetChannelExist(0);
            ChannelHistory.Reset();
            ResetTotalChannelList();

            Intent intent = new Intent();
            intent.setClass(ViewActivity.this, ScanResultActivity.class);

            bundle = new Bundle();
            bundle.putInt(getString(R.string.STR_EXTRAS_TPID), 0);
            bundle.putInt(getString(R.string.STR_EXTRAS_SEARCHMODE), TVScanParams.SCAN_MODE_AUTO); // 0 : manaul  1 : auto 2 : network 3 : all sat 4: vmx search
            bundle.putInt(getString(R.string.STR_EXTRAS_SCANMODE), 0); // 0 : All  1 : FTA  2 : $
            bundle.putInt(getString(R.string.STR_EXTRAS_CHANNELTYPE), 0); // 0 : All  1 : TV  2 : Radio
            bundle.putInt(getString(R.string.STR_EXTRAS_VMX_TRIGGER_ID), triggerID);
            bundle.putInt(getString(R.string.STR_EXTRAS_VMX_TRIGGER_NUM), triggerNum);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }// connie 20180925 for VMX callback-e
//Scoty 20181225 modify VMX EWBS rule
    public class vmxEWBSStopReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context arg0, Intent data) {
            Log.d(TAG, "vmxEWBSStopReceiver onReceive:");
            VMXStopEWBS(0);
        }
    }
    // for VMX need open/close -e

    private boolean isAppForeground(){
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = manager.getRunningAppProcesses();
        if(appProcessInfos == null || appProcessInfos.isEmpty()){
            return false;
        }
        for(ActivityManager.RunningAppProcessInfo info : appProcessInfos){
            if(info.processName.equals(getPackageName()) && info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                return true;
            }
        }
        return false;
    }

    private class HomeKeyReceiver extends BroadcastReceiver {
        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null &&
                    (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS) || action.equals(Intent.ACTION_SCREEN_OFF)
                    || action.equals(getString(R.string.STR_INTERNAL_BROADCAST_HOMEKEY)) // jim 2018/09/27 fix home key broadcast timming cause crash when switch third part app and DTVPlayer
                    || action.equals(Intent.ACTION_SCREEN_ON)))//Scoty 20180831 add standby function// connie 20180802 for add ACTION_SCREEN_OFF
            {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                Log.d(TAG, "onReceive: action:" + action + ",reason:" + reason);
//                final int pvrMode =getCurrentPvrMode();//final int pvrMode =PvrGetCurrentRecMode();
              //  if ((reason != null && reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY) )/*||
              //          (action.equals(Intent.ACTION_SCREEN_OFF))*/) //Scoty 20180831 add standby function// connie 20180802 for add ACTION_SCREEN_OFF
                if ( ( action.equals(getString(R.string.STR_INTERNAL_BROADCAST_HOMEKEY))
                    || action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS) ) && isAppForeground()
                    && reason != null && reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY))  // jim 2018/09/27 fix home key broadcast timming cause crash when switch third part app and DTVPlayer
                {
                    stopEwbsDialog();//Scoty 20181218 add VMX EWBS mode
                    //Scoty 20180712 for pip
                    if(ViewHistory.getCurPipChannel() != null)
                    {
                        viewUiDisplay.ClosePipScreen();
                        pipFrame.SetVisibility(View.INVISIBLE);
                    }
//                        if(getCurrentPvrMode() != PvrInfo.EnPVRMode.NO_ACTION) {//if(PvrGetCurrentRecMode() != NO_ACTION) {
//                            //Log.d(TAG, "onReceive: TOST HomeKeyReceiver");
//                            stopPVRMode(pvrMode, 0);//eric lin 20180629 stop pvr and av play
//                        }
                    if(caMsgView != null) // connie 20181101 fixed error message not clean when exit apk
                        caMsgView.SetVisibility(View.INVISIBLE);
                    if(mCaMessagView != null) {//Scoty 20180801 fixed error message not clean when exit apk
                        mCaMessagView.remove();
                        mCaMessagView = null;
                    }

                    if( mFingerPringView!=null ) { // for VMX need open/close -s
                        mFingerPringView.remove();
                        mFingerPringView = null;// for VMX need open/close -e
                    }

                    if(BootAVPlayHandler!=null)
                    {
                        BootAVPlayHandler.removeCallbacks(BootAVPlayRunnable);
                        BootAVPlayHandler = null;
                    }
                    if( TEST_MODE == 1) {//Scoty 20181001 fixed auto change channel show no video signal
                        removeAutoChangeCh();
                    }

                    stopAllRec();//Scoty 20180720 add stop all records/timeshift
                    PvrPlayStop();
                    AvControlPlayStop(MiscDefine.AvControl.AV_STOP_ALL);
                    AvControlClose(ViewHistory.getPlayId());
                    ScanParamsStopScan(false);
                    if ( countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, sCreenOffTime);//Scoty 20181129 set Screen Saver Time
                    //finish(); // jim 2018/08/22 fix home key not close other activity if finish viewactivity first, because launchMode="singleTask" behavior

                    RecommendChannel Recommend = new RecommendChannel(ViewActivity.this);
                    Recommend.SetPesiLauncher(ViewHistory); // connie 20190819 modify for home key update database
                }
                else if(action.equals(Intent.ACTION_SCREEN_OFF))//Scoty 20180831 add standby function
                {
                    Log.d(TAG, "onReceive: ===>>> ACTION_SCREEN_OFF");
                    PvrPlayStop();
                    ScanParamsStopScan(false);

                    Intent startSingleTask = new Intent();
                    startSingleTask.setClass(context, ViewActivity.class);
                    startSingleTask.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(startSingleTask);

                    CloseAllView();
                    if(ViewHistory.getCurPipChannel() != null)
                    {
                        viewUiDisplay.ClosePipScreen();
                        pipFrame.SetVisibility(View.INVISIBLE);
                    }

                    int pvrMode = getCurrentPvrMode();
                    if(CheckPvrMode(pvrMode)){//Stop TimeShift
                        stopPVRMode(pvrMode,0, 1);//eric lin 20180629 stop pvr and av play
                    }
                    if(mCaMessagView != null) {//Scoty 20180801 fixed error message not clean when exit apk
                        mCaMessagView.remove();
                        mCaMessagView = null;
                    }

                    if ( countDownTimer != null) {
                        countDownTimer.cancel();
                    }

                    ChangeProgrmaToABSchannel();
                    SetStandbyOnOff(1);//standby settings on
                }
                else if(action.equals(Intent.ACTION_SCREEN_ON))//Scoty 20180831 add standby function
                {
                    Log.d(TAG, "onReceive: ===>>> ACTION_SCREEN_ON");
                    SetStandbyOnOff(0);//standby settings off
                    if(mCaMessagView != null) {//Scoty 20180801 fixed error message not clean when exit apk
                        mCaMessagView.remove();
                        mCaMessagView = null;
                    }
                }
            }
        }
    }

    private void subBroadcast()
    {
        //  *****  Book ******
        IntentFilter bookAlarmFilter = new IntentFilter(CallbackService.DTV_BOOK_ALARM_REMIND_PLAY);
        mBookAlarmReceiver = new BookAlarmReceiver();
        registerReceiver(mBookAlarmReceiver, bookAlarmFilter, CallbackService.PERMISSION_DTV_BROADCAST, null);

        IntentFilter bookArriveFilter = new IntentFilter(CallbackService.DTV_BOOK_ALARM_ARRIVE_PLAY);
        mBookArriveReceiver = new BookArriveReceiver();
        registerReceiver(mBookArriveReceiver, bookArriveFilter, CallbackService.PERMISSION_DTV_BROADCAST, null);

        //Scoty 20180615 fixed timer not stop
        IntentFilter bookEndFilter = new IntentFilter(CallbackService.DTV_BOOK_ALARM_END);
        mBookEndReceiver = new BookEndReceiver();
        registerReceiver(mBookEndReceiver, bookEndFilter, CallbackService.PERMISSION_DTV_BROADCAST, null);

        // for VMX need open/close -s
        if(Pvcfg.getCAType() == Pvcfg.CA_VMX) {
            IntentFilter vmxOTAstartFilter = new IntentFilter(CallbackService.DTV_VMX_OTA_START_PLAY);
            mVMXOtaStartReceiver = new VMXOtaStartReceiver();
            registerReceiver(mVMXOtaStartReceiver, vmxOTAstartFilter, CallbackService.PERMISSION_DTV_BROADCAST, null);

            IntentFilter vmxOTAPorcFilter = new IntentFilter(CallbackService.DTV_VMX_OTA_PROC);
            mVMXOtaProcReceiver = new VMXOtaProcReceiver();
            registerReceiver(mVMXOtaProcReceiver, vmxOTAstartFilter, CallbackService.PERMISSION_DTV_BROADCAST, null);

            IntentFilter vmxWatermarkFilter = new IntentFilter(CallbackService.DTV_VMX_WATERMARK);
            mVMXWatermarkReceiver = new vmxWaterMarkReceiver();
            registerReceiver(mVMXWatermarkReceiver, vmxWatermarkFilter, CallbackService.PERMISSION_DTV_BROADCAST, null);


            IntentFilter vmxShowMsgFilter = new IntentFilter(CallbackService.DTV_VMX_SHOW_MSG);
            mVMXShowMsgReceiver = new vmxShowMsgReceiver();
            registerReceiver(mVMXShowMsgReceiver, vmxShowMsgFilter, CallbackService.PERMISSION_DTV_BROADCAST, null);

            IntentFilter vmxSearchFilter = new IntentFilter(CallbackService.DTV_VMX_SEARCH_PLAY);
            mVMXSearchReceiver = new vmxSearchReceiver();
            registerReceiver(mVMXSearchReceiver, vmxSearchFilter, CallbackService.PERMISSION_DTV_BROADCAST, null); // connie 20180903 for VMX-e

            IntentFilter vmxE16MsgFilter = new IntentFilter(CallbackService.DTV_VMX_E16); // connie 20180925 for VMX callback -s
            mVMXE16MsgReceiver = new vmxE16MsgReceiver();
            registerReceiver(mVMXE16MsgReceiver, vmxE16MsgFilter, CallbackService.PERMISSION_DTV_BROADCAST, null);

            IntentFilter vmxEWBSFilter = new IntentFilter(CallbackService.DTV_VMX_EWBS_PLAY);
            mVMXEWBSReceiver = new vmxEWBSReceiver();
            registerReceiver(mVMXEWBSReceiver, vmxEWBSFilter, CallbackService.PERMISSION_DTV_BROADCAST, null);

            IntentFilter vmxFactoryFilter = new IntentFilter(CallbackService.DTV_VMX_FACTORY_PLAY);
            mVMXFactoryReceiver = new vmxFactoryReceiver();
            registerReceiver(mVMXFactoryReceiver, vmxFactoryFilter, CallbackService.PERMISSION_DTV_BROADCAST, null);// connie 20180925 for VMX callback -e

            IntentFilter vmxBlockFilter = new IntentFilter(CallbackService.DTV_VMX_CHBLOCK);
            mVMXBlockReceiver = new vmxChBlockReceiver();
            registerReceiver(mVMXBlockReceiver, vmxBlockFilter, CallbackService.PERMISSION_DTV_BROADCAST, null);

            IntentFilter vmxMailFilter = new IntentFilter(CallbackService.DTV_VMX_MAIL);
            mVMXMailReceiver = new vmxMailReceiver();
            registerReceiver(mVMXMailReceiver, vmxMailFilter, CallbackService.PERMISSION_DTV_BROADCAST, null);
            //Scoty 20181225 modify VMX EWBS rule
            IntentFilter vmxStopEWBSFilter = new IntentFilter(CallbackService.DTV_VMX_EWBS_STOP_PLAY);
            mVMXEWBSStopReceiver = new vmxEWBSStopReceiver();
            registerReceiver(mVMXEWBSStopReceiver, vmxStopEWBSFilter, CallbackService.PERMISSION_DTV_BROADCAST, null);


        }
        // for VMX need open/close -e

        //***** system ******
        IntentFilter homeKeyFilter = new IntentFilter(); // connie 20180802 for add ACTION_SCREEN_OFF
        homeKeyFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        homeKeyFilter.addAction(Intent.ACTION_SCREEN_OFF);
        homeKeyFilter.addAction(Intent.ACTION_SCREEN_ON);//Scoty 20180831 add standby function
        homeKeyFilter.addAction(getString(R.string.STR_INTERNAL_BROADCAST_HOMEKEY)); // jim 2018/09/27 fix home key broadcast timming cause crash when switch third part app and DTVPlayer
        mHomeKeyReceiver = new HomeKeyReceiver();
        registerReceiver(mHomeKeyReceiver, homeKeyFilter);
    }

    private void unSubBroadcast()
    {
        // ****  Book ****
        unregisterReceiver(mBookAlarmReceiver);
        mBookAlarmReceiver = null;
        unregisterReceiver(mBookArriveReceiver);
        mBookArriveReceiver = null;
        unregisterReceiver(mBookEndReceiver);//Scoty 20180615 fixed timer not stop
        mBookEndReceiver = null;
        unregisterReceiver(mHomeKeyReceiver);
        mHomeKeyReceiver = null;

        // for VMX need open/close -s
        if(Pvcfg.getCAType() == Pvcfg.CA_VMX) {
            unregisterReceiver(mVMXOtaStartReceiver);
            mVMXOtaStartReceiver = null;
            unregisterReceiver(mVMXWatermarkReceiver);
            mVMXWatermarkReceiver = null;
            unregisterReceiver(mVMXSearchReceiver);
            mVMXSearchReceiver = null;
            unregisterReceiver(mVMXE16MsgReceiver);
            mVMXE16MsgReceiver = null;
            unregisterReceiver(mVMXEWBSReceiver);
            mVMXEWBSReceiver = null;
            unregisterReceiver(mVMXFactoryReceiver);
            mVMXFactoryReceiver = null;
            unregisterReceiver(mVMXBlockReceiver);
            mVMXBlockReceiver = null;
            unregisterReceiver(mVMXMailReceiver);
            mVMXMailReceiver = null;
            unregisterReceiver(mVMXEWBSStopReceiver);//Scoty 20181225 modify VMX EWBS rule
            mVMXEWBSStopReceiver = null;
        }
        // for VMX need open/close -e
    }

    final Runnable CheckStatusRunnable = new Runnable() {
        public void run() {
            // TODO Auto-generated method stub
            if (detailView.GetVisibility() == View.VISIBLE) {
                int strength = TunerGetStrength(0);//Scoty 20180626 move here get strength quality when detail exist
                int quality = TunerGetQuality(0);//Scoty 20180626 move here get strength quality when detail exist
                String ber = TunerGetBER(0);
                detailView.UpdateTunerStatus( strength, quality, ber);
            }

            CheckSignalHandler.postDelayed(CheckStatusRunnable, 1000);

        }
    };

    public boolean startRecord()
    {
        int ret = 0;
        long PvrFreeSize=0;
        String RecMountPath = GetRecordPath();
        String RecPath, RecFileName;
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MMM-dd-HH-mm");
        boolean updateChannelFlag = false;//Scoty 20180809 modify dual pvr rule
        //Scoty 20180620 remove 4k record not allow message
//        if (AvControlGetVideoResolutionWidth(ViewHistory.getPlayId()) > 1920)
//        {
//            //Log.d(TAG, "startRecord:  AvControlGetVideoResolutionWidth() =" + AvControlGetVideoResolutionWidth(ViewHistory.getPlayId()));
//            Log.d(TAG, "startRecord: 4K stream and 4K display out Do not support now");
//            showPVRErrMsg(1);
//            return false;
//        }

        File file = new File(RecMountPath);
        if (!file.exists())
        {
            Log.d(TAG, "startRecord:  Record Path Not Exit ! path = " + GetRecordPath() );
            SetRecordPath(getDefaultRecPath()); // connie 20180525 get path by getDefaultRecPath()
            file = new File(getDefaultRecPath());
            if (!file.exists()) {
                Log.d(TAG, "startRecord:  Record Path Not Exit ! path = " + GetRecordPath() );
                showPVRErrMsg(2);
                return false;
            }
        }

        PvrFreeSize = file.getUsableSpace();
        Log.d(TAG, "startRecord:  RecPath = " + RecMountPath + "       PvrFreeSize =" + PvrFreeSize);
        if(PvrFreeSize <= DEFAULT_MIN_REC_SPACE)//100 MB
        {
            Log.d(TAG, "startRecord: There is no Free Space to Record !  PvrFreeSize = " + PvrFreeSize);
            showPVRErrMsg(3);
            return false;
        }

        //Scoty 20180716 add max record num -s
        List<PvrInfo> pvrList= new ArrayList<PvrInfo>();
        pvrList = PvrRecordGetAllInfo();
        Log.d(TAG, "startRecord: record_num = " + pvrList.size() + " max Record Num = " + PvrRecordGetMaxRecNum());
        if(pvrList.size() >= PvrRecordGetMaxRecNum())//check max record num
        {
            showPVRErrMsg(4);
            return false;
        }
        if((pvrList.size()+1) == GetTunerNum())//Scoty 20181113 add for dual tuner pvrList
        {
            updateChannelFlag = true;
        }
        //Scoty 20180716 add max record num -e

        Date beginTime = GetLocalTime();
        if (beginTime == null)
        {
            Log.d(TAG, "startRecord:  Can't get current time !");
        }

        RecPath = RecMountPath + "/Records" ;
        File dirFile = new File(RecPath);
        if(!dirFile.exists()) {
            Log.d(TAG, "startRecord:  Add Dir ==> RecPath =" +RecPath);
            dirFile.mkdir();
        }

        String recordName = ViewHistory.getCurChannel().getChannelName();
        recordName = recordName.replace('/', '_');//Scoty 20180529 fixed rec name replace '/' to '_'
        RecFileName = recordName + formatter.format(beginTime.getTime())+".ts";
        RecPath = RecPath + "/" + RecFileName;

        Log.d(TAG, "startRecord:  Rec Full Path =" + RecPath);
        //int startRecRet = PvrRecordStart( ViewHistory.getPlayId() , ViewHistory.getCurChannel().getChannelId(), RecPath, RecordDuration);
        int startRecRet = 0;
        //int RecID = PvrRecordStart( ViewHistory.getPlayId() , ViewHistory.getCurChannel().getChannelId(), RecPath, RecordDuration);//Scoty 20180809 modify dual pvr rule//eric lin 20180712 pesi pvr for one rec
        int RecID = Record_Start_V2_with_Duration(ViewHistory.getCurChannel().getChannelId(), RecordDuration, false, null);
        Log.d(TAG, "startRecord:  RecID = [" + RecID + "]");
        if (-1 != RecID)//if (0 == startRecRet)//eric lin 20180712 pesi pvr for one rec
        {
            new MessageDialogView(mContext,getString(R.string.STR_START_RECORDING),3000)
            {
                public void dialogEnd() {
                    ShowBanner(1);
                }
            }.show();
        }
        else
        {
//            if(startRecRet == -2)
//                showPVRErrMsg(3);
//            else
//                showPVRErrMsg(4);
            showPVRErrMsg(0);
            return false;
        }

        //Scoty 20180615 move set pvrSkip channel here for timer not work -s
        ProgramInfo Program = ProgramInfoGetByChannelId(ViewHistory.getCurChannel().getChannelId());
        if(updateChannelFlag) {//Scoty 20180809 modify dual pvr rule
            pvrList = PvrRecordGetAllInfo();
            //Scoty 20181113 add for dual tuner pvrList -s
            List<Integer> pvrTpList = new ArrayList<>();
            for(int i = 0 ; i < pvrList.size() ; i++) {
                long channelId = pvrList.get(i).getChannelId();
                SimpleChannel channel = ProgramInfoGetSimpleChannelByChannelId(channelId);

                if(i == 0)
                    pvrTpList.add(channel.getTpId());
                else
                {
                    for(int j = 0 ; j < pvrTpList.size() ; j++)
                    {
                        if(channel.getTpId() != pvrTpList.get(j)) {
                            pvrTpList.add(channel.getTpId());
                            break;
                        }
                    }
                }
            }
            //Scoty 20181113 add for dual tuner pvrList -e
            UpdatePvrSkipList(ViewHistory.getCurGroupType(), 1, Program.getTpId(),pvrTpList);//Scoty 20181113 add for dual tuner pvrList
        }
        //List<SimpleChannel> simpleChannelList =ProgramInfoGetPlaySimpleChannelList(ViewHistory.getCurGroupType(), 1);
        //Scoty 20180615 move set pvrSkip channel here for timer not work -e
        //mRecImageView = new RecImageView(ViewActivity.this);//Scoty 20180629 show Rec Icon on top
        if(GposInfoGet().getRecordIconOnOff() == 1)//Scoty 20180806 check record Icon show or not by gpos
            mRecImageView.SetVisibility(true);

        return true;
    }

    public void stopPVRMode( int pvrMode, int recId ,int playav)//Scoty 20180809 modify dual pvr rule//eric lin 20180629 stop pvr and av play
    {
        //Scoty 20180615 move set pvrSkip channel here for timer not work-s
        List<SimpleChannel> simpleChannelList = null;
        //UpdatePvrSkipList(ViewHistory.getCurGroupType(),0,0);
        simpleChannelList =ProgramInfoGetPlaySimpleChannelList(ViewHistory.getCurGroupType(), 0);
        //Scoty 20180615 set pvrSkip channel here for timer not work-e

        stopPVR(pvrMode, recId);//Scoty 20180809 modify dual pvr rule//eric lin 20180712 pesi pvr for one rec
        //mRecID = -1;//eric lin 20180712 pesi pvr for one rec
        if(pvrBanner.getVisibility() == VISIBLE)
            pvrBanner.setVisibility(mDTVActivity, INVISIBLE, 0,isFocusPip());//Scoty 20180802 fixed cur time not update when focus on Pip

        if(mRecImageView != null)//Scoty 20180629 show Rec Icon on top
        {
            if(GposInfoGet().getRecordIconOnOff() == 1) {//Scoty 20180806 check record Icon show or not by gpos
                if (!IsRecExist())//Scoty 20180801 add for check all recording is exist or not
                    mRecImageView.SetVisibility(false);
            }
        }

        if(playav == 1 && (CheckPvrMode(pvrMode))) {//Scoty 20180827 add and modify TimeShift Live Mode//eric lin 20180629 stop pvr and av play
            //Log.d(TAG, "stopPVRMode: TOST playav=1");
            AvControlPlayByChannelId(ViewHistory.getPlayId(), ViewHistory.getCurChannel().getChannelId(), ViewHistory.getCurGroupType(), 1);
        }
    }

    public void showPVRErrMsg( int errType)
    {
        String msg = "";
        if(errType == 1) // 4K not Support
            msg = getString(R.string.STR_4K_NOT_SUPPORT);
        else if(errType == 2)
            msg = getString(R.string.STR_RECORD_PATH_NOT_EXIST); // edwin 20180709 change "exit" to "exist"
        else if(errType == 3)
            msg = getString(R.string.STR_THERE_IS_NO_FREE_SPACE_TO_RECORD);
        else if(errType == 4)//Scoty 20180716 add max record num
            msg = getString(R.string.STR_RECORD_IS_ALREADY_MAX);
        else
            msg = getString(R.string.STR_RECORD_FAIL);
        new MessageDialogView(mContext, msg,3000)
        {
            public void dialogEnd() {
            }
        }.show();
    }

    public void setRecDuration(int dur)
    {
        RecordDuration = dur;
    }

    // jim 2018/07/13 add start speechrecognizer in dtvplayer -s
    private int isSpeaking = 0 ;
    private CountDownTimer speechTimer = null ;
    private void startSpeechListening()
    {
        if ( isSpeaking == 0 ) {
            isSpeaking = 1 ;
            speech.startListening(recognizerIntent);
            speechTimer = new CountDownTimer( 1000*5, 1000)
            {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    isSpeaking = 0 ;
                    speech.stopListening();
                }
            };
            speechTimer.start();
        }
    }

    private BroadcastReceiver mSpeechRecognizerBroadCastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d( TAG, intent.getAction() ) ;
            if ( intent.getAction() == START_SPEECHRECOGNIZER_BROADCAST )
            {
                startSpeechListening() ;
            }
        }
    };

    private void deinit_SpeechRecognizer()
    {
        unregisterReceiver(mSpeechRecognizerBroadCastReceiver);
        speech.stopListening();
        if (speech != null) {
            speech.destroy();
            Log.i(TAG, "destroy");
        }
        if ( speechTimer != null )
            speechTimer.cancel();
    }

    private void InitSpeechRecognizer()
    {
        isSpeaking = 0 ;
        speechTimer = null ;
        IntentFilter IntentFilter = new IntentFilter();
        IntentFilter.addAction(START_SPEECHRECOGNIZER_BROADCAST);
        registerReceiver(mSpeechRecognizerBroadCastReceiver, IntentFilter);

        requestPermissions(this);
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.getDefault());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }

    private static String[] PERMISSIONS_ARRAY = {
            Manifest.permission.RECORD_AUDIO,
            };
    private static void requestPermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions( activity, PERMISSIONS_ARRAY,1 );
        }
    }


    @Override
    public void onReadyForSpeech(Bundle bundle) {
        Log.i(TAG, "onReadyForSpeech");
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(TAG, "onRmsChanged: " + rmsdB);
    }

    @Override
    public void onBufferReceived(byte[] bytes) {
        Log.i(TAG, "onBufferReceived: " + bytes);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(TAG, "onEndOfSpeech");
        isSpeaking = 0 ;
        if ( speechTimer != null )
            speechTimer.cancel();
    }
    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(TAG, "FAILED " + errorMessage);
        isSpeaking = 0 ;
        if ( speechTimer != null )
            speechTimer.cancel();
    }

    @Override
    public void onResults ( Bundle bundle )
    {
        if ( isFocusPip() )
            return;

        Thread channelUp = new Thread( new Runnable() {
            @Override
            public void run () {
                Instrumentation inst = new Instrumentation();
                inst.sendKeyDownUpSync( KEYCODE_CHANNEL_UP );
            }
        } );
        Thread channelDown = new Thread( new Runnable() {
            @Override
            public void run () {
                Instrumentation inst = new Instrumentation();
                inst.sendKeyDownUpSync( KEYCODE_CHANNEL_DOWN );
            }
        } );
        ArrayList<String> matches = bundle.getStringArrayList( SpeechRecognizer.RESULTS_RECOGNITION );
        String result;
        int i;

        if ( matches == null )
        {
            isSpeaking = 0;
            if ( speechTimer != null )
            {
                speechTimer.cancel();
            }
            return;
        }

        for ( i = 0; i < matches.size(); i++ )
        {
            result = matches.get( i );

            if ( IsChannelUp( result ) )
            {
                Log.d( TAG, "onResults: channel up" );
                channelUp.start();
                break;
            }
            else if ( IsChannelDown( result ) )
            {
                Log.d( TAG, "onResults: channel down" );
                channelDown.start();
                break;
            }
            else if ( IsNumber( result ) )
            {
                int curServiceType = GetServiceType();
                if ( okListDialog != null && okListDialog.isShowing() )
                {
                    Log.d( TAG, "onResults: update channel number" );
                    okListDialog.UpdateFilter( matches, curServiceType, OkListDialogView.MODE_CHANNEL_NUM );
                }
                else
                {
                    Log.d( TAG, "onResults: open by channel number" );
                    OpenChannelNameFilterList( matches, OkListDialogView.MODE_CHANNEL_NUM );
                }
                break;
            }
            else if ( IsYoutube( result ) )
            {
                Log.d( TAG, "onResults: is youtube" );
                stopAllRec();//Scoty 20180720 add stop all records/timeshift
                AvControlPlayStop(MiscDefine.AvControl.AV_STOP_ALL);
                AvControlClose(ViewHistory.getPlayId());
                // jim 2018/09/27 open youtube to continue execute last page -s
                //Intent YouTube = new Intent(Intent.ACTION_VIEW, Uri.parse(getString( R.string.STR_YOUTUBE )));
                Intent YouTube = new Intent();
                YouTube.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                YouTube.setPackage("com.google.android.youtube.tv");
                // jim 2018/09/27 open youtube to continue execute last page -e
                startActivity( YouTube );
                break;
            }
            else
            {
                Log.d( TAG, "onResults: not match result = " + result );
            }
        }

        boolean hasMatch = i < matches.size();

        if ( !hasMatch )
        {
            if ( okListDialog != null && okListDialog.isShowing() )
            {
                Log.d( TAG, "onResults: update channel name dialog" );
                int curServiceType = GetServiceType();
                okListDialog.UpdateFilter( matches, curServiceType, OkListDialogView.MODE_CHANNEL_NAME );
            }
            else
            {
                Log.d( TAG, "onResults: open channel name dialog" );
                OpenChannelNameFilterList( matches, OkListDialogView.MODE_CHANNEL_NAME );
            }
        }

        isSpeaking = 0;
        if ( speechTimer != null )
        {
            speechTimer.cancel();
        }
    }

    @Override
    public void onPartialResults(Bundle bundle) {
        Log.i(TAG, "onPartialResults");
    }

    @Override
    public void onEvent(int i, Bundle bundle) {
        Log.i(TAG, "onEvent");
    }
    // jim 2018/07/13 add start speechrecognizer in dtvplayer -e

    private int GetServiceType()
    {
        Log.d( TAG, "GetServiceType: " );

        if ( ViewHistory.getCurGroupType() == ProgramInfo.ALL_TV_TYPE ||
           ( ViewHistory.getCurGroupType() >= ProgramInfo.TV_FAV1_TYPE &&
             ViewHistory.getCurGroupType() < ProgramInfo.ALL_RADIO_TYPE ) )
        {
            return ProgramInfo.ALL_TV_TYPE;
        }
        else if ( ViewHistory.getCurGroupType() == ProgramInfo.ALL_RADIO_TYPE ||
                ( ViewHistory.getCurGroupType() >= ProgramInfo.RADIO_FAV1_TYPE &&
                  ViewHistory.getCurGroupType() < ProgramInfo.ALL_TV_RADIO_TYPE_MAX ) )
        {
            return ProgramInfo.ALL_RADIO_TYPE;
        }
        else
        {
            return ViewHistory.getCurGroupType();//current service type
        }
    }

    private boolean IsChannelDown(String str)
    {
        Log.d(TAG, "IsChannelDown: "+str);

        String[] result = str.split(getString(R.string.STR_HELP_FILTER_SPACE));
        List<String> chDownCaseA = Arrays.asList(getResources().getStringArray(R.array.STR_ARRAY_CH_DOWN_CASE_A));

        if ( result.length == 1 )
        {
            return chDownCaseA.contains(result[0]);
        }

        List<String> chDownCaseB1 = Arrays.asList(getResources().getStringArray(R.array.STR_ARRAY_CH_DOWN_CASE_B1));
        List<String> chDownCaseB2 = Arrays.asList(getResources().getStringArray(R.array.STR_ARRAY_CH_DOWN_CASE_B2));
        return chDownCaseB1.contains((result[0])) && chDownCaseB2.contains(result[result.length - 1]);
    }

    private boolean IsChannelUp(String str)
    {
        Log.d(TAG, "IsChannelUp: str = "+str);

        String[] result = str.split(getString(R.string.STR_HELP_FILTER_SPACE));
        List<String> chUpCaseA = Arrays.asList(getResources().getStringArray(R.array.STR_ARRAY_CH_UP_CASE_A));

        if ( result.length == 1 )
        {
            return chUpCaseA.contains(result[0]);
        }

        List<String> chUpCaseB1 = Arrays.asList(getResources().getStringArray(R.array.STR_ARRAY_CH_UP_CASE_B1));
        List<String> chUpCaseB2 = Arrays.asList(getResources().getStringArray(R.array.STR_ARRAY_CH_UP_CASE_B2));
        return chUpCaseB1.contains(result[0]) && chUpCaseB2.contains(result[result.length - 1]);
    }

    private boolean IsNumber(String result)
    {
        Log.d(TAG, "IsNumber: "+result);

        String[] splitResult = result.split(getString(R.string.STR_HELP_FILTER_SPACE));

        return splitResult.length > 1 && splitResult[0].equalsIgnoreCase(getString( R.string.STR_NUMBER ));
    }

    private boolean IsYoutube ( String result )
    {
        Log.d( TAG, "IsYoutube: "+result );

        List<String> youtubeCase = Arrays.asList( getResources().getStringArray( R.array.STR_ARRAY_YOUTUBE_CASE ) );
        return youtubeCase.contains( result );
    }

    public boolean isFocusPip() {
        if(ViewHistory.getCurPipChannel() != null && pipFrame.getVisibility() == View.VISIBLE)
            return true;
        else
            return false;
    }

    private void SetPipColor()
    {
        if(PvrRecordCheck(ViewHistory.getCurPipChannel().getChannelId()) != -1)
            pipFrame.SetColor(Color.argb(255,255,0,0));
        else
            pipFrame.SetColor(Color.argb(255, 128, 255, 255));
    }

    private int GetPvrSkipFlagByChNum(int chNum, int groupType)
    {
        SimpleChannel simpleChannel = ProgramInfoGetSimpleChannelByChNum(chNum, groupType);

        if (simpleChannel == null)
        {
            return 0;
        }

        return simpleChannel.getPVRSkip();
    }

    private  void clearNoVideoSignal(){//eric lin 20180803 no video signal
        if(mCaMessagView != null) {//Scoty 20180629 show ca message on top
            mCaMessagView.remove();
            mCaMessagView = null;//Scoty 20180725 fixed no signal message can not disappear
        }
    }

    // Johnny 20180803 add to close all view/dialog
    private void CloseAllView()
    {
        // banner info
        if (bannerView.getVisibility() == View.VISIBLE)
        {
            bannerView.setVisibility(View.INVISIBLE);
        }

        // banner detail
        if (detailView.getVisibility() == View.VISIBLE)
        {
            detailView.setVisibility(View.INVISIBLE);
        }

        // pvr banner
        if (pvrBanner.getVisibility() == View.VISIBLE)
        {
            pvrBanner.setVisibility(View.INVISIBLE);
        }

        // oklist dialog
        if (okListDialog != null && okListDialog.isShowing())
        {
            okListDialog.dismiss();
        }

        // subtitle dialog
        if (subtitleDialog != null && subtitleDialog.isShowing())
        {
            subtitleDialog.dismiss();
        }

        // audio dialog
        if (audioDialogView != null && audioDialogView.isShowing())
        {
            audioDialogView.dismiss();
        }

        // teletext dialog
        if (teletextDialogView != null && teletextDialogView.isShowing())
        {
            teletextDialogView.dismiss();
        }

        // rec duration dialog
        if (recDurDialog != null && recDurDialog.isShowing())
        {
            recDurDialog.dismiss();
        }

        // stop multi rec dialog
        if (mStopMultiRecDialog != null && mStopMultiRecDialog.isShowing())
        {
            mStopMultiRecDialog.dismiss();
        }

        // stop ewbs dialog
        stopEwbsDialog();//Scoty 20181218 add VMX EWBS mode
    }

    private void stopEwbsDialog()//Scoty 20181225 modify VMX EWBS rule//Scoty 20181218 add VMX EWBS mode
    {
        if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798 && Pvcfg.getCAType() == Pvcfg.CA_VMX) {
            if (ewsDialog != null) {
                ewsDialog.dismiss();
                ewsDialog = null;
            }
        }
    }

    private void NextKeyEvent(final int tmpKeyCode, final KeyEvent tmpEvent)//Scoty 20180809 modify dual pvr rule
    {
        //eric lin 20180716 delay for timeshift status changed,-start
        Handler handler=new Handler();
        Runnable r=new Runnable() {
            public void run() {
                //what ever you do here will be done after 3 seconds delay.
                onKeyDown(tmpKeyCode, tmpEvent);
                onKeyUp(tmpKeyCode, tmpEvent);
            }
        };
        handler.postDelayed(r, 500);
        //eric lin 20180716 delay for timeshift status changed,-end
    }

    // for VMX need open/close -s
    private void showCAMsg(int VMXMsgMode, String VMXMsg, int VMXMsgDuration) // connie 20180903 for VMX-s
    { // VMXMsgMode :  user=> 0    always  => 1
        Log.d(TAG, "showCAMsg: VMXMsgMode = " + VMXMsgMode + "    VMXMsg = " + VMXMsg + "    VMXMsgDuration=" + VMXMsgDuration);

        caMsgView.ViewCAMsg(View.VISIBLE, VMXMsgMode, VMXMsg, VMXMsgDuration);
    }

    private void showWaterMark(int VMXMsgMode, String VMXMsg, int VMXMsgDuration, int frameX, int frameY, int triggerID, int triggerNum) {
        Log.d(TAG, "showWaterMark: VMXMsgMode =" + VMXMsgMode + "    VMXMsg="+VMXMsg + "    VMXMsgDuration = "+VMXMsgDuration);
        int txtColor = Color.argb(100, 255, 255, 255);
        int bgColor = Color.argb(100, 255, 0, 0);


        if(VMXMsgMode == 2) {
            if(mFingerPringView != null)
                removeWaterMark();
        }
        else
        {
            if ( mFingerPringView != null )
                removeWaterMark();

            mFingerPringView = new FingerPrintView(ViewActivity.this, VMXMsg, VMXMsgDuration, 0, 16, 300, 300, 600, 50, txtColor, bgColor);
            mFingerPringView.SetTriggerInfo(mDTVActivity, triggerID, triggerNum);
            if ( VMXMsgMode == 1 ) // flash
            {
                mFingerPringView.setBlink(1, 500);
                mFingerPringView.setRandom(1, 1000);
            }
            mFingerPringView.setFpTextGravity(Gravity.TOP | Gravity.START);
            mFingerPringView.setFitTextSize();
            mFingerPringView.show();
        }
    }

    private void removeWaterMark() {
        if(mFingerPringView!=null ) { //mWaterMarkMode : 1 ==> alwayse
            mFingerPringView.FingerPrintFinish();
            mFingerPringView.remove();
            mFingerPringView = null;
        }
    }

//Scoty 20181207 modify VMX OTA rule -s
    private void OTAstart(int OTAMode, int triggerID, int triggerNum, int otaFreqNum, ArrayList<Integer> freqList, ArrayList<Integer> bandwidthList) {
        Log.d(TAG, "OTAstart =" + OTAMode + "    triggerID ="+triggerID + "   triggerNum ="+triggerNum + "   otaFreqNum=" + otaFreqNum);
        if(OTAMsg != null && OTAMsg.isShowing())
            return;

        Log.d(TAG, "OTAstart:  OTAMode=" + OTAMode + "   triggerID ="+triggerID + "    triggerNum="+triggerNum);
        int tpId = ViewHistory.getCurChannel().getTpId();
        int satId = TpInfoGet(tpId).getSatId();
        int DSMCC_PID = 7000;
        OTAMsg = new OTADialogView(ViewActivity.this, mDTVActivity, OTAMode, triggerID,triggerNum,0, satId, DSMCC_PID, otaFreqNum, freqList, bandwidthList) {
            public void dialogEnd() {
            }
        };
        // Edwin 20190509 fix dialog not focus -s
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run () {
                OTAMsg.show();
            }
        }, 150);
        // Edwin 20190509 fix dialog not focus -e
    }
//Scoty 20181207 modify VMX OTA rule -e
    private void VMXSmartCardInOut( int status)
    {
        String msg = "";
        if(status == 0)
            msg =  getString(R.string.STR_CARD_IN);
        else if(status == 1)
            msg =  getString(R.string.STR_CARD_OUT);
        else
            msg =  getString(R.string.STR_CARD_HWERR);
        if(CardDetectMsg != null && CardDetectMsg.isShowing())
            CardDetectMsg.dismiss();

        CardDetectMsg = new MessageDialogView(mContext, msg, 3000) {
            public void dialogEnd() {

            }
        };
        CardDetectMsg.show();
    }

    private void VMX_SearchProc(int mode, int startFreq, int endFreq, int triggerID, int triggerNum)
    {
        Log.d(TAG, "VMX_SearchProc: ");
        int startTPID = -1, endTPID = -1;

        startTPID = findTPIDByParam(startFreq);
        endTPID = findTPIDByParam(endFreq);
        if(startTPID == -1 ||  endTPID == -1)
        {
        Log.d(TAG, "VMX_SearchProc:   TP Search !!!  TP  not find !!!!!    startTPID = " + startTPID + "    endTPID = " + endTPID);
            return;
        }

        stopAllRec();

        // ===== Stop PIP ======
        if(ViewHistory.getCurPipChannel() != null) {
            if(pipFrame.getVisibility() == View.VISIBLE)
            {
                viewUiDisplay.ClosePipScreen();
                pipFrame.SetVisibility(View.INVISIBLE);
            }
        }

        // ==== Close Banner & Detail =====
        if (detailView.GetVisibility() == View.VISIBLE)
            detailView.SetVisibility(View.INVISIBLE, null, 0, 0, 0, "");
        CloseBanner();

        if(AvControlGetPlayStatus(ViewHistory.getPlayId()) == HiDtvMediaPlayer.EnPlayStatus.LIVEPLAY.getValue())
            AvControlPlayStop(ViewHistory.getPlayId());

        // Edwin 20181206 vmx search did not delete all channel
        //ProgramManagerInit(ProgramInfo.ALL_TV_TYPE);
        ProgramManagerList = GetProgramManager(ProgramInfo.ALL_TV_TYPE);
        for ( ProgramManagerImpl programManager : ProgramManagerList )
        {
            programManager.DelAllProgram(1);
            programManager.Save();
        }
        AvControlPlayStop(MiscDefine.AvControl.AV_STOP_ALL);
        AvControlClose(ViewHistory.getPlayId());
        SetChannelExist(0);
        ChannelHistory.Reset();
        ResetTotalChannelList();
        if ( mCaMessagView != null )
        {
            mCaMessagView.SetVisibility(INVISIBLE);
        }

        Intent intent = new Intent();
        intent.setClass(ViewActivity.this, ScanResultActivity.class);

        Bundle bundle = new Bundle();
        bundle.putInt(getString(R.string.STR_EXTRAS_TPID), 0);
        bundle.putInt(getString(R.string.STR_EXTRAS_SEARCHMODE), mode); // 0 : manaul  1 : auto 2 : network 3 : all sat 4: vmx search
        bundle.putInt(getString(R.string.STR_EXTRAS_SCANMODE), 0); // 0 : All  1 : FTA  2 : $
        bundle.putInt(getString(R.string.STR_EXTRAS_CHANNELTYPE), 0); // 0 : All  1 : TV  2 : Radio
        bundle.putInt(getString(R.string.STR_EXTRAS_VMX_START_TPID), startTPID);
        bundle.putInt(getString(R.string.STR_EXTRAS_VMX_END_TPID), endTPID);
        bundle.putInt(getString(R.string.STR_EXTRAS_VMX_TRIGGER_ID), triggerID);
        bundle.putInt(getString(R.string.STR_EXTRAS_VMX_TRIGGER_NUM), triggerNum);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private int findTPIDByParam(int freq)
    {
        List<TpInfo> tpList = new ArrayList<>();

        int tpID = -1;
        tpList = TpInfoGetList(GetCurTunerType());
        Log.d(TAG, "findTPIDByParam:  freq = " + freq );
        Log.d(TAG, "findTPIDByParam: tpList.size =" + tpList.size());
        if(GetCurTunerType() == TpInfo.DVBC)
        {
            TpInfo.Cable tp;
            for(int i = 0; i< tpList.size(); i++)
            {
                tp = (tpList.get(i)).CableTp;
                Log.d(TAG, "findTPIDByParam:  tp : "  + i+ "     freq = " + tp.getFreq() + "   symbol =" + tp.getSymbol());
                if (freq == tp.getFreq()) {
                    tpID = tpList.get(i).getTpId();
                    return tpID;
                }
            }
        }
        else if(GetCurTunerType() == TpInfo.DVBT)
        {

        }
        else if(GetCurTunerType() == TpInfo.ISDBT)
        {
            TpInfo.Terr tp;
            for(int i = 0; i< tpList.size(); i++)
            {
                tp = (tpList.get(i)).TerrTp;
                Log.d(TAG, "findTPIDByParam:  tp : "  + i+ "     freq = " + tp.getFreq() + "    band =" + tp.getBand());
                if (freq == tp.getFreq()) {
                    tpID = tpList.get(i).getTpId();
                    return tpID;
                }
            }
        }
        else
        {

        }
        return -1;
    } // connie 20180903 for VMX-e
    // for VMX need open/close -e

    private void removeAutoChangeCh()//Scoty 20181001 fixed auto change channel show no video signal
    {
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

    private void SetAnttena5V() // connie 20181106 for init anttena 5V
    {
        Log.d(TAG, "SetAnttena5V:  CurTunerType =" + GetCurTunerType());
        if(GetCurTunerType() == TpInfo.ISDBT || GetCurTunerType() == TpInfo.DVBT) {

            final GposInfo gpos = GposInfoGet();
            Log.d(TAG, "SetAnttena5V:  gpos.getLnbPower() =" + gpos.getLnbPower());
            if(gpos.getLnbPower() == 0)
            {
                if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798) // connie 20181101 for 3798 5v on/off
                    MtestSetAntenna5V(0, GetCurTunerType(), 0);
                else
                    TunerSetAntenna5V(0, 0);
            }
            else
            {
                if(Pvcfg.getUIModel() == Pvcfg.UIMODEL_3798) // connie 20181101 for 3798 5v on/off
                    MtestSetAntenna5V(0, GetCurTunerType(), 1);
                else
                    TunerSetAntenna5V(0, 1);
            }
        }
    }

    //Scoty 20181204 add VMX scan/signal/mail/sleep key events
    private void GoToAutoSearch()//VMX SIGNAL KEY
    {
        new SureDialog(ViewActivity.this) {
            public void onSetMessage(View v) {
                ((TextView) v).setText(getString(R.string.STR_WOULD_YOU_LIKE_TO_SCAN_NOW));
            }
            public void onSetNegativeButton() {

            }
            public void onSetPositiveButton() {
                AvControlPlayStop(ViewHistory.getPlayId());
                Intent AutoSearch = new Intent();
                AutoSearch.setClass(ViewActivity.this,ScanResultActivity.class);
                Bundle SeachBundle = new Bundle();
                if(GetCurTunerType() == TpInfo.DVBC) {
                    SeachBundle.putInt(getString(R.string.STR_EXTRAS_TPID), 0);
                    SeachBundle.putInt(getString(R.string.STR_EXTRAS_SEARCHMODE), 2); // 0 : manaul  1 : auto 2 : network 3 : all sat
                    SeachBundle.putInt(getString(R.string.STR_EXTRAS_SCANMODE), 0); // 0 : All  1 : FTA  2 : $
                    SeachBundle.putInt(getString(R.string.STR_EXTRAS_CHANNELTYPE), 0); // 0 : All  1 : TV  2 : Radio
                }
                else if(GetCurTunerType() == TpInfo.DVBS){
                    SeachBundle.putInt(getString(R.string.STR_EXTRAS_TPID), 0);
                    SeachBundle.putInt(getString(R.string.STR_EXTRAS_SCANMODE), 0);   // 0 : fta&$, 1 : fta, 2 : $
                    SeachBundle.putInt(getString(R.string.STR_EXTRAS_CHANNELTYPE), 0); // 0 : TV&Radio, 1 : TV, 2 : Radio
                    SeachBundle.putInt(getString(R.string.STR_EXTRAS_SEARCHMODE), 0);
                }
                else if(GetCurTunerType() == TpInfo.DVBT){
                    SeachBundle.putInt(getString(R.string.STR_EXTRAS_TPID), 0);
                    SeachBundle.putInt(getString(R.string.STR_EXTRAS_SEARCHMODE), 1); // 0 : manaul  1 : auto
                    SeachBundle.putInt(getString(R.string.STR_EXTRAS_SCANMODE), 0); // 0 : All  1 : FTA  2 : $
                    SeachBundle.putInt(getString(R.string.STR_EXTRAS_CHANNELTYPE), 0); // 0 : All  1 : TV  2 : Radio
                }
                else if(GetCurTunerType() == TpInfo.ISDBT) {
                    SeachBundle.putInt(getString(R.string.STR_EXTRAS_TPID), 0);
                    SeachBundle.putInt(getString(R.string.STR_EXTRAS_SCANMODE), 0);   // 0 : fta&$, 1 : fta, 2 : $
                    SeachBundle.putInt(getString(R.string.STR_EXTRA_NIT_SEARCH), 0); // 0 : off, 1 : on
                    SeachBundle.putInt(getString(R.string.STR_EXTRA_ONE_SEG_CHANNEL), 0);  // 0 : off, 1 : on
                    SeachBundle.putInt(getString(R.string.STR_EXTRAS_SEARCHMODE), 0);   // 0 : auto  1 : manual
                }
                AutoSearch.putExtras(SeachBundle);
                startActivity(AutoSearch);
            }
        };
    }

    // Johnny 20181219 for mouse control -s
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && detailView.GetVisibility() != View.VISIBLE) {
            //Scoty 20180712 for pip
            if (ViewHistory.getCurPipChannel() != null) {
                viewUiDisplay.ClosePipScreen();
                pipFrame.SetVisibility(View.INVISIBLE);
            }

            if (mCaMessagView != null)//Scoty 20180725 fixed no signal message can not disappear
            {
                mCaMessagView.SetVisibility(View.INVISIBLE);
            }

            if (bannerView.GetVisibility() == View.VISIBLE) {
                CloseBanner();//Scoty 20180621 modify close banner as function// connie 20180524 fix channel num show wrong in fav group
            }

            if (Pvcfg.getPVR_PJ() && getCurrentPvrMode() != PvrInfo.EnPVRMode.NO_ACTION) { // connie 20181024 for can't goto menu when pvr flag not open
                new SureDialog(this) {
                    public void onSetMessage(View v) {
                        ((TextView) v).setText(getString(R.string.STR_DO_YOU_WANT_TO_STOP_ALL_RECORDS_AND_TIMESHIFT));
                    }
                    public void onSetNegativeButton() {
                    }
                    public void onSetPositiveButton() {
                        stopAllRec();
                        openMainMenu();
                    }
                };

                return false;
            }

            openMainMenu();
        }

        return super.onTouchEvent(event);
    }
    // Johnny 20181219 for mouse control -e

    private void LuncherOpenActivity(String openActivity)  // connie 20190830 for open activity from pesi laucner
    {
        if(openActivity != null && !openActivity.equals(""))
        {
            Log.d(TAG, "onResume: openActivity = " + openActivity);

            Intent it = new Intent();
            Bundle bundle = new Bundle();
            if(openActivity.equals(getString(R.string.STR_SYSTEM_SETTINGS)))
            {
                it.setClass(this, SystemSettingsActivity.class);
                startActivity(it, bundle);
            }
            else if (openActivity.equals(getString(R.string.STR_TV_PROGRAMME_GUIDE)))
            {
                int curListPosition = 0;
                if(ViewHistory.getCurChannel()!=null)
                    curListPosition = ViewHistory.getCurListPos(ViewHistory.getCurChannel().getChannelId());
                it.setClass(this, DimensionEPG.class);
                bundle.putInt("type", ViewHistory.getCurGroupType());
                bundle.putInt("cur_channel", curListPosition );
                it.putExtras(bundle);
                startActivity(it, bundle);
            }
            else if(openActivity.equals(getString(R.string.STR_TIMER)))
            {
                it.setClass(this, TimerListActivity.class);
                startActivity(it, bundle);
            }

            Log.d(TAG, "LuncherOpenActivity: out !!!!");
        }
    }

    private void openMainMenu() {
        Intent it = new Intent();
        if (Pvcfg.getUIType() == 1) {//Scoty 20180912 add UI type cfg
            it.setClass(this, MainMenuActivity.class);
        }
        else if (Pvcfg.getUIType() == 2) {
            it.setClass(this, MainMenu2Activity.class);
        }
        else {
            it.setClass(this, MainMenu3Activity.class);
        }

        startActivityForResult(it, 2);
    }

    // Johnny 20210423 temp for RTK avplay issue -s
    private final Runnable waitPlaySuccessTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "johnny run: wait play success timeout");
            stopWaitPlaySuccess();
        }
    };

    private void startWaitPlaySuccess() {
        Log.d(TAG, "johnny startWaitPlaySuccess: ");

        BootAVPlayHandler.removeCallbacks(waitPlaySuccessTimeoutRunnable);

        waitPlaySuccess = true;
        BootAVPlayHandler.postDelayed(waitPlaySuccessTimeoutRunnable, TIMEOUT_WAIT_PLAY_SUCCESS);
    }

    private void stopWaitPlaySuccess() {
        Log.d(TAG, "johnny stopWaitPlaySuccess: ");

        BootAVPlayHandler.removeCallbacks(waitPlaySuccessTimeoutRunnable);

        waitPlaySuccess = false;
    }
    // Johnny 20210423 temp for RTK avplay issue --e


    /////////////////////////////

    //Scoty Add Recommend Channel and Programs
    private void setPersistData(Context context) {
        TvSharedPreference = PreferenceManager.getDefaultSharedPreferences(context);
        tvEditor = TvSharedPreference.edit();
    }

    //Get no duplicate favorite channel list
    private List<SimpleChannel> GetUniqueFavList()
    {
        List<SimpleChannel> FavChannelsList = new ArrayList<>();
        boolean isFirstAddFavChannels = false;
        for(int i = ProgramInfo.TV_FAV1_TYPE; i < ProgramInfo.ALL_TV_RADIO_TYPE_MAX; i++)
        {
            if(i != ProgramInfo.ALL_RADIO_TYPE) {
                int FaveListSize = GetOkList().get(i).ProgramInfoList.size();
                if(FaveListSize > 0 ) {
                    if (!isFirstAddFavChannels) {// first Fav Group no need all add
                        for (int j = 0; j < FaveListSize; j++) {
                            FavChannelsList.add(GetOkList().get(i).ProgramInfoList.get(j));
                        }
                        isFirstAddFavChannels = true;
                    }else{
                        //Use First Add Favorite List to compare with other groups
                        for(int k = 0 ; k < FavChannelsList.size() ; k++)
                        {
                            for(int l = 0 ; l < FaveListSize ; l++)
                            {
                                //same group without the same two channel id
                                if(FavChannelsList.get(k).getChannelId() != GetOkList().get(i).ProgramInfoList.get(l).getChannelId())
                                {
                                    FavChannelsList.add(GetOkList().get(i).ProgramInfoList.get(l));
                                }
                            }
                        }

                    }
                }
            }
        }

        return FavChannelsList;
    }

    private Intent GetProgramIntent(long service_id)
    {
        Intent intent = new Intent();
        intent.setClass(mContext, ViewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(mContext.getString(R.string.STR_CHANGE_PROGRAM_CHANNEL_ID),service_id);
        Log.d(TAG, "GetProgramIntent: intent =  [" + intent+"]");
        return intent;
    }

    private void AddNewFavoriteChannel()
    {
        String PACKAGE_NAME = "com.prime.dtvplayer";
        String AUTHOR = "Prime";
        Uri channelLogoUri = Uri.parse("android.resource://" + PACKAGE_NAME + "/" + R.drawable.cablevision_banner);
        Uri programLogoUri = Uri.parse("android.resource://" + PACKAGE_NAME + "/" + R.drawable.cablevision_banner);
        RecommendChannel Recommend = new RecommendChannel(ViewActivity.this);

//Get Recommend to Set Channel
        Intent intentChannel = new Intent();
        intentChannel.setClass(mContext, ViewActivity.class);
        intentChannel.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        RecommendChannel.ChannelBuilder channelBuilder = Recommend.getChannelBuilder();
        channelBuilder.setType(TvContractCompat.Channels.TYPE_PREVIEW)
                .setDisplayName(getString(R.string.STR_FAVORITE))
                //.setDisplayNumber("1")
                .setDescription(getString(R.string.STR_FAVORITE))
                .setPackageName(PACKAGE_NAME)
                .setOriginalNetworkId(0)
                .setChannelLogo(String.valueOf(channelLogoUri))
                .setInternalProviderData(getString(R.string.STR_FAVORITE))
                .setAppIntent(intentChannel)
                .setSearchable(true)
                .build();

//Get Recommend to Set Program Info
        List<SimpleChannel> UniquefavChannelList = GetUniqueFavList();
        if(UniquefavChannelList.size() > 0)
        {
            for(int i = 0 ; i < UniquefavChannelList.size() ; i++) {
                SimpleChannel FavChannel = UniquefavChannelList.get(i);
                Date nowtime = getLocalTime();
                List<EPGEvent> epgEventList = EpgEventGetEPGEventList(FavChannel.getChannelId(),nowtime.getTime(),nowtime.getTime()+24*60*60*1000, 0);

                String description;
                long starttime,endtime;
                if(epgEventList.size() != 0) {
                    description = epgEventList.get(0).getEventName() + epgEventList.get(0).getShortEvent();
                    starttime = epgEventList.get(0).getStartTime();
                    endtime = epgEventList.get(0).getEndTime();
                }else{
                    description = mContext.getString(R.string.STR_NO_INFO_AVAILABLE);
                    starttime = 120000;
                    endtime = 10001000;
                }

                //Program Builder
                RecommendChannel.ProgramBuilder programBuilder = Recommend.getProgramBuilder();
                Intent programIntent = GetProgramIntent(FavChannel.getChannelId());
                programBuilder.setType(TvContractCompat.PreviewPrograms.TYPE_TV_SERIES)
                        .setTitle(FavChannel.getChannelName())
                        .setDescription(description)
                        .setStartTime(starttime)
                        .setEndTime(endtime)
                        .setAuthor(AUTHOR)
                        .setIntent(programIntent)
                        .setGenre(getString(R.string.STR_FAVORITE))
                        .setPosterArtUri(programLogoUri)
                        .setPesiChannelId(FavChannel.getChannelId())
                        .setPackageName(PACKAGE_NAME)
                        .setDurationMillis(1000000)
                        .setInternalProviderData(getString(R.string.STR_FAVORITE))
                        .setLive(true)
                        .setLogoUri(programLogoUri)
                        .setLogoUriDescription("showing Cablevision")
                        .setSearchable(true)
                        .build();

            }
        }

        //Add Favorite Channel and programs
        if(Recommend.getProgramInfoList().size() > 0) {
            int ProgramSize = 0;

            //first set use Add()
            if(TvSharedPreference.getString(getString(R.string.STR_FAVORITE), "null").equals("null")) {
                ProgramSize = Recommend.Add();
            }else{//Channel is already exist, use RebuildPrograms
                ProgramSize = Recommend.RebuildPrograms(Uri.parse(TvSharedPreference.getString(getString(R.string.STR_FAVORITE), "null")));
            }

            //Get Channel and Program Uri by Recommend.getUriInfo()
            if(ProgramSize > 0)
            {
                tvEditor.putString(getString(R.string.STR_FAVORITE), Recommend.getUriInfo().getChannelUri().toString());
                tvEditor.apply();
                Log.d(TAG, "AddNewFavoriteChannel: channelUri = " + Recommend.getUriInfo().getChannelUri() +"] ProgramSize = " + ProgramSize);
                for (int i = 0; i < Recommend.getUriInfo().getProgramUriList().size(); i++) {
                    Log.d(TAG, "AddNewFavoriteChannel: programUri[" + i + "] = [" + Recommend.getUriInfo().getProgramUriList().get(i) + "]");
                }
            }
        }
    }

    private void UpdateNewFavoriteChannel() {

        RecommendChannel Recommend = new RecommendChannel(ViewActivity.this);

        UpdateFavChannel(Recommend);
        UpdateFavPrograms(Recommend);

    }

    private void UpdateFavPrograms(RecommendChannel recommend)
    {
        String PACKAGE_NAME = "com.prime.dtvplayer";
        String AUTHOR = "Prime";
        long PesichannelId = 714997787;//714604571,714735643
        Uri channelLogoUri = Uri.parse("android.resource://" + PACKAGE_NAME + "/" + R.drawable.blue_btn);
        Uri programLogoUri = Uri.parse("android.resource://" + PACKAGE_NAME + "/" + R.drawable.antenna);
        Date date = new Date();

        RecommendChannel.ProgramBuilder programBuilder = recommend.getProgramBuilder();
        long starttime,endtime;
        starttime = 120000;
        endtime = 10001000;

        TvContentRating tvContentRating = TvContentRating.createRating(
                "com.android.tv",
                "US_TV",
                "US_TV_PG",
                "US_TV_D", "US_TV_L");

        TvContentRating[] ratings = {tvContentRating};
        String[] canonical_genres = { TvContractCompat.Programs.Genres.MOVIES,TvContractCompat.Programs.Genres.COMEDY };

        //String[] contentString = {"com.android.tv","US_TV","US_TV_PG","US_TV_D", "US_TV_L"};
        Intent programIntent = GetProgramIntent(PesichannelId);
        programBuilder.setType(TvContractCompat.PreviewPrograms.TYPE_TV_SERIES)
                .setTitle("Update Favorite 714997787")
                .setDescription("Update Favorite 714997787"+getLocalTime())
                .setStartTime(date.getTime())
                .setEndTime(date.getTime()+2000000000)
                .setAuthor("Update Favorite 714997787")
                .setIntent(programIntent)
                .setGenre(getString(R.string.STR_FAVORITE))
                .setPosterArtUri(programLogoUri)
                .setPesiChannelId(PesichannelId)
                .setPackageName(PACKAGE_NAME)
                .setDurationMillis(1000000)
                .setInternalProviderData(getString(R.string.STR_FAVORITE))
                .setReleaseDate("2021-05-23")
                .setUpdateType(0)
                .setUpdateFilter("714997787")
                .setContentRating(ratings)
                .setCanonicalGenres(canonical_genres)
                .setReviewRating("Review Rating" + PesichannelId)
                .build();

        RecommendChannel.ProgramBuilder programBuilder1 = recommend.getProgramBuilder();
        long PesichannelId1 = 714604571;
        Intent programIntent1 = GetProgramIntent(PesichannelId1);
        programBuilder1.setType(TvContractCompat.PreviewPrograms.TYPE_TV_SERIES)
                .setTitle("Update Favorite 714604571")
                .setDescription("Update Favorite 714604571"+getLocalTime())
                .setStartTime(date.getTime())
                .setEndTime(date.getTime()+1000000000)
                .setAuthor("Update Favorite 714604571")
                .setIntent(programIntent1)
                .setGenre(getString(R.string.STR_FAVORITE))
                //.setPosterArtUri(programLogoUri)
                .setReleaseDate("2021-05-24")
                .setPesiChannelId(PesichannelId)
                .setPackageName(PACKAGE_NAME)
                .setDurationMillis(1000000)
                .setInternalProviderData(getString(R.string.STR_FAVORITE))
                .setUpdateType(0)
                .setUpdateFilter(""+PesichannelId1)
                .setContentRating(ratings)
                .setCanonicalGenres(canonical_genres)
                .setReviewRating("Review Rating" + PesichannelId1)
                .build();

        RecommendChannel.ProgramBuilder programBuilder2 = recommend.getProgramBuilder();
        long PesichannelId2 = 714735643;
        Intent programIntent2 = GetProgramIntent(PesichannelId2);
        programBuilder2.setType(TvContractCompat.PreviewPrograms.TYPE_TV_SERIES)
                .setTitle("Update Favorite 714735643")
                .setDescription("Update Favorite 714735643"+getLocalTime())
                .setStartTime(starttime)
                .setEndTime(endtime)
                .setAuthor("Update Favorite 714735643")
                .setIntent(programIntent2)
                .setGenre(getString(R.string.STR_FAVORITE))
                //.setPosterArtUri(programLogoUri)
                .setPesiChannelId(PesichannelId)
                .setPackageName(PACKAGE_NAME)
                .setDurationMillis(1000000)
                .setInternalProviderData(getString(R.string.STR_FAVORITE))
                .setReleaseDate("2021-05-26")
                .setUpdateType(0)
                .setUpdateFilter(PesichannelId2 +"")
                .setContentRating(ratings)
                .setCanonicalGenres(canonical_genres)
                .setReviewRating("Review Rating" + PesichannelId2)
                .build();


        RecommendChannel.ProgramBuilder programBuilder3 = recommend.getProgramBuilder();
        long PesichannelId3 = 222222;//Test Add if not exist
        Intent programIntent3 = GetProgramIntent(PesichannelId2);
        programBuilder2.setType(TvContractCompat.PreviewPrograms.TYPE_TV_SERIES)
                .setTitle("Update Favorite 2222222")
                .setDescription("Update Favorite 222222"+getLocalTime())
                .setStartTime(500000000)
                .setEndTime(800000000)
                .setAuthor("Update Favorite 222222")
                .setIntent(programIntent3)
                .setGenre(getString(R.string.STR_FAVORITE))
                //.setPosterArtUri(programLogoUri)
                .setReleaseDate("2021-05-25")
                .setPesiChannelId(PesichannelId)
                .setPackageName(PACKAGE_NAME)
                .setDurationMillis(1000000)
                .setInternalProviderData(getString(R.string.STR_FAVORITE))
                .setUpdateType(0)
                .setUpdateFilter(PesichannelId3 +"")
                .setContentRating(ratings)
                .setCanonicalGenres(canonical_genres)
                .setReviewRating("Review Rating" + PesichannelId3)
                .build();


        //int ret = Recommend.Update(Uri.parse(TvSharedPreference.getString(getString(R.string.STR_FAVORITE), "null")));
        int update_Program_size = recommend.UpdatePrograms(Uri.parse(TvSharedPreference.getString(getString(R.string.STR_FAVORITE), "null")));

        Log.d(TAG, "UpdateNewFavoriteChannel: update_Program_size = [" +update_Program_size +"]");
        if(update_Program_size > 0)
            Log.d(TAG, "UpdateNewFavoriteChannel: Update Programs Success");

    }

    private void UpdateFavChannel(RecommendChannel recommend)
    {
        String PACKAGE_NAME = "com.prime.dtvplayer";

        Intent intentChannel = new Intent();
        intentChannel.setClass(mContext, ViewActivity.class);
        intentChannel.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        RecommendChannel.ChannelBuilder channelBuilder = recommend.getChannelBuilder();
        channelBuilder.setType(TvContractCompat.Channels.TYPE_PREVIEW)
                .setDisplayName(getString(R.string.STR_FAVORITE)+"1")
                .setDescription(getString(R.string.STR_FAVORITE)+"1")
                .setPackageName(PACKAGE_NAME)
                .setOriginalNetworkId(0)
                .setInternalProviderData(getString(R.string.STR_FAVORITE)+"1")
                .setSearchable(true)
                .setAppIntent(intentChannel)
                .setInputId("1")
                .build();

        int channel_ret = recommend.UpdateChannel(Uri.parse(TvSharedPreference.getString(getString(R.string.STR_FAVORITE), "null")));
        if(channel_ret > 0)
            Log.d(TAG, "UpdateNewFavoriteChannel: Update Channel Success");
    }

    private void AddNewRecentlyWatchedChannel()
    {
        RecommendChannel Recommend = new RecommendChannel(ViewActivity.this);
        String PACKAGE_NAME = "com.prime.dtvplayer";
        String AUTHOR = "Prime";
        Uri channelLogoUri = Uri.parse("android.resource://" + PACKAGE_NAME + "/" + R.drawable.cablevision_banner);

        Intent intentChannel = new Intent();
        intentChannel.setClass(mContext, ViewActivity.class);
        intentChannel.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        RecommendChannel.ChannelBuilder channelBuilder = Recommend.getChannelBuilder();
        channelBuilder.setType(TvContractCompat.Channels.TYPE_PREVIEW)
                .setDisplayName(getString(R.string.STR_RECENTLY_WATCHED_CHANNEL))
                //.setDisplayNumber("1")
                .setDescription(getString(R.string.STR_RECENTLY_WATCHED_CHANNEL))
                .setPackageName(PACKAGE_NAME)
                .setOriginalNetworkId(0)
                .setChannelLogo(String.valueOf(channelLogoUri))
                .setInternalProviderData(getString(R.string.STR_RECENTLY_WATCHED_CHANNEL))
                .setAppIntent(intentChannel)
                .setSearchable(true)
                .build();

        //Add Recently Watched Programs
        if(ViewHistory.getCurChannel() != null) {
            RecommendChannel.TvProviderProgramInfo curProgramInfo;
            SetNewRecentlyWatchedProgram(Recommend,ViewHistory.getCurChannel().getChannelId());
        }
        if(ViewHistory.getPreChannel() != null) {
            long preChannelId = ViewHistory.getPreChannel().getChannelId();
            RecommendChannel.TvProviderProgramInfo preProgramInfo;
            SetNewRecentlyWatchedProgram(Recommend,preChannelId);
            //Before Add Fast Program, need to check Fast Next and Previous Programs not the same
            if(ViewHistory.getFastNextChId() != preChannelId)
            {
                SetNewRecentlyWatchedProgram(Recommend,ViewHistory.getFastNextChId());
            }
            if(ViewHistory.getFastPreChId() != preChannelId && ViewHistory.getFastPreChId() != ViewHistory.getFastNextChId())
            {
                SetNewRecentlyWatchedProgram(Recommend,ViewHistory.getFastPreChId());
            }
        }else
        {
            //Before Add Fast Program, need to check Fast Next and Previous Programs not the same
            long curChannelId = -1;
            if(ViewHistory.getCurChannel() != null) {
                curChannelId = ViewHistory.getCurChannel().getChannelId();

                if (ViewHistory.getFastNextChId() != curChannelId) {
                    SetNewRecentlyWatchedProgram(Recommend,ViewHistory.getFastNextChId());
                }
                if (ViewHistory.getFastPreChId() != curChannelId && ViewHistory.getFastPreChId() != ViewHistory.getFastNextChId()) {
                    SetNewRecentlyWatchedProgram(Recommend,ViewHistory.getFastPreChId());
                }
            }
        }

        int ProgramSize = 0;
        if(TvSharedPreference.getString(getString(R.string.STR_RECENTLY_WATCHED_CHANNEL), "null").equals("null")) {
            ProgramSize = Recommend.Add();
        }else{
            ProgramSize = Recommend.RebuildPrograms(Uri.parse(TvSharedPreference.getString(getString(R.string.STR_RECENTLY_WATCHED_CHANNEL), "null")));
        }

        //Update no need to get Uri and Save again
        if(ProgramSize > 0) {
            tvEditor.putString(getString(R.string.STR_RECENTLY_WATCHED_CHANNEL), Recommend.getUriInfo().getChannelUri().toString());
            tvEditor.apply();

            Log.d(TAG, "AddNewRecentlyWatchedChannel: channelUri = " + Recommend.getUriInfo().getChannelUri());
            for (int i = 0; i < Recommend.getUriInfo().getProgramUriList().size(); i++) {
                Log.d(TAG, "AddNewRecentlyWatchedChannel: programUri[" + i + "] = [" + Recommend.getUriInfo().getProgramUriList().get(i) + "]");
            }
        }
    }

    private void AddNewWatchNextProgram()
    {
        RecommendChannel Recommend = new RecommendChannel(ViewActivity.this);
        SetWatchNextProgram(Recommend,ViewHistory.getCurChannel().getChannelId());
        int size = Recommend.AddWatchNextProgram();

        Log.d(TAG, "AddWatchNextProgram: size = " + size);
    }

    private void GetFavoriteChannelInfofromDB()
    {
        RecommendChannel Recommend = new RecommendChannel(ViewActivity.this);
        Uri channelUri = Uri.parse(TvSharedPreference.getString(getString(R.string.STR_FAVORITE),"null"));
        RecommendChannel.TvProviderChannelInfo channelInfo = Recommend.GetChannelInfobyChannelUri(channelUri);

        if(channelInfo != null)
            Log.d(TAG, "GetFavoriteChannelInfofromDB: channelCursor \nId = [" + channelInfo.getChannelId()
                    + "]\n displayName = ["
                    + channelInfo.getDisplayName()
                    + "]\n package Name = ["
                    + channelInfo.getPackageName()
                    + "]\n Intent = ["
                    + channelInfo.getAppintent()
                    + "]\n display number = ["
                    + channelInfo.getDisplayNumber()
                    + "]\n input id = ["
                    + channelInfo.getInputId()
                    + "]\n Description = ["
                    + channelInfo.getDescription()
                    + "]\n INTERNAL_PROVIDER_DATA = ["
                    +  channelInfo.getInternalProviderData()
                    + "]\n Type = ["
                    +  channelInfo.getType()
                    + "]\n Searchable = ["
                    +  channelInfo.getSearchable()
                    + "]\n");
    }

    private void GetFavoriteProgramInfofromDB()
    {
        RecommendChannel Recommend = new RecommendChannel(ViewActivity.this);
        Uri channelUri = Uri.parse(TvSharedPreference.getString(getString(R.string.STR_FAVORITE),"null"));
        List<RecommendChannel.TvProviderProgramInfo> programList = Recommend.GetProgramInfobyChannelUri(channelUri);

        if(programList != null && programList.size() > 0)
        {
            for(int i = 0 ; i < programList.size() ; i++) {
                RecommendChannel.TvProviderProgramInfo tmpProgram = programList.get(i);
                Log.d(TAG, "GetFavoriteProgramInfofromDB: +"
                        +"\n getType ["
                        + programList.get(i).getType()
                        +"]\n getTitle ["
                        + programList.get(i).getTitle()
                        +"]\n getDescription ["
                        + programList.get(i).getDescription()
                        +"]\n getStartTime ["
                        + programList.get(i).getStartTime()
                        +"]\n getEndTime ["
                        + programList.get(i).getEndTime()
                        +"]\n getAuthor ["
                        + programList.get(i).getAuthor()
                        +"]\n getIntent ["
                        + programList.get(i).getIntent()
                        +"]\n getGenre ["
                        + programList.get(i).getGenre()
                        +"]\n getPosterArtUri ["
                        + programList.get(i).getPosterArtUri()
                        +"]\n getDurationMillis ["
                        + programList.get(i).getDurationMillis()
                        +"]\n getInternalProviderData ["
                        + programList.get(i).getInternalProviderData()
                        +"]\n getLive ["
                        + programList.get(i).getLive()
                        +"]\n getLogoUri ["
                        + programList.get(i).getLogoUri()
                        +"]\n getLogoUriDescription ["
                        + programList.get(i).getLogoUriDescription()
                        +"]\n getAvailabilityType ["
                        + programList.get(i).getAvailabilityType()
                        +"]\n getCanonicalGenres ["
                        + programList.get(i).getCanonicalGenres()
                        +"]\n getEpisodeNumber ["
                        + programList.get(i).getEpisodeNumber()
                        +"]\n getEpisodeTitle ["
                        + programList.get(i).getEpisodeTitle()
                        +"]\n getInteractionCount ["
                        + programList.get(i).getInteractionCount()
                        +"]\n getInteractionTpye ["
                        + programList.get(i).getInteractionTpye()
                        +"]\n getItemCount ["
                        + programList.get(i).getItemCount()
                        +"]\n getInternalProviderId ["
                        + programList.get(i).getInternalProviderId()
                        +"]\n getOfferPrice ["
                        + programList.get(i).getOfferPrice()
                        +"]\n getPosterArtAspectRatio ["
                        + programList.get(i).getPosterArtAspectRatio()
                        +"]\n getPreviewVideoUri ["
                        + programList.get(i).getPreviewVideoUri()
                        +"]\n getPreviewAudioUri ["
                        + programList.get(i).getPreviewAudioUri()
                        +"]\n getReleaseDate ["
                        + programList.get(i).getReleaseDate()
//                        +"]\n getReviewRating ["
//                        + programList.get(i).getReviewRating()
                        +"]\n getReviewRatingStyle ["
                        + programList.get(i).getReviewRatingStyle()
                        +"]\n getSeasonNumber ["
                        + programList.get(i).getSeasonNumber()
                        +"]\n getStartingPrice ["
                        + programList.get(i).getStartingPrice()
                        +"]\n getThumbnailratio ["
                        + programList.get(i).getThumbnailratio()
                        +"]\n getThumbnailUri ["
                        + programList.get(i).getThumbnailUri()
                        +"]\n getVideoHeight ["
                        + programList.get(i).getVideoHeight()
                        +"]\n getVideoWidth ["
                        + programList.get(i).getVideoWidth()
                        +"]\n getWeight ["
                        + programList.get(i).getWeight()
//                        +"]\n getContentRating ["
//                        + programList.get(i).getContentRating()
                        +"]\n getInternalProviderData ["
                        + programList.get(i).getInternalProviderData()
                        +"]");

                if(programList.get(i).getCanonicalGenres() != null)
                    for(int ii = 0 ; ii < programList.get(i).getCanonicalGenres().length ; ii++)
                        Log.d(TAG, "GetFavoriteProgramInfofromDB: getCanonicalGenres => [" + programList.get(i).getCanonicalGenres()[ii]+"]");

                ///////////////////////////tvContentRatings example = [com.android.tv/US_TV/US_TV_PG/US_TV_D/US_TV_L]
                if(programList.get(i).getContentRating() != null)
                    for(int kk = 0 ; kk < programList.get(i).getContentRating().length ; kk++)
                        Log.d(TAG, "GetFavoriteProgramInfofromDB: getContentRating => "
                                + "\n Domain = [" + programList.get(i).getContentRating()[0].getDomain()
                                + "]\n MainRating = [" + programList.get(i).getContentRating()[0].getMainRating()
                                + "]\n RatingSystem = [" + programList.get(i).getContentRating()[0].getRatingSystem()
                                + "]\n getSubRatings[0] = [" + programList.get(i).getContentRating()[0].getSubRatings().get(0)
                                + "]\n getSubRatings[1] = [" + programList.get(i).getContentRating()[0].getSubRatings().get(1)
                                +"]");


            }
        }

    }

    private void SetNewRecentlyWatchedProgram(RecommendChannel Recommend,long PesichannelId)
    {
        ProgramInfo service = ProgramInfoGetByChannelId(PesichannelId);
        Date nowtime = getLocalTime();
        List<EPGEvent> epgEventList = EpgEventGetEPGEventList(PesichannelId,nowtime.getTime(),nowtime.getTime()+24*60*60*1000, 0);
        String PACKAGE_NAME = "com.prime.dtvplayer";
        String AUTHOR = "Prime";
        Uri programLogoUri = Uri.parse("android.resource://" + PACKAGE_NAME + "/" + R.drawable.cablevision_banner);

        RecommendChannel.ProgramBuilder programBuilder = Recommend.getProgramBuilder();
        String description;
        long starttime,endtime;
        if(epgEventList.size() != 0) {
            description = epgEventList.get(0).getEventName() + epgEventList.get(0).getShortEvent();
            starttime = epgEventList.get(0).getStartTime();
            endtime = epgEventList.get(0).getEndTime();
        }else{
            description = mContext.getString(R.string.STR_NO_INFO_AVAILABLE);
            starttime = 1000000;
            endtime = 1200000;
        }

        String title = "";
        Intent programIntent = GetProgramIntent(PesichannelId);
        if(ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.DVB_TYPE)
            title = service.getDisplayName();
        else
            title = ViewHistory.getCurChannel().getChannelName();

        programBuilder.setType(TvContractCompat.PreviewPrograms.TYPE_TV_SERIES)
                .setTitle(title)
                .setDescription(description)
                .setStartTime(starttime)
                .setEndTime(endtime)
                .setAuthor(AUTHOR)
                .setIntent(programIntent)
                .setGenre(getString(R.string.STR_RECENTLY_WATCHED_CHANNEL))
                .setPosterArtUri(programLogoUri)
                .setPesiChannelId(PesichannelId)
                .setPackageName(PACKAGE_NAME)
                .setDurationMillis(200000)
                .setInternalProviderData(getString(R.string.STR_RECENTLY_WATCHED_CHANNEL))
                .setLive(true)
                .setPosterArtUriDescription("showing cablevision")
                .build();

    }

    private void SetWatchNextProgram(RecommendChannel Recommend, long PesichannelId)
    {

        ProgramInfo service = ProgramInfoGetByChannelId(PesichannelId);
        Date nowtime = getLocalTime();
        List<EPGEvent> epgEventList = EpgEventGetEPGEventList(PesichannelId,nowtime.getTime(),nowtime.getTime()+24*60*60*1000, 0);
        String PACKAGE_NAME = "com.prime.dtvplayer";
        String AUTHOR = "Prime";
        Uri programLogoUri = Uri.parse("android.resource://" + PACKAGE_NAME + "/" + R.drawable.cablevision_banner);

        RecommendChannel.ProgramBuilder programBuilder = Recommend.getProgramBuilder();
        String description;
        long starttime,endtime;
        if(epgEventList.size() != 0) {
            description = epgEventList.get(0).getEventName() + epgEventList.get(0).getShortEvent();
            starttime = epgEventList.get(0).getStartTime();
            endtime = epgEventList.get(0).getEndTime();
        }else{
            description = mContext.getString(R.string.STR_NO_INFO_AVAILABLE);
            starttime = 1000000;
            endtime = 1200000;
        }

        String title = "";
        Intent programIntent = GetProgramIntent(PesichannelId);
        if(ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.DVB_TYPE)
            title = service.getDisplayName();
        else
            title = ViewHistory.getCurChannel().getChannelName();
        programBuilder.setType(TvContractCompat.PreviewPrograms.TYPE_TV_SERIES)
                .setTitle(title)
                .setDescription(description)
                .setStartTime(starttime)
                .setEndTime(endtime)
                .setAuthor(AUTHOR)
                .setIntent(programIntent)
                .setGenre(getString(R.string.STR_WATCH_NEXT_CHANNEL))
                .setPosterArtUri(programLogoUri)
                .setPesiChannelId(PesichannelId)
                .setPackageName(PACKAGE_NAME)
                .setDurationMillis(1200000)
                .setLastEngagementTimeUtcMillis(1000000)
                .setLastPlaybackPositionMillis(800000)
                .setInternalProviderData(getString(R.string.STR_WATCH_NEXT_CHANNEL))
                .setLive(true)
                .setPosterArtUriDescription("showing Cablevision")
                .build();

    }

    private void DeleteNewWatchNextProgram(){
        RecommendChannel Recommend = new RecommendChannel(ViewActivity.this);
        int size = Recommend.DeleteWatchNextProgram();
        Log.d(TAG, "DeleteNewWatchNextProgram: size = ["+size +"]");
    }

    private void DeleteNewChannelbyUri(Uri channelUri, String ChannelName)
    {
        Log.d(TAG, "DeleteNewChannelbyUri: ");
        RecommendChannel Recommend = new RecommendChannel(ViewActivity.this);
        int ret = Recommend.DeleteChannelRow(channelUri);
        //Reset saved uri Value if no error
        if(ret > 0) {
            tvEditor.putString(ChannelName, "null");
            tvEditor.apply();
        }
    }


    private void DeleteNewPrograms(Uri channelUri, int filterType, String filter)
    {
        //filterType = 0: delete all, 1:delete by ChannelId, 2: delete by Program Uri
        RecommendChannel Recommend = new RecommendChannel(ViewActivity.this);

        //Delete
        List<Uri> deleteUriList = Recommend.DeletePrograms(channelUri, filterType, filter);

        if(deleteUriList.size() > 0) {
            for(int i = 0 ; i < deleteUriList.size() ; i++)
                Log.d(TAG, "DeleteNewPrograms: delete program Uri = [" + deleteUriList.get(i) + "]");
        }
    }
}
