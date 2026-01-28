package com.TvInput;

import android.app.Activity;
import android.content.Intent;
import android.media.tv.TvInputInfo;
import android.os.Bundle;
import android.util.Log;

import com.prime.dtvplayer.Activity.ChannelSearchActivity;
import com.prime.dtvplayer.Activity.ChannelSearchDVBSActivity;
import com.prime.dtvplayer.Activity.ChannelSearchDVBTActivity;
import com.prime.dtvplayer.Activity.ChannelSearchISDBTActivity;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.Sysdata.TpInfo;

public class TvInputActivity extends DTVActivity
{
    final String TAG = getClass().getSimpleName();

    static String TvInputServiceID;

    Activity mAct;
    static boolean TV_INPUT_OPENED = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

        mAct = TvInputActivity.this;
        TvInputServiceID = getIntent().getStringExtra(TvInputInfo.EXTRA_INPUT_ID);
        TV_INPUT_OPENED = true;

        Intent it = new Intent();
        if(GetCurTunerType() == TpInfo.DVBC) {
            it.setClass(this, ChannelSearchActivity.class);
        }
        else if(GetCurTunerType() == TpInfo.ISDBT) {
            it.setClass(this, ChannelSearchISDBTActivity.class);
        }
        else if(GetCurTunerType() == TpInfo.DVBT) {
            it.setClass(this, ChannelSearchDVBTActivity.class);
        }
        else if(GetCurTunerType() == TpInfo.DVBS) {
            it.setClass(this, ChannelSearchDVBSActivity.class);
        }
        else
            return;
        Bundle bundle = new Bundle();
        bundle.putString("parent", "MainMenu");
        it.putExtras(bundle);
        CheckMenuLockAndStartActivity( TvInputActivity.this, it );
        finish();
    }

    public static boolean isTvInputOpened ()
    {
        return TV_INPUT_OPENED;
    }

    public static void disableTvInput()
    {
        TV_INPUT_OPENED = false;
    }
}
