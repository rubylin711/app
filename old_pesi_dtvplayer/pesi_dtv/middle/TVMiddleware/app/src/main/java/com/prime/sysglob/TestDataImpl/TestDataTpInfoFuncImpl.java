package com.prime.sysglob.TestDataImpl;


import android.content.Context;
import android.util.Log;
import com.prime.TestData.TestData;
import com.prime.sysdata.TpInfo;
import com.prime.sysglob.TpInfoFunc;
import com.prime.tvclient.TestDataTVClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by johnny_shih on 2017/11/21.
 */

public class TestDataTpInfoFuncImpl implements TpInfoFunc {
    private static final String TAG="TestDataTpInfoFuncImpl";

    private Context context;

    private TestData testData = null;

    public TestDataTpInfoFuncImpl(Context context) {
        this.context = context;
        //this.testData = new TestData( TpInfo.DVBC );
        this.testData = TestDataTVClient.TestData;
    }

    @Override
    public List<TpInfo> GetTpInfoList(int tuner_type)
    {
        return query(tuner_type);
    }

    @Override
    public List<TpInfo> GetTpInfoListBySatId(int satId)
    {
        return query(satId, TpInfo.DVBS);
    }

    @Override
    public TpInfo GetTpInfo(int tp_id)
    {
        return queryOne(tp_id);
    }

    @Override
    public void Save(TpInfo pTp) {
        TpInfo tmp = queryOne(pTp.getTpId());
        if(tmp == null){
            add(pTp);
        }
        else{
            update(pTp);
        }
    }

    @Override
    public void Save(List<TpInfo> pTps) {
        update(pTps);
    }

    @Override
    public void Delete(int tpId) {
        remove(tpId);
    }

    @Override
    public void Add(TpInfo pTp) {
        add(pTp);
    }

    @Override
    public void Update(TpInfo pTp) {
        update(pTp);
    }

    private void add(TpInfo pTp) {
        TpInfo tpInfo = new TpInfo(pTp.getTunerType());
        tpInfo.setTpId(pTp.getTpId());
        tpInfo.setTunerType(pTp.getTunerType());
        tpInfo.setNetwork_id(pTp.getNetwork_id());
        tpInfo.setTransport_id(pTp.getTransport_id());
        tpInfo.setTuner_id(pTp.getTuner_id());
        tpInfo.setOrignal_network_id(pTp.getOrignal_network_id());
        tpInfo.setSatId(pTp.getSatId());

        switch (pTp.getTunerType()){
            case TpInfo.DVBT:
            case TpInfo.ISDBT:
            {
                tpInfo.TerrTp.setChannel(pTp.TerrTp.getChannel());
                tpInfo.TerrTp.setFreq(pTp.TerrTp.getFreq());
                tpInfo.TerrTp.setBand(pTp.TerrTp.getBand());
                tpInfo.TerrTp.setOtherData(pTp.TerrTp.getOtherData());
            }break;
            case TpInfo.DVBS:{
                tpInfo.SatTp.setFreq(pTp.SatTp.getFreq());
                tpInfo.SatTp.setSymbol(pTp.SatTp.getSymbol());
                tpInfo.SatTp.setPolar(pTp.SatTp.getPolar());
                tpInfo.SatTp.setOtherData(pTp.SatTp.getOtherData());
            }break;
            case TpInfo.DVBC:{
                tpInfo.CableTp.setChannel(pTp.CableTp.getChannel());
                tpInfo.CableTp.setFreq(pTp.CableTp.getFreq());
                tpInfo.CableTp.setSymbol(pTp.CableTp.getSymbol());
                tpInfo.CableTp.setQam(pTp.CableTp.getQam());
            }break;
            default:
                Log.e(TAG, "add: Unknow Tuner Type ["+pTp.getTunerType()+"]");
                return;
        }

        Log.d(TAG, "add:  ");
        testData.GetTestDatTpInfoList().add(tpInfo);
    }

    private void update(TpInfo pTp) {
        int count = 0;
        TpInfo tpInfo = new TpInfo(pTp.getTunerType());
        tpInfo.setTpId(pTp.getTpId());
        tpInfo.setTunerType(pTp.getTunerType());
        tpInfo.setNetwork_id(pTp.getNetwork_id());
        tpInfo.setTransport_id(pTp.getTransport_id());
        tpInfo.setTuner_id(pTp.getTuner_id());
        tpInfo.setOrignal_network_id(pTp.getOrignal_network_id());
        tpInfo.setSatId(pTp.getSatId());

        switch (pTp.getTunerType()){
            case TpInfo.DVBT:
            case TpInfo.ISDBT:
            {
                tpInfo.TerrTp.setChannel(pTp.TerrTp.getChannel());
                tpInfo.TerrTp.setFreq(pTp.TerrTp.getFreq());
                tpInfo.TerrTp.setBand(pTp.TerrTp.getBand());
                tpInfo.TerrTp.setOtherData(pTp.TerrTp.getOtherData());
            }break;
            case TpInfo.DVBS:{
                tpInfo.SatTp.setFreq(pTp.SatTp.getFreq());
                tpInfo.SatTp.setSymbol(pTp.SatTp.getSymbol());
                tpInfo.SatTp.setPolar(pTp.SatTp.getPolar());
                tpInfo.SatTp.setOtherData(pTp.SatTp.getOtherData());
            }break;
            case TpInfo.DVBC:{
                tpInfo.CableTp.setChannel(pTp.CableTp.getChannel());
                tpInfo.CableTp.setFreq(pTp.CableTp.getFreq());
                tpInfo.CableTp.setSymbol(pTp.CableTp.getSymbol());
                tpInfo.CableTp.setQam(pTp.CableTp.getQam());
            }break;
            default:
                Log.e(TAG, "add: Unknow Tuner Type ["+pTp.getTunerType()+"]");
                return;
        }

        Log.d(TAG, "update: ");
        List<TpInfo> tpInfoList = testData.GetTestDatTpInfoList();
        for ( int i = 0 ; i < tpInfoList.size() ; i++)
        {
            if (tpInfo.getTpId() == tpInfoList.get(i).getTpId())
            {
                tpInfoList.set(i, tpInfo);
                count++;
            }
        }
    }

