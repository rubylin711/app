package com.TvInput;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.PlaybackParams;
import android.media.tv.TvInputManager;
import android.media.tv.TvTrackInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcel;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

import com.dolphin.dtv.DTVMessage;
import com.dolphin.dtv.EnPVRPlayStatus;
import com.dolphin.dtv.EnTableType;
import com.dolphin.dtv.EnTrickMode;
import com.dolphin.dtv.HiDtvMediaPlayer;
import com.dolphin.dtv.IDTVListener;
import com.google.android.media.tv.companionlibrary.TvPlayer;
import com.mtest.utils.PesiStorageHelper;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.AudioInfo;
import com.prime.dtvplayer.Sysdata.PvrInfo;
import com.prime.dtvplayer.Sysdata.SubtitleInfo;
import com.prime.dtvplayer.View.MessageDialogView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.dolphin.dtv.EnTrickMode.FAST_BACKWARD_EIGHT;
import static com.dolphin.dtv.EnTrickMode.FAST_BACKWARD_FOUR;
import static com.dolphin.dtv.EnTrickMode.FAST_BACKWARD_SIXTEEN;
import static com.dolphin.dtv.EnTrickMode.FAST_BACKWARD_TWO;
import static com.dolphin.dtv.EnTrickMode.FAST_FORWARD_EIGHT;
import static com.dolphin.dtv.EnTrickMode.FAST_FORWARD_FOUR;
import static com.dolphin.dtv.EnTrickMode.FAST_FORWARD_NORMAL;
import static com.dolphin.dtv.EnTrickMode.FAST_FORWARD_SIXTEEN;
import static com.dolphin.dtv.EnTrickMode.FAST_FORWARD_TWO;

public class PesiPlayer implements TvPlayer{
    /**
     * A listener for core events.
     */
    public interface Listener {
        void onStateChanged(boolean playWhenReady, int playbackState);

        void onError(Exception e);

