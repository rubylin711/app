package com.prime.dtvplayer.View;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;

public class SignalDialogView extends Dialog{
    private Context mContext = null;
    private ProgressBar strengthBar ,qualityBar ;
    private TextView strengthValue,qualityValue ;
    private DTVActivity mDtv;
    private Handler CheckSignalHandler=null;

    public SignalDialogView(Context context)
    {
        super(context);
        mContext = context;
        mDtv = ((DTVActivity) context);
//        setCancelable(false);// disable click back button // Johnny 20181210 for keyboard control
        setCanceledOnTouchOutside(false);// disable click home button and other area

        setContentView(R.layout.signal_dialog);
        Window window = getWindow();//get dialog widow size
        WindowManager.LayoutParams lp=getWindow().getAttributes();//set dialog window size to lp

        lp.dimAmount=0.0f;
        getWindow().setGravity(Gravity.CENTER | Gravity.BOTTOM);
        getWindow().setAttributes(lp);//set dialog parameter
        onWindowAttributesChanged(lp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        show();//show dialog
        InitSignalDialog();
    }

    private void InitSignalDialog()
    {
        strengthBar = (ProgressBar)findViewById(R.id.strengthProgBar) ;
        qualityBar = (ProgressBar)findViewById(R.id.qualityProgBar) ;
        strengthValue = (TextView)findViewById(R.id.strengthValueTXV) ;
        qualityValue = (TextView)findViewById(R.id.qualityValueTXV) ;

        UpdateSignal(mDtv.TunerGetLockStatus(0),mDtv.TunerGetStrength(0),mDtv.TunerGetQuality(0));

        CheckSignalHandler = new Handler();
        CheckSignalHandler.post(CheckStatusRunnable);
    }

    final Runnable CheckStatusRunnable = new Runnable() {
        public void run() {
            // TODO Auto-generated method stub
            UpdateSignal(mDtv.TunerGetLockStatus(0),mDtv.TunerGetStrength(0),mDtv.TunerGetQuality(0));
            CheckSignalHandler.postDelayed(CheckStatusRunnable, 1000);

        }
    };
    private void UpdateSignal(int lock, int strength, int quality)
    {
        int barcolor;
        String value;

        if(lock == 1 )
            barcolor = Color.GREEN;
        else
            barcolor =Color.RED;
        strengthBar.setProgressTintList(ColorStateList.valueOf(barcolor));
        strengthBar.setProgress(strength);
        qualityBar.setProgressTintList(ColorStateList.valueOf(barcolor));
        qualityBar.setProgress(quality);

        value = Integer.toString(strength) + " %";
        strengthValue.setText(value);
        value = Integer.toString(quality)+ " %";
        qualityValue.setText(value);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if(CheckSignalHandler != null) {
            CheckSignalHandler.removeCallbacks(CheckStatusRunnable);
            CheckSignalHandler = null;
        }
    }
}
