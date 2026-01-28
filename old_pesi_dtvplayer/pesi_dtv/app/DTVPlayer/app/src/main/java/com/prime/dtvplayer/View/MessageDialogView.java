package com.prime.dtvplayer.View;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.prime.dtvplayer.R;
import android.os.Handler;

/**
 * Created by edwin_weng on 2017/12/6.
 */

abstract public class MessageDialogView extends Dialog {
    private final String TAG = getClass().getSimpleName();
    private Context mContext;
    private int mDelay;
    Handler handler;
    Runnable runnable;
    private TextView dialogMessage;

    private static MessageDialogView StatcMessageDialogView = null;
    public static void SetMessageInstance(MessageDialogView messageDialogView)
    {
        StatcMessageDialogView = messageDialogView;
    }

    public static MessageDialogView GetMessageDialogView()
    {
        return  StatcMessageDialogView;
    }

    public MessageDialogView (Context context, String message, int delay) {
        super(context);
        Log.d(TAG, "MessageDialogView: message = " + message);
        mContext = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.message_dialog_view);

        Window window = getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent); // edwin 20210113 fix dialog has white frame
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;

        dialogMessage = (TextView) findViewById(R.id.messageDialog_message);
        dialogMessage.setText(message);
        mDelay = delay;

        if(mDelay > 0) { // connie 20181116 for no delay time, close dialog by press back key
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            };
        }
    }

    // Edwin 20181129 add to simplify message format -s
    public MessageDialogView (Context context, int messageRes, int delay) {
        super(context);
        Log.d(TAG, "MessageDialogView: messageRes = " + messageRes);
        mContext = context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.message_dialog_view);

        Window window = getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);  // edwin 20210113 fix dialog has white frame
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;

        dialogMessage = (TextView) findViewById(R.id.messageDialog_message);
        dialogMessage.setText(context.getString(messageRes));
        mDelay = delay;

        if(mDelay > 0) { // connie 20181116 for no delay time, close dialog by press back key
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            };
        }
    }
    // Edwin 20181129 add to simplify message format -e

    @Override
    public void show() {
        Log.d(TAG, "show: ");
        super.show();

        if(mDelay > 0) // connie 20181116 for no delay time, close dialog by press back key
            handler.postDelayed(runnable, mDelay);
    }

    @Override
    public void dismiss() {
        dialogEnd();
        super.dismiss();
    }

    public void setText(String text)
    {
        dialogMessage.setText(text);
    }

    abstract public void dialogEnd();
}
