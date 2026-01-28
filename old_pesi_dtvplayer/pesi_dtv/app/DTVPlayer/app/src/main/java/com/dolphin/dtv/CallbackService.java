package com.dolphin.dtv;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Parcel;
import android.util.Log;
import android.view.WindowManager;

import com.prime.dtvplayer.Activity.ViewActivity;
import com.prime.dtvplayer.Config.Pvcfg;
import com.prime.dtvplayer.Sysdata.BookInfo;
import com.prime.dtvplayer.View.BookAlarmDialogView;

import java.util.ArrayList;
import java.util.List;

public class CallbackService extends Service {
    private final String TAG = "CallbackService";

    public static final String DTV_MAIN_ACTIVITY = "com.prime.dtvplayer.Activity.ViewActivity";

    public static final String PERMISSION_DTV_BROADCAST = "android.permission.DTV_BROADCAST";
    /**
        * Intent name. Used for CallbackService to show BookAlarmActivity.
        */
    public static final String DTV_BOOK_ALARM_REMIND = "com.prime.alarm.remind";
    public static final String DTV_BOOK_ALARM_REMIND_PLAY = "com.prime.book.alarm.remindplayer";
    /**
        * Intent name. Used for CallbackService to start book task.
        */
    public static final String DTV_BOOK_ALARM_ARRIVE = "com.prime.book.alarm.arrive";
    public static final String DTV_BOOK_ALARM_ARRIVE_PLAY = "com.prime.book.alarm.arriveplayer";
    /**
     * Intent name, Used for CallbackService to stop book task
     * */
    public static final String DTV_BOOK_ALARM_END = "com.prime.book.end";//Scoty 20180615 fixed timer not stop
    /**
        * Key name. Used for CallbackService to pass bundle..
        */
    public static final String DTV_BOOK_ID = "Id";
    public static final String DTV_BOOK_DURATION = "Duration";
    public static final String DTV_BOOK_CHANNEL_ID = "ChannelId";
    public static final String DTV_BOOK_REC_ID = "RecId";//Scoty 20180720 add timer end rec id
    public static final String DTV_BOOK_TYPE = "Type";

    private BookInfo mBookTask = null;
    private int mDuration = 0;
    private int mType = 0;
    private CountDownTimer BroadCastTimer = null ;

    // for VMX need open/close -s
    // ======== VMX =========
    public static final String DTV_VMX_TRIGGER_ID = "vmx_trigger_id";
    public static final String DTV_VMX_TRIGGER_NUM = "vmx_trigger_num";

    public static final String DTV_VMX_OTA_START = "com.prime.vmx.otastart";
    public static final String DTV_VMX_OTA_START_PLAY = "com.prime.vmx.otastartplayer";
    public static final String DTV_VMX_OTA_MODE = "vmx_ota_mode";
    public static final String DTV_VMX_OTA_FREQ_NUM = "vmx_ota_freq_num";
    public static final String DTV_VMX_OTA_FREQ_PARAM = "vmx_ota_freq_param";
    public static final String DTV_VMX_OTA_BANDWIDTH_PARAM = "vmx_ota_bandwidth_param";

    public static final String DTV_VMX_OTA_PROC = "com.prime.vmx.otaproc";
    public static final String DTV_VMX_OTA_PROC_ERR = "vmx_ota_proc_err";
    public static final String DTV_VMX_OTA_PROC_SCHEDULE = "vmx_ota_proc_schedule";

    public static final String DTV_VMX_WATERMARK = "com.prime.vmx.watermark";
    public static final String DTV_VMX_WATERMARK_MODE = "vmx_watermark_mode";
    public static final String DTV_VMX_WATERMARK_DUR = "vmx_watermark_dur";
    public static final String DTV_VMX_WATERMARK_MSG = "vmx_watermark_msg";
    public static final String DTV_VMX_WATERMARK_FRAMEX = "vmx_watermark_framex";
    public static final String DTV_VMX_WATERMARK_FRAMEY = "vmx_watermark_framey";

    public static final String DTV_VMX_SEARCH = "com.prime.vmx.search";
    public static final String DTV_VMX_SEARCH_PLAY = "com.prime.vmx.searchplayer";
    public static final String DTV_VMX_SEARCH_MODE = "vmx_search_mode";
    public static final String DTV_VMX_SEARCH_START_FREQ = "vmx_search_startfreq";
    public static final String DTV_VMX_SEARCH_END_FREQ = "vmx_search_endfreq";

    public static final String DTV_VMX_SHOW_MSG = "com.prime.vmx.showmsg";
    public static final String DTV_VMX_SHOW_MSG_MODE = "vmx_showmsg_mode";
    public static final String DTV_VMX_SHOW_MSG_DUR = "vmx_showmsg_dur";
    public static final String DTV_VMX_SHOW_MSG_MSG = "vmx_showmsg_msg";

    public static final String DTV_VMX_E16 = "com.prime.vmx.e16";
    public static final String DTV_VMX_E16_ENABLE = "vmx_e16_enable";

    public static final String DTV_VMX_EWBS = "com.prime.vmx.ewbs";
    public static final String DTV_VMX_EWBS_PLAY = "com.prime.vmx.ewbsplayer";
    public static final String DTV_VMX_EWBS_STOP_PLAY = "com.prime.vmx.stop.ewbsplayer";//Scoty 20181225 modify VMX EWBS rule
    public static final String DTV_VMX_EWBS_START_END = "vmx_ewbs_start_end";
    public static final String DTV_VMX_EWBS_SIGNAL_LEVEL = "vmx_ewbs_signal_level";
    public static final String DTV_VNX_EWBS_MODE = "vmx_ewbs_mode";//Scoty 20181218 add VMX EWBS mode
    public static final String DTV_VMX_EWBS_CHANNEL_ID = "vmx_ewbs_channel_id";//Scoty 20181225 modify VMX EWBS rule

