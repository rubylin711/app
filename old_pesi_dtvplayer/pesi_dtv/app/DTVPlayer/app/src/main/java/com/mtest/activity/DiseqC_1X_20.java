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

import static android.view.KeyEvent.*;

public class DiseqC_1X_20 extends DTVActivity {
    final String TAG = "DiseqC_1X_20";

    private static final int LNB_POWER_ON = SatInfo.LNB_POWER_ON;
    private static final int POLAR_13V = TpInfo.Sat.POLAR_V;
    private static final int POLAR_18V = TpInfo.Sat.POLAR_H;

    private List<TextView> mTxtIndexList;
    int mTuner = -1;
    int mPort0 = 0;
    int mPort1 = 0;
    TextView port1;
    TextView port2;

    private View.OnFocusChangeListener mTxtIdxFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (hasFocus) {
                view.setBackgroundResource(R.drawable.shape_rectangle_green);
            }
            else {
                view.setBackgroundResource(R.drawable.shape_rectangle_fail);
                view.setFocusable(false);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diseqc_1x_12);

        initViews();
        changePort( mPort0, POLAR_13V );
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        PesiSharedPreference pesiSharedPreference = new PesiSharedPreference(this);

        switch (keyCode) {

            case KEYCODE_1:
                setIndexFocus(0);
                changePort ( mPort0, POLAR_13V );
                break;

            case KEYCODE_2:
                setIndexFocus(1);
                changePort( mPort1, POLAR_13V );
                break;

            case KEYCODE_DPAD_DOWN:
                if ( mTuner == 0 )
                {
                    port1.setText( getPortString(mPort0, KEYCODE_DPAD_DOWN) );
                    mPort0 = mPort0 == 3 ? 0 : mPort0 + 1;
                    changePort ( mPort0, POLAR_13V );
                    break;
                }
                else if ( mTuner == 1 )
                {
                    port2.setText( getPortString(mPort1, KEYCODE_DPAD_DOWN) );
                    mPort1 = mPort1 == 3 ? 0 : mPort1 + 1;
                    changePort( mPort1, POLAR_13V );
                    break;
                }
                break;

            case KEYCODE_DPAD_UP:
                if ( mTuner == 0 )
                {
                    port1.setText( getPortString(mPort0, KEYCODE_DPAD_UP) );
                    mPort0 = mPort0 == 0 ? 3 : mPort0 - 1;
                    changePort ( mPort0, POLAR_13V );
                    break;
                }
                else if ( mTuner == 1 )
                {
                    port2.setText( getPortString(mPort1, KEYCODE_DPAD_UP) );
                    mPort1 = mPort1 == 0 ? 3 : mPort1 - 1;
                    changePort( mPort1, POLAR_13V );
                    break;
                }
                break;

            case KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
                pesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_diseqc), MtestConfig.TEST_RESULT_FAIL);
                pesiSharedPreference.save();
                finish();
                break;
            case KEYCODE_PROG_BLUE:
            case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
                pesiSharedPreference.putInt(getResources().getString(R.string.str_test_item_diseqc), MtestConfig.TEST_RESULT_PASS);
                pesiSharedPreference.save();
                finish();
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void initViews() {
        TextView txtIndex1 = (TextView) findViewById(R.id.tv_index_1);
        TextView txtIndex2 = (TextView) findViewById(R.id.tv_index_2);
        TextView txtIndex3 = (TextView) findViewById(R.id.tv_index_3);
        port1 = (TextView) findViewById( R.id.tv_port_1 );
        port2 = (TextView) findViewById( R.id.tv_port_2 );

        mTxtIndexList = new ArrayList<>();
        mTxtIndexList.add(txtIndex1);
        mTxtIndexList.add(txtIndex2);
        mTxtIndexList.add(txtIndex3);

        for (TextView textView : mTxtIndexList) {
            textView.setOnFocusChangeListener(mTxtIdxFocusChangeListener);
        }
    }

    private void changePort( int port, int polar ) {
        List<SatInfo> satInfoList = SatInfoGetList(GetCurTunerType());

        if (satInfoList == null || satInfoList.isEmpty()) {
            Log.d(TAG, "changePort: No SatList!");
            return;
        }

        SatInfo satInfo = satInfoList.get(0);
        List<TpInfo> tpInfoList = TpInfoGetListBySatId(satInfo.getSatId());

        satInfo.Antenna.setTone22k( SatInfo.TONE_22K_0K );
        satInfo.Antenna.setDiseqcType( SatInfo.DISEQC_TYPE_1_0 );
        satInfo.Antenna.setDiseqc( port );
        SatInfoUpdate(satInfo);

        if (tpInfoList == null || tpInfoList.isEmpty()) {
            Log.d(TAG, "changeLNBConfig: No TpList!");
            return;
        }

        TpInfo tpInfo = tpInfoList.get(0);

        GposInfo gposInfo = GposInfoGet();
        gposInfo.setLnbPower(LNB_POWER_ON);
        GposInfoUpdate(gposInfo);

        // need tune to change polar(13V 18V)
        Log.d( TAG, "changePort: tpInfo.SatTp.getFreq() = "+tpInfo.SatTp.getFreq() );
        TunerTuneDVBS(0, tpInfo.getTpId(), tpInfo.SatTp.getFreq(), tpInfo.SatTp.getSymbol(), polar);
    }

    private String getPortString(int port, int keycode)
    {
        if ( KEYCODE_DPAD_DOWN == keycode )
        {
            if ( port == 0 )
                return getString( R.string.str_port_b);
            else if ( port == 1 )
                return getString( R.string.str_port_c);
            else if ( port == 2 )
                return getString( R.string.str_port_d);
            else if ( port == 3 )
                return getString( R.string.str_port_a);
        }

        if ( KEYCODE_DPAD_UP == keycode )
        {
            if ( port == 0 )
                return getString( R.string.str_port_d);
            else if ( port == 1 )
                return getString( R.string.str_port_a);
            else if ( port == 2 )
                return getString( R.string.str_port_b);
            else if ( port == 3 )
                return getString( R.string.str_port_c);
        }
        return getString( R.string.str_port_a);
    }

    private void setIndexFocus(int index) {
        mTuner = index;
        TextView textView = mTxtIndexList.get(index);
        textView.setFocusable(true);
        textView.requestFocus();
    }
}
