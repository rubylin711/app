package com.prime.sysglob.TestDataImpl;


import android.content.Context;
import android.util.Log;

import com.prime.TestData.TestData;
import com.prime.sysdata.SatInfo;
import com.prime.sysdata.TpInfo;
import com.prime.sysglob.SatInfoFunc;
import com.prime.tvclient.TestDataTVClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by johnny_shih on 2017/11/23.
 */

public class TestDataSatInfoFuncImpl implements SatInfoFunc {

    private static final String TAG = "TestDataSatInfoFuncImpl";
    private Context context;

    private TestData testData = null;

    public TestDataSatInfoFuncImpl(Context context)
    {
        this.context = context;
        //this.testData = new TestData( TpInfo.DVBC );
        this.testData = TestDataTVClient.TestData;
    }

    @Override
    public List<SatInfo> GetSatinfoList() {
        return query();
    }

    @Override
    public SatInfo GetSatInfo(int satId) {
        return query(satId);
    }

    @Override
    public void Save(SatInfo pSat) {
        SatInfo tmp = query(pSat.getSatId());
        if(tmp != null)
        {
            update(pSat);
        }
        else
        {
            add(pSat);
        }
    }

    @Override
    public void Save(List<SatInfo> pSats) {
        update(pSats);
    }

    @Override
    public void Delete(int satId) {
        remove(satId);
    }

    @Override
    public void Add(SatInfo pSat) {
        add(pSat);
    }

    @Override
    public void Update(SatInfo pSat) {
        update(pSat);
    }

    private void add(SatInfo pSat) {
        SatInfo satInfo = new SatInfo();
        satInfo.setSatId(pSat.getSatId());
        satInfo.setSatName(pSat.getSatName());
        satInfo.setAngle(pSat.getAngle());
        satInfo.setLocation(pSat.getLocation());
        satInfo.setPostionIndex(pSat.getPostionIndex());
        satInfo.setTpNum(pSat.getTpNum());
        satInfo.Antenna.setLnbType(pSat.Antenna.getLnbType());
        satInfo.Antenna.setLnb1(pSat.Antenna.getLnb1());
        satInfo.Antenna.setLnb2(pSat.Antenna.getLnb2());
        satInfo.Antenna.setDiseqcType(pSat.Antenna.getDiseqcType());
        satInfo.Antenna.setDiseqcUse(pSat.Antenna.getDiseqcUse());
        satInfo.Antenna.setDiseqc(pSat.Antenna.getDiseqc());
        satInfo.Antenna.setTone22kUse(pSat.Antenna.getTone22kUse());
        satInfo.Antenna.setTone22k(pSat.Antenna.getTone22k());
        satInfo.Antenna.setV012Use(pSat.Antenna.getV012Use());
        satInfo.Antenna.setV012(pSat.Antenna.getV012());
        satInfo.Antenna.setV1418Use(pSat.Antenna.getV1418Use());
        satInfo.Antenna.setV1418(pSat.Antenna.getV1418());
        satInfo.Antenna.setCku(pSat.Antenna.getCku());
        Log.d(TAG, "add "+pSat.ToString());
        testData.GetTestDatSatInfoList().add(satInfo);
    }

    private void update(SatInfo pSat) {
        int count = 0;
        SatInfo satInfo = new SatInfo();
        satInfo.setSatId(pSat.getSatId());
        satInfo.setSatName(pSat.getSatName());
        satInfo.setAngle(pSat.getAngle());
        satInfo.setLocation(pSat.getLocation());
        satInfo.setPostionIndex(pSat.getPostionIndex());
        satInfo.setTpNum(pSat.getTpNum());
        satInfo.Antenna.setLnbType(pSat.Antenna.getLnbType());
        satInfo.Antenna.setLnb1(pSat.Antenna.getLnb1());
        satInfo.Antenna.setLnb2(pSat.Antenna.getLnb2());
        satInfo.Antenna.setDiseqcType(pSat.Antenna.getDiseqcType());
        satInfo.Antenna.setDiseqcUse(pSat.Antenna.getDiseqcUse());
        satInfo.Antenna.setDiseqc(pSat.Antenna.getDiseqc());
        satInfo.Antenna.setTone22kUse(pSat.Antenna.getTone22kUse());
        satInfo.Antenna.setTone22k(pSat.Antenna.getTone22k());
        satInfo.Antenna.setV012Use(pSat.Antenna.getV012Use());
        satInfo.Antenna.setV012(pSat.Antenna.getV012());
        satInfo.Antenna.setV1418Use(pSat.Antenna.getV1418Use());
        satInfo.Antenna.setV1418(pSat.Antenna.getV1418());
        satInfo.Antenna.setCku(pSat.Antenna.getCku());
        Log.d(TAG, "add "+pSat.ToString());
        List<SatInfo> satInfoList = testData.GetTestDatSatInfoList();
        for ( int i = 0 ; i < satInfoList.size() ; i++)
        {
            if (satInfo.getSatId() == satInfoList.get(i).getSatId())
            {
                satInfoList.set(i, satInfo);
                count++;
            }
        }
    }

