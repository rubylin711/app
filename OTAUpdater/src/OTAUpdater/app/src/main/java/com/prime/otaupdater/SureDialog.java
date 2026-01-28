package com.prime.otaupdater;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by scoty_kuo on 2017/11/08.
 */

public class SureDialog {
    private static final String TAG="DeleteDialog";
    Dialog mDialog = null;
    private Context mContext = null;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public static int TYPE_YES_NO = 0; // connie 20181116 for add sureDialog type
    public static int TYPE_OK = 1; // connie 20181116 for add sureDialog type

    private int mDialogType = TYPE_YES_NO;
    private long mDelayMs = -1;
    private boolean mEnableBackKey = false;
    TextView content;
    Button no ;
    Button yes ;


    public SureDialog(Context context, String msg) {
        mContext = context;//caller activity (DTVBookingManager)
        mDialogType = TYPE_YES_NO;
        dialogInit();
        Window window = mDialog.getWindow();
        ItemSetting(window, msg);
    }

    public SureDialog(Context context, String msg, long delayMs, boolean enableBackKey) {
        mContext = context;//caller activity (DTVBookingManager)
        mDialogType = TYPE_YES_NO;
        mDelayMs = delayMs;
        mEnableBackKey = enableBackKey;
        dialogInit();
        Window window = mDialog.getWindow();
        ItemSetting(window, msg);
    }

    public SureDialog(Context context, int type) {
        mContext = context;//caller activity (DTVBookingManager)
        mDialogType = type;
        dialogInit();
        Window window = mDialog.getWindow();
        ItemSetting(window, "NULL");
    }

    public SureDialog(Context context) {
        mContext = context;
        mDialogType = TYPE_YES_NO;
        dialogInit();
        Window window = mDialog.getWindow();
        ItemSetting(window, "Copy File");
        no.setVisibility(View.GONE);
        yes.setVisibility(View.GONE);
    }

    private void dialogInit()
    {
        mDialog = new Dialog(mContext,R.style.MyDialog){
            @Override
            public boolean onKeyDown(int keyCode, KeyEvent event){//key event
                if (mEnableBackKey) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        //dismissDialog();
                        mHandler.removeCallbacksAndMessages(null);
                        mDialog.dismiss();
                        ((MainActivity) mContext).finish();
                    }
                }
                return super.onKeyDown(keyCode, event);
            }

        };
        mDialog.setCancelable(false);// disable click back button
        mDialog.setCanceledOnTouchOutside(false);// disable click home button and other area

        if(mDialog == null){
            return;
        }

        mDialog.setOnShowListener(dialog -> onShowEvent());

        mDialog.setOnDismissListener(dialog -> onDismissEvent());

        // Edwin 20190510 fix dialog has no focus
        mHandler.postDelayed(()->{
            mDialog.show();

            // dismiss after delay
            if (mDelayMs > 0) {
                mHandler.postDelayed(this::dismissDialog, mDelayMs);
            }
        }, 200);

        mDialog.setContentView(R.layout.sure_dialog);
        WindowManager.LayoutParams lp=mDialog.getWindow().getAttributes();//set dialog window size to lp

        lp.dimAmount=0.0f;
        mDialog.getWindow().setAttributes(lp);//set dialog parameter
        mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }


    @SuppressLint("SetTextI18n")
    private void ItemSetting(Window window, String msg){
        no = (Button)window.findViewById(R.id.noBTN);//No button
        no.setText("NO");
        yes = (Button)window.findViewById(R.id.yesBTN);//Yes button
        yes.setText("YES");

        if(mDialogType == TYPE_OK) // connie 20181116 for add sureDialog type
        {
            yes.setText("OK");
            no.setVisibility(View.INVISIBLE);
        }

        TextView title = (TextView)window.findViewById(R.id.titleTXV);

        content = (TextView)window.findViewById(R.id.contentTXV);//get content default text
        content.setText(msg);
        //onSetMessage(content);//push back to caller to set text

        TextView version = window.findViewById(R.id.firmware_version_2);
        version.setText("v" + SystemProperties.get(MainActivity.PROPERTY_FIRMWARE_VERSION, "0.0.0"));

        if (mDelayMs > 0) {
            no.setVisibility(View.GONE);
            yes.setVisibility(View.GONE);
            content.setGravity(Gravity.CENTER_VERTICAL);
        }

        yes.setFocusable(true);
        yes.requestFocus();
        yes.setFocusableInTouchMode(true);

        //Onclick case
        no.setOnClickListener(v -> {
            onClickNo();//back to dialog callback onSetNegativeButton case
            dismissDialog();//close dialog
        });

        yes.setOnClickListener(v -> {
            onClickYes();//back to dialog callback onSetNegativeButton case
            dismissDialog();//close dialog
        });
    }

    public void onShowEvent(){}
    public void onDismissEvent(){}

    public void dismissDialog(){
        if(mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();//close dialog
        }
    }

    public boolean isShowing(){
        if(mDialog != null && mDialog.isShowing()) {
            return mDialog.isShowing();//check dialog is exist
        }
        return false;
    }

    //abstract public void onSetMessage(View v);
    public void onClickNo() {}
    public void onClickYes() {}

}
