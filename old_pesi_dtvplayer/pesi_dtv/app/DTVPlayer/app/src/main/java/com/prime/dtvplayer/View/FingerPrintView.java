package com.prime.dtvplayer.View;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.prime.dtvplayer.Activity.DTVActivity;
import com.prime.dtvplayer.R;

public class FingerPrintView {
    private final String TAG = getClass().getSimpleName();
    private Context mContext = null;
    private WindowManager mWindowManager = null;
    private WindowManager.LayoutParams mWindowFpParams = null;
    private CustomTextView mFingerPrint = null,mExitBtn = null;
    private String mFpText = null,mBtnText = null;
    private Handler Blinkhandler = null,Randomhandler = null,fingerPrinthandler = null;
    private Runnable fingerPrintrunnable = null;
    private int mFpFontSize = 0,mGravity = 0,mRandom = 0,mDuration = 0, mBlink = 0,
            mFocusType = 0,mExitBtnFlag = 0,mBtnFontSize = 0,mBtnTxtColor = 0,mBtnBgColor = 0,
            mBtnXGap = 0, mBtnYGap = 0, mBtnGravity = 0,BlinkInterval = 1000,RandomInterval = 1000;
    private boolean firstTimeNoUse = true;
    private FrameLayout mWindowFpFrameLayout;
    private int mFpTextWidth, mFpTextHeight;
    private int mBtnWidth, mBtnHeight;

    private int mTriggerID = 0, mTriggerNum = 0;
    private DTVActivity mdtv = null;

    //Blink runnable
    private Runnable BlinkRunnable = new Runnable() {
        int count = 0;
        @Override
        public void run() {
            if(mFingerPrint != null) {
                if ((count % 2) == 0) {
                    mFingerPrint.setVisibility(View.VISIBLE);
                    if(mExitBtn != null) {
                        mExitBtn.setVisibility(View.VISIBLE);
                        mExitBtn.requestFocus();    // Johnny 20180730 add for focus
                    }
                }
                else {
                    mFingerPrint.setVisibility(View.INVISIBLE);
                    if(mExitBtn != null)
                        mExitBtn.setVisibility(View.INVISIBLE);
                }
                count++;
                Blinkhandler.postDelayed(BlinkRunnable, BlinkInterval);
            }
        }
    };

    //Random runnable
    private Runnable RandomRunnable = new Runnable() {
        @Override
        public void run() {
            if(firstTimeNoUse) {
                firstTimeNoUse = false;
                Randomhandler.postDelayed(RandomRunnable, RandomInterval);
                return;
            }

            if(mFingerPrint != null) {
                Display display = mWindowManager.getDefaultDisplay();
                Point windowSize = new Point();
                display.getSize(windowSize);

                // Johnny 20180730 modify -s
                //random (x,y) with same dx and dy
                mFingerPrint.setX((int)(Math.random()* (windowSize.x-mFingerPrint.getWidth())+1));
                mFingerPrint.setY((int)(Math.random()* (windowSize.y-mFingerPrint.getHeight())+1));
                mFingerPrint.setTextColor(setRandomTextColor());
                mFingerPrint.setBackgroundColor(setRandomBgColor());

                if(mExitBtnFlag == 1) {
                    mExitBtn.setX(mFingerPrint.getX() + mBtnXGap);
                    mExitBtn.setY(mFingerPrint.getY() + mBtnYGap);
                }
                // Johnny 20180730 modify -e
            }
            Randomhandler.postDelayed(RandomRunnable, RandomInterval);
        }
    };

    public FingerPrintView(Context context, String text, int duration, int focusType, int fontSize,
                           int x, int y, int dx, int dy, int txt_color, int bg_color)
    {
        mContext = context;
        mFpText = text;
        mFocusType = focusType;
        mDuration = duration;
        mFpFontSize = fontSize;
        mFpTextWidth = dx;
        mFpTextHeight = dy;
        mWindowManager = ((Activity) context).getWindowManager();   // Johnny 20180730 add

        mWindowFpFrameLayout = new FrameLayout(mContext);   // Johnny 20180730 add
        InitBtnParams();
        InitWindowFpParams();
        InitFpTextView(txt_color,bg_color,x, y);
    }

    //show FingerPrint
    public void show()
    {
        // Johnny 20180730 modify -s
        remove();
        mWindowFpFrameLayout.addView(mFingerPrint);
        mFingerPrint.getLayoutParams().width = mFpTextWidth;
        mFingerPrint.getLayoutParams().height = mFpTextHeight;

        if(mExitBtnFlag == 1)
        {
            mWindowFpFrameLayout.addView(mExitBtn);
            mExitBtn.getLayoutParams().width = mBtnWidth;
            mExitBtn.getLayoutParams().height = mBtnHeight;
            mExitBtn.requestFocus();
        }
        // Johnny 20180730 modify -e

        if(mRandom == 1)
        {
            Randomhandler = new Handler();
            Randomhandler.post(RandomRunnable);
        }

        if(mBlink == 1) {
            Blinkhandler = new Handler();
            Blinkhandler.post(BlinkRunnable);
        }

        setDuration(mDuration);//set close FingerPrint Duration // Johnny 20180730 add
        mWindowManager.addView(mWindowFpFrameLayout, mWindowFpParams);  // Johnny 20180730 add fullscreen layout
    }

