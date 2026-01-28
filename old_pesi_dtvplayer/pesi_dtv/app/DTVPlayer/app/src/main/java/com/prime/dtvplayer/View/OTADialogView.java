package com.prime.dtvplayer.View;


import android.app.Dialog;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.ExtKeyboardDefine;

import java.util.ArrayList;

import static android.view.View.INVISIBLE;

/**
 * Created by edwin_weng on 2017/12/6.
 */

abstract public class OTADialogView extends Dialog {
    private final String TAG = getClass().getSimpleName();
    private Context mContext;
    private int mDelay;
    Handler handler = null;
    Runnable runnable;
    private TextView dialogMessage, progressValue;
    private Button OKbutton, cancelButton;
    private ProgressBar progress;
    private int run = 0;
    private String msg, showMsg ;
    private DTVActivity mdtv;
    private int mOTAMode, mTriggerID, mTriggerNum, mTunerId, mSatId, mDsmccPid, mFreqNum;
    private ArrayList<Integer> mFreqList = new ArrayList<>();//Scoty 20181207 modify VMX OTA rule
    private ArrayList<Integer> mBandwidthList = new ArrayList<>();//Scoty 20181207 modify VMX OTA rule
    static class OTAMode
    {
        public static int normal = 0;
        public static int force = 1;
        public static int user = 2;
    }
    static class OTAError
    {
        public static int SEARCHING = 0;
        public static int SW_CANT_FIND = 1;
        public static int SW_IS_THE_LATEST_VERSION = 2;
        public static int HW_ERROR = 3;
        public static int NO_SIGGNAL = 4;
        public static int OTHER = 5;
    }

    public OTADialogView (Context context, DTVActivity dtv, int otaMode, int triggerID, int triggerNum, int tunerId, int satId, int dsmccPid, int otaFreqNum, ArrayList<Integer> freqList, ArrayList<Integer> bandwidthList) {
        super(context);
        //Scoty 20181207 modify VMX OTA rule -s
        mContext = context;
        mdtv = dtv;
        mOTAMode = otaMode;
        mTriggerID = triggerID;
        mTriggerNum = triggerNum;
        mFreqNum = otaFreqNum;
        mFreqList = freqList;
        mBandwidthList = bandwidthList;
        mTunerId = tunerId;
        mSatId = satId;
        mDsmccPid = dsmccPid;
        Log.d(TAG, "OTADialogView:    mOTAMode = " + mOTAMode + "    mTriggerID = " + mTriggerID + "  mTriggerNum = " + mTriggerNum);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.ota_message_dialog);

        WindowManager.LayoutParams wlp = this.getWindow().getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;

        dialogMessage = (TextView) findViewById(R.id.msgTXV);
        progressValue = (TextView) findViewById(R.id.progressValueTXV);
        progress = (ProgressBar) findViewById(R.id.otaPROGBAR);
        OKbutton = (Button) findViewById(R.id.okBTN) ;
        cancelButton = (Button) findViewById(R.id.cancelBTN) ;
        ViewGroup.LayoutParams layoutParams;
        layoutParams = dialogMessage.getLayoutParams();
        layoutParams.width = 800;
        dialogMessage.setLayoutParams(layoutParams);
        dialogMessage.setGravity(Gravity.CENTER);

