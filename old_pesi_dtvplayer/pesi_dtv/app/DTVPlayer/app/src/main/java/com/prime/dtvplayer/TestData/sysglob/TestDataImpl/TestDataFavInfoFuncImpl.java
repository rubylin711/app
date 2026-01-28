package com.prime.dtvplayer.TestData.sysglob.TestDataImpl;

import android.content.Context;
import android.util.Log;

import com.prime.dtvplayer.TestData.TestData.TestData;
import com.prime.dtvplayer.Sysdata.FavInfo;
import com.prime.dtvplayer.TestData.sysglob.FavInfoFunc;
import com.prime.dtvplayer.TestData.tvclient.TestDataTVClient;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by Eric on 2017/11/23.
 */

public class TestDataFavInfoFuncImpl implements FavInfoFunc{
    private static final String TAG = "TestDataFavInfoFuncImpl";
    private Context context;
    private final int DVBT = 1;
    private final int DVBS = 2;
    private final int DVBC = 3;

    TestData testData = null;

    public TestDataFavInfoFuncImpl(Context context) {
        this.context = context;
        this.testData = TestDataTVClient.TestData;
    }

    @Override
    public List<FavInfo> GetFavInfoList(int favMode) {
        List<FavInfo> allFavInfo = testData.GetTestDataFavInfoArray(favMode);
        List<FavInfo> resultFavInfoList = new ArrayList<>();

        if(allFavInfo != null) {
            for (int i = 0; i < allFavInfo.size(); i++) {
                if (allFavInfo.get(i).getFavMode() == favMode)
                    resultFavInfoList.add(allFavInfo.get(i));
            }
            return resultFavInfoList;
        }
        else{
            Log.d(TAG, "allFavInfo is null");
            return null;
        }
    }


    @Override
    public FavInfo GetFavInfo(int favMode, int favNum) {
        List<FavInfo> allFavInfo = testData.GetTestDataFavInfoArray(favMode);
        FavInfo resultFavInfo = new FavInfo();

        if(allFavInfo != null) {
            for (int i = 0; i < allFavInfo.size(); i++) {
            if ( allFavInfo.get(i).getFavMode() == favMode
                    && allFavInfo.get(i).getFavNum() == favNum) {
                Log.d(TAG, "GetFavInfo:　get");
                return allFavInfo.get(i);
                }
            }
        }
        Log.d(TAG, "GetFavInfo:　not get");
        return null;
    }

    @Override
    public void Save(FavInfo favInfo) {
        int favMode = favInfo.getFavMode();
        List<FavInfo> allFavInfo = testData.GetTestDataFavInfoArray(favMode);
        if(allFavInfo == null)//related fav info list is empty
            allFavInfo = testData.GetTestDataFavInfoArrayForSave(favMode);
        else {
            for (int i = 0; i <allFavInfo.size(); i++) {
                if(allFavInfo.get(i).getChannelId() == favInfo.getChannelId())
                    return; //not save duplicate favInfo
            }
        }


        allFavInfo.add(new FavInfo(favInfo.getFavNum(),
                favInfo.getChannelId(),
                favInfo.getFavMode()));
    }

    @Override
    public void Save(List<FavInfo> favInfo) {
        int favMode = favInfo.get(0).getFavMode();
        List<FavInfo> allFavInfo;

        testData.TestDataFavInfoListSave(favMode, favInfo);
        allFavInfo = testData.GetTestDataFavInfoArray(favMode);
        if(allFavInfo != null)
            Log.d(TAG, "Save:　allFavInfo not null, size="+ allFavInfo.size());
        else
            Log.d(TAG, "Save:　allFavInfo is null");

    }

    @Override
    public void Delete(int favMode, long channelId) {
        List<FavInfo> allFavInfo = testData.GetTestDataFavInfoArray(favMode);
        int favNum=0;
        if(allFavInfo != null){
        Log.d(TAG, "Delete: allFavInfo.size()="+allFavInfo.size());
        for ( int i = 0 ; i < allFavInfo.size() ; i++ )
        {
            if ( allFavInfo.get(i).getFavMode() == favMode
                    && allFavInfo.get(i).getChannelId() == channelId) {
                Log.d(TAG, "Delete: find FavInfo"+"favMode="+favMode+"channelId="+ channelId);
                favNum = allFavInfo.get(i).getFavNum();
                allFavInfo.remove(i);
                for ( int j = 0 ; j < allFavInfo.size() ; j++ )
                {
                    if ( allFavInfo.get(j).getFavMode() == favMode
                            && allFavInfo.get(j).getFavNum() > favNum) {
                        allFavInfo.get(j).setFavNum(allFavInfo.get(j).getFavNum()-1);
                    }
                }
            }
        }
    }
    }

