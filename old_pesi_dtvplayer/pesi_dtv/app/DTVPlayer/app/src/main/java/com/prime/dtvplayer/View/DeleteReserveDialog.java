package com.prime.dtvplayer.View;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.BookInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DeleteReserveDialog extends Dialog {
    private final String TAG = getClass().getSimpleName();
    private Context mContext = null;
    private DTVActivity mDTVActivity = null;
    private DTVActivity.BookManager mbookManager = null;
    private BookInfo TimerInfo = null;

    public DeleteReserveDialog(Context context, DTVActivity dtvActivity, DTVActivity.BookManager bookManager, BookInfo timerInfo) {
        super(context);
        mContext = context;
        mDTVActivity = dtvActivity;
        mbookManager = bookManager ;
        TimerInfo = timerInfo;

//        setCancelable(false);// disable click back button // Johnny 20181210 for keyboard control
        setCanceledOnTouchOutside(false);// disable click home button and other area

        new android.os.Handler().postDelayed(new Runnable() { // Edwin 20190509 fix dialog not focus
            @Override
            public void run () {
                show();
            }
        }, 150);
//        show();
        setContentView(R.layout.epg_delete_reserve);
        Window window = getWindow();
        if (window == null){
            Log.d(TAG, "DeleteReserveDialog: window = null");
            return;
        }
        WindowManager.LayoutParams lp = window.getAttributes();

        lp.dimAmount=0.0f;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        final BookInfo DeleteInfo = TimerInfo;
        TextView eventName = (TextView) window.findViewById(R.id.eventNameTXV);
        TextView startTime = (TextView) window.findViewById(R.id.startTimeTXV);
        final Button deleteBTN = (Button)  window.findViewById(R.id.deleteBTN);
        final Button cancelBTN = (Button)  window.findViewById(R.id.cancelBTN);
        eventName.setText(DeleteInfo.getEventName());
        Log.d(TAG, "DeleteReserve:  DeleteInfo.getEventName()" + DeleteInfo.getEventName());
        String[] MonthList = mContext.getResources().getStringArray(R.array.STR_ARRAY_MONTH);
        Date endtime = bookManager.GetEndTime(TimerInfo);
        SimpleDateFormat format=new SimpleDateFormat("HH : mm", Locale.ENGLISH);
        String strEndTime = format.format(endtime);
        int month = DeleteInfo.getMonth();
        if(month < 1 || month > 12)
            month = 0;
        else
            month = month -1 ;
        String TimeStr = String.format(Locale.ENGLISH
                , "%s  %d  %02d : %02d ~ %s"
                , MonthList[month], DeleteInfo.getDate()
                , DeleteInfo.getStartTime()/100
                , DeleteInfo.getStartTime()%100
                , strEndTime);
        startTime.setText(TimeStr);

        deleteBTN.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if(isShowing()){
//                    mDTVActivity.BookInfoDelete(DeleteInfo.getBookId());    // remove when use pesi service
                    mbookManager.DelBookInfo(DeleteInfo);
                    mbookManager.Save();
                    dismiss();
                    mbookManager.UpdateUIBookList();
                }
            }
        });
        cancelBTN.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if(isShowing()){
                    dismiss();
                }
            }
        });

    }
}