    public static final String DTV_VMX_FACTORY = "com.prime.vmx.factory";
    public static final String DTV_VMX_FACTORY_PLAY = "com.prime.vmx.factoryplayer";

    public static final String DTV_VMX_CHBLOCK = "com.prime.vmx.chblock";
    public static final String DTV_VMX_CHBLOCK_MODE = "com.prime.vmx.chblock_mode";

    public static final String DTV_VMX_MAIL = "com.prime.vmx.mail";
    public static final String DTV_VMX_MAIL_MSG = "com.prime.vmx.mail_msg";
    public static final String DTV_VMX_MAIL_TYPE = "com.prime.vmx.mail_mode";
    //===============
    // for VMX need open/close -e

    // DTV class
    private HiDtvMediaPlayer mDtv = null;

    // ====== Book Receiver =====
    private BroadcastReceiver mBookAlarmReceiver = null;
    private BroadcastReceiver mBookArriveReceiver = null;

    // for VMX need open/close -s
    //======= VMX Receiver =====
    private BroadcastReceiver mVmxOtaStartReceiver = null;
    private BroadcastReceiver mVmxSearchReceiver = null;
    private BroadcastReceiver mVmxEWBSReceiver = null;
    private BroadcastReceiver mVmxFactoryReceiver = null;
    // for VMX need open/close -e

    public CallbackService() {
    }

