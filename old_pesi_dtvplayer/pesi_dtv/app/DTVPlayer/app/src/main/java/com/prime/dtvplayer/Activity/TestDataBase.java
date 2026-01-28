package com.prime.dtvplayer.Activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.dolphin.dtv.CallbackService;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Service.DataManager.DataManager;
import com.prime.dtvplayer.Service.Database.DVBContentProvider;
import com.prime.dtvplayer.Service.Database.NetStreamDatabaseTable.NetProgramContentObserver;
import com.prime.dtvplayer.Service.Database.NetStreamDatabaseTable.NetProgramDatabaseTable;
import com.prime.dtvplayer.Sysdata.NetProgramInfo;
import com.prime.dtvplayer.Sysdata.ProgramInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;
import com.prime.dtvplayer.Sysdata.TeletextInfo;
import com.prime.dtvplayer.utils.TVMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;



//TEST for Save and Get from DataBase
//Search Flow :
//1. search DVB prgoram List from Service
//2. Add Youtube and Vod Program to New Program List (complete => modify ScanResultSetChannel() func)
//3. Update TotalChannelList
//4. Save New Program List to Database
//5. Check New Program List Database
//6. Check Reset/First Open DTVPlayer
//7. Check Open DTVPlayer with Program Exist
public class TestDataBase extends DTVActivity {
    private static final String TAG="TestDataBase";
    Context mContext;
    ListView listView;
    DataManager mDataManager;
    List<ProgramInfo> mProgramInfoList;
    List<SimpleChannel> mSimpleChannelList;
    DataManager.NetProgramDatabase mNetProgramDatabase;
    Handler mHandler;
    ContentObserver mTestObserver;
    ArrayAdapter mAdapter;
    List<String> mStrList = new ArrayList<>();
    int count = 0;

    private final String news_url = "<iframe width=\"1920\" height=\"1080\" src=\"https://www.youtube.com/embed/lu_BJKxqGnk?&autoplay=1\" frameborder=\"0\" allow=\"autoplay; \" allowfullscreen></iframe>";
    private final String vod_url = "http://192.168.11.118/test/data/test_wen.m3u8";
    final Runnable TestUpdateRunnable = new Runnable() {
        @Override
        public void run() {

            ArrayList<List<SimpleChannel>> allList = GetProgramManagerTotalChannelList();
            List<SimpleChannel> netList = new ArrayList<>();
            netList.add(allList.get(0).get(allList.get(0).size()-2));
            netList.add(allList.get(0).get(allList.get(0).size()-1));
            if(netList.get(0).getUrl().equals(news_url)) {
                netList.get(0).setUrl(vod_url);
                netList.get(1).setUrl(news_url);
                Log.e(TAG, "if run: <<<" + netList.get(0).getUrl() + ">>>");
            }
            else {
                netList.get(0).setUrl(news_url);
                netList.get(1).setUrl(vod_url);
                Log.e(TAG, "else run: <<<" + netList.get(0).getUrl() + ">>>");
            }
//            Log.e(TAG, "run: <<<" + netList.get(0).getUrl() + ">>>");
            mNetProgramDatabase.SaveNetProgramList(mContext,netList);
            //mNetProgramDatabase.UpdateNetProgramInfo(mContext,netList.get(0));
            mHandler.postDelayed(TestUpdateRunnable, 15000);
        }
    };