    private void update(List<SatInfo> pSats){
        if(pSats.size() > 0) {
            testData.GetTestDatSatInfoList().clear();
            for(int i= 0 ; i < pSats.size() ; i++){
                SatInfo pSat = pSats.get(i);
                add(pSat);
            }
        }
    }

    private void remove(int satId) {
        int count = 0;
        List<SatInfo> satInfoList = testData.GetTestDatSatInfoList();
        for ( int i = 0 ; i < satInfoList.size() ; i++ )
        {
            if ( satInfoList.get(i).getSatId() == satId )
            {
                Log.d(TAG, "remove : where = "+ satInfoList.get(i).getSatId() + "=" + satId);
                satInfoList.remove(i);
                i--;
                count++;
            }
        }
    }

    private SatInfo query(int satId){
        Log.d(TAG, "query:  satId =  "+satId);
        List<SatInfo> satInfoList = testData.GetTestDatSatInfoList();
        if(satInfoList == null || satInfoList.isEmpty()) {
            return null;
        }

        for ( int i = 0 ; i < satInfoList.size() ; i++ )
        {
            SatInfo curSatInfo = satInfoList.get(i);
            if ( curSatInfo.getSatId() == satId )
            {
                return ParseCursor( curSatInfo );
            }
        }

        return null;
    }
    private List<SatInfo> query(){
        Log.d(TAG, "query: List");
        List<SatInfo> satInfoList = testData.GetTestDatSatInfoList();
        if(satInfoList == null || satInfoList.isEmpty())
        {
            return null;
        }

        List<SatInfo> SatInfos = new ArrayList<>();
        SatInfo pSatInfo;
        for ( int i = 0 ; i < satInfoList.size() ; i++ )
        {
            SatInfo curAntInfo = satInfoList.get(i);
            pSatInfo = ParseCursor( curAntInfo );
            SatInfos.add( pSatInfo );
        }

        return SatInfos;
    }

    private SatInfo ParseCursor(SatInfo satInfo){
        SatInfo pSat = new SatInfo();
        pSat.setSatId(satInfo.getSatId());
        pSat.setSatName(satInfo.getSatName());
        pSat.setAngle(satInfo.getAngle());
        pSat.setLocation(satInfo.getLocation());
        pSat.setPostionIndex(satInfo.getPostionIndex());
        pSat.setTpNum(satInfo.getTpNum());
        pSat.Antenna.setLnbType(satInfo.Antenna.getLnbType());
        pSat.Antenna.setLnb1(satInfo.Antenna.getLnb1());
        pSat.Antenna.setLnb2(satInfo.Antenna.getLnb2());
        pSat.Antenna.setDiseqcType(satInfo.Antenna.getDiseqcType());
        pSat.Antenna.setDiseqcUse(satInfo.Antenna.getDiseqcUse());
        pSat.Antenna.setDiseqc(satInfo.Antenna.getDiseqc());
        pSat.Antenna.setTone22kUse(satInfo.Antenna.getTone22kUse());
        pSat.Antenna.setTone22k(satInfo.Antenna.getTone22k());
        pSat.Antenna.setV012Use(satInfo.Antenna.getV012Use());
        pSat.Antenna.setV012(satInfo.Antenna.getV012());
        pSat.Antenna.setV1418Use(satInfo.Antenna.getV1418Use());
        pSat.Antenna.setV1418(satInfo.Antenna.getV1418());
        pSat.Antenna.setCku(satInfo.Antenna.getCku());
        return pSat;
    }
}
