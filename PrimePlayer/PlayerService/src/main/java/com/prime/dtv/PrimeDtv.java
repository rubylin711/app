package com.prime.dtv;

import static com.prime.dtv.Interface.BaseManager.getPesiDtvFrameworkInterfaceCallback;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.tv.TvContentRating;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prime.android.SystemAPP.PrimeSystemApp;
import com.prime.datastructure.ServiceDefine.PrimeDtvInterface;
import com.prime.datastructure.config.Pvcfg;
import com.prime.datastructure.sysdata.AudioInfo;
import com.prime.datastructure.sysdata.BookInfo;
import com.prime.datastructure.sysdata.CaStatus;
import com.prime.datastructure.sysdata.CasData;
import com.prime.datastructure.sysdata.ChannelNode;
import com.prime.datastructure.sysdata.DTVMessage;
import com.prime.datastructure.sysdata.DefaultChannel;
import com.prime.datastructure.sysdata.EPGEvent;
import com.prime.datastructure.sysdata.EnAudioTrackMode;
import com.prime.datastructure.sysdata.EnTVRadioFilter;
import com.prime.datastructure.sysdata.EnTableType;
import com.prime.datastructure.sysdata.EnUseGroupType;
import com.prime.datastructure.sysdata.FavGroup;
import com.prime.datastructure.sysdata.FavGroupName;
import com.prime.datastructure.sysdata.FavInfo;
import com.prime.datastructure.sysdata.GposInfo;
import com.prime.datastructure.sysdata.IrdetoInfo;
import com.prime.datastructure.sysdata.LoaderInfo;
import com.prime.datastructure.sysdata.MailData;
import com.prime.datastructure.sysdata.MiscDefine;
import com.prime.datastructure.sysdata.MusicInfo;
import com.prime.datastructure.sysdata.NetProgramInfo;
import com.prime.datastructure.sysdata.OTACableParameters;
import com.prime.datastructure.sysdata.OTATerrParameters;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.PvrInfo;
import com.prime.datastructure.sysdata.PvrRecFileInfo;
import com.prime.datastructure.sysdata.PvrRecIdx;
import com.prime.datastructure.sysdata.PvrRecStartParam;
import com.prime.datastructure.sysdata.SatInfo;
import com.prime.datastructure.sysdata.SeriesInfo;
import com.prime.datastructure.sysdata.SimpleChannel;
import com.prime.datastructure.sysdata.SubtitleInfo;
import com.prime.datastructure.sysdata.TeletextInfo;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.sysdata.VMXProtectData;
import com.prime.datastructure.utils.ErrorCode;
import com.prime.datastructure.utils.JsonParser;
import com.prime.datastructure.utils.LogUtils;
import com.prime.datastructure.utils.TVMessage;
import com.prime.datastructure.utils.TVScanParams;
import com.prime.datastructure.utils.TVTunerParams;
import com.prime.datastructure.utils.Utils;
import com.prime.dtv.Interface.PesiDtvFrameworkInterface;
import com.prime.datastructure.sysdata.CNSMailData;
import com.prime.datastructure.sysdata.CNSEasData;
import com.prime.dtv.service.CNS.IrdCommand;
import com.prime.dtv.service.CNS.CNSMailParser;
import com.prime.dtv.service.CNS.CNSEasParser;
import com.prime.dtv.service.database.dvbdatabasetable.CNSMailDatabaseTable;
import com.prime.dtv.service.database.dvbdatabasetable.CNSEasDatabaseTable;
import com.prime.dtv.service.Hdmi.EdidInfo;
import com.prime.dtv.service.Player.AvCmdMiddle;
import com.prime.dtv.service.Player.CasSession;
import com.prime.dtv.service.Scan.Scan_utils;
import com.prime.dtv.service.dsmcc.DsmccService;
import com.prime.dtv.utils.HdmiCecUtil;
import com.prime.dtv.utils.TvInputManagerUtils;
import com.prime.dtv.utils.NetworkUnit;
import com.prime.dtv.utils.UsbUtils;

import com.google.gson.GsonBuilder;

import java.io.RandomAccessFile;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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

    private static PrimeDtv gPrimeDtv = null;

    private PesiDtvFrameworkInterface gDtvFramework = null;
    private final PrimeDtvMediaPlayer gPrimeDtvMediaPlayer;
    private final PrimeDtvInterface.DTVCallback gDtvCallback;
    private int gAlreadySearchTpCount = 0;
    private int gTvChCount = 0;
    private int gRadioChCount = 0;
    private Context mContext = null;
    private TIFEpgUpdateManager gTIFEpgUpdateManager = null;

    private ViewUiDisplay ViewUIDisplayManager = null;
    private EpgUiDisplay EpgUiDisplayManager = null;
    private BookManager UIBookManager = null;
    private List<BookInfo> UIBookList = null;
    private DataFromLauncher gDataFromLauncher = null;

    private IMisc mPrimeMiscService;
    private IMedia mPrimeMediaService;
    private HDDInfoManger mHDInfomanger = null;
    // --- VBM ---
    private long mLastVbmServiceId = -1;
    private long mLastVbmEnterTime = 0;
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

    private static final Gson VBM_GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    private static final class VbmRecord {
        String agentId;
        String eventType;
        String timestamp;
        String[] values;
    }

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }

    private static String[] normalizeValues(String[] values) {
        if (values == null) return new String[0];
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) values[i] = "";
        }
        return values;
    }

    public Context getContext() {
        return mContext;
    }

    public void sendVbmMessage(String agentId, String eventType, String[] values) {
        if (gDtvCallback == null) return;

        try {
            VbmRecord record = new VbmRecord();
            record.agentId = safe(agentId);
            record.eventType = safe(eventType);
            record.timestamp = String.valueOf(System.currentTimeMillis());
            record.values = normalizeValues(values);

            String jsonPayload = VBM_GSON.toJson(record);
            TVMessage msg = TVMessage.SetVbmRecord(jsonPayload);
            gDtvCallback.onMessage(msg);

        } catch (Exception e) {
            Log.e(TAG, "Error sending VBM message", e);
        }
    }
    // =================================================================================
// VBM helper: Tuner status formatting
// Spec/Benchmark format: {freq(MHz)}/{BER(scientific)}/{SNR}dB/{Level}dBuV
// Example: 729/1.4E-4/30dB/40dBuV
// value_4 ~ value_7 carry multiple tuner status strings (same as Agent 9 Value_1~Value_4)
// =================================================================================
private static final String VBM_TUNER_STATUS_DEFAULT = "0/0.0E0/0dB/0dBuV";
private static final DecimalFormat VBM_BER_FORMAT =
        new DecimalFormat("0.0E0", DecimalFormatSymbols.getInstance(Locale.US));

private String[] getVbmTunerStatus(int ignoredCurTunerId) {
    // For Agent 4 (Error Log), spec requires Value_4~Value_7 = status of tuner0~tuner3
    String[] out = new String[4];
    for (int i = 0; i < 4; i++) {
        out[i] = buildVbmTunerStatusString(i);
    }
    return out;
                }

