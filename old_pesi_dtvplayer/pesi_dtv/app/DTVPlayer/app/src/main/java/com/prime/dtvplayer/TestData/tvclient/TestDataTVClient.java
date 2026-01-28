package com.prime.dtvplayer.TestData.tvclient;

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

import com.prime.dtvplayer.TestData.TestData.TestData;
import com.prime.dtvplayer.Sysdata.*;
import com.prime.dtvplayer.TestData.sysglob.TestDataImpl.*;
import com.prime.dtvplayer.utils.TVMessage;
import com.prime.dtvplayer.utils.TVScanParams;
import com.prime.dtvplayer.utils.TVTunerParams;

import java.util.Calendar;

import static java.lang.Thread.sleep;

//import com.prime.sysglob.TestDataImpl.TestDataAntInfoFuncImpl;


/**
 * Created by johnny_shih on 2017/11/22.
 */

public abstract class TestDataTVClient {

    private static final String TAG="TestDataTVClient";
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

    abstract public void onConnected();

    abstract public void onDisconnected();

    abstract public void onMessage(TVMessage message);

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
    }

    public void StartScan(TVScanParams sp){
        Log.d(TAG,"StartScan nor work now!!");
    }

    public void StopScan(boolean store){
        Log.d(TAG,"StopScan nor work now!!");
    }

    public int getStrength(int tuner_id){
        Log.d(TAG,"getStrength nor work now!!");
        int ret = 0;
        return ret;
    }
    public int getQuality(int tuner_id) {
        Log.d(TAG,"getQuality nor work now!!");
        int ret = 0;
        return ret;

    }
    public int getLockStatus(int tuner_id) {
        Log.d(TAG,"getLockStatus nor work now!!");
        int ret = 0;
        return ret;
    }
    public int getBER(int tuner_id){
        Log.d(TAG,"getBER nor work now!!");
        int ret = 0;
        return ret;
    }
    public int getSNR(int tuner_id) {
        Log.d(TAG,"getSNR nor work now!!");
        int ret = 0;
        return ret;

    }

    //ethan 20171201 modify TuneFrontEnd
    public int TuneFrontEnd(TVTunerParams tvTunerParams) {
        Log.d(TAG,"TuneFrontEnd nor work now!!");
        return 0;
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
}
