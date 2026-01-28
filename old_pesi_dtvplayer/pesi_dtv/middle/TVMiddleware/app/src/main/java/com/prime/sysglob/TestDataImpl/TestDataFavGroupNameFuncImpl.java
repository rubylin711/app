package com.prime.sysglob.TestDataImpl;

import android.content.Context;

import com.prime.TestData.TestData;
import com.prime.sysdata.FavGroupName;
import com.prime.sysdata.ProgramInfo;
import com.prime.sysglob.FavGroupNameFunc;
import com.prime.tvclient.TestDataTVClient;

import java.util.List;

/**
 * Created by Eric on 2017/11/23.
 */

public class TestDataFavGroupNameFuncImpl implements FavGroupNameFunc{
    private static final String TAG="TestDataFavGroupNameFuncImpl";
    private Context context;
    private final int DVBT = 1;
    private final int DVBS = 2;
    private final int DVBC = 3;

    TestData testData = null;

    public TestDataFavGroupNameFuncImpl(Context context) {
        this.context = context;
        this.testData = TestDataTVClient.TestData;
    }

    @Override
    public String GetFavGroupName(int favMode)
    {
        if(favMode == -1 || favMode >= ProgramInfo.ALL_TV_RADIO_TYPE_MAX)//eric lin 20171214 fix invalid favMode for FavGroup
            return null;
        List<FavGroupName> allGroupName = testData.GetTestDataFavGroupNameArray();
        if(allGroupName != null) {
            for (int i = 0; i < allGroupName.size(); i++) {
            if ( allGroupName.get(i).getGroupType() == favMode)
                return allGroupName.get(i).getGroupName();
        }
        }
        return null;
    }

    @Override
    public void Save(int favMode, String name) {
        List<FavGroupName> allGroupName = testData.GetTestDataFavGroupNameArray();

        if(favMode == -1 || favMode >= ProgramInfo.ALL_TV_RADIO_TYPE_MAX)//eric lin 20171214 fix invalid favMode for FavGroup
            return;
        if(allGroupName != null) {
            for (int i = 0; i < allGroupName.size(); i++) {
            if ( allGroupName.get(i).getGroupType() == favMode){
                allGroupName.get(i).setGroupName(name);
                return;
            }
        }
        }


        allGroupName.add(new FavGroupName(favMode, name));
        /*
        FavGroupName favGroupName = query(favMode);
        if(favGroupName == null) {
            favGroupName = new FavGroupName();
            favGroupName.setFavMode(favMode);
            favGroupName.setFavGroupName(name);
            add(favGroupName);
        }
        else {
            favGroupName.setFavMode(favMode);
            favGroupName.setFavGroupName(name);
            update(favGroupName);
        }
        */
    }

    /*
    private FavGroupName query(int favMode) {

    }

    private void add(FavGroupName favGroupName) {
        ContentValues Value = new ContentValues();
        Value.put(DBFavGroupName.FAV_MODE, favGroupName.getFavMode());
        Value.put(DBFavGroupName.FAV_GROUP_NAME, favGroupName.getFavGroupName());
        Log.d(TAG, "add "+ favGroupName.ToString());
        context.getContentResolver().insert(DBFavGroupName.CONTENT_URI, Value);
    }

    private void update(FavGroupName favGroupName) {
        int count;
        String where = DBFavGroupName.FAV_MODE + " = " + favGroupName.getFavMode();
        ContentValues Value = new ContentValues();
        Value.put(DBFavGroupName.FAV_MODE, favGroupName.getFavMode());
        Value.put(DBFavGroupName.FAV_GROUP_NAME, favGroupName.getFavGroupName());

        Log.d(TAG, "update " + favGroupName.ToString());
        count = context.getContentResolver().update(DBFavGroupName.CONTENT_URI, Value, where, null);
    }

    private void remove(int favMode) {
        int count;
        String where = DBFavGroupName.FAV_MODE + " = " + favMode;
        Log.d(TAG, "remove favMode "+ favMode);
        count = context.getContentResolver().delete(DBFavGroupName.CONTENT_URI, where, null);
    }

    private FavGroupName query(int favMode) {
        FavGroupName favGroupName = null;
        Cursor cursor = context.getContentResolver().query(DBFavGroupName.CONTENT_URI, null, null, null, DBFavGroupName.FAV_MODE);
        if(cursor != null) {
            if (cursor.moveToFirst())
                favGroupName = ParseCursor(cursor);
            cursor.close();
            return favGroupName;
        }
        return null;
    }


    private FavGroupName ParseCursor(Cursor cursor) {
        FavGroupName favGroupName = new FavGroupName();
        favGroupName.setFavMode(dbUtils.GetIntFromTable(cursor, DBFavGroupName.FAV_MODE));
        favGroupName.setFavGroupName(dbUtils.GetStringFromTable(cursor, DBFavGroupName.FAV_GROUP_NAME));
        Log.i(TAG, "ParseCursor FavInfo : "+favGroupName.ToString());
        return favGroupName;
    }
    */
}
