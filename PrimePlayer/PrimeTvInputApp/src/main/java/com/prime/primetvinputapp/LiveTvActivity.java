package com.prime.primetvinputapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.media.tv.TvTrackInfo;
import android.media.tv.TvView;
import android.net.Uri;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.prime.datastructure.TIF.TIFChannelData;
import com.prime.datastructure.sysdata.ProgramInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LiveTvActivity extends AppCompatActivity {
    private final static String TAG = "LiveTvActivity";
    private Boolean DEBUG = true ;
    private TvView mTvView;
    private TextView mTextView ;

    private List<TIFChannelData> mTIFChannelDataList = new ArrayList<>();
    private TIFChannelData mCurrentTIFChannelData ;
    private ChannelChangeManager g_chChangeMgr;

    private static final int REQUEST_CODE_START_SETUP_ACTIVITY = 1;
    private int mChannelIndex = 0 ;
    private int mTotalChannels = 0 ;
    private PlayerControl mPlayControl = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_tv);
        mTvView = (TvView)findViewById(R.id.tvview);
        mTextView = (TextView)findViewById(R.id.channelData);
        g_chChangeMgr = ChannelChangeManager.get_instance(getApplicationContext());
        mPlayControl = PrimeTvInputAppApplication.getPlayerControl();
//        initTIFChannel(mTvView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init_playControl();
        runOnUiThread(()->{
            if (null == g_chChangeMgr)
                g_chChangeMgr = ChannelChangeManager.get_instance(this);
            Log.d(TAG,"g_chChangeMgr.get_cur_ch_id() = "+g_chChangeMgr.get_cur_ch_id());
            startPlayChannels(g_chChangeMgr.get_cur_ch_id());
        });

    }

    @Override
    public boolean onKeyUp( int keyCode, KeyEvent event )
    {
        if (set_channel(event))
            return false;

        if (keyCode == KeyEvent.KEYCODE_PROG_RED) {
            if (g_chChangeMgr.is_recording()) {
                g_chChangeMgr.pvr_record_stop();
            } else {
                g_chChangeMgr.pvr_record_start(g_chChangeMgr.get_cur_ch_id(), 0, 0, false);
            }
            return true;
        }

        return super.onKeyUp(keyCode,event);
    }

    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event )
    {
        if (set_channel(event))
            return false;
        return super.onKeyDown(keyCode,event);
    }

    public void init_playControl() {
        if(mTvView != null && mPlayControl != null) {
            mPlayControl.setTvView(mTvView);
        }
    }

    public boolean set_channel(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        if (KeyEvent.KEYCODE_CHANNEL_UP == keyCode) {
            set_channel_up(action);
            return true;
        }
        if (KeyEvent.KEYCODE_CHANNEL_DOWN == keyCode) {
            set_channel_down(action);
            return true;
        }
        return false;
    }

    public void set_channel_up(int action) {
        if (null == g_chChangeMgr)
            g_chChangeMgr = ChannelChangeManager.get_instance(this);
        long nextUpId = g_chChangeMgr.get_next_ch_up_id();
        if (KeyEvent.ACTION_DOWN == action) {

        }
        else if (KeyEvent.ACTION_UP == action) {
            startPlayChannels(nextUpId);
        }
    }

    public void set_channel_down(int action) {
        if (null == g_chChangeMgr)
            g_chChangeMgr = ChannelChangeManager.get_instance(this);
        long nextDownId = g_chChangeMgr.get_next_ch_down_id();
        if (KeyEvent.ACTION_DOWN == action) {
        }
        else if (KeyEvent.ACTION_UP == action) {
            startPlayChannels(nextDownId);
        }
    }

    public void startPlayChannels(long channelId) {
        g_chChangeMgr.change_channel_by_id(channelId);
        ProgramInfo programInfo = g_chChangeMgr.get_cur_channel();
        Uri channelUri = programInfo.getTvInputChannelUri();
        Log.d(TAG,"startPlayChannels program : "+programInfo.getDisplayName()+" "+programInfo.getChannelId());
        Log.d(TAG,"startPlayChannels channelUri : "+channelUri);
        TIFChannelData data = TIFChannelData.getTIFChannelDataFromUri(this,channelUri);
        if(data != null)
            mTextView.setText(data.toString());
    }
}