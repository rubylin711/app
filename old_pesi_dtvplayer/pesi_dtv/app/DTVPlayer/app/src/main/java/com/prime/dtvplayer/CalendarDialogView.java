package com.prime.dtvplayer;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CalendarView;
import android.widget.TextView;

import java.util.Date;
/**
 * Created by scoty on 2017/11/20.
 */

abstract public class CalendarDialogView{
    private static final String TAG="CalendarDialogView";
    Dialog mDialog = null;
    private Context mContext = null;

    TextView content;
    public CalendarDialogView(Context context, Date date) {
        mContext = context;
        mDialog = new Dialog(mContext,R.style.MyDialog){
            @Override
            public boolean onKeyDown(int keyCode, KeyEvent event){//key event
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                    case KeyEvent.KEYCODE_0:
                        dismissDialog();
                        break;
                }
                return super.onKeyDown(keyCode, event);
            }

        };
        mDialog.setCancelable(false);// disable click back button
        mDialog.setCanceledOnTouchOutside(false);// disable click home button and other area

        if(mDialog == null){
            return;
        }

        mDialog.setOnShowListener(new DialogInterface.OnShowListener(){
            public void onShow(DialogInterface dialog) {
                onShowEvent();
            }
        });

        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener(){
            public void onDismiss(DialogInterface dialog) {
                onDismissEvent();
            }
        });

        mDialog.show();//show dialog
        mDialog.setContentView(R.layout.calendar_dialog);
        Window window = mDialog.getWindow();//get dialog widow size
        WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();//set dialog window size to lp

        lp.dimAmount=0.0f;
        lp.gravity = Gravity.CENTER;
        mDialog.getWindow().setAttributes(lp);//set dialog parameter
        mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialogInit(window,date);

    }
    private void dialogInit(Window window, Date date){
        CalendarView calendar = (CalendarView) window.findViewById(R.id.calendarVIEW);
        calendar.setDate(date.getTime());
        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                long date = year+month+dayOfMonth;

                dismissDialog();
                onSetPositiveButton(year,month,dayOfMonth);
            }
        });


    }
    private void onShowEvent(){}
    private void onDismissEvent(){}

    private void dismissDialog(){
        if(mDialog!=null&& mDialog.isShowing()){
            mDialog.dismiss();//close dialog
        }
    }

    public boolean isShowing(){
        if(mDialog!=null&&mDialog.isShowing()){
            return mDialog.isShowing();//check dialog is exist
        }
        return false;
    }

    abstract public void onSetMessage(View v);
    abstract public void onSetNegativeButton();
    abstract public void onSetPositiveButton(int year, int month, int dayOfMonth);

}
