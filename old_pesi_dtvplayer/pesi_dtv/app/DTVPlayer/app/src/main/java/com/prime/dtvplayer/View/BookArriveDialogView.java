package com.prime.dtvplayer.View;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.dolphin.dtv.CallbackService;
import com.prime.dtvplayer.Activity.ViewActivity;
import com.prime.dtvplayer.R;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Created by johnny_shih on 2018/3/12.
 */

public class BookArriveDialogView extends Dialog
{
    private final String TAG = "BookArriveDialogView";
    private Context mContext = null;

    private Button mOKButton = null;
    private long mTipDuration = 0;

    private boolean mCanStartBookTask = true;

    private boolean mBookArrive = false;

    private static final int DEFAULT_REMIND_DURATION = 5;
    private long mLastTime = 0;
    private int mDuration = 0;
    private long mChannelID = 0;
    private int mType = 0;
    private int mBookTaskID = 0;

    private Handler mTimeHandler = null;
    private Runnable mTimeRunnable = null;

    public BookArriveDialogView(Context context, int duration, long channelID, int type, int taskID)
    {
        super(context, android.R.style.Theme_Material_Dialog_NoActionBar);
        mContext = context;
        mDuration = duration;
        mChannelID = channelID;
        mType = type;
        mBookTaskID = taskID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.book_arrive_dialog);

        initLayoutParams();
        initTipDuration();
        initOKButton();
    }

    @Override
    protected  void onStart()
    {
        super.onStart();

        initClock();
    }

    @Override
    protected void onStop()
    {
        mTimeHandler.removeCallbacks(mTimeRunnable);

        if (mBookArrive)
        {
            sendArriveBroadcast();
        }
        super.onStop();
    }

    private void startBookTask()
    {
        Log.d(TAG, "startBookTask mCanStartBookTask = " + mCanStartBookTask + " mBookArrive = " + mBookArrive);
        if (mCanStartBookTask)
        {
            mCanStartBookTask = false;

            ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> mRunningTasks = activityManager != null ? activityManager.getRunningTasks(1) : null;
            String runningActivity = "";

            if (mRunningTasks != null)
            {
                runningActivity = mRunningTasks.get(0).topActivity.getClassName();
            }

            Log.d(TAG, "startBookTask  runningActivity = " + runningActivity);
            if (!runningActivity.equals(CallbackService.DTV_MAIN_ACTIVITY))
            {
                // Jump to DTVPlayerActivity
                Intent dtvPlayerIntent = new Intent();
                dtvPlayerIntent.setClass(mContext, ViewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(CallbackService.DTV_BOOK_ALARM_ARRIVE_PLAY, mBookTaskID);
                dtvPlayerIntent.putExtras(bundle);

                mContext.startActivity(dtvPlayerIntent);
//                CommonDef.startActivityEx(mContext, dtvPlayerIntent);
                // Send book task broadcast
            }
        }

        if (!mBookArrive)
        {
            mBookArrive = true;
        }
        dismiss();
    }

    private void sendArriveBroadcast()
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                // delay 500ms to prevent uncaught in ViewActivity
                SystemClock.sleep(500);
                Intent bookArriveIntent = new Intent();
                bookArriveIntent.setAction(CallbackService.DTV_BOOK_ALARM_ARRIVE_PLAY);
                Bundle bundle = new Bundle();
                bundle.putInt(CallbackService.DTV_BOOK_ID, mBookTaskID);
                bundle.putLong(CallbackService.DTV_BOOK_CHANNEL_ID, mChannelID);
                bundle.putInt(CallbackService.DTV_BOOK_DURATION, mDuration);
                bundle.putInt(CallbackService.DTV_BOOK_TYPE, mType);
                bookArriveIntent.putExtras(bundle);
                mContext.sendBroadcast(bookArriveIntent, CallbackService.PERMISSION_DTV_BROADCAST);
//                CommonDef.sendBroadcastEx(mContext, bookArriveIntent);
                mBookArrive = false;
                Log.d(TAG, "run: sendBroadcast");
            }
        };
        Executors.newSingleThreadExecutor().submit(runnable);
    }

    private void initLayoutParams()
    {
        WindowManager.LayoutParams windowParams;
        if (getWindow() != null) {
            windowParams = getWindow().getAttributes();
            windowParams.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        }
    }

    private void initClock()
    {
        mTimeHandler = new Handler();
        mTimeRunnable = new Runnable() {
            public void run() {
                // OK Button count down
                if (mTipDuration > 1)
                {
                    if (SystemClock.elapsedRealtime() - mLastTime > 500)
                    {
                        mTipDuration--;
                    }

                    mTimeHandler.postDelayed(mTimeRunnable, 1000);
                }
                else
                {
                    startBookTask();
                }

                mLastTime = SystemClock.elapsedRealtime();
                setOKButtonText();
            }
        };

        // first time no delay
        mLastTime = SystemClock.elapsedRealtime();
        mTimeHandler.post(mTimeRunnable);
    }

    private void initTipDuration()
    {
        mTipDuration = DEFAULT_REMIND_DURATION;
    }

    private void setOKButtonText()
    {
        DecimalFormat format = new DecimalFormat("00");
        String ok = mContext.getResources().getString(R.string.STR_OK);
        String str = ok + "(" + format.format(mTipDuration) + ")";
        mOKButton.setText(str);
    }

    private void initOKButton()
    {
        mOKButton = (Button) findViewById(R.id.okBTN);
        setOKButtonText();
        mOKButton.setOnClickListener(OkClickListener);
        mOKButton.requestFocus();
    }

    private View.OnClickListener OkClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startBookTask();
        }
    };
}