package com.prime.dtvplayer;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.logging.Handler;

/**
 * Created by scoty_kuo on 2017/11/08.
 */

abstract public class SureDialog {
    private static final String TAG="DeleteDialog";
    Dialog mDialog = null;
    private Context mContext = null;

    public static int TYPE_YES_NO = 0; // connie 20181116 for add sureDialog type
    public static int TYPE_OK = 1; // connie 20181116 for add sureDialog type

    private int mDialogType = TYPE_YES_NO;

    TextView content;
    Button no ;
    Button yes ;


    public SureDialog(Context context) {
        mContext = context;//caller activity (DTVBookingManager)
        mDialogType = TYPE_YES_NO;
        dialogInit();
        Window window = mDialog.getWindow();
        ItemSetting(window);

    }

    public SureDialog(Context context, int type) {
        mContext = context;//caller activity (DTVBookingManager)
        mDialogType = type;
        dialogInit();
        Window window = mDialog.getWindow();
        ItemSetting(window);

    }

    private void dialogInit()
    {
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

        new android.os.Handler().postDelayed(new Runnable() { // Edwin 20190510 fix dialog has no focus
            @Override
            public void run () {
                mDialog.show();
            }
        }, 200);
        //mDialog.show();//show dialog
        mDialog.setContentView(R.layout.sure_dialog);
        WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();//set dialog window size to lp

        lp.dimAmount=0.0f;
        mDialog.getWindow().setAttributes(lp);//set dialog parameter
        mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }


    private void ItemSetting(Window window){
        no = (Button)window.findViewById(R.id.noBTN);//No button
        no.setText(R.string.STR_NO);
        yes = (Button)window.findViewById(R.id.yesBTN);//Yes button
        yes.setText(R.string.STR_YES);

        if(mDialogType == TYPE_OK) // connie 20181116 for add sureDialog type
        {
            yes.setText(R.string.STR_OK);
            no.setVisibility(View.INVISIBLE);
        }

        TextView title = (TextView)window.findViewById(R.id.titleTXV);

        content = (TextView)window.findViewById(R.id.contentTXV);//get content default text
        onSetMessage(content);//push back to caller to set text

        yes.setFocusable(true);
        yes.requestFocus();
        yes.setFocusableInTouchMode(true);

        //Onclick case
        no.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                onSetNegativeButton();//back to dialog callback onSetNegativeButton case
                dismissDialog();//close dialog
            }});

        yes.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                onSetPositiveButton();//back to dialog callback onSetNegativeButton case
                dismissDialog();//close dialog
            }});
    }

    public void onShowEvent(){}
    public void onDismissEvent(){}

    public void dismissDialog(){
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
    abstract public void onSetPositiveButton();

}
