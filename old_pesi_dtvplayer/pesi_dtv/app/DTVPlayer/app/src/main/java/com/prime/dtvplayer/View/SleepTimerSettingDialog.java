package com.prime.dtvplayer.View;

import android.app.Dialog;
import android.content.Context;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import com.prime.dtvplayer.R;

public class SleepTimerSettingDialog extends Dialog {
    private Context mContext = null;
    private Spinner spinner_SleepTime;
    private SelectBoxView sel_SleepTime;
    private Button OkBtn;
    private final String AUTO_STANDBY_KEY = "sleep_timeout";

    public SleepTimerSettingDialog(Context context)
    {
        super(context);
        mContext = context;
//        setCancelable(false);// disable click back button // Johnny 20181210 for keyboard control
        setCanceledOnTouchOutside(false);// disable click home button and other area

        setContentView(R.layout.sleep_timer_setting);
        Window window = getWindow();//get dialog widow size
        WindowManager.LayoutParams lp=getWindow().getAttributes();//set dialog window size to lp

        lp.dimAmount=0.0f;
        getWindow().setGravity(Gravity.CENTER);
        getWindow().setAttributes(lp);//set dialog parameter
        onWindowAttributesChanged(lp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        show();//show dialog
        InitSleepTimerSetting();
    }

    private void InitSleepTimerSetting()
    {
        spinner_SleepTime = (Spinner) getWindow().findViewById(R.id.sleepSPINNER);
        sel_SleepTime = new SelectBoxView(mContext,
                spinner_SleepTime, mContext.getResources().getStringArray(R.array.STR_AUTO_STANDBY_OPTION_ENTRIES));
        spinner_SleepTime.setSelection(SetAutoStandbyTimeIndex(Settings.Secure.getInt(mContext.getContentResolver(), AUTO_STANDBY_KEY, 0)));
        spinner_SleepTime.setOnItemSelectedListener(AutoStandbyListener);
        spinner_SleepTime.requestFocus();

        OkBtn = (Button) findViewById(R.id.okBTN);
        OkBtn.setOnKeyListener(SleepTimerOnKeyListener);
    }

    private AdapterView.OnItemSelectedListener AutoStandbyListener =  new SelectBoxView.SelectBoxOnItemSelectedListener()
    {
        @Override
        public void onItemSelected(AdapterView adapterView, View view, int position, long id) {
            super.onItemSelected(adapterView,view,position,id);
            AutoStandbyTimeTransfer(position);
        }
        @Override
        public void onNothingSelected(AdapterView arg0) {
        }
    };

    private View.OnKeyListener SleepTimerOnKeyListener = new View.OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event)
        {
            boolean isActionDown = (event.getAction() == KeyEvent.ACTION_DOWN);
            if (isActionDown)
            {
                switch (keyCode)
                {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                         dismiss();
                        return true;
                    default:
                        break;
                }
            }
            return false;
        }
    };

    private int SetAutoStandbyTimeIndex(int time)
    {
        String[] timeList = mContext.getResources().getStringArray(R.array.STR_AUTO_STANDBY_OPTION_ENTRIES_VALUES);
        int index = 0;
        for(int i = 0 ; i < timeList.length ; i++)
        {
            if(Integer.valueOf(timeList[i]) == time)
            {
                index = i;
                break;
            }
        }
        return index;
    }

    private void AutoStandbyTimeTransfer(int position)
    {
        String[] timeList = mContext.getResources().getStringArray(R.array.STR_AUTO_STANDBY_OPTION_ENTRIES_VALUES);
        int standbyTime = Integer.valueOf(timeList[position]);
        Settings.Secure.putInt(mContext.getContentResolver(), AUTO_STANDBY_KEY, standbyTime);
    }
}
