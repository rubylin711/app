package com.prime.dtvservice;

import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_ID;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_FAIL;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_OBJ;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_STATUS;
import static com.prime.datastructure.CommuincateInterface.MiscDefine.COMMAND_REPLY_SUCCESS;
import static com.prime.datastructure.CommuincateInterface.PmModule.CMD_ServicePlayer_PM_GetProgramByChannelId;
import static com.prime.datastructure.CommuincateInterface.PmModule.CMD_ServicePlayer_PM_GposInfoGet;
import static com.prime.datastructure.CommuincateInterface.PmModule.CMD_ServicePlayer_PM_TpInfoGet;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.prime.datastructure.CommuincateInterface.AvModule;
import com.prime.datastructure.CommuincateInterface.CommandBase;
import com.prime.datastructure.CommuincateInterface.CommonModule;
import com.prime.datastructure.CommuincateInterface.IPrimeDtvService;
import com.prime.datastructure.CommuincateInterface.IPrimeDtvServiceCallback;

import com.prime.dtv.PrimeDtv;
import com.prime.dtvservice.Interface.AvModuleCommand;
import com.prime.dtvservice.Interface.BookModuleCommand;
import com.prime.dtvservice.Interface.CAModuleCommand;
import com.prime.dtvservice.Interface.CommonModuleCommand;
import com.prime.dtvservice.Interface.EpgModuleCommand;
import com.prime.dtvservice.Interface.FeModuleCommand;
import com.prime.dtvservice.Interface.OtaModuleCommand;
import com.prime.dtvservice.Interface.PmModuleCommand;
import com.prime.dtvservice.Interface.PvrModuleCommand;
import com.prime.dtvservice.Interface.ScanModuleCommand;
import com.prime.dtvservice.Interface.TvInputManagerCommand;
import com.prime.dtv.service.CNS.IrdCommand;
import java.nio.charset.StandardCharsets;

public class PrimeDtvService extends Service {
    public static String TAG = "PrimeDtvService";
    private static IPrimeDtvServiceCallback gPrimeDtvServiceCallback = null;
    private static boolean isRunning = false;
    private static final boolean IRD_COMMAND_DEBUG = false;

    // ===== Foreground Service Notification =====
    private static final String CHANNEL_ID = "prime_dtv_service";
    private static final int NOTI_ID = 1001;
    private boolean mForegroundStarted = false;

