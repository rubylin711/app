package com.mtest.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.mtest.config.MtestConfig;
import com.mtest.module.OtaModule;
import com.mtest.module.PesiSharedPreference;
import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.Config.PcToolcfg;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;
import com.prime.dtvplayer.View.MessageDialogView;
import com.prime.dtvplayer.utils.TVMessage;

public class ConnectPcToolActivity extends DTVActivity {
    private static final String TAG = "ConnectPcToolActivity";


    private TextView mTvMsg,mPcToolMsg;
    String pctoolMessage = "",errMessage = "";
    private static final String AllTestPASS = "All_Test_ITem_Pass";//Scoty 20190417 should check all test item pass and then trigger OTA
    private boolean checkAllTestPass = false;//Scoty 20190417 should check all test item pass and then trigger OTA
    private boolean mEnableOpt = true;
    private static final String FinishPcTool = "finish.pctool.pass";//Scoty 20190419 save write pctool finish pass/fail to SharedPreferences
    private int mRedKeyCount = 0;
    private PesiSharedPreference mPesiSharedPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_pc_tool);
        //Scoty 20190417 should check all test item pass and then trigger OTA -s
        Bundle bundle = this.getIntent().getExtras();
        if ( bundle != null ) {
            checkAllTestPass = bundle.getBoolean(AllTestPASS);
            mEnableOpt = bundle.getBoolean(OtaModule.KEY_ENABLE_OPT, true);
        }

        //Log.d(TAG, "onCreate: checkAllTestPass ==>> " + checkAllTestPass);
        //Scoty 20190417 should check all test item pass and then trigger OTA -e

        mTvMsg = (TextView) findViewById(R.id.tv_msg);
        mPcToolMsg = (TextView) findViewById(R.id.pctool_connect_msg);
        mPesiSharedPreference = new PesiSharedPreference(this);

        MtestConnectPctool();//Scoty 20190417 open pctool when enter connect pctool page
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown: keyCode = " + keyCode);
        // reset ota red key count if press other key
        if (keyCode != KeyEvent.KEYCODE_PROG_RED && keyCode != ExtKeyboardDefine.KEYCODE_PROG_RED) {
            mRedKeyCount = 0;
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: { // Johnny 20181210 for keyboard control
                // OTA
                //Scoty 20190419 save write pctool finish pass/fail to SharedPreferences -s
                mRedKeyCount++;
                if(mRedKeyCount >= 3) {
                    if (!checkAllTestPass) {
                        showOTAErrorDialog(getString(R.string.str_ota_msg_pass_all)); // Johnny 20190503 show ota error msg
                    }
                    else if (!getPcToolWriteFinish()) {
                        showOTAErrorDialog(getString(R.string.str_ota_msg_pass_pctool)); // Johnny 20190503 show ota error msg
                    }
                    else {
                        OtaModule otaModule = new OtaModule(this);
                        otaModule.enableOpt(mEnableOpt);//MtestEnableOpt(mEnableOpt);
                        // Johnny 20190620 show ota error msg by return value
                        int ret = otaModule.triggerOTASoftWare();//UpdateMtestOTASoftWare();
                        if (ret != 99) { // 99 = success
                            showOTAErrorDialog(ret);
                        }
                    }

                    mRedKeyCount = 0;
                }
                //Scoty 20190419 save write pctool finish pass/fail to SharedPreferences -e
            }break;

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onMessage(TVMessage tvMessage) {
        super.onMessage(tvMessage);

        switch (tvMessage.getMsgType()) {
            case TVMessage.TYPE_MTEST_PCTOOL:{//Scoty 20190410 add Mtest Pc Tool callback
                int cmdId = tvMessage.GetMtestCmdId(), errCode = tvMessage.GetMtestErrCode();
                Log.d(TAG,"TYPE_MTEST_PCTOOL CMD ID = " + cmdId + " Err Code = " + errCode);

                // connect pc tool
                String errStr = ErrString(errCode);
                showPCToolErr(cmdId, errStr, errCode);

            }break;
            default:
                break;
        }
    }
    //Scoty 20190410 add Mtest Pc Tool callback -s
    private void showPCToolErr(int cmdId, String errStr, int errCode)
    {
        boolean pcToolConnect =false;
        Log.d(TAG, "showPCToolErr: pctoolMessage = " + cmdId+ " errMessage = " + errStr);
        switch(cmdId)
        {
            case PcToolcfg.CMD_CONNECT_TEST: {
                pcToolConnect =true;
                pctoolMessage = getString(R.string.str_connect_pctool) + " " + errStr;
            }break;

            //Write callback
            case PcToolcfg.CMD_WRITE_MAC_ETH: {
                errMessage = getString(R.string.str_write_mac_eth) + " " + errStr;
            }break;
            case PcToolcfg.CMD_WRITE_MAC_BT: {
                errMessage = getString(R.string.str_write_mac_bt) + " " + errStr;
            }break;
            case PcToolcfg.CMD_WRITE_MAC_WIFI: {
                errMessage = getString(R.string.str_write_mac_wifi) + " " + errStr;
            }break;
            case PcToolcfg.CMD_WRITE_HDCP_1_4_TX: {
                errMessage = getString(R.string.str_write_hdcp_1_4_TX) + " " + errStr;
            }break;
            case PcToolcfg.CMD_WRITE_HDCP_1_4_RX: {
                errMessage = getString(R.string.str_write_hdcp_1_4_RX) + " " + errStr;
            }break;
            case PcToolcfg.CMD_WRITE_HDCP_2_2_TX: {
                errMessage = getString(R.string.str_write_hdcp_2_2_TX) + " " + errStr;
            }break;
            case PcToolcfg.CMD_WRITE_HDCP_2_2_RX: {
                errMessage = getString(R.string.str_write_hdcp_2_2_RX) + " " + errStr;
            }break;
            case PcToolcfg.CMD_WRITE_PRIMEID: {
                errMessage = getString(R.string.str_write_prime_id) + " " + errStr;
            }break;
            case PcToolcfg.CMD_WRITE_CUSTOMER_SN: {
                errMessage = getString(R.string.str_write_customer_sn) + " " + errStr;
            }break;
            case PcToolcfg.CMD_WRITE_CHIPID: {
                errMessage = getString(R.string.str_write_chip_id) + " " + errStr;
            }break;
            case PcToolcfg.CMD_WRITE_DRMKEY: {
                errMessage = getString(R.string.str_write_drm_key) + " " + errStr;
            }break;
            case PcToolcfg.CMD_WRITE_WIDEVINE_KEY: {
                errMessage = getString(R.string.str_write_widevine_key) + " " + errStr;
            }break;
            case PcToolcfg.CMD_WRITE_PLAYREADY_KEY_ENC: {
                errMessage = getString(R.string.str_write_playready_key) + " " + errStr;
            }break;
            case PcToolcfg.CMD_WRITE_ATTESTION_KEY: {
                errMessage = getString(R.string.str_write_attestion_key) + " " + errStr;
            }break;
            case PcToolcfg.CMD_WRITE_HDCP_1_4_KEY: {
                errMessage = getString(R.string.str_write_hdcp_1_4_key) + " " + errStr;
            }break;
            case PcToolcfg.CMD_WRITE_NAGRA_CSC: {
                errMessage = getString(R.string.str_write_nagra_csc_pk) + " " + errStr;
            }break;
            case PcToolcfg.CMD_WRITE_ANDROID_SN: {
                errMessage = getString(R.string.str_write_android_sn) + " " + errStr;
            }break;
            case PcToolcfg.CMD_WRITE_FINISH:{
                if(errCode == PcToolcfg.PcToolErr.PCTOOL_NO_ERROR)
                    setPcToolWriteFinish(true);//Scoty 20190419 save write pctool finish pass/fail to SharedPreferences
            }break;
            case PcToolcfg.CMD_WRITE_STATION_FINISH:{ // Johnny 20190521 show station result
                if(errCode == PcToolcfg.PcToolErr.PCTOOL_NO_ERROR) {
                    errMessage = getString(R.string.str_pctool_pass);
                }
                else {
                    errMessage = getString(R.string.str_pctool_fail);
                }
            }break;

            //Read callback
            case PcToolcfg.CMD_READ_MAC_ETH: {
                errMessage = getString(R.string.str_read_mac_eth) + " " + errStr;
            }break;
            case PcToolcfg.CMD_READ_MAC_BT: {
                errMessage = getString(R.string.str_read_mac_bt) + " " + errStr;
            }break;
            case PcToolcfg.CMD_READ_MAC_WIFI: {
                errMessage = getString(R.string.str_read_mac_wifi) + " " + errStr;
            }break;
            case PcToolcfg.CMD_READ_HDCP_1_4_TX: {
                errMessage = getString(R.string.str_read_hdcp_1_4_TX) + " " + errStr;
            }break;
            case PcToolcfg.CMD_READ_HDCP_1_4_RX: {
                errMessage = getString(R.string.str_read_hdcp_1_4_RX) + " " + errStr;
            }break;
            case PcToolcfg.CMD_READ_HDCP_2_2_TX: {
                errMessage = getString(R.string.str_read_hdcp_2_2_TX) + " " + errStr;
            }break;
            case PcToolcfg.CMD_READ_HDCP_2_2_RX: {
                errMessage = getString(R.string.str_read_hdcp_2_2_RX) + " " + errStr;
            }break;
            case PcToolcfg.CMD_READ_PRIMEID: {
                errMessage = getString(R.string.str_read_prime_id) + " " + errStr;
            }break;
            case PcToolcfg.CMD_READ_CUSTOMER_SN: {
                errMessage = getString(R.string.str_read_customer_sn) + " " + errStr;
            }break;
            case PcToolcfg.CMD_READ_CHIPID: {
                errMessage = getString(R.string.str_read_chip_id) + " " + errStr;
            }break;
            case PcToolcfg.CMD_READ_DRMKEY: {
                errMessage = getString(R.string.str_read_drm_key) + " " + errStr;
            }break;
            case PcToolcfg.CMD_READ_CSC_CHECKNUM: {
                errMessage = getString(R.string.str_read_csc_check_num) + " " + errStr;
            }break;
            case PcToolcfg.CMD_READ_NUID_CHECKNUM: {
                errMessage = getString(R.string.str_read_nuid_check_num) + " " + errStr;
            }break;
            case PcToolcfg.CMD_READ_DPT_CRC: {
                errMessage = getString(R.string.str_read_dpt_crc) + " " + errStr;
            }break;
            case PcToolcfg.CMD_READ_CERT_REPORT_CHECKNUM: {
                errMessage = getString(R.string.str_read_cert_report_check_num) + " " + errStr;
            }break;
            case PcToolcfg.CMD_READ_CA_SN: {
                errMessage = getString(R.string.str_read_ca_sn) + " " + errStr;
            }break;
            case PcToolcfg.CMD_READ_CSC_DATA_CONFIG: {
                errMessage = getString(R.string.str_read_csc_data_config) + " " + errStr;
            }break;
            case PcToolcfg.CMD_READ_ANDROID_SN: {
                errMessage = getString(R.string.str_read_android_sn) + " " + errStr;
            }break;
            case PcToolcfg.CMD_READ_MTEST_VERSION: {
                errMessage = getString(R.string.str_read_mtest_version) + " " + errStr;
            }break;

        }
        Log.d(TAG, "showPCToolErr: pctoolMessage = " + pctoolMessage+ " errMessage = " + errMessage);
        if(!pcToolConnect)
        {
            if(errCode == PcToolcfg.PcToolErr.PCTOOL_NO_ERROR)
                mTvMsg.setBackgroundResource(R.drawable.shape_rectangle_green);
            else
                mTvMsg.setBackgroundResource(R.drawable.shape_rectangle_red);
        }
        mTvMsg.setText(errMessage);
        mPcToolMsg.setText(pctoolMessage);
    }

    //Scoty 20190417 modify when error message bg should be red. pass should be green -s
    private String ErrString(int errCode)
    {
        String errStr="";
        switch(errCode)
        {
            case PcToolcfg.PcToolErr.PCTOOL_NO_ERROR:
                errStr = getString(R.string.str_pctool_pass);
                break;
            case PcToolcfg.PcToolErr.PCTOOL_INIT_FAIL:
                errStr = getString(R.string.str_pctool_init_fail);
                break;
            case PcToolcfg.PcToolErr.PCTOOL_WRITE_ERROR:
                errStr = getString(R.string.str_pctool_write_error);
                break;
            case PcToolcfg.PcToolErr.PCTOOL_READ_ERROR:
                errStr = getString(R.string.str_pctool_read_error);
                break;
            case PcToolcfg.PcToolErr.PCTOOL_CRC_CHECK_FAIL:
                errStr = getString(R.string.str_pctool_crc_check_fail);
                break;
            case PcToolcfg.PcToolErr.PCTOOL_TIMEOUT:
                errStr = getString(R.string.str_pctool_timeout);
                break;
            case PcToolcfg.PcToolErr.PCTOOL_CMD_NOT_SUPPORT:
                errStr = getString(R.string.str_pctool_cmd_not_support);
                break;
            case PcToolcfg.PcToolErr.PCTOOL_UNKNOW_ERROR:
                errStr = getString(R.string.str_pctool_unknown_error);
                break;
        }
        return errStr;
    }
//Scoty 20190417 modify when error message bg should be red. pass should be green -e
    //Scoty 20190410 add Mtest Pc Tool callback -e

    //Scoty 20190419 save write pctool finish pass/fail to SharedPreferences -s
    private void setPcToolWriteFinish(boolean value)
    {
        mPesiSharedPreference.putBoolean(FinishPcTool, value);
        mPesiSharedPreference.save();
    }

    private boolean getPcToolWriteFinish()
    {
        return mPesiSharedPreference.getBoolean(FinishPcTool, false);
    }
    //Scoty 20190419 save write pctool finish pass/fail to SharedPreferences -e

    // Johnny 20190503 show ota error msg
    private void showOTAErrorDialog(final String message) {
        new MessageDialogView(this, message, getResources().getInteger(R.integer.MESSAGE_DIALOG_DELAY)) {
            public void dialogEnd() {
            }
        }.show();
    }

    private void showOTAErrorDialog(final int otaReturnValue) {
//        String[] msgArray = getResources().getStringArray(R.array.STR_ARRAY_OTA_RETURN_ERROR_MSG);
//        if (otaReturnValue >= 0 && otaReturnValue < msgArray.length) {
//            new MessageDialogView(this, msgArray[otaReturnValue], getResources().getInteger(R.integer.MESSAGE_DIALOG_DELAY)) {
//                public void dialogEnd() {
//                }
//            }.show();
//        }
//        else {
//            new MessageDialogView(this, getString(R.string.str_ota_unknown_err), getResources().getInteger(R.integer.MESSAGE_DIALOG_DELAY)) {
//                public void dialogEnd() {
//                }
//            }.show();
//        }

        String message = "OTA fail error: " + otaReturnValue;
        new MessageDialogView(this, message, getResources().getInteger(R.integer.MESSAGE_DIALOG_DELAY)) {
            public void dialogEnd() {
            }
        }.show();
    }
}
