package com.prime.dtvplayer.View;

import android.app.Dialog;
import android.content.Context;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.dolphin.dtv.HiDtvMediaPlayer;
import com.prime.dtvplayer.R;
import com.prime.dtvplayer.Sysdata.BookInfo;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Created by johnny_shih on 2018/3/7.
 */

public class OtaDownloadAlarmDialogView extends Dialog {
    private final String TAG = "BookAlarmDialogView";

    private int mBookId = 0;
    private Context mContext = null;

    //private TextView mChannelTextView = null;
    //private TextView mNameTextView = null;
    //private TextView mStartTimeTextView = null;
    //private TextView mDurationTextView = null;
    private TextView mClockTextView = null;

    private Button mOKButton = null;
    private Button mCancelButton = null;

    private HiDtvMediaPlayer mDTV = null;
    //private BookInfo mBookTask = null;

    private long mTipDuration = 0;
    private long mLastTime = 0;

    //private boolean mCanStartBookTask = true;
    private boolean mKeyback = false;
    private boolean mBookAlarm = false;

    private Handler mTimeHandler = null;
    private Runnable mTimeRunnable = null;

    private static final int DEFAULT_REMIND_DURATION = 10;

    private static final int DEFAULT_TIP_DURATION = 10;
    private CountDownTimer cdTimer = null;

    public OtaDownloadAlarmDialogView(Context context)
    {
        super(context, android.R.style.Theme_Material_Dialog_NoActionBar);
        mContext = context;
        //mBookId = bookId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

        setContentView(R.layout.ota_download_alarm_dialog);

        initLayoutParams();
        initDTV();
        //initBookTask();


            initTipDuration();
            //initTextView();
//            initButton(View.VISIBLE);


        initButton();


          //count down timer
//        cdTimer = new CountDownTimer(10000,1000){
//
//            @Override
//            public void onFinish() {
//                //mTextView.setText("Done!");
//            }
//
//            @Override
//            public void onTick(long millisUntilFinished) {
//                setOKButtonText((int)millisUntilFinished/1000);
//                //mTextView.setText("seconds remaining:"+millisUntilFinished/1000);
//            }
//
//        }.start();

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

        //Log.d(TAG, "onStop: mBookAlarm = " + mBookAlarm + " mCanStartBookTask = " + mCanStartBookTask);
        if (mBookAlarm)
        {
            //sendRemindBroadcast();
        }
        else
        {

        }

        Log.d(TAG, "onStop: mBookAlarm = " + mBookAlarm);
        super.onStop();
    }

    private void startBookTask()//eric lin say important
    {


        if (mKeyback)
        {
            return;
        }


        dismiss();
    }

    private void sendRemindBroadcast()
    {
//        Runnable runnable = new Runnable()
//        {
//            public void run()
//            {
//                // delay 500ms to prevent uncaught in ViewActivity
//                SystemClock.sleep(500);
//                Intent bookAlarmIntent = new Intent();
//                bookAlarmIntent.setAction(CallbackService.DTV_BOOK_ALARM_REMIND_PLAY);
//                Bundle bundle = new Bundle();
//                bundle.putInt(CallbackService.DTV_BOOK_ID, mBookTask.getBookId());
//                bundle.putInt(CallbackService.DTV_BOOK_CHANNEL_ID, mBookTask.getChannelId());
//                bundle.putInt(CallbackService.DTV_BOOK_DURATION, mBookTask.getDuration());
//                bundle.putInt(CallbackService.DTV_BOOK_TYPE, mBookTask.getBookType());
//                bookAlarmIntent.putExtras(bundle);
//                mContext.sendBroadcast(bookAlarmIntent, CallbackService.PERMISSION_DTV_BROADCAST);
////                CommonDef.sendBroadcastEx(mContext, bookArriveIntent);
//                mBookAlarm = false;
//
//                Log.d(TAG, "run: sendBroadcast");
//            }
//        };
//        Executors.newSingleThreadExecutor().submit(runnable);
    }

    private void initLayoutParams()
    {
        WindowManager.LayoutParams windowParams;
        if (getWindow() != null) {
            windowParams = getWindow().getAttributes();
            windowParams.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        }
    }