        void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
                                float pixelWidthHeightRatio);
    }

    private String TAG = PesiPlayer.class.getSimpleName();
    private Context mContext;
    private Toast mStartTimeShift = null;
    public HiDtvMediaPlayer mDtv = null ;
    private static boolean mIsPrepareDTV=false;
    //private Listener playbackParamsListener = null;
    private CopyOnWriteArrayList<Listener> listenersPrime=null;//eric lin 20181026 modify pesi player
    private boolean mplayWhenReady;
    private long mChannelId = 0;
    private List<TvPlayer.Callback> mTvPlayerCallbacks;
    public static final int MODE_PLAY_AV = 1;
    public static final int MODE_TIMESHIFT = 2;
    public static final int MDOE_RECORD = 3;
    public static final int MODE_PLAYBACK = 4;
    private static final long TIME_SHIFT_NO_ACTION = -1;
    private boolean mFullBuffer = false;
    private boolean mSeekToBegin = false;
    private long mStartPosition = TIME_SHIFT_NO_ACTION;
    private long mPausePosition = TIME_SHIFT_NO_ACTION;
    private int mRunCount = 0;
    private PesiTvInputService.PesiTvInputSessionImpl mSession;
    private PesiTvInputService.RecordingSession mRecordingSession;//eric lin 20181116 live tv record disk full
    private static int mPlayMode; //1:Play AV, 2:Timeshift, 3:Record, 4:Playback //eric lin 20181026 modify pesi player
    private int mPlaySofFlag=0;//eric lin 20181031 fix rewind/forward issue
    private int mPlayEofFlag=0;//eric lin 20181031 fix rewind/forward issue
    private long mRecordingDuration;//eric lin 20181031 fix rewind/forward issue
    private static StorageManager mStorageManager;

    public PesiPlayer( Context context ){
        mContext = context;
        mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        mDtv = HiDtvMediaPlayer.getInstance() ;
        mTvPlayerCallbacks = new CopyOnWriteArrayList<>();
        if (listenersPrime != null && listenersPrime.size() != 0) {//eric lin 20181026 modify pesi player
            listenersPrime.clear();
        }
        listenersPrime = new CopyOnWriteArrayList<>();
        //mDtv.prepareDTV() ;

        SubScribeEvent();//eric lin 20181109 modify SubScribeEvent flow, add
    }
    public PesiPlayer( Context context, int type){//eric lin 20181107 record A watch B issue
        mDtv = HiDtvMediaPlayer.getInstance() ;
    }

    public void addListenerPrime(Listener listener) {//eric lin 20181026 modify pesi player
        //Log.d(TAG, "addListenerTest: EKK BK1 listeners_test.size()="+listenersPrime.size());
        listenersPrime.add(listener);
        //Log.d(TAG, "addListenerTest: EKK BK2 listeners_test.size()="+listenersPrime.size());
    }
    public void removeListenerPrime(Listener listener) {//eric lin 20181026 modify pesi player
        //Log.d(TAG, "removeListenerPrime: EKK BK1 listeners_test.size()="+listenersPrime.size());
        listenersPrime.remove(listener);
        //Log.d(TAG, "removeListenerPrime: EKK BK2 listeners_test.size()="+listenersPrime.size());
        
        UnSubScribeEvent();//eric lin 20181109 modify SubScribeEvent flow, add
    }

    public void addListener(Listener listener) {
        //SubScribeEvent();//eric lin 20181109 modify SubScribeEvent flow, mark //eric lin 20181026 modify pesi player, add
        //listeners.add(listener);//eric lin 20181026 modify pesi player, mark
    }
    public void removeListener(Listener listener) {
        //UnSubScribeEvent();//eric lin 20181109 modify SubScribeEvent flow,mark //eric lin 20181026 modify pesi player, add
        //listeners.remove(listener);//eric lin 20181026 modify pesi player, mark
    }

    private void registCallback( int callbackCmdStart, int callbackCmdEnd, IDTVListener scanListener)
    {
        for ( int i = callbackCmdStart ; i < callbackCmdEnd ; i++ )
            mDtv.subScribeEvent(i,scanListener,0) ;
    }

    private void unregistCallback( int callbackCmdStart, int callbackCmdEnd, IDTVListener scanListener)
    {
        for ( int i = callbackCmdStart ; i < callbackCmdEnd ; i++ )
            mDtv.unSubScribeEvent(i,scanListener) ;
    }

    private void SubScribeEvent() {
        registCallback(DTVMessage.HI_SVR_EVT_AV_CALLBACK_START, DTVMessage.HI_SVR_EVT_AV_CALLBACK_END, gAVListener);
        registCallback(DTVMessage.HI_SVR_EVT_PVR_CALLBACK_START, DTVMessage.HI_SVR_EVT_PVR_CALLBACK_END, gPVRListener ) ;
        registCallback(DTVMessage.HI_SVR_EVT_EPG_CALLBACK_START, DTVMessage.HI_SVR_EVT_EPG_CALLBACK_END, gEPGListener);
        //registerUsbReceiver(true); // Edwin 20181121 disable Time Shift when usb is mounted
    }

    private void UnSubScribeEvent() {
        unregistCallback(DTVMessage.HI_SVR_EVT_AV_CALLBACK_START, DTVMessage.HI_SVR_EVT_AV_CALLBACK_END, gAVListener);
        unregistCallback(DTVMessage.HI_SVR_EVT_PVR_CALLBACK_START, DTVMessage.HI_SVR_EVT_PVR_CALLBACK_END, gPVRListener ) ;
        unregistCallback(DTVMessage.HI_SVR_EVT_EPG_CALLBACK_START, DTVMessage.HI_SVR_EVT_EPG_CALLBACK_END, gEPGListener);
        //registerUsbReceiver(false); // Edwin 20181121 disable Time Shift when usb is mounted
    }

    private void registerUsbReceiver(boolean isRegister)
    {
        if ( isRegister ) {
            IntentFilter usbIntentFilter = new IntentFilter();
            usbIntentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
            usbIntentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            usbIntentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
            usbIntentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            usbIntentFilter.addDataScheme("file");
            mContext.registerReceiver(mUsbReceiver, usbIntentFilter);
        } else {
            if(mUsbReceiver != null) {
            mContext.unregisterReceiver(mUsbReceiver);
            mUsbReceiver = null;
        }
    }
    }

    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if ( action == null ) return;

            if ( action.equals(Intent.ACTION_MEDIA_EJECT) ) // prepare to remove
            {
                Log.d(TAG, "onReceive: ACTION_MEDIA_EJECT");
                mSession.notifyTimeShiftStatusChanged(TvInputManager.TIME_SHIFT_STATUS_UNSUPPORTED);
            }
            if ( action.equals( Intent.ACTION_MEDIA_UNMOUNTED ) ) // real remove
            {
                Log.d(TAG, "onReceive: ACTION_MEDIA_UNMOUNTED");
                mSession.notifyTimeShiftStatusChanged(TvInputManager.TIME_SHIFT_STATUS_UNSUPPORTED);
                Objects.requireNonNull(UsbMessage(R.string.STR_USB_DISK_NOT_READY)).show();
            }
            if ( action.equals( Intent.ACTION_MEDIA_MOUNTED ) )
            {
                Log.d(TAG, "onReceive: ACTION_MEDIA_MOUNTED");
                mSession.notifyTimeShiftStatusChanged(TvInputManager.TIME_SHIFT_STATUS_AVAILABLE);
                Objects.requireNonNull(UsbMessage(R.string.STR_USB_DISK_READY)).show();
            }
        }
    };

    private MessageDialogView UsbMessage( final int string_res)
    {
        PesiStorageHelper pesiStorageHelper = new PesiStorageHelper(mStorageManager);
        if ( string_res != R.string.STR_USB_DISK_READY && string_res != R.string.STR_USB_DISK_NOT_READY )
            return null;
        MessageDialogView usbMsg = new MessageDialogView(mContext, mContext.getString(string_res), 3000) {
            public void dialogEnd() {
                if ( string_res == R.string.STR_USB_DISK_NOT_READY )
                {
                    mDtv.setRecordPath(mDtv.getDefaultRecPath());
                    mDtv.saveTable(EnTableType.GPOS);
                    stop(true);
                    return;
                }

                for (Object volumeInfo : getVolumes()) {
                    String devType = pesiStorageHelper.getDevType(volumeInfo);
                    String path = pesiStorageHelper.getPath(volumeInfo);
                    if ( devType == null )
                        continue;
                    if ( devType.equals("USB2.0") || devType.equals("USB3.0") ) {
                        Log.d(TAG, "isUsbMounted: volumeInfo.path = "+path);
                        mDtv.setRecordPath(path);
                        mDtv.saveTable(EnTableType.GPOS);
                        return;
                    }
                }
                mDtv.setRecordPath(mDtv.getDefaultRecPath());
                mDtv.saveTable(EnTableType.GPOS);
            }
        };
        usbMsg.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        return usbMsg;
    }

    public static List<Object> getVolumes ()
    {
        PesiStorageHelper pesiStorageHelper = new PesiStorageHelper(mStorageManager);
        return pesiStorageHelper.getVolumes();
    }

    IDTVListener gAVListener = new IDTVListener()
    {
        private static final String CB_TAG="AVListener";
        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj)
        {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            switch (messageID)
            {
                case DTVMessage.HI_SVR_EVT_AV_PLAY_SUCCESS:
                {
                    Log.d(TAG, "HI_SVR_EVT_AV_PLAY_SUCCESS");
                    for (Listener listener : listenersPrime) {
                        Log.d(TAG, "notifyMessage: CKK BK4");
                        listener.onStateChanged(mplayWhenReady, 1);
                    }
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_FRONTEND_STOP:
                {
                    Log.d(TAG, "HI_SVR_EVT_AV_FRONTEND_STOP");
                    for (Listener listener : listenersPrime) {
                        Log.d(TAG, "notifyMessage: CKK BK1");
                        listener.onStateChanged(mplayWhenReady, 0);
                    }
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_FRONTEND_RESUME:
                {
                    Log.d(TAG, "HI_SVR_EVT_AV_FRONTEND_RESUME");
                    for (Listener listener : listenersPrime) {
                        Log.d(TAG, "notifyMessage: CKK BK3");
                        listener.onStateChanged(mplayWhenReady, 1);
                    }
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_CHANNEL_LOCKED:
                {
                    Log.d(TAG, "HI_SVR_EVT_AV_CHANNEL_LOCKED");

                    // Edwin 20181114 add HI_SVR_EVT_AV_CHANNEL_LOCKED handler -s
                    int avLockStatus = ((Parcel) obj).readInt();
                    long channelId = ((Parcel) obj).readInt() & 0xFFFFFFFFL;
                    int rating = ((Parcel) obj).readInt();
                    switch( avLockStatus )//lock status 0(no lock), 1(user lock), 2(parental lock)
                    {
                        case 0://no lock
                        {
                            for (Listener listener : listenersPrime) {
                                Log.d(TAG, "notifyMessage: no lock");
                                listener.onStateChanged(mplayWhenReady, 1);
                            }
                        }break;
                        case 1://user lock
                        {
                            for (Listener listener : listenersPrime) {
                                Log.d(TAG, "notifyMessage: user lock");
                                listener.onStateChanged(mplayWhenReady, 0);
                            }
                        }break;
                        case 2://parental lock
                        {
                            for (Listener listener : listenersPrime) {
                                Log.d(TAG, "notifyMessage: parental lock");
                                listener.onStateChanged(mplayWhenReady, 1);
                            }
                        }break;
                    }
                    // Edwin 20181114 add HI_SVR_EVT_AV_CHANNEL_LOCKED handler -e
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_STOP:
                {
                    Log.d(TAG, "HI_SVR_EVT_AV_STOP") ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_SIGNAL_STAUTS:
                {
                    Log.d(TAG, "HI_SVR_EVT_AV_SIGNAL_STAUTS signal" + (param1==1?"LOCK!!!!":"UNLOCK!!!!") );
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_MOTOR_MOVING:
                {
                    Log.d(TAG, "HI_SVR_EVT_AV_MOTOR_MOVING");
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_CHANNEL_SCRAMBLED:
                {
                    Log.d(TAG, "HI_SVR_EVT_AV_CHANNEL_SCRAMBLED " + (param1==1?"audio":(param1==2?"video":"nothing")) + "is scramble!!!" );
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_PLAY_FAILURE:
                {
                    Log.d(TAG, "HI_SVR_EVT_AV_PLAY_FAILURE Error Code = " + param1);
                    break;
                }
                case DTVMessage.HI_SVR_EVT_AV_CA:
                {
                    Log.d(TAG, "HI_SVR_EVT_AV_CA is " + (param1==1?"CA ":"NOT CA") + " prog" );
                    break;
                }
                default:
                    break;
            }
        }
    };
    IDTVListener gPVRListener = new IDTVListener()
    {
        private static final String CB_TAG="PVRListener";
        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj)
        {
            Log.d(TAG, CB_TAG + "gPVRListener messageID = " + messageID);
            switch (messageID)
            {
                case DTVMessage.HI_SVR_EVT_PVR_PLAY_EOF:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_PLAY_EOF") ;
                    mPlayEofFlag=1;//eric lin 20181031 fix rewind/forward issue
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PVR_PLAY_SOF:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_PLAY_SOF");
                    mPlaySofFlag=1;//eric lin 20181031 fix rewind/forward issue
                    play(); // Edwin 20181109 fix player not play TimeShift when rewind by rewind key
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PVR_PLAY_ERROR:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_PLAY_ERROR") ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PVR_PLAY_REACH_REC:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_PLAY_REACH_REC") ;
                    stop(true);
                    for ( TvPlayer.Callback callback : mTvPlayerCallbacks ) {
                        Log.d(TAG, "notifyMessage: callback.onCompleted()");
                        callback.onCompleted(); // Edwin 20181022 finish TimeShift
                    }
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PVR_REC_DISKFULL:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_REC_DISKFULL");
                    stopAllRec();//eric lin 20181116 live tv record disk full
                    mRecordingSession.notifyError(TvInputManager.RECORDING_ERROR_INSUFFICIENT_SPACE);//eric lin 20181116 live tv record disk full
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PVR_REC_ERROR:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_REC_ERROR") ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PVR_REC_OVER_FIX:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_REC_OVER_FIX param1 = " + param1 + "param2 = " + param2) ;
                    // Edwin 20181030 timeshift auto play
                    int pvrMode = param2;
                    Log.d(TAG, "notifyMessage: pvrMode = "+pvrMode+" mFullBuffer = "+mFullBuffer);
                    if ( pvrMode != PvrInfo.EnPVRMode.NO_ACTION && !mFullBuffer ) // Edwin 20181030 fix more start pos cause playTime early reach sysTime & wrong progress
                    {
                        quickPlay();
                        mFullBuffer = true;
                    }
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PVR_REC_REACH_PLAY:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_REC_REACH_PLAY");
                    break;
                }
                case DTVMessage.HI_SVR_EVT_PVR_REC_DISK_SLOW:
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_REC_DISK_SLOW") ;
                    break;
                }
                case DTVMessage.PESI_EVT_PVR_RECORD_START ://eric lin 20180713 pvr msg,-start
                {
                    Log.d(TAG, "PESI_EVT_PVR_RECORD_START") ;

                    break;
                }
                case DTVMessage.PESI_EVT_PVR_RECORD_STOP :
                {
                    Log.d(TAG, "PESI_EVT_PVR_RECORD_STOP") ;

                    break;
                }
                case DTVMessage.PESI_EVT_PVR_TIMESHIFT_START :
                {
                    Log.d(TAG, "PESI_EVT_PVR_TIMESHIFT_START") ;

                    break;
                }
                case DTVMessage.PESI_EVT_PVR_TIMESHIFT_PLAY_START :
                {
                    Log.d(TAG, "PESI_EVT_PVR_RECORD_STOP") ;

                    break;
                }
                case DTVMessage.PESI_EVT_PVR_TIMESHIFT_STOP :
                {
                    Log.d(TAG, "PESI_EVT_PVR_RECORD_STOP") ;

                    break;
                }
                case DTVMessage.PESI_EVT_PVR_FILE_PLAY_START :
                {
                    Log.d(TAG, "PESI_EVT_PVR_RECORD_STOP") ;

                    break;
                }
                case DTVMessage.PESI_EVT_PVR_FILE_PLAY_STOP :
                {
                    Log.d(TAG, "PESI_EVT_PVR_RECORD_STOP") ;

                    break;
                }//eric lin 20180713 pvr msg,-end
                case DTVMessage.PESI_EVT_PVR_PLAY_PARENTAL_LOCK:  //connie 20180806 for pvr parentalRate
                {
                    Log.d(TAG, "PESI_EVT_PVR_PLAY_PARENTAL_LOCK") ;

                }break;
                case DTVMessage.HI_SVR_EVT_PVR_OPEN_HD_FINISH:  //Scoty 20180827 add HDD Ready command and callback
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_OPEN_HD_FINISH") ;

                }break;
                case DTVMessage.HI_SVR_EVT_PVR_TIMESHIFT_PLAY_STOP:  //Scoty 20180827 add HDD Ready command and callback
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_TIMESHIFT_PLAY_STOP") ;
                }break;
                case DTVMessage.HI_SVR_EVT_PVR_NOT_SUPPORT: //Scoty 20180827 add HDD Ready command and callback
                {
                    Log.d(TAG, "HI_SVR_EVT_PVR_NOT_SUPPORT") ;
                    stop(true);
                }break;
                default:
                    break;
            }
        }
    };

    IDTVListener gEPGListener = new IDTVListener()
    {
        private static final String CB_TAG="EPGListener ";
        @Override
        public void notifyMessage(int messageID, int param1, int parm2, Object obj)
        {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            long channelID = 0;
            switch (messageID)
            {
                case DTVMessage.HI_SVR_EVT_EPG_PF_VERSION_CHANGED:
                {
                    Log.d(TAG, "HI_SVR_EVT_EPG_PF_VERSION_CHANGED rowID = " + param1) ;
                    channelID = param1;
                    PesiTvInputService.updateEPG(mContext, "HI_SVR_EVT_EPG_PF_VERSION_CHANGED"); // Edwin 20181108 update EPG or clean EPG
                    break;
                }
                case DTVMessage.HI_SVR_EVT_EPG_PF_CURR_PROGRAM_FINISH:
                {
                    Log.d(TAG, "HI_SVR_EVT_EPG_PF_CURR_PROGRAM_FINISH rowID = " + param1);
                    channelID = param1;
                    PesiTvInputService.updateEPG(mContext, "HI_SVR_EVT_EPG_PF_CURR_PROGRAM_FINISH"); // Edwin 20181108 update EPG or clean EPG
                    break;
                }
                case DTVMessage.HI_SVR_EVT_EPG_PF_CURR_FREQ_FINISH:
                {
                    Log.d(TAG, "HI_SVR_EVT_EPG_PF_CURR_FREQ_FINISH rowID = " + param1) ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_EPG_PF_ALL_FINISH:
                {
                    Log.d(TAG, "HI_SVR_EVT_EPG_PF_ALL_FINISH");
                    break;
                }
                case DTVMessage.HI_SVR_EVT_EPG_SCH_VERSION_CHANGED:
                {
                    Log.d(TAG, "HI_SVR_EVT_EPG_SCH_VERSION_CHANGED rowID = " + param1) ;
                    channelID = param1;
                    PesiTvInputService.updateEPG(mContext, "HI_SVR_EVT_EPG_SCH_VERSION_CHANGED"); // Edwin 20181108 update EPG or clean EPG
                    break;
                }
                case DTVMessage.HI_SVR_EVT_EPG_SCH_CURR_PROGRAM_FINISH:
                {
                    Log.d(TAG, "HI_SVR_EVT_EPG_SCH_CURR_PROGRAM_FINISH rowID = " + param1) ;
                    channelID = param1;
                    PesiTvInputService.updateEPG(mContext, "HI_SVR_EVT_EPG_SCH_CURR_PROGRAM_FINISH"); // Edwin 20181108 update EPG or clean EPG
                    break;
                }
                case DTVMessage.HI_SVR_EVT_EPG_SCH_CURR_FREQ_FINISH:
                {
                    Log.d(TAG, "HI_SVR_EVT_EPG_SCH_CURR_FREQ_FINISH rowID = " + param1) ;
                    break;
                }
                case DTVMessage.HI_SVR_EVT_EPG_SCH_ALL_FINISH:
                {
                    Log.d(TAG, "HI_SVR_EVT_EPG_SCH_ALL_FINISH" );
                    break;
                }
                case DTVMessage.HI_SVR_EVT_EPG_PARENTAL_RATING:
                {
                    Log.d(TAG, "HI_SVR_EVT_EPG_PARENTAL_RATING rowID = " + param1);
                    break;
                }
                default:
                    break;
            }
        }
    };

    public void setPlayWhenReady(boolean playWhenReady) {
        //Log.d(TAG, "setPlayWhenReady: playWhenReady="+playWhenReady);
        mplayWhenReady = playWhenReady;
    }

    //below is TvPlayer need to implement
    @Override
    public void seekTo(long position) // Edwin 20181024 seekTo begin or seekTo end
    {
        //Log.d(TAG, "seekTo: palyMode="+mPlayMode+", position="+position);
        if(mPlayMode == MODE_PLAYBACK) {//Playback
            Log.d(TAG, "seekTo: PesiPlayer palyMode="+mPlayMode+", position="+position);
            long pos = position/1000;
            mDtv.pvrPlaySeekTo((int)pos);
        }
        else if(mPlayMode == MODE_TIMESHIFT) {//Timeshift
            //int recTime = mDtv.pvrTimeShiftGetRecordTime(0);
            //int seekToSec;
            //long secMs;
            //secMs = ( position >= mStartPosition ) ?
            //        position - mStartPosition : mStartPosition - position;
            //seekToSec = (int) (secMs / 1000);
            //seekToSec = ( seekToSec > recTime ) ? recTime : seekToSec;
            //Log.d(TAG, "seekTo: recTime = "+recTime);
            //Log.d(TAG, "seekTo: seekToSec = "+seekToSec);
            Log.d(TAG, "seekTo: position = "+position);
            Log.d(TAG, "seekTo: mStartPosition = "+mStartPosition);
            Log.d(TAG, "seekTo: System.currentTimeMillis() = "+System.currentTimeMillis());

            if ( position == mStartPosition ) // Edwin 20181029 seekTo begin
            {
                mSeekToBegin = true;
                quickPlay();
                mDtv.pvrTimeShiftSeekPlay(0, 0);
                Log.d(TAG, "seekTo: mSeekToBegin = "+mSeekToBegin);
                return;
            }
            // Edwin 20181029 seekTo end
            stop(true);
        }
    }

    @Override
    public void setPlaybackParams(PlaybackParams params)
    {
        float speed = params.getSpeed();
        Log.d(TAG, "setPlaybackParams: getSpeed = "+speed);

        if(mPlayMode == MODE_PLAYBACK) {
            Log.d(TAG, "setPlaybackParams: PesiPlayer mPlayMode=4 speed="+speed);
            if (speed == 1.0) {
                mDtv.pvrPlayTrickPlay(FAST_FORWARD_NORMAL);
            } else if (speed == 2.0) {
                mDtv.pvrPlayTrickPlay(FAST_FORWARD_TWO);
            } else if (speed == 4.0) {
                mDtv.pvrPlayTrickPlay(FAST_FORWARD_FOUR);
            } else if (speed == 12.0) {
                mDtv.pvrPlayTrickPlay(FAST_FORWARD_EIGHT);
            } else if (speed == 48.0) {
                mDtv.pvrPlayTrickPlay(FAST_FORWARD_SIXTEEN);
            } else if (speed == -2.0) {
                mDtv.pvrPlayTrickPlay(FAST_BACKWARD_TWO);
            } else if (speed == -4.0) {
                mDtv.pvrPlayTrickPlay(FAST_BACKWARD_FOUR);
            } else if (speed == -12.0) {
                mDtv.pvrPlayTrickPlay(FAST_BACKWARD_EIGHT);
            } else if (speed == -48.0) {
                mDtv.pvrPlayTrickPlay(FAST_BACKWARD_SIXTEEN);
            }
        }
        else if(mPlayMode == MODE_TIMESHIFT) {//Timeshift
            //play(); // Edwin 20181026 play() not work for setPlaybackParams()
            quickPlay();
            if ( speed == 1.0 ) {
                mDtv.pvrTimeShiftTrickPlay(0, FAST_FORWARD_NORMAL);
            } else if ( speed == 2.0 ) {
                mDtv.pvrTimeShiftTrickPlay(0, FAST_FORWARD_TWO);
            } else if ( speed == 8.0 || speed == 4.0 ) {
                mDtv.pvrTimeShiftTrickPlay(0, FAST_FORWARD_FOUR);
            } else if ( speed == 32.0 || speed == 12.0 ) {
                mDtv.pvrTimeShiftTrickPlay(0, FAST_FORWARD_EIGHT);
            } else if ( speed == 128.0 || speed == 48.0 ) {
                mDtv.pvrTimeShiftTrickPlay(0, FAST_FORWARD_SIXTEEN);
            } else if ( speed == -2.0 ) {
                mDtv.pvrTimeShiftTrickPlay(0, FAST_BACKWARD_TWO);
            } else if ( speed == -8.0 || speed == -4.0 ) {
                mDtv.pvrTimeShiftTrickPlay(0, FAST_BACKWARD_FOUR);
            } else if ( speed == -32.0 || speed == -12.0 ) {
                mDtv.pvrTimeShiftTrickPlay(0, FAST_BACKWARD_EIGHT);
            } else if ( speed == -128.0 || speed == -48.0 ) {
                mDtv.pvrTimeShiftTrickPlay(0, EnTrickMode.FAST_BACKWARD_SIXTEEN);
            }
        }
    }

    public long getStartPosition () // Edwin 20181024 start position of timeShift.ts on progress bar
    {
        getDuration();
        if ( mStartPosition == TIME_SHIFT_NO_ACTION )
        {
            //Log.d(TAG, "getStartPosition: No Action");
            return System.currentTimeMillis(); // Edwin 20181024 make buffer start point moving
        }
        // Edwin 20181101 fix full buffer
        if ( mFullBuffer )
        {
            Log.d(TAG, "getStartPosition: mFullBuffer = "+mFullBuffer);
            mStartPosition = System.currentTimeMillis() - (mDtv.GposInfoGet().getTimeshiftDuration()*1000);
        }
        // Edwin 20181101 fix play/pause icon when seekTo begin
        if ( mSeekToBegin )
        {
            Log.d(TAG, "getStartPosition: seekTo begin");
            mSeekToBegin = false;
            return System.currentTimeMillis() - 2000;
        }
        Log.d(TAG, "getStartPosition:   mStartPosition = "+mStartPosition);
        return mStartPosition; // Edwin 20181029 return start position
    }

    @Override
    public long getCurrentPosition () // Edwin 20181024 make seek point moving on progress bar
    {
        if(mPlayMode == MODE_PLAYBACK) {//Playback
            long playTime;
            playTime = mDtv.pvrPlayGetPlayTimeMs();
            if(mPlaySofFlag == 1){//eric lin 20181031 fix rewind/forward issue
                //Log.d(TAG, "getCurrentPosition: KKK mPlaySofFlag==1 and playTime="+playTime+", set to 0");
                mPlaySofFlag =0;
                playTime = 0;
            }else if(mPlayEofFlag == 1){
                mPlaySofFlag =0;
                playTime = mRecordingDuration;
                //Log.d(TAG, "getCurrentPosition: KKK mPlayEofFlag=1 and set play time="+playTime);//eric lin test
            }
            //return playTime*1000;
//            if(playTime == 11)
//                return 12*1000;
//            else
//            if(mPlayFileDuration == playTime*1000)
//                mDtv.pvrPlayStop();
            //if(playTime == 49)
            //    playTime = 50;
            return playTime;
        }
        else if(mPlayMode == MODE_TIMESHIFT)
        {
            // Timeshift getCurrentPosition()
            long playTime = mDtv.pvrTimeShiftGetPlaySecond(0);
            if ( playTime == TIME_SHIFT_NO_ACTION )
            {
                if ( mPausePosition == TIME_SHIFT_NO_ACTION )
                {
                    //Log.d(TAG, "getCurrentPosition: No Action");
                    return System.currentTimeMillis(); // Edwin 20181024 make seek point moving
                }
                //Log.d(TAG, "getCurrentPosition: mPausePosition = " + mPausePosition);
                return mPausePosition;
            }
            Log.d(TAG, "getCurrentPosition: playTime = " + playTime);
            //Log.d(TAG, "getCurrentPosition:" +
            //        " recTime = "+mDtv.pvrTimeShiftGetRecordTime(0)+
            //        " diff = "+(System.currentTimeMillis()-mStartPosition)/1000);
            playTime = playTime * 1000;
            playTime = playTime + mStartPosition; // Edwin 20181024 seek point keep moving from base sec
            //Log.d(TAG, "getCurrentPosition: playTime = " + playTime);
            return playTime;
        }
        return 0;
    }

    @Override
    public long getDuration()
    {
        if(mPlayMode == MODE_PLAYBACK) {//Playback
            Log.d(TAG, "getDuration: PesiPlayer MODE_PLAYBACK");
        }
        else if(mPlayMode == MODE_TIMESHIFT) {//Timeshift
            int duration = mDtv.pvrTimeShiftGetRecordTime(0);
            if ( duration != 0 && duration % 10 == 0 )
            {
                Log.d(TAG, "getDuration: "+duration);
                //Toast toast = Toast.makeText(mContext, "Duration: "+(duration/60)+" min", Toast.LENGTH_LONG);
                //toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 20);
                //toast.show();
            }
            return duration * 1000;
        }
        return 0;
    }

    @Override
    public void setSurface(Surface surface) {
        Log.d(TAG, "setSurface: "+surface);
        mDtv = HiDtvMediaPlayer.getInstance();
        if(surface==null)
            Log.d(TAG, "setSurface: surface==null");
        else {
            Log.d(TAG, "setSurface: surface!=null");
            //mDtv.pipModSetDisplay(surface, 0); // jim 2019/09/05 change set surface timming to fix fast continually zapping cause video black
            //mDtv.AvControlOpen(0);//eric lin 20181204 adjust live tv, mark // Edwin 20181121 open AV only one time at setSurface()
        }
    }

    @Override
    public void setVolume(float volume) {
        //Log.d(TAG, "setVolume: "+volume);
    }

    @Override
    public void pause() {

        if(mPlayMode == MODE_PLAYBACK) {//Playback
            Log.d(TAG, "pause: PesiPlayer MODE_PLAYBACK");
            mDtv.pvrPlayPause();
        }
        else if(mPlayMode == MODE_TIMESHIFT) // Timeshift
        {
            // Timeshift pause()
            //GposInfo gposInfo = mDtv.GposInfoGet();
            //gposInfo.setTimeshiftDuration(60);
            //mDtv.GposInfoUpdate(gposInfo);
            int pvrMode = mDtv.pvrGetCurrentPvrMode(mChannelId);
            int ret;
            String timeShiftPath = mDtv.GetRecordPath() + "/timeshift.ts";
            Log.d(TAG, "pause: timeShiftPath = "+timeShiftPath);
            mPausePosition = System.currentTimeMillis(); // Edwin 20181024 make seek point stop at pause position

            if ( pvrMode == PvrInfo.EnPVRMode.NO_ACTION )
            {
                Log.d(TAG, "pause: time shift start");
                mStartPosition = mPausePosition - 2000; // Edwin 20181024 time shift start with a new start position
                //Edwin 20181026 record in sdcard when no USB
                ret = mDtv.pvrTimeShiftStart(0, mDtv.GposInfoGet().getTimeshiftDuration(), 0, timeShiftPath);
                if ( ret == -1 ) {
                    timeShiftPath = Environment.getExternalStorageDirectory().getPath() + "/timeshift.ts";
                    mDtv.pvrTimeShiftStart(0, mDtv.GposInfoGet().getTimeshiftDuration(), 0, timeShiftPath);
                }
                mStartTimeShift = Toast.makeText(mContext, "TimeShift Pause...", Toast.LENGTH_LONG);
                mStartTimeShift.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 20);
                mStartTimeShift.show();
            }
            else {
                Log.d(TAG, "pause: time shift pause");
                mDtv.pvrTimeShiftLivePause(0);
            }

            for ( TvPlayer.Callback callback : mTvPlayerCallbacks ) {
                Log.d(TAG, "pause: callback.onPaused()");
                callback.onPaused(); // Edwin 20181022 pause TimeShift
            }
        }
        setPlayWhenReady(false);
    }

    @Override
    public void play() {
        if(mPlayMode == MODE_PLAYBACK){//Playback
            int playStatus = mDtv.pvrPlayGetCurrentStatus();
            //int playTime = mdtv.PvrPlayGetPlayTime();
            //int speed = mdtv.PvrPlayGetCurrentTrickMode().getValue() / 1024;
            switch (playStatus)
            {
                case EnPVRPlayStatus.PLAY:
                    Log.d(TAG, "play: PesiPlayer palyMode="+mPlayMode+", PLAY");
                    break;

                case EnPVRPlayStatus.PAUSE:
                    Log.d(TAG, "play: PesiPlayer palyMode="+mPlayMode+", PAUSE");
                    mDtv.pvrPlayResume();
                    break;

                case EnPVRPlayStatus.FAST_FORWARD:
                    Log.d(TAG, "play: PesiPlayer palyMode="+mPlayMode+", FAST_FORWARD");
                    break;

                case EnPVRPlayStatus.FAST_BACKWARD:
                    Log.d(TAG, "play: PesiPlayer palyMode="+mPlayMode+", FAST_BACKWARD");
                    break;
            }
        }
        else if(mPlayMode == MODE_TIMESHIFT) // Timeshift
        {
            // timeshift play()
            final int pvrMode = mDtv.pvrGetCurrentPvrMode(mChannelId);
            if ( pvrMode == PvrInfo.EnPVRMode.PVRMODE_TIMESHIFT_LIVE_PAUSE )
            {
                Log.d(TAG, "play: time shift play");
                mRunCount = 0;
                // Edwin 20181026 make time shift to wait a time before playing
                final Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run () {
                        int pauseTime = mDtv.pvrTimeShiftGetRecordTime(0);
                        if ( pauseTime > 5 )
                        {
                            Log.d(TAG, "play: time shift play success");
                            mSession.notifyTimeShiftStatusChanged(TvInputManager.TIME_SHIFT_STATUS_AVAILABLE);
                            mDtv.pvrTimeShiftPlayForLiveChannel(pvrMode);
                            mStartTimeShift.cancel();
                            Toast toast = Toast.makeText(mContext, "TimeShift Play...", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 20);
                            toast.show();
                            return;
                        }
                        if ( mRunCount++ == 10 )
                        {
                            mRunCount = 0;
                            return;
                        }
                        mSession.notifyTimeShiftStatusChanged(TvInputManager.TIME_SHIFT_STATUS_UNAVAILABLE);
                        Log.d(TAG, "play: time shift play too quick, pauseTime = "+pauseTime);
                        handler.postDelayed(this, 1000);
                    }
                });
            }
            else
            {
                Log.d(TAG, "play: time shift resume");
                mDtv.pvrTimeShiftPlayForLiveChannel(pvrMode);
            }

            for ( TvPlayer.Callback callback : mTvPlayerCallbacks ) {
                if ( pvrMode == PvrInfo.EnPVRMode.PVRMODE_TIMESHIFT_LIVE_PAUSE ) {
                    Log.d(TAG, "play: callback.onStarted()");
                    callback.onStarted(); // Edwin 20181022 start playing TimeShift
                }
                else {
                    Log.d(TAG, "play: callback.onResumed()");
                    callback.onResumed(); // Edwin 20181022 resume TimeShift
                }
            }
        }
        setPlayWhenReady(true);
    }

    public void stop (boolean stopTimeShift) //eric lin 20181109 live tv timeshift stop // Edwin 20181024 stop time shift
    {
        Log.d(TAG, "stop: ");
        if ( stopTimeShift )
        {
            mDtv.pvrTimeShiftStop(0);
        }
        mStartPosition = TIME_SHIFT_NO_ACTION;
        mPausePosition = TIME_SHIFT_NO_ACTION;
        mFullBuffer = false;
        mRunCount = 0;
    }
    public void stop()
    {
        Log.d(TAG, "stop: ");
        mStartPosition = TIME_SHIFT_NO_ACTION;
        mPausePosition = TIME_SHIFT_NO_ACTION;
        mFullBuffer = false;
        mRunCount = 0;
    }

    @Override
    public void registerCallback(Callback callback) {
        Log.d(TAG, "registerCallback: "+callback);
        mTvPlayerCallbacks.add(callback);
    }

    @Override
    public void unregisterCallback(Callback callback) {
        Log.d(TAG, "unregisterCallback: "+callback);
        mTvPlayerCallbacks.remove(callback);
    }

   //eric lin 20181102 live tv track,-start
    public int getTrackCount(int type) {
        if(type == TvTrackInfo.TYPE_AUDIO){
            AudioInfo AudioComp = mDtv.AvControlGetAudioListInfo(0/*playId*/);
            return AudioComp.getComponentCount();
        }else if(type == TvTrackInfo.TYPE_VIDEO){
            return 0;
        }else if(type == TvTrackInfo.TYPE_SUBTITLE){
            SubtitleInfo SubtitleComp = mDtv.AvControlGetSubtitleList(0/*playId*/);
            if(SubtitleComp != null) {
                Log.d(TAG, "getTrackCount: XKK type=text count="+SubtitleComp.getComponentCount());
                return SubtitleComp.getComponentCount() - 1;//return SubtitleComp.getComponentCount()-1;//return SubtitleComp.getComponentCount(); //eric lin test
            }
        }
        return 0;
    }

    public PesiMediaFormat getTrackFormat(int type, int index) {
        PesiMediaFormat mf = new PesiMediaFormat();
        if(type == TvTrackInfo.TYPE_AUDIO){
            AudioInfo AudioComp = mDtv.AvControlGetAudioListInfo(0/*playId*/);
            //for (int i = 0; i < AudioComp.getComponentCount(); i++) {
                mf.setChannelCount(2);
                mf.setSampleRate(0);
                mf.setLanguage(AudioComp.ComponentList.get(index).getLangCode());
            Log.d(TAG, "getTrackFormat: XKK index="+index+", audio lang="+mf.getLanguage());
            //}
        }else if(type == TvTrackInfo.TYPE_VIDEO){
//            mf.setWidth(mDtv.AvControlGetVideoResolutionWidth(0/*playId*/));
//            mf.setHeight(mDtv.AvControlGetVideoResolutionHeight(0/*playId*/));
//            Log.d(TAG, "getTrackFormat: XKK index="+index+", video w="+mf.getWidth()+", h="+mf.getHeight());
        }else if(type == TvTrackInfo.TYPE_SUBTITLE){
            SubtitleInfo SubtitleComp = mDtv.AvControlGetSubtitleList(0/*playId*/);
            int testIndex = index + 1;//avoid first option off
            if(SubtitleComp != null) {
                mf.setLanguage(SubtitleComp.Component.get(testIndex).getLangCode());//mf.setLanguage(SubtitleComp.Component.get(index).getLangCode());
                Log.d(TAG, "getTrackFormat: XKK index="+index+", lang="+mf.getLanguage());
            }
            else
                mf.setLanguage(null);
        }else
            return null;
        return mf;
    }

    public int getSelectedTrack(int type) {
        if(type == TvTrackInfo.TYPE_AUDIO){
            AudioInfo AudioComp = mDtv.AvControlGetAudioListInfo(0/*playId*/);
            return AudioComp.getCurPos();
        }else if(type == TvTrackInfo.TYPE_VIDEO){
            return 0;
        }else if(type == TvTrackInfo.TYPE_SUBTITLE){
            SubtitleInfo SubtitleComp = mDtv.AvControlGetSubtitleList(0/*playId*/);
            if(SubtitleComp != null) {
                Log.d(TAG, "getSelectedTrack: XKK type=text getCurPos()="+SubtitleComp.getCurPos());
                int curPos = SubtitleComp.getCurPos()-1;
                if(curPos <0)
                    curPos = 0;
                return curPos;

//                if(curPos == 0)
//                    return -1;
//                else{
//                    return SubtitleComp.getCurPos() - 1;//return SubtitleComp.getCurPos();
//                }
            }
        }
        return 0;
    }

    public void setSelectedTrack(int type, int index) {
        if(type == TvTrackInfo.TYPE_AUDIO){
            AudioInfo AudioComp = mDtv.AvControlGetAudioListInfo(0/*playId*/);
            Log.d(TAG, "setSelectedTrack: XKK type=audio index="+index);
            mDtv.AvControlChangeAudio(0/*playId*/, AudioComp.getComponent(index));
        }else if(type == TvTrackInfo.TYPE_VIDEO){
            Log.d(TAG, "setSelectedTrack: XKK type=video index="+index);
        }else if(type == TvTrackInfo.TYPE_SUBTITLE){
            Log.d(TAG, "setSelectedTrack: YKK type=text index=" + index);
            //if(from == 0 && index == 0){
            //}else {
                if (index == -1) {
                    SubtitleInfo SubtitleComp = mDtv.AvControlGetSubtitleList(0/*playId*/);
                    if (SubtitleComp != null && SubtitleComp.getComponentCount() > 0) {
                        mDtv.AvControlSelectSubtitle(0/*playId*/, SubtitleComp.getComponent(0));//mDtv.AvControlSelectSubtitle(0/*playId*/, SubtitleComp.getComponent(index));
                    }
                } else {
                    SubtitleInfo SubtitleComp = mDtv.AvControlGetSubtitleList(0/*playId*/);
                    int testIndex = index + 1;
                    if (SubtitleComp != null && testIndex < SubtitleComp.getComponentCount())
                        mDtv.AvControlSelectSubtitle(0/*playId*/, SubtitleComp.getComponent(testIndex));//mDtv.AvControlSelectSubtitle(0/*playId*/, SubtitleComp.getComponent(index));
                }
            //}
        }


    }
    //eric lin 20181102 live tv track,-end
 
    private void quickPlay() // Edwin 20181030 simple play function
    {
        Log.d(TAG, "quickPlay: ");
        int pvrMode = mDtv.pvrGetCurrentPvrMode(mChannelId);
        mDtv.pvrTimeShiftPlayForLiveChannel(pvrMode);
        for ( TvPlayer.Callback callback : mTvPlayerCallbacks ) {
            if ( pvrMode == PvrInfo.EnPVRMode.PVRMODE_TIMESHIFT_LIVE_PAUSE ) {
                Log.d(TAG, "quickPlay: callback.onStarted()");
                callback.onStarted();
            }
            else {
                Log.d(TAG, "quickPlay: callback.onResumed()");
                callback.onResumed();
            }
        }
        setPlayWhenReady(true);
    }

    public void setChannelId ( long channelId )
    {
        this.mChannelId = channelId;
    }

    public void setSession ( PesiTvInputService.PesiTvInputSessionImpl session )
    {
        this.mSession = session;
    }

    public void setPlayMode ( int mode )//eric lin 20181026 modify pesi player
    {
        this.mPlayMode = mode;
    }

    public int getPlayMode ()//eric lin 20181026 modify pesi player
    {
        return this.mPlayMode;
    }

    public void setRecordingDuration(long duration){//eric lin 20181031 fix rewind/forward issue
        mRecordingDuration = duration;
    }

    //eric lin 20181116 live tv record disk full,-start
    public void setRecordingSession (PesiTvInputService.RecordingSession session )
    {
        this.mRecordingSession = session;
    }

    public void stopAllRec()//Scoty 20180720 add stop all records/timeshift
    {
        List<PvrInfo> pvrList = new ArrayList<PvrInfo>();
        pvrList = mDtv.pvrRecordGetAllInfo();
        if (pvrList.size() > 0) {
            for(int i = 0 ; i < pvrList.size() ; i++)
            {
                int recId =pvrList.get(i).getRecId();//Scoty 20180809 modify dual pvr rule
                int pvrMode = pvrList.get(i).getPvrMode();
                stopPVRMode(pvrMode, recId,0);//Scoty 20180809 modify dual pvr rule
            }
        }
    }

    public void stopPVRMode( int pvrMode, int recId ,int playav)//Scoty 20180809 modify dual pvr rule//eric lin 20180629 stop pvr and av play
    {
        stopPVR(pvrMode, recId);//Scoty 20180809 modify dual pvr rule//eric lin 20180712 pesi pvr for one rec
        if(playav == 1 && (CheckPvrMode(pvrMode))) {//Scoty 20180827 add and modify TimeShift Live Mode//eric lin 20180629 stop pvr and av play
            mDtv.AvControlPlayByChannelId(0/*ViewHistory.getPlayId()*/, mChannelId/*ViewHistory.getCurChannel().getChannelId()*/, 0 /*ViewHistory.getCurGroupType()*/, 1);
        }
    }

    public void stopPVR( int pvrMode, int recId)
    {
        //Scoty 20180716 fixed timeshft/record not stop -s
        if(CheckPvrMode(pvrMode))//TimeShift //Scoty 20180827 add and modify TimeShift Live Mode
        {
            Log.d(TAG, "stopPVR: TIMESHIFT");
            mDtv.pvrTimeShiftStop(0 /*ViewHistory.getPlayId()*/);
        }
        else if (pvrMode == PvrInfo.EnPVRMode.RECORD) {
            Log.d(TAG, "stopPVR: Record, recId="+recId);
            mDtv.pvrRecordStop(0 /*ViewHistory.getPlayId()*/, recId);
        }
        //Scoty 20180716 fixed timeshft/record not stop -e
    }

    public boolean CheckPvrMode(int pvrMode) //Scoty 20180827 add and modify TimeShift Live Mode
    {
        if(pvrMode==PvrInfo.EnPVRMode.TIMESHIFT_LIVE || pvrMode==PvrInfo.EnPVRMode.TIMESHIFT_FILE
                || pvrMode==PvrInfo.EnPVRMode.PVRMODE_TIMESHIFT_LIVE_PAUSE)
            return true;
        else
            return false;
    }
    //eric lin 20181116 live tv record disk full,-end
}
