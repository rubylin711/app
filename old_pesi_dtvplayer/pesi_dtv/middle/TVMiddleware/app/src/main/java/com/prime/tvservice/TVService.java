package com.prime.tvservice;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.prime.sysglob.TpInfoFunc;
import com.prime.utils.ITVCallback;
import com.prime.utils.TVMessage;
import com.prime.utils.TVScanParams;
import com.prime.utils.TVTunerParams;

import com.prime.utils.TVTunerParams;//eric lin test data
import com.prime.TestData.TestData;//eric lin test data
import com.prime.sysdata.ProgramInfo;//eric lin test data
import com.prime.sysdata.TpInfo;//eric lin test data
import java.util.List;//eric lin test data
import java.util.Random;//eric lin test data
import static java.lang.Thread.sleep;//eric lin test data

/**
 * Created by ethan_lin on 2017/10/30.
 */

public class TVService extends Service {
    private static final String TAG = "TVService";

    private static final int MSG_START_SCAN = 1000;
    private static final int MSG_STOP_SCAN = 1001;

    private static final int MSG_TUNER_GET_STRENGTH = 2000;
    private static final int MSG_TUNER_GET_QUALITY = 2001;
    private static final int MSG_TUNER_GET_LOCKSTATUS = 2002;
    private static final int MSG_TUNER_SET_FREQUENCY = 2003;

    private static Boolean scan_flag = false;//eric lin test data
    private static boolean scan_stop_flag = false;//eric lin test data
    private TestSearchThread tsThread=null;//eric lin test data
    private static int searchedCnt=0;//eric lin test data
    private static int TestServicId=9000;//eric lin test data
    private static int TestLCN=900;//eric lin test data
    private static int s_tuner_id;//eric lin test data
    private static int s_tpId;//eric lin test data
    private static int s_scanMode;//eric lin test data
    private static int s_searchOptionTVRadio;//eric lin test data
    private static int s_searchOptionCaFta;//eric lin test data
    TestData td = new TestData(TpInfo.DVBC);//eric lin test data
    private static int sTunerLockStatus;//eric lin test data
    private Handler CheckSignalHandler=null;//eric lin test data
    private int scanedTvCnt=0;//eric lin test data
    private int scanedRadioCnt=0;//eric lin test data
    private boolean thread_exit=false;//eric lin test data