    private void subBroadCast()
    {
        IntentFilter updateNetProgramDataBaseFilter = new IntentFilter("com.prime.netprogram.database.update");
        BroadcastReceiver UpdateNetProgramReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.d(TAG, "onReceive:  " + intent.toString());
                // do something to update UI
                Log.e(TAG, "onReceive UpdateCurPlayChannelList Net first");
                List<NetProgramInfo> netQueryList = new ArrayList<>();
                netQueryList = mNetProgramDatabase.GetNetProgramList(mContext);

                //List<String> strList = new ArrayList<>();
                mStrList.clear();
                for(int i = 0 ; i < netQueryList.size() ; i++) {
                    String str = new String();
                    str = "["+count+"] " +"netProrgram"+i+"\nname [" + netQueryList.get(i).getChannelName()
                            + "] channelId = [" + netQueryList.get(i).getChannelId()
                            + "] ChannelNum = [" + netQueryList.get(i).getChannelNum()
                            + "] groupType = [" + netQueryList.get(i).getGroupType()
                            + "] PlayStreamType = [" + netQueryList.get(i).getPlayStreamType()
                            + "] skip = [" + netQueryList.get(i).getSkip()
                            + "] lock = [" + netQueryList.get(i).getUserLock()
                            + "]\nVideo_url = [" + netQueryList.get(i).getVideoUrl()
                            + "]";
                    count++;
                    if(count >= 10)
                        count = 0;
                    mStrList.add(str);
                }

                mAdapter.notifyDataSetChanged();
            }
        };

        registerReceiver(UpdateNetProgramReceiver, updateNetProgramDataBaseFilter, "android.permission.NETPROGRAM_BROADCAST", null);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_database);

        mContext = this;
        mDataManager = DataManager.getDataManager();
        mNetProgramDatabase = mDataManager.GetNetProgramDatabase();
        listView = (ListView) findViewById(R.id.listView);
        mHandler = new Handler();
        mHandler.post(TestUpdateRunnable);
        subBroadCast();

        Init();
    }


    private void Init()
    {
        ArrayList<List<SimpleChannel>> allList = GetProgramManagerTotalChannelList();
        List<SimpleChannel> netList = new ArrayList<>();
        netList.add(allList.get(0).get(allList.get(0).size()-2));
        netList.add(allList.get(0).get(allList.get(0).size()-1));
        mNetProgramDatabase.SaveNetProgramList(mContext,netList); //Clear and Add
//        mNetProgramDatabase.AddNetProgramList(this,netList);//Add
//        mNetProgramDatabase.UpdateNetProgramList(this,netList);//Update
//        mNetProgramDatabase.UpdateNetProgramInfo(this,netList.get(1));

        List<NetProgramInfo> netQueryList = new ArrayList<>();
        netQueryList = mNetProgramDatabase.GetNetProgramList(mContext);


        for(int i = 0 ; i < netQueryList.size() ; i++) {
            String str = new String();
//            str = "netProrgram"+i+"\nname [" + netQueryList.get(i).getChannelName()
//                    + "] channelId = [" + netQueryList.get(i).getChannelId()
//                    + "] ChannelNum = [" + netQueryList.get(i).getChannelNum()
//                    + "] groupType = [" + netQueryList.get(i).getGroupType()
//                    + "] PlayStreamType = [" + netQueryList.get(i).getPlayStreamType()
//                    + "] skip = [" + netQueryList.get(i).getSkip()
//                    + "] lock = [" + netQueryList.get(i).getUserLock()
//                    + "]\nVideo_url = [" + netQueryList.get(i).getVideoUrl();

            mStrList.add(str);
        }

        ArrayList<List<SimpleChannel>> TotalChannelList = new ArrayList<List<SimpleChannel>>();
        Log.e(TAG, "UpdateCurPlayChannelList Net first");
        if (TotalChannelList.size() != 0)
            TotalChannelList.clear();

        DataManager mDataManager = DataManager.getDataManager();
        DataManager.NetProgramDatabase mNetProgramDatabase = mDataManager.GetNetProgramDatabase();
        List<SimpleChannel> simpleChannelList = mNetProgramDatabase.GetSimpleChannelList(mContext);
        List<SimpleChannel> netSimpleChannelList = mNetProgramDatabase.GetNetSimpleChannelList(mContext);
        for(int j = 0 ; j < netSimpleChannelList.size() ; j++)
            simpleChannelList.add(netSimpleChannelList.get(j));
        TotalChannelList.add(simpleChannelList);

//        for(int i = 0 ; i < TotalChannelList.size() ; i++)
//            for(int j = 0 ; j < TotalChannelList.get(i).size() ; j++)
//                Log.d(TAG, "exce UpdateCurPlayChannelList: i = ["+i+"] "
//                        + "= ["+ TotalChannelList.get(i).get(j).getChannelName()
//                        + "] = ["+ TotalChannelList.get(i).get(j).getChannelId()
//                        + "] = ["+ TotalChannelList.get(i).get(j).getPlayStreamType()
//                        +"]");

        mAdapter = new ArrayAdapter(mContext, android.R.layout.activity_list_item, android.R.id.text1, mStrList);
        listView.setAdapter(mAdapter);

        //RegisterObserver();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: ");
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }
}
