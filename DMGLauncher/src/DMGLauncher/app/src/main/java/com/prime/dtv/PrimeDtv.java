package com.prime.dtv;

import static com.prime.dtv.Interface.BaseManager.getPesiDtvFrameworkInterfaceCallback;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prime.dmg.launcher.Home.LiveTV.MiniEPG;
import com.prime.dmg.launcher.Utils.Utils;
import com.prime.dtv.Interface.PesiDtvFrameworkInterface;
import com.prime.dtv.config.Pvcfg;
import com.prime.dtv.service.Player.AvCmdMiddle;
import com.prime.dtv.service.Player.CasSession;
import com.prime.dtv.service.Util.ErrorCodeUtil;
import com.prime.dtv.service.datamanager.FavGroup;
import com.prime.dtv.sysdata.AudioInfo;
import com.prime.dtv.sysdata.BookInfo;
import com.prime.dtv.sysdata.CaStatus;
import com.prime.dtv.sysdata.CasData;
import com.prime.dtv.sysdata.ChannelHistory;
import com.prime.dtv.sysdata.ChannelNode;
import com.prime.dtv.sysdata.DTVMessage;
import com.prime.dtv.sysdata.DefaultChannel;
import com.prime.dtv.sysdata.MiscDefine;
import com.prime.dtv.sysdata.PvrRecFileInfo;
import com.prime.dtv.sysdata.PvrRecIdx;
import com.prime.dtv.sysdata.PvrRecStartParam;
import com.prime.dtv.sysdata.EPGEvent;
import com.prime.dtv.sysdata.EnAudioTrackMode;
import com.prime.dtv.sysdata.EnTVRadioFilter;
import com.prime.dtv.sysdata.EnTableType;
import com.prime.dtv.sysdata.EnUseGroupType;
import com.prime.dtv.sysdata.FavGroupName;
import com.prime.dtv.sysdata.FavInfo;
import com.prime.dtv.sysdata.GposInfo;
import com.prime.dtv.sysdata.LoaderInfo;
import com.prime.dtv.sysdata.NetProgramInfo;
import com.prime.dtv.sysdata.OTACableParameters;
import com.prime.dtv.sysdata.OTATerrParameters;
import com.prime.dtv.sysdata.ProgramInfo;
import com.prime.dtv.sysdata.PvrInfo;
import com.prime.dtv.sysdata.SatInfo;
import com.prime.dtv.sysdata.SeriesInfo;
import com.prime.dtv.sysdata.SimpleChannel;
import com.prime.dtv.sysdata.SubtitleInfo;
import com.prime.dtv.sysdata.TeletextInfo;
import com.prime.dtv.sysdata.TpInfo;
import com.prime.dtv.sysdata.VMXProtectData;
import com.prime.dtv.utils.LogUtils;
import com.prime.dtv.utils.TVMessage;
import com.prime.dtv.utils.TVScanParams;
import com.prime.dtv.utils.TVTunerParams;

import java.io.RandomAccessFile;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import vendor.prime.hardware.media.V1_0.IMedia;
import vendor.prime.hardware.misc.V1_0.IMisc;

/** @noinspection ConstantValue*/
public class PrimeDtv {
    private static final String TAG = "PrimeDtv";
    private final static String RequestDTVServiceVersion = "V1.0.3 - 20241010";
    private final static int CALLBACK_EVENT_EMM = 0x01;
    private final static int CALLBACK_EVENT_ECM = 0x02;
    private final static int CALLBACK_EVENT_PRIVATE_DATA = 0x03;
    private final static int CALLBACK_EVENT_OPEN_SSESION = 0x04;
    private final static int WV_TEST = 0xF0;//0x04;
    private final static int WV_TEST_CAS_READY = 0xF1;//0x05;
    private final static int WV_TEST_SET_SESSION_ID = 0xF2;//0x06;
    private static final int ONETIME_OBTAINED_NUMBER = 1000;
    private static final boolean DEBUG_EPG_EVENT_MAP = false;

    private PesiDtvFrameworkInterface gDtvFramework = null;
    private final PrimeDtvMediaPlayer gPrimeDtvMediaPlayer;
    private final DTVCallback gDtvCallback;
    private int gAlreadySearchTpCount = 0;
    private int gTvChCount = 0;
    private int gRadioChCount = 0;
    private Context mContext = null;

    private PrimeDtv.ViewUiDisplay ViewUIDisplayManager = null;
    private PrimeDtv.EpgUiDisplay EpgUiDisplayManager = null;
    private PrimeDtv.BookManager UIBookManager = null;
    private List<BookInfo> UIBookList = null;

    public ChannelHistory ViewHistory = ChannelHistory.GetInstance();

    private IMisc mPrimeMiscService;
    private IMedia mPrimeMediaService;
    private HDDInfoManger mHDInfomanger = null;
    private static final String PRIME_BTPAIR_PACKAGE = "com.prime.btpair";
    private static final String PRIME_BTPAIR_HOOKBEGINACTIVITY = "com.prime.btpair.HookBeginActivity";
    public static final int MINOR_DEVICE_CLASS_KEYBOARD =
            Integer.parseInt("0000001000000", 2);
    public static final int MINOR_DEVICE_CLASS_REMOTE =
            Integer.parseInt("0000000001100", 2);
    public void stop_schedule_eit() {
        LogUtils.d("[Ethan] stop_schedule_eit");
        gDtvFramework.stopScheduleEit();
    }

    public interface DTVCallback {
        void onMessage(TVMessage msg);
    }