    IDTVListener mDTVListener = new IDTVListener()
    {
        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj)
        {
            Log.d(TAG, "notifyMessage: "+ messageID + "," + param1 + "," + param2 + "," + obj.toString());
            switch (messageID)
            {
                case DTVMessage.HI_SVR_EVT_BOOK_REMIND:
                {
                    Log.d(TAG, "notifyMessage: HI_SVR_EVT_BOOK_REMIND");
                    /*
                    Intent bookAlarmIntent = new Intent(DTV_BOOK_ALARM_REMIND);
                    Bundle bundle = new Bundle();
                    bundle.putInt(DTV_BOOK_ID, param1);
                    bookAlarmIntent.putExtras(bundle);
                    sendBroadcast(bookAlarmIntent, PERMISSION_DTV_BROADCAST);
                    */


                    final BookAlarmDialogView tmpBookAlarmDialog = new BookAlarmDialogView(getApplicationContext(), param1);
                    tmpBookAlarmDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    new android.os.Handler().postDelayed(new Runnable() { // Edwin 20190509 fix dialog not focus
                        @Override
                        public void run () {
                            tmpBookAlarmDialog.show();
                        }
                    }, 150);


//                    sendBroadcastAsUser(bookAlarmIntent, UserHandle.ALL, PERMISSION_DTV_BROADCAST);   // require system permission
//                    CommonDef.sendBroadcastEx(DTVService.this, bookAlarmIntent);
                    break;
                }
                case DTVMessage.HI_SVR_EVT_BOOK_TIME_ARRIVE:
                {
                    Log.d(TAG, "notifyMessage: HI_SVR_EVT_BOOK_TIME_ARRIVE");
                    mBookTask = mDtv.GetTaskByIdFromUIBookList(param1);
                    mDuration = mBookTask.getDuration();
                    mType = mBookTask.getBookType();

                    int bookId = param1;
                    long channelID = mBookTask.getChannelId();
                    int duration = mDuration;
                    int type = mType;
                    //Scoty 20180720 remove start record wait 5 secs Dialog
                    startBookTask(bookId);
                    startSendBookBroadcast(bookId,channelID); // connie 20180813 fix timer not work when timer arrive but screen not in  DTVPlayer APK

                    // for VMX need open/close -s
                    if( Pvcfg.getCAType() == Pvcfg.CA_VMX) {
                        Register_VMX_OTA_Start_Receiver();
                        Register_VMX_Search_Receiver();
                        Register_VMX_EWBS_Receiver();
                        Register_VMX_Factory_Receiver();
                    }
                    // for VMX need open/close -e
/*
                    Intent bookArriveIntent = new Intent(DTV_BOOK_ALARM_ARRIVE);
                    Bundle bundle = new Bundle();
                    bundle.putInt(DTV_BOOK_ID, param1);
                    bundle.putLong(DTV_BOOK_CHANNEL_ID, mBookTask.getChannelId());
                    bundle.putInt(DTV_BOOK_DURATION, mDuration);
                    bundle.putInt(DTV_BOOK_TYPE, mType);
                    bookArriveIntent.putExtras(bundle);
                    sendBroadcast(bookArriveIntent, PERMISSION_DTV_BROADCAST);
//                    sendBroadcastAsUser(bookArriveIntent, UserHandle.ALL, PERMISSION_DTV_BROADCAST);  // require system permission
//                    CommonDef.sendBroadcastEx(DTVService.this, bookArriveIntent);
*/
                    break;
                }
                //Scoty 20180615 fixed timer not stop -s
                case DTVMessage.HI_SVR_EVT_BOOK_TIME_END: // not use here, for test
                {
                    Log.d(TAG, "notifyMessage: HI_SVR_EVT_BOOK_TIME_END bookId = " + param1 + " recId = " + param2);
                    Intent bookEndIntent = new Intent(DTV_BOOK_ALARM_END);
                    //Scoty 20180720 add timer end rec id -s
                    Bundle bundle = new Bundle();
                    bundle.putInt(DTV_BOOK_ID, param1);
                    bundle.putInt(DTV_BOOK_REC_ID,param2);
                    bookEndIntent.putExtras(bundle);
                    //Scoty 20180720 add timer end rec id -e
                    sendBroadcast(bookEndIntent, PERMISSION_DTV_BROADCAST);
                    break;
                }
                //Scoty 20180615 fixed timer not stop -e

                // for VMX need open/close -s
                case DTVMessage.HI_SVR_EVT_VMX_OTA_START:
                {
                    Log.d(TAG, "notifyMessage: HI_SVR_EVT_VMX_OTA_START");

                    List<Integer> freqList= new ArrayList<Integer>();
                    List<Integer> bandwidthList= new ArrayList<Integer>();
                    int triggerNum =0, freqNum = 0, freq = 0;
                    if(obj != null) {
                        triggerNum = ((Parcel) obj).readInt();
                        freqNum = ((Parcel) obj).readInt();
                        for( int i = 0; i < freqNum; i++)
                        {
                            freqList.add(((Parcel) obj).readInt());
                            bandwidthList.add(((Parcel) obj).readInt());
                        }
                    }
                    Intent otaStartIntent = new Intent(DTV_VMX_OTA_START);
                    Bundle bundle = new Bundle();
                    bundle.putInt(DTV_VMX_OTA_MODE, param1);
                    bundle.putInt(DTV_VMX_TRIGGER_ID, param2);
                    bundle.putInt(DTV_VMX_TRIGGER_NUM, triggerNum);
                    bundle.putInt(DTV_VMX_OTA_FREQ_NUM, freqNum);
                    bundle.putIntegerArrayList(DTV_VMX_OTA_FREQ_PARAM, (ArrayList<Integer>) freqList);
                    bundle.putIntegerArrayList(DTV_VMX_OTA_BANDWIDTH_PARAM, (ArrayList<Integer>) bandwidthList);
                    Log.d(TAG, "notifyMessage: HI_SVR_EVT_VMX_OTA_START   otaMode = " + param1 + "    triggerID = " + param2 + "   triggerNum = " + triggerNum + "   freqNum =" + freqNum );
                    for( int i = 0; i < freqNum; i++)//Scoty 20181207 modify VMX OTA rule
                    {
                        Log.d(TAG, "notifyMessage:  i =" + i + "    freq = " + freqList.get(i) + "    bandwidth = " + bandwidthList.get(i));
                    }

                    otaStartIntent.putExtras(bundle);
                    sendBroadcast(otaStartIntent, PERMISSION_DTV_BROADCAST);
                }break;

                case DTVMessage.HI_SVR_EVT_VMX_OTA_PROC:
                {
                    Log.d(TAG, "notifyMessage: HI_SVR_EVT_VMX_OTA_PROC");

                    Intent otaPorcIntent = new Intent(DTV_VMX_OTA_PROC);
                    Bundle bundle = new Bundle();
                    bundle.putInt(DTV_VMX_OTA_PROC_ERR, param1);
                    bundle.putInt(DTV_VMX_OTA_PROC_SCHEDULE, param2);
                    sendBroadcast(otaPorcIntent, PERMISSION_DTV_BROADCAST);
                }break;

                case DTVMessage.HI_SVR_EVT_VMX_WATERMARK:
                {
                    Log.d(TAG, "notifyMessage: HI_SVR_EVT_VMX_WATERMARK");
                    int mode, duration, frameX =0, frameY = 0, triggerID = 0, triggerNum = 0;
                    String msg = "";

                    ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.RunningTaskInfo> mRunningTasks = activityManager != null ? activityManager.getRunningTasks(1) : null;
                    String runningActivity = "";

                    if (mRunningTasks != null)
                    {
                        runningActivity = mRunningTasks.get(0).topActivity.getClassName();
                        Log.d(TAG, "notifyMessage:    runningActivity = " + runningActivity);
                        if (runningActivity.indexOf("com.prime.dtvplayer.Activity") == -1)
                            break;
                    }

                    mode = param1;
                    duration = param2;
                    if(obj != null) {
                        msg = ((Parcel) obj).readString();
                        frameX = ((Parcel) obj).readInt();
                        frameY = ((Parcel) obj).readInt();
                        triggerID = ((Parcel) obj).readInt();
                        triggerNum = ((Parcel) obj).readInt();
                    }
                    Log.d(TAG, "notifyMessage:   mode = " + param1 + "    duration = " + param2 + "    msg=" + msg + "  frameX="+frameX + "    frameY = " + frameY + "  triggerID="+triggerID + "   triggerNum ="+ triggerNum);
                    Intent waterarkIntent = new Intent(DTV_VMX_WATERMARK);
                    Bundle bundle = new Bundle();
                    bundle.putInt(DTV_VMX_WATERMARK_MODE, mode);
                    bundle.putInt(DTV_VMX_WATERMARK_DUR, duration);
                    bundle.putString(DTV_VMX_WATERMARK_MSG, msg);
                    bundle.putInt(DTV_VMX_WATERMARK_FRAMEX, frameX);
                    bundle.putInt(DTV_VMX_WATERMARK_FRAMEY, frameY);
                    bundle.putInt(DTV_VMX_TRIGGER_ID, triggerID);
                    bundle.putInt(DTV_VMX_TRIGGER_NUM, triggerNum);
                    waterarkIntent.putExtras(bundle);
                    sendBroadcast(waterarkIntent, PERMISSION_DTV_BROADCAST);
                }break;

                case DTVMessage.HI_SVR_EVT_VMX_SHOW_MSG:
                {
                    Log.d(TAG, "notifyMessage: HI_SVR_EVT_VMX_SHOW_MSG");
                    int mode, duration, triggerID = 0, triggerNum = 0;
                    String msg = "";

                    ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.RunningTaskInfo> mRunningTasks = activityManager != null ? activityManager.getRunningTasks(1) : null;
                    String runningActivity = "";

                    if (mRunningTasks != null)
                    {
                        runningActivity = mRunningTasks.get(0).topActivity.getClassName();
                        Log.d(TAG, "notifyMessage:    runningActivity = " + runningActivity);
                        if (runningActivity.indexOf("com.prime.dtvplayer.Activity") == -1)
                            break;
                    }

                    mode = param1;
                    duration = param2;
                    if(obj != null) {
                        msg = ((Parcel) obj).readString();
                        //triggerID = ((Parcel) obj).readInt();
                        //triggerNum = ((Parcel) obj).readInt();
                    }
                    Log.d(TAG, "notifyMessage:   param1 = " + param1 + "    param2 = " + param2 + "    CAmsg=" + msg + "    triggerID = " + triggerID + "   triggerNum =" + triggerNum);
                    Intent showMsgIntent = new Intent(DTV_VMX_SHOW_MSG);
                    Bundle bundle = new Bundle();
                    bundle.putInt(DTV_VMX_SHOW_MSG_MODE, mode);
                    bundle.putInt(DTV_VMX_SHOW_MSG_DUR, duration);
                    bundle.putString(DTV_VMX_SHOW_MSG_MSG, msg);
                    //bundle.putInt(DTV_VMX_TRIGGER_ID, triggerID);
                    //bundle.putInt(DTV_VMX_TRIGGER_NUM, triggerNum);
                    showMsgIntent.putExtras(bundle);
                    sendBroadcast(showMsgIntent, PERMISSION_DTV_BROADCAST);
                }break;

                case DTVMessage.HI_SVR_EVT_VMX_E16: // connie 20180925
                {
                    Log.d(TAG, "notifyMessage: HI_SVR_EVT_VMX_E16");
                    int enable = 0;

                    ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.RunningTaskInfo> mRunningTasks = activityManager != null ? activityManager.getRunningTasks(1) : null;
                    String runningActivity = "";
                    if (mRunningTasks != null)
                    {
                        runningActivity = mRunningTasks.get(0).topActivity.getClassName();
                        Log.d(TAG, "notifyMessage:    runningActivity = " + runningActivity);
                        if (runningActivity.indexOf("com.prime.dtvplayer.Activity") == -1)
                            break;
                    }

                    enable = param1;

                    Log.d(TAG, "notifyMessage:   enable = " + enable );
                    Intent E16MsgIntent = new Intent(DTV_VMX_E16);
                    Bundle bundle = new Bundle();
                    bundle.putInt(DTV_VMX_E16_ENABLE, enable);
                    E16MsgIntent.putExtras(bundle);
                    sendBroadcast(E16MsgIntent, PERMISSION_DTV_BROADCAST);
                }break;


                case DTVMessage.HI_SVR_EVT_VMX_SEARCH:
                {
                    Log.d(TAG, "notifyMessage: HI_SVR_EVT_VMX_SEARCH");
                    int mode = param1;
                    int startFreq = param2;
                    int endFreq = 0, triggerID = 0, triggerNum = 0;
                    if(obj != null) {
                        endFreq = ((Parcel) obj).readInt();
                        triggerID = ((Parcel) obj).readInt();
                        triggerNum = ((Parcel) obj).readInt();
                    }
                    Log.d(TAG, "notifyMessage: HI_SVR_EVT_VMX_SEARCH   mode = " + mode + "    startFreq = " + startFreq + "    endFreq =" + endFreq + "    triggerID = " + triggerID + "    triggerNum" + triggerNum);
                    Intent searchIntent = new Intent(DTV_VMX_SEARCH);
                    Bundle bundle = new Bundle();
                    bundle.putInt(DTV_VMX_SEARCH_MODE, mode);
                    bundle.putInt(DTV_VMX_SEARCH_START_FREQ, startFreq);
                    bundle.putInt(DTV_VMX_SEARCH_END_FREQ, endFreq);
                    bundle.putInt(DTV_VMX_TRIGGER_ID, triggerID);
                    bundle.putInt(DTV_VMX_TRIGGER_NUM, triggerNum);
                    searchIntent.putExtras(bundle);
                    sendBroadcast(searchIntent, PERMISSION_DTV_BROADCAST);
                }break;

                case DTVMessage.HI_SVR_EVT_VMX_MAIL:
                {
                    Log.d(TAG, "notifyMessage: HI_SVR_EVT_VMX_MAIL");
                    String MailMsg = "";
                    int triggerID = 0, triggerNum = 0;
                    int MailType = param1;
                    triggerID =param2;
                    if(obj != null) {
                        MailMsg = ((Parcel) obj).readString(); // msg
                        triggerNum = ((Parcel) obj).readInt();
                    }

                    Log.d(TAG, "notifyMessage:  MailMsg = " + MailMsg + "    MailType =" + MailType + "    triggerID="+ triggerID+ "    triggerNum =" + triggerNum) ;
                    Intent showMsgIntent = new Intent(DTV_VMX_MAIL);
                    Bundle bundle = new Bundle();
                    bundle.putString(DTV_VMX_MAIL_MSG, MailMsg);
                    bundle.putInt(DTV_VMX_MAIL_TYPE, MailType);
                    bundle.putInt(DTV_VMX_TRIGGER_ID, triggerID);
                    bundle.putInt(DTV_VMX_TRIGGER_NUM, triggerNum);

                    showMsgIntent.putExtras(bundle);
                    sendBroadcast(showMsgIntent, PERMISSION_DTV_BROADCAST);
                }break;

                case DTVMessage.HI_SVR_EVT_EWBS:
                {
//Scoty 20181218 add VMX EWBS mode -s
                    int ewbs_mode = param1;
                    int start_end = param2;
                    int signal_leval = ((Parcel) obj).readInt();
                    int channelId = ((Parcel) obj).readInt();//Scoty 20181225 modify VMX EWBS rule
                    Log.d(TAG, "notifyMessage: HI_SVR_EVT_EWBS   ewbs_mode = " + ewbs_mode +" start_end = " + start_end + "     signal_leval =" + signal_leval );

                    Intent ewbxIntent = new Intent(DTV_VMX_EWBS);
                    Bundle bundle = new Bundle();
                    bundle.putInt(DTV_VNX_EWBS_MODE,ewbs_mode);
                    bundle.putInt(DTV_VMX_EWBS_START_END, start_end);
                    bundle.putInt(DTV_VMX_EWBS_SIGNAL_LEVEL, signal_leval);
                    bundle.putInt(DTV_VMX_EWBS_CHANNEL_ID, channelId);//Scoty 20181225 modify VMX EWBS rule
                    ewbxIntent.putExtras(bundle);
                    sendBroadcast(ewbxIntent, PERMISSION_DTV_BROADCAST);
//Scoty 20181218 add VMX EWBS mode -e
                }break;

                case DTVMessage.HI_SVR_EVT_VMX_FACTORY: {
                    int triggerID = param1;
                    int triggerNum = param2;
                    Log.d(TAG, "notifyMessage: HI_SVR_EVT_VMX_FACTORY   triggerID = " + triggerID + "    triggerNum =" + triggerNum );

                    Intent factoryIntent = new Intent(DTV_VMX_FACTORY);
                    Bundle bundle = new Bundle();
                    bundle.putInt(DTV_VMX_TRIGGER_ID, triggerID);
                    bundle.putInt(DTV_VMX_TRIGGER_NUM, triggerNum);
                    factoryIntent.putExtras(bundle);
                    sendBroadcast(factoryIntent, PERMISSION_DTV_BROADCAST);
                }break;

                case DTVMessage.HI_SVR_EVT_VMX_CHBLOCK:
                {
                    int mode = param1;
                    int triggerID = param2;
                    int triggerNum = 0;
                    if(obj != null)
                        triggerNum = ((Parcel) obj).readInt();
                    Log.d(TAG, "notifyMessage: HI_SVR_EVT_VMX_CHBLOCK  mode = " + mode +" triggerID = " + triggerID + "    triggerNum =" + triggerNum );
                    Intent chBlockIntent = new Intent(DTV_VMX_CHBLOCK);
                    Bundle bundle = new Bundle();
                    bundle.putInt(DTV_VMX_CHBLOCK_MODE, mode);
                    bundle.putInt(DTV_VMX_TRIGGER_ID, triggerID);
                    bundle.putInt(DTV_VMX_TRIGGER_NUM, triggerNum);
                    chBlockIntent.putExtras(bundle);
                    sendBroadcast(chBlockIntent, PERMISSION_DTV_BROADCAST);
                }break;
                case DTVMessage.HI_SVR_EVT_VMX_EWBS_STOP: {//Scoty 20181225 modify VMX EWBS rule
                    Intent stopewbsIntent = new Intent(DTV_VMX_EWBS_STOP_PLAY);
                    sendBroadcast(stopewbsIntent, PERMISSION_DTV_BROADCAST);
                }break;
                // for VMX need open/close -e
                default:
                {
                    break;
                }
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        Log.d(TAG, "onCreate: ");
        super.onCreate();

        // ========  Book Event =========
        mDtv = HiDtvMediaPlayer.getInstance();
        mDtv.subScribeEvent(DTVMessage.HI_SVR_EVT_BOOK_REMIND, mDTVListener, 0);
        mDtv.subScribeEvent(DTVMessage.HI_SVR_EVT_BOOK_TIME_ARRIVE, mDTVListener, 0);
        mDtv.subScribeEvent(DTVMessage.HI_SVR_EVT_BOOK_TIME_END, mDTVListener, 0);

        // for VMX need open/close -s
        if( Pvcfg.getCAType() == Pvcfg.CA_VMX) {
            mDtv.subScribeEvent(DTVMessage.HI_SVR_EVT_VMX_OTA_START, mDTVListener, 0);
            mDtv.subScribeEvent(DTVMessage.HI_SVR_EVT_VMX_WATERMARK, mDTVListener, 0);
            mDtv.subScribeEvent(DTVMessage.HI_SVR_EVT_VMX_SEARCH, mDTVListener, 0);
            mDtv.subScribeEvent(DTVMessage.HI_SVR_EVT_VMX_SHOW_MSG, mDTVListener, 0);
            mDtv.subScribeEvent(DTVMessage.HI_SVR_EVT_VMX_E16, mDTVListener, 0);
            mDtv.subScribeEvent(DTVMessage.HI_SVR_EVT_VMX_MAIL, mDTVListener, 0);
            mDtv.subScribeEvent(DTVMessage.HI_SVR_EVT_EWBS, mDTVListener, 0);
            mDtv.subScribeEvent(DTVMessage.HI_SVR_EVT_VMX_FACTORY, mDTVListener, 0);
            mDtv.subScribeEvent(DTVMessage.HI_SVR_EVT_VMX_CHBLOCK, mDTVListener, 0);
        }
        // for VMX need open/close -e

        IntentFilter bookAlarmIntentFilter = new IntentFilter(DTV_BOOK_ALARM_REMIND);
        mBookAlarmReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.d(TAG, "onReceive: Start Book Remind Alarm Activity " + intent.toString());
                Bundle bundle = intent.getExtras();
                if (bundle == null)
                {
                    return;
                }

                int id = bundle.getInt(DTV_BOOK_ID, 0);
//                int id = (Integer) bundle.get(DTV_BOOK_ID);
                Log.d(TAG, "onReceive: id = " + id);

                final BookAlarmDialogView tmpBookAlarmDialog = new BookAlarmDialogView(context, id);
                tmpBookAlarmDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                new android.os.Handler().postDelayed(new Runnable() { // Edwin 20190509 fix dialog not focus
                    @Override
                    public void run () {
                        tmpBookAlarmDialog.show();
                    }
                }, 150);
            }
        };
        registerReceiver(mBookAlarmReceiver, bookAlarmIntentFilter, PERMISSION_DTV_BROADCAST, null);

