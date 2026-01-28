package com.prime.launcher.Utils.System;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;

import com.prime.launcher.ACSDatabase.ACSContentObserver;
import com.prime.launcher.ACSDatabase.ACSDataProviderHelper;
import com.prime.launcher.ACSDatabase.ACSForceSrceenParam;
import com.prime.launcher.ACSDatabase.ACSHelper;
import com.prime.launcher.BaseActivity;
import com.prime.launcher.R;
import com.prime.launcher.Utils.ActivityUtils;
import com.prime.launcher.Utils.URLUtils;
import com.prime.datastructure.sysdata.MailInfo;
import com.prime.datastructure.utils.TVMessage;

public class ForceScreenActivity extends BaseActivity {
    String TAG = getClass().getSimpleName();

    public static final String ACTION_CLOSE_FORCE_SCREEN = "com.prime.launcher.action.CLOSE_FORCE_SCREEN";
    private TextView mMessageText;
    private ImageView mImage;
    private ImageView mQrImage;
    private int mTaskId;
    private Handler mHandler = new Handler();
    private Context mContext;

    private Runnable mReorderFrontRunnable = new Runnable() {
        @Override // java.lang.Runnable
        public void run() {
            if (ActivityUtils.is_process_on_top(ForceScreenActivity.this) || ForceScreenActivity.this.mTaskId == -1) {
                return;
            }
            showForceScreen(ForceScreenActivity.this, false);
        }
    };

    private BroadcastReceiver mFinishReceiver = new BroadcastReceiver() {
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ACTION_CLOSE_FORCE_SCREEN)) {
                ForceScreenActivity.this.finish();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_force_screen);
        mContext = this;
        ACSForceSrceenParam param = ACSHelper.get_force_screen_param(this);
        TextView textView = (TextView) findViewById(R.id.lo_force_screen_MsgText);
        this.mMessageText = textView;
        if(param.getMessage()!=null&&!param.getMessage().isEmpty())
            textView.setText(param.getMessage());
        ImageView imageView = (ImageView) findViewById(R.id.lo_force_screen_ForceImage);
        this.mImage = imageView;
        if(param.getImage()!=null&&!param.getImage().isEmpty()) {
            if (!isFinishing() && !isDestroyed())
                Glide.with(this)
                        .load(param.getImage())
                        .into(imageView);
            imageView.setVisibility(TextView.VISIBLE);
        }
        else
            imageView.setVisibility(TextView.GONE);

        ImageView imageViewQrcode = (ImageView) findViewById(R.id.lo_force_screen_QRImage);
        this.mQrImage = imageViewQrcode;
        if(param.getQrCode()!=null&&!param.getQrCode().isEmpty()) {
            Bitmap qr_code = URLUtils.generate_qr_code(get_prime_dtv().get_mail_qrcode_type(mContext, param.getQrCode()), 600) ;
            if (!isFinishing() && !isDestroyed())
                Glide.with(this)
                        .load(qr_code)
                        .into(imageViewQrcode);
            imageViewQrcode.setVisibility(TextView.VISIBLE);
        }
        else
            imageViewQrcode.setVisibility(TextView.GONE);

//        RMSHelper.setErrorEvent(this, "E506", getString(R.string.error_e506));
        this.mTaskId = getTaskId();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CLOSE_FORCE_SCREEN);
        registerReceiver(mFinishReceiver,intentFilter, RECEIVER_EXPORTED);
        ContentResolver resolver = this.getContentResolver();
        resolver.registerContentObserver(ACSDataProviderHelper.ACS_PROVIDER_CONTENT_URI,true, new ACSContentObserver(new Handler(), this) {
            @Override
            public void onChange(boolean selfChange) {
                ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
                String acs_data = acsDataProviderHelper.get_acs_provider_data(mContext,"ForceScreenStatus") ;
                Log.d(TAG,"ForceScreenActivity ACS database change , onChange ForceScreenStatus = "+acs_data);
                if(acs_data.equals("0"))
                    mContext.sendBroadcast(new Intent(ACTION_CLOSE_FORCE_SCREEN));
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        this.mTaskId = -1;
        this.mHandler.removeCallbacks(this.mReorderFrontRunnable);
        unregisterReceiver(this.mFinishReceiver);
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        this.mHandler.removeCallbacks(this.mReorderFrontRunnable);
    }

    @Override
    public void onStop() {
        super.onStop();
        this.mHandler.postDelayed(this.mReorderFrontRunnable, 200L);
    }

    @Override
    public void onMessage(TVMessage msg) {
        super.onMessage(msg);
    }

    @Override
    public void onBroadcastMessage(Context context, Intent intent) {
        super.onBroadcastMessage(context, intent);
    }

    public static boolean showForceScreen(Context context, boolean sendClose) {
        if (ACSHelper.get_ForceScreenStatus(context) && ACSHelper.get_force_screen_param(context).is_exist()) {
            Intent intent = new Intent(context, ForceScreenActivity.class);
            context.startActivity(intent);
            return true;
        } else if (!sendClose) {
            return false;
        } else {
            context.sendBroadcast(new Intent(ForceScreenActivity.ACTION_CLOSE_FORCE_SCREEN));
            return false;
        }
    }
}