    @Override
    public void DeleteAll(int favMode) {
        List<FavInfo> allFavInfo = testData.GetTestDataFavInfoArray(favMode);
        if(allFavInfo != null)//eric lin 20171206 add condition
        allFavInfo.clear();
    }

    /*
    private void add(FavInfo favInfo) {
        ContentValues Value = new ContentValues();
        Value.put(DBFavInfo.INDEX, favInfo.getIndex());
        Value.put(DBFavInfo.SERVICE_ID, favInfo.getChannelId());
        Value.put(DBFavInfo.ORIGINAL_NETWORK_ID, favInfo.getOriginalNetworkId());
        Value.put(DBFavInfo.TRANSPORT_STREAM_ID, favInfo.getTransportStreamId());
        Value.put(DBFavInfo.FAV_MODE, favInfo.getFavMode());

        Log.d(TAG, "add "+ favInfo.ToString());
        context.getContentResolver().insert(DBFavInfo.CONTENT_URI, Value);
    }

    private void update(FavInfo favInfo) {
        int count;
        String where = DBFavInfo.INDEX + " = " + favInfo.getIndex();
        ContentValues Value = new ContentValues();
        Value.put(DBFavInfo.INDEX, favInfo.getIndex());
        Value.put(DBFavInfo.SERVICE_ID, favInfo.getChannelId());
        Value.put(DBFavInfo.ORIGINAL_NETWORK_ID, favInfo.getOriginalNetworkId());
        Value.put(DBFavInfo.TRANSPORT_STREAM_ID, favInfo.getTransportStreamId());
        Value.put(DBFavInfo.FAV_MODE, favInfo.getFavMode());

        Log.d(TAG, "update " + favInfo.ToString());
        count = context.getContentResolver().update(DBFavInfo.CONTENT_URI, Value, where, null);
    }

    private void remove(int index) {
        int count;
        String where = DBFavInfo.INDEX + " = " + index;
        Log.d(TAG, "remove favIndex "+ index);
        count = context.getContentResolver().delete(DBFavInfo.CONTENT_URI, where, null);
    }

    private List<FavInfo> query(int favMode) {
        String selection = DBFavInfo.FAV_MODE + " = " + favMode;
        Cursor cursor = context.getContentResolver().query(DBFavInfo.CONTENT_URI, null, selection, null, DBFavInfo.INDEX);
        List<FavInfo> FavInfos = new ArrayList<FavInfo>();
        FavInfo FavInfo = null;
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    FavInfo = ParseCursor(cursor);
                    FavInfos.add(FavInfo);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return FavInfos;
        }
        return null;
    }

    private FavInfo query(int favMode,int index) {
        FavInfo favInfo = null;
        String selection = DBFavInfo.FAV_MODE + " = " + favMode + " and " + DBFavInfo.INDEX + " = " + index;
        Cursor cursor = context.getContentResolver().query(DBFavInfo.CONTENT_URI, null, selection, null, DBFavInfo.INDEX);
        if(cursor != null) {
            if (cursor.moveToFirst())
                favInfo = ParseCursor(cursor);
            cursor.close();
            return favInfo;
        }
        return null;
    }

    private FavInfo ParseCursor(Cursor cursor){
        FavInfo favInfo = new FavInfo();
        favInfo.setIndex(dbUtils.GetIntFromTable(cursor, DBFavInfo.INDEX));
        favInfo.setChannelId(dbUtils.GetIntFromTable(cursor, DBFavInfo.SERVICE_ID));
        favInfo.setOriginalNetworkId(dbUtils.GetIntFromTable(cursor, DBFavInfo.ORIGINAL_NETWORK_ID));
        favInfo.seTransportStreamId(dbUtils.GetIntFromTable(cursor, DBFavInfo.TRANSPORT_STREAM_ID));
        favInfo.setFavMode(dbUtils.GetIntFromTable(cursor, DBFavInfo.FAV_MODE));

        Log.i(TAG, "ParseCursor FavInfo : "+favInfo.ToString());
        return favInfo;
    }
    */
}
