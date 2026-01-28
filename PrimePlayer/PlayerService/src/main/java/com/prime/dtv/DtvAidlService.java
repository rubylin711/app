package com.prime.dtv;

import static com.prime.datastructure.CommuincateInterface.ScanModule.CMD_ServicePlayer_SCAN_StartScan;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.prime.datastructure.CommuincateInterface.IPrimeDtvServiceCallback;
import com.prime.datastructure.CommuincateInterface.IPrimeDtvService;
import com.prime.datastructure.sysdata.ProgramInfo;
import com.prime.datastructure.sysdata.TpInfo;
import com.prime.datastructure.CommuincateInterface.ParcelableClass;
import com.prime.datastructure.utils.TVMessage;
import com.prime.datastructure.utils.TVScanParams;

import java.nio.charset.StandardCharsets;

public class DtvAidlService extends Service {
    private static String TAG = "PlayerService";
    private static IPrimeDtvServiceCallback gPlayerCallback = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        ServiceApplication.getInstance().registerHandler(gServiceHandler);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        ServiceApplication.getInstance().unregisterHandler(gServiceHandler);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started via startService()");
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return gPlayerService;
    }

    private final Handler gServiceHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            TVMessage tvMsg = (TVMessage) msg.obj;
            if (gPlayerCallback != null)
            {
                try {
                    gPlayerCallback.onMessage(tvMsg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    IPrimeDtvService.Stub gPlayerService = new IPrimeDtvService.Stub() {
//        @Override
//        public ParcelableClass invoke(int commandId, ParcelableClass param) throws RemoteException {
//            Log.d(TAG, "invoke commandId:" + commandId + " param:" + param.toString());
//            switch (commandId) {
//                case CMD_ServicePlayer_SCAN_StartScan:
//                {
//                    Log.d(TAG, "invoke CMD_ServicePlayer_SCAN_StartScan");
//                    TVScanParams scanParams = param.getData(TVScanParams.class);
//                    ServiceInterface.get_prime_dtv().start_scan(scanParams);
//                    return null;
//                }
//                case 1111 :
//                {
//                    TpInfo tpInfo = param.getData(TpInfo.class);
//                    Log.d(TAG, "invoke tpInfo:" + tpInfo.ToString());
//                    ProgramInfo programInfo = new ProgramInfo();
//                    programInfo.setServiceId(0);
//                    programInfo.setCA(1);
//                    programInfo.setType(2);
//                    programInfo.setLCN(3);
//                    programInfo.setDisplayNum(4);
//                    programInfo.setDisplayName("5");
//                    programInfo.setLock(6);
//                    programInfo.setSkip(7);
//                    programInfo.setPvrSkip(8);
//                    programInfo.setTpId(9);
//                    programInfo.setSatId(10);
//                    for( int i = 0 ; i < 2 ; i++ )
//                    {
//                        ProgramInfo.AudioInfo audioInfo = new ProgramInfo.AudioInfo();
//                        audioInfo.setCodec(100+i);
//                        audioInfo.setPid(200+i);
//                        audioInfo.setLeftIsoLang("1111"+i);
//                        audioInfo.setRightIsoLang("2222"+i);
//                        programInfo.pAudios.add(audioInfo);
//                    }
//
//                    for( int i = 0 ; i < 2 ; i++ )
//                    {
//                        ProgramInfo.SubtitleInfo subtitleInfo = new ProgramInfo.SubtitleInfo();
//                        subtitleInfo.setPid(300+i);
//                        subtitleInfo.setLang("3333"+i);
//                        subtitleInfo.setComPageId(400+i);
//                        subtitleInfo.setAncPageId(500+i);
//                        programInfo.pSubtitle.add(subtitleInfo);
//                    }
//
//                    for (int i = 0; i < 2; i++) {
//                        ProgramInfo.TeletextInfo teletextInfo = new ProgramInfo.TeletextInfo();
//                        teletextInfo.setPid(600 + i);
//                        teletextInfo.setLang("6666" + i);
//                        teletextInfo.setMagazineNum(700 + i);
//                        teletextInfo.setPageNum(800 + i);
//                        programInfo.pTeletext.add(teletextInfo);
//                    }
//
//                    ProgramInfo.VideoInfo videoInfo = new ProgramInfo.VideoInfo();
//                    videoInfo.setCodec(900);
//                    videoInfo.setPID(1000);
//                    for (int i = 0; i < 2; i++) {
//                        ProgramInfo.CaInfo caInfo = new ProgramInfo.CaInfo();
//                        caInfo.setCaSystemId(1100 + i);
//                        caInfo.setEcmPid(1200 + i);
//                        String str = "abcdef"+i;
//                        caInfo.setPrivateData(str.getBytes(StandardCharsets.UTF_8));
//                        videoInfo.CaInfoList.add(caInfo);
//                    }
//                    programInfo.pVideo = videoInfo;
//
//                    Log.d(TAG, "invoke programInfo:" + programInfo.ToString());
//                    ServiceInterface.get_prime_dtv().get_ca_version();
//                    return new ParcelableClass(programInfo);
//                }
//                default:
//                {
//
//                }break;
//            }
//            return new ParcelableClass( null);
//        }

        @Override
        public Bundle invokeBundle(Bundle data) throws RemoteException {
            return null;
        }

        @Override
        public ParcelableClass invokeParcel(int commandId, ParcelableClass param) throws RemoteException {
            return null;
        }

        @Override
        public void registerCallback(IPrimeDtvServiceCallback callback,String connectedFrom) throws RemoteException {
            gPlayerCallback = callback;
        }

        @Override
        public void unregisterCallback(IPrimeDtvServiceCallback callback) throws RemoteException {
            gPlayerCallback = null;
        }
    };
}