private String buildVbmTunerStatusString(int tunerId) {
        try {
        if (tunerId < 0) return VBM_TUNER_STATUS_DEFAULT;
        // If platform provides tuner lock state, use it; otherwise keep default.
        boolean locked;
        try {
            locked = get_tuner_status(tunerId);
        } catch (Throwable t) {
            locked = true; // best-effort: still try to read metrics
            }
        if (!locked) return VBM_TUNER_STATUS_DEFAULT;

        int strength = get_signal_strength(tunerId);      // signal level (dBuV)
        int snr = get_signal_quality(tunerId);            // SNR (dB)
        int berRaw = get_signal_ber(tunerId);             // typically scaled by 1e7 (benchmark behavior)
        int freqMhz = get_frequency(tunerId);

        double ber = berRaw / 1.0E7d;
        String berStr = VBM_BER_FORMAT.format(ber);

        return freqMhz + "/" + berStr + "/" + snr + "dB/" + strength + "dBuV";
        } catch (Exception e) {
        return VBM_TUNER_STATUS_DEFAULT;
        }
    }
    IDTVListener g_av_listener = new IDTVListener() {
        private static final String CB_TAG = "AVListener";
        // VBM
        private long curChannelId = 0;
        private long curServiceId = 0;
        private int curTunerId = 0;
        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj) {
            //Log.d(TAG, CB_TAG + " messageID = " + messageID);
            try {
                GposInfo gpos = gpos_info_get();
                if (gpos != null) {
                    curChannelId = GposInfo.getCurChannelId(mContext);
                    ProgramInfo p = get_program_by_channel_id(curChannelId);
                    if (p != null) {
                        curServiceId = p.getServiceId();
                        curTunerId = p.getTunerId();
                    }
                }
            } catch (Exception e) { Log.w(TAG, "VBM prep info failed"); }
            switch (messageID) {
                case DTVMessage.PESI_SVR_EVT_AV_STOP: {
                    Log.d(TAG, "PESI_SVR_EVT_AV_STOP");
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_AV_PLAY_SUCCESS: {
                    Log.d(TAG, "PESI_SVR_EVT_AV_PLAY_SUCCESS");
                    TVMessage msg = TVMessage.SetPlayMsg(1);
                    gDtvCallback.onMessage(msg);

                    // --- VBM Agent 3: Zapping Record ---
                    long nowWall = System.currentTimeMillis();
                    long nowReal = android.os.SystemClock.elapsedRealtime();

                    // have last channel
                    if (mLastVbmEnterTime > 0 && mLastVbmServiceId != -1) {
                        long duration = nowReal - mLastVbmEnterTime;

                        String[] values = new String[] {
                                String.valueOf(nowWall - duration), // V0: Enter Time (推算)
                                String.valueOf(nowWall),            // V1: Exit Time
                                String.valueOf(curServiceId),       // V2: Current Service ID
                                String.valueOf(mLastVbmServiceId),  // V3: Last Service ID
                                "0", "0", "Yes",                    // V4:Way, V5:Fav, V6:Auth
                                String.valueOf(duration),           // V7: Duration
                                "N/A", "N/A"  // V8, V9
                        };
                        sendVbmMessage("3", "0", values);
                    }

                    // 更新狀態
                    mLastVbmServiceId = curServiceId;
                    mLastVbmEnterTime = nowReal;
                    // ------------------------------------
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_AV_FCC_VISIBLE: {
                    Log.d(TAG, "PESI_SVR_EVT_AV_FCC_VISIBLE tunerId=" + param1);
                    TVMessage msg = TVMessage.SetFCCVisible(param1);
                    gDtvCallback.onMessage(msg);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_AV_PLAY_FAILURE: {
                    Log.d(TAG, "PESI_SVR_EVT_AV_PLAY_FAILURE Error Code = " + param1);
                    // ---VBM Agent 4: Error Log ---
                    String code;
                    String desc;
                    if (messageID == DTVMessage.PESI_SVR_EVT_AV_FRONTEND_STOP) {
                        code = "E48-32";
                        desc = "No Signal";
                    } else {
                        // 一般 Play Failure，根據 param1 決定錯誤描述
                        code = String.valueOf(param1);
                        switch (param1) {
                            case 1:
                                desc = "Lock fail";
                                break;
                            case 2:
                                desc = "Video attribute error";
                                break;
                            case 4:
                                desc = "Video PID error";
                                break;
                            case 8:
                                desc = "Video play error";
                                break;
                            case 16:
                                desc = "Audio attribute error";
                                break;
                            case 32:
                                desc = "Audio PID error";
                                break;
                            case 64:
                                desc = "Audio play error";
                                break;
                            default:
                                desc = "Play Failure (Unknown: " + param1 + ")";
                                break;
                        }
                    }
                    String[] tunerStatus = getVbmTunerStatus(curTunerId);

                    String[] errValues = new String[] {
                            "PrimeDtv",                   // V0: Owner
                            String.valueOf(curServiceId), // V1: Service ID
                            code,                         // V2: Code
                            desc,                         // V3: Desc
                            tunerStatus[0],                 // V4: Tuner0 status
                            tunerStatus[1],                 // V5: Tuner1 status
                            tunerStatus[2],                 // V6: Tuner2 status
                            tunerStatus[3],                 // V7: Tuner3 status
                            "N/A", "N/A"                      // V8~V9 (Reserved)
                    };
                    sendVbmMessage("4", "0", errValues);
                    break;
                }
                case DTVMessage.PESI_SVR_EVT_AV_CA: {
                    Log.d(TAG, "PESI_SVR_EVT_AV_CA is " + (param1 == 1 ? "CA " : "NOT CA") + " prog");
                    if (param1 == 1) {
                        int err_priority = CaPriority.ERR_E48_52.getValue();
                        String errorCode = "E48_32";
                        String desc = "No Signal";
                        TVMessage msg = TVMessage.SetCAMsg(param1, errorCode, "No Signal!");
                        gDtvCallback.onMessage(msg);
                        String[] tunerStatus = getVbmTunerStatus(curTunerId);

                        String[] errValues = new String[] {
                                "PrimeDtv",                   // V0: Owner
                                String.valueOf(curServiceId), // V1: Service ID
                                errorCode,                         // V2: Code
                                desc,                         // V3: Desc
                                tunerStatus[0],                 // V4: Tuner0 status
                                tunerStatus[1],                 // V5: Tuner1 status
                                tunerStatus[2],                 // V6: Tuner2 status
                                tunerStatus[3],                 // V7: Tuner3 status
                                "N/A", "N/A"                      // V8~V9 (Reserved)
                        };
                        sendVbmMessage("4", "0", errValues);
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
                    // --- Agent 4 ---
                    String[] tunerStatus = getVbmTunerStatus(curTunerId);

                    String[] vals = new String[] {
                            "PrimeDtv",                   // V0: Owner
                            String.valueOf(curServiceId), // V1: Service ID
                            "DEC_ERR",                         // V2: Code
                            "Decoder Error",                         // V3: Desc
                            tunerStatus[0],                 // V4: Tuner0 status
                            tunerStatus[1],                 // V5: Tuner1 status
                            tunerStatus[2],                 // V6: Tuner2 status
                            tunerStatus[3],                 // V7: Tuner3 status
                            "N/A", "N/A"                      // V8~V9 (Reserved)
                    };
                    sendVbmMessage("4", "0", vals);
                }break;
                case DTVMessage.PESI_SVR_EVT_FIRST_FRAME:{  //tifcheck
                    int tunerId = param1;
                    long channelId = PrimeDtvMediaPlayer.get_unsigned_int(param2);
                    TVMessage msg = TVMessage.SetFirstFrame(tunerId, channelId);
                    gDtvCallback.onMessage(msg);
                }break;
                case DTVMessage.PESI_SVR_EVT_VideoUnavailable:{ //tifcheck
                    TVMessage msg = TVMessage.SetVideoUnavailable(param1);
                    gDtvCallback.onMessage(msg);
                }break;
                case DTVMessage.PESI_SVR_EVT_AV_SET_SBUTITLE_BITMAP: {
                    //LogUtils.d("PESI_SVR_EVT_AV_SET_SBUTITLE_BITMAP");
                    Bitmap bitmap = (param1 == 1) ? null : (Bitmap) obj;
                    TVMessage msg = TVMessage.SetSubtitleBitmap(bitmap);
                    gDtvCallback.onMessage(msg);
                }break;
                case DTVMessage.PESI_SVR_EVT_AV_CLOSED_CAPTION_ENABLE: {
                    //LogUtils.d("PESI_SVR_EVT_AV_CLOSED_CAPTION_ENABLE");
                    TVMessage msg = TVMessage.SetClosedCaptionEnable(param1,param2);
                    gDtvCallback.onMessage(msg);
                }break;
                case DTVMessage.PESI_SVR_EVT_AV_CLOSED_CAPTION_DATA: {
                    //LogUtils.d("PESI_SVR_EVT_AV_SET_SBUTITLE_BITMAP");
                    TVMessage msg = TVMessage.SetClosedCaptionData((byte[]) obj);
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
                    final long channel_id = PrimeDtvMediaPlayer.get_unsigned_int(param1);
                    TVMessage msg = TVMessage.SetEPGUpdate(channel_id);
                    gDtvCallback.onMessage(msg);
                    msg = TVMessage.SetEpgPFVersionChanged(channel_id);
                    gDtvCallback.onMessage(msg);
                    if(Pvcfg.isPrimeTvInputEnable()) {
                        Log.d(TAG, "primetif onTIFEpgUpdate pf"); //tifcheck
                        gTIFEpgUpdateManager.onTIFEpgUpdate(channel_id, true);
                    }
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
                    final long channel_id = PrimeDtvMediaPlayer.get_unsigned_int(param1);
                    TVMessage msg = TVMessage.SetEPGUpdate(channel_id);
                    if(Pvcfg.isPrimeTvInputEnable()) { //tifcheck
                        Log.d(TAG, "primetif onTIFEpgUpdate sch channel_id:"+channel_id);
                        gTIFEpgUpdateManager.onTIFEpgUpdate(channel_id, false);
                    }
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
                    Log.d(TAG, "scan  freq finished" + param1 + " " + parm2 +
                            " gTvChCount = "+gTvChCount+" gRadioChCount = "+gRadioChCount);
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
        private long curChannelId = 0;
        private long curServiceId = 0;
        private int curTunerId = 0;
        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj) {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            try {
                GposInfo gpos = gpos_info_get();
                if (gpos != null) {
                    curChannelId = GposInfo.getCurChannelId(mContext);
                    ProgramInfo p = get_program_by_channel_id(curChannelId);
                    if (p != null) {
                        curServiceId = p.getServiceId();
                        curTunerId = p.getTunerId();
                    }
                }
            } catch (Exception e) { Log.w(TAG, "VBM prep info failed"); }
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
                    // [VBM Agent 7] 錄影失敗/不完整 (Result = 1)
                    notifyVbmPvrResult(1, "0", System.currentTimeMillis(), 0); 
                    TVMessage msg = TVMessage.SetPvrRecordStart(1,param1,param2,null);
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
                    
                    // [VBM Agent 7] 錄影完成 (Result = 0)
                    notifyVbmPvrResult(0, "0", System.currentTimeMillis(), 0); 
                    
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
                    TVMessage msg = TVMessage.SetPvrRecordStart(0,param1,param2,(String)obj);
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
                    sendVbmMessage("6","0",(String[])obj);
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
                    sendVbmMessage("6","0",(String[])obj);
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
                    sendVbmMessage("6","0",(String[])obj);
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
                    sendVbmMessage("6","0",(String[])obj);
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
                    sendVbmMessage("6","0",(String[])obj);
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
                    sendVbmMessage("6","0",(String[])obj);
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
                    sendVbmMessage("6","1",(String[])obj);
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
                    sendVbmMessage("6","1",(String[])obj);
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
                    sendVbmMessage("6","1",(String[])obj);
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
                    sendVbmMessage("6","1",(String[])obj);
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
                    sendVbmMessage("6","1",(String[])obj);
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
                    sendVbmMessage("6","1",(String[])obj);
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
    private void dsmccStartTicker(String tickerPid) {
        if (TextUtils.isEmpty(tickerPid)) return;
        Intent i = new Intent();
        i.setClassName("com.prime.dtvservice", DsmccService.CLASS_NAME);
        i.setAction(DsmccService.ACTION_DSMCC_START);
        i.putExtra(DsmccService.KEY_CNS_TICKER_PID, tickerPid);
        mContext.startService(i);
    }
    
    private void dsmccStartAd(String adPid) {
        if (TextUtils.isEmpty(adPid)) return;
        Intent i = new Intent();
        i.setClassName("com.prime.dtvservice", DsmccService.CLASS_NAME);
        i.setAction(DsmccService.ACTION_DSMCC_START);
        i.putExtra(DsmccService.KEY_CNS_AD_PID, adPid);
        mContext.startService(i);
    }
    private void dsmccStopTicker() {
        Intent i = new Intent();
        i.setClassName("com.prime.dtvservice", DsmccService.CLASS_NAME);
        i.setAction(DsmccService.ACTION_DSMCC_STOP);
        i.putExtra(DsmccService.KEY_CNS_TICKER_PID, ""); // 空表示停
        mContext.startService(i);
    }
    
    private void dsmccStopAd() {
        Intent i = new Intent();
        i.setClassName("com.prime.dtvservice", DsmccService.CLASS_NAME);
        i.setAction(DsmccService.ACTION_DSMCC_STOP);
        i.putExtra(DsmccService.KEY_CNS_AD_PID, "");
        mContext.startService(i);
    }

    IDTVListener g_system_listener = new IDTVListener() {
        private static final String CB_TAG = "SystemListener";
        private long curChannelId = 0;
        private long curServiceId = 0;
        private int curTunerId = 0;
        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj) {
            Log.d(TAG, CB_TAG + " messageID = " + messageID);
            TVMessage msg;
            try {
                GposInfo gpos = gpos_info_get();
                if (gpos != null) {
                    curChannelId = GposInfo.getCurChannelId(mContext);
                    ProgramInfo p = get_program_by_channel_id(curChannelId);
                    if (p != null) {
                        curServiceId = p.getServiceId();
                        curTunerId = p.getTunerId();
                    }
                }
            } catch (Exception e) { Log.w(TAG, "VBM prep info failed"); }
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
                    Log.d(TAG, "notifyMessage: PESI_EVT_SYSTEM_PMT_UPDATE_AV  "+programInfo.getChannelId()+" "+GposInfo.getCurChannelId(mContext));
                    if((programInfo!=null) && (programInfo.getChannelId() == GposInfo.getCurChannelId(mContext)))
                        current_channel = true;
                    else
                        current_channel = false;
                    if(current_channel) {
                        if (AvCmdMiddle.is_e213(programInfo.getChannelId())) {
                            LogUtils.d("[e213] call PESI_SVR_EVT_AV_FRONTEND_STOP");
                            getPesiDtvFrameworkInterfaceCallback().sendCallbackMessage(DTVMessage.PESI_SVR_EVT_AV_FRONTEND_STOP, (int) programInfo.getChannelId(), 0, null);
                        }
                        else {
                                av_control_play_by_channel_id(programInfo.getTunerId(), programInfo.getChannelId(), programInfo.getType(), 1);
                        }
                        msg = TVMessage.SetPmtAvChange();//tifcheck
                        gDtvCallback.onMessage(msg);
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
                        av_control_play_by_channel_id(programInfo.getTunerId(), programInfo.getChannelId(), programInfo.getType(), 1);
                    }
                }break;
                case DTVMessage.PESI_EVT_SYSTEM_PMT_UPDATE_SUBTITLE: {
                    msg = TVMessage.SetPmtDvbSubtileChange();//tifcheck
                    gDtvCallback.onMessage(msg);
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

                    GposInfo.setWVCasLicenseURL(mContext, url);
                    GposInfo.setSTBRefCasDataTime(mContext, stbUpdateTime);
                    //gpos_info_update_by_key_string(GposInfo.GPOS_WVCAS_LICENSE_URL, url);
                    //remove_wvcas_license("");
                    //handleCasRefresh(1,0, 0);
                    //gpos_info_update_by_key_string(GposInfo.GPOS_WVCAS_REF_CASDATA_TIME, stbUpdateTime);
                    //saveGposKeyValue(GposInfo.GPOS_WVCAS_LICENSE_URL, url);
                    //saveGposKeyValue(GposInfo.GPOS_WVCAS_REF_CASDATA_TIME, stbUpdateTime);
//                    if(programInfo != null){
//                        av_control_play_stop(0,0,0);
//                        av_control_play_by_channel_id(programInfo.getTunerId(), programInfo.getChannelId(), programInfo.getType(), 1);
//                    }
                }break;
                case DTVMessage.PESI_EVT_SYSTEM_SI_UPDATE_TBC_BAT_WHITE_LIST_FAIL:{
                    if(!Pvcfg.isCheckIllegalNetwork())
                        break;
                    TVMessage tvMessage = TVMessage.SetShowErrorMsg(ErrorCode.ERROR_E301,"", Pvcfg.CA_TYPE.CA_WIDEVINE_CAS);
                    gDtvCallback.onMessage(tvMessage);
                    String tunerStatus[] = getVbmTunerStatus(curTunerId);
                    String desc = "E301";
                    String[] errValues = new String[] {
                            "PrimeDtv",                   // V0: Owner
                            String.valueOf(curServiceId), // V1: Service ID
                            String.valueOf(ErrorCode.ERROR_E301),         // V2: Code
                            desc,                         // V3: Desc
                            tunerStatus[0],                 // V4: Tuner0 status
                            tunerStatus[1],                 // V5: Tuner1 status
                            tunerStatus[2],                 // V6: Tuner2 status
                            tunerStatus[3],                 // V7: Tuner3 status
                            "N/A", "N/A"                      // V8~V9 (Reserved)
                    };

                    sendVbmMessage("4", "0", errValues);

                }break;
                case DTVMessage.PESI_EVT_SYSTEM_SET_NIT_ID_TO_ACS: {
                    TVMessage tvMessage = TVMessage.setNitIdToACS(param1);
                    gDtvCallback.onMessage(tvMessage);
                }break;
                case DTVMessage.PESI_EVT_SYSTEM_CNS_GET_TICKER_DATA: {
                    dsmccStartTicker(String.valueOf(param1));
                    
                }break;
                case DTVMessage.PESI_EVT_SYSTEM_CNS_GET_AD_DATA: {
                    dsmccStartAd(String.valueOf(param1));
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
            long curServiceId = 0;
            int curTunerId = 0;
            try {
                GposInfo gpos = gpos_info_get();
                if (gpos != null) {
                    ProgramInfo p = get_program_by_channel_id(GposInfo.getCurChannelId(mContext));
                    if (p != null) {
                        curServiceId = p.getServiceId();
                        curTunerId = p.getTunerId();
                    }
                }
            } catch(Exception e){Log.w(TAG, "VBM prep ca info failed");}

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
                    //sendCasError(param1, (String) obj);
                    LogUtils.d("[CheckErrorMsg] param1 = "+param1);
                    msg = TVMessage.SetVideoUnavailable(param1);
                    gDtvCallback.onMessage(msg);

                }break;
                case DTVMessage.PESI_EVT_CA_WIDEVINE_REMOVE_LICENSE:{
                    long channelId = (long)param1;
                    if(obj == null)
                        break;
                    String licenseId = (String)obj;
                    remove_wvcas_license(licenseId);
                    ProgramInfo p = get_program_by_channel_id(channelId);
                    LogUtils.d("PESI_EVT_CA_WIDEVINE_REMOVE_LICENSE channelId = "+channelId+" "+GposInfo.getCurChannelId(mContext));
                    LogUtils.d("PESI_EVT_CA_WIDEVINE_REMOVE_LICENSE p = "+p);
                    if(channelId == GposInfo.getCurChannelId(mContext) && p != null){
                        av_control_play_stop(p.getTunerId(),0,1);
                        av_control_play_by_channel_id(p.getTunerId(), p.getChannelId(), p.getType(), 1);
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
                case DTVMessage.PESI_EVT_CA_WIDEVINE_IRD_COMMAND:{
                    if(obj == null)
                        break;
                    IrdCommand irdCommand = (IrdCommand) obj;
                    irdCommand.doIrdCommand(gPrimeDtv);
                }break;
                case DTVMessage.PESI_EVT_CA_WIDEVINE_CA_MAIL:
                    //eric lin 20260106 CNS mail
                    Log.d(TAG, "收到 MPS CA Mail 事件！"); // Log 方便除錯

                    if (obj != null && obj instanceof byte[]) {
                        byte[] rawData = (byte[]) obj;

                        // 1. 解析 (使用您新建的 CNSMailParser)
                        CNSMailData mail = CNSMailParser.parse(rawData);

                        if (mail != null) {
                            // 補上接收時間 (因為原始資料字串裡通常沒有時間，這裡補上當下時間)
                            mail.setReceiveTime(System.currentTimeMillis());

                            // 2. 存檔 (使用您新建的 CNSMailDatabaseTable)
                            // 直接 new 出來操作，它內部會處理 ContentResolver 的細節
                            CNSMailDatabaseTable dbTable = new CNSMailDatabaseTable(mContext);

                            try {
                                dbTable.add(mail); // ★ 這一行取代了原本一大串 ContentValues 的程式碼
                                Log.d(TAG, "MPS CA Mail saved successfully: " + mail.getTitle());
                            } catch (Exception e) {
                                Log.e(TAG, "Save MPS CA Mail failed", e);
                            }
                        } else {
                            Log.e(TAG, "MPS CA Mail parse failed (return null)");
                        }
                    } else {
                        Log.e(TAG, "MPS CA Mail data is null or invalid type");
                    }
                    break;
                case DTVMessage.PESI_EVT_CA_WIDEVINE_EMERGENCY_ALARM:{//eric lin 20260112 CNS EAS message 
                    Log.d(TAG, "收到 MPS EAS 事件！"); // Log 方便除錯

                    if (obj != null && obj instanceof byte[]) {
                        byte[] rawData = (byte[]) obj;

                        // 1. 解析 (使用您新建的 CNSMailParser)
                        CNSEasData eas = CNSEasParser.parse(rawData);

                        if (eas != null) {
                            // 補上接收時間 (因為原始資料字串裡通常沒有時間，這裡補上當下時間)
                            //mail.setReceiveTime(System.currentTimeMillis());

                            // 2. 存檔 (使用您新建的 CNSEasDatabaseTable)
                            // 直接 new 出來操作，它內部會處理 ContentResolver 的細節
                            CNSEasDatabaseTable dbTable = new CNSEasDatabaseTable(mContext);

                            try {
                                dbTable.add(eas); // ★ 這一行取代了原本一大串 ContentValues 的程式碼
                                Log.d(TAG, "MPS CA EAS saved successfully: " + eas.getAlertTitle());
                            } catch (Exception e) {
                                Log.e(TAG, "Save MPS CA EAS failed", e);
                            }

                        } else {
                            Log.e(TAG, "MPS CA EAS parse failed (return null)");
                        }
                    } else {
                        Log.e(TAG, "MPS CA EAS data is null or invalid type");
                    }
                }break;
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

    IDTVListener g_ota_listener = new IDTVListener() {
        private static final String OTA_TAG = "OTAListener";

        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj) {
            Log.d(TAG, OTA_TAG + " messageID = " + messageID + " param1 = " + param1 + " param2 = " + param2);
            TVMessage msg = null;
            String lastVersion;
            switch (messageID) {
                case DTVMessage.PESI_EVT_OTA_SHOW_UPDATE_COUNT_DOWN_DIALOG:
                    Log.d(TAG, "notifyMessage: PESI_EVT_OTA_SHOW_UPDATE_COUNT_DOWN_DIALOG");

                    lastVersion = (String) obj;
                    msg = TVMessage.ShowOTACountDownDialog(lastVersion);
                    gDtvCallback.onMessage(msg);
                    break;
                case DTVMessage.PESI_EVT_OTA_SHOW_UPDATE_DIALOG:
                    Log.d(TAG, "notifyMessage: PESI_EVT_OTA_SHOW_UPDATE_DIALOG");
                    lastVersion = (String) obj;
                    msg = TVMessage.ShowOTAUpdateDialog(lastVersion);
                    gDtvCallback.onMessage(msg);
                    break;
                default:
                    break;
            }
        }
    };
    public void set_system_language(String language) {
        PrimeSystemApp.SetSystemLanguage(mContext, language);
    }

    public void set_hdmi_cec(int cec){
        HdmiCecUtil cecUtil = new HdmiCecUtil(mContext);
        cecUtil.setHdmiCecEnabled((cec==1)?true:false);
    }

    public void set_dcp_level(int hdcp){
        //PrimeSystemApp.set_hdcp_level(hdcp);
        try {
            mPrimeMiscService.invoke_cmd(MiscService.MISC_CMD_SET_HDCPLEVEL, hdcp, 0);
        } catch (RemoteException e) {
            LogUtils.e("mPrimeMiscService not ready !!!!!");
        }
    }

    public void set_hdmi_output_format(int format){
        try {
            mPrimeMiscService.invoke_cmd(MiscService.MISC_CMD_SET_HDMI_RESOLUTION, format, 0);
        } catch (RemoteException e) {
            LogUtils.e("mPrimeMiscService not ready !!!!!");
        }
    }

    public int get_hdmi_output_format_count(){
        try {
            return mPrimeMiscService.invoke_cmd(MiscService.MISC_CMD_GET_HDMI_SUPPORT_FORMAT, 0, 0);
        } catch (RemoteException e) {
            LogUtils.e("mPrimeMiscService not ready !!!!!");
        }
        return 6;
    }

    public void av_control_set_aspect_ratio(int aspectRatio) {
        LogUtils.d("aspectRatio = "+aspectRatio);
        try {
            mPrimeMiscService.invoke_cmd(MiscService.MISC_CMD_SET_ASPECT_RATIO, aspectRatio, 0);
        } catch (RemoteException e) {
            LogUtils.e("mPrimeMiscService not ready !!!!!");
        }

    }

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

    public static PrimeDtv getInstance(PrimeDtvInterface.DTVCallback callback, Context context) {
        if(gPrimeDtv == null)
            gPrimeDtv = new PrimeDtv(callback,context);
        return gPrimeDtv;
    }

    public static PrimeDtv getInstance() {
        return gPrimeDtv;
    }

    public PrimeDtv(PrimeDtvInterface.DTVCallback callback, Context context) {
        Log.d(TAG,"context = "+context);
        mContext = context;
        registerBroadcastReceiver(context);
        //gPrimeDtvMediaPlayer = PrimeDtvMediaPlayer.get_instance(context);
        gDtvFramework = PesiDtvFramework.getInstance(context);
        LogUtils.d(" ");
        gPrimeDtvMediaPlayer = PrimeDtvMediaPlayer.get_instance(context);
        LogUtils.d(" ");
        gDataFromLauncher = DataFromLauncher.getInstance();
        gDtvCallback = callback;
        try {
            mPrimeMiscService = IMisc.getService(true);
            mPrimeMediaService = IMedia.getService(true);
            Log.d(TAG," Get mPrimeMiscService => [" + mPrimeMiscService + "]");
            Log.d(TAG," Get mPrimeMediaService => [" + mPrimeMediaService + "]");
        } catch (RemoteException | NoSuchElementException e) {
            Log.e(TAG, "Exception : getService fail", e);
        }
        if(Pvcfg.isPrimeTvInputEnable())
            gTIFEpgUpdateManager = new TIFEpgUpdateManager(mContext,this);
        //CasData casData = CasRefreshHelper.get_instance().get_cas_data();
        //LogUtils.d("casData "+casData.toString());
        update_cas_config();
        set_cns_ota();
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
        PesiDtvFramework_registCallback ( DTVMessage.PESI_EVT_OTA_START, DTVMessage.PESI_EVT_OTA_END, g_ota_listener ) ;

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
        PesiDtvFramework_unregistCallback ( DTVMessage.PESI_EVT_OTA_START, DTVMessage.PESI_EVT_OTA_END, g_ota_listener ) ;

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
        LogUtils.d("FCC_LOG PrimeDtv set_surface_view: index=" + index +
                " view=" + surfaceView +
                " surface=" + (surfaceView == null ? "null" : surfaceView.getHolder().getSurface()) +
                " surfaceId=" + (surfaceView == null ? 0 : System.identityHashCode(surfaceView.getHolder().getSurface())));
        gDtvFramework.setSurfaceView(context,surfaceView, index);
        //setSurfaceToPlayer(index, surfaceView.getHolder());
    }
    public void set_surface_view(Context context, SurfaceView surfaceView)//gary20200429 add set surface function for send broadcast to set surface in PesiSystem apk
    {
//        gPrimeDtvMediaPlayer.set_surface_view(context, surfaceView);
        LogUtils.d("FCC_LOG PrimeDtv set_surface_view: view=" + surfaceView);
        gDtvFramework.setSurfaceView(context, surfaceView);
    }

    public void set_surface(Context context, Surface surface,int index) {
        LogUtils.d("FCC_LOG PrimeDtv set_surface: index=" + index +
                " surface=" + surface +
                " surfaceId=" + (surface == null ? 0 : System.identityHashCode(surface)) +
                " pid=" + android.os.Process.myPid() +
                " uid=" + android.os.Process.myUid());
        gDtvFramework.setSurface(surface,index);
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

    public void stopAllEitTableOnCurrentTp(int tuner_id){
        LogUtils.d("stopAllEitTableOnCurrentTp tuner_id = "+tuner_id);
        gDtvFramework.stopAllEitTableOnCurrentTp(tuner_id);
    }

    public void UpdatePMT(long channel_id){

        gDtvFramework.UpdatePMT(channel_id);
    }

    public int  StopDVBSubtitle(){
        return gDtvFramework.StopDVBSubtitle();
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

    public int set_tv_input_id(String id) {
        return gDtvFramework.SetTvInputId(id);
    }

    public String av_control_get_tv_input_id() {
        return gDtvFramework.GetTvInputId();
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

    public int av_control_reset_audio_default_language(String lang) {
//        return gPrimeDtvMediaPlayer.av_control_set_command(playId, keyCode);
        return gDtvFramework.AvControlResetAudioDefaultLanguage(lang);
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
        // [VBM Log] 根據是否為 EPG 預約 (-1 為時間預約) 發送對應事件
        // Action 0: Make reservation
        if (bookInfo.getEpgEventId() == -1) {
            notifyVbmBookRecordByTime(bookInfo);
        } else {
            notifyVbmBookReservation(bookInfo, 0);
        }
//        return gPrimeDtvMediaPlayer.book_info_add(bookInfo);
        return gDtvFramework.BookInfoAdd(bookInfo);
    }

    public int book_info_update(BookInfo bookInfo) {
        // [VBM Log] 更新視為 Make reservation (Action 0)
        if (bookInfo.getEpgEventId() == -1) {
            notifyVbmBookRecordByTime(bookInfo);
        } else {
            notifyVbmBookReservation(bookInfo, 0);
        }
//        return gPrimeDtvMediaPlayer.book_info_update(bookInfo);
        return gDtvFramework.BookInfoUpdate(bookInfo);
    }

    public int book_info_update_list(List<BookInfo> bookList) {
//        return gPrimeDtvMediaPlayer.book_info_update_list(bookList);
        return gDtvFramework.BookInfoUpdateList(bookList);
    }

    public int book_info_delete(int bookId) {
        // [VBM Log] 刪除單一預約 -> Agent 7, Event 2, Action 1
        // 需先取得資訊以獲得 Event Name
        BookInfo info = book_info_get(bookId);
        if (info != null) {
            String eventName = info.getEventName();
            if (eventName == null || eventName.isEmpty()) {
                eventName = "Unknown";
            }
            notifyVbmBookRecordingListOp(1, eventName);
        }
//        return gPrimeDtvMediaPlayer.book_info_delete(bookId);
        return gDtvFramework.BookInfoDelete(bookId);
    }

    public int book_info_delete_all() {
        // [VBM Log] 刪除全部 -> Agent 7, Event 2, Action 3
        notifyVbmBookRecordingListOp(3, "All");
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

    public void set_alarm(int requestCode, Intent intent, long triggerTimeMs) {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String startCalFormatted = sdf.format(new Date(triggerTimeMs));
        Log.d(TAG, "set alarm for requestCode " + requestCode + " at " + startCalFormatted);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mContext.getApplicationContext(),
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent);
    }

    public void cancel_alarm(int requestCode, Intent  intent) {
        Log.d(TAG, "cancel_alarm: cancel alarm for requestCode " + requestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mContext.getApplicationContext(),
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);

        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
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
            Intent intent = bookInfo.getIntent();
            Calendar calendar = Calendar.getInstance(); // now
            if (bookInfo.getBookCycle() == BookInfo.BOOK_CYCLE_SERIES_EMPTY) {
                // cycle = BOOK_CYCLE_SERIES_EMPTY
                // set next alarm to start later to check again
                Log.d(TAG, "schedule_next_timer: BOOK_CYCLE_SERIES_EMPTY! adjust alarm time...");
                calendar.add(Calendar.MINUTE, Pvcfg.SERIES_CHECK_MINUTE);
            } else if (bookInfo.getBookCycle() == BookInfo.BOOK_CYCLE_ONETIME_EMPTY) {
                Log.d(TAG, "schedule_next_timer: BOOK_CYCLE_ONETIME_EMPTY! adjust alarm time...");
                // for finding next same program to record
                calendar.add(Calendar.MINUTE, Pvcfg.WAITING_SCHEDULE_CHECK_MINUTE);
            } else {
                calendar.set(bookInfo.getYear(),
                        bookInfo.getMonth() - 1, // bookinfo month = 1 ~ 12, -1 for Calendar
                        bookInfo.getDate(),
                        bookInfo.getStartTime() / 100,
                        bookInfo.getStartTime() % 100,
                        0);

                // adjust record/remind alarm time
                // e.g. ADVANCE_REMINDER_TIME = -1 if we need the alarm to be triggered 1 min earlier
                if (bookInfo.getBookType() == BookInfo.BOOK_TYPE_RECORD
                        || bookInfo.getBookType() == BookInfo.BOOK_TYPE_CHANGE_CHANNEL) {
                    calendar.add(Calendar.MINUTE, BookInfo.ADVANCE_REMINDER_TIME);
                }
            }

            set_alarm(bookInfoId, intent, calendar.getTimeInMillis());
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
        // use another thread because schedule_next_timer() may cost time
        new Thread(() -> {
            List<BookInfo> bookInfoList = book_info_get_list();
            for (BookInfo bookInfo : bookInfoList) {
                if (bookInfo.getBookType() == BookInfo.BOOK_TYPE_RECORD_NOW) {
                    // BOOK_TYPE_RECORD_NOW book found, it may be power loss or reboot
                    // set it back to BOOK_TYPE_RECORD
                    bookInfo.setBookType(BookInfo.BOOK_TYPE_RECORD);
                }

                schedule_next_timer(bookInfo);
            }

            save_table(EnTableType.TIMER);
        }, "schedule_all_timers").start();
    }

    public void re_schedule_next_timer(Context context, BookInfo bookInfo) {
        if (bookInfo == null) {
            Log.e(TAG, "re_schedule_next_timer: bookInfo == null");
            return;
        }

        // cancel previous alarm first
        cancel_alarm(bookInfo.getBookId(), bookInfo.getIntent());
        schedule_next_timer(bookInfo);
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
    public int get_frequency(int nTunerID) {
//        return gPrimeDtvMediaPlayer.get_signal_ber(nTunerID);
        return gDtvFramework.getFrequency(nTunerID);
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
    public ProgramInfo get_program_by_sid_onid_tsid(int sid, int onid, int tsid) {
//        return gPrimeDtvMediaPlayer.get_program_by_channel_id(channelId);
        return gDtvFramework.getProgramBySIdOnIDTsId(sid,onid,tsid);
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

    public Date get_network_time() {
        try {
            URL url = new URL("https://www.google.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.connect();

            long date = connection.getDate();
            return new Date(date);
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        return null;
    }

    public void build_network_time() {
        if (gTaskUpdateNetworkTime != null)
            return;
        gTaskUpdateNetworkTime = new TimerTask() {
            @Override
            public void run() {
                gNetworkTime = get_network_time();
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                allChannels.sort(Comparator.comparingInt(ProgramInfo::getDisplayNum));
                }

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
                            + ", [time scope] " + Utils.ms_to_time(dateStart.getTime(), "MM/dd HH:mm") + " - " + Utils.ms_to_time(dateEnd.getTime(), "MM/dd HH:mm"));

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            allChannels.sort(Comparator.comparingInt(ProgramInfo::getDisplayNum));
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            allChannels.forEach(programInfo -> {
                long channelID = programInfo.getChannelId();
                List<EPGEvent> scheduleList = gEpgEventMap.get(channelID);
                if (null == scheduleList)
                    scheduleList = new ArrayList<>();
                Log.d(TAG, "save_schedule_map: channel num = " + programInfo.getDisplayNum(3) + ", schedule size = " + scheduleList.size());
            });
        }
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                allChannels.sort(Comparator.comparingInt(ProgramInfo::getDisplayNum));
                }

                for (Map.Entry<Object, List<EPGEvent>> entry : gEpgEventMap.entrySet()) {
                    Object key = entry.getKey();
                    List<EPGEvent> eventList = entry.getValue();
                    final ProgramInfo[] channel = {null};
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    allChannels.forEach(programInfo -> {
                        if (programInfo.getChannelId() == (long) key)
                            channel[0] = programInfo;
                    });
                    }
                    Log.d(TAG, "load_schedule_map: key = " + key + ", channel = " + (channel[0] == null ? "000" : channel[0].getDisplayNum(3)) + ", eventList size = " + eventList.size() + ", first event = " + (eventList.isEmpty() ? "null" : eventList.get(0).get_event_name()));
                }
            }

            if (false) {
                List<ProgramInfo> allChannels = new ArrayList<>();
                allChannels.addAll(get_program_info_list(FavGroup.ALL_TV_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.NUM_ALL));
                allChannels.addAll(get_program_info_list(FavGroup.ALL_RADIO_TYPE, MiscDefine.ProgramInfo.POS_ALL, MiscDefine.ProgramInfo.NUM_ALL));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                allChannels.sort(Comparator.comparingInt(ProgramInfo::getDisplayNum));
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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
                            + ", [start ~ end] " + Utils.ms_to_time(newEvent.get_start_time(), "MM/dd HH:mm")
                            + " - " + Utils.ms_to_time(newEvent.get_end_time(), "HH:mm"));
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
                        ", [start ~ end] " + Utils.ms_to_time(gPresent.get_start_time(), "MM/dd HH:mm") + " - " + Utils.ms_to_time(gPresent.get_end_time(), "HH:mm") +
                        ", [current time] " + Utils.ms_to_time(currentTime, "MM/dd HH:mm")));
                return gPresent;
            }
            gPresent = gPrimeDtvMediaPlayer.get_present_event(channelID);
            if (gPresent != null/* || is_correct_present(gPresent)*/) {
                Log.d(TAG, "get_present_event: (from Dtv Service) " +
                        "[present event] " + (gPresent == null ? "NULL" : gPresent.get_event_name() +
                        ", [start ~ end] " + Utils.ms_to_time(gPresent.get_start_time(), "MM/dd HH:mm") + " - " + Utils.ms_to_time(gPresent.get_end_time(), "HH:mm") +
                        ", [current time] " + Utils.ms_to_time(currentTime, "MM/dd HH:mm")));
                ProgramInfo programInfo = get_program_by_channel_id(channelID);
                if (programInfo != null && programInfo.getAdultFlag() == 1)
                    gPresent.set_parental_rate(18);
//                Log.d(TAG, "get_present_event: (from Dtv Service) programInfo tif channel id = " + TIFChannelData.getChannelIdFromUri(mContext,programInfo.getTvInputChannelUri()) +
//                        " event id = "+ gPresent.get_event_id());
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
                        ", [start ~ end] " + Utils.ms_to_time(gFollow.get_start_time(), "MM/dd HH:mm") + " - " + Utils.ms_to_time(gFollow.get_end_time(), "HH:mm") +
                        ", [current time] " + Utils.ms_to_time(currentTime, "MM/dd HH:mm")));
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
                        ", [start ~ end] " + Utils.ms_to_time(gFollow.get_start_time(), "MM/dd HH:mm") + " - " + Utils.ms_to_time(gFollow.get_end_time(), "HH:mm") +
                        ", [current time] " + Utils.ms_to_time(currentTime, "MM/dd HH:mm")));
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
        String presentEnd = Utils.ms_to_time(gPresentEndTime, "HH:mm");
        String followStart = Utils.ms_to_time(followStartTime, "HH:mm");
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

    public void start_schedule_eit(int tp_id,int tuner_id) {
        // use tuner framework to get epg raw data
        LogUtils.d("[Ethan] start_schedule_eit");
        gDtvFramework.startScheduleEit(tp_id, tuner_id);
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

    public void gpos_info_update_by_key_string(String key, long value) {
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
        int ret = gDtvFramework.PvrInit(usbMountPath);
        Pvcfg.setPvrInit(true);
        return ret;
    }
    public int pvr_deinit(){
        int ret = gDtvFramework.PvrDeinit();
        Pvcfg.setPvrInit(false);
        return ret;
    }
    public int pvr_RecordStart(PvrRecStartParam startParam,int tunerId)
    {
        LogUtils.d("channel ID = "+startParam.getmProgramInfo().getChannelId()+", tunerId = "+tunerId);
        return gDtvFramework.PvrRecordStart(startParam, tunerId);
    }
    public int pvr_RecordStop(int recId){
        LogUtils.d("recId = "+recId);
        
        // PVR VBM Log: AgentId=7, EventType=4 (Operation Result)
        // 1. 取得 VBM 相關資訊
        String serviceId = "0"; 
        long startTime = pvr_RecordGetRecStartTimeByRecId(recId); // 暫時使用 current time
        int duration = pvr_RecordGetRecTimeByRecId(recId); // 取得已錄製時長 (秒)

        // 2. 發送 VBM Log: 0 = Record completely (手動停止視為完成)
        // 註: 理想上 startTime 應從 recId 查詢
        notifyVbmPvrResult(0, serviceId, startTime, duration); 
        
        return gDtvFramework.PvrRecordStop(recId);
    }
    public void pvr_RecordStopAll(){
        gDtvFramework.PvrRecordStopAll();
    }
    public int pvr_RecordGetRecTimeByRecId(int recId){
        LogUtils.d("recId = "+recId);
        return gDtvFramework.PvrRecordGetRecTimeByRecId(recId);
    }
    public long pvr_RecordGetRecStartTimeByRecId(int recId){
        LogUtils.d("recId = "+recId);
        return gDtvFramework.PvrRecordGetRecStartTimeByRecId(recId);
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
    public int pvr_GetPlaybackRecCount() {
        return gDtvFramework.PvrGetPlaybackRecCount();
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
        
        // PVR VBM Log: AgentId=7, EventType=3 (Recorded List)
        // Action=3 (Delete all), Info="All", Episode=0
        notifyVbmPvrListOp(3, "All", 0);
        
        return gDtvFramework.PvrDelAllRecs();
    }
    public int pvr_DelSeriesRecs(int masterIndex) {
        LogUtils.d("masterIndex = "+masterIndex);
        
        // PVR VBM Log: AgentId=7, EventType=3 (Recorded List)
        // Action=3 (視為批量刪除), Info="Series Delete", Episode=0
        notifyVbmPvrListOp(3, "Series Delete", 0);
        
        return gDtvFramework.PvrDelSeriesRecs(masterIndex);
    }
    public int pvr_DelRecsByChId(int channelId) {
        LogUtils.d("channelId = "+channelId);
        return gDtvFramework.PvrDelRecsByChId(channelId);
    }
    public int pvr_DelOneRec(PvrRecIdx tableKeyInfo){
        LogUtils.d("MasterID = "+tableKeyInfo.getMasterIdx()+", SeriesID = "+tableKeyInfo.getSeriesIdx() );
        
        // PVR VBM Log: AgentId=7, EventType=3 (Recorded List)
        // 1. 取得檔案資訊以獲得 Event Name
        PvrRecFileInfo info = pvr_GetFileInfoByIndex(tableKeyInfo);
        String eventName = (info != null) ? info.getEventName() : "Unknown";
        // 2. 判斷是否為系列節目
        int episode = (tableKeyInfo.getSeriesIdx() >= 0) ? tableKeyInfo.getSeriesIdx() : 0;

        // 3. 發送 VBM Log
        // Action=1 (Delete single), Info=EventName, Episode=index
        notifyVbmPvrListOp(1, eventName, episode);
        
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
    public boolean pvr_CheckSeriesComplete(byte[]  seriesKey){
        return gDtvFramework.PvrCheckSeriesComplete(seriesKey);
    }
    public boolean pvr_IsIdxInUse(PvrRecIdx pvrRecIdx) {
        return gDtvFramework.PvrIsIdxInUse(pvrRecIdx);
    }

    public int pvr_ModifyPlayStopPos(PvrRecIdx pvrRecIdx, int position) {
        return gDtvFramework.PvrModifyPlayStopPos(pvrRecIdx, position);
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

    public int set_module_type(String type){
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
//            if(ViewHistory.getCurChannel() != null)
//            {
//                UpdateEpgPF();
//            }
        }

        public void UpdateEpgPF() {
//            if(ViewHistory.getCurChannel() != null) {
//                Log.d(TAG, "UpdateEpgPF:  ==>> " + ViewHistory.getCurChannel().get_channel_id());
//
//                EpgPreData = get_present_event(ViewHistory.getCurChannel().get_channel_id());
//                EpgFolData = get_follow_event(ViewHistory.getCurChannel().get_channel_id());
//            }
        }

        public boolean ChangeChannelByDigi(int displayNum) {
//            for(int i = 0; i < ViewHistory.getCurChannelList().size(); i++) {
//                if(ViewHistory.getCurChannelList().get(i).get_channel_num() == displayNum) {
//                    if(ViewHistory.getCurChannelList().get(i).get_channel_id() != ViewHistory.getCurChannel().get_channel_id())//Scoty 20180801 fixed after unlock the channel and then digit set to the same channel no need show password dialog
//                        av_control_play_by_channel_id(ViewHistory.getPlayId(),ViewHistory.getCurChannelList().get(i).get_channel_id(),ViewHistory.getCurGroupType(),1);
//                    return true;
//                }
//            }
            return false;
        }

        public void ChangeProgram()//change channel
        {
//            if(ViewHistory.getCurChannel() != null)
//            {
//                av_control_play_by_channel_id(ViewHistory.getPlayId(), ViewHistory.getCurChannel().get_channel_id(), ViewHistory.getCurGroupType(),1);
//
//                // use MtestTestAvMultiPlay to change program, AvControlPlayByChannelID has problem after split play
////                List<Integer> tunerIDs = new ArrayList<>();
////                List<Long> channelIDs = new ArrayList<>();
////
////                tunerIDs.add(0);
////                channelIDs.add(ViewHistory.getCurChannel().getChannelId());
////                MtestTestAvMultiPlay(1, tunerIDs, channelIDs);
//            }
        }

        public void ChangeBannerInfoUp()
        {
//            if(ViewHistory.getCurChannelList().size() > 1)
//            {
//                ViewHistory.setChannelUp();
//                UpdateEpgPF();
//            }
        }

        public void ChangeBannerInfoDown()
        {
//            if(ViewHistory.getCurChannelList().size() > 1)
//            {
//                ViewHistory.setChannelDown();
//                UpdateEpgPF();
//            }
        }

        public void ChangePreProgram() {
//            if(ViewHistory.getPreChannel() != null &&
//                    (ViewHistory.getPreChannel().get_channel_id() != ViewHistory.getCurChannel().get_channel_id() &&
//                            ViewHistory.getCurGroupType() == ViewHistory.getPreGroupType())) {
//                av_control_play_by_channel_id(ViewHistory.getPlayId(),ViewHistory.getPreChannel().get_channel_id(),ViewHistory.getPreGroupType(),1);
//                UpdateEpgPF();
//            }
        }

        public boolean ChangeGroup(List<SimpleChannel> channelList, int groupType, long channelId) {
//            if (channelList == null)    // Johnny add 20180122
//            {
//                channelList = new ArrayList<>();
//            }
//
//            if(channelList.size() > 0) {
//                av_control_play_by_channel_id(ViewHistory.getPlayId(),channelId, groupType,1);
//                UpdateEpgPF();
//                return true;
//            }
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
//            pip_start(ViewHistory.getCurPipChannel().get_channel_id(),1);
        }

        public void ClosePipScreen()
        {
            pip_stop();
            pip_stop();
//            ViewHistory.setCurPipIndex(-1);
        }

        public void SetPipWindow(int x, int y, int width, int height)
        {
            pip_set_window(x+4,y+4,width - 4,height - 4);
        }

        public void SetAvPipExChange()
        {
//            ViewHistory.setAvPipExchange();
            pip_exchange();
        }

        public void ChangePipChannelUp()
        {
//            if(ViewHistory.getCurChannelList().size() > 1)
//            {
//                ViewHistory.setPipChannelUp();
//            }
        }

        public void ChangePipChannelDown()
        {
//            if(ViewHistory.getCurChannelList().size() > 1)
//            {
//                ViewHistory.setPipChannelDown();
//            }
        }

        public void ChangePipProgram()
        {
//            pip_start(ViewHistory.getCurPipChannel().get_channel_id(),1);
        }

        public void UpdatePipEpgPF() {
//            if(ViewHistory.getCurPipChannel() != null) {
//                Log.d(TAG, "UpdatePipEpgPF:  ==>> " + ViewHistory.getCurPipChannel().get_channel_id());
//
//                EpgPreData = get_present_event(ViewHistory.getCurPipChannel().get_channel_id());
//                EpgFolData = get_follow_event(ViewHistory.getCurPipChannel().get_channel_id());
//            }
        }

        public boolean ChangePipChannelByDigi(int displayNum, int tpId) {
//            for(int i = 0; i < ViewHistory.getCurChannelList().size(); i++) {
//                if(ViewHistory.getCurChannelList().get(i).get_channel_num() == displayNum) {
//                    pip_start(ViewHistory.getCurChannelList().get(i).get_channel_id(),1);
//                    return true;
//                }
//            }
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
//            Log.d(TAG, "ChangeProgram: position == " + position);
//            final int pos = position;
//            if(ViewHistory.getCurChannel() != null) {
//                if (ViewHistory.getCurChannel().get_channel_id() != programInfoList.get(pos).get_channel_id())
//                {
//                    av_control_play_by_channel_id(ViewHistory.getPlayId(), programInfoList.get(pos).get_channel_id(), GroupType,1);
//                }
//            }
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

    public int get_usb_speed_by_serial(String serial) {
        int speed = 0;
        try {
            LogUtils.d("get_usb_speed_by_serial serial = " + serial);
            speed = mPrimeMiscService.getUsbSpeedByDeviceSerial(serial);
        } catch (RemoteException e) {
            Log.e(TAG, "get_usb_speed_by_serial: ", e);
        }

        return speed;
    }

    public int get_usb_speed_by_disk_sys_path(String path) {
        int speed = 0;
        try {
            LogUtils.d("get_usb_speed sys path = " + path);
            speed = mPrimeMiscService.getUsbSpeedByDiskSysPath(path);
        } catch (RemoteException e) {
            Log.e(TAG, "get_usb_speed_by_disk_sys_path: ", e);
        }

        return speed;
    }

    public void remove_wvcas_license(String licenseId) {
        try {
            LogUtils.d("remove licenseId = " + licenseId);
            if(licenseId == null)
                licenseId = "";
            mPrimeMediaService.removeWvcasLicense(licenseId);
        } catch (RemoteException e) {
            Log.e(TAG, "remove_wvcas_license: ", e);
        }
    }

//    public static boolean isRemoteControl (BluetoothDevice device) {
//        BluetoothClass bluetoothClass = device.getBluetoothClass();
//        LogUtils.d("isRemoteControl device="+device+" name="+device.getName()
//                +" getDeviceClass="+bluetoothClass.getDeviceClass()
//                +" devicetype="+device.getType());
//        if (bluetoothClass.getMajorDeviceClass() == BluetoothClass.Device.Major.PERIPHERAL &&
//                BluetoothDevice.DEVICE_TYPE_LE == device.getType() &&
//                (((bluetoothClass.getDeviceClass() & MINOR_DEVICE_CLASS_REMOTE)!= 0)
//                        || ((bluetoothClass.getDeviceClass() & MINOR_DEVICE_CLASS_KEYBOARD)!= 0))) {
//            Log.d(TAG,"isRemoteControl device="+device+" is remote control");
//            return true;
//        }
//        return false;
//    }
//    public static boolean hasRemoteControl () {
//        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (btAdapter != null) {
//            final Set<BluetoothDevice> bondedDevices = btAdapter.getBondedDevices();
//            if (bondedDevices != null) {
//                for (BluetoothDevice device : bondedDevices) {
//                    if (isRemoteControl(device)) {
//                        Log.d(TAG, "Still has remote control device="+device);
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }
//
//    public static void check_bt_remote_connect(Context context) {
//        if (hasRemoteControl())
//            return;
//        Intent newIntent = new Intent();
//        newIntent.setComponent(new ComponentName(PRIME_BTPAIR_PACKAGE, PRIME_BTPAIR_HOOKBEGINACTIVITY));
//        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(newIntent);
//    }

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
                sendCasError(ErrorCode.ERROR_E511, "");
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
                    av_control_play_by_channel_id(programInfo.getTunerId(), programInfo.getChannelId(), programInfo.getType(), 1);
                }
            }
            LogUtils.d("End");
        },"handleCasRefresh");

        newThread.start();
    }

    private void sendCasError(final int errorCode, final String msg) {
        Log.d(TAG, "sendCasError: errorCode = " + errorCode + " msg = " + msg);
        TVMessage tvMessage = TVMessage.SetShowErrorMsg(errorCode, msg, Pvcfg.CA_TYPE.CA_WIDEVINE_CAS);
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
            case MONDAY: {
                pesiDayOfWeek = BookInfo.BOOK_WEEK_DAY_MONDAY;
            }break;
            case TUESDAY: {
                pesiDayOfWeek = BookInfo.BOOK_WEEK_DAY_TUESDAY;
            }break;
            case WEDNESDAY: {
                pesiDayOfWeek = BookInfo.BOOK_WEEK_DAY_WEDNESDAY;
            }break;
            case THURSDAY: {
                pesiDayOfWeek = BookInfo.BOOK_WEEK_DAY_THURSDAY;
            }break;
            case FRIDAY: {
                pesiDayOfWeek = BookInfo.BOOK_WEEK_DAY_FRIDAY;
            }break;
            case SATURDAY: {
                pesiDayOfWeek = BookInfo.BOOK_WEEK_DAY_SATURDAY;
            }break;
            case SUNDAY: {
                pesiDayOfWeek = BookInfo.BOOK_WEEK_DAY_SUNDAY;
            }break;
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
            case BookInfo.BOOK_CYCLE_ONETIME: {
                nextBookDateTime = get_next_date_onetime(localDateTime, bookDateTime);
            }break;
            case BookInfo.BOOK_CYCLE_DAILY: {
                nextBookDateTime = get_next_date_daily(localDateTime, bookDateTime);
            }break;
            case BookInfo.BOOK_CYCLE_WEEKLY: {
                nextBookDateTime = get_next_date_weekly(localDateTime, bookDateTime);
            }break;
            case BookInfo.BOOK_CYCLE_WEEKEND: {
                nextBookDateTime = get_next_date_weekend(localDateTime, bookDateTime);
            }break;
            case BookInfo.BOOK_CYCLE_WEEKDAYS: {
                nextBookDateTime = get_next_date_weekdays(localDateTime, bookDateTime);
            }break;
            case BookInfo.BOOK_CYCLE_MONTHLY: {
                nextBookDateTime = get_next_date_monthly(localDateTime, bookDateTime);
            }break;
            case BookInfo.BOOK_CYCLE_SERIES:
            case BookInfo.BOOK_CYCLE_SERIES_EMPTY: {
                nextBookDateTime = get_next_date_series(localDateTime, bookDateTime, bookinfo);
            }break;
            default: {
                LogUtils.w("Unknown book cycle...");
            }break;
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

    public void delete_maildata_of_db(int mail_id) {
        gDtvFramework.delete_maildata_of_db(mail_id);
    }

    public void save_maildata_to_db(MailData mailData) {
        gDtvFramework.save_maildata_to_db(mailData);
    }

    public MailData get_mail_from_db(int id) {
        return gDtvFramework.get_mail_from_db(id);
    }

    public List<MailData> get_mail_list_from_db() {
        return gDtvFramework.get_mail_list_from_db();
    }

    public void setACSMusicList(String musicList) {
        gDataFromLauncher.setACSMusicList(musicList);
    }

    public List<MusicInfo> getACSMusicList(){
        String musicList = gDataFromLauncher.getACSMusicList();
        if (musicList == null)
            return new ArrayList<>();

        return JsonParser.parse_music_info(musicList);
    }

    public int getPlay_index() {
        return AvCmdMiddle.getPlay_index();
    }

    public void WaitAVPlayReady() {
        AvCmdMiddle.WaitAVPlayReady();
    }

    public void category_update_to_fav(List<ProgramInfo> ProgramInfoList, List<MusicInfo> musicInfoList) {
        Scan_utils mScanUtils = new Scan_utils(mContext);
        mScanUtils.category_update_to_fav(ProgramInfoList, musicInfoList);
    }

    public void clear_cas_data() {
        CasRefreshHelper cas_helper  = CasRefreshHelper.get_instance();
        cas_helper.clear_cas_data();
    }

    public CasData get_cas_data() {
        CasRefreshHelper cas_helper  = CasRefreshHelper.get_instance();
        CasData cas_data = cas_helper.get_cas_data();
        return cas_data;
    }

    public List<FavGroup> fav_group_get_list() {
        return gDtvFramework.fav_group_get_list();
    }

    public void setSetVisibleCompleted(boolean isVisibleCompleted) {
        gDtvFramework.setSetVisibleCompleted(isVisibleCompleted);
    }

    public int init_tuner() {
        return gDtvFramework.init_tuner();
    }

    public boolean ca_monitor_switch(boolean isECM,int enable){
        LogUtils.d("@@@### ca_monitor_switch");
        return gPrimeDtvMediaPlayer.ca_monitor_switch(isECM,enable);
    }

    public byte[] get_service_status(boolean isECM){
        LogUtils.d("@@@### get_service_status");
        return gPrimeDtvMediaPlayer.get_service_status(isECM);
    }

    public IrdetoInfo.UcServiceStatus get_extended_service_status(boolean isECM){
        LogUtils.d("@@@### get_extended_service_status");
        return gPrimeDtvMediaPlayer.get_extended_service_status(isECM);
    }

    public IrdetoInfo.UcStreamStatus get_stream_status(boolean isECM){
        LogUtils.d("@@@### get_stream_status");
        return gPrimeDtvMediaPlayer.get_stream_status(isECM);
    }

    public IrdetoInfo.UcProductStatus get_product_list(){
        LogUtils.d("@@@### get_product_list");
        return gPrimeDtvMediaPlayer.get_product_list();
    }
    // ======= client status =============
    public IrdetoInfo.UcCDSN get_cdsn(){
        LogUtils.d("@@@### get_cdsn");
        return gPrimeDtvMediaPlayer.get_cdsn();
    }
    public IrdetoInfo.UcNationality get_nationality(){
        LogUtils.d("@@@### get_nationality");
        return gPrimeDtvMediaPlayer.get_nationality();
    }
    public IrdetoInfo.UcOperatorInfo get_operator_info(){
        LogUtils.d("@@@### get_operator_info");
        return gPrimeDtvMediaPlayer.get_operator_info();
    }
    public int get_cssn(){
        LogUtils.d("@@@### get_cssn");
        return gPrimeDtvMediaPlayer.get_cssn();
    }
    public IrdetoInfo.UcEcmEmmCount get_ecm_emm_count(){
        LogUtils.d("@@@### get_ecm_emm_count");
        return gPrimeDtvMediaPlayer.get_ecm_emm_count();
    }
    public IrdetoInfo.UcFlexiFlshStatus get_flexi_flash_status(){
        LogUtils.d("@@@### get_flexi_flash_status");
        return gPrimeDtvMediaPlayer.get_flexi_flash_status();
    }
    public IrdetoInfo.UcFlexiCoreStatus get_flexi_core_status(){
        LogUtils.d("@@@### get_flexi_core_status");
        return gPrimeDtvMediaPlayer.get_flexi_core_status();
    }

    public IrdetoInfo.UcSerialNumber get_serial_number(){
        LogUtils.d("@@@### get_serial_number");
        return gPrimeDtvMediaPlayer.get_serial_number();
    }

    public IrdetoInfo.UcTmsData get_tms_data(){
        LogUtils.d("@@@### get_tms_data");
        return gPrimeDtvMediaPlayer.get_tms_data();
    }

    public IrdetoInfo.UcBuildInfo get_build_info(){
        LogUtils.d("@@@### get_build_info");
        return gPrimeDtvMediaPlayer.get_build_info();
    }

    public int get_secure_type(){
        LogUtils.d("@@@### get_secure_type");
        return gPrimeDtvMediaPlayer.get_secure_type();
    }

    public int get_lock_id(){
        LogUtils.d("@@@### get_lock_id");
        return gPrimeDtvMediaPlayer.get_lock_id();
    }

    public IrdetoInfo.UcVersion get_version(){
        LogUtils.d("@@@### get_version");
        return gPrimeDtvMediaPlayer.get_version();
    }

    public IrdetoInfo.UcCapability get_capability(){
        LogUtils.d("@@@### get_capability");
        return gPrimeDtvMediaPlayer.get_capability();
    }
    // ======= client status =============

    public IrdetoInfo.UcMailList get_mail_by_uni_id(long uniId){
        LogUtils.d("@@@### get_capability");
        return gPrimeDtvMediaPlayer.get_mail_by_uni_id(uniId);
    }

    public int get_mail_total_count(){
        LogUtils.d("@@@### get_mail_total_count");
        return gPrimeDtvMediaPlayer.get_mail_total_count();
    }

    public IrdetoInfo.UcMailList get_mails(){
        LogUtils.d("@@@### get_capability");
        return gPrimeDtvMediaPlayer.get_mails();
    }

    public int set_mail_status_to_read(int unid){
        LogUtils.d("@@@### set_mail_status_to_read");
        return gPrimeDtvMediaPlayer.set_mail_status_to_read(unid);
    }

    public int delete_mail_by_unid(int unid){
        LogUtils.d("@@@### delete_mail_by_unid");
        return gPrimeDtvMediaPlayer.delete_mail_by_unid(unid);
    }

    public IrdetoInfo.UcAttributeList get_attribute_by_uni_id(long uniId){
        LogUtils.d("@@@### get_attribute_by_uni_id");
        return gPrimeDtvMediaPlayer.get_attribute_by_uni_id(uniId);
    }

    public int get_attribute_total_count(){
        LogUtils.d("@@@### get_attribute_total_count");
        return gPrimeDtvMediaPlayer.get_attribute_total_count();
    }

    public IrdetoInfo.UcAttributeList get_attributes(int startIndex,int getNumber){
        LogUtils.d("@@@### get_attributes");
        return gPrimeDtvMediaPlayer.get_attributes(startIndex,getNumber);
    }

    public int set_attribute_status_to_read(int index){
        LogUtils.d("@@@### set_attribute_status_to_read");
        return gPrimeDtvMediaPlayer.set_attribute_status_to_read(index);
    }

    public int delete_attribute_by_index(int index){
        LogUtils.d("@@@### get_attribute_total_count");
        return gPrimeDtvMediaPlayer.delete_attribute_by_index(index);
    }

    public int delete_all_mails(){
        LogUtils.d("@@@### delete_all_mails");
        return gPrimeDtvMediaPlayer.delete_all_mails();
    }

    public int delete_all_attributes(){
        LogUtils.d("@@@### delete_all_attributes");
        return gPrimeDtvMediaPlayer.delete_all_attributes();
    }

    public IrdetoInfo.UcLoaderInfo get_irdeto_loader_info(){
        LogUtils.d("@@@### get_irdeto_loader_info");
        return gPrimeDtvMediaPlayer.get_irdeto_loader_info();
    }

    public int set_token(long token){
        LogUtils.d("@@@### set_token token = "+token);
        return gPrimeDtvMediaPlayer.set_token(token);
    }

    public boolean notify_pmt(ProgramInfo programInfo, boolean force){
        if(Pvcfg.getCAType() == Pvcfg.CA_TYPE.CA_IRDETO) {
            LogUtils.d("@@@### notify_pmt");
            return gPrimeDtvMediaPlayer.notify_pmt(programInfo, force);
        }
        return false;
    }

    public boolean notify_cat(ProgramInfo programInfo, byte [] raw_data){
        if(Pvcfg.getCAType() == Pvcfg.CA_TYPE.CA_IRDETO) {
            LogUtils.d("@@@### notify_cat");
            return gPrimeDtvMediaPlayer.notify_cat(programInfo, raw_data);
        }
        return false;
    }

    public static class MiscService {
        public static final int MISC_CMD_PRINTKERNEL = 0x1000 ;
        public static final int MISC_CMD_PRINTIFCONFIG = 0x1001 ;
        public static final int MISC_CMD_GET_EDID_INFO = 0x1004 ;
        public static final int MISC_CMD_GET_EDID_AUDIO_INFO = 0x1005 ;
        public static final int MISC_CMD_GET_HDD_SERIES = 0x1006 ;
        public static final int MISC_CMD_SET_ASPECT_RATIO = 0x1007 ;
        public static final int MISC_CMD_SET_HDCPLEVEL = 0x1008 ;
        public static final int MISC_CMD_GET_HDMI_SUPPORT_FORMAT = 0x1009;
        public static final int MISC_CMD_SET_HDMI_RESOLUTION = 0x100A;
    }

    private static final String ACTION_HDMI_PLUGGED = "android.intent.action.HDMI_PLUGGED";
    public static final String ACTION_TICKER_READY = "com.prime.dtv.ACTION_TICKER_READY";
    private static final String PERMISSION_TICKER = "com.prime.permission.TICKER";

    private final BroadcastReceiver gBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_HDMI_PLUGGED.equals(action)) {
                boolean state = intent.getBooleanExtra("state", false);
                Log.d(TAG, "HDMI_PLUGGED State: " + (state ? "已連接" : "已拔出"));
    
                if (!state) {
                    // 拔掉時如果暫時不需要 EDID，就先什麼都不做
                    return;
                }
    
                try {
                    IMisc misc = IMisc.getService(); // 建議先用不 block 版本；或 true 也可以
    
                    if (misc == null) {
                        Log.w(TAG, "HDMI_PLUGGED: IMisc service is null, skip get EDID");
                        return;
                    }
    
                    String edid = misc.invokeGetString_cmd(
                            MiscService.MISC_CMD_GET_EDID_INFO,
                            0,
                            0
                    );
    
                    if (edid == null) {
                        Log.w(TAG, "HDMI_PLUGGED: got null EDID string");
                        return;
                    }
    
                    EdidInfo edidInfo = new EdidInfo(edid);
                    String manu = edidInfo.getManufactureName();
                    Log.d(TAG, "HDMI_PLUGGED: manufactureName = " + manu);
                    Pvcfg.setSysEdidHyManuname(manu);
    
                } catch (RemoteException e) {
                    // binder 通訊問題
                    Log.e(TAG, "HDMI_PLUGGED: RemoteException when getting EDID from IMisc", e);
    
                } catch (RuntimeException e) {
                    // 包含 ServiceSpecificException / NPE / parsing 問題
                    Log.e(TAG, "HDMI_PLUGGED: RuntimeException when handling EDID", e);
    
                } catch (Exception e) {
                    // 保險一層，避免任何奇怪 Exception 讓整個 BroadcastReceiver 掛掉
                    Log.e(TAG, "HDMI_PLUGGED: unexpected Exception when handling EDID", e);
                }
            }
            else if (ACTION_TICKER_READY.equals(action)) {
                Log.d(TAG, "Received ACTION_TICKER_READY, notifying UI");
                TVMessage msg = TVMessage.SetTickerReady();
//                gDtvCallback.onMessage(msg);
            }
        }
    };
    

    public void registerBroadcastReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_HDMI_PLUGGED);
        filter.addAction(ACTION_TICKER_READY);
        context.registerReceiver(gBroadcastReceiver, filter, PERMISSION_TICKER, null);
    }

    public void do_factory_reset(){
        Intent resetIntent = new Intent("android.intent.action.FACTORY_RESET");
        resetIntent.setPackage("android");
        resetIntent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        resetIntent.putExtra("android.intent.extra.REASON", "ResetConfirmFragment");
        mContext.sendBroadcast(resetIntent);
    }

    public void set_Ethernet_static_ip(String ip, int netMask_prefixLength, String gateway, String dns){
        LogUtils.d("qQQQ ip: "+ip+" prefix: "+netMask_prefixLength+" gateway: "+gateway+" dns: "+dns);
        PrimeSystemApp.set_Ethernet_static_ip(mContext, ip, netMask_prefixLength, gateway,dns);
    }

    public String get_hdd_serial(){
        String hdd_series = "";
        try {
            String sys_path = UsbUtils.get_hdd_sys_path(mContext);
            LogUtils.d("sys path = "+sys_path);
            //hdd_series = mPrimeMiscService.invokeGetString_cmd(MiscService.MISC_CMD_GET_HDD_SERIES, 0 , 0);
            if(sys_path == null)
                return "";
            hdd_series = mPrimeMiscService.getHddSeriesNumberByDiskSysPath(sys_path);
            if(hdd_series != null)
                hdd_series = hdd_series.replaceAll("[\\n\\r]", "");
            LogUtils.d("hdd_series = "+hdd_series);
        } catch (RemoteException e) {
            //throw new RuntimeException(e);
            LogUtils.e("e = "+e);
        }

        return hdd_series;
    }

    public void set_Ethernet_dhcp(){
        LogUtils.d(" ");
        PrimeSystemApp.set_Ethernet_dhcp(mContext);
    }

    public String get_Ethernet_mac(){
        return NetworkUnit.get_Ethernet_mac();
    }

    public String get_Wifi_mac(){
        return NetworkUnit.get_Wifi_mac();
    }

    private void update_cas_config() {
        Log.d(TAG, "update_cas_config: ");
        Thread thread = new Thread(() -> {
            CasRefreshHelper cas_helper  = CasRefreshHelper.get_instance();
            CasData cas_data = cas_helper.get_cas_data();
            Pvcfg.setTryLicenseEntitledOnly(cas_data.getTryLicenseIfEntitledChannel() == 1);
            //Pvcfg.setPVR_PJ(cas_data.getPvr() == 1);
        },"update_cas_config");

        thread.start();
    }

    //For TvInputManager => Parental Rating realted
    public Set<TvContentRating> getRatings(){
        return TvInputManagerUtils.getRatings(mContext);
    }

    public void set_parental_rating_enable(Boolean enable){
        TvInputManagerUtils.set_enable(mContext, enable);
    }

    public void remove_all_rattings(){
        TvInputManagerUtils.remove_all_rattings(mContext);
    }

    public void add_rating(TvContentRating rating){
        TvInputManagerUtils.add_rating(mContext, rating);
    }

    private void notifyVbmPvrEvent(int msgType, int action, int param1, Object obj) {
        if (gDtvCallback == null) return;

        Bundle bundle = new Bundle();
        bundle.putInt("action", action);
        bundle.putInt("request_from", 0);
        bundle.putInt("launch_from", 0);

        if (obj instanceof PvrRecFileInfo) {
            PvrRecFileInfo info = (PvrRecFileInfo) obj;

            try {
                // (A) Service ID
                // ?�設 getServiceId() ?�傳 int ??String
                bundle.putString("service_id", "test");

                // (B) Event Name
                bundle.putString("event_name", info.getEventName());

                // (C) Date & Time Range (Value_4 & Value_5)
                // ?�設 info.getStartTime() ?�傳 long (ms)
                // ?�設 info.getDuration() ?�傳 int (�?
                long startTime = info.getStartTime();
                long durationMs = info.getDurationSec() * 1000L;
                long endTime = startTime + durationMs;

                // VBM ?��?: Date="yyyyMMdd", TimeRange="HHmm-HHmm"
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
                SimpleDateFormat timeFormat = new SimpleDateFormat("HHmm", Locale.US);

                String dateStr = dateFormat.format(new Date(startTime));
                String startStr = timeFormat.format(new Date(startTime));
                String endStr = timeFormat.format(new Date(endTime));
                String timeRange = startStr + "-" + endStr;

                bundle.putString("date", dateStr);
                bundle.putString("time_range", timeRange);

                Log.d(TAG, "VBM PVR Info: " + info.getEventName() + ", " + dateStr + ", " + timeRange);

            } catch (Exception e) {
                Log.e(TAG, "Error parsing PvrRecFileInfo for VBM: " + e.getMessage());
            }
        } else {
            Log.w(TAG, "notifyVbmPvrEvent: obj is not PvrRecFileInfo");
        }

        TVMessage msg = new TVMessage(TVMessage.FLAG_VBM, msgType);
        //msg.setParam1(param1);
        //msg.SetVbmRecord(bundle);

        gDtvCallback.onMessage(msg);
    }

    private String getVbmDiskSpaceString() {
        try {

            List<Long> usbSize = UsbUtils.get_usb_space_info();
            if (usbSize != null && usbSize.size() >= 2) {
                long totalSize = usbSize.get(0); // 假設單位為 MB
                long availableSize = usbSize.get(1);

                if (totalSize <= 0) return "NA";

                long usedSize = totalSize - availableSize;
                int percent = (int) (usedSize * 100 / totalSize);

                // 轉換為 GB 顯示 (若原始單位是 MB)
                double usedGb = usedSize / 1024.0;

                return String.format(Locale.US, "%.0fGB-%d%%", usedGb, percent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calculating VBM disk space", e);
        }
        return "NA";
    }

    // -------------------------------------------------------------------------
    // VBM Helper Methods for Book/Reservation (Agent 7)
    // -------------------------------------------------------------------------

    /**
     * [VBM Agent 7, EventType 0] Operation in EPG (Reservation) - 3.8.1
     * 用於 EPG 預約 (BookInfo.getEpgEventId() != -1)
     */
    private void notifyVbmBookReservation(BookInfo bookInfo, int action) {
        if (gDtvCallback == null || bookInfo == null) return;

        try {
            // Value_0~Value_8（共 9 個）
            String[] values = new String[9];

        // Value_0: Request from (0: Master)
            values[0] = "0";

            // Value_1: Service Id
            values[1] = "0";

        // Value_2: Date (yyyyMMdd)
            String dateStr = String.format(
                    Locale.US, "%04d%02d%02d",
                    bookInfo.getYear(), bookInfo.getMonth(), bookInfo.getDate()
            );
            values[2] = dateStr;

        // Value_3: Start/End time (HHmm-HHmm)
        int startH = bookInfo.getStartTime() / 100;
        int startM = bookInfo.getStartTime() % 100;

        int durVal = bookInfo.getDuration();
        int durationMin = (durVal / 100) * 60 + (durVal % 100);

        int endTotalMin = (startH * 60 + startM) + durationMin;
        int endH = (endTotalMin / 60) % 24;
        int endM = endTotalMin % 60;

            values[3] = String.format(Locale.US, "%02d%02d-%02d%02d", startH, startM, endH, endM);

        // Value_4: Event name
            values[4] = bookInfo.getEventName();

            // Value_5: Action
            values[5] = String.valueOf(action);

        // Value_6: Type (0: Single, 1: Series)
        int type = (bookInfo.getBookCycle() == BookInfo.BOOK_CYCLE_SERIES) ? 1 : 0;
            values[6] = String.valueOf(type);

            // Value_7: Launch from
            values[7] = "0";

            // Value_8: Disk space
            values[8] = getVbmDiskSpaceString();

            // sendVbmMessage（Agent 7 / EventType 0）
            sendVbmMessage("7", "0", values);

        } catch (Exception e) {
            Log.e(TAG, "Error constructing VBM Book Reservation values", e);
        }
    }

    /**
     * [VBM Agent 7, EventType 1] Operation in Record by Time - 3.8.2
     * 用於手動時間預約 (BookInfo.getEpgEventId() == -1)
     */
    private void notifyVbmBookRecordByTime(BookInfo bookInfo) {
        if (gDtvCallback == null || bookInfo == null) return;

        try {
            // Agent 7 / EventType 1
            // Value_0~Value_7（共 8 個）
            String[] values = new String[8];

        // Value_0: Request from
            values[0] = "0";

        // Value_1: Service Id
            values[1] = "0";

        // Value_2: Mode (0: Single, 1: Everyday, 2: Every week)
            int mode;
            int cycle = bookInfo.getBookCycle();
            if (cycle == BookInfo.BOOK_CYCLE_ONETIME) mode = 0;
            else if (cycle == BookInfo.BOOK_CYCLE_DAILY) mode = 1;
            else mode = 2; 
            values[2] = String.valueOf(mode);

        // Value_3: Start date (yyyyMMdd)
            String dateStr = String.format(
                    Locale.US, "%04d%02d%02d",
                    bookInfo.getYear(), bookInfo.getMonth(), bookInfo.getDate()
            );
            values[3] = dateStr;

        // Value_4: Start time (HHmm)
            values[4] = String.format(Locale.US, "%04d", bookInfo.getStartTime());

        // Value_5: Duration (Minutes)
            int durVal = bookInfo.getDuration();
            int durationMins = (durVal / 100) * 60 + (durVal % 100);
            values[5] = String.valueOf(durationMins);

            // Value_6: Repetition
            values[6] = convertWeekToRepetitionString(bookInfo.getWeek(), mode);

            // Value_7: Disk space
            values[7] = getVbmDiskSpaceString();

            // sendVbmMessage（record-only JSON 由 sendVbmMessage 產生）
            sendVbmMessage("7", "1", values);

        } catch (Exception e) {
            Log.e(TAG, "Error processing RecordByTime Log", e);
        }
    }

    private String convertWeekToRepetitionString(int weekFlag, int mode) {
        if (mode == 0) return "0000000";
        if (mode == 1) return "1111111";
        StringBuilder sb = new StringBuilder();
        int[] days = {
                BookInfo.BOOK_WEEK_DAY_SUNDAY, BookInfo.BOOK_WEEK_DAY_MONDAY,
                BookInfo.BOOK_WEEK_DAY_TUESDAY, BookInfo.BOOK_WEEK_DAY_WEDNESDAY,
                BookInfo.BOOK_WEEK_DAY_THURSDAY, BookInfo.BOOK_WEEK_DAY_FRIDAY,
                BookInfo.BOOK_WEEK_DAY_SATURDAY
        };
        for (int dayMask : days) {
            sb.append((weekFlag & dayMask) != 0 ? "1" : "0");
        }
        return sb.toString();
    }

    /**
     * [VBM Agent 7, EventType 2] Operation in Recording List - 3.8.3
     * 用於刪除預約 (BookInfo Delete)
     * Action: 1 (Delete single), 3 (Delete all)
     */
    private void notifyVbmBookRecordingListOp(int action, String info) {
        if (gDtvCallback == null) return;

        try {
            // Agent 7 / EventType 2
            // Value_0~Value_3（共 4 個）
            String[] values = new String[4];

        // Value_0: Request from
            values[0] = "0";

        // Value_1: Action
            values[1] = String.valueOf(action);

            // Value_2: Information
            values[2] = (info != null) ? info : "";

            // Value_3: Disk space
            values[3] = getVbmDiskSpaceString();

            // sendVbmMessage
            sendVbmMessage("7", "2", values);

        } catch (Exception e) {
            Log.e(TAG, "Error constructing VBM Book Recording List Op values", e);
        }
    }


    /**
     * 發送 VBM Agent 7 (Recorded List Operation) 事件
     * @param action 0:Check info, 1:Delete single, 2:Delete channel, 3:Delete all, 4:Playback single, 5:Playback series
     * @param info Event name, Channel name, or "All"
     * @param episode Single:0, Series:Episode number
     */
    private void notifyVbmPvrListOp(int action, String info, int episode) {
        if (gDtvCallback == null) return;

        try {
            // Agent 7 / EventType 3
            // Value_0~Value_4（共 5 個）
            String[] values = new String[5];

        // Value_0: Request from (0: Master)
            values[0] = "0";

        // Value_1: Action
            values[1] = String.valueOf(action);

            // Value_2: Information
            values[2] = (info != null) ? info : "";

            // Value_3: Play back episode
            values[3] = String.valueOf(episode);

            // Value_4: Occupied disk space
            values[4] = getVbmDiskSpaceString();

            // sendVbmMessage
            sendVbmMessage("7", "3", values);

        } catch (Exception e) {
            Log.e(TAG, "Error constructing VBM PVR List Op values", e);
        }
    }


    /**
     * 發送 VBM Agent 7 (Operation Result) 事件
     * @param result 0: Record completely, 1: Record incompletely
     * @param serviceId Service ID
     * @param startTime 錄影開始時間 (用於計算日期與時間範圍)
     * @param durationSec 錄影時長 (秒)
     */
    private void notifyVbmPvrResult(int result, String serviceId, long startTime, int durationSec) {
        if (gDtvCallback == null) return;

        try {
        // 格式化日期與時間
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HHmm", Locale.US);

        Date startDate = new Date(startTime);
        Date endDate = new Date(startTime + (durationSec * 1000L));

            // Agent 7 / EventType 4
            // Value_0~Value_5（共 6 個）
            String[] values = new String[6];

            // Value_0: Request from (0: Master)
            values[0] = "0";

            // Value_1: Service Id
            values[1] = (serviceId != null) ? serviceId : "0";

            // Value_2: Date (yyyyMMdd)
            values[2] = dateFormat.format(startDate);

            // Value_3: Time range (HHmm-HHmm)
            values[3] = timeFormat.format(startDate) + "-" + timeFormat.format(endDate);

            // Value_4: Occupied disk space
            values[4] = getVbmDiskSpaceString();

            // Value_5: Result
            values[5] = String.valueOf(result);

            // sendVbmMessage
            sendVbmMessage("7", "4", values);

        } catch (Exception e) {
            Log.e(TAG, "Error constructing VBM PVR Result values", e);
        }
    }
    public void handleTunerSignalStatusReport(String[] params) {
        Log.i(TAG, "handleTunerSignalStatusReport");

        if (params == null || params.length < 3) {
            Log.e(TAG, "Invalid parameters (need freq, symbolrate, qam)");
            sendVbmMessage("9", "0", getVbmTunerStatus(0));
            return;
        }

        final String freqListStr = params[0] == null ? "" : params[0].trim();
        final int symbolRateKsym;
        final int qam;
        try {
            symbolRateKsym = Integer.parseInt(params[1] == null ? "0" : params[1].trim());
            qam = Integer.parseInt(params[2] == null ? "0" : params[2].trim());
        } catch (NumberFormatException e) {
            Log.e(TAG, "parse params error", e);
            sendVbmMessage("9", "0", getVbmTunerStatus(0));
            return;
        }

        // 去重但保留順序
        final java.util.LinkedHashSet<Integer> reqFreqKhzSet = new java.util.LinkedHashSet<>();
        if (!TextUtils.isEmpty(freqListStr)) {
            for (String p : freqListStr.split("/")) {
                if (TextUtils.isEmpty(p)) continue;
                try {
                    reqFreqKhzSet.add(Integer.parseInt(p.trim()));
                } catch (NumberFormatException ignored) {
                    Log.w(TAG, "skip invalid freq: " + p);
                }
            }
        }
        final java.util.ArrayList<Integer> reqFreqKhz = new java.util.ArrayList<>(reqFreqKhzSet);

        if (reqFreqKhz.isEmpty() || symbolRateKsym <= 0 || qam <= 0) {
            Log.w(TAG, "no valid request, report current status");
            sendVbmMessage("9", "0", getVbmTunerStatus(0));
            return;
        }

        new Thread(() -> {
            final int tunerCount = get_tuner_num(); // 規格：四個 tuner，只處理 0~3

            java.util.ArrayList<Integer> idleTuners = new java.util.ArrayList<>();
            java.util.HashSet<Integer> busyFreqsKhz = new java.util.HashSet<>();

            // 1) 判斷 busy/idle（保守：status 失敗視為 busy）
            for (int t = 0; t < tunerCount; t++) {
                boolean locked;
                try {
                    locked = get_tuner_status(t);
                } catch (Exception e) {
                    Log.w(TAG, "get_tuner_status failed for tuner " + t + ", treat as BUSY");
                    locked = true; // 保守：避免誤動到正在使用中的 tuner
                }

                if (locked) {
                    try {
                        int f = get_frequency(t); // 你們這裡通常是 MHz
                        int fKhz = (f > 10000) ? f : (f * 1000);
                        busyFreqsKhz.add(fKhz);
                        Log.i(TAG, "Tuner " + t + " BUSY at " + fKhz + " KHz");
                    } catch (Exception e) {
                        Log.w(TAG, "get_frequency failed for tuner " + t);
                    }
                } else {
                    idleTuners.add(t);
                }
            }

            // 2) 只分配「目前 busy tuner 沒有鎖到的頻點」
            java.util.ArrayList<Integer> pending = new java.util.ArrayList<>();
            for (Integer f : reqFreqKhz) {
                if (!busyFreqsKhz.contains(f)) pending.add(f);
            }

            // 3) 依序用 idle tuner 鎖 pending freq
            java.util.ArrayList<Integer> lockedTuners = new java.util.ArrayList<>();
            int idx = 0;
            for (int tunerId : idleTuners) {
                if (idx >= pending.size()) break;

                int targetFreqKhz = pending.get(idx++);
                try {
                    TVTunerParams pObj = TVTunerParams.CreateTunerParamDVBC(
                            tunerId,
                            MiscDefine.TpInfo.NONE_SAT_ID,
                            -1,
                            targetFreqKhz,     // ★這裡務必再確認 CreateTunerParamDVBC 的單位
                            symbolRateKsym,
                            qam
                    );
                    tuner_lock(pObj);
                    lockedTuners.add(tunerId);
                    Log.i(TAG, "Locked tuner " + tunerId + " to " + targetFreqKhz + " KHz");
                } catch (Exception e) {
                    Log.e(TAG, "tuner_lock failed for tuner " + tunerId, e);
                }
            }

            // 4) 等待鎖定：輪詢（最多 2 秒）
            long deadline = android.os.SystemClock.elapsedRealtime() + 2000;
            while (!lockedTuners.isEmpty() && android.os.SystemClock.elapsedRealtime() < deadline) {
                boolean allLocked = true;
                for (int t : lockedTuners) {
                    try {
                        if (!get_tuner_status(t)) { allLocked = false; break; }
                    } catch (Exception e) {
                        allLocked = false; break;
                    }
                }
                if (allLocked) break;
                try { Thread.sleep(150); } catch (InterruptedException e) { break; }
            }

            // 5) 回報 Agent 9（四個 tuner 狀態）
            String[] tunerStatus = getVbmTunerStatus(0);
            sendVbmMessage("9", "0", tunerStatus);

            // TODO：若規格要求同時回報 AERM，這裡還要加 AERM 上報
            // TODO：若平台允許，建議量測後釋放/解鎖 idle tuner，避免長期佔用

        }, "TunerSignalStatusReport").start();
    }


    private void set_cns_ota() {
        gDtvFramework.set_cns_ota();
    }

    public String ota_get_update_info() {
        return gDtvFramework.ota_get_update_info();
    }

    public String ota_upload_first_boot_info() {
        return gDtvFramework.ota_upload_first_boot_info();
    }

    public String ota_boot_login() {
        return gDtvFramework.ota_boot_login();
    }

    public String ota_upload_update_status() {
        return gDtvFramework.ota_upload_update_status();
    }

    public void start_ota_update() {;
        gDtvFramework.start_ota_update();
    }

    public void format_hdd(){
        PrimeSystemApp.format_hdd(mContext);
    }
}