    public TVService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind:");
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
       return mbinder;
    }

    private ITVService.Stub mbinder = new ITVService.Stub() {
        @Override
        public void registerCallback(ITVCallback cb) throws RemoteException {
            synchronized(TVService.this) {
                if(cb != null)
                    callbacks.register(cb);
            }
        }

        @Override
        public void unregisterCallback(ITVCallback cb) throws RemoteException {

            //eric lin test data, -start
            exitScanThread();
            /*
            if(tsThread!=null){
                //tsThread.interrupt();
                thread_exit = true;
                tsThread=null;
                Log.d(TAG, "unregisterCallback: tsThread  exit!!!");
            }
            else
                Log.d(TAG, "unregisterCallback: tsThread is NULL !!!");
            */
            //eric lin test data, -end

            synchronized(TVService.this) {
                if(cb != null)
                    callbacks.unregister(cb);
            };
        }

        @Override
        public void StartScan(TVScanParams param) throws RemoteException {
            Log.d(TAG, "StartScan");

            //eric lin test data, -start
            if(tsThread==null){
                thread_exit = false;
                reset_param(false, true);
                tsThread =new TestSearchThread();
                tsThread.start();
                Log.d(TAG, "StartScan: TestSearchThread  start !");
            }
            else
                Log.d(TAG, "StartScan: already exist !");
            //eric lin test data, -end

            Message msg = handler.obtainMessage(MSG_START_SCAN, param);
            handler.sendMessage(msg);
        }

        @Override
        public void StopScan(boolean store) throws RemoteException {
            Log.d(TAG, "StopScan");
            Message msg = handler.obtainMessage(MSG_STOP_SCAN, store);
            handler.sendMessage(msg);
        }

        @Override
        public int GetStrength(int tuner_id) throws RemoteException {
            //Log.d(TAG, "GetStrength");
            //return 60;
            //eric lin test data, -start     
            if(sTunerLockStatus == 0)
                return 20;
            else if(sTunerLockStatus == 1){
                Random ran = new Random();
                return (21 + ran.nextInt(79));
            }
            else
                return 0;
            //eric lin test data, -end
        }

        @Override
        public int GetQuality(int tuner_id) throws RemoteException {
            //Log.d(TAG, "GetQuality");
            //return 90;
            //eric lin test data, -start     
            if(sTunerLockStatus == 0)
                return 20;
            else if(sTunerLockStatus == 1){
                Random ran = new Random();
                return (21 + ran.nextInt(79));
            }
            else
                return 0;
            //eric lin test data, -end
        }

        @Override
        public int GetLockStatus(int tuner_id) throws RemoteException {
            //Log.d(TAG, "GetLockStatus");
            //eric lin test data, -start     
            Random ran = new Random();
            sTunerLockStatus = ran.nextInt(2);
            return sTunerLockStatus;
            //return 1;
            //eric lin test data, -end
        }
		//ethan 20171201 modify TuneFrontEnd -s
        @Override
        public int TuneFrontEnd(TVTunerParams tp) throws RemoteException {
            int tunerId = tp.getTunerId();
            int tpId = tp.getTpId();
            int fe_type = tp.getFe_type();
            int frequency = tp.getFrequency();
            int symbol, qam, bandwith, polar;
            switch (fe_type){
                case TVTunerParams.FE_TYPE_DVBT:
                    bandwith = tp.getBandwith();
                    Log.d(TAG, "TuneFrontEnd (DVBT): TunerID = "+tunerId+" tpId = "+tpId+" Frequency = "+frequency+" Bandwith = "+bandwith);
                    break;
                case TVTunerParams.FE_TYPE_DVBS:
                    symbol = tp.getSymbolRate();
                    polar = tp.getPolar();
                    Log.d(TAG, "TuneFrontEnd (DVBS): TunerID = "+tunerId+" tpId = "+tpId+" Frequency = "+frequency+" SymbolRatte  = "+symbol+" Polar = "+polar);
                    break;
                case TVTunerParams.FE_TYPE_DVBC:
                    symbol = tp.getSymbolRate();
                    qam = tp.getQam();
                    Log.d(TAG, "TuneFrontEnd (DVBC): TunerID = "+tunerId+" tpId = "+tpId+" Frequency = "+frequency+" SymbolRatte  = "+symbol+" Qam = "+qam);
                    break;
        	}
            return 0;
        }//ethan 20171201 modify TuneFrontEnd -e

        @Override
        public void SendEPGUpdateMsg(int serviceType, int serviceChNum) throws  RemoteException {
            sendMessage( TVMessage.SetEPGUpdate(serviceType, serviceChNum) );
        }   // johnny test send epgupdate msg 20171211

        @Override
        public int GetBER(int tunerId) throws RemoteException {
            Random ran = new Random();
            return ran.nextInt(100);
        }

        @Override
        public int GetSNR(int tunerId) throws RemoteException {
            Random ran = new Random();
            return ran.nextInt(100);
        }
    };

    final RemoteCallbackList<ITVCallback> callbacks
            = new RemoteCallbackList<ITVCallback>();

    private synchronized void sendMessage(TVMessage msg){
        //Log.d(TAG,"sendMessage : "+msg.getMsgFlag());
        final int N = callbacks.beginBroadcast();
        for (int i = 0; i < N; i++){
            try{
                callbacks.getBroadcastItem(i).onMessage(msg);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        callbacks.finishBroadcast();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            Log.d(TAG+"-MSG", "handleMessage : "+msg.what);
            switch (msg.what){
                case MSG_START_SCAN:{
                    TVScanParams sp = (TVScanParams) msg.obj;
                    resolveStartScan(sp);
                }break;
                case MSG_STOP_SCAN:{
                    boolean store = (boolean) msg.obj;
                    resolveStopScan(store);
                }break;
            }
        }
    };

    private void resolveStartScan(TVScanParams sp){
        Log.d(TAG, "resolveStartScan");
        sendMessage(TVMessage.SetScanBegin());

        //eric lin test data, -start     
        s_tuner_id = sp.getTunerId();
        s_tpId = sp.getTpId();
        s_scanMode = sp.getScanMode();
        s_searchOptionTVRadio = sp.getSearchOptionTVRadio();
        s_searchOptionCaFta = sp.getSearchOptionCaFta();
        reset_param(true, false);
        //eric lin test data, -end

    }

    private void resolveStopScan(boolean store){
        Log.d(TAG, "resolveStopScan");
        //TODO , need to do save scan result to DB
        scan_stop_flag = true;//eric lin test data
    }

    //eric lin test data, -start
    private void reset_param(boolean flag, boolean tunerP_flag)
    {
        scan_flag = flag;
        scan_stop_flag = false;
        searchedCnt=0;
        scanedTvCnt = 0;
        scanedRadioCnt = 0;
        if(tunerP_flag == true) {
            s_scanMode = -1;
            s_searchOptionTVRadio = -1;
            s_searchOptionCaFta = -1;
        }
    }

    private void TestScanSetScanEnd()
    {
        int tmpTvCnt, tmpRadioCnt;

        exitScanThread();

        tmpTvCnt = scanedTvCnt;
        tmpRadioCnt = scanedRadioCnt;
        
        reset_param(false, true);
        Log.d(TAG, "TestScanSetScanEnd:");        
        sendMessage(TVMessage.SetScanEnd(tmpTvCnt, tmpRadioCnt));
    }

    private void TestSetScanResultUpdateMsg(int ServiceId,int serviceType,int lcn , String ServiceName, int CA_Flag, int TpCount){
        



            sendMessage(TVMessage.SetScanResultUpdate(ServiceId, serviceType, lcn, ServiceName, CA_Flag, TpCount));

    }

    public void exitScanThread(){
        Log.d(TAG, "exitScanThread");

        if(tsThread!=null){
            //tsThread.interrupt();
            thread_exit = true;
            tsThread=null;
            Log.d(TAG, "unregisterCallback: tsThread  exit!!!");
        }
        else
            Log.d(TAG, "unregisterCallback: tsThread is NULL !!!");
    }

    private void TestScanResultUpdate(int cnt, int searchOptionTVRadio, int searchOptionCaFta)
    {

            Log.d(TAG, "TestScanResultUpdate: cnt=" + cnt + "searchOptionTVRadio=" + searchOptionTVRadio
                    + "searchOptionCaFta=" + searchOptionCaFta);
            int SearchedCnt = cnt + 1;

            if (scan_stop_flag == true) {
                return;
            }
            //special case for tp3 not receive data
            if (cnt != 0 && cnt % 7 == 0) {
                //sendMessage(TVMessage.SetScanResultUpdate(0, ProgramInfo.ALL_TV_TYPE, 0, "SearchTVxxx", 0, SearchedCnt));
                TestSetScanResultUpdateMsg(0, ProgramInfo.ALL_TV_TYPE, 0, "SearchTVxxx", 0, SearchedCnt);
                return;
            }

            if (searchOptionTVRadio == TVScanParams.SEARCH_OPTION_ALL) {
                if (searchOptionCaFta == TVScanParams.SEARCH_OPTION_ALL) {
                    //sendMessage(TVMessage.SetScanResultUpdate(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 0, SearchedCnt));
                    TestSetScanResultUpdateMsg(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 0, SearchedCnt);
                    //sendMessage(TVMessage.SetScanResultUpdate(TestServicId++, ProgramInfo.ALL_RADIO_TYPE, TestLCN++, String.format("SearchRadio%d", TestServicId), 0, SearchedCnt));
                    TestSetScanResultUpdateMsg(TestServicId++, ProgramInfo.ALL_RADIO_TYPE, TestLCN++, String.format("SearchRadio%d", TestServicId), 0, SearchedCnt);
                    //sendMessage(TVMessage.SetScanResultUpdate(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 1, SearchedCnt));
                    TestSetScanResultUpdateMsg(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 1, SearchedCnt);
                    scanedTvCnt += 2;
                    scanedRadioCnt += 1;
                    Log.d(TAG, "TestScanResultUpdate: BK1  cnt="+cnt+", scanedTvCnt"+scanedTvCnt+", scanedRadioCnt"+scanedRadioCnt);
                    //
                    if (cnt % 3 == 1) {
                        //sendMessage(TVMessage.SetScanResultUpdate(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 0, SearchedCnt));
                        TestSetScanResultUpdateMsg(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 0, SearchedCnt);
                        scanedTvCnt += 1;
                        Log.d(TAG, "TestScanResultUpdate: BK2  cnt="+cnt+", scanedTvCnt"+scanedTvCnt+", scanedRadioCnt"+scanedRadioCnt);
                    } else if (cnt % 3 == 2) {
                        //sendMessage(TVMessage.SetScanResultUpdate(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 0, SearchedCnt));
                        TestSetScanResultUpdateMsg(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 0, SearchedCnt);
                        //sendMessage(TVMessage.SetScanResultUpdate(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 0, SearchedCnt));
                        TestSetScanResultUpdateMsg(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 0, SearchedCnt);
                        scanedTvCnt += 2;
                        Log.d(TAG, "TestScanResultUpdate: BK3  cnt="+cnt+", scanedTvCnt"+scanedTvCnt+", scanedRadioCnt"+scanedRadioCnt);
                    }
                    //
                } else if (searchOptionCaFta == TVScanParams.SEARCH_OPTION_CA_ONLY) {
                    //sendMessage(TVMessage.SetScanResultUpdate(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 1, SearchedCnt));
                    TestSetScanResultUpdateMsg(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 1, SearchedCnt);
                    scanedTvCnt += 1;
                } else if (searchOptionCaFta == TVScanParams.SEARCH_OPTION_FTA_ONLY) {
                    //sendMessage(TVMessage.SetScanResultUpdate(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 0, SearchedCnt));
                    TestSetScanResultUpdateMsg(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 0, SearchedCnt);
                    //sendMessage(TVMessage.SetScanResultUpdate(TestServicId++, ProgramInfo.ALL_RADIO_TYPE, TestLCN++, String.format("SearchRadio%d", TestServicId), 0, SearchedCnt));
                    TestSetScanResultUpdateMsg(TestServicId++, ProgramInfo.ALL_RADIO_TYPE, TestLCN++, String.format("SearchRadio%d", TestServicId), 0, SearchedCnt);
                    scanedTvCnt += 1;
                    scanedRadioCnt += 1;
                    //
                    if (cnt % 3 == 1) {
                        //sendMessage(TVMessage.SetScanResultUpdate(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 0, SearchedCnt));
                        TestSetScanResultUpdateMsg(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 0, SearchedCnt);
                        scanedTvCnt += 1;
                    } else if (cnt % 3 == 2) {
                        //sendMessage(TVMessage.SetScanResultUpdate(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 0, SearchedCnt));
                        TestSetScanResultUpdateMsg(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 0, SearchedCnt);
                        //sendMessage(TVMessage.SetScanResultUpdate(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 0, SearchedCnt));
                        TestSetScanResultUpdateMsg(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 0, SearchedCnt);
                        scanedTvCnt += 2;
                    }
                    //
                }
            } else if (searchOptionTVRadio == TVScanParams.SEARCH_OPTION_TV_ONLY) {
                if (searchOptionCaFta == TVScanParams.SEARCH_OPTION_ALL) {
                    sendMessage(TVMessage.SetScanResultUpdate(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 0, SearchedCnt));
                    sendMessage(TVMessage.SetScanResultUpdate(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 1, SearchedCnt));
                    scanedTvCnt += 2;
                    //
                    if (cnt % 3 == 1) {
                        sendMessage(TVMessage.SetScanResultUpdate(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 0, SearchedCnt));
                        scanedTvCnt += 1;
                    } else if (cnt % 3 == 2) {
                        sendMessage(TVMessage.SetScanResultUpdate(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 0, SearchedCnt));
                        sendMessage(TVMessage.SetScanResultUpdate(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 0, SearchedCnt));
                        scanedTvCnt += 2;
                    }
                    //
                } else if (searchOptionCaFta == TVScanParams.SEARCH_OPTION_CA_ONLY) {
                    sendMessage(TVMessage.SetScanResultUpdate(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 1, SearchedCnt));
                    scanedTvCnt += 1;
                } else if (searchOptionCaFta == TVScanParams.SEARCH_OPTION_FTA_ONLY) {
                    sendMessage(TVMessage.SetScanResultUpdate(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 0, SearchedCnt));
                    scanedTvCnt += 1;
                    //
                    if (cnt % 3 == 1) {
                        sendMessage(TVMessage.SetScanResultUpdate(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 0, SearchedCnt));
                        scanedTvCnt += 1;
                    } else if (cnt % 3 == 2) {
                        sendMessage(TVMessage.SetScanResultUpdate(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 0, SearchedCnt));
                        sendMessage(TVMessage.SetScanResultUpdate(TestServicId++, ProgramInfo.ALL_TV_TYPE, TestLCN++, String.format("SearchTV%d", TestServicId), 0, SearchedCnt));
                        scanedTvCnt += 2;
                    }
                    //
                }
            } else if (searchOptionTVRadio == TVScanParams.SEARCH_OPTION_RADIO_ONLY) {
                if (searchOptionCaFta == TVScanParams.SEARCH_OPTION_ALL) {
                    sendMessage(TVMessage.SetScanResultUpdate(TestServicId++, ProgramInfo.ALL_RADIO_TYPE, TestLCN++, String.format("SearchRadio%d", TestServicId), 0, SearchedCnt));
                    scanedTvCnt += 1;
                } else if (searchOptionCaFta == TVScanParams.SEARCH_OPTION_CA_ONLY) {

                } else if (searchOptionCaFta == TVScanParams.SEARCH_OPTION_FTA_ONLY) {
                    sendMessage(TVMessage.SetScanResultUpdate(TestServicId++, ProgramInfo.ALL_RADIO_TYPE, TestLCN++, String.format("SearchRadio%d", TestServicId), 0, SearchedCnt));
                    scanedTvCnt += 1;
                }
            }
        
    }

    final Runnable CheckStatusRunnable = new Runnable() {
        int num=100;
        public void run() {
                boolean run = true;
                while (run) {
                    /*
                    synchronized(scan_flag) {
                        if (num > 0) {

                                CheckSignalHandler.postDelayed(CheckStatusRunnable, 10);
                                //Thread.sleep(10);

                            Log.d(TAG, Thread.currentThread().getName() + "this is " + num--);
                            //System.out.println(Thread.currentThread().getName() + "this is " + num--);
                        }
                    }
                    */

                    if (scan_flag == false) {

                            //sleep(1000);
                            CheckSignalHandler.postDelayed(CheckStatusRunnable, 1000);
                            //Log.d(TAG, "TestSearchThread: idle 1000");

                    } else  if(scan_flag == true){

                            //sleep(1000);
                            CheckSignalHandler.postDelayed(CheckStatusRunnable, 1000);
                            Log.d(TAG, "TestSearchThread: scan sleep 1000");
                            Log.d(TAG, "TestSearchThread: scan scan_flag="+scan_flag
                                    +", scan_stop_flag="+scan_stop_flag
                                    +", searchedCnt="+searchedCnt
                                    +", scanedTvCnt="+scanedTvCnt
                                    +", scanedRadioCnt="+scanedRadioCnt);

                        if(scan_flag == true) {//eric lin new test for fix something wrong
                            if (scan_stop_flag == true) {
                                Log.d(TAG, "TestScanSetScanEnd BK1 stop_flag=true");
                                TestScanSetScanEnd();
                            }

                            if (scan_flag == true && s_scanMode == TVScanParams.SCAN_MODE_MANUAL) {
                                Log.d(TAG, "TestSearchThread: SCAN_MODE_MANUAL");
                                TestScanResultUpdate(0, s_searchOptionTVRadio, s_searchOptionCaFta);
                                Log.d(TAG, "TestScanSetScanEnd BK2 manual");
                                TestScanSetScanEnd();
                            } else if (scan_flag == true && s_scanMode == TVScanParams.SCAN_MODE_AUTO) {
                                if (searchedCnt < 10) {//td.GetTestDatTpInfoList().size()) { //eric lin test modify to 20
                                    TestScanResultUpdate(searchedCnt, s_searchOptionTVRadio, s_searchOptionCaFta);
                                    searchedCnt++;
                                    Log.d(TAG, "TestSearchThread: SCAN_MODE_AUTO searchedCnt=" + searchedCnt);
                                }
                                if (searchedCnt >= 10) {//td.GetTestDatTpInfoList().size()) { //eric lin test
                                    Log.d(TAG, "TestScanSetScanEnd BK3 auto");
                                    TestScanSetScanEnd();
                                }
                            }
                        }
                    }else{
                            //sleep(1000);
                            CheckSignalHandler.postDelayed(CheckStatusRunnable, 1000);
                            //Log.d(TAG, "TestSearchThread: else sleep 1000");
                    }
                }



            //Log.d(TAG, "TestSearchThread: delay 1000");
            //CheckSignalHandler.postDelayed(CheckStatusRunnable, 1000);
        }
    };


    class TestSearchThread extends Thread {
        public void run() {
            boolean run = true;
                while (run) {
                    if(thread_exit == true)
                        return;
                    /*
                    synchronized(scan_flag) {
                        if (num > 0) {
                            try {
                                Thread.sleep(10);
                            } catch (Exception e) {
                                e.getMessage();
                            }
                            Log.d(TAG, Thread.currentThread().getName() + "this is " + num--);
                            //System.out.println(Thread.currentThread().getName() + "this is " + num--);
                        }
                    }
                    */
                    if (scan_flag == false) {
                        try {
                            sleep(1000);
                            //Log.d(TAG, "TestSearchThread: idle 1000");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else  if(scan_flag == true){
                        try {
                            sleep(1000);
                            Log.d(TAG, "TestSearchThread: scan sleep 1000");
                            Log.d(TAG, "TestSearchThread: scan scan_flag="+scan_flag
                                    +", scan_stop_flag="+scan_stop_flag
                                    +", searchedCnt="+searchedCnt
                                    +", scanedTvCnt="+scanedTvCnt
                                    +", scanedRadioCnt="+scanedRadioCnt);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                            if (thread_exit != true && scan_stop_flag == true) {
                                Log.d(TAG, "TestScanSetScanEnd BK1 stop_flag=true");
                                TestScanSetScanEnd();
                            }

                            if (thread_exit != true && s_scanMode == TVScanParams.SCAN_MODE_MANUAL) {
                                Log.d(TAG, "TestSearchThread: SCAN_MODE_MANUAL");
                                TestScanResultUpdate(0, s_searchOptionTVRadio, s_searchOptionCaFta);
                                Log.d(TAG, "TestScanSetScanEnd BK2 manual");
                                TestScanSetScanEnd();
                            } else if (thread_exit != true && s_scanMode == TVScanParams.SCAN_MODE_AUTO) {
                                if (searchedCnt < td.GetTestDatTpInfoList().size()) {
                                    TestScanResultUpdate(searchedCnt, s_searchOptionTVRadio, s_searchOptionCaFta);
                                    searchedCnt++;
                                    Log.d(TAG, "TestSearchThread: SCAN_MODE_AUTO searchedCnt=" + searchedCnt);
                                }
                                if (searchedCnt >= td.GetTestDatTpInfoList().size()) {
                                    Log.d(TAG, "TestScanSetScanEnd BK3 auto");
                                    TestScanSetScanEnd();
                                }
                            }
                    }else{
                        try {
                            sleep(1000);
                            //Log.d(TAG, "TestSearchThread: else sleep 1000");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
        }
    }
    //eric lin test data, -end
}
