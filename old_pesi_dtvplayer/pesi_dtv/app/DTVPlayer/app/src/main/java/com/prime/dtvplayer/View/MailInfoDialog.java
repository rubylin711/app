package com.prime.dtvplayer.View;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.MailInfo;

public class MailInfoDialog extends Dialog {
    private final String TAG = getClass().getSimpleName();
    public Dialog mDialog = null;
    private Context mContext = null;

    public MailInfoDialog(Context context, MailInfo mailInfo) {
        super(context);
        mContext = context;
        mDialog = new Dialog(mContext, R.style.MyDialog);

//        mDialog.setCancelable(false);// disable click back button // Johnny 20181210 for keyboard control
        mDialog.setCanceledOnTouchOutside(false);// disable click home button and other area


        //mDialog.show();
        mDialog.setContentView(R.layout.activity_mail_info_dialog);
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();

        lp.dimAmount = 0.0f;
        mDialog.getWindow().setAttributes(lp);
        mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        TextView mailMsg = (TextView) window.findViewById(R.id.mailMsgTXV);
        mailMsg.setText(mailInfo.getMailMsg());
    }
}