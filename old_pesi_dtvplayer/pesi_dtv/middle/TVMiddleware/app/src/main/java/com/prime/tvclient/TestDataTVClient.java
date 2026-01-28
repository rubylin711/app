package com.prime.tvclient;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.prime.TestData.TestData;
import com.prime.sysdata.EPGEvent;
import com.prime.sysdata.ProgramInfo;
import com.prime.sysdata.TpInfo;
import com.prime.sysglob.TestDataImpl.TestDataBookInfoFuncImpl;
import com.prime.sysglob.TestDataImpl.TestDataDefaultChannelFuncImpl;
import com.prime.sysglob.TestDataImpl.TestDataEPGEventFuncImpl;
import com.prime.sysglob.TestDataImpl.TestDataFavGroupNameFuncImpl;
import com.prime.sysglob.TestDataImpl.TestDataFavInfoFuncImpl;
import com.prime.sysglob.TestDataImpl.TestDataGposInfoFuncImpl;
import com.prime.sysglob.TestDataImpl.TestDataProgramInfoFuncImpl;
import com.prime.sysglob.TestDataImpl.TestDataSatInfoFuncImpl;
import com.prime.sysglob.TestDataImpl.TestDataTpInfoFuncImpl;
import com.prime.tvservice.ITVService;
import com.prime.utils.ITVCallback;
import com.prime.utils.TVMessage;
import com.prime.utils.TVScanParams;
import com.prime.utils.TVTunerParams;

import java.util.Calendar;

import static java.lang.Thread.sleep;

//import com.prime.sysglob.TestDataImpl.TestDataAntInfoFuncImpl;


/**
 * Created by johnny_shih on 2017/11/22.
 */

public abstract class TestDataTVClient {

    private static final String TAG="TestDataTVClient";

    private ITVService service;
    private Handler handler;
    private Context context;
    public TestDataGposInfoFuncImpl Gpos;
//    public TestDataAntInfoFuncImpl Ant;
    public TestDataSatInfoFuncImpl Sat;
    public TestDataTpInfoFuncImpl Tp;
    public TestDataProgramInfoFuncImpl Program;
    public TestDataBookInfoFuncImpl Book;
    public TestDataFavInfoFuncImpl Fav;
    public TestDataFavGroupNameFuncImpl FavName;
    public TestDataEPGEventFuncImpl EpgEvent;
    public TestDataDefaultChannelFuncImpl DefaultChannel;
    public static TestData TestData;

    private static final int HMSG_CONNECT = 1000;
    private static final int HMSG_DISCONNECT = 1001;
    private static final int HMSG_ONMESSAGE = 1002;

    private static boolean hasTestedEPGUpdate = false;  // johnny test send epgupdate msg 20171211