        if(otaMode == OTAMode.user)
        {
            dialogMessage.setText(mContext.getResources().getString(R.string.str_ota_user_msg));
            OKbutton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
            OKbutton.setOnClickListener(OKClickListener);
            cancelButton.setOnClickListener(CancelClickListener);
        }
        else
        {
            showWaitMsg();
            progress.setVisibility(View.VISIBLE);
            progressValue.setVisibility(View.VISIBLE);
            progressValue.setText("0 %");
            mdtv.VMXAutoOTA(mOTAMode, mTriggerID, mTriggerNum, mTunerId, mSatId, mDsmccPid, mFreqNum, mFreqList, mBandwidthList);
        }
        //Scoty 20181207 modify VMX OTA rule -e
    }

    @Override
    public void show() {
        Log.d(TAG, "show: ");
        super.show();
        if( handler != null) // connie 20180903 for VMX
            handler.postDelayed(runnable, mDelay);
    }

    @Override
    public void dismiss() {
        dialogEnd();
        super.dismiss();
    }

    public int GetTriggerID()
    {
        return mTriggerID;
    }
    public int GetTriggerNum()
    {
        return mTriggerNum;
    }

    private static int  test_progress = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown: ");

        switch (keyCode) {

            case KeyEvent.KEYCODE_BACK:
            {
                return true;
            }

            case KeyEvent.KEYCODE_PROG_BLUE:
            case ExtKeyboardDefine.KEYCODE_PROG_BLUE: // Johnny 20181210 for keyboard control
            {
                updateStatus(2, 0);
            }break;

            case KeyEvent.KEYCODE_PROG_RED:
            case ExtKeyboardDefine.KEYCODE_PROG_RED: // Johnny 20181210 for keyboard control
            {
                updateStatus(0, test_progress);
                test_progress = test_progress + 10 ;
            }break;

            case KeyEvent.KEYCODE_PROG_YELLOW:
            case ExtKeyboardDefine.KEYCODE_PROG_YELLOW: // Johnny 20181210 for keyboard control
            {
                updateStatus(1, 0);
            }break;

            case KeyEvent.KEYCODE_PROG_GREEN:
            case ExtKeyboardDefine.KEYCODE_PROG_GREEN: // Johnny 20181210 for keyboard control
            {
                updateStatus(3, 0);
            }break;

        }
        return super.onKeyDown(keyCode, event);
    }

    private View.OnClickListener OKClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            cancelButton.setVisibility(View.INVISIBLE);
            OKbutton.setVisibility(View.INVISIBLE);
            progress.setVisibility(View.VISIBLE);
            progressValue.setVisibility(View.VISIBLE);
            progressValue.setText("0 %");
            dialogMessage.setText(mContext.getResources().getString(R.string.str_ota_start));
            showWaitMsg();
            mdtv.VMXAutoOTA(mOTAMode, mTriggerID, mTriggerNum, mTunerId, mSatId, mDsmccPid, mFreqNum, mFreqList, mBandwidthList);//Scoty 20181207 modify VMX OTA rule
        }
    };
    private View.OnClickListener CancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
        }
    };

    private void showWaitMsg()
    {
        showMsg = mContext.getResources().getString(R.string.str_ota_start);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                run++;
                if(run %3 == 0)
                    msg = showMsg+".  ";
                else if( run % 3 == 1)
                    msg = showMsg+".. ";
                else
                    msg = showMsg+"...";
                dialogMessage.setText(msg);
                handler.postDelayed(runnable, 1000);
            }
        };
    }

    public void updateStatus(int otaErr, int otaSchedule)
    {
        Log.d(TAG, "updateStatus: otaErr = " + otaErr + "    otaSchedule = " + otaSchedule);
        if(otaErr == OTAError.SEARCHING) {
            String value = Integer.toString(otaSchedule) + " %";
            progress.setProgress(otaSchedule);
            progressValue.setText(value);
            Log.d(TAG, "updateStatus:  value = " + value);
        }
        else
        {
            if(handler != null )
                handler.removeCallbacks(runnable);
            handler = null;

            cancelButton.setVisibility(View.INVISIBLE);
            OKbutton.setVisibility(View.INVISIBLE);
            if(otaErr == OTAError.SW_CANT_FIND)
                dialogMessage.setText(mContext.getString(R.string.str_ota_fail));
            else if( otaErr == OTAError.SW_IS_THE_LATEST_VERSION)
                dialogMessage.setText(mContext.getString(R.string.str_ota_sw_is_the_last_version));
            else if( otaErr == OTAError.HW_ERROR)
                dialogMessage.setText(mContext.getString(R.string.str_ota_hw_err));
            else if( otaErr == OTAError.NO_SIGGNAL)
                dialogMessage.setText(mContext.getString(R.string.str_ota_no_signal));
            else if( otaErr == OTAError.OTHER)
                dialogMessage.setText(mContext.getString(R.string.str_ota_unknown_err));

            progress.setVisibility(INVISIBLE);
            progressValue.setVisibility(INVISIBLE);
            CountDownTimer counter = new CountDownTimer(
                    3000, 1000)
            {
                @Override
                public void onTick(long l) {
                }
                public void onFinish()
                {
                    dismiss();
                }
            }.start();
        }
    }

    @Override
    protected void onStop()
    {
        if(handler != null)
            handler.removeCallbacks(runnable);
        handler = null;
        super.onStop();
    }

    abstract public void dialogEnd();
}