    private final BroadcastReceiver mSimulateIrdReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(!IRD_COMMAND_DEBUG)
                return;
            if ("com.prime.dtv.SIMULATE_IRD".equals(action)) {
                String data = intent.getStringExtra("data");
                Log.d(TAG, "Received SIMULATE_IRD broadcast, data: " + data);
                if (data != null) {
                    byte[] commandBytes = data.getBytes(StandardCharsets.UTF_8);
                    IrdCommand irdCommand = new IrdCommand(context, commandBytes, 0);
                    irdCommand.doIrdCommand(PrimeDtvServiceApplication.get_prime_dtv());
                }
            }
        }
    };

    public static boolean isPrimeDtvServiceRunning() {
        return isRunning;
    }

    public static void notifyMessage(com.prime.datastructure.utils.TVMessage msg) {
        if (PrimeDtvServiceApplication.getInstance() != null) {
            PrimeDtvServiceApplication.getInstance().broadcastMessage(msg);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created v2");

        // ★★ 最重要：一進來先前景化（避免 startForegroundService ANR）
        ensureForeground();
        if(IRD_COMMAND_DEBUG) {
            IntentFilter filter = new IntentFilter("com.prime.dtv.SIMULATE_IRD");
            registerReceiver(mSimulateIrdReceiver, filter);
        }
        PrimeDtvServiceApplication.getInstance().registerHandler(gServiceHandler);
        isRunning = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");

        unregisterReceiver(mSimulateIrdReceiver);

        PrimeDtvServiceApplication.getInstance().unregisterHandler(gServiceHandler);
        isRunning = false;

        // (可選) 若你想停掉前景通知：
        // stopForeground(true);
        // mForegroundStarted = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 保險：若 service 被系統重啟，onCreate 後仍可能再次進入
        ensureForeground();
        Log.d(TAG, "onStartCommand intent=" + (intent == null ? "null" : intent.getAction()));
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // bind 也可能喚醒 service（某些情境），保險一下
        // ensureForeground();
        return gPlayerService;
    }

    // 你原本的 handler（目前沒用到消息）
    private final Handler gServiceHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            // TVMessage tvMsg = (TVMessage) msg.obj;
            // if (gPrimeDtvServiceCallback != null)
            // {
            // try {
            // gPrimeDtvServiceCallback.onMessage(tvMsg);
            // } catch (RemoteException e) {
            // e.printStackTrace();
            // }
            // }
        }
    };

    // ========= AIDL =========
    IPrimeDtvService.Stub gPlayerService = new IPrimeDtvService.Stub() {
        // @Override
        // public ParcelableClass invoke(int commandId, ParcelableClass param) throws
        // RemoteException {
        // Log.d(TAG, "invoke commandId:" + commandId + " param:" + param.toString());
        // switch (commandId) {
        // case 1111 :
        // {
        // TpInfo tpInfo = param.getData(TpInfo.class);
        // Log.d(TAG, "invoke tpInfo:" + tpInfo.ToString());
        // ProgramInfo programInfo = new ProgramInfo();
        // programInfo.setServiceId(0);
        // programInfo.setCA(1);
        // programInfo.setType(2);
        // programInfo.setLCN(3);
        // programInfo.setDisplayNum(4);
        // programInfo.setDisplayName("5");
        // programInfo.setLock(6);
        // programInfo.setSkip(7);
        // programInfo.setPvrSkip(8);
        // programInfo.setTpId(9);
        // programInfo.setSatId(10);
        // for( int i = 0 ; i < 2 ; i++ )
        // {
        // ProgramInfo.AudioInfo audioInfo = new ProgramInfo.AudioInfo();
        // audioInfo.setCodec(100+i);
        // audioInfo.setPid(200+i);
        // audioInfo.setLeftIsoLang("1111"+i);
        // audioInfo.setRightIsoLang("2222"+i);
        // programInfo.pAudios.add(audioInfo);
        // }
        //
        // for( int i = 0 ; i < 2 ; i++ )
        // {
        // ProgramInfo.SubtitleInfo subtitleInfo = new ProgramInfo.SubtitleInfo();
        // subtitleInfo.setPid(300+i);
        // subtitleInfo.setLang("3333"+i);
        // subtitleInfo.setComPageId(400+i);
        // subtitleInfo.setAncPageId(500+i);
        // programInfo.pSubtitle.add(subtitleInfo);
        // }
        //
        // for (int i = 0; i < 2; i++) {
        // ProgramInfo.TeletextInfo teletextInfo = new ProgramInfo.TeletextInfo();
        // teletextInfo.setPid(600 + i);
        // teletextInfo.setLang("6666" + i);
        // teletextInfo.setMagazineNum(700 + i);
        // teletextInfo.setPageNum(800 + i);
        // programInfo.pTeletext.add(teletextInfo);
        // }
        //
        // ProgramInfo.VideoInfo videoInfo = new ProgramInfo.VideoInfo();
        // videoInfo.setCodec(900);
        // videoInfo.setPID(1000);
        // for (int i = 0; i < 2; i++) {
        // ProgramInfo.CaInfo caInfo = new ProgramInfo.CaInfo();
        // caInfo.setCaSystemId(1100 + i);
        // caInfo.setEcmPid(1200 + i);
        // String str = "abcdef"+i;
        // caInfo.setPrivateData(str.getBytes(StandardCharsets.UTF_8));
        // videoInfo.CaInfoList.add(caInfo);
        // }
        // programInfo.pVideo = videoInfo;
        //
        // Log.d(TAG, "invoke programInfo:" + programInfo.ToString());
        // PrimeDtvServiceApplication.getInstance().getServiceInterface().get_prime_dtv().get_ca_version();
        // return new ParcelableClass(programInfo);
        // }
        // default:
        // {
        //
        // }break;
        // }
        // return new ParcelableClass( null);
        // }

        @Override
        public Bundle invokeBundle(Bundle data) throws RemoteException {
            Log.d(TAG, "PrimeTvInputAppApplication invokeBundle");

            // 確保 classLoader
            data.setClassLoader(PrimeDtvServiceApplication.getInstance().getClassLoader());

            PrimeDtv primeDtv = PrimeDtvServiceApplication.get_prime_dtv();
            Bundle reply = new Bundle();
            reply.setClassLoader(PrimeDtvServiceApplication.getInstance().getClassLoader());

            int command_id = data.getInt(COMMAND_ID, 0);

            // init command 允許在 primeDtv 尚未 ready 時先走
            if (command_id == CommonModule.CMD_ServicePlayer_COMMON_InitService) {
                reply = new CommonModuleCommand().executeCommand(data, reply, primeDtv);
                return reply;
            }

            if (primeDtv == null) {
                Log.w(TAG, "PrimeDtvService not ready !!");
                reply.putInt(COMMAND_REPLY_STATUS, COMMAND_REPLY_FAIL);
                return reply;
            }
            switch (getCommandBase(command_id)) {
                case CommandBase.CMD_ServicePlayer_AV_Base: {
                    Log.d(TAG, "PlayerControl invokeBundle CMD_ServicePlayer_AV_Base primeDtv = " + primeDtv);
                    reply = new AvModuleCommand().executeCommand(data, reply, primeDtv);
                }
                    break;
                case CommandBase.CMD_ServicePlayer_PM_Base: {
                    Log.d(TAG, "invokeBundle CMD_ServicePlayer_PM_Base");
                    reply = new PmModuleCommand().executeCommand(data, reply, primeDtv);
                }
                    break;
                case CommandBase.CMD_ServicePlayer_SCAN_Base: {
                    Log.d(TAG, "invokeBundle CMD_ServicePlayer_SCAN_Base");
                    reply = new ScanModuleCommand().executeCommand(data, reply, primeDtv);
                }
                    break;
                case CommandBase.CMD_ServicePlayer_FE_Base: {
                    Log.d(TAG, "invokeBundle CMD_ServicePlayer_FE_Base");
                    reply = new FeModuleCommand().executeCommand(data, reply, primeDtv);
                }
                    break;
                case CommandBase.CMD_ServicePlayer_EPG_Base: {
                    Log.d(TAG, "invokeBundle CMD_ServicePlayer_EPG_Base");
                    reply = new EpgModuleCommand().executeCommand(data, reply, primeDtv);
                }
                    break;
                case CommandBase.CMD_ServicePlayer_PVR_Base: {
                    Log.d(TAG, "invokeBundle CMD_ServicePlayer_PVR_Base");
                    reply = new PvrModuleCommand().executeCommand(data, reply, primeDtv);
                }
                    break;
                case CommandBase.CMD_ServicePlayer_COMMON_Base: {
                    Log.d(TAG, "invokeBundle CMD_ServicePlayer_COMMON_Base");
                    reply = new CommonModuleCommand().executeCommand(data, reply, primeDtv);
                }
                    break;
                case CommandBase.CMD_ServicePlayer_TVINPUT_MANAGER_Base: {
                    Log.d(TAG, "invokeBundle CMD_ServicePlayer_TVINPUT_MANAGER_Base");
                    reply = new TvInputManagerCommand().executeCommand(data, reply, primeDtv);
                }
                    break;
                case CommandBase.CMD_ServicePlayer_OTA_Base: {
                    Log.d(TAG, "invokeBundle CMD_ServicePlayer_OTA_Base");
                    reply = new OtaModuleCommand().executeCommand(data, reply, primeDtv);
                }
                    break;
                case CommandBase.CMD_ServicePlayer_BOOK_Base: {
                    Log.d(TAG, "invokeBundle CMD_ServicePlayer_BOOK_Base");
                    reply = new BookModuleCommand().executeCommand(data, reply, primeDtv);
                }
                    break;
                case CommandBase.CMD_ServicePlayer_CA_Base: {
                    Log.d(TAG, "invokeBundle CMD_ServicePlayer_CA_Base");
                    reply = new CAModuleCommand().executeCommand(data, reply, primeDtv);
                }
                    break;
                default:
                    // unknown base
                    break;
            }

            return reply;
        }

        @Override
        public void registerCallback(IPrimeDtvServiceCallback callback, String caller) throws RemoteException {
            Log.d("PrimeDtvServiceApplication", "[PrimeDtvServiceAIDL]  registerCallback caller=" + caller + " cb=" + callback);
            PrimeDtvServiceApplication.getInstance().add_aidl_callback(callback, caller);
        }

        @Override
        public void unregisterCallback(IPrimeDtvServiceCallback callback) throws RemoteException {
            Log.d("PrimeDtvServiceApplication", "[PrimeDtvServiceAIDL]  unregisterCallback cb=" + callback);
            PrimeDtvServiceApplication.getInstance().remove_aidl_callback(callback);
        }

        // 你未實作的先留著
        @Override
        public com.prime.datastructure.CommuincateInterface.ParcelableClass invokeParcel(int commandId,
                com.prime.datastructure.CommuincateInterface.ParcelableClass param) throws RemoteException {
            return null;
        }
    };

    public int getCommandBase(int commandId) {
        int commandBase = (commandId >> 8) << 8;
        Log.d(TAG, "commandBase = 0x" + Integer.toHexString(commandBase));
        return commandBase;
    }

    // ========= Foreground =========
    private void ensureForeground() {
        if (mForegroundStarted)
            return;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel ch = new NotificationChannel(
                        CHANNEL_ID,
                        "Prime DTV Service",
                        NotificationManager.IMPORTANCE_MIN);
                ch.setShowBadge(false);
                NotificationManager nm = getSystemService(NotificationManager.class);
                if (nm != null)
                    nm.createNotificationChannel(ch);
            }

            // 注意：先用系統 icon 讓你能直接編過；
            // 之後再換成你自己的 R.drawable.xxx
            Notification n = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Prime DTV")
                    .setContentText("Service running")
                    .setOngoing(true)
                    .setSmallIcon(android.R.drawable.stat_notify_sync)
                    .build();
            if (Build.VERSION.SDK_INT >= 29) {
                startForeground(NOTI_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
            } else {
                startForeground(NOTI_ID, n);
            }
            mForegroundStarted = true;

        } catch (Throwable t) {
            // 若這裡失敗，系統仍可能因為沒前景化而殺 service
            Log.e(TAG, "ensureForeground failed", t);
        }
    }
}