    private void update(List<TpInfo> pTps){
        Log.d(TAG, "update: List");
        if(pTps.size() > 0) {
            testData.GetTestDatTpInfoList().clear();
            for (int i = 0; i < pTps.size(); i++) {
                TpInfo pTp = pTps.get(i);
                add(pTp);
            }
        }
    }

    private void remove(int tpId) {
        int count = 0;
        List<TpInfo> tpInfoList = testData.GetTestDatTpInfoList();
        for ( int i = 0 ; i < tpInfoList.size() ; i++ )
        {
            if ( tpInfoList.get(i).getTpId() == tpId )
            {
                Log.d(TAG, "remove : where = "+ tpInfoList.get(i).getTpId() + "=" + tpId);
                tpInfoList.remove(i);
                i--;
                count++;
            }
        }

    }

    private TpInfo queryOne(int tpId){

        List<TpInfo> tpInfoList = testData.GetTestDatTpInfoList();
        if(tpInfoList == null || tpInfoList.isEmpty()) {
            return null;
        }

        for ( int i = 0 ; i < tpInfoList.size() ; i++ )
        {
            TpInfo curTpInfo = tpInfoList.get(i);
            if ( curTpInfo.getTpId() == tpId )
            {
                int tuner_type = curTpInfo.getTunerType();
                TpInfo pTp;
                pTp = parseCursor( curTpInfo, tuner_type );

                return pTp;
            }
        }

        return null;
    }

    private List<TpInfo> query(int satId, int tuner_type){

        List<TpInfo> tpInfoList = testData.GetTestDatTpInfoList();
        if(tpInfoList == null || tpInfoList.isEmpty()) {
            return null;
        }

        List<TpInfo> pTps = new ArrayList<>();
        TpInfo pTp;
        for ( int i = 0 ; i < tpInfoList.size() ; i++ )
        {
            TpInfo curTpInfo = tpInfoList.get(i);
            if ( curTpInfo.getSatId() == satId && curTpInfo.getTunerType() == tuner_type )
            {
                pTp = parseCursor( curTpInfo, tuner_type );
                pTps.add(pTp);
            }
        }

        if ( pTps.isEmpty() )
        {
            return null;
        }
        else
        {
            return pTps;
        }
    }

    private List<TpInfo> query(int tuner_type){
        List<TpInfo> tpInfoList = testData.GetTestDatTpInfoList();
        if(tpInfoList == null || tpInfoList.isEmpty()) {
            return null;
        }

        List<TpInfo> pTps = new ArrayList<>();
        TpInfo pTp;
        for ( int i = 0 ; i < tpInfoList.size() ; i++ )
        {
            TpInfo curTpInfo = tpInfoList.get(i);
            if ( curTpInfo.getTunerType() == tuner_type )
            {
                pTp = parseCursor( curTpInfo, tuner_type );
                pTps.add( pTp );
            }
        }

        if ( pTps.isEmpty() )
        {
            return null;
        }
        else
        {
            return pTps;
        }
    }

    private TpInfo parseCursor(TpInfo tpInfo, int tunerType){
        TpInfo pTp = new TpInfo(tunerType);
        pTp.setTpId(tpInfo.getTpId());
        pTp.setNetwork_id(tpInfo.getNetwork_id());
        pTp.setSatId(tpInfo.getSatId());
        pTp.setTuner_id(tpInfo.getTuner_id());
        pTp.setOrignal_network_id(tpInfo.getOrignal_network_id());
        pTp.setTransport_id(tpInfo.getTransport_id());

        switch (tunerType){
            case TpInfo.DVBT:
            case TpInfo.ISDBT:
            {
                if(pTp.TerrTp != null) {
                    pTp.TerrTp.setChannel(tpInfo.TerrTp.getChannel());
                    pTp.TerrTp.setBand(tpInfo.TerrTp.getBand());
                    pTp.TerrTp.setFreq(tpInfo.TerrTp.getFreq());
                    pTp.TerrTp.setOtherData(tpInfo.TerrTp.getOtherData());
                }
            }break;
            case TpInfo.DVBS:{
                if(pTp.SatTp != null) {
                    pTp.SatTp.setFreq(tpInfo.SatTp.getFreq());
                    pTp.SatTp.setSymbol(tpInfo.SatTp.getSymbol());
                    pTp.SatTp.setPolar(tpInfo.SatTp.getPolar());
                    pTp.SatTp.setOtherData(tpInfo.SatTp.getOtherData());
                }
            }break;
            case TpInfo.DVBC:{
                if(pTp.CableTp != null){
                    pTp.CableTp.setChannel(tpInfo.CableTp.getChannel());
                    pTp.CableTp.setFreq(tpInfo.CableTp.getFreq());
                    pTp.CableTp.setSymbol(tpInfo.CableTp.getSymbol());
                    pTp.CableTp.setQam(tpInfo.CableTp.getQam());
                    pTp.CableTp.setOtherData(tpInfo.CableTp.getOtherData());
                }
            }break;
        }

        return pTp;
    }

}