    private void initDTV()
    {
        mDTV = HiDtvMediaPlayer.getInstance();
    }

//    private void initBookTask()
//    {
//        mBookTask = mDTV.BookInfoGet(mBookId);
//    }

    private void initClock()
    {
        mClockTextView = (TextView) findViewById(R.id.textClock);
        mTimeHandler = new Handler();
        mTimeRunnable = new Runnable() {
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
                //sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                String timeNow = sdf.format(mDTV.getDtvDate());
                mClockTextView.setText(timeNow);

                // OK Button count down
                if (mTipDuration > 1)
                {
                    Log.d(TAG, "run: mTipDuration="+mTipDuration+", SystemClock.elapsedRealtime()="+SystemClock.elapsedRealtime()+", mLastTime="+mLastTime);//eric lin test
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

//    private void initTextView()
//    {
//        String str;
//        mChannelTextView = (TextView) findViewById(R.id.channelTXV);
//        str = mDTV.GetProgramByChannelId(mBookTask.getChannelId()).getDisplayName();
//        mChannelTextView.setText(str);
//
//        mNameTextView = (TextView) findViewById(R.id.evtNameTXV);
//        str = mBookTask.getEventName();
//        mNameTextView.setText(str);
//
//        mStartTimeTextView = (TextView) findViewById(R.id.startTimeTXV);
//        str = getStartTimeStringFromBook(mBookTask);
//        mStartTimeTextView.setText(str);
//
//        mDurationTextView = (TextView) findViewById(R.id.durationTXV);
//        str = String.format(Locale.getDefault(),
//                "%02d:%02d:%02d",
//                mBookTask.getDuration()/100, mBookTask.getDuration()%100, 0);
//        mDurationTextView.setText(str);
//    }

    private void initButton(/*int visible*/)
    {
        mOKButton = (Button) findViewById(R.id.okBTN);
        mOKButton.setOnClickListener(OkClickListener);
        setOKButtonText();
        mOKButton.requestFocus();

        mCancelButton = (Button) findViewById(R.id.cancelBTN);
        mCancelButton.setOnClickListener(CancelClickListener);
//        mCancelButton.setVisibility(visible);
    }

    private void setOKButtonText()
    {
        DecimalFormat format = new DecimalFormat("00");
        String ok = mContext.getResources().getString(R.string.STR_OK);
        String str = ok + "(" + format.format(mTipDuration) + ")";
        mOKButton.setText(str);
    }

    private void setOKButtonText(int seconds)
    {
        DecimalFormat format = new DecimalFormat("00");
        String ok = mContext.getResources().getString(R.string.STR_OK);
        String str = ok + "(" + format.format(seconds) + ")";
        mOKButton.setText(str);
    }

    private Date getDateFromBook(BookInfo bookInfo)
    {
        String strDate = String.format(Locale.getDefault(),
                "%d/%02d/%02d %02d:%02d:%02d",
                bookInfo.getYear(), bookInfo.getMonth(), bookInfo.getDate(), bookInfo.getStartTime()/100, bookInfo.getStartTime()%100, 0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        Date date = mDTV.getDtvDate();
        try {
            date = sdf.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    private String getStartTimeStringFromBook(BookInfo bookInfo)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        Date date = getDateFromBook(bookInfo);
        return sdf.format(date);
    }

    private void updateBook(BookInfo bookInfo, Date startDate)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        bookInfo.setYear(calendar.get(Calendar.YEAR));
        bookInfo.setMonth(calendar.get(Calendar.MONTH)+1);
        bookInfo.setDate(calendar.get(Calendar.DATE));
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        if(calendar.getFirstDayOfWeek() == Calendar.SUNDAY){
            week = week - 1;
            if(week == 0) {
                week = 7;
            }
        }
        bookInfo.setWeek(week-1);
        bookInfo.setEnable(1);
        mDTV.BookInfoUpdate(bookInfo);
    }

    private View.OnClickListener OkClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startBookTask();
        }
    };

    private View.OnClickListener CancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {
        Log.d(TAG, "onKeyDown: keyCode = " + keyCode);
        switch (keyCode)
        {
            case KeyEvent.KEYCODE_BACK:
            {
                Log.d(TAG, "onKeyDown: KEYCODE_BACK, mBookAlarm = " + mBookAlarm);
                mKeyback = true;
                break;
            }
            default:
            {
                break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
