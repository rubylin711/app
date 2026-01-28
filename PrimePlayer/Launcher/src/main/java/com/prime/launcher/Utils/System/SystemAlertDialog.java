package com.prime.launcher.Utils.System;

import static com.prime.launcher.HomeApplication.get_prime_dtv;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.prime.launcher.ACSDatabase.ACSHelper;
import com.prime.launcher.ACSDatabase.ACSSystemAlertParam;
import com.prime.launcher.HomeApplication;
import com.prime.launcher.R;
import com.prime.launcher.Utils.URLUtils;
import com.prime.datastructure.sysdata.MailInfo;

public class SystemAlertDialog  extends Dialog {
    static String TAG = SystemAlertDialog.class.getSimpleName();
    private static SystemAlertDialog mSystemAlertDialog = null ;
    private TextView mMessageText;
    private ImageView mQrImage;
    private Context mContext;

    public SystemAlertDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_alert);
        //mContext = this.getContext();
        ACSSystemAlertParam param = ACSHelper.get_system_alert_param(mContext);
        mMessageText = (TextView) findViewById(R.id.lo_system_alert_text);

        if(param.getMessage()!=null&&!param.getMessage().isEmpty())
            mMessageText.setText(param.getMessage());

        mQrImage = (ImageView) findViewById(R.id.lo_system_alert_QRImage);
        if(param.getQrCode()!=null&&!param.getQrCode().isEmpty()) {
            Bitmap qr_code = URLUtils.generate_qr_code(get_prime_dtv().get_mail_qrcode_type(mContext, param.getQrCode()), 600) ;

            if (!get().isFinishing() && !get().isDestroyed())
                Glide.with(get())
                        .load(qr_code)
                        .into(mQrImage);
            mQrImage.setVisibility(TextView.VISIBLE);
        }
        else
            mQrImage.setVisibility(TextView.GONE);
    }

    public static boolean showSystemAlert(Context context) {
//        Log.d(TAG, "mSystemAlertDialog = " + mSystemAlertDialog) ;
        if (ACSHelper.get_SystemAlertStatus(context) && ACSHelper.get_system_alert_param(context).is_exist()) {
            if ( mSystemAlertDialog != null )
            {
                mSystemAlertDialog.dismiss();
                mSystemAlertDialog = null ;
            }

            mSystemAlertDialog = new SystemAlertDialog(context);
            mSystemAlertDialog.show();
            return true;
        } else {
            if ( mSystemAlertDialog != null )
            {
                mSystemAlertDialog.dismiss();
                mSystemAlertDialog = null ;
            }
            return false;
        }
    }

    private AppCompatActivity get() {
        return (AppCompatActivity) mContext;
    }
}