        IntentFilter bookArriveIntentFilter = new IntentFilter(DTV_BOOK_ALARM_ARRIVE);
        mBookArriveReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.d(TAG, "onReceive: Start Book Arrive Activity " + intent.toString());
                Bundle bundle = intent.getExtras();
                if (bundle == null)
                {
                    return;
                }
                int bookId = bundle.getInt(DTV_BOOK_ID);
                long channelID = bundle.getLong(DTV_BOOK_CHANNEL_ID);
                int duration = bundle.getInt(DTV_BOOK_DURATION);
                int type = bundle.getInt(DTV_BOOK_TYPE);
                //Scoty 20180720 remove start record wait 5 secs Dialog
                startBookTask(bookId);
                startSendBookBroadcast(bookId,channelID); // connie 20180813 fix timer not work when timer arrive but screen not in  DTVPlayer APK
                //sendBookBroadCast(bookId,channelID);

                //BookArriveDialogView tmpBookArriveDialog = new BookArriveDialogView(context, duration, channelID, type, bookId);
                //tmpBookArriveDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                //tmpBookArriveDialog.show();
            }
        };
        registerReceiver(mBookArriveReceiver, bookArriveIntentFilter, PERMISSION_DTV_BROADCAST, null);
        // for VMX need open/close -s
        if( Pvcfg.getCAType() == Pvcfg.CA_VMX) {
            Register_VMX_OTA_Start_Receiver();
            Register_VMX_Search_Receiver();
            Register_VMX_EWBS_Receiver();
            Register_VMX_Factory_Receiver();
        }
        // for VMX need open/close -e
    }
