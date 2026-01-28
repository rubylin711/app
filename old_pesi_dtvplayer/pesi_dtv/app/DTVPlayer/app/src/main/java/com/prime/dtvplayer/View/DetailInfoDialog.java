package com.prime.dtvplayer.View;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.EPGEvent;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DetailInfoDialog extends Dialog{
    private final String TAG = getClass().getSimpleName();
    Dialog mDialog = null;
    private Context mContext = null;

    public DetailInfoDialog(Context context, DTVActivity.EpgUiDisplay epgUiDisplay, EPGEvent event, long channelID) {
        super(context);
        mContext = context;//caller activity (DTVBookingManager)
        mDialog = new Dialog(mContext, R.style.MyDialog);

//        mDialog.setCancelable(false);// disable click back button // Johnny 20181210 for keyboard control
        mDialog.setCanceledOnTouchOutside(false);// disable click home button and other area

        new android.os.Handler().postDelayed(new Runnable() { // Edwin 20190509 fix dialog not focus
            @Override
            public void run () {
                mDialog.show();
            }
        }, 150);
        //mDialog.show();
        mDialog.setContentView(R.layout.detail_info_dialog);
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();

        lp.dimAmount=0.0f;
            mDialog.getWindow().setAttributes(lp);
            mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);


        TextView diaTitle = (TextView) window.findViewById(R.id.detailTitleTXV);
        TextView startTime = (TextView) window.findViewById(R.id.startTimeTXV);
        TextView eventName = (TextView) window.findViewById(R.id.eventNameTXV);
        TextView detailInfo = (TextView) window.findViewById(R.id.detailTXV);
        String str, shortEventstr ;
        EPGEvent curEvent = event;
        long ChannelID = channelID;
        if(curEvent != null)
            Log.d(TAG, "DetailInfoDialog:  curEvent.Name = " + curEvent.getEventName());
        else
            Log.d(TAG, "DetailInfoDialog: curEvent = NULL!!!!!");
        SimpleDateFormat formatter= new SimpleDateFormat("HH:mm");


        diaTitle.setText((R.string.STR_DETAILED_PROGRAM_INFO));
        // ===  Set Start Time ===
        Date EventTime = new Date(curEvent.getStartTime());
        String strStartTime = formatter.format(EventTime);

        EventTime = new Date(curEvent.getEndTime());
        String strEndTime = formatter.format(EventTime);
        str = strStartTime+ "  -  " +strEndTime;
            startTime.setText(str);
        // === Set  Event   Name ====
        str = curEvent.getEventName();
            eventName.setText(str);
        // === Set   Short   Event ====
        shortEventstr = epgUiDisplay.GetShortEvent(ChannelID, curEvent.getEventId()); // connie 20180806 fix detail show not complete -s
        if(shortEventstr != null && !shortEventstr.equals(""))
            shortEventstr = shortEventstr + "\n\n";
        // ====  Set  Extend  Event ====
        str = epgUiDisplay.GetDetailInfo(ChannelID, curEvent.getEventId());
        detailInfo.setText( shortEventstr + str);// connie 20180806 fix detail show not complete-e
    }

}
