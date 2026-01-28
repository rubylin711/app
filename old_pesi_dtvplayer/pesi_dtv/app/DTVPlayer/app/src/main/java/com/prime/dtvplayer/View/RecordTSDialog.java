package com.prime.dtvplayer.View;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static android.view.KeyEvent.KEYCODE_BACK;


public class RecordTSDialog extends Dialog {
    private static final String TAG="RecordTSDialog";
    private Context mContext = null;
    private Button startRec, stopRec;
    private TextView fileName, duration;
    private int startRecord = 0;
    private DTVActivity mDTVActivity = null;
    private int mTunerId ;
    private String mFileName;
    Handler DurationHandler = null;
    private long curTime=0;
    private SimpleDateFormat curTimeFormatter= new SimpleDateFormat("mm : ss", Locale.getDefault());

    public RecordTSDialog( Context context, DTVActivity mDTV, int TunerId, String FileName) {
        super(context);
        mContext = context;
        mDTVActivity = mDTV;
        mTunerId = TunerId;
        mFileName = FileName;

//        setCancelable(false);// disable click back button // Johnny 20181210 for keyboard control
        setCanceledOnTouchOutside(false);// disable click home button and other area

//        new Handler().postAtFrontOfQueue()
        setContentView(R.layout.rec_ts_dialog);
        Window window = getWindow();//get dialog widow size
        if (window == null) {
            Log.d(TAG, "RecordTSDialog: window = null");
            return;
        }
        WindowManager.LayoutParams lp = window.getAttributes();//set dialog window size to lp
        lp.dimAmount=0.0f;
        window.setAttributes(lp);//set dialog parameter
        int width =lp.width;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setGravity(Gravity.CENTER);

        startRec = (Button) window.findViewById(R.id.startBTN);
        stopRec = (Button) window.findViewById(R.id.stopBTN);
        fileName = (TextView) window.findViewById(R.id.filenameTXV);
        duration = (TextView) window.findViewById(R.id.durationValueTXV);

        fileName.setText(FileName);

        startRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick:  Start Rec TS  ==== > startRecord =" +startRecord );
                if(startRecord == 1)
                    return;
                int ret = mDTVActivity.recordTS_start(0, mFileName);
                if(ret == 0)
                {
                    Log.d(TAG, "onClick: recordTS_start Success !!!!!");
                    // update duration
                    startRecord = 1;
                    curTime = 0;
                    if(DurationHandler==null) {
                        DurationHandler = new Handler();
                        DurationHandler.post(DurationRunnable);
                    }
                }
                else
                    Log.d(TAG, "onClick:  Start Rec TS Fail !!!!!");
            }
        });
        stopRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: stop Rec TS");
                stopRecordTS();
                dismiss();
            }
        });

        new Handler().postDelayed(new Runnable() { // Edwin 20190513 fix dialog has no focus
            @Override
            public void run () {
                show();//show dialog
            }
        }, 150);
        //show();//show dialog
    }

    @Override
    public void onWindowFocusChanged ( boolean hasFocus ) // Edwin 20190513 fix dialog has no focus
    {
        super.onWindowFocusChanged(hasFocus);

        if ( !startRec.isFocused() )
        {
            startRec.requestFocus();
        }
    }

    @Override
    protected void onStop()
    {
        if(DurationHandler != null)
            DurationHandler.removeCallbacks(DurationRunnable);
        DurationHandler = null;
        super.onStop();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//key event
        switch (keyCode) {
            case KEYCODE_BACK:
                Log.d(TAG, "KEYCODE_BACK");
                stopRecordTS();
//                dismiss();    // Johnny 20181210 for keyboard control
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private int stopRecordTS()
    {
        int ret = 0;
        if(startRecord == 1) {
            ret = mDTVActivity.recordTS_stop();
            if(ret == 0)
            {
                startRecord = 0;
                Log.d(TAG, "stopRecordTS:  Success !!!!");
            }
            else
                Log.d(TAG, "stopRecordTS:  Fail !!!!");
        }
        else
            Log.d(TAG, "stopRecordTS");
        return ret;
    }

    final Runnable DurationRunnable = new Runnable() {
        public void run() {
            // TODO Auto-generated method stub
            Log.d(TAG, "run: ");
            String strCurDuration = curTimeFormatter.format(curTime);
            Log.d(TAG, "run:   curTime =  " + curTime + "   str = " + strCurDuration);
            duration.setText(strCurDuration);
            curTime = curTime + 1000;

            DurationHandler.postDelayed(DurationRunnable, 1000);
        }
    };
}