    private ServiceConnection sConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG,"onServiceConnected");
            Message msg = handler.obtainMessage(HMSG_CONNECT, iBinder);
            handler.sendMessage(msg);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG,"onServiceDisconnected");
            Message msg = handler.obtainMessage(HMSG_DISCONNECT);
            handler.sendMessage(msg);
        }
    };

    abstract public void onConnected();

    abstract public void onDisconnected();

    abstract public void onMessage(TVMessage message);

    abstract public void ConnectDB(Context context);

    private ITVCallback.Stub callback = new ITVCallback.Stub() {
        @Override
        public void onMessage(TVMessage message) throws RemoteException {
            Message msg = handler.obtainMessage(HMSG_ONMESSAGE, message);
            handler.sendMessage(msg);
        }
    };

    @SuppressLint("HandlerLeak")
    public void ConnectService(Context context){
        Log.d(TAG, "ServiceConnect");

        this.context = context;
        TestData = new TestData(TpInfo.DVBC);
//        TestData = new TestData(TpInfo.ISDBT);
//        TestData = new TestData(TpInfo.DVBT);
//        TestData = new TestData(TpInfo.DVBS);
        Gpos = new TestDataGposInfoFuncImpl(context);
//        Ant = new TestDataAntInfoFuncImpl(context);
        Sat = new TestDataSatInfoFuncImpl(context);
        Tp = new TestDataTpInfoFuncImpl(context);
        Program = new TestDataProgramInfoFuncImpl(context);
        Book = new TestDataBookInfoFuncImpl(context);
        Fav = new TestDataFavInfoFuncImpl(context);
        FavName = new TestDataFavGroupNameFuncImpl(context);
        EpgEvent = new TestDataEPGEventFuncImpl(context);
        DefaultChannel = new TestDataDefaultChannelFuncImpl(context);
        handler = new Handler(){
            public void handleMessage(Message msg){
                Log.d(TAG+"-MSG", "handleMessage"+msg.what);
                switch(msg.what){
                    case HMSG_CONNECT:{
                        IBinder binder = (IBinder)msg.obj;
                        service = ITVService.Stub.asInterface(binder);

                        if ( !hasTestedEPGUpdate )  // johnny test send epgupdate msg 20171211
                        {
                            TestEPGUpdate();
                            hasTestedEPGUpdate = true;
                        }

                        try {
                            service.registerCallback(TestDataTVClient.this.callback);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        onConnected();
                        break;
                    }
                    case HMSG_DISCONNECT:{
                        if(service != null) {//ethan 20171121 fixed crash issue
                            onDisconnected();
                            try{
                                service.unregisterCallback(TestDataTVClient.this.callback);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                            service = null;
                        }
                        break;
                    }
                    case HMSG_ONMESSAGE:{
                        TVMessage tvMessage = (TVMessage) msg.obj;

                        onMessage(tvMessage);
                        break;
                    }
                }
            }
        };
        Intent intent = new Intent("com.prime.tvservice.ITVService");
        intent.setPackage("com.prime.tvservice");
        context.bindService(intent, sConn, Context.BIND_AUTO_CREATE);
    }

    public void ServiceDisconnect(Context context){
        Log.d(TAG,"onServiceConnected");
        if(handler!=null){
            Message msg = handler.obtainMessage(HMSG_DISCONNECT);
            handler.sendMessage(msg);
        }
        context.unbindService(sConn);
    }

    public void StartScan(TVScanParams sp){
        Log.d(TAG, "StartScan");
        try{
            service.StartScan(sp);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void StopScan(boolean store){
        Log.d(TAG,"StopScan");
        try{
            service.StopScan(store);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public int getStrength(int tuner_id){
        Log.d(TAG,"getStrength");
        int ret = 0;
        try {
            ret = service.GetStrength(tuner_id);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return ret;
    }
    public int getQuality(int tuner_id) {
        Log.d(TAG,"getQuality");
        int ret = 0;
        try {
            ret = service.GetQuality(tuner_id);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return ret;

    }
    public int getLockStatus(int tuner_id) {
        Log.d(TAG,"getLockStatus");
        int ret = 0;
        try {
            ret = service.GetLockStatus(tuner_id);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return ret;
    }
    public int getBER(int tuner_id){
        Log.d(TAG,"getBER");
        int ret = 0;
        try {
            ret = service.GetBER(tuner_id);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return ret;
    }
    public int getSNR(int tuner_id) {
        Log.d(TAG,"getSNR");
        int ret = 0;
        try {
            ret = service.GetSNR(tuner_id);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return ret;

    }

    //ethan 20171201 modify TuneFrontEnd
    public int TuneFrontEnd(TVTunerParams tvTunerParams) {
        Log.d(TAG,"TundFrontEnd");
        try {
            return service.TuneFrontEnd(tvTunerParams);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // johnny test send epgupdate msg 20171211
    private void SendEPGUpdateMsg(int serviceType, int serviceChNum)
    {
        if ( service == null )
        {
            return;
        }

        Log.d(TAG,"SendEPGUpdateMsg");
        try {
            service.SendEPGUpdateMsg(serviceType, serviceChNum);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void ResetFactoryDefault(){
        //DBTransformer dbTransformer = new DBTransformer(context);
        //try {
        //    dbTransformer.xmlToDatabase(DBTransformer.DEFAULT_DB_PATH);
        //} catch (Exception e) {
        //    e.printStackTrace();
        //}
    }


    // johnny test send epg update message -start 20171208
    private void TestEPGUpdate()
    {
        Thread testEPGUpdateThread = new Thread(new TestEPGUpdateRunnable());
        testEPGUpdateThread.start();
    }

    private void TestEPGUpdateMsgSend(int serviceType, int serviceChNum, int sleepSec)
    {
        try {
            Log.d(TAG, "run:  send msg" );
            SendEPGUpdateMsg(serviceType, serviceChNum);
            Log.d(TAG, "run:  sleep start" );
            sleep(sleepSec*1000);
            Log.d(TAG, "run:  sleep end" );
        } catch (InterruptedException e) {
            Log.d(TAG, "run: exception" );
            e.printStackTrace();
        }
    }

    private class TestEPGUpdateRunnable implements Runnable {
        public void run() { // implements Runnable run()
            int sleepSec = 5;
            int displayNum = 97;//chNum = 3;

            Calendar calendar = Calendar.getInstance();
            int curHour = calendar.get(Calendar.HOUR_OF_DAY);
            int curMin = calendar.get(Calendar.MINUTE);
            int curSec = calendar.get(Calendar.SECOND);

            //TestData testData = new TestData(TpInfo.DVBC);
            EPGEvent epgEvent = new EPGEvent();
            epgEvent.setSid(880);
            epgEvent.setOriginalNetworkId(8945);
            epgEvent.setTransportStreamId(1021);
            epgEvent.setEventId(65520); // for now, eventid will reset to 65520 when service disconnected
            epgEvent.setTableId(80);
            epgEvent.setEventType(EPGEvent.EPG_TYPE_SCHEDULE);
            epgEvent.setEventName("Event Name " + epgEvent.getEventId());
            epgEvent.setEventNameLangCodec("fre");
            epgEvent.setStartTimeUtcM( TestData.GetTestDatEpgEventList_Schedule().get(0).getStartTimeUtcM() );
            epgEvent.setStartTimeUtcL( com.prime.TestData.TestData.GetLongByHMS(curHour, curMin, curSec) );
            epgEvent.setDuration( com.prime.TestData.TestData.GetLongByHMS(0, 10, 0) );
            epgEvent.setParentalRate(0);
            epgEvent.setShortEvent("Short Event  " + epgEvent.getEventId());
            epgEvent.setShortEventLangCodec("fre");
            epgEvent.setExtendedEvent("Extended Event " + epgEvent.getEventId());
            epgEvent.setExtendedEventLangCodec("fre");
            TestData.GetTestDatEpgEventList_Schedule().add( epgEvent );
            TestEPGUpdateMsgSend(ProgramInfo.ALL_TV_TYPE, displayNum, sleepSec);

//            for ( int i = 0 ; i < 9 ; i++)
//            Log.d(TAG, "SendEpg Start");
            while( service != null )
            {
                com.prime.TestData.TestData.AddEPGEvent
                        (   TestData.GetTestDatEpgEventList_Schedule(),
                                1, 0, 0,
                                EPGEvent.EPG_TYPE_SCHEDULE
                        );

                TestEPGUpdateMsgSend(ProgramInfo.ALL_TV_TYPE, displayNum, sleepSec);
            }

//            Log.d(TAG, "SendEpg End");
            hasTestedEPGUpdate = false;
        }
    }
    // johnny test send epg update message -end 20171208
}
