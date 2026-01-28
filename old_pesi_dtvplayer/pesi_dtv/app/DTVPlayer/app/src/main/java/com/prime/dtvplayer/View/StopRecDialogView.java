package com.prime.dtvplayer.View;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.PvrInfo;
import com.prime.dtvplayer.Sysdata.SimpleChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public abstract class StopRecDialogView extends Dialog
{
    private final String TAG = getClass().getSimpleName();
    private Context mContext;
    private Button cancel;
    private Button stopItem;
    private Handler CheckTimeHandler=null;
    private int pvrMode;
    DTVActivity mdtv;
    private String RecChannelName;
    private List<PvrInfo> pvrList = new ArrayList<PvrInfo>();//Scoty 20180809 modify dual pvr rule
    private int RecId = 0;
    private long ChannelId = 0;

    public StopRecDialogView(Context context, DTVActivity dtv) {
        super(context);
        mContext    = context;
        mdtv = dtv;
        setCancelable(false);// disable click back button
        setCanceledOnTouchOutside(false);// disable click home button and other area
        show();//show dialog
        setContentView(R.layout.stop_record_dialog);


        Window window = getWindow();//get dialog widow size
        if (window == null) {
            Log.d(TAG, "OkListDialogView: window = null");
            return;
        }
        WindowManager.LayoutParams lp = window.getAttributes();//set dialog window size to lp
        lp.dimAmount=0.0f;
        window.setAttributes(lp);//set dialog parameter
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setGravity(Gravity.CENTER);

        cancel = (Button) findViewById(R.id.cancelBTN);
        stopItem = (Button) findViewById(R.id.stopBTN);
        pvrMode = mdtv.getCurrentPvrMode();//pvrMode = mdtv.PvrGetCurrentRecMode();
//Scoty 20180809 modify dual pvr rule -s
        pvrList = mdtv.PvrRecordGetAllInfo();
        for(int i = 0 ; i < pvrList.size() ; i++)
        {
            if(pvrList.get(i).getPvrMode() == PvrInfo.EnPVRMode.RECORD) {
                RecId = pvrList.get(i).getRecId();
                ChannelId = pvrList.get(i).getChannelId();
                break;
            }
        }

        if(pvrMode == PvrInfo.EnPVRMode.TIMESHIFT_LIVE || pvrMode == PvrInfo.EnPVRMode.TIMESHIFT_FILE ||
                pvrMode == PvrInfo.EnPVRMode.PVRMODE_TIMESHIFT_LIVE_PAUSE ) {//Scoty 20180827 add and modify TimeShift Live Mode
            RecChannelName = mdtv.ViewHistory.getCurChannel().getChannelName();
            stopItem.setText(R.string.STR_TIMESHIFT);
        }
        else if(pvrMode == PvrInfo.EnPVRMode.RECORD) {//else if(pvrMode == REC) {
            int RecTime, Hour, Min, Sec;
            SimpleChannel channel = mdtv.GetSimpleProgramByChannelIdfromTotalChannelListByGroup(mdtv.ViewHistory.getCurGroupType(),ChannelId);
            RecChannelName = channel.getChannelName();

            RecTime = mdtv.PvrRecordGetAlreadyRecTime(mdtv.ViewHistory.getPlayId(), RecId);
            Hour = RecTime / 60 / 60;
            Min = (RecTime - (Hour * 60 * 60)) / 60;
            Sec = RecTime - (Hour * 60 * 60) - (Min * 60);
            String str = RecChannelName +String.format(Locale.getDefault(),"%02d", Hour) + " : " + String.format(Locale.getDefault(),"%02d", Min) + " : " + String.format(Locale.getDefault(),"%02d", Sec);
            stopItem.setText(str);
        }
//Scoty 20180809 modify dual pvr rule -e
        cancel.setOnClickListener(CancelClickListener);
        stopItem.setOnClickListener(StopPVRClickListener);

        if(pvrMode == PvrInfo.EnPVRMode.RECORD && CheckTimeHandler==null) {//if(pvrMode == REC && CheckTimeHandler==null) {
            Log.d(TAG, "onConnected:  ADD HANDLER !!!!");
            CheckTimeHandler = new Handler();
            CheckTimeHandler.post(GetPvrTime);
        }
    }

    private View.OnClickListener StopPVRClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
            onStopPVR();
        }
    };

    private View.OnClickListener CancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
        }
    };

    @Override
    public void onStop()
    {
        super.onStop();
        if(pvrMode == PvrInfo.EnPVRMode.RECORD)  {//if(pvrMode == REC)  {
            CheckTimeHandler.removeCallbacks(GetPvrTime);
            CheckTimeHandler = null;
        }
        Log.d(TAG, "onStop:  CheckTimeHandler REMOVE !!!!!!");
    }

    final Runnable GetPvrTime = new Runnable() {
        public void run() {
            // TODO Auto-generated method stub
            if(pvrMode == PvrInfo.EnPVRMode.RECORD) {//if(pvrMode == REC) {
                int RecTime, Hour, Min, Sec;
                RecTime = mdtv.PvrRecordGetAlreadyRecTime(mdtv.ViewHistory.getPlayId(), RecId);
                Hour = RecTime / 60 / 60;
                Min = (RecTime - (Hour * 60 * 60)) / 60;
                Sec = RecTime - (Hour * 60 * 60) - (Min * 60);
                String str = RecChannelName + " " +String.format(Locale.getDefault(),"%02d", Hour) + " : " + String.format(Locale.getDefault(),"%02d", Min) + " : " + String.format(Locale.getDefault(),"%02d", Sec);
                stopItem.setText(str);
            }
            CheckTimeHandler.postDelayed(GetPvrTime, 1000);
        }
    };

    abstract public void onStopPVR();

}
