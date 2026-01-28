package com.prime.dtvplayer.View;

import android.content.Context;
import android.os.CountDownTimer;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;


public class caMsgLayout extends ConstraintLayout {
    private final String TAG = getClass().getSimpleName();
    private TextView caMsg ;
    private DTVActivity mDTV;
    private int mMsgMode, mVMXTriggerID= 0,  mVMXTriggerNum=0;
    public static int TYPE_USER = 0;
    public static int TYPE_ALWAYSE = 1;

    private CountDownTimer msgTimer = null;

    public caMsgLayout(Context context) {
        super(context);
    }
    public caMsgLayout(Context context, AttributeSet attrs) {
        this(context);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        caMsg = (TextView) findViewById(R.id.caMsgTXV);
    }

    public void SetVisibility( int visible)
    {
        ViewCAMsg( visible, 0,null, 0);
    }

    public void ViewCAMsg(int visible, int mode, String msg, int duration) {
        // mode :  alwayse =0   timer = 1   user = 2
        Log.d(TAG, "ViewCAMsg: visible =" + visible + "     msg = " + msg);
        if (visible == View.VISIBLE) {
            caMsg.setText(msg);
            mMsgMode = mode;
            if(duration != 0)
                StartMsgTick(duration);
            }
        else {
            if(mDTV !=null)
                mDTV.VMXOsmFinish(mVMXTriggerID, mVMXTriggerNum);
            mMsgMode = 0;
            mVMXTriggerID = 0;
            mVMXTriggerNum = 0;
        }
        this.setVisibility(visible);
    }

    public void StartMsgTick(int duration) // sec
    {
        Log.d(TAG, "StartBannerTick: ");
        if (msgTimer != null) {
            msgTimer.cancel();
        }

        msgTimer = new CountDownTimer(
                duration, 1000) {
            @Override
            public void onTick(long l) {
            }

            public void onFinish() {
                ViewCAMsg(View.INVISIBLE, 0, null, 0);
                msgTimer = null;
            }
        }.start();
    }

    public void SetDTVActivity( DTVActivity dtv)
    {
        mDTV = dtv;
    }

    public int GetCAMsgMode()
    {
        return mMsgMode;
    }

    public int GetVisibility()
    {
        return this.getVisibility();
    }

    public int GetTriggerID()
    {
        return mVMXTriggerID;
    }

    public int GetTriggerNum()
    {
        return mVMXTriggerNum;
    }
}
