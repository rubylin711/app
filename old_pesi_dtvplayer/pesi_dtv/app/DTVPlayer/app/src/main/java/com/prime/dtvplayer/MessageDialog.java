package com.prime.dtvplayer;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by scoty on 2017/11/15.
 */

abstract public class MessageDialog {
    private static final String TAG="MessageDialog";
    Dialog mDialog = null;
    private Context mContext = null;
    TextView content;
    Button yes ;
    private Handler mHandler;
    private Runnable mRunnable;
    private int mTickTime=0;

    public MessageDialog(Context context, int tickTime) {
        mContext = context;//caller activity (DTVBookingManager)
        mDialog = new Dialog(mContext,R.style.MyDialog){
            @Override
            public boolean onKeyDown(int keyCode, KeyEvent event){//key event
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                        //dismissDialog();
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
        mDialog.setContentView(R.layout.message_dialog);
        Window window = mDialog.getWindow();//get dialog widow size
        WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();//set dialog window size to lp

        lp.dimAmount=0.0f;
        mDialog.getWindow().setAttributes(lp);//set dialog parameter
        mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialogInit(window);

        if(tickTime != 0)
        {
            mTickTime = tickTime;
            mHandler = new Handler();
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "DDD run");//eric lin test
                    dialogEnd(0);
                    mHandler.removeCallbacks(mRunnable);
                    mDialog.dismiss();
                }
            };
            mHandler.postDelayed(mRunnable, tickTime);
        }

    }


    private void dialogInit(Window window){
        yes = (Button)window.findViewById(R.id.yesBTN);//Yes button
//        yes.setText(R.string.STR_OK);
//        yes.setTextColor(Color.WHITE);

        TextView title = (TextView)window.findViewById(R.id.titleTXV);

        content = (TextView)window.findViewById(R.id.contentTXV);//get content default text
        onSetMessage(content);//push back to caller to set text

        yes.setFocusable(true);
        yes.requestFocus();
        yes.setFocusableInTouchMode(true);

        //Onclick case

        yes.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                onSetPositiveButton(1);//back to dialog callback onSetNegativeButton case
                dismissDialog();//close dialog
            }});
    }

    public void onShowEvent(){}
    public void onDismissEvent(){}

    public void dismissDialog(){
        if(mTickTime != 0) {
            Log.d(TAG, "DDD dismissDialog mTickTime="+mTickTime);//eric lin test
            mHandler.removeCallbacks(mRunnable);
        }
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
    abstract public void onSetPositiveButton(int status);
    abstract public void dialogEnd(int status);
}
