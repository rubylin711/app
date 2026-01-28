package com.mtest.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.mtest.config.MtestConfig;
import com.mtest.module.PesiSharedPreference;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.Sysdata.GposInfo;
import com.prime.dtvplayer.Sysdata.SatInfo;
import com.prime.dtvplayer.Sysdata.TpInfo;

import java.util.ArrayList;
import java.util.List;

public class LnbOnOffActivity extends DTVActivity {
    final String TAG = "LnbOnOffActivity";

    private static final int TONE_22K_ON = SatInfo.TONE_22K_22K;
    private static final int TONE_22K_OFF = SatInfo.TONE_22K_0K;
    private static final int TONE_22K_AUTO = SatInfo.TONE_22K_AUTO;
    private static final int LNB_POWER_ON = SatInfo.LNB_POWER_ON;
    private static final int LNB_POWER_OFF = SatInfo.LNB_POWER_OFF;
    private static final int POLAR_13V = TpInfo.Sat.POLAR_V;
    private static final int POLAR_18V = TpInfo.Sat.POLAR_H;

    private List<TextView> mTxtIndexList;

    private View.OnFocusChangeListener mTxtIdxFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (hasFocus) {
                view.setBackgroundResource(R.drawable.shape_rectangle_focus);
            }
            else {
                view.setBackgroundResource(R.drawable.shape_rectangle_blue_bright);
                view.setFocusable(false);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lnb_on_off);

        initViews();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        PesiSharedPreference pesiSharedPreference = new PesiSharedPreference(this);

        switch (keyCode) {
            case KeyEvent.KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                pesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_lnb), MtestConfig.TEST_RESULT_FAIL);
                pesiSharedPreference.save();
                finish();
                break;
            case KeyEvent.KEYCODE_PROG_BLUE:
            case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
                pesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_lnb), MtestConfig.TEST_RESULT_PASS);
                pesiSharedPreference.save();
                finish();
                break;
            case KeyEvent.KEYCODE_0:
                setIndexFocus(0);
                changeLNBConfig(LNB_POWER_ON, TONE_22K_ON, POLAR_13V);
                break;
            case KeyEvent.KEYCODE_1:
                setIndexFocus(1);
                changeLNBConfig(LNB_POWER_ON, TONE_22K_ON, POLAR_18V);
                break;
            case KeyEvent.KEYCODE_2:
                setIndexFocus(2);
                changeLNBConfig(LNB_POWER_ON, TONE_22K_OFF, POLAR_13V);
                break;
            case KeyEvent.KEYCODE_3:
                setIndexFocus(3);
                changeLNBConfig(LNB_POWER_ON, TONE_22K_OFF, POLAR_18V);
                break;
            case KeyEvent.KEYCODE_4:
                setIndexFocus(4);
                changeLNBConfig(LNB_POWER_OFF, 0, 0);
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void initViews() {

        TextView txtIndex0 = (TextView) findViewById(R.id.tv_index_0);
        TextView txtIndex1 = (TextView) findViewById(R.id.tv_index_1);
        TextView txtIndex2 = (TextView) findViewById(R.id.tv_index_2);
        TextView txtIndex3 = (TextView) findViewById(R.id.tv_index_3);
        TextView txtIndex4 = (TextView) findViewById(R.id.tv_index_4);

        mTxtIndexList = new ArrayList<>();
        mTxtIndexList.add(txtIndex0);
        mTxtIndexList.add(txtIndex1);
        mTxtIndexList.add(txtIndex2);
        mTxtIndexList.add(txtIndex3);
        mTxtIndexList.add(txtIndex4);

        for (TextView textView : mTxtIndexList) {
            textView.setOnFocusChangeListener(mTxtIdxFocusChangeListener);
        }
    }

    private void changeLNBConfig(int lnbPower, int tone22k, int polar) {
        List<SatInfo> satInfoList = SatInfoGetList(GetCurTunerType());
        if (satInfoList == null || satInfoList.isEmpty()) {
            Log.d(TAG, "changeLNBConfig: No SatList!");
            return;
        }

        // update sat(SatInfoUpdate() uses gpos.getlnbpower to set lnb power)
        SatInfo satInfo = satInfoList.get(0);
        satInfo.Antenna.setTone22k( tone22k );
        SatInfoUpdate(satInfo);

        List<TpInfo> tpInfoList = TpInfoGetListBySatId(satInfo.getSatId());
        if (tpInfoList == null || tpInfoList.isEmpty()) {
            Log.d(TAG, "changeLNBConfig: No TpList!");
            return;
        }

        // change lnb power in gpos
        GposInfo gposInfo = GposInfoGet();
        gposInfo.setLnbPower( lnbPower );
        GposInfoUpdate(gposInfo);

        // need tune to change Tone22k(on/off), polar(13V/18V), LNB Power(on/off)
        TpInfo tpInfo = tpInfoList.get(0);
        TunerTuneDVBS(0, tpInfo.getTpId(), tpInfo.SatTp.getFreq(), tpInfo.SatTp.getSymbol(), polar);
    }

    private void setIndexFocus(int index) {
        TextView textView = mTxtIndexList.get(index);
        textView.setFocusable(true);
        textView.requestFocus();
    }
}
