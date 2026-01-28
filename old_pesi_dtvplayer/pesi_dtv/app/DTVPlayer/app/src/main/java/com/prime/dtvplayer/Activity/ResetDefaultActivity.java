package com.prime.dtvplayer.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import com.dolphin.dtv.HiDtvMediaPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.prime.dtvplayer.Config.Pvcfg;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ChannelHistory;
import com.prime.dtvplayer.Sysdata.MiscDefine;
import com.prime.dtvplayer.Sysdata.PROGRAM_PLAY_STREAM_TYPE;
import com.prime.dtvplayer.View.ActivityTitleView;

import static android.view.View.INVISIBLE;

public class ResetDefaultActivity extends DTVActivity {
    private final String TAG = "ResetDefaultActivity";
    private Button mOk,mCancel;
    private ActivityTitleView mSetActivityTitle;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_default);
        ResetDefault_Init();
    }
    public void ResetDefault_Init() {
        Log.d(TAG, "ResetDefault_Init");

        mContext = this;
        mSetActivityTitle = (ActivityTitleView)findViewById(R.id.activityTitleViewLayoutResetDefault);
        mSetActivityTitle.setTitleView(getString(R.string.STR_RESET_DEFAULT));

        mOk = (Button) findViewById(R.id.resetdefaultokBTN);
        mOk.setOnClickListener(new OkOnClick());

        mCancel = (Button) findViewById(R.id.resetdefaultcancelBTN);
        mCancel.setOnClickListener(new CancelOnClick());
        mCancel.requestFocus();

    }
    class OkOnClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Log.d(TAG, "OkOnClick : onclick");
            v.setEnabled(false);
            CurSubtitleIndex = 0;
            ResetDefault();
            AvControlPlayStop(MiscDefine.AvControl.AV_STOP_ALL);
            AvControlClose(ViewHistory.getPlayId());
            CloseNetProgramView();
            ChannelHistory.Reset();
            ResetTotalChannelList();
            DoResetNetProgramDatabase();
            HiDtvMediaPlayer.getInstance().InitNetProgramDatabase();//Save New Netprogram Database
            if(Pvcfg.IsEnableNetworkPrograms())
            {
                //Update TotalChannelList only NetPrograms List and return to ViewActivity Playback
                ProgramInfoPlaySimpleChannelListUpdate(mContext,1);
                SetChannelExist(1);
            }else
            {
                //no Channel
                SetChannelExist(0);
            }
            //if not use first time install UI
            BacktoViewActivity();
            //if use first time install UI
//            finish();
//            GotoFistTimeInstall();
        }
    }
    class CancelOnClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Log.d(TAG, "CancelOnClick : onclick");
            finish();
        }
    }
    public void BacktoViewActivity() {
        Log.d(TAG, "BacktoViewActivity");
        finish();
        Intent intent = new Intent(this, ViewActivity.class);
        startActivity(intent);
    }
    public void GotoFistTimeInstall(){
        Log.d(TAG, "GotoFistTimeInstall:");
        Intent intent = new Intent(this, FirstTimeInstallationActivity.class);
        startActivity(intent);
    }

    //Close Youtube or Vod Playback before clear database
    private void CloseNetProgramView()
    {
        SimpleExoPlayer mExoPlayer = GetExoPlayer();
        SurfaceView mSurfaceViewExoplayer = GetExoplayerSurfaceView();
        WebView mWebview = GetYoutubeWebview();

        if(ViewHistory.getCurChannel() != null) {
            if (ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.VOD_TYPE) {
                mExoPlayer.stop(true);
                mExoPlayer.clearVideoSurfaceView(mSurfaceViewExoplayer);
                mSurfaceViewExoplayer.setVisibility(INVISIBLE);
            } else if (ViewHistory.getCurChannel().getPlayStreamType() == PROGRAM_PLAY_STREAM_TYPE.YOUTUBE_TYPE) {
                mWebview.setVisibility(INVISIBLE);
                mWebview.stopLoading();
                mWebview.onPause();
            }
        }
    }

    private int DoResetProgramDatabase()
    {
        int count = ResetProgramDatabase();

        return count;
    }

    private int DoResetNetProgramDatabase()
    {
        Log.d(TAG, "DoResetNetProgramDatabase: Start");
        int count = ResetNetProgramDatabase();

        return count;
    }

}