    IDTVListener g_av_listener = new IDTVListener() {
        private static final String CB_TAG = "AVListener";

        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj) {
            //Log.d(TAG, CB_TAG + " messageID = " + messageID);
            switch (messageID) {
                case DTVMessage.PESI_SVR_EVT_AV_STOP: {
                    Log.d(TAG, "PESI_SVR_EVT_AV_STOP");
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_AV_PLAY_SUCCESS: {
                    Log.d(TAG, "PESI_SVR_EVT_AV_PLAY_SUCCESS");
                    TVMessage msg = TVMessage.SetPlayMsg(1);
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_AV_PLAY_FAILURE: {
                    Log.d(TAG, "PESI_SVR_EVT_AV_PLAY_FAILURE Error Code = " + param1);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_AV_CA: {
                    Log.d(TAG, "PESI_SVR_EVT_AV_CA is " + (param1 == 1 ? "CA " : "NOT CA") + " prog");
                    if (param1 == 1) {
                        int err_priority = CaPriority.ERR_E48_52.getValue();
                        String errorCode = "E48_32";
                        TVMessage msg =
                                TVMessage.SetCAMsg(param1, errorCode, "No Signal!");
                        gDtvCallback.onMessage(msg);
                    } else {
                        TVMessage msg = TVMessage.SetCAMsg(param1, null, null);
                        gDtvCallback.onMessage(msg);
                    }
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_AV_FRONTEND_STOP: {
                    Log.d(TAG, "PESI_SVR_EVT_AV_FRONTEND_STOP");
                    //TVMessage msg = TVMessage.SetShowErrorMsg(ErrorCodeUtil.ERROR_E301,"");
                    TVMessage msg = TVMessage.SetVideoStatus(1, param1);//eric lin 20180803 no video signal
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_AV_FRONTEND_RESUME: {
                    Log.d(TAG, "PESI_SVR_EVT_AV_FRONTEND_RESUME");
                    TVMessage msg = TVMessage.SetVideoStatus(0, param1);//eric lin 20180803 no video signal
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_AV_DECODER_ERROR:{
                    TVMessage msg = TVMessage.SetDecoderError();
                    gDtvCallback.onMessage(msg);
                }break;
                case DTVMessage.PESI_SVR_EVT_AV_SET_SBUTITLE_BITMAP: {
                    //LogUtils.d("PESI_SVR_EVT_AV_SET_SBUTITLE_BITMAP");
                    TVMessage msg = TVMessage.SetSubtitleBitmap((Bitmap) obj);
                    gDtvCallback.onMessage(msg);
                }break;
                case DTVMessage.PESI_SVR_EVT_AV_SIGNAL_STAUTS: {
                    Log.d(TAG, "PESI_SVR_EVT_AV_SIGNAL_STAUTS tunerId="+param2+" signal" + (param1 == 1 ? "LOCK!!!!" : "UNLOCK!!!!"));
                    TVMessage msg = TVMessage.SetTunerLockStatus(param1);
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_AV_MOTOR_MOVING: {
                    Log.d(TAG, "PESI_SVR_EVT_AV_MOTOR_MOVING");
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_AV_MOTOR_STOP: {
                    Log.d(TAG, "PESI_SVR_EVT_AV_MOTOR_STOP");
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_AV_CHANNEL_SCRAMBLED: {
                    Log.d(TAG, "PESI_SVR_EVT_AV_CHANNEL_SCRAMBLED " + (param1 == 1 ? "audio" : (param1 == 2 ? "video" : "nothing")) + "is scramble!!!");
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_AV_CHANNEL_LOCKED: {
                    Log.d(TAG, "PESI_SVR_EVT_AV_CHANNEL_LOCKED: "
                            + " param1 = " + param1 + " param2 = " + param2);

                    int AvLockStatus = ((Parcel) obj).readInt();
                    long ChannelId = ((Parcel) obj).readInt() & 0xFFFFFFFFL;
                    int rating = ((Parcel) obj).readInt();
                    Log.d(TAG, "notifyMessage: ChannelId = " + ChannelId + " AvLockStatus = " + AvLockStatus + " rating = " + rating);
                    TVMessage msg = TVMessage.SetAVLockStatus(AvLockStatus, ChannelId, rating);
                    gDtvCallback.onMessage(msg);
                    break;
                }
                default:
                    break;
            }
        }
    };

    IDTVListener g_epg_listener = new IDTVListener() {
        private static final String CB_TAG = "EPGListener";

        @Override
        public void notifyMessage(int messageID, int param1, int parm2, Object obj) {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            long channelID = 0;
            switch (messageID) {
                case DTVMessage.PESI_SVR_EVT_EPG_PF_VERSION_CHANGED: {
                    Log.d(TAG, "PESI_SVR_EVT_EPG_PF_VERSION_CHANGED rowID = " + param1);
                    channelID = PrimeDtvMediaPlayer.get_unsigned_int(param1);
                    TVMessage msg = TVMessage.SetEPGUpdate(channelID);
                    gDtvCallback.onMessage(msg);
                    msg = TVMessage.SetEpgPFVersionChanged(channelID);
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_EPG_PF_CURR_PROGRAM_FINISH: {
                    Log.d(TAG, "PESI_SVR_EVT_EPG_PF_CURR_PROGRAM_FINISH rowID = " + param1);
                    channelID = PrimeDtvMediaPlayer.get_unsigned_int(param1);
                    TVMessage msg = TVMessage.SetEPGUpdate(channelID);
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_EPG_PF_CURR_FREQ_FINISH: {
                    Log.d(TAG, "PESI_SVR_EVT_EPG_PF_CURR_FREQ_FINISH rowID = " + param1);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_EPG_PF_ALL_FINISH: {
                    Log.d(TAG, "PESI_SVR_EVT_EPG_PF_ALL_FINISH");
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_EPG_SCH_VERSION_CHANGED: {
                    Log.d(TAG, "PESI_SVR_EVT_EPG_SCH_VERSION_CHANGED rowID = " + param1);
                    channelID = PrimeDtvMediaPlayer.get_unsigned_int(param1);
                    TVMessage msg = TVMessage.SetEPGUpdate(channelID);
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_EPG_SCH_CURR_PROGRAM_FINISH: {
                    Log.d(TAG, "PESI_SVR_EVT_EPG_SCH_CURR_PROGRAM_FINISH rowID = " + param1);
                    channelID = PrimeDtvMediaPlayer.get_unsigned_int(param1);
                    TVMessage msg = TVMessage.SetEPGUpdate(channelID);
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_EPG_SCH_CURR_FREQ_FINISH: {
                    Log.d(TAG, "PESI_SVR_EVT_EPG_SCH_CURR_FREQ_FINISH rowID = " + param1);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_EPG_SCH_ALL_FINISH: {
                    Log.d(TAG, "PESI_SVR_EVT_EPG_SCH_ALL_FINISH");
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_EPG_PARENTAL_RATING: {
                    Log.d(TAG, "PESI_SVR_EVT_EPG_PARENTAL_RATING rowID = " + param1);
                    break;
                }
                default:
                    break;
            }
        }
    };

    IDTVListener g_scan_listener = new IDTVListener() {
        private static final String CB_TAG = "ScanListener";

        @Override
        public void notifyMessage(int messageID, int param1, int parm2, Object obj) {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            switch (messageID) {
                case DTVMessage.PESI_SVR_EVT_SRCH_BEGIN: {
                    Log.d(TAG, "notifyMessage Search begin");
                    TVMessage msg = TVMessage.SetScanBegin();
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_SRCH_LOCK_START: {
//                    Log.d(TAG, " notifyMessage START lockfreq=" + param1 + " symbol = " + parm2 ); // hisi pass freq & symbol rate
                    Log.d(TAG, " notifyMessage START lockfreq=" + param1 + " tpId = " + parm2); // pesi pass freq & tpId
                    gAlreadySearchTpCount++;
                    TVMessage msg = TVMessage.SetScanTP(gAlreadySearchTpCount, parm2);
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_SRCH_LOCK_STATUS: {
                    Log.d(TAG, " notifyMessage STATUS lockstate=" + param1);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_SRCH_CUR_FREQ_TABLE_FINISH: {
                    Log.d(TAG, "notiftMessage Cur freq table finish");
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_SRCH_CUR_FREQ_INFO: {
                    Log.d(TAG, "notifyMessage FREQ_INFO freq=" + param1 + ",tpid=" + parm2);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_SRCH_CUR_SCHEDULE: {
                    int percent = param1 % 100;
                    if ((0 == percent) && (param1 > 99)) {
                        percent = 100;
                    }
                    Log.d(TAG, " notifyMessage SCHEDULE percent=" + percent);

                    TVMessage msg = TVMessage.SetScanScheduleUpdate(percent);
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_SRCH_FINISH: {
                    Log.d(TAG, "scan  freq finished" + param1 + " " + parm2);
                    TVMessage msg = TVMessage.SetScanEnd(gTvChCount, gRadioChCount);
                    gDtvCallback.onMessage(msg);
                    msg = TVMessage.SetAddMusicCategoryToFav();
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_SRCH_ONE_FREQ_FINISH: {
                    Log.d(TAG, "scan one freq finished" + param1 + " " + parm2);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_SRCH_GET_PROG: {
                    long channelId = PrimeDtvMediaPlayer.get_unsigned_int(parm2);
                    Log.d(TAG, "find the channel : tpid=" + param1 + " ,channelId=" + channelId);

                    //ProgramInfo chData = gPrimeDtvMediaPlayer.get_program_by_channel_id(channelId);
                    ProgramInfo chData = gDtvFramework.getProgramByChannelId(channelId);
                    if (null != chData) {
                        if (EnTVRadioFilter.TV.getValue() == chData.getType()) {
                            gTvChCount++;
                            chData.setDisplayNum(gTvChCount);
                        } else if (EnTVRadioFilter.RADIO.getValue() == chData.getType()) {
                            gRadioChCount++;
                            chData.setDisplayNum(gRadioChCount);
                        }

                        TVMessage msg = TVMessage.SetScanResultUpdate(
                                chData.getServiceId(),
                                chData.getType() == EnTVRadioFilter.RADIO.getValue() ? ProgramInfo.PROGRAM_RADIO : ProgramInfo.PROGRAM_TV,
                                chData.getDisplayNum(),
                                chData.getDisplayName(),
                                chData.getCA());
                        gDtvCallback.onMessage(msg);
                    }
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_SRCH_GET_PROG_PESI: {
                    long channelId = PrimeDtvMediaPlayer.get_unsigned_int(parm2);
                    Log.d(TAG, "find the channel : tpid=" + param1 + " ,channelId=" + channelId);
                    ChannelNode chData = new ChannelNode();
                    int count = 0;
                    // for PesiDtvFramework
                    // obj == ProgramInfo, can't be cast to Parcel
                    if (obj instanceof ProgramInfo) {
                        ProgramInfo programInfo = (ProgramInfo) obj;
                        chData.serviceID = programInfo.getServiceId();
                        chData.serviceType = programInfo.getType();
                        chData.LCN = programInfo.getLCN();
                        chData.OrignalServiceName = programInfo.getDisplayName();
                        chData.bCAMode = programInfo.getCA();
                        if (ProgramInfo.PROGRAM_TV == chData.serviceType) {
                            gTvChCount++;
                            count = gTvChCount;
                            Log.d(TAG, "PESI_SVR_EVT_SRCH_GET_PROG_PESI get channel !! TV : [channel name] : " + chData.OrignalServiceName + " ca " + chData.bCAMode);
                        } else if (ProgramInfo.PROGRAM_RADIO == chData.serviceType) {
                            gRadioChCount++;
                            count = gRadioChCount;
                            Log.d(TAG, "PESI_SVR_EVT_SRCH_GET_PROG_PESI get channel !! Radio : [channel name] : " + chData.OrignalServiceName);
                        }
                    }
                    else if (obj != null) { // for legacy PesiDtvMediaPlayer
                        chData.serviceID = ((Parcel) obj).readInt();
                        chData.serviceType = ((Parcel) obj).readInt();
                        chData.LCN = ((Parcel) obj).readInt();
                        chData.OrignalServiceName = ((Parcel) obj).readString();
                        chData.bCAMode = ((Parcel) obj).readInt();
                        if (ProgramInfo.PROGRAM_TV == chData.serviceType) {
                            gTvChCount++;
                            count = gTvChCount;
                            Log.d(TAG, "PESI_SVR_EVT_SRCH_GET_PROG_PESI get channel !! TV : [channel name] : " + chData.OrignalServiceName + " ca " + chData.bCAMode);
                        } else if (ProgramInfo.PROGRAM_RADIO == chData.serviceType) {
                            gRadioChCount++;
                            count = gRadioChCount;
                            Log.d(TAG, "PESI_SVR_EVT_SRCH_GET_PROG_PESI get channel !! Radio : [channel name] : " + chData.OrignalServiceName);
                        }
                    }

                    TVMessage msg = TVMessage.SetScanResultUpdate(
                            chData.serviceID,
                            chData.serviceType,
                            count,
                            chData.OrignalServiceName,
                            chData.bCAMode);
                    gDtvCallback.onMessage(msg);
                    break;
                }
                default:
                    break;
            }
        }
    };

    IDTVListener g_book_listener = new IDTVListener() {
        private static final String CB_TAG = "BOOKListener";

        @Override
        public void notifyMessage(int messageID, int param1, int parm2, Object obj) {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            switch (messageID) {
                case DTVMessage.PESI_SVR_EVT_BOOK_REMIND: {
                    Log.d(TAG, "PESI_SVR_EVT_BOOK_REMIND ID = " + param1);
                    BookInfo bookInfo = book_info_get_coming_book();
                    String name = bookInfo == null ? "" : bookInfo.getEventName();
                    Log.d(TAG, "incoming bookinfo = " + name);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_BOOK_TIME_ARRIVE: {
                    Log.d(TAG, "PESI_SVR_EVT_BOOK_TIME_ARRIVE ID = " + param1);
                    BookInfo bookInfo = book_info_get_coming_book();
                    String name = bookInfo == null ? "" : bookInfo.getEventName();
                    Log.d(TAG, "incoming bookinfo = " + name);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_BOOK_TIME_END: {
                    Log.d(TAG, "PESI_SVR_EVT_BOOK_TIME_END ID = " + param1);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_BOOK_RUN_AD:{
                    LogUtils.d("PESI_SVR_EVT_BOOK_RUN_AD");
                    TVMessage msg = TVMessage.RunAD();
                    gDtvCallback.onMessage(msg);
                }break;
                default:
                    break;
            }
        }
    };

    IDTVListener g_pvr_listener = new IDTVListener() {
        private static final String CB_TAG = "PVRListener";

        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj) {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            switch (messageID) {
                case DTVMessage.PESI_SVR_EVT_PVR_PLAY_EOF: {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_PLAY_EOF");
                    TVMessage msg = TVMessage.SetPvrEOF();
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_PVR_PLAY_SOF: {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_PLAY_SOF");
                    TVMessage msg = TVMessage.SetPvrPlaytoBegin();
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_PVR_PLAY_ERROR: {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_PLAY_ERROR");
                    TVMessage msg = TVMessage.SetPvrFilePlayStart(1);
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_PVR_PLAY_REACH_REC: {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_PLAY_REACH_REC");
                    TVMessage msg = TVMessage.SetPvrPlayReachRec();
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_PVR_REC_DISKFULL: {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_REC_DISKFULL");
                    TVMessage msg = TVMessage.SetPvrDISKFULL();
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_PVR_REC_ERROR: {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_REC_ERROR");
                    TVMessage msg = TVMessage.SetPvrRecordStart(1,param1,param2);
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_PVR_REC_OVER_FIX: {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_REC_OVER_FIX param1 = " + param1 + "param2 = " + param2);
                    TVMessage msg = TVMessage.SetPvrOverFix(param1, param2);//Scoty 20180720 add PESI_SVR_EVT_PVR_REC_OVER_FIX send recId
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_PVR_REC_REACH_PLAY: {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_REC_REACH_PLAY");
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_PVR_REC_DISK_SLOW: {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_REC_DISK_SLOW");
                    break;
                }
                case DTVMessage.PESI_EVT_PVR_RECORD_START://eric lin 20180713 pvr msg,-start
                {
                    Log.d(TAG, "PESI_EVT_PVR_RECORD_START");
                    //TVMessage msg = TVMessage.SetPvrRecordStart(param1, param2);
                    //gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_EVT_PVR_RECORD_STOP: {
                    Log.d(TAG, "PESI_EVT_PVR_RECORD_STOP");
                    TVMessage msg = TVMessage.SetPvrRecordStop();
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_EVT_PVR_TIMESHIFT_START: {
                    Log.d(TAG, "PESI_EVT_PVR_TIMESHIFT_START");
                    TVMessage msg = TVMessage.SetPvrTimeshiftStart();
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_EVT_PVR_TIMESHIFT_PLAY_START: {
                    Log.d(TAG, "PESI_EVT_PVR_RECORD_STOP");
                    TVMessage msg = TVMessage.SetPvrTimeshiftPlayStart();
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_EVT_PVR_TIMESHIFT_STOP: {
                    Log.d(TAG, "PESI_EVT_PVR_RECORD_STOP");
                    TVMessage msg = TVMessage.SetPvrTimeshiftStop();
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_EVT_PVR_FILE_PLAY_START: {
                    Log.d(TAG, "PESI_EVT_PVR_RECORD_STOP");
                    TVMessage msg = TVMessage.SetPvrFilePlayStart();
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_EVT_PVR_FILE_PLAY_STOP: {
                    Log.d(TAG, "PESI_EVT_PVR_RECORD_STOP");
                    TVMessage msg = TVMessage.SetPvrFilePlayStop();
                    gDtvCallback.onMessage(msg);
                    break;
                }//eric lin 20180713 pvr msg,-end
                case DTVMessage.PESI_EVT_PVR_PLAY_PARENTAL_LOCK:  //connie 20180806 for pvr parentalRate
                {
                    Log.d(TAG, "PESI_EVT_PVR_PLAY_PARENTAL_LOCK");
                    TVMessage msg = TVMessage.SetPvrPlayParanentLock();
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_OPEN_HD_FINISH:  //Scoty 20180827 add HDD Ready command and callback
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_OPEN_HD_FINISH");
                    TVMessage msg = TVMessage.SetPvrHardDiskReady();
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_PLAY_STOP:  //Scoty 20180827 add HDD Ready command and callback
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_TIMESHIFT_PLAY_STOP");
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_NOT_SUPPORT: //Scoty 20180827 add HDD Ready command and callback
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_NOT_SUPPORT");
                    TVMessage msg = TVMessage.SetPvrNotSupport();
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_RECORDING_COMPLETED:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_RECORDING_COMPLETED");
                    TVMessage msg = TVMessage.SetPvrRecordCompleted(param1);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_SUCCESS:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_SUCCESS");
                    //TVMessage msg = TVMessage.SetPvrNotSupport();
                    //gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_RECORDING_SUCCESS:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_RECORDING_SUCCESS");
                    TVMessage msg = TVMessage.SetPvrRecordStart(0,param1,param2);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_RECORDING_STOP_SUCCESS:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_RECORDING_STOP_SUCCESS");
                    TVMessage msg = TVMessage.SetPvrRecordStop(0,param1,param2);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_RECORDING_STOP_ERROR:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_RECORDING_STOP_ERROR");
                    TVMessage msg = TVMessage.SetPvrRecordStop(1,param1,param2);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_PLAYBACK_SUCCESS:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_PLAYBACK_SUCCESS");
                    TVMessage msg = TVMessage.SetPvrFilePlayStart(0);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_PLAYBACK_STOP_ERROR:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_PLAYBACK_STOP_ERROR");
                    TVMessage msg = TVMessage.SetPvrFilePlayStop(1);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_PLAYBACK_STOP_SUCCESS:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_PLAYBACK_STOP_SUCCESS");
                    TVMessage msg = TVMessage.SetPvrFilePlayStop(0);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_PLAY_RESUME_ERROR:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_PLAY_RESUME_ERROR");
                    TVMessage msg = TVMessage.SetPvrFilePlayResume(1);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_PLAY_RESUME_SUCCESS:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_RECORDING_SUCCESS");
                    TVMessage msg = TVMessage.SetPvrFilePlayResume(0);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_PLAY_PAUSE_ERROR:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_RECORDING_SUCCESS");
                    TVMessage msg = TVMessage.SetPvrFilePlayPause(1);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_PLAY_PAUSE_SUCCESS:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_PLAY_PAUSE_SUCCESS");
                    TVMessage msg = TVMessage.SetPvrFilePlayPause(0);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_PLAY_FF_ERROR:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_PLAY_FF_ERROR");
                    TVMessage msg = TVMessage.SetPvrFilePlayFastForward(1,param1);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_PLAY_FF_SUCCESS:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_PLAY_FF_SUCCESS");
                    TVMessage msg = TVMessage.SetPvrFilePlayFastForward(0,param1);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_PLAY_RW_ERROR:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_PLAY_RW_ERROR");
                    TVMessage msg = TVMessage.SetPvrFilePlayRewind(1,param1);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_PLAY_RW_SUCCESS:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_PLAY_RW_SUCCESS");
                    TVMessage msg = TVMessage.SetPvrFilePlayRewind(0,param1);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_PLAY_SEEK_ERROR:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_PLAY_SEEK_ERROR");
                    TVMessage msg = TVMessage.SetPvrFileSeek(1);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_PLAY_SEEK_SUCCESS:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_PLAY_SEEK_SUCCESS");
                    TVMessage msg = TVMessage.SetPvrFileSeek(0);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_SUCCESS:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_TIMESHIFT_SUCCESS");
                    TVMessage msg = TVMessage.SetPvrTimeshiftStart(0);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_ERROR:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_TIMESHIFT_ERROR");
                    TVMessage msg = TVMessage.SetPvrTimeshiftStart(2);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_STOP_SUCCESS:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_TIMESHIFT_STOP_SUCCESS");
                    TVMessage msg = TVMessage.SetPvrTimeshiftStop(0);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_STOP_ERROR:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_TIMESHIFT_STOP_ERROR");
                    TVMessage msg = TVMessage.SetPvrTimeshiftStop(1);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_PLAY_SUCCESS:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_TIMESHIFT_PLAY_SUCCESS");
                    //TVMessage msg = TVMessage.SetPvrNotSupport();
                    //gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_PLAY_ERROR:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_TIMESHIFT_PLAY_ERROR");
                    TVMessage msg = TVMessage.SetPvrTimeshiftStart(1);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_RESUME_ERROR:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_TIMESHIFT_RESUME_ERROR");
                    TVMessage msg = TVMessage.SetPvrTimeshiftPlayResume(1);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_RESUME_SUCCESS:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_TIMESHIFT_RESUME_SUCCESS");
                    TVMessage msg = TVMessage.SetPvrTimeshiftPlayResume(0);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_PAUSE_ERROR:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_TIMESHIFT_PAUSE_ERROR");
                    TVMessage msg = TVMessage.SetPvrTimeshiftPlayPause(1);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_PAUSE_SUCCESS:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_TIMESHIFT_PAUSE_SUCCESS");
                    TVMessage msg = TVMessage.SetPvrTimeshiftPlayPause(0);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_FF_ERROR:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_TIMESHIFT_FF_ERROR");
                    TVMessage msg = TVMessage.SetPvrTimeshiftPlayFastForward(1,param1);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_FF_SUCCESS:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_TIMESHIFT_FF_SUCCESS");
                    TVMessage msg = TVMessage.SetPvrTimeshiftPlayFastForward(0,param1);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_RW_ERROR:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_TIMESHIFT_RW_ERROR");
                    TVMessage msg = TVMessage.SetPvrTimeshiftPlayRewind(1,param1);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_RW_SUCCESS:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_TIMESHIFT_RW_SUCCESS");
                    TVMessage msg = TVMessage.SetPvrTimeshiftPlayRewind(0,param1);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_SEEK_ERROR:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_TIMESHIFT_SEEK_ERROR");
                    TVMessage msg = TVMessage.SetPvrTimeshiftPlaySeek(1);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_SEEK_SUCCESS:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_TIMESHIFT_SEEK_SUCCESS");
                    TVMessage msg = TVMessage.SetPvrTimeshiftPlaySeek(0);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_PLAY_CHANGE_AUDIO_TRACK_ERROR:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_PLAY_CHANGE_AUDIO_TRACK_ERROR");
                    TVMessage msg = TVMessage.SetPvrChangeAudioTrack(0,1);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_PLAY_CHANGE_AUDIO_TRACK_SUCCESS:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_PLAY_CHANGE_AUDIO_TRACK_SUCCESS");
                    TVMessage msg = TVMessage.SetPvrChangeAudioTrack(0,0);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_CHANGE_AUDIO_TRACK_ERROR:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_TIMESHIFT_CHANGE_AUDIO_TRACK_ERROR");
                    TVMessage msg = TVMessage.SetPvrChangeAudioTrack(1,1);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_PVR_TIMESHIFT_CHANGE_AUDIO_TRACK_SUCCESS:
                {
                    Log.d(TAG, "PESI_SVR_EVT_PVR_TIMESHIFT_CHANGE_AUDIO_TRACK_SUCCESS");
                    TVMessage msg = TVMessage.SetPvrChangeAudioTrack(1,0);
                    gDtvCallback.onMessage(msg);
                }
                break;
                default:
                    break;
            }
        }
    };

    IDTVListener g_tbm_listener = new IDTVListener() {
        private static final String CB_TAG = "TBMListener";

        @Override
        public void notifyMessage(int messageID, int param1, int parm2, Object obj) {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            switch (messageID) {
                case DTVMessage.PESI_SVR_EVT_TBM_UPDATE: {
                    Log.d(TAG, "PESI_SVR_EVT_TBM_UPDATE table id = " + param1 + " table size = " + parm2);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_TIME_UPDATE: {
                    Log.d(TAG, "PESI_SVR_EVT_TIME_UPDATE pls check!!!!!!");
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_TIMEZONE_UPDATE: {
                    Log.d(TAG, "PESI_SVR_EVT_TIMEZONE_UPDATE timezone sec = " + param1 + " timezonesec size = " + parm2);
                    break;
                }
                default:
                    break;
            }
        }
    };

    IDTVListener g_ci_listener = new IDTVListener() {
        private static final String CB_TAG = "CIListener";

        @Override
        public void notifyMessage(int messageID, int param1, int parm2, Object obj) {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            switch (messageID) {
                case DTVMessage.PESI_SVR_CI_EVT_CARD_INSERT: {
                    Log.d(TAG, "PESI_SVR_CI_EVT_CARD_INSERT");
                    break;
                }
                case DTVMessage.PESI_SVR_CI_EVT_CARD_REMOVE: {
                    Log.d(TAG, "PESI_SVR_CI_EVT_CARD_REMOVE");
                    break;
                }
                case DTVMessage.PESI_SVR_CI_EVT_MMI_MESSAGE: {
                    Log.d(TAG, "PESI_SVR_CI_EVT_MMI_MESSAGE");
                    break;
                }
                case DTVMessage.PESI_SVR_CI_EVT_MMI_MENU: {
                    Log.d(TAG, "PESI_SVR_CI_EVT_MMI_MENU");
                    break;
                }
                case DTVMessage.PESI_SVR_CI_EVT_MMI_LIST: {
                    Log.d(TAG, "PESI_SVR_CI_EVT_MMI_LIST");
                    break;
                }
                case DTVMessage.PESI_SVR_CI_EVT_MMI_ENQ: {
                    Log.d(TAG, "PESI_SVR_CI_EVT_MMI_ENQ");
                    break;
                }
                case DTVMessage.PESI_SVR_CI_EVT_CLOSE_MMI: {
                    Log.d(TAG, "PESI_SVR_CI_EVT_CLOSE_MMI");
                    break;
                }
                default:
                    break;
            }
        }
    };

    IDTVListener g_ews_listener = new IDTVListener() {
        private static final String CB_TAG = "EWSListener";

        @Override
        public void notifyMessage(int messageID, int param1, int parm2, Object obj) {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            switch (messageID) {
                case DTVMessage.PESI_SVR_EVT_EWS_START: {
                    Log.d(TAG, "PESI_SVR_EVT_EWS_START  need change to channelID = " + parm2);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_EWS_STOP: {
                    Log.d(TAG, "PESI_SVR_EVT_EWS_STOP need change back channelID = " + parm2);
                    break;
                }
                default:
                    break;
            }
        }
    };

    IDTVListener g_vmx_listener = new IDTVListener()//Scoty 20180831 add vmx callback event
    {
        private static final String CB_TAG = "VMXListener";

        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj) {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            switch (messageID) {
                // for VMX need open/close -s
//                case DTVMessage.PESI_SVR_EVT_VMX_SHOW_MSG: { // connie 20180903 for VMX -s
//                    Log.d(TAG, "PESI_SVR_EVT_VMX_SHOW_MSG") ;
//                    int mode = 0, duration = 0, triggerID = 0, triggerNum =0;
//                    String CAmsg="";
//
//                    mode = param1;
//                    duration = param2;
//                    if(obj != null) {
//                        CAmsg = ((Parcel) obj).readString();
//                        triggerID = ((Parcel) obj).readInt();
//                        triggerNum = ((Parcel) obj).readInt();
//                    }
//                    Log.d(TAG, CB_TAG + "      mode = " + mode + "     CAmsg = " + CAmsg + "     duration = " + duration + "    triggerID ="+ triggerID + "   triggerNum = "+ triggerNum);
//
//                    TVMessage msg = TVMessage.SetVMXShowMsg(mode, CAmsg, duration, triggerID, triggerNum);
//                    onMessage(msg);
//                }
//                break;
                case DTVMessage.PESI_SVR_EVT_VMX_OTA_START: {
                    Log.d(TAG, "PESI_SVR_EVT_VMX_OTA_START");
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_VMX_OTA_ERR: {
                    Log.d(TAG, "PESI_SVR_EVT_VMX_OTA_ERR");
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_VMX_WATERMARK: {
                    Log.d(TAG, "PESI_SVR_EVT_VMX_WATERMARK");
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_VMX_WATERMARK_CLOSE: {
                    Log.d(TAG, "PESI_SVR_EVT_VMX_WATERMARK_CLOSE");
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_VMX_PIN: {
                    Log.d(TAG, "PESI_SVR_EVT_VMX_PIN");
                    int enable = param1;
                    long channelID = PrimeDtvMediaPlayer.get_unsigned_int(param2);
                    int pinIndex = 0, textSelector = 0;
                    if (obj != null) {
                        pinIndex = ((Parcel) obj).readInt();
                        textSelector = ((Parcel) obj).readInt();
                    }
                    Log.d(TAG, CB_TAG + "        enable = " + enable + "    channelID = " + channelID + "    pinIndex =" + pinIndex + "    textSelector = " + textSelector);
                    TVMessage msg = TVMessage.VMXSetPin(enable, channelID, pinIndex, textSelector);
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_VMX_IPPV: {
                    Log.d(TAG, "PESI_SVR_EVT_VMX_IPPV");
                    int enable = param1;
                    long channelID = PrimeDtvMediaPlayer.get_unsigned_int(param2);
                    int pinIndex = 0;
                    String curToken = "";
                    String cost = "";
                    if (obj != null) {
                        pinIndex = ((Parcel) obj).readInt();
                        curToken = ((Parcel) obj).readString();
                        cost = ((Parcel) obj).readString();
                    }
                    Log.d(TAG, CB_TAG + "        enable = " + enable + "    channelID = " + channelID + "    pinIndex = " + pinIndex + "    curToken = " + curToken + "    cost = " + cost);
                    TVMessage msg = TVMessage.VMXIPPV(enable, channelID, pinIndex, curToken, cost);
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_EVT_VMX_BCIO_NOTIFY: // connie 20180925 add for ippv/pin bcio notify
                {
                    int errType = param1;
                    TVMessage msg = TVMessage.VMXBcioNotify(errType);
                    gDtvCallback.onMessage(msg);
                }
                break;
                case DTVMessage.PESI_SVR_EVT_CARD_DETECT: {
                    Log.d(TAG, "PESI_SVR_EVT_CARD_DETECT");
                    int cardStatus = param1;
                    Log.d(TAG, CB_TAG + "        cardStatus = " + cardStatus);
                    TVMessage msg = TVMessage.VMXCardDetect(cardStatus);
                    gDtvCallback.onMessage(msg);
                    break;// for VMX need open/close -e
                }
                //case DTVMessage.PESI_SVR_EVT_VMX_SEARCH:
                //{
                //    Log.d(TAG, "PESI_SVR_EVT_VMX_SEARCH") ;
                //    break;
                //}
                // connie 20180903 for VMX -e
                default:
                    break;
            }
        }
    };

    IDTVListener g_loader_dtv_listener = new IDTVListener() {
        private static final String CB_TAG = "LoaderDtvListener";

        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj) {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            switch (messageID) {
                case DTVMessage.PESI_LOADERDVT_RETURN_DSMCC_STATUS: {
                    Log.d(TAG, "PESI_LOADERDVT_RETURN_DSMCC_STATUS");
                    int is_locked = 0, dsi_size = 0;
                    is_locked = param1;
                    dsi_size = param2;
                    Log.d(TAG, "PESI_LOADERDVT_RETURN_DSMCC_STATUS is_locked = " + is_locked + " dsi_size" + dsi_size);
                    TVMessage msg = TVMessage.SetDownloadServiceStatus(is_locked, dsi_size);
                    gDtvCallback.onMessage(msg);
                    break;
                }
                default:
                    break;
            }
        }
    };

    IDTVListener g_pio_listener = new IDTVListener() {
        private static final String CB_TAG = "PIOListener";

        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj) {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            TVMessage msg;
            switch (messageID) {
                case DTVMessage.PESI_EVT_PIO_FRONT_PANEL_KEY_CH_DOWN:
                    Log.d(TAG, "notifyMessage: KEY_CH_DOWN");
                    msg = new TVMessage(TVMessage.FLAG_PIO, TVMessage.TYPE_PIO_FRONT_PANEL_KEY_CH_DOWN);
                    gDtvCallback.onMessage(msg);
                    break;
                case DTVMessage.PESI_EVT_PIO_FRONT_PANEL_KEY_POWER:
                    Log.d(TAG, "notifyMessage: KEY_POWER");
                    msg = new TVMessage(TVMessage.FLAG_PIO, TVMessage.TYPE_PIO_FRONT_PANEL_KEY_POWER);
                    gDtvCallback.onMessage(msg);
                    break;
                case DTVMessage.PESI_EVT_PIO_FRONT_PANEL_KEY_CH_UP:
                    Log.d(TAG, "notifyMessage: KEY_CH_UP");
                    msg = new TVMessage(TVMessage.FLAG_PIO, TVMessage.TYPE_PIO_FRONT_PANEL_KEY_CH_UP);
                    gDtvCallback.onMessage(msg);
                    break;
                case DTVMessage.PESI_EVT_PIO_USB_OVERLOAD:
                    Log.d(TAG, "notifyMessage: USB_OVERLOAD, port = " + param1);
                    msg = TVMessage.SetPioUsbOverload(param1);
                    gDtvCallback.onMessage(msg);
                    break;
                case DTVMessage.PESI_EVT_PIO_ANTENNA_OVERLOAD:
                    Log.d(TAG, "notifyMessage: ANTENNA_OVERLOAD");
                    msg = new TVMessage(TVMessage.FLAG_PIO, TVMessage.TYPE_PIO_ANTENNA_OVERLOAD);
                    gDtvCallback.onMessage(msg);
                    break;
                default:
                    break;
            }
        }
    };

    IDTVListener g_system_listener = new IDTVListener() {
        private static final String CB_TAG = "SystemListener";

        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj) {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            TVMessage msg;
            switch (messageID) {
                case DTVMessage.PESI_EVT_SYSTEM_DQA_AUTO_TEST:
                    Log.d(TAG, "notifyMessage: PESI_EVT_SYSTEM_DQA_AUTO_TEST");
                    msg = TVMessage.SetDqaAutoTest(param1);
                    gDtvCallback.onMessage(msg);
                    break;
                case DTVMessage.PESI_EVT_SYSTEM_PMT_UPDATE_AV: {
                    ProgramInfo programInfo = (ProgramInfo) obj;
                    boolean current_channel;
                    Log.d(TAG, "notifyMessage: PESI_EVT_SYSTEM_PMT_UPDATE_AV [CH "+programInfo.getDisplayNum()+" "+programInfo.getDisplayName()+"]");
                    //Log.d(TAG, "notifyMessage: PESI_EVT_SYSTEM_PMT_UPDATE_AV current_channel "+current_channel);
                    Log.d(TAG, "notifyMessage: PESI_EVT_SYSTEM_PMT_UPDATE_AV  "+programInfo.getChannelId()+" "+gpos_info_get().getCurChannelId());
                    if((programInfo!=null) && (programInfo.getChannelId() == gpos_info_get().getCurChannelId()))
                        current_channel = true;
                    else
                        current_channel = false;
                    if(current_channel) {
                        if (AvCmdMiddle.is_e213(programInfo.getChannelId())) {
                            LogUtils.d("[e213] call PESI_SVR_EVT_AV_FRONTEND_STOP");
                            getPesiDtvFrameworkInterfaceCallback().sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_FRONTEND_STOP, (int) programInfo.getChannelId(), 0, null);
                        }
                        else {
                                av_control_play_by_channel_id(programInfo.getTunerId(), programInfo.getChannelId(), ViewHistory.getCurGroupType(), 1);
                        }
                    }
                    else{
                        av_control_set_fast_change_channel(programInfo.getTunerId(), programInfo.getChannelId());
                    }
                }break;
                case DTVMessage.PESI_EVT_SYSTEM_PMT_UPDATE_VERSION: {
                    ProgramInfo programInfo = (ProgramInfo) obj;
                    Log.d(TAG, "notifyMessage: PESI_EVT_SYSTEM_PMT_UPDATE_VERSION");
                    if(param1 == 1){
                        av_control_set_fast_change_channel(programInfo.getTunerId(),programInfo.getChannelId());
                    }else {
                        LogUtils.d("[db_stopav] IN");
                        av_control_play_stop(0,0,0);
                        av_control_play_by_channel_id(programInfo.getTunerId(), programInfo.getChannelId(), ViewHistory.getCurGroupType(), 1);
                    }
                }break;
                case DTVMessage.PESI_EVT_SYSTEM_PMT_UPDATE_SUBTITLE: {

                }break;
                case DTVMessage.PESI_EVT_SYSTEM_START_MONITOR_TABLE: {
                    //ProgramInfo programInfo = (ProgramInfo) obj;
                    startMonitorTable((long)obj, param1, param2);
                }break;
                case DTVMessage.PESI_EVT_SYSTEM_STOP_MONITOR_TABLE: {
                    stopMonitorTable((long)obj, param2);
                }break;
                case DTVMessage.PESI_EVT_SYSTEM_SI_UPDATE_CHANNEL:{
                    LogUtils.d("SIUpdate Send PESI_EVT_SYSTEM_SI_UPDATE_CHANNEL to UI");
                    msg = TVMessage.UpdateChannelList();
                    gDtvCallback.onMessage(msg);
                }break;
                case DTVMessage.PESI_EVT_SYSTEM_SI_UPDATE_SET_CHANNEL:{
                    LogUtils.d("SIUpdate Send PESI_EVT_SYSTEM_SI_UPDATE_SET_CHANNEL to UI");
                    msg = TVMessage.SetChannel((ProgramInfo) obj);
                    gDtvCallback.onMessage(msg);
                }break;
                case DTVMessage.PESI_EVT_SYSTEM_SI_UPDATE_SET_1ST_CHANNEL:{
                    LogUtils.d("SIUpdate Send PESI_EVT_SYSTEM_SI_UPDATE_SET_1ST_CHANNEL to UI");
                    msg = TVMessage.Set1stChannel();
                    gDtvCallback.onMessage(msg);
                }break;
                case DTVMessage.PESI_EVT_SYSTEM_SI_UPDATE_WVCAS_URL:{
                    String url = (String) obj;
                    int stbUpdateTime = param1;
                    long channelId = (long)param2;
                    ProgramInfo programInfo = get_program_by_channel_id(channelId);
                    LogUtils.d("PESI_EVT_SYSTEM_SI_UPDATE_WVCAS_URL mStbUpdateTime "+stbUpdateTime+" URL = "+url+" channelId = "+channelId);
                    gpos_info_update_by_key_string(GposInfo.GPOS_WVCAS_LICENSE_URL, url);
                    //remove_wvcas_license("");
                    //handleCasRefresh(1,0, 0);
                    gpos_info_update_by_key_string(GposInfo.GPOS_WVCAS_REF_CASDATA_TIME, stbUpdateTime);
                    saveGposKeyValue(GposInfo.GPOS_WVCAS_LICENSE_URL, url);
                    saveGposKeyValue(GposInfo.GPOS_WVCAS_REF_CASDATA_TIME, stbUpdateTime);
//                    if(programInfo != null){
//                        av_control_play_stop(0,0,0);
//                        av_control_play_by_channel_id(programInfo.getTunerId(), programInfo.getChannelId(), ViewHistory.getCurGroupType(), 1);
//                    }
                }break;
                case DTVMessage.PESI_EVT_SYSTEM_SI_UPDATE_TBC_BAT_WHITE_LIST_FAIL:{
                    if(!Pvcfg.isCheckIllegalNetwork())
                        break;
                    TVMessage tvMessage = TVMessage.SetShowErrorMsg(ErrorCodeUtil.ERROR_E301,"");
                    gDtvCallback.onMessage(tvMessage);
                }break;
                default:
                    break;
            }
        }
    };

    //Scoty 20190410 add Mtest Pc Tool callback -s
    IDTVListener g_mtest_listener = new IDTVListener() {
        private static final String CB_TAG = "MtestListener";

        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj) {
            Log.d(TAG, CB_TAG + " messageID = " + messageID + " param1 = " + param1 + " param2 = " + param2);
            TVMessage msg;
            switch (messageID) {
                case DTVMessage.PESI_EVT_MTEST_PCTOOL:
                    Log.d(TAG, "notifyMessage: PESI_EVT_MTEST_PCTOOL");
                    msg = TVMessage.SetMtestPcTool(param1, param2);
                    gDtvCallback.onMessage(msg);
                    break;
                case DTVMessage.PESI_EVT_MTEST_FPKEY_RESET:
                    Log.d(TAG, "notifyMessage: PESI_EVT_MTEST_FPKEY_RESET");
                    msg = new TVMessage(TVMessage.FLAG_MTEST, TVMessage.TYPE_MTEST_FRONT_PANEL_KEY_RESET);
                    gDtvCallback.onMessage(msg);
                    break;
                case DTVMessage.PESI_EVT_MTEST_FPKEY_WPS:
                    Log.d(TAG, "notifyMessage: PESI_EVT_MTEST_FPKEY_WPS");
                    msg = new TVMessage(TVMessage.FLAG_MTEST, TVMessage.TYPE_MTEST_FRONT_PANEL_KEY_WPS);
                    gDtvCallback.onMessage(msg);
                    break;
                default:
                    break;
            }
        }
    };
    //Scoty 20190410 add Mtest Pc Tool callback -e
    IDTVListener g_test_listener = new IDTVListener() {
        private static final String CB_TAG = "TestListener";

        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj) {
            Log.d(TAG, CB_TAG + " messageID = " + messageID + " param1 = " + param1 + " param2 = " + param2);
            TVMessage msg;
            switch (messageID) {
                case DTVMessage.PESI_EVT_PT_DEBUG_CALLBACK_TEST:
                    Log.d(TAG, "notifyMessage: PESI_EVT_PT_DEBUG_CALLBACK_TEST");
                    int ret = 0;
                    String name = null;
                    if (obj != null) {
                        ret = ((Parcel) obj).readInt();
                        name = ((Parcel) obj).readString();
                    }
                    msg = TVMessage.SetTestCallbackTest(param1, param2, name);
                    gDtvCallback.onMessage(msg);
                    break;
                default:
                    break;
            }
        }
    };

    final IDTVListener g_ca_listener = new IDTVListener() {
        private static final String CB_TAG = "CaListener";

        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj) {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            TVMessage msg = null;
            switch (messageID) {
                case DTVMessage.PESI_EVT_CA_WIDEVINE_REFRESH_CAS_DATA: {
                    long channel_id;
                    if (obj instanceof Long) {
                        channel_id = (Long) obj; // 正確轉換
                    } else {
                        channel_id = 0; // 或者根據需要處理其他情況
                    }
                    handleCasRefresh(param1, param2, channel_id);
                }break;
                case DTVMessage.PESI_EVT_CA_WIDEVINE_ERROR: {
                    sendCasError(param1, (String) obj);
                }break;
                case DTVMessage.PESI_EVT_CA_WIDEVINE_REMOVE_LICENSE:{
                    long channelId = (long)param1;
                    if(obj == null)
                        break;
                    String licenseId = (String)obj;
                    remove_wvcas_license(licenseId);
                    ProgramInfo p = get_program_by_channel_id(channelId);
                    LogUtils.d("PESI_EVT_CA_WIDEVINE_REMOVE_LICENSE channelId = "+channelId+" "+gpos_info_get().getCurChannelId());
                    LogUtils.d("PESI_EVT_CA_WIDEVINE_REMOVE_LICENSE p = "+p);
                    if(channelId == gpos_info_get().getCurChannelId() && p != null){
                        av_control_play_stop(p.getTunerId(),0,1);
                        av_control_play_by_channel_id(p.getTunerId(), p.getChannelId(), ViewHistory.getCurGroupType(), 1);
                    }

                }break;
                case DTVMessage.PESI_EVT_CA_WIDEVINE://eric lin 20210107 widevine cas
                    Log.d(TAG, "notifyMessage: PESI_EVT_CA_WIDEVINE");
                    Parcel in = ((Parcel) obj);
                    byte[] val = null;
                    TVMessage.widevineMsg wvMsg = new TVMessage.widevineMsg();
                    if (obj != null) {
                        int lens = in.readInt();
                        Log.d(TAG, "notifyMessage: PESI_EVT_CA_WIDEVINE lens=" + lens);
                        wvMsg.ecmDataLen = lens;
                        if (lens > 0) {
                            val = new byte[lens];
                            int sessionIndex = -1;
                            in.readByteArray(val);
                            //Log.d(TAG, "notifyMessage: PESI_EVT_CA_WIDEVINE handleMessage() 1 data[0]="+String.format("%02X ", val[0])+", last data="+String.format("%02X ", val[lens-1]));
                            wvMsg.ecmData = val;
                            if (param1 == CALLBACK_EVENT_ECM) {
                                sessionIndex = in.readInt();
                                //Log.d(TAG, "notifyMessage: CALLBACK_EVENT_ECM sessionIndex="+sessionIndex+ ", data[0]="+String.format("%02X ", val[0]));
                                msg = TVMessage.SetCaWVMsg(param1, val, sessionIndex);
                            } else if (param1 == CALLBACK_EVENT_PRIVATE_DATA) {
                                Log.d(TAG, "notifyMessage: CALLBACK_EVENT_PRIVATE_DATA lens=" + lens);
                                msg = TVMessage.SetCaWVMsg(param1, val);//eric lin 20210112 widevine scheme data
                            }
                            //for(int i=0; i<lens; i++)
                            //{
                            //    Log.d(TAG, "notifyMessage: PESI_EVT_CA_WIDEVINE data["+i+"]="+String.format("%02X ", val[i]));
                            //}

                        } else {
                            int sessionMode;
                            int wv_index;
                            int es_pid;
                            int ecm_pid;

                            sessionMode = in.readInt();
                            wv_index = in.readInt();
                            es_pid = in.readInt();
                            ecm_pid = in.readInt();
                            Log.d(TAG, "notifyMessage: sessionMode=" + sessionMode + ", wv_index=" + wv_index
                                    + ", es_pid=" + es_pid + ", ecm_pid=" + ecm_pid);
                            wvMsg.sessionMode = sessionMode;
                            wvMsg.wv_index = wv_index;
                            wvMsg.es_pid = es_pid;
                            wvMsg.ecm_pid = ecm_pid;
                            msg = TVMessage.SetCaWVMsg(param1, sessionMode, wv_index, es_pid, ecm_pid);
                        }
                    }
                    gDtvCallback.onMessage(msg);
                    break;
                default:
                    break;
            }
        }
    };

    final IDTVListener g_series_listener = new IDTVListener() {
        private static final String CB_TAG = "SeriesListener";
        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj) {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            TVMessage msg = null;
            switch(messageID) {
                case DTVMessage.PESI_EVT_SERIES_UPDATE: {
                    msg = TVMessage.SetSeriesUpdateMsg();
                    gDtvCallback.onMessage(msg);
                }break;
            }
        }
    };

    public enum CaPriority {
        ERR_E48_52(0),
        ERR_E38(1),
        ERR_CA(2),
        ERR_E44(3),
        ERR_PROG_LOCK(4),
        ERR_PESI_DEFINE(5),
        ERR_E42(6),
        ERR_MAX(7);

        private int value;

        private CaPriority(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    public PrimeDtv(DTVCallback callback,Context context) {
        Log.d(TAG,"context = "+context);
        mContext = context;
        //gPrimeDtvMediaPlayer = PrimeDtvMediaPlayer.get_instance(context);
        gDtvFramework = PesiDtvFramework.getInstance(context);
        LogUtils.d(" ");
        gPrimeDtvMediaPlayer = PrimeDtvMediaPlayer.get_instance(context);
        LogUtils.d(" ");
        gDtvCallback = callback;
        try {
            mPrimeMiscService = IMisc.getService(true);
            mPrimeMediaService = IMedia.getService(true);
            Log.d(TAG," Get mPrimeMiscService => [" + mPrimeMiscService + "]");
            Log.d(TAG," Get mPrimeMediaService => [" + mPrimeMediaService + "]");
        } catch (RemoteException | NoSuchElementException e) {
            Log.e(TAG, "Exception : getService fail", e);
        }

    }

    private void PesiDtvFramework_SubScribeEvent()
    {
        PesiDtvFramework_registCallback ( DTVMessage.PESI_SVR_EVT_AV_CALLBACK_START, DTVMessage.PESI_SVR_EVT_AV_CALLBACK_END, g_av_listener ) ;
        PesiDtvFramework_registCallback ( DTVMessage.PESI_SVR_EVT_EPG_CALLBACK_START, DTVMessage.PESI_SVR_EVT_EPG_CALLBACK_END, g_epg_listener ) ;
        PesiDtvFramework_registCallback ( DTVMessage.PESI_SVR_EVT_SRCH_CALLBACK_START, DTVMessage.PESI_SVR_EVT_SRCH_CALLBACK_END, g_scan_listener ) ;
        PesiDtvFramework_registCallback ( DTVMessage.PESI_SVR_EVT_BOOK_CALLBACK_START, DTVMessage.PESI_SVR_EVT_BOOK_CALLBACK_END, g_book_listener ) ;
        PesiDtvFramework_registCallback ( DTVMessage.PESI_SVR_EVT_PVR_CALLBACK_START, DTVMessage.PESI_SVR_EVT_PVR_CALLBACK_END, g_pvr_listener ) ;
        PesiDtvFramework_registCallback ( DTVMessage.PESI_SVR_EVT_TBM_CALLBACK_START, DTVMessage.PESI_SVR_EVT_TBM_CALLBACK_END, g_tbm_listener ) ;
        PesiDtvFramework_registCallback ( DTVMessage.PESI_SVR_CI_EVT_CALLBACK_START, DTVMessage.PESI_SVR_CI_EVT_CALLBACK_END, g_ci_listener ) ;
        PesiDtvFramework_registCallback ( DTVMessage.PESI_SVR_EVT_EWS_CALLBACK_START, DTVMessage.PESI_SVR_EVT_EWS_CALLBACK_END, g_ews_listener ) ;
        PesiDtvFramework_registCallback ( DTVMessage.PESI_SVR_EVT_VMX_CALLBACK_START, DTVMessage.PESI_SVR_EVT_VMX_CALLBACK_END, g_vmx_listener ) ;//Scoty 20180831 add vmx callback event
        PesiDtvFramework_registCallback ( DTVMessage.PESI_SVR_EVT_LOADERDTV_CALLBACK_START, DTVMessage.PESI_SVR_EVT_LOADERDTV_CALLBACK_END, g_loader_dtv_listener ) ;
        PesiDtvFramework_registCallback ( DTVMessage.PESI_EVT_PIO_CALLBACK_START, DTVMessage.PESI_EVT_PIO_CALLBACK_END, g_pio_listener ) ;
        PesiDtvFramework_registCallback ( DTVMessage.PESI_EVT_SYSTEM_CALLBACK_START, DTVMessage.PESI_EVT_SYSTEM_CALLBACK_END, g_system_listener ) ;
        PesiDtvFramework_registCallback ( DTVMessage.PESI_EVT_MTEST_START, DTVMessage.PESI_EVT_MTEST_END, g_mtest_listener ) ;//Scoty 20190410 add Mtest Pc Tool callback
        PesiDtvFramework_registCallback ( DTVMessage.PESI_EVT_PT_DEBUG_CALLBACK_TEST, DTVMessage.PESI_EVT_PT_DEBUG_END, g_test_listener ) ;
        PesiDtvFramework_registCallback ( DTVMessage.PESI_EVT_CA_WIDEVINE, DTVMessage.PESI_EVT_CA_END, g_ca_listener ) ;//eric lin 20210107 widevine cas
    }

    private void PesiDtvFramework_UnSubScribeEvent()
    {
        PesiDtvFramework_unregistCallback ( DTVMessage.PESI_SVR_EVT_AV_CALLBACK_START, DTVMessage.PESI_SVR_EVT_AV_CALLBACK_END, g_av_listener ) ;
        PesiDtvFramework_unregistCallback ( DTVMessage.PESI_SVR_EVT_EPG_CALLBACK_START, DTVMessage.PESI_SVR_EVT_EPG_CALLBACK_END, g_epg_listener ) ;
        PesiDtvFramework_unregistCallback ( DTVMessage.PESI_SVR_EVT_SRCH_CALLBACK_START, DTVMessage.PESI_SVR_EVT_SRCH_CALLBACK_END, g_scan_listener ) ;
        PesiDtvFramework_unregistCallback ( DTVMessage.PESI_SVR_EVT_BOOK_CALLBACK_START, DTVMessage.PESI_SVR_EVT_BOOK_CALLBACK_END, g_book_listener ) ;
        PesiDtvFramework_unregistCallback ( DTVMessage.PESI_SVR_EVT_PVR_CALLBACK_START, DTVMessage.PESI_SVR_EVT_PVR_CALLBACK_END, g_pvr_listener ) ;
        PesiDtvFramework_unregistCallback ( DTVMessage.PESI_SVR_EVT_TBM_CALLBACK_START, DTVMessage.PESI_SVR_EVT_TBM_CALLBACK_END, g_tbm_listener ) ;
        PesiDtvFramework_unregistCallback ( DTVMessage.PESI_SVR_CI_EVT_CALLBACK_START, DTVMessage.PESI_SVR_CI_EVT_CALLBACK_END, g_ci_listener ) ;
        PesiDtvFramework_unregistCallback ( DTVMessage.PESI_SVR_EVT_EWS_CALLBACK_START, DTVMessage.PESI_SVR_EVT_EWS_CALLBACK_END, g_ews_listener ) ;
        PesiDtvFramework_unregistCallback ( DTVMessage.PESI_SVR_EVT_VMX_CALLBACK_START, DTVMessage.PESI_SVR_EVT_VMX_CALLBACK_END, g_vmx_listener ) ;//Scoty 20180831 add vmx callback event
        PesiDtvFramework_unregistCallback ( DTVMessage.PESI_SVR_EVT_LOADERDTV_CALLBACK_START, DTVMessage.PESI_SVR_EVT_LOADERDTV_CALLBACK_END, g_loader_dtv_listener ) ;
        PesiDtvFramework_unregistCallback ( DTVMessage.PESI_EVT_PIO_CALLBACK_START, DTVMessage.PESI_EVT_PIO_CALLBACK_END, g_pio_listener ) ;
        PesiDtvFramework_unregistCallback ( DTVMessage.PESI_EVT_SYSTEM_CALLBACK_START, DTVMessage.PESI_EVT_SYSTEM_CALLBACK_END, g_system_listener ) ;
        PesiDtvFramework_unregistCallback ( DTVMessage.PESI_EVT_MTEST_START, DTVMessage.PESI_EVT_MTEST_END, g_mtest_listener ) ;//Scoty 20190410 add Mtest Pc Tool callback
        PesiDtvFramework_unregistCallback ( DTVMessage.PESI_EVT_PT_DEBUG_CALLBACK_TEST, DTVMessage.PESI_EVT_PT_DEBUG_END, g_test_listener ) ;
        PesiDtvFramework_unregistCallback ( DTVMessage.PESI_EVT_CA_WIDEVINE, DTVMessage.PESI_EVT_CA_END, g_ca_listener ) ;//eric lin 20210107 widevine cas
    }

    private void PesiDtvFramework_registCallback( int callbackCmdStart, int callbackCmdEnd, IDTVListener scanListener)
    {
        for ( int i = callbackCmdStart ; i < callbackCmdEnd ; i++ )
            gDtvFramework.subScribeEvent(i,scanListener,0) ;
    }

    private void PesiDtvFramework_unregistCallback( int callbackCmdStart, int callbackCmdEnd, IDTVListener scanListener)
    {
        for ( int i = callbackCmdStart ; i < callbackCmdEnd ; i++ )
            gDtvFramework.unSubScribeEvent(i,scanListener) ;
    }

    private void register_callback(int callbackCmdStart, int callbackCmdEnd, IDTVListener scanListener) {
        for (int i = callbackCmdStart; i < callbackCmdEnd; i++) {
            gPrimeDtvMediaPlayer.subscribe_event(i, scanListener, 0);
        }
    }

    private void unregister_callback(int callbackCmdStart, int callbackCmdEnd, IDTVListener scanListener) {
        for (int i = callbackCmdStart; i < callbackCmdEnd; i++) {
            gPrimeDtvMediaPlayer.unsubscribe_event(i, scanListener);
        }
    }

    public void register_callbacks() {
        // legacy pesiDtvMediaPlayer
        register_callback(DTVMessage.PESI_SVR_EVT_AV_CALLBACK_START, DTVMessage.PESI_SVR_EVT_AV_CALLBACK_END, g_av_listener);
        register_callback(DTVMessage.PESI_SVR_EVT_EPG_CALLBACK_START, DTVMessage.PESI_SVR_EVT_EPG_CALLBACK_END, g_epg_listener);
        register_callback(DTVMessage.PESI_SVR_EVT_SRCH_CALLBACK_START, DTVMessage.PESI_SVR_EVT_SRCH_CALLBACK_END, g_scan_listener);
        register_callback(DTVMessage.PESI_SVR_EVT_BOOK_CALLBACK_START, DTVMessage.PESI_SVR_EVT_BOOK_CALLBACK_END, g_book_listener);
        register_callback(DTVMessage.PESI_SVR_EVT_PVR_CALLBACK_START, DTVMessage.PESI_SVR_EVT_PVR_CALLBACK_END, g_pvr_listener);
        register_callback(DTVMessage.PESI_SVR_EVT_TBM_CALLBACK_START, DTVMessage.PESI_SVR_EVT_TBM_CALLBACK_END, g_tbm_listener);
        register_callback(DTVMessage.PESI_SVR_CI_EVT_CALLBACK_START, DTVMessage.PESI_SVR_CI_EVT_CALLBACK_END, g_ci_listener);
        register_callback(DTVMessage.PESI_SVR_EVT_EWS_CALLBACK_START, DTVMessage.PESI_SVR_EVT_EWS_CALLBACK_END, g_ews_listener);
        register_callback(DTVMessage.PESI_SVR_EVT_VMX_CALLBACK_START, DTVMessage.PESI_SVR_EVT_VMX_CALLBACK_END, g_vmx_listener);//Scoty 20180831 add vmx callback event
        register_callback(DTVMessage.PESI_SVR_EVT_LOADERDTV_CALLBACK_START, DTVMessage.PESI_SVR_EVT_LOADERDTV_CALLBACK_END, g_loader_dtv_listener);
        register_callback(DTVMessage.PESI_EVT_PIO_CALLBACK_START, DTVMessage.PESI_EVT_PIO_CALLBACK_END, g_pio_listener);
        register_callback(DTVMessage.PESI_EVT_SYSTEM_CALLBACK_START, DTVMessage.PESI_EVT_SYSTEM_CALLBACK_END, g_system_listener);
        register_callback(DTVMessage.PESI_EVT_MTEST_START, DTVMessage.PESI_EVT_MTEST_END, g_mtest_listener);//Scoty 20190410 add Mtest Pc Tool callback
        register_callback(DTVMessage.PESI_EVT_PT_DEBUG_CALLBACK_TEST, DTVMessage.PESI_EVT_PT_DEBUG_END, g_test_listener);
        register_callback(DTVMessage.PESI_EVT_CA_WIDEVINE, DTVMessage.PESI_EVT_CA_END, g_ca_listener);//eric lin 20210107 widevine cas
        register_callback(DTVMessage.PESI_EVT_SERIES_UPDATE, DTVMessage.PESI_EVT_SERIES_END, g_series_listener);
        // tuner framework
        PesiDtvFramework_SubScribeEvent();
    }

    public void unregister_callbacks() {
        // legacy pesiDtvMediaPlayer
        unregister_callback(DTVMessage.PESI_SVR_EVT_AV_CALLBACK_START, DTVMessage.PESI_SVR_EVT_AV_CALLBACK_END, g_av_listener);
        unregister_callback(DTVMessage.PESI_SVR_EVT_EPG_CALLBACK_START, DTVMessage.PESI_SVR_EVT_EPG_CALLBACK_END, g_epg_listener);
        unregister_callback(DTVMessage.PESI_SVR_EVT_SRCH_CALLBACK_START, DTVMessage.PESI_SVR_EVT_SRCH_CALLBACK_END, g_scan_listener);
        unregister_callback(DTVMessage.PESI_SVR_EVT_BOOK_CALLBACK_START, DTVMessage.PESI_SVR_EVT_BOOK_CALLBACK_END, g_book_listener);
        unregister_callback(DTVMessage.PESI_SVR_EVT_PVR_CALLBACK_START, DTVMessage.PESI_SVR_EVT_PVR_CALLBACK_END, g_pvr_listener);
        unregister_callback(DTVMessage.PESI_SVR_EVT_TBM_CALLBACK_START, DTVMessage.PESI_SVR_EVT_TBM_CALLBACK_END, g_tbm_listener);
        unregister_callback(DTVMessage.PESI_SVR_CI_EVT_CALLBACK_START, DTVMessage.PESI_SVR_CI_EVT_CALLBACK_END, g_ci_listener);
        unregister_callback(DTVMessage.PESI_SVR_EVT_EWS_CALLBACK_START, DTVMessage.PESI_SVR_EVT_EWS_CALLBACK_END, g_ews_listener);
        unregister_callback(DTVMessage.PESI_SVR_EVT_VMX_CALLBACK_START, DTVMessage.PESI_SVR_EVT_VMX_CALLBACK_END, g_vmx_listener);//Scoty 20180831 add vmx callback event
        unregister_callback(DTVMessage.PESI_SVR_EVT_LOADERDTV_CALLBACK_START, DTVMessage.PESI_SVR_EVT_LOADERDTV_CALLBACK_END, g_loader_dtv_listener);
        unregister_callback(DTVMessage.PESI_EVT_PIO_CALLBACK_START, DTVMessage.PESI_EVT_PIO_CALLBACK_END, g_pio_listener);
        unregister_callback(DTVMessage.PESI_EVT_SYSTEM_CALLBACK_START, DTVMessage.PESI_EVT_SYSTEM_CALLBACK_END, g_system_listener);
        unregister_callback(DTVMessage.PESI_EVT_MTEST_START, DTVMessage.PESI_EVT_MTEST_END, g_mtest_listener);//Scoty 20190410 add Mtest Pc Tool callback
        unregister_callback(DTVMessage.PESI_EVT_PT_DEBUG_CALLBACK_TEST, DTVMessage.PESI_EVT_PT_DEBUG_END, g_test_listener);
        unregister_callback(DTVMessage.PESI_EVT_CA_WIDEVINE, DTVMessage.PESI_EVT_CA_END, g_ca_listener);//eric lin 20210107 widevine cas
        unregister_callback(DTVMessage.PESI_EVT_SERIES_UPDATE, DTVMessage.PESI_EVT_SERIES_END, g_series_listener);
        // tuner framework
        PesiDtvFramework_UnSubScribeEvent();
    }

    public ArrayList<List<SimpleChannel>> get_program_manager_total_channel_list() {
//        return gPrimeDtvMediaPlayer.get_program_manager_total_channel_list();
        return gDtvFramework.getProgramManagerTotalChannelList();
    }

    public ArrayList<FavGroupName> get_all_program_group() {
//        return gPrimeDtvMediaPlayer.get_all_program_group();
        return gDtvFramework.getAllProgramGroup();
    }

    /*
      video playback -s
      */
    public void pip_mod_set_display(Context context, Surface surface, int type) // 1:TimeShift, 0:View //gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    {
//        gPrimeDtvMediaPlayer.pip_mod_set_display(context, surface, type);
        gDtvFramework.pipModSetDisplay(context, surface, type);
    }

    // jim 2019/05/29 fix Android P set surface failed, using parcel to deliver Surface has some problem -s
    public void pip_mod_clear_display(Surface surface) {
//        gPrimeDtvMediaPlayer.pip_mod_clear_display(surface);
        gDtvFramework.pipModClearDisplay(surface);
    }

    public int comm_get_wind_handle() {
        return gPrimeDtvMediaPlayer.comm_get_wind_handle();
    }

    public int comm_get_timeshift_wind_handle() // Edwin 20181123 to get time shift window handle
    {
        return gPrimeDtvMediaPlayer.comm_get_timeshift_wind_handle();
    }

    public void set_surface_view(Context context, SurfaceView surfaceView, int index)//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    {
//        gPrimeDtvMediaPlayer.set_surface_view(context, surfaceView);
        LogUtils.d("set_surface_view "+ surfaceView);
        gDtvFramework.setSurfaceView(context,surfaceView, index);
        //setSurfaceToPlayer(index, surfaceView.getHolder());
    }
    public void set_surface_view(Context context, SurfaceView surfaceView)//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    {
//        gPrimeDtvMediaPlayer.set_surface_view(context, surfaceView);
        LogUtils.d("set_surface_view "+ surfaceView);
        gDtvFramework.setSurfaceView(context,surfaceView);
    }

    public void setSurfaceToPlayer(int index, SurfaceHolder surfaceHolder) {
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                Log.d(TAG, "index = +"+index+" surfaceCreated: " + holder.getSurface());
                gDtvFramework.setSurfaceToPlayer(index,holder.getSurface());
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                Log.d(TAG, "index = +"+index+" surfaceCreated: " + holder.getSurface());
                gDtvFramework.setSurfaceToPlayer(index,holder.getSurface());
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                Log.d(TAG, "surfaceDestroyed: " + holder.getSurface());
            }
        });
    }

    /*
      video playback -e
      */

    public void start_scan(TVScanParams sp) {
        gTvChCount = 0;
        gRadioChCount = 0;
        gAlreadySearchTpCount = 0;
//        gPrimeDtvMediaPlayer.start_scan(sp);
        gDtvFramework.startScan(sp);
    }

    public void stop_scan(boolean store) {
//        gPrimeDtvMediaPlayer.stop_scan(store);
        gDtvFramework.stopScan(store);
    }

    public void stopMonitorTable(long channel_id, int tuner_id){
        LogUtils.d("AutoUpdateManager channel_id = "+channel_id+" tuner id = "+tuner_id);
        gDtvFramework.stopMonitorTable(channel_id, tuner_id);
    }
    public void startMonitorTable(long channel_id, int isFcc, int tuner_id){
        LogUtils.d("AutoUpdateManager channel_id = "+channel_id+" isFcc = "+isFcc+" tuner_id = "+tuner_id);
        gDtvFramework.startMonitorTable(channel_id, isFcc,tuner_id);
    }

    public void UpdatePMT(long channel_id){

        gDtvFramework.UpdatePMT(channel_id);
    }

    public void StopDVBStubtitle(){
        gDtvFramework.StopDVBSubtitle();
    }

    public List<SatInfo> sat_info_get_list(int tunerType, int pos, int num) {
//        return gPrimeDtvMediaPlayer.sat_info_get_list(tunerType, pos, num);
        return gDtvFramework.satInfoGetList(tunerType, pos, num);
    }

    public SatInfo sat_info_get(int satId) {
//        return gPrimeDtvMediaPlayer.sat_info_get(satId);
        return gDtvFramework.satInfoGet(satId);
    }

    public int sat_info_add(SatInfo pSat) {
        return gDtvFramework.satInfoAdd(pSat);
//        return gPrimeDtvMediaPlayer.sat_info_add(pSat);
    }

    public int sat_info_update(SatInfo pSat) {
//        return gPrimeDtvMediaPlayer.sat_info_update(pSat);
        return gDtvFramework.satInfoUpdate(pSat);
    }

    public int sat_info_update_list(List<SatInfo> pSats) {
//        return gPrimeDtvMediaPlayer.sat_info_update_list(pSats);
        return gDtvFramework.satInfoUpdateList(pSats);
    }

    public int sat_info_delete(int satId) {
//        return gPrimeDtvMediaPlayer.sat_info_delete(satId);
        return gDtvFramework.satInfoDelete(satId);
    }

    public List<TpInfo> tp_info_get_list_by_satId(int tunerType, int satId, int pos, int num) {
//        return gPrimeDtvMediaPlayer.tp_info_get_list_by_satId(tunerType, satId, pos, num);
        return gDtvFramework.tpInfoGetListBySatId(tunerType, satId, pos, num);
    }

    public TpInfo tp_info_get(int tp_id) {
//        return gPrimeDtvMediaPlayer.tp_info_get(tp_id);
        return gDtvFramework.tpInfoGet(tp_id);
    }

    public int tp_info_add(TpInfo pTp) {
//        return gPrimeDtvMediaPlayer.tp_info_add(pTp);
        return gDtvFramework.tpInfoAdd(pTp);
    }

    public int tp_info_update(TpInfo pTp) {
//        return gPrimeDtvMediaPlayer.tp_info_update(pTp);
        return gDtvFramework.tpInfoUpdate(pTp);
    }

    public int tp_info_update_list(List<TpInfo> pTps) {
//        return gPrimeDtvMediaPlayer.tp_info_update_list(pTps);
        return gDtvFramework.tpInfoUpdateList(pTps);
    }

    public int tp_info_delete(int tpId) {
//        return gPrimeDtvMediaPlayer.tp_info_delete(tpId);
        return gDtvFramework.tpInfoDelete(tpId);
    }

    public int AvControlPlayByChannelIdFCC(int mode, List<Long> ch_id_list, List<Integer> tuner_id_list, boolean channelBlocked){
        return gDtvFramework.AvControlPlayByChannelIdFCC(mode, ch_id_list, tuner_id_list ,channelBlocked);
    }

    public int av_control_play_by_channel_id(int playId, long channelId, int groupType, int show) {
//        return gPrimeDtvMediaPlayer.av_control_play_by_channel_id(playId, channelId, groupType, show);
        return gDtvFramework.AvControlPlayByChannelId(playId, channelId, groupType, show);
    }

    public int av_control_pre_play_stop() {
//        return gPrimeDtvMediaPlayer.av_control_pre_play_stop();
        return gDtvFramework.AvControlPrePlayStop();
    }

    public int av_control_play_stop(int tuenrId, int mode, int stop_monitor_table) {
//        return gPrimeDtvMediaPlayer.av_control_play_stop(playId);
        LogUtils.d("[db_stopav]AvControlPlayStop playId = "+tuenrId);
        return gDtvFramework.AvControlPlayStop(tuenrId, mode, stop_monitor_table);
    }

    public int av_control_play_stop_all() {
//        return gPrimeDtvMediaPlayer.av_control_play_stop(playId);
//        LogUtils.d("AvControlPlayStop playId = "+playId);
        return gDtvFramework.AvControlPlayStopAll();
    }

    public int av_control_change_ratio_conversion(int playId, int ratio, int conversion) {
//        return gPrimeDtvMediaPlayer.av_control_change_ratio_conversion(playId, ratio, conversion);
        return gDtvFramework.AvControlChangeRatioConversion(playId, ratio, conversion);
    }

//    public int av_control_set_fast_change_channel(long PreChannelId, long NextChannelId)//Scoty 20180816 add fast change channel
//    {
////        return gPrimeDtvMediaPlayer.av_control_set_fast_change_channel(PreChannelId, NextChannelId);
//        return gDtvFramework.AvControlSetFastChangeChannel(PreChannelId, NextChannelId);
//    }

    public int av_control_set_fast_change_channel(int tunerId, long chId)
    {
        LogUtils.d(" IN ");
        return gDtvFramework.AvControlSetFastChangeChannel(tunerId, chId);
    }

    public int av_control_clear_fast_change_channel(int tunerId, long chId)
    {
        LogUtils.d("[FCC2] ");
        return gDtvFramework.AvControlClearFastChangeChannel(tunerId, chId);
    }

    public int av_control_change_resolution(int playId, int resolution) {
//        return gPrimeDtvMediaPlayer.av_control_change_resolution(playId, resolution);
        return gDtvFramework.AvControlChangeResolution(playId, resolution);
    }

    public int av_control_change_audio(int playId, AudioInfo.AudioComponent component) {
//        return gPrimeDtvMediaPlayer.av_control_change_audio(playId, component);
        return gDtvFramework.AvControlChangeAudio(playId, component);
    }

    public int av_control_set_mute(int playId, boolean mute) {
        Log.d(TAG, "av_control_set_mute: ");
//        return gPrimeDtvMediaPlayer.av_control_set_mute(playId, mute);
        return gDtvFramework.AvControlSetMute(playId, mute);
    }

    public int av_control_set_track_mode(int playId, EnAudioTrackMode stereo) {
//        return gPrimeDtvMediaPlayer.av_control_set_track_mode(playId, stereo);
        return gDtvFramework.AvControlSetTrackMode(playId, stereo);
    }

    public int av_control_audio_output(int playId, int byPass) {
//        return gPrimeDtvMediaPlayer.av_control_audio_output(playId, byPass);
        return gDtvFramework.AvControlAudioOutput(playId, byPass);
    }

    public int av_control_close(int playId) {
//        return gPrimeDtvMediaPlayer.av_control_close(playId);
        return gDtvFramework.AvControlClose(playId);
    }

    public int av_control_open(int playId) {
//        return gPrimeDtvMediaPlayer.av_control_open(playId);
        return gDtvFramework.AvControlOpen(playId);
    }

    public int av_control_show_video(int playId, boolean show) {
//        return gPrimeDtvMediaPlayer.av_control_show_video(playId, show);
        return gDtvFramework.AvControlShowVideo(playId, show);
    }

    public int av_control_freeze_video(int playId, boolean freeze) {
//        return gPrimeDtvMediaPlayer.av_control_freeze_video(playId, freeze);
        return gDtvFramework.AvControlFreezeVideo(playId, freeze);
    }

    //return value should be check
    public AudioInfo av_control_get_audio_list_info(int playId) {
//        return gPrimeDtvMediaPlayer.av_control_get_audio_list_info(playId);
        return gDtvFramework.AvControlGetAudioListInfo(playId);
    }

    /* return status =  LIVEPLAY,TIMESHIFTPLAY.....etc  */
    public int av_control_get_play_status(int playId) {
//        return gPrimeDtvMediaPlayer.av_control_get_play_status(playId);
        return gDtvFramework.AvControlGetPlayStatus(playId);
    }

    public int av_control_set_play_status(int status) {
//        return gPrimeDtvMediaPlayer.av_control_get_play_status(playId);
        return gDtvFramework.AvControlSetPlayStatus(status);
    }

    public boolean av_control_get_mute(int playId) {
//        return gPrimeDtvMediaPlayer.av_control_get_mute(playId);
        return gDtvFramework.AvControlGetMute(playId);
    }

    public EnAudioTrackMode av_control_get_track_mode(int playId) {
//        return gPrimeDtvMediaPlayer.av_control_get_track_mode(playId);
        return gDtvFramework.AvControlGetTrackMode(playId);
    }

    public int av_control_get_ratio(int playId) {
//        return gPrimeDtvMediaPlayer.av_control_get_ratio(playId);
        return gDtvFramework.AvControlGetRatio(playId);
    }

    public int av_control_set_stop_screen(int playId, int stopType) {
//        return gPrimeDtvMediaPlayer.av_control_set_stop_screen(playId, stopType);
        return gDtvFramework.AvControlSetStopScreen(playId, stopType);
    }

    public int av_control_get_stop_screen(int playId) {
//        return gPrimeDtvMediaPlayer.av_control_get_stop_screen(playId);
        return gDtvFramework.AvControlGetStopScreen(playId);
    }

    public int av_control_get_fps(int playId) {
//        return gPrimeDtvMediaPlayer.av_control_get_fps(playId);
        return gDtvFramework.AvControlGetFPS(playId);
    }

    public int av_control_ews_action_control(int playId, boolean enable) {
//        return gPrimeDtvMediaPlayer.av_control_ews_action_control(playId, enable);
        return gDtvFramework.AvControlEwsActionControl(playId, enable);
    }

    //input value should be check
    public int av_control_set_window_size(int playId, Rect rect) {
//        return gPrimeDtvMediaPlayer.av_control_set_window_size(playId, rect);
        return gDtvFramework.AvControlSetWindowSize(playId, rect);
    }

    //return value should be check
    public Rect av_control_get_window_size(int playId) {
//        return gPrimeDtvMediaPlayer.av_control_get_window_size(playId);
        return gDtvFramework.AvControlGetWindowSize(playId);
    }

    public int av_control_get_video_resolution_height(int playId) {
//        return gPrimeDtvMediaPlayer.av_control_get_video_resolution_height(playId);
        return gDtvFramework.AvControlGetVideoResolutionHeight(playId);
    }

    public int av_control_get_video_resolution_width(int playId) {
//        return gPrimeDtvMediaPlayer.av_control_get_video_resolution_width(playId);
        return gDtvFramework.AvControlGetVideoResolutionWidth(playId);
    }

    /* 0: dolby digital, 1: dolby digital plus */
    public int av_control_get_dolby_info_stream_type(int playId) {
//        return gPrimeDtvMediaPlayer.av_control_get_dolby_info_stream_type(playId);
        return gDtvFramework.AvControlGetDolbyInfoStreamType(playId);
    }

    /**
     * get dolby acmod.<br>
     *
     * @return 0: "1+1"; 1: "1/0"; 2: "2/0"; 3: "3/0"; 4:"2/1"; 5:"3/1"; 6:"2/2"; 7:"3/2"; other：error<br>
     */
    public int av_control_get_dolby_info_acmod(int playId) {
//        return gPrimeDtvMediaPlayer.av_control_get_dolby_info_acmod(playId);
        return gDtvFramework.AvControlGetDolbyInfoAcmod(playId);
    }

    public SubtitleInfo.SubtitleComponent av_control_get_current_subtitle(int playId) {
//        return gPrimeDtvMediaPlayer.av_control_get_current_subtitle(playId);
        return gDtvFramework.AvControlGetCurrentSubtitle(playId);
    }

    public SubtitleInfo av_control_get_subtitle_list(int playId) {
//        return gPrimeDtvMediaPlayer.av_control_get_subtitle_list(playId);
        return gDtvFramework.AvControlGetSubtitleList(playId);
    }

    public int av_control_select_subtitle(int playId, SubtitleInfo.SubtitleComponent subtitleComponent) {
//        return gPrimeDtvMediaPlayer.av_control_select_subtitle(playId, subtitleComponent);
        return gDtvFramework.AvControlSelectSubtitle(playId, subtitleComponent);
    }

    public int av_control_show_subtitle(int playId, boolean enable) {
//        return gPrimeDtvMediaPlayer.av_control_show_subtitle(playId, enable);
        return gDtvFramework.AvControlShowSubtitle(playId, enable);
    }

    public boolean av_control_is_subtitle_visible(int playId) {
//        return gPrimeDtvMediaPlayer.av_control_is_subtitle_visible(playId);
        return gDtvFramework.AvControlIsSubtitleVisible(playId);
    }

    public int av_control_set_subt_hoh_preferred(int playId, boolean on) {
//        return gPrimeDtvMediaPlayer.av_control_set_subt_hoh_preferred(playId, on);
        return gDtvFramework.AvControlSetSubtHiStatus(playId, on);
    }

    public int av_control_set_subtitle_language(int playId, int index, String lang) {
//        return gPrimeDtvMediaPlayer.av_control_set_subtitle_language(playId, index, lang);
        return gDtvFramework.AvControlSetSubtitleLanguage(playId, index, lang);
    }

    public TeletextInfo av_control_get_current_teletext(int playId) {
//        return gPrimeDtvMediaPlayer.av_control_get_current_teletext(playId);
        return gDtvFramework.AvControlGetCurrentTeletext(playId);
    }

    public List<TeletextInfo> av_control_get_teletext_list(int playId) {
//        return gPrimeDtvMediaPlayer.av_control_get_teletext_list(playId);
        return gDtvFramework.AvControlGetTeletextList(playId);
    }

    public int av_control_show_teletext(int playId, boolean enable) {
//        return gPrimeDtvMediaPlayer.av_control_show_teletext(playId, enable);
        return gDtvFramework.AvControlShowTeletext(playId, enable);
    }

    public boolean av_control_is_teletext_visible(int playId) {
//        return gPrimeDtvMediaPlayer.av_control_is_teletext_visible(playId);
        return gDtvFramework.AvControlIsTeletextVisible(playId);
    }

    public boolean av_control_is_teletext_available(int playId) {
//        return gPrimeDtvMediaPlayer.av_control_is_teletext_available(playId);
        return gDtvFramework.AvControlIsTeletextAvailable(playId);
    }

    public int av_control_set_teletext_language(int playId, String primeLang) {
//        return gPrimeDtvMediaPlayer.av_control_set_teletext_language(playId, primeLang);
        return gDtvFramework.AvControlSetTeletextLanguage(playId, primeLang);
    }

    public String av_control_get_teletext_language(int playId) {//eric lin 20180705 get ttx lang
//        return gPrimeDtvMediaPlayer.av_control_get_teletext_language(playId);
        return gDtvFramework.AvControlGetTeletextLanguage(playId);
    }

    public int av_control_set_command(int playId, int keyCode) {
//        return gPrimeDtvMediaPlayer.av_control_set_command(playId, keyCode);
        return gDtvFramework.AvControlSetCommand(playId, keyCode);
    }

    public int update_usb_software(String filename) {
//        return gPrimeDtvMediaPlayer.update_usb_software(filename);
        return gDtvFramework.UpdateUsbSoftWare(filename);
    }

    public int update_file_system_software(String pathAndFileName, String partitionName) {
//        return gPrimeDtvMediaPlayer.update_file_system_software(pathAndFileName, partitionName);
        return gDtvFramework.UpdateFileSystemSoftWare(pathAndFileName, partitionName);
    }

    public int update_ota_dvbc_software(int tpId, int freq, int symbol, int qam) {
//        return gPrimeDtvMediaPlayer.update_ota_dvbc_software(tpId, freq, symbol, qam);
        return gDtvFramework.UpdateOTADVBCSoftWare(tpId, freq, symbol, qam);
    }

    public int update_ota_dvbt_software(int tpId, int freq, int bandwith, int qam, int priority) {
//        return gPrimeDtvMediaPlayer.update_ota_dvbt_software(tpId, freq, bandwith, qam, priority);
        return gDtvFramework.UpdateOTADVBTSoftWare(tpId, freq, bandwith, qam, priority);
    }

    public int update_ota_dvbt2_software(int tpId, int freq, int bandwith, int qam, int channelmode) {
//        return gPrimeDtvMediaPlayer.update_ota_dvbt2_software(tpId, freq, bandwith, qam, channelmode);
        return gDtvFramework.UpdateOTADVBT2SoftWare(tpId, freq, bandwith, qam, channelmode);
    }

    public int update_ota_isdbt_software(int tpId, int freq, int bandwith, int qam, int priority) {
//        return gPrimeDtvMediaPlayer.update_ota_isdbt_software(tpId, freq, bandwith, qam, priority);
        return gDtvFramework.UpdateOTAISDBTSoftWare(tpId, freq, bandwith, qam, priority);
    }

    public int update_mtest_ota_software()//Scoty 20190410 add Mtest Trigger OTA command
    {
//        return gPrimeDtvMediaPlayer.update_mtest_ota_software();
        return gDtvFramework.UpdateMtestOTASoftWare();
    }

    public int get_dtv_timezone() {
//        return gPrimeDtvMediaPlayer.get_dtv_timezone();
        return gDtvFramework.getDtvTimeZone();
    }

    public int set_dtv_timezone(int zonesecond) {
//        return gPrimeDtvMediaPlayer.set_dtv_timezone(zonesecond);
        return gDtvFramework.setDtvTimeZone(zonesecond);
    }

    public int get_dtv_daylight()//value: 0(off) or 1(on)
    {
//        return gPrimeDtvMediaPlayer.get_dtv_daylight();
        return gDtvFramework.getDtvDaylight();
    }

    public int set_dtv_daylight(int onoff)//value: 0(off) or 1(on)
    {
//        return gPrimeDtvMediaPlayer.set_dtv_daylight(onoff);
        return gDtvFramework.setDtvDaylight(onoff);
    }

    public int get_setting_tdt_status() {
//        return gPrimeDtvMediaPlayer.get_setting_tdt_status();
        return gDtvFramework.getSettingTDTStatus();
    }

    public int set_setting_tdt_status(int onoff)//value: 0(off) or 1(on)
    {
//        return gPrimeDtvMediaPlayer.set_setting_tdt_status(onoff);
        return gDtvFramework.setSettingTDTStatus(onoff);
    }

    public int set_time_to_system(boolean bSetTimeToSystem) {
//        return gPrimeDtvMediaPlayer.set_time_to_system(bSetTimeToSystem);
        return gDtvFramework.setTimeToSystem(bSetTimeToSystem);
    }

    public Date second_to_date(int isecond) {
        return gPrimeDtvMediaPlayer.second_to_date(isecond);
    }

    public int date_to_second(Date date) {
        return gPrimeDtvMediaPlayer.date_to_second(date);
    }

    public int mtest_enable_opt(boolean enable) {
//        return gPrimeDtvMediaPlayer.mtest_enable_opt(enable);
        return gDtvFramework.MtestEnableOpt(enable);
    }

    public OTACableParameters dvb_get_ota_cable_paras() {
//        return gPrimeDtvMediaPlayer.dvb_get_ota_cable_paras();
        return gDtvFramework.DVBGetOTACableParas();
    }

    public OTATerrParameters dvb_get_ota_isdbt_paras() {
//        return gPrimeDtvMediaPlayer.dvb_get_ota_isdbt_paras();
        return gDtvFramework.DVBGetOTAIsdbtParas();
    }


    public OTATerrParameters dvb_get_ota_terrestrial_paras() {
//        return gPrimeDtvMediaPlayer.dvb_get_ota_terrestrial_paras();
        return gDtvFramework.DVBGetOTATerrestrialParas();
    }

    public OTATerrParameters dvb_get_ota_dvbt2_paras() {
//        return gPrimeDtvMediaPlayer.dvb_get_ota_dvbt2_paras();
        return gDtvFramework.DVBGetOTADVBT2Paras();
    }

    public List<BookInfo> init_ui_book_list() {//Init UI Book List after boot
//        return gPrimeDtvMediaPlayer.init_ui_book_list();
        return gDtvFramework.InitUIBookList();
    }

    public List<BookInfo> get_ui_book_list() {
//        return gPrimeDtvMediaPlayer.get_ui_book_list();
        return gDtvFramework.GetUIBookList();
    }

    public List<BookInfo> book_info_get_list() {
//        return gPrimeDtvMediaPlayer.book_info_get_list();
        return gDtvFramework.BookInfoGetList();
    }

    public BookInfo book_info_get(int bookId) {
//        return gPrimeDtvMediaPlayer.book_info_get(bookId);
        return gDtvFramework.BookInfoGet(bookId);
    }

    public int book_info_add(BookInfo bookInfo) {
//        return gPrimeDtvMediaPlayer.book_info_add(bookInfo);
        return gDtvFramework.BookInfoAdd(bookInfo);
    }

    public int book_info_update(BookInfo bookInfo) {
//        return gPrimeDtvMediaPlayer.book_info_update(bookInfo);
        return gDtvFramework.BookInfoUpdate(bookInfo);
    }

    public int book_info_update_list(List<BookInfo> bookList) {
//        return gPrimeDtvMediaPlayer.book_info_update_list(bookList);
        return gDtvFramework.BookInfoUpdateList(bookList);
    }

    public int book_info_delete(int bookId) {
//        return gPrimeDtvMediaPlayer.book_info_delete(bookId);
        return gDtvFramework.BookInfoDelete(bookId);
    }

    public void set_alarms(Context context, Calendar alarmCal, Intent  intent) {
        PendingIntent tempPendingIntent = null ;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        BookInfo bookInfo = new BookInfo(intent.getExtras());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String startCalFormatted = sdf.format(alarmCal.getTime());
        Log.d(TAG, "Start time: " + startCalFormatted + " intent " + intent );

        tempPendingIntent = PendingIntent.getBroadcast(context, bookInfo.getBookId(), intent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmCal.getTimeInMillis(), tempPendingIntent);
    }

    public void set_alarms(Context context, Intent  intent) {
        PendingIntent tempPendingIntent = null ;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        BookInfo bookInfo = new BookInfo(intent.getExtras());
        Calendar alarmCal = Calendar.getInstance();
        alarmCal.set(bookInfo.getYear(),
                bookInfo.getMonth() - 1, // bookinfo month = 1 ~ 12, -1 for Calendar
                bookInfo.getDate(),
                bookInfo.getStartTime() / 100,
                bookInfo.getStartTime() % 100,
                0);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String startCalFormatted = sdf.format(alarmCal.getTime());
        Log.d(TAG, "Start time: " + startCalFormatted + " intent " + intent );

        tempPendingIntent = PendingIntent.getBroadcast(context, bookInfo.getBookId(), intent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmCal.getTimeInMillis(), tempPendingIntent);
    }

    public void cancel_alarms(Context context, Intent  intent) {
        BookInfo bookInfo = new BookInfo(intent.getExtras());
        PendingIntent temp_pending_intent = PendingIntent.getBroadcast(context, bookInfo.getBookId(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(temp_pending_intent);
    }

    public int book_info_delete_all() {
//        return gPrimeDtvMediaPlayer.book_info_delete_all();
        return gDtvFramework.BookInfoDeleteAll();
    }

    public BookInfo book_info_get_coming_book() {
//        return gPrimeDtvMediaPlayer.book_info_get_coming_book();
        return gDtvFramework.BookInfoGetComingBook();
    }

    public List<BookInfo> book_info_find_conflict_records(BookInfo bookInfo) {
//        return gPrimeDtvMediaPlayer.book_info_find_conflict_books(bookInfo);
        return gDtvFramework.BookInfoFindConflictBooks(bookInfo);
    }

    public List<BookInfo> book_info_find_conflict_reminds(BookInfo bookInfo) {
        return new ArrayList<>();
    }

    public void book_info_save(BookInfo bookInfo) {
        gDtvFramework.BookInfoSave(bookInfo);
    }

    public void schedule_next_timer(BookInfo bookInfo) {
        if (bookInfo == null) {
            Log.e(TAG, "schedule_next_timer: bookInfo == null");
            return;
        }

        // update date in bookinfo
        int bookInfoId = bookInfo.getBookId();
        set_timer_next_date_by_cycle(bookInfoId, bookInfo);

        // set next alarm
        bookInfo = book_info_get(bookInfoId); // get updated bookinfo
        if (bookInfo != null) {
            Intent intent = bookInfo.get_Intent();
            if (bookInfo.getBookCycle() == BookInfo.BOOK_CYCLE_SERIES_EMPTY) {
                // cycle = BOOK_CYCLE_SERIES_EMPTY
                // set next alarm to start later to check again
                Log.d(TAG, "schedule_next_timer: BOOK_CYCLE_SERIES_EMPTY! adjust alarm time...");
                Calendar calendar = Calendar.getInstance(); // now
                calendar.add(Calendar.MINUTE, Pvcfg.SERIES_CHECK_MINUTE);
                set_alarms(mContext, calendar, intent);
            }
            else {
                set_alarms(mContext, intent);
            }
        }
    }

    public void schedule_next_timer(Intent bookIntent) {
        Bundle bookBundle = bookIntent.getExtras();
        if (bookBundle == null) {
            Log.e(TAG, "schedule_next_timer: bundle == null");
            return;
        }

        BookInfo bookInfo = new BookInfo(bookBundle);
        schedule_next_timer(bookInfo);
    }

    public void schedule_all_timers() {
        List<BookInfo> bookInfoList = book_info_get_list();
        for (BookInfo bookInfo : bookInfoList) {
            schedule_next_timer(bookInfo);
        }
    }

    public int tuner_lock(TVTunerParams tunerParams) {
//        return gPrimeDtvMediaPlayer.tuner_lock(tunerParams);
        return gDtvFramework.tunerLock(tunerParams);
    }

    public boolean get_tuner_status(int tuner_id) {
        if (tuner_id < 0)
            return false;
//        return gPrimeDtvMediaPlayer.get_tuner_status(tuner_id);
        return gDtvFramework.getTunerStatus(tuner_id);
    }

    public int get_signal_strength(int nTunerID) {
//        return gPrimeDtvMediaPlayer.get_signal_strength(nTunerID);
        return gDtvFramework.getSignalStrength(nTunerID);
    }

    public int get_signal_quality(int nTunerID) {
//        return gPrimeDtvMediaPlayer.get_signal_quality(nTunerID);
        return gDtvFramework.getSignalQuality(nTunerID);
    }

    public int get_signal_snr(int nTunerID) {
//        return gPrimeDtvMediaPlayer.get_signal_snr(nTunerID);
        return gDtvFramework.getSignalSNR(nTunerID);
    }

    public int get_signal_ber(int nTunerID) {
//        return gPrimeDtvMediaPlayer.get_signal_ber(nTunerID);
        return gDtvFramework.getSignalBER(nTunerID);
    }

    public int set_fake_tuner(int openFlag)//Scoty 20180809 add fake tuner command
    {
//        return gPrimeDtvMediaPlayer.set_fake_tuner(openFlag);
        return gDtvFramework.setFakeTuner(openFlag);
    }

    public int tuner_set_antenna_5v(int tuner_id, int onOff) {
//        return gPrimeDtvMediaPlayer.tuner_set_antenna_5v(tuner_id, onOff);
        return gDtvFramework.tunerSetAntenna5V(tuner_id, onOff);
    }

    public int get_tuner_type() {
//        return gPrimeDtvMediaPlayer.get_tuner_type();
        return gDtvFramework.getTunerType();
    }

    public int save_table(EnTableType tableType) {
//        return gPrimeDtvMediaPlayer.save_table(tableType);
        return gDtvFramework.saveTable(tableType);
    }

    public int clear_table(EnTableType tableType) {
//        return gPrimeDtvMediaPlayer.clear_table(tableType);
        Log.e(TAG,"this function not porting!! please check");
        return 0;
    }

    public int restore_table(EnTableType tableType) {
//        return gPrimeDtvMediaPlayer.restore_table(tableType);
        Log.e(TAG,"this function not porting!! please check");
        return 0;
    }

    public int save_networks() {
        return save_table(EnTableType.ALL);
    }

    public int get_default_open_group_type() {
//        return gPrimeDtvMediaPlayer.get_default_open_group_type();
        Log.e(TAG,"this function not porting!! please check");
        return 0;
    }

    public int get_channel_count(int groupType) {
//        return gPrimeDtvMediaPlayer.get_channel_count(groupType);
        Log.e(TAG,"this function not porting!! please check");
        return 0;
    }

    public List<Integer> get_use_groups(EnUseGroupType useGroupType) {
//        return gPrimeDtvMediaPlayer.get_use_groups(useGroupType);
        Log.e(TAG,"this function not porting!! please check");
        return null;
    }

    public int rebuild_all_group() {
//        return gPrimeDtvMediaPlayer.rebuild_all_group();
        Log.e(TAG,"this function not porting!! please check");
        return 0;
    }

    public String get_channel_group_name(int groupType) {
//        return gPrimeDtvMediaPlayer.get_channel_group_name(groupType);
        Log.e(TAG,"this function not porting!! please check");
        return null;
    }

    public int set_channel_group_name(int groupType, String name) {
//        return gPrimeDtvMediaPlayer.set_channel_group_name(groupType, name);
        Log.e(TAG,"this function not porting!! please check");
        return 0;
    }

    public int del_channel_by_tag(int u32ProgTag) {
//        return gPrimeDtvMediaPlayer.del_channel_by_tag(u32ProgTag);
        Log.e(TAG,"this function not porting!! please check");
        return 0;
    }

    public List<ProgramInfo> get_program_info_list(int type, int pos, int num) {
        LogUtils.d("DataManager");
//        return gPrimeDtvMediaPlayer.get_program_info_list(type, pos, num);
        return gDtvFramework.getProgramInfoList(type, pos, num);
    }

    //Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
    public List<SimpleChannel> get_simple_program_list_from_total_channel_list(int type, int IncludeSkipFlag, int IncludePVRSkipFlag) {
//        return gPrimeDtvMediaPlayer.get_simple_program_list_from_total_channel_list(type, IncludeSkipFlag, IncludePVRSkipFlag);
        return gDtvFramework.getSimpleProgramListfromTotalChannelList(type, IncludeSkipFlag, IncludePVRSkipFlag);
    }

    public List<SimpleChannel> get_simple_program_list(int type, int IncludeSkipFlag, int IncludePVRSkipFlag) {
//        return gPrimeDtvMediaPlayer.get_simple_program_list(type, IncludeSkipFlag, IncludePVRSkipFlag);
        return gDtvFramework.getSimpleProgramList(type, IncludeSkipFlag, IncludePVRSkipFlag);
    }

    public ProgramInfo get_program_by_service_id(int service_id) {
//        return gPrimeDtvMediaPlayer.get_program_by_lcn(lcn, type);
        return gDtvFramework.getProgramByServiceId(service_id);
    }

    public ProgramInfo get_program_by_service_id_transport_stream_id(int service_id, int ts_id) {
//        return gPrimeDtvMediaPlayer.get_program_by_lcn(lcn, type);
        return gDtvFramework.getProgramByServiceIdTransportStreamId(service_id, ts_id);
    }

    public ProgramInfo get_program_by_lcn(int lcn, int type) {
//        return gPrimeDtvMediaPlayer.get_program_by_lcn(lcn, type);
        return gDtvFramework.getProgramByLcn(lcn, type);
    }

    public ProgramInfo get_program_by_ch_num(int chnum, int type) {
//        return gPrimeDtvMediaPlayer.get_program_by_ch_num(chnum, type);
        return gDtvFramework.getProgramByChnum(chnum, type);
    }

    public ProgramInfo get_program_by_channel_id(long channelId) {
//        return gPrimeDtvMediaPlayer.get_program_by_channel_id(channelId);
        return gDtvFramework.getProgramByChannelId(channelId);
    }

    public SimpleChannel get_simple_program_by_channel_id(long channelId) {
//        return gPrimeDtvMediaPlayer.get_simple_program_by_channel_id(channelId);
        return gDtvFramework.getSimpleProgramByChannelId(channelId);
    }

    public SimpleChannel get_simple_program_by_channel_id_from_total_channel_list_by_group(int groupType, long channelId) {
//        return gPrimeDtvMediaPlayer.get_simple_program_by_channel_id_from_total_channel_list_by_group(groupType, channelId);
        return gDtvFramework.getSimpleProgramByChannelIdfromTotalChannelListByGroup(groupType, channelId);
    }

    public SimpleChannel get_simple_program_by_channel_id_from_total_channel_list(long channelId) {

//        return gPrimeDtvMediaPlayer.get_simple_program_by_channel_id_from_total_channel_list(channelId);
        return gDtvFramework.getSimpleProgramByChannelIdfromTotalChannelList(channelId);

    }

    public void delete_program(long channelId) {
//        gPrimeDtvMediaPlayer.delete_program(channelId);
        gDtvFramework.deleteProgram(channelId);
    }

    public int set_default_open_channel(long channelId, int groupType) {
//        return gPrimeDtvMediaPlayer.set_default_open_channel(channelId, groupType);
        return gDtvFramework.setDefaultOpenChannel(channelId, groupType);
    }

    public DefaultChannel get_default_channel() {
//        return gPrimeDtvMediaPlayer.get_default_channel();
        return gDtvFramework.getDefaultChannel();
    }

    public int update_sat_list(List<SatInfo> satInfoList) {
//        return gPrimeDtvMediaPlayer.update_sat_list(satInfoList);
        return gDtvFramework.satInfoUpdateList(satInfoList);
    }

    public int update_tp_list(List<TpInfo> tpInfoList) {
//        return gPrimeDtvMediaPlayer.update_tp_list(tpInfoList);
        return gDtvFramework.tpInfoUpdateList(tpInfoList);
    }

    public int update_simple_channel_list(List<SimpleChannel> simpleChannelList, int type) {
//        return gPrimeDtvMediaPlayer.update_simple_channel_list(simpleChannelList, type);
        return gDtvFramework.updateSimpleChannelList(simpleChannelList, type);
    }

    public int update_program_info(ProgramInfo pProgram) {
//        return gPrimeDtvMediaPlayer.update_program_info(pProgram);
        return gDtvFramework.updateProgramInfo(pProgram);
    }

    public int fav_info_update_list(int favMode, List<FavInfo> favInfo) {
//        return gPrimeDtvMediaPlayer.fav_info_update_list(favInfo);
        return gDtvFramework.favInfoUpdateList(favMode, favInfo);
    }

    public int update_book_list(List<BookInfo> bookInfoList) // Need command and implement
    {
//        return gPrimeDtvMediaPlayer.update_book_list(bookInfoList);
        return gDtvFramework.BookInfoUpdateList(bookInfoList);
    }

    // ========EPG==========
    private final ConcurrentHashMap<Object, List<EPGEvent>> gEpgEventMap = new ConcurrentHashMap<>();
    private final Timer gTimer = new Timer();
    private final Object gThreadLock = new Object();
    private long gPresentEndTime;
    private ExecutorService gExecutors;
    private Thread gCurrentThread = null;
    private TimerTask gTimerTask;
    private TimerTask gTaskUpdateNetworkTime;
    private Date gNetworkTime;
    public EPGEvent gPresent, gFollow;
    private static final Object mEPGEventLock = new Object();


    public int start_epg(long channelID) {
        // epg use pesi c service, not java
        return gPrimeDtvMediaPlayer.start_epg(channelID);
//        Log.e(TAG,"this function not porting!! please check");
//        return 0;
    }

    public void build_network_time() {
        if (gTaskUpdateNetworkTime != null)
            return;
        gTaskUpdateNetworkTime = new TimerTask() {
            @Override
            public void run() {
                gNetworkTime = Utils.get_network_time();
            }
        };
        gTimer.schedule(gTaskUpdateNetworkTime, 0, 60 * 1000);
    }

    public void build_epg_event_map(Context context) {

        if (!Pvcfg.getEnableEPGEventMap()) {
            Log.w(TAG, "build_epg_event_map: EPG Event Map is disabled");
            return;
        }

        try { // interrupt task ( build_schedule_map )
            if (null != gTimerTask) {
                Log.w(TAG, "build_epg_event_map: interrupt task ( build_schedule_map )");
                gTimerTask.cancel();
                gTimer.purge();
                if (gCurrentThread != null && gCurrentThread.isAlive()) {
                    gCurrentThread.interrupt();
                    gCurrentThread.join(5000);
                    if (gCurrentThread.isAlive())
                        Log.w(TAG, "build_epg_event_map: thread is not terminated");
                    else
                        Log.w(TAG, "build_epg_event_map: thread is terminated");
                }
                gExecutors.shutdownNow();
                //return;
            }
        } catch (Exception e) { Log.e(TAG, "build_epg_event_map: " + e.getMessage()); }

        // load from preference
        load_schedule_map(context);

        gTimerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    build_schedule_map(context);
                }
                catch (InterruptedException e) {
                    Log.e(TAG, "build_epg_event_map: Error build EPG Event Map: " + e.getMessage());
                }
            }
        };
        gTimer.schedule(gTimerTask, 0, 60 * 60 * 1000);
    }

    /** @noinspection ResultOfMethodCallIgnored*/
    private void build_schedule_map(Context context) throws InterruptedException {
        synchronized (gThreadLock) {
            gCurrentThread = new Thread(() -> {
                Log.i(TAG, "build_schedule_map: START");
                gExecutors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
                //gExecutors = Executors.newCachedThreadPool();
                List<ProgramInfo> allChannels = new ArrayList<>();
                allChannels.addAll(get_program_info_list(FavGroup.ALL_TV_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.NUM_ALL));
                allChannels.addAll(get_program_info_list(FavGroup.ALL_RADIO_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.NUM_ALL));
                allChannels.sort(Comparator.comparingInt(ProgramInfo::getDisplayNum));

                for (ProgramInfo channel : allChannels) {
                    gExecutors.submit(() -> {
                        long channelID = channel.getChannelId();
                        retry_get_schedule(context, channelID);
                    });
                }

                try {
                    gExecutors.shutdown();
                    gExecutors.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                    save_schedule_map(context);
                }
                catch (InterruptedException e) { // Handle interrupted exception
                    gExecutors.shutdownNow();
                    Thread.currentThread().interrupt();
                    Log.w(TAG, "build_schedule_map: Stop build schedule map, error await termination: " + e.getMessage());
                }
                finally { // Make sure executor is shut down even if an exception occurs
                    if (!gExecutors.isShutdown())
                        gExecutors.shutdownNow();
                }

                Log.i(TAG, "build_schedule_map: END");

            }, "EPG: build_schedule_map()");

            gCurrentThread.start();
        }
    }

    private void retry_get_schedule(Context context, long channelID) {
        ProgramInfo channel = get_program_by_channel_id(channelID);
        String channelNum = channel != null ? channel.getDisplayNum(3) : "unknown";
        int retryCount = 0;
        int maxRetries = Integer.MAX_VALUE;
        long retryDelayMillis = 1000;

        if (null == channel)
            return;

        while (retryCount < maxRetries) {
            final int[] cpuUsage = {0};
            final int   cpuThreshold = 75;

            if (Thread.currentThread().isInterrupted()) {
                Log.w(TAG, "retry_get_schedule: interrupted");
                return;
            }

            List<EPGEvent> eventList = gEpgEventMap.get(channelID);
            if (eventList!= null && !eventList.isEmpty()) {
                for (EPGEvent epgEvent : eventList) {
                    int index = eventList.indexOf(epgEvent);
                    long currentTime = get_current_time();
                    if (index != eventList.size() - 1
                        && currentTime >= epgEvent.get_start_time()
                        && currentTime < epgEvent.get_end_time()) {
                        if (DEBUG_EPG_EVENT_MAP)
                            Log.d(TAG, "retry_get_schedule: EPGEvent is in schedule map, skip get schedule for [channel num] " + channelNum);
                        return;
                    }
                }
            }

            if (low_cpu_usage(cpuUsage, cpuThreshold)) {
                Date dateStart = get_start_date();
                Date dateEnd = get_end_date();
                List<EPGEvent> scheduleList = get_epg_events(channelID, dateStart, dateEnd, 0, 100, 0);
                if (DEBUG_EPG_EVENT_MAP)
                    Log.d(TAG, "retry_get_schedule: [channel num] " + channelNum
                            + ", [cpu usage] " + cpuUsage[0]
                            + ", [schedule size] " + scheduleList.size()
                            + ", [time scope] " + MiniEPG.ms_to_time(dateStart.getTime(), "MM/dd HH:mm") + " - " + MiniEPG.ms_to_time(dateEnd.getTime(), "MM/dd HH:mm"));

                if (!scheduleList.isEmpty())
                    add_to_schedule_map(channelID, scheduleList);
                return;
            }
            else {
                if (DEBUG_EPG_EVENT_MAP)
                    Log.w(TAG, "retry_get_schedule: System busy, retrying for [channel num] " + channelNum + ", [cpu usage] " + cpuUsage[0] + ", [retry count] " + retryCount);
                retryCount++;
                try {
                    Thread.sleep(retryDelayMillis);
                }
                catch (InterruptedException e) {
                    Log.w(TAG, "retry_get_schedule: interrupt this thread (retry_get_schedule)");
                    return;
                }
            }
        }
    }

    /*private void save_single_epg_data(Context context, long channelID, List<EPGEvent> scheduleList) {
        synchronized (gEpgEventMap) {
            gEpgEventMap.put(channelID, scheduleList);
            new Thread(() -> {
                SharedPreferences prefs = context.getSharedPreferences("EPG_PREFS", Context.MODE_PRIVATE);
                Gson gson = new Gson();

                // load existing data
                String existingJson = prefs.getString("EPG_DATA", "{}");
                Type type = new TypeToken<HashMap<Long, List<EPGEvent>>>(){}.getType();
                HashMap<Long, List<EPGEvent>> existingMap = gson.fromJson(existingJson, type);

                // put one schedule list
                existingMap.put(channelID, scheduleList);

                // save updated data
                prefs.edit().putString("EPG_DATA", gson.toJson(existingMap)).apply();
            }).start();
        }
    }*/

    private void save_schedule_map(Context context) {
        if (!Pvcfg.getEnableEPGEventMap()) {
            Log.w(TAG, "save_schedule_map: EPG Event Map is disabled");
            return;
        }
        if (DEBUG_EPG_EVENT_MAP)
            Log.d(TAG, "save_schedule_map: save to preference EPG_PREFS");

        if (false) {
            List<ProgramInfo> allChannels = new ArrayList<>();
            allChannels.addAll(get_program_info_list(FavGroup.ALL_TV_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.NUM_ALL));
            allChannels.addAll(get_program_info_list(FavGroup.ALL_RADIO_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.NUM_ALL));
            allChannels.sort(Comparator.comparingInt(ProgramInfo::getDisplayNum));
            allChannels.forEach(programInfo -> {
                long channelID = programInfo.getChannelId();
                List<EPGEvent> scheduleList = gEpgEventMap.get(channelID);
                if (null == scheduleList)
                    scheduleList = new ArrayList<>();
                Log.d(TAG, "save_schedule_map: channel num = " + programInfo.getDisplayNum(3) + ", schedule size = " + scheduleList.size());
            });
        }

        new Thread(() -> {
            SharedPreferences prefs = context.getSharedPreferences("EPG_PREFS", Context.MODE_PRIVATE);
            Gson gson = new Gson();
            Map<Object, List<EPGEvent>> copyMap = new HashMap<>();

            synchronized(gEpgEventMap) {
                for (Map.Entry<Object, List<EPGEvent>> entry : gEpgEventMap.entrySet()) {
                    List<EPGEvent> originalList = entry.getValue();
                    List<EPGEvent> copyList;
                    synchronized (originalList) {
                        copyList = new ArrayList<>(originalList);
                    }
                    copyMap.put(entry.getKey(), copyList);
                    //copyMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                }
            }
            prefs.edit().putString("EPG_DATA", gson.toJson(copyMap)).apply();
        }).start();
    }

    public void load_schedule_map(Context context) {
        Log.d(TAG, "load_schedule_map: load from preference EPG_PREFS");
        SharedPreferences prefs = context.getSharedPreferences("EPG_PREFS", Context.MODE_PRIVATE);
        String json = prefs.getString("EPG_DATA", null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<HashMap<Long, List<EPGEvent>>>(){}.getType();
            HashMap<Long, List<EPGEvent>> loadedMap = gson.fromJson(json, type);
            synchronized (gEpgEventMap) {
                gEpgEventMap.clear();
                gEpgEventMap.putAll(loadedMap);
            }

            if (DEBUG_EPG_EVENT_MAP) {
                List<ProgramInfo> allChannels = new ArrayList<>();
                allChannels.addAll(get_program_info_list(FavGroup.ALL_TV_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.NUM_ALL));
                allChannels.addAll(get_program_info_list(FavGroup.ALL_RADIO_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.NUM_ALL));
                allChannels.sort(Comparator.comparingInt(ProgramInfo::getDisplayNum));

                for (Map.Entry<Object, List<EPGEvent>> entry : gEpgEventMap.entrySet()) {
                    Object key = entry.getKey();
                    List<EPGEvent> eventList = entry.getValue();
                    final ProgramInfo[] channel = {null};
                    allChannels.forEach(programInfo -> {
                        if (programInfo.getChannelId() == (long) key)
                            channel[0] = programInfo;
                    });
                    Log.d(TAG, "load_schedule_map: key = " + key + ", channel = " + (channel[0] == null ? "000" : channel[0].getDisplayNum(3)) + ", eventList size = " + eventList.size() + ", first event = " + (eventList.isEmpty() ? "null" : eventList.get(0).get_event_name()));
                }
            }

            if (false) {
                List<ProgramInfo> allChannels = new ArrayList<>();
                allChannels.addAll(get_program_info_list(FavGroup.ALL_TV_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.NUM_ALL));
                allChannels.addAll(get_program_info_list(FavGroup.ALL_RADIO_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.NUM_ALL));
                allChannels.sort(Comparator.comparingInt(ProgramInfo::getDisplayNum));
                allChannels.forEach(programInfo -> {
                    long channelID = programInfo.getChannelId();
                    String channelNum = programInfo.getDisplayNum(3);
                    List<EPGEvent> scheduleList = gEpgEventMap.get(channelID);
                    if (null == scheduleList)
                        scheduleList = new ArrayList<>();
                    Log.e(TAG, "load_schedule_map: channel num = " + channelNum + ", schedule size = " + scheduleList.size() + ", first event = " + (scheduleList.isEmpty() ? "null" : scheduleList.get(0).get_event_name()));
                });
            }
        }
    }

    /** @noinspection CommentedOutCode*/
    private void add_to_schedule_map(long channelID, EPGEvent newEvent) {
        /*if (!Pvcfg.getEnableEPGEventMap()) {
            Log.w(TAG, "add_to_schedule_map: EPG Event Map is disabled");
            return;
        }*/

        List<EPGEvent> scheduleList = gEpgEventMap.get(channelID);
        if (null == scheduleList)
            scheduleList = new ArrayList<>();

        boolean hasSameEvent = false;
        for (EPGEvent epgEvent : scheduleList) {
            if (epgEvent.get_event_name().equals(newEvent.get_event_name()) &&
                epgEvent.get_start_time() == newEvent.get_start_time() &&
                epgEvent.get_end_time() == newEvent.get_end_time()) {
                hasSameEvent = true;
                break;
            }
        }

        if (!hasSameEvent) {
            scheduleList.add(newEvent);
            gEpgEventMap.put(channelID, scheduleList);
            save_schedule_map(mContext.getApplicationContext());
        }
    }

    private void add_to_schedule_map(long channelID, List<EPGEvent> newScheduleList) {
        if (newScheduleList.isEmpty())
            return;

        List<EPGEvent> scheduleList = gEpgEventMap.get(channelID);
        if (null == scheduleList)
            scheduleList = new ArrayList<>();

        boolean hasSameEvent = false;
        for (EPGEvent newEvent : newScheduleList) {
            // check if the new event is already in the old list
            for (EPGEvent oldEvent : scheduleList) {
                if (oldEvent.get_event_name().equals(newEvent.get_event_name()) &&
                    oldEvent.get_start_time() == newEvent.get_start_time() &&
                    oldEvent.get_end_time() == newEvent.get_end_time()) {
                    hasSameEvent = true;
                    break;
                }
            }
            // add the new event to the old list
            if (!hasSameEvent) {
                scheduleList.add(newEvent);
                if (DEBUG_EPG_EVENT_MAP)
                    Log.d(TAG, "add_to_schedule_map: add new event: " + newEvent.get_event_name()
                            + ", [start ~ end] " + MiniEPG.ms_to_time(newEvent.get_start_time(), "MM/dd HH:mm")
                            + " - " + MiniEPG.ms_to_time(newEvent.get_end_time(), "HH:mm"));
            }
            hasSameEvent = false;
        }

        // save schedule map to preference
        synchronized (gEpgEventMap) {
            gEpgEventMap.put(channelID, scheduleList);
            save_schedule_map(mContext.getApplicationContext());
        }
    }

    /** @noinspection SameParameterValue*/
    private boolean low_cpu_usage(int[] usage, int threshold) {
        try {
            RandomAccessFile statFile1 = new RandomAccessFile("/proc/stat", "r");
            String[] stats1 = statFile1.readLine().split("\\s+");
            statFile1.close();

            long idle1 = Long.parseLong(stats1[4]) + Long.parseLong(stats1[5]);
            long total1 = Long.parseLong(stats1[1]) + Long.parseLong(stats1[2])
                        + Long.parseLong(stats1[3]) + Long.parseLong(stats1[6])
                        + Long.parseLong(stats1[7]) + Long.parseLong(stats1[8]) + idle1;

            Thread.sleep(1000);

            RandomAccessFile statFile2 = new RandomAccessFile("/proc/stat", "r");
            String[] stats2 = statFile2.readLine().split("\\s+");
            statFile2.close();

            long idle2 = Long.parseLong(stats2[4]) + Long.parseLong(stats2[5]);
            long total2 = Long.parseLong(stats2[1]) + Long.parseLong(stats2[2])
                        + Long.parseLong(stats2[3]) + Long.parseLong(stats2[6])
                        + Long.parseLong(stats2[7]) + Long.parseLong(stats2[8]) + idle2;

            float cpuUsage = 100 * ((total2 - total1) - (idle2 - idle1)) / (float)(total2 - total1);
            usage[0] = (int) cpuUsage;
            //Log.e(TAG, "is_system_idle: [cpuUsage] " + cpuUsage + " < " + threshold + " : " + (cpuUsage < threshold));
            return cpuUsage < threshold;
        }
        catch (Exception e) {
            Log.e(TAG, "is_system_idle: Error reading /proc/stat: " + e.getMessage());
        }
        return false;
    }

    public EPGEvent get_schedule_present_event(long channelID) {
        List<EPGEvent> scheduleList = gEpgEventMap.get(channelID);

        if (scheduleList != null && !scheduleList.isEmpty()) {
            for (EPGEvent presentEvent : scheduleList) {
                if (is_correct_present(presentEvent)) {
                    Log.d(TAG, "get_schedule_present_event: present event name: " + presentEvent.get_event_name());
                    return presentEvent;
                }
            }
        }
        return null;
    }

    public EPGEvent get_schedule_follow_event(long channelID) {
        List<EPGEvent> scheduleList = gEpgEventMap.get(channelID);
        List<EPGEvent> newList = new ArrayList<>();
        EPGEvent OK_EpgEvent = null;

        if (scheduleList != null && !scheduleList.isEmpty()) {
            for (EPGEvent followEvent : scheduleList) {
                if (is_correct_follow(followEvent)) {
                    Log.d(TAG, "get_schedule_follow_event: follow event name: " + followEvent.get_event_name());
                    newList.add(followEvent);
                    if (OK_EpgEvent == null)
                        OK_EpgEvent = followEvent;
                }
            }
            gEpgEventMap.put(channelID, newList);
            return OK_EpgEvent;
        }
        return null;
    }

    public EPGEvent get_present_event(long channelID) {
        // epg use pesi c service, not java
        synchronized(mEPGEventLock) {
            long currentTime = get_current_time();

            gPresent = get_schedule_present_event(channelID);
            if (gPresent != null) {
                Log.d(TAG, "get_present_event: (from schedule map) " +
                        "[present event] " + (gPresent == null ? "NULL" : gPresent.get_event_name() +
                        ", [start ~ end] " + MiniEPG.ms_to_time(gPresent.get_start_time(), "MM/dd HH:mm") + " - " + MiniEPG.ms_to_time(gPresent.get_end_time(), "HH:mm") +
                        ", [current time] " + MiniEPG.ms_to_time(currentTime, "MM/dd HH:mm")));
                return gPresent;
            }
            gPresent = gPrimeDtvMediaPlayer.get_present_event(channelID);
            if (gPresent != null/* || is_correct_present(gPresent)*/) {
                Log.d(TAG, "get_present_event: (from Dtv Service) " +
                        "[present event] " + (gPresent == null ? "NULL" : gPresent.get_event_name() +
                        ", [start ~ end] " + MiniEPG.ms_to_time(gPresent.get_start_time(), "MM/dd HH:mm") + " - " + MiniEPG.ms_to_time(gPresent.get_end_time(), "HH:mm") +
                        ", [current time] " + MiniEPG.ms_to_time(currentTime, "MM/dd HH:mm")));
                ProgramInfo programInfo = get_program_by_channel_id(channelID);
                if (programInfo != null && programInfo.getAdultFlag() == 1)
                    gPresent.set_parental_rate(18);
                gPresentEndTime = gPresent.get_end_time();
                add_to_schedule_map(channelID, gPresent);
                return gPresent;
            }
            Log.d(TAG, "get_present_event: Null Present");
            gPresent = null;
            return gPresent;
        }
    }

    public EPGEvent get_follow_event(long channelID) {
        // epg use pesi c service, not java
        synchronized(mEPGEventLock) {
            long currentTime = get_current_time();

            gFollow = get_schedule_follow_event(channelID);
            if (gFollow != null) {
                Log.d(TAG, "get_follow_event: (from schedule map) " +
                        "[follow event] " + (gFollow == null ? "NULL" : gFollow.get_event_name() +
                        ", [start ~ end] " + MiniEPG.ms_to_time(gFollow.get_start_time(), "MM/dd HH:mm") + " - " + MiniEPG.ms_to_time(gFollow.get_end_time(), "HH:mm") +
                        ", [current time] " + MiniEPG.ms_to_time(currentTime, "MM/dd HH:mm")));
                ProgramInfo programInfo = get_program_by_channel_id(channelID);
                if (programInfo != null && programInfo.getAdultFlag() == 1)
                    gFollow.set_parental_rate(18);
                return gFollow;
            }
            gFollow = gPrimeDtvMediaPlayer.get_follow_event(channelID);
            if (gFollow != null/*is_correct_follow(gFollow)*/) {
                add_to_schedule_map(channelID, gFollow);
                Log.d(TAG, "get_follow_event: (from Dtv Service) " +
                        "[follow event] " + (gFollow == null ? "NULL" : gFollow.get_event_name() +
                        ", [start ~ end] " + MiniEPG.ms_to_time(gFollow.get_start_time(), "MM/dd HH:mm") + " - " + MiniEPG.ms_to_time(gFollow.get_end_time(), "HH:mm") +
                        ", [current time] " + MiniEPG.ms_to_time(currentTime, "MM/dd HH:mm")));
                ProgramInfo programInfo = get_program_by_channel_id(channelID);
                if (programInfo != null && programInfo.getAdultFlag() == 1)
                    gFollow.set_parental_rate(18);
                return gFollow;
            }
            Log.d(TAG, "get_follow_event: Null Follow");
            gFollow = null;
            return gFollow;
        }
    }

    public EPGEvent[] get_present_follow_event(long channelID) {
        // epg use pesi c service, not java
        synchronized(mEPGEventLock) {
            return gPrimeDtvMediaPlayer.get_present_follow_event(channelID);
//            return gDtvFramework.get_present_follow_event(channelID);
        }
    }

    public EPGEvent get_epg_by_event_id(long channelID, int eventId) {
        // epg use pesi c service, not java
        return gPrimeDtvMediaPlayer.get_epg_by_event_id(channelID, eventId);
//        return gDtvFramework.getEpgByEventID(channelID, eventId);
    }

    public List<EPGEvent> get_epg_events(long channelID, Date startTime, Date endTime, int pos, int reqNum, int addEmpty) {
        // epg use pesi c service, not java
        return gPrimeDtvMediaPlayer.get_epg_events(channelID, startTime, endTime, pos, reqNum, addEmpty);
//        return gDtvFramework.getEPGEvents(channelID, startTime, endTime, pos, reqNum, addEmpty);
    }

    public String get_short_description(long channelId, int eventId) {
        // epg use pesi c service, not java
        return gPrimeDtvMediaPlayer.get_short_description(channelId, eventId);
//        return gDtvFramework.getShortDescription(channelId, eventId);
    }

    public String get_detail_description(long channelId, int eventId) {
        // epg use pesi c service, not java
        return gPrimeDtvMediaPlayer.get_detail_description(channelId, eventId);
//        return gDtvFramework.getDetailDescription(channelId, eventId);
    }

    public long get_current_time() {
        //return gNetworkTime != null ? gNetworkTime.getTime() : System.currentTimeMillis();
        return System.currentTimeMillis();
    }

    public Date get_current_date() {
        return new Date(get_current_time());
    }

    public Date get_start_date() {
        Calendar calendar;
        Date startOfDay;
        calendar = Calendar.getInstance();
        calendar.setTime(get_current_date());
        //calendar.add(Calendar.DATE, -1);
        startOfDay = calendar.getTime();
        return startOfDay;
    }

    /** @noinspection CommentedOutCode*/
    public Date get_end_date() {
        Calendar calendar;
        Date endOfDay;

        calendar = Calendar.getInstance();
        calendar.setTime(get_current_date());
        calendar.add(Calendar.DATE, 1);
        /*calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);*/
        endOfDay = calendar.getTime();

        return endOfDay;
    }

    private boolean is_correct_present(EPGEvent present) {
        if (null == present)
            return false;
        long offset = 0;//TimeZone.getDefault().getRawOffset();
        long startTime = present.get_start_time() + offset;
        gPresentEndTime = present.get_end_time() + offset;
        long currentTime = get_current_time();
        return currentTime >= startTime && currentTime <= gPresentEndTime;
    }

    private boolean is_correct_follow(EPGEvent follow) {
        if (null == follow)
            return false;
        long offset = 0;//TimeZone.getDefault().getRawOffset();
        long followStartTime = follow.get_start_time() + offset;
        String presentEnd = MiniEPG.ms_to_time(gPresentEndTime, "HH:mm");
        String followStart = MiniEPG.ms_to_time(followStartTime, "HH:mm");
        return presentEnd.equals(followStart) || followStartTime >= gPresentEndTime;
    }

    public int set_event_lang(String firstEvtLang, String secondEvtLang) {
        // epg use pesi c service, not java
        return gPrimeDtvMediaPlayer.set_event_lang(firstEvtLang, secondEvtLang);
//        return gDtvFramework.setEvtLang(firstEvtLang, secondEvtLang);
    }

    public void setup_epg_channel() {
        gPrimeDtvMediaPlayer.setup_epg_channel();
    }

    public void debug_epg_events(long channelId){
        gPrimeDtvMediaPlayer.debug_epg_events(channelId);
    }

    public int send_epg_data_id(List<ProgramInfo> programInfos) {
        // epg use pesi c service, not java
        return gPrimeDtvMediaPlayer.send_epg_data_id(programInfos);
    }

    public int add_epg_data_id(long channelId, int sid, int tid, int onid){
        return gPrimeDtvMediaPlayer.add_epg_data_id(channelId, sid, tid, onid);
    }

    public int delete_epg_data_id(long channelId, int sid, int tid, int onid){
        return gPrimeDtvMediaPlayer.delete_epg_data_id(channelId, sid, tid, onid);
    }

    public void start_schedule_eit() {
        // use tuner framework to get epg raw data
        LogUtils.d("[Ethan] start_schedule_eit");
        gDtvFramework.startScheduleEit();
    }

    public int set_time(Date date) {
//        return gPrimeDtvMediaPlayer.set_time(date);
        return gDtvFramework.setTime(date);
    }

    public Date get_dtv_date() {
//        return gPrimeDtvMediaPlayer.get_dtv_date();
        return gDtvFramework.getDtvDate();
    }

    public int sync_time(boolean bEnable) {
//        return gPrimeDtvMediaPlayer.sync_time(bEnable);
        return gDtvFramework.syncTime(bEnable);
    }

    public FavInfo fav_info_get(int favMode, int index) {
//        return gPrimeDtvMediaPlayer.fav_info_get(favMode, index);
        return gDtvFramework.favInfoGet(favMode, index);
    }

    public List<FavInfo> fav_info_get_list(int favMode) {
//        return gPrimeDtvMediaPlayer.fav_info_get_list(favMode);
        return gDtvFramework.favInfoGetList(favMode);
    }

    public int fav_info_delete(int favMode, long channelId) {
//        return gPrimeDtvMediaPlayer.fav_info_delete(favMode, channelId);
        return gDtvFramework.favInfoDelete(favMode, channelId);
    }

    public int fav_info_delete_all(int favMode) {
//        return gPrimeDtvMediaPlayer.fav_info_delete_all(favMode);
        return gDtvFramework.favInfoDeleteAll(favMode);
    }

    public int fav_info_save_db(FavInfo favInfo) {
//        return gPrimeDtvMediaPlayer.fav_info_delete(favMode, channelId);
        return gDtvFramework.favInfoSaveDb(favInfo);
    }

    public String fav_group_name_get(int favMode) {
//        return gPrimeDtvMediaPlayer.fav_group_name_get(favMode);
        return gDtvFramework.favGroupNameGet(favMode);
    }

    public int fav_group_name_update(int favMode, String name) {
//        return gPrimeDtvMediaPlayer.fav_group_name_update(favMode, name);
        return gDtvFramework.favGroupNameUpdate(favMode, name);
    }

    public GposInfo gpos_info_get() {
//        return gPrimeDtvMediaPlayer.gpos_info_get();
        return gDtvFramework.GposInfoGet();
    }

    public void gpos_info_update(GposInfo gPos) {
//        gPrimeDtvMediaPlayer.gpos_info_update(gPos);
        gDtvFramework.GposInfoUpdate(gPos);
    }


    public void gpos_info_update_by_key_string(String key, String value) {
//        gPrimeDtvMediaPlayer.gpos_info_update_by_key_string(key, value);
        gDtvFramework.GposInfoUpdateByKeyString(key, value);
    }

    public void gpos_info_update_by_key_string(String key, int value) {
//        gPrimeDtvMediaPlayer.gpos_info_update_by_key_string(key, value);
        gDtvFramework.GposInfoUpdateByKeyString(key, value);
    }

    public int reset_factory_default() {
//        return gPrimeDtvMediaPlayer.reset_factory_default();
        gDtvFramework.ResetFactoryDefault();
        return 0;
    }

    public String get_pesi_service_version() {
//        return gPrimeDtvMediaPlayer.get_pesi_service_version();
        String service_verison = gDtvFramework.GetPesiServiceVersion();
        if(RequestDTVServiceVersion.equals(service_verison)){
            LogUtils.e("Service Version NOT match , Need to check it !!!!!!!!!!!!");
        }
        return service_verison;
    }

    public int mtest_get_gpio_status(int u32GpioNo) {
//        return gPrimeDtvMediaPlayer.mtest_get_gpio_status(u32GpioNo);
        return gDtvFramework.MtestGetGPIOStatus(u32GpioNo);
    }

    public int mtest_set_gpio_status(int u32GpioNo, int bHighVolt) {
//        return gPrimeDtvMediaPlayer.mtest_set_gpio_status(u32GpioNo, bHighVolt);
        return gDtvFramework.MtestSetGPIOStatus(u32GpioNo, bHighVolt);
    }

    public int mtest_get_atr_status(int smartCardStatus) {
//        return gPrimeDtvMediaPlayer.mtest_get_atr_status(smartCardStatus);
        return gDtvFramework.MtestGetATRStatus(smartCardStatus);
    }

    public int mtest_get_hdcp_status() {
//        return gPrimeDtvMediaPlayer.mtest_get_hdcp_status();
        return gDtvFramework.MtestGetHDCPStatus();
    }

    public int mtest_get_hdmi_status() {
//        return gPrimeDtvMediaPlayer.mtest_get_hdmi_status();
        return gDtvFramework.MtestGetHDMIStatus();
    }

    public int mtest_power_save() {
//        return gPrimeDtvMediaPlayer.mtest_power_save();
        return gDtvFramework.MtestPowerSave();
    }

    public int mtest_seven_segment(int enable) {
//        return gPrimeDtvMediaPlayer.mtest_seven_segment(enable);
        return gDtvFramework.MtestSevenSegment(enable);
    }

    public int mtest_set_antenna_5v(int tunerID, int tunerType, int enable) {
//        return gPrimeDtvMediaPlayer.mtest_set_antenna_5v(tunerID, tunerType, enable);
        return gDtvFramework.MtestSetAntenna5V(tunerID, tunerType, enable);
    }

    public int mtest_set_buzzer(int enable) {
//        return gPrimeDtvMediaPlayer.mtest_set_buzzer(enable);
        return gDtvFramework.MtestSetBuzzer(enable);
    }

    public int mtest_set_led_red(int enable) {
//        return gPrimeDtvMediaPlayer.mtest_set_led_red(enable);
        return gDtvFramework.MtestSetLedRed(enable);
    }

    public int mtest_set_led_green(int enable) {
//        return gPrimeDtvMediaPlayer.mtest_set_led_green(enable);
        return gDtvFramework.MtestSetLedGreen(enable);
    }

    public int mtest_set_led_orange(int enable) {
//        return gPrimeDtvMediaPlayer.mtest_set_led_orange(enable);
        return gDtvFramework.MtestSetLedOrange(enable);
    }

    public int mtest_set_led_white(int enable) {
//        return gPrimeDtvMediaPlayer.mtest_set_led_white(enable);
        return gDtvFramework.MtestSetLedWhite(enable);
    }

    public int mtest_set_led_on_off(int status) {
//        return gPrimeDtvMediaPlayer.mtest_set_led_on_off(status);
        return gDtvFramework.MtestSetLedOnOff(status);
    }

    public int mtest_get_front_key(int key) {
//        return gPrimeDtvMediaPlayer.mtest_get_front_key(key);
        return gDtvFramework.MtestGetFrontKey(key);
    }

    public int mtest_set_usb_power(int enable) {
//        return gPrimeDtvMediaPlayer.mtest_set_usb_power(enable);
        return gDtvFramework.MtestSetUsbPower(enable);
    }

    public int mtest_test_usb_read_write(int portNum, String path) {
//        return gPrimeDtvMediaPlayer.mtest_test_usb_read_write(portNum, path);
        return gDtvFramework.MtestTestUsbReadWrite(portNum, path);
    }

    // Johnny 20181221 for mtest split screen
    public int mtest_test_av_multi_play(int tunerNum, List<Integer> tunerIDs, List<Long> channelIDs) {
//        return gPrimeDtvMediaPlayer.mtest_test_av_multi_play(tunerNum, tunerIDs, channelIDs);
        return gDtvFramework.MtestTestAvMultiPlay(tunerNum, tunerIDs, channelIDs);
    }

    public int mtest_test_av_stop_by_tuner_id(int tunerID) {
//        return gPrimeDtvMediaPlayer.mtest_test_av_stop_by_tuner_id(tunerID);
        return gDtvFramework.MtestTestAvStopByTunerID(tunerID);
    }

    public int mtest_mic_set_input_gain(int value) {
//        return gPrimeDtvMediaPlayer.mtest_mic_set_input_gain(value);
        return gDtvFramework.MtestMicSetInputGain(value);
    }

    public int mtest_mic_set_lr_input_gain(int l_r, int value) {
//        return gPrimeDtvMediaPlayer.mtest_mic_set_lr_input_gain(l_r, value);
        return gDtvFramework.MtestMicSetLRInputGain(l_r, value);
    }

    public int mtest_mic_set_alc_gain(int value) {
//        return gPrimeDtvMediaPlayer.mtest_mic_set_alc_gain(value);
        return gDtvFramework.MtestMicSetAlcGain(value);
    }

    public int mtest_get_error_frame_count(int tunerID) {
//        return gPrimeDtvMediaPlayer.mtest_get_error_frame_count(tunerID);
        return gDtvFramework.MtestGetErrorFrameCount(tunerID);
    }

    public int mtest_get_frame_drop_count(int tunerID) {
//        return gPrimeDtvMediaPlayer.mtest_get_frame_drop_count(tunerID);
        return gDtvFramework.MtestGetFrameDropCount(tunerID);
    }

    public String mtest_get_chip_id() {
//        return gPrimeDtvMediaPlayer.mtest_get_chip_id();
        return gDtvFramework.MtestGetChipID();
    }

    public int mtest_start_mtest(String version) {
//        return gPrimeDtvMediaPlayer.mtest_start_mtest(version);
        return gDtvFramework.MtestStartMtest(version);
    }

    public int mtest_connect_pctool() {
//        return gPrimeDtvMediaPlayer.mtest_connect_pctool();
        return gDtvFramework.MtestConnectPctool();
    }

    public List<Integer> mtest_get_wifi_tx_rx_level() {//Scoty 20190417 add wifi level command
//        return gPrimeDtvMediaPlayer.mtest_get_wifi_tx_rx_level();
        return gDtvFramework.MtestGetWiFiTxRxLevel();
    }

    public int mtest_get_wakeup_mode() {
//        return gPrimeDtvMediaPlayer.mtest_get_wakeup_mode();
        return gDtvFramework.MtestGetWakeUpMode();
    }

    // Johnny 20190522 check key before OTA
    public Map<String, Integer> mtest_get_key_status_map() {
//        return gPrimeDtvMediaPlayer.mtest_get_key_status_map();
        return gDtvFramework.MtestGetKeyStatusMap();
    }

    public int test_set_tv_radio_count(int tvCount, int radioCount) {
//        return gPrimeDtvMediaPlayer.test_set_tv_radio_count(tvCount, radioCount);
        gDtvFramework.TestSetTvRadioCount(tvCount, radioCount);
        return 0;
    }

    public int test_change_tuner(int tunerTpe)//Scoty 20180817 add Change Tuner Command
    {
//        return gPrimeDtvMediaPlayer.test_change_tuner(tunerTpe);
        return gDtvFramework.TestChangeTuner(tunerTpe);
    }

    ///////// for pesi middleware---pesimain /////////
    //PIP -start
    public int pip_open(int x, int y, int width, int height) {
//        return gPrimeDtvMediaPlayer.pip_open(x, y, width, height);
        return gDtvFramework.PipOpen(x, y, width, height);
    }

    public int pip_close() {
//        return gPrimeDtvMediaPlayer.pip_close();
        return gDtvFramework.PipClose();
    }

    public int pip_start(long channelId, int show) {
//        return gPrimeDtvMediaPlayer.pip_start(channelId, show);
        return gDtvFramework.PipStart(channelId, show);
    }

    public int pip_stop() {
//        return gPrimeDtvMediaPlayer.pip_stop();
        return gDtvFramework.PipStop();
    }

    public int pip_set_window(int x, int y, int width, int height) {
//        return gPrimeDtvMediaPlayer.pip_set_window(x, y, width, height);
        return gDtvFramework.PipSetWindow(x, y, width, height);
    }

    public int pip_exchange() {
//        return gPrimeDtvMediaPlayer.pip_exchange();
        return gDtvFramework.PipExChange();
    }
    //PIP -end

    //Device -start
    public void set_usb_port() {
        gPrimeDtvMediaPlayer.set_usb_port();
    }

    public List<Integer> get_usb_port_list() {
        return gPrimeDtvMediaPlayer.get_usb_port_list();
    }
    //Device -end

    public List<SimpleChannel> get_cur_play_channel_list() {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        return get_cur_play_channel_list(FavGroup.ALL_TV_TYPE, 0);
    }

    public List<SimpleChannel> get_cur_play_channel_list(int type, int includePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
//        return gPrimeDtvMediaPlayer.get_cur_play_channel_list(type, includePVRSkipFlag);
        return gDtvFramework.getCurPlayChannelList(type, includePVRSkipFlag);
    }

    public int get_cur_play_channel_list_cnt(int type) {//eric lin 20180802 check program exist
//        return gPrimeDtvMediaPlayer.get_cur_play_channel_list_cnt(type);
        return gDtvFramework.getCurPlayChannelListCnt(type);
    }

    public void reset_total_channel_list() {
//        gPrimeDtvMediaPlayer.reset_total_channel_list();
        gDtvFramework.resetTotalChannelList();
    }

    public void update_cur_play_channel_list(Context context, int includePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
//        gPrimeDtvMediaPlayer.update_cur_play_channel_list(context, includePVRSkipFlag);
        gDtvFramework.updateCurPlayChannelList(context, includePVRSkipFlag);
    }
    //Scoty Add ProgramInfo and NetProgramInfo Get TotalChannelList -e

    public void update_cur_play_channel_list(int includePVRSkipFlag) {//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
//        gPrimeDtvMediaPlayer.update_cur_play_channel_list(includePVRSkipFlag);
        gDtvFramework.updateCurPlayChannelList(includePVRSkipFlag);
    }

    public int get_platform() {
//        return gPrimeDtvMediaPlayer.get_platform();
        return gDtvFramework.getPlatform();
    }

    public int get_tuner_num()//Scoty 20181113 add GetTunerNum function
    {
//        return gPrimeDtvMediaPlayer.get_tuner_num();
        return gDtvFramework.getTunerNum();
    }

    //Scoty 20180809 modify dual pvr rule -s//Scoty 20180615 update TV/Radio TotalChannelList -s
    public void update_pvr_skip_list(int groupType, int includePVRSkipFlag, int tpId, List<Integer> pvrTpList)//Scoty 20181113 add for dual tuner pvrList
    {
//        gPrimeDtvMediaPlayer.update_pvr_skip_list(groupType, includePVRSkipFlag, tpId, pvrTpList);
        gDtvFramework.update_pvr_skip_list(groupType, includePVRSkipFlag, tpId, pvrTpList);
    }
    //Scoty 20180809 modify dual pvr rule -e//Scoty 20180615 update TV/Radio TotalChannelList -e

    // pvr -start
    // Start - Series Record
    public int pvr_init(String usbMountPath){
        LogUtils.d("usbMountPath = "+usbMountPath);
        return gDtvFramework.PvrInit(usbMountPath);
    }
    public int pvr_deinit(){
        return gDtvFramework.PvrDeinit();
    }
    public int pvr_RecordStart(PvrRecStartParam startParam,int tunerId)
    {
        LogUtils.d("channel ID = "+startParam.getmProgramInfo().getChannelId()+", tunerId = "+tunerId);
        return gDtvFramework.PvrRecordStart(startParam, tunerId);
    }
    public int pvr_RecordStop(int recId){
        LogUtils.d("recId = "+recId);
        return gDtvFramework.PvrRecordStop(recId);
    }
    public void pvr_RecordStopAll(){
        gDtvFramework.PvrRecordStopAll();
    }
    public int pvr_RecordGetRecTimeByRecId(int recId){
        LogUtils.d("recId = "+recId);
        return gDtvFramework.PvrRecordGetRecTimeByRecId(recId);
    }
    public boolean pvr_PlayCheckLastPositionPoint(PvrRecIdx recIdx){
        LogUtils.d("MasterId = "+recIdx.getMasterIdx()+", SeriesId = "+recIdx.getSeriesIdx());
        return gDtvFramework.PvrPlayCheckLastPositionPoint(recIdx);
    }
    public int pvr_PlayFileStart(PvrRecIdx recIdx, boolean fromLastPosition, int tunerId){
        LogUtils.d("MasterId = "+recIdx.getMasterIdx()+", SeriesId = "+recIdx.getSeriesIdx()+", tunerId = "+tunerId + ", lastPositionFlag = "+fromLastPosition);
        return gDtvFramework.PvrPlayFileStart(recIdx,fromLastPosition,tunerId);
    }
    public int pvr_PlayFileStop(int tunerId) {
        return gDtvFramework.PvrPlayFileStop(tunerId);
    }
    public int pvr_PlayPlay(int playId)
    {
        LogUtils.d("playId = "+playId);
        return gDtvFramework.PvrPlayPlay(playId);
    }
    public int pvr_PlayPause(int playId){
        LogUtils.d("playId = "+playId);
        return gDtvFramework.PvrPlayPause(playId);
    }
    public int pvr_PlayFastForward(int playId) {
        LogUtils.d("playId = " + playId);
        return gDtvFramework.PvrPlayFastForward(playId);
    }
    public int pvr_PlayRewind(int playId) {
        LogUtils.d("playId = " + playId);
        return gDtvFramework.PvrPlayRewind(playId);
    }
    public int pvr_PlaySeek(int playId, PvrInfo.EnSeekMode seekMode, int offsetSec)
    {
        LogUtils.d("playId = "+playId+", seekMode = "+seekMode+", offsetSec = "+offsetSec);
        return gDtvFramework.PvrPlaySeek(playId,seekMode,offsetSec);
    }
    public int pvr_PlaySetSpeed(int playId, PvrInfo.EnPlaySpeed playSpeed)
    {
        LogUtils.d("playId = "+playId+", playSpeed = "+playSpeed);
        return gDtvFramework.PvrPlaySetSpeed(playId,playSpeed);
    }
    public PvrInfo.EnPlaySpeed pvr_PlayGetSpeed(int playId) {
        LogUtils.d("playId = "+playId);
        return gDtvFramework.PvrPlayGetSpeed(playId);
    }
    public int pvr_PlayChangeAudioTrack(int playId, AudioInfo.AudioComponent audioComponent) {
        LogUtils.d("playId = "+playId+", audioComponent = "+audioComponent);
        return gDtvFramework.PvrPlayChangeAudioTrack(playId, audioComponent);
    }
    public int pvr_PlayGetCurrentAudioIndex(int playId) {
        LogUtils.d("playId = "+playId);
        return gDtvFramework.PvrPlayGetCurrentAudioIndex(playId);
    }
    public int pvr_PlayGetPlayTime(int playId) {
        LogUtils.d("playId = "+playId);
        return gDtvFramework.PvrPlayGetPlayTime(playId);
    }
    public PvrInfo.EnPlayStatus pvr_PlayGetPlayStatus(int playId) {
        LogUtils.d("playId = "+playId);
        return gDtvFramework.PvrPlayGetPlayStatus(playId);
    }
    public PvrInfo.PlayTimeInfo pvr_PlayGetPlayTimeInfo(int playId) {
        LogUtils.d("playId = "+playId);
        return gDtvFramework.PvrPlayGetPlayTimeInfo(playId);
    }
    public int pvr_TimeShiftStart(ProgramInfo programInfo,int recordTunerId,int playTunerId) {
        LogUtils.d("channel ID = "+programInfo.getChannelId()+", recordTunerId = "+recordTunerId+", playTunerId = "+playTunerId);
        return gDtvFramework.PvrTimeShiftStart(programInfo, recordTunerId,playTunerId);
    }
    public int pvr_TimeShiftStop() {
        LogUtils.d(" ");
        return gDtvFramework.PvrTimeShiftStop();
    }
    public int pvr_TimeShiftPlayStart(int tunerId) {
        LogUtils.d("tunerId = "+tunerId);
        return gDtvFramework.PvrTimeShiftPlayStart(tunerId);
    }
    public int pvr_TimeShiftPlayPause(int tunerId) {
        LogUtils.d("tunerId = "+tunerId);
        return gDtvFramework.PvrTimeShiftPlayPause(tunerId);
    }
    public int pvr_TimeShiftPlayResume(int tunerId) {
        LogUtils.d("tunerId = "+tunerId);
        return gDtvFramework.PvrTimeShiftPlayResume(tunerId);
    }
    public int pvr_TimeShiftPlayFastForward(int playId) {
        LogUtils.d("playId = "+playId);
        return gDtvFramework.PvrTimeShiftPlayFastForward(playId);
    }
    public int pvr_TimeShiftPlayRewind(int playId) {
        LogUtils.d("playId = "+playId);
        return gDtvFramework.PvrTimeShiftPlayRewind(playId);
    }
    public int pvr_TimeShiftPlaySeek(int playId, PvrInfo.EnSeekMode seekMode, int startPosition) {
        LogUtils.d("playId = "+playId+" ,seekMode = "+seekMode+", startPosition = "+startPosition);
        return gDtvFramework.PvrTimeShiftPlaySeek(playId,seekMode,startPosition);
    }
    public int pvr_TimeShiftPlaySetSpeed(int playId, PvrInfo.EnPlaySpeed playSpeed) {
        LogUtils.d("playId = "+playId+", playSpeed = "+playSpeed);
        return gDtvFramework.PvrTimeShiftPlaySetSpeed(playId,playSpeed);
    }
    public PvrInfo.EnPlaySpeed pvr_TimeShiftPlayGetSpeed(int playId) {
        LogUtils.d("playId = "+playId);
        return gDtvFramework.PvrTimeShiftPlayGetSpeed(playId);
    }
    public int pvr_TimeShiftPlayChangeAudioTrack(int playId, AudioInfo.AudioComponent audioComponent) {
        LogUtils.d("playId = "+playId+", audioComponent = "+audioComponent);
        return gDtvFramework.PvrTimeShiftPlayChangeAudioTrack(playId, audioComponent);
    }
    public int pvr_TimeShiftPlayGetCurrentAudioIndex(int playId) {
        LogUtils.d("playId = "+playId);
        return gDtvFramework.PvrTimeShiftPlayGetCurrentAudioIndex(playId);
    }
    public PvrInfo.EnPlayStatus pvr_TimeShiftPlayGetStatus(int playId) {
        LogUtils.d("playId = "+playId);
        return gDtvFramework.PvrTimeShiftPlayGetStatus(playId);
    }
    public PvrInfo.PlayTimeInfo pvr_TimeShiftGetTimeInfo(int playId) {
        LogUtils.d("playId = "+playId);
        return gDtvFramework.PvrTimeShiftGetTimeInfo(playId);
    }
    public PvrRecFileInfo pvr_GetFileInfoByIndex(PvrRecIdx tableKeyInfo) {
        LogUtils.d("MasterID = "+tableKeyInfo.getMasterIdx()+", SeriesID = "+tableKeyInfo.getSeriesIdx() );
        return gDtvFramework.PvrGetFileInfoByIndex(tableKeyInfo);
    }
    public int pvr_GetRecCount() {
        return gDtvFramework.PvrGetRecCount();
    }
    public int pvr_GetSeriesRecCount(int masterIndex) {
        LogUtils.d("masterIndex = "+masterIndex);
        return gDtvFramework.PvrGetSeriesRecCount(masterIndex);
    }
    public List<PvrRecFileInfo> pvr_GetRecLink(int startIndex, int count) {
        LogUtils.d("startIndex = "+startIndex+", count = "+count );
        return gDtvFramework.PvrGetRecLink(startIndex, count);
    }

    public List<PvrRecFileInfo> pvr_GetPlaybackLink(int startIndex, int count) {
        LogUtils.d("startIndex = "+startIndex+", count = "+count );
        return gDtvFramework.PvrGetPlaybackLink(startIndex, count);
    }

    public List<PvrRecFileInfo> pvr_GetSeriesRecLink(PvrRecIdx tableKeyInfo, int count) {
        LogUtils.d("MasterID = "+tableKeyInfo.getMasterIdx()+", SeriesID = "+tableKeyInfo.getSeriesIdx()+", count = "+count );
        return gDtvFramework.PvrGetSeriesRecLink(tableKeyInfo, count);
    }
    public int pvr_DelAllRecs() {
        return gDtvFramework.PvrDelAllRecs();
    }
    public int pvr_DelSeriesRecs(int masterIndex) {
        LogUtils.d("masterIndex = "+masterIndex);
        return gDtvFramework.PvrDelSeriesRecs(masterIndex);
    }
    public int pvr_DelRecsByChId(int channelId) {
        LogUtils.d("channelId = "+channelId);
        return gDtvFramework.PvrDelRecsByChId(channelId);
    }
    public int pvr_DelOneRec(PvrRecIdx tableKeyInfo){
        LogUtils.d("MasterID = "+tableKeyInfo.getMasterIdx()+", SeriesID = "+tableKeyInfo.getSeriesIdx() );
        return gDtvFramework.PvrDelOneRec(tableKeyInfo);
    }
    public int pvr_DelOnePlayback(PvrRecIdx tableKeyInfo){
        LogUtils.d("MasterID = "+tableKeyInfo.getMasterIdx()+", SeriesID = "+tableKeyInfo.getSeriesIdx() );
        return gDtvFramework.PvrDelOnePlayback(tableKeyInfo);
    }
    public int pvr_CheckSeriesEpisode(byte[] seriesKey, int episode){
        LogUtils.d("episode = "+episode);
        return gDtvFramework.PvrCheckSeriesEpisode(seriesKey,episode);
    }
    public int pvr_CheckSeriesComplete(byte[]  seriesKey){
        return gDtvFramework.PvrCheckSeriesComplete(seriesKey);
    }
    public boolean pvr_IsIdxInUse(PvrRecIdx pvrRecIdx) {
        return gDtvFramework.PvrIsIdxInUse(pvrRecIdx);
    }

    // End - Series Record
    public int pvr_record_check(long channelID)
    {
        Log.e(TAG,"[Warning] This function not porting!! Please check");
        return 0;
    }

    /* === HDD Manger Start === */
    public void hdd_monitor_start(long alert_sizeMB) {
        // 啟動 HDD 監測
        if (mHDInfomanger == null) {
            mHDInfomanger = new HDDInfoManger(mContext, this.gDtvCallback);
        }
        mHDInfomanger.startMonitoring(alert_sizeMB);
    }
    public void hdd_monitor_stop() {
        // 停止 HDD 監測
        if (mHDInfomanger != null) {
            mHDInfomanger.stopMonitoring();
            mHDInfomanger = null;
        }
    }
    /* === HDD Manger End === */

    public List<SimpleChannel> get_channel_list_by_filter(int filterTag, int serviceType, String keyword, int IncludeSkip, int IncludePvrSkip)//Scoty 20181109 modify for skip channel
    {
//        return gPrimeDtvMediaPlayer.get_channel_list_by_filter(filterTag, serviceType, keyword, IncludeSkip, IncludePvrSkip);
        return gDtvFramework.getChannelListByFilter(filterTag, serviceType, keyword, IncludeSkip, IncludePvrSkip);
    }


    // Johnny 20180814 add setDiseqc1.0 port -s
    public int set_diseqc10_port_info(int nTunerId, int nPort, int n22KSwitch, int nPolarity) {
//        return gPrimeDtvMediaPlayer.set_diseqc10_port_info(nTunerId, nPort, n22KSwitch, nPolarity);
        return gDtvFramework.setDiSEqC10PortInfo(nTunerId, nPort, n22KSwitch, nPolarity);
    }

    //Scoty add DiSeqC Motor rule -s
    public int set_diseqc12_move_motor(int nTunerId, int Direct, int Step) {
//        return gPrimeDtvMediaPlayer.set_diseqc12_move_motor(nTunerId, Direct, Step);
        return gDtvFramework.setDiSEqC12MoveMotor(nTunerId, Direct, Step);
    }

    public int set_diseqc12_move_motor_stop(int nTunerId) {
//        return gPrimeDtvMediaPlayer.set_diseqc12_move_motor_stop(nTunerId);
        return gDtvFramework.setDiSEqC12MoveMotorStop(nTunerId);
    }

    public int reset_diseqc12_position(int nTunerId) {
//        return gPrimeDtvMediaPlayer.reset_diseqc12_position(nTunerId);
        return gDtvFramework.resetDiSEqC12Position(nTunerId);
    }

    public int set_diseqc_limit_pos(int nTunerId, int limitType) {
//        return gPrimeDtvMediaPlayer.set_diseqc_limit_pos(nTunerId, limitType);
        return gDtvFramework.setDiSEqCLimitPos(nTunerId, limitType);
    }

    //Scoty add DiSeqC Motor rule -e
    // Johnny 20180814 add setDiseqc1.0 port -e
    public int set_standby_on_off(int onOff) {
//        return gPrimeDtvMediaPlayer.set_standby_on_off(onOff);
        return gDtvFramework.SetStandbyOnOff(onOff);
    }

    // for VMX need open/close -s
    public void vmx_start_scan(TVScanParams sp, int startTPID, int endTPID) // connie 20180919 add for vmx search
    {
//        gPrimeDtvMediaPlayer.vmx_start_scan(sp, startTPID, endTPID);
        gDtvFramework.VMXstartScan(sp, startTPID, endTPID);
    }

    public LoaderInfo get_loader_info() // connie 20180903 for VMX -s
    {
//        return gPrimeDtvMediaPlayer.get_loader_info();
        return gDtvFramework.GetLoaderInfo();
    }

    public CaStatus get_ca_status_info() {
//        return gPrimeDtvMediaPlayer.get_ca_status_info();
        return gDtvFramework.GetCAStatusInfo();
    }

    public int get_ecm_count() {
//        return gPrimeDtvMediaPlayer.get_ecm_count();
        return gDtvFramework.GetECMcount();
    }

    public int get_emm_count() {
//        return gPrimeDtvMediaPlayer.get_emm_count();
        return gDtvFramework.GetEMMcount();
    }

    public String get_lib_date() {
//        return gPrimeDtvMediaPlayer.get_lib_date();
        return gDtvFramework.GetLibDate();
    }

    public String get_chip_id() {
//        return gPrimeDtvMediaPlayer.get_chip_id();
        return gDtvFramework.GetChipID();
    }

    public String get_sn() {
//        return gPrimeDtvMediaPlayer.get_sn();
        return gDtvFramework.GetSN();
    }

    public String get_ca_version() {
//        return gPrimeDtvMediaPlayer.get_ca_version();
        return gDtvFramework.GetCaVersion();
    }

    public String get_sc_number() {
//        return gPrimeDtvMediaPlayer.get_sc_number();
        return gDtvFramework.GetSCNumber();
    }

    public int get_pairing_status() {
//        return gPrimeDtvMediaPlayer.get_pairing_status();
        return gDtvFramework.GetPairingStatus();
    }

    public String get_purse() {
//        return gPrimeDtvMediaPlayer.get_purse();
        return gDtvFramework.GetPurse();
    }

    public int get_group_m() {
//        return gPrimeDtvMediaPlayer.get_group_m();
        return gDtvFramework.GetGroupM();
    }

    public int vmx_set_pin_code(String pincode) {
//        return gPrimeDtvMediaPlayer.vmx_set_pin_code(pincode);
        Log.e(TAG,"this function not porting!! please check");
        return 0;
    }

    public String get_location() {
//        return gPrimeDtvMediaPlayer.get_location();
        Log.e(TAG,"this function not porting!! please check");
        return null;
    }

    public int set_pin_code(String pinCode, int PinIndex, int TextSelect) {
//        return gPrimeDtvMediaPlayer.set_pin_code(pinCode, PinIndex, TextSelect);
        return gDtvFramework.SetPinCode(pinCode, PinIndex, TextSelect);
    }

    public int set_pptv(String pinCode, int pinIndex) {
//        return gPrimeDtvMediaPlayer.set_pptv(pinCode, pinIndex);
        return gDtvFramework.SetPPTV(pinCode, pinIndex);
    }

    public void set_osm_ok() {
//        gPrimeDtvMediaPlayer.set_osm_ok();
        gDtvFramework.SetOMSMok();
    }

    public void vmx_test(int mode) {
//        gPrimeDtvMediaPlayer.vmx_test(mode);
        gDtvFramework.VMXTest(mode);
    }

    //Scoty 20181207 modify VMX OTA rule -s
    public void test_vmx_ota(int mode) {
//        gPrimeDtvMediaPlayer.test_vmx_ota(mode);
        gDtvFramework.TestVMXOTA(mode);
    }

    public int vmx_auto_ota(int OTAMode, int TriggerID, int TriggerNum, int TunerId, int SatId, int DsmccPid, int FreqNum, ArrayList<Integer> FreqList, ArrayList<Integer> BandwidthList) {
//        return gPrimeDtvMediaPlayer.vmx_auto_ota(OTAMode, TriggerID, TriggerNum, TunerId, SatId, DsmccPid, FreqNum, FreqList, BandwidthList);
        gDtvFramework.VMXAutoOTA(OTAMode, TriggerID, TriggerNum, TunerId, SatId, DsmccPid, FreqNum, FreqList, BandwidthList);
        return 0;
    }

    //Scoty 20181207 modify VMX OTA rule -e
    public String vmx_get_box_id() {
//        return gPrimeDtvMediaPlayer.vmx_get_box_id();
        return gDtvFramework.VMXGetBoxID();
    }

    public String vmx_get_virtual_number() {
//        return gPrimeDtvMediaPlayer.vmx_get_virtual_number();
        return gDtvFramework.VMXGetVirtualNumber();
    }

    public void vmx_stop_ewbs(int mode)//Scoty 20181225 modify VMX EWBS rule//Scoty 20181218 add stop EWBS
    {
//        gPrimeDtvMediaPlayer.vmx_stop_ewbs(mode);
        gDtvFramework.VMXStopEWBS(mode);
    }

    public void vmx_stop_emm() {
//        gPrimeDtvMediaPlayer.vmx_stop_emm();
        gDtvFramework.VMXStopEMM();
    }

    public void vmx_osm_finish(int triggerID, int triggerNum) {
//        gPrimeDtvMediaPlayer.vmx_osm_finish(triggerID, triggerNum);
        gDtvFramework.VMXOsmFinish(triggerID, triggerNum);
    }

    public VMXProtectData get_protect_data() {
//        return gPrimeDtvMediaPlayer.get_protect_data();
        return gDtvFramework.GetProtectData();
    }

    public int set_protect_data(int first, int second, int third) {
//        return gPrimeDtvMediaPlayer.set_protect_data(first, second, third);
        return gDtvFramework.SetProtectData(first, second, third);
    }
    // for VMX need open/close -e

    public int enter_view_activity(int enter) {
//        return gPrimeDtvMediaPlayer.enter_view_activity(enter);
        gDtvFramework.EnterViewActivity(enter);
        return 0;
    }

    public int enable_mem_status_check(int enable) {
//        return gPrimeDtvMediaPlayer.enable_mem_status_check(enable);
        gDtvFramework.EnableMemStatusCheck(enable);
        return 0;
    }

    public int loader_dtv_get_jtag() {
//        return gPrimeDtvMediaPlayer.loader_dtv_get_jtag();
        return gDtvFramework.LoaderDtvGetJTAG();
    }

    public int loader_dtv_set_jtag(int value) {
//        return gPrimeDtvMediaPlayer.loader_dtv_set_jtag(value);
        return gDtvFramework.LoaderDtvSetJTAG(value);
    }

    public int loader_dtv_check_isdbt_service(OTATerrParameters ota) {
//        return gPrimeDtvMediaPlayer.loader_dtv_check_isdbt_service(ota);
        return gDtvFramework.LoaderDtvCheckISDBTService(ota);
    }

    public int loader_dtv_check_terrestrial_service(OTATerrParameters ota) {
//        return gPrimeDtvMediaPlayer.loader_dtv_check_terrestrial_service(ota);
        return gDtvFramework.LoaderDtvCheckTerrestrialService(ota);
    }

    public int loader_dtv_check_cable_service(OTACableParameters ota) {
//        return gPrimeDtvMediaPlayer.loader_dtv_check_cable_service(ota);
        return gDtvFramework.LoaderDtvCheckCableService(ota);
    }

    public int loader_dtv_get_stb_sn() {
//        return gPrimeDtvMediaPlayer.loader_dtv_get_stb_sn();
        return gDtvFramework.LoaderDtvGetSTBSN();
    }

    public int loader_dtv_get_chipset_id() {
//        return gPrimeDtvMediaPlayer.loader_dtv_get_chipset_id();
        return gDtvFramework.LoaderDtvGetChipSetId();
    }

    public int loader_dtv_get_sw_version() {
//        return gPrimeDtvMediaPlayer.loader_dtv_get_sw_version();
        return gDtvFramework.LoaderDtvGetSWVersion();
    }

    public int invoke_test() {
//        return gPrimeDtvMediaPlayer.invoke_test();
        return gDtvFramework.InvokeTest();
    }

    public void widevine_cas_session_id(int sessionIndex, int sessionId) {//eric lin 20210107 widevine cas
//        gPrimeDtvMediaPlayer.widevine_cas_session_id(sessionIndex, sessionId);
        gDtvFramework.WidevineCasSessionId(sessionIndex, sessionId);
    }

    public int set_net_stream_info(int groupType, NetProgramInfo netStreamInfo) {
//        return gPrimeDtvMediaPlayer.set_net_stream_info(groupType, netStreamInfo);
        return gDtvFramework.set_net_stream_info(groupType, netStreamInfo);
    }

    public int reset_program_database() {
//        return gPrimeDtvMediaPlayer.reset_net_program_database();
        return gDtvFramework.reset_net_program_database();
    }

    public int reset_net_program_database() {
//        return gPrimeDtvMediaPlayer.reset_net_program_database();
        return gDtvFramework.reset_net_program_database();
    }

    public int set_module_type(int type){
        return gPrimeDtvMediaPlayer.set_module_type(type);
    }

    public List<EPGEvent> EpgEventGetEPGEventList(long channelId, long startTime, long endTime, int addEmpty) {
        ArrayList<EPGEvent> list = null;

        Log.d(TAG, "EpgEventGetEPGEventList:  start = " + startTime + "         endTime = " + endTime);
        if (( channelId >= 0) && (startTime >= 0)  && (endTime-startTime > 0))
        {
            Log.d(TAG, "getEPGEvents(ChannelID = " + channelId + ",startTime = " + startTime
                    + ",duration = " + (startTime - endTime) + ")");

            int i = 0;
            list = new ArrayList<EPGEvent>();
            List<EPGEvent> secondaryList = null;
            Date startDate = new Date(startTime);
            Date endDate = new Date(endTime);
            while (true)
            {
                // update start offset position
                int offset = ONETIME_OBTAINED_NUMBER * (i++);

                // obtain event list by filter with channel and time enabled

                secondaryList = gDtvFramework.getEPGEvents(channelId, startDate, endDate, offset, ONETIME_OBTAINED_NUMBER, addEmpty);
                if (null == secondaryList)
                {
                    // while no event is obtained
                    break;
                }

                list.addAll(secondaryList);
                int count = secondaryList.size();
                if ((count >= 0) && (count < ONETIME_OBTAINED_NUMBER))
                {
                    // while events by filter are all obtained
                    break;
                }

                EPGEvent lastEvent = secondaryList.get(count - 1);
                if (null == lastEvent)
                {
                    // while the last event is invalid
                    break;
                }

                // Date lastEndDate = lastEvent.getEndTime();
                Date lastEndDate = new Date(lastEvent.get_end_time());

                if (lastEndDate.after(endDate))
                {
                    // while endTime of last event is after endTime of filter
                    break;
                }

                // update endTime of filter by endTime of last event as startTime of filter to next
                // time
                startTime = lastEvent.get_end_time();
                startDate = new Date(startTime);
            }
        }
        else
        {
            Log.d(TAG, "getEPGEvents:one or more parameter is invalid.");
        } // if(((null != channel) && (channel.getChannelID() >= 0)) && (startTime >= 0) &&
        // (duration > 0) )
        return list;
    }

    /**
     * First Time Get netprogram.ini File and Add to DataBase
     * After Save Complete Rename to netprogram_already_set.ini
     * In order to not to save database again
     *
     * @return isSuccess : Save DataBase Results
     */
    public boolean init_net_program_database() {
//        return gPrimeDtvMediaPlayer.init_net_program_database();
        return gDtvFramework.init_net_program_database();
    }

    public class ViewUiDisplay{
        //public ChannelHistory History = ChannelHistory.GetInstance();
        public EPGEvent EpgPreData = null;
        public EPGEvent EpgFolData = null;

        public ViewUiDisplay() {
            if(ViewHistory.getCurChannel() != null)
            {
                UpdateEpgPF();
            }
        }

        public void UpdateEpgPF() {
            if(ViewHistory.getCurChannel() != null) {
                Log.d(TAG, "UpdateEpgPF:  ==>> " + ViewHistory.getCurChannel().get_channel_id());

                EpgPreData = get_present_event(ViewHistory.getCurChannel().get_channel_id());
                EpgFolData = get_follow_event(ViewHistory.getCurChannel().get_channel_id());
            }
        }

        public boolean ChangeChannelByDigi(int displayNum) {
            for(int i = 0; i < ViewHistory.getCurChannelList().size(); i++) {
                if(ViewHistory.getCurChannelList().get(i).get_channel_num() == displayNum) {
                    if(ViewHistory.getCurChannelList().get(i).get_channel_id() != ViewHistory.getCurChannel().get_channel_id())//Scoty 20180801 fixed after unlock the channel and then digit set to the same channel no need show password dialog
                        av_control_play_by_channel_id(ViewHistory.getPlayId(),ViewHistory.getCurChannelList().get(i).get_channel_id(),ViewHistory.getCurGroupType(),1);
                    return true;
                }
            }
            return false;
        }

        public void ChangeProgram()//change channel
        {
            if(ViewHistory.getCurChannel() != null)
            {
                av_control_play_by_channel_id(ViewHistory.getPlayId(), ViewHistory.getCurChannel().get_channel_id(), ViewHistory.getCurGroupType(),1);

                // use MtestTestAvMultiPlay to change program, AvControlPlayByChannelID has problem after split play
//                List<Integer> tunerIDs = new ArrayList<>();
//                List<Long> channelIDs = new ArrayList<>();
//
//                tunerIDs.add(0);
//                channelIDs.add(ViewHistory.getCurChannel().getChannelId());
//                MtestTestAvMultiPlay(1, tunerIDs, channelIDs);
            }
        }

        public void ChangeBannerInfoUp()
        {
            if(ViewHistory.getCurChannelList().size() > 1)
            {
                ViewHistory.setChannelUp();
                UpdateEpgPF();
            }
        }

        public void ChangeBannerInfoDown()
        {
            if(ViewHistory.getCurChannelList().size() > 1)
            {
                ViewHistory.setChannelDown();
                UpdateEpgPF();
            }
        }

        public void ChangePreProgram() {
            if(ViewHistory.getPreChannel() != null &&
                    (ViewHistory.getPreChannel().get_channel_id() != ViewHistory.getCurChannel().get_channel_id() &&
                            ViewHistory.getCurGroupType() == ViewHistory.getPreGroupType())) {
                av_control_play_by_channel_id(ViewHistory.getPlayId(),ViewHistory.getPreChannel().get_channel_id(),ViewHistory.getPreGroupType(),1);
                UpdateEpgPF();
            }
        }

        public boolean ChangeGroup(List<SimpleChannel> channelList, int groupType, long channelId) {
            if (channelList == null)    // Johnny add 20180122
            {
                channelList = new ArrayList<>();
            }

            if(channelList.size() > 0) {
                av_control_play_by_channel_id(ViewHistory.getPlayId(),channelId, groupType,1);
                UpdateEpgPF();
                return true;
            }
            return false;
        }

        public boolean CheckNewMail() {
            return false;
        }

        public String GetShortEvent(long channelID, int eventID)
        {
            return get_short_description(channelID, eventID);
        }
        public String GetDetailInfo(long channelID, int eventID)
        {
            return get_detail_description(channelID, eventID);
        }

        //Pip -Start
        public void OpenPipScreen(int x , int y, int width, int height)
        {
            pip_open(x+4,y+4,width-4,height-4);
            pip_start(ViewHistory.getCurPipChannel().get_channel_id(),1);
        }

        public void ClosePipScreen()
        {
            pip_stop();
            pip_stop();
            ViewHistory.setCurPipIndex(-1);
        }

        public void SetPipWindow(int x, int y, int width, int height)
        {
            pip_set_window(x+4,y+4,width - 4,height - 4);
        }

        public void SetAvPipExChange()
        {
            ViewHistory.setAvPipExchange();
            pip_exchange();
        }

        public void ChangePipChannelUp()
        {
            if(ViewHistory.getCurChannelList().size() > 1)
            {
                ViewHistory.setPipChannelUp();
            }
        }

        public void ChangePipChannelDown()
        {
            if(ViewHistory.getCurChannelList().size() > 1)
            {
                ViewHistory.setPipChannelDown();
            }
        }

        public void ChangePipProgram()
        {
            pip_start(ViewHistory.getCurPipChannel().get_channel_id(),1);
        }

        public void UpdatePipEpgPF() {
            if(ViewHistory.getCurPipChannel() != null) {
                Log.d(TAG, "UpdatePipEpgPF:  ==>> " + ViewHistory.getCurPipChannel().get_channel_id());

                EpgPreData = get_present_event(ViewHistory.getCurPipChannel().get_channel_id());
                EpgFolData = get_follow_event(ViewHistory.getCurPipChannel().get_channel_id());
            }
        }

        public boolean ChangePipChannelByDigi(int displayNum, int tpId) {
            for(int i = 0; i < ViewHistory.getCurChannelList().size(); i++) {
                if(ViewHistory.getCurChannelList().get(i).get_channel_num() == displayNum) {
                    pip_start(ViewHistory.getCurChannelList().get(i).get_channel_id(),1);
                    return true;
                }
            }
            return false;
        }

        //Pip -End
    }

    public ViewUiDisplay GetViewUiDisplay()
    {
        if(ViewUIDisplayManager == null)
            ViewUIDisplayManager = new ViewUiDisplay();

        return ViewUIDisplayManager;
    }

    private static Object lock = new Object();
    public class EpgUiDisplay {
        private int GroupType;
        public List<SimpleChannel> programInfoList = null;
        public List<EPGEvent> epgDisplayData = new ArrayList<>();
        public List<EPGEvent> epgUpdateData = new ArrayList<>();

        public EpgUiDisplay(int groupType) {
            GroupType = groupType;
            programInfoList = get_cur_play_channel_list(groupType,0);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
        }

        private List<EPGEvent> EpgPFDataUpdate(int position) {
            SimpleChannel programInfo = programInfoList.get(position);
            List<EPGEvent> tempData = new ArrayList<>();
            EPGEvent present = get_present_event(programInfo.get_channel_id());
            EPGEvent follow = get_follow_event(programInfo.get_channel_id());
            if(present != null)
                tempData.add(present);
            if(follow != null)
                tempData.add(follow);
            if(tempData.size() > 0)
                return tempData;
            else
                return null;
        }

        private List<EPGEvent> EpgScheduleDataUpdate(int position,long startTime, long endTime, int addEmpty) {
            SimpleChannel programInfo = programInfoList.get(position);
            //should remove in hisillion !!-s
//            List<EPGEvent> tempData = new ArrayList<>();
//            List<EPGEvent> epgEventList = EpgEventGetEPGEventList(programInfo.getChannelId(),startTime,endTime);
//            if((epgEventList == null) || (epgEventList.size() == 0))
//                return null;
//            else {
//                for(int i = 0; i < epgEventList.size(); i++) {
//                long EpgStartTime = GetStartTime(epgEventList.get(i));
//                long EpgEndTime = GetEndTime(epgEventList.get(i));
//                if((startTime <= EpgStartTime && endTime >= EpgEndTime) || (endTime > EpgStartTime && endTime < EpgEndTime)
//                        || (startTime < EpgEndTime && startTime > EpgStartTime) || (startTime > EpgStartTime && endTime < EpgEndTime)) {
//                tempData.add(epgEventList.get(i));
//                }
//                }
//                return tempData;
            //return epgEventList; // speed up epg
//            }
            //should remove in hisillion !!-e
            //should use in hisillion !!-s
            List<EPGEvent> epgEventList = EpgEventGetEPGEventList(programInfo.get_channel_id(),startTime,endTime, addEmpty);
            return epgEventList;
            //should use in hisillion !!-e
        }

        public void EitDataUpdate(int position,long startTime, long endTime, int addEmpty) {
            synchronized (lock) {
                if(epgUpdateData != null)
                    epgUpdateData.clear();
                epgUpdateData = EpgScheduleDataUpdate(position, startTime, endTime, addEmpty);
                if(epgUpdateData == null || epgUpdateData.size() == 0) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String srcDate = sdf.format(new Date(startTime));
                    List<EPGEvent> tempEventList = EpgPFDataUpdate(position);
                    if(tempEventList != null && tempEventList.size()!=0) {
                        String EpgDate = sdf.format(tempEventList.get(0).get_start_time());   // johnny modify 20171227
                        if(srcDate.equals(EpgDate) == true) {
                            epgUpdateData = EpgPFDataUpdate(position);
                        }
                    }
                }
            }
        }

        public void EitDataDisplayUpdate() {
            epgDisplayData.clear();
            if(epgUpdateData != null && epgUpdateData.size() != 0) {
                for(int i = 0; i < epgUpdateData.size(); i++) {
                    epgDisplayData.add(epgUpdateData.get(i));
                }
            }
        }

        public void ChangeProgram(int position) {
            Log.d(TAG, "ChangeProgram: position == " + position);
            final int pos = position;
            if(ViewHistory.getCurChannel() != null) {
                if (ViewHistory.getCurChannel().get_channel_id() != programInfoList.get(pos).get_channel_id())
                {
                    av_control_play_by_channel_id(ViewHistory.getPlayId(), programInfoList.get(pos).get_channel_id(), GroupType,1);
                }
            }
        }

        public boolean ChangeGroup(int groupType) {
            List<SimpleChannel> tempList = null;
            tempList = get_cur_play_channel_list(groupType,0);//Scoty 20180615 recover get simple channel list function//Scoty 20180613 change get simplechannel list for PvrSkip rule
            if(tempList != null) {
                GroupType = groupType;
                programInfoList = tempList;
                return true;
            }
            return false;
        }

        public String GetShortEvent(long channelID, int eventID) {
            return get_short_description(channelID, eventID);
        }

        public String GetDetailInfo(long channelID, int eventID)
        {
            return get_detail_description(channelID, eventID);
        }
    }

    public class BookManager {
        public List<BookInfo> BookList = null;
        public BookManager() {
            //BookList = BookInfoGetList();
            BookList = UIBookList;
            if(BookList == null)
                BookList = new ArrayList<>();
        }

        public void UpdateUIBookList()
        {
            BookList = UIBookList;
        }

        public BookInfo CheckExist(long channelId, int groupType,long startTime,long duration) {
            long MINUTE_IN_MILLIS = TimeUnit.MINUTES.toMillis(1);
            long checkBookStartTime = startTime - startTime%MINUTE_IN_MILLIS; // connie 20180530 fix check time exist fail
            long checkBookDuration = duration - duration%MINUTE_IN_MILLIS;// connie 20180530 fix check time exist fail
            for(int i = 0; i < BookList.size(); i++) {
                long BookStartTime = 0,BookDuration = 0;
                String BookStartTimeString = BookList.get(i).getYear() + "-" + String.format("%02d",BookList.get(i).getMonth()) + "-"
                        + String.format("%02d",BookList.get(i).getDate())
                        + " " +  String.format("%02d",BookList.get(i).getStartTime()/100) + ":" + String.format("%02d",BookList.get(i).getStartTime()%100)
                        + ":" + "00";
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date BookDate = null;
                try {
                    BookDate = sdf.parse(BookStartTimeString);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return null;    // johnny add 20171221
                }
                BookStartTime = BookDate.getTime();
                BookDuration = BookList.get(i).getDuration()/100*60*60*1000 + BookList.get(i).getDuration()%100*60*1000;
                if(BookList.get(i).getChannelId() == channelId /*&& BookList.get(i).getGroupType() == groupType*/
                        && BookStartTime == checkBookStartTime
                        && BookDuration == checkBookDuration)// connie 20180530 fix check time exist fail
                {
                    return BookList.get(i);
                }
            }
            return null;
        }

        public boolean CheckFull() {
            if(BookList.size() >= BookInfo.MAX_NUM_OF_BOOKINFO)
                return true;
            else
                return false;
        }

        public int GetEmptyBookId() {
            if(BookList.size() >= BookInfo.MAX_NUM_OF_BOOKINFO)
                return BookInfo.MAX_NUM_OF_BOOKINFO;
            else
                return BookList.size();
        }

        public void AddBookInfo(BookInfo bookInfo) {
            BookList.add(bookInfo);
        }

        public void DelBookInfo(BookInfo bookInfo) {
            for(int i = 0; i < BookList.size(); i++) {
                if(bookInfo.getBookId() == BookList.get(i).getBookId())
                    BookList.remove(i);
            }
            for(int i = 0; i < BookList.size(); i++) {
                BookList.get(i).setBookId(i);
            }
        }

        // Johnny add 20180316, use this to del if you want to del several books.
        // Rearrange BookId after del the whole bookList rather than del one book.
        public void DelBookInfoList(List<BookInfo> bookInfoList) {
            if (bookInfoList == null)
            {
                return;
            }

            for(int i = 0; i < bookInfoList.size(); i++) {
                for (int j = 0 ; j < BookList.size() ; j++)
                {
                    if(bookInfoList.get(i).getBookId() == BookList.get(j).getBookId()) {
                        BookList.remove(j);
                        break;
                    }
                }
            }

            for(int i = 0; i < BookList.size(); i++) {
                BookList.get(i).setBookId(i);
            }
        }

        public int DelBookInfoByChannelId(long channelId) {
            List<BookInfo> found = new ArrayList<>();
            int foundChannel = 0;//Scoty 20180523 modify check timer channel exist or not
            for(BookInfo bookInfo : BookList) {
                if(channelId == bookInfo.getChannelId())
                {
                    found.add(bookInfo);
                    foundChannel = 1;//Scoty 20180523 modify check timer channel exist or not
                }
            }

            if(foundChannel == 1) {//Scoty 20180523 modify check timer channel exist or not
                BookList.removeAll(found);

                for (int i = 0; i < BookList.size(); i++) {
                    BookList.get(i).setBookId(i);
                }
            }
            return foundChannel;
        }
        public Date GetEndTime(BookInfo bookInfo){
            long BookStartTime = 0,BookDuration = 0;
            String BookStartTimeString = bookInfo.getYear() + "-" + String.format("%02d",bookInfo.getMonth()) + "-"
                    + String.format("%02d",bookInfo.getDate())
                    + " " +  String.format("%02d",bookInfo.getStartTime()/100) + ":" + String.format("%02d",bookInfo.getStartTime()%100)
                    + ":" + "00";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date BookDate = null;
            try {
                BookDate = sdf.parse(BookStartTimeString);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
            BookStartTime = BookDate.getTime();
            BookDuration = bookInfo.getDuration()/100*60*60*1000 + bookInfo.getDuration()%100*60*1000;
            Date EndTime = new Date(BookStartTime+BookDuration);
            //Log.d(TAG,"endtime ==> startTime " + startTime + " duration ==>> " + duration + "hour " + hour + " min " + min);
            return EndTime;
        }

        public boolean CheckBookAfterNow(BookInfo bookInfo) {
            Date date = gPrimeDtvMediaPlayer.get_dtv_date();
            String timeString = bookInfo.getYear() + "-" + String.format("%02d",bookInfo.getMonth()) + "-" + String.format("%02d",bookInfo.getDate())
                    + " " +  String.format("%02d",bookInfo.getStartTime()/100) + ":" + String.format("%02d",bookInfo.getStartTime()%100) + ":"
                    + "00";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date BookDate=null;
            try {
                BookDate = sdf.parse(timeString);
                if((BookDate.getTime()) > date.getTime()+60*1000)
                    return true;
                else
                    return false;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return false;
        }

//        public void Save(List<BookInfo> bookList) {
//            if(bookList.size() > 0) {
//                BookInfoUpdateList(bookList);
//            }
//            else {
//                BookInfoDeleteAll();
//            }
//
//            SaveTable(EnTableType.TIMER);
//        }

        public void Save() {
            Log.d(TAG, "Save: size = [" + BookList.size() +"]");
            if(BookList.size() > 0) {
                gPrimeDtvMediaPlayer.update_book_list(BookList);
            }
            else {
                gPrimeDtvMediaPlayer.book_info_delete_all();
            }
            //BookList = GetUIBookList();
            gPrimeDtvMediaPlayer.save_table(EnTableType.TIMER);
        }
    }
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        }
        return false;
    }
    void set_wifi_led(int value){
        try {
            LogUtils.d("value = "+value);
            mPrimeMiscService.SetGpio(53, 1, value);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
    void set_power_led(int value){
        try {
            LogUtils.d("value = "+value);
            mPrimeMiscService.SetGpio(27, 1, value);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
    void set_standby_led(int value){
        try {
            LogUtils.d("value = "+value);
            mPrimeMiscService.SetGpio(26, 1, value);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void remove_wvcas_license(String licenseId) {
        try {
            LogUtils.d("remove licenseId = " + licenseId);
            mPrimeMediaService.removeWvcasLicense(licenseId);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isRemoteControl (BluetoothDevice device) {
        BluetoothClass bluetoothClass = device.getBluetoothClass();
        LogUtils.d("isRemoteControl device="+device+" name="+device.getName()
                +" getDeviceClass="+bluetoothClass.getDeviceClass()
                +" devicetype="+device.getType());
        if (bluetoothClass.getMajorDeviceClass() == BluetoothClass.Device.Major.PERIPHERAL &&
                BluetoothDevice.DEVICE_TYPE_LE == device.getType() &&
                (((bluetoothClass.getDeviceClass() & MINOR_DEVICE_CLASS_REMOTE)!= 0)
                        || ((bluetoothClass.getDeviceClass() & MINOR_DEVICE_CLASS_KEYBOARD)!= 0))) {
            Log.d(TAG,"isRemoteControl device="+device+" is remote control");
            return true;
        }
        return false;
    }
    public static boolean hasRemoteControl () {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter != null) {
            final Set<BluetoothDevice> bondedDevices = btAdapter.getBondedDevices();
            if (bondedDevices != null) {
                for (BluetoothDevice device : bondedDevices) {
                    if (isRemoteControl(device)) {
                        Log.d(TAG, "Still has remote control device="+device);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void check_bt_remote_connect(Context context) {
        if (hasRemoteControl())
            return;
        Intent newIntent = new Intent();
        newIntent.setComponent(new ComponentName(PRIME_BTPAIR_PACKAGE, PRIME_BTPAIR_HOOKBEGINACTIVITY));
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(newIntent);
    }

    public void handleCasRefresh(final int deleteFlag, final int delay, long channel_id) {
        Log.d(TAG, "handleCasRefresh: deleteFlag = " + deleteFlag + " delay = " + delay+" channel_id = "+channel_id);
        Thread newThread = new Thread(() -> {
            boolean need_set_channel = false;
            ProgramInfo programInfo = get_program_by_channel_id(channel_id);
            String content_id = null;
            if(programInfo != null) {
                if(programInfo.getType() == ProgramInfo.PROGRAM_TV) {
                    content_id = CasRefreshHelper.parse_content_id(programInfo.pVideo.getPrivateData(19156));
                } else {
                    content_id = CasRefreshHelper.parse_content_id(programInfo.pAudios.get(0).getPrivateData(19156));
                }
                LogUtils.d("handleCasRefresh Check content_id "+content_id);
            }
            if (delay > 0) {
                try {
                    Thread.sleep(delay * 1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            CasRefreshHelper casRefreshHelper = CasRefreshHelper.get_instance();
            CasData old_casData = casRefreshHelper.get_cas_data();
            List<String> old_EntitledContentIds = old_casData.getEntitledChannelIds();
            LogUtils.d("old_EntitledContentIds = "+old_EntitledContentIds);
            // get latest cas data from server
            casRefreshHelper.request_new_cas_data();
            CasData casData = casRefreshHelper.get_cas_data();
            List<String> new_EntitledContentIds = casData.getEntitledChannelIds();
            LogUtils.d("new_EntitledContentIds = "+new_EntitledContentIds);
            // update configs
            Pvcfg.setTryLicenseEntitledOnly(casData.getTryLicenseIfEntitledChannel() == 1);
            //Pvcfg.setPVR_PJ(casData.getPvr() == 1);

            // send suspended
            if (casData.getSuspended() == 1) {
                sendCasError(ErrorCodeUtil.ERROR_E511, "");
            }
            List<String> unentitledContentIds = casRefreshHelper.get_unentitled_content_ids();
            LogUtils.d("unentitledContentIds = "+unentitledContentIds);
            // remove offline licenses
            if (deleteFlag == 1) {
                // remove all offline license file
                remove_wvcas_license(""); // empty string will remove all
                // clear lic record in lic map
                casRefreshHelper.clear_license_mapping();

                //need_set_channel = true;
            }
            else {
                for (String contentId : unentitledContentIds) {
                    String licenseId = casRefreshHelper.get_license_id(contentId);
                    if (licenseId != null) {
                        // remove unentitled offline license file
                        Log.d(TAG, "handleCasRefresh: licenseId = " + licenseId);
                        remove_wvcas_license(licenseId);
                        // remove unentitled lic record in lic map
                        casRefreshHelper.remove_license_mapping(contentId, licenseId);
                    }
                }
            }
            // try licenses
            if (casData.getTryLicenseAfterRefresh() == 1) {
                List<String> entitledChannelIds = casData.getEntitledChannelIds();
                for (String contentId : entitledChannelIds) {
                    //if(need_set_channel == true && contentId.equals(content_id))
                    //    continue;
                    try {
                        byte[] privateData = CasRefreshHelper.generate_private_data(casData.getCasProvider(), contentId);

                        CasSession casSession = new CasSession(Pvcfg.WVCAS_CA_SYSTEM_ID);
                        casSession.setPrivateData(privateData);
                        casSession.openForRefresh();
                        Thread.sleep(300);
                    } catch (Exception e) {
//                        throw new RuntimeException(e);
                    }
                }
            }
            LogUtils.d("programInfo = "+programInfo);
            if(programInfo != null){
                if(unentitledContentIds.contains(content_id))
                    need_set_channel = true;
                else{
                    if(new_EntitledContentIds.contains(content_id) && !old_EntitledContentIds.contains(content_id))
                        need_set_channel = true;
                }
                if(old_casData.getSuspended() != casData.getSuspended())
                    need_set_channel = true;
                LogUtils.d("getSuspended = "+old_casData.getSuspended()+" "+casData.getSuspended());
                LogUtils.d("unentitledContentIds.contains(content_id) = "+unentitledContentIds.contains(content_id));
                LogUtils.d("old_unentitledContentIds.contains(content_id) = "+old_EntitledContentIds.contains(content_id));
                LogUtils.d("new_unentitledContentIds.contains(content_id) = "+unentitledContentIds.contains(content_id));
                LogUtils.d("need_set_channel = "+need_set_channel);
                if(need_set_channel) {
                    TVMessage tvMessage = TVMessage.SetChannel(programInfo.getChannelId());
                    gDtvCallback.onMessage(tvMessage);
                    av_control_play_stop(0, 0, 0);
                    av_control_play_by_channel_id(programInfo.getTunerId(), programInfo.getChannelId(), ViewHistory.getCurGroupType(), 1);
                }
            }
            LogUtils.d("End");
        },"handleCasRefresh");

        newThread.start();
    }

    private void sendCasError(final int errorCode, final String msg) {
        Log.d(TAG, "sendCasError: errorCode = " + errorCode + " msg = " + msg);
        TVMessage tvMessage = TVMessage.SetShowErrorMsg(errorCode, msg);
        gDtvCallback.onMessage(tvMessage);
    }

    public void ResetCheckAD(){
        gDtvFramework.ResetCheckAD();
    }

    public int add_series(long channelId, byte[] key){
        return gPrimeDtvMediaPlayer.add_series(channelId,key);
    }

    public int delete_series(long channelId, byte[] key){
        return gPrimeDtvMediaPlayer.delete_serires(channelId,key);
    }

    public SeriesInfo get_series_info(long channelId){
        return gPrimeDtvMediaPlayer.get_series_info(channelId);
    }

    public List<SeriesInfo> get_all_series_data(){
        return gPrimeDtvMediaPlayer.get_all_series_data();
    }

    public SeriesInfo.Series get_series(long channelId, byte[] key){
        return gPrimeDtvMediaPlayer.get_series(channelId, key);
    }

    public int save_series() {
        return gPrimeDtvMediaPlayer.save_series();
    }

    private LocalDateTime get_next_date_onetime(LocalDateTime localDateTime, LocalDateTime bookDateTime) {
        LocalDateTime nextDateTime = bookDateTime;

        // next book datetime <= local datetime
        if (!nextDateTime.isAfter(localDateTime)) {
            nextDateTime = null; // no next date if onetime
        }

        return nextDateTime;
    }

    private LocalDateTime get_next_date_daily(LocalDateTime localDateTime, LocalDateTime bookDateTime) {
        LocalDateTime nextDateTime = bookDateTime;

        // next book datetime <= local datetime
        if (!nextDateTime.isAfter(localDateTime)) {
            nextDateTime = bookDateTime.with(localDateTime.toLocalDate()); // set next book date to today
            if (!nextDateTime.isAfter(localDateTime)) { // if next book datetime still  <= local datetime
                nextDateTime = nextDateTime.plusDays(1); // next day
            }
        }

        return nextDateTime;
    }

    private LocalDateTime get_next_date_weekly(LocalDateTime localDateTime, LocalDateTime bookDateTime) {
        LocalDateTime nextDateTime = bookDateTime;

        // next book datetime <= local datetime
        if (!nextDateTime.isAfter(localDateTime)) {
            DayOfWeek bookDayOfWeek = bookDateTime.getDayOfWeek();
            nextDateTime = localDateTime
                    .with(TemporalAdjusters.nextOrSame(bookDayOfWeek)) // next or same dayOfWeek
                    .with(bookDateTime.toLocalTime());
            if (!nextDateTime.isAfter(localDateTime)) { // if next book datetime still <= local datetime
                nextDateTime = nextDateTime.plusWeeks(1); // next week
            }
        }

        return nextDateTime;
    }

    private LocalDateTime get_next_date_weekend(LocalDateTime localDateTime, LocalDateTime bookDateTime) {
        LocalDateTime nextDateTime = bookDateTime;

        // next book datetime <= local datetime
        if (!nextDateTime.isAfter(localDateTime)) {
            nextDateTime = bookDateTime.with(localDateTime.toLocalDate()); // set next book datetime to today
            if (nextDateTime.getDayOfWeek() == DayOfWeek.SATURDAY
                    && !nextDateTime.isAfter(localDateTime)) { // Saturday && next book datetime still <= local datetime
                nextDateTime = nextDateTime.plusDays(1); // now(Saturday) + 1 day = Sunday
            }
            else if (nextDateTime.getDayOfWeek() == DayOfWeek.SUNDAY
                    && !nextDateTime.isAfter(localDateTime)) { // Sunday && next book datetime still <= local datetime
                nextDateTime = nextDateTime.plusDays(6); // now(Sunday) + 6 day = Saturday
            }
            else { // now = Monday, Tuesday, ..., Friday
                nextDateTime = nextDateTime
                        .with(TemporalAdjusters.next(DayOfWeek.SATURDAY)); // next Saturday
            }
        }

        return nextDateTime;
    }

    private LocalDateTime get_next_date_weekdays(LocalDateTime localDateTime, LocalDateTime bookDateTime) {
        LocalDateTime nextDateTime = bookDateTime;

        // next book datetime <= local datetime
        if (!nextDateTime.isAfter(localDateTime)) {
            nextDateTime = bookDateTime.with(localDateTime.toLocalDate()); // set next book datetime to today
            if (nextDateTime.getDayOfWeek() == DayOfWeek.SATURDAY
                    || nextDateTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
                nextDateTime = nextDateTime.with(TemporalAdjusters.next(DayOfWeek.MONDAY)); // next Monday
            }
            else if (nextDateTime.getDayOfWeek() == DayOfWeek.FRIDAY) {
                if (!nextDateTime.isAfter(localDateTime)) { // next book datetime still <= local datetime
                    nextDateTime = nextDateTime.with(TemporalAdjusters.next(DayOfWeek.MONDAY)); // next Monday
                }
            }
            else { // Monday, Tuesday, ..., Thursday
                if (!nextDateTime.isAfter(localDateTime)) { // next book datetime still <= local datetime
                    nextDateTime = nextDateTime.plusDays(1); // next day
                }
            }
        }

        return nextDateTime;
    }

    private LocalDateTime get_next_date_monthly(LocalDateTime localDateTime, LocalDateTime bookDateTime) {
        LocalDateTime nextDateTime = bookDateTime;

        // next book datetime <= local datetime
        if (!nextDateTime.isAfter(localDateTime)) {
            // set to this year to reduce searching time
            nextDateTime = nextDateTime.withYear(localDateTime.getYear());
        }

        // find next valid month with book's dayOfMonth
        // e.g. 01/28 -> 02/28, 01/31 -> 03/31
        while (!nextDateTime.isAfter(localDateTime) // next book datetime <= local datetime
                || nextDateTime.getDayOfMonth() != bookDateTime.getDayOfMonth()) {
            try {
                nextDateTime = nextDateTime.plusMonths(1); // next month
                nextDateTime = nextDateTime.withDayOfMonth(bookDateTime.getDayOfMonth()); // set date to book date
            } catch (DateTimeException ignored) {

            }
        }

        return nextDateTime;
    }

    // bookInfo may be modified if needed
    private LocalDateTime get_next_date_series(LocalDateTime localDateTime, LocalDateTime bookDateTime, @NonNull BookInfo bookInfo) {
        LocalDateTime nextDateTime = bookDateTime;

        // next book datetime <= local datetime
        if (!nextDateTime.isAfter(localDateTime)) {
            add_series(bookInfo.getChannelId(), bookInfo.getSeriesRecKey());
            SeriesInfo.Series series = get_series(bookInfo.getChannelId(), bookInfo.getSeriesRecKey());

            boolean found = false;
            if (series != null) {
                // try to find valid episode in series
                for (SeriesInfo.Episode episode : series.getEpisodeList()) {
                    LocalDateTime episodeStartTime = episode.getStartLocalDateTime();
                    if (!episodeStartTime.isBefore(bookDateTime) // episodeStartTime >= bookDateTime
                            && episodeStartTime.isAfter(localDateTime) // episodeStartTime > localDateTime
                            // pvr_CheckSeriesEpisode != 0 means series not recorded
                            && pvr_CheckSeriesEpisode(bookInfo.getSeriesRecKey(), episode.getEpisodeKey()) != 0) {
                        nextDateTime = episodeStartTime;
                        bookInfo.setEpisode(episode.getEpisodeKey());
                        bookInfo.setEventName(episode.getEventName());

                        // minute to HHmm // may change later
                        int bookDuration = episode.getDuration() / 60 * 100 + episode.getDuration() % 60;
                        bookInfo.setDuration(bookDuration);
                        found = true;
                        break;
                    }
                }
            }

            if (found) { // valid episode found
                // book cycle may be BOOK_CYCLE_SERIES_EMPTY so set back to BOOK_CYCLE_SERIES
                bookInfo.setBookCycle(BookInfo.BOOK_CYCLE_SERIES);
            } else  { // no valid episode found
                LocalDateTime expireTime = bookDateTime.plusWeeks(Pvcfg.SERIES_EXPIRE_WEEKS);
                if (localDateTime.isAfter(expireTime)) { // expired
                    nextDateTime = null;
                } else { // not expired
                    // not expired but no valid episode
                    // set book cycle to BOOK_CYCLE_SERIES_EMPTY
                    bookInfo.setBookCycle(BookInfo.BOOK_CYCLE_SERIES_EMPTY);
                }
            }
        }

        return nextDateTime;
    }

    private int get_pesi_day_of_week(DayOfWeek dayOfWeek) {
        int pesiDayOfWeek = 0;
        switch (dayOfWeek) {
            case MONDAY -> {
                pesiDayOfWeek = BookInfo.BOOK_WEEK_DAY_MONDAY;
            }
            case TUESDAY -> {
                pesiDayOfWeek = BookInfo.BOOK_WEEK_DAY_TUESDAY;
            }
            case WEDNESDAY -> {
                pesiDayOfWeek = BookInfo.BOOK_WEEK_DAY_WEDNESDAY;
            }
            case THURSDAY -> {
                pesiDayOfWeek = BookInfo.BOOK_WEEK_DAY_THURSDAY;
            }
            case FRIDAY -> {
                pesiDayOfWeek = BookInfo.BOOK_WEEK_DAY_FRIDAY;
            }
            case SATURDAY -> {
                pesiDayOfWeek = BookInfo.BOOK_WEEK_DAY_SATURDAY;
            }
            case SUNDAY -> {
                pesiDayOfWeek = BookInfo.BOOK_WEEK_DAY_SUNDAY;
            }
        }

        return pesiDayOfWeek;
    }

    /*
    mode = 1 force
    mode = 0 normal
     */
    public void set_timer_next_date_by_cycle(/*int mode,*/ int bookId, BookInfo pbookinfo) {
        BookInfo bookinfo;
        if(pbookinfo != null) {
            bookinfo = pbookinfo;
        }
        else {
            bookinfo = book_info_get(bookId);
        }

        if(bookinfo == null) {
            LogUtils.e("Can't find bookinfo !!!!!! bookId = "+ bookId +" pbookinfo = " + pbookinfo);
            return;
        }

        LogUtils.d("bookinfo in = " + bookinfo.ToString());

        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime bookDateTime;
        try {
            bookDateTime = LocalDateTime.of(
                    bookinfo.getYear(),
                    bookinfo.getMonth(),
                    bookinfo.getDate(),
                    bookinfo.getStartTime() / 100,
                    bookinfo.getStartTime() % 100);
        } catch (DateTimeException exception) {
            LogUtils.e(exception.getMessage());
            book_info_delete(bookinfo.getBookId()); // delete invalid book
            return;
        }

//        // according to pesidtv preserv.c
//        // return if 1. normal mode 2. recording time has not arrived yet
//        if (mode == 0 && !localDateTime.isAfter(bookDateTime)) {
//            LogUtils.d("Normal mode and recording time has not arrived yet, skip schedule next timer date");
//            return;
//        }

        int cycle = bookinfo.getBookCycle();
        LocalDateTime nextBookDateTime = null;
        switch (cycle) {
            case BookInfo.BOOK_CYCLE_ONETIME -> {
                nextBookDateTime = get_next_date_onetime(localDateTime, bookDateTime);
            }
            case BookInfo.BOOK_CYCLE_DAILY -> {
                nextBookDateTime = get_next_date_daily(localDateTime, bookDateTime);
            }
            case BookInfo.BOOK_CYCLE_WEEKLY -> {
                nextBookDateTime = get_next_date_weekly(localDateTime, bookDateTime);
            }
            case BookInfo.BOOK_CYCLE_WEEKEND -> {
                nextBookDateTime = get_next_date_weekend(localDateTime, bookDateTime);
            }
            case BookInfo.BOOK_CYCLE_WEEKDAYS -> {
                nextBookDateTime = get_next_date_weekdays(localDateTime, bookDateTime);
            }
            case BookInfo.BOOK_CYCLE_MONTHLY -> {
                nextBookDateTime = get_next_date_monthly(localDateTime, bookDateTime);
            }
            case BookInfo.BOOK_CYCLE_SERIES, BookInfo.BOOK_CYCLE_SERIES_EMPTY -> {
                nextBookDateTime = get_next_date_series(localDateTime, bookDateTime, bookinfo);
            }
            default -> {
                LogUtils.w("Unknown book cycle...");
            }
        }

        LogUtils.d("now = " + localDateTime
                + " preBookDateTime = " + bookDateTime
                + " nextBookDateTime = " + nextBookDateTime);
        if (nextBookDateTime == null) {
            book_info_delete(bookinfo.getBookId()); // del bookinfo if no next date
        }
        else {
            bookinfo.setDate(nextBookDateTime.getDayOfMonth());
            bookinfo.setMonth(nextBookDateTime.getMonthValue());
            bookinfo.setYear(nextBookDateTime.getYear());
            bookinfo.setWeek(get_pesi_day_of_week(nextBookDateTime.getDayOfWeek()));
            bookinfo.setStartTime(nextBookDateTime.getHour()*100 + nextBookDateTime.getMinute());

            book_info_update(bookinfo); // update bookinfo
            LogUtils.d("bookinfo out = " + bookinfo.ToString());
        }
    }

    public void saveGposKeyValue(String key, String value){
        gDtvFramework.saveGposKeyValue(key, value);
    }
    public void saveGposKeyValue(String key, int value){
        gDtvFramework.saveGposKeyValue(key, value);
    }
    public void saveGposKeyValue(String key, long value){
        gDtvFramework.saveGposKeyValue(key, value);
    }

    public void backupDatabase(boolean force) {
        gDtvFramework.backupDatabase(force);
    }
}