//Scoty 20180720 remove start record wait 5 secs Dialog -s
    private void sendBookBroadCast(int id, long channelID)
    {
        Intent bookArriveIntent = new Intent();
        bookArriveIntent.setAction(CallbackService.DTV_BOOK_ALARM_ARRIVE_PLAY);
        Bundle bundle = new Bundle();
        bundle.putInt(CallbackService.DTV_BOOK_ID, id);
        bundle.putLong(CallbackService.DTV_BOOK_CHANNEL_ID, channelID);
        bundle.putInt(CallbackService.DTV_BOOK_DURATION, mDuration);
        bundle.putInt(CallbackService.DTV_BOOK_TYPE, mType);
        bookArriveIntent.putExtras(bundle);
        sendBroadcast(bookArriveIntent, CallbackService.PERMISSION_DTV_BROADCAST);
    }

    private void startBookTask(int bookId)
    {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> mRunningTasks = activityManager != null ? activityManager.getRunningTasks(1) : null;
        String runningActivity = "";

        if (mRunningTasks != null)
        {
            runningActivity = mRunningTasks.get(0).topActivity.getClassName();
        }

        Log.d(TAG, "startBookTask  runningActivity = " + runningActivity);
        if (!runningActivity.equals(CallbackService.DTV_MAIN_ACTIVITY))
        {
            // Jump to DTVPlayerActivity
            Intent dtvPlayerIntent = new Intent();
            dtvPlayerIntent.setClass(CallbackService.this, ViewActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(CallbackService.DTV_BOOK_ALARM_ARRIVE_PLAY, bookId);
            dtvPlayerIntent.putExtras(bundle);

            startActivity(dtvPlayerIntent);
//                CommonDef.startActivityEx(mContext, dtvPlayerIntent);
            // Send book task broadcast
        }
    }
//Scoty 20180720 remove start record wait 5 secs Dialog -e
    @Override
    public void onDestroy()
    {
        //  ***    Book  ***
        if (null != mBookAlarmReceiver)
        {
            this.unregisterReceiver(mBookAlarmReceiver);
            mBookAlarmReceiver = null;
        }

        if (null != mBookArriveReceiver)
        {
            this.unregisterReceiver(mBookArriveReceiver);
            mBookArriveReceiver = null;
        }

        // for VMX need open/close -s
        if( Pvcfg.getCAType() == Pvcfg.CA_VMX) {
            if (null != mVmxOtaStartReceiver) {
                this.unregisterReceiver(mVmxOtaStartReceiver);
                mVmxOtaStartReceiver = null;
            }

            if (null != mVmxSearchReceiver) {
                this.unregisterReceiver(mVmxSearchReceiver);
                mVmxSearchReceiver = null;
            }

            if (null != mVmxEWBSReceiver) {
                this.unregisterReceiver(mVmxEWBSReceiver);
                mVmxEWBSReceiver = null;
            }

            if (null != mVmxFactoryReceiver) {
                this.unregisterReceiver(mVmxFactoryReceiver);
                mVmxFactoryReceiver = null;
            }
        }
        // for VMX need open/close -e

        //  ***    Book  ***
        mDtv.unSubScribeEvent(DTVMessage.HI_SVR_EVT_BOOK_REMIND, mDTVListener);
        mDtv.unSubScribeEvent(DTVMessage.HI_SVR_EVT_BOOK_TIME_ARRIVE, mDTVListener);
        mDtv.unSubScribeEvent(DTVMessage.HI_SVR_EVT_BOOK_TIME_END, mDTVListener);

        // for VMX need open/close -s
        if( Pvcfg.getCAType() == Pvcfg.CA_VMX) {
            mDtv.unSubScribeEvent(DTVMessage.HI_SVR_EVT_VMX_WATERMARK, mDTVListener);
            mDtv.unSubScribeEvent(DTVMessage.HI_SVR_EVT_VMX_OTA_START, mDTVListener);
            mDtv.unSubScribeEvent(DTVMessage.HI_SVR_EVT_VMX_SEARCH, mDTVListener);
            mDtv.unSubScribeEvent(DTVMessage.HI_SVR_EVT_VMX_SHOW_MSG, mDTVListener);
            mDtv.unSubScribeEvent(DTVMessage.HI_SVR_EVT_VMX_E16, mDTVListener);
            mDtv.unSubScribeEvent(DTVMessage.HI_SVR_EVT_VMX_MAIL, mDTVListener);
            mDtv.unSubScribeEvent(DTVMessage.HI_SVR_EVT_EWBS, mDTVListener);
            mDtv.unSubScribeEvent(DTVMessage.HI_SVR_EVT_VMX_FACTORY, mDTVListener);
            mDtv.unSubScribeEvent(DTVMessage.HI_SVR_EVT_VMX_CHBLOCK, mDTVListener);
        }
        // for VMX need open/close -e
        super.onDestroy();
    }

    /*@Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent == null)
        {
            return -1;
        }
        return super.onStartCommand(intent, flags, startId);
    }*/

    private void startSendBookBroadcast(final int bookId, final long channelID) // connie 20180813 fix timer not work when timer arrive but screen not in  DTVPlayer APK
    {
        BroadCastTimer = new CountDownTimer( 100*5, 100)
        {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                sendBookBroadCast(bookId,channelID);
            }
        };
        BroadCastTimer.start();

    }

    // for VMX need open/close -s
    public void Register_VMX_OTA_Start_Receiver()
    {
        IntentFilter otaIntentFilter = new IntentFilter(DTV_VMX_OTA_START);
        mVmxOtaStartReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.d(TAG, "onReceive: VMX OTA Start  " + intent.toString());
                Bundle bundle = intent.getExtras();
                if (bundle == null)
                    return;

                int otaMode = bundle.getInt(DTV_VMX_OTA_MODE, 0);
                int triggerID = bundle.getInt(DTV_VMX_TRIGGER_ID, 0);
                int triggerNum = bundle.getInt(DTV_VMX_TRIGGER_NUM, 0);
                int otaFreqNum = bundle.getInt(DTV_VMX_OTA_FREQ_NUM, 0);
                ArrayList<Integer> freqList = bundle.getIntegerArrayList(DTV_VMX_OTA_FREQ_PARAM);//Scoty 20181207 modify VMX OTA rule
                ArrayList<Integer> bandwidthList= bundle.getIntegerArrayList(DTV_VMX_OTA_BANDWIDTH_PARAM);//Scoty 20181207 modify VMX OTA rule

                Log.d(TAG, "onReceive: VMX OTA Mode = " + otaMode + "   triggerID="+ triggerID + "   triggerNum="+triggerNum + "   otaFreqNum = " + otaFreqNum);
                if(otaMode == 0)
                    GoToNormalView();
                startSendOTABroadcast(otaMode, triggerID, triggerNum, otaFreqNum, freqList, bandwidthList);

            }
        };
        registerReceiver(mVmxOtaStartReceiver, otaIntentFilter, PERMISSION_DTV_BROADCAST, null);
    }

    private void GoToNormalView()
    {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> mRunningTasks = activityManager != null ? activityManager.getRunningTasks(1) : null;
        String runningActivity = "";

        if (mRunningTasks != null)
        {
            runningActivity = mRunningTasks.get(0).topActivity.getClassName();
        }

        Log.d(TAG, "startOTATask  runningActivity = " + runningActivity);
        if (!runningActivity.equals(CallbackService.DTV_MAIN_ACTIVITY))
        {
            // Jump to DTVPlayerActivity
            Intent dtvPlayerIntent = new Intent();
            dtvPlayerIntent.setClass(CallbackService.this, ViewActivity.class);
            startActivity(dtvPlayerIntent);
        }
    }
    //Scoty 20181207 modify VMX OTA rule
    private void startSendOTABroadcast(final int mode, final int triggerID, final int triggerNum, final int otaFreqNum, final ArrayList<Integer> freqList,  final ArrayList<Integer> bandwidthList)
    {
        BroadCastTimer = new CountDownTimer( 100*5, 100)
        {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                Intent OTAIntent = new Intent();
                OTAIntent.setAction(CallbackService.DTV_VMX_OTA_START_PLAY);
                Bundle bundle = new Bundle();
                bundle.putInt(CallbackService.DTV_VMX_OTA_MODE, mode);
                bundle.putInt(CallbackService.DTV_VMX_TRIGGER_ID, triggerID);
                bundle.putInt(CallbackService.DTV_VMX_TRIGGER_NUM, triggerNum);
                bundle.putInt(DTV_VMX_OTA_FREQ_NUM, otaFreqNum);
                bundle.putIntegerArrayList(CallbackService.DTV_VMX_OTA_FREQ_PARAM,freqList);//Scoty 20181207 modify VMX OTA rule
                bundle.putIntegerArrayList(CallbackService.DTV_VMX_OTA_BANDWIDTH_PARAM,bandwidthList);//Scoty 20181207 modify VMX OTA rule
                OTAIntent.putExtras(bundle);
                sendBroadcast(OTAIntent, CallbackService.PERMISSION_DTV_BROADCAST);
            }
        };
        BroadCastTimer.start();
    }

    public void Register_VMX_Search_Receiver()
    {
        IntentFilter searchIntentFilter = new IntentFilter(DTV_VMX_SEARCH);
        mVmxSearchReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.d(TAG, "onReceive: DTV_VMX_SEARCH" + intent.toString());
                Bundle bundle = intent.getExtras();
                if (bundle == null)
                    return;

                int mode = bundle.getInt(DTV_VMX_SEARCH_MODE, 0);
                int startFreq = bundle.getInt(DTV_VMX_SEARCH_START_FREQ, 0);
                int endFreq = bundle.getInt(DTV_VMX_SEARCH_END_FREQ, 0);
                Log.d(TAG, "onReceive: DTV_VMX_SEARCH_MODE = " + mode + "   startFreq = " + startFreq + "    endFreq ="+endFreq);

                GoToNormalView();
                startSendSearchBroadcast(mode, startFreq, endFreq);

            }
        };
        registerReceiver(mVmxSearchReceiver, searchIntentFilter, PERMISSION_DTV_BROADCAST, null);
    }

    private void startSendSearchBroadcast(final int mode, final int startFreq, final int endFreq)
    {
        BroadCastTimer = new CountDownTimer( 100*5, 100)
        {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                Intent SearchIntent = new Intent();
                SearchIntent.setAction(CallbackService.DTV_VMX_SEARCH_PLAY);
                Bundle bundle = new Bundle();
                bundle.putInt(CallbackService.DTV_VMX_SEARCH_MODE, mode);
                bundle.putInt(CallbackService.DTV_VMX_SEARCH_START_FREQ, startFreq);
                bundle.putInt(CallbackService.DTV_VMX_SEARCH_END_FREQ, endFreq);


                SearchIntent.putExtras(bundle);
                sendBroadcast(SearchIntent, CallbackService.PERMISSION_DTV_BROADCAST);
            }
        };
        BroadCastTimer.start();
    }

    public void Register_VMX_EWBS_Receiver()
    {
        IntentFilter ewbsIntentFilter = new IntentFilter(DTV_VMX_EWBS);
        mVmxEWBSReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.d(TAG, "onReceive: EWBS  " + intent.toString());
                Bundle bundle = intent.getExtras();
                if (bundle == null)
                    return;

                int ewbs_mode = bundle.getInt(DTV_VNX_EWBS_MODE, 0);//Scoty 20181218 add VMX EWBS mode
                int ewbs_start_end = bundle.getInt(DTV_VMX_EWBS_START_END, 0);
                int ewbs_signal_level = bundle.getInt(DTV_VMX_EWBS_SIGNAL_LEVEL, 0);
                int ewbs_channel_id = bundle.getInt(DTV_VMX_EWBS_CHANNEL_ID, 0);//Scoty 20181225 modify VMX EWBS rule
                Log.d(TAG, "onReceive: VMX EWBS ewbs_start_end = " + ewbs_start_end + "   ewbs_signal_level =" + ewbs_signal_level + " ewbs_channel_id = " + ewbs_channel_id);
                if(ewbs_start_end == 1) // 1: open   0: close
                    GoToNormalView();
                startSendEWBSBroadcast(ewbs_mode, ewbs_start_end, ewbs_signal_level,ewbs_channel_id);//Scoty 20181218 add VMX EWBS mode

            }
        };
        registerReceiver(mVmxEWBSReceiver, ewbsIntentFilter, PERMISSION_DTV_BROADCAST, null);
    }

    public void Register_VMX_Factory_Receiver()
    {
        IntentFilter factoryIntentFilter = new IntentFilter(DTV_VMX_FACTORY);
        mVmxFactoryReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.d(TAG, "onReceive: Factory  " + intent.toString());
                Bundle bundle = intent.getExtras();
                if (bundle == null)
                    return;

                int triggerID = bundle.getInt(DTV_VMX_TRIGGER_ID, 0);
                int triggerNum = bundle.getInt(DTV_VMX_TRIGGER_NUM, 0);
                Log.d(TAG, "onReceive: VMX Factory  triggerID = " + triggerID + "   triggerNum= "+triggerNum);
                GoToNormalView();
                startSendFactoryBroadcast(triggerID, triggerNum);

            }
        };
        registerReceiver(mVmxFactoryReceiver, factoryIntentFilter, PERMISSION_DTV_BROADCAST, null);
    }


    private void startSendEWBSBroadcast(final int ewbs_mode, final int ewbs_start_end, final int ewbs_signal_level, final int ewbs_channel_id)//Scoty 20181225 modify VMX EWBS rule//Scoty 20181218 add VMX EWBS mode
    {
        BroadCastTimer = new CountDownTimer( 100*5, 100)
        {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                Intent EWBSIntent = new Intent();
                EWBSIntent.setAction(CallbackService.DTV_VMX_EWBS_PLAY);
                Bundle bundle = new Bundle();
                bundle.putInt(CallbackService.DTV_VNX_EWBS_MODE, ewbs_mode);//Scoty 20181218 add VMX EWBS mode
                bundle.putInt(CallbackService.DTV_VMX_EWBS_START_END, ewbs_start_end);
                bundle.putInt(CallbackService.DTV_VMX_EWBS_SIGNAL_LEVEL, ewbs_signal_level);
                bundle.putInt(CallbackService.DTV_VMX_EWBS_CHANNEL_ID, ewbs_channel_id);//Scoty 20181225 modify VMX EWBS rule
                EWBSIntent.putExtras(bundle);
                sendBroadcast(EWBSIntent, CallbackService.PERMISSION_DTV_BROADCAST);
            }
        };
        BroadCastTimer.start();
    }

    private void startSendFactoryBroadcast(final int triggerID, final int triggerNum)
    {
        BroadCastTimer = new CountDownTimer( 100*5, 100)
        {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                Intent factoryIntent = new Intent();
                factoryIntent.setAction(CallbackService.DTV_VMX_FACTORY_PLAY);
                Bundle bundle = new Bundle();
                bundle.putInt(CallbackService.DTV_VMX_TRIGGER_ID, triggerID);
                bundle.putInt(CallbackService.DTV_VMX_TRIGGER_NUM, triggerNum);
                factoryIntent.putExtras(bundle);
                sendBroadcast(factoryIntent, CallbackService.PERMISSION_DTV_BROADCAST);
            }
        };
        BroadCastTimer.start();
    }
    // for VMX need open/close -e
}