    //remove all
    public void remove()
    {
        removeAllView();
        removeAllHandle();
    }

    //set FingerPrint focus Type
    public void setFocusType(int focusType)
    {
        mFocusType = focusType;
        if(focusType == 0)
            mWindowFpParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        else
            mWindowFpParams.flags = 0;//focus
    }

    //set Exit Button
    public void setExitBtn(int x , int y , int dx, int dy, int fontsize, int gravity)
    {
        mExitBtnFlag = 1;
        mBtnFontSize = fontsize;
        mBtnGravity = gravity;
        // Johnny 20180730 modify -s
        mBtnWidth = dx;
        mBtnHeight = dy;

        InitBtnTextView();

        //set Exit botton location
        if((x + dx) > mFpTextWidth)
            mBtnXGap = mFpTextWidth - dx;
        else
            mBtnXGap = x;
        if((y + dy) > mFpTextHeight)
            mBtnYGap = mFpTextHeight - dy;
        else
            mBtnYGap = y;

        mExitBtn.setX(mFingerPrint.getX() + mBtnXGap);
        mExitBtn.setY(mFingerPrint.getY() + mBtnYGap);
        // Johnny 20180730 modify -e
    }



    //set Exit Button parameters
    public void setExitBtnParams(String text, int btnTxtColor, int btnBgColor, int gravity)
    {
        mBtnText = text;
        mBtnTxtColor = btnTxtColor;
        mBtnBgColor = btnBgColor;
        mBtnGravity = gravity;

        mExitBtn.setText(mBtnText);
        mExitBtn.setTextColor(mBtnTxtColor);
        mExitBtn.setBackgroundColor(mBtnBgColor);
        mExitBtn.setGravity(mBtnGravity);
    }

    //set FingerPrint Random
    public void setRandom(int random, int randomInterval)
    {
        mRandom = random;
        RandomInterval = randomInterval;
    }

    //set FingerPrint Text Gravity
    public void setFpTextGravity(int gravity)
    {
        mGravity = gravity;
        mFingerPrint.setGravity(gravity);
    }

    //Set FingerPrint Visibility
    public void setVisibility(boolean enable)
    {
        if(mFingerPrint != null) {
            if (enable)
                mFingerPrint.setVisibility(View.VISIBLE);
            else
                mFingerPrint.setVisibility(View.INVISIBLE);
        }
    }

    //Set FingerPrint Blink
    public void setBlink(int blink, int interval)
    {
        mBlink = blink;
        BlinkInterval = interval;
    }

    //set FingerPrint area by percent of the screen
    public void setPercentScreen(int percent)
    {
        Display display = mWindowManager.getDefaultDisplay();
        Point windowSize = new Point();
        display.getSize(windowSize);

        // Johnny 20180730 fixed width/height of percent
        mFpTextWidth = Math.round((float)Math.sqrt(windowSize.x*windowSize.x*percent/100));
        mFpTextHeight = Math.round((float)Math.sqrt(windowSize.y*windowSize.y*percent/100));

        if (mWindowFpFrameLayout.getVisibility() == View.VISIBLE) // has shown
        {
            show();
        }
    }

    // Johnny 20180730 added to let fingerprintView fit text size
    public void setFitTextSize()
    {
        mFpTextWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
        mFpTextHeight = ViewGroup.LayoutParams.WRAP_CONTENT;

        if (mWindowFpFrameLayout.getVisibility() == View.VISIBLE) // has shown
        {
            show();
        }
    }

    // Johnny 20180730 added for fingerprint text rotation
    public void setTextRotation(float degrees)
    {
        mFingerPrint.setRotation(degrees);
    }

    // Johnny 20180730 added to show fingerprint on top
    public void setToFront()
    {
        mWindowFpParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        if (mWindowFpFrameLayout.getVisibility() == View.VISIBLE) // has shown
        {
            show();
        }
    }

    // Johnny 20180730 added to show fingerprint behind other view
    public void setToBack()
    {
        mWindowFpParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA;
        if (mWindowFpFrameLayout.getVisibility() == View.VISIBLE) // has shown
        {
            show();
        }
    }

