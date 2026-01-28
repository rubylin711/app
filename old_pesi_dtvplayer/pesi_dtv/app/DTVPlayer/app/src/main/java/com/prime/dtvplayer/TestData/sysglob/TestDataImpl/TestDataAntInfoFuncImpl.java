/*
package com.prime.sysglob.TestDataImpl;


import android.content.Context;
import android.util.Log;

import com.prime.TestData.TestData;
import com.prime.sysdata.AntInfo;
import com.prime.sysdata.TpInfo;
import com.prime.sysglob.AntInfoFunc;
import com.prime.tvclient.TestDataTVClient;

import java.util.ArrayList;
import java.util.List;

*/
/**
 * Created by johnny_shih on 2017/11/23.
 *//*


public class TestDataAntInfoFuncImpl implements AntInfoFunc {

    private static final String TAG = "TestDataAntInfoFuncImpl";
    private Context context;

    private TestData testData = null;

    public TestDataAntInfoFuncImpl(Context context)
    {
        this.context = context;
        //this.testData = new TestData( TpInfo.DVBC );
        this.testData = TestDataTVClient.TestData;
    }


    @Override
    public List<AntInfo> GetAntInfoList()
    {
        return query();
    }

    @Override
    public AntInfo GetAntInfo(int antId)
    {
        return query(antId);
    }

    @Override
    public void Save(AntInfo pant)
    {
        AntInfo tmp = query(pant.getAntId());
        if(tmp != null)
        {
            update(pant);
        }
        else
        {
            add(pant);
        }
    }

    @Override
    public void Save(List<AntInfo> pants)
    {
        update(pants);
    }

    @Override
    public void Delete(int antId)
    {
        remove(antId);
    }

    private void add(AntInfo pant) {
        AntInfo antInfo = new AntInfo();
        antInfo.setAntId(pant.getAntId());
        antInfo.setLnbType(pant.getLnbType());
        antInfo.setLnb1(pant.getLnb1());
        antInfo.setLnb2(pant.getLnb2());
        antInfo.setSatId(pant.getSatId());
        antInfo.setDiseqcType(pant.getDiseqcType());
        antInfo.setDiseqcUse(pant.getDiseqcUse());
        antInfo.setDiseqc(pant.getDiseqc());
        antInfo.setTone22kUse(pant.getTone22kUse());
        antInfo.setTone22k(pant.getTone22k());
        antInfo.setV012Use(pant.getV012Use());
        antInfo.setV012(pant.getV012());
        antInfo.setV1418Use(pant.getV1418Use());
        antInfo.setV1418(pant.getV1418());
        antInfo.setCku(pant.getCku());

        Log.d(TAG, "add "+ pant.ToString());
        testData.GetTestDatAntInfoList().add(antInfo);
    }


    private void update(AntInfo pant) {
        int count = 0;

        AntInfo antInfo = new AntInfo();
        antInfo.setAntId(pant.getAntId());
        antInfo.setLnbType(pant.getLnbType());
        antInfo.setLnb1(pant.getLnb1());
        antInfo.setLnb2(pant.getLnb2());
        antInfo.setSatId(pant.getSatId());
        antInfo.setDiseqcType(pant.getDiseqcType());
        antInfo.setDiseqcUse(pant.getDiseqcUse());
        antInfo.setDiseqc(pant.getDiseqc());
        antInfo.setTone22kUse(pant.getTone22kUse());
        antInfo.setTone22k(pant.getTone22k());
        antInfo.setV012Use(pant.getV012Use());
        antInfo.setV012(pant.getV012());
        antInfo.setV1418Use(pant.getV1418Use());
        antInfo.setV1418(pant.getV1418());
        antInfo.setCku(pant.getCku());

        Log.d(TAG, "update "+ pant.ToString());
        List<AntInfo> antInfoList = testData.GetTestDatAntInfoList();
        for ( int i = 0 ; i < antInfoList.size() ; i++)
        {
            if (antInfo.getAntId() == antInfoList.get(i).getAntId())
            {
                antInfoList.set(i, antInfo);
                count++;
            }
        }
    }


    private void update(List<AntInfo> pants) {
        Log.d(TAG, "update : All list");
        if(pants.size()>0) {
            testData.GetTestDatAntInfoList().clear();
            for(int i = 0; i < pants.size() ; i++){
                AntInfo tmp = pants.get(i);
                add(tmp);
            }
        }
    }


    private void remove(int antId) {
        int count = 0;
        List<AntInfo> antInfoList = testData.GetTestDatAntInfoList();
        for ( int i = 0 ; i < antInfoList.size() ; i++ )
        {
            if ( antInfoList.get(i).getAntId() == antId )
            {
                Log.d(TAG, "remove : where = "+ antInfoList.get(i).getAntId() + "=" + antId);
                antInfoList.remove(i);
                i--;
                count++;
            }
        }
    }

    private AntInfo query(int antId){
        Log.d(TAG, "query");
        List<AntInfo> antInfoList = testData.GetTestDatAntInfoList();
        if(antInfoList == null || antInfoList.isEmpty()) {
            return null;
        }

        for ( int i = 0 ; i < antInfoList.size() ; i++ )
        {
            AntInfo curAntInfo = antInfoList.get(i);
            if ( curAntInfo.getAntId() == antId )
            {
                return ParseCursor( curAntInfo );
            }
        }

        return null;
    }
    private List<AntInfo> query(){
        Log.d(TAG, "query : List");
        List<AntInfo> antInfoList = testData.GetTestDatAntInfoList();
        if(antInfoList == null || antInfoList.isEmpty())
        {
            return null;
        }

        List<AntInfo> AntInfos = new ArrayList<>();
        AntInfo pAntInfo;
        for ( int i = 0 ; i < antInfoList.size() ; i++ )
        {
            AntInfo curAntInfo = antInfoList.get(i);
            pAntInfo = ParseCursor( curAntInfo );
            AntInfos.add( pAntInfo );
        }

        return AntInfos;
    }
    private AntInfo ParseCursor(AntInfo antInfo){
        AntInfo pAnt = new AntInfo();
        pAnt.setAntId(antInfo.getAntId());
        pAnt.setLnbType(antInfo.getLnbType());
        pAnt.setLnb1(antInfo.getLnb1());
        pAnt.setLnb2(antInfo.getLnb2());
        pAnt.setSatId(antInfo.getSatId());
        pAnt.setDiseqcType(antInfo.getDiseqcType());
        pAnt.setDiseqcUse(antInfo.getDiseqcUse());
        pAnt.setDiseqc(antInfo.getDiseqc());
        pAnt.setTone22kUse(antInfo.getTone22kUse());
        pAnt.setTone22k(antInfo.getTone22k());
        pAnt.setV012Use(antInfo.getV012Use());
        pAnt.setV012(antInfo.getV012());
        pAnt.setV1418Use(antInfo.getV1418Use());
        pAnt.setV1418(antInfo.getV1418());
        pAnt.setCku(antInfo.getCku());
        Log.i(TAG, "pAnt : "+pAnt.ToString());
        return pAnt;
    }
}
*/
