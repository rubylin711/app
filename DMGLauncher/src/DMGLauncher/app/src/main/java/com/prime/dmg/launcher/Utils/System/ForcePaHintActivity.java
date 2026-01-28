package com.prime.dmg.launcher.Utils.System;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.prime.dmg.launcher.ACSDatabase.ACSContentObserver;
import com.prime.dmg.launcher.ACSDatabase.ACSDataProviderHelper;
import com.prime.dmg.launcher.ACSDatabase.ACSHelper;
import com.prime.dmg.launcher.BaseActivity;
import com.prime.dmg.launcher.R;
import com.prime.dmg.launcher.Utils.ActivityUtils;
import com.prime.dtv.utils.TVMessage;

public class ForcePaHintActivity extends BaseActivity {
    String TAG = getClass().getSimpleName();

    public static final String ACTION_CLOSE_FORCE_PA = "com.vasott.tbc.hybrid.aciton.CLOSE_FORCE_PA";
    private TextView mMessageText;
    private ImageView mQrImage;
    private int mTaskId;
    private Handler mHandler = new Handler();
    private Context mContext;

    private Runnable mReorderFrontRunnable = new Runnable() {
        @Override // java.lang.Runnable
        public void run() {
            if (ActivityUtils.is_process_on_top(ForcePaHintActivity.this) || ForcePaHintActivity.this.mTaskId == -1) {
                return;
            }
            showPaDayError(ForcePaHintActivity.this, false);
        }
    };

    private BroadcastReceiver mFinishReceiver = new BroadcastReceiver() {
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ACTION_CLOSE_FORCE_PA)) {
                ForcePaHintActivity.this.finish();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forcepahint);
        TextView textView = (TextView) findViewById(R.id.lo_force_pahint_MsgText);
        this.mMessageText = textView;
        textView.setText(getText(R.string.error_e506));
        ImageView imageView = (ImageView) findViewById(R.id.lo_force_pahint_QRImage);
        this.mQrImage = imageView;
        imageView.setVisibility(TextView.GONE);
//        RMSHelper.setErrorEvent(this, "E506", getString(R.string.error_e506));
        this.mTaskId = getTaskId();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CLOSE_FORCE_PA);
        registerReceiver(mFinishReceiver,intentFilter, RECEIVER_EXPORTED);
        mContext = this;
        ContentResolver resolver = this.getContentResolver();
        resolver.registerContentObserver(ACSDataProviderHelper.ACS_PROVIDER_CONTENT_URI,true, new ACSContentObserver(new Handler(), this) {
            @Override
            public void onChange(boolean selfChange) {
                ACSDataProviderHelper acsDataProviderHelper = new ACSDataProviderHelper();
                String acs_data = acsDataProviderHelper.get_acs_provider_data(mContext,"pa_lock") ;
                Log.d(TAG,"ForceOaHintActivity ACS database change , onChange pa_lock = "+acs_data);
                if(acs_data.equals("0"))
                    mContext.sendBroadcast(new Intent(ForcePaHintActivity.ACTION_CLOSE_FORCE_PA));
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

    public static boolean showPaDayError(Context context, boolean sendClose) {
        if (ACSHelper.get_PaLock(context) == 1) {
            Intent intent = new Intent(context, ForcePaHintActivity.class);
            context.startActivity(intent);
            return true;
        } else if (!sendClose) {
            return false;
        } else {
            context.sendBroadcast(new Intent(ForcePaHintActivity.ACTION_CLOSE_FORCE_PA));
            return false;
        }
    }
}