    //Init Exit Button params
    private void InitBtnParams()
    {
        mBtnText = mContext.getString(R.string.STR_EXIT);
        mBtnTxtColor = Color.argb(255, 255, 255, 255);
        mBtnBgColor = Color.argb(255, 0, 0, 0);
        mBtnGravity = Gravity.CENTER;
        mBtnFontSize = 22;
    }

    private void InitBtnTextView()
    {
        mExitBtn = new CustomTextView(mContext,mBtnFontSize);// 0: auto set font size ; 1.2.3... : set user font size setting
        mExitBtn.setText(mBtnText);
        mExitBtn.setTextSize(mBtnFontSize);
        mExitBtn.setTextColor(mBtnTxtColor);
        mExitBtn.setBackgroundColor(mBtnBgColor);
        mExitBtn.setGravity(mBtnGravity);
        mExitBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                remove();
            }
        });
    }

    //Init WindowFpParams params
    private void InitWindowFpParams()
    {
//        mWindowManager = (WindowManager) mContext.getApplicationContext().getSystemService(   // Johnny 20180730 comment
//                Context.WINDOW_SERVICE);

        mWindowFpParams = new WindowManager.LayoutParams();
        mWindowFpParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

        if(mFocusType == 0)
            mWindowFpParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        else
            mWindowFpParams.flags = 0;

        mWindowFpParams.width = WindowManager.LayoutParams.MATCH_PARENT;    // Johnny 20180730 modify for rotation
        mWindowFpParams.height = WindowManager.LayoutParams.MATCH_PARENT;   // Johnny 20180730 modify for rotation
        mWindowFpParams.gravity = Gravity.CENTER;
        mWindowFpParams.format = PixelFormat.RGBA_8888;

    }

    //Init FingerPrint CustomTextView
    private void InitFpTextView(int txt_color, int bg_color,int x, int y)
    {
        mFingerPrint = new CustomTextView(mContext, mFpFontSize);
        mFingerPrint.setBackgroundColor(bg_color);
        mFingerPrint.setTextColor(txt_color);
        mFingerPrint.setText(mFpText);
        mFingerPrint.setGravity(mGravity);
        mFingerPrint.setX(x);
        mFingerPrint.setY(y);
        setFocusType(mFocusType);

        mFingerPrint.setFocusable(true);
        mFingerPrint.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode)
                    {
                        case KeyEvent.KEYCODE_BACK:
                        {
                            remove();
                        }break;
                    }
                }
                return false;
            }
        });
    }

    //set Random text color
    private int setRandomTextColor()
    {
        int text_alpha = (int)(Math.random()* (255)+1);
        int text_red = (int)(Math.random()* (255)+1);
        int text_green = (int)(Math.random()* (255)+1);
        int text_blue = (int)(Math.random()* (255)+1);

        return Color.argb(text_alpha, text_red, text_green, text_blue);
    }

    //set Random background color
    private int setRandomBgColor()
    {
        int bg_alpha = (int)(Math.random()* (255)+1);
        int bg_red = (int)(Math.random()* (255)+1);
        int bg_green = (int)(Math.random()* (255)+1);
        int bg_blue = (int)(Math.random()* (255)+1);

        return Color.argb(bg_alpha, bg_red, bg_green, bg_blue);
    }

    //set FingerPrint duration
    private void setDuration(int duration)
    {
        mDuration = duration;
        if(duration != 0) {
            fingerPrinthandler = new Handler();
            fingerPrintrunnable = new Runnable() {
                @Override
                public void run() {
                    remove();
                    FingerPrintFinish();
                }
            };
            fingerPrinthandler.postDelayed(fingerPrintrunnable, duration);
        }
    }

    //remove all views
    private void removeAllView()
    {
        try {
            mWindowManager.removeView(mWindowFpFrameLayout);
            mWindowFpFrameLayout.removeAllViews();
        } catch (Exception e) {
            Log.d(TAG, "removeAllView: ");
        }
    }

    //remove all handlers
    private void removeAllHandle()
    {
        if(Blinkhandler != null) {
            Blinkhandler.removeCallbacks(BlinkRunnable);
        }

        if(fingerPrinthandler != null) {
            fingerPrinthandler.removeCallbacks(fingerPrintrunnable);
        }

        if(Randomhandler != null) {
            Randomhandler.removeCallbacks(RandomRunnable);
        }
    }

    public void SetTriggerInfo(DTVActivity dtv, int triggerID, int triggerNum)
    {
        mdtv = dtv;
        mTriggerID = triggerID;
        mTriggerNum = triggerNum;
    }

    public void FingerPrintFinish()
    {
        if(mdtv != null ) {
            Log.d(TAG, "FingerPrintFinish  !!!!!!!   mTriggerID="+ mTriggerID + "   mTriggerNum =" + mTriggerNum);
            mdtv.VMXOsmFinish(mTriggerID, mTriggerNum);
            mTriggerID = 0;
            mTriggerNum = 0;
        }
    }
}
