package com.prime.dtvplayer.View;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;

import java.util.Locale;

abstract public class VMXPinDialogView
{
    private static final String TAG="VMXPinDialogView";
    public static int PIN_TYPE_PINCDOE = 1;
    public static int PIN_TYPE_IPPV = 2;

    Dialog mDialog = null;
    private Context mContext = null;
    private DTVActivity mDTVActivity = null;
    private Resources mResouce = null;
    private Toast toast=null;
    TextView mPinTextView = null;
    TextView mPinMsg=null;
    Button mCancel,mOk;
    private int mPinType;
    private int mPinIndex = 0;
    private int mTextSelect = 0;
    // Johnny Add for two type password 20180125
    private int mChangeChannel = 0;

    private final int k_BcPinVerified            = 0x00, // connie 20180925 add for ippv/pin bcio notify
                    k_BcPinChanged            = 0x01,
                    k_BcPinFailure              = 0x21,
                    k_BcPinBlocked             = 0x22,
                    k_BcPinMayNotBeChanged   = 0x23,
                    k_BcPinMayNotBeBypassed   = 0x24,
                    k_BcPinBadIndex            = 0x25,
                    k_BcPinBadLength           = 0x26,
                    k_BcPinNotEnoughPurse      = 0x30,
                    k_BcPinGeneralError         = 0xFF;

    public VMXPinDialogView(Context context, int PinType, int changeChannel, String msg, DTVActivity DTV, int pinIndex, int TextSelector) {
        mContext = context;
        mResouce = mContext.getResources();
        mChangeChannel = changeChannel;//on normaliview, changeChannel == 1 ; otherwise == 0;
        mPinType = PinType; // 1 : Pin ,  2: IPPV
        mDTVActivity = DTV;
        mDialog = new Dialog(mContext, R.style.transparentDialog);
        if(mPinType == PIN_TYPE_PINCDOE)
            mTextSelect = TextSelector;

        mPinIndex = pinIndex;

        if(mDialog == null){
            return;
        }
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setOnKeyListener( new DialogInterface.OnKeyListener(){
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                if(event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_MENU:
                            if (onDealUpDownKey() || (mChangeChannel == 1)) {
                                ((Activity) mContext).onKeyDown(keyCode, event);
                                dismissDialog();
                                return true;
                            }
                            break;
                        case KeyEvent.KEYCODE_CHANNEL_UP:
                        case KeyEvent.KEYCODE_CHANNEL_DOWN:
                            if (onDealUpDownKey() || (mChangeChannel == 1)) {
                                ((Activity) mContext).onKeyDown(keyCode, event);
                                dismissDialog();
                                return true;
                            }
                            break;
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            if (mCancel.hasFocus()) {
                                dismissDialog();
                                return true;
                            }
                            else
                            {
                                checkPassword();
                            }
                            return true;
                    }
                }
                return false;
            }
        });
        //mDialog.show();
        new android.os.Handler().postDelayed(new Runnable() { // Edwin 20190509 fix dialog not focus
            @Override
            public void run () {
                mDialog.show();
            }
        }, 150);
        mDialog.setContentView(R.layout.vchip_pin);
        Window window = mDialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mOk = (Button) window.findViewById(R.id.ok_icon);
        mCancel = (Button) window.findViewById(R.id.return_icon);
        mPinMsg = (TextView)window.findViewById(R.id.pinMsgTXV);
        SetPinMsg(msg);
        pin_button_init(window);

    }

    private void pin_button_init(Window window ){
        mPinTextView = (TextView)window.findViewById(R.id.pin_textview);


        mCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dismissDialog();
            }
        });
    }

    public void checkPassword() {
        if (check_pin())
        {
            dismissDialog();
            onCheckPasswordIsRight();
        }
        else
        {
            onCheckPasswordIsFalse();
            mPinTextView.setText("");
            mPinTextView.requestFocus();
        }
    }

    private boolean check_pin()
    {
        String curPin = mPinTextView.getText().toString();
        int err =0;
        Log.d(TAG, "check_pin:    mPinType = " + mPinType + "    curPin = " + curPin);
        if(mPinType == 1) // PIN
            err = mDTVActivity.SetPinCode(curPin, mPinIndex, mTextSelect);
        else
            err = mDTVActivity.SetPPTV(curPin, mPinIndex);
        if(err == 0)
            return true;
        return false;
    }

    public void dismissDialog(){
        if(toast!=null)
            toast.cancel();
        if(mDialog!=null&& mDialog.isShowing()){
            mDialog.dismiss();
        }
    }

    public void cancelDialog(){
        if(toast!=null)
            toast.cancel();
        if(mDialog!=null&& mDialog.isShowing()){
            mDialog.cancel();
        }
    }

    public void showDialog(){
        if(mDialog!=null&& mDialog.isShowing()==false){
            mDialog.show();
        }
    }


    public boolean isShowing(){
        if(mDialog!=null&&mDialog.isShowing()){
            return mDialog.isShowing();
        }
        return false;
    }

    public void SetPinMsg(String msg)
    {
        if(msg == null || msg.equals(""))
            return;
        mPinMsg.setVisibility(View.VISIBLE);
        mPinMsg.setText(msg);
    }

    public int GetPinType()
    {
        return mPinType;
    }

    public void SetPinIndex(int index)
    {
        mPinIndex = index;
    }

    public void SetTextSelector(int index)
    {
        mTextSelect = index;
    }

    public void BCIO_Notify(int type)// connie 20180925 add for ippv/pin bcio notify
    {
        String msg = "";
        if(mResouce == null) {
            onBCIO_Notyfy(msg);
            return;
        }

        if(type == k_BcPinVerified)
            msg =mResouce.getString(R.string.STR_PIN_VERIFIED);
        else if(type == k_BcPinChanged)
            msg =mResouce.getString(R.string.STR_PIN_CHANGE);
        else if(type == k_BcPinFailure)
            msg =mResouce.getString(R.string.STR_PIN_FAILURE);
        else if(type == k_BcPinBlocked)
            msg =mResouce.getString(R.string.STR_PIN_BLOCK);
        else if(type == k_BcPinMayNotBeChanged)
            msg =mResouce.getString(R.string.STR_PIN_MAY_NOT_BE_CHANGE);
        else if(type == k_BcPinMayNotBeBypassed)
            msg =mResouce.getString(R.string.STR_PIN_MAY_NOT_BE_PASSED);
        else if(type == k_BcPinBadIndex)
            msg =mResouce.getString(R.string.STR_PIN_BAD_INDEX);
        else if(type == k_BcPinBadLength)
            msg =mResouce.getString(R.string.STR_PIN_BAD_LENGTH);
        else if(type == k_BcPinNotEnoughPurse)
            msg =mResouce.getString(R.string.STR_PIN_NOT_ENOUGH_PURSE);
        else if(type == k_BcPinGeneralError)
            msg =mResouce.getString(R.string.STR_PIN_GENERAL_ERROR);

        onBCIO_Notyfy(msg);
    }

    abstract public void onCheckPasswordIsRight();
    abstract public void onCheckPasswordIsFalse();
    abstract public void onBCIO_Notyfy(String type); // connie 20180925 add for ippv/pin bcio notify
    abstract public boolean onDealUpDownKey();
    //abstract public void onChangeChannel(int playUpDown);
}


